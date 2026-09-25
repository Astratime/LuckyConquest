package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Arc-en-ciel : chaque carte de la main (à suite) devient rouge ou noire au
 * hasard, puis une carte (le Pot de Lutin) est posée sur un emplacement libre
 * de la table, ou part dans la défausse s'il n'y en a plus. GameController
 * s'en charge.
 */
public class RainbowEffect extends Effect {

    private final String cardId;

    /** @param cardId identifiant de la carte posée sur la table */
    public RainbowEffect(String cardId) { this.cardId = cardId; }

    @Override
    public void onPlay(PlayContext context) {
        context.requestRainbow(cardId);
        context.addPopups(getPopups());
    }

    /** Aucun effet au spin : tout se passe quand la carte est jouée. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() {
        return "Les cartes de la main deviennent rouges ou noires au hasard. Ajoute un Pot de Lutin sur la table";
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("ARC-EN-CIEL !", EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }
}
