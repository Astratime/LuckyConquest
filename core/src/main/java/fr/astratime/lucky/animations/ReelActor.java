package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.function.Function;

/**
 * Un rouleau de machine à sous : affiche un symbole, ou fait défiler des
 * symboles tirés au hasard (de haut en bas, avec un léger flou de mouvement)
 * jusqu'à s'arrêter sur le symbole final avec un petit rebond. Le dessin est
 * limité à la fenêtre du rouleau.
 *
 * @param <S> type des symboles (leur image est donnée par {@code regionOf})
 */
public class ReelActor<S> extends Actor {

    private static final float SPEED          = 2400f;  // pixels par seconde à pleine vitesse
    private static final float SLOW_SPEED     = 650f;   // vitesse du rouleau qui fait durer le suspense
    private static final float SPEED_EASING   = 3f;     // rapidité du ralentissement (par seconde)
    private static final float LAND_DURATION  = 0.4f;   // arrivée du symbole final, dépassement compris
    private static final float BLUR_SPEED     = 900f;   // en dessous, plus de flou
    private static final float[] BLUR_OFFSETS = {0.18f, 0.36f};  // fractions de hauteur, vers le haut
    private static final float[] BLUR_ALPHAS  = {0.35f, 0.15f};

    private enum State { IDLE, SPINNING, LANDING }

    private final Function<S, TextureRegion> regionOf;
    private final S[]                        pool;

    private State    state = State.IDLE;
    private S        shown;          // symbole à l'arrêt (null : fenêtre vide)
    private S        current, next, above;
    private S        target;
    private float    offset;         // descente du symbole courant, de 0 à la hauteur du rouleau
    private float    speed;
    private float    elapsed, stopAt, slowAt;
    private float    landTime;
    private Runnable onStopped;

    /**
     * @param pool     symboles tirés au hasard pendant le défilement
     * @param regionOf image de chaque symbole
     */
    public ReelActor(S[] pool, Function<S, TextureRegion> regionOf) {
        this.pool     = pool;
        this.regionOf = regionOf;
    }

    /** @return le symbole affiché à l'arrêt, ou null (fenêtre vide ou rouleau en mouvement). */
    public S getSymbol() { return state == State.IDLE ? shown : null; }

    /** Vide la fenêtre et arrête le rouleau. */
    public void empty() {
        state = State.IDLE;
        shown = null;
    }

    /**
     * Lance le rouleau ; il s'arrête sur {@code symbol} après {@code stopAt}
     * secondes (plus la durée de l'arrivée).
     *
     * @param slowAt    à partir de quand il ralentit pour faire durer le suspense (négatif : jamais)
     * @param onStopped appelé quand le symbole final est en place
     */
    public void spin(S symbol, float stopAt, float slowAt, Runnable onStopped) {
        this.target    = symbol;
        this.stopAt    = stopAt;
        this.slowAt    = slowAt;
        this.onStopped = onStopped;
        current = shown != null ? shown : randomSymbol();
        next    = randomSymbol();
        above   = randomSymbol();
        offset  = 0f;
        speed   = SPEED;
        elapsed = 0f;
        state   = State.SPINNING;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (state == State.SPINNING) {
            elapsed += delta;
            float wanted = slowAt >= 0f && elapsed >= slowAt ? SLOW_SPEED : SPEED;
            speed += (wanted - speed) * Math.min(1f, SPEED_EASING * delta);
            offset += speed * delta;
            float height = getHeight();
            while (offset >= height) {
                offset -= height;
                current = next;
                next    = above;
                above   = randomSymbol();
                if (elapsed >= stopAt) { // le symbole final arrive par le haut
                    next     = target;
                    above    = randomSymbol();
                    offset   = 0f;
                    landTime = 0f;
                    state    = State.LANDING;
                    break;
                }
            }
        } else if (state == State.LANDING) {
            landTime += delta;
            float progress = Math.min(1f, landTime / LAND_DURATION);
            offset = getHeight() * Interpolation.swingOut.apply(progress); // dépasse un peu, puis revient
            if (progress >= 1f) {
                shown = target;
                state = State.IDLE;
                if (onStopped != null) onStopped.run();
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (state == State.IDLE && shown == null) return;
        batch.flush();
        if (!clipBegin(getX(), getY(), getWidth(), getHeight())) return;
        float r = batch.getColor().r, g = batch.getColor().g, b = batch.getColor().b, a = batch.getColor().a;
        float alpha = getColor().a * parentAlpha;
        if (state == State.IDLE) {
            drawSymbol(batch, shown, getY(), alpha);
        } else {
            // Bande de trois symboles qui descend : le courant, puis ceux qui arrivent par le haut.
            float h = getHeight();
            float y = getY() - offset;
            boolean blur = state == State.SPINNING && speed > BLUR_SPEED;
            drawMoving(batch, current, y, h, blur, alpha);
            drawMoving(batch, next, y + h, h, blur, alpha);
            drawMoving(batch, above, y + 2 * h, h, blur, alpha);
        }
        batch.setColor(r, g, b, a);
        batch.flush();
        clipEnd();
    }

    /** Dessine un symbole en mouvement, suivi de copies estompées au-dessus quand il va vite (flou). */
    private void drawMoving(Batch batch, S symbol, float y, float h, boolean blur, float alpha) {
        if (blur) {
            for (int k = 0; k < BLUR_OFFSETS.length; k++) {
                drawSymbol(batch, symbol, y + BLUR_OFFSETS[k] * h, alpha * BLUR_ALPHAS[k]);
            }
        }
        drawSymbol(batch, symbol, y, alpha);
    }

    private void drawSymbol(Batch batch, S symbol, float y, float alpha) {
        if (symbol == null) return;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(regionOf.apply(symbol), getX(), y, getWidth(), getHeight());
    }

    private S randomSymbol() {
        return pool[MathUtils.random(pool.length - 1)];
    }
}
