package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Feu du phare (carte du Gardien du phare) : la prochaine main de l'ennemi est visible, et celle du joueur ne peut pas être cachée.
 */
public class LighthouseEffect extends Effect {
    private final int turns;

    /** @param turns tours où le phare éclaire */
    public LighthouseEffect(int turns) { this.turns = turns; }

    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().addLantern(turns);
        context.queueForSpin(this);
        context.addPopups(getPopups());
    }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().getEnemy().getMaluses().light(turns); }

    @Override
    public String getDescription() { return Lang.f("Pendant {0} tours, tu vois la prochaine main de l'ennemi et ta main reste visible.", turns); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("FEU DU PHARE"), EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.REROLL; }
}
