package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Bingo du Jeton d'or : des jetons de casino tombent un à un et s'empilent en
 * une tour qui grandit au milieu de la table ; le Jeton d'or géant se pose au
 * sommet à {@link #CROWN_TIME}, la tour vacille puis s'écroule en cascade à
 * {@link #TOPPLE_TIME} (les jetons qui roulent, les ondes et les pièces sont
 * lancés par {@link JackpotCelebration}, qui suit {@link #top}).
 */
public class ChipTowerScene extends BingoScene {

    /** Le Jeton d'or se pose au sommet. */
    public static final float CROWN_TIME  = 1.15f;
    /** La tour s'écroule. */
    public static final float TOPPLE_TIME = 1.55f;

    private static final int CHIPS = 12;
    private static final float CHIP_SCALE = 7f;
    private static final int CHIP_W = 24, CHIP_H = 7;
    private static final float STEP = 4 * CHIP_SCALE;     // hauteur d'un jeton dans la pile
    private static final float FALL = 0.18f;
    private static final float GOLD_SIZE = 220f;

    private final Image[] chips = new Image[CHIPS];
    private final Image gold;
    private final Vector2 base = new Vector2(), top = new Vector2();

    /** @param goldChip image du symbole Jeton d'or */
    public ChipTowerScene(TextureRegion goldChip) {
        Color[] colors = {hex("d8343c"), hex("2a6ad8"), hex("2aa860"), hex("1c1c24"), hex("8a3ad8")};
        for (int i = 0; i < CHIPS; i++) {
            chips[i] = image(new TextureRegion(texture(chip(colors[i % colors.length]))), CHIP_SCALE);
            chips[i].setOrigin(chips[i].getWidth() / 2f, 0f);
            addActor(chips[i]);
        }
        gold = image(goldChip, GOLD_SIZE / goldChip.getRegionWidth());
        gold.setOrigin(gold.getWidth() / 2f, 0f);
        addActor(gold);
    }

    /** @return le sommet (Stage) de la tour. */
    public Vector2 top() { return top; }

    /** @return l'instant où le jeton {@code index} se pose. */
    public static float landTime(int index) { return 0.15f + index * (CROWN_TIME - 0.25f) / CHIPS; }

    @Override
    protected void start(float x, float y) {
        base.set(x, y);
        update(0f);
    }

    @Override
    protected void update(float delta) {
        float lean = time < CROWN_TIME + 0.1f ? 0f
            : time < TOPPLE_TIME ? MathUtils.sin((time - CROWN_TIME) * 30f) * 3f * (time - CROWN_TIME) / (TOPPLE_TIME - CROWN_TIME)
            : 0f;
        float fallen = time < TOPPLE_TIME ? 0f : time - TOPPLE_TIME;
        for (int i = 0; i < CHIPS; i++) {
            Image chip = chips[i];
            float land = landTime(i);
            float progress = (time - (land - FALL)) / FALL;
            chip.setVisible(progress >= 0f);
            float restY = base.y + i * STEP;
            float y = progress < 1f ? restY + 700f * (1f - Interpolation.pow2In.apply(Math.max(0f, progress))) : restY;
            float x = base.x + MathUtils.sinDeg(lean) * i * STEP;
            if (fallen > 0f) {
                // Cascade : chaque jeton part sur le côté, du haut vers le bas.
                float delay = (CHIPS - 1 - i) * 0.02f, since = Math.max(0f, fallen - delay);
                float side = i % 2 == 0 ? 1f : -1f;
                x += side * (120f + i * 22f) * since * 3f;
                y += (300f - 1400f * since) * since;
                chip.setRotation(side * since * 720f);
            } else {
                chip.setRotation(lean);
            }
            placeOrigin(chip, x, Math.max(y, base.y - 260f));
        }
        top.set(base.x + MathUtils.sinDeg(lean) * CHIPS * STEP, base.y + CHIPS * STEP);
        float crown = (time - (CROWN_TIME - FALL * 1.4f)) / (FALL * 1.4f);
        gold.setVisible(crown >= 0f && fallen < 0.6f);
        float goldY = crown < 1f ? top.y + 800f * (1f - Interpolation.pow2In.apply(Math.max(0f, crown))) : top.y;
        if (fallen > 0f) goldY += 600f * fallen - 1200f * fallen * fallen;
        gold.setRotation(fallen > 0f ? fallen * 600f : lean);
        gold.setScale(time >= CROWN_TIME && time < CROWN_TIME + 0.12f ? 1.1f : 1f);
        gold.getColor().a = fallen > 0f ? Math.max(0f, 1f - fallen / 0.6f) : 1f;
        placeOrigin(gold, top.x, goldY);
    }

    /** @return un jeton de casino vu de biais : tranche striée de blanc. */
    private static Pixmap chip(Color color) {
        Pixmap pixmap = pixmap(CHIP_W, CHIP_H);
        Color light = new Color(color).lerp(Color.WHITE, 0.35f), white = hex("f4f0e6");
        fillShape(pixmap, (x, y) -> {
            float u = (x + 0.5f - CHIP_W / 2f) / (CHIP_W / 2f);
            float cap = 1f - u * u;
            return y >= 2f - cap * 2f && y <= 4f + cap * 2.5f;
        }, (x, y) -> y <= 2 ? light : (x / 3) % 2 == 0 ? white : color);
        return pixmap;
    }
}
