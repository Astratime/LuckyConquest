package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import fr.astratime.lucky.entities.Symbol;

/**
 * Couleurs de la célébration d'un Bingo selon le symbole aligné : bannière
 * (corps, galons, lettres, bande de travers), éclairs, flash de l'écran et
 * gerbes des feux d'artifice. Le Triple Sept garde la bannière rouge et or
 * d'origine ({@link #body} nul).
 *
 * @param body      corps de la bannière ; {@code null} pour la bande rouge et or d'origine
 * @param trim      galons de la bannière (ignoré si {@code body} est nul)
 * @param letterA   lettres de rang pair
 * @param letterB   lettres de rang impair
 * @param back      bande de travers, derrière la bannière
 * @param boltGlow  halo des éclairs
 * @param boltCore  cœur des éclairs
 * @param flash     flash de l'écran au début de la célébration
 * @param fireworks couleurs des gerbes ; {@code null} pour les couleurs par défaut
 */
public record BingoStyle(Color body, Color trim, Color letterA, Color letterB, Color back,
                         Color boltGlow, Color boltCore, Color flash, Color[] fireworks) {

    private static final Color DEFAULT_BACK = new Color(0.06f, 0.02f, 0.03f, 0.92f);

    /** @return le style de la célébration d'un Bingo de {@code symbol}. */
    public static BingoStyle of(Symbol symbol) {
        return switch (symbol) {
            case SEVEN -> new BingoStyle(c("7a0f12"), c("ff8a1f"), c("ffb347"), c("fff2c0"), DEFAULT_BACK,
                c("ff5a1f"), c("ffe0a0"), c("ff6a2a"), palette("ff3b1f", "ff8a1f", "ffd54a", "ffffff"));
            case DOUBLE_BAR -> new BingoStyle(c("3d4a5c"), c("c8d2dc"), c("e8eef5"), c("8fd3ff"), back("0b0f14"),
                c("8fd3ff"), c("ffffff"), c("dfe8f0"), null);
            case BAR -> new BingoStyle(c("1d1d24"), c("f2f2f2"), c("ffffff"), c("ff4a5a"), back("5a0d12"),
                c("ffffff"), c("ffffff"), c("ffffff"), null);
            case CHERRY -> new BingoStyle(c("c2185b"), c("ffd1e0"), c("ffffff"), c("ffc1d6"), back("3a0614"),
                c("ff7ab0"), c("fff0f6"), c("ff8fb8"), palette("ff4a7a", "ff8fb8", "ffffff", "ff2050"));
            case TRIPLE_CHERRY -> new BingoStyle(c("a3001e"), c("ff4a7a"), c("ffe3ec"), c("ff9ab8"), back("22020a"),
                c("ff3060"), c("ffe0e8"), c("ff4060"), palette("ff2050", "ff4a7a", "ffb0c8", "ffffff"));
            case GRAPE -> new BingoStyle(c("4a1a7a"), c("b388ff"), c("e6d6ff"), c("c77dff"), back("14061f"),
                c("c77dff"), c("f3e8ff"), c("a070ff"), palette("9b4dff", "c77dff", "e6d6ff"));
            case BELL -> new BingoStyle(c("8a5a00"), c("ffd54a"), c("fff7d6"), c("ffd54a"), back("1f1200"),
                c("ffe680"), c("ffffff"), c("fff0b0"), palette("ffd54a", "fff7d6"));
            case DIAMOND -> new BingoStyle(c("0d4f6b"), c("7df9ff"), c("ffffff"), c("b3f5ff"), back("021520"),
                c("7df9ff"), c("ffffff"), c("c8fbff"), palette("7df9ff", "b3f5ff", "ffffff", "4ac8ff"));
            case GOLD_BAR -> new BingoStyle(c("a06a00"), c("ffe066"), c("ffffff"), c("ffe066"), back("1a1000"),
                c("ffd54a"), c("fffbe0"), c("ffe066"), palette("ffd54a", "ffe066", "ffffff"));
            case WATERMELON -> new BingoStyle(c("1f7a2e"), c("ff5a78"), c("ff8fa3"), c("eaffea"), back("0a200e"),
                c("7dff9a"), c("f0fff0"), c("7dff9a"), palette("4affa0", "ff5a78", "ff8fa3", "eaffea"));
            // Triple Sept (et Joker, qui ne s'aligne jamais seul) : la célébration d'origine.
            case TRIPLE_SEVEN, JOKER -> new BingoStyle(null, null, c("ffd54a"), Color.WHITE, DEFAULT_BACK,
                c("ffcc26"), c("ffffd9"), Color.WHITE, null);
        };
    }

    private static Color c(String hex) { return Color.valueOf(hex + "ff"); }

    /** Bande de travers : légèrement transparente, comme celle d'origine. */
    private static Color back(String hex) { return Color.valueOf(hex + "eb"); }

    private static Color[] palette(String... hexes) {
        Color[] colors = new Color[hexes.length];
        for (int i = 0; i < hexes.length; i++) colors[i] = c(hexes[i]);
        return colors;
    }
}
