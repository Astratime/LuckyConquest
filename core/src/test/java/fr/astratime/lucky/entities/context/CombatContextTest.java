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
        context.addReflectHpPercent(3.5f);

        assertEquals(0f, context.getReflectHpPercent(), 1e-6, "sans symbole de défense, pas de renvoi");

        context.markDefenseSymbolDrawn();
        assertEquals(3.5f, context.getReflectHpPercent(), 1e-6);
        assertEquals(0, context.getTotalReflectPercent(), "les Carreaux ne renvoient pas l'attaque ennemie");
    }

    @Test
    void diamondReflectsAddUpToTheirCap() {
        context.addReflectHpPercent(7f);   // Dame de Carreau
        context.addReflectHpPercent(7.5f); // Roi de Carreau
        context.markDefenseSymbolDrawn();
        assertEquals(14.5f, context.getReflectHpPercent(), 1e-6);

        context.addReflectHpPercent(6.5f);
        context.addReflectHpPercent(6f);
        assertEquals(20f, CombatContext.MAX_REFLECT_HP_PERCENT, 1e-6);
        assertEquals(CombatContext.MAX_REFLECT_HP_PERCENT, context.getReflectHpPercent(), 1e-6, "20 % au plus");
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
        context.addReflectHpPercent(3.75f);
        context.markDefenseSymbolDrawn();

        assertEquals(500, context.getTotalReflectPercent());
        assertEquals(3.75f, context.getReflectHpPercent(), 1e-6);
    }
}
