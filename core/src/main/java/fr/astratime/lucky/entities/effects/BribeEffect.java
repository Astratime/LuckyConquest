package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Pot-de-vin : la défense de l'ennemi tombe à 0 ; elle se reforme à son tour. */
public class BribeEffect extends Effect {

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().getEnemy().bribe();
    }

    @Override
    public String getDescription() { return "La défense de l'ennemi tombe à 0. Elle se reforme à son tour."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("POT-DE-VIN : DÉFENSE 0", EffectPopup.Style.ATTACK, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.BRIBE; }
}
