package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Bouchons d'oreille (le Bar) : le Chant n'a plus d'effet pendant quelques pioches.
 */
public class EarplugsEffect extends Effect {

    private final int draws;

    /** @param draws pioches sans Chant */
    public EarplugsEffect(int draws) { this.draws = draws; }

    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().addEarplugs(draws);
        context.addPopups(getPopups());
    }

    /** Rien de plus au tirage. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return Lang.f("Le Chant n'a plus d'effet pendant {0} tours. Tu choisis tes cartes.",
        draws); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("BOUCHONS D'OREILLE"), EffectPopup.Style.DEFENSE, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.INSURANCE; }
}
