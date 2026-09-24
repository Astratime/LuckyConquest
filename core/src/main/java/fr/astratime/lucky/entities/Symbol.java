package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.actions.Action;

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
    /** Joker : compte comme n'importe quel symbole (voir SlotMachine#resolveJokers). */
    JOKER         ("12-joker");

    private final String assetName;

    /** @param assetName nom de fichier (sans extension ni dossier) de la texture du symbole */
    Symbol(String assetName) {
        this.assetName = assetName;
    }

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
