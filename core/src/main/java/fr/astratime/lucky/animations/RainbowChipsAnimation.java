package fr.astratime.lucky.animations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.settings.VisualSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Carte Arc-en-ciel : des jetons de casino de toutes les couleurs traversent
 * l'écran en tournoyant, chacun sur sa propre trajectoire (courbe, ondulation,
 * vitesse), et sèment de la poussière d'étoiles multicolore. Chaque carte à
 * changer de couleur est traversée par un jeton : quand il passe dessus, une
 * gerbe d'étoiles jaillit et la carte change.
 *
 * Posé par-dessus le jeu, l'acteur bloque les clics pendant l'animation.
 */
public class RainbowChipsAnimation extends Actor implements Disposable {

    private static final int   MIN_CHIPS     = 6;
    private static final float CHIP_SIZE     = 64f;
    private static final float STAGGER       = 0.14f;  // entre deux départs de jetons
    private static final float MIN_FLIGHT    = 1.3f;   // durée d'une traversée, en secondes
    private static final float MAX_FLIGHT    = 1.9f;
    private static final float MARGIN        = 90f;    // départ et arrivée hors de l'écran
    private static final float MAX_WOBBLE    = 60f;    // ondulation latérale, en pixels
    private static final float FRAME_RATE    = 16f;    // images de rotation du jeton par seconde
    private static final float DUST_RATE     = 70f;    // poussières semées par seconde et par jeton
    private static final int   BURST         = 36;     // étoiles sur une carte qui change de couleur
    private static final float GRAVITY       = -60f;
    private static final float END_DELAY     = 0.35f;  // les dernières poussières s'éteignent

    /** Une traversée : courbe de Bézier quadratique, avec une ondulation perpendiculaire nulle au milieu. */
    private static final class Chip {
        float startX, startY, controlX, controlY, endX, endY;
        float delay, duration, wobble, wobbleFreq, phase;
        int   row;
        int   target = -1;       // carte traversée au milieu de la course (-1 : aucune)
        boolean reached;
        float dustDebt;
    }

    private final VisualSettings settings;
    private final TextureRegion  pixel;
    private final Texture        chipsTexture = new Texture(Gdx.files.internal("jackpot/rainbow_chips.png"));
    private final TextureRegion[][] frames;
    private final List<Chip>     chips = new ArrayList<>();
    private final List<float[]>  dust  = new ArrayList<>(); // x, y, vx, vy, vie, vie totale, taille, teinte
    private final Color          tint  = new Color();

    private float       time = -1f;
    private float       endTime;
    private IntConsumer onReached;
    private Runnable    onDone;

    /** @param pixel région d'un pixel blanc (poussière d'étoiles) */
    public RainbowChipsAnimation(VisualSettings settings, TextureRegion pixel) {
        this.settings = settings;
        this.pixel    = pixel;
        int size = chipsTexture.getHeight() / 6;
        frames = TextureRegion.split(chipsTexture, size, size);
        setTouchable(Touchable.disabled);
    }

