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

/**
 * Écran d'accueil : affiche le fond du menu casino et un bouton "Jouer"
 * qui lance une nouvelle partie ({@link GameScreen}).
 */
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

    /**
     * Charge le fond, la police pixel art et les textures du bouton, puis
     * construit et affiche les acteurs de l'écran d'accueil.
     *
     * @param luckyGame instance de jeu, utilisée pour le SpriteBatch partagé
     *                  et pour lancer l'écran de jeu au clic sur "Jouer"
     */
    public ControlsScreen(LuckyGame luckyGame) {
        this.luckyGame = luckyGame;
        this.stage = new Stage(new ScreenViewport(), luckyGame.getBatch());

        backgroundTexture = new Texture(Gdx.files.internal("menu/casino_menu.png"));
        buttonUpTexture   = makeColorTexture(Color.GOLDENROD);
        buttonDownTexture = makeColorTexture(Color.valueOf("b8860bff"));

        // Génération de la police à la taille voulue
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Jersey10-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size  = 48; // taille en pixels, directement nette à cette taille
        parameter.color = Color.WHITE;
        font = generator.generateFont(parameter);
        generator.dispose(); // le générateur ne sert plus une fois la police créée

        background  = buildBackground();
        startButton = buildStartButton();

        stage.addActor(background);
        stage.addActor(startButton);
    }

    /** Image de fond, étirée pour remplir tout l'écran. */
    private Image buildBackground() {
        Image img = new Image(new TextureRegionDrawable(new TextureRegion(backgroundTexture)));
        img.setSize(stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        return img;
    }

    /** Bouton "Jouer", centré à l'écran, qui lance une nouvelle partie au clic. */
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

    /** @return l'abscisse du bouton "Jouer", centré horizontalement sur l'écran. */
    private float buttonX() {
        return (stage.getViewport().getWorldWidth() - BUTTON_WIDTH) / 2f;
    }

    /** @return l'ordonnée du bouton "Jouer", centrée verticalement sur l'écran. */
    private float buttonY() {
        return (stage.getViewport().getWorldHeight() - BUTTON_HEIGHT) / 2f;
    }

    /** Lance une nouvelle partie et libère les ressources de cet écran. */
    private void onStart() {
        luckyGame.setScreen(new GameScreen(luckyGame));
        dispose();
    }

    /** @return une texture 1x1 de la couleur donnée, à étirer pour simuler un fond uni. */
    private Texture makeColorTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /** Installe le Stage comme processeur d'entrée de l'écran (clics sur le bouton). */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    /** Met à jour le viewport puis réajuste le fond et le bouton à la nouvelle taille d'écran. */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        background.setSize(stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        startButton.setPosition(buttonX(), buttonY());
    }

    /** Efface l'écran puis met à jour et dessine le Stage. */
    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.WHITE);
        stage.act(delta);
        stage.draw();
    }

    /** Libère toutes les ressources natives (Stage, police, textures) possédées par cet écran. */
    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
        backgroundTexture.dispose();
        buttonUpTexture.dispose();
        buttonDownTexture.dispose();
    }
}
