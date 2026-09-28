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
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

/**
 * Bingo du Triple Cerise : une cible de tir à l'arc surgit au milieu de la
 * table, une flèche traverse l'écran et se plante en plein centre (la cible
 * encaisse le choc, la flèche vibre). Tout s'estompe à la fin de la
 * célébration ; le mot « BINGO! » géant est écrit par {@link GiantWord}.
 *
 * La cible et la flèche sont dessinées en pixel art (petites images agrandies
 * sans lissage). Les positions sont celles du Stage : l'acteur doit être placé
 * à l'origine.
 */
public class BullseyeAnimation extends Group implements Disposable {

    /** Instant où la flèche se plante au centre de la cible, depuis {@link #play}. */
    public static final float IMPACT_TIME = 0.95f;

    private static final float TARGET_POP    = 0.3f;
    private static final float ARROW_FLIGHT  = 0.3f;
    private static final float ARROW_LAUNCH  = IMPACT_TIME - ARROW_FLIGHT;
    private static final float FADE_AT       = 2.45f;
    private static final float FADE_TIME     = 0.45f;

    private static final int   TARGET_PIXELS = 67;    // diamètre de la cible dans son image
    private static final float TARGET_SCALE  = 5f;    // agrandissement entier : pixels nets
    private static final int   ARROW_LENGTH  = 44;
    private static final int   ARROW_HEAD    = 7;     // longueur de la pointe, cachée une fois plantée
    private static final float ARROW_SCALE   = 5f;
    /** Départ de la flèche, relatif au centre de la cible : hors de l'écran, en haut à gauche. */
    private static final float ARROW_FROM_X  = -1100f;
    private static final float ARROW_FROM_Y  = 420f;


    private final Texture    targetTexture = createTarget();
    private final Texture    arrowTexture  = createArrow();

    private final Image flyingArrow;
    private final Image plantedArrow;
    private final Image target;

    private final Vector2 center = new Vector2();

    public BullseyeAnimation() {
        setTouchable(Touchable.disabled);
        setVisible(false);

        target = new Image(new TextureRegionDrawable(new TextureRegion(targetTexture)));
        target.setSize(TARGET_PIXELS * TARGET_SCALE, TARGET_PIXELS * TARGET_SCALE);
        target.setOrigin(target.getWidth() / 2f, target.getHeight() / 2f);

        // Flèche en vol (avec sa pointe), puis plantée (sans la pointe, enfoncée dans la cible).
        TextureRegion full    = new TextureRegion(arrowTexture);
        TextureRegion planted = new TextureRegion(arrowTexture, 0, 0, ARROW_LENGTH - ARROW_HEAD,
            arrowTexture.getHeight());
        flyingArrow  = arrowImage(full);
        plantedArrow = arrowImage(planted);

        addActor(target);
        addActor(plantedArrow);
        addActor(flyingArrow);
    }

    /** Image d'une flèche dont l'origine est son extrémité droite (la pointe, ou le point d'impact). */
    private static Image arrowImage(TextureRegion region) {
        Image image = new Image(new TextureRegionDrawable(region));
        image.setSize(region.getRegionWidth() * ARROW_SCALE, region.getRegionHeight() * ARROW_SCALE);
        image.setOrigin(image.getWidth(), image.getHeight() / 2f);
        return image;
    }

    /** Lance l'animation, la cible centrée en {@code (centerX, centerY)}. */
    public void play(float centerX, float centerY) {
        clearAll();
        center.set(centerX, centerY);
        setVisible(true);
        getColor().a = 1f;

        target.setPosition(centerX - target.getWidth() / 2f, centerY - target.getHeight() / 2f);
        target.setScale(0f);
        target.setRotation(0f);
        SequenceAction impactShake = Actions.sequence();
        for (int i = 0; i < 6; i++) {
            float strength = 14f * (1f - i / 6f);
            impactShake.addAction(Actions.moveTo(target.getX() + MathUtils.random(-strength, strength),
                target.getY() + MathUtils.random(-strength, strength), 0.035f));
        }
        impactShake.addAction(Actions.moveTo(target.getX(), target.getY(), 0.035f));
        target.addAction(Actions.sequence(
            Actions.scaleTo(1.15f, 1.15f, TARGET_POP * 0.7f, Interpolation.pow2Out),
            Actions.scaleTo(1f, 1f, TARGET_POP * 0.3f, Interpolation.pow2In),
            Actions.delay(IMPACT_TIME - TARGET_POP),
            // Le choc : la cible recule d'un coup puis revient en tremblant.
            Actions.parallel(
                Actions.sequence(Actions.scaleTo(0.9f, 0.9f, 0.05f), Actions.scaleTo(1f, 1f, 0.35f, Interpolation.elasticOut)),
                impactShake)));

        float angle = MathUtils.atan2(-ARROW_FROM_Y, -ARROW_FROM_X) * MathUtils.radiansToDegrees;
        placeArrow(flyingArrow, centerX + ARROW_FROM_X, centerY + ARROW_FROM_Y, angle);
        flyingArrow.setVisible(false);
        flyingArrow.addAction(Actions.sequence(
            Actions.delay(ARROW_LAUNCH),
            Actions.visible(true),
            Actions.moveTo(centerX - flyingArrow.getOriginX(), centerY - flyingArrow.getOriginY(), ARROW_FLIGHT),
            Actions.visible(false)));

        placeArrow(plantedArrow, centerX, centerY, angle);
        plantedArrow.setVisible(false);
        SequenceAction quiver = Actions.sequence();
        for (int i = 0; i < 8; i++) {
            float swing = 7f * (1f - i / 8f) * (i % 2 == 0 ? 1f : -1f);
            quiver.addAction(Actions.rotateTo(angle + swing, 0.045f));
        }
        quiver.addAction(Actions.rotateTo(angle, 0.045f));
        plantedArrow.addAction(Actions.sequence(Actions.delay(IMPACT_TIME), Actions.visible(true), quiver));

        addAction(Actions.sequence(Actions.delay(FADE_AT), Actions.fadeOut(FADE_TIME), Actions.visible(false)));
    }

