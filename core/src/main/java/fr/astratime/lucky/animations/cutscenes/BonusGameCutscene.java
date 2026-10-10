package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.assets.GameSounds;
import fr.astratime.lucky.entities.BonusGame;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.List;

/**
 * Le Jeu bonus, par-dessus le combat : « JEU BONUS ! » éclate, les lumières du
 * casino s'éteignent rangée par rangée, puis la machine ouvre ses portes sur une
 * grande grille de 6 x 6, avec sa propre musique. La grille tourne trois fois :
 * ses colonnes s'arrêtent l'une après l'autre, les symboles alignés se figent en
 * doré et ne relancent plus. À la fin, chaque alignement s'allume à son tour avec
 * ce qu'il rapporte, puis le total. Fondu noir, retour au combat.
 *
 * Tout est déjà tiré ({@link BonusGame}) : la scène ne fait que le montrer. Un
 * clic (ou une touche) passe l'ouverture, puis les tirages (le total s'affiche),
 * puis referme la scène. La première fois, la scène s'arrête sur la grille ouverte
 * le temps que le Croupier l'explique (voir {@link #pauseOnGrid}).
 */
public class BonusGameCutscene extends Cutscene {

    /** « JEU BONUS ! » éclate, puis les lumières s'éteignent, puis les portes s'ouvrent sur la grille. */
    public static final float LIGHTS_OUT = 1.0f;
    public static final float DOORS      = 2.0f;
    public static final float GRID       = 3.0f;
    /** Premier tirage, puis un tous les {@link #SPIN_LENGTH}. */
    public static final float FIRST_SPIN  = GRID + 0.4f;
    public static final float SPIN_LENGTH = 2.0f;
    /** Après le départ d'un tirage, la première colonne s'arrête, puis une toutes les {@link #COLUMN_STEP}. */
    private static final float FIRST_STOP  = 0.9f;
    private static final float COLUMN_STEP = 0.12f;
    /** Après le départ d'un tirage, ses symboles alignés se figent en doré. */
    private static final float FREEZE      = FIRST_STOP + COLUMN_STEP * (BonusGame.SIZE - 1) + 0.05f;
    /** Après le dernier gel, les alignements s'allument un par un (au plus {@link #LINES_TIME} secondes en tout). */
    private static final float PAYOUT_PAUSE = 0.45f;
    private static final float LINE_STEP    = 0.4f;
    private static final float LINES_TIME   = 3.2f;
    /** Le total reste à l'écran, puis le fondu noir. */
    private static final float TOTAL_HOLD   = 3.0f;
    private static final float COVER        = 0.6f;
    /** Symboles qui défilent par seconde dans une case qui tourne. */
    private static final float REEL_SPEED   = 14f;

    private static final Color GOLD = c("ffc93a"), GOLD_LIGHT = c("ffe58a"), GOLD_DARK = c("b8801f");
    private static final Color RED = c("c8203a"), RED_DARK = c("6e0f1e"), INK = c("1c0f24"), NIGHT = c("07040c");
    private static final Color BULB_OFF = c("3a2a20"), CREAM = c("fff6dc");

    private final BonusGame game;
    private final BitmapFont font;
    private final GameSounds sounds;
    private final Sound intro, music, freeze, total;
    /** Les symboles de la machine du joueur, puis le Joker : la bande des cases qui tournent. */
    private final List<Symbol> strip = new ArrayList<>();
    private final TextureRegion[] stripArt;
    /** Symbole de départ de chaque case, avant le premier tirage (pour le décor). */
    private final Symbol[][] start = new Symbol[BonusGame.SIZE][BonusGame.SIZE];
    private final float[][] speed = new float[BonusGame.SIZE][BonusGame.SIZE];
    private final Array<Particle> sparks = new Array<>(false, 128);
    private final List<long[]> playing = new ArrayList<>(); // {indice du son, id} des sons lancés par la scène
    private final Sound[] owned;
    /** Les alignements s'allument à partir de là, un tous les {@code lineStep} ; puis le total. */
    private final float payout, lineStep, totalAt;
    private float closeAt;
    private float lastTime;
    private Runnable onGrid;
    private boolean holding, gridShown;
    private long spinLoop = -1;

