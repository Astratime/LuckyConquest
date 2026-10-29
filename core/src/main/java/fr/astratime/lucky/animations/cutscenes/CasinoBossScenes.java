package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;

import static fr.astratime.lucky.assets.Palette.hex;

/** Les scènes de phase et de mort des quatre rois du Casino Englouti (choix d'Astra, 2026-10-24). */
final class CasinoBossScenes {

    private CasinoBossScenes() {}

    static BossScene phase(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case SIRENE -> new SirenPhase(kit);
            case JACKPOT_VIVANT -> new JackpotPhase(kit);
            case REQUIN_BANQUIER -> new SharkPhase(kit);
            case KRAKEN -> new KrakenPhase(kit);
            default -> null;
        };
    }

    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case SIRENE -> new SirenDeath(kit);
            case JACKPOT_VIVANT -> new JackpotDeath(kit);
            case REQUIN_BANQUIER -> new SharkDeath(kit);
            case KRAKEN -> new KrakenDeath(kit);
            default -> null;
        };
    }

    // ----- Accessoires -----

    static final String[] NOTE = {"..www", "..w.w", "..w.w", "..w..", "www..", "www.."};
    static final String[] GLASS = {"owwwwwwwo", ".oppppyo.", "..oppppo.", "...oppo..", "....oo...", "....wo...", "....wo...",
        "...owwo..", "..owwwwo."};
    static final String[] FIN = {"....s", "...ss", "..sss", ".ssss", "sssss"};


    /** Le boss dessiné en bandes qui ondulent comme sous l'eau ({@code amount} : la force de l'onde). */
    static void ripple(BossScene scene, Batch batch, float amount, float alpha, Color tint) {
        int bands = scene.boss.getRegionHeight();
        float scale = scene.bossScale(), width = scene.boss.getRegionWidth() * scale;
        batch.setColor(tint.r, tint.g, tint.b, alpha);
        for (int row = 0; row < bands; row++) {
            TextureRegion band = new TextureRegion(scene.boss, 0, row, scene.boss.getRegionWidth(), 1);
            float shift = MathUtils.sin(scene.time * 9f + row * 0.35f) * amount;
            batch.draw(band, scene.bossX() - width / 2f + shift, scene.bossBottom() + (bands - row - 1) * scale, width, scale + 0.5f);
        }
    }

    /** Un tentacule : une chaîne de ventouses qui part de ({@code x0}, {@code y0}) vers ({@code x1}, {@code y1}) en ondulant. */
    static void tentacle(BossScene scene, Batch batch, float x0, float y0, float x1, float y1, float thick, float wave, float phase,
                         Color color) {
        int segments = 22;
        for (int i = 0; i <= segments; i++) {
            float k = i / (float) segments;
            float dx = x1 - x0, dy = y1 - y0, len = (float) Math.sqrt(dx * dx + dy * dy);
            float nx = -dy / Math.max(1f, len), ny = dx / Math.max(1f, len);
            float off = MathUtils.sin(k * 7f + scene.time * 4f + phase) * wave * k;
            float x = x0 + dx * k + nx * off, y = y0 + dy * k + ny * off;
            float size = thick * (1f - 0.75f * k);
            batch.setColor(color);
            batch.draw(scene.soft, x - size / 2f, y - size / 2f, size, size);
            if (i % 3 == 1) {
                batch.setColor(color.r + 0.3f, color.g + 0.2f, color.b + 0.3f, 1f);
                batch.draw(scene.soft, x - size * 0.2f, y - size * 0.2f, size * 0.4f, size * 0.4f);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Sirène
    // -------------------------------------------------------------------------

    /** Ses notes deviennent des spirales, l'image ondule, ses yeux s'allument. */
    static final class SirenPhase extends BossScene {
        private static final Color PINK = hex("ff7ad8");
        private final TextureRegion note;

        SirenPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.SIRENE, 2);
            note = art("w", new Color[] {Color.WHITE}, NOTE);
        }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_PHASE)) { rumble(0.5f, 8f); sparks(bossX(), bossY(), 70, PINK, 900f, 20f); }
        }

        @Override
        protected void back(Batch batch) {
            bubbles(batch, 30, 0.4f);
            // les notes s'enroulent en spirales autour d'elle
            float spin = time * MathUtils.lerp(60f, 360f, progress(0f, CLIMAX_PHASE));
            for (int arm = 0; arm < 3; arm++) {
                for (int i = 0; i < 9; i++) {
                    float k = ((time * 0.4f + i / 9f) % 1f);
                    float angle = arm * 120f + spin + k * 540f, r = worldHeight() * (0.08f + 0.45f * k);
                    float x = bossX() + MathUtils.cosDeg(angle) * r * 1.3f, y = bossY() + MathUtils.sinDeg(angle) * r;
                    float alpha = progress(0.2f, 0.8f) * (1f - k);
                    draw(batch, note, x, y, fit(46f, 6), 0f, tmp.set(WHITE).lerp(PINK, k), alpha);
                }
            }
            float waves = progress(0.8f, CLIMAX_PHASE);
            showBoss = waves <= 0f;
            if (!showBoss) ripple(this, batch, 18f * waves, 1f, WHITE);
        }

        @Override
        protected void front(Batch batch) {
            eyes(batch, progress(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.2f), PINK, new float[] {14f, 16f}, new float[] {21f, 16f});
            wave(batch, CLIMAX_PHASE, 0.8f, bossX(), bossY(), PINK, worldWidth() * 1.3f);
            flash(batch, PINK, CLIMAX_PHASE, 0.3f, 0.4f);
        }
    }

    /** Elle chante une dernière fois : le verre se brise en morceaux ; elle se dissout en écume et en bulles. */
    static final class SirenDeath extends BossScene {
        private final TextureRegion note, glass;

        SirenDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.SIRENE, 0);
            note = art("w", new Color[] {Color.WHITE}, NOTE);
            glass = art("owpy", new Color[] {hex("1a0a1a"), hex("e8f0ff"), hex("ff7ad8"), hex("ffd860")}, GLASS);
        }

        private float glassX() { return bossX() + bossWidth() * 0.75f; }
        private float glassY() { return groundY() + 90f; }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_DEATH)) {                                     // sa dernière note brise le verre
                rumble(0.4f, 7f);
                sparks(glassX(), glassY(), 30, WHITE, 600f, 14f);
                int w = glass.getRegionWidth(), h = glass.getRegionHeight();
                for (int py = 0; py < h; py += 2) {
                    for (int px = 0; px < w; px += 2) {
                        Particle shard = place(new TextureRegion(glass, px, py, Math.min(2, w - px), Math.min(2, h - py)),
                            glassX() + (px - w / 2f) * fit(120f, 9), glassY() + (h / 2f - py) * fit(120f, 9),
                            (random.nextFloat() - 0.5f) * 700f, 200f + 500f * random.nextFloat(), fit(120f, 9), 1.6f);
                        shard.gravity = 1600f;
                        shard.spin = (random.nextFloat() - 0.5f) * 720f;
                    }
                }
            }
            if (time > CLIMAX_DEATH && time < 4.4f) {                   // elle se défait en écume et en bulles
                float level = progress(CLIMAX_DEATH, 4.2f);
                for (int i = 0; i < 4; i++) {
                    Particle bubble = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth(),
                        bossBottom() + bossHeight() * level + (random.nextFloat() - 0.5f) * 40f, 90f + (random.nextFloat() - 0.5f) * 40f,
                        120f + 200f * random.nextFloat(), 1.6f, 14f + 20f * random.nextFloat());
                    bubble.color.set(0.85f, 0.95f, 1f, 0.8f);
                    glows.add(bubble);
                }
            }
        }

        @Override
        protected void back(Batch batch) {
            bubbles(batch, 24, 0.35f);
            // elle s'efface de bas en haut : le bas du portrait disparaît en premier
            float melt = progress(CLIMAX_DEATH, 4.2f);
            int rows = boss.getRegionHeight(), keep = Math.round(rows * (1f - melt));
            showBoss = false;
            if (keep <= 0) return;
            float scale = bossScale(), width = boss.getRegionWidth() * scale;
            batch.setColor(1f, 1f, 1f, 1f - 0.3f * melt);
            TextureRegion top = new TextureRegion(boss, 0, 0, boss.getRegionWidth(), keep);
            float sing = 6f * MathUtils.sin(time * 5f) * (1f - melt);   // elle se balance en chantant
            batch.draw(top, bossX() - width / 2f + sing, bossBottom() + (rows - keep) * scale, width, keep * scale);
        }

        @Override
        protected void front(Batch batch) {
            // son dernier chant : des notes de plus en plus grandes, qui filent vers le verre
            for (int i = 0; i < 6; i++) {
                float k = ((time * 0.6f + i / 6f) % 1f);
                float x = MathUtils.lerp(bossX() + bossWidth() * 0.1f, glassX(), k);
                float y = bossY() + bossHeight() * 0.15f + MathUtils.sin(k * MathUtils.PI) * 160f;
                float grow = 1f + 0.8f * progress(0.8f, CLIMAX_DEATH);
                draw(batch, note, x, y, fit(46f * grow, 6), 10f * MathUtils.sin(time * 6f + i), tmp.set(WHITE).lerp(hex("ff7ad8"), k),
                    (1f - k * 0.5f) * (1f - progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.3f)));
            }
            if (time < CLIMAX_DEATH) {                                  // le verre vibre, puis se brise
                float buzz = (random.nextFloat() - 0.5f) * 8f * progress(1.4f, CLIMAX_DEATH);
                draw(batch, glass, glassX() + buzz, glassY(), fit(120f, 9), 0f, WHITE, 1f);
            }
            fill(batch, hex("e0f0ff"), 0.15f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 1f));
        }
    }

    // -------------------------------------------------------------------------
    // Jackpot Vivant
    // -------------------------------------------------------------------------

    /** Son levier se tire tout seul : les rouleaux s'affolent, les lumières clignotent, il crache des pièces. */
    static final class JackpotPhase extends BossScene {
        private final TextureRegion[] symbols;
        private static final Color[] BULBS = {hex("ff3a5a"), hex("ffd23a"), hex("3aff8a"), hex("3ab0ff"), hex("ff7ad8")};
        /** Le levier dans son portrait de poisson : planté en (25, 14), boule en (25, 4.5). */
        private static final float LEVER_X = 25f, LEVER_BASE = 14f, LEVER_LENGTH = 9.5f;
        /** Les morceaux de son portrait autour du levier : {x, y, largeur, hauteur} en pixels, depuis le haut. */
        private static final int[][] BODY_PARTS = {{0, 0, 21, 13}, {29, 0, 5, 13}, {0, 13, 34, 29}};

        JackpotPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.JACKPOT_VIVANT, 2);
            symbols = new TextureRegion[] {load("symbols/2-cherry.png"), load("symbols/3-seven.png"), load("symbols/4-bar.png"),
                load("symbols/6-bell.png"), load("symbols/7-diamond.png"), load("symbols/11-watermelon.png")};
        }

        @Override
        protected void step(float delta) {
            if (at(0.7f)) rumble(0.2f, 6f);
            if (at(CLIMAX_PHASE)) rumble(0.8f, 14f);
            if (time > CLIMAX_PHASE && time < 3.8f && random.nextFloat() < 0.6f) {   // il crache des pièces
                spray(coin, portraitX(3f), portraitY(26f), 3, 90f, 170f, 1300f, coinScale(0.04f), 1600f, 1.8f);   // par sa bouche-fente
            }
        }

        /** Son portrait sans le levier (qui est redessiné à part, pour qu'il bouge). */
        private void drawBody(Batch batch) {
            float s = bossScale(), x0 = bossX() + poseX - boss.getRegionWidth() * s / 2f, y0 = bossBottom() + poseY;
            batch.setColor(Color.WHITE);
            for (int[] part : BODY_PARTS) {
                batch.draw(new TextureRegion(boss, part[0], part[1], part[2], part[3]), x0 + part[0] * s,
                    y0 + (boss.getRegionHeight() - part[1] - part[3]) * s, part[2] * s, part[3] * s);
            }
        }

        @Override
        protected void back(Batch batch) {
            bubbles(batch, 20, 0.3f);
            // les lumières qui s'affolent tout autour
            float crazy = progress(0.8f, CLIMAX_PHASE);
            for (int i = 0; i < 28; i++) {
                float k = i / 28f, x = worldWidth() * (0.08f + 0.84f * k), y = worldHeight() * 0.94f;
                int pick = (int) (time * (2f + 18f * crazy) + i) % BULBS.length;
                glow(batch, x, y, 60f, BULBS[pick], 0.8f);
                glow(batch, x, groundY() + 10f, 60f, BULBS[(pick + 2) % BULBS.length], 0.8f);
            }
            poseX = (random.nextFloat() - 0.5f) * 10f * crazy;
            poseY = 6f * Math.abs(MathUtils.sin(time * 20f)) * crazy;
            showBoss = false;
            drawBody(batch);
        }

        @Override
        protected void front(Batch batch) {
            // le levier : il descend tout seul
            float pull = Interpolation.pow2In.apply(progress(0.4f, 0.7f)) * (1f - progress(1.0f, 1.4f));
            float px = portraitX(LEVER_X), py = portraitY(LEVER_BASE), angle = 90f - 150f * pull;
            float length = LEVER_LENGTH * bossScale(), ball = 5f * bossScale();
            float tx = px + MathUtils.cosDeg(angle) * length, ty = py + MathUtils.sinDeg(angle) * length;
            line(batch, hex("1a1018"), 1f, px, py, tx, ty, 2.8f * bossScale());
            line(batch, hex("b8c0cc"), 1f, px, py, tx, ty, 2f * bossScale());
            rect(batch, hex("1a1018"), 1f, tx - ball / 2f - bossScale(), ty - ball / 2f - bossScale(), ball + 2f * bossScale(), ball + 2f * bossScale());
            rect(batch, hex("d8202c"), 1f, tx - ball / 2f, ty - ball / 2f, ball, ball);
            glow(batch, tx, ty, 70f, hex("ff3a5a"), 0.8f);
            // trois rouleaux au-dessus de lui, qui tournent de plus en plus vite
            float w = worldHeight() * 0.12f, y = worldHeight() * 0.74f;
            float speed = MathUtils.lerp(2f, 30f, progress(0.7f, CLIMAX_PHASE));
            for (int r = 0; r < 3; r++) {
                float x = bossX() + (r - 1) * w * 1.15f - w / 2f;
                rect(batch, hex("f4ecdc"), progress(0.6f, 0.9f), x - 6f, y - 6f, w + 12f, w + 12f);
                batch.setColor(1f, 1f, 1f, progress(0.6f, 0.9f));
                reel(batch, symbols, x, y, w, w, time * speed * (1f + 0.2f * r) + r * 2f);
            }
            flash(batch, GOLD, CLIMAX_PHASE, 0.3f, 0.45f);
        }
    }

    /** Trois symboles différents : des étincelles, l'écran s'éteint ; une dernière pièce roule jusqu'au joueur. */
    static final class JackpotDeath extends BossScene {
        private final TextureRegion[] symbols;
        private static final float[] STOPS = {1.2f, 1.8f, 2.4f};
        private static final int[] RESULT = {0, 3, 5};

        JackpotDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.JACKPOT_VIVANT, 0);
            symbols = new TextureRegion[] {load("symbols/2-cherry.png"), load("symbols/3-seven.png"), load("symbols/4-bar.png"),
                load("symbols/6-bell.png"), load("symbols/7-diamond.png"), load("symbols/11-watermelon.png")};
        }

        private float reelX(int r) { float w = worldHeight() * 0.12f; return bossX() + (r - 1) * w * 1.15f - w / 2f; }

        @Override
        protected void step(float delta) {
            for (int r = 0; r < 3; r++) if (at(STOPS[r])) rumble(0.1f, 4f);
            if (at(CLIMAX_DEATH)) {                                       // perdu : des étincelles
                rumble(0.5f, 9f);
                sparks(bossX(), bossBottom() + bossHeight() * 0.6f, 90, hex("7ad0ff"), 900f, 18f);
            }
        }

        @Override
        protected void back(Batch batch) {
            float dark = progress(CLIMAX_DEATH + 0.2f, CLIMAX_DEATH + 0.8f);
            poseShadow = 0.8f * dark;
            poseX = time > CLIMAX_DEATH && time < CLIMAX_DEATH + 0.5f ? (random.nextFloat() - 0.5f) * 14f : 0f;
        }

        @Override
        protected void front(Batch batch) {
            float w = worldHeight() * 0.12f, y = worldHeight() * 0.74f;
            float dark = progress(CLIMAX_DEATH + 0.2f, CLIMAX_DEATH + 0.8f);
            for (int r = 0; r < 3; r++) {                               // chaque rouleau s'arrête sur un symbole différent
                float offset;
                if (time >= STOPS[r]) offset = RESULT[r];
                else {
                    float left = STOPS[r] - time;
                    offset = RESULT[r] - left * 18f;
                }
                rect(batch, hex("f4ecdc"), 1f - dark, reelX(r) - 6f, y - 6f, w + 12f, w + 12f);
                batch.setColor(1f, 1f, 1f, 1f - dark);
                reel(batch, symbols, reelX(r), y, w, w, offset);
            }
            fill(batch, Color.BLACK, 0.6f * dark);
            // une dernière pièce roule vers le joueur (elle grandit en approchant)
            float k = progress(3.2f, 4.8f);
            if (k > 0f) {
                float x = MathUtils.lerp(bossX() + bossWidth() * 0.3f, bossX() - 60f, k);
                float yy = MathUtils.lerp(groundY() + 40f, -40f, Interpolation.pow2In.apply(k));
                float s = coinScale(MathUtils.lerp(0.04f, 0.16f, Interpolation.pow2In.apply(k)));
                glow(batch, x, yy, 180f * (1f + 2f * k), GOLD, 0.5f);
                stretch(batch, coin, x, yy, s * Math.abs(MathUtils.cos(time * 8f)) + s * 0.15f, s, 0f, WHITE, 1f);
            }
            flash(batch, hex("7ad0ff"), CLIMAX_DEATH, 0.3f, 0.4f);
        }
    }

    // -------------------------------------------------------------------------
    // Requin Banquier
    // -------------------------------------------------------------------------

    /** Un nuage rouge : il tourne en cercles de plus en plus rapides, son aileron fend une pluie de billets. */
    static final class SharkPhase extends BossScene {
        private static final Color RED = hex("c8102e"), BLUE = hex("7ad0ff");
        private final TextureRegion fin, bill;
        private float angle;

        SharkPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.REQUIN_BANQUIER, 2);
            fin = art("s", new Color[] {hex("5a7a9a")}, FIN);
            bill = symbol(EnemySymbol.FAKE_MONEY);
        }

        @Override protected void restart() { angle = 0f; }

        @Override
        protected void step(float delta) {
            angle += delta * MathUtils.lerp(90f, 900f, progress(0f, CLIMAX_PHASE));
            if (time > 0.6f && random.nextFloat() < 0.22f) {            // la pluie de billets
                Particle note = place(bill, worldWidth() * random.nextFloat(), worldHeight() + 40f, (random.nextFloat() - 0.5f) * 80f,
                    -200f - 120f * random.nextFloat(), iconScale(0.06f), 3f);
                note.spin = (random.nextFloat() - 0.5f) * 200f;
            }
            if (at(CLIMAX_PHASE)) { rumble(0.6f, 12f); sparks(bossX(), bossY(), 60, RED, 900f, 22f); }
            // l'aileron coupe les billets qu'il touche
            float[] f = finAt();
            for (Particle p : sprites) {
                if (p.region == bill && Math.abs(p.x - f[0]) < 50f && Math.abs(p.y - f[1]) < 80f && p.age < p.life) {
                    p.age = p.life;
                    sparks(p.x, p.y, 6, hex("6ad06a"), 300f, 10f);
                }
            }
        }

        private float[] finAt() {
            return new float[] {bossX() + MathUtils.cosDeg(angle) * worldWidth() * 0.34f, groundY() + 60f + MathUtils.sinDeg(angle) * 50f};
        }

        @Override
        protected void back(Batch batch) {
            bubbles(batch, 24, 0.3f);
            float cloud = progress(0.3f, CLIMAX_PHASE);
            for (int i = 0; i < 7; i++) {                                // le nuage rouge
                glow(batch, worldWidth() * (0.1f + i * 0.13f), worldHeight() * (0.5f + 0.1f * MathUtils.sin(time + i)), 700f, RED, 0.25f * cloud);
            }
            float[] f = finAt();
            boolean behind = MathUtils.sinDeg(angle) > 0f;
            if (behind) drawFin(batch, f);
            poseTintColor.set(RED);
            poseTint = 0.3f * progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.4f);
        }

        private void drawFin(Batch batch, float[] f) {
            float dir = -MathUtils.sinDeg(angle) >= 0f ? 1f : -1f;
            stretch(batch, fin, f[0], f[1], fit(170f, 5) * dir, fit(170f, 5), 0f, WHITE, 1f);
            rect(batch, BLUE, 0.6f, f[0] - 70f, f[1] - 56f, 140f, 6f);                // le sillage
        }

        @Override
        protected void front(Batch batch) {
            float[] f = finAt();
            if (MathUtils.sinDeg(angle) <= 0f) drawFin(batch, f);
            eyes(batch, progress(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.2f), RED, new float[] {11.5f, 14f}, new float[] {23.5f, 14f});
            flash(batch, RED, CLIMAX_PHASE, 0.3f, 0.45f);
        }
    }

    /** Son ventre-coffre cède : les billets s'envolent ; il coule dans le noir. */
    static final class SharkDeath extends BossScene {
        private final TextureRegion safe, bill;

        SharkDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.REQUIN_BANQUIER, 0);
            safe = symbol(EnemySymbol.SAFE);
            bill = symbol(EnemySymbol.FAKE_MONEY);
        }

        private float bellyY() { return bossBottom() + poseY + bossHeight() * 0.3f; }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_DEATH)) { rumble(0.6f, 10f); sparks(bossX(), bellyY(), 50, GOLD, 700f, 18f); }
            if (time > CLIMAX_DEATH && time < 4.2f && random.nextFloat() < 0.7f) {   // les billets s'échappent et remontent
                Particle note = place(bill, bossX() + (random.nextFloat() - 0.5f) * 80f, bossBottom() + bossHeight() * 0.3f - sinkY(),
                    (random.nextFloat() - 0.5f) * 300f, 120f + 160f * random.nextFloat(), iconScale(0.06f), 2.6f);
                note.drag = 0.7f;
                note.spin = (random.nextFloat() - 0.5f) * 240f;
            }
        }

        private float sinkY() { return worldHeight() * 0.5f * Interpolation.pow2In.apply(progress(CLIMAX_DEATH + 0.3f, 4.8f)); }

        @Override
        protected void back(Batch batch) {
            bubbles(batch, 24, 0.3f);
            poseY = -sinkY();
            poseShadow = 0.8f * progress(CLIMAX_DEATH, 4.6f);
            poseTilt = -20f * progress(CLIMAX_DEATH + 0.3f, 4.8f);
        }

        @Override
        protected void front(Batch batch) {
            float s = iconScale(0.16f);
            float open = Interpolation.swingOut.apply(progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.4f));
            float shake = time < CLIMAX_DEATH ? (random.nextFloat() - 0.5f) * 10f * progress(1.2f, CLIMAX_DEATH) : 0f;
            draw(batch, safe, bossX() + shake, bellyY(), s, 0f, tmp.set(1f - poseShadow, 1f - poseShadow, 1f - poseShadow, 1f), 1f);
            // la porte du coffre qui s'ouvre (elle s'aplatit sur le côté)
            stretch(batch, safe, bossX() - s * 8f * open + shake, bellyY(), s * (1f - 0.85f * open), s, 0f, hex("8a8a98"), 1f);
            fill(batch, Color.BLACK, 0.6f * progress(3.4f, 4.8f));      // les profondeurs
        }
    }

    // -------------------------------------------------------------------------
    // Kraken
    // -------------------------------------------------------------------------

    /** Un nuage d'encre : ses bras surgissent de l'encre par tous les bords. */
    static final class KrakenPhase extends BossScene {
        private static final Color INK = hex("12061e"), ARM = hex("6a2a8a"), PURPLE = hex("b05aff");
        private static final float[][] EDGES = {{-0.05f, 0.2f}, {-0.05f, 0.7f}, {1.05f, 0.25f}, {1.05f, 0.75f}, {0.2f, 1.08f},
            {0.8f, 1.08f}, {0.35f, -0.08f}, {0.65f, -0.08f}};

        KrakenPhase(CutsceneKit kit) { super(kit, Moment.PHASE, EnemyKind.KRAKEN, 2); }

        @Override
        protected void step(float delta) {
            if (at(0.4f)) rumble(0.3f, 5f);
            if (at(CLIMAX_PHASE)) { rumble(0.8f, 14f); sparks(bossX(), bossY(), 60, PURPLE, 900f, 20f); }
        }

        @Override
        protected void back(Batch batch) {
            bubbles(batch, 24, 0.3f);
            poseShadow = 0.5f * progress(0.6f, 1.6f);
        }

        @Override
        protected void front(Batch batch) {
            // l'encre envahit l'écran
            float ink = progress(0.3f, 1.4f);
            fill(batch, INK, 0.75f * ink);
            for (int i = 0; i < 10; i++) {
                float x = worldWidth() * (i * 0.11f), y = worldHeight() * (0.5f + 0.3f * MathUtils.sin(i * 2.1f + time * 0.6f));
                batch.setColor(INK.r, INK.g, INK.b, 0.7f * ink);
                batch.draw(soft, x - 400f, y - 400f, 800f, 800f);
            }
            eyes(batch, progress(1.2f, 1.6f), PURPLE, new float[] {14f, 21f}, new float[] {22f, 21f});
            // les bras surgissent de l'encre, des bords vers lui
            for (int i = 0; i < EDGES.length; i++) {
                float out = Interpolation.pow2Out.apply(progress(1.4f + i * 0.08f, CLIMAX_PHASE + i * 0.04f));
                if (out <= 0f) continue;
                float x0 = EDGES[i][0] * worldWidth(), y0 = EDGES[i][1] * worldHeight();
                float x1 = MathUtils.lerp(x0, bossX() + (x0 - bossX()) * 0.25f, out), y1 = MathUtils.lerp(y0, bossY() + (y0 - bossY()) * 0.25f, out);
                tentacle(this, batch, x0, y0, x1, y1, 110f, 50f, i * 1.3f, ARM);
            }
            flash(batch, PURPLE, CLIMAX_PHASE, 0.3f, 0.4f);
        }
    }

    /** Ses huit bras lâchent le salon VIP un à un ; il coule ; sa couronne remonte seule. */
    static final class KrakenDeath extends BossScene {
        private static final Color ARM = hex("6a2a8a"), HEAD = hex("8a1e2a");
        /** Sa couronne dans son portrait : colonnes 9 à 25, lignes 0 à 6. */
        private static final int CROWN_X = 9, CROWN_W = 17, CROWN_H = 7;
        private final TextureRegion crown, body;

        KrakenDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.KRAKEN, 0);
            crown = new TextureRegion(boss, CROWN_X, 0, CROWN_W, CROWN_H);
            body  = new TextureRegion(boss, 0, CROWN_H, boss.getRegionWidth(), boss.getRegionHeight() - CROWN_H);
        }

        private float letGo(int i) { return 0.4f + i * 0.25f; }

        private float sinkY() { return worldHeight() * 0.6f * Interpolation.pow2In.apply(progress(CLIMAX_DEATH, 4.6f)); }

        @Override
        protected void step(float delta) {
            for (int i = 0; i < 8; i++) if (at(letGo(i))) { rumble(0.12f, 5f); sparks(gripX(i), gripY(i), 10, hex("b05aff"), 300f, 14f); }
            if (at(CLIMAX_DEATH)) rumble(0.5f, 8f);
        }

        private float gripX(int i) { return i < 4 ? worldWidth() * 0.02f : worldWidth() * 0.98f; }
        private float gripY(int i) { return worldHeight() * (0.25f + (i % 4) * 0.18f); }

        @Override
        protected void back(Batch batch) {
            bubbles(batch, 30, 0.35f);
            // le salon VIP : des colonnes dorées que ses bras tiennent
            rect(batch, hex("5a4a1a"), 1f, -60f, -60f, worldWidth() * 0.04f + 60f, worldHeight() + 120f);
            rect(batch, hex("5a4a1a"), 1f, worldWidth() * 0.96f, -60f, worldWidth() * 0.04f + 60f, worldHeight() + 120f);
            for (int i = 0; i < 8; i++) {
                float release = Interpolation.pow2Out.apply(progress(letGo(i), letGo(i) + 0.4f));
                float x0 = bossX() + (i < 4 ? -1 : 1) * bossWidth() * 0.3f, y0 = bossY() - sinkY();
                float x1 = MathUtils.lerp(gripX(i), x0 + (gripX(i) - x0) * 0.3f, release);
                float y1 = MathUtils.lerp(gripY(i), y0 - 120f, release);
                tentacle(this, batch, x0, y0, x1, y1, 100f, 30f + 40f * release, i * 1.1f, ARM);
            }
            poseY = -sinkY();
            poseShadow = 0.3f + 0.5f * progress(CLIMAX_DEATH, 4.4f);
            if (time >= CLIMAX_DEATH) {                                 // sa couronne s'est détachée : on la retire du portrait
                showBoss = false;
                float light = 1f - poseShadow, x0 = bossX() - bossWidth() / 2f, y0 = bossBottom() + poseY;
                batch.setColor(light, light, light, 1f);
                batch.draw(body, x0, y0, bossWidth(), body.getRegionHeight() * bossScale());
                cap(batch, x0, y0, bossScale(), CROWN_H - 1, CROWN_X + 2, CROWN_X + CROWN_W - 3, HEAD, light, 1f);   // le haut de sa tête
            }
        }

        @Override
        protected void front(Batch batch) {
            fill(batch, Color.BLACK, 0.5f * progress(CLIMAX_DEATH + 0.5f, 4.6f));
            // sa couronne se détache et remonte seule
            if (time < CLIMAX_DEATH) return;
            float k = progress(CLIMAX_DEATH, 4.8f);
            float crownStart = portraitY(CROWN_H / 2f);
            float y = time < CLIMAX_DEATH ? crownStart : crownStart + worldHeight() * 0.35f * Interpolation.pow2Out.apply(k);
            float x = bossX() + MathUtils.sin(time * 2.4f) * 20f * k;
            glow(batch, x, y, 200f, GOLD, 0.5f * k);
            draw(batch, crown, x, y, bossScale(), MathUtils.sin(time * 3f) * 10f * k, WHITE, 1f);
        }
    }
}
