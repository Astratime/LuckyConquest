package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.assets.StoryPortraits;
import fr.astratime.lucky.entities.tower.Chapter;
import fr.astratime.lucky.i18n.Lang;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Avant la Maison (chapitre 5) : un escalier de marbre à nez d'or, incrusté
 * de jetons, monte à travers les nuages ; on le gravit jusqu'à un manoir doré. Un majordome en ombre ouvre les
 * rideaux de la porte et s'incline. Les fenêtres sont des cartes, elles se
 * retournent une par une. La porte, une fente de machine à sous, s'ouvre. Une
 * pièce géante roule et y entre. Tout le manoir s'allume : « La Maison gagne
 * toujours. » Les cartes des fenêtres montrent toutes un As, et rient. Les
 * portes claquent. Fondu noir.
 */
public class HouseCutscene extends Cutscene {

    /** On gravit l'escalier du ciel (voir le son cutscene/house), puis on arrive devant le manoir. */
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
    private static final Color GOLD = hex("ffc93a"), HOT = hex("fff6c8"), CURTAIN = hex("8a1426"), CURTAIN_LIGHT = hex("c8283c");
    /** Les couleurs des jetons incrustés dans les contremarches. */
    private static final Color[] CHIPS = {hex("c8283c"), hex("2a5ad8"), hex("1f9a4a"), hex("1a1a24"), hex("e8b020")};
    /** Le marbre et l'or de l'escalier, et le ciel du soir de l'illustration (du haut vers le bas). */
    private static final Color MARBLE = hex("f4eef6"), MARBLE_LIGHT = hex("ffffff"), MARBLE_VEIN = hex("d4c6e2"),
        RISER = hex("d2c4e0"), RISER_DARK = hex("9a88b4"), EDGE = hex("5a4a7a"), GOLD_DARK = hex("b8801f");
    private static final Color[] SKY = {hex("1a1a4a"), hex("2a225a"), hex("4a2a6a"), hex("7a3a7a"), hex("b85a7a"),
        hex("e88a6a"), hex("ffb46a")};
    /** L'escalier en perspective : marches (profondeur, hauteur), demi-largeur, hauteur des yeux, première marche. */
    private static final int STEPS = 11;
    private static final float RUN = 1f, RISE = 0.55f, HALF = 2.0f, EYE = 3.2f, NEAR = 1.2f;

    private final TextureRegion house, skyless, coin, butlerLegs, butlerTop, aceRed, aceBlack, cardBack;
    private final BitmapFont    font;
    private final Array<Particle> clouds = new Array<>(false, 32);
    private final Array<Particle> sparks = new Array<>(false, 64);
    private boolean entered, slammed;
    /** La taille d'un gros pixel de l'escalier, sa ligne d'horizon, sa focale (en unités du monde). */
    private float dot = 5f, horizon, focal;
    private final Color shadeTmp = new Color();

