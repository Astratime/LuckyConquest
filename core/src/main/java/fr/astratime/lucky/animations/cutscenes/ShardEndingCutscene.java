package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.enemy.EnemyKind;

/**
 * Fin du chapitre 3, après la victoire sur l'Éclat Originel : le soleil doré
 * brille au-dessus de la ville, puis s'éteint et se brise. Ses éclats retombent
 * sur la ville comme de la neige, et les fenêtres s'allument une à une. Les
 * habitants sortent sur les toits et tendent les mains vers la neige dorée.
 * Trois éclats ne tombent pas : ils flottent, puis filent au loin vers
 * l'horizon (le Prétendant les ramassera au chapitre 4). Fondu noir, puis
 * « La chance n'a plus de maître. »
 */
public class ShardEndingCutscene extends Cutscene {

    /** Le soleil commence à s'éteindre (voir le son cutscene/shard_ending). */
    public static final float DIM_START   = 0.8f;
    /** Il se brise. */
    public static final float BREAK       = 2.0f;
    /** Les habitants sortent sur les toits, puis tendent les mains. */
    public static final float ROOFS       = 2.6f;
    public static final float HANDS_UP    = 3.6f;
    /** Les trois éclats qui ne tombent pas filent vers l'horizon. */
    public static final float SHARDS_FLY  = 6.2f;
    public static final float SHARDS_GONE = 8.2f;
    public static final float COVER_START = 10.0f;
    public static final float COVER_FULL  = 10.9f;

    /** Les trois éclats : la Comète, la Reine, l'Éclat Originel. */
    private static final Color[] SHARDS = {c("ffc93a"), c("e0283a"), c("fff6c8")};
    /** Les habitants sur les toits : position (fraction de la largeur) et quand ils lèvent les mains. */
    private static final float[][] PEOPLE = {{0.08f, 0f}, {0.15f, 0.3f}, {0.31f, 0.15f}, {0.38f, 0.5f},
        {0.57f, 0.1f}, {0.64f, 0.4f}, {0.71f, 0.2f}, {0.86f, 0.35f}, {0.93f, 0.05f}};

    private static final Color GOLD = c("ffc93a"), HOT = c("fff6c8"), SKY_TOP = c("05071a"), SKY_LOW = c("2b1a4a");

    private static final Color DAY_LOW = c("8a4a1e"), DAY_TOP = c("3a2a10");

    private final TextureRegion shard, city, personDown, personUp, gem;
    private final Color low = new Color(), top = new Color();
    private final Starfield stars = new Starfield(170, 0.3f);
    private final Array<Particle> flakes = new Array<>(false, 512);
    private final Array<Particle> burst  = new Array<>(false, 128);
    private float flakeDebt;
    private boolean broken;

    public ShardEndingCutscene(CutsceneKit kit) {
        super(kit.settings(), kit.shake(), kit.sound("shard_ending"));
        shard = kit.portrait(EnemyKind.ECLAT);
        city  = region(skyline(340, 72, 42, false), false);
        Color[] ink = {c("0b0712")};
        personDown = art("o", ink,
            "..oo..",
            "..oo..",
            ".oooo.",
            "oooooo",
            "o.oo.o",
            "o.oo.o",
            "..oo..",
            "..oo..",
            ".o..o.",
            ".o..o.");
        personUp = art("o", ink,
            "o....o",
            "o.oo.o",
            "o.oo.o",
            ".oooo.",
            "..oo..",
            "..oo..",
            "..oo..",
            "..oo..",
            ".o..o.",
            ".o..o.");
        gem = art("o#", new Color[] {c("1a0a0e"), Color.WHITE},
            "..o..",
            ".o#o.",
            "o###o",
            ".o#o.",
            "..o..");
    }

    @Override protected float coverStart() { return COVER_START; }
    @Override protected float coverFull()  { return COVER_FULL; }
    @Override protected Color coverColor() { return Color.BLACK; }

