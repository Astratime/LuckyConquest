package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import fr.astratime.lucky.assets.Palette;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Bingo de la Tirelire : un cochon rose tombe sur la table ; des pièces d'or
 * tombent une à une dans sa fente et il grossit à chaque pièce, de plus en plus
 * vite ; trop plein, il tremble puis éclate à {@link #BURST_TIME} (l'explosion
 * de pièces et de Tirelires est lancée par {@link JackpotCelebration}).
 */
public class PiggyBankScene extends BingoScene {

    /** Le cochon est posé : les pièces commencent à tomber. */
    public static final float LAND_TIME  = 0.3f;
    /** Instants où une pièce entre dans la fente (le cochon grossit). */
    public static final float[] COINS = {0.5f, 0.75f, 0.95f, 1.1f, 1.22f, 1.32f, 1.4f};
    /** Le cochon éclate. */
    public static final float BURST_TIME = 1.65f;

    private static final int PIG_W = 34, PIG_H = 26;
    private static final float PIG_SCALE = 8f;
    private static final float COIN_FALL = 0.22f;
    private static final float SWELL = 0.07f;   // en plus par pièce

    private final Image pig, coin;
    private final Vector2 base = new Vector2(), slot = new Vector2();

    public PiggyBankScene() {
        pig  = image(new TextureRegion(texture(pig())), PIG_SCALE);
        pig.setOrigin(pig.getWidth() / 2f, 0f);
        coin = image(new TextureRegion(texture(coin())), PIG_SCALE);
        coin.setOrigin(coin.getWidth() / 2f, coin.getHeight() / 2f);
        addActor(coin);
        addActor(pig);
    }

    /** @return la fente (Stage), où entrent les pièces. */
    public Vector2 slot() { return slot; }

    /** @return le centre (Stage) du cochon. */
    public Vector2 center() { return new Vector2(base.x, base.y + pig.getHeight() * pig.getScaleY() / 2f); }

    @Override
    protected void start(float x, float y) {
        base.set(x, y);
        pig.setVisible(true);
        pig.setColor(Color.WHITE);
        coin.setVisible(false);
        update(0f);
    }

    @Override
    protected void update(float delta) {
        if (time >= BURST_TIME) {
            pig.setVisible(false);
            coin.setVisible(false);
            return;
        }
        // Il tombe du haut et s'écrase un peu en touchant la table.
        float drop = time < LAND_TIME ? 700f * (1f - Interpolation.pow2In.apply(time / LAND_TIME)) : 0f;
        int fed = 0;
        for (float at : COINS) if (time >= at) fed++;
        float size = 1f + fed * SWELL;
        float squash = 1f;
        if (time >= LAND_TIME && time < LAND_TIME + 0.15f) squash = 1f - 0.2f * (1f - (time - LAND_TIME) / 0.15f);
        // Une pièce vient d'entrer : petit sursaut.
        for (float at : COINS) {
            float since = time - at;
            if (since >= 0f && since < 0.12f) squash *= 1f + 0.08f * (1f - since / 0.12f);
        }
        float tremble = time > COINS[COINS.length - 1] ? 6f * (time - COINS[COINS.length - 1]) / (BURST_TIME - COINS[COINS.length - 1]) : 0f;
        pig.setScale(size * (2f - squash), size * squash);
        pig.setColor(1f, 1f - 0.25f * Math.max(0f, tremble / 6f), 1f - 0.25f * Math.max(0f, tremble / 6f), 1f);
        placeOrigin(pig, base.x + MathUtils.random(-tremble, tremble), base.y + drop);
        slot.set(base.x + 2f * PIG_SCALE * size, base.y + pig.getHeight() * pig.getScaleY());

        // La pièce qui tombe dans la fente.
        coin.setVisible(false);
        for (float at : COINS) {
            float progress = (time - (at - COIN_FALL)) / COIN_FALL;
            if (progress < 0f || progress >= 1f) continue;
            coin.setVisible(true);
            coin.setRotation(progress * 360f);
            coin.setScaleX(MathUtils.cos(progress * 12f));
            placeOrigin(coin, slot.x, slot.y + 420f * (1f - Interpolation.pow2In.apply(progress)) - 20f);
        }
    }

    /** @return la tirelire : cochon rose de profil, fente sur le dos, groin et patte. */
    private static Pixmap pig() {
        Pixmap pixmap = pixmap(PIG_W, PIG_H);
        Color pink = hex("f49ab8"), light = hex("ffc6d8"), dark = hex("c8648a");
        Shape body = (x, y) -> {
            float u = (x + 0.5f - 16f) / 14f, v = (y + 0.5f - 12f) / 9.5f;
            boolean belly = u * u + v * v <= 1f;
            boolean snout = x >= 28 && x <= 32 && y >= 9 && y <= 15;
            boolean ear = x >= 21 && x <= 25 && y >= 1 && y <= 4 && x - 21 >= 4 - y;
            boolean legs = y >= 20 && y <= 24 && ((x >= 6 && x <= 9) || (x >= 20 && x <= 23));
            boolean tail = x <= 2 && y >= 8 && y <= 10;
            return belly || snout || ear || legs || tail;
        };
        fillShape(pixmap, body, (x, y) -> y < 7 && x > 6 && x < 26 ? light : y > 17 ? dark : pink);
        pixmap.setColor(OUTLINE);
        pixmap.fillRectangle(13, 3, 7, 1);     // fente
        pixmap.drawPixel(25, 9);               // œil
        pixmap.setColor(dark);
        pixmap.drawPixel(30, 11);              // narines
        pixmap.drawPixel(30, 13);
        return pixmap;
    }

    /** @return une pièce d'or de face. */
    private static Pixmap coin() {
        Pixmap pixmap = pixmap(11, 11);
        fillOutlinedCircle(pixmap, 5, 5, 5, Palette.GOLD);
        pixmap.setColor(Palette.GOLD_PALE);
        pixmap.fillRectangle(3, 3, 2, 2);
        pixmap.setColor(hex("b07a10"));
        pixmap.fillRectangle(5, 3, 1, 5);
        return pixmap;
    }
}
