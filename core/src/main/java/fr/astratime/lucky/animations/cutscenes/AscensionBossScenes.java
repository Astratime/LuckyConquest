package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;

import static fr.astratime.lucky.assets.Palette.hex;

/** Les scènes de phase et de mort de l'Architecte, boss du chapitre « L'Ascension » (Tour à 10 chapitres, 2026-11-22). */
final class AscensionBossScenes {

    private AscensionBossScenes() {}

    static BossScene phase(CutsceneKit kit, EnemyKind kind, int phase) {
        if (kind != EnemyKind.ARCHITECTE) return null;
        return phase == 2 ? new ArchitectScaffold(kit) : new ArchitectKeystone(kit);
    }

    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        return kind == EnemyKind.ARCHITECTE ? new ArchitectDeath(kit) : null;
    }

    static final Color CHALK = hex("e8f2ff"), PLAN = hex("2a56c8"), STONE = hex("8a8a92"), STONE_DARK = hex("5a5a64");
    static final Color SPIRAL = hex("b88aff");
    /** Les yeux de l'Architecte, en pixels de son portrait : l'œil nu, puis celui du monocle. */
    static final float[] EYE = {13.5f, 14.5f}, MONOCLE = {20.5f, 14.5f};

    /** Un trait de plan au sol : les lignes bleues du plan de la Tour, tracées sous ses pieds. */
    private static void groundPlan(BossScene scene, Batch batch, float alpha) {
        float y = scene.groundY(), w = scene.worldWidth();
        for (int k = 0; k < 6; k++) {
            float x = w * (0.1f + k * 0.16f);
            scene.line(batch, PLAN, 0.6f * alpha, x, y * 0.2f, x + w * 0.05f, y * 0.95f, 3f);
        }
        scene.line(batch, PLAN, 0.6f * alpha, 0f, y * 0.6f, w, y * 0.6f, 3f);
    }

    // -------------------------------------------------------------------------
    // Phase 2 : « Les échafaudages montent » (il gagne l'Écho)
    // -------------------------------------------------------------------------

    /**
     * Des échafaudages poussent du sol autour de lui, perche après perche, planche après planche ; tout en haut,
     * ses yeux s'allument et trois ondes d'écho partent de lui.
     */
    static final class ArchitectScaffold extends BossScene {
        ArchitectScaffold(CutsceneKit kit) { super(kit, Moment.PHASE, EnemyKind.ARCHITECTE, 2); }

        private float poleX(int k) { return bossX() + (k - 1.5f) * bossWidth() * 0.62f; }
        private float rise(int k)  { return Interpolation.pow2Out.apply(progress(0.3f + k * 0.2f, 0.9f + k * 0.2f)); }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 4; k++) if (at(0.3f + k * 0.2f)) rumble(0.1f, 3f);
            for (int k = 0; k < 3; k++) if (at(1.3f + k * 0.18f)) sparks(bossX(), bossBottom() + bossHeight() * (0.4f + k * 0.2f), 8, CHALK, 240f, 8f);
            if (at(CLIMAX_PHASE)) {
                rumble(0.5f, 10f);
                sparks(bossX(), bossY() + bossHeight() * 0.2f, 40, theme.accent(), 600f, 10f);
            }
        }

        @Override
        protected void back(Batch batch) {
            groundPlan(this, batch, progress(0.1f, 0.6f));
            float top = bossBottom() + bossHeight() * 1.15f;
            for (int k = 0; k < 4; k++) {                                        // les perches qui montent
                float h = (top - groundY()) * rise(k);
                rect(batch, hex("8a5a2a"), 1f, poleX(k) - 6f, groundY(), 12f, h);
                rect(batch, hex("b8864a"), 1f, poleX(k) - 6f, groundY(), 3f, h);
            }
            for (int level = 0; level < 3; level++) {                            // les planches
                float k = progress(1.3f + level * 0.18f, 1.5f + level * 0.18f);
                if (k <= 0f) continue;
                float y = groundY() + (top - groundY()) * (0.3f + level * 0.3f);
                float w = (poleX(3) - poleX(0) + 20f) * k;
                rect(batch, hex("a87a3a"), 1f, bossX() - w / 2f, y, w, 12f);
                rect(batch, hex("5e3a18"), 1f, bossX() - w / 2f, y, w, 3f);
            }
            poseY = 14f * gesture(1.6f, CLIMAX_PHASE + 0.6f);
        }

        @Override
        protected void front(Batch batch) {
            eyes(batch, progress(1.8f, CLIMAX_PHASE), theme.accent(), EYE, MONOCLE);
            for (int k = 0; k < 3; k++) wave(batch, CLIMAX_PHASE + k * 0.25f, 0.9f, bossX(), bossY() + bossHeight() * 0.2f, CHALK, bossHeight() * 2.2f);
            if (time >= CLIMAX_PHASE) {
                TextureRegion echo = symbol(EnemySymbol.ECHO);
                if (echo != null) {
                    float k = progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.4f);
                    draw(batch, echo, bossX() + bossWidth() * 0.9f, bossY() + bossHeight() * 0.45f, iconScale(0.12f) * k, 0f, WHITE, k);
                }
            }
            flash(batch, CHALK, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    // -------------------------------------------------------------------------
    // Phase 3 : « La clé de voûte » (sa Tour frappe, il gagne le Vertige)
    // -------------------------------------------------------------------------

    /**
     * Des blocs de pierre tombent du ciel et forment une arche au-dessus de lui ; la clé de voûte, en or, se pose
     * en dernier. Tout tremble, une spirale de vertige s'ouvre derrière lui.
     */
    static final class ArchitectKeystone extends BossScene {
        private static final int BLOCKS = 7;

        ArchitectKeystone(CutsceneKit kit) { super(kit, Moment.PHASE, EnemyKind.ARCHITECTE, 3); }

        /** @return quand le bloc {@code k} se pose : les côtés d'abord, la clé de voûte (le milieu) en dernier. */
        private float landing(int k) {
            int fromSide = Math.min(k, BLOCKS - 1 - k);
            return k == BLOCKS / 2 ? CLIMAX_PHASE : 0.4f + fromSide * 0.35f + (k > BLOCKS / 2 ? 0.15f : 0f);
        }

        private float blockAngle(int k) { return 180f - k * 180f / (BLOCKS - 1); }
        private float archRadius()      { return bossWidth() * 1.0f; }
        private float archX(int k)      { return bossX() + MathUtils.cosDeg(blockAngle(k)) * archRadius(); }
        private float archY(int k)      { return bossBottom() + bossHeight() * 0.55f + MathUtils.sinDeg(blockAngle(k)) * archRadius(); }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < BLOCKS; k++) {
                if (at(landing(k))) {
                    rumble(k == BLOCKS / 2 ? 0.6f : 0.12f, k == BLOCKS / 2 ? 14f : 4f);
                    sparks(archX(k), archY(k), k == BLOCKS / 2 ? 40 : 10, k == BLOCKS / 2 ? GOLD : STONE, 400f, 9f);
                }
            }
        }

        @Override
        protected void back(Batch batch) {
            groundPlan(this, batch, 1f);
            float open = progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.8f);           // la spirale du vertige
            if (open > 0f) {
                for (int i = 0; i < 140; i++) {
                    float a = i * 9f + time * 140f, r = i * bossHeight() * 0.009f * open;
                    rect(batch, i % 20 < 10 ? SPIRAL : hex("6ad88a"), 0.5f * open, bossX() + MathUtils.cosDeg(a) * r - 3f,
                        bossY() + MathUtils.sinDeg(a) * r - 3f, 6f, 6f);
                }
            }
            poseTilt = time > CLIMAX_PHASE ? 4f * MathUtils.sin((time - CLIMAX_PHASE) * 9f) * (1f - progress(CLIMAX_PHASE, CLIMAX_PHASE + 1f)) : 0f;
        }

        @Override
        protected void front(Batch batch) {
            float size = bossWidth() * 0.42f;
            for (int k = 0; k < BLOCKS; k++) {
                float land = landing(k), fall = progress(land - 0.45f, land);
                if (fall <= 0f) continue;
                float y = MathUtils.lerp(worldHeight() + size, archY(k), Interpolation.pow2In.apply(fall));
                boolean key = k == BLOCKS / 2;
                float angle = blockAngle(k) - 90f;
                Color face = key ? GOLD : STONE, edge = key ? hex("c4891e") : STONE_DARK;
                batch.setColor(edge);
                batch.draw(pixel, archX(k) - size / 2f, y - size / 2f, size / 2f, size / 2f, size, size, 1f, 1f, angle);
                batch.setColor(face);
                batch.draw(pixel, archX(k) - size / 2f + 4f, y - size / 2f + 4f, size / 2f - 4f, size / 2f - 4f, size - 8f, size - 8f, 1f, 1f, angle);
                if (key && time >= CLIMAX_PHASE) glow(batch, archX(k), y, size * 3f, GOLD, 0.5f * pulse(6f, 0.6f));
            }
            eyes(batch, progress(CLIMAX_PHASE - 0.3f, CLIMAX_PHASE), GOLD, EYE, MONOCLE);
            flash(batch, GOLD, CLIMAX_PHASE, 0.3f, 0.4f);
        }
    }

    // -------------------------------------------------------------------------
    // Mort : sa Tour s'effondre
    // -------------------------------------------------------------------------

    /**
     * Les pierres de sa Tour lui tombent dessus une à une ; il s'enfonce sous les gravats. Son plan s'envole, se
     * déroule dans le ciel et brûle en pluie de pièces d'or.
     */
    static final class ArchitectDeath extends BossScene {
        private final float[] blockX = new float[14];

        ArchitectDeath(CutsceneKit kit) { super(kit, Moment.DEATH, EnemyKind.ARCHITECTE, 0); }

        @Override
        protected void restart() {
            for (int k = 0; k < blockX.length; k++) blockX[k] = (random.nextFloat() - 0.5f) * 1.6f;
        }

        private float landing(int k) { return 0.4f + k * 0.14f; }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < blockX.length; k++) if (at(landing(k))) rumble(0.08f, 4f);
            if (at(CLIMAX_DEATH)) {
                rumble(0.7f, 12f);
                sparks(bossX(), bossBottom() + bossHeight() * 0.3f, 50, STONE, 500f, 12f);
                spray(coin, bossX(), worldHeight() * 0.8f, 26, 200f, 340f, 700f, coinScale(0.04f), 1400f, 1.6f);
            }
        }

        @Override
        protected void back(Batch batch) {
            groundPlan(this, batch, 1f - progress(1.0f, CLIMAX_DEATH));
            poseSink = 0.85f * Interpolation.pow2In.apply(progress(1.0f, CLIMAX_DEATH + 0.3f));
            poseShadow = 0.4f * progress(1.0f, CLIMAX_DEATH);
            poseTilt = -6f * gesture(0.3f, 1.2f);
        }

        @Override
        protected void front(Batch batch) {
            float size = bossWidth() * 0.24f;
            for (int k = 0; k < blockX.length; k++) {                            // les pierres de sa Tour qui tombent
                float fall = progress(landing(k) - 0.4f, landing(k));
                if (fall <= 0f) continue;
                float restY = bossBottom() + (k / 4) * size * 0.8f;
                float x = bossX() + blockX[k] * bossWidth();
                float y = MathUtils.lerp(worldHeight() + size, restY, Interpolation.pow2In.apply(fall));
                rect(batch, STONE_DARK, 1f, x - size / 2f, y, size, size * 0.7f);
                rect(batch, STONE, 1f, x - size / 2f + 3f, y + 3f, size - 6f, size * 0.7f - 6f);
            }
            // Son plan s'envole, se déroule, puis brûle d'or.
            float fly = progress(0.8f, CLIMAX_DEATH);
            if (fly > 0f && time < CLIMAX_DEATH + 0.5f) {
                float x = MathUtils.lerp(bossX() - bossWidth() * 0.3f, bossX(), fly);
                float y = MathUtils.lerp(bossBottom() + bossHeight() * 0.2f, worldHeight() * 0.78f, Interpolation.pow2Out.apply(fly));
                float w = MathUtils.lerp(bossWidth() * 0.3f, bossWidth() * 1.6f, progress(1.6f, CLIMAX_DEATH));
                float h = w * 0.55f, burn = progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.5f);
                rect(batch, PLAN, 1f - burn, x - w / 2f, y - h / 2f, w, h);
                for (int k = 1; k < 4; k++) rect(batch, CHALK, (1f - burn) * 0.8f, x - w / 2f + w * k / 4f, y - h * 0.35f, 2f, h * 0.7f);
                if (time >= CLIMAX_DEATH) glow(batch, x, y, w * 1.6f, GOLD, 0.7f * (1f - burn));
            }
            flash(batch, GOLD, CLIMAX_DEATH, 0.3f, 0.35f);
        }
    }
}
