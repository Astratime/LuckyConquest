package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les cinématiques du Prophète du Cratère, boss du chapitre 3 de la Tour, « Les Pèlerins de la Comète »
 * (Tour à 10 chapitres, 2026-11-22) :
 * <ul>
 *   <li>Entrée : la route de nuit vers le cratère, une file de lanternes. Les pèlerins s'arrêtent et s'agenouillent ;
 *   il monte sur un rocher au milieu d'eux, lève son bâton : la comète passe dans le ciel et son auréole s'allume.</li>
 *   <li>Phase 2 : « La comète reviendra » : il tend les bras au ciel, une étoile d'or grossit au-dessus de lui et
 *   s'écrase derrière lui, ses yeux s'embrasent.</li>
 *   <li>Mort : les lanternes s'éteignent une à une ; son auréole se détache, remonte vers le ciel et file comme une
 *   étoile filante ; il tombe à genoux, puis en poussière d'or.</li>
 * </ul>
 */
final class PilgrimScenes {

    private PilgrimScenes() {}

    static final Color NIGHT = hex("0c0a1e"), DUSK = hex("3a2448"), ROAD = hex("3a2e28"), HILL = hex("221a1a");
    static final Color LANTERN = hex("ffd27a"), CRATER = hex("e8a830"), ROBE = hex("f0e6cc"), CLOAK = hex("5a3e2a");
    static final Color COMET = hex("ffd54a"), TAIL = hex("a898c8"), CYAN = hex("7ae8ff");

