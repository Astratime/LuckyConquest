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
     * Applique la partie immédiate de l'effet, dès que la carte est jouée.
     * Ne fait rien par défaut : la plupart des effets n'agissent qu'au spin.
     *
     * @param context contexte de la carte jouée (ex : cartes à piocher tout de suite)
     */
    public void onPlay(PlayContext context) { }

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
     * "GAINS x+20" puis "ATTAQUE +10"). Chaque effet doit en fournir au moins un,
     * pour que le joueur voie toujours ce que la carte lui apporte.
     *
     * @return les popups de cet effet, dans l'ordre d'affichage
     */
    public abstract List<EffectPopup> getPopups();
}
