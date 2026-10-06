package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.tower.Chapter;
import fr.astratime.lucky.i18n.Lang;

/**
 * Avant la Maison (chapitre 5) : un escalier de jetons monte à travers les
 * nuages ; on le gravit jusqu'à un manoir doré. Un majordome en ombre ouvre les
 * rideaux de la porte et s'incline. Les fenêtres sont des cartes, elles se
 * retournent une par une. La porte, une fente de machine à sous, s'ouvre. Une
 * pièce géante roule et y entre. Tout le manoir s'allume : « La Maison gagne
 * toujours. » Les cartes des fenêtres montrent toutes un As, et rient. Les
 * portes claquent. Fondu noir.
 */
public class HouseCutscene extends Cutscene {

    /** On gravit l'escalier de jetons (voir le son cutscene/house), puis on arrive devant le manoir. */
    public static final float STAIRS_END  = 1.5f;
    /** Le majordome ouvre les rideaux de la porte, puis s'incline. */
    public static final float CURTAINS    = 2.5f;
    public static final float BOW         = 3.0f;
    /** Les fenêtres se retournent une par une. */
    public static final float FLIP_START  = 3.5f;
    public static final float FLIP_STEP   = 0.13f;
    /** La porte s'ouvre. */
    public static final float DOOR_OPEN   = 5.2f;
    /** La pièce roule vers la porte, puis y entre. */
    public static final float COIN_START  = 5.7f;
    public static final float COIN_IN     = 6.8f;
    /** Le manoir s'allume ; la voix. */
    public static final float LIGHTS      = 6.9f;
    /** Les cartes des fenêtres montrent un As et rient. */
    public static final float ACES        = 8.0f;
    /** Les portes claquent. */
    public static final float SLAM        = 9.9f;
    public static final float COVER_START = 10.0f;
    public static final float COVER_FULL  = 10.5f;

    /** Les fenêtres-cartes de l'illustration (en pixels de l'image, origine en haut à gauche). */
    private static final float[][] WINDOWS = {{22.3f, 43f}, {30.4f, 43f}, {38.3f, 43f}, {90.9f, 43f},
        {98.5f, 43f}, {106.6f, 43f}, {22.3f, 55f}, {30.4f, 55f}, {38.3f, 55f}, {90.9f, 55f}, {98.5f, 55f},
        {106.6f, 55f}};
    /** La porte de l'illustration : centre et taille, en pixels de l'image. */
    private static final float DOOR_X = 64.5f, DOOR_Y = 53f, DOOR_W = 4.9f, DOOR_H = 18.7f;
    private static final Color GOLD = c("ffc93a"), HOT = c("fff6c8"), CURTAIN = c("8a1426"), CURTAIN_LIGHT = c("c8283c");
    /** Les couleurs des jetons de l'escalier. */
    private static final Color[] CHIPS = {c("c8283c"), c("2a5ad8"), c("1f9a4a"), c("1a1a24"), c("e8b020")};

    private final TextureRegion house, coin, butlerLegs, butlerTop, aceRed, aceBlack, cardBack;
    private final BitmapFont    font;
    private final Array<Particle> clouds = new Array<>(false, 32);
    private final Array<Particle> sparks = new Array<>(false, 64);
    private boolean entered, slammed;

