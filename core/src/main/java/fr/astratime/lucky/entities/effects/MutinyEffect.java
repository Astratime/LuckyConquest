package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Mutinerie (le Galion) : au prochain tour de l'ennemi, ses cartes se
 * retournent contre lui : il ne les joue pas, et chacune lui retire
 * {@link #PERCENT_PER_CARD} % de ses PV max (sans le tuer).
 */
public class MutinyEffect extends Effect {

    /** PV max de l'ennemi retirés par chacune de ses cartes retournées, en %. */
    public static final int PERCENT_PER_CARD = 2;

    @Override
    public void apply(TurnContext context) { context.getCombatContext().getEnemy().addMutiny(); }

    @Override
    public String getDescription() { return "Au prochain tour de l'ennemi, ses cartes se retournent contre lui. Il ne les joue pas. "
        + "Chacune lui retire " + PERCENT_PER_CARD + " % de ses PV max."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("MUTINERIE !", EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    @Override
    public boolean canBeDoubled() { return false; }

    @Override
    public EffectSound getSound() { return EffectSound.RUSSIAN_ROULETTE; }
}
