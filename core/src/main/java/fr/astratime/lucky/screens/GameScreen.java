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
import fr.astratime.lucky.entities.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameScreen extends ScreenAdapter {

    // -------------------------------------------------------------------------
    // Constantes
    // -------------------------------------------------------------------------

    private static final String THEME           = "light";
    private static final float  CARD_WIDTH      = 95f;
    private static final float  CARD_HEIGHT     = 135f;
    private static final String BACKGROUND_PATH = "playTable/play_table1.png";

    private static final float BUTTON_WIDTH   = 150f;
    private static final float BUTTON_HEIGHT  = 60f;
    private static final float CARD_TABLE_Y   = 350f;

    private static final float SYMBOL_WIDTH   = 94f;
    private static final float SYMBOL_HEIGHT  = 80f;
    private static final float SLOT_TABLE_Y   = 500f;

    private static final int SCORE_PAIR       = 10;
    private static final int SCORE_JACKPOT    = 50;

    // -------------------------------------------------------------------------
    // Logique de jeu
    // -------------------------------------------------------------------------

    private final DiscardPile discardPile = new DiscardPile();
    private final Deck        deck        = new Deck(discardPile);
    private final SlotMachine slotMachine = new SlotMachine();
    private List<Card>        currentHand = List.of();
    private int               score       = 0;

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
    private final Map<String, Texture> cardTextures   = new HashMap<>();
    private final Map<Symbol, Texture> symbolTextures = new HashMap<>();

    // -------------------------------------------------------------------------
    // Acteurs Scene2D
    // -------------------------------------------------------------------------

    private final Image      background;
    private final Table      cardTable  = new Table();
    private final Table      slotTable  = new Table();
    private final Table      tooltip;
    private final Label      tooltipLabel;
    private       TextButton spinButton;
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

        preloadSymbolTextures();

        background = buildBackground();
        TextButton drawButton = buildDrawButton();
        spinButton  = buildSpinButton();
        spinButton.setDisabled(true);
        scoreLabel  = buildScoreLabel();
        tooltipLabel = new Label("", new Label.LabelStyle(font, Color.WHITE));
        tooltip      = buildTooltip();

        stage.addActor(background);
        stage.addActor(drawButton);
        stage.addActor(spinButton);
        stage.addActor(scoreLabel);
        stage.addActor(slotTable);
        stage.addActor(cardTable);
        stage.addActor(tooltip); // en dernier : affiché au-dessus de tout
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
        TextButton button = new TextButton("Tirer 3 cartes", buildButtonStyle());
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

    private TextButton buildSpinButton() {
        TextButton button = new TextButton("Lancer machine", buildButtonStyle());
        button.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        button.setPosition(20 + BUTTON_WIDTH + 20, 20);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                spinSlotMachine();
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
        Label label = new Label("Points : " + score, new Label.LabelStyle(font, Color.WHITE));
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
    // Logique de pioche
    // -------------------------------------------------------------------------

    private void drawCards() {
        discardPile.addAll(currentHand);
        currentHand = deck.draw(3);

        refreshCardTable();
        spinButton.setDisabled(false);

        Gdx.app.log("GameScreen", "Deck: " + deck.getCards().size() + " | Discard pile: " + discardPile.size());
    }

    private void refreshCardTable() {
        cardTable.clearChildren();
        for (Card card : currentHand) {
            Image cardImage = new Image(new TextureRegionDrawable(new TextureRegion(getCardTexture(card))));
            addCardListeners(cardImage, card);
            cardTable.add(cardImage).size(CARD_WIDTH, CARD_HEIGHT).pad(10f);
        }
        cardTable.pack();

        float worldWidth = stage.getViewport().getWorldWidth();
        cardTable.setPosition((worldWidth - cardTable.getWidth()) / 2f, CARD_TABLE_Y);
    }

    /**
     * Ajoute sur une Image de carte :
     *  - un tooltip au survol (enter/exit)
     *  - un effet de clic : boost du symbole ciblé + disparition de la carte
     */
    private void addCardListeners(Image cardImage, Card card) {
        cardImage.addListener(new InputListener() {

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer != -1) return; // ignore les événements touch, uniquement souris
                tooltipLabel.setText(card.getDescription());
                tooltip.pack();
                // Positionner le tooltip juste au-dessus de la carte en coordonnées stage
                Vector2 stagePos = cardImage.localToStageCoordinates(new Vector2(0, CARD_HEIGHT + 5f));
                tooltip.setPosition(stagePos.x, stagePos.y);
                tooltip.setVisible(true);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer != -1) return;
                tooltip.setVisible(false);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                slotMachine.boostSymbol(card.getTargetSymbol());
                cardImage.setVisible(false);
                tooltip.setVisible(false);
                Gdx.app.log("GameScreen", "Card played : " + card + " -> boost " + card.getTargetSymbol());
                return true;
            }
        });
    }

    // -------------------------------------------------------------------------
    // Logique machine à sous
    // -------------------------------------------------------------------------

    private void spinSlotMachine() {
        slotMachine.spin();           // applique les poids boostés
        slotMachine.resetBoosts();    // réinitialise les probas pour le prochain tour
        refreshSlotTable();
        spinButton.setDisabled(true);

        if (slotMachine.isJackpot()) {
            score += SCORE_JACKPOT;
        } else if (slotMachine.hasPair()) {
            score += SCORE_PAIR;
        }
        refreshScoreLabel();

        Gdx.app.log("GameScreen", "Jackpot: " + slotMachine.isJackpot() + " | Pair: " + slotMachine.hasPair());
    }

    private void refreshSlotTable() {
        slotTable.clearChildren();
        for (Symbol symbol : slotMachine.getResult()) {
            Image symbolImage = new Image(new TextureRegionDrawable(new TextureRegion(symbolTextures.get(symbol))));
            slotTable.add(symbolImage).size(SYMBOL_WIDTH, SYMBOL_HEIGHT).pad(8f);
        }
        slotTable.pack();

        float worldWidth = stage.getViewport().getWorldWidth();
        slotTable.setPosition(worldWidth-(worldWidth /3f), SLOT_TABLE_Y);
    }

    private void refreshScoreLabel() {
        scoreLabel.setText("Points : " + score);
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
                    drawCards();
                    return true;
                }
                if (keycode == Input.Keys.F && !spinButton.isDisabled()) {
                    spinSlotMachine();
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
        buttonDisabledTexture.dispose();
        tooltipBackgroundTexture.dispose();
        cardTextures.values().forEach(Texture::dispose);
        symbolTextures.values().forEach(Texture::dispose);
    }
}
