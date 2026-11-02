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
 * Bingo des Crocs : une mâchoire géante surgit au milieu de la table, s'ouvre
 * grand et claque trois fois (à {@link #BITES}), de plus en plus fort ; au
 * dernier claquement elle se rouvre et les Crocs en sortent en grandissant (les
 * gouttes rouges, les ondes et les cœurs volés sont lancés par {@link JackpotCelebration}).
 */
public class BiteScene extends BingoScene {

    /** Instants où la mâchoire claque. */
    public static final float[] BITES = {0.45f, 0.85f, 1.25f};
    /** Les Crocs sortent de la mâchoire. */
    public static final float REVEAL_TIME = 1.45f;

    private static final int JAW_W = 60, JAW_H = 22;
    private static final float JAW_SCALE = 9f;
    private static final float OPEN = 240f;      // demi-ouverture maximale
    private static final float ICON_SIZE = 260f;

    private final Image upper, lower, icon, glow;
    private final Vector2 center = new Vector2();

    /** @param fangs image du symbole Crocs */
    public BiteScene(TextureRegion fangs) {
        glow  = image(texture(halo(56, hex("ff3048"))));
        glow.setOrigin(glow.getWidth() / 2f, glow.getHeight() / 2f);
        upper = image(new TextureRegion(texture(jaw(true))), JAW_SCALE);
        lower = image(new TextureRegion(texture(jaw(false))), JAW_SCALE);
        upper.setOrigin(upper.getWidth() / 2f, 0f);
        lower.setOrigin(lower.getWidth() / 2f, lower.getHeight());
        icon = image(fangs, ICON_SIZE / fangs.getRegionWidth());
        icon.setOrigin(icon.getWidth() / 2f, icon.getHeight() / 2f);
        addActor(glow);
        addActor(icon);
        addActor(lower);
        addActor(upper);
    }

    /** @return le centre (Stage) de la mâchoire, là où elle claque. */
    public Vector2 center() { return center; }

    @Override
    protected void start(float x, float y) {
        center.set(x, y);
        icon.setVisible(false);
        upper.setVisible(true);
        lower.setVisible(true);
        upper.setColor(Color.WHITE);
        lower.setColor(Color.WHITE);
        placeOrigin(glow, x, y);
        glow.getColor().a = 0f;
        update(0f);
    }

    @Override
    protected void update(float delta) {
        float open;
        float appear = Interpolation.swingOut.apply(Math.min(1f, time / 0.25f));
        if (time < BITES[0]) {
            open = OPEN * Interpolation.pow2Out.apply(Math.min(1f, time / 0.3f));
        } else if (time < REVEAL_TIME) {
            // Entre deux claquements, elle se rouvre un peu moins, puis claque d'un coup.
            int bite = 0;
            while (bite + 1 < BITES.length && time >= BITES[bite + 1]) bite++;
            float next = bite + 1 < BITES.length ? BITES[bite + 1] : REVEAL_TIME;
            float since = time - BITES[bite], span = next - BITES[bite];
            float progress = since / span;
            float reopen = OPEN * (0.8f - 0.15f * bite);
            open = progress < 0.25f ? 0f
                : progress < 0.8f ? reopen * Interpolation.pow2Out.apply((progress - 0.25f) / 0.55f)
                : reopen * (1f - Interpolation.pow3In.apply((progress - 0.8f) / 0.2f));
        } else {
            open = OPEN * 1.3f * Interpolation.pow2Out.apply(Math.min(1f, (time - REVEAL_TIME) / 0.25f));
        }
        float shake = time >= BITES[0] && time < REVEAL_TIME && open < 4f ? 8f : 0f;
        float jx = center.x + MathUtils.random(-shake, shake);
        upper.setScale(appear);
        lower.setScale(appear);
        placeOrigin(upper, jx, center.y + open);
        placeOrigin(lower, jx, center.y - open);

        glow.getColor().a = time < REVEAL_TIME ? 0.3f + 0.3f * (1f - open / OPEN) : 0.8f;
        glow.setScale(3f + 0.4f * MathUtils.sin(time * 14f));
        if (time >= REVEAL_TIME) {
            float since = time - REVEAL_TIME;
            icon.setVisible(true);
            icon.setScale(Interpolation.swingOut.apply(Math.min(1f, since / 0.35f)));
            icon.setRotation(MathUtils.sin(since * 9f) * 5f);
            placeOrigin(icon, center.x, center.y);
            float fade = Math.max(0f, 1f - since / 0.4f);
            upper.getColor().a = fade;
            lower.getColor().a = fade;
        }
    }

    /** @return une moitié de mâchoire : gencive rouge sombre, rangée de crocs blancs vers le centre. */
    private static Pixmap jaw(boolean top) {
        Pixmap pixmap = pixmap(JAW_W, JAW_H);
        Color gum = hex("8a1626"), gumLight = hex("c23040"), tooth = hex("f4eee0"), toothShade = hex("c8bfa8");
        int gumH = 9;
        // La gencive, en arc : plus épaisse au milieu.
        Shape gumShape = (x, y) -> {
            int yy = top ? y : JAW_H - 1 - y;
            float bulge = 2f * (1f - Math.abs(x + 0.5f - JAW_W / 2f) / (JAW_W / 2f));
            return yy < gumH + bulge && x >= 1 && x < JAW_W - 1;
        };
        fillShape(pixmap, gumShape, (x, y) -> (top ? y : JAW_H - 1 - y) < 3 ? gum : gumLight);
        // Les crocs : deux grands sur les côtés, des petits entre eux.
        int[] xs = {4, 10, 16, 22, 28, 34, 40, 46, 52};
        for (int i = 0; i < xs.length; i++) {
            boolean big = i == 1 || i == xs.length - 2;
            int w = big ? 6 : 5, h = big ? 13 : 8, x0 = xs[i];
            Shape fang = (x, y) -> {
                int yy = (top ? y : JAW_H - 1 - y) - gumH + 1;
                if (yy < 0 || yy >= h) return false;
                float half = w / 2f * (1f - (float) yy / h);
                return Math.abs(x + 0.5f - (x0 + w / 2f)) <= half + 0.3f;
            };
            fillShape(pixmap, fang, (x, y) -> x < x0 + w / 2 ? tooth : toothShade);
        }
        return pixmap;
    }
}
