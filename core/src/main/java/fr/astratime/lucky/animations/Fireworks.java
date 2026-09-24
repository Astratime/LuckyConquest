package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Feux d'artifice : chaque fusée monte en laissant une traînée d'étincelles,
 * puis explose dans un éclat blanc en une gerbe de particules colorées qui
 * retombent en s'estompant. Les particules sont de petits carrés (rendu pixel
 * art) : un halo en mélange additif les fait briller, et leur cœur est dessiné
 * en couleur pleine pour rester saturé sur le feutre vert.
 *
 * Les positions sont celles du Stage : l'acteur doit être placé à l'origine.
 */
public class Fireworks extends Actor {

    private static final float ROCKET_SPEED   = 900f;
    private static final float TRAIL_INTERVAL = 0.015f;
    private static final int   BURST_SPARKS   = 140;
    private static final int   FLASH_SPARKS   = 10;      // éclat blanc au moment de l'explosion
    private static final float FLASH_SIZE     = 26f;
    private static final float FLASH_LIFE     = 0.22f;
    private static final float GLOW_SCALE     = 2.4f;    // halo additif autour de chaque étincelle
    private static final float GLOW_ALPHA     = 0.35f;
    private static final float SPARK_SPEED_MIN = 120f;
    private static final float SPARK_SPEED_MAX = 440f;
    private static final float SPARK_LIFE_MIN  = 1.2f;
    private static final float SPARK_LIFE_MAX  = 2.0f;
    private static final float GRAVITY         = -160f;
    private static final float DRAG            = 1.6f;   // freinage par seconde (fraction de vitesse)
    private static final float SPARK_SIZE      = 9f;
    private static final float TRAIL_SIZE      = 4f;
    private static final float ROCKET_SIZE     = 7f;

    /** Couleurs des gerbes, tirées au hasard ; le doré revient plus souvent (thème casino). */
    private static final Color[] COLORS = {
        Color.valueOf("ffd54aff"), Color.valueOf("ffd54aff"), Color.valueOf("ff4a5aff"),
        Color.valueOf("4affa0ff"), Color.valueOf("4ac8ffff"), Color.valueOf("c77dffff"), Color.WHITE,
    };

    private static final class Rocket {
        float x, y, apexY, trailTimer;
        Color color;
    }

    private static final class Spark {
        float x, y, vx, vy, life, maxLife, size;
        Color color;
    }

    private final TextureRegion  pixel;
    private final List<Rocket>   rockets = new ArrayList<>();
    private final List<Spark>    sparks  = new ArrayList<>();

    /** @param pixel région d'un pixel blanc, teintée pour chaque particule */
    public Fireworks(TextureRegion pixel) {
        this.pixel = pixel;
    }

    /** Lance une fusée depuis {@code (x, fromY)} ; elle explose à l'ordonnée {@code apexY}. */
    public void launch(float x, float fromY, float apexY) {
        Rocket rocket = new Rocket();
        rocket.x     = x;
        rocket.y     = fromY;
        rocket.apexY = apexY;
        rocket.color = COLORS[MathUtils.random(COLORS.length - 1)];
        rockets.add(rocket);
    }

    /** @return true s'il ne reste ni fusée ni étincelle. */
    public boolean isEmpty() { return rockets.isEmpty() && sparks.isEmpty(); }

