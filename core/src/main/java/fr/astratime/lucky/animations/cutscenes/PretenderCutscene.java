package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.tower.Chapter;

/**
 * Avant le Prétendant (chapitre 4) : gros plan sur une table renversée, une main
 * lâche un éclat qui roule dans la rue. Une ville en flammes, des joueurs qui se
 * battent pour des éclats dorés. Une ombre traverse la foule : les flammes se
 * penchent sur son passage, les jetons au sol fondent. Elle ramasse trois
 * éclats, un, deux, trois, et les pose sur sa couronne, qui flotte au-dessus
 * d'elle : chacun s'allume de sa couleur (Comète, Reine, Éclat). Le Prétendant
 * est assis sur une pile de machines cassées, comme un trône ; la couronne se
 * pose sur sa tête. Il lève les yeux vers toi. Fondu noir.
 */
public class PretenderCutscene extends Cutscene {

    /** Gros plan sur la table renversée (voir le son cutscene/pretender) : une main lâche un éclat. */
    public static final float DROP         = 0.55f;
    public static final float TABLE_END    = 1.5f;
    /** L'ombre traverse la foule. */
    public static final float SHADOW_START = 3.3f;
    public static final float SHADOW_END   = 5.5f;
    /** Les trois éclats rejoignent l'ombre, un par un. */
    public static final float[] PICK       = {5.8f, 6.25f, 6.7f};
    /** Le Prétendant apparaît, sur son trône de machines cassées. */
    public static final float REVEAL       = 7.2f;
    /** La couronne descend et se pose sur sa tête. */
    public static final float CROWN_DOWN   = 7.8f;
    public static final float CROWN_LAND   = 8.7f;
    /** Il lève les yeux : on s'approche de son visage. */
    public static final float STARE        = 9.4f;
    public static final float COVER_START  = 10.1f;
    public static final float COVER_FULL   = 10.8f;

    /** Centre des yeux sur le portrait (34 x 42 pixels), depuis le milieu et le bas : ils ne sont pas symétriques. */
    private static final float[] EYES_X = {-3f, 4f};
    private static final float EYES_Y = 24.5f;

    /** Les couleurs des trois éclats : la Comète, la Reine, l'Éclat Originel. */
    private static final Color[] SHARDS = {c("ffc93a"), c("e0283a"), c("fff6c8")};
    /** Où brillent les éclats dans la ville (fractions de l'écran). */
    private static final float[][] SHARD_AT = {{0.22f, 0.42f}, {0.78f, 0.47f}, {0.52f, 0.33f}};
    private static final Color FIRE = c("ff7a1a"), GOLD = c("ffc93a");

    private final TextureRegion city, pretender, gem, crown, hand;
    private final Array<Particle> embers = new Array<>(false, 256);
    private final Array<Particle> sparks = new Array<>(false, 128);
    private float emberDebt;
    private int   picked;
    private boolean landed;

    public PretenderCutscene(CutsceneKit kit) {
        super(kit.settings(), kit.shake(), kit.sound("pretender"));
        city = kit.chapterArt(Chapter.MONDE_SANS_MAITRE);
        pretender = kit.portrait(EnemyKind.PRETENDANT);
        gem = art("o#w", new Color[] {c("1a0a0e"), Color.WHITE, c("ffffff")},
            "..o..",
            ".o#o.",
            "o###o",
            ".o#o.",
            "..o..");
        Color[] gold = {c("1a0a0e"), c("ffc93a"), c("b8801f"), c("e0283a")};
        crown = art("ogdr", gold,
            "o.....o.....o",
            "go...ogo...og",
            "ggo.oggdo.ogg",
            "gggogggggoggg",
            "ggggggrgggggg",
            "ddddddddddddd",
            "ooooooooooooo");
        hand = art("osd", new Color[] {c("1a0a0e"), c("f0c8a0"), c("b88a68")},
            "..oooooooo....",
            ".ossssssssoo..",
            "osssssssssssoo",
            "ossssssssssssso",
            "osssssssssssddo",
            ".osssssssdddoo.",
            "..ooooooooooo..");
    }

