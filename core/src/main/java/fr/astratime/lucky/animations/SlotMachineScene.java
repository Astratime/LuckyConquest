package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import fr.astratime.lucky.assets.Palette;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Bingo du Triple Sept (proposition « machine à sous géante ») : une machine
 * à sous géante tombe sur la table, son levier s'abaisse, ses trois rouleaux
 * s'emballent puis s'arrêtent un à un sur le Triple Sept ; au troisième, ses
 * ampoules clignotent comme des folles et son bac déborde (pièces, 777 et
 * feux d'artifice lancés par {@link JackpotCelebration}).
 */
public class SlotMachineScene extends BingoScene {

    /** La machine est posée et le levier s'abaisse. */
    public static final float PULL_TIME = 0.3f;
    /** Arrêt de chaque rouleau ; le dernier est le jackpot. */
    public static final float[] STOPS = {0.95f, 1.25f, 1.55f};

    private static final int   W = 76, H = 74;             // corps de la machine, en pixels de l'image
    private static final int   WINDOW_W = 18, WINDOW_H = 22;
    private static final int[] WINDOW_X = {9, 29, 49};     // coin gauche de chaque fenêtre
    private static final int   WINDOW_Y = 24;              // haut des fenêtres (depuis le haut de l'image)
    private static final float SPIN_SPEED = 1500f;         // défilement des rouleaux, en points par seconde

    private final Image body, lever, glow;
    private final Image[] bulbs;
    private final Reel[]  reels = new Reel[3];
    private final TextureRegion[] strip;
    private final int     jackpotIndex;
    private float originX, originY;   // coin en bas à gauche du corps
    private boolean jackpot;

    /**
     * @param tripleSeven image du Triple Sept, sur laquelle s'arrêtent les rouleaux
     * @param others      autres symboles qui défilent
     */
    public SlotMachineScene(TextureRegion tripleSeven, TextureRegion... others) {
        strip = new TextureRegion[others.length + 1];
        System.arraycopy(others, 0, strip, 0, others.length);
        jackpotIndex = others.length;
        strip[jackpotIndex] = tripleSeven;

        glow = image(texture(halo(80, hex("fff0a0"))));
        glow.setOrigin(glow.getWidth() / 2f, glow.getHeight() / 2f);
        lever = image(texture(lever()));
        lever.setOrigin(lever.getWidth() / 2f, 3 * SCALE);   // pivot en bas du bras
        body = image(texture(body()));
        addActor(glow);
        addActor(lever);
        addActor(body);
        for (int i = 0; i < 3; i++) {
            reels[i] = new Reel();
            addActor(reels[i]);
        }
        int bulbCount = 9;
        bulbs = new Image[bulbCount];
        TextureRegion bulb = new TextureRegion(texture(bulb()));
        for (int i = 0; i < bulbCount; i++) {
            bulbs[i] = image(bulb, SCALE);
            addActor(bulbs[i]);
        }
    }

    /** @return le centre (Stage) du bac à pièces, d'où jaillit le jackpot. */
    public float trayX() { return originX + W * SCALE / 2f; }

    /** @return le haut (Stage) du bac à pièces. */
    public float trayY() { return originY + 12 * SCALE; }

    /** @return le centre (Stage) de la fenêtre {@code index}. */
    public float windowX(int index) { return originX + (WINDOW_X[index] + WINDOW_W / 2f) * SCALE; }

    /** @return la hauteur (Stage) du centre des fenêtres. */
    public float windowY() { return originY + (H - WINDOW_Y - WINDOW_H / 2f) * SCALE; }

    /** La machine est posée, centrée en {@code x}, sa base en {@code y}. */
    @Override
    protected void start(float x, float y) {
        originX = x - W * SCALE / 2f;
        originY = y;
        jackpot = false;
        body.setPosition(originX, originY);
        placeOrigin(lever, originX + W * SCALE + 3 * SCALE, originY + 34 * SCALE);
        placeOrigin(glow, x, windowY());
        glow.setScale(1.5f);
        glow.getColor().a = 0f;
        float left = originX + 8 * SCALE, span = (W - 16) * SCALE;
        for (int i = 0; i < bulbs.length; i++) {
            bulbs[i].setPosition(left + span * i / (bulbs.length - 1) - bulbs[i].getWidth() / 2f,
                originY + (H - 9) * SCALE - bulbs[i].getHeight() / 2f);
        }
        for (int i = 0; i < 3; i++) {
            float wx = originX + WINDOW_X[i] * SCALE, wy = originY + (H - WINDOW_Y - WINDOW_H) * SCALE;
            reels[i].setBounds(wx, wy, WINDOW_W * SCALE, WINDOW_H * SCALE);
            reels[i].reset(STOPS[i], MathUtils.random(strip.length * 3));
        }
        update(0f);
    }

    @Override
    protected void update(float delta) {
        // La machine tombe du haut de l'écran et s'écrase sur la table.
        float drop = time < PULL_TIME ? 700f * (1f - Interpolation.bounceOut.apply(time / PULL_TIME)) : 0f;
        setY(drop);

        // Le levier s'abaisse, puis remonte en ressort.
        float pull;
        if (time < PULL_TIME) pull = 0f;
        else if (time < PULL_TIME + 0.18f) pull = (time - PULL_TIME) / 0.18f;
        else pull = 1f - Interpolation.elasticOut.apply(Math.min(1f, (time - PULL_TIME - 0.18f) / 0.6f));
        lever.setScaleY(1f - 1.7f * pull);   // la boule passe sous le pivot : le bras pivote vers l'avant

        for (Reel reel : reels) reel.advance(time, delta);

        // Les ampoules chenillent pendant la course, puis clignotent toutes ensemble au jackpot.
        if (!jackpot && time >= STOPS[2]) jackpot = true;
        for (int i = 0; i < bulbs.length; i++) {
            boolean on = jackpot ? MathUtils.floor(time * 12f) % 2 == 0
                : (MathUtils.floor(time * 14f) + i) % 3 == 0;
            Color lit = Palette.FESTIVE[(i + (jackpot ? MathUtils.floor(time * 12f) : 0)) % Palette.FESTIVE.length];
            bulbs[i].setColor(on ? lit : hex("5a4030"));
        }
        if (jackpot) glow.getColor().a = 0.45f + 0.2f * MathUtils.sin(time * 30f);
    }

    /**
     * Un rouleau dans sa fenêtre : la bande de symboles défile vers le bas,
     * découpée par la fenêtre ; à son arrêt, elle freine et rebondit sur le Triple Sept.
     */
    private final class Reel extends Actor {
        private float stopAt;
        private float scroll;       // position de la bande, en hauteurs de case
        private float landing;      // position finale (case du Triple Sept)
        private boolean landed;

        void reset(float stopAt, int startCell) {
            this.stopAt = stopAt;
            scroll  = startCell;
            landed  = false;
            float cells = SPIN_SPEED * stopAt / getHeight();
            // Le rouleau doit finir sur le Triple Sept : on règle le départ pour tomber pile.
            float end = scroll + cells;
            landing = (float) Math.ceil((end - jackpotIndex) / strip.length) * strip.length + jackpotIndex;
            scroll  = landing - cells;
        }

        void advance(float time, float delta) {
            if (time < PULL_TIME + 0.1f) return;
            float since = time - (PULL_TIME + 0.1f), span = stopAt - (PULL_TIME + 0.1f);
            float progress = Math.min(1f, since / span);
            float start = landing - SPIN_SPEED * stopAt / getHeight();
            // Vitesse constante puis petit dépassement et retour sur la case.
            float eased = progress < 0.85f ? progress : 0.85f + 0.15f * Interpolation.swingOut.apply((progress - 0.85f) / 0.15f);
            scroll = start + (landing - start) * eased;
            landed = progress >= 1f;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            batch.flush();
            if (!clipBegin()) return;
            Color color = getColor();
            float cell = getHeight();
            int first = MathUtils.floor(scroll);
            float offset = (scroll - first) * cell;
            for (int k = -1; k <= 1; k++) {
                int index = Math.floorMod(first + k, strip.length);
                TextureRegion region = strip[index];
                float size = getWidth() * 0.9f;
                float iconH = size * region.getRegionHeight() / region.getRegionWidth();
                float cy = getY() + getHeight() / 2f - offset + k * cell;
                boolean flashing = landed && k == 0 && MathUtils.floor(time * 10f) % 2 == 0;
                batch.setColor(flashing ? 1f : color.r, flashing ? 0.95f : color.g, flashing ? 0.7f : color.b, parentAlpha);
                batch.draw(region, getX() + (getWidth() - size) / 2f, cy - iconH / 2f, size, iconH);
            }
            batch.setColor(Color.WHITE);
            batch.flush();
            clipEnd();
        }
    }

    /** @return le corps de la machine : fronton doré, trois fenêtres, bac à pièces. */
    private static Pixmap body() {
        Pixmap pixmap = pixmap(W, H);
        Color red = hex("c8232e"), redLight = hex("ff4a5a"), redDark = hex("8a141c");
        Color gold = Palette.GOLD, goldDark = hex("c08a20"), chrome = Palette.STEEL_LIGHT;
        // Corps arrondi en haut.
        Shape shape = (x, y) -> {
            if (y < 14) {
                float dx = x + 0.5f - W / 2f, dy = y - 14f;
                return dx * dx / ((W / 2f) * (W / 2f)) + dy * dy / 196f <= 1f;
            }
            return x >= 0 && x < W;
        };
        fillShape(pixmap, shape, (x, y) -> {
            if (y < 16) return y < 4 ? hex("fff2a0") : (x < W / 3 ? gold : goldDark);   // fronton
            if (y >= H - 14) return y == H - 14 ? chrome : (x < 6 ? redLight : redDark); // socle
            return x < 4 ? redLight : x > W - 5 ? redDark : red;
        });
        // Bande où courent les ampoules.
        hLine(pixmap, 3, W - 4, 16, OUTLINE);
        hLine(pixmap, 3, W - 4, 17, hex("3a1a10"));
        hLine(pixmap, 3, W - 4, 18, hex("3a1a10"));
        hLine(pixmap, 3, W - 4, 19, OUTLINE);
        // Cadre chromé autour des trois fenêtres, et leur fond.
        fillOutlined(pixmap, 6, WINDOW_Y - 3, W - 12, WINDOW_H + 6, chrome);
        for (int wx : WINDOW_X) {
            fillOutlined(pixmap, wx - 1, WINDOW_Y - 1, WINDOW_W + 2, WINDOW_H + 2, Color.WHITE);
            hLine(pixmap, wx, wx + WINDOW_W - 1, WINDOW_Y, hex("e8e8f0"));
        }
        // Ligne de paiement rouge en travers des fenêtres, aux bords du cadre.
        pixmap.setColor(Palette.RUBY);
        pixmap.fillRectangle(4, WINDOW_Y + WINDOW_H / 2 - 1, 2, 2);
        pixmap.fillRectangle(W - 6, WINDOW_Y + WINDOW_H / 2 - 1, 2, 2);
        // Bac à pièces.
        fillOutlined(pixmap, 16, H - 12, W - 32, 8, hex("2a2030"));
        hLine(pixmap, 17, W - 18, H - 11, gold);
        // Rivets.
        pixmap.setColor(goldDark);
        for (int x = 8; x < W - 6; x += 10) pixmap.fillRectangle(x, H - 3, 2, 1);
        return pixmap;
    }

    /** @return le levier : bras chromé, boule rouge en haut. */
    private static Pixmap lever() {
        int width = 9, height = 34;
        Pixmap pixmap = pixmap(width, height);
        fillOutlined(pixmap, width / 2 - 1, 7, 3, height - 7, Palette.STEEL_LIGHT);
        fillOutlinedCircle(pixmap, width / 2, 4, 4, Palette.RED);
        pixmap.setColor(Palette.RUBY);
        pixmap.fillRectangle(width / 2 - 2, 2, 2, 2);
        return pixmap;
    }

    /** @return une ampoule blanche (teintée à l'affichage). */
    private static Pixmap bulb() {
        Pixmap pixmap = pixmap(4, 4);
        pixmap.setColor(Color.WHITE);
        pixmap.fillRectangle(1, 0, 2, 4);
        pixmap.fillRectangle(0, 1, 4, 2);
        return pixmap;
    }
}
