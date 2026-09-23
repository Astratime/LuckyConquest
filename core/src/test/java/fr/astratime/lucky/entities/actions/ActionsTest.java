package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;
import fr.astratime.lucky.entities.events.PlayerHealedEvent;
import fr.astratime.lucky.entities.events.ShieldGainedEvent;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Actions des symboles : attaque (défense, drain, gains), défense et gains. */
class ActionsTest {

    private final Player        player  = new Player("Joueur", 100, List.of());
    private final Enemy         enemy   = new Enemy("Ennemi", 1000); // défense par défaut = 30
    private final CombatContext context = new CombatContext(player, enemy);

    private static <T extends Event> T only(List<Event> events, Class<T> type) {
        List<T> matching = events.stream().filter(type::isInstance).map(type::cast).toList();
        assertEquals(1, matching.size(), type.getSimpleName());
        return matching.get(0);
    }

    @Test
    void attackIsReducedByEnemyDefense() {
        context.addAttackBonus(100);

        EnemyDamagedEvent event = only(new AttackAction(30).resolve(context), EnemyDamagedEvent.class);

        assertEquals(130, event.rawDamage);
        assertEquals(100, event.damage, "130 - 30 de défense");
        assertEquals(900, enemy.getHp());
    }

    @Test
    void attackBelowDefenseDealsNothing() {
        EnemyDamagedEvent event = only(new AttackAction(10).resolve(context), EnemyDamagedEvent.class);

        assertEquals(0, event.damage);
        assertEquals(1000, enemy.getHp());
    }

    @Test
    void ignoreDefenseDealsFullDamage() {
        context.setIgnoreDefense(true);

        EnemyDamagedEvent event = only(new AttackAction(10).resolve(context), EnemyDamagedEvent.class);

        assertEquals(10, event.damage);
    }

    @Test
    void lifeDrainHealsOnlyMissingHp() {
        player.takeDamage(10);
        context.addLifeDrainPercent(50);
        context.addAttackBonus(100);

        List<Event> events = new AttackAction(30).resolve(context); // 100 dégâts -> drain 50

        assertEquals(10, only(events, PlayerHealedEvent.class).amount, "seuls les 10 PV manquants sont rendus");
        assertEquals(100, player.getHp());
    }

    @Test
    void gainsFromDamageConvertsDamageIntoGains() {
        context.setGainsFromDamage(true);
        context.setIgnoreDefense(true);

        List<Event> events = new AttackAction(40).resolve(context);

        assertEquals(40, only(events, GainsEarnedEvent.class).amount);
        assertEquals(40, player.getGains());
    }

    @Test
    void defenseGivesShieldAndEnablesDiamondReflect() {
        context.addDefenseBonus(5);

        ShieldGainedEvent event = only(new DefenseAction(8).resolve(context), ShieldGainedEvent.class);

        assertEquals(13, event.amount);
        assertEquals(13, player.getShield());
        assertTrue(context.isDefenseSymbolDrawn());
    }

    @Test
    void gainsAreMultipliedByTheGainMultiplier() {
        context.addGainMultiplier(2f); // 1 + 2 = x3

        GainsEarnedEvent event = only(new GainAction(8).resolve(context), GainsEarnedEvent.class);

        assertEquals(24, event.amount);
        assertEquals(24, player.getGains());
    }
}
