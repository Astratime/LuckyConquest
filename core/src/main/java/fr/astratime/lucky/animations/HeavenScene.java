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
 * Bingo de l'Apôtre de la Chance : un rayon de lumière dorée tombe du haut de
 * l'écran sur la table, deux grandes ailes blanches se déploient et l'Apôtre
 * descend dans le rayon en battant des ailes ; il se pose à {@link #LAND_TIME}
 * et une auréole s'allume au-dessus de lui (les plumes, les étoiles et les
 * trèfles sont lancés par {@link JackpotCelebration}, qui suit {@link #apostle}).
 */
public class HeavenScene extends BingoScene {

    /** Le rayon de lumière est tombé : l'Apôtre apparaît en haut. */
    public static final float BEAM_TIME = 0.3f;
    /** L'Apôtre se pose. */
    public static final float LAND_TIME = 1.3f;

    private static final float SIZE = 260f;
    private static final float FROM = 560f;   // départ, au-dessus de sa place
    private static final int WING_W = 30, WING_H = 26;

    private final Image beam, leftWing, rightWing, apostle, halo, glow;
    private final Vector2 center = new Vector2(), at = new Vector2();

    /** @param luckApostle image du symbole Apôtre de la Chance */
    public HeavenScene(TextureRegion luckApostle) {
        beam = image(new TextureRegion(texture(beam())), 1f);
        addActor(beam);
        glow = image(texture(halo(56, hex("fff2b0"))));
        glow.setOrigin(glow.getWidth() / 2f, glow.getHeight() / 2f);
        addActor(glow);
        leftWing  = image(texture(wing()));
        leftWing.setOrigin(leftWing.getWidth(), leftWing.getHeight() * 0.4f);
        rightWing = image(texture(wing()));
        rightWing.setOrigin(rightWing.getWidth(), rightWing.getHeight() * 0.4f);
        rightWing.setScaleX(-1f);
        addActor(leftWing);
        addActor(rightWing);
        apostle = image(luckApostle, SIZE / luckApostle.getRegionWidth());
        apostle.setOrigin(apostle.getWidth() / 2f, apostle.getHeight() / 2f);
        addActor(apostle);
        halo = image(texture(aureole()));
        halo.setOrigin(halo.getWidth() / 2f, halo.getHeight() / 2f);
        addActor(halo);
    }

    /** @return la position (Stage) de l'Apôtre. */
    public Vector2 apostle() { return at; }

    @Override
    protected void start(float x, float y) {
        center.set(x, y);
        float height = getStage() != null ? getStage().getHeight() : 1080f;
        beam.setSize(340f, height);
        beam.setOrigin(170f, height);
        beam.setPosition(x - 170f, 0f);
        halo.setVisible(false);
        update(0f);
    }

    @Override
    protected void update(float delta) {
        // Le rayon descend du haut comme un rideau, puis palpite.
        float drop = Interpolation.pow2Out.apply(Math.min(1f, time / BEAM_TIME));
        beam.setScale(1f + 0.08f * MathUtils.sin(time * 7f), drop);
        beam.setColor(1f, 0.95f, 0.75f, 0.55f);

        float descent = time < BEAM_TIME ? 0f : Interpolation.sineOut.apply(Math.min(1f, (time - BEAM_TIME) / (LAND_TIME - BEAM_TIME)));
        boolean visible = time >= BEAM_TIME * 0.6f;
        float bob = time >= LAND_TIME ? MathUtils.sin((time - LAND_TIME) * 5f) * 10f : 0f;
        at.set(center.x, center.y + FROM * (1f - descent) + bob);
        apostle.setVisible(visible);
        leftWing.setVisible(visible);
        rightWing.setVisible(visible);
        apostle.getColor().a = Math.min(1f, (time - BEAM_TIME * 0.6f) / 0.2f);
        placeOrigin(apostle, at.x, at.y);

        // Les ailes s'ouvrent puis battent (plus lentement une fois posé).
        float unfold = Interpolation.swingOut.apply(Math.min(1f, Math.max(0f, time - BEAM_TIME * 0.6f) / 0.4f));
        float flap = MathUtils.sin(time * (time < LAND_TIME ? 16f : 7f)) * 18f;
        leftWing.setScale(unfold, 1f);
        rightWing.setScale(-unfold, 1f);
        leftWing.setRotation(flap);
        rightWing.setRotation(-flap);
        placeOrigin(leftWing, at.x - SIZE * 0.2f, at.y + SIZE * 0.05f);
        placeOrigin(rightWing, at.x + SIZE * 0.2f, at.y + SIZE * 0.05f);

        glow.setScale(4f + 0.4f * MathUtils.sin(time * 8f));
        glow.getColor().a = visible ? 0.7f : 0f;
        placeOrigin(glow, at.x, at.y);

        if (time < LAND_TIME) return;
        float since = time - LAND_TIME;
        halo.setVisible(true);
        halo.setScale(Interpolation.swingOut.apply(Math.min(1f, since / 0.3f)));
        halo.setColor(1f, 1f, 0.8f, 0.8f + 0.2f * MathUtils.sin(since * 10f));
        placeOrigin(halo, at.x, at.y + SIZE * 0.62f);
    }

    /** @return le rayon : une colonne dorée, plus dense au centre (étirée à la hauteur de l'écran). */
    private static Pixmap beam() {
        Pixmap pixmap = pixmap(34, 1);
        for (int x = 0; x < 34; x++) {
            float u = Math.abs(x + 0.5f - 17f) / 17f;
            pixmap.setColor(1f, 1f, 1f, (1f - u * u) * 0.9f);
            pixmap.drawPixel(x, 0);
        }
        return pixmap;
    }

    /** @return une aile gauche : plumes blanches en éventail, ombrées de bleu pâle. */
    private static Pixmap wing() {
        Pixmap pixmap = pixmap(WING_W, WING_H);
        Color white = hex("ffffff"), shade = hex("d4e2f4"), gold = hex("f8d870");
        Shape shape = (x, y) -> {
            // Bord supérieur arrondi, bas en dents de plumes.
            float u = (WING_W - x - 0.5f) / WING_W;           // 0 près du corps, 1 au bout
            float top = 4f + u * u * 8f;
            float feather = 6f + (1f - u) * 14f + ((x / 4) % 2 == 0 ? 2f : 0f);
            return y >= top && y <= top + feather && x >= 1;
        };
        fillShape(pixmap, shape, (x, y) -> x > WING_W - 4 ? gold : (x / 4) % 2 == 0 ? white : shade);
        return pixmap;
    }

    /** @return l'auréole : un anneau d'or vu de biais. */
    private static Pixmap aureole() {
        Pixmap pixmap = pixmap(30, 9);
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 30; x++) {
                float u = (x + 0.5f - 15f) / 15f, v = (y + 0.5f - 4.5f) / 4.5f;
                float d = u * u + v * v;
                if (d > 1f || d < 0.45f) continue;
                pixmap.setColor(d > 0.8f ? hex("c89a20") : hex("ffe070"));
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }
}
