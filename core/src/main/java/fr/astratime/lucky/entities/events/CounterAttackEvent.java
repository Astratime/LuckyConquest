package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Contre-attaque de l'As de Carreau : le Coffre vidé d'un coup sur l'ennemi. */
public class CounterAttackEvent extends EnemyDamagedEvent {

    public CounterAttackEvent(int damage) {
        super(damage, damage);
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
