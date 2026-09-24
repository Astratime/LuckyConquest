package fr.astratime.lucky.animations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.popups.EffectPopup;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Fait apparaître un texte par bonus ou résultat, à un endroit donné (carte
 * jouée, symbole tiré, barre de vie...) — ex : "GAINS x+20" en doré. Il surgit
 * en grossissant, monte doucement puis s'efface.
 *
 * La taille du texte suit l'intensité du bonus ({@link EffectPopup#getIntensity()}),
 * entre MIN_FONT_SIZE (toujours lisible) et MAX_FONT_SIZE. La couleur dépend
 * de la famille d'effet. La police est générée une fois à la taille maximale
 * puis réduite, pour rester nette.
 */
public class EffectPopupAnimator implements Disposable {

    private static final String FONT_PATH       = "fonts/Jersey10-Regular.ttf";
    private static final int    MAX_FONT_SIZE   = 72;
    private static final int    MIN_FONT_SIZE   = 32;
    private static final int    FONT_BORDER_PX  = 3;

    private static final float SCREEN_MARGIN      = 10f;
    private static final float LINE_SPACING       = 0.9f;  // en fraction de la hauteur du texte précédent
    private static final float STAGGER_DELAY      = 0.12f;
    private static final float POP_IN_DURATION    = 0.15f;
    private static final float POP_SETTLE_DURATION = 0.1f;
    private static final float POP_OVERSHOOT      = 1.15f;
    private static final float RISE_DISTANCE      = 70f;
    private static final float RISE_DURATION      = 1.1f;
    private static final float FADE_OUT_DELAY     = 0.6f;
    private static final float FADE_OUT_DURATION  = 0.5f;

    private static final Map<EffectPopup.Style, Color> COLORS = new EnumMap<>(EffectPopup.Style.class);
    static {
        COLORS.put(EffectPopup.Style.GAINS,   new Color(1f, 0.82f, 0.2f, 1f));
        COLORS.put(EffectPopup.Style.ATTACK,  new Color(1f, 0.35f, 0.25f, 1f));
        COLORS.put(EffectPopup.Style.DEFENSE, new Color(0.45f, 0.7f, 1f, 1f));
        COLORS.put(EffectPopup.Style.DRAIN,   new Color(1f, 0.45f, 0.7f, 1f));
        COLORS.put(EffectPopup.Style.REFLECT, new Color(0.3f, 0.95f, 0.95f, 1f));
        COLORS.put(EffectPopup.Style.DRAW,    Color.WHITE);
        COLORS.put(EffectPopup.Style.SPECIAL, new Color(0.75f, 0.5f, 1f, 1f));
        COLORS.put(EffectPopup.Style.DAMAGE,  new Color(0.9f, 0.1f, 0.1f, 1f));
    }

    private final Group            layer;
    private final BitmapFont       font;
    private final Label.LabelStyle labelStyle;

    /** @param layer groupe (plein écran, à l'origine du Stage) dans lequel les textes sont ajoutés. */
    public EffectPopupAnimator(Group layer) {
        this.layer = layer;
        this.layer.setTouchable(Touchable.disabled);

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_PATH));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size        = MAX_FONT_SIZE;
        parameter.color       = Color.WHITE; // teinté par la couleur du Label
        parameter.borderWidth = FONT_BORDER_PX;
        parameter.borderColor = Color.BLACK;
        parameter.minFilter   = Texture.TextureFilter.Linear;
        parameter.magFilter   = Texture.TextureFilter.Linear;
        font = generator.generateFont(parameter);
        generator.dispose();

        labelStyle = new Label.LabelStyle(font, Color.WHITE);
    }

    /**
     * Affiche {@code popups} empilés (le premier en haut), centrés sur
     * {@code (centerX, centerY)} (coordonnées du Stage), sans sortir de l'écran.
     */
    public void play(List<EffectPopup> popups, float centerX, float centerY) {
        play(popups, centerX, centerY, 0f);
    }

    /**
     * Comme {@link #play(List, float, float)}, mais le premier texte n'apparaît
     * qu'après {@code startDelay} secondes (pour enchaîner plusieurs groupes de textes).
     */
    public void play(List<EffectPopup> popups, float centerX, float centerY, float startDelay) {
        float worldWidth = layer.getStage().getViewport().getWorldWidth();
        float y = centerY;

        for (int i = 0; i < popups.size(); i++) {
            EffectPopup popup = popups.get(i);

            Label label = new Label(popup.getText(), labelStyle);
            label.setColor(COLORS.get(popup.getStyle()));
            label.setFontScale(fontSizeFor(popup) / (float) MAX_FONT_SIZE);

            Container<Label> container = new Container<>(label);
            container.setTransform(true); // nécessaire pour animer l'échelle
            container.pack();
            float width  = container.getWidth();
            float height = container.getHeight();
            container.setOrigin(width / 2f, height / 2f);

            if (i > 0) y -= height * LINE_SPACING;
            float x = Math.max(SCREEN_MARGIN, Math.min(worldWidth - width - SCREEN_MARGIN, centerX - width / 2f));
            container.setPosition(x, y - height / 2f);
            container.setScale(0.2f);
            container.getColor().a = 0f;
            layer.addActor(container);

            container.addAction(Actions.sequence(
                Actions.delay(startDelay + i * STAGGER_DELAY),
                Actions.parallel(
                    Actions.fadeIn(POP_IN_DURATION),
                    Actions.scaleTo(POP_OVERSHOOT, POP_OVERSHOOT, POP_IN_DURATION, Interpolation.pow2Out)
                ),
                Actions.scaleTo(1f, 1f, POP_SETTLE_DURATION),
                Actions.parallel(
                    Actions.moveBy(0f, RISE_DISTANCE, RISE_DURATION, Interpolation.pow2Out),
                    Actions.sequence(Actions.delay(FADE_OUT_DELAY), Actions.fadeOut(FADE_OUT_DURATION))
                ),
                Actions.removeActor()
            ));
        }
    }

    /** Retire immédiatement tous les textes encore affichés. */
    public void cancel() {
        layer.clearChildren();
    }

    /** @return la taille de police (en pixels) correspondant à l'intensité du popup. */
    private static float fontSizeFor(EffectPopup popup) {
        return MIN_FONT_SIZE + (MAX_FONT_SIZE - MIN_FONT_SIZE) * popup.getIntensity();
    }

    @Override
    public void dispose() {
        font.dispose();
    }
}
