package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.context.PlayContext;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EffectPopupTest {

    private static PlayContext play(Effect effect, Player player) {
        PlayContext context = new PlayContext(player);
        effect.onPlay(context);
        return context;
    }

    private static PlayContext play(Effect effect) {
        return play(effect, new Player("Joueur", 100, List.of()));
    }

    private static List<String> texts(PlayContext context) {
        return context.getPopups().stream().map(EffectPopup::getText).toList();
    }

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
            new DiamondReflectEffect(330, 33), new AceOfDiamondsEffect(),
            new SpadeIgnoreDefenseEffect(11), new AceOfSpadesEffect(),
            new AttackEffect(5), new DefenseEffect(5), new MultiplierEffect(1.5f),
            new BoostSymbolEffect(Symbol.values()[0], 50), new ExtraDrawEffect(3), new GainEffect(500)
        );
        for (Effect effect : effects) {
            assertFalse(play(effect).getPopups().isEmpty(), effect.getClass().getSimpleName());
        }
    }

    @Test
    void cardWithSeveralBonusesShowsOneTextPerBonus() {
        assertEquals(List.of("GAINS x+20", "ATTAQUE +10"), texts(play(new ClubGainAttackEffect(20, 10))));
    }

    @Test
    void ordinaryEffectsAreQueuedForTheSpin() {
        ClubGainAttackEffect effect = new ClubGainAttackEffect(20, 10);

        assertEquals(List.of(effect), play(effect).getEffectsForSpin());
    }

    @Test
    void gainEffectCreditsGainsImmediatelyWhenPlayed() {
        PlayContext context = play(new GainEffect(500));

        assertEquals(500, context.getGains());
        assertEquals(List.of("GAINS +500"), texts(context));
        assertTrue(context.getEffectsForSpin().isEmpty());
    }

    @Test
    void aceOfClubsConsumesGainsWhenPlayedAndShowsTheComputedBonuses() {
        Player player = new Player("Joueur", 100, List.of());
        player.addGains(1000);

        PlayContext context = play(new AceOfClubsEffect(), player);

        // 30% de 1000 = 300 consommés -> attaque 115 + 300/2 = 265, boost 150 + 300 = 450
        assertEquals(700, player.getGains(), "le coût est payé à la pose de la carte");
        assertEquals(List.of("-30% GAINS", "ATTAQUE +265", "BOOST SYMBOLE +450"), texts(context));
        assertTrue(context.getEffectsForSpin().stream().anyMatch(e -> e instanceof AttackEffect));
        assertFalse(context.getEffectsForSpin().stream().anyMatch(e -> e instanceof AceOfClubsEffect),
            "l'As lui-même n'est pas réappliqué au spin : il consommerait les gains une seconde fois");
    }
}
