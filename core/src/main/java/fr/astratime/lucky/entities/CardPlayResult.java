package fr.astratime.lucky.entities;

import fr.astratime.lucky.popups.EffectPopup;

import java.util.List;

/**
 * Résultat d'une carte jouée : les cartes piochées par ses effets immédiats,
 * et les textes à afficher (un par bonus, avec leurs valeurs réelles).
 */
public class CardPlayResult {

    private final DrawResult        drawResult;
    private final List<EffectPopup> popups;

    public CardPlayResult(DrawResult drawResult, List<EffectPopup> popups) {
        this.drawResult = drawResult;
        this.popups     = List.copyOf(popups);
    }

    /** @return une carte non jouée (absente de la main) : ni pioche ni texte. */
    public static CardPlayResult none() { return new CardPlayResult(DrawResult.empty(), List.of()); }

    /** @return les cartes piochées par les effets immédiats de la carte (vide si aucun). */
    public DrawResult        getDrawResult() { return drawResult; }
    /** @return les textes à afficher pour la carte jouée (liste immuable). */
    public List<EffectPopup> getPopups()     { return popups; }
}
