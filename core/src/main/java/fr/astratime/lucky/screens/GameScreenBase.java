package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import fr.astratime.lucky.controllers.PreparationResolver;
import fr.astratime.lucky.entities.enemy.AspirationRule;
import fr.astratime.lucky.views.AspirationPanel;
import fr.astratime.lucky.views.SpeechBubble;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.animations.BingoCardAnimation;
import fr.astratime.lucky.animations.CardClickParticles;
import fr.astratime.lucky.animations.CardDealAnimator;
import fr.astratime.lucky.animations.CardDiscardAnimator;
import fr.astratime.lucky.animations.cutscenes.CroupierCutscene;
import fr.astratime.lucky.animations.cutscenes.Cutscene;
import fr.astratime.lucky.animations.cutscenes.CutsceneKit;
import fr.astratime.lucky.animations.cutscenes.Cutscenes;
import fr.astratime.lucky.animations.CombatEndAnimation;
import fr.astratime.lucky.animations.Confetti;
import fr.astratime.lucky.animations.DamageVignette;
import fr.astratime.lucky.animations.EffectPopupAnimator;
import fr.astratime.lucky.animations.Fireworks;
import fr.astratime.lucky.animations.JackpotCelebration;
import fr.astratime.lucky.animations.TurnBanner;
import fr.astratime.lucky.animations.PistolShotAnimation;
import fr.astratime.lucky.animations.FullPotentialAnimation;
import fr.astratime.lucky.animations.SymbolStrikes;
import fr.astratime.lucky.animations.RainbowChipsAnimation;
import fr.astratime.lucky.animations.ScreenShake;
import fr.astratime.lucky.assets.BackgroundMusic;
import fr.astratime.lucky.assets.CombatMusic;
import fr.astratime.lucky.assets.CardTextures;
import fr.astratime.lucky.assets.EnemyTextures;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.GameSounds;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.assets.TableTextures;
import fr.astratime.lucky.progress.CombatLog;
import fr.astratime.lucky.progress.Cosmetic;
import fr.astratime.lucky.controllers.GameController;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.CombatJournal;
import fr.astratime.lucky.entities.CombatRecap;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.SpinEconomy;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.TurnResult;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.tower.CroupierRun;
import fr.astratime.lucky.entities.tower.EndlessRun;
import fr.astratime.lucky.entities.tower.Chapter;
import fr.astratime.lucky.entities.tower.TowerRun;
import fr.astratime.lucky.entities.tutorial.TutorialRun;
import fr.astratime.lucky.entities.RankBonus;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.exploration.DungeonRun;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.entities.run.CombatRun;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.progress.PlayerProfile;
import fr.astratime.lucky.views.AchievementToast;
import fr.astratime.lucky.loaders.CardLoader;
import fr.astratime.lucky.settings.AudioSettings;
import fr.astratime.lucky.settings.VisualSettings;
import fr.astratime.lucky.views.CombatJournalView;
import fr.astratime.lucky.views.CardChoiceOverlay;
import fr.astratime.lucky.views.ChestOverlay;
import fr.astratime.lucky.views.CardDetailOverlay;
import fr.astratime.lucky.views.CardImage;
import fr.astratime.lucky.views.CardPileView;
import fr.astratime.lucky.views.CasinoButtons;
import fr.astratime.lucky.views.CombatHud;
import fr.astratime.lucky.views.CrossroadsOverlay;
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
import fr.astratime.lucky.views.TableProjection;
import fr.astratime.lucky.views.ProjectedLayer;
import fr.astratime.lucky.views.FrontScene;
import fr.astratime.lucky.views.Billboard;
import fr.astratime.lucky.views.RisingMachine;
import fr.astratime.lucky.views.SolidPainter;
import fr.astratime.lucky.views.TableProps;
import fr.astratime.lucky.views.Tooltip;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

/**
 * Base de l'écran de combat ({@link GameScreen}) : ses constantes, ses vues et son état, construits une fois ici,
 * et les petites mises à jour de l'affichage (panneaux, placement). Les sous-classes n'ajoutent aucun champ :
 * le constructeur peut donc appeler leurs méthodes sans risque.
 */
abstract class GameScreenBase extends ScreenAdapter {

    // -------------------------------------------------------------------------
    // Constantes d'affichage
    // -------------------------------------------------------------------------

    /** Taille minimale de l'écran de jeu : dans une fenêtre plus petite, il est réduit (voir {@link MinimumScreenViewport}). */
    /** Relance : pause entre l'arrêt du premier tirage et le second. */
    static final float REROLL_PAUSE = 0.45f;
    /** Chant de l'ennemi : pause entre la fin de la distribution et la carte jouée d'office. */
    static final float SONG_DELAY = 0.6f;
    static final float  MIN_WIDTH      = 1600f;
    static final float  MIN_HEIGHT     = 1080f;  // les deux côtés de la table, à la même taille
    static final float  CARD_WIDTH     = 95f;
    static final float  CARD_HEIGHT    = 135f;
    static final String CARD_BACK_PATH = "cards/light/BACK.png";

    static final float BUTTON_MARGIN = 20f;   // boutons en bas à gauche, sous la table

