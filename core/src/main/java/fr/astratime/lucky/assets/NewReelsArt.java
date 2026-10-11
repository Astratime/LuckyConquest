package fr.astratime.lucky.assets;

import static fr.astratime.lucky.assets.PixelCanvas.rgba;

/**
 * Dessins en pixel art des rouleaux ajoutés aux ennemis qui n'avaient que des
 * rouleaux de base : Croupier en chef, Portier, Maison, Machine Originelle,
 * Golem d'or, Cœur de la Montagne et Bandit manchot. Chaque méthode rend une
 * grille 16 x 16 de couleurs RGBA8888 (0 = transparent).
 */
final class NewReelsArt {

    private static final int GOLD = rgba("ffd54a"), GOLD_DARK = rgba("c4891e"), GOLD_LIGHT = rgba("fff6c8");
    private static final int STEEL = rgba("b8c0cc"), STEEL_DARK = rgba("7a8494"), STEEL_LIGHT = rgba("e8eef8");
    private static final int WOOD = rgba("8a5a2a"), WOOD_DARK = rgba("5e3a18");
    private static final int RED = rgba("d8202c"), RED_DARK = rgba("8a1a1a");
    private static final int GREEN = rgba("1f7a3a"), GREEN_DARK = rgba("12502a");
    private static final int STONE = rgba("8a8070"), STONE_DARK = rgba("5a5248");
    private static final int WHITE = rgba("f6f0e2"), BLACK = rgba("1a1018");

    private NewReelsArt() {}

