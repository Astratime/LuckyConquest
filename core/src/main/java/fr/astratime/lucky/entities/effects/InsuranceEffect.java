package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Assurance : ce tour, l'ennemi ne retire pas plus d'une part des PV max du joueur. */
public class InsuranceEffect extends Effect {

    private final int percent;

    /** @param percent PV max que l'ennemi peut retirer au plus ce tour, en % */
    public InsuranceEffect(int percent) { this.percent = percent; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().getPlayer().insure(percent);
    }

    @Override
    public String getDescription() {
        return "Ce tour, l'ennemi ne t'enlève pas plus de " + percent + " % de tes PV.";
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("ASSURÉ : PERTE MAX " + percent + " %", EffectPopup.Style.DEFENSE,
            PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.INSURANCE; }
}
