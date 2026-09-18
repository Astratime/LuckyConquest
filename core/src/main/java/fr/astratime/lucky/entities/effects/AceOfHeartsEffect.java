package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolRegistry;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.events.SymbolBoostedEvent;

import java.util.List;
import java.util.Random;

/** As de Coeur : boost de probabilité sur un symbole d'attaque choisi au hasard. */
public class AceOfHeartsEffect extends Effect {

    /** Boost de poids appliqué au symbole d'attaque choisi au hasard. */
    private static final int    BOOST_AMOUNT = 60;
    private static final Random RANDOM       = new Random();

    /** Choisit un symbole d'attaque au hasard et augmente son poids de tirage pour ce tour. */
    @Override
    public void apply(TurnContext context) {
        List<Symbol> attackSymbols = SymbolRegistry.getAttackSymbols();
        if (attackSymbols.isEmpty()) return;

        Symbol target = attackSymbols.get(RANDOM.nextInt(attackSymbols.size()));
        context.getSpinContext().addWeightBoost(target, BOOST_AMOUNT);
        context.addEvent(new SymbolBoostedEvent(target, BOOST_AMOUNT));
    }

    @Override
    public String getDescription() { return "Boost aleatoire d'un symbole d'attaque"; }
}
