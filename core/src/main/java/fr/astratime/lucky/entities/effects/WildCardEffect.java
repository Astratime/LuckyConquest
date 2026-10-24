package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Joker caméléon (archétype Joker) : pour les combinaisons, la carte prend la couleur et le rang
 * qui rapportent le plus (voir {@link fr.astratime.lucky.entities.Combo#formed}).
 */
public class WildCardEffect extends Effect {


    @Override
    public void apply(TurnContext context) {
        // rien au tirage : la carte compte dans les combinaisons (voir Combo#formed)
    }

    @Override
    public String getDescription() { return Lang.t("Pour les combinaisons, cette carte prend la couleur et le rang qui rapportent le plus."); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("JOKER CAMÉLÉON"), EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.BOOST_SYMBOL; }

    @Override
    public boolean canBeDoubled() { return false; }

}
