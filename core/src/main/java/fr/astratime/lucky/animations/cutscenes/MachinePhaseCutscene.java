package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.Symbol;

/**
 * Changement de phase de la Machine Originelle, au début de son tour.
 *
 * Phase 2 : au sommet de la Tour, l'alarme sonne, ses cinq yeux virent au
 * rouge. Une pince descend de son flanc gauche au bout d'une chaîne, attrape un de
 * tes rouleaux (cerise, sept, cloche) et le remonte. Un sixième compartiment
 * s'ouvre sur ce flanc, ton rouleau y entre et tourne pour elle. Fondu rouge.
 *
 * Phase 3 : le ciel vire au rouge, son corps se fend, des pièces et des
 * étincelles fuient par les fissures. La pince redescend et prend un autre
 * rouleau ; cette fois, des chaînes rouges le verrouillent dans la machine.
 * Fondu rouge sombre.
 */
public class MachinePhaseCutscene extends MachineCutscene {

    /** L'alarme : ses yeux rougissent (voir le son cutscene/machine_phase_2 et _3). */
    public static final float ALARM       = 0.6f;
    /** La pince descend, attrape ton rouleau, puis remonte. */
    public static final float CLAW_DOWN   = 1.5f;
    public static final float GRAB        = 2.4f;
    public static final float CLAW_UP     = 2.7f;
    /** Ton rouleau entre dans son flanc. */
    public static final float INSERTED    = 3.9f;
    /** Phase 3 : les chaînes le verrouillent. */
    public static final float CHAINS      = 4.3f;
    public static final float COVER_START = 5.3f;
    public static final float COVER_FULL  = 6.0f;

    private static final float U = 14f;              // le même « pixel » que la machine de MachineCutscene
    private static final Color OUT = c("140a0a"), GOLD = c("ffc93a"), GOLD_DARK = c("b8801f"), RED = c("e0283a");
    private static final Color STEEL = c("8a8a9a"), STEEL_DARK = c("4a4a58"), GLASS = c("f4ecdc"), SKY_RED = c("5a0a12");
    private static final Color CRACK = c("2a0a06"), HOT = c("fff2b0");

    private final int phase;
    /** Les symboles de ton rouleau : cerise, sept, cloche, bar. */
    private final TextureRegion[] yours;
    private final Array<Particle> sparks = new Array<>(false, 256);
    private boolean grabbed, inserted, chained;
    private float   sparkDebt, yourSpin;

    /** @param phase la phase dans laquelle la Machine entre (2 ou 3) */
    public MachinePhaseCutscene(CutsceneKit kit, int phase) {
        super(kit, phase >= 3 ? "machine_phase_3" : "machine_phase_2");
        this.phase = phase;
        settled = true;
        yours = new TextureRegion[] {load(Symbol.CHERRY.getAssetPath()), load(Symbol.SEVEN.getAssetPath()),
            load(Symbol.BELL.getAssetPath()), load(Symbol.BAR.getAssetPath())};
    }

    @Override protected float coverStart() { return COVER_START; }
    @Override protected float coverFull()  { return COVER_FULL; }
    @Override protected Color coverColor() { return phase >= 3 ? c("1a0204") : c("3a060c"); }

    @Override
    protected void reset() {
        super.reset();
        sparks.clear();
        grabbed = inserted = chained = false;
        sparkDebt = yourSpin = 0f;
        bright = 1f;
    }

