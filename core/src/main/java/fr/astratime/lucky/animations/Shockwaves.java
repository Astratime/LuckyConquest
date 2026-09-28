package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Ondes de choc : des anneaux de carrés (rendu pixel art) qui s'élargissent
 * d'un point en s'estompant — l'onde sonore d'une cloche, ou, aplatis, la
 * poussière soulevée par un symbole lourd qui s'écrase sur la table.
 *
 * Les positions sont celles du Stage : l'acteur doit être placé à l'origine.
 */
public class Shockwaves extends Actor {

    private static final int   DOTS     = 56;   // carrés par anneau
    private static final float DOT_SIZE = 7f;

    private static final class Ring {
        float x, y, radius, flatten, age, life;
        Color color;
    }

    private final TextureRegion pixel;
    private final List<Ring>    rings = new ArrayList<>();

    /** @param pixel région d'un pixel blanc, teintée pour chaque anneau */
    public Shockwaves(TextureRegion pixel) {
        this.pixel = pixel;
        setTouchable(Touchable.disabled);
    }

    /**
     * Fait partir de {@code (x, y)} un anneau qui atteint le rayon {@code radius}
     * en {@code life} secondes.
     *
     * @param flatten rapport hauteur / largeur de l'anneau (1 : cercle ; petit : onde au sol)
     */
    public void ring(float x, float y, float radius, float life, float flatten, Color color) {
        Ring ring = new Ring();
        ring.x       = x;
        ring.y       = y;
        ring.radius  = radius;
        ring.life    = life;
        ring.flatten = flatten;
        ring.color   = color;
        rings.add(ring);
    }

    /** Retire tous les anneaux immédiatement. */
    public void removeAll() { rings.clear(); }

    @Override
    public void act(float delta) {
        super.act(delta);
        Iterator<Ring> it = rings.iterator();
        while (it.hasNext()) {
            Ring ring = it.next();
            ring.age += delta;
            if (ring.age >= ring.life) it.remove();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (rings.isEmpty()) return;
        float r = batch.getColor().r, g = batch.getColor().g, b = batch.getColor().b, a = batch.getColor().a;
        for (Ring ring : rings) {
            float t      = ring.age / ring.life;
            float radius = ring.radius * Interpolation.pow2Out.apply(t);
            float size   = DOT_SIZE * (1f - t * 0.6f);
            batch.setColor(ring.color.r, ring.color.g, ring.color.b, (1f - t) * ring.color.a * parentAlpha);
            for (int i = 0; i < DOTS; i++) {
                float angle = MathUtils.PI2 * i / DOTS;
                float x = ring.x + MathUtils.cos(angle) * radius;
                float y = ring.y + MathUtils.sin(angle) * radius * ring.flatten;
                // Positions arrondies : les carrés restent alignés sur la grille des pixels.
                batch.draw(pixel, Math.round(x - size / 2f), Math.round(y - size / 2f), size, size);
            }
        }
        batch.setColor(r, g, b, a);
    }
}
