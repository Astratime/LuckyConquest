package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.controllers.GameController;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.WearCards;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.loaders.CardLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Les cartes à usure de la boutique : Bougie, Graine et Pile ou face (Astra, 2026-11-14). */
class WearCardsTest {

    private static final Path ASSETS = Path.of("..", "assets");
    private static final CardLoader.AssetReader READER = path -> {
        try {
            return Files.readString(ASSETS.resolve(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    };
    private static final java.util.function.Function<String, Card> CARDS = CardLoader.cardFactory(READER);

    private static Card card(String id) {
        return java.util.Objects.requireNonNull(CARDS.apply(id), id);
    }

    @AfterEach
    void fairCoin() { CoinFlipEffect.coin = () -> java.util.concurrent.ThreadLocalRandom.current().nextBoolean(); }

    /** @return le bonus d'attaque (en %) que la pose de {@code card} met en attente pour le tirage */
    private static int played(Card card, Player player) {
        PlayContext context = new PlayContext(player);
        for (Effect effect : card.getEffects()) effect.onPlay(context);
        return context.getEffectsForSpin().stream().filter(WornAttack.class::isInstance)
            .mapToInt(effect -> Integer.parseInt(effect.getDescription().replaceAll("\\D", ""))).sum();
    }

    @Test
    void theThreeCardsAreSoldInTheShopWithAPlusVersion() {
        for (String id : List.of("bougie", "graine", "pile_ou_face")) {
            assertTrue(card(id).isUpgradable(), id);
            assertTrue(CardLoader.loadBoutique(READER).containsKey(id), id);
        }
        assertEquals(225, played(card("bougie+"), new Player("J", 1000, List.of())), "Bougie + : 150 % x1,5");
    }

    @Test
    void theCandleMeltsTwentyPercentEachTimeItIsPlayed() {
        Player player = new Player("J", 1000, List.of());
        Card candle = card("bougie");
        assertEquals(150, played(candle, player));
        // (la même carte, rejouée après être revenue de la défausse)
        assertEquals(120, player.getLastingEffects().getWearCards().getNextCandle());
        assertEquals(120, played(candle, player));
        assertEquals(96, played(candle, player));
        assertEquals(77, played(candle, player));
        assertEquals(4, player.getLastingEffects().getWearCards().getCandlePlays());
        assertEquals(150, played(candle, new Player("J", 1000, List.of())), "un nouveau combat, une Bougie neuve");
    }

    @Test
    void theSeedGrowsEachTurnItIsNotPlayedThenStartsAgain() {
        WearCards wear = new WearCards();
        for (int turn = 0; turn < 3; turn++) wear.growSeed(20, 25, 300);
        assertEquals(95, wear.getSeedBonus());
        assertEquals(95, wear.harvestSeed(20, 25, 300));
        wear.growSeed(20, 25, 300); // jouée à ce tour : elle ne pousse pas
        assertEquals(20, wear.getSeedBonus());
        for (int turn = 0; turn < 30; turn++) wear.growSeed(20, 25, 300);
        assertEquals(300, wear.getSeedBonus(), "300 % au plus");
        assertTrue(wear.isSeedRipe());
    }

    @Test
    void headsDoublesTheCoinAndTailsDoesNothing() {
        Player player = new Player("J", 1000, List.of());
        CoinFlipEffect.coin = () -> true;
        assertEquals(200, played(card("pile_ou_face"), player));
        CoinFlipEffect.coin = () -> false;
        assertEquals(0, played(card("pile_ou_face"), player));
    }

    @Test
    void inAFightTheSeedGrowsWhileItWaitsInTheDeck() {
        List<Card> deck = new ArrayList<>();
        deck.add(card("graine"));
        for (int i = 0; i < 9; i++) deck.add(new Card("c" + i, "c" + i, "x.png", List.of(), null, 1));
        GameController controller = new GameController(() -> new ArrayList<>(deck), CARDS, Map.of());
        Player player = controller.getGameState().getPlayer();
        WearCards wear = player.getLastingEffects().getWearCards();
        for (int turn = 0; turn < 2; turn++) {
            controller.drawCards(); // si la Graine est piochée, elle reste sur la table
            controller.spin();
        }
        assertEquals(2, wear.getSeedTurns());
        assertEquals(70, wear.getSeedBonus());
    }
}
