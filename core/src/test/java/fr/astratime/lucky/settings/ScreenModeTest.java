package fr.astratime.lucky.settings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScreenModeTest {

    @Test
    void togglingSwitchesBetweenTheTwoModes() {
        assertEquals(ScreenMode.FULLSCREEN, ScreenMode.WINDOWED.toggled());
        assertEquals(ScreenMode.WINDOWED, ScreenMode.FULLSCREEN.toggled());
    }

    @Test
    void aSavedModeIsReadBackAndAnUnknownOneFallsBackToWindowed() {
        for (ScreenMode mode : ScreenMode.values()) assertEquals(mode, ScreenMode.parse(mode.name()));
        assertEquals(ScreenMode.WINDOWED, ScreenMode.parse("n'importe quoi"), "fenêtré par défaut");
        assertEquals(ScreenMode.WINDOWED, ScreenMode.parse(null));
    }
}
