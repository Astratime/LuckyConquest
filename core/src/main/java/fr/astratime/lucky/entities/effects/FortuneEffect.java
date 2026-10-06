package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.ArrayList;
import java.util.List;

/**
 * Fortune (donjon du Trèfle) : multiplie les gains dès la pose (Fortune du
 * Roi), puis, au tirage, donne un bonus d'attaque égal à une part des gains
 * (Bourse garnie, Fortune du Roi).
 */
public class FortuneEffect extends Effect {

    private final int gainFactor;
    private final int attackPercent;

    /**
     * @param gainFactor    multiplicateur des gains à la pose (1 : aucun)
     * @param attackPercent part des gains ajoutée à l'attaque au tirage, en %
     */
    public FortuneEffect(int gainFactor, int attackPercent) {
        this.gainFactor    = gainFactor;
        this.attackPercent = attackPercent;
    }

    @Override
    public void onPlay(PlayContext context) {
        if (gainFactor > 1) context.multiplyGains(gainFactor);
        super.onPlay(context);
    }

    @Override
    public void apply(TurnContext context) {
        CombatContext combat = context.getCombatContext();
        combat.addAttackBonus(Math.round(combat.getPlayer().getGains() * attackPercent / 100f));
    }

    @Override
    public String getDescription() {
        String attack = Lang.f("Attaque +{0}% des gains", attackPercent);
        return gainFactor > 1 ? Lang.f("Gains x{0}. {1}", gainFactor, attack) : attack;
    }

    @Override
    public List<EffectPopup> getPopups() {
        List<EffectPopup> popups = new ArrayList<>();
        if (gainFactor > 1) popups.add(new EffectPopup(Lang.f("GAINS x{0}", gainFactor), EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY));
        popups.add(new EffectPopup(Lang.f("ATTAQUE +{0}% DES GAINS", attackPercent), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
        return popups;
    }

    @Override
    public EffectSound getSound() { return EffectSound.GAINS_MULTIPLIER; }
}
