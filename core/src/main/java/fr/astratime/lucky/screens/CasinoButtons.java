package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

/**
 * Fabrique des boutons au thème casino : fond sombre, liseré doré, police pixel
 * art. La largeur de chaque bouton s'adapte à son texte ; MIN_WIDTH n'est qu'un
 * plancher. Possède la police et les textures partagées par tous les boutons.
 */
class CasinoButtons implements Disposable {

    static final float HEIGHT = 60f;

    private static final float  MIN_WIDTH    = 150f;
    private static final String FONT_PATH    = "fonts/Jersey10-Regular.ttf";
    private static final int    FONT_SIZE    = 30;
    private static final int    BORDER_PX    = 3;
    private static final float  TEXT_PADDING = 20f; // marge horizontale de chaque côté du texte
    private static final Color  GOLD         = Color.GOLDENROD;

    private final BitmapFont font;
    private final Texture    upTexture;
    private final Texture    downTexture;
    private final Texture    disabledTexture;

    CasinoButtons() {
        font            = buildFont();
        upTexture       = makeTexture(Color.valueOf("1a1a1aff"), GOLD);
        downTexture     = makeTexture(Color.valueOf("4a0000ff"), GOLD);
        disabledTexture = makeTexture(Color.valueOf("2a2a2aff"), Color.valueOf("6b5a2eff"));
    }

    /**
     * @param text    texte du bouton (sa largeur s'y adapte)
     * @param sound   bruitage joué au clic
     * @param onClick action déclenchée au clic (jamais quand le bouton est désactivé)
     */
    TextButton create(String text, Sound sound, Runnable onClick) {
        TextButton button = new TextButton(text, buildStyle());
        button.setSize(widthFor(text), HEIGHT);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sound.play();
                onClick.run();
            }
        });
        return button;
    }

    /** Largeur d'un bouton adaptée à son texte (avec une marge), jamais plus petite que MIN_WIDTH. */
    private float widthFor(String text) {
        GlyphLayout layout = new GlyphLayout(font, text);
        return Math.max(MIN_WIDTH, layout.width + TEXT_PADDING * 2);
    }

    private TextButton.TextButtonStyle buildStyle() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font              = font;
        style.up                = new TextureRegionDrawable(new TextureRegion(upTexture));
        style.down              = new TextureRegionDrawable(new TextureRegion(downTexture));
        style.disabled          = new TextureRegionDrawable(new TextureRegion(disabledTexture));
        style.disabledFontColor = Color.valueOf("8a8a8aff");
        return style;
    }

    /** Génère la police pixel art dorée des boutons, à partir de la police TrueType du thème. */
    private static BitmapFont buildFont() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_PATH));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size  = FONT_SIZE;
        parameter.color = GOLD;
        BitmapFont generated = generator.generateFont(parameter);
        generator.dispose(); // le générateur ne sert plus une fois la police créée
        return generated;
    }

    /** Texture de bouton : fond plein entouré d'un liseré. */
    private static Texture makeTexture(Color fill, Color border) {
        int w = (int) MIN_WIDTH;
        int h = (int) HEIGHT;
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setColor(border);
        pixmap.fill();
        pixmap.setColor(fill);
        pixmap.fillRectangle(BORDER_PX, BORDER_PX, w - BORDER_PX * 2, h - BORDER_PX * 2);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        font.dispose();
        upTexture.dispose();
        downTexture.dispose();
        disabledTexture.dispose();
    }
}
