package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
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
    public String getDescription() { return Lang.f("Attaque +{0} %, puis encore +{1} % à chaque tour. Jusqu'à la fin du combat.",
        percent, percent); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("TREMPE : +{0} % PAR TOUR", percent), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }
}
