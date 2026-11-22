package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import fr.astratime.lucky.assets.EnemyTextures;
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
                sparks(kingPixelX(2.5f, cx(), KING_SCALE), kingPixelY(5f, kingY(), KING_SCALE), 30, CYAN, 500f, 10f);
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
                glow(batch, kingPixelX(17.5f, cx(), KING_SCALE), kingPixelY(5.5f, kingY(), KING_SCALE), 260f, COMET, 0.6f * light);
                for (float px : new float[] {14f, 21f}) {
                    glow(batch, kingPixelX(px, cx(), KING_SCALE), kingPixelY(16f, kingY(), KING_SCALE), 90f, COMET, light);
                }
                glow(batch, kingPixelX(2.5f, cx(), KING_SCALE), kingPixelY(5f, kingY(), KING_SCALE), 160f, CYAN, light);
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

    /**
     * Socle des scènes du Prophète, devant le vrai décor des Pèlerins : la comète du ciel (sa tête dorée et sa
     * traîne, en pixels du décor) et les neuf lanternes de la file des pèlerins, qu'une scène éteint ou rallume.
     */
    abstract static class ProphetScene extends BossScene {
        /** La tête de la comète du décor (son centre, en pixels du décor) et les deux bouts de sa traîne. */
        static final float HEAD_X = 264f, HEAD_Y = 46.5f;
        static final int TAIL_FROM = 38, TAIL_TO = 256;
        static final float TAIL_FROM_Y = 20f, TAIL_TO_Y = 45.5f;
        /** Les vitres des lanternes des pèlerins (coin haut-gauche, 6 x 11 pixels du décor), de la plus proche à la plus loin. */
        static final int[][] LANTERNS = {{75, 145}, {97, 141}, {118, 136}, {140, 132}, {162, 128}, {184, 123}, {205, 119},
            {227, 114}, {249, 110}};
        static final Color GLASS_OUT = hex("2e2622");
        /** La main levée du Prophète, en pixels de son portrait (colonnes 28 à 31, lignes 16 à 19). */
        static final float HAND_X = 30f, HAND_Y = 18f;
        private final TextureRegion tailPatch, headPatch;
        private final int tailTop, headTop;

        ProphetScene(CutsceneKit kit, Moment moment, int phase) {
            super(kit, moment, EnemyKind.PROPHETE, phase);
            tailTop = DecorPatches.top(TAIL_FROM_Y, TAIL_TO_Y, 3);
            headTop = DecorPatches.top(HEAD_Y, HEAD_Y, 9);
            com.badlogic.gdx.graphics.Pixmap decor = DecorPatches.read(EnemyKind.PROPHETE);
            if (decor == null) {
                tailPatch = headPatch = null;
                return;
            }
            tailPatch = region(DecorPatches.upright(decor, TAIL_FROM, TAIL_FROM_Y, TAIL_TO, TAIL_TO_Y, 3), false);
            headPatch = region(DecorPatches.upright(decor, TAIL_TO - 1, HEAD_Y, 274, HEAD_Y, 9), false);
            decor.dispose();
        }

        /** @return la tête de la comète du décor, à l'écran. */
        protected float headX() { return decorX(HEAD_X); }
        protected float headY() { return decorY(HEAD_Y); }

        /** La comète quitte le ciel du décor : on peint le ciel par-dessus sa tête et sa traîne. */
        protected void eraseComet(Batch batch, float alpha) {
            DecorPatches.draw(this, batch, tailPatch, TAIL_FROM, tailTop, alpha);
            DecorPatches.draw(this, batch, headPatch, TAIL_TO - 1, headTop, alpha);
        }

        protected float lanternX(int k) { return decorX(LANTERNS[k][0] + 3f); }
        protected float lanternY(int k) { return decorY(LANTERNS[k][1] + 5.5f); }

        /** La lanterne {@code k} éteinte : sa vitre noircie. */
        protected void lanternOut(Batch batch, int k) { decorRect(batch, GLASS_OUT, LANTERNS[k][0], LANTERNS[k][1], 6, 11); }

        /** La lueur de la lanterne {@code k}. */
        protected void lanternGlow(Batch batch, int k, float alpha) {
            glow(batch, lanternX(k), lanternY(k), decorScale() * 22f * pulse(9f + k, 0.8f), LANTERN, alpha);
        }

        /** Une comète en ({@code x}, {@code y}) qui file vers ({@code dx}, {@code dy}) : sa tête et sa traîne. */
        protected void comet(Batch batch, float x, float y, float dx, float dy, float size, float tail, float alpha) {
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            if (length < 0.001f) { dx = 1f; dy = 0f; length = 1f; }
            float ux = -dx / length, uy = -dy / length;
            line(batch, TAIL, 0.55f * alpha, x, y, x + ux * tail, y + uy * tail, size * 0.22f);
            line(batch, hex("fff6c8"), 0.8f * alpha, x, y, x + ux * tail * 0.55f, y + uy * tail * 0.55f, size * 0.09f);
            for (int i = 1; i <= 6; i++) {
                float k = i / 6f;
                glow(batch, x + ux * tail * k, y + uy * tail * k, size * (1.1f - 0.8f * k), COMET, 0.35f * alpha * (1f - k));
            }
            glow(batch, x, y, size * 2.4f, COMET, 0.8f * alpha);
            glow(batch, x, y, size * 0.8f, HOT, alpha);
        }
    }

    /**
     * « La comète répond à son appel » : il lève la main, un fil d'or monte de sa main vers la comète du ciel, qui
     * flamboie, quitte le ciel du décor et plonge sur sa main levée ; elle éclate, les lanternes des pèlerins
     * flambent, et la Comète (le symbole qu'il gagne) reste au-dessus de sa main.
     */
    static final class ProphetPhase extends ProphetScene {
        static final float CALL = 0.35f, LEAVE = 1.1f;
        private final TextureRegion icon;

        ProphetPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, 2);
            icon = symbol(fr.astratime.lucky.entities.enemy.EnemySymbol.GOLDEN_COMET);
        }

        private float handX() { return portraitX(HAND_X); }
        private float handY() { return portraitY(HAND_Y); }

        /** @return où est la comète qui plonge : une courbe de sa place au ciel jusqu'à sa main. */
        private float cometX(float k) {
            float cx = MathUtils.lerp(headX(), handX(), 0.25f);
            return (1 - k) * (1 - k) * headX() + 2 * k * (1 - k) * cx + k * k * handX();
        }

        private float cometY(float k) {
            float cy = Math.max(headY(), handY()) + worldHeight() * 0.12f;
            return (1 - k) * (1 - k) * headY() + 2 * k * (1 - k) * cy + k * k * handY();
        }

        private float dive() { return Interpolation.pow2In.apply(progress(LEAVE, CLIMAX_PHASE)); }

        @Override
        protected void step(float delta) {
            if (at(CALL)) { rumble(0.15f, 3f); sparks(handX(), handY(), 14, COMET, 260f, 8f); }
            if (at(LEAVE)) { rumble(0.25f, 5f); sparks(headX(), headY(), 30, COMET, 420f, 10f); }
            if (time > LEAVE && time < CLIMAX_PHASE && random.nextFloat() < delta * 40f) {
                float k = dive();
                sparks(cometX(k), cometY(k), 2, COMET, 120f, 9f);
            }
            if (at(CLIMAX_PHASE)) {
                rumble(0.7f, 14f);
                sparks(handX(), handY(), 80, COMET, 900f, 16f);
                sparks(handX(), handY(), 40, HOT, 600f, 12f);
                for (int k = 0; k < LANTERNS.length; k++) sparks(lanternX(k), lanternY(k), 6, LANTERN, 200f, 7f);
            }
        }

        @Override
        protected void back(Batch batch) {
            fill(batch, Color.BLACK, 0.3f * progress(0.1f, 0.8f));
            if (time >= LEAVE) eraseComet(batch, progress(LEAVE, LEAVE + 0.12f));
            else glow(batch, headX(), headY(), decorScale() * (16f + 26f * progress(0.6f, LEAVE)) * pulse(10f, 0.7f), COMET,
                0.5f + 0.4f * progress(0.6f, LEAVE));
            float flare = time < CLIMAX_PHASE ? 0f : 1f - 0.6f * progress(CLIMAX_PHASE, CLIMAX_PHASE + 1.2f);
            for (int k = 0; k < LANTERNS.length; k++) lanternGlow(batch, k, 0.25f + 0.65f * flare);
            poseY = 20f * gesture(0.2f, CLIMAX_PHASE + 0.6f);
            poseStretch = 0.06f * progress(0.2f, 1.0f);
            poseTint = 0.45f * progress(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.3f) * pulse(6f, 0.6f);
            poseTintColor.set(COMET);
        }

        @Override
        protected void front(Batch batch) {
            float call = progress(CALL, CALL + 0.6f);                         // le fil d'or de son appel
            if (call > 0f && time < LEAVE + 0.2f) {
                float x = MathUtils.lerp(handX(), headX(), call), y = MathUtils.lerp(handY(), headY(), call);
                float a = 1f - progress(LEAVE, LEAVE + 0.2f);
                line(batch, COMET, 0.5f * a, handX(), handY(), x, y, 5f);
                line(batch, HOT, 0.8f * a, handX(), handY(), x, y, 2f);
                glow(batch, x, y, 60f, COMET, 0.8f * a);
            }
            glow(batch, handX(), handY(), 90f * pulse(7f, 0.6f), COMET, 0.5f * progress(CALL, CALL + 0.4f));
            if (time >= LEAVE && time < CLIMAX_PHASE) {                       // elle plonge sur sa main
                float k = dive(), k2 = Math.min(1f, k + 0.02f);
                float size = decorScale() * MathUtils.lerp(14f, 30f, k);
                comet(batch, cometX(k), cometY(k), cometX(k2) - cometX(k), cometY(k2) - cometY(k), size,
                    worldWidth() * MathUtils.lerp(0.12f, 0.3f, k), 1f);
            }
            if (time >= CLIMAX_PHASE) eyes(batch, pulse(6f, 0.6f), COMET, new float[] {14f, 16f}, new float[] {21f, 16f});
            wave(batch, CLIMAX_PHASE, 0.8f, handX(), handY(), COMET, worldWidth() * 1.2f);
            if (icon != null && time >= CLIMAX_PHASE) {                       // la Comète qu'il gagne, au-dessus de sa main
                float k = Interpolation.pow2Out.apply(progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.45f));
                float x = MathUtils.lerp(handX(), portraitX(40f), k), y = MathUtils.lerp(handY(), portraitY(9f), k);
                glow(batch, x, y, iconScale(0.3f) * pulse(5f, 0.7f), COMET, 0.6f * k);
                draw(batch, icon, x, y, iconScale(0.14f) * (0.4f + 0.6f * k), 0f, WHITE, k);
            }
            flash(batch, COMET, CLIMAX_PHASE, 0.3f, 0.5f);
        }
    }

    /**
     * « Le Prophète rejoint sa comète » : les lanternes des pèlerins s'éteignent une à une ; son auréole (la vraie,
     * ôtée du portrait) part la première vers la comète du ciel. Puis il s'élève lui-même, devient lumière et monte
     * jusqu'à la comète ; il s'y fond dans un éclat. La comète, plus grande, quitte le ciel du décor et file au loin ;
     * dans son sillage, les pèlerins rallument leurs lanternes.
     */
    static final class ProphetDeath extends ProphetScene {
        /** Quand l'auréole quitte sa tête et rejoint la comète, quand il s'élève ; le centre de l'auréole sur le portrait. */
        private static final float HALO_UP = 0.8f, HALO_IN = 1.8f, RISE = 1.0f, HALO_X = 26.5f, HALO_Y = 5.5f;
        /** Quand la comète, avec lui, s'en va, et quand elle a quitté l'écran. */
        private static final float AWAY = 4.4f;
        private final TextureRegion halo, withoutHalo;
        private boolean haloOff;
        private float haloLeft, haloBottom;

        ProphetDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, 0);
            halo = region(EnemyTextures.newProphetPart(true), false);
            withoutHalo = region(EnemyTextures.newProphetPart(false), false);
        }

        @Override protected void restart() { haloOff = false; }

        private float out(int k)    { return 0.25f + k * 0.11f; }
        private float relit(int k)  { return CLIMAX_DEATH + 0.5f + k * 0.1f; }
        private float rise()        { return Interpolation.pow2In.apply(progress(RISE, CLIMAX_DEATH)); }
        private float away()        { return Interpolation.pow2In.apply(progress(CLIMAX_DEATH + 0.2f, AWAY)); }
        private float awayX(float k) { return MathUtils.lerp(headX(), worldWidth() + 300f, k); }
        private float awayY(float k) { return MathUtils.lerp(headY(), worldHeight() + 120f, k * k); }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < LANTERNS.length; k++) {
                if (at(out(k))) sparks(lanternX(k), lanternY(k), 5, LANTERN, 90f, 6f);
                if (at(relit(k))) sparks(lanternX(k), lanternY(k), 8, LANTERN, 160f, 7f);
            }
            if (at(HALO_IN)) { rumble(0.2f, 4f); sparks(headX(), headY(), 30, COMET, 400f, 10f); }
            if (time > RISE && time < CLIMAX_DEATH && random.nextFloat() < delta * 40f) {
                sparks(bossX() + poseX, bossBottom() + poseY + bossHeight() * poseScale * 0.5f, 2, COMET, 160f, 9f);
            }
            if (at(CLIMAX_DEATH)) {                                           // il se fond dans sa comète
                rumble(0.6f, 10f);
                sparks(headX(), headY(), 90, COMET, 900f, 16f);
                sparks(headX(), headY(), 40, HOT, 600f, 12f);
            }
            if (time > CLIMAX_DEATH + 0.2f && time < AWAY && random.nextFloat() < delta * 50f) {
                float k = away();
                sparks(awayX(k), awayY(k), 2, COMET, 140f, 10f);
            }
        }

        @Override
        protected void back(Batch batch) {
            if (time >= HALO_UP && !haloOff) {                                // son auréole se détache du portrait
                haloOff = true;
                haloLeft = Float.NaN;
                wear(withoutHalo);
            }
            for (int k = 0; k < LANTERNS.length; k++) {
                if (time < out(k)) lanternGlow(batch, k, 0.6f);
                else if (time < relit(k)) lanternOut(batch, k);
                else lanternGlow(batch, k, 0.75f + 0.25f * (1f - progress(relit(k), relit(k) + 0.6f)));
            }
            if (time < CLIMAX_DEATH) {                                        // la comète du ciel l'attend
                float wait = progress(0.5f, CLIMAX_DEATH);
                glow(batch, headX(), headY(), decorScale() * (14f + 24f * wait) * pulse(8f, 0.75f), COMET, 0.4f + 0.5f * wait);
            } else {
                eraseComet(batch, 1f);
            }
            float k = rise();
            poseScale = MathUtils.lerp(1f, 0.12f, k);
            poseX = (headX() - bossX()) * k;
            poseY = (headY() - bossBottom() - bossHeight() * poseScale * 0.5f) * k;
            poseTint = progress(RISE - 0.4f, CLIMAX_DEATH - 0.4f);
            poseTintColor.set(COMET);
            showBoss = time < CLIMAX_DEATH;
        }

        @Override
        protected void front(Batch batch) {
            float scale = bossScale();
            if (!haloOff) {
                glow(batch, portraitX(HALO_X), portraitY(HALO_Y), 60f * pulse(5f, 0.7f), COMET, 0.6f);
            } else if (time < HALO_IN + 0.1f) {                              // la vraie auréole monte vers la comète
                if (Float.isNaN(haloLeft)) {
                    haloLeft = bossX() + poseX - boss.getRegionWidth() * scale * poseScale / 2f;
                    haloBottom = bossBottom() + poseY;
                }
                float up = Interpolation.pow2.apply(progress(HALO_UP, HALO_IN));
                float startX = haloLeft + HALO_X * scale, startY = haloBottom + (boss.getRegionHeight() - HALO_Y) * scale;
                float dx = (headX() - startX) * up, dy = (headY() - startY) * up;
                float shrink = 1f - 0.6f * up;
                float hx = startX + dx, hy = startY + dy;
                if (up > 0.05f) line(batch, TAIL, 0.6f * up, hx, hy, hx - dx * 0.4f, hy - dy * 0.4f, 6f);
                batch.setColor(1f, 1f, 1f, 1f - progress(HALO_IN - 0.15f, HALO_IN + 0.1f));
                float w = halo.getRegionWidth() * scale * shrink, h = halo.getRegionHeight() * scale * shrink;
                batch.draw(halo, hx - HALO_X * scale * shrink, hy - (halo.getRegionHeight() - HALO_Y) * scale * shrink, w, h);
                batch.setColor(Color.WHITE);
                glow(batch, hx, hy, 120f * (0.4f + 0.6f * up), COMET, 0.9f);
            }
            if (showBoss && time > RISE) {                                    // il devient lumière en montant
                float cx = bossX() + poseX, cy = bossBottom() + poseY + bossHeight() * poseScale * 0.5f;
                glow(batch, cx, cy, bossHeight() * poseScale * 2.2f, COMET, 0.6f * progress(RISE, RISE + 0.6f));
            }
            if (time >= CLIMAX_DEATH && time < AWAY + 0.1f) {                 // la comète, avec lui, file au loin
                float k = away(), k2 = Math.min(1f, k + 0.02f);
                float size = decorScale() * (20f + 10f * pulse(12f, 0.5f));
                float dx = awayX(k2) - awayX(k), dy = awayY(k2) - awayY(k);
                if (k < 0.02f) { dx = 1f; dy = 0.2f; }
                comet(batch, awayX(k), awayY(k), dx, dy, size, worldWidth() * MathUtils.lerp(0.2f, 0.45f, k), 1f);
            }
            if (time >= CLIMAX_DEATH) {                                       // son sillage d'or qui s'efface
                float k = away(), fade = 1f - progress(AWAY, DEATH_COVER_START);
                line(batch, COMET, 0.25f * fade, headX(), headY(), awayX(k), awayY(k), 6f);
            }
            wave(batch, CLIMAX_DEATH, 0.9f, headX(), headY(), COMET, worldWidth() * 0.9f);
            flash(batch, COMET, CLIMAX_DEATH, 0.3f, 0.4f);
        }
    }
}