    /** Retire toutes les fusées et étincelles immédiatement. */
    public void removeAll() {
        rockets.clear();
        sparks.clear();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        Iterator<Rocket> rocketIt = rockets.iterator();
        while (rocketIt.hasNext()) {
            Rocket rocket = rocketIt.next();
            rocket.y += ROCKET_SPEED * delta;
            rocket.trailTimer += delta;
            while (rocket.trailTimer >= TRAIL_INTERVAL) {
                rocket.trailTimer -= TRAIL_INTERVAL;
                addSpark(rocket.x + MathUtils.random(-2f, 2f), rocket.y, MathUtils.random(-20f, 20f),
                    MathUtils.random(-80f, -20f), MathUtils.random(0.2f, 0.4f), TRAIL_SIZE, Color.GOLD);
            }
            if (rocket.y >= rocket.apexY) {
                explode(rocket);
                rocketIt.remove();
            }
        }

        float drag = Math.max(0f, 1f - DRAG * delta);
        Iterator<Spark> sparkIt = sparks.iterator();
        while (sparkIt.hasNext()) {
            Spark spark = sparkIt.next();
            spark.life -= delta;
            if (spark.life <= 0f) {
                sparkIt.remove();
                continue;
            }
            spark.vx *= drag;
            spark.vy = spark.vy * drag + GRAVITY * delta;
            spark.x += spark.vx * delta;
            spark.y += spark.vy * delta;
        }
    }

    /** Éclat blanc puis gerbe circulaire : la plupart des étincelles sur un anneau, quelques-unes à l'intérieur. */
    private void explode(Rocket rocket) {
        for (int i = 0; i < FLASH_SPARKS; i++) {
            addSpark(rocket.x + MathUtils.random(-12f, 12f), rocket.y + MathUtils.random(-12f, 12f),
                0f, 0f, FLASH_LIFE, FLASH_SIZE, Color.WHITE);
        }
        float ringSpeed = MathUtils.random(SPARK_SPEED_MIN * 1.5f, SPARK_SPEED_MAX);
        for (int i = 0; i < BURST_SPARKS; i++) {
            float angle = MathUtils.random(MathUtils.PI2);
            float speed = MathUtils.randomBoolean(0.7f)
                ? ringSpeed * MathUtils.random(0.9f, 1.05f)
                : MathUtils.random(SPARK_SPEED_MIN, ringSpeed);
            Color color = MathUtils.randomBoolean(0.2f) ? Color.WHITE : rocket.color;
            addSpark(rocket.x, rocket.y, MathUtils.cos(angle) * speed, MathUtils.sin(angle) * speed,
                MathUtils.random(SPARK_LIFE_MIN, SPARK_LIFE_MAX), SPARK_SIZE, color);
        }
    }

    private void addSpark(float x, float y, float vx, float vy, float life, float size, Color color) {
        Spark spark = new Spark();
        spark.x       = x;
        spark.y       = y;
        spark.vx      = vx;
        spark.vy      = vy;
        spark.life    = life;
        spark.maxLife = life;
        spark.size    = size;
        spark.color   = color;
        sparks.add(spark);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (isEmpty()) return;
        Color previous = batch.getColor().cpy();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE); // additif : halo lumineux
        drawSparks(batch, parentAlpha, GLOW_SCALE, GLOW_ALPHA);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        drawSparks(batch, parentAlpha, 1f, 1f);                  // cœur en couleur pleine
        for (Rocket rocket : rockets) {
            batch.setColor(1f, 0.95f, 0.8f, parentAlpha);
            batch.draw(pixel, rocket.x - ROCKET_SIZE / 2f, rocket.y - ROCKET_SIZE / 2f, ROCKET_SIZE, ROCKET_SIZE);
        }
        batch.setColor(previous);
    }

    /** Dessine toutes les étincelles, agrandies de {@code scale} et d'opacité multipliée par {@code alphaScale}. */
    private void drawSparks(Batch batch, float parentAlpha, float scale, float alphaScale) {
        for (Spark spark : sparks) {
            float fade = spark.life / spark.maxLife;
            // Scintillement en fin de vie.
            float alpha = fade < 0.35f && MathUtils.randomBoolean(0.3f) ? 0f : fade;
            batch.setColor(spark.color.r, spark.color.g, spark.color.b, alpha * alphaScale * parentAlpha);
            float size = Math.max(2f, spark.size * (0.5f + fade * 0.5f)) * scale;
            batch.draw(pixel, spark.x - size / 2f, spark.y - size / 2f, size, size);
        }
    }
}
