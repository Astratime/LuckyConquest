package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.enemy.EnemyKind;

/**
 * L'entrée en scène d'un roi de l'Exploration, entre le soldat et le roi :
 * environ 8 secondes. D'abord le décor ({@link #PRELUDE} secondes, un plan à
 * part), puis le roi apparaît ; en place, il fait un geste et son nom
 * s'affiche ; fondu noir et sa présentation. Chaque lieu dessine ses quatre
 * rois ({@link PrairieKings}, {@link PortKings}, {@link MinesKings},
 * {@link CasinoKings}) ; le son est cutscene/king_<i>roi</i>.
 *
 * L'entrée a sa propre horloge : pendant {@link #step} et {@link #draw},
 * {@link #time} part de 0 à la fin du décor.
 */
public abstract class KingEntrance extends Cutscene {

    /** Le décor, avant l'entrée du roi (voir {@link #prelude}). */
    public static final float PRELUDE     = 2.0f;
    /** Le geste du roi en place, dans l'horloge de l'entrée ; son nom s'affiche un peu après. */
    public static final float GESTURE     = 3.4f;
    public static final float COVER_START = PRELUDE + 5.6f;
    public static final float COVER_FULL  = PRELUDE + 6.2f;

    protected final EnemyKind     kind;
    protected final TextureRegion king;
    /** Particules dessinées en lueurs (étincelles) ou en images ({@code region}). */
    protected final Array<Particle> parts = new Array<>(false, 256);
    private final BitmapFont font;
    private float previous;
    private boolean entered;
    /** Le geste du roi : décalage, inclinaison (degrés) et étirement du portrait, remis à zéro à chaque image. */
    private float poseX, poseY, poseTilt, poseStretch;

    protected KingEntrance(CutsceneKit kit, EnemyKind kind) {
        super(kit.settings(), kit.shake(), kit.sound("king_" + kind.name().toLowerCase()));
        this.kind = kind;
        king = kit.portrait(kind);
        font = kit.font();
    }

    /** @return l'entrée en scène du roi {@code king} ({@code null} si ce n'est pas un roi de l'Exploration). */
    public static Cutscene of(CutsceneKit kit, EnemyKind king) {
        return switch (king) {
            case ROI_PIQUE, ROI_TREFLE, ROI_COEUR, ROI_CARREAU -> new PrairieKings(kit, king);
            case CAPITAINE_RAT, TAVERNIER, GARDIEN_PHARE, CAPITAINE_NOIR -> new PortKings(kit, king);
            case BARON_OR, GRAND_FOREUR, MAITRE_FORGE, COEUR_MONTAGNE -> new MinesKings(kit, king);
            case SIRENE, JACKPOT_VIVANT, REQUIN_BANQUIER, KRAKEN -> new CasinoKings(kit, king);
            default -> null;
        };
    }

    @Override protected float coverStart() { return COVER_START; }
    @Override protected float coverFull()  { return COVER_FULL; }
    @Override protected Color coverColor() { return Color.BLACK; }

    @Override
    protected final void reset() {
        parts.clear();
        previous = 0f;
        entered = false;
        restart();
    }

    @Override
    protected final void simulate(float delta) {
        if (time < PRELUDE) {
            stepPrelude(delta);
            update(parts, delta);
            previous = time;
            return;
        }
        if (!entered) {                                  // changement de plan : l'horloge de l'entrée part de 0
            entered = true;
            parts.clear();
            previous = 0f;
        }
        float real = time;
        time -= PRELUDE;
        step(delta);
        update(parts, delta);
        previous = time;
        time = real;
    }

    @Override
    protected final void drawScene(Batch batch) {
        poseX = poseY = poseTilt = poseStretch = 0f;
        if (time < PRELUDE) {
            prelude(batch);
            drawParticles(batch, parts);
            fadeFromBlack(batch, 0.4f);
            fadeThroughBlack(batch, PRELUDE, 0.25f);
            return;
        }
        float real = time;
        time -= PRELUDE;
        draw(batch);
        drawName(batch);
        time = real;
        fadeThroughBlack(batch, PRELUDE, 0.25f);
    }

    /** Le nom du roi, qui s'imprime en bas de l'écran pendant son geste. */
    private void drawName(Batch batch) {
        float show = progress(GESTURE + 0.3f, GESTURE + 0.6f);
        if (show <= 0f || font == null) return;
        float width = worldWidth(), height = worldHeight();
        batch.setColor(0f, 0f, 0f, 0.55f * show);
        batch.draw(soft, width * 0.2f, -height * 0.02f, width * 0.6f, height * 0.18f);
        float scale = font.getData().scaleX;
        font.getData().setScale(scale * MathUtils.lerp(4.2f, 2.8f, Interpolation.pow2Out.apply(show)));
        caption(batch, font, kind.getBarName(), width / 2f, height * 0.07f, show);
        font.getData().setScale(scale);
    }

    /** Remet la scène au début. */
    protected void restart() {}

    /** Fait vivre le décor, avant l'entrée ({@link #time} de 0 à {@link #PRELUDE}). */
    protected void stepPrelude(float delta) {}

    /** Dessine le décor, avant l'entrée : un plan à part, qui plante l'ambiance. */
    protected abstract void prelude(Batch batch);

    /** Fait vivre la scène ; {@link #at} dit quand lancer chaque moment. */
    protected abstract void step(float delta);

    /** Dessine la scène. */
    protected abstract void draw(Batch batch);