    /** Dégâts à partir desquels un coup sur l'ennemi fige l'image un instant (micro-arrêt). */
    static final int   BIG_HIT               = 60;
    /** Un gros coup sonne plus grave. */
    static final float BIG_HIT_PITCH         = 0.85f;
    /** Bouclier de l'ennemi : le son du bouclier, un ton plus bas. */
    static final float ENEMY_SHIELD_PITCH    = 0.84f;
    static final float PLAYER_SHIELD_GAP     = 40f;   // entre ses rouleaux et son bouclier, comme l'ennemi
    static final float SHIELD_TEXT_ABOVE     = 40f;   // texte d'un bouclier brisé, au-dessus de lui
    /** Volume du son de scène d'un Bingo, sous le son de Bingo d'origine joué en même temps. */
    static final float BINGO_SCENE_VOLUME    = 0.8f;
    /** Volume de la musique du combat quand le réglage « Musique » est à 100 %. */
    static final float COMBAT_MUSIC_LEVEL    = 0.15f;
    static final float BIG_HIT_STOP          = 0.09f;
    /** Paire : clignotement doré des deux rouleaux et confettis lâchés par chacun. */
    static final Color PAIR_GLOW             = Palette.GOLD;
    static final float PAIR_GLOW_DURATION    = 1.6f;
    static final int   PAIR_CONFETTI         = 45;
    /** Temps laissé au texte de la riposte, après les autres textes du tirage, avant d'annoncer la fin du combat. */
    static final float RESULT_TEXTS_MARGIN   = 0.4f;
    /** Joker transformé : gerbe de confettis violets sur son rouleau. */
    static final int   JOKER_CONFETTI        = 30;
    /** Textes du tir du pistolet : sous la barre de vie de l'ennemi. */
    static final float PISTOL_TEXT_BELOW     = 170f;
    /** Carte Bingo : hauteur (fraction de l'écran) où elle s'élève pour lancer ses faisceaux. */
    static final float BINGO_CARD_HEIGHT     = 0.58f;
    /** Boutons de fin de combat : écart entre eux, hauteur (fraction de l'écran) et apparition en fondu. */
    static final float END_BUTTONS_GAP    = 30f;
    static final float END_BUTTONS_HEIGHT = 0.46f;  // sous l'annonce, au-dessus de la rangée de cartes
    static final float END_BUTTONS_FADE   = 0.4f;
    /** Écart entre les boutons de fin et le récapitulatif du combat, en dessous. */
    static final float RECAP_GAP          = 18f;
    /** « Passer le tutoriel » : écart à droite de la barre de vie de l'ennemi. */
    static final float SKIP_BUTTON_GAP = 30f;
    /** Carte achetée : posée une fois l'échoppe refermée. */
    static final float PURCHASE_DELAY   = 0.9f;
    /** Bouton pause : écart au cadre des gains, à gauche (il est centré en hauteur sur la barre de vie de l'ennemi). */
    static final float PAUSE_BUTTON_MARGIN = 30f;
    /** Entre le bouton Pause et le bouton Journal, et entre ce dernier et son panneau. */
    static final float JOURNAL_BUTTON_GAP = 12f;
    /** Étape du donjon ou de la Tour : marge en haut à gauche (passer le tutoriel) et en bas à droite (l'étape). */
    static final float STAGE_LABEL_MARGIN = 30f;
    /** Coffre au trésor ouvert : gerbe de confettis. */
    static final int   CHEST_CONFETTI        = 70;
    /** Pot de Lutin qui apparaît : gerbe de confettis. */
    static final int   POT_CONFETTI          = 50;
    /** Temps laissé au texte de la riposte (et au coup sur la barre) avant de passer au tour du joueur. */
    static final float RIPOSTE_TEXT_TIME     = 0.8f;
    static final float END_TEXTS_ABOVE       = 0.6f;  // annonces de fin de tour : au-dessus du filet, en cartes
    /** Le temps de lire « ELLE RÉSISTE ! DERNIER TIRAGE » avant le duel des leviers. */
    static final float LAST_DRAW_DELAY       = 1.2f;
    /** Vue de face : inclinaison des éléments debout sur la table (hauteur / largeur ; 1 : bien droit). */
    static final float CARD_TILT  = 0.92f;
    static final float BADGE_TILT = 0.9f;
    /** Vue de face : les symboles des rouleaux, au repos, un peu redressés vers le joueur. */
    static final float REEL_TILT  = 0.8f;
    /** Combinaison formée : hauteur de son texte au-dessus de la main (en hauteurs de carte). */
    static final float COMBO_TEXT_HEIGHT     = 2f;

    // -------------------------------------------------------------------------
    // Contrôleur — seul point d'accès à la logique de jeu
    // -------------------------------------------------------------------------

    final GameController gameController;

    // -------------------------------------------------------------------------
    // Ressources (à disposer dans dispose())
    // -------------------------------------------------------------------------

    final LuckyGame           luckyGame;
    /** Chapitre de la Tour des épreuves ou donjon de l'Exploration, ou {@code null} pour un combat seul (« Entraînement »). */
    final CombatRun           run;
    /**
     * Adversaire d'un combat seul : le croupier d'entraînement, ou un ennemi déjà vaincu
     * choisi pour l'entraînement libre (ignoré dans la Tour, l'Exploration et le tutoriel).
     */
    final EnemyKind           trainingEnemy;
    final Stage               stage;
    final BitmapFont          font;
    /** Libellés du deck et de la défausse : police du jeu, à balises de couleur (nom crème, nombre doré). */
    final BitmapFont          pileFont = Fonts.jerseyMarkup(20, Color.WHITE, 2f, Palette.TEXT_SHADE);
    final BitmapFont          shopFont = Fonts.jersey(30, Palette.GOLD, 2f, Palette.TEXT_SHADE);
    final BitmapFont          playsFont = Fonts.jersey(24, Color.WHITE, 2f, Palette.TEXT_SHADE);
    final BitmapFont          shieldFont = Fonts.jersey(26, Color.WHITE, 2f, Palette.TEXT_SHADE);
    final Texture             cardBackTexture;
    /** Image du dos des cartes du joueur (cosmétique « Dos de cartes », ou celui d'origine). */
    final String              playerBackPath;
    /** Communs à tous les écrans (voir {@link SharedAssets}) : ne jamais les libérer ici. */
    final CardTextures        cardTextures;
    final EnemyTextures       enemyTextures;
    final CasinoButtons       buttons;
    final HudTextures         hudTextures;
    final VisualSettings      settings      = new VisualSettings();
    final ScreenShake         screenShake   = new ScreenShake(settings);
    CutsceneKit               cutsceneKit;
    /** La cinématique en cours ou la dernière jouée ({@code null} avant la première). */
    Cutscene                  cutscene;
    final DamageVignette      damageVignette = new DamageVignette();
    final Confetti            confetti;
    final CombatEndAnimation  combatEnd;
    final TableTextures       tableTextures;
    final AudioSettings       audio         = new AudioSettings();
    final GameSounds          sounds        = new GameSounds(audio);
    /** Musique du combat (une par lieu, voir {@link CombatMusic}), gardée basse pour ne pas couvrir les bruitages. */
    final BackgroundMusic     music         = new BackgroundMusic(CombatMusic.DEFAULT, audio, COMBAT_MUSIC_LEVEL);
    final CardClickParticles  cardClickParticles = new CardClickParticles();
    final EffectPopupAnimator effectPopupAnimator;
    /**
     * Fête du Bingo, préparée au premier lancer du combat seulement (voir
     * {@link #jackpotCelebration()}) : ses nombreuses textures ne retardent plus l'ouverture
     * du combat. Elle prend place dans {@link #jackpotLayer}, à son rang dans le Stage.
     */
    JackpotCelebration        jackpotCelebration;
    final Group               jackpotLayer = new Group();
    Fireworks.Sounds          jackpotFireworkSounds;
    /** Annonce des succès atteints pendant le combat. */
    final AchievementToast    achievementToast;
    /** Annonces « À TOI DE JOUER ! » et « TOUR ENNEMI ». */
    final TurnBanner          turnBanner = new TurnBanner();
    final Tooltip             tooltip;
    final PileContentOverlay  pileOverlay;
    final CardChoiceOverlay   choiceOverlay;
    final BingoCardAnimation  bingoAnimation;
    final PistolShotAnimation pistolAnimation;
    /** As joué à plein potentiel : petite animation au milieu de l'écran. */
    final FullPotentialAnimation fullPotentialAnimation = new FullPotentialAnimation();
    final SymbolStrikes       symbolStrikes;
    final RainbowChipsAnimation rainbowAnimation;
    final ShopOverlay         shopOverlay;
    final CardDetailOverlay   cardDetail;
    /** Menu pause (Échap ou bouton pause) : options, recommencer, menu principal ; le jeu est figé tant qu'il est ouvert. */
    final PauseOverlay        pauseOverlay;
    /** Icône de l'échoppe (en haut à droite de la zone de jeu), qui ouvre la boutique. */
    final ShopIcon            shopIcon;
    /** Transitions de la Tour des épreuves : choix de l'adversaire, apparition du boss. */
    final EnemyPickOverlay    pickOverlay;
    /** Exploration : le carrefour entre le soldat et le roi. */
    final CrossroadsOverlay   crossroadsOverlay;
    /** Exploration : le coffre au trésor, au bout d'un donjon. */
    final ChestOverlay        chestOverlay;
    /** Étape de la Tour des épreuves (« CHAPITRE 1 · COMBAT 2/3 »), vide pour un combat seul. */
    final Label               stageLabel;
    /** Tutoriel : le Croupier, sa bulle et son voile, et ce qu'il fait faire ({@code null} hors du tutoriel). */
    final GuideOverlay        guide;
    final TutorialDirector    tutorial;
    /** Chiffres du combat en cours, montrés au-dessus des boutons de fin. */
    final CombatRecap         recap = new CombatRecap();
    /** Journal des derniers tours, dans un panneau repliable sous le bouton « Journal » (touche J). */
    final CombatJournal       journal = new CombatJournal();
    final CombatJournalView   journalView;
    /** Tutoriel : le dernier tirage était un Bingo. */
    boolean                   lastSpinBingo;
    /** Le Croupier qui explique le Jeu bonus la première fois qu'il s'ouvre ({@code null} avant). */
    GuideOverlay              bonusGuide;

