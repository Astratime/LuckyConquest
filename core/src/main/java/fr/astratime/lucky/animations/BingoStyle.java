package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.entities.Symbol;

import static fr.astratime.lucky.assets.Palette.hex;

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
            case SEVEN -> new BingoStyle(hex("7a0f12"), Palette.ORANGE, hex("ffb347"), Palette.GOLD_PALE, Palette.BANNER_SHADOW,
                hex("ff5a1f"), hex("ffe0a0"), hex("ff6a2a"), palette(hex("ff3b1f"), Palette.ORANGE, Palette.GOLD, Color.WHITE));
            case DOUBLE_BAR -> new BingoStyle(hex("3d4a5c"), Palette.STEEL_LIGHT, hex("e8eef5"), hex("8fd3ff"), back("0b0f14"),
                hex("8fd3ff"), Color.WHITE, hex("dfe8f0"), null);
            case BAR -> new BingoStyle(hex("1d1d24"), hex("f2f2f2"), Color.WHITE, Palette.RUBY, back("5a0d12"),
                Color.WHITE, Color.WHITE, Color.WHITE, null);
            case CHERRY -> new BingoStyle(hex("c2185b"), hex("ffd1e0"), Color.WHITE, hex("ffc1d6"), back("3a0614"),
                hex("ff7ab0"), hex("fff0f6"), hex("ff8fb8"), palette(hex("ff4a7a"), hex("ff8fb8"), Color.WHITE, hex("ff2050")));
            case TRIPLE_CHERRY -> new BingoStyle(hex("a3001e"), hex("ff4a7a"), hex("ffe3ec"), hex("ff9ab8"), back("22020a"),
                hex("ff3060"), hex("ffe0e8"), hex("ff4060"), palette(hex("ff2050"), hex("ff4a7a"), hex("ffb0c8"), Color.WHITE));
            case GRAPE -> new BingoStyle(hex("4a1a7a"), hex("b388ff"), hex("e6d6ff"), Palette.VIOLET, back("14061f"),
                Palette.VIOLET, hex("f3e8ff"), hex("a070ff"), palette(hex("9b4dff"), Palette.VIOLET, hex("e6d6ff")));
            case BELL -> new BingoStyle(hex("8a5a00"), Palette.GOLD, hex("fff7d6"), Palette.GOLD, back("1f1200"),
                hex("ffe680"), Color.WHITE, hex("fff0b0"), palette(Palette.GOLD, hex("fff7d6")));
            case DIAMOND -> new BingoStyle(hex("0d4f6b"), hex("7df9ff"), Color.WHITE, hex("b3f5ff"), back("021520"),
                hex("7df9ff"), Color.WHITE, hex("c8fbff"), palette(hex("7df9ff"), hex("b3f5ff"), Color.WHITE, Palette.SKY));
            case GOLD_BAR -> new BingoStyle(hex("a06a00"), hex("ffe066"), Color.WHITE, hex("ffe066"), back("1a1000"),
                Palette.GOLD, hex("fffbe0"), hex("ffe066"), palette(Palette.GOLD, hex("ffe066"), Color.WHITE));
            case WATERMELON -> new BingoStyle(hex("1f7a2e"), hex("ff5a78"), hex("ff8fa3"), hex("eaffea"), back("0a200e"),
                hex("7dff9a"), hex("f0fff0"), hex("7dff9a"), palette(Palette.MINT, hex("ff5a78"), hex("ff8fa3"), hex("eaffea")));
            // Rouleaux de la boutique : les couleurs d'un classique de leur type
            case HORSESHOE -> of(Symbol.BELL);
            case CROWN, NUGGET -> of(Symbol.GOLD_BAR);
            case ECU       -> of(Symbol.DIAMOND);
            case HEART     -> of(Symbol.CHERRY);
            case SWORD, DIE -> of(Symbol.SEVEN);
            // Étoile : nuit bleue et or, feu d'artifice rouge, bleu et or.
            case STAR -> new BingoStyle(hex("1a2a6a"), Palette.GOLD, Palette.GOLD, Color.WHITE, back("060a1f"),
                hex("8fd3ff"), Color.WHITE, hex("fff0b0"), palette(hex("ff3040"), hex("3a7bff"), Palette.GOLD));
            case BOMB      -> of(Symbol.TRIPLE_CHERRY);
            // Triple Sept (et Joker, qui ne s'aligne jamais seul) : la célébration d'origine.
            case TRIPLE_SEVEN, JOKER -> new BingoStyle(null, null, Palette.GOLD, Color.WHITE, Palette.BANNER_SHADOW,
                hex("ffcc26"), hex("ffffd9"), Color.WHITE, null);
        };
    }


    /** Bande de travers : légèrement transparente, comme celle d'origine. */
    private static Color back(String hex) { return Color.valueOf(hex + "eb"); }

    private static Color[] palette(Color... colors) {
        return colors;
    }
}
