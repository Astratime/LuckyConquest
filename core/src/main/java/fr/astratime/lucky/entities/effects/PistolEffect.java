package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Pistolet de la Roulette russe : après le tirage, multiplie les dégâts du meilleur symbole d'attaque. */
public class PistolEffect extends Effect {

    private final int multiplier;

    /** @param multiplier multiplicateur des dégâts du symbole visé. */
    public PistolEffect(int multiplier) { this.multiplier = multiplier; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addPistolShot(multiplier);
    }

    @Override
    public String getDescription() { return "Pistolet : dégâts d'un symbole x" + multiplier; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup("PISTOLET CHARGÉ !", EffectPopup.Style.ATTACK, PopupScale.MAX_INTENSITY),
            new EffectPopup("DÉGÂTS x" + multiplier, EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }
}
