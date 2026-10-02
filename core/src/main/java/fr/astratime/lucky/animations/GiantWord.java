package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.Palette;

import java.util.ArrayList;
import java.util.List;

/**
 * Le mot « BINGO! » écrit en géant sur l'écran, à la place de la bannière,
 * par les célébrations qui ont leur propre décor (cible du Triple Cerise,
 * cloche de la Cloche) : les lettres surgissent une à une, de travers et de
 * couleurs alternées, respirent, puis le mot s'estompe.
 *
 * Les positions sont celles du Stage : l'acteur doit être placé à l'origine.
 */
public class GiantWord extends Group implements Disposable {

    private static final float LETTER_GAP = 4f;
    private static final float LETTER_STEP = 0.07f;  // entre l'apparition de deux lettres
    private static final float FADE_TIME   = 0.45f;

    private static final String WORD = "BINGO!";

    private final BitmapFont             font    = Fonts.jersey(230, Color.WHITE, 9f, Palette.TEXT_SHADE, WORD);
    private final List<Container<Label>> letters = new ArrayList<>();

    public GiantWord() {
        setTouchable(Touchable.disabled);
        setVisible(false);
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        for (char c : WORD.toCharArray()) {
            Container<Label> letter = new Container<>(new Label(String.valueOf(c), style));
            letter.setTransform(true);
            letter.pack();
            letter.setOrigin(letter.getWidth() / 2f, letter.getHeight() / 2f);
            letters.add(letter);
            addActor(letter);
        }
    }

    /**
     * Écrit le mot centré en {@code (centerX, centerY)}.
     *
     * @param delay   attente avant la première lettre
     * @param fadeAt  instant (depuis cet appel) où le mot commence à s'estomper
     * @param letterA couleur des lettres de rang pair
     * @param letterB couleur des lettres de rang impair
     */
    public void play(float centerX, float centerY, float delay, float fadeAt, Color letterA, Color letterB) {
        hide();
        setVisible(true);
        getColor().a = 1f;

        float width = -LETTER_GAP;
        for (Container<Label> letter : letters) width += letter.getWidth() + LETTER_GAP;
        float x = centerX - width / 2f;
        for (int i = 0; i < letters.size(); i++) {
            Container<Label> letter = letters.get(i);
            letter.getActor().setColor(i % 2 == 0 ? letterA : letterB);
            letter.setPosition(x, centerY - letter.getHeight() / 2f + (i % 2 == 0 ? 10f : -10f));
            x += letter.getWidth() + LETTER_GAP;
            float tilt = (i % 2 == 0 ? 1f : -1f) * MathUtils.random(4f, 9f);
            letter.setRotation(tilt * 3f);
            letter.addAction(Actions.sequence(
                Actions.delay(delay + i * LETTER_STEP),
                Actions.parallel(
                    Actions.scaleTo(1.6f, 1.6f, 0.1f, Interpolation.pow2Out),
                    Actions.rotateTo(tilt, 0.18f, Interpolation.pow2Out)),
                Actions.scaleTo(1f, 1f, 0.14f, Interpolation.pow2In),
                Actions.forever(Actions.sequence(
                    Actions.scaleTo(1.06f, 1.06f, 0.3f, Interpolation.sine),
                    Actions.scaleTo(1f, 1f, 0.3f, Interpolation.sine)))));
        }
        addAction(Actions.sequence(Actions.delay(fadeAt), Actions.fadeOut(FADE_TIME), Actions.visible(false)));
    }

    /** Cache le mot immédiatement. */
    public void hide() {
        clearActions();
        setVisible(false);
        for (Container<Label> letter : letters) {
            letter.clearActions();
            letter.setScale(0f);
        }
    }

    @Override
    public void dispose() {
        Fonts.release(font);
    }
}
