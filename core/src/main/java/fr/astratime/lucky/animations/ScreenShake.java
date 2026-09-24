package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import fr.astratime.lucky.settings.VisualSettings;

/**
 * Secousse de l'écran : décale la caméra du Stage au hasard, de moins en moins
 * fort jusqu'à la fin de la secousse, puis la recentre. Invisible ; doit être
 * ajouté au Stage pour être mis à jour. Sans effet quand les effets sont réduits.
 */
public class ScreenShake extends Actor {

    private final VisualSettings settings;
    private float duration;
    private float remaining;
    private float strength;

    public ScreenShake(VisualSettings settings) {
        this.settings = settings;
    }

    /**
     * Secoue l'écran pendant {@code duration} secondes, avec un décalage
     * maximal de {@code strength} pixels. Une secousse plus forte en cours n'est
     * pas écourtée.
     */
    public void shake(float duration, float strength) {
        if (settings.isReducedEffects()) return;
        if (remaining > 0f && strength * duration <= this.strength * remaining) return;
        this.duration  = duration;
        this.remaining = duration;
        this.strength  = strength;
    }

    /** Arrête la secousse et recentre la caméra. */
    public void stop() {
        remaining = 0f;
        if (getStage() != null) moveCamera(0f, 0f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (remaining <= 0f) return;
        remaining -= delta;
        if (remaining <= 0f) {
            stop();
            return;
        }
        float current = strength * (remaining / duration);
        moveCamera(MathUtils.random(-current, current), MathUtils.random(-current, current));
    }

    /** Décale la caméra de {@code (dx, dy)} par rapport au centre de l'écran (sa position au repos). */
    private void moveCamera(float dx, float dy) {
        Camera camera = getStage().getCamera();
        camera.position.set(getStage().getViewport().getWorldWidth() / 2f + dx,
            getStage().getViewport().getWorldHeight() / 2f + dy, 0f);
        camera.update();
    }
}
