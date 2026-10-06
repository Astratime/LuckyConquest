package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.events.LastDrawEvent;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.List;

/**
 * Le Dernier tirage de la Machine Originelle, achevée : au sommet de la Tour,
 * elle est seule, fissurée, elle crache des étincelles ; ses rouleaux tournent
 * dans le vide, au ralenti, puis s'arrêtent. Ta machine à un rouleau monte du
 * sol en face d'elle, son œil se pose sur toi. Elle parle (une bulle qui
 * s'écrit, un clic la fait avancer) : un dernier tirage décidera. Puis c'est
 * au joueur : il saisit son levier à la souris et le tire jusqu'en bas. Son
 * rouleau part, le levier de la Machine descend tout seul, les deux rouleaux
 * s'arrêtent (le sien se fait attendre). Le plus haut rang gagne (le Joker en
 * tête) ; à égalité, elle le relance et il retire. Gagné : sa vitre se fend,
 * son œil s'éteint, ta machine crache des pièces, fondu blanc (la victoire,
 * puis « Le Jackpot »). Perdu : ta machine grille, son œil flambe, fondu noir.
 * Le verdict reste {@link #VERDICT} secondes à l'écran.
 *
 * Les manches viennent de {@link LastDrawEvent} : la scène montre le tirage déjà
 * décidé. Un clic ne passe pas cette scène (il sert au levier) ; une touche, si.
 */
public class LastDrawCutscene extends Cutscene {

    /** Ses rouleaux tournent dans le vide jusque-là (voir le son cutscene/last_draw). */
    public static final float IDLE_STOP  = 2.0f;
    /** Ta machine monte du sol. */
    public static final float RISE_START = 2.0f;
    public static final float RISE_END   = 3.4f;
    /** Ta machine est en place : « DERNIER TIRAGE » s'affiche ; puis elle parle. */
    public static final float ARRIVE     = 4.0f;
    public static final float INTRO      = ARRIVE + 1.6f;
    // Une manche commence quand ton levier touche le fond (voir le son cutscene/last_draw_round) : ton rouleau part.
    /** Son levier descend tout seul ; son rouleau part. */
    public static final float HER_PULL   = 0.35f;
    public static final float HER_DOWN   = 0.65f;
    /** Ton rouleau s'arrête, puis le sien, plus tard (suspense). */
    public static final float STOP       = 1.6f;
    public static final float HER_STOP   = 2.45f;
    /** Les deux rouleaux arrêtés, leurs symboles et leurs rangs restent autant de secondes avant l'égalité ou le verdict. */
    public static final float RESULT     = 4.0f;
    /** Durée d'une manche, jusqu'à l'égalité ou au verdict. */
    public static final float ROUND      = HER_STOP + RESULT;
    /** Le verdict reste à l'écran, puis le fondu. */
    public static final float VERDICT    = 4.0f;
    public static final float COVER      = 0.6f;
    /** La bulle s'écrit à cette vitesse (lettres par seconde), puis reste affichée avant la réplique suivante. */
    private static final float LETTERS   = 32f;
    private static final float LINE_HOLD = 1.1f;
    /** Le levier lâché avant le fond remonte en autant de secondes. */
    private static final float SPRING    = 0.3f;
    /** Crans du levier : un cliquetis à chacun. */
    private static final int   NOTCHES   = 6;

    private static final String[] INTRO_LINES = {
        "Tu crois m'avoir achevée ?",
        "Pas si vite. Un dernier tirage.",
        "Le plus haut rang gagne. L'autre tombe.",
        "Tire ton levier. Si tu l'oses."};
    private static final String[] TIE_LINES = {"Égalité.", "Encore. Tire !"};
    private static final String WIN_LINE  = "Non... Impossible...";
    private static final String LOSE_LINE = "La Machine gagne toujours.";

    private static final Color OUT = c("140a0a"), GOLD = c("ffc93a"), GOLD_LIGHT = c("ffe58a"), GOLD_DARK = c("b8801f");
    private static final Color RED = c("c8203a"), RED_LIGHT = c("ff5a6e"), RED_DARK = c("7a0f20");
    private static final Color CHROME = c("d8d8e0"), CHROME_DARK = c("8a8a9a"), GLASS = c("f4ecdc");
    private static final Color SKY_TOP = c("05071a"), SKY_LOW = c("2b1a4a"), STONE = c("3b3550"), STONE_DARK = c("262238");
    private static final Color HOT = c("fff6c8"), EYE_RED = c("ff2a3a"), BUBBLE = c("1c0f24");