    @Override
    protected void simulate(float delta) {
        if (time >= ALARM && time < COVER_START && ((int) ((time - ALARM) / 0.5f)) != ((int) ((time - ALARM - delta) / 0.5f))) {
            rumble(0.15f, phase >= 3 ? 7f : 4f);                       // l'alarme cogne à chaque battement
        }
        if (!grabbed && time >= GRAB) {
            grabbed = true;
            rumble(0.2f, 8f);
        }
        if (!inserted && time >= INSERTED) {
            inserted = true;
            rumble(0.4f, 12f);
        }
        if (phase >= 3 && !chained && time >= CHAINS) {
            chained = true;
            rumble(0.3f, 10f);
        }
        if (inserted) yourSpin += (time < INSERTED + 0.9f ? 18f : 0f) * delta;
        if (phase >= 3) {
            // Son corps se fend : la lumière grésille, pièces et étincelles fuient par les fissures.
            bright = time < ALARM ? 1f : 0.8f + (random.nextFloat() < 0.18f ? -0.3f : 0.1f);
            if (time >= ALARM && time < COVER_START) {
                sparkDebt += 45f * delta;
                float width = worldWidth(), baseY = worldHeight() * 0.04f, bottom = baseY + 3f * U;
                while (sparkDebt >= 1f) {
                    sparkDebt -= 1f;
                    float[] crack = CRACKS[random.nextInt(CRACKS.length)];
                    float x = width / 2f + crack[2] * U, y = bottom + crack[3] * U;
                    boolean piece = random.nextFloat() < 0.15f;
                    Particle spark = particle(x, y, 40f + 100f * random.nextFloat(), piece ? 250f + 250f * random.nextFloat()
                        : 300f + 400f * random.nextFloat(), piece ? 1.2f : 0.4f + 0.4f * random.nextFloat(),
                        piece ? 1.1f : 10f + 12f * random.nextFloat());
                    spark.gravity = piece ? 900f : 500f;
                    if (piece) {
                        spark.region = coin;
                        spark.spin = (random.nextFloat() - 0.5f) * 600f;
                    } else {
                        spark.color.set(random.nextFloat() < 0.5f ? GOLD : HOT);
                    }
                    sparks.add(spark);
                }
            }
        }
        update(sparks, delta);
    }

    /** Les fissures de son corps, en « pixels » de la machine depuis son centre bas : de (x0, y0) à (x1, y1). */
    private static final float[][] CRACKS = {
        {-30f, 40f, -22f, 30f}, {-22f, 30f, -26f, 20f}, {-26f, 20f, -18f, 12f},
        {24f, 42f, 30f, 33f}, {30f, 33f, 22f, 24f}, {22f, 24f, 28f, 15f},
        {-6f, 4f, 2f, 10f}, {2f, 10f, -3f, 16f}};

    @Override
    protected void drawScene(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        float red = phase >= 3 ? Interpolation.pow2Out.apply(progress(ALARM, ALARM + 1.2f)) : 0f;
        drawSky(batch, 0f);
        if (red > 0f) fill(batch, SKY_RED, 0.55f * red);                // le ciel vire au rouge
        float cx = width / 2f, baseY = height * 0.04f;
        float bottom = baseY + 3f * U, w = 72f * U, h = 44f * U;
        float alarm = time < ALARM ? 0f : 0.5f + 0.5f * MathUtils.sin((time - ALARM) * MathUtils.PI * 4f);
        glow(batch, cx, bottom + h / 2f, 150f * U, RED, (phase >= 3 ? 0.35f : 0.22f) * alarm);
        drawSide(batch, cx - w / 2f, bottom, h);                       // le compartiment du flanc, derrière la chaîne
        drawMachine(batch, cx, baseY);
        drawTower(batch, cx, baseY);
        drawRedEyes(batch, cx, bottom, h, alarm);
        if (phase >= 3) drawCracks(batch, cx, bottom, Interpolation.pow2Out.apply(progress(ALARM, ALARM + 1f)));
        drawClaw(batch, cx - w / 2f, bottom, h, height);
        drawParticles(batch, sparks);
        flash(batch, RED, ALARM, 0.25f, 0.35f);
        flash(batch, Color.WHITE, INSERTED, 0.2f, 0.4f);
        if (phase >= 3) flash(batch, RED, CHAINS, 0.25f, 0.4f);
        fadeFromBlack(batch, 0.5f);
    }

    /** Ses cinq yeux rougissent avec l'alarme. */
    private void drawRedEyes(Batch batch, float cx, float bottom, float h, float alarm) {
        if (time < ALARM) return;
        float reelW = 11f * U, reelH = 14f * U, gap = 2f * U, panelW = 5f * reelW + 6f * gap, reelY = bottom + h * 0.45f;
        for (int i = 0; i < 5; i++) {
            float x = cx - panelW / 2f + gap + i * (reelW + gap) + reelW / 2f;
            glow(batch, x, reelY + reelH / 2f, reelW * 1.8f, RED, 0.25f + 0.45f * alarm);
        }
    }

