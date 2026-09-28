package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import fr.astratime.lucky.assets.Palette;

/**
 * Bingo du Diamant : la taille. Une pierre brute tourne lentement sur
 * elle-même ; un burin la frappe d'un coup sec et elle se révèle taillée et
 * étincelante, pendant qu'un faisceau de lumière balaie l'écran comme celui
 * d'un phare (l'éclat et les mini-diamants sont lancés par
 * {@link JackpotCelebration}).
 */
public class GemCutScene extends BingoScene {

    /** Le burin frappe : la pierre brute devient un diamant taillé. */
    public static final float CUT_TIME = 1.0f;

    private static final float SPIN_SPEED    = 4.5f;   // rotation apparente de la pierre brute
    private static final float GEM_SCALE     = 1.9f;   // image d'un symbole (144 px) : ~270 px
    private static final float BEAM_SPEED    = 150f;   // degrés par seconde
    private static final float BEAM_LENGTH   = 1100f;
    private static final float BEAM_WIDTH    = 150f;
    private static final float CHISEL_REACH  = 70f;    // la pointe frappe à cette distance du centre

    private static final int STONE_WIDTH  = 36;
    private static final int STONE_HEIGHT = 30;
    private static final int CHISEL_WIDTH = 8;
    private static final int CHISEL_HEIGHT = 40;

    private final Image stone, gem, chisel;
    private final Beam  beam;
    private final Vector2 center = new Vector2();

    /** @param diamond image (détourée) du diamant taillé */
    public GemCutScene(TextureRegion diamond) {
        stone  = image(texture(stone()));
        stone.setOrigin(stone.getWidth() / 2f, stone.getHeight() / 2f);
        gem    = image(diamond, GEM_SCALE);
        gem.setOrigin(gem.getWidth() / 2f, gem.getHeight() / 2f);
        chisel = image(texture(chisel()));
        chisel.setOrigin(chisel.getWidth() / 2f, 0f); // la pointe
        beam   = new Beam(new TextureRegion(texture(beamCone())));
        addActor(beam);
        addActor(stone);
        addActor(gem);
        addActor(chisel);
    }

    /** @return le centre (Stage) de la pierre, puis du diamant. */
    public Vector2 center() { return center; }

    @Override
    protected void start(float x, float y) {
        center.set(x, y);
        placeOrigin(stone, x, y);
        stone.setVisible(true);
        stone.setScale(0f, 1f);
        stone.getColor().a = 1f;

        placeOrigin(gem, x, y);
        gem.setVisible(false);
        gem.setScale(0.6f);
        gem.addAction(Actions.sequence(
            Actions.delay(CUT_TIME),
            Actions.visible(true),
            Actions.scaleTo(1.18f, 1.18f, 0.12f, Interpolation.pow2Out),
            Actions.scaleTo(1f, 1f, 0.2f, Interpolation.pow2In),
            Actions.forever(Actions.sequence(
                Actions.moveBy(0f, 10f, 0.4f, Interpolation.sine),
                Actions.moveBy(0f, -10f, 0.4f, Interpolation.sine)))));

        // Le burin arrive d'en haut à droite, recule pour prendre son élan, frappe et repart.
        float diagonal = 0.7071f;
        float hitX = x + CHISEL_REACH * diagonal, hitY = y + CHISEL_REACH * diagonal;
        chisel.setRotation(-45f); // pointe vers le bas à gauche
        placeOrigin(chisel, hitX + 260f * diagonal, hitY + 260f * diagonal);
        chisel.getColor().a = 0f;
        chisel.addAction(Actions.sequence(
            Actions.delay(0.3f),
            Actions.parallel(Actions.fadeIn(0.15f),
                Actions.moveBy(-110f * diagonal, -110f * diagonal, 0.3f, Interpolation.pow2Out)),
            Actions.moveBy(60f * diagonal, 60f * diagonal, CUT_TIME - 0.08f - 0.6f, Interpolation.pow2Out),
            Actions.moveTo(hitX - chisel.getOriginX(), hitY - chisel.getOriginY(), 0.08f, Interpolation.pow3In),
            Actions.parallel(
                Actions.moveBy(120f * diagonal, 120f * diagonal, 0.3f, Interpolation.pow2Out),
                Actions.sequence(Actions.delay(0.1f), Actions.fadeOut(0.25f)))));

        beam.setVisible(false);
        beam.getColor().a = 0f;
        beam.addAction(Actions.sequence(
            Actions.delay(CUT_TIME),
            Actions.visible(true),
            Actions.alpha(1f, 0.15f)));
    }

