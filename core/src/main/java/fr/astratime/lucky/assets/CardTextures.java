package fr.astratime.lucky.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.settings.VisualSettings;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache des textures de cartes : chaque image est chargée à la première demande, puis réutilisée.
 * Partagé par toutes les vues de l'écran de jeu (main, ennemi, fenêtres), qui ne disposent
 * pas elles-mêmes les textures obtenues. Avec l'option « Enseignes renforcées », les
 * Carreaux et les Trèfles classiques sont recolorés (voir {@link SuitColors}).
 */
public class CardTextures implements Disposable {

    /** Clé de cache des images aux enseignes renforcées. */
    private static final String STRONG_SUFFIX = "#4";

    private final Map<String, Texture> textures = new HashMap<>();
    private final VisualSettings       settings = new VisualSettings();

    /**
     * @return la texture de la carte, chargée à la demande puis mise en cache par
     *         chemin d'asset ; celle d'une carte « + » a son liseré doré (voir {@link UpgradedCardArt})
     */
    public Texture get(Card card) {
        if (!card.isUpgraded()) return get(card.getAssetPath());
        boolean strong = strongSuits(card.getAssetPath());
        return textures.computeIfAbsent(card.getAssetPath() + Card.UPGRADE_SUFFIX + (strong ? STRONG_SUFFIX : ""),
            key -> build(card.getAssetPath(), strong, true));
    }

    /** @return la texture de l'image de carte {@code path} (face ou dos), chargée à la demande puis mise en cache. */
    public Texture get(String path) {
        if (!strongSuits(path)) return textures.computeIfAbsent(path, p -> new Texture(Gdx.files.internal(p)));
        return textures.computeIfAbsent(path + STRONG_SUFFIX, key -> build(path, true, false));
    }

    /** @return true si l'image {@code path} se recolore, l'option des enseignes renforcées étant active. */
    private boolean strongSuits(String path) {
        return SuitColors.recolors(path) && settings.isStrongSuitColors();
    }

    /** @return la texture de l'image {@code path}, enseignes recolorées et/ou liseré doré des cartes « + ». */
    private static Texture build(String path, boolean strong, boolean gilded) {
        Pixmap source = new Pixmap(Gdx.files.internal(path));
        int width = source.getWidth(), height = source.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) pixels[y * width + x] = source.getPixel(x, y);
        }
        source.dispose();
        if (strong) pixels = SuitColors.recolor(path, pixels);
        if (gilded) pixels = UpgradedCardArt.gild(pixels, width, height);
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) pixmap.drawPixel(x, y, pixels[y * width + x]);
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        textures.values().forEach(Texture::dispose);
        textures.clear();
    }
}