    @Override
    protected void reset() {
        flakes.clear();
        burst.clear();
        flakeDebt = 0f;
        broken = false;
    }

    @Override
    protected void simulate(float delta) {
        float width = worldWidth(), height = worldHeight();
        if (!broken && time >= BREAK) {
            broken = true;
            rumble(0.5f, 8f);
            for (int i = 0; i < 90; i++) {
                Particle piece = particle(sunX(), sunY(), 360f * random.nextFloat(), 200f + 700f * random.nextFloat(),
                    0.8f + 0.8f * random.nextFloat(), 14f + 24f * random.nextFloat());
                piece.drag = 0.15f;
                piece.color.set(random.nextBoolean() ? GOLD : HOT);
                burst.add(piece);
            }
        }
        if (broken) {                                      // la neige dorée
            flakeDebt += MathUtils.lerp(110f, 45f, progress(BREAK, COVER_START)) * delta;
            while (flakeDebt >= 1f) {
                flakeDebt -= 1f;
                Particle flake = particle(width * (random.nextFloat() * 1.2f - 0.1f), height + 20f, 270f,
                    70f + 90f * random.nextFloat(), 99f, 6f + 7f * random.nextFloat());
                flake.spin = random.nextFloat() * MathUtils.PI2;
                flake.color.set(random.nextFloat() < 0.6f ? GOLD : HOT);
                flakes.add(flake);
            }
        }
        for (int i = flakes.size - 1; i >= 0; i--) {
            Particle flake = flakes.get(i);
            flake.age += delta;
            flake.x += (flake.vx + 40f * MathUtils.sin(flake.age * 1.7f + flake.spin)) * delta;
            flake.y += flake.vy * delta;
            if (flake.y < -20f) flakes.removeIndex(i);
        }
        update(burst, delta);
    }

    @Override
    protected void drawScene(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        float dim = progress(DIM_START, BREAK);
        // Le ciel, d'or tant que le soleil brille, puis la nuit.
        gradient(batch, low.set(SKY_LOW).lerp(DAY_LOW, 1f - dim), top.set(SKY_TOP).lerp(DAY_TOP, 1f - dim));
        stars.draw(batch, dim);

        // Le soleil : il pâlit, tremble, puis éclate.
        if (!broken) {
            float shiver = dim * 6f * MathUtils.sin(time * 60f);
            glow(batch, sunX(), sunY(), width * (1.3f - 0.9f * dim), GOLD, 0.7f * (1f - dim * 0.6f));
            glow(batch, sunX(), sunY(), width * (0.5f - 0.3f * dim), HOT, 0.8f);
            float scale = 11f;
            portrait(batch, shard, sunX() + shiver, sunY() - shard.getRegionHeight() * scale / 2f, scale,
                dim * 0.85f, 1f);
        } else {
            float since = time - BREAK;
            glow(batch, sunX(), sunY(), width * 0.8f * (1f + since), HOT, Math.max(0f, 0.8f - since));
        }
        drawParticles(batch, burst);

        // La ville ; ses fenêtres s'allument une à une sous la neige dorée.
        float scale = 4f;
        float cityW = city.getRegionWidth() * scale, cityH = city.getRegionHeight() * scale;
        float lights = 0.45f + 0.55f * progress(BREAK + 0.5f, COVER_START);
        batch.setColor(lights, lights * 0.9f, lights * 0.85f, 1f);
        for (float x = -MARGIN - 60f; x < width + MARGIN; x += cityW) batch.draw(city, x, 0f, cityW, cityH);
        batch.setColor(c("0b0712"));
        batch.draw(pixel, -MARGIN, -MARGIN, width + 2f * MARGIN, MARGIN + 2f);
        glow(batch, width / 2f, 0f, width * 1.2f, GOLD, 0.2f * progress(BREAK, COVER_START));
        drawRooftops(batch, width, height);
        drawShards(batch, width, height);

        additive(batch);
        for (Particle flake : flakes) {
            float twinkle = 0.6f + 0.4f * MathUtils.sin(flake.age * 6f + flake.spin);
            batch.setColor(flake.color.r, flake.color.g, flake.color.b, twinkle);
            batch.draw(pixel, flake.x - flake.size / 2f, flake.y - flake.size / 2f, flake.size, flake.size);
            batch.setColor(flake.color.r, flake.color.g, flake.color.b, 0.25f * twinkle);
            batch.draw(soft, flake.x - flake.size * 2.5f, flake.y - flake.size * 2.5f, flake.size * 5f, flake.size * 5f);
        }
        normal(batch);
        flash(batch, HOT, BREAK, 0.3f, 0.8f);
        fadeFromBlack(batch, 0.5f);
    }

