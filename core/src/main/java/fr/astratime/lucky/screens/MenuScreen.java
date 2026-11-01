package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.assets.EnemyTextures;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.assets.BackgroundMusic;
import fr.astratime.lucky.assets.VolumeSound;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.progress.PlayerProfile;
import fr.astratime.lucky.progress.Rank;
import fr.astratime.lucky.progress.SaveSlots;
import fr.astratime.lucky.settings.AudioSettings;
import fr.astratime.lucky.settings.DisplaySettings;
import fr.astratime.lucky.settings.VisualSettings;
import fr.astratime.lucky.entities.tutorial.TutorialRun;
import fr.astratime.lucky.views.AchievementToast;
import fr.astratime.lucky.views.AchievementsOverlay;
import fr.astratime.lucky.views.BestiaryOverlay;
import fr.astratime.lucky.views.CasinoButtons;
import fr.astratime.lucky.views.GuideOverlay;
import fr.astratime.lucky.views.MenuDecor;
import fr.astratime.lucky.views.MinimumScreenViewport;
import fr.astratime.lucky.views.OptionsMenu;
import fr.astratime.lucky.views.ShiningTitle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Écran d'accueil, devant l'intérieur animé d'un casino ({@link MenuDecor}) qui
 * se décale légèrement avec la souris (parallaxe) : le titre, sur lequel passe
 * un reflet, flotte au-dessus d'un panneau d'options.
 *
 * Au lancement, l'écran s'ouvre sur l'accueil : « Continuer partie » (la dernière
 * partie jouée, ou le choix de la partie quand plusieurs emplacements en ont une),
 * « Nouvelle partie » (choix d'un des {@link SaveSlots#COUNT} emplacements ; le Croupier
 * prévient avant d'effacer une partie), « Options » et « Quitter ». Une partie
 * choisie mène à la page principale, qui a un « Retour » vers l'accueil.
 *
 * Page principale : « Entraînement » (lance un combat contre le croupier d'entraînement, {@link GameScreen}), « Tour des
 * épreuves » (choix d'un chapitre, {@link TowerScreen}), « Options » et « Quitter ». Page des options : affichage (fenêtre agrandie ou plein
 * écran, appliqué tout de suite), effets visuels (normaux ou réduits,
 * réglage partagé avec l'écran de jeu), volume de la musique, volume des sons,
 * mode ADMIN (tout le contenu du jeu ouvert et le mode « Cinématique », {@link CinematicScreen} ;
 * désactivé à chaque lancement, voir {@link PlayerProfile#setAdmin(boolean)})
 * et « Retour ».
 *
 * Les options (panneau partagé avec le menu pause, voir {@link OptionsMenu}) se
 * choisissent à la souris ou aux flèches ; Entrée ou un clic valide,
 * gauche/droite règlent, Échap revient à la page principale.
 */
public class MenuScreen extends ScreenAdapter {

    private static final String ACHIEVEMENT_SOUND = "sounds/ui/achievement.ogg";
    private static final String TITLE       = "LUCKY CONQUEST";
    /** Musique du menu (fournie par Astra) et son volume au réglage maximal : pas de bruitage à couvrir ici. */

    /** Taille minimale du menu : dans une fenêtre plus petite, il est réduit (voir {@link MinimumScreenViewport}). */
    private static final float MIN_WIDTH      = 1280f;
    private static final float MIN_HEIGHT     = 1080f;
    private static final float TITLE_TOP      = 230f;   // du haut de l'écran au centre du titre
    private static final float TITLE_FLOAT    = 8f;     // amplitude du flottement du titre
    private static final float PANEL_CENTER_Y = 0.46f;  // fraction de la hauteur de l'écran
    private static final float PANEL_MARGIN   = 40f;    // le bord du panneau et un peu d'espace sous lui
    /** Options d'une page sans en-tête qui restent centrées ; au-delà, le panneau descend (voir {@link #placeMenu()}). */
    private static final int   MAIN_ENTRIES   = 7;
    private static final float FADE_TIME      = 0.4f;
    private static final float PARALLAX       = 18f;    // décalage maximal du décor, en pixels
    private static final float PARALLAX_EASE  = 4f;
    /** Le Croupier parle une fois les options apparues. */
    private static final float GUIDE_DELAY    = 0.9f;

    private final LuckyGame      luckyGame;
    /** Communs à tous les écrans (voir {@link SharedAssets}) : ne jamais les libérer ici. */
    private final AudioSettings  audio;
    private final HudTextures    hud;
    private final CasinoButtons  guideButtons;
    private final Stage          stage;
    private final VisualSettings settings = new VisualSettings();
    /** Échap sur une sous-page (Accessibilité) : retour à sa page parente ; {@code null} : à la page principale. */
    private Runnable escapeBack;
    private final DisplaySettings display = new DisplaySettings();
    private final BitmapFont     titleFont;
    private final BitmapFont     shineFont;
    private final Sound          clickSound;
    private final Sound          hoverSound;
    private final BackgroundMusic music;

    private final MenuDecor         decor = new MenuDecor();
    private final ShiningTitle      title;
    private final OptionsMenu       menu;
    private final Image             fade;
    /** Le Croupier : il propose le tutoriel au premier lancement, puis présente le menu après le tutoriel. */
    private final Texture           croupier = EnemyTextures.newCroupierPortrait();
    private final GuideOverlay      guide;
    /** Statistiques et succès (Options) ; annonce des succès atteints. */
    private final AchievementsOverlay achievements;
    private final AchievementToast    toast;
    /** Bestiaire et collection (Options) ; ses portraits ne se chargent qu'à sa première ouverture. */
    private BestiaryOverlay           bestiary;
    private final Sound               achievementSound;
    private boolean                 leaving;
    /** L'accueil (ou le choix d'une partie) est affiché : aucune partie n'est encore choisie. */
    private boolean                 home;
    private float                   parallaxX, parallaxY;

    /** @param luckyGame instance de jeu : SpriteBatch partagé et changement d'écran */
    public MenuScreen(LuckyGame luckyGame) {
        this(luckyGame, false);
    }

    /**
     * @param luckyGame instance de jeu : SpriteBatch partagé et changement d'écran
     * @param home      {@code true} : l'accueil (lancement du jeu) ; {@code false} : la page
     *                  principale de la partie chargée (retour d'un autre écran)
     */
    public MenuScreen(LuckyGame luckyGame, boolean home) {
        this.luckyGame = luckyGame;
        this.home      = home;
        SharedAssets assets = luckyGame.getAssets();
        this.audio        = assets.audio;
        this.hud          = assets.hud;
        this.guideButtons = assets.buttons;
        this.stage     = new Stage(new MinimumScreenViewport(MIN_WIDTH, MIN_HEIGHT), luckyGame.getBatch());

        clickSound  = assets.clickSound;
        hoverSound  = assets.hoverSound;
        music       = assets.menuMusic; // partagée : elle continue d'un menu à l'autre
        Color shadow = Palette.TEXT_SHADE;
        titleFont   = Fonts.jersey(128, Palette.GOLD, 7f, shadow, TITLE);
        shineFont   = Fonts.jersey(128, Color.WHITE, 7f, shadow, TITLE);

        title   = new ShiningTitle(TITLE, titleFont, shineFont);
        fade    = new Image(new TextureRegionDrawable(new TextureRegion(hud.pixel)));
        fade.setColor(Color.BLACK);
        fade.setTouchable(Touchable.disabled);

        stage.addActor(decor);
        stage.addActor(title);
        menu = new OptionsMenu(stage, hud, clickSound, hoverSound);

        if (home) showHomePage();
        else showMainPage();
        guide = new GuideOverlay(hud, croupier);
        stage.addActor(guide);
        achievements = new AchievementsOverlay(hud, guideButtons, clickSound);
        stage.addActor(achievements.getActor());
        achievementSound = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(ACHIEVEMENT_SOUND)), audio);
        toast = new AchievementToast(hud, achievementSound);
        stage.addActor(toast);
        stage.addActor(fade);                     // en dernier : fondu d'ouverture et de sortie
        fade.addAction(Actions.fadeOut(FADE_TIME));
        layout();
        if (!home) welcome();
    }

    /**
     * Arrivée sur la page principale : le Croupier parle (voir {@link #startGuide()}) et les
     * succès atteints ailleurs (ou par une partie d'avant les succès) sont annoncés.
     */
    private void welcome() {
        stage.addAction(Actions.delay(GUIDE_DELAY, Actions.run(this::startGuide)));
        stage.addAction(Actions.delay(GUIDE_DELAY, Actions.run(() -> toast.show(luckyGame.getProfile().checkAchievements()))));
    }

    // -------------------------------------------------------------------------
    // Accueil et emplacements de sauvegarde
    // -------------------------------------------------------------------------

    /**
     * Accueil : Continuer partie (s'il y a une partie enregistrée), Nouvelle partie,
     * Options, Quitter.
     */
    private void showHomePage() {
        home = true;
        List<OptionsMenu.Entry> entries = new ArrayList<>();
        List<Integer> used = luckyGame.getSlots().usedSlots();
        if (!used.isEmpty()) {
            entries.add(OptionsMenu.Entry.button(Lang.t("Continuer partie"), () -> {
                if (used.size() == 1) enterGame(used.get(0), false);
                else showSlotsPage(false);
            }));
        }
        entries.add(OptionsMenu.Entry.button(Lang.t("Nouvelle partie"), () -> showSlotsPage(true)));
        entries.add(OptionsMenu.Entry.button(Lang.t("Options"), () -> showOptionsPage(0, 0)));
        entries.add(OptionsMenu.Entry.button(Lang.t("Quitter"), this::onQuit));
        showPage(null, entries);
    }

    /**
     * Choix d'un emplacement : pour une nouvelle partie, les {@link SaveSlots#COUNT}
     * emplacements (le Croupier prévient avant d'effacer une partie) ; pour continuer,
     * ceux qui ont une partie, le dernier joué sélectionné.
     */
    private void showSlotsPage(boolean newGame) {
        SaveSlots slots = luckyGame.getSlots();
        List<OptionsMenu.Entry> entries = new ArrayList<>();
        int selected = 0;
        for (int slot = 1; slot <= SaveSlots.COUNT; slot++) {
            int chosen = slot;
            boolean used = slots.isUsed(slot);
            if (!newGame && !used) continue;
            if (!newGame && slot == slots.lastUsed()) selected = entries.size();
            entries.add(OptionsMenu.Entry.button(slotLabel(slot), () -> {
                if (!newGame) enterGame(chosen, false);
                else if (used) confirmReplace(chosen);
                else enterGame(chosen, true);
            }));
        }
        entries.add(OptionsMenu.Entry.button(Lang.t("Retour"), this::showHomePage));
        showPage(newGame ? Lang.t("NOUVELLE PARTIE") : Lang.t("CONTINUER PARTIE"), entries, selected);
        escapeBack = this::showHomePage;
    }

    /** @return le nom d'un emplacement et ce qu'il contient : vide, ou le rang de sa partie. */
    private String slotLabel(int slot) {
        SaveSlots slots = luckyGame.getSlots();
        if (!slots.isUsed(slot)) return Lang.f("Partie {0} : vide", slot);
        if (slots.peek(slot) == null) return Lang.f("Partie {0} : abîmée", slot);
        Rank rank = slots.rank(slot);
        return rank == null ? Lang.f("Partie {0} : sans rang", slot) : Lang.f("Partie {0} : {1}", slot, rank.getTitle());
    }

    /** L'emplacement {@code slot} a déjà une partie : le Croupier prévient avant de l'effacer. */
    private void confirmReplace(int slot) {
        menu.setActive(false);
        GuideOverlay.Step warning = GuideOverlay.Step.say(Lang.f("Attention : l'emplacement {0} a déjà une partie. "
            + "Une nouvelle partie l'efface pour de bon : pièces, cartes, rang, tout repart de zéro.", slot)).buttons(
            guideButtons.create(Lang.t("Effacer et commencer"), clickSound, () -> {
                guide.stop();
                enterGame(slot, true);
            }),
            guideButtons.create(Lang.t("Annuler"), clickSound, () -> {
                guide.stop();
                menu.setActive(true);
            }));
        guide.toFront();
        fade.toFront();
        guide.play(List.of(warning), null);
    }

    /**
     * Charge la partie de l'emplacement {@code slot} ({@code newGame} : en commence
     * une nouvelle) et passe à la page principale.
     */
    private void enterGame(int slot, boolean newGame) {
        if (newGame) luckyGame.newGame(slot);
        else luckyGame.continueSlot(slot);
        home = false;
        menu.setActive(true);
        showMainPage();
        // Les options de la page viennent d'être ajoutées : le Croupier et les fenêtres repassent devant.
        achievements.getActor().toFront();
        guide.toFront();
        toast.toFront();
        fade.toFront();
        welcome();
    }

    // -------------------------------------------------------------------------
    // Pages
    // -------------------------------------------------------------------------

    /**
     * Page principale : Entraînement, Tour des épreuves, Exploration, Table du croupier, Boutique,
     * Cinématique (en mode ADMIN seulement), Options, Retour (vers l'accueil), Quitter.
     */
    private void showMainPage() {
        List<OptionsMenu.Entry> entries = new ArrayList<>(List.of(
            OptionsMenu.Entry.button(Lang.t("Entraînement"), this::onPlay),
            OptionsMenu.Entry.button(Lang.t("Tour des épreuves"), this::onTower),
            OptionsMenu.Entry.button(Lang.t("Exploration"), () -> goTo(() -> new ExplorationScreen(luckyGame))),
            OptionsMenu.Entry.button(Lang.t("Table du croupier"), () -> goTo(() -> new CroupierTableScreen(luckyGame))),
            OptionsMenu.Entry.button(Lang.t("Boutique"), () -> goTo(() -> new ShopScreen(luckyGame)))));
        // Mode Cinématique : seulement en mode ADMIN.
        if (luckyGame.getProfile().isAdmin()) {
            entries.add(OptionsMenu.Entry.button(Lang.t("Cinématique"), () -> goTo(() -> new CinematicScreen(luckyGame))));
        }
        entries.add(OptionsMenu.Entry.button(Lang.t("Options"), this::showOptionsPage));
        entries.add(OptionsMenu.Entry.button(Lang.t("Retour"), this::showHomePage));
        entries.add(OptionsMenu.Entry.button(Lang.t("Quitter"), this::onQuit));
        showPage(null, entries);
    }

    /** Onglets de la page des options. */
    private static final List<String> OPTION_TABS = List.of(OptionsMenu.TAB_VISUAL, OptionsMenu.TAB_AUDIO,
        OptionsMenu.TAB_ACCESSIBILITY, OptionsMenu.TAB_GAME);
    private static final int GAME_TAB = 3;

    /** Page des options, sur son premier onglet (Visuel). */
    private void showOptionsPage() {
        showOptionsPage(0, 0);
    }

    /**
     * Page des options, en onglets : Visuel (affichage, animations, cartes colorées), Audio
     * (musique, sons), Accessibilité, Jeu (langue, puis, une partie choisie, statistiques,
     * bestiaire, tutoriel, mode ADMIN) ; chaque onglet finit par Retour (vers l'accueil ou la page principale).
     *
     * @param tab      l'onglet affiché
     * @param selected l'option sélectionnée à l'ouverture de l'onglet
     */
    private void showOptionsPage(int tab, int selected) {
        List<List<OptionsMenu.Entry>> pages = List.of(
            OptionsMenu.visualEntries(luckyGame, settings, display),
            OptionsMenu.audioEntries(audio),
            OptionsMenu.accessibilityEntries(settings, () -> { }),
            gameEntries());
        int rows = 0;
        for (List<OptionsMenu.Entry> page : pages) rows = Math.max(rows, page.size() + 1);
        List<OptionsMenu.Entry> entries = new ArrayList<>(pages.get(tab));
        entries.add(OptionsMenu.Entry.button(Lang.t("Retour"), home ? this::showHomePage : this::showMainPage));
        List<String> labels = OPTION_TABS.stream().map(Lang::t).toList();
        showPage(Lang.t("OPTIONS"), new OptionsMenu.Tabs(labels, tab, other -> showOptionsPage(other, 0), rows),
            entries, selected);
        if (home) escapeBack = this::showHomePage;
    }

    /**
     * @return l'onglet Jeu des options : langue, puis (une partie choisie) statistiques,
     *         bestiaire, tutoriel, mode ADMIN
     */
    private List<OptionsMenu.Entry> gameEntries() {
        List<OptionsMenu.Entry> entries = new ArrayList<>();
        int languageIndex = entries.size();
        // Langue : la page est redessinée (au prochain rendu) pour que tous ses textes changent.
        Runnable toggleLanguage = () -> {
            Lang.set(Lang.get().next());
            Gdx.app.postRunnable(() -> showOptionsPage(GAME_TAB, languageIndex));
        };
        entries.add(new OptionsMenu.Entry(() -> Lang.f("Langue : {0}", Lang.get().label), toggleLanguage,
            direction -> toggleLanguage.run()));
        if (home) return entries; // à l'accueil, aucune partie n'est encore choisie
        entries.add(OptionsMenu.Entry.button(Lang.t("Statistiques et succès"), this::showAchievements));
        entries.add(OptionsMenu.Entry.button(Lang.t("Bestiaire et collection"), this::showBestiary));
        entries.add(OptionsMenu.Entry.button(Lang.t("Rejouer le tutoriel"), this::onTutorial));
        PlayerProfile profile = luckyGame.getProfile();
        Runnable toggleAdmin = () -> profile.setAdmin(!profile.isAdmin());
        entries.add(new OptionsMenu.Entry(() -> Lang.f("Mode ADMIN : {0}",
            (profile.isAdmin() ? Lang.t("activé") : Lang.t("désactivé"))),
            toggleAdmin, direction -> toggleAdmin.run()));
        return entries;
    }

    /** Ouvre la fenêtre des statistiques et des succès ; le menu attend qu'elle se ferme. */
    private void showAchievements() {
        menu.setActive(false);
        achievements.show(luckyGame.getProfile(), () -> menu.setActive(true));
        toast.toFront();
        fade.toFront();
    }

    /** Ouvre la fenêtre du bestiaire et de la collection ; le menu attend qu'elle se ferme. */
    private void showBestiary() {
        if (bestiary == null) {
            bestiary = new BestiaryOverlay(hud, luckyGame.getAssets().enemyTextures(), guideButtons, clickSound,
                () -> goTo(() -> new CinematicScreen(luckyGame)));
            stage.addActor(bestiary.getActor());
        }
        menu.setActive(false);
        bestiary.show(luckyGame.getProfile(), () -> { if (!leaving) menu.setActive(true); });
        toast.toFront();
        fade.toFront();
    }

    private void showPage(String caption, List<OptionsMenu.Entry> entries) {
        showPage(caption, entries, 0);
    }

    private void showPage(String caption, List<OptionsMenu.Entry> entries, int selected) {
        showPage(caption, null, entries, selected);
    }

    private void showPage(String caption, OptionsMenu.Tabs tabs, List<OptionsMenu.Entry> entries, int selected) {
        escapeBack = null;
        menu.setEntries(caption, tabs, entries, selected);
        placeMenu();
        fade.toFront();
    }

    /**
     * Centre le panneau des options ; une page principale plus longue que
     * {@link #MAIN_ENTRIES} options (« Retour », « Cinématique » en mode ADMIN) descend,
     * pour que son haut ne couvre pas davantage le titre.
     */
    private void placeMenu() {
        float width  = stage.getViewport().getWorldWidth();
        float height = stage.getViewport().getWorldHeight();
        if (width <= 0f || height <= 0f) return;
        if (menu.hasCaption()) {
            menu.layout(width / 2f, height / 2f); // page des options : la plus longue, au centre de l'écran
            return;
        }
        int extra = Math.max(0, menu.size() - MAIN_ENTRIES);
        // Le bas du panneau reste à l'écran (page principale en mode ADMIN : Cinématique et Retour en plus).
        float lowest = menu.size() * OptionsMenu.OPTION_STEP / 2f + PANEL_MARGIN;
        menu.layout(width / 2f, Math.max(lowest, height * PANEL_CENTER_Y - extra * OptionsMenu.OPTION_STEP / 2f));
    }

    // -------------------------------------------------------------------------
    // Le Croupier
    // -------------------------------------------------------------------------

    /**
     * Sauvegarde refusée ou reprise de sa copie de secours, ou plantage au lancement
     * d'avant : le Croupier prévient d'abord. Premier lancement : le Croupier propose le tutoriel (« Plus tard » : il
     * reste dans les Options). Tutoriel fini ou passé : il présente le menu, une fois.
     */
    private void startGuide() {
        if (leaving || home || menu.hasCaption()) return;
        // Sauvegarde refusée, copie de secours ou plantage : le Croupier le dit d'abord, au lancement.
        String notice = luckyGame.takeNotice();
        if (notice != null) {
            GuideOverlay.Step step = GuideOverlay.Step.say(notice).buttons(
                guideButtons.create(Lang.t("Compris"), clickSound, () -> {
                    guide.stop();
                    startGuide();
                }));
            guide.play(List.of(step), null);
            return;
        }
        PlayerProfile profile = luckyGame.getProfile();
        if (!profile.hasSeen(PlayerProfile.GUIDE_TUTORIAL) && !profile.hasSeen(PlayerProfile.GUIDE_OFFER)) {
            offerTutorial(profile);
        } else if (profile.hasSeen(PlayerProfile.GUIDE_TUTORIAL) && !profile.hasSeen(PlayerProfile.GUIDE_MENU)) {
            showMenuTour(profile);
        }
    }

    private void offerTutorial(PlayerProfile profile) {
        GuideOverlay.Step offer = GuideOverlay.Step.say(Lang.t("Bienvenue à Lucky Conquest. Première fois à ma table ? Je t'apprends à jouer en un combat. Le "
            + "tutoriel reste aussi dans les Options.")).buttons(
            guideButtons.create(Lang.t("Suivre le tutoriel"), clickSound, () -> {
                profile.markSeen(PlayerProfile.GUIDE_OFFER);
                guide.stop();
                onTutorial();
            }),
            guideButtons.create(Lang.t("Plus tard"), clickSound, () -> {
                profile.markSeen(PlayerProfile.GUIDE_OFFER);
                guide.stop();
            }));
        guide.play(List.of(offer), null);
    }

    /** Le Croupier présente chaque bouton du menu principal. */
    private void showMenuTour(PlayerProfile profile) {
        guide.setSkipButton(guideButtons.create(Lang.t("Passer"), clickSound, () -> {
            profile.markSeen(PlayerProfile.GUIDE_MENU);
            guide.stop();
        }));
        String[] lines = {
            Lang.t("Entraînement : un combat contre moi, pour t'exercer. Il ne rapporte rien."),
            Lang.t("Tour des épreuves : six chapitres de combats, jusqu'au sommet. Elle ne rapporte pas de pièces."),
            Lang.t("Exploration : des donjons, leurs rois et leurs coffres. Les seuls combats qui rapportent des "
                + "pièces."),
            Lang.t("Table du croupier : ton deck de 20 cartes et les 11 rouleaux de ta machine."),
            Lang.t("Boutique : tes pièces y achètent des rangs, des cartes et des rouleaux."),
            Lang.t("Options : le tutoriel s'y rejoue. Bonne chance à la table.")};
        List<GuideOverlay.Step> steps = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            // « Options » vient après « Cinématique » quand le mode ADMIN l'affiche.
            int index = i == lines.length - 1 && profile.isAdmin() ? i + 1 : i;
            steps.add(GuideOverlay.Step.say(lines[i], () -> GuideOverlay.boundsOf(menu.getOption(index))));
        }
        guide.play(steps, () -> profile.markSeen(PlayerProfile.GUIDE_MENU));
    }

    // -------------------------------------------------------------------------
    // Actions
    // -------------------------------------------------------------------------

    /** Fondu au noir puis le tutoriel : un combat commenté par le Croupier. */
    private void onTutorial() {
        fadeOutThen(() -> {
            luckyGame.setScreen(new GameScreen(luckyGame, new TutorialRun()));
            dispose();
        });
    }

    /**
     * Fondu au noir puis lancement d'un combat ; l'écran du menu est libéré
     * ensuite (hors de la boucle d'animation du Stage, qu'il ne faut pas
     * disposer pendant qu'il s'exécute).
     */
    private void onPlay() {
        fadeOutThen(() -> {
            luckyGame.setScreen(new GameScreen(luckyGame));
            dispose();
        });
    }

    /** Fondu au noir puis écran des chapitres de la Tour des épreuves. */
    private void onTower() {
        fadeOutThen(() -> {
            luckyGame.setScreen(new TowerScreen(luckyGame));
            dispose();
        });
    }

    /** Fondu au noir puis écran {@code next} (Exploration, Table du croupier, Boutique). */
    private void goTo(Supplier<Screen> next) {
        fadeOutThen(() -> {
            luckyGame.setScreen(next.get());
            dispose();
        });
    }

    /** Fondu au noir puis fermeture du jeu. */
    private void onQuit() {
        fadeOutThen(Gdx.app::exit);
    }

    private void fadeOutThen(Runnable next) {
        leaving = true;
        menu.setActive(false);
        fade.clearActions();
        fade.addAction(Actions.sequence(Actions.fadeIn(FADE_TIME), Actions.run(() -> Gdx.app.postRunnable(next))));
    }

    // -------------------------------------------------------------------------
    // Mise en page et cycle de vie
    // -------------------------------------------------------------------------

    /** Place le décor, le titre, le panneau et les options selon la taille de l'écran. */
    private void layout() {
        float width  = stage.getViewport().getWorldWidth();
        float height = stage.getViewport().getWorldHeight();
        // Le décor déborde un peu de l'écran : la parallaxe ne découvre jamais ses bords.
        decor.layout(width + PARALLAX * 2f, height + PARALLAX * 2f);
        placeDecor();
        fade.setBounds(0f, 0f, width, height);

        title.clearActions(); // le flottement repart de la position de repos
        title.setPosition((width - title.getWidth()) / 2f, height - TITLE_TOP - title.getHeight() / 2f);
        title.addAction(Actions.forever(Actions.sequence(
            Actions.moveBy(0f, TITLE_FLOAT, 1.4f, Interpolation.sine),
            Actions.moveBy(0f, -TITLE_FLOAT, 1.4f, Interpolation.sine))));

        placeMenu();
    }

    /** Décale le décor selon la parallaxe courante (il déborde de PARALLAX de chaque côté). */
    private void placeDecor() {
        decor.setPosition(-PARALLAX + parallaxX, -PARALLAX + parallaxY);
    }

    /** Parallaxe : le décor glisse doucement à l'opposé de la souris. */
    private void updateParallax(float delta) {
        float width  = stage.getViewport().getWorldWidth();
        float height = stage.getViewport().getWorldHeight();
        float mouseX = MathUtils.clamp(Gdx.input.getX() / (float) Gdx.graphics.getWidth(), 0f, 1f);
        float mouseY = MathUtils.clamp(Gdx.input.getY() / (float) Gdx.graphics.getHeight(), 0f, 1f);
        float targetX = -(mouseX - 0.5f) * 2f * PARALLAX;
        float targetY = (mouseY - 0.5f) * 2f * PARALLAX;   // Y de la souris compté depuis le haut
        float ease = Math.min(1f, PARALLAX_EASE * delta);
        parallaxX += (targetX - parallaxX) * ease;
        parallaxY += (targetY - parallaxY) * ease;
        if (width > 0f && height > 0f) placeDecor();
    }

    /**
     * Stage d'abord (souris), puis clavier : flèches haut/bas pour choisir,
     * gauche/droite pour régler, Entrée ou Espace pour valider, Échap pour
     * revenir à la page principale.
     */
    @Override
    public void show() {
        InputAdapter keyboard = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (leaving) return false;
                if (guide.isActive()) return true; // le Croupier parle : le menu attend
                if (achievements.isShown()) {
                    if (keycode == Input.Keys.ESCAPE) {
                        clickSound.play();
                        achievements.hide();
                    }
                    return true;
                }
                if (bestiary != null && bestiary.isShown()) {
                    if (keycode == Input.Keys.ESCAPE) {
                        clickSound.play();
                        bestiary.hide();
                    }
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE && menu.hasCaption()) {
                    clickSound.play();
                    if (escapeBack != null) escapeBack.run();
                    else showMainPage();
                    return true;
                }
                return menu.keyDown(keycode);
            }
        };
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, keyboard));
        music.play();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        layout();
        achievements.layout();
        if (bestiary != null) bestiary.layout();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        if (!settings.isReducedEffects()) updateParallax(delta);
        music.update(); // suit le réglage « Musique » des options
        stage.act(delta);
        stage.draw();
    }

    /** Libère toutes les ressources natives (Stage, décor, polices, textures, son) possédées par cet écran. */
    @Override
    public void dispose() {
        stage.dispose();
        guide.dispose();
        achievements.dispose();
        if (bestiary != null) {
            bestiary.dispose();
        }
        toast.dispose();
        achievementSound.dispose();
        croupier.dispose();
        decor.dispose();
        Fonts.release(titleFont);
        Fonts.release(shineFont);
        menu.dispose();
        // La musique appartient aux ressources partagées : elle continue sur l'écran suivant.
    }
}
