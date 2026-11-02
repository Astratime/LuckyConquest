package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Bingo du Bouclier clouté : le bouclier entre par la gauche en tournant comme
 * une scie, rebondit trois fois sur la table et s'arrête net au centre à
 * {@link #STOP_TIME} ; ses clous jaillissent alors en rayons d'acier tout autour
 * à {@link #SPIKES_TIME} (les étincelles et les boucliers projetés sont lancés
 * par {@link JackpotCelebration}, qui suit {@link #position}).
 */
public class SpikedShieldScene extends BingoScene {

    /** Le bouclier s'arrête au centre. */
    public static final float STOP_TIME   = 0.95f;
    /** Les clous jaillissent. */
    public static final float SPIKES_TIME = 1.25f;

    private static final float SIZE   = 300f;
    private static final float FROM   = -1100f;    // départ, relatif au centre
    private static final int   SPIKES = 12;
    private static final float SPIKE_LENGTH = 520f;

    private final Image shield, glow;
    private final Image[] spikes = new Image[SPIKES];
    private final Vector2 center = new Vector2(), at = new Vector2();

    /** @param spiked image du symbole Bouclier clouté */
    public SpikedShieldScene(TextureRegion spiked) {
        TextureRegion pixel = new TextureRegion(texture(whitePixel()));
        for (int i = 0; i < SPIKES; i++) {
            spikes[i] = image(pixel, 1f);
            spikes[i].setSize(SPIKE_LENGTH, 14f);
            spikes[i].setOrigin(0f, 7f);
            addActor(spikes[i]);
        }
        glow = image(texture(halo(56, hex("b8d8ff"))));
        glow.setOrigin(glow.getWidth() / 2f, glow.getHeight() / 2f);
        addActor(glow);
        shield = image(spiked, SIZE / spiked.getRegionWidth());
        shield.setOrigin(shield.getWidth() / 2f, shield.getHeight() / 2f);
        addActor(shield);
    }

    /** @return la position (Stage) du bouclier. */
    public Vector2 position() { return at; }

    @Override
    protected void start(float x, float y) {
        center.set(x, y);
        for (Image spike : spikes) spike.setVisible(false);
        glow.getColor().a = 0f;
        shield.setColor(Color.WHITE);
        update(0f);
    }

    @Override
    protected void update(float delta) {
        if (time < STOP_TIME) {
            float progress = time / STOP_TIME;
            float eased = Interpolation.pow2Out.apply(progress);
            // Trois rebonds de plus en plus bas.
            float hop = Math.abs(MathUtils.sin(progress * MathUtils.PI * 3f)) * 160f * (1f - progress);
            at.set(center.x + FROM * (1f - eased), center.y + hop);
            shield.setRotation(-progress * 1440f);
            shield.setScale(0.8f + 0.2f * progress);
        } else {
            float since = time - STOP_TIME;
            at.set(center.x + (since < 0.1f ? MathUtils.random(-10f, 10f) : 0f), center.y);
            shield.setRotation(MathUtils.sin(since * 30f) * 12f * Math.max(0f, 1f - since / 0.3f));
            shield.setScale(1f + (time >= SPIKES_TIME ? 0.15f * Interpolation.swingOut.apply(Math.min(1f, (time - SPIKES_TIME) / 0.3f)) : 0f));
        }
        placeOrigin(shield, at.x, at.y);
        placeOrigin(glow, at.x, at.y);
        glow.getColor().a = time >= SPIKES_TIME ? 0.8f : 0f;
        glow.setScale(4f + 0.3f * MathUtils.sin(time * 16f));

        if (time < SPIKES_TIME) return;
        float since = time - SPIKES_TIME;
        float reach = Interpolation.pow3Out.apply(Math.min(1f, since / 0.2f));
        for (int i = 0; i < SPIKES; i++) {
            Image spike = spikes[i];
            spike.setVisible(true);
            spike.setRotation(i * 360f / SPIKES + 15f);
            spike.setScaleX(reach);
            spike.setScaleY(Math.max(0.1f, 1f - since / 0.9f));
            spike.setPosition(at.x, at.y - spike.getOriginY());
            float shade = i % 2 == 0 ? 1f : 0.75f;
            spike.setColor(0.85f * shade, 0.92f * shade, 1f * shade, Math.max(0f, 0.9f - since * 0.8f));
        }
    }
}
