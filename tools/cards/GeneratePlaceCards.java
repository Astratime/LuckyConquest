import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Dessine les images des cartes des lieux de l'Exploration ouverts après la
 * prairie (assets/cards/special/*.png) : le Port des Contrebandiers, les
 * Mines d'Or et le Casino Englouti. Même style que les cartes des donjons de
 * la prairie (voir GenerateDungeonCards) : le cadre de l'Aimant, recoloré à la
 * couleur du lieu, et un pixel art de 12 x 12 sur la pièce d'or du centre.
 *
 * Lancement, depuis la racine du dépôt : java tools/cards/GeneratePlaceCards.java
 */
public class GeneratePlaceCards {

    private static final int GRID_W = 40, GRID_H = 56, PIXEL = 12;
    private static final String TEMPLATE = "assets/cards/special/magnet.png";
    private static final String OUT_DIR  = "assets/cards/special/";

    /** Couleurs du cadre de l'Aimant, remplacées par celles de chaque couleur de donjon. */
    private static final int FRAME_DARK = 0xff16285f, FRAME = 0xff284696, BACK = 0xff1e3473, PLATE = 0xff14235a;
    /** Or de la pièce, sur laquelle l'icône est dessinée. */
    private static final int COIN = 0xffeeba3c;
    private static final int OUTLINE = 0xff190e08;

    public static void main(String[] args) throws Exception {
        int[][] template = load(TEMPLATE);
        clearIcon(template);

        // --- Le Port des Contrebandiers : bleu marine et cuivre ---
        int[] port = {0xff0e2a3a, 0xff1e5a72, 0xff143e52, 0xff0a1e2a};
        write(template, port, "scorbut", new String[] {
            "...oooooo...",
            "..oggggggo..",
            ".oggggggggo.",
            ".ogoogooggo.",
            ".ogoogoogGo.",
            ".oggggggGGo.",
            "..oggooggo..",
            "..ogggggGo..",
            "...owowowo..",
            "...oooooo...",
            "..r.....r...",
            ".rr......r.."},
            Map.of('g', 0xffa8c860, 'G', 0xff6a8a30, 'w', 0xfff0ead0, 'r', 0xff8a1a1a));
        write(template, port, "longue_vue", new String[] {
            "..........oo",
            "........ooyo",
            "......ooyyYo",
            ".....oyyYYo.",
            "....obbbbo..",
            "...obbbbo...",
            "..obbBbo....",
            ".oyybbo.....",
            "oyyYYo......",
            "oyYYo.......",
            ".ooo........",
            "............"},
            Map.of('y', 0xffe8b048, 'Y', 0xff9a6a20, 'b', 0xff5e3a18, 'B', 0xff8a5a2a));
        write(template, port, "rhum", new String[] {
            ".....oo.....",
            "....orro....",
            "....owwo....",
            "....obbo....",
            "...obbbbo...",
            "..obbbbbBo..",
            "..owwwwwwo..",
            "..owrrrrwo..",
            "..owwwwwwo..",
            "..obbbbbBo..",
            "..obbbbBBo..",
            "...oooooo..."},
            Map.of('b', 0xff8a4a1a, 'B', 0xff5a2e0e, 'w', 0xfff0e6c8, 'r', 0xffc0283a));
        write(template, port, "carte_au_tresor", new String[] {
            "oooooooooooo",
            "opppppppppPo",
            "opdpppppppPo",
            "oppdppppppPo",
            "opppdpppppPo",
            "oppppdppppPo",
            "opppppdprpro",
            "oppppppdprPo",
            "opppppprprPo",
            "oppppppppppo",
            "oPPPPPPPPPPo",
            "oooooooooooo"},
            Map.of('p', 0xfff0dca0, 'P', 0xffc8a868, 'd', 0xff8a5a2a, 'r', 0xffd8202c));

        // --- Les Mines d'Or : brun et or ---
        int[] mines = {0xff3a240e, 0xff7a5020, 0xff563814, 0xff24160a};
        write(template, mines, "pioche_du_mineur", new String[] {
            "..oooooo....",
            ".osssssSo...",
            "osoooobSSo..",
            "oo...obo.So.",
            ".....obo..o.",
            "....obo.....",
            "....obo.....",
            "...obo......",
            "...obo......",
            "..obo.......",
            "..obo.......",
            "...o........"},
            Map.of('s', 0xffc8d0dc, 'S', 0xff7a8494, 'b', 0xff8a5a2a));
        write(template, mines, "casque", new String[] {
            "....oooo....",
            "...owwWWo...",
            "..oyyoowyo..",
            ".oyyywwyyYo.",
            ".oyyyyyyyYo.",
            "oyyyyyyyyYYo",
            "oyyyyyyyyYYo",
            "oooooooooooo",
            "oYYYYYYYYYYo",
            ".oooooooooo.",
            "............",
            "............"},
            Map.of('y', 0xffe8c020, 'Y', 0xffa08010, 'w', 0xfffff2a0, 'W', 0xffffffff));
        write(template, mines, "veine_d_or", new String[] {
            "rrrrrrrrrrrr",
            "rRrrrrryyrRr",
            "rrrrrryYyrrr",
            "rrRrryYyrrrr",
            "rrrryYyrrrRr",
            "rrryYYyrrrrr",
            "rryYyrrrrrrr",
            "rrryyRrrrRrr",
            "rRrryYyrrrrr",
            "rrrrrryYyrrr",
            "rrrRrrrryyRr",
            "rrrrrrrrrrrr"},
            Map.of('r', 0xff5a4a42, 'R', 0xff3a302a, 'y', 0xffffd54a, 'Y', 0xfffff2a0));

        // --- Le Casino Englouti : turquoise et violet ---
        int[] casino = {0xff1a1440, 0xff2a8a8a, 0xff1e5a66, 0xff120e2a};
        write(template, casino, "bulle_d_air", new String[] {
            "...oooooo...",
            "..obbbbbbo..",
            ".obwwbbbbbo.",
            "obwwbbbbbbBo",
            "obwbbbbbbbBo",
            "obbbbbbbbbBo",
            "obbbbbbbbbBo",
            "obbbbbbbbBBo",
            ".obbbbbbBBo.",
            "..oBBBBBBo..",
            "...oooooo...",
            "............"},
            Map.of('b', 0xff9ae0f4, 'B', 0xff4aa8c8, 'w', 0xffffffff));
        write(template, casino, "perle_noire", new String[] {
            "............",
            "..oooooooo..",
            ".ossssssSSo.",
            "oSssssssssSo",
            "ooooooooooo.",
            "....okko....",
            "...okwkKo...",
            "...okkkKo...",
            "....oKKo....",
            "oooooooooooo",
            ".oSssssssSo.",
            "..oooooooo.."},
            Map.of('s', 0xfff0c8d8, 'S', 0xffc890a8, 'k', 0xff3a3050, 'K', 0xff1a1428, 'w', 0xffc8c0e0));
        write(template, casino, "trident", new String[] {
            "o....o....o.",
            "yo..oyo..oy.",
            "yo..oyo..oy.",
            "yo..oyo..oy.",
            "yYooyYyooyY.",
            ".yYYYYYYYY..",
            "..ooyYYoo...",
            "....oyYo....",
            "....oyYo....",
            "....oyYo....",
            "....oyYo....",
            ".....oo....."},
            Map.of('y', 0xffffd54a, 'Y', 0xffc4891e));
    }

    /** Efface le dessin de l'Aimant sur la pièce d'or (garde la pièce et ses reflets). */
    private static void clearIcon(int[][] grid) {
        for (int y = 20; y <= 34; y++) {
            for (int x = 12; x <= 27; x++) {
                double d = Math.hypot(x - 19.5, y - 27.5);
                if (d <= 8.6) grid[y][x] = COIN;
            }
        }
    }

    /** Écrit l'image de la carte {@code name} : cadre recoloré par {@code frame}, icône {@code icon} au centre. */
    private static void write(int[][] template, int[] frame, String name, String[] icon, Map<Character, Integer> palette)
            throws Exception {
        Map<Integer, Integer> recolor = new HashMap<>();
        recolor.put(FRAME_DARK, frame[0]);
        recolor.put(FRAME, frame[1]);
        recolor.put(BACK, frame[2]);
        recolor.put(PLATE, frame[3]);
        int[][] grid = new int[GRID_H][GRID_W];
        for (int y = 0; y < GRID_H; y++) {
            for (int x = 0; x < GRID_W; x++) grid[y][x] = recolor.getOrDefault(template[y][x], template[y][x]);
        }
        int left = 14, top = 21;
        for (int row = 0; row < icon.length; row++) {
            if (icon[row].length() != 12) throw new IllegalArgumentException(name + " : ligne " + row + " de " + icon[row].length() + " pixels");
            for (int col = 0; col < 12; col++) {
                char c = icon[row].charAt(col);
                if (c == '.') continue;
                grid[top + row][left + col] = c == 'o' ? OUTLINE : palette.get(c);
            }
        }
        BufferedImage image = new BufferedImage(GRID_W * PIXEL, GRID_H * PIXEL, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < GRID_H * PIXEL; y++) {
            for (int x = 0; x < GRID_W * PIXEL; x++) image.setRGB(x, y, grid[y / PIXEL][x / PIXEL]);
        }
        ImageIO.write(image, "png", new File(OUT_DIR + name + ".png"));
    }

    /** @return la grille de couleurs (ARGB) de l'image {@code path}, un pixel par case de 12 x 12. */
    private static int[][] load(String path) throws Exception {
        BufferedImage image = ImageIO.read(new File(path));
        int[][] grid = new int[GRID_H][GRID_W];
        for (int y = 0; y < GRID_H; y++) {
            for (int x = 0; x < GRID_W; x++) {
                int c = image.getRGB(x * PIXEL + PIXEL / 2, y * PIXEL + PIXEL / 2);
                grid[y][x] = (c >>> 24) == 0 ? 0 : c;
            }
        }
        return grid;
    }
}
