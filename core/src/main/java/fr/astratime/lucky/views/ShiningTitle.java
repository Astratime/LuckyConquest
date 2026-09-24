package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

/**
 * Titre doré sur lequel un reflet passe à intervalles réguliers : une bande
 * claire balaie le texte de gauche à droite. Le reflet est le même texte dans
 * une police blanche, dessiné seulement à l'intérieur de la bande (découpe) :
 * il n'éclaire que les lettres, pas le fond.
 */
public class ShiningTitle extends Actor {

    private static final float PERIOD      = 4f;     // secondes entre deux reflets
    private static final float SWEEP_TIME  = 0.7f;
    private static final float BAND_WIDTH  = 90f;
    private static final float SHINE_ALPHA = 0.85f;

    private final Label title;
    private final Label shine;
    private float       time = PERIOD - 1f;   // premier reflet peu après l'ouverture

    /**
     * @param titleFont police du titre (dorée)
     * @param shineFont même police, en blanc, pour le reflet
     */
    public ShiningTitle(String text, BitmapFont titleFont, BitmapFont shineFont) {
        title = new Label(text, new Label.LabelStyle(titleFont, Color.WHITE));
        shine = new Label(text, new Label.LabelStyle(shineFont, Color.WHITE));
        title.pack();
        shine.pack();
        setSize(title.getWidth(), title.getHeight());
        setTouchable(Touchable.disabled);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        time = (time + delta) % PERIOD;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        title.setPosition(getX(), getY());
        title.draw(batch, parentAlpha);
        if (time > SWEEP_TIME) return;

        float progress = Interpolation.sine.apply(time / SWEEP_TIME);
        float bandX = getX() - BAND_WIDTH + (getWidth() + BAND_WIDTH) * progress;
        batch.flush();
        if (clipBegin(bandX, getY(), BAND_WIDTH, getHeight())) {
            shine.setPosition(getX(), getY());
            shine.getColor().a = SHINE_ALPHA;
            shine.draw(batch, parentAlpha);
            batch.flush();
            clipEnd();
        }
    }
}
