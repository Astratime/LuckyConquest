package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Bannière « BINGO!!! » qui traverse l'écran : une bande rouge et or inclinée,
 * doublée d'une bande noire de travers derrière elle, entre en trombe par la
 * gauche, tremble, dérive un instant pendant que des éclairs crépitent sur
 * ses bords, puis ressort à droite. Les lettres surgissent une à une, chacune
 * légèrement de travers et d'une couleur alternée.
 */
public class BingoBanner extends Group {

    private static final String TEXT         = "BINGO!!!";
    private static final float  ANGLE        = 8f;     // inclinaison de la bannière
    private static final float  BACK_ANGLE   = 4f;     // inclinaison supplémentaire de la bande noire
    private static final float  BAND_HEIGHT  = 198f;   // 2 x la hauteur de l'image : pixels nets
    private static final float  LENGTH_RATIO = 1.6f;   // longueur de la bande / largeur de l'écran
    private static final float  LETTER_GAP   = 6f;

    private static final float ENTER = 0.28f;
    private static final float HOLD  = 1.3f;
    private static final float EXIT  = 0.3f;
    /** Durée de la traversée complète (entrée, pause, sortie). */
    public static final float  DURATION = ENTER + HOLD + EXIT;
    private static final float SHAKE = 14f;
    private static final float DRIFT = 50f;   // glissement pendant la pause

    private static final Color GOLD = Color.valueOf("ffd54aff");

    private final Image                  backBand;
    private final Image                  band;
    private final List<Container<Label>> letters = new ArrayList<>();
    private final LightningBolts         lightning;

    /**
     * @param bandTexture profil vertical de la bande (étiré horizontalement)
     * @param pixel       région d'un pixel blanc (bande noire, éclairs)
     * @param font        grande police à contour pour les lettres
     */
    public BingoBanner(Texture bandTexture, TextureRegion pixel, BitmapFont font) {
        setTransform(true); // nécessaire pour la rotation du groupe
        setTouchable(Touchable.disabled);
        setVisible(false);

        backBand = new Image(new TextureRegionDrawable(pixel));
        backBand.setColor(0.06f, 0.02f, 0.03f, 0.92f);
        addActor(backBand);

        band = new Image(new TextureRegionDrawable(new TextureRegion(bandTexture)));
        addActor(band);

        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        for (int i = 0; i < TEXT.length(); i++) {
            Label label = new Label(String.valueOf(TEXT.charAt(i)), style);
            label.setColor(i % 2 == 0 ? GOLD : Color.WHITE);
            Container<Label> letter = new Container<>(label);
            letter.setTransform(true);
            letter.pack();
            letter.setOrigin(letter.getWidth() / 2f, letter.getHeight() / 2f);
            letter.setRotation((i % 2 == 0 ? 1 : -1) * MathUtils.random(4f, 9f));
            letters.add(letter);
            addActor(letter);
        }

        lightning = new LightningBolts(pixel);
        addActor(lightning);
    }

    /**
     * Fait traverser l'écran à la bannière, centrée en {@code (centerX, centerY)}
     * pendant sa pause.
     *
     * @param screenWidth largeur de l'écran, pour partir et sortir hors champ
     */
    public void play(float centerX, float centerY, float screenWidth) {
        clearActions();
        layoutFor(screenWidth);

        float heldX  = centerX - getWidth() / 2f;
        float heldY  = centerY - getHeight() / 2f;
        setPosition(-getWidth() - screenWidth * 0.2f, heldY);
        setVisible(true);

        SequenceAction shake = Actions.sequence();
        for (int i = 0; i < 8; i++) {
            float strength = SHAKE * (1f - i / 8f);
            shake.addAction(Actions.moveTo(heldX + MathUtils.random(-strength, strength),
                heldY + MathUtils.random(-strength, strength), 0.04f));
        }
        shake.addAction(Actions.moveTo(heldX, heldY, 0.04f));

        addAction(Actions.sequence(
            Actions.moveTo(heldX, heldY, ENTER, Interpolation.pow3Out),
            shake,
            Actions.moveBy(DRIFT, 0f, HOLD - 9 * 0.04f),
            Actions.moveTo(screenWidth + screenWidth * 0.2f, heldY, EXIT, Interpolation.pow3In),
            Actions.visible(false)));

        for (int i = 0; i < letters.size(); i++) {
            Container<Label> letter = letters.get(i);
            letter.clearActions();
            letter.setScale(0f);
            letter.addAction(Actions.sequence(
                Actions.delay(ENTER * 0.6f + i * 0.045f),
                Actions.scaleTo(1.4f, 1.4f, 0.08f, Interpolation.pow2Out),
                Actions.scaleTo(1f, 1f, 0.12f, Interpolation.pow2In)));
        }

        lightning.clearActions();
        lightning.setVisible(false);
        lightning.addAction(Actions.sequence(
            Actions.delay(ENTER), Actions.visible(true), Actions.delay(HOLD), Actions.visible(false)));
    }

    /** Cache la bannière immédiatement. */
    public void hide() {
        clearActions();
        setVisible(false);
    }

    /** Dimensionne la bande pour l'écran, place les lettres au centre et les ancrages des éclairs sur les bords. */
    private void layoutFor(float screenWidth) {
        float length = screenWidth * LENGTH_RATIO;
        setSize(length, BAND_HEIGHT);
        setOrigin(length / 2f, BAND_HEIGHT / 2f);
        setRotation(ANGLE);

        band.setBounds(0f, 0f, length, BAND_HEIGHT);
        backBand.setBounds(-60f, -34f, length + 120f, BAND_HEIGHT + 68f);
        backBand.setOrigin(backBand.getWidth() / 2f, backBand.getHeight() / 2f);
        backBand.setRotation(BACK_ANGLE);

        float textWidth = -LETTER_GAP;
        for (Container<Label> letter : letters) textWidth += letter.getWidth() + LETTER_GAP;
        float x = (length - textWidth) / 2f;
        for (int i = 0; i < letters.size(); i++) {
            Container<Label> letter = letters.get(i);
            float offsetY = i % 2 == 0 ? 8f : -8f;
            letter.setPosition(x, (BAND_HEIGHT - letter.getHeight()) / 2f + offsetY);
            x += letter.getWidth() + LETTER_GAP;
        }

        lightning.setPosition(0f, 0f);
        lightning.clearAnchors();
        float start = (length - textWidth) / 2f - 80f;
        for (float ax = start; ax <= start + textWidth + 160f; ax += 110f) {
            lightning.addAnchor(ax, BAND_HEIGHT - 6f, 90f + MathUtils.random(-35f, 35f), MathUtils.random(110f, 190f));
            lightning.addAnchor(ax + 55f, 6f, -90f + MathUtils.random(-35f, 35f), MathUtils.random(110f, 190f));
        }
    }
}
