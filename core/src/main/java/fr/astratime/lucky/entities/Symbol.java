package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.actions.Action;
import fr.astratime.lucky.i18n.Lang;

import java.util.List;

/**
 * Identité et asset d'un symbole de machine à sous.
 * Le comportement associé (ce que fait le symbole) est défini dans SymbolRegistry.
 */
public enum Symbol {

    DOUBLE_BAR    ("1-double_bar"),
    CHERRY        ("2-cherry"),
    SEVEN         ("3-seven"),
    BAR           ("4-bar"),
    GRAPE         ("5-grape"),
    BELL          ("6-bell"),
    DIAMOND       ("7-diamond"),
    TRIPLE_CHERRY ("8-triple_cherry"),
    TRIPLE_SEVEN  ("9-triple_seven"),
    GOLD_BAR      ("10-gold_bar"),
    WATERMELON    ("11-watermelon"),
    // Rouleaux achetés à la boutique (voir fr.astratime.lucky.progress.ReelShop)
    HORSESHOE     ("13-horseshoe"),
    ECU           ("14-ecu"),
    SWORD         ("15-sword"),
    HEART         ("16-heart"),
    DIE           ("17-die"),
    STAR          ("18-star"),
    BOMB          ("19-bomb"),
    CROWN         ("20-crown"),
    /** Rouleau de la Mine : gagné en vidant les quatre donjons des Mines d'Or (voir Place#getReelReward). */
    NUGGET        ("21-nugget"),
    // Nouveaux rouleaux de la boutique (2026-11-01)
    FANGS         ("22-fangs"),
    PIGGY_BANK    ("23-piggy_bank"),
    SPIKED_SHIELD ("24-spiked_shield"),
    SCYTHE        ("25-scythe"),
    MOON          ("26-moon"),
    /** Roi démon : sa Démonisation, pleine, impose un Bingo de Descente aux enfers. */
    DEMON_KING    ("27-demon_king"),
    /** Apôtre de la Chance : son Ange chanceux, plein, impose un Bingo de Lumière du bingo. */
    LUCK_APOSTLE  ("28-luck_apostle"),
    // Rouleaux gagnés en vidant un lieu (voir Place#getReelReward)
    TICKET        ("29-ticket"),
    ANCHOR        ("30-anchor"),
    GOLD_CHIP     ("31-gold_chip"),
    FLAME         ("32-flame"),
    /** N'existe qu'au Bingo imposé par le Roi démon : jamais dans une machine. */
    DESCENT       ("33-descent"),
    /** N'existe qu'au Bingo imposé par l'Apôtre de la Chance : jamais dans une machine. */
    BINGO_LIGHT   ("34-bingo_light"),
    /** Joker : compte comme n'importe quel symbole (voir SlotMachine#resolveJokers). */
    JOKER         ("12-joker");

    /** Nombre de rouleaux de la machine du joueur, ni plus ni moins (le Joker est à part). */
    public static final int MACHINE_SIZE = 11;

    /** Les 11 rouleaux classiques : la machine de départ du joueur. */
    private static final List<Symbol> CLASSIC = List.of(DOUBLE_BAR, CHERRY, SEVEN, BAR, GRAPE, BELL, DIAMOND,
        TRIPLE_CHERRY, TRIPLE_SEVEN, GOLD_BAR, WATERMELON);

    private final String assetName;

    /** @param assetName nom de fichier (sans extension ni dossier) de la texture du symbole */
    Symbol(String assetName) {
        this.assetName = assetName;
    }

    /** @return les 11 rouleaux classiques, dans l'ordre : la machine de départ du joueur. */
    public static List<Symbol> classicReels() { return CLASSIC; }

    /** @return {@code true} pour un des 11 rouleaux classiques (les autres s'achètent à la boutique). */
    public boolean isClassic() { return CLASSIC.contains(this); }

    /** @return {@code true} pour Descente aux enfers et Lumière du bingo, qui ne sortent qu'au Bingo d'une jauge pleine. */
    public boolean isSummoned() { return this == DESCENT || this == BINGO_LIGHT; }

