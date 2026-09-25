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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.animations.BingoCardAnimation;
import fr.astratime.lucky.animations.CardClickParticles;
import fr.astratime.lucky.animations.CardDealAnimator;
import fr.astratime.lucky.animations.CardDiscardAnimator;
import fr.astratime.lucky.animations.CombatEndAnimation;
import fr.astratime.lucky.animations.Confetti;
import fr.astratime.lucky.animations.DamageVignette;
import fr.astratime.lucky.animations.EffectPopupAnimator;
import fr.astratime.lucky.animations.JackpotCelebration;
import fr.astratime.lucky.animations.PistolShotAnimation;
import fr.astratime.lucky.animations.RainbowChipsAnimation;
import fr.astratime.lucky.animations.ScreenShake;
import fr.astratime.lucky.assets.CardTextures;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.GameSounds;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.TableTextures;
import fr.astratime.lucky.controllers.GameController;
import fr.astratime.lucky.controllers.PreparationResolver;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.CardPlayResult;
import fr.astratime.lucky.entities.DrawResult;
import fr.astratime.lucky.entities.GameState;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolOutcome;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.choices.BetChoice;
import fr.astratime.lucky.entities.choices.CardChoice;
import fr.astratime.lucky.entities.choices.RouletteChoice;
import fr.astratime.lucky.entities.events.DamageReflectedEvent;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;
import fr.astratime.lucky.entities.events.GainsLostEvent;
import fr.astratime.lucky.entities.events.JackpotEvent;
import fr.astratime.lucky.entities.events.PlayerDamagedEvent;
import fr.astratime.lucky.entities.events.PistolShotEvent;
import fr.astratime.lucky.entities.events.PlayerHealedEvent;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;
import fr.astratime.lucky.settings.AudioSettings;
import fr.astratime.lucky.settings.VisualSettings;
import fr.astratime.lucky.views.CardChoiceOverlay;
import fr.astratime.lucky.views.CardDetailOverlay;
import fr.astratime.lucky.views.CardImage;
import fr.astratime.lucky.views.CasinoButtons;
import fr.astratime.lucky.views.CombatHud;
import fr.astratime.lucky.views.HandView;
import fr.astratime.lucky.views.HealthBarView;
import fr.astratime.lucky.views.PileContentOverlay;
import fr.astratime.lucky.views.PilesView;
import fr.astratime.lucky.views.PlayArea;
import fr.astratime.lucky.views.ShopOverlay;
import fr.astratime.lucky.views.SidePanel;
import fr.astratime.lucky.views.SlotView;
import fr.astratime.lucky.views.TableView;
import fr.astratime.lucky.views.Tooltip;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    /** Temps laissé au texte de la riposte, après les autres textes du tirage, avant d'annoncer la fin du combat. */
    private static final float RESULT_TEXTS_MARGIN   = 0.4f;
    /** Joker transformé : gerbe de confettis violets sur son rouleau. */
    private static final int   JOKER_CONFETTI        = 30;
    /** Textes du tir du pistolet : sous la barre de vie de l'ennemi. */
    private static final float PISTOL_TEXT_BELOW     = 170f;
    /** Carte Bingo : hauteur (fraction de l'écran) où elle s'élève pour lancer ses faisceaux. */
    private static final float BINGO_CARD_HEIGHT     = 0.58f;
    /** Icône de l'échoppe : taille de l'image, marge au bord et agrandissement au survol. */
    private static final float SHOP_ICON_WIDTH  = 150f;
    private static final float SHOP_ICON_HEIGHT = 108f;
    private static final float SHOP_ICON_MARGIN = 30f;
    private static final float SHOP_HOVER_SCALE = 1.12f;
    /** Carte achetée : posée une fois l'échoppe refermée. */
    private static final float PURCHASE_DELAY   = 0.9f;
    /** Pot de Lutin qui apparaît : gerbe de confettis. */
    private static final int   POT_CONFETTI          = 50;
    /** Temps laissé au texte de la riposte (et au coup sur la barre) avant de terminer le tour. */
    private static final float RIPOSTE_TEXT_TIME     = 0.4f;

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
    private final BitmapFont          shopFont = Fonts.jersey(30, Color.valueOf("ffd454ff"), 2f, Color.valueOf("1a0f0fff"));
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
    private final CardChoiceOverlay   choiceOverlay;
    private final BingoCardAnimation  bingoAnimation;
    private final PistolShotAnimation pistolAnimation;
    private final RainbowChipsAnimation rainbowAnimation;
    private final ShopOverlay         shopOverlay;
    private final CardDetailOverlay   cardDetail;
    /** Icône de l'échoppe (en haut à droite de la zone de jeu), qui ouvre la boutique. */
    private final Group               shopIcon = new Group();

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
        tooltip             = new Tooltip(hudTextures);
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
        choiceOverlay   = new CardChoiceOverlay(stage, hudTextures, cardBackTexture, CARD_WIDTH, CARD_HEIGHT);
        bingoAnimation  = new BingoCardAnimation(settings, new TextureRegion(hudTextures.pixel));
        pistolAnimation = new PistolShotAnimation(hudTextures.pistol, new TextureRegion(hudTextures.pixel));
        rainbowAnimation = new RainbowChipsAnimation(settings, new TextureRegion(hudTextures.pixel));
        shopOverlay      = new ShopOverlay(stage, hudTextures, tooltip, CARD_WIDTH, CARD_HEIGHT);
        cardDetail       = new CardDetailOverlay(stage, hudTextures, tooltip, CARD_WIDTH, CARD_HEIGHT);
        hand.setOnInspect(this::showCardDetail);
        pileOverlay.setOnInspect(this::showCardDetail);
        buildShopIcon();

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
        stage.addActor(shopIcon);
        stage.addActor(pistolAnimation);
        stage.addActor(popupLayer);
        stage.addActor(combatEnd);
        stage.addActor(confetti);
        stage.addActor(damageVignette);
        stage.addActor(jackpotCelebration); // par-dessus le jeu et le panneau : bloque les clics pendant la fête
        stage.addActor(bingoAnimation);         // carte Bingo : faisceaux et téléportation, bloque les clics
        stage.addActor(rainbowAnimation);       // carte Arc-en-ciel : jetons et poussière d'étoiles, bloque les clics
        stage.addActor(choiceOverlay.getActor()); // choix demandé par une carte (Pari, Roulette russe)
        stage.addActor(shopOverlay.getActor());   // échoppe, ouverte depuis son icône
        stage.addActor(pileOverlay.getActor()); // voile de consultation des piles, par-dessus le jeu
        stage.addActor(cardDetail.getActor());  // fiche d'une carte, par-dessus tout le reste
        stage.addActor(tooltip.getActor());     // en dernier : toujours au-dessus
        stage.addActor(screenShake);            // invisible : met à jour la caméra
    }

    // -------------------------------------------------------------------------
    // Actions du joueur — transmises au GameController
    // -------------------------------------------------------------------------

    /** Pioche une nouvelle main et lance son animation de distribution ; active le spin, désactive la pioche. */
    private void onDrawCards() {
        if (drawButton.isDisabled() || isBusy()) return;
        hand.setLocked(false); // une carte achetée entre deux tours reste sur la table
        hand.deal(gameController.drawCards());
        spinButton.setDisabled(false);
        drawButton.setDisabled(true);
    }

    /**
     * Transmet la carte jouée (déjà retirée de la main affichée) au contrôleur,
     * affiche le texte de chacun de ses bonus à sa place, déclenche l'effet de
     * particules à l'endroit cliqué, puis distribue les cartes éventuellement
     * piochées par ses effets immédiats. Une carte qui demande un choix (Pari,
     * Roulette russe) ouvre sa fenêtre ; le Bingo bloque la main, s'élève dans
     * ses faisceaux lumineux, se téléporte sur la défausse, puis lance la machine.
     */
    private void onCardPlayed(Card card, CardImage image, Vector2 cardCenter, Vector2 clickPos) {
        sounds.cardClick.play();
        cardClickParticles.play(clickPos.x, clickPos.y);
        Gdx.app.log("GameScreen", "Carte jouee : " + card);

        CardPlayResult playResult = gameController.playCard(card);
        if (playResult.isAutoSpin()) {
            playBingo(image, playResult.getPopups());
        } else {
            hand.slam(image);
            effectPopupAnimator.play(playResult.getPopups(), cardCenter.x, cardCenter.y);
        }
        hud.refresh();
        refreshGains(); // une carte peut créditer ou consommer des gains immédiatement
        refreshEffects();

        DrawResult drawResult = playResult.getDrawResult();
        if (!drawResult.getAddedToHand().isEmpty() || !drawResult.getDiscarded().isEmpty()) {
            hand.deal(drawResult);
        }
        askChoice(playResult.getChoice(), cardCenter);
        if (playResult.getRainbow() != null) playRainbow(playResult.getRainbow());
    }

    /**
     * Arc-en-ciel : les jetons traversent l'écran ; chaque carte qui change de
     * couleur le fait au passage de son jeton. Puis le Pot de Lutin apparaît sur
     * la table, ou file dans la défausse si la table est pleine.
     */
    private void playRainbow(CardPlayResult.Rainbow rainbow) {
        if (!rainbow.addedToHand()) piles.addInFlight(1); // compté dans la défausse à son arrivée
        List<CardPlayResult.Recolor> recolored = new ArrayList<>();
        List<Vector2> targets = new ArrayList<>();
        for (CardPlayResult.Recolor recolor : rainbow.recolored()) {
            Vector2 center = hand.centerOf(recolor.before());
            if (center == null) continue;
            recolored.add(recolor);
            targets.add(center);
        }
        rainbowAnimation.play(targets,
            index -> {
                CardPlayResult.Recolor recolor = recolored.get(index);
                hand.recolor(recolor.before(), recolor.after());
                sounds.cardFlip.play();
            },
            () -> {
                Vector2 at = rainbow.addedToHand() ? hand.conjure(rainbow.added()) : potToDiscard(rainbow.added());
                confetti.burst(at.x, at.y, POT_CONFETTI);
                effectPopupAnimator.play(List.of(new EffectPopup("POT DE LUTIN !", EffectPopup.Style.GAINS,
                    PopupScale.MAX_INTENSITY)), at.x, at.y + CARD_HEIGHT * 0.8f);
                if (!recolored.isEmpty()) {
                    effectPopupAnimator.play(List.of(new EffectPopup("TOUT EN " + suitName(rainbow.suit()) + " !",
                        EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY)),
                        playArea.getCenterX(), table.getHandRowY() + CARD_HEIGHT * 2f);
                }
                sounds.twoSymbols.play();
            });
    }

    /** @return le nom affiché de {@code suit}, en majuscules. */
    private static String suitName(Card.Suit suit) {
        return switch (suit) {
            case COEUR   -> "COEUR";
            case CARREAU -> "CARREAU";
            case TREFLE  -> "TRÈFLE";
            case PIQUE   -> "PIQUE";
        };
    }

    /** Icône de l'échoppe : l'étal du marché et son nom ; au survol elle grossit, au clic elle ouvre la boutique. */
    private void buildShopIcon() {
        Image stall = new Image(new TextureRegionDrawable(new TextureRegion(hudTextures.shop)));
        stall.setSize(SHOP_ICON_WIDTH, SHOP_ICON_HEIGHT);
        Label name = new Label("ÉCHOPPE", new Label.LabelStyle(shopFont, Color.WHITE));
        name.pack();
        name.setPosition((SHOP_ICON_WIDTH - name.getWidth()) / 2f, 0f);
        stall.setPosition(0f, name.getHeight());
        shopIcon.addActor(stall);
        shopIcon.addActor(name);
        shopIcon.setSize(SHOP_ICON_WIDTH, SHOP_ICON_HEIGHT + name.getHeight());
        shopIcon.setOrigin(SHOP_ICON_WIDTH / 2f, 0f);
        shopIcon.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                if (pointer != -1) return;
                shopIcon.clearActions();
                shopIcon.addAction(Actions.scaleTo(SHOP_HOVER_SCALE, SHOP_HOVER_SCALE, 0.12f, Interpolation.pow2Out));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                if (pointer != -1) return;
                shopIcon.clearActions();
                shopIcon.addAction(Actions.scaleTo(1f, 1f, 0.12f));
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                openShop();
            }
        });
    }

    /**
     * Ouvre l'échoppe, sauf pendant une animation, une fenêtre ou la résolution
     * d'un tirage (ni pioche ni lancer possibles), ou une fois le combat terminé.
     */
    private void openShop() {
        if (isBusy() || isCombatOver() || (drawButton.isDisabled() && spinButton.isDisabled())) return;
        sounds.buttonClick.play();
        shopOverlay.show(gameController.getShopOffers(), () -> player().getGains(), cardTextures::get,
            (text, action) -> buttons.create(text, sounds.buttonClick, action),
            offer -> {
                GameController.Purchase purchase = gameController.buy(offer);
                if (purchase == null) return false;
                refreshGains();
                stage.addAction(Actions.delay(PURCHASE_DELAY, Actions.run(() -> placePurchase(purchase))));
                return true;
            },
            offer -> showCardDetail(offer.card()));
    }

    /** Affiche la fiche de {@code card} (avec son prix si elle est vendue à l'échoppe), par-dessus tout le reste. */
    private void showCardDetail(Card card) {
        Integer price = gameController.getShopOffers().stream()
            .filter(offer -> offer.card().getId().equals(card.getId()))
            .map(GameController.ShopOffer::price).findFirst().orElse(null);
        cardDetail.show(card, cardTextures.get(card), price);
        tooltip.getActor().toFront();
    }

    /** Carte achetée : elle apparaît sur la table s'il y a de la place, sinon elle file dans le deck. */
    private void placePurchase(GameController.Purchase purchase) {
        Card card = purchase.card();
        Vector2 at;
        if (purchase.addedToHand()) {
            at = hand.conjure(card);
        } else {
            CardImage image = new CardImage(new TextureRegionDrawable(new TextureRegion(cardTextures.get(card))));
            image.setSize(CARD_WIDTH, CARD_HEIGHT);
            image.setTouchable(Touchable.disabled);
            image.setOrigin(CARD_WIDTH / 2f, CARD_HEIGHT / 2f);
            float x = piles.deck().getTopX(), y = piles.deck().getTopY();
            image.setPosition(x, y + CARD_HEIGHT * 0.6f);
            image.setScale(0f);
            stage.addActor(image);
            image.addAction(Actions.sequence(
                Actions.scaleTo(1f, 1f, 0.35f, Interpolation.swingOut),
                Actions.delay(0.4f),
                Actions.moveTo(x, y, 0.3f, Interpolation.pow2In),
                Actions.run(() -> piles.refresh(player())),
                Actions.removeActor()));
            at = new Vector2(x + CARD_WIDTH / 2f, y + CARD_HEIGHT * 1.1f);
        }
        confetti.burst(at.x, at.y, POT_CONFETTI);
        effectPopupAnimator.play(List.of(new EffectPopup(card.getName().toUpperCase() + " !", EffectPopup.Style.SPECIAL,
            PopupScale.MAX_INTENSITY)), at.x, at.y + CARD_HEIGHT * 0.8f);
    }

    /** Table pleine : le Pot de Lutin apparaît au-dessus de la défausse et s'y pose. @return son centre */
    private Vector2 potToDiscard(Card pot) {
        CardImage image = new CardImage(new TextureRegionDrawable(new TextureRegion(cardTextures.get(pot))));
        image.setSize(CARD_WIDTH, CARD_HEIGHT);
        image.setTouchable(Touchable.disabled);
        image.setOrigin(CARD_WIDTH / 2f, CARD_HEIGHT / 2f);
        float x = piles.discard().getTopX(), y = piles.discard().getTopY();
        image.setPosition(x, y + CARD_HEIGHT * 0.6f);
        image.setScale(0f);
        stage.addActor(image);
        image.addAction(Actions.sequence(
            Actions.scaleTo(1f, 1f, 0.35f, Interpolation.swingOut),
            Actions.delay(0.4f),
            Actions.moveTo(x, y, 0.3f, Interpolation.pow2In),
            Actions.run(() -> {
                piles.onCardLanded();
                piles.refresh(player());
            }),
            Actions.removeActor()));
        return new Vector2(x + CARD_WIDTH / 2f, y + CARD_HEIGHT * 1.1f);
    }

    /** Ouvre la fenêtre du choix demandé par la carte jouée (rien si elle n'en demande pas). */
    private void askChoice(CardChoice choice, Vector2 cardCenter) {
        if (choice instanceof BetChoice) {
            choiceOverlay.showBet(gameController.getBetOptions(), slots::regionOf, symbol -> {
                effectPopupAnimator.play(gameController.placeBet(symbol), cardCenter.x, cardCenter.y);
                refreshEffects();
            });
        } else if (choice instanceof RouletteChoice roulette) {
            choiceOverlay.showRoulette(roulette.cursed(), roulette.pistolMultiplier(), roulette.penaltyPercent(),
                index -> {
                    GameController.RouletteOutcome outcome = gameController.pickRouletteCard(index);
                    effectPopupAnimator.play(outcome.popups(), cardCenter.x, cardCenter.y);
                    if (outcome.cursed()) {
                        damageVignette.flash(settings.isReducedEffects() ? 0.3f : 0.6f);
                        screenShake.shake(0.3f, 9f);
                    }
                    refreshGains();
                });
        }
    }

    /**
     * Bingo : plus aucune carte ne peut être jouée ; la carte s'élève au-dessus
     * de la table dans ses faisceaux, se téléporte sur la défausse, puis la
     * machine se lance d'elle-même.
     */
    private void playBingo(CardImage image, List<EffectPopup> popups) {
        hand.setLocked(true);
        spinButton.setDisabled(true);
        drawButton.setDisabled(true);
        float centerX = playArea.getCenterX();
        float centerY = stage.getViewport().getWorldHeight() * BINGO_CARD_HEIGHT;
        effectPopupAnimator.play(popups, centerX, centerY + CARD_HEIGHT * 1.3f, 0.5f);
        sounds.bingoThreeSymbols.play();
        bingoAnimation.play(image, centerX, centerY, piles.discard().getTopX(), piles.discard().getTopY(),
            this::spin);
    }

    /**
     * Lance la machine à sous, envoie les cartes restées sur la table à la
     * défausse (animation), affiche les symboles et les textes du tirage, met à
     * jour PV et score, puis termine le combat si l'un des deux camps est vaincu,
     * ou repasse la main à la phase de pioche sinon.
     */
    private void onSpin() {
        if (spinButton.isDisabled() || isBusy()) return;
        spin();
    }

    /** @return {@code true} si une fenêtre ou une animation attend : ni pioche ni lancer possibles. */
    private boolean isBusy() {
        return pileOverlay.isShown() || choiceOverlay.isShown() || shopOverlay.isShown() || cardDetail.isShown()
            || bingoAnimation.isPlaying() || rainbowAnimation.isPlaying();
    }

    /** Lance la machine (bouton, touche F, ou d'elle-même après une carte Bingo). */
    private void spin() {
        int enemyHpBefore = gameController.getGameState().getEnemy().getHp();
        TurnResult result = gameController.spin();
        table.setReelsRainbow(false); // la bordure d'un jackpot précédent s'arrête au lancer suivant
        // Gains et PV du tirage ne se montrent qu'à l'apparition de leurs textes, après l'arrêt des rouleaux.
        gainsNotYetShown += sumOf(result, GainsEarnedEvent.class, gains -> gains.amount)
            - sumOf(result, GainsLostEvent.class, lost -> lost.amount);
        hud.holdBack(enemyHpBefore - gameController.getGameState().getEnemy().getHp(),
            sumOf(result, PlayerDamagedEvent.class, hit -> hit.damage),
            sumOf(result, PlayerHealedEvent.class, heal -> heal.amount));
        hand.discardAll();
        spinButton.setDisabled(true);
        drawButton.setDisabled(true); // la suite du tour attend l'arrêt des rouleaux
        slots.spin(result.getDrawnSymbols(), result.getSymbols(), this::onJokerTransformed,
            () -> onReelsStopped(result));
        refreshEffects(); // les symboles retirés se rapprochent de leur retour

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
        // Un jackpot fait attendre la riposte de l'ennemi jusqu'à la fin de sa célébration.
        float riposteDelay = result.isJackpot() ? JackpotCelebration.DURATION : 0f;
        Vector2 enemyBar    = hud.getEnemyBarCenter();
        Vector2 pistolTexts = new Vector2(enemyBar.x, enemyBar.y - PISTOL_TEXT_BELOW);
        float shotAt = slots.playResultPopups(result, hud.besidePlayerHealthBar(SlotView.POPUP_PLAYER_GAP),
            pistolTexts, this::onEventShown, riposteDelay);
        if (shotAt >= 0f) aimPistol(result, enemyBar, shotAt);
        refreshEffects(); // jauges vidées ou remplies par le tirage
        playSymbolResultSound(result);
        if (result.isPair()) celebratePair(result.getSymbols());
        if (result.isJackpot()) return; // voir onJackpotShown
        if (isCombatOver()) {
            stage.addAction(Actions.delay(SlotView.popupsDuration(result) + RESULT_TEXTS_MARGIN,
                Actions.run(this::finishTurn)));
        } else {
            finishTurn();
        }
    }

    /** Le pistolet surgit au-dessus du symbole qu'il multiplie et tire sur l'ennemi à l'instant {@code shotAt}. */
    private void aimPistol(TurnResult result, Vector2 target, float shotAt) {
        PistolShotEvent shot = result.getPistolEvents().stream()
            .filter(e -> e instanceof PistolShotEvent).map(e -> (PistolShotEvent) e).findFirst().orElse(null);
        if (shot == null) return; // contre-attaque du Coffre seule : pas de pistolet
        Vector2 from = slots.getReelCenter(shot.slotIndex >= 0 ? shot.slotIndex : 1);
        stage.addAction(Actions.delay(Math.max(0f, shotAt - PistolShotAnimation.AIM_TIME),
            Actions.run(() -> pistolAnimation.play(from, target))));
    }

    /** Un Joker se transforme : texte « JOKER ! » et confettis sur son rouleau. */
    private void onJokerTransformed(int reel) {
        Vector2 center = slots.getReelCenter(reel);
        effectPopupAnimator.play(List.of(new EffectPopup("JOKER !", EffectPopup.Style.SPECIAL,
            PopupScale.SECONDARY_INTENSITY)), center.x, center.y + SlotView.CELL_HEIGHT / 2f);
        confetti.burst(center.x, center.y, JOKER_CONFETTI);
        sounds.twoSymbols.play();
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
        } else if (event instanceof GainsLostEvent lost) {
            onGainsShown(-lost.amount);
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
     * arc-en-ciel des rouleaux et célébration. La riposte de l'ennemi attend la
     * fin de la célébration, et le tour se termine après elle.
     */
    private void onJackpotShown() {
        sounds.bingoThreeSymbols.play();
        table.setReelsRainbow(true);
        table.setLightsParty(true);
        jackpotCelebration.play(() -> {
            table.setLightsParty(false);
            // La riposte de l'ennemi s'affiche maintenant (voir onReelsStopped) : le tour se termine après elle.
            stage.addAction(Actions.delay(SlotView.RIPOSTE_AFTER_BONUS + RIPOSTE_TEXT_TIME,
                Actions.run(this::finishTurn)));
        });
    }

    /** Le texte d'un gain (ou d'une perte) du tirage vient d'apparaître : le compteur du panneau le suit. */
    private void onGainsShown(int amount) {
        gainsNotYetShown -= amount;
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
        bingoAnimation.cancel();
        pistolAnimation.cancel();
        rainbowAnimation.cancel();
        shopOverlay.hide();
        cardDetail.hide();
        choiceOverlay.hide();
        hand.setLocked(false);
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

    /** Met à jour les barres de vie, les gains, les effets actifs et les compteurs du deck et de la défausse. */
    private void refreshAll() {
        hud.refresh();
        refreshGains();
        refreshEffects();
        piles.refresh(player());
    }

    /**
     * Met à jour les effets de cartes actifs du panneau latéral : symboles
     * retirés des rouleaux (et tirages restants), Porte-bonheur, paris en cours.
     */
    private void refreshEffects() {
        List<SidePanel.EffectRow> rows = new ArrayList<>();
        LastingEffects lasting = player().getLastingEffects();
        TextureRegion cross = new TextureRegion(hudTextures.iconCross);
        for (Map.Entry<Symbol, Integer> removed : lasting.getRemovedSymbols().entrySet()) {
            int turns = removed.getValue();
            rows.add(new SidePanel.EffectRow(slots.regionOf(removed.getKey()), cross,
                "Retiré " + turns + (turns > 1 ? " tours" : " tour")));
        }
        if (lasting.getGainBonus() > 0f) {
            rows.add(new SidePanel.EffectRow(new TextureRegion(hudTextures.iconClover), null,
                "Gains +" + Math.round(lasting.getGainBonus() * 100f) + " %"));
        }
        if (lasting.getCorruptionTurns() > 0) {
            int turns = lasting.getCorruptionTurns();
            rows.add(new SidePanel.EffectRow(new TextureRegion(hudTextures.iconCorruption), null,
                "Corruption " + turns + (turns > 1 ? " tours" : " tour")));
        }
        if (lasting.getBlades() > 0) {
            rows.add(new SidePanel.EffectRow(new TextureRegion(hudTextures.iconBlade), null,
                "Lames " + lasting.getBlades() + " (+" + lasting.getBlades() * PreparationResolver.BLADE_ATTACK + ")"));
        }
        if (lasting.getBlood() > 0) {
            rows.add(new SidePanel.EffectRow(new TextureRegion(hudTextures.iconBlood), null,
                "Sang " + lasting.getBlood()));
        }
        if (lasting.getVault() > 0) {
            rows.add(new SidePanel.EffectRow(new TextureRegion(hudTextures.iconVault), null,
                "Coffre " + lasting.getVault()));
        }
        for (Symbol bet : gameController.getBetsThisTurn()) {
            rows.add(new SidePanel.EffectRow(slots.regionOf(bet), null, "Pari x2 à x4"));
        }
        sidePanel.setActiveEffects(rows);
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
        shopIcon.setPosition(playArea.getX() + playArea.getWidth() - SHOP_ICON_WIDTH - SHOP_ICON_MARGIN,
            playArea.getHeight() - shopIcon.getHeight() - SHOP_ICON_MARGIN);
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
                if (keycode == Input.Keys.ESCAPE && cardDetail.isShown()) {
                    cardDetail.hide();
                    return true;
                }
                if (cardDetail.isShown()) return true;
                if (keycode == Input.Keys.ESCAPE && shopOverlay.isShown()) {
                    shopOverlay.hide();
                    return true;
                }
                if (choiceOverlay.isShown() || shopOverlay.isShown() || bingoAnimation.isPlaying()
                    || rainbowAnimation.isPlaying()) {
                    return true; // un choix ou une animation de carte est en cours
                }
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
        choiceOverlay.layout();
        shopOverlay.layout();
        cardDetail.layout();
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
        shopFont.dispose();
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
        choiceOverlay.dispose();
        bingoAnimation.dispose();
        rainbowAnimation.dispose();
        shopOverlay.dispose();
        cardDetail.dispose();
        jackpotCelebration.dispose();
        damageVignette.dispose();
        combatEnd.dispose();
        effectPopupAnimator.dispose();
    }
}
