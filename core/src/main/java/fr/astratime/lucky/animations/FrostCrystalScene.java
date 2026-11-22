package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Bingo du Flocon (rouleau du Palace des Neiges) : le givre gagne la table, un
 * immense cristal de glace se forme au centre en tournant lentement, le Flocon
 * brille en son cœur, puis à {@link #SHATTER_TIME} le cristal vole en éclats (les
 * éclats, les ondes et les pièces sont lancés par {@link JackpotCelebration}, qui
 * suit {@link #center}).
 */
public class FrostCrystalScene extends BingoScene {

    /** Le cristal a fini de se former. */
    public static final float FORMED_TIME = 0.8f;
    /** Le cristal éclate. */
    public static final float SHATTER_TIME = 1.15f;

    private static final int CRYSTAL = 41;

    private final Image crystal, glow, flake;
    private final Vector2 center = new Vector2();

    /** @param snowflakeIcon image du symbole Flocon */
    public FrostCrystalScene(TextureRegion snowflakeIcon) {
        glow = image(texture(halo(64, hex("cfefff"))));
        glow.setOrigin(glow.getWidth() / 2f, glow.getHeight() / 2f);
        addActor(glow);
        crystal = image(texture(crystal()));
        crystal.setOrigin(crystal.getWidth() / 2f, crystal.getHeight() / 2f);
        addActor(crystal);
        flake = image(snowflakeIcon, 140f / snowflakeIcon.getRegionWidth());
        flake.setOrigin(flake.getWidth() / 2f, flake.getHeight() / 2f);
        addActor(flake);
    }

    /** @return le centre (Stage) du cristal, d'où partent les éclats. */
    public Vector2 center() { return center; }

    @Override
    protected void start(float x, float y) {
        center.set(x, y);
        update(0f);
    }

    @Override
    protected void update(float delta) {
        float grow = Interpolation.pow3Out.apply(Math.min(1f, time / FORMED_TIME));
        boolean whole = time < SHATTER_TIME;
        crystal.setVisible(whole);
        crystal.setScale(0.15f + 0.85f * grow);
        crystal.setRotation(time * 40f);
        crystal.getColor().a = 0.4f + 0.6f * grow;
        placeOrigin(crystal, center.x, center.y);
        // Le Flocon brille au cœur, puis s'envole quand le cristal éclate.
        float since = Math.max(0f, time - SHATTER_TIME);
        flake.setScale(0.5f + 0.5f * grow + since * 0.8f);
        flake.setRotation(-time * 60f);
        placeOrigin(flake, center.x, center.y + since * 120f);
        float pulse = 0.5f + 0.5f * MathUtils.sin(time * 14f);
        glow.getColor().a = whole ? 0.35f + 0.3f * grow * pulse : Math.max(0f, 0.8f - since);
        glow.setScale(whole ? 2f + 3f * grow : 5f + since * 6f);
        placeOrigin(glow, center.x, center.y);
    }

    /** @return un cristal de glace à six branches, chacune garnie de petites branches. */
    private static Pixmap crystal() {
        Pixmap pixmap = pixmap(CRYSTAL, CRYSTAL);
        Color ice = hex("bfe8ff"), deep = hex("6aa8d8"), white = Color.WHITE;
        float c = CRYSTAL / 2f;
        for (int arm = 0; arm < 6; arm++) {
            float angle = arm * 60f * MathUtils.degreesToRadians;
            float dx = MathUtils.cos(angle), dy = MathUtils.sin(angle);
            for (float r = 0; r <= c - 1; r += 0.5f) {
                plot(pixmap, c + dx * r, c + dy * r, r < 4 ? white : ice);
                plot(pixmap, c + dx * r + dy * 0.6f, c + dy * r - dx * 0.6f, deep);
            }
            for (float at : new float[] {0.45f, 0.72f}) { // les petites branches, en V vers l'extérieur
                float bx = c + dx * (c - 1) * at, by = c + dy * (c - 1) * at, len = (c - 1) * (at < 0.5f ? 0.35f : 0.25f);
                for (int side : new int[] {-1, 1}) {
                    float a2 = angle + side * 50f * MathUtils.degreesToRadians;
                    for (float r = 0; r <= len; r += 0.5f) plot(pixmap, bx + MathUtils.cos(a2) * r, by + MathUtils.sin(a2) * r, ice);
                }
            }
            plot(pixmap, c + dx * (c - 1), c + dy * (c - 1), white);
        }
        return pixmap;
    }

    private static void plot(Pixmap pixmap, float x, float y, Color color) {
        pixmap.setColor(color);
        pixmap.drawPixel(Math.round(x), Math.round(y));
    }
}
