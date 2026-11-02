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
 * Bingo de l'Ancre : la table se couvre d'une mer bleue qui ondule, une ancre
 * géante tombe du haut de l'écran au bout de sa chaîne, plonge dans l'eau à
 * {@link #SPLASH_TIME}, rebondit sur sa chaîne puis se balance (les gerbes
 * d'eau, les vagues en anneaux et les Ancres projetées sont lancées par
 * {@link JackpotCelebration}, qui suit {@link #splash}).
 */
public class AnchorDropScene extends BingoScene {

    /** L'ancre touche l'eau. */
    public static final float SPLASH_TIME = 0.7f;

    private static final float ANCHOR_SIZE = 300f;
    private static final int LINKS = 14;
    private static final float LINK_STEP = 44f;

    private final Image sea, anchor;
    private final Image[] links = new Image[LINKS];
    private final Vector2 rest = new Vector2(), splash = new Vector2(), at = new Vector2();
    private float top;

    /** @param anchorIcon image du symbole Ancre */
    public AnchorDropScene(TextureRegion anchorIcon) {
        sea = image(new TextureRegion(texture(sea())), 1f);
        addActor(sea);
        TextureRegion link = new TextureRegion(texture(link()));
        for (int i = 0; i < LINKS; i++) {
            links[i] = image(link, 6f);
            links[i].setOrigin(links[i].getWidth() / 2f, links[i].getHeight() / 2f);
            addActor(links[i]);
        }
        anchor = image(anchorIcon, ANCHOR_SIZE / anchorIcon.getRegionWidth());
        anchor.setOrigin(anchor.getWidth() / 2f, anchor.getHeight());   // l'anneau, en haut
        addActor(anchor);
    }

    /** @return l'endroit (Stage) où l'ancre plonge. */
    public Vector2 splash() { return splash; }

    @Override
    protected void start(float x, float y) {
        rest.set(x, y);
        float width = getStage() != null ? getStage().getWidth() : 1920f;
        top = getStage() != null ? getStage().getHeight() : 1080f;
        sea.setSize(width, y + 40f);
        sea.setPosition(0f, 0f);
        splash.set(x, y - ANCHOR_SIZE * 0.6f);
        update(0f);
    }

    @Override
    protected void update(float delta) {
        // La mer monte doucement et ondule.
        sea.setColor(0.3f, 0.6f, 1f, 0.38f * Math.min(1f, time / 0.4f));
        sea.setScaleY(1f + 0.03f * MathUtils.sin(time * 4f));

        // Chute libre, puis petit rebond sur la chaîne, puis balancement.
        float hang;     // hauteur de l'anneau au-dessus de sa place finale
        float swing = 0f;
        if (time < SPLASH_TIME) {
            hang = (top + ANCHOR_SIZE) * (1f - Interpolation.pow2In.apply(time / SPLASH_TIME));
        } else {
            float since = time - SPLASH_TIME;
            hang = -60f * MathUtils.sin(Math.min(MathUtils.PI, since * 9f)) * Math.max(0f, 1f - since * 2f);
            swing = MathUtils.sin(since * 4f) * 9f * Math.min(1f, since * 2f);
        }
        float ringY = rest.y + ANCHOR_SIZE * 0.4f + hang;
        at.set(rest.x, ringY);
        anchor.setRotation(swing);
        placeOrigin(anchor, at.x, at.y);

        // La chaîne, maillon par maillon, de l'anneau jusqu'en haut.
        for (int i = 0; i < LINKS; i++) {
            Image link = links[i];
            float y = ringY + 20f + i * LINK_STEP;
            float x = rest.x - MathUtils.sinDeg(swing) * (i + 1) * LINK_STEP * 0.3f;
            link.setVisible(y < top + LINK_STEP);
            link.setRotation(i % 2 == 0 ? 0f : 90f);
            placeOrigin(link, x, y);
        }
    }

    /** @return la mer : un dégradé bleu, plus clair en surface (étiré à la largeur de l'écran). */
    private static Pixmap sea() {
        Pixmap pixmap = pixmap(1, 24);
        for (int y = 0; y < 24; y++) {
            float t = y / 23f;
            pixmap.setColor(0.55f + 0.45f * (1f - t), 0.8f + 0.2f * (1f - t), 1f, y == 0 ? 1f : 0.5f + 0.5f * t);
            pixmap.drawPixel(0, y);
        }
        return pixmap;
    }

    /** @return un maillon de chaîne en fer. */
    private static Pixmap link() {
        Pixmap pixmap = pixmap(6, 9);
        Color iron = hex("6a7080"), light = hex("a0a8b8");
        fillShape(pixmap, (x, y) -> {
            boolean outer = (x >= 0 && x <= 5) && (y >= 0 && y <= 8) && !((x == 0 || x == 5) && (y == 0 || y == 8));
            boolean inner = x >= 2 && x <= 3 && y >= 2 && y <= 6;
            return outer && !inner;
        }, (x, y) -> x < 2 ? light : iron);
        return pixmap;
    }
}
