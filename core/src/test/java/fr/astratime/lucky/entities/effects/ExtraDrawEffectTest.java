package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtraDrawEffectTest {

    @Test
    void requestsAnImmediateDrawWhenPlayed() {
        PlayContext context = new PlayContext();

        new ExtraDrawEffect(2).onPlay(context);

        assertEquals(2, context.getCardsToDraw());
    }

    @Test
    void severalDrawEffectsStackOnTheSamePlay() {
        PlayContext context = new PlayContext();

        new ExtraDrawEffect(1).onPlay(context);
        new ExtraDrawEffect(3).onPlay(context);

        assertEquals(4, context.getCardsToDraw());
    }
}
