package fr.astratime.lucky.entities;

import fr.astratime.lucky.controllers.ActionResolver;
import fr.astratime.lucky.controllers.CombatResolver;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.context.SpinContext;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** Les rouleaux de la boutique, la machine du joueur et le bonus de son rang. */
class ShopReelsTest {

    private static Player player(RankBonus bonus) {
        return new Player("Joueur", Player.BASE_HP, List.of(), bonus, Symbol.classicReels());
    }

    /** Résout un tirage de trois {@code symbol} sans bonus de paire ni de jackpot à vérifier. */
    private static void resolve(Player player, Enemy enemy, Symbol... symbols) {
        new CombatResolver().resolve(new CombatContext(player, enemy), new ActionResolver().resolve(symbols), symbols,
            List.of());
    }

    @Test
    void theRankAddsItsHpAndRaisesEveryReelOfItsType() {
        Player player = player(new RankBonus(100, 100, 50, 70));
        assertEquals(200, player.getMaxHp());
        assertEquals(200, player.getHp());

        Enemy enemy = new Enemy("Ennemi", 100_000);
        int defense = enemy.getDefense();
        resolve(player, enemy, Symbol.BAR, null, null);
        assertEquals(100_000 - (10 + 100 - defense), enemy.getHp(), "BAR : 10 + 100 du rang, moins la défense");

        resolve(player, new Enemy("Ennemi", 100_000), Symbol.GRAPE, null, null);
        assertEquals(8 + 50, player.getShield(), "Raisin : 8 + 50 du rang");

        int gains = player.getGains();
        resolve(player, new Enemy("Ennemi", 100_000), Symbol.BELL, null, null);
        assertEquals(gains + 8 + 70, player.getGains(), "Cloche : 8 + 70 du rang");
    }

    @Test
    void theRankIsKeptFromOneFightToTheNext() {
        Player player = player(new RankBonus(100, 100, 50, 70));
        player.takeDamage(30);
        Player next = player.nextCombat();
        assertEquals(200, next.getMaxHp());
        assertEquals(170, next.getHp());
        assertEquals(player.getRankBonus(), next.getRankBonus());
    }

    @Test
    void theSwordIgnoresTheDefense() {
        Enemy enemy = new Enemy("Ennemi", 100_000);
        resolve(player(RankBonus.NONE), enemy, Symbol.SWORD, null, null);
        assertEquals(100_000 - 70, enemy.getHp());
    }

    @Test
    void theBombHitsHardButCostsLife() {
        Player player = player(RankBonus.NONE);
        Enemy enemy = new Enemy("Ennemi", 100_000);
        int defense = enemy.getDefense();
        resolve(player, enemy, Symbol.BOMB, null, null);
        assertEquals(100_000 - (200 - defense), enemy.getHp());
        assertEquals(95, player.getHp(), "5 % de 100 PV max");
    }

    @Test
    void theHeartHealsAndFillsTheBloodWithTheRest() {
        Player player = player(RankBonus.NONE);
        player.takeDamage(4);
        resolve(player, new Enemy("Ennemi", 100_000), Symbol.HEART, null, null);
        assertEquals(100, player.getHp());
        assertEquals(6, player.getLastingEffects().getBlood(), "10 PV soignés, 4 manquaient");
    }

    @Test
    void theStarAttacksShieldsAndEarns() {
        Player player = player(new RankBonus(0, 100, 0, 0)); // l'attaque passe la défense
        Enemy enemy = new Enemy("Ennemi", 100_000);
        int defense = enemy.getDefense();
        resolve(player, enemy, Symbol.STAR, null, null);
        assertEquals(100_000 - (30 + 100 - defense), enemy.getHp());
        assertEquals(30, player.getShield());
        assertEquals(30, player.getGains());
    }

    @Test
    void theMachineOnlyDrawsItsOwnReelsAndTheJoker() {
        List<Symbol> reels = new ArrayList<>(Symbol.classicReels());
        reels.set(reels.indexOf(Symbol.BAR), Symbol.CROWN);
        SlotMachine machine = new SlotMachine(reels, new Random(7));
        boolean crown = false;
        for (int i = 0; i < 500; i++) {
            for (Symbol symbol : machine.spin(new SpinContext())) {
                assertNotEquals(Symbol.BAR, symbol);
                assertTrue(reels.contains(symbol) || symbol == Symbol.JOKER, String.valueOf(symbol));
                crown |= symbol == Symbol.CROWN;
            }
        }
        assertTrue(crown, "la Couronne sort");
    }
}
