package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Événement émis quand l'ennemi se soigne en volant la vie du joueur (Croc). */
public class EnemyDrainEvent extends EnemyHealedEvent {

    /** @param amount points de vie réellement rendus à l'ennemi */
    public EnemyDrainEvent(int amount) { super(amount); }

    @Override
    public String describe() { return "Ennemi draine " + amount + " PV"; }

    @Override
    public List<EffectPopup> getPopups() {
        if (amount <= 0) return List.of();
        return List.of(EffectPopup.scaled("DRAIN +" + amount, EffectPopup.Style.DRAIN, amount, PopupScale.ENEMY_HEAL));
    }
}
