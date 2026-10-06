package fr.astratime.lucky.entities.exploration;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.i18n.Lang;

import java.util.List;
import java.util.Random;

/**
 * Les donjons de l'Exploration : un par couleur dans la prairie, puis quatre
 * par lieu (voir {@link Place}). Chacun enchaîne deux combats (voir
 * {@link DungeonRun}) : un premier ennemi (le soldat de la couleur), puis son
 * chef (le roi). Au bout,
 * un coffre au trésor donne une carte du donjon, tirée au hasard dans
 * {@link #getLoot()} (deux cartes à 40 %, une plus forte à 20 %).
 */
public enum Dungeon {

    PIQUE("Donjon du Pique", Card.Suit.PIQUE,
        "Des lames partout. Le Soldat de Pique garde l'entrée. Le Roi de Pique attend au fond.",
        EnemyKind.SOLDAT_PIQUE, EnemyKind.ROI_PIQUE,
        List.of(new Loot("pierre_a_aiguiser", 40), new Loot("dague_de_l_ombre", 40), new Loot("guillotine", 20))),

    TREFLE("Donjon du Trèfle", Card.Suit.TREFLE,
        "Ça sent l'or. Le Soldat de Trèfle compte les pièces. Le Roi de Trèfle les garde toutes.",
        EnemyKind.SOLDAT_TREFLE, EnemyKind.ROI_TREFLE,
        List.of(new Loot("trefle_a_quatre_feuilles", 40), new Loot("bourse_garnie", 40), new Loot("fortune_du_roi", 20))),

    COEUR("Donjon du Coeur", Card.Suit.COEUR,
        "Les murs battent comme un coeur. Le Soldat de Coeur se relève toujours. Le Roi de Coeur a soif.",
        EnemyKind.SOLDAT_COEUR, EnemyKind.ROI_COEUR,
        List.of(new Loot("transfusion", 40), new Loot("baiser_vampire", 40), new Loot("pacte_de_sang", 20))),

    CARREAU("Donjon du Carreau", Card.Suit.CARREAU,
        "Tout brille. Le Soldat de Carreau ne baisse jamais son écu. Le Roi de Carreau dort sur son trésor.",
        EnemyKind.SOLDAT_CARREAU, EnemyKind.ROI_CARREAU,
        List.of(new Loot("rempart", 40), new Loot("miroir_taille", 40), new Loot("diamant_brut", 20))),

    // ----- Le Port des Contrebandiers -----

    CALE("La Cale", "Cale",
        "Il fait noir. Ça grouille. Les rats rongent tout, même tes cartes.",
        EnemyKind.RAT_CALES, EnemyKind.CAPITAINE_RAT,
        List.of(new Loot("piege_a_rats", 40), new Loot("rhum", 40), new Loot("carte_au_tresor", 20))),

    TAVERNE("La Taverne", "Taverne",
        "Les chopes se vident. Les têtes tournent. Tes rouleaux aussi.",
        EnemyKind.BUVEUR, EnemyKind.TAVERNIER,
        List.of(new Loot("chope", 40), new Loot("tournee_generale", 40), new Loot("fut_de_poudre", 20))),

    PHARE("Le Phare", "Phare",
        "Sa lumière balaie la mer. Elle t'aveugle. Tu joues sans voir.",
        EnemyKind.GUETTEUR, EnemyKind.GARDIEN_PHARE,
        List.of(new Loot("longue_vue", 40), new Loot("lanterne", 40), new Loot("rayon_du_phare", 20))),

    GALION("Le Galion", "Galion",
        "Un navire noir. Des pirates partout. Ils volent tes meilleures cartes.",
        EnemyKind.PIRATE, EnemyKind.CAPITAINE_NOIR,
        List.of(new Loot("sabre_d_abordage", 40), new Loot("pavillon_noir", 40), new Loot("mutinerie", 20))),

    // ----- Les Mines d'Or -----

    FILON("Le Filon", "Filon",
        "L'or brille dans la roche. Ici, tes gains se changent en cailloux.",
        EnemyKind.CHERCHEUR_OR, EnemyKind.BARON_OR,
        List.of(new Loot("pioche_du_mineur", 40), new Loot("tamis", 40), new Loot("veine_d_or", 20))),

    PUITS("Le Puits", "Puits",
        "Un trou sans fond. Les foreuses hurlent. Elles percent ton bouclier.",
        EnemyKind.FOREUR, EnemyKind.GRAND_FOREUR,
        List.of(new Loot("casque", 40), new Loot("etai", 40), new Loot("corde_de_rappel", 20))),

