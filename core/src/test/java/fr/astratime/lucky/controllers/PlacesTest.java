package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.enemy.EnemyCards;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.entities.exploration.PlaceRule;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Règles des lieux de l'Exploration (Scorbut, grisou, marée) et ennemis qui en dépendent. */
class PlacesTest {

    private static Card card(String id) {
        return new Card(id, id, "x.png", List.of(), null, 0);
    }

    /** @return un joueur qui a {@code hand} en main. */
    private static Player playerHolding(Card... hand) {
        Player player = new Player("Joueur", 100, List.of(hand));
        player.draw(hand.length);
        return player;
    }

    @Test
    void eachPlaceSetsTheSwordShieldAndHealOfAllItsEnemies() {
        int[][] stats = {{50, 1_000, 10}, {150, 10_000, 20}, {400, 50_000, 30}};
        Place[] places = {Place.PORT, Place.MINES, Place.CASINO};
        for (int p = 0; p < places.length; p++) {
            for (Dungeon dungeon : places[p].getDungeons()) {
                for (EnemyKind kind : List.of(dungeon.getSoldier(), dungeon.getKing())) {
                    assertEquals(stats[p][0], kind.swordDamage(), kind + " : épée");
                    assertEquals(stats[p][1], kind.getBaseDefense(), kind + " : défense de base");
                    assertEquals(stats[p][1], kind.shieldDefense(), kind + " : Bouclier à la même échelle");
                    assertEquals(stats[p][2], kind.potionPercent(0), 1e-4, kind + " : soin");
                }
            }
        }
        assertEquals(EnemySymbol.SWORD_DAMAGE, EnemyKind.ROI_PIQUE.swordDamage(), "la prairie ne change pas");
        assertEquals(EnemySymbol.SHIELD_DEFENSE, EnemyKind.ROI_PIQUE.shieldDefense());
        Card tenOfDiamonds = new Card("c", "c", "x.png", List.of(), Card.Suit.CARREAU, 10);
        Card tenOfSpades   = new Card("p", "p", "x.png", List.of(), Card.Suit.PIQUE, 10);
        assertEquals(EnemyCards.shieldBonus(tenOfDiamonds) * 500, EnemyKind.KRAKEN.shieldBonus(tenOfDiamonds),
            "les Carreaux du Casino protègent 500 fois plus");
        assertEquals(EnemyCards.swordBonus(tenOfSpades) * 20, EnemyKind.KRAKEN.swordBonus(tenOfSpades),
            "les Piques du Casino frappent 20 fois plus");
        assertEquals(EnemyCards.shieldBonus(tenOfDiamonds), EnemyKind.ROI_PIQUE.shieldBonus(tenOfDiamonds));
        assertEquals(EnemySymbol.SHIELD_DEFENSE, EnemyKind.CROUPIER.shieldDefense(), "la Tour ne change pas");
        assertEquals(60f, EnemyKind.KRAKEN.potionPercent(EnemySymbol.POTION_PERCENT), 1e-4,
            "les Cœurs renforcent son soin en proportion");
    }

    @Test
    void thePrairieDungeonsSetHowHardTheirEnemiesHit() {
        assertEquals(48,  EnemyKind.SOLDAT_PIQUE.damagePercent(), "Pique : les coups portent moins");
        assertEquals(48,  EnemyKind.ROI_PIQUE.damagePercent());
        assertEquals(165, EnemyKind.ROI_TREFLE.damagePercent(), "les autres donjons frappent plus fort");
        assertEquals(210, EnemyKind.ROI_COEUR.damagePercent());
        assertEquals(165, EnemyKind.ROI_CARREAU.damagePercent());
        assertEquals(100, EnemyKind.CROUPIER.damagePercent(), "la Tour ne change pas");
        assertEquals(100, EnemyKind.KRAKEN.damagePercent(), "les autres lieux non plus");
        assertEquals(Math.round(EnemySymbol.SWORD_DAMAGE * 0.48), EnemyKind.ROI_PIQUE.hurt(EnemySymbol.SWORD_DAMAGE));
        assertEquals(Math.round(EnemySymbol.SWORD_DAMAGE * 2.1), EnemyKind.ROI_COEUR.hurt(EnemySymbol.SWORD_DAMAGE));

        Player player = new Player("Joueur", Player.BASE_HP, List.of());
        Enemy king = new Enemy(EnemyKind.ROI_COEUR);
        int before = player.getHp();
        new EnemyTurnResolver.Turn(king, player, 0).strike(EnemySymbol.SWORD_DAMAGE);
        assertEquals(Math.round(EnemySymbol.SWORD_DAMAGE * 2.1), before - player.getHp(), "le coup porté suit le réglage");
    }

