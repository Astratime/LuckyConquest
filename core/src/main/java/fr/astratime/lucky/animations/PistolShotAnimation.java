package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;

/**
 * Tir du pistolet de la Roulette russe : le revolver surgit au-dessus du
 * symbole visé, pivote vers la barre de vie de l'ennemi, puis tire — recul,
 * éclair au bout du canon, traçante jusqu'à la cible et impact — avant de
 * disparaître.
 */
public class PistolShotAnimation extends Actor {

    /** Temps entre le début de l'animation et le coup de feu. */
    public static final float AIM_TIME   = 0.8f;

    private static final float APPEAR_TIME = 0.25f;
    private static final float TURN_TIME   = 0.35f;
    private static final float TRACER_TIME = 0.18f;
    private static final float FLASH_TIME  = 0.12f;
    private static final float RECOIL_TIME = 0.25f;
    private static final float FADE_TIME   = 0.45f;
    private static final float HOLD_TIME   = 0.35f;  // pistolet encore visible après le tir
    private static final float END_TIME    = AIM_TIME + HOLD_TIME + FADE_TIME;

    private static final float SCALE        = 2.2f;
    private static final float RISE         = 150f;  // le pistolet surgit au-dessus du symbole...
    private static final float SIDE         = -230f; // ... et à sa gauche, pour viser en biais
    private static final float RECOIL       = 22f;   // pixels, le long du canon
    private static final float RECOIL_ANGLE = 18f;   // degrés, le canon se relève
    // Dans l'image du pistolet (pixels) : poignée (pivot) et bout du canon.
    private static final float GRIP_X   = 30f, GRIP_Y   = 30f;
    private static final float MUZZLE_X = 116f, MUZZLE_Y = 80f;

    private final TextureRegion gun;
    private final TextureRegion pixel;

    private float   time = -1f;
    private float   startX, startY;    // position de la poignée
    private float   targetX, targetY;
    private float   aimAngle;           // degrés, canon vers la cible

    /**
     * @param gunTexture image du revolver, canon vers la droite
     * @param pixel      région d'un pixel blanc (traçante, éclairs)
     */
    public PistolShotAnimation(Texture gunTexture, TextureRegion pixel) {
        this.gun   = new TextureRegion(gunTexture);
        this.pixel = pixel;
        setTouchable(Touchable.disabled);
    }

    /**
     * Joue le tir : le pistolet apparaît en {@code from} (coordonnées du Stage)
     * et tire sur {@code target} au bout de {@link #AIM_TIME} secondes.
     */
    public void play(Vector2 from, Vector2 target) {
        startX  = from.x + SIDE;
        startY  = from.y + RISE;
        targetX = target.x;
        targetY = target.y;
        // Angle du canon : depuis la poignée, en tenant compte du décalage du canon dans l'image.
        float dx = targetX - startX, dy = targetY - startY;
        float offset = MathUtils.atan2(MUZZLE_Y - GRIP_Y, MUZZLE_X - GRIP_X) * MathUtils.radiansToDegrees;
        aimAngle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees - offset * 0.5f;
        time = 0f;
    }

    /** Arrête l'animation (nouveau combat). */
    public void cancel() { time = -1f; }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (time < 0f) return;
        time += delta;
        if (time > END_TIME) time = -1f;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (time < 0f) return;
        float r = batch.getColor().r, g = batch.getColor().g, b = batch.getColor().b, a = batch.getColor().a;

        float appear = Interpolation.swingOut.apply(Math.min(1f, time / APPEAR_TIME));
        float turn   = Interpolation.pow2Out.apply(MathUtils.clamp((time - APPEAR_TIME) / TURN_TIME, 0f, 1f));
        float recoil = time < AIM_TIME ? 0f : Math.max(0f, 1f - (time - AIM_TIME) / RECOIL_TIME);
        float alpha  = time < END_TIME - FADE_TIME ? 1f : Math.max(0f, (END_TIME - time) / FADE_TIME);

        float angle = aimAngle * turn + RECOIL_ANGLE * recoil;
        float cos = MathUtils.cosDeg(angle), sin = MathUtils.sinDeg(angle);
        float x = startX - cos * RECOIL * recoil;
        float y = startY - sin * RECOIL * recoil;
        float scale = SCALE * appear;
        boolean flip = angle > 90f || angle < -90f; // canon vers la gauche : l'image est retournée

        batch.setColor(1f, 1f, 1f, alpha * parentAlpha);
        batch.draw(gun, x - GRIP_X, y - GRIP_Y, GRIP_X, GRIP_Y, gun.getRegionWidth(), gun.getRegionHeight(),
            scale, flip ? -scale : scale, angle);

        if (time >= AIM_TIME) drawShot(batch, x, y, angle, flip, parentAlpha);
        batch.setColor(r, g, b, a);
    }

    /** Éclair au bout du canon, traçante jusqu'à la cible et impact. */
    private void drawShot(Batch batch, float gripX, float gripY, float angle, boolean flip, float parentAlpha) {
        float since = time - AIM_TIME;
        float mx = (MUZZLE_X - GRIP_X) * SCALE, my = (MUZZLE_Y - GRIP_Y) * SCALE * (flip ? -1f : 1f);
        float cos = MathUtils.cosDeg(angle), sin = MathUtils.sinDeg(angle);
        float muzzleX = gripX + mx * cos - my * sin;
        float muzzleY = gripY + mx * sin + my * cos;

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        if (since < TRACER_TIME) {
            float fade = 1f - since / TRACER_TIME;
            float dx = targetX - muzzleX, dy = targetY - muzzleY;
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            float tracerAngle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
            batch.setColor(1f, 0.85f, 0.4f, 0.6f * fade * parentAlpha);
            batch.draw(pixel, muzzleX, muzzleY - 5f, 0f, 5f, length, 10f, 1f, 1f, tracerAngle);
            batch.setColor(1f, 1f, 0.9f, fade * parentAlpha);
            batch.draw(pixel, muzzleX, muzzleY - 1.5f, 0f, 1.5f, length, 3f, 1f, 1f, tracerAngle);
            star(batch, targetX, targetY, 70f * (0.6f + since / TRACER_TIME), fade * parentAlpha);
        }
        if (since < FLASH_TIME) {
            star(batch, muzzleX, muzzleY, 50f, (1f - since / FLASH_TIME) * parentAlpha);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** Éclair en étoile (quatre branches) centré sur {@code (x, y)}. */
    private void star(Batch batch, float x, float y, float size, float alpha) {
        batch.setColor(1f, 0.8f, 0.3f, 0.7f * alpha);
        for (int k = 0; k < 4; k++) {
            batch.draw(pixel, x, y - size * 0.08f, 0f, size * 0.08f, size, size * 0.16f, 1f, 1f, k * 90f + 45f);
        }
        batch.setColor(1f, 1f, 0.85f, alpha);
        batch.draw(pixel, x - size * 0.2f, y - size * 0.2f, size * 0.2f, size * 0.2f, size * 0.4f, size * 0.4f,
            1f, 1f, 45f);
    }
}
