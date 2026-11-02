package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Bingo de la Lune : la nuit tombe sur la table, un fin croissant se lève
 * derrière l'horizon, monte en grossissant et s'arrondit peu à peu jusqu'à la
 * pleine lune à {@link #FULL_TIME}, qui s'illumine d'un halo argenté (les
 * étoiles, les ondes et les Lunes qui tombent sont lancées par
 * {@link JackpotCelebration}, qui suit {@link #center}).
 */
public class MoonriseScene extends BingoScene {

    /** La lune est pleine. */
    public static final float FULL_TIME = 1.35f;

    private static final int MOON = 40;
    private static final float MOON_SCALE = 8f;
    private static final float RISE_FROM = -620f;   // départ, relatif à sa place finale

    private static final int PHASES = 10;

    private final Image night, glow, moon;
    /** Du fin croissant ({@code 0}) à la pleine lune. */
    private final TextureRegionDrawable[] phases = new TextureRegionDrawable[PHASES];
    private final Vector2 center = new Vector2(), at = new Vector2();

    public MoonriseScene() {
        night = image(new TextureRegion(texture(whitePixel())), 1f);
        addActor(night);
        glow = image(texture(halo(56, hex("dfe8ff"))));
        glow.setOrigin(glow.getWidth() / 2f, glow.getHeight() / 2f);
        addActor(glow);
        for (int i = 0; i < PHASES; i++) {
            phases[i] = new TextureRegionDrawable(new TextureRegion(texture(moon((float) i / (PHASES - 1)))));
        }
        moon = image(phases[PHASES - 1].getRegion(), MOON_SCALE);
        moon.setOrigin(moon.getWidth() / 2f, moon.getHeight() / 2f);
        addActor(moon);
    }

    /** @return le centre (Stage) de la lune. */
    public Vector2 center() { return at; }

    @Override
    protected void start(float x, float y) {
        center.set(x, y);
        float width = getStage() != null ? getStage().getWidth() : 1920f;
        float height = getStage() != null ? getStage().getHeight() : 1080f;
        night.setSize(width, height);
        night.setPosition(0f, 0f);
        night.setColor(hex("0a1030"));
        night.getColor().a = 0f;
        moon.setColor(Color.WHITE);
        update(0f);
    }

    @Override
    protected void update(float delta) {
        float dawn = time < FULL_TIME + 0.2f ? 1f : Math.max(0.4f, 1f - (time - FULL_TIME - 0.2f) / 0.5f);
        night.getColor().a = 0.62f * Math.min(1f, time / 0.45f) * dawn; // la nuit s'éclaircit une fois la lune pleine
        float rise = Interpolation.pow2Out.apply(Math.min(1f, time / FULL_TIME));
        at.set(center.x, center.y + RISE_FROM * (1f - rise));
        float scale = 0.55f + 0.45f * rise;
        moon.setScale(scale);
        placeOrigin(moon, at.x, at.y);
        // Elle s'arrondit, du croissant à la pleine lune.
        float reveal = Interpolation.pow2In.apply(Math.min(1f, time / FULL_TIME));
        moon.setDrawable(phases[Math.min(PHASES - 1, (int) (reveal * (PHASES - 1) + 0.001f))]);

        glow.setScale(time < FULL_TIME ? 3f * scale : 4.5f + 0.4f * MathUtils.sin(time * 6f));
        glow.getColor().a = time < FULL_TIME ? 0.25f * reveal : 0.85f;
        placeOrigin(glow, at.x, at.y);
        if (time >= FULL_TIME) {
            float since = time - FULL_TIME;
            float pop = 1f + 0.12f * Interpolation.swingOut.apply(Math.min(1f, since / 0.3f));
            moon.setScale(pop);
            placeOrigin(moon, at.x, at.y);
        }
    }

    /**
     * @param lit 0 : fin croissant, 1 : pleine lune
     * @return la lune : disque argenté, quelques cratères, la partie dans l'ombre transparente
     */
    private static Pixmap moon(float lit) {
        Pixmap pixmap = pixmap(MOON, MOON);
        int r = MOON / 2;
        Color silver = hex("e8ecf6"), shade = hex("b8c0d4"), crater = hex("9ea6bc");
        // L'ombre : un disque qui glisse vers la gauche et découvre la lune.
        float shadowX = r - 6f - (2f * r - 6f) * lit;
        Shape lunar = (x, y) -> {
            float dx = x + 0.5f - r, dy = y + 0.5f - r;
            if (dx * dx + dy * dy > (r - 1) * (r - 1)) return false;
            if (lit >= 1f) return true;
            float sx = x + 0.5f - shadowX;
            return sx * sx + dy * dy > (r - 1) * (r - 1);
        };
        fillShape(pixmap, lunar, (x, y) -> {
            if (dist(x, y, 13, 14) < 4 || dist(x, y, 26, 24) < 5 || dist(x, y, 17, 29) < 2 || dist(x, y, 28, 11) < 2) {
                return crater;
            }
            return x + y > MOON * 1.25f ? shade : silver;
        });
        return pixmap;
    }

    private static float dist(int x, int y, int cx, int cy) {
        return (float) Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy));
    }
}
