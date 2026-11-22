package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.assets.ShardsArt;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.tower.Chapter;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Avant le Collectionneur (les Éclats dispersés) : la plaine brûlée au pied du cratère, la nuit. Partout dans
 * l'herbe, des éclats de toutes les couleurs brillent. Un à un, ils se soulèvent et filent tous dans la même
 * direction. On les suit : ils entrent dans une grande vitrine de verre et se rangent sur ses étagères, clic, clic,
 * clic. Trois éclats restent en l'air, bleu, rose et vert. Derrière la vitre, une silhouette s'allume : le
 * Collectionneur. La porte de verre s'ouvre, les trois éclats viennent se sertir sur sa poitrine ; le bleu
 * s'allume. Sa loupe de joaillier jette un éclair. Fondu blanc.
 */
public class CollectorCutscene extends Cutscene {

    /** La plaine : les éclats brillent dans l'herbe (voir le son cutscene/collector). */
    public static final float PLAIN_END   = 3.6f;
    /** Les éclats se soulèvent, un à un, puis filent vers la droite. */
    public static final float LIFT        = 1.4f;
    public static final float FLY         = 2.6f;
    /** La vitrine : les éclats se rangent sur ses étagères (un clic chacun). */
    public static final float[] SHELVE    = {3.9f, 4.15f, 4.4f, 4.6f, 4.8f, 4.95f};
    /** Le Collectionneur s'allume derrière la vitre ; la porte s'ouvre. */
    public static final float REVEAL      = 5.6f;
    public static final float DOOR        = 6.3f;
    /** Les trois éclats de sa vitrine personnelle se sertissent sur sa poitrine, un par un. */
    public static final float[] SET       = {7.0f, 7.3f, 7.6f};
    /** Le premier (l'Héritage) s'allume ; sa loupe jette un éclair. */
    public static final float LIGHT       = 8.1f;
    public static final float GLINT       = 8.6f;
    public static final float COVER_START = 9.2f;
    public static final float COVER_FULL  = 9.9f;

    private static final Color NIGHT = hex("0a0a1e"), FRAME = hex("8a6a3a"), FRAME_DARK = hex("5a4224"),
        PANE = hex("1e2a3a"), HOT = hex("fff6c8");
    /** Les couleurs des éclats de la plaine. */
    private static final Color[] COLORS = {hex("ffd54a"), hex("e0283a"), hex("fff6c8"), hex("5ab0ff"), hex("ff6a9e"),
        hex("5ee08a"), hex("c8a0ff")};
    /** Où brillent les éclats dans la plaine (fractions de l'écran), et leur couleur. */
    private static final float[][] FIELD = {{0.08f, 0.20f}, {0.18f, 0.32f}, {0.30f, 0.16f}, {0.42f, 0.27f},
        {0.55f, 0.19f}, {0.66f, 0.30f}, {0.76f, 0.15f}, {0.88f, 0.25f}, {0.24f, 0.08f}, {0.60f, 0.07f}};

    private final TextureRegion plain, collector, gem;
    private final Color[] vitrine = new Color[ShardsArt.VITRINE_COLORS.length];
    private final Array<Particle> sparks = new Array<>(false, 256);
    private int shelved, set;
    private boolean lit, glinted;

    public CollectorCutscene(CutsceneKit kit) {
        super(kit.settings(), kit.shake(), kit.sound("collector"));
        plain = kit.chapterArt(Chapter.ECLATS_DISPERSES);
        collector = kit.portrait(EnemyKind.COLLECTIONNEUR);
        gem = art("o#w", new Color[] {hex("1a0a0e"), Color.WHITE, hex("ffffff")},
            "..o..",
            ".o#o.",
            "o###o",
            ".o#o.",
            "..o..");
        for (int i = 0; i < vitrine.length; i++) vitrine[i] = hex(ShardsArt.VITRINE_COLORS[i]);
    }

    @Override protected float coverStart() { return COVER_START; }
    @Override protected float coverFull()  { return COVER_FULL; }
    @Override protected Color coverColor() { return HOT; }

    @Override
    protected void reset() {
        sparks.clear();
        shelved = set = 0;
        lit = glinted = false;
    }

    @Override
    protected void simulate(float delta) {
        while (shelved < SHELVE.length && time >= SHELVE[shelved]) {          // clic : un éclat rangé
            float[] at = shelf(shelved);
            burst(at[0], at[1], COLORS[shelved % COLORS.length], 14, 300f);
            rumble(0.08f, 2f);
            shelved++;
        }
        while (set < SET.length && time >= SET[set]) {                         // un éclat serti sur sa poitrine
            burst(chestX(set), chestY(set), vitrine[set], 24, 420f);
            rumble(0.12f, 4f);
            set++;
        }
        if (!lit && time >= LIGHT) {
            lit = true;
            burst(chestX(0), chestY(0), vitrine[0], 60, 800f);
            rumble(0.4f, 9f);
        }
        if (!glinted && time >= GLINT) {
            glinted = true;
            burst(eyeX(), eyeY(), HOT, 30, 600f);
        }
        update(sparks, delta);
    }

