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
            case ECLAT -> new ShardDeath(kit);
            case PRETENDANT -> new PretenderDeath(kit);
            case MAISON -> new HouseDeath(kit);
            default -> null;
        };
    }

    // ----- Accessoires -----

    /** Une carte blanche au Trèfle noir. */
    static final String[] CLUB_CARD = {
        "oooooooooooo",
        "owwwwwwwwwwo",
        "owwwwkkwwwwo",
        "owwwkkkkwwwo",
        "owwwkkkkwwwo",
        "owkkwkkwkkwo",
        "okkkkkkkkkko",
        "okkkkkkkkkko",
        "owkkwkkwkkwo",
        "owwwwkkwwwwo",
        "owwwwkkwwwwo",
        "owwwkkkkwwwo",
        "owwwwwwwwwwo",
        "owwwwwwwwwwo",
        "oooooooooooo"};
    static final String[] CARD_BACK = {
        "oooooooo",
        "orrrrrro",
        "orwrrwro",
        "orrwwrro",
        "orrwwrro",
        "orwrrwro",
        "orrrrrro",
        "oooooooo"};
    static final String[] CROWN = {
        "g..g..g",
        "gg.g.gg",
        "ggggggg",
        "grgbgrg",
        "ggggggg"};

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
            card = art("owk", new Color[] {c("1a0a10"), c("f4ecdc"), c("14141c")}, CLUB_CARD);
            back = art("orw", new Color[] {c("1a0a10"), c("8a1424"), c("e8c860")}, CARD_BACK);
        }

        @Override
        protected void step(float delta) {
            if (at(FLIP)) {                                             // les cartes volent
                rumble(0.3f, 6f);
                spray(back, bossX(), groundY() + 60f, 26, 30f, 150f, 1300f, fit(70f, 8), 1600f, 1.6f);
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
            float scale = fit(worldHeight() * 0.34f, 15);
            float split = Interpolation.swingOut.apply(progress(2.5f, 3.1f));
            float y = time < CLIMAX_PHASE ? cardY() : groundY() + 120f + 60f;
            float spread = worldWidth() * 0.16f * split;
            draw(batch, card, bossX() - spread, y, scale, -4f * split, Color.WHITE, 1f);
            if (split > 0f) draw(batch, card, bossX() + spread, y, scale, 4f * split, Color.WHITE, split);
            wave(batch, CLIMAX_PHASE, 0.8f, bossX(), y, GOLD, worldWidth() * 1.4f);
            flash(batch, GOLD, CLIMAX_PHASE, 0.35f, 0.6f);
        }
    }

    /** Sa couronne roule comme une bille de roulette et s'arrête sur le zéro ; son château de cartes s'écroule. */
    static final class QueenDeath extends BossScene {
        static final float FALL = 0.5f;
        private final TextureRegion crown, back, wheel;
        private boolean collapsed;

        QueenDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.REINE, 0);
            crown = art("grb", new Color[] {c("ffc93a"), c("c8102e"), c("3a7aff")}, CROWN);
            back = art("orw", new Color[] {c("1a0a10"), c("8a1424"), c("e8c860")}, CARD_BACK);
            wheel = symbol(EnemySymbol.ZERO);
        }

        @Override protected void restart() { collapsed = false; }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_DEATH)) {                                     // la bille s'arrête sur le zéro
                rumble(0.5f, 9f);
                sparks(wheelX(), wheelY() + wheelR(), 60, GOLD, 700f, 20f);
            }
            if (!collapsed && time >= CLIMAX_DEATH + 0.2f) {            // le château de cartes s'écroule
                collapsed = true;
                for (int row = 0; row < 5; row++) {
                    for (int i = 0; i <= row; i++) {
                        float[] p = castle(row, i);
                        Particle card = place(back, p[0], p[1], (random.nextFloat() - 0.5f) * 500f, 200f * random.nextFloat(),
                            fit(64f, 8), 2.2f);
                        card.gravity = 1500f;
                        card.spin = (random.nextFloat() - 0.5f) * 600f;
                    }
                }
            }
        }

        private float wheelX() { return worldWidth() * 0.74f; }
        private float wheelY() { return groundY() + worldHeight() * 0.2f; }
        private float wheelR() { return worldHeight() * 0.13f; }

        /** @return la place d'une carte du château (rangée {@code row} depuis le haut), derrière la Reine. */
        private float[] castle(int row, int i) {
            float x = worldWidth() * 0.24f + (i - row / 2f) * 70f, y = groundY() + 40f + (4 - row) * 74f;
            return new float[] {x, y};
        }

        @Override
        protected void back(Batch batch) {
            if (!collapsed) {
                for (int row = 0; row < 5; row++) {
                    for (int i = 0; i <= row; i++) {
                        float[] p = castle(row, i);
                        float wobble = time > 1.8f ? MathUtils.sin(time * 20f + row) * 3f * progress(1.8f, CLIMAX_DEATH) : 0f;
                        draw(batch, back, p[0] + wobble, p[1], fit(64f, 8), wobble, Color.WHITE, 1f);
                    }
                }
            }
            float spin = time * 120f;
            draw(batch, wheel, wheelX(), wheelY(), fit(wheelR() * 2f, 16), time < CLIMAX_DEATH ? spin : CLIMAX_DEATH * 120f,
                Color.WHITE, 1f);
            poseShadow = 0.5f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 1.0f);
            poseAlpha = 1f - 0.6f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 1.5f);
            poseY = -30f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 1.5f);
        }

        @Override
        protected void front(Batch batch) {
            float scale = fit(42f, 5);
            float x, y, rot;
            if (time < FALL + 0.5f) {                                   // elle tombe de sa tête
                float k = progress(FALL, FALL + 0.5f);
                x = bossX() + 60f * k;
                y = MathUtils.lerp(bossBottom() + bossHeight() * 0.95f, groundY() + 30f, Interpolation.bounceOut.apply(k));
                rot = -40f * k;
            } else if (time < 1.4f) {                                   // elle roule sur la table
                float k = progress(FALL + 0.5f, 1.4f);
                x = MathUtils.lerp(bossX() + 60f, wheelX() - wheelR(), k);
                y = groundY() + 30f;
                rot = -40f - 600f * k;
            } else {                                                    // elle tourne autour de la roue et s'arrête en haut
                float k = Interpolation.pow2Out.apply(progress(1.4f, CLIMAX_DEATH));
                float angle = 180f - (180f + 360f + 270f) * k;          // de la gauche jusqu'en haut, en un tour et demi
                x = wheelX() + MathUtils.cosDeg(angle) * wheelR() * 1.05f;
                y = wheelY() + MathUtils.sinDeg(angle) * wheelR() * 1.05f;
                rot = angle * 2f;
            }
            draw(batch, crown, x, y, scale, rot, Color.WHITE, 1f);
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

    /** Le temps se fige : il éclate au ralenti, les morceaux restent suspendus ; le sablier se vide, tout tombe. */
    static final class ShardDeath extends BossScene {
        static final float DROP = 4.3f;
        private static final int SHARD = 4;
        private final Array<float[]> pieces = new Array<>();        // x0, y0, dx, dy, spin, row, col
        private final TextureRegion hourglass;
        private final Array<TextureRegion> regions = new Array<>();
        private boolean dropped;

        ShardDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.ECLAT, 0);
            hourglass = symbol(EnemySymbol.HOURGLASS);
        }

        @Override protected void restart() { pieces.clear(); regions.clear(); dropped = false; }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_DEATH)) {
                rumble(0.4f, 8f);
                float scale = bossScale(), left = bossX() - bossWidth() / 2f;
                int width = boss.getRegionWidth(), height = boss.getRegionHeight();
                for (int py = 0; py < height; py += SHARD) {
                    for (int px = 0; px < width; px += SHARD) {
                        int w = Math.min(SHARD, width - px), h = Math.min(SHARD, height - py);
                        float x = left + (px + w / 2f) * scale, y = bossBottom() + (height - py - h / 2f) * scale;
                        float angle = MathUtils.atan2(y - bossY(), x - bossX());
                        float dist = 120f + 260f * random.nextFloat();
                        pieces.add(new float[] {x, y, MathUtils.cos(angle) * dist, MathUtils.sin(angle) * dist,
                            (random.nextFloat() - 0.5f) * 90f});
                        regions.add(new TextureRegion(boss, px, py, w, h));
                    }
                }
            }
            if (!dropped && time >= DROP) {                             // le dernier grain : tout tombe d'un coup
                dropped = true;
                rumble(0.5f, 10f);
                for (int i = 0; i < pieces.size; i++) {
                    float[] p = pieces.get(i);
                    Particle piece = place(regions.get(i), p[0] + p[2], p[1] + p[3], 0f, 0f, bossScale(), 1.4f);
                    piece.gravity = 2600f;
                    piece.rotation = p[4];
                    piece.spin = (random.nextFloat() - 0.5f) * 400f;
                }
                pieces.clear();
            }
        }

        @Override
        protected void back(Batch batch) {
            showBoss = time < CLIMAX_DEATH;
            float k = progress(0.3f, CLIMAX_DEATH);
            poseX = (random.nextFloat() - 0.5f) * 16f * k;
            poseTintColor.set(WHITE);
            poseTint = 0.6f * k * k;
            // le temps figé : le décor se décolore en bleu froid
            float frozen = progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.4f) * (1f - progress(DROP, DROP + 0.3f));
            fill(batch, c("2a4a8a"), 0.35f * frozen);
        }

        @Override
        protected void front(Batch batch) {
            float burst = Interpolation.pow3Out.apply(progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.35f));
            float hover = MathUtils.sin(time * 1.5f) * 4f;
            for (int i = 0; i < pieces.size; i++) {                     // suspendus, ils dérivent à peine
                float[] p = pieces.get(i);
                float x = p[0] + p[2] * burst, y = p[1] + p[3] * burst + hover;
                draw(batch, regions.get(i), x, y, bossScale(), p[4] * burst, WHITE, 1f);
            }
            if (time >= CLIMAX_DEATH) {                                 // le sablier qui se vide
                float x = worldWidth() * 0.82f, y = worldHeight() * 0.62f, s = iconScale(0.22f);
                float show = progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.3f);
                draw(batch, hourglass, x, y, s, 0f, WHITE, show);
                float sand = 1f - progress(CLIMAX_DEATH + 0.3f, DROP);
                rect(batch, c("ffd060"), show, x - 3f, y - s * 6f, 6f, s * 6f * (sand > 0f ? 1f : 0f));
            }
            flash(batch, WHITE, CLIMAX_DEATH, 0.3f, 0.7f);
            flash(batch, c("a0c8ff"), DROP, 0.25f, 0.4f);
        }
    }

    // -------------------------------------------------------------------------
    // Le Prétendant
    // -------------------------------------------------------------------------

    /** Phase 2 : un rayon arrache l'éclat de l'Éclat Originel de sa couronne ; les chaînes du Rouleau interdit cassent. */
    static final class PretenderShard extends BossScene {
        private static final Color CYAN = c("7ae8ff"), CHAIN = c("8a8aa0");
        private final TextureRegion gem, link;

        PretenderShard(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.PRETENDANT, 2);
            gem = art("o#w", new Color[] {c("0a2a3a"), c("7ae8ff"), c("ffffff")},
                "..o..", ".o#o.", "o#w#o", ".o#o.", "..o..");
            link = art("o", new Color[] {c("8a8aa0")}, "oooo", "o..o", "oooo");
        }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_PHASE)) {
                rumble(0.6f, 12f);
                sparks(gemX(), crownY(), 70, CYAN, 900f, 22f);
                for (int side = -1; side <= 1; side += 2) {             // les chaînes cassent
                    spray(link, bossX() + side * bossWidth() * 0.75f, bossY(), 14, side < 0 ? 100f : -80f,
                        side < 0 ? 260f : 80f, 700f, fit(28f, 3), 1600f, 1.6f);
                }
            }
        }

        private float gemX()   { return bossX(); }
        private float crownY() { return bossBottom() + bossHeight() * 0.9f; }

        @Override
        protected void back(Batch batch) {
            float beam = progress(0.7f, 1.2f) * (1f - progress(CLIMAX_PHASE + 0.4f, CLIMAX_PHASE + 0.8f));
            additive(batch);
            batch.setColor(CYAN.r, CYAN.g, CYAN.b, 0.35f * beam);
            batch.draw(soft, gemX() - 90f, crownY() - 40f, 180f, worldHeight());
            normal(batch);
            if (time < CLIMAX_PHASE) {                                  // les chaînes tendues de chaque côté
                for (int side = -1; side <= 1; side += 2) {
                    float x = bossX() + side * bossWidth() * 0.75f;
                    for (int i = 0; i < 8; i++) {
                        float shake = (random.nextFloat() - 0.5f) * 6f * progress(1.2f, CLIMAX_PHASE);
                        draw(batch, link, x + shake, groundY() + 40f + i * 60f, fit(28f, 3), 90f, CHAIN, 1f);
                    }
                }
            }
            poseY = 10f * gesture(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.4f);
        }

        @Override
        protected void front(Batch batch) {
            float pull = Interpolation.pow3In.apply(progress(CLIMAX_PHASE, CLIMAX_PHASE + 1.0f));
            float y = MathUtils.lerp(crownY(), worldHeight() + 100f, pull) + (time < CLIMAX_PHASE
                ? 6f * MathUtils.sin(time * 30f) * progress(1.2f, CLIMAX_PHASE) : 0f);
            glow(batch, gemX(), y, 120f, CYAN, 0.8f);
            draw(batch, gem, gemX(), y, fit(46f, 5), time * 60f, WHITE, 1f);
            flash(batch, CYAN, CLIMAX_PHASE, 0.3f, 0.5f);
        }
    }

    /** Phase 3 : le fantôme de la Reine apparaît derrière lui et reprend son éclat ; sa couronne glisse. */
    static final class PretenderQueen extends BossScene {
        private static final Color RUBY = c("ff3a5a");
        private final TextureRegion queen, gem;

        PretenderQueen(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.PRETENDANT, 3);
            queen = kit.portrait(EnemyKind.REINE);
            gem = art("o#w", new Color[] {c("3a0a14"), RUBY, c("ffffff")},
                "..o..", ".o#o.", "o#w#o", ".o#o.", "..o..");
        }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_PHASE)) { rumble(0.5f, 10f); sparks(bossX(), crownY(), 60, RUBY, 800f, 22f); }
        }

        private float crownY() { return bossBottom() + bossHeight() * 0.9f; }
        private float ghostX() { return bossX() + bossWidth() * 0.55f; }

        @Override
        protected void back(Batch batch) {
            float ghost = progress(0.4f, 1.4f) * (1f - progress(3.4f, 4.1f));
            float scale = bossScale() * 1.15f;
            float w = queen.getRegionWidth() * scale, h = queen.getRegionHeight() * scale;
            float bob = MathUtils.sin(time * 2f) * 10f;
            additive(batch);
            batch.setColor(0.75f, 0.85f, 1f, 0.45f * ghost);
            batch.draw(queen, ghostX() - w / 2f, bossBottom() + 40f + bob, w, h);
            normal(batch);
            float slip = Interpolation.swingOut.apply(progress(CLIMAX_PHASE + 0.2f, CLIMAX_PHASE + 0.7f));
            poseTilt = -7f * slip;                                     // sa couronne glisse : il penche
            poseX = -20f * slip;
            poseShadow = 0.15f * slip;
        }

        @Override
        protected void front(Batch batch) {
            float k = Interpolation.pow2InInverse.apply(progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.7f));
            float x = MathUtils.lerp(bossX(), ghostX() - 60f, k), y = MathUtils.lerp(crownY(), worldHeight() * 0.72f, k)
                + 60f * MathUtils.sin(k * MathUtils.PI);
            float alpha = 1f - progress(3.2f, 4.0f);
            glow(batch, x, y, 110f, RUBY, 0.7f * alpha);
            draw(batch, gem, x, y, fit(44f, 5), 0f, WHITE, alpha);
            flash(batch, RUBY, CLIMAX_PHASE, 0.3f, 0.4f);
        }
    }

    /** Assis sur un trône de jetons qui s'effondre jeton par jeton ; le vent emporte sa couronne en carton. */
    static final class PretenderDeath extends BossScene {
        static final float BLOW = 3.4f;
        private final TextureRegion chip, crown;
        private int fallen;
        private static final int CHIPS = 28;

        PretenderDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.PRETENDANT, 0);
            chip = load("hud/chip_enemy.png");
            crown = art("grb", new Color[] {c("b8925a"), c("8a6a3a"), c("d8b880")}, CROWN);
        }

        @Override protected void restart() { fallen = 0; }

        /** @return la place du jeton {@code i} du trône (un dossier haut et deux accoudoirs). */
        private float[] chipAt(int i) {
            float size = 56f, x0 = bossX(), base = groundY() + 10f;
            if (i < 12) return new float[] {x0 - 2.5f * size + (i % 6) * size, base + (i / 6) * size * 0.6f};          // l'assise
            if (i < 22) return new float[] {x0 - 2.5f * size + (i % 2 == 0 ? 0 : 5) * size, base + (2 + (i - 12) / 2) * size * 0.6f}; // accoudoirs
            return new float[] {x0 - 1.5f * size + (i - 22) % 4 * size, base + (7 + (i - 22) / 4) * size * 0.6f};     // le dossier
        }

        @Override
        protected void step(float delta) {
            if (time >= CLIMAX_DEATH) {                                 // il s'effondre jeton par jeton, du haut vers le bas
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
            if (at(CLIMAX_DEATH)) rumble(0.8f, 12f);
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
            poseShadow = 0.4f * drop;
        }

        @Override
        protected void front(Batch batch) {
            if (time < BLOW - 0.4f) return;
            float k = progress(BLOW, BLOW + 1.6f);                      // la couronne en carton s'envole avec le vent
            float x = bossX() + worldWidth() * 0.7f * Interpolation.pow2In.apply(k);
            float y = bossBottom() + poseY + bossHeight() * 0.95f + 240f * k + 40f * MathUtils.sin(k * 12f);
            draw(batch, crown, x, y, fit(48f, 5), -30f * k * 10f, WHITE, 1f);
            for (int i = 0; i < 12; i++) {                              // le vent
                float wx = ((time * 1400f + i * 263f) % (worldWidth() + 400f)) - 200f;
                line(batch, WHITE, 0.15f * progress(BLOW - 0.4f, BLOW), wx, worldHeight() * (0.3f + 0.05f * i), wx + 160f,
                    worldHeight() * (0.3f + 0.05f * i), 3f);
            }
        }
    }

    // -------------------------------------------------------------------------
    // La Maison
    // -------------------------------------------------------------------------

    /** Phase 2 : sa façade bascule dans un nuage de poussière et dévoile la salle de jeu ; l'enseigne clignote. */
    static final class HouseFacade extends BossScene {
        private static final Color WALL = c("7a2a2a"), WINDOW = c("ffd060"), NEON = c("ff4fa0");

        HouseFacade(CutsceneKit kit) { super(kit, Moment.PHASE, EnemyKind.MAISON, 2); }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_PHASE + 0.35f)) {                             // la façade touche le sol
                rumble(0.8f, 16f);
                for (int i = 0; i < 40; i++) {
                    Particle dust = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth() * 1.6f, groundY() + 20f,
                        60f + 60f * random.nextFloat(), 200f + 300f * random.nextFloat(), 1.6f, 120f + 120f * random.nextFloat());
                    dust.color.set(0.6f, 0.55f, 0.5f, 0.6f);
                    dust.drag = 0.3f;
                    glows.add(dust);
                }
            }
        }

        @Override
        protected void back(Batch batch) {
            float flicker = time > CLIMAX_PHASE + 0.6f && MathUtils.sin(time * 37f) > -0.2f ? 1f : 0.15f;
            float y = bossBottom() + bossHeight() + 30f;
            glow(batch, bossX(), y + 20f, bossWidth() * 1.4f, NEON, 0.5f * flicker);
            rect(batch, NEON, flicker, bossX() - bossWidth() * 0.4f, y, bossWidth() * 0.8f, 18f);
        }

        @Override
        protected void front(Batch batch) {
            // la façade : un mur qui tremble, puis bascule vers l'avant (elle s'écrase vers le bas)
            float fall = Interpolation.pow2In.apply(progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.35f));
            if (fall >= 1f) return;
            float shake = (random.nextFloat() - 0.5f) * 10f * progress(0.6f, CLIMAX_PHASE);
            float w = bossWidth() * 1.05f, h = bossHeight() * (1f - fall);
            float x = bossX() - w / 2f + shake, y = bossBottom();
            rect(batch, WALL, 1f, x, y, w, h);
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 4; col++) {
                    rect(batch, WINDOW, 0.85f, x + w * (0.1f + col * 0.22f), y + h * (0.12f + row * 0.22f), w * 0.12f, h * 0.12f);
                }
            }
            float cracks = progress(0.8f, CLIMAX_PHASE);
            for (int i = 0; i < 5; i++) {
                line(batch, c("1a0808"), cracks, x + w * (0.2f + 0.15f * i), y + h, x + w * (0.25f + 0.13f * i), y + h * (1f - 0.6f * cracks), 4f);
            }
        }
    }

    /** Phase 3 : la porte du coffre saute ; les pièces se déversent, les fioles de soin se brisent au sol. */
    static final class HouseVault extends BossScene {
        private final TextureRegion safe, potion;
        private static final Color DOOR = c("8a8a98");

        HouseVault(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.MAISON, 3);
            safe = symbol(EnemySymbol.SAFE);
            potion = symbol(EnemySymbol.POTION);
        }

        private float safeX() { return bossX(); }
        private float safeY() { return groundY() + worldHeight() * 0.16f; }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_PHASE)) {
                rumble(0.8f, 16f);
                sparks(safeX(), safeY(), 80, GOLD, 900f, 24f);
                spray(coin, safeX(), safeY(), 70, 20f, 160f, 1400f, coinScale(0.04f), 1500f, 2.2f);
                spray(potion, safeX(), safeY(), 8, 50f, 130f, 900f, iconScale(0.06f), 1700f, 0.9f);
            }
            // les fioles qui touchent le sol se brisent : une flaque rouge
            for (Particle piece : sprites) {
                if (piece.region == potion && piece.y < groundY() + 20f && piece.age < piece.life) {
                    piece.age = piece.life;
                    sparks(piece.x, groundY() + 20f, 14, c("ff3a5a"), 400f, 16f);
                }
            }
        }

        @Override
        protected void back(Batch batch) { poseShadow = 0.35f; }

        @Override
        protected void front(Batch batch) {
            float s = iconScale(0.3f), shake = time < CLIMAX_PHASE ? (random.nextFloat() - 0.5f) * 14f * progress(0.5f, CLIMAX_PHASE) : 0f;
            draw(batch, safe, safeX() + shake, safeY(), s, 0f, WHITE, 1f);
            if (time < CLIMAX_PHASE) {
                rect(batch, DOOR, 1f, safeX() - s * 6f + shake, safeY() - s * 6f, s * 12f, s * 12f);
            } else {
                float k = progress(CLIMAX_PHASE, CLIMAX_PHASE + 1.2f);   // la porte s'envole en tournant
                float x = safeX() + 900f * k, y = safeY() + 500f * k - 900f * k * k;
                batch.setColor(DOOR);
                batch.draw(pixel, x - s * 6f, y - s * 6f, s * 6f, s * 6f, s * 12f, s * 12f, 1f, 1f, -400f * k);
            }
            wave(batch, CLIMAX_PHASE, 0.8f, safeX(), safeY(), GOLD, worldWidth() * 1.4f);
            flash(batch, GOLD, CLIMAX_PHASE, 0.3f, 0.6f);
        }
    }

    /** Les lumières s'éteignent étage par étage, la grande enseigne tombe, le rideau de fer descend. */
    static final class HouseDeath extends BossScene {
        static final float SHUTTER = 3.0f;
        private static final Color LIT = c("ffd060"), NEON = c("ff4fa0"), IRON = c("5a5a66");

        HouseDeath(CutsceneKit kit) { super(kit, Moment.DEATH, EnemyKind.MAISON, 0); }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_DEATH + 0.3f)) {                              // l'enseigne s'écrase
                rumble(0.6f, 12f);
                sparks(bossX() + 140f, groundY() + 30f, 90, NEON, 900f, 22f);
            }
            if (at(SHUTTER + 1.0f)) rumble(0.2f, 5f);
        }

        @Override
        protected void back(Batch batch) {
            poseShadow = 0.55f * progress(0.4f, CLIMAX_DEATH);
            if (time < CLIMAX_DEATH) {                                  // l'enseigne, en haut
                float flicker = time > 1.6f && MathUtils.sin(time * 41f) < 0.3f ? 0.2f : 1f;
                float y = bossBottom() + bossHeight() + 30f;
                glow(batch, bossX(), y + 20f, bossWidth() * 1.4f, NEON, 0.5f * flicker);
                rect(batch, NEON, flicker, bossX() - bossWidth() * 0.4f, y, bossWidth() * 0.8f, 18f);
            }
        }

        @Override
        protected void front(Batch batch) {
            float w = bossWidth(), h = bossHeight(), x0 = bossX() - w / 2f, y0 = bossBottom();
            for (int row = 0; row < 4; row++) {                         // les fenêtres, éteintes du haut vers le bas
                float off = progress(0.5f + (3 - row) * 0.4f, 0.7f + (3 - row) * 0.4f);
                for (int col = 0; col < 3; col++) {
                    float x = x0 + w * (0.18f + col * 0.26f), y = y0 + h * (0.12f + row * 0.18f);
                    additive(batch);
                    batch.setColor(LIT.r, LIT.g, LIT.b, 0.55f * (1f - off));
                    batch.draw(soft, x - 20f, y - 20f, w * 0.12f + 40f, h * 0.1f + 40f);
                    normal(batch);
                }
            }
            if (time >= CLIMAX_DEATH) {                                 // l'enseigne tombe en pivotant
                float k = Interpolation.pow2In.apply(progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.3f));
                float y = MathUtils.lerp(y0 + h + 30f, groundY() + 20f, k);
                batch.setColor(NEON.r * 0.5f, NEON.g * 0.5f, NEON.b * 0.5f, 1f);
                batch.draw(pixel, bossX() - w * 0.4f + 140f * k, y, w * 0.4f, 9f, w * 0.8f, 18f, 1f, 1f, -25f * k);
            }
            float shutter = Interpolation.pow2Out.apply(progress(SHUTTER, SHUTTER + 1.0f));   // le rideau de fer
            if (shutter > 0f) {
                float sh = h * shutter;
                rect(batch, IRON, 1f, x0 - 10f, y0 + h - sh, w + 20f, sh);
                for (float y = y0 + h - sh + 14f; y < y0 + h; y += 28f) rect(batch, c("3a3a44"), 1f, x0 - 10f, y, w + 20f, 5f);
            }
            flash(batch, NEON, CLIMAX_DEATH + 0.3f, 0.3f, 0.4f);
        }
    }
}
