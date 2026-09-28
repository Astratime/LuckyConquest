package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Palette;

/**
 * Bingo de la Cloche : une cloche d'église descend, suspendue à sa poutre ;
 * une mailloche, suspendue au-dessus de son flanc droit comme le marteau d'un
 * clocher, prend son élan et la frappe de face, puis la cloche se
 * balance de gauche à droite de moins en moins fort. À chaque extrémité d'un
 * balancement, {@link SwingListener} est prévenu (pour faire sonner la cloche
 * et en faire jaillir des clochettes).
 *
 * La cloche, sa poutre et la mailloche sont dessinées en pixel art (petites
 * images agrandies sans lissage). Les positions sont celles du Stage : l'acteur
 * doit être placé à l'origine.
 */
public class ChurchBellAnimation extends Group implements Disposable {

    /** Appelé à chaque extrémité d'un balancement de la cloche. */
    public interface SwingListener {
        /**
         * @param mouthX    abscisse (Stage) du centre de la bouche de la cloche
         * @param mouthY    ordonnée (Stage) du centre de la bouche de la cloche
         * @param direction -1 si la cloche penche vers la gauche, 1 vers la droite
         */
        void onSwingPeak(float mouthX, float mouthY, int direction);
    }

    /** Instant où la mailloche frappe la cloche, depuis {@link #play}. */
    public static final float STRIKE_TIME = 0.85f;

    private static final float SCALE        = 5f;     // agrandissement entier : pixels nets
    private static final float DROP_TIME    = 0.35f;  // descente de la cloche
    private static final float DROP_HEIGHT  = 420f;
    private static final float MALLET_IN    = 0.3f;   // la mailloche apparaît
    private static final float WINDUP_AT    = 0.45f;  // elle prend son élan
    private static final float SWING_DOWN   = 0.12f;  // durée de la frappe
    // Angles de la mailloche (0 : tête en haut ; 180 : suspendue, tête en bas).
    private static final float MALLET_REST   = 200f;  // repos : tête légèrement à droite
    private static final float MALLET_WINDUP = 245f;  // élan : tête rejetée vers la droite
    private static final float MALLET_HIT    = 180f;  // frappe : manche vertical, tête contre la cloche
    private static final float MALLET_RECOIL = 220f;
    private static final float FADE_AT     = 2.45f;
    private static final float FADE_TIME   = 0.45f;

    // Balancement amorti : angle = -AMPLITUDE * e^(-DAMPING t) * sin(2π t / PERIOD).
    private static final float AMPLITUDE = 30f;
    private static final float DAMPING   = 0.9f;
    private static final float PERIOD    = 0.9f;
    private static final int   PEAKS     = 4;

    // Image de la cloche (en pixels de l'image, y vers le bas) : joug en bois,
    // anse, corps, puis le battant qui dépasse sous la bouche.
    private static final int BELL_WIDTH   = 52;
    private static final int YOKE_ROWS    = 5;
    private static final int CANON_ROWS   = 4;
    private static final int BODY_ROWS    = 40;
    private static final int CLAPPER_ROWS = 7;
    private static final int BELL_HEIGHT  = YOKE_ROWS + CANON_ROWS + BODY_ROWS + CLAPPER_ROWS;
    /** Rangée du corps où frappe la mailloche (flanc de la cloche). */
    private static final int STRIKE_ROW   = 20;

    private static final int MALLET_WIDTH  = 16;
    private static final int MALLET_HEAD   = 12;
    private static final int MALLET_HEIGHT = 42;
    private static final int BEAM_WIDTH    = 90;
    private static final int BEAM_HEIGHT   = 8;

    private final Texture bellTexture   = createBell();
    private final Texture malletTexture = createMallet();
    private final Texture beamTexture   = createBeam();

    private final Group tower = new Group();   // poutre et cloche, qui descendent ensemble
    private final Image beam;
    private final Image bell;
    private final Image mallet;

    private final Vector2 pivot = new Vector2();
    private SwingListener listener;
    private boolean swinging;
    private float   swingTime;
    private int     nextPeak;

