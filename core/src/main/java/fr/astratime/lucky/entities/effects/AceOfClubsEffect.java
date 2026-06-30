package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolRegistry;
import fr.astratime.lucky.entities.TurnContext;

import java.util.List;
import java.util.Random;

/**
 * As de Trefle : consomme 30% des gains actuels du joueur pour offrir
 * un gros boost de probabilité ET d'attaque sur un symbole d'attaque au hasard.
 * Le coût est payé immédiatement (à la pose de la carte) ; c'est un choix
 * tactique fort, donc le retour doit être proportionnellement plus grand
 * que les cartes de Trefle classiques.
 */
public class AceOfClubsEffect extends Effect {

    private static final float CONSUME_PERCENT    = 0.3f;
    private static final int   WEIGHT_BOOST_AMOUNT = 150;
    private static final int   ATTACK_BOOST_AMOUNT = 15;
    private static final Random RANDOM             = new Random();

    @Override
    public void apply(TurnContext context) {
        Player player = context.getCombatContext().getPlayer();
        player.consumeGainsPercent(CONSUME_PERCENT); // le montant consommé n'est pas utilisé ici

        List<Symbol> attackSymbols = SymbolRegistry.getAttackSymbols();
        if (attackSymbols.isEmpty()) return;

        Symbol target = attackSymbols.get(RANDOM.nextInt(attackSymbols.size()));
        context.getSpinContext().addWeightBoost(target, WEIGHT_BOOST_AMOUNT);
        context.getCombatContext().addAttackBonus(ATTACK_BOOST_AMOUNT);
    }

    @Override
    public String getDescription() {
        return "Consomme 30% des gains : gros boost d'un symbole d'attaque";
    }
}
