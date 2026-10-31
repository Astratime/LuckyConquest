package fr.astratime.lucky.views;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import fr.astratime.lucky.i18n.Lang;

/**
 * Icône de l'échoppe, en haut à droite de l'écran de combat : l'étal du marché
 * et son nom ; au survol elle grossit, au clic elle ouvre la boutique.
 */
public class ShopIcon extends Group {

    private static final float WIDTH       = 150f;
    private static final float HEIGHT      = 108f;
    private static final float HOVER_SCALE = 1.12f;

    private final Image image;
    private final Label name;

    /**
     * @param stall  image de l'étal
     * @param font   police de son nom
     * @param hover  bruitage au survol
     * @param onOpen clic : ouvre la boutique
     */
    public ShopIcon(Texture stall, BitmapFont font, Sound hover, Runnable onOpen) {
        image = new Image(new TextureRegionDrawable(new TextureRegion(stall)));
        image.setSize(WIDTH, HEIGHT);
        name = new Label(Lang.t("ÉCHOPPE"), new Label.LabelStyle(font, Color.WHITE));
        name.pack();
        name.setPosition((WIDTH - name.getWidth()) / 2f, 0f);
        image.setPosition(0f, name.getHeight());
        addActor(image);
        addActor(name);
        setSize(WIDTH, HEIGHT + name.getHeight());
        setOrigin(WIDTH / 2f, 0f);
        addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                if (pointer != -1) return;
                hover.play();
                clearActions();
                addAction(Actions.scaleTo(HOVER_SCALE, HOVER_SCALE, 0.12f, Interpolation.pow2Out));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                if (pointer != -1) return;
                clearActions();
                addAction(Actions.scaleTo(1f, 1f, 0.12f));
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                onOpen.run();
            }
        });
    }

    /**
     * Vue de face : l'étal est dessiné en volume sur la table ({@link TableProps}) ;
     * l'icône ne garde que son nom et sa zone cliquable.
     */
    public void setFrontView(boolean frontView) {
        image.getColor().a = frontView ? 0f : 1f;
        name.getColor().a  = frontView ? 0f : 1f; // écrit sur le comptoir de l'étal
    }

    /** @return le nom de l'échoppe, tel qu'il s'affiche. */
    public CharSequence getTitle() { return name.getText(); }

    /** @return la police du nom. */
    public BitmapFont getNameFont() { return name.getStyle().font; }

    /** @return la hauteur du nom, sous l'étal. */
    public float getNameHeight() { return name.getHeight(); }
}
