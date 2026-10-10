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
import fr.astratime.lucky.settings.AudioSettings;
import fr.astratime.lucky.settings.DisplaySettings;
import fr.astratime.lucky.settings.VisualSettings;
import fr.astratime.lucky.entities.tutorial.TutorialRun;
import fr.astratime.lucky.views.AchievementToast;
import fr.astratime.lucky.views.AchievementsOverlay;
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

    /** Bruitage du clic (CC0, Kenney.nl — voir assets/sounds/CREDITS.txt). */
    private static final String CLICK_SOUND = "sounds/button-click.ogg";
    private static final String ACHIEVEMENT_SOUND = "sounds/ui/achievement.ogg";
    /** Survol d'une option (synthétisé, voir tools/sounds/generate_sounds.py). */
    private static final String HOVER_SOUND = "sounds/ui/menu_hover.ogg";
    private static final String TITLE       = "LUCKY CONQUEST";
    /** Musique du menu (fournie par Astra) et son volume au réglage maximal : pas de bruitage à couvrir ici. */
    private static final String MUSIC       = "music/main_menu.ogg";
    private static final float  MUSIC_LEVEL = 0.3f;

    /** Taille minimale du menu : dans une fenêtre plus petite, il est réduit (voir {@link MinimumScreenViewport}). */
    private static final float MIN_WIDTH      = 1280f;
    private static final float MIN_HEIGHT     = 1080f;
    private static final float TITLE_TOP      = 230f;   // du haut de l'écran au centre du titre
    private static final float TITLE_FLOAT    = 8f;     // amplitude du flottement du titre
    private static final float PANEL_CENTER_Y = 0.46f;  // fraction de la hauteur de l'écran
    /** Options de la page principale hors mode ADMIN. */
    private static final int   MAIN_ENTRIES   = 7;
    private static final float FADE_TIME      = 0.4f;
    private static final float PARALLAX       = 18f;    // décalage maximal du décor, en pixels
    private static final float PARALLAX_EASE  = 4f;
    /** Le Croupier parle une fois les options apparues. */
    private static final float GUIDE_DELAY    = 0.9f;

    private final LuckyGame      luckyGame;
    private final Stage          stage;
    private final VisualSettings settings = new VisualSettings();
    private final AudioSettings  audio    = new AudioSettings();
    private final DisplaySettings display = new DisplaySettings();
    private final HudTextures    hud      = new HudTextures();
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
    private final CasinoButtons     guideButtons = new CasinoButtons();
    private final GuideOverlay      guide;
    /** Statistiques et succès (Options) ; annonce des succès atteints. */
    private final AchievementsOverlay achievements;
    private final AchievementToast    toast;
    private final Sound               achievementSound;
    private boolean                 leaving;
    private float                   parallaxX, parallaxY;

    /** @param luckyGame instance de jeu : SpriteBatch partagé et changement d'écran */
    public MenuScreen(LuckyGame luckyGame) {
        this.luckyGame = luckyGame;
        this.stage     = new Stage(new MinimumScreenViewport(MIN_WIDTH, MIN_HEIGHT), luckyGame.getBatch());

        clickSound  = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(CLICK_SOUND)), audio);
        hoverSound  = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(HOVER_SOUND)), audio);
        music       = new BackgroundMusic(MUSIC, audio, MUSIC_LEVEL);
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

        showMainPage();
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
        stage.addAction(Actions.delay(GUIDE_DELAY, Actions.run(this::startGuide)));
        // Succès atteints ailleurs (ou par une partie d'avant les succès) : annoncés en arrivant au menu.
        stage.addAction(Actions.delay(GUIDE_DELAY, Actions.run(() -> toast.show(luckyGame.getProfile().checkAchievements()))));
    }

    // -------------------------------------------------------------------------
    // Pages
    // -------------------------------------------------------------------------

    /**
     * Page principale : Entraînement, Tour des épreuves, Exploration, Table du croupier, Boutique,
     * Cinématique (en mode ADMIN seulement), Options, Quitter.
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
        entries.add(OptionsMenu.Entry.button(Lang.t("Quitter"), this::onQuit));
        showPage(null, entries);
    }

    /** Page des options : affichage, effets visuels, musique, sons, langue, tutoriel, mode ADMIN, Retour. */
    private void showOptionsPage() {
        showOptionsPage(0);
    }

    /** @param selected l'option sélectionnée à l'ouverture de la page */
    private void showOptionsPage(int selected) {
        List<OptionsMenu.Entry> entries = new ArrayList<>(
            OptionsMenu.settingsEntries(luckyGame, settings, audio, display, () -> { }));
        int languageIndex = entries.size();
        // Langue : la page est redessinée (au prochain rendu) pour que tous ses textes changent.
        Runnable toggleLanguage = () -> {
            Lang.set(Lang.get().next());
            Gdx.app.postRunnable(() -> showOptionsPage(languageIndex));
        };
        entries.add(new OptionsMenu.Entry(() -> Lang.f("Langue : {0}", Lang.get().label), toggleLanguage,
            direction -> toggleLanguage.run()));
        entries.add(OptionsMenu.Entry.button(Lang.t("Statistiques et succès"), this::showAchievements));
        entries.add(OptionsMenu.Entry.button(Lang.t("Rejouer le tutoriel"), this::onTutorial));
        PlayerProfile profile = luckyGame.getProfile();
        Runnable toggleAdmin = () -> profile.setAdmin(!profile.isAdmin());
        entries.add(new OptionsMenu.Entry(() -> Lang.f("Mode ADMIN : {0}",
            (profile.isAdmin() ? Lang.t("activé") : Lang.t("désactivé"))),
            toggleAdmin, direction -> toggleAdmin.run()));
        entries.add(OptionsMenu.Entry.button(Lang.t("Retour"), this::showMainPage));
        showPage(Lang.t("OPTIONS"), entries, selected);
    }

    /** Ouvre la fenêtre des statistiques et des succès ; le menu attend qu'elle se ferme. */
    private void showAchievements() {
        menu.setActive(false);
        achievements.show(luckyGame.getProfile(), () -> menu.setActive(true));
        toast.toFront();
        fade.toFront();
    }

    private void showPage(String caption, List<OptionsMenu.Entry> entries) {
        showPage(caption, entries, 0);
    }

    private void showPage(String caption, List<OptionsMenu.Entry> entries, int selected) {
        menu.setEntries(caption, entries, selected);
        placeMenu();
        fade.toFront();
    }

    /**
     * Centre le panneau des options ; une page principale plus longue que
     * {@link #MAIN_ENTRIES} options (« Cinématique » en mode ADMIN) descend,
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
        menu.layout(width / 2f, height * PANEL_CENTER_Y - extra * OptionsMenu.OPTION_STEP / 2f);
    }

    // -------------------------------------------------------------------------
    // Le Croupier
    // -------------------------------------------------------------------------

    /**
     * Premier lancement : le Croupier propose le tutoriel (« Plus tard » : il
     * reste dans les Options). Tutoriel fini ou passé : il présente le menu, une fois.
     */
    private void startGuide() {
        if (leaving || menu.hasCaption()) return;
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
                if (keycode == Input.Keys.ESCAPE && menu.hasCaption()) {
                    clickSound.play();
                    showMainPage();
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
        toast.dispose();
        achievementSound.dispose();
        croupier.dispose();
        guideButtons.dispose();
        decor.dispose();
        Fonts.release(titleFont);
        Fonts.release(shineFont);
        menu.dispose();
        clickSound.dispose();
        hoverSound.dispose();
        music.dispose();
        hud.dispose();
    }
}
