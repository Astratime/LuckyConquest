package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.assets.BackgroundMusic;
import fr.astratime.lucky.assets.VolumeSound;
import fr.astratime.lucky.settings.AudioSettings;
import fr.astratime.lucky.settings.DisplaySettings;
import fr.astratime.lucky.settings.VisualSettings;
import fr.astratime.lucky.views.MenuDecor;
import fr.astratime.lucky.views.MinimumScreenViewport;
import fr.astratime.lucky.views.OptionsMenu;
import fr.astratime.lucky.views.ShiningTitle;

import java.util.ArrayList;
import java.util.List;

/**
 * Écran d'accueil, devant l'intérieur animé d'un casino ({@link MenuDecor}) qui
 * se décale légèrement avec la souris (parallaxe) : le titre, sur lequel passe
 * un reflet, flotte au-dessus d'un panneau d'options.
 *
 * Page principale : « Jouer » (lance un combat, {@link GameScreen}), « Options »
 * et « Quitter ». Page des options : affichage (fenêtre agrandie ou plein
 * écran, appliqué tout de suite), effets visuels (normaux ou réduits,
 * réglage partagé avec l'écran de jeu), volume de la musique, volume des sons,
 * et « Retour ».
 *
 * Les options (panneau partagé avec le menu pause, voir {@link OptionsMenu}) se
 * choisissent à la souris ou aux flèches ; Entrée ou un clic valide,
 * gauche/droite règlent, Échap revient à la page principale.
 */
public class MenuScreen extends ScreenAdapter {

    /** Bruitage du clic (CC0, Kenney.nl — voir assets/sounds/CREDITS.txt). */
    private static final String CLICK_SOUND = "sounds/button-click.ogg";
    /** Survol d'une option (synthétisé, voir tools/sounds/generate_sounds.py). */
    private static final String HOVER_SOUND = "sounds/ui/menu_hover.ogg";
    private static final String TITLE       = "LUCKY CONQUEST";
    /** Musique du menu (fournie par Astra) et son volume au réglage maximal : pas de bruitage à couvrir ici. */
    private static final String MUSIC       = "music/main_menu.ogg";
    private static final float  MUSIC_LEVEL = 0.3f;

    /** Taille minimale du menu : dans une fenêtre plus petite, il est réduit (voir {@link MinimumScreenViewport}). */
    private static final float MIN_WIDTH      = 1280f;
    private static final float MIN_HEIGHT     = 1080f;
    private static final float TITLE_TOP      = 230f;   // du haut de l'écran au centre du titre
    private static final float TITLE_FLOAT    = 8f;     // amplitude du flottement du titre
    private static final float PANEL_CENTER_Y = 0.46f;  // fraction de la hauteur de l'écran
    private static final float FADE_TIME      = 0.4f;
    private static final float PARALLAX       = 18f;    // décalage maximal du décor, en pixels
    private static final float PARALLAX_EASE  = 4f;

    private final LuckyGame      luckyGame;
    private final Stage          stage;
    private final VisualSettings settings = new VisualSettings();
    private final AudioSettings  audio    = new AudioSettings();
    private final DisplaySettings display = new DisplaySettings();
    private final HudTextures    hud      = new HudTextures();
    private final BitmapFont     titleFont;
    private final BitmapFont     shineFont;
    private final Sound          clickSound;
    private final Sound          hoverSound;
    private final BackgroundMusic music;

    private final MenuDecor         decor = new MenuDecor();
    private final ShiningTitle      title;
    private final OptionsMenu       menu;
    private final Image             fade;
    private boolean                 leaving;
    private float                   parallaxX, parallaxY;

