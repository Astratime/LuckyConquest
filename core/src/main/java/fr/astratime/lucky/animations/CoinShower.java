package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

/**
 * Pluie de pièces d'or qui tournent sur elles-mêmes : chaque pièce tombe,
 * rebondit sur la table puis s'efface. Les pièces « collectées » quittent leur
 * chute pour filer vers une cible (la pièce du panneau des gains) ; leur
 * arrivée est signalée à chaque fois.
 *
 * Les positions sont celles du Stage : l'acteur doit être placé à l'origine.
 */
public class CoinShower extends Actor {

    private static final int   FRAME_SIZE   = 20;     // taille d'une étape de rotation dans l'image
    private static final float SPIN_FPS     = 14f;
    private static final float GRAVITY      = -1500f;
    private static final float BOUNCE       = 0.45f;  // vitesse conservée à chaque rebond
    private static final int   MAX_BOUNCES  = 2;
    private static final float REST_TIME    = 0.35f;  // posée sur la table avant de s'effacer
    private static final float FADE_TIME    = 0.4f;
    private static final float COLLECT_DELAY_MIN = 0.3f;  // chute avant de filer vers la cible
    private static final float COLLECT_DELAY_MAX = 0.7f;
    private static final float COLLECT_DURATION  = 0.6f;
    private static final float COLLECT_ARC       = 180f;  // hauteur de la courbe vers la cible

    private static final class Coin {
        float x, y, vx, vy, floor, age, phase, scale, restTime, alpha = 1f;
        int     bounces;
        boolean collector, collecting;
        float   collectAt, startX, startY, progress;
    }

    private final TextureRegion[] frames;
    private final List<Coin>      coins = new ArrayList<>();
    private final Supplier<Vector2> target;
    private final Runnable          onCollected;

    /**
     * @param spinStrip   étapes de rotation de la pièce, côte à côte (carrés de FRAME_SIZE)
     * @param target      position (Stage) vers laquelle filent les pièces collectées
     * @param onCollected appelé à l'arrivée de chaque pièce collectée
     */
    public CoinShower(TextureRegion spinStrip, Supplier<Vector2> target, Runnable onCollected) {
        this.target      = target;
        this.onCollected = onCollected;
        int count = spinStrip.getRegionWidth() / FRAME_SIZE;
        frames = new TextureRegion[count];
        for (int i = 0; i < count; i++) {
            frames[i] = new TextureRegion(spinStrip, i * FRAME_SIZE, 0, FRAME_SIZE, FRAME_SIZE);
        }
    }

    /**
     * Lâche une pièce au-dessus de l'écran, à l'abscisse {@code x}.
     *
     * @param floor     ordonnée (Stage) de la table où elle rebondit
     * @param collector true si elle doit filer vers la cible au lieu de retomber
     */
    public void drop(float x, float topY, float floor, boolean collector) {
        Coin coin = new Coin();
        coin.x         = x;
        coin.y         = topY + MathUtils.random(0f, 80f);
        coin.vx        = MathUtils.random(-120f, 120f);
        coin.vy        = MathUtils.random(-350f, -80f);
        coin.floor     = floor;
        coin.phase     = MathUtils.random(frames.length);
        coin.scale     = MathUtils.randomBoolean(0.35f) ? 3f : 2f; // échelle entière : pixels nets
        coin.collector = collector;
        coin.collectAt = MathUtils.random(COLLECT_DELAY_MIN, COLLECT_DELAY_MAX);
        coins.add(coin);
    }

    /** @return true s'il ne reste aucune pièce à l'écran. */
    public boolean isEmpty() { return coins.isEmpty(); }

    /** Retire toutes les pièces immédiatement. */
    public void removeAll() { coins.clear(); }

    @Override
    public void act(float delta) {
        super.act(delta);
        Iterator<Coin> it = coins.iterator();
        while (it.hasNext()) {
            Coin coin = it.next();
            coin.age += delta;
            if (coin.collecting ? updateCollecting(coin, delta) : updateFalling(coin, delta)) {
                it.remove();
            }
        }
    }

    /** @return true quand la pièce est arrivée sur la cible. */
    private boolean updateCollecting(Coin coin, float delta) {
        coin.progress = Math.min(1f, coin.progress + delta / COLLECT_DURATION);
        float t = Interpolation.pow2In.apply(coin.progress);
        Vector2 end = target.get();
        // Courbe de Bézier quadratique : la pièce remonte en arc avant de plonger vers la cible.
        float controlX = (coin.startX + end.x) / 2f;
        float controlY = Math.max(coin.startY, end.y) + COLLECT_ARC;
        float u = 1f - t;
        coin.x = u * u * coin.startX + 2 * u * t * controlX + t * t * end.x;
        coin.y = u * u * coin.startY + 2 * u * t * controlY + t * t * end.y;
        if (coin.progress >= 1f) {
            onCollected.run();
            return true;
        }
        return false;
    }

    /** @return true quand la pièce a fini de s'effacer. */
    private boolean updateFalling(Coin coin, float delta) {
        if (coin.collector && coin.age >= coin.collectAt) {
            coin.collecting = true;
            coin.startX     = coin.x;
            coin.startY     = coin.y;
            return false;
        }
        if (coin.bounces > MAX_BOUNCES) { // posée : attend puis s'efface
            coin.restTime += delta;
            if (coin.restTime > REST_TIME) coin.alpha -= delta / FADE_TIME;
            return coin.alpha <= 0f;
        }
        coin.vy += GRAVITY * delta;
        coin.x  += coin.vx * delta;
        coin.y  += coin.vy * delta;
        if (coin.y <= coin.floor && coin.vy < 0) {
            coin.y = coin.floor;
            coin.bounces++;
            coin.vy = -coin.vy * BOUNCE;
            coin.vx *= 0.6f;
            if (coin.bounces > MAX_BOUNCES) {
                coin.vx = 0f;
                coin.vy = 0f;
            }
        }
        return false;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float r = batch.getColor().r, g = batch.getColor().g, b = batch.getColor().b, a = batch.getColor().a;
        for (Coin coin : coins) {
            boolean resting = coin.bounces > MAX_BOUNCES;
            int frame = resting ? 0 : (int) (coin.age * SPIN_FPS + coin.phase) % frames.length;
            float size = FRAME_SIZE * coin.scale;
            batch.setColor(1f, 1f, 1f, coin.alpha * parentAlpha);
            batch.draw(frames[frame], coin.x - size / 2f, coin.y - size / 2f, size, size);
        }
        batch.setColor(r, g, b, a);
    }
}
