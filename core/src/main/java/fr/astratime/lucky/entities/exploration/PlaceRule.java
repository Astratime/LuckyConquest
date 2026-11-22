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
 *       d'un tour sans soin, il perd {@link #THIRST_PERCENT} % de ses PV max (son bouclier ne protège pas) ;</li>
 *   <li>{@link #WAGON_BONDE} : tous les {@link #CROWDED_PERIOD} tours, le joueur joue {@link #CROWDED_PLAYS}
 *       carte de moins ce tour-là (le wagon est bondé) ;</li>
 *   <li>{@link #VERGLAS} : tous les {@link #ICE_PERIOD} tours, les combinaisons ne donnent que
 *       {@link #ICE_COMBO_PERCENT} % de leur Puissance (attaque ; les gains ne changent pas) ;</li>
 *   <li>{@link #PRESAGE} : à chaque tour, le temple annonce un symbole de la machine du joueur ; s'il sort
 *       au tirage, Puissance +{@link #OMEN_POWER} ce tour, sinon l'ennemi frappe {@link #OMEN_ENEMY_PERCENT} %
 *       plus fort à son tour ;</li>
 *   <li>{@link #MINUIT} : l'horloge du manoir avance d'une heure à chaque tour ; tous les
 *       {@link #MIDNIGHT_PERIOD} tours, minuit sonne et l'ennemi joue deux tours de suite.</li>
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
        + "PV max. Ton bouclier ne protège pas."),
    WAGON_BONDE("Wagon bondé", "Tous les 3 tours, le wagon est bondé : tu joues 1 carte de moins ce tour-là."),
    VERGLAS("Verglas", "Un tour sur deux, la table est verglacée : tes combinaisons ne donnent que la moitié de leur "
        + "Puissance."),
    PRESAGE("Présage", "Chaque tour, le temple annonce un symbole de ta machine. S'il sort à ton tirage, Puissance +1 "
        + "ce tour. Sinon, l'ennemi frappe 30 % plus fort à son tour."),
    MINUIT("Minuit", "L'horloge du manoir avance d'une heure à chaque tour. Au 6e tour, puis tous les 6 tours, "
        + "minuit sonne : l'ennemi joue deux tours de suite.");

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

    /** Wagon bondé : tous les tant de tours (au 3e, au 6e…). */
    public static final int CROWDED_PERIOD = 3;
    /** Wagon bondé : cartes jouées en moins ce tour-là. */
    public static final int CROWDED_PLAYS = 1;

    /** Verglas : tous les tant de tours (au 2e, au 4e…). */
    public static final int ICE_PERIOD = 2;
    /** Verglas : part de la Puissance des combinaisons gardée ce tour-là, en %. */
    public static final int ICE_COMBO_PERCENT = 50;

    /** Présage : Puissance gagnée quand le symbole annoncé sort au tirage. */
    public static final int OMEN_POWER = 1;
    /** Présage : force en plus des attaques de l'ennemi, à son tour, quand le symbole annoncé n'est pas sorti (en %). */
    public static final int OMEN_ENEMY_PERCENT = 30;

    /** Minuit : l'horloge sonne tous les tant de tours (au 6e, au 12e…), l'ennemi rejoue aussitôt. */
    public static final int MIDNIGHT_PERIOD = 6;

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

    /** @return {@code true} si le wagon est bondé au tour {@code turn} (à partir de 1) : une carte jouée de moins. */
    public boolean isCrowded(int turn) { return this == WAGON_BONDE && turn % CROWDED_PERIOD == 0; }

    /** @return les tours avant le prochain wagon bondé, celui-ci compris (1 : ce tour). */
    public static int turnsBeforeCrowd(int turn) { return CROWDED_PERIOD - (turn - 1) % CROWDED_PERIOD; }

    /** @return {@code true} si la table est verglacée au tour {@code turn} (à partir de 1) : combinaisons à moitié. */
    public boolean isIcy(int turn) { return this == VERGLAS && turn % ICE_PERIOD == 0; }

    /** @return {@code true} si le Présage joue (un symbole annoncé à chaque tour). */
    public boolean hasOmen() { return this == PRESAGE; }

    /** @return {@code true} si minuit sonne au tour {@code turn} (à partir de 1) : l'ennemi y joue deux tours de suite. */
    public boolean isMidnight(int turn) { return this == MINUIT && turn % MIDNIGHT_PERIOD == 0; }

    /** @return l'heure de l'horloge du manoir au tour {@code turn} : 1 au premier tour … {@link #MIDNIGHT_PERIOD} à minuit. */
    public static int clockHour(int turn) { return (turn - 1) % MIDNIGHT_PERIOD + 1; }

    /** @return {@code true} si la Soif joue (soins divisés, PV perdus sans soin). */
    public boolean isThirsty() { return this == SOIF; }

    /** @return le niveau de la marée au tour {@code turn} (1 au premier tour, 0 tous les {@link #TIDE_CYCLE} tours). */
    public static int tide(int turn) { return turn % TIDE_CYCLE; }

    /** @return {@code true} si la marée est haute au tour {@code turn}. */
    public boolean isHighTide(int turn) { return this == MAREE && tide(turn) >= HIGH_TIDE; }
}
