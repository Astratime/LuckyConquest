package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.RankBonus;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.choices.RouletteChoice;
import fr.astratime.lucky.entities.enemy.EnemyKind;
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
 * un Bingo Triple Sept (x5). Le Bingo est joué en dernier : il lance la machine tout seul. Cinq
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
     * x 5 (Bingo) ; le pistolet rejoue ensuite le coup le plus fort du tour, tel
     * qu'il a frappé, x4 (Roulette gagnante). AUCUN : pas de rang.
     */
    @ParameterizedTest(name = "{0} gains, rang {1} : {8} dégâts")
    @CsvSource({
        // gains, rang, As de Trèfle (consommé, attaque), As de Pique (consommé, Lames), par Triple Sept, pistolet, total
        "10000, AUCUN, 3000, 663, 500, 2, 71370, 285480, 499560",
        "10000, AVARE, 3000, 663, 500, 2, 80370, 321480, 562560",
        "10000, PARIEUR, 3000, 663, 500, 2, 89370, 357480, 625560",
        "10000, FLAMBEUR, 3000, 663, 500, 2, 102870, 411480, 720060",
        "10000, HABITUE, 3000, 663, 500, 2, 116370, 465480, 814560",
        "10000, REQUIN, 3000, 663, 500, 2, 179370, 717480, 1255560",
        "10000, GROS_BONNET, 3000, 663, 500, 2, 192870, 771480, 1350060",
        "10000, BARON, 3000, 663, 500, 2, 269370, 1077480, 1885560",
        "10000, MAGNAT, 3000, 663, 500, 2, 323370, 1293480, 2263560",
        "10000, ROI_DU_CASINO, 3000, 663, 500, 2, 566370, 2265480, 3964560",
        "10000, LEGENDE, 3000, 663, 500, 2, 701370, 2805480, 4909560",
        "100000, AUCUN, 30000, 1847, 2500, 10, 576990, 2307960, 4038900",
        "100000, AVARE, 30000, 1847, 2500, 10, 603990, 2415960, 4227900",
        "100000, PARIEUR, 30000, 1847, 2500, 10, 630990, 2523960, 4416900",
        "100000, FLAMBEUR, 30000, 1847, 2500, 10, 671490, 2685960, 4700400",
        "100000, HABITUE, 30000, 1847, 2500, 10, 711990, 2847960, 4983900",
        "100000, REQUIN, 30000, 1847, 2500, 10, 900990, 3603960, 6306900",
        "100000, GROS_BONNET, 30000, 1847, 2500, 10, 941490, 3765960, 6590400",
        "100000, BARON, 30000, 1847, 2500, 10, 1170990, 4683960, 8196900",
        "100000, MAGNAT, 30000, 1847, 2500, 10, 1332990, 5331960, 9330900",
        "100000, ROI_DU_CASINO, 30000, 1847, 2500, 10, 2061990, 8247960, 14433900",
        "100000, LEGENDE, 30000, 1847, 2500, 10, 2466990, 9867960, 17268900",
        "1000000, AUCUN, 300000, 5592, 2500, 10, 1588140, 6352560, 11116950",
        "1000000, AVARE, 300000, 5592, 2500, 10, 1615140, 6460560, 11305950",
        "1000000, PARIEUR, 300000, 5592, 2500, 10, 1642140, 6568560, 11494950",
        "1000000, FLAMBEUR, 300000, 5592, 2500, 10, 1682640, 6730560, 11778450",
        "1000000, HABITUE, 300000, 5592, 2500, 10, 1723140, 6892560, 12061950",
        "1000000, REQUIN, 300000, 5592, 2500, 10, 1912140, 7648560, 13384950",
        "1000000, GROS_BONNET, 300000, 5592, 2500, 10, 1952640, 7810560, 13668450",
        "1000000, BARON, 300000, 5592, 2500, 10, 2182140, 8728560, 15274950",
        "1000000, MAGNAT, 300000, 5592, 2500, 10, 2344140, 9376560, 16408950",
        "1000000, ROI_DU_CASINO, 300000, 5592, 2500, 10, 3073140, 12292560, 21511950",
        "1000000, LEGENDE, 300000, 5592, 2500, 10, 3478140, 13912560, 24346950",
    })
    void asTrefleAsPiqueRouletteCorruptionEtBingoTripleSept(int gains, String rank, int clubsConsumed,
                                                           int clubsAttack, int spadesConsumed, int blades,
                                                           long perSymbol, long pistol, long total)
            throws Exception {
        RankBonus rankBonus = "AUCUN".equals(rank) ? RankBonus.NONE : Rank.valueOf(rank).getBonus();
        // La carte « Bingo » tire son symbole au hasard : ici, on impose le Triple Sept.
        Card bingo = new Card("bingo", "Bingo", "cards/special/bingo.png",
            List.of(new BingoEffect(5, Symbol.TRIPLE_SEVEN)), null, 0, true);
        List<Card> deck = List.of(CARDS.apply("1_trefle"), CARDS.apply("1_pique"),
            CARDS.apply("russian_roulette"), CARDS.apply("corruption"), bingo);
        GameController controller = new GameController(() -> new ArrayList<>(deck), CARDS, Map.of(),
            cards -> new Player("Joueur", Player.BASE_HP, cards, rankBonus, Symbol.classicReels()));
        Player player = controller.getGameState().getPlayer();
        // Une cible à la défense de base, avec le plus de PV possible : elle encaisse tous les coups.
        setGameState(controller, new GameState(player, new Enemy("Cible", Integer.MAX_VALUE)));
        player.addGains(gains);
        player.getLastingEffects().addBonusPlays(1);
        controller.drawCards();

        play(controller, "1_trefle");
        assertEquals(gains - clubsConsumed, player.getGains(), "As de Trèfle : -30 % des gains, 1 000 000 au plus");
        play(controller, "1_pique");
        assertEquals(gains - clubsConsumed - spadesConsumed, player.getGains(), "As de Pique : -10 % des gains, seulement ce qui achète des Lames");
        assertEquals(blades, player.getLastingEffects().getBlades(), "une Lame par 2,5 tirages (250 gains), 10 au plus");
        play(controller, "russian_roulette");
        RouletteChoice roulette = (RouletteChoice) controller.getPendingChoice();
        assertFalse(controller.pickRouletteCard(roulette.cursed().indexOf(false)).cursed(), "Roulette gagnante");
        play(controller, "corruption");
        play(controller, "bingo");

        TurnResult turn = controller.spin();
        assertArrayEquals(new Symbol[] {Symbol.TRIPLE_SEVEN, Symbol.TRIPLE_SEVEN, Symbol.TRIPLE_SEVEN},
            turn.getSymbols());

        assertEquals(clubsAttack, 115 + Math.min(10_000, Math.round(10f * (float) Math.sqrt(clubsConsumed))),
            "attaque de l'As de Trèfle : la racine des gains consommés, +10 000 au plus");
        long base = 90 + rankBonus.attack() + blades * PreparationResolver.BLADE_ATTACK + clubsAttack;
        assertEquals(perSymbol, Math.round(base * (1 + 0.5 * blades) * 2 * 4.5 * 5));

        List<EnemyDamagedEvent> hits = symbolHits(turn);
        assertEquals(3, hits.size());
        // L'As de Pique perce 80 % de chaque coup : les 20 % restants usent la défense de base
        // de la cible (30) dès le premier coup ; les suivants passent en entier.
        assertEquals(Enemy.BASE_DEFENSE, hits.get(0).blocked, "le premier coup use la défense");
        assertEquals(perSymbol - Enemy.BASE_DEFENSE, hits.get(0).damage);
        for (EnemyDamagedEvent hit : hits.subList(1, hits.size())) {
            assertEquals(perSymbol, hit.damage);
            assertEquals(0, hit.blocked, "la défense est usée");
        }

        List<PistolShotEvent> shots = turn.getPistolEvents().stream()
            .filter(PistolShotEvent.class::isInstance).map(PistolShotEvent.class::cast).toList();
        assertEquals(1, shots.size());
        assertEquals(400, shots.get(0).percent);
        assertEquals(perSymbol * 4, pistol, "pistolet : le coup le plus fort du tour, x4");
        assertEquals(pistol, shots.get(0).damage);

        assertEquals(total, hits.stream().mapToLong(hit -> hit.damage).sum() + shots.get(0).damage);
    }

    @Test
    void unCoupAfficheSesVraisDegatsMemeAuDelaDesPvDeLEnnemi() {
        Player player = new Player("Joueur", 100, List.of());
        Enemy enemy = new Enemy("Ennemi", 1_000_000);
        CombatContext context = new CombatContext(player, enemy);
        context.multiplySymbolPower(1_000_000); // 90 x 1 000 000 : 90 millions de dégâts
        Symbol[] symbols = {Symbol.TRIPLE_SEVEN, null, null};
        TurnResult turn = new CombatResolver().resolve(context, new ActionResolver().resolve(symbols), symbols,
            List.of());
        EnemyDamagedEvent hit = symbolHits(turn).get(0);
        assertEquals(90_000_000, hit.rawDamage, "plus de plafond : le vrai coup");
        assertEquals(90_000_000 - hit.blocked, hit.damage, "affiché tel quel, même au-delà des PV");
        assertTrue(enemy.isDefeated());
        assertEquals(0, enemy.getHp(), "ses PV ne descendent pas sous 0");
    }

    @Test
    void lePistoletRejoueLeVraiCoupSansPlafond() {
        Player player = new Player("Joueur", 100, List.of());
        Enemy boss = new Enemy(EnemyKind.COMETE); // sa Garde : il survit au premier coup, arrêté à son seuil
        CombatContext context = new CombatContext(player, boss);
        context.multiplySymbolPower(1_000_000_000);
        context.addPistolShot(100);
        Symbol[] symbols = {Symbol.TRIPLE_SEVEN, null, null};
        TurnResult turn = new CombatResolver().resolve(context, new ActionResolver().resolve(symbols), symbols,
            List.of());
        EnemyDamagedEvent hit = symbolHits(turn).get(0);
        assertTrue(hit.rawDamage > boss.getMaxHp(), "le coup n'est plus plafonné aux PV max");
        PistolShotEvent shot = turn.getPistolEvents().stream()
            .filter(PistolShotEvent.class::isInstance).map(PistolShotEvent.class::cast).findFirst().orElseThrow();
        assertEquals(hit.rawDamage, shot.rawDamage, "le pistolet rejoue le vrai coup");
        assertEquals((long) Math.ceil(boss.getMaxHp() * (double) EnemyKind.PHASE_TWO_RATIO), boss.getHp(),
            "la Garde tient : il s'arrête au seuil de sa phase 2");
    }

    @Test
    void lePistoletRejoueLeCoupAvecSesMultiplicateurs() {
        Player player = new Player("Joueur", 100, List.of());
        CombatContext context = new CombatContext(player, new Enemy("Ennemi", 1_000_000));
        context.multiplySymbolPower(10);
        context.addPistolShot(100);
        Symbol[] symbols = {Symbol.TRIPLE_SEVEN, null, null};
        TurnResult turn = new CombatResolver().resolve(context, new ActionResolver().resolve(symbols), symbols,
            List.of());
        assertEquals(900, symbolHits(turn).get(0).rawDamage, "Triple Sept 90 x10");
        PistolShotEvent shot = (PistolShotEvent) turn.getPistolEvents().get(0);
        assertEquals(900, shot.rawDamage, "le pistolet garde le x10 du Bingo");
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