    /**
     * @param game     le Jeu bonus, déjà joué
     * @param reels    les symboles de la machine du joueur (la bande des cases qui tournent)
     */
    public BonusGameCutscene(CutsceneKit kit, BonusGame game, List<Symbol> reels) {
        super(kit.settings(), kit.shake(), null);
        this.game = game;
        font   = kit.font();
        sounds = kit.gameSounds();
        intro  = kit.sound("bonus_game");
        music  = kit.loop("bonus_game_music");
        freeze = kit.sound("bonus_game_freeze");
        total  = kit.sound("bonus_game_total");
        owned  = new Sound[] {intro, music, freeze, total};
        strip.addAll(reels);
        strip.add(Symbol.JOKER);
        stripArt = new TextureRegion[strip.size()];
        for (int i = 0; i < stripArt.length; i++) stripArt[i] = load(strip.get(i).getAssetPath());
        for (int row = 0; row < BonusGame.SIZE; row++) {
            for (int col = 0; col < BonusGame.SIZE; col++) {
                start[row][col] = reels.get(random.nextInt(reels.size()));
                speed[row][col] = REEL_SPEED * (0.85f + 0.3f * random.nextFloat());
            }
        }
        int lines = game.getLines().size();
        float step = lines == 0 ? 0f : Math.min(LINE_STEP, LINES_TIME / lines);
        payout  = FIRST_SPIN + (BonusGame.SPINS - 1) * SPIN_LENGTH + FREEZE + PAYOUT_PAUSE;
        totalAt = payout + step * lines + (lines == 0 ? 0f : 0.3f);
        lineStep = step;
    }

    /**
     * La première fois : la scène s'arrête sur la grille ouverte, avant le premier
     * tirage, et appelle {@code onGrid} (le Croupier explique) ; {@link #resumeGrid}
     * la relance.
     */
    public void pauseOnGrid(Runnable onGrid) { this.onGrid = onGrid; }

    /** Le Croupier a fini : les tirages commencent. */
    public void resumeGrid() { holding = false; }

    @Override protected float coverStart() { return closeAt; }
    @Override protected float coverFull()  { return closeAt + COVER; }
    @Override protected Color coverColor() { return Color.BLACK; }

    @Override
    protected void reset() {
        stopSounds();
        sparks.clear();
        closeAt = totalAt + TOTAL_HOLD;
        lastTime = 0f;
        holding = false;
        gridShown = false;
        sound(intro, false);
    }

    // -------------------------------------------------------------------------
    // Clics et touches : passer l'ouverture, puis les tirages, puis refermer
    // -------------------------------------------------------------------------

    @Override
    protected boolean pointerDown(float x, float y) {
        skip();
        return true;
    }

    @Override
    public void skip() {
        if (!isPlaying() || holding || time >= closeAt) return;
        if (time < GRID) {
            stop(intro);
            jump(GRID);
        } else if (time < totalAt) {
            stopSpinLoop();
            jump(totalAt);
        } else {
            closeAt = time;
        }
    }

    /** La scène saute à {@code at} : les sons et les gerbes d'entre-deux ne se jouent pas. */
    private void jump(float at) {
        time = at;
        lastTime = at;
        sparks.clear();
        if (at >= GRID) showGrid();
    }

    @Override
    public void act(float delta) {
        super.act(holding ? 0f : delta);
        if (!isPlaying() && !playing.isEmpty()) stopSounds(); // l'écran est couvert : la musique s'arrête avec la scène
    }

    // -------------------------------------------------------------------------
    // Ce qui arrive au fil du temps
    // -------------------------------------------------------------------------