    /**
     * Lance les jetons : le jeton numéro {@code i} passe sur {@code targets.get(i)}
     * (coordonnées du Stage) au milieu de sa course ; d'autres jetons traversent
     * l'écran librement.
     *
     * @param onReached appelé avec l'indice de la cible quand son jeton passe dessus
     * @param onDone    appelé quand tous les jetons sont sortis de l'écran
     */
    public void play(List<Vector2> targets, IntConsumer onReached, Runnable onDone) {
        float width  = getStage().getViewport().getWorldWidth();
        float height = getStage().getViewport().getWorldHeight();
        setBounds(0f, 0f, width, height);
        this.onReached = onReached;
        this.onDone    = onDone;
        chips.clear();
        dust.clear();

        int count = Math.max(MIN_CHIPS, targets.size());
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < count; i++) order.add(i);
        java.util.Collections.shuffle(order); // départs dans le désordre
        endTime = 0f;
        for (int i = 0; i < count; i++) {
            Vector2 middle = i < targets.size() ? targets.get(i)
                : new Vector2(MathUtils.random(width * 0.25f, width * 0.9f), MathUtils.random(height * 0.25f, height * 0.8f));
            Chip chip = trajectory(middle, width, height);
            chip.target     = i < targets.size() ? i : -1;
            chip.row        = i % frames.length;
            chip.delay      = order.get(i) * STAGGER;
            chip.duration   = MathUtils.random(MIN_FLIGHT, MAX_FLIGHT);
            chip.wobble     = MathUtils.random(-MAX_WOBBLE, MAX_WOBBLE);
            chip.wobbleFreq = MathUtils.random(1, 3);
            chip.phase      = MathUtils.random(8f);
            chips.add(chip);
            endTime = Math.max(endTime, chip.delay + chip.duration);
        }
        endTime += END_DELAY;
        time = 0f;
        setTouchable(Touchable.enabled);
        toFront();
    }

    /**
     * Trajectoire qui passe par {@code middle} : départ sur un bord de l'écran
     * (gauche, droite, haut ou bas, au hasard), arrivée à l'opposé, avec une
     * courbure différente pour chaque jeton.
     */
    private static Chip trajectory(Vector2 middle, float width, float height) {
        Chip chip = new Chip();
        float angle = MathUtils.random(360f);       // direction de la traversée
        float reach = (float) Math.hypot(width, height);
        float dx = MathUtils.cosDeg(angle), dy = MathUtils.sinDeg(angle);
        // Départ et arrivée décalés du même côté : la courbe, qui passe par le milieu, se bombe de l'autre.
        float bend = MathUtils.random(-0.3f, 0.3f) * reach;
        float px = -dy * bend, py = dx * bend;
        chip.startX = clampOut(middle.x - dx * reach + px, width);
        chip.startY = clampOut(middle.y - dy * reach + py, height);
        chip.endX   = clampOut(middle.x + dx * reach + px, width);
        chip.endY   = clampOut(middle.y + dy * reach + py, height);
        // Bézier quadratique passant par le milieu à t = 0,5.
        chip.controlX = 2f * middle.x - (chip.startX + chip.endX) / 2f;
        chip.controlY = 2f * middle.y - (chip.startY + chip.endY) / 2f;
        return chip;
    }

    /** Ramène {@code value} juste hors de l'écran (entre -MARGIN et size + MARGIN). */
    private static float clampOut(float value, float size) {
        return MathUtils.clamp(value, -MARGIN, size + MARGIN);
    }

    /** @return {@code true} pendant l'animation. */
    public boolean isPlaying() { return time >= 0f; }

    /** Interrompt l'animation sans rien appeler (nouveau combat). */
    public void cancel() {
        time = -1f;
        chips.clear();
        dust.clear();
        onReached = null;
        onDone    = null;
        setTouchable(Touchable.disabled);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (time < 0f) return;
        time += delta;
        Vector2 pos = new Vector2();
        float rate = settings.isReducedEffects() ? DUST_RATE / 3f : DUST_RATE;
        for (Chip chip : chips) {
            float t = (time - chip.delay) / chip.duration;
            if (t < 0f || t > 1f) continue;
            position(chip, t, pos);
            chip.dustDebt += rate * delta;
            while (chip.dustDebt >= 1f) {
                chip.dustDebt -= 1f;
                addDust(pos.x, pos.y, 60f, MathUtils.random(0.5f, 1.1f));
            }
            if (!chip.reached && chip.target >= 0 && t >= 0.5f) {
                chip.reached = true;
                position(chip, 0.5f, pos);
                int count = settings.isReducedEffects() ? BURST / 3 : BURST;
                for (int i = 0; i < count; i++) addDust(pos.x, pos.y, 260f, MathUtils.random(0.5f, 1f));
                if (onReached != null) onReached.accept(chip.target);
            }
        }
        for (int i = dust.size() - 1; i >= 0; i--) {
            float[] d = dust.get(i);
            d[0] += d[2] * delta;
            d[1] += d[3] * delta;
            d[3] += GRAVITY * delta;
            d[2] *= 1f - 1.5f * delta;
            d[4] -= delta;
            if (d[4] <= 0f) dust.remove(i);
        }
        if (time >= endTime) {
            Runnable done = onDone;
            for (Chip chip : chips) { // une cible jamais atteinte change quand même
                if (!chip.reached && chip.target >= 0 && onReached != null) onReached.accept(chip.target);
            }
            cancel();
            if (done != null) done.run();
        }
    }

    /** Position du jeton à l'instant {@code t} (0 à 1) de sa traversée. */
    private static void position(Chip chip, float t, Vector2 out) {
        float u = 1f - t;
        float x = u * u * chip.startX + 2f * u * t * chip.controlX + t * t * chip.endX;
        float y = u * u * chip.startY + 2f * u * t * chip.controlY + t * t * chip.endY;
        // Ondulation perpendiculaire à la direction générale, nulle au milieu (le jeton passe sur sa cible).
        float dx = chip.endX - chip.startX, dy = chip.endY - chip.startY;
        float length = Math.max(1f, (float) Math.hypot(dx, dy));
        float wave = chip.wobble * MathUtils.sin(MathUtils.PI2 * chip.wobbleFreq * t) * Math.abs(t - 0.5f) * 2f;
        out.set(x - dy / length * wave, y + dx / length * wave);
    }

    private void addDust(float x, float y, float speed, float life) {
        float angle = MathUtils.random(360f);
        float v = MathUtils.random(0.2f, 1f) * speed;
        dust.add(new float[] { x + MathUtils.random(-10f, 10f), y + MathUtils.random(-10f, 10f),
            MathUtils.cosDeg(angle) * v, MathUtils.sinDeg(angle) * v, life, life,
            MathUtils.random(3f, 8f), MathUtils.random(1f) });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (time < 0f) return;
        float r = batch.getColor().r, g = batch.getColor().g, b = batch.getColor().b, a = batch.getColor().a;

        // Poussière d'étoiles : additive, multicolore, qui scintille ; les plus grosses ont des branches.
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (float[] d : dust) {
            float life = d[4] / d[5];
            float twinkle = 0.6f + 0.4f * MathUtils.sin(time * 20f + d[7] * 40f);
            hsv(d[7] + time * 0.3f, 0.55f, 1f);
            batch.setColor(tint.r, tint.g, tint.b, life * twinkle * parentAlpha);
            float size = d[6] * (0.5f + 0.5f * life);
            batch.draw(pixel, d[0] - size / 2f, d[1] - size / 2f, size, size);
            if (d[6] > 6f) {
                batch.draw(pixel, d[0] - size * 1.2f, d[1] - 0.75f, size * 2.4f, 1.5f);
                batch.draw(pixel, d[0] - 0.75f, d[1] - size * 1.2f, 1.5f, size * 2.4f);
            }
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Jetons qui tournoient.
        Vector2 pos = new Vector2();
        batch.setColor(1f, 1f, 1f, parentAlpha);
        for (Chip chip : chips) {
            float t = (time - chip.delay) / chip.duration;
            if (t < 0f || t > 1f) continue;
            position(chip, t, pos);
            TextureRegion[] row = frames[chip.row];
            TextureRegion frame = row[(int) (time * FRAME_RATE + chip.phase) % row.length];
            batch.draw(frame, pos.x - CHIP_SIZE / 2f, pos.y - CHIP_SIZE / 2f, CHIP_SIZE, CHIP_SIZE);
        }
        batch.setColor(r, g, b, a);
    }

    /** Met dans {@link #tint} la couleur de teinte {@code hue} (0 à 1, cyclique). */
    private void hsv(float hue, float saturation, float value) {
        tint.fromHsv((hue % 1f) * 360f, saturation, value);
        tint.a = 1f;
    }

    @Override
    public void dispose() {
        chipsTexture.dispose();
    }
}
