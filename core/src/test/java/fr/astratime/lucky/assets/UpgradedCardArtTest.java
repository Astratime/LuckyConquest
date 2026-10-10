package fr.astratime.lucky.assets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Le liseré doré des cartes « + ». */
class UpgradedCardArtTest {

    private static final int WHITE = 0xffffffff;

    /** Carte blanche de 100 x 140, aux coins transparents. */
    private static int[] card() {
        int[] pixels = new int[100 * 140];
        java.util.Arrays.fill(pixels, WHITE);
        pixels[0] = 0;
        pixels[99] = 0;
        return pixels;
    }

    @Test
    void theEdgeTurnsGoldAndTheMiddleStaysTheSame() {
        int[] gilded = UpgradedCardArt.gild(card(), 100, 140);

        int edge = gilded[70 * 100 + 2];
        assertNotEquals(WHITE, edge, "le bord change");
        assertTrue((edge >>> 24) > (edge >>> 8 & 0xff), "plus rouge que bleu : de l'or");
        assertEquals(WHITE, gilded[70 * 100 + 50], "le milieu ne change pas");
        assertEquals(0, gilded[0], "les coins transparents le restent");
    }

    @Test
    void theBadgeSitsInTheTopRightCorner() {
        int[] gilded = UpgradedCardArt.gild(card(), 100, 140);
        int radius = Math.round(100 * UpgradedCardArt.BADGE);
        int center = Math.round(100 - 100 * UpgradedCardArt.BORDER * 0.5f - radius * 0.95f);
        assertEquals(WHITE, gilded[Math.round(100 * UpgradedCardArt.BORDER * 0.5f + radius * 0.95f) * 100 + center],
            "le centre du « + » est blanc");
        assertNotEquals(WHITE, gilded[(radius) * 100 + center + radius / 2 + 1], "la pastille est dorée autour");
    }
}
