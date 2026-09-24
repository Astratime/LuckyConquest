package fr.astratime.lucky.entities.context;

import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.choices.CardChoice;
import fr.astratime.lucky.entities.effects.Effect;
import fr.astratime.lucky.popups.EffectPopup;

import java.util.ArrayList;
import java.util.List;

/**
 * Contexte d'une carte au moment où elle est jouée (pendant le tour, avant le spin).
 *
 * Chaque effet y déclare, via {@link Effect#onPlay(PlayContext)} :
 *  - ce qu'il demande immédiatement (cartes à piocher, gains à créditer ou à
 *    consommer) — GameController applique ensuite pioche et gains ;
 *  - les effets à appliquer au moment du spin ;
 *  - les textes (popups) à afficher, avec leurs valeurs réelles.
 */
public class PlayContext {

    private final Player            player;
    private final List<Effect>      effectsForSpin = new ArrayList<>();
    private final List<EffectPopup> popups         = new ArrayList<>();
    private int cardsToDraw = 0;
    private int gains       = 0;
    private CardChoice choice;
    private boolean    autoSpin = false;

    /** @param player joueur qui joue la carte (ses gains peuvent être consommés immédiatement) */
    public PlayContext(Player player) {
        this.player = player;
    }

    /** Demande à piocher {@code count} cartes supplémentaires immédiatement. */
    public void addCardsToDraw(int count) { cardsToDraw += count; }

    /** @return le nombre de cartes à piocher immédiatement suite aux effets de la carte jouée. */
    public int getCardsToDraw() { return cardsToDraw; }

    /** Demande à créditer immédiatement {@code amount} gains au joueur. */
    public void addGains(int amount) { gains += amount; }

    /** @return les gains à créditer immédiatement suite aux effets de la carte jouée. */
    public int getGains() { return gains; }

    /**
     * Consomme immédiatement un pourcentage des gains actuels du joueur.
     *
     * @param percent pourcentage à consommer (0.3f = 30%)
     * @return le montant effectivement consommé
     */
    public int consumeGainsPercent(float percent) { return player.consumeGainsPercent(percent); }

    /** @return les effets de cartes qui durent plusieurs tours, à modifier immédiatement (Recyclage, Porte-bonheur). */
    public LastingEffects getLastingEffects() { return player.getLastingEffects(); }

    /** Demande au joueur un choix (Pari, Roulette russe) avant la suite du tour. */
    public void requestChoice(CardChoice choice) { this.choice = choice; }

    /** @return le choix demandé au joueur, ou {@code null} si aucun. */
    public CardChoice getChoice() { return choice; }

    /** Plus aucune carte ne peut être jouée : la machine se lance d'elle-même (Bingo). */
    public void requestAutoSpin() { autoSpin = true; }

    /** @return {@code true} si la machine doit se lancer d'elle-même après cette carte. */
    public boolean isAutoSpin() { return autoSpin; }

    /** Met {@code effect} en attente : il sera appliqué au TurnContext au moment du spin. */
    public void queueForSpin(Effect effect) { effectsForSpin.add(effect); }

    /** @return les effets à appliquer au moment du spin, dans l'ordre où ils ont été ajoutés. */
    public List<Effect> getEffectsForSpin() { return effectsForSpin; }

    /** Ajoute des textes à afficher pour la carte jouée. */
    public void addPopups(List<EffectPopup> toAdd) { popups.addAll(toAdd); }

    /** @return les textes à afficher pour la carte jouée, dans l'ordre d'affichage. */
    public List<EffectPopup> getPopups() { return popups; }
}
