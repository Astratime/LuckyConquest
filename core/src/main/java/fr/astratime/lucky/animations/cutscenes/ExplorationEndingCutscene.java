package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.i18n.Lang;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Fin de l'Exploration (choix d'Astra, 2026-10-24), la première fois que les quatre donjons du
 * Désert de Braise sont vidés : le Croupier marche sur la carte du monde, les six lieux
 * s'allument l'un après l'autre (prairie, foire, port, mines, casino, désert), puis il pose
 * l'écaille d'or d'Ignar sur la table de jeu et sourit. Environ 7 secondes ; elle se rejoue
 * dans le menu Cinématique.
 */
public class ExplorationEndingCutscene extends Cutscene {

    /** Chaque lieu s'allume à son tour, à partir de {@link #LIGHTS}, tous les {@link #LIGHT_STEP}. */
    public static final float LIGHTS = 0.6f, LIGHT_STEP = 0.55f;
    /** La table de jeu, l'écaille posée, le sourire. */
    public static final float TABLE = 4.2f, SCALE_DOWN = 5.0f, SMILE = 5.5f;
    public static final float COVER_START = 7.0f, COVER_FULL = 7.7f;

    private static final Color GOLD = hex("ffc93a"), FELT = hex("1e6a3a"), FELT_DARK = hex("124a28"), WOOD = hex("6a3a1a");
    private static final String[] SCALE = {"..oo..", ".oyyo.", "oyyyyo", "oyyyyo", ".oyyo.", "..oo.."};

    private final TextureRegion croupier, scale;
    private final TextureRegion[] maps = new TextureRegion[Place.values().length];
    private final BitmapFont font;
    private final Array<Particle> sparks = new Array<>(false, 256);
    private float previous;

    public ExplorationEndingCutscene(CutsceneKit kit) {
        super(kit.settings(), kit.shake(), kit.sound("exploration_ending"));
        croupier = kit.portrait(EnemyKind.CROUPIER);
        scale = art("oy", new Color[] {hex("8a5a10"), hex("ffd23a")}, SCALE);
        for (Place place : Place.values()) {
            Texture map = kit.enemies() == null ? null : kit.enemies().map(place);
            maps[place.ordinal()] = map == null ? null : new TextureRegion(map);
        }
        font = kit.font();
    }

    @Override protected float coverStart() { return COVER_START; }
    @Override protected float coverFull()  { return COVER_FULL; }
    @Override protected Color coverColor() { return Color.BLACK; }

    @Override
    protected void reset() {
        sparks.clear();
        previous = 0f;
    }

    private boolean at(float moment) { return previous < moment && time >= moment; }

    /** @return le centre de la vignette du lieu {@code i} sur la carte du monde (deux rangées de trois). */
    private float[] spot(int i) {
        float w = worldWidth(), h = worldHeight();
        return new float[] {w * (0.2f + 0.3f * (i % 3)), h * (i < 3 ? 0.68f : 0.36f)};
    }

    @Override
    protected void simulate(float delta) {
        for (int i = 0; i < maps.length; i++) {
            if (!at(LIGHTS + i * LIGHT_STEP)) continue;
            float[] p = spot(i);
            for (int k = 0; k < 24; k++) {
                Particle spark = particle(p[0], p[1], 360f * random.nextFloat(), 300f * random.nextFloat(), 0.8f, 10f);
                spark.color.set(GOLD);
                spark.drag = 0.2f;
                sparks.add(spark);
            }
            rumble(0.1f, 3f);
        }
        if (at(SCALE_DOWN)) {
            rumble(0.2f, 5f);
            for (int k = 0; k < 40; k++) {
                Particle spark = particle(worldWidth() / 2f, worldHeight() * 0.3f, 360f * random.nextFloat(), 500f * random.nextFloat(), 1f, 12f);
                spark.color.set(GOLD);
                spark.drag = 0.2f;
                sparks.add(spark);
            }
        }
        update(sparks, delta);
        previous = time;
    }

    @Override
    protected void drawScene(Batch batch) {
        float w = worldWidth(), h = worldHeight();
        if (time < TABLE) drawWorld(batch, w, h);
        else drawTable(batch, w, h);
        drawParticles(batch, sparks);
        fadeFromBlack(batch, 0.4f);
        fadeThroughBlack(batch, TABLE, 0.25f);
    }

