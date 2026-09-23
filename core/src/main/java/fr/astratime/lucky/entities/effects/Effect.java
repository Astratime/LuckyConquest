package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;

import java.util.List;

/**
 * Effet produit par une carte jouée en phase 1.
 * Deux moments d'application, sans accès au GameState complet :
 *  - {@link #onPlay(PlayContext)} : immédiatement, quand la carte est jouée
 *    (ex : piocher des cartes pendant le tour) ;
 *  - {@link #apply(TurnContext)} : au moment du spin, pour modifier le
 *    SpinContext (boosts machine) ou le CombatContext (bonus de combat).
 */
public abstract class Effect {

    /**
     * Appelé dès que la carte est jouée. Par défaut, l'effet est mis en attente
     * pour le spin et ses textes ({@link #getPopups()}) sont affichés.
     * Les effets immédiats (pioche, gains...) redéfinissent cette méthode ; ceux
     * dont les valeurs dépendent de l'état du jeu à la pose (ex : As de Trèfle)
     * y calculent aussi leurs textes.
     *
     * @param context contexte de la carte jouée
     */
    public void onPlay(PlayContext context) {
        context.queueForSpin(this);
        context.addPopups(getPopups());
    }

    /**
     * Applique l'effet de la carte au contexte du tour, au moment du spin.
     *
     * @param context contexte du tour (spin + combat) à modifier
     */
    public abstract void apply(TurnContext context);

    /** @return une courte description de l'effet, affichée en infobulle sur la carte. */
    public abstract String getDescription();

    /**
     * Textes animés affichés quand la carte est jouée (un par bonus, ex :
     * "GAINS x+20" puis "ATTAQUE +10"). Chaque effet doit en déclarer, pour que
     * le joueur voie toujours ce que la carte lui apporte. Pour un effet dont
     * certaines valeurs ne sont connues qu'à la pose, ce sont les textes fixes :
     * les autres sont ajoutés par {@link #onPlay(PlayContext)}.
     *
     * @return les popups fixes de cet effet, dans l'ordre d'affichage
     */
    public abstract List<EffectPopup> getPopups();
}
