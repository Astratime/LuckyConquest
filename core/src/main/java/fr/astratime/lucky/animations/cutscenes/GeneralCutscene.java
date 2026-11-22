package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.tower.Chapter;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Avant le Général Va-Tout (La Guerre des Tables) : le champ de bataille de feutre à l'aube,
 * les canons tirent des jetons d'un camp à l'autre. Ses quatre soldats (le Sergent, le Canonnier,
 * l'Espionne, le Mercenaire) entrent au pas de chaque côté, au son du tambour, et se mettent en
 * ligne. Le Général se lève derrière eux, sous un éclat d'or ; il pousse deux grosses piles de
 * jetons au milieu de la table : il fait Va-tout. On s'approche de son regard d'or. Fondu noir.
 */
public class GeneralCutscene extends Cutscene {

    /** Les canons tirent : un jeton part de chaque camp à ces instants (voir le son cutscene/general). */
    public static final float[] SHOTS     = {0.4f, 1.0f, 1.5f, 2.0f};
    /** Durée du vol d'un jeton tiré, jusqu'à son explosion. */
    public static final float FLIGHT      = 0.6f;
    /** Ses soldats entrent au pas, au tambour (un coup toutes les {@link #STEP} s). */
    public static final float MARCH_START = 2.8f;
    public static final float MARCH_END   = 4.8f;
    public static final float STEP        = 0.25f;
    /** Le Général se lève derrière eux. */
    public static final float RISE        = 5.2f;
    public static final float RISE_END    = 6.0f;
    /** Il pousse ses deux piles de jetons au milieu : Va-tout. */
    public static final float PUSH        = 6.4f;
    public static final float ALL_IN      = 7.2f;
    /** On s'approche de son regard. */
    public static final float STARE       = 8.0f;
    public static final float COVER_START = 8.8f;
    public static final float COVER_FULL  = 9.5f;

    /** Ses quatre soldats, dans l'ordre où ils se placent (de gauche à droite). */
    private static final EnemyKind[] TROOPS = {EnemyKind.SERGENT, EnemyKind.CANONNIER, EnemyKind.ESPIONNE, EnemyKind.MERCENAIRE};
    /** Les yeux sur le portrait (34 x 42), depuis le milieu et depuis le bas. */
    private static final float[] EYES_X = {-3.5f, 3.5f};
    private static final float EYES_Y = 26.5f;

    private static final Color GOLD = hex("ffc93a"), FIRE = hex("ff8a1e"), SMOKE = hex("7a6e66");
    private static final Color[] CHIPS = {hex("d8283c"), hex("2a5ad8"), hex("1f9a4a"), hex("ffd54a")};

    private final TextureRegion field, general, chip;
    private final TextureRegion[] troops = new TextureRegion[TROOPS.length];
    private final Array<Particle> smoke = new Array<>(false, 256);
    private final Array<Particle> sparks = new Array<>(false, 256);
    private int  landed;
    private boolean pushed;

    public GeneralCutscene(CutsceneKit kit) {
        super(kit.settings(), kit.shake(), kit.sound("general"));
        field = kit.chapterArt(Chapter.GUERRE_DES_TABLES);
        general = kit.portrait(EnemyKind.GENERAL);
        for (int i = 0; i < TROOPS.length; i++) troops[i] = kit.portrait(TROOPS[i]);
        chip = art("orw", new Color[] {hex("1a0f0f"), Color.WHITE, hex("ffffff")},
            "..ooooo..",
            ".orrrrro.",
            "orrwrwrro",
            "orwrrrwro",
            "orrwrwrro",
            ".orrrrro.",
            "..ooooo..");
    }

    @Override protected float coverStart() { return COVER_START; }
    @Override protected float coverFull()  { return COVER_FULL; }
    @Override protected Color coverColor() { return Color.BLACK; }

    @Override
    protected void reset() {
        smoke.clear();
        sparks.clear();
        landed = 0;
        pushed = false;
    }

    @Override
    protected void simulate(float delta) {
        while (landed < SHOTS.length && time >= SHOTS[landed] + FLIGHT) {   // un jeton tombe : il explose
            float x = landX(landed), y = worldHeight() * 0.3f;
            rumble(0.25f, 7f);
            for (int i = 0; i < 26; i++) {
                Particle spark = particle(x, y, 20f + 140f * random.nextFloat(), 200f + 400f * random.nextFloat(), 0.7f, 10f);
                spark.gravity = 600f;
                spark.color.set(i % 3 == 0 ? GOLD : FIRE);
                sparks.add(spark);
            }
            for (int i = 0; i < 10; i++) {
                Particle cloud = particle(x, y, 60f + 60f * random.nextFloat(), 40f + 50f * random.nextFloat(), 2.4f,
                    60f + 50f * random.nextFloat());
                cloud.color.set(SMOKE);
                smoke.add(cloud);
            }
            landed++;
        }
        if (!pushed && time >= ALL_IN) {                                      // les deux piles se rejoignent
            pushed = true;
            rumble(0.4f, 10f);
            for (int i = 0; i < 40; i++) {
                Particle spark = particle(worldWidth() / 2f, pileY(), 360f * random.nextFloat(), 200f + 350f * random.nextFloat(),
                    0.8f, 12f);
                spark.drag = 0.2f;
                spark.color.set(GOLD);
                sparks.add(spark);
            }
        }
        update(smoke, delta);
        update(sparks, delta);
    }

    @Override
    protected void drawScene(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        // Le champ de bataille, qui s'assombrit quand le Général se lève.
        float zoom = Math.max(width / field.getRegionWidth(), height / field.getRegionHeight()) * 1.06f;
        float drift = MathUtils.lerp(-16f, 16f, progress(0f, COVER_FULL));
        float dark = 1f - 0.45f * progress(RISE - 0.4f, RISE_END);
        batch.setColor(dark, dark * 0.95f, dark * 0.92f, 1f);
        float artW = field.getRegionWidth() * zoom, artH = field.getRegionHeight() * zoom;
        batch.draw(field, (width - artW) / 2f + drift, (height - artH) / 2f, artW, artH);

        // Les jetons tirés d'un camp à l'autre, en cloche.
        for (int i = 0; i < SHOTS.length; i++) {
            float t = progress(SHOTS[i], SHOTS[i] + FLIGHT);
            if (time < SHOTS[i] || t >= 1f) continue;
            float fromX = i % 2 == 0 ? width * 0.3f : width * 0.7f;
            float x = MathUtils.lerp(fromX, landX(i), t);
            float y = MathUtils.lerp(height * 0.42f, height * 0.3f, t) + MathUtils.sin(t * MathUtils.PI) * height * 0.35f;
            glow(batch, x, y, 60f, FIRE, 0.5f);
            batch.setColor(CHIPS[i % CHIPS.length]);
            sprite(batch, chip, x, y, height / 140f, time * 400f);
            if (t < 0.15f) glow(batch, fromX, height * 0.42f, 140f, GOLD, 0.8f * (1f - t / 0.15f)); // le coup de feu
        }
        drawParticles(batch, smoke);

        // Le Général se lève derrière ses soldats ; on s'approche de son visage.
        if (time >= RISE - 0.2f) {
            float rise = Interpolation.pow2Out.apply(progress(RISE, RISE_END));
            float scale = generalScale();
            float baseY = generalBaseY() - (1f - rise) * 42f * scale * 0.6f;
            glow(batch, width / 2f, baseY + 24f * scale, 34f * scale, GOLD, 0.35f * rise);
            portrait(batch, general, width / 2f, baseY, scale, 0.85f * (1f - progress(RISE, RISE_END)), Math.min(1f, rise * 2f));
            if (time >= STARE) {                                              // son regard d'or
                float stare = progress(STARE, STARE + 0.3f);
                for (float eye : EYES_X) glow(batch, width / 2f + eye * scale, baseY + EYES_Y * scale, 5f * scale, GOLD, 0.9f * stare);
            }
        }

        // Ses quatre soldats entrent au pas, deux de chaque côté, et se mettent en ligne.
        if (time >= MARCH_START && time < STARE + 0.4f) {
            float scale = height / 150f;
            float march = progress(MARCH_START, MARCH_END);
            float leave = progress(STARE - 0.2f, STARE + 0.4f);
            for (int i = 0; i < troops.length; i++) {
                boolean left = i < 2;
                float target = width * (0.14f + i * 0.24f);
                float start = left ? -width * 0.2f - (1 - i) * width * 0.12f : width * 1.2f + (i - 2) * width * 0.12f;
                float x = MathUtils.lerp(start, target, Interpolation.sine.apply(march));
                float bob = march < 1f ? Math.abs(MathUtils.sin((time - MARCH_START) / STEP * MathUtils.PI / 2f)) * 8f : 0f;
                float y = height * 0.04f + bob - leave * height * 0.4f;
                portrait(batch, troops[i], x, y, scale, 0.15f, 1f - leave);
            }
        }

        // Va-tout : ses deux piles de jetons glissent au milieu de la table.
        if (time >= PUSH - 0.3f && time < STARE + 0.4f) {
            float push = Interpolation.pow2In.apply(progress(PUSH, ALL_IN));
            float appear = progress(PUSH - 0.3f, PUSH);
            float leave = progress(STARE - 0.2f, STARE + 0.4f);
            float chipScale = height / 120f;
            for (int side = -1; side <= 1; side += 2) {
                float x = width / 2f + side * MathUtils.lerp(width * 0.32f, width * 0.05f, push);
                for (int k = 0; k < 9; k++) {
                    batch.setColor(CHIPS[(k + (side > 0 ? 2 : 0)) % CHIPS.length].r, CHIPS[(k + (side > 0 ? 2 : 0)) % CHIPS.length].g,
                        CHIPS[(k + (side > 0 ? 2 : 0)) % CHIPS.length].b, appear * (1f - leave));
                    sprite(batch, chip, x, pileY() + k * 4f * chipScale, chipScale * 1.6f, 0f);
                }
            }
            if (time >= ALL_IN) glow(batch, width / 2f, pileY() + 20f * chipScale, 260f, GOLD, 0.6f * (1f - progress(ALL_IN, STARE)));
        }
        drawParticles(batch, sparks);
        flash(batch, GOLD, ALL_IN, 0.3f, 0.45f);
        fadeFromBlack(batch, 0.5f);
    }

    /** @return où tombe le jeton tiré {@code i} (fractions de l'écran, en quinconce). */
    private float landX(int i) { return worldWidth() * (i % 2 == 0 ? 0.62f + 0.06f * i : 0.38f - 0.06f * i); }

    /** @return la taille du Général : il grandit quand on s'approche de son visage. */
    private float generalScale() {
        return MathUtils.lerp(worldHeight() / 70f, worldHeight() / 40f, Interpolation.pow2In.apply(progress(STARE, COVER_FULL)));
    }

    /** @return le bas du portrait du Général, qui descend quand on s'approche de son visage. */
    private float generalBaseY() {
        return MathUtils.lerp(worldHeight() * 0.22f, -worldHeight() * 0.3f, Interpolation.pow2In.apply(progress(STARE, COVER_FULL)));
    }

    /** @return la hauteur de la table, où il pousse ses jetons. */
    private float pileY() { return worldHeight() * 0.1f; }
}
