package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.entities.SymbolBreakdown;
import fr.astratime.lucky.popups.EffectPopup;

import java.util.List;

/**
 * Représente un événement survenu pendant la résolution d'un tour.
 * TurnResult est un journal d'événements — GameScreen les lit pour l'affichage.
 * Cette structure prépare le terrain pour les animations futures.
 */
public abstract class Event {
    /** Le calcul de ce qu'a fait le symbole, étape par étape, ou {@code null} (voir {@link #withBreakdown}). */
    private SymbolBreakdown breakdown;

    /** Joint à cet événement le calcul du symbole qui l'a produit (dégâts, bouclier, gains). @return cet événement */
    public Event withBreakdown(SymbolBreakdown breakdown) {
        this.breakdown = breakdown;
        return this;
    }

    /** @return le calcul du symbole qui a produit cet événement, ou {@code null} s'il ne vient pas d'un symbole. */
    public SymbolBreakdown getBreakdown() { return breakdown; }

    /** @return une description textuelle de l'événement, destinée au journal/log affiché en jeu. */
    public abstract String describe();

    /**
     * Textes animés affichés à l'écran pour cet événement (ex : "DÉGÂTS 45").
     * Aucun par défaut : un événement déjà montré ailleurs (ex : boost de symbole,
     * affiché quand la carte a été jouée) n'en a pas besoin.
     *
     * @return les popups de cet événement, dans l'ordre d'affichage
     */
    public List<EffectPopup> getPopups() { return List.of(); }
}
