package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Rouleau retenu (archétype Relanceur) : le rouleau de gauche garde le symbole qu'il affiche depuis le
 * dernier tirage (le bouton HOLD des vraies machines).
 */
public class HeldReelEffect extends Effect {


    @Override
    public void apply(TurnContext context) {
        fr.astratime.lucky.entities.Symbol[] last = context.getCombatContext().getPlayer().getLastingEffects().getLastDrawn();
        if (last != null && last.length > 0 && last[0] != null) context.getSpinContext().forceReel(LEFT_REEL, last[0]);
    }

    @Override
    public String getDescription() { return Lang.t("Le rouleau de gauche garde le symbole qu'il affiche."); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("ROULEAU RETENU"), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.RIGGED_REEL; }

    @Override
    public boolean canBeDoubled() { return false; }

    /** Le rouleau retenu : celui de gauche. */
    static final int LEFT_REEL = 0;

}
