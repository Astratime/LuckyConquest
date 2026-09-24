package fr.astratime.lucky.views;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SidePanelTest {

    @Test
    void formatGainsGroupsDigitsByThree() {
        assertEquals("0", SidePanel.formatGains(0));
        assertEquals("500", SidePanel.formatGains(500));
        assertEquals("1 000", SidePanel.formatGains(1000));
        assertEquals("12 500", SidePanel.formatGains(12500));
        assertEquals("1 234 567", SidePanel.formatGains(1234567));
    }

    @Test
    void formatGainsKeepsTheSign() {
        assertEquals("-1 500", SidePanel.formatGains(-1500));
    }
}
