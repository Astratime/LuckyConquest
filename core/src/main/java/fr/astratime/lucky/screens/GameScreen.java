package fr.astratime.lucky.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.controllers.GameController;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.TurnResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsabilité unique : afficher l'état du jeu et transmettre les actions
 * du joueur au GameController. Aucune logique métier ici.
 */
public class GameScreen extends ScreenAdapter {

    // -------------------------------------------------------------------------
    // Constantes d'affichage
    // -------------------------------------------------------------------------

    private static final String THEME           = "light";
    private static final float  CARD_WIDTH      = 95f;
    private static final float  CARD_HEIGHT     = 135f;
    private static final String BACKGROUND_PATH = "playTable/play_table1.png";

    private static final float BUTTON_WIDTH  = 150f;
    private static final float BUTTON_HEIGHT = 60f;
    private static final float CARD_TABLE_Y  = 350f;

    private static final float SYMBOL_WIDTH  = 94f;
    private static final float SYMBOL_HEIGHT = 80f;
    private static final float SLOT_TABLE_Y  = 200f;

    private static final float HEALTH_BAR_WIDTH      = 300f;
    private static final float HEALTH_BAR_HEIGHT     = 22f;
    private static final float HEALTH_BAR_TOP_MARGIN = 10f;

    // -------------------------------------------------------------------------
    // Contrôleur — seul point d'accès à la logique de jeu
    // -------------------------------------------------------------------------

    private final GameController gameController = new GameController();

    // -------------------------------------------------------------------------
    // Ressources (à disposer dans dispose())
    // -------------------------------------------------------------------------

    private final LuckyGame  luckyGame;
    private final Stage      stage;
    private final BitmapFont font;
    private final Texture    backgroundTexture;
    private final Texture    buttonUpTexture;
    private final Texture    buttonDownTexture;
    private final Texture    buttonDisabledTexture;
    private final Texture    tooltipBackgroundTexture;
    private final Texture    healthBarBgTexture;
    private final Texture    healthBarFillTexture;
    private final Map<String, Texture> cardTextures   = new HashMap<>();
    private final Map<Symbol, Texture> symbolTextures = new HashMap<>();

    // -------------------------------------------------------------------------
    // Acteurs Scene2D
    // -------------------------------------------------------------------------

    private final Image      background;
    private final Image      healthBarBg;
    private final Image      healthBarFill;
    private final Table      cardTable = new Table();
    private final Table      slotTable = new Table();
    private final Table      tooltip;
    private final Label      tooltipLabel;
    private       TextButton spinButton;
    private  TextButton drawButton;
    private       Label      scoreLabel;

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------

    public GameScreen(LuckyGame luckyGame) {
        this.luckyGame = luckyGame;
        // Le SpriteBatch est partagé avec LuckyGame et ne doit PAS être disposé ici.
        this.stage = new Stage(new ScreenViewport(), luckyGame.getBatch());

        font                     = new BitmapFont();
        backgroundTexture        = new Texture(Gdx.files.internal(BACKGROUND_PATH));
        buttonUpTexture          = makeColorTexture(Color.DARK_GRAY);
        buttonDownTexture        = makeColorTexture(Color.GRAY);
        buttonDisabledTexture    = makeColorTexture(Color.valueOf("333333ff"));
        tooltipBackgroundTexture = makeColorTexture(Color.BLACK);
        healthBarBgTexture       = makeColorTexture(Color.valueOf("550000ff"));
        healthBarFillTexture     = makeColorTexture(Color.RED);

        preloadSymbolTextures();

        background    = buildBackground();
        healthBarBg   = buildHealthBarBg();
        healthBarFill = buildHealthBarFill();
        drawButton = buildDrawButton();
        spinButton    = buildSpinButton();
        spinButton.setDisabled(true);
        scoreLabel    = buildScoreLabel();
        tooltipLabel  = new Label("", new Label.LabelStyle(font, Color.WHITE));
        tooltip       = buildTooltip();

        stage.addActor(background);
        stage.addActor(healthBarBg);
        stage.addActor(healthBarFill);
        stage.addActor(drawButton);
        stage.addActor(spinButton);
        stage.addActor(scoreLabel);
        stage.addActor(slotTable);
        stage.addActor(cardTable);
        stage.addActor(tooltip); // en dernier : toujours au-dessus
    }

