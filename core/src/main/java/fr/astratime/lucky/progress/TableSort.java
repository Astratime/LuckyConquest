package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.i18n.Lang;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Tris de la Table du croupier : l'ordre des cartes de la collection (onglet
 * « Deck ») et des rouleaux possédés (onglet « Rouleaux »). Par défaut, ce que
 * le joueur emmène au combat (son deck, sa machine) s'affiche en premier.
 */
public final class TableSort {

    private TableSort() {}

    /** Tris des cartes de la collection. */
    public enum CardSort {
        /** Les cartes du deck d'abord, puis les autres ; chaque groupe par famille. */
        DECK_FIRST("Deck d'abord"),
        /** Par famille : Pique, Coeur, Trèfle, Carreau, puis archétypes, puis les autres ; de l'As au 2. */
        FAMILY("Famille"),
        /** Par valeur, de l'As au 2, toutes familles mêlées ; les cartes sans valeur à la fin. */
        VALUE("Valeur"),
        /** Par nom, de A à Z. */
        NAME("Nom"),
        /** Dans l'ordre où le joueur les a obtenues. */
        OBTAINED("Obtention");

        private final String label;

        CardSort(String label) { this.label = label; }

        /** @return le nom du tri, dans la langue choisie. */
        public String getLabel() { return Lang.t(label); }

        /** @return le tri suivant (le dernier revient au premier). */
        public CardSort next() { return values()[(ordinal() + 1) % values().length]; }
    }

    /** Tris des rouleaux possédés. */
    public enum ReelSort {
        /** Les rouleaux de la machine d'abord (dans son ordre), puis les autres. */
        MACHINE_FIRST("Machine d'abord"),
        /** Du plus rare au plus commun ; à rareté égale, le plus cher à la boutique d'abord. */
        RARITY("Rareté"),
        /** Par nom, de A à Z. */
        NAME("Nom"),
        /** Les classiques, puis les autres dans l'ordre où le joueur les a obtenus. */
        OBTAINED("Obtention");

        private final String label;

        ReelSort(String label) { this.label = label; }

        /** @return le nom du tri, dans la langue choisie. */
        public String getLabel() { return Lang.t(label); }

        /** @return le tri suivant (le dernier revient au premier). */
        public ReelSort next() { return values()[(ordinal() + 1) % values().length]; }
    }

    /**
     * @param cards la collection, dans l'ordre d'obtention
     * @param deck  exemplaires de chaque carte dans le deck, par id
     * @return les cartes dans l'ordre du tri {@code sort}
     */
    public static List<Card> sortCards(List<Card> cards, Map<String, Integer> deck, CardSort sort) {
        List<Card> sorted = new ArrayList<>(cards);
        Comparator<Card> byName = Comparator.comparing(Card::getName, collator());
        Comparator<Card> byFamily = Comparator.comparingInt(TableSort::familyOrder)
            .thenComparingInt(TableSort::valueOrder)
            .thenComparing(Card::isUpgraded)
            .thenComparing(byName);
        switch (sort) {
            case DECK_FIRST -> sorted.sort(Comparator.comparing((Card card) -> deck.getOrDefault(card.getId(), 0) <= 0)
                .thenComparing(byFamily));
            case FAMILY -> sorted.sort(byFamily);
            case VALUE  -> sorted.sort(Comparator.comparingInt(TableSort::valueOrder)
                .thenComparingInt(TableSort::familyOrder)
                .thenComparing(Card::isUpgraded)
                .thenComparing(byName));
            case NAME -> sorted.sort(byName.thenComparing(Card::isUpgraded));
            case OBTAINED -> { }
        }
        return sorted;
    }

    /**
     * @param owned   les rouleaux possédés, dans l'ordre d'obtention
     * @param machine les rouleaux de la machine, dans son ordre
     * @return les rouleaux dans l'ordre du tri {@code sort}
     */
    public static List<Symbol> sortReels(List<Symbol> owned, List<Symbol> machine, ReelSort sort) {
        List<Symbol> sorted = new ArrayList<>(owned);
        switch (sort) {
            case MACHINE_FIRST -> sorted.sort(Comparator.comparingInt((Symbol symbol) -> {
                int index = machine.indexOf(symbol);
                return index < 0 ? Integer.MAX_VALUE : index;
            }));
            case RARITY -> sorted.sort(Comparator.comparingInt((Symbol symbol) -> -symbol.getRarity().jackpotSpins)
                .thenComparingLong(symbol -> -ReelShop.getPrices().getOrDefault(symbol, 0L)));
            case NAME -> sorted.sort(Comparator.comparing(Symbol::getDisplayName, collator()));
            case OBTAINED -> { }
        }
        return sorted; // les tris de Java gardent l'ordre d'obtention à égalité
    }

    /** Pique, Coeur, Trèfle, Carreau, puis les archétypes (dans leur ordre), puis les autres cartes. */
    private static int familyOrder(Card card) {
        if (card.getSuit() != null) return switch (card.getSuit()) {
            case PIQUE   -> 0;
            case COEUR   -> 1;
            case TREFLE  -> 2;
            case CARREAU -> 3;
        };
        if (card.getArchetype() != null) return 4 + card.getArchetype().ordinal();
        return 100;
    }

    /** As d'abord, puis Roi, Dame, Valet, 10 … 2 ; les cartes sans valeur à la fin. */
    private static int valueOrder(Card card) {
        if (!card.hasRank()) return 100;
        return card.getRank() == 1 ? 0 : 14 - card.getRank();
    }

    private static Collator collator() {
        Collator collator = Collator.getInstance(Lang.isEnglish() ? Locale.ENGLISH : Locale.FRENCH);
        collator.setStrength(Collator.PRIMARY);
        return collator;
    }
}
