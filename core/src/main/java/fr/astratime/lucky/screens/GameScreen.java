package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.animations.CardClickParticles;
import fr.astratime.lucky.animations.CardDealAnimator;
import fr.astratime.lucky.animations.CardDiscardAnimator;
import fr.astratime.lucky.animations.EffectPopupAnimator;
import fr.astratime.lucky.assets.CardTextures;
import fr.astratime.lucky.assets.GameSounds;
import fr.astratime.lucky.controllers.GameController;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.CardPlayResult;
import fr.astratime.lucky.entities.DrawResult;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolOutcome;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.views.CasinoButtons;
import fr.astratime.lucky.views.CombatHud;
import fr.astratime.lucky.views.HandView;
import fr.astratime.lucky.views.PileContentOverlay;
import fr.astratime.lucky.views.PilesView;
import fr.astratime.lucky.views.SlotView;
import fr.astratime.lucky.views.Tooltip;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Écran de combat. Responsabilité : orchestrer l'affichage et transmettre les
 * actions du joueur au GameController — aucune logique métier ici.
 *
 * L'affichage est réparti entre des vues dédiées : {@link HandView} (la main),
 * {@link PilesView} (deck et défausse), {@link SlotView} (symboles tirés et
 * textes du tirage), {@link CombatHud} (barres de vie et score). Cet écran
 * possède les ressources partagées, les boutons et le clavier, et enchaîne les
 * phases du tour (pioche, cartes jouées, spin, fin de combat).
 */
public class GameScreen extends ScreenAdapter {

    // -------------------------------------------------------------------------
    // Constantes d'affichage
    // -------------------------------------------------------------------------

    private static final float  CARD_WIDTH        = 95f;
    private static final float  CARD_HEIGHT       = 135f;
    private static final String BACKGROUND_PATH   = "playTable/play_table3.png";
    private static final String CARD_BACK_PATH    = "cards/light/BACK.png";
    private static final float  BACKGROUND_SHRINK = 100f;

    private static final float BUTTON_MARGIN      = 20f;
    private static final float RESTART_BUTTON_GAP = 10f;

    // Deck (dos visible) affiché au-dessus des boutons "Tirer" et "Lancer
    // machine", décalé vers la gauche : c'est de là que partent les cartes
    // distribuées. La défausse est son symétrique, à droite de l'écran.
    private static final float DECK_TOP_MARGIN = 20f;
    private static final float DECK_LEFT_SHIFT = 40f;
    private static final float DECK_Y          = BUTTON_MARGIN + CasinoButtons.HEIGHT + DECK_TOP_MARGIN;

    // -------------------------------------------------------------------------
    // Contrôleur — seul point d'accès à la logique de jeu
    // -------------------------------------------------------------------------

    private final GameController gameController = new GameController();

    // -------------------------------------------------------------------------
    // Ressources (à disposer dans dispose())
    // -------------------------------------------------------------------------

    private final LuckyGame           luckyGame;
    private final Stage               stage;
    private final BitmapFont          font;
    private final Texture             backgroundTexture;
    private final Texture             cardBackTexture;
    private final CardTextures        cardTextures  = new CardTextures();
    private final CasinoButtons       buttons       = new CasinoButtons();
    private final GameSounds          sounds        = new GameSounds();
    private final CardClickParticles  cardClickParticles = new CardClickParticles();
    private final EffectPopupAnimator effectPopupAnimator;
    private final Tooltip             tooltip;
    private final PileContentOverlay  pileOverlay;

    // -------------------------------------------------------------------------
    // Vues et acteurs Scene2D
    // -------------------------------------------------------------------------

    private final Image      background;
    private final CombatHud  hud;
    private final PilesView  piles;
    private final HandView   hand;
    private final SlotView   slots;
    /** Calque des textes animés (bonus des cartes, résultats du tirage) : au-dessus du jeu, sous le voile des piles. */
    private final Group      popupLayer = new Group();
    private final TextButton drawButton;
    private final TextButton spinButton;
    private final TextButton restartButton;

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------

