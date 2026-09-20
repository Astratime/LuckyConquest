package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.context.SpinContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.effects.Effect;

import java.util.List;

/**
 * Phase 1 : applique les effets des cartes jouées par le joueur
 * et construit le TurnContext (SpinContext + CombatContext) qui sera
 * utilisé pour le spin et la résolution du combat.
 */
public class PreparationResolver {

    /**
     * Construit un TurnContext neuf pour {@code player}/{@code enemy}, puis applique
     * chaque effet en attente dessus (dans l'ordre où les cartes ont été jouées).
     *
     * @param pendingEffects effets accumulés depuis le début du tour
     * @param player         joueur du combat en cours
     * @param enemy          ennemi du combat en cours
     * @param baseDrawCount  nombre de cartes piochées par défaut, avant bonus des effets de ce tour
     * @return le TurnContext résultant, prêt pour le spin et le combat
     */
    public TurnContext resolve(List<Effect> pendingEffects, Player player, Enemy enemy, int baseDrawCount) {
        SpinContext   spinContext   = new SpinContext();
        CombatContext combatContext = new CombatContext(player, enemy);
        TurnContext   turnContext   = new TurnContext(baseDrawCount, spinContext, combatContext);

        pendingEffects.forEach(effect -> effect.apply(turnContext));

        return turnContext;
    }
}
