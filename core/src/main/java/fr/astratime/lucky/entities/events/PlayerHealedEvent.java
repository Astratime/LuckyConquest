package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.entities.effects.EffectPopup;
import fr.astratime.lucky.entities.effects.PopupScale;

import java.util.List;

/** Événement émis quand le joueur est soigné (drain de vie d'une attaque, cartes Coeur). */
public class PlayerHealedEvent extends Event {
    /** Points de vie réellement rendus au joueur (plafonnés à ses PV max). */
    public final int amount;

    /** @param amount points de vie rendus au joueur. */
    public PlayerHealedEvent(int amount) { this.amount = amount; }

    @Override
    public String describe() { return "Joueur soigne de " + amount + " PV (drain)"; }

    /** Vie rendue par le drain, taille maximale selon {@link PopupScale}. */
    @Override
    public List<EffectPopup> getPopups() {
        return List.of(EffectPopup.scaled("VIE +" + amount, EffectPopup.Style.DRAIN, amount, PopupScale.SPIN_HEAL));
    }
}
