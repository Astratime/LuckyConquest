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
 * 10 descentes environ, au lieu de 30). Depuis le 2026-11-26 (plafond des cartes « +X % de tes gains
 * actuels ») ils viennent du parcours du simulateur ({@code ./gradlew :core:simulate -Pjourney -Pstake=15}) :
 * 10 descentes typiques dans le lieu le plus rentable ouvert (lieu normal du rang ou lieu difficile déjà
 * scellé), robots Malin, Pique, Coeur et Trèfle. Astra : les prix montent toujours, d'un niveau à l'autre
 * (+10 % au moins) comme d'un rang à l'autre (+5 % au moins). Sans Prestige, les rangs gardent leurs prix,
 * sauf ceux que le plafond a touchés (Requin, Gros bonnet, Baron, Roi du casino, Légende), baissés comme les
 * pièces par descente du joueur moyen du rang d'avant ({@code -Pprofiles}).
 */
public enum Rank {

    AVARE("Avare", "Il garde chaque pièce.",
        new RankBonus(100, 100, 10, 35, 70), 4_400_000L,
        new long[] {96_000_000L, 1_500_000_000L, 10_000_000_000L, 23_000_000_000L, 42_000_000_000L, 60_000_000_000L, 75_000_000_000L, 110_000_000_000L, 130_000_000_000L, 140_000_000_000L}),
    PARIEUR("Parieur", "Il mise sans trembler.",
        new RankBonus(200, 200, 20, 60, 150), 42_000_000L,
        new long[] {140_000_000L, 1_800_000_000L, 12_000_000_000L, 25_000_000_000L, 53_000_000_000L, 88_000_000_000L, 110_000_000_000L, 160_000_000_000L, 210_000_000_000L, 230_000_000_000L}),
    FLAMBEUR("Flambeur", "Il brûle ses billets pour rire.",
        new RankBonus(350, 350, 35, 130, 250), 86_000_000L,
        new long[] {150_000_000L, 1_900_000_000L, 16_000_000_000L, 31_000_000_000L, 67_000_000_000L, 110_000_000_000L, 150_000_000_000L, 220_000_000_000L, 240_000_000_000L, 260_000_000_000L}),
    HABITUE("Habitué", "Le croupier connaît son nom.",
        // Rapide de Minuit (2026-11-21) : le Flambeur y gagne ~6 M par descente (joueur moyen) ; prix après Prestige
        // réduits dans la même proportion (180 / 460).
        new RankBonus(500, 500, 50, 140, 400), 180_000_000L,
        new long[] {170_000_000L, 2_300_000_000L, 20_000_000_000L, 37_000_000_000L, 94_000_000_000L, 190_000_000_000L, 220_000_000_000L, 300_000_000_000L, 390_000_000_000L, 430_000_000_000L}),
    REQUIN("Requin", "Il flaire les pigeons.",
        new RankBonus(1_200, 1_200, 120, 150, 550), 1_400_000_000L,
        new long[] {180_000_000L, 2_600_000_000L, 24_000_000_000L, 41_000_000_000L, 100_000_000_000L, 200_000_000_000L, 230_000_000_000L, 320_000_000_000L, 410_000_000_000L, 450_000_000_000L}),
    GROS_BONNET("Gros bonnet", "Sa table est toujours pleine.",
        // Palace des Neiges (2026-11-21) : le Requin y gagne ~62 M par descente (joueur moyen), soit ~32
        // descentes ; un peu au-dessus du Requin (1,9 Md) pour garder des prix croissants. Prix après Prestige
        // réduits dans la même proportion (2 / 9,9).
        new RankBonus(1_350, 1_350, 135, 180, 750), 1_600_000_000L,
        new long[] {560_000_000L, 3_000_000_000L, 26_000_000_000L, 43_000_000_000L, 100_000_000_000L, 210_000_000_000L, 240_000_000_000L, 370_000_000_000L, 470_000_000_000L, 520_000_000_000L}),
    BARON("Baron du tapis vert", "Il règne sur la salle.",
        // 2026-11-22 : 2 200 -> 2 350 (défense 220 -> 235). Au Manoir hanté, le Baron moyen gagnait 49-80 % (médiane 57) ;
        // il gagne maintenant 59-82 % (médiane 70), comme le Roi au Temple (63-70 %). Ennemis et pièces inchangés.
        new RankBonus(2_350, 2_350, 235, 300, 1_000), 18_000_000_000L,
        new long[] {1_800_000_000L, 3_200_000_000L, 29_000_000_000L, 46_000_000_000L, 120_000_000_000L, 220_000_000_000L, 320_000_000_000L, 390_000_000_000L, 490_000_000_000L, 550_000_000_000L}),
    MAGNAT("Magnat", "Il possède la moitié du casino.",
        // Manoir hanté (2026-11-21) : le Baron y gagne ~1,1 Md par descente (joueur moyen, médiane des 5 profils) ;
        // prix après Prestige réduits dans la même proportion (34 / 39).
        new RankBonus(2_800, 2_800, 280, 360, 1_400), 34_000_000_000L,
        new long[] {8_000_000_000L, 8_800_000_000L, 50_000_000_000L, 66_000_000_000L, 180_000_000_000L, 310_000_000_000L, 420_000_000_000L, 670_000_000_000L, 790_000_000_000L, 870_000_000_000L}),
    ROI_DU_CASINO("Roi du casino", "Il possède l'autre moitié.",
        new RankBonus(5_500, 5_500, 550, 620, 2_000), 61_000_000_000L,
        new long[] {20_000_000_000L, 22_000_000_000L, 52_000_000_000L, 91_000_000_000L, 220_000_000_000L, 340_000_000_000L, 460_000_000_000L, 700_000_000_000L, 880_000_000_000L, 970_000_000_000L}),
    LEGENDE("Légende du Jackpot", "Les tables murmurent son nom.",
        // Temple du Destin (2026-11-21) : le Roi du casino y gagne ~21 Md par descente (joueur moyen, médiane des
        // decks) ; 30 descentes. Prix après Prestige montés dans la même proportion (630 / 500).
        new RankBonus(7_000, 7_000, 700, 820, 2_800), 160_000_000_000L,
        new long[] {37_000_000_000L, 100_000_000_000L, 130_000_000_000L, 410_000_000_000L, 660_000_000_000L, 800_000_000_000L, 1_300_000_000_000L, 1_400_000_000_000L, 1_500_000_000_000L, 1_700_000_000_000L});

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
