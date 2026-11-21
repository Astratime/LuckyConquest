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
 * Bingo de la Vapeur (rouleau du Rapide de Minuit) : une locomotive noire
 * entre par la gauche dans un nuage de fumée, freine au milieu de la table,
 * puis à {@link #WHISTLE_TIME} son sifflet hurle et une colonne de vapeur
 * blanche jaillit de la cheminée (les jets de symboles, les ondes et les
 * pièces sont lancés par {@link JackpotCelebration}, qui suit {@link #chimney}).
 */
public class SteamEngineScene extends BingoScene {

    /** La locomotive s'arrête au milieu de la table. */
    public static final float STOP_TIME = 0.75f;
    /** Le sifflet hurle : la vapeur jaillit. */
    public static final float WHISTLE_TIME = 1.1f;

    private static final int ENGINE_W = 56, ENGINE_H = 30;
    private static final float PLUME_W = 260f;

    private final Image engine, plume, glow, steam;
    private final Vector2 base = new Vector2(), chimney = new Vector2();
    private float enterFrom;

    /** @param steamIcon image du symbole Vapeur */
    public SteamEngineScene(TextureRegion steamIcon) {
        glow = image(texture(halo(56, hex("fff4d0"))));
        glow.setOrigin(glow.getWidth() / 2f, glow.getHeight() / 2f);
        addActor(glow);
        plume = image(new TextureRegion(texture(plume())), 1f);
        addActor(plume);
        steam = image(steamIcon, 180f / steamIcon.getRegionWidth());
        steam.setOrigin(steam.getWidth() / 2f, steam.getHeight() / 2f);
        addActor(steam);
        engine = image(texture(engine()));
        engine.setOrigin(engine.getWidth() / 2f, 0f);
        addActor(engine);
    }

    /** @return le haut (Stage) de la cheminée, d'où sort la vapeur. */
    public Vector2 chimney() { return chimney; }

    @Override
    protected void start(float x, float y) {
        base.set(x, y);
        enterFrom = -engine.getWidth();
        float height = getStage() != null ? getStage().getHeight() : 1080f;
        plume.setSize(PLUME_W, height);
        plume.setOrigin(PLUME_W / 2f, 0f);
        update(0f);
    }

    @Override
    protected void update(float delta) {
        float arrive = Interpolation.pow3Out.apply(Math.min(1f, time / STOP_TIME));
        float x = MathUtils.lerp(enterFrom, base.x, arrive);
        // La locomotive tressaute sur les rails tant qu'elle roule, puis s'affaisse en freinant.
        float bump = time < STOP_TIME ? Math.abs(MathUtils.sin(time * 40f)) * 6f : 0f;
        placeOrigin(engine, x, base.y + bump);
        chimney.set(x + (12 - ENGINE_W / 2f) * SCALE, base.y + bump + ENGINE_H * SCALE - 4f);

        boolean whistling = time >= WHISTLE_TIME;
        plume.setVisible(whistling);
        steam.setVisible(whistling);
        glow.getColor().a = whistling ? 0.6f : 0f;
        if (!whistling) return;
        float since = time - WHISTLE_TIME;
        float shoot = Interpolation.pow3Out.apply(Math.min(1f, since / 0.3f));
        float sway = 1f + 0.15f * MathUtils.sin(since * 26f);
        float fade = Math.max(0f, 1f - Math.max(0f, since - 0.9f) / 0.45f);
        plume.setScale(sway * (0.6f + 0.4f * shoot), shoot);
        plume.getColor().a = 0.9f * fade;
        placeOrigin(plume, chimney.x + MathUtils.sin(since * 9f) * 10f, chimney.y);
        steam.setScale(0.6f + 0.6f * shoot);
        steam.setRotation(MathUtils.sin(since * 12f) * 8f);
        placeOrigin(steam, chimney.x, chimney.y + 240f * shoot);
        glow.setScale(3f + 3f * shoot);
        placeOrigin(glow, chimney.x, chimney.y + 60f);
    }

    /** @return la locomotive : chaudière noire cerclée de laiton, cheminée, cabine, chasse-buffles et roues rouges. */
    private static Pixmap engine() {
        Pixmap pixmap = pixmap(ENGINE_W, ENGINE_H);
        Color black = hex("26262e"), shine = hex("4a4a58"), brass = Palette.GOLD, red = hex("c8302a"),
            redDark = hex("7a1a18"), lamp = hex("fff2a0"), window = hex("ffd27a");
        // Chaudière (cylindre couché), cerclée de laiton.
        fillShape(pixmap, (x, y) -> x >= 6 && x <= 38 && y >= 9 && y <= 20,
            (x, y) -> x % 8 == 3 ? brass : y <= 11 ? shine : black);
        // Cheminée évasée, dôme de laiton.
        fillShape(pixmap, (x, y) -> y >= 1 && y <= 9 && Math.abs(x - 12) <= (y <= 2 ? 3 : 2), (x, y) -> y <= 2 ? shine : black);
        fillShape(pixmap, (x, y) -> y >= 6 && y <= 9 && Math.abs(x - 24) <= 2, (x, y) -> brass);
        // Cabine, sa fenêtre allumée et son toit.
        fillShape(pixmap, (x, y) -> x >= 38 && x <= 52 && y >= 4 && y <= 20, (x, y) -> y <= 5 ? shine : black);
        fillShape(pixmap, (x, y) -> x >= 42 && x <= 48 && y >= 8 && y <= 12, (x, y) -> window);
        // Lanterne avant et chasse-buffles.
        fillShape(pixmap, (x, y) -> x >= 2 && x <= 6 && y >= 11 && y <= 14, (x, y) -> lamp);
        fillShape(pixmap, (x, y) -> y >= 20 && y <= 25 && x >= 1 + (25 - y) && x <= 8, (x, y) -> red);
        // Châssis et roues.
        fillShape(pixmap, (x, y) -> x >= 6 && x <= 52 && y >= 20 && y <= 22, (x, y) -> redDark);
        for (int cx : new int[] {16, 28, 44}) {
            int r = cx == 44 ? 4 : 5;
            fillShape(pixmap, (x, y) -> (x - cx) * (x - cx) + (y - 24) * (y - 24) <= r * r,
                (x, y) -> (x - cx) * (x - cx) + (y - 24) * (y - 24) <= 2 ? brass : red);
        }
        // Bielle de laiton entre les deux grandes roues.
        hLine(pixmap, 16, 28, 24, brass);
        return pixmap;
    }

    /** @return une colonne de vapeur : blanche au cœur, grise aux bords, de plus en plus large vers le haut. */
    private static Pixmap plume() {
        int w = 16, h = 32;
        Pixmap pixmap = pixmap(w, h);
        Color edge = hex("c8d2dc"), core = Color.WHITE;
        for (int y = 0; y < h; y++) {
            float t = (float) y / h; // 0 en haut
            float half = w / 2f * (1f - 0.6f * t);
            for (int x = 0; x < w; x++) {
                float d = Math.abs(x + 0.5f - w / 2f);
                if (d > half) continue;
                Color c = d < half * 0.55f ? core : edge;
                pixmap.setColor(c.r, c.g, c.b, 0.3f + 0.6f * t);
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }
}
