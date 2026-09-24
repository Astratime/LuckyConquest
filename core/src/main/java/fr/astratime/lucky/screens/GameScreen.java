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
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.animations.CardClickParticles;
import fr.astratime.lucky.animations.CardDealAnimator;
import fr.astratime.lucky.animations.CardDiscardAnimator;
import fr.astratime.lucky.animations.CombatEndAnimation;
import fr.astratime.lucky.animations.Confetti;
import fr.astratime.lucky.animations.DamageVignette;
import fr.astratime.lucky.animations.EffectPopupAnimator;
import fr.astratime.lucky.animations.JackpotCelebration;
import fr.astratime.lucky.animations.ScreenShake;
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
import fr.astratime.lucky.entities.events.DamageReflectedEvent;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;
import fr.astratime.lucky.entities.events.JackpotEvent;
import fr.astratime.lucky.entities.events.PlayerDamagedEvent;
import fr.astratime.lucky.entities.events.PlayerHealedEvent;
import fr.astratime.lucky.settings.AudioSettings;
import fr.astratime.lucky.settings.VisualSettings;
import fr.astratime.lucky.views.CasinoButtons;
import fr.astratime.lucky.views.CombatHud;
import fr.astratime.lucky.views.HandView;
import fr.astratime.lucky.views.HealthBarView;
import fr.astratime.lucky.views.PileContentOverlay;
import fr.astratime.lucky.views.PilesView;
import fr.astratime.lucky.views.PlayArea;
import fr.astratime.lucky.views.SidePanel;
import fr.astratime.lucky.views.SlotView;
import fr.astratime.lucky.views.TableView;
import fr.astratime.lucky.views.Tooltip;

