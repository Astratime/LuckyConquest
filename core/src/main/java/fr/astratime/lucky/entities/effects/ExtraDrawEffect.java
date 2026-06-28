package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.TurnContext;

/** Augmente le nombre de cartes piochées au prochain tour. */
public class ExtraDrawEffect extends Effect {

    private final int extraCards;

    public ExtraDrawEffect(int extraCards) { this.extraCards = extraCards; }

    @Override
    public void apply(TurnContext context) {
        context.addDrawCount(extraCards);
    }

    @Override
    public String getDescription() { return "+" + extraCards + " carte(s) au prochain tour"; }
}
