package fr.astratime.lucky.assets;

import com.badlogic.gdx.graphics.Color;

/**
 * Couleurs partagées du jeu : la palette de base, en pixel art, puis les rôles
 * de l'interface construits sur elle. Une couleur qui sert à plusieurs endroits
 * vient d'ici ; une couleur propre à un seul objet (la chair d'une pastèque, le
 * bois d'un pressoir…) reste à côté du code qui le dessine.
 *
 * Comme {@link Color#WHITE}, ces constantes ne doivent jamais être modifiées :
 * pour la faire varier, travailler sur une copie ({@link Color#cpy()}).
 */
public final class Palette {

    // -------------------------------------------------------------------------
    // Palette de base
    // -------------------------------------------------------------------------

    /** Contour sombre des dessins en pixel art et des textes. */
    public static final Color OUTLINE = hex("1a0f0f");

    /** Or du casino : titres, jetons, reflets dorés. */
    public static final Color GOLD      = hex("ffd54a");
    /** Or pâle, presque blanc : halos, traînées lumineuses, cœur des objets chauffés. */
    public static final Color GOLD_PALE = hex("fff2c0");
    /** Crème des textes courants, sur les panneaux sombres. */
    public static final Color CREAM     = hex("f0e0b0");

    public static final Color RED    = hex("e0303c");
    public static final Color ORANGE = hex("ff8a1f");
    public static final Color VIOLET = hex("c77dff");

    // Couleurs vives de la fête (confettis, feux d'artifice).
    public static final Color RUBY = hex("ff4a5a");
    public static final Color MINT = hex("4affa0");
    public static final Color SKY  = hex("4ac8ff");

    // Métaux : acier bleuté et fer sombre.
    public static final Color STEEL_LIGHT = hex("c8d2dc");
    public static final Color STEEL       = hex("8a97a8");
    public static final Color STEEL_DARK  = hex("4a5566");
    public static final Color IRON        = hex("4a4a58");
    public static final Color IRON_DARK   = hex("3a3a44");

    // Bois, du plus clair au plus sombre.
    public static final Color WOOD_HIGHLIGHT = hex("dca466");
    public static final Color WOOD_LIGHT     = hex("b07a3c");
    public static final Color WOOD           = hex("8a5a2c");
    public static final Color WOOD_DARK      = hex("6e4524");

    // -------------------------------------------------------------------------
    // Rôles
    // -------------------------------------------------------------------------

    /** Contour des textes, pour qu'ils restent lisibles sur la table. */
    public static final Color TEXT_SHADE = OUTLINE;
    /** Titres et valeurs mises en avant (gains, noms de cartes, option choisie). */
    public static final Color TEXT_TITLE = GOLD;
    /** Textes courants des panneaux. */
    public static final Color TEXT_BODY  = CREAM;
    /** Refus et alertes (gains insuffisants, choix impossible). */
    public static final Color TEXT_ALERT = hex("ff5a5a");

    /** Voile noir posé sur le jeu derrière une fenêtre (échoppe, choix d'une carte). */
    public static final Color VEIL = new Color(0f, 0f, 0f, 0.72f);
    /** Bande sombre, légèrement transparente, derrière les bannières. */
    public static final Color BANNER_SHADOW = new Color(0.06f, 0.02f, 0.03f, 0.92f);

    /** Couleurs de la fête, tirées au hasard par les confettis et les feux d'artifice. */
    public static final Color[] FESTIVE = {GOLD, RUBY, MINT, SKY, VIOLET, Color.WHITE};

    /** @return la couleur {@code rrggbb}, opaque. */
    private static Color hex(String rrggbb) {
        return Color.valueOf(rrggbb);
    }

    private Palette() {}
}
