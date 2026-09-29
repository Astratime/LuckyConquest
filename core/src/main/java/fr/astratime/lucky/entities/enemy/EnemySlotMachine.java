package fr.astratime.lucky.entities.enemy;

import java.util.Map;
import java.util.Random;

/**
 * Machine à sous de l'ennemi : trois rouleaux, chacun tirant une Épée, un
 * Bouclier ou une Potion. Les cartes Trèfle rendent un symbole plus probable.
 */
public class EnemySlotMachine {

    /** Nombre de rouleaux. */
    public static final int SYMBOL_COUNT = 3;

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
        EnemySymbol[] result = new EnemySymbol[SYMBOL_COUNT];
        float total = 0f;
        for (EnemySymbol symbol : EnemySymbol.values()) total += weight(symbol, luck);
        for (int i = 0; i < result.length; i++) {
            float roll = random.nextFloat() * total;
            result[i] = EnemySymbol.values()[EnemySymbol.values().length - 1];
            for (EnemySymbol symbol : EnemySymbol.values()) {
                roll -= weight(symbol, luck);
                if (roll < 0f) {
                    result[i] = symbol;
                    break;
                }
            }
        }
        return result;
    }

    private static float weight(EnemySymbol symbol, Map<EnemySymbol, Integer> luck) {
        return 1f + luck.getOrDefault(symbol, 0) / 100f;
    }
}
