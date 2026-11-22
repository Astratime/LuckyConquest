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
        new RankBonus(100, 100, 10, 35, 70), 4_400_000L,
        new long[] {1_500_000L, 1_700_000L, 1_800_000L, 2_000_000L, 2_200_000L, 2_400_000L, 2_700_000L, 2_900_000L, 3_200_000L, 3_500_000L}),
    PARIEUR("Parieur", "Il mise sans trembler.",
        new RankBonus(200, 200, 20, 60, 150), 42_000_000L,
        new long[] {44_000_000L, 48_000_000L, 53_000_000L, 58_000_000L, 64_000_000L, 70_000_000L, 77_000_000L, 85_000_000L, 93_000_000L, 100_000_000L}),
    FLAMBEUR("Flambeur", "Il brûle ses billets pour rire.",
        new RankBonus(350, 350, 35, 130, 250), 86_000_000L,
        new long[] {65_000_000L, 72_000_000L, 79_000_000L, 87_000_000L, 96_000_000L, 110_000_000L, 120_000_000L, 130_000_000L, 140_000_000L, 150_000_000L}),
    HABITUE("Habitué", "Le croupier connaît son nom.",
        // Rapide de Minuit (2026-11-21) : le Flambeur y gagne ~6 M par descente (joueur moyen) ; prix après Prestige
        // réduits dans la même proportion (180 / 460).
        new RankBonus(500, 500, 50, 140, 400), 180_000_000L,
        new long[] {430_000_000L, 470_000_000L, 550_000_000L, 590_000_000L, 670_000_000L, 700_000_000L, 780_000_000L, 860_000_000L, 940_000_000L, 1_100_000_000L}),
    REQUIN("Requin", "Il flaire les pigeons.",
        new RankBonus(1_200, 1_200, 120, 150, 550), 1_900_000_000L,
        new long[] {1_700_000_000L, 1_800_000_000L, 2_000_000_000L, 2_200_000_000L, 2_400_000_000L, 2_700_000_000L, 2_900_000_000L, 3_200_000_000L, 3_500_000_000L, 3_900_000_000L}),
    GROS_BONNET("Gros bonnet", "Sa table est toujours pleine.",
        // Palace des Neiges (2026-11-21) : le Requin y gagne ~62 M par descente (joueur moyen), soit ~32
        // descentes ; un peu au-dessus du Requin (1,9 Md) pour garder des prix croissants. Prix après Prestige
        // réduits dans la même proportion (2 / 9,9).
        new RankBonus(1_350, 1_350, 135, 180, 750), 2_000_000_000L,
        new long[] {5_100_000_000L, 5_500_000_000L, 6_100_000_000L, 6_700_000_000L, 7_300_000_000L, 8_100_000_000L, 8_900_000_000L, 9_700_000_000L, 11_000_000_000L, 12_000_000_000L}),
    BARON("Baron du tapis vert", "Il règne sur la salle.",
        // 2026-11-22 : 2 200 -> 2 350 (défense 220 -> 235). Au Manoir hanté, le Baron moyen gagnait 49-80 % (médiane 57) ;
        // il gagne maintenant 59-82 % (médiane 70), comme le Roi au Temple (63-70 %). Ennemis et pièces inchangés.
        new RankBonus(2_350, 2_350, 235, 300, 1_000), 21_000_000_000L,
        new long[] {38_000_000_000L, 41_000_000_000L, 45_000_000_000L, 50_000_000_000L, 55_000_000_000L, 61_000_000_000L, 67_000_000_000L, 73_000_000_000L, 81_000_000_000L, 89_000_000_000L}),
    MAGNAT("Magnat", "Il possède la moitié du casino.",
        // Manoir hanté (2026-11-21) : le Baron y gagne ~1,1 Md par descente (joueur moyen, médiane des 5 profils) ;
        // prix après Prestige réduits dans la même proportion (34 / 39).
        new RankBonus(2_800, 2_800, 280, 360, 1_400), 34_000_000_000L,
        new long[] {290_000_000_000L, 310_000_000_000L, 350_000_000_000L, 380_000_000_000L, 420_000_000_000L, 460_000_000_000L, 510_000_000_000L, 560_000_000_000L, 620_000_000_000L, 680_000_000_000L}),
    ROI_DU_CASINO("Roi du casino", "Il possède l'autre moitié.",
        new RankBonus(5_500, 5_500, 550, 620, 2_000), 170_000_000_000L,
        new long[] {500_000_000_000L, 550_000_000_000L, 600_000_000_000L, 660_000_000_000L, 730_000_000_000L, 800_000_000_000L, 880_000_000_000L, 970_000_000_000L, 1_100_000_000_000L, 1_200_000_000_000L}),
    LEGENDE("Légende du Jackpot", "Les tables murmurent son nom.",
        // Temple du Destin (2026-11-21) : le Roi du casino y gagne ~21 Md par descente (joueur moyen, médiane des
        // decks) ; 30 descentes. Prix après Prestige montés dans la même proportion (630 / 500).
        new RankBonus(7_000, 7_000, 700, 820, 2_800), 630_000_000_000L,
        new long[] {14_000_000_000_000L, 15_000_000_000_000L, 16_000_000_000_000L, 18_000_000_000_000L, 19_000_000_000_000L, 21_000_000_000_000L, 24_000_000_000_000L, 25_000_000_000_000L, 29_000_000_000_000L, 32_000_000_000_000L});

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

    /**
     * Plancher de Prestige (2026-11-20, idée d'Astra) : après un Prestige, les PV max, l'attaque et la défense ne
     * descendent jamais sous {@value #PRESTIGE_FLOOR_PERCENT} % de ceux de la Légende. Le joueur reste assez fort pour
     * l'Exploration difficile, qui ouvre le Prestige suivant, au lieu de tout reprendre en mode normal.
     */
    public static final int PRESTIGE_FLOOR_PERCENT = 50;

    /**
     * @param active   rang joué ({@code null} : sans rang)
     * @param prestige niveau de Prestige
     * @return les bonus en combat : ceux du rang, relevés au plancher de Prestige s'il y a un Prestige, puis le Prestige
     */
    public static RankBonus bonusAt(Rank active, int prestige) {
        RankBonus own = active == null ? RankBonus.NONE : active.getBonus();
        if (prestige > 0) {
            RankBonus top = LEGENDE.getBonus();
            own = new RankBonus(Math.max(own.hp(), floor(top.hp())), Math.max(own.attack(), floor(top.attack())),
                Math.max(own.defense(), floor(top.defense())), own.gains(), own.heart());
        }
        return own.withPrestige(prestige);
    }

    private static int floor(int value) { return value * PRESTIGE_FLOOR_PERCENT / 100; }

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
