package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Prime de chasse (échoppe) : jouée, elle consomme {@code costPercent} % des
 * gains ; si l'ennemi tombe pendant le tirage de ce tour, les gains gagnés ce
 * tour sont doublés (voir TurnEngine#bounty).
 */
public class BountyEffect extends Effect {

    private final int costPercent;

    /** @param costPercent part des gains (en %) consommée quand la carte est jouée */
    public BountyEffect(int costPercent) { this.costPercent = costPercent; }

    @Override
    public void onPlay(PlayContext context) {
        double paid = context.getPlayer().consumeGainsPercent(costPercent / 100f);
        context.addPopups(getPopups());
        context.addPopups(List.of(new EffectPopup(Lang.f("GAINS -{0}", Lang.big(paid)), EffectPopup.Style.DAMAGE,
            PopupScale.SECONDARY_INTENSITY)));
        context.queueForSpin(this); // la prime attend le tirage
    }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().addBounty(); }

    @Override
    public String getDescription() {
        return Lang.f("Consomme {0}% de tes gains. Si l'ennemi tombe ce tour, les gains du tour sont doublés",
            costPercent);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("PRIME DE CHASSE"), EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ALL_IN; }
}
