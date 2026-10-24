package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;

/**
 * Les scènes de phase et de mort des boss de la Tour des épreuves (choix d'Astra, 2026-10-24).
 * La phase de la Comète et toute la Machine Originelle ont leurs propres cinématiques.
 */
final class TowerBossScenes {

    private TowerBossScenes() {}

    static BossScene phase(CutsceneKit kit, EnemyKind kind, int phase) {
        return switch (kind) {
            case REINE -> new QueenPhase(kit);
            case ECLAT -> new ShardPhase(kit);
            case PRETENDANT -> phase == 2 ? new PretenderShard(kit) : new PretenderQueen(kit);
            case MAISON -> phase == 2 ? new HouseFacade(kit) : new HouseVault(kit);
            default -> null;
        };
    }

    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case COMETE -> new CometDeath(kit);
            case REINE -> new QueenDeath(kit);
            case PRETENDANT -> new PretenderDeath(kit);
            case MAISON -> new HouseDeath(kit);
            default -> null;
        };
    }

    // ----- Accessoires -----


    // -------------------------------------------------------------------------
    // Comète Dorée : elle remonte, se défait en étoiles filantes, devient une constellation
    // -------------------------------------------------------------------------

    static final class CometDeath extends BossScene {
        static final float RISE = 1.2f;
        private final Starfield stars = new Starfield(160, 0.3f);
        private final TextureRegion city;
        private final Array<Particle> meteors = new Array<>(false, 64);
        /** La constellation : ses étoiles (en part de l'écran) et les traits qui les relient. */
        private static final float[][] CONSTELLATION = {
            {0.62f, 0.78f}, {0.66f, 0.84f}, {0.71f, 0.80f}, {0.68f, 0.73f}, {0.60f, 0.70f},
            {0.52f, 0.66f}, {0.43f, 0.62f}, {0.34f, 0.60f}, {0.50f, 0.74f}, {0.40f, 0.72f}};
        private static final int[][] LINKS = {{0, 1}, {1, 2}, {2, 3}, {3, 0}, {3, 4}, {4, 5}, {5, 6}, {6, 7}, {0, 8}, {8, 9}};

        CometDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.COMETE, 0);
            city = region(skyline(360, 70, 7, false), false);
        }

        @Override protected void restart() { meteors.clear(); }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_DEATH)) {                                    // elle se défait en étoiles filantes
                rumble(0.6f, 10f);
                sparks(bossX(), riseY(), 120, GOLD, 1100f, 26f);
                for (int i = 0; i < 36; i++) {
                    Particle star = particle(bossX(), riseY(), 200f + 140f * random.nextFloat(),
                        500f + 700f * random.nextFloat(), 1.6f + 0.8f * random.nextFloat(), 10f + 10f * random.nextFloat());
                    star.gravity = 400f;
                    star.color.set(random.nextBoolean() ? GOLD : HOT);
                    meteors.add(star);
                }
            }
            update(meteors, delta);
        }

        private float riseY() { return bossY() + worldHeight() * 0.35f * Interpolation.pow2In.apply(progress(RISE, CLIMAX_DEATH)); }

        @Override
        protected void back(Batch batch) {
            stars.draw(batch, 1f);
            drawConstellation(batch);
            batch.setColor(Color.WHITE);
            batch.draw(city, -MARGIN, groundY() - 10f, worldWidth() + 2f * MARGIN, worldHeight() * 0.2f);
            float k = progress(RISE, CLIMAX_DEATH);
            poseY = riseY() - bossY();
            poseScale = 1f - 0.5f * k;
            poseShadow = 0.4f * (1f - k) + (random.nextFloat() < 0.12f ? 0.2f : 0f);
            showBoss = time < CLIMAX_DEATH;
            glow(batch, bossX(), riseY(), bossWidth() * (1.5f + k), GOLD, 0.3f + 0.4f * k);
        }

        @Override
        protected void front(Batch batch) {
            additive(batch);
            for (Particle star : meteors) {                            // une traînée derrière chaque étoile filante
                float angle = MathUtils.atan2(star.vy, star.vx) * MathUtils.radiansToDegrees + 180f;
                batch.setColor(star.color.r, star.color.g, star.color.b, 0.7f * (1f - star.life()));
                batch.draw(trail, star.x, star.y - star.size / 2f, 0f, star.size / 2f, star.size * 12f, star.size, 1f, 1f, angle);
            }
            normal(batch);
            for (Particle star : meteors) glow(batch, star.x, star.y, star.size * 3f, HOT, 1f - star.life());
            wave(batch, CLIMAX_DEATH, 0.9f, bossX(), riseY(), GOLD, worldWidth() * 1.6f);
            flash(batch, HOT, CLIMAX_DEATH, 0.4f, 0.7f);
        }

        /** Ses étoiles s'allument une à une à sa place dans le ciel, puis les traits les relient. */
        private void drawConstellation(Batch batch) {
            float w = worldWidth(), h = worldHeight();
            float lines = progress(3.4f, 4.4f);
            for (int[] link : LINKS) {
                float[] a = CONSTELLATION[link[0]], b = CONSTELLATION[link[1]];
                line(batch, GOLD, 0.45f * lines, a[0] * w, a[1] * h, b[0] * w, b[1] * h, 3f);
            }
            for (int i = 0; i < CONSTELLATION.length; i++) {
                float on = progress(2.9f + i * 0.08f, 3.1f + i * 0.08f);
                if (on <= 0f) continue;
                float twinkle = 0.8f + 0.2f * MathUtils.sin(time * 6f + i);
                glow(batch, CONSTELLATION[i][0] * w, CONSTELLATION[i][1] * h, 46f * on, GOLD, on * twinkle);
                glow(batch, CONSTELLATION[i][0] * w, CONSTELLATION[i][1] * h, 14f * on, HOT, on);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Reine des Tables
    // -------------------------------------------------------------------------

    /** Mise royale : elle renverse sa table, plaque une carte Trèfle géante, chaque Trèfle se dédouble. */
    static final class QueenPhase extends BossScene {
        static final float FLIP = 0.9f;
        private final TextureRegion card, back;
        private static final Color FELT = c("1f6a3a"), WOOD = c("5a2a14");

        QueenPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.REINE, 2);
            card = gameCard("cards/light/1-C.png");                 // l'As de Trèfle du jeu
            back = gameCard("cards/light/BACK.png");
        }

        @Override
        protected void step(float delta) {
            if (at(FLIP)) {                                             // les cartes volent
                rumble(0.3f, 6f);
                spray(back, bossX(), groundY() + 60f, 26, 30f, 150f, 1300f, fit(98f, 27), 1600f, 1.6f);
            }
            if (at(CLIMAX_PHASE)) {
                rumble(0.7f, 14f);
                sparks(bossX(), cardY(), 90, GOLD, 1000f, 26f);
            }
        }

        private float cardY() {
            return MathUtils.lerp(worldHeight() + 200f, groundY() + 120f, Interpolation.pow3In.apply(progress(1.5f, CLIMAX_PHASE)));
        }

        @Override
        protected void back(Batch batch) {
            poseY = 12f * gesture(FLIP - 0.2f, FLIP + 0.3f);
            poseTilt = -6f * gesture(FLIP - 0.2f, FLIP + 0.4f);
            poseTintColor.set(GOLD);
            poseTint = 0.3f * progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.4f);
        }

        @Override
        protected void front(Batch batch) {
            // sa table : à plat, puis renversée d'un revers (elle bascule vers l'arrière)
            float flip = Interpolation.pow2Out.apply(progress(FLIP, FLIP + 0.35f));
            float tableW = worldWidth() * 0.7f, tableH = 90f * (1f - flip) + 18f;
            float tableY = groundY() + 30f + 140f * flip;
            float alpha = 1f - progress(FLIP + 0.4f, FLIP + 0.8f);
            rect(batch, WOOD, alpha, bossX() - tableW / 2f, tableY - 14f, tableW, 14f + tableH * 0.15f);
            rect(batch, FELT, alpha, bossX() - tableW / 2f, tableY, tableW, tableH);
            if (time < 1.5f) return;
            // la carte Trèfle géante, plaquée, puis ses Trèfles qui se dédoublent
            float scale = fit(worldHeight() * 0.34f, 27);
            float split = Interpolation.swingOut.apply(progress(2.5f, 3.1f));
            float y = time < CLIMAX_PHASE ? cardY() : groundY() + 120f + 60f;
            float spread = worldWidth() * 0.16f * split;
            draw(batch, card, bossX() - spread, y, scale, -4f * split, Color.WHITE, 1f);
            if (split > 0f) draw(batch, card, bossX() + spread, y, scale, 4f * split, Color.WHITE, split);
            wave(batch, CLIMAX_PHASE, 0.8f, bossX(), y, GOLD, worldWidth() * 1.4f);
            flash(batch, GOLD, CLIMAX_PHASE, 0.35f, 0.6f);
        }
    }

    /**
     * Sa vraie couronne lui tombe de la tête ; elle se fend, se brise en deux, puis part en poussière d'or.
     * Derrière elle, son château de cartes s'écroule.
     */
    static final class QueenDeath extends BossScene {
        static final float SLIP = 0.5f, LAND = 1.15f, DUST = 3.0f;
        /** Sa couronne dans son portrait : colonnes 9 à 25, lignes 0 à 9. */
        private static final int CROWN_X = 9, CROWN_W = 17, CROWN_H = 10;
        private final TextureRegion crown, left, right, body, scepter, back;
        private boolean collapsed, dusted;
        private final float[] halves = new float[8];                // x, y, rotation, vx des deux moitiés

        QueenDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.REINE, 0);
            crown   = new TextureRegion(boss, CROWN_X, 0, CROWN_W, CROWN_H);
            left    = new TextureRegion(boss, CROWN_X, 0, CROWN_W / 2, CROWN_H);
            right   = new TextureRegion(boss, CROWN_X + CROWN_W / 2, 0, CROWN_W - CROWN_W / 2, CROWN_H);
            body    = new TextureRegion(boss, 0, CROWN_H, boss.getRegionWidth(), boss.getRegionHeight() - CROWN_H);
            scepter = new TextureRegion(boss, CROWN_X + CROWN_W, 0, boss.getRegionWidth() - CROWN_X - CROWN_W, CROWN_H);
            back    = gameCard("cards/light/BACK.png");
        }

        @Override protected void restart() { collapsed = false; dusted = false; }

        private float crownScale() { return bossScale(); }
        private float groundX() { return bossX() + bossWidth() * 0.55f; }
        private float groundCrownY() { return groundY() + CROWN_H * crownScale() / 2f; }

        @Override
        protected void step(float delta) {
            if (at(LAND)) { rumble(0.25f, 6f); sparks(groundX(), groundY() + 10f, 30, GOLD, 500f, 14f); }
            if (at(CLIMAX_DEATH)) {                                     // elle se brise en deux
                rumble(0.5f, 10f);
                sparks(groundX(), groundCrownY(), 60, GOLD, 800f, 18f);
            }
            if (!dusted && time >= DUST) {                              // chaque moitié part en poussière d'or
                dusted = true;
                for (int side = 0; side < 2; side++) {
                    float x = halfX(side), y = halfY();
                    for (int i = 0; i < 70; i++) {
                        Particle grain = particle(x + (random.nextFloat() - 0.5f) * 80f, y + (random.nextFloat() - 0.5f) * 60f,
                            60f + 60f * random.nextFloat(), 40f + 140f * random.nextFloat(), 1.6f + random.nextFloat(), 8f + 10f * random.nextFloat());
                        grain.color.set(random.nextBoolean() ? GOLD : HOT);
                        grain.drag = 0.5f;
                        glows.add(grain);
                    }
                }
            }
            if (!collapsed && time >= CLIMAX_DEATH + 0.2f) {            // le château de cartes s'écroule
                collapsed = true;
                for (int row = 0; row < 5; row++) {
                    for (int i = 0; i <= row; i++) {
                        float[] p = castle(row, i);
                        Particle card = place(back, p[0], p[1], (random.nextFloat() - 0.5f) * 500f, 200f * random.nextFloat(),
                            fit(64f, 27), 2.2f);
                        card.gravity = 1500f;
                        card.spin = (random.nextFloat() - 0.5f) * 600f;
                    }
                }
            }
        }

        /** @return la place d'une carte du château (rangée {@code row} depuis le haut), derrière la Reine. */
        private float[] castle(int row, int i) {
            float x = worldWidth() * 0.22f + (i - row / 2f) * 60f, y = groundY() + 40f + (4 - row) * 70f;
            return new float[] {x, y};
        }

        private float split() { return Interpolation.pow2Out.apply(progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.5f)); }
        private float halfX(int side) { return groundX() + (side == 0 ? -1f : 1f) * (CROWN_W * crownScale() / 4f + 70f * split()); }
        private float halfY() { return groundCrownY() + 40f * MathUtils.sin(split() * MathUtils.PI); }

        @Override
        protected void back(Batch batch) {
            if (!collapsed) {
                for (int row = 0; row < 5; row++) {
                    for (int i = 0; i <= row; i++) {
                        float[] p = castle(row, i);
                        float wobble = time > 1.8f ? MathUtils.sin(time * 20f + row) * 3f * progress(1.8f, CLIMAX_DEATH) : 0f;
                        draw(batch, back, p[0] + wobble, p[1], fit(64f, 27), wobble, WHITE, 1f);
                    }
                }
            }
            poseShadow = 0.5f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 1.0f);
            poseAlpha = 1f - 0.6f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 1.5f);
            poseY = -30f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 1.5f);
            poseX = time < SLIP ? (random.nextFloat() - 0.5f) * 8f : 0f;
            // dès que sa couronne glisse, on la dessine à part : son portrait reste sans couronne
            showBoss = time < SLIP;
            if (!showBoss) {
                float scale = bossScale(), x0 = bossX() + poseX - bossWidth() / 2f, y0 = bossBottom() + poseY;
                float light = 1f - poseShadow;
                batch.setColor(light, light, light, poseAlpha);
                batch.draw(body, x0, y0, body.getRegionWidth() * scale, body.getRegionHeight() * scale);
                batch.draw(scepter, x0 + (CROWN_X + CROWN_W) * scale, y0 + body.getRegionHeight() * scale,
                    scepter.getRegionWidth() * scale, CROWN_H * scale);
            }
        }

        @Override
        protected void front(Batch batch) {
            float scale = crownScale();
            if (time < SLIP) return;
            if (time < CLIMAX_DEATH) {                                  // elle tombe de sa tête, roule un peu, puis se fend
                float k = progress(SLIP, LAND);
                float headX = portraitX(CROWN_X + CROWN_W / 2f), headY = portraitY(CROWN_H / 2f);
                float x = MathUtils.lerp(headX, groundX(), k);
                float y = MathUtils.lerp(headY, groundCrownY(), Interpolation.bounceOut.apply(k)) + 80f * MathUtils.sin(k * MathUtils.PI);
                float rot = -25f * Interpolation.pow2Out.apply(k) + (time > LAND ? 25f * progress(LAND, LAND + 0.3f) : 0f);
                draw(batch, crown, x, y, scale, rot, WHITE, 1f);
                float crack = progress(LAND + 0.3f, CLIMAX_DEATH);
                if (crack > 0f) {
                    crack(batch, groundX(), groundCrownY() + CROWN_H * scale / 2f, -90f, CROWN_H * scale * crack, 7L, c("1a0a10"), 1f, 5f);
                }
                return;
            }
            if (time >= DUST + 0.2f) return;                            // il n'en reste que de la poussière
            float fade = 1f - progress(DUST - 0.2f, DUST + 0.2f);
            float s = split();
            draw(batch, left, halfX(0), halfY(), scale, -30f * s, WHITE, fade);
            draw(batch, right, halfX(1), halfY(), scale, 30f * s, WHITE, fade);
            flash(batch, GOLD, CLIMAX_DEATH, 0.3f, 0.5f);
        }
    }

    // -------------------------------------------------------------------------
    // Éclat Originel
    // -------------------------------------------------------------------------

    /** Il se fend ; chaque éclat montre un nouveau symbole, ils tournent et se ressoudent dans une autre couleur. */
    static final class ShardPhase extends BossScene {
        static final float SPLIT = CLIMAX_PHASE, MERGE = 3.6f;
        private static final EnemySymbol[] REFLECTS = {
            EnemySymbol.MIRROR, EnemySymbol.HOURGLASS, EnemySymbol.THORNS, EnemySymbol.FANG, EnemySymbol.RAGE, EnemySymbol.PREDICTION};
        private static final Color NEW = c("ff5ad8"), OLD = c("7ae8ff");
        private final TextureRegion[] tiles = new TextureRegion[6];
        private final TextureRegion[] icons = new TextureRegion[6];

        ShardPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.ECLAT, 2);
            int w = boss.getRegionWidth() / 3, h = boss.getRegionHeight() / 2;
            for (int i = 0; i < 6; i++) {
                tiles[i] = new TextureRegion(boss, (i % 3) * w, (i / 3) * h, w, h);
                icons[i] = symbol(REFLECTS[i]);
            }
        }

        @Override
        protected void step(float delta) {
            if (at(SPLIT)) { rumble(0.6f, 12f); sparks(bossX(), bossY(), 100, OLD, 1100f, 24f); }
            if (at(MERGE + 0.3f)) { rumble(0.3f, 6f); sparks(bossX(), bossY(), 80, NEW, 900f, 22f); }
        }

        @Override
        protected void back(Batch batch) {
            showBoss = time < SPLIT || time >= MERGE + 0.3f;
            poseTintColor.set(NEW);
            poseTint = 0.4f * progress(MERGE + 0.3f, MERGE + 0.7f);
            glow(batch, bossX(), bossY(), bossWidth() * 2.4f, time < MERGE ? OLD : NEW, 0.35f);
        }

        @Override
        protected void front(Batch batch) {
            if (time < SPLIT) {                                         // les fissures
                float k = progress(0.4f, SPLIT);
                for (int i = 0; i < 7; i++) {
                    float angle = i * 51.4f + 20f, length = bossWidth() * 0.55f * Math.min(1f, k * 7f - i);
                    if (length <= 0f) continue;
                    line(batch, WHITE, 0.9f, bossX(), bossY(), bossX() + MathUtils.cosDeg(angle) * length,
                        bossY() + MathUtils.sinDeg(angle) * length, 5f);
                }
                return;
            }
            if (time >= MERGE + 0.3f) { flash(batch, NEW, MERGE + 0.3f, 0.3f, 0.5f); return; }
            // les éclats s'écartent, tournent en kaléidoscope, puis reviennent
            float out = Interpolation.pow2Out.apply(progress(SPLIT, SPLIT + 0.4f)) * (1f - Interpolation.pow2In.apply(progress(MERGE, MERGE + 0.3f)));
            float turn = (time - SPLIT) * 140f;
            float scale = bossScale();
            for (int i = 0; i < 6; i++) {
                float angle = i * 60f + turn, r = worldHeight() * 0.28f * out;
                float x = bossX() + MathUtils.cosDeg(angle) * r, y = bossY() + MathUtils.sinDeg(angle) * r;
                Color tint = tmp.set(OLD).lerp(NEW, progress(SPLIT, MERGE));
                draw(batch, tiles[i], x, y, scale, angle - 90f, WHITE, 1f);
                additive(batch);
                draw(batch, tiles[i], x, y, scale, angle - 90f, tint, 0.35f);
                normal(batch);
                draw(batch, icons[i], x, y, iconScale(0.08f) * out, 0f, WHITE, out);
            }
            flash(batch, OLD, SPLIT, 0.3f, 0.6f);
        }
    }

    // -------------------------------------------------------------------------
    // Le Prétendant
    // -------------------------------------------------------------------------

    /** Les trois éclats sur la couronne du Prétendant, comme à son entrée : la Comète, la Reine, l'Éclat Originel. */
    static final Color[] SHARDS = {Color.valueOf("ffc93a"), Color.valueOf("e0283a"), Color.valueOf("fff6c8")};
    /** Leur place sur son portrait (pixels, depuis le haut à gauche), et celle de ses yeux. */
    static final float[] GEM_X = {10f, 17f, 24f};
    static final float GEM_Y = 3.5f;
    static final float[][] PRETENDER_EYES = {{14f, 17.5f}, {21f, 17.5f}};
    static final String[] GEM = {"..o..", ".o#o.", "o###o", ".o#o.", "..o.."};

    /** Socle des trois scènes du Prétendant : ses éclats sertis, qui se fendent puis se brisent. */
    abstract static class PretenderScene extends BossScene {
        protected final TextureRegion gem, chip;

        PretenderScene(CutsceneKit kit, Moment moment, int phase) {
            super(kit, moment, EnemyKind.PRETENDANT, phase);
            gem  = art("o#", new Color[] {c("1a0a0e"), Color.WHITE}, GEM);
            chip = load("hud/chip_enemy.png");
        }

        /** Dessine l'éclat {@code i} sur sa couronne ; {@code crack} de 0 à 1 le fend. */
        protected void drawGem(Batch batch, int i, float crack, float alpha) {
            float x = portraitX(GEM_X[i]), y = portraitY(GEM_Y), scale = bossScale() * 1.1f * poseScale;
            float shake = crack > 0f ? (random.nextFloat() - 0.5f) * 6f * crack : 0f;
            glow(batch, x, y, scale * 14f, SHARDS[i], 0.7f * alpha);
            draw(batch, gem, x + shake, y, scale, 0f, SHARDS[i], alpha);
            if (crack > 0f) crack(batch, x + shake, y + scale * 2.5f, -80f, scale * 5f * crack, 3L + i, c("1a0a0e"), alpha, 3f);
        }

        /** L'éclat {@code i} se brise : ses morceaux et ses étincelles. */
        protected void shatter(int i) {
            float x = portraitX(GEM_X[i]), y = portraitY(GEM_Y);
            sparks(x, y, 50, SHARDS[i], 900f, 18f);
            for (int k = 0; k < 10; k++) {
                Particle piece = place(new TextureRegion(gem, 1 + random.nextInt(3), 1 + random.nextInt(3), 1, 1), x, y,
                    (random.nextFloat() - 0.5f) * 800f, 200f + 500f * random.nextFloat(), bossScale() * 0.8f, 1.4f);
                piece.color.set(SHARDS[i]);
                piece.gravity = 1600f;
                piece.spin = (random.nextFloat() - 0.5f) * 700f;
            }
        }

        /** Des jetons de casino qui volent dans tous les sens depuis ({@code x}, {@code y}). */
        protected void chips(float x, float y, int count, float speed) {
            for (int k = 0; k < count; k++) {
                Particle token = place(chip, x, y, 0f, 0f, fit(50f, chip.getRegionHeight()) * (0.7f + 0.6f * random.nextFloat()), 2.2f);
                float angle = 360f * random.nextFloat(), v = speed * (0.4f + 0.6f * random.nextFloat());
                token.vx = MathUtils.cosDeg(angle) * v;
                token.vy = MathUtils.sinDeg(angle) * v + 300f;
                token.gravity = 900f + 900f * random.nextFloat();
                token.spin = (random.nextFloat() - 0.5f) * 1400f;
            }
        }
    }

    /** Phase 2 : les éclats de gauche et de droite de sa couronne se fendent et se brisent ; ses jetons volent partout. */
    static final class PretenderShard extends PretenderScene {
        static final float CRACK = 1.0f;

        PretenderShard(CutsceneKit kit) { super(kit, Moment.PHASE, 2); }

        @Override
        protected void step(float delta) {
            if (at(CRACK)) rumble(0.2f, 5f);
            if (at(CLIMAX_PHASE)) {
                rumble(0.8f, 14f);
                shatter(0);
                shatter(2);
                chips(bossX(), bossY(), 60, 1600f);
            }
            if (time > CLIMAX_PHASE && time < 3.6f && random.nextFloat() < 0.5f) {   // le chaos : encore des jetons
                chips(worldWidth() * random.nextFloat(), worldHeight() * (0.2f + 0.6f * random.nextFloat()), 2, 900f);
            }
        }

        @Override
        protected void back(Batch batch) {
            poseX = time < CLIMAX_PHASE ? (random.nextFloat() - 0.5f) * 6f * progress(CRACK, CLIMAX_PHASE) : 0f;
            poseY = 10f * gesture(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.4f);
            glow(batch, bossX(), bossY(), bossWidth() * 2.2f, GOLD, 0.25f);
        }

        @Override
        protected void front(Batch batch) {
            float crack = progress(CRACK, CLIMAX_PHASE);
            for (int i = 0; i < 3; i++) {
                if (i != 1 && time >= CLIMAX_PHASE) continue;          // les éclats des côtés ont volé en morceaux
                drawGem(batch, i, i == 1 ? 0f : crack, 1f);
            }
            wave(batch, CLIMAX_PHASE, 0.8f, bossX(), portraitY(GEM_Y), GOLD, worldWidth() * 1.3f);
            flash(batch, GOLD, CLIMAX_PHASE, 0.3f, 0.45f);
        }
    }

    /** Phase 3 : le fantôme de la Reine paraît, le dernier éclat (au milieu) se brise ; une aura d'or l'entoure, ses yeux s'allument. */
    static final class PretenderQueen extends PretenderScene {
        private final TextureRegion queen;

        PretenderQueen(CutsceneKit kit) {
            super(kit, Moment.PHASE, 3);
            queen = kit.portrait(EnemyKind.REINE);
        }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_PHASE)) { rumble(0.7f, 12f); shatter(1); }
            if (time > CLIMAX_PHASE && random.nextFloat() < 0.8f) {   // son aura : des étincelles d'or qui montent
                Particle mote = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth() * 1.2f, bossBottom() + bossHeight() * random.nextFloat(),
                    90f, 120f + 160f * random.nextFloat(), 1.0f, 10f + 10f * random.nextFloat());
                mote.color.set(GOLD);
                glows.add(mote);
            }
        }

        private float ghostX() { return bossX() + bossWidth() * 0.6f; }

        @Override
        protected void back(Batch batch) {
            float ghost = progress(0.3f, 1.3f) * (1f - progress(3.6f, 4.2f));
            float scale = bossScale() * 1.15f;
            float w = queen.getRegionWidth() * scale, h = queen.getRegionHeight() * scale;
            float bob = MathUtils.sin(time * 2f) * 10f;
            additive(batch);
            batch.setColor(0.75f, 0.85f, 1f, 0.45f * ghost);
            batch.draw(queen, ghostX() - w / 2f, bossBottom() + 40f + bob, w, h);
            normal(batch);
            float aura = progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.5f);   // son aura d'or
            float pulse = 0.8f + 0.2f * MathUtils.sin(time * 7f);
            glow(batch, bossX(), bossY(), bossWidth() * 2.8f * pulse, GOLD, 0.55f * aura);
            poseTintColor.set(GOLD);
            poseTint = 0.3f * aura * pulse;
        }

        @Override
        protected void front(Batch batch) {
            if (time < CLIMAX_PHASE) drawGem(batch, 1, progress(1.2f, CLIMAX_PHASE), 1f);
            eyes(batch, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.3f), GOLD, PRETENDER_EYES);
            flash(batch, c("ff3a5a"), CLIMAX_PHASE, 0.3f, 0.4f);
        }
    }

    /** Il tombe et lâche tous ses jetons ; sa couronne s'envole ; son âme monte au ciel avec le fantôme de la Reine, puis ils disparaissent. */
    static final class PretenderDeath extends PretenderScene {
        static final float SOUL = 3.1f;
        /** Sa couronne dans son portrait : colonnes 9 à 25, lignes 0 à 9. */
        private static final int CROWN_X = 9, CROWN_W = 17, CROWN_H = 10;
        private final TextureRegion crown, body, sideLeft, sideRight, queen;
        private int fallen;
        private static final int CHIPS = 28;

        PretenderDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, 0);
            crown     = new TextureRegion(boss, CROWN_X, 0, CROWN_W, CROWN_H);
            body      = new TextureRegion(boss, 0, CROWN_H, boss.getRegionWidth(), boss.getRegionHeight() - CROWN_H);
            sideLeft  = new TextureRegion(boss, 0, 0, CROWN_X, CROWN_H);
            sideRight = new TextureRegion(boss, CROWN_X + CROWN_W, 0, boss.getRegionWidth() - CROWN_X - CROWN_W, CROWN_H);
            queen     = kit.portrait(EnemyKind.REINE);
        }

        @Override protected void restart() { fallen = 0; }

        /** @return la place du jeton {@code i} du trône (un dossier haut et deux accoudoirs). */
        private float[] chipAt(int i) {
            float size = 56f, x0 = bossX(), base = groundY() + 10f;
            if (i < 12) return new float[] {x0 - 2.5f * size + (i % 6) * size, base + (i / 6) * size * 0.6f};
            if (i < 22) return new float[] {x0 - 2.5f * size + (i % 2 == 0 ? 0 : 5) * size, base + (2 + (i - 12) / 2) * size * 0.6f};
            return new float[] {x0 - 1.5f * size + (i - 22) % 4 * size, base + (7 + (i - 22) / 4) * size * 0.6f};
        }

        @Override
        protected void step(float delta) {
            if (time >= CLIMAX_DEATH) {                                 // le trône s'effondre jeton par jeton
                int target = Math.min(CHIPS, (int) ((time - CLIMAX_DEATH) * 30f) + 1);
                while (fallen < target) {
                    float[] p = chipAt(CHIPS - 1 - fallen);
                    Particle piece = place(chip, p[0], p[1], (random.nextFloat() - 0.5f) * 600f, 300f * random.nextFloat(),
                        fit(56f, chip.getRegionHeight()), 1.8f);
                    piece.gravity = 1800f;
                    piece.spin = (random.nextFloat() - 0.5f) * 720f;
                    fallen++;
                }
            }
            if (at(CLIMAX_DEATH)) { rumble(0.8f, 12f); chips(bossX(), bossY(), 40, 1100f); }   // il lâche tous ses jetons
        }

        @Override
        protected void back(Batch batch) {
            for (int i = 0; i < CHIPS - fallen; i++) {
                float[] p = chipAt(i);
                float wobble = time > 1.2f ? (random.nextFloat() - 0.5f) * 6f * progress(1.2f, CLIMAX_DEATH) : 0f;
                draw(batch, chip, p[0] + wobble, p[1], fit(56f, chip.getRegionHeight()), 0f, WHITE, 1f);
            }
            float drop = Interpolation.bounceOut.apply(progress(CLIMAX_DEATH + 0.3f, CLIMAX_DEATH + 0.9f));
            poseY = 140f - 160f * drop;                                 // assis sur le trône, puis au sol
            poseTilt = 0f;
            poseShadow = 0.4f * drop + 0.3f * progress(SOUL, SOUL + 0.6f);
            // dès que sa couronne s'envole, son portrait reste sans elle
            showBoss = time < CLIMAX_DEATH;
            if (!showBoss) {
                float scale = bossScale(), x0 = bossX() - bossWidth() / 2f, y0 = bossBottom() + poseY;
                float light = 1f - poseShadow;
                batch.setColor(light, light, light, 1f);
                batch.draw(body, x0, y0, body.getRegionWidth() * scale, body.getRegionHeight() * scale);
                float top = y0 + body.getRegionHeight() * scale;
                batch.draw(sideLeft, x0, top, CROWN_X * scale, CROWN_H * scale);
                batch.draw(sideRight, x0 + (CROWN_X + CROWN_W) * scale, top, sideRight.getRegionWidth() * scale, CROWN_H * scale);
            }
        }

        @Override
        protected void front(Batch batch) {
            if (time >= CLIMAX_DEATH) {                                 // sa couronne s'envole
                float k = progress(CLIMAX_DEATH, CLIMAX_DEATH + 1.6f);
                float x = portraitX(CROWN_X + CROWN_W / 2f) + worldWidth() * 0.45f * Interpolation.pow2In.apply(k);
                float y = MathUtils.lerp(portraitY(CROWN_H / 2f), worldHeight() + 200f, Interpolation.pow2In.apply(k))
                    + 40f * MathUtils.sin(k * 10f);
                draw(batch, crown, x, y, bossScale(), -400f * k, WHITE, 1f);
            }
            // son âme monte au ciel avec le fantôme de la Reine, puis tous deux s'effacent
            float soul = progress(SOUL, SOUL + 0.4f) * (1f - progress(4.3f, 4.9f));
            if (soul > 0f) {
                float rise = Interpolation.pow2In.apply(progress(SOUL, 4.9f)) * worldHeight() * 0.5f;
                float scale = bossScale(), w = boss.getRegionWidth() * scale, h = boss.getRegionHeight() * scale;
                additive(batch);
                batch.setColor(0.9f, 0.9f, 1f, 0.5f * soul);
                batch.draw(body, bossX() - w / 2f - 130f, bossBottom() + poseY + rise, w, body.getRegionHeight() * scale);
                float qw = queen.getRegionWidth() * scale, qh = queen.getRegionHeight() * scale;
                batch.setColor(0.75f, 0.85f, 1f, 0.45f * soul);
                batch.draw(queen, bossX() + 150f - qw / 2f, bossBottom() + 40f + rise * 1.05f, qw, qh);
                normal(batch);
                glow(batch, bossX(), bossBottom() + h * 0.6f + rise, w * 2.2f, WHITE, 0.25f * soul);
            }
        }
    }

    // -------------------------------------------------------------------------
    // La Maison
    // -------------------------------------------------------------------------

    /**
     * Socle des scènes de la Maison, dans le décor de son entrée : ciel du soir, halo doré, manoir posé sur les
     * nuages. {@link #evil} fait virer le décor au diabolique (ciel rouge, braises), {@link #holy} à l'angélique.
     */
    abstract static class HouseScene extends BossScene {
        static final Color SKY_TOP = c("1a1a4a"), SKY_MID = c("7a3a7a"), SKY_LOW = c("ffb46a"), HALO = c("e89a6a");
        static final Color HELL_TOP = c("1a0204"), HELL_LOW = c("c81a0a"), ROOM = c("3a1420"), FLOOR = c("6a2a1a");
        /** Le corps central de son portrait (colonnes 8 à 26, lignes 13 à 37), ses yeux-fenêtres. */
        static final float BODY_LEFT = 8f, BODY_RIGHT = 27f, BODY_TOP = 13f, BODY_BOTTOM = 38f;
        static final float[][] WINDOW_EYES = {{13f, 22f}, {23f, 22f}};
        protected final TextureRegion machine, wing, horn;
        protected float evil, holy;

        HouseScene(CutsceneKit kit, Moment moment, int phase) {
            super(kit, moment, EnemyKind.MAISON, phase);
            machine = art("ogdwr", new Color[] {c("0a0808"), c("ffc93a"), c("b8801f"), c("f4ecdc"), c("e0283a")},
                "..oooooooooo..", ".oggggggggggo.", "oggggggggggggo", "ogoooooooooogo", "ogowwowwowwogo", "ogowrowrowrogo",
                "ogowwowwowwogo", "ogoooooooooogo", "oggggggggggggo", "ogggddddddgggo", "oggggggggggggo", "oddddddddddddo",
                "oooooooooooooo");
            wing = art("wg", new Color[] {c("ffffff"), c("c8d0ec")},
                "......ww", "....wwww", "..wwwwwg", ".wwwwwg.", "wwwwgg..", "wwgg....", "wg......");
            horn = art("rd", new Color[] {c("8a0a0a"), c("3a0404")},
                "....d", "...rd", "..rrd", ".rrd.", "rrrd.", "rrd..");
        }

        /** Le décor : le ciel de son entrée (qui vire au rouge avec {@link #evil}), le halo et les nuages. */
        protected void decor(Batch batch) {
            int bands = 30;
            float height = worldHeight() + 2f * MARGIN, band = height / bands;
            for (int i = 0; i < bands; i++) {
                float k = i / (float) (bands - 1);
                if (k < 0.5f) tmp.set(SKY_LOW).lerp(SKY_MID, k * 2f);
                else tmp.set(SKY_MID).lerp(SKY_TOP, (k - 0.5f) * 2f);
                Color hell = new Color(HELL_LOW).lerp(HELL_TOP, k);
                tmp.lerp(hell, evil);
                if (holy > 0f) tmp.lerp(c("fff4d8"), holy * (0.55f - 0.3f * k));
                batch.setColor(tmp.r, tmp.g, tmp.b, 1f);
                batch.draw(pixel, -MARGIN, -MARGIN + i * band - 1f, worldWidth() + 2f * MARGIN, band + 2f);
            }
            for (int i = 0; i < 40; i++) {                              // les étoiles du soir
                float x = (i * 173.3f) % worldWidth(), y = worldHeight() * (0.72f + 0.26f * ((i * 61) % 100) / 100f);
                rect(batch, c("e8e0ff"), 0.8f * (1f - evil) * (1f - holy), x, y, 4f, 4f);
            }
            glow(batch, bossX(), bossY(), worldWidth() * 0.9f, tmp.set(HALO).lerp(HELL_LOW, evil).lerp(GOLD, holy), 0.45f);
            if (holy > 0f) {                                            // des rayons de lumière tombent du ciel
                additive(batch);
                for (int i = 0; i < 7; i++) {
                    float x = worldWidth() * (0.1f + i * 0.13f) + 30f * MathUtils.sin(time + i);
                    batch.setColor(1f, 0.95f, 0.8f, 0.12f * holy);
                    batch.draw(soft, x - 60f, -MARGIN, 0f, 0f, 120f, worldHeight() * 1.3f, 1f, 1f, -12f);
                }
                normal(batch);
            }
            clouds(batch);
        }

        /** Les nuages sur lesquels repose le manoir, comme à son entrée. */
        protected void clouds(Batch batch) {
            Color pale = tmp.set(c("f6e8f0")).lerp(c("5a1a1a"), evil);
            for (int i = 0; i < 16; i++) {
                float x = worldWidth() * i / 15f, y = groundY() + 20f + 18f * MathUtils.sin(i * 1.7f), r = 150f + 40f * ((i * 7) % 3);
                batch.setColor(pale.r, pale.g, pale.b, 1f);
                batch.draw(soft, x - r, y - r * 0.6f, r * 2f, r * 1.2f);
            }
            rect(batch, pale, 1f, -MARGIN, -MARGIN, worldWidth() + 2f * MARGIN, groundY() + MARGIN);
        }

        /** L'intérieur du manoir : une salle de jeu où volent des machines à sous aux ailes d'ange. */
        protected void interior(Batch batch, float alpha) {
            if (alpha <= 0f) return;
            float x0 = portraitX(BODY_LEFT), x1 = portraitX(BODY_RIGHT), y0 = portraitY(BODY_BOTTOM), y1 = portraitY(BODY_TOP);
            rect(batch, ROOM, alpha, x0, y0, x1 - x0, y1 - y0);
            rect(batch, FLOOR, alpha, x0, y0, x1 - x0, (y1 - y0) * 0.18f);
            glow(batch, (x0 + x1) / 2f, y1 - 20f, (x1 - x0) * 0.9f, GOLD, 0.35f * alpha);
            float scale = (x1 - x0) / 50f;
            for (int i = 0; i < 3; i++) {
                float mx = MathUtils.lerp(x0, x1, 0.2f + i * 0.3f), my = MathUtils.lerp(y0, y1, i == 1 ? 0.6f : 0.4f)
                    + 10f * MathUtils.sin(time * 3f + i * 2f);
                float flap = 1f + 0.25f * MathUtils.sin(time * 14f + i);
                float ww = wing.getRegionWidth() * scale, wh = wing.getRegionHeight() * scale * flap;
                batch.setColor(1f, 1f, 1f, alpha);
                batch.draw(wing, mx - 7f * scale - ww, my - wh * 0.2f, ww, wh);                 // l'aile gauche
                batch.draw(wing, mx + 7f * scale + ww, my - wh * 0.2f, -ww, wh);                // l'aile droite, en miroir
                draw(batch, machine, mx, my, scale, 0f, WHITE, alpha);
            }
        }

        /** La Maison démoniaque : son portrait rougi, des cornes sur le toit, des yeux de braise. */
        protected void demon(Batch batch, float alpha) {
            if (alpha <= 0f) return;
            float scale = bossScale(), x = portraitX(17f), y = portraitY(1f);
            for (int side = -1; side <= 1; side += 2) {
                float hw = horn.getRegionWidth() * scale * 1.6f, hh = horn.getRegionHeight() * scale * 1.6f;
                batch.setColor(1f, 1f, 1f, alpha);
                batch.draw(horn, x + side * 40f - (side < 0 ? hw : 0f) + (side < 0 ? hw : 0f), y - 10f, side * hw, hh);
            }
            eyes(batch, alpha, c("ff2a0a"), WINDOW_EYES);
        }

        /** Des braises qui montent : le décor diabolique. */
        protected void embers(float rate) {
            if (random.nextFloat() >= rate) return;
            Particle ember = particle(worldWidth() * random.nextFloat(), groundY(), 85f + 10f * random.nextFloat(),
                120f + 200f * random.nextFloat(), 2.2f, 8f + 10f * random.nextFloat());
            ember.color.set(random.nextBoolean() ? c("ff5a1a") : c("ffb040"));
            glows.add(ember);
        }

        /** Des plumes blanches qui tombent : le décor angélique. */
        protected void feathers(float rate) {
            if (random.nextFloat() >= rate) return;
            Particle plume = place(wing, worldWidth() * random.nextFloat(), worldHeight() + 40f, 0f, -90f, 3f, 4f);
            plume.vx = (random.nextFloat() - 0.5f) * 80f;
            plume.spin = (random.nextFloat() - 0.5f) * 120f;
        }
    }

    /** Phase 2 : sa façade se fend et tombe ; derrière, la même silhouette montre l'intérieur, des machines aux ailes d'ange. */
    static final class HouseFacade extends HouseScene {
        HouseFacade(CutsceneKit kit) { super(kit, Moment.PHASE, 2); }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_PHASE + 0.35f)) {                             // la façade touche les nuages
                rumble(0.8f, 16f);
                for (int i = 0; i < 40; i++) {
                    Particle dust = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth() * 1.6f, groundY() + 20f,
                        60f + 60f * random.nextFloat(), 200f + 300f * random.nextFloat(), 1.6f, 120f + 120f * random.nextFloat());
                    dust.color.set(0.9f, 0.8f, 0.7f, 0.5f);
                    dust.drag = 0.3f;
                    glows.add(dust);
                }
            }
        }

        @Override
        protected void back(Batch batch) {
            decor(batch);
            poseX = time < CLIMAX_PHASE ? (random.nextFloat() - 0.5f) * 8f * progress(0.6f, CLIMAX_PHASE) : 0f;
        }

        @Override
        protected void front(Batch batch) {
            interior(batch, time < CLIMAX_PHASE ? 0f : 1f);
            if (time < CLIMAX_PHASE) {                                  // les fissures de la façade
                float k = progress(0.8f, CLIMAX_PHASE);
                for (int i = 0; i < 4; i++) {
                    crack(batch, portraitX(10f + i * 5f), portraitY(BODY_TOP), -90f + (i - 1.5f) * 12f,
                        (portraitY(BODY_TOP) - portraitY(BODY_BOTTOM)) * 0.8f * k, 20L + i, c("1a0808"), 1f, 5f);
                }
            } else {                                                    // la façade bascule vers nous et tombe
                float fall = Interpolation.pow2In.apply(progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.35f));
                if (fall < 1f) {
                    TextureRegion facade = new TextureRegion(boss, (int) BODY_LEFT, (int) BODY_TOP, (int) (BODY_RIGHT - BODY_LEFT),
                        (int) (BODY_BOTTOM - BODY_TOP));
                    float scale = bossScale(), w = facade.getRegionWidth() * scale, h = facade.getRegionHeight() * scale * (1f - fall);
                    batch.setColor(1f, 1f, 1f, 1f);
                    batch.draw(facade, portraitX(BODY_LEFT), portraitY(BODY_BOTTOM) - 60f * fall, w, h);
                }
            }
            flash(batch, GOLD, CLIMAX_PHASE, 0.3f, 0.4f);
        }
    }

    /** Phase 3 : la Maison est détruite ; une nouvelle Maison, démoniaque, sort des nuages ; le ciel devient rouge. */
    static final class HouseVault extends HouseScene {
        static final float BREAK = 1.2f;
        private boolean broken;

        HouseVault(CutsceneKit kit) { super(kit, Moment.PHASE, 3); }

        @Override protected void restart() { broken = false; }

        @Override
        protected void step(float delta) {
            if (!broken && time >= BREAK) {                             // la Maison est détruite
                broken = true;
                rumble(0.7f, 14f);
                MinesBossScenes.crumble(this, 3, WHITE, 500f);
            }
            if (at(CLIMAX_PHASE)) { rumble(0.8f, 16f); sparks(bossX(), groundY() + 40f, 90, c("ff3a0a"), 1100f, 24f); }
            embers(evil * 0.8f);
        }

        @Override
        protected void back(Batch batch) {
            evil = progress(BREAK, CLIMAX_PHASE);
            decor(batch);
            // la nouvelle Maison sort des nuages, rouge et noire
            float rise = Interpolation.pow2Out.apply(progress(CLIMAX_PHASE - 0.4f, CLIMAX_PHASE + 0.3f));
            showBoss = time < BREAK || rise > 0f;
            if (time >= BREAK) {
                poseY = -bossHeight() * (1f - rise);
                poseTintColor.set(c("ff1a0a"));
                poseTint = 0.35f;
                poseShadow = 0.6f;
            }
        }

        @Override
        protected void front(Batch batch) {
            if (time < BREAK) interior(batch, 1f);
            if (time >= BREAK) demon(batch, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.3f));
            clouds(batch);                                              // elle sort de derrière les nuages
            flash(batch, c("ff3a0a"), CLIMAX_PHASE, 0.3f, 0.5f);
        }
    }

    /** La Maison démoniaque est détruite ; le décor redevient angélique : lumière, plumes, ciel du soir. */
    static final class HouseDeath extends HouseScene {
        private boolean broken;

        HouseDeath(CutsceneKit kit) { super(kit, Moment.DEATH, 0); }

        @Override protected void restart() { broken = false; }

        @Override
        protected void step(float delta) {
            if (!broken && time >= CLIMAX_DEATH) {
                broken = true;
                rumble(0.9f, 16f);
                MinesBossScenes.crumble(this, 3, c("ff6a4a"), 700f);
                sparks(bossX(), bossY(), 120, c("ff5a1a"), 1200f, 26f);
            }
            embers(evil * 0.8f);
            feathers(holy * 0.35f);
        }

        @Override
        protected void back(Batch batch) {
            evil = 1f - progress(CLIMAX_DEATH + 0.2f, CLIMAX_DEATH + 1.4f);
            holy = progress(CLIMAX_DEATH + 0.4f, CLIMAX_DEATH + 1.6f);
            decor(batch);
            showBoss = time < CLIMAX_DEATH;
            poseTintColor.set(c("ff1a0a"));
            poseTint = 0.35f;
            poseShadow = 0.6f;
            poseX = (random.nextFloat() - 0.5f) * 10f * progress(1.0f, CLIMAX_DEATH);
        }

        @Override
        protected void front(Batch batch) {
            if (time < CLIMAX_DEATH) demon(batch, 1f);
            wave(batch, CLIMAX_DEATH, 1.0f, bossX(), bossY(), WHITE, worldWidth() * 1.6f);
            flash(batch, WHITE, CLIMAX_DEATH, 0.4f, 0.7f);
        }
    }
}