    @Override
    protected void simulate(float delta) {
        float from = lastTime;
        lastTime = time;
        if (crossed(from, LIGHTS_OUT) || crossed(from, LIGHTS_OUT + 0.33f) || crossed(from, LIGHTS_OUT + 0.66f)) {
            rumble(0.12f, 3f);
        }
        if (crossed(from, GRID)) showGrid();
        for (int spin = 0; spin < BonusGame.SPINS; spin++) {
            float at = spinStart(spin);
            if (crossed(from, at)) {
                stopSpinLoop();
                if (sounds != null) spinLoop = sounds.reelSpin.loop(0.6f);
            }
            for (int col = 0; col < BonusGame.SIZE; col++) {
                if (crossed(from, at + stopTime(col)) && sounds != null) sounds.reelStop.play(0.5f);
            }
            if (crossed(from, at + stopTime(BonusGame.SIZE - 1))) stopSpinLoop();
            if (crossed(from, at + FREEZE) && burstFrozen(spin)) sound(freeze, false);
        }
        List<BonusGame.Line> lines = game.getLines();
        for (int i = 0; i < lines.size(); i++) {
            if (crossed(from, payout + i * lineStep)) {
                if (sounds != null) sounds.coinsGain.play(0.7f, Math.min(2f, 1f + i * 0.06f), 0f);
                float[] end = cellCenter(lines.get(i).cells().get(lines.get(i).length() - 1));
                burst(end[0], end[1], 10, GOLD_LIGHT);
            }
        }
        if (crossed(from, totalAt)) {
            sound(total, false);
            burst(worldWidth() / 2f, totalY(), 40, GOLD);
        }
        update(sparks, delta);
    }

    /** @return {@code true} si l'instant {@code at} vient de passer (depuis {@code from}). */
    private boolean crossed(float from, float at) { return from < at && time >= at; }

    /** La grille est ouverte : sa musique part ; la première fois, la scène s'arrête pour le Croupier. */
    private void showGrid() {
        if (gridShown) return;
        gridShown = true;
        sound(music, true);
        if (onGrid != null) {
            holding = true;
            Runnable callback = onGrid;
            onGrid = null;
            callback.run();
        }
    }

    /** Gerbe dorée sur les cases figées par le tirage {@code spin} ; @return {@code true} s'il y en a de nouvelles. */
    private boolean burstFrozen(int spin) {
        boolean any = false;
        for (int row = 0; row < BonusGame.SIZE; row++) {
            for (int col = 0; col < BonusGame.SIZE; col++) {
                if (!game.isFrozen(spin, row, col) || spin > 0 && game.isFrozen(spin - 1, row, col)) continue;
                any = true;
                float[] center = cellCenter(new int[] {row, col});
                burst(center[0], center[1], 4, GOLD_LIGHT);
            }
        }
        return any;
    }

    private void burst(float x, float y, int count, Color color) {
        if (settings.isReducedEffects()) count = Math.max(1, count / 3);
        for (int i = 0; i < count; i++) {
            Particle spark = particle(x, y, random.nextFloat() * 360f, 60f + random.nextFloat() * 160f,
                0.5f + random.nextFloat() * 0.5f, 10f + random.nextFloat() * 14f);
            spark.drag = 0.1f;
            spark.color.set(color);
            sparks.add(spark);
        }
    }

    private static float spinStart(int spin) { return FIRST_SPIN + spin * SPIN_LENGTH; }

    private static float stopTime(int col) { return FIRST_STOP + col * COLUMN_STEP; }

    // -------------------------------------------------------------------------
    // Dessin
    // -------------------------------------------------------------------------

    private float cellHeight() {
        float byHeight = worldHeight() * 0.092f;
        float byWidth  = worldWidth() * 0.62f / (BonusGame.SIZE * 1.2f + (BonusGame.SIZE + 1) * 0.1f);
        return Math.min(byHeight, byWidth);
    }

