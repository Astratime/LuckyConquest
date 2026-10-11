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
 *
 * Les cartes spéciales s'affichent au style des cartes classiques : l'image de {@value #CLASSIC_DIR}
 * (version blanche) remplace celle de {@value #SPECIAL_DIR} et des cartes Bingo de {@value #TEST_DIR},
 * gardées telles quelles. À défaut, l'image pleine illustration de {@value #FULL_ART_DIR} est prise.
 * Les versions en couleurs ({@value #CLASSIC_COLOR_DIR}) ne sont pas encore affichées.
 */
public class CardTextures implements Disposable {

    /** Dossier des anciennes images des cartes spéciales (cadre et pièce d'or). */
    static final String SPECIAL_DIR  = "cards/special/";
    /** Dossier des cartes spéciales en pleine illustration, mêmes noms de fichiers. */
    static final String FULL_ART_DIR = "cards/special_full/";
    /** Dossier des cartes Bingo par symbole et de la carte du Jeu bonus. */
    static final String TEST_DIR     = "cards/test/";
    /** Dossier des cartes spéciales au style des cartes classiques, version blanche (celle du jeu). */
    static final String CLASSIC_DIR  = "cards/special_classic/blanc/";
    /** Même dossier, version aux couleurs de chaque donjon. */
    static final String CLASSIC_COLOR_DIR = "cards/special_classic/couleurs/";

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
        String path = art(card.getAssetPath());
        boolean strong = strongSuits(path);
        return textures.computeIfAbsent(path + Card.UPGRADE_SUFFIX + (strong ? STRONG_SUFFIX : ""),
            key -> build(path, strong, true));
    }

    /** @return true si la carte {@code card} s'affiche dans sa nouvelle illustration (elle reçoit alors le reflet arc-en-ciel). */
    public static boolean isFullArt(Card card) {
        return !art(card.getAssetPath()).equals(card.getAssetPath());
    }

    /**
     * @return l'image à afficher pour {@code path} : sa version au style classique si elle existe,
     *         sinon sa version pleine illustration, sinon {@code path}
     */
    static String art(String path) {
        if (path == null) return path;
        String dir = path.startsWith(SPECIAL_DIR) ? SPECIAL_DIR : path.startsWith(TEST_DIR) ? TEST_DIR : null;
        if (dir == null || Gdx.files == null) return path;
        String name = path.substring(dir.length());
        if (Gdx.files.internal(CLASSIC_DIR + name).exists()) return CLASSIC_DIR + name;
        if (dir.equals(SPECIAL_DIR) && Gdx.files.internal(FULL_ART_DIR + name).exists()) return FULL_ART_DIR + name;
        return path;
    }

    /** @return la texture de l'image de carte {@code original} (face ou dos), chargée à la demande puis mise en cache. */
    public Texture get(String original) {
        String path = art(original);
        if (!strongSuits(path)) return textures.computeIfAbsent(path, p -> new Texture(Gdx.files.internal(p)));
        return textures.computeIfAbsent(path + STRONG_SUFFIX, key -> build(path, true, false));
    }

    /** Clé de cache des images assombries (cartes du joueur copiées par son Ombre). */
    private static final String DARK_SUFFIX = "#dark";

    /** @return la texture de l'image {@code path} en version sombre (les cartes copiées par l'Ombre du Joueur). */
    public Texture getDark(String original) {
        String path = art(original);
        return textures.computeIfAbsent(path + DARK_SUFFIX, key -> {
            Pixmap source = new Pixmap(Gdx.files.internal(path));
            Pixmap pixmap = new Pixmap(source.getWidth(), source.getHeight(), Pixmap.Format.RGBA8888);
            pixmap.setBlending(Pixmap.Blending.None);
            for (int y = 0; y < source.getHeight(); y++) {
                for (int x = 0; x < source.getWidth(); x++) pixmap.drawPixel(x, y, darken(source.getPixel(x, y)));
            }
            source.dispose();
            Texture texture = new Texture(pixmap);
            pixmap.dispose();
            return texture;
        });
    }

    /** @return le pixel RGBA8888 {@code rgba} assombri et teinté de violet, comme le jeu sombre de l'ennemi. */
    static int darken(int rgba) {
        int r = rgba >>> 24, g = (rgba >>> 16) & 0xff, b = (rgba >>> 8) & 0xff, a = rgba & 0xff;
        int grey = (r * 30 + g * 59 + b * 11) / 100;
        r = (r * 35 + grey * 15) / 100 + 18;
        g = (g * 30 + grey * 15) / 100 + 10;
        b = (b * 35 + grey * 15) / 100 + 34;
        return Math.min(255, r) << 24 | Math.min(255, g) << 16 | Math.min(255, b) << 8 | a;
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
