package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.effects.AttackEffect;
import fr.astratime.lucky.entities.effects.BoostSymbolEffect;
import fr.astratime.lucky.entities.effects.MultiplierEffect;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PreparationResolverTest {

    @Test
    void appliesEveryPendingEffectToAFreshTurnContext() {
        Player player = new Player("Joueur", 100, List.of());
        Enemy  enemy  = new Enemy("Ennemi", 1000);

        TurnContext context = new PreparationResolver().resolve(
            List.of(new AttackEffect(10), new AttackEffect(5), new MultiplierEffect(2f), new BoostSymbolEffect(Symbol.BELL, 40)),
            player, enemy);

        assertEquals(15, context.getCombatContext().getAttackBonus());
        assertEquals(3f, context.getCombatContext().getGainMultiplier(), 1e-6);
        assertEquals(40, context.getSpinContext().getWeightBoost(Symbol.BELL));
        assertEquals(1, context.getEvents().size(), "le boost de symbole est journalisé");
        assertSame(player, context.getCombatContext().getPlayer());
    }
}
