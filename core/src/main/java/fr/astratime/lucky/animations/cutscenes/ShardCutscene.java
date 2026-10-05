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
 * Avant l'Éclat Originel (chapitre 3) : le cratère de la comète, vu du ciel. On
 * descend. Au fond, un cœur doré bat. Trois rouleaux géants sortent du sol et se
 * mettent en orbite ; les battements accélèrent, les rouleaux tournent de plus en
 * plus vite, puis tout s'arrête net sur trois 7. Le cœur explose en soleil. Fondu doré.
 */
public class ShardCutscene extends Cutscene {

    /** Premier battement du cœur (voir le son cutscene/shard). */
    public static final float FIRST_BEAT  = 1.0f;
    /** Les rouleaux sortent du sol. */
    public static final float REELS_RISE  = 2.2f;
    /** Les rouleaux sont en orbite et tournent. */
    public static final float REELS_SPIN  = 3.0f;
    /** Tout s'arrête net : trois 7. */
    public static final float STOP        = 5.0f;
    /** Le cœur explose en soleil. */
    public static final float SUN         = 5.45f;
    public static final float COVER_START = 6.05f;
    public static final float COVER_FULL  = 6.8f;

    /** Les battements du cœur : de plus en plus rapprochés, jusqu'à l'arrêt. */
    static final float[] BEATS = {1.0f, 1.28f, 2.0f, 2.26f, 2.85f, 3.07f, 3.55f, 3.73f, 4.1f, 4.25f, 4.5f, 4.62f,
        4.78f, 4.88f};

    private static final Color GOLD = c("ffc93a"), HOT = c("fff6c8"), ORANGE = c("ff8a1e"), DEEP = c("1a0e08");

    private final TextureRegion crater, shard;
    private final TextureRegion[] symbols;
    private final Array<Particle> sparks = new Array<>(false, 256);
    private float orbit, spin, spinSpeed;
    private int   beat;
    private boolean stopped, exploded;

    public ShardCutscene(CutsceneKit kit) {
        super(kit.settings(), kit.shake(), kit.sound("shard"));
        crater = kit.chapterArt(Chapter.DERNIER_TIRAGE);
        shard  = kit.portrait(EnemyKind.ECLAT);
        symbols = new TextureRegion[] {
            load("symbols/3-seven.png"), load("symbols/2-cherry.png"), load("symbols/4-bar.png"),
            load("symbols/6-bell.png"), load("symbols/7-diamond.png"), load("symbols/11-watermelon.png")};
    }

    @Override protected float coverStart() { return COVER_START; }
    @Override protected float coverFull()  { return COVER_FULL; }
    @Override protected Color coverColor() { return GOLD; }

    @Override
    protected void reset() {
        orbit = 0f;
        spin = 0.5f;
        spinSpeed = 0f;
        beat = 0;
        stopped = exploded = false;
        sparks.clear();
    }

    @Override
    protected void simulate(float delta) {
        while (beat < BEATS.length && time >= BEATS[beat]) {
            rumble(0.15f, 2f + beat * 0.6f);
            for (int i = 0; i < 6 + beat; i++) sparks.add(spark(heartX(), heartY(), 300f));
            beat++;
        }
        if (time >= REELS_RISE && !stopped) {
            float k = progress(REELS_SPIN, STOP);
            orbit += MathUtils.lerp(30f, 260f, k * k) * delta;
            spinSpeed = time < REELS_SPIN ? 0f : MathUtils.lerp(4f, 26f, k);
            spin += spinSpeed * delta;
        }
        if (!stopped && time >= STOP) {
            stopped = true;
            spin = MathUtils.ceil(spin);                // pile sur un symbole : le 7 (indice 0)
            spin -= Math.floorMod((int) spin, symbols.length);
            rumble(0.3f, 9f);
        }
        if (!exploded && time >= SUN) {
            exploded = true;
            rumble(0.9f, 18f);
            for (int i = 0; i < 120; i++) sparks.add(spark(heartX(), heartY(), 1100f));
        }
        update(sparks, delta);
    }

    private Particle spark(float x, float y, float speed) {
        Particle spark = particle(x, y, 360f * random.nextFloat(), speed * (0.3f + 0.7f * random.nextFloat()),
            0.6f + 0.6f * random.nextFloat(), 10f + 22f * random.nextFloat());
        spark.drag = 0.2f;
        spark.color.set(random.nextBoolean() ? GOLD : HOT);
        return spark;
    }