    @Override protected float coverStart() { return COVER_START; }
    @Override protected float coverFull()  { return COVER_FULL; }
    @Override protected Color coverColor() { return Color.BLACK; }

    @Override
    protected void reset() {
        embers.clear();
        sparks.clear();
        emberDebt = 0f;
        picked = 0;
        landed = false;
    }

    @Override
    protected void simulate(float delta) {
        float width = worldWidth(), height = worldHeight();
        emberDebt += 70f * delta;
        while (emberDebt >= 1f) {                         // braises qui montent des incendies
            emberDebt -= 1f;
            Particle ember = particle(width * random.nextFloat(), height * (0.05f + 0.5f * random.nextFloat()),
                80f + 20f * random.nextFloat(), 60f + 120f * random.nextFloat(), 1.2f + 1.5f * random.nextFloat(),
                5f + 8f * random.nextFloat());
            ember.color.set(random.nextFloat() < 0.7f ? FIRE : GOLD);
            ember.gravity = -20f;
            embers.add(ember);
        }
        if (!landed && time >= CROWN_LAND) {                 // la couronne se pose
            landed = true;
            rumble(0.2f, 6f);
            for (int i = 0; i < 24; i++) {
                Particle spark = particle(crownX(1), crownY(), 360f * random.nextFloat(), 200f + 250f * random.nextFloat(),
                    0.5f, 12f);
                spark.drag = 0.1f;
                spark.color.set(GOLD);
                sparks.add(spark);
            }
        }
        while (picked < PICK.length && time >= PICK[picked]) {
            for (int i = 0; i < 18; i++) {
                Particle spark = particle(crownX(picked), crownY(), 360f * random.nextFloat(),
                    150f + 250f * random.nextFloat(), 0.5f, 12f);
                spark.drag = 0.1f;
                spark.color.set(SHARDS[picked]);
                sparks.add(spark);
            }
            picked++;
        }
        update(embers, delta);
        update(sparks, delta);
    }

