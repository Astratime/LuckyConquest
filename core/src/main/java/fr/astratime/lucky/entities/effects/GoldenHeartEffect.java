package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Cœur d'or (le Gouffre) : ce tour, les bonus de gains (rang, multiplicateurs des
 * cartes) deviennent aussi des bonus d'attaque (voir {@link fr.astratime.lucky.controllers.CombatResolver}).
 */
public class GoldenHeartEffect extends Effect {

    @Override
    public void apply(TurnContext context) { context.getCombatContext().addGoldenHeart(); }

    @Override
    public String getDescription() { return Lang.t("Ce tour, chaque symbole d'attaque gagne en attaque le bonus de gains de ton rang plus 100 fois les multiplicateurs de gains de tes cartes (Trèfle...)."); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("COEUR D'OR : GAINS = ATTAQUE"), EffectPopup.Style.GAINS, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public boolean canBeDoubled() { return false; }

    @Override
    public EffectSound getSound() { return EffectSound.GAIN; }
}
