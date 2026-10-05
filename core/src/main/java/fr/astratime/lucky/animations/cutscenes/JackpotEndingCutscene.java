package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * Fin du chapitre 6, après le Dernier tirage : la Machine Originelle s'éteint,
 * puis s'effondre en pluie de pièces. Une comète dorée en jaillit et repart
 * dans le ciel. On la suit : la nuit du chapitre 1 revient, avec sa ville de
 * casinos. Fondu blanc, puis « Tu as tiré le levier. Le monde a recommencé. »
 */
public class JackpotEndingCutscene extends MachineCutscene {

    /** La machine s'éteint (voir le son cutscene/jackpot_ending). */
    public static final float POWER_DOWN  = 0.6f;
    /** Elle s'effondre en pièces. */
    public static final float COLLAPSE    = 1.5f;
    /** La comète jaillit des décombres. */
    public static final float COMET       = 2.6f;
    /** On suit la comète vers le ciel : la ville du chapitre 1 apparaît. */
    public static final float FOLLOW      = 3.0f;
    public static final float NIGHT       = 4.3f;
    public static final float COVER_START = 5.5f;
    public static final float COVER_FULL  = 6.3f;

    private static final Color GOLD = c("ffc93a"), HOT = c("fff6c8"), ORANGE = c("ff7a1a");

    private final TextureRegion coin, city;
    private final Array<Particle> coins = new Array<>(false, 256);
    private final Array<Particle> fire  = new Array<>(false, 256);
    private final Array<Particle> streaks = new Array<>(false, 32);
    private boolean collapsed, launched;
    private float   fireDebt, streakDebt, cometX, cometY;

    public JackpotEndingCutscene(CutsceneKit kit) {
        super(kit, "jackpot_ending");
        settled = true;
        coin = load("hud/coin.png");
        city = region(skyline(340, 72, 42, false), false);
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
        }
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
        flash(batch, HOT, COLLAPSE, 0.25f, 0.5f);
        flash(batch, HOT, COMET, 0.25f, 0.6f);
        fadeFromBlack(batch, 0.4f);
    }
}