    // -------------------------------------------------------------------------
    // Vues et acteurs Scene2D
    // -------------------------------------------------------------------------

    final PlayArea   playArea;
    final TableView  table;
    /**
     * Vue de face (Options > Visuel) : la table vue en perspective, l'ennemi en grand
     * derrière ({@link #frontScene}). Tout ce qui est posé sur la table est dans
     * {@link #tableLayer}, qui se dessine en perspective ; le reste reste à plat.
     */
    final TableProjection projection = new TableProjection();
    /** Bulle des réactions de l'ennemi (rois et boss : Bingo du joueur, phase, joueur presque vaincu). */
    final SpeechBubble    speech;
    /** Bingos du joueur déjà commentés par l'ennemi dans ce combat : ses deux phrases alternent. */
    int                   bingoLines;
    /** L'ennemi a déjà dit sa phrase « tu es presque vaincu » dans ce combat (une seule fois). */
    boolean               lowHpLineSaid;
    final ProjectedLayer  tableLayer;
    final FrontScene      frontScene;
    /**
     * Vue de face : ce qui se tient debout sur la table, face au joueur, rangée par
     * rangée (du fond vers nous) ; en vue classique, ces groupes ne changent rien.
     */
    final Billboard enemyReelsStand, enemyStand, shopStand, slotsStand, reelsStand, handStand;
    /** Vue de face : les volumes posés sur la table (decks, défausses, échoppe). */
    final SolidPainter  solids;
    final TableProps    props;
    /** Vue de face : les machines à sous qui sortent de la table au tirage. */
    final RisingMachine playerMachine, enemyMachine;
    final SidePanel  sidePanel;
    final CombatHud  hud;
    /** Côté de l'ennemi : le croupier, ses rouleaux, ses cartes et ses piles ; joue son tour en animation. */
    final EnemyView  enemyView;
    /** Bouclier du joueur, à droite de ses rouleaux (comme la défense de l'ennemi en face). */
    final ShieldBadge playerShield;
    final PilesView  piles;
    final HandView   hand;
    final SlotView   slots;
    /** Calque des textes animés (bonus des cartes, résultats du tirage) : au-dessus du jeu, sous le voile des piles. */
    final Group      popupLayer = new Group();
    /** Le levier, à gauche des rouleaux du joueur, et la Mise, à droite de son bouclier. */
    final SpinControls spinControls;
    /** Cartes jouées ce tour sur la limite (« Cartes 2/4 »), coût du tirage et Mise, à droite du bouton de Mise. */
    final Label      playsLabel;
    /** Panneau de la Machine Originelle : les règles de ses Aspirations, au-dessus de la main. */
    final AspirationPanel aspirationPanel;
    final TextButton restartButton;
    final TextButton menuButton;
    /** Bouton pause, en haut entre le cadre des gains et la barre de vie de l'ennemi : ouvre le même menu qu'Échap. */
    final TextButton pauseButton;
    final TextButton journalButton;
    /** Boutons de fin de combat (Recommencer, Menu principal), centrés sous l'annonce de victoire ou de défaite. */
    final Table      endButtons = new Table();

    /** Entrées du jeu (Stage puis raccourcis clavier), rendues à la fermeture du menu pause. */
    InputProcessor gameInput;

    /** Temps restant du micro-arrêt en cours (voir {@link #hitStop(float)}). */
    float hitStop = 0f;

    /**
     * Gains du dernier tirage dont le texte n'est pas encore apparu : le compteur
     * du panneau ne les ajoute qu'à l'apparition de leur texte « GAINS + ».
     */
    double gainsNotYetShown = 0;

    /** Durée de l'annonce « TOUR ENNEMI » du tirage en cours (0 si l'ennemi, vaincu, ne riposte pas). */
    TurnResult currentResult;

    /** Tirage des ennemis dorés (un soldat d'Exploration sur {@value DungeonRun#GOLDEN_CHANCE}). */
    final Random goldenRandom = new Random();
    /** {@code true} si ce combat écrit le journal détaillé (ni ADMIN, ni tutoriel). */
    boolean combatLogOn;