    public HouseCutscene(CutsceneKit kit) {
        super(kit.settings(), kit.shake(), kit.sound("house"));
        house = kit.chapterArt(Chapter.LA_MAISON);
        skyless = pixels(skyless(StoryPortraits.houseArt()));
        coin  = load("hud/coin.png");
        font  = kit.font();
        // Les As des fenêtres : les vraies cartes du jeu (As de Coeur, As de Pique, et leur dos).
        aceRed   = gameCard("cards/light/1-H.png");
        aceBlack = gameCard("cards/light/1-P.png");
        cardBack = gameCard("cards/light/BACK.png");
        Color[] shade = {hex("05030a"), hex("2a2030")};
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
        butlerLegs = art("o", new Color[] {hex("05030a")},
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
        fill(batch, hex("1a1640"), 1f);
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
                batch.setColor(flip < 0.5f ? hex("f4ecdc") : hex("ffe9a0"));
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
     * L'escalier du ciel : des marches de marbre blanc à nez d'or, incrustées de jetons de casino, bordées de
     * balustres, qui montent en perspective à travers les nuages jusqu'au manoir ; on les gravit (la caméra monte
     * de trois marches). Dessiné en gros pixels, comme l'illustration du manoir.
     */
    private void drawStairs(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        dot = Math.max(3f, Math.round(height / 180f));
        for (int i = 0; i < SKY.length; i++) {                         // le ciel du soir de l'illustration, en bandes
            float band = (height + 2f * MARGIN) / SKY.length;
            batch.setColor(SKY[SKY.length - 1 - i]);
            batch.draw(pixel, -MARGIN, -MARGIN + i * band - 1f, width + 2f * MARGIN, band + 2f);
        }
        for (int i = 0; i < 30; i++) {
            float x = (i * 211.7f) % width, y = height * (0.7f + 0.28f * ((i * 37) % 100) / 100f);
            batch.setColor(hex("e8e0ff"));
            batch.draw(pixel, snap(x), snap(y), dot, dot);
        }
        float climb = Interpolation.sine.apply(progress(0f, STAIRS_END));
        horizon = height * 0.36f;
        focal = height * 0.67f;
        float scroll = climb * 3f;
        // Le manoir, posé sur le palier du haut, au loin.
        float zTop = NEAR + (STEPS - scroll) * RUN, yTop = (STEPS - scroll) * RISE - EYE;
        float zHouse = zTop + 1.2f, scale = focal * 7.6f / zHouse / skyless.getRegionHeight();
        float hw = skyless.getRegionWidth() * scale, hh = skyless.getRegionHeight() * scale;
        float base = screenY(zHouse, yTop) - hh * 0.12f;
        glow(batch, width / 2f, base + hh * 0.45f, hw * 1.5f, hex("e89a6a"), 0.55f);
        glow(batch, width / 2f, base + hh * 0.45f, hw * 0.8f, GOLD, 0.3f);
        batch.setColor(Color.WHITE);
        batch.draw(skyless, width / 2f - hw / 2f, base, hw, hh);
        // La mer de nuages, de chaque côté de l'escalier.
        for (int i = 0; i < 12; i++) {
            float x = width * (i - 0.5f) / 10f, y = horizon - height * (0.06f + 0.05f * (i % 3)), r = width * (0.12f + 0.03f * (i % 2));
            batch.setColor(hex("c8a0c0"));
            batch.draw(soft, x - r, y - r * 0.45f - 10f, r * 2f, r * 0.9f);
            batch.setColor(hex("f0c8d0"));
            batch.draw(soft, x - r, y - r * 0.45f, r * 2f, r * 0.9f);
        }
        batch.setColor(hex("f0c8d0"));
        batch.draw(pixel, -MARGIN, -MARGIN, width + 2f * MARGIN, horizon - height * 0.12f + MARGIN);
        // Les marches, de la plus lointaine à la plus proche.
        for (int i = STEPS - 1; i >= 0; i--) {
            float u = i - scroll, z = NEAR + u * RUN, y = (u + 1f) * RISE - EYE;
            if (z < 0.25f) continue;
            step(batch, i, z, y);
        }
        for (Particle cloud : clouds) {                               // les nuages défilent vers le bas
            float y = (cloud.y + height * 0.6f - climb * height * 1.4f) % (height * 1.4f);
            if (y < -height * 0.3f) y += height * 1.4f;
            batch.setColor(1f, 0.92f, 0.95f, 0.4f);
            batch.draw(soft, cloud.x - cloud.size / 2f, y - cloud.size * 0.3f, cloud.size * 1.3f, cloud.size * 0.6f);
        }
    }

    /** Une marche : sa contremarche (nez d'or, jetons incrustés), son giron de marbre veiné, ses deux balustres. */
    private void step(Batch batch, int index, float z, float y) {
        float cx = worldWidth() / 2f, half = focal * HALF / z;
        float x0 = snap(cx - half), x1 = snap(cx + half);
        // Le giron, s'il est sous nos yeux : un trapèze de marbre qui file vers le palier suivant.
        if (y < 0f) {
            float front = snap(screenY(z, y)), back = snap(screenY(z + RUN, y));
            for (float row = front; row < back; row += dot) {
                float k = (row + dot / 2f - horizon) / (front - horizon);          // 1 au bord avant, plus petit au fond
                float rowHalf = half * k;
                float a = snap(cx - rowHalf), b = snap(cx + rowHalf);
                float depth = (row - front) / Math.max(dot, back - front);           // le fond du giron, dans l'ombre de la marche suivante
                fillRect(batch, row == front ? MARBLE_LIGHT : shadeTmp.set(MARBLE).lerp(RISER, 0.6f * depth), a, row, b - a, dot);
                fillRect(batch, EDGE, a, row, dot, dot);
                fillRect(batch, EDGE, b - dot, row, dot, dot);
            }
            for (int v = 0; v < 2; v++) {                                          // les veines du marbre
                long seed = index * 31L + v * 7L;
                float va = 0.15f + 0.7f * hash(seed), vb = 0.15f + 0.6f * hash(seed + 3);
                for (int p = 0; p < 6; p++) {
                    float along = (va + p * 0.05f * (hash(seed + 11) < 0.5f ? 1f : -1f)) * 2f - 1f;
                    float depth = Math.min(0.95f, vb + p * 0.05f), zz = z + RUN * depth;
                    float sx = cx + along * focal * HALF / zz, sy = screenY(zz, y);
                    if (snap(sy) >= back || snap(sy) <= front) continue;
                    fillRect(batch, MARBLE_VEIN, snap(sx), snap(sy), dot, dot);
                }
            }
        }
        // La contremarche : de la hauteur du giron d'en dessous à celle de ce giron.
        float bottom = snap(screenY(z, y - RISE)), top = snap(screenY(z, y));
        int rows = Math.round((top - bottom) / dot);
        if (rows >= 1) {
            fillRect(batch, RISER, x0, bottom, x1 - x0, top - bottom);
            fillRect(batch, RISER_DARK, x0, bottom, x1 - x0, dot);                 // l'ombre au pied
            if (rows >= 3) fillRect(batch, GOLD_DARK, x0, top - 2f * dot, x1 - x0, dot);
            fillRect(batch, GOLD, x0, top - dot, x1 - x0, dot);                    // le nez d'or de la marche
            fillRect(batch, EDGE, x0, bottom, dot, top - bottom);
            fillRect(batch, EDGE, x1 - dot, bottom, dot, top - bottom);
            if (rows >= 6) {                                                       // une rangée de jetons incrustés
                int size = Math.max(3, Math.min(7, (rows - 3) / 2 * 2 + 1));
                float chip = size * dot, cy = snap((bottom + dot + top - 2f * dot) / 2f - chip / 2f);
                int count = Math.max(3, (int) ((x1 - x0) / (chip * 2.2f)));
                float gap = (x1 - x0) / count;
                for (int c = 0; c < count; c++) chipAt(batch, snap(x0 + gap * (c + 0.5f) - chip / 2f), cy, size, CHIPS[(index + c) % CHIPS.length]);
            } else if (rows >= 4) {
                fillRect(batch, CHIPS[index % CHIPS.length], x0 + dot, snap((bottom + top) / 2f - dot), x1 - x0 - 2f * dot, dot);
            }
        }
        // Les balustres : un pilier de marbre coiffé d'or à chaque bout, reliés par une rampe d'or.
        if (y < 0.6f) {
            for (int side = -1; side <= 1; side += 2) {
                float postW = Math.max(dot, snap(focal * 0.16f / z)), postH = snap(focal * 0.85f / z);
                float px = side < 0 ? x0 : x1 - postW, py = top;
                fillRect(batch, MARBLE, px, py, postW, postH);
                if (postW > dot) fillRect(batch, RISER_DARK, px + postW - dot, py, dot, postH);
                fillRect(batch, GOLD, px - (postW > dot ? dot : 0f), py + postH, postW + (postW > dot ? 2f * dot : 0f), dot);
                fillRect(batch, GOLD_DARK, px, py, postW, dot);
                float nz = z + RUN, ny = y + RISE, nhalf = focal * HALF / nz;               // la rampe, vers le balustre suivant
                float nx = side < 0 ? snap(worldWidth() / 2f - nhalf) : snap(worldWidth() / 2f + nhalf) - Math.max(dot, snap(focal * 0.16f / nz));
                float ntop = snap(screenY(nz, ny)) + snap(focal * 0.85f / nz);
                float sx = px + postW / 2f, sy = py + postH, ex = nx + Math.max(dot, snap(focal * 0.16f / nz)) / 2f, ey = ntop;
                int n = Math.max(2, (int) (Math.max(Math.abs(ex - sx), Math.abs(ey - sy)) / dot));
                for (int s = 0; s <= n; s++) {
                    float t = s / (float) n;
                    fillRect(batch, GOLD, snap(MathUtils.lerp(sx, ex, t) - dot / 2f), snap(MathUtils.lerp(sy, ey, t)), dot, dot);
                    fillRect(batch, GOLD_DARK, snap(MathUtils.lerp(sx, ex, t) - dot / 2f), snap(MathUtils.lerp(sy, ey, t)) - dot, dot, dot);
                }
            }
        }
    }

    /** Un jeton de casino incrusté, rond, de {@code size} gros pixels, à rayures blanches et cœur blanc. */
    private void chipAt(Batch batch, float x, float y, int size, Color color) {
        float c = (size - 1) / 2f;
        for (int py = 0; py < size; py++) {
            for (int px = 0; px < size; px++) {
                float dx = px - c, dy = py - c, d = dx * dx + dy * dy;
                if (d > (c + 0.5f) * (c + 0.5f)) continue;
                Color shade = d > (c - 0.5f) * (c - 0.5f) ? (px == 0 || py == 0 || px == size - 1 || py == size - 1) && (px + py) % 2 == 0 ? Color.WHITE : color
                    : d <= 0.6f ? Color.WHITE : color;
                fillRect(batch, shade, x + px * dot, y + py * dot, dot, dot);
            }
        }
        fillRect(batch, EDGE, x + c * dot, y - dot, dot, dot);
    }

    private void fillRect(Batch batch, Color color, float x, float y, float w, float h) {
        batch.setColor(color);
        batch.draw(pixel, x, y, w, h);
    }

    /** @return {@code v} posé sur la grille des gros pixels de l'escalier. */
    private float snap(float v) { return Math.round(v / dot) * dot; }

    /** @return l'ordonnée à l'écran d'un point à la profondeur {@code z} et à la hauteur {@code y} (sous nos yeux : négative). */
    private float screenY(float z, float y) { return horizon + focal * y / z; }

    private static float hash(long seed) {
        long h = seed * 0x9E3779B97F4A7C15L;
        h ^= h >>> 29;
        return (h & 0xFFFF) / 65536f;
    }

    /** @return l'illustration du manoir sans son ciel (il flotte sur ses nuages, au sommet de l'escalier). */
    private static int[][] skyless(int[][] art) {
        java.util.Set<Integer> sky = new java.util.HashSet<>();
        for (Color color : SKY) sky.add(Color.rgba8888(color));
        for (String c : new String[] {"e8e0ff", "c87a6a", "e89a6a"}) sky.add(Color.rgba8888(hex(c)));
        int[][] out = new int[art.length][];
        for (int y = 0; y < art.length; y++) {
            out[y] = art[y].clone();
            for (int x = 0; x < out[y].length; x++) if (sky.contains(out[y][x])) out[y][x] = 0;
        }
        return out;
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