    public HouseCutscene(CutsceneKit kit) {
        super(kit.settings(), kit.shake(), kit.sound("house"));
        house = kit.chapterArt(Chapter.LA_MAISON);
        coin  = load("hud/coin.png");
        font  = kit.font();
        // Les As des fenêtres : un grand A au centre, cerné de noir pour trancher sur le manoir doré.
        String[] ace = {
            "oooooooooo",
            "owwwwwwwwo",
            "owrwwwwwwo",
            "owwwwwwwwo",
            "owwwrrwwwo",
            "owwrwwrwwo",
            "owwrwwrwwo",
            "owwrrrrwwo",
            "owwrwwrwwo",
            "owwrwwrwwo",
            "owwwwwwwwo",
            "owwwwwwrwo",
            "owwwwwwwwo",
            "oooooooooo"};
        aceRed   = art("owr", new Color[] {c("1a0a10"), c("f4ecdc"), c("c8283c")}, ace);
        aceBlack = art("owr", new Color[] {c("1a0a10"), c("f4ecdc"), c("1a1a24")}, ace);
        cardBack = art("owr", new Color[] {c("1a0a10"), c("8a1424"), c("c8283c")},
            "oooooooooo",
            "owwwwwwwwo",
            "owrwrwrwwo",
            "owwrwrwrwo",
            "owrwrwrwwo",
            "owwrwrwrwo",
            "owrwrwrwwo",
            "owwrwrwrwo",
            "owrwrwrwwo",
            "owwrwrwrwo",
            "owrwrwrwwo",
            "owwrwrwrwo",
            "owwwwwwwwo",
            "oooooooooo");
        Color[] shade = {c("05030a"), c("2a2030")};
        butlerTop = art("ow", shade,
            "...ooo...",
            "..ooooo..",
            "..ooooo..",
            "...ooo...",
            "..ooooo..",
            ".ooowooo.",
            "oooowoooo",
            "oooowoooo",
            "oo.owo.oo",
            "oo.ooo.oo",
            "oo.ooo.oo",
            "...ooo...");
        butlerLegs = art("o", new Color[] {c("05030a")},
            "..ooooo..",
            ".ooooooo.",
            ".ooo.ooo.",
            ".oo...oo.",
            ".oo...oo.",
            ".oo...oo.",
            ".oo...oo.",
            "ooo...ooo");
    }

    @Override protected float coverStart() { return COVER_START; }
    @Override protected float coverFull()  { return COVER_FULL; }
    @Override protected Color coverColor() { return Color.BLACK; }

    @Override
    protected void reset() {
        entered = slammed = false;
        clouds.clear();
        sparks.clear();
        for (int i = 0; i < 14; i++) {                  // nuages au premier plan, qui dérivent
            Particle cloud = particle(worldWidth() * (random.nextFloat() * 1.4f - 0.2f),
                worldHeight() * (0.02f + 0.16f * random.nextFloat()), 0f, 20f + 40f * random.nextFloat(), 99f,
                260f + 260f * random.nextFloat());
            cloud.color.set(1f, 0.92f, 0.95f, 0.5f);
            clouds.add(cloud);
        }
    }

    @Override
    protected void simulate(float delta) {
        if (!entered && time >= COIN_IN) {
            entered = true;
            rumble(0.25f, 5f);
            for (int i = 0; i < 24; i++) {
                Particle spark = particle(doorX(), doorY(), 360f * random.nextFloat(), 200f + 300f * random.nextFloat(),
                    0.6f, 14f);
                spark.drag = 0.1f;
                spark.color.set(GOLD);
                sparks.add(spark);
            }
        }
        if (!slammed && time >= SLAM) {
            slammed = true;
            rumble(0.4f, 14f);
        }
        for (Particle cloud : clouds) cloud.x += cloud.vx * delta;
        update(sparks, delta);
    }

