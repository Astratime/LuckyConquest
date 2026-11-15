package fr.astratime.lucky.entities.exploration;

import fr.astratime.lucky.entities.enemy.Difficulty;
import fr.astratime.lucky.entities.enemy.EnemyKind;

import java.util.ArrayList;
import java.util.List;

/**
 * Mode difficile de l'Exploration (Astra, 2026-11-15) : ouvert une fois le Désert de Braise vidé, il a sa propre
 * progression (chaque lieu difficile s'ouvre quand les quatre donjons du précédent sont vidés en difficile).
 *
 * <p>Le 1er lieu difficile (la prairie) a la difficulté du dernier lieu en mode normal (le Désert) : en moyenne
 * sur ses huit ennemis, les mêmes PV, la même défense et la même attaque. Chaque lieu difficile suivant est deux
 * fois plus fort que le précédent (x2 au 2e lieu, x4 au 3e … x32 au Désert difficile). Les écarts entre les donjons
 * d'un même lieu sont gardés : un ennemi garde ses PV, sa défense et son attaque par rapport à ceux de son lieu.
 *
 * <p>Les pièces suivent : un tirage coûte celui du Désert x2 par lieu, fois {@link #COINS_PERCENT} %, et tous les
 * gains du combat, le coffre et la part gardée après une défaite se comptent à partir de ce coût.
 */
public final class HardExploration {

    /**
     * Prime de pièces du mode difficile, en % du coût de tirage qui suit la difficulté (100 : les pièces grimpent
     * comme la force des ennemis, rien de plus). Valeur proposée, à confirmer par Astra.
     */
    public static final int COINS_PERCENT = 100;

    private HardExploration() { }

    /** @return la force du lieu {@code place} en mode difficile par rapport au Désert en normal : x1, x2, x4 … x32. */
    public static long strength(Place place) { return 1L << place.ordinal(); }

    /** @return {@code true} si {@code kind} combat en Exploration difficile à la difficulté {@code difficulty}. */
    public static boolean applies(EnemyKind kind, Difficulty difficulty) {
        return difficulty.hard() && !difficulty.isEndless() && Place.of(kind) != null;
    }

    /** @return le multiplicateur des PV des ennemis de {@code place} en mode difficile. */
    public static double hpFactor(Place place) { return Factors.HP[place.ordinal()]; }

    /** @return le multiplicateur de l'attaque des ennemis de {@code place} en mode difficile. */
    public static double attackFactor(Place place) { return Factors.ATTACK[place.ordinal()]; }

    /** @return le multiplicateur de la défense des ennemis de {@code place} en mode difficile. */
    public static double defenseFactor(Place place) { return Factors.DEFENSE[place.ordinal()]; }

    /** @return le coût d'un tirage dans le lieu {@code place} difficile : le Désert x2 par lieu, prime comprise. */
    public static int spinCost(Place place) {
        return (int) Math.min(Integer.MAX_VALUE,
            (long) Place.DESERT.getSpinCost() * strength(place) * COINS_PERCENT / 100);
    }

    /**
     * @return le coût de tirage de force du lieu difficile (voir {@link Place#getPowerSpinCost()}) : celui du Désert
     *         x2 par lieu, comme ses ennemis (la prime de pièces n'y compte pas)
     */
    public static int powerSpinCost(Place place) {
        return (int) Math.min(Integer.MAX_VALUE, Place.DESERT.getPowerSpinCost() * strength(place));
    }

    /** Les multiplicateurs, calculés une fois sur les fiches des ennemis en mode normal. */
    private static final class Factors {
        static final double[] HP = new double[Place.values().length];
        static final double[] ATTACK = new double[Place.values().length];
        static final double[] DEFENSE = new double[Place.values().length];

        static {
            double desertHp = averageHp(Place.DESERT), desertAttack = averageAttack(Place.DESERT),
                desertDefense = averageDefense(Place.DESERT);
            for (Place place : Place.values()) {
                long strength = strength(place);
                HP[place.ordinal()] = ratio(desertHp, averageHp(place)) * strength;
                ATTACK[place.ordinal()] = ratio(desertAttack, averageAttack(place)) * strength;
                DEFENSE[place.ordinal()] = ratio(desertDefense, averageDefense(place)) * strength;
            }
        }

        private static double ratio(double target, double own) { return own > 0 ? target / own : 1; }

        private static List<EnemyKind> enemies(Place place) {
            List<EnemyKind> enemies = new ArrayList<>();
            for (Dungeon dungeon : place.getDungeons()) {
                enemies.add(dungeon.getSoldier());
                enemies.add(dungeon.getKing());
            }
            return enemies;
        }

        /** PV max moyens des soldats et rois du lieu, en mode normal. */
        private static double averageHp(Place place) {
            return enemies(place).stream().mapToDouble(kind -> kind.getMaxHp(Difficulty.NORMAL)).average().orElse(0);
        }

        /** Coup d'Épée moyen qui porte (Épée du lieu x part qui porte), en mode normal. */
        private static double averageAttack(Place place) {
            return enemies(place).stream().mapToDouble(kind -> kind.hurt(kind.swordDamage(), Difficulty.NORMAL))
                .average().orElse(0);
        }

        /** Défense de base moyenne, en mode normal. */
        private static double averageDefense(Place place) {
            return enemies(place).stream().mapToDouble(kind -> kind.getBaseDefense(Difficulty.NORMAL)).average().orElse(0);
        }
    }
}
