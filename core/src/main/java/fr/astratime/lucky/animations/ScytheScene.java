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
 * Bingo de la Faux : un voile violet tombe sur la table, une faux géante se
 * lève, se balance en arrière puis fauche l'écran d'un grand arc de droite à
 * gauche ({@link #SWING_START} à {@link #SWING_END}) ; une entaille en
 * croissant reste suspendue puis s'efface (la traînée, les âmes qui s'envolent
 * et les Faux projetées sont lancées par {@link JackpotCelebration}, qui suit
 * {@link #bladeTip}).
 */
public class ScytheScene extends BingoScene {

    /** La faux commence son coup. */
    public static final float SWING_START = 0.75f;
    /** Le coup est donné : l'entaille apparaît. */
    public static final float SWING_END   = 1.05f;

    private static final int   SCYTHE_W = 40, SCYTHE_H = 64;
    private static final float SCYTHE_SCALE = 9f;
    private static final float RISE = -55f, WIND = -78f, DONE = 95f;   // angles (degrés, 0 = manche vertical)
    private static final int   SLASH_W = 80, SLASH_H = 40;

    private final Image veil, scythe, slash;
    private final Vector2 pivot = new Vector2(), tip = new Vector2();
    private final Vector2 tipLocal = new Vector2();

    public ScytheScene() {
        veil = image(new TextureRegion(texture(whitePixel())), 1f);
        addActor(veil);
        slash = image(new TextureRegion(texture(slash())), 12f);
        slash.setOrigin(slash.getWidth() / 2f, slash.getHeight() / 2f);
        addActor(slash);
        scythe = image(new TextureRegion(texture(scythe())), SCYTHE_SCALE);
        scythe.setOrigin(20 * SCYTHE_SCALE, 0f);              // le bas du manche
        tipLocal.set((1 - 20) * SCYTHE_SCALE, (SCYTHE_H - 18) * SCYTHE_SCALE); // pointe de la lame
        addActor(scythe);
    }

    /** @return la position (Stage) de la pointe de la lame. */
    public Vector2 bladeTip() { return tip; }

    @Override
    protected void start(float x, float y) {
        pivot.set(x, y);
        float width = getStage() != null ? getStage().getWidth() : 1920f;
        float height = getStage() != null ? getStage().getHeight() : 1080f;
        veil.setSize(width, height);
        veil.setPosition(0f, 0f);
        veil.setColor(hex("1a0830"));
        veil.getColor().a = 0f;
        slash.setVisible(false);
        scythe.setColor(Color.WHITE);
        update(0f);
    }

    @Override
    protected void update(float delta) {
        veil.getColor().a = Math.min(0.5f, time / 0.5f * 0.5f) * (time > 1.9f ? Math.max(0f, 1f - (time - 1.9f) / 0.5f) : 1f);
        float angle, rise = 0f;
        if (time < 0.4f) {
            float progress = Interpolation.pow2Out.apply(time / 0.4f);
            angle = RISE;
            rise = -500f * (1f - progress);
        } else if (time < SWING_START) {
            angle = MathUtils.lerp(RISE, WIND, Interpolation.sine.apply((time - 0.4f) / (SWING_START - 0.4f)));
        } else if (time < SWING_END) {
            angle = MathUtils.lerp(WIND, DONE, Interpolation.pow2In.apply((time - SWING_START) / (SWING_END - SWING_START)));
        } else {
            float since = time - SWING_END;
            angle = DONE + 10f * MathUtils.sin(since * 14f) * Math.max(0f, 1f - since / 0.5f);
        }
        scythe.setRotation(angle);
        placeOrigin(scythe, pivot.x, pivot.y + rise);
        tip.set(tipLocal).rotateDeg(angle).add(pivot.x, pivot.y + rise);

        if (time < SWING_END) return;
        float since = time - SWING_END;
        slash.setVisible(true);
        slash.setRotation(8f);
        slash.setScale(1f + since * 0.15f, Math.max(0f, 1f - since / 1.2f) + 0.05f);
        slash.setColor(1f, 0.85f, 1f, Math.max(0f, 1f - since / 1.3f));
        placeOrigin(slash, pivot.x, pivot.y + 380f);
    }

    /** @return la faux : long manche de bois, lame d'acier courbée vers la gauche. */
    private static Pixmap scythe() {
        Pixmap pixmap = pixmap(SCYTHE_W, SCYTHE_H);
        Color wood = hex("6b4424"), woodLight = hex("94643a"), steel = hex("c8d0dc"), edge = hex("ffffff"),
            steelDark = hex("7a8494"), grip = hex("3a2414");
        // Manche, légèrement tordu.
        Shape handle = (x, y) -> y >= 10 && Math.abs(x - (20 + (y - 10) / 26)) <= 1;
        fillShape(pixmap, handle, (x, y) -> y > 30 && y < 34 ? grip : x <= 20 ? woodLight : wood);
        // Lame : croissant entre deux cercles, vers la gauche, depuis le haut du manche.
        Shape blade = (x, y) -> {
            float dx = x + 0.5f - 22f, dy = y + 0.5f - 22f;
            boolean outer = dx * dx + dy * dy <= 21f * 21f;
            float ex = x + 0.5f - 24f, ey = y + 0.5f - 27f;
            boolean inner = ex * ex + ey * ey <= 20f * 20f;
            return outer && !inner && x <= 22 && y <= 16;
        };
        fillShape(pixmap, blade, (x, y) -> y <= 3 ? edge : y >= 10 ? steelDark : steel);
        // Le collier qui tient la lame.
        pixmap.setColor(steelDark);
        pixmap.fillRectangle(18, 8, 5, 3);
        return pixmap;
    }

    /** @return l'entaille : un croissant blanc, plus épais au milieu. */
    private static Pixmap slash() {
        Pixmap pixmap = pixmap(SLASH_W, SLASH_H);
        for (int x = 0; x < SLASH_W; x++) {
            float u = (x + 0.5f) / SLASH_W * 2f - 1f;
            float mid = SLASH_H * 0.75f - (1f - u * u) * SLASH_H * 0.55f;
            float thick = 1f + (1f - u * u) * 3.5f;
            for (int y = 0; y < SLASH_H; y++) {
                float d = Math.abs(y + 0.5f - mid);
                if (d > thick) continue;
                pixmap.setColor(1f, 1f, 1f, d < thick * 0.5f ? 1f : 0.5f);
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }
}
