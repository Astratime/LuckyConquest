package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.enemy.EnemySignature;
import fr.astratime.lucky.entities.enemy.EnemySymbol;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

/**
 * Dessine les images des cartes propres des ennemis (assets/cards/dark/sig/*.png),
 * dans le style du jeu sombre : le cadre du Rouleau interdit, le dessin du
 * symbole que la carte renforce (le même que sur ses rouleaux) dans la fenêtre,
 * et en bas le moment où elle agit : un soleil pour « ce tour », une flèche pour
 * « son prochain symbole », un éclair pour « tout de suite ».
 *
 * Lancement : ./gradlew :core:enemyCards
 */
public final class EnemyCardArt {

    private static final String TEMPLATE = "cards/dark/FORBIDDEN_REEL.png";
    private static final String OUT_DIR  = "cards/dark/sig/";

    /** Couleurs du cadre du jeu sombre (ARGB). */
    private static final int FACE = 0xff34314f, WINDOW = 0xff1a1828, GOLD = 0xffe8c050, RED = 0xffd23a3a;
    /** Taille d'un pixel du dessin du symbole, et d'un pixel des marques du cadre, en pixels de l'image. */
    private static final int SYMBOL_PIXEL = 5, MARK_PIXEL = 10;
    /** Fenêtre du cadre, en pixels de l'image. */
    private static final int WINDOW_X0 = 50, WINDOW_Y0 = 70, WINDOW_X1 = 139, WINDOW_Y1 = 169;

    private EnemyCardArt() {}

    public static void main(String[] args) throws Exception {
        String assets = System.getProperty("lucky.assets", "assets");
        BufferedImage template = ImageIO.read(new File(assets, TEMPLATE));
        new File(assets, OUT_DIR).mkdirs();
        for (EnemySignature signature : EnemySignature.values()) {
            BufferedImage image = draw(template, signature);
            ImageIO.write(image, "png", new File(assets, signature.assetPath()));
        }
        System.out.println(EnemySignature.values().length + " cartes propres dessinées dans " + new File(assets, OUT_DIR));
    }

