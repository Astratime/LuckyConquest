package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.effects.Effect;
import fr.astratime.lucky.i18n.Lang;

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
    /** Version « + », obtenue en fusionnant {@link #UPGRADE_COST} exemplaires de la carte (valeurs +50 %). */
    private final boolean      upgraded;
    /** {@code true} si la carte a une version « + » (au moins une valeur à améliorer). */
    private final boolean      upgradable;

    /** Suffixe de l'identifiant d'une carte « + » (ex : "13_trefle+"). */
    public static final String UPGRADE_SUFFIX = "+";
    /** Exemplaires d'une carte fusionnés en un exemplaire de sa version « + ». */
    public static final int    UPGRADE_COST   = 3;
    /** Prix d'une fusion, en pièces (Astra, 2026-10-10). */
    public static final long   UPGRADE_PRICE  = 10_000_000L;
    /** Les valeurs d'une carte « + » : celles de la carte x {@value}. */
    public static final float  UPGRADE_FACTOR = 1.5f;

    /** @return l'identifiant de la version « + » de la carte {@code id}. */
    public static String upgradedId(String id) { return isUpgradedId(id) ? id : id + UPGRADE_SUFFIX; }

    /** @return {@code true} si {@code id} est celui d'une carte « + ». */
    public static boolean isUpgradedId(String id) { return id.endsWith(UPGRADE_SUFFIX); }

    /** @return l'identifiant de la carte de base de {@code id} (lui-même si ce n'est pas une carte « + »). */
    public static String baseId(String id) {
        return isUpgradedId(id) ? id.substring(0, id.length() - UPGRADE_SUFFIX.length()) : id;
    }

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
        this(id, name, assetPath, effects, suit, rank, consumable, false, false);
    }

    /**
     * @param upgraded   {@code true} pour une carte « + » (son id finit par {@link #UPGRADE_SUFFIX})
     * @param upgradable {@code true} si la carte a une version « + »
     * @see #Card(String, String, String, List, Suit, int, boolean)
     */
    public Card(String id, String name, String assetPath, List<Effect> effects, Suit suit, int rank,
                boolean consumable, boolean upgraded, boolean upgradable) {
        this.upgraded   = upgraded;
        this.upgradable = upgradable;
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
    /** @return le nom affiché de la carte, suivi de « + » pour une carte « + ». */
    public String       getName()      { return upgraded ? Lang.t(name) + " +" : Lang.t(name); }
    /** @return {@code true} pour une carte « + » (valeurs +50 %, liseré doré). */
    public boolean      isUpgraded()   { return upgraded; }
    /** @return {@code true} si la carte a une version « + » (voir {@link #isUpgraded()}). */
    public boolean      isUpgradable() { return upgradable; }
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
        if (effects.isEmpty()) return Lang.t("Aucun effet");
        return effects.stream()
            .map(Effect::getDescription)
            .collect(Collectors.joining("\n"));
    }

    /** @return le nom affiché de la carte (voir {@link #getName()}). */
    @Override
    public String toString() { return upgraded ? name + " +" : name; }
}
