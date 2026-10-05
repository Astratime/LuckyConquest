package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.events.LastDrawEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Le Dernier tirage de la Machine Originelle, achevée : au sommet de la Tour,
 * ta machine à un rouleau fait face à la sienne. Une main gantée tire ton
 * levier, puis le sien descend tout seul. Ton rouleau s'arrête, le sien se fait
 * attendre... Le plus haut rang gagne (le Joker en tête) ; à égalité, on relance.
 * Gagné : sa vitre se fend, son œil s'éteint, ta machine crache des pièces, fondu
 * blanc (la victoire, puis « Le Jackpot »). Perdu : ta machine grille, son œil
 * flambe, fondu noir.
 *
 * Les manches viennent de {@link LastDrawEvent} : la scène montre le tirage déjà décidé.
 */
public class LastDrawCutscene extends Cutscene {

    /** Le décor sort du noir, « DERNIER TIRAGE » s'affiche ; la première manche commence ensuite. */
    public static final float INTRO      = 1.6f;
    /** Une manche, à partir de son début : la main saisit ton levier... */
    public static final float GRAB       = 0.0f;
    /** ... et le tire (voir le son cutscene/last_draw_round) ; ton rouleau part. */
    public static final float PULL       = 0.25f;
    public static final float PULL_DOWN  = 0.55f;
    /** Son levier descend tout seul ; son rouleau part. */
    public static final float HER_PULL   = 0.85f;
    public static final float HER_DOWN   = 1.15f;
    /** Ton rouleau s'arrête, puis le sien, plus tard (suspense). */
    public static final float STOP       = 2.2f;
    public static final float HER_STOP   = 3.0f;
    /** Durée d'une manche, et pause « ÉGALITÉ ! ON RELANCE » après une manche nulle. */
    public static final float ROUND      = 3.3f;
    public static final float TIE        = 1.2f;
    /** Après la dernière manche : le verdict, puis le fondu. */
    public static final float VERDICT    = 1.9f;
    public static final float COVER      = 0.6f;

    private static final Color OUT = c("140a0a"), GOLD = c("ffc93a"), GOLD_LIGHT = c("ffe58a"), GOLD_DARK = c("b8801f");
    private static final Color RED = c("c8203a"), RED_LIGHT = c("ff5a6e"), RED_DARK = c("7a0f20");
    private static final Color CHROME = c("d8d8e0"), CHROME_DARK = c("8a8a9a"), GLASS = c("f4ecdc");
    private static final Color SKY_TOP = c("05071a"), SKY_LOW = c("2b1a4a"), STONE = c("3b3550"), STONE_DARK = c("262238");
    private static final Color HOT = c("fff6c8"), EYE_RED = c("ff2a3a");

    private final LastDrawEvent draw;
    private final BitmapFont font;
    private final Starfield stars = new Starfield(180, 0.25f);
    private final TextureRegion[] mineStrip, herStrip;
    private final int[] mineAt, herAt;               // où s'arrête chaque manche, dans chaque bande
    private final TextureRegion eye, gem, glove, coin;
    private final Array<Particle> coins  = new Array<>(false, 128);
    private final Array<Particle> sparks = new Array<>(false, 128);
    private final Sound roundSound, tieSound, winSound, loseSound;
    private final List<long[]> playing = new ArrayList<>(); // {indice du son, id} des sons lancés par la scène
    private final Sound[] sounds;
    private final float end;                          // fin de la dernière manche
    private int   roundsStarted, landed, herLanded;
    private boolean tieSounded, verdict;
    private float bright = 1f, mineBright = 1f, sparkDebt;

