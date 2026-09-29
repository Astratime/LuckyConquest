package fr.astratime.lucky.entities.enemy;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.events.Event;

import java.util.List;
import java.util.Map;

/**
 * Déroulé du tour de l'ennemi, pour l'affichage : les cartes piochées, les
 * trois qu'il a jouées, ce que ses Trèfles ont favorisé, les symboles tirés et
 * ce que chacun a fait, puis les suites de son attaque (renvoi de dégâts).
 *
 * @param drawn      cartes piochées (au plus {@link fr.astratime.lucky.entities.Enemy#HAND_SIZE})
 * @param played     cartes jouées parmi elles (au plus {@link fr.astratime.lucky.entities.Enemy#PLAYS_PER_TURN})
 * @param luck       chance ajoutée par les Trèfles, par symbole (en %)
 * @param symbols    symboles arrêtés sur ses trois rouleaux
 * @param outcomes   événements produits par chaque symbole, dans l'ordre des rouleaux
 * @param afterEvents événements qui suivent les symboles (ex : renvoi de dégâts)
 */
public record EnemyTurnResult(List<Card> drawn, List<Card> played, Map<EnemySymbol, Integer> luck,
                              EnemySymbol[] symbols, List<List<Event>> outcomes, List<Event> afterEvents) {

    /** @return tous les événements du tour de l'ennemi, dans l'ordre. */
    public List<Event> events() {
        return java.util.stream.Stream.concat(outcomes.stream().flatMap(List::stream), afterEvents.stream()).toList();
    }
}
