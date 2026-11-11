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
    private static final Color BULB_OFF = hex("3a2a1a"), BEVEL = hex("ffffff");
    private static final Color[] CONFETTI = {GOLD, RED, hex("3ac1ff"), hex("5fd36a"), hex("ff7ad9"), CREAM};

    private final BonusGame game;
    private final BitmapFont font;
    private final GameSounds sounds;
    private final Sound intro, music, total;
    private final Sound[] owned;
    private final List<long[]> playing = new ArrayList<>();
    private final Array<Particle> sparks = new Array<>(false, 128);
    /** Poussière d'or du fond, confettis, copeaux d'argent du ticket et pièces qui volent. */
    private final Array<Particle> dust = new Array<>(false, 128), confetti = new Array<>(false, 256);
    private final Array<Particle> flakes = new Array<>(false, 128), coins = new Array<>(false, 64);
    private final TextureRegion coin;
    /** Le résultat se montre ; puis le total. */
    private final float reveal, totalAt;
    private float closeAt, lastTime, dustTimer;
    /** Assombrit ce que dessine {@link #box} (les coffres restés fermés). */
    private float shade = 1f;
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
        coin = load("hud/coin.png");
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
        clearParticles();
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
            lastTime = totalAt - 0.001f;   // le total éclate quand même (pièces, confettis)
            clearParticles();
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
        boolean rich = !settings.isReducedEffects();
        if (crossed(from, START)) {
            sound(music, true);
            if (game.getKind() == BonusGame.Kind.WHEEL && sounds != null) spinLoop = sounds.reelSpin.loop(0.5f);
        }
        // poussière d'or qui monte doucement derrière le jeu
        if (rich && time >= SHOW) {
            dustTimer -= delta;
            while (dustTimer <= 0f) {
                dustTimer += 0.07f;
                Particle mote = particle(random.nextFloat() * worldWidth(), -10f, 80f + random.nextFloat() * 20f,
                    25f + random.nextFloat() * 45f, 3f + random.nextFloat() * 3f, 5f + random.nextFloat() * 9f);
                mote.color.set(random.nextBoolean() ? GOLD : GOLD_LIGHT);
                mote.color.a = 0.55f;
                dust.add(mote);
            }
        }
        switch (game.getKind()) {
            case WHEEL -> {
                if (crossed(from, reveal)) {
                    stopSpinLoop();
                    if (sounds != null) sounds.reelStop.play(0.8f);
                    float[] tile = wheelTile(game.getPicked().get(0));
                    burst(tile[0], tile[1], 28, GOLD_LIGHT);
                    confettiBurst(tile[0], tile[1], 40);
                    rumble(0.3f, 5f);
                }
            }
            case CHESTS -> {
                for (int i = 0; i < game.getPicked().size(); i++) {
                    if (crossed(from, chestOpen(i))) {
                        if (sounds != null) sounds.coinsGain.play(0.7f, 1f + i * 0.1f, 0f);
                        float[] c = chestCenter(game.getPicked().get(i));
                        burst(c[0], c[1], 18, GOLD_LIGHT);
                        fountain(c[0], c[1] + unitSize() * 0.2f, 10);
                        rumble(0.15f, 3f);
                    }
                }
            }
            default -> {
                float cell = unitSize() * 1.15f;
                for (int i = 0; i < BonusGame.SCRATCH_CELLS; i++) {
                    float[] c = scratchCenter(i);
                    if (rich && time > scratchAt(i) - SCRATCH_STEP && time < scratchAt(i)) {
                        // les copeaux d'argent tombent du bord gratté
                        float left = cell * (1f - scratchProgress(i));
                        for (int n = 0; n < 2; n++) {
                            Particle flake = particle(c[0] - cell / 2f + random.nextFloat() * cell,
                                c[1] - cell / 2f + left, 200f + random.nextFloat() * 140f, 40f + random.nextFloat() * 80f,
                                0.6f + random.nextFloat() * 0.4f, 3f + random.nextFloat() * 4f);
                            flake.gravity = 500f;
                            flake.spin = (random.nextFloat() - 0.5f) * 900f;
                            flake.color.set(random.nextBoolean() ? SILVER : SILVER_DARK);
                            flakes.add(flake);
                        }
                    }
                    if (crossed(from, scratchAt(i))) {
                        boolean win = game.getPicked().contains(i);
                        if (sounds != null) sounds.coinsGain.play(win ? 0.7f : 0.25f, 1f + i * 0.04f, 0f);
                        burst(c[0], c[1], win ? 10 : 4, win ? GOLD_LIGHT : SILVER);
                    }
                }
                if (crossed(from, reveal)) {
                    for (int i : game.getPicked()) {
                        float[] c = scratchCenter(i);
                        burst(c[0], c[1], 14, GOLD_LIGHT);
                        confettiBurst(c[0], c[1], 14);
                    }
                    rumble(0.25f, 4f);
                }
            }
        }
        if (crossed(from, totalAt)) {
            sound(total, false);
            burst(worldWidth() / 2f, totalY(), 40, GOLD);
            coinShower(rich ? 36 : 10);
            confettiBurst(worldWidth() * 0.12f, totalY(), 30);
            confettiBurst(worldWidth() * 0.88f, totalY(), 30);
        }
        update(sparks, delta);
        update(dust, delta);
        update(coins, delta);
        update(confetti, delta);
        update(flakes, delta);
        for (Particle piece : confetti) piece.vx += MathUtils.sin(piece.age * 7f + piece.spin) * 40f * delta; // ça virevolte
    }

    private void clearParticles() {
        sparks.clear();
        dust.clear();
        confetti.clear();
        flakes.clear();
        coins.clear();
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

    /** Des confettis de toutes les couleurs, lancés vers le haut, qui retombent en virevoltant. */
    private void confettiBurst(float x, float y, int count) {
        if (settings.isReducedEffects()) count = Math.max(1, count / 4);
        for (int i = 0; i < count; i++) {
            Particle piece = particle(x, y, 50f + random.nextFloat() * 80f, 220f + random.nextFloat() * 260f,
                1.6f + random.nextFloat() * 1.0f, 5f + random.nextFloat() * 5f);
            piece.gravity = 380f;
            piece.drag = 0.4f;
            piece.rotation = random.nextFloat() * 360f;
            piece.spin = (random.nextFloat() - 0.5f) * 720f;
            piece.color.set(CONFETTI[random.nextInt(CONFETTI.length)]);
            confetti.add(piece);
        }
    }

    /** Des pièces jaillissent de ({@code x}, {@code y}) et retombent en tournant. */
    private void fountain(float x, float y, int count) {
        if (settings.isReducedEffects()) count = Math.max(1, count / 3);
        for (int i = 0; i < count; i++) {
            Particle piece = particle(x, y, 65f + random.nextFloat() * 50f, 260f + random.nextFloat() * 200f,
                1.1f + random.nextFloat() * 0.4f, 0.28f + random.nextFloat() * 0.12f);
            piece.gravity = 900f;
            piece.spin = 400f + random.nextFloat() * 500f;
            coins.add(piece);
        }
    }

    /** La pluie de pièces du total, du haut de l'écran. */
    private void coinShower(int count) {
        for (int i = 0; i < count; i++) {
            Particle piece = particle(random.nextFloat() * worldWidth(), worldHeight() + 30f + random.nextFloat() * 260f,
                270f, 80f + random.nextFloat() * 120f, 2.6f + random.nextFloat() * 0.8f, 0.3f + random.nextFloat() * 0.18f);
            piece.gravity = 260f;
            piece.spin = 300f + random.nextFloat() * 600f;
            coins.add(piece);
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
            drawRays(batch, appear);
            drawParticles(batch, dust);
            drawBulbs(batch, appear);
            switch (game.getKind()) {
                case WHEEL -> drawWheel(batch, appear);
                case CHESTS -> drawChests(batch, appear);
                default -> drawTicket(batch, appear);
            }
            drawCaptions(batch, appear);
            BonusRankBadge.draw(this, batch, font, game.getKind().rank, worldWidth() / 2f + unitSize() * 4.4f,
                centerY() + unitSize() * 1.4f, START, appear);
        }
        drawBanner(batch);
        drawParticles(batch, sparks);
        drawPieces(batch, flakes);
        drawPieces(batch, confetti);
        drawCoins(batch);
    }

    /** Le titre : il éclate au centre, puis monte au-dessus du jeu. Un reflet passe dessus. */
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
        // un éclat de lumière balaie le titre toutes les trois secondes
        float sweep = (time % 3f) / 0.6f;
        if (sweep < 1f && time > 0.5f) {
            float half = 210f * scale / 2f;
            glow(batch, worldWidth() / 2f - half + sweep * half * 2f, y, 60f * scale / 2f, CREAM, 0.5f * (1f - Math.abs(sweep * 2f - 1f)));
        }
    }

    /** De grands rayons dorés qui tournent lentement derrière le jeu. */
    private void drawRays(Batch batch, float alpha) {
        if (settings.isReducedEffects()) return;
        float cx = worldWidth() / 2f, cy = centerY(), length = worldWidth() * 0.62f, thick = unitSize() * 1.6f;
        additive(batch);
        int count = 14;
        for (int i = 0; i < count; i++) {
            float degrees = time * 9f + i * 360f / count;
            Color color = i % 2 == 0 ? GOLD : RED;
            batch.setColor(color.r, color.g, color.b, (i % 2 == 0 ? 0.3f : 0.18f) * alpha);
            // la traînée est éclatante côté centre et s'efface vers l'extérieur
            batch.draw(trail, cx - length, cy - thick / 2f, length, thick / 2f, length, thick, 1f, 1f, degrees + 180f);
        }
        normal(batch);
    }

    /** Une rangée d'ampoules en chenillard tout autour de l'écran. */
    private void drawBulbs(Batch batch, float alpha) {
        float w = worldWidth(), h = worldHeight(), step = 34f, size = 9f, inset = 14f;
        boolean party = time >= reveal;
        int index = 0;
        for (float x = inset; x <= w - inset; x += step) {
            bulb(batch, x, inset, size, index, party, alpha);
            bulb(batch, w - x, h - inset, size, index++, party, alpha);
        }
        for (float y = inset + step; y <= h - inset - step; y += step) {
            bulb(batch, w - inset, y, size, index, party, alpha);
            bulb(batch, inset, h - y, size, index++, party, alpha);
        }
    }

    private void bulb(Batch batch, float x, float y, float size, int index, boolean party, float alpha) {
        boolean lit = party ? MathUtils.sin(time * 14f) > -0.2f : (index + (int) (time * 9f)) % 3 != 0;
        if (lit) glow(batch, x, y, size * 4f, GOLD, 0.35f * alpha);
        Color color = lit ? GOLD_LIGHT : BULB_OFF;
        batch.setColor(color.r, color.g, color.b, alpha);
        batch.draw(pixel, x - size / 2f, y - size / 2f, size, size);
    }

    private void box(Batch batch, float x, float y, float w, float h, Color color, float alpha) {
        batch.setColor(color.r * shade, color.g * shade, color.b * shade, alpha);
        batch.draw(pixel, x, y, w, h);
    }

    /** Un losange plein centré sur ({@code x}, {@code y}). */
    private void diamond(Batch batch, float x, float y, float size, Color color, float alpha) {
        batch.setColor(color.r, color.g, color.b, alpha);
        batch.draw(pixel, x - size / 2f, y - size / 2f, size / 2f, size / 2f, size, size, 1f, 1f, 45f);
    }

    /** Une petite étoile scintillante (une croix de lumière). */
    private void twinkle(Batch batch, float x, float y, float size, float alpha) {
        if (alpha <= 0.01f) return;
        additive(batch);
        batch.setColor(CREAM.r, CREAM.g, CREAM.b, alpha);
        batch.draw(pixel, x - size / 2f, y - 1f, size, 2f);
        batch.draw(pixel, x - 1f, y - size / 2f, 2f, size);
        normal(batch);
        glow(batch, x, y, size * 1.4f, GOLD_LIGHT, alpha * 0.6f);
    }

    private void label(Batch batch, String text, float x, float y, float scale, float alpha) {
        font.getData().setScale(scale);
        caption(batch, font, text, x, y, alpha);
        font.getData().setScale(1f);
    }

    /** Confettis et copeaux : de petits rectangles qui tournent. */
    private void drawPieces(Batch batch, Array<Particle> pieces) {
        for (Particle piece : pieces) {
            float alpha = 1f - piece.life() * piece.life();
            float flip = Math.abs(MathUtils.cosDeg(piece.rotation * 1.7f));   // il se retourne en tombant
            batch.setColor(piece.color.r * (0.6f + 0.4f * flip), piece.color.g * (0.6f + 0.4f * flip),
                piece.color.b * (0.6f + 0.4f * flip), alpha);
            float w = piece.size, h = piece.size * (0.4f + 0.6f * flip);
            batch.draw(pixel, piece.x - w / 2f, piece.y - h / 2f, w / 2f, h / 2f, w, h, 1f, 1f, piece.rotation);
        }
    }

    /** Les pièces : elles tournent sur elles-mêmes (elles s'aplatissent puis reviennent). */
    private void drawCoins(Batch batch) {
        for (Particle piece : coins) {
            float alpha = Math.min(1f, (1f - piece.life()) * 4f);
            float squash = Math.abs(MathUtils.cosDeg(piece.rotation));
            float w = coin.getRegionWidth() * piece.size, h = coin.getRegionHeight() * piece.size;
            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(coin, piece.x - w * Math.max(0.12f, squash) / 2f, piece.y - h / 2f, w * Math.max(0.12f, squash), h);
        }
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

    private float[] wheelTile(int index) {
        float a = (wheelAngle() + index * 360f / game.getValues().size()) * MathUtils.degreesToRadians;
        return new float[] {wheelX() + MathUtils.cos(a) * wheelRadius(), wheelY() + MathUtils.sin(a) * wheelRadius()};
    }

    private void drawWheel(Batch batch, float alpha) {
        List<Double> values = game.getValues();
        int count = values.size();
        float slice = 360f / count, angle = wheelAngle(), r = wheelRadius(), cx = wheelX(), cy = wheelY();
        boolean stopped = time >= reveal;
        float spinning = time >= START && !stopped ? 1f - progress(START, reveal) : 0f;
        glow(batch, cx, cy, r * 3.2f, GOLD, (0.18f + (stopped ? 0.08f * MathUtils.sin(time * 5f) : 0f)) * alpha);
        // Disque : anneau doré épais, fond rouge sombre.
        float disc = r * 1.32f;
        batch.setColor(GOLD_DARK.r, GOLD_DARK.g, GOLD_DARK.b, alpha);
        batch.draw(ring, cx - disc, cy - disc, disc * 2f, disc * 2f);
        batch.setColor(GOLD.r, GOLD.g, GOLD.b, 0.7f * alpha);
        batch.draw(ring, cx - disc * 0.96f, cy - disc * 0.96f, disc * 1.92f, disc * 1.92f);
        // Ampoules sur l'anneau : elles courent pendant que la roue tourne, puis clignotent toutes.
        int bulbs = 24;
        for (int i = 0; i < bulbs; i++) {
            float a = (i * 360f / bulbs - angle * 0.5f) * MathUtils.degreesToRadians;
            float bx = cx + MathUtils.cos(a) * disc * 0.93f, by = cy + MathUtils.sin(a) * disc * 0.93f;
            boolean lit = stopped ? MathUtils.sin(time * 12f + (i % 2) * MathUtils.PI) > 0f : (i + (int) (time * 12f)) % 4 < 2;
            float size = unitSize() * 0.12f;
            if (lit) glow(batch, bx, by, size * 4f, GOLD_LIGHT, 0.4f * alpha);
            box(batch, bx - size / 2f, by - size / 2f, size, size, lit ? CREAM : GOLD_DARK, alpha);
        }
        int winner = game.getPicked().get(0);
        float beat = stopped ? 1f + 0.1f * Math.abs(MathUtils.sin(time * 5f)) : 1f;
        for (int i = 0; i < count; i++) {
            float a = (angle + i * slice) * MathUtils.degreesToRadians;
            float x = cx + MathUtils.cos(a) * r, y = cy + MathUtils.sin(a) * r;
            boolean lit = stopped && i == winner;
            float tile = unitSize() * 0.95f * (lit ? beat : 1f);
            Color back = lit ? GOLD : (i % 2 == 0 ? RED : RED_DARK);
            // rayon vers le centre
            batch.setColor(GOLD_DARK.r, GOLD_DARK.g, GOLD_DARK.b, 0.8f * alpha);
            float mx = (cx + x) / 2f, my = (cy + y) / 2f;
            batch.draw(pixel, mx - r / 2f, my - 2f, r / 2f, 2f, r, 4f, 1f, 1f, a * MathUtils.radiansToDegrees);
            // traînée de mouvement quand la roue va vite
            if (spinning > 0.25f && !settings.isReducedEffects()) {
                additive(batch);
                batch.setColor(back.r, back.g, back.b, 0.35f * spinning * alpha);
                float tail = tile * 1.6f * spinning;
                batch.draw(trail, x - tail, y - tile * 0.35f, tail, tile * 0.35f, tail, tile * 0.7f, 1f, 1f,
                    a * MathUtils.radiansToDegrees + 90f);
                normal(batch);
            }
            box(batch, x - tile / 2f - 3f, y - tile / 2f - 3f, tile + 6f, tile + 6f, lit ? CREAM : GOLD_DARK, alpha);
            box(batch, x - tile / 2f, y - tile / 2f, tile, tile, back, alpha);
            box(batch, x - tile / 2f, y + tile / 2f - tile * 0.14f, tile, tile * 0.12f, lit ? GOLD_LIGHT : BEVEL, 0.25f * alpha);
            if (lit) {
                glow(batch, x, y, tile * 2.6f, GOLD_LIGHT, (0.3f + 0.15f * MathUtils.sin(time * 6f)) * alpha);
                twinkle(batch, x + tile * 0.45f, y + tile * 0.45f, tile * 0.5f * Math.abs(MathUtils.sin(time * 4f)), alpha);
            }
            label(batch, times(values.get(i)), x, y, 1.1f, alpha);
        }
        // Moyeu : un soleil doré qui tourne avec la roue.
        float hub = unitSize() * 0.55f;
        glow(batch, cx, cy, hub * 3f, GOLD_LIGHT, 0.35f * alpha);
        batch.setColor(GOLD_DARK.r, GOLD_DARK.g, GOLD_DARK.b, alpha);
        batch.draw(pixel, cx - hub / 2f, cy - hub / 2f, hub / 2f, hub / 2f, hub, hub, 1f, 1f, angle);
        batch.setColor(GOLD.r, GOLD.g, GOLD.b, alpha);
        batch.draw(pixel, cx - hub * 0.4f, cy - hub * 0.4f, hub * 0.4f, hub * 0.4f, hub * 0.8f, hub * 0.8f, 1f, 1f, angle + 45f);
        diamond(batch, cx, cy, hub * 0.35f, CREAM, alpha);
        // Flèche en haut : elle cogne chaque case qui passe, puis se calme.
        float phase = ((angle + slice / 2f - 90f) % slice + slice) % slice / slice;
        float kick = spinning > 0f ? (float) Math.pow(1f - phase, 5) * (8f + 22f * spinning) : 0f;
        float tip = cy + r + unitSize() * 0.72f;
        float s = unitSize() * 0.36f;
        glow(batch, cx, tip, s * 3f, RED, 0.3f * alpha);
        batch.setColor(GOLD_DARK.r, GOLD_DARK.g, GOLD_DARK.b, alpha);
        batch.draw(pixel, cx - s * 0.6f, tip - s * 0.6f, s * 0.6f, s * 0.6f + s * 0.3f, s * 1.2f, s * 1.2f, 1f, 1f, 45f + kick);
        batch.setColor(RED.r, RED.g, RED.b, alpha);
        batch.draw(pixel, cx - s / 2f, tip - s / 2f, s / 2f, s / 2f + s * 0.3f, s, s, 1f, 1f, 45f + kick);
        diamond(batch, cx, tip + s * 0.1f, s * 0.3f, CREAM, alpha);
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
            float lift = opened ? Interpolation.swingOut.apply(progress(chestOpen(order), chestOpen(order) + 0.4f)) : 0f;
            float a = alpha;
            shade = order < 0 && time >= reveal ? 0.45f : 1f;
            // le coffre choisi tremble juste avant de s'ouvrir, une lueur passe sous le couvercle
            float before = order >= 0 && !opened ? progress(chestOpen(order) - 0.45f, chestOpen(order)) : 0f;
            float x = c[0] + (before > 0f ? MathUtils.sin(time * 70f) * 4f * before : 0f);
            float y = c[1] + (!opened && time < reveal ? MathUtils.sin(time * 2.2f + i) * 2f : 0f);
            float bottom = y - h / 2f, bodyH = h * 0.62f;
            if (opened) {
                glow(batch, x, y + h * 0.2f, w * 2.6f, GOLD_LIGHT, (0.35f + 0.08f * MathUtils.sin(time * 5f + i)) * alpha);
                drawBeams(batch, x, bottom + bodyH, w, alpha * Math.min(1f, (time - chestOpen(order)) * 3f), i);
            }
            // ombre au sol
            box(batch, x - w * 0.55f, bottom - h * 0.1f, w * 1.1f, h * 0.08f, NIGHT, 0.5f * a);
            // caisse : planches, cerclage doré, coins de métal et rivets
            box(batch, x - w / 2f, bottom, w, bodyH, WOOD, a);
            box(batch, x - w / 2f, bottom + bodyH * 0.33f, w, 2f, WOOD_DARK, a);
            box(batch, x - w / 2f, bottom + bodyH * 0.66f, w, 2f, WOOD_DARK, a);
            box(batch, x - w / 2f, bottom + h * 0.25f, w, h * 0.08f, GOLD_DARK, a);
            for (int side = -1; side <= 1; side += 2) {
                float sx = x + side * (w / 2f - w * 0.06f);
                box(batch, sx - w * 0.06f, bottom, w * 0.12f, bodyH, GOLD_DARK, a);
                box(batch, sx - 2f, bottom + bodyH * 0.2f, 4f, 4f, GOLD_LIGHT, a);
                box(batch, sx - 2f, bottom + bodyH * 0.75f, 4f, 4f, GOLD_LIGHT, a);
            }
            if (opened) {
                // l'intérieur : un tas d'or qui brille
                box(batch, x - w * 0.42f, bottom + bodyH - h * 0.06f, w * 0.84f, h * 0.12f, GOLD_DARK, alpha);
                box(batch, x - w * 0.38f, bottom + bodyH, w * 0.76f, h * 0.1f, GOLD, alpha);
                batch.setColor(1f, 1f, 1f, alpha);
                for (int g = 0; g < 4; g++) {
                    sprite(batch, coin, x - w * 0.3f + g * w * 0.2f, bottom + bodyH + h * 0.12f + (g % 2) * h * 0.06f,
                        h * 0.2f / coin.getRegionHeight(), 0f);
                }
                twinkle(batch, x + w * 0.2f, bottom + bodyH + h * 0.2f, h * 0.3f * Math.abs(MathUtils.sin(time * 4f + i)), alpha);
            }
            // couvercle qui se soulève et bascule en arrière
            float lidH = h * 0.38f * (1f - 0.45f * lift);
            float ly = bottom + bodyH + lift * h * 0.5f;
            if (before > 0f) box(batch, x - w / 2f, bottom + bodyH - 1f, w, 3f + before * 3f, GOLD_LIGHT, before * alpha);
            box(batch, x - w / 2f - 2f, ly, w + 4f, lidH, WOOD_DARK, a);
            box(batch, x - w / 2f + w * 0.08f, ly + lidH, w * 0.84f, h * 0.07f * (1f - lift), WOOD_DARK, a); // le dessus bombé
            box(batch, x - w / 2f - 2f, ly, w + 4f, h * 0.05f, GOLD_DARK, a);
            box(batch, x - w / 2f + w * 0.1f, ly, w * 0.08f, lidH, GOLD_DARK, a);
            box(batch, x + w / 2f - w * 0.18f, ly, w * 0.08f, lidH, GOLD_DARK, a);
            // serrure et son trou
            if (!opened) {
                box(batch, x - w * 0.09f, ly - h * 0.1f, w * 0.18f, h * 0.22f, GOLD, a);
                box(batch, x - w * 0.025f, ly - h * 0.06f, w * 0.05f, h * 0.1f, INK, a);
            }
            if (!opened && time < reveal) {
                // un reflet scintille de temps en temps sur les coffres fermés
                float spark = Math.max(0f, MathUtils.sin(time * 1.7f + i * 2.3f) - 0.85f) / 0.15f;
                twinkle(batch, x - w * 0.3f + (i % 3) * w * 0.2f, ly + lidH * 0.6f, h * 0.35f * spark, spark * alpha);
            }
            shade = 1f;
            if (shown) {
                label(batch, times(values.get(i)), x, ly + lidH + h * 0.28f, opened ? 1.3f : 1.0f, order < 0 ? 0.6f * a : a);
            }
        }
    }

    /** Des rayons de lumière qui montent en éventail d'un coffre ouvert. */
    private void drawBeams(Batch batch, float x, float y, float w, float alpha, int seed) {
        if (settings.isReducedEffects() || alpha <= 0f) return;
        additive(batch);
        float length = unitSize() * 1.5f;
        for (int b = 0; b < 5; b++) {
            float degrees = 90f + (b - 2) * 14f + MathUtils.sin(time * 1.5f + b + seed) * 4f;
            float thick = w * 0.4f;
            batch.setColor(GOLD_LIGHT.r, GOLD_LIGHT.g, GOLD_LIGHT.b, (0.6f + 0.25f * MathUtils.sin(time * 4f + b)) * alpha);
            batch.draw(trail, x - length, y - thick / 2f, length, thick / 2f, length, thick, 1f, 1f, degrees + 180f);
        }
        normal(batch);
    }

    // --- Ticket à gratter -----------------------------------------------------

    private float scratchAt(int index) { return START + 0.4f + SCRATCH_STEP * (index + 1); }

    private float scratchProgress(int index) {
        return Interpolation.pow2Out.apply(progress(scratchAt(index) - SCRATCH_STEP, scratchAt(index)));
    }

    private float[] scratchCenter(int index) {
        float step = unitSize() * 1.35f;
        int row = index / 3, col = index % 3;
        return new float[] {worldWidth() / 2f + (col - 1) * step, centerY() + (1 - row) * step};
    }

    private void drawTicket(Batch batch, float alpha) {
        float step = unitSize() * 1.35f, cell = unitSize() * 1.15f;
        float tw = step * 3f + unitSize() * 0.6f, th = step * 3f + unitSize() * 0.6f;
        float tx = worldWidth() / 2f - tw / 2f, ty = centerY() - th / 2f;
        glow(batch, worldWidth() / 2f, centerY(), tw * 2f, GOLD, 0.15f * alpha);
        box(batch, tx - 6f + 8f, ty - 6f - 8f, tw + 12f, th + 12f, NIGHT, 0.6f * alpha);   // ombre
        box(batch, tx - 6f, ty - 6f, tw + 12f, th + 12f, GOLD_DARK, alpha);
        box(batch, tx - 2f, ty - 2f, tw + 4f, th + 4f, GOLD, alpha);
        box(batch, tx, ty, tw, th, CREAM, alpha);
        // motif de petits losanges pâles sur le fond
        float motif = unitSize() * 0.3f;
        for (float my = ty + motif; my < ty + th - motif * 0.5f; my += motif) {
            for (float mx = tx + motif * ((int) ((my - ty) / motif) % 2 == 0 ? 1f : 0.5f); mx < tx + tw - motif * 0.4f; mx += motif) {
                diamond(batch, mx, my, motif * 0.22f, GOLD, 0.3f * alpha);
            }
        }
        // pointillés dorés à l'intérieur du cadre
        float dot = 4f, inset = unitSize() * 0.1f;
        for (float dx = tx + inset; dx < tx + tw - inset; dx += dot * 3f) {
            box(batch, dx, ty + inset, dot, dot, GOLD_DARK, alpha);
            box(batch, dx, ty + th - inset - dot, dot, dot, GOLD_DARK, alpha);
        }
        for (float dy = ty + inset; dy < ty + th - inset; dy += dot * 3f) {
            box(batch, tx + inset, dy, dot, dot, GOLD_DARK, alpha);
            box(batch, tx + tw - inset - dot, dy, dot, dot, GOLD_DARK, alpha);
        }
        // encoches de ticket sur les côtés
        for (int side = 0; side < 2; side++) {
            float nx = side == 0 ? tx - 6f : tx + tw - 6f;
            for (int n = 0; n < 5; n++) box(batch, nx, ty + th * (0.15f + n * 0.175f) - 5f, 12f, 10f, NIGHT, alpha);
        }
        // bandeau vert du haut, avec ses pièces
        float band = unitSize() * 0.26f;
        box(batch, tx, ty + th - band - unitSize() * 0.06f, tw, band, GREEN, alpha);
        box(batch, tx, ty + th - band - unitSize() * 0.06f, tw, 2f, GOLD, alpha);
        box(batch, tx, ty + th - unitSize() * 0.06f - 2f, tw, 2f, GOLD, alpha);
        batch.setColor(1f, 1f, 1f, alpha);
        for (int k = 0; k < 5; k++) {
            float bob = MathUtils.sin(time * 3f + k) * 2f;
            sprite(batch, coin, tx + tw * (0.1f + k * 0.2f), ty + th - band / 2f - unitSize() * 0.06f + bob,
                band * 0.8f / coin.getRegionHeight(), 0f);
        }
        List<Double> values = game.getValues();
        for (int i = 0; i < values.size(); i++) {
            float[] c = scratchCenter(i);
            float x = c[0] - cell / 2f, y = c[1] - cell / 2f;
            boolean win = game.getPicked().contains(i) && time >= reveal;
            float beat = win ? 1f + 0.06f * Math.abs(MathUtils.sin(time * 5f)) : 1f;
            float bc = cell * beat, bx = c[0] - bc / 2f, by = c[1] - bc / 2f;
            if (win) glow(batch, c[0], c[1], cell * 2.4f, GOLD_LIGHT, (0.3f + 0.12f * MathUtils.sin(time * 6f)) * alpha);
            box(batch, bx - 3f, by - 3f, bc + 6f, bc + 6f, win ? CREAM : GOLD_DARK, alpha);
            box(batch, bx, by, bc, bc, win ? GOLD : INK, alpha);
            box(batch, bx, by + bc * 0.9f, bc, bc * 0.1f, win ? GOLD_LIGHT : BEVEL, 0.12f * alpha);
            label(batch, times(values.get(i)), c[0], c[1], win ? 1.45f : 1.3f, alpha);
            if (win) twinkle(batch, bx + bc * 0.85f, by + bc * 0.85f, bc * 0.45f * Math.abs(MathUtils.sin(time * 4f + i)), alpha);
            // la couche argentée part de haut en bas quand la case se gratte, avec un bord déchiqueté
            float k = scratchProgress(i);
            if (k < 1f) {
                float left = cell * (1f - k);
                box(batch, x, y, cell, left, SILVER, alpha);
                int teeth = 8;
                for (int t = 0; t < teeth; t++) {
                    float tooth = left > 4f ? ((i * 7 + t * 13) % 5) * cell * 0.012f : 0f;
                    box(batch, x + t * cell / teeth, y + left, cell / teeth, tooth, SILVER, alpha);
                }
                for (int s = 0; s < 4; s++) {
                    box(batch, x + cell * 0.1f + s * cell * 0.22f, y + left * 0.3f, cell * 0.06f, left * 0.4f,
                        SILVER_DARK, alpha);
                }
                // un reflet glisse sur l'argent encore intact
                float shine = ((time * 0.8f + i * 0.13f) % 1.6f) / 1.6f;
                if (left > cell * 0.2f) box(batch, x + cell * shine * 0.9f, y, cell * 0.08f, left, CREAM, 0.35f * alpha);
                // la pièce qui gratte, en zigzag sur le bord
                if (k > 0f) {
                    batch.setColor(1f, 1f, 1f, alpha);
                    float sx = x + cell * (0.5f + 0.42f * MathUtils.sin(time * 34f));
                    sprite(batch, coin, sx, y + left + cell * 0.08f, cell * 0.34f / coin.getRegionHeight(),
                        MathUtils.sin(time * 34f) * 25f - 30f);
                }
            }
        }
        // un grand reflet balaie tout le ticket de temps en temps
        float sweep = ((time - START) % 2.8f) / 1.1f;
        if (sweep < 1f && time > START && !settings.isReducedEffects()) {
            additive(batch);
            float bandW = tw * 0.12f, sx = tx + (tw - bandW) * sweep;
            batch.setColor(CREAM.r, CREAM.g, CREAM.b, 0.16f * (1f - Math.abs(sweep * 2f - 1f)) * alpha);
            batch.draw(pixel, sx, ty, bandW, th);
            batch.draw(pixel, sx + bandW * 1.3f, ty, bandW * 0.3f, th);
            normal(batch);
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
            glow(batch, worldWidth() / 2f, y, 420f, GOLD, (0.3f + 0.1f * MathUtils.sin(time * 6f)) * pop);
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
