package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.RankBonus;
import fr.astratime.lucky.i18n.Lang;

/**
 * Rangs achetés à la boutique, dans l'ordre. Chacun augmente les PV max du
 * joueur et la valeur de base de ses rouleaux d'attaque et de défense, et multiplie
 * ses gains (en %, toutes les sources de la machine : voir {@code GainFormula}). Le bonus d'un rang remplace celui du rang d'avant. Celui de défense
 * vaut le cinquième de celui d'attaque : plus fort, un seul symbole de défense
 * arrêtait presque tous les coups.
 *
 * <p>Après un Prestige, les rangs se rachètent à d'autres prix (Astra : chacun en
 * 10 descentes environ dans le lieu du rang d'avant, au lieu de 30). Ils viennent du
 * simulateur ({@code ./gradlew :core:simulate -Prankplace -Pstake=15 -PrpRuns=300 -Plevel=N},
 * médiane des decks sauf Carreau, colonne typical30). Astra : les prix doivent toujours monter,
 * donc ils partent du Prestige IV mesuré (10 descentes) et prennent +10 % à chaque niveau.
 */
public enum Rank {

    AVARE("Avare", "Il garde chaque pièce.",
        new RankBonus(100, 100, 10, 35, 70), 3_000_000L,
        new long[] {1_500_000L, 1_700_000L, 1_800_000L, 2_000_000L, 2_200_000L, 2_400_000L, 2_700_000L, 2_900_000L, 3_200_000L, 3_500_000L}),
    PARIEUR("Parieur", "Il mise sans trembler.",
        new RankBonus(200, 200, 20, 60, 150), 32_000_000L,
        new long[] {44_000_000L, 48_000_000L, 53_000_000L, 58_000_000L, 64_000_000L, 70_000_000L, 77_000_000L, 85_000_000L, 93_000_000L, 100_000_000L}),
    FLAMBEUR("Flambeur", "Il brûle ses billets pour rire.",
        new RankBonus(350, 350, 35, 130, 250), 110_000_000L,
        new long[] {65_000_000L, 72_000_000L, 79_000_000L, 87_000_000L, 96_000_000L, 110_000_000L, 120_000_000L, 130_000_000L, 140_000_000L, 150_000_000L}),
    HABITUE("Habitué", "Le croupier connaît son nom.",
        new RankBonus(500, 500, 50, 140, 400), 300_000_000L,
        new long[] {1_100_000_000L, 1_200_000_000L, 1_400_000_000L, 1_500_000_000L, 1_700_000_000L, 1_800_000_000L, 2_000_000_000L, 2_200_000_000L, 2_400_000_000L, 2_700_000_000L}),
    REQUIN("Requin", "Il flaire les pigeons.",
        new RankBonus(1_200, 1_200, 120, 150, 550), 2_000_000_000L,
        new long[] {1_700_000_000L, 1_800_000_000L, 2_000_000_000L, 2_200_000_000L, 2_400_000_000L, 2_700_000_000L, 2_900_000_000L, 3_200_000_000L, 3_500_000_000L, 3_900_000_000L}),
    GROS_BONNET("Gros bonnet", "Sa table est toujours pleine.",
        new RankBonus(1_350, 1_350, 135, 180, 750), 4_000_000_000L,
        new long[] {25_000_000_000L, 27_000_000_000L, 30_000_000_000L, 33_000_000_000L, 36_000_000_000L, 40_000_000_000L, 44_000_000_000L, 48_000_000_000L, 53_000_000_000L, 58_000_000_000L}),
    BARON("Baron du tapis vert", "Il règne sur la salle.",
        new RankBonus(2_200, 2_200, 220, 300, 1_000), 11_000_000_000L,
        new long[] {38_000_000_000L, 41_000_000_000L, 45_000_000_000L, 50_000_000_000L, 55_000_000_000L, 61_000_000_000L, 67_000_000_000L, 73_000_000_000L, 81_000_000_000L, 89_000_000_000L}),
    MAGNAT("Magnat", "Il possède la moitié du casino.",
        new RankBonus(2_800, 2_800, 280, 360, 1_400), 21_000_000_000L,
        new long[] {330_000_000_000L, 360_000_000_000L, 400_000_000_000L, 440_000_000_000L, 480_000_000_000L, 530_000_000_000L, 590_000_000_000L, 640_000_000_000L, 710_000_000_000L, 780_000_000_000L}),
    ROI_DU_CASINO("Roi du casino", "Il possède l'autre moitié.",
        new RankBonus(5_500, 5_500, 550, 620, 2_000), 49_000_000_000L,
        new long[] {500_000_000_000L, 550_000_000_000L, 600_000_000_000L, 660_000_000_000L, 730_000_000_000L, 800_000_000_000L, 880_000_000_000L, 970_000_000_000L, 1_100_000_000_000L, 1_200_000_000_000L}),
    LEGENDE("Légende du Jackpot", "Les tables murmurent son nom.",
        new RankBonus(7_000, 7_000, 700, 820, 2_800), 120_000_000_000L,
        new long[] {11_000_000_000_000L, 12_000_000_000_000L, 13_000_000_000_000L, 14_000_000_000_000L, 15_000_000_000_000L, 17_000_000_000_000L, 19_000_000_000_000L, 20_000_000_000_000L, 23_000_000_000_000L, 25_000_000_000_000L});

    private final String    title;
    private final String    description;
    private final RankBonus bonus;
    private final long      price;
    /** Prix au Prestige 1 à 10 (case 0 : Prestige 1). */
    private final long[]    prestigePrices;

    Rank(String title, String description, RankBonus bonus, long price, long[] prestigePrices) {
        this.title       = title;
        this.description = description;
        this.bonus       = bonus;
        this.price       = price;
        this.prestigePrices = prestigePrices;
    }

    /** @return le nom du rang (ex : "Avare"). */
    public String getTitle() { return Lang.t(title); }
    /** @return sa présentation, en une phrase courte. */
    public String getDescription() { return Lang.t(description); }
    /** @return ses bonus en combat. */
    public RankBonus getBonus() { return bonus; }
    /** @return son prix, en pièces, sans Prestige. */
    public long getPrice() { return price; }

    /** @return son prix, en pièces, au niveau de Prestige {@code prestige}. */
    public long getPrice(int prestige) {
        if (prestige <= 0) return price;
        return prestigePrices[Math.min(prestigePrices.length, prestige) - 1];
    }

    /** @return ses bonus écrits en phrases courtes (ex : "PV +100. Attaque +100. ..."). */
    public String getBonusText() {
        return Lang.f("PV +{0}. Rouleaux d'attaque +{1}. Rouleaux de défense +{2}. Gains +{3} %.",
            bonus.hp(), bonus.attack(), bonus.defense(), bonus.gains());
    }
}