    @Override
    protected void drawScene(Batch batch) {
        if (time < STAIRS_END) {
            drawStairs(batch);
            fadeFromBlack(batch, 0.6f);
            fadeThroughBlack(batch, STAIRS_END, 0.25f);
            return;
        }
        float width = worldWidth(), height = worldHeight();
        fill(batch, c("1a1640"), 1f);
        // Le manoir : on s'approche lentement de sa porte.
        float zoom = artZoom();
        float lights = progress(LIGHTS, LIGHTS + 0.3f);
        float bright = 0.85f + 0.15f * lights;
        batch.setColor(Math.min(1f, bright), Math.min(1f, bright * 0.97f), Math.min(1f, bright * 0.9f), 1f);
        batch.draw(house, artX(0f), artY(80f), house.getRegionWidth() * zoom, house.getRegionHeight() * zoom);
        if (lights > 0f) glow(batch, doorX(), doorY() + 10f * zoom, 90f * zoom, GOLD, 0.25f * lights);

        // Les fenêtres-cartes se retournent une par une, puis brillent.
        for (int i = 0; i < WINDOWS.length; i++) {
            float at = FLIP_START + i * FLIP_STEP;
            if (time < at) continue;
            float flip = progress(at, at + 0.25f);
            float x = artX(WINDOWS[i][0]), y = artY(WINDOWS[i][1]);
            float w = 5f * zoom * Math.abs(MathUtils.cos(flip * MathUtils.PI)), h = 7f * zoom;
            if (flip < 1f) {
                batch.setColor(flip < 0.5f ? c("f4ecdc") : c("ffe9a0"));
                batch.draw(pixel, x - w / 2f, y - h / 2f, w, h);
            }
            glow(batch, x, y, 14f * zoom, flip < 1f ? HOT : GOLD, (flip < 1f ? 0.5f : 0.2f) + 0.2f * lights);
        }
        drawAces(batch, zoom);

        drawDoor(batch);
        drawButler(batch, zoom);
        drawCoin(batch);
        drawParticles(batch, sparks);

        // Nuages devant le manoir.
        for (Particle cloud : clouds) {
            batch.setColor(cloud.color);
            batch.draw(soft, cloud.x - cloud.size / 2f, cloud.y - cloud.size * 0.3f, cloud.size, cloud.size * 0.6f);
        }

        // La voix de la Maison, puis le rire des As.
        if (time >= ACES + 0.3f) {
            font.getData().setScale(3f);
            for (int i = 0; i < 6; i++) {
                float at = ACES + 0.3f + i * 0.22f;
                float k = progress(at, at + 0.15f) * (1f - progress(at + 0.5f, at + 0.7f));
                float x = artX(WINDOWS[i * 2][0]) + (i % 2 == 0 ? -1f : 1f) * 6f * zoom;
                caption(batch, font, Lang.t("HA !"), x, artY(WINDOWS[i * 2][1]) + 6f * zoom + k * 10f, k);
            }
            font.getData().setScale(1f);
        }
        if (time >= LIGHTS + 0.2f) {
            float alpha = progress(LIGHTS + 0.2f, LIGHTS + 0.6f);
            batch.setColor(0f, 0f, 0f, 0.55f * alpha);
            batch.draw(soft, width * 0.1f, height * 0.04f, width * 0.8f, height * 0.22f);
            font.getData().setScale(2.4f);
            caption(batch, font, Lang.t("La Maison gagne toujours."), width / 2f, height * 0.15f, alpha);
            font.getData().setScale(1f);
        }
        if (slammed && !settings.isReducedEffects()) {
            float dark = 1f - (time - SLAM) / 0.2f;
            if (dark > 0f) fill(batch, Color.BLACK, 0.5f * dark);
        }
        fadeThroughBlack(batch, STAIRS_END, 0.25f);
    }

