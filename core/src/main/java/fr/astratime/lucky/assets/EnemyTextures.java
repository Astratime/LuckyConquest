package fr.astratime.lucky.assets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.tower.Chapter;

import java.util.EnumMap;
import java.util.Map;

/**
 * Textures des ennemis : le portrait de chacun (le croupier démoniaque et ceux
 * de la Tour des épreuves, voir {@link EnemyPortraits}) et sa silhouette
 * blanche, pour le faire flasher quand il est touché, les symboles de leurs
 * machines à sous, dessinés en pixel art, l'illustration de chaque chapitre, et
 * leurs cartes (jeu sombre, chargées à la demande).
 */
public class EnemyTextures implements Disposable {

    /** Agrandissement des pixels du croupier et des symboles. */
    public static final int CROUPIER_SCALE = 5;
    public static final int SYMBOL_SCALE   = 4;

    private static final int OUTLINE = Color.rgba8888(Palette.OUTLINE);

    public final Texture croupier;
    /** Silhouette blanche du croupier, posée par-dessus quand il est touché. */
    public final Texture croupierFlash;
    /** Dos des cartes de l'ennemi. */
    public final Texture cardBack;
    /** Illustrations des chapitres de la Tour des épreuves. */
    private final Map<Chapter, Texture>     chapterArt = new EnumMap<>(Chapter.class);
    private final Map<EnemySymbol, Texture> symbols = new EnumMap<>(EnemySymbol.class);
    private final Map<EnemyKind, Texture>   portraits = new EnumMap<>(EnemyKind.class);
    private final Map<EnemyKind, Texture>   flashes   = new EnumMap<>(EnemyKind.class);
    private final CardTextures              cards;
    /** Exploration : coffre au trésor (fermé, ouvert), carte de la prairie et entrée de chaque donjon. */
    public final Texture                    chestClosed;
    public final Texture                    chestOpen;
    public final Texture                    prairie;
    private final Map<Card.Suit, Texture>   gates = new EnumMap<>(Card.Suit.class);

    /** @param cards cache des images de cartes, partagé avec l'écran de jeu (non possédé) */
    public EnemyTextures(CardTextures cards) {
        this.cards = cards;
        cardBack   = cards.get("cards/dark/BACK.png");
        int[][] croupierGrid = croupierGrid();
        croupier      = texture(croupierGrid, false);
        croupierFlash = texture(croupierGrid, true);
        symbols.put(EnemySymbol.SWORD,  texture(swordGrid(), false));
        symbols.put(EnemySymbol.SHIELD, texture(shieldGrid(), false));
        symbols.put(EnemySymbol.POTION, texture(potionGrid(), false));
        symbols.put(EnemySymbol.THORNS, texture(EnemyPortraits.thorns(), false));
        symbols.put(EnemySymbol.FANG,   texture(EnemyPortraits.fang(), false));
        symbols.put(EnemySymbol.RAGE,   texture(EnemyPortraits.rage(), false));
        symbols.put(EnemySymbol.LOADED_DIE, texture(Chapter2Art.loadedDie(), false));
        symbols.put(EnemySymbol.INTEREST,   texture(Chapter2Art.interest(), false));
        symbols.put(EnemySymbol.ZERO,       texture(Chapter2Art.zero(), false));
        symbols.put(EnemySymbol.MIRROR,     texture(Chapter3Art.mirror(), false));
        symbols.put(EnemySymbol.HOURGLASS,  texture(Chapter3Art.hourglass(), false));
        symbols.put(EnemySymbol.ALL_IN,     texture(Chapter3Art.allIn(), false));
        portraits.put(EnemyKind.CROUPIER, croupier);
        flashes.put(EnemyKind.CROUPIER, croupierFlash);
        addPortrait(EnemyKind.GARDIEN, EnemyPortraits.gardien());
        addPortrait(EnemyKind.SANGSUE, EnemyPortraits.sangsue());
        addPortrait(EnemyKind.BRETTEUR, EnemyPortraits.bretteur());
        addPortrait(EnemyKind.COMETE, EnemyPortraits.comete());
        addPortrait(EnemyKind.CHEF, Chapter2Art.chef());
        addPortrait(EnemyKind.TRICHEUR, Chapter2Art.tricheur());
        addPortrait(EnemyKind.USURIER, Chapter2Art.usurier());
        addPortrait(EnemyKind.ROULETTE, Chapter2Art.roulette());
        addPortrait(EnemyKind.REINE, Chapter2Art.reine());
        addPortrait(EnemyKind.GARDIENNE, Chapter3Art.gardienne());
        addPortrait(EnemyKind.MIROIR, Chapter3Art.miroir());
        addPortrait(EnemyKind.HORLOGER, Chapter3Art.horloger());
        addPortrait(EnemyKind.FOU, Chapter3Art.fou());
        addPortrait(EnemyKind.ECLAT, Chapter3Art.eclat());
        addPortrait(EnemyKind.SOLDAT_PIQUE, ExplorationArt.soldier(Card.Suit.PIQUE));
        addPortrait(EnemyKind.ROI_PIQUE, ExplorationArt.king(Card.Suit.PIQUE));
        addPortrait(EnemyKind.SOLDAT_TREFLE, ExplorationArt.soldier(Card.Suit.TREFLE));
        addPortrait(EnemyKind.ROI_TREFLE, ExplorationArt.king(Card.Suit.TREFLE));
        addPortrait(EnemyKind.SOLDAT_COEUR, ExplorationArt.soldier(Card.Suit.COEUR));
        addPortrait(EnemyKind.ROI_COEUR, ExplorationArt.king(Card.Suit.COEUR));
        addPortrait(EnemyKind.SOLDAT_CARREAU, ExplorationArt.soldier(Card.Suit.CARREAU));
        addPortrait(EnemyKind.ROI_CARREAU, ExplorationArt.king(Card.Suit.CARREAU));
        chestClosed = texture(ExplorationArt.chestClosed(), false);
        chestOpen   = texture(ExplorationArt.chestOpen(), false);
        prairie     = texture(ExplorationArt.prairie(), false);
        for (Card.Suit suit : Card.Suit.values()) gates.put(suit, texture(ExplorationArt.gate(suit), false));
        chapterArt.put(Chapter.GENESE, texture(EnemyPortraits.genesis(), false));
        chapterArt.put(Chapter.TABLES_SACREES, texture(Chapter2Art.tables(), false));
        chapterArt.put(Chapter.DERNIER_TIRAGE, texture(Chapter3Art.crater(), false));
    }

