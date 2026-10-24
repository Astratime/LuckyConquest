package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;

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
    static final String[] CROWN = {"g..g..g", "gg.g.gg", "ggggggg", "grgbgrg", "ggggggg"};

    static Color c(String hex) { return Color.valueOf(hex); }

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
        private static final Color PINK = c("ff7ad8");
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
            eyes(batch, progress(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.2f), PINK, 0.7f, 0.08f);
            wave(batch, CLIMAX_PHASE, 0.8f, bossX(), bossY(), PINK, worldWidth() * 1.3f);
            flash(batch, PINK, CLIMAX_PHASE, 0.3f, 0.4f);
        }
    }

    /** Une fausse note, un verre se fend ; elle se dissout en écume et en bulles. */
    static final class SirenDeath extends BossScene {
        private final TextureRegion note, glass;

        SirenDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.SIRENE, 0);
            note = art("w", new Color[] {Color.WHITE}, NOTE);
            glass = art("owpy", new Color[] {c("1a0a1a"), c("e8f0ff"), c("ff7ad8"), c("ffd860")}, GLASS);
        }

        private float glassX() { return bossX() + bossWidth() * 0.75f; }
        private float glassY() { return groundY() + 90f; }

        @Override
        protected void step(float delta) {
            if (at(1.3f)) rumble(0.3f, 6f);                              // la fausse note
            if (at(CLIMAX_DEATH)) {
                rumble(0.4f, 7f);
                sparks(glassX(), glassY(), 30, WHITE, 600f, 14f);
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
            float shake = time > 1.3f && time < 1.8f ? (random.nextFloat() - 0.5f) * 10f : 0f;
            batch.draw(top, bossX() - width / 2f + shake, bossBottom() + (rows - keep) * scale, width, keep * scale);
        }

        @Override
        protected void front(Batch batch) {
            // ses notes, justes, puis la fausse note qui part de travers
            for (int i = 0; i < 5; i++) {
                float k = ((time * 0.5f + i / 5f) % 1f);
                float x = bossX() - bossWidth() * 0.6f - k * 200f, y = bossY() + k * 300f;
                float wrong = time > 1.3f ? 1f : 0f;
                draw(batch, note, x + wrong * (random.nextFloat() - 0.5f) * 20f, y, fit(46f, 6), wrong * 40f * MathUtils.sin(time * 30f + i),
                    wrong > 0f ? c("8a8aa0") : WHITE, (1f - k) * (1f - progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.5f)));
            }
            draw(batch, glass, glassX(), glassY(), fit(120f, 9), 0f, WHITE, 1f);
            float crack = progress(1.3f, CLIMAX_DEATH);                  // le verre se fend
            for (int i = 0; i < 4; i++) {
                float a = -60f + i * 40f, len = 60f * Math.min(1f, crack * 4f - i);
                if (len > 0f) line(batch, Color.BLACK, 0.9f, glassX(), glassY() + 40f, glassX() + MathUtils.cosDeg(a) * len,
                    glassY() + 40f + MathUtils.sinDeg(a) * len, 3f);
            }
            fill(batch, c("e0f0ff"), 0.15f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 1f));
        }
    }

    // -------------------------------------------------------------------------
    // Jackpot Vivant
    // -------------------------------------------------------------------------

    /** Son levier se tire tout seul : les rouleaux s'affolent, les lumières clignotent, il crache des pièces. */
    static final class JackpotPhase extends BossScene {
        private final TextureRegion[] symbols;
        private static final Color[] BULBS = {c("ff3a5a"), c("ffd23a"), c("3aff8a"), c("3ab0ff"), c("ff7ad8")};

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
                spray(coin, bossX(), bossBottom() + bossHeight() * 0.4f, 3, 30f, 150f, 1300f, coinScale(0.04f), 1600f, 1.8f);
            }
        }

        private float leverX() { return bossX() + bossWidth() * 0.62f; }

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
        }

        @Override
        protected void front(Batch batch) {
            // le levier : il descend tout seul
            float pull = Interpolation.pow2In.apply(progress(0.4f, 0.7f)) * (1f - progress(1.0f, 1.4f));
            float base = bossBottom() + bossHeight() * 0.55f, angle = 80f - 150f * pull;
            float tx = leverX() + 30f + MathUtils.cosDeg(angle) * 160f, ty = base + MathUtils.sinDeg(angle) * 160f;
            line(batch, c("c8c8d0"), 1f, leverX() + 30f, base, tx, ty, 12f);
            glow(batch, tx, ty, 70f, c("ff3a5a"), 1f);
            // trois rouleaux au-dessus de lui, qui tournent de plus en plus vite
            float w = worldHeight() * 0.12f, y = worldHeight() * 0.74f;
            float speed = MathUtils.lerp(2f, 30f, progress(0.7f, CLIMAX_PHASE));
            for (int r = 0; r < 3; r++) {
                float x = bossX() + (r - 1) * w * 1.15f - w / 2f;
                rect(batch, c("f4ecdc"), progress(0.6f, 0.9f), x - 6f, y - 6f, w + 12f, w + 12f);
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
                sparks(bossX(), bossBottom() + bossHeight() * 0.6f, 90, c("7ad0ff"), 900f, 18f);
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
                rect(batch, c("f4ecdc"), 1f - dark, reelX(r) - 6f, y - 6f, w + 12f, w + 12f);
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
            flash(batch, c("7ad0ff"), CLIMAX_DEATH, 0.3f, 0.4f);
        }
    }

    // -------------------------------------------------------------------------
    // Requin Banquier
    // -------------------------------------------------------------------------

    /** Un nuage rouge : il tourne en cercles de plus en plus rapides, son aileron fend une pluie de billets. */
    static final class SharkPhase extends BossScene {
        private static final Color RED = c("c8102e"), BLUE = c("7ad0ff");
        private final TextureRegion fin, bill;
        private float angle;

        SharkPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.REQUIN_BANQUIER, 2);
            fin = art("s", new Color[] {c("5a7a9a")}, FIN);
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
                    sparks(p.x, p.y, 6, c("6ad06a"), 300f, 10f);
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
            eyes(batch, progress(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.2f), RED, 0.68f, 0.1f);
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
            stretch(batch, safe, bossX() - s * 8f * open + shake, bellyY(), s * (1f - 0.85f * open), s, 0f, c("8a8a98"), 1f);
            fill(batch, Color.BLACK, 0.6f * progress(3.4f, 4.8f));      // les profondeurs
        }
    }

    // -------------------------------------------------------------------------
    // Kraken
    // -------------------------------------------------------------------------

    /** Un nuage d'encre : ses bras surgissent de l'encre par tous les bords. */
    static final class KrakenPhase extends BossScene {
        private static final Color INK = c("12061e"), ARM = c("6a2a8a"), PURPLE = c("b05aff");
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
            eyes(batch, progress(1.2f, 1.6f), PURPLE, 0.7f, 0.1f);
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
        private static final Color ARM = c("6a2a8a");
        private final TextureRegion crown;

        KrakenDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.KRAKEN, 0);
            crown = art("grb", new Color[] {c("ffc93a"), c("c8102e"), c("3a7aff")}, CROWN);
        }

        private float letGo(int i) { return 0.4f + i * 0.25f; }

        private float sinkY() { return worldHeight() * 0.6f * Interpolation.pow2In.apply(progress(CLIMAX_DEATH, 4.6f)); }

        @Override
        protected void step(float delta) {
            for (int i = 0; i < 8; i++) if (at(letGo(i))) { rumble(0.12f, 5f); sparks(gripX(i), gripY(i), 10, c("b05aff"), 300f, 14f); }
            if (at(CLIMAX_DEATH)) rumble(0.5f, 8f);
        }

        private float gripX(int i) { return i < 4 ? worldWidth() * 0.02f : worldWidth() * 0.98f; }
        private float gripY(int i) { return worldHeight() * (0.25f + (i % 4) * 0.18f); }

        @Override
        protected void back(Batch batch) {
            bubbles(batch, 30, 0.35f);
            // le salon VIP : des colonnes dorées que ses bras tiennent
            rect(batch, c("5a4a1a"), 1f, -60f, -60f, worldWidth() * 0.04f + 60f, worldHeight() + 120f);
            rect(batch, c("5a4a1a"), 1f, worldWidth() * 0.96f, -60f, worldWidth() * 0.04f + 60f, worldHeight() + 120f);
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
                int cut = Math.round(boss.getRegionHeight() * 0.2f), rows = boss.getRegionHeight() - cut;
                TextureRegion body = new TextureRegion(boss, 0, cut, boss.getRegionWidth(), rows);
                float light = 1f - poseShadow;
                batch.setColor(light, light, light, 1f);
                batch.draw(body, bossX() - bossWidth() / 2f, bossBottom() + poseY, bossWidth(), rows * bossScale());
            }
        }

        @Override
        protected void front(Batch batch) {
            fill(batch, Color.BLACK, 0.5f * progress(CLIMAX_DEATH + 0.5f, 4.6f));
            // sa couronne se détache et remonte seule
            if (time < CLIMAX_DEATH) return;
            float k = progress(CLIMAX_DEATH, 4.8f);
            float crownStart = bossBottom() + bossHeight() * 0.9f;
            float y = time < CLIMAX_DEATH ? crownStart : crownStart + worldHeight() * 0.35f * Interpolation.pow2Out.apply(k);
            float x = bossX() + MathUtils.sin(time * 2.4f) * 20f * k;
            glow(batch, x, y, 200f, GOLD, 0.5f * k);
            draw(batch, crown, x, y, fit(70f, 5), MathUtils.sin(time * 3f) * 10f * k, WHITE, 1f);
        }
    }
}
