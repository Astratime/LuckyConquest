package fr.astratime.lucky.progress;

/**
 * Où le {@link PlayerProfile} est enregistré entre deux lancements du jeu :
 * quelques valeurs texte, par clé. Dans le jeu, les préférences de libGDX
 * ({@link GdxProfileStorage}) ; dans les tests, une simple table en mémoire.
 */
public interface ProfileStorage {

    /** @return la valeur de {@code key}, ou {@code null} si elle n'a jamais été enregistrée. */
    String get(String key);

    /** Change la valeur de {@code key} (écrite pour de bon par {@link #flush()}). */
    void put(String key, String value);

    /** Écrit les changements sur le disque. */
    void flush();
}
