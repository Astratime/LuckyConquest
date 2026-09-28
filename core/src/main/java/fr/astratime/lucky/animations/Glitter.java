package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Petites lumières des célébrations du Bingo, en pixel art : des scintillements
 * (étoiles à quatre branches qui s'ouvrent puis se referment sur place) et des
 * braises (carrés lumineux qui s'envolent en ondulant, ou retombent comme des
 * gouttes, et vacillent en s'éteignant). Un halo en mélange additif les fait
 * briller.
 *
 * Les positions sont celles du Stage : l'acteur doit être placé à l'origine.
 */
public class Glitter extends Actor {

    private static final float GLOW_SCALE = 2.2f;
    private static final float GLOW_ALPHA = 0.35f;
    private static final float WOBBLE     = 30f;   // ondulation latérale des braises

    private static final class Particle {
        boolean star;
        float   x, y, vx, vy, gravity, size, life, maxLife, phase;
        Color   color;
    }

    private final TextureRegion  pixel;
    private final List<Particle> particles = new ArrayList<>();
    private float                time;

    /** @param pixel région d'un pixel blanc, teintée pour chaque lumière */
    public Glitter(TextureRegion pixel) {
        this.pixel = pixel;
        setTouchable(Touchable.disabled);
    }

    /** Fait scintiller une étoile de taille {@code size} en {@code (x, y)} pendant {@code life} secondes. */
    public void twinkle(float x, float y, float size, float life, Color color) {
        Particle particle = add(x, y, size, life, color);
        particle.star = true;
    }

    /**
     * Lâche une braise en {@code (x, y)} à la vitesse {@code (vx, vy)} ; une
     * {@code gravity} positive la fait retomber, nulle elle file droit.
     */
    public void ember(float x, float y, float vx, float vy, float gravity, float size, float life, Color color) {
        Particle particle = add(x, y, size, life, color);
        particle.vx      = vx;
        particle.vy      = vy;
        particle.gravity = gravity;
    }

    private Particle add(float x, float y, float size, float life, Color color) {
        Particle particle = new Particle();
        particle.x       = x;
        particle.y       = y;
        particle.size    = size;
        particle.life    = life;
        particle.maxLife = life;
        particle.phase   = MathUtils.random(MathUtils.PI2);
        particle.color   = color;
        particles.add(particle);
        return particle;
    }

    /** Retire toutes les lumières immédiatement. */
    public void removeAll() { particles.clear(); }

    @Override
    public void act(float delta) {
        super.act(delta);
        time += delta;
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle particle = it.next();
            particle.life -= delta;
            if (particle.life <= 0f) {
                it.remove();
                continue;
            }
            if (particle.star) continue;
            particle.vy -= particle.gravity * delta;
            particle.x  += (particle.vx + MathUtils.sin(time * 4f + particle.phase) * WOBBLE) * delta;
            particle.y  += particle.vy * delta;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (particles.isEmpty()) return;
        Color previous = batch.getColor().cpy();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE); // additif : halo lumineux
        drawAll(batch, parentAlpha, GLOW_SCALE, GLOW_ALPHA);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        drawAll(batch, parentAlpha, 1f, 1f);
        batch.setColor(previous);
    }

    private void drawAll(Batch batch, float parentAlpha, float scale, float alphaScale) {
        for (Particle particle : particles) {
            float t = 1f - particle.life / particle.maxLife; // 0 à la naissance, 1 à la fin
            if (particle.star) {
                // L'étoile s'ouvre puis se referme ; ses branches restent fines, son cœur blanc.
                float open   = MathUtils.sin(t * MathUtils.PI);
                float length = particle.size * open * scale;
                float thick  = Math.max(2f, particle.size * 0.18f) * scale;
                batch.setColor(particle.color.r, particle.color.g, particle.color.b, open * alphaScale * parentAlpha);
                batch.draw(pixel, particle.x - length / 2f, particle.y - thick / 2f, length, thick);
                batch.draw(pixel, particle.x - thick / 2f, particle.y - length / 2f, thick, length);
                float core = thick * 1.6f;
                batch.setColor(1f, 1f, 1f, open * alphaScale * parentAlpha);
                batch.draw(pixel, particle.x - core / 2f, particle.y - core / 2f, core, core);
            } else {
                float fade  = 1f - t;
                // Vacille en fin de vie, comme les étincelles des feux d'artifice.
                float alpha = fade < 0.35f && MathUtils.randomBoolean(0.3f) ? 0f : fade;
                float size  = Math.max(2f, particle.size * (0.5f + fade * 0.5f)) * scale;
                batch.setColor(particle.color.r, particle.color.g, particle.color.b, alpha * alphaScale * parentAlpha);
                batch.draw(pixel, particle.x - size / 2f, particle.y - size / 2f, size, size);
            }
        }
    }
}
