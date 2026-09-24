package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Contour lumineux d'une couleur qui pulse : un bord net et un halo plus large
 * et transparent, dont l'intensité oscille. Sert à attirer l'œil sur un
 * rouleau (suspense, paire).
 */
public class GlowBorder extends Actor {

    private static final float THICKNESS = 6f;
    private static final float GLOW      = 8f;

    private final TextureRegion pixel;
    private final Color         glowColor = new Color();
    private float               pulseSpeed = 6f;   // radians par seconde
    private float               time;

    /** @param pixel région d'un pixel blanc, teintée de la couleur du contour */
    public GlowBorder(TextureRegion pixel, Color color) {
        this.pixel = pixel;
        glowColor.set(color);
        setVisible(false);
    }

    /** Change la couleur et la vitesse de pulsation (en radians par seconde). */
    public void setGlow(Color color, float pulseSpeed) {
        glowColor.set(color);
        this.pulseSpeed = pulseSpeed;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        time += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float r = batch.getColor().r, g = batch.getColor().g, b = batch.getColor().b, a = batch.getColor().a;
        float pulse = 0.55f + 0.45f * MathUtils.sin(time * pulseSpeed);
        float alpha = getColor().a * parentAlpha;
        batch.setColor(glowColor.r, glowColor.g, glowColor.b, 0.3f * pulse * alpha);
        drawFrame(batch, -GLOW, THICKNESS + GLOW);
        batch.setColor(glowColor.r, glowColor.g, glowColor.b, (0.5f + 0.5f * pulse) * alpha);
        drawFrame(batch, 0f, THICKNESS);
        batch.setColor(r, g, b, a);
    }

    /** Cadre de quatre rectangles d'épaisseur {@code size}, décalé de {@code inset} vers l'intérieur. */
    private void drawFrame(Batch batch, float inset, float size) {
        float x = getX() + inset, y = getY() + inset;
        float w = getWidth() - inset * 2, h = getHeight() - inset * 2;
        batch.draw(pixel, x, y + h - size, w, size);
        batch.draw(pixel, x, y, w, size);
        batch.draw(pixel, x, y + size, size, h - size * 2);
        batch.draw(pixel, x + w - size, y + size, size, h - size * 2);
    }
}
