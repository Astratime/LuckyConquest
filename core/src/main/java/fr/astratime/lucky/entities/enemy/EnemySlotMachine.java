package fr.astratime.lucky.entities.enemy;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Machine à sous de l'ennemi : trois rouleaux, chacun tirant un des symboles
 * de l'ennemi (voir {@link EnemyKind#getWeights()}), selon son poids. Les
 * cartes Trèfle rendent un symbole plus probable.
 */
public class EnemySlotMachine {

    /** Nombre de rouleaux. */
    public static final int SYMBOL_COUNT = 3;
    /** Rouleaux au plus (la Machine Originelle, quand elle a volé un rouleau au joueur). */
    public static final int MAX_SYMBOL_COUNT = 6;

    private final Random random;

    public EnemySlotMachine(Random random) {
        this.random = random;
    }

    /**
     * @param luck chance ajoutée à certains symboles, en pourcentage de leur
     *             chance de base (ex : +50 : une fois et demie plus probable)
     * @return les symboles arrêtés sur les trois rouleaux
     */
    public EnemySymbol[] spin(Map<EnemySymbol, Integer> luck) {
        return spin(EnemyKind.CROUPIER.getWeights(), luck);
    }

    /**
     * @param weights symboles des rouleaux et leur poids de base
     * @param luck    chance ajoutée à certains symboles, en pourcentage de leur chance de base
     * @return les symboles arrêtés sur les trois rouleaux
     */
    public EnemySymbol[] spin(Map<EnemySymbol, Integer> weights, Map<EnemySymbol, Integer> luck) {
        return spin(weights, luck, SYMBOL_COUNT);
    }

    /**
     * Comme {@link #spin(Map, Map)}, avec {@code count} rouleaux (la Machine
     * Originelle en a cinq, puis six).
     */
    public EnemySymbol[] spin(Map<EnemySymbol, Integer> weights, Map<EnemySymbol, Integer> luck, int count) {
        EnemySymbol[] result = new EnemySymbol[count];
        List<EnemySymbol> symbols = List.copyOf(weights.keySet());
        float total = 0f;
        for (EnemySymbol symbol : symbols) total += weight(symbol, weights, luck);
        for (int i = 0; i < result.length; i++) {
            float roll = random.nextFloat() * total;
            result[i] = symbols.get(symbols.size() - 1);
            for (EnemySymbol symbol : symbols) {
                roll -= weight(symbol, weights, luck);
                if (roll < 0f) {
                    result[i] = symbol;
                    break;
                }
            }
        }
        return result;
    }

    private static float weight(EnemySymbol symbol, Map<EnemySymbol, Integer> weights, Map<EnemySymbol, Integer> luck) {
        return weights.get(symbol) * (1f + luck.getOrDefault(symbol, 0) / 100f);
    }
}
