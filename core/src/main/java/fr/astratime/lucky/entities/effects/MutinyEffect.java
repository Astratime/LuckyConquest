package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Mutinerie (le Galion) : à son prochain tour, l'ennemi joue
 * {@link #CARDS_LESS} cartes de moins que d'habitude (jamais moins de zéro).
 */
public class MutinyEffect extends Effect {

    /** Cartes de moins jouées par l'ennemi à son prochain tour. */
    public static final int CARDS_LESS = 2;

    @Override
    public void apply(TurnContext context) { context.getCombatContext().getEnemy().addMutiny(); }

    @Override
    public String getDescription() { return "Au prochain tour de l'ennemi, l'équipage se révolte. Il joue " + CARDS_LESS + " cartes de moins."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("MUTINERIE !", EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    @Override
    public boolean canBeDoubled() { return false; }

    @Override
    public EffectSound getSound() { return EffectSound.RUSSIAN_ROULETTE; }
}
