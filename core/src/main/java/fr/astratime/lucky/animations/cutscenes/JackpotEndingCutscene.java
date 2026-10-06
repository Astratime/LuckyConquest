package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.Symbol;

/**
 * Fin du chapitre 6, après le Dernier tirage : la Machine Originelle s'éteint,
 * ses cinq yeux se ferment un par un (le dernier cligne deux fois), puis elle
 * s'effondre en pluie de pièces et de symboles de tous les chapitres. Une
 * comète dorée en jaillit et repart dans le ciel. On la suit : la nuit du
 * chapitre 1 revient, avec sa ville de casinos. Une fenêtre s'allume : une
 * petite machine à sous y clignote, son levier descend, le tout premier tirage
 * recommence. Fondu blanc, puis « Tu as tiré le levier. Le monde a recommencé. »
 */
public class JackpotEndingCutscene extends MachineCutscene {

    /** La machine s'éteint (voir le son cutscene/jackpot_ending). */
    public static final float POWER_DOWN  = 0.6f;
    /** Ses yeux se ferment, un toutes les {@link #LID_STEP} secondes ; le dernier cligne deux fois. */
    public static final float LIDS        = 1.3f;
    public static final float LID_STEP    = 0.25f;
    public static final float LAST_BLINKS = 2.35f;
    public static final float LAST_CLOSE  = 3.05f;
    /** Elle s'effondre en pièces et en symboles. */
    public static final float COLLAPSE    = 3.4f;
    /** La comète jaillit des décombres. */
    public static final float COMET       = 5.1f;
    /** On suit la comète vers le ciel : la ville du chapitre 1 apparaît. */
    public static final float FOLLOW      = 5.5f;
    public static final float NIGHT       = 6.8f;
    /** Un immeuble s'approche, une fenêtre s'allume : une petite machine à sous. */
    public static final float WINDOW      = 7.3f;
    public static final float WINDOW_LIT  = 7.9f;
    /** Son levier descend : le tout premier tirage. */
    public static final float FIRST_PULL  = 8.5f;
    public static final float COVER_START = 9.5f;
    public static final float COVER_FULL  = 10.3f;

    private static final Color GOLD = c("ffc93a"), HOT = c("fff6c8"), ORANGE = c("ff7a1a");

    private final TextureRegion city;
    /** Les rouleaux de la petite machine du chapitre 1 : cerise, sept, cloche. */
    private final TextureRegion[] firstReels;
    private final Array<Particle> coins = new Array<>(false, 256);
    private final Array<Particle> fire  = new Array<>(false, 256);
    private final Array<Particle> streaks = new Array<>(false, 32);
    private boolean collapsed, launched;
    private float   fireDebt, streakDebt, cometX, cometY;

    public JackpotEndingCutscene(CutsceneKit kit) {
        super(kit, "jackpot_ending");
        settled = true;
        city = region(skyline(340, 72, 42, false), false);
        firstReels = new TextureRegion[] {load(Symbol.CHERRY.getAssetPath()), load(Symbol.SEVEN.getAssetPath()),
            load(Symbol.BELL.getAssetPath()), load(Symbol.BAR.getAssetPath())};
    }

    @Override protected float coverStart() { return COVER_START; }
    @Override protected float coverFull()  { return COVER_FULL; }

    @Override
    protected void reset() {
        super.reset();
        coins.clear();
        fire.clear();
        streaks.clear();
        collapsed = launched = false;
        fireDebt = streakDebt = 0f;
        bright = 1f;
        java.util.Arrays.fill(lids, 0f);
    }

