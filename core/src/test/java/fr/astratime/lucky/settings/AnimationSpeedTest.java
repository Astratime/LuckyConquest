package fr.astratime.lucky.settings;

import fr.astratime.lucky.i18n.Lang;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** La vitesse des animations des Options : x1, x1,5, x2, en boucle. */
class AnimationSpeedTest {

    @AfterEach
    void backToFrench() {
        Lang.use(Lang.Language.FRENCH, "{}");
    }

    @Test
    void theSpeedsAreOneOneAndAHalfAndTwo() {
        assertEquals(1f, AnimationSpeed.X1.factor());
        assertEquals(1.5f, AnimationSpeed.X1_5.factor());
        assertEquals(2f, AnimationSpeed.X2.factor());
    }

    @Test
    void steppingLoopsBothWays() {
        assertEquals(AnimationSpeed.X1_5, AnimationSpeed.X1.step(1));
        assertEquals(AnimationSpeed.X2, AnimationSpeed.X1_5.step(1));
        assertEquals(AnimationSpeed.X1, AnimationSpeed.X2.step(1));
        assertEquals(AnimationSpeed.X2, AnimationSpeed.X1.step(-1));
    }

    @Test
    void anUnknownSavedSpeedIsNormal() {
        assertEquals(AnimationSpeed.X1, AnimationSpeed.parse(null));
        assertEquals(AnimationSpeed.X1, AnimationSpeed.parse("X3"));
        assertEquals(AnimationSpeed.X2, AnimationSpeed.parse("X2"));
    }

    @Test
    void theLabelUsesTheDecimalSeparatorOfTheLanguage() {
        Lang.use(Lang.Language.FRENCH, "{}");
        assertEquals("Animations : x1,5", AnimationSpeed.X1_5.label());
        Lang.use(Lang.Language.ENGLISH, "{\"Animations : x{0}\": \"Animations: x{0}\"}");
        assertEquals("Animations: x1.5", AnimationSpeed.X1_5.label());
        assertEquals("Animations: x2", AnimationSpeed.X2.label());
    }
}
