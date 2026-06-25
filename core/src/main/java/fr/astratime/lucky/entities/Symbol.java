package fr.astratime.lucky.entities;

public enum Symbol {

    DOUBLE_BAR ("1-double_bar"),
    CHERRY     ("2-cherry"),
    SEVEN      ("3-seven"),
    BAR        ("4-bar"),
    GRAPE      ("5-grape"),
    BELL       ("6-bell");

    private final String assetName;

    Symbol(String assetName) {
        this.assetName = assetName;
    }

    /** Chemin relatif au dossier assets de l'image représentant ce symbole. */
    public String getAssetPath() {
        return "symbols/" + assetName + ".png";
    }
}
