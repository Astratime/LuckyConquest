package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Bingo du Dé : trois dés géants (ceux du Sept, voir {@link FlamingDieScene})
 * roulent sur le tapis depuis la gauche, l'un derrière l'autre, en rebondissant
 * et en tournoyant, leurs faces défilant ; ils s'arrêtent côte à côte sur
 * 6-6-6 et rougeoient (la gerbe de flammes est lancée par
 * {@link JackpotCelebration}).
 */
public class TripleDiceScene extends BingoScene {

    /** Instants où chaque dé s'immobilise, de droite à gauche (le premier lancé va le plus loin). */
    public static final float[] LANDINGS = {0.95f, 1.1f, 1.25f};
    /** Les trois dés rougeoient et la gerbe de flammes jaillit. */
    public static final float FLAME_TIME = 1.35f;

    private static final float DIE_SCALE = 6f;
    private static final float SPACING   = 205f;
    private static final float ROLL_FROM = -900f;    // départ, relatif à la place finale de chaque dé
    private static final float ROLL_TIME = 0.95f;
    /** Contacts avec la table (fractions de la roulade) et hauteur du saut avant chacun. */
    private static final float[] BOUNCES = {0.3f, 0.6f, 0.82f, 1f};
    private static final float[] HEIGHTS = {200f, 120f, 55f, 20f};
    private static final float FACE_CHANGE = 0.07f;
    private static final int   FINAL_FACE  = 6;

    private final TextureRegion[] faces = new TextureRegion[7];
    private final Image[] dice  = new Image[3];
    private final Image[] glows = new Image[3];
    private final float[] homeX = new float[3];
    private final float[] faceTimers = new float[3];
    private final boolean[] landed = new boolean[3];
    private float centerY;

    public TripleDiceScene() {
        for (int value = 1; value <= 6; value++) faces[value] = new TextureRegion(texture(FlamingDieScene.face(value)));
        TextureRegion glow = new TextureRegion(texture(halo(FlamingDieScene.SIZE + 14, c("ff5a1f"))));
        for (int i = 0; i < dice.length; i++) {
            glows[i] = image(glow, DIE_SCALE);
            glows[i].setOrigin(glows[i].getWidth() / 2f, glows[i].getHeight() / 2f);
            addActor(glows[i]);
        }
        for (int i = 0; i < dice.length; i++) {
            dice[i] = image(faces[1], DIE_SCALE);
            dice[i].setOrigin(dice[i].getWidth() / 2f, dice[i].getHeight() / 2f);
            addActor(dice[i]);
        }
    }

    /** @return l'abscisse (Stage) du dé {@code index} posé (0 : gauche, 1 : milieu, 2 : droite). */
    public float dieX(int index) { return homeX[index]; }

    /** @return la position (Stage) du haut des dés posés, d'où partent les flammes. */
    public float topY() { return centerY + halfSize(); }

    /** @return la moitié du côté d'un dé affiché. */
    public float halfSize() { return FlamingDieScene.SIZE * DIE_SCALE / 2f; }

    /** Fait rouler les dés ; ils s'arrêtent alignés, centrés en {@code (x, y)}. */
    @Override
    protected void start(float x, float y) {
        centerY = y;
        for (int i = 0; i < dice.length; i++) {
            homeX[i] = x + (i - 1) * SPACING;
            landed[i] = false;
            faceTimers[i] = MathUtils.random(FACE_CHANGE);
            dice[i].setColor(Color.WHITE);
            dice[i].setScale(1f);
            dice[i].setVisible(false);
            setFace(i, MathUtils.random(1, 5));
            glows[i].getColor().a = 0f;
            placeOrigin(glows[i], homeX[i], y);
            glows[i].addAction(Actions.sequence(
                Actions.delay(FLAME_TIME),
                Actions.alpha(0.9f, 0.12f),
                Actions.forever(Actions.sequence(Actions.alpha(0.5f, 0.18f), Actions.alpha(0.9f, 0.18f)))));
        }
        update(0f);
    }

    @Override
    protected void update(float delta) {
        for (int i = 0; i < dice.length; i++) {
            // Le dé de droite part le premier : il va le plus loin.
            float land = LANDINGS[dice.length - 1 - i];
            float from = land - ROLL_TIME;
            if (time < from) continue;
            if (time >= land) {
                if (!landed[i]) land(i);
                continue;
            }
            Image die = dice[i];
            die.setVisible(true);
            float progress = (time - from) / ROLL_TIME;
            float x = homeX[i] + ROLL_FROM * (1f - Interpolation.pow2Out.apply(progress));
            float hop = 0f, previous = 0f;
            for (int b = 0; b < BOUNCES.length; b++) {
                if (progress < BOUNCES[b]) {
                    float t = (progress - previous) / (BOUNCES[b] - previous);
                    hop = HEIGHTS[b] * 4f * t * (1f - t);
                    break;
                }
                previous = BOUNCES[b];
            }
            placeOrigin(die, x, centerY + hop);
            die.setRotation(-1080f * Interpolation.pow2Out.apply(progress));
            faceTimers[i] += delta;
            if (faceTimers[i] >= FACE_CHANGE) {
                faceTimers[i] = 0f;
                setFace(i, MathUtils.random(1, 5));
            }
        }
    }

    /** Le dé {@code index} se pose sur le 6 : petit écrasement, puis il rougeoit avec les autres. */
    private void land(int index) {
        landed[index] = true;
        Image die = dice[index];
        die.setVisible(true);
        placeOrigin(die, homeX[index], centerY);
        die.setRotation(0f);
        setFace(index, FINAL_FACE);
        float glowAt = Math.max(0f, FLAME_TIME - time - 0.31f);
        die.addAction(Actions.sequence(
            Actions.scaleTo(1.18f, 0.82f, 0.06f),
            Actions.scaleTo(1f, 1f, 0.25f, Interpolation.elasticOut),
            Actions.delay(glowAt),
            Actions.forever(Actions.sequence(
                Actions.color(c("ffb090"), 0.18f),
                Actions.color(Color.WHITE, 0.18f)))));
    }

    private void setFace(int index, int value) {
        ((TextureRegionDrawable) dice[index].getDrawable()).setRegion(faces[value]);
    }
}
