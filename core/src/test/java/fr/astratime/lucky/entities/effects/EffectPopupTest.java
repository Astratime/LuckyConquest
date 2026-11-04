package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.popups.EffectPopup;

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
            new DiamondReflectEffect(3.5f, 33), new AceOfDiamondsEffect(),
            new SpadeIgnoreDefenseEffect(11, 1, 40), new AceOfSpadesEffect(), new CorruptionEffect(3),
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

        // 30% de 1000 = 300 consommés -> attaque 115 + 10 x racine(300) = 288, boost 150 + 300 = 450
        assertEquals(700, player.getGains(), "le coût est payé à la pose de la carte");
        assertEquals(List.of("-300 GAINS", "ATTAQUE +288", "BOOST SYMBOLE +450"), texts(context));
        assertTrue(context.getEffectsForSpin().stream().anyMatch(e -> e instanceof AttackEffect));
        assertFalse(context.getEffectsForSpin().stream().anyMatch(e -> e instanceof AceOfClubsEffect),
            "l'As lui-même n'est pas réappliqué au spin : il consommerait les gains une seconde fois");
    }

    @Test
    void aceOfClubsNeverConsumesMoreThanWhatStillAddsAttack() {
        Player player = new Player("Joueur", 100, List.of());
        player.addGains(10_000_000);

        PlayContext context = play(new AceOfClubsEffect(), player);

        // 1 000 000 de gains suffisent à atteindre +10 000 d'attaque : le reste n'est pas pris.
        assertEquals(9_000_000, player.getGains());
        assertEquals(List.of("-1 M GAINS", "ATTAQUE +10115", "BOOST SYMBOLE +1150"), texts(context));
    }

    @Test
    void aceOfSpadesOnlyConsumesTheGainsThatBuyBlades() {
        Player player = new Player("Joueur", 100, List.of());
        player.addGains(10_000_000);
        play(new AceOfSpadesEffect(), player);
        assertEquals(10, player.getLastingEffects().getBlades(), "10 Lames au plus");
        assertEquals(10_000_000 - 10 * 250, player.getGains(), "10 x 250 gains, pas 1 000 000");

        Player small = new Player("Joueur", 100, List.of());
        small.addGains(7_400); // 10 % = 740 : 2 Lames pour 500, les 240 restants n'achètent rien
        play(new AceOfSpadesEffect(), small);
        assertEquals(2, small.getLastingEffects().getBlades());
        assertEquals(7_400 - 500, small.getGains());
    }

    @Test
    void theBladePriceFollowsTheSpinCost() {
        Player player = new Player("Joueur", 100, List.of());
        player.addGains(100_000_000);
        PlayContext context = new PlayContext(player, 140_000); // Désert
        new AceOfSpadesEffect().onPlay(context);
        // 10 % = 10 M ; une Lame coûte 2,5 tirages = 350 000 : 10 Lames (le plafond) pour 3,5 M
        assertEquals(10, player.getLastingEffects().getBlades());
        assertEquals(100_000_000 - 10 * 350_000, player.getGains());
    }

    @Test
    void aceOfClubsCountsGainsAtThePlacePowerSpinCostLikeFortune() {
        Player player = new Player("Joueur", 100, List.of());
        player.addGains(1_000_000_000);
        PlayContext context = new PlayContext(player, 140_000); // Désert : tirage de force 3 000
        new AceOfClubsEffect().onPlay(context);
        // 1 M de gains « de force » = 1 M x 140 000 / 3 000 = 46,7 M de gains réels, pour +10 000
        assertEquals(1_000_000_000 - 46_666_667, player.getGains());
        assertTrue(texts(context).contains("ATTAQUE +10115"), texts(context).toString());

        Player poor = new Player("Joueur", 100, List.of());
        poor.addGains(3_000_000); // 30 % = 900 000 réels = 19 286 de force : +1 389 d'attaque
        PlayContext small = new PlayContext(poor, 140_000);
        new AceOfClubsEffect().onPlay(small);
        assertEquals(2_100_000, poor.getGains());
        assertTrue(texts(small).contains("ATTAQUE +1504"), texts(small).toString());
    }
}
