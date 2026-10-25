package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;

/** Les scènes de phase et de mort des quatre rois du Désert de Braise (choix d'Astra, 2026-10-24). */
final class DesertBossScenes {

    private DesertBossScenes() {}

    static BossScene phase(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case EMIR -> new EmirPhase(kit);
            case PHARAON -> new PharaohPhase(kit);
            case DJINN -> new DjinnPhase(kit);
            case DRAGON -> new DragonPhase(kit);
            default -> null;
        };
    }

    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case EMIR -> new EmirDeath(kit);
            case PHARAON -> new PharaohDeath(kit);
            case DJINN -> new DjinnDeath(kit);
            case DRAGON -> new DragonDeath(kit);
            default -> null;
        };
    }

    // ----- Accessoires -----

    static final Color SAND = c("e8b86a"), SAND_DARK = c("b8843a"), LAVA = c("ff6a1e"), EMBER = c("ffb84a"),
        GREEN = c("6aff6a"), PURPLE = c("a05aff");
    static final String[] CAMEL = {
        "...........oo..", "..........oyyo.", "...oo.....oyo..", "..oyyo...oyo...", ".oyyyyoooyyo...",
        "oyyyyyyyyyyo...", "oyyyyyyyyyo....", ".oyyyyyyyo.....", ".oyo..oyo......", ".oyo..oyo......", ".oo...oo......."};
    static final String[] SCARAB = {".o..o.", "..oo..", "oggggo", ".gggg.", "oggggo", ".o..o."};
    static final String[] LAMP = {
        "....oo......", "...oggo.....", "..oggggo...o", "ooggggggoooo", "oggggggggo..", ".oggggggo...", "..oooooo...."};

    static final String[] INGOT = {"..oooooo..", ".owyyyyyo.", "oyyyyyyyyo", "oddddddddo", "oooooooooo"};
    static final String[] GEM = {".ooooo.", "obwwbbo", "obbbbbo", ".obbbo.", "..obo..", "...o..."};
    static final String[] CROWN = {"y...y...y", "yy.yry.yy", "yyyyyyyyy", "ybyyryyby", "ddddddddd"};
    static final String[] CUP = {"ooooooo", "oyywyyo", ".oyyyo.", "..oyo..", "...y...", "..yyy..", ".ddddd."};
    static final String[] RUBY = {".ooo.", "orrwo", "orrro", ".oro.", "..o.."};

    static Color c(String hex) { return Color.valueOf(hex); }

    /**
     * Le couvercle du sarcophage du Pharaon, debout : un corps d'or, la coiffe rayée bleu et or, le masque
     * et sa barbe, des bandes en bas. Centré en {@code x}, posé sur {@code bottom}, {@code w} x {@code h}.
     */
    static void sarcophagus(BossScene scene, Batch batch, float x, float bottom, float w, float h, float alpha) {
        if (alpha <= 0f) return;
        Color line = c("1a0e06"), gold = c("c4891e"), light = c("ffd54a"), blue = c("1a3a8e");
        float headW = w * 0.8f, headY = bottom + h * 0.6f;
        scene.rect(batch, line, alpha, x - w / 2f - 6f, bottom - 6f, w + 12f, h * 0.66f + 12f);
        scene.rect(batch, line, alpha, x - headW / 2f - 6f, headY - 6f, headW + 12f, h * 0.4f + 12f);
        scene.rect(batch, gold, alpha, x - w / 2f, bottom, w, h * 0.66f);
        for (int k = 0; k < 7; k++) {                                                        // les bandes du bas
            scene.rect(batch, k % 2 == 0 ? blue : light, alpha, x - w / 2f, bottom + k * h * 0.035f, w, h * 0.035f);
        }
        for (int k = 0; k < 8; k++) {                                                        // la coiffe rayée
            scene.rect(batch, k % 2 == 0 ? blue : light, alpha, x - headW / 2f + k * headW / 8f, headY, headW / 8f, h * 0.4f);
        }
        float faceW = w * 0.38f, faceY = bottom + h * 0.7f;
        scene.rect(batch, c("e8b84a"), alpha, x - faceW / 2f, faceY, faceW, h * 0.22f);           // le masque
        scene.rect(batch, line, alpha, x - faceW * 0.32f, faceY + h * 0.13f, faceW * 0.18f, h * 0.025f);
        scene.rect(batch, line, alpha, x + faceW * 0.14f, faceY + h * 0.13f, faceW * 0.18f, h * 0.025f);
        scene.rect(batch, line, alpha, x - faceW * 0.15f, faceY + h * 0.05f, faceW * 0.3f, h * 0.012f);
        scene.rect(batch, light, alpha, x - w * 0.05f, faceY - h * 0.1f, w * 0.1f, h * 0.1f);   // la barbe
        scene.rect(batch, c("2a8a6a"), alpha, x - w / 2f, bottom + h * 0.52f, w, h * 0.05f);    // le collier
        scene.rect(batch, light, alpha, x - w / 2f, bottom + h * 0.5f, w, h * 0.02f);
    }

    /** Des traînées de sable qui filent à l'horizontale ({@code alpha} : force de la tempête). */
    static void sandstorm(BossScene scene, Batch batch, float alpha) {
        if (alpha <= 0f) return;
        float width = scene.worldWidth() + 400f;
        for (int i = 0; i < 90; i++) {
            float x = ((i * 173.3f + scene.time * (900f + (i % 7) * 120f)) % width) - 200f;
            float y = (i * 61.7f) % scene.worldHeight();
            scene.rect(batch, SAND, 0.5f * alpha, x, y, 60f + (i % 5) * 20f, 4f);
        }
        scene.fill(batch, SAND_DARK, 0.25f * alpha);
    }

    // -------------------------------------------------------------------------
    // Émir des sables
    // -------------------------------------------------------------------------

    /** La tempête se lève ; le contrat se déchire en créances qui tournent en tornade ; ses yeux d'or ; son ombrelle devient un cimeterre. */
    static final class EmirPhase extends BossScene {
        private final TextureRegion bill;

        EmirPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.EMIR, 2);
            bill = symbol(EnemySymbol.BILL);
        }

        @Override
        protected void step(float delta) {
            if (at(0.3f)) rumble(1.6f, 4f);
            if (at(1.0f)) sparks(bossX(), bossY(), 30, c("f0e2b8"), 500f, 10f);
            if (at(CLIMAX_PHASE)) { rumble(0.5f, 12f); sparks(bossX(), bossY(), 50, GOLD, 700f, 14f); }
        }

        @Override
        protected void back(Batch batch) {
            sandstorm(this, batch, progress(0.2f, 1.0f));
        }

        @Override
        protected void front(Batch batch) {
            float torn = progress(1.0f, 1.3f);
            for (int i = 0; i < 24 && torn > 0f; i++) {                                      // la tornade de créances
                float h = (i / 24f + time * 0.3f) % 1f, a = time * 400f + i * 47f, r = bossWidth() * (0.3f + 0.6f * h) * torn;
                float x = bossX() + MathUtils.cosDeg(a) * r, y = bossBottom() + h * bossHeight() * 1.3f;
                draw(batch, bill, x, y, iconScale(0.05f), a, WHITE, torn);
            }
            float blade = progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.25f);                      // le cimeterre
            if (blade > 0f) {
                float x0 = bossX() - bossWidth() * 0.55f, y0 = bossY();
                for (int k = 0; k < 8; k++) {
                    float t0 = k / 8f, t1 = (k + 1) / 8f;
                    line(batch, c("dce4ee"), blade, x0 + 40f * MathUtils.sin(t0 * 2f), y0 + t0 * 360f * blade,
                        x0 + 40f * MathUtils.sin(t1 * 2f), y0 + t1 * 360f * blade, 22f - k * 2f);
                }
            }
            eyes(batch, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.2f), GOLD, new float[] {14f, 16f}, new float[] {21f, 16f});
            flash(batch, GOLD, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /** La tempête tombe ; les créances retombent en neige et brûlent en pièces d'or ; il s'enfonce dans la dune ; son ombrelle reste, les chameaux s'en vont. */
    static final class EmirDeath extends BossScene {
        private final TextureRegion bill, camel;

        EmirDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.EMIR, 0);
            bill = symbol(EnemySymbol.BILL);
            camel = art("oy", new Color[] {c("3a2412"), c("a8784a")}, CAMEL);
        }

        @Override
        protected void step(float delta) {
            if (bill != null && time < 2.4f && random.nextFloat() < 0.5f) {                   // la neige de créances
                Particle note = place(bill, worldWidth() * random.nextFloat(), worldHeight() + 40f, 0f, -220f, iconScale(0.04f), 2.4f);
                note.spin = 90f;
            }
            if (at(CLIMAX_DEATH)) {
                for (Particle note : sprites) if (note.region == bill) note.region = coin;    // elles brûlent en pièces d'or
                sparks(worldWidth() / 2f, worldHeight() / 2f, 60, GOLD, 900f, 12f);
                rumble(0.5f, 8f);
            }
        }

        @Override
        protected void back(Batch batch) {
            sandstorm(this, batch, 1f - progress(0f, 1.2f));
            poseSink = Interpolation.pow2In.apply(progress(CLIMAX_DEATH, 3.8f));
            poseY = -bossHeight() * 0.1f * poseSink;
        }

        @Override
        protected void front(Batch batch) {
            for (int x = -40; x < worldWidth() + 40; x += 20) {                              // la dune qui l'avale
                float y = groundY() + 60f + 30f * MathUtils.sin(x / 200f);
                rect(batch, SAND, 1f, x, -MARGIN, 22f, y + MARGIN);
            }
            float stay = progress(3.6f, 3.9f);                                              // l'ombrelle plantée
            if (stay > 0f) {
                float x = bossX() + 60f, y = groundY() + 60f;
                line(batch, c("6e4524"), 1f, x, y, x, y + 300f, 10f);
                for (int k = 0; k < 9; k++) {
                    line(batch, k % 2 == 0 ? c("e0283a") : GOLD, 1f, x, y + 300f, x + MathUtils.cosDeg(160f - k * 17.5f) * 200f,
                        y + 260f + MathUtils.sinDeg(160f - k * 17.5f) * 60f, 26f);
                }
            }
            float leave = progress(3.9f, 5.6f);                                              // les chameaux s'en vont
            for (int k = 0; k < 3; k++) {
                draw(batch, camel, worldWidth() * (0.62f + 0.12f * k) + leave * worldWidth() * 0.5f, groundY() + 120f, fit(140f, 11), 0f,
                    WHITE, 1f);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Pharaon maudit
    // -------------------------------------------------------------------------

    /**
     * Des rouleaux de machine à sous apparaissent sur son sarcophage, un levier sort sur le côté ; il le tire :
     * les rouleaux tournent et s'arrêtent un à un sur son symbole, le Venin. Ses yeux verts s'allument.
     */
    static final class PharaohPhase extends BossScene {
        /** Les rouleaux s'arrêtent un à un, le dernier à l'apogée. */
        private static final float REELS_IN = 0.4f, LEVER_OUT = 0.8f, PULL = 1.1f, SPIN = 1.35f;
        private static final float[] STOPS = {1.75f, 2.0f, CLIMAX_PHASE};
        private final TextureRegion[] symbols;
        private final int venom;

        PharaohPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.PHARAON, 2);
            EnemySymbol[] faces = {EnemySymbol.SWORD, EnemySymbol.WRAP, EnemySymbol.POTION, EnemySymbol.THORNS, EnemySymbol.VENOM};
            venom = 4;
            TextureRegion[] loaded = new TextureRegion[faces.length];
            for (int i = 0; i < faces.length; i++) loaded[i] = symbol(faces[i]);
            symbols = loaded[0] == null ? null : loaded;
        }

        private float caseW() { return bossWidth() * 0.85f; }
        private float caseH() { return bossHeight() * 1.12f; }
        private float caseX() { return bossX() - bossWidth() * 1.05f; }
        private float window() { return caseW() * 0.24f; }
        private float reelX(int r) { return caseX() + (r - 1) * caseW() * 0.29f - window() / 2f; }
        private float reelY() { return bossBottom() + caseH() * 0.3f; }
        private float leverX() { return caseX() + caseW() / 2f + 8f; }
        private float leverY() { return bossBottom() + caseH() * 0.42f; }
        /** Le levier : dressé (70°), tiré vers le bas (-35°) puis il remonte. */
        private float leverAngle() {
            return MathUtils.lerp(70f, -35f, gesture(PULL, PULL + 0.6f));
        }

        @Override
        protected void step(float delta) {
            if (at(REELS_IN)) sparks(caseX(), reelY() + window() / 2f, 30, GREEN, 400f, 10f);
            if (at(PULL + 0.3f)) rumble(0.2f, 6f);
            for (int r = 0; r < 3; r++) {
                if (!at(STOPS[r])) continue;
                rumble(0.1f, 4f);
                sparks(reelX(r) + window() / 2f, reelY() + window() / 2f, 14, GREEN, 300f, 8f);
            }
            if (at(CLIMAX_PHASE)) { rumble(0.6f, 14f); sparks(caseX(), reelY(), 50, GREEN, 700f, 14f); }
            if (time > CLIMAX_PHASE && time < 4.2f && random.nextFloat() < 0.5f) {           // le venin coule des rouleaux
                int r = random.nextInt(3);
                Particle drop = particle(reelX(r) + window() * random.nextFloat(), reelY(), 270f, 60f, 1.4f, 14f);
                drop.gravity = 500f;
                drop.color.set(GREEN);
                glows.add(drop);
            }
        }

        @Override
        protected void back(Batch batch) {
            float pull = gesture(PULL, PULL + 0.6f);
            poseX = -30f * pull;                                                             // il se penche sur le levier
            poseTilt = 4f * pull;
            poseTint = 0.35f * progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.4f);
            poseTintColor.set(GREEN);
            sarcophagus(this, batch, caseX(), bossBottom(), caseW(), caseH(), 1f);
            float show = progress(REELS_IN, REELS_IN + 0.4f);                                // les rouleaux apparaissent
            if (show > 0f && symbols != null) {
                float w = window();
                rect(batch, c("1a0e06"), show, reelX(0) - 14f, reelY() - 14f, reelX(2) - reelX(0) + w + 28f, w + 28f);
                for (int r = 0; r < 3; r++) {
                    float offset;
                    if (time < SPIN) offset = venom + 1f;                                       // au repos, sur la Potion
                    else if (time >= STOPS[r]) offset = venom + 5f * (r + 2);
                    else offset = venom + 5f * (r + 2) - (STOPS[r] - time) * 16f;
                    rect(batch, c("f0e2b8"), show, reelX(r) - 4f, reelY() - 4f, w + 8f, w + 8f);
                    batch.setColor(1f, 1f, 1f, show);
                    reel(batch, symbols, reelX(r), reelY(), w, w, offset);
                    if (time >= STOPS[r]) glow(batch, reelX(r) + w / 2f, reelY() + w / 2f, w * 2.2f, GREEN, 0.5f * pulse(9f, 0.5f));
                }
            }
            float lever = progress(LEVER_OUT, LEVER_OUT + 0.3f);                             // le levier sort du flanc
            if (lever > 0f) {
                float a = leverAngle(), length = caseH() * 0.36f * lever;
                float ex = leverX() + MathUtils.cosDeg(a) * length, ey = leverY() + MathUtils.sinDeg(a) * length;
                rect(batch, c("1a0e06"), 1f, leverX() - 14f, leverY() - 22f, 28f, 44f);
                line(batch, c("8a97a8"), 1f, leverX(), leverY(), ex, ey, 14f);
                rect(batch, c("1a0e06"), 1f, ex - 22f, ey - 22f, 44f, 44f);
                rect(batch, c("c8102e"), 1f, ex - 18f, ey - 18f, 36f, 36f);
            }
        }

        @Override
        protected void front(Batch batch) {
            eyes(batch, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.2f), GREEN, new float[] {14f, 15f}, new float[] {21f, 15f});
            flash(batch, GREEN, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /** Debout devant son sarcophage ouvert, il se fend, puis part en poussière ; le sarcophage se referme sur le vide, les torches s'éteignent. */
    static final class PharaohDeath extends BossScene {
        private static final float CLOSE = 3.9f;
        /** Ses pixels : place dans le portrait, couleur, départ (secondes après l'apogée), vitesse. */
        private final int[][] grid;
        private final float[] delay, speedX, speedY;

        PharaohDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.PHARAON, 0);
            grid = fr.astratime.lucky.assets.FairDesertPortraits.pixels(EnemyKind.PHARAON);
            int w = grid[0].length, h = grid.length;
            delay = new float[w * h];
            speedX = new float[w * h];
            speedY = new float[w * h];
            java.util.Random seed = new java.util.Random(5);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {                                                // il s'effrite du haut vers le bas
                    int i = y * w + x;
                    delay[i] = 1.0f * y / h + 0.3f * seed.nextFloat();
                    speedX[i] = 120f + 260f * seed.nextFloat();
                    speedY[i] = 40f + 160f * seed.nextFloat();
                }
            }
        }

        private float caseW() { return bossWidth() * 1.25f; }
        private float caseH() { return bossHeight() * 1.18f; }

        @Override
        protected void step(float delta) {
            if (at(0.8f)) rumble(0.3f, 8f);
            if (at(CLIMAX_DEATH)) { rumble(0.4f, 8f); sparks(bossX(), bossY(), 30, SAND, 400f, 12f); }
            if (at(CLOSE + 0.35f)) { rumble(0.6f, 14f); sparks(bossX(), bossBottom() + caseH() * 0.5f, 40, SAND, 600f, 14f); }
        }

        @Override
        protected void back(Batch batch) {
            for (int t = 0; t < 4; t++) {                                                    // les torches, qui s'éteignent une à une
                float x = worldWidth() * (t < 2 ? 0.08f + 0.12f * t : 0.8f + 0.12f * (t - 2)), on = 1f - progress(4.3f + t * 0.2f, 4.4f + t * 0.2f);
                rect(batch, c("6e4524"), 1f, x - 10f, worldHeight() * 0.4f, 20f, 120f);
                if (on > 0f) glow(batch, x, worldHeight() * 0.4f + 140f, 300f * pulse(17f + t, 0.75f), c("ff9a3a"), 0.7f * on);
            }
            float w = caseW(), h = caseH(), x = bossX();
            rect(batch, c("1a0e06"), 1f, x - w / 2f - 6f, bossBottom() - 6f, w + 12f, h + 12f);   // la cuve ouverte, dressée
            rect(batch, c("c4891e"), 1f, x - w / 2f, bossBottom(), w, h);
            rect(batch, c("140a04"), 1f, x - w / 2f + 24f, bossBottom() + 20f, w - 48f, h - 44f);
            poseX = time > 0.8f && time < CLIMAX_DEATH ? (random.nextFloat() - 0.5f) * 8f * progress(0.8f, CLIMAX_DEATH) : 0f;
            showBoss = time < CLIMAX_DEATH;
        }

        @Override
        protected void front(Batch batch) {
            float cracks = progress(0.8f, CLIMAX_DEATH);                                     // il se fend
            if (cracks > 0f && time < CLIMAX_DEATH) {
                crack(batch, portraitX(17f), portraitY(8f), -80f, bossHeight() * 0.5f * cracks, 3L, c("ffe8a0"), cracks, 5f);
                crack(batch, portraitX(12f), portraitY(20f), -120f, bossHeight() * 0.3f * cracks, 9L, c("ffe8a0"), cracks, 4f);
            }
            if (time >= CLIMAX_DEATH) drawDust(batch);
            float close = Interpolation.bounceOut.apply(progress(CLOSE, CLOSE + 0.6f));      // le couvercle glisse et se ferme
            float x = MathUtils.lerp(worldWidth() + caseW(), bossX(), close);
            sarcophagus(this, batch, x, bossBottom(), caseW(), caseH(), 1f);
        }

        /** Ses pixels s'envolent en poussière, emportés par le vent, et prennent la couleur du sable. */
        private void drawDust(Batch batch) {
            float k = time - CLIMAX_DEATH, scale = bossScale();
            int w = grid[0].length, h = grid.length;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int color = grid[y][x];
                    if (color == 0) continue;
                    int i = y * w + x;
                    float t = Math.max(0f, k - delay[i]), fade = 1f - t / 1.4f;
                    if (fade <= 0f) continue;
                    tmp.set(color).lerp(SAND, Math.min(1f, t * 2.5f));
                    float px = portraitX(x) + speedX[i] * t, py = portraitY(y + 1f) + speedY[i] * t - 220f * t * t;
                    float size = scale * (1f - 0.5f * Math.min(1f, t));
                    rect(batch, tmp, fade, px, py, size, size);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Djinn du Mirage
    // -------------------------------------------------------------------------

    /** Il se dédouble en trois (deux copies transparentes) ; tout ondule ; les palmiers deviennent des rouleaux qui tournent. */
    static final class DjinnPhase extends BossScene {
        private final TextureRegion[] symbols;

        DjinnPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.DJINN, 2);
            symbols = new TextureRegion[] {load("symbols/2-cherry.png"), load("symbols/3-seven.png"), load("symbols/6-bell.png"),
                load("symbols/7-diamond.png")};
        }

        @Override
        protected void step(float delta) {
            if (at(1.0f)) sparks(bossX(), bossY(), 40, PURPLE, 600f, 12f);
            if (at(CLIMAX_PHASE)) { rumble(0.6f, 10f); sparks(bossX(), bossY(), 50, c("fff2c0"), 700f, 14f); }
        }

        @Override
        protected void back(Batch batch) {
            float reels = progress(1.4f, 1.8f);
            for (int r = 0; r < 4; r++) {                                                    // les palmiers devenus rouleaux
                float x = worldWidth() * (0.08f + 0.28f * r), w = 150f, h = 130f;
                rect(batch, c("3a9a3a"), 1f - reels, x + w / 2f - 10f, groundY(), 20f, 360f);
                if (reels > 0f) {
                    rect(batch, c("1a1018"), reels, x - 10f, groundY() + 200f, w + 20f, h + 20f);
                    rect(batch, c("f6f0e2"), reels, x, groundY() + 210f, w, h);
                    batch.setColor(1f, 1f, 1f, reels);
                    reel(batch, symbols, x, groundY() + 210f, w, h, time * (6f + r));
                }
            }
            beginTilt(batch, 2.5f * MathUtils.sin(time * 5f) * progress(0.6f, 1.2f));      // tout ondule
            float split = Interpolation.pow2Out.apply(progress(0.8f, 1.6f));
            for (int side = -1; side <= 1 && split > 0f; side += 2) {                         // ses deux copies
                float x = bossX() + side * bossWidth() * 1.1f * split;
                batch.setColor(0.7f, 0.6f, 1f, 0.35f);
                batch.draw(boss, x - bossWidth() / 2f, bossBottom() + 10f * MathUtils.sin(time * 3f + side), bossWidth(), bossHeight());
            }
            poseY = 14f * MathUtils.sin(time * 2.5f);
        }

        @Override
        protected void front(Batch batch) {
            endTilt(batch);
            eyes(batch, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.2f), GOLD, new float[] {14f, 16f}, new float[] {21f, 16f});
            flash(batch, PURPLE, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /** Ses copies éclatent en bulles ; il est aspiré en spirale dans sa lampe ; la lampe roule ; l'oasis s'évapore, la terre se craquèle. */
    static final class DjinnDeath extends BossScene {
        private final TextureRegion lamp;

        DjinnDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.DJINN, 0);
            lamp = art("og", new Color[] {c("4a3008"), GOLD}, LAMP);
        }

        private float lampX() { return bossX() + bossWidth() * 0.9f - 400f * progress(3.4f, 4.6f); }
        private float lampY() { return groundY() + 40f; }

        @Override
        protected void step(float delta) {
            if (at(0.5f)) for (int side = -1; side <= 1; side += 2) sparks(bossX() + side * bossWidth() * 1.1f, bossY(), 30, c("d8c8ff"), 500f, 12f);
            if (at(CLIMAX_DEATH)) rumble(0.4f, 8f);
        }

        @Override
        protected void back(Batch batch) {
            float dry = progress(3.6f, 4.8f);                                                // l'oasis s'évapore
            rect(batch, c("2a8ad8"), 1f - dry, bossX() - bossWidth() * 1.4f, groundY() - 40f, bossWidth() * 2.8f, 60f);
            for (int k = 0; k < 16 && dry > 0f; k++) {
                crack(batch, (k * 211f) % worldWidth(), groundY() * 0.5f, 10f + k * 23f, 120f, k, c("8a5a2a"), dry, 4f);
            }
            if (time < 0.5f) {                                                               // ses copies, avant d'éclater
                for (int side = -1; side <= 1; side += 2) {
                    batch.setColor(0.7f, 0.6f, 1f, 0.35f);
                    batch.draw(boss, bossX() + side * bossWidth() * 1.1f - bossWidth() / 2f, bossBottom(), bossWidth(), bossHeight());
                }
            }
            float suck = Interpolation.pow2In.apply(progress(1.2f, CLIMAX_DEATH + 0.6f));   // aspiré en spirale dans la lampe
            poseScale = 1f - 0.97f * suck;
            poseTilt = 720f * suck;
            poseX = (lampX() - bossX()) * suck;
            poseY = (lampY() - bossBottom()) * suck;
            showBoss = suck < 1f;
        }

        @Override
        protected void front(Batch batch) {
            for (int side = -1; side <= 1; side += 2) wave(batch, 0.5f, 0.5f, bossX() + side * bossWidth() * 1.1f, bossY(), c("d8c8ff"), 600f);
            float roll = progress(3.4f, 4.6f);                                               // la lampe se renverse et roule
            draw(batch, lamp, lampX(), lampY(), fit(110f, 7), 90f * progress(3.1f, 3.4f) + 540f * roll, WHITE, 1f);
        }
    }

    // -------------------------------------------------------------------------
    // Ignar, le Dragon du Volcan
    // -------------------------------------------------------------------------

    /** Il s'envole ; le volcan entre en éruption, la lave pleut ; ses écailles rougissent ; il plonge, son souffle remplit l'écran. */
    static final class DragonPhase extends BossScene {
        DragonPhase(CutsceneKit kit) { super(kit, Moment.PHASE, EnemyKind.DRAGON, 2); }

        @Override
        protected void step(float delta) {
            if (at(0.6f)) rumble(1.4f, 8f);
            if (time > 0.6f && time < CLIMAX_PHASE && random.nextFloat() < 0.8f) {           // les pierres de lave
                Particle rock = particle(worldWidth() * 0.5f, worldHeight() * 0.55f, 40f + 100f * random.nextFloat(),
                    700f + 500f * random.nextFloat(), 2f, 18f + 14f * random.nextFloat());
                rock.gravity = 1300f;
                rock.color.set(random.nextBoolean() ? LAVA : EMBER);
                glows.add(rock);
            }
            if (at(CLIMAX_PHASE)) rumble(0.8f, 18f);
        }

        @Override
        protected void back(Batch batch) {
            float peak = worldHeight() * 0.55f;
            for (int x = -40; x < worldWidth() + 40; x += 14) {                              // le volcan, derrière lui
                float y = peak - Math.abs(x - worldWidth() / 2f) * 0.8f;
                rect(batch, c("3a1e18"), 1f, x, -MARGIN, 16f, Math.max(0f, y) + MARGIN);
            }
            glow(batch, worldWidth() / 2f, peak, 900f * progress(0.6f, 1.2f), LAVA, 0.8f);
            float fly = Interpolation.pow2Out.apply(progress(0.2f, 1.2f));
            float dive = Interpolation.pow2In.apply(progress(1.6f, CLIMAX_PHASE));
            poseY = 260f * fly - 200f * dive;
            poseScale = 1f + 0.8f * dive;
            poseTint = 0.6f * progress(0.8f, 1.6f) * pulse(10f, 0.6f);
            poseTintColor.set(LAVA);
        }

        @Override
        protected void front(Batch batch) {
            float breath = progress(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.3f) * (1f - progress(3.2f, 3.8f));
            if (breath > 0f) {                                                               // le souffle remplit l'écran
                fill(batch, LAVA, 0.55f * breath);
                glow(batch, bossX(), bossY(), worldWidth() * 1.4f, EMBER, breath);
            }
            eyes(batch, progress(1.0f, 1.3f), GOLD, new float[] {13f, 14.5f}, new float[] {22f, 14.5f});
            flash(batch, EMBER, CLIMAX_PHASE, 0.4f, 0.5f);
        }
    }

    /** Il s'écrase dans le cratère, la lave gicle ; le volcan gronde et entre en éruption : il crache son trésor (pièces, lingots, gemmes, couronnes, coupes). */
    static final class DragonDeath extends BossScene {
        private static final float ERUPT_FROM = 3.0f, ERUPT_TO = 5.0f;
        private final TextureRegion[] treasure;

        DragonDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.DRAGON, 0);
            Color[] gold = {c("6a4008"), c("ffd23a"), c("fff6c8"), c("c4891e"), c("3ad0ff"), c("ff3a5a")};
            treasure = new TextureRegion[] {
                art("oywd", new Color[] {gold[0], gold[1], gold[2], gold[3]}, INGOT),
                art("obw", new Color[] {c("0a3a5a"), gold[4], gold[2]}, GEM),
                art("ydrb", new Color[] {gold[1], gold[3], gold[5], gold[4]}, CROWN),
                art("oywd", new Color[] {gold[0], gold[1], gold[2], gold[3]}, CUP),
                art("orw", new Color[] {c("5a0a1a"), gold[5], c("ffc8d0")}, RUBY),
                coin};
        }

        /** Ignar meurt un peu plus longtemps : c'est le dernier roi. */
        @Override protected float coverStart() { return DEATH_COVER_START + 0.6f; }
        @Override protected float coverFull()  { return DEATH_COVER_FULL + 0.6f; }

        private float crater() { return worldHeight() * 0.5f; }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_DEATH)) {
                rumble(0.9f, 16f);
                for (int i = 0; i < 60; i++) {                                               // la lave gicle
                    Particle drop = particle(worldWidth() / 2f, crater(), 30f + 120f * random.nextFloat(), 600f + 700f * random.nextFloat(),
                        1.4f, 14f + 14f * random.nextFloat());
                    drop.gravity = 1500f;
                    drop.color.set(random.nextBoolean() ? LAVA : EMBER);
                    glows.add(drop);
                }
            }
            if (at(ERUPT_FROM - 0.4f)) rumble(0.4f, 6f);                                    // le volcan gronde
            if (at(ERUPT_FROM)) {
                rumble(1.2f, 18f);
                sparks(worldWidth() / 2f, crater(), 80, GOLD, 1100f, 18f);
            }
            if (time > ERUPT_FROM && time < ERUPT_TO) {                                      // l'éruption de trésors
                int count = time < ERUPT_FROM + 0.4f ? 4 : 2;
                for (int i = 0; i < count; i++) {
                    TextureRegion piece = treasure[random.nextInt(treasure.length)];
                    float size = piece == coin ? coinScale(0.05f) : fit(44f + 30f * random.nextFloat(), piece.getRegionHeight());
                    spray(piece, worldWidth() / 2f + (random.nextFloat() - 0.5f) * 60f, crater(), 1, 62f, 118f,
                        1500f + 500f * random.nextFloat(), size, 1500f, 2.6f);
                }
                if (random.nextFloat() < 0.6f) sparks(worldWidth() / 2f, crater() + 40f, 3, GOLD, 900f, 12f);
            }
        }

        @Override
        protected void back(Batch batch) {
            float quiet = 1f - progress(CLIMAX_DEATH + 0.3f, 3.6f);
            for (int x = -40; x < worldWidth() + 40; x += 14) {
                float y = crater() - Math.abs(x - worldWidth() / 2f) * 0.7f;
                rect(batch, c("3a1e18"), 1f, x, -MARGIN, 16f, Math.max(0f, y) + MARGIN);
            }
            glow(batch, worldWidth() / 2f, crater(), 700f, LAVA, 0.3f + 0.5f * quiet);
            float erupt = progress(ERUPT_FROM - 0.2f, ERUPT_FROM + 0.2f) * (1f - progress(ERUPT_TO, ERUPT_TO + 0.6f));
            if (erupt > 0f) {                                                                // le cratère d'or
                glow(batch, worldWidth() / 2f, crater(), 1100f, GOLD, 0.8f * erupt * pulse(12f, 0.7f));
                for (int k = 0; k < 5; k++) {
                    float spread = (k - 2) * 26f;
                    rect(batch, k % 2 == 0 ? GOLD : c("fff2a0"), 0.8f * erupt, worldWidth() / 2f + spread - 14f, crater(), 28f,
                        worldHeight() * (0.3f + 0.08f * MathUtils.sin(time * 20f + k)));
                }
            }
            float crash = Interpolation.pow2In.apply(progress(0.6f, CLIMAX_DEATH));
            poseY = (crater() - bossBottom()) * crash;
            poseScale = 1f - 0.5f * crash;
            poseSink = progress(CLIMAX_DEATH - 0.1f, CLIMAX_DEATH + 0.3f);
            poseTilt = -25f * crash;
            showBoss = poseSink < 1f;
        }

        @Override
        protected void front(Batch batch) {
            flash(batch, GOLD, ERUPT_FROM, 0.3f, 0.45f);
        }
    }
}
