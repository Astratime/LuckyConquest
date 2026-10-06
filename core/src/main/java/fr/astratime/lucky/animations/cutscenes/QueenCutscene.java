package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.enemy.EnemyKind;

/**
 * Avant la Reine des Tables (chapitre 2) : une cité en ruine, la nuit. Du vent ;
 * une carte vole entre les ruines et se colle à la roulette : une Dame de cœur.
 * La grande roulette dorée de la place se met à tourner seule. La bille court,
 * les cases s'allument derrière elle en traînée rouge ; elle saute de case en
 * case et tombe sur le zéro. Des jetons volent partout et forment un trône. Une
 * couronne de cartes se pose au-dessus, la Reine rit. Deux dés roulent jusqu'au
 * trône et s'allument comme des yeux ; la Reine y apparaît. Fondu rouge.
 */
public class QueenCutscene extends Cutscene {

    /** La Dame de cœur vole dans le vent (voir le son cutscene/queen) et se colle à la roulette. */
    public static final float CARD_START  = 0.2f;
    public static final float CARD_STUCK  = 1.4f;
    /** La roue se met à tourner. */
    public static final float WHEEL_START = 2.4f;
    /** La bille est lancée. */
    public static final float BALL_START  = 3.0f;
    /** La bille commence à sauter de case en case. */
    public static final float BOUNCE      = 5.7f;
    /** La bille tombe dans le zéro. */
    public static final float ZERO        = 6.5f;
    /** Les jetons jaillissent et forment le trône. */
    public static final float CHIPS       = 6.7f;
    public static final float THRONE      = 7.7f;
    /** Une couronne de cartes se pose au-dessus du trône ; la Reine rit. */
    public static final float CROWN       = 7.8f;
    public static final float LAUGH       = 8.5f;
    /** Les dés roulent jusqu'au trône. */
    public static final float DICE        = 9.0f;
    /** Les dés s'allument : la Reine apparaît. */
    public static final float EYES        = 9.7f;
    public static final float COVER_START = 10.2f;
    public static final float COVER_FULL  = 10.9f;

    private static final int    POCKETS = 37;
    private static final float  SQUASH  = 0.3f;
    /** La bille saute de case en case : ses cases (0 = le zéro) et l'instant de chaque saut. */
    private static final int[]   HOPS     = {9, 6, 4, 2, 1, 0};
    private static final float[] HOP_AT   = {5.7f, 5.88f, 6.04f, 6.18f, 6.3f, 6.4f};
    /** La traînée rouge de la bille : ses dernières positions. */
    private static final int     TRAIL    = 14;

    private static final Color SKY_TOP = c("070516"), SKY_LOW = c("2a1236"), GROUND = c("120910");
    private static final Color RED = c("e0283a"), GOLD = c("ffc93a"), GREEN = c("2fbf5a");

    private final TextureRegion wheel, city, throne, die, queen, lady, back;
    private final float[] trailX = new float[TRAIL], trailY = new float[TRAIL];
    private int   trailCount;
    private final Array<Particle> dust = new Array<>(false, 64);
    private float dustDebt;
    private final Pixmap        throneShape;
    private final Starfield     stars = new Starfield(140, 0.4f);
    private final Matrix4       saved = new Matrix4(), squashed = new Matrix4();
    private final Array<Particle> sparks = new Array<>(false, 128);
    private Array<Particle> chips = new Array<>();
    private float wheelAngle, wheelSpeed, ballAngle;
    private boolean landed, burst;

    public QueenCutscene(CutsceneKit kit) {
        super(kit.settings(), kit.shake(), kit.sound("queen"));
        wheel  = region(wheelPixmap(), true);
        city   = region(skyline(340, 60, 17, true), false);
        throneShape = thronePixmap();
        throne = region(copy(throneShape), false);
        die    = region(diePixmap(), false);
        queen  = kit.portrait(EnemyKind.REINE);
        Color[] card = {c("1a0a0e"), c("f4ecdc"), c("e0283a"), c("ffc93a")};
        lady = art("owrg", card,
            "ooooooooo",
            "owwwwwwwo",
            "owrwwwwwo",
            "owwwgwgwo",
            "owwwgggwo",
            "owwrrrrwo",
            "owwrwwrwo",
            "owwrwwrwo",
            "owwrwwrwo",
            "owwrrrrwo",
            "owwwrwwwo",
            "owwwwwrwo",
            "ooooooooo");
        back = art("orw", card,
            "ooooooooo",
            "orrrrrrro",
            "orwrwrwro",
            "orrwrwrro",
            "orwrwrwro",
            "orrwrwrro",
            "orwrwrwro",
            "orrwrwrro",
            "orwrwrwro",
            "orrwrwrro",
            "orwrwrwro",
            "orrrrrrro",
            "ooooooooo");
    }

