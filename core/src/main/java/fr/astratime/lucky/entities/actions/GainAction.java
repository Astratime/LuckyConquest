package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.CombatContext;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;

import java.util.List;

/** Accorde des gains au joueur, multipliés par le gainMultiplier (cartes Trèfle). */
public class GainAction extends Action {

    private final int baseGain;

    public GainAction(int baseGain) { this.baseGain = baseGain; }

    @Override
    public List<Event> resolve(CombatContext context) {
        int gain = Math.round(baseGain * context.getGainMultiplier());
        context.getPlayer().addGains(gain);
        return List.of(new GainsEarnedEvent(gain));
    }

    @Override
    public String getDescription() { return baseGain + " gains de base"; }
}
