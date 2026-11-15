package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.Palette;

/**
 * Bulle de dialogue d'un ennemi (ses réactions, voir
 * {@link fr.astratime.lucky.entities.enemy.EnemyLines}) : un cadre crème au contour
 * sombre en pixels, comme une bande dessinée, avec une pointe en escalier tournée
 * vers sa bouche. Elle s'ouvre d'un petit saut, reste le temps de la lire puis
 * s'efface d'elle-même ; elle ne bloque ni les clics ni le combat.
 */
public class SpeechBubble extends Group implements Disposable {

    private static final float PAD_X     = 16f;
    private static final float PAD_Y     = 10f;
    private static final float MAX_WIDTH = 380f;   // largeur du texte avant retour à la ligne
    private static final float BORDER    = 3f;
    /** Pointe : marches de pixels, de la bulle vers la bouche de l'ennemi. */
    private static final float STEP      = 6f;
    private static final int   STEPS     = 4;
    private static final float TAIL      = STEP * STEPS;
    private static final float MARGIN    = 8f;     // distance minimale au bord de l'écran
    private static final float APPEAR    = 0.18f;
    private static final float VANISH    = 0.3f;
    /** Temps de lecture : une base, plus un peu par lettre, borné. */
    private static final float READ_BASE = 1.6f, READ_PER_CHAR = 0.045f, READ_MAX = 4.5f;

    private final Texture    pixel;
    private final BitmapFont font  = Fonts.jersey(32, Palette.OUTLINE);
    private final Label      label = new Label("", new Label.LabelStyle(font, Color.WHITE));
    /** La pointe part du bas de la bulle, à gauche (l'ennemi est à gauche) ou à droite. */
    private boolean tailLeft = true;

    /** @param pixel texture d'un pixel blanc (cadre et pointe) */
    public SpeechBubble(Texture pixel) {
        this.pixel = pixel;
        label.setWrap(true);
        label.setAlignment(Align.center);
        addActor(label);
        setTouchable(Touchable.disabled);
        setTransform(true);
        setVisible(false);
    }

    /**
     * Affiche {@code text}, la pointe sur {@code (mouthX, mouthY)} (coordonnées du Stage) ;
     * la bulle s'ouvre au-dessus et à droite de ce point, ou à gauche s'il n'y a pas la place.
     * Une nouvelle phrase remplace la précédente.
     */
    public void say(String text, float mouthX, float mouthY) {
        if (text == null || text.isEmpty()) return;
        label.setText(text);
        Label probe = new Label(text, label.getStyle());
        float textWidth = Math.min(MAX_WIDTH, probe.getPrefWidth());
        label.setWidth(textWidth);
        label.invalidate();
        float textHeight = label.getPrefHeight();
        float width  = textWidth + PAD_X * 2f;
        float height = textHeight + PAD_Y * 2f;
        setSize(width, height);
        label.setBounds(PAD_X, PAD_Y, textWidth, textHeight);

        float worldWidth  = getStage() != null ? getStage().getViewport().getWorldWidth() : Float.MAX_VALUE;
        float worldHeight = getStage() != null ? getStage().getViewport().getWorldHeight() : Float.MAX_VALUE;
        // La pointe monte en diagonale depuis la bouche : la bulle commence une pointe plus haut, décalée.
        tailLeft = mouthX + width + MARGIN <= worldWidth;
        float x = tailLeft ? mouthX - STEP : mouthX - width + STEP;
        float y = mouthY + TAIL;
        x = Math.max(MARGIN, Math.min(worldWidth - width - MARGIN, x));
        y = Math.max(MARGIN + TAIL, Math.min(worldHeight - height - MARGIN, y));
        setPosition(x, y);
        setOrigin(tailLeft ? 0f : width, 0f);

        float read = Math.min(READ_MAX, READ_BASE + READ_PER_CHAR * text.length());
        clearActions();
        setVisible(true);
        getColor().a = 0f;
        setScale(0.6f);
        addAction(Actions.sequence(
            Actions.parallel(Actions.fadeIn(APPEAR), Actions.scaleTo(1f, 1f, APPEAR, Interpolation.swingOut)),
            Actions.delay(read),
            Actions.fadeOut(VANISH),
            Actions.visible(false)));
        toFront();
    }

    /** @return {@code true} tant que la bulle est à l'écran. */
    public boolean isShown() { return isVisible(); }

    /** Efface la bulle tout de suite (fin du combat, nouvel ennemi). */
    public void hide() {
        clearActions();
        setVisible(false);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color previous = batch.getColor().cpy();
        float alpha = getColor().a * parentAlpha;
        applyTransform(batch, computeTransform());
        float w = getWidth(), h = getHeight();
        // Contour, puis fond crème un cran à l'intérieur ; coins coupés d'un pixel de contour (style pixel art).
        batch.setColor(Palette.OUTLINE.r, Palette.OUTLINE.g, Palette.OUTLINE.b, alpha);
        batch.draw(pixel, BORDER, 0f, w - BORDER * 2f, h);
        batch.draw(pixel, 0f, BORDER, w, h - BORDER * 2f);
        drawTail(batch, 0f);
        batch.setColor(Palette.CREAM.r, Palette.CREAM.g, Palette.CREAM.b, alpha);
        batch.draw(pixel, BORDER * 2f, BORDER, w - BORDER * 4f, h - BORDER * 2f);
        batch.draw(pixel, BORDER, BORDER * 2f, w - BORDER * 2f, h - BORDER * 4f);
        drawTail(batch, BORDER);
        resetTransform(batch);
        batch.setColor(previous);
        super.draw(batch, parentAlpha);
    }

    /**
     * La pointe : {@link #STEPS} marches qui descendent sous la bulle vers la bouche,
     * rentrées de {@code inset} (contour, puis fond).
     */
    private void drawTail(Batch batch, float inset) {
        float base = tailLeft ? STEP * 3f : getWidth() - STEP * 3f;
        for (int i = 0; i < STEPS; i++) {
            float width = STEP * (STEPS - i) * 0.75f + STEP - inset * 2f;
            if (width <= 0f) continue;
            float top = -STEP * i, bottom = -STEP * (i + 1);
            float y = bottom + (i == STEPS - 1 ? inset : 0f);
            float height = top - y + (i == 0 ? inset + BORDER : 0f);
            // Chaque marche glisse vers le coin : la pointe vise en biais la bouche.
            float shift = STEP * 0.5f * i;
            float x = tailLeft ? base - shift - width / 2f : base + shift - width / 2f;
            batch.draw(pixel, x + (tailLeft ? inset : -inset) * 0f, y, width, height);
        }
    }

    @Override
    public void dispose() {
        Fonts.release(font);
    }
}