    /** Rien ne va plus : un jeton de casino barré d'une croix rouge. */
    static int[][] nothingGoes() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.ellipseShaded(7.5f, 7.5f, 6.5f, 6.5f, GREEN, GREEN_DARK, 2f);
        g.ellipse(7.5f, 7.5f, 4f, 4f, WHITE);
        g.ellipse(7.5f, 7.5f, 3f, 3f, GREEN);
        for (int i = 0; i < 4; i++) { // encoches blanches du jeton
            g.set(7 + (i % 2), i < 2 ? 1 : 14, WHITE);
            g.set(i < 2 ? 1 : 14, 7 + (i % 2), WHITE);
        }
        for (int i = 2; i <= 13; i++) {
            g.set(i, i, RED);
            g.set(i + 1, i, RED_DARK);
            g.set(15 - i, i, RED);
            g.set(14 - i, i, RED_DARK);
        }
        return g.outlined();
    }

    /** Râteau : le râteau de croupier en bois, qui tire une pile de jetons dorés. */
    static int[][] rake() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.line(2, 1, 11, 10, WOOD);
        g.line(3, 1, 12, 10, WOOD_DARK);
        g.rect(8, 10, 15, 11, WOOD_DARK);
        g.rect(8, 10, 15, 10, WOOD);
        g.rect(1, 12, 6, 13, GOLD);
        g.rect(1, 13, 6, 13, GOLD_DARK);
        g.rect(2, 10, 5, 11, GOLD);
        g.rect(2, 11, 5, 11, GOLD_DARK);
        g.rect(1, 14, 6, 15, GOLD);
        g.set(3, 12, GOLD_LIGHT);
        return g.outlined();
    }

    /** Vestiaire : un cintre où pend un ticket numéroté. */
    static int[][] cloakroom() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.line(7, 0, 8, 0, STEEL);
        g.set(8, 1, STEEL);
        g.set(7, 2, STEEL);
        g.line(7, 3, 1, 7, WOOD);
        g.line(8, 3, 14, 7, WOOD);
        g.line(1, 7, 14, 7, WOOD_DARK);
        g.line(7, 7, 7, 9, STEEL_DARK);
        g.rect(4, 9, 11, 15, rgba("f0d8a0"));
        g.rect(10, 9, 11, 15, rgba("c8b078"));
        g.rect(6, 11, 9, 13, RED);
        g.set(7, 12, WHITE);
        return g.outlined();
    }

    /** Videur : un poing ganté de noir, prêt à frapper. */
    static int[][] bouncer() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int glove = rgba("3a3440"), gloveLight = rgba("6a6070");
        g.rect(3, 4, 12, 11, glove);
        g.rect(3, 4, 12, 5, gloveLight);
        for (int x = 4; x <= 10; x += 3) g.line(x, 4, x, 7, BLACK);
        g.rect(1, 7, 4, 10, glove);
        g.rect(1, 7, 4, 7, gloveLight);
        g.rect(5, 11, 11, 15, rgba("2a2430"));
        g.rect(5, 12, 11, 12, GOLD);
        return g.outlined();
    }

    /** Mur porteur : un pan de briques rouges, un pilier de pierre au milieu. */
    static int[][] loadBearing() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int brick = rgba("b0503a"), brickDark = rgba("7a3426"), mortar = rgba("d8c8b0");
        g.rect(0, 2, 15, 15, mortar);
        for (int row = 0; row < 5; row++) {
            int y = 3 + row * 3, shift = row % 2 == 0 ? 0 : 2;
            for (int x = -2 + shift; x < 16; x += 4) {
                g.rect(Math.max(0, x), y, Math.min(15, x + 2), y + 1, brick);
                g.rect(Math.max(0, x), y + 1, Math.min(15, x + 2), y + 1, brickDark);
            }
        }
        g.rect(6, 0, 9, 15, STONE);
        g.rect(8, 0, 9, 15, STONE_DARK);
        g.rect(5, 0, 10, 1, STONE_DARK);
        return g.outlined();
    }

    /** Coffre-fort : une porte d'acier, sa molette dorée. */
    static int[][] safe() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.rect(1, 1, 14, 14, STEEL);
        g.rect(10, 1, 14, 14, STEEL_DARK);
        g.rect(1, 1, 14, 2, STEEL_LIGHT);
        g.ellipseShaded(7f, 8f, 4f, 4f, GOLD, GOLD_DARK, 1.5f);
        g.ellipse(7f, 8f, 1.5f, 1.5f, BLACK);
        g.line(7, 4, 7, 5, GOLD_LIGHT);
        g.rect(12, 6, 13, 10, BLACK);
        g.rect(2, 15, 4, 15, STEEL_DARK);
        g.rect(11, 15, 13, 15, STEEL_DARK);
        return g.outlined();
    }

    /** Avantage de la maison : une roulette dont la case verte domine, une flèche vers la Maison. */
    static int[][] houseEdge() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.ellipseShaded(7.5f, 7.5f, 7f, 7f, WOOD, WOOD_DARK, 2f);
        g.ellipse(7.5f, 7.5f, 5.5f, 5.5f, BLACK);
        g.triangle(7, 7, 2, 2, 12, 2, RED);
        g.triangle(7, 7, 13, 3, 13, 12, BLACK);
        g.triangle(8, 8, 2, 13, 13, 13, GREEN);
        g.triangle(7, 7, 2, 3, 2, 12, RED);
        g.ellipse(7.5f, 7.5f, 1.5f, 1.5f, GOLD);
        g.set(11, 11, WHITE);
        return g.outlined();
    }

    /** Engrenage : une roue dentée de laiton. */
    static int[][] gear() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int brass = rgba("d0a040"), brassDark = rgba("8a6420");
        int[][] teeth = {{6, 0, 9, 2}, {6, 13, 9, 15}, {0, 6, 2, 9}, {13, 6, 15, 9},
            {2, 2, 4, 4}, {11, 2, 13, 4}, {2, 11, 4, 13}, {11, 11, 13, 13}};
        for (int[] t : teeth) g.rect(t[0], t[1], t[2], t[3], brass);
        g.ellipseShaded(7.5f, 7.5f, 5.5f, 5.5f, brass, brassDark, 2f);
        g.ellipse(7.5f, 7.5f, 2f, 2f, BLACK);
        g.set(5, 4, GOLD_LIGHT);
        return g.outlined();
    }

    /** Court-circuit : un éclair bleu qui fend une ampoule. */
    static int[][] shortCircuit() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int glass = rgba("f0f0c8"), blue = rgba("60c0ff"), blueLight = rgba("d0f0ff");
        g.ellipseShaded(7.5f, 6f, 5f, 5.5f, glass, rgba("c8c8a0"), 2f);
        g.rect(5, 11, 10, 14, STEEL);
        g.rect(5, 12, 10, 12, STEEL_DARK);
        g.rect(6, 15, 9, 15, STEEL_DARK);
        g.line(9, 0, 6, 5, blue);
        g.line(6, 5, 10, 6, blue);
        g.line(10, 6, 5, 12, blue);
        g.set(8, 2, blueLight);
        g.set(8, 6, blueLight);
        return g.outlined();
    }

    /** Comète : la Comète Dorée qui tombe, sa traînée derrière elle. */
    static int[][] goldenComet() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int orange = rgba("e8742a");
        for (int i = 0; i < 9; i++) {
            g.set(i, i, orange);
            g.set(i + 1, i, GOLD_DARK);
            g.set(i, i + 1, GOLD_DARK);
        }
        g.ellipseShaded(11f, 11f, 4f, 4f, GOLD, GOLD_DARK, 1.5f);
        g.ellipse(10f, 10f, 1.5f, 1.5f, GOLD_LIGHT);
        return g.outlined();
    }

    /** Aspiration : un tourbillon violet qui avale un petit rouleau. */
    static int[][] aspiration() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int violet = rgba("8a3ad8"), violetLight = rgba("c890ff"), violetDark = rgba("4a1a80");
        g.ellipse(7.5f, 7.5f, 7f, 7f, violetDark);
        g.ellipse(7.5f, 7.5f, 5f, 5f, violet);
        g.ellipse(7.5f, 7.5f, 3f, 3f, violetLight);
        g.ellipse(7.5f, 7.5f, 1.5f, 1.5f, BLACK);
        g.line(1, 7, 4, 3, violetLight);
        g.line(14, 8, 11, 12, violetLight);
        g.rect(9, 1, 13, 4, WHITE); // le rouleau aspiré
        g.rect(9, 2, 13, 2, RED);
        return g.outlined();
    }

    /** Placage : un lingot plaqué d'or, au reflet éclatant. */
    static int[][] plating() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.rect(1, 3, 14, 12, GOLD);
        g.rect(1, 9, 14, 12, GOLD_DARK);
        g.rect(1, 3, 14, 4, GOLD_LIGHT);
        g.line(3, 6, 6, 6, GOLD_LIGHT);
        g.line(4, 7, 5, 7, GOLD_LIGHT);
        g.set(12, 1, GOLD_LIGHT); g.set(11, 1, GOLD); g.set(13, 1, GOLD); g.set(12, 0, GOLD); g.set(12, 2, GOLD);
        return g.outlined();
    }

    /** Lingot : un lingot d'or massif en trapèze, la tranche ombrée. */
    static int[][] ingot() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.triangle(0, 13, 4, 6, 4, 13, GOLD_DARK);
        g.triangle(15, 13, 11, 6, 11, 13, GOLD_DARK);
        g.rect(4, 6, 11, 13, GOLD);
        g.rect(0, 13, 15, 14, GOLD_DARK);
        g.rect(4, 6, 11, 7, GOLD_LIGHT);
        g.set(6, 9, GOLD_LIGHT);
        g.set(7, 9, GOLD_LIGHT);
        return g.outlined();
    }

    /** Éboulement : des rochers qui dévalent, un nuage de poussière. */
    static int[][] rockfall() {
        PixelCanvas g = new PixelCanvas(16, 16);
        int dust = rgba("c8b898");
        g.ellipse(4f, 13f, 4f, 2.5f, dust);
        g.ellipse(12f, 13f, 3.5f, 2.5f, dust);
        g.ellipseShaded(5f, 9f, 3.5f, 3f, STONE, STONE_DARK, 1f);
        g.ellipseShaded(11f, 10f, 3f, 3f, STONE, STONE_DARK, 1f);
        g.ellipseShaded(8f, 3.5f, 3f, 2.5f, STONE, STONE_DARK, 1f);
        g.line(9, 7, 10, 7, STONE_DARK);
        g.set(4, 8, rgba("b0a898"));
        return g.outlined();
    }

    /** Battement : un cœur de pierre veiné d'or, qui bat. */
    static int[][] heartbeat() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.ellipseShaded(4.5f, 5f, 4f, 4f, STONE, STONE_DARK, 1.5f);
        g.ellipseShaded(11f, 5f, 4f, 4f, STONE, STONE_DARK, 1.5f);
        g.triangle(1, 6, 15, 6, 8, 15, STONE);
        g.triangle(8, 6, 15, 6, 8, 15, STONE_DARK);
        g.line(4, 4, 7, 8, GOLD);
        g.line(7, 8, 10, 6, GOLD);
        g.line(10, 6, 8, 12, GOLD);
        g.set(3, 3, GOLD_LIGHT);
        return g.outlined();
    }

    /** Pièce avalée : une fente de machine à sous où tombe une pièce d'or. */
    static int[][] swallowedCoin() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.rect(0, 10, 15, 15, STEEL);
        g.rect(0, 13, 15, 15, STEEL_DARK);
        g.rect(4, 11, 11, 12, BLACK);
        g.ellipseShaded(7.5f, 5f, 4f, 4.5f, GOLD, GOLD_DARK, 1.5f);
        g.rect(7, 3, 8, 7, GOLD_DARK);
        g.set(6, 2, GOLD_LIGHT);
        return g.outlined();
    }

    /** Bras armé : le levier de la machine, tiré à fond, sa boule rouge en haut. */
    static int[][] armedArm() {
        PixelCanvas g = new PixelCanvas(16, 16);
        g.rect(1, 9, 6, 15, STEEL);
        g.rect(4, 9, 6, 15, STEEL_DARK);
        g.line(5, 11, 12, 4, STEEL_LIGHT);
        g.line(5, 12, 12, 5, STEEL_DARK);
        g.ellipseShaded(12.5f, 3f, 2.8f, 2.8f, RED, RED_DARK, 1f);
        g.set(12, 2, rgba("ff8a8a"));
        return g.outlined();
    }
}
