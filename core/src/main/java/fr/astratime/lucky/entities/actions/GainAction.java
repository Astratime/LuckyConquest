package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;

import java.util.List;

/**
 * Accorde des gains au joueur, multipliés par le gainMultiplier (cartes Trèfle),
 * le facteur de gains du tirage (combos, Porte-bonheur) et la puissance des
 * symboles (Bingo).
 */
public class GainAction extends Action {

    private final int baseGain;

    /** @param baseGain gain de base avant application du multiplicateur. */
    public GainAction(int baseGain) { this.baseGain = baseGain; }

    /** Crédite au joueur {@code baseGain * gainMultiplier * gainFactor * symbolPower} (arrondi). */
    @Override
    public List<Event> resolve(CombatContext context) {
        int gain = Math.round(baseGain * context.getGainMultiplier() * context.getGainFactor()
            * context.getSymbolPower());
        context.getPlayer().addGains(gain);
        return List.of(new GainsEarnedEvent(gain));
    }

    @Override
    public String getDescription() { return baseGain + " gains de base"; }
}