    private void addPortrait(EnemyKind kind, int[][] grid) {
        portraits.put(kind, texture(grid, false));
        flashes.put(kind, texture(grid, true));
    }

    /** @return la texture du symbole {@code symbol} (16 x 16 pixels). */
    public Texture symbol(EnemySymbol symbol) { return symbols.get(symbol); }

    /** @return le portrait de l'ennemi {@code kind}. */
    public Texture portrait(EnemyKind kind) { return portraits.get(kind); }

    /** @return la silhouette blanche du portrait de l'ennemi {@code kind}. */
    public Texture portraitFlash(EnemyKind kind) { return flashes.get(kind); }

    /** @return l'entrée du donjon de la couleur {@code suit}, sur la carte de l'Exploration. */
    public Texture gate(Card.Suit suit) { return gates.get(suit); }

    /** @return l'illustration du chapitre {@code chapter}. */
    public Texture chapterArt(Chapter chapter) { return chapterArt.get(chapter); }

    /** @return la face de la carte sombre {@code card} (chargée une fois, puis gardée). */
    public Texture card(Card card) {
        return cards.get(card);
    }

    // -------------------------------------------------------------------------
    // Dessins (grilles de couleurs RGBA, 0 = transparent), contour ajouté ensuite
    // -------------------------------------------------------------------------

