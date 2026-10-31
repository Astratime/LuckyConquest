package fr.astratime.lucky.views;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Vue de face du combat : la table, posée à plat dans le Stage comme en vue
 * classique, est montrée en perspective, vue de face et un peu au-dessus. Son
 * rectangle devient un trapèze : le bord du joueur reste presque à sa taille,
 * le bord de l'ennemi rétrécit et remonte au milieu de l'écran, laissant la
 * place à l'ennemi en grand derrière la table (voir {@link FrontScene}).
 *
 * <p>La projection est une homographie (projection d'un plan sur un autre) :
 * {@link #project} donne le point de l'écran d'un point de la table,
 * {@link #unproject} l'inverse (un clic sur l'écran, ramené sur la table).
 *
 * <p>Tout ce qui est posé sur la table (rouleaux, cartes, piles, levier…) garde
 * ses coordonnées de toujours ; seuls les éléments dessinés à plat par-dessus
 * (infobulles, textes animés, symboles qui frappent, confettis) passent leurs
 * points par {@link #toScreen}, qui ne déplace que les points de la table.
 * Désactivée (vue classique), elle ne change rien.
 */
public class TableProjection {

    /** Hauteur du bord de l'ennemi (haut du trapèze), en fraction de la hauteur de l'écran. */
    static final float TOP_HEIGHT = 0.55f;
    /** Largeur du bord de l'ennemi, en fraction de celle du bord du joueur. */
    static final float TOP_WIDTH  = 0.66f;
    /** Le bord du joueur descend un peu et s'élargit : il est plus près de nous. */
    static final float BOTTOM_DROP   = 18f;
    static final float BOTTOM_SPREAD = 14f;

    private boolean         enabled;
    /** Rectangle de la table dans le Stage (rebord compris). */
    private final Rectangle table = new Rectangle();
    /** Coins du trapèze à l'écran : bas gauche, bas droite, haut droite, haut gauche (x, y). */
    private final float[]   quad  = new float[8];
    /** Homographie table vers écran, et son inverse (matrices 3x3, par lignes). */
    private final float[]   forward = new float[9];
    private final float[]   inverse = new float[9];

    /** @return {@code true} en vue de face. */
    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /**
     * Recalcule le trapèze pour la table {@code (x, y, width, height)} dans un
     * écran de hauteur {@code worldHeight} (après un redimensionnement).
     */
    public void update(float x, float y, float width, float height, float worldHeight) {
        table.set(x, y, width, height);
        float bottomY = y - BOTTOM_DROP;
        float left    = x - BOTTOM_SPREAD, right = x + width + BOTTOM_SPREAD;
        float center  = x + width / 2f;
        float topHalf = (right - left) * TOP_WIDTH / 2f;
        float topY    = worldHeight * TOP_HEIGHT;
        quad[0] = left;             quad[1] = bottomY;
        quad[2] = right;            quad[3] = bottomY;
        quad[4] = center + topHalf; quad[5] = topY;
        quad[6] = center - topHalf; quad[7] = topY;
        squareToQuad(quad, forward);
        // Ramène d'abord la table au carré unité.
        multiply(forward, new float[] {1f / width, 0f, -x / width, 0f, 1f / height, -y / height, 0f, 0f, 1f}, forward);
        invert(forward, inverse);
    }

    /** @return le rectangle de la table dans le Stage (ne pas modifier). */
    public Rectangle getTable() { return table; }

    /** @return la hauteur (écran) du bord de l'ennemi, le haut du trapèze. */
    public float getTopY() { return quad[5]; }

    /** @return l'abscisse (écran) du coin haut gauche du trapèze. */
    public float getTopLeftX() { return quad[6]; }

    /** @return l'abscisse (écran) du coin haut droit du trapèze. */
    public float getTopRightX() { return quad[4]; }

    /**
     * Point de la table vers l'écran, en vue de face ; les points hors de la
     * table (panneau des gains, barres de vie) ne bougent pas.
     *
     * @return {@code point}, modifié
     */
    public Vector2 toScreen(Vector2 point) {
        if (enabled && table.contains(point)) project(point);
        return point;
    }

    /** Comme {@link #toScreen(Vector2)}, pour un point {@code (x, y)} : renvoie un nouveau vecteur. */
    public Vector2 toScreen(float x, float y) {
        return toScreen(new Vector2(x, y));
    }

    /** Point de la table (même hors du rectangle) vers l'écran. @return {@code point}, modifié */
    public Vector2 project(Vector2 point) {
        return apply(forward, point);
    }

    /** Point de l'écran vers la table. @return {@code point}, modifié */
    public Vector2 unproject(Vector2 point) {
        return apply(inverse, point);
    }

    /** @return l'agrandissement (horizontal) de la table au point {@code (x, y)} : 1 au bord du joueur, moins au loin. */
    public float scaleAt(float x, float y) {
        Vector2 left = project(new Vector2(x - 5f, y)), right = project(new Vector2(x + 5f, y));
        return (right.x - left.x) / 10f;
    }

    private static Vector2 apply(float[] m, Vector2 p) {
        float w = m[6] * p.x + m[7] * p.y + m[8];
        float x = (m[0] * p.x + m[1] * p.y + m[2]) / w;
        float y = (m[3] * p.x + m[4] * p.y + m[5]) / w;
        return p.set(x, y);
    }

    /**
     * Homographie du carré unité vers le quadrilatère {@code q} (coins (0,0),
     * (1,0), (1,1), (0,1) dans cet ordre), d'après Heckbert.
     */
    private static void squareToQuad(float[] q, float[] m) {
        float x0 = q[0], y0 = q[1], x1 = q[2], y1 = q[3], x2 = q[4], y2 = q[5], x3 = q[6], y3 = q[7];
        float sx = x0 - x1 + x2 - x3, sy = y0 - y1 + y2 - y3;
        float dx1 = x1 - x2, dx2 = x3 - x2, dy1 = y1 - y2, dy2 = y3 - y2;
        float det = dx1 * dy2 - dx2 * dy1;
        float g = (sx * dy2 - dx2 * sy) / det;
        float h = (dx1 * sy - sx * dy1) / det;
        m[0] = x1 - x0 + g * x1; m[1] = x3 - x0 + h * x3; m[2] = x0;
        m[3] = y1 - y0 + g * y1; m[4] = y3 - y0 + h * y3; m[5] = y0;
        m[6] = g;                m[7] = h;                m[8] = 1f;
    }

    /** {@code out = a × b} (3x3) ; {@code out} peut être {@code a} ou {@code b}. */
    private static void multiply(float[] a, float[] b, float[] out) {
        float[] r = new float[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                r[i * 3 + j] = a[i * 3] * b[j] + a[i * 3 + 1] * b[3 + j] + a[i * 3 + 2] * b[6 + j];
            }
        }
        System.arraycopy(r, 0, out, 0, 9);
    }

    /** {@code out = m⁻¹} (3x3, par la comatrice). */
    private static void invert(float[] m, float[] out) {
        float a = m[0], b = m[1], c = m[2], d = m[3], e = m[4], f = m[5], g = m[6], h = m[7], i = m[8];
        float A = e * i - f * h, B = -(d * i - f * g), C = d * h - e * g;
        float det = a * A + b * B + c * C;
        out[0] = A / det;                 out[1] = -(b * i - c * h) / det; out[2] = (b * f - c * e) / det;
        out[3] = B / det;                 out[4] = (a * i - c * g) / det;  out[5] = -(a * f - c * d) / det;
        out[6] = C / det;                 out[7] = -(a * h - b * g) / det; out[8] = (a * e - b * d) / det;
    }
}
