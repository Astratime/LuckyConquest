package fr.astratime.lucky;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.loaders.CardLoader;
import fr.astratime.lucky.progress.CrashReport;
import fr.astratime.lucky.progress.GdxProfileStorage;
import fr.astratime.lucky.progress.SaveSlots;
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

    /** Les emplacements de sauvegarde. */
    private SaveSlots slots;
    /** Emplacement de la partie chargée ({@link #profile}). */
    private int slot;
    /** Collection, deck et pièces du joueur, gardés d'une partie à l'autre (ceux de l'emplacement {@link #slot}). */
    private PlayerProfile profile;
    /** Messages à dire au lancement, déjà traduits (voir {@link #takeNotice()}). */
    private final List<String> notices = new ArrayList<>();
    /** Message sur la sauvegarde de la partie chargée (refusée ou reprise de sa copie), déjà traduit. */
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
        // Posé avant toute lecture d'asset : rétablit à la volée les fichiers
        // brouillés du build livré, transparent si les assets sont en clair (dev).
        fr.astratime.lucky.security.ObfuscatedFiles.install();
        batch = new SpriteBatch();
        Lang.load();
        slots = GdxProfileStorage.slots();
        if (CrashReport.takePending(CrashReport.DEFAULT_FILE)) {
            notices.add(Lang.f("Le jeu a planté la dernière fois. Désolé ! Le rapport est ici : {0}. "
                + "Envoie-le avec ton signalement du bug, il aide à le corriger.", CrashReport.DEFAULT_FILE));
        }
        // La dernière partie jouée est chargée tout de suite (l'emplacement 1 s'il n'y en a aucune) :
        // « Continuer partie » la reprend, « Nouvelle partie » en ouvre une autre.
        int last = slots.lastUsed();
        load(last == 0 ? 1 : last, false);

        setScreen(new MenuScreen(this, true));
    }

    /**
     * Charge la partie de l'emplacement {@code newSlot} (rien à faire si c'est déjà
     * elle) et la note comme la dernière jouée. Le mode ADMIN reste tel qu'il était.
     *
     * @param newSlot l'emplacement, de 1 à {@link SaveSlots#COUNT}
     */
    public void continueSlot(int newSlot) {
        if (newSlot != slot) load(newSlot, false);
        slots.setLastUsed(slot);
    }

    /**
     * Efface la partie de l'emplacement {@code newSlot} et y commence une nouvelle
     * partie, enregistrée tout de suite (l'emplacement est pris), notée comme la dernière jouée.
     */
    public void newGame(int newSlot) {
        slots.erase(newSlot);
        load(newSlot, true);
        profile.save();
        slots.setLastUsed(slot);
    }

    /** @return les emplacements de sauvegarde. */
    public SaveSlots getSlots() {
        return slots;
    }

    /** @return l'emplacement de la partie chargée. */
    public int getSlot() {
        return slot;
    }

    /** Charge la partie de l'emplacement {@code newSlot} ; {@code fresh} : une partie neuve, sans l'ancien profil. */
    private void load(int newSlot, boolean fresh) {
        boolean admin = profile != null && profile.isAdmin();
        SecureProfileStorage storage = fresh
            ? new SecureProfileStorage(slots.file(newSlot), java.util.Map::of, () -> { })
            : GdxProfileStorage.open(slots, newSlot);
        slot    = newSlot;
        profile = new PlayerProfile(storage, CardLoader.loadStartingCollection(),
            CardLoader.loadStarterDeckCopies());
        saveNotice = null;
        if (storage.isRejected()) {
            saveNotice = Lang.t("Ta sauvegarde a été modifiée en dehors du jeu, ou abîmée, et sa copie de secours aussi. "
                + "Je ne peux pas les reprendre : ta partie repart de zéro. L'ancien fichier est gardé à côté, "
                + "en .refuse.");
        } else if (storage.isRestoredFromBackup()) {
            saveNotice = Lang.t("Ta sauvegarde a été modifiée en dehors du jeu, ou abîmée. J'ai repris sa copie de "
                + "secours : tes tout derniers progrès sont peut-être perdus.");
        }
        // Mode ADMIN : toutes les cartes à collectionner (celles des coffres, le profil les connaît déjà).
        List<String> catalog = new ArrayList<>(CardLoader.loadStartingCollection().keySet());
        catalog.addAll(CardLoader.loadBoutique().keySet());
        profile.setCatalog(catalog);
        profile.setUpgradable(CardLoader.loadAll().stream().filter(Card::isUpgradable).map(Card::getId).toList());
        profile.setAdmin(admin);
    }

    /**
     * @return le prochain message que le Croupier dit au lancement (sauvegarde refusée
     *         ou reprise depuis la copie de secours, plantage du lancement d'avant),
     *         traduit ; {@code null} s'il n'y en a plus (chacun n'est dit qu'une fois)
     */
    public String takeNotice() {
        if (saveNotice != null) {
            String notice = saveNotice;
            saveNotice = null;
            return notice;
        }
        return notices.isEmpty() ? null : notices.remove(0);
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
