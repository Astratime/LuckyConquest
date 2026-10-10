package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.animations.BingoCardAnimation;
import fr.astratime.lucky.animations.CardClickParticles;
import fr.astratime.lucky.animations.CardDealAnimator;
import fr.astratime.lucky.animations.CardDiscardAnimator;
import fr.astratime.lucky.animations.cutscenes.BonusGameCutscene;
import fr.astratime.lucky.animations.cutscenes.Cutscene;
import fr.astratime.lucky.animations.cutscenes.CutsceneKit;
import fr.astratime.lucky.animations.cutscenes.Cutscenes;
import fr.astratime.lucky.animations.cutscenes.LastDrawCutscene;
import fr.astratime.lucky.animations.CombatEndAnimation;
import fr.astratime.lucky.animations.Confetti;
import fr.astratime.lucky.animations.DamageVignette;
import fr.astratime.lucky.animations.EffectPopupAnimator;
import fr.astratime.lucky.animations.Fireworks;
import fr.astratime.lucky.animations.JackpotCelebration;
import fr.astratime.lucky.animations.TurnBanner;
import fr.astratime.lucky.animations.PistolShotAnimation;
import fr.astratime.lucky.animations.SymbolStrikes;
import fr.astratime.lucky.animations.RainbowChipsAnimation;
import fr.astratime.lucky.animations.ScreenShake;
import fr.astratime.lucky.assets.BackgroundMusic;
import fr.astratime.lucky.assets.CardTextures;
import fr.astratime.lucky.assets.EnemyTextures;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.GameSounds;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.assets.TableTextures;
import fr.astratime.lucky.controllers.GameController;
import fr.astratime.lucky.entities.BonusGame;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.CardPlayResult;
import fr.astratime.lucky.entities.Combo;
import fr.astratime.lucky.entities.DrawResult;
import fr.astratime.lucky.entities.GameState;
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
import fr.astratime.lucky.entities.choices.CardChoice;
import fr.astratime.lucky.entities.choices.RouletteChoice;
import fr.astratime.lucky.entities.SlotMachine;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.tower.TowerRun;
import fr.astratime.lucky.entities.tutorial.TutorialRun;
import fr.astratime.lucky.entities.RankBonus;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
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
import fr.astratime.lucky.loaders.CardLoader;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;
import fr.astratime.lucky.settings.AudioSettings;
import fr.astratime.lucky.settings.VisualSettings;
import fr.astratime.lucky.views.CardChoiceOverlay;
import fr.astratime.lucky.views.ChestOverlay;
import fr.astratime.lucky.views.CardDetailOverlay;
import fr.astratime.lucky.views.CardImage;
import fr.astratime.lucky.views.CasinoButtons;
import fr.astratime.lucky.views.CombatHud;
import fr.astratime.lucky.views.EnemyPickOverlay;
import fr.astratime.lucky.views.EnemyView;
import fr.astratime.lucky.views.GuideOverlay;
import fr.astratime.lucky.views.HandView;
import fr.astratime.lucky.views.MinimumScreenViewport;
import fr.astratime.lucky.views.HealthBarView;
import fr.astratime.lucky.views.PileContentOverlay;
import fr.astratime.lucky.views.PilesView;
import fr.astratime.lucky.views.PauseOverlay;
import fr.astratime.lucky.views.PlayArea;
import fr.astratime.lucky.views.ShieldBadge;
import fr.astratime.lucky.views.SpinControls;
import fr.astratime.lucky.views.ShopIcon;
import fr.astratime.lucky.views.ShopOverlay;
import fr.astratime.lucky.views.SidePanel;
import fr.astratime.lucky.views.SlotView;
import fr.astratime.lucky.views.TableView;
import fr.astratime.lucky.views.Tooltip;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

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
public class GameScreen extends ScreenAdapter {

    // -------------------------------------------------------------------------
    // Constantes d'affichage
    // -------------------------------------------------------------------------

    /** Taille minimale de l'écran de jeu : dans une fenêtre plus petite, il est réduit (voir {@link MinimumScreenViewport}). */
    /** Relance : pause entre l'arrêt du premier tirage et le second. */
    private static final float REROLL_PAUSE = 0.45f;
    /** Chant de l'ennemi : pause entre la fin de la distribution et la carte jouée d'office. */
    private static final float SONG_DELAY = 0.6f;
    private static final float  MIN_WIDTH      = 1600f;
    private static final float  MIN_HEIGHT     = 1080f;  // les deux côtés de la table, à la même taille
    private static final float  CARD_WIDTH     = 95f;
    private static final float  CARD_HEIGHT    = 135f;
    private static final String CARD_BACK_PATH = "cards/light/BACK.png";
    /** Carte de test de l'échoppe qui ouvre le Jeu bonus à coup sûr (mode ADMIN seulement). */
    private static final String BONUS_GAME_CARD = "jeu_bonus";

    private static final float BUTTON_MARGIN = 20f;   // boutons en bas à gauche, sous la table

    /** Dégâts à partir desquels un coup sur l'ennemi fige l'image un instant (micro-arrêt). */
    private static final int   BIG_HIT               = 60;
    /** Un gros coup sonne plus grave. */
    private static final float BIG_HIT_PITCH         = 0.85f;
    /** Bouclier de l'ennemi : le son du bouclier, un ton plus bas. */
    private static final float ENEMY_SHIELD_PITCH    = 0.84f;
    private static final float PLAYER_SHIELD_GAP     = 40f;   // entre ses rouleaux et son bouclier, comme l'ennemi
    private static final float SHIELD_TEXT_ABOVE     = 40f;   // texte d'un bouclier brisé, au-dessus de lui
    /** Volume du son de scène d'un Bingo, sous le son de Bingo d'origine joué en même temps. */
    private static final float BINGO_SCENE_VOLUME    = 0.8f;
    /** Volume de la musique du combat quand le réglage « Musique » est à 100 %. */
    private static final float COMBAT_MUSIC_LEVEL    = 0.15f;
    private static final float BIG_HIT_STOP          = 0.09f;
    /** Paire : clignotement doré des deux rouleaux et confettis lâchés par chacun. */
    private static final Color PAIR_GLOW             = Palette.GOLD;
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
    /** Boutons de fin de combat : écart entre eux, hauteur (fraction de l'écran) et apparition en fondu. */
    private static final float END_BUTTONS_GAP    = 30f;
    private static final float END_BUTTONS_HEIGHT = 0.46f;  // sous l'annonce, au-dessus de la rangée de cartes
    private static final float END_BUTTONS_FADE   = 0.4f;
    /** Icône de l'échoppe : marge au bord de la zone de jeu. */
    private static final float SHOP_ICON_MARGIN = 30f;
    /** Carte achetée : posée une fois l'échoppe refermée. */
    private static final float PURCHASE_DELAY   = 0.9f;
    /** Étape de la Tour des épreuves : en haut à gauche de la zone de jeu. */
    private static final float STAGE_LABEL_MARGIN = 30f;
    /** Coffre au trésor ouvert : gerbe de confettis. */
    private static final int   CHEST_CONFETTI        = 70;
    /** Pot de Lutin qui apparaît : gerbe de confettis. */
    private static final int   POT_CONFETTI          = 50;
    /** Temps laissé au texte de la riposte (et au coup sur la barre) avant de passer au tour du joueur. */
    private static final float RIPOSTE_TEXT_TIME     = 0.8f;
    /** Le temps de lire « ELLE RÉSISTE ! DERNIER TIRAGE » avant le duel des leviers. */
    private static final float LAST_DRAW_DELAY       = 1.2f;
    /** Combinaison formée : hauteur de son texte au-dessus de la main (en hauteurs de carte). */
    private static final float COMBO_TEXT_HEIGHT     = 2f;

    // -------------------------------------------------------------------------
    // Contrôleur — seul point d'accès à la logique de jeu
    // -------------------------------------------------------------------------

    private final GameController gameController;

    // -------------------------------------------------------------------------
    // Ressources (à disposer dans dispose())
    // -------------------------------------------------------------------------

