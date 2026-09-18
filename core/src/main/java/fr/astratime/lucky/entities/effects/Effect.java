package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;

/**
 * Effet produit par une carte jouée en phase 1.
 * S'applique sur le TurnContext uniquement — pas d'accès au GameState complet.
 * Les effets modifient soit le SpinContext (boosts machine), soit le CombatContext
 * (bonus de combat), soit le drawCount du tour suivant.
 */
public abstract class Effect {

    /**
     * Applique l'effet de la carte au contexte du tour en cours.
     *
     * @param context contexte du tour (spin + combat) à modifier
     */
    public abstract void apply(TurnContext context);

    /** @return une courte description de l'effet, affichée en infobulle sur la carte. */
    public abstract String getDescription();
}