    private void burst(float x, float y, Color color, int count, float speed) {
        for (int i = 0; i < count; i++) {
            Particle spark = particle(x, y, 360f * random.nextFloat(), speed * (0.3f + 0.7f * random.nextFloat()),
                0.4f + 0.5f * random.nextFloat(), 6f + 14f * random.nextFloat());
            spark.drag = 0.3f;
            spark.color.set(random.nextBoolean() ? color : HOT);
            sparks.add(spark);
        }
    }

    @Override
    protected void drawScene(Batch batch) {
        if (time < PLAIN_END) {
            drawPlain(batch);
            fadeFromBlack(batch, 0.6f);
        } else {
            drawVitrine(batch);
        }
        drawParticles(batch, sparks);
        flash(batch, HOT, LIGHT, 0.25f, 0.5f);
        fadeThroughBlack(batch, PLAIN_END, 0.25f);
    }

    /** La plaine du cratère : ses éclats brillent, se soulèvent un à un, puis filent tous vers la droite. */
    private void drawPlain(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        fill(batch, NIGHT, 1f);
        float zoom = Math.max(width / plain.getRegionWidth(), height / plain.getRegionHeight()) * 1.15f;
        float pan = 60f * progress(0f, PLAIN_END);
        batch.setColor(0.85f, 0.85f, 0.95f, 1f);
        batch.draw(plain, (width - plain.getRegionWidth() * zoom) / 2f - pan, (height - plain.getRegionHeight() * zoom) / 2f,
            plain.getRegionWidth() * zoom, plain.getRegionHeight() * zoom);
        float scale = height / 90f;
        for (int i = 0; i < FIELD.length; i++) {
            Color color = COLORS[i % COLORS.length];
            float x0 = FIELD[i][0] * width - pan, y0 = FIELD[i][1] * height;
            float lift = Interpolation.pow2Out.apply(progress(LIFT + i * 0.08f, LIFT + 0.6f + i * 0.08f));
            float fly = Interpolation.pow2In.apply(progress(FLY + i * 0.05f, PLAIN_END - 0.2f));
            float x = MathUtils.lerp(x0, width * 1.2f, fly);
            float y = y0 + 60f * lift + MathUtils.sin(time * 3f + i) * 6f * lift + height * 0.25f * fly;
            float twinkle = 0.6f + 0.4f * MathUtils.sin(time * 5f + i * 1.7f);
            glow(batch, x, y, 70f * scale * (0.5f + 0.5f * twinkle), color, 0.5f);
            if (fly > 0f) {                                                    // une traînée derrière l'éclat qui file
                additive(batch);
                batch.setColor(color.r, color.g, color.b, 0.5f * fly);
                batch.draw(trail, x, y - 6f, 0f, 6f, 300f * fly, 12f, 1f, 1f, 180f + 14f);
                normal(batch);
            }
            batch.setColor(color);
            batch.draw(gem, x - gem.getRegionWidth() * scale * 0.7f, y - gem.getRegionHeight() * scale * 0.7f,
                gem.getRegionWidth() * scale * 1.4f, gem.getRegionHeight() * scale * 1.4f);
        }
        batch.setColor(Color.WHITE);
    }

