package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Contour arc-en-ciel animé : le bord de l'acteur est fait de petits carrés
 * dont la teinte dépend de leur place sur le contour, et les couleurs défilent
 * en boucle autour du cadre. Un halo plus large et transparent l'entoure.
 */
public class RainbowBorder extends Actor {

    private static final float THICKNESS   = 6f;     // un « pixel » de la grille x3, doublé
    private static final float GLOW        = 6f;     // halo autour du contour
    private static final float SPEED       = 0.6f;   // tours de roue chromatique par seconde
    private static final float SATURATION  = 0.85f;

    private final TextureRegion pixel;
    private final Color         color = new Color();
    private float               time;

    /** @param pixel région d'un pixel blanc, teintée pour chaque carré */
    public RainbowBorder(TextureRegion pixel) {
        this.pixel = pixel;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        time += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float r = batch.getColor().r, g = batch.getColor().g, b = batch.getColor().b, a = batch.getColor().a;
        float alpha = getColor().a * parentAlpha;
        drawRing(batch, -GLOW, THICKNESS + GLOW, 0.35f * alpha);
        drawRing(batch, 0f, THICKNESS, alpha);
        batch.setColor(r, g, b, a);
    }

    /**
     * Trace le contour en carrés de côté {@code size}, décalé de {@code inset}
     * vers l'intérieur (négatif : vers l'extérieur), dans le sens des aiguilles
     * d'une montre à partir du coin haut gauche.
     */
    private void drawRing(Batch batch, float inset, float size, float alpha) {
        float left = getX() + inset, bottom = getY() + inset;
        float w = getWidth() - inset * 2, h = getHeight() - inset * 2;
        float right = left + w - size, top = bottom + h - size;
        float perimeter = 2 * (w + h);
        for (float x = left; x < right; x += size)   drawSquare(batch, x, top, size, x - left, perimeter, alpha);
        for (float y = top; y > bottom; y -= size)   drawSquare(batch, right, y, size, w + (top - y), perimeter, alpha);
        for (float x = right; x > left; x -= size)   drawSquare(batch, x, bottom, size, w + h + (right - x), perimeter, alpha);
        for (float y = bottom; y < top; y += size)   drawSquare(batch, left, y, size, 2 * w + h + (y - bottom), perimeter, alpha);
    }

    /** Dessine un carré dont la teinte dépend de sa distance {@code along} sur le contour et du temps. */
    private void drawSquare(Batch batch, float x, float y, float size, float along, float perimeter, float alpha) {
        float hue = ((along / perimeter - time * SPEED) % 1f + 1f) % 1f;
        color.fromHsv(hue * 360f, SATURATION, 1f);
        batch.setColor(color.r, color.g, color.b, alpha);
        batch.draw(pixel, x, y, size, size);
    }
}