    public ChurchBellAnimation() {
        setTouchable(Touchable.disabled);
        setVisible(false);

        beam = image(beamTexture);
        bell = image(bellTexture);
        // Axe de rotation : le milieu du joug, posé sur la poutre.
        bell.setOrigin(BELL_WIDTH / 2f * SCALE, (BELL_HEIGHT - YOKE_ROWS / 2f) * SCALE);
        mallet = image(malletTexture);
        // Axe de rotation : le bout du manche, opposé à la tête.
        mallet.setOrigin(MALLET_WIDTH / 2f * SCALE, 2f * SCALE);

        tower.addActor(beam);
        tower.addActor(bell);
        addActor(tower);
        addActor(mallet);
    }

    private static Image image(Texture texture) {
        Image image = new Image(new TextureRegionDrawable(new TextureRegion(texture)));
        image.setSize(texture.getWidth() * SCALE, texture.getHeight() * SCALE);
        return image;
    }

    /** @param listener prévenu à chaque extrémité d'un balancement */
    public void setSwingListener(SwingListener listener) { this.listener = listener; }

    /** Lance l'animation : la cloche est suspendue en {@code (pivotX, pivotY)}, le milieu de son joug. */
    public void play(float pivotX, float pivotY) {
        hide();
        setVisible(true);
        getColor().a = 1f;
        pivot.set(pivotX, pivotY);

        beam.setPosition(pivotX - beam.getWidth() / 2f, pivotY - beam.getHeight() / 2f);
        bell.setPosition(pivotX - bell.getOriginX(), pivotY - bell.getOriginY());
        bell.setRotation(0f);
        tower.setPosition(0f, DROP_HEIGHT);
        tower.addAction(Actions.moveTo(0f, 0f, DROP_TIME, Interpolation.pow2Out));

        // Suspendue tête en bas, la face gauche de sa tête touche le flanc de la cloche.
        Vector2 strike = strikePoint();
        float headCenter = (MALLET_HEIGHT - 2f - MALLET_HEAD / 2f) * SCALE;
        mallet.setPosition(strike.x + MALLET_WIDTH / 2f * SCALE - mallet.getOriginX(),
            strike.y + headCenter - mallet.getOriginY());
        mallet.setRotation(MALLET_REST);
        mallet.getColor().a = 0f;
        mallet.addAction(Actions.sequence(
            Actions.delay(MALLET_IN),
            Actions.fadeIn(0.12f),
            Actions.delay(WINDUP_AT - MALLET_IN - 0.12f),
            Actions.rotateTo(MALLET_WINDUP, STRIKE_TIME - SWING_DOWN - WINDUP_AT, Interpolation.pow2Out),
            Actions.rotateTo(MALLET_HIT, SWING_DOWN, Interpolation.pow3In),
            Actions.run(this::startSwing),
            // Le choc la renvoie en arrière, puis elle s'efface.
            Actions.parallel(
                Actions.rotateTo(MALLET_RECOIL, 0.25f, Interpolation.pow2Out),
                Actions.sequence(Actions.delay(0.15f), Actions.fadeOut(0.3f)))));

        addAction(Actions.sequence(Actions.delay(FADE_AT), Actions.fadeOut(FADE_TIME), Actions.visible(false)));
    }

    /** @return le point (Stage) du flanc droit de la cloche immobile où frappe la mailloche. */
    public Vector2 strikePoint() {
        int bodyRow = YOKE_ROWS + CANON_ROWS + STRIKE_ROW;
        return new Vector2(pivot.x + halfWidth(STRIKE_ROW) * SCALE,
            pivot.y - (bodyRow - YOKE_ROWS / 2f) * SCALE);
    }

    /** @return la position (Stage) du centre de la bouche de la cloche, selon son inclinaison actuelle. */
    public Vector2 mouth() {
        float distance = (YOKE_ROWS / 2f + CANON_ROWS + BODY_ROWS) * SCALE;
        float angle = bell.getRotation();
        // Sous l'axe : (0, -distance) tourné de l'angle de la cloche.
        return new Vector2(pivot.x + MathUtils.sinDeg(angle) * distance,
            pivot.y + tower.getY() - MathUtils.cosDeg(angle) * distance);
    }

