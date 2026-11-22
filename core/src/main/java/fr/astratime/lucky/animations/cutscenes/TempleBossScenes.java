package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.assets.TemplePortraits;
import fr.astratime.lucky.assets.TemplePortraits.Prop;
import fr.astratime.lucky.entities.enemy.EnemyKind;

import java.util.Random;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les scènes de phase et de mort des quatre rois du Temple du Destin (10 lieux, Astra 2026-11-20 ; redessinées le
 * 2026-11-22 pour que l'image montre la phrase : le grand bourdon pendu au torii qui saoule l'écran, le dernier coup
 * qui le change en statue de bronze, les ciseaux du destin qui coupent le fil de la Tisseuse, les dés de sa couronne
 * qui roulent sans le Seigneur, la main du joueur dans les miroirs d'or, l'Idole qui s'effrite en tas de sable).
 */
final class TempleBossScenes {

    private TempleBossScenes() {}

    static BossScene phase(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case MAITRE_CLOCHES -> new BellPhase(kit);
            case TISSEUSE -> new WeaverPhase(kit);
            case SEIGNEUR_DES -> new DicePhase(kit);
            case IDOLE_OR -> new IdolPhase(kit);
            default -> null;
        };
    }

    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case MAITRE_CLOCHES -> new BellDeath(kit);
            case TISSEUSE -> new WeaverDeath(kit);
            case SEIGNEUR_DES -> new DiceDeath(kit);
            case IDOLE_OR -> new IdolDeath(kit);
            default -> null;
        };
    }

    static final Color BRONZE = hex("c87a3a"), AMBER = hex("ffd27a"), THREAD = hex("fff2a0"), RUBY = hex("e8323a");
    static final Color IVORY = hex("f6f0e2"), DUST = hex("c8b8a0"), STONE = hex("a89a88"), ROPE = hex("d8b878");

    /** Les yeux des portraits du Temple (pixels 13-14 et 20-21, lignes 15-16) ; le troisième œil de l'Idole. */
    static final float[] LEFT_EYE = {14f, 16f}, RIGHT_EYE = {21f, 16f}, THIRD_EYE = {17.5f, 12f};

    /** Un dé d'ivoire, face six. */
    static final String[] DIE = {
        "ooooooooo", "oiiiiiiio", "oiriiirio", "oiiiiiiio", "oiriiirio", "oiiiiiiio", "oiriiirio", "oiiiiiiio", "ooooooooo"};

    static final Color[] SAND_COLORS = {hex("3e352a"), hex("9a8a6c"), hex("7c6e56"), hex("5e5242")};

    /** Un grand bourdon pendu à la poutre du torii : où il pend, sa taille, son balancement. */
    private static float bellScale(BossScene scene) { return scene.worldHeight() * 0.24f / ManorBossScenes.BELL.length; }

    // -------------------------------------------------------------------------
    // Le Maître des cloches
    // -------------------------------------------------------------------------

    /**
     * « Le bourdon te saoule : Ivresse ! » : un grand bourdon de bronze pend à la poutre du torii ; il tire sa corde
     * quatre fois, le bourdon sonne, et à chaque coup l'image tangue un peu plus et se dédouble, comme quand on a
     * trop bu.
     */
    static final class BellPhase extends BossScene {
        private static final float[] TOLLS = {0.5f, 0.95f, 1.4f, 1.85f};
        private final TextureRegion bell;

        BellPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.MAITRE_CLOCHES, 2);
            bell = art("obhdc", ManorBossScenes.BELL_COLORS, ManorBossScenes.BELL);
        }

        private float pivotX()  { return decorX(255f); }
        private float pivotY()  { return decorY(64f); }
        private float drunk()   { return progress(TOLLS[0], CLIMAX_PHASE + 0.4f); }
        private float angle() {
            float swing = 0f;
            for (float toll : TOLLS) swing += gesture(toll - 0.22f, toll + 0.22f) * (toll == TOLLS[0] ? 1f : 0.9f);
            return 16f * MathUtils.sin((time - TOLLS[0] + 0.22f) * MathUtils.PI / 0.45f) * Math.min(1f, swing + progress(TOLLS[0], 2.4f) * 0.4f);
        }

        @Override
        protected void step(float delta) {
            for (float toll : TOLLS) if (at(toll)) { rumble(0.2f, 4f); sparks(pivotX(), pivotY() - bellScale(this) * 12f, 12, AMBER, 400f, 8f); }
            if (at(CLIMAX_PHASE)) rumble(0.6f, 12f);
        }

        @Override
        protected void back(Batch batch) {
            float sway = drunk();
            float pull = 0f;
            for (float toll : TOLLS) pull = Math.max(pull, gesture(toll - 0.2f, toll + 0.05f));
            poseY = -10f * pull;
            poseTilt = 6f * MathUtils.sin(time * 3.2f) * sway;
            poseTint = 0.25f * progress(1.2f, CLIMAX_PHASE);
            poseTintColor.set(BRONZE);
            if (sway > 0f) {                                                                 // la double vue : son image en double
                float off = bossWidth() * 0.12f * sway * (0.6f + 0.4f * MathUtils.sin(time * 2.3f));
                float w = bossWidth(), h = bossHeight();
                batch.setColor(1f, 1f, 1f, 0.3f * sway);
                batch.draw(boss, bossX() - w / 2f + off, bossBottom(), w / 2f, 0f, w, h, 1f, 1f, poseTilt * 1.4f);
                batch.draw(boss, bossX() - w / 2f - off, bossBottom(), w / 2f, 0f, w, h, 1f, 1f, -poseTilt);
            }
        }

        @Override
        protected void front(Batch batch) {
            float scale = bellScale(this), w = ManorBossScenes.BELL[0].length() * scale, h = ManorBossScenes.BELL.length * scale;
            float a = angle();
            line(batch, hex("2a1a10"), 1f, pivotX(), pivotY() + scale, pivotX(), pivotY() - scale * 0.5f, scale * 0.8f);
            batch.setColor(WHITE);
            batch.draw(bell, pivotX() - w / 2f, pivotY() - h, w / 2f, h, w, h, 1f, 1f, a);
            float lipX = pivotX() + MathUtils.sinDeg(-a) * h * 0.95f, lipY = pivotY() - MathUtils.cosDeg(a) * h * 0.95f;
            line(batch, ROPE, 1f, lipX, lipY, portraitX(28.5f), portraitY(9f), 5f);          // sa corde, jusqu'au bourdon
            for (int k = 0; k < TOLLS.length; k++) wave(batch, TOLLS[k], 0.9f, lipX, lipY + h * 0.4f, AMBER, h * (2.6f + 0.6f * k));
            if (drunk() > 0f) fill(batch, BRONZE, 0.1f * drunk() * pulse(2.5f, 0.3f));
            flash(batch, AMBER, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /**
     * « Le dernier coup de cloche s'éteint » : il tire une dernière fois, le bourdon sonne, et à chaque écho plus
     * faible il se fige un peu plus en statue de bronze ; le bourdon de sa tête (le vrai) se fend, glisse et roule
     * au sol, muet.
     */
    static final class BellDeath extends BossScene {
        private static final float TOLL = 0.6f, LOOSE = 2.9f, REST = 3.9f;
        private final TextureRegion bell, bald;
        private final Piece crown;
        private float crownX, crownY;
        private boolean loose;

        BellDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.MAITRE_CLOCHES, 0);
            bell = art("obhdc", ManorBossScenes.BELL_COLORS, ManorBossScenes.BELL);
            int[][] rest = TemplePortraits.without(EnemyKind.MAITRE_CLOCHES, Prop.BELL);
            crown = lift(TemplePortraits.pixels(EnemyKind.MAITRE_CLOCHES), rest);
            bald = pixels(rest);
        }

        @Override protected float textAt() { return 3.3f; }
        @Override protected void restart() { loose = false; }

        private float pivotX() { return decorX(255f); }
        private float pivotY() { return decorY(64f); }
        private float statue() { return progress(TOLL + 0.4f, LOOSE - 0.2f); }

        @Override
        protected void step(float delta) {
            if (at(TOLL)) rumble(0.4f, 6f);
            for (int k = 0; k < 4; k++) if (at(TOLL + 0.5f + k * 0.5f)) sparks(bossX(), bossY(), 6, BRONZE, 200f, 6f);
            if (at(LOOSE)) rumble(0.15f, 4f);
            if (at(REST)) { rumble(0.2f, 3f); sparks(bossX() - bossWidth() * 0.9f, groundY() + 10f, 14, BRONZE, 250f, 7f); }
        }

        @Override
        protected float decorLight() { return 0.62f - 0.25f * statue(); }

        @Override
        protected void back(Batch batch) {
            poseY = -10f * gesture(TOLL - 0.2f, TOLL + 0.05f);
            poseTint = 0.5f * statue();                                                    // il se fige en bronze
            poseTintColor.set(BRONZE);
            poseShadow = 0.6f * statue();
            if (time >= LOOSE && !loose && crown != null) { loose = true; crownX = pieceX(crown); crownY = pieceY(crown); wear(bald); }
        }

        @Override
        protected void front(Batch batch) {
            float scale = bellScale(this), w = ManorBossScenes.BELL[0].length() * scale, h = ManorBossScenes.BELL.length * scale;
            float a = 16f * MathUtils.sin((time - TOLL) * MathUtils.PI / 0.45f) * (time < TOLL - 0.2f ? 0f : (float) Math.exp(-Math.max(0f, time - TOLL) * 0.9f));
            line(batch, hex("2a1a10"), 1f, pivotX(), pivotY() + scale, pivotX(), pivotY() - scale * 0.5f, scale * 0.8f);
            batch.setColor(WHITE);
            batch.draw(bell, pivotX() - w / 2f, pivotY() - h, w / 2f, h, w, h, 1f, 1f, a);
            float lipX = pivotX() + MathUtils.sinDeg(-a) * h * 0.95f, lipY = pivotY() - MathUtils.cosDeg(a) * h * 0.95f;
            line(batch, ROPE, 1f, lipX, lipY, portraitX(28.5f), portraitY(9f), 5f);
            for (int k = 0; k < 5; k++) {                                                   // l'écho qui s'éteint, de plus en plus faible
                float at = TOLL + k * 0.5f;
                float life = progress(at, at + 1.2f);
                if (time >= at && life < 1f) {
                    float size = h * (2.8f + 0.4f * k) * Interpolation.pow2Out.apply(life);
                    glowRing(batch, lipX, lipY + h * 0.4f, size, AMBER, 0.8f * (1f - life) * (1f - k / 5f));
                }
            }
            if (crown != null) {
                if (time < LOOSE) {
                    attached(batch, crown, 1f);
                    if (time > 2.2f) crack(batch, portraitX(17f), portraitY(0.5f), -95f, bossScale() * 8f * progress(2.2f, LOOSE), 7L, hex("3a1a0a"), 1f, 4f);
                } else {                                                                     // il glisse de sa tête et roule au sol
                    float k = progress(LOOSE, REST), fall = Interpolation.bounceOut.apply(Math.min(1f, k * 1.6f));
                    float x = MathUtils.lerp(crownX, bossX() - bossWidth() * 0.9f, Interpolation.pow2Out.apply(k));
                    float y = MathUtils.lerp(crownY, groundY() + crown.height * bossScale() * 0.5f, fall);
                    draw(batch, crown.region, x, y, bossScale(), 200f * Interpolation.pow2Out.apply(k), WHITE, 1f);
                    draw(batch, crown.region, x, y, bossScale(), 200f * Interpolation.pow2Out.apply(k), BRONZE, 0.35f * statue());
                }
            }
        }

        private void glowRing(Batch batch, float x, float y, float size, Color color, float alpha) {
            additive(batch);
            batch.setColor(color.r, color.g, color.b, alpha);
            batch.draw(ring, x - size / 2f, y - size / 2f, size, size);
            normal(batch);
        }
    }

    // -------------------------------------------------------------------------
    // La Tisseuse de destins
    // -------------------------------------------------------------------------

    /** « Elle lit ton fil » : des fils d'or jaillissent d'elle vers les bords de l'écran, ses yeux-bobines s'allument. */
    static final class WeaverPhase extends BossScene {
        WeaverPhase(CutsceneKit kit) { super(kit, Moment.PHASE, EnemyKind.TISSEUSE, 2); }

        @Override
        protected void step(float delta) {
            if (at(0.6f)) rumble(0.2f, 4f);
            if (at(CLIMAX_PHASE)) { rumble(0.5f, 10f); sparks(bossX(), bossY(), 50, THREAD, 700f, 12f); }
        }

        @Override
        protected void back(Batch batch) {
            float spread = Interpolation.pow2Out.apply(progress(0.5f, CLIMAX_PHASE));
            for (int k = 0; k < 10; k++) {
                float angle = k * 36f + time * 10f, length = Math.max(worldWidth(), worldHeight()) * spread;
                line(batch, THREAD, 0.8f, bossX(), bossY(), bossX() + MathUtils.cosDeg(angle) * length,
                    bossY() + MathUtils.sinDeg(angle) * length, 4f);
            }
            poseY = 30f * progress(1.0f, CLIMAX_PHASE);
            poseTint = 0.2f * progress(1.4f, CLIMAX_PHASE);
            poseTintColor.set(THREAD);
        }

        @Override
        protected void front(Batch batch) {
            eyes(batch, progress(1.4f, CLIMAX_PHASE) * pulse(6f, 0.6f), AMBER, LEFT_EYE, RIGHT_EYE);
            flash(batch, THREAD, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /**
     * « Le fil de la Tisseuse est coupé » : elle est suspendue à cinq fils d'or ; les grands ciseaux du destin
     * descendent et les coupent un à un ; au dernier, son propre fil est tranché et elle se détricote, rang après
     * rang, en brins d'or qui s'envolent.
     */
    static final class WeaverDeath extends BossScene {
        private static final float FIRST = 0.5f, GAP = 0.35f, LAST = FIRST + 4 * GAP, UNRAVEL = LAST + 0.3f, DONE = 4.2f;
        private static final int[] ORDER = {0, 4, 1, 3, 2};
        private final TextureRegion open, shut;
        private final TextureRegion[] rows;

        WeaverDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.TISSEUSE, 0);
            Color[] gold = {hex("3a2a08"), hex("ffe08a"), hex("c4891e"), hex("8a1a22"), hex("ffffff")};
            open = art("osdrw", gold, ManorBossScenes.SCISSORS_OPEN);
            shut = art("osdrw", gold, ManorBossScenes.SCISSORS_SHUT);
            rows = new TextureRegion[boss.getRegionHeight()];
            for (int r = 0; r < rows.length; r++) rows[r] = new TextureRegion(boss, 0, r, boss.getRegionWidth(), 1);
        }

        @Override protected float textAt() { return UNRAVEL + 0.6f; }

        private float snip(int k)  { return FIRST + k * GAP; }
        private float topX(int k)  { return worldWidth() * (0.14f + 0.18f * k); }
        private float holdX(int k) { return bossX() + (k - 2) * bossWidth() * 0.22f; }
        private float holdY()      { return portraitY(2f); }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 5; k++) if (at(snip(k))) {
                rumble(0.06f, 3f + k);
                int t = ORDER[k];
                sparks(MathUtils.lerp(holdX(t), topX(t), 0.5f), MathUtils.lerp(holdY(), worldHeight(), 0.5f), 12, THREAD, 300f, 7f);
            }
            if (time > UNRAVEL && time < DONE) {                                            // les brins d'or qui s'envolent
                float k = progress(UNRAVEL, DONE - 0.4f);
                int row = Math.round(MathUtils.lerp(boss.getRegionHeight(), 0f, k));
                if (random.nextFloat() < delta * 60f) {
                    Particle strand = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth(), portraitY(row), 20f + 50f * random.nextFloat(),
                        200f + 200f * random.nextFloat(), 1.4f, 10f);
                    strand.color.set(THREAD);
                    glows.add(strand);
                }
            }
            if (at(UNRAVEL)) rumble(0.3f, 6f);
        }

        @Override
        protected void back(Batch batch) {
            for (int k = 0; k < 5; k++) {                                                    // les fils qui la tiennent
                float cut = time - snip(indexOf(k));
                if (cut < 0f) {
                    line(batch, THREAD, 0.9f, holdX(k), holdY(), topX(k), worldHeight() + MARGIN, 3f);
                } else if (cut < 0.8f) {                                                   // les deux bouts retombent
                    float midX = MathUtils.lerp(holdX(k), topX(k), 0.5f), midY = MathUtils.lerp(holdY(), worldHeight(), 0.5f);
                    float drop = Interpolation.pow2In.apply(cut / 0.8f);
                    line(batch, THREAD, 0.9f * (1f - drop), topX(k), worldHeight() + MARGIN, midX, midY - drop * worldHeight() * 0.3f, 3f);
                    line(batch, THREAD, 0.9f * (1f - drop), holdX(k), holdY(), midX, midY - drop * worldHeight() * 0.6f, 3f);
                }
            }
            poseY = -14f * progress(LAST, UNRAVEL);
            showBoss = false;
            float k = progress(UNRAVEL, DONE - 0.4f), scale = bossScale();                 // elle se détricote, du bas vers le haut
            int height = rows.length;
            float left = bossX() - boss.getRegionWidth() * scale / 2f;
            for (int r = 0; r < height; r++) {
                float from = (height - 1 - r) / (float) height;                             // la ligne du bas part la première
                float go = MathUtils.clamp((k - from * 0.85f) / 0.15f, 0f, 1f);
                float y = bossBottom() + poseY + (height - 1 - r) * scale;
                if (go >= 1f) continue;
                float dx = go * worldWidth() * 0.4f * ((r % 2 == 0) ? 1f : -1f), dy = go * worldHeight() * 0.1f;
                batch.setColor(1f, 1f, 1f, 1f - go);
                batch.draw(rows[r], left + dx, y + dy, boss.getRegionWidth() * scale, scale);
                if (go > 0f) {
                    additive(batch);
                    batch.setColor(THREAD.r, THREAD.g, THREAD.b, 0.8f * (1f - go));
                    batch.draw(rows[r], left + dx, y + dy, boss.getRegionWidth() * scale, scale);
                    normal(batch);
                }
            }
        }

        private int indexOf(int thread) {
            for (int k = 0; k < ORDER.length; k++) if (ORDER[k] == thread) return k;
            return 0;
        }

        @Override
        protected void front(Batch batch) {
            float scale = worldHeight() * 0.26f / ManorBossScenes.SCISSORS_OPEN.length;
            int current = Math.min(4, Math.max(0, (int) ((time - FIRST + GAP * 0.6f) / GAP)));
            if (time > FIRST - 0.5f && time < LAST + 0.6f) {                                 // les ciseaux du destin, d'un fil à l'autre
                int t = ORDER[current];
                float x = MathUtils.lerp(holdX(t), topX(t), 0.5f), y = MathUtils.lerp(holdY(), worldHeight(), 0.5f);
                float closing = 1f - Math.abs(time - snip(current)) / 0.12f;
                float appear = progress(FIRST - 0.5f, FIRST - 0.2f) * (1f - progress(LAST + 0.3f, LAST + 0.6f));
                draw(batch, closing > 0f ? shut : open, x, y, scale, -30f, WHITE, appear);
            }
            flash(batch, THREAD, UNRAVEL, 0.3f, 0.35f);
        }
    }

    // -------------------------------------------------------------------------
    // Le Seigneur des Dés
    // -------------------------------------------------------------------------

    /** « Les dés sont pipés » : deux dés géants tombent du ciel, rebondissent et s'arrêtent sur six ; il grandit, rouge. */
    static final class DicePhase extends BossScene {
        private final TextureRegion die;

        DicePhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.SEIGNEUR_DES, 2);
            die = art("oir", new Color[] {hex("1a1018"), IVORY, RUBY}, DIE);
        }

        @Override
        protected void step(float delta) {
            if (at(1.2f) || at(1.4f)) rumble(0.25f, 10f);
            if (at(CLIMAX_PHASE)) { rumble(0.5f, 12f); sparks(bossX(), bossY(), 50, RUBY, 700f, 14f); }
        }

        private float dieX(int k) { return bossX() + (k == 0 ? -1f : 1f) * bossWidth() * 0.9f; }

        @Override
        protected void back(Batch batch) {
            poseScale = 1f + 0.12f * progress(1.6f, CLIMAX_PHASE);
            poseTint = 0.35f * progress(1.6f, CLIMAX_PHASE);
            poseTintColor.set(RUBY);
        }

        @Override
        protected void front(Batch batch) {
            for (int k = 0; k < 2; k++) {
                float land = Interpolation.bounceOut.apply(progress(0.4f + k * 0.2f, 1.2f + k * 0.2f));
                float y = MathUtils.lerp(worldHeight() + 200f, groundY() + 80f, land);
                float spin = (1f - land) * 720f * (k == 0 ? 1f : -1f);
                draw(batch, die, dieX(k), y, fit(150f, 9), spin, WHITE, 1f);
            }
            eyes(batch, progress(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.2f) * pulse(7f, 0.7f), RUBY, LEFT_EYE, RIGHT_EYE);
            flash(batch, RUBY, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /**
     * « Les dés du Seigneur roulent sans lui » : il chancelle, les deux dés d'or de sa couronne (les vrais)
     * tombent ; il s'effondre dans ses robes et disparaît derrière les grands dés de la salle, pendant que ses
     * deux dés roulent seuls sur le damier jusqu'à nous et s'arrêtent.
     */
    static final class DiceDeath extends BossScene {
        private static final float DROP = 0.8f, ROLL = 3.0f;
        private final TextureRegion bare;
        private final Piece left, right;
        private final float[] startX = new float[2], startY = new float[2];
        private boolean dropped;

        DiceDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.SEIGNEUR_DES, 0);
            int[][] full = TemplePortraits.pixels(EnemyKind.SEIGNEUR_DES);
            int[][] rest = TemplePortraits.without(EnemyKind.SEIGNEUR_DES, Prop.DIE_LEFT);
            int[][] none = TemplePortraits.without(EnemyKind.SEIGNEUR_DES, Prop.DIE_LEFT, Prop.DIE_RIGHT);
            left = lift(full, rest);
            right = lift(rest, none);
            bare = pixels(none);
        }

        @Override protected float textAt() { return 3.3f; }
        @Override protected void restart() { dropped = false; }

        @Override
        protected void step(float delta) {
            if (at(DROP)) rumble(0.1f, 3f);
            if (at(CLIMAX_DEATH)) rumble(0.5f, 10f);
            for (int k = 0; k < 2; k++) {
                for (int b = 0; b < 4; b++) if (at(DROP + 0.5f + k * 0.12f + b * 0.55f)) rumble(0.04f, 2f);
            }
        }

        @Override
        protected void back(Batch batch) {
            poseTilt = 6f * MathUtils.sin(time * 7f) * (1f - progress(1.4f, 1.6f)) * progress(0.1f, 0.5f);
            float collapse = Interpolation.pow2In.apply(progress(1.6f, CLIMAX_DEATH + 0.6f));   // il s'effondre dans ses robes
            poseSink = collapse;
            poseStretch = -0.2f * collapse;
            poseShadow = 0.4f * collapse;
            showBoss = collapse < 0.99f;
            if (time >= DROP && !dropped) {                                                  // ses vrais dés quittent sa couronne
                dropped = true;
                Piece[] dice = {left, right};
                for (int k = 0; k < 2; k++) if (dice[k] != null) { startX[k] = pieceX(dice[k]); startY[k] = pieceY(dice[k]); }
                wear(bare);
            }
        }

        /** Un dé qui roule seul sur le damier : il tourne d'un quart de tour à chaque rebond, puis s'arrête. */
        private void roll(Batch batch, Piece die, int k) {
            float from = DROP + k * 0.12f, k01 = progress(from, from + ROLL);
            float eased = Interpolation.pow2Out.apply(k01);
            float x = MathUtils.lerp(startX[k], bossX() + (k == 0 ? -1f : 1f) * worldWidth() * 0.22f, eased);
            float ground = groundY() * (1f - 0.55f * eased) + die.height * bossScale() * (1f + 0.6f * eased) * 0.5f;
            float hop = Math.abs(MathUtils.sin(eased * MathUtils.PI * 4f)) * worldHeight() * 0.06f * (1f - eased);
            float fall = Interpolation.pow2In.apply(progress(from, from + 0.35f));
            float y = k01 < 0.12f ? MathUtils.lerp(startY[k], ground, fall) : ground + hop;
            float spin = (k == 0 ? 1f : -1f) * 90f * Math.round(eased * 8f) + (k == 0 ? 1f : -1f) * 90f * ((eased * 8f) % 1f) * (1f - eased);
            draw(batch, die.region, x, y, bossScale() * (1f + 0.6f * eased), spin, WHITE, 1f);
        }

        @Override
        protected void front(Batch batch) {
            Piece[] dice = {left, right};
            if (time < DROP) {
                for (Piece die : dice) attached(batch, die, 1f);
                return;
            }
            for (int k = 0; k < 2; k++) if (dice[k] != null) roll(batch, dice[k], k);
        }
    }

    // -------------------------------------------------------------------------
    // L'Idole d'Or
    // -------------------------------------------------------------------------

    /**
     * « Son or te renvoie ton image : Reflet ! » : l'Idole s'éveille, deux grands miroirs d'or se dressent à ses
     * côtés ; un éclat les balaie et ils montrent ta main, tes cartes, à l'envers comme dans un miroir ; son
     * troisième œil s'ouvre.
     */
    static final class IdolPhase extends BossScene {
        private static final String[] HAND = {"cards/light/1-P.png", "cards/light/13-C.png", "cards/light/12-H.png", "cards/light/7-D.png"};
        private final TextureRegion[] cards = new TextureRegion[HAND.length];

        IdolPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.IDOLE_OR, 2);
            for (int k = 0; k < HAND.length; k++) cards[k] = gameCard(HAND[k]);
        }

        private float mirrorX(int s) { return bossX() + s * bossWidth() * 1.25f; }
        private float mirrorY()      { return bossBottom() + bossHeight() * 0.5f; }
        private float mirrorW()      { return bossWidth() * 0.9f; }
        private float mirrorH()      { return bossHeight() * 0.95f; }

        @Override
        protected void step(float delta) {
            if (at(0.5f)) rumble(0.3f, 6f);
            if (at(CLIMAX_PHASE)) { rumble(0.6f, 12f); sparks(bossX(), bossY(), 60, GOLD, 800f, 14f); }
        }

        @Override
        protected void back(Batch batch) {
            float halo = progress(0.4f, CLIMAX_PHASE);
            glow(batch, bossX(), bossY(), bossHeight() * (1.2f + 0.4f * halo), GOLD, 0.45f * halo * pulse(4f, 0.7f));
            float rise = Interpolation.pow2Out.apply(progress(0.4f, 1.0f));
            for (int s = -1; s <= 1; s += 2) {                                               // les deux miroirs d'or
                float x = mirrorX(s), y = mirrorY() - (1f - rise) * worldHeight() * 0.6f, w = mirrorW(), h = mirrorH();
                rect(batch, hex("3a2a08"), 1f, x - w / 2f - 14f, y - h / 2f - 14f, w + 28f, h + 28f);
                rect(batch, GOLD, 1f, x - w / 2f - 10f, y - h / 2f - 10f, w + 20f, h + 20f);
                rect(batch, hex("c4891e"), 1f, x - w / 2f - 10f, y - h / 2f - 10f, 8f, h + 20f);
                rect(batch, hex("5a4a2a"), 1f, x - w / 2f, y - h / 2f, w, h);
                float show = progress(1.0f, 1.6f);                                           // ta main, à l'envers
                float cardScale = h * 0.42f / 27f;
                for (int k = 0; k < cards.length; k++) {
                    float a = (k - 1.5f) * 14f, cx = x + (k - 1.5f) * w * 0.17f, cy = y - h * 0.08f + Math.abs(k - 1.5f) * -h * 0.03f;
                    batch.setColor(1f, 0.95f, 0.75f, 0.85f * show);
                    float cw = 19f * cardScale, ch = 27f * cardScale;
                    batch.draw(cards[k], cx + cw / 2f, cy - ch / 2f, -cw / 2f, ch / 2f, -cw, ch, 1f, 1f, -a);   // retournée comme dans un miroir
                }
                rect(batch, GOLD, 0.18f * show, x - w / 2f, y - h / 2f, w, h);                // le reflet doré par-dessus
                float sweep = progress(1.2f + (s + 1) * 0.1f, 1.8f + (s + 1) * 0.1f);       // l'éclat qui balaie le miroir
                if (sweep > 0f && sweep < 1f) {
                    float sx = x - w / 2f + w * sweep;
                    line(batch, WHITE, 0.6f, sx - 20f, y - h / 2f, sx + 20f, y + h / 2f, 18f);
                }
            }
            poseY = 20f * progress(0.8f, CLIMAX_PHASE);
            poseTint = 0.25f * progress(1.0f, CLIMAX_PHASE);
            poseTintColor.set(GOLD);
        }

        @Override
        protected void front(Batch batch) {
            eyes(batch, progress(1.6f, 2.0f) * pulse(8f, 0.6f), RUBY, THIRD_EYE);
            wave(batch, CLIMAX_PHASE, 1.0f, bossX(), bossY(), GOLD, bossHeight() * 2.6f);
            flash(batch, GOLD, CLIMAX_PHASE, 0.3f, 0.4f);
        }
    }

    /**
     * « L'Idole d'Or retourne à la poussière » : son or se ternit en pierre et se fend, puis elle s'effrite du haut
     * vers le bas : chaque grain de son corps tombe et forme un tas de sable à ses pieds, sous un nuage de poussière.
     */
    static final class IdolDeath extends BossScene {
        private static final float CRUMBLE = 1.9f, PILE = 4.2f;
        private final int[][] grid;
        private final float[] delay, drift;
        private final TextureRegion heap;

        IdolDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.IDOLE_OR, 0);
            grid = TemplePortraits.pixels(EnemyKind.IDOLE_OR);
            int w = grid[0].length, h = grid.length;
            delay = new float[w * h];
            drift = new float[w * h];
            Random seed = new Random(9);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    delay[y * w + x] = 1.3f * y / h + 0.35f * seed.nextFloat();              // elle s'effrite du haut vers le bas
                    drift[y * w + x] = (seed.nextFloat() - 0.5f) * 1.2f;
                }
            }
            heap = art("owsd", SAND_COLORS, PalaceBossScenes.snowHeap(40, 14));
        }

        @Override protected float textAt() { return 3.3f; }

        private final Color grain = new Color();

        private float heapScale() { return bossWidth() * 1.4f / 40f; }

        @Override
        protected void step(float delta) {
            if (at(0.6f)) rumble(0.3f, 5f);
            if (at(CRUMBLE)) rumble(1.6f, 5f);
            if (time > CRUMBLE && time < PILE && random.nextFloat() < delta * 20f) {
                Particle dust = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth() * 1.4f, groundY() + 30f, 90f, 80f, 1.6f, 140f);
                dust.color.set(DUST.r, DUST.g, DUST.b, 0.12f);
                glows.add(dust);
            }
        }

        @Override
        protected void back(Batch batch) {
            poseTint = 0.4f * progress(0.3f, CRUMBLE);                                     // l'or se ternit en pierre
            poseTintColor.set(STONE);
            poseShadow = 0.55f * progress(0.3f, CRUMBLE);
            showBoss = time < CRUMBLE;
        }

        @Override
        protected void front(Batch batch) {
            if (time < CRUMBLE + 0.1f) {
                for (int k = 0; k < 3; k++) {
                    crack(batch, bossX() + (k - 1) * bossWidth() * 0.25f, bossY() + bossHeight() * 0.2f, -70f - 20f * k,
                        bossHeight() * 0.5f * progress(0.6f + k * 0.3f, CRUMBLE), 11L + k, hex("3a2a08"), 1f, 6f);
                }
            }
            float grow = Interpolation.pow2Out.apply(progress(CRUMBLE + 0.3f, PILE));
            if (time >= CRUMBLE) {                                                          // les grains, du haut vers le bas
                float scale = bossScale(), x0 = bossX() - grid[0].length * scale / 2f;
                int w = grid[0].length, h = grid.length;
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        int c = grid[y][x];
                        if (c == 0) continue;
                        float t = time - CRUMBLE - delay[y * w + x];
                        float px = x0 + x * scale, py = bossBottom() + (h - 1 - y) * scale;
                        float alpha = 1f;
                        if (t > 0f) {
                            px += drift[y * w + x] * scale * 6f * t + (bossX() - px) * Math.min(1f, t * 0.6f) * 0.5f;
                            py -= 0.5f * 1800f * t * t;
                            float floor = groundY() + 12f * scale * grow;
                            if (py < floor) continue;                                        // il a rejoint le tas
                        }
                        float stone = Math.min(1f, 0.7f + 0.3f * Math.max(0f, t));
                        float r = ((c >>> 24) & 0xff) / 255f, g = ((c >>> 16) & 0xff) / 255f, b = ((c >>> 8) & 0xff) / 255f;
                        r = MathUtils.lerp(r, STONE.r, stone); g = MathUtils.lerp(g, STONE.g, stone); b = MathUtils.lerp(b, STONE.b, stone);
                        rect(batch, grain.set(r * 0.6f, g * 0.6f, b * 0.6f, 1f), alpha, px, py, scale, scale);
                    }
                }
            }
            if (grow > 0f) {
                float scale = heapScale();
                stretch(batch, heap, bossX(), groundY() - 4f + 14f * scale * grow / 2f, scale, scale * grow, 0f, WHITE, 1f);
            }
            flash(batch, DUST, CRUMBLE, 0.2f, 0.25f);
        }
    }
}