    @Override
    protected void update(float delta) {
        if (time < CUT_TIME) {
            // Rotation apparente : la pierre s'aplatit et se retourne.
            stone.setScaleX(MathUtils.cos(time * SPIN_SPEED) * Math.min(1f, time / 0.2f));
        } else if (stone.isVisible()) {
            stone.setVisible(false);
        }
        beam.angle = (time - CUT_TIME) * BEAM_SPEED + 20f;
        beam.setPosition(center.x, center.y);
    }

    /** Faisceau de phare : deux cônes de lumière opposés, en mélange additif, qui tournent. */
    private static final class Beam extends Actor {
        private final TextureRegion cone;
        private float angle;

        Beam(TextureRegion cone) { this.cone = cone; }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            Color previous = batch.getColor().cpy();
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            batch.setColor(0.75f, 0.97f, 1f, 0.55f * getColor().a * parentAlpha);
            for (float offset : new float[] {0f, 180f}) {
                batch.draw(cone, getX(), getY() - BEAM_WIDTH / 2f, 0f, BEAM_WIDTH / 2f,
                    BEAM_LENGTH, BEAM_WIDTH, 1f, 1f, angle + offset);
            }
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            batch.setColor(previous);
        }
    }

    /** @return un cône de lumière qui s'élargit et s'estompe vers la droite. */
    private static Pixmap beamCone() {
        int length = 64, height = 32;
        Pixmap pixmap = pixmap(length, height);
        for (int x = 0; x < length; x++) {
            float spread = 1.5f + x * (height / 2f - 1.5f) / length;
            float alpha = 1f - (float) x / length;
            for (int y = 0; y < height; y++) {
                float distance = Math.abs(y + 0.5f - height / 2f) / spread;
                if (distance > 1f) continue;
                pixmap.setColor(1f, 1f, 1f, alpha * (1f - distance * distance));
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }

    /** @return la pierre brute : bloc irrégulier gris bleuté à facettes ternes. */
    private static Pixmap stone() {
        Pixmap pixmap = pixmap(STONE_WIDTH, STONE_HEIGHT);
        int[][] points = {{4, 12}, {12, 2}, {26, 1}, {34, 10}, {31, 24}, {17, 29}, {5, 23}};
        String[] facets = {"9aaabb", "7a8a9c", "6a7a8c", "4e5a68", "5a6878", "7a8a9c", "8a9aac"};
        int cx = 18, cy = 14;
        for (int i = 0; i < points.length; i++) {
            int[] a = points[i], b = points[(i + 1) % points.length];
            pixmap.setColor(c(facets[i]));
            pixmap.fillTriangle(cx, cy, a[0], a[1], b[0], b[1]);
        }
        pixmap.setColor(OUTLINE);
        for (int i = 0; i < points.length; i++) {
            int[] a = points[i], b = points[(i + 1) % points.length];
            pixmap.drawLine(a[0], a[1], b[0], b[1]);
        }
        pixmap.setColor(c("3e4a58"));
        pixmap.drawLine(cx, cy, 31, 24);
        pixmap.drawLine(cx, cy, 5, 23);
        pixmap.setColor(c("c8e6f0"));
        pixmap.drawPixel(13, 5);
        pixmap.drawPixel(14, 5);
        pixmap.drawPixel(13, 6);
        return pixmap;
    }

    /** @return le burin, pointe en bas : lame d'acier biseautée, manche en bois. */
    private static Pixmap chisel() {
        Pixmap pixmap = pixmap(CHISEL_WIDTH, CHISEL_HEIGHT);
        int bladeFrom = 22;
        fillOutlined(pixmap, 1, 0, CHISEL_WIDTH - 2, bladeFrom + 1, Palette.WOOD);
        pixmap.setColor(Palette.WOOD_LIGHT);
        pixmap.drawLine(2, 1, 2, bladeFrom - 1);
        hLine(pixmap, 1, CHISEL_WIDTH - 2, 4, Palette.IRON_DARK);
        for (int y = bladeFrom; y < CHISEL_HEIGHT; y++) {
            int half = y > CHISEL_HEIGHT - 6 ? Math.max(1, (CHISEL_HEIGHT - y) / 2) : 3;
            int center = CHISEL_WIDTH / 2;
            hLine(pixmap, center - half, center + half - 1, y, OUTLINE);
            if (half > 1) {
                hLine(pixmap, center - half + 1, center - 1, y, c("e0e8f0"));
                hLine(pixmap, center, center + half - 2, y, Palette.STEEL);
            }
        }
        return pixmap;
    }
}
