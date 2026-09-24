package fr.astratime.lucky.assets;

import com.badlogic.gdx.audio.Sound;
import fr.astratime.lucky.settings.AudioSettings;

/**
 * Bruitage dont le volume suit le réglage « Sons » ({@link AudioSettings}) :
 * chaque lecture multiplie le volume demandé par celui du réglage, relu au
 * moment de jouer. Le reste est délégué au son d'origine.
 */
public class VolumeSound implements Sound {

    private final Sound         sound;
    private final AudioSettings settings;

    public VolumeSound(Sound sound, AudioSettings settings) {
        this.sound    = sound;
        this.settings = settings;
    }

    private float scaled(float volume) {
        return volume * settings.getSoundVolume();
    }

    @Override public long play()                                   { return sound.play(scaled(1f)); }
    @Override public long play(float volume)                       { return sound.play(scaled(volume)); }
    @Override public long play(float volume, float pitch, float pan) { return sound.play(scaled(volume), pitch, pan); }
    @Override public long loop()                                   { return sound.loop(scaled(1f)); }
    @Override public long loop(float volume)                       { return sound.loop(scaled(volume)); }
    @Override public long loop(float volume, float pitch, float pan) { return sound.loop(scaled(volume), pitch, pan); }
    @Override public void stop()                                   { sound.stop(); }
    @Override public void pause()                                  { sound.pause(); }
    @Override public void resume()                                 { sound.resume(); }
    @Override public void dispose()                                { sound.dispose(); }
    @Override public void stop(long soundId)                       { sound.stop(soundId); }
    @Override public void pause(long soundId)                      { sound.pause(soundId); }
    @Override public void resume(long soundId)                     { sound.resume(soundId); }
    @Override public void setLooping(long soundId, boolean looping) { sound.setLooping(soundId, looping); }
    @Override public void setPitch(long soundId, float pitch)      { sound.setPitch(soundId, pitch); }
    @Override public void setVolume(long soundId, float volume)    { sound.setVolume(soundId, scaled(volume)); }
    @Override public void setPan(long soundId, float pan, float volume) { sound.setPan(soundId, pan, scaled(volume)); }
}
