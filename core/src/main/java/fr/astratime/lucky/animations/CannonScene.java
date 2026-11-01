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
 * Bingo du Sept (proposition « canon ») : un canon de pirate roule jusqu'au
 * bas de la table, lève son fût, sa mèche grésille, puis il tire un « 7 »
 * géant en feu qui monte en tournoyant et éclate au-dessus de la table (les
 * étincelles de la mèche, la fumée et l'explosion sont lancées par
 * {@link JackpotCelebration}, qui suit {@link #fusePosition} et {@link #shotPosition}).
 */
public class CannonScene extends BingoScene {

    /** Le canon est en place, la mèche s'allume. */
    public static final float FUSE_TIME  = 0.45f;
    /** Le coup part. */
    public static final float FIRE_TIME  = 1.0f;
    /** Le « 7 » éclate au-dessus de la table. */
    public static final float BURST_TIME = 1.5f;

    private static final float ROLL_FROM = -520f;    // départ du canon, relatif à sa place
    private static final float AIM       = 62f;      // angle du fût une fois levé
    private static final float RECOIL    = 70f;
    private static final float SHOT_SIZE = 190f;     // largeur du « 7 » tiré, à l'éclatement

    private static final int BARREL_W = 40, BARREL_H = 16;
    private static final int CARRIAGE_W = 34, CARRIAGE_H = 18;

    private final Image barrel, carriage, wheel, shot, glow;
    private final Vector2 base = new Vector2(), target = new Vector2();
    private final Vector2 muzzle = new Vector2(), fuse = new Vector2(), shotAt = new Vector2();

    /** @param seven image du symbole Sept */
    public CannonScene(TextureRegion seven) {
        carriage = image(texture(carriage()));
        wheel    = image(texture(wheel()));
        wheel.setOrigin(wheel.getWidth() / 2f, wheel.getHeight() / 2f);
        barrel   = image(texture(barrel()));
        barrel.setOrigin(10 * SCALE, BARREL_H * SCALE / 2f); // tourillon, vers l'arrière du fût
        glow = image(texture(halo(48, hex("ffb347"))));
        glow.setOrigin(glow.getWidth() / 2f, glow.getHeight() / 2f);
        shot = image(seven, SHOT_SIZE / seven.getRegionWidth());
        shot.setOrigin(shot.getWidth() / 2f, shot.getHeight() / 2f);
        addActor(barrel);
        addActor(carriage);
        addActor(wheel);
        addActor(glow);
        addActor(shot);
    }

    /** @return la position (Stage) du bout de la mèche, qui grésille avant le tir. */
    public Vector2 fusePosition() { return fuse; }

    /** @return la position (Stage) de la bouche du canon. */
    public Vector2 muzzlePosition() { return muzzle; }

    /** @return la position (Stage) du « 7 » tiré, puis celle de son éclatement. */
    public Vector2 shotPosition() { return shotAt; }

    /** Le canon s'arrête en {@code (x, y)} (centre de son affût) ; le « 7 » éclate en {@code target}. */
    public void aimAt(float targetX, float targetY) { target.set(targetX, targetY); }

    @Override
    protected void start(float x, float y) {
        base.set(x, y);
        shot.setVisible(false);
        glow.setVisible(false);
        shot.setColor(Color.WHITE);
        update(0f);
    }

    @Override
    protected void update(float delta) {
        // Le canon roule jusqu'à sa place, puis recule au tir et revient doucement.
        float offset;
        if (time < FUSE_TIME) {
            offset = ROLL_FROM * (1f - Interpolation.pow2Out.apply(time / FUSE_TIME));
        } else if (time < FIRE_TIME) {
            offset = 0f;
        } else {
            float since = time - FIRE_TIME;
            offset = since < 0.08f ? -RECOIL * since / 0.08f
                : -RECOIL * (1f - Interpolation.pow2Out.apply(Math.min(1f, (since - 0.08f) / 0.6f)));
        }
        float cx = base.x + offset, cy = base.y;
        float carriageW = CARRIAGE_W * SCALE, carriageH = CARRIAGE_H * SCALE;
        carriage.setPosition(cx - carriageW / 2f, cy - carriageH / 2f);
        placeOrigin(wheel, cx - carriageW * 0.12f, cy - carriageH * 0.28f);
        wheel.setRotation(-offset * 0.9f);

        // Le fût se lève pendant que la mèche brûle ; il tressaute au tir.
        float aim = time < FUSE_TIME ? 8f
            : MathUtils.lerp(8f, AIM, Interpolation.swingOut.apply(Math.min(1f, (time - FUSE_TIME) / 0.3f)));
        if (time >= FIRE_TIME && time < FIRE_TIME + 0.12f) aim += 6f * (1f - (time - FIRE_TIME) / 0.12f);
        float pivotX = cx + carriageW * 0.05f, pivotY = cy + carriageH * 0.3f;
        placeOrigin(barrel, pivotX, pivotY);
        barrel.setRotation(aim);
        float squash = time >= FIRE_TIME && time < FIRE_TIME + 0.15f ? 1f - 0.12f * (1f - (time - FIRE_TIME) / 0.15f) : 1f;
        barrel.setScale(squash, 2f - squash);
        float length = (BARREL_W - 10) * SCALE;
        muzzle.set(pivotX + MathUtils.cosDeg(aim) * length, pivotY + MathUtils.sinDeg(aim) * length);
        float back = 10 * SCALE;
        float fuseLeft = time < FIRE_TIME ? 1f - Math.max(0f, time - FUSE_TIME) / (FIRE_TIME - FUSE_TIME) : 0f;
        float fuseX = pivotX - MathUtils.cosDeg(aim) * back * 0.8f, fuseY = pivotY - MathUtils.sinDeg(aim) * back * 0.8f;
        fuse.set(fuseX - 10f - 30f * fuseLeft, fuseY + 16f + 30f * fuseLeft);

        // Le « 7 » file de la bouche vers le haut de la table en grossissant.
        if (time < FIRE_TIME || time >= BURST_TIME) {
            shot.setVisible(false);
            glow.setVisible(false);
            if (time >= BURST_TIME) shotAt.set(target);
            return;
        }
        float progress = (time - FIRE_TIME) / (BURST_TIME - FIRE_TIME);
        float eased = Interpolation.pow2Out.apply(progress);
        shotAt.set(MathUtils.lerp(muzzle.x, target.x, eased),
            MathUtils.lerp(muzzle.y, target.y, eased) + 90f * 4f * eased * (1f - eased));
        shot.setVisible(true);
        glow.setVisible(true);
        float scale = 0.35f + 0.65f * eased;
        shot.setScale(scale);
        shot.setRotation(35f * (1f - eased) + MathUtils.sin(time * 18f) * 8f * (1f - eased));
        placeOrigin(shot, shotAt.x, shotAt.y);
        glow.setScale(scale * (1.4f + 0.2f * MathUtils.sin(time * 40f)));
        placeOrigin(glow, shotAt.x, shotAt.y);
    }

    /** @return le fût en fonte, bouche à droite, cerclé de laiton. */
    private static Pixmap barrel() {
        Pixmap pixmap = pixmap(BARREL_W, BARREL_H);
        Color iron = hex("3c3f4a"), light = hex("6a7080"), dark = hex("24262e"), brass = hex("d9a640");
        // Le fût s'évase vers la culasse (à gauche).
        Shape shape = (x, y) -> {
            float half = x < 6 ? 7.5f - Math.abs(x - 3) * 0.3f : 6.5f - (x - 6) * 1.5f / 32f;
            if (x >= BARREL_W - 3) half += 1f; // bourrelet de la bouche
            return Math.abs(y + 0.5f - BARREL_H / 2f) <= half;
        };
        fillShape(pixmap, shape, (x, y) -> {
            if (x == 7 || x == 8 || x == 22 || x == BARREL_W - 3) return brass;
            if (y < BARREL_H / 2 - 3) return light;
            if (y > BARREL_H / 2 + 3) return dark;
            return iron;
        });
        // Bouche, vue de biais : un trou sombre.
        pixmap.setColor(hex("0a0a0e"));
        pixmap.fillRectangle(BARREL_W - 2, BARREL_H / 2 - 3, 1, 6);
        return pixmap;
    }

    /** @return l'affût en bois, posé sur la table. */
    private static Pixmap carriage() {
        Pixmap pixmap = pixmap(CARRIAGE_W, CARRIAGE_H);
        Shape shape = (x, y) -> {
            int top = 4 + Math.max(0, 14 - x) / 3;   // l'arrière descend
            return y >= top && y < CARRIAGE_H - 4 && x >= 1 && x < CARRIAGE_W - 1;
        };
        fillShape(pixmap, shape, (x, y) -> y % 4 == 0 ? Palette.WOOD_DARK
            : y < 8 ? Palette.WOOD_LIGHT : Palette.WOOD);
        // Ferrures.
        pixmap.setColor(Palette.IRON);
        pixmap.fillRectangle(CARRIAGE_W - 8, 5, 2, CARRIAGE_H - 9);
        pixmap.fillRectangle(6, 9, 2, CARRIAGE_H - 13);
        return pixmap;
    }

    /** @return une roue à rayons. */
    private static Pixmap wheel() {
        int size = 15;
        Pixmap pixmap = pixmap(size, size);
        fillOutlinedCircle(pixmap, size / 2, size / 2, size / 2, Palette.WOOD);
        pixmap.setColor(Palette.WOOD_DARK);
        pixmap.fillCircle(size / 2, size / 2, size / 2 - 3);
        pixmap.setColor(Palette.WOOD_LIGHT);
        pixmap.drawLine(size / 2, 2, size / 2, size - 3);
        pixmap.drawLine(2, size / 2, size - 3, size / 2);
        pixmap.drawLine(4, 4, size - 5, size - 5);
        pixmap.drawLine(4, size - 5, size - 5, 4);
        pixmap.setColor(Palette.IRON);
        pixmap.fillCircle(size / 2, size / 2, 1);
        return pixmap;
    }
}