    /**
     * Rareté d'un symbole, comme sur la table de gains d'une vraie machine : ce
     * que rapporte son Bingo, en coûts de tirage (voir SpinEconomy).
     */
    public enum Rarity {
        /** Cerise, BAR, Raisin, Cloche. */
        COMMON(20),
        /** Sept, Double BAR, Pastèque, Diamant. */
        MEDIUM(30),
        /** Triple Cerise, Triple Sept, Lingot, et tous les rouleaux de la boutique et de la Mine. */
        RARE(50);

        /** Gains d'un Bingo de ce symbole, en coûts de tirage. */
        public final int jackpotSpins;

        Rarity(int jackpotSpins) { this.jackpotSpins = jackpotSpins; }
    }

    /** @return la rareté du symbole (table de gains des Bingos). */
    public Rarity getRarity() {
        return switch (this) {
            case CHERRY, BAR, GRAPE, BELL                -> Rarity.COMMON;
            case SEVEN, DOUBLE_BAR, WATERMELON, DIAMOND  -> Rarity.MEDIUM;
            default                                      -> Rarity.RARE;
        };
    }

    /** @return le chemin de la texture du symbole, relatif au dossier assets. */
    public String getAssetPath() {
        return "symbols/" + assetName + ".png";
    }

    /** @return le nom du symbole tel qu'affiché au joueur (ex : "CLOCHE"). */
    public String getDisplayName() {
        return switch (this) {
            case DOUBLE_BAR    -> Lang.t("DOUBLE BAR");
            case CHERRY        -> Lang.t("CERISE");
            case SEVEN         -> Lang.t("SEPT");
            case BAR           -> Lang.t("BAR");
            case GRAPE         -> Lang.t("RAISIN");
            case BELL          -> Lang.t("CLOCHE");
            case DIAMOND       -> Lang.t("DIAMANT");
            case TRIPLE_CHERRY -> Lang.t("TRIPLE CERISE");
            case TRIPLE_SEVEN  -> Lang.t("TRIPLE SEPT");
            case GOLD_BAR      -> Lang.t("LINGOT");
            case WATERMELON    -> Lang.t("PASTEQUE");
            case HORSESHOE     -> Lang.t("FER À CHEVAL");
            case ECU           -> Lang.t("ÉCU");
            case SWORD         -> Lang.t("ÉPÉE");
            case HEART         -> Lang.t("COEUR");
            case DIE           -> Lang.t("DÉ");
            case STAR          -> Lang.t("ÉTOILE");
            case BOMB          -> Lang.t("BOMBE");
            case CROWN         -> Lang.t("COURONNE");
            case NUGGET        -> Lang.t("PÉPITE");
            case FANGS         -> Lang.t("CROCS");
            case PIGGY_BANK    -> Lang.t("TIRELIRE");
            case SPIKED_SHIELD -> Lang.t("BOUCLIER CLOUTÉ");
            case SCYTHE        -> Lang.t("FAUX");
            case MOON          -> Lang.t("LUNE");
            case DEMON_KING    -> Lang.t("ROI DÉMON");
            case LUCK_APOSTLE  -> Lang.t("APÔTRE DE LA CHANCE");
            case TICKET        -> Lang.t("TICKET");
            case ANCHOR        -> Lang.t("ANCRE");
            case GOLD_CHIP     -> Lang.t("JETON D'OR");
            case FLAME         -> Lang.t("FLAMME");
            case DESCENT       -> Lang.t("DESCENTE AUX ENFERS");
            case BINGO_LIGHT   -> Lang.t("LUMIÈRE DU BINGO");
            case JOKER         -> Lang.t("JOKER");
        };
    }

    /** @return la description de l'effet de ce symbole (voir SymbolRegistry), affichée en infobulle. */
    public String getDescription() {
        if (this == JOKER) return Lang.t("Joker : compte comme n'importe quel symbole");
        return SymbolRegistry.getAction(this)
            .map(Action::getDescription)
            .orElse(Lang.t("Aucun effet"));
    }
}
