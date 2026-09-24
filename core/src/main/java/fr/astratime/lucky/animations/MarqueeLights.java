package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;

import java.util.ArrayList;
import java.util.List;

/**
 * Guirlande d'ampoules de casino réparties sur le contour de l'acteur : une
 * ampoule sur trois est allumée et l'allumage tourne autour du cadre
 * (chenillard). En mode fête (jackpot, victoire), toutes les ampoules
 * clignotent en alternance, plus vite. Les ampoules allumées ont un halo
 * lumineux dessiné en mélange additif.
 */
public class MarqueeLights extends Actor {

    private static final float SPACING     = 44f;   // entre deux ampoules
    private static final float BULB_SIZE   = 21f;   // 7 pixels de la grille x3
    private static final float GLOW_SIZE   = 46f;
    private static final float GLOW_ALPHA  = 0.55f;
    private static final float CHASE_SPEED = 7f;    // pas du chenillard par seconde
    private static final float PARTY_SPEED = 12f;   // clignotements par seconde en mode fête

    private final TextureRegion off;
    private final TextureRegion on;
    private final Texture       glow;
    private final List<float[]> bulbs = new ArrayList<>();
    private float               time;
    private boolean             party;

    /**
     * @param bulbs image des deux états d'une ampoule côte à côte (éteinte, allumée)
     * @param glow  halo d'une ampoule allumée (dégradé lisse)
     */
    public MarqueeLights(Texture bulbs, Texture glow) {
        this.glow = glow;
        int size = bulbs.getHeight();
        off = new TextureRegion(bulbs, 0, 0, size, size);
        on  = new TextureRegion(bulbs, size, 0, size, size);
        setTouchable(Touchable.disabled);
    }

    /** Mode fête : toutes les ampoules clignotent en alternance (jackpot, victoire), ou reprise du chenillard. */
    public void setParty(boolean party) {
        this.party = party;
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        placeBulbs();
    }

    @Override
    protected void positionChanged() {
        super.positionChanged();
        placeBulbs();
    }

    /** Répartit les ampoules à intervalles réguliers sur le contour, en commençant par le coin bas gauche. */
    private void placeBulbs() {
        bulbs.clear();
        float w = getWidth(), h = getHeight();
        if (w <= 0f || h <= 0f) return;
        int perSideX = Math.max(1, Math.round(w / SPACING));
        int perSideY = Math.max(1, Math.round(h / SPACING));
        for (int i = 0; i < perSideX; i++) bulbs.add(new float[] {getX() + w * i / perSideX, getY()});
        for (int i = 0; i < perSideY; i++) bulbs.add(new float[] {getX() + w, getY() + h * i / perSideY});
        for (int i = 0; i < perSideX; i++) bulbs.add(new float[] {getX() + w - w * i / perSideX, getY() + h});
        for (int i = 0; i < perSideY; i++) bulbs.add(new float[] {getX(), getY() + h - h * i / perSideY});
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        time += delta;
    }

    private boolean isLit(int index) {
        if (party) return ((int) (time * PARTY_SPEED) + index) % 2 == 0;
        return (index + (int) (time * CHASE_SPEED)) % 3 == 0;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float r = batch.getColor().r, g = batch.getColor().g, b = batch.getColor().b, a = batch.getColor().a;
        batch.setColor(1f, 1f, 1f, GLOW_ALPHA * parentAlpha);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE); // halo additif
        for (int i = 0; i < bulbs.size(); i++) {
            if (!isLit(i)) continue;
            float[] bulb = bulbs.get(i);
            batch.draw(glow, bulb[0] - GLOW_SIZE / 2f, bulb[1] - GLOW_SIZE / 2f, GLOW_SIZE, GLOW_SIZE);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(1f, 1f, 1f, parentAlpha);
        for (int i = 0; i < bulbs.size(); i++) {
            float[] bulb = bulbs.get(i);
            batch.draw(isLit(i) ? on : off, bulb[0] - BULB_SIZE / 2f, bulb[1] - BULB_SIZE / 2f, BULB_SIZE, BULB_SIZE);
        }
        batch.setColor(r, g, b, a);
    }
}
