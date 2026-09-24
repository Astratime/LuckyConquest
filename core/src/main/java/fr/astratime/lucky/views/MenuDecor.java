package fr.astratime.lucky.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.animations.CoinShower;
import fr.astratime.lucky.animations.ReelActor;

import java.util.ArrayList;
import java.util.List;

/**
 * Décor animé du menu : l'intérieur du casino (assets/menu/casino_interior.png)
 * et, par-dessus, ce qui bouge. Les néons « COIN » et « BAR » grésillent de
 * temps en temps, les ampoules des machines à sous clignotent, les rouleaux
 * des machines se lancent à tour de rôle (un jackpot de temps en temps fait
 * clignoter la machine et cracher des pièces) et de la poussière dorée flotte
 * dans les cônes de lumière des lampes.
 *
 * Tout est placé d'après la grille du dessin (480 x 270, agrandi x4) : le
 * décor reste aligné quelle que soit la taille qu'on lui donne ({@link #layout}).
 */
public class MenuDecor extends Group implements Disposable {

    private static final float GRID_WIDTH  = 480f;
    private static final float GRID_HEIGHT = 270f;

    // Machines à sous du dessin : 4 caisses de 32 de large, rouleaux de 8 x 12 (coordonnées de la grille).
    private static final int   MACHINES       = 4;
    private static final int   MACHINE_X      = 10;
    private static final int   MACHINE_STEP   = 36;
    private static final int   MACHINE_TOP    = 82;
    private static final int   MACHINE_BASE   = 196;
    private static final int   REEL_OFFSET_X  = 4;
    private static final int   REEL_OFFSET_Y  = 23;
    private static final int   REEL_WIDTH     = 8;
    private static final int   REEL_HEIGHT    = 12;
    private static final int   TRAY_OFFSET_Y  = 58;
    private static final int   JACKPOT_SYMBOL = 0;   // le 7
    private static final float JACKPOT_CHANCE = 0.25f;
    private static final float SPIN_WAIT_MIN  = 2.5f;
    private static final float SPIN_WAIT_MAX  = 7f;
    private static final int   JACKPOT_COINS  = 14;

    // Lampes suspendues : cônes de lumière où flotte la poussière.
    private static final int[] LAMPS        = {70, 240, 410};
    private static final int   CONE_TOP     = 42;
    private static final int   CONE_BOTTOM  = 200;
    private static final float CONE_SPREAD  = 60f;
    private static final int   DUST_PER_LAMP = 22;

    private static final float BULB_BLINK       = 0.35f;  // secondes entre deux alternances
    private static final float BULB_PARTY_BLINK = 0.08f;  // pendant un jackpot

    private final Texture backgroundTexture = load("menu/casino_interior.png");
    private final Texture neonCoinTexture    = load("menu/neon_coin.png");
    private final Texture neonBarTexture    = load("menu/neon_bar.png");
    private final Texture bulbsATexture     = load("menu/bulbs_a.png");
    private final Texture bulbsBTexture     = load("menu/bulbs_b.png");
    private final Texture symbolsTexture    = load("menu/mini_symbols.png");
    private final Texture coinTexture       = load("jackpot/coin_spin.png");

    private final Image                     background;
    private final Neon                      neonCoin;
    private final Neon                      neonBar;
    private final Image                     bulbsA;
    private final Image                     bulbsB;
    private final List<List<ReelActor<Integer>>> reels = new ArrayList<>();
    private final float[]                   nextSpin = new float[MACHINES];
    private final CoinShower                coins;
    private final Dust                      dust = new Dust();

    private float bulbTimer;
    private float partyTime;   // temps restant du clignotement de fête (jackpot)
    private float gridScaleX = 1f, gridScaleY = 1f;

