package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.GameState;

/** Permet de piocher des cartes supplémentaires ce tour. */
public class ExtraDrawEffect extends Effect {

    private final int extraCards;

    public ExtraDrawEffect(int extraCards) {
        this.extraCards = extraCards;
    }

    @Override
    public void apply(GameState state) {
        state.getTurnContext().addDrawCount(extraCards);
    }

    @Override
    public String getDescription() {
        return "+" + extraCards + " carte(s) a piocher";
    }
}