    GameScreenBase(LuckyGame luckyGame, CombatRun run, EnemyKind trainingEnemy) {
        this.luckyGame     = luckyGame;
        this.run           = run;
        this.trainingEnemy = trainingEnemy == null ? EnemyKind.ENTRAINEMENT : trainingEnemy;
        // La table a l'air du lieu en Exploration (et contre un ennemi de l'Exploration à l'entraînement) ;
        // ailleurs, la table de casino. Les cosmétiques choisis (tapis, machine) habillent la table de chaque lieu.
        PlayerProfile profile = luckyGame.getProfile();
        tableTextures  = TableTextures.forPlace(run instanceof DungeonRun dungeonRun ? Place.of(dungeonRun.getDungeon())
            : run == null ? Place.of(this.trainingEnemy) : null,
            profile.getCosmetic(Cosmetic.Kind.FELT), profile.getCosmetic(Cosmetic.Kind.MACHINE));
        String backPath = profile.getCosmetic(Cosmetic.Kind.BACK).getBackPath();
        playerBackPath = backPath != null ? backPath : CARD_BACK_PATH;
        SharedAssets assets = luckyGame.getAssets();
        cardTextures  = assets.cardTextures();
        enemyTextures = assets.enemyTextures();
        buttons       = assets.buttons;
        hudTextures   = assets.hud;
        this.gameController = createController(luckyGame.getProfile(), run);
        // Journal détaillé des combats : ni ADMIN, ni tutoriel
        combatLogOn = !(run instanceof TutorialRun) && !luckyGame.getProfile().isAdmin();
        if (combatLogOn) gameController.setCombatLog(line -> CombatLog.append(CombatLog.DEFAULT_FILE, line));
        startRun(luckyGame.getProfile());
        // Le SpriteBatch est partagé avec LuckyGame et ne doit PAS être disposé ici.
        this.stage = new Stage(new MinimumScreenViewport(MIN_WIDTH, MIN_HEIGHT), luckyGame.getBatch());

        font                = new BitmapFont();
        cardBackTexture     = cardTextures.get(playerBackPath);
        effectPopupAnimator = new EffectPopupAnimator(popupLayer, effect -> sounds.effect(effect).play());
        tooltip             = new Tooltip(hudTextures);
        pileOverlay         = new PileContentOverlay(stage, font, tooltip, cardTextures::get, CARD_WIDTH, CARD_HEIGHT);

        playArea   = new PlayArea(stage, SidePanel.WIDTH);
        confetti   = new Confetti(new TextureRegion(hudTextures.pixel), sounds.confettiPop, sounds.confettiRain);
        Fireworks.Sounds fireworkSounds = new Fireworks.Sounds(sounds.fireworkLaunch, sounds.fireworkBurst);
        table      = new TableView(playArea, tableTextures, CARD_WIDTH, CARD_HEIGHT);
        tableLayer = new ProjectedLayer(projection, table::drawOpaqueMask);
        enemyReelsStand = new Billboard(projection, BADGE_TILT,
            () -> new Vector2(playArea.getCenterX(), table.getEnemyReelWindow().y));
        enemyStand      = new Billboard(projection, CARD_TILT, () -> new Vector2(playArea.getCenterX(), table.getEnemyCardRowY()));
        slotsStand      = new Billboard(projection, REEL_TILT, () -> {
            Rectangle window = table.getPlayerReelWindow();
            return new Vector2(window.x + window.width / 2f, window.y);
        });
        reelsStand      = new Billboard(projection, BADGE_TILT, () -> new Vector2(playArea.getCenterX(), table.getReelRowY()));
        handStand       = new Billboard(projection, CARD_TILT, () -> new Vector2(playArea.getCenterX(), table.getHandRowY()));
        solids          = new SolidPainter(projection, hudTextures.pixel);
        playerMachine   = new RisingMachine(solids, table::getPlayerReelWindow, table::getPlayerReelCount,
            tableTextures.reelFrameDrawable(), tableTextures.reelCellDrawable(), false);
        enemyMachine    = new RisingMachine(solids, table::getEnemyReelWindow, table::getEnemyReelCount,
            tableTextures.reelFrameDrawable(), tableTextures.reelCellDrawable(), true);
        enemyView  = new EnemyView(table, enemyTextures, pileFont, tooltip, effectPopupAnimator,
            sounds.cardDeal, sounds.cardFlip, sounds.reelSpin, sounds.reelStop,
            () -> gameController.getGameState().getEnemy(), hudTextures.pixel);
        speech = new SpeechBubble(hudTextures.pixel);
        playerShield = new ShieldBadge(Lang.t("Bouclier"), 0, enemyTextures.symbol(EnemySymbol.SHIELD), shieldFont,
            hudTextures.pixel);
        addShieldTooltip();
        sidePanel  = new SidePanel(hudTextures);
        achievementToast = new AchievementToast(hudTextures, sounds.achievement);
        jackpotFireworkSounds = fireworkSounds;
        jackpotLayer.setTouchable(Touchable.childrenOnly);
        hud        = new CombatHud(playArea, hudTextures, gameController::getGameState);

        spinControls  = new SpinControls(table, playerShield, hudTextures, playsFont, shieldFont, tooltip,
            sounds.spinButton, sounds.buttonClick, this::onSpin, this::onStake);
        restartButton = buttons.createAction(Lang.t("Recommencer"), sounds.buttonClick, this::onRestart);
        menuButton    = buttons.createAction(Lang.t("Menu principal"), sounds.buttonClick, this::onBackToMenu);
        pauseButton   = buttons.create(Lang.t("Pause"), sounds.buttonClick, this::showPauseMenu);
        journalButton = buttons.create(Lang.t("Journal"), sounds.buttonClick, this::toggleJournal);
        journalView   = new CombatJournalView(hudTextures);
        journalView.setEntries(List.of());
        playsLabel    = new Label("", new Label.LabelStyle(playsFont, Color.WHITE));
        aspirationPanel = new AspirationPanel(hudTextures);
        endButtons.add(restartButton).size(restartButton.getWidth(), restartButton.getHeight());
        endButtons.add(menuButton).size(menuButton.getWidth(), menuButton.getHeight()).padLeft(END_BUTTONS_GAP);
        endButtons.pack();
        endButtons.setVisible(false);

        piles = new PilesView(playArea, cardBackTexture, pileFont, tooltip, sounds.buttonClick, sounds.pileHover,
            CARD_WIDTH, CARD_HEIGHT, this::onDeckClicked, this::onDiscardClicked);
        hand  = new HandView(table, CARD_WIDTH, CARD_HEIGHT, cardTextures, tooltip, sounds.cardHover, piles, this::player,
            new CardDealAnimator(handStand, cardBackTexture, CARD_WIDTH, CARD_HEIGHT, sounds.cardDeal, sounds.cardFlip),
            new CardDiscardAnimator(handStand, cardBackTexture, CARD_WIDTH, CARD_HEIGHT, sounds.cardDeal, sounds.cardFlip),
            this::onCardPlayed);
        hand.setBackPath(playerBackPath);
        slots = new SlotView(table, tooltip, effectPopupAnimator, sounds.reelSpin, sounds.reelStop, sounds.reelSuspense,
            gameController.getGameState().getPlayer().getSlotMachine().getReels());
        combatEnd = new CombatEndAnimation(playArea, screenShake, new TextureRegion(hudTextures.pixel),
            hudTextures.bannerBand, new TextureRegion(cardBackTexture), confetti, fireworkSounds, CARD_WIDTH, CARD_HEIGHT);
        choiceOverlay   = new CardChoiceOverlay(stage, hudTextures, cardBackTexture, cardTextures, CARD_WIDTH, CARD_HEIGHT);
        bingoAnimation  = new BingoCardAnimation(settings, new TextureRegion(hudTextures.pixel));
        pistolAnimation = new PistolShotAnimation(hudTextures.pistol, new TextureRegion(hudTextures.pixel));
        fullPotentialAnimation.setOnImpact(() -> { if (!settings.isReducedEffects()) screenShake.shake(0.3f, 8f); });
        symbolStrikes   = new SymbolStrikes(settings, screenShake, new TextureRegion(hudTextures.pixel), hudTextures.coin,
            sidePanel::bumpCoin);
        rainbowAnimation = new RainbowChipsAnimation(settings, new TextureRegion(hudTextures.pixel));
        cutsceneKit      = new CutsceneKit(settings, screenShake, sounds, enemyTextures, shopFont);
        shopOverlay      = new ShopOverlay(stage, hudTextures, tooltip, CARD_WIDTH, CARD_HEIGHT);
        cardDetail       = new CardDetailOverlay(stage, hudTextures, tooltip, CARD_WIDTH, CARD_HEIGHT);
        pauseOverlay     = new PauseOverlay(luckyGame, luckyGame.getBatch(), hudTextures, sounds.buttonClick, sounds.menuHover,
            settings, modeMenuLabel(),
            new PauseOverlay.Listener() {
                @Override public void onResume()         { Gdx.input.setInputProcessor(gameInput); if (cutscene != null) cutscene.resumeSound(); }
                @Override public void onRestart()        { Gdx.input.setInputProcessor(gameInput); GameScreenBase.this.onRestart(); }
                @Override public void onMainMenu()       { onBackToMenu(); }
                @Override public void onModeMenu()       { onBackToChapters(); }
                @Override public void onEffectsChanged() { GameScreenBase.this.onEffectsChanged(); }
            });
        pickOverlay      = new EnemyPickOverlay(stage, hudTextures, enemyTextures, sounds.cardFlip, sounds.cardHover,
            sounds.fireworkBurst);
        crossroadsOverlay = new CrossroadsOverlay(stage, hudTextures, sounds.cardFlip, sounds.cardHover);
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
        hand.setHoverListener(this::previewCombos);
        pileOverlay.setOnInspect(this::showCardDetail);
        shopIcon = new ShopIcon(hudTextures.shop, shopFont, sounds.shopHover, this::openShop);
        shopStand = new Billboard(projection, 1f, () -> new Vector2(shopIcon.getX() + shopIcon.getWidth() / 2f, shopIcon.getY()));
        props     = new TableProps(solids, shopIcon);
        props.addPile(enemyView.getDeckPile());    // du fond vers le joueur
        props.addPile(enemyView.getDiscardPile());
        props.addPile(piles.deck());
        props.addPile(piles.discard());
        enemyView.setMachine(() -> enemyMachine.raise(enemyView.getReelGroup()), enemyMachine::lower);
        frontScene = new FrontScene(projection, playArea, enemyTextures, enemyView, tooltip,
            () -> gameController.getGameState().getEnemy());
        frontScene.setDecor(frontDecor());
        // Ce qui est dessiné à plat par-dessus la table reporte ses points à leur place à l'écran.
        tooltip.setTableProjection(projection);
        effectPopupAnimator.setTableProjection(projection);
        confetti.setTableProjection(projection);
        pistolAnimation.setTableProjection(projection);
        symbolStrikes.setTableProjection(projection);
        bingoAnimation.setTableProjection(projection);
        rainbowAnimation.setTableProjection(projection);

        layout();
        refreshAll();

        addActors();
        applyTableView(); // vue de face ou classique (Options), une fois tout posé

        if (run instanceof CroupierRun) { // le Croupier entre en scène avant le combat
            Cutscene intro = Cutscenes.beforeBoss(cutsceneKit, EnemyKind.GRAND_CROUPIER);
            if (intro instanceof CroupierCutscene croupierIntro) { // il distribue tes cinq prochaines cartes
                List<Card> pile = player().getDeck().getCards();
                List<String> next = new ArrayList<>();
                for (int i = pile.size() - 1; i >= 0 && next.size() < 5; i--) next.add(pile.get(i).getAssetPath());
                croupierIntro.withCards(next, false);
            }
            playCutscene(intro, this::startPlayerTurn);
        } else if (run instanceof DungeonRun dungeonRun && dungeonRun.getRival() != null) {
            // Le Joueur Masqué prend la place du soldat : sa scène d'abord (son masque tombe au Désert).
            playCutscene(Cutscenes.beforeBoss(cutsceneKit, dungeonRun.getRival()), this::startPlayerTurn);
        } else {
            announceGolden();
            startPlayerTurn(); // le combat commence par le tour du joueur, main piochée d'office
        }
    }

