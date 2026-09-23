package fr.astratime.lucky.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

/**
 * Barre de vie : fond (piste), remplissage proportionnel aux PV, et label
 * "PV/PV max" centré dessus. Réutilisée à l'identique pour l'ennemi et pour
 * le joueur (seules la couleur et la position diffèrent).
 */
public class HealthBarView implements Disposable {

    private final float width;

    private final Texture bgTexture;
    private final Texture fillTexture;
    private final Image   bg;
    private final Image   fill;
    private final Label   label;

    public HealthBarView(BitmapFont font, Color bgColor, Color fillColor, float width, float height) {
        this.width = width;

        bgTexture   = Textures.solidColor(bgColor);
        fillTexture = Textures.solidColor(fillColor);

        bg = new Image(new TextureRegionDrawable(new TextureRegion(bgTexture)));
        bg.setSize(width, height);

        fill = new Image(new TextureRegionDrawable(new TextureRegion(fillTexture)));
        fill.setSize(width, height);

        label = new Label("", new Label.LabelStyle(font, Color.WHITE));
        label.setSize(width, height);
        label.setAlignment(Align.center);
    }

    /** Ajoute les trois acteurs de la barre au Stage (fond, remplissage, label), dans cet ordre. */
    public void addTo(Stage stage) {
        stage.addActor(bg);
        stage.addActor(fill);
        stage.addActor(label);
    }

    /** Repositionne les trois acteurs à {@code (x, y)} (ex : lors d'un resize). */
    public void setPosition(float x, float y) {
        bg.setPosition(x, y);
        fill.setPosition(x, y);
        label.setPosition(x, y);
    }

    /** Met à jour la largeur du remplissage et le texte "PV/PV max" selon les PV actuels. */
    public void refresh(int hp, int maxHp) {
        float ratio = (float) hp / maxHp;
        fill.setWidth(width * ratio);
        label.setText(hp + "/" + maxHp);
    }

    @Override
    public void dispose() {
        bgTexture.dispose();
        fillTexture.dispose();
    }
}