    /**
     * L'escalier de jetons : des piles de jetons de casino qui montent en
     * diagonale à travers les nuages ; on grimpe, le manoir apparaît en haut.
     */
    private void drawStairs(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, c("6a5aa0"), c("1a1640"));
        float climb = Interpolation.sine.apply(progress(0f, STAIRS_END));
        // Le manoir, au loin, en haut des marches.
        float zoom = height * 0.32f / house.getRegionHeight() * (1f + 0.4f * climb);
        float hw = house.getRegionWidth() * zoom, hh = house.getRegionHeight() * zoom;
        float hy = height - hh * MathUtils.lerp(0.55f, 0.95f, climb);
        glow(batch, width * 0.7f, hy + hh * 0.4f, hw * 1.3f, GOLD, 0.25f);
        batch.setColor(0.9f, 0.85f, 0.85f, 1f);
        batch.draw(house, width * 0.7f - hw / 2f, hy, hw, hh);
        // Les marches de jetons, de bas en haut ; la caméra monte avec nous.
        float stepW = width * 0.12f, stepH = height * 0.11f, chip = stepH / 5f;
        float shift = climb * 4f;
        for (int i = 0; i < 12; i++) {
            float sx = width * 0.02f + (i - shift) * stepW * 0.75f, sy = -height * 0.05f + (i - shift) * stepH;
            if (sy > hy + hh * 0.2f) break;
            for (int k = 0; k < 5; k++) {                              // une pile de jetons
                Color color = CHIPS[(i * 3 + k) % CHIPS.length];
                float y = sy + k * chip;
                batch.setColor(0f, 0f, 0f, 0.6f);
                batch.draw(pixel, sx - 2f, y - 2f, stepW + 4f, chip + 2f);
                batch.setColor(color);
                batch.draw(pixel, sx, y, stepW, chip - 2f);
                batch.setColor(1f, 1f, 1f, 0.85f);                    // les stries blanches du jeton
                for (int s = 0; s < 4; s++) batch.draw(pixel, sx + (0.1f + s * 0.25f) * stepW, y + 2f, stepW * 0.08f, chip - 6f);
                batch.setColor(1f, 1f, 1f, 0.2f);
                batch.draw(pixel, sx, y + chip - 5f, stepW, 3f);
            }
        }
        for (Particle cloud : clouds) {                               // les nuages défilent vers le bas
            float y = (cloud.y + height * 0.6f - climb * height * 1.4f) % (height * 1.4f);
            if (y < -height * 0.3f) y += height * 1.4f;
            batch.setColor(1f, 0.92f, 0.95f, 0.45f);
            batch.draw(soft, cloud.x - cloud.size / 2f, y - cloud.size * 0.3f, cloud.size * 1.3f, cloud.size * 0.6f);
        }
    }

    /** Le majordome en ombre : il ouvre les rideaux rouges de la porte, puis s'incline et s'efface. */
    private void drawButler(Batch batch, float zoom) {
        if (time < STAIRS_END || time >= LIGHTS + 0.3f) return;
        float open = Interpolation.pow2Out.apply(progress(CURTAINS, CURTAINS + 0.45f));
        float w = DOOR_W * zoom, h = DOOR_H * zoom, dx = doorX(), dy = doorY();
        if (time < DOOR_OPEN + 0.3f) {                                   // les deux rideaux s'écartent
            float half = (w / 2f + 0.6f * zoom) * (1f - 0.8f * open);
            for (int side = -1; side <= 1; side += 2) {
                float x = side < 0 ? dx - w / 2f - 0.6f * zoom : dx + w / 2f + 0.6f * zoom - half;
                batch.setColor(CURTAIN);
                batch.draw(pixel, x, dy - h / 2f, half, h + 0.8f * zoom);
                batch.setColor(CURTAIN_LIGHT);
                for (int f = 1; f < 3; f++) batch.draw(pixel, x + half * f / 3f, dy - h / 2f, 0.4f * zoom, h);
            }
            batch.setColor(GOLD);
            batch.draw(pixel, dx - w / 2f - 0.8f * zoom, dy + h / 2f, w + 1.6f * zoom, 0.8f * zoom);
        }
        // Le majordome, à gauche de la porte : il s'incline à {@link #BOW}.
        float fade = 1f - progress(LIGHTS - 0.2f, LIGHTS + 0.3f);
        float bow = Interpolation.sine.apply(progress(BOW, BOW + 0.3f)) * (1f - progress(BOW + 0.7f, BOW + 1.0f));
        float scale = h * 0.9f / (butlerLegs.getRegionHeight() + butlerTop.getRegionHeight());
        float bx = dx - w / 2f - 6f * zoom, by = dy - h / 2f;
        float legsH = butlerLegs.getRegionHeight() * scale, topW = butlerTop.getRegionWidth() * scale;
        batch.setColor(1f, 1f, 1f, fade);
        batch.draw(butlerLegs, bx - butlerLegs.getRegionWidth() * scale / 2f, by, butlerLegs.getRegionWidth() * scale, legsH);
        batch.draw(butlerTop, bx - topW / 2f, by + legsH - scale, topW / 2f, scale, topW,
            butlerTop.getRegionHeight() * scale, 1f, 1f, -55f * bow);
    }

    /** Les cartes des fenêtres se retournent sur un As, et rient : elles sautillent. */
    private void drawAces(Batch batch, float zoom) {
        if (time < ACES) return;
        for (int i = 0; i < WINDOWS.length; i++) {
            float at = ACES + (i % 6) * 0.05f;
            float flip = progress(at, at + 0.2f);
            float laugh = time > at + 0.3f ? Math.abs(MathUtils.sin((time - at) * 14f + i)) * 1.2f * zoom : 0f;
            float x = artX(WINDOWS[i][0]), y = artY(WINDOWS[i][1]) + laugh;
            // La carte se retourne : le dos rouge se referme, puis l'As s'ouvre.
            float turn = MathUtils.cos(flip * MathUtils.PI);
            float w = 5f * zoom * Math.abs(turn), h = 7f * zoom;
            batch.setColor(0f, 0f, 0f, 0.45f);                               // une ombre, pour détacher la carte
            batch.draw(soft, x - 4.5f * zoom, y - 5.5f * zoom, 9f * zoom, 11f * zoom);
            batch.setColor(Color.WHITE);
            batch.draw(turn > 0f ? cardBack : i % 2 == 0 ? aceRed : aceBlack, x - w / 2f, y - h / 2f, w, h);
        }
    }

    /** La porte-fente : elle s'ouvre sur de la lumière, puis claque. */
    private void drawDoor(Batch batch) {
        if (time < DOOR_OPEN) return;
        float open = Interpolation.pow2Out.apply(progress(DOOR_OPEN, DOOR_OPEN + 0.4f));
        if (slammed) open *= Math.max(0f, 1f - (time - SLAM) / 0.08f);
        float zoom = artZoom();
        float w = DOOR_W * zoom * open, h = DOOR_H * zoom;
        glow(batch, doorX(), doorY(), (40f + 60f * open) * zoom, HOT, 0.6f * open);
        batch.setColor(HOT.r, HOT.g, HOT.b, open);
        batch.draw(pixel, doorX() - w / 2f, doorY() - h / 2f, w, h);
    }

    /** La pièce géante : elle roule depuis le bas à gauche, rapetisse au loin, et entre dans la fente. */
    private void drawCoin(Batch batch) {
        if (time < COIN_START || time >= COIN_IN) return;
        float k = progress(COIN_START, COIN_IN);
        float e = Interpolation.pow2In.apply(k) * 0.5f + k * 0.5f;
        float x = MathUtils.lerp(worldWidth() * 0.12f, doorX(), e);
        float y = MathUtils.lerp(worldHeight() * 0.2f, doorY(), e) + MathUtils.sin(k * MathUtils.PI) * worldHeight() * 0.12f;
        float size = MathUtils.lerp(11f, 0.6f, e);
        float squash = Math.abs(MathUtils.cos(k * 14f));             // la pièce tourne sur elle-même
        glow(batch, x, y, coin.getRegionWidth() * size * 2f, GOLD, 0.4f);
        batch.setColor(Color.WHITE);
        float w = coin.getRegionWidth() * size, h = coin.getRegionHeight() * size;
        batch.draw(coin, x - w * squash / 2f, y - h / 2f, w * Math.max(0.12f, squash), h);
    }

    private float artZoom() {
        float base = Math.max(worldWidth() / house.getRegionWidth(), worldHeight() / house.getRegionHeight()) * 1.04f;
        return base * MathUtils.lerp(1f, 1.35f, Interpolation.pow2.apply(progress(STAIRS_END, COVER_FULL)));
    }

    /** @return l'abscisse à l'écran de la colonne {@code px} de l'illustration (la porte au centre). */
    private float artX(float px) { return worldWidth() / 2f + (px - DOOR_X) * artZoom(); }

    /** @return l'ordonnée à l'écran de la ligne {@code py} de l'illustration (origine en haut). */
    private float artY(float py) { return -10f + (house.getRegionHeight() - py) * artZoom(); }

    private float doorX() { return artX(DOOR_X); }
    private float doorY() { return artY(DOOR_Y); }
}
