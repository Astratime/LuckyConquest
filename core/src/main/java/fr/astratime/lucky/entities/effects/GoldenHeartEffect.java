package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Cœur d'or (le Gouffre) : ce tour, les gains gagnés frappent aussi l'ennemi
 * (voir {@link fr.astratime.lucky.controllers.CombatResolver}).
 */
public class GoldenHeartEffect extends Effect {

    @Override
    public void apply(TurnContext context) { context.getCombatContext().addGoldenHeart(); }

    @Override
    public String getDescription() { return "Ce tour, tes gains comptent aussi comme attaque."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("CŒUR D'OR : GAINS = ATTAQUE", EffectPopup.Style.GAINS, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public boolean canBeDoubled() { return false; }

    @Override
    public EffectSound getSound() { return EffectSound.GAIN; }
}