    private final LuckyGame           luckyGame;
    /** Chapitre de la Tour des épreuves ou donjon de l'Exploration, ou {@code null} pour un combat seul (« Entraînement »). */
    private final CombatRun           run;
    private final Stage               stage;
    private final BitmapFont          font;
    /** Libellés du deck et de la défausse : police du jeu, à balises de couleur (nom crème, nombre doré). */
    private final BitmapFont          pileFont = Fonts.jerseyMarkup(20, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont          shopFont = Fonts.jersey(30, Palette.GOLD, 2f, Palette.TEXT_SHADE);
    private final BitmapFont          playsFont = Fonts.jersey(24, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont          shieldFont = Fonts.jersey(26, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private final Texture             cardBackTexture;
    private final CardTextures        cardTextures  = new CardTextures();
    private final EnemyTextures       enemyTextures = new EnemyTextures(cardTextures);
    private final CasinoButtons       buttons       = new CasinoButtons();
    private final HudTextures         hudTextures   = new HudTextures();
    private final VisualSettings      settings      = new VisualSettings();
    private final ScreenShake         screenShake   = new ScreenShake(settings);
    private CutsceneKit               cutsceneKit;
    /** La cinématique en cours ou la dernière jouée ({@code null} avant la première). */
    private Cutscene                  cutscene;
    private final DamageVignette      damageVignette = new DamageVignette();
    private final Confetti            confetti;
    private final CombatEndAnimation  combatEnd;
    private final TableTextures       tableTextures = new TableTextures();
    private final AudioSettings       audio         = new AudioSettings();
    private final GameSounds          sounds        = new GameSounds(audio);
    /** Musique du combat (fournie par Astra), gardée basse pour ne pas couvrir les bruitages. */
    private final BackgroundMusic     music         = new BackgroundMusic("music/combat.ogg", audio, COMBAT_MUSIC_LEVEL);
    private final CardClickParticles  cardClickParticles = new CardClickParticles();
    private final EffectPopupAnimator effectPopupAnimator;
    private final JackpotCelebration  jackpotCelebration;
    /** Annonces « À TOI DE JOUER ! » et « TOUR ENNEMI ». */
    private final TurnBanner          turnBanner = new TurnBanner();
    private final Tooltip             tooltip;
    private final PileContentOverlay  pileOverlay;
    private final CardChoiceOverlay   choiceOverlay;
    private final BingoCardAnimation  bingoAnimation;
    private final PistolShotAnimation pistolAnimation;
    private final SymbolStrikes       symbolStrikes;
    private final RainbowChipsAnimation rainbowAnimation;
    private final ShopOverlay         shopOverlay;
    private final CardDetailOverlay   cardDetail;
    /** Menu pause (Échap ou bouton pause) : options, recommencer, menu principal ; le jeu est figé tant qu'il est ouvert. */
    private final PauseOverlay        pauseOverlay;
    /** Icône de l'échoppe (en haut à droite de la zone de jeu), qui ouvre la boutique. */
    private final ShopIcon            shopIcon;
    /** Transitions de la Tour des épreuves : choix de l'adversaire, apparition du boss. */
    private final EnemyPickOverlay    pickOverlay;
    /** Exploration : le coffre au trésor, au bout d'un donjon. */
    private final ChestOverlay        chestOverlay;
    /** Étape de la Tour des épreuves (« CHAPITRE 1 · COMBAT 2/3 »), vide pour un combat seul. */
    private final Label               stageLabel;
    /** Tutoriel : le Croupier, sa bulle et son voile, et ce qu'il fait faire ({@code null} hors du tutoriel). */
    private final GuideOverlay        guide;
    private final TutorialDirector    tutorial;
    /** Tutoriel : le dernier tirage était un Bingo. */
    private boolean                   lastSpinBingo;
    /** Le Croupier qui explique le Jeu bonus la première fois qu'il s'ouvre ({@code null} avant). */
    private GuideOverlay              bonusGuide;

    // -------------------------------------------------------------------------
    // Vues et acteurs Scene2D
    // -------------------------------------------------------------------------

    private final PlayArea   playArea;
    private final TableView  table;
    private final SidePanel  sidePanel;
    private final CombatHud  hud;
    /** Côté de l'ennemi : le croupier, ses rouleaux, ses cartes et ses piles ; joue son tour en animation. */
    private final EnemyView  enemyView;
    /** Bouclier du joueur, à droite de ses rouleaux (comme la défense de l'ennemi en face). */
    private final ShieldBadge playerShield;
    private final PilesView  piles;
    private final HandView   hand;
    private final SlotView   slots;
    /** Calque des textes animés (bonus des cartes, résultats du tirage) : au-dessus du jeu, sous le voile des piles. */
    private final Group      popupLayer = new Group();
    /** Le levier, à gauche des rouleaux du joueur, et la Mise, à droite de son bouclier. */
    private final SpinControls spinControls;
    /** Cartes jouées ce tour sur la limite (« Cartes 2/4 »), coût du tirage et Mise, à droite du bouton de Mise. */
    private final Label      playsLabel;
    private final TextButton restartButton;
    private final TextButton menuButton;
    /** Bouton pause, en bas à droite : ouvre le même menu qu'Échap. */
    private final TextButton pauseButton;
    /** Boutons de fin de combat (Recommencer, Menu principal), centrés sous l'annonce de victoire ou de défaite. */
    private final Table      endButtons = new Table();

    /** Entrées du jeu (Stage puis raccourcis clavier), rendues à la fermeture du menu pause. */
    private InputProcessor gameInput;

    /** Temps restant du micro-arrêt en cours (voir {@link #hitStop(float)}). */
    private float hitStop = 0f;

    /**
     * Gains du dernier tirage dont le texte n'est pas encore apparu : le compteur
     * du panneau ne les ajoute qu'à l'apparition de leur texte « GAINS + ».
     */
    private int gainsNotYetShown = 0;

    /** Durée de l'annonce « TOUR ENNEMI » du tirage en cours (0 si l'ennemi, vaincu, ne riposte pas). */
    private TurnResult currentResult;

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
        this(luckyGame, null);
    }

    /**
     * Écran de jeu d'une ascension de la Tour des épreuves.
     *
     * @param run chapitre ou donjon en cours ({@code null} : un combat seul, comme « Entraînement »)
     */
    public GameScreen(LuckyGame luckyGame, CombatRun run) {
        this.luckyGame = luckyGame;
        this.run       = run;
        // Le deck construit par le joueur (ou le deck de départ), relu à chaque nouveau combat.
        PlayerProfile profile = luckyGame.getProfile();
        if (run instanceof TutorialRun) {
            // Tutoriel : son deck rangé d'avance, la machine de départ, aucun rang, une échoppe qui ne vend que le Bingo.
            this.gameController = new GameController(() -> CardLoader.loadDeck(TutorialRun.deck()),
                CardLoader.cardFactory(), TutorialRun.shop(),
                cards -> new Player(Lang.t("Joueur"), Player.BASE_HP, cards, RankBonus.NONE, Symbol.classicReels()));
        } else {
            Map<String, Integer> shop = new LinkedHashMap<>(CardLoader.loadShop());
            if (!profile.isAdmin()) shop.remove(BONUS_GAME_CARD); // carte de test du Jeu bonus : mode ADMIN seulement
            this.gameController = new GameController(() -> CardLoader.loadDeck(profile.getDeck()),
                CardLoader.cardFactory(), shop,
                cards -> new Player(Lang.t("Joueur"), Player.BASE_HP, cards, profile.getRankBonus(), profile.getMachine()));
        }
        if (run != null) gameController.setPlaceRule(run.getPlaceRule()); // Exploration : la règle du lieu
        gameController.setBonusGameEnabled(!(run instanceof TutorialRun)); // pas de Jeu bonus dans le tutoriel
        EnemyKind.setTowerHard(run instanceof TowerRun tower && tower.isHard()); // Tour : mode difficile
        gameController.restart(firstEnemy()); // le premier ennemi du chapitre, ou le croupier d'entraînement
        if (run instanceof TutorialRun) {
            TutorialRun.arrange(player().getDeck().getCards());
            TutorialRun.arrange(gameController.getGameState().getEnemy());
        }
        // Le SpriteBatch est partagé avec LuckyGame et ne doit PAS être disposé ici.
        this.stage = new Stage(new MinimumScreenViewport(MIN_WIDTH, MIN_HEIGHT), luckyGame.getBatch());

        font                = new BitmapFont();
        cardBackTexture     = cardTextures.get(CARD_BACK_PATH);
        effectPopupAnimator = new EffectPopupAnimator(popupLayer, effect -> sounds.effect(effect).play());
        tooltip             = new Tooltip(hudTextures);
        pileOverlay         = new PileContentOverlay(stage, font, tooltip, cardTextures::get, CARD_WIDTH, CARD_HEIGHT);

        playArea   = new PlayArea(stage, SidePanel.WIDTH);
        confetti   = new Confetti(new TextureRegion(hudTextures.pixel), sounds.confettiPop, sounds.confettiRain);
        Fireworks.Sounds fireworkSounds = new Fireworks.Sounds(sounds.fireworkLaunch, sounds.fireworkBurst);
        table      = new TableView(playArea, tableTextures, CARD_WIDTH, CARD_HEIGHT);
        enemyView  = new EnemyView(table, enemyTextures, pileFont, tooltip, effectPopupAnimator,
            sounds.cardDeal, sounds.cardFlip, sounds.reelSpin, sounds.reelStop,
            () -> gameController.getGameState().getEnemy(), hudTextures.pixel);
        playerShield = new ShieldBadge(Lang.t("Bouclier"), 0, enemyTextures.symbol(EnemySymbol.SHIELD), shieldFont,
            hudTextures.pixel);
        playerShield.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer != -1) return;
                Vector2 top = playerShield.localToStageCoordinates(new Vector2(ShieldBadge.ICON_SIZE / 2f, playerShield.getHeight()));
                tooltip.show(Lang.t("Ton bouclier"), Lang.t("Absorbe les attaques de l'ennemi et s'use à chaque coup\nGagné avec les symboles de défense ; "
                    + "ce qui reste en fin de tour remplit le Coffre"),
                    top.x, top.y + 6f);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) tooltip.hide();
            }
        });
        sidePanel  = new SidePanel(hudTextures);
        jackpotCelebration = new JackpotCelebration(playArea, screenShake, settings, hudTextures,
            sidePanel::getCoinCenter, sidePanel::bumpCoin, fireworkSounds);
        hud        = new CombatHud(playArea, hudTextures, gameController::getGameState);

        spinControls  = new SpinControls(table, playerShield, hudTextures, playsFont, shieldFont, tooltip,
            sounds.spinButton, sounds.buttonClick, this::onSpin, this::onStake);
        restartButton = buttons.createAction(Lang.t("Recommencer"), sounds.buttonClick, this::onRestart);
        menuButton    = buttons.createAction(Lang.t("Menu principal"), sounds.buttonClick, this::onBackToMenu);
        pauseButton   = buttons.create(Lang.t("Pause"), sounds.buttonClick, this::showPauseMenu);
        playsLabel    = new Label("", new Label.LabelStyle(playsFont, Color.WHITE));
        endButtons.add(restartButton).size(restartButton.getWidth(), restartButton.getHeight());
        endButtons.add(menuButton).size(menuButton.getWidth(), menuButton.getHeight()).padLeft(END_BUTTONS_GAP);
        endButtons.pack();
        endButtons.setVisible(false);

        piles = new PilesView(playArea, cardBackTexture, pileFont, tooltip, sounds.buttonClick, sounds.pileHover,
            CARD_WIDTH, CARD_HEIGHT, this::onDeckClicked, this::onDiscardClicked);
        hand  = new HandView(table, CARD_WIDTH, CARD_HEIGHT, cardTextures, tooltip, sounds.cardHover, piles, this::player,
            new CardDealAnimator(stage, cardBackTexture, CARD_WIDTH, CARD_HEIGHT, sounds.cardDeal, sounds.cardFlip),
            new CardDiscardAnimator(stage, cardBackTexture, CARD_WIDTH, CARD_HEIGHT, sounds.cardDeal, sounds.cardFlip),
            this::onCardPlayed);
        slots = new SlotView(table, tooltip, effectPopupAnimator, sounds.reelSpin, sounds.reelStop, sounds.reelSuspense,
            gameController.getGameState().getPlayer().getSlotMachine().getReels());
        combatEnd = new CombatEndAnimation(playArea, screenShake, new TextureRegion(hudTextures.pixel),
            hudTextures.bannerBand, new TextureRegion(cardBackTexture), confetti, fireworkSounds, CARD_WIDTH, CARD_HEIGHT);
        choiceOverlay   = new CardChoiceOverlay(stage, hudTextures, cardBackTexture, cardTextures, CARD_WIDTH, CARD_HEIGHT);
        bingoAnimation  = new BingoCardAnimation(settings, new TextureRegion(hudTextures.pixel));
        pistolAnimation = new PistolShotAnimation(hudTextures.pistol, new TextureRegion(hudTextures.pixel));
        symbolStrikes   = new SymbolStrikes(settings, screenShake, new TextureRegion(hudTextures.pixel), hudTextures.coin,
            sidePanel::bumpCoin);
        rainbowAnimation = new RainbowChipsAnimation(settings, new TextureRegion(hudTextures.pixel));
        cutsceneKit      = new CutsceneKit(settings, screenShake, sounds, enemyTextures, shopFont);
        shopOverlay      = new ShopOverlay(stage, hudTextures, tooltip, CARD_WIDTH, CARD_HEIGHT);
        cardDetail       = new CardDetailOverlay(stage, hudTextures, tooltip, CARD_WIDTH, CARD_HEIGHT);
        pauseOverlay     = new PauseOverlay(luckyGame, luckyGame.getBatch(), hudTextures, sounds.buttonClick, sounds.menuHover,
            settings,
            new PauseOverlay.Listener() {
                @Override public void onResume()         { Gdx.input.setInputProcessor(gameInput); if (cutscene != null) cutscene.resumeSound(); }
                @Override public void onRestart()        { Gdx.input.setInputProcessor(gameInput); GameScreen.this.onRestart(); }
                @Override public void onMainMenu()       { onBackToMenu(); }
                @Override public void onEffectsChanged() { GameScreen.this.onEffectsChanged(); }
            });
        pickOverlay      = new EnemyPickOverlay(stage, hudTextures, enemyTextures, sounds.cardFlip, sounds.cardHover,
            sounds.fireworkBurst);
        chestOverlay     = new ChestOverlay(stage, hudTextures, enemyTextures, cardTextures, sounds.cardFlip,
            sounds.coinsGain, (x, y) -> confetti.burst(x, y, CHEST_CONFETTI));
        stageLabel       = new Label("", new Label.LabelStyle(shopFont, Color.WHITE));
        stageLabel.setTouchable(Touchable.disabled);
        refreshStageLabel();
        hand.setOnInspect(this::showCardDetail);
        sidePanel.setEffectHelp(tooltip, row -> {
            sounds.cardInspect.play();
            cardDetail.showEffect(row.name(), row.icon(), row.text(), row.description());
        });
        if (run instanceof TutorialRun) {
            guide    = new GuideOverlay(hudTextures, enemyTextures.portrait(EnemyKind.ENTRAINEMENT));
            guide.setSkipButton(buttons.create(Lang.t("Passer le tutoriel"), sounds.buttonClick, this::skipTutorial));
            tutorial = new TutorialDirector(guide, new TutorialBoard());
        } else {
            guide    = null;
            tutorial = null;
        }
        hand.setBlockedReason(this::unplayableReason, this::onCardRefused);
        pileOverlay.setOnInspect(this::showCardDetail);
        shopIcon = new ShopIcon(hudTextures.shop, shopFont, sounds.shopHover, this::openShop);

        layout();
        refreshAll();

        stage.addActor(table.getActor());
        stage.addActor(enemyView.getActor());
        stage.addActor(playerShield);
        stage.addActor(sidePanel.getActor());
        piles.addTo(stage);
        hud.addTo(stage);
        spinControls.addTo(stage);
        stage.addActor(pauseButton);
        stage.addActor(playsLabel);
        stage.addActor(slots.getActor());
        stage.addActor(hand.getActor());
        stage.addActor(shopIcon);
        stage.addActor(stageLabel);
        stage.addActor(pistolAnimation);
        stage.addActor(symbolStrikes);
        stage.addActor(popupLayer);
        stage.addActor(turnBanner);   // annonce des tours : au-dessus du jeu, ne bloque pas les clics
        stage.addActor(combatEnd);
        stage.addActor(endButtons); // au-dessus de l'annonce de fin de combat
        stage.addActor(pickOverlay.getActor()); // Tour des épreuves : choix de l'adversaire, boss
        stage.addActor(chestOverlay.getActor()); // Exploration : coffre au trésor
        stage.addActor(confetti);
        stage.addActor(damageVignette);
        stage.addActor(jackpotCelebration); // par-dessus le jeu et le panneau : bloque les clics pendant la fête
        stage.addActor(bingoAnimation);         // carte Bingo : faisceaux et téléportation, bloque les clics
        stage.addActor(rainbowAnimation);       // carte Arc-en-ciel : jetons et poussière d'étoiles, bloque les clics
        stage.addActor(choiceOverlay.getActor()); // choix demandé par une carte (Pari, Roulette russe)
        stage.addActor(shopOverlay.getActor());   // échoppe, ouverte depuis son icône
        stage.addActor(pileOverlay.getActor()); // voile de consultation des piles, par-dessus le jeu
        if (guide != null) stage.addActor(guide); // tutoriel : le Croupier, sous les fiches et les infobulles
        stage.addActor(cardDetail.getActor());  // fiche d'une carte, par-dessus tout le reste
        stage.addActor(tooltip.getActor());     // en dernier : toujours au-dessus
        stage.addActor(screenShake);            // invisible : met à jour la caméra

        startPlayerTurn(); // le combat commence par le tour du joueur, main piochée d'office
    }

    // -------------------------------------------------------------------------
    // Actions du joueur — transmises au GameController
    // -------------------------------------------------------------------------

    /**
     * Début du tour du joueur (début du combat, ou fin du tour ennemi) :
     * annonce « À TOI DE JOUER ! », puis pioche automatique de la main.
     */
    private void startPlayerTurn() {
        setSpinDisabled(true);
        turnBanner.play(TurnBanner.Side.PLAYER, playArea.getCenterX(), turnBannerY(), playArea.getWidth());
        stage.addAction(Actions.delay(TurnBanner.DURATION, Actions.run(this::drawHand)));
    }

    /** Pioche une nouvelle main et lance son animation de distribution ; active le spin. */
    private void drawHand() {
        if (isCombatOver()) return;
        refreshCombos(); // les combinaisons du tour précédent s'éteignent
        hand.setLocked(false); // une carte achetée entre deux tours reste sur la table
        showReelCount(gameController.getReelCount()); // le 4e rouleau de la surchauffe ne dure qu'un tour
        slots.setBlockedReels(blockedReels(player().getLastingEffects().getForbiddenReel())); // Rouleau interdit, rouleaux volés
        GameController.Purchase gift = gameController.claimBonusCard(); // Bingo glissé dans le deck après un Bingo de gains
        if (gift != null) placePurchase(gift);
        DrawResult drawn = gameController.drawCards(); // Grignotage, Scorbut, Aveuglement et Chant s'y appliquent
        hand.setHidden(gameController.isHandHidden());
        hand.deal(drawn);
        List<EffectPopup> notices = gameController.getTurnNotices();
        if (!notices.isEmpty()) {
            effectPopupAnimator.play(notices, playArea.getCenterX(), table.getHandRowY() + CARD_HEIGHT * 1.3f);
            refreshEffects(); // les mauvais sorts de l'ennemi s'appliquent : ils quittent le panneau
        }
        refreshPlays();
        setSpinDisabled(false);
        if (tutorial != null) tutorial.beat(TutorialDirector.Beat.HAND_DEALT, () -> { });
        // Chant de l'ennemi : une fois la main distribuée, des cartes partent d'office.
        float dealt = drawn.getAddedToHand().size() * CardDealAnimator.DEAL_STAGGER_DELAY + SONG_DELAY;
        stage.addAction(Actions.delay(dealt, Actions.run(() -> {
            for (Card card : gameController.takeSongCards()) hand.forcePlay(card);
        })));
    }

    /** Fin du tour du joueur (ses résultats sont affichés) : annonce « TOUR ENNEMI » avant la riposte. */
    private void announceEnemyTurn() {
        turnBanner.play(TurnBanner.Side.ENEMY, playArea.getCenterX(), turnBannerY(), playArea.getWidth());
    }

    /** @return la hauteur des annonces de tour : sur le filet, entre le côté du joueur et celui de l'ennemi. */
    private float turnBannerY() {
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
    private void onCardPlayed(Card card, CardImage image, Vector2 cardCenter, Vector2 clickPos) {
        sounds.cardPlay.play();
        cardClickParticles.play(clickPos.x, clickPos.y);
        Gdx.app.log("GameScreen", "Carte jouee : " + card);

        List<Combo> combosBefore = gameController.getCurrentCombos();
        CardPlayResult playResult = gameController.playCard(card);
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
        showReelCount(gameController.getReelCount()); // Machine en surchauffe : un 4e rouleau surgit
        announceCombos(combosBefore);

        DrawResult drawResult = playResult.getDrawResult();
        if (!drawResult.getAddedToHand().isEmpty() || !drawResult.getDiscarded().isEmpty()) {
            hand.deal(drawResult);
        }
        askChoice(playResult.getChoice(), cardCenter);
        if (playResult.getRainbow() != null) playRainbow(playResult.getRainbow());
    }

    /**
     * Met en avant les combinaisons que forment les cartes jouées ; le nom et le
     * multiplicateur de celles qui viennent d'être formées (absentes de
     * {@code before}) s'affichent au-dessus de la main, suivis du total s'il y en
     * a plusieurs.
     */
    private void announceCombos(List<Combo> before) {
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

    /** Fait briller dans le panneau les combinaisons des cartes jouées ce tour. @return ces combinaisons */
    private List<Combo> refreshCombos() {
        List<Combo> combos = gameController.getCurrentCombos();
        sidePanel.setCombos(combos);
        return combos;
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
    private static String suitName(Card.Suit suit) {
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
    private void openShop() {
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
            offer -> showCardDetail(offer.card()));
        if (guide != null) { // l'échoppe passe devant tout : le Croupier reste visible au-dessus
            guide.toFront();
            tooltip.getActor().toFront();
        }
    }

    /** Affiche la fiche de {@code card} (avec son prix si elle est vendue à l'échoppe), par-dessus tout le reste. */
    private void showCardDetail(Card card) {
        Integer price = gameController.getShopOffers().stream()
            .filter(offer -> offer.card().getId().equals(card.getId()))
            .map(GameController.ShopOffer::price).findFirst().orElse(null);
        sounds.cardInspect.play();
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
            at = dropOnPile(card, piles.deck().getTopX(), piles.deck().getTopY(), () -> piles.refresh(player()));
        }
        confetti.burst(at.x, at.y, POT_CONFETTI);
        effectPopupAnimator.play(List.of(new EffectPopup(card.getName().toUpperCase() + " !", EffectPopup.Style.SPECIAL,
            PopupScale.MAX_INTENSITY)), at.x, at.y + CARD_HEIGHT * 0.8f);
    }

    /** Table pleine : le Pot de Lutin apparaît au-dessus de la défausse et s'y pose. @return son centre */
    private Vector2 potToDiscard(Card pot) {
        return dropOnPile(pot, piles.discard().getTopX(), piles.discard().getTopY(), () -> {
            piles.onCardLanded();
            piles.refresh(player());
        });
    }

    /**
     * {@code card} surgit au-dessus de la pile posée en ({@code x}, {@code y}),
     * puis s'y pose ; {@code landed} quand elle y arrive. @return le point au-dessus de la pile
     */
    private Vector2 dropOnPile(Card card, float x, float y, Runnable landed) {
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
    private void askChoice(CardChoice choice, Vector2 cardCenter) {
        if (choice instanceof BetChoice) {
            choiceOverlay.showBet(gameController.getBetOptions(), slots::regionOf, symbol -> {
                effectPopupAnimator.play(gameController.placeBet(symbol), cardCenter.x, cardCenter.y);
                refreshEffects();
            });
        } else if (choice instanceof RiggedReelChoice) {
            choiceOverlay.showRiggedReel(gameController.getBetOptions(), slots::regionOf, symbol -> {
                effectPopupAnimator.play(gameController.rigReel(symbol), cardCenter.x, cardCenter.y);
                refreshEffects();
            });
        } else if (choice instanceof RouletteChoice roulette) {
            choiceOverlay.showRoulette(roulette.cursed(), roulette.pistolPercent(), roulette.cursedPercent(),
                roulette.penaltyPercent(),
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

    /**
     * Le joueur clique sur une carte qui ne peut pas être jouée (limite de
     * cartes du tour atteinte, ou Bingo d'un symbole recyclé) : elle reste dans
     * la main, son infobulle donne la raison (voir {@link HandView}) et un texte
     * « LIMITE ATTEINTE ! » ou « BINGO BLOQUÉ ! » surgit sur la carte.
     */
    private void onCardRefused(Card card, String reason, Vector2 cardCenter) {
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
    private void playBingo(CardImage image, List<EffectPopup> popups) {
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
    private void onSpin() {
        if (spinControls.isDisabled() || isBusy()) return;
        if (tutorial != null && !tutorial.allowsSpin()) return; // le Croupier n'a pas encore demandé de lancer
        spin();
    }

    /** Active ou désactive le lancer, et la Mise avec lui (elle se choisit avant le lancer). */
    private void setSpinDisabled(boolean disabled) {
        spinControls.setDisabled(disabled);
    }

    /**
     * Bouton de Mise : passe au palier suivant (aucune, 10 %, 25 %, 50 % des
     * gains), et un jeton de casino tombe sur la table. On ne mise que ce qu'on a.
     */
    private void onStake() {
        if (spinControls.isStakeDisabled() || isBusy()) return;
        if (tutorial != null && !tutorial.allowsStake()) return;
        if (!gameController.canStake()) {
            Vector2 above = spinControls.aboveStake();
            effectPopupAnimator.play(List.of(new EffectPopup(Lang.t("PAS DE GAINS À MISER"), EffectPopup.Style.DAMAGE,
                PopupScale.SECONDARY_INTENSITY)), above.x, above.y);
            return;
        }
        SpinEconomy.Stake stake = gameController.nextStake();
        spinControls.dropChips(stake);
        if (stake != SpinEconomy.Stake.NONE) sounds.cardDeal.play();
        refreshPlays();
        if (tutorial != null) tutorial.onStake(stake);
    }

    /** @return {@code true} si une fenêtre ou une animation attend : ni pioche ni lancer possibles. */
    private boolean isBusy() {
        return pileOverlay.isShown() || choiceOverlay.isShown() || shopOverlay.isShown() || cardDetail.isShown()
            || bingoAnimation.isPlaying() || rainbowAnimation.isPlaying() || pickOverlay.isShown() || chestOverlay.isShown();
    }

    /** Lance la machine (bouton, touche F, ou d'elle-même après une carte Bingo). */
    private void spin() {
        int enemyHpBefore = gameController.getGameState().getEnemy().getHp();
        int cost   = gameController.getSpinCost();
        int staked = gameController.getStakeAmount();
        TurnResult result = gameController.spin();
        if (tutorial != null) tutorial.onSpin();
        lastSpinBingo = result.isJackpot();
        table.setReelsRainbow(false); // la bordure d'un jackpot précédent s'arrête au lancer suivant
        // Gains et PV du tirage ne se montrent qu'à l'apparition de leurs textes, après l'arrêt des rouleaux.
        gainsNotYetShown += sumOf(result, GainsEarnedEvent.class, gains -> gains.amount)
            - sumOf(result, GainsLostEvent.class, lost -> lost.amount);
        if (result.getBonusGame() != null) gainsNotYetShown += result.getBonusGame().getTotal(); // à la fin du Jeu bonus
        payForSpin(cost, staked); // seuls le coût et la Mise partent tout de suite
        int enemyHeal = sumOf(result, EnemyHealedEvent.class, heal -> heal.amount);
        hud.holdBack(enemyHpBefore - gameController.getGameState().getEnemy().getHp() + enemyHeal, enemyHeal,
            sumOf(result, PlayerDamagedEvent.class, hit -> hit.damage),
            sumOf(result, PlayerHealedEvent.class, heal -> heal.amount));
        hand.discardAll();
        setSpinDisabled(true); // le tour du joueur se termine : la suite attend l'arrêt des rouleaux
        showReelCount(result.getDrawnSymbols().length);
        Symbol[] rerolled = result.getRerolledDraw();
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

    /** Le tirage est payé : le coût (et la Mise) quittent les gains, les jetons glissent dans la machine. */
    private void payForSpin(int cost, int staked) {
        spinControls.pull(); // le levier reste tiré, les jetons misés glissent dans la machine
        if (cost <= 0 && staked <= 0) return;
        List<EffectPopup> popups = new ArrayList<>();
        if (cost > 0) popups.add(new EffectPopup(Lang.f("TIRAGE -{0}", SidePanel.formatGains(cost)), EffectPopup.Style.DAMAGE,
            PopupScale.SECONDARY_INTENSITY));
        if (staked > 0) popups.add(new EffectPopup(Lang.f("MISE -{0}", SidePanel.formatGains(staked)), EffectPopup.Style.DAMAGE,
            PopupScale.SECONDARY_INTENSITY));
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
    private void onReelsStopped(TurnResult result) {
        currentResult = result;
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

    /** Les résultats du tirage sont affichés : le Croupier du tutoriel les commente, puis le tour de l'ennemi. */
    private void afterPlayerSpin() {
        if (tutorial != null) tutorial.beat(TutorialDirector.Beat.SPIN_RESOLVED, this::playEnemyTurn);
        else playEnemyTurn();
    }

    /**
     * Tour de l'ennemi (s'il a joué) : « TOUR ENNEMI » est annoncé, puis
     * l'ennemi pioche, joue ses cartes et lance sa machine (voir
     * {@link EnemyView#playTurn}) ; ensuite, la fin du tour.
     */
    private void playEnemyTurn() {
        EnemyTurnResult enemyTurn = currentResult.getEnemyTurn();
        if (enemyTurn == null) {
            endTurn();
            return;
        }
        announceEnemyTurn();
        stage.addAction(Actions.delay(TurnBanner.DURATION,
            Actions.run(() -> enemyView.playTurn(enemyTurn, this::onEventShown, this::endTurn))));
    }

    /**
     * Fin du tour : ce qui reste du bouclier du joueur remplit son Coffre (texte
     * à droite de sa barre de vie), puis le combat se termine si un camp est
     * vaincu, ou le joueur reprend la main. Le Dernier tirage de la Machine
     * Originelle se joue en cinématique (le duel des leviers) ; ce qui le suit
     * s'affiche sous son fondu final.
     */
    private void endTurn() {
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
    private void showEndEvents(List<Event> events) {
        float margin = showEndTexts(events) ? RIPOSTE_TEXT_TIME : RESULT_TEXTS_MARGIN;
        stage.addAction(Actions.delay(margin, Actions.run(this::finishTurn)));
    }

    /** @return {@code true} si les événements {@code events} ont des textes, affichés à droite de la barre de vie du joueur. */
    private boolean showEndTexts(List<Event> events) {
        Vector2 anchor = hud.besidePlayerHealthBar(SlotView.POPUP_PLAYER_GAP);
        List<EffectPopup> texts = new ArrayList<>();
        events.forEach(event -> texts.addAll(event.getPopups()));
        if (!texts.isEmpty()) effectPopupAnimator.play(texts, anchor.x, anchor.y);
        events.forEach(this::onEventShown);
        refreshEffects();
        return !texts.isEmpty();
    }

    /**
     * Animation du symbole tiré : vers l'ennemi pour une attaque, vers le
     * bouclier du joueur pour une défense, vers la pièce des gains pour un
     * gain. Rien si le symbole n'a rien fait (ennemi déjà vaincu…).
     */
    private void playSymbolStrike(SymbolOutcome outcome, Vector2 reel, float delay) {
        if (outcome.getEvents().isEmpty()) return;
        Action action = SymbolRegistry.getAction(outcome.getSymbol()).orElse(null);
        Vector2 target;
        Vector2 shield = playerShield.localToStageCoordinates(new Vector2(ShieldBadge.ICON_SIZE / 2f, ShieldBadge.ICON_SIZE / 2f));
        if (action instanceof MixedAction) { // l'Étoile se divise : ennemi, bouclier et gains
            symbolStrikes.play(outcome.getSymbol(), reel, enemyView.getCroupierCenter(), delay, shield,
                sidePanel.getCoinCenter());
            return;
        } else if (action instanceof AttackAction) {
            target = enemyView.getCroupierCenter();
        } else if (action instanceof DefenseAction) {
            target = shield;
        } else if (action instanceof HealAction) {
            target = hud.getPlayerChipCenter();
        } else if (action instanceof GainAction) {
            target = sidePanel.getCoinCenter();
        } else {
            return;
        }
        symbolStrikes.play(outcome.getSymbol(), reel, target, delay);
    }

    /** Le pistolet surgit au-dessus du symbole qu'il rejoue et tire sur l'ennemi à l'instant {@code shotAt}. */
    private void aimPistol(TurnResult result, Vector2 target, float shotAt) {
        PistolShotEvent shot = result.getPistolEvents().stream()
            .filter(e -> e instanceof PistolShotEvent).map(e -> (PistolShotEvent) e).findFirst().orElse(null);
        if (shot == null || shot.blank) return; // contre-attaque du Coffre seule, ou tir à blanc
        Vector2 from = slots.getReelCenter(shot.slotIndex >= 0 ? shot.slotIndex : 1);
        stage.addAction(Actions.delay(Math.max(0f, shotAt - PistolShotAnimation.AIM_TIME),
            Actions.run(() -> pistolAnimation.play(from, target))));
    }

    /** Affiche {@code count} rouleaux (4 avec la Machine en surchauffe) et replace le bouclier à leur droite. */
    private void showReelCount(int count) {
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
    private void playReroll() {
        Vector2 center = slots.getReelCenter(1);
        effectPopupAnimator.play(List.of(new EffectPopup(Lang.t("RELANCE !"), EffectPopup.Style.SPECIAL,
            PopupScale.MAX_INTENSITY)), center.x, center.y + SlotView.CELL_HEIGHT);
        confetti.burst(center.x, center.y, JOKER_CONFETTI);
    }

    /** Un Joker se transforme : texte « JOKER ! » et confettis sur son rouleau. */
    private void onJokerTransformed(int reel) {
        Vector2 center = slots.getReelCenter(reel);
        effectPopupAnimator.play(List.of(new EffectPopup(Lang.t("JOKER !"), EffectPopup.Style.SPECIAL,
            PopupScale.SECONDARY_INTENSITY)), center.x, center.y + SlotView.CELL_HEIGHT / 2f);
        confetti.burst(center.x, center.y, JOKER_CONFETTI);
        sounds.resultPair.play();
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

    /** Termine le tour de l'ennemi : fin du combat si un camp est vaincu, sinon au tour du joueur. */
    private void finishTurn() {
        if (isCombatOver()) {
            endCombat();
        } else if (tutorial != null) {
            tutorial.beat(TutorialDirector.Beat.ENEMY_TURN_DONE, this::startPlayerTurn);
        } else {
            startPlayerTurn();
        }
    }

    /** Le texte d'un événement du tirage vient d'apparaître : met à jour ce qui en dépend. */
    private void onEventShown(Event event) {
        if (event instanceof GainsEarnedEvent gains) {
            onGainsShown(gains.amount);
        } else if (event instanceof GainsLostEvent lost) {
            onGainsShown(-lost.amount);
        } else if (event instanceof JackpotEvent jackpot) {
            onJackpotShown(jackpot.symbol);
        } else if (event instanceof EnemyDamagedEvent hit) {
            onEnemyDefense(enemyView.onPlayerAttack(hit));
            if (hit.damage > 0) onEnemyHit(hit.damage);
        } else if (event instanceof DamageReflectedEvent reflect && reflect.damage > 0) {
            onEnemyHit(reflect.damage);
        } else if (event instanceof PlayerDamagedEvent hit) {
            if (hit.blocked > 0) onPlayerBlocked(hit);
            if (hit.damage > 0) onPlayerHit(hit.damage);
        } else if (event instanceof ShieldGainedEvent shield && shield.amount > 0) {
            playerShield.add(shield.amount);
            sounds.shieldGain.play();
        } else if (event instanceof EnemyShieldedEvent shield && shield.defense > 0) {
            sounds.shieldGain.play(0.8f, ENEMY_SHIELD_PITCH, 0f); // un ton plus bas : c'est le bouclier de l'ennemi
        } else if (event instanceof PlayerHealedEvent heal && heal.amount > 0) {
            hud.revealPlayerHeal(heal.amount);
        } else if (event instanceof EnemyHealedEvent heal) {
            if (heal.amount > 0) hud.revealEnemyHeal(heal.amount);
            enemyView.heal();
        } else if (event instanceof ReelForbiddenEvent forbidden) {
            slots.setBlockedReels(blockedReels(forbidden.reel));
            sounds.shieldBreak.play();
            screenShake.shake(0.25f, 7f);
        }
    }

    /**
     * @return les rouleaux du joueur barrés au prochain tirage : {@code forbidden}
     *         (Rouleau interdit, -1 : aucun) et ceux volés par la Machine Originelle
     */
    private java.util.Set<Integer> blockedReels(int forbidden) {
        java.util.Set<Integer> blocked = new java.util.TreeSet<>();
        if (forbidden >= 0) blocked.add(forbidden);
        int stolen = gameController.getGameState().getEnemy().getStolenReels();
        if (stolen >= 1) blocked.add(SlotMachine.SYMBOL_COUNT - 1);
        if (stolen >= 2) blocked.add(0);
        return blocked;
    }

    /** L'ennemi encaisse un coup : sa barre réagit ; un gros coup fige l'image un instant et secoue l'écran. */
    private void onEnemyHit(long damage) {
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
    private void onEnemyDefense(EnemyView.DefenseReaction reaction) {
        switch (reaction) {
            case BLOCKED -> sounds.shieldBlock.play(0.9f, ENEMY_SHIELD_PITCH, 0f);
            case BROKEN  -> shieldBroken(Lang.t("DÉFENSE BRISÉE !"), enemyView.getDefenseCenter(), ENEMY_SHIELD_PITCH);
            case PIERCED -> shieldBroken(Lang.t("DÉFENSE PERCÉE !"), enemyView.getDefenseCenter(), ENEMY_SHIELD_PITCH);
            case NONE    -> { }
        }
    }

    /** Le bouclier du joueur absorbe une attaque de l'ennemi : il encaisse, s'use, et se brise s'il n'en reste rien. */
    private void onPlayerBlocked(PlayerDamagedEvent hit) {
        if (playerShield.block(hit.shieldLeft)) {
            shieldBroken(Lang.t("BOUCLIER BRISÉ !"), playerShield.localToStageCoordinates(new Vector2(ShieldBadge.ICON_SIZE / 2f, ShieldBadge.ICON_SIZE / 2f)), 1f);
        } else {
            sounds.shieldBlock.play();
        }
    }

    /** Un bouclier se brise : fracas, petite secousse et texte {@code text} au-dessus de lui ({@code at}). */
    private void shieldBroken(String text, Vector2 at, float pitch) {
        sounds.shieldBreak.play(1f, pitch, 0f);
        screenShake.shake(0.15f, 4f);
        effectPopupAnimator.play(List.of(new EffectPopup(text, EffectPopup.Style.DAMAGE, PopupScale.SECONDARY_INTENSITY * 0.8f)),
            at.x, at.y + SHIELD_TEXT_ABOVE);
    }

    /** Le joueur est touché : sa barre réagit, un voile rouge passe sur les bords et l'écran tremble. */
    private void onPlayerHit(int damage) {
        hud.revealPlayerHit(damage);
        sounds.playerHurt.play(1f, damage >= BIG_HIT ? BIG_HIT_PITCH : 1f, 0f);
        damageVignette.flash(settings.isReducedEffects() ? 0.3f : 0.6f);
        screenShake.shake(0.3f, 9f);
    }

    /** @return la somme de {@code amount} sur les événements du tirage de type {@code type}. */
    private static <E extends Event> int sumOf(TurnResult result, Class<E> type, ToIntFunction<E> amount) {
        return result.getEvents().stream().filter(type::isInstance).map(type::cast).mapToInt(amount).sum();
    }

    /**
     * Le texte « JACKPOT ! » vient d'apparaître : bruitage du bingo, bordure
     * arc-en-ciel des rouleaux et célébration propre au {@code symbol} aligné.
     * Le tour de l'ennemi attend la fin de la célébration.
     */
    private void onJackpotShown(Symbol symbol) {
        sounds.bingo(symbol).play(BINGO_SCENE_VOLUME); // accordé à la mise en scène du symbole
        table.setReelsRainbow(true);
        table.setLightsParty(true);
        // Le son de Bingo d'origine du jeu retentit quand « BINGO! » apparaît.
        jackpotCelebration.play(symbol, () -> sounds.bingoClassic.play(), () -> {
            table.setLightsParty(false);
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
    private void playBonusGame(Runnable then) {
        BonusGame bonus = currentResult.getBonusGame();
        if (bonus == null) {
            then.run();
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
    private void explainBonusGame(BonusGameCutscene scene) {
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
            GuideOverlay.Step.say(Lang.t("Le Jeu bonus ! Après un Bingo, il s'ouvre une fois sur dix.")),
            GuideOverlay.Step.say(Lang.t("La grille tourne trois fois. Les symboles alignés se figent en doré, "
                + "les autres relancent.")),
            GuideOverlay.Step.say(Lang.t("Aligne au moins trois symboles identiques : en ligne, en colonne ou en "
                + "diagonale. Le Joker compte pour n'importe lequel.")),
            GuideOverlay.Step.say(Lang.t("Plus l'alignement est long et le symbole rare, plus tu gagnes. "
                + "Les gains tombent à la fin.")),
            GuideOverlay.Step.say(Lang.t("Comme tes symboles de gains, la grille profite de ton rang, de tes "
                + "cartes Trèfle et de tes combinaisons du tour."))), () -> {
                profile.markSeen(PlayerProfile.GUIDE_BONUS);
                scene.resumeGrid();
            });
    }

    /** @return la dette qui suit le compteur affiché : elle change quand les gains du tirage apparaissent. */
    private SpinEconomy.Debt shownDebt() {
        return SpinEconomy.debt(player().getGains() - gainsNotYetShown,
            gameController.getGameState().getEnemy().getKind().getSpinCost());
    }

    /** Le texte d'un gain (ou d'une perte) du tirage vient d'apparaître : le compteur du panneau le suit. */
    private void onGainsShown(int amount) {
        if (amount > 0) sounds.coinsGain.play();
        else if (amount < 0) sounds.coinsLoss.play();
        SpinEconomy.Debt before = shownDebt();
        gainsNotYetShown -= amount;
        refreshGains();
        if (shownDebt() != before) refreshEffects(); // Endetté ou Huissier, au rythme du compteur
    }

    /** Affiche par-dessus le jeu les cartes restant dans le deck (triées, pas dans l'ordre de pioche). */
    private void onDeckClicked() {
        pileOverlay.show(Lang.t("Deck"), player().getDeck().getCards());
    }

    /** Affiche par-dessus le jeu les cartes de la défausse (triées). */
    private void onDiscardClicked() {
        pileOverlay.show(Lang.t("Defausse"), player().getDiscardPile().getCards());
    }

    /** Échap : ouvre le menu pause, avec le bruitage du clic. */
    private void openPauseMenu() {
        sounds.buttonClick.play();
        showPauseMenu();
    }

    /** Ouvre le menu pause : le jeu se fige et le menu reçoit seul les entrées. */
    private void showPauseMenu() {
        if (pauseOverlay.isShown()) return;
        tooltip.hide();
        if (cutscene != null) cutscene.pauseSound();
        pauseOverlay.show();
        Gdx.input.setInputProcessor(pauseOverlay.getInput());
    }

    /** Effets réduits choisis dans le menu pause : une secousse en cours s'arrête. */
    private void onEffectsChanged() {
        if (settings.isReducedEffects()) screenShake.stop();
    }

    /** Fige l'animation pendant {@code duration} secondes pour marquer un gros coup (sauf effets réduits). */
    private void hitStop(float duration) {
        if (!settings.isReducedEffects()) hitStop = Math.max(hitStop, duration);
    }

    /**
     * Recommence un combat : réinitialise le GameController et tout
     * l'affichage. Dans la Tour des épreuves, le chapitre repart du premier combat.
     */
    private void onRestart() {
        if (cutscene != null) cutscene.cancel();
        music.setDucked(false);
        if (run != null) run.restart();
        gameController.restart(firstEnemy());
        if (tutorial != null) {
            TutorialRun.arrange(player().getDeck().getCards());
            TutorialRun.arrange(gameController.getGameState().getEnemy());
            tutorial.reset();
        }
        resetBoard();
    }

    /**
     * Combat suivant d'un chapitre de la Tour des épreuves, contre {@code kind} :
     * le joueur garde ses PV, ses gains et ses cartes, l'affichage repart de zéro.
     */
    private void startNextCombat(EnemyKind kind) {
        gameController.startCombat(kind);
        resetBoard();
    }

    /** Remet tout l'affichage à l'état d'un début de combat, puis lance le tour du joueur. */
    private void resetBoard() {
        stage.getRoot().clearActions(); // la suite du tour en cours (tour de l'ennemi, fin du tour) n'a plus lieu
        hand.reset();
        effectPopupAnimator.cancel(); // les gains en attente ne seront jamais affichés
        jackpotCelebration.cancel();
        turnBanner.hide();
        bingoAnimation.cancel();
        pistolAnimation.cancel();
        symbolStrikes.cancel();
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
        showReelCount(gameController.getReelCount());
        enemyView.reset();
        playerShield.setValue(0);
        pickOverlay.hide();
        chestOverlay.hide();
        refreshAll();
        refreshStageLabel();
        layout();
        endButtons.setVisible(false);
        startPlayerTurn();
    }

    /**
     * Retour au menu principal : l'écran du menu remplace celui du combat, qui
     * est libéré ensuite (hors de la boucle d'animation du Stage, qu'il ne faut
     * pas disposer pendant qu'il s'exécute).
     */
    private void onBackToMenu() {
        endButtons.setTouchable(Touchable.disabled); // un seul retour, même en cliquant plusieurs fois
        Gdx.app.postRunnable(() -> {
            luckyGame.setScreen(new MenuScreen(luckyGame));
            dispose();
        });
    }

    /** Tour des épreuves : retour à l'écran des chapitres ; Exploration : retour à la carte. */
    private void onBackToChapters() {
        endButtons.setTouchable(Touchable.disabled);
        Gdx.app.postRunnable(() -> {
            luckyGame.setScreen(isDungeon() ? new ExplorationScreen(luckyGame) : new TowerScreen(luckyGame));
            dispose();
        });
    }

    /** @return {@code true} dans un donjon de l'Exploration. */
    private boolean isDungeon() { return run instanceof DungeonRun; }

    /**
     * Donjon terminé : le coffre au trésor. Son contenu est tiré et enregistré
     * tout de suite (une carte du donjon et des pièces) ; le joueur l'ouvre d'un clic.
     */
    private void openChest() {
        DungeonRun dungeonRun = (DungeonRun) run;
        // Ouvre peut-être le lieu suivant ; le dernier donjon des Mines donne le Rouleau de la Mine.
        Symbol earnedReel = luckyGame.getProfile().clearDungeon(dungeonRun.getDungeon().name());
        chestOverlay.setEarnedReel(earnedReel);
        openChest(dungeonRun.getChestCards());
    }

    /**
     * Ouvre le coffre : une carte tirée et enregistrée tout de suite ; s'il en
     * reste {@code left - 1} (Carte au trésor), le bouton mène au coffre suivant.
     */
    private void openChest(int left) {
        DungeonRun dungeonRun = (DungeonRun) run;
        String cardId = dungeonRun.getDungeon().rollLoot(new java.util.Random());
        PlayerProfile.ChestReward reward = luckyGame.getProfile().openChest(cardId);
        chestOverlay.getActor().toFront();
        TextButton next = left > 1
            ? buttons.createAction(Lang.t("Coffre suivant"), sounds.buttonClick, () -> openChest(left - 1))
            : buttons.createAction(Lang.t("Exploration"), sounds.buttonClick, this::onBackToChapters);
        chestOverlay.show(dungeonRun.getDungeon(), reward, CardLoader.cardFactory().apply(cardId), next);
    }

    /**
     * Tour des épreuves, combat gagné : vers le choix de l'adversaire (après le
     * 1er combat) ou l'apparition du boss (après le 2e).
     */
    private void onContinue() {
        endButtons.setVisible(false);
        combatEnd.reset();
        table.setLightsParty(false);
        confetti.removeAll();
        CombatRun.Next next = run.win();
        refreshStageLabel();
        switch (next) {
            case CHOOSE_ENEMY -> pickOverlay.showChoice(run.getChoices(), this::createButton,
                index -> startNextCombat(run.choose(index)));
            case BOSS -> introduceBoss();
            case CLEARED -> {
                if (isDungeon()) openChest();
                else onBackToChapters();
            }
        }
    }

    /**
     * Après le 2e combat de la Tour ou le soldat d'un donjon, « Continuer » : la
     * cinématique du boss ou du roi joue d'abord, et sa présentation apparaît sous
     * le fondu final. « Affronter » lance ensuite le combat.
     */
    private void introduceBoss() {
        playCutscene(Cutscenes.beforeBoss(cutsceneKit, run.getEnemy()), this::showBoss);
    }

    /**
     * Joue {@code next} par-dessus tout l'écran (la musique s'efface pendant la
     * scène), puis {@code then} quand l'écran est couvert ; sans cinématique,
     * {@code then} tout de suite.
     */
    private void playCutscene(Cutscene next, Runnable then) {
        if (next == null) {
            then.run();
            return;
        }
        if (cutscene != null) {
            cutscene.remove();
            cutscene.dispose();
        }
        cutscene = next;
        stage.addActor(cutscene);
        tooltip.hide();
        music.setDucked(true);
        Cutscene playing = cutscene;
        cutscene.play(() -> {
            music.setDucked(false);
            then.run();
            playing.toFront(); // le fondu final s'estompe par-dessus l'écran suivant
        });
    }

    /** La présentation du boss, avec « Affronter ». */
    private void showBoss() {
        pickOverlay.showBoss(run.getEnemy(), this::createButton, () -> startNextCombat(run.getEnemy()));
    }

    /** @return le texte du bouton qui ramène au choix du chapitre ou du donjon. */
    private String backText() { return isDungeon() ? Lang.t("Exploration") : Lang.t("Chapitres"); }

    /** @return un bouton du casino ({@code text}), qui joue le clic et lance {@code action}. */
    private TextButton createButton(String text, Runnable action) {
        return buttons.create(text, sounds.buttonClick, action);
    }

    /**
     * Boutons de fin de combat : Recommencer et Menu principal pour un combat
     * seul ; dans la Tour des épreuves, Continuer (victoire), Réessayer
     * (défaite) ou Chapitres (chapitre terminé), et Chapitres.
     */
    private void buildEndButtons(boolean victory) {
        endButtons.clearChildren();
        List<TextButton> shown = new ArrayList<>();
        if (run == null) {
            shown.add(restartButton);
            shown.add(menuButton);
        } else if (run instanceof TutorialRun) {
            if (!victory) shown.add(restartButton);
            shown.add(menuButton);
        } else if (!victory) {
            shown.add(buttons.createAction(isDungeon() ? Lang.t("Réessayer le donjon") : Lang.t("Réessayer le chapitre"),
                sounds.buttonClick, this::onRestart));
            shown.add(buttons.create(backText(), sounds.buttonClick, this::onBackToChapters));
        } else if (run.isBossStage() && isDungeon()) {
            shown.add(buttons.createAction(Lang.t("Ouvrir le coffre !"), sounds.buttonClick, this::onContinue));
        } else if (run.isBossStage()) {
            // Boss battu : le chapitre est terminé dans ce mode, le suivant s'ouvre.
            if (run instanceof TowerRun tower) luckyGame.getProfile().clearChapter(tower.getChapter(), tower.isHard());
            shown.add(buttons.createAction(Lang.t("Chapitre terminé !"), sounds.buttonClick, this::onBackToChapters));
        } else {
            shown.add(buttons.createAction(Lang.t("Continuer"), sounds.buttonClick, this::onContinue));
            shown.add(buttons.create(backText(), sounds.buttonClick, this::onBackToChapters));
        }
        // Les gains affichés, versés en pièces pour la boutique après le roi d'un donjon.
        int earned = combatReward(victory);
        if (earned > 0) {
            Label coinsLabel = new Label(Lang.f("PIÈCES +{0}", PlayerProfile.formatCoins(earned)), new Label.LabelStyle(shopFont, Palette.GOLD));
            endButtons.add(coinsLabel).colspan(shown.size()).padBottom(14f).row();
        }
        String ending = run != null && victory && run.isBossStage() ? run.getEnding() : null;
        if (ending != null) {
            // Le dernier chapitre clôt l'histoire : sa phrase de fin, au-dessus du bouton. Il ouvre le mode difficile.
            if (run instanceof TowerRun && luckyGame.getProfile().openTowerHard()) {
                ending += "\n" + Lang.t("La Tour recommence en mode difficile.");
            }
            Label endingLabel = new Label(ending, new Label.LabelStyle(shopFont, Palette.GOLD));
            endingLabel.setAlignment(Align.center);
            endButtons.add(endingLabel).colspan(shown.size()).padBottom(18f).row();
        }
        for (int i = 0; i < shown.size(); i++) {
            TextButton button = shown.get(i);
            endButtons.add(button).size(button.getWidth(), button.getHeight()).padLeft(i == 0 ? 0f : END_BUTTONS_GAP);
        }
        endButtons.pack();
        endButtons.setPosition(playArea.getCenterX() - endButtons.getWidth() / 2f,
            playArea.getHeight() * END_BUTTONS_HEIGHT);
    }

    /** Étape de la Tour des épreuves, en haut à gauche de la zone de jeu (rien pour un combat seul). */
    private void refreshStageLabel() {
        if (run == null) {
            stageLabel.setText("");
            return;
        }
        if (run instanceof TutorialRun) {
            stageLabel.setText(run.getLabel().toUpperCase());
            stageLabel.pack();
            return;
        }
        String step = run.isBossStage() ? (isDungeon() ? Lang.t("ROI") : Lang.t("BOSS"))
            : Lang.f("COMBAT {0}/{1}", (run.getStage() + 1), run.getStageCount());
        stageLabel.setText(run.getLabel().toUpperCase() + " · " + step);
        stageLabel.pack();
    }

    // -------------------------------------------------------------------------
    // Fin de combat et sons
    // -------------------------------------------------------------------------

    /** @return les pièces gagnées à la fin de ce combat (voir {@link PlayerProfile#combatReward}). */
    private int combatReward(boolean victory) {
        if (!isDungeon()) return 0; // seule l'Exploration rapporte des pièces (ni Entraînement, ni Tour)
        return PlayerProfile.combatReward(victory, run.isBossStage(),
            gameController.getGameState().getPlayer().getGains());
    }

    /** @return l'ennemi du combat en cours de la Tour ou du donjon, ou le croupier d'entraînement. */
    private EnemyKind firstEnemy() {
        return run != null ? run.getEnemy() : EnemyKind.ENTRAINEMENT;
    }

    /** @return pourquoi {@code card} ne se joue pas maintenant (règles du combat, ou le Croupier du tutoriel), ou {@code null}. */
    private String unplayableReason(Card card) {
        String refusal = tutorial != null ? tutorial.refusal(card) : null;
        return refusal != null ? refusal : gameController.unplayableReason(card);
    }

    /** « Passer le tutoriel » : il ne sera plus proposé ; retour au menu (où le Croupier présente les modes). */
    private void skipTutorial() {
        luckyGame.getProfile().markSeen(PlayerProfile.GUIDE_TUTORIAL);
        guide.stop();
        onBackToMenu();
    }

    /** Ce que le Croupier du tutoriel montre sur l'écran de jeu, et ce qu'il y règle. */
    private final class TutorialBoard implements TutorialDirector.Board {
        @Override public Rectangle enemyBar()  { return GuideOverlay.boundsOf(hud.getEnemyHealthBar()); }
        @Override public Rectangle playerBar() { return GuideOverlay.boundsOf(hud.getPlayerHealthBar()); }
        @Override public Rectangle enemy()     { return GuideOverlay.around(enemyView.getCroupierCenter(), 260f, 260f); }
        @Override public Rectangle hand(java.util.function.Predicate<Card> which) { return GameScreen.this.hand.boundsOf(which); }
        @Override public Rectangle spinButton() { return GuideOverlay.boundsOf(spinControls.getLever()); }
        @Override public Rectangle stakeButton() { return GuideOverlay.boundsOf(spinControls.getStakeButton()); }
        @Override public Rectangle reels() {
            Vector2 first = slots.getReelCenter(0), last = slots.getReelCenter(slots.getReelCount() - 1);
            return new Rectangle(first.x - SlotView.CELL_WIDTH / 2f, first.y - SlotView.CELL_HEIGHT / 2f,
                last.x - first.x + SlotView.CELL_WIDTH, SlotView.CELL_HEIGHT);
        }
        @Override public Rectangle playerShield() { return GuideOverlay.boundsOf(GameScreen.this.playerShield); }
        @Override public Rectangle enemyDefense() {
            return GuideOverlay.boundsOf(enemyView.getDefenseBadge());
        }
        @Override public Rectangle gains()    { return GuideOverlay.boundsOf(sidePanel.getGainsBox()); }
        @Override public Rectangle combos()   { return GuideOverlay.boundsOf(sidePanel.getCombosBox()); }
        @Override public Rectangle effects()  { return GuideOverlay.boundsOf(sidePanel.getEffectsBox()); }
        @Override public Rectangle shopIcon() { return GuideOverlay.boundsOf(GameScreen.this.shopIcon); }
        @Override public boolean shopShown()      { return shopOverlay.isShown(); }
        @Override public boolean enemyDefeated()  { return gameController.getGameState().getEnemy().isDefeated(); }
        @Override public boolean lastSpinWasBingo() { return lastSpinBingo; }
        @Override public void rig(Symbol[] symbols) { gameController.rigSpin(symbols); }
        @Override public void rigJackpot(Symbol symbol) { gameController.rigJackpot(symbol); }
        @Override public void refreshHand() { GameScreen.this.hand.refreshBlocked(); }
    }

    /** Le combat est terminé dès que le joueur ou l'ennemi n'a plus de points de vie. */
    private boolean isCombatOver() {
        GameState gameState = gameController.getGameState();
        return gameState.getEnemy().isDefeated() || gameState.getPlayer().isDefeated();
    }

    /**
     * Fige la partie (plus de pioche ni de spin), met en scène la victoire ou la
     * défaite, puis affiche les boutons pour recommencer ou revenir au menu principal.
     */
    private void endCombat() {
        setSpinDisabled(true);
        luckyGame.getProfile().addCoins(combatReward(gameController.getGameState().getEnemy().isDefeated()));
        buildEndButtons(gameController.getGameState().getEnemy().isDefeated());
        Runnable showRestart = () -> {
            endButtons.setTouchable(Touchable.childrenOnly);
            endButtons.setVisible(true);
            endButtons.getColor().a = 0f;
            endButtons.addAction(Actions.fadeIn(END_BUTTONS_FADE));
            endButtons.toFront();
        };
        if (gameController.getGameState().getEnemy().isDefeated()) {
            table.setLightsParty(true);
            HealthBarView enemyBar = hud.getEnemyHealthBar();
            enemyBar.hit(Color.WHITE, 0.4f);
            enemyBar.addAction(Actions.fadeOut(0.5f)); // l'ennemi part en jetons
            enemyView.defeat();
            // Fin de l'histoire (chapitres 3 et 6) : sa cinématique, puis la phrase de fin et les boutons.
            Cutscene ending = run instanceof TowerRun tower && run.isBossStage()
                ? Cutscenes.ending(cutsceneKit, tower.getChapter()) : null;
            Runnable afterVictory = ending == null ? showRestart : () -> playCutscene(ending, showRestart);
            if (tutorial != null) { // tutoriel réussi : le Croupier conclut, puis le menu
                luckyGame.getProfile().markSeen(PlayerProfile.GUIDE_TUTORIAL);
                afterVictory = () -> tutorial.beat(TutorialDirector.Beat.VICTORY, showRestart);
            }
            combatEnd.playVictory(hud.getEnemyChipCenter(), afterVictory);
            sounds.victory.play();
        } else {
            combatEnd.playDefeat(showRestart);
            sounds.defeat.play();
        }
    }

    /**
     * Joue le bruitage correspondant au tirage : paire (2 identiques) ou aucun.
     * Celui du jackpot (bingo) accompagne sa célébration (voir onJackpotShown).
     */
    private void playSymbolResultSound(TurnResult result) {
        if (result.isPair()) {
            sounds.resultPair.play();
        } else if (!result.isJackpot()) {
            sounds.resultNone.play();
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
        refreshPlays();
        piles.refresh(player());
    }

    /**
     * Met à jour le compteur de cartes jouées ce tour (en rouge une fois la limite
     * atteinte), suivi du coût du tirage et de la Mise ; et le bouton de Mise.
     */
    private void refreshPlays() {
        StringBuilder text = new StringBuilder(Lang.f("Cartes {0}/{1}",
            gameController.getCardsPlayedThisTurn(), gameController.getPlayLimit()));
        if (gameController.getSpinCost() > 0) {
            text.append("   ").append(Lang.f("Tirage -{0}", SidePanel.formatGains(gameController.getSpinCost())));
        }
        if (gameController.getStake() != SpinEconomy.Stake.NONE) {
            text.append("   ").append(Lang.f("Mise -{0}", SidePanel.formatGains(gameController.getStakeAmount())));
        }
        spinControls.showStake(gameController.getStake());
        playsLabel.setText(text);
        playsLabel.setColor(gameController.isPlayLimitReached() ? Palette.TEXT_ALERT : Palette.TEXT_TITLE);
        playsLabel.pack();
    }

    /**
     * Met à jour les effets actifs du panneau latéral (voir {@link CombatEffectRows}).
     * Chaque effet a son nom et sa description, pour son infobulle et sa fiche.
     */
    private void refreshEffects() {
        sidePanel.setActiveEffects(new CombatEffectRows(gameController, hudTextures, slots::regionOf, shownDebt()).rows());
    }

    /** Met à jour le compteur de gains, sans les gains du tirage dont le texte n'est pas encore apparu. */
    private void refreshGains() {
        sidePanel.setGains(player().getGains() - gainsNotYetShown);
    }

    /** Place le bouclier du joueur à droite de ses rouleaux, et son levier à leur gauche. */
    private void placePlayerShield() {
        playerShield.setPosition(table.getPlayerReelsRight() + PLAYER_SHIELD_GAP,
            table.getReelRowY() + (SlotView.CELL_HEIGHT - playerShield.getHeight()) / 2f);
        spinControls.placeLever();
    }

    /** Place (ou replace après un redimensionnement) les éléments qui dépendent de la taille de l'écran. */
    private void layout() {
        table.layout();
        enemyView.layout();
        placePlayerShield();
        pauseButton.setPosition(playArea.getX() + playArea.getWidth() - pauseButton.getWidth() - BUTTON_MARGIN,
            BUTTON_MARGIN);
        spinControls.placeStake();
        playsLabel.pack();
        playsLabel.setPosition(playArea.getX() + BUTTON_MARGIN,
            BUTTON_MARGIN + (pauseButton.getHeight() - playsLabel.getHeight()) / 2f);
        sidePanel.layout(stage);
        hud.layout();
        piles.layout(table.getDeckX(), table.getPilesY());
        slots.layout();
        hand.layout(false);
        endButtons.setPosition(playArea.getCenterX() - endButtons.getWidth() / 2f,
            playArea.getHeight() * END_BUTTONS_HEIGHT);
        shopIcon.setPosition(playArea.getX() + playArea.getWidth() - shopIcon.getWidth() - SHOP_ICON_MARGIN,
            playArea.getHeight() - shopIcon.getHeight() - SHOP_ICON_MARGIN);
        if (stageLabel != null) {
            stageLabel.pack();
            stageLabel.setPosition(playArea.getX() + STAGE_LABEL_MARGIN,
                playArea.getHeight() - stageLabel.getHeight() - STAGE_LABEL_MARGIN);
        }
        if (guide != null) { // « Passer le tutoriel » sous « TUTORIEL », loin de l'échoppe
            guide.setSkipCorner(playArea.getX() + STAGE_LABEL_MARGIN,
                playArea.getHeight() - STAGE_LABEL_MARGIN - stageLabel.getHeight() - STAGE_LABEL_MARGIN / 2f);
        }
    }

    // -------------------------------------------------------------------------
    // Cycle de vie ScreenAdapter
    // -------------------------------------------------------------------------

    /**
     * Installe le processeur d'entrée de l'écran : le Stage (clics, survols)
     * en priorité, puis les raccourcis clavier : F lance la machine si
     * possible ; Échap ferme la fenêtre ouverte (carte, échoppe, pile) ou, s'il
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
                if (keycode == Input.Keys.ESCAPE) {
                    openPauseMenu();
                    return true;
                }
                if (cutscene != null && cutscene.isPlaying()) {
                    cutscene.skip(); // n'importe quelle autre touche passe la cinématique
                    return true;
                }
                if (choiceOverlay.isShown() || shopOverlay.isShown() || pickOverlay.isShown() || chestOverlay.isShown()
                    || bingoAnimation.isPlaying()
                    || rainbowAnimation.isPlaying()) {
                    return true; // un choix ou une animation de carte est en cours
                }
                if (keycode == Input.Keys.F && !spinControls.isDisabled()) {
                    onSpin();
                    return true;
                }
                return false;
            }
        };
        gameInput = new InputMultiplexer(stage, keyboardInput);
        Gdx.input.setInputProcessor(pauseOverlay.isShown() ? pauseOverlay.getInput() : gameInput);
        music.play();
    }

    /** Met à jour le viewport puis repositionne les éléments qui dépendent de la taille de l'écran. */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        layout();
        choiceOverlay.layout();
        pickOverlay.layout();
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
            hitStop -= delta; // micro-arrêt : l'image reste figée un instant sur un gros coup
        } else {
            stage.act(delta);
        }
        spinControls.followShield();
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
        cardTextures.dispose();
        buttons.dispose();
        tooltip.dispose();
        enemyView.dispose();
        enemyTextures.dispose();
        hud.dispose();
        sidePanel.dispose();
        hudTextures.dispose();
        symbolStrikes.dispose();
        slots.dispose();
        sounds.dispose();
        music.dispose();
        cardClickParticles.dispose();
        pileOverlay.dispose();
        choiceOverlay.dispose();
        pickOverlay.dispose();
        chestOverlay.dispose();
        bingoAnimation.dispose();
        rainbowAnimation.dispose();
        if (cutscene != null) cutscene.dispose();
        if (guide != null) guide.dispose();
        if (bonusGuide != null) bonusGuide.dispose();
        shopOverlay.dispose();
        cardDetail.dispose();
        pauseOverlay.dispose();
        jackpotCelebration.dispose();
        turnBanner.dispose();
        damageVignette.dispose();
        combatEnd.dispose();
        effectPopupAnimator.dispose();
    }
}
