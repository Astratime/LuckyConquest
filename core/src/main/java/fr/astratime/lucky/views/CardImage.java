package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Carte de la main, vivante comme dans Balatro : au repos elle ondule
 * doucement (chaque carte à son rythme) ; survolée, elle se soulève, grossit un
 * peu, s'incline vers la souris et projette une ombre plus longue.
 *
 * Ces mouvements ne s'appliquent que tant que la carte est dans la main
 * (cliquable) : quand un animateur la prend en charge (défausse), il la rend
 * non cliquable et garde seul la main sur son échelle et sa rotation. La
 * levée et l'ombre sont dessinées sans toucher à la position de la carte, qui
 * reste celle de la rangée.
 */
public class CardImage extends Image {

    private static final float HOVER_SCALE   = 1.1f;
    private static final float HOVER_LIFT    = 18f;   // pixels
    private static final float TILT_MAX      = 8f;    // degrés, souris au bord de la carte
    private static final float SWAY_ANGLE    = 1.6f;  // degrés, au repos
    private static final float SWAY_SPEED    = 1.7f;  // radians par seconde
    private static final float EASE          = 14f;   // rapidité du rattrapage (par seconde)
    private static final float SHADOW_ALPHA  = 0.35f;
    private static final float SHADOW_OFFSET = 4f;    // pixels, carte posée ; plus loin quand elle est levée

    private final float phase = MathUtils.random(MathUtils.PI2);
    private float   time;
    private float   lift;
    private float   pointerTilt;
    private boolean hovered;

    public CardImage(Drawable drawable) {
        super(drawable);
    }

    /** Survol : la carte se soulève et s'incline vers la souris, ou retombe. */
    public void setHovered(boolean hovered) {
        this.hovered = hovered;
    }

    /** Inclinaison selon la position {@code localX} de la souris sur la carte (0 à gauche, largeur à droite). */
    public void tiltToward(float localX) {
        float fromCenter = MathUtils.clamp(localX / getWidth() * 2f - 1f, -1f, 1f);
        pointerTilt = -fromCenter * TILT_MAX;
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        setOrigin(getWidth() / 2f, getHeight() / 2f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!inHand()) return;
        time += delta;
        float ease = Math.min(1f, EASE * delta);
        float targetScale    = hovered ? HOVER_SCALE : 1f;
        float targetRotation = hovered ? pointerTilt : MathUtils.sin(time * SWAY_SPEED + phase) * SWAY_ANGLE;
        lift += ((hovered ? HOVER_LIFT : 0f) - lift) * ease;
        setScale(getScaleX() + (targetScale - getScaleX()) * ease);
        setRotation(getRotation() + (targetRotation - getRotation()) * ease);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!inHand()) {
            super.draw(batch, parentAlpha);
            return;
        }
        float x = getX(), y = getY();
        Color color = getColor();
        float r = color.r, g = color.g, b = color.b, a = color.a;

        // Ombre : la carte, noire et transparente, décalée d'autant plus qu'elle est levée.
        float shadow = SHADOW_OFFSET + lift * 0.5f;
        setPosition(x + shadow, y - shadow);
        color.set(0f, 0f, 0f, SHADOW_ALPHA * a);
        super.draw(batch, parentAlpha);

        color.set(r, g, b, a);
        setPosition(x, y + lift);
        super.draw(batch, parentAlpha);
        setPosition(x, y);
    }

    private boolean inHand() {
        return getTouchable() == Touchable.enabled && getParent() != null;
    }
}
