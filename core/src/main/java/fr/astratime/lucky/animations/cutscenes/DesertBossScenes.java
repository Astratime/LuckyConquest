package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import java.util.Random;

import static fr.astratime.lucky.assets.Palette.hex;

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

    static final Color SAND = hex("e8b86a"), SAND_DARK = hex("b8843a"), LAVA = hex("ff6a1e"), EMBER = hex("ffb84a"),
        GREEN = hex("6aff6a"), PURPLE = hex("a05aff");
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


    /**
     * Le couvercle du sarcophage du Pharaon, debout : un corps d'or, la coiffe rayée bleu et or, le masque
     * et sa barbe, des bandes en bas. Centré en {@code x}, posé sur {@code bottom}, {@code w} x {@code h}.
     */
    static void sarcophagus(BossScene scene, Batch batch, float x, float bottom, float w, float h, float alpha) {
        if (alpha <= 0f) return;
        Color line = hex("1a0e06"), gold = hex("c4891e"), light = hex("ffd54a"), blue = hex("1a3a8e");
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
        scene.rect(batch, hex("e8b84a"), alpha, x - faceW / 2f, faceY, faceW, h * 0.22f);           // le masque
        scene.rect(batch, line, alpha, x - faceW * 0.32f, faceY + h * 0.13f, faceW * 0.18f, h * 0.025f);
        scene.rect(batch, line, alpha, x + faceW * 0.14f, faceY + h * 0.13f, faceW * 0.18f, h * 0.025f);
        scene.rect(batch, line, alpha, x - faceW * 0.15f, faceY + h * 0.05f, faceW * 0.3f, h * 0.012f);
        scene.rect(batch, light, alpha, x - w * 0.05f, faceY - h * 0.1f, w * 0.1f, h * 0.1f);   // la barbe
        scene.rect(batch, hex("2a8a6a"), alpha, x - w / 2f, bottom + h * 0.52f, w, h * 0.05f);    // le collier
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
            if (at(1.0f)) sparks(bossX(), bossY(), 30, hex("f0e2b8"), 500f, 10f);
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
                    line(batch, hex("dce4ee"), blade, x0 + 40f * MathUtils.sin(t0 * 2f), y0 + t0 * 360f * blade,
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
            camel = art("oy", new Color[] {hex("3a2412"), hex("a8784a")}, CAMEL);
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
                line(batch, hex("6e4524"), 1f, x, y, x, y + 300f, 10f);
                for (int k = 0; k < 9; k++) {
                    line(batch, k % 2 == 0 ? hex("e0283a") : GOLD, 1f, x, y + 300f, x + MathUtils.cosDeg(160f - k * 17.5f) * 200f,
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
     * « Le sarcophage se ferme : Venin ! » : il se tient devant la cuve ouverte de son sarcophage ; des rouleaux
     * apparaissent sur le couvercle dressé à côté, il tire le levier, les rouleaux s'arrêtent un à un sur le Venin ;
     * le couvercle glisse et se ferme sur lui dans un grand fracas, le venin vert suinte des joints, les yeux du
     * masque s'allument ; puis le couvercle se rouvre : il en sort, vert de venin.
     */
    static final class PharaohPhase extends BossScene {
        private static final float REELS_IN = 0.4f, LEVER_OUT = 0.6f, PULL = 0.85f, SPIN = 1.1f, SHUT = 1.95f, REOPEN = 3.3f;
        private static final float[] STOPS = {1.45f, 1.65f, 1.85f};
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

        /** @return 1 quand le couvercle est fermé sur lui, 0 quand il est dressé à côté. */
        private float shut() {
            return Interpolation.bounceOut.apply(progress(SHUT, CLIMAX_PHASE)) * (1f - Interpolation.pow2.apply(progress(REOPEN, REOPEN + 0.5f)));
        }

        private float caseW() { return bossWidth() * 1.25f; }
        private float caseH() { return bossHeight() * 1.18f; }
        private float caseX() { return MathUtils.lerp(bossX() - bossWidth() * 1.45f, bossX(), shut()); }
        private float window() { return caseW() * 0.2f; }
        private float reelX(int r) { return caseX() + (r - 1) * caseW() * 0.24f - window() / 2f; }
        private float reelY() { return bossBottom() + caseH() * 0.3f; }
        private float leverX() { return caseX() - caseW() / 2f - 8f; }
        private float leverY() { return bossBottom() + caseH() * 0.42f; }
        /** Le levier : dressé (110°), tiré vers le bas (215°) puis il remonte. */
        private float leverAngle() { return MathUtils.lerp(110f, 215f, gesture(PULL, PULL + 0.5f)); }

        @Override
        protected void step(float delta) {
            if (at(REELS_IN)) sparks(caseX(), reelY() + window() / 2f, 30, GREEN, 400f, 10f);
            if (at(PULL + 0.25f)) rumble(0.2f, 6f);
            for (int r = 0; r < 3; r++) {
                if (!at(STOPS[r])) continue;
                rumble(0.1f, 4f);
                sparks(reelX(r) + window() / 2f, reelY() + window() / 2f, 14, GREEN, 300f, 8f);
            }
            if (at(CLIMAX_PHASE)) { rumble(0.7f, 16f); sparks(bossX(), bossBottom() + caseH() * 0.5f, 50, SAND, 700f, 14f); }
            if (time > CLIMAX_PHASE && time < REOPEN && random.nextFloat() < 0.7f) {             // le venin suinte des joints
                float side = random.nextBoolean() ? -1f : 1f;
                Particle drop = particle(caseX() + side * caseW() / 2f, bossBottom() + caseH() * (0.1f + 0.8f * random.nextFloat()),
                    side > 0f ? -20f : 200f, 50f, 1.4f, 14f);
                drop.gravity = 500f;
                drop.color.set(GREEN);
                glows.add(drop);
            }
            if (at(REOPEN + 0.5f)) sparks(bossX(), bossY(), 30, GREEN, 500f, 10f);
        }

        @Override
        protected void back(Batch batch) {
            float w = caseW(), h = caseH(), x = bossX();
            rect(batch, hex("1a0e06"), 1f, x - w / 2f - 6f, bossBottom() - 6f, w + 12f, h + 12f);   // la cuve ouverte, dressée
            rect(batch, hex("c4891e"), 1f, x - w / 2f, bossBottom(), w, h);
            rect(batch, hex("140a04"), 1f, x - w / 2f + 24f, bossBottom() + 20f, w - 48f, h - 44f);
            float pull = gesture(PULL, PULL + 0.5f);
            poseX = -30f * pull;                                                             // il se penche sur le levier
            poseTilt = 4f * pull;
            poseTint = 0.4f * progress(REOPEN, REOPEN + 0.5f) * pulse(6f, 0.7f);
            poseTintColor.set(GREEN);
        }

        @Override
        protected void front(Batch batch) {
            float lever = progress(LEVER_OUT, LEVER_OUT + 0.3f) * (1f - progress(SHUT - 0.2f, SHUT));   // le levier sort du flanc
            if (lever > 0f) {
                float a = leverAngle(), length = caseH() * 0.3f * lever;
                float ex = leverX() + MathUtils.cosDeg(a) * length, ey = leverY() + MathUtils.sinDeg(a) * length;
                rect(batch, hex("1a0e06"), 1f, leverX() - 14f, leverY() - 22f, 28f, 44f);
                line(batch, hex("8a97a8"), 1f, leverX(), leverY(), ex, ey, 14f);
                rect(batch, hex("1a0e06"), 1f, ex - 22f, ey - 22f, 44f, 44f);
                rect(batch, hex("c8102e"), 1f, ex - 18f, ey - 18f, 36f, 36f);
            }
            sarcophagus(this, batch, caseX(), bossBottom(), caseW(), caseH(), 1f);              // le couvercle, qui se ferme sur lui
            float show = progress(REELS_IN, REELS_IN + 0.4f);                                // les rouleaux, sur le couvercle
            if (show > 0f && symbols != null) {
                float w = window();
                rect(batch, hex("1a0e06"), show, reelX(0) - 14f, reelY() - 14f, reelX(2) - reelX(0) + w + 28f, w + 28f);
                for (int r = 0; r < 3; r++) {
                    float offset;
                    if (time < SPIN) offset = venom + 1f;                                       // au repos, sur la Potion
                    else if (time >= STOPS[r]) offset = venom + 5f * (r + 2);
                    else offset = venom + 5f * (r + 2) - (STOPS[r] - time) * 16f;
                    rect(batch, hex("f0e2b8"), show, reelX(r) - 4f, reelY() - 4f, w + 8f, w + 8f);
                    batch.setColor(1f, 1f, 1f, show);
                    reel(batch, symbols, reelX(r), reelY(), w, w, offset);
                    if (time >= STOPS[r]) glow(batch, reelX(r) + w / 2f, reelY() + w / 2f, w * 2.2f, GREEN, 0.5f * pulse(9f, 0.5f));
                }
            }
            float closed = progress(CLIMAX_PHASE - 0.05f, CLIMAX_PHASE + 0.2f) * (1f - progress(REOPEN, REOPEN + 0.2f));
            if (closed > 0f) {                                                               // les yeux du masque s'allument
                float faceW = caseW() * 0.38f, eyeY = bossBottom() + caseH() * (0.7f + 0.13f) + caseH() * 0.0125f;
                glow(batch, caseX() - faceW * 0.23f, eyeY, 120f, GREEN, closed * pulse(8f, 0.6f));
                glow(batch, caseX() + faceW * 0.23f, eyeY, 120f, GREEN, closed * pulse(8f, 0.6f));
            }
            if (time > REOPEN) eyes(batch, progress(REOPEN + 0.3f, REOPEN + 0.5f), GREEN, new float[] {14f, 15f}, new float[] {21f, 15f});
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
            Random seed = new Random(5);
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
                rect(batch, hex("6e4524"), 1f, x - 10f, worldHeight() * 0.4f, 20f, 120f);
                if (on > 0f) glow(batch, x, worldHeight() * 0.4f + 140f, 300f * pulse(17f + t, 0.75f), hex("ff9a3a"), 0.7f * on);
            }
            float w = caseW(), h = caseH(), x = bossX();
            rect(batch, hex("1a0e06"), 1f, x - w / 2f - 6f, bossBottom() - 6f, w + 12f, h + 12f);   // la cuve ouverte, dressée
            rect(batch, hex("c4891e"), 1f, x - w / 2f, bossBottom(), w, h);
            rect(batch, hex("140a04"), 1f, x - w / 2f + 24f, bossBottom() + 20f, w - 48f, h - 44f);
            poseX = time > 0.8f && time < CLIMAX_DEATH ? (random.nextFloat() - 0.5f) * 8f * progress(0.8f, CLIMAX_DEATH) : 0f;
            showBoss = time < CLIMAX_DEATH;
        }

        @Override
        protected void front(Batch batch) {
            float cracks = progress(0.8f, CLIMAX_DEATH);                                     // il se fend
            if (cracks > 0f && time < CLIMAX_DEATH) {
                crack(batch, portraitX(17f), portraitY(8f), -80f, bossHeight() * 0.5f * cracks, 3L, hex("ffe8a0"), cracks, 5f);
                crack(batch, portraitX(12f), portraitY(20f), -120f, bossHeight() * 0.3f * cracks, 9L, hex("ffe8a0"), cracks, 4f);
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

    /** L'oasis du décor : un ovale d'eau (pixels du décor), et le sable autour. */
    static final float POND_X = 180f, POND_Y = 180f, POND_RX = 90f, POND_RY = 30f;
    static final Color POND_SAND = hex("c69c5c"), POND_RIM = hex("3c96c8"), POND_WATER = hex("55abd1");
    static final String[] SCROLL_END = {"oooooooooooooooooooo", "ossssssssssssssssddo", "oooooooooooooooooooo"};
    static final String[] SEAL = {"..oooo..", ".orrrro.", "orrwrrro", "orrrrrro", "orrrrrro", "orrrrrro", ".orrrro.", "..oooo.."};

    /** Un ovale peint dans le décor, ligne par ligne (centre et rayons en pixels du décor). */
    static void decorOval(BossScene scene, Batch batch, Color color, float cx, float cy, float rx, float ry) {
        if (rx <= 0f || ry <= 0f) return;
        for (float y = -ry; y < ry; y += 1f) {
            float half = rx * (float) Math.sqrt(Math.max(0f, 1f - (y + 0.5f) * (y + 0.5f) / (ry * ry)));
            scene.decorRect(batch, color, Math.round(cx - half), cy + y, Math.round(2f * half), 1f);
        }
    }

    /**
     * « Voeu exaucé : Lettre de change ! » : sa lampe fume à ses pieds, il s'en élève, bras ouverts ; trois étoiles
     * de voeu tournent autour de lui ; un grand parchemin se déroule devant lui, une plume d'or y écrit, et son
     * sceau rouge s'écrase dessus ; la lettre de change s'envole vers toi.
     */
    static final class DjinnPhase extends BossScene {
        private static final float UNROLL = 1.0f, WRITE = 1.4f, SEAL_AT = CLIMAX_PHASE, SEND = 2.9f;
        private final TextureRegion lamp, rod, seal;

        DjinnPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.DJINN, 2);
            lamp = art("og", new Color[] {hex("4a3008"), GOLD}, LAMP);
            rod = art("osd", new Color[] {hex("3a2412"), hex("c8904a"), hex("6e4524")}, SCROLL_END);
            seal = art("orw", new Color[] {hex("5a0a0a"), hex("c8102e"), hex("ff8a8a")}, SEAL);
        }

        private float paperW() { return bossWidth() * 1.1f; }
        private float paperH() { return bossHeight() * 0.85f * Interpolation.pow2Out.apply(progress(UNROLL, UNROLL + 0.4f)); }
        /** Le centre du parchemin : devant lui, puis il s'envole vers toi. */
        private float paperX() { return bossX() + bossWidth() * 1.15f + worldWidth() * 0.3f * fly(); }
        private float paperY() { return bossY() - worldHeight() * 0.15f * fly(); }
        private float fly() { return Interpolation.pow2In.apply(progress(SEND, SEND + 0.7f)); }

        @Override
        protected void step(float delta) {
            if (time < 1.2f && random.nextFloat() < 0.6f) {                                  // la fumée de la lampe
                Particle smoke = particle(bossX() - bossWidth() * 0.55f, groundY() + 40f, 80f + 20f * random.nextFloat(), 140f, 1.2f, 60f);
                smoke.color.set(0.75f, 0.6f, 1f, 0.35f);
                glows.add(smoke);
            }
            if (at(0.7f)) sparks(bossX(), bossY(), 40, PURPLE, 600f, 12f);
            if (at(SEAL_AT)) { rumble(0.6f, 12f); sparks(paperX(), paperY() - paperH() * 0.25f, 40, hex("ff3a3a"), 500f, 12f); }
            if (at(SEND)) sparks(paperX(), paperY(), 30, GOLD, 500f, 10f);
        }

        @Override
        protected void back(Batch batch) {
            draw(batch, lamp, bossX() - bossWidth() * 0.55f, groundY() + 30f, fit(90f, 7), 0f, WHITE, 1f);   // sa lampe, à ses pieds
            float rise = Interpolation.pow2Out.apply(progress(0.3f, 1.0f));
            poseY = 40f * rise + 10f * MathUtils.sin(time * 2.5f);
            poseStretch = 0.06f * rise;
            for (int k = 0; k < 3; k++) {                                                    // les trois étoiles de voeu
                float a = time * 220f + k * 120f, stars = progress(0.6f, 0.9f) * (1f - progress(UNROLL + 0.2f, UNROLL + 0.5f));
                float x = bossX() + MathUtils.cosDeg(a) * bossWidth() * 0.8f, y = bossY() + 40f + MathUtils.sinDeg(a) * bossHeight() * 0.25f;
                if (stars > 0f) { glow(batch, x, y, 120f, GOLD, stars); rect(batch, hex("fff2c0"), stars, x - 6f, y - 6f, 12f, 12f); }
            }
        }

        @Override
        protected void front(Batch batch) {
            float h = paperH();
            if (h > 1f && fly() < 1f) {                                                       // le parchemin
                float w = paperW() * (1f - 0.3f * fly()), x = paperX(), y = paperY(), alpha = 1f - fly();
                float scale = w / 20f;
                rect(batch, hex("1a1018"), alpha, x - w / 2f - 4f, y - h / 2f, w + 8f, h);
                rect(batch, hex("f0e2b8"), alpha, x - w / 2f, y - h / 2f, w, h);
                int lines = (int) (7 * progress(WRITE, SEAL_AT - 0.1f));                    // la plume écrit, ligne après ligne
                for (int l = 0; l < 7 && l < lines + 1; l++) {
                    float k = l < lines ? 1f : (7 * progress(WRITE, SEAL_AT - 0.1f)) % 1f;
                    float ly = y + h / 2f - h * (0.16f + 0.1f * l), lw = w * (l == 0 ? 0.5f : 0.75f) * k;
                    rect(batch, hex("4a3018"), alpha, x - w * 0.38f, ly, lw, 6f);
                    if (l == lines && time < SEAL_AT) {                                        // la plume d'or
                        float qx = x - w * 0.38f + lw, qy = ly;
                        line(batch, GOLD, 1f, qx, qy, qx + 30f, qy + 70f, 8f);
                        line(batch, hex("fff2c0"), 1f, qx + 10f, qy + 20f, qx + 34f, qy + 64f, 4f);
                    }
                }
                draw(batch, rod, x, y + h / 2f, scale, 0f, WHITE, alpha);
                draw(batch, rod, x, y - h / 2f, scale, 0f, WHITE, alpha);
                float stamp = progress(SEAL_AT - 0.15f, SEAL_AT);                              // le sceau rouge s'écrase
                if (stamp > 0f) {
                    float size = MathUtils.lerp(3f, 1f, Interpolation.pow2In.apply(stamp));
                    draw(batch, seal, x + w * 0.22f, y - h * 0.28f, w * 0.03f * size, 0f, WHITE, alpha * stamp);
                }
            }
            eyes(batch, progress(0.5f, 0.8f), GOLD, new float[] {14f, 16f}, new float[] {21f, 16f});
            flash(batch, PURPLE, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /** « Le Djinn retourne dans sa lampe » : il est aspiré en spirale dans sa lampe ; la lampe roule ; l'oasis du décor s'évapore, son fond se craquèle. */
    static final class DjinnDeath extends BossScene {
        private final TextureRegion lamp;

        DjinnDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.DJINN, 0);
            lamp = art("og", new Color[] {hex("4a3008"), GOLD}, LAMP);
        }

        private float lampX() { return bossX() + bossWidth() * 0.9f - 400f * progress(3.4f, 4.6f); }
        private float lampY() { return groundY() + 40f; }

        @Override
        protected void step(float delta) {
            if (at(0.5f)) sparks(bossX(), bossY(), 30, hex("d8c8ff"), 500f, 12f);
            if (at(CLIMAX_DEATH)) rumble(0.4f, 8f);
            if (time > 3.6f && time < 4.8f && random.nextFloat() < 0.5f) {                   // la vapeur de l'oasis
                Particle steam = particle(decorX(POND_X + (random.nextFloat() - 0.5f) * 140f), decorY(POND_Y), 90f, 120f, 1.4f, 80f);
                steam.color.set(1f, 1f, 1f, 0.25f);
                glows.add(steam);
            }
        }

        @Override
        protected void back(Batch batch) {
            float dry = progress(3.6f, 4.8f);                                                // l'oasis du décor s'évapore
            if (dry > 0f) {
                decorOval(this, batch, POND_SAND, POND_X, POND_Y, POND_RX + 1f, POND_RY + 1f);
                float left = 1f - dry;
                decorOval(this, batch, POND_RIM, POND_X, POND_Y, POND_RX * left, POND_RY * left);
                decorOval(this, batch, POND_WATER, POND_X, POND_Y - 1f, (POND_RX - 10f) * left, (POND_RY - 5f) * left);
                for (int k = 0; k < 9; k++) {                                                // le fond se craquèle
                    crack(batch, decorX(POND_X - 70f + k * 17f), decorY(POND_Y + (k % 3 - 1) * 8f), 20f + k * 37f, 90f, k, hex("8a5a2a"), dry, 4f);
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
            float roll = progress(3.4f, 4.6f);                                               // la lampe se renverse et roule
            draw(batch, lamp, lampX(), lampY(), fit(110f, 7), 90f * progress(3.1f, 3.4f) + 540f * roll, WHITE, 1f);
        }
    }

    // -------------------------------------------------------------------------
    // Ignar, le Dragon du Volcan
    // -------------------------------------------------------------------------

    static final String[] WING_LEFT = {"ooo.............", "orrooo..........", ".orrrroo........", ".orrrrrrooo.....", "..orrrrrrrrooo..", "..orrrrrrrrrrroo", "...orrrrrrrrrrro", "...ordrrdrrrrrro", "....od.od.drrrro", "....o..o..odrrro", "..........o.odro", "............o.oo"};
    static final String[] WING_RIGHT = {".............ooo", "..........ooorro", "........oorrrro.", ".....ooorrrrrro.", "..ooorrrrrrrro..", "oorrrrrrrrrrro..", "orrrrrrrrrrro...", "orrrrrrdrrdro...", "orrrrd.do.do....", "orrrdo..o..o....", "ordo.o..........", "oo.o............"};
    /** Le cratère du volcan du décor (pixels du décor). */
    static final float CRATER_X = 70f, CRATER_Y = 30f;

    /**
     * « Envol : Brûlure ! » : il déplie ses ailes, s'arrache du sol et monte haut dans le ciel ; le volcan du
     * décor crache sa lave ; ses écailles rougissent ; il plonge sur toi et son souffle remplit l'écran.
     */
    static final class DragonPhase extends BossScene {
        private static final float UNFOLD = 0.1f, LIFT = 0.5f, TOP = 1.5f, DIVE = 1.85f;
        private final TextureRegion left, right;

        DragonPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.DRAGON, 2);
            Color[] scales = {hex("3a0a08"), hex("a82a1e"), hex("6a1a12")};
            left = art("ord", scales, WING_LEFT);
            right = art("ord", scales, WING_RIGHT);
        }

        @Override
        protected void step(float delta) {
            if (at(LIFT)) { rumble(0.5f, 8f); sparks(bossX(), groundY() + 20f, 40, SAND_DARK, 500f, 10f); }
            for (int k = 0; k < 6; k++) if (at(LIFT + k * 0.26f)) rumble(0.05f, 3f);           // chaque battement d'ailes
            if (time > 0.6f && time < CLIMAX_PHASE && random.nextFloat() < 0.8f) {           // le volcan du décor crache sa lave
                Particle rock = particle(decorX(CRATER_X), decorY(CRATER_Y), 50f + 80f * random.nextFloat(),
                    600f + 500f * random.nextFloat(), 2f, 18f + 14f * random.nextFloat());
                rock.gravity = 1300f;
                rock.color.set(random.nextBoolean() ? LAVA : EMBER);
                glows.add(rock);
            }
            if (at(CLIMAX_PHASE)) rumble(0.8f, 18f);
        }

        private float open() { return Interpolation.pow2Out.apply(progress(UNFOLD, LIFT)); }

        @Override
        protected void back(Batch batch) {
            glow(batch, decorX(CRATER_X), decorY(CRATER_Y), 700f * progress(0.6f, 1.2f), LAVA, 0.8f);
            float fly = Interpolation.pow2Out.apply(progress(LIFT, TOP));
            float dive = Interpolation.pow2In.apply(progress(DIVE, CLIMAX_PHASE));
            poseY = worldHeight() * 0.3f * fly * (1f - dive) + 12f * MathUtils.sin(time * 12f) * fly - 120f * dive;
            poseScale = MathUtils.lerp(1f, 0.7f, fly) * (1f - dive) + 1.9f * dive;
            poseTint = 0.6f * progress(0.8f, 1.6f) * pulse(10f, 0.6f);
            poseTintColor.set(LAVA);
            float shadow = 1f - 0.6f * fly;                                                  // son ombre au sol, qui rapetisse
            rect(batch, hex("1a0806"), 0.4f * (1f - dive), bossX() - bossWidth() * 0.5f * shadow, groundY() + 6f, bossWidth() * shadow, 18f * shadow);
            float open = open();                                                             // ses ailes, qui battent
            if (open > 0f) {
                float flap = 28f * MathUtils.sin(time * 12f) * progress(LIFT - 0.2f, LIFT), scale = bossScale() * poseScale * 1.7f;
                float w = 16f * scale * open, h = 12f * scale, sx = portraitX(5f), dx = portraitX(29f), sy = portraitY(27f);
                batch.setColor(1f, 1f, 1f, 1f);
                batch.draw(left, sx - w, sy - h * 0.35f, w, h * 0.35f, w, h, 1f, 1f, -flap);
                batch.draw(right, dx, sy - h * 0.35f, 0f, h * 0.35f, w, h, 1f, 1f, flap);
            }
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
            Color[] gold = {hex("6a4008"), hex("ffd23a"), hex("fff6c8"), hex("c4891e"), hex("3ad0ff"), hex("ff3a5a")};
            treasure = new TextureRegion[] {
                art("oywd", new Color[] {gold[0], gold[1], gold[2], gold[3]}, INGOT),
                art("obw", new Color[] {hex("0a3a5a"), gold[4], gold[2]}, GEM),
                art("ydrb", new Color[] {gold[1], gold[3], gold[5], gold[4]}, CROWN),
                art("oywd", new Color[] {gold[0], gold[1], gold[2], gold[3]}, CUP),
                art("orw", new Color[] {hex("5a0a1a"), gold[5], hex("ffc8d0")}, RUBY),
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
                rect(batch, hex("3a1e18"), 1f, x, -MARGIN, 16f, Math.max(0f, y) + MARGIN);
            }
            glow(batch, worldWidth() / 2f, crater(), 700f, LAVA, 0.3f + 0.5f * quiet);
            float erupt = progress(ERUPT_FROM - 0.2f, ERUPT_FROM + 0.2f) * (1f - progress(ERUPT_TO, ERUPT_TO + 0.6f));
            if (erupt > 0f) {                                                                // le cratère d'or
                glow(batch, worldWidth() / 2f, crater(), 1100f, GOLD, 0.8f * erupt * pulse(12f, 0.7f));
                for (int k = 0; k < 5; k++) {
                    float spread = (k - 2) * 26f;
                    rect(batch, k % 2 == 0 ? GOLD : hex("fff2a0"), 0.8f * erupt, worldWidth() / 2f + spread - 14f, crater(), 28f,
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
