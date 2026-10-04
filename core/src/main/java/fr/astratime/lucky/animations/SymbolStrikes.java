package fr.astratime.lucky.animations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.settings.VisualSettings;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Animation propre à chaque symbole tiré par le joueur, qui part de son
 * rouleau et montre ce que fait le symbole :
 * <ul>
 *   <li>attaque, vers l'ennemi : marteau (BAR), deux marteaux (DOUBLE BAR),
 *       cerise-bombe (CERISE), trois cerises-bombes (TRIPLE CERISE), 7 en
 *       comète (SEPT), éclair géant (TRIPLE SEPT) ;</li>
 *   <li>défense, vers le bouclier du joueur : grains de raisin (RAISIN),
 *       diamant (DIAMANT) ;</li>
 *   <li>gains, vers la pièce du panneau des gains : cloche qui sonne
 *       (CLOCHE), pastèque qui éclate (PASTÈQUE), lingot qui tombe
 *       (LINGOT).</li>
 *   <li>rouleaux de la boutique : fer à cheval lancé vers les gains (FER À
 *       CHEVAL), écu qui s'abat sur le bouclier (ÉCU), épée qui transperce
 *       l'ennemi (ÉPÉE), coeur qui bat puis rejoint le joueur (COEUR), dé qui
 *       rebondit jusqu'à l'ennemi (DÉ), étoile filante qui se divise vers
 *       l'ennemi, le bouclier et les gains (ÉTOILE), grosse bombe à mèche
 *       (BOMBE), couronne posée en sacre puis fontaine de pièces (COURONNE).</li>
 * </ul>
 * Chaque animation touche sa cible {@link #IMPACT_TIME} secondes après son
 * départ : le texte du symbole apparaît à cet instant (voir SlotView).
 *
 * Les positions sont celles du Stage : l'acteur doit être placé à l'origine.
 */
public class SymbolStrikes extends Actor implements Disposable {

    /** Temps entre le départ d'une animation et le moment où elle touche sa cible. */
    public static final float IMPACT_TIME = 0.45f;

    private static final float END_TIME  = 1.25f;
    private static final float FADE_TIME = 0.3f;

    // Marteau (BAR, DOUBLE BAR)
    private static final float HANDLE_LENGTH  = 200f;
    private static final float HANDLE_SCALE   = 4f;
    private static final float HAMMER_HEAD    = 0.85f;   // taille du symbole utilisé comme tête
    private static final float RAISED_ANGLE   = -70f;
    private static final float WINDUP_ANGLE   = -88f;
    private static final float STRIKE_ANGLE   = -15f;    // le manche penche encore un peu quand la tête frappe
    private static final float REBOUND_ANGLE  = -12f;
    private static final float SECOND_HAMMER  = 0.17f;   // le second marteau du DOUBLE BAR frappe après le premier

    // Projectiles (cerises, 7, diamant)
    private static final float BOMB_SCALE     = 0.7f;
    private static final float BOMB_ARC       = 260f;
    private static final float CHERRY_STAGGER = 0.1f;
    private static final float SEVEN_SCALE    = 0.75f;

    // Gains
    private static final float COIN_SIZE      = 44f;
    private static final float COIN_FLIGHT    = 0.4f;
    private static final float ABOVE_REEL     = 110f;    // la cloche, la pastèque et le lingot, au-dessus du rouleau

    private static final Color JUICE  = Color.valueOf("d81b3cff");
    private static final Color FIRE   = Color.valueOf("ff8a1fff");
    private static final Color FLAME  = Color.valueOf("ffd23fff");
    private static final Color VIOLET = Color.valueOf("9b4dd6ff");
    private static final Color CRYSTAL = Color.valueOf("7df9ffff");
    private static final Color GOLD   = Color.valueOf("ffd700ff");
    private static final Color STEEL  = Color.valueOf("cfe3f5ff");
    private static final Color BOLT   = Color.valueOf("a8e6ffff");
    private static final Color MELON  = Color.valueOf("ff4d6aff");
    private static final Color RIND   = Color.valueOf("2e9e3eff");
    private static final Color LUCK   = Color.valueOf("6ae07aff");
    private static final Color AZURE  = Color.valueOf("4aa8ffff");
    private static final Color LOVE   = Color.valueOf("ff5c8aff");
    private static final Color SMOKE  = Color.valueOf("5a5a66ff");

    // Rouleaux de la boutique
    private static final float SWORD_SCALE    = 0.8f;
    private static final float SWORD_OVERSHOOT = 140f;   // l'épée transperce et ressort derrière l'ennemi
    private static final float HEART_RISE     = 0.25f;   // le coeur bat au-dessus de son rouleau, puis part
    private static final float STAR_SPLIT     = 0.2f;    // l'étoile se divise en trois
    private static final float BIG_BOMB_ARC   = 360f;
    private static final float BIG_BOMB_SCALE = 1.05f;
    private static final float BOMB_FUSE_X    = 56f;     // bout de la mèche, depuis le centre de l'image de la bombe
    private static final float BOMB_FUSE_Y    = 52f;

    /** Une animation en cours. */
    private static final class Strike {
        /** Symbole tiré : il choisit l'animation, son image vole. */
        final Symbol symbol;
        final Symbol art;
        final float  fromX, fromY, toX, toY;
        /** Cibles en plus de la principale (l'Étoile : bouclier, puis pièce des gains). */
        final Vector2[] extra;
        final long   seed = MathUtils.random(Long.MAX_VALUE);
        final List<Flyer> flyers = new ArrayList<>();
        float time;  // négatif : départ différé

        Strike(Symbol symbol, Vector2 from, Vector2 to, float delay, Vector2[] extra) {
            this.symbol = symbol;
            this.art    = symbol;
            this.extra  = extra;
            fromX = from.x; fromY = from.y;
            toX   = to.x;   toY   = to.y;
            time  = -delay;
        }
    }

    /** Objet qui vole en cloche d'un point à un autre (grain, pièce, pépin). */
    private static final class Flyer {
        TextureRegion region, landedAs;  // landedAs : l'image prise à mi-course (pépin changé en pièce)
        float x0, y0, x1, y1, start, duration, arc, size, spin;
        Color tint = Color.WHITE;
        boolean arrived;
        Runnable onArrival;
    }

    /** Étincelle, goutte ou éclat, soumis à la gravité. */
    private static final class Particle {
        float x, y, vx, vy, gravity, age, life, size, rotation;
        final Color color = new Color();
        boolean glow, square, flash;
    }

    private final VisualSettings settings;
    private final ScreenShake    shake;
    private final Runnable       onCoinArrived;
    private final Map<Symbol, TextureRegion> symbolArt = new EnumMap<>(Symbol.class);
    private final List<Texture>  textures  = new ArrayList<>();
    private final TextureRegion  pixel, coin, dot, ring, handle, grain, seed;
    private final List<Strike>   strikes   = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private float flash;  // éclair du TRIPLE SEPT : l'écran blanchit un instant

    /**
     * @param pixel         région d'un pixel blanc
     * @param coinTexture   pièce d'or du panneau des gains
     * @param onCoinArrived appelé à l'arrivée de chaque pièce au panneau des gains
     */
    public SymbolStrikes(VisualSettings settings, ScreenShake shake, TextureRegion pixel, Texture coinTexture,
                         Runnable onCoinArrived) {
        this.settings      = settings;
        this.shake         = shake;
        this.pixel         = pixel;
        this.coin          = new TextureRegion(coinTexture);
        this.onCoinArrived = onCoinArrived;
        for (Symbol symbol : Symbol.values()) {
            symbolArt.put(symbol, region(cutOut(new Pixmap(Gdx.files.internal(symbol.getAssetPath())))));
        }
        dot    = region(dotPixmap());
        ring   = region(ringPixmap());
        handle = region(handlePixmap());
        grain  = region(grainPixmap());
        seed   = region(seedPixmap());
        setTouchable(Touchable.disabled);
    }

    /**
     * Lance l'animation de {@code symbol}, qui part de {@code from} (centre de
     * son rouleau) et touche {@code to} (sa cible) {@link #IMPACT_TIME}
     * secondes après {@code delay}.
     */
    public void play(Symbol symbol, Vector2 from, Vector2 to, float delay) {
        play(symbol, from, to, delay, new Vector2[0]);
    }

    /**
     * Comme {@link #play(Symbol, Vector2, Vector2, float)}, avec des cibles en
     * plus : l'Étoile vise aussi le bouclier du joueur puis la pièce des gains.
     */
    public void play(Symbol symbol, Vector2 from, Vector2 to, float delay, Vector2... extra) {
        Strike strike = new Strike(symbol, from, to, delay, extra);
        prepare(strike);
        strikes.add(strike);
    }

    /** Arrête toutes les animations (nouveau combat). */
    public void cancel() {
        strikes.clear();
        particles.clear();
        flash = 0f;
    }

    // -------------------------------------------------------------------------
    // Préparation : les objets qui volent vers la cible
    // -------------------------------------------------------------------------

    private void prepare(Strike s) {
        switch (s.symbol) {
            case GRAPE -> {
                for (int i = 0; i < 7; i++) {
                    float start = i * 0.035f;
                    Flyer f = flyer(s, grain, s.fromX + MathUtils.random(-30f, 30f), s.fromY + MathUtils.random(-10f, 25f),
                        s.toX + MathUtils.random(-12f, 12f), s.toY + MathUtils.random(-12f, 12f),
                        start, IMPACT_TIME - 0.12f + i * 0.04f - start, MathUtils.random(110f, 200f), 40f);
                    f.onArrival = () -> {
                        pop(f.x1, f.y1, VIOLET, 8);
                        flashAt(f.x1, f.y1, VIOLET, 90f);
                    };
                }
            }
            case DIAMOND -> {
                Flyer f = flyer(s, symbolArt.get(s.art), s.fromX, s.fromY, s.toX, s.toY,
                    0f, IMPACT_TIME, 140f, 80f);
                f.spin = 1f;
                f.onArrival = () -> crystalBurst(s.toX, s.toY);
            }
            case BELL -> {
                for (int i = 0; i < 6; i++) {
                    coinFlyer(s, s.fromX, s.fromY + ABOVE_REEL - 20f, 0.12f + i * 0.06f);
                }
            }
            case WATERMELON -> {
                for (int i = 0; i < 6; i++) {
                    Flyer f = coinFlyer(s, s.fromX + MathUtils.random(-20f, 20f), s.fromY + ABOVE_REEL,
                        0.25f + i * 0.04f);
                    f.landedAs = f.region;
                    f.region   = seed;
                    f.size     = 20f;
                }
            }
            case GOLD_BAR -> {
                for (int i = 0; i < 8; i++) {
                    coinFlyer(s, s.fromX + MathUtils.random(-40f, 40f), s.fromY + 60f, 0.28f + i * 0.04f);
                }
            }
            case CROWN -> {
                for (int i = 0; i < 12; i++) {
                    float angle = 30f + i * 10f;
                    coinFlyer(s, s.fromX + MathUtils.cosDeg(angle) * 30f, s.fromY + ABOVE_REEL + 10f,
                        0.3f + i * 0.03f);
                }
            }
            case HORSESHOE -> {
                for (int i = 0; i < 3; i++) {
                    coinFlyer(s, s.toX + MathUtils.random(-50f, 50f), s.toY + 120f, IMPACT_TIME + 0.02f + i * 0.06f)
                        .arc = 40f;
                }
            }
            default -> { }
        }
    }

    private Flyer coinFlyer(Strike s, float x, float y, float start) {
        Flyer f = flyer(s, coin, x, y, s.toX, s.toY, start, COIN_FLIGHT, MathUtils.random(120f, 220f), COIN_SIZE);
        f.spin = 1f;
        f.onArrival = onCoinArrived;
        return f;
    }

    private static Flyer flyer(Strike s, TextureRegion region, float x0, float y0, float x1, float y1,
                               float start, float duration, float arc, float size) {
        Flyer f = new Flyer();
        f.region = region;
        f.x0 = x0; f.y0 = y0; f.x1 = x1; f.y1 = y1;
        f.start = start; f.duration = duration; f.arc = arc; f.size = size;
        s.flyers.add(f);
        return f;
    }

    // -------------------------------------------------------------------------
    // Mise à jour
    // -------------------------------------------------------------------------

    @Override
    public void act(float delta) {
        super.act(delta);
        for (Iterator<Strike> it = strikes.iterator(); it.hasNext(); ) {
            Strike s = it.next();
            float before = s.time;
            s.time += delta;
            if (s.time < 0f) continue;
            update(s, Math.max(0f, before), s.time, delta);
            for (Flyer f : s.flyers) {
                if (!f.arrived && s.time >= f.start + f.duration) {
                    f.arrived = true;
                    if (f.onArrival != null) f.onArrival.run();
                }
            }
            if (s.time > END_TIME) it.remove();
        }
        for (Iterator<Particle> it = particles.iterator(); it.hasNext(); ) {
            Particle p = it.next();
            p.age += delta;
            if (p.age >= p.life) { it.remove(); continue; }
            p.vy -= p.gravity * delta;
            p.x  += p.vx * delta;
            p.y  += p.vy * delta;
            p.rotation += p.vx * delta;
        }
        flash = Math.max(0f, flash - delta * 2.5f);
    }

    /** Ce qui se passe entre {@code before} et {@code now} : impacts, traînées, étincelles. */
    private void update(Strike s, float before, float now, float delta) {
        switch (s.symbol) {
            case BAR -> {
                if (crossed(before, now, IMPACT_TIME)) hammerImpact(s, false);
            }
            case DOUBLE_BAR -> {
                if (crossed(before, now, IMPACT_TIME - SECOND_HAMMER * 0.5f)) hammerImpact(s, false);
                if (crossed(before, now, IMPACT_TIME + SECOND_HAMMER * 0.5f)) hammerImpact(s, true);
            }
            case CHERRY -> {
                if (now < IMPACT_TIME) fuseSparks(bombPosition(s, now, 0, 1), delta);
                if (crossed(before, now, IMPACT_TIME)) cherryBlast(s.toX, s.toY, 1f);
            }
            case TRIPLE_CHERRY -> {
                for (int i = 0; i < 3; i++) {
                    float impact = IMPACT_TIME + (i - 1) * CHERRY_STAGGER;
                    if (now < impact && now >= impact - IMPACT_TIME) fuseSparks(bombPosition(s, now, i, 3), delta);
                    if (crossed(before, now, impact)) {
                        Vector2 at = bombTarget(s, i, 3);
                        cherryBlast(at.x, at.y, 0.8f);
                    }
                }
            }
            case SEVEN -> {
                if (now < IMPACT_TIME) cometTrail(sevenPosition(s, now), delta);
                if (crossed(before, now, IMPACT_TIME)) {
                    flashAt(s.toX, s.toY, FIRE, 200f);
                    burst(s.toX, s.toY, 26, FIRE, 520f, 0.55f, 16f, true);
                    burst(s.toX, s.toY, 14, FLAME, 360f, 0.45f, 10f, true);
                    ringAt(s.toX, s.toY, FIRE, 1f);
                    shake.shake(0.18f, 6f);
                }
            }
            case TRIPLE_SEVEN -> {
                if (now > 0.15f && now < IMPACT_TIME) chargeSparks(s, delta);
                if (crossed(before, now, IMPACT_TIME)) {
                    if (!settings.isReducedEffects()) flash = 0.45f;
                    burst(s.toX, s.toY, 30, BOLT, 600f, 0.5f, 10f, true);
                    burst(s.toX, s.toY, 16, Color.WHITE, 420f, 0.4f, 8f, true);
                    ringAt(s.toX, s.toY, BOLT, 1.4f);
                    shake.shake(0.3f, 10f);
                }
            }
            case BELL -> {
                for (float t : new float[] {0.1f, 0.28f, 0.46f}) {
                    if (crossed(before, now, t)) {
                        ringAt(s.fromX, s.fromY + ABOVE_REEL, GOLD, 1.3f);
                        burst(s.fromX, s.fromY + ABOVE_REEL, 6, GOLD, 220f, 0.35f, 8f, true);
                    }
                }
            }
            case WATERMELON -> {
                if (crossed(before, now, 0.25f)) {
                    float y = s.fromY + ABOVE_REEL;
                    flashAt(s.fromX, y, MELON, 180f);
                    burst(s.fromX, y, 30, MELON, 460f, 0.75f, 22f, false);
                    burst(s.fromX, y, 14, RIND, 380f, 0.65f, 18f, false);
                    ringAt(s.fromX, y, MELON, 1.1f);
                    shake.shake(0.12f, 4f);
                }
            }
            case GOLD_BAR -> {
                if (crossed(before, now, 0.25f)) {
                    burst(s.fromX, s.fromY + 45f, 18, GOLD, 300f, 0.5f, 8f, true);
                    shake.shake(0.12f, 4f);
                }
                if (crossed(before, now, 0.4f)) ringAt(s.fromX, s.fromY + 60f, GOLD, 0.9f);
            }
            case HORSESHOE -> {
                if (now < IMPACT_TIME && MathUtils.random() < delta * 40f) {
                    Vector2 at = horseshoePosition(s, now);
                    Particle p = particle(at.x, at.y, MathUtils.randomBoolean() ? GOLD : LUCK, 40f, 0.4f, 7f, true);
                    p.gravity = 80f;
                }
                if (crossed(before, now, IMPACT_TIME)) {
                    flashAt(s.toX, s.toY, GOLD, 150f);
                    burst(s.toX, s.toY, 16, GOLD, 320f, 0.5f, 8f, true);
                    burst(s.toX, s.toY, 10, LUCK, 260f, 0.5f, 7f, true);
                    ringAt(s.toX, s.toY, LUCK, 0.9f);
                    onCoinArrived.run();
                }
            }
            case ECU -> {
                if (crossed(before, now, IMPACT_TIME)) {
                    flashAt(s.toX, s.toY, AZURE, 170f);
                    ringAt(s.toX, s.toY, AZURE, 1.1f);
                    ringAt(s.toX, s.toY, Color.WHITE, 0.6f);
                    burst(s.toX, s.toY, 16, STEEL, 360f, 0.5f, 9f, false);
                    shake.shake(0.15f, 5f);
                }
            }
            case SWORD -> {
                if (crossed(before, now, IMPACT_TIME)) {
                    flashAt(s.toX, s.toY, Color.WHITE, 190f);
                    burst(s.toX, s.toY, 20, STEEL, 520f, 0.45f, 9f, true);
                    burst(s.toX, s.toY, 12, JUICE, 380f, 0.6f, 12f, false);
                    shake.shake(0.18f, 7f);
                }
            }
            case HEART -> {
                if (now < IMPACT_TIME && MathUtils.random() < delta * 30f) {
                    Vector2 at = heartPosition(s, now);
                    Particle p = particle(at.x + MathUtils.random(-20f, 20f), at.y, LOVE, 30f, 0.5f, 9f, true);
                    p.gravity = -90f; // les étincelles d'amour montent
                }
                if (crossed(before, now, HEART_RISE * 0.35f) || crossed(before, now, HEART_RISE * 0.8f)) {
                    ringAt(s.fromX, s.fromY + ABOVE_REEL, LOVE, 0.6f); // battement
                }
                if (crossed(before, now, IMPACT_TIME)) {
                    flashAt(s.toX, s.toY, LOVE, 160f);
                    burst(s.toX, s.toY, 18, LOVE, 260f, 0.6f, 10f, true);
                    ringAt(s.toX, s.toY, LOVE, 0.9f);
                }
            }
            case DIE -> {
                for (float bounce : new float[] {IMPACT_TIME * 0.33f, IMPACT_TIME * 0.66f}) {
                    if (crossed(before, now, bounce)) {
                        Vector2 at = diePosition(s, bounce);
                        burst(at.x, at.y - 30f, 6, Color.WHITE, 160f, 0.3f, 6f, true);
                    }
                }
                if (crossed(before, now, IMPACT_TIME)) {
                    flashAt(s.toX, s.toY, Color.WHITE, 170f);
                    for (int i = 0; i < 14; i++) { // les points du dé s'éparpillent
                        Particle p = particle(s.toX, s.toY, Color.valueOf("1a0f0fff"), MathUtils.random(200f, 420f),
                            MathUtils.random(0.4f, 0.7f), MathUtils.random(8f, 12f), false);
                        p.square = true;
                        p.gravity = 900f;
                    }
                    burst(s.toX, s.toY, 16, Color.WHITE, 420f, 0.45f, 9f, true);
                    ringAt(s.toX, s.toY, Color.WHITE, 1f);
                    shake.shake(0.16f, 6f);
                }
            }
            case STAR -> {
                if (now < STAR_SPLIT) cometTrail(starPosition(s, now), delta * 0.5f);
                if (crossed(before, now, STAR_SPLIT)) {
                    Vector2 at = starPosition(s, STAR_SPLIT);
                    flashAt(at.x, at.y, FLAME, 140f);
                    burst(at.x, at.y, 12, FLAME, 260f, 0.4f, 7f, true);
                }
                if (now >= STAR_SPLIT && now < IMPACT_TIME) {
                    for (int i = 0; i < starTargets(s).length; i++) {
                        Vector2 at = starShard(s, now, i);
                        if (MathUtils.random() < delta * 30f) particle(at.x, at.y, starColor(i), 20f, 0.3f, 6f, true);
                    }
                }
                if (crossed(before, now, IMPACT_TIME)) {
                    Vector2[] targets = starTargets(s);
                    for (int i = 0; i < targets.length; i++) {
                        flashAt(targets[i].x, targets[i].y, starColor(i), 120f);
                        burst(targets[i].x, targets[i].y, 12, starColor(i), 300f, 0.45f, 8f, true);
                        ringAt(targets[i].x, targets[i].y, starColor(i), 0.7f);
                    }
                    if (targets.length > 2) onCoinArrived.run();
                    shake.shake(0.12f, 4f);
                }
            }
            case BOMB -> {
                if (now < IMPACT_TIME) {
                    Vector2 fuse = fuseTip(bigBombPosition(s, now), bombTilt(now));
                    fuseSparks(new Vector2(fuse.x, fuse.y - 34f), delta * 2.5f); // fuseSparks vise 34 px au-dessus
                }
                if (crossed(before, now, IMPACT_TIME)) {
                    if (!settings.isReducedEffects()) flash = 0.25f;
                    flashAt(s.toX, s.toY, FLAME, 300f);
                    burst(s.toX, s.toY, 40, FIRE, 680f, 0.7f, 22f, true);
                    burst(s.toX, s.toY, 24, FLAME, 480f, 0.5f, 14f, true);
                    burst(s.toX, s.toY, 16, STEEL, 520f, 0.8f, 10f, false);
                    for (int i = 0; i < 14; i++) { // fumée qui monte
                        Particle p = particle(s.toX + MathUtils.random(-50f, 50f), s.toY + MathUtils.random(-20f, 30f),
                            SMOKE, MathUtils.random(20f, 70f), MathUtils.random(0.8f, 1.2f), MathUtils.random(30f, 48f), false);
                        p.gravity = -140f;
                    }
                    ringAt(s.toX, s.toY, FIRE, 1.8f);
                    ringAt(s.toX, s.toY, Color.WHITE, 1.1f);
                    shake.shake(0.35f, 13f);
                }
                if (crossed(before, now, IMPACT_TIME + 0.08f)) { // le recul brûle le joueur
                    burst(s.fromX, s.fromY, 10, FIRE, 240f, 0.4f, 9f, true);
                    flashAt(s.fromX, s.fromY, FIRE, 120f);
                }
            }
            case CROWN -> {
                if (crossed(before, now, 0.22f)) {
                    float y = s.fromY + ABOVE_REEL;
                    flashAt(s.fromX, y, GOLD, 220f);
                    ringAt(s.fromX, y, GOLD, 1.3f);
                    burst(s.fromX, y, 22, GOLD, 360f, 0.6f, 9f, true);
                    shake.shake(0.12f, 4f);
                }
                if (now > 0.22f && now < 0.9f && MathUtils.random() < delta * 25f) {
                    float angle = MathUtils.random(360f);
                    flashAt(s.fromX + MathUtils.cosDeg(angle) * 60f, s.fromY + ABOVE_REEL + MathUtils.sinDeg(angle) * 40f,
                        Color.WHITE, 40f);
                }
            }
            default -> { }
        }
    }

    private static boolean crossed(float before, float now, float at) {
        return before < at && now >= at;
    }

    // -------------------------------------------------------------------------
    // Particules
    // -------------------------------------------------------------------------

    private void hammerImpact(Strike s, boolean second) {
        float x = s.toX + (s.symbol == Symbol.DOUBLE_BAR ? (second ? 18f : -18f) : 0f);
        flashAt(x, s.toY, Color.WHITE, 160f);
        burst(x, s.toY, 18, Color.WHITE, 450f, 0.4f, 9f, true);
        burst(x, s.toY, 12, STEEL, 380f, 0.6f, 10f, false);
        ringAt(x, s.toY, Color.WHITE, 1f);
        shake.shake(0.18f, second ? 8f : 6f);
    }

    private void cherryBlast(float x, float y, float strength) {
        flashAt(x, y, FLAME, 170f * strength);
        burst(x, y, (int) (34 * strength), JUICE, 520f * strength, 0.75f, 20f, false);
        burst(x, y, (int) (12 * strength), FLAME, 380f * strength, 0.35f, 10f, true);
        ringAt(x, y, JUICE, strength);
        shake.shake(0.15f, 5f * strength);
    }

    private void crystalBurst(float x, float y) {
        for (int i = 0; i < 16; i++) {
            Particle p = particle(x, y, CRYSTAL, MathUtils.random(160f, 420f), MathUtils.random(0.4f, 0.7f),
                MathUtils.random(7f, 13f), true);
            p.square = true;
            p.gravity = 400f;
        }
        ringAt(x, y, CRYSTAL, 0.9f);
        flashAt(x, y, CRYSTAL, 130f);
    }

    private void pop(float x, float y, Color color, int count) {
        burst(x, y, count, color, 200f, 0.35f, 7f, true);
    }

    private void fuseSparks(Vector2 bomb, float delta) {
        if (MathUtils.random() > delta * 50f) return;
        Particle p = particle(bomb.x + MathUtils.random(-4f, 4f), bomb.y + 34f, FLAME, MathUtils.random(40f, 120f),
            MathUtils.random(0.15f, 0.3f), MathUtils.random(4f, 7f), true);
        p.gravity = -60f;
    }

    private void cometTrail(Vector2 at, float delta) {
        int count = Math.max(1, Math.round(delta * 90f));
        for (int i = 0; i < count; i++) {
            Particle p = particle(at.x + MathUtils.random(-14f, 14f), at.y + MathUtils.random(-14f, 14f),
                MathUtils.randomBoolean() ? FIRE : FLAME, MathUtils.random(10f, 60f), MathUtils.random(0.25f, 0.45f),
                MathUtils.random(10f, 18f), true);
            p.gravity = -120f; // les flammes montent
        }
    }

    private void chargeSparks(Strike s, float delta) {
        if (MathUtils.random() > delta * 40f) return;
        float angle = MathUtils.random(360f), distance = MathUtils.random(60f, 110f);
        Particle p = particle(s.toX + MathUtils.cosDeg(angle) * distance, s.toY + MathUtils.sinDeg(angle) * distance,
            BOLT, 0f, 0.25f, 6f, true);
        p.vx = -MathUtils.cosDeg(angle) * distance * 4f;
        p.vy = -MathUtils.sinDeg(angle) * distance * 4f;
    }

    private void burst(float x, float y, int count, Color color, float speed, float life, float size, boolean glow) {
        if (settings.isReducedEffects()) count = Math.max(1, count / 2);
        for (int i = 0; i < count; i++) {
            Particle p = particle(x, y, color, MathUtils.random(speed * 0.3f, speed), MathUtils.random(life * 0.6f, life),
                MathUtils.random(size * 0.6f, size), glow);
            p.gravity = glow ? 300f : 1200f;
            p.vy += glow ? 0f : 220f; // le jus et les éclats jaillissent avant de retomber
        }
    }

    private Particle particle(float x, float y, Color color, float speed, float life, float size, boolean glow) {
        Particle p = new Particle();
        float angle = MathUtils.random(360f);
        p.x = x; p.y = y;
        p.vx = MathUtils.cosDeg(angle) * speed;
        p.vy = MathUtils.sinDeg(angle) * speed;
        p.life = life;
        p.size = size;
        p.color.set(color);
        p.glow = glow;
        particles.add(p);
        return p;
    }

    /** Éclair bref et immobile, au point d'impact. */
    private void flashAt(float x, float y, Color color, float size) {
        Particle p = particle(x, y, color, 0f, 0.2f, size, true);
        p.flash = true;
    }

    /** Onde de choc : un anneau qui s'élargit en s'effaçant (dessiné comme une particule). */
    private void ringAt(float x, float y, Color color, float strength) {
        Particle p = new Particle();
        p.x = x; p.y = y;
        p.life = 0.4f;
        p.size = -160f * strength; // taille négative : anneau, de diamètre final |size|
        p.color.set(color);
        p.glow = true;
        particles.add(p);
    }

    // -------------------------------------------------------------------------
    // Trajectoires
    // -------------------------------------------------------------------------

    /** Cible de la cerise-bombe {@code index} (sur {@code count}) : elles s'écartent un peu les unes des autres. */
    private static Vector2 bombTarget(Strike s, int index, int count) {
        if (count == 1) return new Vector2(s.toX, s.toY);
        return new Vector2(s.toX + (index - 1) * 38f, s.toY + (index == 1 ? -14f : 10f));
    }

    private static Vector2 bombPosition(Strike s, float time, int index, int count) {
        float impact = IMPACT_TIME + (count == 1 ? 0f : (index - 1) * CHERRY_STAGGER);
        float u = MathUtils.clamp((time - (impact - IMPACT_TIME)) / IMPACT_TIME, 0f, 1f);
        Vector2 target = bombTarget(s, index, count);
        float x = MathUtils.lerp(s.fromX + (index - (count - 1) / 2f) * 30f, target.x, u);
        float y = MathUtils.lerp(s.fromY, target.y, u) + BOMB_ARC * 4f * u * (1f - u);
        return new Vector2(x, y);
    }

    private static Vector2 sevenPosition(Strike s, float time) {
        float u = Interpolation.pow2In.apply(MathUtils.clamp(time / IMPACT_TIME, 0f, 1f));
        float x = MathUtils.lerp(s.fromX, s.toX, u);
        float y = MathUtils.lerp(s.fromY, s.toY, u) + 70f * 4f * u * (1f - u);
        return new Vector2(x, y);
    }

    /** Fer à cheval lancé en haute cloche vers la pièce des gains. */
    private static Vector2 horseshoePosition(Strike s, float time) {
        float u = MathUtils.clamp(time / IMPACT_TIME, 0f, 1f);
        float x = MathUtils.lerp(s.fromX, s.toX, Interpolation.pow2Out.apply(u));
        float y = MathUtils.lerp(s.fromY, s.toY, u) + 240f * 4f * u * (1f - u);
        return new Vector2(x, y);
    }

    /** Écu : il monte au-dessus de son rouleau, puis s'abat sur le bouclier du joueur. */
    private static Vector2 ecuPosition(Strike s, float time) {
        float riseEnd = 0.2f;
        float topX = s.fromX + (s.toX - s.fromX) * 0.5f, topY = Math.max(s.fromY, s.toY) + 170f;
        if (time < riseEnd) {
            float u = Interpolation.pow2Out.apply(time / riseEnd);
            return new Vector2(MathUtils.lerp(s.fromX, topX, u), MathUtils.lerp(s.fromY, topY, u));
        }
        float u = Interpolation.pow3In.apply(MathUtils.clamp((time - riseEnd) / (IMPACT_TIME - riseEnd), 0f, 1f));
        return new Vector2(MathUtils.lerp(topX, s.toX, u), MathUtils.lerp(topY, s.toY, u));
    }

    /** Épée : elle file droit sur l'ennemi, le transperce et ressort derrière lui. */
    private static Vector2 swordPosition(Strike s, float time) {
        float dx = s.toX - s.fromX, dy = s.toY - s.fromY;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        float u = time < IMPACT_TIME ? Interpolation.pow3In.apply(time / IMPACT_TIME)
            : 1f + (time - IMPACT_TIME) / 0.15f * SWORD_OVERSHOOT / Math.max(1f, length);
        return new Vector2(s.fromX + dx * u, s.fromY + dy * u);
    }

    /** Coeur : il bat au-dessus de son rouleau, puis flotte jusqu'au jeton du joueur. */
    private static Vector2 heartPosition(Strike s, float time) {
        float y0 = s.fromY + ABOVE_REEL;
        if (time < HEART_RISE) {
            float u = Interpolation.pow2Out.apply(Math.min(1f, time / 0.12f));
            return new Vector2(s.fromX, MathUtils.lerp(s.fromY, y0, u));
        }
        float u = Interpolation.sine.apply(MathUtils.clamp((time - HEART_RISE) / (IMPACT_TIME - HEART_RISE), 0f, 1f));
        float x = MathUtils.lerp(s.fromX, s.toX, u) + 30f * MathUtils.sin(u * MathUtils.PI2);
        return new Vector2(x, MathUtils.lerp(y0, s.toY, u));
    }

    /** Dé : il roule vers l'ennemi en trois bonds de plus en plus courts. */
    private static Vector2 diePosition(Strike s, float time) {
        float u = MathUtils.clamp(time / IMPACT_TIME, 0f, 1f);
        float x = MathUtils.lerp(s.fromX, s.toX, u);
        float base = MathUtils.lerp(s.fromY, s.toY, u);
        int hop = Math.min(2, (int) (u * 3f));
        float v = u * 3f - hop;
        float height = (hop == 0 ? 200f : hop == 1 ? 110f : 60f) * 4f * v * (1f - v);
        return new Vector2(x, base + height);
    }

    /** Étoile filante : elle monte de son rouleau avant de se diviser. */
    private static Vector2 starPosition(Strike s, float time) {
        float u = Interpolation.pow2Out.apply(MathUtils.clamp(time / STAR_SPLIT, 0f, 1f));
        return new Vector2(s.fromX + 40f * u, s.fromY + 150f * u);
    }

    /** @return les cibles de l'Étoile : l'ennemi, puis ses cibles en plus (bouclier, pièce des gains). */
    private static Vector2[] starTargets(Strike s) {
        Vector2[] targets = new Vector2[1 + s.extra.length];
        targets[0] = new Vector2(s.toX, s.toY);
        System.arraycopy(s.extra, 0, targets, 1, s.extra.length);
        return targets;
    }

    /** Éclat {@code index} de l'Étoile, entre la division et l'impact. */
    private static Vector2 starShard(Strike s, float time, int index) {
        Vector2 split = starPosition(s, STAR_SPLIT);
        Vector2 target = starTargets(s)[index];
        float u = Interpolation.pow2In.apply(MathUtils.clamp((time - STAR_SPLIT) / (IMPACT_TIME - STAR_SPLIT), 0f, 1f));
        float x = MathUtils.lerp(split.x, target.x, u);
        float y = MathUtils.lerp(split.y, target.y, u) + 90f * 4f * u * (1f - u);
        return new Vector2(x, y);
    }

    /** Couleur de l'éclat {@code index} de l'Étoile : attaque, bouclier, gains. */
    private static Color starColor(int index) {
        return index == 0 ? FIRE : index == 1 ? AZURE : GOLD;
    }

    /** Grosse bombe : elle se balance un peu en vol (degrés). */
    private static float bombTilt(float time) {
        return 12f * MathUtils.sin(time * 14f);
    }

    /** @return le bout de la mèche de la grosse bombe en {@code at}, penchée de {@code tilt} degrés. */
    private static Vector2 fuseTip(Vector2 at, float tilt) {
        return new Vector2(BOMB_FUSE_X * BIG_BOMB_SCALE, BOMB_FUSE_Y * BIG_BOMB_SCALE).rotateDeg(tilt).add(at);
    }

    /** Grosse bombe : lancée très haut, elle retombe lourdement sur l'ennemi. */
    private static Vector2 bigBombPosition(Strike s, float time) {
        float u = MathUtils.clamp(time / IMPACT_TIME, 0f, 1f);
        float x = MathUtils.lerp(s.fromX, s.toX, u);
        float y = MathUtils.lerp(s.fromY, s.toY, Interpolation.pow2In.apply(u)) + BIG_BOMB_ARC * 4f * u * (1f - u);
        return new Vector2(x, y);
    }

    // -------------------------------------------------------------------------
    // Dessin
    // -------------------------------------------------------------------------

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (strikes.isEmpty() && particles.isEmpty() && flash <= 0f) return;
        Color previous = batch.getColor().cpy();
        for (Strike s : strikes) {
            if (s.time < 0f) continue;
            float alpha = parentAlpha * MathUtils.clamp((END_TIME - s.time) / FADE_TIME, 0f, 1f);
            drawStrike(batch, s, alpha);
            for (Flyer f : s.flyers) drawFlyer(batch, s, f, parentAlpha);
        }
        drawParticles(batch, parentAlpha);
        if (flash > 0f && getStage() != null) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            batch.setColor(0.85f, 0.95f, 1f, flash * parentAlpha);
            batch.draw(pixel, 0f, 0f, getStage().getWidth(), getStage().getHeight());
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
        batch.setColor(previous);
    }

    private void drawStrike(Batch batch, Strike s, float alpha) {
        float t = s.time;
        switch (s.symbol) {
            case BAR -> drawHammer(batch, s, t, IMPACT_TIME, false, alpha);
            case DOUBLE_BAR -> {
                drawHammer(batch, s, t, IMPACT_TIME - SECOND_HAMMER * 0.5f, false, alpha);
                drawHammer(batch, s, t, IMPACT_TIME + SECOND_HAMMER * 0.5f, true, alpha);
            }
            case CHERRY -> drawBomb(batch, s, t, 0, 1, alpha);
            case TRIPLE_CHERRY -> {
                for (int i = 0; i < 3; i++) drawBomb(batch, s, t, i, 3, alpha);
            }
            case SEVEN -> {
                if (t < IMPACT_TIME) {
                    Vector2 at = sevenPosition(s, t);
                    float scale = SEVEN_SCALE * (1f + 0.4f * t / IMPACT_TIME);
                    float tilt = MathUtils.atan2(s.toY - s.fromY, s.toX - s.fromX) * MathUtils.radiansToDegrees - 90f;
                    glow(batch, at.x, at.y, 130f * scale, FIRE, alpha * 0.8f);
                    symbol(batch, s.art, at.x, at.y, scale, tilt * 0.25f, alpha);
                }
            }
            case TRIPLE_SEVEN -> drawLightning(batch, s, t, alpha);
            case BELL -> {
                float appear = Interpolation.swingOut.apply(Math.min(1f, t / 0.15f));
                float swing = 28f * (float) Math.exp(-2.2f * t) * MathUtils.sin(t * 16f);
                symbol(batch, s.art, s.fromX, s.fromY + ABOVE_REEL, 1.1f * appear, swing, alpha);
            }
            case WATERMELON -> {
                if (t < 0.25f) {
                    float appear = Interpolation.swingOut.apply(Math.min(1f, t / 0.12f));
                    float wobble = t > 0.12f ? MathUtils.sin(t * 90f) * 0.08f : 0f;
                    symbolScaled(batch, s.art, s.fromX, s.fromY + ABOVE_REEL,
                        1.05f * appear * (1f + wobble), 1.05f * appear * (1f - wobble), 0f, alpha);
                }
            }
            case GOLD_BAR -> {
                float u = Math.min(1f, t / 0.25f);
                float y = MathUtils.lerp(s.fromY + 420f, s.fromY + 60f, Interpolation.pow2In.apply(u));
                if (t > 0.25f) y += 18f * Math.abs(MathUtils.sin((t - 0.25f) * 12f)) * Math.max(0f, 1f - (t - 0.25f) * 4f);
                symbol(batch, s.art, s.fromX, y, 0.9f, 0f, alpha * Math.min(1f, t / 0.08f));
                if (t > 0.3f && t < 0.75f) shine(batch, s.fromX, y, (t - 0.3f) / 0.45f, alpha);
            }
            case HORSESHOE -> {
                if (t < IMPACT_TIME) {
                    Vector2 at = horseshoePosition(s, t);
                    glow(batch, at.x, at.y, 100f, GOLD, alpha * 0.6f);
                    symbol(batch, s.art, at.x, at.y, 0.7f, -720f * t / IMPACT_TIME, alpha);
                }
            }
            case ECU -> {
                if (t < IMPACT_TIME + 0.15f) {
                    Vector2 at = ecuPosition(s, Math.min(t, IMPACT_TIME));
                    float appear = Interpolation.swingOut.apply(Math.min(1f, t / 0.15f));
                    float squash = t > IMPACT_TIME ? 1f - 0.3f * MathUtils.sin((t - IMPACT_TIME) / 0.15f * MathUtils.PI) : 1f;
                    glow(batch, at.x, at.y, 140f, AZURE, alpha * 0.7f);
                    symbolScaled(batch, s.art, at.x, at.y, 0.9f * appear * (2f - squash), 0.9f * appear * squash, 0f,
                        alpha * (t > IMPACT_TIME ? 1f - (t - IMPACT_TIME) / 0.15f : 1f));
                }
            }
            case SWORD -> {
                if (t < IMPACT_TIME + 0.15f) {
                    Vector2 at = swordPosition(s, t);
                    float angle = MathUtils.atan2(s.toY - s.fromY, s.toX - s.fromX) * MathUtils.radiansToDegrees;
                    float fade = t > IMPACT_TIME ? 1f - (t - IMPACT_TIME) / 0.15f : Math.min(1f, t / 0.06f);
                    if (t > 0.1f) { // traînée d'acier derrière la lame
                        Vector2 back = swordPosition(s, Math.max(0f, t - 0.08f));
                        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                        segment(batch, back.x, back.y, at.x, at.y, 16f, STEEL, alpha * fade * 0.5f);
                        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                    }
                    symbol(batch, s.art, at.x, at.y, SWORD_SCALE, angle - 90f, alpha * fade); // la pointe (en haut de l'image) vise l'ennemi
                }
                if (t >= IMPACT_TIME && t < IMPACT_TIME + 0.35f) { // entaille en croix qui s'élargit
                    float since = (t - IMPACT_TIME) / 0.35f;
                    float len = 160f * Interpolation.pow2Out.apply(Math.min(1f, since * 2f));
                    float a = alpha * (1f - since);
                    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                    segment(batch, s.toX - len / 2f, s.toY + len / 2f, s.toX + len / 2f, s.toY - len / 2f, 10f, Color.WHITE, a);
                    segment(batch, s.toX - len / 2f, s.toY + len / 2f, s.toX + len / 2f, s.toY - len / 2f, 26f, STEEL, a * 0.4f);
                    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                }
            }
            case HEART -> {
                if (t < IMPACT_TIME) {
                    Vector2 at = heartPosition(s, t);
                    float beat = t < HEART_RISE ? 1f + 0.25f * Math.abs(MathUtils.sin(t / HEART_RISE * MathUtils.PI * 2f)) : 1f;
                    float shrink = t > HEART_RISE ? 1f - 0.4f * (t - HEART_RISE) / (IMPACT_TIME - HEART_RISE) : 1f;
                    glow(batch, at.x, at.y, 130f * beat, LOVE, alpha * 0.7f);
                    symbol(batch, s.art, at.x, at.y, 0.8f * beat * shrink, 0f, alpha);
                }
            }
            case DIE -> {
                if (t < IMPACT_TIME) {
                    Vector2 at = diePosition(s, t);
                    symbol(batch, s.art, at.x, at.y, 0.7f, -540f * t / IMPACT_TIME, alpha);
                }
            }
            case STAR -> {
                if (t < STAR_SPLIT) {
                    Vector2 at = starPosition(s, t);
                    glow(batch, at.x, at.y, 140f, FLAME, alpha * 0.8f);
                    symbol(batch, s.art, at.x, at.y, 0.8f, 360f * t, alpha);
                } else if (t < IMPACT_TIME) {
                    for (int i = 0; i < starTargets(s).length; i++) {
                        Vector2 at = starShard(s, t, i);
                        glow(batch, at.x, at.y, 80f, starColor(i), alpha * 0.8f);
                        symbol(batch, s.art, at.x, at.y, 0.45f, 540f * t, alpha);
                    }
                }
            }
            case BOMB -> {
                if (t < IMPACT_TIME) {
                    Vector2 at = bigBombPosition(s, t);
                    float swell = 1f + 0.12f * MathUtils.sin(t * 50f) * (t / IMPACT_TIME); // elle va exploser
                    float tilt = bombTilt(t);
                    Vector2 fuse = fuseTip(at, tilt);
                    glow(batch, fuse.x, fuse.y, 34f + 14f * MathUtils.sin(t * 70f), FLAME, alpha);
                    symbol(batch, s.art, at.x, at.y, BIG_BOMB_SCALE * swell, tilt, alpha);
                }
            }
            case CROWN -> {
                float y0 = s.fromY + ABOVE_REEL;
                float u = Math.min(1f, t / 0.22f);
                float y = MathUtils.lerp(y0 + 300f, y0, Interpolation.bounceOut.apply(u));
                glow(batch, s.fromX, y, 170f, GOLD, alpha * (0.4f + 0.3f * MathUtils.sin(t * 12f)));
                symbol(batch, s.art, s.fromX, y, 1.15f, 0f, alpha * Math.min(1f, t / 0.08f));
                if (t > 0.25f && t < 0.75f) shine(batch, s.fromX, y, (t - 0.25f) / 0.5f, alpha);
            }
            default -> { }
        }
    }

    /**
     * Marteau dont la tête est le symbole lui-même : il surgit levé au-dessus
     * de l'ennemi, s'arme puis s'abat sur lui à {@code impact}, rebondit.
     * {@code mirror} : il frappe depuis la gauche.
     */
    private void drawHammer(Batch batch, Strike s, float t, float impact, boolean mirror, float alpha) {
        float start  = impact - IMPACT_TIME;
        float local  = t - start;
        if (local < 0f) return;
        float appear = Interpolation.swingOut.apply(Math.min(1f, local / 0.15f));
        float angle;
        if (local < 0.25f) {
            angle = MathUtils.lerp(RAISED_ANGLE, WINDUP_ANGLE, Interpolation.pow2Out.apply(MathUtils.clamp((local - 0.1f) / 0.15f, 0f, 1f)));
        } else if (local < IMPACT_TIME) {
            angle = MathUtils.lerp(WINDUP_ANGLE, STRIKE_ANGLE, Interpolation.pow3In.apply((local - 0.25f) / (IMPACT_TIME - 0.25f)));
        } else {
            float since = local - IMPACT_TIME;
            angle = STRIKE_ANGLE + REBOUND_ANGLE * MathUtils.sin(Math.min(1f, since / 0.25f) * MathUtils.PI);
        }
        TextureRegion head = symbolArt.get(s.art);
        float headLength = head.getRegionWidth() * HAMMER_HEAD * 0.5f; // la tête est dressée : sa largeur devient sa hauteur
        float side   = mirror ? -1f : 1f;
        // Le manche part d'un pivot fixe, placé pour que la tête frappe le centre de l'ennemi.
        float hitX   = s.toX + (s.symbol == Symbol.DOUBLE_BAR ? side * 18f : 0f), hitY = s.toY + headLength * 0.6f;
        float pivotX = hitX + side * HANDLE_LENGTH * MathUtils.cosDeg(STRIKE_ANGLE);
        float pivotY = hitY + HANDLE_LENGTH * MathUtils.sinDeg(STRIKE_ANGLE);
        float rot    = side * angle;
        float cos = MathUtils.cosDeg(rot), sin = MathUtils.sinDeg(rot);
        float dirX = -side * cos, dirY = -side * sin; // du pivot vers la tête

        batch.setColor(1f, 1f, 1f, alpha);
        float hw = handle.getRegionWidth() * HANDLE_SCALE, hh = handle.getRegionHeight() * HANDLE_SCALE;
        batch.draw(handle, pivotX - hw, pivotY - hh / 2f, hw, hh / 2f, hw, hh, appear * side, appear, rot);
        float headX = pivotX + dirX * HANDLE_LENGTH * appear, headY = pivotY + dirY * HANDLE_LENGTH * appear;
        symbol(batch, s.art, headX, headY, HAMMER_HEAD * appear, rot + side * 90f, alpha); // texte lisible des deux côtés
        if (local >= IMPACT_TIME - 0.08f && local < IMPACT_TIME + 0.12f) {
            float streak = 1f - Math.abs(local - IMPACT_TIME) / 0.12f;
            glow(batch, headX, headY - headLength, 120f, Color.WHITE, alpha * streak);
        }
    }

    /** Cerise-bombe {@code index} (sur {@code count}) : lancée en cloche, elle tournoie, mèche allumée. */
    private void drawBomb(Batch batch, Strike s, float t, int index, int count, float alpha) {
        float impact = IMPACT_TIME + (count == 1 ? 0f : (index - 1) * CHERRY_STAGGER);
        if (t < impact - IMPACT_TIME || t >= impact) return;
        Vector2 at = bombPosition(s, t, index, count);
        float u = (t - (impact - IMPACT_TIME)) / IMPACT_TIME;
        float spin = (index % 2 == 0 ? -1f : 1f) * 300f * u;
        glow(batch, at.x, at.y + 34f, 26f + 10f * MathUtils.sin(t * 60f), FLAME, alpha);
        symbol(batch, s.art, at.x, at.y, BOMB_SCALE * (count == 1 ? 1f : 0.85f), spin, alpha);
    }

    /** TRIPLE SEPT : le symbole monte au-dessus de son rouleau et luit, puis un éclair tombe du ciel sur l'ennemi. */
    private void drawLightning(Batch batch, Strike s, float t, float alpha) {
        if (t < IMPACT_TIME + 0.1f) {
            float rise = Interpolation.pow2Out.apply(Math.min(1f, t / 0.3f));
            float pulse = 0.5f + 0.5f * MathUtils.sin(t * 40f);
            glow(batch, s.fromX, s.fromY + 120f * rise, 150f, BOLT, alpha * (0.4f + 0.4f * pulse));
            symbol(batch, s.art, s.fromX, s.fromY + 120f * rise, 0.85f + 0.15f * rise, 0f, alpha);
        }
        if (t < IMPACT_TIME || t > IMPACT_TIME + 0.4f || getStage() == null) return;
        float since = t - IMPACT_TIME;
        float strength = since < 0.25f ? 1f : 1f - (since - 0.25f) / 0.15f;
        if (((int) (since / 0.05f)) % 3 == 2) strength *= 0.35f; // l'éclair vacille
        long frame = s.seed + (long) (since / 0.06f);
        float topY = getStage().getHeight() + 20f;
        float x = s.toX + 60f, y = topY;
        int segments = 7;
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 1; i <= segments; i++) {
            float u = i / (float) segments;
            float nx = i == segments ? s.toX : MathUtils.lerp(s.toX + 60f, s.toX, u) + jitter(frame, i) * 32f;
            float ny = MathUtils.lerp(topY, s.toY, u);
            segment(batch, x, y, nx, ny, 22f, BOLT, alpha * strength * 0.5f);
            segment(batch, x, y, nx, ny, 7f, Color.WHITE, alpha * strength);
            x = nx; y = ny;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        glow(batch, s.toX, s.toY, 220f, BOLT, alpha * strength);
    }

    /** @return un décalage entre -1 et 1, fixe pour une image de l'éclair. */
    private static float jitter(long frame, int index) {
        long h = (frame * 31L + index) * 0x9E3779B97F4A7C15L;
        h ^= h >>> 29;
        return ((h & 0xffff) / 32767.5f) - 1f;
    }

    private void segment(Batch batch, float x0, float y0, float x1, float y1, float width, Color color, float alpha) {
        float dx = x1 - x0, dy = y1 - y0;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        batch.setColor(color.r, color.g, color.b, alpha);
        batch.draw(pixel, x0, y0 - width / 2f, 0f, width / 2f, length + width / 2f, width, 1f, 1f,
            MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees);
    }

    private void drawFlyer(Batch batch, Strike s, Flyer f, float parentAlpha) {
        float local = s.time - f.start;
        if (local < 0f || f.arrived) return;
        float u = Interpolation.pow2In.apply(MathUtils.clamp(local / f.duration, 0f, 1f));
        float x = MathUtils.lerp(f.x0, f.x1, u);
        float y = MathUtils.lerp(f.y0, f.y1, u) + f.arc * 4f * u * (1f - u);
        TextureRegion region = f.landedAs != null && u > 0.35f ? f.landedAs : f.region;
        float size = region == f.region ? f.size : COIN_SIZE;
        float width = region.getRegionWidth() >= region.getRegionHeight() ? size
            : size * region.getRegionWidth() / region.getRegionHeight();
        float height = width * region.getRegionHeight() / region.getRegionWidth();
        float squeeze = f.spin > 0f ? Math.abs(MathUtils.cos(local * 14f)) * 0.8f + 0.2f : 1f;
        float appear = Math.min(1f, local / 0.08f);
        if (s.symbol == Symbol.DIAMOND) glow(batch, x, y, 110f, CRYSTAL, parentAlpha * 0.7f);
        batch.setColor(f.tint.r, f.tint.g, f.tint.b, parentAlpha * appear);
        batch.draw(region, x - width / 2f, y - height / 2f, width / 2f, height / 2f, width, height, squeeze, 1f, 0f);
    }

    private void drawParticles(Batch batch, float parentAlpha) {
        for (Particle p : particles) {
            float fade = 1f - p.age / p.life;
            if (p.glow) batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            batch.setColor(p.color.r, p.color.g, p.color.b, fade * parentAlpha);
            if (p.size < 0f) {
                float d = -p.size * Interpolation.pow2Out.apply(p.age / p.life);
                batch.draw(ring, p.x - d / 2f, p.y - d / 2f, d, d);
            } else if (p.flash) {
                float size = p.size * (0.6f + 0.4f * fade);
                batch.draw(dot, p.x - size / 2f, p.y - size / 2f, size, size);
            } else if (p.square) {
                batch.draw(pixel, p.x - p.size / 2f, p.y - p.size / 2f, p.size / 2f, p.size / 2f,
                    p.size, p.size, 1f, 1f, 45f + p.rotation);
            } else {
                float size = p.size * (0.5f + 0.5f * fade);
                batch.draw(dot, p.x - size / 2f, p.y - size / 2f, size, size);
            }
            if (p.glow) batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    private void symbol(Batch batch, Symbol symbol, float x, float y, float scale, float rotation, float alpha) {
        symbolScaled(batch, symbol, x, y, scale, scale, rotation, alpha);
    }

    private void symbolScaled(Batch batch, Symbol symbol, float x, float y, float scaleX, float scaleY,
                              float rotation, float alpha) {
        TextureRegion region = symbolArt.get(symbol);
        float w = region.getRegionWidth(), h = region.getRegionHeight();
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(region, x - w / 2f, y - h / 2f, w / 2f, h / 2f, w, h, scaleX, scaleY, rotation);
    }

    /** Halo lumineux (additif) de diamètre {@code size}. */
    private void glow(Batch batch, float x, float y, float size, Color color, float alpha) {
        if (alpha <= 0f) return;
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(color.r, color.g, color.b, alpha * 0.55f);
        batch.draw(dot, x - size / 2f, y - size / 2f, size, size);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** Reflet qui traverse le lingot en diagonale ({@code progress} de 0 à 1). */
    private void shine(Batch batch, float x, float y, float progress, float alpha) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(1f, 1f, 0.85f, alpha * MathUtils.sin(progress * MathUtils.PI) * 0.8f);
        float sx = x - 60f + 120f * progress;
        batch.draw(pixel, sx - 6f, y - 40f, 6f, 40f, 12f, 80f, 1f, 1f, -25f);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    // -------------------------------------------------------------------------
    // Petites images en pixel art
    // -------------------------------------------------------------------------

    private TextureRegion region(Pixmap pixmap) {
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        textures.add(texture);
        return new TextureRegion(texture);
    }

    private static Pixmap pixmap(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        return pixmap;
    }

    /**
     * @return le dessin d'un symbole sans le fond clair de sa case (le fond
     *         touchant les bords devient transparent), recadré au plus juste ;
     *         {@code source} est libérée
     */
    private static Pixmap cutOut(Pixmap source) {
        int width = source.getWidth(), height = source.getHeight();
        int background = source.getPixel(0, 0);
        boolean[] clear = new boolean[width * height];
        java.util.ArrayDeque<Integer> queue = new java.util.ArrayDeque<>();
        for (int x = 0; x < width; x++) { queue.add(x); queue.add((height - 1) * width + x); }
        for (int y = 0; y < height; y++) { queue.add(y * width); queue.add(y * width + width - 1); }
        while (!queue.isEmpty()) {
            int i = queue.poll();
            if (clear[i] || source.getPixel(i % width, i / width) != background) continue;
            clear[i] = true;
            int x = i % width, y = i / width;
            if (x > 0) queue.add(i - 1);
            if (x < width - 1) queue.add(i + 1);
            if (y > 0) queue.add(i - width);
            if (y < height - 1) queue.add(i + width);
        }
        int minX = width, minY = height, maxX = -1, maxY = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (clear[y * width + x]) continue;
                minX = Math.min(minX, x); maxX = Math.max(maxX, x);
                minY = Math.min(minY, y); maxY = Math.max(maxY, y);
            }
        }
        if (maxX < 0) return source;
        Pixmap result = pixmap(maxX - minX + 1, maxY - minY + 1);
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                result.drawPixel(x - minX, y - minY, clear[y * width + x] ? 0 : source.getPixel(x, y));
            }
        }
        source.dispose();
        return result;
    }

    /** Disque blanc aux bords adoucis : particules et halos. */
    private static Pixmap dotPixmap() {
        int size = 32;
        Pixmap pixmap = pixmap(size, size);
        float r = size / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float d = (float) Math.hypot(x + 0.5f - r, y + 0.5f - r) / r;
                float a = MathUtils.clamp(1f - d * d, 0f, 1f);
                pixmap.setColor(1f, 1f, 1f, a);
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }

    /** Anneau blanc : ondes de choc. */
    private static Pixmap ringPixmap() {
        int size = 64;
        Pixmap pixmap = pixmap(size, size);
        float r = size / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float d = (float) Math.hypot(x + 0.5f - r, y + 0.5f - r);
                float a = MathUtils.clamp(1f - Math.abs(d - (r - 4f)) / 3.5f, 0f, 1f);
                pixmap.setColor(1f, 1f, 1f, a);
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }

    /** Manche de bois du marteau, cerclé de fer au bout (côté tête à gauche). */
    private static Pixmap handlePixmap() {
        int width = (int) (HANDLE_LENGTH / HANDLE_SCALE), height = 5;
        Pixmap pixmap = pixmap(width, height);
        pixmap.setColor(Color.valueOf("1a0f0fff"));
        pixmap.fill();
        pixmap.setColor(Color.valueOf("8b5a2bff"));
        pixmap.fillRectangle(0, 1, width - 1, 3);
        pixmap.setColor(Color.valueOf("b07a40ff"));
        pixmap.drawLine(0, 1, width - 2, 1);
        pixmap.setColor(Color.valueOf("3b2a20ff")); // poignée gainée
        pixmap.fillRectangle(width - 10, 1, 9, 3);
        pixmap.setColor(Color.valueOf("9aa4b0ff")); // frette de fer, sous la tête
        pixmap.fillRectangle(0, 1, 3, 3);
        return pixmap;
    }

    /** Grain de raisin : 9 x 9 pixels, reflet en haut à gauche. */
    private static Pixmap grainPixmap() {
        Pixmap pixmap = pixmap(9, 9);
        pixmap.setColor(Color.valueOf("1a0f0fff"));
        pixmap.fillCircle(4, 4, 4);
        pixmap.setColor(Color.valueOf("7b2fb5ff"));
        pixmap.fillCircle(4, 4, 3);
        pixmap.setColor(Color.valueOf("a15de0ff"));
        pixmap.fillRectangle(2, 2, 2, 2);
        pixmap.setColor(Color.valueOf("e6d6ffff"));
        pixmap.drawPixel(2, 2);
        return pixmap;
    }

    /** Pépin de pastèque : petite goutte noire. */
    private static Pixmap seedPixmap() {
        Pixmap pixmap = pixmap(4, 6);
        pixmap.setColor(Color.valueOf("1a0f0fff"));
        pixmap.fillRectangle(1, 0, 2, 6);
        pixmap.fillRectangle(0, 2, 4, 3);
        pixmap.setColor(Color.valueOf("5a4a3aff"));
        pixmap.drawPixel(1, 2);
        return pixmap;
    }

    @Override
    public void dispose() {
        for (Texture texture : textures) texture.dispose();
    }
}