    /** Les fissures dorées de son corps (phase 3), qui s'ouvrent de 0 à 1. */
    private void drawCracks(Batch batch, float cx, float bottom, float open) {
        if (open <= 0f) return;
        for (int i = 0; i < CRACKS.length; i++) {
            float k = MathUtils.clamp(open * CRACKS.length - i * 0.6f, 0f, 1f);
            if (k <= 0f) continue;
            float[] crack = CRACKS[i];
            float x0 = cx + crack[0] * U, y0 = bottom + crack[1] * U;
            float x1 = MathUtils.lerp(x0, cx + crack[2] * U, k), y1 = MathUtils.lerp(y0, bottom + crack[3] * U, k);
            float length = (float) Math.hypot(x1 - x0, y1 - y0);
            float angle = MathUtils.atan2(y1 - y0, x1 - x0) * MathUtils.radiansToDegrees;
            glow(batch, x1, y1, 10f * U, GOLD, 0.35f * bright);
            batch.setColor(CRACK);
            batch.draw(pixel, x0, y0 - 1.2f * U, 0f, 1.2f * U, length, 2.4f * U, 1f, 1f, angle);
            batch.setColor(GOLD.r, GOLD.g, GOLD.b, 0.9f);
            batch.draw(pixel, x0, y0 - 0.4f * U, 0f, 0.4f * U, length, 0.8f * U, 1f, 1f, angle);
        }
    }

    /** @return où est le rouleau volé (centre), selon l'instant : en bas de l'écran, puis remonté dans son flanc gauche. */
    private float[] yourReel(float side, float bottom, float h, float height) {
        float restX = worldWidth() * 0.07f, restY = height * 0.2f;
        float slotX = side - 7f * U, slotY = bottom + h * 0.45f + 7f * U;
        float k = Interpolation.pow2InInverse.apply(progress(CLAW_UP, INSERTED));
        return new float[] {MathUtils.lerp(restX, slotX, k), MathUtils.lerp(restY, slotY, k)};
    }

    /** Le compartiment qui s'ouvre sur son flanc gauche ({@code side}) : ton rouleau y tourne pour elle. */
    private void drawSide(Batch batch, float side, float bottom, float h) {
        float open = Interpolation.pow2Out.apply(progress(CLAW_DOWN, CLAW_DOWN + 0.6f));
        if (open <= 0f) return;
        float bw = 14f * U * open, by = bottom + h * 0.45f - 2f * U, bh = 18f * U;
        batch.setColor(OUT.r * bright, OUT.g * bright, OUT.b * bright, 1f);
        batch.draw(pixel, side - bw - U, by - U, bw + 2f * U, bh + 2f * U);
        batch.setColor(GOLD_DARK.r * bright, GOLD_DARK.g * bright, GOLD_DARK.b * bright, 1f);
        batch.draw(pixel, side - bw, by, bw, bh);
        batch.setColor(OUT);
        batch.draw(pixel, side - bw + 1.5f * U * open, by + 2f * U, 11f * U * open, 14f * U);
    }

