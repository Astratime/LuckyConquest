package fr.astratime.lucky.i18n;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Langue du jeu, réglée dans les options du menu principal.
 *
 * Les textes du jeu sont écrits en français dans le code : le texte français
 * sert de clé. {@link #t(String)} renvoie sa traduction dans la langue
 * choisie, lue dans {@code i18n/<code>.json} ({@code {"Retour": "Back"}}).
 * Un texte sans traduction reste en français.
 *
 * Pour un texte qui contient des valeurs, {@link #f(String, Object...)} prend
 * un modèle où {@code {0}}, {@code {1}}… marquent leur place : la traduction
 * peut les déplacer (« Encore {0}. » → « {0} left. »).
 */
public final class Lang {

    /** Langues proposées dans les options. Le français est la langue d'origine du jeu. */
    public enum Language {
        FRENCH("fr", "Français"),
        ENGLISH("en", "English");

        /** Code de la langue, nom de son fichier de traductions (ex : {@code en} → {@code i18n/en.json}). */
        public final String code;
        /** Nom de la langue dans sa propre langue, tel qu'affiché dans les options. */
        public final String label;

        Language(String code, String label) {
            this.code  = code;
            this.label = label;
        }

        /** @return la langue suivante de la liste (bascule des options). */
        public Language next() { return values()[(ordinal() + 1) % values().length]; }

        /** @return la langue de code {@code code}, ou le français s'il est inconnu. */
        public static Language parse(String code) {
            for (Language language : values()) {
                if (language.code.equals(code)) return language;
            }
            return FRENCH;
        }
    }

    private static final String PREFERENCES = "lucky-conquest";
    private static final String KEY         = "language";

    private static Language            current = Language.FRENCH;
    private static Map<String, String> table   = Map.of();

    private Lang() { }

    /** Applique la langue mémorisée (au lancement du jeu). */
    public static void load() {
        Preferences preferences = Gdx.app.getPreferences(PREFERENCES);
        apply(Language.parse(preferences.getString(KEY, Language.FRENCH.code)));
    }

    /** Change de langue et mémorise le choix. */
    public static void set(Language language) {
        apply(language);
        Preferences preferences = Gdx.app.getPreferences(PREFERENCES);
        preferences.putString(KEY, language.code);
        preferences.flush();
    }

    /** @return la langue en cours. */
    public static Language get() { return current; }

    private static void apply(Language language) {
        current = language;
        if (language == Language.FRENCH) {
            table = Map.of();
            return;
        }
        FileHandle file = Gdx.files.internal("i18n/" + language.code + ".json");
        table = file.exists() ? parse(file.readString("UTF-8")) : Map.of();
    }

    /**
     * Utilise directement une table de traductions (tests, sans libGDX).
     *
     * @param json contenu d'un fichier {@code i18n/<code>.json}, ignoré pour le français
     */
    public static void use(Language language, String json) {
        current = language;
        table   = language == Language.FRENCH ? Map.of() : parse(json);
    }

    /** @return les traductions d'un fichier {@code i18n/<code>.json} : texte français → texte traduit. */
    public static Map<String, String> parse(String json) {
        Map<String, String> result = new HashMap<>();
        for (JsonValue entry = new JsonReader().parse(json).child; entry != null; entry = entry.next) {
            String translated = entry.asString();
            if (translated != null && !translated.isEmpty()) result.put(entry.name, translated);
        }
        return result;
    }

    /** @return {@code french} dans la langue choisie (inchangé s'il n'a pas de traduction). */
    public static String t(String french) {
        if (french == null) return null;
        String translated = table.get(french);
        return translated != null ? translated : french;
    }

    /**
     * @param template texte français où {@code {0}}, {@code {1}}… marquent la place des valeurs
     * @return le modèle traduit, chaque marque remplacée par sa valeur
     */
    public static String f(String template, Object... args) {
        String text = t(template);
        if (args.length == 0) return text;
        StringBuilder result = new StringBuilder(text.length() + 16);
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '{' && i + 2 < text.length() && Character.isDigit(text.charAt(i + 1)) && text.charAt(i + 2) == '}') {
                int index = text.charAt(i + 1) - '0';
                result.append(index < args.length ? String.valueOf(args[index]) : "");
                i += 3;
            } else {
                result.append(c);
                i++;
            }
        }
        return result.toString();
    }

    /** @return le nombre décimal {@code number} (écrit avec un point) avec la virgule française, ou le point anglais. */
    public static String decimal(String number) {
        return isEnglish() ? number : number.replace('.', ',');
    }

    /** @return {@code number} avec le séparateur des milliers (12 500 ; en anglais 12,500). */
    public static String grouped(long number) {
        String digits = Long.toString(Math.abs(number));
        StringBuilder text = new StringBuilder(number < 0 ? "-" : "");
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && (digits.length() - i) % 3 == 0) text.append(thousands());
            text.append(digits.charAt(i));
        }
        return text.toString();
    }

    /**
     * @return {@code number} abrégé à partir d'un million, avec une décimale au
     *         plus : 5 500 000 → « 5,5 M » (« 5.5M » en anglais), 2 000 000 000 →
     *         « 2 Md » (« 2B ») ; en dessous, avec le séparateur des milliers
     */
    public static String big(long number) {
        long size = Math.abs(number);
        if (size < 1_000_000L) return grouped(number);
        boolean billions = size >= 999_950_000L;
        long tenths = Math.round(number / (billions ? 100_000_000d : 100_000d));
        String value = tenths % 10 == 0 ? grouped(tenths / 10) : grouped(tenths / 10) + (isEnglish() ? "." : ",") + Math.abs(tenths % 10);
        if (tenths < 0 && tenths / 10 == 0) value = "-" + value;
        if (isEnglish()) return value + (billions ? "B" : "M");
        return value + (billions ? " Md" : " M");
    }

    /** @return le séparateur des milliers : une espace en français, une virgule en anglais (1 000 / 1,000). */
    public static char thousands() { return isEnglish() ? ',' : ' '; }

    /**
     * @return {@code true} si {@code count} s'écrit au pluriel : au-delà de 1 en français
     *         (0 tour, 1 tour, 2 tours), dès qu'il n'est pas 1 en anglais (0 turns, 1 turn)
     */
    public static boolean plural(long count) {
        return isEnglish() ? count != 1 : count > 1;
    }

    /** @return {@code true} si la langue choisie est l'anglais (formats de nombres, pluriels). */
    public static boolean isEnglish() { return current == Language.ENGLISH; }
}
