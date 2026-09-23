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
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
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
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.controllers.GameController;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.DrawResult;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolOutcome;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
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

    // Main du joueur : une rangée de cartes centrée, positionnée à la main
    // (et non via une Table) pour pouvoir animer le réagencement quand des
    // cartes sont piochées pendant le tour.
    private static final float HAND_CARD_GAP      = 20f;
    private static final float HAND_MOVE_DURATION = 0.25f;

    // Thème casino des boutons : fond sombre, liseré doré, police pixel art.
    // La largeur de chaque bouton s'adapte au texte qu'il contient (mesuré
    // via buttonWidth()) ; BUTTON_WIDTH n'est plus qu'un plancher minimal.
    private static final String BUTTON_FONT_PATH    = "fonts/Jersey10-Regular.ttf";
    private static final int    BUTTON_FONT_SIZE    = 30;
    private static final int    BUTTON_BORDER_PX    = 3;
    private static final float  BUTTON_TEXT_PADDING = 20f; // marge horizontale de chaque côté du texte
    private static final Color  BUTTON_GOLD         = Color.GOLDENROD;

    // Deck (dos visible) affiché au-dessus des boutons "Tirer" et "Lancer
    // machine" — c'est de là que partent les cartes distribuées. La défausse
    // est son symétrique, à droite de l'écran, à la même hauteur.
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
    private final CardDiscardAnimator cardDiscardAnimator;
    private final EffectPopupAnimator effectPopupAnimator;

    /**
     * Cartes animées en route vers la défausse, déjà comptées dans le modèle :
     * la défausse affichée ne les compte qu'à leur arrivée.
     */
    private int discardInFlight = 0;

    // -------------------------------------------------------------------------
    // Acteurs Scene2D
    // -------------------------------------------------------------------------

    private final Image      background;
    private final CardPileView deckPile;
    private final CardPileView discardPile;
    private final HealthBarView enemyHealthBar;
    private final HealthBarView playerHealthBar;
    private final Group      handGroup  = new Group();
    /** Calque des textes de bonus affichés quand une carte est jouée : au-dessus de la main, sous le voile des piles. */
    private final Group      popupLayer = new Group();
    /** Images des cartes de la main (non jouées), dans l'ordre d'affichage ; userObject = la Card. */
    private final List<Image> handImages = new ArrayList<>();
    private final Table      slotTable = new Table();
    private final Tooltip    tooltip;
    private final PileContentOverlay pileOverlay;
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
        cardDiscardAnimator = new CardDiscardAnimator(stage, cardBackTexture, CARD_WIDTH, CARD_HEIGHT, sounds.cardDeal, sounds.cardFlip);
        effectPopupAnimator = new EffectPopupAnimator(popupLayer);

        preloadSymbolTextures();

        background      = buildBackground();
        enemyHealthBar  = new HealthBarView(font, Color.valueOf("550000ff"), Color.RED, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
        playerHealthBar = new HealthBarView(font, Color.valueOf("005500ff"), Color.GREEN, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
        enemyHealthBar.setPosition(healthBarX(), healthBarY());
        playerHealthBar.setPosition(healthBarX(), playerHealthBarY());
        drawButton = buildDrawButton();
        spinButton    = buildSpinButton();
        spinButton.setDisabled(true);
        scoreLabel    = buildScoreLabel();
        restartButton = buildRestartButton();
        restartButton.setVisible(false);
        tooltip       = new Tooltip(font);
        // Piles positionnées une fois la largeur réelle des boutons connue.
        deckPile      = buildPile("Deck", this::onDeckClicked);
        deckPile.setPosition(deckX(), DECK_Y);
        discardPile   = buildPile("Defausse", this::onDiscardClicked);
        discardPile.setPosition(discardX(), DECK_Y);
        pileOverlay   = new PileContentOverlay(stage, font, tooltip, this::getCardTexture, CARD_WIDTH, CARD_HEIGHT);

        refreshHealthBar();
        refreshPlayerHealthBar();
        refreshPiles();

        stage.addActor(background);
        stage.addActor(deckPile);
        stage.addActor(discardPile);
        enemyHealthBar.addTo(stage);
        playerHealthBar.addTo(stage);
        stage.addActor(drawButton);
        stage.addActor(spinButton);
        stage.addActor(scoreLabel);
        stage.addActor(restartButton);
        stage.addActor(slotTable);
        stage.addActor(handGroup);
        stage.addActor(popupLayer);
        stage.addActor(pileOverlay.getActor()); // voile de consultation des piles, par-dessus le jeu
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

    /**
     * Pile de cartes dos visible (deck ou défausse) avec son compteur. Au survol,
     * une infobulle invite à cliquer ; au clic, {@code onClick} affiche son contenu.
     */
    private CardPileView buildPile(String name, Runnable onClick) {
        CardPileView pile = new CardPileView(name, cardBackTexture, font, CARD_WIDTH, CARD_HEIGHT);
        pile.addListener(new InputListener() {

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer != -1 || (fromActor != null && fromActor.isDescendantOf(pile))) return;
                tooltip.show("Cliquer pour voir les cartes", pile.getX(), pile.getLabelTopY() + 5f);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer != -1 || (toActor != null && toActor.isDescendantOf(pile))) return;
                tooltip.hide();
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                sounds.buttonClick.play();
                onClick.run();
                return true;
            }
        });
        return pile;
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
        String text = "Tirer 6 cartes";
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
    // Positionnement du deck et de la défausse
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

    /** Symétrique du deck par rapport au centre de l'écran : la défausse est ancrée à droite. */
    private float discardX() {
        return stage.getViewport().getWorldWidth() - deckX() - deckPile.getWidth();
    }

    // -------------------------------------------------------------------------
    // Interactions joueur — transmises au GameController
    // -------------------------------------------------------------------------

    /** Pioche une nouvelle main et lance son animation de distribution ; active le spin, désactive la pioche. */
    private void onDrawCards() {
        if (drawButton.isDisabled() || pileOverlay.isShown()) return;
        cardDealAnimator.cancel();
        clearHand();
        dealIntoHand(gameController.drawCards());
        spinButton.setDisabled(false);
        drawButton.setDisabled(true);
    }

    /**
     * Lance la machine à sous, envoie les cartes restées sur la table à la
     * défausse (animation), met à jour tout l'affichage (symboles, PV, score),
     * puis termine le combat si l'un des deux camps est vaincu, ou repasse la
     * main à la phase de pioche sinon.
     */
    private void onSpin() {
        if (spinButton.isDisabled() || pileOverlay.isShown()) return;
        TurnResult result = gameController.spin();
        discardHandWithAnimation();
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

    /** Affiche par-dessus le jeu les cartes restant dans le deck (triées, pas dans l'ordre de pioche). */
    private void onDeckClicked() {
        pileOverlay.show("Deck", gameController.getGameState().getPlayer().getDeck().getCards());
    }

    /** Affiche par-dessus le jeu les cartes de la défausse (triées). */
    private void onDiscardClicked() {
        pileOverlay.show("Defausse", gameController.getGameState().getPlayer().getDiscardPile().getCards());
    }

    /**
     * Journalise, pour chaque symbole tiré, son effet de base (voir
     * {@link Symbol#getDescription()}), ses dégâts finaux — base + bonus
     * d'attaque des cartes jouées, {@code avant} défense de l'ennemi — et le
     * résultat complet obtenu ce tour (dégâts réellement encaissés par
     * l'ennemi après défense, drain de vie, gains, etc.).
     */
    private void logSymbolOutcomes(TurnResult result) {
        for (SymbolOutcome outcome : result.getSymbolOutcomes()) {
            Symbol symbol = outcome.getSymbol();
            String resultText = outcome.getEvents().isEmpty()
                ? "aucun effet"
                : outcome.getEvents().stream().map(Event::describe).collect(Collectors.joining(", "));

            List<EnemyDamagedEvent> damageEvents = outcome.getEvents().stream()
                .filter(e -> e instanceof EnemyDamagedEvent)
                .map(e -> (EnemyDamagedEvent) e)
                .toList();
            String finalDamageText = damageEvents.isEmpty()
                ? ""
                : " | degats finaux (avec buffs, avant defense): " + damageEvents.stream().mapToInt(e -> e.rawDamage).sum();

            Gdx.app.log("GameScreen", symbol + " - base: " + symbol.getDescription()
                + finalDamageText + " | resultat: " + resultText);
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
     * Transmet la carte jouée au contrôleur (ses effets de tour seront appliqués
     * au prochain spin), affiche le texte de chacun de ses bonus à la place de
     * la carte, la retire de la main, déclenche l'effet de particules à l'endroit
     * cliqué (en coordonnées du Stage), puis distribue les cartes éventuellement
     * piochées par ses effets immédiats.
     */
    private void onCardPlayed(Card card, Image cardImage, float stageX, float stageY) {
        sounds.cardClick.play();
        Vector2 cardCenter = cardImage.localToStageCoordinates(new Vector2(CARD_WIDTH / 2f, CARD_HEIGHT / 2f));
        effectPopupAnimator.play(card.getEffects().stream()
            .flatMap(effect -> effect.getPopups().stream())
            .toList(), cardCenter.x, cardCenter.y);
        handImages.remove(cardImage);
        cardImage.remove();
        tooltip.hide();
        cardClickParticles.play(stageX, stageY);
        Gdx.app.log("GameScreen", "Carte jouee : " + card);

        DrawResult drawResult = gameController.playCard(card);
        refreshScoreLabel(); // une carte peut créditer des gains immédiatement
        if (!drawResult.getAddedToHand().isEmpty() || !drawResult.getDiscarded().isEmpty()) {
            dealIntoHand(drawResult);
        }
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
        cardDiscardAnimator.cancel();
        effectPopupAnimator.cancel();
        discardInFlight = 0;
        pileOverlay.hide();
        clearHand();
        refreshPiles();
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
     * Ajoute à la main les cartes piochées : chacune est placée (invisible) à sa
     * position finale, les cartes déjà présentes glissent pour recentrer la
     * rangée, puis l'animation de distribution les révèle depuis le deck. Les
     * cartes piochées sans place dans la main partent du deck vers la défausse.
     */
    private void dealIntoHand(DrawResult drawResult) {
        refreshPiles(); // le deck a déjà perdu les cartes piochées

        List<Image> newImages = new ArrayList<>();
        for (Card card : drawResult.getAddedToHand()) {
            Image cardImage = new Image(new TextureRegionDrawable(new TextureRegion(getCardTexture(card))));
            cardImage.setUserObject(card);
            cardImage.setSize(CARD_WIDTH, CARD_HEIGHT);
            cardImage.setVisible(false); // révélée seulement à la fin de son animation de distribution
            addCardListeners(cardImage, card);
            handGroup.addActor(cardImage);
            handImages.add(cardImage);
            newImages.add(cardImage);
        }
        layoutHand(true);
        cardDealAnimator.deal(newImages, deckPile.getTopX(), deckPile.getTopY());

        int overflow = drawResult.getDiscarded().size();
        if (overflow > 0) {
            discardInFlight += overflow;
            refreshPiles();
            cardDiscardAnimator.discardFromDeck(overflow,
                deckPile.getTopX(), deckPile.getTopY(),
                discardPile.getTopX(), discardPile.getTopY(),
                newImages.size() * 0.15f, // après la distribution des cartes gardées
                this::onCardLandedInDiscard);
        }
    }

    /**
     * Place les cartes de la main sur une rangée centrée. Avec {@code animate},
     * les cartes déjà révélées glissent vers leur nouvelle place ; les autres
     * (en cours de distribution) y sont placées directement.
     */
    private void layoutHand(boolean animate) {
        int count = handImages.size();
        float rowWidth = count * CARD_WIDTH + Math.max(0, count - 1) * HAND_CARD_GAP;
        float startX   = (stage.getViewport().getWorldWidth() - rowWidth) / 2f;

        for (int i = 0; i < count; i++) {
            Image cardImage = handImages.get(i);
            float x = startX + i * (CARD_WIDTH + HAND_CARD_GAP);
            cardImage.clearActions();
            if (animate && cardImage.isVisible()) {
                cardImage.addAction(Actions.moveTo(x, CARD_TABLE_Y, HAND_MOVE_DURATION, Interpolation.pow2Out));
            } else {
                cardImage.setPosition(x, CARD_TABLE_Y);
            }
        }
    }

    /** Fin de tour : les cartes restées sur la table se retournent puis glissent jusqu'à la défausse. */
    private void discardHandWithAnimation() {
        cardDealAnimator.cancel(); // révèle d'un coup les cartes encore en cours de distribution
        tooltip.hide();
        discardInFlight += handImages.size();
        refreshPiles();
        cardDiscardAnimator.discardFromTable(new ArrayList<>(handImages),
            discardPile.getTopX(), discardPile.getTopY(), this::onCardLandedInDiscard);
        handImages.clear();
    }

    /** Une carte animée vient d'arriver sur la défausse : elle y est désormais comptée. */
    private void onCardLandedInDiscard() {
        discardInFlight = Math.max(0, discardInFlight - 1);
        refreshPiles();
    }

    /** Retire toutes les cartes de la main affichée, sans animation. */
    private void clearHand() {
        handImages.clear();
        handGroup.clearChildren();
    }

    /** Met à jour les compteurs du deck et de la défausse (sans les cartes encore en vol vers celle-ci). */
    private void refreshPiles() {
        Player player = gameController.getGameState().getPlayer();
        deckPile.setCount(player.getDeck().getCards().size());
        discardPile.setCount(Math.max(0, player.getDiscardPile().size() - discardInFlight));
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
     * la machine si possible, Échap = fermer la consultation d'une pile).
     */
    @Override
    public void show() {
        InputAdapter keyboardInput = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE && pileOverlay.isShown()) {
                    pileOverlay.hide();
                    return true;
                }
                if (keycode == Input.Keys.SPACE && !drawButton.isDisabled()) {
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
        discardPile.setPosition(discardX(), DECK_Y);
        layoutHand(false);
        pileOverlay.hide();
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
        pileOverlay.dispose();
        effectPopupAnimator.dispose();
    }
}
