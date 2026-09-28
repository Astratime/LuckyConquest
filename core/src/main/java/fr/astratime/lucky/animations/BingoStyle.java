package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import fr.astratime.lucky.assets.Palette;
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

    /** @return le style de la célébration d'un Bingo de {@code symbol}. */
    public static BingoStyle of(Symbol symbol) {
        return switch (symbol) {
            case SEVEN -> new BingoStyle(c("7a0f12"), Palette.ORANGE, c("ffb347"), Palette.GOLD_PALE, Palette.BANNER_SHADOW,
                c("ff5a1f"), c("ffe0a0"), c("ff6a2a"), palette(c("ff3b1f"), Palette.ORANGE, Palette.GOLD, Color.WHITE));
            case DOUBLE_BAR -> new BingoStyle(c("3d4a5c"), Palette.STEEL_LIGHT, c("e8eef5"), c("8fd3ff"), back("0b0f14"),
                c("8fd3ff"), Color.WHITE, c("dfe8f0"), null);
            case BAR -> new BingoStyle(c("1d1d24"), c("f2f2f2"), Color.WHITE, Palette.RUBY, back("5a0d12"),
                Color.WHITE, Color.WHITE, Color.WHITE, null);
            case CHERRY -> new BingoStyle(c("c2185b"), c("ffd1e0"), Color.WHITE, c("ffc1d6"), back("3a0614"),
                c("ff7ab0"), c("fff0f6"), c("ff8fb8"), palette(c("ff4a7a"), c("ff8fb8"), Color.WHITE, c("ff2050")));
            case TRIPLE_CHERRY -> new BingoStyle(c("a3001e"), c("ff4a7a"), c("ffe3ec"), c("ff9ab8"), back("22020a"),
                c("ff3060"), c("ffe0e8"), c("ff4060"), palette(c("ff2050"), c("ff4a7a"), c("ffb0c8"), Color.WHITE));
            case GRAPE -> new BingoStyle(c("4a1a7a"), c("b388ff"), c("e6d6ff"), Palette.VIOLET, back("14061f"),
                Palette.VIOLET, c("f3e8ff"), c("a070ff"), palette(c("9b4dff"), Palette.VIOLET, c("e6d6ff")));
            case BELL -> new BingoStyle(c("8a5a00"), Palette.GOLD, c("fff7d6"), Palette.GOLD, back("1f1200"),
                c("ffe680"), Color.WHITE, c("fff0b0"), palette(Palette.GOLD, c("fff7d6")));
            case DIAMOND -> new BingoStyle(c("0d4f6b"), c("7df9ff"), Color.WHITE, c("b3f5ff"), back("021520"),
                c("7df9ff"), Color.WHITE, c("c8fbff"), palette(c("7df9ff"), c("b3f5ff"), Color.WHITE, Palette.SKY));
            case GOLD_BAR -> new BingoStyle(c("a06a00"), c("ffe066"), Color.WHITE, c("ffe066"), back("1a1000"),
                Palette.GOLD, c("fffbe0"), c("ffe066"), palette(Palette.GOLD, c("ffe066"), Color.WHITE));
            case WATERMELON -> new BingoStyle(c("1f7a2e"), c("ff5a78"), c("ff8fa3"), c("eaffea"), back("0a200e"),
                c("7dff9a"), c("f0fff0"), c("7dff9a"), palette(Palette.MINT, c("ff5a78"), c("ff8fa3"), c("eaffea")));
            // Triple Sept (et Joker, qui ne s'aligne jamais seul) : la célébration d'origine.
            case TRIPLE_SEVEN, JOKER -> new BingoStyle(null, null, Palette.GOLD, Color.WHITE, Palette.BANNER_SHADOW,
                c("ffcc26"), c("ffffd9"), Color.WHITE, null);
        };
    }

    private static Color c(String hex) { return Color.valueOf(hex + "ff"); }

    /** Bande de travers : légèrement transparente, comme celle d'origine. */
    private static Color back(String hex) { return Color.valueOf(hex + "eb"); }

    private static Color[] palette(Color... colors) {
        return colors;
    }
}
