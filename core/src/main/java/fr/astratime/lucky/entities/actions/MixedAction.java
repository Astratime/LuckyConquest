package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.events.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Étoile : plusieurs actions d'un coup (attaque, bouclier, gains), chacune
 * avec ses bonus (cartes du tour et rang du joueur).
 */
public class MixedAction extends Action {

    private final List<Action> actions;

    public MixedAction(Action... actions) { this.actions = List.of(actions); }

    @Override
    public List<Event> resolve(CombatContext context) {
        List<Event> events = new ArrayList<>();
        for (Action action : actions) events.addAll(action.resolve(context));
        return events;
    }

    /** @return les actions de ce rouleau, dans l'ordre où elles sont résolues. */
    public List<Action> getActions() { return actions; }

    @Override
    public String getDescription() {
        return actions.stream().map(Action::getDescription).collect(Collectors.joining(", "));
    }
}
