package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.ArrayList;
import java.util.List;

/**
 * Événement émis au début du tour de l'ennemi quand ses Épines renvoient au
 * joueur une part des dégâts qu'il a infligés : son bouclier en absorbe
 * d'abord ce qu'il peut, comme pour une attaque.
 */
public class ThornsEvent extends PlayerDamagedEvent {

    /**
     * @param damage     dégâts effectivement infligés au joueur (après bouclier)
     * @param blocked    dégâts absorbés par son bouclier
     * @param shieldLeft bouclier restant après ce coup
     */
    public ThornsEvent(int damage, int blocked, int shieldLeft) {
        super(damage, blocked, shieldLeft);
    }

    @Override
    public String describe() { return "Epines : " + super.describe(); }

    /** « ÉPINES -X », puis ce que le bouclier a bloqué. */
    @Override
    public List<EffectPopup> getPopups() {
        List<EffectPopup> popups = new ArrayList<>();
        popups.add(EffectPopup.scaled(Lang.f("ÉPINES -{0}", Lang.big(damage)), EffectPopup.Style.DAMAGE, damage, PopupScale.SPIN_LIFE_LOST));
        if (blocked > 0) popups.add(EnemyDamagedEvent.blockedPopup(blocked, damage == 0));
        return popups;
    }
}