    @Override
    protected void simulate(float delta) {
        float width = worldWidth(), height = worldHeight();
        if (time >= POWER_DOWN && time < COLLAPSE) {                 // elle s'éteint en grésillant
            float k = progress(POWER_DOWN, COLLAPSE);
            bright = 1f - 0.45f * k + (random.nextFloat() < 0.25f ? 0.25f : 0f);
        }
        if (!collapsed && time >= COLLAPSE) {
            collapsed = true;
            rumble(1.0f, 20f);
            for (int i = 0; i < 160; i++) {
                Particle piece = particle(width / 2f + (random.nextFloat() - 0.5f) * width * 0.4f, height * 0.4f,
                    40f + 100f * random.nextFloat(), 500f + 900f * random.nextFloat(), 1.6f + random.nextFloat(),
                    1.2f + 1.2f * random.nextFloat());
                piece.gravity = 1300f;
                piece.region = coin;
                piece.spin = (random.nextFloat() - 0.5f) * 900f;
                coins.add(piece);
            }
            for (int i = 0; i < 26; i++) {                            // les symboles de tous les chapitres
                TextureRegion symbol = parade[random.nextInt(parade.length - 1)];
                Particle piece = particle(width / 2f + (random.nextFloat() - 0.5f) * width * 0.35f, height * 0.45f,
                    50f + 80f * random.nextFloat(), 600f + 700f * random.nextFloat(), 2.0f + random.nextFloat(),
                    70f / symbol.getRegionWidth() * (0.8f + 0.5f * random.nextFloat()));
                piece.gravity = 900f;
                piece.region = symbol;
                piece.spin = (random.nextFloat() - 0.5f) * 400f;
                coins.add(piece);
            }
        }
        for (int i = 0; i < lids.length - 1; i++) {                // ses yeux se ferment un par un
            lids[i] = progress(LIDS + i * LID_STEP, LIDS + i * LID_STEP + 0.15f);
        }
        lids[lids.length - 1] = lastLid();
        if (collapsed) bright = Math.max(0.15f, 0.55f - (time - COLLAPSE));
        if (!launched && time >= COMET) {
            launched = true;
            rumble(0.4f, 10f);
        }
        if (launched && time < NIGHT + 0.6f) {                       // la traîne de feu de la comète
            float k = progress(COMET, NIGHT);
            cometX = MathUtils.lerp(width / 2f, width * 0.2f, Interpolation.pow2In.apply(k));
            cometY = MathUtils.lerp(height * 0.45f, height * 1.25f, Interpolation.pow2Out.apply(k));
            fireDebt += 180f * delta;
            while (fireDebt >= 1f) {
                fireDebt -= 1f;
                Particle flame = particle(cometX + (random.nextFloat() - 0.5f) * 30f, cometY, 270f + (random.nextFloat()
                    - 0.5f) * 50f, 80f + 160f * random.nextFloat(), 0.5f + 0.5f * random.nextFloat(),
                    40f + 50f * random.nextFloat());
                flame.drag = 0.3f;
                flame.color.set(random.nextFloat() < 0.5f ? GOLD : ORANGE);
                fire.add(flame);
            }
        }
        if (time >= NIGHT - 0.4f && time < COVER_START) {             // la nuit du chapitre 1 et ses étoiles filantes
            streakDebt += 6f * delta;
            while (streakDebt >= 1f) {
                streakDebt -= 1f;
                Particle streak = particle(width * (0.3f + 0.9f * random.nextFloat()), height * (0.6f + 0.4f *
                    random.nextFloat()), 200f + 18f * random.nextFloat(), 1000f + 600f * random.nextFloat(),
                    0.5f + 0.4f * random.nextFloat(), 160f + 160f * random.nextFloat());
                streaks.add(streak);
            }
        }
        update(coins, delta);
        update(fire, delta);
        update(streaks, delta);
    }

    /** @return la paupière du dernier œil : il cligne deux fois, puis se ferme. */
    private float lastLid() {
        float t = time - LAST_BLINKS;
        if (t < 0f) return 0f;
        if (t < 0.6f) {                                              // deux clignements de 0,3 s
            float k = (t % 0.3f) / 0.3f;
            return k < 0.4f ? k / 0.4f : Math.max(0f, 1f - (k - 0.4f) / 0.35f);
        }
        return progress(LAST_CLOSE, LAST_CLOSE + 0.2f);
    }

