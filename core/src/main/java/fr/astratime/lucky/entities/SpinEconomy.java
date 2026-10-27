package fr.astratime.lucky.entities;

import fr.astratime.lucky.i18n.Lang;

import java.util.Locale;

/**
 * Les gains en combat : chaque tirage coûte des gains, et ce que rapportent
 * les Paires, les Bingos et les symboles de gains se compte en coûts de
 * tirage, pour suivre le lieu (ou le chapitre de la Tour) tout seul.
 * <ul>
 *   <li>Coût d'un tirage (« C ») : voir EnemyKind#getSpinCost (100 à la Prairie, 5 000 au Casino).</li>
 *   <li>Départ d'un combat : {@link #STARTING_SPINS} C de gains (une fois par chapitre de la Tour).</li>
 *   <li>Paire : {@link #PAIR_SPINS} C ; Bingo : 20, 30 ou 50 C selon la {@linkplain Symbol.Rarity rareté}.</li>
 *   <li>Dette : seul le coût du tirage fait passer les gains sous 0. Endetté (jusqu'à
 *       -{@link #BAILIFF_SPINS} C) : symboles -25 % ; Huissier (au-delà) : -50 %, et l'ennemi joue une carte de plus.</li>
 *   <li>Mise : avant le tirage, une part des gains part dans la cagnotte du Jeu bonus
 *       ({@link BonusPot}) et renforce l'attaque et la défense du tour ({@link Stake}).</li>
 * </ul>
 */
public final class SpinEconomy {

    /** Coût d'un tirage de base (Entraînement, Prairie, chapitres 1 et 2) : les gains des symboles y valent leur base. */
    public static final int BASE_SPIN_COST = 100;
    /** Gains au début d'un combat, en coûts de tirage. */
    public static final int STARTING_SPINS = 5;
    /** Gains d'une Paire, en coûts de tirage. */
    public static final int PAIR_SPINS = 3;
    /** Dette (en coûts de tirage) à partir de laquelle l'Huissier remplace l'Endetté. */
    public static final int BAILIFF_SPINS = 5;
    /** Facteur des symboles d'attaque et de défense quand le joueur est endetté. */
    public static final float INDEBTED_FACTOR = 0.75f;
    /** Facteur des symboles d'attaque et de défense quand l'Huissier est là. */
    public static final float BAILIFF_FACTOR = 0.5f;

    private SpinEconomy() { }

    /** Ce que la dette fait au joueur. */
    public enum Debt {
        /** Gains à 0 ou plus : rien. */
        NONE(1f),
        /** Un peu de dette : attaque et bouclier des symboles -25 %. */
        INDEBTED(INDEBTED_FACTOR),
        /** Beaucoup de dette : -50 %, et l'ennemi joue une carte de plus. */
        BAILIFF(BAILIFF_FACTOR);

        /** Facteur de l'attaque et du bouclier des symboles. */
        public final float factor;

        Debt(float factor) { this.factor = factor; }
    }

    /** @return la dette du joueur qui a {@code gains} gains, quand un tirage coûte {@code spinCost}. */
    public static Debt debt(double gains, int spinCost) {
        if (gains >= 0) return Debt.NONE;
        return gains <= -(double) BAILIFF_SPINS * spinCost ? Debt.BAILIFF : Debt.INDEBTED;
    }

    /** @return les gains de départ d'un combat où un tirage coûte {@code spinCost}. */
    public static int startingGains(int spinCost) { return STARTING_SPINS * spinCost; }

    /** @return les gains d'une Paire. */
    public static int pairGains(int spinCost) { return PAIR_SPINS * spinCost; }

    /** @return les gains d'un Bingo de {@code symbol}. */
    public static int jackpotGains(Symbol symbol, int spinCost) { return symbol.getRarity().jackpotSpins * spinCost; }

    /** @return le facteur des gains des symboles de gains : ils suivent le coût du tirage (x1 pour 100). */
    public static float gainScale(int spinCost) { return spinCost / (float) BASE_SPIN_COST; }

    /**
     * Paliers de la Mise (choix d'Astra, 2026-10-27) : posée avant le tirage, elle
     * prend {@link #percent} % des gains (en plus du prix du tirage), qui partent dans
     * la cagnotte du Jeu bonus ({@link BonusPot}), et multiplie l'attaque et la
     * défense du tour par {@link #power}. Elle ne touche pas aux gains du tirage.
     */
    public enum Stake {
        NONE(0, 1f),
        LOW(5, 1.1f),
        MEDIUM(10, 1.2f),
        HIGH(15, 1.3f);

        /** Part des gains misée, en %. */
        public final int   percent;
        /** Multiplicateur de l'attaque et de la défense du tour. */
        public final float power;

        Stake(int percent, float power) {
            this.percent = percent;
            this.power   = power;
        }

        /** @return le palier suivant (le dernier revient à 0). */
        public Stake next() { return values()[(ordinal() + 1) % values().length]; }

        /** @return ce que coûte cette Mise au joueur qui a {@code gains} gains (rien sans gains). */
        public double amount(double gains) {
            return gains > 0 ? BigNumber.percent(gains, percent) : 0;
        }

        /** @return le multiplicateur écrit pour l'écran (« 1,2 », « 1.2 » en anglais). */
        public String powerText() {
            return Lang.decimal(String.format(Locale.ROOT, "%.1f", power));
        }
    }
}
