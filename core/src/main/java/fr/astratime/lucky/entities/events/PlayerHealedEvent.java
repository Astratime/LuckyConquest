package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.entities.effects.EffectPopup;

import java.util.List;

/** Événement émis quand le joueur est soigné (drain de vie d'une attaque, cartes Coeur). */
public class PlayerHealedEvent extends Event {
    /** Points de vie rendus au joueur. */
    public final int amount;

    /** @param amount points de vie rendus au joueur. */
    public PlayerHealedEvent(int amount) { this.amount = amount; }

    @Override
    public String describe() { return "Joueur soigne de " + amount + " PV (drain)"; }

    /** Vie rendue par le drain ; 30 donne la taille de texte maximale. */
    @Override
    public List<EffectPopup> getPopups() {
        return List.of(EffectPopup.scaled("VIE +" + amount, EffectPopup.Style.DRAIN, amount, 30f));
    }
}
