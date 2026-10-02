package fr.astratime.lucky.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Fabrique des polices pixel art du thème casino, générées à la taille voulue pour rester nettes.
 *
 * Les polices sont partagées : deux demandes identiques (taille, couleurs, contour,
 * caractères) renvoient la même instance, générée une seule fois. Chaque police
 * obtenue doit être rendue par {@link #release(BitmapFont)} (et jamais disposée
 * directement) : elle n'est libérée qu'une fois rendue par tous ses utilisateurs.
 * Le générateur FreeType reste ouvert tant qu'une police est en service.
 */
public final class Fonts {

    private static final String JERSEY_PATH = "fonts/Jersey10-Regular.ttf";

    private record Key(int size, Color color, float borderWidth, Color borderColor, String characters,
                       boolean markup) {}

    private static final class Entry {
        final Key  key;
        int        users;

        Entry(Key key) { this.key = key; }
    }

    private static final Map<Key, BitmapFont>          fonts   = new HashMap<>();
    private static final Map<BitmapFont, Entry>        entries = new IdentityHashMap<>();
    private static FreeTypeFontGenerator               generator;

    /**
     * @return la police Jersey 10 à la taille {@code size} (en pixels), de la couleur
     *         donnée. À rendre par {@link #release(BitmapFont)}.
     */
    public static BitmapFont jersey(int size, Color color) {
        return jersey(size, color, 0, Color.BLACK);
    }

    /**
     * @param borderWidth épaisseur du contour (en pixels), 0 pour aucun contour
     * @return la police Jersey 10 à la taille {@code size}, entourée d'un contour
     *         {@code borderColor} pour rester lisible sur un fond coloré.
     *         À rendre par {@link #release(BitmapFont)}.
     */
    public static BitmapFont jersey(int size, Color color, float borderWidth, Color borderColor) {
        return jersey(size, color, borderWidth, borderColor, FreeTypeFontGenerator.DEFAULT_CHARS);
    }

    /**
     * Comme {@link #jersey(int, Color, float, Color)}, mais seuls les caractères de
     * {@code characters} sont générés : à réserver aux très grandes polices qui
     * n'écrivent qu'un texte connu d'avance, pour ne pas remplir la mémoire graphique
     * de lettres jamais affichées. Un caractère absent n'est pas dessiné.
     */
    public static BitmapFont jersey(int size, Color color, float borderWidth, Color borderColor, String characters) {
        return obtain(new Key(size, new Color(color), borderWidth, new Color(borderColor), distinct(characters), false));
    }

    /**
     * Comme {@link #jersey(int, Color, float, Color)}, avec les balises de couleur
     * ({@code [GOLD]…[]}) activées. Instance distincte de la police sans balises.
     */
    public static BitmapFont jerseyMarkup(int size, Color color, float borderWidth, Color borderColor) {
        return obtain(new Key(size, new Color(color), borderWidth, new Color(borderColor),
            FreeTypeFontGenerator.DEFAULT_CHARS, true));
    }

    /** Rend une police obtenue par {@code jersey} : elle est disposée quand plus personne ne s'en sert. */
    public static void release(BitmapFont font) {
        Entry entry = entries.get(font);
        if (entry == null) throw new IllegalArgumentException("Police inconnue ou déjà rendue");
        if (--entry.users > 0) return;
        entries.remove(font);
        fonts.remove(entry.key);
        font.dispose();
        if (fonts.isEmpty() && generator != null) {
            generator.dispose();
            generator = null;
        }
    }

    /** @return les caractères de {@code text}, sans doublon et triés (même clé pour un même jeu de lettres). */
    private static String distinct(String text) {
        StringBuilder chars = new StringBuilder();
        text.chars().distinct().sorted().forEach(c -> chars.append((char) c));
        return chars.toString();
    }

    private static BitmapFont obtain(Key key) {
        BitmapFont font = fonts.get(key);
        if (font == null) {
            font = generate(key);
            fonts.put(key, font);
            entries.put(font, new Entry(key));
        }
        entries.get(font).users++;
        return font;
    }

    private static BitmapFont generate(Key key) {
        if (generator == null) generator = new FreeTypeFontGenerator(Gdx.files.internal(JERSEY_PATH));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size        = key.size();
        parameter.color       = key.color();
        parameter.borderWidth = key.borderWidth();
        parameter.borderColor = key.borderColor();
        parameter.characters  = key.characters();
        BitmapFont font = generator.generateFont(parameter);
        font.getData().markupEnabled = key.markup();
        return font;
    }

    private Fonts() {}
}
