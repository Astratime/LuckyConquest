package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.animations.ScreenShake;
import fr.astratime.lucky.settings.VisualSettings;

import java.util.Random;

/**
 * Cinématique avant la Comète Dorée (boss du chapitre 1 de la Tour des
 * épreuves) : d'abord la ville de casinos, calme ; les enseignes clignotent,
 * une machine à sous attend seule dans une rue. Puis le ciel nocturne au-dessus
 * de la ville, une pluie d'étoiles filantes (l'une tombe en dés). Une météorite
 * en feu traverse le ciel ; les enseignes grésillent et s'éteignent une à une,
 * les passants lèvent la tête. Elle s'écrase derrière la ville. L'explosion s'ouvre en boule de feu et en onde
 * de choc, puis tout l'écran passe au blanc ; la présentation du boss apparaît sous le blanc
 * ({@code onWhite}), qui s'estompe ensuite.
 *
 * Un clic ou une touche passe la cinématique : l'écran blanchit tout de suite.
 * Avec les effets réduits, ni secousse ni éclair à l'impact (le fondu blanc reste).
 *
 * Tout est dessiné à chaque image (pas d'enfants) ; l'acteur couvre le Stage
 * et bloque les clics tant qu'il est visible.
 */
public class CometCutscene extends Cutscene {

    /** La rue de casinos, calme (voir le son cutscene/comet) ; puis le ciel. */
    public static final float STREET_END   = 1.5f;
    /** Le ciel sort du noir. */
    public static final float SKY_IN       = 2.3f;
    /** La pluie d'étoiles filantes s'arrête de grossir. */
    public static final float STARS_PEAK   = 4.1f;
    /** Une étoile filante tombe en dés. */
    public static final float DICE_STAR    = 4.6f;
    /** Plus de nouvelles étoiles filantes. */
    public static final float STARS_END    = 6.7f;
    /** La météorite apparaît dans le coin en haut à droite. */
    public static final float METEOR_START = 5.8f;
    /** Les enseignes grésillent et s'éteignent une à une ; les passants lèvent la tête. */
    public static final float SIGNS_OUT    = 7.6f;
    public static final float LOOK_UP      = 7.8f;
    /** La météorite s'écrase derrière la ville. */
    public static final float IMPACT       = 9.6f;
    /** L'écran commence à blanchir. */
    public static final float WHITE_START  = 9.9f;
    /** L'écran est tout blanc : la présentation du boss apparaît dessous. */
    public static final float WHITE_FULL   = 10.8f;
    /** Les enseignes du premier plan : position (fraction de la largeur) et couleur. */
    private static final float[] SIGNS     = {0.08f, 0.27f, 0.62f, 0.83f};
    private static final Color[] NEONS     = {c("ff4fa0"), c("4fd8ff"), c("ffc93a"), c("ff4fa0")};
    /** Les passants : position (fraction de la largeur). */
    private static final float[] PEOPLE    = {0.17f, 0.21f, 0.39f, 0.52f, 0.56f, 0.74f, 0.92f};
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

    private final TextureRegion  meteor, city, moon, slot, die, walker, watcher;

    private final float[] starX = new float[STAR_COUNT], starY = new float[STAR_COUNT];
    private final float[] starSize = new float[STAR_COUNT], starPhase = new float[STAR_COUNT];
    private final Array<Streak> streaks = new Array<>(false, 64);
    private final Array<Flame>  flames  = new Array<>(false, 256);
    private final Array<Flame>  debris  = new Array<>(false, 96);

    private float    streakDebt, flameDebt, nextRumble;
    private boolean  exploded;
    private float    meteorX, meteorY, meteorScale, meteorAngle, meteorDirX = -1f, meteorDirY = -1f;

    public CometCutscene(VisualSettings settings, ScreenShake shake, Sound sound) {
        super(settings, shake, sound);
        meteor = region(meteorRock(), false);
        city   = region(skyline(), false);
        moon   = region(crescent(), false);
        Color[] slotColors = {c("140a0a"), c("e0283a"), c("7a0f20"), c("ffc93a"), c("fff6c8"), c("f4ecdc"),
            c("1a1418"), c("c8c8d4")};
        slot = art("ordgywkc", slotColors,
            "...gggggggggg...r",
            "..gyggyggyggyg..c",
            "..oooooooooooo..c",
            "..orrrrrrrrrro..c",
            "..orwwwwwwwwro..c",
            "..orwrwwgwwkro.cc",
            "..orwrwggwwkro.c.",
            "..orwwwwwwwwrocc.",
            "..orrrrrrrrrro...",
            "..orddddddddro...",
            "..ordkkkkkkdro...",
            "..orddddddddro...",
            "..orrrrrrrrrro...",
            "..orrrrrrrrrro...",
            "..oooooooooooo...");
        die = art("owr", new Color[] {c("1a0a0e"), c("f4ecdc"), c("e0283a")},
            "ooooooo",
            "owwwwwo",
            "owrwwwo",
            "owwrwwo",
            "owwwrwo",
            "owwwwwo",
            "ooooooo");
        Color[] ink = {c("05030a")};
        walker = art("o", ink,
            ".oo.",
            ".oo.",
            "oooo",
            "oooo",
            "oooo",
            ".oo.",
            ".oo.",
            "o..o");
        watcher = art("o", ink,
            "..oo",
            ".oo.",
            "oooo",
            "oooo",
            "oooo",
            ".oo.",
            ".oo.",
            "o..o");
    }