    /**
     * Construit l'écran de jeu : charge les ressources, crée les vues dans leur
     * état initial et les ajoute au Stage dans l'ordre de rendu voulu
     * (arrière-plan d'abord, infobulle en dernier).
     *
     * @param luckyGame instance de jeu, qui fournit le SpriteBatch partagé
     */
    public GameScreen(LuckyGame luckyGame) {
        this.luckyGame = luckyGame;
        // Le SpriteBatch est partagé avec LuckyGame et ne doit PAS être disposé ici.
        this.stage = new Stage(new ScreenViewport(), luckyGame.getBatch());

        font                = new BitmapFont();
        backgroundTexture   = new Texture(Gdx.files.internal(BACKGROUND_PATH));
        cardBackTexture     = new Texture(Gdx.files.internal(CARD_BACK_PATH));
        effectPopupAnimator = new EffectPopupAnimator(popupLayer);
        tooltip             = new Tooltip(font);
        pileOverlay         = new PileContentOverlay(stage, font, tooltip, cardTextures::get, CARD_WIDTH, CARD_HEIGHT);

        background = new Image(new TextureRegionDrawable(new TextureRegion(backgroundTexture)));
        hud        = new CombatHud(stage, font);

        drawButton    = buttons.create("Tirer " + GameController.DEFAULT_DRAW_COUNT + " cartes", sounds.buttonClick, this::onDrawCards);
        spinButton    = buttons.create("Lancer machine", sounds.spinButton, this::onSpin);
        restartButton = buttons.create("Recommencer", sounds.buttonClick, this::onRestart);
        drawButton.setPosition(BUTTON_MARGIN, BUTTON_MARGIN);
        spinButton.setPosition(drawButton.getX() + drawButton.getWidth() + BUTTON_MARGIN, BUTTON_MARGIN);
        spinButton.setDisabled(true);
        restartButton.setVisible(false);

        piles = new PilesView(stage, cardBackTexture, font, tooltip, sounds.buttonClick, CARD_WIDTH, CARD_HEIGHT,
            this::onDeckClicked, this::onDiscardClicked);
        hand  = new HandView(stage, CARD_WIDTH, CARD_HEIGHT, cardTextures, tooltip, piles, this::player,
            new CardDealAnimator(stage, cardBackTexture, CARD_WIDTH, CARD_HEIGHT, sounds.cardDeal, sounds.cardFlip),
            new CardDiscardAnimator(stage, cardBackTexture, CARD_WIDTH, CARD_HEIGHT, sounds.cardDeal, sounds.cardFlip),
            this::onCardPlayed);
        slots = new SlotView(stage, tooltip, effectPopupAnimator);

        layout();
        refreshAll();

        stage.addActor(background);
        piles.addTo(stage);
        hud.addTo(stage);
        stage.addActor(drawButton);
        stage.addActor(spinButton);
        stage.addActor(restartButton);
        stage.addActor(slots.getActor());
        stage.addActor(hand.getActor());
        stage.addActor(popupLayer);
        stage.addActor(pileOverlay.getActor()); // voile de consultation des piles, par-dessus le jeu
        stage.addActor(tooltip.getActor());     // en dernier : toujours au-dessus
    }

    // -------------------------------------------------------------------------
    // Actions du joueur — transmises au GameController
    // -------------------------------------------------------------------------

    /** Pioche une nouvelle main et lance son animation de distribution ; active le spin, désactive la pioche. */
    private void onDrawCards() {
        if (drawButton.isDisabled() || pileOverlay.isShown()) return;
        hand.clear();
        hand.deal(gameController.drawCards());
        spinButton.setDisabled(false);
        drawButton.setDisabled(true);
    }