    @Override
    protected void drawScene(Batch batch) {
        if (time < TABLE_END) {
            drawTable(batch);
            fadeFromBlack(batch, 0.5f);
            fadeThroughBlack(batch, TABLE_END, 0.25f);
            return;
        }
        float width = worldWidth(), height = worldHeight();
        // La ville en flammes, qui tremble à la chaleur ; elle s'assombrit au passage de l'ombre.
        float zoom = Math.max(width / city.getRegionWidth(), height / city.getRegionHeight()) * 1.08f;
        float drift = MathUtils.lerp(-20f, 20f, progress(TABLE_END, COVER_FULL));
        float dark = 1f - 0.55f * progress(SHADOW_START, SHADOW_END) - 0.2f * progress(REVEAL, STARE);
        float flicker = 0.92f + 0.08f * MathUtils.sin(time * 23f) * MathUtils.sin(time * 7f);
        batch.setColor(dark * flicker, dark * 0.9f * flicker, dark * 0.85f * flicker, 1f);
        float artW = city.getRegionWidth() * zoom, artH = city.getRegionHeight() * zoom;
        batch.draw(city, (width - artW) / 2f + drift, (height - artH) / 2f, artW, artH);
        glow(batch, width / 2f, 0f, width * 1.4f, FIRE, 0.25f * flicker);
        drawParticles(batch, embers);

        // Les éclats dorés que la foule se dispute, puis qui s'envolent vers l'ombre.
        for (int i = 0; i < SHARDS.length; i++) {
            float fly = progress(PICK[i] - 0.35f, PICK[i]);
            if (fly >= 1f) continue;
            float e = Interpolation.pow2In.apply(fly);
            float x = MathUtils.lerp(width * SHARD_AT[i][0], crownX(i), e);
            float y = MathUtils.lerp(height * SHARD_AT[i][1], crownY(), e) + 8f * MathUtils.sin(time * 4f + i);
            drawGem(batch, x, y, SHARDS[i], 7f);
        }

        // L'ombre traverse la foule, de gauche à droite : les flammes se penchent, les jetons fondent.
        float shadowX = MathUtils.lerp(-width * 0.2f, width * 1.2f,
            Interpolation.sine.apply(progress(SHADOW_START, SHADOW_END)));
        drawStreet(batch, shadowX);
        if (time >= SHADOW_START && time < SHADOW_END + 0.2f) {
            float x = shadowX;
            batch.setColor(0f, 0f, 0f, 0.45f);
            batch.draw(soft, x - width * 0.35f, -height * 0.2f, width * 0.7f, height * 1.4f);
            portrait(batch, pretender, x, -40f, 16f, 1f, 1f);
        }

        // Le Prétendant, en ombre, puis éclairé par ses éclats ; on s'approche de son visage.
        if (time >= PICK[0] - 0.4f) {
            float appear = progress(PICK[0] - 0.4f, PICK[0]);
            float light = progress(REVEAL, REVEAL + 0.5f);
            float scale = MathUtils.lerp(10f, 15f, Interpolation.pow2In.apply(progress(STARE, COVER_FULL)));
            float baseY = MathUtils.lerp(height * 0.12f, -height * 0.35f, Interpolation.pow2In.apply(
                progress(STARE, COVER_FULL)));
            glow(batch, width / 2f, baseY + 30f * scale, 30f * scale, GOLD, 0.25f * light);
            drawThrone(batch, width / 2f, baseY, scale, 1f - 0.85f * light, appear);
            portrait(batch, pretender, width / 2f, baseY, scale, 1f - 0.9f * light, appear);
            // La couronne flotte au-dessus de lui, puis se pose ; les éclats y sont sertis.
            float hover = (1f - Interpolation.bounceOut.apply(progress(CROWN_DOWN, CROWN_LAND))) * 12f * scale
                + (time < CROWN_DOWN ? MathUtils.sin(time * 3f) * scale : 0f);
            float crownLight = Math.max(0.35f, light);
            if (time < CROWN_LAND + 0.15f) {
                glow(batch, width / 2f, baseY + 38.5f * scale + hover, 22f * scale, GOLD, 0.3f * appear);
                batch.setColor(crownLight, crownLight, crownLight, appear * (1f - progress(CROWN_LAND, CROWN_LAND + 0.15f)));
                sprite(batch, crown, width / 2f, baseY + 39f * scale + hover, scale * 1.4f, -6f);
            }
            for (int i = 0; i < picked; i++) {                         // les éclats sur la couronne
                float x = width / 2f + (i - 1) * 7f * scale;
                float y = baseY + 38.5f * scale + hover;
                drawGem(batch, x, y, SHARDS[i], scale * 0.8f);
            }
        }
        drawParticles(batch, sparks);
        // Le regard : deux lueurs dans les yeux.
        if (time >= STARE) {
            float stare = progress(STARE, STARE + 0.3f);
            float scale = MathUtils.lerp(10f, 15f, Interpolation.pow2In.apply(progress(STARE, COVER_FULL)));
            float baseY = MathUtils.lerp(height * 0.12f, -height * 0.35f, Interpolation.pow2In.apply(
                progress(STARE, COVER_FULL)));
            for (float eye : EYES_X) {
                glow(batch, width / 2f + eye * scale, baseY + EYES_Y * scale, 4f * scale, GOLD, 0.9f * stare);
            }
        }
        fadeThroughBlack(batch, TABLE_END, 0.25f);
    }

