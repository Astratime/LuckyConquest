package fr.astratime.lucky.entities.choices;

import java.util.List;

/**
 * Roulette russe : le joueur retourne une carte parmi plusieurs, faces cachées
 * (voir GameController#pickRouletteCard). Une seule est le Joker maudit.
 *
 * @param pistolMultiplier multiplicateur des dégâts du pistolet avec une bonne carte
 * @param cursedMultiplier multiplicateur des dégâts du pistolet avec le Joker maudit
 * @param penaltyPercent   pourcentage des gains perdus s'il tire le Joker maudit
 * @param cursed           pour chaque carte (de gauche à droite), {@code true} si c'est le Joker maudit
 */
public record RouletteChoice(int pistolMultiplier, int cursedMultiplier, int penaltyPercent, List<Boolean> cursed) implements CardChoice {

    public RouletteChoice {
        cursed = List.copyOf(cursed);
    }
}
