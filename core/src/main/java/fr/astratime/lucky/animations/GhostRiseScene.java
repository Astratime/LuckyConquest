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
 * Bingo du Spectre (rouleau du Manoir hanté) : une pierre tombale sort de la
 * table et se fend ; à {@link #RISE_TIME}, le Spectre s'en échappe, tourne
 * deux fois autour d'elle en ondulant, puis file vers le haut de l'écran (la
 * brume, les ondes, les jets de symboles et les pièces sont lancés par
 * {@link JackpotCelebration}, qui suit {@link #ghost}).
 */
public class GhostRiseScene extends BingoScene {

    /** La pierre tombale est sortie de la table. */
    public static final float STONE_TIME = 0.5f;
    /** Elle se fend : le Spectre s'échappe. */
    public static final float RISE_TIME = 1.0f;

    private static final int STONE_W = 22, STONE_H = 28;

    private final Image stone, crack, glow, spectre;
    private final Vector2 base = new Vector2(), ghost = new Vector2();

    /** @param spectreIcon image du symbole Spectre */
    public GhostRiseScene(TextureRegion spectreIcon) {
        glow = image(texture(halo(56, hex("c8f0e8"))));
        glow.setOrigin(glow.getWidth() / 2f, glow.getHeight() / 2f);
        addActor(glow);
        stone = image(texture(stone(false)));
        stone.setOrigin(stone.getWidth() / 2f, 0f);
        addActor(stone);
        crack = image(texture(stone(true)));
        crack.setOrigin(crack.getWidth() / 2f, 0f);
        addActor(crack);
        spectre = image(spectreIcon, 200f / spectreIcon.getRegionWidth());
        spectre.setOrigin(spectre.getWidth() / 2f, spectre.getHeight() / 2f);
        addActor(spectre);
    }

    /** @return où se trouve le Spectre (Stage), d'où partent la brume et les symboles. */
    public Vector2 ghost() { return ghost; }

    @Override
    protected void start(float x, float y) {
        base.set(x, y);
        update(0f);
    }

    @Override
    protected void update(float delta) {
        // La pierre tombale monte de la table, puis tremble avant de se fendre.
        float out = Interpolation.pow3Out.apply(Math.min(1f, time / STONE_TIME));
        float tremble = time > STONE_TIME && time < RISE_TIME ? MathUtils.sin(time * 70f) * 4f : 0f;
        placeOrigin(stone, base.x + tremble, base.y - STONE_H * SCALE * (1f - out));
        placeOrigin(crack, base.x + tremble, base.y - STONE_H * SCALE * (1f - out));
        boolean risen = time >= RISE_TIME;
        stone.setVisible(!risen);
        crack.setVisible(risen);
        spectre.setVisible(risen);
        glow.getColor().a = risen ? 0.55f : 0.15f * out;
        float top = base.y + STONE_H * SCALE;
        if (!risen) {
            ghost.set(base.x, top);
            glow.setScale(1.5f);
            placeOrigin(glow, base.x, top - 40f);
            return;
        }
        // Il tourne deux fois autour de la tombe en montant, puis file tout droit vers le ciel.
        float since = time - RISE_TIME;
        float circle = Math.min(1f, since / 0.9f);
        float angle = circle * MathUtils.PI2 * 2f;
        float radius = 220f * (1f - 0.4f * circle);
        float lift = since > 0.9f ? Interpolation.pow2In.apply(Math.min(1f, (since - 0.9f) / 0.5f)) * 900f : 0f;
        ghost.set(base.x + MathUtils.sin(angle) * radius, top + 80f + 160f * circle + lift);
        spectre.setScale(0.5f + 0.5f * Math.min(1f, since / 0.3f));
        spectre.setRotation(MathUtils.sin(since * 10f) * 12f);
        spectre.getColor().a = 0.85f + 0.15f * MathUtils.sin(since * 20f);
        placeOrigin(spectre, ghost.x, ghost.y);
        glow.setScale(3f + MathUtils.sin(since * 8f));
        placeOrigin(glow, ghost.x, ghost.y);
    }

    /** @return la pierre tombale grise à l'arrondi, une croix gravée ; {@code cracked} : fendue en deux. */
    private static Pixmap stone(boolean cracked) {
        Pixmap pixmap = pixmap(STONE_W, STONE_H);
        Color stone = hex("8a8aa0"), shade = hex("5a5a6e"), moss = hex("4a7a4a"), dark = hex("1a1420");
        fillShape(pixmap, (x, y) -> {
            if (y >= 8) return x >= 2 && x <= STONE_W - 3;
            float dx = x + 0.5f - STONE_W / 2f, dy = 8 - y;
            return dx * dx / 81f + dy * dy / 64f <= 1f;
        }, (x, y) -> x >= STONE_W - 7 ? shade : y >= STONE_H - 3 && x % 3 == 0 ? moss : stone);
        fillShape(pixmap, (x, y) -> (x >= 10 && x <= 11 && y >= 6 && y <= 16) || (y >= 9 && y <= 10 && x >= 7 && x <= 14),
            (x, y) -> shade);
        if (cracked) {
            fillShape(pixmap, (x, y) -> y >= 1 && y < STONE_H - 4 && Math.abs(x - (11 + (int) (3 * MathUtils.sin(y * 0.9f)))) <= 0,
                (x, y) -> dark);
        }
        return pixmap;
    }
}
