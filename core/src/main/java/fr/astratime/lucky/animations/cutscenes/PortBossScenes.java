package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;

/** Les scènes de phase et de mort des quatre rois du Port des Contrebandiers (choix d'Astra, 2026-10-24). */
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
    static final String[] SHIP = {
        "...............kk.............", "...............kwk............", "...............kkk............",
        "...............p..............", "......p........p.......p......", ".....sss......sss.....sss.....",
        "....sssss....sssss...sssss....", "....sssss....sssss...sssss....", "...ssssss...sssssss..ssssss...",
        "...ssssss...sssssss..ssssss...", "...ssssss...sssssss..ssssss...", "....ssss.....sssss....ssss....",
        "......p........p.......p......", "hhhhhhhhhhhhhhhhhhhhhhhhhhhhhh", ".hhhyhhhhyhhhhyhhhhyhhhhyhhhh.",
        "..hhhhhhhhhhhhhhhhhhhhhhhhhh..", "...hhhhhhhhhhhhhhhhhhhhhhhh...", "....hhhhhhhhhhhhhhhhhhhhhh...."};
    static final String[] SAILOR = {
        "..oooooo..", ".oooooooo.", "oooooooooo", ".oooooooo.", ".oooooooo.", ".oooooooo.", "..oooooo..", "...oooo...",
        "..oooooo..", ".oooooooo.", "oooooooooo", "oooooooooo", "oooooooooo"};
    static final String[] STOOL = {"oooooo", "o....o", "o....o", "o....o"};
    static final String[] HOOK = {".o.o.", "o.o.o", "..o..", "..o..", "..o.."};
    static final String[] HAT = {"...oooo...", "..oooooo..", "..owwwwo..", "oooooooooo", ".oooooooo."};
    static final String[] FLAG = {"oooooooo", "oowwwooo", "owkwkwoo", "oowwwooo", "ooowoooo", "owoowooo", "oooooooo"};
    static final String[] CHEST = {".yyyyyyyy.", "yddddddddy", "yddddddddy", "yyyyyyyyyy", "ydddgddddy", "yddddddddy",
        "yddddddddy", "yyyyyyyyyy"};

    static Color c(String hex) { return Color.valueOf(hex); }

    /** La mer : son niveau {@code level} (en y), une ligne d'écume qui ondule. */
    static void sea(BossScene scene, Batch batch, float level, float alpha) {
        scene.rect(batch, SEA, alpha, -60f, -60f, scene.worldWidth() + 120f, level + 60f);
        for (int i = 0; i < 40; i++) {
            float x = i * scene.worldWidth() / 39f, y = level + 6f * MathUtils.sin(scene.time * 3f + i * 0.8f);
            scene.rect(batch, FOAM, 0.6f * alpha, x - 30f, y - 4f, 60f, 8f);
        }
    }

    // -------------------------------------------------------------------------
    // Capitaine Rat
    // -------------------------------------------------------------------------

    /** Grappins lancés : une nuée de rats grimpe aux cordes et envahit l'écran par les bords. */
    static final class RatPhase extends BossScene {
        private static final float[][] HOOKS = {{-0.02f, 0.92f}, {1.02f, 0.88f}, {-0.02f, 0.55f}, {1.02f, 0.5f}};
        private static final Color ROPE = c("b89a6a");
        private final TextureRegion rat, hook;

        RatPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.CAPITAINE_RAT, 2);
            rat = art("ogrp", new Color[] {c("0a0808"), c("4a4048"), c("e0283a"), c("c87a8a")}, RAT);
            hook = art("o", new Color[] {c("9a9ab0")}, HOOK);
        }

        private float hookTime(int i) { return 0.3f + i * 0.25f; }
        private float[] anchor(int i) {
            return new float[] {bossX() + (HOOKS[i][0] < 0.5f ? -1 : 1) * bossWidth() * (0.7f + 0.2f * (i / 2)), groundY() + 20f};
        }

        @Override
        protected void step(float delta) {
            for (int i = 0; i < HOOKS.length; i++) if (at(hookTime(i) + 0.3f)) rumble(0.15f, 5f);
            if (at(CLIMAX_PHASE)) { rumble(0.7f, 12f); sparks(bossX(), bossY(), 50, c("e0283a"), 700f, 20f); }
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
            eyes(batch, progress(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.2f), c("ff2a3a"), new float[] {14f, 18f}, new float[] {21f, 18f});
            flash(batch, c("e0283a"), CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /** Le navire gîte ; ses rats sautent par-dessus bord en file ; il glisse de la planche ; son chapeau flotte. */
    static final class RatDeath extends BossScene {
        private final TextureRegion rat, hat;

        RatDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.CAPITAINE_RAT, 0);
            rat = art("ogrp", new Color[] {c("0a0808"), c("4a4048"), c("e0283a"), c("c87a8a")}, RAT);
            hat = art("ow", new Color[] {c("1a1410"), c("e8e0d0")}, HAT);
        }

        private float list() { return -9f * Interpolation.pow2Out.apply(progress(0.2f, 1.6f)); }
        private float plankEnd() { return worldWidth() * 0.82f; }

        @Override
        protected void step(float delta) {
            if (at(0.2f)) rumble(0.5f, 6f);
            for (int i = 0; i < 8; i++) {                                // les rats sautent un par un
                if (at(0.5f + i * 0.17f)) {
                    Particle jumper = place(rat, worldWidth() * 0.62f, groundY() + 40f, 420f, 700f, fit(46f, 11), 1.5f);
                    jumper.gravity = 1700f;
                    jumper.spin = -300f;
                }
            }
            if (at(CLIMAX_DEATH + 0.55f)) {                              // il touche l'eau
                rumble(0.5f, 9f);
                for (int i = 0; i < 40; i++) {
                    Particle drop = particle(plankEnd() + 40f, waterLevel(), 50f + 80f * random.nextFloat(), 500f + 500f * random.nextFloat(),
                        1.0f, 16f + 12f * random.nextFloat());
                    drop.gravity = 1600f;
                    drop.color.set(FOAM);
                    glows.add(drop);
                }
            }
        }

        private float waterLevel() { return groundY() * 0.7f; }

        @Override
        protected void back(Batch batch) {
            beginTilt(batch, list());
            rect(batch, WOOD, 1f, -60f, groundY() - 20f, worldWidth() * 0.66f, 40f);              // le pont
            rect(batch, DARK_WOOD, 1f, worldWidth() * 0.55f, groundY() + 10f, plankEnd() - worldWidth() * 0.55f, 16f); // la planche
            // il recule sur la planche puis glisse et tombe
            float walk = Interpolation.pow2.apply(progress(0.6f, CLIMAX_DEATH));
            float drop = Interpolation.pow2In.apply(progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.55f));
            poseX = (plankEnd() - bossX()) * walk + 40f * drop;
            poseY = 30f - (bossBottom() + bossHeight()) * drop;
            poseTilt = -50f * drop;
            poseScale = 0.8f;
            showBoss = drop < 1f;
        }

        @Override
        protected void front(Batch batch) {
            endTilt(batch);
            sea(this, batch, waterLevel(), 1f);
            if (time < CLIMAX_DEATH + 0.8f) return;
            float bob = MathUtils.sin(time * 3f) * 8f;                   // son chapeau flotte
            float k = progress(CLIMAX_DEATH + 0.8f, 5f);
            draw(batch, hat, plankEnd() + 40f - 120f * k, waterLevel() + 14f + bob, fit(60f, 5), MathUtils.sin(time * 2f) * 8f, WHITE, 1f);
        }
    }

    // -------------------------------------------------------------------------
    // Tavernier
    // -------------------------------------------------------------------------

    /** Il fracasse une chope : les tabourets volent, une bagarre éclate en ombres chinoises. */
    static final class TavernPhase extends BossScene {
        private final TextureRegion mug, sailor, stool;
        private static final Color BEER = c("ffc040"), LAMP = c("ff9a3a");

        TavernPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.TAVERNIER, 2);
            mug = art("owbdg", new Color[] {c("1a0e06"), c("f4f0e0"), c("ffb030"), c("c87a10"), c("8a6a3a")}, MUG);
            sailor = art("o", new Color[] {Color.WHITE}, SAILOR);
            stool = art("o", new Color[] {c("6a4020")}, STOOL);
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

    /** Il essuie son comptoir et souffle les bougies une à une ; les portes battantes se referment. */
    static final class TavernDeath extends BossScene {
        private static final float[] PUFFS = {1.1f, 1.6f, 2.1f, 2.5f, 2.9f};
        private static final Color FLAME = c("ffb040"), CLOTH = c("e8e0d0"), DOOR = c("6a4020");

        TavernDeath(CutsceneKit kit) { super(kit, Moment.DEATH, EnemyKind.TAVERNIER, 0); }

        private float candleX(int i) { return worldWidth() * (0.14f + i * 0.18f); }
        private float barTop() { return groundY() + 60f; }

        @Override
        protected void step(float delta) {
            for (int i = 0; i < PUFFS.length; i++) {
                if (at(PUFFS[i])) {                                      // la flamme s'éteint : un filet de fumée
                    for (int k = 0; k < 8; k++) {
                        Particle smoke = particle(candleX(i), barTop() + 80f, 80f + 20f * random.nextFloat(), 60f + 40f * random.nextFloat(),
                            1.6f, 30f + 20f * random.nextFloat());
                        smoke.color.set(0.6f, 0.6f, 0.65f, 0.35f);
                        glows.add(smoke);
                    }
                }
            }
            if (at(4.3f)) rumble(0.3f, 6f);
        }

        private int lit() {
            int count = 0;
            for (float puff : PUFFS) if (time < puff) count++;
            return count;
        }

        @Override
        protected void back(Batch batch) {
            float light = lit() / (float) PUFFS.length;
            poseShadow = 0.65f * (1f - light);
            poseX = MathUtils.sin(time * 5f) * 30f * (1f - progress(2.6f, 3.2f));   // il essuie le comptoir
        }

        @Override
        protected void front(Batch batch) {
            float light = lit() / (float) PUFFS.length;
            rect(batch, WOOD, 1f, -60f, groundY() - 10f, worldWidth() + 120f, 70f);
            float wipe = bossX() + poseX * 3f;                            // le chiffon
            rect(batch, CLOTH, 1f - progress(2.6f, 3.0f), wipe - 40f, barTop() - 8f, 80f, 16f);
            for (int i = 0; i < PUFFS.length; i++) {                     // les bougies
                rect(batch, CLOTH, 1f, candleX(i) - 8f, barTop(), 16f, 60f);
                if (time < PUFFS[i]) {
                    float flicker = 0.85f + 0.15f * MathUtils.sin(time * 23f + i * 3f);
                    glow(batch, candleX(i), barTop() + 78f, 160f * flicker, FLAME, 0.55f);
                    glow(batch, candleX(i), barTop() + 74f, 26f, HOT, 1f);
                }
            }
            fill(batch, Color.BLACK, 0.5f * (1f - light));
            // les portes battantes se ferment en se balançant
            float close = progress(3.5f, 4.3f);
            if (close > 0f) {
                float swing = close < 1f ? Interpolation.swingOut.apply(close) : 1f + 0.05f * MathUtils.sin((time - 4.3f) * 14f)
                    * Math.max(0f, 1f - (time - 4.3f));
                float half = worldWidth() / 2f, w = half * swing;
                rect(batch, DOOR, 1f, -60f, groundY() + 40f, w + 60f, worldHeight() * 0.55f);
                rect(batch, DOOR, 1f, worldWidth() - w, groundY() + 40f, w + 60f, worldHeight() * 0.55f);
                for (int s = 0; s < 4; s++) {
                    rect(batch, DARK_WOOD, 1f, w - 20f - s * 90f, groundY() + 60f, 10f, worldHeight() * 0.5f);
                    rect(batch, DARK_WOOD, 1f, worldWidth() - w + 10f + s * 90f, groundY() + 60f, 10f, worldHeight() * 0.5f);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Gardien du Phare
    // -------------------------------------------------------------------------

    /** La tempête : pluie et éclairs ; le faisceau du phare tourne de plus en plus vite et vire au rouge. */
    static final class LighthousePhase extends BossScene {
        private static final Color BEAM = c("fff07a"), RED = c("ff3a2a");
        private static final float[] BOLTS = {0.7f, 1.5f, CLIMAX_PHASE, 3.3f};
        private float angle;

        LighthousePhase(CutsceneKit kit) { super(kit, Moment.PHASE, EnemyKind.GARDIEN_PHARE, 2); }

        @Override protected void restart() { angle = 0f; }

        @Override
        protected void step(float delta) {
            angle += delta * MathUtils.lerp(60f, 720f, progress(0f, CLIMAX_PHASE));
            for (float bolt : BOLTS) if (at(bolt)) rumble(0.35f, 9f);
        }

        private float lampY() { return bossBottom() + bossHeight() * 0.95f; }

        @Override
        protected void back(Batch batch) {
            rain(batch, 160, 0.35f);
            for (int i = 0; i < BOLTS.length; i++) {
                float a = 1f - (time - BOLTS[i]) / 0.25f;
                if (time >= BOLTS[i] && a > 0f) {
                    fill(batch, WHITE, 0.25f * a);
                    bolt(batch, worldWidth() * (0.15f + 0.23f * i), groundY(), 31L * (i + 1), a);
                }
            }
            sea(this, batch, groundY(), 1f);
            Color color = tmp.set(BEAM).lerp(RED, progress(1.6f, CLIMAX_PHASE));
            float length = worldWidth() * 1.2f * Math.abs(MathUtils.cosDeg(angle));      // il balaie : de face, il raccourcit
            float side = MathUtils.cosDeg(angle) >= 0f ? 1f : -1f;
            additive(batch);
            batch.setColor(color.r, color.g, color.b, 0.45f);
            batch.draw(soft, side > 0 ? bossX() : bossX() - length, lampY() - 70f, length, 140f);
            normal(batch);
            glow(batch, bossX(), lampY(), 220f, color, 0.8f);
        }

        @Override
        protected void front(Batch batch) {
            rain(batch, 60, 0.5f);
            flash(batch, RED, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /** La lampe vacille, balaie une dernière fois et se fend ; dans le noir, une petite barque passe, sauvée. */
    static final class LighthouseDeath extends BossScene {
        private static final Color BEAM = c("fff07a");
        private final TextureRegion ship;

        LighthouseDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.GARDIEN_PHARE, 0);
            ship = art("kwpshy", new Color[] {c("0a0608"), c("f0f0f0"), c("3a2618"), c("d8d0c0"), c("1c120c"), c("ffd060")}, SHIP);
        }

        private float lampY() { return bossBottom() + bossHeight() * 0.95f; }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_DEATH)) {                                       // la lampe se fend
                rumble(0.5f, 8f);
                sparks(bossX(), lampY(), 60, BEAM, 800f, 18f);
            }
        }

        private float lamp() {
            if (time >= CLIMAX_DEATH) return 0f;
            float flicker = time > 0.8f && MathUtils.sin(time * 31f) * MathUtils.sin(time * 7f) > 0.3f ? 0.25f : 1f;
            return flicker;
        }

        @Override
        protected void back(Batch batch) {
            sea(this, batch, groundY(), 1f);
            // la barque, sauvée par le dernier balayage
            float k = progress(CLIMAX_DEATH + 0.4f, 5.0f);
            if (k > 0f) {
                float x = MathUtils.lerp(-200f, worldWidth() + 200f, k);
                draw(batch, ship, x, groundY() + 30f + MathUtils.sin(time * 3f) * 5f, fit(110f, 18), MathUtils.sin(time * 2f) * 3f,
                    tmp.set(0.5f, 0.55f, 0.7f, 1f), 1f);
                glow(batch, x + 20f, groundY() + 50f, 60f, BEAM, 0.7f);           // sa petite lanterne
            }
            float on = lamp();
            float sweep = MathUtils.lerp(-60f, 200f, progress(1.4f, CLIMAX_DEATH));       // le dernier balayage
            if (on > 0f) {
                additive(batch);
                batch.setColor(BEAM.r, BEAM.g, BEAM.b, 0.4f * on);
                batch.draw(soft, bossX(), lampY() - 70f, 0f, 70f, worldWidth(), 140f, 1f, 1f, sweep + 180f);
                normal(batch);
                glow(batch, bossX(), lampY(), 220f, BEAM, 0.8f * on);
            }
            poseShadow = 0.7f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.6f);
        }

        @Override
        protected void front(Batch batch) {
            if (time >= CLIMAX_DEATH) {                                   // les fissures de la lampe
                for (int i = 0; i < 5; i++) {
                    float a = i * 72f + 15f, len = 60f;
                    line(batch, WHITE, 0.6f * (1f - progress(CLIMAX_DEATH + 0.6f, CLIMAX_DEATH + 1.6f)), bossX(), lampY(),
                        bossX() + MathUtils.cosDeg(a) * len, lampY() + MathUtils.sinDeg(a) * len, 4f);
                }
            }
            fill(batch, Color.BLACK, 0.45f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.5f));
            flash(batch, BEAM, CLIMAX_DEATH, 0.25f, 0.5f);
        }
    }

    // -------------------------------------------------------------------------
    // Capitaine Noir
    // -------------------------------------------------------------------------

    /** Une bordée de canons : la fumée couvre l'écran, seul son œil brille dans le noir. */
    static final class BlackPhase extends BossScene {
        private static final Color FIRE = c("ffb040"), EYE = c("9a5aff");

        private final com.badlogic.gdx.utils.Array<Particle> smoke = new com.badlogic.gdx.utils.Array<>(false, 128);

        BlackPhase(CutsceneKit kit) { super(kit, Moment.PHASE, EnemyKind.CAPITAINE_NOIR, 2); }

        @Override protected void restart() { smoke.clear(); }

        private float shot(int i) { return 0.6f + i * 0.18f; }
        private float gunX(int i) { return worldWidth() * (0.08f + i * 0.12f); }

        @Override
        protected void step(float delta) {
            for (int i = 0; i < 8; i++) {
                if (at(shot(i))) {                                      // un canon tire : flamme et fumée
                    rumble(0.15f, 7f);
                    sparks(gunX(i), groundY() + 30f, 20, FIRE, 700f, 22f);
                    for (int k = 0; k < 10; k++) {
                        Particle puff = particle(gunX(i), groundY() + 30f, 60f + 60f * random.nextFloat(), 120f + 260f * random.nextFloat(),
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
            sea(this, batch, groundY() - 30f, 1f);
            rect(batch, DARK_WOOD, 1f, -60f, groundY() - 10f, worldWidth() + 120f, 60f);   // le flanc du navire
            for (int i = 0; i < 8; i++) rect(batch, Color.BLACK, 1f, gunX(i) - 20f, groundY() + 10f, 40f, 30f);
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
            fill(batch, c("1a1a22"), 0.75f * progress(1.4f, CLIMAX_PHASE));
            if (k > 0f) {
                float x = portraitX(21f), y = portraitY(16f);                 // son œil, à côté du bandeau
                float pulse = 0.8f + 0.2f * MathUtils.sin(time * 6f);
                glow(batch, x, y, bossScale() * 14f * k * pulse, EYE, 0.8f);
                glow(batch, x, y, bossScale() * 2.5f * k, WHITE, 1f);
            }
            flash(batch, FIRE, 0.6f, 0.15f, 0.3f);
        }
    }

    /** Son galion sombre, le mât en dernier ; le drapeau noir flotte ; un coffre remonte à la surface. */
    static final class BlackDeath extends BossScene {
        private final TextureRegion ship, flag, chest;

        BlackDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.CAPITAINE_NOIR, 0);
            ship = art("kwpshy", new Color[] {c("0a0608"), c("3a3a44"), c("2a1a10"), c("2a2a34"), c("140c08"), c("9a5aff")}, SHIP);
            flag = art("owk", new Color[] {c("0a0a0a"), c("e8e8e8"), c("0a0a0a")}, FLAG);
            chest = art("ydg", new Color[] {c("ffc93a"), c("6a3a14"), c("fff0a0")}, CHEST);
        }

        private float sink() { return Interpolation.pow2In.apply(progress(0.4f, CLIMAX_DEATH + 0.8f)); }
        private float seaLevel() { return groundY() + worldHeight() * 0.12f; }

        @Override
        protected void step(float delta) {
            if (at(0.4f)) rumble(0.8f, 6f);
            if (at(CLIMAX_DEATH + 0.8f)) {                               // le mât disparaît : des bulles
                rumble(0.4f, 7f);
                for (int i = 0; i < 30; i++) {
                    Particle bubble = particle(bossX() + (random.nextFloat() - 0.5f) * 300f, seaLevel(), 80f + 20f * random.nextFloat(),
                        200f + 300f * random.nextFloat(), 0.8f, 14f + 10f * random.nextFloat());
                    bubble.color.set(FOAM);
                    glows.add(bubble);
                }
            }
        }

        @Override
        protected void back(Batch batch) {
            // le galion derrière lui, qui s'enfonce en penchant : le mât reste en dernier
            float s = sink(), scale = fit(worldHeight() * 0.7f, 18);
            float y = seaLevel() + worldHeight() * 0.25f - worldHeight() * 0.75f * s;
            if (s < 1f) draw(batch, ship, bossX(), y, scale, 18f * s, WHITE, 1f);
            poseSink = Interpolation.pow2In.apply(progress(0.8f, CLIMAX_DEATH));
            poseShadow = 0.4f;
            showBoss = poseSink < 0.99f;
        }

        @Override
        protected void front(Batch batch) {
            sea(this, batch, seaLevel(), 1f);
            float bob = MathUtils.sin(time * 3f) * 6f;
            if (time >= CLIMAX_DEATH + 0.8f) {                           // le drapeau noir flotte
                draw(batch, flag, bossX() - 160f, seaLevel() + 10f + bob, fit(70f, 7), MathUtils.sin(time * 2f) * 6f, WHITE, 1f);
            }
            float rise = Interpolation.pow2Out.apply(progress(3.4f, 4.3f));   // le coffre remonte
            if (rise > 0f) {
                float y = MathUtils.lerp(seaLevel() - 120f, seaLevel() + 26f + bob, rise);
                draw(batch, chest, bossX() + 180f, y, fit(80f, 8), MathUtils.sin(time * 2.5f) * 5f, WHITE, 1f);
                glow(batch, bossX() + 180f, y, 200f, GOLD, 0.4f * rise);
            }
        }
    }
}