    /** Place la flèche {@code arrow} pour que son extrémité droite soit en {@code (x, y)}, tournée de {@code angle}. */
    private static void placeArrow(Image arrow, float x, float y, float angle) {
        arrow.clearActions();
        arrow.setPosition(x - arrow.getOriginX(), y - arrow.getOriginY());
        arrow.setRotation(angle);
    }

    /**
     * @param time temps écoulé depuis {@link #play} pendant le vol de la flèche
     * @return la position (Stage) de la pointe de la flèche à cet instant
     */
    public Vector2 arrowTipAt(float time, Vector2 out) {
        float t = MathUtils.clamp((time - ARROW_LAUNCH) / ARROW_FLIGHT, 0f, 1f);
        return out.set(center.x + ARROW_FROM_X * (1f - t), center.y + ARROW_FROM_Y * (1f - t));
    }

    /** @return l'instant où la flèche part (voir {@link #arrowTipAt}). */
    public static float arrowLaunchTime() { return ARROW_LAUNCH; }

    /** Cache tout immédiatement. */
    public void hide() {
        clearAll();
        setVisible(false);
    }

    private void clearAll() {
        clearActions();
        target.clearActions();
        flyingArrow.clearActions();
        plantedArrow.clearActions();
    }

    /**
     * @return la cible de tir à l'arc : anneaux blanc, noir, bleu, rouge et or
     *         cernés de liserés sombres, chacun éclairé en haut à gauche.
     */
    private static Texture createTarget() {
        int size = TARGET_PIXELS;
        int c = size / 2;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        Color outline = Color.valueOf("1a0f0fff");
        String[][] rings = {   // couleur, reflet — de l'extérieur vers le centre
            {"f2ede2ff", "ffffffff"},
            {"2a2a33ff", "4a4a58ff"},
            {"2f7fe0ff", "6fb2ffff"},
            {"e0303cff", "ff6a72ff"},
            {"ffd23cff", "fff0a0ff"},
        };
        int radius = c;
        int step   = 6;
        for (String[] ring : rings) {
            pixmap.setColor(outline);
            pixmap.fillCircle(c, c, radius);
            pixmap.setColor(Color.valueOf(ring[1]));
            pixmap.fillCircle(c, c, radius - 1);
            pixmap.setColor(Color.valueOf(ring[0]));
            pixmap.fillCircle(c + 1, c + 1, radius - 2); // décalé : un reflet reste en haut à gauche
            radius -= step;
        }
        // Mouche : le point central, au cœur de l'or.
        pixmap.setColor(Color.valueOf("c8962aff"));
        pixmap.fillCircle(c, c, 2);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /** @return la flèche, pointe à droite : empennage rouge, fût en bois, pointe d'acier. */
    private static Texture createArrow() {
        int height = 9, mid = 4;
        Pixmap pixmap = new Pixmap(ARROW_LENGTH, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        Color outline = Color.valueOf("1a0f0fff");
        Color wood    = Color.valueOf("b07a3cff");
        Color woodLit = Color.valueOf("dca466ff");
        Color feather = Color.valueOf("e0303cff");
        Color featherLit = Color.valueOf("ff8a90ff");
        Color steel   = Color.valueOf("c8d2dcff");
        Color steelDark = Color.valueOf("6a7684ff");

        int headStart = ARROW_LENGTH - ARROW_HEAD;
        // Fût, cerné de sombre.
        pixmap.setColor(outline);
        pixmap.fillRectangle(2, mid - 1, headStart - 2, 3);
        pixmap.setColor(wood);
        pixmap.drawLine(2, mid, headStart - 1, mid);
        pixmap.setColor(woodLit);
        pixmap.drawLine(4, mid, headStart - 4, mid);
        // Empennage : deux plumes en biais au talon.
        for (int i = 0; i < 9; i++) {
            int spread = Math.min(4, 1 + i / 2);
            pixmap.setColor(outline);
            pixmap.drawPixel(i, mid - spread - 1);
            pixmap.drawPixel(i, mid + spread + 1);
            pixmap.setColor(i % 3 == 0 ? featherLit : feather);
            pixmap.drawLine(i, mid - spread, i, mid - 1);
            pixmap.drawLine(i, mid + 1, i, mid + spread);
        }
        // Pointe triangulaire.
        for (int i = 0; i < ARROW_HEAD; i++) {
            int half = Math.max(0, 3 - (i * 4) / ARROW_HEAD);
            int x = headStart + i;
            pixmap.setColor(outline);
            pixmap.drawLine(x, mid - half - 1, x, mid + half + 1);
            if (half > 0 || i < ARROW_HEAD - 1) {
                pixmap.setColor(steelDark);
                pixmap.drawLine(x, mid, x, mid + half);
                pixmap.setColor(steel);
                pixmap.drawLine(x, mid - half, x, mid);
            }
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        targetTexture.dispose();
        arrowTexture.dispose();
    }
}
