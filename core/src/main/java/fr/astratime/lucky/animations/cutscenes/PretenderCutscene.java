package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.tower.Chapter;

/**
 * Avant le Prétendant (chapitre 4) : une ville en flammes, des tables renversées,
 * des joueurs qui se battent pour des éclats dorés. Une ombre traverse la foule.
 * Elle ramasse trois éclats, un, deux, trois, et les pose sur sa couronne : chacun
 * s'allume de sa couleur (Comète, Reine, Éclat). Le Prétendant lève les yeux vers
 * toi. Fondu noir.
 */
public class PretenderCutscene extends Cutscene {

    /** L'ombre traverse la foule (voir le son cutscene/pretender). */
    public static final float SHADOW_START = 1.8f;
    public static final float SHADOW_END   = 3.0f;
    /** Les trois éclats rejoignent l'ombre, un par un. */
    public static final float[] PICK       = {3.3f, 3.75f, 4.2f};
    /** Le Prétendant apparaît, couronné. */
    public static final float REVEAL       = 4.7f;
    /** Il lève les yeux : on s'approche de son visage. */
    public static final float STARE        = 5.4f;
    public static final float COVER_START  = 6.1f;
    public static final float COVER_FULL   = 6.8f;

    /** Les couleurs des trois éclats : la Comète, la Reine, l'Éclat Originel. */
    private static final Color[] SHARDS = {c("ffc93a"), c("e0283a"), c("fff6c8")};
    /** Où brillent les éclats dans la ville (fractions de l'écran). */
    private static final float[][] SHARD_AT = {{0.22f, 0.42f}, {0.78f, 0.47f}, {0.52f, 0.33f}};
    private static final Color FIRE = c("ff7a1a"), GOLD = c("ffc93a");

    private final TextureRegion city, pretender, gem;
    private final Array<Particle> embers = new Array<>(false, 256);
    private final Array<Particle> sparks = new Array<>(false, 128);
    private float emberDebt;
    private int   picked;

