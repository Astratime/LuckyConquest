package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.choices.CardChoice;
import fr.astratime.lucky.popups.EffectPopup;

import java.util.List;

/**
 * Résultat d'une carte jouée : les cartes piochées par ses effets immédiats,
 * et les textes à afficher (un par bonus, avec leurs valeurs réelles).
 */
public class CardPlayResult {

    private final DrawResult        drawResult;
    private final List<EffectPopup> popups;
    private final CardChoice        choice;
    private final boolean           autoSpin;

    public CardPlayResult(DrawResult drawResult, List<EffectPopup> popups) {
        this(drawResult, popups, null, false);
    }

    /**
     * @param choice   choix demandé au joueur avant la suite du tour, ou {@code null}
     * @param autoSpin {@code true} si la main est bloquée et la machine se lance d'elle-même (Bingo)
     */
    public CardPlayResult(DrawResult drawResult, List<EffectPopup> popups, CardChoice choice, boolean autoSpin) {
        this.drawResult = drawResult;
        this.popups     = List.copyOf(popups);
        this.choice     = choice;
        this.autoSpin   = autoSpin;
    }

    /** @return une carte non jouée (absente de la main) : ni pioche ni texte. */
    public static CardPlayResult none() { return new CardPlayResult(DrawResult.empty(), List.of()); }

    /** @return les cartes piochées par les effets immédiats de la carte (vide si aucun). */
    public DrawResult        getDrawResult() { return drawResult; }
    /** @return les textes à afficher pour la carte jouée (liste immuable). */
    public List<EffectPopup> getPopups()     { return popups; }
    /** @return le choix demandé au joueur avant la suite du tour, ou {@code null} si aucun. */
    public CardChoice        getChoice()     { return choice; }
    /** @return {@code true} si plus aucune carte ne peut être jouée et que la machine se lance d'elle-même. */
    public boolean           isAutoSpin()    { return autoSpin; }
}
