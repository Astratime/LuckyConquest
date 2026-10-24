package fr.astratime.lucky.entities;

import fr.astratime.lucky.i18n.Lang;

/**
 * Archétype de deck d'une carte : la façon de jouer qu'elle sert (en plus des
 * quatre couleurs, qui ont leurs cartes à suite). Affiché sur la fiche de la
 * carte ; plus tard, les donjons de nouveaux lieux proposeront ces cartes.
 */
public enum Archetype {
    /** Tout tourne autour du rouleau Joker. */
    JOKER("Joker", "Tout tourne autour du rouleau Joker."),
    /** Les attaques rapportent des gains, et les gains de l'attaque. */
    LUTIN("Lutin", "Tes attaques rapportent des gains, et tes gains de l'attaque."),
    /** Poison (Créances) qui fait des dégâts et rapporte des gains. */
    RENTIER("Rentier", "Des Créances sur l'ennemi : elles lui font perdre des PV et te rapportent des gains à chacun de ses tours."),
    /** Malus qui gênent l'ennemi. */
    BLUFFEUR("Bluffeur", "Des malus qui gênent l'ennemi, et des cartes qui en profitent."),
    /** Manipule les rouleaux du tirage. */
    RELANCEUR("Relanceur", "Tu prépares et manipules les rouleaux du tirage.");

    private final String displayName;
    private final String description;

    Archetype(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /** @return le nom affiché de l'archétype. */
    public String getDisplayName() { return Lang.t(displayName); }

    /** @return la façon de jouer de l'archétype, en une phrase. */
    public String getDescription() { return Lang.t(description); }
}