    private float cellWidth() { return cellHeight() * 1.2f; }
    private float gap()       { return cellHeight() * 0.1f; }
    private float gridWidth() { return BonusGame.SIZE * cellWidth() + (BonusGame.SIZE + 1) * gap(); }
    private float gridHeight() { return BonusGame.SIZE * cellHeight() + (BonusGame.SIZE + 1) * gap(); }
    private float gridX()     { return (worldWidth() - gridWidth()) / 2f; }
    private float gridY()     { return worldHeight() * 0.5f - gridHeight() / 2f; }
    private float totalY()    { return gridY() - cellHeight() * 1.1f; }

    /** @return le coin bas gauche de la case {@code {ligne, colonne}} (la ligne 0 en haut). */
    private float[] cellCorner(int row, int col) {
        return new float[] {gridX() + gap() + col * (cellWidth() + gap()),
            gridY() + gap() + (BonusGame.SIZE - 1 - row) * (cellHeight() + gap())};
    }

    private float[] cellCenter(int[] cell) {
        float[] corner = cellCorner(cell[0], cell[1]);
        return new float[] {corner[0] + cellWidth() / 2f, corner[1] + cellHeight() / 2f};
    }

    @Override
    protected void drawScene(Batch batch) {
        // Le casino s'assombrit, puis s'éteint.
        float dark = 0.35f * Interpolation.pow2Out.apply(progress(0f, 0.4f)) + 0.6f * progress(LIGHTS_OUT, DOORS);
        fill(batch, NIGHT, Math.min(0.95f, dark));
        drawBulbs(batch);
        if (time >= DOORS - 0.3f) drawMachine(batch);
        drawBanner(batch);
        drawParticles(batch, sparks);
    }

    /** « JEU BONUS ! » : il éclate au centre, puis monte au-dessus de la grille. */
    private void drawBanner(Batch batch) {
        float pop = Interpolation.swingOut.apply(progress(0f, 0.45f));
        float rise = Interpolation.pow2.apply(progress(LIGHTS_OUT + 0.6f, DOORS + 0.4f));
        float y = MathUtils.lerp(worldHeight() * 0.55f, gridY() + gridHeight() + cellHeight() * 1.15f, rise);
        float scale = MathUtils.lerp(4.2f, 2.4f, rise) * pop;
        if (scale <= 0.01f) return;
        glow(batch, worldWidth() / 2f, y, 420f * scale / 3f, GOLD, 0.35f * (1f - 0.5f * rise));
        font.getData().setScale(scale);
        caption(batch, font, Lang.t("JEU BONUS !"), worldWidth() / 2f, y, 1f);
        font.getData().setScale(1f);
    }

    /** Les lumières du casino, tout autour de l'écran : elles clignotent, puis s'éteignent rangée par rangée. */
    private void drawBulbs(Batch batch) {
        float w = worldWidth(), h = worldHeight(), step = 34f, size = 10f;
        int rows = 3;
        for (int ring = 0; ring < rows; ring++) {
            float off = LIGHTS_OUT + ring * 0.33f;               // la rangée extérieure s'éteint la première
            if (time >= off + 0.15f) continue;
            float inset = 14f + ring * 26f;
            boolean on = time < off;
            float flicker = on ? 1f : (MathUtils.sin(time * 90f) > 0f ? 1f : 0.2f);
            for (float x = inset; x <= w - inset; x += step) {
                bulb(batch, x, inset, size, on, flicker, (int) (x / step) + ring);
                bulb(batch, x, h - inset, size, on, flicker, (int) (x / step) + ring);
            }
            for (float y = inset + step; y <= h - inset - step; y += step) {
                bulb(batch, inset, y, size, on, flicker, (int) (y / step) + ring);
                bulb(batch, w - inset, y, size, on, flicker, (int) (y / step) + ring);
            }
        }
    }

    private void bulb(Batch batch, float x, float y, float size, boolean on, float flicker, int index) {
        boolean chase = (index + (int) (time * 8f)) % 3 != 0;
        if (on && chase) glow(batch, x, y, size * 4f, GOLD, 0.35f * flicker);
        batch.setColor(on && chase ? GOLD_LIGHT : BULB_OFF);
        if (on && chase) batch.setColor(GOLD_LIGHT.r, GOLD_LIGHT.g, GOLD_LIGHT.b, flicker);
        batch.draw(pixel, x - size / 2f, y - size / 2f, size, size);
    }

