package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Bingo du Triple Sept (proposition « supernova ») : trois « 7 » arrivent des
 * bords de l'écran et tournent autour du centre de la table de plus en plus
 * vite et de plus en plus près, puis fusionnent dans un éclair blanc ; un
 * soleil doré naît, ses rayons tournent lentement et le Triple Sept géant
 * apparaît en son cœur (traînées, ondes de choc, pièces et feux d'artifice
 * lancés par {@link JackpotCelebration}, qui suit {@link #sevenPosition}).
 */
public class SupernovaScene extends BingoScene {

    /** Les trois « 7 » fusionnent. */
    public static final float MERGE_TIME = 1.3f;

    private static final float START_RADIUS = 470f;
    private static final float SEVEN_SIZE   = 160f;
    private static final float TRIPLE_SIZE  = 330f;
    private static final int   RAYS         = 16;
    private static final float RAY_LENGTH   = 900f;

    private final Image[] sevens = new Image[3];
    private final Vector2[] positions = {new Vector2(), new Vector2(), new Vector2()};
    private final Image[] rays = new Image[RAYS];
    private final Image core, tripleSeven;
    private float centerX, centerY;

    /**
     * @param seven       image du Sept, pour les trois qui tournoient
     * @param tripleSeven image du Triple Sept, au cœur du soleil
     */
    public SupernovaScene(TextureRegion seven, TextureRegion tripleSeven) {
        TextureRegion pixel = new TextureRegion(texture(whitePixel()));
        for (int i = 0; i < RAYS; i++) {
            rays[i] = image(pixel, 1f);
            rays[i].setSize(RAY_LENGTH, i % 2 == 0 ? 46f : 22f);
            rays[i].setOrigin(0f, rays[i].getHeight() / 2f);
            addActor(rays[i]);
        }
        core = image(texture(halo(64, hex("fff3a8"))));
        core.setOrigin(core.getWidth() / 2f, core.getHeight() / 2f);
        addActor(core);
        for (int i = 0; i < 3; i++) {
            sevens[i] = image(seven, SEVEN_SIZE / seven.getRegionWidth());
            sevens[i].setOrigin(sevens[i].getWidth() / 2f, sevens[i].getHeight() / 2f);
            addActor(sevens[i]);
        }
        this.tripleSeven = image(tripleSeven, TRIPLE_SIZE / tripleSeven.getRegionWidth());
        this.tripleSeven.setOrigin(this.tripleSeven.getWidth() / 2f, this.tripleSeven.getHeight() / 2f);
        addActor(this.tripleSeven);
    }

    /** @return la position (Stage) du « 7 » {@code index} (0 à 2) pendant sa course. */
    public Vector2 sevenPosition(int index) { return positions[index]; }

    @Override
    protected void start(float x, float y) {
        centerX = x;
        centerY = y;
        for (Image ray : rays) ray.setVisible(false);
        core.setVisible(false);
        tripleSeven.setVisible(false);
        for (Image seven : sevens) {
            seven.setVisible(true);
            seven.setColor(Color.WHITE);
        }
        update(0f);
    }

    @Override
    protected void update(float delta) {
        if (time < MERGE_TIME) {
            // Spirale : le rayon fond, la rotation accélère.
            float progress = time / MERGE_TIME;
            float radius = START_RADIUS * (1f - Interpolation.pow2In.apply(progress) * 0.85f - progress * 0.15f);
            float spin = 140f + 900f * progress * progress;   // degrés, cumulés approximativement
            for (int i = 0; i < 3; i++) {
                float angle = 90f + i * 120f + spin * progress;
                positions[i].set(centerX + MathUtils.cosDeg(angle) * radius * 1.25f,
                    centerY + MathUtils.sinDeg(angle) * radius * 0.75f);
                placeOrigin(sevens[i], positions[i].x, positions[i].y);
                sevens[i].setRotation(angle * 2f);
                sevens[i].setScale(1.15f - 0.35f * progress);
                // Ils rougeoient de plus en plus à l'approche.
                float heat = progress * progress;
                sevens[i].setColor(1f, 1f - 0.2f * heat, 1f - 0.6f * heat, 1f);
            }
            return;
        }
        for (Image seven : sevens) seven.setVisible(false);
        for (Vector2 position : positions) position.set(centerX, centerY);

        float since = time - MERGE_TIME;
        float grow = Interpolation.swingOut.apply(Math.min(1f, since / 0.35f));
        // Rayons qui jaillissent puis tournent lentement, en alternant or et blanc.
        for (int i = 0; i < RAYS; i++) {
            Image ray = rays[i];
            ray.setVisible(true);
            ray.setScaleX(Math.min(1f, since / 0.25f));
            ray.setRotation(i * 360f / RAYS + since * 25f);
            ray.setPosition(centerX, centerY - ray.getHeight() / 2f);
            float pulse = 0.22f + 0.1f * MathUtils.sin(since * 8f + i);
            if (i % 2 == 0) ray.setColor(1f, 0.85f, 0.3f, pulse);
            else            ray.setColor(1f, 1f, 0.9f, pulse * 0.8f);
        }
        core.setVisible(true);
        core.setScale(grow * (2.4f + 0.2f * MathUtils.sin(since * 12f)));
        core.getColor().a = 0.8f;
        placeOrigin(core, centerX, centerY);
        tripleSeven.setVisible(true);
        tripleSeven.setScale(grow * (1f + 0.04f * MathUtils.sin(since * 10f)));
        tripleSeven.setRotation(MathUtils.sin(since * 6f) * 4f);
        placeOrigin(tripleSeven, centerX, centerY);
    }
}
