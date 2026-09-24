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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.VolumeSound;
import fr.astratime.lucky.settings.AudioSettings;
import fr.astratime.lucky.settings.VisualSettings;
import fr.astratime.lucky.views.MenuDecor;
import fr.astratime.lucky.views.MenuOption;
import fr.astratime.lucky.views.ShiningTitle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * Écran d'accueil, devant l'intérieur animé d'un casino ({@link MenuDecor}) qui
 * se décale légèrement avec la souris (parallaxe) : le titre, sur lequel passe
 * un reflet, flotte au-dessus d'un panneau d'options.
 *
 * Page principale : « Jouer » (lance un combat, {@link GameScreen}), « Options »
 * et « Quitter ». Page des options : effets visuels (normaux ou réduits,
 * réglage partagé avec l'écran de jeu), volume de la musique, volume des sons,
 * et « Retour ».
 *
 * L'option sélectionnée (survol de la souris ou flèches du clavier) s'encadre
 * d'or avec un jeton qui tournoie de chaque côté (voir {@link MenuOption}) ;
 * Entrée ou un clic la valide, gauche/droite règlent un volume, Échap revient
 * à la page principale.
 */
public class MenuScreen extends ScreenAdapter {

    private static final String CHIP_PATH   = "menu/chip_spin.png";
    /** Bruitage du clic (CC0, Kenney.nl — voir assets/sounds/CREDITS.txt). */
    private static final String CLICK_SOUND = "sounds/button-click.ogg";

    private static final float TITLE_TOP      = 230f;   // du haut de l'écran au centre du titre
    private static final float TITLE_FLOAT    = 8f;     // amplitude du flottement du titre
    private static final float PANEL_CENTER_Y = 0.46f;  // fraction de la hauteur de l'écran
    private static final float OPTION_WIDTH   = 460f;
    private static final float OPTION_HEIGHT  = 76f;
    private static final float OPTION_GAP     = 14f;
    private static final float PANEL_SIDE     = 100f;   // de chaque côté des options : place des jetons
    private static final float PANEL_PAD      = 30f;
    private static final float CAPTION_SPACE  = 56f;    // en-tête « OPTIONS » de la page des options
    private static final float FADE_TIME      = 0.4f;
    private static final float POP_STAGGER    = 0.06f;
    private static final float PARALLAX       = 18f;    // décalage maximal du décor, en pixels
    private static final float PARALLAX_EASE  = 4f;

    /** Une option : son texte (relu après chaque réglage), son action, et son réglage gauche/droite éventuel. */
    private record Entry(Supplier<String> text, Runnable action, IntConsumer adjust) {}

    private final LuckyGame      luckyGame;
    private final Stage          stage;
    private final VisualSettings settings = new VisualSettings();
    private final AudioSettings  audio    = new AudioSettings();
    private final HudTextures    hud      = new HudTextures();
    private final Texture        chipTexture;
    private final BitmapFont     titleFont;
    private final BitmapFont     shineFont;
    private final BitmapFont     optionFont;
    private final BitmapFont     captionFont;
    private final Sound          clickSound;

    private final MenuDecor         decor = new MenuDecor();
    private final ShiningTitle      title;
    private final Image             panel;
    private final Label             caption;
    private final Image             fade;
    private final List<MenuOption>  options = new ArrayList<>();
    private final List<Entry>       entries = new ArrayList<>();
    private int                     selected;
    private boolean                 leaving;
    private float                   parallaxX, parallaxY;

    /** @param luckyGame instance de jeu : SpriteBatch partagé et changement d'écran */
    public MenuScreen(LuckyGame luckyGame) {
        this.luckyGame = luckyGame;
        this.stage     = new Stage(new ScreenViewport(), luckyGame.getBatch());

        chipTexture = new Texture(Gdx.files.internal(CHIP_PATH));
        clickSound  = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(CLICK_SOUND)), audio);
        Color shadow = Color.valueOf("12080aff");
        titleFont   = Fonts.jersey(128, Color.valueOf("ffd54aff"), 7f, shadow);
        shineFont   = Fonts.jersey(128, Color.WHITE, 7f, shadow);
        optionFont  = Fonts.jersey(58, Color.WHITE, 4f, shadow);
        captionFont = Fonts.jersey(40, Color.valueOf("f0e0b0ff"), 3f, shadow);

        title   = new ShiningTitle("LUCKY CONQUEST", titleFont, shineFont);
        panel   = new Image(hud.panelDrawable());
        panel.setTouchable(Touchable.disabled);
        caption = new Label("OPTIONS", new Label.LabelStyle(captionFont, Color.WHITE));
        fade    = new Image(new TextureRegionDrawable(new TextureRegion(hud.pixel)));
        fade.setColor(Color.BLACK);
        fade.setTouchable(Touchable.disabled);

        stage.addActor(decor);
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

    /** Page principale : Jouer, Options, Quitter. */
    private void showMainPage() {
        setEntries(false, List.of(
            new Entry(() -> "Jouer", this::onPlay, null),
            new Entry(() -> "Options", this::showOptionsPage, null),
            new Entry(() -> "Quitter", this::onQuit, null)));
    }

    /** Page des options : effets visuels, musique, sons, Retour. */
    private void showOptionsPage() {
        setEntries(true, List.of(
            new Entry(this::effectsLabel, this::onToggleEffects, direction -> onToggleEffects()),
            new Entry(() -> "Musique : " + AudioSettings.percent(audio.getMusicVolume()) + " %",
                () -> cycle(audio::stepMusicVolume, audio.getMusicVolume()), audio::stepMusicVolume),
            new Entry(() -> "Sons : " + AudioSettings.percent(audio.getSoundVolume()) + " %",
                () -> cycle(audio::stepSoundVolume, audio.getSoundVolume()), audio::stepSoundVolume),
            new Entry(() -> "Retour", this::showMainPage, null)));
    }

    /** Remplace les options affichées ; elles apparaissent l'une après l'autre, la première sélectionnée. */
    private void setEntries(boolean withCaption, List<Entry> newEntries) {
        options.forEach(Actor::remove);
        options.clear();
        entries.clear();
        entries.addAll(newEntries);
        caption.setVisible(withCaption);
        Label.LabelStyle style = new Label.LabelStyle(optionFont, Color.WHITE);
        for (int i = 0; i < entries.size(); i++) {
            int index = i;
            MenuOption option = new MenuOption(entries.get(i).text().get(), style, hud.insetDrawable(),
                new TextureRegion(chipTexture), OPTION_WIDTH, OPTION_HEIGHT);
            option.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    super.enter(event, x, y, pointer, fromActor);
                    if (pointer == -1) select(index);
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    activate(index);
                }
            });
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

    /** Valide une option : bruitage du clic puis son action, et mise à jour de son texte (réglage). */
    private void activate(int index) {
        if (leaving) return;
        select(index);
        clickSound.play();
        Entry entry = entries.get(index);
        entry.action().run();
        if (index < options.size() && entries.get(index) == entry) options.get(index).setText(entry.text().get());
    }

    /** Règle l'option sélectionnée vers la gauche ({@code -1}) ou la droite ({@code +1}), si elle se règle. */
    private void adjust(int direction) {
        Entry entry = entries.get(selected);
        if (entry.adjust() == null) return;
        entry.adjust().accept(direction);
        options.get(selected).setText(entry.text().get());
        clickSound.play(); // au nouveau volume : on entend le résultat
    }

    /** Volume au clic : palier suivant, puis retour à 0 après le maximum. */
    private static void cycle(IntConsumer step, float current) {
        if (current >= 1f) {
            for (int i = 0; i < Math.round(1f / AudioSettings.STEP); i++) step.accept(-1);
        } else {
            step.accept(1);
        }
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
        fade.clearActions();
        fade.addAction(Actions.sequence(Actions.fadeIn(FADE_TIME), Actions.run(() -> Gdx.app.postRunnable(next))));
    }

    /** Bascule entre effets visuels normaux et réduits (réglage mémorisé, partagé avec l'écran de jeu). */
    private void onToggleEffects() {
        settings.setReducedEffects(!settings.isReducedEffects());
    }

    private String effectsLabel() {
        return settings.isReducedEffects() ? "Effets : reduits" : "Effets : normaux";
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
                if (leaving || options.isEmpty()) return false;
                switch (keycode) {
                    case Input.Keys.UP, Input.Keys.W -> select((selected - 1 + options.size()) % options.size());
                    case Input.Keys.DOWN, Input.Keys.S -> select((selected + 1) % options.size());
                    case Input.Keys.LEFT, Input.Keys.A -> adjust(-1);
                    case Input.Keys.RIGHT, Input.Keys.D -> adjust(1);
                    case Input.Keys.ENTER, Input.Keys.SPACE -> activate(selected);
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
        if (!settings.isReducedEffects()) updateParallax(delta);
        stage.act(delta);
        stage.draw();
    }

    /** Libère toutes les ressources natives (Stage, décor, polices, textures, son) possédées par cet écran. */
    @Override
    public void dispose() {
        stage.dispose();
        decor.dispose();
        chipTexture.dispose();
        titleFont.dispose();
        shineFont.dispose();
        optionFont.dispose();
        captionFont.dispose();
        clickSound.dispose();
        hud.dispose();
    }
}
