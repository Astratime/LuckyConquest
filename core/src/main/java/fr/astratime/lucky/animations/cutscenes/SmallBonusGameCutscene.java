package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.assets.GameSounds;
import fr.astratime.lucky.entities.BonusGame;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntConsumer;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les petits Jeux bonus (Roue, Plinko, Course de chevaux), par-dessus le combat,
 * dans le décor du Jeu bonus : le casino s'assombrit, le titre éclate puis monte,
 * le jeu se joue au centre, puis le total. Fondu noir, retour au combat.
 *
 * Tout est déjà tiré ({@link BonusGame#playSmall}) : la scène ne fait que le montrer.
 * Seule la Course attend un choix : le joueur clique sur un couloir (ou tape 1 à 5),
 * puis la course part. Ensuite, un clic (ou une touche) passe au total, puis referme la scène.
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
    /** Course : les stalles s'ouvrent après le choix, puis la course dure {@link #RACE}. */
    private static final float GATES = 0.9f, RACE = 5.5f;
    private static final float TOTAL_HOLD = 3.0f;
    private static final float COVER = 0.6f;
    /** Tant que le joueur n'a pas choisi son cheval. */
    private static final float WAITING = Float.MAX_VALUE / 4f;

    private static final Color GOLD = hex("ffc93a"), GOLD_LIGHT = hex("ffe58a"), GOLD_DARK = hex("b8801f");
    private static final Color RED = hex("c8203a"), RED_DARK = hex("6e0f1e"), INK = hex("1c0f24"), NIGHT = hex("07040c");
    private static final Color CREAM = hex("fff6dc");
    private static final Color SILVER = hex("b9c0c8"), SILVER_DARK = hex("7d858e");
    private static final Color TURF = hex("2f7a3e"), TURF_DARK = hex("25632f"), DIRT = hex("a87a4a");
    private static final Color BULB_OFF = hex("3a2a1a"), BEVEL = hex("ffffff");
    private static final Color[] CONFETTI = {GOLD, RED, hex("3ac1ff"), hex("5fd36a"), hex("ff7ad9"), CREAM};
    /** Casaque de chaque jockey, du favori à l'outsider. */
    private static final Color[] SILKS = {hex("e8333f"), hex("2f7fe8"), hex("f2c522"), hex("9b4fe0"), hex("f07a1d")};
    /** Couleur des cases du Plinko, du bord (le gros lot) au centre. */
    private static final Color[] SLOT_COLORS = {hex("d4202f"), hex("e8701d"), hex("e8b81d"), hex("3f9e4a"), hex("2f6fc8")};

    private final BonusGame game;
    private final BitmapFont font;
    private final GameSounds sounds;
    private final Sound intro, music, total;
    private final Sound[] owned;
    private final List<long[]> playing = new ArrayList<>();
    private final Array<Particle> sparks = new Array<>(false, 128);
    /** Poussière d'or du fond, confettis, poussière de la piste et pièces qui volent. */
    private final Array<Particle> dust = new Array<>(false, 128), confetti = new Array<>(false, 256);
    private final Array<Particle> flakes = new Array<>(false, 128), coins = new Array<>(false, 64);
    private final TextureRegion coin, ball;
    /** Les chevaux, deux images de galop chacun (casaque de son couloir). */
    private final TextureRegion[][] horses = new TextureRegion[BonusGame.HORSE_ODDS.size()][2];
    /** Course : ce que fait le choix du joueur (l'écran de combat le fait payer) ; {@code null} : la course seule. */
    private final IntConsumer chooser;
    /** Plinko : le chemin de la bille (vers la droite ?), une rangée de clous après l'autre. */
    private final boolean[] path = new boolean[BonusGame.PLINKO_ROWS];
    /** Course : le temps d'arrivée de chaque cheval (le gagnant à {@link #RACE}), et son allure. */
    private final float[] finish = new float[BonusGame.HORSE_ODDS.size()], phase = new float[BonusGame.HORSE_ODDS.size()];
    private final Vector2 mouse = new Vector2();
    /** Course : le choix fait, les stalles s'ouvrent ({@link #WAITING} avant). */
    private float chosenAt, raceStart;
    /** Le résultat se montre ; puis le total. */
    private float reveal, totalAt;
    private float closeAt, lastTime, dustTimer;
    private long spinLoop = -1;

    public SmallBonusGameCutscene(CutsceneKit kit, BonusGame game) {
        this(kit, game, null);
    }

    /**
     * @param chooser Course de chevaux : appelé avec le cheval choisi (0 = le favori) ; il doit faire
     *                payer la course ({@code GameController.chooseHorse}). Sans lui, la scène choisit seule.
     */
    public SmallBonusGameCutscene(CutsceneKit kit, BonusGame game, IntConsumer chooser) {
        super(kit.settings(), kit.shake(), null);
        this.game = game;
        this.chooser = chooser;
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
        for (int i = 0; i < horses.length; i++) {
            Color[] colors = {hex("8a5a2e"), hex("3a2414"), SILKS[i], hex("f2d2b0"), hex("1a1010")};
            horses[i][0] = art("bmjsh", colors,
                "...........jj.......",
                "..........jjjj......",
                "...........js...mmb.",
                "..m.....bbbbjbbmbbbb",
                ".mmbbbbbbbbbbbbbbbbh",
                "m..bbbbbbbbbbbbbb...",
                "...bbbbbbbbbbbbb....",
                "....bb.......bb.....",
                "...b..b.....b..b....",
                "..b....b...b....b...",
                ".h......h.h......h..");
            horses[i][1] = art("bmjsh", colors,
                "...........jj.......",
                "..........jjjj......",
                "...........js...mmb.",
                "..m.....bbbbjbbmbbbb",
                ".mmbbbbbbbbbbbbbbbbh",
                ".m.bbbbbbbbbbbbbb...",
                "...bbbbbbbbbbbbb....",
                "....bb......bb......",
                ".....bb....bb.......",
                "......b....b........",
                "......h....h........");
        }
    }

    /** @return le titre du jeu. */
    public static String title(BonusGame.Kind kind) {
        return switch (kind) {
            case WHEEL -> Lang.t("ROUE DE LA CAGNOTTE !");
            case PLINKO -> Lang.t("PLINKO DE LA CAGNOTTE !");
            case HORSES -> Lang.t("COURSE DE CHEVAUX !");
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
                // le gagnant arrive à RACE, les autres juste après, dans le désordre
                List<Integer> others = new ArrayList<>();
                for (int i = 0; i < finish.length; i++) if (i != game.getPicked().get(0)) others.add(i);
                Collections.shuffle(others, random);
                finish[game.getPicked().get(0)] = RACE;
                for (int k = 0; k < others.size(); k++) finish[others.get(k)] = RACE * (1.03f + 0.035f * k + random.nextFloat() * 0.02f);
                for (int i = 0; i < phase.length; i++) phase[i] = random.nextFloat() * MathUtils.PI2;
                chosenAt = raceStart = WAITING;
                if (game.isPending()) schedule(WAITING);
                else startRace(START);
            }
        }
        sound(intro, false);
    }

    /** Le résultat se montre à {@code at}, le total une seconde après. */
    private void schedule(float at) {
        reveal = at;
        totalAt = at >= WAITING ? WAITING : at + (game.getKind() == BonusGame.Kind.HORSES ? 1.6f : 1.0f);
        closeAt = totalAt >= WAITING ? WAITING : totalAt + TOTAL_HOLD;
    }

    private void startRace(float at) {
        chosenAt = at;
        raceStart = at + GATES;
        schedule(raceStart + RACE);
    }

    /** @return {@code true} tant que la course attend le choix du joueur. */
    private boolean waiting() { return game.getKind() == BonusGame.Kind.HORSES && chosenAt >= WAITING; }

    /** Le joueur choisit son cheval : la course part. */
    private void choose(int horse) {
        if (!waiting() || time < START - 0.2f) return;
        if (chooser != null) chooser.accept(horse);
        else game.choose(horse);
        if (game.isPending()) game.choose(horse); // le choix doit être fait
        if (sounds != null) sounds.reelStop.play(0.7f);
        float[] lane = laneCenter(horse);
        burst(trackLeft(), lane[1], 14, GOLD_LIGHT);
        startRace(Math.max(time, START));
    }

    @Override
    public boolean key(int keycode) {
        if (!waiting()) return false;
        int horse = -1;
        if (keycode >= Input.Keys.NUM_1 && keycode <= Input.Keys.NUM_5) horse = keycode - Input.Keys.NUM_1;
        if (keycode >= Input.Keys.NUMPAD_1 && keycode <= Input.Keys.NUMPAD_5) horse = keycode - Input.Keys.NUMPAD_1;
        if (horse >= 0) choose(horse);
        return true; // en attendant le choix, les autres touches ne passent pas la course
    }

    @Override
    protected boolean pointerDown(float x, float y) {
        if (waiting()) {
            int lane = laneAt(x, y);
            if (lane >= 0) choose(lane);
            return true;
        }
        skip();
        return true;
    }

    @Override
    public void skip() {
        if (!isPlaying() || time >= closeAt || waiting()) return;
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
                if (crossed(from, raceStart)) {
                    if (sounds != null) {
                        sounds.reelStop.play(0.9f, 0.7f, 0f);
                        spinLoop = sounds.reelSpin.loop(0.4f, 0.6f, 0f);   // le roulement des sabots
                    }
                    rumble(0.25f, 3f);
                }
                if (rich && time > raceStart && time < reveal + 0.6f) {
                    // les sabots soulèvent la poussière de la piste
                    for (int i = 0; i < finish.length; i++) {
                        if (random.nextFloat() > 0.35f) continue;
                        float[] h = horseAt(i);
                        Particle grain = particle(h[0] - unitSize() * 0.3f, h[1] - unitSize() * 0.2f, 120f + random.nextFloat() * 50f,
                            30f + random.nextFloat() * 60f, 0.4f + random.nextFloat() * 0.3f, 3f + random.nextFloat() * 4f);
                        grain.gravity = 160f;
                        grain.spin = (random.nextFloat() - 0.5f) * 600f;
                        grain.color.set(DIRT);
                        flakes.add(grain);
                    }
                }
                if (crossed(from, reveal)) {
                    stopSpinLoop();
                    boolean won = game.getChoice() == game.getPicked().get(0);
                    float[] lane = laneCenter(game.getPicked().get(0));
                    burst(finishX(), lane[1], 30, GOLD_LIGHT);
                    if (sounds != null) sounds.coinsGain.play(won ? 0.9f : 0.4f);
                    if (won) {
                        confettiBurst(finishX(), lane[1], 60);
                        fountain(finishX(), lane[1], 14);
                        rumble(0.4f, 6f);
                    }
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
                default -> drawRace(batch, appear);
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

    // --- Course de chevaux -----------------------------------------------------

    private float laneHeight() { return unitSize() * 0.74f; }
    private float trackTop()   { return centerY() + unitSize() * 1.95f; }
    private float trackLeft()  { return worldWidth() / 2f - unitSize() * 4.4f; }
    private float finishX()    { return worldWidth() / 2f + unitSize() * 4.5f; }

    private float[] laneCenter(int lane) {
        return new float[] {(trackLeft() + finishX()) / 2f, trackTop() - (lane + 0.5f) * laneHeight()};
    }

    /** @return le couloir sous ({@code x}, {@code y}), ou -1. */
    private int laneAt(float x, float y) {
        if (x < trackLeft() - unitSize() * 2f || x > finishX() + unitSize() * 0.8f) return -1;
        float from = trackTop() - y;
        if (from < 0f) return -1;
        int lane = (int) (from / laneHeight());
        return lane < finish.length ? lane : -1;
    }

    /** @return où en est le cheval {@code i} : 0 dans sa stalle, 1 sur la ligne d'arrivée. */
    private float horseProgress(int i) {
        if (time <= raceStart) return 0f;
        float u = (time - raceStart) / finish[i];
        if (u <= 1f) return u + 0.045f * MathUtils.sin(MathUtils.PI2 * u * 1.3f + phase[i]) * MathUtils.sin(MathUtils.PI * u);
        return 1f + 0.07f * (1f - (float) Math.exp(-(u - 1f) * 6f));
    }

    /** @return la largeur d'un cheval à l'écran. */
    private float horseWidth() { return horses[0][0].getRegionWidth() * horseScale(); }
    private float horseScale() { return laneHeight() * 0.68f / horses[0][0].getRegionHeight(); }

    /** @return le bout du nez du cheval {@code i} : juste après sa stalle au départ, sur la ligne à l'arrivée. */
    private float[] horseAt(int i) {
        float start = trackLeft() + unitSize() * 0.2f + horseWidth();
        return new float[] {start + horseProgress(i) * (finishX() - start), laneCenter(i)[1]};
    }

    private void drawRace(Batch batch, float alpha) {
        List<Double> odds = game.getValues();
        int count = odds.size();
        float lh = laneHeight(), top = trackTop(), bottom = top - lh * count;
        float left = trackLeft() - unitSize() * 1.95f, right = finishX() + unitSize() * 0.75f;
        int chosen = game.getChoice(), winner = game.getPicked().get(0);
        boolean done = time >= reveal;
        // la piste : bord de terre, cadre doré
        box(batch, left - 8f, bottom - 8f, right - left + 16f, top - bottom + 16f, GOLD_DARK, alpha);
        box(batch, left - 3f, bottom - 3f, right - left + 6f, top - bottom + 6f, GOLD, alpha);
        box(batch, left, bottom, right - left, top - bottom, DIRT, alpha);
        int hovered = waiting() && getStage() != null
            ? laneAtMouse() : -1;
        for (int i = 0; i < count; i++) {
            float y = top - (i + 1) * lh;
            box(batch, trackLeft(), y + 2f, right - trackLeft(), lh - 4f, i % 2 == 0 ? TURF : TURF_DARK, alpha);
            // l'herbe défile un peu sous les chevaux pendant la course
            float scroll = time > raceStart ? (time - raceStart) * unitSize() * 0.4f : 0f;
            for (float gx = trackLeft() + ((unitSize() * 0.5f - scroll % (unitSize() * 0.5f))); gx < right - 4f; gx += unitSize() * 0.5f) {
                box(batch, gx, y + lh * 0.25f, 3f, 3f, TURF_DARK, 0.6f * alpha);
                box(batch, gx + unitSize() * 0.22f, y + lh * 0.65f, 3f, 3f, TURF, 0.6f * alpha);
            }
            // lices blanches entre les couloirs
            for (float dx = trackLeft(); dx < right; dx += 14f) box(batch, dx, y, 8f, 2f, CREAM, 0.7f * alpha);
            boolean highlight = i == hovered || i == chosen;
            if (waiting()) {
                float pulse = 0.08f + 0.06f * MathUtils.sin(time * 4f + i);
                box(batch, left, y + 2f, right - left, lh - 4f, GOLD_LIGHT, (i == hovered ? 0.25f : pulse) * alpha);
            } else if (i == chosen) {
                box(batch, left, y + 2f, right - left, lh - 4f, GOLD_LIGHT, (0.12f + 0.05f * MathUtils.sin(time * 5f)) * alpha);
            }
            if (done && i == winner) box(batch, left, y + 2f, right - left, lh - 4f, CREAM, (0.18f + 0.08f * MathUtils.sin(time * 6f)) * alpha);
            // la plaque du couloir : numéro, cote et chances (plus de losanges = gagne plus souvent)
            float py = y + lh / 2f, plateX = left + unitSize() * 0.12f, plateW = unitSize() * 1.7f;
            box(batch, plateX, y + 5f, plateW, lh - 10f, highlight ? GOLD : INK, alpha);
            box(batch, plateX + 3f, y + 8f, lh - 16f, lh - 16f, SILKS[i], alpha);
            label(batch, Integer.toString(i + 1), plateX + 3f + (lh - 16f) / 2f, py, 1.0f, alpha);
            float textX = plateX + lh - 16f + 6f + (plateW - lh + 10f) / 2f;
            label(batch, times(odds.get(i)), textX, py + lh * 0.14f, 0.95f, alpha);
            int pips = count - i;
            for (int p = 0; p < count; p++) {
                diamond(batch, textX + (p - (count - 1) / 2f) * unitSize() * 0.16f, py - lh * 0.24f, unitSize() * 0.1f,
                    p < pips ? (highlight ? INK : GOLD_LIGHT) : (highlight ? GOLD_DARK : BULB_OFF), alpha);
            }
        }
        // la ligne d'arrivée, en damier
        float cell = lh / 4f;
        for (int k = 0; k < count * 4; k++) {
            for (int c = 0; c < 2; c++) {
                box(batch, finishX() - cell + c * cell, bottom + k * cell, cell, cell, (k + c) % 2 == 0 ? CREAM : INK, alpha);
            }
        }
        if (done) glow(batch, finishX(), laneCenter(winner)[1], lh * 3f, GOLD_LIGHT, 0.4f * alpha * Math.max(0f, 1f - (time - reveal) * 0.5f));
        // les chevaux
        for (int i = 0; i < count; i++) {
            float[] h = horseAt(i);
            boolean running = time > raceStart && horseProgress(i) < 1.06f;
            int frame = running ? (int) (time * 11f + i) % 2 : 0;
            float bob = running ? Math.abs(MathUtils.sin(time * 17f + i)) * 3f : MathUtils.sin(time * 2f + i) * 1.5f;
            float scale = horseScale(), sw = horseWidth();
            batch.setColor(0f, 0f, 0f, 0.3f * alpha);
            batch.draw(pixel, h[0] - sw * 0.9f, h[1] - lh * 0.36f, sw * 0.8f, 4f);       // ombre
            batch.setColor(1f, 1f, 1f, alpha);
            sprite(batch, horses[i][frame], h[0] - sw * 0.5f, h[1] + bob, scale, 0f);
            if (i == chosen) {
                // une flèche dorée au-dessus du cheval du joueur
                float my = h[1] + lh * 0.38f + Math.abs(MathUtils.sin(time * 6f)) * 4f;
                glow(batch, h[0] - sw * 0.42f, my, unitSize() * 0.6f, GOLD_LIGHT, 0.4f * alpha);
                diamond(batch, h[0] - sw * 0.42f, my, unitSize() * 0.2f, GOLD, alpha);
                diamond(batch, h[0] - sw * 0.42f, my, unitSize() * 0.09f, CREAM, alpha);
            }
        }
        // les stalles : elles se lèvent quand la course part
        float lift = Interpolation.pow2In.apply(progress(raceStart - 0.25f, raceStart + 0.15f));
        if (lift < 1f) {
            for (int i = 0; i < count; i++) {
                float y = top - (i + 1) * lh, gx = trackLeft() + unitSize() * 0.05f;
                float gy = y + 3f + lift * lh;
                box(batch, gx, gy, unitSize() * 0.16f, lh - 6f, SILVER_DARK, (1f - lift) * alpha);
                for (int b = 0; b < 3; b++) {
                    box(batch, gx + 2f, gy + (b + 0.5f) * (lh - 6f) / 3f - 1f, unitSize() * 0.16f - 4f, 3f, SILVER, (1f - lift) * alpha);
                }
            }
        }
        if (waiting() && time >= START) {
            float pulse = 1f + 0.06f * MathUtils.sin(time * 5f);
            glow(batch, worldWidth() / 2f, top + unitSize() * 0.42f, 360f, GOLD, 0.25f * alpha);
            label(batch, Lang.t("CHOISIS TON CHEVAL !"), worldWidth() / 2f, top + unitSize() * 0.42f, 1.4f * pulse, alpha);
        }
    }

    /** @return le couloir sous la souris, ou -1. */
    private int laneAtMouse() {
        try {
            getStage().screenToStageCoordinates(mouse.set(Gdx.input.getX(), Gdx.input.getY()));
            return laneAt(mouse.x, mouse.y);
        } catch (RuntimeException noInput) {
            return -1;
        }
    }

    // --- Textes ----------------------------------------------------------------

    private void drawCaptions(Batch batch, float alpha) {
        float y = totalY();
        if (game.getKind() == BonusGame.Kind.HORSES && time >= reveal && time < totalAt) {
            // le résultat de la course, avant le total
            boolean won = game.getChoice() == game.getPicked().get(0);
            float pop = Interpolation.swingOut.apply(progress(reveal, reveal + 0.35f));
            String line = won ? Lang.f("TON CHEVAL GAGNE ! {0}", times(game.getValues().get(game.getChoice())))
                : Lang.f("CHEVAL {0} GAGNANT : LOT DE CONSOLATION {1}", game.getPicked().get(0) + 1, times(BonusGame.HORSE_CONSOLATION));
            if (won) glow(batch, worldWidth() / 2f, y, 420f, GOLD, 0.35f * pop);
            label(batch, line, worldWidth() / 2f, y, Math.max(0.01f, (won ? 1.8f : 1.3f) * pop), 1f);
        } else if (time < totalAt) {
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
