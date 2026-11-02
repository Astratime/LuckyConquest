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
 * Bingo de la Lumière du bingo : une boule de bingo dorée géante descend du
 * ciel en tournant, le symbole brillant dans sa fenêtre ; elle s'arrête au
 * milieu de la table, se fissure de rayons de lumière ({@link #CRACK_TIME}),
 * puis éclate en deux à {@link #OPEN_TIME} : une couronne de rayons blancs et
 * or tourne autour du symbole libéré, qui grandit (les étincelles, les ondes
 * et la pluie de symboles sont lancées par {@link JackpotCelebration}, qui
 * suit {@link #center}).
 */
public class LightOrbScene extends BingoScene {

    /** La boule se fissure. */
    public static final float CRACK_TIME = 0.95f;
    /** La boule éclate. */
    public static final float OPEN_TIME  = 1.3f;

    private static final int BALL = 36;
    private static final float BALL_SCALE = 9f;
    private static final float ICON_SIZE = 150f, FREED_SIZE = 300f;
    private static final int RAYS = 14;
    private static final float RAY_LENGTH = 900f;

    private final Image[] rays = new Image[RAYS];
    private final Image glow, top, bottom, cracks, icon;
    private final Vector2 center = new Vector2(), at = new Vector2();

    /** @param light image du symbole Lumière du bingo */
    public LightOrbScene(TextureRegion light) {
        TextureRegion pixel = new TextureRegion(texture(whitePixel()));
        for (int i = 0; i < RAYS; i++) {
            rays[i] = image(pixel, 1f);
            rays[i].setSize(RAY_LENGTH, i % 2 == 0 ? 40f : 18f);
            rays[i].setOrigin(0f, rays[i].getHeight() / 2f);
            addActor(rays[i]);
        }
        glow = image(texture(halo(56, hex("fff2b0"))));
        glow.setOrigin(glow.getWidth() / 2f, glow.getHeight() / 2f);
        addActor(glow);
        top = image(new TextureRegion(texture(half(true))), BALL_SCALE);
        top.setOrigin(top.getWidth() / 2f, 0f);
        bottom = image(new TextureRegion(texture(half(false))), BALL_SCALE);
        bottom.setOrigin(bottom.getWidth() / 2f, bottom.getHeight());
        addActor(top);
        addActor(bottom);
        icon = image(light, FREED_SIZE / light.getRegionWidth());
        icon.setOrigin(icon.getWidth() / 2f, icon.getHeight() / 2f);
        addActor(icon);
        cracks = image(new TextureRegion(texture(cracks())), BALL_SCALE);
        cracks.setOrigin(cracks.getWidth() / 2f, cracks.getHeight() / 2f);
        addActor(cracks);
    }

    /** @return la position (Stage) de la boule, puis du symbole libéré. */
    public Vector2 center() { return at; }

    @Override
    protected void start(float x, float y) {
        center.set(x, y);
        for (Image ray : rays) ray.setVisible(false);
        top.setColor(Color.WHITE);
        bottom.setColor(Color.WHITE);
        update(0f);
    }

    @Override
    protected void update(float delta) {
        float sky = getStage() != null ? getStage().getHeight() : 1080f;
        // Descente en se balançant, puis tremblement avant d'éclater.
        float land = Interpolation.pow2Out.apply(Math.min(1f, time / CRACK_TIME));
        float sway = MathUtils.sin(time * 5f) * 60f * (1f - land);
        float tremble = time >= CRACK_TIME && time < OPEN_TIME ? 7f * (time - CRACK_TIME) / (OPEN_TIME - CRACK_TIME) : 0f;
        at.set(center.x + sway + MathUtils.random(-tremble, tremble),
            MathUtils.lerp(sky + BALL * BALL_SCALE, center.y, land) + MathUtils.random(-tremble, tremble));

        float split = time < OPEN_TIME ? 0f : Interpolation.pow2Out.apply(Math.min(1f, (time - OPEN_TIME) / 0.5f));
        float spin = time < OPEN_TIME ? MathUtils.sin(time * 4f) * 12f : 0f;
        top.setRotation(spin + split * 25f);
        bottom.setRotation(spin - split * 25f);
        placeOrigin(top, at.x - split * 120f, at.y + split * 420f);
        placeOrigin(bottom, at.x + split * 120f, at.y - split * 420f);
        float fade = Math.max(0f, 1f - split * 1.4f);
        top.getColor().a = fade;
        bottom.getColor().a = fade;

        cracks.setVisible(time >= CRACK_TIME && time < OPEN_TIME);
        cracks.setRotation(spin);
        cracks.getColor().a = 0.5f + 0.5f * MathUtils.sin(time * 40f);
        placeOrigin(cracks, at.x, at.y);

        // Dans sa fenêtre, puis libéré et géant.
        float freed = time < OPEN_TIME ? 0f : Interpolation.swingOut.apply(Math.min(1f, (time - OPEN_TIME) / 0.35f));
        icon.setScale(ICON_SIZE / FREED_SIZE + (1f - ICON_SIZE / FREED_SIZE) * freed);
        icon.setRotation(time < OPEN_TIME ? spin : MathUtils.sin(time * 6f) * 4f);
        placeOrigin(icon, at.x, at.y);

        glow.setScale(time < OPEN_TIME ? 3f + 1.5f * land : 6f + 0.5f * MathUtils.sin(time * 9f));
        glow.getColor().a = time < OPEN_TIME ? 0.4f : 0.85f;
        placeOrigin(glow, at.x, at.y);

        if (time < OPEN_TIME) return;
        float since = time - OPEN_TIME;
        for (int i = 0; i < RAYS; i++) {
            Image ray = rays[i];
            ray.setVisible(true);
            ray.setScaleX(Math.min(1f, since / 0.25f));
            ray.setRotation(i * 360f / RAYS + since * 30f);
            ray.setPosition(at.x, at.y - ray.getHeight() / 2f);
            float pulse = 0.25f + 0.1f * MathUtils.sin(since * 8f + i);
            if (i % 2 == 0) ray.setColor(1f, 0.88f, 0.4f, pulse);
            else            ray.setColor(1f, 1f, 1f, pulse);
        }
    }

    /** @return une moitié de la boule : or brillant, bande blanche, fenêtre ronde au centre (vide). */
    private static Pixmap half(boolean upper) {
        int h = BALL / 2;
        Pixmap pixmap = pixmap(BALL, h);
        Color gold = hex("f2c440"), light = hex("ffe890"), shade = hex("c08a18"), band = hex("fff8e8");
        int r = BALL / 2;
        Shape shape = (x, y) -> {
            int by = upper ? y : y + h;                      // ordonnée dans la boule entière
            float dx = x + 0.5f - r, dy = by + 0.5f - r;
            boolean ball = dx * dx + dy * dy <= (r - 1) * (r - 1);
            boolean window = dx * dx + dy * dy <= 9.5f * 9.5f;
            return ball && !window;
        };
        fillShape(pixmap, shape, (x, y) -> {
            int by = upper ? y : y + h;
            if (Math.abs(by + 0.5f - r) < 12f && Math.abs(by + 0.5f - r) > 9f) return band;
            if (x + by < BALL * 0.7f) return light;
            if (x + by > BALL * 1.35f) return shade;
            return gold;
        });
        return pixmap;
    }

    /** @return des fissures de lumière en étoile, sur toute la boule. */
    private static Pixmap cracks() {
        Pixmap pixmap = pixmap(BALL, BALL);
        pixmap.setColor(1f, 1f, 0.85f, 1f);
        int c = BALL / 2;
        int[][] ends = {{2, 10}, {33, 6}, {30, 33}, {4, 29}, {18, 1}, {35, 20}};
        for (int[] end : ends) {
            int mx = (c + end[0]) / 2 + (end[1] % 3) - 1, my = (c + end[1]) / 2 + (end[0] % 3) - 1;
            pixmap.drawLine(c, c, mx, my);
            pixmap.drawLine(mx, my, end[0], end[1]);
        }
        return pixmap;
    }
}