    private void startSwing() {
        swinging  = true;
        swingTime = 0f;
        nextPeak  = 0;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!swinging) return;
        swingTime += delta;
        float omega = MathUtils.PI2 / PERIOD;
        bell.setRotation(-AMPLITUDE * (float) Math.exp(-DAMPING * swingTime) * MathUtils.sin(omega * swingTime));
        // Extrémités : sin = ±1 ; la première penche à gauche (la frappe vient de la droite).
        while (nextPeak < PEAKS && swingTime >= (MathUtils.HALF_PI + nextPeak * MathUtils.PI) / omega) {
            if (listener != null) {
                Vector2 mouth = mouth();
                listener.onSwingPeak(mouth.x, mouth.y, nextPeak % 2 == 0 ? -1 : 1);
            }
            nextPeak++;
        }
    }

    /** Cache tout immédiatement. */
    public void hide() {
        clearActions();
        tower.clearActions();
        mallet.clearActions();
        swinging = false;
        setVisible(false);
    }

    /** @return la demi-largeur (en pixels de l'image) de la rangée {@code row} du corps de la cloche. */
    private static int halfWidth(int row) {
        float t = (row + 0.5f) / BODY_ROWS;
        float half;
        if (t < 0.12f)     half = 7f + 7f * (float) Math.sqrt(t / 0.12f);     // épaule arrondie
        else if (t < 0.7f) half = 14f + (t - 0.12f) * 6f;                     // taille, presque droite
        else               half = 17.5f + (t - 0.7f) / 0.3f * (t - 0.7f) / 0.3f * 7.5f; // pince évasée
        return Math.round(half);
    }

    /**
     * @return la cloche d'église en bronze : joug en bois, anse, corps éclairé à
     *         gauche et ombré à droite, cordons décoratifs, pince épaisse et
     *         battant qui dépasse de la bouche.
     */
    private static Texture createBell() {
        Pixmap pixmap = new Pixmap(BELL_WIDTH, BELL_HEIGHT, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        Color outline = Palette.OUTLINE;
        Color bronze  = Color.valueOf("d9a441ff");
        Color light   = Color.valueOf("ffe08aff");
        Color mid     = Color.valueOf("b98530ff");
        Color shadow  = Color.valueOf("8a5a1eff");
        Color band    = Color.valueOf("7a4c16ff");
        Color wood    = Palette.WOOD;
        Color woodLit = Palette.WOOD_LIGHT;
        int center = BELL_WIDTH / 2;

        // Joug : pièce de bois qui porte la cloche.
        int yokeHalf = 15;
        fillOutlined(pixmap, center - yokeHalf, 0, yokeHalf * 2, YOKE_ROWS, outline, wood);
        pixmap.setColor(woodLit);
        pixmap.drawLine(center - yokeHalf + 1, 1, center + yokeHalf - 2, 1);

        // Anse : les attaches de bronze entre le joug et le corps.
        fillOutlined(pixmap, center - 5, YOKE_ROWS - 1, 10, CANON_ROWS + 2, outline, mid);
        pixmap.setColor(light);
        pixmap.drawLine(center - 4, YOKE_ROWS, center - 4, YOKE_ROWS + CANON_ROWS - 1);

        // Corps.
        int top = YOKE_ROWS + CANON_ROWS;
        for (int row = 0; row < BODY_ROWS; row++) {
            int half = halfWidth(row);
            int y = top + row;
            for (int x = center - half; x < center + half; x++) {
                boolean edge = x == center - half || x == center + half - 1 || row == 0 || row == BODY_ROWS - 1;
                float across = (x - (center - half) + 0.5f) / (half * 2f); // 0 à gauche, 1 à droite
                Color color;
                if (edge)                                  color = outline;
                else if (row == 11 || row == 12 || row == 33) color = band;       // cordons décoratifs
                else if (row >= BODY_ROWS - 4)             color = across < 0.3f ? mid : shadow; // pince
                else if (across < 0.14f)                   color = mid;
                else if (across < 0.32f)                   color = light;
                else if (across < 0.72f)                   color = bronze;
                else if (across < 0.86f)                   color = mid;
                else                                       color = shadow;
                pixmap.setColor(color);
                pixmap.drawPixel(x, y);
            }
        }

        // Battant : une boule de fer qui dépasse sous la bouche.
        int clapperY = top + BODY_ROWS + 3;
        pixmap.setColor(outline);
        pixmap.fillCircle(center, clapperY, 3);
        pixmap.drawLine(center, top + BODY_ROWS - 1, center, clapperY);
        pixmap.setColor(Color.valueOf("6a6a78ff"));
        pixmap.fillCircle(center, clapperY, 2);
        pixmap.setColor(Color.valueOf("a8a8b8ff"));
        pixmap.drawPixel(center - 1, clapperY - 1);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /** @return la mailloche, tête en haut : tête de bois cerclée de fer, manche au grip rouge. */
    private static Texture createMallet() {
        Pixmap pixmap = new Pixmap(MALLET_WIDTH, MALLET_HEIGHT, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        Color outline = Palette.OUTLINE;
        fillOutlined(pixmap, 0, 0, MALLET_WIDTH, MALLET_HEAD, outline, Color.valueOf("7a3b1eff"));
        pixmap.setColor(Color.valueOf("a8582eff"));
        pixmap.drawLine(1, 1, MALLET_WIDTH - 2, 1);
        pixmap.setColor(Palette.STEEL_LIGHT); // cercles de fer
        pixmap.drawLine(1, 3, MALLET_WIDTH - 2, 3);
        pixmap.drawLine(1, MALLET_HEAD - 4, MALLET_WIDTH - 2, MALLET_HEAD - 4);

        int handle = 4;
        int left = (MALLET_WIDTH - handle) / 2;
        fillOutlined(pixmap, left, MALLET_HEAD - 1, handle, MALLET_HEIGHT - MALLET_HEAD + 1, outline,
            Palette.WOOD_LIGHT);
        pixmap.setColor(Palette.WOOD_HIGHLIGHT);
        pixmap.drawLine(left + 1, MALLET_HEAD, left + 1, MALLET_HEIGHT - 12);
        // Grip entouré de cuir rouge.
        for (int y = MALLET_HEIGHT - 10; y < MALLET_HEIGHT - 1; y++) {
            pixmap.setColor(y % 2 == 0 ? Palette.RED : Color.valueOf("a81e28ff"));
            pixmap.drawLine(left + 1, y, left + handle - 2, y);
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /** @return la poutre du clocher, où la cloche est suspendue. */
    private static Texture createBeam() {
        Pixmap pixmap = new Pixmap(BEAM_WIDTH, BEAM_HEIGHT, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        fillOutlined(pixmap, 0, 0, BEAM_WIDTH, BEAM_HEIGHT, Palette.OUTLINE, Palette.WOOD_DARK);
        pixmap.setColor(Palette.WOOD);
        pixmap.drawLine(1, 1, BEAM_WIDTH - 2, 1);
        pixmap.setColor(Color.valueOf("4e2f18ff")); // veines du bois
        for (int x = 4; x < BEAM_WIDTH - 4; x += 9) {
            pixmap.drawLine(x, 3 + (x / 9) % 3, x + 4, 3 + (x / 9) % 3);
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /** Remplit un rectangle de {@code fill} cerné d'un liseré {@code outline}. */
    private static void fillOutlined(Pixmap pixmap, int x, int y, int width, int height, Color outline, Color fill) {
        pixmap.setColor(outline);
        pixmap.fillRectangle(x, y, width, height);
        pixmap.setColor(fill);
        pixmap.fillRectangle(x + 1, y + 1, width - 2, height - 2);
    }

    @Override
    public void dispose() {
        bellTexture.dispose();
        malletTexture.dispose();
        beamTexture.dispose();
    }
}
