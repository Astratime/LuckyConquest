package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.assets.GameSounds;
import fr.astratime.lucky.entities.BonusGame;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.List;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les petits Jeux bonus (Roue, Coffres, Ticket à gratter), par-dessus le combat,
 * dans le décor du Jeu bonus : le casino s'assombrit, le titre éclate puis monte,
 * le jeu se joue au centre, puis le total. Fondu noir, retour au combat.
 *
 * Tout est déjà tiré ({@link BonusGame#playSmall}) : la scène ne fait que le montrer.
 * Un clic (ou une touche) passe au total, puis referme la scène.
 */
public class SmallBonusGameCutscene extends Cutscene {

    /** Le titre éclate, puis le jeu apparaît. */
    private static final float SHOW = 1.2f;
    /** Le jeu commence. */
    private static final float START = 1.8f;
    /** Roue : elle tourne, puis s'arrête. */
    private static final float WHEEL_SPIN = 3.6f;
    /** Coffres : un coffre s'ouvre tous les {@link #CHEST_STEP}, puis les autres se dévoilent. */
    private static final float CHEST_STEP = 1.0f;
    /** Ticket : une case se gratte tous les {@link #SCRATCH_STEP}. */
    private static final float SCRATCH_STEP = 0.32f;
    private static final float TOTAL_HOLD = 3.0f;
    private static final float COVER = 0.6f;

    private static final Color GOLD = hex("ffc93a"), GOLD_LIGHT = hex("ffe58a"), GOLD_DARK = hex("b8801f");
    private static final Color RED = hex("c8203a"), RED_DARK = hex("6e0f1e"), INK = hex("1c0f24"), NIGHT = hex("07040c");
    private static final Color CREAM = hex("fff6dc"), WOOD = hex("7a4a24"), WOOD_DARK = hex("4a2a12");
    private static final Color SILVER = hex("b9c0c8"), SILVER_DARK = hex("7d858e"), GREEN = hex("2f8f4e");

    private final BonusGame game;
    private final BitmapFont font;
    private final GameSounds sounds;
    private final Sound intro, music, total;
    private final Sound[] owned;
    private final List<long[]> playing = new ArrayList<>();
    private final Array<Particle> sparks = new Array<>(false, 128);
    /** Le résultat se montre ; puis le total. */
    private final float reveal, totalAt;
    private float closeAt, lastTime;
    private long spinLoop = -1;

    public SmallBonusGameCutscene(CutsceneKit kit, BonusGame game) {
        super(kit.settings(), kit.shake(), null);
        this.game = game;
        font = kit.font();
        sounds = kit.gameSounds();
        intro = kit.sound("bonus_game");
        music = kit.loop("bonus_game_music");
        total = kit.sound("bonus_game_total");
        owned = new Sound[] {intro, music, total};
        reveal = switch (game.getKind()) {
            case WHEEL -> START + WHEEL_SPIN;
            case CHESTS -> START + 0.4f + CHEST_STEP * game.getPicked().size();
            default -> START + 0.4f + SCRATCH_STEP * BonusGame.SCRATCH_CELLS;
        };
        totalAt = reveal + 1.0f;
    }

    /** @return le titre du jeu. */
    public static String title(BonusGame.Kind kind) {
        return switch (kind) {
            case WHEEL -> Lang.t("ROUE DE LA CAGNOTTE !");
            case CHESTS -> Lang.t("COFFRES DE LA CAGNOTTE !");
            case SCRATCH -> Lang.t("TICKET À GRATTER !");
            default -> Lang.t("JEU BONUS !");
        };
    }

    /** @return {@code value} écrit « x1,5 » (« x1.5 » en anglais), « x2 ». */
    static String times(double value) {
        String number = value == Math.rint(value) ? Long.toString(Math.round(value))
            : Lang.decimal(String.valueOf(Math.round(value * 100) / 100.0));
        return "x" + number;
    }

    @Override protected float coverStart() { return closeAt; }
    @Override protected float coverFull()  { return closeAt + COVER; }
    @Override protected Color coverColor() { return Color.BLACK; }

    @Override
    protected void reset() {
        stopSounds();
        sparks.clear();
        closeAt = totalAt + TOTAL_HOLD;
        lastTime = 0f;
        sound(intro, false);
    }

    @Override
    protected boolean pointerDown(float x, float y) {
        skip();
        return true;
    }

    @Override
    public void skip() {
        if (!isPlaying() || time >= closeAt) return;
        if (time < totalAt) {
            stop(intro);
            stopSpinLoop();
            if (time < START) sound(music, true);
            time = totalAt;
            lastTime = totalAt;
            sparks.clear();
        } else {
            closeAt = time;
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isPlaying() && !playing.isEmpty()) stopSounds();
    }

    // -------------------------------------------------------------------------
    // Ce qui arrive au fil du temps
    // -------------------------------------------------------------------------

    @Override
    protected void simulate(float delta) {
        float from = lastTime;
        lastTime = time;
        if (crossed(from, START)) {
            sound(music, true);
            if (game.getKind() == BonusGame.Kind.WHEEL && sounds != null) spinLoop = sounds.reelSpin.loop(0.5f);
        }
        switch (game.getKind()) {
            case WHEEL -> {
                if (crossed(from, reveal)) {
                    stopSpinLoop();
                    if (sounds != null) sounds.reelStop.play(0.8f);
                    burst(wheelX(), wheelY() + wheelRadius(), 24, GOLD_LIGHT);
                }
            }
            case CHESTS -> {
                for (int i = 0; i < game.getPicked().size(); i++) {
                    if (crossed(from, chestOpen(i))) {
                        if (sounds != null) sounds.coinsGain.play(0.7f, 1f + i * 0.1f, 0f);
                        float[] c = chestCenter(game.getPicked().get(i));
                        burst(c[0], c[1], 16, GOLD_LIGHT);
                    }
                }
            }
            default -> {
                for (int i = 0; i < BonusGame.SCRATCH_CELLS; i++) {
                    if (crossed(from, scratchAt(i))) {
                        boolean win = game.getPicked().contains(i);
                        if (sounds != null) sounds.coinsGain.play(win ? 0.7f : 0.25f, 1f + i * 0.04f, 0f);
                        float[] c = scratchCenter(i);
                        burst(c[0], c[1], win ? 10 : 4, win ? GOLD_LIGHT : SILVER);
                    }
                }
            }
        }
        if (crossed(from, totalAt)) {
            sound(total, false);
            burst(worldWidth() / 2f, totalY(), 40, GOLD);
        }
        update(sparks, delta);
    }

    private boolean crossed(float from, float at) { return from < at && time >= at; }

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

    // -------------------------------------------------------------------------
    // Dessin
    // -------------------------------------------------------------------------

    private float unitSize() { return Math.min(worldHeight() * 0.11f, worldWidth() * 0.08f); }
    private float centerY()  { return worldHeight() * 0.48f; }
    private float totalY()   { return centerY() - unitSize() * 3.2f; }

    @Override
    protected void drawScene(Batch batch) {
        float dark = 0.92f * Interpolation.pow2Out.apply(progress(0f, 0.8f));
        fill(batch, NIGHT, dark);
        float appear = Interpolation.pow2Out.apply(progress(SHOW, START));
        if (appear > 0f) {
            switch (game.getKind()) {
                case WHEEL -> drawWheel(batch, appear);
                case CHESTS -> drawChests(batch, appear);
                default -> drawTicket(batch, appear);
            }
            drawCaptions(batch, appear);
        }
        drawBanner(batch);
        drawParticles(batch, sparks);
    }

    /** Le titre : il éclate au centre, puis monte au-dessus du jeu. */
    private void drawBanner(Batch batch) {
        float pop = Interpolation.swingOut.apply(progress(0f, 0.45f));
        float rise = Interpolation.pow2.apply(progress(0.8f, SHOW + 0.3f));
        float y = MathUtils.lerp(worldHeight() * 0.55f, centerY() + unitSize() * (game.getKind() == BonusGame.Kind.WHEEL ? 3.9f : 3.3f), rise);
        float scale = MathUtils.lerp(3.4f, 2.0f, rise) * pop;
        if (scale <= 0.01f) return;
        glow(batch, worldWidth() / 2f, y, 420f * scale / 3f, GOLD, 0.35f * (1f - 0.5f * rise));
        font.getData().setScale(scale);
        caption(batch, font, title(game.getKind()), worldWidth() / 2f, y, 1f);
        font.getData().setScale(1f);
    }

    private void box(Batch batch, float x, float y, float w, float h, Color color, float alpha) {
        batch.setColor(color.r, color.g, color.b, alpha);
        batch.draw(pixel, x, y, w, h);
    }

    private void label(Batch batch, String text, float x, float y, float scale, float alpha) {
        font.getData().setScale(scale);
        caption(batch, font, text, x, y, alpha);
        font.getData().setScale(1f);
    }

    // --- Roue -----------------------------------------------------------------

    private float wheelX()      { return worldWidth() / 2f; }
    private float wheelY()      { return centerY() - unitSize() * 0.15f; }
    private float wheelRadius() { return unitSize() * 2.1f; }

    /** @return l'angle de la roue : elle tourne vite, ralentit et s'arrête la case gagnante en haut. */
    private float wheelAngle() {
        int count = game.getValues().size();
        float slice = 360f / count;
        float target = 90f - game.getPicked().get(0) * slice + 360f * 5f; // 5 tours, puis la case gagnante en haut
        float k = Interpolation.pow3Out.apply(progress(START, reveal));
        return target * k;
    }

    private void drawWheel(Batch batch, float alpha) {
        List<Double> values = game.getValues();
        int count = values.size();
        float slice = 360f / count, angle = wheelAngle(), r = wheelRadius(), cx = wheelX(), cy = wheelY();
        glow(batch, cx, cy, r * 3.2f, GOLD, 0.18f * alpha);
        // Disque : anneau doré, fond rouge sombre.
        float disc = r * 1.25f;
        batch.setColor(GOLD_DARK.r, GOLD_DARK.g, GOLD_DARK.b, alpha);
        batch.draw(ring, cx - disc, cy - disc, disc * 2f, disc * 2f);
        int winner = game.getPicked().get(0);
        boolean stopped = time >= reveal;
        for (int i = 0; i < count; i++) {
            float a = (angle + i * slice) * MathUtils.degreesToRadians;
            float x = cx + MathUtils.cos(a) * r, y = cy + MathUtils.sin(a) * r;
            float tile = unitSize() * 0.95f;
            boolean lit = stopped && i == winner;
            Color back = lit ? GOLD : (i % 2 == 0 ? RED : RED_DARK);
            // rayon vers le centre
            batch.setColor(GOLD_DARK.r, GOLD_DARK.g, GOLD_DARK.b, 0.8f * alpha);
            float mx = (cx + x) / 2f, my = (cy + y) / 2f;
            batch.draw(pixel, mx - r / 2f, my - 2f, r / 2f, 2f, r, 4f, 1f, 1f, a * MathUtils.radiansToDegrees);
            box(batch, x - tile / 2f - 3f, y - tile / 2f - 3f, tile + 6f, tile + 6f, GOLD_DARK, alpha);
            box(batch, x - tile / 2f, y - tile / 2f, tile, tile, back, alpha);
            if (lit) glow(batch, x, y, tile * 2.6f, GOLD_LIGHT, (0.3f + 0.15f * MathUtils.sin(time * 6f)) * alpha);
            label(batch, times(values.get(i)), x, y, 1.1f, alpha);
        }
        // Moyeu et flèche en haut.
        float hub = unitSize() * 0.45f;
        box(batch, cx - hub / 2f, cy - hub / 2f, hub, hub, GOLD, alpha);
        float tip = cy + r + unitSize() * 0.72f;
        batch.setColor(CREAM.r, CREAM.g, CREAM.b, alpha);
        float s = unitSize() * 0.32f;
        batch.draw(pixel, cx - s / 2f, tip - s / 2f, s / 2f, s / 2f, s, s, 1f, 1f, 45f);
    }

    // --- Coffres --------------------------------------------------------------

    private float chestOpen(int order) { return START + 0.4f + CHEST_STEP * (order + 1) - 0.5f; }

    private float[] chestCenter(int index) {
        float step = unitSize() * 1.7f;
        int row = index / 3, col = index % 3;
        return new float[] {worldWidth() / 2f + (col - 1) * step, centerY() - unitSize() * 0.3f + (1 - row) * step};
    }

    private void drawChests(Batch batch, float alpha) {
        List<Double> values = game.getValues();
        float w = unitSize() * 1.3f, h = unitSize() * 1.0f;
        for (int i = 0; i < values.size(); i++) {
            float[] c = chestCenter(i);
            int order = game.getPicked().indexOf(i);
            boolean opened = order >= 0 && time >= chestOpen(order);
            boolean shown = opened || time >= reveal;   // les autres se dévoilent à la fin, en sombre
            float lift = opened ? Interpolation.pow2Out.apply(progress(chestOpen(order), chestOpen(order) + 0.35f)) : 0f;
            float a = order < 0 && time >= reveal ? 0.5f * alpha : alpha;
            if (opened) glow(batch, c[0], c[1], w * 2.4f, GOLD_LIGHT, 0.35f * alpha);
            // caisse
            box(batch, c[0] - w / 2f, c[1] - h / 2f, w, h * 0.62f, WOOD, a);
            box(batch, c[0] - w / 2f, c[1] - h / 2f + h * 0.25f, w, h * 0.08f, GOLD_DARK, a);
            // couvercle qui se soulève
            float ly = c[1] - h / 2f + h * 0.62f + lift * h * 0.45f;
            box(batch, c[0] - w / 2f, ly, w, h * 0.38f, WOOD_DARK, a);
            box(batch, c[0] - w * 0.08f, ly - h * 0.06f, w * 0.16f, h * 0.2f, GOLD, a);
            if (shown) {
                if (opened) box(batch, c[0] - w * 0.4f, c[1] - h * 0.05f, w * 0.8f, h * 0.18f, GOLD_LIGHT, alpha);
                label(batch, times(values.get(i)), c[0], ly + h * 0.38f + h * 0.28f, opened ? 1.3f : 1.0f, a);
            }
        }
    }

    // --- Ticket à gratter -----------------------------------------------------

    private float scratchAt(int index) { return START + 0.4f + SCRATCH_STEP * (index + 1); }

    private float[] scratchCenter(int index) {
        float step = unitSize() * 1.35f;
        int row = index / 3, col = index % 3;
        return new float[] {worldWidth() / 2f + (col - 1) * step, centerY() + (1 - row) * step};
    }

    private void drawTicket(Batch batch, float alpha) {
        float step = unitSize() * 1.35f, cell = unitSize() * 1.15f;
        float tw = step * 3f + unitSize() * 0.6f, th = step * 3f + unitSize() * 0.6f;
        float tx = worldWidth() / 2f - tw / 2f, ty = centerY() - th / 2f;
        box(batch, tx - 6f, ty - 6f, tw + 12f, th + 12f, GOLD_DARK, alpha);
        box(batch, tx, ty, tw, th, CREAM, alpha);
        box(batch, tx, ty + th - unitSize() * 0.12f, tw, unitSize() * 0.12f, GREEN, alpha);
        List<Double> values = game.getValues();
        for (int i = 0; i < values.size(); i++) {
            float[] c = scratchCenter(i);
            float x = c[0] - cell / 2f, y = c[1] - cell / 2f;
            boolean win = game.getPicked().contains(i) && time >= reveal;
            box(batch, x, y, cell, cell, win ? GOLD : INK, alpha);
            if (win) glow(batch, c[0], c[1], cell * 2.2f, GOLD_LIGHT, (0.25f + 0.1f * MathUtils.sin(time * 6f)) * alpha);
            label(batch, times(values.get(i)), c[0], c[1], 1.3f, alpha);
            // la couche argentée part de haut en bas quand la case se gratte
            float k = Interpolation.pow2Out.apply(progress(scratchAt(i) - SCRATCH_STEP, scratchAt(i)));
            if (k < 1f) {
                float left = cell * (1f - k);
                box(batch, x, y, cell, left, SILVER, alpha);
                for (int s = 0; s < 4; s++) {
                    box(batch, x + cell * 0.1f + s * cell * 0.22f, y + left * 0.3f, cell * 0.06f, left * 0.4f,
                        SILVER_DARK, alpha);
                }
            }
        }
    }

    // --- Textes ----------------------------------------------------------------

    private void drawCaptions(Batch batch, float alpha) {
        float y = totalY();
        if (time < totalAt) {
            String line = game.getPot() > 0
                ? Lang.f("MISE DE BASE : CAGNOTTE {0} + {1}", Lang.big(game.getPot()), Lang.big(game.getUnit() - game.getPot()))
                : Lang.f("MISE DE BASE : {0}", Lang.big(game.getUnit()));
            label(batch, line, worldWidth() / 2f, y, 1.1f, alpha);
        } else {
            float pop = Interpolation.swingOut.apply(progress(totalAt, totalAt + 0.4f));
            glow(batch, worldWidth() / 2f, y, 360f, GOLD, 0.3f * pop);
            label(batch, Lang.f("GAINS +{0}", Lang.big(game.getTotal())), worldWidth() / 2f, y, Math.max(0.01f, 2.4f * pop), 1f);
        }
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
                if (sound != null) sound.stop(entry[1]);
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
