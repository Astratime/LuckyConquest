package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Événement émis quand l'ennemi passe à sa deuxième phase : ses rouleaux changent de symboles. */
public class EnemyPhaseEvent extends Event {
    /** Sa nouvelle phase. */
    public final int phase;

    public EnemyPhaseEvent(int phase) { this.phase = phase; }

    @Override
    public String describe() { return "Ennemi : phase " + phase; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("PHASE " + phase + " !", EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY * 1.4f));
    }
}
