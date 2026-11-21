package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;

import static fr.astratime.lucky.assets.Palette.hex;

/** Les scènes de phase et de mort des quatre rois du Manoir hanté (10 lieux, Astra 2026-11-20). */
final class ManorBossScenes {

    private ManorBossScenes() {}

    static BossScene phase(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case COMTE_MINUIT -> new CountPhase(kit);
            case BIBLIOTHECAIRE -> new LibrarianPhase(kit);
            case FOSSOYEUR -> new DiggerPhase(kit);
            case MARIONNETTISTE -> new PuppeteerPhase(kit);
            default -> null;
        };
    }

    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case COMTE_MINUIT -> new CountDeath(kit);
            case BIBLIOTHECAIRE -> new LibrarianDeath(kit);
            case FOSSOYEUR -> new DiggerDeath(kit);
            case MARIONNETTISTE -> new PuppeteerDeath(kit);
            default -> null;
        };
    }

    static final Color BAT = hex("1a0f22"), CRIMSON = hex("d8203a"), CANDLE = hex("ffd27a"), VIOLET = hex("b88aff");
    static final Color GRAVE = hex("6ad8a0"), EARTH = hex("5a3a22"), THREAD = hex("e8e0d0"), DUST = hex("b8a898");
    static final String[] BOOK = {"cccccccc", "cppppppc", "cppppppc", "cppppppc", "cppppppc", "cccccccc"};
    static final String[] SCISSORS = {"o...o", ".o.o.", "..s..", ".s.s.", "ss.ss"};

    /** Une chauve-souris qui file (un petit rectangle sombre battant des ailes, dans les lueurs). */
    private static void bats(BossScene scene, float x, float y, int count) {
        for (int k = 0; k < count; k++) {
            Cutscene.Particle bat = scene.particle(x, y, 200f + 300f * scene.random.nextFloat(), 500f, 1.8f, 18f);
            bat.color.set(BAT);
            scene.glows.add(bat);
        }
    }

    // -------------------------------------------------------------------------
    // Le Comte de Minuit
    // -------------------------------------------------------------------------

    /** « Les douze coups sonnent » : douze coups de cloche, une lueur rouge, des chauves-souris à chaque coup. */
    static final class CountPhase extends BossScene {
        private final TextureRegion fang;

        CountPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.COMTE_MINUIT, 2);
            fang = symbol(EnemySymbol.FANG);
        }

        private float strike(int k) { return 0.3f + k * (CLIMAX_PHASE - 0.3f) / 12f; }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 12; k++) {
                if (at(strike(k))) { rumble(0.08f, 2f + k * 0.5f); bats(this, bossX(), bossY() + bossHeight() * 0.3f, 2); }
            }
            if (at(CLIMAX_PHASE)) { rumble(0.6f, 12f); bats(this, bossX(), bossY(), 24); sparks(bossX(), bossY(), 50, CRIMSON, 700f, 12f); }
        }

        @Override
        protected void back(Batch batch) {
            fill(batch, Color.BLACK, 0.5f * progress(0.2f, 1.0f));
            poseTint = 0.45f * progress(0.6f, CLIMAX_PHASE) * pulse(6f, 0.6f);
            poseTintColor.set(CRIMSON);
            poseY = 30f * progress(1.4f, CLIMAX_PHASE);
        }

        @Override
        protected void front(Batch batch) {
            for (int k = 0; k < 12; k++) flash(batch, CRIMSON, strike(k), 0.06f, 0.12f);
            if (fang != null && time >= CLIMAX_PHASE) {
                draw(batch, fang, bossX(), bossY() + bossHeight() * 0.65f, iconScale(0.12f), 0f, WHITE,
                    progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.3f));
            }
            if (time >= CLIMAX_PHASE) glow(batch, bossX(), bossY(), bossHeight() * 1.4f, CRIMSON, 0.3f * pulse(5f, 0.6f));
            flash(batch, CRIMSON, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /** Le jour se lève : il tombe en poussière, et la poussière s'envole en chauves-souris. */
    static final class CountDeath extends BossScene {
        CountDeath(CutsceneKit kit) { super(kit, Moment.DEATH, EnemyKind.COMTE_MINUIT, 0); }

        @Override
        protected void step(float delta) {
            if (time > 1.0f && time < 3.4f && random.nextFloat() < delta * 30f) {
                bats(this, bossX() + (random.nextFloat() - 0.5f) * bossWidth(), bossBottom() + bossHeight() * random.nextFloat(), 1);
                sparks(bossX() + (random.nextFloat() - 0.5f) * bossWidth(), bossBottom() + bossHeight() * random.nextFloat(), 3,
                    DUST, 200f, 8f);
            }
            if (at(CLIMAX_DEATH)) { rumble(0.5f, 8f); spray(coin, bossX(), bossY(), 20, 40f, 140f, 800f, coinScale(0.04f), 1400f, 1.5f); }
        }

        @Override
        protected void back(Batch batch) {
            glow(batch, bossX(), worldHeight(), worldHeight() * progress(0.6f, 3.0f), CANDLE, 0.3f * progress(0.6f, 2.0f));
            poseTint = 0.6f * progress(0.8f, 2.4f);
            poseTintColor.set(DUST);
            poseAlpha = 1f - progress(1.4f, 3.4f);
            showBoss = time < 3.5f;
        }

        @Override
        protected void front(Batch batch) { flash(batch, CANDLE, CLIMAX_DEATH, 0.3f, 0.3f); }
    }

    // -------------------------------------------------------------------------
    // La Bibliothécaire
    // -------------------------------------------------------------------------

    /** « Les bougies s'éteignent » : trois bougies soufflées une à une, le noir, ses yeux violets s'allument. */
    static final class LibrarianPhase extends BossScene {
        private final TextureRegion blind;

        LibrarianPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.BIBLIOTHECAIRE, 2);
            blind = symbol(EnemySymbol.BLIND);
        }

        private float candleX(int k) { return bossX() + (k - 1) * bossWidth() * 0.9f; }
        private float candleY()      { return bossBottom() + bossHeight() * 1.05f; }
        private float out(int k)     { return 0.6f + k * 0.45f; }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 3; k++) if (at(out(k))) sparks(candleX(k), candleY() + 30f, 12, DUST, 200f, 8f);
            if (at(CLIMAX_PHASE)) { rumble(0.4f, 8f); sparks(bossX(), bossY() + bossHeight() * 0.3f, 50, VIOLET, 600f, 12f); }
        }

        @Override
        protected void back(Batch batch) {
            int lit = 0;
            for (int k = 0; k < 3; k++) if (time < out(k)) lit++;
            fill(batch, Color.BLACK, 0.25f * (3 - lit));
            for (int k = 0; k < 3; k++) {
                if (time < out(k)) glow(batch, candleX(k), candleY() + 30f, 200f * pulse(12f + k, 0.8f), CANDLE, 0.6f);
            }
            poseY = 20f * progress(1.6f, CLIMAX_PHASE);
        }

        @Override
        protected void front(Batch batch) {
            for (int k = 0; k < 3; k++) {
                rect(batch, hex("efe6d0"), 1f, candleX(k) - 10f, candleY() - 40f, 20f, 60f);
                if (time < out(k)) rect(batch, CANDLE, pulse(14f + k, 0.7f), candleX(k) - 5f, candleY() + 20f, 10f, 20f);
            }
            if (time >= CLIMAX_PHASE - 0.3f) {
                glow(batch, bossX(), bossY() + bossHeight() * 0.25f, bossWidth() * 0.6f, VIOLET,
                    0.5f * progress(CLIMAX_PHASE - 0.3f, CLIMAX_PHASE) * pulse(7f, 0.6f));
            }
            if (blind != null && time >= CLIMAX_PHASE) {
                draw(batch, blind, bossX(), bossY() + bossHeight() * 0.65f, iconScale(0.12f), 0f, WHITE,
                    progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.3f));
            }
            flash(batch, VIOLET, CLIMAX_PHASE, 0.3f, 0.3f);
        }
    }

    /** Ses livres se referment sur elle et tombent en pile ; une page s'envole et devient une pluie de pièces. */
    static final class LibrarianDeath extends BossScene {
        private final TextureRegion book;

        LibrarianDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.BIBLIOTHECAIRE, 0);
            book = art("cp", new Color[] {hex("5a2a7a"), hex("efe6d0")}, BOOK);
        }

        private float bookX(int k) { return bossX() + MathUtils.cosDeg(k * 60f + time * 120f) * bossWidth() * 0.8f * (1f - fall()); }
        private float bookY(int k) { return bossY() + MathUtils.sinDeg(k * 60f + time * 120f) * bossHeight() * 0.4f * (1f - fall()); }
        private float fall()       { return Interpolation.pow2In.apply(progress(1.6f, CLIMAX_DEATH)); }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_DEATH)) {
                rumble(0.6f, 10f);
                sparks(bossX(), bossY(), 40, hex("efe6d0"), 600f, 10f);
                spray(coin, bossX(), bossY(), 24, 40f, 140f, 900f, coinScale(0.04f), 1400f, 1.6f);
            }
        }

        @Override
        protected void back(Batch batch) {
            poseStretch = -0.1f * gesture(0.3f, 1.2f);
            showBoss = time < CLIMAX_DEATH;
        }

        @Override
        protected void front(Batch batch) {
            if (time < CLIMAX_DEATH) {
                for (int k = 0; k < 6; k++) {
                    draw(batch, book, bookX(k), bookY(k), fit(70f, 6), time * 200f + k * 30f, WHITE, progress(0.2f, 0.6f));
                }
            } else {
                for (int k = 0; k < 6; k++) {                                              // la pile de livres
                    draw(batch, book, bossX() + (k % 2 == 0 ? -6f : 6f), groundY() + 20f + k * 36f, fit(70f, 6), 90f + (k % 3 - 1) * 6f,
                        WHITE, 1f);
                }
            }
            flash(batch, VIOLET, CLIMAX_DEATH, 0.3f, 0.35f);
        }
    }

    // -------------------------------------------------------------------------
    // Le Fossoyeur
    // -------------------------------------------------------------------------

    /** « La terre tremble » : il frappe le sol de sa pelle, le sol se fend, des lueurs vertes sortent des fissures. */
    static final class DiggerPhase extends BossScene {
        private final TextureRegion rockfall;

        DiggerPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.FOSSOYEUR, 2);
            rockfall = symbol(EnemySymbol.ROCKFALL);
        }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 3; k++) if (at(0.6f + k * 0.5f)) { rumble(0.2f, 6f + 2f * k); sparks(bossX(), groundY(), 25, EARTH, 600f, 12f); }
            if (at(CLIMAX_PHASE)) {
                rumble(0.7f, 14f);
                sparks(bossX(), groundY(), 60, GRAVE, 800f, 14f);
                if (rockfall != null) spray(rockfall, bossX(), worldHeight(), 8, 250f, 290f, 600f, iconScale(0.06f), 1400f, 1.4f);
            }
        }

        @Override
        protected void back(Batch batch) {
            float hit = 0f;
            for (int k = 0; k < 3; k++) hit = Math.max(hit, gesture(0.4f + k * 0.5f, 0.6f + k * 0.5f));
            poseStretch = -0.08f * hit;
            poseTint = 0.3f * progress(1.6f, CLIMAX_PHASE);
            poseTintColor.set(GRAVE);
        }

        @Override
        protected void front(Batch batch) {
            for (int k = 0; k < 3; k++) {
                float open = progress(0.6f + k * 0.5f, 0.9f + k * 0.5f);
                if (open > 0f) crack(batch, bossX() + (k - 1) * 120f, groundY(), -90f + (k - 1) * 30f, 200f * open, 31L + k, GRAVE, 0.9f, 6f);
            }
            if (time >= CLIMAX_PHASE) glow(batch, bossX(), groundY(), bossWidth() * 1.4f, GRAVE, 0.35f * pulse(6f, 0.6f));
            flash(batch, GRAVE, CLIMAX_PHASE, 0.3f, 0.3f);
        }
    }

    /** Il s'enfonce dans sa propre fosse ; la terre se referme sur lui et une pierre tombale pousse, couverte de pièces. */
    static final class DiggerDeath extends BossScene {
        DiggerDeath(CutsceneKit kit) { super(kit, Moment.DEATH, EnemyKind.FOSSOYEUR, 0); }

        @Override
        protected void step(float delta) {
            if (time > 0.6f && time < CLIMAX_DEATH && random.nextFloat() < delta * 20f) sparks(bossX(), groundY(), 4, EARTH, 400f, 10f);
            if (at(0.6f)) rumble(1.8f, 5f);
            if (at(3.2f)) spray(coin, bossX(), groundY() + 160f, 18, 60f, 120f, 700f, coinScale(0.04f), 1400f, 1.4f);
        }

        @Override
        protected void back(Batch batch) {
            poseSink = Interpolation.pow2In.apply(progress(0.6f, CLIMAX_DEATH + 0.4f));
            poseShadow = 0.4f * progress(0.6f, CLIMAX_DEATH);
        }

        @Override
        protected void front(Batch batch) {
            float rise = Interpolation.pow2Out.apply(progress(CLIMAX_DEATH + 0.2f, 3.2f));
            if (rise > 0f) {
                float h = 220f * rise;
                rect(batch, hex("6a6a78"), 1f, bossX() - 80f, groundY(), 160f, h);
                rect(batch, hex("4a4a58"), 1f, bossX() - 10f, groundY() + h - 120f * rise, 20f, 90f * rise);
                rect(batch, hex("4a4a58"), 1f, bossX() - 40f, groundY() + h - 70f * rise, 80f, 18f * rise);
            }
            rect(batch, EARTH, 1f, bossX() - 220f, groundY() - 20f, 440f, 24f);
            flash(batch, GRAVE, CLIMAX_DEATH, 0.3f, 0.25f);
        }
    }

    // -------------------------------------------------------------------------
    // Le Marionnettiste
    // -------------------------------------------------------------------------

    /** « Le rideau se lève » : des fils tombent du plafond et l'agitent, une lumière de scène s'allume sur lui. */
    static final class PuppeteerPhase extends BossScene {
        private final TextureRegion song;

        PuppeteerPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.MARIONNETTISTE, 2);
            song = symbol(EnemySymbol.SONG);
        }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 4; k++) if (at(0.6f + k * 0.35f)) rumble(0.08f, 3f);
            if (at(CLIMAX_PHASE)) { rumble(0.5f, 10f); sparks(bossX(), bossY(), 50, GOLD, 700f, 12f); }
        }

        @Override
        protected void back(Batch batch) {
            fill(batch, Color.BLACK, 0.5f * progress(0.2f, 0.8f));
            glow(batch, bossX(), bossY(), bossHeight() * 1.2f, CANDLE, 0.45f * progress(0.8f, 1.4f));
            float jerk = progress(1.0f, CLIMAX_PHASE);
            poseTilt = 10f * MathUtils.sin(time * 14f) * jerk;
            poseY = 30f * Math.abs(MathUtils.sin(time * 7f)) * jerk;
        }

        @Override
        protected void front(Batch batch) {
            float drop = Interpolation.pow2Out.apply(progress(0.4f, 1.0f));
            for (int k = 0; k < 4; k++) {
                float x = bossX() + (k - 1.5f) * bossWidth() * 0.35f;
                line(batch, THREAD, 0.8f, x, worldHeight(), x, MathUtils.lerp(worldHeight(), bossY() + poseY, drop), 3f);
            }
            if (song != null && time >= CLIMAX_PHASE) {
                for (int k = 0; k < 2; k++) draw(batch, song, bossX() + (k == 0 ? -1f : 1f) * bossWidth() * 0.7f, bossY(),
                    iconScale(0.1f), 0f, WHITE, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.3f));
            }
            flash(batch, GOLD, CLIMAX_PHASE, 0.3f, 0.3f);
        }
    }

    /** Une paire de ciseaux coupe ses fils un à un ; il s'effondre comme un pantin, et le rideau tombe sur des pièces. */
    static final class PuppeteerDeath extends BossScene {
        private final TextureRegion scissors;

        PuppeteerDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.MARIONNETTISTE, 0);
            scissors = art("os", new Color[] {hex("dce4ee"), hex("8a1a1a")}, SCISSORS);
        }

        private float cut(int k) { return 0.6f + k * 0.4f; }
        private float threadX(int k) { return bossX() + (k - 1.5f) * bossWidth() * 0.35f; }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 4; k++) if (at(cut(k))) sparks(threadX(k), bossY() + bossHeight() * 0.6f, 8, THREAD, 300f, 6f);
            if (at(CLIMAX_DEATH)) { rumble(0.6f, 12f); spray(coin, bossX(), groundY() + 60f, 30, 40f, 140f, 1000f, coinScale(0.04f), 1500f, 1.6f); }
        }

        @Override
        protected void back(Batch batch) {
            float slump = Interpolation.bounceOut.apply(progress(cut(3), CLIMAX_DEATH));
            poseTilt = -20f * slump;
            poseSink = 0.3f * slump;
            poseShadow = 0.5f * progress(CLIMAX_DEATH, 3.4f);
        }

        @Override
        protected void front(Batch batch) {
            for (int k = 0; k < 4; k++) {
                float x = threadX(k), y = bossY() + bossHeight() * 0.6f;
                if (time < cut(k)) line(batch, THREAD, 0.8f, x, worldHeight(), x, y, 3f);
                else line(batch, THREAD, 0.8f * (1f - progress(cut(k), cut(k) + 0.6f)), x, worldHeight(), x, worldHeight() - 120f, 3f);
            }
            for (int k = 0; k < 4; k++) {
                if (time > cut(k) - 0.3f && time < cut(k) + 0.1f) {
                    draw(batch, scissors, threadX(k), bossY() + bossHeight() * 0.6f, fit(60f, 5), 20f * MathUtils.sin(time * 40f),
                        WHITE, 1f);
                }
            }
            float curtain = Interpolation.pow2In.apply(progress(3.6f, 4.6f));
            if (curtain > 0f) rect(batch, hex("8a1a2a"), 1f, 0f, worldHeight() * (1f - curtain), worldWidth(), worldHeight() * curtain);
            flash(batch, GOLD, CLIMAX_DEATH, 0.3f, 0.3f);
        }
    }
}
