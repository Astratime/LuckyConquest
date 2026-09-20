package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.ShieldGainedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Accorde du bouclier au joueur, consommé lors de la riposte de l'ennemi
 * (voir Player.takeDamage, appelé par CombatResolver) pour absorber une
 * partie des dégâts subis.
 * Lit dans le CombatContext :
 *  - defenseBonus       : bonus plat de bouclier (cartes jouées)
 *  - reflectPercentBonus : reflect additif accordé par symbole (cartes Carreau),
 *                          consommé lors de la riposte ennemie par CombatResolver
 *  - conditionalReflect  : si vrai (As de Carreau), reflect 100% sous 20% de vie, sinon 50%
 */
public class DefenseAction extends Action {

    private final int baseShield;

    /** @param baseShield bouclier de base accordé avant bonus. */
    public DefenseAction(int baseShield) { this.baseShield = baseShield; }

    /**
     * Accorde le bouclier (base + bonus) au joueur, puis fixe le pourcentage
     * de renvoi de dégâts selon le contexte (conditionnel ou additif).
     */
    @Override
    public List<Event> resolve(CombatContext context) {
        List<Event> events = new ArrayList<>();
        Player player = context.getPlayer();

        int shield = baseShield + context.getDefenseBonus();
        player.addShield(shield);
        events.add(new ShieldGainedEvent(shield));

        if (context.isConditionalReflect()) {
            int percent = player.getHpRatio() < 0.2f ? 100 : 50;
            player.setReflectPercent(percent);
        } else if (context.getReflectPercentBonus() > 0) {
            player.setReflectPercent(context.getReflectPercentBonus());
        }

        return events;
    }

    @Override
    public String getDescription() { return baseShield + " bouclier de base"; }
}
