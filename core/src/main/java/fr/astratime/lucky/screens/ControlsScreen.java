package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import fr.astratime.lucky.LuckyGame;

public class ControlsScreen extends ScreenAdapter {

    private static final float BUTTON_WIDTH  = 220f;
    private static final float BUTTON_HEIGHT = 70f;

    private final LuckyGame luckyGame;
    private final Stage     stage;
    private final BitmapFont font;
    private final Texture backgroundTexture;
    private final Texture buttonUpTexture;
    private final Texture buttonDownTexture;

    private final Image      background;
    private final TextButton startButton;

    public ControlsScreen(LuckyGame luckyGame) {
        this.luckyGame = luckyGame;
        this.stage = new Stage(new ScreenViewport(), luckyGame.getBatch());

        backgroundTexture = new Texture(Gdx.files.internal("menu/casino_menu.png"));
        buttonUpTexture   = makeColorTexture(Color.GOLDENROD);
        buttonDownTexture = makeColorTexture(Color.valueOf("b8860bff"));

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Jersey10-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size  = 48;
        parameter.color = Color.WHITE;
        font = generator.generateFont(parameter);
        generator.dispose();

        background  = buildBackground();
        startButton = buildStartButton();

        stage.addActor(background);
        stage.addActor(startButton);
    }

    private Image buildBackground() {
        Image img = new Image(new TextureRegionDrawable(new TextureRegion(backgroundTexture)));
        img.setSize(stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        return img;
    }

    private TextButton buildStartButton() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.up   = new TextureRegionDrawable(new TextureRegion(buttonUpTexture));
        style.down = new TextureRegionDrawable(new TextureRegion(buttonDownTexture));

        TextButton button = new TextButton("Jouer", style);
        button.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        button.setPosition(buttonX(), buttonY());
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onStart();
            }
        });
        return button;
    }

    private float buttonX() {
        return (stage.getViewport().getWorldWidth() - BUTTON_WIDTH) / 2f;
    }

    private float buttonY() {
        return (stage.getViewport().getWorldHeight() - BUTTON_HEIGHT) / 2f;
    }

    private void onStart() {
        luckyGame.setScreen(new GameScreen(luckyGame));
        dispose();
    }

    private Texture makeColorTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        background.setSize(stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        startButton.setPosition(buttonX(), buttonY());
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.WHITE);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
        backgroundTexture.dispose();
        buttonUpTexture.dispose();
        buttonDownTexture.dispose();
    }
}
