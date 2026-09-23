package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.entities.Card;

import java.util.HashMap;
import java.util.Map;

/** Cache des textures de cartes : chaque image est chargée à la première demande, puis réutilisée. */
class CardTextures implements Disposable {

    private final Map<String, Texture> textures = new HashMap<>();

    /** @return la texture de la carte, chargée à la demande puis mise en cache par chemin d'asset. */
    Texture get(Card card) {
        return textures.computeIfAbsent(card.getAssetPath(), path -> new Texture(Gdx.files.internal(path)));
    }

    @Override
    public void dispose() {
        textures.values().forEach(Texture::dispose);
        textures.clear();
    }
}
