package fr.astratime.lucky.views;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.settings.AudioSettings;
import fr.astratime.lucky.settings.DisplaySettings;
import fr.astratime.lucky.settings.VisualSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu pause, ouvert par Échap ou le bouton pause pendant un combat : un voile
 * assombrit le jeu (figé tant que le menu est ouvert) et un panneau propose de
 * reprendre la partie, de régler les options (les mêmes que celles du menu
 * principal), de recommencer le combat ou de revenir au menu principal. On ne
 * quitte pas le jeu depuis un combat : seulement depuis le menu principal.
 *
 * Il a son propre Stage, dessiné par-dessus celui du jeu : tant qu'il est
 * ouvert, il reçoit seul les clics et le clavier ({@link #getInput()}).
 * Échap ferme le menu, ou revient de la page des options à la page pause.
 */
public class PauseOverlay implements Disposable {

    /** Taille minimale du menu : dans une fenêtre plus petite, il est réduit. */
    private static final float MIN_WIDTH  = 1280f;
    private static final float MIN_HEIGHT = 900f;

    /** Actions du menu pause, fournies par l'écran de jeu. */
    public interface Listener {
        /** Le joueur reprend la partie (le menu est déjà fermé). */
        void onResume();
        /** Le joueur recommence le combat (le menu est déjà fermé). */
        void onRestart();
        /** Le joueur revient au menu principal (le menu est déjà fermé). */
        void onMainMenu();
        /** Les effets visuels viennent de passer de normaux à réduits, ou l'inverse. */
        void onEffectsChanged();
    }

    private final Stage       stage;
    private final Image       veil;
    private final OptionsMenu menu;
    private final Listener    listener;
    /** Onglets des options : Visuel, Audio, Accessibilité. */
    private final List<List<OptionsMenu.Entry>> optionTabs;
    private final InputProcessor input;
    private boolean shown;
    private boolean optionsPage;

    /**
     * @param clickSound bruitage des options (celui des boutons du jeu)
     * @param hoverSound bruitage du survol des options
     * @param settings   réglages visuels de l'écran de jeu (ceux que lit sa secousse)
     */
    public PauseOverlay(LuckyGame game, Batch batch, HudTextures hud, Sound clickSound, Sound hoverSound,
                        VisualSettings settings,
                        Listener listener) {
        this.listener = listener;
        stage = new Stage(new MinimumScreenViewport(MIN_WIDTH, MIN_HEIGHT), batch);
        veil  = new Image(new TextureRegionDrawable(new TextureRegion(hud.pixel)));
        veil.setColor(Palette.VEIL);
        stage.addActor(veil);
        menu = new OptionsMenu(stage, hud, clickSound, hoverSound);
        optionTabs = List.of(
            OptionsMenu.visualEntries(game, settings, new DisplaySettings()),
            OptionsMenu.audioEntries(new AudioSettings()),
            OptionsMenu.accessibilityEntries(settings, listener::onEffectsChanged));

        InputAdapter keyboard = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    clickSound.play();
                    if (optionsPage) showPausePage();
                    else resume();
                    return true;
                }
                return menu.keyDown(keycode);
            }
        };
        input = new InputMultiplexer(stage, keyboard);
    }

    /** Ouvre le menu sur sa page pause. */
    public void show() {
        shown = true;
        showPausePage();
    }

    /** Ferme le menu sans rien faire d'autre (ex : l'écran de jeu se ferme). */
    public void hide() {
        shown = false;
    }

    /** @return {@code true} si le menu est ouvert (le jeu est alors figé). */
    public boolean isShown() { return shown; }

    /** @return le processeur d'entrée du menu, à installer tant qu'il est ouvert. */
    public InputProcessor getInput() { return input; }

    /** Page pause : Reprendre, Options, Recommencer, Menu principal. */
    private void showPausePage() {
        optionsPage = false;
        menu.setEntries(Lang.t("PAUSE"), List.of(
            OptionsMenu.Entry.button(Lang.t("Reprendre"), this::resume),
            OptionsMenu.Entry.button(Lang.t("Options"), this::showOptionsPage),
            OptionsMenu.Entry.button(Lang.t("Recommencer"), () -> {
                shown = false;
                listener.onRestart();
            }),
            OptionsMenu.Entry.button(Lang.t("Menu principal"), () -> {
                shown = false;
                listener.onMainMenu();
            })));
    }

    /** Page des options, sur son premier onglet. */
    private void showOptionsPage() {
        showOptionsPage(0);
    }

    /** Page des options, en onglets (Visuel, Audio, Accessibilité) ; chacun finit par Retour. */
    private void showOptionsPage(int tab) {
        optionsPage = true;
        int rows = 0;
        for (List<OptionsMenu.Entry> page : optionTabs) rows = Math.max(rows, page.size() + 1);
        List<OptionsMenu.Entry> entries = new ArrayList<>(optionTabs.get(tab));
        entries.add(OptionsMenu.Entry.button(Lang.t("Retour"), this::showPausePage));
        List<String> labels = List.of(OptionsMenu.TAB_VISUAL, OptionsMenu.TAB_AUDIO, OptionsMenu.TAB_ACCESSIBILITY)
            .stream().map(Lang::t).toList();
        menu.setEntries(Lang.t("OPTIONS"), new OptionsMenu.Tabs(labels, tab, this::showOptionsPage, rows), entries, 0);
    }

    private void resume() {
        shown = false;
        listener.onResume();
    }

    /** Adapte le voile et centre le panneau (après un redimensionnement de la fenêtre). */
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        float worldWidth  = stage.getViewport().getWorldWidth();
        float worldHeight = stage.getViewport().getWorldHeight();
        veil.setBounds(0f, 0f, worldWidth, worldHeight);
        menu.layout(worldWidth / 2f, worldHeight / 2f);
    }

    /** Anime et dessine le menu par-dessus le jeu, s'il est ouvert. */
    public void render(float delta) {
        if (!shown) return;
        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        menu.dispose();
    }
}
