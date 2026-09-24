package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Textures;

/**
 * Infobulle noire réutilisable, affichant un texte au-dessus d'un acteur
 * survolé (carte ou symbole). Une seule instance partagée par tous les
 * acteurs survolables de l'écran.
 */
public class Tooltip implements Disposable {

    private final Texture backgroundTexture;
    private final Label   label;
    private final Table   table;

    public Tooltip(BitmapFont font) {
        backgroundTexture = Textures.solidColor(Color.BLACK);
        label = new Label("", new Label.LabelStyle(font, Color.WHITE));
        table = new Table();
        table.setBackground(new TextureRegionDrawable(new TextureRegion(backgroundTexture)));
        table.add(label).pad(8f);
        table.setVisible(false);
    }

    /** @return l'acteur de l'infobulle, à ajouter au Stage en dernier pour rester toujours au-dessus. */
    public Table getActor() { return table; }

    /** Affiche {@code text}, positionné juste au-dessus de {@code (x, y)} (coordonnées du Stage). */
    public void show(String text, float x, float y) {
        label.setText(text);
        table.pack();
        table.setPosition(x, y);
        table.setVisible(true);
    }

    /** Cache l'infobulle. */
    public void hide() {
        table.setVisible(false);
    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
    }
}
