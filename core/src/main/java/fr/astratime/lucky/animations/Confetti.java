package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Confettis : petits rectangles colorés qui jaillissent (gerbe) ou tombent du
 * haut de l'écran (pluie), tournoient et se retournent comme du papier en
 * retombant lentement, puis s'estompent.
 *
 * Les positions sont celles du Stage : l'acteur doit être placé à l'origine.
 */
public class Confetti extends Actor {

    private static final float GRAVITY        = -900f;
    private static final float TERMINAL_SPEED = -170f;  // chute lente, freinée par l'air
    private static final float DRAG           = 1.8f;   // freinage horizontal (fraction par seconde)
    private static final float FLUTTER        = 40f;    // balancement latéral en tombant
    private static final float FADE_TIME      = 0.5f;

    private static final Color[] COLORS = {
        Color.valueOf("ffd54aff"), Color.valueOf("ff4a5aff"), Color.valueOf("4affa0ff"),
        Color.valueOf("4ac8ffff"), Color.valueOf("c77dffff"), Color.WHITE,
    };

    private static final class Piece {
        float x, y, vx, vy, rotation, spin, width, height, flip, flipSpeed, phase, life;
        Color color;
    }

    private final TextureRegion pixel;
    private final List<Piece>   pieces = new ArrayList<>();
    private float               time;

    /** @param pixel région d'un pixel blanc, teintée pour chaque confetti */
    public Confetti(TextureRegion pixel) {
        this.pixel = pixel;
        setTouchable(Touchable.disabled);
    }

    /** Fait jaillir {@code count} confettis de {@code (x, y)}, vers le haut en éventail. */
    public void burst(float x, float y, int count) {
        for (int i = 0; i < count; i++) {
            float angle = MathUtils.random(50f, 130f);
            float speed = MathUtils.random(300f, 650f);
            add(x, y, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed, MathUtils.random(1.6f, 2.4f));
        }
    }

    /** Fait tomber {@code count} confettis du haut de l'écran, entre {@code minX} et {@code maxX}. */
    public void rain(float minX, float maxX, float topY, int count) {
        for (int i = 0; i < count; i++) {
            add(MathUtils.random(minX, maxX), topY + MathUtils.random(0f, 400f),
                MathUtils.random(-60f, 60f), MathUtils.random(-150f, -60f), MathUtils.random(4f, 6f));
        }
    }

    /** Retire tous les confettis immédiatement. */
    public void removeAll() { pieces.clear(); }

    private void add(float x, float y, float vx, float vy, float life) {
        Piece piece = new Piece();
        piece.x         = x;
        piece.y         = y;
        piece.vx        = vx;
        piece.vy        = vy;
        piece.rotation  = MathUtils.random(360f);
        piece.spin      = MathUtils.random(-360f, 360f);
        piece.width     = MathUtils.random(6f, 9f);
        piece.height    = MathUtils.random(10f, 15f);
        piece.flipSpeed = MathUtils.random(6f, 12f);
        piece.phase     = MathUtils.random(MathUtils.PI2);
        piece.life      = life;
        piece.color     = COLORS[MathUtils.random(COLORS.length - 1)];
        pieces.add(piece);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        time += delta;
        float drag = Math.max(0f, 1f - DRAG * delta);
        Iterator<Piece> it = pieces.iterator();
        while (it.hasNext()) {
            Piece piece = it.next();
            piece.life -= delta;
            if (piece.life <= 0f) {
                it.remove();
                continue;
            }
            piece.vx *= drag;
            piece.vy = Math.max(TERMINAL_SPEED, piece.vy + GRAVITY * delta);
            piece.x += (piece.vx + MathUtils.sin(time * 3f + piece.phase) * FLUTTER) * delta;
            piece.y += piece.vy * delta;
            piece.rotation += piece.spin * delta;
            piece.flip     += piece.flipSpeed * delta;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (pieces.isEmpty()) return;
        float r = batch.getColor().r, g = batch.getColor().g, b = batch.getColor().b, a = batch.getColor().a;
        for (Piece piece : pieces) {
            float alpha = Math.min(1f, piece.life / FADE_TIME) * parentAlpha;
            // Le papier se retourne : sa hauteur apparente suit un cosinus, sa teinte s'assombrit de dos.
            float facing = MathUtils.cos(piece.flip);
            float shade  = 0.65f + 0.35f * Math.abs(facing);
            batch.setColor(piece.color.r * shade, piece.color.g * shade, piece.color.b * shade, alpha);
            float height = Math.max(1.5f, piece.height * Math.abs(facing));
            batch.draw(pixel, piece.x - piece.width / 2f, piece.y - height / 2f, piece.width / 2f, height / 2f,
                piece.width, height, 1f, 1f, piece.rotation);
        }
        batch.setColor(r, g, b, a);
    }
}