    @Test
    void scurvyComesBackEveryThreeTurnsStartingWithTheFirst() {
        assertTrue(PlaceRule.SCORBUT.scurvyArrives(1));
        assertFalse(PlaceRule.SCORBUT.scurvyArrives(2));
        assertFalse(PlaceRule.SCORBUT.scurvyArrives(3));
        assertTrue(PlaceRule.SCORBUT.scurvyArrives(4));
        assertTrue(PlaceRule.SCORBUT.scurvyArrives(7));
        assertFalse(PlaceRule.NONE.scurvyArrives(1));
    }

    @Test
    void anUnplayedScurvyCardHalvesAttackDefenseAndGains() {
        Player sick = playerHolding(card(PlaceRule.SCURVY_CARD), card("autre"));
        CombatContext halved = new PreparationResolver()
            .resolve(List.of(), sick, new Enemy("Ennemi", 1000), PlaceRule.SCORBUT, 1).getCombatContext();
        assertEquals(0.5f, halved.getAttackFactor(), 1e-6);
        assertEquals(0.5f, halved.getDefenseFactor(), 1e-6);
        assertEquals(0.5f, halved.getGainFactor(), 1e-6);

        Player healthy = playerHolding(card("autre"));
        CombatContext normal = new PreparationResolver()
            .resolve(List.of(), healthy, new Enemy("Ennemi", 1000), PlaceRule.SCORBUT, 1).getCombatContext();
        assertEquals(1f, normal.getAttackFactor(), 1e-6, "Scorbut joué : plus de malus");
    }

    @Test
    void firedampExplodesEveryFifthTurn() {
        assertFalse(PlaceRule.GRISOU.firedampExplodes(4));
        assertTrue(PlaceRule.GRISOU.firedampExplodes(5));
        assertTrue(PlaceRule.GRISOU.firedampExplodes(10));
        assertFalse(PlaceRule.NONE.firedampExplodes(5));
        assertEquals(3, PlaceRule.turnsBeforeFiredamp(3), "tours 3, 4 et 5");
        assertEquals(1, PlaceRule.turnsBeforeFiredamp(5), "à la fin de ce tour");
    }

    @Test
    void highTideCutsTheAttackFromThreeToFiveThenTheSeaGoesBackDown() {
        for (int turn = 1; turn <= 12; turn++) {
            boolean high = PlaceRule.MAREE.isHighTide(turn);
            assertEquals(turn % 6 >= 3, high, "tour " + turn);
        }
        CombatContext context = new PreparationResolver()
            .resolve(List.of(), playerHolding(card("autre")), new Enemy("Ennemi", 1000), PlaceRule.MAREE, 3)
            .getCombatContext();
        assertEquals(0.7f, context.getAttackFactor(), 1e-6);
    }

    @Test
    void goldSkinHalvesDamageWhileTheGolemIsAboveHalfItsHp() {
        Enemy golem = new Enemy(EnemyKind.GOLEM_OR);
        assertEquals(500, golem.takeDamage(1_000));
        Enemy weakened = new Enemy(EnemyKind.GOLEM_OR);
        weakened.takeDamage(weakened.getMaxHp() + 2); // 2 x (PV max / 2 + 1) : juste sous la moitié
        assertEquals(1_000, weakened.takeDamage(1_000), "sous la moitié, plus de peau d'or");
    }

    @Test
    void theKrakenPlaysOneCardPerArmLeft() {
        Enemy kraken = new Enemy(EnemyKind.KRAKEN);
        assertEquals(EnemyKind.KRAKEN_ARMS, kraken.getPlaysPerTurn());
        kraken.takeDamage(kraken.getMaxHp() / 2);
        assertEquals(EnemyKind.KRAKEN_ARMS / 2, kraken.getPlaysPerTurn());
        kraken.takeDamage(kraken.getMaxHp());
        assertEquals(1, kraken.getPlaysPerTurn(), "toujours au moins un bras");
        assertEquals(5, new Enemy(EnemyKind.PIEUVRE).getPlaysPerTurn());
    }
}
