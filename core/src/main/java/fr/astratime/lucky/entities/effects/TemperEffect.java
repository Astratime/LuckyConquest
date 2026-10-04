package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Trempe (la Forge) : l'attaque gagne une part tout de suite, puis autant à
 * chaque tour, jusqu'à la fin du combat.
 */
public class TemperEffect extends Effect {

    private final int percent;

    /** @param percent attaque gagnée à chaque tour, en % */
    public TemperEffect(int percent) { this.percent = percent; }

    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().addTemper(percent);
        context.addPopups(getPopups());
    }

    /** Rien de plus au tirage. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return "Attaque +" + percent + " %, puis encore +" + percent + " % à chaque tour. Jusqu'à la fin du combat."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("TREMPE : +" + percent + " % PAR TOUR", EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }
}
