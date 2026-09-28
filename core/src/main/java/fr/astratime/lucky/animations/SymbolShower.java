package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Symboles de la machine projetés à travers l'écran lors d'un Bingo, de trois
 * façons : une pluie (ils tombent, rebondissent sur la table puis s'effacent),
 * un jet (lancés vers le haut, ils tournoient et retombent hors de l'écran) ou
 * une envolée (ils montent lentement en se balançant et s'estompent).
 *
 * Les positions sont celles du Stage : l'acteur doit être placé à l'origine.
 */
public class SymbolShower extends Actor {

    /** Appelé quand un symbole lourd touche la table pour la première fois. */
    public interface LandingListener {
        void onLand(float x, float y);
    }

    private static final float BOUNCE      = 0.35f;  // vitesse conservée à chaque rebond
    private static final int   MAX_BOUNCES = 2;
    private static final float REST_TIME   = 0.4f;   // posé sur la table avant de s'effacer
    private static final float FADE_TIME   = 0.4f;
    private static final float SWAY_ANGLE  = 18f;    // balancement d'un symbole qui s'envole
    private static final float SWAY_SPEED  = 5f;

    private enum Motion { RAIN, JET, RISE }

    private static final class Piece {
        TextureRegion region;
        Motion  motion;
        float   x, y, vx, vy, gravity, floor, rotation, spin, size, age, life, restTime, phase, alpha = 1f;
        int     bounces;
        boolean heavy;
    }

    private final List<Piece>     pieces = new ArrayList<>();
    private       LandingListener landingListener;

    public SymbolShower() {
        setTouchable(Touchable.disabled);
    }

    /** @param listener prévenu de l'atterrissage des symboles lourds (voir {@link #rain}) */
    public void setLandingListener(LandingListener listener) { this.landingListener = listener; }

    /**
     * Fait tomber un symbole depuis {@code (x, topY)} ; il rebondit sur la table à
     * l'ordonnée {@code floor} puis s'efface.
     *
     * @param size    largeur affichée du symbole
     * @param speed   vitesse initiale de chute (positive)
     * @param gravity accélération de la chute (positive) : faible, il flotte ; forte, il s'écrase
     * @param heavy   true pour prévenir le {@link LandingListener} à son premier impact
     */
    public void rain(TextureRegion region, float x, float topY, float floor, float size,
                     float speed, float gravity, boolean heavy) {
        Piece piece = add(region, Motion.RAIN, x, topY, size);
        piece.vx      = MathUtils.random(-60f, 60f);
        piece.vy      = -speed;
        piece.gravity = gravity;
        piece.floor   = floor;
        piece.heavy   = heavy;
        piece.spin    = heavy ? 0f : MathUtils.random(-90f, 90f);
        piece.rotation = heavy ? MathUtils.random(-8f, 8f) : MathUtils.random(-30f, 30f);
    }

    /**
     * Lance un symbole depuis {@code (x, y)} dans la direction {@code angleDeg} ;
     * il tournoie, retombe et disparaît sous l'écran.
     */
    public void jet(TextureRegion region, float x, float y, float angleDeg, float speed, float size, float gravity) {
        Piece piece = add(region, Motion.JET, x, y, size);
        piece.vx      = MathUtils.cosDeg(angleDeg) * speed;
        piece.vy      = MathUtils.sinDeg(angleDeg) * speed;
        piece.gravity = gravity;
        piece.spin    = MathUtils.random(-420f, 420f);
        piece.life    = 3.5f; // sécurité : un symbole sorti par le côté finit par disparaître
    }

    /** Fait monter un symbole depuis {@code (x, y)} en se balançant ; il s'estompe au bout de {@code life} secondes. */
    public void rise(TextureRegion region, float x, float y, float speed, float size, float life) {
        Piece piece = add(region, Motion.RISE, x, y, size);
        piece.vy   = speed;
        piece.life = life;
    }

    private Piece add(TextureRegion region, Motion motion, float x, float y, float size) {
        Piece piece = new Piece();
        piece.region = region;
        piece.motion = motion;
        piece.x      = x;
        piece.y      = y;
        piece.size   = size;
        piece.phase  = MathUtils.random(MathUtils.PI2);
        pieces.add(piece);
        return piece;
    }

    /** @return true s'il ne reste aucun symbole à l'écran. */
    public boolean isEmpty() { return pieces.isEmpty(); }

    /** Retire tous les symboles immédiatement. */
    public void removeAll() { pieces.clear(); }

    @Override
    public void act(float delta) {
        super.act(delta);
        Iterator<Piece> it = pieces.iterator();
        while (it.hasNext()) {
            Piece piece = it.next();
            piece.age += delta;
            boolean done = switch (piece.motion) {
                case RAIN -> updateRain(piece, delta);
                case JET  -> updateJet(piece, delta);
                case RISE -> updateRise(piece, delta);
            };
            if (done) it.remove();
        }
    }

    /** @return true quand le symbole a fini de s'effacer. */
    private boolean updateRain(Piece piece, float delta) {
        if (piece.bounces > MAX_BOUNCES) { // posé : attend puis s'efface
            piece.restTime += delta;
            if (piece.restTime > REST_TIME) piece.alpha -= delta / FADE_TIME;
            return piece.alpha <= 0f;
        }
        piece.vy       -= piece.gravity * delta;
        piece.x        += piece.vx * delta;
        piece.y        += piece.vy * delta;
        piece.rotation += piece.spin * delta;
        if (piece.y <= piece.floor && piece.vy < 0f) {
            piece.y = piece.floor;
            if (piece.bounces == 0 && piece.heavy && landingListener != null) {
                landingListener.onLand(piece.x, piece.floor - piece.size * 0.3f);
            }
            piece.bounces++;
            piece.vy    = -piece.vy * BOUNCE;
            piece.vx   *= 0.5f;
            piece.spin *= 0.4f;
            if (piece.bounces > MAX_BOUNCES) {
                piece.vx   = 0f;
                piece.vy   = 0f;
                piece.spin = 0f;
            }
        }
        return false;
    }

    /** @return true quand le symbole est retombé sous l'écran (ou a trop duré). */
    private boolean updateJet(Piece piece, float delta) {
        piece.vy       -= piece.gravity * delta;
        piece.x        += piece.vx * delta;
        piece.y        += piece.vy * delta;
        piece.rotation += piece.spin * delta;
        return (piece.vy < 0f && piece.y < -piece.size) || piece.age > piece.life;
    }

    /** @return true quand le symbole s'est estompé. */
    private boolean updateRise(Piece piece, float delta) {
        piece.y       += piece.vy * delta;
        piece.x       += MathUtils.sin(piece.age * SWAY_SPEED * 0.5f + piece.phase) * 40f * delta;
        piece.rotation = MathUtils.sin(piece.age * SWAY_SPEED + piece.phase) * SWAY_ANGLE;
        piece.alpha    = Math.min(1f, Math.min(piece.age / 0.25f, (piece.life - piece.age) / FADE_TIME));
        return piece.age >= piece.life;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (pieces.isEmpty()) return;
        float r = batch.getColor().r, g = batch.getColor().g, b = batch.getColor().b, a = batch.getColor().a;
        for (Piece piece : pieces) {
            float width  = piece.size;
            float height = width * piece.region.getRegionHeight() / piece.region.getRegionWidth();
            batch.setColor(1f, 1f, 1f, Math.max(0f, piece.alpha) * parentAlpha);
            batch.draw(piece.region, piece.x - width / 2f, piece.y - height / 2f, width / 2f, height / 2f,
                width, height, 1f, 1f, piece.rotation);
        }
        batch.setColor(r, g, b, a);
    }
}
