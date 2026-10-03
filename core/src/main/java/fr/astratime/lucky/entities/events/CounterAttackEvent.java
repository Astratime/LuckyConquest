package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Contre-attaque de l'As de Carreau : le Coffre vidé d'un coup sur l'ennemi, sans tenir compte de sa défense. */
public class CounterAttackEvent extends EnemyDamagedEvent {

    /**
     * @param damage      dégâts infligés à l'ennemi
     * @param defenseLeft défense de l'ennemi, ignorée par la contre-attaque
     */
    public CounterAttackEvent(int damage, int defenseLeft) {
        super(damage, damage, 0, defenseLeft, true);
    }

    @Override
    public String describe() { return "Contre-attaque : ennemi -" + damage + " PV"; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup("CONTRE-ATTAQUE !", EffectPopup.Style.DEFENSE, PopupScale.MAX_INTENSITY),
            EffectPopup.scaled("DÉGÂTS " + damage, EffectPopup.Style.ATTACK, damage, PopupScale.SPIN_DAMAGE));
    }
}
