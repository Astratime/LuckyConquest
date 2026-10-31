package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.Card;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class PortraitBlinkTest {

    private static final int SKIN = 0xf0c8a0ff, EYE = 0x1a1018ff, OUTLINE = 0x22141aff;

    /** Un visage carré détouré, avec deux yeux de 2 x 2 pixels. */
    private static int[][] face(boolean withEyes) {
        int[][] g = new int[20][16];
        for (int y = 2; y < 18; y++) {
            for (int x = 2; x < 14; x++) {
                boolean edge = y == 2 || y == 17 || x == 2 || x == 13;
                g[y][x] = edge ? OUTLINE : SKIN;
            }
        }
        if (withEyes) {
            for (int[] p : new int[][] {{5, 6}, {6, 6}, {5, 7}, {6, 7}, {9, 6}, {10, 6}, {9, 7}, {10, 7}}) {
                g[p[1]][p[0]] = EYE;
            }
        }
        return g;
    }

    @Test
    void closesBothEyesWithTheSkinAndALidLine() {
        int[][] closed = PortraitBlink.closeEyes(face(true));
        assertNotNull(closed);
        assertEquals(SKIN, closed[6][5]);
        assertEquals(SKIN, closed[6][10]);
        assertNotEquals(EYE, closed[7][5]);
        assertNotEquals(SKIN, closed[7][5]); // la paupière : un trait plus sombre que la peau
        assertEquals(OUTLINE, closed[2][5]); // le contour ne bouge pas
    }

    @Test
    void noEyesNoBlink() {
        assertNull(PortraitBlink.closeEyes(face(false)));
    }

    @Test
    void findsTheEyesOfAPrairieSoldier() {
        assertNotNull(PortraitBlink.closeEyes(ExplorationArt.soldier(Card.Suit.PIQUE)));
    }
}
