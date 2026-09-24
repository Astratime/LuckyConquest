package fr.astratime.lucky.animations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.views.PlayArea;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Mise en scène de la fin d'un combat, par-dessus l'écran de jeu.
 *
 * Victoire : l'ennemi explose en jetons de casino qui volent et retombent,
 * la bannière « VICTOIRE ! » traverse l'écran, des feux d'artifice éclatent
 * et des confettis pleuvent. Défaite : la zone de jeu s'assombrit, des cartes
 * s'éparpillent en tournoyant et « DÉFAITE » tombe au centre de l'écran.
 *
 * Pendant la mise en scène, le calque intercepte les clics ; la fin est
 * ensuite signalée (pour afficher le bouton « Recommencer »). L'assombrissement
 * et le texte de la défaite restent jusqu'à {@link #reset()}.
 */
public class CombatEndAnimation extends Group implements Disposable {

    public static final float VICTORY_DURATION = 2.8f;
    public static final float DEFEAT_DURATION  = 2.4f;

    private static final int   CHIP_COUNT     = 60;
    private static final int   CARD_COUNT     = 18;
    private static final float CARD_SCALE     = 0.8f;
    private static final int   VICTORY_CONFETTI = 160;
    private static final float[] VICTORY_ROCKETS = {0.4f, 0.8f, 1.2f, 1.6f, 2.0f};
    private static final float DARKEN_ALPHA   = 0.6f;
    private static final float DARKEN_TIME    = 1.2f;
    private static final Color DEFEAT_COLOR   = Color.valueOf("c8c0c0ff");

    private final PlayArea    playArea;
    private final ScreenShake screenShake;
    private final Confetti    confetti;
    private final float       cardWidth;
    private final float       cardHeight;

    private final Texture     chipsTexture = new Texture(Gdx.files.internal("jackpot/chips.png"));
    private final Texture     bandTexture  = new Texture(Gdx.files.internal("jackpot/banner_band.png"));
    private final BitmapFont  bigFont      = Fonts.jersey(170, Color.WHITE, 7f, Color.valueOf("12080aff"));
    private final TextureRegion[] chips;
    private final TextureRegion   cardBack;

    private final Image       darken;
    private final Fireworks   fireworks;
    private final Debris      debris = new Debris();
    private final BingoBanner victoryBanner;
    private final Label       defeatLabel;

    private float    elapsed;
    private float    duration;
    private boolean  running;
    private boolean  victory;
    private int      rocketsLaunched;
    private Runnable onFinished;

    /**
     * @param pixel    région d'un pixel blanc (assombrissement, étincelles)
     * @param cardBack dos de carte, pour les cartes qui s'éparpillent à la défaite
     * @param confetti confettis partagés de l'écran de jeu
     */
    public CombatEndAnimation(PlayArea playArea, ScreenShake screenShake, TextureRegion pixel,
                              TextureRegion cardBack, Confetti confetti, float cardWidth, float cardHeight) {
        this.playArea    = playArea;
        this.screenShake = screenShake;
        this.confetti    = confetti;
        this.cardBack    = cardBack;
        this.cardWidth   = cardWidth;
        this.cardHeight  = cardHeight;
        setTouchable(Touchable.disabled);

        int chipSize = chipsTexture.getHeight();
        chips = new TextureRegion[chipsTexture.getWidth() / chipSize];
        for (int i = 0; i < chips.length; i++) chips[i] = new TextureRegion(chipsTexture, i * chipSize, 0, chipSize, chipSize);

        darken = new Image(new TextureRegionDrawable(pixel));
        darken.setColor(0f, 0f, 0f, 0f);
        darken.setTouchable(Touchable.disabled);
        fireworks     = new Fireworks(pixel);
        victoryBanner = new BingoBanner("VICTOIRE !", bandTexture, pixel, bigFont);
        defeatLabel   = new Label("DÉFAITE", new Label.LabelStyle(bigFont, Color.WHITE));
        defeatLabel.setColor(DEFEAT_COLOR);
        defeatLabel.setVisible(false);

        addActor(darken);
        addActor(fireworks);
        addActor(debris);
        addActor(victoryBanner);
        addActor(defeatLabel);
    }

    /** Victoire : l'ennemi, dont le jeton est en {@code enemyCenter} (Stage), explose en jetons. */
    public void playVictory(Vector2 enemyCenter, Runnable onFinished) {
        start(true, VICTORY_DURATION, onFinished);
        for (int i = 0; i < CHIP_COUNT; i++) {
            float angle = MathUtils.random(20f, 160f); // éventail vers le haut
            float speed = MathUtils.random(350f, 850f);
            float size  = chips[0].getRegionWidth() * (MathUtils.randomBoolean() ? 3f : 4f);
            debris.add(chips[MathUtils.random(chips.length - 1)], enemyCenter.x, enemyCenter.y,
                MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed, size, size);
        }
        victoryBanner.play(playArea.getCenterX(), getHeight() * 0.62f, getWidth());
        confetti.rain(playArea.getX(), playArea.getX() + playArea.getWidth(), getHeight(), VICTORY_CONFETTI);
        screenShake.shake(0.4f, 10f);
    }

