package fr.astratime.lucky.animations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Textures;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.settings.VisualSettings;
import fr.astratime.lucky.views.PlayArea;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * Célébration du jackpot (Bingo), par-dessus tout l'écran de jeu, sous des
 * projecteurs disco multicolores ({@link DiscoSpotlights}). Elle change selon
 * le symbole aligné. Le Triple Sept garde la célébration d'origine :
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
 *   <li>Pastèque : un sabre tranche une pastèque en plein vol ({@link KatanaScene}) ;</li>
 *   <li>Fer à cheval : un fer géant lancé s'accroche à un piquet, puis il pleut des trèfles ({@link HorseshoeScene}) ;</li>
 *   <li>Écu : trois écus forment un mur où ricochent des étincelles, puis un blason doré s'allume ({@link ShieldWallScene}) ;</li>
 *   <li>Épée : trois épées se plantent devant l'ennemi, puis tranchent l'écran d'un X lumineux ({@link SwordsScene}) ;</li>
 *   <li>Cœur : un cœur géant bat trois fois, puis éclate en petits cœurs ({@link HeartbeatScene}) ;</li>
 *   <li>Dé : trois dés roulent et s'arrêtent sur 6-6-6, puis une gerbe de flammes ({@link TripleDiceScene}) ;</li>
 *   <li>Étoile : une étoile filante explose en feu d'artifice rouge, bleu et or ({@link ShootingStarScene}) ;</li>
 *   <li>Bombe : compte à rebours 3, 2, 1, puis une explosion qui envahit l'écran ({@link BombScene}) ;</li>
 *   <li>Couronne : tapis rouge, fanfare, la couronne descend sur son coussin ({@link CoronationScene}) ;</li>
 *   <li>Pépite : une pioche fend un rocher d'où jaillit un geyser de pépites ({@link MineScene}).</li>
 * </ul>
 *
 * Pendant {@link #DURATION}, le calque intercepte les clics : le joueur ne peut
 * pas continuer avant la fin. Les dernières particules finissent ensuite de
 * tomber sans bloquer le jeu. Une célébration déjà vue se passe d'un clic
 * ({@link #skip()}) : la mise en scène s'efface et le combat reprend.
 */
public class JackpotCelebration extends Group implements Disposable {

    /** Durée pendant laquelle le jeu attend la fin de la célébration. */
    public static final float DURATION = 3f;

    private static final float FLASH_ALPHA = 0.7f;
    /** Fondu de la mise en scène quand le joueur passe la célébration. */
    private static final float SKIP_FADE   = 0.25f;
    /** L'indication « Clic : passer » apparaît en fondu, dans le bas du panneau de gauche (libre pendant le combat). */
    private static final float HINT_DELAY  = 0.3f;
    private static final float HINT_FADE   = 0.3f;
    private static final float HINT_Y      = 200f;
    /** Hauteur (fraction de l'écran) où les projecteurs dessinent leurs flaques de lumière sur la table. */
    private static final float SPOTLIGHT_FLOOR = 0.14f;
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
    private final BitmapFont     hintFont     = Fonts.jersey(30, Color.WHITE, 3f, Palette.TEXT_SHADE);

    private final Image        flash;
    /** Projecteurs disco multicolores qui balaient la table pendant toute la fête. */
    private final DiscoSpotlights spotlights = new DiscoSpotlights();
    private final Shockwaves   shockwaves;
    private final Glitter      glitter;
    /** Lumières devant les décors : les étincelles qui ricochent sur le mur d'écus. */
    private final Glitter      frontGlitter;
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
    private final HorseshoeScene      horseshoe      = new HorseshoeScene();
    private final ShieldWallScene     shieldWall     = new ShieldWallScene();
    private final SwordsScene         swords         = new SwordsScene();
    private final HeartbeatScene      heartbeat      = new HeartbeatScene();
    private final TripleDiceScene     tripleDice     = new TripleDiceScene();
    private final ShootingStarScene   shootingStar   = new ShootingStarScene();
    private final BombScene           bomb           = new BombScene();
    private final CoronationScene     coronation     = new CoronationScene();
    private final MineScene           mine           = new MineScene();
    private final List<BingoScene>    scenes = List.of(flamingDie, hydraulicPress, forge, cherryTree, winePress,
        gemCut, safe, katana, horseshoe, shieldWall, swords, heartbeat, tripleDice, shootingStar, bomb, coronation,
        mine);
    private final Group        bannerLayer = new Group();
    /** La mise en scène au premier plan (objets, bannière, mot géant, flash) : elle s'efface quand on passe. */
    private final Group        foreground  = new Group();
    private final Label        skipHint;
    /** Une bannière par symbole, créée à son premier Bingo (ses lettres ont les couleurs du symbole). */
    private final Map<Symbol, BingoBanner> banners = new EnumMap<>(Symbol.class);

    private final List<Emitter> emitters = new ArrayList<>();
    private int[]      emitted = new int[0];
    private BingoStyle style;
    /** Fumée (du dé en feu, de la poussière du coffre qui s'écrase). */
    private static final Color SMOKE = new Color(0.55f, 0.55f, 0.58f, 1f);
    /** false quand la mise en scène remplace la bannière « BINGO!!! » (Triple Cerise). */
    private boolean    showBanner;
    /** Instant où « BINGO! » apparaît : la bannière dès le début, ou le mot géant d'une scène. */
    private float      wordAt;

    private boolean  running;
    private boolean  skippable;
    private boolean  skipped;
    private float    elapsed;
    private Runnable onFinished;

    /**
     * @param hud             pixel blanc et bande des bannières, partagés avec l'écran de jeu
     * @param coinTarget      position (Stage) de la pièce du compteur des gains
     * @param onCoinCollected appelé à l'arrivée de chaque pièce sur le compteur
     * @param settings        effets réduits : pas de flash (la secousse est coupée par {@code screenShake})
     * @param fireworkSounds  bruitages des fusées
     */
    public JackpotCelebration(PlayArea playArea, ScreenShake screenShake, VisualSettings settings, HudTextures hud,
                              Supplier<Vector2> coinTarget, Runnable onCoinCollected, Fireworks.Sounds fireworkSounds) {
        this.playArea      = playArea;
        this.screenShake   = screenShake;
        this.settings      = settings;
        this.pixel         = new TextureRegion(hud.pixel);
        this.bandTexture   = hud.bannerBand;
        setTouchable(Touchable.disabled);

        shockwaves = new Shockwaves(pixel);
        glitter    = new Glitter(pixel);
        frontGlitter = new Glitter(pixel);
        fireworks  = new Fireworks(pixel, fireworkSounds);
        symbols    = new SymbolShower();
        coins      = new CoinShower(new TextureRegion(coinTexture), coinTarget, onCoinCollected);
        flash      = new Image(new TextureRegionDrawable(pixel));
        flash.setTouchable(Touchable.disabled);
        flash.getColor().a = 0f;
        bannerLayer.setTouchable(Touchable.disabled);
        churchBell.setSwingListener(this::onBellSwing);

        addActor(spotlights); // derrière tout le reste : la lumière éclaire la table, pas les objets
        addActor(shockwaves);
        addActor(glitter);
        addActor(fireworks);
        addActor(symbols);
        addActor(coins);
        foreground.setTransform(false);
        foreground.setTouchable(Touchable.disabled);
        foreground.addActor(bullseye);
        foreground.addActor(churchBell);
        for (BingoScene scene : scenes) foreground.addActor(scene);
        foreground.addActor(frontGlitter);
        foreground.addActor(bannerLayer);
        foreground.addActor(giantWord);
        foreground.addActor(flash);
        addActor(foreground);

        skipHint = new Label(Lang.t("Clic : passer"), new Label.LabelStyle(hintFont, Color.WHITE));
        skipHint.setTouchable(Touchable.disabled);
        skipHint.setVisible(false);
        addActor(skipHint);

        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                skip();
                return true;
            }
        });
    }

    /**
     * Lance la célébration du Bingo de {@code symbol} ; {@code onWord} est appelé
     * quand « BINGO! » apparaît (bannière ou mot géant de la scène),
     * {@code onFinished} au bout de {@link #DURATION} (ou dès que le joueur la passe).
     *
     * @param skippable {@code true} si la célébration a déjà été vue : un clic la passe
     */
    public void play(Symbol symbol, boolean skippable, Runnable onWord, Runnable onFinished) {
        cancel();
        this.onFinished = onFinished;
        this.skippable  = skippable;
        skipped         = false;
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

        spotlights.play(playArea.getX(), playArea.getX() + playArea.getWidth(), height, height * SPOTLIGHT_FLOOR,
            DURATION, settings.isReducedEffects());

        emitters.clear();
        showBanner   = true;
        wordAt       = 0f;
        schedule(symbol, iconOf(symbol));
        at(wordAt, onWord);
        emitted = new int[emitters.size()];

        if (showBanner) banner(symbol).play(playArea.getCenterX(), height * BANNER_Y, width);

        if (skippable) {
            skipHint.setText(Lang.t("Clic : passer"));
            skipHint.pack();
            skipHint.setPosition(playArea.getX() / 2f - skipHint.getWidth() / 2f, HINT_Y);
            skipHint.getColor().a = 0f;
            skipHint.setVisible(true);
            skipHint.addAction(Actions.sequence(Actions.delay(HINT_DELAY), Actions.fadeIn(HINT_FADE)));
        }
    }

    /** @return {@code true} si la dernière célébration a été passée par le joueur. */
    public boolean wasSkipped() { return skipped; }

    /** @return {@code true} pendant la célébration, tant qu'elle bloque le jeu. */
    public boolean isRunning() { return running; }

    /**
     * Passe la célébration, si elle a déjà été vue : la mise en scène s'efface,
     * le son de la scène s'arrête (voir {@code onFinished}) et le combat reprend tout de suite.
     * Les pièces et les étincelles déjà lancées finissent de tomber.
     *
     * @return {@code true} si la célébration a été passée
     */
    public boolean skip() {
        if (!running || !skippable) return false;
        running = false;
        skipped = true;
        setTouchable(Touchable.disabled);
        emitters.clear();
        spotlights.stop();
        screenShake.stop();
        hideHint();
        foreground.clearActions();
        foreground.addAction(Actions.sequence(Actions.fadeOut(SKIP_FADE), Actions.run(this::hideForeground),
            Actions.alpha(1f)));
        onFinished.run();
        return true;
    }

    /** Cache tout de suite les objets de la mise en scène (pas les particules qui tombent). */
    private void hideForeground() {
        for (BingoBanner banner : banners.values()) banner.hide();
        bullseye.hide();
        churchBell.hide();
        for (BingoScene scene : scenes) scene.hide();
        giantWord.hide();
        flash.clearActions();
        flash.getColor().a = 0f;
    }

    private void hideHint() {
        skipHint.clearActions();
        skipHint.addAction(Actions.sequence(Actions.fadeOut(HINT_FADE / 2f), Actions.visible(false)));
    }

    /** Arrête tout immédiatement (nouvelle partie), sans appeler la fin de la célébration. */
    public void cancel() {
        running = false;
        setTouchable(Touchable.disabled);
        emitters.clear();
        coins.removeAll();
        spotlights.stop();
        fireworks.removeAll();
        symbols.removeAll();
        glitter.removeAll();
        frontGlitter.removeAll();
        shockwaves.removeAll();
        foreground.clearActions();
        foreground.getColor().a = 1f;
        hideForeground();
        skipHint.clearActions();
        skipHint.setVisible(false);
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
            if (skippable) hideHint();
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
            case HORSESHOE     -> scheduleHorseshoe(icon);
            case ECU           -> scheduleShieldWall(icon);
            case SWORD         -> scheduleSwords(icon);
            case HEART         -> scheduleHeartbeat(icon);
            case DIE           -> scheduleTripleDice(icon);
            case STAR          -> scheduleShootingStar(icon);
            case BOMB          -> scheduleBomb(icon);
            case CROWN         -> scheduleCoronation(icon);
            case NUGGET        -> scheduleMine(icon);
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

    /** Fer à cheval : le fer s'accroche au piquet dans un « ding ! » doré ; trèfles et pièces pleuvent. */
    private void scheduleHorseshoe(TextureRegion icon) {
        float centerX = playArea.getCenterX();
        float hook = HorseshoeScene.HOOK_TIME;
        playScene(horseshoe, centerX, getHeight() * 0.1f, hook + 0.12f, 0.8f);
        Color gold = Color.valueOf("ffe680ff");
        at(hook, () -> {
            Vector2 peg = horseshoe.peg();
            screenShake.shake(0.25f, 8f);
            impactFlash(1f, 0.95f, 0.7f, 0.4f);
            for (int e = 0; e < 18; e++) {
                float angle = MathUtils.random(360f);
                float speed = MathUtils.random(250f, 600f);
                glitter.ember(peg.x, peg.y, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed, 900f,
                    MathUtils.random(5f, 9f), MathUtils.random(0.4f, 0.7f), Color.WHITE);
            }
        });
        // Le « ding » : trois ondes dorées partent du piquet.
        for (int k = 0; k < 3; k++) {
            at(hook + k * 0.12f, () -> shockwaves.ring(horseshoe.peg().x, horseshoe.peg().y, 260f, 0.8f, 1f, gold));
        }
        TextureRegion clover = horseshoe.clover();
        emit(hook, hook, 18, i -> symbols.jet(clover, horseshoe.peg().x, horseshoe.peg().y,
            i * 360f / 18 + MathUtils.random(-8f, 8f), MathUtils.random(450f, 850f), MathUtils.random(40f, 56f), 1100f));
        emit(hook + 0.15f, hook + 1.4f, 48, i -> symbols.rain(i % 4 == 0 ? icon : clover, tableX(40f),
            getHeight() + 60f, floorY(), i % 4 == 0 ? MathUtils.random(55f, 75f) : MathUtils.random(40f, 60f),
            MathUtils.random(150f, 400f), 1000f, false));
        emit(hook, 2.6f, 50, i -> glitter.twinkle(tableX(0f), getHeight() * MathUtils.random(0.05f, 0.95f),
            MathUtils.random(12f, 24f), 0.6f, i % 3 == 0 ? Palette.MINT : gold));
        coinRain(hook, hook + 1.2f, 40, 2);
    }

    /** Écu : les trois écus s'abattent en mur ; des étincelles y ricochent, puis le blason doré s'allume. */
    private void scheduleShieldWall(TextureRegion icon) {
        float centerX = playArea.getCenterX(), baseY = getHeight() * 0.1f;
        float blazonAt = ShieldWallScene.BLAZON_TIME;
        playScene(shieldWall, centerX, baseY, blazonAt + 0.12f, 0.84f);
        for (int k = 0; k < ShieldWallScene.LANDINGS.length; k++) {
            int index = ShieldWallScene.ORDER[k], strength = k;
            at(ShieldWallScene.LANDINGS[k], () -> {
                float x = shieldWall.shieldX(index);
                screenShake.shake(0.2f, 7f + strength * 3f);
                shockwaves.ring(x, baseY, 200f, 0.45f, 0.25f, Palette.STEEL_LIGHT);
                for (int e = 0; e < 12; e++) {
                    float side = e % 2 == 0 ? -1f : 1f;
                    glitter.ember(x + side * MathUtils.random(30f, 80f), baseY, side * MathUtils.random(60f, 200f),
                        MathUtils.random(30f, 120f), 300f, MathUtils.random(8f, 13f), MathUtils.random(0.4f, 0.7f), SMOKE);
                }
            });
        }
        // Des étincelles frappent le mur et ricochent vers le haut.
        Color[] sparks = {Palette.GOLD, Palette.ORANGE, Color.WHITE};
        emit(ShieldWallScene.HITS_FROM, ShieldWallScene.HITS_UNTIL, 18, i -> {
            float half = shieldWall.wallWidth() / 2f - 40f;
            float x = centerX + MathUtils.random(-half, half);
            float y = MathUtils.random(shieldWall.wallBottom() + 80f, shieldWall.wallTop() - 20f);
            screenShake.shake(0.08f, 3f);
            frontGlitter.twinkle(x, y, MathUtils.random(44f, 64f), 0.3f, Color.WHITE);
            for (int e = 0; e < 14; e++) {
                float angle = MathUtils.random(15f, 165f);
                float speed = MathUtils.random(350f, 850f);
                frontGlitter.ember(x, y, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed, 1600f,
                    MathUtils.random(6f, 11f), MathUtils.random(0.4f, 0.8f), sparks[e % sparks.length]);
            }
        });
        Color gold = Color.valueOf("ffe680ff");
        at(blazonAt, () -> {
            screenShake.shake(0.25f, 6f);
            impactFlash(1f, 0.9f, 0.5f, 0.45f);
        });
        for (int k = 0; k < 3; k++) {
            at(blazonAt + k * 0.12f, () -> shockwaves.ring(shieldWall.blazonCenter().x, shieldWall.blazonCenter().y,
                340f, 0.8f, 1f, gold));
        }
        emit(blazonAt, blazonAt, 18, i -> symbols.jet(icon, shieldWall.blazonCenter().x, shieldWall.blazonCenter().y,
            i * 360f / 18 + MathUtils.random(-6f, 6f), MathUtils.random(500f, 950f), MathUtils.random(50f, 75f), 1100f));
        emit(blazonAt, 2.6f, 80, i -> glitter.twinkle(shieldWall.blazonCenter().x + MathUtils.random(-160f, 160f),
            shieldWall.blazonCenter().y + MathUtils.random(-140f, 140f), MathUtils.random(14f, 28f), 0.5f, gold));
        coinRain(blazonAt, blazonAt + 1f, 24, 1);
    }

    /** Épée : les épées se plantent devant l'ennemi, puis le grand X lumineux tranche l'écran en gerbes d'étincelles. */
    private void scheduleSwords(TextureRegion icon) {
        float centerX = playArea.getCenterX(), groundY = getHeight() * 0.52f;
        float slash = SwordsScene.SLASH_TIME, second = slash + SwordsScene.SECOND_SLASH;
        playScene(swords, centerX, groundY, second + 0.25f, 0.5f);
        Color[] fire = style.fireworks();
        for (int k = 0; k < SwordsScene.LANDINGS.length; k++) {
            int index = SwordsScene.ORDER[k], strength = k;
            at(SwordsScene.LANDINGS[k], () -> {
                Vector2 ground = swords.groundPoint(index);
                screenShake.shake(0.2f, 6f + strength * 3f);
                shockwaves.ring(ground.x, ground.y, 150f, 0.4f, 0.25f, Palette.STEEL_LIGHT);
                for (int e = 0; e < 16; e++) {
                    float angle = MathUtils.random(20f, 160f);
                    float speed = MathUtils.random(200f, 550f);
                    glitter.ember(ground.x, ground.y, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed,
                        1400f, MathUtils.random(4f, 8f), MathUtils.random(0.3f, 0.6f), e % 3 == 0 ? Color.WHITE : SMOKE);
                }
            });
        }
        // Deux coups de lame : « \ » puis « / », chacun en gerbe d'étincelles le long du trait.
        for (int k = 0; k < 2; k++) {
            float time = k == 0 ? slash : second;
            float angle = k == 0 ? -SwordsScene.SLASH_ANGLE : SwordsScene.SLASH_ANGLE;
            at(time, () -> {
                screenShake.shake(0.3f, 12f);
                impactFlash(1f, 0.85f, 0.6f, 0.4f);
            });
            emit(time, time + 0.08f, 60, i -> {
                float along = MathUtils.random(-700f, 700f);
                float x = centerX + MathUtils.cosDeg(angle) * along, y = groundY + MathUtils.sinDeg(angle) * along;
                float out = angle + (i % 2 == 0 ? 90f : -90f) + MathUtils.random(-30f, 30f);
                float speed = MathUtils.random(150f, 500f);
                glitter.ember(x, y, MathUtils.cosDeg(out) * speed, MathUtils.sinDeg(out) * speed, 900f,
                    MathUtils.random(5f, 10f), MathUtils.random(0.4f, 0.8f), fire[i % fire.length]);
            });
        }
        emit(second, second + 0.2f, 22, i -> symbols.jet(icon, centerX, groundY, i * 360f / 22 + MathUtils.random(-8f, 8f),
            MathUtils.random(500f, 1000f), MathUtils.random(55f, 80f), 1300f));
        for (float time : new float[] {1.8f, 2.1f, 2.4f}) at(time, () -> rocket(false));
        coinRain(second, second + 1f, 18, 1);
    }

    /** Cœur : à chaque battement, une onde rose ; le cœur éclate en petits cœurs qui retombent sur la table. */
    private void scheduleHeartbeat(TextureRegion icon) {
        float centerX = playArea.getCenterX(), centerY = getHeight() * 0.45f;
        float burst = HeartbeatScene.BURST_TIME;
        playScene(heartbeat, centerX, centerY, burst + 0.12f, 0.8f);
        Color pink = Color.valueOf("ff6fa0ff"), pale = Color.valueOf("ffc1d6ff");
        for (int k = 0; k < HeartbeatScene.BEATS.length; k++) {
            int strength = k;
            float beat = HeartbeatScene.BEATS[k];
            at(beat, () -> {
                screenShake.shake(0.12f, 3f + strength * 2f);
                shockwaves.ring(centerX, centerY, 260f + strength * 70f, 0.6f, 1f, pink);
                for (int e = 0; e < 6 + strength * 3; e++) {
                    glitter.twinkle(centerX + MathUtils.random(-170f, 170f), centerY + MathUtils.random(-150f, 150f),
                        MathUtils.random(14f, 24f), 0.4f, pale);
                }
            });
            at(beat + 0.08f, () -> shockwaves.ring(centerX, centerY, 200f + strength * 50f, 0.5f, 1f, pale));
        }
        Color[] palette = style.fireworks();
        at(burst, () -> {
            screenShake.shake(0.4f, 12f);
            impactFlash(1f, 0.55f, 0.7f, 0.45f);
        });
        for (int k = 0; k < 3; k++) {
            Color color = palette[k % palette.length];
            at(burst + k * 0.1f, () -> shockwaves.ring(centerX, centerY, 380f, 0.7f, 1f, color));
        }
        // Les petits cœurs jaillissent tout autour puis retombent, et d'autres pleuvent du haut de l'écran.
        emit(burst, burst, 30, i -> symbols.jet(icon, centerX, centerY, i * 360f / 30 + MathUtils.random(-6f, 6f),
            MathUtils.random(450f, 950f), MathUtils.random(45f, 70f), 1300f));
        emit(burst + 0.3f, burst + 1.2f, 30, i -> symbols.rain(icon, tableX(40f), getHeight() + 60f, floorY(),
            MathUtils.random(40f, 65f), MathUtils.random(150f, 400f), 900f, false));
        emit(burst, burst, 80, i -> {
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(250f, 750f);
            glitter.ember(centerX, centerY, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed, 700f,
                MathUtils.random(6f, 11f), MathUtils.random(0.8f, 1.4f), palette[i % palette.length]);
        });
        coinRain(burst, burst + 1f, 18, 1);
    }

    /** Dé : chaque dé qui se pose fait trembler la table ; sur 6-6-6, une gerbe de flammes jaillit des trois. */
    private void scheduleTripleDice(TextureRegion icon) {
        float centerX = playArea.getCenterX(), centerY = getHeight() * 0.34f;
        float flame = TripleDiceScene.FLAME_TIME;
        playScene(tripleDice, centerX, centerY, flame + 0.12f, 0.8f);
        Color[] fire = style.fireworks();
        float half = tripleDice.halfSize();
        for (int k = 0; k < TripleDiceScene.LANDINGS.length; k++) {
            int index = TripleDiceScene.LANDINGS.length - 1 - k, strength = k; // le dé de droite se pose le premier
            at(TripleDiceScene.LANDINGS[k], () -> {
                screenShake.shake(0.2f, 6f + strength * 3f);
                shockwaves.ring(tripleDice.dieX(index), centerY - half, 180f, 0.45f, 0.3f, fire[1]);
            });
        }
        at(flame, () -> {
            screenShake.shake(0.45f, 14f);
            impactFlash(1f, 0.55f, 0.2f, 0.4f);
            for (int i = 0; i < 3; i++) shockwaves.ring(tripleDice.dieX(i), tripleDice.topY(), 220f, 0.6f, 0.4f, fire[0]);
        });
        // La gerbe : un éventail de flammes au-dessus des dés, puis elles continuent de lécher le dessus.
        emit(flame, flame + 0.1f, 120, i -> {
            float angle = MathUtils.random(55f, 125f);
            float speed = MathUtils.random(450f, 1000f);
            glitter.ember(tripleDice.dieX(i % 3) + MathUtils.random(-half, half), tripleDice.topY(),
                MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed, 600f, MathUtils.random(10f, 18f),
                MathUtils.random(0.5f, 0.9f), fire[i % 3]);
        });
        emit(flame, 2.6f, 420, i -> glitter.ember(tripleDice.dieX(i % 3) + MathUtils.random(-half, half),
            tripleDice.topY() - MathUtils.random(0f, half * 0.6f), MathUtils.random(-30f, 30f),
            MathUtils.random(250f, 520f), 0f, MathUtils.random(8f, 16f), MathUtils.random(0.5f, 1.1f), fire[i % 3]));
        emit(flame, flame + 0.5f, 14, i -> symbols.jet(icon, tripleDice.dieX(i % 3), tripleDice.topY(),
            MathUtils.random(60f, 120f), MathUtils.random(900f, 1300f), MathUtils.random(55f, 80f), 1500f));
        for (float time : new float[] {1.8f, 2.1f, 2.4f}) at(time, () -> rocket(false));
        coinRain(flame, flame + 1f, 18, 1);
    }

    /** Étoile : l'étoile filante laisse une traîne de lumière, puis éclate en trois gerbes rouge, bleue et or. */
    private void scheduleShootingStar(TextureRegion icon) {
        float centerX = playArea.getCenterX(), centerY = getHeight() * 0.45f;
        float burst = ShootingStarScene.BURST_TIME;
        playScene(shootingStar, centerX, centerY, burst + 0.12f, 0.82f);
        Color[] trail = {Palette.GOLD, Color.WHITE, Color.valueOf("8fd3ffff")};
        emit(ShootingStarScene.LAUNCH_TIME + 0.03f, burst, 260, i -> {
            Vector2 star = shootingStar.starPosition();
            glitter.ember(star.x + MathUtils.random(-14f, 14f), star.y + MathUtils.random(-14f, 14f),
                MathUtils.random(-25f, 25f), MathUtils.random(-40f, 10f), 0f, MathUtils.random(9f, 18f),
                MathUtils.random(0.4f, 0.8f), trail[i % trail.length]);
        });
        emit(ShootingStarScene.LAUNCH_TIME + 0.03f, burst, 24, i -> {
            Vector2 star = shootingStar.starPosition();
            glitter.twinkle(star.x + MathUtils.random(-30f, 30f), star.y + MathUtils.random(-30f, 30f),
                MathUtils.random(16f, 28f), 0.5f, Color.WHITE);
        });
        Color[] colors = style.fireworks();
        at(burst, () -> {
            screenShake.shake(0.35f, 10f);
            impactFlash(1f, 1f, 0.9f, 0.5f);
        });
        float[][] bursts = {{0f, 0f}, {-190f, 70f}, {190f, 50f}};
        for (int k = 0; k < bursts.length; k++) {
            Color color = colors[k % colors.length];
            float dx = bursts[k][0], dy = bursts[k][1];
            at(burst + k * 0.14f, () -> {
                fireworks.burst(centerX + dx, centerY + dy, color);
                shockwaves.ring(centerX + dx, centerY + dy, 300f, 0.7f, 1f, color);
            });
        }
        emit(burst, burst, 20, i -> symbols.jet(icon, centerX, centerY, i * 360f / 20 + MathUtils.random(-8f, 8f),
            MathUtils.random(500f, 950f), MathUtils.random(50f, 75f), 1100f));
        emit(burst, 2.6f, 60, i -> glitter.twinkle(tableX(0f), getHeight() * MathUtils.random(0.05f, 0.98f),
            MathUtils.random(12f, 26f), 0.5f, colors[i % colors.length]));
        for (float time : new float[] {1.6f, 1.85f, 2.1f, 2.35f}) at(time, () -> rocket(false));
        coinRain(burst, burst + 1f, 18, 1);
    }

    /** Bombe : la mèche grésille pendant le compte à rebours, puis l'explosion secoue tout et laisse un nuage de fumée. */
    private void scheduleBomb(TextureRegion icon) {
        float centerX = playArea.getCenterX(), centerY = getHeight() * 0.42f;
        float blast = BombScene.BLAST_TIME;
        bomb.setReducedEffects(settings.isReducedEffects());
        playScene(bomb, centerX, centerY, blast + 0.12f, 0.8f);
        Color[] sparks = {Palette.GOLD, Palette.ORANGE, Color.WHITE};
        emit(0.25f, blast, 170, i -> {
            Vector2 tip = bomb.fuseTip();
            glitter.ember(tip.x, tip.y, MathUtils.random(-180f, 180f), MathUtils.random(80f, 320f), 900f,
                MathUtils.random(6f, 11f), MathUtils.random(0.25f, 0.45f), sparks[i % sparks.length]);
        });
        for (int k = 0; k < BombScene.TICKS.length; k++) {
            int strength = k;
            at(BombScene.TICKS[k], () -> {
                screenShake.shake(0.1f, 3f + strength * 2f);
                shockwaves.ring(centerX, centerY, 200f + strength * 40f, 0.4f, 1f,
                    strength == 2 ? Palette.RUBY : Color.WHITE);
            });
        }
        Color[] fire = {Color.valueOf("ff3b1fff"), Palette.ORANGE, Palette.GOLD, Color.WHITE};
        at(blast, () -> {
            screenShake.shake(0.7f, 24f);
            impactFlash(1f, 0.85f, 0.6f, 0.8f);
        });
        for (int k = 0; k < 3; k++) {
            Color color = fire[k];
            at(blast + k * 0.1f, () -> shockwaves.ring(centerX, centerY, 700f, 0.7f, 1f, color));
        }
        // Débris enflammés, puis un nuage de fumée qui monte lentement.
        emit(blast, blast, 110, i -> {
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(400f, 1200f);
            glitter.ember(centerX, centerY, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed, 900f,
                MathUtils.random(6f, 12f), MathUtils.random(0.6f, 1.2f), i % 4 == 0 ? SMOKE : fire[i % fire.length]);
        });
        emit(blast + 0.1f, 2.6f, 110, i -> glitter.ember(centerX + MathUtils.random(-320f, 320f),
            centerY + MathUtils.random(-200f, 180f), MathUtils.random(-40f, 40f), MathUtils.random(40f, 140f), 0f,
            MathUtils.random(22f, 40f), MathUtils.random(1f, 1.6f), SMOKE));
        emit(blast, blast, 14, i -> symbols.jet(icon, centerX, centerY, i * 360f / 14 + MathUtils.random(-10f, 10f),
            MathUtils.random(600f, 1100f), MathUtils.random(50f, 75f), 1300f));
        for (float time : new float[] {1.9f, 2.2f, 2.45f}) at(time, () -> rocket(false));
        coinRain(blast, blast + 1f, 24, 1);
    }

    /** Couronne : les trompettes crachent des notes à chaque coup de fanfare ; au sacre, pluie de pièces et de joyaux. */
    private void scheduleCoronation(TextureRegion icon) {
        float centerX = playArea.getCenterX();
        float crowned = CoronationScene.CROWN_TIME;
        playScene(coronation, centerX, getHeight() * 0.03f, crowned + 0.12f, 0.84f);
        Color gold = Color.valueOf("ffe680ff");
        for (float blast : CoronationScene.FANFARE) {
            at(blast, () -> {
                screenShake.shake(0.08f, 3f);
                for (int side = 0; side < 2; side++) {
                    Vector2 bell = coronation.bell(side);
                    shockwaves.ring(bell.x, bell.y, 120f, 0.4f, 1f, gold);
                    float out = side == 0 ? -1f : 1f;
                    for (int n = 0; n < 3; n++) {
                        symbols.rise(coronation.note(), bell.x + out * MathUtils.random(10f, 90f),
                            bell.y + MathUtils.random(-10f, 40f), MathUtils.random(140f, 260f),
                            MathUtils.random(40f, 54f), MathUtils.random(0.9f, 1.3f));
                    }
                    glitter.twinkle(bell.x, bell.y, 30f, 0.3f, Color.WHITE);
                }
            });
        }
        at(crowned, () -> {
            screenShake.shake(0.3f, 8f);
            impactFlash(1f, 0.92f, 0.6f, 0.45f);
        });
        for (int k = 0; k < 3; k++) {
            at(crowned + k * 0.12f, () -> shockwaves.ring(coronation.cushionTop().x, coronation.cushionTop().y + 60f,
                360f, 0.8f, 1f, gold));
        }
        TextureRegion[] jewels = coronation.jewels();
        emit(crowned, crowned + 1.2f, 46, i -> symbols.rain(i % 6 == 0 ? icon : jewels[i % jewels.length], tableX(40f),
            getHeight() + 60f, floorY(), i % 6 == 0 ? MathUtils.random(55f, 75f) : MathUtils.random(32f, 46f),
            MathUtils.random(150f, 400f), 1000f, false));
        emit(crowned, 2.6f, 60, i -> glitter.twinkle(tableX(0f), getHeight() * MathUtils.random(0.05f, 0.98f),
            MathUtils.random(12f, 26f), 0.5f, gold));
        coinRain(crowned, crowned + 1.2f, 100, 2);
    }

    /** Pépite : chaque coup de pioche fait voler éclats et étincelles ; le rocher fendu crache un geyser de pépites. */
    private void scheduleMine(TextureRegion icon) {
        float centerX = playArea.getCenterX();
        float[] hits = MineScene.HITS;
        float last = hits[hits.length - 1];
        playScene(mine, centerX, getHeight() * 0.12f, last + 0.12f, 0.8f);
        Color[] sparks = {Palette.GOLD, Color.WHITE, Palette.GOLD_PALE};
        Color chip = Color.valueOf("8a8a98ff");
        for (int k = 0; k < hits.length; k++) {
            int strength = k;
            at(hits[k], () -> {
                Vector2 hit = mine.strikePoint();
                screenShake.shake(0.2f, 8f + strength * 5f);
                shockwaves.ring(hit.x, hit.y, 160f + strength * 60f, 0.45f, 0.4f, Palette.GOLD);
                for (int e = 0; e < 22 + strength * 12; e++) {
                    float angle = MathUtils.random(20f, 160f);
                    float speed = MathUtils.random(300f, 800f);
                    glitter.ember(hit.x, hit.y, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed, 1500f,
                        MathUtils.random(5f, 10f), MathUtils.random(0.4f, 0.8f),
                        e % 3 == 0 ? chip : sparks[e % sparks.length]);
                }
            });
        }
        at(last, () -> impactFlash(1f, 0.9f, 0.5f, 0.5f));
        // Le geyser : pépites et paillettes d'or jaillissent tout droit du cœur du rocher.
        emit(last, last + 1f, 50, i -> symbols.jet(icon, mine.heart().x + MathUtils.random(-30f, 30f), mine.heart().y,
            MathUtils.random(78f, 102f), MathUtils.random(1000f, 1450f), MathUtils.random(50f, 80f), 1400f));
        emit(last, last + 1.1f, 170, i -> {
            float angle = MathUtils.random(75f, 105f);
            float speed = MathUtils.random(600f, 1250f);
            glitter.ember(mine.heart().x + MathUtils.random(-25f, 25f), mine.heart().y, MathUtils.cosDeg(angle) * speed,
                MathUtils.sinDeg(angle) * speed, 1200f, MathUtils.random(6f, 12f), MathUtils.random(0.7f, 1.2f),
                sparks[i % sparks.length]);
        });
        emit(last, 2.6f, 50, i -> glitter.twinkle(tableX(0f), getHeight() * MathUtils.random(0.1f, 0.95f),
            MathUtils.random(14f, 26f), 0.5f, Color.valueOf("fffbe0ff")));
        coinRain(last, last + 1.2f, 60, 2);
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
        wordAt = BullseyeAnimation.IMPACT_TIME + 0.12f;
        giantWord.play(centerX, getHeight() * BULLSEYE_WORD_Y, wordAt, WORD_FADE_AT,
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
        wordAt = strike + 0.12f;
        giantWord.play(centerX, getHeight() * BELL_WORD_Y, wordAt, WORD_FADE_AT,
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
        this.wordAt = wordAt;
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
        spotlights.dispose();
        for (Texture band : styleBands) band.dispose();
        for (Texture icon : icons.values()) icon.dispose();
        Fonts.release(bannerFont);
        Fonts.release(hintFont);
        bullseye.dispose();
        churchBell.dispose();
        for (BingoScene scene : scenes) scene.dispose();
        giantWord.dispose();
    }
}
