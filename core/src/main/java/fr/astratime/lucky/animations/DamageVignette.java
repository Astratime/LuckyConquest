package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Disposable;

/**
 * Voile rouge sur les bords de l'écran quand le joueur est touché : il
 * apparaît d'un coup puis s'estompe. Le dégradé (transparent au centre, rouge
 * vers les bords) est calculé une fois dans une petite texture lissée, étirée
 * sur tout l'écran.
 */
public class DamageVignette extends Actor implements Disposable {

    private static final int   TEXTURE_WIDTH  = 128;
    private static final int   TEXTURE_HEIGHT = 72;
    private static final float INNER          = 0.55f;  // rayon (normalisé) où le rouge commence
    private static final float FADE_DURATION  = 0.6f;

    private final Texture texture;

    public DamageVignette() {
        setTouchable(Touchable.disabled);
        getColor().a = 0f;
        Pixmap pixmap = new Pixmap(TEXTURE_WIDTH, TEXTURE_HEIGHT, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < TEXTURE_HEIGHT; y++) {
            for (int x = 0; x < TEXTURE_WIDTH; x++) {
                float dx = (x + 0.5f) / TEXTURE_WIDTH * 2f - 1f;
                float dy = (y + 0.5f) / TEXTURE_HEIGHT * 2f - 1f;
                float distance = (float) Math.sqrt(dx * dx + dy * dy) / (float) Math.sqrt(2f) * 1.6f;
                float t = MathUtils.clamp((distance - INNER) / (1f - INNER), 0f, 1f);
                float alpha = t * t * (3f - 2f * t); // lissage (smoothstep)
                pixmap.drawPixel(x, y, ((int) (0.85f * 255) << 24) | ((int) (0.05f * 255) << 16)
                    | ((int) (0.08f * 255) << 8) | (int) (alpha * 255));
            }
        }
        texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
    }

    /** Fait apparaître le voile avec l'opacité {@code strength} (0 à 1), puis l'estompe. */
    public void flash(float strength) {
        clearActions();
        getColor().a = strength;
        addAction(Actions.fadeOut(FADE_DURATION));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float alpha = getColor().a * parentAlpha;
        if (alpha <= 0f || getStage() == null) return;
        float r = batch.getColor().r, g = batch.getColor().g, b = batch.getColor().b, a = batch.getColor().a;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(texture, 0f, 0f, getStage().getViewport().getWorldWidth(), getStage().getViewport().getWorldHeight());
        batch.setColor(r, g, b, a);
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}