    /** Survol du bouclier du joueur : son infobulle. */
    void addShieldTooltip() {
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
    }

    /**
     * Ajoute les acteurs à la scène, du fond vers l'avant : l'ordre décide de ce qui passe devant.
     * Ce qui est posé sur la table va dans son calque ({@link #tableLayer}, en perspective en vue de face).
     */
    void addActors() {
        stage.addActor(frontScene);             // vue de face : décor et ennemi en grand, derrière la table
        tableLayer.addActor(table.getActor());  // le dessus de la table (en perspective en vue de face)
        tableLayer.addActor(slots.getActor());  // les rouleaux, couchés sur la table au repos
        tableLayer.addActor(enemyView.getDeckPile());    // les piles, sur leur emplacement :
        tableLayer.addActor(enemyView.getDiscardPile()); // en vue de face, des volumes (props)
        piles.addTo(tableLayer);
        stage.addActor(tableLayer);
        stage.addActor(props);                  // vue de face : piles et échoppe en volume
        // Debout sur la table, du fond vers le joueur ; les machines sortent de la table au tirage.
        stage.addActor(enemyMachine);
        enemyReelsStand.addActor(enemyView.getDefenseBadge());
        stage.addActor(enemyReelsStand);
        enemyStand.addActor(enemyView.getActor());
        stage.addActor(enemyStand);
        shopStand.addActor(shopIcon);
        stage.addActor(shopStand);
        handStand.addActor(aspirationPanel);
        handStand.addActor(hand.getActor());
        stage.addActor(handStand);
        stage.addActor(slotsStand);             // vue de face : les symboles des rouleaux, redressés
        reelsStand.addActor(playerShield);
        spinControls.addTo(reelsStand);
        stage.addActor(reelsStand);
        stage.addActor(playerMachine);
        stage.addActor(sidePanel.getActor());
        hud.addTo(stage);
        stage.addActor(pauseButton);
        stage.addActor(journalButton);
        stage.addActor(playsLabel);
        stage.addActor(stageLabel);
        stage.addActor(pistolAnimation);
        stage.addActor(symbolStrikes);
        stage.addActor(popupLayer);
        stage.addActor(speech);       // réactions de l'ennemi : au-dessus du jeu, sous les annonces, ne bloque pas les clics
        stage.addActor(turnBanner);   // annonce des tours : au-dessus du jeu, ne bloque pas les clics
        stage.addActor(combatEnd);
        stage.addActor(endButtons); // au-dessus de l'annonce de fin de combat
        stage.addActor(journalView.getActor()); // journal du combat : par-dessus la table et la fin de combat
        stage.addActor(pickOverlay.getActor()); // Tour des épreuves : choix de l'adversaire, boss
        stage.addActor(crossroadsOverlay.getActor()); // Exploration : carrefour avant le roi
        stage.addActor(chestOverlay.getActor()); // Exploration : coffre au trésor
        stage.addActor(confetti);
        stage.addActor(damageVignette);
        stage.addActor(fullPotentialAnimation); // As à plein potentiel : par-dessus la table, ne bloque pas les clics
        stage.addCaptureListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                if (fullPotentialAnimation.isPlaying()) fullPotentialAnimation.skip(); // un clic la fait disparaître
                return false; // le clic sert quand même au jeu
            }
        });
        stage.addActor(jackpotLayer); // par-dessus le jeu et le panneau : bloque les clics pendant la fête
        stage.addActor(achievementToast);   // passe devant tout quand il s'affiche, sans bloquer les clics
        stage.addActor(bingoAnimation);         // carte Bingo : faisceaux et téléportation, bloque les clics
        stage.addActor(rainbowAnimation);       // carte Arc-en-ciel : jetons et poussière d'étoiles, bloque les clics
        stage.addActor(choiceOverlay.getActor()); // choix demandé par une carte (Pari, Roulette russe)
        stage.addActor(shopOverlay.getActor());   // échoppe, ouverte depuis son icône
        stage.addActor(pileOverlay.getActor()); // voile de consultation des piles, par-dessus le jeu
        if (guide != null) stage.addActor(guide); // tutoriel : le Croupier, sous les fiches et les infobulles
        stage.addActor(cardDetail.getActor());  // fiche d'une carte, par-dessus tout le reste
        stage.addActor(tooltip.getActor());     // en dernier : toujours au-dessus
        stage.addActor(screenShake);            // invisible : met à jour la caméra
    }

    /**
     * @return le contrôleur du combat : le deck construit par le joueur (ou le deck de départ),
     *         relu à chaque nouveau combat, sa machine et son rang ; le tutoriel a les siens
     */
    static GameController createController(PlayerProfile profile, CombatRun run) {
        if (run instanceof TutorialRun) {
            // Tutoriel : son deck rangé d'avance, la machine de départ, aucun rang, une échoppe qui ne vend que le Bingo.
            return new GameController(() -> CardLoader.loadDeck(TutorialRun.deck()),
                CardLoader.cardFactory(), TutorialRun.shop(),
                cards -> new Player(Lang.t("Joueur"), Player.BASE_HP, cards, RankBonus.NONE, Symbol.classicReels()));
        } else {
            // Échoppe : la carte de test du Jeu bonus en ADMIN seulement ; les Bingos d'un rouleau précis à
            // l'Entraînement seulement (partout en ADMIN).
            Map<String, Integer> shop = GameController.stallFor(CardLoader.loadShop(), CardLoader.cardFactory(),
                profile.isAdmin(), run == null);
            return new GameController(() -> CardLoader.loadDeck(profile.getDeck()),
                CardLoader.cardFactory(), shop,
                cards -> new Player(Lang.t("Joueur"), Player.BASE_HP, cards, profile.getRankBonus(), profile.getMachine()));
        }
    }

    /** Ce que le Croupier du tutoriel montre sur l'écran de jeu, et ce qu'il y règle. */
    final class TutorialBoard implements TutorialDirector.Board {
        @Override public Rectangle enemyBar()  { return GuideOverlay.boundsOf(hud.getEnemyHealthBar()); }
        @Override public Rectangle playerBar() { return GuideOverlay.boundsOf(hud.getPlayerHealthBar()); }
        @Override public Rectangle enemy()     { return GuideOverlay.around(enemyView.getCroupierCenter(), 260f, 260f); }
        @Override public Rectangle hand(Predicate<Card> which) { return GameScreenBase.this.hand.boundsOf(which); }
        @Override public Rectangle spinButton() { return GuideOverlay.boundsOf(spinControls.getLever()); }
        @Override public Rectangle stakeButton() { return GuideOverlay.boundsOf(spinControls.getStakeButton()); }
        @Override public Rectangle reels() {
            Vector2 first = slots.getReelCenter(0), last = slots.getReelCenter(slots.getReelCount() - 1);
            return new Rectangle(first.x - SlotView.CELL_WIDTH / 2f, first.y - SlotView.CELL_HEIGHT / 2f,
                last.x - first.x + SlotView.CELL_WIDTH, SlotView.CELL_HEIGHT);
        }
        @Override public Rectangle playerShield() { return GuideOverlay.boundsOf(GameScreenBase.this.playerShield); }
        @Override public Rectangle enemyDefense() {
            return GuideOverlay.boundsOf(enemyView.getDefenseBadge());
        }
        @Override public Rectangle gains()    { return GuideOverlay.boundsOf(sidePanel.getGainsBox()); }
        @Override public Rectangle combos()   { return GuideOverlay.boundsOf(sidePanel.getCombosBox()); }
        @Override public Rectangle effects()  { return GuideOverlay.boundsOf(sidePanel.getEffectsBox()); }
        @Override public Rectangle shopIcon() { return GuideOverlay.boundsOf(GameScreenBase.this.shopIcon); }
        @Override public boolean shopShown()      { return shopOverlay.isShown(); }
        @Override public boolean enemyDefeated()  { return gameController.getGameState().getEnemy().isDefeated(); }
        @Override public boolean lastSpinWasBingo() { return lastSpinBingo; }
        @Override public void rig(Symbol[] symbols) { gameController.rigSpin(symbols); }
        @Override public void rigJackpot(Symbol symbol) { gameController.rigJackpot(symbol); }
        @Override public void refreshHand() { GameScreenBase.this.hand.refreshBlocked(); }
    }

    /**
     * Joue le bruitage correspondant au tirage : paire (2 identiques) ou aucun.
     * Celui du jackpot (bingo) accompagne sa célébration (voir onJackpotShown).
     */
    void playSymbolResultSound(TurnResult result) {
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
    /** @return l'ennemi du combat en cours. */
    EnemyKind enemyKind() { return gameController.getGameState().getEnemy().getKind(); }

    /**
     * L'ennemi dit {@code line} dans sa bulle, près de sa bouche (le grand ennemi en vue de face) ;
     * rien sans phrase ou pendant le tutoriel.
     */
    void enemySays(String line) {
        if (line == null || tutorial != null) return;
        Vector2 mouth = projection.isEnabled() ? frontScene.getMouth() : enemyView.getMouth();
        speech.say(line, mouth.x, mouth.y);
    }

    Player player() {
        return gameController.getGameState().getPlayer();
    }

    /** Met à jour les barres de vie, les gains, les effets actifs et les compteurs du deck et de la défausse. */
    void refreshAll() {
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
    /**
     * Panneau des Aspirations de la Machine Originelle : les règles et les cartes
     * jouées ce tour ; les rouleaux qu'elle aspirera si rien ne change portent une chaîne.
     */
    void refreshAspiration() {
        List<AspirationRule> rules = player().getLastingEffects().getAspirations();
        List<Card> played = player().getPlayedCards();
        aspirationPanel.update(rules, played);
        Set<Integer> blocked = blockedReels(), threatened = new TreeSet<>();
        for (AspirationRule rule : rules) {
            if (rule.isMet(played)) continue;
            Set<Integer> taken = new TreeSet<>(blocked);
            taken.addAll(threatened);
            int reel = PreparationResolver.aspiredReel(taken);
            if (reel >= 0) threatened.add(reel);
        }
        slots.setThreatenedReels(threatened);
        aspirationPanel.setPosition(playArea.getX() + BUTTON_MARGIN, table.getHandRowY() + CARD_HEIGHT + BUTTON_MARGIN);
    }

    void refreshPlays() {
        StringBuilder text = new StringBuilder(Lang.f("Cartes {0}/{1}",
            gameController.getCardsPlayedThisTurn(), gameController.getPlayLimit()));
        if (gameController.getSpinCost() > 0) {
            text.append("   ").append(Lang.f("Tirage -{0}", SidePanel.formatGains(gameController.getSpinPrice())));
            double staked = gameController.getStakeAmount();
            if (staked > 0) text.append("   ").append(Lang.f("Mise -{0}", SidePanel.formatGains(staked)));
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
    void refreshEffects() {
        sidePanel.setActiveEffects(new CombatEffectRows(gameController, hudTextures, slots::regionOf, shownDebt()).rows());
    }

    /** Met à jour le compteur de gains, sans les gains du tirage dont le texte n'est pas encore apparu. */
    void refreshGains() {
        sidePanel.setGains(player().getGains() - gainsNotYetShown);
        refreshPot();
    }

    /** La cagnotte du Jeu bonus, sous les gains, avec son infobulle. */
    void refreshPot() {
        int inTen = Math.round(gameController.getBonusGameChance() * 10);
        sidePanel.setPot(gameController.getPot().getAmount(), Lang.t("Cagnotte"),
            Lang.f("Tes Mises s'y accumulent. Au prochain Jeu bonus, elle se joue puis repart de 0 : la Roue, "
                + "le Plinko ou le Coup de poing la multiplient, et la grille, plus rare, la paie à chaque "
                + "alignement. Ce qu'elle rapporte ne se mise pas avant la fin de la descente. Ici, un Bingo ouvre "
                + "un Jeu bonus {0} fois sur 10.", inTen));
        // Gains misables : montrés dès qu'une partie des gains vient de la cagnotte (ceux-là ne se misent pas).
        boolean locked = player().getPotWinnings() > 0;
        sidePanel.setStakeable(locked ? Lang.f("Misables : {0}", SidePanel.formatGains(player().getStakeableGains())) : null,
            Lang.t("Gains misables"),
            Lang.t("La Mise ne prend que ces gains-là : ceux rapportés par la cagnotte ne se misent pas avant la fin "
                + "de la descente. Tirages et achats se paient d'abord avec les gains de la cagnotte."));
    }

    /** Place le bouclier du joueur à droite de ses rouleaux, et son levier à leur gauche. */
    void placePlayerShield() {
        playerShield.setPosition(table.getPlayerReelsRight() + PLAYER_SHIELD_GAP,
            table.getReelRowY() + (SlotView.CELL_HEIGHT - playerShield.getHeight()) / 2f);
        spinControls.placeLever();
    }

    /** Ouvre ou replie le journal du combat. */
    void toggleJournal() {
        journalView.toggle();
        if (tooltip != null) tooltip.hide();
    }

    /** Remet le journal du combat à jour (à la fin de chaque tour) et ajuste la hauteur de son panneau. */
    void refreshJournal() {
        journalView.setEntries(journal.getEntries());
        placeJournal();
    }

    /** Le panneau du journal s'ouvre sous les boutons Pause et Journal, et s'arrête au-dessus de la barre de vie du joueur. */
    void placeJournal() {
        float top = pauseButton.getY() - JOURNAL_BUTTON_GAP;
        float bottom = hud.getPlayerHealthBar().getY() + HealthBarView.HEIGHT + JOURNAL_BUTTON_GAP;
        journalView.layout(pauseButton.getX(), top, top - bottom);
    }

    /** Place (ou replace après un redimensionnement) les éléments qui dépendent de la taille de l'écran. */
    void layout() {
        table.layout();
        Rectangle tableBounds = table.getBounds();
        projection.update(tableBounds.x, tableBounds.y, tableBounds.width, tableBounds.height, playArea.getHeight());
        enemyView.layout();
        placePlayerShield();
        hud.layout(); // avant la pause, qui se cale sur la barre de vie de l'ennemi
        // Pause : en haut, à droite du cadre des gains, à hauteur de la barre de vie de l'ennemi.
        HealthBarView enemyBar = hud.getEnemyHealthBar();
        pauseButton.setPosition(playArea.getX() + PAUSE_BUTTON_MARGIN,
            enemyBar.getY() + (HealthBarView.HEIGHT - pauseButton.getHeight()) / 2f);
        journalButton.setPosition(pauseButton.getX() + pauseButton.getWidth() + JOURNAL_BUTTON_GAP, pauseButton.getY());
        placeJournal();
        spinControls.placeStake();
        playsLabel.pack();
        playsLabel.setPosition(playArea.getX() + BUTTON_MARGIN,
            BUTTON_MARGIN + (pauseButton.getHeight() - playsLabel.getHeight()) / 2f);
        sidePanel.layout(stage);
        piles.layout(table.getDeckX(), table.getPilesY());
        slots.layout();
        hand.layout(false);
        endButtons.setPosition(playArea.getCenterX() - endButtons.getWidth() / 2f,
            playArea.getHeight() * END_BUTTONS_HEIGHT);
        placeShopIcon();
        if (stageLabel != null) placeStageLabel();
        if (guide != null) { // « Passer le tutoriel » à droite de la barre de vie de l'ennemi
            guide.setSkipAnchor(enemyBar.getRight() + SKIP_BUTTON_GAP, enemyBar.getY() + HealthBarView.HEIGHT / 2f);
        }
    }

    /**
     * Échoppe à droite de la table, entre le deck de l'ennemi (en haut) et la défausse
     * du joueur (en bas) : centrée sur leur colonne et dans l'espace libre entre leurs tapis.
     */
    void placeShopIcon() {
        CardPileView discard = piles.discard();
        float columnCenter = discard.getX() + discard.getWidth() / 2f;
        float gapBottom    = discard.getY() + discard.getHeight();
        float gapTop       = table.getEnemyPilesY();
        float shift = projection.isEnabled() ? TableProps.STALL_SHIFT : 0f; // vue de face : l'étal s'écarte du rebord
        shopIcon.setPosition(columnCenter - shift - shopIcon.getWidth() / 2f,
            (gapBottom + gapTop) / 2f - shopIcon.getHeight() / 2f);
    }

    /** @return la place dans la main (0 à 8) de la touche 1 à 9 (rangée du haut ou pavé numérique), sinon -1. */
    static int cardKey(int keycode) {
        if (keycode >= Input.Keys.NUM_1 && keycode <= Input.Keys.NUM_9) return keycode - Input.Keys.NUM_1;
        if (keycode >= Input.Keys.NUMPAD_1 && keycode <= Input.Keys.NUMPAD_9) return keycode - Input.Keys.NUMPAD_1;
        return -1;
    }

    /**
     * Vue de la table (Options > Visuel) : classique ou de face. Le tutoriel garde
     * la vue classique, où le Croupier montre les éléments de la table.
     */
    void applyTableView() {
        boolean front = settings.isFrontView() && tutorial == null;
        if (front == projection.isEnabled()) return;
        projection.setEnabled(front);
        enemyView.setFrontView(front);
        enemyView.getDeckPile().setFrontView(front);
        enemyView.getDiscardPile().setFrontView(front);
        piles.deck().setFrontView(front);
        piles.discard().setFrontView(front);
        // Vue de face : les piles de gauche de la table empilent leurs cartes vers le milieu.
        piles.deck().setStackTowardRight(front);
        enemyView.getDiscardPile().setStackTowardRight(front);
        shopIcon.setFrontView(front);
        spinControls.setFrontView(front);
        placeShopIcon();
        playerMachine.putAway();
        enemyMachine.putAway();
        // Vue de face : les symboles des rouleaux se redressent vers le joueur.
        if (front) {
            enemyReelsStand.addActorAt(0, enemyView.getReelGroup());
            slotsStand.addActor(slots.getActor());
        } else {
            enemyView.takeBackReels();
            tableLayer.addActorAt(1, slots.getActor()); // couchés sur la table, juste au-dessus du feutre
        }
        tooltip.hide();
    }

    /**
     * @return le facteur de vitesse des animations (Options) ; une cinématique
     *         accompagnée de sa musique reste à vitesse normale, pour rester accordée
     */
    float animationSpeed() {
        if (cutscene != null && cutscene.isPlaying() && cutscene.hasSoundtrack()) return 1f;
        return settings.getAnimationSpeed().factor();
    }

    /** @return le décor de la vue de face : celui du donjon en Exploration, sinon le casino. */
    FrontScene.Decor frontDecor() {
        if (run instanceof DungeonRun dungeonRun) return FrontScene.Decor.of(dungeonRun.getDungeon());
        if (run instanceof TowerRun towerRun)     return FrontScene.Decor.of(towerRun.getChapter());
        if (run instanceof EndlessRun || run instanceof CroupierRun) return FrontScene.Decor.of(Chapter.LE_JACKPOT); // le sommet de la Tour
        if (run == null) return FrontScene.Decor.of(trainingEnemy); // Entraînement : le décor de l'ennemi choisi (son donjon, son chapitre)
        return FrontScene.Decor.CASINO;
    }

    // Implémentées plus bas (GameScreenRun, GameScreen)
    abstract void playCutscene(Cutscene next, Runnable then);
    abstract void announceGolden();
    abstract Set<Integer> blockedReels();
    abstract String modeMenuLabel();
    abstract void onBackToChapters();
    abstract void onBackToMenu();
    abstract void onCardPlayed(Card card, CardImage image, Vector2 cardCenter, Vector2 clickPos);
    abstract void onCardRefused(Card card, String reason, Vector2 cardCenter);
    abstract void onDeckClicked();
    abstract void onDiscardClicked();
    abstract void onEffectsChanged();
    abstract void onRestart();
    abstract void onSpin();
    abstract void onStake();
    abstract void openShop();
    abstract void placeStageLabel();
    abstract void previewCombos(Card hovered);
    abstract void refreshStageLabel();
    abstract void showCardDetail(Card card);
    abstract void showPauseMenu();
    abstract SpinEconomy.Debt shownDebt();
    abstract void skipTutorial();
    abstract void startPlayerTurn();
    abstract void startRun(PlayerProfile profile);
    abstract String unplayableReason(Card card);
}
