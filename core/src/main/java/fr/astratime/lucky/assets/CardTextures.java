package fr.astratime.lucky.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
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

    /**
     * @return la texture de la carte, chargée à la demande puis mise en cache par
     *         chemin d'asset ; celle d'une carte « + » a son liseré doré (voir {@link UpgradedCardArt})
     */
    public Texture get(Card card) {
        if (!card.isUpgraded()) return get(card.getAssetPath());
        return textures.computeIfAbsent(card.getAssetPath() + Card.UPGRADE_SUFFIX, key -> gilded(card.getAssetPath()));
    }

    /** @return la texture de l'image {@code path} avec le liseré doré et la pastille « + » des cartes « + ». */
    private static Texture gilded(String path) {
        Pixmap source = new Pixmap(Gdx.files.internal(path));
        int width = source.getWidth(), height = source.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) pixels[y * width + x] = source.getPixel(x, y);
        }
        source.dispose();
        int[] gilded = UpgradedCardArt.gild(pixels, width, height);
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) pixmap.drawPixel(x, y, gilded[y * width + x]);
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
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
