package fr.astratime.lucky.entities;

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
    WATERMELON    ("11-watermelon");

    private final String assetName;

    /** @param assetName nom de fichier (sans extension ni dossier) de la texture du symbole */
    Symbol(String assetName) {
        this.assetName = assetName;
    }

    /** @return le chemin de la texture du symbole, relatif au dossier assets. */
    public String getAssetPath() {
        return "symbols/" + assetName + ".png";
    }
}
