package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import fr.astratime.lucky.assets.Palette;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Bingo du Sept (proposition « roue de la fortune ») : une grande roue de
 * foire, garnie des symboles des rouleaux, tourne à toute vitesse ; son
 * cliquet claque à chaque case, elle ralentit et s'arrête sur la case dorée
 * du « 7 », qui s'illumine puis bondit hors de la roue (les gerbes de 7 et
 * les étincelles sont lancées par {@link JackpotCelebration}).
 */
public class FortuneWheelScene extends BingoScene {

    /** La roue s'immobilise sur le « 7 ». */
    public static final float STOP_TIME = 1.45f;
    /** Le « 7 » bondit hors de sa case. */
    public static final float POP_TIME  = 1.6f;

    private static final int   RADIUS   = 40;        // rayon de la roue, en pixels de l'image
    private static final int   SEGMENTS = 8;
    private static final float TURNS    = 4.25f;     // tours complets avant l'arrêt
    private static final float ICON_SIZE = 74f;
    private static final float BIG_SEVEN = 190f;

    private final Image wheel, hub, pointer, stand, glow, seven;
    private final Image[] icons = new Image[SEGMENTS];
    private float centerX, centerY;
    private float tick;           // battement du cliquet (degrés), retombe à chaque image
    private int   lastSegment;
    private boolean stopped;

    /**
     * @param seven  image du symbole Sept, sur la case dorée
     * @param others images des autres cases (au moins {@code SEGMENTS - 1})
     */
    public FortuneWheelScene(TextureRegion seven, TextureRegion... others) {
        stand = image(texture(stand()));
        glow  = image(texture(halo(60, hex("ffe680"))));
        glow.setOrigin(glow.getWidth() / 2f, glow.getHeight() / 2f);
        wheel = image(texture(wheel()));
        wheel.setOrigin(wheel.getWidth() / 2f, wheel.getHeight() / 2f);
        hub = image(texture(hub()));
        hub.setOrigin(hub.getWidth() / 2f, hub.getHeight() / 2f);
        pointer = image(texture(pointer()));
        pointer.setOrigin(pointer.getWidth() / 2f, pointer.getHeight() - 2 * SCALE); // pivot en haut
        addActor(stand);
        addActor(glow);
        addActor(wheel);
        for (int i = 0; i < SEGMENTS; i++) {
            TextureRegion region = i == 0 ? seven : others[(i - 1) % others.length];
            icons[i] = image(region, ICON_SIZE / region.getRegionWidth());
            icons[i].setOrigin(icons[i].getWidth() / 2f, icons[i].getHeight() / 2f);
            addActor(icons[i]);
        }
        addActor(hub);
        addActor(pointer);
        this.seven = image(seven, BIG_SEVEN / seven.getRegionWidth());
        this.seven.setOrigin(this.seven.getWidth() / 2f, this.seven.getHeight() / 2f);
        addActor(this.seven);
    }

    /** @return le rayon de la roue affichée. */
    public float radius() { return RADIUS * SCALE; }

    /** @return la position (Stage) du haut de la roue, où se trouve le « 7 » à l'arrêt. */
    public float topY() { return centerY + radius() * 0.68f; }

    @Override
    protected void start(float x, float y) {
        centerX = x;
        centerY = y;
        stopped = false;
        tick = 0f;
        lastSegment = 0;
        float size = (RADIUS * 2 + 1) * SCALE;
        stand.setPosition(x - stand.getWidth() / 2f, y - size / 2f - stand.getHeight() + 6 * SCALE);
        placeOrigin(wheel, x, y);
        placeOrigin(hub, x, y);
        placeOrigin(pointer, x, y + size / 2f + 4 * SCALE);
        placeOrigin(glow, x, y + radius() * 0.68f);
        glow.getColor().a = 0f;
        glow.setScale(1.6f);
        seven.setVisible(false);
        seven.setColor(Color.WHITE);
        for (Image icon : icons) icon.setColor(Color.WHITE);
        // La roue tombe du haut de l'écran sur son pied.
        setY(700f);
        addAction(Actions.moveTo(0f, 0f, 0.3f, Interpolation.bounceOut));
        update(0f);
    }

    @Override
    protected void update(float delta) {
        float progress = Math.min(1f, time / STOP_TIME);
        // Le « 7 » (case 0) finit en haut : la case 0 est dessinée en haut à l'angle 0.
        float angle = -TURNS * 360f * (1f - Interpolation.pow3Out.apply(progress));
        wheel.setRotation(angle);
        for (int i = 0; i < SEGMENTS; i++) {
            float a = 90f + angle + i * 360f / SEGMENTS;
            float distance = radius() * 0.68f;
            placeOrigin(icons[i], centerX + MathUtils.cosDeg(a) * distance, centerY + MathUtils.sinDeg(a) * distance);
            icons[i].setRotation(a - 90f);
        }
        // Le cliquet saute à chaque case qui passe sous lui.
        int segment = MathUtils.floor(-angle / (360f / SEGMENTS) + 0.5f);
        if (segment != lastSegment) {
            lastSegment = segment;
            tick = 24f;
        }
        tick = Math.max(0f, tick - delta * 260f);
        pointer.setRotation(tick);

        if (time >= STOP_TIME && !stopped) {
            stopped = true;
            glow.addAction(Actions.sequence(Actions.alpha(1f, 0.1f),
                Actions.forever(Actions.sequence(Actions.alpha(0.55f, 0.15f), Actions.alpha(1f, 0.15f)))));
            icons[0].addAction(Actions.forever(Actions.sequence(
                Actions.color(hex("fff2a0"), 0.1f), Actions.color(Color.WHITE, 0.1f))));
        }
        // Le grand « 7 » jaillit de sa case et monte au-dessus de la roue.
        if (time >= POP_TIME) {
            float t = Math.min(1f, (time - POP_TIME) / 0.4f);
            seven.setVisible(true);
            seven.setScale(0.35f + 0.65f * Interpolation.swingOut.apply(t));
            seven.setRotation(MathUtils.sin(time * 9f) * 6f);
            placeOrigin(seven, centerX, MathUtils.lerp(topY(), centerY + radius() + 70f, Interpolation.pow2Out.apply(t)));
        }
    }

