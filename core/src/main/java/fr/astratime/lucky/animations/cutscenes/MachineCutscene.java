package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemySymbol;

/**
 * Avant la Machine Originelle (chapitre 6) : le sommet de la Tour, dans les
 * étoiles. On monte le long de la machine, immense. Le levier descend, tout
 * seul, lentement. Cinq rouleaux s'allument un par un, le sol tremble. Les
 * rouleaux tournent : on voit passer les symboles de tous les chapitres. Ils
 * s'arrêtent sur cinq yeux, qui te regardent. Fondu blanc.
 */
public class MachineCutscene extends Cutscene {

    /** La caméra a fini de monter le long de la machine. */
    public static final float CLIMB_END   = 2.0f;
    /** Le levier descend (voir le son cutscene/machine). */
    public static final float LEVER_START = 1.9f;
    public static final float LEVER_DOWN  = 3.2f;
    /** Les rouleaux s'allument, un toutes les {@link #LIGHT_STEP} secondes. */
    public static final float LIGHT_START = 3.35f;
    public static final float LIGHT_STEP  = 0.2f;
    /** Les rouleaux tournent. */
    public static final float SPIN_START  = 4.3f;
    /** Ils s'arrêtent, un toutes les {@link #STOP_STEP} secondes. */
    public static final float STOP_START  = 5.5f;
    public static final float STOP_STEP   = 0.14f;
    public static final float COVER_START = 6.5f;
    public static final float COVER_FULL  = 7.2f;

    private static final int   REELS = 5;
    private static final float U     = 14f;          // un « pixel » de la machine, à l'écran
    /** Les symboles de tous les chapitres, qui défilent sur les rouleaux. */
    private static final EnemySymbol[] PARADE = {EnemySymbol.THORNS, EnemySymbol.LOADED_DIE, EnemySymbol.MIRROR,
        EnemySymbol.FAKE_MONEY, EnemySymbol.TAX, EnemySymbol.FANG, EnemySymbol.ZERO, EnemySymbol.HOURGLASS,
        EnemySymbol.PREDICTION, EnemySymbol.BANKRUPTCY, EnemySymbol.RAGE, EnemySymbol.INTEREST, EnemySymbol.ALL_IN,
        EnemySymbol.DUEL, EnemySymbol.NEW_RULE};

    private static final Color OUT = c("140a0a"), GOLD = c("ffc93a"), GOLD_LIGHT = c("ffe58a"), GOLD_DARK = c("b8801f");
    private static final Color GLASS = c("f4ecdc"), RED = c("e0283a"), SKY_TOP = c("05071a"), SKY_LOW = c("2b1a4a");
    private static final Color STONE = c("3b3550"), STONE_DARK = c("262238");

    private static final Color NIGHT_LOW = c("5a2c5e");

    private final Starfield stars = new Starfield(200, 0f);
    private final Color low = new Color();
    private final TextureRegion[] parade = new TextureRegion[PARADE.length + 1];
    private final TextureRegion   eye, gem;
    private final float[] reelSpin = new float[REELS];
    /** Pour la fin du chapitre : la machine telle qu'au combat (levier baissé, tout allumé, cinq yeux). */
    protected boolean settled;
    /** Assombrit la machine (1 = normale) : elle s'éteint en s'effondrant. */
    protected float   bright = 1f;
    private float nextTremble;
    private boolean clunk;
    private int stoppedCount;

    public MachineCutscene(CutsceneKit kit) {
        this(kit, "machine");
    }

