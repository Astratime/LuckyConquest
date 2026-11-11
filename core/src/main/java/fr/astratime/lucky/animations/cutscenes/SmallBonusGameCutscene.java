package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
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
import java.util.Collections;
import java.util.List;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les petits Jeux bonus (Roue, Plinko, Loterie), par-dessus le combat,
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
    /** Plinko : la bille passe une rangée de clous tous les {@link #PLINKO_STEP}. */
    private static final float PLINKO_STEP = 0.34f;
    /** Loterie : une boule sort du boulier tous les {@link #LOTTERY_STEP}. */
    private static final float LOTTERY_STEP = 1.1f;
    private static final int LOTTERY_SLOTS = BonusGame.LOTTERY_BALLS;
    private static final float TOTAL_HOLD = 3.0f;
    private static final float COVER = 0.6f;

    private static final Color GOLD = hex("ffc93a"), GOLD_LIGHT = hex("ffe58a"), GOLD_DARK = hex("b8801f");
    private static final Color RED = hex("c8203a"), RED_DARK = hex("6e0f1e"), INK = hex("1c0f24"), NIGHT = hex("07040c");
    private static final Color CREAM = hex("fff6dc");
    private static final Color SILVER = hex("b9c0c8"), SILVER_DARK = hex("7d858e");
    private static final Color BULB_OFF = hex("3a2a1a"), BEVEL = hex("ffffff");
    private static final Color[] CONFETTI = {GOLD, RED, hex("3ac1ff"), hex("5fd36a"), hex("ff7ad9"), CREAM};
    /** Couleur des boules de la Loterie, de la plus petite valeur à la plus grande (dorée). */
    private static final Color[] BALL_COLORS = {hex("2f6fc8"), hex("3f9e4a"), hex("e8701d"), hex("9b4fe0"), hex("d4202f"), hex("ffc93a")};
    /** Couleur des cases du Plinko, du bord (le gros lot) au centre. */
    private static final Color[] SLOT_COLORS = {hex("d4202f"), hex("e8701d"), hex("e8b81d"), hex("3f9e4a"), hex("2f6fc8")};

    private final BonusGame game;
    private final BitmapFont font;
    private final GameSounds sounds;
    private final Sound intro, music, total;
    private final Sound[] owned;
    private final List<long[]> playing = new ArrayList<>();
    private final Array<Particle> sparks = new Array<>(false, 128);
    /** Poussière d'or du fond, confettis, étincelles et pièces qui volent. */
    private final Array<Particle> dust = new Array<>(false, 128), confetti = new Array<>(false, 256);
    private final Array<Particle> flakes = new Array<>(false, 128), coins = new Array<>(false, 64);
    private final TextureRegion coin, ball, globe;
    /** Plinko : le chemin de la bille (vers la droite ?), une rangée de clous après l'autre. */
    private final boolean[] path = new boolean[BonusGame.PLINKO_ROWS];
    /** Loterie : les boules qui tourbillonnent dans le boulier (angle, vitesse, fréquence, phase). */
    private final float[][] mixer = new float[16][4];
    /** Le résultat se montre ; puis le total. */
    private float reveal, totalAt;
    private float closeAt, lastTime, dustTimer;
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
        Pixmap disc = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        disc.setColor(Color.WHITE);
        disc.fillCircle(8, 8, 7);
        ball = region(disc, false);
        Pixmap big = new Pixmap(96, 96, Pixmap.Format.RGBA8888);
        big.setColor(Color.WHITE);
        big.fillCircle(48, 48, 47);
        globe = region(big, true);
    }

    /** @return le titre du jeu. */
    public static String title(BonusGame.Kind kind) {
        return switch (kind) {
            case WHEEL -> Lang.t("ROUE DE LA CAGNOTTE !");
            case PLINKO -> Lang.t("PLINKO DE LA CAGNOTTE !");
            case LOTTERY -> Lang.t("LOTERIE DE LA CAGNOTTE !");
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
        lastTime = 0f;
        switch (game.getKind()) {
            case WHEEL -> schedule(START + WHEEL_SPIN);
            case PLINKO -> {
                // un chemin au hasard qui mène à la case tirée
                List<Boolean> rights = new ArrayList<>();
                for (int row = 0; row < path.length; row++) rights.add(row < game.getPicked().get(0));
                Collections.shuffle(rights, random);
                for (int row = 0; row < path.length; row++) path[row] = rights.get(row);
                schedule(START + 0.5f + PLINKO_STEP * (path.length + 1));
            }
            default -> {
                for (float[] m : mixer) {
                    m[0] = random.nextFloat() * MathUtils.PI2;
                    m[1] = (random.nextBoolean() ? 1f : -1f) * (0.6f + random.nextFloat());
                    m[2] = 1.5f + random.nextFloat() * 2.5f;
                    m[3] = random.nextFloat() * MathUtils.PI2;
                }
                schedule(ballLand(LOTTERY_SLOTS - 1) + 0.3f);
            }
        }
        sound(intro, false);
    }

    /** Le résultat se montre à {@code at}, le total une seconde après. */
    private void schedule(float at) {
        reveal = at;
        totalAt = at + (game.getKind() == BonusGame.Kind.LOTTERY ? 1.4f : 1.0f);
        closeAt = totalAt + TOTAL_HOLD;
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
            case PLINKO -> {
                for (int row = 0; row <= path.length; row++) {
                    if (crossed(from, pegHit(row))) {
                        float[] at = ballAt(row);
                        if (row < path.length) {
                            if (sounds != null) sounds.reelStop.play(0.18f, 1.3f + row * 0.06f, 0f);
                            burst(at[0], at[1], 3, CREAM);
                        }
                    }
                }
                if (crossed(from, reveal)) {
                    int slot = game.getPicked().get(0);
                    float[] c = slotCenter(slot);
                    if (sounds != null) sounds.coinsGain.play(0.8f);
                    burst(c[0], c[1], 24, GOLD_LIGHT);
                    double value = game.getValues().get(slot);
                    if (value >= 7) confettiBurst(c[0], c[1], value >= 50 ? 60 : 30);
                    if (value >= 14) fountain(c[0], c[1], value >= 50 ? 20 : 8);
                    rumble(value >= 50 ? 0.5f : 0.2f, value >= 50 ? 8f : 3f);
                }
            }
            default -> {
                for (int i = 0; i < LOTTERY_SLOTS; i++) {
                    if (crossed(from, ballOut(i)) && sounds != null) sounds.reelStop.play(0.4f, 1.2f, 0f);
                    if (crossed(from, ballLand(i))) {
                        double value = game.getValues().get(i);
                        float[] c = trayCenter(i);
                        if (sounds != null) sounds.coinsGain.play(0.6f, 1f + i * 0.12f, 0f);
                        burst(c[0], c[1], value >= 5 ? 24 : 12, ballColor(value));
                        if (value >= 5) confettiBurst(c[0], c[1], value >= 10 ? 50 : 20);
                        if (value >= 10) {
                            fountain(c[0], c[1], 14);
                            rumble(0.4f, 6f);
                        }
                    }
                }
                if (crossed(from, reveal)) {
                    float[] mid = trayCenter(1);
                    burst(mid[0], mid[1] - unitSize() * 1.2f, 20, GOLD_LIGHT);
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
                case PLINKO -> drawPlinko(batch, appear);
                default -> drawLottery(batch, appear);
            }
            drawCaptions(batch, appear);
            BonusRankBadge.draw(this, batch, font, game.getKind().rank, worldWidth() - 120f, worldHeight() - 92f, START, appear);
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
        batch.setColor(color.r, color.g, color.b, alpha);
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

    // --- Plinko ---------------------------------------------------------------

    private float pegSpacing() { return unitSize() * 0.72f; }
    private float rowHeight()  { return unitSize() * 0.46f; }
    private float boardTop()   { return centerY() + unitSize() * 2.05f; }
    private float pegY(int row) { return boardTop() - row * rowHeight(); }
    private float slotTop()    { return pegY(BonusGame.PLINKO_ROWS) + rowHeight() * 0.55f; }
    private float slotHeight() { return unitSize() * 0.75f; }

    /** @return l'instant où la bille touche le clou de la rangée {@code row} ({@code PLINKO_ROWS} : elle tombe dans sa case). */
    private float pegHit(int row) { return START + 0.5f + PLINKO_STEP * (row + 1); }

    /** @return combien de fois la bille est partie à droite avant la rangée {@code row}. */
    private int rights(int row) {
        int count = 0;
        for (int r = 0; r < row && r < path.length; r++) if (path[r]) count++;
        return count;
    }

    private float[] slotCenter(int slot) {
        return new float[] {worldWidth() / 2f + (slot - BonusGame.PLINKO_ROWS / 2f) * pegSpacing(), slotTop() - slotHeight() / 2f};
    }

    /** @return où la bille touche la rangée {@code row} (sur le clou), ou se pose dans sa case. */
    private float[] ballAt(int row) {
        float r = ballRadius();
        if (row >= path.length) {
            float[] slot = slotCenter(rights(path.length));
            return new float[] {slot[0], slotTop() - r - 3f};
        }
        float x = worldWidth() / 2f + (rights(row) - row / 2f) * pegSpacing();
        return new float[] {x, pegY(row) + pegRadius() + r};
    }

    private float ballRadius() { return unitSize() * 0.13f; }
    private float pegRadius()  { return unitSize() * 0.07f; }

    /** @return la bille à l'instant {@code t}, ou {@code null} avant qu'elle ne tombe. */
    private float[] ballPosition(float t) {
        float drop = START + 0.5f;
        if (t < drop) return null;
        if (t < pegHit(0)) {
            float[] first = ballAt(0);
            float f = Interpolation.pow2In.apply((t - drop) / PLINKO_STEP);
            return new float[] {first[0], MathUtils.lerp(boardTop() + unitSize() * 0.75f, first[1], f)};
        }
        if (t >= reveal) return ballAt(path.length);
        int row = Math.min(path.length - 1, (int) ((t - pegHit(0)) / PLINKO_STEP));
        float f = (t - pegHit(row)) / PLINKO_STEP;
        float[] a = ballAt(row), b = ballAt(row + 1);
        float hop = row + 1 < path.length ? unitSize() * 0.22f : unitSize() * 0.1f;
        return new float[] {MathUtils.lerp(a[0], b[0], f), MathUtils.lerp(a[1], b[1], f * f) + hop * 4f * f * (1f - f)};
    }

    private void drawPlinko(Batch batch, float alpha) {
        List<Double> values = game.getValues();
        int rows = BonusGame.PLINKO_ROWS, slots = values.size();
        float sp = pegSpacing(), cx = worldWidth() / 2f;
        float left = cx - sp * (slots / 2f + 0.35f), right = cx + sp * (slots / 2f + 0.35f);
        float top = boardTop() + unitSize() * 0.45f, bottom = slotTop() - slotHeight() - unitSize() * 0.12f;
        // le panneau : cadre doré, fond violet nuit
        glow(batch, cx, (top + bottom) / 2f, (right - left) * 1.8f, GOLD, 0.15f * alpha);
        box(batch, left - 8f, bottom - 8f, right - left + 16f, top - bottom + 16f, GOLD_DARK, alpha);
        box(batch, left - 3f, bottom - 3f, right - left + 6f, top - bottom + 6f, GOLD, alpha);
        box(batch, left, bottom, right - left, top - bottom, INK, alpha);
        // ampoules en haut du panneau
        int bulbs = 14;
        for (int i = 0; i < bulbs; i++) {
            float bx = left + (i + 0.5f) * (right - left) / bulbs, by = top - unitSize() * 0.14f;
            boolean lit = (i + (int) (time * 9f)) % 3 != 0;
            float size = unitSize() * 0.09f;
            if (lit) glow(batch, bx, by, size * 4f, GOLD_LIGHT, 0.35f * alpha);
            box(batch, bx - size / 2f, by - size / 2f, size, size, lit ? CREAM : GOLD_DARK, alpha);
        }
        // les clous, en triangle ; celui que la bille vient de toucher s'allume
        float peg = pegRadius() * 2f;
        for (int row = 0; row < rows; row++) {
            int hit = rights(row) + 1;
            float lit = time >= pegHit(row) ? Math.max(0f, 1f - (time - pegHit(row)) / 0.35f) : 0f;
            for (int i = 0; i <= row + 2; i++) {
                float px = cx + (i - (row + 2) / 2f) * sp, py = pegY(row);
                boolean flash = i == hit && lit > 0f;
                if (flash) glow(batch, px, py, peg * 6f, GOLD_LIGHT, 0.6f * lit * alpha);
                Color color = flash ? CREAM : SILVER;
                batch.setColor(color.r, color.g, color.b, alpha);
                batch.draw(ball, px - peg / 2f, py - peg / 2f, peg, peg);
                batch.setColor(1f, 1f, 1f, 0.5f * alpha);
                batch.draw(ball, px - peg * 0.3f, py, peg * 0.25f, peg * 0.25f);   // petit reflet
            }
        }
        // les cases du bas : le gros lot sur les bords
        boolean landed = time >= reveal;
        int slotWon = game.getPicked().get(0);
        float beat = landed ? 1f + 0.08f * Math.abs(MathUtils.sin(time * 5f)) : 1f;
        for (int k = 0; k < slots; k++) {
            float[] c = slotCenter(k);
            int distance = Math.abs(k - slots / 2);
            Color color = SLOT_COLORS[Math.min(SLOT_COLORS.length - 1, slots / 2 - distance)];
            boolean won = landed && k == slotWon;
            float w = (sp - 6f) * (won ? beat : 1f), h = slotHeight();
            if (won) glow(batch, c[0], c[1], w * 3f, GOLD_LIGHT, (0.45f + 0.15f * MathUtils.sin(time * 6f)) * alpha);
            float a = alpha, dim = landed && !won ? 0.4f : 1f;
            box(batch, c[0] - w / 2f - 2f, c[1] - h / 2f - 2f, w + 4f, h + 4f, won ? CREAM : GOLD_DARK, a);
            box(batch, c[0] - w / 2f, c[1] - h / 2f, w, h, won ? GOLD : tmp.set(color).mul(dim, dim, dim, 1f), a);
            box(batch, c[0] - w / 2f, c[1] + h / 2f - h * 0.12f, w, h * 0.12f, BEVEL, 0.25f * a * dim);
            label(batch, times(values.get(k)), c[0], c[1] - h * 0.1f, won ? 1.0f : 0.85f, landed && !won ? 0.5f * a : a);
            if (won) twinkle(batch, c[0] + w * 0.4f, c[1] + h * 0.4f, w * 0.5f * Math.abs(MathUtils.sin(time * 4f)), alpha);
        }
        // la bille, avec sa traînée
        float[] at = ballPosition(time);
        if (at != null) {
            float r = ballRadius();
            if (!settings.isReducedEffects() && !landed) {
                for (int k = 4; k >= 1; k--) {
                    float[] back = ballPosition(time - k * 0.035f);
                    if (back == null) continue;
                    glow(batch, back[0], back[1], r * (3.4f - k * 0.5f), GOLD_LIGHT, 0.12f * (5 - k) * alpha);
                }
            }
            glow(batch, at[0], at[1], r * 5f, GOLD_LIGHT, 0.5f * alpha);
            batch.setColor(INK.r, INK.g, INK.b, alpha);
            batch.draw(ball, at[0] - r - 2f, at[1] - r - 2f, r * 2f + 4f, r * 2f + 4f);
            batch.setColor(GOLD_LIGHT.r, GOLD_LIGHT.g, GOLD_LIGHT.b, alpha);
            batch.draw(ball, at[0] - r, at[1] - r, r * 2f, r * 2f);
            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(ball, at[0] - r * 0.55f, at[1] + r * 0.05f, r * 0.6f, r * 0.6f);
        }
    }

    // --- Loterie ---------------------------------------------------------------

    private float sphereX()      { return worldWidth() / 2f - unitSize() * 2.3f; }
    private float sphereY()      { return centerY() + unitSize() * 0.55f; }
    private float sphereRadius() { return unitSize() * 1.55f; }
    private float lotteryBall()  { return unitSize() * 0.8f; }

    /** @return l'instant où la boule {@code i} quitte le boulier, et celui où elle se pose dans sa case. */
    private float ballOut(int i)  { return START + 1.4f + i * LOTTERY_STEP; }
    private float ballLand(int i) { return ballOut(i) + 0.8f; }

    /** @return la case de la boule {@code i}, sur le plateau à droite du boulier. */
    private float[] trayCenter(int i) {
        return new float[] {worldWidth() / 2f + unitSize() * (0.7f + i * 1.45f), centerY() - unitSize() * 0.45f};
    }

    /** @return la couleur d'une boule selon sa valeur (la plus rare est dorée). */
    private static Color ballColor(double value) {
        int index = BonusGame.LOTTERY_VALUES.indexOf(value);
        return BALL_COLORS[Math.max(0, Math.min(BALL_COLORS.length - 1, index))];
    }

    /** Une boule de loto : contour sombre, couleur, reflet, et sa valeur si elle est sortie. */
    private void lottoBall(Batch batch, float x, float y, float size, Color color, String text, float alpha) {
        batch.setColor(INK.r, INK.g, INK.b, alpha);
        batch.draw(ball, x - size / 2f - 2f, y - size / 2f - 2f, size + 4f, size + 4f);
        batch.setColor(color.r, color.g, color.b, alpha);
        batch.draw(ball, x - size / 2f, y - size / 2f, size, size);
        if (text != null) {
            batch.setColor(CREAM.r, CREAM.g, CREAM.b, alpha);
            batch.draw(ball, x - size * 0.39f, y - size * 0.39f, size * 0.78f, size * 0.78f);
        }
        batch.setColor(1f, 1f, 1f, 0.7f * alpha);
        batch.draw(ball, x - size * 0.3f, y + size * 0.12f, size * 0.2f, size * 0.2f);
        if (text != null) {
            Color old = font.getColor().cpy();
            font.setColor(INK);
            label(batch, text, x, y - size * 0.03f, size / unitSize() * 1.2f, alpha);
            font.setColor(old);
        }
    }

    private void drawLottery(Batch batch, float alpha) {
        List<Double> values = game.getValues();
        float cx = sphereX(), cy = sphereY(), r = sphereRadius(), size = lotteryBall();
        // le pied doré et la manivelle
        box(batch, cx - r * 0.45f, cy - r - unitSize() * 0.55f, r * 0.9f, unitSize() * 0.3f, GOLD_DARK, alpha);
        box(batch, cx - r * 0.15f, cy - r - unitSize() * 0.3f, r * 0.3f, unitSize() * 0.32f, GOLD, alpha);
        float crank = time * (time < reveal ? 4f : 0.5f);
        float hx = cx + r + unitSize() * 0.25f, hy = cy;
        box(batch, cx + r - 2f, hy - 3f, unitSize() * 0.27f, 6f, GOLD_DARK, alpha);
        float kx = hx + MathUtils.cos(crank) * unitSize() * 0.3f, ky = hy + MathUtils.sin(crank) * unitSize() * 0.3f;
        batch.setColor(GOLD.r, GOLD.g, GOLD.b, alpha);
        batch.draw(pixel, hx, hy - 3f, 0f, 3f, unitSize() * 0.3f, 6f, 1f, 1f, crank * MathUtils.radiansToDegrees);
        diamond(batch, kx, ky, unitSize() * 0.16f, RED, alpha);
        // la goulotte, du bas du boulier jusqu'au plateau
        float[] last = trayCenter(LOTTERY_SLOTS - 1);
        float chuteY = trayCenter(0)[1] - size / 2f - 6f;
        box(batch, cx, chuteY - 6f, last[0] + size - cx, 6f, GOLD_DARK, alpha);
        box(batch, cx - 3f, chuteY, 6f, cy - r - chuteY + 4f, GOLD_DARK, alpha);
        // le plateau : trois cases
        for (int i = 0; i < LOTTERY_SLOTS; i++) {
            float[] c = trayCenter(i);
            float w = size * 1.25f;
            box(batch, c[0] - w / 2f - 3f, c[1] - w / 2f - 3f, w + 6f, w + 6f, GOLD, alpha);
            box(batch, c[0] - w / 2f, c[1] - w / 2f, w, w, INK, alpha);
            if (i < LOTTERY_SLOTS - 1) label(batch, "+", c[0] + unitSize() * 0.725f, c[1], 1.4f, 0.8f * alpha);
        }
        // la sphère de verre : fond, boules qui tourbillonnent, reflets
        glow(batch, cx, cy, r * 3f, GOLD, 0.15f * alpha);
        batch.setColor(INK.r, INK.g, INK.b, 0.85f * alpha);
        batch.draw(globe, cx - r, cy - r, r * 2f, r * 2f);
        int drawn = 0;
        for (int i = 0; i < LOTTERY_SLOTS; i++) if (time >= ballOut(i)) drawn++;
        float mixing = time < reveal ? 1f : 0.25f;
        for (int b = 0; b < mixer.length; b++) {
            if (b < drawn) continue;   // les boules tirées ont quitté le boulier
            float[] m = mixer[b];
            float a = m[0] + time * m[1] * 2.6f * mixing;
            float dist = r * 0.72f * (0.35f + 0.65f * Math.abs(MathUtils.sin(time * m[2] * mixing + m[3])));
            float bx = cx + MathUtils.cos(a) * dist, by = cy + MathUtils.sin(a) * dist * 0.9f;
            lottoBall(batch, bx, by, size * 0.34f, BALL_COLORS[b % BALL_COLORS.length], null, alpha);
        }
        batch.setColor(CREAM.r, CREAM.g, CREAM.b, 0.55f * alpha);
        batch.draw(ring, cx - r * 1.08f, cy - r * 1.08f, r * 2.16f, r * 2.16f);
        batch.setColor(GOLD.r, GOLD.g, GOLD.b, 0.8f * alpha);
        batch.draw(ring, cx - r * 1.14f, cy - r * 1.14f, r * 2.28f, r * 2.28f);
        additive(batch);
        batch.setColor(1f, 1f, 1f, 0.18f * alpha);
        batch.draw(globe, cx - r * 0.62f, cy + r * 0.3f, r * 0.22f, r * 0.22f);   // reflet du verre
        normal(batch);
        // les boules tirées : elles descendent la goulotte, roulent et se posent
        for (int i = 0; i < values.size(); i++) {
            if (time < ballOut(i)) continue;
            float[] c = trayCenter(i);
            float f = progress(ballOut(i), ballLand(i));
            float x, y;
            if (f < 0.35f) {                       // la chute sous le boulier
                float k = Interpolation.pow2In.apply(f / 0.35f);
                x = cx;
                y = MathUtils.lerp(cy - r * 0.6f, chuteY + size / 2f, k);
            } else {                               // elle roule jusqu'à sa case, avec un petit rebond
                float k = Interpolation.pow2Out.apply((f - 0.35f) / 0.65f);
                x = MathUtils.lerp(cx, c[0], k);
                y = chuteY + size / 2f + Math.abs(MathUtils.sin(k * MathUtils.PI * 2f)) * unitSize() * 0.15f * (1f - k);
                if (f >= 1f) y = c[1];
            }
            double value = values.get(i);
            boolean landed = f >= 1f;
            float pop = landed ? 1f + 0.25f * (1f - progress(ballLand(i), ballLand(i) + 0.25f)) : 1f;
            if (landed) glow(batch, x, y, size * 3f, ballColor(value), (0.35f + 0.1f * MathUtils.sin(time * 5f + i)) * alpha);
            if (landed && value >= 5) twinkle(batch, x + size * 0.45f, y + size * 0.45f, size * 0.6f * Math.abs(MathUtils.sin(time * 4f + i)), alpha);
            lottoBall(batch, x, y, size * pop, ballColor(value), times(value), alpha);
        }
        // la somme, une fois les trois boules posées
        if (time >= reveal) {
            double sum = 0;
            for (double value : values) sum += value;
            float pop = Interpolation.swingOut.apply(progress(reveal, reveal + 0.35f));
            float[] mid = trayCenter(1);
            label(batch, "= " + times(sum), mid[0], mid[1] - unitSize() * 1.2f, Math.max(0.01f, 1.6f * pop), alpha);
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