    /**
     * Dans la ville du chapitre 1, un immeuble s'approche ; une fenêtre
     * s'allume : une petite machine à sous y clignote, son levier descend et
     * ses trois rouleaux partent. Le tout premier tirage.
     */
    private void drawFirstMachine(Batch batch, float width, float height) {
        float come = Interpolation.pow2Out.apply(progress(WINDOW, WINDOW_LIT));
        if (come <= 0f) return;
        float u = height / 75f;
        float bw = 46f * u, bh = 70f * u, bx = width * 0.66f;
        float by = MathUtils.lerp(-bh - MARGIN, -MARGIN, come);
        rect(batch, c("07040c"), bx - bw / 2f, by, bw, bh);                      // l'immeuble, en ombre
        rect(batch, c("07040c"), bx - bw / 2f + 6f * u, by + bh, bw - 12f * u, 4f * u);
        for (int row = 0; row < 5; row++) {                                       // des fenêtres éteintes
            for (int col = 0; col < 4; col++) {
                if (row == 3 && (col == 1 || col == 2)) continue;
                rect(batch, c("2a1a20"), bx - bw / 2f + (5f + col * 10f) * u, by + (8f + row * 12f) * u, 6f * u, 6f * u);
            }
        }
        float wx = bx - bw / 2f + 14f * u, wy = by + 40f * u, ww = 18f * u, wh = 15f * u;     // la fenêtre
        float lit = progress(WINDOW_LIT, WINDOW_LIT + 0.25f);
        rect(batch, c("2a1a20"), wx, wy, ww, wh);
        if (lit > 0f) {
            glow(batch, wx + ww / 2f, wy + wh / 2f, 60f * u, c("ffb040"), 0.45f * lit);
            batch.setColor(1f, 0.8f, 0.45f, lit);
            batch.draw(pixel, wx, wy, ww, wh);
        }
        rect(batch, c("07040c"), wx + ww / 2f - 0.5f * u, wy, u, wh);                 // croisillons
        rect(batch, c("07040c"), wx, wy + wh * 0.62f, ww, u);
        if (lit <= 0f) return;
        // La petite machine, derrière la vitre.
        float mw = 10f * u, mh = 9f * u, mx = wx + ww / 2f - mw / 2f - u, my = wy + 1f * u;
        batch.setColor(0.08f, 0.04f, 0.05f, lit);
        batch.draw(pixel, mx - 0.5f * u, my - 0.5f * u, mw + u, mh + u);
        batch.setColor(0.85f * lit + 0.15f, 0.15f, 0.2f, lit);
        batch.draw(pixel, mx, my, mw, mh);
        float spin = time < FIRST_PULL + 0.25f ? 0f : (time - FIRST_PULL - 0.25f) * 14f;
        for (int i = 0; i < 3; i++) {
            float rx = mx + (0.8f + i * 3f) * u, ry = my + 3.5f * u, rs = 2.6f * u;
            batch.setColor(1f, 1f, 1f, lit);
            batch.draw(pixel, rx - 0.2f * u, ry - 0.2f * u, rs + 0.4f * u, rs + 0.4f * u);
            reel(batch, firstReels, rx, ry, rs, rs, (i + spin * (1f + 0.15f * i)) % firstReels.length);
        }
        for (int i = 0; i < 4; i++) {                                              // ses ampoules clignotent
            boolean on = ((int) (time * 6f) + i) % 2 == 0;
            float lx = mx + (1f + i * 2.6f) * u, ly = my + mh - 1.2f * u;
            batch.setColor(on ? 1f : 0.5f, on ? 0.95f : 0.4f, on ? 0.6f : 0.2f, lit);
            batch.draw(pixel, lx, ly, 0.9f * u, 0.9f * u);
            if (on) glow(batch, lx + 0.45f * u, ly + 0.45f * u, 3f * u, c("ffc93a"), 0.5f * lit);
        }
        float pull = Interpolation.pow2In.apply(progress(FIRST_PULL, FIRST_PULL + 0.25f));    // le levier
        float angle = MathUtils.lerp(75f, -55f, pull), length = 4.5f * u;
        float px = mx + mw + 0.4f * u, py = my + mh * 0.55f;
        batch.setColor(0.8f, 0.8f, 0.85f, lit);
        batch.draw(pixel, px, py - 0.3f * u, 0f, 0.3f * u, length, 0.6f * u, 1f, 1f, angle);
        batch.setColor(0.9f, 0.15f, 0.2f, lit);
        batch.draw(pixel, px + MathUtils.cosDeg(angle) * length - 0.7f * u, py + MathUtils.sinDeg(angle) * length - 0.7f * u,
            1.4f * u, 1.4f * u);
        additive(batch);                                                           // reflet sur la vitre
        batch.setColor(1f, 1f, 1f, 0.12f * lit);
        batch.draw(pixel, wx + 2f * u, wy, 0f, 0f, 1.5f * u, wh, 1f, 1f, -15f);
        normal(batch);
    }

