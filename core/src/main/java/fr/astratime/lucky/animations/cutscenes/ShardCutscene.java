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
 * Avant l'Éclat Originel (chapitre 3) : au bord du cratère, les restes de la
 * Comète fument encore, des pièces fondues brillent. Puis le cratère vu du ciel.
 * On descend ; au fond, un cœur doré bat, des veines dorées s'allument dans la
 * roche à chaque battement. Trois rouleaux géants sortent du sol et se mettent en
 * orbite ; les battements accélèrent, les rouleaux tournent de plus en plus vite.
 * Ils ratent deux fois (7-7-cerise, 7-cerise-7), le cœur hésite ; puis tout
 * s'arrête net sur trois 7. Le cœur explose en soleil. Fondu doré.
 */
public class ShardCutscene extends Cutscene {

    /** Au bord du cratère, les restes de la Comète (voir le son cutscene/shard) ; puis la vue du ciel. */
    public static final float RIM_END     = 1.5f;
    /** Premier battement du cœur. */
    public static final float FIRST_BEAT  = 2.5f;
    /** Les rouleaux sortent du sol. */
    public static final float REELS_RISE  = 4.7f;
    /** Les rouleaux sont en orbite et tournent. */
    public static final float REELS_SPIN  = 5.5f;
    /** Deux ratés : ils s'arrêtent sur 7-7-cerise, puis 7-cerise-7, et repartent. */
    public static final float[] MISSES    = {7.5f, 8.25f};
    public static final float MISS_HOLD   = 0.4f;
    /** Tout s'arrête net : trois 7. */
    public static final float STOP        = 9.0f;
    /** Le cœur explose en soleil. */
    public static final float SUN         = 9.45f;
    public static final float COVER_START = 10.05f;
    public static final float COVER_FULL  = 10.8f;

    /** Les battements du cœur : de plus en plus rapprochés, sauf quand il hésite (les ratés), jusqu'à l'arrêt. */
    static final float[] BEATS = {2.5f, 2.78f, 3.5f, 3.76f, 4.5f, 4.76f, 5.35f, 5.57f, 6.05f, 6.23f, 6.6f, 6.75f,
        7.0f, 7.12f, 7.28f, 7.38f, 8.0f, 8.12f, 8.72f, 8.8f, 8.88f};
    /** Ce que montrent les trois rouleaux à chaque raté (0 : le 7, 1 : la cerise). */
    private static final int[][] MISS_SHOWN = {{0, 0, 1}, {0, 1, 0}};

    private static final Color GOLD = c("ffc93a"), HOT = c("fff6c8"), ORANGE = c("ff8a1e"), DEEP = c("1a0e08");

    private final TextureRegion crater, shard, coin;
    private final TextureRegion[] symbols;
    private final Array<Particle> sparks = new Array<>(false, 256);
    private final Array<Particle> smoke  = new Array<>(false, 128);
    private float orbit, spin, spinSpeed;
    private int   beat;
    private boolean stopped, exploded;
    private int   missed;
    private float smokeDebt;

    public ShardCutscene(CutsceneKit kit) {
        super(kit.settings(), kit.shake(), kit.sound("shard"));
        crater = kit.chapterArt(Chapter.DERNIER_TIRAGE);
        shard  = kit.portrait(EnemyKind.ECLAT);
        symbols = new TextureRegion[] {
            load("symbols/3-seven.png"), load("symbols/2-cherry.png"), load("symbols/4-bar.png"),
            load("symbols/6-bell.png"), load("symbols/7-diamond.png"), load("symbols/11-watermelon.png")};
        coin = load("hud/coin.png");
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
        missed = 0;
        smokeDebt = 0f;
        sparks.clear();
        smoke.clear();
    }

