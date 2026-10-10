package fr.astratime.lucky;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.loaders.CardLoader;
import fr.astratime.lucky.progress.GdxProfileStorage;
import fr.astratime.lucky.progress.SecureProfileStorage;
import fr.astratime.lucky.progress.PlayerProfile;
import fr.astratime.lucky.screens.MenuScreen;
import fr.astratime.lucky.settings.ScreenMode;
import fr.astratime.lucky.settings.ScreenModeSwitcher;

import java.util.ArrayList;
import java.util.List;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class LuckyGame extends Game {

    /** SpriteBatch partagé par tous les écrans (créé une seule fois, réutilisé partout). */
    private SpriteBatch batch;

    private final ScreenModeSwitcher screenModeSwitcher;

    /** Collection, deck et pièces du joueur, gardés d'une partie à l'autre (chargés au lancement). */
    private PlayerProfile profile;
    /** Message sur la sauvegarde à dire au lancement (voir {@link #takeSaveNotice()}). */
    private String saveNotice;

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
        Lang.load();
        SecureProfileStorage storage = GdxProfileStorage.open();
        profile = new PlayerProfile(storage, CardLoader.loadStartingCollection(),
            CardLoader.loadStarterDeckCopies());
        if (storage.isRejected()) {
            saveNotice = "Ta sauvegarde a été modifiée en dehors du jeu, ou abîmée, et sa copie de secours aussi. "
                + "Je ne peux pas les reprendre : ta partie repart de zéro. L'ancien fichier est gardé à côté, "
                + "en .refuse.";
        } else if (storage.isRestoredFromBackup()) {
            saveNotice = "Ta sauvegarde a été modifiée en dehors du jeu, ou abîmée. J'ai repris sa copie de "
                + "secours : tes tout derniers progrès sont peut-être perdus.";
        }
        // Mode ADMIN : toutes les cartes à collectionner (celles des coffres, le profil les connaît déjà).
        List<String> catalog = new ArrayList<>(CardLoader.loadStartingCollection().keySet());
        catalog.addAll(CardLoader.loadBoutique().keySet());
        profile.setCatalog(catalog);
        profile.setUpgradable(CardLoader.loadAll().stream().filter(Card::isUpgradable).map(Card::getId).toList());

        setScreen(new MenuScreen(this));
    }

    /**
     * @return ce que le Croupier dit au joueur sur sa sauvegarde (refusée, ou
     *         reprise depuis la copie de secours), en français ; {@code null}
     *         si tout va bien ou si c'est déjà dit (une seule fois par lancement)
     */
    public String takeSaveNotice() {
        String notice = saveNotice;
        saveNotice = null;
        return notice;
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
