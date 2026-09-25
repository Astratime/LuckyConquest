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
 * Lit dans le CombatContext le bonus plat de bouclier (cartes jouées), et y
 * signale qu'un symbole de défense est sorti : c'est ce qui active le renvoi
 * de dégâts des cartes Carreau (calculé lors de la riposte par CombatResolver).
 */
public class DefenseAction extends Action {

    private final int baseShield;

    /** @param baseShield bouclier de base accordé avant bonus. */
    public DefenseAction(int baseShield) { this.baseShield = baseShield; }

    /** Accorde le bouclier (base + bonus) au joueur et active le renvoi des cartes Carreau. */
    @Override
    public List<Event> resolve(CombatContext context) {
        List<Event> events = new ArrayList<>();
        Player player = context.getPlayer();

        int shield = Math.round((baseShield + context.getDefenseBonus()) * context.getSymbolPower()
            * context.getDefenseFactor());
        player.addShield(shield);
        events.add(new ShieldGainedEvent(shield));

        context.markDefenseSymbolDrawn();

        return events;
    }

    @Override
    public String getDescription() { return baseShield + " bouclier de base"; }
}
