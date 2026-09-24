package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.settings.VisualSettings;
import fr.astratime.lucky.views.MenuOption;

import java.util.ArrayList;
import java.util.List;

/**
 * Écran d'accueil, devant l'intérieur d'un casino (machines à sous, bar,
 * billard) : le titre du jeu flotte au-dessus d'un panneau d'options.
 *
 * Page principale : « Jouer » (lance un combat, {@link GameScreen}) et
 * « Options ». Page des options : les effets visuels (normaux ou réduits,
 * réglage partagé avec l'écran de jeu) et « Retour ».
 *
 * L'option sélectionnée (survol de la souris ou flèches du clavier) s'encadre
 * d'or avec un jeton qui tournoie de chaque côté (voir {@link MenuOption}) ;
 * Entrée ou un clic la valide, Échap revient à la page principale.
 */
public class MenuScreen extends ScreenAdapter {

    private static final String BACKGROUND_PATH = "menu/casino_interior.png";
    private static final String CHIP_PATH       = "menu/chip_spin.png";
    /** Bruitage du clic (CC0, Kenney.nl — voir assets/sounds/CREDITS.txt). */
    private static final String CLICK_SOUND     = "sounds/button-click.ogg";

    private static final float TITLE_TOP      = 230f;   // du haut de l'écran au centre du titre
    private static final float TITLE_FLOAT    = 8f;     // amplitude du flottement du titre
    private static final float PANEL_CENTER_Y = 0.46f;  // fraction de la hauteur de l'écran
    private static final float OPTION_WIDTH   = 420f;
    private static final float OPTION_HEIGHT  = 84f;
    private static final float OPTION_GAP     = 22f;
    private static final float PANEL_SIDE     = 100f;   // de chaque côté des options : place des jetons
    private static final float PANEL_PAD      = 36f;
    private static final float CAPTION_SPACE  = 60f;    // en-tête « OPTIONS » de la page des options
    private static final float FADE_TIME      = 0.4f;
    private static final float POP_STAGGER    = 0.06f;

    private final LuckyGame      luckyGame;
    private final Stage          stage;
    private final VisualSettings settings = new VisualSettings();
    private final HudTextures    hud      = new HudTextures();
    private final Texture        backgroundTexture;
    private final Texture        chipTexture;
    private final BitmapFont     titleFont;
    private final BitmapFont     optionFont;
    private final BitmapFont     captionFont;
    private final Sound          clickSound;

    private final Image             background;
    private final Label             title;
    private final Image             panel;
    private final Label             caption;
    private final Image             fade;
    private final List<MenuOption>  options = new ArrayList<>();
    private int                     selected;
    private boolean                 leaving;

    /** @param luckyGame instance de jeu : SpriteBatch partagé et changement d'écran */
    public MenuScreen(LuckyGame luckyGame) {
        this.luckyGame = luckyGame;
        this.stage     = new Stage(new ScreenViewport(), luckyGame.getBatch());

        backgroundTexture = new Texture(Gdx.files.internal(BACKGROUND_PATH));
        chipTexture       = new Texture(Gdx.files.internal(CHIP_PATH));
        clickSound        = Gdx.audio.newSound(Gdx.files.internal(CLICK_SOUND));
        Color shadow      = Color.valueOf("12080aff");
        titleFont   = Fonts.jersey(128, Color.valueOf("ffd54aff"), 7f, shadow);
        optionFont  = Fonts.jersey(64, Color.WHITE, 4f, shadow);
        captionFont = Fonts.jersey(40, Color.valueOf("f0e0b0ff"), 3f, shadow);

        background = new Image(new TextureRegionDrawable(new TextureRegion(backgroundTexture)));
        title      = new Label("LUCKY CONQUEST", new Label.LabelStyle(titleFont, Color.WHITE));
        panel      = new Image(hud.panelDrawable());
        panel.setTouchable(Touchable.disabled);
        caption    = new Label("OPTIONS", new Label.LabelStyle(captionFont, Color.WHITE));
        fade       = new Image(new TextureRegionDrawable(new TextureRegion(hud.pixel)));
        fade.setColor(Color.BLACK);
        fade.setTouchable(Touchable.disabled);

        stage.addActor(background);
        stage.addActor(title);
        stage.addActor(panel);
        stage.addActor(caption);

        showMainPage();
        stage.addActor(fade);                     // en dernier : fondu d'ouverture et de sortie
        fade.addAction(Actions.fadeOut(FADE_TIME));
        layout();
    }

    // -------------------------------------------------------------------------
    // Pages
    // -------------------------------------------------------------------------

    /** Page principale : Jouer, Options. */
    private void showMainPage() {
        setOptions(false, new String[] {"Jouer", "Options"}, new Runnable[] {this::onPlay, this::showOptionsPage});
    }

    /** Page des options : effets visuels, Retour. */
    private void showOptionsPage() {
        setOptions(true, new String[] {effectsLabel(), "Retour"}, new Runnable[] {this::onToggleEffects, this::showMainPage});
    }

