package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.actions.AttackAction;
import fr.astratime.lucky.entities.actions.DefenseAction;
import fr.astratime.lucky.entities.actions.GainAction;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;
import fr.astratime.lucky.entities.events.ShieldGainedEvent;
import fr.astratime.lucky.entities.exploration.Place;
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
        DamageBreakdown breakdown = (DamageBreakdown) hit.getBreakdown();
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
    void shieldAndGainSymbolsKeepTheirCalculationToo() {
        Player player = new Player("Joueur", Player.BASE_HP, List.of(), RankBonus.NONE, Symbol.classicReels());
        CombatContext context = new CombatContext(player, new Enemy(Place.PORT.getDungeons().get(0).getSoldier()));
        context.addDefenseBonus(10, "Cartes");
        context.multiplyDefense(2f, "Corruption");
        context.multiplyGains(1.5f, "Porte-bonheur");
        context.multiplySymbolPower(3, "Mise");

        ShieldGainedEvent shield = (ShieldGainedEvent) new DefenseAction(20).resolve(context).get(0);
        List<String> shieldLines = shield.getBreakdown().lines(Symbol.GRAPE);
        assertTrue(shieldLines.contains("Cartes : +10"));
        assertTrue(shieldLines.contains("Corruption : x2"));
        assertTrue(shieldLines.contains("Mise : x3"));
        assertFalse(shieldLines.contains("Porte-bonheur : x1,5"), "un multiplicateur de gains ne touche pas le bouclier");
        assertEquals((20 + 10) * 2 * 3, shield.amount);
        assertEquals("= " + Lang.big(shield.amount) + " de bouclier", shieldLines.get(shieldLines.size() - 1));

        GainsEarnedEvent gains = (GainsEarnedEvent) new GainAction(8).resolve(context).get(0);
        List<String> gainLines = gains.getBreakdown().lines(Symbol.BELL);
        assertTrue(gainLines.contains("Porte-bonheur : x1,5"));
        assertTrue(gainLines.contains("Mise : x3"));
        assertTrue(gainLines.contains("Coût du tirage : x5"), "Port : un tirage coûte 500");
        assertFalse(gainLines.contains("Corruption : x2"));
        assertEquals(Math.round(8 * 1.5f * 3 * 5), gains.amount);
        assertEquals("= " + Lang.big(gains.amount) + " gains", gainLines.get(gainLines.size() - 1));
    }

    @Test
    void factorsLoseTheirUselessDecimals() {
        assertEquals("9,5", DamageBreakdown.factor(9.5f));
        assertEquals("10", DamageBreakdown.factor(10f));
        assertEquals("0,75", DamageBreakdown.factor(0.75f));
        assertEquals("1,15", DamageBreakdown.factor(1.1500001f));
    }
}
