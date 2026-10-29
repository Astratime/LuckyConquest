package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Tour de passe-passe (archétype Bluffeur) : le prochain effet spécial de l'ennemi est annulé
 * (une chance sur deux contre un boss ou un roi).
 */
public class SleightOfHandEffect extends Effect {


    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().getEnemy().getMaluses().addSleight();
    }

    @Override
    public String getDescription() { return Lang.t("Le prochain effet spécial de l'ennemi est annulé (une chance sur deux contre un boss ou un roi)."); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("TOUR DE PASSE-PASSE"), EffectPopup.Style.DEFENSE, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.INSURANCE; }

    @Override
    public boolean canBeDoubled() { return false; }

}
