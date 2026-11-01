package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.BackgroundMusic;
import fr.astratime.lucky.assets.CardTextures;
import fr.astratime.lucky.assets.EnemyTextures;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.VolumeSound;
import fr.astratime.lucky.settings.AudioSettings;
import fr.astratime.lucky.views.CasinoButtons;

/**
 * Ressources communes à tous les écrans, chargées une seule fois pour tout le
 * jeu (voir {@link fr.astratime.lucky.LuckyGame#getAssets()}) au lieu d'être
 * rechargées à chaque changement d'écran : textures du HUD, des cartes et des
 * ennemis, boutons du casino, bruits de clic et de survol.
 *
 * Les écrans s'en servent sans jamais les libérer : seul le jeu le fait, à sa
 * fermeture ({@link #dispose()}).
 */
public final class SharedAssets implements Disposable {

    /** Bruitage du clic (CC0, Kenney.nl — voir assets/sounds/CREDITS.txt). */
    private static final String CLICK_SOUND = "sounds/button-click.ogg";
    /** Survol d'une option (synthétisé, voir tools/sounds/generate_sounds.py). */
    private static final String HOVER_SOUND = "sounds/ui/menu_hover.ogg";
    private static final String MENU_MUSIC       = "music/main_menu.ogg";
    private static final float  MENU_MUSIC_LEVEL = 0.3f;

    /** Réglages du son, lus à chaque bruit (un changement dans les options s'applique tout de suite). */
    public final AudioSettings audio = new AudioSettings();
    public final HudTextures   hud   = new HudTextures();
    public final CasinoButtons buttons = new CasinoButtons();
    /** Clic d'un bouton ou d'une option. */
    public final Sound         clickSound = load(CLICK_SOUND);
    /** Survol d'une option de menu. */
    public final Sound         hoverSound = load(HOVER_SOUND);

    /**
     * Musique des écrans de menu (menu principal, Exploration, Tour, Table du croupier,
     * Boutique, Cinématique) : une seule pour tous, elle continue d'un menu à l'autre
     * au lieu de repartir du début ; seul le combat l'arrête.
     */
    public final BackgroundMusic menuMusic = new BackgroundMusic(MENU_MUSIC, audio, MENU_MUSIC_LEVEL);
    /** Chargées à la première demande : le menu principal n'en a pas besoin au lancement. */
    private CardTextures  cardTextures;
    private EnemyTextures enemyTextures;

    private Sound load(String path) {
        return new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(path)), audio);
    }

    /** @return les images des cartes (chargées à la demande, puis gardées). */
    public CardTextures cardTextures() {
        if (cardTextures == null) cardTextures = new CardTextures();
        return cardTextures;
    }

    /** @return les images des ennemis, de leurs symboles et des chapitres. */
    public EnemyTextures enemyTextures() {
        if (enemyTextures == null) enemyTextures = new EnemyTextures(cardTextures());
        return enemyTextures;
    }

    @Override
    public void dispose() {
        if (enemyTextures != null) enemyTextures.dispose();
        if (cardTextures != null) cardTextures.dispose();
        buttons.dispose();
        hud.dispose();
        clickSound.dispose();
        hoverSound.dispose();
        menuMusic.dispose();
    }
}
