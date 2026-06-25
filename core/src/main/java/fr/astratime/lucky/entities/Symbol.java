package fr.astratime.lucky.entities;

public enum Symbol {

    DOUBLE_BAR ("1-double_bar"),
    CHERRY     ("2-cherry"),
    SEVEN      ("3-seven"),
    BAR        ("4-bar"),
    GRAPE      ("5-grape"),
    BELL       ("6-bell"),
    DIAMOND ("7-diamond"),
    TRIPLE_CHERRY     ("8-triple_cherry"),
    TRIPLE_SEVEN      ("9-triple_seven"),
    GOLD_BAR        ("10-gold_bar"),
    WATERMELON      ("11-watermelon");

    private final String assetName;

    Symbol(String assetName) {
        this.assetName = assetName;
    }

    /** Chemin relatif au dossier assets de l'image représentant ce symbole. */
    public String getAssetPath() {
        return "symbols/" + assetName + ".png";
    }
}
