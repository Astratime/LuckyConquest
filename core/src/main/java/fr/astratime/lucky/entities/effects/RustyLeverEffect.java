package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Levier rouillé (la Salle des machines) : relance sans paire, et bonus
 * d'attaque si le tirage fait une paire (voir {@link fr.astratime.lucky.controllers.CombatResolver}).
 */
public class RustyLeverEffect extends Effect {

    /** Attaque ajoutée au tirage s'il fait une paire, en %. */
    public static final int ATTACK_PERCENT = 30;

    @Override
    public void apply(TurnContext context) {
        context.getSpinContext().addReroll();
        context.getCombatContext().addLever();
    }

    @Override
    public String getDescription() { return "Relance si le tirage n'a pas de paire. Avec une paire, attaque +" + ATTACK_PERCENT + " %."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("LEVIER ROUILLÉ", EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.REROLL; }
}
