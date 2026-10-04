package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** L'ennemi se blesse lui-même au début de son tour (Mutinerie : ses cartes se retournent contre lui). */
public class EnemySelfHitEvent extends DamageReflectedEvent {

    /** Ce qui le blesse (ex : "MUTINERIE"). */
    public final String cause;

    /**
     * @param cause  ce qui le blesse, en majuscules
     * @param damage PV qu'il perd
     */
    public EnemySelfHitEvent(String cause, int damage) {
        super(damage);
        this.cause = cause;
    }

    @Override
    public String describe() { return cause + " : ennemi -" + damage + " PV"; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(EffectPopup.scaled(cause + " -" + damage, EffectPopup.Style.ATTACK, damage, PopupScale.SPIN_DAMAGE));
    }
}