    @Override protected float coverStart() { return COVER_START; }
    @Override protected float coverFull()  { return COVER_FULL; }
    @Override protected Color coverColor() { return RED; }

    @Override
    protected void reset() {
        wheelAngle = 0f;
        wheelSpeed = 0f;
        ballAngle = 90f;
        landed = burst = false;
        sparks.clear();
        dust.clear();
        dustDebt = 0f;
        trailCount = 0;
        chips = new Array<>();
    }

    @Override
    protected void simulate(float delta) {
        // La roue accélère, tourne, puis ralentit une fois la bille tombée.
        float target = time < WHEEL_START ? 0f : time < ZERO + 0.3f ? 240f : 40f;
        wheelSpeed = MathUtils.lerp(wheelSpeed, target, Math.min(1f, delta * (time < ZERO ? 1.4f : 2.5f)));
        wheelAngle += wheelSpeed * delta;
        if (time >= BALL_START && time < BOUNCE) {
            float k = progress(BALL_START, BOUNCE);
            ballAngle -= MathUtils.lerp(620f, 200f, k) * delta;
        }
        if (time >= BALL_START && time < ZERO) {                    // la traînée rouge suit la bille
            System.arraycopy(trailX, 0, trailX, 1, TRAIL - 1);
            System.arraycopy(trailY, 0, trailY, 1, TRAIL - 1);
            trailX[0] = ballX();
            trailY[0] = ballY();
            trailCount = Math.min(TRAIL, trailCount + 1);
        } else if (trailCount > 0 && time >= ZERO) {
            trailCount--;
        }
        if (time < WHEEL_START) {                                   // le vent : de la poussière qui file
            dustDebt += 30f * delta;
            while (dustDebt >= 1f) {
                dustDebt -= 1f;
                Particle bit = particle(-MARGIN, worldHeight() * (0.25f + 0.6f * random.nextFloat()),
                    -5f + 10f * random.nextFloat(), 700f + 500f * random.nextFloat(), 2.5f, 4f + 6f * random.nextFloat());
                bit.color.set(0.7f, 0.6f, 0.7f, 0.5f);
                dust.add(bit);
            }
        }
        update(dust, delta);
        if (!landed && time >= ZERO) {
            landed = true;
            rumble(0.25f, 6f);
            for (int i = 0; i < 30; i++) {
                Particle spark = particle(ballX(), ballY(), 20f + 140f * random.nextFloat(),
                    200f + 300f * random.nextFloat(), 0.5f + 0.4f * random.nextFloat(), 10f + 14f * random.nextFloat());
                spark.gravity = 500f;
                spark.color.set(random.nextBoolean() ? GREEN : GOLD);
                sparks.add(spark);
            }
        }
        if (!burst && time >= CHIPS) {
            burst = true;
            rumble(0.3f, 5f);
            chips = assemble(throneShape, centerX(), wheelY(), centerX(), throneY(), throneScale(), worldWidth() * 0.45f);
        }
        update(sparks, delta);
    }

    @Override
    protected void drawScene(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, SKY_LOW, SKY_TOP);
        stars.draw(batch, 1f);
        // La cité en ruine, en ombre chinoise.
        float horizon = height * 0.36f, scale = 4f;
        float cityWidth = city.getRegionWidth() * scale;
        batch.setColor(0.8f, 0.7f, 0.8f, 1f);
        for (float x = -MARGIN; x < width + MARGIN; x += cityWidth) {
            batch.draw(city, x, horizon - 8f, cityWidth, city.getRegionHeight() * scale);
        }
        batch.setColor(GROUND);
        batch.draw(pixel, -MARGIN, -MARGIN, width + 2f * MARGIN, horizon + MARGIN);

        // Lueur rouge qui monte derrière le trône.
        float threat = progress(CHIPS, EYES);
        glow(batch, centerX(), throneY(), height * 1.1f, RED, 0.35f * threat);

        drawParticles(batch, dust);
        drawWheel(batch);
        drawThrone(batch);
        drawCardCrown(batch);
        drawDice(batch);
        drawParticles(batch, sparks);

