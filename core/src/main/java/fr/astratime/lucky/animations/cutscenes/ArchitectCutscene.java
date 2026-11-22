package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.tower.Chapter;
import fr.astratime.lucky.i18n.Lang;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Avant l'Architecte (chapitre « L'Ascension ») : on monte le long de la Tour, jusqu'aux nuages. Noir. Un plan bleu
 * se déroule : un trait blanc dessine la Tour, étage après étage, puis la Machine tout en haut. Un compas trace un
 * cercle autour d'elle. L'Architecte sort de l'ombre derrière son plan : « Chaque étage, c'est moi qui l'ai posé. »
 * Les traits du plan s'embrasent d'or : « Monte. Je t'attends en haut. » Fondu noir.
 */
public class ArchitectCutscene extends Cutscene {

    /** On monte le long de la Tour (voir le son cutscene/architect), puis le plan se déroule. */
    public static final float CLIMB_END   = 1.6f;
    public static final float UNROLL_END  = 2.2f;
    /** Les étages de la Tour se dessinent un par un. */
    public static final float FLOORS_START = 2.3f;
    public static final float FLOOR_STEP   = 0.28f;
    public static final int   FLOORS       = 8;
    /** La Machine se dessine au sommet. */
    public static final float MACHINE      = FLOORS_START + FLOORS * FLOOR_STEP + 0.1f;
    /** L'Architecte sort de l'ombre, puis son compas trace un cercle autour de la Machine. */
    public static final float ARCHITECT    = 5.4f;
    public static final float COMPASS      = 6.0f;
    public static final float LINE_ONE     = 7.0f;
    /** Les traits s'embrasent d'or. */
    public static final float IGNITE       = 8.4f;
    public static final float LINE_TWO     = 8.8f;
    public static final float COVER_START  = 10.4f;
    public static final float COVER_FULL   = 10.9f;

    private static final Color PAPER = hex("1e3c8a"), PAPER_DARK = hex("142a64"), GRID = hex("2e56b0");
    private static final Color CHALK = hex("e8f2ff"), GOLD = hex("ffc93a"), HOT = hex("fff6c8");

    private final TextureRegion tower, architect;
    private final BitmapFont    font;
    private final Array<Particle> sparks = new Array<>(false, 96);
    private boolean ignited;

    public ArchitectCutscene(CutsceneKit kit) {
        super(kit.settings(), kit.shake(), kit.sound("architect"));
        tower     = kit.chapterArt(Chapter.L_ASCENSION);
        architect = kit.portrait(EnemyKind.ARCHITECTE);
        font      = kit.font();
    }

    @Override protected float coverStart() { return COVER_START; }
    @Override protected float coverFull()  { return COVER_FULL; }
    @Override protected Color coverColor() { return Color.BLACK; }

    @Override
    protected void reset() {
        ignited = false;
        sparks.clear();
    }

    @Override
    protected void simulate(float delta) {
        if (!ignited && time >= IGNITE) {
            ignited = true;
            rumble(0.5f, 10f);
            for (int i = 0; i < 60; i++) {
                Particle spark = particle(towerX() + (random.nextFloat() - 0.5f) * towerWidth(0f) * 2f,
                    towerBottom() + random.nextFloat() * towerHeight(), 360f * random.nextFloat(),
                    120f + 260f * random.nextFloat(), 1.2f, 10f);
                spark.drag = 0.4f;
                spark.color.set(GOLD);
                sparks.add(spark);
            }
        }
        update(sparks, delta);
    }

    @Override
    protected void drawScene(Batch batch) {
        if (time < CLIMB_END) {
            drawClimb(batch);
            fadeFromBlack(batch, 0.5f);
            fadeThroughBlack(batch, CLIMB_END, 0.25f);
            return;
        }
        drawPaper(batch);
        drawTower(batch);
        drawMachine(batch);
        drawArchitect(batch);
        drawCompass(batch);
        drawParticles(batch, sparks);
        flash(batch, GOLD, IGNITE, 0.35f, 0.4f);
        float one = progress(LINE_ONE, LINE_ONE + 0.4f) * (1f - progress(IGNITE - 0.2f, IGNITE));
        float two = progress(LINE_TWO, LINE_TWO + 0.4f);
        speech(batch, Lang.t("Chaque étage, c'est moi qui l'ai posé."), one);
        speech(batch, Lang.t("Monte. Je t'attends en haut."), two);
        fadeThroughBlack(batch, CLIMB_END, 0.25f);
    }

    /** La montée : l'illustration de la Tour défile vers le bas, on grimpe vers sa lueur. */
    private void drawClimb(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        fill(batch, hex("0a0a24"), 1f);
        float zoom = Math.max(width / tower.getRegionWidth(), height / tower.getRegionHeight()) * 2.2f;
        float w = tower.getRegionWidth() * zoom, h = tower.getRegionHeight() * zoom;
        float climb = Interpolation.sine.apply(progress(0f, CLIMB_END));
        float y = MathUtils.lerp(-h * 0.05f, height - h * 0.95f, climb) - h * 0.05f;
        batch.setColor(Color.WHITE);
        batch.draw(tower, width / 2f - w / 2f, y, w, h);
        glow(batch, width / 2f, y + h * 0.95f, w * 0.4f, GOLD, 0.35f * climb);
    }