    private final LastDrawEvent draw;
    private final BitmapFont font;
    private final GlyphLayout bubbleLayout = new GlyphLayout();
    private final Starfield stars = new Starfield(180, 0.25f);
    private final TextureRegion[] mineStrip, herStrip;
    private final int[] mineAt, herAt;               // où s'arrête chaque manche, dans chaque bande
    private final float[] roundAt;                   // quand chaque manche a commencé (ton levier au fond), -1 avant
    private final TextureRegion eye, gem, glove, coin;
    private final Array<Particle> coins  = new Array<>(false, 128);
    private final Array<Particle> sparks = new Array<>(false, 128);
    private final Sound roundSound, tieSound, winSound, loseSound, notchSound;
    private final List<long[]> playing = new ArrayList<>(); // {indice du son, id} des sons lancés par la scène
    private final Sound[] sounds;
    private float end;                                // fin de la dernière manche (-1 : pas encore jouée)
    private int   roundsStarted, landed, herLanded;
    private boolean tieSaid, verdict, introSaid, idleStopped, risen;
    private float bright = 1f, mineBright = 1f, sparkDebt;
    /** La bulle de la Machine : ses répliques, celle en cours et quand elle a commencé à s'écrire. */
    private String[] lines;
    private int   line;
    private float lineAt;
    /** Ton levier, à la souris : saisi, où en est le geste, et où il en était quand tu l'as lâché. */
    private boolean dragging;
    private float grabY, dragPull, releasedPull, releasedAt = -10f;
    private int   notch;

