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
 * Fin de l'Exploration, la première fois que les quatre donjons du Désert de Braise sont vidés
 * (refaite le 2026-11-23 pour les dix lieux). Environ 12 secondes, rejouable dans le menu Cinématique.
 * <ol>
 *   <li>La carte du monde : les dix lieux, leurs vraies cartes vues du ciel, reliés par une route
 *       en lacet. Le Croupier saute d'un lieu à l'autre ; chaque lieu s'allume quand il y arrive,
 *       et la carte du dernier roi vaincu de ce lieu (la vraie carte de la Boutique) s'en échappe
 *       pour rejoindre sa main, en bas de l'écran.</li>
 *   <li>Sa table de jeu : il étale les neuf premières cartes en éventail sur le tapis, puis pose
 *       celle d'Ignar, le dernier roi, au centre. Les dix cartes brillent, ses yeux s'allument,
 *       et la réplique tombe : « La chance a fait le tour du monde ».</li>
 * </ol>
 * Les instants ci-dessous sont repris dans tools/sounds/generate_sounds.py (cutscene/exploration_ending).
 */
public class ExplorationEndingCutscene extends Cutscene {

    /** Il arrive au premier lieu à {@link #ARRIVE}, puis au suivant toutes les {@link #STEP} secondes. */
    public static final float ARRIVE = 1.0f, STEP = 0.5f;
    /** La carte du roi sort du lieu, monte, puis file vers sa main en {@link #FLY} secondes. */
    public static final float RISE = 0.3f, FLY = 0.55f;
    /** Changement de plan : la table. */
    public static final float TABLE = 6.7f;
    /** Les neuf cartes étalées une à une, puis celle d'Ignar posée au centre. */
    public static final float DEAL = 7.1f, DEAL_STEP = 0.14f, DEAL_TIME = 0.35f, IGNAR_UP = 8.55f, IGNAR_DOWN = 9.0f;
    /** Les cartes brillent, ses yeux s'allument, la réplique. */
    public static final float SHINE = 9.3f, EYES = 9.5f, LINE = 9.75f;
    public static final float COVER_START = 11.4f, COVER_FULL = 12.1f;

    /** La carte de chaque roi final, dans l'ordre des lieux (les mêmes images que dans la Boutique). */
    private static final String[] KING_CARDS = {
        "roi_ronces",           // Prairie : Roi de Carreau
        "roi_carte_biseautee",  // Foire : le Roi forain
        "roi_haut_les_mains",   // Rapide de Minuit : le Desperado
        "roi_grappin",          // Port : le Capitaine Noir
        "roi_grand_cru",        // Palace des Neiges : le Prince des Neiges
        "roi_filon",            // Mines : le Coeur de la Montagne
        "roi_fil_de_soie",      // Manoir : le Marionnettiste
        "roi_encre",            // Casino : le Kraken
        "roi_jugement",         // Temple : l'Idole d'or
        "roi_braise",           // Désert : Ignar
    };
    private static final int PLACES = 10, LAST = PLACES - 1;

    private static final Color GOLD = hex("ffc93a"), GOLD_DARK = hex("a8761a"), GOLD_LIGHT = hex("fff0b0"),
        NIGHT_LOW = hex("05060f"), NIGHT_TOP = hex("161032"), ROAD = hex("2c2a44"), FRAME = hex("120c18"),
        FELT = hex("1e6a3a"), FELT_DARK = hex("155530"), WOOD = hex("5a2e14"), WOOD_DARK = hex("3a1a0a"),
        WOOD_LIGHT = hex("8a4a20"), OUT = hex("0a0506"), WALL = hex("2a0c10"), WALL_DARK = hex("160608"),
        RED = hex("ff3a2a");

    private final TextureRegion croupier;
    private final TextureRegion[] maps = new TextureRegion[PLACES];
    private final TextureRegion[] cards = new TextureRegion[PLACES];
    private final BitmapFont font;
    private final Starfield stars;
    private final Array<Particle> sparks = new Array<>(false, 512);
    private float previous;

