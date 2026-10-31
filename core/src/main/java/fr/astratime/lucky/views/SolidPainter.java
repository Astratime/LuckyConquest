package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * Vue de face : dessine des volumes posés sur la table (piles de cartes,
 * échoppe, machines à sous), à leur place dans la perspective. Un volume se
 * donne en coordonnées de la table à plat ({@code x}, {@code y}) plus une
 * hauteur {@code z} au-dessus du feutre ; {@link TableProjection#project(float, float, float, Vector2)}
 * le pose à l'écran.
 *
 * <p>Une boîte montre son dessus, sa face avant (vers le joueur) et le côté
 * tourné vers le milieu de la table, comme on la verrait de la place du joueur.
 */
public class SolidPainter {

    /** Découpage des faces (la perspective n'est pas linéaire en profondeur). */
    private static final int GRID = 4;

    /** Assombrissement des faces : dessus, avant, côté. */
    public static final float TOP = 1f, FRONT = 0.82f, SIDE = 0.62f;

    private final TableProjection projection;
    private final TextureRegion   pixel;
    private final float[]         vertices = new float[20];
    private final float[]         grid     = new float[(GRID + 1) * (GRID + 1) * 2];
    private final Vector2         point    = new Vector2();
    private final Color           tint     = new Color();

    public SolidPainter(TableProjection projection, Texture pixel) {
        this.projection = projection;
        this.pixel      = new TextureRegion(pixel);
    }

    public TableProjection getProjection() { return projection; }

    /** @return un pixel blanc, pour les faces unies */
    public TextureRegion pixel() { return pixel; }

    /**
     * Boîte unie {@code [x0, x1] x [y0, y1] x [z0, z1]} de couleur {@code color}
     * (faces ombrées). Rien n'est dessiné sous la table ({@code z < 0}).
     */
    public void box(Batch batch, float x0, float y0, float z0, float x1, float y1, float z1, Color color, float alpha) {
        box(batch, x0, y0, z0, x1, y1, z1, pixel, pixel, pixel, color, color, alpha, true);
    }

    /**
     * Boîte texturée : {@code top} sur le dessus (haut de l'image vers le fond),
     * {@code front} sur l'avant, {@code side} sur le côté visible (haut de l'image
     * en haut), teint de {@code sideColor}. Sans {@code withFront}, l'avant est
     * laissé à l'appelant (la machine à sous dessine le sien).
     */
    public void box(Batch batch, float x0, float y0, float z0, float x1, float y1, float z1,
                    TextureRegion top, TextureRegion front, TextureRegion side, Color color, Color sideColor,
                    float alpha, boolean withFront) {
        z0 = Math.max(z0, 0f);
        if (z1 <= z0) return;
        float center = projection.getTable().x + projection.getTable().width / 2f;
        if (x1 < center) {
            face(batch, side, sideColor, SIDE, alpha, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
        } else if (x0 > center) {
            face(batch, side, sideColor, SIDE, alpha, x0, y1, z0, x0, y0, z0, x0, y0, z1, x0, y1, z1);
        }
        if (withFront) face(batch, front, color, FRONT, alpha, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        face(batch, top, color, TOP, alpha, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
    }

    /**
     * Quadrilatère plat dans l'espace de la table, de coins a (bas gauche de
     * l'image), b (bas droite), c (haut droite), d (haut gauche), chacun
     * {@code x, y, z}. {@code shade} assombrit la couleur.
     */
    public void face(Batch batch, TextureRegion region, Color color, float shade, float alpha,
                     float ax, float ay, float az, float bx, float by, float bz,
                     float cx, float cy, float cz, float dx, float dy, float dz) {
        for (int j = 0; j <= GRID; j++) {
            float v = (float) j / GRID;
            for (int i = 0; i <= GRID; i++) {
                float u = (float) i / GRID;
                // Point du quadrilatère (interpolation bilinéaire des coins).
                float x = lerp(lerp(ax, bx, u), lerp(dx, cx, u), v);
                float y = lerp(lerp(ay, by, u), lerp(dy, cy, u), v);
                float z = lerp(lerp(az, bz, u), lerp(dz, cz, u), v);
                projection.project(x, y, z, point);
                grid[(j * (GRID + 1) + i) * 2]     = point.x;
                grid[(j * (GRID + 1) + i) * 2 + 1] = point.y;
            }
        }
        tint.set(color.r * shade, color.g * shade, color.b * shade, color.a * alpha);
        float packed = tint.toFloatBits();
        float u0 = region.getU(), u1 = region.getU2();
        float vBottom = region.getV2(), vTop = region.getV();
        for (int j = 0; j < GRID; j++) {
            for (int i = 0; i < GRID; i++) {
                float ua = lerp(u0, u1, (float) i / GRID), ub = lerp(u0, u1, (float) (i + 1) / GRID);
                float va = lerp(vBottom, vTop, (float) j / GRID), vb = lerp(vBottom, vTop, (float) (j + 1) / GRID);
                vertex(0, i, j, packed, ua, va);
                vertex(5, i, j + 1, packed, ua, vb);
                vertex(10, i + 1, j + 1, packed, ub, vb);
                vertex(15, i + 1, j, packed, ub, va);
                batch.draw(region.getTexture(), vertices, 0, vertices.length);
            }
        }
    }

    private void vertex(int at, int i, int j, float color, float u, float v) {
        vertices[at]     = grid[(j * (GRID + 1) + i) * 2];
        vertices[at + 1] = grid[(j * (GRID + 1) + i) * 2 + 1];
        vertices[at + 2] = color;
        vertices[at + 3] = u;
        vertices[at + 4] = v;
    }

    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }
}
