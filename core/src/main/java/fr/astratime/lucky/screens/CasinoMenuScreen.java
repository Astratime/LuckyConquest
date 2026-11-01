package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.assets.BackgroundMusic;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.views.CasinoButtons;
import fr.astratime.lucky.views.MenuDecor;
import fr.astratime.lucky.views.MinimumScreenViewport;

import java.util.function.Supplier;

/**
 * Écran de menu devant le décor du casino assombri : Tour des épreuves,
 * Exploration, Cinématiques, Boutique, Table du croupier.
 *
 * Il porte tout ce que ces écrans ont en commun : la scène et sa taille
 * minimale, le décor et son voile, le titre, la musique du menu, le fondu
 * d'ouverture et de sortie, le clavier (désactivé pendant la sortie) et le
 * cycle de vie. Un écran n'écrit que son contenu ({@link #layoutContent}),
 * ses touches ({@link #onKey}) et ses ressources propres.
 *
 * Utilisation dans le constructeur d'un écran : ajouter ses acteurs à
 * {@link #stage}, puis appeler {@link #reveal()} (le fondu passe devant tout).
 */
abstract class CasinoMenuScreen extends ScreenAdapter {

    static final float MIN_WIDTH  = 1600f;
    static final float MIN_HEIGHT = 1080f;
    static final float MARGIN     = 60f;
    static final float FADE_TIME  = 0.4f;
    /** Opacité du voile noir posé sur le décor. */
    static final float VEIL_ALPHA = 0.6f;


    protected final LuckyGame       luckyGame;
    protected final SharedAssets    assets;
    protected final Stage           stage;
    protected final HudTextures     hud;
    protected final CasinoButtons   buttons;
    protected final Sound           clickSound;
    protected final Sound           hoverSound;
    protected final BackgroundMusic music;
    protected final MenuDecor       decor = new MenuDecor();
    protected final Image           veil;
    protected final Label           title;
    /** Fondu au noir : à l'ouverture, à la sortie, et devant le guide du Croupier. */
    protected final Image           fade;
    private   final BitmapFont      titleFont;

    /** L'écran est en train de se fermer : plus de clic ni de touche. */
    protected boolean leaving;

    /**
     * @param titleText texte du titre (déjà traduit)
     * @param titleSize taille de sa police
     * @param veilAlpha opacité du voile sur le décor
     */
    protected CasinoMenuScreen(LuckyGame luckyGame, String titleText, int titleSize, float veilAlpha) {
        this.luckyGame  = luckyGame;
        this.assets     = luckyGame.getAssets();
        this.hud        = assets.hud;
        this.buttons    = assets.buttons;
        this.clickSound = assets.clickSound;
        this.hoverSound = assets.hoverSound;
        this.stage      = new Stage(new MinimumScreenViewport(MIN_WIDTH, MIN_HEIGHT), luckyGame.getBatch());
        this.music      = assets.menuMusic; // partagée : elle continue d'un menu à l'autre

        TextureRegionDrawable pixel = new TextureRegionDrawable(new TextureRegion(hud.pixel));
        veil = new Image(pixel);
        veil.setColor(0f, 0f, 0f, veilAlpha);
        veil.setTouchable(Touchable.disabled);
        titleFont = Fonts.jersey(titleSize, Palette.GOLD, 6f, Palette.TEXT_SHADE);
        title = new Label(titleText, new Label.LabelStyle(titleFont, Color.WHITE));
        title.pack();
        fade = new Image(pixel);
        fade.setColor(Color.BLACK);
        fade.setTouchable(Touchable.disabled);

        stage.addActor(decor);
        stage.addActor(veil);
        stage.addActor(title);
    }

    /** Fin du constructeur : le fondu passe devant tout le contenu et s'ouvre. */
    protected final void reveal() {
        stage.addActor(fade);
        fade.addAction(Actions.fadeOut(FADE_TIME));
    }

    // -------------------------------------------------------------------------
    // Sortie
    // -------------------------------------------------------------------------

    /** Fondu au noir, puis {@code next} (au calme, hors du rendu en cours). Rien si l'écran se ferme déjà. */
    protected void fadeOutThen(Runnable next) {
        if (leaving) return;
        leaving = true;
        fade.toFront();
        fade.clearActions();
        fade.addAction(Actions.sequence(Actions.fadeIn(FADE_TIME), Actions.run(() -> Gdx.app.postRunnable(next))));
    }

    /** Fondu au noir, puis l'écran {@code next} remplace celui-ci, qui est libéré. */
    protected void goTo(Supplier<? extends Screen> next) {
        fadeOutThen(() -> {
            luckyGame.setScreen(next.get());
            dispose();
        });
    }

    /** Retour au menu principal. */
    protected void onBack() {
        goTo(() -> new MenuScreen(luckyGame));
    }

    // -------------------------------------------------------------------------
    // Mise en page et cycle de vie
    // -------------------------------------------------------------------------

    /** Place le décor, le voile et le fondu sur tout l'écran, puis le contenu ({@link #layoutContent}). */
    protected final void layout() {
        float width  = stage.getViewport().getWorldWidth();
        float height = stage.getViewport().getWorldHeight();
        decor.layout(width, height);
        decor.setPosition(0f, 0f);
        veil.setBounds(0f, 0f, width, height);
        fade.setBounds(0f, 0f, width, height);
        layoutContent(width, height);
    }

    /** Place le titre et le contenu de l'écran, de taille {@code width} x {@code height}. */
    protected abstract void layoutContent(float width, float height);

    /**
     * Touche du clavier, quand la scène ne l'a pas prise et que l'écran ne se ferme pas.
     *
     * @return {@code true} si la touche a servi
     */
    protected abstract boolean onKey(int keycode);

    /**
     * Molette de la souris, quand la scène ne l'a pas prise et que l'écran ne se ferme pas.
     *
     * @return {@code true} si elle a servi (par défaut : non)
     */
    protected boolean onScroll(float amountY) {
        return false;
    }

    /** Stage d'abord (souris), puis clavier ({@link #onKey}), et la musique du menu. */
    @Override
    public void show() {
        InputAdapter keyboard = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                return !leaving && onKey(keycode);
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                return !leaving && onScroll(amountY);
            }
        };
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, keyboard));
        music.play();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        layout();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        music.update();
        stage.act(delta);
        stage.draw();
    }

    /** Libère ce qui appartient à l'écran ; les {@link SharedAssets} restent au jeu. Un écran l'étend pour ses ressources. */
    @Override
    public void dispose() {
        stage.dispose();
        decor.dispose();
        music.setDucked(false); // partagée : rendue à plein volume pour l'écran suivant
        Fonts.release(titleFont);
    }
}
