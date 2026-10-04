package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.RankBonus;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.choices.RouletteChoice;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.effects.BingoEffect;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.PistolShotEvent;
import fr.astratime.lucky.loaders.CardLoader;
import fr.astratime.lucky.progress.Rank;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Référence des dégâts d'un gros combo, joué comme en vrai (vraies cartes des
 * JSON, GameController), sans rang puis avec chaque rang : As de Trèfle, As de
 * Pique, Roulette russe gagnante, Corruption, puis la carte « Bingo » qui tire
 * un Bingo Triple Sept (x10). Le Bingo est joué en dernier : il lance la machine tout seul. Cinq
 * cartes, soit un coup de plus que la limite de 4 (comme avec Dans la manche).
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

    /**
     * Chaque Triple Sept frappe de (90 + attaque du rang + 20 par Lame + bonus de
     * l'As de Trèfle) x (1 + 0,5 par Lame) x 2 (Corruption) x 4,5 (Paire des deux As)
     * x 10 (Bingo) ; le pistolet tire ensuite l'attaque du symbole avant ces
     * multiplicateurs, x50. AUCUN : pas de rang.
     */
    @ParameterizedTest(name = "{0} gains, rang {1} : {8} dégâts")
    @CsvSource({
        // gains, rang, As de Trèfle (consommé, attaque), As de Pique (consommé, Lames), par Triple Sept, pistolet, total
        "10000, AUCUN, 3000, 663, 700, 2, 142740, 39650, 467870",
        "10000, AVARE, 3000, 663, 700, 2, 160740, 44650, 526870",
        "10000, PARIEUR, 3000, 663, 700, 2, 178740, 49650, 585870",
        "10000, FLAMBEUR, 3000, 663, 700, 2, 205740, 57150, 674370",
        "10000, HABITUE, 3000, 663, 700, 2, 232740, 64650, 762870",
        "10000, REQUIN, 3000, 663, 700, 2, 277740, 77150, 910370",
        "10000, GROS_BONNET, 3000, 663, 700, 2, 322740, 89650, 1057870",
        "10000, BARON, 3000, 663, 700, 2, 394740, 109650, 1293870",
        "10000, MAGNAT, 3000, 663, 700, 2, 502740, 139650, 1647870",
        "10000, ROI_DU_CASINO, 3000, 663, 700, 2, 646740, 179650, 2119870",
        "10000, LEGENDE, 3000, 663, 700, 2, 862740, 239650, 2827870",
        "100000, AUCUN, 30000, 1847, 7000, 5, 641655, 101850, 2026815",
        "100000, AVARE, 30000, 1847, 7000, 5, 673155, 106850, 2126315",
        "100000, PARIEUR, 30000, 1847, 7000, 5, 704655, 111850, 2225815",
        "100000, FLAMBEUR, 30000, 1847, 7000, 5, 751905, 119350, 2375065",
        "100000, HABITUE, 30000, 1847, 7000, 5, 799155, 126850, 2524315",
        "100000, REQUIN, 30000, 1847, 7000, 5, 877905, 139350, 2773065",
        "100000, GROS_BONNET, 30000, 1847, 7000, 5, 956655, 151850, 3021815",
        "100000, BARON, 30000, 1847, 7000, 5, 1082655, 171850, 3419815",
        "100000, MAGNAT, 30000, 1847, 7000, 5, 1271655, 201850, 4016815",
        "100000, ROI_DU_CASINO, 30000, 1847, 7000, 5, 1523655, 241850, 4812815",
        "100000, LEGENDE, 30000, 1847, 7000, 5, 1901655, 301850, 6006815",
        "1000000, AUCUN, 300000, 5592, 70000, 5, 1821330, 289100, 5753090",
        "1000000, AVARE, 300000, 5592, 70000, 5, 1852830, 294100, 5852590",
        "1000000, PARIEUR, 300000, 5592, 70000, 5, 1884330, 299100, 5952090",
        "1000000, FLAMBEUR, 300000, 5592, 70000, 5, 1931580, 306600, 6101340",
        "1000000, HABITUE, 300000, 5592, 70000, 5, 1978830, 314100, 6250590",
        "1000000, REQUIN, 300000, 5592, 70000, 5, 2057580, 326600, 6499340",
        "1000000, GROS_BONNET, 300000, 5592, 70000, 5, 2136330, 339100, 6748090",
        "1000000, BARON, 300000, 5592, 70000, 5, 2262330, 359100, 7146090",
        "1000000, MAGNAT, 300000, 5592, 70000, 5, 2451330, 389100, 7743090",
        "1000000, ROI_DU_CASINO, 300000, 5592, 70000, 5, 2703330, 429100, 8539090",
        "1000000, LEGENDE, 300000, 5592, 70000, 5, 3081330, 489100, 9733090",
    })
    void asTrefleAsPiqueRouletteCorruptionEtBingoTripleSept(int gains, String rank, int clubsConsumed,
                                                           int clubsAttack, int spadesConsumed, int blades,
                                                           long perSymbol, long pistol, long total)
            throws Exception {
        RankBonus rankBonus = "AUCUN".equals(rank) ? RankBonus.NONE : Rank.valueOf(rank).getBonus();
        // La carte « Bingo » tire son symbole au hasard : ici, on impose le Triple Sept.
        Card bingo = new Card("bingo", "Bingo", "cards/special/bingo.png",
            List.of(new BingoEffect(10, Symbol.TRIPLE_SEVEN)), null, 0, true);
        List<Card> deck = List.of(CARDS.apply("1_trefle"), CARDS.apply("1_pique"),
            CARDS.apply("russian_roulette"), CARDS.apply("corruption"), bingo);
        GameController controller = new GameController(() -> new ArrayList<>(deck), CARDS, Map.of(),
            cards -> new Player("Joueur", Player.BASE_HP, cards, rankBonus, Symbol.classicReels()));
        Player player = controller.getGameState().getPlayer();
        // Une cible sans défense, avec le plus de PV possible : elle encaisse tous les coups.
        setGameState(controller, new GameState(player, new Enemy("Cible", Integer.MAX_VALUE)));
        player.addGains(gains);
        player.getLastingEffects().addBonusPlays(1);
        controller.drawCards();

        play(controller, "1_trefle");
        assertEquals(gains - clubsConsumed, player.getGains(), "As de Trèfle : -30 % des gains");
        play(controller, "1_pique");
        assertEquals(gains - clubsConsumed - spadesConsumed, player.getGains(), "As de Pique : -10 % des gains");
        assertEquals(blades, player.getLastingEffects().getBlades(), "une Lame par 250 gains consommés, 5 au plus");
        play(controller, "russian_roulette");
        RouletteChoice roulette = (RouletteChoice) controller.getPendingChoice();
        assertFalse(controller.pickRouletteCard(roulette.cursed().indexOf(false)).cursed(), "Roulette gagnante");
        play(controller, "corruption");
        play(controller, "bingo");

        TurnResult turn = controller.spin();
        assertArrayEquals(new Symbol[] {Symbol.TRIPLE_SEVEN, Symbol.TRIPLE_SEVEN, Symbol.TRIPLE_SEVEN},
            turn.getSymbols());

        assertEquals(clubsAttack, 115 + Math.round(10f * (float) Math.sqrt(clubsConsumed)), "attaque de l'As de Trèfle");
        long base = 90 + rankBonus.attack() + blades * PreparationResolver.BLADE_ATTACK + clubsAttack;
        assertEquals(perSymbol, Math.round(base * (1 + 0.5 * blades) * 2 * 4.5 * 10));

        List<EnemyDamagedEvent> hits = symbolHits(turn);
        assertEquals(3, hits.size());
        for (EnemyDamagedEvent hit : hits) {
            assertEquals(perSymbol, hit.damage);
            assertEquals(0, hit.blocked, "l'As de Pique ignore la défense");
        }

        List<PistolShotEvent> shots = turn.getPistolEvents().stream()
            .filter(PistolShotEvent.class::isInstance).map(PistolShotEvent.class::cast).toList();
        assertEquals(1, shots.size());
        assertEquals(50, shots.get(0).multiplier);
        assertEquals(base * 50, pistol, "pistolet : attaque du symbole avant les multiplicateurs, x50");
        assertEquals(pistol, shots.get(0).damage);

        assertEquals(total, hits.stream().mapToLong(hit -> hit.damage).sum() + shots.get(0).damage);
    }

    @Test
    void unCoupNeDepassePasLesPvMaxDeLEnnemi() {
        Player player = new Player("Joueur", 100, List.of());
        CombatContext context = new CombatContext(player, new Enemy("Ennemi", 1_000_000));
        context.multiplySymbolPower(1_000_000); // 90 x 1 000 000 : 90 millions de dégâts
        context.addPistolShot(100_000);          // 90 x 100 000 : 9 millions
        Symbol[] symbols = {Symbol.TRIPLE_SEVEN, null, null};
        TurnResult turn = new CombatResolver().resolve(context, new ActionResolver().resolve(symbols), symbols,
            List.of());
        EnemyDamagedEvent hit = symbolHits(turn).get(0);
        assertEquals(1_000_000, hit.rawDamage, "plafonné aux PV max");
        assertTrue(context.getEnemy().isDefeated());
    }

    @Test
    void lePistoletTireLAttaqueAvantLesMultiplicateurs() {
        Player player = new Player("Joueur", 100, List.of());
        CombatContext context = new CombatContext(player, new Enemy("Ennemi", 1_000_000));
        context.multiplySymbolPower(10);
        context.addPistolShot(50);
        Symbol[] symbols = {Symbol.TRIPLE_SEVEN, null, null};
        TurnResult turn = new CombatResolver().resolve(context, new ActionResolver().resolve(symbols), symbols,
            List.of());
        assertEquals(900, symbolHits(turn).get(0).rawDamage, "Triple Sept 90 x10");
        PistolShotEvent shot = (PistolShotEvent) turn.getPistolEvents().get(0);
        assertEquals(90 * 50, shot.rawDamage, "le pistolet ignore le x10 du Bingo");
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
