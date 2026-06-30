package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.ShieldGainedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Accorde du bouclier au joueur, à consommer lors du tour ennemi
 * (non implémenté pour l'instant — voir Player.shield).
 * Lit dans le CombatContext :
 *  - defenseBonus       : bonus plat de bouclier (cartes jouées)
 *  - reflectPercentBonus : reflect additif accordé par symbole (cartes Carreau)
 *  - conditionalReflect  : si vrai (As de Carreau), reflect 100% sous 20% de vie, sinon 50%
 */
public class DefenseAction extends Action {

    private final int baseShield;

    public DefenseAction(int baseShield) { this.baseShield = baseShield; }

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
