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
 * JSON, GameController), sans rang puis avec chaque rang : As de Coeur, As de
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
     * Chaque Triple Sept frappe de (90 + attaque du rang + 20 par Lame)
     * x (1 + 0,5 par Lame) x 2 (Corruption) x 4,5 (Paire des deux As)
     * x 5 (Bingo) ; le pistolet rejoue ensuite le coup le plus fort du tour, tel
     * qu'il a frappé, x4 (Roulette gagnante). AUCUN : pas de rang.
     */
    @ParameterizedTest(name = "{0} gains, rang {1} : {6} dégâts")
    @CsvSource({
        // gains, rang, As de Pique (consommé, Lames), par Triple Sept, pistolet, total
        "10000, AUCUN, 1000, 4, 22950, 91800, 160620",
        "10000, AVARE, 1000, 4, 36450, 145800, 255120",
        "10000, PARIEUR, 1000, 4, 49950, 199800, 349620",
        "10000, FLAMBEUR, 1000, 4, 70200, 280800, 491370",
        "10000, HABITUE, 1000, 4, 90450, 361800, 633120",
        "10000, REQUIN, 1000, 4, 184950, 739800, 1294620",
        "10000, GROS_BONNET, 1000, 4, 205200, 820800, 1436370",
        "10000, BARON, 1000, 4, 319950, 1279800, 2239620",
        "10000, MAGNAT, 1000, 4, 400950, 1603800, 2806620",
        "10000, ROI_DU_CASINO, 1000, 4, 765450, 3061800, 5358120",
        "10000, LEGENDE, 1000, 4, 967950, 3871800, 6775620",
        "100000, AUCUN, 2500, 10, 78300, 313200, 548070",
        "100000, AVARE, 2500, 10, 105300, 421200, 737070",
        "100000, PARIEUR, 2500, 10, 132300, 529200, 926070",
        "100000, FLAMBEUR, 2500, 10, 172800, 691200, 1209570",
        "100000, HABITUE, 2500, 10, 213300, 853200, 1493070",
        "100000, REQUIN, 2500, 10, 402300, 1609200, 2816070",
        "100000, GROS_BONNET, 2500, 10, 442800, 1771200, 3099570",
        "100000, BARON, 2500, 10, 672300, 2689200, 4706070",
        "100000, MAGNAT, 2500, 10, 834300, 3337200, 5840070",
        "100000, ROI_DU_CASINO, 2500, 10, 1563300, 6253200, 10943070",
        "100000, LEGENDE, 2500, 10, 1968300, 7873200, 13778070",
        "1000000, AUCUN, 2500, 10, 78300, 313200, 548070",
        "1000000, AVARE, 2500, 10, 105300, 421200, 737070",
        "1000000, PARIEUR, 2500, 10, 132300, 529200, 926070",
        "1000000, FLAMBEUR, 2500, 10, 172800, 691200, 1209570",
        "1000000, HABITUE, 2500, 10, 213300, 853200, 1493070",
        "1000000, REQUIN, 2500, 10, 402300, 1609200, 2816070",
        "1000000, GROS_BONNET, 2500, 10, 442800, 1771200, 3099570",
        "1000000, BARON, 2500, 10, 672300, 2689200, 4706070",
        "1000000, MAGNAT, 2500, 10, 834300, 3337200, 5840070",
        "1000000, ROI_DU_CASINO, 2500, 10, 1563300, 6253200, 10943070",
        "1000000, LEGENDE, 2500, 10, 1968300, 7873200, 13778070",
    })
    void asCoeurAsPiqueRouletteCorruptionEtBingoTripleSept(int gains, String rank, int spadesConsumed, int blades,
                                                          long perSymbol, long pistol, long total)
            throws Exception {
        RankBonus rankBonus = "AUCUN".equals(rank) ? RankBonus.NONE : Rank.valueOf(rank).getBonus();
        // La carte « Bingo » tire son symbole au hasard : ici, on impose le Triple Sept.
        Card bingo = new Card("bingo", "Bingo", "cards/special/bingo.png",
            List.of(new BingoEffect(5, Symbol.TRIPLE_SEVEN)), null, 0, true);
        List<Card> deck = List.of(CARDS.apply("1_coeur"), CARDS.apply("1_pique"),
            CARDS.apply("russian_roulette"), CARDS.apply("corruption"), bingo);
        GameController controller = new GameController(() -> new ArrayList<>(deck), CARDS, Map.of(),
            cards -> new Player("Joueur", Player.BASE_HP, cards, rankBonus, Symbol.classicReels()));
        Player player = controller.getGameState().getPlayer();
        // Une cible à la défense de base, avec le plus de PV possible : elle encaisse tous les coups.
        setGameState(controller, new GameState(player, new Enemy("Cible", Integer.MAX_VALUE)));
        player.addGains(gains);
        player.getLastingEffects().addBonusPlays(1);
        controller.drawCards();

        play(controller, "1_coeur");
        assertEquals(gains, player.getGains(), "As de Coeur : il ne coûte rien, et à PV pleins son Berserk vaut x1");
        play(controller, "1_pique");
        assertEquals(gains - spadesConsumed, player.getGains(), "As de Pique : -10 % des gains, seulement ce qui achète des Lames");
        assertEquals(blades, player.getLastingEffects().getBlades(), "une Lame par 2,5 tirages (250 gains), 10 au plus");
        play(controller, "russian_roulette");
        RouletteChoice roulette = (RouletteChoice) controller.getPendingChoice();
        assertFalse(controller.pickRouletteCard(roulette.cursed().indexOf(false)).cursed(), "Roulette gagnante");
        play(controller, "corruption");
        play(controller, "bingo");

        TurnResult turn = controller.spin();
        assertArrayEquals(new Symbol[] {Symbol.TRIPLE_SEVEN, Symbol.TRIPLE_SEVEN, Symbol.TRIPLE_SEVEN},
            turn.getSymbols());

        long base = 90 + rankBonus.attack() + blades * PreparationResolver.BLADE_ATTACK;
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