    /** Les toits au premier plan ; les habitants y sortent et tendent les mains vers la neige. */
    private void drawRooftops(Batch batch, float width, float height) {
        float roof = height * 0.1f, u = height / 110f;
        batch.setColor(c("07040c"));
        batch.draw(pixel, -MARGIN, -MARGIN, width + 2f * MARGIN, roof + MARGIN);
        for (int i = 0; i < 7; i++) {                                     // cheminées et lucarnes
            float x = width * (0.05f + i * 0.15f);
            batch.draw(pixel, x, roof, 5f * u, (5f + (i % 3) * 3f) * u);
        }
        for (float[] person : PEOPLE) {
            float out = progress(ROOFS + person[1], ROOFS + person[1] + 0.5f);   // il sort par la lucarne
            if (out <= 0f) continue;
            boolean up = time >= HANDS_UP + person[1] && ((int) ((time - HANDS_UP - person[1]) * 2.5f) % 4 != 3);
            TextureRegion pose = up ? personUp : personDown;
            float scale = 4f * u / 2f, h = pose.getRegionHeight() * scale;
            float x = width * person[0], y = roof - h * (1f - Interpolation.pow2Out.apply(out));
            batch.setColor(Color.WHITE);
            batch.draw(pose, x - pose.getRegionWidth() * scale / 2f, y, pose.getRegionWidth() * scale, h);
            if (up) glow(batch, x, y + h, 10f * scale, GOLD, 0.25f);      // la neige dorée dans leurs mains
        }
    }

    /** Les trois éclats qui ne tombent pas : ils flottent où était le soleil, puis filent vers l'horizon. */
    private void drawShards(Batch batch, float width, float height) {
        if (time < BREAK) return;
        float appear = progress(BREAK, BREAK + 0.4f);
        for (int i = 0; i < SHARDS.length; i++) {
            float angle = time * 70f + i * 120f;
            float hx = sunX() + MathUtils.cosDeg(angle) * 70f, hy = sunY() + MathUtils.sinDeg(angle) * 40f;
            float fly = Interpolation.pow2In.apply(progress(SHARDS_FLY + i * 0.15f, SHARDS_GONE + i * 0.15f));
            if (fly >= 1f) continue;
            float x = MathUtils.lerp(hx, width * (0.92f + 0.03f * i), fly);
            float y = MathUtils.lerp(hy, height * (0.3f + 0.02f * i), fly);
            float scale = MathUtils.lerp(10f, 2f, fly) * appear;
            if (fly > 0f) {                                               // leur traînée
                additive(batch);
                float dir = MathUtils.atan2(y - hy, x - hx) * MathUtils.radiansToDegrees;
                batch.setColor(SHARDS[i].r, SHARDS[i].g, SHARDS[i].b, 0.6f * (1f - fly * 0.5f));
                float length = 260f * (1f - fly * 0.6f);
                batch.draw(trail, x - length, y - 6f, length, 6f, length, 12f, 1f, 1f, dir);
                normal(batch);
            }
            glow(batch, x, y, scale * 16f, SHARDS[i], 0.8f);
            batch.setColor(SHARDS[i]);
            sprite(batch, gem, x, y, scale, 0f);
        }
    }

    private float sunX() { return worldWidth() / 2f; }
    private float sunY() { return worldHeight() * 0.62f; }
}