    @Override
    protected void drawScene(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        fill(batch, DEEP, 1f);
        // Le cratère vu du ciel : on descend vers son cœur.
        float descent = Interpolation.pow2.apply(progress(0f, REELS_SPIN));
        float zoom = MathUtils.lerp(1f, 1.7f, descent) * Math.max(width / crater.getRegionWidth(),
            height / crater.getRegionHeight()) * 1.05f;
        float artW = crater.getRegionWidth() * zoom, artH = crater.getRegionHeight() * zoom;
        float dim = 1f - 0.45f * progress(REELS_RISE, STOP);
        batch.setColor(dim, dim * 0.95f, dim * 0.9f, 1f);
        batch.draw(crater, heartX() - artW / 2f, heartY() - artH * 0.53f, artW, artH);

        // Le cœur qui bat.
        float pulse = heartPulse();
        float heat = 0.4f + 0.6f * progress(FIRST_BEAT, STOP);
        glow(batch, heartX(), heartY(), (260f + 220f * pulse) * (1f + heat), ORANGE, 0.45f * heat);
        glow(batch, heartX(), heartY(), (140f + 160f * pulse) * (1f + heat * 0.5f), GOLD, 0.8f);
        glow(batch, heartX(), heartY(), 70f + 90f * pulse, HOT, 0.9f);

        drawReels(batch);

        // Le soleil : la lumière grossit et l'Éclat apparaît dedans.
        if (exploded) {
            float since = time - SUN;
            float grow = Interpolation.pow3Out.apply(Math.min(1f, since / 0.6f));
            glow(batch, heartX(), heartY(), width * 1.8f * grow, ORANGE, 0.6f);
            glow(batch, heartX(), heartY(), width * 1.1f * grow, GOLD, 0.8f);
            additive(batch);
            for (int i = 0; i < 16; i++) {                       // rayons
                float angle = i * 22.5f + since * 25f;
                float length = width * 0.9f * grow;
                batch.setColor(1f, 0.9f, 0.5f, 0.35f);
                batch.draw(trail, heartX(), heartY() - 14f, 0f, 14f, length, 28f, 1f, 1f, angle);
            }
            normal(batch);
            float appear = Interpolation.swingOut.apply(Math.min(1f, since / 0.45f));
            float scale = 13f * appear;
            portrait(batch, shard, heartX(), heartY() - shard.getRegionHeight() * scale / 2f, scale, 0f, 1f);
        }
        drawParticles(batch, sparks);
        flash(batch, HOT, SUN, 0.3f, 0.7f);
        flash(batch, Color.WHITE, STOP, 0.12f, 0.3f);
        fadeFromBlack(batch, 0.9f);
    }

    /** Les trois rouleaux géants, en orbite autour du cœur. */
    private void drawReels(Batch batch) {
        if (time < REELS_RISE || exploded) return;
        float rise = Interpolation.swingOut.apply(progress(REELS_RISE, REELS_SPIN));
        float radiusX = worldWidth() * 0.36f * rise, radiusY = worldHeight() * 0.26f * rise;
        float w = 240f * (0.3f + 0.7f * rise), h = 200f * (0.3f + 0.7f * rise);
        for (int i = 0; i < 3; i++) {
            float angle = orbit + 90f + i * 120f;
            float x = heartX() + MathUtils.cosDeg(angle) * radiusX;
            float y = heartY() + MathUtils.sinDeg(angle) * radiusY + 30f;
            float depth = 0.85f + 0.15f * -MathUtils.sinDeg(angle);   // plus gros devant
            float rw = w * depth, rh = h * depth;
            // Cadre doré et vitre blanche.
            glow(batch, x, y, rw * 2.2f, GOLD, 0.35f);
            batch.setColor(c("1a0e08"));
            batch.draw(pixel, x - rw / 2f - 14f, y - rh / 2f - 14f, rw + 28f, rh + 28f);
            batch.setColor(GOLD);
            batch.draw(pixel, x - rw / 2f - 10f, y - rh / 2f - 10f, rw + 20f, rh + 20f);
            batch.setColor(c("f4ecdc"));
            batch.draw(pixel, x - rw / 2f, y - rh / 2f, rw, rh);
            batch.setColor(Color.WHITE);
            reel(batch, symbols, x - rw / 2f, y - rh / 2f, rw, rh, spin + i * 0.37f * (stopped ? 0f : 1f));
            if (spinSpeed > 8f && !stopped) {                   // flou de vitesse
                batch.setColor(1f, 1f, 1f, Math.min(0.6f, (spinSpeed - 8f) / 20f));
                batch.draw(pixel, x - rw / 2f, y - rh / 2f, rw, rh);
            }
        }
    }

    /** @return l'éclat du dernier battement (1 au battement, puis il retombe). */
    private float heartPulse() {
        if (beat == 0) return 0f;
        float since = time - BEATS[beat - 1];
        return Math.max(0f, 1f - since / 0.25f);
    }

    private float heartX() { return worldWidth() / 2f; }
    private float heartY() { return worldHeight() * 0.48f; }
}
