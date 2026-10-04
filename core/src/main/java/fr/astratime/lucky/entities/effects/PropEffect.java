package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Étai (le Puits) : la défense du tirage augmente, et le Forage ne perce
 * plus le bouclier pendant quelques tours.
 */
public class PropEffect extends Effect {

    private final int turns;
    private final int defensePercent;

    /**
     * @param turns          tours sans Forage qui perce, celui-ci compris
     * @param defensePercent défense ajoutée au tirage, en %
     */
    public PropEffect(int turns, int defensePercent) {
        this.turns          = turns;
        this.defensePercent = defensePercent;
    }

    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().addProp(turns);
        context.queueForSpin(this);
        context.addPopups(getPopups());
    }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().multiplyDefense(1f + defensePercent / 100f); }

    @Override
    public String getDescription() { return "Défense +" + defensePercent + " %. Le Forage ne perce plus ton bouclier pendant " + turns + " tours."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("ÉTAI : DÉFENSE +" + defensePercent + " %", EffectPopup.Style.DEFENSE, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.DEFENSE; }
}
