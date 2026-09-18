package fr.astratime.lucky.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.controllers.GameController;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.TurnResult;

import java.util.ArrayList;
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
    private static final String CARD_BACK_PATH  = "cards/" + THEME + "/BACK.png";
    private static final float  BACKGROUND_SHRINK = 100f;

    private static final float BUTTON_WIDTH  = 150f;
    private static final float BUTTON_HEIGHT = 60f;
    private static final float CARD_TABLE_Y  = 350f;

    // Distribution animée des cartes : elles arrivent dos visible depuis le
    // dessus de l'écran, puis se retournent (flip) pour révéler leur face.
    private static final float DEAL_STAGGER_DELAY = 0.15f;
    private static final float DEAL_MOVE_DURATION = 0.35f;
    private static final float FLIP_PAUSE_DELAY   = 0.05f;
    private static final float FLIP_HALF_DURATION = 0.12f;

    private static final float SCORE_LABEL_TOP_MARGIN = 40f;
    private static final float RESTART_BUTTON_GAP      = 10f;

    private static final float SYMBOL_WIDTH  = 94f;
    private static final float SYMBOL_HEIGHT = 80f;
    private static final float SLOT_TABLE_Y  = 200f;

    private static final float HEALTH_BAR_WIDTH         = 300f;
    private static final float HEALTH_BAR_HEIGHT        = 22f;
    private static final float HEALTH_BAR_TOP_MARGIN    = 10f;
    private static final float HEALTH_BAR_BOTTOM_MARGIN = 100f;

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
    private final Texture    playerHealthBarBgTexture;
    private final Texture    playerHealthBarFillTexture;
    private final Texture    cardBackTexture;
    private final Map<String, Texture> cardTextures   = new HashMap<>();
    private final Map<Symbol, Texture> symbolTextures = new HashMap<>();

    /** Cartes en cours d'animation de distribution (dos -> face), à nettoyer si une nouvelle donne démarre. */
    private final List<Image> flyingCards = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Acteurs Scene2D
    // -------------------------------------------------------------------------

    private final Image      background;
    private final Image      healthBarBg;
    private final Image      healthBarFill;
    private final Label      healthBarLabel;
    private final Image      playerHealthBarBg;
    private final Image      playerHealthBarFill;
    private final Label      playerHealthBarLabel;
    private final Table      cardTable = new Table();
    private final Table      slotTable = new Table();
    private final Table      tooltip;
    private final Label      tooltipLabel;
    private       TextButton spinButton;
    private  TextButton drawButton;
    private       Label      scoreLabel;
    private       TextButton restartButton;

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
        healthBarBgTexture         = makeColorTexture(Color.valueOf("550000ff"));
        healthBarFillTexture       = makeColorTexture(Color.RED);
        playerHealthBarBgTexture   = makeColorTexture(Color.valueOf("005500ff"));
        playerHealthBarFillTexture = makeColorTexture(Color.GREEN);
        cardBackTexture            = new Texture(Gdx.files.internal(CARD_BACK_PATH));

        preloadSymbolTextures();

        background          = buildBackground();
        healthBarBg         = buildHealthBarBg();
        healthBarFill       = buildHealthBarFill();
        healthBarLabel       = buildHealthBarLabel();
        playerHealthBarBg   = buildPlayerHealthBarBg();
        playerHealthBarFill = buildPlayerHealthBarFill();
        playerHealthBarLabel = buildPlayerHealthBarLabel();
        drawButton = buildDrawButton();
        spinButton    = buildSpinButton();
        spinButton.setDisabled(true);
        scoreLabel    = buildScoreLabel();
        restartButton = buildRestartButton();
        restartButton.setVisible(false);
        tooltipLabel  = new Label("", new Label.LabelStyle(font, Color.WHITE));
        tooltip       = buildTooltip();

        refreshHealthBar();
        refreshPlayerHealthBar();

        stage.addActor(background);
        stage.addActor(healthBarBg);
        stage.addActor(healthBarFill);
        stage.addActor(healthBarLabel);
        stage.addActor(playerHealthBarBg);
        stage.addActor(playerHealthBarFill);
        stage.addActor(playerHealthBarLabel);
        stage.addActor(drawButton);
        stage.addActor(spinButton);
        stage.addActor(scoreLabel);
        stage.addActor(restartButton);
        stage.addActor(slotTable);
        stage.addActor(cardTable);
        stage.addActor(tooltip); // en dernier : toujours au-dessus
    }

    // -------------------------------------------------------------------------
    // Construction des acteurs
    // -------------------------------------------------------------------------

    private Image buildBackground() {
        Image img = new Image(new TextureRegionDrawable(new TextureRegion(backgroundTexture)));
        img.setSize(backgroundWidth(), backgroundHeight());
        img.setPosition(backgroundX(), backgroundY());
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

    private Label buildHealthBarLabel() {
        return buildHealthBarLabel(healthBarX(), healthBarY());
    }

    private Image buildPlayerHealthBarBg() {
        Image img = new Image(new TextureRegionDrawable(new TextureRegion(playerHealthBarBgTexture)));
        img.setSize(HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
        img.setPosition(healthBarX(), playerHealthBarY());
        return img;
    }

    private Image buildPlayerHealthBarFill() {
        Image img = new Image(new TextureRegionDrawable(new TextureRegion(playerHealthBarFillTexture)));
        img.setSize(HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
        img.setPosition(healthBarX(), playerHealthBarY());
        return img;
    }

    private Label buildPlayerHealthBarLabel() {
        return buildHealthBarLabel(healthBarX(), playerHealthBarY());
    }

    private Label buildHealthBarLabel(float x, float y) {
        Label label = new Label("", new Label.LabelStyle(font, Color.WHITE));
        label.setSize(HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
        label.setPosition(x, y);
        label.setAlignment(Align.center);
        return label;
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
        label.setPosition(20, stage.getViewport().getWorldHeight() - SCORE_LABEL_TOP_MARGIN);
        return label;
    }

    private TextButton buildRestartButton() {
        TextButton button = new TextButton("Recommencer", buildButtonStyle());
        button.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        button.setPosition(20, restartButtonY());
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onRestart();
            }
        });
        return button;
    }

    private Table buildTooltip() {
        Table t = new Table();
        t.setBackground(new TextureRegionDrawable(new TextureRegion(tooltipBackgroundTexture)));
        t.add(tooltipLabel).pad(8f);
        t.setVisible(false);
        return t;
    }

    // -------------------------------------------------------------------------
    // Positionnement de la table de jeu
    // -------------------------------------------------------------------------

    private float backgroundWidth() {
        return stage.getViewport().getWorldWidth() - BACKGROUND_SHRINK;
    }

    private float backgroundHeight() {
        return stage.getViewport().getWorldHeight() - BACKGROUND_SHRINK;
    }

    private float backgroundX() {
        return BACKGROUND_SHRINK / 2f;
    }

    private float backgroundY() {
        return BACKGROUND_SHRINK / 2f;
    }

    // -------------------------------------------------------------------------
    // Positionnement des barres de vie
    // -------------------------------------------------------------------------

    private float healthBarX() {
        return (stage.getViewport().getWorldWidth() - HEALTH_BAR_WIDTH) / 2f;
    }

    private float healthBarY() {
        return stage.getViewport().getWorldHeight() - HEALTH_BAR_HEIGHT - HEALTH_BAR_TOP_MARGIN;
    }

    private float playerHealthBarY() {
        return HEALTH_BAR_BOTTOM_MARGIN;
    }

    private float restartButtonY() {
        return stage.getViewport().getWorldHeight() - SCORE_LABEL_TOP_MARGIN - BUTTON_HEIGHT - RESTART_BUTTON_GAP;
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
        refreshPlayerHealthBar();
        refreshScoreLabel();

        if (isCombatOver()) {
            endCombat();
        } else {
            spinButton.setDisabled(true);
            drawButton.setDisabled(false);
        }

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

    /** Le combat est terminé dès que le joueur ou l'ennemi n'a plus de points de vie. */
    private boolean isCombatOver() {
        GameState gameState = gameController.getGameState();
        return gameState.getEnemy().isDefeated() || gameState.getPlayer().isDefeated();
    }

    private void endCombat() {
        spinButton.setDisabled(true);
        drawButton.setDisabled(true);
        restartButton.setVisible(true);
    }

    /** Recommence un combat : réinitialise le GameController et tout l'affichage. */
    private void onRestart() {
        gameController.restart();
        cancelCardDealAnimation();
        cardTable.clearChildren();
        slotTable.clearChildren();
        refreshHealthBar();
        refreshPlayerHealthBar();
        refreshScoreLabel();
        spinButton.setDisabled(true);
        drawButton.setDisabled(false);
        restartButton.setVisible(false);
    }

    // -------------------------------------------------------------------------
    // Rafraîchissement de l'affichage
    // -------------------------------------------------------------------------

    private void refreshCardTable(List<Card> hand) {
        cancelCardDealAnimation();
        cardTable.clearChildren();

        List<Image> cardImages = new ArrayList<>();
        for (Card card : hand) {
            Image cardImage = new Image(new TextureRegionDrawable(new TextureRegion(getCardTexture(card))));
            cardImage.setVisible(false); // révélée seulement à la fin de son animation de distribution
            addCardListeners(cardImage, card);
            cardTable.add(cardImage).size(CARD_WIDTH, CARD_HEIGHT).pad(10f);
            cardImages.add(cardImage);
        }
        cardTable.pack();

        float worldWidth = stage.getViewport().getWorldWidth();
        cardTable.setPosition((worldWidth - cardTable.getWidth()) / 2f, CARD_TABLE_Y);
        cardTable.validate();

        dealCards(cardImages);
    }

    /**
     * Anime l'arrivée des cartes : chacune part, dos visible, d'un point de
     * "pioche" au-dessus de l'écran, glisse jusqu'à sa place dans cardTable
     * (déjà calculée mais invisible), puis se retourne pour révéler sa face.
     */
    private void dealCards(List<Image> targets) {
        float deckX = cardTable.getX() + cardTable.getWidth() / 2f - CARD_WIDTH / 2f;
        float deckY = stage.getViewport().getWorldHeight();

        for (int i = 0; i < targets.size(); i++) {
            Image target = targets.get(i);
            Vector2 targetPos = target.localToStageCoordinates(new Vector2(0f, 0f));

            Image flyingCard = new Image(new TextureRegionDrawable(new TextureRegion(cardBackTexture)));
            flyingCard.setSize(CARD_WIDTH, CARD_HEIGHT);
            flyingCard.setOrigin(CARD_WIDTH / 2f, CARD_HEIGHT / 2f);
            flyingCard.setPosition(deckX, deckY);
            stage.addActor(flyingCard);
            flyingCards.add(flyingCard);

            flyingCard.addAction(Actions.sequence(
                Actions.delay(i * DEAL_STAGGER_DELAY),
                Actions.moveTo(targetPos.x, targetPos.y, DEAL_MOVE_DURATION, Interpolation.pow2Out),
                Actions.delay(FLIP_PAUSE_DELAY),
                Actions.scaleTo(0f, 1f, FLIP_HALF_DURATION),
                Actions.run(() -> flyingCard.setDrawable(target.getDrawable())),
                Actions.scaleTo(1f, 1f, FLIP_HALF_DURATION),
                Actions.run(() -> {
                    flyingCards.remove(flyingCard);
                    flyingCard.remove();
                    target.setVisible(true);
                })
            ));
        }
    }

    /** Retire toute carte encore en cours de distribution (ex : nouvelle donne avant la fin de l'animation). */
    private void cancelCardDealAnimation() {
        for (Image flyingCard : flyingCards) {
            flyingCard.remove();
        }
        flyingCards.clear();
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
        healthBarLabel.setText(enemy.getHp() + "/" + enemy.getMaxHp());
    }

    private void refreshPlayerHealthBar() {
        Player player = gameController.getGameState().getPlayer();
        float ratio = (float) player.getHp() / player.getMaxHp();
        playerHealthBarFill.setWidth(HEALTH_BAR_WIDTH * ratio);
        playerHealthBarLabel.setText(player.getHp() + "/" + player.getMaxHp());
    }

    private void refreshScoreLabel() {
        scoreLabel.setText("Points : " + gameController.getGameState().getPlayer().getGains());
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

        background.setSize(backgroundWidth(), backgroundHeight());
        background.setPosition(backgroundX(), backgroundY());
        healthBarBg.setPosition(healthBarX(), healthBarY());
        healthBarFill.setPosition(healthBarX(), healthBarY());
        healthBarLabel.setPosition(healthBarX(), healthBarY());
        playerHealthBarBg.setPosition(healthBarX(), playerHealthBarY());
        playerHealthBarFill.setPosition(healthBarX(), playerHealthBarY());
        playerHealthBarLabel.setPosition(healthBarX(), playerHealthBarY());
        scoreLabel.setPosition(20, worldHeight - SCORE_LABEL_TOP_MARGIN);
        restartButton.setPosition(20, restartButtonY());
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
        playerHealthBarBgTexture.dispose();
        playerHealthBarFillTexture.dispose();
        cardBackTexture.dispose();
        cardTextures.values().forEach(Texture::dispose);
        symbolTextures.values().forEach(Texture::dispose);
    }
}
