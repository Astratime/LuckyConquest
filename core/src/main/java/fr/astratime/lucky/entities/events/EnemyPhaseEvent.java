package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Événement émis quand l'ennemi change de phase : ses rouleaux changent de
 * symboles (Éclat Originel), il perd un éclat (Prétendant), une partie (la
 * Maison) ou vole un rouleau (Machine Originelle).
 */
public class EnemyPhaseEvent extends Event {
    /** Sa nouvelle phase. */
    public final int phase;

    /** Texte qui annonce la phase. */
    public final String text;

    public EnemyPhaseEvent(int phase) { this(phase, "PHASE " + phase + " !"); }

    public EnemyPhaseEvent(int phase, String text) {
        this.phase = phase;
        this.text  = text;
    }

    @Override
    public String describe() { return "Ennemi : phase " + phase; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(text, EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY * 1.4f));
    }
}