    /** La machine : son cadre doré et ses ampoules, la grille, et ses portes qui s'ouvrent. */
    private void drawMachine(Batch batch) {
        float appear = Interpolation.pow2Out.apply(progress(DOORS - 0.3f, DOORS));
        float gx = gridX(), gy = gridY(), gw = gridWidth(), gh = gridHeight();
        float pad = cellHeight() * 0.35f;
        float fx = gx - pad, fy = gy - pad, fw = gw + 2f * pad, fh = gh + 2f * pad;
        // Cadre : or sombre, or, intérieur rouge profond.
        batch.setColor(GOLD_DARK.r, GOLD_DARK.g, GOLD_DARK.b, appear);
        batch.draw(pixel, fx - 6f, fy - 6f, fw + 12f, fh + 12f);
        batch.setColor(GOLD.r, GOLD.g, GOLD.b, appear);
        batch.draw(pixel, fx, fy, fw, fh);
        batch.setColor(RED_DARK.r, RED_DARK.g, RED_DARK.b, appear);
        batch.draw(pixel, gx, gy, gw, gh);
        // Ampoules du cadre, en chenillard.
        int perSide = 14;
        for (int i = 0; i < perSide; i++) {
            float k = (i + 0.5f) / perSide;
            frameBulb(batch, fx + k * fw, fy + pad / 2f, i, appear);
            frameBulb(batch, fx + k * fw, fy + fh - pad / 2f, i + 7, appear);
            frameBulb(batch, fx + pad / 2f, fy + k * fh, i + 3, appear);
            frameBulb(batch, fx + fw - pad / 2f, fy + k * fh, i + 11, appear);
        }
        drawCells(batch, appear);
        if (time >= payout) drawLines(batch);
        drawDoors(batch, gx, gy, gw, gh);
        drawCaptions(batch);
    }

    private void frameBulb(Batch batch, float x, float y, int index, float alpha) {
        boolean lit = time >= GRID && (index + (int) (time * 10f)) % 4 != 0;
        float size = cellHeight() * 0.13f;
        if (lit) glow(batch, x, y, size * 4f, GOLD_LIGHT, 0.4f * alpha);
        Color color = lit ? CREAM : GOLD_DARK;
        batch.setColor(color.r, color.g, color.b, alpha);
        batch.draw(pixel, x - size / 2f, y - size / 2f, size, size);
    }

    /** Les portes de la machine : deux battants rouge et or qui glissent sur les côtés. */
    private void drawDoors(Batch batch, float gx, float gy, float gw, float gh) {
        float open = Interpolation.pow2InInverse.apply(progress(DOORS + 0.2f, GRID));
        if (open >= 1f) return;
        float half = gw / 2f, slide = half * open;
        for (int side = 0; side < 2; side++) {
            float x = side == 0 ? gx - slide : gx + half + slide;
            float clipLeft = Math.max(x, gx), clipRight = Math.min(x + half, gx + gw);
            if (clipRight <= clipLeft) continue;
            batch.setColor(RED);
            batch.draw(pixel, clipLeft, gy, clipRight - clipLeft, gh);
            batch.setColor(GOLD);
            float edge = side == 0 ? x + half - 6f : x;  // le bord qui s'écarte
            if (edge >= gx && edge + 6f <= gx + gw) batch.draw(pixel, edge, gy, 6f, gh);
            float diamond = cellHeight() * 0.5f, cx = x + half / 2f;
            if (cx > gx && cx < gx + gw) {
                batch.setColor(GOLD_LIGHT);
                batch.draw(pixel, cx - diamond / 2f, gy + gh / 2f - diamond / 2f, diamond / 2f, diamond / 2f, diamond,
                    diamond, 1f, 1f, 45f);
            }
        }
    }

