package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.choices.RouletteChoice;
import fr.astratime.lucky.entities.effects.BingoEffect;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.PistolShotEvent;
import fr.astratime.lucky.loaders.CardLoader;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Dégâts d'un gros combo, joué comme en vrai (vraies cartes des JSON, GameController) :
 * 1 000 000 de gains, As de Trèfle, As de Pique, Roulette russe gagnante,
 * Corruption, puis la carte « Bingo » qui tire un Bingo Triple Sept (x100).
 * Le Bingo est joué en dernier : il lance la machine tout seul.
 */
class ComboDamageTest {

    /** Les tests du module core s'exécutent depuis core/ : les assets sont à côté. */
    private static final Path ASSETS = Path.of("..", "assets");

    private static final Function<String, Card> CARDS = CardLoader.cardFactory(path -> {
        try {
            return Files.readString(ASSETS.resolve(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    });

    @Test
    void asTrefleAsPiqueRouletteCorruptionEtBingoTripleSept() throws Exception {
        // La carte « Bingo » tire son symbole au hasard : ici, on impose le Triple Sept.
        Card bingo = new Card("bingo", "Bingo", "cards/special/bingo.png",
            List.of(new BingoEffect(100, Symbol.TRIPLE_SEVEN)), null, 0, true);
        List<Card> deck = List.of(CARDS.apply("1_trefle"), CARDS.apply("1_pique"),
            CARDS.apply("russian_roulette"), CARDS.apply("corruption"), bingo);
        GameController controller = new GameController(() -> new ArrayList<>(deck));
        Player player = controller.getGameState().getPlayer();
        // Une cible sans défense et avec assez de PV pour encaisser tous les coups.
        setGameState(controller, new GameState(player, new Enemy("Cible", Integer.MAX_VALUE)));
        player.addGains(1_000_000);
        player.getLastingEffects().addBonusPlays(1); // 5 cartes : une de plus que la limite de 4
        controller.drawCards();

        play(controller, "1_trefle");
        assertEquals(700_000, player.getGains(), "As de Trèfle : -30 % des gains (300 000)");
        play(controller, "1_pique");
        assertEquals(630_000, player.getGains(), "As de Pique : -10 % des gains (70 000)");
        assertEquals(5, player.getLastingEffects().getBlades(), "70 000 gains : 5 Lames (le maximum)");
        play(controller, "russian_roulette");
        RouletteChoice roulette = (RouletteChoice) controller.getPendingChoice();
        assertFalse(controller.pickRouletteCard(roulette.cursed().indexOf(false)).cursed(), "Roulette gagnante");
        play(controller, "corruption");
        play(controller, "bingo");

        TurnResult turn = controller.spin();
        assertArrayEquals(new Symbol[] {Symbol.TRIPLE_SEVEN, Symbol.TRIPLE_SEVEN, Symbol.TRIPLE_SEVEN},
            turn.getSymbols());

        // Attaque de chaque Triple Sept : (90 de base + 5 Lames x 20 + bonus de l'As de Trèfle)
        // x 3,5 (As de Pique, 5 Lames) x 10 (Corruption) x 4,5 (Paire des deux As) x 100 (Bingo).
        int clubsBonus = 115 + Math.round(10f * (float) Math.sqrt(300_000)); // 5 592
        int base       = 90 + 5 * PreparationResolver.BLADE_ATTACK + clubsBonus;  // 5 782
        assertEquals(5_782, base);
        long perSymbol = Math.round(base * 3.5 * 10 * 4.5 * 100);                // 91 066 500
        assertEquals(91_066_500L, perSymbol);

        List<EnemyDamagedEvent> hits = symbolHits(turn);
        assertEquals(3, hits.size());
        for (EnemyDamagedEvent hit : hits) {
            assertEquals(perSymbol, hit.damage, 8, "arrondi des flottants près");
            assertEquals(0, hit.blocked, "l'As de Pique ignore la défense");
        }

        // Pistolet x50 sur le meilleur symbole : 91 066 496 x 50 = 4 553 324 800,
        // plafonné au maximum d'un entier (les PV des ennemis n'iront jamais plus loin).
        List<PistolShotEvent> shots = turn.getPistolEvents().stream()
            .filter(PistolShotEvent.class::isInstance).map(PistolShotEvent.class::cast).toList();
        assertEquals(1, shots.size());
        assertEquals(50, shots.get(0).multiplier);
        assertEquals(Integer.MAX_VALUE, shots.get(0).rawDamage);

        long total = hits.stream().mapToLong(hit -> hit.damage).sum() + shots.get(0).damage;
        assertEquals(273_199_488L + Integer.MAX_VALUE, total);
    }

    private static void play(GameController controller, String id) {
        Card card = controller.getGameState().getPlayer().getCurrentHand().stream()
            .filter(c -> c.getId().equals(id)).findFirst().orElseThrow();
        assertNull(controller.unplayableReason(card), id);
        controller.playCard(card);
    }

    /** @return les coups des symboles (sans le pistolet). */
    private static List<EnemyDamagedEvent> symbolHits(TurnResult turn) {
        List<EnemyDamagedEvent> hits = new ArrayList<>();
        for (Event event : turn.getEvents()) {
            if (event instanceof EnemyDamagedEvent hit && !(event instanceof PistolShotEvent)) hits.add(hit);
        }
        return hits;
    }

    /** Remplace l'ennemi du combat par une cible d'entraînement (aucun accès public pour ça). */
    private static void setGameState(GameController controller, GameState state) throws Exception {
        Field field = GameController.class.getDeclaredField("gameState");
        field.setAccessible(true);
        field.set(controller, state);
    }
}