    @Override
    protected void simulate(float delta) {
        while (beat < BEATS.length && time >= BEATS[beat]) {
            rumble(0.15f, 2f + beat * 0.6f);
            for (int i = 0; i < 6 + beat; i++) sparks.add(spark(heartX(), heartY(), 300f));
            beat++;
        }
        if (time < RIM_END) {                                // la fumée des restes de la Comète
            smokeDebt += 22f * delta;
            while (smokeDebt >= 1f) {
                smokeDebt -= 1f;
                Particle puff = particle(worldWidth() * (0.25f + 0.5f * random.nextFloat()), worldHeight() * 0.32f,
                    80f + 20f * random.nextFloat(), 60f + 80f * random.nextFloat(), 1.4f + random.nextFloat(),
                    120f + 140f * random.nextFloat());
                puff.color.set(0.4f, 0.37f, 0.38f, 0.8f);
                puff.drag = 0.7f;
                smoke.add(puff);
            }
        }
        while (missed < MISSES.length && time >= MISSES[missed]) {   // un raté : clac, clac... non
            missed++;
            rumble(0.2f, 6f);
        }
        if (time >= REELS_RISE && !stopped) {
            float k = progress(REELS_SPIN, STOP);
            boolean holding = holding();
            orbit += MathUtils.lerp(30f, 260f, k * k) * delta * (holding ? 0.25f : 1f);
            spinSpeed = time < REELS_SPIN || holding ? 0f : MathUtils.lerp(4f, 26f, k);
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
        update(smoke, delta);
    }

    /** @return {@code true} pendant un raté : les rouleaux sont arrêtés sur la mauvaise combinaison. */
    private boolean holding() {
        for (float miss : MISSES) if (time >= miss && time < miss + MISS_HOLD) return true;
        return false;
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
        if (time < RIM_END) {
            drawRim(batch);
            fadeFromBlack(batch, 0.6f);
            fadeThroughBlack(batch, RIM_END, 0.25f);
            return;
        }
        float width = worldWidth(), height = worldHeight();
        fill(batch, DEEP, 1f);
        // Le cratère vu du ciel : on descend vers son cœur.
        float descent = Interpolation.pow2.apply(progress(RIM_END, REELS_SPIN));
        float zoom = MathUtils.lerp(1f, 1.7f, descent) * Math.max(width / crater.getRegionWidth(),
            height / crater.getRegionHeight()) * 1.05f;
        float artW = crater.getRegionWidth() * zoom, artH = crater.getRegionHeight() * zoom;
        float dim = 1f - 0.45f * progress(REELS_RISE, STOP);
        batch.setColor(dim, dim * 0.95f, dim * 0.9f, 1f);
        batch.draw(crater, heartX() - artW / 2f, heartY() - artH * 0.53f, artW, artH);

        // Le cœur qui bat ; des veines dorées s'allument dans la roche à chaque battement.
        float pulse = heartPulse();
        float heat = (0.4f + 0.6f * progress(FIRST_BEAT, STOP)) * (holding() ? 0.7f : 1f);
        drawVeins(batch, zoom, pulse);
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
        fadeThroughBlack(batch, RIM_END, 0.25f);
    }

    /**
     * Au bord du cratère : un gros morceau de la Comète, noirci, fendu de lave,
     * qui fume encore ; autour, des pièces fondues qui brillent.
     */
    private void drawRim(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, c("3a1a10"), c("0a0612"));
        float zoom = Math.max(width / crater.getRegionWidth(), height / crater.getRegionHeight()) * 2.4f;
        batch.setColor(0.6f, 0.45f, 0.38f, 1f);                        // le cratère, loin derrière
        batch.draw(crater, (width - crater.getRegionWidth() * zoom) / 2f, -crater.getRegionHeight() * zoom * 0.62f,
            crater.getRegionWidth() * zoom, crater.getRegionHeight() * zoom);
        float ground = height * 0.3f, slide = progress(0f, RIM_END) * 40f;
        batch.setColor(c("1a100c"));
        batch.draw(pixel, -MARGIN, -MARGIN, width + 2f * MARGIN, ground + MARGIN);
        // Le rocher de la Comète, en blocs, avec ses fissures de lave qui respirent.
        float rx = width * 0.5f - slide, ry = ground - 40f, u = height / 32f;
        glow(batch, rx, ry + 6f * u, 40f * u, ORANGE, 0.35f);
        int[][] blocks = {{-9, 0, 18, 7}, {-7, 7, 14, 5}, {-5, 12, 9, 3}, {-12, 0, 4, 4}, {9, 0, 4, 3}};
        for (int[] b : blocks) {
            batch.setColor(c("0e0a0c"));
            batch.draw(pixel, rx + (b[0] - 0.5f) * u, ry + (b[1] - 0.5f) * u, (b[2] + 1f) * u, (b[3] + 1f) * u);
            batch.setColor(c("3a2c30"));
            batch.draw(pixel, rx + b[0] * u, ry + b[1] * u, b[2] * u, b[3] * u);
            batch.setColor(c("4e3c40"));
            batch.draw(pixel, rx + b[0] * u, ry + (b[1] + b[3] - 1f) * u, b[2] * u, 0.6f * u);
        }
        float lava = 0.6f + 0.4f * MathUtils.sin(time * 5f);
        int[][] cracks = {{-6, 3, 7, 0}, {-1, 3, 0, 9}, {2, 8, 5, 0}, {-4, 9, 4, 0}, {4, 1, 0, 5}};   // x, y, largeur, hauteur
        for (int[] k : cracks) {
            float w = k[2] > 0 ? k[2] * u : 0.6f * u, h = k[3] > 0 ? k[3] * u : 0.6f * u;
            batch.setColor(1f, 0.5f + 0.3f * lava, 0.1f, 1f);
            batch.draw(pixel, rx + k[0] * u, ry + k[1] * u, w, h);
            glow(batch, rx + k[0] * u + w / 2f, ry + k[1] * u + h / 2f, Math.max(w, h) * 2.5f, ORANGE, 0.5f * lava);
        }
        // Les pièces fondues, en flaques dorées qui luisent.
        for (int i = 0; i < 7; i++) {
            float px = width * (0.12f + i * 0.13f) - slide * 1.4f, py = ground * (0.25f + 0.3f * ((i * 37) % 3) / 2f);
            float glowing = 0.6f + 0.4f * MathUtils.sin(time * 3f + i * 1.3f);
            glow(batch, px, py, 160f, GOLD, 0.35f * glowing);
            batch.setColor(c("e8a020"));
            batch.draw(pixel, px - 50f, py - 8f, 100f, 16f);
            batch.setColor(c("ffe58a"));
            batch.draw(pixel, px - 30f, py + 2f, 50f, 5f);
            if (i % 2 == 0) {                                               // une pièce encore entière, à moitié fondue
                batch.setColor(1f, 0.9f, 0.7f, 1f);
                batch.draw(coin, px - 22f, py - 2f, 44f, 30f);
            }
        }
        for (Particle puff : smoke) {                                      // la fumée qui monte
            float life = puff.life();
            batch.setColor(puff.color.r, puff.color.g, puff.color.b, puff.color.a * Math.min(1f, life * 4f) * (1f - life));
            float size = puff.size * (0.6f + life);
            batch.draw(soft, puff.x - size / 2f, puff.y - size / 2f, size, size);
        }
    }