    protected MachineCutscene(CutsceneKit kit, String sound) {
        super(kit.settings(), kit.shake(), kit.sound(sound));
        Color[] eyeColors = {OUT, Color.WHITE, c("e0283a"), c("ffc93a"), c("6e0f1e")};
        eye = art("owrgd", eyeColors,
            "...oooooo...",
            ".oowwwwwwoo.",
            "owwwggggwwwo",
            "owwgrrrrgwwo",
            "owwgrddrgwwo",
            "owwgrrrrgwwo",
            "owwwggggwwwo",
            ".oowwwwwwoo.",
            "...oooooo...");
        gem = art("ow", new Color[] {OUT, Color.WHITE},
            "..o..",
            ".owo.",
            "owwwo",
            ".owo.",
            "..o..");
        for (int i = 0; i < PARADE.length; i++) parade[i] = kit.symbol(PARADE[i]);
        parade[PARADE.length] = eye;                    // l'œil, sur lequel les rouleaux s'arrêtent
    }

    @Override protected float coverStart() { return COVER_START; }
    @Override protected float coverFull()  { return COVER_FULL; }

    @Override
    protected void reset() {
        for (int i = 0; i < REELS; i++) reelSpin[i] = settled ? PARADE.length : i * 3f;
        nextTremble = LIGHT_START;
        clunk = false;
        stoppedCount = 0;
    }

    @Override
    protected void simulate(float delta) {
        if (!clunk && time >= LEVER_DOWN) {
            clunk = true;
            rumble(0.35f, 10f);
        }
        if (time >= nextTremble && time < STOP_START + REELS * STOP_STEP) {
            rumble(0.2f, 2.5f + 4f * progress(LIGHT_START, STOP_START));
            nextTremble += 0.18f;
        }
        for (int i = 0; i < REELS; i++) {
            float stopAt = STOP_START + i * STOP_STEP;
            if (time >= SPIN_START && time < stopAt) {
                reelSpin[i] += MathUtils.lerp(2f, 22f, progress(SPIN_START, SPIN_START + 0.6f)) * delta;
            } else if (time >= stopAt && i >= stoppedCount) {
                reelSpin[i] = PARADE.length;              // pile sur l'œil
                stoppedCount = i + 1;
                rumble(0.12f, 6f);
            }
        }
    }

    @Override
    protected void drawScene(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        // La caméra monte : le décor descend.
        float climb = Interpolation.pow2.apply(progress(0f, CLIMB_END));
        float camera = MathUtils.lerp(-height * 0.75f, 0f, climb);
        drawSky(batch, 0f);
        float cx = width / 2f;
        float baseY = height * 0.04f - camera;           // bas de la machine à l'écran
        // Halo derrière la machine.
        float lit = progress(LIGHT_START, LIGHT_START + REELS * LIGHT_STEP);
        glow(batch, cx, baseY + 30f * U, 140f * U, c("6a4aff"), 0.25f);
        glow(batch, cx, baseY + 30f * U, 110f * U, GOLD, 0.15f + 0.35f * lit);
        // Rayons de lumière quand tout s'allume.
        if (lit > 0f) {
            additive(batch);
            for (int i = 0; i < 12; i++) {
                batch.setColor(1f, 0.85f, 0.4f, 0.12f * lit);
                batch.draw(trail, cx, baseY + 30f * U - 20f, 0f, 20f, width, 40f, 1f, 1f, i * 30f + time * 8f);
            }
            normal(batch);
        }
        drawTower(batch, cx, baseY);
        drawMachine(batch, cx, baseY);
        flash(batch, Color.WHITE, LEVER_DOWN, 0.15f, 0.25f);
        fadeFromBlack(batch, 0.7f);
    }

    /** Le ciel étoilé ; {@code night} (0 à 1) le fait virer à la nuit du chapitre 1, à l'horizon rose. */
    protected void drawSky(Batch batch, float night) {
        gradient(batch, low.set(SKY_LOW).lerp(NIGHT_LOW, night), SKY_TOP);
        stars.draw(batch, 1f);
    }

