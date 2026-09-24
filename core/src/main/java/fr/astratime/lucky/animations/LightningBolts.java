package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.ArrayList;
import java.util.List;

/**
 * Éclairs dorés qui jaillissent de points d'ancrage (le bord de la bannière
 * « BINGO!!! ») : chaque éclair est une ligne brisée redessinée au hasard
 * plusieurs fois par seconde, avec un halo doré et un cœur presque blanc, ce
 * qui le fait crépiter.
 *
 * Les ancrages sont exprimés dans le repère du parent : l'acteur doit être
 * placé à l'origine de son parent.
 */
public class LightningBolts extends Actor {

    private static final float REDRAW_INTERVAL = 0.06f;
    private static final int   SEGMENTS        = 7;
    private static final float JITTER          = 22f;   // écart latéral maximal d'un coude
    private static final float SHOW_CHANCE     = 0.7f;  // chaque ancrage n'a pas d'éclair à chaque tirage
    private static final float GLOW_WIDTH      = 12f;
    private static final float CORE_WIDTH      = 4f;

    private record Anchor(float x, float y, float angleDeg, float length) {}

    private final TextureRegion      pixel;
    private final List<Anchor>       anchors = new ArrayList<>();
    private final List<float[]>      bolts   = new ArrayList<>();
    private float                    timer;

    /** @param pixel région d'un pixel blanc, étirée pour tracer les segments */
    public LightningBolts(TextureRegion pixel) {
        this.pixel = pixel;
    }

    /** Ajoute un point d'où part un éclair, dans la direction {@code angleDeg} (degrés). */
    public void addAnchor(float x, float y, float angleDeg, float length) {
        anchors.add(new Anchor(x, y, angleDeg, length));
    }

    /** Retire tous les points d'ancrage (et les éclairs affichés). */
    public void clearAnchors() {
        anchors.clear();
        bolts.clear();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        timer -= delta;
        if (timer <= 0f) {
            timer = REDRAW_INTERVAL;
            regenerate();
        }
    }

    /** Retire les éclairs affichés et en tire de nouveaux. */
    private void regenerate() {
        bolts.clear();
        for (Anchor anchor : anchors) {
            if (!MathUtils.randomBoolean(SHOW_CHANCE)) continue;
            float angle  = anchor.angleDeg() + MathUtils.random(-15f, 15f);
            float length = anchor.length() * MathUtils.random(0.7f, 1.1f);
            float dirX = MathUtils.cosDeg(angle), dirY = MathUtils.sinDeg(angle);
            float[] points = new float[(SEGMENTS + 1) * 2];
            for (int i = 0; i <= SEGMENTS; i++) {
                float along  = length * i / SEGMENTS;
                // Les extrémités restent sur l'axe ; les coudes s'en écartent au hasard.
                float offset = i == 0 || i == SEGMENTS ? 0f : MathUtils.random(-JITTER, JITTER);
                points[i * 2]     = anchor.x() + dirX * along - dirY * offset;
                points[i * 2 + 1] = anchor.y() + dirY * along + dirX * offset;
            }
            bolts.add(points);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float r = batch.getColor().r, g = batch.getColor().g, b = batch.getColor().b, a = batch.getColor().a;
        float alpha = getColor().a * parentAlpha;
        for (float[] points : bolts) {
            batch.setColor(1f, 0.8f, 0.15f, 0.55f * alpha);
            drawPolyline(batch, points, GLOW_WIDTH);
            batch.setColor(1f, 1f, 0.85f, alpha);
            drawPolyline(batch, points, CORE_WIDTH);
        }
        batch.setColor(r, g, b, a);
    }

    private void drawPolyline(Batch batch, float[] points, float width) {
        for (int i = 0; i + 3 < points.length; i += 2) {
            drawSegment(batch, getX() + points[i], getY() + points[i + 1],
                getX() + points[i + 2], getY() + points[i + 3], width);
        }
    }

    /** Trace un segment épais de (x1, y1) à (x2, y2) : un pixel étiré puis tourné autour de son départ. */
    private void drawSegment(Batch batch, float x1, float y1, float x2, float y2, float width) {
        float dx = x2 - x1, dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        float angle  = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
        // Un peu plus long que le segment pour que les coudes se rejoignent sans trou.
        batch.draw(pixel, x1 - width / 2f, y1 - width / 2f, width / 2f, width / 2f,
            length + width, width, 1f, 1f, angle);
    }
}
