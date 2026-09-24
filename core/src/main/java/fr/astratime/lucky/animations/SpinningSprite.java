package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;

/**
 * Image animée en boucle à partir d'une bande d'étapes de même taille, côte à
 * côte (ex : un jeton qui tournoie sur lui-même). Dessinée aux dimensions de
 * l'acteur, autour de son centre (l'échelle de l'acteur s'applique).
 */
public class SpinningSprite extends Actor {

    private final TextureRegion[] frames;
    private final float           fps;
    private float                 time;

    /**
     * @param strip      étapes côte à côte, chacune carrée (côté = hauteur de la bande)
     * @param fps        étapes par seconde
     * @param startFrame étape de départ (pour décaler deux sprites identiques)
     */
    public SpinningSprite(TextureRegion strip, float fps, int startFrame) {
        int size  = strip.getRegionHeight();
        int count = strip.getRegionWidth() / size;
        frames = new TextureRegion[count];
        for (int i = 0; i < count; i++) frames[i] = new TextureRegion(strip, i * size, 0, size, size);
        this.fps  = fps;
        this.time = startFrame / fps;
        setTouchable(Touchable.disabled);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        time += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float r = batch.getColor().r, g = batch.getColor().g, b = batch.getColor().b, a = batch.getColor().a;
        batch.setColor(1f, 1f, 1f, getColor().a * parentAlpha);
        TextureRegion frame = frames[(int) (time * fps) % frames.length];
        batch.draw(frame, getX(), getY(), getWidth() / 2f, getHeight() / 2f, getWidth(), getHeight(),
            getScaleX(), getScaleY(), getRotation());
        batch.setColor(r, g, b, a);
    }
}
