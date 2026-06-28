package fr.astratime.lucky.entities;

/**
 * Résultat d'un tour après résolution par CombatResolver.
 * Contient uniquement ce dont GameScreen a besoin pour mettre à jour l'affichage.
 * Aucune logique ici : c'est un conteneur de données en lecture seule.
 */
public class TurnResult {

    public final int     damageDealt;
    public final int     scoreGained;
    public final boolean isJackpot;
    public final boolean hasPair;

    public TurnResult(int damageDealt, int scoreGained, boolean isJackpot, boolean hasPair) {
        this.damageDealt = damageDealt;
        this.scoreGained = scoreGained;
        this.isJackpot   = isJackpot;
        this.hasPair     = hasPair;
    }
}