    private void rect(Batch batch, Color color, float x, float y, float w, float h) {
        batch.setColor(color);
        batch.draw(pixel, x, y, w, h);
    }

    @Override
    protected void drawScene(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        // On suit la comète : le sommet de la Tour descend et sort de l'écran.
        float follow = Interpolation.pow2In.apply(progress(FOLLOW, NIGHT)) * height * 1.3f;
        float sink = collapsed ? Interpolation.pow2In.apply(progress(COLLAPSE, COLLAPSE + 1f)) * 40f * 14f : 0f;
        super.drawSky(batch, progress(FOLLOW, NIGHT));
        float baseY = height * 0.04f - follow;
        drawMachine(batch, width / 2f, baseY - sink);
        drawTower(batch, width / 2f, baseY);
        if (collapsed) {
            float dust = Math.max(0f, 1f - (time - COLLAPSE) / 1.6f);
            glow(batch, width / 2f, baseY + 10f * 14f, width * 0.9f, c("8a6a40"), 0.6f * dust);
        }
        drawParticles(batch, coins);
        drawParticles(batch, fire);
        if (launched && time < NIGHT + 0.6f) {
            glow(batch, cometX, cometY, 260f, ORANGE, 0.6f);
            glow(batch, cometX, cometY, 120f, HOT, 0.95f);
        }

        // La ville de casinos du chapitre 1 remonte du bas de l'écran.
        float night = Interpolation.pow2Out.apply(progress(NIGHT - 0.6f, NIGHT + 0.6f));
        if (night > 0f) {
            float scale = 4f, cityW = city.getRegionWidth() * scale, cityH = city.getRegionHeight() * scale;
            float y = MathUtils.lerp(-cityH, 0f, night);
            batch.setColor(0.8f, 0.72f, 0.7f, 1f);
            for (float x = -MARGIN - 30f; x < width + MARGIN; x += cityW) batch.draw(city, x, y, cityW, cityH);
            additive(batch);
            for (Particle streak : streaks) {
                float life = streak.life();
                float alpha = Math.min(1f, life * 6f) * (1f - life * life);
                float angle = MathUtils.atan2(streak.vy, streak.vx) * MathUtils.radiansToDegrees;
                batch.setColor(1f, 0.95f, 0.8f, alpha);
                batch.draw(trail, streak.x - streak.size, streak.y - 2f, streak.size, 2f, streak.size, 4f, 1f, 1f, angle);
            }
            normal(batch);
        }
        drawFirstMachine(batch, width, height);
        flash(batch, HOT, COLLAPSE, 0.25f, 0.5f);
        flash(batch, HOT, COMET, 0.25f, 0.6f);
        fadeFromBlack(batch, 0.4f);
    }
}
