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

    public enum Suit { COEUR, CARREAU, TREFLE, PIQUE }

    private final String       id;
    private final String       name;
    private final String       assetPath;
    private final List<Effect> effects;
    private final Suit         suit;
    private final int          rank;

    public Card(String id, String name, String assetPath,
                List<Effect> effects, Suit suit, int rank) {
        this.id        = id;
        this.name      = name;
        this.assetPath = assetPath;
        this.effects   = List.copyOf(effects);
        this.suit      = suit;
        this.rank      = rank;
    }

    public String       getId()        { return id; }
    public String       getName()      { return name; }
    public Suit         getSuit()      { return suit; }
    public int          getRank()      { return rank; }
    public List<Effect> getEffects()   { return effects; }

    /**
     * Chemin de l'asset stocké explicitement dans la carte.
     * Le paramètre theme est conservé pour ne pas casser les appels existants,
     * mais il est ignoré : le chemin est défini dans le JSON.
     */
    public String getAssetPath(String theme) { return assetPath; }

    public String getDescription() {
        if (effects.isEmpty()) return "Aucun effet";
        return effects.stream()
            .map(Effect::getDescription)
            .collect(Collectors.joining("\n"));
    }

    @Override
    public String toString() { return name; }
}
