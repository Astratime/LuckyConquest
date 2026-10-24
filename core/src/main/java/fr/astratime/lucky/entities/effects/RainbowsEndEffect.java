package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Bout de l'arc-en-ciel (archétype Lutin) : si le tirage fait une paire d'un symbole d'attaque,
 * un Pot de Lutin rejoint la pioche du joueur.
 */
public class RainbowsEndEffect extends Effect {
    private final String cardId;

    /**
     * Bout de l'arc-en-ciel.
     * @param cardId carte glissée dans la pioche (le Pot de Lutin, ou sa version « + »)
     */
    public RainbowsEndEffect(String cardId) {
        this.cardId = cardId;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addRainbowsEnd(cardId);
    }

    @Override
    public String getDescription() { return (fr.astratime.lucky.entities.Card.isUpgradedId(cardId) ? Lang.t("Si deux symboles d'attaque pareils sortent, un Pot de Lutin + rejoint ta pioche.") : Lang.t("Si deux symboles d'attaque pareils sortent, un Pot de Lutin rejoint ta pioche.")); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("BOUT DE L'ARC-EN-CIEL ?"), EffectPopup.Style.GAINS, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.RAINBOW; }

}
