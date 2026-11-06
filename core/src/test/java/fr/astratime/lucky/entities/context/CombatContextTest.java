package fr.astratime.lucky.entities.context;

import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Règles du renvoi de dégâts (cartes Carreau et As de Carreau). */
class CombatContextTest {

    private final Player        player  = new Player("Joueur", 100, List.of());
    private final CombatContext context = new CombatContext(player, new Enemy("Ennemi", 1000));

    @Test
    void noReflectByDefault() {
        assertEquals(0, context.getTotalReflectPercent());
    }

    @Test
    void diamondReflectNeedsADefenseSymbol() {
        context.addReflectAttackPercent(70f);

        assertEquals(0f, context.getReflectAttackPercent(), 1e-6, "sans symbole de défense, pas de renvoi");

        context.markDefenseSymbolDrawn();
        assertEquals(70f, context.getReflectAttackPercent(), 1e-6);
        assertEquals(0f, context.getReflectHpPercent(), 1e-6, "plus rien sur ses PV max (Astra 2026-11-06)");
        assertEquals(0, context.getTotalReflectPercent(), "les Carreaux ne renvoient pas l'attaque ennemie");
    }

    @Test
    void diamondReflectsAddUpToTheirCap() {
        context.addReflectAttackPercent(70f);  // Dame de Carreau
        context.addReflectAttackPercent(75f);  // Roi de Carreau
        context.markDefenseSymbolDrawn();
        assertEquals(145f, context.getReflectAttackPercent(), 1e-6);

        context.addReflectAttackPercent(65f);
        context.addReflectAttackPercent(60f);
        assertEquals(200f, CombatContext.MAX_REFLECT_ATTACK_PERCENT, 1e-6);
        assertEquals(CombatContext.MAX_REFLECT_ATTACK_PERCENT, context.getReflectAttackPercent(), 1e-6, "200 % de ses coups au plus");
    }

    @Test
    void guaranteedReflectWorksWithoutDefenseSymbol() {
        context.addGuaranteedReflect(500, 1000);

        assertEquals(500, context.getTotalReflectPercent());
    }

    @Test
    void guaranteedReflectIsStrongerUnderLowHp() {
        context.addGuaranteedReflect(500, 1000);
        player.takeDamage(85); // 15% de vie < 20%

        assertEquals(1000, context.getTotalReflectPercent());
    }

    @Test
    void guaranteedAndDiamondReflectsStaySeparate() {
        context.addGuaranteedReflect(500, 1000);
        context.addReflectAttackPercent(75f);
        context.markDefenseSymbolDrawn();

        assertEquals(500, context.getTotalReflectPercent());
        assertEquals(75f, context.getReflectAttackPercent(), 1e-6);
    }
}
