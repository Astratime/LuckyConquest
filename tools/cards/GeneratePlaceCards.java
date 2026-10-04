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

        // --- Coffres des donjons des lieux (une carte par donjon) ---
        write(template, port, "piege_a_rats", new String[] {
            "............",
            "............",
            "...ooooo....",
            "..ossssso...",
            "..os...so...",
            "..os...so.o.",
            "..os...soyo.",
            "oooooooooYYo",
            "obbbbbbbbbbo",
            "oBBBBBBBBBBo",
            ".oooooooooo.",
            "............"},
            Map.of('s', 0xffc8d0dc, 'y', 0xfff0c840, 'Y', 0xffc89820, 'b', 0xff8a5a2a, 'B', 0xff5e3a18));
        write(template, port, "chope", new String[] {
            "..oooooo....",
            ".owwwwwwo...",
            ".owwWwwWwo..",
            ".oyyyyyyoo..",
            ".oyyyyYyo.o.",
            ".oyyyyYyoyo.",
            ".oyyyyYyoyo.",
            ".oyyyyYyoyo.",
            ".oyyyyYyo.o.",
            ".oyyyyYyoo..",
            ".oYYYYYYo...",
            "..oooooo...."},
            Map.of('w', 0xfffff8e8, 'W', 0xffe0d8c0, 'y', 0xfff0b030, 'Y', 0xffb87818));
        write(template, port, "tournee_generale", new String[] {
            "..w.....w...",
            ".oooo..oooo.",
            ".owwo..owwo.",
            ".oyyo..oyyo.",
            ".oyyo..oyyo.",
            ".oyYo..oyYo.",
            "..oo....oo..",
            "...o....o...",
            "...o....o...",
            "..ooo..ooo..",
            "............",
            "............"},
            Map.of('w', 0xfffff8e8, 'y', 0xfff0b030, 'Y', 0xffb87818));
        write(template, port, "fut_de_poudre", new String[] {
            "........o.r.",
            ".......o.rR.",
            "...oooooo...",
            "..obbbbbbo..",
            ".oBBBBBBBBo.",
            ".obbbbbbbbo.",
            ".obbkkkkbbo.",
            ".obbkkkkbbo.",
            ".obbbbbbbbo.",
            ".oBBBBBBBBo.",
            "..obbbbbbo..",
            "...oooooo..."},
            Map.of('b', 0xff8a5a2a, 'B', 0xff5e3a18, 'k', 0xff2a2a2a, 'r', 0xffe84020, 'R', 0xffffd040));
        write(template, port, "lanterne", new String[] {
            "....oooo....",
            "...o....o...",
            "...oooooo...",
            "..oyyyyyyo..",
            "..oywwwwyo..",
            "..oywWWwyo..",
            "..oywWWwyo..",
            "..oywwwwyo..",
            "..oyyyyyyo..",
            "...oooooo...",
            "..oyyyyyyo..",
            "...oooooo..."},
            Map.of('y', 0xffb88a30, 'w', 0xffffe080, 'W', 0xffffffff));
        write(template, port, "rayon_du_phare", new String[] {
            "y....oo....y",
            ".y..owwo..y.",
            "..yyowwoyy..",
            "....oooo....",
            "....orro....",
            "....owwo....",
            "...orrrro...",
            "...owwwwo...",
            "...orrrro...",
            "..owwwwwwo..",
            "..oooooooo..",
            "bbbbbbbbbbbb"},
            Map.of('y', 0xffffe080, 'w', 0xfff0ead0, 'r', 0xffc0283a, 'b', 0xff2a6a9a));
        write(template, port, "sabre_d_abordage", new String[] {
            "..........o.",
            ".........oso",
            "........osso",
            ".......osso.",
            "......osso..",
            ".....osso...",
            "....osso....",
            ".yo.oso.....",
            "..yoso......",
            "..oyyo......",
            ".obo.yo.....",
            "obo........."},
            Map.of('s', 0xffd8e0ec, 'y', 0xffe8b048, 'b', 0xff5e3a18));
        write(template, port, "pavillon_noir", new String[] {
            "b...........",
            "booooooooooo",
            "bokkkkkkkkko",
            "bokkkwwwkkko",
            "bokkwwwwwkko",
            "bokkwkwkwkko",
            "bokkkwwwkkko",
            "bokkwkkkwkko",
            "bokkkkkkkkko",
            "booooooooooo",
            "b...........",
            "b..........."},
            Map.of('b', 0xff8a5a2a, 'k', 0xff1a1a1a, 'w', 0xfff0ead0));
        write(template, port, "mutinerie", new String[] {
            "o..........o",
            "so........so",
            ".so......so.",
            "..so....so..",
            "...so..so...",
            "....soso....",
            ".....ss.....",
            "....soso....",
            "..yoo..ooy..",
            "..oyo..oyo..",
            ".obo....obo.",
            "obo......obo"},
            Map.of('s', 0xffd8e0ec, 'y', 0xffe8b048, 'b', 0xff5e3a18));
        write(template, mines, "tamis", new String[] {
            "............",
            "..oooooooo..",
            ".ossssssSSo.",
            "osgsgsgsgsSo",
            "ossyssgssySo",
            "osgsgsyssgSo",
            ".ossssssSSo.",
            "..oooooooo..",
            "....y..y....",
            "......y.....",
            "...y........",
            "............"},
            Map.of('s', 0xffb8a888, 'S', 0xff7a6a4a, 'g', 0xff4a3a2a, 'y', 0xffffd54a));
        write(template, mines, "etai", new String[] {
            "oooooooooooo",
            "obbbbbbbbbbo",
            "oBBBBBBBBBBo",
            "oooooooooooo",
            "..obo..obo..",
            "..obo..obo..",
            "..obo..obo..",
            "..obo..obo..",
            "..obo..obo..",
            "..obo..obo..",
            "oooooooooooo",
            "rrrrrrrrrrrr"},
            Map.of('b', 0xffb07a3a, 'B', 0xff7a5020, 'r', 0xff5a4a42));
        write(template, mines, "corde_de_rappel", new String[] {
            "....ooo.....",
            "...os.so....",
            "...o...o....",
            "....o.......",
            "...ooooo....",
            "..obbbbbo...",
            ".obBoooBbo..",
            ".obo...obo..",
            ".obBoooBbo..",
            "..obbbbbo...",
            "...ooooo....",
            "............"},
            Map.of('s', 0xffc8d0dc, 'b', 0xffd8b070, 'B', 0xff9a7030));
        write(template, mines, "marteau_de_forge", new String[] {
            "..oooooooo..",
            ".osssssssSo.",
            ".osssssssSo.",
            ".oSSSSSSSSo.",
            "..oooobooo..",
            ".....obo....",
            ".....obo....",
            ".....obo....",
            ".....obo....",
            ".....oBo....",
            ".....oBo....",
            "......o....."},
            Map.of('s', 0xff9aa4b4, 'S', 0xff5a6474, 'b', 0xff8a5a2a, 'B', 0xff5e3a18));
        write(template, mines, "trempe", new String[] {
            "....w..w....",
            "...w..w..w..",
            "..........o.",
            ".........oro",
            "........orRo",
            ".......orRo.",
            "......orRo..",
            ".....orRo...",
            "..o.orRo....",
            "..oyoRo.....",
            "...oyo......",
            "bbbobobbbbbb"},
            Map.of('w', 0xffe8eef4, 'r', 0xffff5020, 'R', 0xffffb040, 'y', 0xffe8b048, 'b', 0xff2a6a9a));
        write(template, mines, "lame_forgee", new String[] {
            ".....oo.....",
            "....osSo....",
            "....osSo....",
            "....osSo....",
            "....osSo....",
            "....osSo....",
            "....osSo....",
            "..oooooooo..",
            "..oyyyyyYo..",
            "..oooooooo..",
            ".....obo....",
            ".....ooo...."},
            Map.of('s', 0xffe8eef4, 'S', 0xff9aa4b4, 'y', 0xffffd54a, 'Y', 0xffc4891e, 'b', 0xff5e3a18));
        write(template, mines, "dynamite", new String[] {
            "........o.y.",
            ".......o.yr.",
            "......o..y..",
            ".oooooooo...",
            ".orrrrrRo...",
            ".owwwwwwo...",
            ".orrrrrRo...",
            ".orrrrrRo...",
            ".owwwwwwo...",
            ".orrrrrRo...",
            ".oooooooo...",
            "............"},
            Map.of('r', 0xffd8202c, 'R', 0xff8a1018, 'w', 0xfff0ead0, 'y', 0xffffd040));
        write(template, mines, "lampe_a_carbure", new String[] {
            "....oooo....",
            "...oyyyyo...",
            "..oywwwwyo..",
            ".oyywWWwyyo.",
            ".oyywWWwyyo.",
            "..oywwwwyo..",
            "...oyyyyo...",
            "....oyyo....",
            "...oyYYyo...",
            "..oyYYYYyo..",
            "..oyyyyyyo..",
            "...oooooo..."},
            Map.of('y', 0xffb88a30, 'Y', 0xff7a5a20, 'w', 0xffffe080, 'W', 0xffffffff));
        write(template, mines, "coeur_d_or", new String[] {
            "............",
            "..ooo..ooo..",
            ".oyyyooyyyo.",
            "oywyyyyyyyYo",
            "oywyyyyyyyYo",
            "oyyyyyyyyyYo",
            ".oyyyyyyyYo.",
            "..oyyyyyYo..",
            "...oyyyYo...",
            "....oyYo....",
            ".....oo.....",
            "............"},
            Map.of('y', 0xffffd54a, 'Y', 0xffc4891e, 'w', 0xfffff2a0));
        write(template, casino, "bouchons_d_oreille", new String[] {
            "............",
            ".ooo........",
            "ozzzo.......",
            "ozzZo..ooo..",
            "ozzZo.ozzzo.",
            "ozzZo.ozzZo.",
            "ozzZo.ozzZo.",
            ".ooo..ozzZo.",
            "......ozzZo.",
            ".......ooo..",
            "............",
            "............"},
            Map.of('z', 0xfff09040, 'Z', 0xffb05a18));
        write(template, casino, "cocktail_des_abysses", new String[] {
            ".........oo.",
            "oooooooooo..",
            "otttttttgto.",
            ".otttttgto..",
            "..otttttto..",
            "...otttto...",
            "....otto....",
            ".....oo.....",
            ".....ww.....",
            ".....ww.....",
            "...owwwwo...",
            "...oooooo..."},
            Map.of('t', 0xff3ad0c8, 'g', 0xff6ab030, 'w', 0xffd8eef4));
        write(template, casino, "piece_truquee", new String[] {
            "...oooooo...",
            "..oyyyyyyo..",
            ".oyyYYYYyyo.",
            "oyyYyyyyYyYo",
            "oyyyyyyYyyYo",
            "oyyyyyYyyyYo",
            "oyyyyyYyyyYo",
            "oyyyyyyyyyYo",
            "oyyyyyYyyyYo",
            ".oyyyyyyyYo.",
            "..oYYYYYYo..",
            "...oooooo..."},
            Map.of('y', 0xffffd54a, 'Y', 0xffa0701a));
        write(template, casino, "levier_rouille", new String[] {
            "........ooo.",
            ".......orrro",
            ".......orRro",
            "........ooo.",
            ".........oo.",
            "........ono.",
            ".......ono..",
            "......ono...",
            ".ooooonoo...",
            ".onnnnnno...",
            ".oNNNNNNo...",
            ".oooooooo..."},
            Map.of('r', 0xffd8202c, 'R', 0xffff8080, 'n', 0xffa85a28, 'N', 0xff6a3418));
        write(template, casino, "jackpot_englouti", new String[] {
            "...w....w...",
            ".w....w....w",
            "oooooooooooo",
            "o.rr.rr.rr.o",
            "o..r..r..r.o",
            "o..r..r..r.o",
            "o.r..r..r..o",
            "o.r..r..r..o",
            "oooooooooooo",
            "..bbbbbbbb..",
            ".bbbbbbbbbb.",
            "............"},
            Map.of('w', 0xffd8eef4, 'r', 0xffd8202c, 'b', 0xff2a8a8a));
        write(template, casino, "cage_a_requin", new String[] {
            "oooooooooooo",
            "os.s.s.s.s.o",
            "os.s.s.s.s.o",
            "os.sgs.s.s.o",
            "os.ggs.s.s.o",
            "osgggs.s.s.o",
            "os.s.s.s.s.o",
            "os.s.s.s.s.o",
            "oooooooooooo",
            "bbbbbbbbbbbb",
            ".bbbbbbbbbb.",
            "............"},
            Map.of('s', 0xffc8d0dc, 'g', 0xff6a7a8a, 'b', 0xff2a8a8a));
        write(template, casino, "coffre_de_l_epave", new String[] {
            "............",
            "..oooooooo..",
            ".obbbbbbbbo.",
            "obBBBBBBBBbo",
            "oooooooooooo",
            "obbbbyybbbbo",
            "obbbbyYbbbbo",
            "obbbbbbbbbbo",
            "oBBBBBBBBBBo",
            "oooooooooooo",
            "..g......g..",
            ".ggg....ggg."},
            Map.of('b', 0xff8a5a2a, 'B', 0xff5e3a18, 'y', 0xffffd54a, 'Y', 0xffc4891e, 'g', 0xff3aa060));
        write(template, casino, "harpon", new String[] {
            "..........oo",
            ".........oso",
            "........osso",
            ".......oSSo.",
            "......obo...",
            ".....obo....",
            "....obo.....",
            "...obo......",
            "..obo.......",
            ".obo........",
            "obo.........",
            "oo.........."},
            Map.of('s', 0xffe8eef4, 'S', 0xff9aa4b4, 'b', 0xff8a5a2a));
        write(template, casino, "ancre", new String[] {
            ".....oo.....",
            "....osSo....",
            ".....oo.....",
            "..ooossooo..",
            "..oSSssSSo..",
            "..ooossooo..",
            ".....ss.....",
            "o....ss....o",
            "so...ss...so",
            ".so..ss..so.",
            "..sssssSSs..",
            "...oooooo..."},
            Map.of('s', 0xffb8c0cc, 'S', 0xff6a7484));
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