    public LastDrawCutscene(CutsceneKit kit, LastDrawEvent draw) {
        super(kit.settings(), kit.shake(), kit.sound("last_draw"));
        this.draw = draw;
        font = kit.font();
        roundSound = kit.sound("last_draw_round");
        tieSound   = kit.sound("last_draw_tie");
        winSound   = kit.sound("last_draw_win");
        loseSound  = kit.sound("last_draw_lose");
        sounds = new Sound[] {roundSound, tieSound, winSound, loseSound};
        List<Symbol> classic = Symbol.classicReels();
        mineStrip = strip(draw.playerReels);
        herStrip  = strip(classic);
        int rounds = draw.mine.size();
        mineAt = new int[rounds];
        herAt  = new int[rounds];
        for (int r = 0; r < rounds; r++) {
            mineAt[r] = draw.playerReels.indexOf(draw.mine.get(r));
            herAt[r]  = classic.indexOf(draw.hers.get(r));
        }
        end = roundStart(rounds - 1) + ROUND;
        Color[] eyeColors = {OUT, Color.WHITE, c("e0283a"), GOLD, c("6e0f1e")};
        eye = art("owrgd", eyeColors,
            "...oooooo...",
            ".oowwwwwwoo.",
            "owwwggggwwwo",
            "owwgrrrrgwwo",
            "owwgrddrgwwo",
            "owwgrrrrgwwo",
            "owwwggggwwwo",
            ".oowwwwwwoo.",
            "...oooooo...");
        gem = art("ow", new Color[] {OUT, Color.WHITE},
            "..o..",
            ".owo.",
            "owwwo",
            ".owo.",
            "..o..");
        glove = art("owg", new Color[] {OUT, Color.WHITE, c("c8c8d4")},
            "..oo.oo.oo..",
            ".owwowwowwo.",
            ".owwowwowwoo",
            ".owwowwowwowo",
            "ooowwwwwwwwwo",
            "owwwwwwwwwwgo",
            "owwwwwwwwwggo",
            ".owwwwwwwggo.",
            "..oowwwwggo..",
            "....oooooo...",
            "....ogggo....",
            "....ogggo....",
            "....ooooo....");
        coin = load("hud/coin.png");
    }

    private TextureRegion[] strip(List<Symbol> reels) {
        TextureRegion[] strip = new TextureRegion[reels.size()];
        for (int i = 0; i < strip.length; i++) strip[i] = load(reels.get(i).getAssetPath());
        return strip;
    }

    /** @return le début de la manche {@code round} (une pause « égalité » après chaque manche nulle). */
    private static float roundStart(int round) { return INTRO + round * (ROUND + TIE); }

    @Override protected float coverStart() { return end + VERDICT; }
    @Override protected float coverFull()  { return end + VERDICT + COVER; }
    @Override protected Color coverColor() { return draw.playerWins ? Color.WHITE : Color.BLACK; }

    @Override
    protected void reset() {
        stopSounds();
        coins.clear();
        sparks.clear();
        roundsStarted = landed = herLanded = 0;
        tieSounded = verdict = false;
        bright = mineBright = 1f;
        sparkDebt = 0f;
    }

    // -------------------------------------------------------------------------
    // Sons de la scène (en plus de celui de l'intro, joué par Cutscene)
    // -------------------------------------------------------------------------

    private void sound(Sound sound) {
        if (sound == null) return;
        for (int i = 0; i < sounds.length; i++) {
            if (sounds[i] == sound) playing.add(new long[] {i, sound.play()});
        }
    }

    private void stopSounds() {
        for (long[] id : playing) sounds[(int) id[0]].stop(id[1]);
        playing.clear();
    }

    @Override
    public void skip() {
        if (isPlaying() && time < coverStart()) stopSounds();
        super.skip();
    }

    @Override
    public void pauseSound() {
        super.pauseSound();
        if (isVisible()) for (long[] id : playing) sounds[(int) id[0]].pause(id[1]);
    }

    @Override
    public void resumeSound() {
        super.resumeSound();
        if (isVisible()) for (long[] id : playing) sounds[(int) id[0]].resume(id[1]);
    }

    @Override
    public void cancel() {
        stopSounds();
        super.cancel();
    }

    // -------------------------------------------------------------------------
    // Déroulé
    // -------------------------------------------------------------------------