    /**
     * Transmet la carte jouée (déjà retirée de la main affichée) au contrôleur,
     * affiche le texte de chacun de ses bonus à sa place, déclenche l'effet de
     * particules à l'endroit cliqué, puis distribue les cartes éventuellement
     * piochées par ses effets immédiats.
     */
    private void onCardPlayed(Card card, Vector2 cardCenter, Vector2 clickPos) {
        sounds.cardClick.play();
        cardClickParticles.play(clickPos.x, clickPos.y);
        Gdx.app.log("GameScreen", "Carte jouee : " + card);

        CardPlayResult playResult = gameController.playCard(card);
        effectPopupAnimator.play(playResult.getPopups(), cardCenter.x, cardCenter.y);
        hud.refresh(gameController.getGameState()); // une carte peut créditer ou consommer des gains immédiatement

        DrawResult drawResult = playResult.getDrawResult();
        if (!drawResult.getAddedToHand().isEmpty() || !drawResult.getDiscarded().isEmpty()) {
            hand.deal(drawResult);
        }
    }

    /**
     * Lance la machine à sous, envoie les cartes restées sur la table à la
     * défausse (animation), affiche les symboles et les textes du tirage, met à
     * jour PV et score, puis termine le combat si l'un des deux camps est vaincu,
     * ou repasse la main à la phase de pioche sinon.
     */
    private void onSpin() {
        if (spinButton.isDisabled() || pileOverlay.isShown()) return;
        TurnResult result = gameController.spin();
        hand.discardAll();
        slots.show(result.getSymbols());
        slots.playResultPopups(result, hud.besidePlayerHealthBar(SlotView.POPUP_PLAYER_GAP));
        hud.refresh(gameController.getGameState());
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
        pileOverlay.show("Deck", player().getDeck().getCards());
    }

    /** Affiche par-dessus le jeu les cartes de la défausse (triées). */
    private void onDiscardClicked() {
        pileOverlay.show("Defausse", player().getDiscardPile().getCards());
    }

    /** Recommence un combat : réinitialise le GameController et tout l'affichage. */
    private void onRestart() {
        gameController.restart();
        hand.reset();
        effectPopupAnimator.cancel();
        piles.resetInFlight();
        pileOverlay.hide();
        slots.clear();
        refreshAll();
        spinButton.setDisabled(true);
        drawButton.setDisabled(false);
        restartButton.setVisible(false);
    }

    // -------------------------------------------------------------------------
    // Fin de combat, sons et journal
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Rafraîchissement et positionnement
    // -------------------------------------------------------------------------

    /** @return le joueur de la partie en cours (change à chaque nouveau combat). */
    private Player player() {
        return gameController.getGameState().getPlayer();
    }

    /** Met à jour les barres de vie, le score et les compteurs du deck et de la défausse. */
    private void refreshAll() {
        hud.refresh(gameController.getGameState());
        piles.refresh(player());
    }

    /** Place (ou replace après un redimensionnement) les éléments qui dépendent de la taille de l'écran. */
    private void layout() {
        float worldHeight = stage.getViewport().getWorldHeight();
        background.setSize(stage.getViewport().getWorldWidth() - BACKGROUND_SHRINK, worldHeight - BACKGROUND_SHRINK);
        background.setPosition(BACKGROUND_SHRINK / 2f, BACKGROUND_SHRINK / 2f);
        restartButton.setPosition(BUTTON_MARGIN,
            worldHeight - CombatHud.SCORE_LABEL_TOP_MARGIN - CasinoButtons.HEIGHT - RESTART_BUTTON_GAP);
        hud.layout();
        piles.layout(deckX(), DECK_Y);
        slots.layout();
        hand.layout(false);
    }

    /** Au-dessus des boutons "Tirer" et "Lancer machine", décalé vers la gauche par rapport au centre de la rangée. */
    private float deckX() {
        float center = (drawButton.getX() + spinButton.getX() + spinButton.getWidth()) / 2f;
        return center - CARD_WIDTH / 2f - DECK_LEFT_SHIFT;
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

    /** Met à jour le viewport puis repositionne les éléments qui dépendent de la taille de l'écran. */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        layout();
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

    /** Libère toutes les ressources natives (Stage, polices, textures, sons) possédées par cet écran. */
    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
        backgroundTexture.dispose();
        cardBackTexture.dispose();
        cardTextures.dispose();
        buttons.dispose();
        tooltip.dispose();
        hud.dispose();
        slots.dispose();
        sounds.dispose();
        cardClickParticles.dispose();
        pileOverlay.dispose();
        effectPopupAnimator.dispose();
    }
}
