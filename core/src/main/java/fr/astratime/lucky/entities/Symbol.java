package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.actions.Action;

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

    /** @return le chemin de la texture du symbole, relatif au dossier assets. */
    public String getAssetPath() {
        return "symbols/" + assetName + ".png";
    }

    /** @return le nom du symbole tel qu'affiché au joueur (ex : "CLOCHE"). */
    public String getDisplayName() {
        return switch (this) {
            case DOUBLE_BAR    -> "DOUBLE BAR";
            case CHERRY        -> "CERISE";
            case SEVEN         -> "SEPT";
            case BAR           -> "BAR";
            case GRAPE         -> "RAISIN";
            case BELL          -> "CLOCHE";
            case DIAMOND       -> "DIAMANT";
            case TRIPLE_CHERRY -> "TRIPLE CERISE";
            case TRIPLE_SEVEN  -> "TRIPLE SEPT";
            case GOLD_BAR      -> "LINGOT";
            case WATERMELON    -> "PASTEQUE";
            case HORSESHOE     -> "FER À CHEVAL";
            case ECU           -> "ÉCU";
            case SWORD         -> "ÉPÉE";
            case HEART         -> "COEUR";
            case DIE           -> "DÉ";
            case STAR          -> "ÉTOILE";
            case BOMB          -> "BOMBE";
            case CROWN         -> "COURONNE";
            case NUGGET        -> "PÉPITE";
            case JOKER         -> "JOKER";
        };
    }

    /** @return la description de l'effet de ce symbole (voir SymbolRegistry), affichée en infobulle. */
    public String getDescription() {
        if (this == JOKER) return "Joker : compte comme n'importe quel symbole";
        return SymbolRegistry.getAction(this)
            .map(Action::getDescription)
            .orElse("Aucun effet");
    }
}
