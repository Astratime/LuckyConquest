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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.animations.CardClickParticles;
import fr.astratime.lucky.animations.CardDealAnimator;
import fr.astratime.lucky.animations.CardDiscardAnimator;
import fr.astratime.lucky.animations.EffectPopupAnimator;
import fr.astratime.lucky.animations.JackpotCelebration;
import fr.astratime.lucky.assets.CardTextures;
import fr.astratime.lucky.assets.GameSounds;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.TableTextures;
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
import fr.astratime.lucky.entities.events.GainsEarnedEvent;
import fr.astratime.lucky.entities.events.JackpotEvent;
import fr.astratime.lucky.views.CasinoButtons;
import fr.astratime.lucky.views.CombatHud;
import fr.astratime.lucky.views.HandView;
import fr.astratime.lucky.views.PileContentOverlay;
import fr.astratime.lucky.views.PilesView;
import fr.astratime.lucky.views.PlayArea;
import fr.astratime.lucky.views.SidePanel;
import fr.astratime.lucky.views.SlotView;
import fr.astratime.lucky.views.TableView;
import fr.astratime.lucky.views.Tooltip;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Écran de combat. Responsabilité : orchestrer l'affichage et transmettre les
 * actions du joueur au GameController — aucune logique métier ici.
 *
 * L'affichage est réparti entre des vues dédiées : {@link TableView} (la table
 * de casino et ses emplacements), {@link HandView} (la main), {@link PilesView}
 * (deck et défausse), {@link SlotView} (symboles tirés et textes du tirage),
 * {@link CombatHud} (barres de vie), {@link SidePanel} (panneau latéral gauche :
 * gains). Ces vues se placent dans la zone de jeu ({@link PlayArea}), à droite
 * du panneau ; la main, les symboles et les piles se posent aux emplacements de
 * la table. Cet écran possède les ressources partagées, les boutons et le
 * clavier, et enchaîne les phases du tour (pioche, cartes jouées, spin, fin de
 * combat).
 */
public class GameScreen extends ScreenAdapter {

    // -------------------------------------------------------------------------
    // Constantes d'affichage
    // -------------------------------------------------------------------------

    private static final float  CARD_WIDTH     = 95f;
    private static final float  CARD_HEIGHT    = 135f;
    private static final String CARD_BACK_PATH = "cards/light/BACK.png";

    private static final float BUTTON_MARGIN = 20f;   // boutons en bas à gauche, sous la table

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
    private final Texture             cardBackTexture;
    private final CardTextures        cardTextures  = new CardTextures();
    private final CasinoButtons       buttons       = new CasinoButtons();
    private final HudTextures         hudTextures   = new HudTextures();
    private final TableTextures       tableTextures = new TableTextures();
    private final GameSounds          sounds        = new GameSounds();
    private final CardClickParticles  cardClickParticles = new CardClickParticles();
    private final EffectPopupAnimator effectPopupAnimator;
    private final JackpotCelebration  jackpotCelebration;
    private final Tooltip             tooltip;
    private final PileContentOverlay  pileOverlay;

    // -------------------------------------------------------------------------
    // Vues et acteurs Scene2D
    // -------------------------------------------------------------------------

    private final PlayArea   playArea;
    private final TableView  table;
    private final SidePanel  sidePanel;
    private final CombatHud  hud;
    private final PilesView  piles;
    private final HandView   hand;
    private final SlotView   slots;
    /** Calque des textes animés (bonus des cartes, résultats du tirage) : au-dessus du jeu, sous le voile des piles. */
    private final Group      popupLayer = new Group();
    private final TextButton drawButton;
    private final TextButton spinButton;
    private final TextButton restartButton;

    /**
     * Gains du dernier tirage dont le texte n'est pas encore apparu : le compteur
     * du panneau ne les ajoute qu'à l'apparition de leur texte « GAINS + ».
     */
    private int gainsNotYetShown = 0;

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
        cardBackTexture     = new Texture(Gdx.files.internal(CARD_BACK_PATH));
        effectPopupAnimator = new EffectPopupAnimator(popupLayer);
        tooltip             = new Tooltip(font);
        pileOverlay         = new PileContentOverlay(stage, font, tooltip, cardTextures::get, CARD_WIDTH, CARD_HEIGHT);

        playArea   = new PlayArea(stage, SidePanel.WIDTH);
        table      = new TableView(playArea, tableTextures, CARD_WIDTH, CARD_HEIGHT);
        sidePanel  = new SidePanel(hudTextures);
        jackpotCelebration = new JackpotCelebration(playArea, sidePanel::getCoinCenter, sidePanel::bumpCoin);
        hud        = new CombatHud(playArea, hudTextures);

        drawButton    = buttons.create("Tirer " + GameController.DEFAULT_DRAW_COUNT + " cartes", sounds.buttonClick, this::onDrawCards);
        spinButton    = buttons.create("Lancer machine", sounds.spinButton, this::onSpin);
        restartButton = buttons.create("Recommencer", sounds.buttonClick, this::onRestart);
        spinButton.setDisabled(true);
        restartButton.setVisible(false);
        sidePanel.setFooter(restartButton);

