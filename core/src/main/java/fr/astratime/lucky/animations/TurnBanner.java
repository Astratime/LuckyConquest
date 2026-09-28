package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.Textures;

/**
 * Annonce de changement de tour (« À TOI DE JOUER ! », « TOUR ENNEMI ») : une
 * bande aux galons dorés s'ouvre au milieu de la zone de jeu, le titre la
 * traverse en ralentissant au centre, puis la bande se referme. Bleue pour le
 * joueur, rouge pour l'ennemi. Elle ne bloque pas les clics.
 */
public class TurnBanner extends Group implements Disposable {

    /** Durée totale de l'annonce. */
    public static final float DURATION = 1.1f;

    private static final float OPEN      = 0.14f;
    private static final float TEXT_IN   = 0.22f;
    private static final float HOLD      = 0.5f;
    private static final float TEXT_OUT  = 0.16f;
    private static final float CLOSE     = DURATION - OPEN - HOLD - TEXT_OUT; // la bande se referme derrière le titre
    private static final float BAND_HEIGHT = 128f;  // 99 px d'image, ~1,3x : galons bien visibles
    private static final float TEXT_TRAVEL = 260f;  // le titre arrive de la droite et repart à gauche

    /** Camp dont c'est le tour. */
    public enum Side { PLAYER, ENEMY }

    private final Texture    playerBand = Textures.bannerBand(Color.valueOf("1d3a6eff"), Color.valueOf("ffd54aff"));
    private final Texture    enemyBand  = Textures.bannerBand(Color.valueOf("7a0f12ff"), Color.valueOf("ff8a1fff"));
    private final BitmapFont font       = Fonts.jersey(84, Color.WHITE, 5f, Color.valueOf("12080aff"));

    private final Image band;
    private final Label title;

    public TurnBanner() {
        setTouchable(Touchable.disabled);
        setVisible(false);
        band  = new Image(new TextureRegionDrawable(new TextureRegion(playerBand)));
        title = new Label("", new Label.LabelStyle(font, Color.WHITE));
        title.setAlignment(Align.center);
        addActor(band);
        addActor(title);
    }

    /**
     * Annonce le tour de {@code side} sur une bande de largeur {@code width}
     * centrée en {@code (centerX, centerY)}.
     */
    public void play(Side side, float centerX, float centerY, float width) {
        clearActions();
        band.clearActions();
        title.clearActions();
        setVisible(true);
        getColor().a = 1f;

        boolean player = side == Side.PLAYER;
        ((TextureRegionDrawable) band.getDrawable()).setRegion(new TextureRegion(player ? playerBand : enemyBand));
        title.setText(player ? "À TOI DE JOUER !" : "TOUR ENNEMI");
        title.setColor(player ? Color.valueOf("ffd54aff") : Color.valueOf("ffb0a0ff"));
        title.pack();

        band.setBounds(centerX - width / 2f, centerY - BAND_HEIGHT / 2f, width, BAND_HEIGHT);
        band.setOrigin(width / 2f, BAND_HEIGHT / 2f);
        band.setScale(1f, 0f);
        band.addAction(Actions.sequence(
            Actions.scaleTo(1f, 1f, OPEN, Interpolation.pow2Out),
            Actions.delay(TEXT_IN + HOLD - OPEN + TEXT_OUT * 0.5f),
            Actions.scaleTo(1f, 0f, CLOSE + TEXT_OUT * 0.5f + OPEN, Interpolation.pow2In)));

        float textX = centerX - title.getWidth() / 2f;
        float textY = centerY - title.getHeight() / 2f;
        title.setPosition(textX + TEXT_TRAVEL, textY);
        title.getColor().a = 0f;
        title.addAction(Actions.sequence(
            Actions.delay(OPEN * 0.5f),
            Actions.parallel(Actions.moveTo(textX, textY, TEXT_IN, Interpolation.pow3Out), Actions.fadeIn(TEXT_IN * 0.6f)),
            Actions.moveBy(-20f, 0f, HOLD),
            Actions.parallel(Actions.moveBy(-TEXT_TRAVEL, 0f, TEXT_OUT, Interpolation.pow2In), Actions.fadeOut(TEXT_OUT))));

        addAction(Actions.sequence(Actions.delay(DURATION), Actions.visible(false)));
    }

    /** Cache l'annonce immédiatement. */
    public void hide() {
        clearActions();
        band.clearActions();
        title.clearActions();
        setVisible(false);
    }

    @Override
    public void dispose() {
        playerBand.dispose();
        enemyBand.dispose();
        font.dispose();
    }
}
