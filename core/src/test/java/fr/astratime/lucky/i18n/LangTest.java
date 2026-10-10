package fr.astratime.lucky.i18n;

import fr.astratime.lucky.entities.CardFamily;
import fr.astratime.lucky.entities.Combo;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.entities.exploration.PlaceRule;
import fr.astratime.lucky.entities.tower.Chapter;
import fr.astratime.lucky.progress.Rank;
import fr.astratime.lucky.settings.ScreenMode;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La traduction anglaise : chaque texte du jeu doit y avoir sa traduction, avec
 * les mêmes valeurs ({0}, {1}…). Un texte ajouté sans traduction fait échouer ces tests.
 */
class LangTest {

    /** Les tests du module core s'exécutent depuis core/ : les assets sont à côté. */
    private static final Path ASSETS  = Path.of("..", "assets");
    private static final Path SOURCES = Path.of("src", "main", "java");
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\d}");

    @AfterEach
    void backToFrench() {
        Lang.use(Lang.Language.FRENCH, "{}");
    }

    private static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError(path + " illisible", e);
        }
    }

    private static Map<String, String> english() {
        return Lang.parse(read(ASSETS.resolve("i18n/en.json")));
    }

    @Test
    void frenchIsTheDefaultAndNeedsNoTranslation() {
        assertEquals(Lang.Language.FRENCH, Lang.get());
        assertEquals("Retour", Lang.t("Retour"));
        assertEquals("Encore 3 tours.", Lang.f("Encore {0}.", "3 tours"));
    }

    @Test
    void englishTranslatesAndMovesTheValues() {
        Lang.use(Lang.Language.ENGLISH, "{\"Retour\": \"Back\", \"Encore {0}.\": \"{0} left.\"}");
        assertEquals("Back", Lang.t("Retour"));
        assertEquals("3 turns left.", Lang.f("Encore {0}.", "3 turns"));
        // Sans traduction : le texte reste en français.
        assertEquals("Texte inconnu", Lang.t("Texte inconnu"));
    }

    @Test
    void languageCycleAndCodes() {
        assertEquals(Lang.Language.ENGLISH, Lang.Language.FRENCH.next());
        assertEquals(Lang.Language.FRENCH, Lang.Language.ENGLISH.next());
        assertEquals(Lang.Language.ENGLISH, Lang.Language.parse("en"));
        assertEquals(Lang.Language.FRENCH, Lang.Language.parse("??"));
    }

    @Test
    void translationsKeepTheSameValues() {
        for (Map.Entry<String, String> entry : english().entrySet()) {
            assertEquals(placeholders(entry.getKey()), placeholders(entry.getValue()),
                "Valeurs différentes dans la traduction de : " + entry.getKey());
        }
    }

    @Test
    void everyTextOfTheCodeIsTranslated() throws IOException {
        Map<String, String> english = english();
        List<String> missing = new ArrayList<>();
        List<Path> files;
        try (Stream<Path> walk = Files.walk(SOURCES)) {
            files = walk.filter(path -> path.toString().endsWith(".java")).toList();
        }
        int found = 0;
        for (Path file : files) {
            for (String key : keys(read(file))) {
                found++;
                if (!english.containsKey(key)) missing.add(file.getFileName() + " : " + key);
            }
        }
        assertTrue(found > 500, "Les textes du code n'ont pas été trouvés");
        assertEquals(List.of(), missing, "Textes sans traduction anglaise");
    }

    @Test
    void everyCardNameIsTranslated() throws IOException {
        Map<String, String> english = english();
        List<String> missing = new ArrayList<>();
        try (Stream<Path> walk = Files.list(ASSETS.resolve("cards/definitions"))) {
            for (Path file : walk.toList()) {
                for (JsonValue card = new JsonReader().parse(read(file)).child; card != null; card = card.next) {
                    String name = card.getString("name", null);
                    if (name != null && !english.containsKey(name)) missing.add(name);
                }
            }
        }
        assertEquals(List.of(), missing, "Cartes sans nom anglais");
    }

    @Test
    void everyGameDataTextIsTranslated() {
        Map<String, String> english = english();
        List<String> missing = new ArrayList<>();
        List<Supplier<String>> texts = new ArrayList<>();
        for (EnemyKind kind : EnemyKind.values()) {
            texts.add(kind::getDisplayName);
            texts.add(kind::getBarName);
            texts.add(kind::getDescription);
        }
        for (EnemySymbol symbol : EnemySymbol.values()) texts.add(symbol::getDisplayName);
        for (Symbol symbol : Symbol.values()) texts.add(symbol::getDisplayName);
        for (Dungeon dungeon : Dungeon.values()) {
            texts.add(dungeon::getName);
            texts.add(dungeon::getShortName);
            texts.add(dungeon::getDescription);
        }
        for (Place place : Place.values()) {
            texts.add(place::getName);
            texts.add(place::getShortName);
            texts.add(place::getDescription);
        }
        for (PlaceRule rule : PlaceRule.values()) {
            texts.add(rule::getName);
            texts.add(rule::getDescription);
        }
        for (Chapter chapter : Chapter.values()) {
            texts.add(chapter::getTitle);
            texts.add(chapter::getDescription);
            texts.add(chapter::getEnding);
        }
        for (Rank rank : Rank.values()) {
            texts.add(rank::getTitle);
            texts.add(rank::getDescription);
        }
        for (Combo combo : Combo.values()) {
            texts.add(combo::getDisplayName);
            texts.add(combo::getRule);
        }
        for (CardFamily family : CardFamily.values()) texts.add(family::getDisplayName);
        for (ScreenMode mode : ScreenMode.values()) texts.add(mode::getLabel);
        for (LastingEffects.HouseRule rule : LastingEffects.HouseRule.values()) {
            texts.add(rule::getShortName);
            texts.add(rule::getAnnounce);
        }
        for (fr.astratime.lucky.progress.Achievement achievement : fr.astratime.lucky.progress.Achievement.values()) {
            texts.add(achievement::getName);
            texts.add(achievement::getDescription);
        }
        for (Supplier<String> text : texts) {
            String french = text.get();
            if (french != null && !english.containsKey(french)) missing.add(french);
        }
        assertEquals(List.of(), missing, "Données du jeu sans traduction anglaise");
    }

    private static TreeSet<String> placeholders(String text) {
        TreeSet<String> found = new TreeSet<>();
        Matcher matcher = PLACEHOLDER.matcher(text);
        while (matcher.find()) found.add(matcher.group());
        return found;
    }

    /**
     * @return les textes passés à {@code Lang.t(…)} ou {@code Lang.f(…)} dans {@code source} : chaque
     *         chaîne (ou suite de chaînes jointes par {@code +}) de leur premier argument
     */
    static List<String> keys(String source) {
        List<String> keys = new ArrayList<>();
        int at = 0;
        while ((at = nextCall(source, at)) >= 0) {
            int depth = 0;
            int i = at;
            StringBuilder key = null;
            while (i < source.length()) {
                char c = source.charAt(i);
                if (c == '"') {
                    int end = i + 1;
                    StringBuilder literal = new StringBuilder();
                    while (source.charAt(end) != '"') {
                        char d = source.charAt(end);
                        if (d == '\\') {
                            char e = source.charAt(++end);
                            literal.append(switch (e) {
                                case 'n' -> '\n';
                                case 't' -> '\t';
                                default -> e;
                            });
                        } else {
                            literal.append(d);
                        }
                        end++;
                    }
                    if (depth == 0) {
                        if (key == null) key = new StringBuilder();
                        key.append(literal);
                    }
                    i = end + 1;
                    continue;
                }
                if (c == '+' || Character.isWhitespace(c)) {
                    i++;
                    continue;
                }
                if (key != null) {
                    keys.add(key.toString());
                    key = null;
                }
                if (c == '(' || c == '[' || c == '{') depth++;
                if (c == ')' || c == ']' || c == '}') {
                    if (depth == 0) break;
                    depth--;
                }
                if (c == ',' && depth == 0) break;
                i++;
            }
            if (key != null) keys.add(key.toString());
        }
        return keys;
    }

    /** @return la position juste après le prochain {@code Lang.t(} ou {@code Lang.f(}, ou -1. */
    private static int nextCall(String source, int from) {
        int t = source.indexOf("Lang.t(", from);
        int f = source.indexOf("Lang.f(", from);
        int next = t < 0 ? f : f < 0 ? t : Math.min(t, f);
        return next < 0 ? -1 : next + "Lang.t(".length();
    }
}
