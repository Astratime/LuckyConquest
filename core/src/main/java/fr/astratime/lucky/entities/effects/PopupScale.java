package fr.astratime.lucky.entities.effects;

/**
 * Réglages de taille des textes animés ({@link EffectPopup}), regroupés ici pour
 * équilibrer l'affichage sans parcourir chaque effet et chaque événement.
 *
 * Chaque valeur est celle qui donne la taille de texte maximale : un bonus
 * égal (ou supérieur) s'affiche en grand, un bonus deux fois plus petit à
 * mi-taille, etc. (la taille minimale reste toujours lisible).
 */
public final class PopupScale {

    // --- Cartes jouées ---------------------------------------------------------
    /** Bonus d'attaque d'une carte (Pique, Trèfle, effet ATTACK). */
    public static final float CARD_ATTACK_BONUS    = 15f;
    /** Bonus de bouclier d'une carte (effet DEFENSE). */
    public static final float CARD_DEFENSE_BONUS   = 30f;
    /** Multiplicateur de gains ajouté par une carte (Trèfle, effet MULTIPLIER). */
    public static final float CARD_GAIN_MULTIPLIER = 30f;
    /** Pourcentage de drain de vie d'une carte (Coeur). */
    public static final float CARD_DRAIN_PERCENT   = 15f;
    /** Pourcentage de renvoi de dégâts d'une carte (Carreau). */
    public static final float CARD_REFLECT_PERCENT = 40f;
    /** Boost de poids d'un symbole (effet BOOST_SYMBOL). */
    public static final float CARD_SYMBOL_BOOST    = 200f;
    /** Cartes piochées immédiatement (effet EXTRA_DRAW). */
    public static final float CARD_EXTRA_DRAW      = 4f;
    /** Gains crédités immédiatement (effet GAIN). */
    public static final float CARD_GAINS           = 1000f;

    // --- As de Trèfle (valeurs calculées à la pose) ----------------------------
    public static final float ACE_OF_CLUBS_CONSUMED = 1000f;
    public static final float ACE_OF_CLUBS_ATTACK   = 300f;
    public static final float ACE_OF_CLUBS_BOOST    = 1500f;

    // --- Résultats du tirage ---------------------------------------------------
    /** Dégâts infligés à l'ennemi par un symbole (après défense). */
    public static final float SPIN_DAMAGE    = 100f;
    /** Gains obtenus (symbole, paire ou jackpot). */
    public static final float SPIN_GAINS     = 2000f;
    /** Bouclier gagné par un symbole. */
    public static final float SPIN_SHIELD    = 40f;
    /** Vie perdue par le joueur lors de la riposte. */
    public static final float SPIN_LIFE_LOST = 30f;
    /** Vie rendue par le drain. */
    public static final float SPIN_HEAL      = 30f;
    /** Dégâts renvoyés à l'ennemi. */
    public static final float SPIN_REFLECT   = 30f;

    // --- Textes sans valeur chiffrée -------------------------------------------
    /** Intensité des textes sans valeur chiffrée qui accompagnent un bonus plus important. */
    public static final float SECONDARY_INTENSITY = 0.6f;
    /** Intensité des textes toujours affichés en grand (As, jackpot). */
    public static final float MAX_INTENSITY       = 1f;

    private PopupScale() {}
}
