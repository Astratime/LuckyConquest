package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.effects.Effect;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Représente une carte du jeu.
 *
 * Les cartes sont créées par CardLoader depuis les fichiers JSON.
 * Suit et rank sont optionnels : une future carte custom peut ne pas avoir de
 * suite classique (suit=null) ou de rang pertinent (rank=1 par défaut).
 */
public class Card {

    /** Les quatre suites classiques d'un jeu de cartes. */
    public enum Suit {
        COEUR, CARREAU, TREFLE, PIQUE;

        /** @return {@code true} pour les suites rouges (Coeur, Carreau). */
        public boolean isRed() { return this == COEUR || this == CARREAU; }

        /** @return l'identifiant de la carte de rang {@code rank} de cette suite (ex : "12_coeur"). */
        public String cardId(int rank) { return rank + "_" + name().toLowerCase(); }
    }

    private final String       id;
    private final String       name;
    private final String       assetPath;
    private final List<Effect> effects;
    private final Suit         suit;
    private final int          rank;
    private final boolean      consumable;

    /**
     * @param id        identifiant unique de la carte (tel que défini dans le JSON)
     * @param name      nom affiché de la carte
     * @param assetPath chemin de la texture de la carte
     * @param effects   effets déclenchés quand la carte est jouée
     * @param suit      suite de la carte, ou {@code null} pour une carte sans suite
     * @param rank      rang de la carte (1 par défaut si non pertinent)
     */
    public Card(String id, String name, String assetPath,
                List<Effect> effects, Suit suit, int rank) {
        this(id, name, assetPath, effects, suit, rank, false);
    }

    /**
     * @param consumable {@code true} si la carte disparaît une fois jouée
     *                   (ni défausse, ni deck : ex. Pot de Lutin)
     * @see #Card(String, String, String, List, Suit, int)
     */
    public Card(String id, String name, String assetPath,
                List<Effect> effects, Suit suit, int rank, boolean consumable) {
        this.consumable = consumable;
        this.id        = id;
        this.name      = name;
        this.assetPath = assetPath;
        this.effects   = List.copyOf(effects);
        this.suit      = suit;
        this.rank      = rank;
    }

    /** @return l'identifiant unique de la carte. */
    public String       getId()        { return id; }
    /** @return le nom affiché de la carte. */
    public String       getName()      { return name; }
    /** @return la suite de la carte, ou {@code null} si elle n'en a pas. */
    public Suit         getSuit()      { return suit; }
    /** @return le rang de la carte. */
    public int          getRank()      { return rank; }
    /** @return {@code true} si la carte disparaît une fois jouée (elle ne rejoint pas la défausse). */
    public boolean      isConsumable() { return consumable; }
    /** @return les effets déclenchés quand la carte est jouée (liste immuable). */
    public List<Effect> getEffects()   { return effects; }

    /** @return le chemin de la texture de la carte, tel que défini dans le JSON. */
    public String getAssetPath() { return assetPath; }

    /** @return la description de la carte, construite à partir de celle de chacun de ses effets. */
    public String getDescription() {
        if (effects.isEmpty()) return "Aucun effet";
        return effects.stream()
            .map(Effect::getDescription)
            .collect(Collectors.joining("\n"));
    }

    /** @return le nom affiché de la carte (voir {@link #getName()}). */
    @Override
    public String toString() { return name; }
}
