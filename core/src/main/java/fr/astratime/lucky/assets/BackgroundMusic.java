package fr.astratime.lucky.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.settings.AudioSettings;

/**
 * Musique de fond d'un écran, jouée en boucle. Son volume est celui du réglage
 * « Musique » ({@link AudioSettings}) multiplié par {@code level}, gardé bas pour
 * ne pas couvrir les bruitages ; il suit le réglage dès qu'il change.
 */
public class BackgroundMusic implements Disposable {

    private final Music         music;
    private final AudioSettings audio;
    private final float         level;
    private float               applied = -1f;

    /**
     * @param path  chemin interne du fichier (ex : "music/combat.ogg")
     * @param level volume de la musique quand le réglage est à 100 %
     */
    public BackgroundMusic(String path, AudioSettings audio, float level) {
        this.music = Gdx.audio.newMusic(Gdx.files.internal(path));
        this.audio = audio;
        this.level = level;
        music.setLooping(true);
    }

    /** Lance (ou reprend) la musique. */
    public void play() {
        update();
        music.play();
    }

    /** Applique le réglage « Musique » s'il a changé ; à appeler à chaque image. */
    public void update() {
        float volume = level * audio.getMusicVolume();
        if (volume == applied) return;
        applied = volume;
        music.setVolume(volume);
    }

    @Override
    public void dispose() {
        music.stop();
        music.dispose();
    }
}
