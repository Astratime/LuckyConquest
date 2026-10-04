import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Dessine les images des cartes vendues à la Boutique (assets/cards/special/*.png),
 * dans le style des cartes spéciales : le cadre de l'Aimant, recoloré en violet
 * de casino, et un pixel art de 12 x 12 sur la pièce d'or du centre.
 *
 * Écrit aussi les icônes du panneau latéral (assets/hud/icon_*.png) des
 * effets en cours : Coffre-fort, Double ou rien.
 *
 * Lancement, depuis la racine du dépôt : java tools/cards/GenerateShopCards.java
 */
public class GenerateShopCards {

    private static final int GRID_W = 40, GRID_H = 56, PIXEL = 12;
    private static final String TEMPLATE = "assets/cards/special/magnet.png";
    private static final String OUT_DIR  = "assets/cards/special/";

    /** Couleurs du cadre de l'Aimant, remplacées par celles de la Boutique. */
    private static final int FRAME_DARK = 0xff16285f, FRAME = 0xff284696, BACK = 0xff1e3473, PLATE = 0xff14235a;
    /** Or de la pièce, sur laquelle l'icône est dessinée. */
    private static final int COIN = 0xffeeba3c;
    private static final int OUTLINE = 0xff190e08;
    /** Cadre violet de casino des cartes de la Boutique. */
    private static final int[] BOUTIQUE = {0xff2a0c3e, 0xff6a2a8e, 0xff461a62, 0xff1e0830};

    /** Icônes du panneau latéral : 5 px par pixel d'icône, fond transparent. */
    private static final int HUD_PIXEL = 5;
    private static final Map<String, String> HUD_ICONS = Map.of(
        "safe", "icon_safe", "double_or_nothing", "icon_double");

    public static void main(String[] args) throws Exception {
        int[][] template = load(TEMPLATE);
        clearIcon(template);

        write(template, BOUTIQUE, "reroll", new String[] {
            "....oooo.o..",
            "..oogggGoGo.",
            ".ogGooooGGo.",
            ".oGo...oGGGo",
            "ogo....ooooo",
            "ogo.........",
            ".........ogo",
            "ooooo....ogo",
            "oGGGo...oGo.",
            ".oGGoooogGo.",
            ".oGoGgggoo..",
            "..o.oooo...."},
            Map.of('g', 0xff6ae07a, 'G', 0xff1e8a3e));
        write(template, BOUTIQUE, "rigged_reel", new String[] {
            "oooooooooooo",
            "occcccccccCo",
            "ocrrrrrrrcCo",
            "ocRRRRRrrcCo",
            "occcccrrRcCo",
            "occccrrRccCo",
            "occccrrRccCo",
            "occcrrRcccCo",
            "occcrrRcccCo",
            "occcRRccccCo",
            "oCCCCCCCCCCo",
            "oooooooooooo"},
            Map.of('c', 0xfff4ecd8, 'C', 0xffcdbf9e, 'r', 0xffd8202c, 'R', 0xff8a1018));
        write(template, BOUTIQUE, "ghost_reel", new String[] {
            "....oooo....",
            "..oowwwwoo..",
            ".owwwwwwwwo.",
            ".owvvwwvvwo.",
            "owwvvwwvvwWo",
            "owwwwwwwwwWo",
            "owwwwoowwwWo",
            "owwwwoowwwWo",
            "owwwwwwwwwWo",
            "owwwwwwwwwWo",
            "owWoowWoowWo",
            "oo..oo..oo.."},
            Map.of('w', 0xfff4f4ff, 'W', 0xffa8a8d0, 'v', 0xff5a2a8a));
        write(template, BOUTIQUE, "rank_token", new String[] {
            "...oooooo...",
            "..owwrrwwo..",
            ".owrrrrrrwo.",
            "orrrryyrrrro",
            "orryyyyyyrro",
            "owrryyyyrrwo",
            "owrryyyyrrwo",
            "orryyrryyrro",
            "owryrrrryrwo",
            ".owrrrrrrwo.",
            "..owwrrwwo..",
            "...oooooo..."},
            Map.of('w', 0xfff4f4f4, 'r', 0xffd8202c, 'y', 0xffffd54a));
        write(template, BOUTIQUE, "all_in", new String[] {
            "......oooo..",
            ".....oggggo.",
            ".....oGGGGo.",
            ".oooooggggo.",
            "orrrroGGGGo.",
            "oRRRRoggggo.",
            "orrrroGGGGo.",
            "oRRRRoggggo.",
            "orrrroGGGGo.",
            "oRRRRoggggo.",
            "orrrroooooo.",
            "oooooo......"},
            Map.of('r', 0xffe0303c, 'R', 0xff8a1018, 'g', 0xff3ab84a, 'G', 0xff1e6a2e));
        write(template, BOUTIQUE, "safe", new String[] {
            "oooooooooooo",
            "osssssssssSo",
            "osooooooooSo",
            "osollyylloSo",
            "osolyddyloSo",
            "osolyddyloSo",
            "osollyylloSo",
            "osolllllloSo",
            "osooooooooSo",
            "osssssssssSo",
            "oooooooooooo",
            ".oo......oo."},
            Map.of('s', 0xffa8a8b4, 'S', 0xff60606e, 'l', 0xffd0d0dc, 'y', 0xffffd54a, 'd', 0xff3a3a46));
        write(template, BOUTIQUE, "insurance", new String[] {
            ".....oo.....",
            "...oobbBoo..",
            "..obbbbbBBo.",
            ".obbbbbbbBBo",
            "obbbbbbbbbBo",
            "oooooohooooo",
            ".....oho....",
            ".....oho....",
            ".....oho....",
            "..o..oho....",
            ".ohooho.....",
            "..ohho......"},
            Map.of('b', 0xff3a8ae0, 'B', 0xff1e4a8a, 'h', 0xff8a5a2a));
        write(template, BOUTIQUE, "bribe", new String[] {
            "............",
            "oooooooooooo",
            "onnnnnNnnnno",
            "onNnnNNNnnNo",
            "onnnnNnnnnno",
            "onnnnNNNnnno",
            "onnnnnnNnnno",
            "onNnnNNNnnNo",
            "onnnnnNnnnno",
            "oooooooooooo",
            ".oNNNNNNNNNo",
            ".ooooooooooo"},
            Map.of('n', 0xff8ad87a, 'N', 0xff2c7a2e));
        write(template, BOUTIQUE, "double_or_nothing", new String[] {
            "oooooo......",
            "orwwwo...y..",
            "owwwWo..yyy.",
            "owwwWo...y..",
            "owwwro......",
            "oooooo......",
            "......oooooo",
            "..y...orwwwo",
            ".yyy..owwwWo",
            "..y...owwwWo",
            "......owwwro",
            "......oooooo"},
            Map.of('w', 0xfff8f8f8, 'W', 0xffc0c0cc, 'r', 0xffd8202c, 'y', 0xffffffff));
        write(template, BOUTIQUE, "overheat", new String[] {
            ".....o......",
            "....oro.....",
            "...orro..o..",
            "...orOro.oro",
            "..orOOroorro",
            ".orOOyOrrOro",
            ".orOyyyOOOro",
            "orOyyYyyOOro",
            "orOyYYYyyOro",
            "orOyYYYYyOro",
            ".orOyyyyOro.",
            "..oorrrroo.."},
            Map.of('r', 0xffd8301c, 'O', 0xfff08a1c, 'y', 0xffffd54a, 'Y', 0xfffff3c0));
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
        if (HUD_ICONS.containsKey(name)) writeHudIcon(HUD_ICONS.get(name), icon, palette);
    }

    /** Écrit l'icône {@code icon} seule, sur fond transparent, pour le panneau latéral. */
    private static void writeHudIcon(String name, String[] icon, Map<Character, Integer> palette) throws Exception {
        BufferedImage image = new BufferedImage(12 * HUD_PIXEL, 12 * HUD_PIXEL, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 12 * HUD_PIXEL; y++) {
            for (int x = 0; x < 12 * HUD_PIXEL; x++) {
                char c = icon[y / HUD_PIXEL].charAt(x / HUD_PIXEL);
                image.setRGB(x, y, c == '.' ? 0 : c == 'o' ? OUTLINE : palette.get(c));
            }
        }
        ImageIO.write(image, "png", new File("assets/hud/" + name + ".png"));
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