    public PretenderCutscene(CutsceneKit kit) {
        super(kit.settings(), kit.shake(), kit.sound("pretender"));
        city = kit.chapterArt(Chapter.MONDE_SANS_MAITRE);
        pretender = kit.portrait(EnemyKind.PRETENDANT);
        gem = art("o#w", new Color[] {c("1a0a0e"), Color.WHITE, c("ffffff")},
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
        embers.clear();
        sparks.clear();
        emberDebt = 0f;
        picked = 0;
    }

    @Override
    protected void simulate(float delta) {
        float width = worldWidth(), height = worldHeight();
        emberDebt += 70f * delta;
        while (emberDebt >= 1f) {                         // braises qui montent des incendies
            emberDebt -= 1f;
            Particle ember = particle(width * random.nextFloat(), height * (0.05f + 0.5f * random.nextFloat()),
                80f + 20f * random.nextFloat(), 60f + 120f * random.nextFloat(), 1.2f + 1.5f * random.nextFloat(),
                5f + 8f * random.nextFloat());
            ember.color.set(random.nextFloat() < 0.7f ? FIRE : GOLD);
            ember.gravity = -20f;
            embers.add(ember);
        }
        while (picked < PICK.length && time >= PICK[picked]) {
            for (int i = 0; i < 18; i++) {
                Particle spark = particle(crownX(picked), crownY(), 360f * random.nextFloat(),
                    150f + 250f * random.nextFloat(), 0.5f, 12f);
                spark.drag = 0.1f;
                spark.color.set(SHARDS[picked]);
                sparks.add(spark);
            }
            picked++;
        }
        update(embers, delta);
        update(sparks, delta);
    }

    @Override
    protected void drawScene(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        // La ville en flammes, qui tremble à la chaleur ; elle s'assombrit au passage de l'ombre.
        float zoom = Math.max(width / city.getRegionWidth(), height / city.getRegionHeight()) * 1.08f;
        float drift = MathUtils.lerp(-20f, 20f, progress(0f, COVER_FULL));
        float dark = 1f - 0.55f * progress(SHADOW_START, SHADOW_END) - 0.2f * progress(REVEAL, STARE);
        float flicker = 0.92f + 0.08f * MathUtils.sin(time * 23f) * MathUtils.sin(time * 7f);
        batch.setColor(dark * flicker, dark * 0.9f * flicker, dark * 0.85f * flicker, 1f);
        float artW = city.getRegionWidth() * zoom, artH = city.getRegionHeight() * zoom;
        batch.draw(city, (width - artW) / 2f + drift, (height - artH) / 2f, artW, artH);
        glow(batch, width / 2f, 0f, width * 1.4f, FIRE, 0.25f * flicker);
        drawParticles(batch, embers);

        // Les éclats dorés que la foule se dispute, puis qui s'envolent vers l'ombre.
        for (int i = 0; i < SHARDS.length; i++) {
            float fly = progress(PICK[i] - 0.35f, PICK[i]);
            if (fly >= 1f) continue;
            float e = Interpolation.pow2In.apply(fly);
            float x = MathUtils.lerp(width * SHARD_AT[i][0], crownX(i), e);
            float y = MathUtils.lerp(height * SHARD_AT[i][1], crownY(), e) + 8f * MathUtils.sin(time * 4f + i);
            drawGem(batch, x, y, SHARDS[i], 7f);
        }

        // L'ombre traverse la foule, de gauche à droite.
        if (time >= SHADOW_START && time < SHADOW_END + 0.2f) {
            float k = Interpolation.sine.apply(progress(SHADOW_START, SHADOW_END));
            float x = MathUtils.lerp(-width * 0.2f, width * 1.2f, k);
            batch.setColor(0f, 0f, 0f, 0.45f);
            batch.draw(soft, x - width * 0.35f, -height * 0.2f, width * 0.7f, height * 1.4f);
            portrait(batch, pretender, x, -40f, 16f, 1f, 1f);
        }

        // Le Prétendant, en ombre, puis éclairé par ses éclats ; on s'approche de son visage.
        if (time >= PICK[0] - 0.4f) {
            float appear = progress(PICK[0] - 0.4f, PICK[0]);
            float light = progress(REVEAL, REVEAL + 0.5f);
            float scale = MathUtils.lerp(10f, 15f, Interpolation.pow2In.apply(progress(STARE, COVER_FULL)));
            float baseY = MathUtils.lerp(height * 0.12f, -height * 0.35f, Interpolation.pow2In.apply(
                progress(STARE, COVER_FULL)));
            glow(batch, width / 2f, baseY + 30f * scale, 30f * scale, GOLD, 0.25f * light);
            portrait(batch, pretender, width / 2f, baseY, scale, 1f - 0.9f * light, appear);
            for (int i = 0; i < picked; i++) {                         // les éclats sur la couronne
                float x = width / 2f + (i - 1) * 7f * scale;
                float y = baseY + 38.5f * scale;
                drawGem(batch, x, y, SHARDS[i], scale * 0.8f);
            }
        }
        drawParticles(batch, sparks);
        // Le regard : deux lueurs dans les yeux.
        if (time >= STARE) {
            float stare = progress(STARE, STARE + 0.3f);
            float scale = MathUtils.lerp(10f, 15f, Interpolation.pow2In.apply(progress(STARE, COVER_FULL)));
            float baseY = MathUtils.lerp(height * 0.12f, -height * 0.35f, Interpolation.pow2In.apply(
                progress(STARE, COVER_FULL)));
            for (int side = -1; side <= 1; side += 2) {
                glow(batch, width / 2f + side * 3f * scale, baseY + 26f * scale, 5f * scale, GOLD, 0.9f * stare);
            }
        }
        fadeFromBlack(batch, 0.7f);
    }

    private void drawGem(Batch batch, float x, float y, Color color, float scale) {
        glow(batch, x, y, scale * 14f, color, 0.7f);
        batch.setColor(color);
        sprite(batch, gem, x, y, scale, 0f);
    }

    /** @return où l'éclat {@code i} rejoint la couronne de l'ombre (avant qu'on s'approche). */
    private float crownX(int i) { return worldWidth() / 2f + (i - 1) * 70f; }
    private float crownY()      { return worldHeight() * 0.12f + 38.5f * 10f; }
}
