package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Arc-en-ciel : une suite est tirée au hasard et toutes les cartes à suite de
 * la main la prennent jusqu'à la fin du tour, puis une carte (le Pot de Lutin) est posée sur un emplacement libre
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
        return "Toutes les cartes de la main prennent une meme suite, tiree au hasard, jusqu'a la fin du tour. Ajoute un Pot de Lutin sur la table";
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("ARC-EN-CIEL !", EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }
}
