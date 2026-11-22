package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les scènes de phase et de mort de l'Architecte, boss du chapitre « L'Ascension » (Tour à 10 chapitres, 2026-11-22),
 * devant le vrai décor du chapitre : sa Tour, au milieu, qui monte vers le ciel avec ses marches de cartes.
 * Les scènes découpent la vraie Tour dans le décor (et peignent le ciel derrière) pour la faire frapper ou s'effondrer.
 */
final class AscensionBossScenes {

    private AscensionBossScenes() {}

    static BossScene phase(CutsceneKit kit, EnemyKind kind, int phase) {
        if (kind != EnemyKind.ARCHITECTE) return null;
        return phase == 2 ? new ArchitectScaffold(kit) : new ArchitectKeystone(kit);
    }

    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        return kind == EnemyKind.ARCHITECTE ? new ArchitectDeath(kit) : null;
    }

    static final Color CHALK = hex("e8f2ff"), PLAN = hex("2a56c8"), SPIRAL = hex("b88aff");
    /** Les pierres de la Tour du décor : leur face et leurs joints. */
    static final Color BRICK = hex("6a667f"), BRICK_DARK = hex("3d3a51"), DUST = hex("8a8494");
    static final Color WOOD = hex("b8864a"), WOOD_DARK = hex("6e4620"), WOOD_LIGHT = hex("d8a868");
    /** Les yeux de l'Architecte, en pixels de son portrait : l'œil nu, puis celui du monocle. */
    static final float[] EYE = {14f, 15f}, MONOCLE = {21f, 15f};  // centres des yeux 2 x 2 (colonnes 13-14 et 20-21, lignes 14-15)

    /**
     * Socle des scènes de l'Architecte : sa Tour, découpée dans le décor (colonnes {@link #LEFT} à {@link #RIGHT},
     * lignes 0 à {@link #BASE} - 1, avec les bouts de cartes qui en dépassent) et le ciel qu'elle cache.
     */
    abstract static class ArchitectScene extends BossScene {
        static final int LEFT = 119, RIGHT = 241, BASE = 160, SPILL = 16;
        /** La fenêtre allumée en haut de la Tour (son centre, en pixels du décor). */
        static final float WINDOW_X = 180.5f, WINDOW_Y = 24f;
        protected final TextureRegion sky, tower;

        ArchitectScene(CutsceneKit kit, Moment moment, int phase) {
            super(kit, moment, EnemyKind.ARCHITECTE, phase);
            Pixmap decor = DecorPatches.read(EnemyKind.ARCHITECTE);
            if (decor == null) {
                sky = tower = null;
                return;
            }
            sky = region(DecorPatches.sideways(decor, LEFT, 0, RIGHT, BASE - 1, SPILL, false), false);
            tower = region(DecorPatches.sideways(decor, LEFT, 0, RIGHT, BASE - 1, SPILL, true), false);
            decor.dispose();
        }

        /** @return le pied de la Tour, au milieu, à l'écran. */
        protected float footX() { return decorX(LEFT - SPILL + (RIGHT - LEFT + 1 + 2 * SPILL) / 2f); }
        protected float footY() { return decorY(BASE); }

        /** Le ciel où se dressait la Tour. */
        protected void drawSky(Batch batch) { DecorPatches.draw(this, batch, sky, LEFT - SPILL, 0, 1f); }

        /** La Tour, penchée de {@code degrees} autour de son pied, décalée de {@code dx}. */
        protected void drawTower(Batch batch, float degrees, float dx) {
            if (tower == null) return;
            float s = decorScale(), w = tower.getRegionWidth() * s, h = tower.getRegionHeight() * s;
            batch.setColor(litLikeDecor(WHITE, tmp));
            batch.draw(tower, footX() - w / 2f + dx, footY(), w / 2f, 0f, w, h, 1f, 1f, degrees);
            batch.setColor(Color.WHITE);
        }

        /** @return le haut de la Tour à l'écran quand elle penche de {@code degrees} (abscisse, puis ordonnée). */
        protected float topX(float degrees) { return footX() - MathUtils.sinDeg(degrees) * BASE * decorScale(); }
        protected float topY(float degrees) { return footY() + MathUtils.cosDeg(degrees) * BASE * decorScale(); }
    }

    // -------------------------------------------------------------------------
    // Phase 2 : « Les échafaudages montent » (il gagne l'Écho)
    // -------------------------------------------------------------------------

    /**
     * Des échafaudages de bois montent le long de sa Tour, étage après étage : poteaux, planchers, croix de
     * contreventement, une échelle. Arrivés en haut, ses yeux s'allument et sa voix revient en écho : deux échos de
     * lui, pâles, apparaissent sur les planchers à gauche et à droite, chacun avec son onde ; l'Écho se pose à côté.
     */
    static final class ArchitectScaffold extends ArchitectScene {
        /** Les poteaux (colonnes du décor) et les planchers (lignes du décor), du sol vers le haut. */
        private static final float[] POLES = {106f, 136f, 226f, 256f};
        private static final float[] FLOORS = {160f, 132f, 104f, 76f, 48f, 20f};
        private static final float BUILD = 0.25f, LEVEL = 0.27f;
        /** Les deux échos : où ils se tiennent (colonne, plancher) et quand ils apparaissent. */
        private static final float[][] ECHOES = {{121f, 48f, CLIMAX_PHASE}, {241f, 48f, CLIMAX_PHASE + 0.3f}};

        ArchitectScaffold(CutsceneKit kit) { super(kit, Moment.PHASE, 2); }

        /** @return de 0 à 1 : l'étage {@code level} (entre le plancher level et le suivant) qui monte et se pose. */
        private float built(int level) { return Interpolation.pow2Out.apply(progress(BUILD + level * LEVEL, BUILD + (level + 1) * LEVEL)); }

        @Override
        protected void step(float delta) {
            for (int level = 0; level < FLOORS.length - 1; level++) {
                if (at(BUILD + (level + 1) * LEVEL)) {
                    rumble(0.08f, 3f);
                    float y = decorY(FLOORS[level + 1]);
                    sparks(decorX(POLES[0]), y, 4, WOOD_LIGHT, 160f, 6f);
                    sparks(decorX(POLES[3]), y, 4, WOOD_LIGHT, 160f, 6f);
                }
            }
            if (at(CLIMAX_PHASE)) {
                rumble(0.5f, 10f);
                sparks(bossX(), bossY() + bossHeight() * 0.2f, 40, CHALK, 600f, 10f);
            }
            for (float[] echo : ECHOES) if (at(echo[2])) sparks(decorX(echo[0]), decorY(echo[1]) + bossHeight() * 0.3f, 20, CHALK, 300f, 8f);
        }

        @Override
        protected void back(Batch batch) {
            float s = decorScale(), beam = 2.2f * s, plank = 3f * s;
            for (int level = 0; level < FLOORS.length - 1; level++) {
                float k = built(level);
                if (k <= 0f) continue;
                float bottom = decorY(FLOORS[level]), top = decorY(FLOORS[level + 1]);
                float lift = (1f - k) * (top - bottom);                       // l'étage monte du dessous et se pose
                float b = bottom - lift, t = top - lift;
                for (int bay = 0; bay < POLES.length; bay += 2) {            // les croix, dans les deux travées
                    float l = decorX(POLES[bay]), r = decorX(POLES[bay + 1]);
                    line(batch, WOOD_DARK, k, l, b, r, t, beam * 0.8f);
                    line(batch, WOOD_DARK, k, r, b, l, t, beam * 0.8f);
                }
                if (level % 2 == 0) {                                         // l'échelle, à gauche
                    float l = decorX(POLES[0] - 7f), r = decorX(POLES[0] - 2f);
                    rect(batch, WOOD_DARK, k, l, b, s, t - b);
                    rect(batch, WOOD_DARK, k, r, b, s, t - b);
                    for (int rung = 1; rung < 6; rung++) rect(batch, WOOD, k, l, b + (t - b) * rung / 6f, r - l + s, s);
                }
                for (float pole : POLES) {                                    // les poteaux
                    rect(batch, WOOD_DARK, k, decorX(pole) - beam / 2f, b, beam, t - b);
                    rect(batch, WOOD_LIGHT, k, decorX(pole) - beam / 2f, b, beam * 0.35f, t - b);
                }
                float l = decorX(POLES[0] - 3f), r = decorX(POLES[3] + 3f);   // le plancher du haut de l'étage
                rect(batch, WOOD_DARK, k, l, t - plank * 0.5f, r - l, plank);
                rect(batch, WOOD, k, l, t - plank * 0.15f, r - l, plank * 0.55f);
            }
            for (float[] echo : ECHOES) {                                     // ses échos, debout sur les planchers
                float k = progress(echo[2], echo[2] + 0.35f);
                if (k <= 0f) continue;
                float scale = bossScale() * 0.55f, h = boss.getRegionHeight() * scale;
                float x = decorX(echo[0]), y = decorY(echo[1]) + h / 2f;
                float a = k * (0.5f + 0.15f * MathUtils.sin(time * 9f + echo[0]));
                glow(batch, x, y, h * 1.4f, CHALK, 0.25f * k);
                draw(batch, boss, x, y + 10f * gesture(echo[2], echo[2] + 0.8f), scale, 0f, CHALK, a);
            }
            poseY = 14f * gesture(1.6f, CLIMAX_PHASE + 0.6f);
        }

        @Override
        protected void front(Batch batch) {
            eyes(batch, progress(1.8f, CLIMAX_PHASE), theme.accent(), EYE, MONOCLE);
            for (int k = 0; k < 3; k++) wave(batch, CLIMAX_PHASE + k * 0.25f, 0.9f, bossX(), bossY() + bossHeight() * 0.2f, CHALK, bossHeight() * 2.2f);
            for (float[] echo : ECHOES) wave(batch, echo[2] + 0.1f, 0.8f, decorX(echo[0]), decorY(echo[1]) + bossHeight() * 0.3f, CHALK, bossHeight() * 1.2f);
            if (time >= CLIMAX_PHASE) {
                TextureRegion echo = symbol(EnemySymbol.ECHO);
                if (echo != null) {
                    float k = progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.4f);
                    glow(batch, bossX() + bossWidth() * 0.9f, bossY() + bossHeight() * 0.45f, iconScale(0.3f), CHALK, 0.4f * k);
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
     * Des pierres de sa Tour tombent et forment une arche au-dessus de lui ; la clé de voûte, en or, se pose en
     * dernier et la fenêtre de la Tour s'allume. Alors la Tour elle-même (la vraie, découpée dans le décor) prend son
     * élan en arrière et s'abat comme un marteau : le choc fait trembler l'écran. Elle se redresse en vacillant, une
     * spirale de vertige s'ouvre derrière lui et le Vertige se pose à côté de lui.
     */
    static final class ArchitectKeystone extends ArchitectScene {
        private static final int BLOCKS = 7;
        private static final float KEY = 1.15f, WINDUP = 1.25f, SWING = 1.75f, STRIKE = -40f;
        private final TextureRegion vertigo;
        private float hitX, hitY;

        ArchitectKeystone(CutsceneKit kit) {
            super(kit, Moment.PHASE, 3);
            vertigo = symbol(EnemySymbol.VERTIGO);
        }

        /** @return quand le bloc {@code k} se pose : les côtés d'abord, la clé de voûte (le milieu) en dernier. */
        private float landing(int k) {
            int fromSide = Math.min(k, BLOCKS - 1 - k);
            return k == BLOCKS / 2 ? KEY : 0.3f + fromSide * 0.22f + (k > BLOCKS / 2 ? 0.1f : 0f);
        }

        private float blockAngle(int k) { return 180f - k * 180f / (BLOCKS - 1); }
        private float archRadius()      { return bossWidth() * 0.95f; }
        private float archX(int k)      { return bossX() + MathUtils.cosDeg(blockAngle(k)) * archRadius(); }
        private float archY(int k)      { return bossBottom() + bossHeight() * 0.5f + MathUtils.sinDeg(blockAngle(k)) * archRadius(); }

        /** @return la bascule de la Tour : l'élan en arrière, le coup, puis elle se redresse en vacillant. */
        private float lean() {
            if (time < WINDUP) return 0f;
            if (time < SWING) return 9f * Interpolation.pow2Out.apply(progress(WINDUP, SWING));
            if (time < CLIMAX_PHASE) return MathUtils.lerp(9f, STRIKE, Interpolation.pow3In.apply(progress(SWING, CLIMAX_PHASE)));
            float u = time - CLIMAX_PHASE;
            return STRIKE * (float) Math.exp(-2.4f * u) * MathUtils.cos(u * 5.5f);
        }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < BLOCKS; k++) {
                if (at(landing(k))) {
                    boolean key = k == BLOCKS / 2;
                    rumble(key ? 0.3f : 0.1f, key ? 8f : 4f);
                    sparks(archX(k), archY(k), key ? 40 : 10, key ? GOLD : DUST, 400f, 9f);
                }
            }
            if (at(CLIMAX_PHASE)) {                                           // le coup de la Tour
                hitX = Math.min(worldWidth() * 0.97f, topX(STRIKE));
                hitY = Math.min(worldHeight() * 0.9f, topY(STRIKE));
                rumble(0.8f, 18f);
                sparks(hitX, hitY, 70, GOLD, 900f, 14f);
                sparks(hitX, hitY, 40, DUST, 700f, 16f);
                for (int i = 0; i < 10; i++) {                                // des pierres de la Tour sautent sous le choc
                    Particle stone = particle(hitX, hitY, 90f + 180f * random.nextFloat(), 500f + 500f * random.nextFloat(),
                        1.4f, decorScale() * (4f + 4f * random.nextFloat()));
                    stone.region = pixel;
                    stone.gravity = 1800f;
                    stone.spin = 400f * (random.nextFloat() - 0.5f);
                    stone.color.set(BRICK);
                    sprites.add(stone);
                }
            }
        }

        @Override
        protected void back(Batch batch) {
            float lean = lean();
            drawSky(batch);
            drawTower(batch, lean, 0f);
            float lit = progress(KEY, KEY + 0.3f);                           // sa fenêtre s'allume quand la clé se pose
            if (lit > 0f) {
                float wx = footX() + (WINDOW_X - (LEFT - SPILL + (RIGHT - LEFT + 1 + 2 * SPILL) / 2f)) * decorScale();
                float rx = footX() + (wx - footX()) * MathUtils.cosDeg(lean) - (decorY(WINDOW_Y) - footY()) * MathUtils.sinDeg(lean);
                float ry = footY() + (wx - footX()) * MathUtils.sinDeg(lean) + (decorY(WINDOW_Y) - footY()) * MathUtils.cosDeg(lean);
                glow(batch, rx, ry, decorScale() * 70f * pulse(7f, 0.7f), GOLD, 0.7f * lit);
            }
            float open = progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.8f);           // la spirale du vertige
            if (open > 0f) {
                for (int i = 0; i < 140; i++) {
                    float a = i * 9f + time * 140f, r = i * bossHeight() * 0.009f * open;
                    rect(batch, i % 20 < 10 ? SPIRAL : hex("6ad88a"), 0.5f * open, bossX() + MathUtils.cosDeg(a) * r - 3f,
                        bossY() + MathUtils.sinDeg(a) * r - 3f, 6f, 6f);
                }
            }
            poseTilt = time > CLIMAX_PHASE ? 4f * MathUtils.sin((time - CLIMAX_PHASE) * 9f) * (1f - progress(CLIMAX_PHASE, CLIMAX_PHASE + 1.2f)) : 0f;
            poseY = 10f * gesture(WINDUP, CLIMAX_PHASE + 0.3f);
        }

        @Override
        protected void front(Batch batch) {
            float size = bossWidth() * 0.4f;
            for (int k = 0; k < BLOCKS; k++) {                                // l'arche : pierres de la Tour, clé d'or
                float land = landing(k), fall = progress(land - 0.4f, land);
                if (fall <= 0f) continue;
                float y = MathUtils.lerp(worldHeight() + size, archY(k), Interpolation.pow2In.apply(fall));
                boolean key = k == BLOCKS / 2;
                float angle = blockAngle(k) - 90f, w = key ? size * 0.9f : size, h = size * 0.8f;
                litLikeDecor(key ? GOLD : BRICK, tmp);
                Color face = key ? GOLD : tmp;
                batch.setColor(key ? hex("8a5a10") : BRICK_DARK);
                batch.draw(pixel, archX(k) - w / 2f, y - h / 2f, w / 2f, h / 2f, w, h, 1f, 1f, angle);
                batch.setColor(face.r, face.g, face.b, 1f);
                batch.draw(pixel, archX(k) - w / 2f + 5f, y - h / 2f + 5f, w / 2f - 5f, h / 2f - 5f, w - 10f, h - 10f, 1f, 1f, angle);
                if (key && time >= KEY) glow(batch, archX(k), y, size * 2.4f, GOLD, 0.5f * pulse(6f, 0.6f));
            }
            eyes(batch, progress(KEY, KEY + 0.4f), GOLD, EYE, MONOCLE);
            if (time >= CLIMAX_PHASE) {
                wave(batch, CLIMAX_PHASE, 0.9f, hitX, hitY, GOLD, worldWidth() * 1.2f);
                wave(batch, CLIMAX_PHASE + 0.15f, 0.9f, hitX, hitY, DUST, worldWidth() * 0.8f);
            }
            if (vertigo != null && time >= CLIMAX_PHASE + 0.2f) {             // le Vertige qu'il gagne
                float k = Interpolation.pow2Out.apply(progress(CLIMAX_PHASE + 0.2f, CLIMAX_PHASE + 0.6f));
                float x = bossX() + bossWidth() * 0.95f, y = bossY() + bossHeight() * 0.1f;
                glow(batch, x, y, iconScale(0.3f), SPIRAL, 0.5f * k);
                draw(batch, vertigo, x, y, iconScale(0.12f) * k, (1f - k) * 360f + 6f * MathUtils.sin(time * 4f), WHITE, k);
            }
            flash(batch, GOLD, CLIMAX_PHASE, 0.3f, 0.45f);
        }
    }

    // -------------------------------------------------------------------------
    // Mort : sa Tour s'effondre
    // -------------------------------------------------------------------------

    /**
     * « La Tour de l'Architecte s'effondre » : sa vraie Tour, au fond, tremble et se fend ; son plan quitte son bras et
     * s'envole. La Tour s'écroule sur elle-même, pierre après pierre (ses vrais morceaux, découpés dans le décor) ;
     * le ciel apparaît là où elle se dressait. Le plan se déchire en deux, un nuage de poussière roule ; des pierres
     * tombent devant lui et il disparaît sous les gravats.
     */
    static final class ArchitectDeath extends ArchitectScene {
        private static final int COLS = 7, ROWS = 8;
        /** Quand la Tour se fend, quand elle s'écroule. */
        private static final float CRACK = 0.35f, FALL = 1.35f, GRAVITY = 3200f;
        /** Son plan roulé, sous son bras droit, en pixels du portrait : son centre et sa taille (contour compris). */
        private static final float PLAN_X = 9f, PLAN_Y = 36f, PLAN_W = 14f, PLAN_H = 6f;
        /** Le moment où son plan quitte son bras. */
        private static final float PLAN_OFF = 0.8f;
        private static final Color PAPER = hex("8ab8ff"), PAPER_DARK = hex("4a7ad0"), PAPER_END = hex("d8e8ff");
        private final Texture withPlan, withoutPlan;
        private final TextureRegion[] chunks = new TextureRegion[COLS * ROWS];
        private final float[] start = new float[COLS * ROWS], driftX = new float[COLS * ROWS], spin = new float[COLS * ROWS];
        private final float[] restY = new float[COLS * ROWS];
        private final boolean[] onHim = new boolean[COLS * ROWS];
        private int chunkW, chunkH;

        ArchitectDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, 0);
            withPlan = boss.getTexture();
            withoutPlan = kit.architectWithoutPlan().getTexture();
            if (tower != null) {
                chunkW = (int) Math.ceil(tower.getRegionWidth() / (float) COLS);
                chunkH = tower.getRegionHeight() / ROWS;
                for (int c = 0; c < COLS; c++) {
                    for (int r = 0; r < ROWS; r++) {
                        int x = c * chunkW, w = Math.min(chunkW, tower.getRegionWidth() - x);
                        chunks[c * ROWS + r] = new TextureRegion(tower, x, r * chunkH, w, chunkH);
                    }
                }
            }
        }

        @Override
        protected void restart() {
            for (int c = 0; c < COLS; c++) {
                for (int r = 0; r < ROWS; r++) {
                    int i = c * ROWS + r;
                    start[i] = FALL + (ROWS - 1 - r) * 0.05f + random.nextFloat() * 0.12f;   // le pied cède le premier
                    float side = (c - (COLS - 1) / 2f) / ((COLS - 1) / 2f);
                    driftX[i] = side * (40f + 160f * random.nextFloat()) + (random.nextFloat() - 0.5f) * 80f;
                    spin[i] = (random.nextFloat() - 0.5f) * 220f;
                    onHim[i] = Math.abs(c - (COLS - 1) / 2f) < 1.6f && r % 3 == 1;
                    restY[i] = random.nextFloat() * 26f * (1f - Math.abs(side) * 0.6f);  // le tas, plus haut au milieu
                }
            }
        }

        /** @return où est le morceau {@code i} : le décalage depuis sa place (abscisse, ordonnée), et sa rotation. */
        private float fallTime(int i) { return Math.max(0f, time - start[i]); }

        private float chunkHomeX(int i) { return decorX(LEFT - SPILL + (i / ROWS) * chunkW + chunkW / 2f); }
        private float chunkHomeY(int i) { return decorY((i % ROWS) * chunkH + chunkH / 2f); }

        /** @return la hauteur où le morceau {@code i} s'arrête : sur le tas au pied de la Tour, ou sur lui. */
        private float chunkRest(int i) {
            return onHim[i] ? bossBottom() + bossHeight() * 0.1f + restY[i] * decorScale() * 0.6f
                : footY() + (restY[i] + chunkH * 0.3f) * decorScale();
        }

        private float chunkY(int i) {
            float t = fallTime(i);
            return Math.max(chunkRest(i), chunkHomeY(i) - 0.5f * GRAVITY * t * t);
        }

        private boolean landed(int i) { return fallTime(i) > 0f && chunkY(i) <= chunkRest(i); }

        private float chunkX(int i) {
            float t = fallTime(i), fall = (float) Math.sqrt(Math.max(0f, 2f * (chunkHomeY(i) - chunkRest(i)) / GRAVITY));
            float x = chunkHomeX(i) + driftX[i] * Math.min(t, fall);
            return onHim[i] ? MathUtils.lerp(x, bossX() + (chunkHomeX(i) - footX()) * 0.5f, Math.min(1f, t / Math.max(0.01f, fall))) : x;
        }

        private float chunkSpin(int i) {
            float t = fallTime(i), fall = (float) Math.sqrt(Math.max(0f, 2f * (chunkHomeY(i) - chunkRest(i)) / GRAVITY));
            return spin[i] * Math.min(t, fall);
        }

        @Override
        protected void step(float delta) {
            if (at(CRACK)) rumble(0.3f, 4f);
            if (time > CRACK && time < FALL && random.nextFloat() < delta * 30f) {
                sparks(footX() + (random.nextFloat() - 0.5f) * (RIGHT - LEFT) * decorScale(), footY(), 2, DUST, 120f, 14f);
            }
            if (tower != null) {
                for (int i = 0; i < chunks.length; i++) {                    // chaque pierre qui touche le sol
                    float t = fallTime(i), fall = (float) Math.sqrt(Math.max(0f, 2f * (chunkHomeY(i) - chunkRest(i)) / GRAVITY));
                    if (t > 0f && t - delta <= fall && t > fall) {
                        sparks(chunkX(i), chunkRest(i), 3, DUST, 200f, 18f);
                        if (i % 6 == 0) rumble(0.1f, 4f);
                    }
                }
            }
            if (at(CLIMAX_DEATH)) {
                rumble(0.9f, 16f);
                for (int k = 0; k < 9; k++) sparks(footX() + (k - 4) * decorScale() * 18f, footY(), 10, DUST, 500f, 40f);
            }
        }

        @Override
        protected void back(Batch batch) {
            if (time >= PLAN_OFF && boss.getTexture() != withoutPlan) boss.setTexture(withoutPlan); // son plan part
            if (time < FALL) {                                                // elle tremble et se fend
                float shake = progress(CRACK, FALL) * 4f * MathUtils.sin(time * 70f);
                drawSky(batch);
                drawTower(batch, 0.3f * shake, shake * decorScale() * 0.4f);
                float cracks = progress(CRACK, FALL);
                for (int k = 0; k < 5; k++) {                                 // les fissures, du pied vers le haut et des flancs
                    float x = k < 3 ? footX() + (k - 1) * decorScale() * 40f : decorX(k == 3 ? LEFT + 4 : RIGHT - 4);
                    float y = k < 3 ? footY() : decorY(70f + (k - 3) * 30f);
                    crack(batch, x, y, k < 3 ? 80f + k * 10f : (k == 3 ? 20f : 160f), decorScale() * 90f * cracks, 31L + k,
                        hex("120a16"), cracks, decorScale() * 2f);
                }
                glow(batch, decorX(WINDOW_X), decorY(WINDOW_Y), decorScale() * 60f, GOLD, 0.35f * pulse(30f, 0.2f) * cracks);
            } else {
                drawSky(batch);
                for (int i = 0; i < chunks.length; i++) if (!onHim[i]) drawChunk(batch, i);
            }
            float dust = progress(FALL + 0.3f, CLIMAX_DEATH + 0.4f) * (1f - progress(4.2f, DEATH_COVER_FULL));
            for (int k = 0; k < 7; k++) {                                     // le nuage de poussière qui roule
                float x = footX() + (k - 3) * decorScale() * 26f * (1f + 0.4f * progress(CLIMAX_DEATH, 4.5f));
                float y = footY() + decorScale() * (12f + 10f * (k % 2)) + decorScale() * 14f * progress(CLIMAX_DEATH, 4.5f);
                glow(batch, x, y, decorScale() * 70f, DUST, 0.45f * dust);
            }
            poseSink = 0.75f * Interpolation.pow2In.apply(progress(CLIMAX_DEATH - 0.3f, CLIMAX_DEATH + 0.6f));
            poseShadow = 0.45f * progress(FALL, CLIMAX_DEATH);
            poseTilt = -6f * gesture(0.3f, 1.4f);
            showBoss = poseSink < 0.74f;
        }

        /** Le morceau {@code i} de la Tour, en chute ou sur le tas (plus sombre une fois tombé). */
        private void drawChunk(Batch batch, int i) {
            TextureRegion chunk = chunks[i];
            if (chunk == null) return;
            float s = decorScale(), w = chunk.getRegionWidth() * s, h = chunk.getRegionHeight() * s;
            litLikeDecor(WHITE, tmp);
            float dark = landed(i) ? 0.7f : 1f;
            batch.setColor(tmp.r * dark, tmp.g * dark, tmp.b * dark, 1f);
            batch.draw(chunk, chunkX(i) - w / 2f, chunkY(i) - h / 2f, w / 2f, h / 2f, w, h, 1f, 1f, chunkSpin(i));
            batch.setColor(Color.WHITE);
        }

        @Override
        protected void front(Batch batch) {
            if (time >= FALL) for (int i = 0; i < chunks.length; i++) if (onHim[i]) drawChunk(batch, i);
            // Son vrai plan quitte son bras (il disparaît du portrait au même instant), s'envole, se déroule, puis se déchire.
            float fly = progress(PLAN_OFF, FALL + 0.4f);
            if (time >= PLAN_OFF && time < 4.4f) {
                float px = bossScale();
                float x = MathUtils.lerp(portraitX(PLAN_X), bossX() - bossWidth() * 0.9f, fly);
                float y = MathUtils.lerp(portraitY(PLAN_Y), worldHeight() * 0.72f, Interpolation.pow2Out.apply(fly));
                float unroll = progress(1.3f, 2.1f);
                float w = MathUtils.lerp(PLAN_W * px, bossWidth() * 1.1f, unroll);
                float h = MathUtils.lerp(PLAN_H * px, w * 0.6f, unroll);
                if (unroll <= 0f) {                                           // encore roulé, comme sous son bras
                    rect(batch, PAPER, 1f, x - w / 2f, y - h / 2f, w, h);
                    rect(batch, PAPER_DARK, 1f, x - w / 2f, y - h / 2f, w, h / 3f);
                    rect(batch, PAPER_END, 1f, x - w / 2f, y - h / 2f, px * 2f, h);
                } else {                                                      // déroulé : le plan de sa Tour, qui se déchire
                    float tear = Interpolation.pow2In.apply(progress(CLIMAX_DEATH, 4.4f));
                    float a = 1f - progress(3.6f, 4.4f);
                    for (int half = -1; half <= 1; half += 2) {
                        float hx = x + half * (w / 4f + tear * worldWidth() * 0.12f), hy = y - tear * worldHeight() * 0.45f;
                        float rot = half * tear * 35f;
                        batch.setColor(PLAN.r, PLAN.g, PLAN.b, a);
                        batch.draw(pixel, hx - w / 4f, hy - h / 2f, w / 4f, h / 2f, w / 2f, h, 1f, 1f, rot);
                        batch.setColor(CHALK.r, CHALK.g, CHALK.b, 0.8f * a * unroll);   // la Tour tracée à la craie
                        batch.draw(pixel, hx - half * w / 4f + (half < 0 ? -w * 0.12f : w * 0.02f), hy - h * 0.38f,
                            w / 4f, h / 2f, w * 0.1f, h * 0.7f, 1f, 1f, rot);
                    }
                    batch.setColor(Color.WHITE);
                }
            }
            flash(batch, DUST, CLIMAX_DEATH, 0.35f, 0.4f);
        }
    }
}
