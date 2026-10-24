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
    static final String[] SCALE = {"..oo..", ".oyyo.", "oyyyyo", "oyyyyo", ".oyyo.", "..oo.."};

    static Color c(String hex) { return Color.valueOf(hex); }

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
            eyes(batch, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.2f), GOLD, new float[] {13.5f, 15.5f}, new float[] {20.5f, 15.5f});
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

    /** Sarcophage : il se recouche, le couvercle claque ; sable et scarabées ; le couvercle se fend, il se relève plus grand, masque d'or. */
    static final class PharaohPhase extends BossScene {
        private final TextureRegion scarab;

        PharaohPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.PHARAON, 2);
            scarab = art("og", new Color[] {c("0a0608"), c("2a6a5a")}, SCARAB);
        }

        @Override
        protected void step(float delta) {
            if (at(0.8f)) rumble(0.3f, 10f);
            if (time > 0.9f && time < CLIMAX_PHASE && random.nextFloat() < 0.7f) {           // le sable coule du plafond
                Particle grain = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth() * 2f, worldHeight() + 20f, 270f, 300f, 2f, 8f);
                grain.gravity = 600f;
                grain.color.set(SAND);
                glows.add(grain);
            }
            if (at(CLIMAX_PHASE)) { rumble(0.6f, 14f); sparks(bossX(), bossY(), 50, GOLD, 700f, 14f); }
        }

        @Override
        protected void back(Batch batch) {
            float down = Interpolation.pow2In.apply(progress(0.2f, 0.8f));
            float up = Interpolation.swingOut.apply(progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.5f));
            poseSink = down * (1f - up);
            poseScale = 1f + 0.15f * up;
            poseTint = 0.4f * up;
            poseTintColor.set(GOLD);
        }

        @Override
        protected void front(Batch batch) {
            float lid = progress(0.6f, 0.8f) * (1f - progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.2f));
            if (lid > 0f) {                                                                  // le couvercle claqué, puis fendu
                rect(batch, c("e8b84a"), 1f, bossX() - bossWidth() * 0.6f, bossBottom(), bossWidth() * 1.2f, 70f);
                if (time > 1.6f) crack(batch, bossX(), bossBottom() + 35f, 10f, bossWidth() * 0.5f, 7L, c("1a0e06"), 1f, 6f);
            }
            for (int s = 0; s < 14 && time > 0.9f; s++) {                                    // les scarabées
                float x = bossX() + MathUtils.sin(time * 3f + s) * bossWidth() * (0.6f + 0.05f * s);
                draw(batch, scarab, x, groundY() + (s % 4) * 18f, fit(40f, 6), time * 200f + s * 40f, WHITE, 1f);
            }
            eyes(batch, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.2f), GREEN, new float[] {13.5f, 14.5f}, new float[] {20.5f, 14.5f});
            flash(batch, GOLD, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /** Son masque se fend ; ses bandelettes se déroulent en spirale ; il s'effrite en sable ; le sarcophage se referme, les torches s'éteignent. */
    static final class PharaohDeath extends BossScene {
        PharaohDeath(CutsceneKit kit) { super(kit, Moment.DEATH, EnemyKind.PHARAON, 0); }

        @Override
        protected void step(float delta) {
            if (at(0.6f)) { rumble(0.3f, 8f); sparks(bossX(), portraitY(16f), 30, GOLD, 500f, 10f); }
            if (time > CLIMAX_DEATH && time < 3.6f && random.nextFloat() < 0.9f) {            // il s'effrite en sable
                Particle grain = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth(), bossBottom() + random.nextFloat() * bossHeight(),
                    250f + 40f * random.nextFloat(), 100f + 200f * random.nextFloat(), 1.4f, 10f);
                grain.gravity = 500f;
                grain.color.set(SAND);
                glows.add(grain);
            }
            if (at(3.8f)) rumble(0.3f, 8f);
        }

        @Override
        protected void back(Batch batch) {
            for (int t = 0; t < 4; t++) {                                                    // les torches, qui s'éteignent une à une
                float x = worldWidth() * (t < 2 ? 0.08f + 0.12f * t : 0.8f + 0.12f * (t - 2)), on = 1f - progress(3.9f + t * 0.25f, 4.0f + t * 0.25f);
                rect(batch, c("6e4524"), 1f, x - 10f, worldHeight() * 0.4f, 20f, 120f);
                if (on > 0f) glow(batch, x, worldHeight() * 0.4f + 140f, 300f * pulse(17f + t, 0.75f), c("ff9a3a"), 0.7f * on);
            }
            poseAlpha = 1f - progress(CLIMAX_DEATH, 3.6f);
            showBoss = poseAlpha > 0f;
        }

        @Override
        protected void front(Batch batch) {
            if (time < CLIMAX_DEATH + 0.5f) {                                                // le masque fendu en deux
                float split = Interpolation.pow2In.apply(progress(0.6f, CLIMAX_DEATH));
                float y = portraitY(9f), h = 15f * bossScale(), w = 6f * bossScale();
                rect(batch, GOLD, 1f - progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.5f), bossX() - w - 120f * split, y - h - 200f * split, w, h);
                rect(batch, GOLD, 1f - progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.5f), bossX() + 120f * split, y - h - 200f * split, w, h);
            }
            float unroll = progress(1.0f, 3.2f);                                             // les bandelettes en spirale
            for (int k = 0; k < 40 * unroll; k++) {
                float a = k * 25f + time * 120f, r = 40f + k * 9f;
                float x = bossX() + MathUtils.cosDeg(a) * r, y = bossY() + MathUtils.sinDeg(a) * r * 0.8f;
                rect(batch, c("e0d4b4"), 0.9f, x - 20f, y - 6f, 40f, 12f);
            }
            float close = Interpolation.pow2Out.apply(progress(3.6f, 4.0f));                 // le sarcophage se referme
            rect(batch, c("c4891e"), 1f, bossX() - bossWidth() * 0.6f, groundY() - 20f, bossWidth() * 1.2f, 120f);
            rect(batch, c("e8b84a"), 1f, bossX() - bossWidth() * 0.6f + (1f - close) * bossWidth(), groundY() + 100f, bossWidth() * 1.2f, 40f);
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
            eyes(batch, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.2f), GOLD, new float[] {13.5f, 15.5f}, new float[] {20.5f, 15.5f});
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
            eyes(batch, progress(1.0f, 1.3f), GOLD, new float[] {12.5f, 14f}, new float[] {21.5f, 14f});
            flash(batch, EMBER, CLIMAX_PHASE, 0.4f, 0.5f);
        }
    }

    /** Il s'écrase dans le cratère, la lave gicle ; le volcan se tait, la fumée blanchit ; son dernier souffle fait un anneau ; une écaille d'or descend vers toi. */
    static final class DragonDeath extends BossScene {
        private final TextureRegion scale;

        DragonDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.DRAGON, 0);
            scale = art("oy", new Color[] {c("8a5a10"), c("ffd23a")}, SCALE);
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
            if (time > 3.0f && time < 5.4f && random.nextFloat() < 0.4f) {                  // la fumée devient blanche
                Particle smoke = particle(worldWidth() / 2f + (random.nextFloat() - 0.5f) * 120f, crater(), 90f, 120f, 2.4f, 140f);
                smoke.color.set(0.9f, 0.9f, 0.92f, 0.3f);
                glows.add(smoke);
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
            float crash = Interpolation.pow2In.apply(progress(0.6f, CLIMAX_DEATH));
            poseY = (crater() - bossBottom()) * crash;
            poseScale = 1f - 0.5f * crash;
            poseSink = progress(CLIMAX_DEATH - 0.1f, CLIMAX_DEATH + 0.3f);
            poseTilt = -25f * crash;
            showBoss = poseSink < 1f;
        }

        @Override
        protected void front(Batch batch) {
            wave(batch, 3.4f, 1.6f, worldWidth() / 2f, crater() + 220f, c("e8e8f0"), 520f); // l'anneau de fumée
            float drift = progress(3.8f, 5.9f);                                             // l'écaille d'or
            if (drift > 0f) {
                float x = worldWidth() / 2f + 100f * MathUtils.sin(drift * 5f), y = MathUtils.lerp(crater() + 300f, worldHeight() * 0.3f, drift);
                draw(batch, scale, x, y, fit(50f + 90f * drift, 6), 20f * MathUtils.sin(drift * 7f), WHITE, 1f);
                glow(batch, x, y, 300f, GOLD, 0.6f);
            }
        }
    }
}
