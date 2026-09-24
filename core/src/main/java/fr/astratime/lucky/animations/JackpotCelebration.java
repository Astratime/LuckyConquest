package fr.astratime.lucky.animations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
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
import fr.astratime.lucky.views.PlayArea;

import java.util.function.Supplier;

/**
 * Célébration du jackpot, par-dessus tout l'écran de jeu : un flash blanc et
 * une secousse de l'écran, la bannière « BINGO!!! » qui traverse l'écran avec
 * ses éclairs, une pluie de pièces d'or (dont une partie file vers le compteur
 * des gains) et des feux d'artifice qui éclatent au-dessus de la table, côté
 * adverse.
 *
 * Pendant {@link #DURATION}, le calque intercepte les clics : le joueur ne peut
 * pas continuer avant la fin. Les dernières pièces et étincelles finissent
 * ensuite de tomber sans bloquer le jeu.
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

    private final PlayArea   playArea;
    private final Texture    pixelTexture = Textures.solidColor(Color.WHITE);
    private final Texture    coinTexture  = new Texture(Gdx.files.internal("jackpot/coin_spin.png"));
    private final Texture    bandTexture  = new Texture(Gdx.files.internal("jackpot/banner_band.png"));
    private final BitmapFont bannerFont   = Fonts.jersey(170, Color.WHITE, 7f, Color.valueOf("12080aff"));

    private final Image       flash;
    private final Fireworks   fireworks;
    private final CoinShower  coins;
    private final BingoBanner banner;

    private boolean  running;
    private boolean  shaking;
    private float    elapsed;
    private int      coinsDropped;
    private int      rocketsLaunched;
    private Runnable onFinished;

    /**
     * @param coinTarget      position (Stage) de la pièce du compteur des gains
     * @param onCoinCollected appelé à l'arrivée de chaque pièce sur le compteur
     */
    public JackpotCelebration(PlayArea playArea, Supplier<Vector2> coinTarget, Runnable onCoinCollected) {
        this.playArea = playArea;
        setTouchable(Touchable.disabled);

        TextureRegion pixel = new TextureRegion(pixelTexture);
        fireworks = new Fireworks(pixel);
        coins     = new CoinShower(new TextureRegion(coinTexture), coinTarget, onCoinCollected);
        banner    = new BingoBanner(bandTexture, pixel, bannerFont);
        flash     = new Image(new TextureRegionDrawable(pixel));
        flash.setTouchable(Touchable.disabled);
        flash.getColor().a = 0f;

        addActor(fireworks);
        addActor(coins);
        addActor(banner);
        addActor(flash);
    }

    /** Lance la célébration ; {@code onFinished} est appelé au bout de {@link #DURATION}. */
    public void play(Runnable onFinished) {
        cancel();
        this.onFinished = onFinished;
        running         = true;
        shaking         = true;
        elapsed         = 0f;
        coinsDropped    = 0;
        rocketsLaunched = 0;

        float width  = getStage().getViewport().getWorldWidth();
        float height = getStage().getViewport().getWorldHeight();
        setBounds(0f, 0f, width, height);
        setTouchable(Touchable.enabled); // intercepte les clics jusqu'à la fin

        flash.setBounds(0f, 0f, width, height);
        flash.getColor().a = FLASH_ALPHA;
        flash.addAction(Actions.fadeOut(FLASH_TIME));

        banner.play(playArea.getCenterX(), height * BANNER_Y, width);
    }

    /** Arrête tout immédiatement (nouvelle partie), sans appeler la fin de la célébration. */
    public void cancel() {
        running = false;
        setTouchable(Touchable.disabled);
        coins.clear();
        fireworks.clear();
        banner.hide();
        flash.clearActions();
        flash.getColor().a = 0f;
        stopShake();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!running) return;
        elapsed += delta;
        float height = getHeight();

        int coinsDue = Math.round(COIN_COUNT * Math.min(1f, elapsed / COIN_SPAWN_TIME));
        for (; coinsDropped < coinsDue; coinsDropped++) {
            float x     = MathUtils.random(playArea.getX(), playArea.getX() + playArea.getWidth());
            float floor = height * MathUtils.random(COIN_FLOOR_MIN, COIN_FLOOR_MAX);
            coins.drop(x, height, floor, coinsDropped % COLLECT_EVERY == 0);
        }

        for (; rocketsLaunched < ROCKET_TIMES.length && elapsed >= ROCKET_TIMES[rocketsLaunched]; rocketsLaunched++) {
            float x = MathUtils.random(playArea.getX() + ROCKET_SIDE_MARGIN,
                playArea.getX() + playArea.getWidth() - ROCKET_SIDE_MARGIN);
            boolean low  = ROCKET_TIMES[rocketsLaunched] < LOW_ROCKETS_UNTIL;
            float   apex = low ? MathUtils.random(LOW_APEX_MIN, LOW_APEX_MAX) : MathUtils.random(APEX_MIN, APEX_MAX);
            fireworks.launch(x, height * ROCKET_FROM, height * apex);
        }

        if (elapsed < SHAKE_TIME) {
            float strength = SHAKE * (1f - elapsed / SHAKE_TIME);
            moveCamera(MathUtils.random(-strength, strength), MathUtils.random(-strength, strength));
        } else if (shaking) {
            stopShake();
        }

        if (elapsed >= DURATION) {
            running = false;
            setTouchable(Touchable.disabled);
            onFinished.run();
        }
    }

    private void stopShake() {
        if (getStage() != null) moveCamera(0f, 0f);
        shaking = false;
    }

    /** Décale la caméra de {@code (dx, dy)} par rapport au centre de l'écran (sa position au repos). */
    private void moveCamera(float dx, float dy) {
        Camera camera = getStage().getCamera();
        camera.position.set(getStage().getViewport().getWorldWidth() / 2f + dx,
            getStage().getViewport().getWorldHeight() / 2f + dy, 0f);
        camera.update();
    }

    @Override
    public void dispose() {
        pixelTexture.dispose();
        coinTexture.dispose();
        bandTexture.dispose();
        bannerFont.dispose();
    }
}
