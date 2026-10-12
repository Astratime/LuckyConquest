package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.enemy.EnemyKind;

/**
 * La Comète Dorée passe sous la moitié de ses PV, au début de son tour : dans
 * la nuit, blessée, elle s'éteint presque ; sa lueur bat de plus en plus vite,
 * puis elle s'embrase d'un coup. Une onde de choc dorée part d'elle, des
 * flammes montent tout autour et une pluie de météores dorés tombe du ciel.
 * Fondu orange, puis la suite de son tour (et sa nouvelle musique).
 */
public class CometPhaseCutscene extends Cutscene {

    /** Ses battements de lueur, de plus en plus rapprochés (voir le son cutscene/comet_phase_2). */
    static final float[] BEATS = {0.6f, 1.2f, 1.7f, 2.1f, 2.4f};
    /** Elle s'embrase. */
    public static final float IGNITE      = 2.7f;
    /** Plus de nouveaux météores. */
    public static final float METEORS_END = 4.6f;
    public static final float COVER_START = 4.6f;
    public static final float COVER_FULL  = 5.3f;

    private static final Color SKY_TOP = c("05071a"), SKY_LOW = c("2b1a4a"), GROUND = c("0e0a12");
    private static final Color GOLD = c("ffc93a"), HOT = c("fff6c8"), ORANGE = c("ff7a1a"), RED = c("c92a12");

    private final TextureRegion comet;
    private final Color         sky   = new Color();
    private final Starfield     stars = new Starfield(140, 0.35f);
    private final Array<Particle> flames  = new Array<>(false, 256);
    private final Array<Particle> sparks  = new Array<>(false, 256);
    private final Array<Particle> meteors = new Array<>(false, 64);
    private int     beat;
    private boolean ignited;
    private float   flameDebt, meteorDebt;

    public CometPhaseCutscene(CutsceneKit kit) {
        super(kit.settings(), kit.shake(), kit.sound("comet_phase_2"));
        comet = kit.portrait(EnemyKind.COMETE);
    }

    @Override protected float coverStart() { return COVER_START; }
    @Override protected float coverFull()  { return COVER_FULL; }
    @Override protected Color coverColor() { return ORANGE; }

    @Override
    protected void reset() {
        beat = 0;
        ignited = false;
        flameDebt = meteorDebt = 0f;
        flames.clear();
        sparks.clear();
        meteors.clear();
    }

    @Override
    protected void simulate(float delta) {
        while (beat < BEATS.length && time >= BEATS[beat]) {           // sa lueur bat
            rumble(0.12f, 2f + beat * 1.5f);
            for (int i = 0; i < 4 + 3 * beat; i++) sparks.add(spark(cometX(), cometY(), 220f + 60f * beat));
            beat++;
        }
        if (!ignited && time >= IGNITE) {                              // elle s'embrase
            ignited = true;
            rumble(0.8f, 16f);
            for (int i = 0; i < 140; i++) sparks.add(spark(cometX(), cometY(), 1200f));
        }
        if (ignited && time < COVER_FULL) {
            flameDebt += 70f * delta;                                   // des flammes montent autour d'elle
            while (flameDebt >= 1f) {
                flameDebt -= 1f;
                float x = cometX() + (random.nextFloat() - 0.5f) * cometWidth() * 1.3f;
                Particle flame = particle(x, cometBottom() + cometHeight() * 0.2f * random.nextFloat(),
                    80f + 20f * random.nextFloat(), 160f + 260f * random.nextFloat(), 0.6f + 0.5f * random.nextFloat(),
                    50f + 70f * random.nextFloat());
                flame.color.set(random.nextFloat() < 0.4f ? GOLD : random.nextFloat() < 0.6f ? ORANGE : RED);
                flame.drag = 0.6f;
                flames.add(flame);
            }
        }
        if (ignited && time < METEORS_END) {
            meteorDebt += 9f * delta;                                   // la pluie de météores dorés
            while (meteorDebt >= 1f) {
                meteorDebt -= 1f;
                Particle meteor = particle(worldWidth() * (0.1f + 1.1f * random.nextFloat()), worldHeight() + 60f,
                    230f + 15f * random.nextFloat(), 900f + 500f * random.nextFloat(), 1.4f, 12f + 10f * random.nextFloat());
                meteor.color.set(random.nextBoolean() ? GOLD : HOT);
                meteors.add(meteor);
            }
        }
        update(flames, delta);
        update(sparks, delta);
        update(meteors, delta);
    }