    /** La vitrine : les éclats se rangent ; le Collectionneur s'allume derrière la vitre, sort, et se sertit ses trois éclats. */
    private void drawVitrine(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, hex("120c1a"), hex("2a1a30"));
        float x0 = width * 0.18f, x1 = width * 0.82f, y0 = height * 0.08f, y1 = height * 0.95f;
        // Le cadre de bois doré et le verre sombre.
        rect(batch, FRAME_DARK, x0 - 24f, y0 - 24f, x1 - x0 + 48f, y1 - y0 + 48f);
        rect(batch, FRAME, x0 - 14f, y0 - 14f, x1 - x0 + 28f, y1 - y0 + 28f);
        rect(batch, PANE, x0, y0, x1 - x0, y1 - y0);
        // Le Collectionneur, qui s'allume derrière la vitre, puis s'avance.
        float reveal = progress(REVEAL, REVEAL + 0.7f);
        if (reveal > 0f) {
            batch.setColor(Color.WHITE);
            portrait(batch, collector, width / 2f, collectorBottom(), collectorScale(), 1f - reveal, 1f);
        }
        // Les étagères et leurs éclats rangés.
        for (int row = 0; row < 3; row++) {
            float y = shelfY(row);
            rect(batch, FRAME, x0, y - 8f, x1 - x0, 10f);
        }
        for (int i = 0; i < SHELVE.length; i++) {
            float[] at = shelf(i);
            float arrive = Interpolation.pow3Out.apply(progress(SHELVE[i] - 0.35f, SHELVE[i]));
            if (arrive <= 0f) continue;
            float x = MathUtils.lerp(-80f, at[0], arrive), y = MathUtils.lerp(height * 0.6f, at[1], arrive);
            drawGem(batch, x, y, COLORS[i % COLORS.length], 1f, 0.7f);
        }
        // La porte de verre qui s'ouvre : un reflet qui glisse sur la vitre.
        float door = Interpolation.pow2.apply(progress(DOOR, DOOR + 0.6f));
        float reflectX = MathUtils.lerp(x0 + 40f, x1 + 200f, door);
        additive(batch);
        batch.setColor(0.6f, 0.75f, 0.9f, 0.18f * (1f - door));
        batch.draw(pixel, reflectX, y0, 60f, y1 - y0);
        batch.setColor(0.6f, 0.75f, 0.9f, 0.1f * (1f - door));
        batch.draw(pixel, reflectX + 90f, y0, 20f, y1 - y0);
        normal(batch);
        // Les trois éclats de sa vitrine personnelle : ils flottent, puis viennent se sertir sur sa poitrine.
        if (time >= SHELVE[SHELVE.length - 1]) {
            for (int i = 0; i < vitrine.length; i++) {
                float hoverX = width * (0.35f + 0.15f * i), hoverY = height * 0.78f + MathUtils.sin(time * 4f + i) * 8f;
                float into = Interpolation.pow3In.apply(progress(SET[i] - 0.35f, SET[i]));
                float x = MathUtils.lerp(hoverX, chestX(i), into), y = MathUtils.lerp(hoverY, chestY(i), into);
                float bright = i == 0 && lit ? 1f : time >= SET[i] ? 0.35f : 0.8f;
                drawGem(batch, x, y, vitrine[i], bright, time >= SET[i] ? 0.55f : 1f);
            }
        }
        // Sa loupe qui jette un éclair.
        if (glinted) {
            float since = time - GLINT;
            float k = Math.max(0f, 1f - since / 0.5f);
            glow(batch, eyeX(), eyeY(), 260f * k, HOT, k);
            additive(batch);
            batch.setColor(1f, 1f, 1f, 0.7f * k);
            batch.draw(trail, eyeX(), eyeY() - 4f, 0f, 4f, 320f * k, 8f, 1f, 1f, 30f);
            batch.draw(trail, eyeX(), eyeY() - 4f, 0f, 4f, 320f * k, 8f, 1f, 1f, 210f);
            normal(batch);
        }
    }

    /** Un éclat de couleur {@code color}, de taille {@code size} (1 : sa taille sur les étagères). */
    private void drawGem(Batch batch, float x, float y, Color color, float bright, float size) {
        float scale = worldHeight() / 60f * size;
        glow(batch, x, y, 90f * scale / 3f * (0.6f + 0.4f * bright), color, 0.6f * bright);
        batch.setColor(color.r * (0.5f + 0.5f * bright), color.g * (0.5f + 0.5f * bright), color.b * (0.5f + 0.5f * bright), 1f);
        float w = gem.getRegionWidth() * scale, h = gem.getRegionHeight() * scale;
        batch.draw(gem, x - w / 2f, y - h / 2f, w, h);
        batch.setColor(Color.WHITE);
    }

    private void rect(Batch batch, Color color, float x, float y, float w, float h) {
        batch.setColor(color);
        batch.draw(pixel, x, y, w, h);
        batch.setColor(Color.WHITE);
    }

    private float shelfY(int row) { return worldHeight() * (0.30f + 0.22f * row); }

    /** @return où se range l'éclat {@code i} : deux par étagère, aux bords de la vitrine (le milieu reste à lui). */
    private float[] shelf(int i) {
        float left = worldWidth() * 0.24f, right = worldWidth() * 0.76f;
        return new float[] {i % 2 == 0 ? left : right, shelfY(i / 2) + 24f};
    }

    /** @return son pas en avant, quand la porte s'ouvre (de 0 à 1). */
    private float step() { return Interpolation.pow2Out.apply(progress(DOOR, SET[0])); }
    private float collectorScale() { return worldHeight() * 0.8f / collector.getRegionHeight() * (1f + 0.08f * step()); }
    private float collectorBottom() { return worldHeight() * 0.1f - 30f * step(); }
    /** @return où est serti l'éclat {@code i} sur sa poitrine (voir {@link ShardsArt#VITRINE_AT}). */
    private float chestX(int i) {
        return worldWidth() / 2f + (ShardsArt.VITRINE_AT[i][0] + 0.5f - collector.getRegionWidth() / 2f) * collectorScale();
    }
    private float chestY(int i) {
        return collectorBottom() + (collector.getRegionHeight() - ShardsArt.VITRINE_AT[i][1] - 0.5f) * collectorScale();
    }
    /** @return sa loupe de joaillier (pixel 20, 15 du portrait). */
    private float eyeX() { return worldWidth() / 2f + (20.5f - collector.getRegionWidth() / 2f) * collectorScale(); }
    private float eyeY() { return collectorBottom() + (collector.getRegionHeight() - 15.5f) * collectorScale(); }
}