        // La Reine, d'abord en ombre, puis éclairée par ses dés.
        if (time >= EYES - 0.3f) {
            float appear = progress(EYES - 0.3f, EYES + 0.2f);
            float light = progress(EYES, EYES + 0.4f);
            portrait(batch, queen, centerX(), throneY() - throneScale() * 13f, 9f, 1f - light * 0.85f, appear);
        }
        drawFlyingCard(batch);
        flash(batch, GREEN, ZERO, 0.25f, 0.25f);
        fadeFromBlack(batch, 0.8f);
    }

    private void drawWheel(Batch batch) {
        float radius = wheelRadius();
        // Ombre et halo doré sous la roue.
        glow(batch, centerX(), wheelY(), radius * 2.8f, GOLD, 0.18f + 0.2f * progress(WHEEL_START, BALL_START));
        saved.set(batch.getTransformMatrix());
        squashed.set(saved).translate(centerX(), wheelY(), 0f).scale(1f, SQUASH, 1f);
        batch.setTransformMatrix(squashed);
        batch.setColor(Color.WHITE);
        float size = radius * 2f;
        batch.draw(wheel, -radius, -radius, radius, radius, size, size, 1f, 1f, wheelAngle);
        if (time >= CARD_STUCK) {                                      // la Dame de cœur, collée au centre
            float cs = radius * 0.035f;
            batch.draw(lady, -lady.getRegionWidth() * cs / 2f, -lady.getRegionHeight() * cs / 2f,
                lady.getRegionWidth() * cs / 2f, lady.getRegionHeight() * cs / 2f, lady.getRegionWidth() * cs,
                lady.getRegionHeight() * cs, 1f, 1f, wheelAngle + 20f);
        }
        batch.setTransformMatrix(saved);
        // Les cases s'allument derrière la bille : une traînée rouge.
        if (trailCount > 1) {
            additive(batch);
            for (int i = 0; i < trailCount; i++) {
                float fade = 1f - i / (float) TRAIL;
                batch.setColor(1f, 0.15f, 0.2f, 0.8f * fade);
                batch.draw(soft, trailX[i] - 80f * fade, trailY[i] - 45f * fade, 160f * fade, 90f * fade);
            }
            normal(batch);
        }
        // La bille et son reflet.
        if (time >= BALL_START) {
            float x = ballX(), y = ballY();
            glow(batch, x, y, 90f, Color.WHITE, 0.6f);
            batch.setColor(c("1a1418"));
            batch.draw(pixel, x - 11f, y - 11f, 22f, 22f);
            batch.setColor(c("f4f4ff"));
            batch.draw(pixel, x - 9f, y - 9f, 18f, 18f);
            batch.setColor(Color.WHITE);
            batch.draw(pixel, x - 7f, y + 1f, 6f, 6f);
        }
    }

    /** Les jetons qui s'assemblent, puis le trône. */
    private void drawThrone(Batch batch) {
        if (!burst) return;
        float build = progress(CHIPS, THRONE);
        if (build < 1f) {
            drawAssembled(batch, chips, build);
            return;
        }
        float scale = throneScale();
        batch.setColor(Color.WHITE);
        batch.draw(throne, centerX() - throne.getRegionWidth() * scale / 2f,
            throneY() - throne.getRegionHeight() * scale / 2f, throne.getRegionWidth() * scale,
            throne.getRegionHeight() * scale);
    }

    /** La Dame de cœur, emportée par le vent entre les ruines, qui vient se coller sur la roulette. */
    private void drawFlyingCard(Batch batch) {
        if (time < CARD_START || time >= CARD_STUCK) return;
        float k = progress(CARD_START, CARD_STUCK);
        float x = MathUtils.lerp(-worldWidth() * 0.1f, centerX(), Interpolation.sine.apply(k));
        float y = MathUtils.lerp(worldHeight() * 0.7f, wheelY(), Interpolation.pow2In.apply(k))
            + MathUtils.sin(k * MathUtils.PI * 3f) * worldHeight() * 0.06f * (1f - k);
        float flip = MathUtils.cos(k * MathUtils.PI * 5f);                // elle tournoie : face, dos, face...
        float scale = MathUtils.lerp(14f, wheelRadius() * 0.035f, k);
        TextureRegion face = flip >= 0f ? lady : back;
        float w = face.getRegionWidth() * scale * Math.max(0.1f, Math.abs(flip)), h = face.getRegionHeight() * scale;
        batch.setColor(Color.WHITE);
        batch.draw(face, x - w / 2f, y - h / 2f, w / 2f, h / 2f, w, h, 1f, 1f, k * 340f + 20f);
    }

    /** Une couronne de cinq cartes en éventail qui tombe et se pose au-dessus du trône ; elle tremble quand la Reine rit. */
    private void drawCardCrown(Batch batch) {
        if (time < CROWN) return;
        float land = Interpolation.bounceOut.apply(progress(CROWN, CROWN + 0.6f));
        float laugh = time >= LAUGH && time < DICE + 0.3f ? MathUtils.sin((time - LAUGH) * 40f) * 4f : 0f;
        float scale = 10f;
        float x = centerX(), y = MathUtils.lerp(worldHeight() + 150f, throneY() + throneScale() * 15f + 30f, land);
        glow(batch, x, y, 260f, RED, 0.3f * land);
        for (int i = -2; i <= 2; i++) {
            TextureRegion face = i == 0 ? lady : back;
            float w = face.getRegionWidth() * scale, h = face.getRegionHeight() * scale;
            batch.setColor(Color.WHITE);
            batch.draw(face, x - w / 2f, y - h * 0.2f + laugh * (i % 2 == 0 ? 1f : -1f), w / 2f, h * 0.2f, w, h, 1f, 1f,
                -i * 18f);
        }
    }

    /** Deux dés qui roulent jusqu'au pied du trône, puis s'allument en rouge. */
    private void drawDice(Batch batch) {
        if (time < DICE) return;
        float roll = Interpolation.pow2Out.apply(progress(DICE, EYES - 0.1f));
        float y = throneY() - throneScale() * 15f + 30f;
        for (int side = -1; side <= 1; side += 2) {
            float x = MathUtils.lerp(centerX() + side * worldWidth() * 0.6f, centerX() + side * throneScale() * 16f, roll);
            float hop = Math.abs(MathUtils.sin(roll * MathUtils.PI * 3f)) * 60f * (1f - roll);
            float spin = (1f - roll) * 900f * side;
            float eyes = progress(EYES - 0.1f, EYES + 0.2f);
            if (eyes > 0f) glow(batch, x, y + hop, 160f + 40f * MathUtils.sin(time * 12f), RED, 0.8f * eyes);
            batch.setColor(1f, 1f - 0.5f * eyes, 1f - 0.5f * eyes, 1f);
            sprite(batch, die, x, y + hop, 7f, spin);
        }
    }

    private float centerX()     { return worldWidth() / 2f; }
    private float wheelY()      { return worldHeight() * 0.2f; }
    private float wheelRadius() { return Math.min(worldWidth() * 0.32f, 560f); }
    private float throneY()     { return worldHeight() * 0.6f; }
    private float throneScale() { return 12f; }

    /** @return l'angle de la bille : elle tourne, puis saute de case en case et suit la roue dans le zéro. */
    private float ballAngleNow() {
        if (time < BOUNCE) return ballAngle;
        int hop = 0;
        while (hop + 1 < HOP_AT.length && time >= HOP_AT[hop + 1]) hop++;
        return wheelAngle - 90f + HOPS[hop] * 360f / POCKETS;
    }

    private float ballRadius() {
        if (time < BOUNCE) return wheelRadius() * MathUtils.lerp(0.98f, 0.86f, progress(BALL_START, BOUNCE));
        return wheelRadius() * 0.8f;
    }

    private float ballX() { return centerX() + MathUtils.cosDeg(ballAngleNow()) * ballRadius(); }

    private float ballY() {
        float y = wheelY() + MathUtils.sinDeg(ballAngleNow()) * ballRadius() * SQUASH;
        if (time >= BOUNCE && time < ZERO) {                        // petits bonds entre les cases
            int hop = 0;
            while (hop + 1 < HOP_AT.length && time >= HOP_AT[hop + 1]) hop++;
            float end = hop + 1 < HOP_AT.length ? HOP_AT[hop + 1] : ZERO;
            float k = (time - HOP_AT[hop]) / (end - HOP_AT[hop]);
            y += 4f * k * (1f - k) * 50f * (1f - hop / (float) HOPS.length);
        }
        return y;
    }

    // -------------------------------------------------------------------------
    // Images
    // -------------------------------------------------------------------------

    /** @return la roulette vue de dessus : jante dorée, cases rouges et noires, le zéro vert, cône de bois. */
    private static Pixmap wheelPixmap() {
        int size = 128;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        float center = (size - 1) / 2f, step = 360f / POCKETS;
        Color gold = c("ffc93a"), goldDark = c("b07a1a"), black = c("1a1418"), red = c("c41e30"), green = c("1f9e48");
        Color wood = c("6b3a1e"), woodDark = c("4a2614");
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - center, dy = center - y;
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                float angle = (MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees + 90f + step / 2f + 720f) % 360f;
                int pocket = (int) (angle / step);
                float within = angle - pocket * step;
                Color color;
                if (d > 63.5f) continue;
                else if (d > 59f) color = d > 62f ? goldDark : gold;
                else if (d > 44f) {
                    if (within < 0.9f) color = gold;                      // séparateurs dorés
                    else color = pocket == 0 ? green : pocket % 2 == 1 ? red : black;
                    if (d > 52f && d < 55f && pocket % 2 == 0 && pocket > 0) color = c("2a2226");
                } else if (d > 42f) color = gold;
                else if (d > 12f) {
                    int spoke = (int) (angle / 45f);
                    color = Math.abs(angle - spoke * 45f - 22.5f) < 2.5f ? goldDark : (spoke % 2 == 0 ? wood : woodDark);
                } else color = d > 9f ? goldDark : gold;
                pixmap.setColor(color);
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }

    /** @return le trône de la Reine : dossier de velours rouge, cadre doré, couronne au sommet. */
    private static Pixmap thronePixmap() {
        Pixmap p = new Pixmap(24, 30, Pixmap.Format.RGBA8888);
        p.setBlending(Pixmap.Blending.None);
        Color gold = c("ffc93a"), goldDark = c("a8701a"), velvet = c("a3172b"), velvetDark = c("6e0f1e"), out = c("1a0a0e");
        p.setColor(out);
        p.fillRectangle(3, 4, 18, 26);
        p.setColor(gold);
        p.fillRectangle(4, 5, 16, 18);                                 // cadre du dossier
        p.setColor(velvet);
        p.fillRectangle(6, 7, 12, 15);
        p.setColor(velvetDark);
        p.fillRectangle(6, 18, 12, 4);
        p.setColor(gold);
        p.fillRectangle(11, 10, 2, 6);                                 // un losange doré au centre
        p.fillRectangle(10, 12, 4, 2);
        p.setColor(out);                                               // couronne
        p.fillRectangle(6, 0, 12, 5);
        p.setColor(gold);
        p.fillRectangle(7, 2, 10, 2);
        p.drawPixel(7, 1);
        p.drawPixel(11, 1);
        p.drawPixel(12, 1);
        p.drawPixel(16, 1);
        p.setColor(c("e0283a"));
        p.drawPixel(11, 2);
        p.drawPixel(12, 2);
        p.setColor(goldDark);                                          // assise et accoudoirs
        p.fillRectangle(1, 21, 22, 3);
        p.setColor(gold);
        p.fillRectangle(1, 21, 22, 1);
        p.setColor(velvet);
        p.fillRectangle(4, 22, 16, 2);
        p.setColor(goldDark);                                          // pieds
        p.fillRectangle(3, 24, 3, 6);
        p.fillRectangle(18, 24, 3, 6);
        p.setColor(gold);
        p.fillRectangle(3, 24, 1, 6);
        p.fillRectangle(18, 24, 1, 6);
        return p;
    }

    /** @return un dé blanc cerné de noir, cinq points rouges. */
    private static Pixmap diePixmap() {
        Pixmap p = new Pixmap(11, 11, Pixmap.Format.RGBA8888);
        p.setBlending(Pixmap.Blending.None);
        p.setColor(c("1a0a0e"));
        p.fillRectangle(0, 0, 11, 11);
        p.setColor(c("f4ecdc"));
        p.fillRectangle(1, 1, 9, 9);
        p.setColor(c("cfc4b0"));
        p.fillRectangle(1, 8, 9, 2);
        p.setColor(c("e0283a"));
        int[][] pips = {{2, 2}, {7, 2}, {5, 5}, {2, 7}, {7, 7}};
        for (int[] pip : pips) p.fillRectangle(pip[0], pip[1], 2, 2);
        return p;
    }

    private static Pixmap copy(Pixmap source) {
        Pixmap pixmap = new Pixmap(source.getWidth(), source.getHeight(), Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.drawPixmap(source, 0, 0);
        return pixmap;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (!throneShape.isDisposed()) throneShape.dispose();
    }
}