    /**
     * Gros plan sur une table de jeu renversée dans la rue en feu : une main
     * lâche un éclat doré, qui rebondit et roule hors de l'écran.
     */
    private void drawTable(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        float zoom = Math.max(width / city.getRegionWidth(), height / city.getRegionHeight()) * 2.2f;
        float flicker = 0.92f + 0.08f * MathUtils.sin(time * 23f) * MathUtils.sin(time * 7f);
        batch.setColor(0.45f * flicker, 0.3f * flicker, 0.28f * flicker, 1f);      // la ville, floue derrière
        batch.draw(city, (width - city.getRegionWidth() * zoom) / 2f, -height * 0.4f,
            city.getRegionWidth() * zoom, city.getRegionHeight() * zoom);
        glow(batch, width * 0.5f, height * 0.2f, width * 1.4f, FIRE, 0.3f * flicker);
        float ground = height * 0.25f;
        batch.setColor(c("1a0f0c"));
        batch.draw(pixel, -MARGIN, -MARGIN, width + 2f * MARGIN, ground + MARGIN);
        batch.setColor(c("2e1c14"));
        for (int i = 0; i < 9; i++) batch.draw(pixel, -MARGIN + i * width / 8f, ground - 8f, width / 8f - 10f, 6f);
        // La table, renversée sur le flanc : le plateau de feutre debout face à nous, les pieds de côté.
        float tx = width * 0.3f, ty = ground - 20f, tw = width * 0.36f, th = height * 0.42f;
        batch.setColor(c("2a160a"));
        for (int k = 0; k < 2; k++) batch.draw(pixel, tx + tw - 20f, ty + th * (0.2f + 0.55f * k), width * 0.12f, 22f);
        batch.setColor(c("3a2010"));
        batch.draw(pixel, tx, ty, tw / 2f, 0f, tw, th, 1f, 1f, 4f);
        batch.setColor(c("1f7a3a"));
        batch.draw(pixel, tx + 16f, ty + 16f, tw / 2f - 16f, -16f, tw - 32f, th - 32f, 1f, 1f, 4f);
        batch.setColor(c("f4ecdc"));                                          // les lignes du tapis de jeu
        batch.draw(pixel, tx + tw * 0.2f, ty + th * 0.5f, tw * 0.3f, -th * 0.5f, tw * 0.6f, 4f, 1f, 1f, 4f);
        batch.draw(pixel, tx + tw * 0.48f, ty + th * 0.2f, 2f, th * 0.3f, 4f, th * 0.6f, 1f, 1f, 4f);
        for (int k = 0; k < 5; k++) {                                          // des jetons renversés au sol
            Color color = k % 3 == 0 ? c("c8283c") : k % 3 == 1 ? c("2a5ad8") : c("1f9a4a");
            float cx = width * (0.12f + k * 0.18f), cy = ground * (0.35f + 0.2f * (k % 2));
            batch.setColor(color);
            batch.draw(pixel, cx, cy, 70f, 22f);
            batch.setColor(1f, 1f, 1f, 0.8f);
            for (int st = 0; st < 3; st++) batch.draw(pixel, cx + 10f + st * 22f, cy + 3f, 7f, 16f);
        }
        drawParticles(batch, embers);
        // La main, qui dépasse du haut de la table : elle se crispe, puis s'ouvre.
        float scale = height / 70f;
        float hx = tx + tw * 0.6f, hy = ty + th + 3f * scale;
        float open = progress(DROP - 0.15f, DROP);
        batch.setColor(0.85f, 0.7f, 0.65f, 1f);
        sprite(batch, hand, hx, hy, scale, -25f - 40f * open);
        // L'éclat tombe devant la table, rebondit, roule vers la droite et sort.
        if (time >= DROP) {
            float t = time - DROP;
            float fall = Math.min(1f, t / 0.3f);
            float x = hx + 6f * scale + Math.max(0f, t - 0.3f) * width * 0.9f;
            float floor = ground * 0.5f;
            float y = t < 0.3f ? MathUtils.lerp(hy - 4f * scale, floor, fall * fall)
                : floor + Math.abs(MathUtils.sin((t - 0.3f) * 9f)) * 120f * Math.max(0f, 1f - (t - 0.3f) * 1.6f);
            drawGem(batch, x, y, GOLD, scale * 1.6f);
        } else {
            drawGem(batch, hx + 4f * scale, hy - 3f * scale, GOLD, scale * 1.6f);
        }
    }

