package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.BigNumber;
import fr.astratime.lucky.entities.SpinEconomy;
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
 * Ce bonus d'attaque est limité à {@code attackCapSpins} fois le coût du tirage
 * de force du lieu (Astra : 30, soit 3 000 à la Prairie et 60 000 au Casino), et les gains y sont ramenés à ce
 * coût : l'étirement des coûts de tirage (2026-10-29) ne change pas la force qu'ils donnent.
 * Bourse garnie : au tirage, une part de tous les gains s'ajoute à l'attaque.
 */
public class FortuneEffect extends Effect {

    private final int gainPercent;
    private final int attackPercent;
    private final int attackCapSpins;

    /** Comme {@link #FortuneEffect(int, int, int)}, sans limite d'attaque. */
    public FortuneEffect(int gainPercent, int attackPercent) { this(gainPercent, attackPercent, 0); }

    /**
     * @param gainPercent   part des gains actuels ajoutée à la pose, en % (0 : aucune)
     * @param attackPercent part ajoutée à l'attaque au tirage, en % : des gains gagnés à la
     *                      pose si {@code gainPercent > 0}, sinon de tous les gains
     * @param attackCapSpins bonus d'attaque au plus égal à autant de fois le coût du tirage (0 : aucune limite)
     */
    public FortuneEffect(int gainPercent, int attackPercent, int attackCapSpins) {
        this.gainPercent    = gainPercent;
        this.attackPercent  = attackPercent;
        this.attackCapSpins = attackCapSpins;
    }

    /**
     * @return le bonus d'attaque tiré de {@code gains} là où un tirage coûte {@code spinCost} : les gains sont
     *         ramenés au coût de tirage de force du lieu ({@link SpinEconomy#powerSpinCost}), puis la limite
     *         est {@code attackCapSpins} fois ce coût
     */
    int attackFrom(double gains, int spinCost) {
        int power = SpinEconomy.powerSpinCost(spinCost);
        int attack = BigNumber.toInt(BigNumber.percent(gains * power / spinCost, attackPercent));
        if (attackCapSpins <= 0) return attack;
        return (int) Math.min(attack, (long) attackCapSpins * power);
    }

    @Override
    public void onPlay(PlayContext context) {
        if (gainPercent > 0) {
            double added = context.addGainsPercent(gainPercent);
            int attack = attackFrom(added, context.getSpinCost());
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
        combat.addAttackBonus(attackFrom(combat.getPlayer().getGains(), combat.getSpinCost()), "Fortune");
    }

    @Override
    public String getDescription() {
        String cap = attackCapSpins > 0 ? Lang.f(" (au plus {0} fois le prix du tirage)", attackCapSpins) : "";
        return (gainPercent > 0
            ? Lang.f("Gains +{0} % de tes gains actuels. Attaque +{1} % des gains gagnés", gainPercent, attackPercent)
            : Lang.f("Attaque +{0}% des gains", attackPercent)) + cap;
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