    /** Remplace les options affichées ; elles apparaissent l'une après l'autre, la première sélectionnée. */
    private void setOptions(boolean withCaption, String[] texts, Runnable[] actions) {
        options.forEach(Actor::remove);
        options.clear();
        caption.setVisible(withCaption);
        Label.LabelStyle style = new Label.LabelStyle(optionFont, Color.WHITE);
        for (int i = 0; i < texts.length; i++) {
            int index = i;
            MenuOption option = new MenuOption(texts[i], style, hud.insetDrawable(), new TextureRegion(chipTexture),
                OPTION_WIDTH, OPTION_HEIGHT);
            option.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    super.enter(event, x, y, pointer, fromActor);
                    if (pointer == -1) select(index);
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    activate(index, actions[index]);
                }
            });
            option.setUserObject(actions[i]);
            option.setScale(0f);
            option.addAction(Actions.sequence(Actions.delay(i * POP_STAGGER),
                Actions.scaleTo(1f, 1f, 0.25f, Interpolation.swingOut)));
            options.add(option);
            stage.addActor(option);
        }
        fade.toFront();
        selected = -1;
        select(0);
        layout();
    }

    /** Sélectionne l'option {@code index} et désélectionne les autres. */
    private void select(int index) {
        if (index == selected) return;
        selected = index;
        for (int i = 0; i < options.size(); i++) options.get(i).setSelected(i == index);
    }

    /** Valide une option : bruitage du clic puis son action. */
    private void activate(int index, Runnable action) {
        if (leaving) return;
        select(index);
        clickSound.play();
        action.run();
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
        leaving = true;
        fade.clearActions();
        fade.addAction(Actions.sequence(
            Actions.fadeIn(FADE_TIME),
            Actions.run(() -> Gdx.app.postRunnable(() -> {
                luckyGame.setScreen(new GameScreen(luckyGame));
                dispose();
            }))));
    }

    /** Bascule entre effets visuels normaux et réduits (réglage mémorisé, partagé avec l'écran de jeu). */
    private void onToggleEffects() {
        settings.setReducedEffects(!settings.isReducedEffects());
        options.get(0).setText(effectsLabel());
    }

    private String effectsLabel() {
        return settings.isReducedEffects() ? "Effets : reduits" : "Effets : normaux";
    }

    // -------------------------------------------------------------------------
    // Mise en page et cycle de vie
    // -------------------------------------------------------------------------

    /** Place le fond, le titre, le panneau et les options selon la taille de l'écran. */
    private void layout() {
        float width  = stage.getViewport().getWorldWidth();
        float height = stage.getViewport().getWorldHeight();
        background.setBounds(0f, 0f, width, height);
        fade.setBounds(0f, 0f, width, height);

        title.pack();
        title.clearActions(); // le flottement repart de la position de repos
        title.setPosition((width - title.getWidth()) / 2f, height - TITLE_TOP - title.getHeight() / 2f);
        title.addAction(Actions.forever(Actions.sequence(
            Actions.moveBy(0f, TITLE_FLOAT, 1.4f, Interpolation.sine),
            Actions.moveBy(0f, -TITLE_FLOAT, 1.4f, Interpolation.sine))));

        float listHeight  = options.size() * OPTION_HEIGHT + Math.max(0, options.size() - 1) * OPTION_GAP;
        float captionH    = caption.isVisible() ? CAPTION_SPACE : 0f;
        float panelWidth  = OPTION_WIDTH + PANEL_SIDE * 2f;
        float panelHeight = listHeight + captionH + PANEL_PAD * 2f;
        float panelX      = (width - panelWidth) / 2f;
        float panelY      = height * PANEL_CENTER_Y - panelHeight / 2f;
        panel.setBounds(panelX, panelY, panelWidth, panelHeight);

        caption.pack();
        caption.setPosition((width - caption.getWidth()) / 2f, panelY + panelHeight - PANEL_PAD - caption.getHeight());

        float y = panelY + panelHeight - PANEL_PAD - captionH - OPTION_HEIGHT;
        for (MenuOption option : options) {
            option.setPosition((width - OPTION_WIDTH) / 2f, y);
            y -= OPTION_HEIGHT + OPTION_GAP;
        }
    }

    /**
     * Stage d'abord (souris), puis clavier : flèches haut/bas pour choisir,
     * Entrée ou Espace pour valider, Échap pour revenir à la page principale.
     */
    @Override
    public void show() {
        InputAdapter keyboard = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (leaving || options.isEmpty()) return false;
                switch (keycode) {
                    case Input.Keys.UP, Input.Keys.W -> select((selected - 1 + options.size()) % options.size());
                    case Input.Keys.DOWN, Input.Keys.S -> select((selected + 1) % options.size());
                    case Input.Keys.ENTER, Input.Keys.SPACE ->
                        activate(selected, (Runnable) options.get(selected).getUserObject());
                    case Input.Keys.ESCAPE -> {
                        if (!caption.isVisible()) return false;
                        clickSound.play();
                        showMainPage();
                    }
                    default -> { return false; }
                }
                return true;
            }
        };
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, keyboard));
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        layout();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        stage.act(delta);
        stage.draw();
    }

    /** Libère toutes les ressources natives (Stage, polices, textures, son) possédées par cet écran. */
    @Override
    public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
        chipTexture.dispose();
        titleFont.dispose();
        optionFont.dispose();
        captionFont.dispose();
        clickSound.dispose();
        hud.dispose();
    }
}
