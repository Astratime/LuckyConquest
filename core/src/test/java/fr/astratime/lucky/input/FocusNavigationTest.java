package fr.astratime.lucky.input;

import com.badlogic.gdx.math.Rectangle;
import fr.astratime.lucky.input.FocusNavigation.Direction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FocusNavigationTest {

    /** Une colonne de trois boutons (menu) et un bouton à droite du deuxième. */
    private static final Rectangle TOP = new Rectangle(100, 300, 200, 40);
    private static final Rectangle MID = new Rectangle(100, 240, 200, 40);
    private static final Rectangle LOW = new Rectangle(100, 180, 200, 40);
    private static final Rectangle SIDE = new Rectangle(400, 245, 80, 30);
    private static final List<Rectangle> ALL = List.of(TOP, MID, LOW, SIDE);

    @Test
    void upAndDownFollowTheColumn() {
        assertEquals(2, FocusNavigation.pick(MID, ALL, Direction.DOWN));
        assertEquals(0, FocusNavigation.pick(MID, ALL, Direction.UP));
        assertEquals(1, FocusNavigation.pick(TOP, ALL, Direction.DOWN));
    }

    @Test
    void rightGoesToTheSideButtonAndLeftComesBack() {
        assertEquals(3, FocusNavigation.pick(MID, ALL, Direction.RIGHT));
        assertEquals(1, FocusNavigation.pick(SIDE, ALL, Direction.LEFT));
    }

    @Test
    void nothingBeyondTheEdge() {
        assertEquals(-1, FocusNavigation.pick(TOP, ALL, Direction.UP));
        assertEquals(-1, FocusNavigation.pick(SIDE, ALL, Direction.RIGHT));
    }

    @Test
    void alignedNeighbourBeatsCloserButFarSideOne() {
        // Main de cartes : la carte juste à droite, pas celle de la rangée du dessus.
        Rectangle card1 = new Rectangle(0, 0, 60, 90);
        Rectangle card2 = new Rectangle(70, 0, 60, 90);
        Rectangle above = new Rectangle(40, 120, 30, 30);
        assertEquals(1, FocusNavigation.pick(card1, List.of(card1, card2, above), Direction.RIGHT));
    }

    @Test
    void wideRowDoesNotJumpRightToATabAbove() {
        // Options : à droite d'une longue ligne, pas d'élément → la flèche part au réglage.
        Rectangle row = new Rectangle(652, 340, 615, 80);
        Rectangle tab = new Rectangle(1138, 700, 150, 60);
        assertEquals(-1, FocusNavigation.pick(row, List.of(row, tab), Direction.RIGHT));
    }

    @Test
    void nearestFromPointer() {
        assertEquals(3, FocusNavigation.nearest(450, 250, ALL));
    }
}