    public LastDrawCutscene(CutsceneKit kit, LastDrawEvent draw) {
        super(kit.settings(), kit.shake(), kit.sound("last_draw"));
        this.draw = draw;
        font = kit.font();
        roundSound = kit.sound("last_draw_round");
        tieSound   = kit.sound("last_draw_tie");
        winSound   = kit.sound("last_draw_win");
        loseSound  = kit.sound("last_draw_lose");
        notchSound = kit.sound("last_draw_notch");
        sounds = new Sound[] {roundSound, tieSound, winSound, loseSound, notchSound};
        List<Symbol> classic = Symbol.classicReels();
        mineStrip = strip(draw.playerReels);
        herStrip  = strip(classic);
        int rounds = draw.mine.size();
        mineAt  = new int[rounds];
        herAt   = new int[rounds];
        roundAt = new float[rounds];
        for (int r = 0; r < rounds; r++) {
            mineAt[r] = draw.playerReels.indexOf(draw.mine.get(r));
            herAt[r]  = classic.indexOf(draw.hers.get(r));
        }
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

    /** @return le début de la manche {@code round} (ton levier au fond), ou une date très lointaine si elle attend. */
    private float roundStart(int round) { return round < roundsStarted ? roundAt[round] : Float.MAX_VALUE / 4f; }

    @Override protected float coverStart() { return end < 0f ? Float.MAX_VALUE / 2f : end + VERDICT; }
    @Override protected float coverFull()  { return end < 0f ? Float.MAX_VALUE : end + VERDICT + COVER; }
    @Override protected Color coverColor() { return draw.playerWins ? Color.WHITE : Color.BLACK; }

    @Override
    protected void reset() {
        stopSounds();
        coins.clear();
        sparks.clear();
        roundsStarted = landed = herLanded = 0;
        tieSaid = verdict = introSaid = dragging = idleStopped = risen = false;
        bright = mineBright = 1f;
        sparkDebt = 0f;
        end = -1f;
        lines = null;
        releasedPull = 0f;
        releasedAt = -10f;
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
    // La bulle et le levier
    // -------------------------------------------------------------------------

    /** La Machine dit {@code said}, une réplique après l'autre. */
    private void say(String... said) {
        lines = new String[said.length];
        for (int i = 0; i < said.length; i++) lines[i] = Lang.t(said[i]);
        line = 0;
        lineAt = time;
    }

    /** @return le temps qu'il faut pour écrire la réplique en cours. */
    private float typing() { return lines[line].length() / LETTERS; }

    /** La réplique suivante, ou la fin de la bulle (la dernière du verdict reste jusqu'au fondu). */
    private void nextLine() {
        if (line < lines.length - 1) {
            line++;
            lineAt = time;
        } else if (!verdict) {
            lines = null;
        }
    }

    /** @return {@code true} quand le joueur peut tirer son levier : elle a fini de parler et la manche suivante attend. */
    private boolean awaitingPull() {
        if (!introSaid || lines != null || roundsStarted >= draw.mine.size()) return false;
        return roundsStarted == 0 || time >= roundAt[roundsStarted - 1] + ROUND;
    }

    @Override
    protected boolean pointerDown(float x, float y) {
        if (lines != null && !verdict) {                       // la bulle : finir la réplique, ou passer à la suivante
            if (time - lineAt < typing()) lineAt = time - typing();
            else nextLine();
            return true;
        }
        if (!awaitingPull() || !onLever(x, y)) return true;
        dragging = true;
        grabY = y;
        dragPull = 0f;
        notch = 0;
        return true;
    }

    @Override
    protected void pointerDragged(float x, float y) {
        if (!dragging) return;
        Mine m = mine();
        dragPull = MathUtils.clamp((grabY - y) / (26f * m.u), 0f, 1f);
        int reached = (int) (dragPull * NOTCHES);
        if (reached > notch) sound(notchSound);
        notch = Math.max(notch, reached);
        if (dragPull >= 1f) startRound();
    }

    @Override
    protected void pointerUp(float x, float y) {
        if (!dragging) return;
        dragging = false;                                      // lâché avant le fond : il remonte
        releasedPull = dragPull;
        releasedAt = time;
    }

    /** @return {@code true} si ({@code x}, {@code y}) tombe sur ta machine ou son levier (on peut l'attraper là). */
    private boolean onLever(float x, float y) {
        Mine m = mine();
        float left = m.cx - m.w / 2f - 28f * m.u, right = m.cx + m.w / 2f;
        return x >= left && x <= right && y >= m.base && y <= m.base + m.h + 22f * m.u;
    }

    /** Ton levier touche le fond : la manche commence. */
    private void startRound() {
        dragging = false;
        roundAt[roundsStarted] = time;
        roundsStarted++;
        tieSaid = false;
        if (roundsStarted == draw.mine.size()) end = time + ROUND;
        sound(roundSound);
        rumble(0.15f, 5f);
    }

    // -------------------------------------------------------------------------
    // Déroulé
    // -------------------------------------------------------------------------

    @Override
    protected void simulate(float delta) {
        int rounds = draw.mine.size();
        if (time < ARRIVE + 1f) {                                // fissurée, elle crache des étincelles
            Mine hers = hers();
            sparkDebt += MathUtils.lerp(30f, 4f, progress(0f, ARRIVE + 1f)) * delta;
            while (sparkDebt >= 1f) {
                sparkDebt -= 1f;
                Particle spark = particle(hers.cx + (random.nextFloat() - 0.5f) * hers.w,
                    hers.base + hers.h * (0.2f + 0.8f * random.nextFloat()), 30f + 120f * random.nextFloat(),
                    150f + 300f * random.nextFloat(), 0.3f + 0.4f * random.nextFloat(), 10f + 12f * random.nextFloat());
                spark.gravity = 700f;
                spark.color.set(random.nextBoolean() ? GOLD_LIGHT : c("ff8a3a"));
                sparks.add(spark);
            }
        }
        if (!idleStopped && time >= IDLE_STOP) {
            idleStopped = true;
            rumble(0.15f, 5f);
        }
        if (!risen && time >= RISE_END) {                       // ta machine se pose : la poussière
            risen = true;
            rumble(0.3f, 10f);
            Mine mine = mine();
            for (int i = 0; i < 40; i++) {
                Particle dust = particle(mine.cx + (random.nextFloat() - 0.5f) * mine.w * 1.4f, mine.base,
                    random.nextBoolean() ? 10f + 30f * random.nextFloat() : 140f + 30f * random.nextFloat(),
                    80f + 200f * random.nextFloat(), 0.5f + 0.5f * random.nextFloat(), 30f + 40f * random.nextFloat());
                dust.drag = 0.2f;
                dust.color.set(0.55f, 0.5f, 0.6f, 0.6f);
                sparks.add(dust);
            }
        }
        if (!introSaid && time >= INTRO) {
            introSaid = true;
            say(INTRO_LINES);
        }
        if (lines != null && time - lineAt >= typing() + LINE_HOLD) nextLine();
        int round = roundsStarted - 1;
        if (round >= 0) {
            float t = time - roundAt[round];
            if (t >= HER_DOWN && t - delta < HER_DOWN) rumble(0.25f, 9f);
            if (landed <= round && t >= STOP) {
                landed = round + 1;
                rumble(0.12f, 4f);
            }
            if (herLanded <= round && t >= HER_STOP) {
                herLanded = round + 1;
                rumble(0.2f, 8f);
            }
            if (round < rounds - 1 && !tieSaid && t >= ROUND) {
                tieSaid = true;
                sound(tieSound);
                say(TIE_LINES);
            }
        }
        if (!verdict && end >= 0f && time >= end) {
            verdict = true;
            say(draw.playerWins ? WIN_LINE : LOSE_LINE);
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

    private Mine mine() {
        Mine m = place(0.3f, 1f, 26f);
        float rise = 1f - Interpolation.pow2Out.apply(progress(RISE_START, RISE_END));   // elle monte du sol
        if (rise > 0f) {
            float drop = rise * (m.h + 30f * m.u);
            m.base -= drop;
            m.reelY -= drop;
            m.tray -= drop;
        }
        return m;
    }
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
        // Projecteurs de casino qui balaient les deux machines (le tien s'allume quand ta machine arrive).
        float lit = progress(0.3f, 1.2f), mineLit = progress(RISE_START, RISE_END);
        additive(batch);
        for (int i = 0; i < 2; i++) {
            float x = i == 0 ? mine.cx : hers.cx, sway = MathUtils.sin(time * 1.3f + i * 2f) * 8f;
            batch.setColor(i == 0 ? 1f : 1f, i == 0 ? 0.9f : 0.5f, i == 0 ? 0.6f : 0.5f, 0.18f * (i == 0 ? mineLit : lit));
            batch.draw(trail, x, height, 0f, 60f, height * 1.1f, 120f, 1f, 1f, -90f + sway);
        }
        normal(batch);
        glow(batch, mine.cx, mine.base + mine.h * 0.6f, mine.h * 2.2f, GOLD, 0.25f * mineLit * mineBright);
        glow(batch, hers.cx, hers.base + hers.h * 0.6f, hers.h * 2.4f, c("6a4aff"), 0.3f * lit);
        glow(batch, hers.cx, hers.base + hers.h * 0.6f, hers.h * 2.0f, EYE_RED, 0.2f * lit * bright
            + (verdict && !draw.playerWins ? 0.45f * progress(end, end + 0.4f) : 0f));
        boolean rising = time < RISE_END;                         // en montant, elle sort de sous les dalles
        if (rising) drawPlayerMachine(batch, mine);
        drawFloor(batch, width, hers.base);
        if (!rising) drawPlayerMachine(batch, mine);
        drawHerMachine(batch, hers);
        drawParticles(batch, sparks);
        drawParticles(batch, coins);
        drawTexts(batch, mine, hers);
        drawHint(batch, mine);
        drawBubble(batch, hers);
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

    /** @return où en est ton levier (0 levé, 1 au fond) : sous ta main, lâché qui remonte, ou remonté après le fond. */
    private float playerLever() {
        if (dragging) return dragPull;
        float released = releasedPull * (1f - Interpolation.pow2Out.apply(MathUtils.clamp((time - releasedAt) / SPRING, 0f, 1f)));
        if (roundsStarted == 0) return released;
        float t = time - roundAt[roundsStarted - 1];
        return Math.max(released, 1f - Interpolation.pow2Out.apply(MathUtils.clamp((t - 0.12f) / 0.4f, 0f, 1f)));
    }

    /** @return où en est son levier : il descend tout seul de {@link #HER_PULL} à {@link #HER_DOWN}, puis remonte. */
    private float herLever() {
        if (roundsStarted == 0) return 0f;
        float t = time - roundAt[roundsStarted - 1];
        if (t < HER_PULL) return 0f;
        if (t < HER_DOWN) return Interpolation.pow2In.apply((t - HER_PULL) / (HER_DOWN - HER_PULL));
        return 1f - Interpolation.pow2Out.apply(MathUtils.clamp((t - HER_DOWN - 0.12f) / 0.4f, 0f, 1f));
    }

    /** Ta machine : rouge et chrome, un rouleau, le levier à gauche. */
    private void drawPlayerMachine(Batch batch, Mine m) {
        float u = m.u, left = m.cx - m.w / 2f, b = mineBright;
        float pull = playerLever();
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
        drawWindow(batch, m, mineStrip, offset(mineAt, mineStrip.length, 0f, STOP, Interpolation.pow3Out, 2f), b,
            landed > 0 && landed == roundsStarted);
        drawTray(batch, m, b);
        if (draw.playerWins && verdict) glow(batch, m.cx, m.reelY + m.reelH / 2f, m.reelW * 3f, GOLD,
            0.5f + 0.3f * MathUtils.sin(time * 14f));
        // Ta main (gantée) sur la boule, tant que tu tiens le levier ; quand il t'attend, une main fantôme montre le geste.
        if (dragging) {
            batch.setColor(Color.WHITE);
            sprite(batch, glove, knob[0] - 1.5f * u, knob[1], 0.9f * u, 0f);
        } else if (awaitingPull()) {
            float demo = (time % 1.8f) / 1.8f, k = Interpolation.pow2.apply(MathUtils.clamp(demo / 0.6f, 0f, 1f));
            float angle = 180f - MathUtils.lerp(80f, -60f, k), length = 18f * u;
            float pivotX = left - u - 4f * u + 1.5f * u, pivotY = m.base + m.h * 0.55f;
            float alpha = 0.55f * MathUtils.clamp(Math.min(demo / 0.1f, (1f - demo) / 0.25f), 0f, 1f);
            batch.setColor(1f, 1f, 1f, alpha);
            sprite(batch, glove, pivotX + MathUtils.cosDeg(angle) * length - 1.5f * u,
                pivotY + MathUtils.sinDeg(angle) * length, 0.9f * u, 0f);
        }
    }

    /** Sa machine : dorée, plus grande, son œil au-dessus du rouleau ; le levier à droite, qui descend tout seul. */
    private void drawHerMachine(Batch batch, Mine m) {
        float u = m.u, left = m.cx - m.w / 2f, b = bright;
        float pull = herLever();
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
        float stare = (pull > 0f ? 0.6f : 0.25f) + (verdict && !draw.playerWins ? 0.6f : 0f)
            + 0.8f * Math.max(0f, 1f - Math.abs(time - (RISE_END + 0.3f)) / 0.5f);   // son œil se pose sur toi
        glow(batch, m.cx, eyeY, 16f * u, EYE_RED, stare * b);
        batch.setColor(b, b, b, 1f);
        sprite(batch, eye, m.cx, eyeY, 0.8f * u, 0f);
        bulbs(batch, left + 2f * u, m.base + 11.5f * u, m.w - 4f * u, 8, u, b, 1);
        drawBodyCracks(batch, m, b);
        // Avant le duel, ses rouleaux tournent dans le vide, au ralenti, puis s'arrêtent.
        float idle = roundsStarted == 0 ? 7f * (1f - Interpolation.pow2Out.apply(progress(0.3f, IDLE_STOP))) : 0f;
        drawWindow(batch, m, herStrip, offset(herAt, herStrip.length, HER_DOWN, HER_STOP, Interpolation.pow5Out, 3f) - idle,
            b, herLanded > 0 && herLanded == roundsStarted);
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

    /** Achevée : des fissures sur son corps doré, qui rougeoient. */
    private void drawBodyCracks(Batch batch, Mine m, float b) {
        float u = m.u, left = m.cx - m.w / 2f;
        float[][] cracks = {{0.12f, 0.95f, -60f, 9f}, {0.12f, 0.95f, -100f, 6f}, {0.85f, 0.3f, 70f, 8f},
            {0.9f, 0.85f, -120f, 7f}, {0.3f, 0.12f, 40f, 6f}};
        float hot = 0.5f + 0.3f * MathUtils.sin(time * 6f);
        for (float[] crack : cracks) {
            float x = left + crack[0] * m.w, y = m.base + crack[1] * m.h;
            float angle = crack[2], length = crack[3] * u;
            batch.setColor(OUT.r * b, OUT.g * b, OUT.b * b, 1f);
            batch.draw(pixel, x, y - 0.5f * u, 0f, 0.5f * u, length, u, 1f, 1f, angle);
            float ex = x + MathUtils.cosDeg(angle) * length, ey = y + MathUtils.sinDeg(angle) * length;
            batch.draw(pixel, ex, ey - 0.4f * u, 0f, 0.4f * u, length * 0.5f, 0.8f * u, 1f, 1f, angle + 35f);
            glow(batch, x + MathUtils.cosDeg(angle) * length / 2f, y + MathUtils.sinDeg(angle) * length / 2f,
                length * 1.2f, c("ff5a2a"), 0.25f * hot * b);
        }
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
        float stamp = progress(ARRIVE + 0.4f, ARRIVE + 0.7f);
        if (stamp > 0f) {
            font.getData().setScale(scale * MathUtils.lerp(4.2f, 2.6f, Interpolation.pow2Out.apply(stamp)));
            caption(batch, font, Lang.t("DERNIER TIRAGE"), width / 2f, height * 0.93f, stamp * (1f - tie) * (1f - gone));
        }
        if (tie > 0f) {
            font.getData().setScale(scale * MathUtils.lerp(3.6f, 2.6f, Interpolation.pow2Out.apply(tie)));
            caption(batch, font, Lang.t("ÉGALITÉ ! ON RELANCE"), width / 2f, height * 0.93f, tie);
        }
        font.getData().setScale(scale * 2f);
        float names = progress(ARRIVE + 0.8f, ARRIVE + 1.2f) * (1f - gone);
        caption(batch, font, Lang.t("TOI"), mine.cx, mine.base - 3.5f * mine.u, names);
        caption(batch, font, Lang.t("ELLE"), hers.cx, mine.base - 3.5f * mine.u, names);
        if (round >= 0) {                                    // les symboles et leurs rangs restent jusqu'au fondu
            font.getData().setScale(scale * 1.6f);
            if (t >= STOP) plate(batch, label(draw.mine.get(round)), mine.cx, mine.base - 8f * mine.u,
                progress(roundStart(round) + STOP, roundStart(round) + STOP + 0.2f));
            if (t >= HER_STOP) plate(batch, label(draw.hers.get(round)), hers.cx, mine.base - 8f * mine.u,
                progress(roundStart(round) + HER_STOP, roundStart(round) + HER_STOP + 0.2f));
            if (t >= HER_STOP + 0.3f) {                          // entre les deux machines : qui l'emporte
                int own = LastDrawEvent.score(draw.mine.get(round)), theirs = LastDrawEvent.score(draw.hers.get(round));
                float k = Interpolation.pow2Out.apply(progress(roundStart(round) + HER_STOP + 0.3f,
                    roundStart(round) + HER_STOP + 0.6f));
                font.getData().setScale(scale * MathUtils.lerp(5f, 3.4f, k));
                caption(batch, font, own + (own > theirs ? "  >  " : own < theirs ? "  <  " : "  =  ") + theirs,
                    width / 2f, mine.reelY + mine.reelH / 2f, k * (1f - gone));
            }
        }
        if (verdict) {
            font.getData().setScale(scale * MathUtils.lerp(4.6f, 3f, gone));
            caption(batch, font, draw.playerWins ? Lang.t("TU GAGNES !") : Lang.t("LA MACHINE GAGNE"), width / 2f, height * 0.93f, gone);
        }
        font.getData().setScale(scale);
    }

    /** Quand ton levier t'attend : « TIRE LE LEVIER ! » au-dessus de ta machine, et une flèche qui descend le long du levier. */
    private void drawHint(Batch batch, Mine m) {
        if (!awaitingPull() || dragging) return;
        float pulse = 0.8f + 0.2f * MathUtils.sin(time * 6f), u = m.u;
        float scale = font.getData().scaleX;
        font.getData().setScale(scale * (2.3f + 0.15f * MathUtils.sin(time * 6f)));
        caption(batch, font, Lang.t("TIRE LE LEVIER !"), m.cx, m.base + m.h + 15f * u, pulse);
        font.getData().setScale(scale);
        float x = m.cx - m.w / 2f - 12f * u, y = m.base + m.h * 0.95f - (time * 30f * u % (16f * u));
        for (int i = 0; i < 3; i++) {                                  // chevron vers le bas
            float w = (5f - i * 1.5f) * u;
            rect(batch, OUT, x - w / 2f - 0.5f * u, y - i * u - 0.5f * u, w + u, 2f * u, 1f);
        }
        for (int i = 0; i < 3; i++) {
            float w = (5f - i * 1.5f) * u;
            batch.setColor(GOLD.r, GOLD.g, GOLD.b, pulse);
            batch.draw(pixel, x - w / 2f, y - i * u, w, u);
        }
    }

    /** La bulle de la Machine, à gauche de son dôme, qui pointe vers son œil ; la réplique s'écrit lettre à lettre. */
    private void drawBubble(Batch batch, Mine hers) {
        if (lines == null) return;
        String text = lines[line];
        int shown = MathUtils.clamp((int) ((time - lineAt) * LETTERS), 0, text.length());
        float scale = font.getData().scaleX, u = worldHeight() / 80f;
        font.getData().setScale(scale * 1.9f);
        bubbleLayout.setText(font, text);
        float pad = 2.5f * u, w = bubbleLayout.width + 2f * pad, h = bubbleLayout.height + 2.4f * pad;
        float eyeY = hers.base + hers.h + 5f * hers.u;
        float right = hers.cx - 9f * hers.u - 4f * u, x = right - w, y = eyeY + 4f * u;
        float pop = Interpolation.swingOut.apply(progress(lineAt, lineAt + 0.18f));
        float alpha = Math.min(1f, pop * 1.5f);
        // La queue de la bulle, en marches, vers son œil.
        for (int i = 0; i < 4; i++) {
            float sx = right - 3f * u + i * 1.6f * u, sy = y - (i + 1) * 1.4f * u;
            box(batch, GOLD, sx - 0.4f * u, sy - 0.4f * u, 2.8f * u, 2.2f * u, alpha);
            box(batch, BUBBLE, sx, sy, 2f * u, 1.4f * u, alpha);
        }
        float cx = x + w / 2f, cy = y + h / 2f, sw = w * (0.85f + 0.15f * pop), sh = h * (0.85f + 0.15f * pop);
        batch.setColor(GOLD.r, GOLD.g, GOLD.b, alpha);
        batch.draw(pixel, cx - sw / 2f - 0.6f * u, cy - sh / 2f, sw + 1.2f * u, sh);
        batch.draw(pixel, cx - sw / 2f, cy - sh / 2f - 0.6f * u, sw, sh + 1.2f * u);
        batch.setColor(BUBBLE.r, BUBBLE.g, BUBBLE.b, 0.95f * alpha);
        batch.draw(pixel, cx - sw / 2f, cy - sh / 2f, sw, sh);
        if (shown > 0 && pop > 0.5f) {
            Color old = font.getColor().cpy();
            font.setColor(old.r, old.g, old.b, alpha);
            font.draw(batch, text.substring(0, shown), x + pad, y + h / 2f + bubbleLayout.height / 2f);
            font.setColor(old);
        }
        boolean waiting = shown == text.length() && !verdict;            // la suite : un petit triangle qui clignote
        if (waiting && (int) (time * 3f) % 2 == 0) {
            box(batch, GOLD, x + w - 2.5f * u, y + 1f * u, 1.4f * u, 0.6f * u, alpha);
            box(batch, GOLD, x + w - 2.2f * u, y + 0.5f * u, 0.8f * u, 0.5f * u, alpha);
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
        return Lang.f("{0}  -  RANG {1}", symbol.getDisplayName(), LastDrawEvent.score(symbol));
    }

    /** Un rectangle de couleur {@code color}, d'opacité {@code alpha}. */
    private void box(Batch batch, Color color, float x, float y, float w, float h, float alpha) {
        batch.setColor(color.r, color.g, color.b, alpha);
        batch.draw(pixel, x, y, w, h);
    }

    private void rect(Batch batch, Color color, float x, float y, float w, float h, float b) {
        batch.setColor(color.r * b, color.g * b, color.b * b, color.a);
        batch.draw(pixel, x, y, w, h);
    }
}