    /** Les 36 cases : figées en doré, arrêtées, ou qui tournent. */
    private void drawCells(Batch batch, float alpha) {
        int spin = currentSpin();
        for (int row = 0; row < BonusGame.SIZE; row++) {
            for (int col = 0; col < BonusGame.SIZE; col++) {
                float[] corner = cellCorner(row, col);
                float x = corner[0], y = corner[1], w = cellWidth(), h = cellHeight();
                boolean gold = goldAt(row, col);
                Color back = gold ? GOLD : INK;
                batch.setColor(back.r, back.g, back.b, alpha);
                batch.draw(pixel, x, y, w, h);
                batch.setColor(1f, 1f, 1f, alpha);
                float inset = h * (gold ? 0.12f : 0.06f);
                if (spin < 0 || isStill(spin, row, col)) {
                    Symbol shown = spin < 0 ? start[row][col] : game.getGrid(spin)[row][col];
                    batch.draw(stripArt[strip.indexOf(shown)], x + inset, y + inset, w - 2f * inset, h - 2f * inset);
                } else {
                    reel(batch, stripArt, x + inset, y + inset, w - 2f * inset, h - 2f * inset, offset(spin, row, col));
                }
                if (gold) { // figée : cadre doré qui brille, voile d'or sur le symbole
                    float shine = 0.5f + 0.5f * MathUtils.sin(time * 4f + row + col);
                    additive(batch);
                    batch.setColor(GOLD.r, GOLD.g, GOLD.b, (0.1f + 0.08f * shine) * alpha);
                    batch.draw(pixel, x + inset, y + inset, w - 2f * inset, h - 2f * inset);
                    normal(batch);
                    glow(batch, x + w / 2f, y + h / 2f, w * 1.8f, GOLD_LIGHT, (0.15f + 0.15f * shine) * alpha);
                }
            }
        }
    }

    /** @return le dernier tirage parti (-1 avant le premier). */
    private int currentSpin() {
        for (int spin = BonusGame.SPINS - 1; spin >= 0; spin--) {
            if (time >= spinStart(spin)) return spin;
        }
        return -1;
    }

    /** @return {@code true} si la case ne tourne pas au tirage {@code spin} (figée avant, ou déjà arrêtée). */
    private boolean isStill(int spin, int row, int col) {
        boolean kept = spin > 0 && game.isFrozen(spin - 1, row, col);
        return kept || time >= spinStart(spin) + stopTime(col);
    }

    /** @return {@code true} si la case est dorée : figée par un tirage dont le gel est passé. */
    private boolean goldAt(int row, int col) {
        for (int spin = BonusGame.SPINS - 1; spin >= 0; spin--) {
            if (time >= spinStart(spin) + FREEZE) return game.isFrozen(spin, row, col);
        }
        return false;
    }

    /** @return la position de la bande d'une case qui tourne : elle s'arrête pile sur son symbole. */
    private float offset(int spin, int row, int col) {
        int target = strip.indexOf(game.getGrid(spin)[row][col]);
        float left = spinStart(spin) + stopTime(col) - time;    // secondes avant l'arrêt
        float run = left > 0.3f ? left : left * left / 0.3f;    // elle ralentit sur la fin
        return target - run * speed[row][col];
    }

