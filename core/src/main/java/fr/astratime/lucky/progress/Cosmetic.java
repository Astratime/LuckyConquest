package fr.astratime.lucky.progress;

import fr.astratime.lucky.i18n.Lang;

/**
 * Cosmétiques : dos des cartes du joueur, tapis (feutre) de la table de combat et
 * habillage de la machine à sous. Ils ne changent rien aux combats.
 *
 * Chaque sorte a un cosmétique d'origine, toujours possédé (le dos et la table
 * du jeu, la table de chaque lieu en Exploration). Les autres se débloquent
 * par les succès, par le bestiaire (ennemis vaincus) ou s'achètent avec des pièces.
 * Leurs images sont dans {@code assets/cosmetics/<style>/} (voir
 * {@code tools/cosmetics/generate_cosmetics.py}).
 */
public enum Cosmetic {

    BACK_CLASSIC(Kind.BACK, null, Unlock.DEFAULT, 0),
    BACK_NEON(Kind.BACK, "neon", Unlock.ACHIEVEMENTS, 10),
    BACK_ABYSS(Kind.BACK, "abysses", Unlock.BESTIARY, 25),
    BACK_WOOD(Kind.BACK, "bois", Unlock.COINS, 10_000_000_000L),
    BACK_GOLD(Kind.BACK, "or", Unlock.COINS, 400_000_000_000L),

    FELT_CLASSIC(Kind.FELT, null, Unlock.DEFAULT, 0),
    FELT_NEON(Kind.FELT, "neon", Unlock.ACHIEVEMENTS, 20),
    FELT_ABYSS(Kind.FELT, "abysses", Unlock.BESTIARY, 50),
    FELT_WOOD(Kind.FELT, "bois", Unlock.COINS, 20_000_000_000L),
    FELT_GOLD(Kind.FELT, "or", Unlock.COINS, 600_000_000_000L),

    MACHINE_CLASSIC(Kind.MACHINE, null, Unlock.DEFAULT, 0),
    MACHINE_NEON(Kind.MACHINE, "neon", Unlock.ACHIEVEMENTS, 30),
    MACHINE_ABYSS(Kind.MACHINE, "abysses", Unlock.BESTIARY, Unlock.ALL),
    MACHINE_WOOD(Kind.MACHINE, "bois", Unlock.COINS, 40_000_000_000L),
    MACHINE_GOLD(Kind.MACHINE, "or", Unlock.COINS, 900_000_000_000L);

    /** Sorte de cosmétique : ce qu'il habille. */
    public enum Kind {
        BACK("Dos de cartes"),
        FELT("Tapis de table"),
        MACHINE("Machine");

        private final String label;

        Kind(String label) { this.label = label; }

        /** @return le nom de la sorte, dans la langue choisie. */
        public String getLabel() { return Lang.t(label); }
    }

    /** Façon de débloquer un cosmétique. */
    public enum Unlock {
        /** Possédé dès le départ. */
        DEFAULT,
        /** Un nombre de succès atteints. */
        ACHIEVEMENTS,
        /** Un nombre d'ennemis du bestiaire vaincus ({@link #ALL} : tous). */
        BESTIARY,
        /** Acheté avec des pièces. */
        COINS;

        /** Objectif du bestiaire : tous ses ennemis. */
        public static final long ALL = -1;
    }

    private final Kind   kind;
    private final String style;
    private final Unlock unlock;
    /** Nombre de succès ou d'ennemis à atteindre, ou prix en pièces, selon {@link #unlock}. */
    private final long   goal;

    Cosmetic(Kind kind, String style, Unlock unlock, long goal) {
        this.kind   = kind;
        this.style  = style;
        this.unlock = unlock;
        this.goal   = goal;
    }

    public Kind getKind() { return kind; }

    public Unlock getUnlock() { return unlock; }

    /** @return le nombre de succès ou d'ennemis à atteindre ({@link Unlock#ALL} : tous), ou le prix en pièces. */
    public long getGoal() { return goal; }

    /** @return {@code true} pour le cosmétique d'origine de sa sorte. */
    public boolean isDefault() { return unlock == Unlock.DEFAULT; }

    /** @return le cosmétique d'origine de la sorte {@code kind}. */
    public static Cosmetic defaultOf(Kind kind) {
        for (Cosmetic cosmetic : values()) if (cosmetic.kind == kind && cosmetic.isDefault()) return cosmetic;
        throw new IllegalArgumentException(kind.name());
    }

    /** @return le nom du style, dans la langue choisie (ex : « Néon »). */
    public String getName() {
        if (style == null) return Lang.t("Classique");
        return Lang.t(switch (style) {
            case "neon"    -> "Néon";
            case "abysses" -> "Abysses";
            case "bois"    -> "Bois";
            case "or"      -> "Or";
            default        -> style;
        });
    }

    /** @return ce que le cosmétique habille, dans la langue choisie. */
    public String getDescription() {
        if (isDefault()) return switch (kind) {
            case BACK    -> Lang.t("Le dos de cartes d'origine.");
            case FELT    -> Lang.t("Le tapis d'origine : chaque lieu garde le sien.");
            case MACHINE -> Lang.t("La machine d'origine : chaque lieu garde la sienne.");
        };
        return switch (kind) {
            case BACK    -> Lang.f("Le dos de tes cartes, style {0}.", getName());
            case FELT    -> Lang.f("Le tapis de la table de combat, style {0}, dans tous les lieux.", getName());
            case MACHINE -> Lang.f("Ta machine à sous, style {0}, dans tous les lieux.", getName());
        };
    }

    /** @return l'image du dos de cartes, {@code null} pour celui d'origine (ou si ce n'est pas un dos). */
    public String getBackPath() {
        return kind == Kind.BACK && style != null ? folder() + "back.png" : null;
    }

    /** @return l'image du feutre, {@code null} pour celui du lieu (ou si ce n'est pas un tapis). */
    public String getFeltPath() {
        return kind == Kind.FELT && style != null ? folder() + "felt.png" : null;
    }

    /** @return le dossier de la carrosserie et des fenêtres de la machine, {@code null} pour celles du lieu. */
    public String getMachineFolder() {
        return kind == Kind.MACHINE && style != null ? folder() : null;
    }

    /** @return le dossier des images de son style (aperçu de la boutique), {@code null} pour l'origine. */
    public String getStyleFolder() {
        return style == null ? null : folder();
    }

    private String folder() {
        return "cosmetics/" + style + "/";
    }
}
