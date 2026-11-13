package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.assets.CardTextures;
import fr.astratime.lucky.assets.EnemyTextures;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.List;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les cinématiques du Croupier, boss secret, au sommet de la Tour des épreuves.
 * Chacune a son propre son (sounds/cutscene/croupier*.ogg), calé sur les instants
 * ci-dessous : si un instant change ici, le changer aussi dans tools/sounds/generate_sounds.py.
 * <ul>
 *   <li>{@link Scene#INTRO} : au sommet de la Tour, derrière la Machine Originelle éteinte, une
 *       porte se dessine en lignes d'or ; tu entres. Une salle de machines, une par lieu, toutes
 *       éteintes ; au fond, le Croupier bat les cartes. Il claque des doigts : les machines
 *       s'allument jusqu'à lui, il se révèle, son ombre à cornes grandit. Il te distribue tes
 *       propres cartes.</li>
 *   <li>{@link Scene#RIG} (phase 2) : ta machine tourne ; sous la table, sa main gantée tourne une
 *       clé d'or ; ton rouleau réellement truqué s'arrête sur le symbole imposé, enchaîné et cadenassé.</li>
 *   <li>{@link Scene#OPEN} (phase 3) : la lampe tombe sur le tapis, il jette sa vraie prochaine main
 *       face visible devant toi, son chapeau glisse, son ombre monte jusqu'au plafond.</li>
 *   <li>{@link Scene#ENDING} : la vraie fin. Il pose une main perdante, s'incline, te laisse son
 *       chapeau et le sabot ; les machines s'éteignent, le toit s'ouvre sur l'aube, il s'efface.
 *       Contrechamp : c'est toi, derrière la table, le chapeau sur la tête.</li>
 * </ul>
 * Toutes se rejouent dans le menu Cinématique (avec des cartes et un rouleau au hasard).
 */
public class CroupierCutscene extends Cutscene {

    /** Les quatre scènes du Croupier. */
    public enum Scene { INTRO, RIG, OPEN, ENDING }

    // ----- Entrée (son cutscene/croupier) ------------------------------------
    /** La porte se dessine en lignes d'or, puis s'ouvre ; tu marches vers elle. */
    public static final float TRACE_START = 0.3f, TRACE_END = 1.2f, DOOR_OPEN = 1.3f, DOOR_OPENED = 1.8f;
    public static final float WALK_START = 1.5f, WALK_END = 2.5f;
    /** Changement de plan : la salle des machines, la caméra la longe (un pas toutes les STEP_EVERY s). */
    public static final float HALL_CUT = 2.6f, PAN_END = 4.6f, STEP_START = 2.8f, STEP_EVERY = 0.38f;
    public static final int   STEPS = 5;
    /** Il bat les cartes (deux mélanges), puis la caméra recule. */
    public static final float RIFFLE_1 = 4.1f, RIFFLE_2 = 4.6f, RIFFLE_LENGTH = 0.35f;
    public static final float PULL_START = 5.0f, PULL_END = 5.45f;
    /** Il claque des doigts ; les machines s'allument, une toutes les LIGHT_STEP s, jusqu'à lui. */
    public static final float SNAP = 5.5f, LIGHT_START = 5.65f, LIGHT_STEP = 0.15f;
    /** Il se révèle (gilet pourpre, yeux rouges), son ombre à cornes grandit. */
    public static final float CHANGE = 6.6f, CHANGE_END = 7.0f, SHADOW_END = 7.4f;
    /** Gros plan sur le tapis : une carte posée toutes les DEAL_STEP s, puis retournée toutes les FLIP_STEP s. */
    public static final float DEAL_CUT = 7.6f, DEAL_START = 7.85f, DEAL_STEP = 0.28f, DEAL_FLIGHT = 0.25f;
    public static final float FLIP_START = 9.3f, FLIP_STEP = 0.1f, FLIP_LENGTH = 0.12f;
    /** Sa réplique, en deux temps, puis le fondu rouge. */
    public static final float LINE_1 = 9.75f, LINE_2 = 10.3f, INTRO_COVER = 10.9f;

    // ----- Phase 2 (son cutscene/croupier_rig) --------------------------------
    /** Éclair rouge sur ta machine, puis plan sous la table : sa main, la clé, trois crans. */
    public static final float RIG_FLASH = 0.9f, UNDER_CUT = 1.5f, HAND_IN = 1.55f, HAND_AT = 1.95f, KEY_IN = 2.0f;
    public static final float[] KEY_TURNS = {2.2f, 2.45f, 2.7f};
    /** Retour sur ta machine : ton rouleau s'arrête, la chaîne s'enroule, le cadenas claque ; les autres s'arrêtent. */
    public static final float BACK_CUT = 3.0f, RIG_STOP = 3.15f, CHAIN_START = 3.2f, CHAIN_END = 3.45f, LOCK = 3.5f;
    public static final float[] OTHERS_STOP = {3.75f, 3.9f};
    public static final float RIG_CAPTION = 3.6f, PHASE_COVER = 4.2f;

    // ----- Phase 3 (son cutscene/croupier_open) -------------------------------
    /** La lampe tombe sur le tapis, il jette ses cartes, elles retombent une toutes les LAND_STEP s. */
    public static final float LAMP_DROP = 0.45f, LIGHT_HIT = 0.85f, THROW = 1.2f, PEAK = 1.6f;
    public static final float LAND_START = 1.8f, LAND_STEP = 0.14f;
    /** Son chapeau glisse, ses yeux brillent, son ombre monte jusqu'au plafond. */
    public static final float GLARE = 3.0f, HAT_SLIDE_END = 3.3f, GLARE_END = 3.6f, OPEN_CAPTION = 3.25f;

    // ----- Vraie fin (son cutscene/croupier_ending) ---------------------------
    /** Il pose sa dernière main, perdante (une carte toutes les LAY_STEP s). */
    public static final float LAY_START = 0.4f, LAY_STEP = 0.16f, LAY_FLIGHT = 0.2f;
    /** Il ôte son chapeau, s'incline, le pose sur la table (TOC), puis glisse le sabot vers toi. */
    public static final float HAT_OFF = 1.25f, HAT_CHEST = 1.6f, BOW_DOWN = 2.1f, BOW_UP = 2.3f, BOW_END = 2.7f;
    public static final float HAT_DOWN = 2.8f, HAT_ON_TABLE = 3.2f, SHOE_START = 3.4f, SHOE_STOP = 4.0f;
    /** Les machines s'éteignent (une toutes les OFF_STEP s), le toit s'ouvre sur l'aube, il s'efface. */
    public static final float OFF_START = 4.3f, OFF_STEP = 0.22f, ROOF_START = 4.8f, ROOF_END = 6.0f;
    public static final float VANISH_START = 5.3f, VANISH_END = 6.2f;
    /** Contrechamp : toi, derrière la table, avec son chapeau. */
    public static final float REVERSE_CUT = 6.5f, ENDING_CAPTION = 7.1f, ENDING_COVER = 8.4f;

    private static final float COVER_LENGTH = 0.6f;
    /** La taille d'un pixel de ta machine (phase 2), en unités du décor. */
    private static final float MY_U = 1.45f;

    // ----- Couleurs ------------------------------------------------------------
    private static final Color OUT = hex("140a0a"), GOLD = hex("ffc93a"), GOLD_LIGHT = hex("ffe58a"), GOLD_DARK = hex("b8801f");
    private static final Color RED = hex("ff2a3a"), GLASS = hex("f4ecdc"), FELT = hex("1e6a3a"), FELT_DARK = hex("124a28");
    private static final Color FELT_LIGHT = hex("2e8a4a"), WOOD = hex("6a3418"), WOOD_LIGHT = hex("8a4a22"), WOOD_DARK = hex("3a1a0a");
    private static final Color WALL = hex("1c0c18"), WALL_DARK = hex("120610"), VELVET = hex("7a1020"), VELVET_DARK = hex("4a0814");
    private static final Color CARPET = hex("5a0e1a"), CARPET_DARK = hex("3a0812"), STONE = hex("3b3550"), STONE_DARK = hex("262238");
    private static final Color SKY_TOP = hex("05071a"), SKY_LOW = hex("2b1a4a"), DOOR_LIGHT = hex("ffe2a0");
    private static final Color DAWN_LOW = hex("ffcf8a"), DAWN_MID = hex("ff8a6a"), DAWN_TOP = hex("4a3a8a");
    private static final Color SILHOUETTE = hex("0a0610"), SHADE = hex("2a6a3a"), BULB = hex("fff6c8");
    private static final Color MY_BODY = hex("b81e3a"), MY_LIGHT = hex("e8485a"), MY_DARK = hex("7a0f22");

    /** Les machines de la salle, une par lieu : couleurs du corps (clair, normal, sombre) et symbole. */
    private static final Color[][] HALL_COLORS = {
        {hex("6ccf6a"), hex("3f9a4a"), hex("24602c")},   // la prairie
        {hex("ee7ac8"), hex("c04a9a"), hex("7a2462")},   // la Fête foraine
        {hex("5a9ae0"), hex("2a6ab0"), hex("173e6e")},   // le Port
        {hex("e8a452"), hex("b8742a"), hex("6e4214")},   // les Mines
        {hex("4cd0c8"), hex("1f9a9a"), hex("0f5a5e")},   // le Casino
        {hex("f4d070"), hex("d0a040"), hex("8a6420")}};  // le Désert
    private static final Symbol[] HALL_SYMBOLS = {Symbol.CHERRY, Symbol.TICKET, Symbol.ANCHOR, Symbol.NUGGET,
        Symbol.GOLD_CHIP, Symbol.FLAME};
    private static final float[] HALL_X = {0.075f, 0.19f, 0.305f, 0.695f, 0.81f, 0.925f};
    /** Ordre d'allumage, des bords vers lui (et d'extinction, de lui vers les bords). */
    private static final int[] LIGHT_ORDER = {0, 5, 1, 4, 2, 3};

    private static final String[] DEFAULT_CARDS = {"cards/light/1-P.png", "cards/light/13-H.png",
        "cards/light/12-C.png", "cards/light/11-D.png", "cards/light/10-P.png"};
    /** Sa dernière main, perdante : rien ne se suit, rien ne se ressemble. */
    private static final String[] LOSING_HAND = {"cards/light/2-C.png", "cards/light/7-H.png",
        "cards/light/4-P.png", "cards/light/9-D.png", "cards/light/3-C.png"};
    private static final Symbol[] DEFAULT_REELS = {Symbol.CHERRY, Symbol.BELL, Symbol.SEVEN, Symbol.BAR,
        Symbol.CHERRY, Symbol.BELL};
    private static final float CARD_RATIO = 190f / 270f;

    private final Scene scene;
    private final TextureRegion croupier, grand, bareHead, hat, back, silhouette, dealer, hands, glove, key, padlock, shoe;
    private final TextureRegion[] hallSymbols = new TextureRegion[HALL_SYMBOLS.length];
    private final TextureRegion[] losing = new TextureRegion[LOSING_HAND.length];
    private final BitmapFont font;
    private final Array<Particle> sparks = new Array<>(false, 256);
    private final Starfield stars = new Starfield(160, 0.35f);
    private final Matrix4 saved = new Matrix4(), view = new Matrix4();
    private final float[] reelOffsets = new float[3];

    /** Les cartes montrées : les tiennes (entrée) ou sa prochaine main (phase 3). */
    private TextureRegion[] hand;
    private boolean handIsHis;
    /** Tes rouleaux (phase 2), le rouleau truqué et la place du symbole imposé dans la bande. */
    private TextureRegion[] strip;
    private int riggedReel = 1, riggedIndex = 2;

    private float previous, bright = 1f, alpha = 1f;

    public CroupierCutscene(CutsceneKit kit, Scene scene) {
        super(kit.settings(), kit.shake(), kit.sound(switch (scene) {
            case INTRO -> "croupier";
            case RIG -> "croupier_rig";
            case OPEN -> "croupier_open";
            case ENDING -> "croupier_ending";
        }));
        this.scene = scene;
        croupier = kit.portrait(EnemyKind.CROUPIER);
        grand = kit.portrait(EnemyKind.GRAND_CROUPIER);
        int[][][] parts = EnemyTextures.grandCroupierParts();
        bareHead = pixels(parts[0]);
        hat = new TextureRegion(pixels(parts[1]), 4, 0, 26, 12);       // le haut-de-forme seul (contour compris)
        back = load("cards/light/BACK.png");
        for (int i = 0; i < HALL_SYMBOLS.length; i++) hallSymbols[i] = load(HALL_SYMBOLS[i].getAssetPath());
        for (int i = 0; i < LOSING_HAND.length; i++) losing[i] = load(LOSING_HAND[i]);
        silhouette = art("x", new Color[] {SILHOUETTE},
            "......xxx......",
            ".....xxxxx.....",
            ".....xxxxx.....",
            ".....xxxxx.....",
            ".....xxxxx.....",
            "......xxx......",
            "......xxx......",
            "...xxxxxxxxx...",
            "..xxxxxxxxxxx..",
            ".xxxxxxxxxxxxx.",
            ".xxxxxxxxxxxxx.",
            ".xxx.xxxxx.xxx.",
            ".xxx.xxxxx.xxx.",
            ".xxx.xxxxx.xxx.",
            ".xxx.xxxxx.xxx.",
            ".xx..xxxxx..xx.",
            ".xx..xxxxx..xx.",
            ".xx..xxxxx..xx.",
            "..x..xxxxx..x..",
            ".....xxxxx.....",
            ".....xxxxx.....",
            ".....xx.xx.....",
            ".....xx.xx.....",
            ".....xx.xx.....",
            ".....xx.xx.....",
            ".....xx.xx.....",
            ".....xx.xx.....",
            ".....xx.xx.....",
            ".....xx.xx.....",
            "....xxx.xxx....");
        dealer = art("x", new Color[] {SILHOUETTE},
            "........xxxxxx........",
            ".......xxxxxxxx.......",
            ".......xxxxxxxx.......",
            ".......xxxxxxxx.......",
            ".......xxxxxxxx.......",
            "........xxxxxx........",
            ".........xxxx.........",
            "....xxxxxxxxxxxxxx....",
            "...xxxxxxxxxxxxxxxx...",
            "..xxxxxxxxxxxxxxxxxx..",
            "..xxxxxxxxxxxxxxxxxx..",
            "..xxx.xxxxxxxxxx.xxx..",
            "..xxx.xxxxxxxxxx.xxx..",
            "..xxx.xxxxxxxxxx.xxx..",
            ".xxx..xxxxxxxxxx..xxx.",
            ".xxx..xxxxxxxxxx..xxx.",
            ".xxx..xxxxxxxxxx..xxx.",
            "xxxx..xxxxxxxxxx..xxxx",
            "xxxx..xxxxxxxxxx..xxxx");
        hands = art("x", new Color[] {SILHOUETTE},
            "xxxx..............xxxx",
            "xxxxx............xxxxx");
        glove = art("owsr", new Color[] {OUT, hex("f4efe4"), hex("c8c0b4"), hex("5a0e1a")},
            "...oooooooo.........",
            "..owwwwwwwwooooooooo",
            ".owwwwwwwwwsrrrrrrrr",
            "owwwwwwwwwwsrrrrrrrr",
            "owwwwwwwwwwsrrrrrrrr",
            ".owwwwwwwwwsrrrrrrrr",
            "..owwwwwwwsooooooooo",
            "...oooooooo.........");
        key = art("ydo", new Color[] {GOLD, GOLD_DARK, OUT},          // vue de face : l'anneau, la tige dans la serrure
            "..ooooo..",
            ".oyyyyyo.",
            "oyyoooyyo",
            "oyo...oyo",
            "oyyoooyyo",
            ".oyyyyyo.",
            "..oyydo..",
            "...oydo..",
            "...oydo..",
            "..oyyddo.",
            "..oooooo.");
        padlock = art("ydo", new Color[] {GOLD, GOLD_DARK, OUT},
            "..ooooo..",
            ".oyyyyyo.",
            ".oyo.oyo.",
            ".oyo.oyo.",
            "ooooooooo",
            "oyyyyyyyo",
            "oyyyoyyyo",
            "oyyyoyyyo",
            "oyyyyyyyo",
            "odddddddo",
            "ooooooooo");
        shoe = art("owbc", new Color[] {OUT, WOOD_LIGHT, WOOD, hex("f4ecdc")},
            "...ooooooooooooooo",
            "..owwwwwwwwwwwwwwo",
            ".obbbbbbbbbbbbbbwo",
            "obccccccccccccccbo",
            "obbbbbbbbbbbbbbbbo",
            "obbbbbbbbbbbbbbbbo",
            "oooooooooooooooooo");
        font = kit.font();
        withCards(null, false);
        withMachine(null, -1, null);
    }

    /**
     * Les cartes de la scène : les tiennes (entrée, les cinq premières) ou sa prochaine main
     * (phase 3, {@code his} : dessinées sombres, comme ses cartes au combat). {@code null} : des cartes par défaut.
     */
    public CroupierCutscene withCards(List<String> paths, boolean his) {
        List<String> chosen = new ArrayList<>();
        if (paths != null) for (String path : paths) if (path != null) chosen.add(CardTextures.displayPath(path));
        if (chosen.isEmpty()) chosen.addAll(List.of(DEFAULT_CARDS));
        int count = Math.min(scene == Scene.INTRO ? 5 : 8, chosen.size());
        hand = new TextureRegion[count];
        for (int i = 0; i < count; i++) hand[i] = load(chosen.get(i));
        handIsHis = his;
        return this;
    }

    /**
     * Ta machine (phase 2) : tes rouleaux {@code reels}, le rouleau truqué {@code rigged} (0 à 2) et le
     * symbole imposé {@code symbol}. {@code null} ou hors bornes : des valeurs par défaut.
     */
    public CroupierCutscene withMachine(List<Symbol> reels, int rigged, Symbol symbol) {
        List<Symbol> list = new ArrayList<>(reels == null || reels.isEmpty() ? List.of(DEFAULT_REELS) : reels);
        if (symbol == null) symbol = Symbol.SEVEN;
        if (!list.contains(symbol)) list.add(symbol);
        riggedIndex = list.indexOf(symbol);
        riggedReel = rigged >= 0 && rigged < 3 ? rigged : 1;
        strip = new TextureRegion[list.size()];
        for (int i = 0; i < list.size(); i++) strip[i] = load(list.get(i).getAssetPath());
        return this;
    }

    /** @return l'instant où la scène se couvre (fondu final). */
    private float length() {
        return switch (scene) {
            case INTRO -> INTRO_COVER;
            case RIG, OPEN -> PHASE_COVER;
            case ENDING -> ENDING_COVER;
        };
    }

    @Override protected float coverStart() { return length(); }
    @Override protected float coverFull()  { return length() + COVER_LENGTH; }
    @Override protected Color coverColor() { return scene == Scene.ENDING ? Color.WHITE : hex("1a0204"); }

    @Override
    protected void reset() {
        sparks.clear();
        previous = 0f;
        for (int i = 0; i < reelOffsets.length; i++) reelOffsets[i] = i * 1.7f;
    }

    /** @return {@code true} à l'image où le temps passe {@code moment}. */
    private boolean at(float moment) { return previous < moment && time >= moment; }

    @Override
    protected void simulate(float delta) {
        float w = worldWidth(), h = worldHeight(), u = unit();
        switch (scene) {
            case INTRO -> {
                if (at(DOOR_OPEN)) burst(w * 0.72f, h * 0.22f + 11f * u, DOOR_LIGHT, 30, 380f, 0.9f);
                if (at(SNAP)) {
                    rumble(0.1f, 3f);
                    burst(w * 0.58f, h * 0.45f, GOLD, 16, 260f, 0.6f);
                }
                for (int k = 0; k < LIGHT_ORDER.length; k++) {
                    if (at(LIGHT_START + k * LIGHT_STEP)) burst(HALL_X[LIGHT_ORDER[k]] * w, h * 0.55f, GOLD, 10, 220f, 0.6f);
                }
                if (at(CHANGE)) {
                    rumble(0.45f, 9f);
                    burst(w / 2f, h * 0.6f, RED, 60, 650f, 1.0f);
                }
                for (int i = 0; i < hand.length; i++) if (at(DEAL_START + i * DEAL_STEP)) rumble(0.06f, 2.5f);
            }
            case RIG -> {
                if (at(RIG_FLASH)) rumble(0.15f, 4f);
                for (float turn : KEY_TURNS) {
                    if (at(turn)) {
                        rumble(0.08f, 3f);
                        burst(w * 0.5f, h * 0.5f, GOLD, 8, 200f, 0.4f);
                    }
                }
                if (at(RIG_STOP)) rumble(0.15f, 5f);
                if (at(LOCK)) {
                    rumble(0.35f, 9f);
                    burst(reelCenterX(riggedReel), h * 0.47f, GOLD, 45, 520f, 0.9f);
                }
                for (int i = 0; i < 3; i++) {                                 // les rouleaux tournent
                    if (time < stopTime(i)) reelOffsets[i] += MathUtils.lerp(4f, 16f, progress(0f, 0.4f)) * delta;
                }
            }
            case OPEN -> {
                if (at(LIGHT_HIT)) rumble(0.25f, 6f);
                for (int i = 0; i < hand.length; i++) {
                    if (at(landTime(i))) {
                        rumble(0.05f, 2f);
                        burst(fanX(i, w), fanY(i, h), GOLD, 8, 180f, 0.5f);
                    }
                }
                if (at(GLARE)) rumble(0.5f, 8f);
            }
            case ENDING -> {
                if (at(HAT_ON_TABLE)) rumble(0.08f, 2f);
                if (at(SHOE_STOP)) rumble(0.06f, 2f);
                if (time > VANISH_START && time < VANISH_END && random.nextFloat() < 0.6f) {
                    Particle mote = particle(w / 2f + (random.nextFloat() - 0.5f) * 14f * u, h * 0.42f + random.nextFloat() * 22f * u,
                        80f + 20f * random.nextFloat(), 60f + 80f * random.nextFloat(), 1.2f, 6f);
                    mote.color.set(GOLD_LIGHT);
                    mote.drag = 0f;
                    sparks.add(mote);
                }
            }
        }
        update(sparks, delta);
        previous = time;
    }

    private void burst(float x, float y, Color color, int count, float speed, float life) {
        for (int k = 0; k < count; k++) {
            Particle spark = particle(x, y, 360f * random.nextFloat(), speed * random.nextFloat(), life, 9f);
            spark.color.set(color);
            spark.drag = 0.2f;
            sparks.add(spark);
        }
    }

    @Override
    protected void drawScene(Batch batch) {
        float w = worldWidth(), h = worldHeight();
        bright = 1f;
        alpha = 1f;
        switch (scene) {
            case INTRO -> drawIntro(batch, w, h);
            case RIG -> drawRig(batch, w, h);
            case OPEN -> drawOpen(batch, w, h);
            case ENDING -> drawEnding(batch, w, h);
        }
        bright = 1f;
        alpha = 1f;
        drawParticles(batch, sparks);
        drawCaptions(batch, w, h);
        fadeFromBlack(batch, 0.4f);
    }

    /** @return la taille d'un « pixel » du décor à l'écran. */
    private float unit() { return Math.min(worldHeight() / 64f, worldWidth() / 110f); }

    // =========================================================================
    // Entrée
    // =========================================================================

    private void drawIntro(Batch batch, float w, float h) {
        if (time < HALL_CUT) drawTowerTop(batch, w, h);
        else if (time < DEAL_CUT) {
            float pan = Interpolation.sine.apply(progress(HALL_CUT, PAN_END));
            float pull = Interpolation.pow2.apply(progress(PULL_START, PULL_END));
            float zoom = MathUtils.lerp(1.8f, 1f, pull);
            float cx = MathUtils.lerp(MathUtils.lerp(w * 0.02f, w * 0.5f, pan), w * 0.5f, pull);
            float cy = MathUtils.lerp(h * 0.5f, h * 0.5f, pull);
            float[] lit = new float[HALL_X.length];
            for (int k = 0; k < LIGHT_ORDER.length; k++) {
                float on = LIGHT_START + k * LIGHT_STEP;
                lit[LIGHT_ORDER[k]] = progress(on, on + 0.08f);
            }
            float change = progress(CHANGE, CHANGE_END);
            drawHallBackdrop(batch, w, h, 0f, 0f);
            camera(batch, cx, cy, zoom);
            float s = croupierScale(h);
            drawHall(batch, w, h, lit, 0f, () -> {
                glow(batch, w / 2f, croupierBottom(h) + 26f * s, 110f * s, RED, 0.55f * change);
                drawHornedShadow(batch, w / 2f, croupierBottom(h), s, Interpolation.pow2Out.apply(progress(CHANGE, SHADOW_END)),
                    0.85f * change);
            });
            portrait(batch, croupier, w / 2f, croupierBottom(h), s, 0f, 1f - change);
            portrait(batch, grand, w / 2f, croupierBottom(h), s, 0f, change);
            if (change > 0f) drawEyes(batch, w / 2f, croupierBottom(h), s, change);
            drawHallTable(batch, w, h);
            drawShuffle(batch, w, h);
            if (time >= SNAP) glow(batch, w / 2f + 10f * s, croupierBottom(h) + 9f * s, 60f * s, GOLD, 0.9f * (1f - progress(SNAP, SNAP + 0.4f)));
            endCamera(batch);
            if (time < PULL_START) drawWalker(batch, w, h);                   // toi, de dos, qui longes les machines
            flash(batch, Color.WHITE, SNAP, 0.12f, 0.25f);
            flash(batch, RED, CHANGE, 0.3f, 0.45f);
            fadeThroughBlack(batch, HALL_CUT, 0.2f);
        } else drawDeal(batch, w, h);
        fadeThroughBlack(batch, DEAL_CUT, 0.15f);
    }

    /** Le sommet de la Tour, la nuit : la Machine Originelle éteinte, une porte d'or se dessine derrière elle. */
    private void drawTowerTop(Batch batch, float w, float h) {
        float u = unit();
        gradient(batch, SKY_LOW, SKY_TOP);
        stars.draw(batch, 1f);
        float moonX = w * 0.86f, moonY = h * 0.82f;                            // la lune
        glow(batch, moonX, moonY, 40f * u, hex("c8d8ff"), 0.35f);
        for (int y = -4; y <= 4; y++) {
            float half = (float) Math.sqrt(16.5f - y * y);
            rect(batch, hex("e8eef8"), moonX - half * u, moonY + y * u, 2f * half * u, u);
        }
        rect(batch, hex("c8d0e0"), moonX - u, moonY + u, 2f * u, u);
        rect(batch, hex("c8d0e0"), moonX + 1.5f * u, moonY - 2f * u, u, u);
        float top = h * 0.22f;
        drawBattlements(batch, w, top, u);
        drawDarkMachine(batch, w * 0.34f, top, u);
        drawDoor(batch, w * 0.72f, top, u);
        // Toi, de dos, qui marches vers la porte et entres dans la lumière.
        float walk = progress(WALK_START, WALK_END);
        if (walk > 0f && walk < 1f) {
            float k = Interpolation.sine.apply(walk);
            float x = MathUtils.lerp(w * 0.62f, w * 0.72f, k), y = MathUtils.lerp(-h * 0.08f, top + 0.5f * u, k);
            float scale = MathUtils.lerp(0.95f, 0.3f, k) * u;
            float bob = Math.abs(MathUtils.sin(time * 9f)) * 0.6f * scale;
            batch.setColor(1f, 1f, 1f, 1f - progress(WALK_END - 0.2f, WALK_END));
            batch.draw(silhouette, x - silhouette.getRegionWidth() * scale / 2f, y + bob,
                silhouette.getRegionWidth() * scale, silhouette.getRegionHeight() * scale);
        }
    }

    /** Les créneaux du sommet de la Tour, sur toute la largeur. */
    private void drawBattlements(Batch batch, float w, float top, float u) {
        rect(batch, STONE_DARK, -MARGIN, -MARGIN, w + 2f * MARGIN, top + MARGIN);
        for (int row = 0; row * 5f * u < top + MARGIN; row++) {
            float y = top - (row + 1) * 5f * u;
            for (float x = -MARGIN + (row % 2) * 4.2f * u - 8.4f * u; x < w + MARGIN; x += 8.4f * u) {
                rect(batch, STONE, x + 0.5f * u, y + 0.5f * u, 7.4f * u, 4f * u);
            }
        }
        for (float x = -MARGIN; x < w + MARGIN; x += 12f * u) {
            rect(batch, OUT, x - 0.5f * u, top - u, 8f * u, 6f * u);
            rect(batch, STONE_DARK, x, top - u, 7f * u, 5f * u);
            rect(batch, STONE, x + u, top, 5f * u, 3f * u);
        }
    }

    /** La Machine Originelle, éteinte depuis ta victoire : silhouette dorée assombrie, cinq paupières closes. */
    private void drawDarkMachine(Batch batch, float cx, float base, float u) {
        float U = 0.6f * u, w = 72f * U, h = 44f * U, left = cx - w / 2f, bottom = base + 4f * U;
        bright = 0.45f;
        float pivotX = left + w + 2f * U, pivotY = bottom + h * 0.55f;        // le levier, baissé pour de bon
        batch.setColor(OUT);
        batch.draw(pixel, pivotX + U, pivotY - 1.5f * U, 0f, 1.5f * U, 24f * U, 3f * U, 1f, 1f, -70f);
        rect(batch, OUT, left - U, bottom - U, w + 2f * U, h + 2f * U);
        rect(batch, GOLD, left, bottom, w, h);
        rect(batch, GOLD_LIGHT, left, bottom + h - 3f * U, w, 2f * U);
        rect(batch, GOLD_DARK, left + w - 3f * U, bottom, 3f * U, h);
        for (int step = 0; step < 4; step++) {
            float sw = w - (step + 1) * 14f * U, sy = bottom + h + step * 4f * U;
            rect(batch, OUT, cx - sw / 2f - U, sy, sw + 2f * U, 5f * U);
            rect(batch, step % 2 == 0 ? GOLD : GOLD_LIGHT, cx - sw / 2f, sy, sw, 4f * U);
        }
        float reelW = 11f * U, reelH = 14f * U, gap = 2f * U, panelW = 5 * reelW + 6 * gap, reelY = bottom + h * 0.45f;
        rect(batch, OUT, cx - panelW / 2f - U, reelY - gap - U, panelW + 2f * U, reelH + 2f * gap + 2f * U);
        rect(batch, GOLD_DARK, cx - panelW / 2f, reelY - gap, panelW, reelH + 2f * gap);
        for (int i = 0; i < 5; i++) {
            float x = cx - panelW / 2f + gap + i * (reelW + gap);
            rect(batch, OUT, x, reelY, reelW, reelH);
            rect(batch, GOLD_DARK, x + 0.5f * U, reelY + 0.5f * U, reelW - U, reelH - U);   // paupière close
            rect(batch, OUT, x + 0.5f * U, reelY + reelH * 0.45f, reelW - U, 0.8f * U);
        }
        for (int i = 0; i < 18; i++) {                                        // ses ampoules, éteintes
            float x = left + (i + 0.5f) * w / 18f;
            for (float y : new float[] {bottom + h - 1.5f * U, bottom + 1.5f * U}) {
                rect(batch, OUT, x - U, y - U, 2f * U, 2f * U);
                rect(batch, GOLD_DARK, x - 0.5f * U, y - 0.5f * U, U, U);
            }
        }
        float trayW = 40f * U, trayY = bottom + 4f * U;                        // le plateau à pièces
        rect(batch, OUT, cx - trayW / 2f, trayY, trayW, 7f * U);
        rect(batch, hex("2a1a10"), cx - trayW / 2f + U, trayY + U, trayW - 2f * U, 5f * U);
        float gemY = bottom + h + 20f * U;                                     // la pierre du sommet, qui luit à peine
        rect(batch, OUT, cx - 2.5f * U, gemY - 2.5f * U, 5f * U, 5f * U);
        rect(batch, GLASS, cx - 1.5f * U, gemY - 1.5f * U, 3f * U, 3f * U);
        bright = 1f;
        glow(batch, cx, gemY, 16f * U, hex("c8d8ff"), 0.25f + 0.1f * MathUtils.sin(time * 3f));
        glow(batch, cx, bottom + h / 2f, 90f * U, hex("6a4aff"), 0.12f);
    }

    /** La porte cachée : tracée en lignes d'or, puis ouverte sur la lumière de la salle. */
    private void drawDoor(Batch batch, float cx, float base, float u) {
        float dw = 12f * u, dh = 22f * u, left = cx - dw / 2f, bottom = base;
        float open = Interpolation.pow2Out.apply(progress(DOOR_OPEN, DOOR_OPENED));
        if (open > 0f) {                                                      // la lumière de l'autre côté
            rect(batch, DOOR_LIGHT, left, bottom, dw, dh);
            glow(batch, cx, bottom + dh / 2f, 70f * u, DOOR_LIGHT, 0.55f * open);
            glow(batch, cx, bottom, 60f * u, GOLD, 0.35f * open);
            float leaf = dw * (1f - open);                                    // le battant s'efface
            rect(batch, WOOD_DARK, left, bottom, leaf, dh);
            if (leaf > u) rect(batch, GOLD_DARK, left + leaf - u, bottom, u, dh);
        }
        float trace = progress(TRACE_START, TRACE_END);                       // les trois côtés, dans l'ordre
        if (trace <= 0f) return;
        float t = 0.6f * u, total = 2f * dh + dw, done = trace * total;
        float side1 = Math.min(done, dh);
        rect(batch, GOLD, left - t, bottom, t, side1);
        float side2 = MathUtils.clamp(done - dh, 0f, dw);
        if (side2 > 0f) rect(batch, GOLD, left - t, bottom + dh, side2 + t, t);
        float side3 = MathUtils.clamp(done - dh - dw, 0f, dh);
        if (side3 > 0f) rect(batch, GOLD, left + dw, bottom + dh - side3 + t, t, side3);
        float tipX, tipY;
        if (done < dh) { tipX = left; tipY = bottom + side1; }
        else if (done < dh + dw) { tipX = left + side2; tipY = bottom + dh; }
        else { tipX = left + dw; tipY = bottom + dh - side3; }
        if (trace < 1f) glow(batch, tipX, tipY, 10f * u, GOLD_LIGHT, 0.9f);
        glow(batch, cx, bottom + dh / 2f, 40f * u, GOLD, 0.15f * trace);
        rect(batch, GOLD_LIGHT, cx + 3.5f * u, bottom + dh * 0.45f, u, u);  // la poignée
    }

    /** Toi, de dos, en bas de l'écran : tu longes les machines avec la caméra. */
    private void drawWalker(Batch batch, float w, float h) {
        float scale = h * 0.55f / silhouette.getRegionHeight();
        float bob = Math.abs(MathUtils.sin((time - HALL_CUT) * MathUtils.PI / STEP_EVERY)) * 0.5f * scale;
        float out = Interpolation.pow2In.apply(progress(PAN_END, PULL_START));
        float x = w * 0.26f - out * w * 0.3f;
        float sw = silhouette.getRegionWidth() * scale, sh = silhouette.getRegionHeight() * scale;
        batch.setColor(GOLD.r, GOLD.g, GOLD.b, 0.7f);                         // la lumière des lustres sur ses épaules
        batch.draw(silhouette, x - sw / 2f - 5f, -h * 0.28f + bob + 5f, sw, sh);
        batch.draw(silhouette, x - sw / 2f + 5f, -h * 0.28f + bob + 5f, sw, sh);
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(silhouette, x - silhouette.getRegionWidth() * scale / 2f, -h * 0.28f + bob,
            silhouette.getRegionWidth() * scale, silhouette.getRegionHeight() * scale);
    }

    /** Il bat les cartes : deux paquets qui s'inclinent l'un vers l'autre, les cartes qui cascadent. */
    private void drawShuffle(Batch batch, float w, float h) {
        if (time >= SNAP) return;
        float s = croupierScale(h), y = h * 0.36f, ch = 5.5f * s;
        for (int side = -1; side <= 1; side += 2) {
            float riffle = Math.max(riffle(RIFFLE_1), riffle(RIFFLE_2));
            float x = w / 2f + side * (4.5f * s - riffle * 1.5f * s);
            batch.setColor(Color.WHITE);
            batch.draw(back, x - ch * CARD_RATIO / 2f, y, ch * CARD_RATIO / 2f, 0f, ch * CARD_RATIO, ch, 1f, 1f,
                -side * riffle * 25f);
            if (riffle > 0f) {                                                 // les cartes qui tombent en cascade
                for (int k = 0; k < 4; k++) {
                    float fx = w / 2f + side * (0.8f + k) * 0.7f * s, fy = y + ch * (0.3f + 0.15f * ((k + (int) (time * 30f)) % 4));
                    rect(batch, GLASS, fx - 0.4f * s, fy, 0.8f * s, 0.25f * s);
                }
            }
        }
    }

    /** @return l'élan du mélange qui commence à {@code start} (0 au repos, 1 au plus fort). */
    private float riffle(float start) {
        float k = progress(start, start + RIFFLE_LENGTH);
        return k <= 0f || k >= 1f ? 0f : MathUtils.sin(k * MathUtils.PI);
    }

    /** Gros plan sur le tapis : il distribue tes cinq cartes, faces cachées, puis les retourne. */
    private void drawDeal(Batch batch, float w, float h) {
        drawFeltClose(batch, w, h, h + MARGIN, false);
        glow(batch, w / 2f, h * 0.5f, w * 0.9f, GOLD, 0.12f);
        // Son ombre à cornes tombe sur le tapis, depuis le haut.
        float s = h * 1.0f / 42f;
        batch.setColor(0f, 0f, 0f, 0.35f);
        batch.draw(grand, w / 2f - 17f * s, h * 0.6f, 34f * s, 42f * s);
        float ch = h * 0.34f, y = h * 0.4f;
        for (int i = 0; i < hand.length; i++) {
            float land = DEAL_START + i * DEAL_STEP, k = Interpolation.pow2Out.apply(progress(land - DEAL_FLIGHT, land));
            if (k <= 0f) continue;
            float x = dealX(i, w), cx = MathUtils.lerp(w / 2f, x, k), cy = MathUtils.lerp(h * 1.15f, y, k);
            float flip = progress(FLIP_START + i * FLIP_STEP, FLIP_START + i * FLIP_STEP + FLIP_LENGTH);
            card(batch, hand[i], cx, cy, ch * MathUtils.lerp(1.2f, 1f, k), (1f - k) * 200f, flip, false);
            if (flip > 0f && flip < 1f) glow(batch, x, y, ch * 1.3f, GOLD, 0.4f);
        }
    }

    private static float dealX(int i, float w) { return w * (0.5f + 0.17f * (i - 2)); }

    // =========================================================================
    // Phase 2 : la table truquée
    // =========================================================================

    private void drawRig(Batch batch, float w, float h) {
        if (time < UNDER_CUT || time >= BACK_CUT) drawMyMachine(batch, w, h);
        else drawUnderTable(batch, w, h);
        flash(batch, RED, RIG_FLASH, 0.35f, 0.55f);
        flash(batch, GOLD_LIGHT, LOCK, 0.2f, 0.35f);
        fadeThroughBlack(batch, UNDER_CUT, 0.12f);
        fadeThroughBlack(batch, BACK_CUT, 0.12f);
    }

    /** @return l'instant où le rouleau {@code i} s'arrête. */
    private float stopTime(int i) {
        if (i == riggedReel) return RIG_STOP;
        int other = i < riggedReel ? i : i - 1;
        return OTHERS_STOP[other];
    }

    /** Ta machine, en gros plan, et ses trois rouleaux : le tien truqué s'arrête sur son symbole. */
    private void drawMyMachine(Batch batch, float w, float h) {
        gradient(batch, hex("1a0204"), hex("3a060c"));
        float u = unit();
        for (int k = 0; k < 9; k++) {                                         // les lumières floues de la salle
            float x = w * ((k * 0.137f + 0.05f) % 1f), y = h * (0.25f + 0.55f * ((k * 0.311f) % 1f));
            glow(batch, x, y, (18f + 6f * (k % 3)) * u, k % 2 == 0 ? GOLD : RED, 0.12f + 0.05f * MathUtils.sin(time * 3f + k));
        }
        float U = MY_U * u, bottom = h * 0.05f;
        float red = time >= RIG_FLASH && time < UNDER_CUT ? 1f - progress(RIG_FLASH, RIG_FLASH + 0.5f) : 0f;
        machine(batch, w / 2f, bottom, U, MY_LIGHT, MY_BODY, MY_DARK, 3, 1f, (i, x, y, rw, rh) -> {
            float offset;
            if (time >= stopTime(i)) offset = i == riggedReel ? riggedIndex : Math.round(reelOffsets[i]);
            else offset = reelOffsets[i];
            batch.setColor(1f, 1f - 0.6f * red, 1f - 0.6f * red, 1f);
            reel(batch, strip, x, y, rw, rh, offset);
            if (i == riggedReel && time >= RIG_STOP) {                        // la chaîne, puis le cadenas
                drawChain(batch, x, y, rw, rh, U);
                if (time >= LOCK) {
                    float pop = Interpolation.swingOut.apply(progress(LOCK, LOCK + 0.3f));
                    glow(batch, x + rw / 2f, y + rh / 2f, rw * 2.5f, GOLD, 0.35f * pop);
                    batch.setColor(Color.WHITE);
                    sprite(batch, padlock, x + rw / 2f, y - 2.6f * U, 0.75f * U * pop, 0f);
                }
            }
        });
    }

    /** @return le milieu à l'écran du rouleau {@code i} de ta machine. */
    private float reelCenterX(int i) {
        float U = MY_U * unit(), reelW = 9f * U, gap = 1.5f * U, panelW = 3 * reelW + 4 * gap;
        return worldWidth() / 2f - panelW / 2f + gap + i * (reelW + gap) + reelW / 2f;
    }

    /** Une chaîne d'or qui fait le tour du rouleau truqué, maillon après maillon, de CHAIN_START à CHAIN_END. */
    private void drawChain(Batch batch, float x, float y, float rw, float rh, float U) {
        float grow = progress(CHAIN_START, CHAIN_END);
        if (grow <= 0f) return;
        float m = 1.6f * U, left = x - m, bottom = y - m, cw = rw + 2f * m, ch = rh + 2f * m;
        float perimeter = 2f * (cw + ch), link = 1.5f * U;
        int links = (int) (perimeter / link * grow);
        for (int k = 0; k < links; k++) {
            float d = (k + 0.5f) * link, lx, ly, a;
            if (d < cw) { lx = left + d; ly = bottom + ch; a = 0f; }                       // en haut, vers la droite
            else if (d < cw + ch) { lx = left + cw; ly = bottom + ch - (d - cw); a = 90f; } // à droite, vers le bas
            else if (d < 2f * cw + ch) { lx = left + cw - (d - cw - ch); ly = bottom; a = 0f; }
            else { lx = left; ly = bottom + (d - 2f * cw - ch); a = 90f; }
            boolean flat = k % 2 == 0;
            float lw = flat ? 1.8f * U : 1.3f * U, lh = flat ? 1.1f * U : 0.6f * U;
            batch.setColor(OUT);
            batch.draw(pixel, lx - lw / 2f - 0.3f * U, ly - lh / 2f - 0.3f * U, lw / 2f + 0.3f * U, lh / 2f + 0.3f * U,
                lw + 0.6f * U, lh + 0.6f * U, 1f, 1f, a);
            batch.setColor(flat ? GOLD : GOLD_DARK);
            batch.draw(pixel, lx - lw / 2f, ly - lh / 2f, lw / 2f, lh / 2f, lw, lh, 1f, 1f, a);
            if (flat) {
                batch.setColor(OUT);
                batch.draw(pixel, lx - 0.4f * U, ly - 0.15f * U, 0.4f * U, 0.15f * U, 0.8f * U, 0.3f * U, 1f, 1f, a);
            }
        }
    }

    /** Sous la table : le mécanisme à engrenages, sa main gantée qui tourne une clé d'or, trois crans. */
    private void drawUnderTable(Batch batch, float w, float h) {
        float u = unit();
        fill(batch, hex("0c0406"), 1f);
        for (int k = 0; k < 7; k++) {                                         // le dessous du plateau : planches
            float y = h - (k + 1) * 4f * u;
            rect(batch, k % 2 == 0 ? WOOD_DARK : hex("2a1206"), -MARGIN, y, w + 2f * MARGIN, 4f * u);
            rect(batch, OUT, -MARGIN, y, w + 2f * MARGIN, 0.5f * u);
        }
        for (float x = w * 0.12f; x < w; x += w * 0.38f) {                     // pieds de la table
            rect(batch, OUT, x - 3f * u, -MARGIN, 6f * u, h);
            rect(batch, WOOD_DARK, x - 2f * u, -MARGIN, 4f * u, h);
        }
        glow(batch, w * 0.5f, h * 0.55f, 80f * u, RED, 0.18f);
        // Le boîtier de cuivre et ses engrenages.
        float bx = w * 0.5f, by = h * 0.5f, bw = 44f * u, bh = 28f * u;
        rect(batch, OUT, bx - bw / 2f - u, by - bh / 2f - u, bw + 2f * u, bh + 2f * u);
        rect(batch, hex("6a4a1a"), bx - bw / 2f, by - bh / 2f, bw, bh);
        rect(batch, hex("8a6a2a"), bx - bw / 2f, by + bh / 2f - 2f * u, bw, 2f * u);
        int turns = 0;
        for (float turn : KEY_TURNS) if (time >= turn) turns++;
        float step = 0f;
        for (float turn : KEY_TURNS) step += Interpolation.pow2Out.apply(progress(turn, turn + 0.1f));
        gear(batch, bx - 12f * u, by + 3f * u, 7f * u, step * 30f, u);
        gear(batch, bx + 2f * u, by - 5f * u, 5f * u, -step * 42f, u);
        gear(batch, bx + 13f * u, by + 5f * u, 4f * u, step * 52f, u);
        // La serrure.
        float kx = bx + 2f * u, ky = by + 9f * u;
        rect(batch, OUT, kx - 2.5f * u, ky - 2.5f * u, 5f * u, 5f * u);
        rect(batch, GOLD_DARK, kx - 2f * u, ky - 2f * u, 4f * u, 4f * u);
        rect(batch, OUT, kx - 0.5f * u, ky - 1.5f * u, u, 2.5f * u);
        if (time >= KEY_IN) glow(batch, kx, ky, 14f * u, GOLD, 0.25f + 0.2f * turns);
        // Sa main gantée, la clé d'or entre ses doigts, arrive de la droite ; la clé entre, puis tourne de trois crans.
        float in = Interpolation.pow2Out.apply(progress(HAND_IN, HAND_AT));
        float push = Interpolation.pow2Out.apply(progress(HAND_AT, KEY_IN));
        float tx = MathUtils.lerp(w * 0.7f, 0f, in), ty = MathUtils.lerp(-h * 0.25f, 0f, in) + (1f - push) * 1.5f * u;
        float angle = -step * 25f, s = 1.3f * u;
        float kw = key.getRegionWidth() * s, kh = key.getRegionHeight() * s;
        float px = kx + tx, py = ky + ty;                                      // le pivot : le trou de la serrure
        batch.setColor(Color.WHITE);
        batch.draw(key, px - kw / 2f, py, kw / 2f, 0f, kw, kh, 1f, 1f, angle);
        float gs = 0.9f * s, gw = glove.getRegionWidth() * gs, gh = glove.getRegionHeight() * gs;
        float gx = px + 0.15f * kw, gy = py + 0.62f * kh - gh / 2f;              // les doigts pincent l'anneau de la clé
        batch.setColor(hex("5a0e1a"));                                         // sa manche, jusqu'au bord de l'écran
        batch.draw(pixel, gx + gw - gs, gy + 1.5f * gs, px - (gx + gw - gs), py - (gy + 1.5f * gs), w, gh - 3f * gs, 1f, 1f, angle);
        batch.setColor(Color.WHITE);
        batch.draw(glove, gx, gy, px - gx, py - gy, gw, gh, 1f, 1f, angle);
        // Deux yeux rouges au fond, sous la table : c'est bien lui.
        float eyes = 0.5f + 0.5f * MathUtils.sin(time * 4f);
        glow(batch, w * 0.86f, h * 0.62f, 8f * u, RED, 0.5f * eyes);
        glow(batch, w * 0.9f, h * 0.62f, 8f * u, RED, 0.5f * eyes);
    }

    /** Un engrenage de rayon {@code r}, tourné de {@code angle} degrés. */
    private void gear(Batch batch, float x, float y, float r, float angle, float u) {
        for (int pass = 0; pass < 2; pass++) {
            Color color = pass == 0 ? OUT : GOLD_DARK;
            float grow = pass == 0 ? 0.6f * u : 0f;
            batch.setColor(color);
            for (int k = 0; k < 4; k++) {
                float len = 2f * r + 2f * grow, thick = 0.45f * r + grow;
                batch.draw(pixel, x - len / 2f, y - thick / 2f, len / 2f, thick / 2f, len, thick, 1f, 1f, angle + k * 45f);
            }
            float body = 1.6f * r + grow;
            batch.draw(pixel, x - body / 2f, y - body / 2f, body / 2f, body / 2f, body, body, 1f, 1f, angle + 22.5f);
        }
        rect(batch, GOLD_LIGHT, x - 0.3f * r, y + 0.2f * r, 0.25f * r, 0.25f * r);
        rect(batch, OUT, x - 0.2f * r, y - 0.2f * r, 0.4f * r, 0.4f * r);
    }

    // =========================================================================
    // Phase 3 : cartes sur table
    // =========================================================================

    private void drawOpen(Batch batch, float w, float h) {
        float drop = Interpolation.bounceOut.apply(progress(LAMP_DROP, LIGHT_HIT));
        float dark = progress(LAMP_DROP, LIGHT_HIT);
        float[] lit = {1f, 1f, 1f, 1f, 1f, 1f};
        drawHallBackdrop(batch, w, h, 0f, 0f);
        camera(batch, w / 2f, OPEN_CY * h, OPEN_ZOOM);
        float s = croupierScale(h), bottom = croupierBottom(h);
        float glare = Interpolation.pow2Out.apply(progress(GLARE, GLARE_END));
        drawHall(batch, w, h, lit, 0f, () -> {
            glow(batch, w / 2f, bottom + 26f * s, 110f * s, RED, 0.45f + 0.3f * glare);
            drawHornedShadow(batch, w / 2f, bottom, s, 0.25f + 0.75f * glare, 0.8f);
        });
        endCamera(batch);
        fill(batch, Color.BLACK, 0.55f * dark);                               // la salle plonge dans le noir
        camera(batch, w / 2f, OPEN_CY * h, OPEN_ZOOM);
        float slide = Interpolation.pow2Out.apply(progress(GLARE, HAT_SLIDE_END));
        if (slide <= 0f) portrait(batch, grand, w / 2f, bottom, s, 0f, 1f);
        else {                                                                // son chapeau glisse en arrière
            portrait(batch, bareHead, w / 2f, bottom, s, 0f, 1f);
            drawHat(batch, w / 2f, bottom, s, -0.5f * slide * s, slide * 2.5f * s, slide * 12f);
        }
        drawEyes(batch, w / 2f, bottom, s, 0.6f + 0.6f * glare);
        drawLamp(batch, w / 2f, h, MathUtils.lerp(h * 0.9f, h * 0.81f, drop), 1f + 0.6f * dark);
        endCamera(batch);
        drawFeltClose(batch, w, h, h * 0.24f, true);
        glow(batch, w / 2f, h * 0.12f, w * 0.75f, GOLD_LIGHT, 0.3f * dark);   // le faisceau sur le tapis
        // Il jette sa main : les cartes montent, tournoient, retombent face visible devant toi.
        float ch = h * 0.19f;
        for (int i = 0; i < hand.length; i++) {
            if (time < THROW) break;
            float rise = Interpolation.pow2Out.apply(progress(THROW, PEAK));
            float land = landTime(i), fall = Interpolation.pow2In.apply(progress(PEAK, land));
            float peakX = w / 2f + (i - (hand.length - 1) / 2f) * w * 0.05f, peakY = h * 0.8f + (i % 2) * h * 0.05f;
            float x, y, spin;
            if (time < PEAK) {
                x = MathUtils.lerp(w / 2f, peakX, rise);
                y = MathUtils.lerp(h * 0.42f, peakY, rise);
                spin = rise * 360f * (i % 2 == 0 ? 1f : -1f);
            } else {
                x = MathUtils.lerp(peakX, fanX(i, w), fall);
                y = MathUtils.lerp(peakY, fanY(i, h), fall);
                spin = MathUtils.lerp(360f * (i % 2 == 0 ? 1f : -1f), fanAngle(i), fall);
            }
            float flip = progress(land - 0.15f, land);
            card(batch, hand[i], x, y, ch * MathUtils.lerp(0.6f, 1f, time < PEAK ? 0f : fall), spin, flip, handIsHis);
            if (time >= land) glow(batch, x, y, ch * 1.2f, GOLD, 0.3f * (1f - progress(land, land + 0.4f)));
        }
        flash(batch, GOLD_LIGHT, LIGHT_HIT, 0.25f, 0.3f);
        flash(batch, RED, GLARE, 0.35f, 0.4f);
    }

    /** Le cadrage de la phase 3 : au plus près de lui, la lampe au-dessus. */
    private static final float OPEN_CY = 0.56f, OPEN_ZOOM = 1.35f;

    /** @return l'instant où la carte {@code i} de sa main touche le tapis. */
    private static float landTime(int i) { return LAND_START + i * LAND_STEP; }

    private float fanX(int i, float w) { return w / 2f + (i - (hand.length - 1) / 2f) * w * Math.min(0.11f, 0.7f / hand.length); }

    private float fanY(int i, float h) {
        float off = (i - (hand.length - 1) / 2f) / Math.max(1f, hand.length / 2f);
        return h * 0.115f - off * off * h * 0.025f;
    }

    private float fanAngle(int i) { return -(i - (hand.length - 1) / 2f) * 6f; }

    // =========================================================================
    // Vraie fin
    // =========================================================================

    private void drawEnding(Batch batch, float w, float h) {
        if (time < REVERSE_CUT) drawFarewell(batch, w, h);
        else drawNewCroupier(batch, w, h);
        float k = 1f - Math.abs(time - REVERSE_CUT) / 0.25f;                  // passage au blanc
        if (k > 0f) fill(batch, Color.WHITE, Interpolation.pow2Out.apply(k));
    }

    /** Il pose sa dernière main, s'incline, laisse son chapeau et le sabot ; la salle s'éteint, l'aube entre. */
    private void drawFarewell(Batch batch, float w, float h) {
        float[] lit = new float[HALL_X.length];
        for (int k = 0; k < LIGHT_ORDER.length; k++) {
            float off = OFF_START + k * OFF_STEP;
            lit[LIGHT_ORDER[LIGHT_ORDER.length - 1 - k]] = 1f - progress(off, off + 0.1f);
        }
        float roof = Interpolation.pow2InInverse.apply(progress(ROOF_START, ROOF_END));
        float dawn = progress(ROOF_START, ENDING_COVER);
        drawHallBackdrop(batch, w, h, roof, dawn);
        float zoom = MathUtils.lerp(1.15f, 1f, Interpolation.pow2.apply(progress(OFF_START, ROOF_END)));
        camera(batch, w / 2f, h * 0.55f, zoom);
        drawHall(batch, w, h, lit, roof, null);
        float s = croupierScale(h), bottom = croupierBottom(h);
        float bow = Interpolation.pow2.apply(progress(HAT_CHEST, BOW_DOWN)) - Interpolation.pow2.apply(progress(BOW_UP, BOW_END));
        float vanish = progress(VANISH_START, VANISH_END);
        float body = bottom - bow * 3f * s;
        if (time < HAT_OFF) portrait(batch, grand, w / 2f, bottom, s, 0f, 1f);
        else {
            portrait(batch, bareHead, w / 2f, body, s, 0f, 1f - vanish);
            drawEyes(batch, w / 2f, body, s, 1f - vanish);
        }
        if (vanish > 0f) glow(batch, w / 2f, bottom + 21f * s, 40f * s, GOLD_LIGHT, 0.5f * MathUtils.sin(vanish * MathUtils.PI));
        drawHallTable(batch, w, h);
        // Sa dernière main, perdante, posée à sa droite (le chapeau ira à sa gauche).
        float ch = 4.2f * s;
        for (int i = 0; i < losing.length; i++) {
            float land = LAY_START + i * LAY_STEP, k = Interpolation.pow2Out.apply(progress(land - LAY_FLIGHT, land));
            if (k <= 0f) continue;
            float x = MathUtils.lerp(w / 2f, w / 2f + (3.5f + i * 3.0f) * s, k), y = MathUtils.lerp(h * 0.45f, h * 0.36f, k);
            card(batch, losing[i], x, y, ch, (i - 2) * 4f * k, 1f, false);
        }
        // Le chapeau : sur sa tête, contre sa poitrine, puis posé sur la table.
        if (time >= HAT_OFF) {
            float toChest = Interpolation.pow2Out.apply(progress(HAT_OFF, HAT_CHEST));
            float toTable = Interpolation.pow2.apply(progress(HAT_DOWN, HAT_ON_TABLE));
            float headX = 0f, headY = 0f;
            float chestX = -4f * s, chestY = -21f * s - bow * 3f * s;
            float tableX = -16f * s, tableY = h * 0.395f - (bottom + 36f * s);
            float dx = MathUtils.lerp(MathUtils.lerp(headX, chestX, toChest), tableX, toTable);
            float dy = MathUtils.lerp(MathUtils.lerp(headY, chestY, toChest), tableY, toTable);
            float rot = MathUtils.lerp(toChest * 18f, 0f, toTable);
            drawHat(batch, w / 2f, bottom, s, dx, dy, rot);
        }
        // Le sabot glisse vers toi.
        float slide = Interpolation.pow2Out.apply(progress(SHOE_START, SHOE_STOP));
        if (time >= SHOE_START) {
            batch.setColor(Color.WHITE);
            sprite(batch, shoe, MathUtils.lerp(w / 2f + 9f * s, w / 2f + 3f * s, slide),
                MathUtils.lerp(h * 0.37f, h * 0.2f, slide), MathUtils.lerp(0.6f, 1.6f, slide) * s, 0f);
        }
        endCamera(batch);
        if (roof > 0f) {                                                      // le jour qui tombe du toit ouvert
            additive(batch);
            batch.setColor(DAWN_LOW.r, DAWN_LOW.g, DAWN_LOW.b, 0.35f * roof);
            batch.draw(soft, w * 0.2f, -h * 0.4f, w * 0.6f, h * 2.2f);
            normal(batch);
            glow(batch, w / 2f, h * 0.95f, w * 0.8f, DAWN_LOW, 0.35f * roof);
        }
    }

    /** Contrechamp à l'aube : toi, derrière la table, son chapeau sur la tête, sa main perdante et le sabot devant toi. */
    private void drawNewCroupier(Batch batch, float w, float h) {
        float u = unit();
        float rise = progress(REVERSE_CUT, ENDING_COVER + COVER_LENGTH);
        gradient(batch, DAWN_LOW, DAWN_TOP);
        float sunY = h * (0.42f + 0.1f * rise);
        glow(batch, w * 0.5f, sunY, w * 1.3f, DAWN_MID, 0.55f);
        glow(batch, w * 0.5f, sunY, 50f * u, hex("fff2c0"), 1f);
        for (int c = 0; c < 6; c++) {                                          // nuages en pixels, qui dérivent
            float cx = ((c * 0.19f + 0.03f + time * 0.012f) % 1.2f - 0.1f) * w, cy = h * (0.6f + 0.07f * (c % 3));
            for (int b = 0; b < 4; b++) {
                rect(batch, hex("ffd8b0"), cx + b * 5f * u, cy + (b % 2) * u, 7f * u, 2.5f * u);
                rect(batch, hex("f4a88a"), cx + b * 5f * u, cy - u, 7f * u, u);
            }
        }
        float zoom = MathUtils.lerp(1f, 1.06f, Interpolation.sine.apply(rise));
        camera(batch, w / 2f, h * 0.5f, zoom);
        drawBattlements(batch, w, h * 0.36f, u);
        // Toi, en contre-jour derrière la table, les mains posées sur le tapis.
        float scale = h * 0.5f / dealer.getRegionHeight(), sx = w / 2f, sy = h * 0.29f;
        float dw = dealer.getRegionWidth() * scale, dh = dealer.getRegionHeight() * scale;
        batch.setColor(DAWN_LOW.r, DAWN_LOW.g, DAWN_LOW.b, 0.9f);              // le liseré de lumière de l'aube
        for (int k = 0; k < 4; k++) {
            float ox = (k % 2 == 0 ? 1 : -1) * 0.25f * scale, oy = (k < 2 ? 0.25f : 0f) * scale;
            batch.draw(dealer, sx - dw / 2f + ox, sy + oy, dw, dh);
        }
        batch.setColor(Color.WHITE);
        batch.draw(dealer, sx - dw / 2f, sy, dw, dh);
        float hatScale = scale * 0.42f;                                       // son chapeau, sur ta tête
        sprite(batch, hat, sx, sy + dh - 1.6f * scale + 5f * hatScale, hatScale, -3f);
        // La table : le tapis où tu poses les mains, son liseré d'or, le bois au premier plan.
        float felt = sy + 1.2f * scale, edge = h * 0.1f;
        rect(batch, OUT, -MARGIN, felt, w + 2f * MARGIN, 0.6f * u);
        rect(batch, FELT, -MARGIN, edge, w + 2f * MARGIN, felt - edge);
        for (float y = felt - 4f * u; y > edge; y -= 6f * u) {
            for (float x = -MARGIN + ((int) (y / (6f * u)) % 2) * 6f * u; x < w + MARGIN; x += 12f * u) {
                rect(batch, FELT_DARK, x, y, 1.5f * u, 1.5f * u);
            }
        }
        batch.setColor(Color.WHITE);                                          // tes mains, sur le tapis
        batch.draw(hands, sx - dw / 2f, sy - 0.8f * scale, dw, hands.getRegionHeight() * scale);
        for (int i = 0; i < losing.length; i++) {                              // sa dernière main
            card(batch, losing[i], sx - (8.5f - i * 1.6f) * scale, h * 0.2f + (i % 2) * 0.2f * scale, 3.2f * scale,
                (i - 2) * 9f, 1f, false);
        }
        batch.setColor(Color.WHITE);
        sprite(batch, shoe, sx + 8.5f * scale, h * 0.2f, 0.75f * scale, 0f);
        rect(batch, GOLD, -MARGIN, edge, w + 2f * MARGIN, 0.6f * u);
        rect(batch, OUT, -MARGIN, edge - u, w + 2f * MARGIN, u);
        rect(batch, WOOD, -MARGIN, -MARGIN, w + 2f * MARGIN, edge - u + MARGIN);
        rect(batch, WOOD_LIGHT, -MARGIN, edge - 2.5f * u, w + 2f * MARGIN, u);
        endCamera(batch);
        glow(batch, w * 0.5f, sunY, w * 0.5f, hex("fff2c0"), 0.25f * rise);
    }

    // =========================================================================
    // La salle des machines
    // =========================================================================

    /** Le fond de la salle (avant la caméra) : le ciel de l'aube derrière le toit qui s'ouvre. */
    private void drawHallBackdrop(Batch batch, float w, float h, float roof, float dawn) {
        if (roof > 0f) {
            gradient(batch, DAWN_LOW, DAWN_TOP);
            glow(batch, w / 2f, h, w, hex("fff2c0"), 0.4f * dawn);
        } else fill(batch, WALL_DARK, 1f);
    }

    /**
     * La salle : mur et rideaux de velours, plafond à caissons (qui s'ouvre en deux avec {@code roof}),
     * lustres, moquette, et les six machines des lieux ({@code lit} : allumage de chacune).
     */
    private void drawHall(Batch batch, float w, float h, float[] lit, float roof, Runnable onWall) {
        float u = unit(), ceiling = h * 0.9f, floor = h * 0.3f;
        // Le mur, ses pilastres dorés.
        rect(batch, WALL, -w, floor, 3f * w, ceiling - floor);
        for (float x = -w; x < 2f * w; x += w / 7f) {
            rect(batch, WALL_DARK, x - 2f * u, floor, 4f * u, ceiling - floor);
            rect(batch, hex("4a3010"), x - 0.5f * u, floor, u, ceiling - floor);
        }
        if (onWall != null) onWall.run();                                     // ce qui se dessine sur le mur (son ombre)
        // La moquette, à motifs.
        rect(batch, CARPET, -w, -h, 3f * w, floor + h);
        for (float y = floor - 3f * u; y > -h * 0.5f; y -= 6f * u) {
            for (float x = -w + ((int) (y / (6f * u)) % 2) * 6f * u; x < 2f * w; x += 12f * u) {
                rect(batch, CARPET_DARK, x, y, 3f * u, 3f * u);
                rect(batch, hex("8a5a1a"), x + u, y + u, u, u);
            }
        }
        rect(batch, OUT, -w, floor - 0.5f * u, 3f * w, u);
        // Le plafond à caissons (il s'ouvre en deux à la fin).
        float open = roof * w * 0.65f;
        for (int side = -1; side <= 1; side += 2) {
            float x0 = side < 0 ? -w - open : w / 2f + open, x1 = side < 0 ? w / 2f - open : 2f * w + open;
            rect(batch, WOOD_DARK, x0, ceiling, x1 - x0, h);
            for (float x = x0 + 2f * u; x < x1 - 8f * u; x += 10f * u) {
                rect(batch, hex("2a1206"), x, ceiling + 2f * u, 8f * u, 6f * u);
                rect(batch, GOLD_DARK, x, ceiling + 2f * u, 8f * u, 0.5f * u);
            }
            rect(batch, GOLD_DARK, x0, ceiling, x1 - x0, u);
        }
        // Les rideaux de velours, en festons sous le plafond, et sur les côtés.
        for (float x = -w; x < 2f * w; x += 8f * u) {
            rect(batch, VELVET_DARK, x, ceiling - 5f * u, 8f * u, 5f * u);
            rect(batch, VELVET, x + u, ceiling - 4f * u, 6f * u, 4f * u);
            rect(batch, VELVET, x + 2f * u, ceiling - 5.5f * u, 4f * u, 1.5f * u);
            rect(batch, GOLD_DARK, x + 3.5f * u, ceiling - 6.5f * u, u, u);
        }
        // Les lustres.
        float chandelier = 0.5f + 0.5f * average(lit);
        for (float x : new float[] {w * 0.25f, w * 0.75f}) {
            float y = ceiling - 10f * u;
            rect(batch, OUT, x - 0.25f * u, y + 2f * u, 0.5f * u, 4f * u);
            rect(batch, OUT, x - 6f * u, y - 0.5f * u, 12f * u, 2.5f * u);
            rect(batch, GOLD_DARK, x - 5.5f * u, y, 11f * u, 1.5f * u);
            for (int c = -2; c <= 2; c++) {
                float cx = x + c * 2.6f * u;
                rect(batch, GLASS, cx - 0.4f * u, y + 1.5f * u, 0.8f * u, 1.6f * u);
                glow(batch, cx, y + 3.6f * u, 4f * u, GOLD_LIGHT, 0.7f * chandelier);
            }
            glow(batch, x, y, 40f * u, GOLD, 0.12f * chandelier);
        }
        // Les six machines, une par lieu.
        for (int i = 0; i < HALL_X.length; i++) {
            float on = lit[i];
            bright = 0.35f + 0.65f * on;
            final int index = i;
            machine(batch, HALL_X[i] * w, floor - u, 0.52f * u, HALL_COLORS[i][0], HALL_COLORS[i][1], HALL_COLORS[i][2],
                1, on, (r, x, y, rw, rh) -> {
                    if (on <= 0f) return;
                    batch.setColor(on, on, on, 1f);
                    sprite(batch, hallSymbols[index], x + rw / 2f, y + rh / 2f, rw * 0.8f / hallSymbols[index].getRegionWidth(), 0f);
                });
            bright = 1f;
            if (on > 0f) glow(batch, HALL_X[i] * w, floor + 14f * u, 30f * u, HALL_COLORS[i][0], 0.25f * on);
        }
        // La lampe au-dessus de sa table.
        drawLamp(batch, w / 2f, h, h * 0.9f, 1f);
    }

    private static float average(float[] values) {
        float sum = 0f;
        for (float v : values) sum += v;
        return sum / values.length;
    }

    /** La lampe de jeu : chaîne depuis le plafond, abat-jour vert, ampoule ; {@code shadeY} = bas de l'abat-jour. */
    private void drawLamp(Batch batch, float x, float h, float shadeY, float power) {
        float u = unit(), top = h + MARGIN, shade = shadeY - 0.0f;
        rect(batch, OUT, x - 0.3f * u, shade + 3.5f * u, 0.6f * u, top - shade);
        for (int k = 0; k < 3; k++) {
            float half = (3f + k * 1.8f) * u, y = shade + (2 - k) * 1.2f * u;
            rect(batch, OUT, x - half - 0.5f * u, y - 0.5f * u, 2f * half + u, 1.7f * u);
            rect(batch, k == 2 ? hex("3a8a4a") : SHADE, x - half, y, 2f * half, 1.2f * u);
        }
        rect(batch, GOLD, x - 6.6f * u, shade, 13.2f * u, 0.5f * u);
        glow(batch, x, shade - u, 10f * u, BULB, 0.9f);
        glow(batch, x, shade - 10f * u, 60f * u * power, GOLD_LIGHT, 0.18f * power);
    }

    /** @return l'échelle du portrait du Croupier dans la salle (42 pixels de haut). */
    private static float croupierScale(float h) { return h * 0.42f / 42f; }

    /** @return le bas du portrait du Croupier dans la salle (caché par sa table). */
    private static float croupierBottom(float h) { return h * 0.3f; }

    /** Sa table, devant lui : plateau de tapis vert au liseré d'or, façade de bois. */
    private void drawHallTable(Batch batch, float w, float h) {
        float u = unit(), left = w * 0.33f, right = w * 0.67f, top = h * 0.38f, front = h * 0.33f, bottom = h * 0.2f;
        rect(batch, OUT, left - u, bottom - u, right - left + 2f * u, top - bottom + 2f * u);
        rect(batch, FELT, left, front, right - left, top - front);
        rect(batch, FELT_LIGHT, left, top - u, right - left, u);
        rect(batch, GOLD, left, front, right - left, 0.8f * u);
        rect(batch, WOOD, left, bottom, right - left, front - bottom);
        rect(batch, WOOD_LIGHT, left, front - 1.5f * u, right - left, u);
        for (float x = left + 4f * u; x < right - 4f * u; x += 9f * u) {
            rect(batch, WOOD_DARK, x, bottom + 1.5f * u, 7f * u, front - bottom - 4f * u);
            rect(batch, GOLD_DARK, x + 3f * u, bottom + (front - bottom) / 2f - 0.5f * u, u, u);
        }
    }

    /** Le tapis vu de près (en bas de l'écran jusqu'à {@code top}), avec, en haut, le bord de sa table. */
    private void drawFeltClose(Batch batch, float w, float h, float top, boolean edge) {
        float u = unit();
        rect(batch, FELT, -MARGIN, -MARGIN, w + 2f * MARGIN, top + MARGIN);
        for (float y = top - 6f * u; y > -MARGIN; y -= 8f * u) {              // le motif du tapis
            for (float x = -MARGIN + ((int) (y / (8f * u)) % 2) * 8f * u; x < w + MARGIN; x += 16f * u) {
                rect(batch, FELT_DARK, x, y, 2f * u, 2f * u);
            }
        }
        rect(batch, GOLD_DARK, w * 0.06f, h * 0.06f, w * 0.88f, 0.5f * u);   // le liseré d'or peint sur le tapis
        if (top < h) {
            rect(batch, GOLD_DARK, w * 0.06f, top - 3f * u, w * 0.88f, 0.5f * u);
        }
        if (edge) {
            rect(batch, OUT, -MARGIN, top, w + 2f * MARGIN, 2f * u);
            rect(batch, WOOD, -MARGIN, top + 0.5f * u, w + 2f * MARGIN, 1.5f * u);
            rect(batch, GOLD, -MARGIN, top - 0.5f * u, w + 2f * MARGIN, 0.5f * u);
        }
    }

    // =========================================================================
    // Le Croupier
    // =========================================================================

    /** Son ombre à cornes sur le mur derrière lui, agrandie de {@code grow} (0 = sa taille, 1 = jusqu'au plafond). */
    private void drawHornedShadow(Batch batch, float x, float bottom, float s, float grow, float strength) {
        if (strength <= 0f) return;
        float scale = s * (1f + 0.9f * grow);
        batch.setColor(0.04f, 0f, 0.02f, strength);
        batch.draw(grand, x - 17f * scale + 4f * s, bottom, 34f * scale, 42f * scale);
    }

    /** Ses yeux rouges qui brillent ({@code strength} 0 à 1). */
    private void drawEyes(Batch batch, float x, float bottom, float s, float strength) {
        if (strength <= 0f) return;
        float y = bottom + (42f - 16.5f) * s;
        for (float ex : new float[] {13.5f, 20.5f}) {
            glow(batch, x + (ex - 17f) * s, y, 7f * s, RED, 0.6f * Math.min(1f, strength));
        }
    }

    /**
     * Son haut-de-forme, ôté de sa tête (à dessiner sur sa tête nue) : décalé de ({@code dx}, {@code dy})
     * et tourné de {@code rotation} degrés. Sans décalage, il retombe pile à sa place sur le portrait.
     */
    private void drawHat(Batch batch, float x, float bottom, float s, float dx, float dy, float rotation) {
        // Le chapeau rogné : colonnes 4 à 29, lignes 0 à 11 du portrait ; son centre est en (17, 6).
        batch.setColor(Color.WHITE);
        sprite(batch, hat, x + dx, bottom + (42f - 6f) * s + dy, s, rotation);
    }

    // =========================================================================
    // Outils
    // =========================================================================

    /** Le contenu d'une fenêtre de rouleau d'une machine. */
    private interface ReelPainter {
        void paint(int index, float x, float y, float w, float h);
    }

    /**
     * Une machine à sous, en pixel art ({@code U} : taille d'un pixel), posée en ({@code cx}, {@code bottom}) :
     * levier, corps aux couleurs {@code light}/{@code body}/{@code dark}, dôme à gradins, ampoules,
     * {@code reels} rouleaux et plateau à pièces ; {@code lit} allume ses ampoules.
     */
    private void machine(Batch batch, float cx, float bottom, float U, Color light, Color body, Color dark,
                         int reels, float lit, ReelPainter painter) {
        float reelW = 9f * U, reelH = 11f * U, gap = 1.5f * U;
        float panelW = reels * reelW + (reels + 1) * gap;
        float w = panelW + 6f * U, h = reelH + 2f * gap + 15f * U, left = cx - w / 2f;
        // Le levier, derrière le corps.
        float pivotX = left + w, pivotY = bottom + h * 0.5f, length = 11f * U, angle = 78f;
        rect(batch, OUT, pivotX - 0.5f * U, pivotY - 2.5f * U, 3f * U, 5f * U);
        rect(batch, GOLD_DARK, pivotX, pivotY - 2f * U, 2f * U, 4f * U);
        tint(batch, OUT);
        batch.draw(pixel, pivotX + U, pivotY - U, 0f, U, length, 2f * U, 1f, 1f, angle);
        tint(batch, hex("d8d8e0"));
        batch.draw(pixel, pivotX + U, pivotY - 0.4f * U, 0f, 0.4f * U, length, 0.8f * U, 1f, 1f, angle);
        float knobX = pivotX + U + MathUtils.cosDeg(angle) * length, knobY = pivotY + MathUtils.sinDeg(angle) * length;
        rect(batch, OUT, knobX - 2f * U, knobY - 2f * U, 4f * U, 4f * U);
        rect(batch, RED, knobX - 1.5f * U, knobY - 1.5f * U, 3f * U, 3f * U);
        // Le corps.
        rect(batch, OUT, left - U, bottom - U, w + 2f * U, h + 2f * U);
        rect(batch, body, left, bottom, w, h);
        rect(batch, light, left, bottom + h - 3f * U, w, 2f * U);
        rect(batch, light, left, bottom, 1.5f * U, h);
        rect(batch, dark, left + w - 2f * U, bottom, 2f * U, h);
        rect(batch, dark, left, bottom, w, 1.5f * U);
        // Le dôme à gradins et sa pierre.
        for (int step = 0; step < 3; step++) {
            float sw = w - (step + 1) * 5f * U, sy = bottom + h + step * 2.5f * U;
            rect(batch, OUT, cx - sw / 2f - U, sy, sw + 2f * U, 3.5f * U);
            rect(batch, step % 2 == 0 ? body : light, cx - sw / 2f, sy, sw, 2.5f * U);
        }
        float gemY = bottom + h + 8.5f * U;
        rect(batch, OUT, cx - 1.5f * U, gemY - 1.5f * U, 3f * U, 3f * U);
        rect(batch, lit > 0.5f ? BULB : GOLD_DARK, cx - U, gemY - U, 2f * U, 2f * U);
        if (lit > 0.5f) glow(batch, cx, gemY, 10f * U, GOLD_LIGHT, 0.5f * lit);
        // Les ampoules, en haut et en bas du corps.
        int bulbs = Math.max(4, (int) (w / (3f * U)));
        for (int i = 0; i < bulbs; i++) {
            float x = left + (i + 0.5f) * w / bulbs;
            boolean on = lit > 0.5f && ((int) (time * 7f) + i) % 2 == 0;
            for (float y : new float[] {bottom + h - 1.5f * U, bottom + 3f * U}) {
                rect(batch, OUT, x - 0.75f * U, y - 0.75f * U, 1.5f * U, 1.5f * U);
                rect(batch, on ? BULB : GOLD_DARK, x - 0.35f * U, y - 0.35f * U, 0.7f * U, 0.7f * U);
                if (on) glow(batch, x, y, 4f * U, GOLD, 0.45f * bright);
            }
        }
        // Le panneau des rouleaux.
        float reelY = bottom + 7.5f * U;
        rect(batch, OUT, cx - panelW / 2f - U, reelY - gap - U, panelW + 2f * U, reelH + 2f * gap + 2f * U);
        rect(batch, GOLD_DARK, cx - panelW / 2f, reelY - gap, panelW, reelH + 2f * gap);
        for (int i = 0; i < reels; i++) {
            float x = cx - panelW / 2f + gap + i * (reelW + gap);
            rect(batch, OUT, x, reelY, reelW, reelH);
            float glass = 0.25f + 0.75f * lit;
            batch.setColor(GLASS.r * glass * bright, GLASS.g * glass * bright, GLASS.b * glass * bright, 1f);
            batch.draw(pixel, x + 0.5f * U, reelY + 0.5f * U, reelW - U, reelH - U);
            float pad = 1.2f * U;
            painter.paint(i, x + pad, reelY + (reelH - (reelW - 2f * pad)) / 2f, reelW - 2f * pad, reelW - 2f * pad);
            rect(batch, hex("00000030"), x + 0.5f * U, reelY + reelH - 2f * U, reelW - U, 1.5f * U);   // l'ombre du haut
        }
        rect(batch, RED, cx - panelW / 2f - 1.8f * U, reelY + reelH / 2f - 0.4f * U, 0.8f * U, 0.8f * U);
        rect(batch, RED, cx + panelW / 2f + U, reelY + reelH / 2f - 0.4f * U, 0.8f * U, 0.8f * U);
        // Le plateau à pièces.
        float trayW = w * 0.55f, trayY = bottom + 1.5f * U;
        rect(batch, OUT, cx - trayW / 2f, trayY + 3f * U, trayW, 0.6f * U);
        for (int i = 0; i < 5; i++) {
            rect(batch, GOLD, cx - trayW / 2f + (1f + i * trayW / 5.5f / U) * U, trayY + 3.6f * U, 1.4f * U, (0.6f + (i % 3) * 0.4f) * U);
        }
    }

    /** Une carte du jeu, centrée en ({@code x}, {@code y}), de hauteur {@code height} ; {@code flip} : 0 dos, 1 face. */
    private void card(Batch batch, TextureRegion face, float x, float y, float height, float rotation, float flip, boolean his) {
        float squash = Math.max(0.04f, Math.abs(MathUtils.cos(flip * MathUtils.PI)));
        TextureRegion region = flip < 0.5f ? back : face;
        float width = height * CARD_RATIO;
        batch.setColor(0f, 0f, 0f, 0.35f);
        batch.draw(region, x - width * squash / 2f + 0.03f * height, y - height / 2f - 0.03f * height,
            width * squash / 2f, height / 2f, width * squash, height, 1f, 1f, rotation);
        if (his && flip >= 0.5f) batch.setColor(0.62f, 0.55f, 0.78f, 1f);    // ses cartes : sombres, comme au combat
        else batch.setColor(Color.WHITE);
        batch.draw(region, x - width * squash / 2f, y - height / 2f, width * squash / 2f, height / 2f,
            width * squash, height, 1f, 1f, rotation);
    }

    /** Ses répliques, en grand, en haut de l'écran. */
    private void drawCaptions(Batch batch, float w, float h) {
        switch (scene) {
            case INTRO -> {
                line(batch, Lang.t("« TU AS APPRIS TOUT CE QUE JE SAVAIS. »"), w / 2f, h * 0.9f, progress(LINE_1, LINE_1 + 0.3f));
                line(batch, Lang.t("« VOYONS SI TU SAIS GAGNER. »"), w / 2f, h * 0.81f, progress(LINE_2, LINE_2 + 0.3f));
            }
            case RIG -> line(batch, Lang.t("IL TRUQUE TA TABLE !"), w / 2f, h * 0.93f, progress(RIG_CAPTION, RIG_CAPTION + 0.25f));
            case OPEN -> line(batch, Lang.t("CARTES SUR TABLE !"), w / 2f, h * 0.93f, progress(OPEN_CAPTION, OPEN_CAPTION + 0.25f));
            case ENDING -> {
                if (time >= REVERSE_CUT) {
                    line(batch, Lang.t("« À TOI DE DISTRIBUER. »"), w / 2f, h * 0.9f, progress(ENDING_CAPTION, ENDING_CAPTION + 0.4f));
                }
            }
        }
    }

    private void line(Batch batch, String text, float x, float y, float a) {
        if (font == null || a <= 0f) return;
        float scale = font.getData().scaleX;
        font.getData().setScale(scale * 2f);
        caption(batch, font, text, x, y, a);
        font.getData().setScale(scale);
    }

    /** La caméra : le point ({@code cx}, {@code cy}) au centre de l'écran, agrandi {@code zoom} fois. */
    private void camera(Batch batch, float cx, float cy, float zoom) {
        saved.set(batch.getTransformMatrix());
        view.set(saved).translate(worldWidth() / 2f, worldHeight() / 2f, 0f).scale(zoom, zoom, 1f).translate(-cx, -cy, 0f);
        batch.setTransformMatrix(view);
    }

    private void endCamera(Batch batch) {
        batch.setTransformMatrix(saved);
    }

    private void tint(Batch batch, Color color) {
        batch.setColor(color.r * bright, color.g * bright, color.b * bright, color.a * alpha);
    }

    private void rect(Batch batch, Color color, float x, float y, float w, float h) {
        tint(batch, color);
        batch.draw(pixel, x, y, w, h);
    }
}