    /** @return {@code true} une seule fois : à l'image où le temps passe {@code moment}. */
    protected boolean at(float moment) { return previous < moment && time >= moment; }

    /** @return la progression (0 à 1, puis 0) d'un geste qui dure de {@code from} à {@code to}, aller et retour. */
    protected float gesture(float from, float to) {
        float k = progress(from, to);
        return MathUtils.sin(k * MathUtils.PI);
    }

    /**
     * Le geste du roi pour cette image : il se décale de ({@code dx}, {@code dy}),
     * s'incline de {@code tilt} degrés autour de ses pieds et s'étire de {@code stretch} (0 = normal).
     */
    protected void pose(float dx, float dy, float tilt, float stretch) {
        poseX = dx;
        poseY = dy;
        poseTilt = tilt;
        poseStretch = stretch;
    }

    /** Le roi, centré en bas sur ({@code x}, {@code y}) ; {@code shadow} = 1 en ombre noire. */
    protected void drawKing(Batch batch, float x, float y, float scale, float shadow, float alpha) {
        float width = king.getRegionWidth() * scale * (1f - poseStretch * 0.5f);
        float height = king.getRegionHeight() * scale * (1f + poseStretch);
        float light = 1f - shadow;
        batch.setColor(light, light, light, alpha);
        batch.draw(king, x + poseX - width / 2f, y + poseY, width / 2f, 0f, width, height, 1f, 1f, poseTilt);
    }

    /** Une gerbe de {@code count} étincelles de couleur {@code color} autour de ({@code x}, {@code y}). */
    protected void sparks(float x, float y, int count, Color color, float speed, float size) {
        for (int i = 0; i < count; i++) {
            Particle spark = particle(x, y, 360f * random.nextFloat(), speed * (0.3f + 0.7f * random.nextFloat()),
                0.4f + 0.5f * random.nextFloat(), size * (0.5f + random.nextFloat()));
            spark.drag = 0.15f;
            spark.color.set(color);
            parts.add(spark);
        }
    }

    /** Des morceaux (images {@code region}) projetés de ({@code x}, {@code y}), qui retombent. */
    protected void debris(float x, float y, int count, TextureRegion region, float scale, float speed) {
        for (int i = 0; i < count; i++) {
            Particle piece = particle(x, y, 20f + 140f * random.nextFloat(), speed * (0.4f + 0.6f * random.nextFloat()),
                1.0f + 0.6f * random.nextFloat(), scale * (0.6f + 0.6f * random.nextFloat()));
            piece.gravity = 1400f;
            piece.region = region;
            piece.spin = (random.nextFloat() - 0.5f) * 720f;
            parts.add(piece);
        }
    }

    /** Une lueur qui pulse, entre {@code low} et 1 fois {@code alpha}. */
    protected float pulse(float speed, float low) {
        return low + (1f - low) * (0.5f + 0.5f * MathUtils.sin(time * speed));
    }

    /** Dessine un rectangle plein. */
    protected void rect(Batch batch, Color color, float x, float y, float w, float h) {
        batch.setColor(color);
        batch.draw(pixel, x, y, w, h);
    }

    /** Dessine un trait de ({@code x0}, {@code y0}) à ({@code x1}, {@code y1}), épais de {@code thick}. */
    protected void line(Batch batch, Color color, float x0, float y0, float x1, float y1, float thick) {
        float dx = x1 - x0, dy = y1 - y0;
        batch.setColor(color);
        batch.draw(pixel, x0, y0 - thick / 2f, 0f, thick / 2f, (float) Math.sqrt(dx * dx + dy * dy), thick, 1f, 1f,
            MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees);
    }

    /** De la pluie qui tombe en biais sur tout l'écran ({@code count} gouttes, {@code alpha}). */
    protected void rain(Batch batch, int count, float alpha) {
        float width = worldWidth() + 2f * MARGIN, height = worldHeight() + 2f * MARGIN;
        batch.setColor(0.7f, 0.78f, 0.95f, alpha);
        for (int i = 0; i < count; i++) {
            float speed = 1400f + (i * 37 % 11) * 60f;
            float x = ((i * 157.3f + time * 300f) % width) - MARGIN;
            float y = height - ((i * 91.7f + time * speed) % height) - MARGIN;
            batch.draw(pixel, x, y, 1.5f, 0f, 3f, 46f, 1f, 1f, -14f);
        }
    }

    /** Un éclair zigzag de ({@code x}, haut de l'écran) jusqu'à {@code bottom}, dessiné {@code seed} par {@code seed}. */
    protected void bolt(Batch batch, float x, float bottom, long seed, float alpha) {
        if (alpha <= 0f) return;
        java.util.Random zig = new java.util.Random(seed);
        float y = worldHeight() + MARGIN, step = (y - bottom) / 9f;
        additive(batch);
        for (int i = 0; i < 9; i++) {
            float nx = x + (zig.nextFloat() - 0.5f) * 140f, ny = y - step;
            tmp.set(0.6f, 0.7f, 1f, 0.5f * alpha);
            line(batch, tmp, x, y, nx, ny, 22f);
            tmp.set(1f, 1f, 1f, alpha);
            line(batch, tmp, x, y, nx, ny, 7f);
            x = nx;
            y = ny;
        }
        normal(batch);
    }

    protected float cx() { return worldWidth() / 2f; }
    /** @return le bas du roi quand il est en place. */
    protected float kingY() { return worldHeight() * 0.18f; }
    /** Agrandissement du portrait du roi en place. */
    protected static final float KING_SCALE = 12f;
}
