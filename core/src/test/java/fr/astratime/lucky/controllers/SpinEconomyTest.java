package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.BonusGame;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.SpinEconomy;
import fr.astratime.lucky.entities.SpinEconomy.Debt;
import fr.astratime.lucky.entities.SpinEconomy.Stake;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.actions.GainAction;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;
import fr.astratime.lucky.entities.events.ShieldGainedEvent;
import fr.astratime.lucky.entities.events.StatusEvent;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.entities.tower.Chapter;
import fr.astratime.lucky.entities.tower.TowerRun;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/** Les gains en combat : coût du tirage, départ, Paires et Bingos, dette et Mise (voir {@link SpinEconomy}). */
class SpinEconomyTest {

    private static List<Card> plainCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 20; i++) cards.add(new Card("c" + i, "c" + i, "c.png", List.of(), null, 1));
        return cards;
    }

    /** Un combat réel (après {@link GameController#restart}) contre {@code kind}. */
    private static GameController fight(EnemyKind kind) {
        GameController controller = new GameController(SpinEconomyTest::plainCards);
        controller.restart(kind);
        return controller;
    }

    private static Player player(GameController controller) { return controller.getGameState().getPlayer(); }

    private static long damageDealt(TurnResult result) {
        return result.getSymbolOutcomes().stream().flatMap(o -> o.getEvents().stream())
            .filter(e -> e instanceof EnemyDamagedEvent).mapToLong(e -> ((EnemyDamagedEvent) e).rawDamage).sum();
    }

    @Test
    void theSpinCostFollowsThePlaceAndTheTowerChapter() {
        assertEquals(100, EnemyKind.ENTRAINEMENT.getSpinCost());
        assertEquals(100, EnemyKind.CROUPIER.getSpinCost(), "chapitre 1");
        assertEquals(100, EnemyKind.CHEF.getSpinCost(), "chapitre 2");
        assertEquals(500, EnemyKind.GARDIENNE.getSpinCost(), "chapitre 3");
        assertEquals(500, EnemyKind.PRETENDANT.getSpinCost(), "chapitre 4");
        assertEquals(2_000, EnemyKind.PORTIER.getSpinCost(), "chapitre 5");
        assertEquals(5_000, EnemyKind.MACHINE_ORIGINELLE.getSpinCost(), "chapitre 6");
        // Ordre d'ouverture : prairie, Fête foraine, Port, Mines, Casino, Désert de Braise ; coûts étirés
        // (Astra, 2026-10-29) pour qu'un lieu paie au moins deux fois le lieu d'avant
        Map<Place, Integer> costs = Map.of(Place.PRAIRIE, 100, Place.FOIRE, 700, Place.PORT, 3_000,
            Place.MINES, 12_000, Place.CASINO, 45_000, Place.DESERT, 140_000);
        for (Place place : Place.values()) {
            for (var dungeon : place.getDungeons()) {
                assertEquals((int) costs.get(place), dungeon.getSoldier().getSpinCost(), place + " soldat");
                assertEquals((int) costs.get(place), dungeon.getKing().getSpinCost(), place + " roi");
            }
        }
    }

    @Test
    void aFightStartsWithFiveSpinsAndEachSpinCostsOne() {
        GameController controller = fight(EnemyKind.CROUPIER);
        assertEquals(500, player(controller).getGains());
        assertEquals(100, controller.getSpinCost());
        controller.drawCards();
        controller.rigSpin(Symbol.SEVEN, Symbol.BAR, Symbol.GRAPE); // ni gains, ni Paire
        controller.spin();
        assertEquals(400, player(controller).getGains());

        GameController casino = fight(Place.CASINO.getDungeons().get(0).getSoldier());
        assertEquals(225_000, player(casino).getGains());
    }

    @Test
    void theNextFightOfAChapterKeepsTheGainsWithoutANewStart() {
        GameController controller = fight(EnemyKind.CROUPIER);
        player(controller).addGains(1_000);
        controller.startCombat(EnemyKind.GARDIEN);
        assertEquals(1_500, player(controller).getGains());
    }

    @Test
    void aControllerWithoutRealFightSpinsForFree() {
        GameController controller = new GameController(SpinEconomyTest::plainCards);
        assertEquals(0, controller.getSpinCost());
        assertEquals(0, player(controller).getGains());
    }

    @Test
    void pairsAndBingosPayInSpinCosts() {
        assertEquals(1_500, SpinEconomy.pairGains(500));
        assertEquals(10_000, SpinEconomy.jackpotGains(Symbol.CHERRY, 500), "fruit commun : 20");
        assertEquals(15_000, SpinEconomy.jackpotGains(Symbol.SEVEN, 500), "symbole moyen : 30");
        assertEquals(25_000, SpinEconomy.jackpotGains(Symbol.TRIPLE_SEVEN, 500), "symbole rare : 50");
        assertEquals(25_000, SpinEconomy.jackpotGains(Symbol.NUGGET, 500), "rouleau de la Mine : 50");
        assertEquals(25_000, SpinEconomy.jackpotGains(Symbol.CROWN, 500), "rouleau de la boutique : 50");
    }

    @Test
    void gainSymbolsFollowTheSpinCost() {
        Enemy portSoldier = new Enemy(Place.PORT.getDungeons().get(0).getSoldier());
        Player player = new Player("Joueur", Player.BASE_HP, plainCards());
        CombatContext context = new CombatContext(player, portSoldier);
        new GainAction(12).resolve(context);
        assertEquals(360, player.getGains(), "Cloche au Port : 12 x 30");
    }

    @Test
    void debtStartsBelowZeroAndTheBailiffComesAtFiveSpins() {
        assertEquals(Debt.NONE, SpinEconomy.debt(0, 100));
        assertEquals(Debt.INDEBTED, SpinEconomy.debt(-1, 100));
        assertEquals(Debt.INDEBTED, SpinEconomy.debt(-499, 100));
        assertEquals(Debt.BAILIFF, SpinEconomy.debt(-500, 100));
        assertEquals(Debt.INDEBTED, SpinEconomy.debt(-2_000, 500));
    }

    @Test
    void onlyTheSpinCostDigsTheDebt() {
        GameController controller = fight(EnemyKind.CROUPIER);
        player(controller).addGains(-10_000);
        assertEquals(0, player(controller).getGains(), "une perte ne fait pas passer sous 0");
        player(controller).paySpin(100);
        assertEquals(-100, player(controller).getGains());
        assertEquals(Debt.INDEBTED, controller.getDebt());
    }

    @Test
    void debtWeakensTheSymbols() {
        GameController rich = fight(EnemyKind.CROUPIER);
        rich.drawCards();
        rich.rigSpin(Symbol.DOUBLE_BAR, Symbol.GRAPE, Symbol.BELL);
        long full = damageDealt(rich.spin());

        GameController poor = fight(EnemyKind.CROUPIER);
        player(poor).paySpin(500); // à 0 : le tirage le fait passer à -100
        poor.drawCards();
        poor.rigSpin(Symbol.DOUBLE_BAR, Symbol.GRAPE, Symbol.BELL);
        TurnResult result = poor.spin();
        assertEquals(Math.round(full * SpinEconomy.INDEBTED_FACTOR), damageDealt(result));
        assertTrue(result.getEvents().stream().anyMatch(e -> e instanceof StatusEvent));
    }

    @Test
    void theBailiffMakesTheEnemyPlayOneMoreCard() {
        GameController controller = fight(EnemyKind.CROUPIER);
        player(controller).paySpin(1_000); // -500, puis -600 après le tirage
        controller.drawCards();
        controller.rigSpin(Symbol.BAR, Symbol.GRAPE, Symbol.BELL);
        TurnResult result = controller.spin();
        assertEquals(Debt.BAILIFF, controller.getDebt());
        assertNotNull(result.getEnemyTurn());
        assertEquals(EnemyKind.CROUPIER.getPlaysPerTurn() + 1, result.getEnemyTurn().played().size());
    }

    private static int shieldGained(TurnResult result) {
        return result.getSymbolOutcomes().stream().flatMap(o -> o.getEvents().stream())
            .filter(e -> e instanceof ShieldGainedEvent).mapToInt(e -> ((ShieldGainedEvent) e).amount).sum();
    }

    @Test
    void theStakeTakesAShareOfTheGainsIntoThePotAndStrengthensAttackAndDefense() {
        assertEquals(Stake.NONE, Stake.HIGH.next());
        assertEquals(List.of(0, 5, 10, 15), Arrays.stream(Stake.values()).map(stake -> stake.percent).toList());
        assertEquals(List.of(1f, 1.1f, 1.2f, 1.3f), Arrays.stream(Stake.values()).map(stake -> stake.power).toList());

        GameController plain = fight(EnemyKind.CROUPIER);
        plain.drawCards();
        plain.rigSpin(Symbol.SEVEN, Symbol.GRAPE, Symbol.BELL);
        TurnResult base = plain.spin();

        GameController staked = fight(EnemyKind.CROUPIER);
        staked.drawCards();
        double gains = player(staked).getGains();
        assertEquals(Stake.LOW, staked.nextStake());
        assertEquals(Stake.MEDIUM, staked.nextStake());
        assertEquals(Stake.HIGH, staked.nextStake());
        assertEquals(gains * 0.15, staked.getStakeAmount(), 1e-6, "Mise 15 % : 15 % des gains");
        assertEquals(staked.getSpinCost(), staked.getSpinPrice(), "le prix du tirage ne change pas");
        staked.rigSpin(Symbol.SEVEN, Symbol.GRAPE, Symbol.BELL);
        TurnResult result = staked.spin();
        assertEquals(damageDealt(base) * 1.3, damageDealt(result), 1.0, "attaque x1,3");
        assertEquals(shieldGained(base) * 1.3, shieldGained(result), 1.0, "défense x1,3");
        assertEquals(player(plain).getGains() - gains * 0.15, player(staked).getGains(), 1e-6,
            "la Mise est payée en plus du tirage");
        assertEquals(gains * 0.15, staked.getPot().getAmount(), 1e-6, "elle part dans la cagnotte");
        assertEquals(0, plain.getPot().getAmount(), "sans Mise, la cagnotte ne bouge pas");
        assertEquals(Stake.HIGH, staked.getStake(), "la Mise reste posée d'un tour à l'autre (Astra)");
        staked.drawCards();
        assertEquals(player(staked).getStakeableGains() * 0.15, staked.getStakeAmount(), 1.0,
            "au tour suivant, 15 % des gains du moment");
        staked.restart(EnemyKind.CROUPIER);
        assertEquals(Stake.NONE, staked.getStake(), "un nouveau combat repart sans Mise");
    }

    @Test
    void theStakeDoesNotTouchTheGainsOfABingo() {
        GameController plain = fight(EnemyKind.CROUPIER);
        plain.setBonusGameEnabled(false); // pas de Jeu bonus au hasard après le Bingo
        plain.drawCards();
        plain.rigSpin(Symbol.SEVEN, Symbol.SEVEN, Symbol.SEVEN);
        double before = player(plain).getGains();
        plain.spin();
        double earned = player(plain).getGains() - before;

        GameController staked = fight(EnemyKind.CROUPIER);
        staked.setBonusGameEnabled(false);
        staked.drawCards();
        staked.nextStake();
        double stakedBefore = player(staked).getGains();
        double stake = staked.getStakeAmount();
        staked.rigSpin(Symbol.SEVEN, Symbol.SEVEN, Symbol.SEVEN);
        staked.spin();
        assertEquals(earned, player(staked).getGains() - stakedBefore + stake, 1e-6, "les gains du Bingo, sans x");
    }

    @Test
    void thereIsNothingToStakeWithoutGains() {
        GameController controller = fight(EnemyKind.CROUPIER);
        player(controller).paySpin(player(controller).getGains());
        assertEquals(Stake.NONE, controller.nextStake(), "pas de gains : rien à miser");
        assertEquals(0, controller.getStakeAmount());
    }

    @Test
    void stolenPotWinningsNoLongerBlockTheStake() {
        GameController controller = fight(EnemyKind.BANQUEROUTE);
        Player player = player(controller);
        player.addGains(500_000);
        player.addPotWinnings(player.getGains()); // tout vient de la cagnotte : rien à miser
        assertEquals(0, player.getStakeableGains(), 1e-6);
        player.addGains(-player.getGains()); // Faillite : il prend tout
        player.addGains(200_000); // nouveaux gains gagnés au combat
        assertEquals(200_000, player.getStakeableGains(), 1e-6, "les gains volés ne bloquent plus la Mise");
        assertEquals(200_000, player.nextCombat().getStakeableGains(), 1e-6, "ni au combat suivant (Machine Originelle)");
        assertNotEquals(SpinEconomy.Stake.NONE, controller.nextStake(), "la Mise 5 % se choisit");
    }

    @Test
    void theBonusGamePaysThePotOnEveryLineThenEmptiesIt() {
        GameController controller = fight(EnemyKind.CROUPIER);
        controller.setBonusGameChance(1f); // le Jeu bonus s'ouvre à coup sûr après le Bingo
        controller.getPot().add(10_000);
        controller.drawCards();
        controller.nextStake(); // Mise 5 %, versée à la cagnotte avant le tirage
        double stake = controller.getStakeAmount();
        controller.rigSpin(Symbol.SEVEN, Symbol.SEVEN, Symbol.SEVEN);
        TurnResult result = controller.spin();
        BonusGame bonus = result.getBonusGame();
        assertNotNull(bonus);
        assertEquals(10_000 + stake, bonus.getPot(), 1e-6, "la Mise du tirage est déjà dans la cagnotte");
        assertEquals(0, controller.getPot().getAmount(), "la cagnotte repart de 0");
        for (BonusGame.Line line : bonus.getLines()) {
            assertTrue(line.gains() >= bonus.getPot(), "chaque alignement : sa valeur + la cagnotte");
        }
        Player player = player(controller);
        assertEquals(Math.max(0, player.getGains() - bonus.getPot() * bonus.getLines().size()),
            player.getStakeableGains(), 1e-6, "ce que la cagnotte a rapporté ne se remise pas");
        player.addGains(1_000);
        assertEquals(1_000, player.nextCombat().getStakeableGains() - Math.max(0,
            player.getGains() - 1_000 - bonus.getPot() * bonus.getLines().size()), 1e-6, "ni au combat suivant de la descente");
    }

    @Test
    void thePotIsAddedToEachLineWithoutMultiplier() {
        BonusGame game = BonusGame.play(Symbol.classicReels(), 100, new Random(7));
        assertFalse(game.getLines().isEmpty());
        BonusGame paid = game.withPot(1_000);
        assertEquals(game.getTotal() + 1_000.0 * game.getLines().size(), paid.getTotal(), 1e-6);
        for (int i = 0; i < game.getLines().size(); i++) {
            assertEquals(game.getLines().get(i).gains() + 1_000, paid.getLines().get(i).gains(), 1e-6);
        }
        assertSame(game, game.withPot(0), "sans cagnotte, rien ne change");
    }

    @Test
    void theTowerPotIsSharedByTheChapterAndClearedWhenItStartsOver() {
        TowerRun run = new TowerRun(Chapter.values()[0]);
        run.getPot().add(500);
        run.win();
        assertEquals(500, run.getPot().getAmount(), "partagée d'un combat à l'autre");
        run.restart();
        assertEquals(0, run.getPot().getAmount(), "effacée quand le chapitre recommence");
        assertEquals(0.30f, BonusGame.TOWER_TRIGGER_CHANCE, "un Bingo ouvre le Jeu bonus 3 fois sur 10 dans la Tour");
    }

    @Test
    void gainsGoFarBeyondTwoBillion() {
        GameController controller = fight(EnemyKind.CROUPIER);
        Player player = player(controller);
        player.addGains(2_000_000_000);
        player.addGains(2_000_000_000);
        assertEquals(4_000_000_500d, player.getGains(), "plus de limite à 2,1 milliards");
        player.addGains(1e300);
        player.addGains(1e308);
        player.addGains(1e308);
        assertEquals(fr.astratime.lucky.entities.BigNumber.MAX, player.getGains(), "jamais l'infini");
        player.addGains(-1e308);
        assertTrue(player.getGains() > 0 && Double.isFinite(player.getGains()));
    }

    @Test
    void theShopFollowsThePlaceAndEachCardDoublesAtEachPurchase() {
        GameController controller = new GameController(SpinEconomyTest::plainCards,
            id -> new Card(id, id, "x.png", List.of(), null, 1),
            new LinkedHashMap<>(Map.of(GameController.SHOP_BINGO, 20_000, "corruption", 10_000)));
        controller.restart(Place.CASINO.getDungeons().get(0).getSoldier()); // tirage à 45 000 : x450
        Player player = player(controller);
        player.addGains(100_000_000);
        assertEquals(9_000_000, price(controller, GameController.SHOP_BINGO));
        assertEquals(4_500_000, price(controller, "corruption"));
        assertNotNull(controller.buy(offer(controller, GameController.SHOP_BINGO)));
        assertEquals(18_000_000, price(controller, GameController.SHOP_BINGO), "le Bingo suivant coûte le double");
        assertNotNull(controller.buy(offer(controller, "corruption")));
        assertEquals(9_000_000, price(controller, "corruption"), "la Corruption double aussi, à part du Bingo");
        assertNotNull(controller.buy(offer(controller, GameController.SHOP_BINGO)));
        assertEquals(36_000_000, price(controller, GameController.SHOP_BINGO));
        assertEquals(100_000_000 + 225_000 - 9_000_000 - 4_500_000 - 18_000_000, player.getGains());

        controller.startCombat(Place.CASINO.getDungeons().get(0).getKing());
        assertEquals(9_000_000, price(controller, GameController.SHOP_BINGO), "chaque combat repart du prix de base");

        controller.restart(Place.PRAIRIE.getDungeons().get(0).getSoldier());
        assertEquals(20_000, price(controller, GameController.SHOP_BINGO), "Prairie : prix inchangé");
    }

    private static GameController.ShopOffer offer(GameController controller, String id) {
        return controller.getShopOffers().stream().filter(o -> o.card().getId().equals(id)).findFirst().orElseThrow();
    }

    private static double price(GameController controller, String id) { return offer(controller, id).price(); }

    @Test
    void thePotDeLutinPaysFiveSpins() {
        Card pot = new Card("pot", "pot", "x.png",
            List.of(new fr.astratime.lucky.entities.effects.SpinGainsEffect(5, 2)), null, 1, true);
        GameController controller = new GameController(() -> {
            List<Card> cards = plainCards();
            cards.add(pot);
            return cards;
        });
        controller.restart(Place.PORT.getDungeons().get(0).getSoldier());
        double before = player(controller).getGains();
        controller.drawCards();
        Card inHand = player(controller).getCurrentHand().stream().filter(c -> c.getId().equals("pot")).findFirst()
            .orElseGet(() -> {
                player(controller).addToHandOrDeck(pot);
                return pot;
            });
        controller.playCard(inHand);
        assertEquals(before + 5 * 3_000, player(controller).getGains(), "5 tirages de 3 000 au Port");
    }
}