    /** Le sommet de la Tour : créneaux de pierre sous la machine. */
    protected void drawTower(Batch batch, float cx, float top) {
        float machineBright = bright;
        bright = 1f;                                   // la pierre ne s'éteint pas avec la machine
        float w = 100f * U;
        rect(batch, STONE_DARK, cx - w / 2f, top - 60f * U, w, 60f * U);
        for (int row = 0; row < 12; row++) {                       // briques
            float y = top - (row + 1) * 5f * U;
            for (int col = 0; col < 12; col++) {
                float x = cx - w / 2f + col * 8.4f * U + (row % 2) * 4.2f * U;
                if (x + 8f * U > cx + w / 2f) continue;
                rect(batch, STONE, x + U * 0.5f, y + U * 0.5f, 7.4f * U, 4f * U);
            }
        }
        for (int i = 0; i < 9; i++) {                              // créneaux
            float x = cx - w / 2f + i * 12f * U;
            rect(batch, STONE_DARK, x, top - U, 7f * U, 5f * U);
            rect(batch, STONE, x + U, top, 5f * U, 3f * U);
        }
        bright = machineBright;
    }

    /** La machine : corps doré, cinq rouleaux, plateau à pièces, dôme et levier. */
    protected void drawMachine(Batch batch, float cx, float baseY) {
        float w = 72f * U, h = 44f * U;
        float left = cx - w / 2f, bottom = baseY + 3f * U;
        // Levier (derrière le corps) : il descend tout seul.
        float lever = settled ? 1f : Interpolation.sine.apply(progress(LEVER_START, LEVER_DOWN));
        float pivotX = left + w + 2f * U, pivotY = bottom + h * 0.55f;
        float angle = MathUtils.lerp(80f, -70f, lever), length = 24f * U;
        rect(batch, OUT, pivotX - U, pivotY - 3f * U, 5f * U, 6f * U);
        rect(batch, GOLD_DARK, pivotX, pivotY - 2f * U, 3f * U, 4f * U);
        batch.setColor(OUT);
        batch.draw(pixel, pivotX + U, pivotY - 1.5f * U, 0f, 1.5f * U, length, 3f * U, 1f, 1f, angle);
        batch.setColor(c("d8d8e0"));
        batch.draw(pixel, pivotX + U, pivotY - 0.7f * U, 0f, 0.7f * U, length, 1.4f * U, 1f, 1f, angle);
        float knobX = pivotX + U + MathUtils.cosDeg(angle) * length, knobY = pivotY + MathUtils.sinDeg(angle) * length;
        rect(batch, OUT, knobX - 3.5f * U, knobY - 3.5f * U, 7f * U, 7f * U);
        rect(batch, RED, knobX - 2.5f * U, knobY - 2.5f * U, 5f * U, 5f * U);
        rect(batch, c("ff8a9a"), knobX - 1.5f * U, knobY + 0.5f * U, 1.5f * U, 1.5f * U);

        // Corps.
        rect(batch, OUT, left - U, bottom - U, w + 2f * U, h + 2f * U);
        rect(batch, GOLD, left, bottom, w, h);
        rect(batch, GOLD_LIGHT, left, bottom + h - 3f * U, w, 2f * U);
        rect(batch, GOLD_LIGHT, left, bottom, 2f * U, h);
        rect(batch, GOLD_DARK, left + w - 3f * U, bottom, 3f * U, h);
        rect(batch, GOLD_DARK, left, bottom, w, 2f * U);
        // Dôme à gradins et diamant au sommet.
        for (int step = 0; step < 4; step++) {
            float sw = w - (step + 1) * 14f * U, sy = bottom + h + step * 4f * U;
            rect(batch, OUT, cx - sw / 2f - U, sy, sw + 2f * U, 5f * U);
            rect(batch, step % 2 == 0 ? GOLD : GOLD_LIGHT, cx - sw / 2f, sy, sw, 4f * U);
        }
        float gemY = bottom + h + 20f * U;
        glow(batch, cx, gemY, 30f * U, Color.WHITE, (0.4f + 0.3f * MathUtils.sin(time * 5f)) * bright);
        batch.setColor(Color.WHITE);
        sprite(batch, gem, cx, gemY, 2f * U, 0f);

        // Ampoules sur le pourtour : elles s'allument avec les rouleaux.
        float lit = settled ? 1f : progress(LIGHT_START, LIGHT_START + REELS * LIGHT_STEP);
        int bulbs = 18;
        for (int i = 0; i < bulbs; i++) {
            float x = left + (i + 0.5f) * w / bulbs;
            boolean on = lit > i / (float) bulbs && ((int) (time * 8f) + i) % 2 == 0;
            for (float y : new float[] {bottom + h - 1.5f * U, bottom + 1.5f * U}) {
                rect(batch, OUT, x - U, y - U, 2f * U, 2f * U);
                if (on) glow(batch, x, y, 6f * U, GOLD, 0.6f * bright);
                rect(batch, on ? c("fff6c8") : GOLD_DARK, x - 0.5f * U, y - 0.5f * U, U, U);
            }
        }

        // Les cinq rouleaux.
        float reelW = 11f * U, reelH = 14f * U, gap = 2f * U;
        float panelW = REELS * reelW + (REELS + 1) * gap;
        float reelY = bottom + h * 0.45f;
        rect(batch, OUT, cx - panelW / 2f - U, reelY - gap - U, panelW + 2f * U, reelH + 2f * gap + 2f * U);
        rect(batch, GOLD_DARK, cx - panelW / 2f, reelY - gap, panelW, reelH + 2f * gap);
        for (int i = 0; i < REELS; i++) {
            float x = cx - panelW / 2f + gap + i * (reelW + gap);
            float on = settled ? 1f : progress(LIGHT_START + i * LIGHT_STEP, LIGHT_START + i * LIGHT_STEP + 0.12f);
            rect(batch, OUT, x, reelY, reelW, reelH);
            if (on <= 0f) continue;
            if (on > 0.9f) glow(batch, x + reelW / 2f, reelY + reelH / 2f, reelW * 2.2f, GOLD, 0.25f * bright);
            on *= bright;
            batch.setColor(GLASS.r * on, GLASS.g * on, GLASS.b * on, 1f);
            batch.draw(pixel, x + 0.5f * U, reelY + 0.5f * U, reelW - U, reelH - U);
            batch.setColor(on, on, on, 1f);
            float pad = 1.5f * U;
            reel(batch, parade, x + pad, reelY + pad + U, reelW - 2f * pad, reelW - 2f * pad,
                reelSpin[i] % parade.length);
            if (i < stoppedCount && !settled) {                         // l'œil qui te fixe
                float since = time - (STOP_START + i * STOP_STEP);
                glow(batch, x + reelW / 2f, reelY + reelH / 2f, reelW * 1.6f, RED, 0.5f * Math.max(0f, 1f - since));
            }
        }
        // Ligne de gain rouge.
        rect(batch, RED, cx - panelW / 2f - 2f * U, reelY + reelH / 2f - 0.5f * U, U, U);
        rect(batch, RED, cx + panelW / 2f + U, reelY + reelH / 2f - 0.5f * U, U, U);
        // Plateau à pièces.
        float trayW = 40f * U, trayY = bottom + 4f * U;
        rect(batch, OUT, cx - trayW / 2f, trayY, trayW, 7f * U);
        rect(batch, c("2a1a10"), cx - trayW / 2f + U, trayY + U, trayW - 2f * U, 5f * U);
        for (int i = 0; i < 9; i++) {
            float x = cx - trayW / 2f + 3f * U + i * 4f * U;
            rect(batch, GOLD, x, trayY + U, 3f * U, 2f * U + (i % 3) * U);
        }
    }

    private void rect(Batch batch, Color color, float x, float y, float w, float h) {
        batch.setColor(color.r * bright, color.g * bright, color.b * bright, color.a);
        batch.draw(pixel, x, y, w, h);
    }
}
