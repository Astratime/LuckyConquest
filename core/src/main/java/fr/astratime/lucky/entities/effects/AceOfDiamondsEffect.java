package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;

/**
 * As de Carreau : chaque symbole de défense renvoie 100% des dégâts ennemis
 * si le joueur est sous 20% de vie, sinon 50%. Remplace le renvoi additif
 * des cartes de Carreau classiques (voir DefenseAction.resolve).
 */
public class AceOfDiamondsEffect extends Effect {

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().setConditionalReflect(true);
    }

    @Override
    public String getDescription() {
        return "Renvoi 100% des degats si vie < 20%, sinon 50%";
    }
}
