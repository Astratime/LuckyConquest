package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import fr.astratime.lucky.assets.Palette;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Bingo de la Flamme : un brasero de fer se pose sur la table, la Flamme y
 * prend, vacille et grandit de plus en plus vite, puis à {@link #ERUPT_TIME}
 * une colonne de feu jaillit jusqu'en haut de l'écran en se tordant (les
 * braises, les ondes et les Flammes projetées sont lancées par
 * {@link JackpotCelebration}, qui suit {@link #mouth}).
 */
public class FirePillarScene extends BingoScene {

    /** La Flamme prend dans le brasero. */
    public static final float CATCH_TIME = 0.3f;
    /** La colonne de feu jaillit. */
    public static final float ERUPT_TIME = 1.1f;

    private static final int BRAZIER_W = 34, BRAZIER_H = 20;
    private static final float FLAME_SIZE = 200f;
    private static final float PILLAR_W = 300f;

    private final Image brazier, flame, pillar, core, glow;
    private final Vector2 base = new Vector2(), mouth = new Vector2();

    /** @param flameIcon image du symbole Flamme */
    public FirePillarScene(TextureRegion flameIcon) {
        glow = image(texture(halo(56, hex("ff8a20"))));
        glow.setOrigin(glow.getWidth() / 2f, glow.getHeight() / 2f);
        addActor(glow);
        pillar = image(new TextureRegion(texture(pillar(hex("ff5a10"), hex("ffb020")))), 1f);
        core   = image(new TextureRegion(texture(pillar(hex("ffd040"), hex("fff6c0")))), 1f);
        addActor(pillar);
        addActor(core);
        flame = image(flameIcon, FLAME_SIZE / flameIcon.getRegionWidth());
        flame.setOrigin(flame.getWidth() / 2f, 0f);
        addActor(flame);
        brazier = image(texture(brazier()));
        brazier.setOrigin(brazier.getWidth() / 2f, 0f);
        addActor(brazier);
    }

    /** @return le haut (Stage) du brasero, d'où sort le feu. */
    public Vector2 mouth() { return mouth; }

    @Override
    protected void start(float x, float y) {
        base.set(x, y);
        float height = getStage() != null ? getStage().getHeight() : 1080f;
        pillar.setSize(PILLAR_W, height);
        core.setSize(PILLAR_W * 0.45f, height);
        pillar.setOrigin(PILLAR_W / 2f, 0f);
        core.setOrigin(core.getWidth() / 2f, 0f);
        update(0f);
    }

    @Override
    protected void update(float delta) {
        float land = Interpolation.bounceOut.apply(Math.min(1f, time / CATCH_TIME));
        placeOrigin(brazier, base.x, base.y + 500f * (1f - land));
        mouth.set(base.x, base.y + BRAZIER_H * SCALE - 10f);

        // La Flamme vacille et grandit jusqu'à l'éruption.
        float grow = time < CATCH_TIME ? 0f : Math.min(1f, (time - CATCH_TIME) / (ERUPT_TIME - CATCH_TIME));
        float flicker = 1f + 0.08f * MathUtils.sin(time * 37f) + 0.05f * MathUtils.sin(time * 23f);
        flame.setVisible(time >= CATCH_TIME);
        float size = time < ERUPT_TIME ? 0.3f + 0.9f * Interpolation.pow2In.apply(grow) : 1.3f;
        flame.setScale(size * (2f - flicker), size * flicker);
        flame.setRotation(MathUtils.sin(time * 11f) * 5f);
        placeOrigin(flame, mouth.x, mouth.y - 20f);

        glow.setScale((2f + 4f * grow) * flicker);
        glow.getColor().a = time >= CATCH_TIME ? 0.75f : 0f;
        placeOrigin(glow, mouth.x, mouth.y + 100f);

        boolean erupting = time >= ERUPT_TIME;
        pillar.setVisible(erupting);
        core.setVisible(erupting);
        if (!erupting) return;
        float since = time - ERUPT_TIME;
        float shoot = Interpolation.pow3Out.apply(Math.min(1f, since / 0.25f));
        float twist = 1f + 0.18f * MathUtils.sin(since * 30f);
        float fade = Math.max(0f, 1f - Math.max(0f, since - 0.8f) / 0.5f);
        pillar.setScale(twist * fade + 0.05f, shoot);
        core.setScale((2f - twist) * fade + 0.05f, shoot);
        pillar.getColor().a = 0.85f * fade;
        core.getColor().a = 0.95f * fade;
        placeOrigin(pillar, mouth.x + MathUtils.sin(since * 13f) * 12f, mouth.y);
        placeOrigin(core, mouth.x - MathUtils.sin(since * 13f) * 8f, mouth.y);
    }

    /** @return le brasero : coupe de fer sur trois pieds, braises rougeoyantes au bord. */
    private static Pixmap brazier() {
        Pixmap pixmap = pixmap(BRAZIER_W, BRAZIER_H);
        Color iron = Palette.IRON, dark = Palette.IRON_DARK, ember = hex("ff6a20"), hot = hex("ffd040");
        Shape bowl = (x, y) -> {
            if (y < 3 || y > 11) return false;
            int inset = (y - 3) / 2;
            return x >= 1 + inset && x <= BRAZIER_W - 2 - inset;
        };
        fillShape(pixmap, bowl, (x, y) -> y <= 4 ? (x % 3 == 0 ? hot : ember) : y % 3 == 0 ? dark : iron);
        Shape legs = (x, y) -> y > 11 && y < BRAZIER_H && (Math.abs(x - 8 + (y - 12) / 2) <= 1
            || Math.abs(x - 16.5f) <= 1 || Math.abs(x - 25 - (y - 12) / 2) <= 1);
        fillShape(pixmap, legs, (x, y) -> dark);
        return pixmap;
    }

    /** @return une colonne de feu : bords {@code edge}, cœur {@code core}, qui s'effile vers le haut. */
    private static Pixmap pillar(Color edge, Color core) {
        int w = 16, h = 32;
        Pixmap pixmap = pixmap(w, h);
        for (int y = 0; y < h; y++) {
            float t = (float) y / h;                 // 0 en haut
            float half = w / 2f * (0.45f + 0.55f * t);
            for (int x = 0; x < w; x++) {
                float d = Math.abs(x + 0.5f - w / 2f);
                if (d > half) continue;
                Color c = d < half * 0.5f ? core : edge;
                pixmap.setColor(c.r, c.g, c.b, 0.35f + 0.65f * t);
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }
}
