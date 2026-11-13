package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.context.SpinContext;
import fr.astratime.lucky.entities.SymbolRegistry;
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
            new ClubLuckyStarEffect(10, 4), new CardGainsEffect(2), new AceOfClubsEffect(),
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
        assertEquals(List.of("BONNE ÉTOILE +4", "ATTAQUE +10"), texts(play(new ClubLuckyStarEffect(10, 4))));
    }

    @Test
    void ordinaryEffectsAreQueuedForTheSpin() {
        CardGainsEffect effect = new CardGainsEffect(2);

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
    void aceOfClubsEmptiesTheLuckyStarForAGuaranteedBingo() {
        Player player = new Player("Joueur", 100, List.of());
        player.getLastingEffects().addLuckyStar(20); // + 10 de la carte = 30 : garanti

        PlayContext context = play(new AceOfClubsEffect(), player);

        assertEquals(0, player.getLastingEffects().getLuckyStar(), "la jauge est vidée à la pose");
        assertEquals(0, player.getGains(), "plus aucun gain consommé");
        assertEquals(1, context.getEffectsForSpin().size(), "le Bingo est imposé au tirage");
        SpinContext spin = new SpinContext();
        context.getEffectsForSpin().forEach(e -> e.apply(new TurnContext(spin, new CombatContext(player, null))));
        assertTrue(spin.isJackpotForced());
    }

    @Test
    void aceOfClubsChanceFollowsTheLuckyStar() {
        AceOfClubsEffect ace = new AceOfClubsEffect();
        assertEquals(0.5f, ace.chance(15), 1e-6);
        assertEquals(1f, ace.chance(45), 1e-6);
        assertEquals(20, new AceOfClubsEffect(1.5f).getGuaranteed(), "version « + » : garanti dès 20");

        Player player = new Player("Joueur", 100, List.of());
        AceOfClubsEffect unlucky = new AceOfClubsEffect() {
            @Override protected boolean roll(float chance) { return false; }
        };
        PlayContext context = play(unlucky, player);
        assertEquals(List.of("BONNE ÉTOILE 33 % : PAS DE BINGO"), texts(context));
        assertTrue(context.getEffectsForSpin().isEmpty());
    }

    @Test
    void theStarsAimAtTheirKindOfSymbol() {
        for (AceOfClubsEffect.Kind kind : AceOfClubsEffect.Kind.values()) {
            Player player = new Player("Joueur", 100, List.of());
            player.getLastingEffects().addLuckyStar(100);
            PlayContext context = play(new AceOfClubsEffect(kind, 1f), player);
            SpinContext spin = new SpinContext();
            context.getEffectsForSpin().forEach(e -> e.apply(new TurnContext(spin, new CombatContext(player, null))));
            Symbol symbol = spin.getJackpotSymbol();
            switch (kind) {
                case ANY     -> assertEquals(null, symbol);
                case ATTACK  -> assertTrue(SymbolRegistry.getAttackSymbols().contains(symbol));
                case DEFENSE -> assertTrue(SymbolRegistry.getDefenseSymbols().contains(symbol));
                case GAINS   -> assertTrue(SymbolRegistry.getGainSymbols().contains(symbol));
            }
        }
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
}