    /** @param luckyGame instance de jeu : SpriteBatch partagé et changement d'écran */
    public MenuScreen(LuckyGame luckyGame) {
        this.luckyGame = luckyGame;
        this.stage     = new Stage(new MinimumScreenViewport(MIN_WIDTH, MIN_HEIGHT), luckyGame.getBatch());

        clickSound  = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(CLICK_SOUND)), audio);
        hoverSound  = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(HOVER_SOUND)), audio);
        music       = new BackgroundMusic(MUSIC, audio, MUSIC_LEVEL);
        Color shadow = Palette.TEXT_SHADE;
        titleFont   = Fonts.jersey(128, Palette.GOLD, 7f, shadow, TITLE);
        shineFont   = Fonts.jersey(128, Color.WHITE, 7f, shadow, TITLE);

        title   = new ShiningTitle(TITLE, titleFont, shineFont);
        fade    = new Image(new TextureRegionDrawable(new TextureRegion(hud.pixel)));
        fade.setColor(Color.BLACK);
        fade.setTouchable(Touchable.disabled);

        stage.addActor(decor);
        stage.addActor(title);
        menu = new OptionsMenu(stage, hud, clickSound, hoverSound);

        showMainPage();
        stage.addActor(fade);                     // en dernier : fondu d'ouverture et de sortie
        fade.addAction(Actions.fadeOut(FADE_TIME));
        layout();
    }

    // -------------------------------------------------------------------------
    // Pages
    // -------------------------------------------------------------------------

    /** Page principale : Jouer, Options, Quitter. */
    private void showMainPage() {
        showPage(null, List.of(
            OptionsMenu.Entry.button("Jouer", this::onPlay),
            OptionsMenu.Entry.button("Options", this::showOptionsPage),
            OptionsMenu.Entry.button("Quitter", this::onQuit)));
    }

    /** Page des options : affichage, effets visuels, musique, sons, Retour. */
    private void showOptionsPage() {
        List<OptionsMenu.Entry> entries = new ArrayList<>(
            OptionsMenu.settingsEntries(luckyGame, settings, audio, display, () -> { }));
        entries.add(OptionsMenu.Entry.button("Retour", this::showMainPage));
        showPage("OPTIONS", entries);
    }

    private void showPage(String caption, List<OptionsMenu.Entry> entries) {
        menu.setEntries(caption, entries);
        fade.toFront();
    }

    // -------------------------------------------------------------------------
    // Actions
    // -------------------------------------------------------------------------

    /**
     * Fondu au noir puis lancement d'un combat ; l'écran du menu est libéré
     * ensuite (hors de la boucle d'animation du Stage, qu'il ne faut pas
     * disposer pendant qu'il s'exécute).
     */
    private void onPlay() {
        fadeOutThen(() -> {
            luckyGame.setScreen(new GameScreen(luckyGame));
            dispose();
        });
    }

    /** Fondu au noir puis fermeture du jeu. */
    private void onQuit() {
        fadeOutThen(Gdx.app::exit);
    }

    private void fadeOutThen(Runnable next) {
        leaving = true;
        menu.setActive(false);
        fade.clearActions();
        fade.addAction(Actions.sequence(Actions.fadeIn(FADE_TIME), Actions.run(() -> Gdx.app.postRunnable(next))));
    }

    // -------------------------------------------------------------------------
    // Mise en page et cycle de vie
    // -------------------------------------------------------------------------

    /** Place le décor, le titre, le panneau et les options selon la taille de l'écran. */
    private void layout() {
        float width  = stage.getViewport().getWorldWidth();
        float height = stage.getViewport().getWorldHeight();
        // Le décor déborde un peu de l'écran : la parallaxe ne découvre jamais ses bords.
        decor.layout(width + PARALLAX * 2f, height + PARALLAX * 2f);
        placeDecor();
        fade.setBounds(0f, 0f, width, height);

        title.clearActions(); // le flottement repart de la position de repos
        title.setPosition((width - title.getWidth()) / 2f, height - TITLE_TOP - title.getHeight() / 2f);
        title.addAction(Actions.forever(Actions.sequence(
            Actions.moveBy(0f, TITLE_FLOAT, 1.4f, Interpolation.sine),
            Actions.moveBy(0f, -TITLE_FLOAT, 1.4f, Interpolation.sine))));

        menu.layout(width / 2f, height * PANEL_CENTER_Y);
    }

    /** Décale le décor selon la parallaxe courante (il déborde de PARALLAX de chaque côté). */
    private void placeDecor() {
        decor.setPosition(-PARALLAX + parallaxX, -PARALLAX + parallaxY);
    }

    /** Parallaxe : le décor glisse doucement à l'opposé de la souris. */
    private void updateParallax(float delta) {
        float width  = stage.getViewport().getWorldWidth();
        float height = stage.getViewport().getWorldHeight();
        float mouseX = MathUtils.clamp(Gdx.input.getX() / (float) Gdx.graphics.getWidth(), 0f, 1f);
        float mouseY = MathUtils.clamp(Gdx.input.getY() / (float) Gdx.graphics.getHeight(), 0f, 1f);
        float targetX = -(mouseX - 0.5f) * 2f * PARALLAX;
        float targetY = (mouseY - 0.5f) * 2f * PARALLAX;   // Y de la souris compté depuis le haut
        float ease = Math.min(1f, PARALLAX_EASE * delta);
        parallaxX += (targetX - parallaxX) * ease;
        parallaxY += (targetY - parallaxY) * ease;
        if (width > 0f && height > 0f) placeDecor();
    }

    /**
     * Stage d'abord (souris), puis clavier : flèches haut/bas pour choisir,
     * gauche/droite pour régler, Entrée ou Espace pour valider, Échap pour
     * revenir à la page principale.
     */
    @Override
    public void show() {
        InputAdapter keyboard = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (leaving) return false;
                if (keycode == Input.Keys.ESCAPE && menu.hasCaption()) {
                    clickSound.play();
                    showMainPage();
                    return true;
                }
                return menu.keyDown(keycode);
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
        if (!settings.isReducedEffects()) updateParallax(delta);
        music.update(); // suit le réglage « Musique » des options
        stage.act(delta);
        stage.draw();
    }

    /** Libère toutes les ressources natives (Stage, décor, polices, textures, son) possédées par cet écran. */
    @Override
    public void dispose() {
        stage.dispose();
        decor.dispose();
        Fonts.release(titleFont);
        Fonts.release(shineFont);
        menu.dispose();
        clickSound.dispose();
        hoverSound.dispose();
        music.dispose();
        hud.dispose();
    }
}
