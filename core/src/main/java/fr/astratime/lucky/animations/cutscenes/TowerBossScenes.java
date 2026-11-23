package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.assets.StoryPortraits;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les scènes de phase et de mort des boss de l'Histoire (choix d'Astra, 2026-10-24 ; refaites le 2026-11-22 pour
 * montrer ce que dit chaque phrase, devant le décor de leur chapitre). La phase de la Comète, toute la Machine
 * Originelle et le Croupier ont leurs propres cinématiques.
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
        private static final Color FELT = hex("1f6a3a"), WOOD = hex("5a2a14");

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
     * Sa vraie couronne lui glisse de la tête ; elle tombe, se fend, se brise en deux puis part en poussière d'or.
     * Derrière elle, son château de cartes s'écroule. Sans couronne, la Reine n'est plus qu'une carte : elle se
     * retourne sur le dos d'une carte et tombe à plat.
     */
    static final class QueenDeath extends BossScene {
        static final float SLIP = 0.5f, LAND = 1.15f, DUST = 3.0f, FLIP = 3.5f, TURNED = 3.9f, TOPPLE = 4.3f, DOWN = 4.75f;
        private static final Color VIOLET = hex("c8a0ff");
        private final Piece crown;
        private final TextureRegion bare, left, right, back, self;
        private boolean off, collapsed, dusted;
        private float crownX, crownY;

        QueenDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.REINE, 0);
            int[][] rest = StoryPortraits.without(EnemyKind.REINE, StoryPortraits.Prop.QUEEN_CROWN);
            crown = lift(StoryPortraits.pixels(EnemyKind.REINE), rest);
            bare  = pixels(rest);
            left  = new TextureRegion(crown.region, 0, 0, crown.width / 2, crown.height);
            right = new TextureRegion(crown.region, crown.width / 2, 0, crown.width - crown.width / 2, crown.height);
            back  = gameCard("cards/light/BACK.png");                   // son château de cartes
            self  = back;                                               // elle, devenue une carte de son château
        }

        @Override protected void restart() { off = false; collapsed = false; dusted = false; }

        private float groundX() { return bossX() + bossWidth() * 0.62f; }
        private float groundCrownY() { return groundY() + crown.height * bossScale() / 2f + 30f; }
        private float cardScale() { return bossHeight() / self.getRegionHeight(); }

        @Override
        protected void step(float delta) {
            if (at(LAND)) { rumble(0.25f, 6f); sparks(groundX(), groundCrownY() - 20f, 30, GOLD, 500f, 14f); }
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
            if (at(FLIP)) sparks(bossX(), bossY(), 30, VIOLET, 400f, 12f);
            if (at(DOWN)) {                                             // la carte tombe à plat
                rumble(0.3f, 8f);
                for (int i = 0; i < 24; i++) {
                    Particle dust = particle(bossX() + random.nextFloat() * bossHeight(), bossBottom() + 20f, 60f + 60f * random.nextFloat(),
                        120f + 200f * random.nextFloat(), 1.2f, 60f + 60f * random.nextFloat());
                    dust.color.set(0.8f, 0.6f, 0.7f, 0.35f);
                    dust.drag = 0.3f;
                    glows.add(dust);
                }
            }
        }

        /** @return la place d'une carte du château (rangée {@code row} depuis le haut), derrière la Reine. */
        private float[] castle(int row, int i) {
            float x = worldWidth() * 0.22f + (i - row / 2f) * 60f, y = groundY() + 40f + (4 - row) * 70f;
            return new float[] {x, y};
        }

        private float split() { return Interpolation.pow2Out.apply(progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.5f)); }
        private float halfX(int side) { return groundX() + (side == 0 ? -1f : 1f) * (crown.width * bossScale() / 4f + 70f * split()); }
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
            if (!off && time >= SLIP) {                                 // sa vraie couronne glisse : elle n'en a plus
                off = true;
                crownX = pieceX(crown);
                crownY = pieceY(crown);
                wear(bare);
            }
            poseShadow = 0.45f * progress(CLIMAX_DEATH, FLIP);
            poseX = time < SLIP ? (random.nextFloat() - 0.5f) * 8f : 0f;
            poseTilt = -4f * gesture(SLIP, LAND + 0.4f);
            showBoss = time < FLIP;
            if (time >= FLIP) drawTurning(batch);
        }

        /** Elle se retourne comme une carte : son portrait se referme, le dos d'une carte s'ouvre, puis elle tombe à plat. */
        private void drawTurning(Batch batch) {
            float k = progress(FLIP, TURNED);
            if (k < 0.5f) {
                float w = bossWidth() * (1f - 2f * k), h = bossHeight();
                batch.setColor(0.55f, 0.55f, 0.55f, 1f);
                batch.draw(boss, bossX() - w / 2f, bossBottom(), w, h);
                return;
            }
            float s = cardScale(), full = self.getRegionWidth() * s, w = full * (2f * k - 1f), h = self.getRegionHeight() * s;
            float fall = time < TOPPLE ? 0f : Interpolation.bounceOut.apply(progress(TOPPLE, DOWN + 0.25f));
            float x = bossX() - w / 2f, light = 0.75f + 0.25f * progress(TURNED, TURNED + 0.3f);
            glow(batch, bossX(), bossY(), bossWidth() * 1.6f, VIOLET, 0.3f * (1f - progress(TURNED, DOWN)));
            batch.setColor(light, light, light, 1f);
            batch.draw(self, x, bossBottom(), w, 0f, w, h, 1f, 1f, -90f * fall);
        }

        @Override
        protected void front(Batch batch) {
            float scale = bossScale();
            if (!off) return;
            if (time < CLIMAX_DEATH) {                                  // elle tombe de sa tête, roule un peu, puis se fend
                float k = progress(SLIP, LAND);
                float x = MathUtils.lerp(crownX, groundX(), k);
                float y = MathUtils.lerp(crownY, groundCrownY(), Interpolation.bounceOut.apply(k)) + 80f * MathUtils.sin(k * MathUtils.PI);
                float rot = -25f * Interpolation.pow2Out.apply(k) + (time > LAND ? 25f * progress(LAND, LAND + 0.3f) : 0f);
                draw(batch, crown.region, x, y, scale, rot, WHITE, 1f);
                float crack = progress(LAND + 0.3f, CLIMAX_DEATH);
                if (crack > 0f) {
                    crack(batch, groundX(), groundCrownY() + crown.height * scale / 2f, -90f, crown.height * scale * crack, 7L,
                        hex("1a0a10"), 1f, 5f);
                }
                return;
            }
            flash(batch, GOLD, CLIMAX_DEATH, 0.3f, 0.5f);
            if (time >= DUST + 0.2f) return;                            // il n'en reste que de la poussière
            float fade = 1f - progress(DUST - 0.2f, DUST + 0.2f);
            float s = split();
            draw(batch, left, halfX(0), halfY(), scale, -30f * s, WHITE, fade);
            draw(batch, right, halfX(1), halfY(), scale, 30f * s, WHITE, fade);
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
        private static final Color NEW = hex("ff5ad8"), OLD = hex("7ae8ff");
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

    /** Les couleurs des trois pouvoirs volés, comme à son entrée : la Comète, la Reine, l'Éclat Originel. */
    static final Color[] SHARDS = {Color.valueOf("ffc93a"), Color.valueOf("e0283a"), Color.valueOf("fff6c8")};
    /** Ses yeux sur son portrait (pixels, depuis le haut à gauche). */
    static final float[][] PRETENDER_EYES = {{14f, 17.5f}, {21f, 17.5f}};

    /**
     * Le morceau {@code region}, centré à l'écran sur ({@code x}, {@code y}) et agrandi {@code scale} fois, vole en
     * carrés de {@code cell} pixels teintés {@code tint} ; {@code squash} écrase sa hauteur (un morceau couché).
     */
    static void burst(BossScene scene, TextureRegion region, float x, float y, float scale, float squash, int cell, Color tint,
                      float speed, float gravity, float life) {
        int w = region.getRegionWidth(), h = region.getRegionHeight();
        for (int py = 0; py < h; py += cell) {
            for (int px = 0; px < w; px += cell) {
                int cw = Math.min(cell, w - px), ch = Math.min(cell, h - py);
                float cx = x + (px + cw / 2f - w / 2f) * scale, cy = y + (h / 2f - py - ch / 2f) * scale * squash;
                float dx = cx - x, dy = cy - y, length = Math.max(1f, (float) Math.sqrt(dx * dx + dy * dy));
                float v = speed * (0.4f + 0.6f * scene.random.nextFloat());
                Cutscene.Particle piece = scene.place(new TextureRegion(region, px, py, cw, ch), cx, cy,
                    dx / length * v + (scene.random.nextFloat() - 0.5f) * speed * 0.4f, dy / length * v * 0.5f + speed * 0.35f,
                    scale, life * (0.7f + 0.6f * scene.random.nextFloat()));
                piece.gravity = gravity;
                piece.spin = (scene.random.nextFloat() - 0.5f) * 600f;
                piece.color.set(tint);
            }
        }
    }

    /** Socle des trois scènes du Prétendant : ses vrais éclats, qui se fendent puis se brisent un à un. */
    abstract static class PretenderScene extends BossScene {

        PretenderScene(CutsceneKit kit, Moment moment, int phase) { super(kit, moment, EnemyKind.PRETENDANT, phase); }

        /** @return son portrait sans {@code props}. */
        protected static int[][] without(StoryPortraits.Prop... props) { return StoryPortraits.without(EnemyKind.PRETENDANT, props); }

        /** Dessine son vrai éclat {@code piece} à sa place, qui brille de {@code color} ; {@code crack} de 0 à 1 le fend. */
        protected void drawShard(Batch batch, Piece piece, Color color, float crack) {
            float x = pieceX(piece), y = pieceY(piece), s = bossScale();
            float shake = crack > 0f ? (random.nextFloat() - 0.5f) * 8f * crack : 0f;
            glow(batch, x, y, s * (14f + 6f * crack), color, 0.55f + 0.3f * crack);
            draw(batch, piece.region, x + shake, y, s, 0f, WHITE, 1f);
            if (crack > 0f) {
                crack(batch, x + shake, y + piece.height * s * 0.45f, -95f, piece.height * s * 0.9f * crack, 3L, hex("1a0a0e"), 1f, 4f);
                crack(batch, x + shake, y, -20f, piece.width * s * 0.5f * crack, 5L, hex("1a0a0e"), 1f, 3f);
            }
        }

        /** Son vrai éclat {@code piece} se brise : ses pixels volent, avec des étincelles de sa couleur. */
        protected void shatter(Piece piece, Color color) {
            float x = pieceX(piece), y = pieceY(piece);
            sparks(x, y, 60, color, 900f, 18f);
            burst(this, piece.region, x, y, bossScale(), 1f, 1, WHITE, 700f, 1500f, 1.4f);
        }
    }

    /**
     * Phase 2 : sa carte Rouleau interdit flotte à côté de lui, tenue par un fil de lumière à l'éclat de l'Éclat
     * Originel (à droite). L'éclat se fend et se brise : le fil casse, le cadenas de la carte tombe, la carte se
     * fend en deux et part en cendres. Plus de rouleau interdit.
     */
    static final class PretenderShard extends PretenderScene {
        static final float CRACK = 1.0f, ASH = 2.75f;
        private static final Color REEL = hex("e0283a"), ASHES = hex("4a3a5a");
        private final Piece shard;
        private final TextureRegion rest, card, cardLeft, cardRight, lock;
        private boolean loose;

        PretenderShard(CutsceneKit kit) {
            super(kit, Moment.PHASE, 2);
            int[][] after = without(StoryPortraits.Prop.PRETENDER_SHARD_RIGHT);
            shard     = lift(StoryPortraits.pixels(EnemyKind.PRETENDANT), after);
            rest      = pixels(after);
            card      = gameCard("cards/dark/FORBIDDEN_REEL.png");      // la carte du jeu, telle qu'il la joue
            cardLeft  = new TextureRegion(card, 0, 0, 10, 18);
            cardRight = new TextureRegion(card, 10, 0, 9, 18);
            lock      = new TextureRegion(card, 0, 18, 19, 9);
        }

        @Override protected void restart() { loose = false; }

        private float cardScale() { return fit(worldHeight() * 0.34f, 27); }
        private float cardX() { return bossX() + bossWidth() * 0.5f + worldWidth() * 0.15f; }
        private float cardY() { return bossY() + 20f + 10f * MathUtils.sin(time * 2.4f) + 60f * (1f - Interpolation.pow2Out.apply(progress(0f, 0.6f))); }
        /** @return le centre du haut de la carte (sa fenêtre barrée), au-dessus du cadenas. */
        private float topY() { return cardY() + 4.5f * cardScale(); }
        private float lockY() { return cardY() - 9f * cardScale(); }

        /** @return la place à l'écran d'une moitié de la carte fendue ({@code side} -1 ou 1) : elles s'écartent et basculent. */
        private float halfX(int side) {
            float k = Interpolation.pow2Out.apply(progress(CLIMAX_PHASE, ASH));
            return cardX() + side * (cardScale() * 4.75f + 70f * k);
        }
        private float halfY() { return topY() - 50f * progress(CLIMAX_PHASE, ASH) * progress(CLIMAX_PHASE, ASH); }

        @Override
        protected void step(float delta) {
            if (at(CRACK)) rumble(0.2f, 5f);
            if (at(CLIMAX_PHASE)) {
                rumble(0.8f, 14f);
                shatter(shard, SHARDS[2]);
                sparks(cardX(), topY(), 50, REEL, 800f, 18f);
                Particle padlock = place(lock, cardX(), lockY(), 120f, 260f, cardScale(), 1.6f);   // le cadenas saute
                padlock.gravity = 1800f;
                padlock.spin = -260f;
            }
            if (at(ASH)) {                                              // les deux moitiés partent en cendres
                for (int side = -1; side <= 1; side += 2) {
                    burst(this, side < 0 ? cardLeft : cardRight, halfX(side), halfY(), cardScale(), 1f, 2, ASHES, 260f, -120f, 1.8f);
                    for (int i = 0; i < 30; i++) {
                        Particle smoke = particle(halfX(side) + (random.nextFloat() - 0.5f) * 120f, halfY() + (random.nextFloat() - 0.5f) * 160f,
                            80f + 20f * random.nextFloat(), 60f + 100f * random.nextFloat(), 1.6f, 70f + 70f * random.nextFloat());
                        smoke.color.set(0.35f, 0.25f, 0.45f, 0.5f);
                        smoke.drag = 0.4f;
                        glows.add(smoke);
                    }
                }
            }
        }

        @Override
        protected void back(Batch batch) {
            if (!loose) { loose = true; wear(rest); }                   // son éclat de droite est dessiné à part
            poseX = time < CLIMAX_PHASE ? (random.nextFloat() - 0.5f) * 6f * progress(CRACK, CLIMAX_PHASE) : 0f;
            poseTilt = 5f * gesture(CLIMAX_PHASE, CLIMAX_PHASE + 0.7f);  // il recule
        }

        @Override
        protected void front(Batch batch) {
            float s = cardScale(), crack = progress(CRACK, CLIMAX_PHASE);
            if (time < CLIMAX_PHASE) {
                // le fil de lumière entre l'éclat et la carte
                float flicker = time > CRACK && random.nextFloat() < 0.3f * crack ? 0.2f : 1f;
                float link = progress(0.3f, 0.8f) * flicker;
                additive(batch);
                line(batch, SHARDS[2], 0.35f * link, pieceX(shard), pieceY(shard), cardX() - s * 9f, topY(), 18f);
                line(batch, WHITE, 0.8f * link, pieceX(shard), pieceY(shard), cardX() - s * 9f, topY(), 4f);
                normal(batch);
                drawShard(batch, shard, SHARDS[2], crack);
                float shake = (random.nextFloat() - 0.5f) * 6f * crack;
                float alpha = progress(0f, 0.4f);
                glow(batch, cardX(), cardY(), s * 40f, REEL, 0.45f * alpha * (0.8f + 0.2f * MathUtils.sin(time * 8f)));
                draw(batch, card, cardX() + shake, cardY(), s, 0f, WHITE, alpha);
                if (crack > 0f) crack(batch, cardX() + shake, topY() + s * 8f, -90f, s * 17f * crack, 11L, hex("fff6c8"), crack, 4f);
            } else if (time < ASH) {                                    // la carte fendue en deux, sans son cadenas
                float k = progress(CLIMAX_PHASE, ASH), light = 1f - 0.5f * k;
                draw(batch, cardLeft, halfX(-1), halfY(), s, 22f * k, tmp.set(light, light, light, 1f), 1f);
                draw(batch, cardRight, halfX(1), halfY(), s, -22f * k, tmp.set(light, light, light, 1f), 1f);
            }
            wave(batch, CLIMAX_PHASE, 0.8f, cardX(), cardY(), REEL, worldWidth() * 1.1f);
            flash(batch, SHARDS[2], CLIMAX_PHASE, 0.3f, 0.45f);
        }
    }

    /**
     * Phase 3 : le fantôme de la Reine paraît à sa gauche et tire sur l'éclat qu'il lui a volé (le rouge, à gauche) ;
     * il se brise, son éclat rouge rejoint la Reine, qui remonte au ciel avec sa mise royale. Ses yeux s'allument.
     */
    static final class PretenderQueen extends PretenderScene {
        private final Piece shard;
        private final TextureRegion queen, rest;
        private boolean loose;

        PretenderQueen(CutsceneKit kit) {
            super(kit, Moment.PHASE, 3);
            int[][] before = without(StoryPortraits.Prop.PRETENDER_SHARD_RIGHT);
            int[][] after  = without(StoryPortraits.Prop.PRETENDER_SHARD_RIGHT, StoryPortraits.Prop.PRETENDER_SHARD_LEFT);
            shard = lift(before, after);
            rest  = pixels(after);
            queen = kit.portrait(EnemyKind.REINE);
        }

        @Override protected void restart() { loose = false; }

        private float ghostX() { return bossX() - bossWidth() * 0.95f; }
        private float rise() { return worldHeight() * 0.45f * Interpolation.pow2In.apply(progress(CLIMAX_PHASE + 0.5f, 4.2f)); }
        private float ghostY() { return bossBottom() + 50f + MathUtils.sin(time * 2f) * 10f + rise(); }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_PHASE)) {
                rumble(0.7f, 12f);
                shatter(shard, SHARDS[1]);
                float x = pieceX(shard), y = pieceY(shard);
                for (int i = 0; i < 40; i++) {                          // son pouvoir rejoint la Reine
                    Particle mote = particle(x, y, 0f, 0f, 0.6f + 0.3f * random.nextFloat(), 14f + 10f * random.nextFloat());
                    float tx = ghostX() + (random.nextFloat() - 0.5f) * 80f, ty = ghostY() + bossHeight() * 0.5f;
                    mote.vx = (tx - x) / mote.life;
                    mote.vy = (ty - y) / mote.life;
                    mote.color.set(SHARDS[1]);
                    glows.add(mote);
                }
            }
        }

        @Override
        protected void back(Batch batch) {
            if (!loose) { loose = true; wear(rest); }                   // son éclat de gauche est dessiné à part
            float ghost = progress(0.3f, 1.3f) * (1f - progress(3.6f, 4.3f));
            float scale = bossScale() * 1.1f;
            float w = queen.getRegionWidth() * scale, h = queen.getRegionHeight() * scale;
            additive(batch);
            batch.setColor(0.75f, 0.85f, 1f, 0.5f * ghost);
            batch.draw(queen, ghostX() - w / 2f, ghostY(), w, h);
            float red = progress(CLIMAX_PHASE + 0.3f, CLIMAX_PHASE + 0.6f) * ghost;
            batch.setColor(1f, 0.25f, 0.3f, 0.35f * red);
            batch.draw(queen, ghostX() - w / 2f, ghostY(), w, h);
            normal(batch);
            poseX = time < CLIMAX_PHASE ? -10f * progress(1.0f, CLIMAX_PHASE) + (random.nextFloat() - 0.5f) * 4f : 0f;
            poseTintColor.set(GOLD);
            poseTint = 0.2f * progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.5f);
        }

        @Override
        protected void front(Batch batch) {
            if (time < CLIMAX_PHASE) {
                float pull = progress(1.0f, 1.6f);
                if (pull > 0f) {                                        // la Reine tire sur son éclat : un fil rouge
                    additive(batch);
                    float qx = ghostX() + bossWidth() * 0.3f, qy = ghostY() + bossHeight() * 0.35f;
                    line(batch, SHARDS[1], 0.4f * pull, qx, qy, pieceX(shard), pieceY(shard), 16f);
                    line(batch, WHITE, 0.7f * pull, qx, qy, pieceX(shard), pieceY(shard), 4f);
                    normal(batch);
                }
                drawShard(batch, shard, SHARDS[1], progress(1.2f, CLIMAX_PHASE));
            }
            eyes(batch, progress(CLIMAX_PHASE + 0.2f, CLIMAX_PHASE + 0.5f), GOLD, PRETENDER_EYES);
            flash(batch, SHARDS[1], CLIMAX_PHASE, 0.3f, 0.4f);
        }
    }

    /**
     * Le trône du maître de la chance l'attend au bout de la rue, sous un rayon de lumière. Il marche vers lui, tend
     * la main ; le dernier éclat (celui de la Comète, sur sa poitrine) se brise, il s'écroule au pied des marches,
     * sa vraie couronne roule jusqu'au trône. Le rayon s'éteint et le trône s'effondre en poussière d'or : il n'y
     * aura jamais de trône pour lui.
     */
    static final class PretenderDeath extends PretenderScene {
        static final float WALK = 0.3f, REACH = 2.0f, FALL = CLIMAX_DEATH, DOWN = FALL + 0.55f, CRUMBLE = 3.15f;
        private static final Color STONE = hex("6a4a3a"), STONE_TOP = hex("9a7a5a"), BEAM = hex("fff2a0");
        private static final String[] THRONE = {
            ".........oo.........", "........oyyo........", "...ooo..oggo..ooo...", "...oyo.oggggo.oyo...",
            "...oggoggggggoggo...", "...oggggggggggggo...", "...ogdrrrrrrrrdgo...", "...ogdrrrrrrrrdgo...",
            "...ogdrrRrrRrrdgo...", "...ogdrrrrrrrrdgo...", "...ogdrrrrrrrrdgo...", "...ogdrrRrrRrrdgo...",
            "...ogdrrrrrrrrdgo...", "oooogdrrrrrrrrdgoooo", "oyyoodrrrrrrrrdooyyo", "oggggggggggggggggggo",
            "orrrrrrrrrrrrrrrrrro", "oddddddddddddddddddo", "oooooooooooooooooooo", ".ogo............ogo.",
            ".ogo............ogo.", ".ogo............ogo.", ".odo............odo.", "oyyyo..........oyyyo",
            "ooooo..........ooooo"};
        private final Piece shard, crown;
        private final TextureRegion noShard, bare, throne;
        private boolean loose, off;
        private float crownX, crownY;

        PretenderDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, 0);
            int[][] start = without(StoryPortraits.Prop.PRETENDER_SHARD_RIGHT, StoryPortraits.Prop.PRETENDER_SHARD_LEFT);
            int[][] broken = without(StoryPortraits.Prop.PRETENDER_SHARD_RIGHT, StoryPortraits.Prop.PRETENDER_SHARD_LEFT,
                StoryPortraits.Prop.PRETENDER_SHARD_CHEST);
            int[][] uncrowned = without(StoryPortraits.Prop.PRETENDER_SHARD_RIGHT, StoryPortraits.Prop.PRETENDER_SHARD_LEFT,
                StoryPortraits.Prop.PRETENDER_SHARD_CHEST, StoryPortraits.Prop.PRETENDER_CROWN);
            shard   = lift(start, broken);
            crown   = lift(broken, uncrowned);
            noShard = pixels(broken);
            bare    = pixels(uncrowned);
            throne  = art("ogdyrR", new Color[] {hex("1a0a0e"), hex("ffc93a"), hex("b8801f"), hex("fff2a0"), hex("b0202c"), hex("7a1420")}, THRONE);
        }

        @Override protected void restart() { loose = false; off = false; }

        private float throneScale() { return bossScale() * 1.45f; }
        private float throneX() { return worldWidth() * 0.85f; }
        private float daisTop() { return groundY() + 78f; }
        private float throneY() { return daisTop() + THRONE.length * throneScale() / 2f; }
        /** @return où sa couronne finit sa course : sur l'estrade, au pied du trône. */
        private float restX() { return throneX() - 6f * throneScale(); }
        private float restY() { return daisTop() + crown.height * bossScale() * 0.4f; }

        /** @return le degré dont il bascule vers le trône (négatif : vers la droite). */
        private float tilt() {
            float lean = -10f * Interpolation.pow2Out.apply(progress(REACH, FALL));
            float fall = Interpolation.bounceOut.apply(progress(FALL, DOWN + 0.2f));
            return MathUtils.lerp(lean, -86f, fall);
        }

        /** @return la place à l'écran du pixel ({@code px}, {@code py}) de son portrait, tel qu'il bascule. */
        private float mapX(float px, float py) {
            float s = bossScale(), dx = (px - boss.getRegionWidth() / 2f) * s, dy = (boss.getRegionHeight() - py) * s;
            return bossX() + poseX + dx * MathUtils.cosDeg(poseTilt) - dy * MathUtils.sinDeg(poseTilt);
        }
        private float mapY(float px, float py) {
            float s = bossScale(), dx = (px - boss.getRegionWidth() / 2f) * s, dy = (boss.getRegionHeight() - py) * s;
            return bossBottom() + poseY + dx * MathUtils.sinDeg(poseTilt) + dy * MathUtils.cosDeg(poseTilt);
        }

        @Override
        protected void step(float delta) {
            if (at(FALL)) { rumble(0.6f, 12f); shatter(shard, SHARDS[0]); }
            if (at(DOWN)) {                                             // il touche le sol, au pied des marches
                rumble(0.4f, 9f);
                sparks(bossX() + bossHeight() * 0.4f, groundY() + 30f, 40, hex("d8c8a8"), 500f, 12f);
            }
            if (at(FALL + 0.8f)) sparks(restX(), restY(), 20, GOLD, 300f, 10f);
            if (at(CRUMBLE)) {                                          // le trône s'effondre
                rumble(0.7f, 12f);
                burst(this, throne, throneX(), throneY(), throneScale(), 1f, 2, WHITE, 420f, 1500f, 1.8f);
                for (int i = 0; i < 40; i++) {
                    Particle dust = particle(throneX() + (random.nextFloat() - 0.5f) * 200f, daisTop() + random.nextFloat() * 200f,
                        60f + 60f * random.nextFloat(), 60f + 160f * random.nextFloat(), 1.8f, 80f + 80f * random.nextFloat());
                    dust.color.set(random.nextBoolean() ? GOLD : hex("d8c8a8"));
                    dust.color.a = 0.4f;
                    dust.drag = 0.3f;
                    glows.add(dust);
                }
            }
        }

        @Override
        protected void back(Batch batch) {
            // la marche vers le trône, en sautillant, puis la chute
            float walk = Interpolation.smooth.apply(progress(WALK, REACH));
            poseX = MathUtils.lerp(-worldWidth() * 0.2f, worldWidth() * 0.05f, walk);
            poseTilt = tilt();
            float down = Interpolation.bounceOut.apply(progress(FALL, DOWN + 0.2f));
            poseY = (time > WALK && time < REACH ? Math.abs(MathUtils.sin(time * 9f)) * 14f : 0f)
                + bossWidth() / 2f * Math.abs(MathUtils.sinDeg(poseTilt)) + (groundY() - bossBottom()) * down;
            poseShadow = 0.45f * progress(FALL, DOWN + 0.6f);
            if (!loose) { loose = true; wear(noShard); }                // son dernier éclat est dessiné à part
            if (!off && time >= FALL) {                                 // sa vraie couronne lui échappe
                off = true;
                crownX = mapX(crown.left + crown.width / 2f, crown.top + crown.height / 2f);
                crownY = mapY(crown.left + crown.width / 2f, crown.top + crown.height / 2f);
                wear(bare);
            }
            // l'estrade et le trône, sous son rayon de lumière
            float beam = progress(0f, 0.5f) * (1f - progress(FALL, FALL + 0.5f));
            if (beam > 0f) {
                additive(batch);
                for (int i = 0; i < 3; i++) {
                    float w = 420f - i * 120f;
                    batch.setColor(BEAM.r, BEAM.g, BEAM.b, (0.16f + 0.08f * i) * beam * (0.9f + 0.1f * MathUtils.sin(time * 5f + i)));
                    batch.draw(soft, throneX() - w / 2f, daisTop() - 60f, w, worldHeight() * 1.6f);
                }
                normal(batch);
                glow(batch, throneX(), throneY(), 700f, GOLD, 0.55f * beam);
            }
            float wide = 12f * throneScale() + 70f, narrow = 12f * throneScale();
            rect(batch, STONE, 1f, throneX() - wide, groundY() - 10f, 2f * wide, 50f);
            rect(batch, STONE_TOP, 1f, throneX() - wide, groundY() + 32f, 2f * wide, 8f);
            rect(batch, STONE, 1f, throneX() - narrow, groundY() + 40f, 2f * narrow, 38f);
            rect(batch, STONE_TOP, 1f, throneX() - narrow, groundY() + 70f, 2f * narrow, 8f);
            if (time < CRUMBLE) {
                float shake = time > CRUMBLE - 0.5f ? (random.nextFloat() - 0.5f) * 8f : 0f;
                draw(batch, throne, throneX() + shake, throneY(), throneScale(), 0f, WHITE, 1f);
                float crack = progress(CRUMBLE - 0.6f, CRUMBLE);
                if (crack > 0f) {
                    crack(batch, throneX(), throneY() + THRONE.length * throneScale() * 0.45f, -90f, THRONE.length * throneScale() * 0.9f * crack,
                        17L, hex("1a0a0e"), 1f, 5f);
                }
            }
        }

        @Override
        protected void front(Batch batch) {
            if (time < FALL) {
                float reach = progress(REACH - 0.6f, FALL);
                drawShard(batch, shard, SHARDS[0], reach);
            }
            if (off) {                                                  // la couronne vole, rebondit et roule au pied du trône
                float k = progress(FALL, FALL + 0.8f), roll = progress(FALL + 0.8f, FALL + 1.4f);
                float x = MathUtils.lerp(crownX, restX() - 30f, Interpolation.pow2Out.apply(k)) + 30f * Interpolation.pow2Out.apply(roll);
                float y = MathUtils.lerp(crownY, restY(), Interpolation.bounceOut.apply(k)) + 120f * MathUtils.sin(k * MathUtils.PI);
                float rot = -540f * Interpolation.pow2Out.apply(k) - 60f * Interpolation.pow2Out.apply(roll);
                draw(batch, crown.region, x, y, bossScale(), rot, tmp.set(1f, 1f, 1f, 1f), 1f);
                if (time > CRUMBLE) glow(batch, x, y, 120f, GOLD, 0.35f * (1f - progress(CRUMBLE, CRUMBLE + 1.5f)));
            }
            flash(batch, SHARDS[0], FALL, 0.3f, 0.45f);
            flash(batch, GOLD, CRUMBLE, 0.25f, 0.3f);
        }
    }

    // -------------------------------------------------------------------------
    // La Maison
    // -------------------------------------------------------------------------

    /**
     * Socle des scènes de la Maison, dans son décor angélique (choix d'Astra, 2026-11-22) : un ciel doré et rose,
     * des rayons de lumière, une mer de nuages où repose le manoir. {@link #evil} le fait virer au diabolique (ciel
     * rouge, nuages de cendre, braises) pour la phase 3 ; {@link #holy} l'éclaire encore plus (sa mort).
     */
    abstract static class HouseScene extends BossScene {
        static final Color SKY_TOP = hex("4a4aa8"), SKY_MID = hex("d898c8"), SKY_LOW = hex("ffe2a8"), HALO = hex("fff0c0");
        static final Color HELL_TOP = hex("140204"), HELL_MID = hex("5a0a0a"), HELL_LOW = hex("e8320a");
        static final Color CLOUD = hex("fff6f2"), CLOUD_SHADE = hex("d4a0c4"), ASH = hex("2a0c0e"), ASH_SHADE = hex("120406");
        static final Color HELL = hex("c81a0a");
        /** Le corps de son portrait (colonnes 7 à 26, lignes 13 à 38), le haut de son plancher, ses yeux-fenêtres. */
        static final float BODY_LEFT = 7f, BODY_RIGHT = 27f, BODY_TOP = 13f, BODY_BOTTOM = 39f, BODY_FLOOR = 34f;
        static final float[][] WINDOW_EYES = {{12f, 22f}, {22f, 22f}};
        protected final TextureRegion machine, wing, horn;
        protected float evil, holy;
        private final Color hell = new Color(), shade = new Color();

        HouseScene(CutsceneKit kit, Moment moment, int phase) {
            super(kit, moment, EnemyKind.MAISON, phase);
            machine = art("ogdwr", new Color[] {hex("0a0808"), hex("ffc93a"), hex("b8801f"), hex("f4ecdc"), hex("e0283a")},
                "..oooooooooo..", ".oggggggggggo.", "oggggggggggggo", "ogoooooooooogo", "ogowwowwowwogo", "ogowrowrowrogo",
                "ogowwowwowwogo", "ogoooooooooogo", "oggggggggggggo", "ogggddddddgggo", "oggggggggggggo", "oddddddddddddo",
                "oooooooooooooo");
            wing = art("wg", new Color[] {hex("ffffff"), hex("c8d0ec")},
                "......ww", "....wwww", "..wwwwwg", ".wwwwwg.", "wwwwgg..", "wwgg....", "wg......");
            horn = art("rd", new Color[] {hex("8a0a0a"), hex("3a0404")},
                "....d", "...rd", "..rrd", ".rrd.", "rrrd.", "rrd..");
        }

        /** @return son portrait sans {@code props}. */
        protected static int[][] without(StoryPortraits.Prop... props) { return StoryPortraits.without(EnemyKind.MAISON, props); }

        /** @return le haut de la mer de nuages, où tombent et se brisent les morceaux. */
        protected float cloudTop() { return groundY() + 40f; }

        /** @return où la porte de son coffre finit sa course, couchée sur les nuages (phase 3, puis sa mort). */
        protected float doorRestX() { return portraitX(30f) + worldWidth() * 0.2f; }

        /**
         * Le ciel : doré au ras des nuages, rose puis bleu en haut, des étincelles d'or, un halo et des rayons de
         * lumière qui tombent sur le manoir ; avec {@link #evil}, il vire au rouge sang et la lumière s'éteint.
         */
        protected void sky(Batch batch) {
            int bands = 36;
            float height = worldHeight() + 2f * MARGIN, band = height / bands;
            for (int i = 0; i < bands; i++) {
                float k = i / (float) (bands - 1);
                if (k < 0.45f) tmp.set(SKY_LOW).lerp(SKY_MID, k / 0.45f);
                else tmp.set(SKY_MID).lerp(SKY_TOP, (k - 0.45f) / 0.55f);
                if (k < 0.5f) hell.set(HELL_LOW).lerp(HELL_MID, k * 2f);
                else hell.set(HELL_MID).lerp(HELL_TOP, (k - 0.5f) * 2f);
                tmp.lerp(hell, evil);
                if (holy > 0f) tmp.lerp(hex("fff8e0"), holy * (0.55f - 0.3f * k));
                batch.setColor(tmp.r, tmp.g, tmp.b, 1f);
                batch.draw(pixel, -MARGIN, -MARGIN + i * band - 1f, worldWidth() + 2f * MARGIN, band + 2f);
            }
            for (int i = 0; i < 44; i++) {                              // des étincelles d'or qui scintillent
                float x = (i * 173.3f) % worldWidth(), y = worldHeight() * (0.45f + 0.53f * ((i * 61) % 100) / 100f);
                float twinkle = 0.5f + 0.5f * MathUtils.sin(time * 3f + i * 1.3f), s = 3f + 3f * twinkle;
                rect(batch, hex("fff4c8"), 0.8f * twinkle * (1f - evil), x - s / 2f, y - s / 2f, s, s);
            }
            tmp.set(HALO).lerp(HELL_LOW, evil).lerp(GOLD, holy);
            glow(batch, bossX(), bossY() + bossHeight() * 0.1f, worldWidth() * 1.1f, tmp, 0.3f);
            glow(batch, bossX(), bossY() + bossHeight() * 0.1f, worldWidth() * 0.4f, tmp, 0.25f * (1f - 0.6f * evil));
            float rays = 0.07f * (1f - evil) + 0.1f * holy;            // les rayons de lumière tombent sur le manoir
            if (rays > 0f) {
                additive(batch);
                for (int i = 0; i < 9; i++) {
                    float angle = 180f + (i - 4f) * 11f + 2.5f * MathUtils.sin(time * 0.8f + i);
                    batch.setColor(1f, 0.95f, 0.8f, rays * (i % 2 == 0 ? 1f : 0.6f));
                    batch.draw(soft, bossX() - 55f, worldHeight() + 120f, 55f, 0f, 110f, worldHeight() * 1.5f, 1f, 1f, angle);
                }
                normal(batch);
            }
            for (int i = 0; i < 9; i++) {                               // la mer de nuages, au loin
                float x = worldWidth() * (i - 0.5f) / 8f, y = groundY() + 150f + 25f * MathUtils.sin(i * 2.3f), r = 120f + 30f * (i % 3);
                cloudColor(CLOUD_SHADE, ASH_SHADE);
                batch.setColor(tmp.r, tmp.g, tmp.b, 0.75f);
                batch.draw(soft, x - r, y - r * 0.45f, r * 2f, r * 0.9f);
            }
        }

        private void cloudColor(Color holyColor, Color evilColor) {
            tmp.set(holyColor).lerp(evilColor, evil);
            if (holy > 0f) tmp.lerp(hex("fff4d8"), 0.4f * holy);
        }

        /** La mer de nuages où repose le manoir, comme à son entrée ; de cendre, éclairée de rouge, avec {@link #evil}. */
        protected void clouds(Batch batch) {
            cloudColor(CLOUD_SHADE, ASH_SHADE);
            shade.set(tmp);
            cloudColor(CLOUD, ASH);
            for (int i = 0; i < 16; i++) {                              // l'ombre des nuages, puis leur dessus
                float x = worldWidth() * i / 15f, y = groundY() + 12f + 18f * MathUtils.sin(i * 1.7f), r = 150f + 40f * ((i * 7) % 3);
                batch.setColor(shade.r, shade.g, shade.b, 1f);
                batch.draw(soft, x - r, y - r * 0.6f - 14f, r * 2f, r * 1.2f);
            }
            for (int i = 0; i < 16; i++) {
                float x = worldWidth() * i / 15f, y = groundY() + 20f + 18f * MathUtils.sin(i * 1.7f), r = 140f + 40f * ((i * 7) % 3);
                batch.setColor(tmp.r, tmp.g, tmp.b, 1f);
                batch.draw(soft, x - r, y - r * 0.6f, r * 2f, r * 1.2f);
            }
            rect(batch, tmp, 1f, -MARGIN, -MARGIN, worldWidth() + 2f * MARGIN, groundY() + MARGIN);
            if (evil > 0f) {                                            // les braises de l'enfer, sous la cendre
                glow(batch, bossX(), groundY(), worldWidth() * 1.4f, HELL, 0.45f * evil);
                fill(batch, HELL, 0.12f * evil);
            }
        }

        /** Des machines à sous aux ailes d'ange qui volent dans sa salle de jeu (sans façade). */
        protected void interior(Batch batch, float alpha) {
            if (alpha <= 0f) return;
            float x0 = portraitX(BODY_LEFT), x1 = portraitX(BODY_RIGHT), y0 = portraitY(BODY_FLOOR), y1 = portraitY(BODY_TOP);
            glow(batch, (x0 + x1) / 2f, y1 - 20f, (x1 - x0) * 0.9f, GOLD, 0.35f * alpha);
            float scale = (x1 - x0) / 50f;
            for (int i = 0; i < 3; i++) {
                float mx = MathUtils.lerp(x0, x1, 0.2f + i * 0.3f), my = MathUtils.lerp(y0, y1, i == 1 ? 0.62f : 0.38f)
                    + 10f * MathUtils.sin(time * 3f + i * 2f);
                flyer(batch, mx, my, scale, i, alpha);
            }
        }

        /** Une machine à sous aux ailes d'ange, centrée sur ({@code x}, {@code y}). */
        protected void flyer(Batch batch, float x, float y, float scale, int i, float alpha) {
            float flap = 1f + 0.25f * MathUtils.sin(time * 14f + i);
            float ww = wing.getRegionWidth() * scale, wh = wing.getRegionHeight() * scale * flap;
            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(wing, x - 7f * scale - ww, y - wh * 0.2f, ww, wh);                 // l'aile gauche
            batch.draw(wing, x + 7f * scale + ww, y - wh * 0.2f, -ww, wh);                // l'aile droite, en miroir
            draw(batch, machine, x, y, scale, 0f, WHITE, alpha);
        }

        /** La Maison démoniaque : des cornes sur le toit, des yeux de braise. */
        protected void demon(Batch batch, float alpha) {
            if (alpha <= 0f) return;
            float scale = bossScale(), x = portraitX(17f), y = portraitY(1f);
            for (int side = -1; side <= 1; side += 2) {
                float hw = horn.getRegionWidth() * scale * 1.6f, hh = horn.getRegionHeight() * scale * 1.6f;
                batch.setColor(1f, 1f, 1f, alpha);
                batch.draw(horn, x + side * 40f, y - 10f, side * hw, hh);
            }
            eyes(batch, alpha, hex("ff2a0a"), WINDOW_EYES);
        }

        /** Des braises qui montent. */
        protected void embers(float rate) {
            if (random.nextFloat() >= rate) return;
            Particle ember = particle(worldWidth() * random.nextFloat(), groundY(), 85f + 10f * random.nextFloat(),
                120f + 200f * random.nextFloat(), 2.2f, 8f + 10f * random.nextFloat());
            ember.color.set(random.nextBoolean() ? hex("ff5a1a") : hex("ffb040"));
            glows.add(ember);
        }

        /** Des plumes blanches qui tombent. */
        protected void feathers(float rate) {
            if (random.nextFloat() >= rate) return;
            Particle plume = place(wing, worldWidth() * random.nextFloat(), worldHeight() + 40f, 0f, -90f, 3f, 4f);
            plume.vx = (random.nextFloat() - 0.5f) * 80f;
            plume.spin = (random.nextFloat() - 0.5f) * 120f;
        }

        /** Un nuage de poussière qui monte des nuages, de {@code left} à {@code right}. */
        protected void dust(float left, float right, float y, int count, Color color) {
            for (int i = 0; i < count; i++) {
                Particle puff = particle(MathUtils.lerp(left, right, random.nextFloat()), y, 60f + 60f * random.nextFloat(),
                    200f + 300f * random.nextFloat(), 1.6f, 120f + 120f * random.nextFloat());
                puff.color.set(color);
                puff.drag = 0.3f;
                glows.add(puff);
            }
        }
    }

    /**
     * Phase 2 : dans son ciel angélique, sa vraie façade (corps, fenêtres, porte, blason au bouclier) se fissure
     * tout autour, se décolle, bascule vers nous et s'écrase en morceaux sur les nuages. Derrière, sa salle de jeu
     * ouverte, où volent des machines aux ailes d'ange. Plus de défense.
     */
    static final class HouseFacade extends HouseScene {
        static final float CRACK = 0.45f, TIP = 1.35f;
        private static final Color SEAM = hex("1a0808");
        private final Piece facade;
        private final TextureRegion open;
        private boolean loose;

        HouseFacade(CutsceneKit kit) {
            super(kit, Moment.PHASE, 2);
            int[][] rest = without(StoryPortraits.Prop.HOUSE_FACADE);
            facade = lift(StoryPortraits.pixels(EnemyKind.MAISON), rest);
            open = pixels(rest);
        }

        @Override protected void restart() { loose = false; }

        private float facadeBottom() { return portraitY(facade.top + facade.height); }

        @Override
        protected void step(float delta) {
            if (at(CRACK)) rumble(0.2f, 5f);
            if (at(TIP)) { rumble(0.3f, 8f); sparks(pieceX(facade), facadeBottom(), 30, hex("fff0c8"), 500f, 12f); }
            if (at(CLIMAX_PHASE)) {                                     // elle s'écrase sur les nuages, en morceaux
                rumble(0.9f, 18f);
                float y = facadeBottom() - 20f;
                burst(this, facade.region, pieceX(facade), y, bossScale() * 1.25f, 0.25f, 3, WHITE, 900f, 1700f, 1.7f);
                dust(bossX() - bossWidth(), bossX() + bossWidth(), cloudTop(), 34, tmp.set(1f, 0.95f, 0.9f, 0.35f));
            }
        }

        @Override
        protected void back(Batch batch) {
            sky(batch);
            clouds(batch);
            if (!loose && time >= TIP) { loose = true; wear(open); }    // la façade se décolle : elle est dessinée à part
            poseX = time < TIP ? (random.nextFloat() - 0.5f) * 8f * progress(CRACK, TIP) : 0f;
        }

        @Override
        protected void front(Batch batch) {
            if (!loose) {                                               // les fissures courent tout autour de la façade
                float k = progress(CRACK, TIP);
                float left = portraitX(BODY_LEFT), right = portraitX(BODY_RIGHT), top = portraitY(BODY_TOP), bottom = portraitY(BODY_BOTTOM);
                crack(batch, left, top, -90f, (top - bottom) * k, 21L, SEAM, 1f, 6f);
                crack(batch, right, top, -90f, (top - bottom) * k, 22L, SEAM, 1f, 6f);
                crack(batch, left, top, 0f, (right - left) * k, 23L, SEAM, 1f, 6f);
                crack(batch, bossX() + poseX, portraitY(5f), -90f, (portraitY(5f) - top) * k, 24L, SEAM, 1f, 4f);
            } else {
                interior(batch, progress(TIP, CLIMAX_PHASE + 0.3f));
                if (time < CLIMAX_PHASE) {                              // elle bascule vers nous, pied posé, et tombe
                    float k = Interpolation.pow2In.apply(progress(TIP, CLIMAX_PHASE));
                    float s = bossScale(), angle = 88f * k;
                    float w = facade.width * s * (1f + 0.7f * MathUtils.sinDeg(angle)), h = facade.height * s * MathUtils.cosDeg(angle);
                    float light = 1f - 0.35f * k;
                    batch.setColor(light, light, light, 1f);
                    batch.draw(facade.region, pieceX(facade) - w / 2f, facadeBottom() - 110f * k, w, Math.max(2f, h));
                }
            }
            flash(batch, hex("fff0c8"), CLIMAX_PHASE, 0.3f, 0.45f);
        }
    }

    /**
     * Phase 3 : les verrous de la porte de son coffre (la vraie, ronde, sur son aile droite) sautent un à un ; la
     * porte se décroche, pend, tombe sur les nuages, roule et se couche. Les potions du coffre vide en jaillissent
     * et se brisent : plus de soins. Le ciel angélique vire au rouge : la Maison devient démon.
     */
    static final class HouseVault extends HouseScene {
        static final float[] BOLTS = {0.35f, 0.65f, 0.95f, 1.25f};
        static final float LURCH = 1.0f, HANG = 1.4f, DROP = 1.8f, FLAT = 3.2f, EVIL = 2.7f;
        private static final Color STEEL = hex("c8c8d4"), HEAL = hex("ff4a6a");
        private final Piece door;
        private final TextureRegion before, after, potion;
        private final Array<Particle> potions = new Array<>(false, 8);
        private final float[] landing = new float[8];
        private boolean loose;

        HouseVault(CutsceneKit kit) {
            super(kit, Moment.PHASE, 3);
            int[][] open = without(StoryPortraits.Prop.HOUSE_FACADE);                          // sa façade est déjà tombée
            int[][] empty = without(StoryPortraits.Prop.HOUSE_FACADE, StoryPortraits.Prop.HOUSE_VAULT);
            door   = lift(open, empty);
            before = pixels(open);
            after  = pixels(empty);
            potion = symbol(EnemySymbol.POTION);
        }

        @Override protected void restart() { loose = false; potions.clear(); }

        /** @return le rayon de la porte une fois tombée (elle tombe vers nous : 1,5 fois sa taille au mur). */
        private float radius() { return door.width * bossScale() * 0.75f; }
        private float restY() { return cloudTop() + radius() * 0.8f; }

        /** @return le centre de la porte : au mur, puis elle tombe sur les nuages et roule vers la droite. */
        private float doorX() {
            float wall = pieceX(door);
            if (time < CLIMAX_PHASE) return wall + 14f * progress(DROP, CLIMAX_PHASE);
            return MathUtils.lerp(wall + 14f, doorRestX(), Interpolation.pow2Out.apply(progress(CLIMAX_PHASE, FLAT)));
        }
        private float doorY() {
            float wall = pieceY(door);
            if (time < HANG) return wall - 3f * progress(LURCH, HANG);
            if (time < DROP) return wall - 3f - 6f * progress(HANG, DROP);
            if (time < CLIMAX_PHASE) return MathUtils.lerp(wall - 9f, restY(), Interpolation.pow2In.apply(progress(DROP, CLIMAX_PHASE)));
            return restY() + 30f * Math.abs(MathUtils.sin((time - CLIMAX_PHASE) * 7f)) * (1f - progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.7f));
        }
        private float doorRotation() {
            if (time < HANG) return -12f * Interpolation.swingOut.apply(progress(LURCH, LURCH + 0.25f));
            if (time < DROP) return -12f - 26f * Interpolation.swingOut.apply(progress(HANG, HANG + 0.3f)) + 6f * MathUtils.sin((time - HANG) * 12f);
            if (time < CLIMAX_PHASE) return -38f - 160f * progress(DROP, CLIMAX_PHASE);
            return -198f - (doorX() - pieceX(door) - 14f) / radius() * MathUtils.radiansToDegrees;
        }

        @Override
        protected void step(float delta) {
            for (int i = 0; i < BOLTS.length; i++) {
                if (at(BOLTS[i])) {                                     // un verrou saute
                    float angle = 45f + 90f * i;
                    sparks(pieceX(door) + MathUtils.cosDeg(angle) * radius() / 1.5f, pieceY(door) + MathUtils.sinDeg(angle) * radius() / 1.5f,
                        16, STEEL, 450f, 8f);
                    rumble(0.1f, 4f);
                }
            }
            if (at(LURCH)) rumble(0.2f, 6f);
            if (at(CLIMAX_PHASE)) {                                     // la porte touche les nuages ; le coffre vide crache ses potions
                rumble(0.9f, 18f);
                sparks(doorX(), cloudTop(), 30, STEEL, 600f, 12f);
                dust(doorX() - 120f, doorX() + 120f, cloudTop(), 24, tmp.set(1f, 0.95f, 0.92f, 0.35f));
                float x = portraitX(30f), y = portraitY(28f), g = 1300f;
                for (int i = 0; i < 5; i++) {                           // chacune se brise entre 2,95 et 3,15 s (le son)
                    float side = i % 2 == 0 ? 1f : -0.6f, flight = 0.75f + 0.05f * i;
                    landing[i] = cloudTop() + 10f * random.nextFloat();
                    float vy = (g * flight * flight / 2f - (y - landing[i])) / flight;
                    Particle bottle = place(potion, x, y, side * (110f + 70f * i), vy, iconScale(0.08f), 3f);
                    bottle.gravity = g;
                    bottle.spin = side * -320f;
                    bottle.delay = i;
                    sprites.removeValue(bottle, true);
                    potions.add(bottle);
                }
            }
            if (at(FLAT)) { rumble(0.4f, 10f); dust(doorRestX() - 90f, doorRestX() + 90f, cloudTop(), 20, tmp.set(1f, 0.9f, 0.9f, 0.3f)); }
            for (int i = potions.size - 1; i >= 0; i--) {               // une potion qui touche les nuages se brise
                Particle bottle = potions.get(i);
                bottle.update(delta);
                if (bottle.y <= landing[(int) bottle.delay] && bottle.vy < 0f) {
                    potions.removeIndex(i);
                    sparks(bottle.x, bottle.y + 10f, 26, HEAL, 500f, 14f);
                    burst(this, potion, bottle.x, bottle.y + 20f, bottle.size, 1f, 4, WHITE, 300f, 1600f, 0.9f);
                }
            }
            if (time > EVIL) embers(evil * 0.8f);
        }

        @Override
        protected void back(Batch batch) {
            evil = progress(EVIL, EVIL + 0.8f);
            sky(batch);
            clouds(batch);
            if (!loose && time >= LURCH) { loose = true; wear(after); } // la vraie porte du coffre se décroche
            else if (!loose) wear(before);
            poseX = time < LURCH ? (random.nextFloat() - 0.5f) * 5f * progress(0.3f, LURCH) : 0f;
            poseTintColor.set(hex("ff1a0a"));
            poseTint = 0.35f * evil;
            poseShadow = 0.4f * evil;
        }

        @Override
        protected void front(Batch batch) {
            if (!loose) {
                glow(batch, pieceX(door), pieceY(door), bossScale() * 12f, STEEL, 0.45f * progress(0.3f, LURCH));
            } else {
                if (time >= CLIMAX_PHASE - 0.1f) {                      // le coffre vide : une lueur rose s'en échappe, puis s'éteint
                    glow(batch, portraitX(30f), portraitY(28f), bossScale() * 16f, HEAL, 0.6f * gesture(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.9f));
                }
                float k = time < FLAT ? 1f : 1f - 0.7f * Interpolation.bounceOut.apply(progress(FLAT, FLAT + 0.4f));
                float s = bossScale() * (1f + 0.5f * progress(DROP, CLIMAX_PHASE)), w = door.width * s, h = door.height * s * k;
                float y = time < FLAT ? doorY() : restY() - door.height * s * (1f - k) / 2f;
                batch.setColor(WHITE);
                batch.draw(door.region, doorX() - w / 2f, y - h / 2f, w / 2f, h / 2f, w, h, 1f, 1f, time < FLAT ? doorRotation() : 0f);
            }
            for (Particle bottle : potions) {
                glow(batch, bottle.x, bottle.y, bottle.size * 30f, HEAL, 0.4f);
                draw(batch, potion, bottle.x, bottle.y, bottle.size, bottle.rotation, WHITE, 1f);
            }
            demon(batch, evil);
            flash(batch, WHITE, CLIMAX_PHASE, 0.25f, 0.3f);
            flash(batch, hex("ff3a0a"), EVIL, 0.3f, 0.35f);
        }
    }

    /**
     * La Maison démoniaque, sans façade ni coffre, tremble sous son ciel rouge et explose dans une lumière d'or ;
     * le ciel redevient angélique, ses machines aux ailes d'ange s'envolent, libres, et des plumes tombent.
     */
    static final class HouseDeath extends HouseScene {
        private final TextureRegion ruined, door;
        private boolean broken;

        HouseDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, 0);
            int[][] open = without(StoryPortraits.Prop.HOUSE_FACADE);
            int[][] empty = without(StoryPortraits.Prop.HOUSE_FACADE, StoryPortraits.Prop.HOUSE_VAULT);
            ruined = pixels(empty);
            Piece piece = lift(open, empty);                            // la porte de son coffre, restée couchée sur les nuages
            door = piece == null ? null : piece.region;
        }

        @Override protected void restart() { broken = false; }

        @Override
        protected void step(float delta) {
            if (!broken && time >= CLIMAX_DEATH) {
                broken = true;
                rumble(0.9f, 16f);
                MinesBossScenes.crumble(this, 3, hex("ffe0a0"), 800f);
                sparks(bossX(), bossY(), 90, GOLD, 1200f, 26f);
                sparks(bossX(), bossY(), 50, WHITE, 900f, 20f);
                for (int i = 0; i < 16; i++) {                          // une gerbe de plumes jaillit de l'explosion
                    float angle = 360f * i / 16f + 10f * random.nextFloat(), speed = 400f + 300f * random.nextFloat();
                    Particle plume = place(wing, bossX(), bossY(), MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed, 3.5f, 3f);
                    plume.drag = 0.2f;
                    plume.gravity = 60f;
                    plume.spin = (random.nextFloat() - 0.5f) * 300f;
                }
            }
            embers(evil * 0.8f);
            feathers(holy * 0.4f);
        }

        @Override
        protected void back(Batch batch) {
            evil = 1f - progress(CLIMAX_DEATH + 0.2f, CLIMAX_DEATH + 1.4f);
            holy = progress(CLIMAX_DEATH + 0.4f, CLIMAX_DEATH + 1.6f);
            sky(batch);
            clouds(batch);
            if (door != null) {                                         // la porte du coffre, couchée là où elle a roulé
                float s = bossScale() * 1.5f, w = door.getRegionWidth() * s, h = door.getRegionHeight() * s * 0.3f;
                float light = 1f - 0.4f * evil;
                batch.setColor(light, light * (1f - 0.3f * evil), light * (1f - 0.3f * evil), 1f);
                batch.draw(door, doorRestX() - w / 2f, cloudTop() + door.getRegionHeight() * s * 0.4f - h / 2f, w, h);
            }
            if (time < CLIMAX_DEATH) wear(ruined);
            showBoss = time < CLIMAX_DEATH;
            poseTintColor.set(hex("ff1a0a"));
            poseTint = 0.35f;
            poseShadow = 0.4f;
            poseX = (random.nextFloat() - 0.5f) * 10f * progress(1.0f, CLIMAX_DEATH);
        }

        @Override
        protected void front(Batch batch) {
            if (time < CLIMAX_DEATH) {
                demon(batch, 1f);
                float x0 = portraitX(BODY_LEFT), x1 = portraitX(BODY_RIGHT);
                glow(batch, (x0 + x1) / 2f, bossY(), (x1 - x0) * 1.2f, hex("ff3a0a"), 0.3f * progress(1.0f, CLIMAX_DEATH));
                glow(batch, (x0 + x1) / 2f, bossY(), (x1 - x0) * 0.8f, GOLD, 0.5f * progress(2.0f, CLIMAX_DEATH));
            } else {                                                    // ses machines aux ailes d'ange s'envolent, libres
                glow(batch, bossX(), bossY(), worldWidth() * 0.8f, GOLD, 0.5f * (1f - progress(CLIMAX_DEATH, CLIMAX_DEATH + 1.5f)));
                float scale = bossWidth() / 50f, k = Interpolation.pow2In.apply(progress(CLIMAX_DEATH + 0.3f, 5.4f));
                for (int i = 0; i < 3; i++) {
                    float x = bossX() + (i - 1) * bossWidth() * (0.35f + 0.9f * k) + 20f * MathUtils.sin(time * 3f + i);
                    float y = bossY() + (i == 1 ? 60f : 0f) + worldHeight() * 0.75f * k;
                    flyer(batch, x, y, scale, i, progress(CLIMAX_DEATH + 0.3f, CLIMAX_DEATH + 0.7f));
                }
            }
            wave(batch, CLIMAX_DEATH, 1.0f, bossX(), bossY(), GOLD, worldWidth() * 1.6f);
            wave(batch, CLIMAX_DEATH + 0.15f, 1.0f, bossX(), bossY(), WHITE, worldWidth() * 1.2f);
            flash(batch, hex("fff4d0"), CLIMAX_DEATH, 0.5f, 0.75f);
        }
    }
}
