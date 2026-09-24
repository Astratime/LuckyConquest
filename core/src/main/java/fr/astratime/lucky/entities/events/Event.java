package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;

import java.util.List;

/**
 * Représente un événement survenu pendant la résolution d'un tour.
 * TurnResult est un journal d'événements — GameScreen les lit pour l'affichage.
 * Cette structure prépare le terrain pour les animations futures.
 */
public abstract class Event {
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
