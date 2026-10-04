package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Lanterne (le Phare) : la main ne peut plus être cachée (Aveuglement)
 * pendant quelques pioches, et la défense du tirage augmente.
 */
public class LanternEffect extends Effect {

    private final int draws;
    private final int defensePercent;

    /**
     * @param draws          pioches où la main reste visible
     * @param defensePercent défense ajoutée au tirage, en %
     */
    public LanternEffect(int draws, int defensePercent) {
        this.draws          = draws;
        this.defensePercent = defensePercent;
    }

    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().addLantern(draws);
        context.queueForSpin(this);
        context.addPopups(getPopups());
    }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().multiplyDefense(1f + defensePercent / 100f); }

    @Override
    public String getDescription() { return "Ta main reste visible pendant " + draws + " tours. Défense +" + defensePercent + " %."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("LANTERNE", EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY),
            new EffectPopup("DÉFENSE +" + defensePercent + " %", EffectPopup.Style.DEFENSE, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.INSURANCE; }
}
