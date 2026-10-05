package fr.astratime.lucky.animations;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.settings.VisualSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Cinématique avant la Comète Dorée (boss du chapitre 1 de la Tour des
 * épreuves) : un ciel nocturne au-dessus d'une ville de casinos, une pluie
 * d'étoiles filantes, puis une météorite en feu qui traverse le ciel et
 * s'écrase derrière la ville. L'explosion s'ouvre en boule de feu et en onde
 * de choc, puis tout l'écran passe au blanc ; le combat commence sous le blanc
 * ({@code onWhite}), qui s'estompe ensuite.
 *
 * Un clic ou une touche passe la cinématique : l'écran blanchit tout de suite.
 * Avec les effets réduits, ni secousse ni éclair à l'impact (le fondu blanc reste).
 *
 * Tout est dessiné à chaque image (pas d'enfants) ; l'acteur couvre le Stage
 * et bloque les clics tant qu'il est visible.
 */
public class CometCutscene extends Actor implements Disposable {

    /** Le ciel sort du noir. */
    public static final float SKY_IN       = 0.8f;
    /** La pluie d'étoiles filantes s'arrête de grossir. */
    public static final float STARS_PEAK   = 2.6f;
    /** Plus de nouvelles étoiles filantes. */
    public static final float STARS_END    = 4.2f;
    /** La météorite apparaît dans le coin en haut à droite. */
    public static final float METEOR_START = 3.3f;
    /** La météorite s'écrase derrière la ville (voir le son cutscene/comet). */
    public static final float IMPACT       = 5.6f;
    /** L'écran commence à blanchir. */
    public static final float WHITE_START  = 5.9f;
    /** L'écran est tout blanc : le combat commence dessous. */
    public static final float WHITE_FULL   = 6.8f;
    private static final float WHITE_HOLD  = 0.2f;
    private static final float WHITE_OUT   = 0.9f;
    private static final float SKIP_WHITE  = 0.35f;   // durée du blanc quand on passe la cinématique

    private static final float MARGIN      = 40f;     // le décor déborde, pour les secousses
    private static final int   SKY_BANDS   = 48;
    private static final int   STAR_COUNT  = 170;
    private static final float CITY_SCALE  = 4f;
    private static final float MOON_SCALE  = 4f;
    private static final float METEOR_SCALE_FROM = 1.6f;
    private static final float METEOR_SCALE_TO   = 6.5f;

    private static final Color SKY_TOP     = c("05071a");
    private static final Color SKY_LOW     = c("2b1a4a");
    private static final Color HORIZON     = c("5a2c5e");
    private static final Color FIRE_HOT    = c("fff6c8");
    private static final Color FIRE_GOLD   = c("ffc93a");
    private static final Color FIRE_ORANGE = c("ff7a1a");
    private static final Color FIRE_RED    = c("c92a12");
    private static final Color SMOKE       = c("2a1a1e");

    private final VisualSettings settings;
    private final ScreenShake    shake;
    private final Sound          sound;
    private final Random         random = new Random();
    private final List<Texture>  textures = new ArrayList<>();
    private final TextureRegion  pixel, soft, trail, ring, meteor, city, moon;

    private final float[] starX = new float[STAR_COUNT], starY = new float[STAR_COUNT];
    private final float[] starSize = new float[STAR_COUNT], starPhase = new float[STAR_COUNT];
    private final Array<Streak> streaks = new Array<>(false, 64);
    private final Array<Flame>  flames  = new Array<>(false, 256);
    private final Array<Flame>  debris  = new Array<>(false, 96);
    private final Color tmp = new Color();

    private Runnable onWhite;
    private float    time;
    private float    skipAt = -1f;      // instant où le joueur a passé la cinématique
    private float    whiteAt = -1f;     // instant où l'écran est devenu tout blanc
    private float    streakDebt, flameDebt, nextRumble;
    private boolean  exploded;
    private long     soundId = -1;
    private float    meteorX, meteorY, meteorScale, meteorAngle, meteorDirX = -1f, meteorDirY = -1f;

    public CometCutscene(VisualSettings settings, ScreenShake shake, Sound sound) {
        this.settings = settings;
        this.shake    = shake;
        this.sound    = sound;
        pixel  = region(solid(), false);
        soft   = region(softDisc(64), true);
        trail  = region(trailGradient(), true);
        ring   = region(softRing(128), true);
        meteor = region(meteorRock(), false);
        city   = region(skyline(), false);
        moon   = region(crescent(), false);
        setVisible(false);
        setTouchable(Touchable.disabled);
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                skip();
                return true;
            }
        });
    }

    /**
     * Lance la cinématique par-dessus tout l'écran ; {@code onWhite} est appelé
     * une fois, quand l'écran est tout blanc (le combat doit commencer là).
     */
    public void play(Runnable onWhite) {
        this.onWhite = onWhite;
        time = 0f;
        skipAt = -1f;
        whiteAt = -1f;
        streakDebt = flameDebt = 0f;
        nextRumble = METEOR_START + 0.6f;
        exploded = false;
        streaks.clear();
        flames.clear();
        debris.clear();
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = random.nextFloat();
            starY[i] = 0.25f + 0.75f * (float) Math.pow(random.nextFloat(), 0.8f);
            starSize[i] = random.nextFloat() < 0.12f ? 3f : random.nextFloat() < 0.4f ? 2f : 1.5f;
            starPhase[i] = random.nextFloat() * MathUtils.PI2;
        }
        setVisible(true);
        setTouchable(Touchable.enabled);
        toFront();
        soundId = sound.play();
    }

    /** @return {@code true} tant que la cinématique joue, jusqu'à ce que l'écran blanc commence à s'estomper. */
    public boolean isPlaying() { return isVisible() && whiteAt < 0f; }

    /** Passe la cinématique : l'écran blanchit tout de suite, puis le combat commence. */
    public void skip() {
        if (!isPlaying() || skipAt >= 0f || time >= WHITE_START) return;
        skipAt = time;
        if (soundId != -1) sound.stop(soundId);
        soundId = -1;
    }

    /** Menu pause : le son se fige avec l'image. */
    public void pauseSound() {
        if (isVisible() && soundId != -1) sound.pause(soundId);
    }

    /** Fin de la pause : le son reprend. */
    public void resumeSound() {
        if (isVisible() && soundId != -1) sound.resume(soundId);
    }

    /** Arrête et cache la cinématique sans lancer le combat. */
    public void cancel() {
        if (soundId != -1) sound.stop(soundId);
        soundId = -1;
        onWhite = null;
        setVisible(false);
        setTouchable(Touchable.disabled);
    }

    @Override
    public Actor hit(float x, float y, boolean touchable) {
        return touchable && getTouchable() == Touchable.enabled && isVisible() ? this : null;
    }

    // -------------------------------------------------------------------------
    // Déroulement
    // -------------------------------------------------------------------------

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isVisible()) return;
        time += delta;
        if (whiteAt >= 0f) {
            if (time - whiteAt >= WHITE_HOLD + WHITE_OUT) {
                setVisible(false);
                setTouchable(Touchable.disabled);
            }
            return;
        }
        if (skipAt < 0f) simulate(delta);
        if (whiteAlpha() >= 1f) {
            whiteAt = time;
            setTouchable(Touchable.disabled); // le combat se joue déjà dessous
            Runnable callback = onWhite;
            onWhite = null;
            if (callback != null) callback.run();
        }
    }

    /** Fait vivre le ciel : étoiles filantes, météorite, flammes et explosion. */
    private void simulate(float delta) {
        float width = worldWidth(), height = worldHeight();
        // Étoiles filantes : de plus en plus nombreuses, puis plus aucune.
        if (time > 0.4f && time < STARS_END) {
            float rate = time < STARS_PEAK ? MathUtils.lerp(3f, 26f, (time - 0.4f) / (STARS_PEAK - 0.4f))
                : MathUtils.lerp(26f, 4f, (time - STARS_PEAK) / (STARS_END - STARS_PEAK));
            streakDebt += rate * delta;
            while (streakDebt >= 1f) {
                streakDebt -= 1f;
                streaks.add(new Streak(width, height));
            }
        }
        for (int i = streaks.size - 1; i >= 0; i--) {
            Streak streak = streaks.get(i);
            streak.age += delta;
            streak.x += streak.vx * delta;
            streak.y += streak.vy * delta;
            if (streak.age >= streak.life) streaks.removeIndex(i);
        }
        // La météorite.
        if (time >= METEOR_START && time < IMPACT) {
            float progress = (time - METEOR_START) / (IMPACT - METEOR_START);
            float eased = Interpolation.pow2In.apply(progress) * 0.6f + progress * 0.4f;
            float fromX = width * 1.12f, fromY = height * 1.15f;
            float toX = impactX(), toY = impactY();
            float oldX = meteorX, oldY = meteorY;
            meteorX = MathUtils.lerp(fromX, toX, eased);
            meteorY = MathUtils.lerp(fromY, toY, eased) + 60f * 4f * eased * (1f - eased);
            if (progress > 0f) {
                float dx = meteorX - oldX, dy = meteorY - oldY, length = (float) Math.sqrt(dx * dx + dy * dy);
                if (length > 0.01f) {
                    meteorDirX = dx / length;
                    meteorDirY = dy / length;
                }
            }
            meteorScale = MathUtils.lerp(METEOR_SCALE_FROM, METEOR_SCALE_TO, eased);
            meteorAngle -= 120f * delta;
            flameDebt += MathUtils.lerp(70f, 220f, progress) * delta;
            while (flameDebt >= 1f) {
                flameDebt -= 1f;
                flames.add(Flame.fire(meteorX, meteorY, meteorDirX, meteorDirY, meteorScale, random));
            }
            if (time >= nextRumble) {
                shake.shake(0.25f, 1.5f + 6f * progress);
                nextRumble += 0.22f;
            }
        }
        if (!exploded && time >= IMPACT) {
            exploded = true;
            shake.shake(1.1f, 22f);
            for (int i = 0; i < 70; i++) debris.add(Flame.debris(impactX(), impactY(), random));
            for (int i = 0; i < 60; i++) flames.add(Flame.blast(impactX(), impactY(), random));
        }
        for (int i = flames.size - 1; i >= 0; i--) {
            if (flames.get(i).update(delta)) flames.removeIndex(i);
        }
        for (int i = debris.size - 1; i >= 0; i--) {
            if (debris.get(i).update(delta)) debris.removeIndex(i);
        }
    }

    /** @return l'opacité du voile blanc (1 = écran tout blanc). */
    private float whiteAlpha() {
        if (whiteAt >= 0f) {
            float out = time - whiteAt - WHITE_HOLD;
            return out <= 0f ? 1f : 1f - Interpolation.pow2.apply(Math.min(1f, out / WHITE_OUT));
        }
        float alpha = time <= WHITE_START ? 0f
            : Interpolation.pow2In.apply(Math.min(1f, (time - WHITE_START) / (WHITE_FULL - WHITE_START)));
        if (skipAt >= 0f) alpha = Math.max(alpha, Math.min(1f, (time - skipAt) / SKIP_WHITE));
        return alpha;
    }

    // -------------------------------------------------------------------------
    // Dessin
    // -------------------------------------------------------------------------

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float white = whiteAlpha();
        if (whiteAt < 0f) drawScene(batch);
        if (white > 0f) fill(batch, Color.WHITE, white);
        batch.setColor(Color.WHITE);
    }

    private void drawScene(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        float left = -MARGIN, right = width + MARGIN, bottom = -MARGIN, top = height + MARGIN;
        float horizon = horizonY();
        float glow = time < METEOR_START ? 0f : Math.min(1f, (time - METEOR_START) / (IMPACT - METEOR_START));
        float blast = exploded ? Math.max(0f, 1f - (time - IMPACT) / 1.6f) : 0f;

        // Ciel : dégradé de la nuit vers l'horizon, qui rougit à l'approche de la météorite.
        float bandHeight = (top - horizon) / SKY_BANDS;
        for (int i = 0; i < SKY_BANDS; i++) {
            float k = i / (float) (SKY_BANDS - 1);   // 0 en bas, 1 en haut
            if (k < 0.35f) tmp.set(HORIZON).lerp(SKY_LOW, k / 0.35f);
            else tmp.set(SKY_LOW).lerp(SKY_TOP, (k - 0.35f) / 0.65f);
            float heat = (1f - k) * (glow * glow * 0.55f + blast * 0.7f);
            tmp.lerp(FIRE_ORANGE, Math.min(0.85f, heat));
            batch.setColor(tmp);
            batch.draw(pixel, left, horizon + i * bandHeight - 1f, right - left, bandHeight + 2f);
        }
        batch.setColor(HORIZON);
        batch.draw(pixel, left, bottom, right - left, horizon - bottom);

        // Croissant de lune, qui pâlit quand le ciel s'embrase.
        float moonSize = moon.getRegionWidth() * MOON_SCALE;
        float moonX = width * 0.14f, moonY = height * 0.78f;
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(1f, 0.92f, 0.7f, 0.25f * (1f - glow));
        batch.draw(soft, moonX - moonSize * 1.5f, moonY - moonSize * 1.5f, moonSize * 4f, moonSize * 4f);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(1f, 1f, 1f, 1f - 0.6f * glow);
        batch.draw(moon, moonX, moonY, moonSize, moonSize);

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        // Étoiles fixes qui scintillent (elles pâlissent quand le ciel s'embrase).
        float starFade = 1f - Math.min(1f, glow * 1.3f);
        for (int i = 0; i < STAR_COUNT; i++) {
            float twinkle = 0.45f + 0.55f * (0.5f + 0.5f * MathUtils.sin(time * (2f + i % 5) + starPhase[i]));
            float x = starX[i] * width, y = horizon + starY[i] * (height - horizon);
            float size = starSize[i];
            batch.setColor(1f, 0.96f, 0.85f, twinkle * starFade);
            batch.draw(pixel, x - size / 2f, y - size / 2f, size, size);
            if (size >= 3f) {
                batch.setColor(0.8f, 0.85f, 1f, 0.25f * twinkle * starFade);
                batch.draw(soft, x - 8f, y - 8f, 16f, 16f);
            }
        }
        // Étoiles filantes.
        for (Streak streak : streaks) {
            float life = streak.age / streak.life;
            float alpha = Math.min(1f, life * 6f) * (1f - life * life);
            float angle = MathUtils.atan2(streak.vy, streak.vx) * MathUtils.radiansToDegrees;
            batch.setColor(streak.color.r, streak.color.g, streak.color.b, alpha);
            batch.draw(trail, streak.x - streak.length, streak.y - streak.thickness / 2f, streak.length,
                streak.thickness / 2f, streak.length, streak.thickness, 1f, 1f, angle);
            batch.setColor(1f, 1f, 1f, alpha * 0.8f);
            float head = streak.thickness * 3f;
            batch.draw(soft, streak.x - head / 2f, streak.y - head / 2f, head, head);
        }
        // Explosion derrière la ville : boule de feu et onde de choc.
        if (exploded) drawBlast(batch, false);
        // Traîne de feu et météorite.
        drawFlames(batch);
        if (time >= METEOR_START && time < IMPACT) drawMeteor(batch);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // La ville de casinos en ombre chinoise, fenêtres allumées.
        float cityWidth = city.getRegionWidth() * CITY_SCALE, cityHeight = city.getRegionHeight() * CITY_SCALE;
        float cityLight = 0.55f + 0.45f * Math.max(glow * glow, blast);
        batch.setColor(cityLight, cityLight * 0.85f, cityLight * 0.8f, 1f);
        for (float x = impactX() - cityWidth * 0.49f; x > left; x -= cityWidth) {
            batch.draw(city, x - cityWidth, horizon - cityHeight * 0.18f, cityWidth, cityHeight);
        }
        for (float x = impactX() - cityWidth * 0.49f; x < right; x += cityWidth) {
            batch.draw(city, x, horizon - cityHeight * 0.18f, cityWidth, cityHeight);
        }
        batch.setColor(c("0b0712"));
        batch.draw(pixel, left, bottom, right - left, horizon - cityHeight * 0.18f - bottom + 1f);

        // Devant la ville : l'éclat de l'explosion et les débris.
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        if (exploded) drawBlast(batch, true);
        for (Flame piece : debris) {
            float life = piece.age / piece.life;
            tmp.set(FIRE_GOLD).lerp(FIRE_RED, life);
            batch.setColor(tmp.r, tmp.g, tmp.b, 1f - life);
            float angle = MathUtils.atan2(piece.vy, piece.vx) * MathUtils.radiansToDegrees;
            float length = piece.size * 3f;
            batch.draw(trail, piece.x - length, piece.y - piece.size / 2f, length, piece.size / 2f, length, piece.size,
                1f, 1f, angle);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Éclair de l'impact (pas avec les effets réduits).
        if (exploded && !settings.isReducedEffects()) {
            float flash = 1f - (time - IMPACT) / 0.3f;
            if (flash > 0f) fill(batch, FIRE_HOT, 0.85f * flash);
        }
        // Le ciel sort du noir.
        if (time < SKY_IN) fill(batch, Color.BLACK, 1f - Interpolation.pow2Out.apply(time / SKY_IN));
    }

    private void drawFlames(Batch batch) {
        for (Flame flame : flames) {
            float life = flame.age / flame.life;
            if (life < 0.25f) tmp.set(FIRE_HOT).lerp(FIRE_GOLD, life / 0.25f);
            else if (life < 0.55f) tmp.set(FIRE_GOLD).lerp(FIRE_ORANGE, (life - 0.25f) / 0.3f);
            else if (life < 0.8f) tmp.set(FIRE_ORANGE).lerp(FIRE_RED, (life - 0.55f) / 0.25f);
            else tmp.set(FIRE_RED).lerp(SMOKE, (life - 0.8f) / 0.2f);
            float alpha = (1f - life) * 0.75f;
            float size = flame.size * (0.6f + 1.2f * life);
            batch.setColor(tmp.r, tmp.g, tmp.b, alpha);
            batch.draw(soft, flame.x - size / 2f, flame.y - size / 2f, size, size);
        }
    }

    private void drawMeteor(Batch batch) {
        float size = meteor.getRegionWidth() * meteorScale;
        float pulse = 1f + 0.08f * MathUtils.sin(time * 40f);
        float halo = size * 4.2f * pulse;
        batch.setColor(FIRE_ORANGE.r, FIRE_ORANGE.g, FIRE_ORANGE.b, 0.55f);
        batch.draw(soft, meteorX - halo / 2f, meteorY - halo / 2f, halo, halo);
        halo = size * 2.2f * pulse;
        batch.setColor(FIRE_GOLD.r, FIRE_GOLD.g, FIRE_GOLD.b, 0.8f);
        batch.draw(soft, meteorX - halo / 2f, meteorY - halo / 2f, halo, halo);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(Color.WHITE);
        batch.draw(meteor, meteorX - size / 2f, meteorY - size / 2f, size / 2f, size / 2f, size, size, 1f, 1f,
            meteorAngle);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        // Le front de la météorite chauffé à blanc.
        halo = size * 1.1f;
        batch.setColor(FIRE_HOT.r, FIRE_HOT.g, FIRE_HOT.b, 0.6f);
        batch.draw(soft, meteorX + meteorDirX * size * 0.3f - halo / 2f, meteorY + meteorDirY * size * 0.3f - halo / 2f,
            halo, halo);
    }

    /** La boule de feu et l'onde de choc ; {@code front} : la lueur qui déborde devant la ville. */
    private void drawBlast(Batch batch, boolean front) {
        float since = time - IMPACT;
        float x = impactX(), y = impactY();
        float grow = Interpolation.pow3Out.apply(Math.min(1f, since / 1.1f));
        float fade = Math.max(0f, 1f - since / 2.2f);
        float radius = worldWidth() * 0.55f * grow;
        if (front) {
            float glowSize = radius * 1.2f;
            batch.setColor(FIRE_GOLD.r, FIRE_GOLD.g, FIRE_GOLD.b, 0.45f * fade);
            batch.draw(soft, x - glowSize, y - glowSize * 0.6f, glowSize * 2f, glowSize * 1.2f);
            return;
        }
        Color[] layers = {FIRE_RED, FIRE_ORANGE, FIRE_GOLD, FIRE_HOT};
        float[] sizes = {1f, 0.78f, 0.55f, 0.32f};
        for (int i = 0; i < layers.length; i++) {
            float layer = radius * sizes[i] * 2f;
            batch.setColor(layers[i].r, layers[i].g, layers[i].b, 0.9f * fade);
            batch.draw(soft, x - layer / 2f, y - layer / 2f, layer, layer);
        }
        float wave = Interpolation.pow2Out.apply(Math.min(1f, since / 0.9f));
        float waveWidth = worldWidth() * 1.6f * wave;
        batch.setColor(1f, 0.9f, 0.7f, 0.8f * (1f - wave));
        batch.draw(ring, x - waveWidth / 2f, y - waveWidth * 0.14f, waveWidth, waveWidth * 0.28f);
        float sky = worldWidth() * 0.9f * wave;
        batch.setColor(1f, 0.8f, 0.5f, 0.5f * (1f - wave));
        batch.draw(ring, x - sky / 2f, y - sky / 2f, sky, sky);
    }

    private void fill(Batch batch, Color color, float alpha) {
        batch.setColor(color.r, color.g, color.b, alpha);
        batch.draw(pixel, -MARGIN, -MARGIN, worldWidth() + 2f * MARGIN, worldHeight() + 2f * MARGIN);
    }

    private float worldWidth()  { return getStage().getViewport().getWorldWidth(); }
    private float worldHeight() { return getStage().getViewport().getWorldHeight(); }
    private float horizonY()    { return worldHeight() * 0.2f; }
    private float impactX()     { return worldWidth() * 0.46f; }
    private float impactY()     { return horizonY() + city.getRegionHeight() * CITY_SCALE * 0.25f; }

    // -------------------------------------------------------------------------
    // Particules
    // -------------------------------------------------------------------------

    /** Une étoile filante : traverse le ciel en diagonale, vers le bas à gauche. */
    private final class Streak {
        float x, y, vx, vy, age, life, length, thickness;
        final Color color;

        Streak(float width, float height) {
            x = width * (0.15f + 1.1f * random.nextFloat());
            y = height * (0.55f + 0.5f * random.nextFloat());
            float angle = MathUtils.degreesToRadians * (200f + 18f * random.nextFloat());
            float speed = 900f + 800f * random.nextFloat();
            vx = MathUtils.cos(angle) * speed;
            vy = MathUtils.sin(angle) * speed;
            life = 0.5f + 0.6f * random.nextFloat();
            length = 120f + 220f * random.nextFloat();
            thickness = random.nextFloat() < 0.2f ? 6f : 3.5f;
            float hue = random.nextFloat();
            color = hue < 0.5f ? Color.WHITE : hue < 0.8f ? c("ffe9a0") : c("b8d4ff");
        }
    }

    /** Flamme, fumée ou débris : part d'un point, ralentit (ou retombe), grossit et s'éteint. */
    private static final class Flame {
        float x, y, vx, vy, age, life, size, gravity, drag;

        /** @return {@code true} quand la particule est éteinte. */
        boolean update(float delta) {
            age += delta;
            vy -= gravity * delta;
            float slow = (float) Math.pow(drag, delta);
            vx *= slow;
            vy *= slow;
            x += vx * delta;
            y += vy * delta;
            return age >= life;
        }

        static Flame fire(float x, float y, float dirX, float dirY, float scale, Random random) {
            Flame flame = new Flame();
            float spread = 10f * scale;
            flame.x = x + (random.nextFloat() - 0.5f) * spread;
            flame.y = y + (random.nextFloat() - 0.5f) * spread;
            float speed = 80f + 160f * random.nextFloat();
            flame.vx = -dirX * speed + (random.nextFloat() - 0.5f) * 90f;
            flame.vy = -dirY * speed + (random.nextFloat() - 0.5f) * 90f + 30f;
            flame.life = 0.45f + 0.5f * random.nextFloat();
            flame.size = (14f + 10f * random.nextFloat()) * scale;
            flame.gravity = -40f;  // la fumée monte
            flame.drag = 0.3f;
            return flame;
        }

        static Flame blast(float x, float y, Random random) {
            Flame flame = new Flame();
            float angle = MathUtils.PI * random.nextFloat();
            float speed = 250f + 650f * random.nextFloat();
            flame.x = x;
            flame.y = y;
            flame.vx = MathUtils.cos(angle) * speed * 1.4f;
            flame.vy = MathUtils.sin(angle) * speed;
            flame.life = 0.8f + 0.9f * random.nextFloat();
            flame.size = 60f + 90f * random.nextFloat();
            flame.gravity = -60f;
            flame.drag = 0.12f;
            return flame;
        }

        static Flame debris(float x, float y, Random random) {
            Flame flame = new Flame();
            float angle = MathUtils.degreesToRadians * (20f + 140f * random.nextFloat());
            float speed = 500f + 900f * random.nextFloat();
            flame.x = x;
            flame.y = y;
            flame.vx = MathUtils.cos(angle) * speed;
            flame.vy = MathUtils.sin(angle) * speed;
            flame.life = 0.9f + 0.9f * random.nextFloat();
            flame.size = 4f + 5f * random.nextFloat();
            flame.gravity = 900f;
            flame.drag = 0.7f;
            return flame;
        }
    }

    // -------------------------------------------------------------------------
    // Images
    // -------------------------------------------------------------------------

    private TextureRegion region(Pixmap pixmap, boolean smooth) {
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        if (smooth) texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        textures.add(texture);
        return new TextureRegion(texture);
    }

    private static Pixmap solid() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        return pixmap;
    }

    /** @return un disque blanc qui s'estompe doucement vers le bord. */
    private static Pixmap softDisc(int size) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        float center = (size - 1) / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float d = (float) Math.hypot(x - center, y - center) / (size / 2f);
                float a = d >= 1f ? 0f : (1f - d) * (1f - d);
                pixmap.setColor(1f, 1f, 1f, a);
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }

    /** @return un anneau blanc flou (onde de choc). */
    private static Pixmap softRing(int size) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        float center = (size - 1) / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float d = (float) Math.hypot(x - center, y - center) / (size / 2f);
                float a = Math.max(0f, 1f - Math.abs(d - 0.88f) / 0.1f);
                pixmap.setColor(1f, 1f, 1f, a * a);
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }

    /** @return la traîne d'une étoile filante : transparente à gauche, éclatante à droite (la tête). */
    private static Pixmap trailGradient() {
        int width = 128, height = 8;
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        for (int x = 0; x < width; x++) {
            float along = x / (float) (width - 1);
            for (int y = 0; y < height; y++) {
                float across = 1f - Math.abs(y - (height - 1) / 2f) / (height / 2f);
                float taper = along * along;
                pixmap.setColor(1f, 1f, 1f, taper * Math.max(0f, across) * (0.3f + 0.7f * along));
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }

    /** @return la météorite en pixel art : roche sombre craquelée de lave, cernée de noir. */
    private static Pixmap meteorRock() {
        int size = 22;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        Random random = new Random(7);
        float center = (size - 1) / 2f;
        float[] bumps = new float[12];
        for (int i = 0; i < bumps.length; i++) bumps[i] = 8.2f + 1.8f * random.nextFloat();
        Color outline = c("140a0a"), dark = c("3a1f1c"), rock = c("5e3428"), light = c("8a5236");
        Color lava = c("ff8a1e"), hot = c("ffe066");
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - center, dy = y - center;
                float angle = (MathUtils.atan2(dy, dx) + MathUtils.PI) / MathUtils.PI2 * bumps.length;
                int a = (int) angle % bumps.length;
                float radius = MathUtils.lerp(bumps[a], bumps[(a + 1) % bumps.length], angle - (int) angle);
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                if (d > radius) continue;
                Color color;
                if (d > radius - 1.2f) color = outline;
                else if (dx + dy < -6f) color = light;          // éclairée en haut à gauche (Pixmap : y vers le bas)
                else if (dx + dy > 5f) color = dark;
                else color = rock;
                // Fissures de lave.
                float crack = MathUtils.sin(dx * 1.3f + dy * 0.7f) + MathUtils.sin(dy * 1.7f - dx * 0.4f);
                if (d < radius - 1.5f && Math.abs(crack) < 0.18f) color = d < radius * 0.5f ? hot : lava;
                pixmap.setColor(color);
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }

    /** @return la ville de casinos en ombre chinoise : immeubles, enseignes et une tour à étoile, fenêtres dorées. */
    private static Pixmap skyline() {
        int width = 340, height = 72;
        int towerX = 160, towerWidth = 16;                       // la tour du grand casino
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        Random random = new Random(42);
        Color body = c("0b0712"), window = c("ffcf5a"), windowDim = c("8a5a2a"), neon = c("ff4fa0"), neon2 = c("4fd8ff");
        int x = 0;
        while (x < width) {
            boolean tower = x >= towerX - 4 && x <= towerX + 4;
            if (!tower && x < towerX && x + 20 > towerX) x = towerX;  // laisse la place à la tour
            tower = x == towerX;
            int buildingWidth = tower ? towerWidth : 8 + random.nextInt(14);
            int buildingHeight = tower ? 56 : 12 + random.nextInt(28);
            int top = height - buildingHeight;
            pixmap.setColor(body);
            pixmap.fillRectangle(x, top, buildingWidth, buildingHeight);
            for (int wy = top + 3; wy < height - 2; wy += 3) {
                for (int wx = x + 2; wx < x + buildingWidth - 2; wx += 3) {
                    float roll = random.nextFloat();
                    if (roll < 0.45f) continue;
                    pixmap.setColor(roll < 0.85f ? window : windowDim);
                    pixmap.drawPixel(wx, wy);
                }
            }
            if (tower) {                                             // l'étoile du casino au sommet
                int cx = x + buildingWidth / 2;
                pixmap.setColor(body);
                pixmap.fillRectangle(x + 3, top - 3, buildingWidth - 6, 3);
                pixmap.fillRectangle(cx, top - 9, 1, 6);
                pixmap.setColor(neon);
                pixmap.fillRectangle(x + 1, top + 6, buildingWidth - 2, 2);
                pixmap.setColor(window);
                pixmap.fillRectangle(cx - 1, top - 12, 3, 3);
                pixmap.drawPixel(cx, top - 14);
                pixmap.drawPixel(cx, top - 8);
                pixmap.drawPixel(cx - 3, top - 11);
                pixmap.drawPixel(cx + 3, top - 11);
            } else if (random.nextFloat() < 0.35f && buildingWidth > 10) {   // enseigne au néon sur le toit
                pixmap.setColor(random.nextBoolean() ? neon : neon2);
                pixmap.fillRectangle(x + 2, top - 3, buildingWidth - 4, 2);
            }
            x += buildingWidth + random.nextInt(3);
        }
        return pixmap;
    }

    /** @return un croissant de lune pâle en pixel art. */
    private static Pixmap crescent() {
        int size = 21;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        Color pale = c("fff4d6"), shade = c("e8d6a8");
        float center = (size - 1) / 2f, radius = 9.6f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float d = (float) Math.hypot(x - center, y - center);
                float cut = (float) Math.hypot(x - center - 6f, y - center + 3f);
                if (d > radius || cut < radius * 0.92f) continue;
                pixmap.setColor(d > radius - 2f || cut < radius * 0.92f + 1.5f ? shade : pale);
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }

    private static Color c(String hex) { return Color.valueOf(hex); }

    @Override
    public void dispose() {
        cancel();
        for (Texture texture : textures) texture.dispose();
        textures.clear();
    }
}
