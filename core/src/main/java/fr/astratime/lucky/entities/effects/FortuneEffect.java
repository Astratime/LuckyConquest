package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.BigNumber;
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
 * Fortune (donjon du Trèfle). Fortune du Roi : ajoute une part des gains dès la
 * pose, puis, au tirage, une part de ces gains gagnés s'ajoute à l'attaque.
 * Bourse garnie : au tirage, une part de tous les gains s'ajoute à l'attaque.
 */
public class FortuneEffect extends Effect {

    private final int gainPercent;
    private final int attackPercent;

    /**
     * @param gainPercent   part des gains actuels ajoutée à la pose, en % (0 : aucune)
     * @param attackPercent part ajoutée à l'attaque au tirage, en % : des gains gagnés à la
     *                      pose si {@code gainPercent > 0}, sinon de tous les gains
     */
    public FortuneEffect(int gainPercent, int attackPercent) {
        this.gainPercent   = gainPercent;
        this.attackPercent = attackPercent;
    }

    @Override
    public void onPlay(PlayContext context) {
        if (gainPercent > 0) {
            double added = context.addGainsPercent(gainPercent);
            int attack = BigNumber.toInt(BigNumber.percent(added, attackPercent));
            context.queueForSpin(new AttackEffect(attack));
            context.addPopups(List.of(
                EffectPopup.scaled(Lang.f("GAINS +{0}", Lang.big(added)), EffectPopup.Style.GAINS, added, PopupScale.SPIN_GAINS),
                new EffectPopup(Lang.f("ATTAQUE +{0}", Lang.big(attack)), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY)));
            return;
        }
        super.onPlay(context);
    }

    @Override
    public void apply(TurnContext context) {
        if (gainPercent > 0) return; // Fortune du Roi : son attaque est mise en attente à la pose
        CombatContext combat = context.getCombatContext();
        combat.addAttackBonus(BigNumber.toInt(BigNumber.percent(combat.getPlayer().getGains(), attackPercent)), "Fortune");
    }

    @Override
    public String getDescription() {
        return gainPercent > 0
            ? Lang.f("Gains +{0} % de tes gains actuels. Attaque +{1} % des gains gagnés", gainPercent, attackPercent)
            : Lang.f("Attaque +{0}% des gains", attackPercent);
    }

    @Override
    public List<EffectPopup> getPopups() {
        if (gainPercent > 0) return List.of(); // les valeurs dépendent des gains (voir onPlay)
        List<EffectPopup> popups = new ArrayList<>();
        popups.add(new EffectPopup(Lang.f("ATTAQUE +{0}% DES GAINS", attackPercent), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
        return popups;
    }

    @Override
    public EffectSound getSound() { return EffectSound.GAINS_MULTIPLIER; }
}
