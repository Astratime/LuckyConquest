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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.assets.BackgroundMusic;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.assets.VolumeSound;
import fr.astratime.lucky.progress.PlayerProfile;
import fr.astratime.lucky.settings.AudioSettings;
import fr.astratime.lucky.views.CasinoButtons;
import fr.astratime.lucky.views.MenuDecor;
import fr.astratime.lucky.views.MinimumScreenViewport;

/**
 * Boutique : pour l'instant, elle montre seulement les pièces du joueur (voir
 * {@link PlayerProfile#getCoins()}), gagnées dans les donjons de l'Exploration
 * (gains de combat et coffres). Les achats viendront plus tard. Échap ou « Retour » ramène au menu principal.
 */
public class ShopScreen extends ScreenAdapter {

    private static final String CLICK_SOUND = "sounds/button-click.ogg";
    private static final String MUSIC       = "music/main_menu.ogg";
    private static final float  MUSIC_LEVEL = 0.3f;

    private static final float MIN_WIDTH   = 1600f;
    private static final float MIN_HEIGHT  = 1080f;
    private static final float TITLE_TOP   = 110f;
    private static final float PANEL_WIDTH = 900f;
    private static final float PANEL_HEIGHT = 520f;
    private static final float COIN_SIZE   = 128f;
    private static final float FADE_TIME   = 0.4f;

    private final LuckyGame       luckyGame;
    private final Stage           stage;
    private final AudioSettings   audio  = new AudioSettings();
    private final HudTextures     hud    = new HudTextures();
    private final CasinoButtons   buttons = new CasinoButtons();
    private final MenuDecor       decor  = new MenuDecor();
    private final Sound           clickSound;
    private final BackgroundMusic music;
    private final BitmapFont      titleFont = Fonts.jersey(96, Palette.GOLD, 6f, Palette.TEXT_SHADE);
    private final BitmapFont      coinsFont = Fonts.jersey(88, Palette.GOLD, 5f, Palette.TEXT_SHADE);
    private final BitmapFont      bodyFont  = Fonts.jersey(36, Palette.CREAM, 2f, Palette.TEXT_SHADE);

    private final Image      veil;
    private final Label      title;
    private final Image      panel;
    private final Image      coin;
    private final Label      coins;
    private final Label      text;
    private final TextButton backButton;
    private final Image      fade;
    private boolean leaving;

    public ShopScreen(LuckyGame luckyGame) {
        this.luckyGame = luckyGame;
        this.stage     = new Stage(new MinimumScreenViewport(MIN_WIDTH, MIN_HEIGHT), luckyGame.getBatch());
        clickSound = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(CLICK_SOUND)), audio);
        music      = new BackgroundMusic(MUSIC, audio, MUSIC_LEVEL);

        TextureRegionDrawable pixel = new TextureRegionDrawable(new TextureRegion(hud.pixel));
        veil = new Image(pixel);
        veil.setColor(0f, 0f, 0f, 0.6f);
        veil.setTouchable(Touchable.disabled);
        title = new Label("BOUTIQUE", new Label.LabelStyle(titleFont, Color.WHITE));
        title.pack();
        panel = new Image(hud.panelDrawable());
        panel.setTouchable(Touchable.disabled);
        coin = new Image(new TextureRegionDrawable(new TextureRegion(hud.coin)));
        coin.setSize(COIN_SIZE, COIN_SIZE);
        coin.setOrigin(Align.center);
        coin.addAction(Actions.forever(Actions.sequence(
            Actions.moveBy(0f, 10f, 1.2f, Interpolation.sine), Actions.moveBy(0f, -10f, 1.2f, Interpolation.sine))));
        long amount = luckyGame.getProfile().getCoins();
        coins = new Label(PlayerProfile.formatCoins(amount) + (amount > 1 ? " pièces" : " pièce"),
            new Label.LabelStyle(coinsFont, Color.WHITE));
        coins.pack();
        text = new Label("Les gains de tes donjons et leurs coffres remplissent ta bourse. "
            + "La boutique ouvre bientôt.", new Label.LabelStyle(bodyFont, Color.WHITE));
        text.setWrap(true);
        text.setAlignment(Align.center);
        backButton = buttons.create("Retour", clickSound, this::onBack);

        fade = new Image(pixel);
        fade.setColor(Color.BLACK);
        fade.setTouchable(Touchable.disabled);

        stage.addActor(decor);
        stage.addActor(veil);
        stage.addActor(title);
        stage.addActor(panel);
        stage.addActor(coin);
        stage.addActor(coins);
        stage.addActor(text);
        stage.addActor(backButton);
        stage.addActor(fade);
        fade.addAction(Actions.fadeOut(FADE_TIME));
        layout();
    }

    /** Retour au menu principal. */
    private void onBack() {
        if (leaving) return;
        leaving = true;
        fade.clearActions();
        fade.addAction(Actions.sequence(Actions.fadeIn(FADE_TIME), Actions.run(() -> Gdx.app.postRunnable(() -> {
            luckyGame.setScreen(new MenuScreen(luckyGame));
            dispose();
        }))));
    }

    private void layout() {
        float width  = stage.getViewport().getWorldWidth();
        float height = stage.getViewport().getWorldHeight();
        decor.layout(width, height);
        decor.setPosition(0f, 0f);
        veil.setBounds(0f, 0f, width, height);
        fade.setBounds(0f, 0f, width, height);
        title.setPosition((width - title.getWidth()) / 2f, height - TITLE_TOP - title.getHeight() / 2f);
        float panelX = (width - PANEL_WIDTH) / 2f, panelY = (height - PANEL_HEIGHT) / 2f - 30f;
        panel.setBounds(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);
        coin.setPosition((width - COIN_SIZE) / 2f, panelY + PANEL_HEIGHT - 60f - COIN_SIZE);
        coins.setPosition((width - coins.getWidth()) / 2f, coin.getY() - 30f - coins.getHeight());
        text.setWidth(PANEL_WIDTH - 120f);
        text.setHeight(text.getPrefHeight());
        text.setPosition(panelX + 60f, panelY + 60f);
        backButton.setPosition((width - backButton.getWidth()) / 2f, (panelY - backButton.getHeight()) / 2f);
    }

    @Override
    public void show() {
        InputAdapter keyboard = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (leaving || keycode != Input.Keys.ESCAPE && keycode != Input.Keys.ENTER) return false;
                clickSound.play();
                onBack();
                return true;
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

    @Override
    public void dispose() {
        stage.dispose();
        decor.dispose();
        buttons.dispose();
        clickSound.dispose();
        music.dispose();
        hud.dispose();
        Fonts.release(titleFont);
        Fonts.release(coinsFont);
        Fonts.release(bodyFont);
    }
}
