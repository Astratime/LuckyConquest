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

    /** Forme dessinée pixel par pixel : {@code true} pour les pixels qui en font partie. */
    @FunctionalInterface
    protected interface Shape {
        boolean contains(int x, int y);
    }

    /** Couleur d'un pixel intérieur d'une forme (ombres, reflets, motifs). */
    @FunctionalInterface
    protected interface Shader {
        Color at(int x, int y);
    }

    /**
     * Remplit {@code shape} (sur toute l'image) de la couleur que donne
     * {@code shader}, cernée d'un liseré {@link #OUTLINE} sur son pourtour.
     */
    protected static void fillShape(Pixmap pixmap, Shape shape, Shader shader) {
        int width = pixmap.getWidth(), height = pixmap.getHeight();
        Shape inside = (x, y) -> x >= 0 && y >= 0 && x < width && y < height && shape.contains(x, y);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!inside.contains(x, y)) continue;
                boolean edge = !inside.contains(x - 1, y) || !inside.contains(x + 1, y)
                    || !inside.contains(x, y - 1) || !inside.contains(x, y + 1);
                pixmap.setColor(edge ? OUTLINE : shader.at(x, y));
                pixmap.drawPixel(x, y);
            }
        }
    }

    /**
     * Dessine un motif écrit ligne par ligne, son coin en haut à gauche en
     * {@code (x, y)} : chaque caractère est la couleur d'un pixel dans
     * {@code colors} (dans l'ordre de {@code keys}), les autres sont transparents.
     */
    protected static void drawGrid(Pixmap pixmap, String[] rows, int x, int y, String keys, Color... colors) {
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < rows[row].length(); col++) {
                int key = keys.indexOf(rows[row].charAt(col));
                if (key < 0) continue;
                pixmap.setColor(colors[key]);
                pixmap.drawPixel(x + col, y + row);
            }
        }
    }

    /** @return un halo rond de {@code color}, de plus en plus dense vers le centre. */
    protected static Pixmap halo(int size, Color color) {
        Pixmap pixmap = pixmap(size, size);
        int rings = 5;
        for (int ring = 0; ring < rings; ring++) {
            pixmap.setColor(color.r, color.g, color.b, 0.2f + ring * 0.14f);
            pixmap.fillCircle(size / 2, size / 2, size / 2 - 1 - ring * size / (rings * 3));
        }
        return pixmap;
    }

    /** @return un pixel blanc, à étirer et teinter (traits de lumière, coups de lame). */
    protected static Pixmap whitePixel() {
        Pixmap pixmap = pixmap(1, 1);
        pixmap.setColor(Color.WHITE);
        pixmap.drawPixel(0, 0);
        return pixmap;
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
