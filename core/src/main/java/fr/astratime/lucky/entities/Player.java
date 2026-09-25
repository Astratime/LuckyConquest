package fr.astratime.lucky.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Le joueur : points de vie, deck/défausse/main courante, machine à sous
 * personnelle, gains accumulés et modificateurs de combat du tour
 * (bouclier).
 */
public class Player {

    /** Nombre maximum de cartes (non jouées) sur la table pendant un tour. */
    public static final int MAX_HAND_SIZE = 8;

    private final String      name;
    private final DiscardPile discardPile = new DiscardPile();
    private final Deck        deck;
    private final SlotMachine slotMachine = new SlotMachine();
    /** Effets de cartes qui durent plusieurs tours (Recyclage, Porte-bonheur). */
    private final LastingEffects lastingEffects = new LastingEffects();
    /** Cartes sur la table, pas encore jouées ce tour. */
    private final List<Card>  currentHand = new ArrayList<>();
    /** Cartes jouées ce tour : quittent la main, rejoignent la défausse en fin de tour. */
    private final List<Card>  playedCards = new ArrayList<>();
    private       int         hp;
    private final int         maxHp;

    /** Monnaie gagnée en combat. Servira à acheter des bonus en combat. */
    private int gains = 0;

    /** Bouclier accumulé ce tour par les DefenseAction. */
    private int shield = 0;

    /**
     * @param name     nom affiché du joueur
     * @param maxHp    points de vie maximum (le joueur démarre à pleine vie)
     * @param cards    cartes composant le deck initial (mélangées à la construction du Deck)
     */
    public Player(String name, int maxHp, List<Card> cards) {
        this.name  = name;
        this.maxHp = maxHp;
        this.hp    = maxHp;
        this.deck  = new Deck(cards, discardPile);
    }

    /**
     * Inflige des dégâts au joueur : le bouclier accumulé absorbe les dégâts
     * en priorité, seul l'excédent (s'il y en a) retire des points de vie,
     * sans jamais descendre sous 0.
     *
     * @param damage dégâts bruts, avant absorption par le bouclier
     * @return les dégâts effectivement retirés des points de vie (après bouclier)
     */
    public int takeDamage(int damage) {
        int absorbed  = Math.min(shield, damage);
        shield -= absorbed;
        int remaining = damage - absorbed;
        int actualLoss = Math.min(hp, remaining);
        hp -= actualLoss;
        return actualLoss;
    }
    /**
     * Rend {@code amount} points de vie, sans dépasser le maximum.
     *
     * @return les points de vie réellement rendus (0 si le joueur était déjà au maximum)
     */
    public int heal(int amount) {
        int healed = Math.min(maxHp, hp + amount) - hp;
        hp += healed;
        return healed;
    }
    /** @return {@code true} si le joueur n'a plus de points de vie. */
    public boolean isDefeated()        { return hp <= 0; }

    /** @return la proportion de vie restante, entre 0 et 1 (utilisé par les effets conditionnels). */
    public float getHpRatio() { return (float) hp / maxHp; }

    /** Ajoute {@code amount} aux gains accumulés, sans jamais passer sous 0. */
    public void addGains(int amount) { gains = Math.max(0, gains + amount); }

    /**
     * Consomme un pourcentage des gains actuels (ex : coût d'un As de Trèfle).
     *
     * @param percent pourcentage à consommer (0.3f = 30%)
     * @return le montant effectivement consommé
     */
    public int consumeGainsPercent(float percent) {
        int amount = Math.round(gains * percent);
        gains -= amount;
        return amount;
    }

    // -------------------------------------------------------------------------
    // Main, cartes jouées et défausse
    // -------------------------------------------------------------------------

    /**
     * Pioche {@code count} cartes du deck. Celles qui tiennent dans la main
     * (jusqu'à {@link #MAX_HAND_SIZE}) la rejoignent, le surplus part
     * directement à la défausse.
     *
     * @param count nombre de cartes à piocher
     * @return les cartes ajoutées à la main et celles défaussées faute de place
     */
    public DrawResult draw(int count) {
        List<Card> drawn = deck.draw(count);
        int room = Math.max(0, MAX_HAND_SIZE - currentHand.size());
        List<Card> added     = drawn.subList(0, Math.min(room, drawn.size()));
        List<Card> discarded = drawn.subList(added.size(), drawn.size());
        currentHand.addAll(added);
        discardPile.addAll(discarded);
        return new DrawResult(added, discarded);
    }

