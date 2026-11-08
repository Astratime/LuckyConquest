package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.progress.TableSort.CardSort;
import fr.astratime.lucky.progress.TableSort.ReelSort;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Les tris de la Table du croupier. */
class TableSortTest {

    private static Card card(String id, String name, Card.Suit suit, int rank) {
        return new Card(id, name, "", List.of(), suit, rank);
    }

    private static final Card TWO_HEARTS  = card("2_coeur", "2 de Coeur", Card.Suit.COEUR, 2);
    private static final Card ACE_HEARTS  = card("1_coeur", "As de Coeur", Card.Suit.COEUR, 1);
    private static final Card KING_SPADES = card("13_pique", "Roi de Pique", Card.Suit.PIQUE, 13);
    private static final Card JOKER       = card("joker", "Joker", null, 1);
    private static final Card BOOST       = card("double", "Double", null, 1);

    /** La collection dans l'ordre d'obtention. */
    private static final List<Card> COLLECTION = List.of(TWO_HEARTS, JOKER, ACE_HEARTS, BOOST, KING_SPADES);

    private static List<String> ids(List<Card> cards) { return cards.stream().map(Card::getId).toList(); }

    @Test
    void byDefaultTheDeckCardsComeFirst() {
        Map<String, Integer> deck = Map.of("double", 1, "1_coeur", 1);
        List<Card> sorted = TableSort.sortCards(COLLECTION, deck, CardSort.DECK_FIRST);
        assertEquals(List.of("1_coeur", "double", "13_pique", "2_coeur", "joker"), ids(sorted));
        assertEquals(CardSort.DECK_FIRST, CardSort.values()[0], "le tri par défaut");
    }

    @Test
    void byFamilySpadesThenHeartsFromAceToTwoThenTheOthers() {
        assertEquals(List.of("13_pique", "1_coeur", "2_coeur", "double", "joker"),
            ids(TableSort.sortCards(COLLECTION, Map.of(), CardSort.FAMILY)));
    }

    @Test
    void byValueAceFirstAndCardsWithoutValueLast() {
        assertEquals(List.of("1_coeur", "13_pique", "2_coeur", "double", "joker"),
            ids(TableSort.sortCards(COLLECTION, Map.of(), CardSort.VALUE)));
    }

    @Test
    void byNameAndByObtentionOrder() {
        assertEquals(List.of("2_coeur", "1_coeur", "double", "joker", "13_pique"),
            ids(TableSort.sortCards(COLLECTION, Map.of(), CardSort.NAME)));
        assertEquals(ids(COLLECTION), ids(TableSort.sortCards(COLLECTION, Map.of(), CardSort.OBTAINED)));
    }

    @Test
    void aPlusCardComesRightAfterItsBaseCard() {
        Card plus = new Card("1_coeur+", "As de Coeur", "", List.of(), Card.Suit.COEUR, 1, false, true, true);
        List<Card> sorted = TableSort.sortCards(List.of(plus, TWO_HEARTS, ACE_HEARTS), Map.of(), CardSort.FAMILY);
        assertEquals(List.of("1_coeur", "1_coeur+", "2_coeur"), ids(sorted));
    }

    @Test
    void reelsOfTheMachineComeFirstInItsOrder() {
        List<Symbol> owned = List.of(Symbol.CHERRY, Symbol.BAR, Symbol.SWORD, Symbol.BELL, Symbol.BOMB);
        List<Symbol> machine = List.of(Symbol.BOMB, Symbol.BAR);
        assertEquals(List.of(Symbol.BOMB, Symbol.BAR, Symbol.CHERRY, Symbol.SWORD, Symbol.BELL),
            TableSort.sortReels(owned, machine, ReelSort.MACHINE_FIRST));
        assertEquals(ReelSort.MACHINE_FIRST, ReelSort.values()[0], "le tri par défaut");
    }

    @Test
    void reelsByRarityTheRarestAndMostExpensiveFirst() {
        List<Symbol> owned = List.of(Symbol.CHERRY, Symbol.SEVEN, Symbol.SWORD, Symbol.BOMB);
        assertEquals(List.of(Symbol.BOMB, Symbol.SWORD, Symbol.SEVEN, Symbol.CHERRY),
            TableSort.sortReels(owned, List.of(), ReelSort.RARITY));
    }

    @Test
    void sortsCycleBackToTheDefault() {
        CardSort card = CardSort.DECK_FIRST;
        for (int i = 0; i < CardSort.values().length; i++) card = card.next();
        assertEquals(CardSort.DECK_FIRST, card);
        assertEquals(ReelSort.MACHINE_FIRST, ReelSort.OBTAINED.next());
    }
}
