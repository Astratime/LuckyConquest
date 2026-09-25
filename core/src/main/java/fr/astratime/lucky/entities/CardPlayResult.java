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
    private final Rainbow           rainbow;

    /**
     * Résultat d'un Arc-en-ciel.
     *
     * @param recolored  cartes de la main remplacées par leur version d'une autre couleur (dans l'ordre de la main)
     * @param added      carte ajoutée (Pot de Lutin)
     * @param addedToHand {@code true} si elle est posée sur la table, {@code false} si elle part en défausse
     */
    public record Rainbow(List<Recolor> recolored, Card added, boolean addedToHand) {
        public Rainbow {
            recolored = List.copyOf(recolored);
        }
    }

    /** Une carte de la main qui change de couleur : {@code before} devient {@code after}. */
    public record Recolor(Card before, Card after) { }

    public CardPlayResult(DrawResult drawResult, List<EffectPopup> popups) {
        this(drawResult, popups, null, false);
    }

    /**
     * @param choice   choix demandé au joueur avant la suite du tour, ou {@code null}
     * @param autoSpin {@code true} si la main est bloquée et la machine se lance d'elle-même (Bingo)
     */
    public CardPlayResult(DrawResult drawResult, List<EffectPopup> popups, CardChoice choice, boolean autoSpin) {
        this(drawResult, popups, choice, autoSpin, null);
    }

    /** @param rainbow résultat de l'Arc-en-ciel joué, ou {@code null} */
    public CardPlayResult(DrawResult drawResult, List<EffectPopup> popups, CardChoice choice, boolean autoSpin,
                          Rainbow rainbow) {
        this.rainbow    = rainbow;
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
    /** @return le résultat de l'Arc-en-ciel joué, ou {@code null} si la carte n'en était pas un. */
    public Rainbow           getRainbow()    { return rainbow; }
}