    static BossScene phase(CutsceneKit kit, EnemyKind kind) {
        return kind == EnemyKind.PROPHETE ? new ProphetPhase(kit) : null;
    }

    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        return kind == EnemyKind.PROPHETE ? new ProphetDeath(kit) : null;
    }

    // -------------------------------------------------------------------------
    // Entrée
    // -------------------------------------------------------------------------

    /** L'entrée du Prophète : les pèlerins s'agenouillent, il lève son bâton, la comète passe. */
    static final class ProphetEntrance extends KingEntrance {
        /** Il monte sur le rocher, les pèlerins s'agenouillent, la comète passe. */
        static final float RISE = 0.4f, KNEEL = 2.0f, COMET_PASS = 3.4f;
        private final Starfield stars = new Starfield(120, 0.45f);

        ProphetEntrance(CutsceneKit kit) { super(kit, EnemyKind.PROPHETE); }

        @Override
        protected void stepPrelude(float delta) {
            if (at(1.2f)) rumble(0.1f, 2f);
        }

        @Override
        protected void prelude(Batch batch) {
            drawRoad(batch, 1f, 0f);
        }

        @Override
        protected void step(float delta) {
            if (at(KNEEL)) rumble(0.2f, 4f);
            if (at(COMET_PASS)) {
                rumble(0.5f, 10f);
                sparks(cx(), kingY() + 46f * KING_SCALE, 50, COMET, 600f, 12f);
                sparks(kingPixelX(2f, cx(), KING_SCALE), kingPixelY(3f, kingY(), KING_SCALE), 30, CYAN, 500f, 10f);
            }
        }

        @Override
        protected void draw(Batch batch) {
            float kneel = progress(KNEEL - 0.4f, KNEEL + 0.4f);
            drawRoad(batch, 1f - 0.7f * kneel, progress(COMET_PASS - 0.6f, COMET_PASS + 1.2f));
            // Le rocher où il monte.
            rect(batch, hex("2a2224"), cx() - 260f, kingY() - 60f, 520f, 70f);
            rect(batch, hex("3a3034"), cx() - 220f, kingY() - 10f, 440f, 20f);
            float rise = Interpolation.pow2Out.apply(progress(RISE, RISE + 1.0f));
            float lift = gesture(COMET_PASS - 0.3f, COMET_PASS + 1.4f);
            pose(0f, -200f * (1f - rise), 0f, 0.08f * lift);
            drawKing(batch, cx(), kingY(), KING_SCALE, 1f - rise, rise);
            // Son auréole et ses yeux s'allument quand la comète passe.
            float light = progress(COMET_PASS, COMET_PASS + 0.4f) * pulse(4f, 0.7f);
            if (light > 0f) {
                glow(batch, kingPixelX(17f, cx(), KING_SCALE), kingPixelY(5f, kingY(), KING_SCALE), 260f, COMET, 0.6f * light);
                for (float px : new float[] {13.5f, 20.5f}) {
                    glow(batch, kingPixelX(px, cx(), KING_SCALE), kingPixelY(15.5f, kingY(), KING_SCALE), 90f, COMET, light);
                }
                glow(batch, kingPixelX(2f, cx(), KING_SCALE), kingPixelY(4f, kingY(), KING_SCALE), 160f, CYAN, light);
            }
            flash(batch, COMET, COMET_PASS, 0.25f, 0.35f);
        }

        /**
         * La route de nuit vers le cratère et la file des pèlerins : debout ({@code standing} = 1) ou agenouillés ;
         * {@code comet} : la comète qui traverse le ciel, de 0 à 1.
         */
        private void drawRoad(Batch batch, float standing, float comet) {
            float width = worldWidth(), height = worldHeight();
            gradient(batch, DUSK, NIGHT);
            stars.draw(batch, 1f);
            if (comet > 0f && comet < 1f) {                                   // la comète qui passe
                float x = width * (1.1f - 1.3f * comet), y = height * (0.92f - 0.12f * comet);
                line(batch, TAIL, x, y, x + 420f, y + 70f, 6f);
                line(batch, hex("fff6c8"), x, y, x + 200f, y + 34f, 3f);
                glow(batch, x, y, 120f, COMET, 0.9f);
            }
            glow(batch, width * 0.78f, height * 0.42f, 520f, CRATER, 0.35f); // la lueur du cratère
            rect(batch, HILL, 0f, 0f, width, height * 0.42f);
            // La route qui monte vers le cratère.
            for (int k = 0; k < 12; k++) {
                float u = k / 11f;
                float w = MathUtils.lerp(width * 0.5f, 40f, u);
                rect(batch, ROAD, MathUtils.lerp(width * 0.25f, width * 0.76f, u) - w / 2f, height * 0.42f * (1f - u),
                    w, height * 0.42f / 11f + 2f);
            }
            // Les pèlerins, deux files de part et d'autre, chacun sa lanterne.
            for (int k = 0; k < 10; k++) {
                float u = k / 10f;
                float size = MathUtils.lerp(70f, 16f, u);
                float h = size * MathUtils.lerp(0.55f, 1.6f, standing);
                for (int side = -1; side <= 1; side += 2) {
                    float x = MathUtils.lerp(width * 0.25f, width * 0.76f, u) + side * MathUtils.lerp(width * 0.2f, 30f, u);
                    float y = height * 0.42f * (1f - u) - size * 0.3f;
                    rect(batch, (k + side) % 3 == 0 ? CLOAK : ROBE, x - size * 0.3f, y, size * 0.6f, h);
                    rect(batch, (k + side) % 3 == 0 ? hex("3a2618") : hex("c8b890"), x - size * 0.25f, y + h, size * 0.5f, size * 0.4f);
                    glow(batch, x + side * size * 0.5f, y + h * 0.6f, size * 1.4f * pulse(9f + k, 0.8f), LANTERN, 0.7f);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Phase 2 et mort
    // -------------------------------------------------------------------------

    /** « La comète reviendra » : il tend les bras, une étoile d'or grossit au ciel et tombe derrière lui. */
    static final class ProphetPhase extends BossScene {
        ProphetPhase(CutsceneKit kit) { super(kit, Moment.PHASE, EnemyKind.PROPHETE, 2); }

        private float starY() {
            float fall = Interpolation.pow3In.apply(progress(1.2f, CLIMAX_PHASE));
            return MathUtils.lerp(worldHeight() * 0.95f, bossBottom(), fall);
        }

        @Override
        protected void step(float delta) {
            if (time > 0.3f && time < CLIMAX_PHASE && random.nextFloat() < delta * 20f) {
                sparks(bossX() + worldWidth() * 0.18f, starY(), 3, COMET, 200f, 8f);
            }
            if (at(CLIMAX_PHASE)) {
                rumble(0.7f, 14f);
                sparks(bossX() + worldWidth() * 0.18f, bossBottom(), 80, COMET, 900f, 16f);
                sparks(bossX(), bossY(), 40, HOT, 600f, 12f);
            }
        }

        @Override
        protected void back(Batch batch) {
            fill(batch, Color.BLACK, 0.45f * progress(0.1f, 0.8f));
            if (time < CLIMAX_PHASE + 0.1f) {
                float x = bossX() + worldWidth() * 0.18f;
                float size = MathUtils.lerp(40f, 260f, progress(0.3f, CLIMAX_PHASE));
                line(batch, TAIL, 0.6f, x, starY(), x + 60f, starY() + 400f, 10f);
                glow(batch, x, starY(), size, COMET, 0.9f);
            }
            if (time >= CLIMAX_PHASE) glow(batch, bossX(), bossBottom(), worldWidth() * 0.6f, CRATER, 0.4f * pulse(4f, 0.6f));
            poseY = 24f * gesture(0.2f, CLIMAX_PHASE + 0.4f);
            poseStretch = 0.08f * progress(0.2f, 1.0f);
            poseTint = 0.4f * progress(CLIMAX_PHASE - 0.2f, CLIMAX_PHASE + 0.3f) * pulse(6f, 0.6f);
            poseTintColor.set(COMET);
        }

        @Override
        protected void front(Batch batch) {
            if (time >= CLIMAX_PHASE) eyes(batch, pulse(6f, 0.6f), COMET, new float[] {13.5f, 15.5f}, new float[] {20.5f, 15.5f});
            flash(batch, COMET, CLIMAX_PHASE, 0.3f, 0.5f);
        }
    }

    /** Les lanternes s'éteignent, son auréole file au ciel en étoile filante, il tombe en poussière d'or. */
    static final class ProphetDeath extends BossScene {
        ProphetDeath(CutsceneKit kit) { super(kit, Moment.DEATH, EnemyKind.PROPHETE, 0); }

        private float out(int k) { return 0.2f + k * 0.18f; }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 8; k++) if (at(out(k))) sparks(lanternX(k), lanternY(k), 6, LANTERN, 120f, 6f);
            if (time > 1.6f && time < 3.6f && random.nextFloat() < delta * 40f) {
                sparks(bossX() + (random.nextFloat() - 0.5f) * bossWidth(), bossBottom() + bossHeight() * random.nextFloat(), 3,
                    COMET, 200f, 8f);
            }
            if (at(CLIMAX_DEATH)) {
                rumble(0.5f, 8f);
                spray(coin, bossX(), bossY(), 24, 40f, 140f, 800f, coinScale(0.04f), 1400f, 1.5f);
            }
        }

        private float lanternX(int k) { return worldWidth() * (0.08f + k * 0.12f); }
        private float lanternY(int k) { return bossBottom() + 40f + (k % 2) * 30f; }

        @Override
        protected void back(Batch batch) {
            for (int k = 0; k < 8; k++) {
                if (time < out(k)) glow(batch, lanternX(k), lanternY(k), 110f * pulse(10f + k, 0.8f), LANTERN, 0.7f);
            }
            // L'auréole qui remonte et file en étoile filante.
            float up = Interpolation.pow2In.apply(progress(0.8f, 2.6f));
            float hx = bossX() + up * worldWidth() * 0.4f, hy = bossY() + bossHeight() * 0.45f + up * worldHeight() * 0.6f;
            if (time < 2.8f) {
                if (up > 0.05f) line(batch, TAIL, 0.6f * up, hx, hy, hx - 300f * up, hy - 160f * up, 6f);
                glow(batch, hx, hy, 120f, COMET, 0.9f);
            }
            poseSink = 0.35f * progress(1.4f, 2.2f);
            poseTint = 0.7f * progress(1.6f, 3.0f);
            poseTintColor.set(COMET);
            poseAlpha = 1f - progress(2.4f, 3.6f);
            showBoss = time < 3.7f;
        }

        @Override
        protected void front(Batch batch) { flash(batch, COMET, CLIMAX_DEATH, 0.3f, 0.35f); }
    }
}
