package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Pierre à aiguiser (donjon du Pique) : ajoute des Lames dès la pose, et un
 * bonus d'attaque au tirage. Les Lames restent tout le combat, pour l'As de Pique.
 */
public class BladesAttackEffect extends Effect {

    private final int blades;
    private final int attackBonus;

    /**
     * @param blades      Lames ajoutées quand la carte est jouée
     * @param attackBonus bonus d'attaque plat accordé pour ce tour
     */
    public BladesAttackEffect(int blades, int attackBonus) {
        this.blades      = blades;
        this.attackBonus = attackBonus;
    }

    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().addBlades(blades);
        super.onPlay(context);
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addAttackBonus(attackBonus);
    }

    @Override
    public String getDescription() { return Lang.f("+{0} Lames. Attaque +{1}", blades, attackBonus); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(Lang.f("LAMES +{0}", blades), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY),
            EffectPopup.scaled(Lang.f("ATTAQUE +{0}", attackBonus), EffectPopup.Style.ATTACK, attackBonus, PopupScale.CARD_ATTACK_BONUS));
    }

    @Override
    public EffectSound getSound() { return EffectSound.SPADE_IGNORE_DEFENSE; }
}
