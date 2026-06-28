package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.GameState;

/**
 * Inflige des dégâts bruts à l'ennemi.
 * Le bonus d'attaque (cartes jouées) est ajouté une seule fois
 * par CombatResolver, pas ici, pour éviter de l'appliquer plusieurs fois.
 */
public class AttackAction extends Action {

    private final int damagePerSymbol;

    public AttackAction(int damagePerSymbol) {
        this.damagePerSymbol = damagePerSymbol;
    }

    @Override
    public void apply(GameState state, int count) {
        state.getEnemy().takeDamage(count * damagePerSymbol);
    }

    @Override
    public String getDescription() {
        return damagePerSymbol + " degats par symbole";
    }
}
