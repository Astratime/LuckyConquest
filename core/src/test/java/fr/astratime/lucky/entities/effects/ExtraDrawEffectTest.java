package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.context.PlayContext;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExtraDrawEffectTest {

    @Test
    void requestsAnImmediateDrawWhenPlayed() {
        PlayContext context = new PlayContext(new Player("Joueur", 100, List.of()));

        new ExtraDrawEffect(2).onPlay(context);

        assertEquals(2, context.getCardsToDraw());
        assertTrue(context.getEffectsForSpin().isEmpty(), "la pioche est immédiate : rien à appliquer au spin");
    }

    @Test
    void severalDrawEffectsStackOnTheSamePlay() {
        PlayContext context = new PlayContext(new Player("Joueur", 100, List.of()));

        new ExtraDrawEffect(1).onPlay(context);
        new ExtraDrawEffect(3).onPlay(context);

        assertEquals(4, context.getCardsToDraw());
    }
}