    public ExplorationEndingCutscene(CutsceneKit kit) {
        super(kit.settings(), kit.shake(), kit.sound("exploration_ending"));
        croupier = kit.portrait(EnemyKind.CROUPIER);
        Place[] places = Place.values();
        for (int i = 0; i < PLACES && i < places.length; i++) {
            Texture map = kit.enemies() == null ? null : kit.enemies().map(places[i]);
            maps[i] = map == null ? null : new TextureRegion(map);
        }
        for (int i = 0; i < PLACES; i++) cards[i] = gameCard("cards/special_classic/blanc/" + KING_CARDS[i] + ".png");
        font = kit.font();
        stars = new Starfield(140, 0.05f);
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

    private static float arrival(int i) { return ARRIVE + i * STEP; }

    // =========================================================================
    // Mise en page
    // =========================================================================

    private float tileW() { return worldWidth() * 0.165f; }
    private float tileH() { return tileW() * 96f / 160f; }

    /** @return le centre du lieu {@code i} : la route va de gauche à droite en haut, revient de droite à gauche en bas. */
    private float[] spot(int i) {
        float w = worldWidth(), h = worldHeight();
        int col = i < 5 ? i : 9 - i;
        return new float[] {w * (0.12f + 0.19f * col), h * (i < 5 ? 0.76f : 0.45f)};
    }

    /** @return la place de la carte {@code i} dans sa main, en bas de l'écran : x, y, rotation. */
    private float[] handSlot(int i) {
        float w = worldWidth(), h = worldHeight(), k = i - 4.5f;
        return new float[] {w / 2f + k * w * 0.07f, h * 0.13f - k * k * 1.8f, -k * 3.5f};
    }

    private float cardScale() { return worldHeight() * 0.15f / 27f; }

    /** @return la place de la carte {@code i} sur le tapis : un éventail de neuf, celle d'Ignar au centre devant. */
    private float[] feltSlot(int i) {
        float w = worldWidth(), h = worldHeight();
        if (i == LAST) return new float[] {w / 2f, h * 0.125f, 0f};
        float k = i - 4f;                                                     // Ignar se pose devant, sous la carte du milieu
        return new float[] {w / 2f + k * w * 0.098f, h * 0.32f - k * k * 2.4f, -k * 2.5f};
    }

    private float tableScale() { return worldHeight() * 0.55f / 42f; }

    private float tableTop() { return worldHeight() * 0.40f; }

    // =========================================================================
    // Simulation
    // =========================================================================

    @Override
    protected void simulate(float delta) {
        for (int i = 0; i < PLACES; i++) {
            if (at(arrival(i))) {
                float[] p = spot(i);
                int count = i == LAST ? 60 : 26;
                for (int k = 0; k < count; k++) {
                    Particle spark = particle(p[0], p[1], 360f * random.nextFloat(), (i == LAST ? 520f : 320f) * random.nextFloat(),
                        0.7f + 0.4f * random.nextFloat(), 8f + 8f * random.nextFloat());
                    spark.color.set(GOLD);
                    spark.drag = 0.15f;
                    sparks.add(spark);
                }
                rumble(0.1f, i == LAST ? 7f : 2.5f);
            }
            if (at(arrival(i) + RISE + FLY)) {                                  // la carte arrive dans sa main
                float[] s = handSlot(i);
                for (int k = 0; k < 8; k++) {
                    Particle spark = particle(s[0], s[1], 360f * random.nextFloat(), 160f * random.nextFloat(), 0.45f, 7f);
                    spark.color.set(GOLD_LIGHT);
                    spark.drag = 0.2f;
                    sparks.add(spark);
                }
            }
        }
        if (at(TABLE)) sparks.clear();
        for (int i = 0; i < LAST; i++) {
            if (!at(DEAL + i * DEAL_STEP + DEAL_TIME)) continue;
            float[] s = feltSlot(i);
            for (int k = 0; k < 6; k++) {
                Particle dust = particle(s[0], s[1] - cardScale() * 12f, 180f * random.nextFloat(), 120f * random.nextFloat(), 0.4f, 8f);
                dust.color.set(GOLD_LIGHT);
                dust.drag = 0.15f;
                sparks.add(dust);
            }
        }
        if (at(IGNAR_DOWN)) {
            rumble(0.35f, 9f);
            float[] s = feltSlot(LAST);
            for (int k = 0; k < 90; k++) {
                Particle spark = particle(s[0], s[1], 360f * random.nextFloat(), 700f * random.nextFloat(), 0.9f + 0.6f * random.nextFloat(),
                    9f + 10f * random.nextFloat());
                spark.color.set(k % 3 == 0 ? hex("ff7a2a") : GOLD);
                spark.drag = 0.12f;
                spark.gravity = -220f;
                sparks.add(spark);
            }
        }
        update(sparks, delta);
        previous = time;
    }

    // =========================================================================
    // Dessin
    // =========================================================================

    @Override
    protected void drawScene(Batch batch) {
        float w = worldWidth(), h = worldHeight();
        if (time < TABLE) drawWorld(batch, w, h);
        else drawTable(batch, w, h);
        drawParticles(batch, sparks);
        if (time >= TABLE) drawLine(batch, w, h);
        fadeFromBlack(batch, 0.5f);
        fadeThroughBlack(batch, TABLE, 0.3f);
    }

    /** La carte du monde : les dix lieux, la route, le Croupier qui saute de l'un à l'autre, les cartes qui rejoignent sa main. */
    private void drawWorld(Batch batch, float w, float h) {
        gradient(batch, NIGHT_LOW, NIGHT_TOP);
        stars.draw(batch, 1f);
        float tw = tileW(), th = tileH();
        // La route : grise, puis dorée à mesure qu'il avance.
        float walked = walked();
        for (int i = 1; i < PLACES; i++) {
            float[] a = spot(i - 1), b = spot(i);
            line(batch, OUT, a[0], a[1], b[0], b[1], 14f);
            line(batch, ROAD, a[0], a[1], b[0], b[1], 8f);
            float k = MathUtils.clamp(walked - (i - 1), 0f, 1f);
            if (k > 0f) {
                line(batch, GOLD_DARK, a[0], a[1], MathUtils.lerp(a[0], b[0], k), MathUtils.lerp(a[1], b[1], k), 10f);
                line(batch, GOLD, a[0], a[1], MathUtils.lerp(a[0], b[0], k), MathUtils.lerp(a[1], b[1], k), 4f);
            }
        }
        // Les dix lieux.
        for (int i = 0; i < PLACES; i++) {
            float[] p = spot(i);
            float on = progress(arrival(i), arrival(i) + 0.3f);
            float pulse = on > 0f ? 1f + 0.08f * (1f - progress(arrival(i), arrival(i) + 0.4f)) * MathUtils.sin(progress(arrival(i), arrival(i) + 0.4f) * MathUtils.PI) : 1f;
            float fw = tw * pulse, fh = th * pulse;
            if (on > 0f) glow(batch, p[0], p[1], tw * 1.4f, GOLD, 0.10f + 0.25f * (1f - progress(arrival(i), arrival(i) + 0.8f)));
            rect(batch, on > 0f ? GOLD_DARK : FRAME, p[0] - fw / 2f - 6f, p[1] - fh / 2f - 6f, fw + 12f, fh + 12f);
            rect(batch, OUT, p[0] - fw / 2f - 2f, p[1] - fh / 2f - 2f, fw + 4f, fh + 4f);
            if (maps[i] != null) {
                float light = 0.18f + 0.82f * on;
                batch.setColor(light, light, light * (0.9f + 0.1f * on), 1f);
                batch.draw(maps[i], p[0] - fw / 2f, p[1] - fh / 2f, fw, fh);
            }
            float white = 1f - progress(arrival(i), arrival(i) + 0.35f);           // l'éclair de lumière sur la carte
            if (on > 0f && white > 0f && !settings.isReducedEffects()) {
                additive(batch);
                batch.setColor(1f, 0.9f, 0.6f, 0.6f * white);
                batch.draw(pixel, p[0] - fw / 2f, p[1] - fh / 2f, fw, fh);
                normal(batch);
            }
            float ring = progress(arrival(i), arrival(i) + 0.6f);              // l'onde dorée
            if (ring > 0f && ring < 1f) {
                additive(batch);
                batch.setColor(GOLD.r, GOLD.g, GOLD.b, 0.7f * (1f - ring));
                float size = tw * (0.4f + 1.3f * ring);
                batch.draw(this.ring, p[0] - size / 2f, p[1] - size / 2f, size, size);
                normal(batch);
            }
        }
        // Les cartes des rois : elles sortent du lieu, montent, puis filent dans sa main.
        float cs = cardScale();
        for (int i = 0; i < PLACES; i++) {
            float t0 = arrival(i);
            if (time < t0) continue;
            float[] p = spot(i), s = handSlot(i);
            float rise = Interpolation.pow2Out.apply(progress(t0, t0 + RISE));
            float fly = Interpolation.pow2InInverse.apply(progress(t0 + RISE, t0 + RISE + FLY));
            float x, y, scale, rotation;
            if (fly <= 0f) {
                x = p[0];
                y = p[1] + rise * th * 0.75f;
                scale = cs * (0.3f + 0.9f * rise);
                rotation = (1f - rise) * 180f;
            } else {
                float sx = p[0], sy = p[1] + th * 0.75f;
                x = MathUtils.lerp(sx, s[0], fly);
                y = MathUtils.lerp(sy, s[1], fly) + MathUtils.sin(fly * MathUtils.PI) * h * 0.06f;
                scale = cs * MathUtils.lerp(1.2f, 1f, fly);
                rotation = MathUtils.lerp(0f, s[2], fly) + MathUtils.sin(fly * MathUtils.PI) * 25f;
            }
            if (fly < 1f) glow(batch, x, y, cs * 40f, GOLD, 0.45f);
            drawCard(batch, cards[i], x, y, scale, rotation, 0f);
        }
        drawWalker(batch, walked, th);                                         // le Croupier, devant tout
    }

    /** @return combien de lieux il a déjà parcourus (0 au premier, 9 au Désert), avec la part du trajet en cours. */
    private float walked() {
        if (time <= arrival(0)) return 0f;
        float k = (time - arrival(0)) / STEP;
        return Math.min(LAST, k);
    }

    /** Le Croupier qui marche jusqu'au premier lieu, puis saute de lieu en lieu. */
    private void drawWalker(Batch batch, float walked, float th) {
        float s = worldHeight() * 0.15f / 42f, x, y, hop;
        if (time < arrival(0)) {                                               // il entre par la gauche
            float k = Interpolation.pow2Out.apply(progress(0f, arrival(0)));
            float[] first = spot(0);
            x = MathUtils.lerp(-40f * s, first[0], k);
            y = first[1];
            hop = Math.abs(MathUtils.sin(time * 9f)) * 0.1f;
        } else {
            int from = Math.min(LAST, (int) walked), to = Math.min(LAST, from + 1);
            float k = walked - from;
            float[] a = spot(from), b = spot(to);
            float e = Interpolation.smooth.apply(k);
            x = MathUtils.lerp(a[0], b[0], e);
            y = MathUtils.lerp(a[1], b[1], e);
            hop = from == to ? 0f : MathUtils.sin(k * MathUtils.PI) * 0.6f;
        }
        float bottom = y - th * 0.35f + hop * th;
        glow(batch, x, y - th * 0.35f, 34f * s, Color.BLACK, 0.5f * (1f - hop));  // son ombre sur la carte
        portrait(batch, croupier, x, bottom, s, 0f, 1f);
    }

    /** Sa table : le Croupier derrière le tapis, les cartes étalées, celle d'Ignar au centre. */
    private void drawTable(Batch batch, float w, float h) {
        float s = tableScale(), top = tableTop();
        // Le mur sombre, la lampe au-dessus de la table.
        gradient(batch, WALL_DARK, WALL);
        for (float x = -MARGIN; x < w + MARGIN; x += w / 9f) rect(batch, WALL_DARK, x, top, 10f, h);
        float lamp = 0.85f + 0.15f * MathUtils.sin(time * 3f);
        glow(batch, w / 2f, h * 0.95f, w * 0.9f, hex("ffb84a"), 0.28f * lamp);
        // Le Croupier, derrière sa table.
        float bottom = top - 9f * s;
        portrait(batch, croupier, w / 2f, bottom, s, 0f, 1f);
        float eyes = progress(EYES, EYES + 0.25f);
        if (eyes > 0f) {
            float flicker = 0.85f + 0.15f * MathUtils.sin(time * 9f);
            for (float ex : new float[] {13.5f, 20.5f}) {
                glow(batch, w / 2f + (ex - 17f) * s, bottom + (42f - 16.5f) * s, 7f * s, RED, 0.65f * eyes * flicker);
            }
        }
        // La table : bord de bois, tapis vert à motifs, liseré d'or.
        rect(batch, OUT, -MARGIN, top - 2f, w + 2f * MARGIN, 20f);
        rect(batch, WOOD, -MARGIN, top, w + 2f * MARGIN, 16f);
        rect(batch, WOOD_LIGHT, -MARGIN, top + 12f, w + 2f * MARGIN, 4f);
        rect(batch, FELT, -MARGIN, -MARGIN, w + 2f * MARGIN, top + MARGIN);
        for (float y = top - 30f; y > -MARGIN; y -= 36f) {
            for (float x = -MARGIN + ((int) (y / 36f) % 2) * 36f; x < w + MARGIN; x += 72f) rect(batch, FELT_DARK, x, y, 8f, 8f);
        }
        rect(batch, GOLD_DARK, w * 0.05f, top - 22f, w * 0.9f, 3f);
        glow(batch, w / 2f, top * 0.55f, w * 0.8f, hex("ffe08a"), 0.12f * lamp);
        // Les neuf cartes, glissées depuis sa main (le bas de l'écran) jusqu'à l'éventail.
        float cs = cardScale() * 1.1f;
        for (int i = 0; i < LAST; i++) {
            float t0 = DEAL + i * DEAL_STEP;
            if (time < t0) continue;
            float k = progress(t0, t0 + DEAL_TIME);
            float e = Interpolation.pow2Out.apply(k);
            float[] from = handSlot(i), to = feltSlot(i);
            float x = MathUtils.lerp(from[0], to[0], e), y = MathUtils.lerp(-cs * 20f, to[1], e);
            float rotation = MathUtils.lerp(from[2] + 40f * (i < 4 ? -1 : 1), to[2], e);
            drawCard(batch, cards[i], x, y, cs, rotation, shine(i));
        }
        // La carte d'Ignar : elle monte, plane, puis il la plaque au centre.
        if (time >= IGNAR_UP - 0.3f) {
            float[] to = feltSlot(LAST);
            float up = Interpolation.pow2Out.apply(progress(IGNAR_UP - 0.3f, IGNAR_UP));
            float down = Interpolation.pow3In.apply(progress(IGNAR_UP + 0.1f, IGNAR_DOWN));
            float hover = MathUtils.sin(time * 6f) * 6f * (1f - down);
            float y = MathUtils.lerp(-cs * 20f, h * 0.30f, up) + (to[1] - h * 0.30f) * down + hover;
            float scale = cs * MathUtils.lerp(1.7f, 1.35f, down);
            float halo = time < IGNAR_DOWN ? 0.4f : 0.4f + 0.6f * (1f - progress(IGNAR_DOWN, IGNAR_DOWN + 0.6f));
            glow(batch, to[0], y, scale * 60f, hex("ff8a2a"), halo);
            drawCard(batch, cards[LAST], to[0], y, scale, 0f, shine(LAST));
        }
        flash(batch, hex("fff0c0"), IGNAR_DOWN, 0.35f, 0.45f);
    }

    /** @return la brillance de la carte {@code i} : un éclat doré qui passe de gauche à droite. */
    private float shine(int i) {
        float x = i == LAST ? 4f : i;
        float t0 = SHINE + x * 0.06f;
        float k = progress(t0, t0 + 0.45f);
        return k <= 0f || k >= 1f ? 0f : k;
    }

    /** Une carte du jeu, avec son ombre, et son éclat {@code shine} (0 = pas d'éclat, sinon où en est le reflet). */
    private void drawCard(Batch batch, TextureRegion card, float x, float y, float scale, float rotation, float shine) {
        float cw = card.getRegionWidth() * scale, ch = card.getRegionHeight() * scale;
        batch.setColor(0f, 0f, 0f, 0.4f);
        batch.draw(card, x - cw / 2f + scale * 1.2f, y - ch / 2f - scale * 1.2f, cw / 2f, ch / 2f, cw, ch, 1f, 1f, rotation);
        batch.setColor(Color.WHITE);
        batch.draw(card, x - cw / 2f, y - ch / 2f, cw / 2f, ch / 2f, cw, ch, 1f, 1f, rotation);
        if (shine > 0f) {
            // Un reflet en biais qui traverse la carte, dans son repère.
            float band = cw * 0.35f, along = MathUtils.lerp(-cw * 0.6f, cw * 0.6f, shine);
            float cos = MathUtils.cosDeg(rotation), sin = MathUtils.sinDeg(rotation);
            additive(batch);
            batch.setColor(1f, 0.95f, 0.75f, 0.55f * MathUtils.sin(shine * MathUtils.PI));
            batch.draw(soft, x + along * cos - band / 2f, y + along * sin - ch * 0.55f, band / 2f, ch * 0.55f, band, ch * 1.1f, 1f, 1f, rotation - 20f);
            normal(batch);
            glow(batch, x, y, cw * 1.4f, GOLD, 0.25f * MathUtils.sin(shine * MathUtils.PI));
        }
    }

    /** La réplique, en grand, en haut de l'écran. */
    private void drawLine(Batch batch, float w, float h) {
        float alpha = progress(LINE, LINE + 0.35f);
        if (alpha <= 0f || font == null) return;
        float scale = font.getData().scaleX;
        font.getData().setScale(scale * 2.2f);
        caption(batch, font, Lang.t("LA CHANCE A FAIT LE TOUR DU MONDE"), w / 2f, h * 0.93f, alpha);
        font.getData().setScale(scale);
    }

    private void rect(Batch batch, Color color, float x, float y, float w, float h) {
        batch.setColor(color);
        batch.draw(pixel, x, y, w, h);
    }

    private void line(Batch batch, Color color, float x0, float y0, float x1, float y1, float thick) {
        float dx = x1 - x0, dy = y1 - y0;
        if (dx == 0f && dy == 0f) return;
        batch.setColor(color);
        batch.draw(pixel, x0, y0 - thick / 2f, 0f, thick / 2f, (float) Math.sqrt(dx * dx + dy * dy), thick, 1f, 1f,
            MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees);
    }
}