    public MenuDecor() {
        setTouchable(Touchable.disabled);
        background = image(backgroundTexture);
        bulbsA     = image(bulbsATexture);
        bulbsB     = image(bulbsBTexture);
        bulbsB.setVisible(false);

        TextureRegion[] symbols = new TextureRegion[3];
        for (int i = 0; i < symbols.length; i++) symbols[i] = new TextureRegion(symbolsTexture, i * 8, 0, 8, 12);
        Integer[] pool = {0, 1, 2};
        for (int m = 0; m < MACHINES; m++) {
            List<ReelActor<Integer>> machine = new ArrayList<>();
            for (int r = 0; r < 3; r++) {
                ReelActor<Integer> reel = new ReelActor<>(pool, index -> symbols[index]);
                reel.show(MathUtils.random(2));
                addActor(reel);
                machine.add(reel);
            }
            reels.add(machine);
            nextSpin[m] = MathUtils.random(0.5f, SPIN_WAIT_MAX);
        }

        neonCoin = new Neon(neonCoinTexture);
        neonBar = new Neon(neonBarTexture);
        addActor(neonCoin);
        addActor(neonBar);
        addActor(dust);
        coins = new CoinShower(new TextureRegion(coinTexture), Vector2::new, () -> { }, 0.6f);
        addActor(coins);
    }

    private Image image(Texture texture) {
        Image image = new Image(new TextureRegionDrawable(new TextureRegion(texture)));
        image.setTouchable(Touchable.disabled);
        addActor(image);
        return image;
    }

    private static Texture load(String path) {
        return new Texture(Gdx.files.internal(path));
    }

    /** Étire le décor sur {@code (0, 0, width, height)} (coordonnées du groupe) et replace ce qui bouge. */
    public void layout(float width, float height) {
        setSize(width, height);
        gridScaleX = width / GRID_WIDTH;
        gridScaleY = height / GRID_HEIGHT;
        for (Actor actor : new Actor[] {background, bulbsA, bulbsB, neonCoin, neonBar}) {
            actor.setBounds(0f, 0f, width, height);
        }
        for (int m = 0; m < MACHINES; m++) {
            int machineX = MACHINE_X + m * MACHINE_STEP;
            for (int r = 0; r < 3; r++) {
                int gx = machineX + REEL_OFFSET_X + r * REEL_WIDTH;
                int gy = MACHINE_TOP + REEL_OFFSET_Y;
                reels.get(m).get(r).setBounds(gridX(gx), gridY(gy + REEL_HEIGHT), REEL_WIDTH * gridScaleX,
                    REEL_HEIGHT * gridScaleY);
            }
        }
    }

    /** @return l'abscisse (dans le groupe) de la colonne {@code gx} de la grille du dessin. */
    private float gridX(float gx) { return gx * gridScaleX; }

    /** @return l'ordonnée (dans le groupe) de la ligne {@code gy} de la grille (comptée depuis le haut). */
    private float gridY(float gy) { return (GRID_HEIGHT - gy) * gridScaleY; }

    @Override
    public void act(float delta) {
        super.act(delta);
        partyTime = Math.max(0f, partyTime - delta);
        bulbTimer += delta;
        float blink = partyTime > 0f ? BULB_PARTY_BLINK : BULB_BLINK;
        if (bulbTimer >= blink) {
            bulbTimer = 0f;
            bulbsA.setVisible(!bulbsA.isVisible());
            bulbsB.setVisible(!bulbsA.isVisible());
        }
        for (int m = 0; m < MACHINES; m++) {
            nextSpin[m] -= delta;
            if (nextSpin[m] <= 0f && !reels.get(m).get(2).isSpinning()) spin(m);
        }
    }

    /** Lance les rouleaux de la machine {@code m} ; un jackpot fait clignoter les ampoules et cracher des pièces. */
    private void spin(int m) {
        nextSpin[m] = MathUtils.random(SPIN_WAIT_MIN, SPIN_WAIT_MAX);
        boolean jackpot = MathUtils.randomBoolean(JACKPOT_CHANCE);
        List<ReelActor<Integer>> machine = reels.get(m);
        for (int r = 0; r < 3; r++) {
            int symbol = jackpot ? JACKPOT_SYMBOL : MathUtils.random(2);
            Runnable onStop = r == 2 && jackpot ? () -> payOut(m) : null;
            machine.get(r).spin(symbol, 0.7f + r * 0.3f, -1f, onStop);
        }
    }

