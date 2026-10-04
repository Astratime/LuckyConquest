package fr.astratime.lucky.entities.exploration;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.enemy.EnemyKind;

import java.util.List;
import java.util.Random;

/**
 * Les donjons de l'Exploration, un par couleur. Chacun enchaîne deux combats
 * (voir {@link DungeonRun}) : le soldat de la couleur, puis son roi. Au bout,
 * un coffre au trésor donne une carte de la couleur, tirée au hasard dans
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
        List.of(new Loot("rempart", 40), new Loot("miroir_taille", 40), new Loot("diamant_brut", 20)));

    private final String     name;
    private final Card.Suit  suit;
    private final String     description;
    private final EnemyKind  soldier;
    private final EnemyKind  king;
    private final List<Loot> loot;

    Dungeon(String name, Card.Suit suit, String description, EnemyKind soldier, EnemyKind king, List<Loot> loot) {
        this.name        = name;
        this.suit        = suit;
        this.description = description;
        this.soldier     = soldier;
        this.king        = king;
        this.loot        = loot;
    }

    /** @return le nom du donjon (ex : "Donjon du Pique"). */
    public String getName() { return name; }
    /** @return le nom de sa couleur (ex : "Pique"), sous son entrée sur la carte. */
    public String getShortName() { return name.substring(name.lastIndexOf(' ') + 1); }
    /** @return la couleur du donjon. */
    public Card.Suit getSuit() { return suit; }
    /** @return sa présentation, en quelques phrases courtes. */
    public String getDescription() { return description; }
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
