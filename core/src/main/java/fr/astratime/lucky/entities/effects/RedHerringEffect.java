package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Fausse piste (archétype Bluffeur) : au prochain tirage de l'ennemi, un de ses rouleaux ne tourne pas
 * (une chance sur deux contre un boss ou un roi).
 */
public class RedHerringEffect extends Effect {


    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().getEnemy().addRedHerring();
    }

    @Override
    public String getDescription() { return Lang.t("Au prochain tirage de l'ennemi, un de ses rouleaux ne tourne pas (une chance sur deux contre un boss ou un roi)."); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("FAUSSE PISTE"), EffectPopup.Style.DEFENSE, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.RIGGED_REEL; }

    @Override
    public boolean canBeDoubled() { return false; }

}