    /** La carte du monde : six vignettes qui s'allument, le Croupier qui marche de l'une à l'autre. */
    private void drawWorld(Batch batch, float w, float h) {
        fill(batch, hex("0a0814"), 1f);
        float tileW = w * 0.24f, tileH = tileW * 96f / 160f;
        int lit = 0;
        for (int i = 0; i < maps.length; i++) {
            float[] p = spot(i);
            boolean on = time >= LIGHTS + i * LIGHT_STEP;
            if (on) lit = i;
            if (i > 0) {                                                       // le chemin d'un lieu au suivant
                float[] q = spot(i - 1);
                tmp.set(on ? GOLD : hex("2a2a3a"));
                line(batch, tmp, q[0], q[1], p[0], p[1], on ? 8f : 4f);
            }
            rect(batch, hex("1a1018"), p[0] - tileW / 2f - 8f, p[1] - tileH / 2f - 8f, tileW + 16f, tileH + 16f);
            if (maps[i] != null) {
                float light = on ? 1f : 0.2f;
                batch.setColor(light, light, light, 1f);
                batch.draw(maps[i], p[0] - tileW / 2f, p[1] - tileH / 2f, tileW, tileH);
            }
            if (on) glow(batch, p[0], p[1], tileW * 1.3f, GOLD, 0.25f * (1f - progress(LIGHTS + i * LIGHT_STEP, LIGHTS + i * LIGHT_STEP + 0.6f)) + 0.08f);
        }
        // Le Croupier marche vers le dernier lieu allumé.
        float since = time - LIGHTS - lit * LIGHT_STEP;
        float[] from = spot(Math.max(0, lit - 1)), to = spot(lit);
        float k = lit == 0 ? 1f : Interpolation.pow2.apply(MathUtils.clamp(since / LIGHT_STEP + 1f, 0f, 1f));
        float x = MathUtils.lerp(from[0], to[0], k), y = MathUtils.lerp(from[1], to[1], k) - tileH * 0.2f;
        float bob = Math.abs(MathUtils.sin(time * 10f)) * 10f;
        portrait(batch, croupier, x, y + bob, 4f, 0f, 1f);
    }

    /** La table de jeu : il pose l'écaille d'or d'Ignar, et sourit. */
    private void drawTable(Batch batch, float w, float h) {
        gradient(batch, hex("1a0a0a"), hex("05030a"));
        glow(batch, w / 2f, h * 0.6f, w, hex("ffb84a"), 0.25f);
        portrait(batch, croupier, w / 2f, h * 0.28f, 14f, 0f, 1f);
        rect(batch, WOOD, -MARGIN, -MARGIN, w + 2f * MARGIN, h * 0.32f + MARGIN);
        rect(batch, FELT_DARK, w * 0.05f, -MARGIN, w * 0.9f, h * 0.3f + MARGIN);
        rect(batch, FELT, w * 0.07f, -MARGIN, w * 0.86f, h * 0.28f + MARGIN);
        float down = Interpolation.bounceOut.apply(progress(SCALE_DOWN - 0.5f, SCALE_DOWN));
        float y = MathUtils.lerp(h * 0.75f, h * 0.18f, down);
        glow(batch, w / 2f, y, 400f, GOLD, 0.6f);
        batch.setColor(Color.WHITE);
        sprite(batch, scale, w / 2f, y, 22f, 10f * (1f - down));
        float smile = progress(SMILE, SMILE + 0.3f);                           // son sourire
        if (smile > 0f) {
            glow(batch, w / 2f, h * 0.28f + 21f * 14f, 300f * smile, GOLD, 0.4f * smile);
            if (font != null) {
                float scale = font.getData().scaleX;
                font.getData().setScale(scale * 2.2f);                         // la réplique, en grand
                caption(batch, font, Lang.t("LA CHANCE A FAIT LE TOUR DU MONDE"), w / 2f, h * 0.9f, smile);
                font.getData().setScale(scale);
            }
        }
    }

    private void rect(Batch batch, Color color, float x, float y, float w, float h) {
        batch.setColor(color);
        batch.draw(pixel, x, y, w, h);
    }

    private void line(Batch batch, Color color, float x0, float y0, float x1, float y1, float thick) {
        float dx = x1 - x0, dy = y1 - y0;
        batch.setColor(color);
        batch.draw(pixel, x0, y0 - thick / 2f, 0f, thick / 2f, (float) Math.sqrt(dx * dx + dy * dy), thick, 1f, 1f,
            MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees);
    }
}
