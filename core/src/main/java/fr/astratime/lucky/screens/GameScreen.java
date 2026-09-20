package fr.astratime.lucky.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
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
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolOutcome;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.events.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private static final String BACKGROUND_PATH = "playTable/play_table3.png";
    private static final String CARD_BACK_PATH  = "cards/" + THEME + "/BACK.png";
    private static final float  BACKGROUND_SHRINK = 100f;

    private static final float BUTTON_WIDTH  = 150f;
    private static final float BUTTON_HEIGHT = 60f;
    private static final float CARD_TABLE_Y  = 350f;

    // Thème casino des boutons : fond sombre, liseré doré, police pixel art.
    // La largeur de chaque bouton s'adapte au texte qu'il contient (mesuré
    // via buttonWidth()) ; BUTTON_WIDTH n'est plus qu'un plancher minimal.
    private static final String BUTTON_FONT_PATH    = "fonts/Jersey10-Regular.ttf";
    private static final int    BUTTON_FONT_SIZE    = 30;
    private static final int    BUTTON_BORDER_PX    = 3;
    private static final float  BUTTON_TEXT_PADDING = 20f; // marge horizontale de chaque côté du texte
    private static final Color  BUTTON_GOLD         = Color.GOLDENROD;

    // Pile de cartes (dos visible) affichée au-dessus des boutons "Tirer" et
    // "Lancer machine" — c'est de là que partent les cartes distribuées.
    private static final int   DECK_STACK_SIZE   = 4;
    private static final float DECK_STACK_OFFSET = 3f;
    private static final float DECK_TOP_MARGIN   = 20f;
    private static final float DECK_LEFT_SHIFT   = 40f;
    private static final float DECK_Y            = 20f + BUTTON_HEIGHT + DECK_TOP_MARGIN;

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
    private final BitmapFont buttonFont;
    private final Texture    backgroundTexture;
    private final Texture    buttonUpTexture;
    private final Texture    buttonDownTexture;
    private final Texture    buttonDisabledTexture;
    private final Texture    cardBackTexture;
    private final Map<String, Texture> cardTextures   = new HashMap<>();
    private final Map<Symbol, Texture> symbolTextures = new HashMap<>();

    private final GameSounds sounds = new GameSounds();
    private final CardClickParticles cardClickParticles = new CardClickParticles();
    private final CardDealAnimator cardDealAnimator;

    // -------------------------------------------------------------------------
    // Acteurs Scene2D
    // -------------------------------------------------------------------------

    private final Image      background;
    private final Group      deck;
    private final HealthBarView enemyHealthBar;
    private final HealthBarView playerHealthBar;
    private final Table      cardTable = new Table();
    private final Table      slotTable = new Table();
    private final Tooltip    tooltip;
    private       TextButton spinButton;
    private  TextButton drawButton;
    private       Label      scoreLabel;
    private       TextButton restartButton;

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------

    /**
     * Construit l'écran de jeu : charge toutes les textures/polices, crée
     * les acteurs Scene2D dans leur état initial et les ajoute au Stage
     * dans l'ordre de rendu voulu (arrière-plan d'abord, tooltip en dernier).
     *
     * @param luckyGame instance de jeu, utilisée pour le SpriteBatch partagé
     *                  et pour changer d'écran (non utilisé directement ici
     *                  mais conservé pour la cohérence avec les autres écrans)
     */
    public GameScreen(LuckyGame luckyGame) {
        this.luckyGame = luckyGame;
        // Le SpriteBatch est partagé avec LuckyGame et ne doit PAS être disposé ici.
        this.stage = new Stage(new ScreenViewport(), luckyGame.getBatch());

        font                     = new BitmapFont();
        buttonFont               = buildButtonFont();
        backgroundTexture        = new Texture(Gdx.files.internal(BACKGROUND_PATH));
        buttonUpTexture          = makeButtonTexture(Color.valueOf("1a1a1aff"), BUTTON_GOLD);
        buttonDownTexture        = makeButtonTexture(Color.valueOf("4a0000ff"), BUTTON_GOLD);
        buttonDisabledTexture    = makeButtonTexture(Color.valueOf("2a2a2aff"), Color.valueOf("6b5a2eff"));
        cardBackTexture            = new Texture(Gdx.files.internal(CARD_BACK_PATH));
        cardDealAnimator = new CardDealAnimator(stage, cardBackTexture, CARD_WIDTH, CARD_HEIGHT, sounds.cardDeal, sounds.cardFlip);

        preloadSymbolTextures();

        background      = buildBackground();
        enemyHealthBar  = new HealthBarView(font, Color.valueOf("550000ff"), Color.RED, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
        playerHealthBar = new HealthBarView(font, Color.valueOf("005500ff"), Color.GREEN, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
        enemyHealthBar.setPosition(healthBarX(), healthBarY());
        playerHealthBar.setPosition(healthBarX(), playerHealthBarY());
        drawButton = buildDrawButton();
        spinButton    = buildSpinButton();
        spinButton.setDisabled(true);
        deck          = buildDeck(); // positionné une fois la largeur réelle des boutons connue
        scoreLabel    = buildScoreLabel();
        restartButton = buildRestartButton();
        restartButton.setVisible(false);
        tooltip       = new Tooltip(font);

        refreshHealthBar();
        refreshPlayerHealthBar();

        stage.addActor(background);
        stage.addActor(deck);
        enemyHealthBar.addTo(stage);
        playerHealthBar.addTo(stage);
        stage.addActor(drawButton);
        stage.addActor(spinButton);
        stage.addActor(scoreLabel);
        stage.addActor(restartButton);
        stage.addActor(slotTable);
        stage.addActor(cardTable);
        stage.addActor(tooltip.getActor()); // en dernier : toujours au-dessus
    }

    // -------------------------------------------------------------------------
    // Construction des acteurs
    // -------------------------------------------------------------------------

    /** Image de la table de jeu, réduite et centrée (voir {@link #backgroundWidth()} et consorts). */
    private Image buildBackground() {
        Image img = new Image(new TextureRegionDrawable(new TextureRegion(backgroundTexture)));
        img.setSize(backgroundWidth(), backgroundHeight());
        img.setPosition(backgroundX(), backgroundY());
        return img;
    }

    /** Pile décorative de cartes dos visible, au-dessus des boutons : point de départ des cartes distribuées. */
    private Group buildDeck() {
        Group group = new Group();
        for (int i = 0; i < DECK_STACK_SIZE; i++) {
            Image card = new Image(new TextureRegionDrawable(new TextureRegion(cardBackTexture)));
            card.setSize(CARD_WIDTH, CARD_HEIGHT);
            card.setPosition(i * DECK_STACK_OFFSET, i * DECK_STACK_OFFSET);
            group.addActor(card);
        }
        group.setPosition(deckX(), DECK_Y);
        return group;
    }

    /** Génère la police pixel art dorée utilisée par les boutons, à partir de la police TrueType du thème. */
    private BitmapFont buildButtonFont() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(BUTTON_FONT_PATH));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size  = BUTTON_FONT_SIZE;
        parameter.color = BUTTON_GOLD;
        BitmapFont generated = generator.generateFont(parameter);
        generator.dispose(); // le générateur ne sert plus une fois la police créée
        return generated;
    }

    /** Bouton "Tirer 3 cartes", en bas à gauche de l'écran. */
    private TextButton buildDrawButton() {
        String text = "Tirer 3 cartes";
        TextButton button = new TextButton(text, buildButtonStyle());
        button.setSize(buttonWidth(text), BUTTON_HEIGHT);
        button.setPosition(20, 20);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sounds.buttonClick.play();
                onDrawCards();
            }
        });
        return button;
    }

    /** Bouton "Lancer machine", juste à droite du bouton de pioche. Son propre bruitage, plus marquant que les autres boutons. */
    private TextButton buildSpinButton() {
        String text = "Lancer machine";
        TextButton button = new TextButton(text, buildButtonStyle());
        button.setSize(buttonWidth(text), BUTTON_HEIGHT);
        button.setPosition(drawButton.getX() + drawButton.getWidth() + 20, 20);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sounds.spinButton.play();
                onSpin();
            }
        });
        return button;
    }

    /** Largeur d'un bouton adaptée à son texte (avec une marge), jamais plus petite que BUTTON_WIDTH. */
    private float buttonWidth(String text) {
        GlyphLayout layout = new GlyphLayout(buttonFont, text);
        return Math.max(BUTTON_WIDTH, layout.width + BUTTON_TEXT_PADDING * 2);
    }

    /** Style commun (thème casino) partagé par tous les boutons de l'écran. */
    private TextButton.TextButtonStyle buildButtonStyle() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font              = buttonFont;
        style.up                = new TextureRegionDrawable(new TextureRegion(buttonUpTexture));
        style.down              = new TextureRegionDrawable(new TextureRegion(buttonDownTexture));
        style.disabled          = new TextureRegionDrawable(new TextureRegion(buttonDisabledTexture));
        style.disabledFontColor = Color.valueOf("8a8a8aff");
        return style;
    }

    /** Label affichant les points (gains) du joueur, en haut à gauche de l'écran. */
    private Label buildScoreLabel() {
        Label label = new Label("Points : 0", new Label.LabelStyle(font, Color.WHITE));
        label.setPosition(20, stage.getViewport().getWorldHeight() - SCORE_LABEL_TOP_MARGIN);
        return label;
    }

    /** Bouton "Recommencer", affiché sous le score uniquement quand le combat est terminé. */
    private TextButton buildRestartButton() {
        String text = "Recommencer";
        TextButton button = new TextButton(text, buildButtonStyle());
        button.setSize(buttonWidth(text), BUTTON_HEIGHT);
        button.setPosition(20, restartButtonY());
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sounds.buttonClick.play();
                onRestart();
            }
        });
        return button;
    }

    // -------------------------------------------------------------------------
    // Positionnement de la table de jeu
    // -------------------------------------------------------------------------

    /** @return la largeur de l'image de table, réduite de BACKGROUND_SHRINK par rapport à l'écran. */
    private float backgroundWidth() {
        return stage.getViewport().getWorldWidth() - BACKGROUND_SHRINK;
    }

    /** @return la hauteur de l'image de table, réduite de BACKGROUND_SHRINK par rapport à l'écran. */
    private float backgroundHeight() {
        return stage.getViewport().getWorldHeight() - BACKGROUND_SHRINK;
    }

    /** @return l'abscisse de l'image de table, centrée horizontalement sur l'écran. */
    private float backgroundX() {
        return BACKGROUND_SHRINK / 2f;
    }

    /** @return l'ordonnée de l'image de table, centrée verticalement sur l'écran. */
    private float backgroundY() {
        return BACKGROUND_SHRINK / 2f;
    }

    // -------------------------------------------------------------------------
    // Positionnement des barres de vie
    // -------------------------------------------------------------------------

    /** @return l'abscisse commune aux deux barres de vie, centrées horizontalement sur l'écran. */
    private float healthBarX() {
        return (stage.getViewport().getWorldWidth() - HEALTH_BAR_WIDTH) / 2f;
    }

    /** @return l'ordonnée de la barre de vie de l'ennemi, ancrée en haut de l'écran. */
    private float healthBarY() {
        return stage.getViewport().getWorldHeight() - HEALTH_BAR_HEIGHT - HEALTH_BAR_TOP_MARGIN;
    }

    /** @return l'ordonnée de la barre de vie du joueur, ancrée à distance fixe du bas de l'écran. */
    private float playerHealthBarY() {
        return HEALTH_BAR_BOTTOM_MARGIN;
    }

    /** @return l'ordonnée du bouton "Recommencer", juste sous le label de score. */
    private float restartButtonY() {
        return stage.getViewport().getWorldHeight() - SCORE_LABEL_TOP_MARGIN - BUTTON_HEIGHT - RESTART_BUTTON_GAP;
    }

    // -------------------------------------------------------------------------
    // Positionnement du deck
    // -------------------------------------------------------------------------

    /**
     * Au-dessus des boutons "Tirer" et "Lancer machine", décalé vers la
     * gauche par rapport au centre de la rangée (DECK_LEFT_SHIFT).
     * Nécessite que drawButton/spinButton soient déjà construits.
     */
    private float deckX() {
        float buttonsLeft  = drawButton.getX();
        float buttonsRight = spinButton.getX() + spinButton.getWidth();
        float center       = (buttonsLeft + buttonsRight) / 2f;
        return center - CARD_WIDTH / 2f - DECK_LEFT_SHIFT;
    }

    /** @return l'abscisse de la carte du dessus de la pile : point de départ des cartes distribuées. */
    private float deckTopX() { return deckX() + (DECK_STACK_SIZE - 1) * DECK_STACK_OFFSET; }
    /** @return l'ordonnée de la carte du dessus de la pile : point de départ des cartes distribuées. */
    private float deckTopY() { return DECK_Y + (DECK_STACK_SIZE - 1) * DECK_STACK_OFFSET; }

    // -------------------------------------------------------------------------
    // Interactions joueur — transmises au GameController
    // -------------------------------------------------------------------------

    /** Pioche une nouvelle main et lance son animation de distribution ; active le spin, désactive la pioche. */
    private void onDrawCards() {
        List<Card> hand = gameController.drawCards();
        refreshCardTable(hand);
        spinButton.setDisabled(false);
        drawButton.setDisabled(true);
    }

    /**
     * Lance la machine à sous, met à jour tout l'affichage (symboles, PV,
     * score), puis termine le combat si l'un des deux camps est vaincu,
     * ou repasse la main à la phase de pioche sinon.
     */
    private void onSpin() {
        TurnResult result = gameController.spin();
        refreshSlotTable(result.getSymbols());
        refreshHealthBar();
        refreshPlayerHealthBar();
        refreshScoreLabel();
        playSymbolResultSound(result);

        if (isCombatOver()) {
            endCombat();
        } else {
            spinButton.setDisabled(true);
            drawButton.setDisabled(false);
        }

        logSymbolOutcomes(result);
        Gdx.app.log("GameScreen", result.getEvents().stream()
            .map(Event::describe)
            .collect(Collectors.joining(" | ")));
    }

    /**
     * Journalise, pour chaque symbole tiré, son effet de base (voir
     * {@link Symbol#getDescription()}) à côté du résultat réellement obtenu
     * ce tour une fois les bonus des cartes jouées appliqués (bonus
     * d'attaque/bouclier, multiplicateur de gains, drain de vie, etc.).
     */
    private void logSymbolOutcomes(TurnResult result) {
        for (SymbolOutcome outcome : result.getSymbolOutcomes()) {
            Symbol symbol = outcome.getSymbol();
            String resultText = outcome.getEvents().isEmpty()
                ? "aucun effet"
                : outcome.getEvents().stream().map(Event::describe).collect(Collectors.joining(", "));
            Gdx.app.log("GameScreen", symbol + " — base: " + symbol.getDescription() + " | resultat: " + resultText);
        }
    }

    /** Joue le bruitage correspondant au tirage : bingo (3 identiques), paire (2 identiques), ou aucun. */
    private void playSymbolResultSound(TurnResult result) {
        if (result.isJackpot()) {
            sounds.bingoThreeSymbols.play();
        } else if (result.isPair()) {
            sounds.twoSymbols.play();
        } else {
            sounds.oneSymbol.play();
        }
    }

    /**
     * Transmet la carte jouée au contrôleur (ses effets seront appliqués au
     * prochain spin), la retire de la main et déclenche l'effet de
     * particules à l'endroit cliqué (en coordonnées du Stage).
     */
    private void onCardPlayed(Card card, Image cardImage, float stageX, float stageY) {
        sounds.cardClick.play();
        gameController.playCard(card);
        cardImage.setVisible(false);
        tooltip.hide();
        cardClickParticles.play(stageX, stageY);
        Gdx.app.log("GameScreen", "Carte jouee : " + card);
    }

    /** Le combat est terminé dès que le joueur ou l'ennemi n'a plus de points de vie. */
    private boolean isCombatOver() {
        GameState gameState = gameController.getGameState();
        return gameState.getEnemy().isDefeated() || gameState.getPlayer().isDefeated();
    }

    /** Fige la partie (plus de pioche ni de spin) et affiche le bouton pour recommencer. */
    private void endCombat() {
        spinButton.setDisabled(true);
        drawButton.setDisabled(true);
        restartButton.setVisible(true);
    }

    /** Recommence un combat : réinitialise le GameController et tout l'affichage. */
    private void onRestart() {
        gameController.restart();
        cardDealAnimator.cancel();
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

    /**
     * Reconstruit la main affichée : place chaque carte (invisible) à sa
     * position finale dans {@code cardTable}, puis déclenche l'animation de
     * distribution qui les révèle progressivement.
     */
    private void refreshCardTable(List<Card> hand) {
        cardDealAnimator.cancel();
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

        cardDealAnimator.deal(cardImages, deckTopX(), deckTopY());
    }

    /** Reconstruit la rangée de symboles affichés après un spin, centrée horizontalement. */
    private void refreshSlotTable(Symbol[] symbols) {
        slotTable.clearChildren();
        for (Symbol symbol : symbols) {
            Image img = new Image(new TextureRegionDrawable(new TextureRegion(symbolTextures.get(symbol))));
            addSymbolListeners(img, symbol);
            slotTable.add(img).size(SYMBOL_WIDTH, SYMBOL_HEIGHT).pad(8f);
        }
        slotTable.pack();

        float worldWidth = stage.getViewport().getWorldWidth();
        slotTable.setPosition((worldWidth - slotTable.getWidth()) / 2f, SLOT_TABLE_Y);
    }

    /** Met à jour la largeur du remplissage et le texte "PV/PV max" de la barre de vie de l'ennemi. */
    private void refreshHealthBar() {
        Enemy enemy = gameController.getGameState().getEnemy();
        enemyHealthBar.refresh(enemy.getHp(), enemy.getMaxHp());
    }

    /** Met à jour la largeur du remplissage et le texte "PV/PV max" de la barre de vie du joueur. */
    private void refreshPlayerHealthBar() {
        Player player = gameController.getGameState().getPlayer();
        playerHealthBar.refresh(player.getHp(), player.getMaxHp());
    }

    /** Met à jour le label affichant les points (gains) du joueur. */
    private void refreshScoreLabel() {
        scoreLabel.setText("Points : " + gameController.getGameState().getPlayer().getGains());
    }

    // -------------------------------------------------------------------------
    // Listeners des cartes
    // -------------------------------------------------------------------------

    /**
     * Attache à une image de carte : l'affichage de sa description au survol
     * (dans la tooltip), et le fait de jouer la carte au clic.
     */
    private void addCardListeners(Image cardImage, Card card) {
        cardImage.addListener(new InputListener() {

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer != -1) return;
                Vector2 pos = cardImage.localToStageCoordinates(new Vector2(0, CARD_HEIGHT + 5f));
                tooltip.show(card.getDescription(), pos.x, pos.y);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer != -1) return;
                tooltip.hide();
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Vector2 stagePos = cardImage.localToStageCoordinates(new Vector2(x, y));
                onCardPlayed(card, cardImage, stagePos.x, stagePos.y);
                return true;
            }
        });
    }

    /** Attache à une image de symbole l'affichage de sa description au survol (dans la tooltip). Pas de clic : un symbole ne se joue pas. */
    private void addSymbolListeners(Image symbolImage, Symbol symbol) {
        symbolImage.addListener(new InputListener() {

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer != -1) return;
                Vector2 pos = symbolImage.localToStageCoordinates(new Vector2(0, SYMBOL_HEIGHT + 5f));
                tooltip.show(symbol.getDescription(), pos.x, pos.y);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer != -1) return;
                tooltip.hide();
            }
        });
    }

    // -------------------------------------------------------------------------
    // Gestion des textures
    // -------------------------------------------------------------------------

    /** Charge une fois pour toutes la texture de chaque symbole de la machine à sous. */
    private void preloadSymbolTextures() {
        for (Symbol symbol : Symbol.values()) {
            symbolTextures.put(symbol, new Texture(Gdx.files.internal(symbol.getAssetPath())));
        }
    }

    /** @return la texture de la carte, chargée à la demande puis mise en cache par chemin d'asset. */
    private Texture getCardTexture(Card card) {
        String path = card.getAssetPath(THEME);
        return cardTextures.computeIfAbsent(path, p -> new Texture(Gdx.files.internal(p)));
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

    /** Texture de bouton "casino" : fond plein entouré d'un liseré doré. */
    private Texture makeButtonTexture(Color fill, Color border) {
        int w = (int) BUTTON_WIDTH;
        int h = (int) BUTTON_HEIGHT;
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setColor(border);
        pixmap.fill();
        pixmap.setColor(fill);
        pixmap.fillRectangle(BUTTON_BORDER_PX, BUTTON_BORDER_PX, w - BUTTON_BORDER_PX * 2, h - BUTTON_BORDER_PX * 2);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    // -------------------------------------------------------------------------
    // Cycle de vie ScreenAdapter
    // -------------------------------------------------------------------------

    /**
     * Installe le processeur d'entrée de l'écran : le Stage (clics, survols)
     * en priorité, puis un raccourci clavier (Espace = piocher, F = lancer
     * la machine si possible).
     */
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

    /** Met à jour le viewport puis repositionne les acteurs ancrés en haut/bas de l'écran. */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        float worldWidth  = stage.getViewport().getWorldWidth();
        float worldHeight = stage.getViewport().getWorldHeight();

        background.setSize(backgroundWidth(), backgroundHeight());
        background.setPosition(backgroundX(), backgroundY());
        enemyHealthBar.setPosition(healthBarX(), healthBarY());
        playerHealthBar.setPosition(healthBarX(), playerHealthBarY());
        scoreLabel.setPosition(20, worldHeight - SCORE_LABEL_TOP_MARGIN);
        restartButton.setPosition(20, restartButtonY());
    }

    /** Efface l'écran, met à jour et dessine le Stage, puis l'effet de particules par-dessus. */
    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        stage.act(delta);
        stage.draw();

        SpriteBatch batch = luckyGame.getBatch();
        batch.begin();
        cardClickParticles.render(batch, delta);
        batch.end();
    }

    /** Libère toutes les ressources natives (Stage, polices, textures) possédées par cet écran. */
    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
        buttonFont.dispose();
        backgroundTexture.dispose();
        buttonUpTexture.dispose();
        buttonDownTexture.dispose();
        buttonDisabledTexture.dispose();
        tooltip.dispose();
        enemyHealthBar.dispose();
        playerHealthBar.dispose();
        cardBackTexture.dispose();
        cardTextures.values().forEach(Texture::dispose);
        symbolTextures.values().forEach(Texture::dispose);
        sounds.dispose();
        cardClickParticles.dispose();
    }
}
