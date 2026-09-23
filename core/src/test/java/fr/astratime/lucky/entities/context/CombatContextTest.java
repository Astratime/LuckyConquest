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
        context.addReflectPercentBonus(330);

        assertEquals(0, context.getTotalReflectPercent(), "sans symbole de défense, pas de renvoi");

        context.markDefenseSymbolDrawn();
        assertEquals(330, context.getTotalReflectPercent());
    }

    @Test
    void diamondReflectsAddUp() {
        context.addReflectPercentBonus(330);
        context.addReflectPercentBonus(390);
        context.markDefenseSymbolDrawn();

        assertEquals(720, context.getTotalReflectPercent());
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
    void guaranteedAndDiamondReflectsAddUp() {
        context.addGuaranteedReflect(500, 1000);
        context.addReflectPercentBonus(390);
        context.markDefenseSymbolDrawn();

        assertEquals(890, context.getTotalReflectPercent());
    }
}