    /** Le plan bleu se déroule de gauche à droite, quadrillé. */
    private void drawPaper(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        fill(batch, Color.BLACK, 1f);
        float unroll = Interpolation.pow2Out.apply(progress(CLIMB_END, UNROLL_END));
        float paperW = width * unroll;
        batch.setColor(PAPER);
        batch.draw(pixel, 0f, 0f, paperW, height);
        batch.setColor(GRID);
        for (float x = 24f; x < paperW; x += 48f) batch.draw(pixel, x, 0f, 1.5f, height);
        for (float y = 24f; y < height; y += 48f) batch.draw(pixel, 0f, y, paperW, 1.5f);
        if (unroll < 1f) {                                                    // le rouleau, au bord qui avance
            batch.setColor(PAPER_DARK);
            batch.draw(pixel, paperW - 14f, 0f, 28f, height);
            batch.setColor(CHALK.r, CHALK.g, CHALK.b, 0.4f);
            batch.draw(pixel, paperW - 6f, 0f, 4f, height);
        }
    }

    /** La Tour dessinée au trait : ses deux murs montent avec chaque étage, puis son étage se ferme d'un trait. */
    private void drawTower(Batch batch) {
        Color ink = ink();
        for (int floor = 0; floor < FLOORS; floor++) {
            float at = FLOORS_START + floor * FLOOR_STEP;
            float k = progress(at, at + FLOOR_STEP);
            if (k <= 0f) break;
            float y0 = towerBottom() + floor * floorHeight(), y1 = y0 + floorHeight() * k;
            float half0 = towerWidth(floor / (float) FLOORS), half1 = towerWidth((floor + k) / FLOORS);
            line(batch, towerX() - half0, y0, towerX() - half1, y1, 4f, ink);
            line(batch, towerX() + half0, y0, towerX() + half1, y1, 4f, ink);
            if (k >= 1f) {
                line(batch, towerX() - half1, y1, towerX() + half1, y1, 3f, ink);
                float windowW = half1 * 0.25f;                                // deux fenêtres par étage
                for (int side = -1; side <= 1; side += 2) {
                    float wx = towerX() + side * half1 * 0.45f;
                    line(batch, wx - windowW / 2f, y0 + floorHeight() * 0.3f, wx - windowW / 2f, y0 + floorHeight() * 0.75f, 2f, ink);
                    line(batch, wx + windowW / 2f, y0 + floorHeight() * 0.3f, wx + windowW / 2f, y0 + floorHeight() * 0.75f, 2f, ink);
                }
            }
        }
    }

    /** La Machine, au sommet : un coffre à trois rouleaux et son levier. */
    private void drawMachine(Batch batch) {
        float k = progress(MACHINE, MACHINE + 0.6f);
        if (k <= 0f) return;
        Color ink = ink();
        float w = towerWidth(1f) * 2.4f, h = floorHeight() * 1.6f;
        float x0 = towerX() - w / 2f, y0 = towerTop();
        float alpha = k;
        tmp.set(ink.r, ink.g, ink.b, alpha);
        line(batch, x0, y0, x0 + w, y0, 4f, tmp);
        line(batch, x0, y0, x0, y0 + h, 4f, tmp);
        line(batch, x0 + w, y0, x0 + w, y0 + h, 4f, tmp);
        line(batch, x0, y0 + h, x0 + w / 2f, y0 + h * 1.35f, 4f, tmp);
        line(batch, x0 + w, y0 + h, x0 + w / 2f, y0 + h * 1.35f, 4f, tmp);
        for (int r = 0; r < 3; r++) {                                         // les trois rouleaux
            float rx = x0 + w * (0.14f + r * 0.27f), rw = w * 0.2f;
            line(batch, rx, y0 + h * 0.25f, rx + rw, y0 + h * 0.25f, 2f, tmp);
            line(batch, rx, y0 + h * 0.8f, rx + rw, y0 + h * 0.8f, 2f, tmp);
            line(batch, rx, y0 + h * 0.25f, rx, y0 + h * 0.8f, 2f, tmp);
            line(batch, rx + rw, y0 + h * 0.25f, rx + rw, y0 + h * 0.8f, 2f, tmp);
        }
        line(batch, x0 + w, y0 + h * 0.6f, x0 + w * 1.25f, y0 + h * 1.1f, 3f, tmp);   // le levier
        if (ignited) glow(batch, towerX(), y0 + h * 0.6f, w * 1.6f, GOLD, 0.35f + 0.1f * MathUtils.sin(time * 6f));
    }