    /** Des veines dorées, du cœur vers les bords du cratère : elles s'allument à chaque battement. */
    private void drawVeins(Batch batch, float zoom, float pulse) {
        if (time < FIRST_BEAT) return;
        float lit = progress(FIRST_BEAT, FIRST_BEAT + 1.5f) * (0.25f + 0.75f * pulse);
        if (lit <= 0f) return;
        additive(batch);
        for (int i = 0; i < 14; i++) {
            float angle = i * 360f / 14f + ((i * 53) % 17), length = (90f + ((i * 41) % 60)) * zoom * 0.25f;
            float x = heartX(), y = heartY(), a = angle;
            for (int seg = 0; seg < 4; seg++) {                     // une veine en zigzag, qui s'affine
                float l = length / 4f, thick = (4f - seg) * 2.5f;
                batch.setColor(1f, 0.78f, 0.3f, 0.8f * lit * (1f - seg * 0.2f));
                batch.draw(pixel, x, y - thick / 2f, 0f, thick / 2f, l, thick, 1f, 1f, a);
                x += MathUtils.cosDeg(a) * l;
                y += MathUtils.sinDeg(a) * l;
                a += ((i + seg) % 2 == 0 ? 18f : -18f);
            }
        }
        normal(batch);
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
            float shown = spin + i * 0.37f * (stopped ? 0f : 1f);
            if (!stopped && holding()) shown = MISS_SHOWN[missed - 1][i];   // un raté : pile sur la mauvaise combinaison
            reel(batch, symbols, x - rw / 2f, y - rh / 2f, rw, rh, shown);
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
