package fr.astratime.lucky.entities.exploration;

import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Le carrefour d'un donjon de l'Exploration : entre le soldat et le roi, deux
 * portes mènent chacune à une salle tirée au hasard. Le joueur en choisit une.
 * Les salles ne donnent jamais de pièces : seulement des PV, ou un avantage
 * pour le combat contre le roi (voir {@link Boon}), appliqué au début de ce
 * combat et perdu si le donjon recommence.
 */
public final class Crossroads {

    /** Tripot : part des PV du moment misée. */
    public static final int DEN_STAKE_PERCENT = 20;
    /** Tripot : chance de gagner la mise. */
    public static final float DEN_WIN_CHANCE  = 0.5f;
    /** Fontaine : part des PV max soignée, ou sacrifiée pour une carte jouable de plus. */
    public static final int FOUNTAIN_PERCENT  = 30;
    /** Fontaine : cartes jouables en plus au premier tour contre le roi, après un sacrifice. */
    public static final int FOUNTAIN_PLAYS    = 1;
    /** Coffre piégé : part des PV max perdue dans le piège. */
    public static final int TRAP_PERCENT      = 15;
    /** Machine abandonnée : poids du symbole trouvé contre le roi (x2). */
    public static final int MACHINE_FACTOR    = 2;

    /** Les salles derrière les portes. */
    public enum Room {
        MACHINE("Machine abandonnée", "Un tirage gratuit : le symbole obtenu sortira 2 fois plus souvent contre le roi."),
        DEN("Tripot", "Mise 20 % de tes PV à pile ou face. Gagné : une carte de l'échoppe offerte contre le roi."),
        FOUNTAIN("Fontaine", "Bois pour soigner 30 % de tes PV max, ou offre-lui 30 % de tes PV pour 1 carte jouable de plus au premier tour."),
        CHEST("Coffre piégé", "Une carte classique au hasard rejoint ta main contre le roi, mais le piège te coûte 15 % de tes PV max."),
        EMPTY("Salle vide", "Rien. Peut-être.");

        private final String name;
        private final String hint;

        Room(String name, String hint) {
            this.name = name;
            this.hint = hint;
        }

        /** @return le nom de la salle, écrit sur sa porte. */
        public String getName() { return Lang.t(name); }

        /** @return ce que fait la salle, sous son nom. */
        public String getHint() { return Lang.t(hint); }
    }

    /**
     * Ce que le carrefour donne pour le combat contre le roi.
     *
     * @param favored    symbole qui sort {@link #MACHINE_FACTOR} fois plus souvent ({@code null} : aucun)
     * @param cards      cartes ajoutées à la main au début du combat, par id
     * @param extraPlays cartes jouables en plus au premier tour
     */
    public record Boon(Symbol favored, List<String> cards, int extraPlays) {
        /** Aucun avantage. */
        public static final Boon NONE = new Boon(null, List.of(), 0);

        /** @return {@code true} sans aucun avantage. */
        public boolean isEmpty() { return favored == null && cards.isEmpty() && extraPlays == 0; }
    }

    private static final String[] SUITS = {"pique", "coeur", "trefle", "carreau"};

    private final Random random;
    private Symbol favored;
    private final List<String> cards = new ArrayList<>();
    private int extraPlays;

    public Crossroads(Random random) {
        this.random = random;
    }

    /** @return les deux salles derrière les portes, différentes l'une de l'autre. */
    public List<Room> openDoors() {
        List<Room> rooms = new ArrayList<>(List.of(Room.values()));
        Room first = rooms.remove(random.nextInt(rooms.size()));
        Room second = rooms.get(random.nextInt(rooms.size()));
        return List.of(first, second);
    }

    /**
     * Machine abandonnée : un tirage gratuit parmi les rouleaux du joueur (jamais le Joker).
     *
     * @return le symbole obtenu, qui sortira plus souvent contre le roi
     */
    public Symbol searchMachine(Player player) {
        List<Symbol> reels = new ArrayList<>(player.getSlotMachine().getReels());
        reels.removeIf(symbol -> symbol == Symbol.JOKER);
        if (reels.isEmpty()) return null;
        favored = reels.get(random.nextInt(reels.size()));
        return favored;
    }

    /**
     * Tripot : le joueur mise {@link #DEN_STAKE_PERCENT} % de ses PV (jamais le dernier).
     * Gagné, il garde ses PV et une carte de {@code stallCards} lui est offerte contre le roi.
     *
     * @param stallCards ids des cartes de l'échoppe
     * @return la carte gagnée, ou {@code null} si la mise est perdue (les PV aussi)
     */
    public String gamble(Player player, List<String> stallCards) {
        if (random.nextFloat() < DEN_WIN_CHANCE && !stallCards.isEmpty()) {
            String card = stallCards.get(random.nextInt(stallCards.size()));
            cards.add(card);
            return card;
        }
        player.sacrificeHp(denStake(player));
        return null;
    }

    /** @return les PV que coûte la mise du Tripot (perdue). */
    public static int denStake(Player player) {
        return Math.max(1, player.getHp() * DEN_STAKE_PERCENT / 100);
    }

    /** Fontaine, boire : soigne {@link #FOUNTAIN_PERCENT} % des PV max. @return les PV rendus */
    public int drink(Player player) {
        return player.heal(percentOfMax(player, FOUNTAIN_PERCENT));
    }

    /**
     * Fontaine, offrande : {@link #FOUNTAIN_PERCENT} % des PV max (jamais le dernier PV)
     * contre {@link #FOUNTAIN_PLAYS} carte jouable de plus au premier tour contre le roi.
     *
     * @return les PV offerts
     */
    public int offer(Player player) {
        extraPlays += FOUNTAIN_PLAYS;
        return player.sacrificeHp(percentOfMax(player, FOUNTAIN_PERCENT));
    }

    /**
     * Coffre piégé : le piège coûte {@link #TRAP_PERCENT} % des PV max (jamais le dernier),
     * et une carte classique du 2 au Roi, d'une enseigne au hasard, rejoint la main contre le roi.
     *
     * @return l'id de la carte trouvée
     */
    public String openTrappedChest(Player player) {
        player.sacrificeHp(percentOfMax(player, TRAP_PERCENT));
        String card = (2 + random.nextInt(12)) + "_" + SUITS[random.nextInt(SUITS.length)];
        cards.add(card);
        return card;
    }

    /** @return l'avantage gagné pour le combat contre le roi (à appliquer une fois). */
    public Boon getBoon() {
        return new Boon(favored, List.copyOf(cards), extraPlays);
    }

    /** Le donjon recommence : l'avantage du carrefour est perdu. */
    public void reset() {
        favored = null;
        cards.clear();
        extraPlays = 0;
    }

    private static int percentOfMax(Player player, int percent) {
        return (int) ((long) player.getMaxHp() * percent / 100);
    }
}
