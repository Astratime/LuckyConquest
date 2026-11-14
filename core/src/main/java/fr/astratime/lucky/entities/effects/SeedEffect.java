package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.WearCards;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Graine (carte à usure, demande d'Astra du 2026-11-14) : un petit bonus d'attaque, qui pousse à chaque tour
 * passé sans la jouer (dans la main, le deck ou la défausse : la main est vidée à chaque tour). Jouée, elle est
 * récoltée et repart de son bonus de départ.
 */
public class SeedEffect extends Effect {

    private final int percent;
    private final int growth;
    private final int max;

    /**
     * @param percent attaque en plus si elle est jouée tout de suite, en %
     * @param growth  attaque gagnée à chaque tour passé sans être jouée, en %
     * @param max     attaque en plus au plus, en %
     */
    public SeedEffect(int percent, int growth, int max) {
        this.percent = percent;
        this.growth  = growth;
        this.max     = max;
    }

    /** Fin du tour : la Graine qui n'a pas été jouée pousse (voir {@link WearCards#growSeed}). */
    public void grow(WearCards wear) { wear.growSeed(percent, growth, max); }

    @Override
    public void onPlay(PlayContext context) {
        int bonus = context.getLastingEffects().getWearCards().harvestSeed(percent, growth, max);
        context.queueForSpin(new WornAttack(bonus, "Graine"));
        context.addPopups(List.of(EffectPopup.scaled(Lang.f("ATTAQUE +{0} %", bonus), EffectPopup.Style.ATTACK, bonus,
            PopupScale.CARD_ATTACK_BONUS * 2),
            new EffectPopup(Lang.t("RÉCOLTE : LA GRAINE REPART"), EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY)));
    }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().multiplyAttack(1f + percent / 100f, "Graine"); }

    @Override
    public String getDescription() {
        return Lang.f("Attaque +{0} %, et +{1} % de plus pour chaque tour passé sans la jouer ({2} % au plus). "
            + "Une fois jouée, elle repart de +{0} %.", percent, growth, max);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(EffectPopup.scaled(Lang.f("ATTAQUE +{0} %", percent), EffectPopup.Style.ATTACK, percent,
            PopupScale.CARD_ATTACK_BONUS * 2));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }
}