    /** @return la roue : cases rouges et crème, case du 7 dorée, jante en laiton cloutée d'ampoules. */
    private static Pixmap wheel() {
        int size = RADIUS * 2 + 1;
        Pixmap pixmap = pixmap(size, size);
        Color red = hex("c8232e"), redDark = hex("9a1820"), cream = hex("f3e6c4"), creamDark = hex("d8c79c");
        Color gold = hex("ffd54a"), goldDark = hex("e0a020"), rim = hex("b07a2c"), rimLight = hex("e8b850");
        float c = RADIUS;
        Shape disc = (x, y) -> {
            float dx = x - c, dy = y - c;
            return dx * dx + dy * dy <= RADIUS * RADIUS;
        };
        float step = 360f / SEGMENTS;
        fillShape(pixmap, disc, (x, y) -> {
            float dx = x - c, dy = c - y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance > RADIUS - 4) return distance > RADIUS - 2 ? rim : rimLight;
            // Angle mesuré depuis le haut ; la case 0 est centrée en haut.
            float a = MathUtils.atan2(dy, dx) * MathUtils.radDeg - 90f;
            a = ((a % 360f) + 360f + step / 2f) % 360f;
            int segment = (int) (a / step);
            float inSegment = a - segment * step;
            if (inSegment < 1.6f * 40f / Math.max(distance, 1f)) return OUTLINE; // rayons entre les cases
            boolean shade = distance > RADIUS - 8;
            if (segment == 0) return shade ? goldDark : gold;
            return segment % 2 == 1 ? (shade ? redDark : red) : (shade ? creamDark : cream);
        });
        // Ampoules sur la jante.
        for (int k = 0; k < 24; k++) {
            float a = k * 15f;
            int bx = Math.round(c + MathUtils.cosDeg(a) * (RADIUS - 2.5f));
            int by = Math.round(c + MathUtils.sinDeg(a) * (RADIUS - 2.5f));
            pixmap.setColor(k % 2 == 0 ? Color.WHITE : hex("fff2a0"));
            pixmap.fillRectangle(bx - 1, by - 1, 2, 2);
        }
        return pixmap;
    }

    /** @return le moyeu doré au centre de la roue. */
    private static Pixmap hub() {
        int size = 15;
        Pixmap pixmap = pixmap(size, size);
        fillOutlinedCircle(pixmap, size / 2, size / 2, size / 2, hex("e0a020"));
        pixmap.setColor(hex("ffd54a"));
        pixmap.fillCircle(size / 2, size / 2, size / 2 - 2);
        pixmap.setColor(Color.WHITE);
        pixmap.fillRectangle(size / 2 - 2, size / 2 - 3, 2, 2);
        return pixmap;
    }

    /** @return le cliquet rouge qui pointe vers le bas, sur le haut de la roue. */
    private static Pixmap pointer() {
        int width = 13, height = 16;
        Pixmap pixmap = pixmap(width, height);
        Shape shape = (x, y) -> {
            if (y < 5) return Math.abs(x - width / 2) <= 5;           // tête ronde
            float half = 5f * (height - 1 - y) / (height - 6f);       // pointe vers le bas
            return Math.abs(x - width / 2) <= half;
        };
        fillShape(pixmap, shape, (x, y) -> x < width / 2 ? Palette.RUBY : Palette.RED);
        pixmap.setColor(Color.WHITE);
        pixmap.fillRectangle(width / 2 - 1, 2, 2, 2);
        return pixmap;
    }

    /** @return le pied en bois de la roue. */
    private static Pixmap stand() {
        int width = 46, height = 30;
        Pixmap pixmap = pixmap(width, height);
        Shape shape = (x, y) -> {
            if (y >= height - 5) return x >= 2 && x < width - 2;      // socle
            float half = 6f + y * 10f / height;                        // montant qui s'évase
            return Math.abs(x + 0.5f - width / 2f) <= half;
        };
        fillShape(pixmap, shape, (x, y) -> y >= height - 5 ? (y == height - 4 ? Palette.WOOD_LIGHT : Palette.WOOD_DARK)
            : x < width / 2 - 2 ? Palette.WOOD_LIGHT : Palette.WOOD);
        return pixmap;
    }
}
