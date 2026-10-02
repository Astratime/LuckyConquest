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
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.assets.HudTextures;
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
 * selon le symbole aligné. Le Triple Sept garde la célébration d'origine :
 * flash blanc, bannière « BINGO!!! » qui traverse l'écran, pluie de pièces
 * d'or (dont une partie file vers le compteur des gains) et feux d'artifice.
 * Chaque autre symbole joue une scène avec un décor en pixel art — un objet,
 * un geste, un impact — puis « BINGO! » s'écrit en géant ({@link GiantWord})
 * aux couleurs du symbole (voir {@link BingoStyle}) :
 *
 * <ul>
 *   <li>Sept : un dé roule et se pose sur un « 7 » qui s'embrase ({@link FlamingDieScene}) ;</li>
 *   <li>Double Bar : les mâchoires d'une presse hydraulique se percutent ({@link HydraulicPressScene}) ;</li>
 *   <li>Bar : un marteau de forge frappe trois fois une barre rougeoyante qui éclate ({@link ForgeScene}) ;</li>
 *   <li>Cerise : un cerisier pousse, fleurit, puis lâche ses cerises ({@link CherryTreeScene}) ;</li>
 *   <li>Triple Cerise : une flèche se plante en plein centre d'une cible ({@link BullseyeAnimation}) ;</li>
 *   <li>Raisin : un pressoir écrase une grappe géante ({@link WinePressScene}) ;</li>
 *   <li>Cloche : une mailloche frappe une cloche d'église qui se balance en
 *       projetant des clochettes ({@link ChurchBellAnimation}) ;</li>
 *   <li>Diamant : un burin taille une pierre brute en diamant, sous un faisceau de phare ({@link GemCutScene}) ;</li>
 *   <li>Lingot : un coffre-fort tombe, sa molette tourne, il s'ouvre sur un flot d'or ({@link SafeScene}) ;</li>
 *   <li>Pastèque : un sabre tranche une pastèque en plein vol ({@link KatanaScene}).</li>
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
    private final TextureRegion  pixel;
    private final Texture        coinTexture  = new Texture(Gdx.files.internal("jackpot/coin_spin.png"));
    private final Texture        bandTexture;
    private final List<Texture>  styleBands   = new ArrayList<>();
    /** Image de chaque symbole, détourée de sa case blanche, chargée à son premier Bingo. */
    private final Map<Symbol, Texture> icons  = new EnumMap<>(Symbol.class);
    private static final String  BANNER_TEXT  = "BINGO!!!";

    private final BitmapFont     bannerFont   = Fonts.jersey(170, Color.WHITE, 7f, Palette.TEXT_SHADE, BANNER_TEXT);

    private final Image        flash;
    private final Shockwaves   shockwaves;
    private final Glitter      glitter;
    private final Fireworks    fireworks;
    private final SymbolShower symbols;
    private final CoinShower   coins;
    private final BullseyeAnimation bullseye  = new BullseyeAnimation();
    private final ChurchBellAnimation churchBell = new ChurchBellAnimation();
    private final GiantWord         giantWord = new GiantWord();
    private final FlamingDieScene     flamingDie     = new FlamingDieScene();
    private final HydraulicPressScene hydraulicPress = new HydraulicPressScene();
    private final ForgeScene          forge          = new ForgeScene();
    private final CherryTreeScene     cherryTree     = new CherryTreeScene(iconOf(Symbol.CHERRY));
    private final WinePressScene      winePress      = new WinePressScene(iconOf(Symbol.GRAPE));
    private final GemCutScene         gemCut         = new GemCutScene(iconOf(Symbol.DIAMOND));
    private final SafeScene           safe           = new SafeScene();
    private final KatanaScene         katana         = new KatanaScene();
    private final List<BingoScene>    scenes = List.of(flamingDie, hydraulicPress, forge, cherryTree, winePress,
        gemCut, safe, katana);
    private final Group        bannerLayer = new Group();
    /** Une bannière par symbole, créée à son premier Bingo (ses lettres ont les couleurs du symbole). */
    private final Map<Symbol, BingoBanner> banners = new EnumMap<>(Symbol.class);

    private final List<Emitter> emitters = new ArrayList<>();
    private int[]      emitted = new int[0];
    private BingoStyle style;
    /** Fumée (du dé en feu, de la poussière du coffre qui s'écrase). */
    private static final Color SMOKE = new Color(0.55f, 0.55f, 0.58f, 1f);
    /** false quand la mise en scène remplace la bannière « BINGO!!! » (Triple Cerise). */
    private boolean    showBanner;

    private boolean  running;
    private float    elapsed;
    private Runnable onFinished;

    /**
     * @param hud             pixel blanc et bande des bannières, partagés avec l'écran de jeu
     * @param coinTarget      position (Stage) de la pièce du compteur des gains
     * @param onCoinCollected appelé à l'arrivée de chaque pièce sur le compteur
     * @param settings        effets réduits : pas de flash (la secousse est coupée par {@code screenShake})
     */
    public JackpotCelebration(PlayArea playArea, ScreenShake screenShake, VisualSettings settings, HudTextures hud,
                              Supplier<Vector2> coinTarget, Runnable onCoinCollected) {
        this.playArea      = playArea;
        this.screenShake   = screenShake;
        this.settings      = settings;
        this.pixel         = new TextureRegion(hud.pixel);
        this.bandTexture   = hud.bannerBand;
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
        churchBell.setSwingListener(this::onBellSwing);

        addActor(shockwaves);
        addActor(glitter);
        addActor(fireworks);
        addActor(symbols);
        addActor(coins);
        addActor(bullseye);
        addActor(churchBell);
        for (BingoScene scene : scenes) addActor(scene);
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
        for (BingoScene scene : scenes) scene.hide();
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
            case SEVEN         -> scheduleFlamingDie(icon);
            case DOUBLE_BAR    -> scheduleHydraulicPress(icon);
            case BAR           -> scheduleForge(icon);
            case CHERRY        -> scheduleCherryTree(icon);
            case TRIPLE_CHERRY -> scheduleBullseye(icon);
            case GRAPE         -> scheduleWinePress(icon);
            case BELL          -> scheduleChime(icon);
            case DIAMOND       -> scheduleGemCut(icon);
            case GOLD_BAR      -> scheduleSafe(icon);
            case WATERMELON    -> scheduleKatana(icon);
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

    // --- Scènes à décor : un objet en pixel art, un geste, un impact, puis « BINGO! » en géant ---

    /** Sept : un dé roule, se pose sur un « 7 » qui s'embrase ; des 7 en feu jaillissent, le mot fume en partant. */
    private void scheduleFlamingDie(TextureRegion icon) {
        float centerX = playArea.getCenterX();
        playScene(flamingDie, centerX, getHeight() * 0.36f, FlamingDieScene.LAND_TIME + 0.12f, 0.8f);
        float land = FlamingDieScene.LAND_TIME;
        Color[] fire = style.fireworks();
        float half = flamingDie.halfSize();
        at(land, () -> {
            screenShake.shake(0.4f, 14f);
            impactFlash(1f, 0.55f, 0.2f, 0.4f);
            shockwaves.ring(centerX, getHeight() * 0.36f - half, 260f, 0.6f, 0.3f, fire[1]);
        });
        // Le « 7 » s'embrase d'un coup, puis les flammes lèchent le dessus et les flancs du dé.
        emit(land, land + 0.1f, 60, i -> glitter.ember(centerX + MathUtils.random(-half, half),
            flamingDie.topY() - MathUtils.random(0f, half * 1.5f), MathUtils.random(-80f, 80f),
            MathUtils.random(350f, 700f), 0f, MathUtils.random(10f, 18f), MathUtils.random(0.5f, 0.9f), fire[i % 3]));
        emit(land, 2.6f, 420, i -> glitter.ember(centerX + MathUtils.random(-half * 1.1f, half * 1.1f),
            flamingDie.topY() - MathUtils.random(0f, half * 1.6f), MathUtils.random(-30f, 30f), MathUtils.random(250f, 520f),
            0f, MathUtils.random(8f, 16f), MathUtils.random(0.5f, 1.1f), fire[i % 3]));
        emit(land, land, 10, i -> sevenJet(icon, centerX, flamingDie.topY()));
        emit(land + 0.1f, land + 1.3f, 14, i -> sevenJet(icon, centerX, flamingDie.topY()));
        // Le mot fume en s'effaçant.
        emit(2.05f, 2.6f, 60, i -> glitter.ember(centerX + MathUtils.random(-330f, 330f),
            getHeight() * 0.8f + MathUtils.random(-70f, 50f), MathUtils.random(-15f, 15f), MathUtils.random(60f, 130f),
            0f, MathUtils.random(10f, 18f), MathUtils.random(0.7f, 1.1f), SMOKE));
        for (float time : new float[] {1.6f, 2.0f, 2.3f}) at(time, () -> rocket(false));
        coinRain(land, land + 1f, 18, 1);
    }

    private void sevenJet(TextureRegion icon, float x, float y) {
        symbols.jet(icon, x + MathUtils.random(-40f, 40f), y, MathUtils.random(60f, 120f),
            MathUtils.random(900f, 1300f), MathUtils.random(60f, 85f), 1500f);
    }

    /** Double Bar : la presse hydraulique ; au choc, étincelles et Double Bar jaillissent sur les côtés. */
    private void scheduleHydraulicPress(TextureRegion icon) {
        float centerX = playArea.getCenterX(), centerY = getHeight() * 0.5f;
        float slam = HydraulicPressScene.SLAM_TIME;
        playScene(hydraulicPress, centerX, centerY, slam + 0.3f, 0.5f);
        Color[] sparks = {Palette.GOLD, Palette.ORANGE, Color.WHITE};
        at(slam, () -> {
            screenShake.shake(0.5f, 18f);
            impactFlash(1f, 1f, 1f, 0.5f);
            shockwaves.ring(centerX, centerY, 520f, 0.6f, 0.2f, Palette.STEEL_LIGHT);
        });
        float half = 37f * BingoScene.SCALE;
        emit(slam, slam, 70, i -> {
            float side = i % 2 == 0 ? -1f : 1f;
            glitter.ember(centerX + side * MathUtils.random(half * 0.6f, half), centerY,
                side * MathUtils.random(300f, 900f), MathUtils.random(-120f, 320f), 1000f,
                MathUtils.random(4f, 8f), MathUtils.random(0.5f, 0.9f), sparks[i % sparks.length]);
        });
        emit(slam, slam + 0.25f, 26, i -> symbols.jet(icon, centerX + MathUtils.random(-half, half), centerY,
            i % 2 == 0 ? MathUtils.random(-15f, 35f) : MathUtils.random(145f, 195f),
            MathUtils.random(700f, 1200f), MathUtils.random(60f, 90f), 1100f));
        emit(slam + 0.3f, 2.6f, 40, i -> glitter.twinkle(tableX(0f), getHeight() * MathUtils.random(0.1f, 0.9f),
            MathUtils.random(14f, 26f), 0.5f, Color.valueOf("8fd3ffff")));
        coinRain(slam, slam + 1f, 18, 1);
    }

    /** Bar : la forge ; trois coups de marteau en gerbes d'étincelles, la barre éclate au troisième. */
    private void scheduleForge(TextureRegion icon) {
        float centerX = playArea.getCenterX();
        float[] hits = ForgeScene.HITS;
        float last = hits[hits.length - 1];
        playScene(forge, centerX, getHeight() * 0.16f, last + 0.12f, 0.78f);
        Color[] sparks = {Palette.GOLD, Palette.ORANGE, Palette.GOLD_PALE};
        for (int k = 0; k < hits.length; k++) {
            int strength = k;
            at(hits[k], () -> {
                Vector2 hit = forge.strikePoint();
                screenShake.shake(0.2f, 8f + strength * 5f);
                shockwaves.ring(hit.x, hit.y, 180f + strength * 60f, 0.45f, 0.3f, sparks[1]);
                for (int e = 0; e < 25 + strength * 15; e++) {
                    float angle = MathUtils.random(15f, 165f);
                    float speed = MathUtils.random(300f, 850f);
                    glitter.ember(hit.x, hit.y, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed, 1500f,
                        MathUtils.random(4f, 8f), MathUtils.random(0.4f, 0.8f), sparks[e % sparks.length]);
                }
            });
        }
        at(last, () -> impactFlash(1f, 0.9f, 0.7f, 0.5f));
        emit(last, last, 30, i -> {
            Vector2 hit = forge.strikePoint();
            symbols.jet(icon, hit.x, hit.y, MathUtils.random(20f, 160f), MathUtils.random(700f, 1300f),
                MathUtils.random(50f, 80f), 1400f);
        });
        coinRain(last, last + 0.8f, 18, 1);
    }

    /** Cerise : le cerisier pousse, fleurit, se couvre de cerises ; secoué, il les laisse tomber en pluie. */
    private void scheduleCherryTree(TextureRegion icon) {
        float centerX = playArea.getCenterX();
        playScene(cherryTree, centerX, getHeight() * 0.1f, 1.2f, 0.82f);
        at(CherryTreeScene.SHAKE_TIME, () -> screenShake.shake(0.3f, 6f));
        at(CherryTreeScene.DROP_TIME, () -> {
            for (Vector2 fruit : cherryTree.fruitPositions()) {
                symbols.rain(icon, fruit.x, fruit.y, getHeight() * MathUtils.random(0.05f, 0.14f),
                    MathUtils.random(55f, 70f), MathUtils.random(0f, 120f), 1400f, false);
            }
        });
        emit(CherryTreeScene.DROP_TIME, CherryTreeScene.DROP_TIME + 1f, 30, i -> symbols.rain(icon, tableX(40f),
            getHeight() + 60f, floorY(), MathUtils.random(45f, 70f), MathUtils.random(150f, 400f), 1000f, false));
        // Pétales emportés par le vent.
        Color[] petals = {Color.valueOf("ff6fa0ff"), Color.valueOf("ffb3cfff"), Color.valueOf("ff4a8aff")};
        emit(0.8f, 2.6f, 110, i -> glitter.ember(tableX(0f), getHeight() * MathUtils.random(0.35f, 1f),
            MathUtils.random(20f, 70f), MathUtils.random(-90f, -40f), 0f, MathUtils.random(9f, 13f),
            MathUtils.random(1.2f, 2f), petals[i % petals.length]));
        coinRain(CherryTreeScene.DROP_TIME, CherryTreeScene.DROP_TIME + 0.8f, 18, 1);
    }

    /** Raisin : le pressoir écrase une grappe géante ; le jus gicle, des bulles montent, des grains roulent. */
    private void scheduleWinePress(TextureRegion icon) {
        float centerX = playArea.getCenterX();
        float crush = WinePressScene.CRUSH_TIME;
        playScene(winePress, centerX, getHeight() * 0.06f, crush + 0.12f, 0.8f);
        Color[] juice = style.fireworks();
        float half = 23f * BingoScene.SCALE;
        at(crush, () -> {
            screenShake.shake(0.45f, 14f);
            impactFlash(0.75f, 0.5f, 1f, 0.35f);
            shockwaves.ring(centerX, winePress.vatTop(), 320f, 0.6f, 0.3f, juice[1]);
        });
        emit(crush, crush, 90, i -> {
            float angle = MathUtils.random(40f, 140f);
            float speed = MathUtils.random(400f, 950f);
            glitter.ember(centerX + MathUtils.random(-half, half), winePress.vatTop(), MathUtils.cosDeg(angle) * speed,
                MathUtils.sinDeg(angle) * speed, 1300f, MathUtils.random(6f, 12f), MathUtils.random(0.8f, 1.3f),
                juice[i % juice.length]);
        });
        emit(crush, 2.6f, 120, i -> glitter.ember(centerX + MathUtils.random(-half, half), winePress.vatTop(),
            MathUtils.random(-15f, 15f), MathUtils.random(80f, 200f), 0f, MathUtils.random(8f, 16f),
            MathUtils.random(1.5f, 2.2f), juice[i % juice.length]));
        emit(crush, crush + 0.3f, 24, i -> {
            boolean left = i % 2 == 0;
            symbols.jet(icon, centerX + (left ? -half : half), winePress.vatTop(),
                left ? MathUtils.random(140f, 170f) : MathUtils.random(10f, 40f),
                MathUtils.random(500f, 900f), MathUtils.random(45f, 65f), 1400f);
        });
        coinRain(crush, crush + 1f, 18, 1);
    }

    /** Diamant : la taille ; au coup de burin, éclat, faisceau de phare et mini-diamants. */
    private void scheduleGemCut(TextureRegion icon) {
        float centerX = playArea.getCenterX(), centerY = getHeight() * 0.42f;
        float cut = GemCutScene.CUT_TIME;
        playScene(gemCut, centerX, centerY, cut + 0.12f, 0.8f);
        Color[] ice = style.fireworks();
        at(cut, () -> {
            screenShake.shake(0.25f, 6f);
            impactFlash(0.85f, 1f, 1f, 0.6f);
            shockwaves.ring(centerX, centerY, 420f, 0.7f, 1f, ice[0]);
        });
        emit(cut, cut, 26, i -> symbols.jet(icon, centerX, centerY, i * 360f / 26 + MathUtils.random(-6f, 6f),
            MathUtils.random(500f, 1000f), MathUtils.random(45f, 70f), 900f));
        emit(cut, cut, 30, i -> glitter.twinkle(centerX + MathUtils.random(-200f, 200f),
            centerY + MathUtils.random(-160f, 160f), MathUtils.random(18f, 34f), MathUtils.random(0.4f, 0.8f),
            ice[i % ice.length]));
        emit(cut, 2.6f, 90, i -> glitter.twinkle(tableX(0f), getHeight() * MathUtils.random(0.05f, 0.98f),
            MathUtils.random(14f, 30f), MathUtils.random(0.4f, 0.8f), ice[i % ice.length]));
        coinRain(cut, cut + 1f, 18, 1);
    }

    /** Lingot : le coffre-fort tombe, sa molette tourne, sa porte s'ouvre sur un flot de lingots et de pièces. */
    private void scheduleSafe(TextureRegion icon) {
        float centerX = playArea.getCenterX(), baseY = getHeight() * 0.12f;
        float open = SafeScene.OPEN_TIME;
        playScene(safe, centerX, baseY, open + 0.12f, 0.8f);
        at(SafeScene.LAND_TIME, () -> {
            screenShake.shake(0.45f, 16f);
            shockwaves.ring(centerX, baseY, 360f, 0.55f, 0.25f, Palette.STEEL_LIGHT);
            for (int e = 0; e < 24; e++) {
                float side = e % 2 == 0 ? -1f : 1f;
                glitter.ember(centerX + side * MathUtils.random(60f, 140f), baseY, side * MathUtils.random(80f, 260f),
                    MathUtils.random(40f, 160f), 400f, MathUtils.random(8f, 14f), MathUtils.random(0.4f, 0.7f), SMOKE);
            }
        });
        at(open, () -> {
            screenShake.shake(0.3f, 8f);
            impactFlash(1f, 0.9f, 0.5f, 0.5f);
        });
        emit(open, open + 0.9f, 40, i -> {
            Vector2 mouth = safe.opening();
            symbols.jet(icon, mouth.x + MathUtils.random(-60f, 60f), mouth.y, MathUtils.random(55f, 125f),
                MathUtils.random(800f, 1300f), MathUtils.random(60f, 90f), 1300f);
        });
        emit(open, open + 1.3f, 60, i -> {
            Vector2 mouth = safe.opening();
            glitter.twinkle(mouth.x + MathUtils.random(-120f, 120f), mouth.y + MathUtils.random(-100f, 260f),
                MathUtils.random(14f, 26f), 0.5f, Color.valueOf("fffbe0ff"));
        });
        coinRain(open, open + 1.2f, 120, 2);
    }

    /** Pastèque : le sabre tranche une pastèque en l'air ; jus, pépins et tranches jaillissent. */
    private void scheduleKatana(TextureRegion icon) {
        float centerX = playArea.getCenterX(), centerY = getHeight() * 0.48f;
        float slice = KatanaScene.SLICE_TIME;
        playScene(katana, centerX, centerY, slice + 0.25f, 0.8f);
        Color juice = Color.valueOf("ff5a78ff");
        Color seed  = Color.valueOf("1a1a1aff");
        at(slice, () -> {
            screenShake.shake(0.3f, 10f);
            impactFlash(1f, 1f, 1f, 0.4f);
        });
        emit(slice, slice + 0.2f, 80, i -> {
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(250f, 800f);
            boolean isSeed = i % 3 == 0;
            glitter.ember(centerX, centerY, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed, 1300f,
                isSeed ? 6f : MathUtils.random(7f, 12f), MathUtils.random(0.9f, 1.5f), isSeed ? seed : juice);
        });
        emit(slice, slice + 0.2f, 22, i -> symbols.jet(icon, centerX, centerY, i * 360f / 22 + MathUtils.random(-8f, 8f),
            MathUtils.random(500f, 1000f), MathUtils.random(55f, 80f), 1300f));
        for (float time : new float[] {1.6f, 2.0f, 2.3f}) at(time, () -> rocket(false));
        coinRain(slice, slice + 1f, 18, 1);
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

    /**
     * Lance le décor {@code scene} en {@code (x, y)} et écrit « BINGO! » en géant
     * à {@code wordAt}, centré sur la table à la hauteur {@code wordY} (fraction de l'écran).
     */
    private void playScene(BingoScene scene, float x, float y, float wordAt, float wordY) {
        useGiantWord();
        scene.play(x, y);
        giantWord.play(playArea.getCenterX(), getHeight() * wordY, wordAt, WORD_FADE_AT,
            style.letterA(), style.letterB());
    }

    /** Flash de l'écran à l'impact d'une scène (sauf effets réduits). */
    private void impactFlash(float r, float g, float b, float alpha) {
        if (settings.isReducedEffects()) return;
        flash.clearActions();
        flash.setColor(r, g, b, alpha);
        flash.addAction(Actions.fadeOut(0.25f));
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
            BingoBanner banner = new BingoBanner(BANNER_TEXT, band, pixel, bannerFont,
                bannerStyle.letterA(), bannerStyle.letterB(), bannerStyle.back());
            banner.setLightningColors(bannerStyle.boltGlow(), bannerStyle.boltCore());
            bannerLayer.addActor(banner);
            return banner;
        });
    }

    @Override
    public void dispose() {
        coinTexture.dispose();
        for (Texture band : styleBands) band.dispose();
        for (Texture icon : icons.values()) icon.dispose();
        Fonts.release(bannerFont);
        bullseye.dispose();
        churchBell.dispose();
        for (BingoScene scene : scenes) scene.dispose();
        giantWord.dispose();
    }
}