    @Override
    protected void simulate(float delta) {
        int rounds = draw.mine.size();
        if (roundsStarted < rounds && time >= roundStart(roundsStarted)) {
            roundsStarted++;
            tieSounded = false;
            sound(roundSound);
        }
        int round = roundsStarted - 1;
        if (round >= 0) {
            float t = time - roundStart(round);
            if (t >= PULL_DOWN && t - delta < PULL_DOWN) rumble(0.15f, 5f);
            if (t >= HER_DOWN && t - delta < HER_DOWN) rumble(0.25f, 9f);
            if (landed <= round && t >= STOP) {
                landed = round + 1;
                rumble(0.12f, 4f);
            }
            if (herLanded <= round && t >= HER_STOP) {
                herLanded = round + 1;
                rumble(0.2f, 8f);
            }
            if (round < rounds - 1 && !tieSounded && t >= ROUND) {
                tieSounded = true;
                sound(tieSound);
            }
        }
        if (!verdict && time >= end) {
            verdict = true;
            sound(draw.playerWins ? winSound : loseSound);
            rumble(0.6f, draw.playerWins ? 14f : 18f);
            Mine mine = mine(), hers = hers();
            if (draw.playerWins) {
                for (int i = 0; i < 110; i++) {                // ta machine crache des pièces
                    Particle piece = particle(mine.cx + (random.nextFloat() - 0.5f) * mine.u * 10f, mine.tray,
                        45f + 90f * random.nextFloat(), 500f + 1100f * random.nextFloat(), 1.4f + random.nextFloat(),
                        0.5f + 0.5f * random.nextFloat());
                    piece.gravity = 1300f;
                    piece.region = coin;
                    piece.spin = (random.nextFloat() - 0.5f) * 900f;
                    coins.add(piece);
                }
                burst(hers.cx, hers.reelY + hers.reelH / 2f, c("ffb040"), 50);
            } else {
                burst(mine.cx, mine.reelY + mine.reelH / 2f, c("ff8a3a"), 60);
            }
        }
        if (verdict) {
            float k = progress(end, end + 1.2f);
            if (draw.playerWins) bright = 1f - 0.65f * k + (random.nextFloat() < 0.2f * (1f - k) ? 0.3f : 0f);
            else mineBright = 1f - 0.7f * k + (random.nextFloat() < 0.25f * (1f - k) ? 0.35f : 0f);
            sparkDebt += 40f * delta * (1f - k);                // ce qui reste du perdant grésille
            Mine loser = draw.playerWins ? hers() : mine();
            while (sparkDebt >= 1f) {
                sparkDebt -= 1f;
                Particle spark = particle(loser.cx + (random.nextFloat() - 0.5f) * loser.w,
                    loser.base + loser.h * random.nextFloat(), 40f + 100f * random.nextFloat(),
                    120f + 260f * random.nextFloat(), 0.3f + 0.4f * random.nextFloat(), 10f + 14f * random.nextFloat());
                spark.gravity = 600f;
                spark.color.set(random.nextBoolean() ? GOLD_LIGHT : c("ff8a3a"));
                sparks.add(spark);
            }
        }
        update(coins, delta);
        update(sparks, delta);
    }