    /**
     * Croupier démoniaque, en buste : haut-de-forme au ruban rouge percé de
     * deux cornes, peau pourpre, yeux dorés, sourire en coin plein de dents,
     * nœud papillon, gilet sombre sur chemise blanche, et un éventail de cartes
     * contre le gilet.
     */
    static int[][] croupierGrid() {
        int w = 34, h = 42;
        int[][] g = new int[h][w];
        int hat = c("2a2233"), hatLight = c("4a3f58"), band = c("c0283a");
        int horn = c("efe2c4"), hornShade = c("b8a482");
        int skin = c("b8506a"), skinShade = c("86324c");
        int eye = c("ffd54a"), eyeCore = c("fff6c8");
        int mouth = c("3a0c14"), teeth = c("f4efe4");
        int vest = c("2a1e38"), vestLight = c("46385a");
        int shirt = c("ece6da"), shirtShade = c("c8c0b4");
        int tie = c("d8202c"), tieKnot = c("8a1018"), gold = c("ffd54a"), goldDark = c("c4891e");
        int card = c("f6f0e2"), red = c("d8202c");

        // Buste : épaules arrondies, gilet sombre.
        for (int y = 27; y < h; y++) {
            for (int x = 3; x < w - 3; x++) {
                boolean corner = y < 31 && (Math.hypot(x - 7, y - 31) > 4.2 && x < 7
                    || Math.hypot(x - 26, y - 31) > 4.2 && x > 26);
                if (!corner) g[y][x] = x < 7 || x > 26 ? vestLight : vest;
            }
        }
        // Chemise en V, ombrée à droite.
        for (int y = 25; y < 38; y++) {
            float halfWidth = 5f - (y - 25) * 0.42f;
            for (int x = 0; x < w; x++) {
                if (Math.abs(x - 16.5f) <= halfWidth) g[y][x] = x > 17 ? shirtShade : shirt;
            }
        }
        // Chaîne de montre et boutons dorés.
        for (int x = 9; x <= 13; x++) g[33 + (x == 11 ? 1 : 0)][x] = x % 2 == 0 ? gold : goldDark;
        g[39][16] = gold;
        g[39][17] = goldDark;
        // Nœud papillon.
        for (int x = 13; x <= 20; x++) g[27][x] = tie;
        for (int x : new int[] {13, 14, 19, 20}) { g[26][x] = tie; g[28][x] = tie; }
        g[27][16] = tieKnot;
        g[27][17] = tieKnot;
        // Cou.
        for (int y = 23; y < 26; y++) for (int x = 14; x < 20; x++) g[y][x] = skinShade;
        // Tête ovale, ombrée à droite, et oreilles pointues.
        for (int y = 10; y < 25; y++) {
            for (int x = 0; x < w; x++) {
                double dx = (x - 16.5) / 6.8, dy = (y - 17.0) / 7.2;
                if (dx * dx + dy * dy <= 1) g[y][x] = x > 19 ? skinShade : skin;
            }
        }
        for (int y = 14; y < 18; y++) {
            g[y][9] = skin;
            g[y][24] = skinShade;
        }
        g[13][8] = skin;
        g[13][25] = skinShade;
        // Sourcils froncés, yeux dorés.
        for (int x = 11; x <= 14; x++) g[14 + (x > 12 ? 1 : 0)][x] = OUTLINE;
        for (int x = 19; x <= 22; x++) g[14 + (x < 21 ? 1 : 0)][x] = OUTLINE;
        for (int x = 12; x <= 14; x++) g[16][x] = eye;
        for (int x = 19; x <= 21; x++) g[16][x] = eye;
        g[16][13] = eyeCore;
        g[16][20] = eyeCore;
        // Nez.
        g[18][16] = skinShade;
        g[18][17] = skinShade;
        // Sourire plein de dents, deux crocs.
        for (int x = 12; x <= 21; x++) g[20][x] = mouth;
        g[19][11] = mouth;
        g[19][22] = mouth;
        for (int x = 13; x <= 20; x++) g[21][x] = teeth;
        g[21][12] = mouth;
        g[21][21] = mouth;
        for (int x = 13; x <= 20; x++) g[22][x] = mouth;
        g[22][14] = teeth;
        g[22][19] = teeth;
        // Haut-de-forme : calotte, reflet, ruban rouge, bord.
        for (int y = 1; y < 10; y++) for (int x = 10; x <= 23; x++) g[y][x] = hat;
        for (int y = 2; y < 7; y++) g[y][12] = hatLight;
        for (int x = 10; x <= 23; x++) { g[7][x] = band; g[8][x] = band; }
        for (int x = 6; x <= 27; x++) { g[9][x] = hat; g[10][x] = hat; }
        for (int x = 7; x <= 26; x++) g[9][x] = hatLight;
        // Cornes, qui percent le haut-de-forme de chaque côté.
        int[][] hornRows = {{8, 6, 8}, {7, 5, 7}, {6, 5, 6}, {5, 4, 5}, {4, 4, 4}, {3, 4, 4}};
        for (int[] row : hornRows) {
            for (int x = row[1]; x <= row[2]; x++) {
                g[row[0]][x] = x == row[2] ? hornShade : horn;
                g[row[0]][w - 1 - x] = x == row[2] ? hornShade : horn;
            }
        }
        // Éventail de cartes dans la main droite (à droite du gilet).
        int[][] fan = {{23, 30}, {25, 29}, {27, 30}};
        for (int[] pos : fan) {
            for (int y = pos[1]; y < pos[1] + 6; y++) for (int x = pos[0]; x < pos[0] + 4; x++) g[y][x] = card;
        }
        for (int[] pos : fan) {
            for (int y = pos[1]; y < pos[1] + 6; y++) g[y][pos[0]] = OUTLINE; // bord gauche de chaque carte
        }
        g[31][26] = red;
        g[32][28] = red;
        g[32][29] = red;
        return outlined(g);
    }

