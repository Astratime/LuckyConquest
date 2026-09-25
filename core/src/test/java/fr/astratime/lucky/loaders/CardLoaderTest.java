package fr.astratime.lucky.loaders;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.context.PlayContext;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Charge les vrais fichiers JSON de assets/ (sans libGDX) : toute faute de frappe
 * dans une définition de carte, un type d'effet inconnu, une image manquante ou
 * un id inconnu dans le deck de départ fait échouer ces tests.
 */
class CardLoaderTest {

    /** Les tests du module core s'exécutent depuis core/ : les assets sont à côté. */
    private static final Path ASSETS = Path.of("..", "assets");

    private static final CardLoader.AssetReader READER = path -> {
        try {
            return Files.readString(ASSETS.resolve(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    };

    @Test
    void everyCardDefinitionLoads() {
        List<Card> cards = CardLoader.loadAll(READER);

        assertEquals(52 + 18, cards.size(), "4 suites de 13 cartes + 18 cartes spéciales");
        assertEquals(cards.size(), cards.stream().map(Card::getId).distinct().count(), "les ids doivent être uniques");
    }

    @Test
    void everyCardImageExists() {
        for (Card card : CardLoader.loadAll(READER)) {
            assertTrue(Files.exists(ASSETS.resolve(card.getAssetPath())),
                card.getId() + " : image introuvable " + card.getAssetPath());
        }
    }

    @Test
    void everyCardShowsSomethingWhenPlayed() {
        for (Card card : CardLoader.loadAll(READER)) {
            assertFalse(card.getEffects().isEmpty(), card.getId() + " n'a aucun effet");
            PlayContext context = new PlayContext(new Player("Joueur", 100, List.of()));
            card.getEffects().forEach(effect -> effect.onPlay(context));
            assertFalse(context.getPopups().isEmpty(), card.getId() + " n'affiche aucun texte quand elle est jouée");
        }
    }

    @Test
    void starterDeckHasTheExpectedComposition() {
        List<Card> deck = CardLoader.loadStarterDeck(READER);
        Map<String, Long> copies = deck.stream().collect(Collectors.groupingBy(Card::getId, Collectors.counting()));

        assertEquals(26, deck.size());
        for (String special : List.of("magnet", "joker", "recycle", "bet", "lucky_charm", "rainbow")) {
            assertEquals(1L, copies.get(special), special);
        }
        assertEquals(2L, copies.get("draw_2"));
        assertEquals(1L, copies.get("draw_3"));
        assertEquals(1L, copies.get("gain_500"));
        for (String suit : List.of("coeur", "trefle", "carreau", "pique")) {
            for (int rank : List.of(1, 11, 12, 13)) {
                assertEquals(1L, copies.get(rank + "_" + suit), rank + "_" + suit);
            }
        }
    }

    @Test
    void eachCopyInTheStarterDeckIsADistinctCard() {
        List<Card> deck = CardLoader.loadStarterDeck(READER);
        Map<Card, Boolean> unique = new IdentityHashMap<>();
        deck.forEach(card -> unique.put(card, true));

        assertEquals(deck.size(), unique.size(), "deux exemplaires ne doivent pas partager la même instance");
    }

    @Test
    void unknownIdInTheStarterDeckIsRejected() {
        CardLoader.AssetReader withBadDeck = path -> path.endsWith("starter.json")
            ? "[ { \"id\": \"carte_inexistante\", \"copies\": 1 } ]"
            : READER.read(path);

        assertThrows(IllegalArgumentException.class, () -> CardLoader.loadStarterDeck(withBadDeck));
    }

    @Test
    void unknownEffectTypeIsRejected() {
        CardLoader.AssetReader withBadEffect = path -> path.endsWith("special.json")
            ? "[ { \"id\": \"x\", \"name\": \"X\", \"assetPath\": \"x.png\", \"effects\": [ { \"type\": \"INCONNU\" } ] } ]"
            : READER.read(path);

        assertThrows(IllegalArgumentException.class, () -> CardLoader.loadAll(withBadEffect));
    }

    @Test
    void potDeLutinIsAConsumableCardOutsideTheStarterDeck() {
        Card pot = CardLoader.cardFactory(READER).apply("pot_de_lutin");

        assertTrue(pot.isConsumable());
        assertTrue(CardLoader.loadStarterDeck(READER).stream().noneMatch(c -> c.getId().equals("pot_de_lutin")));
    }

    @Test
    void shopSellsConsumableCardsThatAreNotInTheStarterDeck() {
        Map<String, Integer> shop = CardLoader.loadShop(READER);
        Map<String, Integer> expected = new java.util.LinkedHashMap<>();
        expected.put("bingo", 10000);
        expected.put("russian_roulette", 8000);
        expected.put("combo_full", 500);
        expected.put("combo_brelan", 1000);
        expected.put("combo_suite", 1000);
        expected.put("combo_paire", 1200);
        expected.put("combo_couleur", 1000);
        expected.put("corruption", 5000);
        assertEquals(List.copyOf(expected.entrySet()), List.copyOf(shop.entrySet()), "prix et ordre de l'échoppe");

        List<String> starter = CardLoader.loadStarterDeck(READER).stream().map(Card::getId).toList();
        for (String id : shop.keySet()) {
            assertTrue(CardLoader.cardFactory(READER).apply(id).isConsumable(), id + " doit être consommable");
            assertFalse(starter.contains(id), id + " ne s'obtient qu'à l'échoppe");
        }
    }
}