    private void burst(float x, float y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            Particle spark = particle(x, y, random.nextFloat() * 360f, 200f + 600f * random.nextFloat(),
                0.4f + 0.5f * random.nextFloat(), 14f + 20f * random.nextFloat());
            spark.drag = 0.2f;
            spark.gravity = 300f;
            spark.color.set(color);
            sparks.add(spark);
        }
    }

    // -------------------------------------------------------------------------
    // Dessin
    // -------------------------------------------------------------------------

    /** Où se tient une des deux machines, à l'écran. */
    private static final class Mine {
        float cx, base, u, w, h, reelX, reelY, reelW, reelH, tray;
    }

    private Mine mine() { return place(0.3f, 1f, 26f); }
    private Mine hers() { return place(0.7f, 1.12f, 26f); }

    private Mine place(float at, float scale, float width) {
        Mine m = new Mine();
        m.u = worldHeight() / 80f * scale;
        m.cx = worldWidth() * at;
        m.base = worldHeight() * 0.15f;
        m.w = width * m.u;
        m.h = 34f * m.u;
        m.reelW = 18f * m.u;
        m.reelH = 15f * m.u;
        m.reelX = m.cx - m.reelW / 2f;
        m.reelY = m.base + 14f * m.u;
        m.tray = m.base + 5f * m.u;
        return m;
    }

    @Override
    protected void drawScene(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, SKY_LOW, SKY_TOP);
        stars.draw(batch, 1f);
        Mine mine = mine(), hers = hers();
        // Projecteurs de casino qui balaient les deux machines.
        float lit = progress(0.3f, 1.2f);
        additive(batch);
        for (int i = 0; i < 2; i++) {
            float x = i == 0 ? mine.cx : hers.cx, sway = MathUtils.sin(time * 1.3f + i * 2f) * 8f;
            batch.setColor(i == 0 ? 1f : 1f, i == 0 ? 0.9f : 0.5f, i == 0 ? 0.6f : 0.5f, 0.18f * lit);
            batch.draw(trail, x, height, 0f, 60f, height * 1.1f, 120f, 1f, 1f, -90f + sway);
        }
        normal(batch);
        glow(batch, mine.cx, mine.base + mine.h * 0.6f, mine.h * 2.2f, GOLD, 0.25f * lit * mineBright);
        glow(batch, hers.cx, hers.base + hers.h * 0.6f, hers.h * 2.4f, c("6a4aff"), 0.3f * lit);
        glow(batch, hers.cx, hers.base + hers.h * 0.6f, hers.h * 2.0f, EYE_RED, 0.2f * lit * bright
            + (verdict && !draw.playerWins ? 0.45f * progress(end, end + 0.4f) : 0f));
        drawFloor(batch, width, mine.base);
        drawPlayerMachine(batch, mine);
        drawHerMachine(batch, hers);
        drawParticles(batch, sparks);
        drawParticles(batch, coins);
        drawTexts(batch, mine, hers);
        int round = roundsStarted - 1;
        if (round >= 0) {
            float t = time - roundStart(round);
            flash(batch, Color.WHITE, roundStart(round) + HER_DOWN, 0.15f, 0.2f);
            if (t >= 0f) flash(batch, HOT, roundStart(round) + HER_STOP, 0.12f, 0.15f);
        }
        if (verdict) flash(batch, draw.playerWins ? HOT : EYE_RED, end, 0.35f, 0.55f);
        fadeFromBlack(batch, 0.6f);
    }

    /** Le sommet de la Tour : dalles et créneaux de pierre. */
    private void drawFloor(Batch batch, float width, float top) {
        float u = worldHeight() / 80f;
        rect(batch, STONE_DARK, -MARGIN, -MARGIN, width + 2f * MARGIN, top + MARGIN, 1f);
        for (int row = 0; row * 5f * u < top + MARGIN; row++) {
            float y = top - (row + 1) * 5f * u;
            for (float x = -MARGIN + (row % 2) * 4.2f * u; x < width + MARGIN; x += 8.4f * u) {
                rect(batch, STONE, x + u * 0.5f, y + u * 0.5f, 7.4f * u, 4f * u, 1f);
            }
        }
        rect(batch, OUT, -MARGIN, top - u, width + 2f * MARGIN, u, 1f);
    }

    /** @return la position (en symboles) de la bande d'un rouleau, qui s'arrête pile sur la manche jouée. */
    private float offset(int[] at, int length, float spinFrom, float stopAt, Interpolation ease, float spins) {
        int round = Math.max(0, roundsStarted - 1);
        int from = round == 0 ? (at[0] + length / 2) % length : at[round - 1];
        if (roundsStarted == 0) return from;
        float t = time - roundStart(round);
        float distance = Math.floorMod(at[round] - from, length) + spins * length;
        if (t < spinFrom) return from;
        if (t >= stopAt) {                                       // petit rebond au moment où il se pose
            float since = t - stopAt;
            return at[round] - 0.12f * (float) Math.exp(-since * 10f) * MathUtils.sin(since * 28f);
        }
        return from + distance * ease.apply((t - spinFrom) / (stopAt - spinFrom));
    }

    /** @return où en est un levier (0 levé, 1 baissé) : il descend de {@code from} à {@code down}, puis remonte. */
    private float lever(float from, float down) {
        if (roundsStarted == 0) return 0f;
        float t = time - roundStart(roundsStarted - 1);
        if (t < from) return 0f;
        if (t < down) return Interpolation.pow2In.apply((t - from) / (down - from));
        return 1f - Interpolation.pow2Out.apply(MathUtils.clamp((t - down - 0.12f) / 0.4f, 0f, 1f));
    }

    /** Ta machine : rouge et chrome, un rouleau, le levier à gauche. */
    private void drawPlayerMachine(Batch batch, Mine m) {
        float u = m.u, left = m.cx - m.w / 2f, b = mineBright;
        float pull = lever(PULL, PULL_DOWN);
        float[] knob = drawLever(batch, left - u, m.base + m.h * 0.55f, u, pull, -1, b);
        // Corps.
        rect(batch, OUT, left - u, m.base - u, m.w + 2f * u, m.h + 2f * u, b);
        rect(batch, RED, left, m.base, m.w, m.h, b);
        rect(batch, RED_LIGHT, left, m.base + m.h - 3f * u, m.w, 2f * u, b);
        rect(batch, RED_LIGHT, left, m.base, 2f * u, m.h, b);
        rect(batch, RED_DARK, left + m.w - 3f * u, m.base, 3f * u, m.h, b);
        rect(batch, CHROME, left, m.base + 10f * u, m.w, 1.5f * u, b);
        rect(batch, CHROME_DARK, left, m.base, m.w, 2f * u, b);
        // Fronton à ampoules.
        float signW = m.w - 4f * u, signY = m.base + m.h + u;
        rect(batch, OUT, m.cx - signW / 2f - u, signY - u, signW + 2f * u, 8f * u, b);
        rect(batch, GOLD, m.cx - signW / 2f, signY, signW, 6f * u, b);
        bulbs(batch, m.cx - signW / 2f, signY + 3f * u, signW, 7, u, b, 0);
        drawWindow(batch, m, mineStrip, offset(mineAt, mineStrip.length, PULL_DOWN, STOP, Interpolation.pow3Out, 2f), b,
            landed > 0 && landed == roundsStarted);
        drawTray(batch, m, b);
        if (draw.playerWins && verdict) glow(batch, m.cx, m.reelY + m.reelH / 2f, m.reelW * 3f, GOLD,
            0.5f + 0.3f * MathUtils.sin(time * 14f));
        // La main gantée saisit le levier et le tire.
        if (roundsStarted > 0) {
            float t = time - roundStart(roundsStarted - 1);
            float in = Interpolation.pow2Out.apply(MathUtils.clamp((t - GRAB) / (PULL - GRAB), 0f, 1f));
            float out = MathUtils.clamp((t - PULL_DOWN - 0.15f) / 0.3f, 0f, 1f);
            float alpha = in * (1f - out);
            if (alpha > 0f) {
                float dx = (1f - in) * -14f * u - out * 10f * u, dy = (1f - in) * 6f * u;
                batch.setColor(1f, 1f, 1f, alpha);
                sprite(batch, glove, knob[0] + dx - 1.5f * u, knob[1] + dy, 0.9f * u, 0f);
            }
        }
    }

    /** Sa machine : dorée, plus grande, son œil au-dessus du rouleau ; le levier à droite, qui descend tout seul. */
    private void drawHerMachine(Batch batch, Mine m) {
        float u = m.u, left = m.cx - m.w / 2f, b = bright;
        float pull = lever(HER_PULL, HER_DOWN);
        float[] knob = drawLever(batch, left + m.w + u, m.base + m.h * 0.55f, u, pull, 1, b);
        if (pull > 0f) glow(batch, knob[0], knob[1], 14f * u, EYE_RED, 0.5f * pull * b);
        rect(batch, OUT, left - u, m.base - u, m.w + 2f * u, m.h + 2f * u, b);
        rect(batch, GOLD, left, m.base, m.w, m.h, b);
        rect(batch, GOLD_LIGHT, left, m.base + m.h - 3f * u, m.w, 2f * u, b);
        rect(batch, GOLD_LIGHT, left, m.base, 2f * u, m.h, b);
        rect(batch, GOLD_DARK, left + m.w - 3f * u, m.base, 3f * u, m.h, b);
        rect(batch, GOLD_DARK, left, m.base, m.w, 2f * u, b);
        // Dôme à gradins, œil et diamant.
        for (int step = 0; step < 3; step++) {
            float sw = m.w - (step + 1) * 6f * u, sy = m.base + m.h + step * 4f * u;
            rect(batch, OUT, m.cx - sw / 2f - u, sy, sw + 2f * u, 5f * u, b);
            rect(batch, step % 2 == 0 ? GOLD : GOLD_LIGHT, m.cx - sw / 2f, sy, sw, 4f * u, b);
        }
        float gemY = m.base + m.h + 15f * u;
        glow(batch, m.cx, gemY, 22f * u, Color.WHITE, (0.4f + 0.3f * MathUtils.sin(time * 5f)) * b);
        batch.setColor(b, b, b, 1f);
        sprite(batch, gem, m.cx, gemY, 1.6f * u, 0f);
        float eyeY = m.base + m.h + 5f * u;
        float stare = (pull > 0f ? 0.6f : 0.25f) + (verdict && !draw.playerWins ? 0.6f : 0f);
        glow(batch, m.cx, eyeY, 16f * u, EYE_RED, stare * b);
        batch.setColor(b, b, b, 1f);
        sprite(batch, eye, m.cx, eyeY, 0.8f * u, 0f);
        bulbs(batch, left + 2f * u, m.base + 11.5f * u, m.w - 4f * u, 8, u, b, 1);
        drawWindow(batch, m, herStrip, offset(herAt, herStrip.length, HER_DOWN, HER_STOP, Interpolation.pow5Out, 3f), b,
            herLanded > 0 && herLanded == roundsStarted);
        if (draw.playerWins && verdict) drawCrack(batch, m);
        drawTray(batch, m, b);
    }

    /**
     * Le levier sur le côté {@code side} (-1 gauche, 1 droite), pivot en
     * ({@code x}, {@code y}) ; {@code pull} 0 levé, 1 baissé.
     * @return la position de sa boule
     */
    private float[] drawLever(Batch batch, float x, float y, float u, float pull, int side, float b) {
        float angle = MathUtils.lerp(80f, -60f, pull), length = 18f * u;
        if (side < 0) angle = 180f - angle;
        float px = side > 0 ? x : x - 4f * u;
        rect(batch, OUT, px - u, y - 3f * u, 5f * u, 6f * u, b);
        rect(batch, GOLD_DARK, px, y - 2f * u, 3f * u, 4f * u, b);
        float pivotX = px + 1.5f * u;
        batch.setColor(OUT.r * b, OUT.g * b, OUT.b * b, 1f);
        batch.draw(pixel, pivotX, y - 1.5f * u, 0f, 1.5f * u, length, 3f * u, 1f, 1f, angle);
        batch.setColor(CHROME.r * b, CHROME.g * b, CHROME.b * b, 1f);
        batch.draw(pixel, pivotX, y - 0.7f * u, 0f, 0.7f * u, length, 1.4f * u, 1f, 1f, angle);
        float knobX = pivotX + MathUtils.cosDeg(angle) * length, knobY = y + MathUtils.sinDeg(angle) * length;
        rect(batch, OUT, knobX - 3f * u, knobY - 3f * u, 6f * u, 6f * u, b);
        rect(batch, c("e0283a"), knobX - 2f * u, knobY - 2f * u, 4f * u, 4f * u, b);
        rect(batch, c("ff8a9a"), knobX - 1.2f * u, knobY + 0.4f * u, 1.2f * u, 1.2f * u, b);
        return new float[] {knobX, knobY};
    }

    /** La vitre du rouleau, la bande qui défile et la ligne de gain ; {@code landed} : posée, elle brille. */
    private void drawWindow(Batch batch, Mine m, TextureRegion[] strip, float offset, float b, boolean landed) {
        float u = m.u;
        rect(batch, OUT, m.reelX - 1.5f * u, m.reelY - 1.5f * u, m.reelW + 3f * u, m.reelH + 3f * u, b);
        rect(batch, CHROME, m.reelX - u, m.reelY - u, m.reelW + 2f * u, m.reelH + 2f * u, b);
        rect(batch, GLASS, m.reelX, m.reelY, m.reelW, m.reelH, b);
        batch.setColor(b, b, b, 1f);
        reel(batch, strip, m.reelX, m.reelY, m.reelW, m.reelH, ((offset % strip.length) + strip.length) % strip.length);
        // Ombre en haut et en bas de la vitre : le rouleau est un cylindre.
        for (int i = 0; i < 4; i++) {
            float a = 0.22f * (1f - i / 4f);
            batch.setColor(0f, 0f, 0f, a);
            batch.draw(pixel, m.reelX, m.reelY + m.reelH - (i + 1) * u, m.reelW, u);
            batch.draw(pixel, m.reelX, m.reelY + i * u, m.reelW, u);
        }
        rect(batch, c("e0283a"), m.reelX - 3f * u, m.reelY + m.reelH / 2f - 0.5f * u, 1.5f * u, u, b);
        rect(batch, c("e0283a"), m.reelX + m.reelW + 1.5f * u, m.reelY + m.reelH / 2f - 0.5f * u, 1.5f * u, u, b);
        if (landed && !verdict) glow(batch, m.cx, m.reelY + m.reelH / 2f, m.reelW * 2f, GOLD,
            0.25f + 0.15f * MathUtils.sin(time * 10f));
    }

    /** Sa vitre se fend : des éclats qui partent du centre. */
    private void drawCrack(Batch batch, Mine m) {
        float k = Interpolation.pow2Out.apply(progress(end, end + 0.25f));
        float x = m.cx, y = m.reelY + m.reelH / 2f;
        batch.setColor(Color.WHITE);
        for (int i = 0; i < 7; i++) {
            float angle = i * 51f + 17f, length = (0.35f + 0.25f * ((i * 37) % 5) / 5f) * m.reelW * k;
            batch.setColor(OUT);
            batch.draw(pixel, x, y - m.u * 0.4f, 0f, m.u * 0.4f, length, m.u * 0.8f, 1f, 1f, angle);
            batch.setColor(1f, 1f, 1f, 0.7f);
            batch.draw(pixel, x, y - m.u * 0.15f, 0f, m.u * 0.15f, length * 0.9f, m.u * 0.3f, 1f, 1f, angle + 2f);
        }
    }

    private void drawTray(Batch batch, Mine m, float b) {
        float u = m.u, trayW = m.w * 0.6f;
        rect(batch, OUT, m.cx - trayW / 2f, m.tray - 2f * u, trayW, 6f * u, b);
        rect(batch, c("2a1a10"), m.cx - trayW / 2f + u, m.tray - u, trayW - 2f * u, 4f * u, b);
    }

    /** Une rangée d'ampoules qui clignotent en chenillard. */
    private void bulbs(Batch batch, float x, float y, float width, int count, float u, float b, int phase) {
        for (int i = 0; i < count; i++) {
            float bx = x + (i + 0.5f) * width / count;
            boolean on = ((int) (time * 8f) + i + phase) % 2 == 0 && b > 0.6f;
            rect(batch, OUT, bx - u, y - u, 2f * u, 2f * u, b);
            if (on) glow(batch, bx, y, 6f * u, GOLD, 0.6f);
            rect(batch, on ? HOT : GOLD_DARK, bx - 0.5f * u, y - 0.5f * u, u, u, b);
        }
    }

    /** Titre, noms, symboles tirés avec leur rang, égalité et verdict. */
    private void drawTexts(Batch batch, Mine mine, Mine hers) {
        float width = worldWidth(), height = worldHeight();
        float scale = font.getData().scaleX;
        int round = roundsStarted - 1;
        float t = round >= 0 ? time - roundStart(round) : 0f;
        // Égalité : le texte prend la place du titre jusqu'à la manche suivante.
        float tie = round >= 0 && round < draw.mine.size() - 1
            ? progress(roundStart(round) + ROUND, roundStart(round) + ROUND + 0.2f) : 0f;
        float gone = verdict ? Interpolation.pow2Out.apply(progress(end, end + 0.3f)) : 0f;
        // « DERNIER TIRAGE », qui tombe et s'imprime.
        float stamp = progress(0.4f, 0.7f);
        if (stamp > 0f) {
            font.getData().setScale(scale * MathUtils.lerp(4.2f, 2.6f, Interpolation.pow2Out.apply(stamp)));
            caption(batch, font, "DERNIER TIRAGE", width / 2f, height * 0.93f, stamp * (1f - tie) * (1f - gone));
        }
        if (tie > 0f) {
            font.getData().setScale(scale * MathUtils.lerp(3.6f, 2.6f, Interpolation.pow2Out.apply(tie)));
            caption(batch, font, "ÉGALITÉ ! ON RELANCE", width / 2f, height * 0.93f, tie);
        }
        font.getData().setScale(scale * 2f);
        float names = progress(0.8f, 1.2f) * (1f - gone);
        caption(batch, font, "TOI", mine.cx, mine.base - 3.5f * mine.u, names);
        caption(batch, font, "ELLE", hers.cx, mine.base - 3.5f * mine.u, names);
        if (round >= 0 && !verdict) {
            font.getData().setScale(scale * 1.6f);
            if (t >= STOP) plate(batch, label(draw.mine.get(round)), mine.cx, mine.base - 8f * mine.u,
                progress(roundStart(round) + STOP, roundStart(round) + STOP + 0.2f));
            if (t >= HER_STOP) plate(batch, label(draw.hers.get(round)), hers.cx, mine.base - 8f * mine.u,
                progress(roundStart(round) + HER_STOP, roundStart(round) + HER_STOP + 0.2f));
        }
        if (verdict) {
            font.getData().setScale(scale * MathUtils.lerp(4.6f, 3f, gone));
            caption(batch, font, draw.playerWins ? "TU GAGNES !" : "LA MACHINE GAGNE", width / 2f, height * 0.93f, gone);
        }
        font.getData().setScale(scale);
    }

    /** Le symbole tiré et son rang, sur une plaque sombre sous la machine. */
    private void plate(Batch batch, String text, float x, float y, float alpha) {
        float u = worldHeight() / 80f, w = 30f * u, h = 4.2f * u;
        batch.setColor(0f, 0f, 0f, 0.6f * alpha);
        batch.draw(pixel, x - w / 2f, y - h / 2f, w, h);
        batch.setColor(GOLD.r, GOLD.g, GOLD.b, 0.8f * alpha);
        batch.draw(pixel, x - w / 2f, y + h / 2f - 2f, w, 2f);
        batch.draw(pixel, x - w / 2f, y - h / 2f, w, 2f);
        caption(batch, font, text, x, y, alpha);
    }

    private static String label(Symbol symbol) {
        return symbol.getDisplayName() + "  -  RANG " + LastDrawEvent.score(symbol);
    }

    private void rect(Batch batch, Color color, float x, float y, float w, float h, float b) {
        batch.setColor(color.r * b, color.g * b, color.b * b, color.a);
        batch.draw(pixel, x, y, w, h);
    }
}
