package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.CardFamily;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.effects.AttackEffect;
import fr.astratime.lucky.entities.effects.ExtraDrawEffect;
import fr.astratime.lucky.entities.effects.GainEffect;
import fr.astratime.lucky.entities.enemy.EnemyCards;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySlotMachine;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.tower.Chapter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/** Les chapitres 4 à 6 de la Tour des épreuves : leurs ennemis, leurs symboles et la Machine Originelle. */
class TowerChapters456Test {

    /** Un tour de {@code enemy} dont les rouleaux montrent {@code symbol} (en cherchant la graine). */
    private static EnemyTurnResult turnWith(EnemySymbol symbol, Supplier<Enemy> enemy, Supplier<Player> player) {
        for (int seed = 0; seed < 500; seed++) {
            EnemyTurnResult turn = new EnemyTurnResolver(new Random(seed)).resolve(enemy.get(), player.get(), 0, 0f);
            if (Arrays.asList(turn.symbols()).contains(symbol)) return turn;
        }
        throw new AssertionError("aucun tirage avec " + symbol);
    }

    private static Card card(String id, Card.Suit suit, int rank) {
        return new Card(id, id, "x.png", List.of(), suit, rank);
    }

    @Test
    void theNewChaptersHaveAstrasHpAndPower() {
        int[][] hp = {{1_600_000, 2_800_000, 5_500_000}, {2_000_000, 3_500_000, 7_000_000},
            {2_500_000, 4_500_000, 10_000_000}};
        Chapter[] chapters = {Chapter.MONDE_SANS_MAITRE, Chapter.LA_MAISON, Chapter.LE_JACKPOT};
        for (int i = 0; i < chapters.length; i++) {
            Chapter chapter = chapters[i];
            assertEquals(4 + i, chapter.getNumber());
            int power = (4 + i) * 100;
            assertEquals(hp[i][0], chapter.getFirstEnemy().getMaxHp(), chapter.name());
            assertEquals(hp[i][2], chapter.getBoss().getMaxHp(), chapter.name());
            assertTrue(chapter.getBoss().isBoss());
            for (EnemyKind challenger : chapter.getChallengers()) {
                if (challenger != EnemyKind.TEMPS_MORT) assertEquals(hp[i][1], challenger.getMaxHp(), challenger.name());
                assertEquals(power, challenger.getPower());
                assertTrue(challenger.isTower());
            }
            assertEquals(power, chapter.getBoss().getPower());
        }
        assertTrue(EnemyKind.TEMPS_MORT.getMaxHp() < 4_500_000, "le Temps Mort a moins de PV");
        assertEquals("Tu as tiré le levier. Le monde a recommencé.", Chapter.LE_JACKPOT.getEnding());
    }

    @Test
    void fakeMoneyVanishesAtTheNextSpinUnlessSpent() {
        Player[] holder = new Player[1];
        turnWith(EnemySymbol.FAKE_MONEY, () -> new Enemy(EnemyKind.FAUSSAIRE), () -> {
            holder[0] = new Player("Joueur", 1_000_000, List.of());
            holder[0].addGains(100_000);
            return holder[0];
        });
        Player player = holder[0];
        int fake = player.getLastingEffects().getFakeGains();
        assertTrue(fake > 0);
        player.addGains(-1_000); // un achat : la fausse monnaie part d'abord
        assertEquals(fake - 1_000, player.getLastingEffects().getFakeGains());
        int before = player.getGains();
        new PreparationResolver().resolve(List.of(), player, new Enemy(EnemyKind.FAUSSAIRE));
        assertEquals(before - (fake - 1_000), player.getGains());
        assertEquals(0, player.getLastingEffects().getFakeGains());
    }

    @Test
    void theTaxTakesTwentyPercentThenAThousandEvenBelowZero() {
        Player player = new Player("Joueur", 100, List.of());
        player.addGains(10_000);
        assertEquals(3_000, player.payTax(EnemySymbol.TAX_PERCENT, EnemySymbol.TAX_FLAT));
        assertEquals(7_000, player.getGains());
        player.addGains(-7_000);
        player.payTax(EnemySymbol.TAX_PERCENT, EnemySymbol.TAX_FLAT);
        assertEquals(-1_000, player.getGains(), "même à sec, la Taxe fait mal");
        player.addGains(-500);
        assertEquals(-1_000, player.getGains(), "une autre perte n'aggrave pas la dette");
        player.addGains(3_000);
        assertEquals(2_000, player.getGains(), "les gains remboursent la dette");
    }

