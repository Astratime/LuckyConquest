package fr.astratime.lucky;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import fr.astratime.lucky.screens.ControlsScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class LuckyGame extends Game {

    /** SpriteBatch partagé par tous les écrans (créé une seule fois, réutilisé partout). */
    private SpriteBatch batch;

    /**
     * Appelé une fois par libGDX au lancement de l'application.
     * Crée le SpriteBatch partagé et affiche l'écran d'accueil.
     */
    @Override
    public void create() {
        batch = new SpriteBatch();

        setScreen(new ControlsScreen(this));
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

    /** @return le SpriteBatch partagé, à utiliser par tous les écrans plutôt que d'en recréer un. */
    public SpriteBatch getBatch() {
        return this.batch;
    }
}