    @Override protected float coverStart() { return WHITE_START; }
    @Override protected float coverFull()  { return WHITE_FULL; }

    @Override
    protected void reset() {
        streakDebt = flameDebt = 0f;
        nextRumble = METEOR_START + 1.0f;
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
    }

    // -------------------------------------------------------------------------
    // Déroulement
    // -------------------------------------------------------------------------

    /** Fait vivre le ciel : étoiles filantes, météorite, flammes et explosion. */
    @Override
    protected void simulate(float delta) {
        float width = worldWidth(), height = worldHeight();
        // Étoiles filantes : de plus en plus nombreuses, puis plus aucune.
        if (time > STREET_END + 0.4f && time < STARS_END) {
            float rate = time < STARS_PEAK ? MathUtils.lerp(3f, 26f, (time - STREET_END - 0.4f) / (STARS_PEAK - STREET_END - 0.4f))
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

    // -------------------------------------------------------------------------
    // Dessin
    // -------------------------------------------------------------------------

    @Override
    protected void drawScene(Batch batch) {
        if (time < STREET_END) {
            drawStreet(batch);
            fadeFromBlack(batch, 0.6f);
            fadeThroughBlack(batch, STREET_END, 0.25f);
            return;
        }
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
        drawDiceStar(batch);
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
        drawForeground(batch, Math.max(glow * glow, blast));

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
        fadeThroughBlack(batch, STREET_END, 0.25f);
    }

    /**
     * La rue de casinos, avant tout : façades, enseignes au néon qui clignotent,
     * et une machine à sous seule sur le trottoir, ses ampoules allumées.
     */
    private void drawStreet(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        float pan = progress(0f, STREET_END) * 60f;
        fill(batch, c("1a1028"), 1f);
        float u = height / 80f;
        for (int i = 0; i < 4; i++) {                                   // façades et vitrines
            float x = -MARGIN + i * width * 0.28f - pan;
            batch.setColor(i % 2 == 0 ? c("22152e") : c("2a1a36"));
            batch.draw(pixel, x, height * 0.25f, width * 0.27f, height * 0.8f);
            for (int w = 0; w < 3; w++) {
                batch.setColor(c("ffcf5a").r, c("ffcf5a").g * 0.8f, 0.3f, 0.35f + 0.1f * ((i + w) % 2));
                batch.draw(pixel, x + width * (0.03f + w * 0.08f), height * 0.62f, width * 0.05f, height * 0.12f);
            }
            Color neon = NEONS[i % NEONS.length];                       // l'enseigne, qui clignote
            boolean on = ((int) (time * 3f) + i) % 3 != 0;
            float sx = x + width * 0.04f, sy = height * 0.82f, sw = width * 0.19f, sh = height * 0.07f;
            if (on) glow(batch, sx + sw / 2f, sy + sh / 2f, sw * 1.6f, neon, 0.35f);
            batch.setColor(on ? neon : tmp.set(neon).mul(0.35f, 0.35f, 0.35f, 1f));
            batch.draw(pixel, sx, sy, sw, 4f);
            batch.draw(pixel, sx, sy + sh - 4f, sw, 4f);
            batch.draw(pixel, sx, sy, 4f, sh);
            batch.draw(pixel, sx + sw - 4f, sy, 4f, sh);
            for (int k = 0; k < 5; k++) batch.draw(pixel, sx + sw * (0.12f + k * 0.17f), sy + sh * 0.3f, sw * 0.09f, sh * 0.4f);
        }
        batch.setColor(c("0e0816"));                                     // le trottoir
        batch.draw(pixel, -MARGIN, -MARGIN, width + 2f * MARGIN, height * 0.25f + MARGIN);
        batch.setColor(c("2a1e36"));
        batch.draw(pixel, -MARGIN, height * 0.25f - 6f, width + 2f * MARGIN, 6f);
        // La machine à sous, seule ; ses ampoules clignotent.
        float scale = 2.8f * u, mx = width * 0.5f - pan * 0.5f, my = height * 0.18f;
        glow(batch, mx, my + 6f * scale, 40f * scale, c("ffc93a"), 0.3f + 0.1f * MathUtils.sin(time * 6f));
        batch.setColor(Color.WHITE);
        batch.draw(slot, mx - slot.getRegionWidth() * scale / 2f, my, slot.getRegionWidth() * scale,
            slot.getRegionHeight() * scale);
        for (int k = 0; k < 4; k++) {
            if (((int) (time * 6f) + k) % 2 == 0) glow(batch, mx + (k - 1.5f) * 3f * scale - 0.5f * scale,
                my + 13.5f * scale, 3f * scale, c("fff6c8"), 0.8f);
        }
    }

    /** Une étoile filante qui tombe en dés : deux dés qui culbutent, avec une petite traîne. */
    private void drawDiceStar(Batch batch) {
        if (time < DICE_STAR || time > DICE_STAR + 0.9f) return;
        float k = progress(DICE_STAR, DICE_STAR + 0.9f);
        float width = worldWidth(), height = worldHeight();
        for (int i = 0; i < 2; i++) {
            float x = MathUtils.lerp(width * 0.2f, width * 0.75f, k) - i * 60f;
            float y = MathUtils.lerp(height * 0.95f, height * 0.5f, k) + i * 30f;
            float alpha = Math.min(1f, k * 6f) * (1f - k * k);
            additive(batch);
            batch.setColor(1f, 0.95f, 0.8f, 0.6f * alpha);
            batch.draw(trail, x - 220f, y - 3f, 220f, 3f, 220f, 6f, 1f, 1f, -39f);
            normal(batch);
            batch.setColor(1f, 1f, 1f, alpha);
            sprite(batch, die, x, y, 5f, k * 900f + i * 45f);
        }
        additive(batch);
    }

    /**
     * Le premier plan : enseignes au néon au bas des façades (elles grésillent
     * et s'éteignent une à une quand la météorite approche) et passants, qui
     * lèvent la tête vers le ciel.
     */
    private void drawForeground(Batch batch, float fire) {
        float width = worldWidth(), height = worldHeight();
        float base = height * 0.05f, u = height / 90f;
        normal(batch);
        for (int i = 0; i < SIGNS.length; i++) {
            float out = SIGNS_OUT + i * 0.35f;
            boolean on = time < out || (time < out + 0.3f && random.nextFloat() < 0.5f);   // il grésille, puis s'éteint
            Color neon = NEONS[i];
            float sx = width * SIGNS[i], sy = base + 18f * u, sw = 22f * u, sh = 6f * u;
            // La devanture du casino qui porte l'enseigne : façade, toit, porte éclairée.
            float fw = sw * 1.4f, fh = sy + sh + 3f * u - base;
            batch.setColor(c("120a1c"));
            batch.draw(pixel, sx - fw / 2f, -MARGIN, fw, fh + base + MARGIN);
            batch.setColor(c("2a1a36"));
            batch.draw(pixel, sx - fw / 2f - u, base + fh, fw + 2f * u, 1.2f * u);
            float door = on ? 0.55f : 0.2f;
            batch.setColor(1f, 0.8f, 0.4f, door);
            batch.draw(pixel, sx - 3f * u, base, 6f * u, 10f * u);
            batch.setColor(c("120a1c"));
            batch.draw(pixel, sx - 0.4f * u, base, 0.8f * u, 10f * u);
            if (on) glow(batch, sx, sy + sh / 2f, sw * 2f, neon, 0.4f);
            batch.setColor(on ? neon : tmp.set(neon).mul(0.25f, 0.25f, 0.25f, 1f));
            batch.draw(pixel, sx - sw / 2f, sy, sw, 0.8f * u);
            batch.draw(pixel, sx - sw / 2f, sy + sh - 0.8f * u, sw, 0.8f * u);
            batch.draw(pixel, sx - sw / 2f, sy, 0.8f * u, sh);
            batch.draw(pixel, sx + sw / 2f - 0.8f * u, sy, 0.8f * u, sh);
            for (int k = 0; k < 4; k++) batch.draw(pixel, sx - sw / 2f + sw * (0.15f + k * 0.2f), sy + sh * 0.3f, sw * 0.1f, sh * 0.4f);
            if (time >= out && time < out + 0.3f) {                       // les étincelles du néon qui grille
                glow(batch, sx + (random.nextFloat() - 0.5f) * sw, sy + sh, 4f * u, Color.WHITE, 0.8f);
            }
        }
        for (int i = 0; i < PEOPLE.length; i++) {
            boolean looking = time >= LOOK_UP + i * 0.12f;
            TextureRegion pose = looking ? watcher : walker;
            float scale = 1.1f * u, x = width * PEOPLE[i];
            if (fire > 0f) glow(batch, x, base + 8f * scale, 10f * scale, FIRE_ORANGE, 0.35f * fire);   // la lueur du ciel sur eux
            batch.setColor(Color.WHITE);
            batch.draw(pose, x - pose.getRegionWidth() * scale / 2f, base, pose.getRegionWidth() * scale,
                pose.getRegionHeight() * scale);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
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
}
