package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Miroir taillé (donjon du Carreau) : renvoi garanti des attaques ennemies, sans
 * avoir besoin de tirer un symbole de défense. S'additionne au renvoi des cartes Carreau.
 */
public class GuaranteedReflectEffect extends Effect {

    private final int percent;

    /** @param percent part des attaques ennemies renvoyée, en % */
    public GuaranteedReflectEffect(int percent) { this.percent = percent; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addGuaranteedReflect(percent, percent);
    }

    @Override
    public String getDescription() {
        return Lang.f("Renvoie {0}% des attaques ennemies. Pas besoin de symbole de défense", percent);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(EffectPopup.scaled(Lang.f("RENVOI {0}%", percent), EffectPopup.Style.REFLECT, percent, PopupScale.CARD_REFLECT_PERCENT));
    }

    @Override
    public EffectSound getSound() { return EffectSound.DIAMOND_REFLECT; }
}
