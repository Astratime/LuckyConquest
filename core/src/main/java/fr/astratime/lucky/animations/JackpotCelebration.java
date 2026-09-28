package fr.astratime.lucky.animations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.Textures;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.settings.VisualSettings;
import fr.astratime.lucky.views.PlayArea;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * Célébration du jackpot (Bingo), par-dessus tout l'écran de jeu. Elle change
 * selon le symbole aligné : chacun a les couleurs de sa bannière « BINGO!!! »
 * et de ses éclairs (voir {@link BingoStyle}) et sa propre mise en scène.
 *
 * <ul>
 *   <li>Triple Sept : flash blanc, pluie de pièces d'or (dont une partie file
 *       vers le compteur des gains) et feux d'artifice multicolores ;</li>
 *   <li>Sept : fournaise — des braises montent de la table, des 7 jaillissent
 *       et des fusées rouge et orange éclatent ;</li>
 *   <li>Double Bar : une averse de barres d'acier s'écrase sur la table en
 *       soulevant des ondes de poussière ;</li>
 *   <li>Bar : trois rangées de barres tombent d'un bloc, chaque volée fait
 *       trembler l'écran ;</li>
 *   <li>Cerise : une fontaine de cerises jaillit du centre de la table ;</li>
 *   <li>Triple Cerise : une flèche se plante en plein centre d'une cible et
 *       « BINGO! » s'écrit en géant (voir {@link BullseyeAnimation}) ;</li>
 *   <li>Raisin : des bulles violettes montent pendant que des grappes tombent
 *       mollement ;</li>
 *   <li>Cloche : une mailloche frappe une cloche d'église qui se balance en
 *       projetant des clochettes, et « BINGO! » s'écrit en géant (voir
 *       {@link ChurchBellAnimation}) ;</li>
 *   <li>Diamant : l'écran scintille de toutes parts et des diamants tombent
 *       lentement ;</li>
 *   <li>Lingot : une avalanche de lingots et de pièces, presque toutes
 *       ramassées par le compteur ;</li>
 *   <li>Pastèque : des pastèques sont lancées des deux côtés et éclaboussent
 *       l'écran de jus et de pépins.</li>
 * </ul>
 *
 * Pendant {@link #DURATION}, le calque intercepte les clics : le joueur ne peut
 * pas continuer avant la fin. Les dernières particules finissent ensuite de
 * tomber sans bloquer le jeu.
 */
public class JackpotCelebration extends Group implements Disposable {

    /** Durée pendant laquelle le jeu attend la fin de la célébration. */
    public static final float DURATION = 3f;

    private static final float FLASH_ALPHA = 0.7f;
    private static final float FLASH_TIME  = 0.35f;
    private static final float SHAKE_TIME  = 0.45f;
    private static final float SHAKE       = 12f;

    private static final int   COIN_COUNT      = 90;
    private static final float COIN_SPAWN_TIME = 1.4f;
    private static final int   COLLECT_EVERY   = 3;      // une pièce sur trois file vers le compteur
    private static final float COIN_FLOOR_MIN  = 0.14f;  // table : fractions de la hauteur de l'écran
    private static final float COIN_FLOOR_MAX  = 0.48f;

    // Fusées : les premières éclatent bas, sous la bannière ; les suivantes, côté
    // adverse de la table, une fois la bannière sortie (elles montent ~0,8 s).
    private static final float[] ROCKET_TIMES      = {0.3f, 0.55f, 1.1f, 1.3f, 1.5f, 1.7f, 1.9f, 2.1f, 2.3f, 2.5f};
    private static final float   LOW_ROCKETS_UNTIL = 1f;
    private static final float   ROCKET_FROM       = 0.1f;   // départ : bas de la table
    private static final float   LOW_APEX_MIN      = 0.36f;  // explosion sous la bannière
    private static final float   LOW_APEX_MAX      = 0.46f;
    private static final float   APEX_MIN          = 0.5f;   // explosion côté adverse de la table
    private static final float   APEX_MAX          = 0.88f;
    private static final float   ROCKET_SIDE_MARGIN = 120f;

    private static final float BANNER_Y = 0.72f;         // centre de la bannière (fraction de la hauteur)
    private static final float BULLSEYE_Y      = 0.4f;   // centre de la cible du Triple Cerise
    private static final float BULLSEYE_WORD_Y = 0.8f;   // mot « BINGO! » géant, au-dessus de la cible
    private static final float BELL_PIVOT_Y    = 0.86f;  // axe de la cloche d'église, sous le haut de l'écran
    private static final float BELL_WORD_Y     = 0.22f;  // mot « BINGO! » géant, sous la cloche
    /** Le mot « BINGO! » géant commence à s'estomper, avec son décor. */
    private static final float WORD_FADE_AT    = 2.45f;

    /**
     * Apparitions étalées dans le temps : {@code count} appels de {@code spawn}
     * répartis entre {@code start} et {@code end} (tous d'un coup s'ils sont égaux).
     */
    private record Emitter(float start, float end, int count, IntConsumer spawn) {}

    private final PlayArea       playArea;
    private final ScreenShake    screenShake;
    private final VisualSettings settings;
    private final Texture        pixelTexture = Textures.solidColor(Color.WHITE);
    private final TextureRegion  pixel        = new TextureRegion(pixelTexture);
    private final Texture        coinTexture  = new Texture(Gdx.files.internal("jackpot/coin_spin.png"));
    private final Texture        bandTexture  = new Texture(Gdx.files.internal("jackpot/banner_band.png"));
    private final List<Texture>  styleBands   = new ArrayList<>();
    /** Image de chaque symbole, détourée de sa case blanche, chargée à son premier Bingo. */
    private final Map<Symbol, Texture> icons  = new EnumMap<>(Symbol.class);
    private final BitmapFont     bannerFont   = Fonts.jersey(170, Color.WHITE, 7f, Color.valueOf("12080aff"));

    private final Image        flash;
    private final Shockwaves   shockwaves;
    private final Glitter      glitter;
    private final Fireworks    fireworks;
    private final SymbolShower symbols;
    private final CoinShower   coins;
    private final BullseyeAnimation bullseye  = new BullseyeAnimation();
    private final ChurchBellAnimation churchBell = new ChurchBellAnimation();
    private final GiantWord         giantWord = new GiantWord();
    private final Group        bannerLayer = new Group();
    /** Une bannière par symbole, créée à son premier Bingo (ses lettres ont les couleurs du symbole). */
    private final Map<Symbol, BingoBanner> banners = new EnumMap<>(Symbol.class);

    private final List<Emitter> emitters = new ArrayList<>();
    private int[]      emitted = new int[0];
    private BingoStyle style;
    /** Couleur des ondes soulevées par un symbole lourd qui touche la table, et secousse de l'impact. */
    private Color      landingColor = Color.WHITE;
    private float      landingShake;
    /** false quand la mise en scène remplace la bannière « BINGO!!! » (Triple Cerise). */
    private boolean    showBanner;

    private boolean  running;
    private float    elapsed;
    private Runnable onFinished;

    /**
     * @param coinTarget      position (Stage) de la pièce du compteur des gains
     * @param onCoinCollected appelé à l'arrivée de chaque pièce sur le compteur
     * @param settings        effets réduits : pas de flash (la secousse est coupée par {@code screenShake})
     */
    public JackpotCelebration(PlayArea playArea, ScreenShake screenShake, VisualSettings settings,
                              Supplier<Vector2> coinTarget, Runnable onCoinCollected) {
        this.playArea      = playArea;
        this.screenShake   = screenShake;
        this.settings      = settings;
        setTouchable(Touchable.disabled);

        shockwaves = new Shockwaves(pixel);
        glitter    = new Glitter(pixel);
        fireworks  = new Fireworks(pixel);
        symbols    = new SymbolShower();
        coins      = new CoinShower(new TextureRegion(coinTexture), coinTarget, onCoinCollected);
        flash      = new Image(new TextureRegionDrawable(pixel));
        flash.setTouchable(Touchable.disabled);
        flash.getColor().a = 0f;
        bannerLayer.setTouchable(Touchable.disabled);
        symbols.setLandingListener(this::onSymbolLanded);
        churchBell.setSwingListener(this::onBellSwing);

        addActor(shockwaves);
        addActor(glitter);
        addActor(fireworks);
        addActor(symbols);
        addActor(coins);
        addActor(bullseye);
        addActor(churchBell);
        addActor(bannerLayer);
        addActor(giantWord);
        addActor(flash);
    }

    /**
     * Lance la célébration du Bingo de {@code symbol} ; {@code onFinished} est
     * appelé au bout de {@link #DURATION}.
     */
    public void play(Symbol symbol, Runnable onFinished) {
        cancel();
        this.onFinished = onFinished;
        running         = true;
        elapsed         = 0f;
        style           = BingoStyle.of(symbol);

        float width  = getStage().getViewport().getWorldWidth();
        float height = getStage().getViewport().getWorldHeight();
        setBounds(0f, 0f, width, height);
        setTouchable(Touchable.enabled); // intercepte les clics jusqu'à la fin

        if (!settings.isReducedEffects()) {
            flash.setBounds(0f, 0f, width, height);
            flash.setColor(style.flash().r, style.flash().g, style.flash().b, FLASH_ALPHA);
            flash.addAction(Actions.fadeOut(FLASH_TIME));
        }

        emitters.clear();
        landingColor = Color.WHITE;
        landingShake = 0f;
        showBanner   = true;
        schedule(symbol, iconOf(symbol));
        emitted = new int[emitters.size()];

        if (showBanner) banner(symbol).play(playArea.getCenterX(), height * BANNER_Y, width);
    }

    /** Arrête tout immédiatement (nouvelle partie), sans appeler la fin de la célébration. */
    public void cancel() {
        running = false;
        setTouchable(Touchable.disabled);
        emitters.clear();
        coins.removeAll();
        fireworks.removeAll();
        symbols.removeAll();
        glitter.removeAll();
        shockwaves.removeAll();
        for (BingoBanner banner : banners.values()) banner.hide();
        bullseye.hide();
        churchBell.hide();
        giantWord.hide();
        flash.clearActions();
        flash.getColor().a = 0f;
        screenShake.stop();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!running) return;
        elapsed += delta;

        for (int i = 0; i < emitters.size(); i++) {
            Emitter emitter = emitters.get(i);
            float progress = emitter.end() <= emitter.start()
                ? (elapsed >= emitter.start() ? 1f : 0f)
                : MathUtils.clamp((elapsed - emitter.start()) / (emitter.end() - emitter.start()), 0f, 1f);
            int due = Math.round(emitter.count() * progress);
            for (; emitted[i] < due; emitted[i]++) emitter.spawn().accept(emitted[i]);
        }

        if (elapsed >= DURATION) {
            running = false;
            setTouchable(Touchable.disabled);
            onFinished.run();
        }
    }

    // -------------------------------------------------------------------------
    // Mises en scène
    // -------------------------------------------------------------------------

    /** Programme les effets du Bingo de {@code symbol}, dont l'image est {@code icon}. */
    private void schedule(Symbol symbol, TextureRegion icon) {
        switch (symbol) {
            case SEVEN         -> scheduleFurnace(icon);
            case DOUBLE_BAR    -> scheduleSteelRain(icon);
            case BAR           -> scheduleHammer(icon);
            case CHERRY        -> scheduleCherryFountain(icon);
            case TRIPLE_CHERRY -> scheduleBullseye(icon);
            case GRAPE         -> scheduleBubbles(icon);
            case BELL          -> scheduleChime(icon);
            case DIAMOND       -> scheduleSparkle(icon);
            case GOLD_BAR      -> scheduleGoldAvalanche(icon);
            case WATERMELON    -> scheduleSplash(icon);
            case TRIPLE_SEVEN, JOKER -> scheduleCasino();
        }
    }

    /** Triple Sept, la célébration d'origine : pluie de pièces et feux d'artifice multicolores. */
    private void scheduleCasino() {
        at(0f, () -> screenShake.shake(SHAKE_TIME, SHAKE));
        coinRain(0f, COIN_SPAWN_TIME, COIN_COUNT, COLLECT_EVERY);
        for (float time : ROCKET_TIMES) {
            boolean low = time < LOW_ROCKETS_UNTIL;
            at(time, () -> rocket(low));
        }
    }

    /** Sept : braises qui montent de la table, 7 qui jaillissent, fusées rouge et orange. */
    private void scheduleFurnace(TextureRegion icon) {
        at(0f, () -> screenShake.shake(0.6f, 16f));
        Color[] fire = {Color.valueOf("ff3b1fff"), Color.valueOf("ff8a1fff"), Color.valueOf("ffd54aff")};
        emit(0f, 2.6f, 240, i -> glitter.ember(tableX(0f), getHeight() * MathUtils.random(0.05f, 0.2f),
            MathUtils.random(-30f, 30f), MathUtils.random(160f, 420f), 0f,
            MathUtils.random(5f, 11f), MathUtils.random(1f, 1.8f), fire[i % fire.length]));
        emit(0.05f, 1.2f, 16, i -> symbols.jet(icon, tableX(80f), -60f, MathUtils.random(75f, 105f),
            MathUtils.random(1150f, 1500f), MathUtils.random(70f, 100f), 1500f));
        for (float time : new float[] {1.0f, 1.4f, 1.8f, 2.2f, 2.5f}) at(time, () -> rocket(false));
        coinRain(0.2f, 1.2f, 18, 1);
    }

    /** Double Bar : averse de barres d'acier qui s'écrasent sur la table en soulevant de la poussière. */
    private void scheduleSteelRain(TextureRegion icon) {
        at(0f, () -> screenShake.shake(SHAKE_TIME, 8f));
        landingColor = Color.valueOf("c8d2dcff");
        landingShake = 5f;
        emit(0.15f, 1.9f, 34, i -> symbols.rain(icon, tableX(60f), getHeight() + 60f, floorY(),
            MathUtils.random(70f, 105f), MathUtils.random(500f, 900f), 2600f, true));
        emit(0.3f, 2.4f, 40, i -> glitter.twinkle(tableX(0f), getHeight() * MathUtils.random(0.15f, 0.9f),
            MathUtils.random(14f, 26f), 0.5f, Color.valueOf("8fd3ffff")));
        coinRain(0.2f, 1.2f, 18, 1);
    }

    /** Bar : trois rangées de barres tombent d'un bloc ; chaque volée secoue l'écran. */
    private void scheduleHammer(TextureRegion icon) {
        landingColor = Color.WHITE;
        landingShake = 14f;
        float[] volleys = {0.2f, 0.85f, 1.5f};
        int perVolley = 7;
        for (int v = 0; v < volleys.length; v++) {
            float floor = getHeight() * (0.16f + v * 0.1f); // chaque rangée se pose un peu plus haut
            emit(volleys[v], volleys[v], perVolley, i -> {
                float step = playArea.getWidth() / (perVolley + 1);
                symbols.rain(icon, playArea.getX() + step * (i + 1), getHeight() + 80f, floor,
                    95f, 1400f, 3200f, true);
            });
        }
        coinRain(0.3f, 1.4f, 18, 1);
    }

    /** Cerise : une fontaine de cerises jaillit du centre de la table, sous des gerbes roses. */
    private void scheduleCherryFountain(TextureRegion icon) {
        at(0f, () -> screenShake.shake(SHAKE_TIME, 8f));
        float centerX = playArea.getCenterX();
        emit(0f, 1.8f, 44, i -> symbols.jet(icon, centerX + MathUtils.random(-40f, 40f), -40f,
            MathUtils.random(62f, 118f), MathUtils.random(1100f, 1500f), MathUtils.random(60f, 90f), 1400f));
        emit(0f, 1.8f, 90, i -> glitter.ember(centerX + MathUtils.random(-30f, 30f), 0f,
            MathUtils.random(-260f, 260f), MathUtils.random(700f, 1100f), 900f,
            MathUtils.random(5f, 9f), MathUtils.random(1.2f, 1.8f), style.fireworks()[i % style.fireworks().length]));
        for (float time : new float[] {1.0f, 1.4f, 1.8f, 2.2f}) at(time, () -> rocket(false));
        coinRain(0.2f, 1.2f, 18, 1);
    }

    /**
     * Triple Cerise : une cible surgit au milieu de la table, une flèche se plante
     * en plein centre et « BINGO! » s'écrit en géant (à la place de la bannière).
     * L'impact secoue l'écran et fait exploser des cerises tout autour.
     */
    private void scheduleBullseye(TextureRegion icon) {
        useGiantWord();
        float centerX = playArea.getCenterX();
        float centerY = getHeight() * BULLSEYE_Y;
        bullseye.play(centerX, centerY);
        giantWord.play(centerX, getHeight() * BULLSEYE_WORD_Y, BullseyeAnimation.IMPACT_TIME + 0.12f, WORD_FADE_AT,
            style.letterA(), style.letterB());

        // Traînée d'étincelles derrière la flèche en vol.
        float launch = BullseyeAnimation.arrowLaunchTime();
        Vector2 tip = new Vector2();
        emit(launch, BullseyeAnimation.IMPACT_TIME, 24, i -> {
            bullseye.arrowTipAt(elapsed, tip);
            glitter.ember(tip.x, tip.y, 0f, 0f, 0f, MathUtils.random(5f, 8f), 0.35f, Color.WHITE);
        });

        float impact = BullseyeAnimation.IMPACT_TIME;
        at(impact, () -> {
            screenShake.shake(0.45f, 16f);
            if (!settings.isReducedEffects()) {
                flash.clearActions();
                flash.setColor(1f, 1f, 1f, 0.55f);
                flash.addAction(Actions.fadeOut(0.25f));
            }
        });
        Color[] palette = style.fireworks();
        for (int k = 0; k < 3; k++) {
            Color color = palette[k % palette.length];
            at(impact + k * 0.12f, () -> shockwaves.ring(centerX, centerY, 330f, 0.7f, 1f, color));
        }
        // Explosion de cerises : un anneau rapide de triples cerises, un anneau plus lent de
        // cerises simples, une seconde salve, puis une pluie de cerises qui retombe sur la table.
        TextureRegion cherry = iconOf(Symbol.CHERRY);
        emit(impact, impact, 32, i -> symbols.jet(icon, centerX, centerY, i * 360f / 32 + MathUtils.random(-5f, 5f),
            MathUtils.random(900f, 1300f), MathUtils.random(60f, 85f), 1300f));
        emit(impact, impact, 28, i -> symbols.jet(cherry, centerX, centerY, i * 360f / 28 + MathUtils.random(-6f, 6f),
            MathUtils.random(450f, 800f), MathUtils.random(45f, 65f), 1100f));
        emit(impact + 0.15f, impact + 0.35f, 30, i -> symbols.jet(i % 2 == 0 ? icon : cherry, centerX, centerY,
            MathUtils.random(360f), MathUtils.random(600f, 1200f), MathUtils.random(50f, 80f), 1300f));
        emit(impact + 0.3f, impact + 1.3f, 36, i -> symbols.rain(i % 3 == 0 ? icon : cherry, tableX(40f),
            getHeight() + 60f, floorY(), MathUtils.random(45f, 75f), MathUtils.random(150f, 400f), 900f, false));
        emit(impact, impact, 70, i -> {
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(250f, 750f);
            glitter.ember(centerX, centerY, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed, 700f,
                MathUtils.random(6f, 11f), MathUtils.random(0.8f, 1.4f), palette[i % palette.length]);
        });
        for (float time : new float[] {1.5f, 1.8f, 2.1f, 2.4f}) at(time, () -> rocket(false));
        coinRain(impact, impact + 1f, 18, 1);
    }

    /** Raisin : des bulles violettes montent pendant que des grappes tombent mollement. */
    private void scheduleBubbles(TextureRegion icon) {
        at(0f, () -> screenShake.shake(SHAKE_TIME, 6f));
        at(0f, () -> shockwaves.ring(playArea.getCenterX(), getHeight() * BANNER_Y,
            playArea.getWidth() * 0.6f, 0.9f, 1f, Color.valueOf("c77dffff")));
        Color[] purples = style.fireworks();
        emit(0f, 2.7f, 170, i -> glitter.ember(tableX(0f), getHeight() * MathUtils.random(-0.05f, 0.15f),
            MathUtils.random(-15f, 15f), MathUtils.random(90f, 220f), 0f, MathUtils.random(8f, 18f),
            MathUtils.random(2f, 3f), purples[i % purples.length]));
        emit(0.1f, 2.0f, 26, i -> symbols.rain(icon, tableX(60f), getHeight() + 60f, floorY(),
            MathUtils.random(70f, 100f), MathUtils.random(60f, 160f), 380f, false));
        coinRain(0.2f, 1.2f, 18, 1);
    }

    /**
     * Cloche : une cloche d'église est frappée par une mailloche puis se balance ;
     * « BINGO! » s'écrit en géant sous elle (à la place de la bannière) et, à
     * chaque balancement, elle sonne et projette des clochettes (voir {@link #onBellSwing}).
     */
    private void scheduleChime(TextureRegion icon) {
        useGiantWord();
        float centerX = playArea.getCenterX();
        churchBell.play(centerX, getHeight() * BELL_PIVOT_Y);
        float strike = ChurchBellAnimation.STRIKE_TIME;
        giantWord.play(centerX, getHeight() * BELL_WORD_Y, strike + 0.12f, WORD_FADE_AT,
            style.letterA(), style.letterB());

        Color gold = Color.valueOf("ffe680ff");
        at(strike, () -> {
            screenShake.shake(0.35f, 12f);
            if (!settings.isReducedEffects()) {
                flash.clearActions();
                flash.setColor(1f, 0.95f, 0.75f, 0.45f);
                flash.addAction(Actions.fadeOut(0.25f));
            }
            Vector2 hit = churchBell.strikePoint();
            for (int k = 0; k < 10; k++) {
                float angle = MathUtils.random(-70f, 70f);
                float speed = MathUtils.random(250f, 600f);
                glitter.ember(hit.x, hit.y, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed, 900f,
                    MathUtils.random(5f, 9f), MathUtils.random(0.4f, 0.7f), Color.WHITE);
            }
        });
        for (int k = 0; k < 3; k++) {
            at(strike + k * 0.12f, () -> {
                Vector2 hit = churchBell.strikePoint();
                shockwaves.ring(hit.x, hit.y, 260f, 0.8f, 1f, gold);
            });
        }
        // Clochettes qui tombent de la bouche pendant tout le balancement.
        emit(strike + 0.1f, 2.3f, 26, i -> {
            Vector2 mouth = churchBell.mouth();
            symbols.jet(icon, mouth.x, mouth.y, MathUtils.random(200f, 340f), MathUtils.random(150f, 450f),
                MathUtils.random(60f, 85f), 1100f);
        });
        emit(0.3f, 2.6f, 50, i -> glitter.twinkle(tableX(0f), getHeight() * MathUtils.random(0.05f, 0.95f),
            MathUtils.random(12f, 24f), 0.6f, gold));
        coinRain(strike, strike + 1.2f, 30, 2);
    }

    /**
     * La cloche d'église atteint l'extrémité d'un balancement : elle sonne (onde
     * dorée, petite secousse) et projette une gerbe de clochettes du côté où
     * elle penche.
     */
    private void onBellSwing(float mouthX, float mouthY, int direction) {
        if (!running) return;
        shockwaves.ring(mouthX, mouthY, 200f, 0.7f, 0.6f, Color.valueOf("ffe680ff"));
        screenShake.shake(0.12f, 5f);
        TextureRegion bellIcon = iconOf(Symbol.BELL);
        for (int k = 0; k < 9; k++) {
            float angle = direction < 0 ? MathUtils.random(110f, 170f) : MathUtils.random(10f, 70f);
            symbols.jet(bellIcon, mouthX, mouthY, angle, MathUtils.random(550f, 1000f),
                MathUtils.random(70f, 100f), 1300f);
        }
        for (int k = 0; k < 6; k++) {
            glitter.twinkle(mouthX + MathUtils.random(-80f, 80f), mouthY + MathUtils.random(-40f, 60f),
                MathUtils.random(14f, 26f), 0.5f, Color.WHITE);
        }
    }

    /** Diamant : l'écran scintille de toutes parts et des diamants tombent lentement. */
    private void scheduleSparkle(TextureRegion icon) {
        at(0f, () -> screenShake.shake(SHAKE_TIME, 6f));
        Color[] ice = style.fireworks();
        emit(0f, 2.7f, 180, i -> glitter.twinkle(tableX(0f), getHeight() * MathUtils.random(0.05f, 0.98f),
            MathUtils.random(14f, 34f), MathUtils.random(0.4f, 0.8f), ice[i % ice.length]));
        emit(0.1f, 1.9f, 26, i -> symbols.rain(icon, tableX(60f), getHeight() + 60f, floorY(),
            MathUtils.random(60f, 95f), MathUtils.random(80f, 200f), 520f, false));
        for (float time : new float[] {1.2f, 1.6f, 2.0f, 2.4f}) at(time, () -> rocket(false));
        coinRain(0.2f, 1.2f, 18, 1);
    }

    /** Lingot : avalanche de lingots et de pièces, presque toutes ramassées par le compteur. */
    private void scheduleGoldAvalanche(TextureRegion icon) {
        at(0f, () -> screenShake.shake(0.6f, 14f));
        landingColor = Color.valueOf("ffd54aff");
        landingShake = 4f;
        emit(0.1f, 1.8f, 38, i -> symbols.rain(icon, tableX(60f), getHeight() + 60f, floorY(),
            MathUtils.random(75f, 105f), MathUtils.random(400f, 800f), 2400f, true));
        coinRain(0f, 1.8f, 140, 2);
        emit(0.3f, 2.5f, 50, i -> glitter.twinkle(tableX(0f), getHeight() * MathUtils.random(0.1f, 0.6f),
            MathUtils.random(12f, 22f), 0.5f, Color.valueOf("fffbe0ff")));
    }

    /** Pastèque : des pastèques sont lancées des deux côtés et éclaboussent l'écran de jus et de pépins. */
    private void scheduleSplash(TextureRegion icon) {
        at(0f, () -> screenShake.shake(SHAKE_TIME, 10f));
        float left  = playArea.getX() + 30f;
        float right = playArea.getX() + playArea.getWidth() - 30f;
        emit(0f, 1.8f, 34, i -> {
            boolean fromLeft = i % 2 == 0;
            float x = fromLeft ? left : right;
            symbols.jet(icon, x, -40f, fromLeft ? MathUtils.random(50f, 70f) : MathUtils.random(110f, 130f),
                MathUtils.random(1200f, 1550f), MathUtils.random(75f, 105f), 1300f);
            splash(x, fromLeft);
        });
        for (float time : new float[] {1.1f, 1.5f, 1.9f, 2.3f}) at(time, () -> rocket(false));
        coinRain(0.2f, 1.2f, 18, 1);
    }

    /** Gerbe de jus (rose) et de pépins (noirs) lancée d'un coin de la table avec une pastèque. */
    private void splash(float x, boolean fromLeft) {
        Color juice = Color.valueOf("ff5a78ff");
        Color seed  = Color.valueOf("1a1a1aff");
        for (int k = 0; k < 8; k++) {
            float angle = fromLeft ? MathUtils.random(40f, 80f) : MathUtils.random(100f, 140f);
            float speed = MathUtils.random(700f, 1200f);
            boolean isSeed = k % 3 == 0;
            glitter.ember(x, 0f, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed, 1300f,
                isSeed ? 6f : MathUtils.random(7f, 12f), MathUtils.random(1.2f, 1.8f), isSeed ? seed : juice);
        }
    }

    // -------------------------------------------------------------------------
    // Outils des mises en scène
    // -------------------------------------------------------------------------

    /**
     * La mise en scène a son propre décor et écrit « BINGO! » en géant : pas de
     * bannière, ni de flash d'entrée (celui de l'impact suffit).
     */
    private void useGiantWord() {
        showBanner = false;
        flash.clearActions();
        flash.getColor().a = 0f;
    }

    /** Programme {@code count} apparitions réparties entre {@code start} et {@code end}. */
    private void emit(float start, float end, int count, IntConsumer spawn) {
        emitters.add(new Emitter(start, end, count, spawn));
    }

    /** Programme {@code action} à l'instant {@code time}. */
    private void at(float time, Runnable action) {
        emit(time, time, 1, i -> action.run());
    }

    /** Pluie de {@code count} pièces entre {@code start} et {@code end} ; une sur {@code collectEvery} file vers le compteur. */
    private void coinRain(float start, float end, int count, int collectEvery) {
        emit(start, end, count, i -> coins.drop(tableX(0f), getHeight(), floorY(), i % collectEvery == 0));
    }

    /** Fusée aux couleurs du style ; {@code low} : elle éclate sous la bannière. */
    private void rocket(boolean low) {
        float x    = tableX(ROCKET_SIDE_MARGIN);
        float apex = low ? MathUtils.random(LOW_APEX_MIN, LOW_APEX_MAX) : MathUtils.random(APEX_MIN, APEX_MAX);
        if (style.fireworks() != null) {
            fireworks.launch(x, getHeight() * ROCKET_FROM, getHeight() * apex, style.fireworks());
        } else {
            fireworks.launch(x, getHeight() * ROCKET_FROM, getHeight() * apex);
        }
    }

    /** Un symbole lourd touche la table : onde de poussière au sol, étincelles et petite secousse. */
    private void onSymbolLanded(float x, float y) {
        shockwaves.ring(x, y, 110f, 0.45f, 0.28f, landingColor);
        for (int k = 0; k < 5; k++) {
            glitter.ember(x + MathUtils.random(-30f, 30f), y, MathUtils.random(-220f, 220f),
                MathUtils.random(120f, 320f), 1100f, 5f, MathUtils.random(0.3f, 0.5f), landingColor);
        }
        if (landingShake > 0f) screenShake.shake(0.12f, landingShake);
    }

    /** @return une abscisse au hasard sur la table, à {@code margin} de ses bords. */
    private float tableX(float margin) {
        return MathUtils.random(playArea.getX() + margin, playArea.getX() + playArea.getWidth() - margin);
    }

    /** @return une ordonnée au hasard sur la table, où rebondissent pièces et symboles. */
    private float floorY() {
        return getHeight() * MathUtils.random(COIN_FLOOR_MIN, COIN_FLOOR_MAX);
    }

    /** @return l'image de {@code symbol} détourée de sa case blanche, chargée à sa première utilisation. */
    private TextureRegion iconOf(Symbol symbol) {
        return new TextureRegion(icons.computeIfAbsent(symbol,
            s -> Textures.cutOut(Gdx.files.internal(s.getAssetPath()))));
    }

    /** @return la bannière du Bingo de {@code symbol}, créée à sa première utilisation. */
    private BingoBanner banner(Symbol symbol) {
        return banners.computeIfAbsent(symbol, s -> {
            BingoStyle bannerStyle = BingoStyle.of(s);
            Texture band = bandTexture;
            if (bannerStyle.body() != null) {
                band = Textures.bannerBand(bannerStyle.body(), bannerStyle.trim());
                styleBands.add(band);
            }
            BingoBanner banner = new BingoBanner("BINGO!!!", band, pixel, bannerFont,
                bannerStyle.letterA(), bannerStyle.letterB(), bannerStyle.back());
            banner.setLightningColors(bannerStyle.boltGlow(), bannerStyle.boltCore());
            bannerLayer.addActor(banner);
            return banner;
        });
    }

    @Override
    public void dispose() {
        pixelTexture.dispose();
        coinTexture.dispose();
        bandTexture.dispose();
        for (Texture band : styleBands) band.dispose();
        for (Texture icon : icons.values()) icon.dispose();
        bannerFont.dispose();
        bullseye.dispose();
        churchBell.dispose();
        giantWord.dispose();
    }
}
