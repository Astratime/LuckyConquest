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
 * Ce bonus d'attaque est limité à {@code attackCapSpins} fois le coût du tirage
 * du lieu (Astra : 30, soit 3 000 à la Prairie et 150 000 au Casino).
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

    /** @return le bonus d'attaque {@code attack}, ramené à la limite du lieu dont le tirage coûte {@code spinCost}. */
    int capAttack(int attack, int spinCost) {
        if (attackCapSpins <= 0) return attack;
        return (int) Math.min(attack, (long) attackCapSpins * spinCost);
    }

    @Override
    public void onPlay(PlayContext context) {
        if (gainPercent > 0) {
            double added = context.addGainsPercent(gainPercent);
            int attack = capAttack(BigNumber.toInt(BigNumber.percent(added, attackPercent)), context.getSpinCost());
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
        combat.addAttackBonus(capAttack(BigNumber.toInt(BigNumber.percent(combat.getPlayer().getGains(), attackPercent)),
            combat.getSpinCost()), "Fortune");
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
