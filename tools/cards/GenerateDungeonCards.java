import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Dessine les images des cartes des donjons de l'Exploration
 * (assets/cards/special/*.png), dans le style des cartes spéciales : le cadre
 * de l'Aimant, recoloré à la couleur du donjon, et un pixel art de 12 x 12
 * sur la pièce d'or du centre.
 *
 * Lancement, depuis la racine du dépôt : java tools/cards/GenerateDungeonCards.java
 */
public class GenerateDungeonCards {

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

        // --- Pique : gris acier sur cadre indigo ---
        int[] pique = {0xff1e1e3a, 0xff3e3e72, 0xff28284e, 0xff141428};
        write(template, pique, "pierre_a_aiguiser", new String[] {
            "........y...",
            "......oo.y.y",
            ".....owWo.y.",
            "....owWo....",
            "...owWo.....",
            "..owWo......",
            ".owWo.......",
            "oooooooooo..",
            "oSssssssso..",
            "oSSsssssso..",
            ".oSSSSSSo...",
            "..oooooo...."},
            Map.of('w', 0xffe8ecf4, 'W', 0xff9aa4b4, 's', 0xff8a8a96, 'S', 0xff5a5a68, 'y', 0xffffffff));
        write(template, pique, "dague_de_l_ombre", new String[] {
            "..........oo",
            ".........owo",
            "........owWo",
            ".......owWo.",
            "......owWo..",
            "..o..owWo...",
            "..oooWWo....",
            "...oppo.....",
            "..oppoo.....",
            ".opo.o......",
            "opo.........",
            "oo.........."},
            Map.of('w', 0xff6a6a8a, 'W', 0xff3a3a56, 'p', 0xff8a3ad8));
        write(template, pique, "guillotine", new String[] {
            "oooooooooooo",
            "obBBBBBBBBbo",
            "obooooooooBo",
            "obowwwwwwoBo",
            "obowwwwwo.Bo",
            "obowwwwo..Bo",
            "obooooo...Bo",
            "ob........Bo",
            "ob...rr...Bo",
            "ooooooooooBo",
            "obbbbbbbbbbo",
            "oooooooooooo"},
            Map.of('b', 0xff8a5a2a, 'B', 0xff5e3a18, 'w', 0xffd8dce6, 'r', 0xffc0283a));

        // --- Trèfle : vert sur cadre vert ---
        int[] trefle = {0xff0e3a1a, 0xff2a7a3e, 0xff1a5228, 0xff0a2a12};
        write(template, trefle, "trefle_a_quatre_feuilles", new String[] {
            "..ooo..ooo..",
            ".oglgooglgo.",
            ".ogggooGggo.",
            ".oggGooGggo.",
            "..oooGGooo..",
            ".oooGGGGooo.",
            "oglgoGGoglgo",
            "ogggooooGggo",
            ".oGgo..oggo.",
            "..oo.oo.oo..",
            ".....oGo....",
            "......oo...."},
            Map.of('g', 0xff3ab84a, 'G', 0xff1e7a2e, 'l', 0xff9aee8a));
        write(template, trefle, "bourse_garnie", new String[] {
            "....oooo....",
            "...obbbbo...",
            "....oyyo....",
            "...obbbbo...",
            "..obbbbbbo..",
            ".obbbbBbbbo.",
            "obbbbbbBbbbo",
            "obbbbbbBbbbo",
            "obbbbbbbbBbo",
            ".obbbbbbbbo.",
            "..oooooooo..",
            "............"},
            Map.of('b', 0xff8a4a22, 'B', 0xff5e2e12, 'y', 0xffffe082));
        write(template, trefle, "fortune_du_roi", new String[] {
            ".o...oo...o.",
            "oyo.oyyo.oyo",
            "oyyooyyooyyo",
            "oyyyyyyyyyyo",
            "oyryyyyyyryo",
            "oyyyyRyyyyyo",
            "oooooooooooo",
            "ovvvvvvvvvvo",
            "oooooooooooo",
            "..o..o..o...",
            ".oyo.oyo.oyo",
            "..o..o..o..."},
            Map.of('y', 0xffffe9a0, 'r', 0xffd8202c, 'R', 0xff3ad86a, 'v', 0xff8a1e2a));

        // --- Coeur : rouge sur cadre rouge ---
        int[] coeur = {0xff4a0e16, 0xff8e1e2c, 0xff601420, 0xff300a10};
        write(template, coeur, "transfusion", new String[] {
            ".....oo.....",
            "....orro....",
            "...orrrro...",
            "...orrrro...",
            "..orrwwrro..",
            "..orwwwwro..",
            ".orrwwwwrro.",
            ".orrrwwrrro.",
            ".oRrrrrrrRo.",
            "..oRrrrrRo..",
            "...oRRRRo...",
            "....oooo...."},
            Map.of('r', 0xffd8202c, 'R', 0xff8a1018, 'w', 0xffffffff));
        write(template, coeur, "baiser_vampire", new String[] {
            "............",
            "..ooo..ooo..",
            ".orrroorrro.",
            "orrrrrrrrrro",
            "oRRRRRRRRRRo",
            "oooooooooooo",
            ".owo....owo.",
            ".owo....owo.",
            "..o......o..",
            "orrrrrrrrrro",
            ".oRRRRRRRRo.",
            "..oooooooo.."},
            Map.of('r', 0xffe0283a, 'R', 0xff9a1424, 'w', 0xffffffff));
        write(template, coeur, "pacte_de_sang", new String[] {
            ".oo...oo....",
            "orro.orro...",
            "orrrorrrro.o",
            "orrrrrrrrowo",
            "orrrrrrrowo.",
            ".orrrrrowo..",
            "..orrrowo...",
            "...orowo....",
            "...owoo.....",
            "..oyoo......",
            ".oyo........",
            ".oo........."},
            Map.of('r', 0xffd8202c, 'w', 0xffd8dce6, 'y', 0xff3a2a2e));

        // --- Carreau : bleu glacé sur cadre ambre ---
        int[] carreau = {0xff5a2e0a, 0xffb05e18, 0xff7e4210, 0xff3a1e06};
        write(template, carreau, "rempart", new String[] {
            "oo.oo..oo.oo",
            "osoosooosoos",
            "osssssssssso",
            "oSsssssssSso",
            "osssoooosSso",
            "osssobbosSso",
            "oSssobbossso",
            "osssobbosSso",
            "osssobbossso",
            "oSssobbosSso",
            "osssobbossso",
            "oooooooooooo"},
            Map.of('s', 0xffa8a8b4, 'S', 0xff70707e, 'b', 0xff5e3a18));
        write(template, carreau, "miroir_taille", new String[] {
            ".....oo.....",
            "....ollo....",
            "...olwllo...",
            "..olwlllLo..",
            ".olwllllLLo.",
            "olllllllLLLo",
            "oLlllllllLLo",
            ".oLllllllLo.",
            "..oLllllLo..",
            "...oLlllo...",
            "....oLLo....",
            ".....oo....."},
            Map.of('l', 0xffaee4f4, 'L', 0xff5aa8c8, 'w', 0xffffffff));
        write(template, carreau, "diamant_brut", new String[] {
            "............",
            "...oooooo...",
            "..ocwccCCo..",
            ".ocwcccCCCo.",
            "oooooooooooo",
            "oCcwcccccCCo",
            ".oCcwccccCo.",
            "..oCcccccCo.",
            "...oCccCo...",
            "....oCCo....",
            ".....oo.....",
            "............"},
            Map.of('c', 0xff4ad8f0, 'C', 0xff1e8ab0, 'w', 0xffffffff));
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
