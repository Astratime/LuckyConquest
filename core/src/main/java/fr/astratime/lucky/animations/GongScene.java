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
 * Bingo du Gong (rouleau du Temple du Destin) : un grand gong de bronze,
 * suspendu à un portique rouge, monte du bas de la table ; un maillet
 * s'élance et le frappe à {@link #STRIKE_TIME}. Le gong tremble et brille,
 * le symbole Gong s'envole de son centre (les ondes, les jets de symboles
 * et les pièces sont lancés par {@link JackpotCelebration}, qui suit {@link #center}).
 */
public class GongScene extends BingoScene {

    /** Le portique est en place. */
    public static final float RISE_TIME = 0.55f;
    /** Le maillet frappe le gong. */
    public static final float STRIKE_TIME = 1.05f;

    private static final int FRAME_W = 64, FRAME_H = 56, MALLET_W = 10, MALLET_H = 30;
    /** Centre du disque dans l'image du portique (pixels, depuis le bas). */
    private static final int DISC_X = 32, DISC_Y = 24;

    private final Image frame, mallet, glow, gong;
    private final Vector2 base = new Vector2(), center = new Vector2();
    private float riseFrom;

    /** @param gongIcon image du symbole Gong */
    public GongScene(TextureRegion gongIcon) {
        glow = image(texture(halo(64, hex("ffd27a"))));
        glow.setOrigin(glow.getWidth() / 2f, glow.getHeight() / 2f);
        addActor(glow);
        frame = image(texture(frame()));
        frame.setOrigin(frame.getWidth() / 2f, 0f);
        addActor(frame);
        mallet = image(texture(mallet()));
        mallet.setOrigin(mallet.getWidth() / 2f, 0f);
        addActor(mallet);
        gong = image(gongIcon, 160f / gongIcon.getRegionWidth());
        gong.setOrigin(gong.getWidth() / 2f, gong.getHeight() / 2f);
        addActor(gong);
    }

    /** @return le centre (Stage) du disque du gong, d'où partent les ondes. */
    public Vector2 center() { return center; }

    @Override
    protected void start(float x, float y) {
        base.set(x, y);
        riseFrom = y - frame.getHeight() - 40f;
        update(0f);
    }

    @Override
    protected void update(float delta) {
        float rise = Interpolation.pow3Out.apply(Math.min(1f, time / RISE_TIME));
        float y = MathUtils.lerp(riseFrom, base.y, rise);
        boolean struck = time >= STRIKE_TIME;
        float since = time - STRIKE_TIME;
        // Le portique tremble après le coup, de moins en moins.
        float shake = struck ? MathUtils.sin(since * 70f) * 6f * Math.max(0f, 1f - since / 1.2f) : 0f;
        placeOrigin(frame, base.x + shake, y);
        center.set(base.x + shake + (DISC_X - FRAME_W / 2f) * SCALE, y + DISC_Y * SCALE);

        // Le maillet pivote depuis sa poignée, à droite du gong : il recule, puis s'abat.
        float swing;
        if (time < RISE_TIME) swing = -10f;
        else if (time < STRIKE_TIME) {
            float t = (time - RISE_TIME) / (STRIKE_TIME - RISE_TIME);
            swing = t < 0.6f ? MathUtils.lerp(-10f, -70f, Interpolation.pow2Out.apply(t / 0.6f))
                : MathUtils.lerp(-70f, 40f, Interpolation.pow3In.apply((t - 0.6f) / 0.4f));
        } else swing = 40f - 25f * Math.min(1f, since / 0.4f);
        mallet.setRotation(swing);
        placeOrigin(mallet, center.x + 21 * SCALE, center.y - 22 * SCALE);

        gong.setVisible(struck);
        glow.getColor().a = struck ? 0.7f * Math.max(0.3f, 1f - since / 1.4f) : 0f;
        if (!struck) return;
        float pop = Interpolation.swingOut.apply(Math.min(1f, since / 0.35f));
        gong.setScale(0.5f + 0.7f * pop);
        gong.setRotation(MathUtils.sin(since * 20f) * 6f);
        placeOrigin(gong, center.x, center.y + 260f * Interpolation.pow2Out.apply(Math.min(1f, since / 0.6f)));
        glow.setScale(2.5f + 2.5f * pop + 0.3f * MathUtils.sin(since * 30f));
        placeOrigin(glow, center.x, center.y);
    }

    /** @return le portique rouge (deux poteaux, deux linteaux) et son gong de bronze suspendu par deux cordes. */
    private static Pixmap frame() {
        Pixmap pixmap = pixmap(FRAME_W, FRAME_H);
        Color red = hex("b02a24"), redDark = hex("6e1814"), rope = hex("d8c08a"), bronze = hex("c8902a"),
            bronzeDark = hex("7a5014"), shine = hex("ffe39a"), boss = Palette.GOLD;
        int discY = FRAME_H - 1 - DISC_Y;
        // Poteaux.
        fillShape(pixmap, (x, y) -> (x >= 4 && x <= 8 || x >= 55 && x <= 59) && y >= 6 && y < FRAME_H,
            (x, y) -> x == 5 || x == 56 ? red : y > FRAME_H - 4 ? redDark : red);
        // Linteau supérieur incurvé et linteau inférieur.
        fillShape(pixmap, (x, y) -> y >= 1 + (Math.abs(x - 32) < 20 ? 1 : 0) && y <= 5 && x >= 0 && x <= 63,
            (x, y) -> y <= 2 ? hex("2a1a18") : red);
        fillShape(pixmap, (x, y) -> y >= 9 && y <= 11 && x >= 2 && x <= 61, (x, y) -> redDark);
        // Cordes.
        pixmap.setColor(rope);
        pixmap.drawLine(22, 12, 26, discY - 14);
        pixmap.drawLine(42, 12, 38, discY - 14);
        // Disque de bronze : bord sombre, anneaux, bossage d'or au centre.
        fillShape(pixmap, (x, y) -> dist(x, y, DISC_X, discY) <= 17, (x, y) -> {
            float d = dist(x, y, DISC_X, discY);
            if (d <= 3.5f) return boss;
            if (d > 14.5f) return bronzeDark;
            if (Math.abs(d - 9f) < 0.8f || Math.abs(d - 12.5f) < 0.6f) return bronzeDark;
            return x - DISC_X + (y - discY) < -10 && d > 5f ? shine : bronze;
        });
        return pixmap;
    }

    /** @return le maillet : manche de bois, tête ronde enveloppée de tissu rouge (la poignée en bas). */
    private static Pixmap mallet() {
        Pixmap pixmap = pixmap(MALLET_W, MALLET_H);
        Color wood = hex("8a5a2a"), cloth = hex("d03a30"), clothDark = hex("8a1e1a");
        fillShape(pixmap, (x, y) -> x >= 4 && x <= 5 && y >= 7, (x, y) -> wood);
        fillShape(pixmap, (x, y) -> dist(x, y, 4.5f, 4f) <= 4.4f, (x, y) -> x < 4 ? cloth : clothDark);
        return pixmap;
    }

    private static float dist(int x, int y, float cx, float cy) {
        float dx = x + 0.5f - cx - 0.5f, dy = y + 0.5f - cy - 0.5f;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
