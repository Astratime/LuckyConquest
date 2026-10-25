package fr.astratime.lucky.entities.exploration;

import fr.astratime.lucky.i18n.Lang;

/**
 * La règle d'un lieu de l'Exploration : elle joue dans tous ses combats, contre
 * le joueur seul. La Bulle d'air l'annule quelques tours.
 * <ul>
 *   <li>{@link #SCORBUT} : au début du combat, puis tous les {@link #SCURVY_PERIOD} tours,
 *       une carte de la main est remplacée par la carte Scorbut ; tant qu'elle reste en
 *       main sans être jouée, le tirage donne moitié moins d'attaque, de défense et de gains ;</li>
 *   <li>{@link #GRISOU} : tous les {@link #FIREDAMP_PERIOD} tours, un coup de grisou retire
 *       au joueur {@link #FIREDAMP_PERCENT} % de ses PV max (son bouclier le protège) ;</li>
 *   <li>{@link #MAREE} : la marée monte d'un cran à chaque tour ; de {@link #HIGH_TIDE} à
 *       {@link #TIDE_CYCLE} - 1, la marée haute retire {@link #HIGH_TIDE_MALUS} % à l'attaque
 *       du joueur ; à {@link #TIDE_CYCLE}, elle redescend à 0 ;</li>
 *   <li>{@link #MANEGE} : tous les {@link #CAROUSEL_PERIOD} tours, un rouleau du joueur, au hasard, est
 *       bloqué pour ce tirage (il tourne sur le manège) ;</li>
 *   <li>{@link #SOIF} : les soins du joueur sont divisés par {@link #THIRST_HEAL_DIVISOR} ; à la fin
 *       d'un tour sans soin, il perd {@link #THIRST_PERCENT} % de ses PV max (son bouclier ne protège pas).</li>
 * </ul>
 */
public enum PlaceRule {

    NONE(null, null),
    SCORBUT("Scorbut", "Au début du combat, puis tous les 3 tours, une carte de ta main devient Scorbut. "
        + "Tant qu'elle reste en main, ton tirage donne moitié moins d'attaque, de défense et de gains. Joue-la pour t'en débarrasser."),
    GRISOU("Coup de grisou", "Tous les 5 tours, une explosion te retire 10 % de tes PV max. Ton bouclier te protège. "
        + "L'ennemi n'est pas touché."),
    MAREE("Marée", "La marée monte d'un cran à chaque tour. De 3 à 5, la marée haute retire 30 % à ton attaque. "
        + "À 6, elle redescend à 0."),
    MANEGE("Manège", "Tous les 3 tours, le manège tourne : un de tes rouleaux, au hasard, est bloqué pour ce tirage."),
    SOIF("Soif", "Tes soins sont divisés par 2. À la fin de chaque tour où tu ne t'es pas soigné, tu perds 1 % de tes "
        + "PV max. Ton bouclier ne protège pas.");

    /** Id de la carte Scorbut. */
    public static final String SCURVY_CARD = "scorbut";
    /** Le Scorbut revient tous les tant de tours. */
    public static final int SCURVY_PERIOD = 3;
    /** Le tirage, Scorbut en main, est multiplié d'autant (attaque, défense, gains). */
    public static final float SCURVY_FACTOR = 0.5f;
    /** Un coup de grisou tous les tant de tours. */
    public static final int FIREDAMP_PERIOD = 5;
    /** Dégâts d'un coup de grisou, en % des PV max du joueur. */
    public static final int FIREDAMP_PERCENT = 10;
    /** Niveau de marée à partir duquel la marée est haute. */
    public static final int HIGH_TIDE = 3;
    /** Arrivée à ce niveau, la marée redescend à 0. */
    public static final int TIDE_CYCLE = 6;
    /** Attaque retirée par la marée haute, en %. */
    public static final int HIGH_TIDE_MALUS = 30;

    /** Le manège tourne tous les tant de tours (au 3e, au 6e…). */
    public static final int CAROUSEL_PERIOD = 3;
    /** Soif : les soins du joueur sont divisés d'autant. */
    public static final int THIRST_HEAL_DIVISOR = 2;
    /** Soif : PV perdus à la fin d'un tour sans soin, en % des PV max. */
    public static final int THIRST_PERCENT = 1;

    private final String name;
    private final String description;

    PlaceRule(String name, String description) {
        this.name        = name;
        this.description = description;
    }

    /** @return le nom de la règle (ex : "Scorbut"), ou {@code null} sans règle. */
    public String getName() { return Lang.t(name); }
    /** @return la règle, en quelques phrases courtes, ou {@code null} sans règle. */
    public String getDescription() { return Lang.t(description); }

    /** @return {@code true} si le Scorbut remplace une carte de la main au tour {@code turn} (à partir de 1). */
    public boolean scurvyArrives(int turn) {
        return this == SCORBUT && (turn - 1) % SCURVY_PERIOD == 0;
    }

    /** @return {@code true} si un coup de grisou éclate à la fin du tour {@code turn}. */
    public boolean firedampExplodes(int turn) {
        return this == GRISOU && turn % FIREDAMP_PERIOD == 0;
    }

    /** @return les tours avant le prochain coup de grisou, celui-ci compris (1 : à la fin de ce tour). */
    public static int turnsBeforeFiredamp(int turn) {
        return FIREDAMP_PERIOD - (turn - 1) % FIREDAMP_PERIOD;
    }

    /** @return {@code true} si le manège bloque un rouleau au tirage du tour {@code turn} (à partir de 1). */
    public boolean carouselTurns(int turn) { return this == MANEGE && turn % CAROUSEL_PERIOD == 0; }

    /** @return les tours avant le prochain tour de manège, celui-ci compris (1 : ce tour). */
    public static int turnsBeforeCarousel(int turn) { return CAROUSEL_PERIOD - (turn - 1) % CAROUSEL_PERIOD; }

    /** @return {@code true} si la Soif joue (soins divisés, PV perdus sans soin). */
    public boolean isThirsty() { return this == SOIF; }

    /** @return le niveau de la marée au tour {@code turn} (1 au premier tour, 0 tous les {@link #TIDE_CYCLE} tours). */
    public static int tide(int turn) { return turn % TIDE_CYCLE; }

    /** @return {@code true} si la marée est haute au tour {@code turn}. */
    public boolean isHighTide(int turn) { return this == MAREE && tide(turn) >= HIGH_TIDE; }
}
