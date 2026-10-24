package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;

/** Les scènes de phase et de mort des quatre rois des Mines d'Or (choix d'Astra, 2026-10-24). */
final class MinesBossScenes {

    private MinesBossScenes() {}

    static BossScene phase(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case BARON_OR -> new BaronPhase(kit);
            case GRAND_FOREUR -> new DrillPhase(kit);
            case MAITRE_FORGE -> new ForgePhase(kit);
            case COEUR_MONTAGNE -> new MountainPhase(kit);
            default -> null;
        };
    }

    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case BARON_OR -> new BaronDeath(kit);
            case GRAND_FOREUR -> new DrillDeath(kit);
            case MAITRE_FORGE -> new ForgeDeath(kit);
            case COEUR_MONTAGNE -> new MountainDeath(kit);
            default -> null;
        };
    }

    // ----- Accessoires -----

    static final Color ROCK = c("2a1a0e"), ROCK_LIGHT = c("4a3020"), LAVA = c("ff5a1a"), STONE = c("8a8a90");
    static final String[] ANVIL = {
        "oooooooooooooooooooooo", "oLLLLLLLLLLLLLLLLLLLLo", "ogggggggggggggggggggo.", ".ooggggggggggggggoo...",
        "....oggggggggggo......", ".....oddddddddo.......", "....oddddddddddo......", "...oddddddddddddo.....",
        "...oooooooooooooo....."};
    static final String[] PEBBLE = {".oo.", "oddo", "oddo", ".oo."};
    static final String[] BOULDER = {"..oooo..", ".oddddo.", "oddddddo", "oddwdddo", "odddddwo", "oddddddo", ".oddddo.", "..oooo.."};
    static final String[] TOP_HAT = {"..oooooo..", "..oooooo..", "..oooooo..", "..oyyyyo..", "oooooooooo"};
    static final String[] SWORD = {"..w..", ".wsw.", ".wsw.", ".wsw.", ".wsw.", ".wsw.", ".wsw.", ".wsw.", "ggggg", "..b..", "..b..", ".ggg."};
    static final String[] HAMMER = {"oooooo", "oddddo", "oooooo", "..bb..", "..bb..", "..bb..", "..bb..", "..bb..", "..bb.."};
    static final String[] SPIKE = {"...o...", "..odo..", "..odo..", ".oddo..", ".oddwo.", "oddddwo", "odddddo"};
    static final String[] CRYSTAL = {"..o..", ".owo.", "oww#o", "ow##o", "o###o", ".o#o.", "..o.."};
    static final String[] HEART = {
        ".ooo...ooo.", "orrro.orrro", "orwrrorrrro", "orwrrrrrrro", "orrrrrrrrdo", ".orrrrrrdo.", "..orrrrdo..",
        "...orrdo...", "....odo....", ".....o....."};

    static Color c(String hex) { return Color.valueOf(hex); }

    /** Les parois de la mine, à gauche et à droite. */
    static void walls(BossScene scene, Batch batch) {
        float w = scene.worldWidth(), h = scene.worldHeight();
        for (int i = 0; i < 12; i++) {
            float y = i * h / 11f, bulge = 40f * MathUtils.sin(i * 1.7f);
            scene.rect(batch, ROCK, 1f, -60f, y - 60f, w * 0.12f + bulge + 60f, h / 11f + 120f);
            scene.rect(batch, ROCK, 1f, w * 0.88f - bulge, y - 60f, w * 0.12f + bulge + 60f, h / 11f + 120f);
        }
    }

    /** Fait tomber des rochers et des cailloux du plafond. */
    static void cavein(BossScene scene, TextureRegion boulder, TextureRegion pebble, int count) {
        for (int i = 0; i < count; i++) {
            TextureRegion piece = i % 3 == 0 ? boulder : pebble;
            Cutscene.Particle rock = scene.place(piece, scene.worldWidth() * scene.random.nextFloat(),
                scene.worldHeight() + 60f + scene.random.nextFloat() * 600f, 0f, -200f, scene.fit(30f + 50f * scene.random.nextFloat(), 8), 2.2f);
            rock.gravity = 1800f;
            rock.spin = (scene.random.nextFloat() - 0.5f) * 300f;
        }
    }

    /** Le boss se défait en morceaux de {@code size} pixels qui tombent, teintés {@code color}. */
    static void crumble(BossScene scene, int size, Color color, float spread) {
        float scale = scene.bossScale(), left = scene.bossX() - scene.bossWidth() / 2f;
        int width = scene.boss.getRegionWidth(), height = scene.boss.getRegionHeight();
        for (int py = 0; py < height; py += size) {
            for (int px = 0; px < width; px += size) {
                int w = Math.min(size, width - px), h = Math.min(size, height - py);
                float x = left + (px + w / 2f) * scale, y = scene.bossBottom() + (height - py - h / 2f) * scale;
                Cutscene.Particle piece = scene.place(new TextureRegion(scene.boss, px, py, w, h), x, y,
                    (scene.random.nextFloat() - 0.5f) * spread, scene.random.nextFloat() * spread * 0.5f, scale, 1.6f + scene.random.nextFloat());
                piece.gravity = 1500f;
                piece.color.set(color);
                piece.spin = (scene.random.nextFloat() - 0.5f) * 200f;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Baron de l'Or
    // -------------------------------------------------------------------------

    /** Il croque une pièce d'or : ses dents deviennent dorées, des veines d'or courent le long des parois. */
    static final class BaronPhase extends BossScene {
        BaronPhase(CutsceneKit kit) { super(kit, Moment.PHASE, EnemyKind.BARON_OR, 2); }

        private float mouthY() { return bossBottom() + bossHeight() * 0.6f; }

        @Override
        protected void step(float delta) {
            if (at(1.4f)) { rumble(0.2f, 6f); sparks(bossX(), mouthY(), 30, GOLD, 500f, 14f); }
            if (at(CLIMAX_PHASE)) rumble(0.6f, 10f);
        }

        @Override
        protected void back(Batch batch) {
            walls(this, batch);
            // les veines d'or qui courent dans la roche, des bords vers le centre
            float run = progress(CLIMAX_PHASE - 0.3f, CLIMAX_PHASE + 0.9f);
            additive(batch);
            for (int v = 0; v < 8; v++) {
                float side = v % 2 == 0 ? -1f : 1f, x = side < 0 ? 0f : worldWidth(), y = worldHeight() * (0.15f + 0.11f * v);
                int segments = (int) (10 * run);
                java.util.Random zig = new java.util.Random(v * 17L);
                for (int s = 0; s < segments; s++) {
                    float nx = x - side * (40f + 30f * zig.nextFloat()), ny = y + (zig.nextFloat() - 0.5f) * 60f;
                    line(batch, GOLD, 0.9f, x, y, nx, ny, 8f);
                    line(batch, HOT, 0.8f, x, y, nx, ny, 3f);
                    x = nx;
                    y = ny;
                }
            }
            normal(batch);
            poseTilt = 6f * gesture(0.8f, 1.8f);                         // il croque
            poseTintColor.set(GOLD);
            poseTint = 0.3f * progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.5f);
        }

        @Override
        protected void front(Batch batch) {
            float k = Interpolation.pow2Out.apply(progress(0.5f, 1.3f));
            if (time < 1.4f) {                                          // la pièce monte à sa bouche
                float x = MathUtils.lerp(bossX() + bossWidth() * 0.7f, bossX() + bossWidth() * 0.12f, k);
                float y = MathUtils.lerp(bossBottom() + bossHeight() * 0.3f, mouthY(), k);
                draw(batch, coin, x, y, coinScale(0.07f), 0f, WHITE, 1f);
            } else if (time < 2.0f) {                                   // croquée
                draw(batch, new TextureRegion(coin, 0, 0, coin.getRegionWidth(), coin.getRegionHeight() / 2),
                    bossX() + bossWidth() * 0.12f, mouthY() - 20f, coinScale(0.07f), 0f, WHITE, 1f - progress(1.6f, 2.0f));
            }
            float teeth = progress(1.4f, 2.0f);                         // ses dents en or
            glow(batch, bossX(), mouthY() - bossHeight() * 0.05f, 140f, GOLD, 0.7f * teeth * (0.8f + 0.2f * MathUtils.sin(time * 9f)));
            flash(batch, GOLD, CLIMAX_PHASE, 0.3f, 0.45f);
        }
    }

    /** Son or se change en pierre ; la statue s'effrite ; son haut-de-forme roule au sol. */
    static final class BaronDeath extends BossScene {
        private final TextureRegion hat;

        BaronDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.BARON_OR, 0);
            hat = art("oy", new Color[] {c("141018"), c("ffc93a")}, TOP_HAT);
        }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_DEATH)) { rumble(0.7f, 12f); crumble(this, 6, c("9a9aa0"), 300f); sparks(bossX(), bossY(), 40, STONE, 600f, 20f); }
        }

        @Override
        protected void back(Batch batch) {
            walls(this, batch);
            float stone = progress(0.4f, CLIMAX_DEATH - 0.3f);
            poseShadow = 0.75f * stone;                                 // il devient gris, de pierre
            poseTintColor.set(STONE);
            poseTint = 0.95f * stone;
            poseX = (random.nextFloat() - 0.5f) * 8f * progress(1.8f, CLIMAX_DEATH);
            showBoss = time < CLIMAX_DEATH;
        }

        @Override
        protected void front(Batch batch) {
            float x, y, rot;
            if (time < CLIMAX_DEATH) {
                x = bossX() + poseX;
                y = bossBottom() + bossHeight() * 1.02f;
                rot = 0f;
            } else {                                                    // il tombe et roule
                float k = progress(CLIMAX_DEATH, 4.6f);
                x = bossX() + worldWidth() * 0.3f * Interpolation.pow2Out.apply(k);
                y = MathUtils.lerp(bossBottom() + bossHeight(), groundY() + 30f, Interpolation.bounceOut.apply(Math.min(1f, k * 3f)));
                rot = -720f * Interpolation.pow2Out.apply(k);
            }
            draw(batch, hat, x, y, fit(90f, 5), rot, time < CLIMAX_DEATH ? tmp.set(WHITE).lerp(STONE, 0.5f * progress(0.4f, 2f)) : WHITE, 1f);
            flash(batch, WHITE, CLIMAX_DEATH, 0.3f, 0.4f);
        }
    }

    // -------------------------------------------------------------------------
    // Grand Foreur
    // -------------------------------------------------------------------------

    /** Sa foreuse surchauffe au rouge ; la roche éclate en pics. */
    static final class DrillPhase extends BossScene {
        private final TextureRegion drill, spike;
        private float spin;

        DrillPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.GRAND_FOREUR, 2);
            drill = symbol(EnemySymbol.DRILL);
            spike = art("odw", new Color[] {c("1a0e06"), c("6a4a30"), c("a08060")}, SPIKE);
        }

        @Override protected void restart() { spin = 0f; }

        private float drillX() { return bossX() + bossWidth() * 0.7f; }
        private float drillY() { return bossBottom() + bossHeight() * 0.35f; }

        @Override
        protected void step(float delta) {
            spin += delta * MathUtils.lerp(200f, 2000f, progress(0f, CLIMAX_PHASE));
            if (time > 0.4f && time < CLIMAX_PHASE && random.nextFloat() < 0.5f) sparks(drillX(), drillY(), 2, LAVA, 600f, 12f);
            if (at(CLIMAX_PHASE)) { rumble(0.8f, 16f); sparks(bossX(), groundY(), 80, c("a08060"), 900f, 20f); }
        }

        @Override
        protected void back(Batch batch) {
            walls(this, batch);
            poseX = (random.nextFloat() - 0.5f) * 10f * progress(0.5f, CLIMAX_PHASE);
        }

        @Override
        protected void front(Batch batch) {
            float heat = progress(0.3f, CLIMAX_PHASE);
            glow(batch, drillX(), drillY(), 300f * (0.6f + heat), LAVA, 0.7f * heat);
            draw(batch, drill, drillX() + poseX, drillY(), iconScale(0.26f), -spin % 30f, tmp.set(WHITE).lerp(c("ff4a2a"), heat), 1f);
            // les pics de roche qui jaillissent du sol, du centre vers les bords
            for (int i = 0; i < 14; i++) {
                float delay = CLIMAX_PHASE + Math.abs(i - 6.5f) * 0.05f;
                float up = Interpolation.swingOut.apply(progress(delay, delay + 0.25f));
                if (up <= 0f) continue;
                float x = worldWidth() * (0.04f + i * 0.0708f), h = worldHeight() * (0.12f + 0.06f * ((i * 7) % 3));
                stretch(batch, spike, x, groundY() + h * up / 2f, fit(h * 0.6f, 7), fit(h, 7) * up, 0f, WHITE, 1f);
            }
            flash(batch, LAVA, CLIMAX_PHASE, 0.3f, 0.45f);
        }
    }

    /** Sa foreuse tousse, se grippe et explose en vapeur ; le tunnel s'effondre ; la poussière retombe. */
    static final class DrillDeath extends BossScene {
        private final TextureRegion drill, boulder, pebble;
        private final Array<Particle> steam = new Array<>(false, 128);
        private static final float[] COUGHS = {0.6f, 1.2f, 1.7f, 2.1f};

        DrillDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.GRAND_FOREUR, 0);
            drill = symbol(EnemySymbol.DRILL);
            boulder = art("odw", new Color[] {c("1a0e06"), c("5a4030"), c("8a6a50")}, BOULDER);
            pebble = art("od", new Color[] {c("1a0e06"), c("5a4030")}, PEBBLE);
        }

        @Override protected void restart() { steam.clear(); }

        private float drillX() { return bossX() + bossWidth() * 0.7f; }
        private float drillY() { return bossBottom() + bossHeight() * 0.35f; }

        private void puff(int count, float speed, float size) {
            for (int i = 0; i < count; i++) {
                Particle p = particle(drillX(), drillY(), 360f * random.nextFloat(), speed * (0.3f + 0.7f * random.nextFloat()), 2.4f,
                    size * (0.6f + 0.6f * random.nextFloat()));
                p.drag = 0.3f;
                steam.add(p);
            }
        }

        @Override
        protected void step(float delta) {
            for (float cough : COUGHS) if (at(cough)) { rumble(0.12f, 5f); puff(4, 120f, 120f); }
            if (at(CLIMAX_DEATH)) { rumble(0.6f, 12f); puff(40, 700f, 320f); sparks(drillX(), drillY(), 50, LAVA, 900f, 18f); }
            if (at(CLIMAX_DEATH + 0.5f)) { rumble(1.2f, 14f); cavein(this, boulder, pebble, 50); }
            update(steam, delta);
        }

        @Override
        protected void back(Batch batch) {
            walls(this, batch);
            poseShadow = 0.4f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 1f);
            poseY = -18f * progress(CLIMAX_DEATH + 0.5f, CLIMAX_DEATH + 1.2f);
        }

        @Override
        protected void front(Batch batch) {
            if (time < CLIMAX_DEATH) {
                float jam = 0f;                                         // elle tourne par à-coups
                for (float cough : COUGHS) if (time >= cough) jam += 1f;
                float angle = time * (400f - jam * 90f);
                draw(batch, drill, drillX() + (random.nextFloat() - 0.5f) * 3f * jam, drillY(), iconScale(0.26f), angle % 30f, WHITE, 1f);
            }
            for (Particle p : steam) {                                  // la vapeur
                float size = p.size * (1f + p.life());
                batch.setColor(0.9f, 0.9f, 0.92f, 0.55f * (1f - p.life()));
                batch.draw(soft, p.x - size / 2f, p.y - size / 2f, size, size);
            }
            float dust = progress(CLIMAX_DEATH + 0.5f, CLIMAX_DEATH + 0.9f) * (1f - progress(3.6f, 5.0f));
            fill(batch, c("6a5a4a"), 0.55f * dust);                     // la poussière, qui retombe
            flash(batch, WHITE, CLIMAX_DEATH, 0.3f, 0.5f);
        }
    }

    // -------------------------------------------------------------------------
    // Maître de Forge
    // -------------------------------------------------------------------------

    /** Il plonge son arme dans la lave ; l'enclume chauffe à blanc ; ses coups de marteau rythment les étincelles. */
    static final class ForgePhase extends BossScene {
        private static final float[] STRIKES = {0.5f, 1.0f, 1.4f, 1.75f, 2.0f, CLIMAX_PHASE};
        private final TextureRegion anvil, sword;

        ForgePhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.MAITRE_FORGE, 2);
            anvil = art("oLgd", new Color[] {c("0a0808"), c("8a8a9a"), c("4a4a5a"), c("2e2e3a")}, ANVIL);
            sword = art("wsgb", new Color[] {c("e8e8f0"), c("9a9ab0"), c("ffc93a"), c("6b3a1e")}, SWORD);
        }

        private float anvilX() { return bossX() + bossWidth() * 0.75f; }
        private float anvilY() { return groundY() + 60f; }

        @Override
        protected void step(float delta) {
            for (int i = 0; i < STRIKES.length; i++) {
                if (at(STRIKES[i])) {
                    boolean last = i == STRIKES.length - 1;
                    rumble(last ? 0.6f : 0.12f, last ? 14f : 5f);
                    sparks(anvilX(), anvilY() + 40f, last ? 120 : 30, last ? HOT : GOLD, last ? 1200f : 700f, last ? 24f : 16f);
                }
            }
        }

        @Override
        protected void back(Batch batch) {
            walls(this, batch);
            glow(batch, worldWidth() * 0.25f, groundY(), 700f, LAVA, 0.45f);
            float swing = 0f;                                           // son bras frappe au rythme
            for (float strike : STRIKES) swing = Math.max(swing, gesture(strike - 0.2f, strike + 0.05f));
            poseTilt = -6f * swing;
            poseX = 20f * swing;
        }

        @Override
        protected void front(Batch batch) {
            // la lave, et son arme plongée dedans puis ressortie rouge
            float poolX = worldWidth() * 0.25f;
            rect(batch, LAVA, 1f, poolX - 200f, groundY() - 20f, 400f, 40f);
            glow(batch, poolX, groundY(), 500f, LAVA, 0.6f + 0.1f * MathUtils.sin(time * 6f));
            float dip = Interpolation.pow2Out.apply(progress(0.2f, 0.6f)) * (1f - Interpolation.pow2In.apply(progress(1.2f, 1.6f)));
            float hot = progress(0.6f, 1.2f);
            draw(batch, sword, poolX, groundY() + 120f - 110f * dip, fit(200f, 12), 180f, tmp.set(WHITE).lerp(LAVA, hot), 1f);
            float white = progress(0.5f, CLIMAX_PHASE);                  // l'enclume chauffe à blanc
            draw(batch, anvil, anvilX(), anvilY(), fit(140f, 9), 0f, WHITE, 1f);
            additive(batch);
            draw(batch, anvil, anvilX(), anvilY(), fit(140f, 9), 0f, tmp.set(LAVA).lerp(WHITE, white), white);
            normal(batch);
            glow(batch, anvilX(), anvilY(), 320f, tmp.set(LAVA).lerp(HOT, white), 0.7f * white);
            flash(batch, HOT, CLIMAX_PHASE, 0.3f, 0.5f);
        }
    }

    /** Le feu tombe en braises ; son marteau tombe avec un dernier « ting » ; l'arme refroidit et grisaille. */
    static final class ForgeDeath extends BossScene {
        private final TextureRegion anvil, sword, hammer;

        ForgeDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.MAITRE_FORGE, 0);
            anvil = art("oLgd", new Color[] {c("0a0808"), c("8a8a9a"), c("4a4a5a"), c("2e2e3a")}, ANVIL);
            sword = art("wsgb", new Color[] {c("e8e8f0"), c("9a9ab0"), c("ffc93a"), c("6b3a1e")}, SWORD);
            hammer = art("odb", new Color[] {c("1a1a20"), c("5a5a66"), c("6b3a1e")}, HAMMER);
        }

        private float anvilX() { return bossX() + bossWidth() * 0.75f; }
        private float anvilY() { return groundY() + 60f; }

        @Override
        protected void step(float delta) {
            if (time < 3.5f && random.nextFloat() < 0.4f) {             // les braises qui montent
                Particle ember = particle(worldWidth() * 0.25f + (random.nextFloat() - 0.5f) * 300f, groundY(), 90f + (random.nextFloat() - 0.5f) * 40f,
                    80f + 80f * random.nextFloat(), 1.8f, 10f);
                ember.color.set(LAVA);
                glows.add(ember);
            }
            if (at(CLIMAX_DEATH)) { rumble(0.25f, 5f); sparks(anvilX(), anvilY() + 40f, 12, HOT, 400f, 12f); }
        }

        @Override
        protected void back(Batch batch) {
            walls(this, batch);
            float fire = 1f - progress(0.2f, CLIMAX_DEATH);
            glow(batch, worldWidth() * 0.25f, groundY(), 700f * (0.4f + 0.6f * fire), LAVA, 0.45f * fire + 0.1f);
            poseShadow = 0.6f * (1f - fire);
            float bow = progress(CLIMAX_DEATH + 0.2f, CLIMAX_DEATH + 1f);
            poseTilt = 5f * bow;
            poseY = -12f * bow;
        }

        @Override
        protected void front(Batch batch) {
            float fire = 1f - progress(0.2f, CLIMAX_DEATH);
            float poolX = worldWidth() * 0.25f;
            rect(batch, tmp.set(c("3a1a0a")).lerp(LAVA, fire), 1f, poolX - 200f, groundY() - 20f, 400f, 40f);
            float cool = progress(1.0f, 4.2f);                          // l'arme posée, du rouge au gris
            draw(batch, sword, anvilX(), anvilY() + 30f, fit(170f, 12), 90f, tmp.set(LAVA).lerp(STONE, cool), 1f);
            draw(batch, anvil, anvilX(), anvilY(), fit(140f, 9), 0f, WHITE, 1f);
            // son marteau lui glisse de la main et tombe sur l'enclume
            float fall = Interpolation.pow2In.apply(progress(1.6f, CLIMAX_DEATH));
            float bounce = time > CLIMAX_DEATH ? 20f * MathUtils.sin(progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.3f) * MathUtils.PI) : 0f;
            float y = MathUtils.lerp(bossBottom() + bossHeight() * 0.55f, anvilY() + 70f, fall) + bounce;
            float rot = time > CLIMAX_DEATH ? -70f - 20f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.3f) : -70f * fall;
            draw(batch, hammer, anvilX() - 40f, y, fit(110f, 9), rot, WHITE, 1f);
            flash(batch, WHITE, CLIMAX_DEATH, 0.12f, 0.25f);
        }
    }

    // -------------------------------------------------------------------------
    // Coeur de la Montagne
    // -------------------------------------------------------------------------

    /** Un cœur bat dans la roche ; une armure de cristal pousse sur lui, plaque après plaque. */
    static final class MountainPhase extends BossScene {
        private static final Color RED = c("ff3a3a"), CRYSTAL_COLOR = c("7ae8ff");
        private static final float[][] PLATES = {
            {-0.45f, 0.2f}, {0.45f, 0.2f}, {-0.5f, 0.5f}, {0.5f, 0.5f}, {-0.35f, 0.8f}, {0.35f, 0.8f}, {0f, 1.0f}, {-0.2f, 0.35f},
            {0.2f, 0.35f}, {0f, 0.6f}};
        private final TextureRegion heart, crystal;
        private float beat, nextBeat;

        MountainPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.COEUR_MONTAGNE, 2);
            heart = art("orwd", new Color[] {c("1a0606"), RED, c("ff9a9a"), c("8a1010")}, HEART);
            crystal = art("ow#", new Color[] {c("0a2a3a"), c("e0ffff"), CRYSTAL_COLOR}, CRYSTAL);
        }

        @Override protected void restart() { beat = 0f; nextBeat = 0.2f; }

        private float plateTime(int i) { return 1.0f + i * 0.13f; }

        @Override
        protected void step(float delta) {
            beat = Math.max(0f, beat - delta * 4f);
            if (time >= nextBeat) { beat = 1f; nextBeat += 0.55f; rumble(0.1f, 4f); }
            for (int i = 0; i < PLATES.length; i++) {
                if (at(plateTime(i))) sparks(plateX(i), plateY(i), 10, CRYSTAL_COLOR, 400f, 12f);
            }
            if (at(CLIMAX_PHASE)) { rumble(0.6f, 12f); sparks(bossX(), bossY(), 70, CRYSTAL_COLOR, 900f, 20f); }
        }

        private float plateX(int i) { return bossX() + PLATES[i][0] * bossWidth(); }
        private float plateY(int i) { return bossBottom() + PLATES[i][1] * bossHeight(); }

        @Override
        protected void back(Batch batch) {
            walls(this, batch);
            fill(batch, RED, 0.1f * beat);
            poseScale = 1f + 0.02f * beat;
            poseTintColor.set(CRYSTAL_COLOR);
            poseTint = 0.25f * progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.4f);
        }

        @Override
        protected void front(Batch batch) {
            float x = portraitX(17f), y = portraitY(33.5f);                // au centre de son cœur doré
            glow(batch, x, y, 260f * (1f + 0.3f * beat), RED, 0.5f + 0.4f * beat);
            draw(batch, heart, x, y, fit(70f * (1f + 0.15f * beat), 10), 0f, WHITE, 0.9f * (1f - progress(1.6f, CLIMAX_PHASE)));
            for (int i = 0; i < PLATES.length; i++) {                   // les plaques de cristal
                float grow = Interpolation.swingOut.apply(progress(plateTime(i), plateTime(i) + 0.2f));
                if (grow <= 0f) continue;
                draw(batch, crystal, plateX(i), plateY(i), fit(90f, 7) * grow, PLATES[i][0] * 40f, WHITE, 0.95f);
                additive(batch);
                draw(batch, crystal, plateX(i), plateY(i), fit(90f, 7) * grow, PLATES[i][0] * 40f, CRYSTAL_COLOR, 0.3f + 0.3f * beat);
                normal(batch);
            }
            flash(batch, CRYSTAL_COLOR, CLIMAX_PHASE, 0.3f, 0.45f);
        }
    }

    /** Ses battements ralentissent ; la montagne se fend ; la grotte s'effondre ; une veine d'or brille dans la faille. */
    static final class MountainDeath extends BossScene {
        private static final Color RED = c("ff3a3a");
        private static final float[] BEATS = {0.3f, 1.0f, 1.9f};
        private final TextureRegion heart, boulder, pebble;

        MountainDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.COEUR_MONTAGNE, 0);
            heart = art("orwd", new Color[] {c("1a0606"), RED, c("ff9a9a"), c("8a1010")}, HEART);
            boulder = art("odw", new Color[] {c("1a0e06"), c("5a4030"), c("8a6a50")}, BOULDER);
            pebble = art("od", new Color[] {c("1a0e06"), c("5a4030")}, PEBBLE);
        }

        private float beat() {
            float beat = 0f;
            for (float at : BEATS) if (time >= at) beat = Math.max(beat, 1f - (time - at) / 0.3f);
            return Math.max(0f, beat);
        }

        @Override
        protected void step(float delta) {
            for (float b : BEATS) if (at(b)) rumble(0.12f, 4f);
            if (at(CLIMAX_DEATH)) { rumble(1.2f, 16f); cavein(this, boulder, pebble, 40); }
        }

        /** @return la faille, de 0 (fermée) à 1 (ouverte). */
        private float split() { return Interpolation.pow2Out.apply(progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.8f)); }

        @Override
        protected void back(Batch batch) {
            walls(this, batch);
            float open = split(), w = worldWidth() * 0.5f;
            if (open > 0f) {                                            // la montagne s'ouvre : la veine d'or dans la faille
                float gap = 40f + 160f * open;
                rect(batch, Color.BLACK, 1f, w - gap / 2f, -60f, gap, worldHeight() + 120f);
                float vein = progress(3.4f, 4.2f);
                additive(batch);
                java.util.Random zig = new java.util.Random(5L);
                float y = -60f, x = w;
                while (y < worldHeight() + 60f) {
                    float nx = w + (zig.nextFloat() - 0.5f) * gap * 0.6f, ny = y + 60f;
                    line(batch, GOLD, 0.9f * vein, x, y, nx, ny, 10f);
                    line(batch, HOT, vein, x, y, nx, ny, 4f);
                    x = nx;
                    y = ny;
                }
                normal(batch);
                glow(batch, w, worldHeight() * 0.5f, worldHeight(), GOLD, 0.3f * vein);
            }
            poseShadow = 0.4f + 0.3f * progress(1f, CLIMAX_DEATH);
            showBoss = time < CLIMAX_DEATH;                             // ensuite : ses deux moitiés, dessinées devant
        }

        @Override
        protected void front(Batch batch) {
            float beat = beat();
            if (time < CLIMAX_DEATH) {
                float x = portraitX(17f), y = portraitY(33.5f);                // au centre de son cœur doré
                glow(batch, x, y, 240f * (1f + 0.3f * beat), RED, 0.2f + 0.5f * beat);
                draw(batch, heart, x, y, fit(70f * (1f + 0.15f * beat), 10), 0f, tmp.set(WHITE).lerp(STONE, progress(0.3f, 2.2f)), 0.9f);
            } else {                                                    // il se fend avec la montagne : ses deux moitiés s'écartent
                float open = split();
                int half = boss.getRegionWidth() / 2;
                TextureRegion left = new TextureRegion(boss, 0, 0, half, boss.getRegionHeight());
                TextureRegion right = new TextureRegion(boss, half, 0, boss.getRegionWidth() - half, boss.getRegionHeight());
                float scale = bossScale(), height = boss.getRegionHeight() * scale, shift = bossWidth() * 0.7f * open;
                batch.setColor(0.55f, 0.55f, 0.55f, 1f);
                batch.draw(left, bossX() - half * scale - shift, bossBottom(), half * scale, 0f, half * scale, height, 1f, 1f, 10f * open);
                batch.draw(right, bossX() + shift, bossBottom(), 0f, 0f, right.getRegionWidth() * scale, height, 1f, 1f, -10f * open);
            }
            float dust = progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.4f) * (1f - progress(3.2f, 4.4f));
            fill(batch, c("6a5a4a"), 0.45f * dust);
            flash(batch, WHITE, CLIMAX_DEATH, 0.3f, 0.5f);
        }
    }
}