    private Particle spark(float x, float y, float speed) {
        Particle spark = particle(x, y, 360f * random.nextFloat(), speed * (0.3f + 0.7f * random.nextFloat()),
            0.5f + 0.6f * random.nextFloat(), 10f + 20f * random.nextFloat());
        spark.drag = 0.2f;
        spark.color.set(random.nextBoolean() ? GOLD : HOT);
        return spark;
    }

    @Override
    protected void drawScene(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        float fire = Interpolation.pow2Out.apply(progress(IGNITE, IGNITE + 0.6f));
        gradient(batch, sky.set(SKY_LOW).lerp(RED, 0.35f * fire), SKY_TOP);  // le ciel rougit
        stars.draw(batch, 1f - 0.6f * fire);
        drawMeteors(batch);
        batch.setColor(GROUND);                                         // le sol du cratère
        batch.draw(pixel, -MARGIN, -MARGIN, width + 2f * MARGIN, height * 0.18f + MARGIN);

        // Sa lueur : faible et qui bat, puis un brasier.
        float pulse = pulse();
        float x = cometX(), y = cometY();
        glow(batch, x, y, cometWidth() * (1.4f + 0.6f * pulse + 2.2f * fire), ORANGE, 0.25f + 0.25f * pulse + 0.4f * fire);
        glow(batch, x, y, cometWidth() * (0.9f + 0.4f * pulse + 1.2f * fire), GOLD, 0.2f + 0.3f * pulse + 0.5f * fire);
        drawParticles(batch, flames);

        // Elle-même : presque éteinte et vacillante, puis pleine lumière, un peu plus grande.
        float flicker = ignited ? 0f : 0.55f - 0.35f * pulse + (random.nextFloat() < 0.1f ? 0.15f : 0f);
        float swell = 1f + 0.12f * Interpolation.swingOut.apply(progress(IGNITE, IGNITE + 0.5f));
        portrait(batch, comet, x, cometBottom(), cometScale() * swell, MathUtils.clamp(flicker, 0f, 1f), 1f);

        drawWave(batch, x, y);
        drawParticles(batch, sparks);
        flash(batch, HOT, IGNITE, 0.35f, 0.8f);
        fadeFromBlack(batch, 0.4f);
    }

    /** L'onde de choc dorée de son embrasement. */
    private void drawWave(Batch batch, float x, float y) {
        if (!ignited) return;
        float k = progress(IGNITE, IGNITE + 0.9f);
        if (k >= 1f) return;
        float size = worldWidth() * 2.2f * Interpolation.pow2Out.apply(k);
        additive(batch);
        batch.setColor(GOLD.r, GOLD.g, GOLD.b, 0.8f * (1f - k));
        batch.draw(ring, x - size / 2f, y - size / 2f, size, size);
        normal(batch);
    }

    /** Les météores dorés : une traînée derrière chacun. */
    private void drawMeteors(Batch batch) {
        additive(batch);
        for (Particle meteor : meteors) {
            float angle = MathUtils.atan2(meteor.vy, meteor.vx) * MathUtils.radiansToDegrees + 180f;
            float length = meteor.size * 14f;
            batch.setColor(meteor.color.r, meteor.color.g, meteor.color.b, 0.7f);
            batch.draw(trail, meteor.x, meteor.y - meteor.size / 2f, 0f, meteor.size / 2f, length, meteor.size, 1f, 1f, angle);
        }
        normal(batch);
        for (Particle meteor : meteors) glow(batch, meteor.x, meteor.y, meteor.size * 4f, HOT, 0.9f);
    }

    /** @return l'éclat du dernier battement (1 au battement, puis il retombe). */
    private float pulse() {
        if (beat == 0 || ignited) return 0f;
        return Math.max(0f, 1f - (time - BEATS[beat - 1]) / 0.3f);
    }

    private float cometScale()  { return worldHeight() * 0.42f / comet.getRegionHeight(); }
    private float cometWidth()  { return comet.getRegionWidth() * cometScale(); }
    private float cometHeight() { return comet.getRegionHeight() * cometScale(); }
    private float cometBottom() { return worldHeight() * 0.2f; }
    private float cometX()      { return worldWidth() / 2f; }
    private float cometY()      { return cometBottom() + cometHeight() / 2f; }
}
