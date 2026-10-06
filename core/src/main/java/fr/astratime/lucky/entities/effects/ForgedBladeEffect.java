package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Lame forgée (la Forge) : un coup d'épée de plus après le tirage, à une
 * part de la plus grosse attaque du tour (voir {@link fr.astratime.lucky.controllers.CombatResolver}).
 */
public class ForgedBladeEffect extends Effect {

    /** Force du coup, en % de la plus grosse attaque du tour. */
    public static final int PERCENT = 50;

    @Override
    public void apply(TurnContext context) { context.getCombatContext().addForgedBlade(); }

    @Override
    public String getDescription() { return Lang.f("Un coup d'épée en plus. Il frappe à {0} % de ta plus grosse attaque du tour.",
        PERCENT); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("LAME FORGÉE"), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }
}
