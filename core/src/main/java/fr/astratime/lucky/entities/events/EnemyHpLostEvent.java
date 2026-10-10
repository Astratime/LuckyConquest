package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;

/**
 * L'ennemi perd des PV par son propre symbole (Faillite, Duel gagné) : un texte,
 * et sa barre de vie descend quand il apparaît.
 */
public class EnemyHpLostEvent extends StatusEvent {
    /** PV réellement perdus. */
    public final int lost;

    public EnemyHpLostEvent(String text, EffectPopup.Style style, int lost) {
        super(text, style);
        this.lost = lost;
    }
}
