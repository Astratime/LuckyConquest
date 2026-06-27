package fr.astratime.lucky.entities;

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

    Symbol(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetPath() {
        return "symbols/" + assetName + ".png";
    }

    /**
     * Retourne true si ce symbole compte comme une attaque.
     * Les symboles d'attaque infligent des dégâts à l'ennemi après le spin.
     */
    public boolean isAttack() {
        switch (this) {
            case DOUBLE_BAR:
            case CHERRY:
            case SEVEN:
            case BAR:
                return true;
            default:
                return false;
        }
    }
}