    FORGE("La Forge", "Forge",
        "Il fait chaud. Les enclumes sonnent. Chaque coup affûte leurs armes.",
        EnemyKind.FORGERON, EnemyKind.MAITRE_FORGE,
        List.of(new Loot("marteau_de_forge", 40), new Loot("trempe", 40), new Loot("lame_forgee", 20))),

    GOUFFRE("Le Gouffre", "Gouffre",
        "Tout au fond, l'or s'est mis à marcher. Sa peau encaisse la moitié des coups.",
        EnemyKind.GOLEM_OR, EnemyKind.COEUR_MONTAGNE,
        List.of(new Loot("dynamite", 40), new Loot("lampe_a_carbure", 40), new Loot("coeur_d_or", 20))),

    // ----- Le Casino Englouti -----

    BAR("Le Bar", "Bar",
        "Les verres flottent. Une voix chante sous l'eau. Ta main n'obéit plus.",
        EnemyKind.BARMAN_NOYE, EnemyKind.SIRENE,
        List.of(new Loot("bulle_d_air", 40), new Loot("bouchons_d_oreille", 40), new Loot("cocktail_des_abysses", 20))),

    MACHINES("La Salle des machines", "Machines",
        "Les machines tournent encore. Seules. Trois symboles pareils, et tout explose.",
        EnemyKind.BANDIT_MANCHOT, EnemyKind.JACKPOT_VIVANT,
        List.of(new Loot("piece_truquee", 40), new Loot("levier_rouille", 40), new Loot("jackpot_englouti", 20))),

    COFFRES("Les Coffres", "Coffres",
        "Des coffres ouverts. Des requins qui tournent. Ils sentent tes gains.",
        EnemyKind.REQUIN, EnemyKind.REQUIN_BANQUIER,
        List.of(new Loot("perle_noire", 40), new Loot("cage_a_requin", 40), new Loot("coffre_de_l_epave", 20))),

    VIP("La Salle VIP", "VIP",
        "Une table immense. Huit bras qui distribuent. Le Kraken attend son tour.",
        EnemyKind.PIEUVRE, EnemyKind.KRAKEN,
        List.of(new Loot("harpon", 40), new Loot("ancre", 40), new Loot("trident", 20)));

    private final String     name;
    private final String     shortName;
    private final Card.Suit  suit;
    private final String     description;
    private final EnemyKind  soldier;
    private final EnemyKind  king;
    private final List<Loot> loot;

    Dungeon(String name, Card.Suit suit, String description, EnemyKind soldier, EnemyKind king, List<Loot> loot) {
        this(name, name.substring(name.lastIndexOf(' ') + 1), suit, description, soldier, king, loot);
    }

    /** Donjon sans couleur (hors de la prairie), avec le nom court écrit sous son entrée. */
    Dungeon(String name, String shortName, String description, EnemyKind soldier, EnemyKind king, List<Loot> loot) {
        this(name, shortName, null, description, soldier, king, loot);
    }

    Dungeon(String name, String shortName, Card.Suit suit, String description, EnemyKind soldier, EnemyKind king,
            List<Loot> loot) {
        this.name        = name;
        this.shortName   = shortName;
        this.suit        = suit;
        this.description = description;
        this.soldier     = soldier;
        this.king        = king;
        this.loot        = loot;
    }

    /** @return le nom du donjon (ex : "Donjon du Pique"). */
    public String getName() { return Lang.t(name); }
    /** @return son nom court (ex : "Pique", "Cale"), sous son entrée sur la carte. */
    public String getShortName() { return Lang.t(shortName); }
    /** @return la couleur du donjon de la prairie, ou {@code null} pour les autres lieux. */
    public Card.Suit getSuit() { return suit; }
    /** @return sa présentation, en quelques phrases courtes. */
    public String getDescription() { return Lang.t(description); }
    /** @return l'ennemi du premier combat. */
    public EnemyKind getSoldier() { return soldier; }
    /** @return l'ennemi du second combat, le roi de la couleur. */
    public EnemyKind getKing() { return king; }
    /** @return les cartes que peut donner le coffre, et leurs chances (100 % au total). */
    public List<Loot> getLoot() { return loot; }

    /** @return l'id de la carte trouvée dans le coffre, tirée selon les chances de {@link #getLoot()}. */
    public String rollLoot(Random random) {
        int total = loot.stream().mapToInt(Loot::percent).sum();
        int roll  = random.nextInt(total);
        for (Loot entry : loot) {
            roll -= entry.percent();
            if (roll < 0) return entry.cardId();
        }
        return loot.get(loot.size() - 1).cardId();
    }

    /**
     * Carte que peut donner le coffre d'un donjon.
     *
     * @param cardId  id de la carte
     * @param percent chance de la trouver, en %
     */
    public record Loot(String cardId, int percent) { }
}
