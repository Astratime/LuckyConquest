package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.enemy.EnemyKind;

/**
 * L'entrée en scène d'un roi de l'Exploration, entre le soldat et le roi :
 * environ 4 secondes, le roi apparaît, puis fondu noir et sa présentation.
 * Chaque lieu dessine ses quatre rois ({@link PrairieKings}, {@link PortKings},
 * {@link MinesKings}, {@link CasinoKings}) ; le son est cutscene/king_<i>roi</i>.
 */
public abstract class KingEntrance extends Cutscene {

    public static final float COVER_START = 3.6f;
    public static final float COVER_FULL  = 4.2f;

    protected final EnemyKind     kind;
    protected final TextureRegion king;
    /** Particules dessinées en lueurs (étincelles) ou en images ({@code region}). */
    protected final Array<Particle> parts = new Array<>(false, 256);
    private float previous;

    protected KingEntrance(CutsceneKit kit, EnemyKind kind) {
        super(kit.settings(), kit.shake(), kit.sound("king_" + kind.name().toLowerCase()));
        this.kind = kind;
        king = kit.portrait(kind);
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
        restart();
    }

    @Override
    protected final void simulate(float delta) {
        step(delta);
        update(parts, delta);
        previous = time;
    }

    @Override
    protected final void drawScene(Batch batch) {
        draw(batch);
        fadeFromBlack(batch, 0.4f);
    }

    /** Remet la scène au début. */
    protected void restart() {}

    /** Fait vivre la scène ; {@link #at} dit quand lancer chaque moment. */
    protected abstract void step(float delta);

    /** Dessine la scène. */
    protected abstract void draw(Batch batch);

    /** @return {@code true} une seule fois : à l'image où le temps passe {@code moment}. */
    protected boolean at(float moment) { return previous < moment && time >= moment; }

    /** Le roi, centré en bas sur ({@code x}, {@code y}) ; {@code shadow} = 1 en ombre noire. */
    protected void drawKing(Batch batch, float x, float y, float scale, float shadow, float alpha) {
        portrait(batch, king, x, y, scale, shadow, alpha);
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

    protected float cx() { return worldWidth() / 2f; }
    /** @return le bas du roi quand il est en place. */
    protected float kingY() { return worldHeight() * 0.18f; }
    /** Agrandissement du portrait du roi en place. */
    protected static final float KING_SCALE = 12f;
}