    // -------------------------------------------------------------------------
    // Construction des acteurs
    // -------------------------------------------------------------------------

    private Image buildBackground() {
        Image img = new Image(new TextureRegionDrawable(new TextureRegion(backgroundTexture)));
        img.setSize(stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        return img;
    }

    private Image buildHealthBarBg() {
        Image img = new Image(new TextureRegionDrawable(new TextureRegion(healthBarBgTexture)));
        img.setSize(HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
        img.setPosition(healthBarX(), healthBarY());
        return img;
    }

    private Image buildHealthBarFill() {
        Image img = new Image(new TextureRegionDrawable(new TextureRegion(healthBarFillTexture)));
        img.setSize(HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
        img.setPosition(healthBarX(), healthBarY());
        return img;
    }

    private TextButton buildDrawButton() {
        TextButton button = new TextButton("Tirer 3 cartes", buildButtonStyle());
        button.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        button.setPosition(20, 20);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onDrawCards();
            }
        });
        return button;
    }

    private TextButton buildSpinButton() {
        TextButton button = new TextButton("Lancer machine", buildButtonStyle());
        button.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        button.setPosition(20 + BUTTON_WIDTH + 20, 20);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onSpin();
            }
        });
        return button;
    }

    private TextButton.TextButtonStyle buildButtonStyle() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font              = font;
        style.up                = new TextureRegionDrawable(new TextureRegion(buttonUpTexture));
        style.down              = new TextureRegionDrawable(new TextureRegion(buttonDownTexture));
        style.disabled          = new TextureRegionDrawable(new TextureRegion(buttonDisabledTexture));
        style.disabledFontColor = Color.GRAY;
        return style;
    }

    private Label buildScoreLabel() {
        Label label = new Label("Points : 0", new Label.LabelStyle(font, Color.WHITE));
        label.setPosition(20, stage.getViewport().getWorldHeight() - 40);
        return label;
    }

    private Table buildTooltip() {
        Table t = new Table();
        t.setBackground(new TextureRegionDrawable(new TextureRegion(tooltipBackgroundTexture)));
        t.add(tooltipLabel).pad(8f);
        t.setVisible(false);
        return t;
    }

    // -------------------------------------------------------------------------
    // Positionnement de la barre de vie
    // -------------------------------------------------------------------------

    private float healthBarX() {
        return (stage.getViewport().getWorldWidth() - HEALTH_BAR_WIDTH) / 2f;
    }

    private float healthBarY() {
        return stage.getViewport().getWorldHeight() - HEALTH_BAR_HEIGHT - HEALTH_BAR_TOP_MARGIN;
    }

    // -------------------------------------------------------------------------
    // Interactions joueur — transmises au GameController
    // -------------------------------------------------------------------------

    private void onDrawCards() {
        List<Card> hand = gameController.drawCards();
        refreshCardTable(hand);
        spinButton.setDisabled(false);
        drawButton.setDisabled(true);
    }

    private void onSpin() {
        TurnResult result = gameController.spin();
        refreshSlotTable(result.getSymbols());
        refreshHealthBar();
        refreshScoreLabel();
        spinButton.setDisabled(true);
        drawButton.setDisabled(false);

        Gdx.app.log("GameScreen", result.getEvents().stream()
            .map(e -> e.describe())
            .reduce("", (a, b) -> a + " | " + b));
    }

    private void onCardPlayed(Card card, Image cardImage) {
        gameController.playCard(card);
        cardImage.setVisible(false);
        tooltip.setVisible(false);
        Gdx.app.log("GameScreen", "Carte jouee : " + card);
    }

    // -------------------------------------------------------------------------
    // Rafraîchissement de l'affichage
    // -------------------------------------------------------------------------

    private void refreshCardTable(List<Card> hand) {
        cardTable.clearChildren();
        for (Card card : hand) {
            Image cardImage = new Image(new TextureRegionDrawable(new TextureRegion(getCardTexture(card))));
            addCardListeners(cardImage, card);
            cardTable.add(cardImage).size(CARD_WIDTH, CARD_HEIGHT).pad(10f);
        }
        cardTable.pack();

        float worldWidth = stage.getViewport().getWorldWidth();
        cardTable.setPosition((worldWidth - cardTable.getWidth()) / 2f, CARD_TABLE_Y);
    }

    private void refreshSlotTable(Symbol[] symbols) {
        slotTable.clearChildren();
        for (Symbol symbol : symbols) {
            Image img = new Image(new TextureRegionDrawable(new TextureRegion(symbolTextures.get(symbol))));
            slotTable.add(img).size(SYMBOL_WIDTH, SYMBOL_HEIGHT).pad(8f);
        }
        slotTable.pack();

        float worldWidth = stage.getViewport().getWorldWidth();
        slotTable.setPosition((worldWidth - slotTable.getWidth()) / 2f, SLOT_TABLE_Y);
    }

    private void refreshHealthBar() {
        Enemy enemy = gameController.getGameState().getEnemy();
        float ratio = (float) enemy.getHp() / enemy.getMaxHp();
        healthBarFill.setWidth(HEALTH_BAR_WIDTH * ratio);
    }

    private void refreshScoreLabel() {
        scoreLabel.setText("Points : " + gameController.getGameState().getScore());
    }

    // -------------------------------------------------------------------------
    // Listeners des cartes
    // -------------------------------------------------------------------------

    private void addCardListeners(Image cardImage, Card card) {
        cardImage.addListener(new InputListener() {

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer != -1) return;
                tooltipLabel.setText(card.getDescription());
                tooltip.pack();
                Vector2 pos = cardImage.localToStageCoordinates(new Vector2(0, CARD_HEIGHT + 5f));
                tooltip.setPosition(pos.x, pos.y);
                tooltip.setVisible(true);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer != -1) return;
                tooltip.setVisible(false);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                onCardPlayed(card, cardImage);
                return true;
            }
        });
    }

    // -------------------------------------------------------------------------
    // Gestion des textures
    // -------------------------------------------------------------------------

    private void preloadSymbolTextures() {
        for (Symbol symbol : Symbol.values()) {
            symbolTextures.put(symbol, new Texture(Gdx.files.internal(symbol.getAssetPath())));
        }
    }

    private Texture getCardTexture(Card card) {
        String path = card.getAssetPath(THEME);
        return cardTextures.computeIfAbsent(path, p -> new Texture(Gdx.files.internal(p)));
    }

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
        InputAdapter keyboardInput = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.SPACE) {
                    onDrawCards();
                    return true;
                }
                if (keycode == Input.Keys.F && !spinButton.isDisabled()) {
                    onSpin();
                    return true;
                }
                return false;
            }
        };
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, keyboardInput));
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        float worldWidth  = stage.getViewport().getWorldWidth();
        float worldHeight = stage.getViewport().getWorldHeight();

        background.setSize(worldWidth, worldHeight);
        healthBarBg.setPosition(healthBarX(), healthBarY());
        healthBarFill.setPosition(healthBarX(), healthBarY());
        scoreLabel.setPosition(20, worldHeight - 40);
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
        buttonDisabledTexture.dispose();
        tooltipBackgroundTexture.dispose();
        healthBarBgTexture.dispose();
        healthBarFillTexture.dispose();
        cardTextures.values().forEach(Texture::dispose);
        symbolTextures.values().forEach(Texture::dispose);
    }
}