    /** Les alignements s'allument un par un, avec ce qu'ils rapportent. */
    private void drawLines(Batch batch) {
        List<BonusGame.Line> lines = game.getLines();
        float thick = cellHeight() * 0.12f;
        for (int i = 0; i < lines.size(); i++) {
            float at = payout + i * lineStep;
            if (time < at) break;
            BonusGame.Line line = lines.get(i);
            float[] a = cellCenter(line.cells().get(0)), b = cellCenter(line.cells().get(line.length() - 1));
            boolean current = time < at + Math.max(lineStep, 0.4f) || time >= totalAt;
            float alpha = current ? 1f : 0.45f;
            float dx = b[0] - a[0], dy = b[1] - a[1];
            float length = (float) Math.hypot(dx, dy) + cellHeight() * 0.6f;
            float angle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
            float mx = (a[0] + b[0]) / 2f, my = (a[1] + b[1]) / 2f;
            additive(batch);
            batch.setColor(GOLD_LIGHT.r, GOLD_LIGHT.g, GOLD_LIGHT.b, 0.5f * alpha);
            batch.draw(pixel, mx - length / 2f, my - thick, length / 2f, thick, length, thick * 2f, 1f, 1f, angle);
            normal(batch);
            batch.setColor(CREAM.r, CREAM.g, CREAM.b, 0.9f * alpha);
            batch.draw(pixel, mx - length / 2f, my - thick / 4f, length / 2f, thick / 4f, length, thick / 2f, 1f, 1f,
                angle);
            if (time < at + 0.9f && time < totalAt) {
                float k = progress(at, at + 0.9f);
                font.getData().setScale(1.3f);
                caption(batch, font, "+" + Lang.grouped(line.gains()), b[0], b[1] + cellHeight() * (0.4f + 0.5f * k),
                    1f - k * k);
                font.getData().setScale(1f);
            }
        }
    }

    /** « TIRAGE 2/3 » sous la grille, puis le total qui monte, puis le total final. */
    private void drawCaptions(Batch batch) {
        if (time < GRID) return;
        float y = totalY();
        if (time < payout) {
            int spin = Math.max(0, currentSpin());
            font.getData().setScale(1.4f);
            caption(batch, font, Lang.f("TIRAGE {0}/{1}", spin + 1, BonusGame.SPINS), worldWidth() / 2f, y, 1f);
        } else if (time < totalAt) {
            long sum = 0;
            for (int i = 0; i < game.getLines().size() && time >= payout + i * lineStep; i++) {
                sum += game.getLines().get(i).gains();
            }
            font.getData().setScale(1.6f);
            caption(batch, font, Lang.f("GAINS +{0}", Lang.grouped(sum)), worldWidth() / 2f, y, 1f);
        } else {
            float pop = Interpolation.swingOut.apply(progress(totalAt, totalAt + 0.4f));
            String text = game.getLines().isEmpty() ? Lang.t("AUCUN ALIGNEMENT")
                : Lang.f("GAINS +{0}", Lang.grouped(game.getTotal()));
            glow(batch, worldWidth() / 2f, y, 360f, GOLD, 0.3f * pop);
            font.getData().setScale(Math.max(0.01f, 2.4f * pop));
            caption(batch, font, text, worldWidth() / 2f, y, 1f);
        }
        font.getData().setScale(1f);
    }

    // -------------------------------------------------------------------------
    // Sons de la scène
    // -------------------------------------------------------------------------

    private void sound(Sound sound, boolean loop) {
        if (sound == null) return;
        for (int i = 0; i < owned.length; i++) {
            if (owned[i] == sound) playing.add(new long[] {i, loop ? sound.loop() : sound.play()});
        }
    }

    private void stop(Sound sound) {
        for (int i = playing.size() - 1; i >= 0; i--) {
            long[] entry = playing.get(i);
            if (owned[(int) entry[0]] == sound) {
                sound.stop(entry[1]);
                playing.remove(i);
            }
        }
    }

    private void stopSpinLoop() {
        if (spinLoop != -1 && sounds != null) sounds.reelSpin.stop(spinLoop);
        spinLoop = -1;
    }

    private void stopSounds() {
        for (long[] entry : playing) {
            Sound sound = owned[(int) entry[0]];
            if (sound != null) sound.stop(entry[1]);
        }
        playing.clear();
        stopSpinLoop();
    }

    @Override
    public void pauseSound() {
        for (long[] entry : playing) owned[(int) entry[0]].pause(entry[1]);
        if (spinLoop != -1 && sounds != null) sounds.reelSpin.pause(spinLoop);
    }

    @Override
    public void resumeSound() {
        for (long[] entry : playing) owned[(int) entry[0]].resume(entry[1]);
        if (spinLoop != -1 && sounds != null) sounds.reelSpin.resume(spinLoop);
    }

    @Override
    public void cancel() {
        stopSounds();
        super.cancel();
    }

}
