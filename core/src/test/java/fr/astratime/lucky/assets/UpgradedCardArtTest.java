package fr.astratime.lucky.assets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Le liseré doré des cartes « + ». */
class UpgradedCardArtTest {

    private static final int WHITE = 0xffffffff;
    private static final int GRAY  = 0x808080ff;

    /** Carte grise de 100 x 140, aux coins transparents. */
    private static int[] card() {
        int[] pixels = new int[100 * 140];
        java.util.Arrays.fill(pixels, GRAY);
        pixels[0] = 0;
        pixels[99] = 0;
        return pixels;
    }

    @Test
    void theEdgeTurnsGoldAndTheMiddleStaysTheSame() {
        int[] gilded = UpgradedCardArt.gild(card(), 100, 140);

        int edge = gilded[70 * 100 + 2];
        assertNotEquals(GRAY, edge, "le bord change");
        assertTrue((edge >>> 24) > (edge >>> 8 & 0xff), "plus rouge que bleu : de l'or");
        assertEquals(GRAY, gilded[70 * 100 + 50], "le milieu ne change pas");
        assertEquals(0, gilded[0], "les coins transparents le restent");
    }

    @Test
    void theBadgeIsAPixelCoinWithAWhitePlusAndABlackOutline() {
        int middle = UpgradedCardArt.BADGE_CELLS / 2;
        assertEquals(WHITE, UpgradedCardArt.badgeCell(middle, middle), "le « + » au centre");
        assertEquals(0, UpgradedCardArt.badgeCell(0, 0), "les coins restent la carte");
        int edge = UpgradedCardArt.badgeCell(middle, 0);
        assertTrue((edge >>> 24) < 0x30, "un contour noir sur le bord");
    }

    @Test
    void theBadgeSitsInTheTopRightCorner() {
        int[] gilded = UpgradedCardArt.gild(card(), 100, 140);
        assertEquals(GRAY, gilded[70 * 100 + 50], "loin de la pastille, rien ne change");
        boolean found = false;
        for (int x = 70; x < 100 && !found; x++) {
            for (int y = 0; y < 30; y++) if (gilded[y * 100 + x] == WHITE) found = true;
        }
        assertTrue(found, "le « + » blanc est en haut à droite");
    }
}
