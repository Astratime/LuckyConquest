package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.context.PlayContext;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EffectPopupTest {

    @Test
    void intensityIsClampedBetweenZeroAndOne() {
        assertEquals(1f, EffectPopup.scaled("X", EffectPopup.Style.GAINS, 5000, 1000).getIntensity());
        assertEquals(0f, new EffectPopup("X", EffectPopup.Style.GAINS, -2f).getIntensity());
        assertEquals(0.5f, EffectPopup.scaled("X", EffectPopup.Style.GAINS, 500, 1000).getIntensity(), 1e-6);
    }

    @Test
    void everyEffectShowsAtLeastOnePopupWhenPlayed() {
        List<Effect> effects = List.of(
            new HeartDrainEffect(12), new AceOfHeartsEffect(),
            new ClubGainAttackEffect(20, 10), new AceOfClubsEffect(),
            new DiamondReflectEffect(33), new AceOfDiamondsEffect(),
            new SpadeIgnoreDefenseEffect(11), new AceOfSpadesEffect(),
            new AttackEffect(5), new DefenseEffect(5), new MultiplierEffect(1.5f),
            new BoostSymbolEffect(Symbol.values()[0], 50), new ExtraDrawEffect(3), new GainEffect(500)
        );
        for (Effect effect : effects) {
            assertFalse(effect.getPopups().isEmpty(), effect.getClass().getSimpleName());
        }
    }

    @Test
    void clubCardShowsGainsThenAttack() {
        List<EffectPopup> popups = new ClubGainAttackEffect(20, 10).getPopups();

        assertEquals("GAINS x+20", popups.get(0).getText());
        assertEquals(EffectPopup.Style.GAINS, popups.get(0).getStyle());
        assertEquals("ATTAQUE +10", popups.get(1).getText());
    }

    @Test
    void gainEffectCreditsGainsImmediatelyWhenPlayed() {
        PlayContext context = new PlayContext();

        new GainEffect(500).onPlay(context);

        assertEquals(500, context.getGains());
        assertEquals(0, context.getCardsToDraw());
    }
}
