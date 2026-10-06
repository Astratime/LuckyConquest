package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Pièce truquée (la Salle des machines) : le prochain tirage de l'ennemi ne
 * fait pas de Jackpot.
 */
public class LoadedCoinEffect extends Effect {

    @Override
    public void apply(TurnContext context) { context.getCombatContext().getEnemy().addLoadedCoin(); }

    @Override
    public String getDescription() { return Lang.t("Le prochain tirage de l'ennemi ne peut pas faire 3 symboles pareils."); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("PIÈCE TRUQUÉE"), EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.RIGGED_REEL; }
}
