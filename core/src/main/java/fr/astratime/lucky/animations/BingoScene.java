package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Palette;

import java.util.ArrayList;
import java.util.List;

/**
 * Décor animé d'une célébration de Bingo (dé, presse, forge, coffre-fort…) :
 * des objets en pixel art — petites images dessinées au lancement du jeu puis
 * agrandies sans lissage — qui jouent une courte scène, puis s'estompent à
 * {@link #FADE_AT}. Les effets autour (étincelles, symboles projetés, mot
 * « BINGO! » géant) sont programmés par {@link JackpotCelebration} aux instants
 * que chaque scène publie.
 *
 * Les positions sont celles du Stage : l'acteur doit être placé à l'origine.
 */
public abstract class BingoScene extends Group implements Disposable {

    /** Instant (depuis {@link #play}) où la scène commence à s'estomper. */
    public static final float FADE_AT   = 2.45f;
    private static final float FADE_TIME = 0.45f;

    /** Agrandissement des images : entier, pour des pixels nets. */
    protected static final float SCALE = 5f;

    protected static final Color OUTLINE = Palette.OUTLINE;

    private final List<Texture> textures = new ArrayList<>();
    /** Temps écoulé depuis {@link #play}. */
    protected float time;

    protected BingoScene() {
        setTouchable(Touchable.disabled);
        setVisible(false);
    }

    /** Lance la scène, centrée en {@code (x, y)} (le sens précis dépend de la scène). */
    public final void play(float x, float y) {
        hide();
        setVisible(true);
        getColor().a = 1f;
        time = 0f;
        start(x, y);
        addAction(Actions.sequence(Actions.delay(FADE_AT), Actions.fadeOut(FADE_TIME), Actions.visible(false)));
    }

    /** Place les objets de la scène et programme leurs mouvements. */
    protected abstract void start(float x, float y);

    /** Mise à jour image par image, pour les mouvements calculés (appelée seulement pendant la scène). */
    protected void update(float delta) {}

    /** Cache la scène immédiatement. */
    public void hide() {
        clearActionsDeep(this);
        setVisible(false);
    }

    private static void clearActionsDeep(Actor actor) {
        actor.clearActions();
        if (actor instanceof Group group) {
            for (Actor child : group.getChildren()) clearActionsDeep(child);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isVisible()) return;
        time += delta;
        update(delta);
    }

    // -------------------------------------------------------------------------
    // Outils de pixel art
    // -------------------------------------------------------------------------

    /** @return une image vide de {@code width} x {@code height} pixels, sans mélange (les pixels sont remplacés). */
    protected static Pixmap pixmap(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        return pixmap;
    }

    /** @return la texture de {@code pixmap} (qui est libérée), disposée avec la scène. */
    protected Texture texture(Pixmap pixmap) {
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        textures.add(texture);
        return texture;
    }

    /** @return une image de {@code texture} agrandie de {@link #SCALE}. */
    protected static Image image(Texture texture) {
        return image(new TextureRegion(texture), SCALE);
    }

    /** @return une image de {@code region} agrandie de {@code scale}. */
    protected static Image image(TextureRegion region, float scale) {
        Image image = new Image(new TextureRegionDrawable(region));
        image.setSize(region.getRegionWidth() * scale, region.getRegionHeight() * scale);
        image.setTouchable(Touchable.disabled);
        return image;
    }

    /** Place {@code image} pour que son origine soit en {@code (x, y)}. */
    protected static void placeOrigin(Image image, float x, float y) {
        image.setPosition(x - image.getOriginX(), y - image.getOriginY());
    }

    /** Remplit un rectangle de {@code fill} cerné d'un liseré {@link #OUTLINE}. */
    protected static void fillOutlined(Pixmap pixmap, int x, int y, int width, int height, Color fill) {
        pixmap.setColor(OUTLINE);
        pixmap.fillRectangle(x, y, width, height);
        pixmap.setColor(fill);
        pixmap.fillRectangle(x + 1, y + 1, width - 2, height - 2);
    }

    /** Remplit un disque de {@code fill} cerné d'un liseré {@link #OUTLINE}. */
    protected static void fillOutlinedCircle(Pixmap pixmap, int x, int y, int radius, Color fill) {
        pixmap.setColor(OUTLINE);
        pixmap.fillCircle(x, y, radius);
        pixmap.setColor(fill);
        pixmap.fillCircle(x, y, radius - 1);
    }

    /** Trace une ligne horizontale de {@code x1} à {@code x2} (inclus). */
    protected static void hLine(Pixmap pixmap, int x1, int x2, int y, Color color) {
        pixmap.setColor(color);
        pixmap.drawLine(x1, y, x2, y);
    }

    /** @return la couleur {@code hex} (« rrggbb »), opaque. */
    protected static Color c(String hex) {
        return Color.valueOf(hex + "ff");
    }

    @Override
    public void dispose() {
        for (Texture texture : textures) texture.dispose();
    }
}
