package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.RankBonus;
import fr.astratime.lucky.i18n.Lang;

/**
 * Rangs achetés à la boutique, dans l'ordre. Chacun augmente les PV max du
 * joueur et la valeur de base de ses rouleaux d'attaque, de défense et de
 * gains. Le bonus d'un rang remplace celui du rang d'avant. Celui de défense
 * vaut le cinquième de celui d'attaque : plus fort, un seul symbole de défense
 * arrêtait presque tous les coups.
 */
public enum Rank {

    AVARE("Avare", "Il garde chaque pièce.",
        new RankBonus(100, 100, 10, 70), 1_500_000L),
    PARIEUR("Parieur", "Il mise sans trembler.",
        new RankBonus(200, 200, 20, 150), 11_000_000L),
    FLAMBEUR("Flambeur", "Il brûle ses billets pour rire.",
        new RankBonus(350, 350, 35, 250), 28_000_000L),
    HABITUE("Habitué", "Le croupier connaît son nom.",
        new RankBonus(500, 500, 50, 400), 114_000_000L),
    REQUIN("Requin", "Il flaire les pigeons.",
        new RankBonus(1_200, 1_200, 120, 550), 280_000_000L),
    GROS_BONNET("Gros bonnet", "Sa table est toujours pleine.",
        new RankBonus(1_350, 1_350, 135, 750), 1_440_000_000L),
    BARON("Baron du tapis vert", "Il règne sur la salle.",
        new RankBonus(2_200, 2_200, 220, 1_000), 2_140_000_000L),
    MAGNAT("Magnat", "Il possède la moitié du casino.",
        new RankBonus(2_800, 2_800, 280, 1_400), 16_300_000_000L),
    ROI_DU_CASINO("Roi du casino", "Il possède l'autre moitié.",
        new RankBonus(5_500, 5_500, 550, 2_000), 30_000_000_000L),
    LEGENDE("Légende du Jackpot", "Les tables murmurent son nom.",
        new RankBonus(7_000, 7_000, 700, 2_800), 90_000_000_000L);

    private final String    title;
    private final String    description;
    private final RankBonus bonus;
    private final long      price;

    Rank(String title, String description, RankBonus bonus, long price) {
        this.title       = title;
        this.description = description;
        this.bonus       = bonus;
        this.price       = price;
    }

    /** @return le nom du rang (ex : "Avare"). */
    public String getTitle() { return Lang.t(title); }
    /** @return sa présentation, en une phrase courte. */
    public String getDescription() { return Lang.t(description); }
    /** @return ses bonus en combat. */
    public RankBonus getBonus() { return bonus; }
    /** @return son prix, en pièces. */
    public long getPrice() { return price; }

    /** @return ses bonus écrits en phrases courtes (ex : "PV +100. Attaque +100. ..."). */
    public String getBonusText() {
        return Lang.f("PV +{0}. Rouleaux d'attaque +{1}. Rouleaux de défense +{2}. Rouleaux de gains +{3}.",
            bonus.hp(), bonus.attack(), bonus.defense(), bonus.gains());
    }
}
