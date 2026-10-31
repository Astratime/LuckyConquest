package fr.astratime.lucky.views;

import com.badlogic.gdx.math.Vector2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Vue de face : la table devient un trapèze, et un clic à l'écran retombe au bon endroit de la table. */
class TableProjectionTest {

    private static TableProjection projection() {
        TableProjection projection = new TableProjection();
        projection.update(350f, 100f, 1230f, 900f, 1080f);
        projection.setEnabled(true);
        return projection;
    }

    @Test
    void tableCornersBecomeATrapezoid() {
        TableProjection p = projection();
        Vector2 bottomLeft = p.project(new Vector2(350f, 100f));
        Vector2 topLeft    = p.project(new Vector2(350f, 1000f));
        Vector2 topRight   = p.project(new Vector2(1580f, 1000f));
        assertEquals(100f - TableProjection.BOTTOM_DROP, bottomLeft.y, 0.01f);
        assertEquals(1080f * TableProjection.TOP_HEIGHT, topLeft.y, 0.01f);
        assertEquals(p.getTopLeftX(), topLeft.x, 0.01f);
        assertEquals(p.getTopRightX(), topRight.x, 0.01f);
        assertTrue(topRight.x - topLeft.x < 1230f, "le bord de l'ennemi est plus étroit");
    }

    @Test
    void clicksComeBackToTheSamePointOfTheTable() {
        TableProjection p = projection();
        for (float x = 360f; x < 1580f; x += 137f) {
            for (float y = 110f; y < 1000f; y += 97f) {
                Vector2 back = p.unproject(p.project(new Vector2(x, y)));
                assertEquals(x, back.x, 0.05f);
                assertEquals(y, back.y, 0.05f);
            }
        }
    }

    @Test
    void farSideIsSmallerAndPointsOffTheTableStayPut() {
        TableProjection p = projection();
        assertTrue(p.scaleAt(965f, 950f) < p.scaleAt(965f, 150f));
        Vector2 sidePanel = p.toScreen(150f, 700f);
        assertEquals(new Vector2(150f, 700f), sidePanel);
        p.setEnabled(false);
        assertEquals(new Vector2(900f, 500f), p.toScreen(900f, 500f), "vue classique : rien ne bouge");
    }
}
