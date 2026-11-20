package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.events.Event;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Drain de vie relatif (2026-11-20) : le soin suit la part des PV de l'ennemi enlevée, rapportée aux PV max du joueur. */
class LifeDrainTest {

    @Test
    void theHealFollowsTheShareOfTheEnemysHpNotTheRawDamage() {
        // 10 % de drain, 10 % des PV de l'ennemi enlevés : 10 % des PV max du joueur, quels que soient les PV de l'ennemi
        assertEquals(100, LifeDrain.amount(10, 10_000, 100_000, 1_000));
        assertEquals(100, LifeDrain.amount(10, 10_000_000, 100_000_000, 1_000));
        assertEquals(0, LifeDrain.amount(0, 10_000, 100_000, 1_000));
        assertEquals(1_000, LifeDrain.amount(10, 5_000_000, 100_000, 1_000), "jamais plus que tous ses PV");
        assertEquals("10", LifeDrain.per10(10));
        assertEquals("14", LifeDrain.per10(14));
    }

    @Test
    void aPlayersBloodIsCappedAtFiveTimesItsMaxHp() {
        Player player = new Player("Joueur", 1_000, List.of());
        assertEquals(5_000, player.getLastingEffects().getMaxBlood());
        player.getLastingEffects().addBlood(1_000_000);
        assertEquals(5_000, player.getLastingEffects().getBlood());
    }

    @Test
    void oneTurnHealsHalfTheMaxHpAtMostAndTheExcessFillsTheBlood() {
        Player player = new Player("Joueur", 1_000, List.of());
        player.takeDamage(100);
        CombatContext context = new CombatContext(player, new Enemy("Ennemi", 100_000));
        List<Event> events = new ArrayList<>();
        LifeDrain.apply(context, 100_000, 50, events); // voudrait rendre 5 000
        assertEquals(1_000, player.getHp(), "les 100 PV manquants");
        assertEquals(400, player.getLastingEffects().getBlood(), "plafond 500 : 400 en trop, dans le Sang");
        LifeDrain.apply(context, 100_000, 50, events);
        assertEquals(400, player.getLastingEffects().getBlood(), "plafond du tour atteint");

        CombatContext nextTurn = new CombatContext(player, new Enemy("Ennemi", 100_000));
        LifeDrain.apply(nextTurn, 10_000, 10, events);
        assertEquals(500, player.getLastingEffects().getBlood(), "tour suivant : 100 de soin, tout en Sang");
    }
    @Test
    void underThirstTheDrainLosesItsHalvedPartInsteadOfFillingTheBlood() {
        Player player = new Player("Joueur", 1_000, List.of());
        player.takeDamage(100);
        player.setHealDivisor(2);
        CombatContext context = new CombatContext(player, new Enemy("Ennemi", 100_000));
        List<Event> events = new ArrayList<>();
        LifeDrain.apply(context, 10_000, 40, events); // voudrait rendre 400, la Soif en laisse 200
        assertEquals(1_000, player.getHp(), "les 100 PV manquants");
        assertEquals(100, player.getLastingEffects().getBlood(), "seul le vrai surplus (200 - 100) remplit le Sang");
    }
}
