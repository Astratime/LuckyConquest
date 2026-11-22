package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.assets.PlacesPortraits;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.views.FrontScene;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les scènes de phase et de mort des quatre rois du Port des Contrebandiers (choix d'Astra, 2026-10-24 ; refaites
 * le 2026-11-22 pour montrer ce que dit leur phrase, dans leur vrai décor : la cale, la taverne, le phare, le galion).
 */
final class PortBossScenes {

    private PortBossScenes() {}

    static BossScene phase(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case CAPITAINE_RAT -> new RatPhase(kit);
            case TAVERNIER -> new TavernPhase(kit);
            case GARDIEN_PHARE -> new LighthousePhase(kit);
            case CAPITAINE_NOIR -> new BlackPhase(kit);
            default -> null;
        };
    }

    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case CAPITAINE_RAT -> new RatDeath(kit);
            case TAVERNIER -> new TavernDeath(kit);
            case GARDIEN_PHARE -> new LighthouseDeath(kit);
            case CAPITAINE_NOIR -> new BlackDeath(kit);
            default -> null;
        };
    }

    // ----- Accessoires -----

    static final Color SEA = Color.valueOf("0a2a4a"), FOAM = Color.valueOf("8ac8f0"), WOOD = Color.valueOf("4a2a14"),
        DARK_WOOD = Color.valueOf("2a160a");
    static final String[] RAT = {
        "..oo....oo..", ".opgo..ogpo.", ".ogggoogggo.", "..oggggggo..", "..orgggrgo..", "..oggggggo..", "...ogppgo...",
        "..oggggggo..", ".oggggggggo.", ".oggggggggo.", "..oo....oo.."};
    static final String[] MUG = {
        ".wwwwww...", "owwwwwwo..", "obbbbbbooo", "obdbbbbo.o", "obdbbbbo.o", "obdbbbbo.o", "obdbbbbooo", "obbbbbbo..",
        "oggggggo..", ".oooooo..."};
    static final String[] SAILOR = {
        "..oooooo..", ".oooooooo.", "oooooooooo", ".oooooooo.", ".oooooooo.", ".oooooooo.", "..oooooo..", "...oooo...",
        "..oooooo..", ".oooooooo.", "oooooooooo", "oooooooooo", "oooooooooo"};
    static final String[] STOOL = {"oooooo", "o....o", "o....o", "o....o"};
    static final String[] HOOK = {".o.o.", "o.o.o", "..o..", "..o..", "..o.."};

    /** La mer : son niveau {@code level} (en y), une ligne d'écume qui ondule. */
    static void sea(BossScene scene, Batch batch, float level, float alpha) {
        scene.rect(batch, SEA, alpha, -60f, -60f, scene.worldWidth() + 120f, level + 60f);
        for (int i = 0; i < 40; i++) {
            float x = i * scene.worldWidth() / 39f, y = level + 6f * MathUtils.sin(scene.time * 3f + i * 0.8f);
            scene.rect(batch, FOAM, 0.6f * alpha, x - 30f, y - 4f, 60f, 8f);
        }
    }

    /** @return les pixels du décor de {@code kind}, à modifier par la scène ({@code null} sans fichiers, dans les tests). */
    static Pixmap decorPixmap(EnemyKind kind) {
        String path = FrontScene.Decor.of(kind).getPath();
        if (Gdx.files == null || !Gdx.files.internal(path).exists()) return null;
        return new Pixmap(Gdx.files.internal(path));
    }

    /** @return une copie de {@code source}. */
    static Pixmap copy(Pixmap source) {
        Pixmap copy = new Pixmap(source.getWidth(), source.getHeight(), Pixmap.Format.RGBA8888);
        copy.setBlending(Pixmap.Blending.None);
        copy.drawPixmap(source, 0, 0);
        return copy;
    }

    /** Redessine l'image {@code image} du décor (ou sa variante) par-dessus le décor, à sa place, éclairée comme lui. */
    static void redrawDecor(BossScene scene, Batch batch, TextureRegion image, float alpha) {
        if (image == null || alpha <= 0f) return;
        float scale = scene.decorScale(), w = image.getRegionWidth() * scale, h = image.getRegionHeight() * scale;
        Color lit = scene.litLikeDecor(Color.WHITE, scene.tmp);
        batch.setColor(lit.r, lit.g, lit.b, alpha);
        batch.draw(image, scene.worldWidth() / 2f - w / 2f, scene.worldHeight() + Cutscene.MARGIN - h, w, h);
    }

    /** Repeint les pixels ({@code px}, {@code py}, {@code w} x {@code h}) du décor en {@code color}, éclairés comme lui. */
    static void decorPaint(BossScene scene, Batch batch, Color color, float alpha, float px, float py, float w, float h) {
        if (alpha <= 0f) return;
        Color lit = scene.litLikeDecor(color, scene.tmp);
        batch.setColor(lit.r, lit.g, lit.b, alpha);
        float s = scene.decorScale();
        batch.draw(scene.pixel, scene.decorX(px), scene.decorY(py + h), w * s, h * s);
    }

    /** Un quadrilatère plein (quatre coins dans l'ordre), de la couleur {@code color}. */
    static void quad(BossScene scene, Batch batch, Color color, float alpha, float x0, float y0, float x1, float y1, float x2,
                     float y2, float x3, float y3) {
        TextureRegion p = scene.pixel;
        float c = Color.toFloatBits(color.r, color.g, color.b, alpha), u = (p.getU() + p.getU2()) / 2f, v = (p.getV() + p.getV2()) / 2f;
        float[] vertices = {x0, y0, c, u, v, x1, y1, c, u, v, x2, y2, c, u, v, x3, y3, c, u, v};
        batch.draw(p.getTexture(), vertices, 0, vertices.length);
    }

    /** Fait fumer ({@code count} volutes grises qui montent) depuis ({@code x}, {@code y}). */
    static void smoke(BossScene scene, Array<Cutscene.Particle> into, float x, float y, int count, float size) {
        for (int i = 0; i < count; i++) {
            Cutscene.Particle puff = scene.particle(x + (scene.random.nextFloat() - 0.5f) * size * 0.3f, y,
                80f + 20f * (scene.random.nextFloat() - 0.5f), 50f + 60f * scene.random.nextFloat(), 1.8f + scene.random.nextFloat(),
                size * (0.6f + 0.5f * scene.random.nextFloat()));
            puff.drag = 0.6f;
            into.add(puff);
        }
    }

    /** Dessine la fumée {@code puffs} (gris {@code grey}), qui grossit et s'efface. */
    static void drawSmoke(BossScene scene, Batch batch, Array<Cutscene.Particle> puffs, float grey, float alpha) {
        for (Cutscene.Particle puff : puffs) {
            float size = puff.size * (1f + 1.5f * puff.life());
            batch.setColor(grey, grey, grey * 1.05f, alpha * (1f - puff.life()));
            batch.draw(scene.soft, puff.x - size / 2f, puff.y - size / 2f, size, size);
        }
    }

    // -------------------------------------------------------------------------
    // Capitaine Rat
    // -------------------------------------------------------------------------

    /** Grappins lancés : une nuée de rats grimpe aux cordes et envahit l'écran par les bords. */
    static final class RatPhase extends BossScene {
        private static final float[][] HOOKS = {{-0.02f, 0.92f}, {1.02f, 0.88f}, {-0.02f, 0.55f}, {1.02f, 0.5f}};
        private static final Color ROPE = hex("b89a6a");
        private final TextureRegion rat, hook;

        RatPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.CAPITAINE_RAT, 2);
            rat = art("ogrp", new Color[] {hex("0a0808"), hex("4a4048"), hex("e0283a"), hex("c87a8a")}, RAT);
            hook = art("o", new Color[] {hex("9a9ab0")}, HOOK);
        }

        private float hookTime(int i) { return 0.3f + i * 0.25f; }
        private float[] anchor(int i) {
            return new float[] {bossX() + (HOOKS[i][0] < 0.5f ? -1 : 1) * bossWidth() * (0.7f + 0.2f * (i / 2)), groundY() + 20f};
        }

        @Override
        protected void step(float delta) {
            for (int i = 0; i < HOOKS.length; i++) if (at(hookTime(i) + 0.3f)) rumble(0.15f, 5f);
            if (at(CLIMAX_PHASE)) { rumble(0.7f, 12f); sparks(bossX(), bossY(), 50, hex("e0283a"), 700f, 20f); }
        }

        @Override
        protected void back(Batch batch) {
            for (int i = 0; i < HOOKS.length; i++) {                    // les cordes des grappins, puis les rats qui y grimpent
                float throwK = Interpolation.pow2Out.apply(progress(hookTime(i), hookTime(i) + 0.3f));
                if (throwK <= 0f) continue;
                float sx = HOOKS[i][0] * worldWidth(), sy = HOOKS[i][1] * worldHeight();
                float[] a = anchor(i);
                float ex = MathUtils.lerp(sx, a[0], throwK), ey = MathUtils.lerp(sy, a[1], throwK);
                line(batch, ROPE, 1f, sx, sy, ex, ey, 6f);
                draw(batch, hook, ex, ey, fit(40f, 5), MathUtils.atan2(ey - sy, ex - sx) * MathUtils.radiansToDegrees + 90f, WHITE, 1f);
                if (throwK < 1f) continue;
                for (int r = 0; r < 6; r++) {                           // ils descendent la corde vers le pont
                    float k = ((time - hookTime(i) - 0.3f) * 0.8f + r / 6f) % 1f;
                    float x = MathUtils.lerp(sx, a[0], k), y = MathUtils.lerp(sy, a[1], k) + 16f;
                    draw(batch, rat, x, y, fit(46f, 11), MathUtils.atan2(a[1] - sy, a[0] - sx) * MathUtils.radiansToDegrees + 90f, WHITE, 1f);
                }
            }
            poseY = 10f * gesture(CLIMAX_PHASE - 0.2f, CLIMAX_PHASE + 0.3f);
        }

        @Override
        protected void front(Batch batch) {
            // l'invasion : des rats qui courent par les bords sur le pont
            float flood = progress(1.2f, 3.6f);
            int count = (int) (40 * flood);
            for (int r = 0; r < count; r++) {
                int side = r % 2 == 0 ? 1 : -1;
                float run = ((time * (300f + (r * 37 % 7) * 40f) + r * 97f) % (worldWidth() * 0.5f));
                float x = side > 0 ? -60f + run : worldWidth() + 60f - run;
                float y = groundY() - 10f + (r * 53 % 9) * 12f + Math.abs(MathUtils.sin(time * 18f + r)) * 8f;
                stretch(batch, rat, x, y, fit(54f, 11) * side, fit(54f, 11), 0f, WHITE, 1f);
            }
            eyes(batch, progress(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.2f), hex("ff2a3a"), new float[] {14f, 18f}, new float[] {21f, 18f});
            flash(batch, hex("e0283a"), CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /**
     * « Le Capitaine Rat quitte le navire » : la cale du décor gîte et prend l'eau ; le vrai hublot du décor s'ouvre,
     * ses rats y filent un par un ; il bondit à leur suite, son vrai tricorne (celui de son portrait) s'envole et
     * tombe à l'eau ; il se faufile par le hublot et s'éloigne à la nage, la mer entre par le hublot, son chapeau flotte.
     */
    static final class RatDeath extends BossScene {
        /** Le hublot du décor de la cale : son centre et son rayon (pixels du décor), le carré qui le contient. */
        private static final float PX = 48.5f, PY = 62.5f, PR = 17.5f;
        private static final int P_LEFT = 31, P_TOP = 45, P_SIZE = 35;
        private static final float OPEN = 0.4f, LEAP = 1.7f, GONE = CLIMAX_DEATH + 0.2f, GUSH = 3.9f;
        private static final int RATS = 9;
        private final TextureRegion rat, bare, glass, outside;
        private final Piece hat;
        private float hatX, hatY;
        private final Color light = new Color();
        private boolean off;

        RatDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.CAPITAINE_RAT, 0);
            rat = art("ogrp", new Color[] {hex("0a0808"), hex("4a4048"), hex("e0283a"), hex("c87a8a")}, RAT);
            int[][] rest = PlacesPortraits.without(EnemyKind.CAPITAINE_RAT, PlacesPortraits.Prop.RAT_HAT);
            hat = lift(PlacesPortraits.pixels(EnemyKind.CAPITAINE_RAT), rest);
            bare = pixels(rest);
            Pixmap decor = decorPixmap(kind);
            glass = decor == null ? null : region(disc(decor), false);
            if (decor != null) decor.dispose();
            outside = region(outside(), false);
        }

        /** @return la vitre ronde du vrai hublot du décor (son cercle de laiton compris), sans le mur autour. */
        private static Pixmap disc(Pixmap decor) {
            Pixmap disc = new Pixmap(P_SIZE, P_SIZE, Pixmap.Format.RGBA8888);
            disc.setBlending(Pixmap.Blending.None);
            float r = P_SIZE / 2f;
            for (int y = 0; y < P_SIZE; y++) {
                for (int x = 0; x < P_SIZE; x++) {
                    float dx = x + 0.5f - r, dy = y + 0.5f - r;
                    if (dx * dx + dy * dy <= r * r) disc.drawPixel(x, y, decor.getPixel(P_LEFT + x, P_TOP + y));
                }
            }
            return disc;
        }

        /** @return ce qu'on voit par le hublot ouvert : la nuit, la mer et son écume. */
        private static Pixmap outside() {
            Pixmap view = new Pixmap(P_SIZE, P_SIZE, Pixmap.Format.RGBA8888);
            view.setBlending(Pixmap.Blending.None);
            float r = P_SIZE / 2f - 2f;
            int sky = Color.rgba8888(hex("101a38")), sea = Color.rgba8888(hex("1a4a7a")), foam = Color.rgba8888(hex("8ac8f0")),
                star = Color.rgba8888(hex("e8e8f0"));
            for (int y = 0; y < P_SIZE; y++) {
                for (int x = 0; x < P_SIZE; x++) {
                    float dx = x + 0.5f - P_SIZE / 2f, dy = y + 0.5f - P_SIZE / 2f;
                    if (dx * dx + dy * dy > r * r) continue;
                    int color = y < 17 ? sky : y == 17 || (y == 22 && x % 5 < 2) || (y == 27 && x % 6 < 2) ? foam : sea;
                    if ((x == 12 && y == 8) || (x == 22 && y == 11) || (x == 17 && y == 5)) color = star;
                    view.drawPixel(x, y, color);
                }
            }
            return view;
        }

        @Override protected void restart() { off = false; }

        /** @return la gîte du navire (degrés). */
        private float list() { return -6f * Interpolation.pow2Out.apply(progress(0.1f, 1.5f)) + 1.2f * MathUtils.sin(time * 1.7f); }

        /** @return le niveau de l'eau qui monte dans la cale. */
        private float waterLevel() { return decorY(168f) - 40f + 210f * (float) Math.pow(progress(0.2f, 5.2f), 0.8f); }

        private float portholeX() { return decorX(PX); }
        private float portholeY() { return decorY(PY); }

        /** @return le point ({@code x}, {@code y}) de la cale penchée, à l'écran (la cale tourne autour du centre de l'écran). */
        private float screenX(float x, float y) {
            float a = list(), cx = worldWidth() / 2f, cy = worldHeight() / 2f;
            return cx + (x - cx) * MathUtils.cosDeg(a) - (y - cy) * MathUtils.sinDeg(a);
        }

        private float screenY(float x, float y) {
            float a = list(), cx = worldWidth() / 2f, cy = worldHeight() / 2f;
            return cy + (x - cx) * MathUtils.sinDeg(a) + (y - cy) * MathUtils.cosDeg(a);
        }

        @Override
        protected void step(float delta) {
            if (at(0.15f)) rumble(0.6f, 7f);                             // la coque heurte un récif
            if (at(LEAP)) rumble(0.2f, 5f);
            if (at(LEAP + 0.55f) && off) {                               // son chapeau tombe à l'eau
                for (int i = 0; i < 18; i++) {
                    Particle drop = particle(hatX + 50f, waterLevel(), 60f + 60f * random.nextFloat(), 300f + 300f * random.nextFloat(), 0.8f,
                        12f + 10f * random.nextFloat());
                    drop.gravity = 1400f;
                    drop.color.set(FOAM);
                    glows.add(drop);
                }
            }
            if (at(CLIMAX_DEATH)) {                                      // il passe le hublot
                rumble(0.4f, 8f);
                sparks(screenX(portholeX(), portholeY()), screenY(portholeX(), portholeY()), 30, FOAM, 500f, 14f);
            }
            if (time > GUSH && time < 5.0f) {                            // la mer entre par le hublot
                float x = screenX(portholeX(), portholeY()), y = screenY(portholeX(), portholeY());
                for (int i = 0; i < 3; i++) {
                    Particle water = particle(x + (random.nextFloat() - 0.5f) * 40f, y - PR * decorScale() * 0.5f, -10f - 30f * random.nextFloat(),
                        250f + 200f * random.nextFloat(), 1.1f, 18f + 12f * random.nextFloat());
                    water.gravity = 1300f;
                    water.color.set(random.nextBoolean() ? FOAM : WHITE);
                    water.color.a = 0.7f;
                    glows.add(water);
                }
            }
        }

        @Override
        protected void back(Batch batch) {
            fill(batch, Color.BLACK, 1f);
            beginTilt(batch, list());                                    // la cale gîte : le décor penche, et lui avec
            redrawDecor(this, batch, decorImage(), 1f);
            float px = portholeX(), py = portholeY(), s = decorScale(), r = PR * s;
            Color lit = litLikeDecor(WHITE, light);
            float open = Interpolation.pow2Out.apply(progress(OPEN, OPEN + 0.4f));
            if (open > 0f && glass != null) {                           // le vrai hublot s'ouvre sur sa charnière gauche
                draw(batch, outside, px, py, s, 0f, lit, 1f);
                float w = P_SIZE * s * MathUtils.cosDeg(78f * open), hinge = px - P_SIZE * s / 2f;
                stretch(batch, glass, hinge + w / 2f, py, s * MathUtils.cosDeg(78f * open), s, 0f, tmp.set(lit).mul(0.8f, 0.8f, 0.8f, 1f), 1f);
            }
            // ses rats filent le long du plancher, grimpent au mur et passent le hublot
            float floor = decorY(168f) + 24f;
            for (int i = 0; i < RATS; i++) {
                float t0 = OPEN + 0.2f + i * 0.13f, k = progress(t0, t0 + 0.8f);
                if (k <= 0f || k >= 1f) continue;
                float x, y, size = fit(62f, 11);
                if (k < 0.55f) {
                    float run = k / 0.55f;
                    x = MathUtils.lerp(worldWidth() * 0.72f + i * 30f, px + r * 0.2f, run);
                    y = floor + Math.abs(MathUtils.sin(time * 20f + i)) * 10f;
                } else {
                    float climb = (k - 0.55f) / 0.45f;
                    x = MathUtils.lerp(px + r * 0.2f, px, climb);
                    y = MathUtils.lerp(floor, py, Interpolation.pow2Out.apply(climb));
                    size *= 1f - 0.6f * climb;
                }
                draw(batch, rat, x, y, size, k < 0.55f ? 0f : -90f, tmp.set(lit).lerp(WHITE, 0.45f), 1f);
            }
            // lui : il tremble, s'accroupit, bondit vers le hublot (de plus en plus petit, au fond de la cale) et s'y faufile
            float k = progress(LEAP, CLIMAX_DEATH), squeeze = progress(CLIMAX_DEATH, GONE);
            float scale = MathUtils.lerp(1f, 0.24f, Interpolation.pow2Out.apply(k)) * (1f - squeeze);
            float cx = MathUtils.lerp(bossX(), px, k), cy = MathUtils.lerp(bossY(), py, k) + 260f * MathUtils.sin(k * MathUtils.PI);
            poseScale = Math.max(0.001f, scale);
            poseX = cx - bossX() + (time < LEAP ? (random.nextFloat() - 0.5f) * 6f * progress(0.3f, 1.0f) : 0f);
            poseY = cy - bossBottom() - bossHeight() * poseScale / 2f;
            poseStretch = -0.12f * gesture(LEAP - 0.3f, LEAP + 0.05f);
            poseTilt = 25f * MathUtils.sin(k * MathUtils.PI);
            showBoss = time < GONE;
            if (time >= LEAP && !off && hat != null) {                  // son vrai tricorne reste en l'air quand il bondit
                off = true;
                hatX = screenX(pieceX(hat), pieceY(hat));
                hatY = screenY(pieceX(hat), pieceY(hat));
                wear(bare);
            }
            float swim = progress(CLIMAX_DEATH + 0.2f, 4.4f);                    // par le hublot : il s'éloigne à la nage
            if (swim > 0f && swim < 1f) {
                float x = px - r * 0.15f + r * 0.6f * swim, y = py - 2f * s + MathUtils.sin(time * 6f) * 3f;
                draw(batch, rat, x, y, fit(r * 0.35f, 11), 0f, tmp.set(0.15f, 0.15f, 0.2f, 1f), 1f - progress(4.0f, 4.4f));
            }
        }

        @Override
        protected void front(Batch batch) {
            endTilt(batch);
            float level = waterLevel();
            if (off && hat != null) {                                    // le chapeau tombe, puis flotte
                float fall = progress(LEAP, LEAP + 0.55f);
                float x = hatX + 50f * fall - 60f * progress(LEAP + 0.55f, 5.5f);
                float rest = level + hat.height * bossScale() * 0.25f + MathUtils.sin(time * 3f) * 6f;
                float y = fall < 1f ? MathUtils.lerp(hatY, rest, Interpolation.pow2In.apply(fall)) : rest;
                float spin = fall < 1f ? 35f * MathUtils.sin(fall * MathUtils.PI2) : MathUtils.sin(time * 2f) * 8f;
                sea(this, batch, level, 0.75f);
                draw(batch, hat.region, x, y, bossScale(), spin, WHITE, 1f);
                if (fall >= 1f) rect(batch, SEA, 0.45f, x - hat.width * bossScale() / 2f - 10f, level - 30f, hat.width * bossScale() + 20f,
                    30f + 4f * MathUtils.sin(time * 3f));
            } else {
                sea(this, batch, level, 0.75f);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Tavernier
    // -------------------------------------------------------------------------

    /** Il fracasse une chope : les tabourets volent, une bagarre éclate en ombres chinoises. */
    static final class TavernPhase extends BossScene {
        private final TextureRegion mug, sailor, stool;
        private static final Color BEER = hex("ffc040"), LAMP = hex("ff9a3a");

        TavernPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.TAVERNIER, 2);
            mug = art("owbdg", new Color[] {hex("1a0e06"), hex("f4f0e0"), hex("ffb030"), hex("c87a10"), hex("8a6a3a")}, MUG);
            sailor = art("o", new Color[] {Color.WHITE}, SAILOR);
            stool = art("o", new Color[] {hex("6a4020")}, STOOL);
        }

        private float mugX() { return bossX() + bossWidth() * 0.6f; }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_PHASE)) {
                rumble(0.8f, 14f);
                for (int i = 0; i < 40; i++) {
                    Particle foam = particle(mugX(), groundY() + 60f, 20f + 140f * random.nextFloat(), 400f + 600f * random.nextFloat(),
                        0.9f, 18f + 14f * random.nextFloat());
                    foam.gravity = 1400f;
                    foam.color.set(random.nextBoolean() ? BEER : WHITE);
                    glows.add(foam);
                }
                spray(stool, bossX(), groundY() + 40f, 7, 30f, 150f, 1300f, fit(70f, 4), 1500f, 2.0f);
            }
        }

        @Override
        protected void back(Batch batch) {
            glow(batch, bossX(), worldHeight() * 0.85f, 600f, LAMP, 0.35f + 0.1f * MathUtils.sin(time * 8f));
            // la bagarre : des silhouettes noires qui se cognent derrière lui
            float fight = progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.4f);
            for (int i = 0; i < 6; i++) {
                if (fight <= 0f) break;
                float x = worldWidth() * (0.1f + i * 0.16f), bounce = Math.abs(MathUtils.sin(time * 9f + i * 1.3f)) * 50f;
                float lean = MathUtils.sin(time * 7f + i) * 18f;
                draw(batch, sailor, x, groundY() + 120f + bounce, fit(200f, 13), lean, Color.BLACK, 0.85f * fight);
                if (MathUtils.sin(time * 9f + i * 1.3f) > 0.95f) sparks(x, groundY() + 240f, 3, WHITE, 300f, 14f);
            }
            float raise = Interpolation.pow2Out.apply(progress(1.2f, 2.0f));
            float slam = Interpolation.pow3In.apply(progress(2.0f, CLIMAX_PHASE));
            poseY = 20f * raise * (1f - slam);
            poseTilt = -8f * raise * (1f - slam) + 4f * gesture(CLIMAX_PHASE, CLIMAX_PHASE + 0.3f);
        }

        @Override
        protected void front(Batch batch) {
            rect(batch, WOOD, 1f, -60f, groundY() - 10f, worldWidth() + 120f, 70f);   // le comptoir
            if (time < CLIMAX_PHASE) {                                    // la chope levée, puis abattue
                float raise = Interpolation.pow2Out.apply(progress(1.2f, 2.0f));
                float slam = Interpolation.pow3In.apply(progress(2.0f, CLIMAX_PHASE));
                float y = groundY() + 100f + 260f * raise * (1f - slam);
                draw(batch, mug, mugX(), y, fit(110f, 10), 0f, WHITE, 1f);
            }
            flash(batch, BEER, CLIMAX_PHASE, 0.3f, 0.45f);
        }
    }

    /**
     * « La taverne ferme ses portes » : il souffle les deux lanternes du décor, le feu de la cheminée tombe en braises ;
     * il salue, et les deux lourds battants de la taverne claquent devant lui ; la barre tombe en travers, la lumière
     * de la petite fenêtre s'éteint.
     */
    static final class TavernDeath extends BossScene {
        /** Les vitres des deux lanternes du décor de la taverne (centres, pixels du décor). */
        private static final float[][] LANTERNS = {{140f, 30.5f}, {226f, 30.5f}};
        private static final float[] BLOW = {0.6f, 1.25f};
        private static final float FIRE_OUT = 2.1f, SLAM = CLIMAX_DEATH, BAR = 3.45f, WINDOW_OUT = 4.3f;
        private static final int DOOR_W = 18, DOOR_H = 22;
        private static final Color FLAME = hex("ffb040"), EMBER = hex("ff5a1a"), GLASS_OFF = hex("3a2c1c"), HEARTH = hex("180e0b"),
            IRON = hex("2e2e36"), BAR_WOOD = hex("5a3418"), BAR_LIGHT = hex("7a4a24");
        private final TextureRegion doorLit, doorDark;
        private final Array<Particle> smoke = new Array<>(false, 64);
        private final Color light = new Color();

        TavernDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.TAVERNIER, 0);
            Color[] colors = {hex("1a0e06"), hex("6a4020"), hex("7e5028"), hex("3a2010"), IRON, hex("8a8a9a"), FLAME, hex("1a0e06")};
            doorLit = art("opqdirwb", colors, door());
            colors[6] = hex("24180c");
            doorDark = art("opqdirwb", colors, door());
        }

        /** Un battant de la porte : planches, deux ferrures cloutées, une petite fenêtre à barreaux, un anneau. */
        private static String[] door() {
            String[] rows = new String[DOOR_H];
            for (int y = 0; y < DOOR_H; y++) {
                StringBuilder row = new StringBuilder();
                for (int x = 0; x < DOOR_W; x++) {
                    char c;
                    if (x == 0 || x == DOOR_W - 1 || y == 0 || y == DOOR_H - 1) c = 'o';
                    else if (y == 4 || y == 5 || y == 16 || y == 17) c = (y == 4 || y == 16) && x % 4 == 2 ? 'r' : 'i';
                    else if (x >= 8 && x <= 15 && y >= 6 && y <= 12) c = x == 8 || x == 15 || y == 6 || y == 12 ? 'o' : x == 11 || x == 12 || y == 9 ? 'b' : 'w';
                    else if (x == 14 && y >= 13 && y <= 15) c = 'r';
                    else c = x % 4 == 3 ? 'd' : x % 4 == 0 ? 'q' : 'p';
                    row.append(c);
                }
                rows[y] = row.toString();
            }
            return rows;
        }

        @Override protected void restart() { smoke.clear(); }

        private float mouthX() { return portraitX(17f); }
        private float mouthY() { return portraitY(22f); }

        @Override
        protected void step(float delta) {
            for (int i = 0; i < LANTERNS.length; i++) {
                float lx = decorX(LANTERNS[i][0]), ly = decorY(LANTERNS[i][1]);
                if (time > BLOW[i] - 0.35f && time < BLOW[i] && random.nextFloat() < 0.7f) {   // son souffle file vers la lanterne
                    float k = random.nextFloat();
                    Particle breath = particle(MathUtils.lerp(mouthX(), lx, k), MathUtils.lerp(mouthY(), ly, k), 0f, 0f, 0.35f, 40f);
                    breath.color.set(0.9f, 0.9f, 1f, 0.35f);
                    glows.add(breath);
                }
                if (at(BLOW[i])) smoke(this, smoke, lx, ly + 30f, 6, 50f);
            }
            if (time > 0.3f && time < FIRE_OUT + 0.6f && random.nextFloat() < 0.25f) {         // les braises qui montent
                Particle ember = particle(decorX(316f) + (random.nextFloat() - 0.5f) * 120f, decorY(160f), 90f, 60f + 60f * random.nextFloat(),
                    1.4f, 10f);
                ember.color.set(EMBER);
                glows.add(ember);
            }
            if (at(FIRE_OUT)) smoke(this, smoke, decorX(316f), decorY(150f), 10, 120f);
            if (at(SLAM)) {
                rumble(0.6f, 14f);
                for (int i = 0; i < 24; i++) {                            // la poussière chassée par le claquement
                    Particle dust = particle(worldWidth() / 2f, worldHeight() * (0.2f + 0.7f * random.nextFloat()),
                        random.nextBoolean() ? 0f : 180f, 200f + 300f * random.nextFloat(), 0.9f, 60f + 40f * random.nextFloat());
                    dust.drag = 0.1f;
                    dust.color.set(0.55f, 0.45f, 0.35f, 0.5f);
                    glows.add(dust);
                }
            }
            if (at(BAR + 0.25f)) rumble(0.3f, 9f);
            update(smoke, delta);
        }

        /** @return la flamme de la cheminée, de 1 (vive) à 0 (des braises). */
        private float fire() { return 1f - Interpolation.pow2In.apply(progress(0.3f, FIRE_OUT)); }

        private int lit() {
            int count = 0;
            for (float blow : BLOW) if (time < blow) count++;
            return count;
        }

        @Override
        protected float decorLight() { return 0.62f - 0.08f * (LANTERNS.length - lit()) - 0.14f * (1f - fire()); }

        @Override
        protected void back(Batch batch) {
            for (int i = 0; i < LANTERNS.length; i++) {                  // les lanternes du décor, allumées puis soufflées
                float lx = decorX(LANTERNS[i][0]), ly = decorY(LANTERNS[i][1]);
                if (time < BLOW[i]) {
                    float flicker = 0.85f + 0.15f * MathUtils.sin(time * 23f + i * 3f) * (time > BLOW[i] - 0.35f ? 3f : 1f);
                    glow(batch, lx, ly, 200f * flicker, FLAME, 0.45f);
                    glow(batch, lx, ly, 40f, HOT, 0.8f);
                } else {
                    decorPaint(this, batch, GLASS_OFF, 1f, LANTERNS[i][0] - 3f, LANTERNS[i][1] - 5.5f, 6f, 11f);
                }
            }
            float fire = fire();                                         // le feu de la cheminée du décor tombe en braises
            decorPaint(this, batch, HEARTH, 1f - fire, 294f, 118f, 44f, 50f);
            float fx = decorX(316f), fy = decorY(158f);
            glow(batch, fx, fy + 40f * fire, 380f * (0.4f + 0.6f * fire), FLAME, 0.25f + 0.4f * fire * pulse(11f, 0.7f));
            glow(batch, fx, fy - 10f, 160f, EMBER, 0.5f * (1f - fire) * (1f - progress(3.6f, 5f)) * pulse(4f, 0.6f));
            drawSmoke(this, batch, smoke, 0.55f, 0.45f);
            // il se penche vers chaque lanterne pour la souffler, puis salue
            float lean = 0f;
            for (int i = 0; i < BLOW.length; i++) lean += (i == 0 ? -1f : 1f) * gesture(BLOW[i] - 0.5f, BLOW[i] + 0.15f);
            poseTilt = 7f * lean + 9f * gesture(1.7f, 2.4f);
            poseY = 10f * Math.abs(lean) - 14f * gesture(1.7f, 2.4f);
            poseShadow = 0.15f + 0.3f * (1f - fire);
        }

        @Override
        protected void front(Batch batch) {
            // les deux battants claquent : de profil au début, ils se rabattent jusqu'au milieu de l'écran
            float close = Interpolation.pow2In.apply(progress(SLAM - 0.45f, SLAM));
            if (time > SLAM) close = 1f - 0.05f * Math.abs(MathUtils.sin((time - SLAM) * 16f)) * Math.max(0f, 1f - (time - SLAM) * 2.5f);
            if (close <= 0f) return;
            float half = worldWidth() / 2f + MARGIN, height = worldHeight() + 2f * MARGIN, w = half * close;
            float sy = height / DOOR_H, sx = w / DOOR_W, y = worldHeight() / 2f;
            Color shade = light.set(0.8f, 0.78f, 0.75f, 1f);
            float window = 1f - progress(WINDOW_OUT, WINDOW_OUT + 0.25f);
            stretch(batch, doorDark, -MARGIN + w / 2f, y, sx, sy, 0f, shade, 1f);
            stretch(batch, doorDark, worldWidth() + MARGIN - w / 2f, y, -sx, sy, 0f, shade, 1f);
            stretch(batch, doorLit, -MARGIN + w / 2f, y, sx, sy, 0f, shade, window);
            stretch(batch, doorLit, worldWidth() + MARGIN - w / 2f, y, -sx, sy, 0f, shade, window);
            float windowY = worldHeight() + MARGIN - 9.5f * sy;          // la lueur de leurs fenêtres (rangées 7 à 11)
            glow(batch, -MARGIN + 12f * sx, windowY, 260f * close, FLAME, 0.4f * window);
            glow(batch, worldWidth() + MARGIN - 12f * sx, windowY, 260f * close, FLAME, 0.4f * window);
            // la barre tombe en travers dans ses ferrures
            if (time > SLAM + 0.3f) {
                float barY = worldHeight() * 0.42f;
                for (float side : new float[] {0.3f, 0.7f}) {
                    rect(batch, IRON, 1f, worldWidth() * side - 30f, barY - 60f, 60f, 120f);
                    rect(batch, Color.BLACK, 1f, worldWidth() * side - 30f, barY - 60f, 60f, 8f);
                }
                float drop = Interpolation.pow2In.apply(progress(BAR, BAR + 0.25f));
                float bounce = time > BAR + 0.25f ? 12f * MathUtils.sin(progress(BAR + 0.25f, BAR + 0.45f) * MathUtils.PI) : 0f;
                float by = MathUtils.lerp(worldHeight() + 100f, barY, drop) + bounce;
                rect(batch, Color.BLACK, 1f, worldWidth() * 0.16f - 6f, by - 36f, worldWidth() * 0.68f + 12f, 72f);
                rect(batch, BAR_WOOD, 1f, worldWidth() * 0.16f, by - 30f, worldWidth() * 0.68f, 60f);
                rect(batch, BAR_LIGHT, 1f, worldWidth() * 0.16f, by + 14f, worldWidth() * 0.68f, 12f);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Gardien du Phare
    // -------------------------------------------------------------------------

    /** La lanterne du phare du décor (centre de sa vitre, pixels du décor) et le haut de son toit. */
    static final float LAMP_X = 70.5f, LAMP_Y = 39f, ROOF_Y = 14f;

    /** @return le bord haut, puis le bord bas, du faisceau du phare du décor à la colonne {@code x} (pixels du décor). */
    static float beamTop(float x)    { return 35f - (x - 90f) / 7.7f; }
    static float beamBottom(float x) { return 41f + (x - 90f) / 7.75f; }

    /** Efface du décor du phare son faisceau fixe : le ciel (et ses étoiles, et la lune) réapparaît dessous. */
    static void removeBeam(Pixmap decor) {
        for (int x = 90; x < decor.getWidth(); x++) {
            int top = Math.max(0, MathUtils.floor(beamTop(x)) - 1), bottom = Math.min(128, MathUtils.ceil(beamBottom(x)) + 1);
            int above = sky(decor, x, top - 1, -1), below = sky(decor, x, bottom + 1, 1);
            if (above == 0) above = below;
            for (int y = top; y <= bottom; y++) {
                int c = decor.getPixel(x, y), r = c >>> 24 & 0xff, b = c >>> 8 & 0xff;
                if (r < 56 || r > 110 || Math.abs(r - b) >= 22) continue;          // une étoile, la lune : on les garde
                float k = (y - top + 1f) / (bottom - top + 2f);
                decor.drawPixel(x, y, mix(above, below, k));
            }
        }
    }

    /** @return le premier pixel de ciel (sans étoile ni faisceau) en partant de ({@code x}, {@code y}) dans le sens {@code dy}. */
    private static int sky(Pixmap decor, int x, int y, int dy) {
        for (; y >= 0 && y < decor.getHeight(); y += dy) {
            int c = decor.getPixel(x, y), r = c >>> 24 & 0xff, b = c >>> 8 & 0xff;
            if (b > r + 16 && r < 60) return c;
        }
        return 0;
    }

    private static int mix(int a, int b, float k) {
        int out = 0xff;
        for (int shift = 8; shift <= 24; shift += 8) {
            int ca = a >>> shift & 0xff, cb = b >>> shift & 0xff;
            out |= Math.round(ca + (cb - ca) * k) << shift;
        }
        return out;
    }

    /** Éteint la lanterne du phare du décor : sa vitre jaune devient sombre. */
    static void darkenLamp(Pixmap decor) {
        int glass = Color.rgba8888(hex("3c3428")), glint = Color.rgba8888(hex("5a5040"));
        for (int y = 28; y <= 48; y++) {
            for (int x = 50; x <= 90; x++) {
                int c = decor.getPixel(x, y), r = c >>> 24 & 0xff, b = c >>> 8 & 0xff;
                if (r > 180 && b < 170) decor.drawPixel(x, y, (x + y) % 9 == 0 ? glint : glass);
            }
        }
    }

    /**
     * « La tempête se lève » : les nuages couvrent le ciel du décor, la pluie tombe, la mer grossit et se brise sur le
     * phare du décor ; son faisceau se met à tourner de plus en plus vite et vire au rouge ; la foudre frappe le phare.
     */
    static final class LighthousePhase extends BossScene {
        private static final Color BEAM = hex("fff07a"), RED = hex("ff3a2a"), CLOUD = hex("0c0e16"), BOLT = hex("a0b4ff");
        private static final float[] BOLTS = {0.7f, 1.5f, CLIMAX_PHASE, 3.3f};
        private static final float[] WAVES = {0.5f, 1.2f, 1.85f, 2.5f, 3.3f};
        private final TextureRegion beamless;
        private final Color beam = new Color();
        private float angle;

        LighthousePhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.GARDIEN_PHARE, 2);
            Pixmap decor = decorPixmap(kind);
            if (decor != null) removeBeam(decor);
            beamless = decor == null ? null : region(decor, false);
        }

        @Override protected void restart() { angle = 0f; }

        private float storm() { return progress(0f, 1.4f); }
        private float lightning() {
            float light = 0f;
            for (float bolt : BOLTS) if (time >= bolt) light = Math.max(light, 1f - (time - bolt) / 0.25f);
            return light;
        }

        @Override
        protected float decorLight() { return 0.62f - 0.22f * storm() + 0.6f * lightning(); }

        @Override
        protected void step(float delta) {
            angle += delta * MathUtils.lerp(40f, 760f, Interpolation.pow2In.apply(progress(0.3f, CLIMAX_PHASE)));
            for (float bolt : BOLTS) if (at(bolt)) rumble(bolt == CLIMAX_PHASE ? 0.7f : 0.3f, bolt == CLIMAX_PHASE ? 14f : 8f);
            for (float wave : WAVES) {
                if (!at(wave)) continue;                                 // une vague se brise au pied du phare
                for (int i = 0; i < 34; i++) {
                    Particle spray = particle(decorX(55f + 30f * random.nextFloat()), decorY(130f), 60f + 60f * random.nextFloat(),
                        500f + 500f * random.nextFloat() * (0.6f + storm()), 1.2f, 20f + 18f * random.nextFloat());
                    spray.gravity = 1500f;
                    spray.color.set(random.nextBoolean() ? FOAM : WHITE);
                    glows.add(spray);
                }
            }
            if (at(CLIMAX_PHASE)) sparks(decorX(LAMP_X), decorY(ROOF_Y), 50, BOLT, 800f, 18f);
        }

        @Override
        protected void back(Batch batch) {
            redrawDecor(this, batch, beamless, 1f);                       // son faisceau fixe s'efface : c'est le nôtre qui tourne
            float storm = storm();
            for (int i = 0; i < 9; i++) {                                // les nuages d'orage
                float size = 520f + 80f * (i % 4);
                float x = ((i * 260f + time * (60f + 25f * (i % 3))) % (worldWidth() + 700f)) - 350f;
                float y = worldHeight() * (0.8f + 0.07f * (i % 3));
                batch.setColor(CLOUD.r, CLOUD.g, CLOUD.b, 0.8f * storm);
                batch.draw(soft, x - size / 2f, y - size * 0.3f, size, size * 0.6f);
            }
            for (int i = 0; i < BOLTS.length; i++) {
                float a = 1f - (time - BOLTS[i]) / 0.25f;
                if (time < BOLTS[i] || a <= 0f) continue;
                if (BOLTS[i] == CLIMAX_PHASE) {                          // la foudre frappe le toit du phare
                    float x = decorX(LAMP_X), y = decorY(ROOF_Y), length = worldHeight() + MARGIN - y;
                    additive(batch);
                    crack(batch, x, y, 95f, length, 7L, BOLT, 0.6f * a, 24f);
                    crack(batch, x, y, 95f, length, 7L, WHITE, a, 8f);
                    normal(batch);
                    glow(batch, x, y, 400f, WHITE, a);
                } else {
                    bolt(batch, worldWidth() * (0.45f + 0.17f * i), decorY(128f), 31L * (i + 1), a);
                }
            }
            rain(batch, 160, 0.35f * storm);
            for (int row = 0; row < 7; row++) {                          // la houle grossit sur la mer du décor
                float y = decorY(134f + row * 13f), amp = (6f + 14f * storm) * (1f + row * 0.15f);
                for (int k = 0; k < 12; k++) {
                    float x = ((k * 170f + row * 60f + time * (40f + row * 12f) * (1f + storm)) % (worldWidth() + 200f)) - 100f;
                    rect(batch, FOAM, 0.35f * storm, x - 28f, y + amp * MathUtils.sin(time * 2.5f + k + row), 56f, 6f);
                }
            }
            // le faisceau tourne : de profil il porte loin, de face il éblouit
            float s = decorScale(), lx = decorX(LAMP_X), ly = decorY(LAMP_Y);
            beam.set(BEAM).lerp(RED, progress(1.4f, CLIMAX_PHASE));
            float c = MathUtils.cosDeg(angle), side = c >= 0f ? 1f : -1f, along = Math.abs(c);
            float length = 300f * s * along, h0 = 3.5f * s, h1 = h0 + 35f * s * along;
            additive(batch);
            quad(this, batch, beam, 0.3f, lx, ly - h0, lx + side * length, ly - h1, lx + side * length, ly + h1, lx, ly + h0);
            normal(batch);
            glow(batch, lx, ly, 160f + 520f * (1f - along), beam, 0.45f + 0.45f * (1f - along));
            poseShadow = 0.2f * storm;
        }

        @Override
        protected void front(Batch batch) {
            float x = portraitX(4.5f), y = portraitY(15.5f);             // sa lanterne rougit avec le phare
            glow(batch, x, y, bossScale() * 16f, beam, 0.7f);
            glow(batch, x, y, bossScale() * 4f, WHITE, 0.8f);
            rain(batch, 60, 0.5f * storm());
            flash(batch, RED, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /**
     * « Le phare s'éteint » : le faisceau du phare du décor faiblit et bégaie, la lanterne de son gardien vacille avec
     * lui ; puis tout s'éteint d'un coup : la vitre du phare devient noire, une fumée monte du phare et de sa lanterne,
     * il ne reste que la lune ; le gardien baisse la tête, une dernière braise rougit dans sa lanterne et meurt.
     */
    static final class LighthouseDeath extends BossScene {
        private static final Color BEAM = hex("fff07a"), EMBER = hex("ff6a2a");
        private static final float[][] SPUTTER = {{0.9f, 1.0f}, {1.45f, 1.6f}, {1.95f, 2.05f}, {2.2f, 2.34f}};
        private final TextureRegion unlit, lampOff;
        private final Array<Particle> towerSmoke = new Array<>(false, 64), lampSmoke = new Array<>(false, 64);

        LighthouseDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.GARDIEN_PHARE, 0);
            Pixmap decor = decorPixmap(kind);
            if (decor != null) {
                removeBeam(decor);
                darkenLamp(decor);
            }
            unlit = decor == null ? null : region(decor, false);
            lampOff = pixels(lampOff(PlacesPortraits.pixels(kind)));
        }

        /** @return ses pixels de lanterne (la vitre et sa lumière, colonnes 0 à 9), éteints ; le reste vide. */
        private static int[][] lampOff(int[][] full) {
            int glass = Color.rgba8888(hex("fff6a0")), light = Color.rgba8888(hex("ffe060"));
            int darkGlass = Color.rgba8888(hex("4a4234")), darkLight = Color.rgba8888(hex("3a3226"));
            int[][] grid = new int[full.length][full[0].length];
            for (int y = 8; y <= 20 && y < full.length; y++) {
                for (int x = 0; x <= 9; x++) {
                    if (full[y][x] == glass) grid[y][x] = darkGlass;
                    else if (full[y][x] == light) grid[y][x] = darkLight;
                }
            }
            return grid;
        }

        @Override protected void restart() { towerSmoke.clear(); lampSmoke.clear(); }

        private boolean on() {
            if (time >= CLIMAX_DEATH) return false;
            for (float[] s : SPUTTER) if (time >= s[0] && time < s[1]) return false;
            return true;
        }

        /** @return le faisceau qui faiblit avant de s'éteindre (0 : plein, 1 : éteint). */
        private float dim() { return on() ? 0.45f * progress(0.3f, CLIMAX_DEATH) : 1f; }

        @Override
        protected float decorLight() { return 0.62f - 0.2f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.6f); }

        @Override
        protected void step(float delta) {
            float tx = decorX(LAMP_X), ty = decorY(LAMP_Y - 6f);
            for (float[] s : SPUTTER) if (at(s[0])) rumble(0.08f, 3f);
            if (at(CLIMAX_DEATH)) {
                rumble(0.35f, 5f);
                smoke(this, towerSmoke, tx, ty, 14, 90f);
                smoke(this, lampSmoke, portraitX(4.5f), portraitY(10f), 6, 34f);
            }
            if (time > CLIMAX_DEATH && time < 4.6f && random.nextFloat() < 0.15f) {
                smoke(this, towerSmoke, tx, ty, 1, 70f);
                smoke(this, lampSmoke, portraitX(4.5f), portraitY(10f), 1, 24f);
            }
            update(towerSmoke, delta);
            update(lampSmoke, delta);
        }

        @Override
        protected void back(Batch batch) {
            float dim = dim();
            redrawDecor(this, batch, unlit, dim);                          // le vrai faisceau faiblit, bégaie, s'éteint
            if (on()) glow(batch, decorX(LAMP_X), decorY(LAMP_Y), 260f, BEAM, 0.5f * (1f - dim));
            drawSmoke(this, batch, towerSmoke, 0.22f, 0.45f);
            poseShadow = 0.6f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 1.2f);
            float bow = Interpolation.pow2.apply(progress(3.0f, 4.2f));  // il baisse la tête
            poseTilt = -5f * bow;
            poseY = -14f * bow;
        }

        @Override
        protected void front(Batch batch) {
            float x = portraitX(4.5f), y = portraitY(15.5f);
            if (on()) {                                                  // sa lanterne vacille avec le phare
                glow(batch, x, y, bossScale() * 16f, BEAM, 0.7f * (1f - 0.5f * dim()));
            } else {
                onPortrait(batch, lampOff, 0f, 0f, 1f, 1f);
                if (time > CLIMAX_DEATH) {                               // une dernière braise dans la mèche
                    float ember = (1f - progress(CLIMAX_DEATH, 4.3f)) * pulse(7f, 0.5f);
                    glow(batch, x, portraitY(16.5f), bossScale() * 4f, EMBER, 0.9f * ember);
                }
            }
            drawSmoke(this, batch, lampSmoke, 0.4f, 0.5f);
        }
    }

    // -------------------------------------------------------------------------
    // Capitaine Noir
    // -------------------------------------------------------------------------

    /** Une bordée de canons par les sabords du bastingage du décor : la fumée couvre l'écran, seul son œil brille. */
    static final class BlackPhase extends BossScene {
        private static final Color FIRE = hex("ffb040"), EYE = hex("9a5aff"), IRON = hex("141016");
        private static final int GUNS = 7;

        private final Array<Particle> smoke = new Array<>(false, 128);

        BlackPhase(CutsceneKit kit) { super(kit, Moment.PHASE, EnemyKind.CAPITAINE_NOIR, 2); }

        @Override protected void restart() { smoke.clear(); }

        private float shot(int i) { return 0.6f + i * 0.2f; }
        /** Les canons sortent par les jours du bastingage du décor (un sur deux, pixels du décor). */
        private float gunPx(int i) { return 74.5f + 32f * i; }
        private static final float GUN_PY = 134.5f;

        @Override
        protected void step(float delta) {
            for (int i = 0; i < GUNS; i++) {
                if (at(shot(i))) {                                      // un canon tire : flamme et fumée
                    float x = decorX(gunPx(i)), y = decorY(GUN_PY);
                    rumble(0.15f, 7f);
                    sparks(x, y, 20, FIRE, 700f, 22f);
                    for (int k = 0; k < 10; k++) {
                        Particle puff = particle(x, y, 60f + 60f * random.nextFloat(), 120f + 260f * random.nextFloat(),
                            3.6f, 260f + 200f * random.nextFloat());
                        puff.drag = 0.4f;
                        smoke.add(puff);
                    }
                }
            }
            if (at(CLIMAX_PHASE)) rumble(0.6f, 10f);
            update(smoke, delta);
        }

        @Override
        protected void back(Batch batch) {
            for (int i = 0; i < GUNS; i++) {                             // les gueules des canons, dans les jours du bastingage
                float recoil = 1.5f * gesture(shot(i), shot(i) + 0.25f);
                decorPaint(this, batch, IRON, 1f, gunPx(i) - 3.5f, GUN_PY - 3.5f + recoil, 7f, 7f);
            }
            poseShadow = 0.9f * progress(1.6f, CLIMAX_PHASE);
        }

        @Override
        protected void front(Batch batch) {
            // la fumée (dessinée par les particules), puis seul son œil
            float k = progress(CLIMAX_PHASE - 0.2f, CLIMAX_PHASE + 0.3f);
            for (Particle puff : smoke) {
                float size = puff.size * (1f + 1.5f * puff.life());
                batch.setColor(0.32f, 0.32f, 0.36f, 0.8f * (1f - puff.life() * puff.life()));
                batch.draw(soft, puff.x - size / 2f, puff.y - size / 2f, size, size);
            }
            fill(batch, hex("1a1a22"), 0.75f * progress(1.4f, CLIMAX_PHASE));
            if (k > 0f) {
                float x = portraitX(21f), y = portraitY(16f);                 // son œil, à côté du bandeau
                float pulse = 0.8f + 0.2f * MathUtils.sin(time * 6f);
                glow(batch, x, y, bossScale() * 14f * k * pulse, EYE, 0.8f);
                glow(batch, x, y, bossScale() * 2.5f * k, WHITE, 1f);
            }
            flash(batch, FIRE, 0.6f, 0.15f, 0.3f);
        }
    }

    /**
     * « Le Capitaine Noir sombre avec son navire » : la mer monte sur le pont du décor ; le galion (le vrai décor : son
     * pont, ses mâts, ses voiles à tête de mort) gîte et s'enfonce, son capitaine debout dessus jusqu'au bout ; quand
     * l'eau atteint sa tête, son vrai tricorne (celui de son portrait) se détache et reste à flotter ; les mâts, le
     * pavillon noir disparaissent en dernier dans un remous de bulles.
     */
    static final class BlackDeath extends BossScene {
        private static final Color SKY_TOP = hex("15112a"), SKY_LOW = hex("3f2d4b"), LANTERN = hex("ffc060");
        /** Les lanternes du décor du galion (pixels du décor). */
        private static final float[][] LAMPS = {{120f, 40f}, {240f, 40f}};
        private final TextureRegion bare;
        private final Piece hat;
        private final Matrix4 saved = new Matrix4(), sunk = new Matrix4();
        private final Color low = new Color(), top = new Color();
        private float hatX, hatY, hatAt;
        private boolean off;

        BlackDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.CAPITAINE_NOIR, 0);
            int[][] rest = PlacesPortraits.without(EnemyKind.CAPITAINE_NOIR, PlacesPortraits.Prop.NOIR_HAT);
            hat = lift(PlacesPortraits.pixels(EnemyKind.CAPITAINE_NOIR), rest);
            bare = pixels(rest);
        }

        @Override protected void restart() { off = false; }

        private float sink() { return 720f * Interpolation.pow2In.apply(progress(0.5f, 4.4f)); }
        private float list() { return 7f * Interpolation.pow2.apply(progress(0.4f, 3.8f)); }
        private float seaLevel() { return -MARGIN + (worldHeight() * 0.42f + MARGIN) * Interpolation.pow2Out.apply(progress(0.2f, 3.0f)); }
        private float pivotX() { return worldWidth() / 2f; }
        private float pivotY() { return worldHeight() * 0.3f; }

        /** @return le point ({@code x}, {@code y}) du navire, à l'écran (penché, enfoncé). */
        private float shipX(float x, float y) {
            float a = list();
            return pivotX() + (x - pivotX()) * MathUtils.cosDeg(a) - (y - pivotY()) * MathUtils.sinDeg(a);
        }

        private float shipY(float x, float y) {
            float a = list();
            return pivotY() + (x - pivotX()) * MathUtils.sinDeg(a) + (y - pivotY()) * MathUtils.cosDeg(a) - sink();
        }

        @Override
        protected void step(float delta) {
            if (at(0.4f)) rumble(1.0f, 6f);
            if (at(CLIMAX_DEATH)) rumble(0.5f, 9f);
            if (time > 0.6f && time < 4.8f && random.nextFloat() < 0.5f) {   // les remous où le navire s'enfonce
                Particle bubble = particle(worldWidth() * (0.1f + 0.8f * random.nextFloat()), seaLevel() - 10f, 90f,
                    60f + 80f * random.nextFloat(), 0.6f, 10f + 12f * random.nextFloat());
                bubble.color.set(FOAM);
                glows.add(bubble);
            }
            if (at(3.9f)) {                                              // les mâts passent sous l'eau
                rumble(0.4f, 6f);
                for (int i = 0; i < 40; i++) {
                    Particle drop = particle(shipX(worldWidth() * (0.15f + 0.7f * random.nextFloat()), worldHeight()), seaLevel(),
                        60f + 60f * random.nextFloat(), 300f + 400f * random.nextFloat(), 0.9f, 14f + 12f * random.nextFloat());
                    drop.gravity = 1500f;
                    drop.color.set(FOAM);
                    glows.add(drop);
                }
            }
        }

        @Override
        protected void back(Batch batch) {
            gradient(batch, litLikeDecor(SKY_LOW, low), litLikeDecor(SKY_TOP, top));   // le ciel derrière les mâts
            for (int i = 0; i < 40; i++) {
                float x = (i * 397.3f) % worldWidth(), y = worldHeight() * 0.35f + (i * 211.7f) % (worldHeight() * 0.65f);
                rect(batch, WHITE, 0.35f + 0.25f * MathUtils.sin(time * 2f + i), x, y, 4f, 4f);
            }
            saved.set(batch.getTransformMatrix());                       // le navire : le vrai décor, penché et enfoncé
            sunk.set(saved).translate(0f, -sink(), 0f).translate(pivotX(), pivotY(), 0f).rotate(0f, 0f, 1f, list())
                .translate(-pivotX(), -pivotY(), 0f);
            batch.setTransformMatrix(sunk);
            redrawDecor(this, batch, decorImage(), 1f);
            for (float[] lamp : LAMPS) {                                 // ses lanternes, tant qu'elles sont hors de l'eau
                float x = decorX(lamp[0]), y = decorY(lamp[1]);
                if (shipY(x, y) > seaLevel()) glow(batch, x, y, 160f, LANTERN, 0.45f * pulse(9f, 0.8f));
            }
            if (!off && hat != null) {                                   // l'eau atteint sa tête : son vrai tricorne se détache
                float hx = pieceX(hat), bottom = portraitY(hat.top + hat.height);
                if (shipY(hx, bottom) < seaLevel() + 10f) {
                    off = true;
                    hatX = shipX(hx, pieceY(hat));
                    hatY = shipY(hx, pieceY(hat));
                    hatAt = time;
                    wear(bare);
                    sparks(hatX, seaLevel(), 24, FOAM, 400f, 14f);
                }
            }
            poseShadow = 0.3f;
        }

        @Override
        protected void front(Batch batch) {
            batch.setTransformMatrix(saved);
            float level = seaLevel();
            sea(this, batch, level, 0.86f);
            if (off && hat != null) {                                    // le tricorne flotte et dérive
                float k = progress(hatAt, hatAt + 3f), up = Interpolation.pow2Out.apply(progress(hatAt, hatAt + 0.3f));
                float x = hatX + 160f * Interpolation.pow2Out.apply(k);
                float y = MathUtils.lerp(hatY, level + hat.height * bossScale() * 0.2f + MathUtils.sin(time * 3f) * 6f, up);
                draw(batch, hat.region, x, y, bossScale(), list() * (1f - up) + MathUtils.sin(time * 2f) * 7f, WHITE, 1f);
            }
        }
    }
}
