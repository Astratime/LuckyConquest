package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.exploration.HardExploration;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.i18n.Lang;

import java.util.Locale;

/**
 * Les gains en combat : chaque tirage coûte des gains, et ce que rapportent
 * les Paires, les Bingos et les symboles de gains se compte en coûts de
 * tirage, pour suivre le lieu (ou le chapitre de la Tour) tout seul.
 * <ul>
 *   <li>Coût d'un tirage (« C ») : voir EnemyKind#getSpinCost (100 à la Prairie, 5 000 au Casino).</li>
 *   <li>Départ d'un combat : {@link #STARTING_SPINS} C de gains (une fois par chapitre de la Tour).</li>
 *   <li>Paire : {@link #PAIR_SPINS} C ; Bingo : 10, 15 ou 25 C selon la {@linkplain Symbol.Rarity rareté} ;
 *       symboles de gains : leur base x {@link #SYMBOL_TABLE} / 100 C. Tout est ensuite multiplié par le
 *       multiplicateur du joueur ({@link GainFormula}).</li>
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
    /** Gains d'une Paire, en coûts de tirage, avant le multiplicateur du joueur. */
    public static final float PAIR_SPINS = 1.5f;
    /**
     * Table des symboles de gains : leur base (Couronne 100, Cloche 12…) x ce facteur, en centièmes de C
     * (Couronne 0,5 C). Astra, 2026-11-16 : la table entière est divisée par 2 quand le multiplicateur du
     * joueur s'applique partout, pour garder les pièces par descente.
     */
    public static final float SYMBOL_TABLE = 0.5f;
    /**
     * Plafond des cartes « +X % de tes gains actuels » (Chope, Cocktail des abysses, Pot de Lutin, Fortune du roi…),
     * en coûts de tirage (Astra, 2026-11-26) : elles ne comptent les gains que jusqu'à ce montant. Sans lui, chaque
     * tour du deck multipliait tous les gains déjà faits, et un long combat rapportait des milliers de milliards.
     */
    public static final int CURRENT_GAINS_CAP_SPINS = 1_000;
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

    /**
     * @return le coût de tirage qui règle la force tirée des gains là où un tirage coûte {@code spinCost} : celui
     *         d'avant l'étirement pour un lieu d'Exploration ({@link Place#getPowerSpinCost()}), sinon {@code spinCost}
     */
    public static int powerSpinCost(int spinCost) {
        for (Place place : Place.values()) if (place.getSpinCost() == spinCost) return place.getPowerSpinCost();
        // Exploration difficile : la force suit celle des ennemis (le Désert x2 par lieu), pas la prime de pièces.
        for (Place place : Place.values()) {
            if (HardExploration.spinCost(place) == spinCost) return HardExploration.powerSpinCost(place);
        }
        return spinCost;
    }

    /** @return les gains de départ d'un combat où un tirage coûte {@code spinCost}. */
    public static int startingGains(int spinCost) { return STARTING_SPINS * spinCost; }

    /** @return les gains d'une Paire. */
    public static int pairGains(int spinCost) { return Math.round(PAIR_SPINS * spinCost); }

    /** @return les gains d'un Bingo de {@code symbol}. */
    public static int jackpotGains(Symbol symbol, int spinCost) { return symbol.getRarity().jackpotSpins * spinCost; }

    /** @return le facteur des prix qui suivent le coût du tirage (x1 pour 100). */
    public static float gainScale(int spinCost) { return spinCost / (float) BASE_SPIN_COST; }

    /** @return ce que vaut un point de base d'un symbole de gains : {@link #SYMBOL_TABLE} x C / 100. */
    public static float symbolScale(int spinCost) { return SYMBOL_TABLE * gainScale(spinCost); }

    /** @return {@code base} points de base d'un symbole écrits en gains à 100 de coût de tirage (Lingot 25 : « 12,5 »). */
    public static String symbolBaseText(int base) {
        float value = base * SYMBOL_TABLE;
        return value == Math.round(value) ? String.valueOf(Math.round(value))
            : fr.astratime.lucky.i18n.Lang.decimal(String.valueOf(value));
    }

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
