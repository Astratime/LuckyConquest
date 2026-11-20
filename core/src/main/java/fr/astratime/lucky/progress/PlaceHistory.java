package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.exploration.Place;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Historique de l'Exploration (2026-11-20) : pour chaque lieu, en mode normal et en mode difficile, les combats
 * joués et gagnés, les rois affrontés et battus, les pièces gagnées et le meilleur combat. Montré dans
 * « Statistiques et succès » pour voir où l'on gagne et où l'on bloque. Le mode ADMIN et le tutoriel n'y comptent pas.
 * Enregistré dans le profil sous forme de texte : {@code "PRAIRIE:12,9,4,3,52000,9000;PORT#D:..."}.
 */
public final class PlaceHistory {

    /** Une ligne de l'historique : un lieu, dans un mode. */
    public record Line(long fights, long wins, long kings, long kingWins, long coins, long best) {
        static final Line EMPTY = new Line(0, 0, 0, 0, 0, 0);

        /** @return la part de combats gagnés, en % (0 sans combat). */
        public int winPercent() { return fights == 0 ? 0 : (int) Math.round(100.0 * wins / fights); }

        /** @return les pièces gagnées en moyenne par combat (0 sans combat). */
        public long coinsPerFight() { return fights == 0 ? 0 : coins / fights; }

        Line plus(boolean king, boolean won, long reward) {
            long safe = Math.max(0, reward);
            return new Line(fights + 1, wins + (won ? 1 : 0), kings + (king ? 1 : 0), kingWins + (king && won ? 1 : 0),
                coins > Long.MAX_VALUE - safe ? Long.MAX_VALUE : coins + safe, Math.max(best, safe));
        }
    }

    private static final String HARD = "#D";

    private final Map<String, Line> lines = new LinkedHashMap<>();

    /** Un combat de l'Exploration terminé dans {@code place} ({@code hard} : mode difficile). */
    public void record(Place place, boolean hard, boolean king, boolean won, long coins) {
        lines.merge(key(place, hard), Line.EMPTY.plus(king, won, coins), (old, one) -> old.plus(king, won, coins));
    }

    /** @return la ligne de {@code place} dans ce mode (vide si aucun combat). */
    public Line get(Place place, boolean hard) { return lines.getOrDefault(key(place, hard), Line.EMPTY); }

    /** @return {@code true} si aucun combat n'est encore noté. */
    public boolean isEmpty() { return lines.isEmpty(); }

    private static String key(Place place, boolean hard) { return place.name() + (hard ? HARD : ""); }

    /** @return l'historique en texte, pour le profil. */
    public String encode() {
        StringBuilder text = new StringBuilder();
        lines.forEach((key, line) -> {
            if (!text.isEmpty()) text.append(';');
            text.append(key).append(':').append(line.fights).append(',').append(line.wins).append(',')
                .append(line.kings).append(',').append(line.kingWins).append(',').append(line.coins).append(',')
                .append(line.best);
        });
        return text.toString();
    }

    /** @return l'historique lu dans {@code text} ; les lignes illisibles ou d'un lieu disparu sont ignorées. */
    public static PlaceHistory decode(String text) {
        PlaceHistory history = new PlaceHistory();
        if (text == null || text.isBlank()) return history;
        for (String entry : text.split(";")) {
            int colon = entry.indexOf(':');
            if (colon <= 0) continue;
            String key = entry.substring(0, colon);
            String placeName = key.endsWith(HARD) ? key.substring(0, key.length() - HARD.length()) : key;
            String[] values = entry.substring(colon + 1).split(",");
            if (values.length != 6) continue;
            try {
                Place.valueOf(placeName);
                long[] v = new long[6];
                for (int i = 0; i < 6; i++) v[i] = Math.max(0, Long.parseLong(values[i].trim()));
                history.lines.put(key, new Line(v[0], v[1], v[2], v[3], v[4], v[5]));
            } catch (IllegalArgumentException ignored) {
                // lieu inconnu ou nombre illisible : la ligne est oubliée
            }
        }
        return history;
    }
}
