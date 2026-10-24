package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;

/** Les scènes de phase et de mort des quatre rois de la prairie (choix d'Astra, 2026-10-24). */
final class PrairieBossScenes {

    private PrairieBossScenes() {}

    static BossScene phase(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case ROI_PIQUE -> new SpadePhase(kit);
            case ROI_TREFLE -> new ClubPhase(kit);
            case ROI_COEUR -> new HeartPhase(kit);
            case ROI_CARREAU -> new DiamondPhase(kit);
            default -> null;
        };
    }

    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case ROI_PIQUE -> new SpadeDeath(kit);
            case ROI_TREFLE -> new ClubDeath(kit);
            case ROI_COEUR -> new HeartDeath(kit);
            case ROI_CARREAU -> new DiamondDeath(kit);
            default -> null;
        };
    }

    // ----- Accessoires -----

    static final Color[] METAL = {c("140a0a"), c("e8e8f0"), c("9a9ab0"), c("ffc93a"), c("6b3a1e")};
    /** Une lame (pointe en bas, garde dorée en haut). */
    static final String[] BLADE = {
        ".ggg.", ".gog.", "..b..", "..b..", "ggggg", ".sws.", ".sws.", ".sws.", ".sws.", ".sws.", ".sws.", ".sws.",
        ".sws.", "..s.."};
    /** Un cœur (sert aussi de feuille de trèfle, en vert). */
    static final String[] HEART = {
        ".ooo...ooo.", "orrro.orrro", "orwrrorrrro", "orwrrrrrrro", "orrrrrrrrdo", ".orrrrrrdo.", "..orrrrdo..",
        "...orrdo...", "....odo....", ".....o....."};
    static final String[] STAINED_GLASS = {
        ".ooo...ooo.", "oyrro.orrpo", "orrpoooprro", "oprroyorrdo", "ooooooooooo", ".oyrrorpdo.", "..orrordo..",
        "...ooyoo...", "....odo....", ".....o....."};
    static final String[] BOTTLE = {
        "..oo..", "..gg..", "..gg..", ".gggg.", "gggggg", "gwgggg", "gwgggg", "gggggg", "gggggg", "oooooo"};
    static final String[] CUP = {
        "ooooooo", "oyyyyyo", "oywyyyo", ".oyyyo.", "..oyo..", "...o...", "..ooo..", ".ooooo."};
    static final String[] SHIELD = {
        "ooooooooo", "oyyyyyyyo", "oyrrrrryo", "oyrryrryo", "oyryyyryo", "oyrryrryo", ".oyrrryo.", "..oyyyo..",
        "...oyo...", "....o...."};
    static final String[] BOOK = {
        "oooooooooooo", "oyyyyyoyyyyo", "oywwwyowwwyo", "oyyyyyoyyyyo", "oywwwyowwwyo", "oyyyyyoyyyyo", "oywwwyowwwyo",
        "oyyyyyoyyyyo", "oooooooooooo"};

    /** Une pile de pièces de {@code count} pièces, posée en ({@code x}, {@code y}). */
    static void stack(BossScene scene, Batch batch, TextureRegion coin, float x, float y, int count, float size) {
        float scale = size / coin.getRegionWidth(), step = size * 0.28f;
        for (int i = 0; i < count; i++) scene.draw(batch, coin, x, y + i * step, scale, 0f, BossScene.WHITE, 1f);
    }

    // -------------------------------------------------------------------------
    // Roi de Pique
    // -------------------------------------------------------------------------

    /** Son épée ruisselle de rouge, le cercle de lames se dresse et se tourne vers le joueur, ses yeux rougissent. */
    static final class SpadePhase extends BossScene {
        private static final Color RED = c("ff2a3a");
        private final TextureRegion blade, sword;

        SpadePhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.ROI_PIQUE, 2);
            blade = art("owsgb", METAL, BLADE);
            sword = blade;
        }

        private float swordX() { return bossX() + bossWidth() * 0.62f; }
        private float swordY() { return bossBottom() + bossHeight() * 0.45f; }

        @Override
        protected void step(float delta) {
            if (time > 0.3f && time < 2.0f && random.nextFloat() < 0.35f) {   // le sang goutte de la lame
                Particle drop = particle(swordX() + (random.nextFloat() - 0.5f) * 10f, swordY() - 60f, 270f,
                    60f, 1.0f, 12f);
                drop.gravity = 900f;
                drop.color.set(RED);
                glows.add(drop);
            }
            if (at(CLIMAX_PHASE)) { rumble(0.6f, 12f); sparks(bossX(), bossY(), 80, RED, 900f, 22f); }
        }

        @Override
        protected void back(Batch batch) {
            // le cercle de lames : couchées au sol, elles se dressent puis pointent vers le joueur
            float rise = Interpolation.pow2Out.apply(progress(0.6f, 1.6f));
            float turn = Interpolation.swingOut.apply(progress(1.7f, CLIMAX_PHASE));
            float scale = fit(worldHeight() * 0.16f, 14);
            for (int i = 0; i < 10; i++) {
                float angle = i * 36f + time * 20f;
                float x = bossX() + MathUtils.cosDeg(angle) * worldWidth() * 0.32f;
                float y = bossY() + MathUtils.sinDeg(angle) * worldHeight() * 0.3f * rise - (1f - rise) * worldHeight() * 0.2f;
                float rotation = MathUtils.lerp(angle + 90f, 0f, turn);          // la pointe vers le bas : vers le joueur
                draw(batch, blade, x, y, scale * (1f + 0.25f * turn), rotation, turn > 0f ? tmp.set(WHITE).lerp(RED, 0.5f * turn) : WHITE,
                    0.4f + 0.6f * rise);
            }
            poseTintColor.set(RED);
            poseTint = 0.25f * progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.4f);
            poseY = 8f * gesture(CLIMAX_PHASE - 0.2f, CLIMAX_PHASE + 0.3f);
        }

        @Override
        protected void front(Batch batch) {
            float red = progress(0.3f, 1.8f);
            draw(batch, sword, swordX(), swordY(), fit(worldHeight() * 0.2f, 14), 180f, tmp.set(WHITE).lerp(RED, 0.7f * red), 1f);
            eyes(batch, progress(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.2f), RED, 0.72f, 0.1f);
            wave(batch, CLIMAX_PHASE, 0.8f, bossX(), bossY(), RED, worldWidth() * 1.3f);
            flash(batch, RED, CLIMAX_PHASE, 0.3f, 0.45f);
        }
    }

    /** Son épée se brise en deux, les lames tombent comme des dominos, il pose un genou à terre. */
    static final class SpadeDeath extends BossScene {
        private final TextureRegion blade, top, bottom;

        SpadeDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.ROI_PIQUE, 0);
            blade = art("owsgb", METAL, BLADE);
            top = new TextureRegion(blade, 0, 0, 5, 7);
            bottom = new TextureRegion(blade, 0, 7, 5, 7);
        }

        private float swordX() { return bossX() + bossWidth() * 0.62f; }
        private float swordY() { return bossBottom() + bossHeight() * 0.45f; }

        @Override
        protected void step(float delta) {
            if (at(1.2f)) rumble(0.2f, 5f);
            if (at(CLIMAX_DEATH)) {                                     // l'épée casse
                rumble(0.6f, 12f);
                sparks(swordX(), swordY(), 70, WHITE, 900f, 20f);
                float scale = fit(worldHeight() * 0.2f, 14);
                Particle a = place(top, swordX(), swordY() + 40f, 300f, 700f, scale, 2.4f);
                a.gravity = 1600f;
                a.spin = 500f;
                Particle b = place(bottom, swordX(), swordY() - 40f, -120f, 200f, scale, 2.4f);
                b.gravity = 1600f;
                b.spin = -300f;
            }
            for (int i = 0; i < 9; i++) if (at(CLIMAX_DEATH + 0.3f + i * 0.12f)) rumble(0.08f, 3f);
        }

        @Override
        protected void back(Batch batch) {
            float scale = fit(worldHeight() * 0.2f, 14);
            for (int i = 0; i < 9; i++) {                               // les lames plantées, qui tombent l'une sur l'autre
                float fall = Interpolation.bounceOut.apply(progress(CLIMAX_DEATH + 0.3f + i * 0.12f, CLIMAX_DEATH + 0.6f + i * 0.12f));
                float x = worldWidth() * (0.06f + i * 0.11f);
                if (Math.abs(x - bossX()) < bossWidth() * 0.6f) continue;
                draw(batch, blade, x + 40f * fall, groundY() + worldHeight() * 0.1f - 30f * fall, scale, 180f - 80f * fall, WHITE, 1f);
            }
            float kneel = Interpolation.pow2Out.apply(progress(CLIMAX_DEATH + 0.2f, CLIMAX_DEATH + 0.8f));
            poseStretch = -0.22f * kneel;                               // un genou à terre
            poseY = -20f * kneel;
            poseTilt = 6f * kneel;
            poseShadow = 0.5f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 1.5f);
        }

        @Override
        protected void front(Batch batch) {
            if (time < CLIMAX_DEATH) {
                float shake = (random.nextFloat() - 0.5f) * 8f * progress(1.2f, CLIMAX_DEATH);
                draw(batch, blade, swordX() + shake, swordY(), fit(worldHeight() * 0.2f, 14), 180f, WHITE, 1f);
                float crack = progress(1.2f, CLIMAX_DEATH);
                line(batch, WHITE, crack, swordX() - 14f, swordY(), swordX() + 14f, swordY() + 6f, 4f);
            }
            flash(batch, WHITE, CLIMAX_DEATH, 0.3f, 0.6f);
        }
    }

    // -------------------------------------------------------------------------
    // Roi de Trèfle
    // -------------------------------------------------------------------------

    /** Il vide une bouteille sur la table ; l'écran tangue, les pièces glissent. */
    static final class ClubPhase extends BossScene {
        private static final Color GREEN = c("3fbf5a"), WOOD = c("5a3a1e"), DRINK = c("c8902a");
        private final TextureRegion bottle;

        ClubPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.ROI_TREFLE, 2);
            bottle = art("ogw", new Color[] {c("0a1a0c"), c("2a8a3a"), c("bfffc8")}, BOTTLE);
        }

        private float sway() { return 5f * MathUtils.sin(time * 2.6f) * progress(CLIMAX_PHASE - 0.6f, CLIMAX_PHASE + 0.3f); }
        private float tableY() { return groundY() + 60f; }

        @Override
        protected void step(float delta) {
            if (time > 0.8f && time < 2.0f) {                           // la bouteille se vide
                Particle drop = particle(bottleX() - 30f, bottleY() - 40f, 260f, 120f, 0.6f, 14f);
                drop.gravity = 1400f;
                drop.color.set(DRINK);
                glows.add(drop);
            }
            if (at(CLIMAX_PHASE)) { rumble(0.7f, 10f); sparks(bossX(), tableY() + 30f, 60, GREEN, 700f, 20f); }
        }

        private float bottleX() { return bossX() + bossWidth() * 0.45f; }
        private float bottleY() { return tableY() + 180f; }

        @Override
        protected void back(Batch batch) {
            beginTilt(batch, sway());
            poseY = 40f;
            poseTilt = 4f * gesture(0.5f, 2.0f);
        }

        @Override
        protected void front(Batch batch) {
            float w = worldWidth() * 0.8f;
            rect(batch, WOOD, 1f, bossX() - w / 2f, groundY() - 10f, w, 70f);
            float spill = progress(0.8f, 2.0f);                          // la flaque sur la table
            rect(batch, DRINK, 0.8f, bossX() - 40f - 160f * spill, tableY() - 4f, 320f * spill + 40f, 8f);
            float tilt = Interpolation.pow2Out.apply(progress(0.5f, 0.9f)) * (1f - progress(2.0f, 2.3f));
            draw(batch, bottle, bottleX(), bottleY(), fit(210f, 10), 120f * tilt, WHITE, 1f);
            // les pièces glissent avec le tangage
            float slide = time < CLIMAX_PHASE ? 0f : (time - CLIMAX_PHASE);
            for (int i = 0; i < 7; i++) {
                float x = bossX() - w * 0.4f + i * w * 0.13f + MathUtils.sin(time * 2.6f) * 60f * slide;
                draw(batch, coin, x, tableY() + 10f, coinScale(0.045f), 0f, WHITE, 1f);
            }
            endTilt(batch);
            wave(batch, CLIMAX_PHASE, 0.8f, bossX(), bossY(), GREEN, worldWidth() * 1.3f);
            flash(batch, GREEN, CLIMAX_PHASE, 0.3f, 0.4f);
        }
    }

    /** Son trèfle à quatre feuilles perd ses feuilles une à une ; ses poches sont vides ; la dernière feuille s'envole. */
    static final class ClubDeath extends BossScene {
        private final TextureRegion leaf;
        private static final float[] DROPS = {1.0f, 1.6f, 2.1f};
        private static final Color POCKET = c("e8e0d0");

        ClubDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.ROI_TREFLE, 0);
            leaf = art("orwd", new Color[] {c("0a1a0c"), c("3fbf5a"), c("9af0a8"), c("1f7a34")}, HEART);
        }

        private float cloverX() { return bossX() - bossWidth() * 0.75f; }
        private float cloverY() { return worldHeight() * 0.68f; }

        @Override
        protected void step(float delta) {
            for (int i = 0; i < DROPS.length; i++) {
                if (at(DROPS[i])) {                                     // une feuille tombe
                    float angle = i * 90f;
                    Particle piece = place(leaf, cloverX() + MathUtils.cosDeg(angle + 90f) * 50f,
                        cloverY() + MathUtils.sinDeg(angle + 90f) * 50f, (random.nextFloat() - 0.5f) * 80f, 0f, fit(84f, 10), 2.6f);
                    piece.gravity = 260f;
                    piece.rotation = angle;
                    piece.spin = (random.nextFloat() - 0.5f) * 200f;
                    piece.drag = 0.5f;
                }
            }
            if (at(CLIMAX_DEATH)) rumble(0.4f, 7f);
        }

        @Override
        protected void back(Batch batch) {
            poseShadow = 0.45f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 1.2f);
            poseY = -14f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.6f);
            poseTilt = -3f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.6f);
        }

        @Override
        protected void front(Batch batch) {
            // le trèfle, avec les feuilles qui lui restent
            for (int i = 0; i < 4; i++) {
                if (i < DROPS.length && time >= DROPS[i]) continue;
                float angle = i * 90f, x = cloverX(), y = cloverY();
                if (i == 3 && time >= CLIMAX_DEATH) {                   // la dernière s'envole
                    float k = progress(CLIMAX_DEATH, CLIMAX_DEATH + 2.2f);
                    x += worldWidth() * 0.9f * Interpolation.pow2In.apply(k);
                    y += worldHeight() * 0.3f * k + 30f * MathUtils.sin(k * 14f);
                    angle += 900f * k;
                }
                draw(batch, leaf, x + MathUtils.cosDeg(angle + 90f) * 50f, y + MathUtils.sinDeg(angle + 90f) * 50f,
                    fit(84f, 10), angle, WHITE, 1f);
            }
            glow(batch, cloverX(), cloverY(), 200f, c("3fbf5a"), 0.4f * (1f - progress(0.6f, CLIMAX_DEATH)));
            // ses poches retournées, vides
            float out = Interpolation.swingOut.apply(progress(CLIMAX_DEATH + 0.2f, CLIMAX_DEATH + 0.6f));
            for (int side = -1; side <= 1; side += 2) {
                float x = bossX() + poseX + side * bossWidth() * 0.42f, y = bossBottom() + poseY + bossHeight() * 0.3f;
                rect(batch, POCKET, out, x - 34f, y - 80f * out, 68f, 80f * out);
            }
            if (out > 0.5f && random.nextFloat() < 0.04f) sparks(bossX(), bossBottom() + bossHeight() * 0.25f, 4, c("aaaaaa"), 120f, 8f);
        }
    }

    // -------------------------------------------------------------------------
    // Roi de Coeur
    // -------------------------------------------------------------------------

    /** Son vitrail se fissure en rouge et bat plus vite ; il écrase sa coupe ; une brume rouge monte. */
    static final class HeartPhase extends BossScene {
        private static final Color RED = c("e0283a");
        private final TextureRegion glass, cup;
        private float beat, nextBeat;

        HeartPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.ROI_COEUR, 2);
            glass = art("orpyd", new Color[] {c("140a10"), c("e0283a"), c("ff8a9a"), c("ffc93a"), c("8a1020")}, STAINED_GLASS);
            cup = art("oyw", new Color[] {c("4a3008"), c("ffc93a"), c("fff0a0")}, CUP);
        }

        @Override protected void restart() { beat = 0f; nextBeat = 0.2f; }

        @Override
        protected void step(float delta) {
            beat = Math.max(0f, beat - delta * 4f);
            if (time >= nextBeat) {                                     // le cœur bat de plus en plus vite
                beat = 1f;
                nextBeat += MathUtils.lerp(0.7f, 0.18f, progress(0f, CLIMAX_PHASE));
            }
            if (at(CLIMAX_PHASE)) {                                     // il écrase sa coupe
                rumble(0.6f, 12f);
                sparks(cupX(), cupY(), 60, RED, 700f, 20f);
                spray(cup, cupX(), cupY(), 1, 60f, 120f, 300f, fit(70f, 8), 1600f, 1.2f);
            }
            if (time > CLIMAX_PHASE && random.nextFloat() < 0.6f) {     // la brume rouge
                Particle mist = particle(worldWidth() * random.nextFloat(), groundY(), 90f, 60f + 60f * random.nextFloat(),
                    2.2f, 200f + 160f * random.nextFloat());
                mist.color.set(RED.r, RED.g, RED.b, 0.25f);
                glows.add(mist);
            }
        }

        private float cupX() { return bossX() + bossWidth() * 0.5f; }
        private float cupY() { return bossBottom() + bossHeight() * 0.5f; }

        @Override
        protected void back(Batch batch) {
            float x = bossX(), y = worldHeight() * 0.7f, size = worldHeight() * 0.5f;
            float pump = 1f + 0.06f * beat;
            glow(batch, x, y, size * 1.6f * pump, RED, 0.25f + 0.35f * beat);
            draw(batch, glass, x, y, fit(size, 10) * pump, 0f, WHITE, 0.9f);
            float cracks = progress(0.9f, CLIMAX_PHASE);                // les fissures rouges
            additive(batch);
            for (int i = 0; i < 6; i++) {
                float angle = 30f + i * 60f + (i % 2) * 17f, len = size * 0.45f * Math.min(1f, cracks * 6f - i);
                if (len > 0f) line(batch, RED, 0.9f, x, y, x + MathUtils.cosDeg(angle) * len, y + MathUtils.sinDeg(angle) * len, 9f);
            }
            normal(batch);
            poseTintColor.set(RED);
            poseTint = 0.15f * beat + 0.2f * progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.5f);
        }

        @Override
        protected void front(Batch batch) {
            if (time < CLIMAX_PHASE) {
                float squeeze = progress(1.6f, CLIMAX_PHASE);
                stretch(batch, cup, cupX(), cupY(), fit(120f, 8) * (1f - 0.4f * squeeze), fit(120f, 8) * (1f - 0.2f * squeeze), 0f, WHITE, 1f);
            }
            fill(batch, RED, 0.12f * progress(CLIMAX_PHASE, CLIMAX_PHASE + 1.2f));
            flash(batch, RED, CLIMAX_PHASE, 0.3f, 0.5f);
        }
    }

    /** Un tracé de battements ralentit puis devient plat ; le vitrail se défait en pétales. */
    static final class HeartDeath extends BossScene {
        private static final Color RED = c("e0283a"), LINE = c("6aff8a");
        private static final float[] BEATS = {0.3f, 0.9f, 1.6f, 2.5f};
        private final TextureRegion glass, petal;

        HeartDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.ROI_COEUR, 0);
            glass = art("orpyd", new Color[] {c("140a10"), c("e0283a"), c("ff8a9a"), c("ffc93a"), c("8a1020")}, STAINED_GLASS);
            petal = art("rp", new Color[] {c("e0283a"), c("ff8a9a")}, ".rr.", "rppr", "rrrr", ".rr.");
        }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_DEATH)) {                                     // le vitrail se défait en pétales
                rumble(0.4f, 6f);
                float x = bossX(), y = worldHeight() * 0.7f;
                for (int i = 0; i < 70; i++) {
                    Particle p = place(petal, x + (random.nextFloat() - 0.5f) * worldHeight() * 0.5f,
                        y + (random.nextFloat() - 0.5f) * worldHeight() * 0.4f, (random.nextFloat() - 0.3f) * 260f,
                        random.nextFloat() * 120f, fit(18f + 14f * random.nextFloat(), 4), 2.4f + random.nextFloat());
                    p.gravity = 120f;
                    p.drag = 0.6f;
                    p.spin = (random.nextFloat() - 0.5f) * 300f;
                }
            }
        }

        @Override
        protected void back(Batch batch) {
            if (time < CLIMAX_DEATH) {
                float x = bossX(), y = worldHeight() * 0.7f, size = worldHeight() * 0.5f;
                float dim = progress(0.3f, CLIMAX_DEATH);
                glow(batch, x, y, size * 1.4f, RED, 0.35f * (1f - dim));
                draw(batch, glass, x, y, fit(size, 10), 0f, tmp.set(WHITE).lerp(c("806070"), dim), 0.9f);
            }
            poseShadow = 0.6f * progress(1.0f, CLIMAX_DEATH + 0.8f);
            poseY = -16f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 1f);
        }

        @Override
        protected void front(Batch batch) {
            // le tracé des battements, de gauche à droite ; le dernier pic n'arrive pas
            float y0 = worldHeight() * 0.86f, w = worldWidth();
            float head = w * progress(0f, CLIMAX_DEATH + 0.8f);
            rect(batch, Color.BLACK, 0.45f, 0f, y0 - 70f, w, 140f);
            float px = 0f, py = y0;
            for (float x = 8f; x <= head; x += 8f) {
                float t = (x / w) * (CLIMAX_DEATH + 0.8f), y = y0;
                for (int i = 0; i < BEATS.length - 1; i++) {
                    float d = (t - BEATS[i]) * 12f, strength = 1f - i * 0.28f;
                    if (d > 0f && d < 1f) y += MathUtils.sin(d * MathUtils.PI2) * 60f * strength;
                }
                line(batch, LINE, 0.9f, px, py, x, y, 4f);
                px = x;
                py = y;
            }
            glow(batch, head, py, 50f, LINE, 0.8f);
        }
    }

    // -------------------------------------------------------------------------
    // Roi de Carreau
    // -------------------------------------------------------------------------

    /** Ses pièces se dédoublent, les piles montent comme des tours ; un grand livre d'or tourne ses pages. */
    static final class DiamondPhase extends BossScene {
        private static final Color ORANGE = c("ff9a2a");
        private final TextureRegion book, page;

        DiamondPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.ROI_CARREAU, 2);
            book = art("oyw", new Color[] {c("4a2a08"), c("ffc93a"), c("fff0c0")}, BOOK);
            page = art("w", new Color[] {c("fff0c0")}, "wwwww", "wwwww", "wwwww", "wwwww", "wwwww", "wwwww", "wwwww");
        }

        private int doublings() { return Math.min(5, (int) (time / 0.4f)); }

        @Override
        protected void step(float delta) {
            for (int i = 1; i <= 5; i++) if (at(i * 0.4f)) { rumble(0.1f, 3f + i); sparks(bossX(), groundY() + 40f, 10 * i, GOLD, 500f, 14f); }
            if (at(CLIMAX_PHASE)) { rumble(0.7f, 12f); spray(coin, bossX(), groundY() + 60f, 40, 30f, 150f, 1200f, coinScale(0.035f), 1500f, 2f); }
        }

        @Override
        protected void back(Batch batch) {
            // les piles de pièces : de plus en plus nombreuses, de plus en plus hautes
            int piles = 1 << doublings();
            float grow = Interpolation.pow2Out.apply(progress(CLIMAX_PHASE - 0.3f, CLIMAX_PHASE + 0.4f));
            for (int i = 0; i < Math.min(piles, 16); i++) {
                int side = i % 2 == 0 ? 1 : -1;
                float x = bossX() + side * (bossWidth() * 0.65f + (i / 2) * worldWidth() * 0.045f);
                int height = 2 + (i * 7 % 5) + (int) (14 * grow * (1f - (i / 2) * 0.08f));
                stack(this, batch, coin, x, groundY() + 10f, height, worldHeight() * 0.05f);
            }
            poseTintColor.set(GOLD);
            poseTint = 0.25f * progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.5f);
        }

        @Override
        protected void front(Batch batch) {
            float x = bossX(), y = worldHeight() * 0.8f, scale = fit(worldHeight() * 0.14f, 9);
            float open = progress(0.2f, 0.6f);
            glow(batch, x, y, 320f, GOLD, 0.5f * open);
            draw(batch, book, x, y, scale, 0f, WHITE, open);
            float flip = (time * (1.2f + 3f * progress(0f, CLIMAX_PHASE))) % 1f;      // ses pages tournent de plus en plus vite
            float side = MathUtils.cosDeg(flip * 180f);
            stretch(batch, page, x + side * scale * 3f, y, scale * Math.abs(side) * 1.1f, scale, 0f, WHITE, open);
            wave(batch, CLIMAX_PHASE, 0.8f, bossX(), bossY(), ORANGE, worldWidth() * 1.3f);
            flash(batch, GOLD, CLIMAX_PHASE, 0.3f, 0.45f);
        }
    }

    /** Son tas de trésor l'avale comme des sables mouvants ; son bouclier tourne sur le sol et tombe à plat. */
    static final class DiamondDeath extends BossScene {
        private final TextureRegion shield;
        private static final Color PILE = c("d8a020"), PILE_LIGHT = c("ffd860");

        DiamondDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.ROI_CARREAU, 0);
            shield = art("oyr", new Color[] {c("3a1a04"), c("ffc93a"), c("ff9a2a")}, SHIELD);
        }

        @Override
        protected void step(float delta) {
            if (time > 0.5f && time < CLIMAX_DEATH + 0.8f && random.nextFloat() < 0.5f) {   // les pièces glissent vers lui
                Particle p = place(coin, bossX() + (random.nextBoolean() ? 1 : -1) * worldWidth() * (0.3f + 0.15f * random.nextFloat()),
                    groundY() + 60f, 0f, 0f, 1.0f, coinScale(0.035f));
                p.vx = (bossX() - p.x) * 0.9f;
                p.vy = -10f;
            }
            if (at(CLIMAX_DEATH)) { rumble(0.6f, 10f); sparks(bossX(), groundY() + 80f, 60, GOLD, 700f, 18f); }
            if (at(4.0f)) rumble(0.15f, 4f);
        }

        @Override
        protected void back(Batch batch) {
            float sink = Interpolation.pow2In.apply(progress(0.6f, CLIMAX_DEATH + 0.4f));
            poseSink = sink;                                            // le haut de son portrait reste, le bas disparaît dans l'or
            poseX = MathUtils.sin(time * 9f) * 6f * (1f - sink);
            showBoss = sink < 0.99f;
        }

        @Override
        protected void front(Batch batch) {
            // le tas de trésor, devant lui
            float w = bossWidth() * 2.2f;
            for (int i = 0; i < 14; i++) {
                float k = i / 13f, h = (1f - (2f * k - 1f) * (2f * k - 1f)) * worldHeight() * 0.1f + worldHeight() * 0.08f;
                rect(batch, i % 2 == 0 ? PILE : PILE_LIGHT, 1f, bossX() - w / 2f + k * w - w / 26f, groundY(), w / 13f + 2f, h);
            }
            for (int i = 0; i < 9; i++) {
                float shimmer = 0.5f + 0.5f * MathUtils.sin(time * 5f + i * 1.7f);
                glow(batch, bossX() - w * 0.4f + i * w * 0.1f, groundY() + 40f + (i % 3) * 20f, 30f, HOT, 0.6f * shimmer);
            }
            if (time < CLIMAX_DEATH) return;
            // son bouclier : il roule, tourne sur la tranche de plus en plus lentement, puis tombe à plat
            float k = progress(CLIMAX_DEATH, 4.0f);
            float x = bossX() + worldWidth() * 0.22f * Interpolation.pow2Out.apply(k), y = groundY() + 60f;
            float scale = fit(170f, 10);
            float wide = k < 1f ? Math.abs(MathUtils.cos(k * k * 30f)) * (1f - k) + 0.12f : 1f;
            float flat = k < 1f ? 1f : 0.35f;
            stretch(batch, shield, x, y, scale * wide, scale * flat, 0f, WHITE, 1f);
        }
    }

    static Color c(String hex) { return Color.valueOf(hex); }
}
