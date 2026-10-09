package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.actions.AttackAction;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.i18n.Lang;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Le détail du coup (voir {@link DamageBreakdown}) et les grands nombres abrégés ({@link Lang#big}). */
class DamageBreakdownTest {

    @AfterEach
    void backToFrench() {
        Lang.use(Lang.Language.FRENCH, "{}");
    }

    @Test
    void bigNumbersAreShortenedFromAMillion() {
        assertEquals("950", Lang.big(950));
        assertEquals("999 999", Lang.big(999_999));
        assertEquals("1 M", Lang.big(1_000_000));
        assertEquals("5,5 M", Lang.big(5_500_000));
        assertEquals("12,3 M", Lang.big(12_340_000));
        assertEquals("2 Md", Lang.big(2_000_000_000L));
        assertEquals("1,2 Md", Lang.big(1_234_567_890L));
        assertEquals("-5,5 M", Lang.big(-5_500_000));

        Lang.use(Lang.Language.ENGLISH, "{}");
        assertEquals("999,999", Lang.big(999_999));
        assertEquals("5.5M", Lang.big(5_500_000));
        assertEquals("1.2B", Lang.big(1_234_567_890L));
    }

    @Test
    void anAttackKeepsItsCalculationStepByStep() {
        Player player = new Player("Joueur", Player.BASE_HP, List.of(), RankBonus.NONE, Symbol.classicReels());
        Enemy enemy = new Enemy(EnemyKind.ENTRAINEMENT);
        CombatContext context = new CombatContext(player, enemy);
        context.addAttackBonus(20, "Cartes");
        context.multiplyAttack(2.5f, "Corruption");
        context.multiplySymbolPower(3, "Mise");

        EnemyDamagedEvent hit = (EnemyDamagedEvent) new AttackAction(30).resolve(context).get(0);
        DamageBreakdown breakdown = hit.getBreakdown();
        assertNotNull(breakdown);
        assertEquals(30, breakdown.base());
        assertEquals((30 + 20) * 2.5 * 3, breakdown.uncapped());
        assertEquals(hit.damage, breakdown.damage());

        List<String> lines = breakdown.lines(Symbol.SEVEN);
        assertEquals(Symbol.SEVEN.getDisplayName() + " : 30", lines.get(0));
        assertTrue(lines.contains("Cartes : +20"));
        assertTrue(lines.contains("Corruption : x2,5"));
        assertTrue(lines.contains("Mise : x3"));
        assertEquals("= " + Lang.big(hit.damage) + " dégâts", lines.get(lines.size() - 1));
    }

    @Test
    void factorsLoseTheirUselessDecimals() {
        assertEquals("9,5", DamageBreakdown.factor(9.5f));
        assertEquals("10", DamageBreakdown.factor(10f));
        assertEquals("0,75", DamageBreakdown.factor(0.75f));
        assertEquals("1,15", DamageBreakdown.factor(1.1500001f));
    }
}