    @Test
    void aFulfilledPredictionTriplesTheNextAttacks() {
        Enemy enemy = new Enemy(EnemyKind.CARTOMANCIENNE);
        enemy.predict(Symbol.BELL);
        assertFalse(enemy.checkPrediction(new Symbol[] {Symbol.CHERRY, Symbol.SEVEN, Symbol.BAR}));
        assertNull(enemy.getPrediction(), "une Prédiction ne vaut qu'un tirage");
        enemy.predict(Symbol.BELL);
        assertTrue(enemy.checkPrediction(new Symbol[] {Symbol.CHERRY, Symbol.BELL, null}));
        assertTrue(enemy.takePredictionHit());
        assertFalse(enemy.takePredictionHit());
    }

    @Test
    void theDuelCountsAcesAsFourteenAndSpecialCardsAsNothing() {
        assertEquals(14, EnemyTurnResolver.duelRank(card("as", Card.Suit.PIQUE, 1)));
        assertEquals(12, EnemyTurnResolver.duelRank(card("dame", Card.Suit.COEUR, 12)));
        assertEquals(0, EnemyTurnResolver.duelRank(card("joker", null, 1)));
    }

    @Test
    void friskedCardsComeBackAtTheNextFight() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 10; i++) cards.add(card("c" + i, Card.Suit.TREFLE, 2 + i));
        Player player = new Player("Joueur", 100, cards);
        Card taken = player.frisk(new Random(1));
        assertNotNull(taken);
        assertFalse(player.getAllCards().contains(taken));
        assertTrue(player.nextCombat().getAllCards().contains(taken), "confisquée pour un combat seulement");
    }

    @Test
    void bankruptcyTakesEverythingFromARichPlayerElseHurtsTheEnemy() {
        Player[] holder = new Player[1];
        turnWith(EnemySymbol.BANKRUPTCY, () -> new Enemy(EnemyKind.BANQUEROUTE), () -> {
            holder[0] = new Player("Joueur", 1_000_000, List.of());
            holder[0].addGains(100_000_000);
            return holder[0];
        });
        assertEquals(0, holder[0].getGains());

        Enemy[] foe = new Enemy[1];
        turnWith(EnemySymbol.BANKRUPTCY, () -> foe[0] = new Enemy(EnemyKind.BANQUEROUTE),
            () -> new Player("Joueur", 1_000_000, List.of()));
        assertTrue(foe[0].getHp() < foe[0].getMaxHp(), "plus pauvre que lui : c'est lui qui perd");
    }

    @Test
    void thePretenderLosesAShardForEachThirdOfHisHp() {
        assertEquals(1, EnemyKind.PRETENDANT.phaseFor(1f));
        assertEquals(2, EnemyKind.PRETENDANT.phaseFor(0.6f));
        assertEquals(3, EnemyKind.PRETENDANT.phaseFor(0.3f));
        assertEquals(3, EnemyKind.shards(1));
        assertEquals(1, EnemyKind.shards(3));
    }

    @Test
    void theHouseCancelsTheFirstBigHitOnlyOnceAndLosesItsPartsByThirds() {
        Enemy house = new Enemy(EnemyKind.MAISON);
        assertEquals(1_000, house.takeDamage(1_000), "un petit coup passe");
        assertEquals(0, house.takeDamage(house.getMaxHp() / 10), "la Maison gagne toujours");
        assertNotNull(house.takeNotice());
        assertEquals(house.getMaxHp() / 10, house.takeDamage(house.getMaxHp() / 10), "une seule fois");

        house.takeDamage(house.getMaxHp() / 3);
        assertTrue(house.enterPhaseTwo());
        assertEquals(0, house.getBaseDefense(), "la Façade est tombée");
        assertFalse(house.getWeights().containsKey(EnemySymbol.SHIELD));
        house.takeDamage(house.getMaxHp() / 3);
        assertTrue(house.enterPhaseTwo());
        assertEquals(Map.of(EnemySymbol.SWORD, 1), house.getWeights(), "le Coffre est tombé : plus de soins");
    }

    @Test
    void theOriginalMachineStealsReelsThenResistsForALastDraw() {
        Enemy machine = new Enemy(EnemyKind.MACHINE_ORIGINELLE);
        assertEquals(5, machine.getReelCount());
        assertEquals(5, new EnemySlotMachine(new Random(1)).spin(machine.getWeights(), Map.of(), 5).length);
        machine.takeDamage(machine.getMaxHp() * 6 / 10);
        assertTrue(machine.enterPhaseTwo());
        assertEquals(6, machine.getReelCount());
        assertEquals(1, machine.getStolenReels());
        machine.takeDamage(machine.getMaxHp() * 35 / 100);
        assertTrue(machine.enterPhaseTwo());
        assertEquals(2, machine.getStolenReels(), "le joueur n'a plus qu'un rouleau");

        TurnContext spin = new PreparationResolver().resolve(List.of(), new Player("Joueur", 100, List.of()), machine);
        assertEquals(java.util.Set.of(0, 2), spin.getSpinContext().getBlockedReels());

        machine.takeDamage(machine.getMaxHp());
        assertFalse(machine.isDefeated(), "elle résiste au coup fatal");
        assertEquals(1, machine.getHp());
        assertTrue(machine.isLastDrawPending());
        machine.takeDamage(machine.getMaxHp());
        assertEquals(1, machine.getHp(), "les coups suivants du même tour ne l'achèvent pas avant le Dernier tirage");

        Player player = new Player("Joueur", 100, List.of());
        var events = new TurnEngine().lastDraw(player, machine);
        assertFalse(machine.isLastDrawPending());
        assertTrue(machine.isDefeated() != player.isDefeated(), "le meilleur score gagne, l'autre tombe");
        var draw = (fr.astratime.lucky.entities.events.LastDrawEvent) events.get(0);
        assertEquals(machine.isDefeated(), draw.playerWins, "la cinématique montre le vrai vainqueur");
        assertEquals(draw.mine.size(), draw.hers.size());
        for (int round = 0; round < draw.mine.size() - 1; round++) {
            assertEquals(TurnEngine.score(draw.mine.get(round)), TurnEngine.score(draw.hers.get(round)), "égalité : on relance");
        }
        assertEquals(draw.playerWins, TurnEngine.score(draw.own()) > TurnEngine.score(draw.theirs()));
        assertTrue(TurnEngine.score(Symbol.JOKER) > TurnEngine.score(Symbol.WATERMELON));
    }

    @Test
    void losingTheLastDrawIsADefeatEvenAfterSeveralFatalHits() {
        for (int seed = 0; seed < 40; seed++) {
            Enemy machine = new Enemy(EnemyKind.MACHINE_ORIGINELLE);
            Player player = new Player("Joueur", 100, List.of());
            machine.takeDamage(machine.getMaxHp());
            machine.takeDamage(machine.getMaxHp());             // un deuxième coup fatal dans le même tour
            var draw = (fr.astratime.lucky.entities.events.LastDrawEvent)
                new TurnEngine().lastDraw(player, machine).get(0);
            assertEquals(draw.playerWins, machine.isDefeated(), "seed " + seed);
            assertEquals(!draw.playerWins, player.isDefeated(), "seed " + seed);
        }
    }

    @Test
    void thePorterBansAFamilyAndTheShadowCopiesThePlayersDeck() {
        Card draw   = new Card("pioche", "Pioche", "x.png", List.of(new ExtraDrawEffect(2)), null, 1);
        Card gains  = new Card("gains", "Gains", "x.png", List.of(new GainEffect(500)), null, 1);
        Card attack = new Card("attaque", "Attaque", "x.png", List.of(new AttackEffect(10)), null, 1);
        assertTrue(CardFamily.PIOCHE.contains(draw));
        assertFalse(CardFamily.PIOCHE.contains(gains));
        assertTrue(CardFamily.GAINS.contains(gains));
        assertTrue(CardFamily.ATTAQUE.contains(attack));

        List<Card> shadow = EnemyCards.shadowDeck(List.of(card("as", Card.Suit.COEUR, 1), draw, gains, attack));
        assertEquals(4, shadow.size());
        assertEquals(Card.Suit.COEUR, shadow.get(0).getSuit());
        assertEquals(14, shadow.get(0).getRank());
        assertEquals(Card.Suit.CARREAU, shadow.get(1).getSuit());
        assertEquals(Card.Suit.TREFLE, shadow.get(2).getSuit());
        assertEquals(Card.Suit.PIQUE, shadow.get(3).getSuit());
    }

    @Test
    void theDeadTimeLastsTenTurns() {
        assertEquals(10, EnemyKind.TEMPS_MORT.getTurnLimit());
        assertEquals(0, EnemyKind.MAISON.getTurnLimit());
    }
}