    /** Épée, lame en diagonale vers le haut à droite, garde et pommeau dorés. */
    private static int[][] swordGrid() {
        int[][] g = new int[16][16];
        int steel = c("dce4ee"), steelDark = c("8a97a8"), gold = c("ffd54a"), goldDark = c("c4891e"),
            grip = c("6e4524");
        for (int i = 0; i < 9; i++) {
            g[10 - i][5 + i] = steel;
            g[11 - i][5 + i] = steelDark;
        }
        g[1][14] = steel;
        for (int i = -2; i <= 2; i++) g[11 + i][5 + i] = i == 0 ? goldDark : gold;
        g[12][3] = grip;
        g[13][2] = grip;
        g[14][1] = gold;
        return outlined(g);
    }

    /** Bouclier d'acier au liseré doré, marqué d'une croix rouge. */
    private static int[][] shieldGrid() {
        int[][] g = new int[16][16];
        int steel = c("6a8cc8"), steelDark = c("3f5a8e"), rim = c("ffd54a"), red = c("d8202c");
        for (int y = 1; y < 15; y++) {
            int half = y < 9 ? 6 : 6 - (y - 8);
            for (int x = 8 - half; x < 8 + half; x++) {
                boolean edge = x == 8 - half || x == 7 + half || y == 1;
                g[y][x] = edge ? rim : (x >= 8 ? steelDark : steel);
            }
        }
        for (int y = 3; y < 11; y++) { g[y][7] = red; g[y][8] = red; }
        for (int x = 4; x < 12; x++) { g[5][x] = red; g[6][x] = red; }
        return outlined(g);
    }

    /** Fiole ronde de potion rouge, bouchon de liège, reflet. */
    private static int[][] potionGrid() {
        int[][] g = new int[16][16];
        int glass = c("c8e6f0"), potion = c("e83a5a"), potionDark = c("a01e3c"), cork = c("b07a3c"),
            shine = c("ffffff");
        for (int y = 5; y < 15; y++) {
            for (int x = 2; x < 14; x++) {
                if (Math.hypot(x - 7.5, y - 9.5) > 5.2) continue;
                g[y][x] = y < 8 ? glass : (x > 9 ? potionDark : potion);
            }
        }
        for (int y = 2; y < 5; y++) { g[y][7] = glass; g[y][8] = glass; }
        g[1][7] = cork;
        g[1][8] = cork;
        g[2][7] = cork;
        g[2][8] = cork;
        g[9][5] = shine;
        g[10][5] = shine;
        return outlined(g);
    }

    // -------------------------------------------------------------------------
    // Outils
    // -------------------------------------------------------------------------

    /** @return {@code g} entouré d'un contour sombre (tout pixel vide qui touche un pixel dessiné). */
    private static int[][] outlined(int[][] g) {
        int h = g.length, w = g[0].length;
        int[][] out = new int[h][];
        for (int y = 0; y < h; y++) out[y] = g[y].clone();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (g[y][x] != 0) continue;
                boolean touches = y > 0 && g[y - 1][x] != 0 || y < h - 1 && g[y + 1][x] != 0
                    || x > 0 && g[y][x - 1] != 0 || x < w - 1 && g[y][x + 1] != 0;
                if (touches) out[y][x] = OUTLINE;
            }
        }
        return out;
    }

    /** @return la texture de la grille ({@code flash} : silhouette toute blanche). */
    private static Texture texture(int[][] g, boolean flash) {
        Pixmap pixmap = new Pixmap(g[0].length, g.length, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < g.length; y++) {
            for (int x = 0; x < g[0].length; x++) {
                int color = g[y][x];
                pixmap.drawPixel(x, y, color == 0 ? 0 : flash ? 0xffffffff : color);
            }
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private static int c(String hex) {
        return Color.rgba8888(Color.valueOf(hex));
    }

    @Override
    public void dispose() {
        portraits.values().forEach(Texture::dispose); // le croupier compris
        flashes.values().forEach(Texture::dispose);
        symbols.values().forEach(Texture::dispose);
        chapterArt.values().forEach(Texture::dispose);
        gates.values().forEach(Texture::dispose);
        chestClosed.dispose();
        chestOpen.dispose();
        prairie.dispose();
    }
}
