package fr.astratime.lucky.input;

import com.badlogic.gdx.math.Rectangle;

import java.util.List;

/**
 * Choix de l'élément suivant quand la manette pousse la croix ou le stick
 * gauche : parmi les éléments cliquables de l'écran, celui qui est le plus
 * proche dans cette direction. Calcul pur (rectangles en coordonnées de la
 * scène, y vers le haut), testé sans écran.
 */
public final class FocusNavigation {

    /** Les quatre directions de la croix, en coordonnées de la scène (y vers le haut). */
    public enum Direction {
        UP(0, 1), DOWN(0, -1), LEFT(-1, 0), RIGHT(1, 0);

        public final int dx, dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        boolean horizontal() { return dx != 0; }
    }

    /** Écart minimal (en unités de la scène) pour qu'un élément compte comme « plus loin » dans la direction. */
    private static final float MIN_ALONG = 2f;
    /** Un élément aligné (qui partage une bande avec le point de départ) passe devant un élément décalé. */
    private static final float ALIGNED_SIDE_WEIGHT = 0.5f;
    private static final float SIDE_WEIGHT = 2.5f;
    /** Au-delà de ce décalage (rapport côté / avance), un élément non aligné n'est pas « dans cette direction ». */
    private static final float MAX_SIDE_RATIO = 1.75f;

    private FocusNavigation() { }

    /**
     * @param from       l'élément actuel (ou un petit rectangle autour du pointeur)
     * @param candidates les éléments cliquables de l'écran
     * @param direction  la direction poussée
     * @return la place dans {@code candidates} du meilleur élément dans cette direction, ou -1 s'il n'y en a pas
     */
    public static int pick(Rectangle from, List<Rectangle> candidates, Direction direction) {
        float fx = from.x + from.width / 2f, fy = from.y + from.height / 2f;
        int best = -1;
        float bestScore = Float.MAX_VALUE;
        for (int i = 0; i < candidates.size(); i++) {
            Rectangle r = candidates.get(i);
            float cx = r.x + r.width / 2f, cy = r.y + r.height / 2f;
            float dx = cx - fx, dy = cy - fy;
            float along = dx * direction.dx + dy * direction.dy;
            if (along < MIN_ALONG) continue;
            // Avance mesurée entre les bords quand c'est possible : une longue ligne juste
            // en dessous passe devant un petit bouton plus loin mais mieux centré.
            float gap = direction.horizontal()
                ? (direction.dx > 0 ? r.x - (from.x + from.width) : from.x - (r.x + r.width))
                : (direction.dy > 0 ? r.y - (from.y + from.height) : from.y - (r.y + r.height));
            float distance = Math.max(0f, Math.min(along, gap));
            boolean aligned = direction.horizontal()
                ? overlaps(from.y, from.y + from.height, r.y, r.y + r.height)
                : overlaps(from.x, from.x + from.width, r.x, r.x + r.width);
            float side = Math.abs(direction.horizontal() ? dy : dx);
            if (!aligned) {
                // Décalé : son centre doit être au-delà du bord de l'élément actuel, dans un cône de 60° environ.
                float beyond = direction.horizontal()
                    ? (direction.dx > 0 ? cx - (from.x + from.width) : from.x - cx)
                    : (direction.dy > 0 ? cy - (from.y + from.height) : from.y - cy);
                if (beyond <= 0f || side > beyond * MAX_SIDE_RATIO) continue;
            }
            float score = distance + side * (aligned ? ALIGNED_SIDE_WEIGHT : SIDE_WEIGHT);
            if (score < bestScore) {
                bestScore = score;
                best = i;
            }
        }
        return best;
    }

    /** @return la place du rectangle dont le centre est le plus proche de ({@code x}, {@code y}), ou -1 si la liste est vide. */
    public static int nearest(float x, float y, List<Rectangle> candidates) {
        int best = -1;
        float bestDistance = Float.MAX_VALUE;
        for (int i = 0; i < candidates.size(); i++) {
            Rectangle r = candidates.get(i);
            float dx = r.x + r.width / 2f - x, dy = r.y + r.height / 2f - y;
            float d = dx * dx + dy * dy;
            if (d < bestDistance) {
                bestDistance = d;
                best = i;
            }
        }
        return best;
    }

    private static boolean overlaps(float a0, float a1, float b0, float b1) {
        return a0 < b1 && b0 < a1;
    }
}
