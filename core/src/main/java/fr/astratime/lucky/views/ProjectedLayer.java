package fr.astratime.lucky.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Disposable;

import java.util.function.Consumer;

/**
 * Calque de tout ce qui est posé sur la table du combat. En vue classique, un
 * simple groupe. En vue de face ({@link TableProjection}), il se dessine
 * d'abord dans une image hors écran, à plat comme toujours, puis cette image
 * est posée en perspective sur le trapèze de la table, découpée en une grille
 * de petits quadrilatères (assez fine pour que la déformation soit invisible).
 *
 * <p>Les clics et survols suivent : un point de l'écran est ramené sur la
 * table ({@link TableProjection#unproject}) avant de chercher la carte ou le
 * bouton touché. Les coordonnées « du Stage » des éléments du calque restent
 * celles de la table à plat.
 */
public class ProjectedLayer extends Group implements Disposable {

    /** Cases de la grille, dans chaque sens. */
    private static final int   GRID   = 24;
    /** Marge dessinée autour de la table (cartes qui volent, textes qui dépassent du rebord). */
    private static final float MARGIN = 40f;

    private final TableProjection projection;
    /** Dessine la table en blanc opaque : rend l'image opaque partout où est la table. */
    private final Consumer<Batch> opaqueMask;
    private FrameBuffer           buffer;
    private final float[]         vertices = new float[20];
    private final Vector2         point    = new Vector2();

    public ProjectedLayer(TableProjection projection, Consumer<Batch> opaqueMask) {
        this.projection = projection;
        this.opaqueMask = opaqueMask;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!projection.isEnabled() || getStage() == null) {
            super.draw(batch, parentAlpha);
            return;
        }
        int width = Gdx.graphics.getBackBufferWidth(), height = Gdx.graphics.getBackBufferHeight();
        if (width <= 0 || height <= 0) return;
        batch.end();
        Texture image = drawOffscreen(batch, parentAlpha, width, height);
        getStage().getViewport().apply();
        batch.begin();
        drawWarped(batch, image, parentAlpha);
    }

    /** Dessine le calque à plat dans l'image hors écran. @return l'image */
    private Texture drawOffscreen(Batch batch, float parentAlpha, int width, int height) {
        if (buffer == null || buffer.getWidth() != width || buffer.getHeight() != height) {
            if (buffer != null) buffer.dispose();
            buffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
            buffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        buffer.begin();
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        super.draw(batch, parentAlpha);
        // Les dessins translucides posés sur le feutre ont entamé la transparence de l'image :
        // la table est rendue opaque (canal alpha seul).
        batch.flush();
        Gdx.gl.glColorMask(false, false, false, true);
        int src = batch.getBlendSrcFunc(), dst = batch.getBlendDstFunc();
        batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);
        opaqueMask.accept(batch);
        batch.flush();
        Gdx.gl.glColorMask(true, true, true, true);
        batch.setBlendFunction(src, dst);
        batch.end();
        buffer.end();
        return buffer.getColorBufferTexture();
    }

    /** Pose l'image en perspective sur le trapèze de la table. */
    private void drawWarped(Batch batch, Texture image, float parentAlpha) {
        Camera camera = getStage().getCamera();
        float viewWidth  = getStage().getViewport().getWorldWidth();
        float viewHeight = getStage().getViewport().getWorldHeight();
        float viewLeft   = camera.position.x - viewWidth / 2f;
        float viewBottom = camera.position.y - viewHeight / 2f;
        Rectangle table = projection.getTable();
        float x0 = Math.max(viewLeft, table.x - MARGIN);
        float y0 = Math.max(viewBottom, table.y - MARGIN);
        float x1 = Math.min(viewLeft + viewWidth, table.x + table.width + MARGIN);
        float y1 = Math.min(viewBottom + viewHeight, table.y + table.height + MARGIN);
        float color = new Color(1f, 1f, 1f, parentAlpha * getColor().a).toFloatBits();

        int src = batch.getBlendSrcFunc(), dst = batch.getBlendDstFunc();
        batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA); // l'image a ses couleurs déjà fondues
        for (int i = 0; i < GRID; i++) {
            float ax = x0 + (x1 - x0) * i / GRID, bx = x0 + (x1 - x0) * (i + 1) / GRID;
            for (int j = 0; j < GRID; j++) {
                float ay = y0 + (y1 - y0) * j / GRID, by = y0 + (y1 - y0) * (j + 1) / GRID;
                corner(0, ax, ay, color, viewLeft, viewBottom, viewWidth, viewHeight);
                corner(5, ax, by, color, viewLeft, viewBottom, viewWidth, viewHeight);
                corner(10, bx, by, color, viewLeft, viewBottom, viewWidth, viewHeight);
                corner(15, bx, ay, color, viewLeft, viewBottom, viewWidth, viewHeight);
                batch.draw(image, vertices, 0, vertices.length);
            }
        }
        batch.setBlendFunction(src, dst);
    }

    /** Sommet {@code at} d'un quadrilatère : le point {@code (x, y)} de la table, à sa place à l'écran. */
    private void corner(int at, float x, float y, float color, float left, float bottom, float width, float height) {
        projection.project(point.set(x, y));
        vertices[at]     = point.x;
        vertices[at + 1] = point.y;
        vertices[at + 2] = color;
        vertices[at + 3] = (x - left) / width;   // l'image hors écran a son origine en bas à gauche
        vertices[at + 4] = (y - bottom) / height;
    }

    /** Un point de l'écran (clic, survol) ramené sur la table, en vue de face. */
    @Override
    public Vector2 parentToLocalCoordinates(Vector2 parentCoords) {
        if (projection.isEnabled()) projection.unproject(parentCoords);
        return super.parentToLocalCoordinates(parentCoords);
    }

    @Override
    public void dispose() {
        if (buffer != null) buffer.dispose();
        buffer = null;
    }
}
