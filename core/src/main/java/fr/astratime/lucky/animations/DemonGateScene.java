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
 * Bingo du Roi démon : le sol de la table se fend, un portail de flammes
 * s'ouvre en tournoyant, deux yeux rouges s'allument au fond, puis le Roi démon
 * monte du gouffre à {@link #RISE_TIME} et se dresse au-dessus à
 * {@link #RISEN_TIME} (les flammes, les ondes rouges et les cendres sont lancées
 * par {@link JackpotCelebration}, qui suit {@link #portal}).
 */
public class DemonGateScene extends BingoScene {

    /** Les yeux s'allument dans le portail. */
    public static final float EYES_TIME  = 0.55f;
    /** Le Roi démon commence à monter. */
    public static final float RISE_TIME  = 0.9f;
    /** Il est dressé au-dessus du portail. */
    public static final float RISEN_TIME = 1.35f;

    private static final float KING_SIZE = 300f;
    private static final float PORTAL_W = 520f, PORTAL_H = 150f;

    private final Image rim, hole, eyes, king, glow;
    private final Vector2 portal = new Vector2();

    /** @param demonKing image du symbole Roi démon */
    public DemonGateScene(TextureRegion demonKing) {
        glow = image(texture(halo(56, hex("ff2a10"))));
        glow.setOrigin(glow.getWidth() / 2f, glow.getHeight() / 2f);
        addActor(glow);
        rim = image(new TextureRegion(texture(ring(hex("ff7a1a"), hex("ffd040")))), 1f);
        rim.setSize(PORTAL_W, PORTAL_H);
        rim.setOrigin(PORTAL_W / 2f, PORTAL_H / 2f);
        hole = image(new TextureRegion(texture(ring(hex("12020a"), hex("3a0610")))), 1f);
        hole.setSize(PORTAL_W * 0.86f, PORTAL_H * 0.8f);
        hole.setOrigin(hole.getWidth() / 2f, hole.getHeight() / 2f);
        addActor(rim);
        addActor(hole);
        eyes = image(texture(eyes()));
        eyes.setOrigin(eyes.getWidth() / 2f, eyes.getHeight() / 2f);
        addActor(eyes);
        king = image(demonKing, KING_SIZE / demonKing.getRegionWidth());
        king.setOrigin(king.getWidth() / 2f, 0f);
        addActor(king);
    }

    /** @return le centre (Stage) du portail. */
    public Vector2 portal() { return portal; }

    @Override
    protected void start(float x, float y) {
        portal.set(x, y);
        king.setVisible(false);
        eyes.setVisible(false);
        update(0f);
    }

    @Override
    protected void update(float delta) {
        float open = Interpolation.swingOut.apply(Math.min(1f, time / 0.5f));
        rim.setScale(open * (1f + 0.04f * MathUtils.sin(time * 20f)), open);
        hole.setScale(open);
        rim.setRotation(MathUtils.sin(time * 3f) * 3f);
        placeOrigin(rim, portal.x, portal.y);
        placeOrigin(hole, portal.x, portal.y);
        rim.setColor(1f, 0.8f + 0.2f * MathUtils.sin(time * 25f), 0.8f, 1f);
        glow.setScale(open * (5f + 0.5f * MathUtils.sin(time * 9f)), open * 2.2f);
        glow.getColor().a = 0.7f;
        placeOrigin(glow, portal.x, portal.y + 40f);

        // Les yeux clignent dans le noir, puis montent avec le roi.
        eyes.setVisible(time >= EYES_TIME && time < RISE_TIME + 0.1f);
        float blink = (time - EYES_TIME) < 0.15f ? (time - EYES_TIME) / 0.15f : 1f;
        eyes.setScale(1f, blink);
        placeOrigin(eyes, portal.x, portal.y + 6f);

        if (time < RISE_TIME) return;
        king.setVisible(true);
        float rise = Interpolation.pow2Out.apply(Math.min(1f, (time - RISE_TIME) / (RISEN_TIME - RISE_TIME)));
        float since = Math.max(0f, time - RISEN_TIME);
        float breathe = 1f + 0.05f * MathUtils.sin(since * 8f);
        king.setScale(0.6f + 0.4f * rise * breathe);
        king.setColor(1f, 0.55f + 0.45f * rise, 0.55f + 0.45f * rise, Math.min(1f, rise * 3f));
        placeOrigin(king, portal.x + (since > 0f && since < 0.2f ? MathUtils.random(-8f, 8f) : 0f),
            portal.y - KING_SIZE * 0.5f + KING_SIZE * 0.75f * rise);
    }

    /** @return une ellipse pleine (bord {@code edge}, centre {@code core}), étirée à la taille du portail. */
    private static Pixmap ring(Color edge, Color core) {
        int w = 52, h = 16;
        Pixmap pixmap = pixmap(w, h);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float u = (x + 0.5f - w / 2f) / (w / 2f), v = (y + 0.5f - h / 2f) / (h / 2f);
                float d = u * u + v * v;
                if (d > 1f) continue;
                pixmap.setColor(d > 0.55f ? edge : core);
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }

    /** @return deux yeux rouges en amande. */
    private static Pixmap eyes() {
        Pixmap pixmap = pixmap(24, 5);
        Color red = hex("ff2020"), hot = hex("ffd0a0");
        String[] rows = {
            "..rrrr..........rrrr..",
            ".rrhhrrr......rrrhhrr.",
            "rrrhhhrrr....rrrhhhrrr",
            ".rrrrrr........rrrrrr.",
            "...rr............rr...",
        };
        drawGrid(pixmap, rows, 1, 0, "rh", red, hot);
        return pixmap;
    }
}