        piles = new PilesView(playArea, cardBackTexture, font, tooltip, sounds.buttonClick, CARD_WIDTH, CARD_HEIGHT,
            this::onDeckClicked, this::onDiscardClicked);
        hand  = new HandView(table, CARD_WIDTH, CARD_HEIGHT, cardTextures, tooltip, piles, this::player,
            new CardDealAnimator(stage, cardBackTexture, CARD_WIDTH, CARD_HEIGHT, sounds.cardDeal, sounds.cardFlip),
            new CardDiscardAnimator(stage, cardBackTexture, CARD_WIDTH, CARD_HEIGHT, sounds.cardDeal, sounds.cardFlip),
            this::onCardPlayed);
        slots = new SlotView(table, tooltip, effectPopupAnimator);

        layout();
        refreshAll();

        stage.addActor(table.getActor());
        stage.addActor(sidePanel.getActor()); // contient le bouton "Recommencer"
        piles.addTo(stage);
        hud.addTo(stage);
        stage.addActor(drawButton);
        stage.addActor(spinButton);
        stage.addActor(slots.getActor());
        stage.addActor(hand.getActor());
        stage.addActor(popupLayer);
        stage.addActor(jackpotCelebration); // par-dessus le jeu et le panneau : bloque les clics pendant la fête
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
        hud.refresh(gameController.getGameState());
        refreshGains(); // une carte peut créditer ou consommer des gains immédiatement

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
        table.setReelsRainbow(false); // la bordure d'un jackpot précédent s'arrête au lancer suivant
        gainsNotYetShown += result.getEvents().stream()
            .filter(event -> event instanceof GainsEarnedEvent)
            .mapToInt(event -> ((GainsEarnedEvent) event).amount)
            .sum();
        hand.discardAll();
        slots.show(result.getSymbols());
        slots.playResultPopups(result, hud.besidePlayerHealthBar(SlotView.POPUP_PLAYER_GAP), this::onEventShown);
        hud.refresh(gameController.getGameState());
        playSymbolResultSound(result);

        if (result.isJackpot()) {
            // La suite du tour attend la fin de la célébration (voir onJackpotShown).
            spinButton.setDisabled(true);
            drawButton.setDisabled(true);
        } else {
            finishTurn();
        }

        logSymbolOutcomes(result);
        Gdx.app.log("GameScreen", result.getEvents().stream()
            .map(Event::describe)
            .collect(Collectors.joining(" | ")));
    }

    /** Termine le tour : fin du combat si un camp est vaincu, sinon retour à la phase de pioche. */
    private void finishTurn() {
        if (isCombatOver()) {
            endCombat();
        } else {
            spinButton.setDisabled(true);
            drawButton.setDisabled(false);
        }
    }

    /** Le texte d'un événement du tirage vient d'apparaître : met à jour ce qui en dépend. */
    private void onEventShown(Event event) {
        if (event instanceof GainsEarnedEvent gains) {
            onGainsShown(gains.amount);
        } else if (event instanceof JackpotEvent) {
            onJackpotShown();
        }
    }

    /**
     * Le texte « JACKPOT ! » vient d'apparaître : bruitage du bingo, bordure
     * arc-en-ciel des rouleaux et célébration. Le tour se termine à la fin de
     * la célébration.
     */
    private void onJackpotShown() {
        sounds.bingoThreeSymbols.play();
        table.setReelsRainbow(true);
        jackpotCelebration.play(this::finishTurn);
    }

    /** Le texte d'un gain du tirage vient d'apparaître : le compteur du panneau l'ajoute à son tour. */
    private void onGainsShown(int amount) {
        gainsNotYetShown = Math.max(0, gainsNotYetShown - amount);
        refreshGains();
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
        effectPopupAnimator.cancel(); // les gains en attente ne seront jamais affichés
        jackpotCelebration.cancel();
        table.setReelsRainbow(false);
        gainsNotYetShown = 0;
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

    /**
     * Joue le bruitage correspondant au tirage : paire (2 identiques) ou aucun.
     * Celui du jackpot (bingo) accompagne sa célébration (voir onJackpotShown).
     */
    private void playSymbolResultSound(TurnResult result) {
        if (result.isPair()) {
            sounds.twoSymbols.play();
        } else if (!result.isJackpot()) {
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

    /** Met à jour les barres de vie, les gains et les compteurs du deck et de la défausse. */
    private void refreshAll() {
        hud.refresh(gameController.getGameState());
        refreshGains();
        piles.refresh(player());
    }

    /** Met à jour le compteur de gains, sans les gains du tirage dont le texte n'est pas encore apparu. */
    private void refreshGains() {
        sidePanel.setGains(player().getGains() - gainsNotYetShown);
    }

    /** Place (ou replace après un redimensionnement) les éléments qui dépendent de la taille de l'écran. */
    private void layout() {
        table.layout();
        drawButton.setPosition(playArea.getX() + BUTTON_MARGIN, BUTTON_MARGIN);
        spinButton.setPosition(drawButton.getX() + drawButton.getWidth() + BUTTON_MARGIN, BUTTON_MARGIN);
        sidePanel.layout(stage);
        hud.layout();
        piles.layout(table.getDeckX(), table.getPilesY());
        slots.layout();
        hand.layout(false);
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
        tableTextures.dispose();
        cardBackTexture.dispose();
        cardTextures.dispose();
        buttons.dispose();
        tooltip.dispose();
        hud.dispose();
        sidePanel.dispose();
        hudTextures.dispose();
        slots.dispose();
        sounds.dispose();
        cardClickParticles.dispose();
        pileOverlay.dispose();
        jackpotCelebration.dispose();
        effectPopupAnimator.dispose();
    }
}
