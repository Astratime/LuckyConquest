package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.popups.EffectPopup;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventPopupTest {

    private static String text(Event event) {
        List<EffectPopup> popups = event.getPopups();
        assertEquals(1, popups.size(), event.getClass().getSimpleName());
        return popups.get(0).getText();
    }

    @Test
    void spinResultsAreShownWithTheirValues() {
        assertEquals("DÉGÂTS 45", text(new EnemyDamagedEvent(45, 45, 0, 0, false)));
        assertEquals("GAINS +500", text(new GainsEarnedEvent(500)));
        assertEquals("BOUCLIER +8", text(new ShieldGainedEvent(8)));
        assertEquals("PV -10", text(new PlayerDamagedEvent(10)));
        assertEquals("VIE +4", text(new PlayerHealedEvent(4)));
        assertEquals("RENVOI 5", text(new DamageReflectedEvent(5)));
        assertEquals("JACKPOT !", text(new JackpotEvent(Symbol.SEVEN)));
    }

    @Test
    void fullyShieldedRiposteShowsBlocked() {
        EffectPopup popup = new PlayerDamagedEvent(0).getPopups().get(0);

        assertEquals("BLOQUÉ !", popup.getText());
        assertEquals(EffectPopup.Style.DEFENSE, popup.getStyle());
    }

    @Test
    void blockedHitsShowWhatTheShieldAbsorbed() {
        List<EffectPopup> partly = new EnemyDamagedEvent(40, 70, 30, 0, false).getPopups();
        assertEquals(List.of("DÉGÂTS 40", "BLOQUÉ -30"), partly.stream().map(EffectPopup::getText).toList());
        assertEquals(EffectPopup.Style.DEFENSE, partly.get(1).getStyle());

        assertEquals("BLOQUÉ ! -25", text(new EnemyDamagedEvent(0, 25, 25, 75, false)));
        assertEquals("BLOQUÉ ! -20", text(new PlayerDamagedEvent(0, 20, 5)));
        assertEquals(List.of("PV -15", "BLOQUÉ -5"),
            new PlayerDamagedEvent(15, 5, 0).getPopups().stream().map(EffectPopup::getText).toList());
    }

    @Test
    void lostLifeUsesTheDamageStyle() {
        assertEquals(EffectPopup.Style.DAMAGE, new PlayerDamagedEvent(10).getPopups().get(0).getStyle());
    }

    @Test
    void symbolBoostIsNotShownAgainAtSpin() {
        assertTrue(new SymbolBoostedEvent(Symbol.SEVEN, 100).getPopups().isEmpty(),
            "déjà affiché quand la carte a été jouée");
    }
}
