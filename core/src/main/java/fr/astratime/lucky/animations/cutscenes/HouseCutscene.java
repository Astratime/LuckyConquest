package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.tower.Chapter;

/**
 * Avant la Maison (chapitre 5) : au-dessus des nuages, un manoir doré. Ses
 * fenêtres sont des cartes, elles se retournent une par une. La porte, une fente
 * de machine à sous, s'ouvre. Une pièce géante roule et y entre. Tout le manoir
 * s'allume : « La Maison gagne toujours. » Les portes claquent. Fondu noir.
 */
public class HouseCutscene extends Cutscene {

    /** Les fenêtres se retournent une par une (voir le son cutscene/house). */
    public static final float FLIP_START  = 1.0f;
    public static final float FLIP_STEP   = 0.13f;
    /** La porte s'ouvre. */
    public static final float DOOR_OPEN   = 2.7f;
    /** La pièce roule vers la porte, puis y entre. */
    public static final float COIN_START  = 3.2f;
    public static final float COIN_IN     = 4.3f;
    /** Le manoir s'allume ; la voix. */
    public static final float LIGHTS      = 4.4f;
    /** Les portes claquent. */
    public static final float SLAM        = 5.9f;
    public static final float COVER_START = 6.0f;
    public static final float COVER_FULL  = 6.5f;

    /** Les fenêtres-cartes de l'illustration (en pixels de l'image, origine en haut à gauche). */
    private static final float[][] WINDOWS = {{22.3f, 43f}, {30.4f, 43f}, {38.3f, 43f}, {90.9f, 43f},
        {98.5f, 43f}, {106.6f, 43f}, {22.3f, 55f}, {30.4f, 55f}, {38.3f, 55f}, {90.9f, 55f}, {98.5f, 55f},
        {106.6f, 55f}};
    /** La porte de l'illustration : centre et taille, en pixels de l'image. */
    private static final float DOOR_X = 64.5f, DOOR_Y = 53f, DOOR_W = 4.9f, DOOR_H = 18.7f;
    private static final Color GOLD = c("ffc93a"), HOT = c("fff6c8");

    private final TextureRegion house, coin;
    private final BitmapFont    font;
    private final Array<Particle> clouds = new Array<>(false, 32);
    private final Array<Particle> sparks = new Array<>(false, 64);
    private boolean entered, slammed;

    public HouseCutscene(CutsceneKit kit) {
        super(kit.settings(), kit.shake(), kit.sound("house"));
        house = kit.chapterArt(Chapter.LA_MAISON);
        coin  = load("hud/coin.png");
        font  = kit.font();
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

        drawDoor(batch);
        drawCoin(batch);
        drawParticles(batch, sparks);

        // Nuages devant le manoir.
        for (Particle cloud : clouds) {
            batch.setColor(cloud.color);
            batch.draw(soft, cloud.x - cloud.size / 2f, cloud.y - cloud.size * 0.3f, cloud.size, cloud.size * 0.6f);
        }

        // La voix de la Maison.
        if (time >= LIGHTS + 0.2f) {
            float alpha = progress(LIGHTS + 0.2f, LIGHTS + 0.6f);
            batch.setColor(0f, 0f, 0f, 0.55f * alpha);
            batch.draw(soft, width * 0.1f, height * 0.04f, width * 0.8f, height * 0.22f);
            font.getData().setScale(2.4f);
            caption(batch, font, "La Maison gagne toujours.", width / 2f, height * 0.15f, alpha);
            font.getData().setScale(1f);
        }
        if (slammed && !settings.isReducedEffects()) {
            float dark = 1f - (time - SLAM) / 0.2f;
            if (dark > 0f) fill(batch, Color.BLACK, 0.5f * dark);
        }
        fadeFromBlack(batch, 0.8f);
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
        return base * MathUtils.lerp(1f, 1.35f, Interpolation.pow2.apply(progress(0f, COVER_FULL)));
    }

    /** @return l'abscisse à l'écran de la colonne {@code px} de l'illustration (la porte au centre). */
    private float artX(float px) { return worldWidth() / 2f + (px - DOOR_X) * artZoom(); }

    /** @return l'ordonnée à l'écran de la ligne {@code py} de l'illustration (origine en haut). */
    private float artY(float py) { return -10f + (house.getRegionHeight() - py) * artZoom(); }

    private float doorX() { return artX(DOOR_X); }
    private float doorY() { return artY(DOOR_Y); }
}
