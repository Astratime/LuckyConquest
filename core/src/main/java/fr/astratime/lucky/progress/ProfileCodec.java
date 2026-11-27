package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.enemy.EnemyKind;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Écriture et lecture des tables de la sauvegarde (« id:n,id:n », « NOM,NOM »…).
 * Sorti de {@link PlayerProfile}, sans changer le format : les vieilles sauvegardes se lisent toujours.
 */
final class ProfileCodec {

    private ProfileCodec() { }

    /** @return {@code wins} écrit "ROI:nombre,ROI:nombre". */
    static String encodeKingWins(Map<EnemyKind, Integer> wins) {
        return wins.entrySet().stream().map(entry -> entry.getKey().name() + ":" + entry.getValue())
            .collect(Collectors.joining(","));
    }

    /** @return les victoires lues dans {@code text} (voir {@link #encodeKingWins(Map)}) ; les entrées illisibles sont ignorées. */
    static Map<EnemyKind, Integer> decodeKingWins(String text) {
        Map<EnemyKind, Integer> wins = new EnumMap<>(EnemyKind.class);
        if (text == null || text.isBlank()) return wins;
        for (String entry : text.split(",")) {
            int colon = entry.indexOf(':');
            if (colon <= 0) continue;
            try {
                int count = Integer.parseInt(entry.substring(colon + 1).trim());
                if (count > 0) wins.put(EnemyKind.valueOf(entry.substring(0, colon).trim()), count);
            } catch (IllegalArgumentException ignored) { }
        }
        return wins;
    }

    static void readEnemies(String text, Set<EnemyKind> enemies) {
        Set<String> names = new LinkedHashSet<>();
        readNames(text, names);
        for (EnemyKind kind : EnemyKind.values()) if (names.contains(kind.name())) enemies.add(kind);
    }

    /** @return {@code pots} écrit "CLE:montant,CLE:montant". */
    static String encodePots(Map<String, Double> pots) {
        StringBuilder text = new StringBuilder();
        pots.forEach((key, amount) -> {
            if (!(amount > 0)) return;
            if (text.length() > 0) text.append(',');
            text.append(key).append(':').append(amount);
        });
        return text.toString();
    }

    /** @return les cagnottes écrites par {@link #encodePots(Map)} (les entrées illisibles sont ignorées). */
    static Map<String, Double> decodePots(String text) {
        Map<String, Double> pots = new LinkedHashMap<>();
        if (text == null || text.isBlank()) return pots;
        for (String entry : text.split(",")) {
            int colon = entry.lastIndexOf(':');
            if (colon <= 0) continue;
            try {
                double amount = Double.parseDouble(entry.substring(colon + 1).trim());
                if (amount > 0 && Double.isFinite(amount)) pots.put(entry.substring(0, colon).trim(), amount);
            } catch (NumberFormatException ignored) {
                // entrée abîmée : ignorée
            }
        }
        return pots;
    }

    /** Ajoute à {@code names} les noms de {@code text} ("a,b,c" ; rien si {@code null}). */
    static void readNames(String text, Set<String> names) {
        if (text == null) return;
        for (String name : text.split(",")) if (!name.isBlank()) names.add(name.trim());
    }

    /** @return {@code copies} écrit "id:n,id:n" (dans l'ordre de la table). */
    static String encode(Map<String, Integer> copies) {
        StringBuilder text = new StringBuilder();
        copies.forEach((id, count) -> {
            if (count <= 0) return;
            if (text.length() > 0) text.append(',');
            text.append(id).append(':').append(count);
        });
        return text.toString();
    }

    /** @return la table écrite par {@link #encode(Map)} (les entrées illisibles sont ignorées). */
    static Map<String, Integer> decode(String text) {
        Map<String, Integer> copies = new LinkedHashMap<>();
        if (text.isBlank()) return copies;
        for (String entry : text.split(",")) {
            int colon = entry.lastIndexOf(':');
            if (colon <= 0) continue;
            try {
                int count = Integer.parseInt(entry.substring(colon + 1).trim());
                if (count > 0) copies.merge(entry.substring(0, colon).trim(), count, Integer::sum);
            } catch (NumberFormatException ignored) {
                // entrée abîmée : ignorée
            }
        }
        return copies;
    }

    /** @return {@code symbols} écrits "NOM,NOM". */
    static String encodeSymbols(List<Symbol> symbols) {
        return String.join(",", symbols.stream().map(Symbol::name).toList());
    }

    /** @return les symboles écrits par {@link #encodeSymbols(List)} (les noms inconnus sont ignorés). */
    static List<Symbol> decodeSymbols(String text) {
        List<Symbol> symbols = new ArrayList<>();
        if (text == null || text.isBlank()) return symbols;
        for (String name : text.split(",")) {
            try {
                symbols.add(Symbol.valueOf(name.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
                // nom abîmé : ignoré
            }
        }
        return symbols;
    }
}