    /** Les flammes de la rue et les jetons au sol : à son passage, les flammes se penchent et les jetons fondent. */
    private void drawStreet(Batch batch, float shadowX) {
        float width = worldWidth(), height = worldHeight();
        boolean passing = time >= SHADOW_START - 0.3f;
        for (int i = 0; i < 12; i++) {                                     // des flammes le long de la rue
            float x = width * (i + 0.5f) / 12f, base = height * 0.02f + (i % 3) * 8f;
            float dx = passing ? x - shadowX : 99999f;
            float lean = MathUtils.clamp(dx / (width * 0.25f), -1f, 1f) * Math.max(0f, 1f - Math.abs(dx) / (width * 0.5f));
            float flick = 0.8f + 0.2f * MathUtils.sin(time * 15f + i * 1.7f);
            for (int k = 0; k < 4; k++) {
                float y = base + k * 22f * flick, off = lean * k * 26f + MathUtils.sin(time * 9f + i + k) * 4f;
                glow(batch, x + off, y, (70f - k * 13f) * flick, k < 2 ? FIRE : GOLD, 0.55f - k * 0.08f);
            }
        }
        for (int i = 0; i < 9; i++) {                                      // les jetons au sol, qui fondent
            float x = width * (0.08f + i * 0.105f), y = height * 0.035f + (i % 2) * 10f;
            float melt = time < SHADOW_START ? 0f : MathUtils.clamp((shadowX - x) / (width * 0.12f), 0f, 1f);
            float w = 70f * (1f + 0.6f * melt), h = 22f * (1f - 0.65f * melt);
            Color color = i % 3 == 0 ? c("c8283c") : i % 3 == 1 ? c("2a5ad8") : c("1f9a4a");
            float d = 1f - 0.5f * melt;
            batch.setColor(0f, 0f, 0f, 0.6f);
            batch.draw(pixel, x - w / 2f - 2f, y - 2f, w + 4f, h + 4f);
            batch.setColor(color.r * d, color.g * d, color.b * d, 1f);
            batch.draw(pixel, x - w / 2f, y, w, h);
            if (melt < 0.6f) {
                batch.setColor(1f, 1f, 1f, 0.8f * (1f - melt));
                for (int s = 0; s < 3; s++) batch.draw(pixel, x - w / 2f + (0.15f + s * 0.3f) * w, y + 2f, w * 0.1f, h - 4f);
            } else {
                glow(batch, x, y + h, w, FIRE, 0.4f * melt);
            }
        }
    }

    /** Son trône : une pile de machines à sous cassées, sous lui et autour. */
    private void drawThrone(Batch batch, float cx, float baseY, float scale, float shadow, float alpha) {
        float light = 1f - shadow;
        float[][] boxes = {{-14f, -8f, 14f, 12f, -8f}, {13f, -9f, 15f, 13f, 6f}, {-4f, -14f, 16f, 12f, 2f},
            {-20f, 2f, 11f, 10f, -15f}, {19f, 1f, 11f, 10f, 12f}, {-10f, -24f, 18f, 12f, -3f}, {9f, -23f, 18f, 12f, 4f}};
        for (float[] b : boxes) {
            float w = b[2] * scale, h = b[3] * scale, x = cx + b[0] * scale - w / 2f, y = baseY + b[1] * scale;
            float tint = 0.25f + 0.75f * light;
            batch.setColor(0.08f, 0.04f, 0.05f, alpha);
            batch.draw(pixel, x - scale, y - scale, w / 2f + scale, h / 2f + scale, w + 2f * scale, h + 2f * scale, 1f, 1f, b[4]);
            batch.setColor(0.8f * tint, 0.62f * tint, 0.2f * tint, alpha);
            batch.draw(pixel, x, y, w / 2f, h / 2f, w, h, 1f, 1f, b[4]);
            batch.setColor(0.1f, 0.06f, 0.08f, alpha);                     // la vitre des rouleaux, brisée
            batch.draw(pixel, x + w * 0.15f, y + h * 0.4f, w * 0.35f, h * 0.1f, w * 0.7f, h * 0.35f, 1f, 1f, b[4]);
        }
    }

    private void drawGem(Batch batch, float x, float y, Color color, float scale) {
        glow(batch, x, y, scale * 14f, color, 0.7f);
        batch.setColor(color);
        sprite(batch, gem, x, y, scale, 0f);
    }

    /** @return où l'éclat {@code i} rejoint la couronne de l'ombre (avant qu'on s'approche). */
    private float crownX(int i) { return worldWidth() / 2f + (i - 1) * 70f; }
    private float crownY() {
        float hover = (1f - Interpolation.bounceOut.apply(progress(CROWN_DOWN, CROWN_LAND))) * 12f * 10f;
        return worldHeight() * 0.12f + 38.5f * 10f + hover;
    }
}
