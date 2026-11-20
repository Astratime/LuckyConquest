package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;
import java.util.Random;

/**
 * Ticket à gratter (échoppe, 2026-11-20) : un pur pari. Joué, il se gratte tout de suite et rapporte
 * 0, 4, 6 ou 10 fois le coût du tirage en gains (4 en moyenne, autant que son prix de base), sans aucun
 * multiplicateur. Comme toute carte de l'échoppe, son prix double à chaque achat dans le combat.
 */
public class ScratchTicketEffect extends Effect {

    /** Lots, en coûts du tirage, et leur chance (%) : 0 (40), 4 (20), 6 (20), 10 (20). */
    public static final int[] PRIZES  = {0, 4, 6, 10};
    public static final int[] CHANCES = {40, 20, 20, 20};

    private static final Random RANDOM = new Random();
    private final Random random;

    public ScratchTicketEffect() { this(RANDOM); }

    /** @param random tirage du lot (tests) */
    public ScratchTicketEffect(Random random) { this.random = random; }

    @Override
    public void onPlay(PlayContext context) {
        int prize = draw(random.nextInt(100));
        double won = (double) prize * context.getSpinCost();
        if (won > 0) {
            context.addGains(won);
            context.addPopups(List.of(EffectPopup.scaled(Lang.f("TICKET GAGNANT : GAINS +{0}", Lang.big(won)),
                EffectPopup.Style.GAINS, prize * 100, PopupScale.CARD_GAINS)));
        } else {
            context.addPopups(List.of(new EffectPopup(Lang.t("TICKET PERDANT"), EffectPopup.Style.DAMAGE,
                PopupScale.SECONDARY_INTENSITY)));
        }
    }

    /** @param roll nombre de 0 à 99 @return le lot, en coûts du tirage */
    static int draw(int roll) {
        int sum = 0;
        for (int i = 0; i < PRIZES.length; i++) {
            sum += CHANCES[i];
            if (roll < sum) return PRIZES[i];
        }
        return 0;
    }

    /** Rien au tirage : le ticket est gratté quand il est joué. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() {
        return Lang.t("Gratte tout de suite : 0, 4, 6 ou 10 fois le coût du tirage en gains (4 en moyenne), "
            + "sans multiplicateur. Un pur pari");
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("TICKET À GRATTER"), EffectPopup.Style.GAINS, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.GAIN; }
}
