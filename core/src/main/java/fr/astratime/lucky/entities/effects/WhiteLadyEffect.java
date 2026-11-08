package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Dame blanche (archétype Revenant) : plus d'attaque pour chaque carte de la
 * défausse, comptée quand la Dame est jouée.
 */
public class WhiteLadyEffect extends Effect {
    private final int perCard;
    private final int maxPercent;
    /** Attaque en plus comptée à la pose, en % (-1 : la carte elle-même, pas encore jouée). */
    private final int counted;

    /**
     * Dame blanche.
     * @param perCard    attaque en plus par carte de la défausse, en %
     * @param maxPercent attaque en plus au plus, en %
     */
    public WhiteLadyEffect(int perCard, int maxPercent) { this(perCard, maxPercent, -1); }

    private WhiteLadyEffect(int perCard, int maxPercent, int counted) {
        this.perCard    = perCard;
        this.maxPercent = maxPercent;
        this.counted    = counted;
    }

    @Override
    public void onPlay(PlayContext context) {
        int percent = Math.min(maxPercent, perCard * context.getPlayer().getDiscardPile().size());
        context.queueForSpin(new WhiteLadyEffect(perCard, maxPercent, percent));
        context.addPopups(List.of(EffectPopup.scaled(Lang.f("DAME BLANCHE : ATTAQUE +{0} %", percent), EffectPopup.Style.ATTACK,
            percent, maxPercent)));
    }

    @Override
    public void apply(TurnContext context) {
        if (counted > 0) context.getCombatContext().multiplyAttack(1f + counted / 100f, "Dame blanche");
    }

    @Override
    public String getDescription() {
        return Lang.f("Attaque +{0} % par carte dans ta défausse, {1} % au plus.", perCard, maxPercent);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("DAME BLANCHE"), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }
}
