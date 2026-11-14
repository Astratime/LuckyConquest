package fr.astratime.lucky.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import fr.astratime.lucky.entities.FullPotential.Gauges;
import fr.astratime.lucky.entities.effects.AceOfClubsEffect;
import fr.astratime.lucky.entities.effects.AceOfDiamondsEffect;
import fr.astratime.lucky.entities.effects.AceOfHeartsEffect;
import fr.astratime.lucky.entities.effects.AceOfSpadesEffect;
import fr.astratime.lucky.entities.effects.Effect;

/** Les As à plein potentiel, qui déclenchent leur animation. */
class FullPotentialTest {

    private static Card card(Effect effect) {
        return new Card("test", "Test", "cards/light/1-H.png", List.of(effect), Card.Suit.COEUR, 1, false, false, false);
    }

    @Test
    void aceOfHeartsNeedsFullBlood() {
        Card ace = card(new AceOfHeartsEffect());
        assertEquals(Optional.of(FullPotential.HEARTS), FullPotential.of(ace, new Gauges(LastingEffects.MAX_BLOOD, 0, 0, 0)));
        assertEquals(Optional.empty(), FullPotential.of(ace, new Gauges(LastingEffects.MAX_BLOOD - 1, 0, 0, 0)));
    }

    @Test
    void aceOfSpadesNeedsTenBlades() {
        Card ace = card(new AceOfSpadesEffect());
        assertEquals(Optional.of(FullPotential.SPADES), FullPotential.of(ace, new Gauges(0, 0, 0, 10)));
        assertEquals(Optional.empty(), FullPotential.of(ace, new Gauges(0, 0, 0, 9)));
    }

    @Test
    void aceOfDiamondsNeedsFullVault() {
        Card ace = card(new AceOfDiamondsEffect());
        assertEquals(Optional.of(FullPotential.DIAMONDS), FullPotential.of(ace, new Gauges(0, LastingEffects.MAX_VAULT, 0, 0)));
        assertEquals(Optional.empty(), FullPotential.of(ace, new Gauges(0, LastingEffects.MAX_VAULT - 1, 0, 0)));
    }

    @Test
    void aceOfClubsNeedsGuaranteedBingo() {
        Card ace = card(new AceOfClubsEffect());
        int needed = AceOfClubsEffect.GUARANTEED - AceOfClubsEffect.OWN_STARS; // la carte ajoute ses propres points
        assertEquals(Optional.of(FullPotential.CLUBS), FullPotential.of(ace, new Gauges(0, 0, needed, 0)));
        assertEquals(Optional.empty(), FullPotential.of(ace, new Gauges(0, 0, needed - 1, 0)));
        Card star = card(new AceOfClubsEffect(AceOfClubsEffect.Kind.GAINS, 1f));
        assertEquals(Optional.of(FullPotential.CLUBS), FullPotential.of(star, new Gauges(0, 0, needed, 0)));
    }

    @Test
    void otherCardsNeverAnimate() {
        Card ace = card(new AceOfHeartsEffect());
        Card other = new Card("x", "X", "cards/light/2-H.png", List.of(), Card.Suit.COEUR, 2, false, false, false);
        assertEquals(Optional.empty(), FullPotential.of(other, new Gauges(LastingEffects.MAX_BLOOD, LastingEffects.MAX_VAULT, 100, 50)));
        assertEquals(Optional.empty(), FullPotential.of(ace, new Gauges(0, LastingEffects.MAX_VAULT, 100, 50)));
    }
}
