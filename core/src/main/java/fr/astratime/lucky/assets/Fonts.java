package fr.astratime.lucky.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

/** Fabrique des polices pixel art du thème casino, générées à la taille voulue pour rester nettes. */
public final class Fonts {

    private static final String JERSEY_PATH = "fonts/Jersey10-Regular.ttf";

    /**
     * @return la police Jersey 10 à la taille {@code size} (en pixels), de la couleur
     *         donnée. L'appelant en est propriétaire et doit la disposer.
     */
    public static BitmapFont jersey(int size, Color color) {
        return jersey(size, color, 0, Color.BLACK);
    }

    /**
     * @param borderWidth épaisseur du contour (en pixels), 0 pour aucun contour
     * @return la police Jersey 10 à la taille {@code size}, entourée d'un contour
     *         {@code borderColor} pour rester lisible sur un fond coloré. L'appelant
     *         en est propriétaire et doit la disposer.
     */
    public static BitmapFont jersey(int size, Color color, float borderWidth, Color borderColor) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(JERSEY_PATH));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size        = size;
        parameter.color       = color;
        parameter.borderWidth = borderWidth;
        parameter.borderColor = borderColor;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose(); // le générateur ne sert plus une fois la police créée
        return font;
    }

    private Fonts() {}
}