import java.util.List;
import java.util.function.ToIntFunction;
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

    /** Dégâts à partir desquels un coup sur l'ennemi fige l'image un instant (micro-arrêt). */
    private static final int   BIG_HIT               = 60;
    private static final float BIG_HIT_STOP          = 0.09f;
    /** Paire : clignotement doré des deux rouleaux et confettis lâchés par chacun. */
    private static final Color PAIR_GLOW             = Color.valueOf("ffd54aff");
    private static final float PAIR_GLOW_DURATION    = 1.6f;
    private static final int   PAIR_CONFETTI         = 45;
    /** Temps laissé aux textes du tirage (jusqu'à la riposte) avant d'annoncer la fin du combat. */
    private static final float RESULT_TEXTS_DURATION = 1.9f;

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
    private final VisualSettings      settings      = new VisualSettings();
    private final ScreenShake         screenShake   = new ScreenShake(settings);
    private final DamageVignette      damageVignette = new DamageVignette();
    private final Confetti            confetti;
    private final CombatEndAnimation  combatEnd;
    private final TableTextures       tableTextures = new TableTextures();
    private final GameSounds          sounds        = new GameSounds(new AudioSettings());
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
    private final TextButton effectsButton;

    /** Temps restant du micro-arrêt en cours (voir {@link #hitStop(float)}). */
    private float hitStop = 0f;

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
        confetti   = new Confetti(new TextureRegion(hudTextures.pixel));
        table      = new TableView(playArea, tableTextures, CARD_WIDTH, CARD_HEIGHT);
        sidePanel  = new SidePanel(hudTextures);
        jackpotCelebration = new JackpotCelebration(playArea, screenShake, settings,
            sidePanel::getCoinCenter, sidePanel::bumpCoin);
        hud        = new CombatHud(playArea, hudTextures, gameController::getGameState);

        drawButton    = buttons.createAction("Tirer " + GameController.DEFAULT_DRAW_COUNT + " cartes", sounds.buttonClick, this::onDrawCards);
        spinButton    = buttons.createAction("Lancer machine", sounds.spinButton, this::onSpin);
        restartButton = buttons.createAction("Recommencer", sounds.buttonClick, this::onRestart);
        spinButton.setDisabled(true);
        restartButton.setVisible(false);
        effectsButton = buttons.create(effectsLabel(), sounds.buttonClick, this::onToggleEffects);
        sidePanel.addFooter(restartButton);
        sidePanel.addFooter(effectsButton);

        piles = new PilesView(playArea, cardBackTexture, font, tooltip, sounds.buttonClick, CARD_WIDTH, CARD_HEIGHT,
            this::onDeckClicked, this::onDiscardClicked);
        hand  = new HandView(table, CARD_WIDTH, CARD_HEIGHT, cardTextures, tooltip, piles, this::player,
            new CardDealAnimator(stage, cardBackTexture, CARD_WIDTH, CARD_HEIGHT, sounds.cardDeal, sounds.cardFlip),
            new CardDiscardAnimator(stage, cardBackTexture, CARD_WIDTH, CARD_HEIGHT, sounds.cardDeal, sounds.cardFlip),
            this::onCardPlayed);
        slots = new SlotView(table, tooltip, effectPopupAnimator, sounds.cardClick);
        combatEnd = new CombatEndAnimation(playArea, screenShake, new TextureRegion(hudTextures.pixel),
            new TextureRegion(cardBackTexture), confetti, CARD_WIDTH, CARD_HEIGHT);

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
        stage.addActor(combatEnd);
        stage.addActor(confetti);
        stage.addActor(damageVignette);
        stage.addActor(jackpotCelebration); // par-dessus le jeu et le panneau : bloque les clics pendant la fête
        stage.addActor(pileOverlay.getActor()); // voile de consultation des piles, par-dessus le jeu
        stage.addActor(tooltip.getActor());     // en dernier : toujours au-dessus
        stage.addActor(screenShake);            // invisible : met à jour la caméra
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
        hud.refresh();
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
        int enemyHpBefore = gameController.getGameState().getEnemy().getHp();
        TurnResult result = gameController.spin();
        table.setReelsRainbow(false); // la bordure d'un jackpot précédent s'arrête au lancer suivant
        // Gains et PV du tirage ne se montrent qu'à l'apparition de leurs textes, après l'arrêt des rouleaux.
        gainsNotYetShown += sumOf(result, GainsEarnedEvent.class, gains -> gains.amount);
        hud.holdBack(enemyHpBefore - gameController.getGameState().getEnemy().getHp(),
            sumOf(result, PlayerDamagedEvent.class, hit -> hit.damage),
            sumOf(result, PlayerHealedEvent.class, heal -> heal.amount));
        hand.discardAll();
        spinButton.setDisabled(true);
        drawButton.setDisabled(true); // la suite du tour attend l'arrêt des rouleaux
        slots.spin(result.getSymbols(), () -> onReelsStopped(result));

        logSymbolOutcomes(result);
        Gdx.app.log("GameScreen", result.getEvents().stream()
            .map(Event::describe)
            .collect(Collectors.joining(" | ")));
    }

    /**
     * Les rouleaux sont arrêtés : textes du tirage, bruitage du résultat, puis fin
     * du tour. Un jackpot attend la fin de sa célébration ; un combat terminé
     * attend que tous les coups se soient affichés.
     */
    private void onReelsStopped(TurnResult result) {
        slots.playResultPopups(result, hud.besidePlayerHealthBar(SlotView.POPUP_PLAYER_GAP), this::onEventShown);
        playSymbolResultSound(result);
        if (result.isPair()) celebratePair(result.getSymbols());
        if (result.isJackpot()) return; // voir onJackpotShown
        if (isCombatOver()) {
            stage.addAction(Actions.delay(RESULT_TEXTS_DURATION, Actions.run(this::finishTurn)));
        } else {
            finishTurn();
        }
    }

    /** Paire : les deux rouleaux identiques clignotent en doré et lâchent une gerbe de confettis. */
    private void celebratePair(Symbol[] symbols) {
        for (int i = 0; i < symbols.length; i++) {
            for (int j = 0; j < symbols.length; j++) {
                if (i != j && symbols[i] == symbols[j]) {
                    table.highlightReel(i, PAIR_GLOW, 10f, PAIR_GLOW_DURATION);
                    Vector2 center = slots.getReelCenter(i);
                    confetti.burst(center.x, center.y, PAIR_CONFETTI);
                    break;
                }
            }
        }
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
        } else if (event instanceof EnemyDamagedEvent hit && hit.damage > 0) {
            onEnemyHit(hit.damage);
        } else if (event instanceof DamageReflectedEvent reflect && reflect.damage > 0) {
            onEnemyHit(reflect.damage);
        } else if (event instanceof PlayerDamagedEvent hit && hit.damage > 0) {
            onPlayerHit(hit.damage);
        } else if (event instanceof PlayerHealedEvent heal && heal.amount > 0) {
            hud.revealPlayerHeal(heal.amount);
        }
    }

    /** L'ennemi encaisse un coup : sa barre réagit ; un gros coup fige l'image un instant et secoue l'écran. */
    private void onEnemyHit(int damage) {
        hud.revealEnemyHit(damage);
        if (damage >= BIG_HIT) {
            hitStop(BIG_HIT_STOP);
            screenShake.shake(0.2f, 6f);
        }
    }

    /** Le joueur est touché : sa barre réagit, un voile rouge passe sur les bords et l'écran tremble. */
    private void onPlayerHit(int damage) {
        hud.revealPlayerHit(damage);
        damageVignette.flash(settings.isReducedEffects() ? 0.3f : 0.6f);
        screenShake.shake(0.3f, 9f);
    }

    /** @return la somme de {@code amount} sur les événements du tirage de type {@code type}. */
    private static <E extends Event> int sumOf(TurnResult result, Class<E> type, ToIntFunction<E> amount) {
        return result.getEvents().stream().filter(type::isInstance).map(type::cast).mapToInt(amount).sum();
    }

    /**
     * Le texte « JACKPOT ! » vient d'apparaître : bruitage du bingo, bordure
     * arc-en-ciel des rouleaux et célébration. Le tour se termine à la fin de
     * la célébration.
     */
    private void onJackpotShown() {
        sounds.bingoThreeSymbols.play();
        table.setReelsRainbow(true);
        table.setLightsParty(true);
        jackpotCelebration.play(() -> {
            table.setLightsParty(false);
            finishTurn();
        });
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

    /** Bascule entre effets normaux et réduits (secousses, flashs, micro-arrêts), et mémorise le choix. */
    private void onToggleEffects() {
        settings.setReducedEffects(!settings.isReducedEffects());
        effectsButton.setText(effectsLabel());
        if (settings.isReducedEffects()) screenShake.stop();
    }

    private String effectsLabel() {
        return settings.isReducedEffects() ? "Effets : reduits" : "Effets : normaux";
    }

    /** Fige l'animation pendant {@code duration} secondes pour marquer un gros coup (sauf effets réduits). */
    private void hitStop(float duration) {
        if (!settings.isReducedEffects()) hitStop = Math.max(hitStop, duration);
    }

    /** Recommence un combat : réinitialise le GameController et tout l'affichage. */
    private void onRestart() {
        gameController.restart();
        hand.reset();
        effectPopupAnimator.cancel(); // les gains en attente ne seront jamais affichés
        jackpotCelebration.cancel();
        table.setReelsRainbow(false);
        table.setLightsParty(false);
        combatEnd.reset();
        hud.getEnemyHealthBar().clearActions();
        hud.getEnemyHealthBar().getColor().a = 1f;
        hud.clearHeldBack();
        confetti.removeAll();
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

    /**
     * Fige la partie (plus de pioche ni de spin), met en scène la victoire ou la
     * défaite, puis affiche le bouton pour recommencer.
     */
    private void endCombat() {
        spinButton.setDisabled(true);
        drawButton.setDisabled(true);
        Runnable showRestart = () -> restartButton.setVisible(true);
        if (gameController.getGameState().getEnemy().isDefeated()) {
            table.setLightsParty(true);
            HealthBarView enemyBar = hud.getEnemyHealthBar();
            enemyBar.hit(Color.WHITE, 0.4f);
            enemyBar.addAction(Actions.fadeOut(0.5f)); // l'ennemi part en jetons
            combatEnd.playVictory(hud.getEnemyChipCenter(), showRestart);
        } else {
            combatEnd.playDefeat(showRestart);
        }
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
        hud.refresh();
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
        if (hitStop > 0f) {
            hitStop -= delta; // micro-arrêt : l'image reste figée un instant sur un gros coup
        } else {
            stage.act(delta);
        }
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
        damageVignette.dispose();
        combatEnd.dispose();
        effectPopupAnimator.dispose();
    }
}
