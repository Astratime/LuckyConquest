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

    /** Volume gardé quand la musique s'efface derrière une cinématique. */
    private static final float DUCKED = 0.15f;

    private Music               music;
    private String              path;
    private boolean             playing;
    private final AudioSettings audio;
    private final float         level;
    private float               applied = -1f;
    private boolean             ducked;

    /**
     * @param path  chemin interne du fichier (ex : "music/combat.ogg")
     * @param level volume de la musique quand le réglage est à 100 %
     */
    public BackgroundMusic(String path, AudioSettings audio, float level) {
        this.audio = audio;
        this.level = level;
        load(path);
    }

    private void load(String path) {
        this.path  = path;
        this.music = Gdx.audio.newMusic(Gdx.files.internal(path));
        music.setLooping(true);
        applied = -1f;
    }

    /** Lance la musique, ou la laisse continuer si elle joue déjà. */
    public void play() {
        update();
        music.play();
        playing = true;
    }

    /** Arrête la musique ; {@link #play()} la relancera du début. */
    public void stop() {
        music.stop();
        playing = false;
    }

    /**
     * Passe à la musique {@code path}, ou à {@code fallback} si son fichier n'existe
     * pas ; elle reprend du début, et joue tout de suite si la précédente jouait.
     * Rien ne change si c'est déjà celle-là.
     */
    public void switchTo(String path, String fallback) {
        String next = Gdx.files.internal(path).exists() ? path : fallback;
        if (next.equals(this.path)) return;
        music.stop();
        music.dispose();
        load(next);
        if (playing) play();
    }

    /** Applique le réglage « Musique » s'il a changé ; à appeler à chaque image. */
    public void update() {
        // Coupée sans qu'on l'ait arrêtée (flux interrompu, périphérique audio changé) : on la relance.
        if (playing && !music.isPlaying()) music.play();
        float volume = level * audio.getMusicVolume() * (ducked ? DUCKED : 1f);
        if (volume == applied) return;
        applied = volume;
        music.setVolume(volume);
    }

    /** Baisse la musique (pendant une cinématique) ou lui rend son volume. */
    public void setDucked(boolean ducked) {
        this.ducked = ducked;
        update();
    }

    @Override
    public void dispose() {
        music.stop();
        music.dispose();
    }
}