    /** Défaite : la table s'assombrit, les cartes s'éparpillent et « DÉFAITE » tombe au centre. */
    public void playDefeat(Runnable onFinished) {
        start(false, DEFEAT_DURATION, onFinished);
        darken.setBounds(playArea.getX(), 0f, playArea.getWidth(), getHeight()); // le panneau (Recommencer) reste clair
        darken.addAction(Actions.alpha(DARKEN_ALPHA, DARKEN_TIME));

        float fromY = getHeight() * 0.38f;
        for (int i = 0; i < CARD_COUNT; i++) {
            float angle = MathUtils.random(20f, 160f);
            float speed = MathUtils.random(400f, 900f);
            debris.add(cardBack, playArea.getCenterX() + MathUtils.random(-60f, 60f), fromY,
                MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed,
                cardWidth * CARD_SCALE, cardHeight * CARD_SCALE);
        }

        defeatLabel.pack();
        float x = playArea.getCenterX() - defeatLabel.getWidth() / 2f;
        float y = getHeight() * 0.55f;
        defeatLabel.setPosition(x, getHeight());
        defeatLabel.getColor().a = 0f;
        defeatLabel.setVisible(true);
        defeatLabel.addAction(Actions.sequence(
            Actions.delay(0.5f),
            Actions.parallel(Actions.fadeIn(0.3f), Actions.moveTo(x, y, 0.9f, Interpolation.bounceOut))));
        screenShake.shake(0.35f, 8f);
    }

    /** Efface tout (nouveau combat), sans signaler de fin. */
    public void reset() {
        running = false;
        setTouchable(Touchable.disabled);
        darken.clearActions();
        darken.getColor().a = 0f;
        defeatLabel.clearActions();
        defeatLabel.setVisible(false);
        victoryBanner.hide();
        fireworks.removeAll();
        debris.removeAll();
    }

    private void start(boolean victory, float duration, Runnable onFinished) {
        reset();
        this.victory     = victory;
        this.duration    = duration;
        this.onFinished  = onFinished;
        elapsed          = 0f;
        rocketsLaunched  = 0;
        running          = true;
        setBounds(0f, 0f, getStage().getViewport().getWorldWidth(), getStage().getViewport().getWorldHeight());
        setTouchable(Touchable.enabled); // intercepte les clics jusqu'à la fin
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!running) return;
        elapsed += delta;
        if (victory) {
            for (; rocketsLaunched < VICTORY_ROCKETS.length && elapsed >= VICTORY_ROCKETS[rocketsLaunched];
                 rocketsLaunched++) {
                float x = MathUtils.random(playArea.getX() + 150f, playArea.getX() + playArea.getWidth() - 150f);
                fireworks.launch(x, getHeight() * 0.1f, getHeight() * MathUtils.random(0.45f, 0.85f));
            }
        }
        if (elapsed >= duration) {
            running = false;
            setTouchable(Touchable.disabled);
            onFinished.run();
        }
    }

    @Override
    public void dispose() {
        chipsTexture.dispose();
        bandTexture.dispose();
        bigFont.dispose();
    }

    /** Objets projetés (jetons, cartes) : ils volent en tournoyant, retombent et sortent de l'écran. */
    private static final class Debris extends Actor {

        private static final float GRAVITY = -1300f;

        private static final class Piece {
            TextureRegion region;
            float x, y, vx, vy, rotation, spin, width, height;
        }

        private final List<Piece> pieces = new ArrayList<>();

        Debris() {
            setTouchable(Touchable.disabled);
        }

        void add(TextureRegion region, float x, float y, float vx, float vy, float width, float height) {
            Piece piece = new Piece();
            piece.region   = region;
            piece.x        = x;
            piece.y        = y;
            piece.vx       = vx;
            piece.vy       = vy;
            piece.rotation = MathUtils.random(360f);
            piece.spin     = MathUtils.random(-540f, 540f);
            piece.width    = width;
            piece.height   = height;
            pieces.add(piece);
        }

        void removeAll() { pieces.clear(); }

        @Override
        public void act(float delta) {
            super.act(delta);
            Iterator<Piece> it = pieces.iterator();
            while (it.hasNext()) {
                Piece piece = it.next();
                piece.vy += GRAVITY * delta;
                piece.x  += piece.vx * delta;
                piece.y  += piece.vy * delta;
                piece.rotation += piece.spin * delta;
                if (piece.y < -Math.max(piece.width, piece.height)) it.remove(); // sorti par le bas
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            float r = batch.getColor().r, g = batch.getColor().g, b = batch.getColor().b, a = batch.getColor().a;
            batch.setColor(1f, 1f, 1f, parentAlpha);
            for (Piece piece : pieces) {
                batch.draw(piece.region, piece.x - piece.width / 2f, piece.y - piece.height / 2f,
                    piece.width / 2f, piece.height / 2f, piece.width, piece.height, 1f, 1f, piece.rotation);
            }
            batch.setColor(r, g, b, a);
        }
    }
}
