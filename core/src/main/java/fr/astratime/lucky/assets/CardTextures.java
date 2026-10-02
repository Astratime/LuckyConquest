package fr.astratime.lucky.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.entities.Card;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache des textures de cartes : chaque image est chargée à la première demande, puis réutilisée.
 * Partagé par toutes les vues de l'écran de jeu (main, ennemi, fenêtres), qui ne disposent
 * pas elles-mêmes les textures obtenues.
 */
public class CardTextures implements Disposable {

    private final Map<String, Texture> textures = new HashMap<>();

    /** @return la texture de la carte, chargée à la demande puis mise en cache par chemin d'asset. */
    public Texture get(Card card) {
        return get(card.getAssetPath());
    }

    /** @return la texture de l'image de carte {@code path} (face ou dos), chargée à la demande puis mise en cache. */
    public Texture get(String path) {
        return textures.computeIfAbsent(path, p -> new Texture(Gdx.files.internal(p)));
    }

    @Override
    public void dispose() {
        textures.values().forEach(Texture::dispose);
        textures.clear();
    }
}
