package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Tournée générale (la Taverne) : la machine tourne deux fois, le meilleur
 * tirage reste (celui qui aligne le plus de symboles identiques).
 */
public class BestOfTwoEffect extends Effect {

    @Override
    public void apply(TurnContext context) { context.getSpinContext().addBestOfTwo(); }

    @Override
    public String getDescription() { return Lang.t("La machine tourne deux fois. Tu gardes le meilleur tirage."); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("TOURNÉE GÉNÉRALE !"), EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.REROLL; }
}
