package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.Symbol;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Statistiques de la partie, montrées dans les Options et comptées pour les
 * succès. Le mode ADMIN et le tutoriel n'y comptent pas (voir {@link PlayerProfile}).
 * Enregistrées dans le profil sous forme de texte : {@code "WINS:12,BINGOS:3,..."}.
 */
public final class PlayerStats {

    /** Les compteurs, dans l'ordre où les Options les montrent. */
    public enum Stat {
        /** Combats gagnés, tous modes confondus. */
        WINS,
        /** Plus gros coup infligé à un ennemi. */
        BEST_HIT,
        /** Bingos réussis. */
        BINGOS,
        /** Jeux bonus joués. */
        BONUS_GAMES,
        /** Gains gagnés en combat, au total (ce qui est dépensé ne se retire pas). */
        GAINS,
        /** Pièces gagnées à la fin des combats de l'Exploration. */
        COINS,
        /** Carrés réussis. */
        SQUARES
    }

    private final Map<Stat, Long> values  = new EnumMap<>(Stat.class);
    /** Symboles dont le joueur a déjà fait le Bingo. */
    private final Set<Symbol>     bingoSymbols = EnumSet.noneOf(Symbol.class);

    /** @return la valeur de {@code stat} (0 au départ). */
    public long get(Stat stat) { return values.getOrDefault(stat, 0L); }

    /** Ajoute {@code amount} à {@code stat} (rien si négatif). */
    public void add(Stat stat, long amount) {
        if (amount > 0) values.merge(stat, amount, PlayerStats::saturatedAdd);
    }

    /** Garde {@code value} si elle dépasse le record de {@code stat}. */
    public void record(Stat stat, long value) {
        if (value > get(stat)) values.put(stat, value);
    }

    /** Un Bingo de {@code symbol} : compté, et le symbole rejoint ceux déjà réussis. */
    public void addBingo(Symbol symbol) {
        add(Stat.BINGOS, 1);
        bingoSymbols.add(symbol);
    }

    /** @return le nombre de symboles différents dont le Bingo a été réussi. */
    public int bingoSymbolCount() { return bingoSymbols.size(); }

    /** @return les statistiques en texte, pour le profil. */
    public String encode() {
        StringBuilder text = new StringBuilder();
        values.forEach((stat, value) -> text.append(text.isEmpty() ? "" : ",").append(stat.name()).append(':').append(value));
        StringBuilder symbols = new StringBuilder();
        for (Symbol symbol : bingoSymbols) symbols.append(symbols.isEmpty() ? "" : "+").append(symbol.name());
        if (!symbols.isEmpty()) text.append(text.isEmpty() ? "" : ",").append("SYMBOLS:").append(symbols);
        return text.toString();
    }

    /** @return les statistiques lues dans {@code text} (vides si {@code null}) ; ce qui est illisible est ignoré. */
    public static PlayerStats decode(String text) {
        PlayerStats stats = new PlayerStats();
        if (text == null) return stats;
        for (String part : text.split(",")) {
            int colon = part.indexOf(':');
            if (colon < 0) continue;
            String name  = part.substring(0, colon).trim();
            String value = part.substring(colon + 1).trim();
            if (name.equals("SYMBOLS")) {
                for (String symbol : value.split("\\+")) {
                    try { stats.bingoSymbols.add(Symbol.valueOf(symbol)); } catch (IllegalArgumentException ignored) { }
                }
                continue;
            }
            try {
                stats.values.put(Stat.valueOf(name), Math.max(0L, Long.parseLong(value)));
            } catch (IllegalArgumentException ignored) { } // NumberFormatException aussi
        }
        return stats;
    }

    private static long saturatedAdd(long a, long b) {
        long sum = a + b;
        return sum < 0 ? Long.MAX_VALUE : sum;
    }
}