    /** Jackpot de la machine {@code m} : ses ampoules s'emballent et des pièces tombent de son bac. */
    private void payOut(int m) {
        partyTime = 1.5f;
        float trayX = gridX(MACHINE_X + m * MACHINE_STEP + 16);
        float trayY = gridY(MACHINE_TOP + TRAY_OFFSET_Y);
        float floor = gridY(MACHINE_BASE + 8);
        for (int i = 0; i < JACKPOT_COINS; i++) {
            coins.drop(trayX + MathUtils.random(-10f, 10f) * gridScaleX / 4f, trayY - 80f, floor, false);
        }
    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        neonCoinTexture.dispose();
        neonBarTexture.dispose();
        bulbsATexture.dispose();
        bulbsBTexture.dispose();
        symbolsTexture.dispose();
        coinTexture.dispose();
    }

    /**
     * Néon (calque plein cadre) qui bourdonne légèrement et, de temps en
     * temps, grésille : il s'éteint et se rallume plusieurs fois très vite.
     */
    private static final class Neon extends Image {

        private static final float FLICKER_CHANCE = 0.3f;   // grésillements par seconde, en moyenne

        private float time = MathUtils.random(10f);
        private float flicker;   // temps restant du grésillement en cours

        Neon(Texture texture) {
            super(new TextureRegionDrawable(new TextureRegion(texture)));
            setTouchable(Touchable.disabled);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            time += delta;
            if (flicker > 0f) {
                flicker -= delta;
            } else if (MathUtils.randomBoolean(FLICKER_CHANCE * delta)) {
                flicker = MathUtils.random(0.15f, 0.45f);
            }
            float hum = 0.9f + 0.1f * MathUtils.sin(time * 9f);
            boolean off = flicker > 0f && MathUtils.randomBoolean(0.5f);
            getColor().a = off ? 0.15f : hum;
        }
    }

    /** Poussière dorée qui flotte lentement dans les cônes de lumière des lampes, en scintillant. */
    private final class Dust extends Actor {

        private static final class Mote {
            int   lamp;
            float x, y, vx, vy, phase;   // en coordonnées de la grille (y compté depuis le haut)
        }

        private final List<Mote>    motes = new ArrayList<>();
        private final Color         tint  = Color.valueOf("fff0c0ff");
        /** Un pixel clair : coin du fond crème des mini-symboles, teinté en doré. */
        private final TextureRegion pixel = new TextureRegion(symbolsTexture, 1, 0, 1, 1);
        private float            time;

        Dust() {
            setTouchable(Touchable.disabled);
            for (int lamp = 0; lamp < LAMPS.length; lamp++) {
                for (int i = 0; i < DUST_PER_LAMP; i++) {
                    Mote mote = new Mote();
                    mote.lamp = lamp;
                    respawn(mote);
                    motes.add(mote);
                }
            }
        }

        /** Replace une poussière au hasard dans le cône de sa lampe. */
        private void respawn(Mote mote) {
            float t = MathUtils.random(0.1f, 0.9f);
            mote.y     = CONE_TOP + t * (CONE_BOTTOM - CONE_TOP);
            mote.x     = LAMPS[mote.lamp] + MathUtils.random(-1f, 1f) * (4f + CONE_SPREAD * t) * 0.8f;
            mote.vx    = MathUtils.random(-2f, 2f);
            mote.vy    = MathUtils.random(-1.5f, 1.5f);
            mote.phase = MathUtils.random(MathUtils.PI2);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            time += delta;
            for (Mote mote : motes) {
                mote.x += mote.vx * delta;
                mote.y += mote.vy * delta;
                float t = (mote.y - CONE_TOP) / (CONE_BOTTOM - CONE_TOP);
                float half = 4f + CONE_SPREAD * t;
                if (t < 0.05f || t > 0.95f || Math.abs(mote.x - LAMPS[mote.lamp]) > half) respawn(mote);
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            float r = batch.getColor().r, g = batch.getColor().g, b = batch.getColor().b, a = batch.getColor().a;
            for (Mote mote : motes) {
                float t     = (mote.y - CONE_TOP) / (CONE_BOTTOM - CONE_TOP);
                float alpha = (0.35f + 0.35f * MathUtils.sin(time * 2.5f + mote.phase)) * (1f - t) * parentAlpha;
                batch.setColor(tint.r, tint.g, tint.b, alpha);
                batch.draw(pixel, gridX(mote.x), gridY(mote.y), gridScaleX, gridScaleY);
            }
            batch.setColor(r, g, b, a);
        }
    }
}
