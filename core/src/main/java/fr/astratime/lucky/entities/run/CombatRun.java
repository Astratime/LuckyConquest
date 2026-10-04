package fr.astratime.lucky.entities.run;

import fr.astratime.lucky.entities.enemy.EnemyKind;

import java.util.List;

/**
 * Une suite de combats enchaînés par l'écran de jeu : un chapitre de la Tour
 * des épreuves ({@link fr.astratime.lucky.entities.tower.TowerRun}) ou un
 * donjon de l'Exploration ({@link fr.astratime.lucky.entities.exploration.DungeonRun}).
 * Le joueur garde ses PV, ses gains et ses cartes d'un combat à l'autre ; une
 * défaite fait tout recommencer au premier combat.
 */
public interface CombatRun {

    /** @return l'ennemi du combat en cours. */
    EnemyKind getEnemy();

    /** @return le combat en cours, de 0 (le premier) à {@link #getStageCount()} - 1 (le boss). */
    int getStage();

    /** @return le nombre de combats. */
    int getStageCount();

    /** @return {@code true} pendant le dernier combat (le boss). */
    default boolean isBossStage() { return getStage() == getStageCount() - 1; }

    /**
     * Combat en cours gagné : prépare le suivant.
     *
     * @return ce qui vient ensuite
     */
    Next win();

    /** @return les adversaires cachés sous les cartes du choix, dans l'ordre des cartes (vide sans choix en cours). */
    default List<EnemyKind> getChoices() { return List.of(); }

    /**
     * Le joueur retourne la carte {@code index} : son ennemi sera l'adversaire du combat.
     *
     * @return l'ennemi choisi
     */
    default EnemyKind choose(int index) { throw new IllegalStateException("Aucun choix d'adversaire en cours"); }

    /** Défaite : tout recommence au premier combat. */
    void restart();

    /** @return le nom de la suite de combats, en haut de l'écran de jeu (ex : "Chapitre 1"). */
    String getLabel();

    /** @return la phrase de fin, affichée après le dernier combat gagné, ou {@code null}. */
    default String getEnding() { return null; }

    /** Ce qui suit un combat gagné. */
    enum Next { CHOOSE_ENEMY, BOSS, CLEARED }
}
