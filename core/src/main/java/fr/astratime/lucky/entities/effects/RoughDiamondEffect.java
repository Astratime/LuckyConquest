package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Diamant brut (donjon du Carreau, la carte rare) : le Coffre double dès la
 * pose, puis il est vidé sur l'ennemi en contre-attaque, multiplié, au tirage.
 */
public class RoughDiamondEffect extends Effect {

    private final int counter;

    /** @param counter multiplicateur du Coffre infligé en contre-attaque */
    public RoughDiamondEffect(int counter) { this.counter = counter; }

    @Override
    public void onPlay(PlayContext context) {
        LastingEffects lasting = context.getLastingEffects();
        lasting.addVault(lasting.getVault());
        super.onPlay(context);
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addCounterAttack(counter);
    }

    @Override
    public String getDescription() {
        return "Le Coffre double. Contre-attaque : vide le Coffre sur l'ennemi x" + counter;
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup("COFFRE x2", EffectPopup.Style.DEFENSE, PopupScale.MAX_INTENSITY),
            new EffectPopup("CONTRE-ATTAQUE x" + counter, EffectPopup.Style.REFLECT, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ACE_OF_DIAMONDS; }
}
