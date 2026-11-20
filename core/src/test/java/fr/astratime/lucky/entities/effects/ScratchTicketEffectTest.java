package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.context.PlayContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Ticket à gratter : 0, 4, 6 ou 10 coûts du tirage, 4 en moyenne, sans multiplicateur. */
class ScratchTicketEffectTest {

    @Test
    void thePrizesAverageFourSpinCosts() {
        int total = 0;
        for (int roll = 0; roll < 100; roll++) total += ScratchTicketEffect.draw(roll);
        assertEquals(400, total, "4 coûts du tirage en moyenne, autant que le prix de base");
        assertEquals(0, ScratchTicketEffect.draw(0));
        assertEquals(10, ScratchTicketEffect.draw(99));
    }

    @Test
    void scratchingAddsThePrizeTimesTheSpinCostAtOnce() {
        Player player = new Player("Joueur", 100, List.of());
        PlayContext context = new PlayContext(player, 500);
        Random fixed = new Random() { @Override public int nextInt(int bound) { return 99; } }; // lot de 10
        new ScratchTicketEffect(fixed).onPlay(context);
        assertEquals(5_000.0, context.getGains());
    }
}