    /** @return l'image de la carte propre {@code signature}, sur le cadre {@code template}. */
    static BufferedImage draw(BufferedImage template, EnemySignature signature) {
        BufferedImage image = new BufferedImage(template.getWidth(), template.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < template.getHeight(); y++) {
            for (int x = 0; x < template.getWidth(); x++) image.setRGB(x, y, template.getRGB(x, y));
        }
        fill(image, WINDOW_X0, WINDOW_Y0, WINDOW_X1, WINDOW_Y1, WINDOW); // la fenêtre, vidée de sa croix
        fill(image, 20, 20, 49, 49, FACE);                              // la marque du coin
        fill(image, 60, 180, 139, 249, FACE);                           // le cadenas
        mark(image, 20, 20, new String[] {".r.", "rrr", ".r."}, RED);

        int[][] grid = grid(signature);
        int width = grid[0].length * SYMBOL_PIXEL, height = grid.length * SYMBOL_PIXEL;
        int left = (WINDOW_X0 + WINDOW_X1 + 1 - width) / 2, top = (WINDOW_Y0 + WINDOW_Y1 + 1 - height) / 2;
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[0].length; x++) {
                if (grid[y][x] == 0) continue;
                int rgba = grid[y][x];
                int argb = (rgba >>> 8) | (rgba << 24);
                fill(image, left + x * SYMBOL_PIXEL, top + y * SYMBOL_PIXEL,
                    left + (x + 1) * SYMBOL_PIXEL - 1, top + (y + 1) * SYMBOL_PIXEL - 1, argb);
            }
        }
        String[] badge = switch (signature.getTiming()) {
            case TURN -> new String[] {"r.r.r", ".rrr.", "rrrrr", ".rrr.", "r.r.r"};
            case NEXT -> new String[] {"..r..", "...r.", "rrrrr", "...r.", "..r.."};
            case NOW  -> new String[] {"..rrr", ".rrr.", "rrrrr", ".rrr.", "rrr.."};
        };
        mark(image, 70, 190, badge, GOLD);
        return image;
    }

    /** @return le dessin de la carte : celui de son symbole, ou le sien pour les cartes sans symbole propre. */
    static int[][] grid(EnemySignature signature) {
        return switch (signature) {
            case QUEUE_DE_COMETE -> pixels(new String[] {
                "...........kkk..",
                "..........kyWyk.",
                ".........kyWWWyk",
                "........kyWWWWyk",
                ".......kyyWWWyk.",
                "......koyyyyyk..",
                ".....kooyyykk...",
                "....koooyykk....",
                "...kooookk......",
                "..koookk........",
                ".kookk..........",
                "kook............",
                "kok.............",
                "kk.............."},
                Map.of('y', GOLD_RGBA, 'W', 0xfff8e0ff, 'o', 0xe8742aff));
            case MAIN_DE_LA_REINE -> pixels(new String[] {
                "..k....k....k...",
                ".kyk..kyk..kyk..",
                ".kyyk.kyk.kyyk..",
                ".kyyykyyykyyyk..",
                ".kyryyyryyyryk..",
                ".kyyyyyyyyyyyk..",
                ".kYYYYYYYYYYYk..",
                ".kkkkkkkkkkkkk..",
                "...kpppppppk....",
                "..kppPPPPPppk...",
                "..kpPPPPPPPpk...",
                "...kkkkkkkkk...."},
                Map.of('y', GOLD_RGBA, 'Y', 0xb08a2aff, 'r', 0xc0283aff, 'p', 0x8a3ad8ff, 'P', 0xb070f0ff));
            case SAC_DE_BUTIN -> pixels(new String[] {
                "......kkkk......",
                ".....kbbbbk.....",
                "......kyyk......",
                ".....kbbbbk.....",
                "....kbbbbbbk....",
                "...kbbBbbbbbk...",
                "..kbbBBbbbbbbk..",
                "..kbbBbbWWbbbk..",
                "..kbbbbbWbbbbk..",
                "..kbbbbbbbbbBk..",
                "...kbbbbbbbBk...",
                "....kkkkkkkk...."},
                Map.of('b', 0x8a5a2aff, 'B', 0xb07a3aff, 'y', GOLD_RGBA, 'W', 0xf0f0f8ff));
            case ECLAT_VOLE -> pixels(new String[] {
                ".......kk.......",
                "......kWck......",
                ".....kWccck.....",
                "....kWcccCck....",
                "...kWccccCCck...",
                "...kccccCCCck...",
                "....kcccCCck....",
                ".....kccCck.....",
                "......kCck......",
                ".......kk......."},
                Map.of('W', 0xf8f0ffff, 'c', 0xb070f0ff, 'C', 0x6a3ab0ff));
            case REMONTOIR -> pixels(new String[] {
                "...kkk..........",
                "..kyyyk.........",
                ".kyk.kyk........",
                ".ky...yk........",
                ".kyk.kyk........",
                "..kyyykkkkkkkk..",
                "...kkkyyyyyyyyk.",
                ".......kkkykkyk.",
                ".........kyk.kk.",
                "..........k....."},
                Map.of('y', GOLD_RGBA));
            case COMPTE_A_REBOURS -> pixels(new String[] {
                ".....kkkkkk.....",
                "...kkWWWWWWkk...",
                "..kWWWWkWWWWWk..",
                ".kWWWWWkWWWWWWk.",
                ".kWWWWWkWWWWWWk.",
                "kWWWWWWkWWWWWWWk",
                "kWWWWWWkkkkkWWWk",
                "kWWWWWWWWWWWWWWk",
                ".kWWWWWWWWWWWWk.",
                ".kWWWWWWWWWWWWk.",
                "..kWWWWWWWWWWk..",
                "...kkWWWWWWkk...",
                ".....kkkkkk....."},
                Map.of('W', 0xe8e4f4ff));
            case ENCRE -> pixels(new String[] {
                ".......kk.......",
                "......kiik......",
                ".....kiiiik.....",
                "....kiiiiiik....",
                "...kiiIiiiiik...",
                "..kiiIIiiiiiik..",
                "..kiiIiiiiiiik..",
                "..kiiiiiiiiiik..",
                "...kiiiiiiiik...",
                "....kkkkkkkk...."},
                Map.of('i', 0x2a2050ff, 'I', 0x6a60a0ff));
            default -> EnemyTextures.symbolGrid(symbolOf(signature));
        };
    }

    /** @return le symbole dont la carte porte le dessin. */
    private static EnemySymbol symbolOf(EnemySignature signature) {
        return signature.getSymbol() != null ? signature.getSymbol() : EnemySymbol.SWORD;
    }

    /** Or des dessins, en RGBA. */
    private static final int GOLD_RGBA = 0xe8c050ff;

    /** @return la grille RGBA du dessin {@code rows} ({@code k} : contour, {@code .} : vide). */
    private static int[][] pixels(String[] rows, Map<Character, Integer> palette) {
        int[][] grid = new int[rows.length][rows[0].length()];
        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < rows[y].length(); x++) {
                char c = rows[y].charAt(x);
                grid[y][x] = c == '.' ? 0 : c == 'k' ? PixelCanvas.OUTLINE : palette.getOrDefault(c, PixelCanvas.OUTLINE);
            }
        }
        return grid;
    }

    /** Dessine la marque {@code rows} en pixels de {@link #MARK_PIXEL}, à partir de ({@code x0}, {@code y0}). */
    private static void mark(BufferedImage image, int x0, int y0, String[] rows, int color) {
        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < rows[y].length(); x++) {
                if (rows[y].charAt(x) == '.') continue;
                fill(image, x0 + x * MARK_PIXEL, y0 + y * MARK_PIXEL,
                    x0 + (x + 1) * MARK_PIXEL - 1, y0 + (y + 1) * MARK_PIXEL - 1, color);
            }
        }
    }

    private static void fill(BufferedImage image, int x0, int y0, int x1, int y1, int argb) {
        for (int y = y0; y <= y1; y++) for (int x = x0; x <= x1; x++) image.setRGB(x, y, argb);
    }
}
