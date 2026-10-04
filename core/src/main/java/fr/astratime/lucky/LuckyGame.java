package fr.astratime.lucky;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import fr.astratime.lucky.loaders.CardLoader;
import fr.astratime.lucky.progress.GdxProfileStorage;
import fr.astratime.lucky.progress.PlayerProfile;
import fr.astratime.lucky.screens.MenuScreen;
import fr.astratime.lucky.settings.ScreenMode;
import fr.astratime.lucky.settings.ScreenModeSwitcher;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class LuckyGame extends Game {

    /** SpriteBatch partagé par tous les écrans (créé une seule fois, réutilisé partout). */
    private SpriteBatch batch;

    private final ScreenModeSwitcher screenModeSwitcher;

    /** Collection, deck et pièces du joueur, gardés d'une partie à l'autre (chargés au lancement). */
    private PlayerProfile profile;

    /** Jeu sans réglage du mode d'affichage (plateformes sans fenêtre, tests). */
    public LuckyGame() {
        this(ScreenModeSwitcher.NONE);
    }

    /** @param screenModeSwitcher change le mode d'affichage de la fenêtre, fourni par le lanceur */
    public LuckyGame(ScreenModeSwitcher screenModeSwitcher) {
        this.screenModeSwitcher = screenModeSwitcher;
    }

    /**
     * Appelé une fois par libGDX au lancement de l'application.
     * Crée le SpriteBatch partagé et affiche l'écran d'accueil.
     */
    @Override
    public void create() {
        batch = new SpriteBatch();
        profile = new PlayerProfile(new GdxProfileStorage(), CardLoader.loadStartingCollection(),
            CardLoader.loadStarterDeckCopies());

        setScreen(new MenuScreen(this));
    }

    /** Délègue le rendu à l'écran actif (voir {@link Game#render()}). */
    @Override
    public void render() {
        super.render();
    }

    /**
     * Libère l'écran actif puis le SpriteBatch partagé. {@link Game#dispose()}
     * se contente d'appeler {@code hide()} sur l'écran actif : ses ressources
     * (textures, polices, sons) doivent être libérées explicitement.
     */
    @Override
    public void dispose() {
        Screen current = getScreen();
        super.dispose();
        if (current != null) current.dispose();
        batch.dispose();
    }

    /** Passe la fenêtre du jeu en mode {@code mode} (choisi dans les options). */
    public void applyScreenMode(ScreenMode mode) {
        screenModeSwitcher.apply(mode);
    }

    /** @return la collection, le deck et les pièces du joueur. */
    public PlayerProfile getProfile() {
        return profile;
    }

    /** @return le SpriteBatch partagé, à utiliser par tous les écrans plutôt que d'en recréer un. */
    public SpriteBatch getBatch() {
        return this.batch;
    }
}
