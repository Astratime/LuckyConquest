package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Deck;
import fr.astratime.lucky.entities.DiscardPile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameScreen extends ScreenAdapter {

    // -------------------------------------------------------------------------
    // Constantes
    // -------------------------------------------------------------------------

    private static final String THEME           = "light";
    private static final float  CARD_WIDTH      = 95f;   // moitié de la taille source (190x270)
    private static final float  CARD_HEIGHT     = 135f;
    private static final String BACKGROUND_PATH = "playTable/play_table1.png";

    private static final float BUTTON_WIDTH  = 150f;
    private static final float BUTTON_HEIGHT = 60f;
    private static final float CARD_TABLE_Y  = 350f;

    // -------------------------------------------------------------------------
    // Logique de jeu
    // -------------------------------------------------------------------------

    private final DiscardPile discardPile = new DiscardPile();
    private final Deck        deck        = new Deck(discardPile);
    private List<Card>        currentHand = List.of();

    // -------------------------------------------------------------------------
    // Ressources (à disposer dans dispose())
    // -------------------------------------------------------------------------

    private final LuckyGame  luckyGame;
    private final Stage      stage;
    private final BitmapFont font;
    private final Texture    backgroundTexture;
    private final Texture    buttonUpTexture;
    private final Texture    buttonDownTexture;
    private final Map<String, Texture> cardTextures = new HashMap<>();

    // -------------------------------------------------------------------------
    // Acteurs Scene2D
    // -------------------------------------------------------------------------

    private final Image background;
    private final Table cardTable = new Table();

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------

    public GameScreen(LuckyGame luckyGame) {
        this.luckyGame = luckyGame;
        // Le SpriteBatch est partagé avec LuckyGame et ne doit PAS être disposé ici.
        this.stage = new Stage(new ScreenViewport(), luckyGame.getBatch());

        font              = new BitmapFont();
        backgroundTexture = new Texture(Gdx.files.internal(BACKGROUND_PATH));
        buttonUpTexture   = makeColorTexture(Color.DARK_GRAY);
        buttonDownTexture = makeColorTexture(Color.GRAY);

        background = buildBackground();
        TextButton drawButton = buildDrawButton();

        stage.addActor(background);
        stage.addActor(drawButton);
        stage.addActor(cardTable);
    }

    // -------------------------------------------------------------------------
    // Construction des acteurs
    // -------------------------------------------------------------------------

    private Image buildBackground() {
        Image img = new Image(new TextureRegionDrawable(new TextureRegion(backgroundTexture)));
        img.setSize(stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        return img;
    }

    private TextButton buildDrawButton() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.up   = new TextureRegionDrawable(new TextureRegion(buttonUpTexture));
        style.down = new TextureRegionDrawable(new TextureRegion(buttonDownTexture));

        TextButton button = new TextButton("Tirer 3 cartes", style);
        button.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        button.setPosition(20, 20);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                drawCards();
            }
        });
        return button;
    }

    // -------------------------------------------------------------------------
    // Logique de pioche
    // -------------------------------------------------------------------------

    private void drawCards() {
        discardPile.addAll(currentHand);
        currentHand = deck.draw(3);

        refreshCardTable();

        Gdx.app.log("GameScreen", "Deck: " + deck.getCards().size() + " | Discard pile: " + discardPile.size());
    }

    private void refreshCardTable() {
        cardTable.clearChildren();
        for (Card card : currentHand) {
            Image cardImage = new Image(new TextureRegionDrawable(new TextureRegion(getCardTexture(card))));
            cardTable.add(cardImage).size(CARD_WIDTH, CARD_HEIGHT).pad(10f);
        }
        cardTable.pack();

        float worldWidth = stage.getViewport().getWorldWidth();
        cardTable.setPosition((worldWidth - cardTable.getWidth()) / 2f, CARD_TABLE_Y);
    }

    // -------------------------------------------------------------------------
    // Gestion des textures
    // -------------------------------------------------------------------------

    /** Charge ou récupère depuis le cache la texture d'une carte. */
    private Texture getCardTexture(Card card) {
        String path = card.getAssetPath(THEME);
        return cardTextures.computeIfAbsent(path, p -> new Texture(Gdx.files.internal(p)));
    }

    /** Crée une texture 1×1 px d'une couleur unie (à stocker et disposer). */
    private Texture makeColorTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    // -------------------------------------------------------------------------
    // Cycle de vie ScreenAdapter
    // -------------------------------------------------------------------------

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        background.setSize(stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
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
        cardTextures.values().forEach(Texture::dispose);
    }
}