    /**
     * Joue une carte de la main : elle quitte la table et reste de côté
     * jusqu'à la fin du tour (elle ne peut donc pas être repiochée ce tour-ci).
     *
     * @param card carte jouée
     * @return {@code false} si la carte n'était pas dans la main (rien n'est fait)
     */
    public boolean playCard(Card card) {
        if (!currentHand.remove(card)) return false;
        if (!card.isConsumable()) playedCards.add(card); // une carte consommable disparaît
        return true;
    }

    /**
     * Remplace une carte de la main par une autre, à la même place (ex : sa
     * couleur changée par l'Arc-en-ciel).
     *
     * @return {@code false} si {@code card} n'était pas dans la main (rien n'est fait)
     */
    public boolean replaceInHand(Card card, Card replacement) {
        int index = currentHand.indexOf(card);
        if (index < 0) return false;
        currentHand.set(index, replacement);
        return true;
    }

    /**
     * Remplace, dans la main et parmi les cartes jouées, chaque carte présente
     * dans {@code originals} par sa carte d'origine (fin de l'effet d'un Arc-en-ciel).
     */
    public void restoreCards(Map<Card, Card> originals) {
        currentHand.replaceAll(card -> originals.getOrDefault(card, card));
        playedCards.replaceAll(card -> originals.getOrDefault(card, card));
    }

    /**
     * Pose une nouvelle carte sur la table s'il reste un emplacement libre,
     * sinon la met directement dans la défausse.
     *
     * @return {@code true} si la carte a rejoint la main
     */
    public boolean addToHandOrDiscard(Card card) {
        if (currentHand.size() < MAX_HAND_SIZE) {
            currentHand.add(card);
            return true;
        }
        discardPile.addAll(List.of(card));
        return false;
    }

    /**
     * Fin de tour : envoie à la défausse les cartes jouées puis celles restées
     * sur la table, et vide la main.
     *
     * @return les cartes restées sur la table (non jouées), dans leur ordre dans la main
     */
    public List<Card> discardHand() {
        List<Card> remaining = new ArrayList<>(currentHand);
        discardPile.addAll(playedCards);
        discardPile.addAll(remaining);
        playedCards.clear();
        currentHand.clear();
        return remaining;
    }

    /** Ajoute {@code amount} au bouclier accumulé ce tour. */
    public void addShield(int amount) { shield += amount; }

    /** Réinitialise le bouclier en fin de tour. */
    public void resetTurnDefenses() {
        shield = 0;
    }

    /** @return le nom affiché du joueur. */
    public String      getName()                   { return name; }
    /** @return les points de vie actuels. */
    public int         getHp()                     { return hp; }
    /** @return les points de vie maximum. */
    public int         getMaxHp()                  { return maxHp; }
    /** @return la défausse du joueur. */
    public DiscardPile getDiscardPile()             { return discardPile; }
    /** @return le deck du joueur. */
    public Deck        getDeck()                    { return deck; }
    /** @return la machine à sous personnelle du joueur. */
    public SlotMachine getSlotMachine()             { return slotMachine; }
    /** @return les effets de cartes qui durent plusieurs tours pendant ce combat. */
    public LastingEffects getLastingEffects()      { return lastingEffects; }
    /** @return les cartes sur la table, pas encore jouées ce tour (vue non modifiable). */
    public List<Card>  getCurrentHand()             { return Collections.unmodifiableList(currentHand); }
    /** @return les cartes jouées ce tour (vue non modifiable). */
    public List<Card>  getPlayedCards()             { return Collections.unmodifiableList(playedCards); }
    /** @return les gains accumulés. */
    public int         getGains()                   { return gains; }
    /** @return le bouclier accumulé ce tour. */
    public int         getShield()                  { return shield; }
}