    /**
     * La pince : elle sort du compartiment au bout d'une chaîne, descend vers ton
     * rouleau, le serre et le remonte dedans. En phase 3, des chaînes rouges l'y verrouillent.
     */
    private void drawClaw(Batch batch, float side, float bottom, float h, float height) {
        if (time < CLAW_DOWN) {
            drawYourReel(batch, yourReel(side, bottom, h, height), 1f);
            return;
        }
        float[] reel = yourReel(side, bottom, h, height);
        float anchorX = side - 7f * U, anchorY = bottom + h * 0.45f + 16f * U;
        float down = Interpolation.pow2Out.apply(progress(CLAW_DOWN, GRAB));
        float clawX = time < CLAW_UP ? MathUtils.lerp(anchorX, reel[0], down) : reel[0];
        float clawY = time < CLAW_UP ? MathUtils.lerp(anchorY, reel[1] + 9f * U, down) : reel[1] + 9f * U;
        if (time < INSERTED + 0.2f) {
            // La chaîne : des maillons de l'ancrage à la pince.
            float dx = clawX - anchorX, dy = clawY - anchorY, length = (float) Math.hypot(dx, dy);
            int links = Math.max(1, (int) (length / (2.4f * U)));
            for (int i = 0; i <= links; i++) {
                float x = anchorX + dx * i / links, y = anchorY + dy * i / links;
                batch.setColor(OUT);
                batch.draw(pixel, x - 1.1f * U, y - 1.1f * U, 2.2f * U, 2.2f * U);
                batch.setColor(i % 2 == 0 ? STEEL : STEEL_DARK);
                batch.draw(pixel, x - 0.6f * U, y - 0.6f * U, 1.2f * U, 1.2f * U);
            }
        }
        drawYourReel(batch, reel, time < INSERTED ? 1f : 0f);
        if (time >= INSERTED + 0.2f) {
            if (phase >= 3) drawChains(batch, reel);
            return;
        }
        // La pince : trois griffes qui se referment sur le rouleau.
        float shut = progress(GRAB - 0.15f, GRAB);
        batch.setColor(OUT);
        batch.draw(pixel, clawX - 3f * U, clawY - U, 6f * U, 3f * U);
        batch.setColor(STEEL);
        batch.draw(pixel, clawX - 2.3f * U, clawY - 0.4f * U, 4.6f * U, 1.8f * U);
        for (int side2 = -1; side2 <= 1; side2 += 2) {
            float angle = side2 < 0 ? MathUtils.lerp(-120f, -80f, shut) : MathUtils.lerp(-60f, -100f, shut);
            float x = clawX + side2 * 2.5f * U;
            batch.setColor(OUT);
            batch.draw(pixel, x, clawY - 0.9f * U, 0f, 0.9f * U, 7f * U, 1.8f * U, 1f, 1f, angle);
            batch.setColor(STEEL_DARK);
            batch.draw(pixel, x, clawY - 0.4f * U, 0f, 0.4f * U, 6.5f * U, 0.8f * U, 1f, 1f, angle);
        }
    }

    /** Ton rouleau : une vitre dorée où défilent tes symboles ; {@code glowing} = 1 tant qu'il est encore à toi. */
    private void drawYourReel(Batch batch, float[] at, float glowing) {
        float rw = 11f * U, rh = 14f * U, x = at[0] - rw / 2f, y = at[1] - rh / 2f;
        if (glowing > 0f) glow(batch, at[0], at[1], rw * 3f, GOLD, 0.35f * glowing);
        batch.setColor(OUT);
        batch.draw(pixel, x - U, y - U, rw + 2f * U, rh + 2f * U);
        batch.setColor(GOLD);
        batch.draw(pixel, x - 0.5f * U, y - 0.5f * U, rw + U, rh + U);
        batch.setColor(GLASS.r * bright, GLASS.g * bright, GLASS.b * bright, 1f);
        batch.draw(pixel, x + 0.5f * U, y + 0.5f * U, rw - U, rh - U);
        float pad = 1.5f * U;
        batch.setColor(bright, bright, bright, 1f);
        reel(batch, yours, x + pad, y + pad + U, rw - 2f * pad, rw - 2f * pad, yourSpin % yours.length);
        if (inserted) glow(batch, at[0], at[1], rw * 1.6f, RED, 0.3f + 0.2f * MathUtils.sin(time * 9f));
    }

    /** Phase 3 : deux chaînes rouges en croix verrouillent le rouleau volé. */
    private void drawChains(Batch batch, float[] at) {
        float k = Interpolation.pow2Out.apply(progress(CHAINS, CHAINS + 0.35f));
        if (k <= 0f) return;
        float size = 22f * U * k;
        for (int cross = -1; cross <= 1; cross += 2) {
            float angle = cross * 52f;
            int links = 9;
            for (int i = 0; i < links; i++) {
                float t = (i + 0.5f) / links - 0.5f;
                float x = at[0] + MathUtils.cosDeg(angle) * size * t, y = at[1] + MathUtils.sinDeg(angle) * size * t;
                batch.setColor(OUT);
                batch.draw(pixel, x - 1.2f * U, y - 1.2f * U, 2.4f * U, 2.4f * U);
                batch.setColor(i % 2 == 0 ? RED : c("8a1020"));
                batch.draw(pixel, x - 0.7f * U, y - 0.7f * U, 1.4f * U, 1.4f * U);
            }
        }
        glow(batch, at[0], at[1], 20f * U, RED, 0.4f * k);
    }
}
