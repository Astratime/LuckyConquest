package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.RankBonus;
import fr.astratime.lucky.i18n.Lang;

/**
 * Rangs achetés à la boutique, dans l'ordre. Chacun augmente les PV max du
 * joueur et la valeur de base de ses rouleaux d'attaque, de défense et de
 * gains. Le bonus d'un rang remplace celui du rang d'avant. Celui de défense
 * vaut le cinquième de celui d'attaque : plus fort, un seul symbole de défense
 * arrêtait presque tous les coups.
 *
 * <p>Après un Prestige, les rangs se rachètent à d'autres prix (Astra : chacun en
 * 10 descentes environ dans le lieu du rang d'avant, au lieu de 30). Ils viennent du
 * simulateur ({@code ./gradlew :core:simulate -Prankplace -Pstake=15 -PrpRuns=300 -Plevel=N},
 * médiane des decks sauf Carreau, colonne typical30), lissés d'un niveau à l'autre et
 * jamais moins de 1,5 fois le rang d'avant.
 */
public enum Rank {

    AVARE("Avare", "Il garde chaque pièce.",
        new RankBonus(100, 100, 10, 70), 3_000_000L,
        new long[] {1_200_000L, 1_300_000L, 1_400_000L, 1_500_000L, 1_600_000L, 1_700_000L, 1_800_000L, 2_000_000L, 2_100_000L, 2_200_000L}),
    PARIEUR("Parieur", "Il mise sans trembler.",
        new RankBonus(200, 200, 20, 150), 32_000_000L,
        new long[] {14_000_000L, 16_000_000L, 20_000_000L, 24_000_000L, 28_000_000L, 34_000_000L, 41_000_000L, 50_000_000L, 60_000_000L, 72_000_000L}),
    FLAMBEUR("Flambeur", "Il brûle ses billets pour rire.",
        new RankBonus(350, 350, 35, 250), 110_000_000L,
        new long[] {49_000_000L, 56_000_000L, 65_000_000L, 74_000_000L, 85_000_000L, 97_000_000L, 110_000_000L, 130_000_000L, 150_000_000L, 170_000_000L}),
    HABITUE("Habitué", "Le croupier connaît son nom.",
        new RankBonus(500, 500, 50, 400), 300_000_000L,
        new long[] {220_000_000L, 290_000_000L, 390_000_000L, 530_000_000L, 720_000_000L, 970_000_000L, 1_300_000_000L, 1_800_000_000L, 2_400_000_000L, 3_300_000_000L}),
    REQUIN("Requin", "Il flaire les pigeons.",
        new RankBonus(1_200, 1_200, 120, 550), 2_000_000_000L,
        new long[] {1_100_000_000L, 1_300_000_000L, 1_700_000_000L, 2_000_000_000L, 2_500_000_000L, 3_100_000_000L, 3_800_000_000L, 4_700_000_000L, 5_900_000_000L, 7_200_000_000L}),
    GROS_BONNET("Gros bonnet", "Sa table est toujours pleine.",
        new RankBonus(1_350, 1_350, 135, 750), 4_000_000_000L,
        new long[] {3_000_000_000L, 4_600_000_000L, 7_000_000_000L, 11_000_000_000L, 16_000_000_000L, 25_000_000_000L, 38_000_000_000L, 57_000_000_000L, 87_000_000_000L, 130_000_000_000L}),
    BARON("Baron du tapis vert", "Il règne sur la salle.",
        new RankBonus(2_200, 2_200, 220, 1_000), 11_000_000_000L,
        new long[] {8_300_000_000L, 12_000_000_000L, 18_000_000_000L, 26_000_000_000L, 38_000_000_000L, 56_000_000_000L, 82_000_000_000L, 120_000_000_000L, 180_000_000_000L, 260_000_000_000L}),
    MAGNAT("Magnat", "Il possède la moitié du casino.",
        new RankBonus(2_800, 2_800, 280, 1_400), 21_000_000_000L,
        new long[] {12_000_000_000L, 18_000_000_000L, 27_000_000_000L, 39_000_000_000L, 57_000_000_000L, 100_000_000_000L, 200_000_000_000L, 380_000_000_000L, 750_000_000_000L, 1_500_000_000_000L}),
    ROI_DU_CASINO("Roi du casino", "Il possède l'autre moitié.",
        new RankBonus(5_500, 5_500, 550, 2_000), 49_000_000_000L,
        new long[] {36_000_000_000L, 62_000_000_000L, 110_000_000_000L, 180_000_000_000L, 320_000_000_000L, 540_000_000_000L, 940_000_000_000L, 1_600_000_000_000L, 2_800_000_000_000L, 4_800_000_000_000L}),
    LEGENDE("Légende du Jackpot", "Les tables murmurent son nom.",
        new RankBonus(7_000, 7_000, 700, 2_800), 120_000_000_000L,
        new long[] {180_000_000_000L, 340_000_000_000L, 640_000_000_000L, 1_200_000_000_000L, 2_300_000_000_000L, 4_400_000_000_000L, 8_400_000_000_000L, 16_000_000_000_000L, 30_000_000_000_000L, 58_000_000_000_000L});

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
        return Lang.f("PV +{0}. Rouleaux d'attaque +{1}. Rouleaux de défense +{2}. Rouleaux de gains +{3}.",
            bonus.hp(), bonus.attack(), bonus.defense(), bonus.gains());
    }
}