    /** Le compas trace un cercle autour de la Machine. */
    private void drawCompass(Batch batch) {
        float k = progress(COMPASS, COMPASS + 0.9f);
        if (k <= 0f) return;
        Color ink = ink();
        float cx = towerX(), cy = towerTop() + floorHeight() * 0.9f, radius = towerWidth(1f) * 2.2f;
        int segments = 48, drawn = Math.round(segments * k);
        for (int i = 0; i < drawn; i++) {
            float a0 = 90f + i * 360f / segments, a1 = a0 + 360f / segments;
            line(batch, cx + MathUtils.cosDeg(a0) * radius, cy + MathUtils.sinDeg(a0) * radius,
                cx + MathUtils.cosDeg(a1) * radius, cy + MathUtils.sinDeg(a1) * radius, 3f, ink);
        }
        if (k < 1f) {                     // c'est SON compas (celui de sa main) qui trace : un fil d'or relie sa pointe au trait
            float a = 90f + 360f * k;
            float tipX = cx + MathUtils.cosDeg(a) * radius, tipY = cy + MathUtils.sinDeg(a) * radius;
            float fromX = architectX(COMPASS_TIP[0]), fromY = architectY(COMPASS_TIP[1]);
            line(batch, fromX, fromY, tipX, tipY, 2f, GOLD);
            glow(batch, tipX, tipY, 24f, GOLD, 0.7f);
            glow(batch, architectX(COMPASS_HINGE[0]), architectY(COMPASS_HINGE[1]), 6f * architectScale(), GOLD, 0.6f);
        }
    }

    /** En pixels de son portrait : ses yeux 2 x 2 (l'œil nu, puis celui du monocle), la charnière et une pointe de son compas. */
    private static final float[] EYE = {14f, 15f}, MONOCLE = {21f, 15f}, COMPASS_HINGE = {29.5f, 23.5f}, COMPASS_TIP = {26.5f, 14.5f};

    private float architectScale() { return worldHeight() * 0.62f / architect.getRegionHeight(); }
    private float architectX(float px) { return worldWidth() * 0.8f + (px - architect.getRegionWidth() / 2f) * architectScale(); }
    private float architectY(float py) { return -worldHeight() * 0.02f + (architect.getRegionHeight() - py) * architectScale(); }

    /** L'Architecte, à droite du plan : il sort de l'ombre, puis ses yeux et son monocle s'allument. */
    private void drawArchitect(Batch batch) {
        float k = progress(ARCHITECT, ARCHITECT + 0.8f);
        if (k <= 0f) return;
        float scale = architectScale();
        float x = worldWidth() * 0.8f, y = -worldHeight() * 0.02f;
        float shadow = 1f - progress(ARCHITECT + 0.4f, ARCHITECT + 1.2f);
        glow(batch, x, y + architect.getRegionHeight() * scale * 0.6f, architect.getRegionWidth() * scale * 1.6f,
            ignited ? GOLD : CHALK, 0.25f * k);
        portrait(batch, architect, x, y, scale, shadow, k);
        if (time >= COMPASS) {                                                 // ses yeux s'allument, le monocle plus fort
            float pulse = MathUtils.sin(time * 8f);
            glow(batch, architectX(EYE[0]), architectY(EYE[1]), 6f * scale, GOLD, 0.4f + 0.2f * pulse);
            glow(batch, architectX(MONOCLE[0]), architectY(MONOCLE[1]), 10f * scale, GOLD, 0.5f + 0.3f * pulse);
        }
    }

    /** Une phrase de l'Architecte, en bas de l'écran, sur un bandeau sombre. */
    private void speech(Batch batch, String text, float alpha) {
        if (alpha <= 0f) return;
        float width = worldWidth(), height = worldHeight();
        batch.setColor(0f, 0f, 0f, 0.55f * alpha);
        batch.draw(soft, width * 0.08f, height * 0.03f, width * 0.84f, height * 0.2f);
        font.getData().setScale(2.2f);
        caption(batch, font, text, width / 2f, height * 0.13f, alpha);
        font.getData().setScale(1f);
    }

    /** @return la couleur des traits : craie blanche, puis or quand ils s'embrasent. */
    private Color ink() {
        float k = progress(IGNITE, IGNITE + 0.4f);
        return tmp.set(CHALK).lerp(HOT, k).lerp(GOLD, k * 0.6f);
    }

    /** Un trait de {@code thick} pixels, de ({@code x0}, {@code y0}) à ({@code x1}, {@code y1}). */
    private void line(Batch batch, float x0, float y0, float x1, float y1, float thick, Color color) {
        float dx = x1 - x0, dy = y1 - y0, length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length <= 0f) return;
        batch.setColor(color);
        batch.draw(pixel, x0, y0 - thick / 2f, 0f, thick / 2f, length, thick, 1f, 1f, MathUtils.atan2(dy, dx) * MathUtils.radDeg);
    }

    private float towerX()      { return worldWidth() * 0.4f; }
    private float towerBottom() { return worldHeight() * 0.06f; }
    private float floorHeight() { return worldHeight() * 0.072f; }
    private float towerHeight() { return floorHeight() * FLOORS; }
    private float towerTop()    { return towerBottom() + towerHeight(); }
    /** @return la demi-largeur de la Tour à la hauteur {@code t} (0 en bas, 1 en haut). */
    private float towerWidth(float t) { return worldWidth() * MathUtils.lerp(0.12f, 0.05f, t); }
}
