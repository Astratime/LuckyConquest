package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.ScreenUtils;
import fr.astratime.lucky.controllers.ReelStall;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.animations.CardDealAnimator;
import fr.astratime.lucky.animations.cutscenes.BonusGameCutscene;
import fr.astratime.lucky.animations.cutscenes.SmallBonusGameCutscene;
import fr.astratime.lucky.animations.cutscenes.CroupierCutscene;
import fr.astratime.lucky.animations.cutscenes.Cutscene;
import fr.astratime.lucky.animations.cutscenes.Cutscenes;
import fr.astratime.lucky.animations.cutscenes.LastDrawCutscene;
import fr.astratime.lucky.animations.JackpotCelebration;
import fr.astratime.lucky.animations.TurnBanner;
import fr.astratime.lucky.animations.PistolShotAnimation;
import fr.astratime.lucky.entities.FullPotential;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.controllers.GameController;
import fr.astratime.lucky.entities.BigNumber;
import fr.astratime.lucky.entities.BonusGame;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.CardPlayResult;
import fr.astratime.lucky.entities.Combo;
import fr.astratime.lucky.entities.DrawResult;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.SpinEconomy;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolOutcome;
import fr.astratime.lucky.entities.SymbolRegistry;
import fr.astratime.lucky.entities.actions.Action;
import fr.astratime.lucky.entities.actions.AttackAction;
import fr.astratime.lucky.entities.actions.DefenseAction;
import fr.astratime.lucky.entities.actions.GainAction;
import fr.astratime.lucky.entities.actions.HealAction;
import fr.astratime.lucky.entities.actions.MixedAction;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.choices.BetChoice;
import fr.astratime.lucky.entities.choices.RiggedReelChoice;
import fr.astratime.lucky.entities.choices.JokerWorkshopChoice;
import fr.astratime.lucky.entities.choices.CalibrationChoice;
import fr.astratime.lucky.entities.choices.CardChoice;
import fr.astratime.lucky.entities.choices.RouletteChoice;
import fr.astratime.lucky.entities.SlotMachine;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemyLines;
import fr.astratime.lucky.entities.tower.TowerRun;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.events.BreachEvent;
import fr.astratime.lucky.entities.events.EnemyHpLostEvent;
import fr.astratime.lucky.entities.events.EnemyPhaseEvent;
import fr.astratime.lucky.entities.events.ComboEvent;
import fr.astratime.lucky.entities.events.DamageReflectedEvent;
import fr.astratime.lucky.entities.events.EnemyHealedEvent;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.EnemyShieldedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;
import fr.astratime.lucky.entities.events.GainsLostEvent;
import fr.astratime.lucky.entities.events.JackpotEvent;
import fr.astratime.lucky.entities.events.PlayerDamagedEvent;
import fr.astratime.lucky.entities.events.LastDrawEvent;
import fr.astratime.lucky.entities.events.ReelForbiddenEvent;
import fr.astratime.lucky.entities.events.PistolShotEvent;
import fr.astratime.lucky.entities.events.PlayerHealedEvent;
import fr.astratime.lucky.entities.events.ShieldGainedEvent;
import fr.astratime.lucky.entities.exploration.DungeonRun;
import fr.astratime.lucky.entities.run.CombatRun;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.progress.PlayerProfile;
import fr.astratime.lucky.progress.PlayerStats;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;
import fr.astratime.lucky.views.ChestOverlay;
import fr.astratime.lucky.views.CardImage;
import fr.astratime.lucky.views.CombatHud;
import fr.astratime.lucky.views.EnemyPickOverlay;
import fr.astratime.lucky.views.EnemyView;
import fr.astratime.lucky.views.GuideOverlay;
import fr.astratime.lucky.views.HandView;
import fr.astratime.lucky.views.PilesView;
import fr.astratime.lucky.views.PlayArea;
import fr.astratime.lucky.views.ShieldBadge;
import fr.astratime.lucky.views.SpinControls;
import fr.astratime.lucky.views.ShopIcon;
import fr.astratime.lucky.views.ShopOverlay;
import fr.astratime.lucky.views.SidePanel;
import fr.astratime.lucky.views.SlotView;
import fr.astratime.lucky.views.TableView;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * Écran de combat. Responsabilité : orchestrer l'affichage et transmettre les
 * actions du joueur au GameController — aucune logique métier ici.
 *
 * L'affichage est réparti entre des vues dédiées : {@link TableView} (la table
 * de casino et ses emplacements), {@link HandView} (la main), {@link PilesView}
 * (deck et défausse), {@link SlotView} (symboles tirés et textes du tirage),
 * {@link CombatHud} (barres de vie), {@link SidePanel} (panneau latéral gauche :
 * gains), {@link SpinControls} (levier et Mise), {@link ShopIcon} (icône de
 * l'échoppe). Les lignes du panneau des effets viennent de {@link CombatEffectRows},
 * le journal du tirage de {@link TurnLog}. Ces vues se placent dans la zone de jeu ({@link PlayArea}), à droite
 * du panneau ; la main, les symboles et les piles se posent aux emplacements de
 * la table. Cet écran possède les ressources partagées, les boutons et le
 * clavier, et enchaîne les phases du tour : annonce « À TOI DE JOUER ! » et
 * pioche automatique, cartes jouées, spin (qui termine le tour du joueur),
 * annonce « TOUR ENNEMI » et riposte, puis de nouveau le tour du joueur,
 * jusqu'à la fin du combat.
 *
 * Dans la Tour des épreuves ({@link fr.astratime.lucky.entities.tower.TowerRun}),
 * l'écran enchaîne les trois combats d'un chapitre : après une victoire, le
 * joueur choisit son adversaire parmi trois cartes faces cachées, puis affronte
 * le boss (voir {@link EnemyPickOverlay}) ; il garde ses PV, ses gains et ses
 * cartes d'un combat à l'autre. Une défaite fait recommencer le chapitre. Dans
 * un donjon de l'Exploration ({@link DungeonRun}), le soldat puis le roi, et
 * enfin le coffre au trésor ({@link ChestOverlay}).
 *
 * Le joueur joue son deck (voir {@link PlayerProfile}). À la fin de chaque
 * combat, les gains acquis pendant le combat sont versés en pièces.
 */
public class GameScreen extends GameScreenRun {

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
        this(luckyGame, null, EnemyKind.ENTRAINEMENT);
    }

    /**
     * Entraînement libre : un combat seul contre {@code enemy}, un ennemi déjà vaincu
     * choisi dans le bestiaire. Comme l'Entraînement, il ne rapporte rien.
     */
    public GameScreen(LuckyGame luckyGame, EnemyKind enemy) {
        this(luckyGame, null, enemy);
    }

    /**
     * Écran de jeu d'une ascension de la Tour des épreuves.
     *
     * @param run chapitre ou donjon en cours ({@code null} : un combat seul, comme « Entraînement »)
     */
    public GameScreen(LuckyGame luckyGame, CombatRun run) {
        this(luckyGame, run, EnemyKind.ENTRAINEMENT);
    }

    private GameScreen(LuckyGame luckyGame, CombatRun run, EnemyKind trainingEnemy) {
        super(luckyGame, run, trainingEnemy);
    }

    // -------------------------------------------------------------------------
    // Actions du joueur — transmises au GameController
    // -------------------------------------------------------------------------

    /**
     * Début du tour du joueur (début du combat, ou fin du tour ennemi) :
     * annonce « À TOI DE JOUER ! », puis pioche automatique de la main.
     */
    void startPlayerTurn() {
        playerMachine.lower();
        List<Card> onTable = gameController.getGameState().getEnemy().getRevealedHand();
        if (!onTable.isEmpty()) enemyView.showOnTable(onTable); // le Croupier joue cartes sur table
        setSpinDisabled(true);
        turnBanner.play(TurnBanner.Side.PLAYER, playArea.getCenterX(), turnBannerY(), playArea.getWidth());
        stage.addAction(Actions.delay(TurnBanner.DURATION, Actions.run(this::drawHand)));
    }

    /** Pioche une nouvelle main et lance son animation de distribution ; active le spin. */
    void drawHand() {
        if (isCombatOver()) return;
        refreshCombos(); // les combinaisons du tour précédent s'éteignent
        hand.setLocked(false); // une carte achetée entre deux tours reste sur la table
        showReelCount(gameController.getReelCount()); // le 4e rouleau de la surchauffe ne dure qu'un tour
        slots.setBlockedReels(blockedReels()); // Rouleau interdit, Rien ne va plus, rouleaux volés
        refreshAspiration();
        GameController.Purchase gift = gameController.claimBonusCard(); // Bingo glissé dans le deck après un Bingo de gains
        if (gift != null) placePurchase(gift);
        DrawResult drawn = gameController.drawCards(); // Grignotage, Scorbut, Aveuglement et Chant s'y appliquent
        hand.setHidden(gameController.getHiddenCards());
        hand.deal(drawn);
        List<EffectPopup> notices = gameController.getTurnNotices();
        if (!notices.isEmpty()) {
            effectPopupAnimator.play(notices, playArea.getCenterX(), table.getHandRowY() + CARD_HEIGHT * 1.3f);
            refreshEffects(); // les mauvais sorts de l'ennemi s'appliquent : ils quittent le panneau
        }
        refreshPlays();
        spinControls.dropChips(gameController.getStake()); // la Mise reste posée d'un tour à l'autre
        setSpinDisabled(false);
        if (tutorial != null) tutorial.beat(TutorialDirector.Beat.HAND_DEALT, () -> { });
        // Chant de l'ennemi : une fois la main distribuée, des cartes partent d'office.
        float dealt = drawn.getAddedToHand().size() * CardDealAnimator.DEAL_STAGGER_DELAY + SONG_DELAY;
        stage.addAction(Actions.delay(dealt, Actions.run(() -> {
            for (Card card : gameController.takeSongCards()) hand.forcePlay(card);
        })));
    }

    /** Fin du tour du joueur (ses résultats sont affichés) : annonce « TOUR ENNEMI » avant la riposte. */
    void announceEnemyTurn() {
        turnBanner.play(TurnBanner.Side.ENEMY, playArea.getCenterX(), turnBannerY(), playArea.getWidth());
    }

    /** @return la hauteur des annonces de tour : sur le filet, entre le côté du joueur et celui de l'ennemi. */
    float turnBannerY() {
        return table.getDividerY();
    }

    /**
     * Transmet la carte jouée (déjà retirée de la main affichée) au contrôleur,
     * affiche le texte de chacun de ses bonus à sa place, déclenche l'effet de
     * particules à l'endroit cliqué, puis distribue les cartes éventuellement
     * piochées par ses effets immédiats. Une carte qui demande un choix (Pari,
     * Roulette russe) ouvre sa fenêtre ; le Bingo bloque la main, s'élève dans
     * ses faisceaux lumineux, se téléporte sur la défausse, puis lance la machine.
     */
    void onCardPlayed(Card card, CardImage image, Vector2 cardCenter, Vector2 clickPos) {
        sounds.cardPlay.play();
        cardClickParticles.play(clickPos.x, clickPos.y);
        Gdx.app.log("GameScreen", "Carte jouee : " + card);

        List<Combo> combosBefore = gameController.getCurrentCombos();
        LastingEffects lasting = gameController.getGameState().getPlayer().getLastingEffects();
        FullPotential.Gauges gauges = FullPotential.Gauges.before(lasting);
        CardPlayResult playResult = gameController.playCard(card);
        if (!playResult.getPopups().isEmpty()) { // carte vraiment jouée
            FullPotential.of(card, gauges.withBladesAfter(lasting.getBlades())).ifPresent(this::playFullPotential);
        }
        recap.cardPlayed();
        journal.cardPlayed(card);
        if (tutorial != null) tutorial.onCardPlayed(card);
        int maps = gameController.takeTreasureMaps(); // Carte au trésor : le coffre du donjon grossit
        if (run instanceof DungeonRun dungeonRun) for (int i = 0; i < maps; i++) dungeonRun.addTreasureMap();
        if (playResult.isAutoSpin()) {
            playBingo(image, playResult.getPopups());
        } else {
            hand.slam(image);
            effectPopupAnimator.play(playResult.getPopups(), cardCenter.x, cardCenter.y);
        }
        hud.refresh();
        refreshGains(); // une carte peut créditer ou consommer des gains immédiatement
        refreshEffects();
        hand.refreshBlocked(); // limite de cartes atteinte, ou Bingo rendu injouable par un Recyclage
        refreshPlays();
        refreshAspiration(); // les conditions remplies s'allument
        showReelCount(gameController.getReelCount()); // Machine en surchauffe : un 4e rouleau surgit
        announceCombos(combosBefore);

        DrawResult drawResult = playResult.getDrawResult();
        if (!drawResult.getAddedToHand().isEmpty() || !drawResult.getDiscarded().isEmpty()) {
            hand.deal(drawResult);
        }
        askChoice(playResult.getChoice(), cardCenter);
        if (playResult.getRainbow() != null) playRainbow(playResult.getRainbow());
    }

    /** Un As joué à plein potentiel : son animation au milieu de l'écran, vers l'ennemi ou la machine. */
    void playFullPotential(FullPotential kind) {
        Vector2 center = new Vector2(stage.getWidth() / 2f, stage.getHeight() / 2f);
        Vector2 target = kind == FullPotential.CLUBS ? slots.getReelCenter(1) : enemyView.getCroupierCenter();
        fullPotentialAnimation.play(kind, center, target, settings.isReducedEffects());
    }

    /**
     * Met en avant les combinaisons que forment les cartes jouées ; le nom et le
     * multiplicateur de celles qui viennent d'être formées (absentes de
     * {@code before}) s'affichent au-dessus de la main, suivis du total s'il y en
     * a plusieurs.
     */
    void announceCombos(List<Combo> before) {
        List<Combo> combos = refreshCombos();
        List<EffectPopup> popups = new ArrayList<>();
        for (Combo combo : combos) {
            if (before.contains(combo)) continue;
            popups.add(new EffectPopup(combo.getDisplayName() + " x" + combo.formatFactor() + " !",
                EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY));
        }
        if (popups.isEmpty()) return;
        sounds.comboFormed.play();
        if (combos.size() > 1) {
            popups.add(new EffectPopup(Lang.f("TOTAL x{0}", Combo.formatFactor(Combo.totalFactor(combos))),
                EffectPopup.Style.GAINS, PopupScale.SECONDARY_INTENSITY));
        }
        effectPopupAnimator.play(popups, playArea.getCenterX(), table.getHandRowY() + CARD_HEIGHT * COMBO_TEXT_HEIGHT);
    }

    /**
     * Aide aux combinaisons (option Accessibilité) : au survol d'une carte jouable,
     * le panneau montre les combinaisons qu'on aurait en la jouant ; {@code null} efface l'aperçu.
     */
    void previewCombos(Card hovered) {
        if (hovered == null) {
            sidePanel.clearPreview();
            return;
        }
        if (!settings.isComboHint()) return;
        List<Card> played = new ArrayList<>(player().getPlayedCards());
        played.add(hovered);
        sidePanel.previewCombos(Combo.formed(played));
    }

    /** Fait briller dans le panneau les combinaisons des cartes jouées ce tour. @return ces combinaisons */
    List<Combo> refreshCombos() {
        List<Combo> combos = gameController.getCurrentCombos();
        sidePanel.setCombos(combos);
        return combos;
    }

    /**
     * Arc-en-ciel : les jetons traversent l'écran ; chaque carte qui change de
     * couleur le fait au passage de son jeton. Puis le Pot de Lutin apparaît sur
     * la table, ou file dans la défausse si la table est pleine.
     */
    void playRainbow(CardPlayResult.Rainbow rainbow) {
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
                effectPopupAnimator.play(List.of(new EffectPopup(Lang.t("POT DE LUTIN !"), EffectPopup.Style.GAINS,
                    PopupScale.MAX_INTENSITY)), at.x, at.y + CARD_HEIGHT * 0.8f);
                if (!recolored.isEmpty()) {
                    effectPopupAnimator.play(List.of(new EffectPopup(Lang.f("TOUT EN {0} !", suitName(rainbow.suit())),
                        EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY)),
                        playArea.getCenterX(), table.getHandRowY() + CARD_HEIGHT * 2f);
                }
                sounds.resultPair.play();
            });
    }

    /** @return le nom affiché de {@code suit}, en majuscules. */
    static String suitName(Card.Suit suit) {
        return switch (suit) {
            case COEUR   -> Lang.t("COEUR");
            case CARREAU -> Lang.t("CARREAU");
            case TREFLE  -> Lang.t("TRÈFLE");
            case PIQUE   -> Lang.t("PIQUE");
        };
    }

    /**
     * Ouvre l'échoppe pendant le tour du joueur (main piochée, machine pas encore
     * lancée), sauf pendant une animation ou une fenêtre, ou une fois le combat terminé.
     */
    void openShop() {
        if (isBusy() || isCombatOver() || spinControls.isDisabled()) return;
        sounds.buttonClick.play();
        shopOverlay.show(gameController.getShopOffers(), () -> player().getGains(), cardTextures::get,
            (text, action) -> buttons.create(text, sounds.buttonClick, action),
            gameController::unavailableReason,
            offer -> {
                GameController.Purchase purchase = gameController.buy(offer);
                if (purchase == null) return false;
                if (tutorial != null) tutorial.onPurchase();
                sounds.purchase.play();
                refreshGains();
                stage.addAction(Actions.delay(PURCHASE_DELAY, Actions.run(() -> placePurchase(purchase))));
                return true;
            },
            offer -> showCardDetail(offer.card()), reelStand());
        if (guide != null) { // l'échoppe passe devant tout : le Croupier reste visible au-dessus
            guide.toFront();
            tooltip.getActor().toFront();
        }
    }

    /** @return le côté rouleaux de l'échoppe : achats et retraits passent par le contrôleur, puis l'écran suit. */
    ShopOverlay.ReelStand reelStand() {
        return new ShopOverlay.ReelStand() {
            @Override public List<ReelStall.ReelOffer> offers() { return gameController.getReelOffers(); }
            @Override public List<Symbol> machine() { return player().getSlotMachine().getReels(); }
            @Override public double removePrice() { return gameController.getReelRemovePrice(); }
            @Override public String buyProblem(Symbol symbol) { return gameController.reelBuyProblem(symbol); }
            @Override public String removeProblem(Symbol symbol) { return gameController.reelRemoveProblem(symbol); }
            @Override public boolean buy(Symbol symbol) { return reelChanged(gameController.buyReel(symbol)); }
            @Override public boolean remove(Symbol symbol) { return reelChanged(gameController.removeReel(symbol)); }
            @Override public TextureRegion region(Symbol symbol) { return slots.regionOf(symbol); }
        };
    }

    /** Un rouleau acheté ou retiré à l'échoppe : la machine, les gains, la main et les effets suivent. @return {@code done} */
    boolean reelChanged(boolean done) {
        if (!done) return false;
        sounds.purchase.play();
        slots.setMachine(player().getSlotMachine().getReels());
        refreshGains();
        hand.refreshBlocked(); // un Bingo d'un rouleau retiré ne se joue plus
        refreshEffects();
        return true;
    }

    /** Affiche la fiche de {@code card} (avec son prix si elle est vendue à l'échoppe), par-dessus tout le reste. */
    void showCardDetail(Card card) {
        Double price = gameController.getShopOffers().stream()
            .filter(offer -> offer.card().getId().equals(card.getId()))
            .map(GameController.ShopOffer::price).findFirst().orElse(null);
        sounds.cardInspect.play();
        cardDetail.show(card, cardTextures.get(card), price);
        tooltip.getActor().toFront();
    }

    /** Carte achetée : elle apparaît sur la table s'il y a de la place, sinon elle file dans le deck. */
    void placePurchase(GameController.Purchase purchase) {
        Card card = purchase.card();
        Vector2 at;
        if (purchase.addedToHand()) {
            at = hand.conjure(card);
        } else {
            at = dropOnPile(card, piles.deck().getTopX(), piles.deck().getTopY(), () -> piles.refresh(player()));
        }
        confetti.burst(at.x, at.y, POT_CONFETTI);
        effectPopupAnimator.play(List.of(new EffectPopup(card.getName().toUpperCase() + " !", EffectPopup.Style.SPECIAL,
            PopupScale.MAX_INTENSITY)), at.x, at.y + CARD_HEIGHT * 0.8f);
    }

    /** Table pleine : le Pot de Lutin apparaît au-dessus de la défausse et s'y pose. @return son centre */
    Vector2 potToDiscard(Card pot) {
        return dropOnPile(pot, piles.discard().getTopX(), piles.discard().getTopY(), () -> {
            piles.onCardLanded();
            piles.refresh(player());
        });
    }

    /**
     * {@code card} surgit au-dessus de la pile posée en ({@code x}, {@code y}),
     * puis s'y pose ; {@code landed} quand elle y arrive. @return le point au-dessus de la pile
     */
    Vector2 dropOnPile(Card card, float x, float y, Runnable landed) {
        CardImage image = new CardImage(new TextureRegionDrawable(new TextureRegion(cardTextures.get(card))));
        image.setSize(CARD_WIDTH, CARD_HEIGHT);
        image.setTouchable(Touchable.disabled);
        image.setOrigin(CARD_WIDTH / 2f, CARD_HEIGHT / 2f);
        image.setPosition(x, y + CARD_HEIGHT * 0.6f);
        image.setScale(0f);
        stage.addActor(image);
        image.addAction(Actions.sequence(
            Actions.scaleTo(1f, 1f, 0.35f, Interpolation.swingOut),
            Actions.delay(0.4f),
            Actions.moveTo(x, y, 0.3f, Interpolation.pow2In),
            Actions.run(landed),
            Actions.removeActor()));
        return new Vector2(x + CARD_WIDTH / 2f, y + CARD_HEIGHT * 1.1f);
    }

    /** Ouvre la fenêtre du choix demandé par la carte jouée (rien si elle n'en demande pas). */
    void askChoice(CardChoice choice, Vector2 cardCenter) {
        List<Symbol> options = gameController.getBetOptions();
        switch (choice) {
            case null -> { }
            case BetChoice bet -> choiceOverlay.showBet(options, slots::regionOf, picked(gameController::placeBet, cardCenter));
            case RiggedReelChoice rigged ->
                choiceOverlay.showRiggedReel(options, slots::regionOf, picked(gameController::rigReel, cardCenter));
            case JokerWorkshopChoice workshop ->
                choiceOverlay.showJokerWorkshop(options, slots::regionOf, picked(gameController::shapeJoker, cardCenter));
            case CalibrationChoice calibration -> choiceOverlay.showCalibration(options, calibration.turns(), slots::regionOf,
                picked(gameController::calibrate, cardCenter));
            case RouletteChoice roulette -> choiceOverlay.showRoulette(roulette.cursed(), roulette.pistolPercent(),
                roulette.cursedPercent(), roulette.penaltyPercent(),
                index -> {
                    GameController.RouletteOutcome outcome = gameController.pickRouletteCard(index);
                    effectPopupAnimator.play(outcome.popups(), cardCenter.x, cardCenter.y);
                    if (outcome.cursed()) {
                        sounds.coinsLoss.play();
                        damageVignette.flash(settings.isReducedEffects() ? 0.3f : 0.6f);
                        screenShake.shake(0.3f, 9f);
                    }
                    refreshGains();
                });
        }
    }

    /** @return ce qui suit le choix d'un symbole : {@code pick} l'applique, ses textes surgissent de la carte jouée. */
    Consumer<Symbol> picked(Function<Symbol, List<EffectPopup>> pick, Vector2 cardCenter) {
        return symbol -> {
            effectPopupAnimator.play(pick.apply(symbol), cardCenter.x, cardCenter.y);
            refreshEffects();
        };
    }

    /**
     * Le joueur clique sur une carte qui ne peut pas être jouée (limite de
     * cartes du tour atteinte, ou Bingo d'un symbole recyclé) : elle reste dans
     * la main, son infobulle donne la raison (voir {@link HandView}) et un texte
     * « LIMITE ATTEINTE ! » ou « BINGO BLOQUÉ ! » surgit sur la carte.
     */
    void onCardRefused(Card card, String reason, Vector2 cardCenter) {
        Gdx.app.log("GameScreen", "Carte refusee : " + card + " (" + reason + ")");
        String text = tutorial != null && tutorial.refusal(card) != null ? Lang.t("PAS CELLE-LÀ !")
            : gameController.isPlayLimitReached() ? Lang.t("LIMITE ATTEINTE !") : Lang.t("BINGO BLOQUÉ !");
        effectPopupAnimator.play(List.of(new EffectPopup(text, EffectPopup.Style.DAMAGE,
            PopupScale.SECONDARY_INTENSITY)), cardCenter.x, cardCenter.y - CARD_HEIGHT * 0.3f); // monte sur la carte, sous l'infobulle
    }

    /**
     * Bingo : plus aucune carte ne peut être jouée ; la carte s'élève au-dessus
     * de la table dans ses faisceaux, se téléporte sur la défausse, puis la
     * machine se lance d'elle-même.
     */
    void playBingo(CardImage image, List<EffectPopup> popups) {
        hand.setLocked(true);
        setSpinDisabled(true);
        float centerX = playArea.getCenterX();
        float centerY = stage.getViewport().getWorldHeight() * BINGO_CARD_HEIGHT;
        effectPopupAnimator.play(popups, centerX, centerY + CARD_HEIGHT * 1.3f, 0.5f); // avec le son de la carte
        bingoAnimation.play(image, centerX, centerY, piles.discard().getTopX(), piles.discard().getTopY(),
            this::spin);
    }

    /**
     * Lance la machine à sous, envoie les cartes restées sur la table à la
     * défausse (animation), affiche les symboles et les textes du tirage, met à
     * jour PV et score, puis termine le combat si l'un des deux camps est vaincu,
     * ou repasse la main à la phase de pioche sinon.
     */
    void onSpin() {
        if (spinControls.isDisabled() || isBusy()) return;
        if (tutorial != null && !tutorial.allowsSpin()) return; // le Croupier n'a pas encore demandé de lancer
        spin();
    }

    /** @return la fête du Bingo, créée à la première demande (premier lancer du combat). */
    JackpotCelebration jackpotCelebration() {
        if (jackpotCelebration == null) {
            jackpotCelebration = new JackpotCelebration(playArea, screenShake, settings, hudTextures,
                sidePanel::getCoinCenter, sidePanel::bumpCoin, jackpotFireworkSounds);
            jackpotLayer.addActor(jackpotCelebration);
        }
        return jackpotCelebration;
    }

    /** Active ou désactive le lancer, et la Mise avec lui (elle se choisit avant le lancer). */
    void setSpinDisabled(boolean disabled) {
        spinControls.setDisabled(disabled);
    }

    /**
     * Bouton de Mise : passe au palier suivant (aucune, 5 %, 10 %, 15 % des
     * gains), et un jeton de casino tombe sur la table. On ne mise que ce qu'on a.
     */
    void onStake() {
        if (spinControls.isStakeDisabled() || isBusy()) return;
        if (tutorial != null && !tutorial.allowsStake()) return;
        SpinEconomy.Stake before = gameController.getStake();
        SpinEconomy.Stake stake = gameController.nextStake();
        if (stake == SpinEconomy.Stake.NONE && before == SpinEconomy.Stake.NONE) { // pas de gains : rien à miser
            Vector2 above = spinControls.aboveStake();
            String why = player().getPotWinnings() > 0 ? Lang.t("RIEN DE MISABLE")
                                                       : Lang.t("PAS DE GAINS À MISER");
            effectPopupAnimator.play(List.of(new EffectPopup(why, EffectPopup.Style.DAMAGE,
                PopupScale.SECONDARY_INTENSITY)), above.x, above.y);
            return;
        }
        spinControls.dropChips(stake);
        if (stake != SpinEconomy.Stake.NONE) sounds.cardDeal.play();
        refreshPlays();
        if (tutorial != null) tutorial.onStake(stake);
    }

    /** @return {@code true} si une fenêtre ou une animation attend : ni pioche ni lancer possibles. */
    boolean isBusy() {
        return pileOverlay.isShown() || choiceOverlay.isShown() || shopOverlay.isShown() || cardDetail.isShown()
            || bingoAnimation.isPlaying() || rainbowAnimation.isPlaying() || pickOverlay.isShown() || chestOverlay.isShown();
    }

    /** Lance la machine (bouton, touche F, ou d'elle-même après une carte Bingo). */
    void spin() {
        jackpotCelebration(); // prête avant que les rouleaux ne puissent aligner un Bingo
        long enemyHpBefore = gameController.getGameState().getEnemy().getHp();
        double cost = gameController.getSpinPrice();
        double staked = gameController.getStakeAmount();
        TurnResult result = gameController.spin();
        if (tutorial != null) tutorial.onSpin();
        lastSpinBingo = result.isJackpot();
        table.setReelsRainbow(false); // la bordure d'un jackpot précédent s'arrête au lancer suivant
        // Gains et PV du tirage ne se montrent qu'à l'apparition de leurs textes, après l'arrêt des rouleaux.
        gainsNotYetShown += result.getEvents().stream().filter(e -> e instanceof GainsEarnedEvent)
            .mapToDouble(e -> ((GainsEarnedEvent) e).amount).sum()
            - result.getEvents().stream().filter(e -> e instanceof GainsLostEvent)
            .mapToDouble(e -> ((GainsLostEvent) e).amount).sum();
        if (result.getBonusGame() != null) gainsNotYetShown += result.getBonusGame().getTotal(); // à la fin du Jeu bonus
        payForSpin(cost, staked); // seuls le coût et la Mise partent tout de suite
        long enemyHeal = result.getEvents().stream().filter(EnemyHealedEvent.class::isInstance)
            .mapToLong(e -> ((EnemyHealedEvent) e).amount).sum();
        hud.holdBack(enemyHpBefore - gameController.getGameState().getEnemy().getHp() + enemyHeal, enemyHeal,
            sumOf(result, PlayerDamagedEvent.class, hit -> hit.damage),
            sumOf(result, PlayerHealedEvent.class, heal -> heal.amount));
        hand.discardAll();
        setSpinDisabled(true); // le tour du joueur se termine : la suite attend l'arrêt des rouleaux
        showReelCount(result.getDrawnSymbols().length);
        // Aspiration : le panneau s'efface ; un rouleau aspiré (vide au tirage) est barré comme un rouleau bloqué
        aspirationPanel.update(List.of(), List.of());
        slots.setThreatenedReels(Set.of());
        Set<Integer> empty = new TreeSet<>();
        for (int i = 0; i < result.getDrawnSymbols().length; i++) if (result.getDrawnSymbols()[i] == null) empty.add(i);
        slots.setBlockedReels(empty);
        Symbol[] rerolled = result.getRerolledDraw();
        playerMachine.raise(slots.getActor()); // vue de face : la machine sort de la table
        if (rerolled == null) {
            slots.spin(result.getDrawnSymbols(), result.getSymbols(), this::onJokerTransformed,
                () -> onReelsStopped(result));
        } else { // Relance : le premier tirage s'arrête sans paire, puis la machine repart
            slots.spin(rerolled, rerolled, reel -> { }, () -> stage.addAction(Actions.delay(REROLL_PAUSE,
                Actions.run(() -> {
                    playReroll();
                    slots.spin(result.getDrawnSymbols(), result.getSymbols(), this::onJokerTransformed,
                        () -> onReelsStopped(result));
                }))));
        }
        refreshEffects(); // les symboles retirés se rapprochent de leur retour
        refreshPlays();   // nouveau tour : le compteur de cartes repart à zéro

        TurnLog.log(result);
    }

    /** Le tirage est payé : le coût (et la Mise, versée à la cagnotte) quittent les gains, les jetons glissent dans la machine. */
    void payForSpin(double cost, double staked) {
        spinControls.pull(); // le levier reste tiré, les jetons misés glissent dans la machine
        if (cost <= 0) return;
        List<EffectPopup> popups = new ArrayList<>();
        popups.add(new EffectPopup(Lang.f("TIRAGE -{0}", SidePanel.formatGains(cost)), EffectPopup.Style.DAMAGE,
            PopupScale.SECONDARY_INTENSITY));
        if (staked > 0) popups.add(new EffectPopup(Lang.f("MISE -{0} : CAGNOTTE", SidePanel.formatGains(staked)),
            EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY));
        Vector2 coin = sidePanel.getCoinCenter();
        effectPopupAnimator.play(popups, coin.x, coin.y);
        sounds.coinsLoss.play();
        refreshGains();
        refreshEffects(); // dette : Endetté ou Huissier
    }

    /**
     * Les rouleaux sont arrêtés : textes du tirage et bruitage du résultat ; une
     * fois les résultats du joueur affichés (après la célébration d'un jackpot,
     * voir {@link #onJackpotShown}), vient le tour de l'ennemi.
     */
    void onReelsStopped(TurnResult result) {
        currentResult = result;
        recordTurn(result);
        Vector2 enemyBar    = hud.getEnemyBarCenter();
        Vector2 pistolTexts = new Vector2(enemyBar.x, enemyBar.y - PISTOL_TEXT_BELOW);
        float shotAt = slots.playResultPopups(result, pistolTexts, this::onEventShown, this::playSymbolStrike);
        if (shotAt >= 0f) aimPistol(result, enemyBar, shotAt);
        refreshEffects(); // jauges vidées ou remplies par le tirage
        playSymbolResultSound(result);
        if (result.isPair()) celebratePair(result.getSymbols());
        if (result.isJackpot()) return; // voir onJackpotShown
        stage.addAction(Actions.delay(SlotView.popupsDuration(result), Actions.run(this::afterPlayerSpin)));
    }

    /**
     * Compte le tirage dans les statistiques (plus gros coup, gains, Bingo,
     * Carré, Jeu bonus ; pas dans le tutoriel) et annonce les succès atteints.
     */
    void recordTurn(TurnResult result) {
        recap.record(result);
        if (tutorial != null) return;
        Set<Event> events = Collections.newSetFromMap(new IdentityHashMap<>());
        events.addAll(result.getEvents());
        events.addAll(result.getCardEvents());
        long bestHit = 0L;
        long gains   = 0L;
        boolean square = false;
        Symbol bingo = null;
        for (Event event : events) {
            if (event instanceof EnemyDamagedEvent hit) bestHit = Math.max(bestHit, hit.damage);
            if (event instanceof GainsEarnedEvent earned) gains += Math.max(0, earned.amount);
            if (event instanceof ComboEvent combo && combo.combo == Combo.CARRE) square = true;
            if (event instanceof JackpotEvent jackpot) bingo = jackpot.symbol;
        }
        BonusGame bonus = result.getBonusGame();
        long best = bestHit, earned = BigNumber.toLong(gains + (bonus != null ? Math.max(0, bonus.getTotal()) : 0));
        boolean squared = square;
        Symbol bingoSymbol = bingo;
        luckyGame.getProfile().recordStats(stats -> {
            stats.record(PlayerStats.Stat.BEST_HIT, best);
            stats.add(PlayerStats.Stat.GAINS, earned);
            if (squared) stats.add(PlayerStats.Stat.SQUARES, 1);
            if (bingoSymbol != null) stats.addBingo(bingoSymbol);
            if (bonus != null) stats.add(PlayerStats.Stat.BONUS_GAMES, 1);
            stats.add(PlayerStats.Stat.SPINS, 1);
        });
        announceAchievements();
    }

    /** Annonce les succès qui viennent d'être atteints (aucun dans le tutoriel ni en mode ADMIN). */
    void announceAchievements() {
        if (tutorial != null) return;
        achievementToast.show(luckyGame.getProfile().checkAchievements());
    }

    /** Les résultats du tirage sont affichés : le Croupier du tutoriel les commente, puis le tour de l'ennemi. */
    void afterPlayerSpin() {
        if (tutorial != null) tutorial.beat(TutorialDirector.Beat.SPIN_RESOLVED, this::playEnemyTurn);
        else playEnemyTurn();
    }

    /**
     * Tour de l'ennemi (s'il a joué) : « TOUR ENNEMI » est annoncé, puis
     * l'ennemi pioche, joue ses cartes et lance sa machine (voir
     * {@link EnemyView#playTurn}) ; ensuite, la fin du tour.
     */
    void playEnemyTurn() {
        playerMachine.lower(); // le tirage du joueur est fini : sa machine redescend sous la table
        EnemyTurnResult enemyTurn = currentResult.getEnemyTurn();
        if (enemyTurn == null) {
            endTurn();
            return;
        }
        // Minuit (Manoir hanté) : l'ennemi rejoue aussitôt un second tour.
        EnemyTurnResult midnight = currentResult.getMidnightTurn();
        playEnemyTurn(enemyTurn, midnight == null ? this::endTurn : () -> playEnemyTurn(midnight, this::endTurn));
    }

    /** Un tour de l'ennemi {@code enemyTurn} (sa cinématique de phase d'abord, s'il en change), puis {@code then}. */
    void playEnemyTurn(EnemyTurnResult enemyTurn, Runnable then) {
        Runnable play = () -> {
            announceEnemyTurn();
            stage.addAction(Actions.delay(TurnBanner.DURATION,
                Actions.run(() -> enemyView.playTurn(enemyTurn, this::onEventShown, then))));
        };
        // Un boss ou un roi change de phase : sa cinématique de transition joue d'abord.
        int phase = enemyTurn.openingEvents().stream().filter(event -> event instanceof EnemyPhaseEvent)
            .mapToInt(event -> ((EnemyPhaseEvent) event).phase).max().orElse(0);
        Cutscene transition = phase >= 2 && tutorial == null
            ? Cutscenes.phase(cutsceneKit, gameController.getGameState().getEnemy().getKind(), phase) : null;
        if (transition instanceof CroupierCutscene croupierPhase) { // ce qu'il a vraiment truqué, ou sa vraie main
            LastingEffects lasting = player().getLastingEffects();
            croupierPhase.withMachine(player().getSlotMachine().getReels(), lasting.getRiggedReel(), lasting.getRiggedSymbol());
            List<String> shown = new ArrayList<>();
            for (Card card : gameController.getGameState().getEnemy().getRevealedHand()) shown.add(card.getAssetPath());
            croupierPhase.withCards(shown, true);
        }
        playCutscene(transition, play);
    }

    /**
     * Fin du tour : ce qui reste du bouclier du joueur remplit son Coffre (texte
     * à droite de sa barre de vie), puis le combat se termine si un camp est
     * vaincu, ou le joueur reprend la main. Le Dernier tirage de la Machine
     * Originelle se joue en cinématique (le duel des leviers) ; ce qui le suit
     * s'affiche sous son fondu final.
     */
    void endTurn() {
        playerShield.restore(0); // ce qui en reste part au Coffre
        List<Event> endEvents = currentResult.getEnemyTurnEvents();
        int duel = 0;
        while (duel < endEvents.size() && !(endEvents.get(duel) instanceof LastDrawEvent)) duel++;
        if (duel == endEvents.size()) {
            showEndEvents(endEvents);
            return;
        }
        LastDrawEvent draw = (LastDrawEvent) endEvents.get(duel);
        List<Event> after = endEvents.subList(duel, endEvents.size());
        boolean before = showEndTexts(endEvents.subList(0, duel)); // « ELLE RÉSISTE ! DERNIER TIRAGE »
        stage.addAction(Actions.delay(before ? LAST_DRAW_DELAY : 0f, Actions.run(() ->
            playCutscene(new LastDrawCutscene(cutsceneKit, draw), () -> showEndEvents(after)))));
    }

    /** Affiche les événements de fin de tour {@code events}, puis termine le tour. */
    void showEndEvents(List<Event> events) {
        float margin = showEndTexts(events) ? RIPOSTE_TEXT_TIME : RESULT_TEXTS_MARGIN;
        stage.addAction(Actions.delay(margin, Actions.run(this::finishTurn)));
    }

    /**
     * @return {@code true} si les événements {@code events} ont des textes, affichés au milieu de la table
     *         (Garde, Coup de grisou, Venin… : bien lisibles, au-dessus du filet où passera « À TOI DE JOUER »)
     */
    boolean showEndTexts(List<Event> events) {
        List<EffectPopup> texts = new ArrayList<>();
        events.forEach(event -> texts.addAll(event.getPopups()));
        if (!texts.isEmpty()) {
            effectPopupAnimator.playCentered(texts, playArea.getCenterX(), turnBannerY() + CARD_HEIGHT * END_TEXTS_ABOVE);
        }
        events.forEach(this::onEventShown);
        refreshEffects();
        return !texts.isEmpty();
    }

    /**
     * Animation du symbole tiré : vers l'ennemi pour une attaque, vers le
     * bouclier du joueur pour une défense, vers la pièce des gains pour un
     * gain. Rien si le symbole n'a rien fait (ennemi déjà vaincu…).
     */
    void playSymbolStrike(SymbolOutcome outcome, Vector2 reel, float delay) {
        if (outcome.getEvents().isEmpty()) return;
        Action action = SymbolRegistry.getAction(outcome.getSymbol()).orElse(null);
        Vector2 target;
        Vector2 shield = playerShield.localToStageCoordinates(new Vector2(ShieldBadge.ICON_SIZE / 2f, ShieldBadge.ICON_SIZE / 2f));
        switch (action) {
            case null -> { return; }
            case MixedAction mixed -> { // l'Étoile se divise : ennemi, bouclier et gains
                symbolStrikes.play(outcome.getSymbol(), reel, enemyView.getCroupierCenter(), delay, shield,
                    sidePanel.getCoinCenter());
                return;
            }
            case AttackAction attack  -> target = enemyView.getCroupierCenter();
            case DefenseAction defense -> target = shield;
            case HealAction heal       -> target = hud.getPlayerChipCenter();
            case GainAction gain       -> target = sidePanel.getCoinCenter();
        }
        symbolStrikes.play(outcome.getSymbol(), reel, target, delay);
    }

    /** Le pistolet surgit au-dessus du symbole qu'il rejoue et tire sur l'ennemi à l'instant {@code shotAt}. */
    void aimPistol(TurnResult result, Vector2 target, float shotAt) {
        PistolShotEvent shot = result.getPistolEvents().stream()
            .filter(e -> e instanceof PistolShotEvent).map(e -> (PistolShotEvent) e).findFirst().orElse(null);
        if (shot == null || shot.blank) return; // contre-attaque du Coffre seule, ou tir à blanc
        Vector2 from = slots.getReelCenter(shot.slotIndex >= 0 ? shot.slotIndex : 1);
        stage.addAction(Actions.delay(Math.max(0f, shotAt - PistolShotAnimation.AIM_TIME),
            Actions.run(() -> pistolAnimation.play(from, target))));
    }

    /** Affiche {@code count} rouleaux (4 avec la Machine en surchauffe) et replace le bouclier à leur droite. */
    void showReelCount(int count) {
        if (count == slots.getReelCount()) return;
        boolean grows = count > slots.getReelCount();
        slots.setReelCount(count);
        placePlayerShield();
        if (grows) { // la machine chauffe : elle tremble et lâche des étincelles
            screenShake.shake(0.3f, 6f);
            Vector2 center = slots.getReelCenter(count - 1);
            confetti.burst(center.x, center.y, JOKER_CONFETTI);
        }
    }

    /** Relance : « RELANCE ! » au-dessus des rouleaux, levier et étincelles. */
    void playReroll() {
        Vector2 center = slots.getReelCenter(1);
        effectPopupAnimator.play(List.of(new EffectPopup(Lang.t("RELANCE !"), EffectPopup.Style.SPECIAL,
            PopupScale.MAX_INTENSITY)), center.x, center.y + SlotView.CELL_HEIGHT);
        confetti.burst(center.x, center.y, JOKER_CONFETTI);
    }

    /** Un Joker se transforme : texte « JOKER ! » et confettis sur son rouleau. */
    void onJokerTransformed(int reel) {
        Vector2 center = slots.getReelCenter(reel);
        effectPopupAnimator.play(List.of(new EffectPopup(Lang.t("JOKER !"), EffectPopup.Style.SPECIAL,
            PopupScale.SECONDARY_INTENSITY)), center.x, center.y + SlotView.CELL_HEIGHT / 2f);
        confetti.burst(center.x, center.y, JOKER_CONFETTI);
        sounds.resultPair.play();
    }

    /** Paire : les deux rouleaux identiques clignotent en doré et lâchent une gerbe de confettis. */
    void celebratePair(Symbol[] symbols) {
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

    /** Termine le tour de l'ennemi : fin du combat si un camp est vaincu, sinon au tour du joueur. */
    void finishTurn() {
        journal.record(currentResult, gameController.getGameState().getEnemy().getName());
        refreshJournal();
        Player hurt = player();
        if (!lowHpLineSaid && !isCombatOver() && EnemyLines.isLow(hurt.getHp(), hurt.getMaxHp())
                && EnemyLines.speaks(enemyKind())) {
            lowHpLineSaid = true; // une fois par combat
            enemySays(EnemyLines.lowHp(enemyKind()));
        }
        if (isCombatOver()) {
            endCombat();
        } else if (tutorial != null) {
            tutorial.beat(TutorialDirector.Beat.ENEMY_TURN_DONE, this::startPlayerTurn);
        } else {
            startPlayerTurn();
        }
    }

    /** Le texte d'un événement du tirage vient d'apparaître : met à jour ce qui en dépend. */
    void onEventShown(Event event) {
        switch (event) {
            case EnemyPhaseEvent phase -> {
                switchMusic(); // la Comète et la Machine Originelle changent de musique à leur phase 2
                enemySays(EnemyLines.phase(enemyKind(), phase.phase));
            }
            case GainsEarnedEvent gains -> onGainsShown(gains.amount);
            case GainsLostEvent lost -> onGainsShown(-lost.amount);
            case JackpotEvent jackpot -> onJackpotShown(jackpot.symbol);
            case EnemyDamagedEvent hit -> {
                onEnemyDefense(enemyView.onPlayerAttack(hit));
                if (hit.damage > 0) onEnemyHit(hit.damage);
            }
            case DamageReflectedEvent reflect when reflect.damage > 0 -> onEnemyHit(reflect.damage);
            case EnemyHpLostEvent self when self.lost > 0 -> onEnemyHit(self.lost); // Faillite, Duel gagné : sa barre descend aussi
            case BreachEvent breach when breach.damage > 0 -> onEnemyHit(breach.damage); // Brèche : sa barre descend après sa phase
            case PlayerDamagedEvent hit -> {
                if (hit.blocked > 0) onPlayerBlocked(hit);
                if (hit.damage > 0) onPlayerHit(hit.damage);
            }
            case ShieldGainedEvent shield when shield.amount > 0 -> {
                playerShield.add(shield.amount);
                sounds.shieldGain.play();
            }
            case EnemyShieldedEvent shield when shield.defense > 0 ->
                sounds.shieldGain.play(0.8f, ENEMY_SHIELD_PITCH, 0f); // un ton plus bas : c'est le bouclier de l'ennemi
            case PlayerHealedEvent heal when heal.amount > 0 -> hud.revealPlayerHeal(heal.amount);
            case EnemyHealedEvent heal -> {
                if (heal.amount > 0) hud.revealEnemyHeal(heal.amount);
                enemyView.heal();
            }
            case ReelForbiddenEvent forbidden -> {
                slots.setBlockedReels(blockedReels());
                sounds.shieldBreak.play();
                screenShake.shake(0.25f, 7f);
            }
            default -> { }
        }
    }

    /**
     * @return les rouleaux du joueur barrés au prochain tirage : {@code forbidden}
     *         (Rouleau interdit, -1 : aucun) et ceux volés par la Machine Originelle
     */
    Set<Integer> blockedReels() {
        Set<Integer> blocked = new TreeSet<>(player().getLastingEffects().getForbiddenReels());
        int stolen = gameController.getGameState().getEnemy().getStolenReels();
        if (stolen >= 1) blocked.add(SlotMachine.SYMBOL_COUNT - 1);
        if (stolen >= 2) blocked.add(0);
        return blocked;
    }

    /** L'ennemi encaisse un coup : sa barre réagit ; un gros coup fige l'image un instant et secoue l'écran. */
    void onEnemyHit(long damage) {
        hud.revealEnemyHit(damage);
        enemyView.hit();
        sounds.enemyHurt.play(1f, damage >= BIG_HIT ? BIG_HIT_PITCH : 1f, 0f);
        if (damage >= BIG_HIT) {
            hitStop(BIG_HIT_STOP);
            screenShake.shake(0.2f, 6f);
        }
    }

    /**
     * La défense de l'ennemi réagit à un coup : « clang » d'un coup absorbé,
     * fracas d'une défense brisée ou percée (un ton plus bas : c'est l'ennemi).
     */
    void onEnemyDefense(EnemyView.DefenseReaction reaction) {
        switch (reaction) {
            case BLOCKED -> sounds.shieldBlock.play(0.9f, ENEMY_SHIELD_PITCH, 0f);
            case BROKEN  -> shieldBroken(Lang.t("DÉFENSE BRISÉE !"), enemyView.getDefenseCenter(), ENEMY_SHIELD_PITCH);
            case PIERCED -> shieldBroken(Lang.t("DÉFENSE PERCÉE !"), enemyView.getDefenseCenter(), ENEMY_SHIELD_PITCH);
            case NONE    -> { }
        }
    }

    /** Le bouclier du joueur absorbe une attaque de l'ennemi : il encaisse, s'use, et se brise s'il n'en reste rien. */
    void onPlayerBlocked(PlayerDamagedEvent hit) {
        if (playerShield.block(hit.shieldLeft)) {
            shieldBroken(Lang.t("BOUCLIER BRISÉ !"), playerShield.localToStageCoordinates(new Vector2(ShieldBadge.ICON_SIZE / 2f, ShieldBadge.ICON_SIZE / 2f)), 1f);
        } else {
            sounds.shieldBlock.play();
        }
    }

    /** Un bouclier se brise : fracas, petite secousse et texte {@code text} au-dessus de lui ({@code at}). */
    void shieldBroken(String text, Vector2 at, float pitch) {
        sounds.shieldBreak.play(1f, pitch, 0f);
        screenShake.shake(0.15f, 4f);
        effectPopupAnimator.play(List.of(new EffectPopup(text, EffectPopup.Style.DAMAGE, PopupScale.SECONDARY_INTENSITY * 0.8f)),
            at.x, at.y + SHIELD_TEXT_ABOVE);
    }

    /** Le joueur est touché : sa barre réagit, un voile rouge passe sur les bords et l'écran tremble. */
    void onPlayerHit(int damage) {
        hud.revealPlayerHit(damage);
        sounds.playerHurt.play(1f, damage >= BIG_HIT ? BIG_HIT_PITCH : 1f, 0f);
        damageVignette.flash(settings.isReducedEffects() ? 0.3f : 0.6f);
        screenShake.shake(0.3f, 9f);
    }

    /** @return la somme de {@code amount} sur les événements du tirage de type {@code type}. */
    static <E extends Event> int sumOf(TurnResult result, Class<E> type, ToIntFunction<E> amount) {
        return result.getEvents().stream().filter(type::isInstance).map(type::cast).mapToInt(amount).sum();
    }

    /**
     * Le texte « JACKPOT ! » vient d'apparaître : bruitage du bingo, bordure
     * arc-en-ciel des rouleaux et célébration propre au {@code symbol} aligné.
     * Le tour de l'ennemi attend la fin de la célébration.
     */
    void onJackpotShown(Symbol symbol) {
        Sound scene   = sounds.bingo(symbol);
        long  sceneId = scene.play(BINGO_SCENE_VOLUME); // accordé à la mise en scène du symbole
        table.setReelsRainbow(true);
        table.setLightsParty(true);
        PlayerProfile profile = luckyGame.getProfile();
        // Le son de Bingo d'origine du jeu retentit quand « BINGO! » apparaît. Déjà vue, la célébration se passe d'un clic.
        JackpotCelebration jackpotCelebration = jackpotCelebration();
        jackpotCelebration.play(symbol, profile.hasSeenBingo(symbol), () -> sounds.bingoClassic.play(), () -> {
            if (jackpotCelebration.wasSkipped()) scene.stop(sceneId);
            profile.markBingoSeen(symbol);
            table.setLightsParty(false);
            if (!gameController.getGameState().getEnemy().isDefeated()) {
                enemySays(EnemyLines.bingo(enemyKind(), bingoLines++)); // le roi ou le boss réagit au Bingo
            }
            // Après la célébration (et le Jeu bonus, s'il s'ouvre) et les derniers textes du tirage, au tour de l'ennemi.
            playBonusGame(() -> stage.addAction(Actions.delay(SlotView.RIPOSTE_AFTER_BONUS,
                Actions.run(this::afterPlayerSpin))));
        });
    }

    /**
     * Le Jeu bonus du tirage, s'il s'est ouvert : sa scène joue par-dessus le
     * combat (la première fois, le Croupier l'explique sur la grille ouverte),
     * puis ses gains rejoignent le compteur, et {@code then}. Sans Jeu bonus,
     * {@code then} tout de suite.
     */
    void playBonusGame(Runnable then) {
        BonusGame bonus = currentResult.getBonusGame();
        if (bonus == null) {
            then.run();
            return;
        }
        if (bonus.getKind() != BonusGame.Kind.GRID) { // Roue, Plinko, Coup de poing
            playCutscene(new SmallBonusGameCutscene(cutsceneKit, bonus), () -> {
                onGainsShown(bonus.getTotal());
                then.run();
            });
            return;
        }
        BonusGameCutscene scene = new BonusGameCutscene(cutsceneKit, bonus, player().getSlotMachine().getReels());
        PlayerProfile profile = luckyGame.getProfile();
        if (!profile.hasSeen(PlayerProfile.GUIDE_BONUS)) scene.pauseOnGrid(() -> explainBonusGame(scene));
        playCutscene(scene, () -> {
            onGainsShown(bonus.getTotal());
            then.run();
        });
    }

    /** Première fois : le Croupier explique le Jeu bonus sur la grille ouverte, puis les tirages commencent. */
    void explainBonusGame(BonusGameCutscene scene) {
        PlayerProfile profile = luckyGame.getProfile();
        if (bonusGuide == null) {
            bonusGuide = new GuideOverlay(hudTextures, enemyTextures.portrait(EnemyKind.ENTRAINEMENT));
            bonusGuide.setSkipButton(buttons.create(Lang.t("Passer"), sounds.buttonClick, () -> {
                bonusGuide.stop();
                profile.markSeen(PlayerProfile.GUIDE_BONUS);
                scene.resumeGrid();
            }));
            stage.addActor(bonusGuide);
        }
        bonusGuide.toFront();
        bonusGuide.play(List.of(
            GuideOverlay.Step.say(Lang.t("La grille de la cagnotte ! C'est le plus rare et le plus généreux des Jeux bonus.")),
            GuideOverlay.Step.say(Lang.t("La grille tourne trois fois. Les symboles alignés se figent en doré, "
                + "les autres relancent.")),
            GuideOverlay.Step.say(Lang.t("Aligne au moins trois symboles identiques : en ligne, en colonne ou en "
                + "diagonale. Le Joker compte pour n'importe lequel.")),
            GuideOverlay.Step.say(Lang.t("Plus l'alignement est long et le symbole rare, plus tu gagnes. "
                + "Les gains tombent à la fin.")),
            GuideOverlay.Step.say(Lang.t("Ton rang, tes cartes et tes combinaisons du tour ajoutent "
                + "un petit bonus à chaque alignement."))), () -> {
                profile.markSeen(PlayerProfile.GUIDE_BONUS);
                scene.resumeGrid();
            });
    }

    /** @return la dette qui suit le compteur affiché : elle change quand les gains du tirage apparaissent. */
    SpinEconomy.Debt shownDebt() {
        return SpinEconomy.debt(player().getGains() - gainsNotYetShown,
            gameController.getGameState().getEnemy().getSpinCost());
    }

    /** Le texte d'un gain (ou d'une perte) du tirage vient d'apparaître : le compteur du panneau le suit. */
    void onGainsShown(double amount) {
        if (amount > 0) sounds.coinsGain.play();
        else if (amount < 0) sounds.coinsLoss.play();
        SpinEconomy.Debt before = shownDebt();
        gainsNotYetShown -= amount;
        refreshGains();
        if (shownDebt() != before) refreshEffects(); // Endetté ou Huissier, au rythme du compteur
    }

    /** Affiche par-dessus le jeu les cartes restant dans le deck (triées, pas dans l'ordre de pioche). */
    void onDeckClicked() {
        pileOverlay.show(Lang.t("Deck"), player().getDeck().getCards());
    }

    /** Affiche par-dessus le jeu les cartes de la défausse (triées). */
    void onDiscardClicked() {
        pileOverlay.show(Lang.t("Defausse"), player().getDiscardPile().getCards());
    }

    /** Échap : ouvre le menu pause, avec le bruitage du clic. */
    void openPauseMenu() {
        sounds.buttonClick.play();
        showPauseMenu();
    }

    /** Ouvre le menu pause : le jeu se fige et le menu reçoit seul les entrées. */
    void showPauseMenu() {
        if (pauseOverlay.isShown()) return;
        tooltip.hide();
        if (cutscene != null) cutscene.pauseSound();
        pauseOverlay.show();
        Gdx.input.setInputProcessor(pauseOverlay.getInput());
    }

    /** Effets réduits choisis dans le menu pause : une secousse en cours s'arrête. */
    void onEffectsChanged() {
        if (settings.isReducedEffects()) screenShake.stop();
    }

    /** Fige l'animation pendant {@code duration} secondes pour marquer un gros coup (sauf effets réduits). */
    void hitStop(float duration) {
        if (!settings.isReducedEffects()) hitStop = Math.max(hitStop, duration);
    }

    // -------------------------------------------------------------------------
    // Cycle de vie ScreenAdapter
    // -------------------------------------------------------------------------

    /**
     * Installe le processeur d'entrée de l'écran : le Stage (clics, survols)
     * en priorité, puis les raccourcis clavier : Espace ou F lance la machine si
     * possible, M change la Mise, 1 à 9 jouent la carte à cette place de la main,
     * B ouvre l'échoppe, J ouvre ou replie le journal du combat (à la manette : Y lance,
     * LB la Mise, RB l'échoppe) ; Échap ferme la fenêtre ouverte (carte, échoppe, pile, journal) ou, s'il
     * n'y en a pas, ouvre le menu pause. La pioche est automatique au début de
     * chaque tour du joueur.
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
                if (keycode == Input.Keys.ESCAPE && pileOverlay.isShown()) {
                    pileOverlay.hide();
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE && journalView.isShown()) {
                    journalView.hide();
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE) {
                    openPauseMenu();
                    return true;
                }
                if (cutscene != null && cutscene.isPlaying()) {
                    // la scène peut se servir de la touche (levier du duel) ; sinon toute autre touche la passe
                    if (!cutscene.keyDown(keycode)) cutscene.skip();
                    return true;
                }
                if (jackpotCelebration != null && jackpotCelebration.skip()) return true; // une touche passe aussi un Bingo déjà vu
                if (keycode == Input.Keys.J) { // le journal s'ouvre à tout moment, même pendant un choix
                    toggleJournal();
                    return true;
                }
                if (choiceOverlay.isShown() || shopOverlay.isShown() || pickOverlay.isShown() || chestOverlay.isShown()
                    || bingoAnimation.isPlaying()
                    || rainbowAnimation.isPlaying()) {
                    return true; // un choix ou une animation de carte est en cours
                }
                if ((keycode == Input.Keys.F || keycode == Input.Keys.SPACE || keycode == Input.Keys.BUTTON_Y)
                    && !spinControls.isDisabled()) {
                    onSpin();
                    return true;
                }
                // M : la touche M d'un clavier AZERTY arrive comme le point-virgule (place QWERTY)
                if (keycode == Input.Keys.M || keycode == Input.Keys.SEMICOLON || keycode == Input.Keys.BUTTON_L1) {
                    onStake();
                    return true;
                }
                if (guide != null && guide.isActive()) return false; // le Croupier guide : cartes et échoppe à la souris
                int card = cardKey(keycode);
                if (card >= 0 && !spinControls.isDisabled()) return hand.playAt(card);
                if (keycode == Input.Keys.B || keycode == Input.Keys.BUTTON_R1) {
                    openShop();
                    return true;
                }
                return false;
            }
        };
        gameInput = new InputMultiplexer(stage, keyboardInput);
        Gdx.input.setInputProcessor(pauseOverlay.isShown() ? pauseOverlay.getInput() : gameInput);
        luckyGame.getAssets().menuMusic.stop(); // le combat a sa propre musique
        switchMusic();
        if (tutorial == null) luckyGame.getProfile().meet(gameController.getGameState().getEnemy().getKind());
        music.play();
    }

    /** Met à jour le viewport puis repositionne les éléments qui dépendent de la taille de l'écran. */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        layout();
        choiceOverlay.layout();
        pickOverlay.layout();
        crossroadsOverlay.layout();
        chestOverlay.layout();
        shopOverlay.layout();
        cardDetail.layout();
        pileOverlay.hide();
        pauseOverlay.resize(width, height);
    }

    /** Efface l'écran, met à jour et dessine le Stage, puis l'effet de particules par-dessus. */
    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        music.update(); // suit le réglage « Musique », aussi depuis le menu pause
        if (pauseOverlay.isShown()) {
            // En pause : le jeu reste affiché, figé, sous le menu.
        } else if (hitStop > 0f) {
            hitStop -= delta * animationSpeed(); // micro-arrêt : l'image reste figée un instant sur un gros coup
        } else {
            stage.act(delta * animationSpeed());
        }
        spinControls.followShield();
        applyTableView(); // suit le réglage, aussi changé depuis le menu pause
        stage.getViewport().apply();
        stage.draw();

        SpriteBatch batch = luckyGame.getBatch();
        batch.begin();
        cardClickParticles.render(batch, delta);
        batch.end();

        pauseOverlay.render(delta);
    }

    /** Libère toutes les ressources natives (Stage, polices, textures, sons) possédées par cet écran. */
    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
        Fonts.release(shopFont);
        Fonts.release(playsFont);
        Fonts.release(shieldFont);
        Fonts.release(pileFont);
        tableTextures.dispose();
        tableLayer.dispose();
        frontScene.dispose();
        speech.dispose();
        props.dispose();
        playerMachine.dispose();
        enemyMachine.dispose();
        tooltip.dispose();
        enemyView.dispose();
        hud.dispose();
        sidePanel.dispose();
        journalView.dispose();
        symbolStrikes.dispose();
        slots.dispose();
        sounds.dispose();
        music.dispose();
        cardClickParticles.dispose();
        pileOverlay.dispose();
        choiceOverlay.dispose();
        pickOverlay.dispose();
        crossroadsOverlay.dispose();
        chestOverlay.dispose();
        bingoAnimation.dispose();
        fullPotentialAnimation.dispose();
        rainbowAnimation.dispose();
        if (cutscene != null) cutscene.dispose();
        if (guide != null) guide.dispose();
        if (bonusGuide != null) bonusGuide.dispose();
        shopOverlay.dispose();
        cardDetail.dispose();
        pauseOverlay.dispose();
        if (jackpotCelebration != null) jackpotCelebration.dispose();
        achievementToast.dispose();
        aspirationPanel.dispose();
        turnBanner.dispose();
        damageVignette.dispose();
        combatEnd.dispose();
        effectPopupAnimator.dispose();
    }
}
