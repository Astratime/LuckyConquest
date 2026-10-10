package fr.astratime.lucky.assets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Les enseignes renforcées : Carreau bleu, Trèfle vert, Cœur et Pique inchangés. */
class SuitColorsTest {

    private static final int RED    = 0xac3232ff; // le rouge des Carreaux et des Cœurs
    private static final int INK    = 0x222034ff; // l'encre des Trèfles et des Piques du jeu clair
    private static final int SILVER = 0xccccccff; // le gris des Trèfles et des Piques du jeu sombre
    private static final int PAPER  = 0xdadadaff;

    @Test
    void diamondsTurnBlueAndClubsGreenInBothDecks() {
        int[] diamond = SuitColors.recolor("cards/light/7-D.png", new int[] { RED, PAPER });
        assertTrue(blue(diamond[0]) > red(diamond[0]), "Carreau bleu");
        assertEquals(PAPER, diamond[1], "le fond ne change pas");

        int[] club = SuitColors.recolor("cards/light/12-C.png", new int[] { INK });
        assertTrue(green(club[0]) > red(club[0]) && green(club[0]) > blue(club[0]), "Trèfle vert");

        int[] enemyClub = SuitColors.recolor("cards/dark/K-C.png", new int[] { SILVER, INK });
        assertTrue(green(enemyClub[0]) > red(enemyClub[0]), "Trèfle de l'ennemi vert");
        assertEquals(INK, enemyClub[1], "le cadre sombre de la carte ennemie ne change pas");
        assertTrue(blue(SuitColors.recolor("cards/dark/A-D.png", new int[] { RED })[0]) > RED >>> 24);
    }

    @Test
    void heartsSpadesAndOtherImagesStayTheSame() {
        int[] pixels = { RED, INK, SILVER };
        assertSame(pixels, SuitColors.recolor("cards/light/7-H.png", pixels));
        assertSame(pixels, SuitColors.recolor("cards/dark/7-P.png", pixels));
        assertSame(pixels, SuitColors.recolor("cards/light/BACK.png", pixels));
        assertFalse(SuitColors.recolors("cards/special/bingo.png"));
        assertTrue(SuitColors.recolors("cards/light/1-D.png"));
    }

    private static int red(int rgba)   { return rgba >>> 24; }
    private static int green(int rgba) { return rgba >>> 16 & 0xff; }
    private static int blue(int rgba)  { return rgba >>> 8 & 0xff; }
}
