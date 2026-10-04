package fr.astratime.lucky.loaders;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.SpinContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.progress.PlayerProfile;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Charge les vrais fichiers JSON de assets/ (sans libGDX) : toute faute de frappe
 * dans une définition de carte, un type d'effet inconnu, une image manquante ou
 * un id inconnu dans le deck de départ fait échouer ces tests.
 */
class CardLoaderTest {

    /** Les tests du module core s'exécutent depuis core/ : les assets sont à côté. */
    private static final Path ASSETS = Path.of("..", "assets");

    private static final CardLoader.AssetReader READER = path -> {
        try {
            return Files.readString(ASSETS.resolve(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    };

    @Test
    void everyCardDefinitionLoads() {
        List<Card> cards = CardLoader.loadAll(READER);

        assertEquals(52 + 17 + 12 + 19, cards.size(),
            "4 suites de 13 cartes + 17 cartes spéciales + 12 cartes des donjons + 19 cartes de test (Bingo par symbole)");
        assertEquals(cards.size(), cards.stream().map(Card::getId).distinct().count(), "les ids doivent être uniques");
    }

    @Test
    void everyCardImageExists() {
        for (Card card : CardLoader.loadAll(READER)) {
            assertTrue(Files.exists(ASSETS.resolve(card.getAssetPath())),
                card.getId() + " : image introuvable " + card.getAssetPath());
        }
    }

    @Test
    void everyCardShowsSomethingWhenPlayed() {
        for (Card card : CardLoader.loadAll(READER)) {
            assertFalse(card.getEffects().isEmpty(), card.getId() + " n'a aucun effet");
            PlayContext context = new PlayContext(new Player("Joueur", 100, List.of()));
            card.getEffects().forEach(effect -> effect.onPlay(context));
            assertFalse(context.getPopups().isEmpty(), card.getId() + " n'affiche aucun texte quand elle est jouée");
        }
    }

    @Test
    void starterDeckHasTwentyCardsTheAcesFacesAndFourSpecials() {
        List<Card> deck = CardLoader.loadStarterDeck(READER);
        Map<String, Long> copies = deck.stream().collect(Collectors.groupingBy(Card::getId, Collectors.counting()));

        assertEquals(PlayerProfile.DECK_SIZE, deck.size());
        for (String special : List.of("draw_2", "joker", "gain_500", "lucky_charm")) {
            assertEquals(1L, copies.get(special), special);
        }
        for (String suit : List.of("coeur", "trefle", "carreau", "pique")) {
            for (int rank : List.of(1, 11, 12, 13)) {
                assertEquals(1L, copies.get(rank + "_" + suit), rank + "_" + suit);
            }
        }
        assertNull(copies.get("magnet"), "l'Aimant n'est plus dans le deck de départ");
    }

    @Test
    void startingCollectionHoldsTheOldStarterCardsWithoutInTheSleeve() {
        Map<String, Integer> collection = CardLoader.loadStartingCollection(READER);

        assertFalse(collection.containsKey("in_the_sleeve"), "Dans la manche sera débloquée dans un autre donjon");
        assertEquals(2, collection.get("gain_500"), "Dans la manche est remplacée par un Gains +500");
        assertEquals(2, collection.get("draw_2"));
        assertEquals(27, collection.values().stream().mapToInt(Integer::intValue).sum());
        for (Map.Entry<String, Integer> entry : CardLoader.loadStarterDeckCopies(READER).entrySet()) {
            assertTrue(collection.getOrDefault(entry.getKey(), 0) >= entry.getValue(),
                "le deck de départ ne contient que des cartes possédées : " + entry.getKey());
        }
        assertDoesNotThrow(() -> CardLoader.loadDeck(collection, READER), "toutes les cartes de la collection existent");
    }

    @Test
    void eachCopyInTheStarterDeckIsADistinctCard() {
        List<Card> deck = CardLoader.loadStarterDeck(READER);
        Map<Card, Boolean> unique = new IdentityHashMap<>();
        deck.forEach(card -> unique.put(card, true));

        assertEquals(deck.size(), unique.size(), "deux exemplaires ne doivent pas partager la même instance");
    }

    @Test
    void unknownIdInTheStarterDeckIsRejected() {
        CardLoader.AssetReader withBadDeck = path -> path.endsWith("starter.json")
            ? "[ { \"id\": \"carte_inexistante\", \"copies\": 1 } ]"
            : READER.read(path);

        assertThrows(IllegalArgumentException.class, () -> CardLoader.loadStarterDeck(withBadDeck));
    }

    @Test
    void unknownEffectTypeIsRejected() {
        CardLoader.AssetReader withBadEffect = path -> path.endsWith("special.json")
            ? "[ { \"id\": \"x\", \"name\": \"X\", \"assetPath\": \"x.png\", \"effects\": [ { \"type\": \"INCONNU\" } ] } ]"
            : READER.read(path);

        assertThrows(IllegalArgumentException.class, () -> CardLoader.loadAll(withBadEffect));
    }

    @Test
    void potDeLutinIsAConsumableCardOutsideTheStarterDeck() {
        Card pot = CardLoader.cardFactory(READER).apply("pot_de_lutin");

        assertTrue(pot.isConsumable());
        assertTrue(CardLoader.loadStarterDeck(READER).stream().noneMatch(c -> c.getId().equals("pot_de_lutin")));
    }

    @Test
    void testBingoCardsForceTheJackpotOfTheirSymbol() {
        for (Symbol symbol : Symbol.values()) {
            if (symbol == Symbol.JOKER) continue;
            Card card = CardLoader.cardFactory(READER).apply("bingo_" + symbol.name().toLowerCase());
            TurnContext context = new TurnContext(new SpinContext(),
                new CombatContext(new Player("Joueur", 100, List.of()), new Enemy("Ennemi", 100)));
            card.getEffects().forEach(effect -> effect.apply(context));

            assertTrue(context.getSpinContext().isJackpotForced(), card.getId());
            assertEquals(symbol, context.getSpinContext().getJackpotSymbol(), card.getId());
        }
    }

    @Test
    void shopSellsConsumableCardsThatAreNotInTheStarterDeck() {
        Map<String, Integer> shop = CardLoader.loadShop(READER);
        Map<String, Integer> expected = new java.util.LinkedHashMap<>();
        expected.put("bingo", 10000);
        expected.put("russian_roulette", 8000);
        expected.put("corruption", 5000);
        for (Symbol symbol : Symbol.values()) {
            if (symbol != Symbol.JOKER) expected.put("bingo_" + symbol.name().toLowerCase(), 0); // cartes de test
        }
        assertEquals(List.copyOf(expected.entrySet()), List.copyOf(shop.entrySet()), "prix et ordre de l'échoppe");

        List<String> starter = CardLoader.loadStarterDeck(READER).stream().map(Card::getId).toList();
        for (String id : shop.keySet()) {
            assertTrue(CardLoader.cardFactory(READER).apply(id).isConsumable(), id + " doit être consommable");
            assertFalse(starter.contains(id), id + " ne s'obtient qu'à l'échoppe");
        }
    }

    @Test
    void everyReelSymbolHasItsBingoCard() {
        java.util.Set<String> ids = new java.util.HashSet<>();
        for (Card card : CardLoader.loadAll(READER)) ids.add(card.getId());
        List<fr.astratime.lucky.entities.Symbol> symbols =
            new java.util.ArrayList<>(fr.astratime.lucky.entities.SymbolRegistry.getAttackSymbols());
        symbols.addAll(fr.astratime.lucky.entities.SymbolRegistry.getDefenseSymbols());
        symbols.addAll(fr.astratime.lucky.entities.SymbolRegistry.getGainSymbols());
        for (fr.astratime.lucky.entities.Symbol symbol : symbols) {
            String id = fr.astratime.lucky.controllers.GameController.bingoGiftId(symbol);
            assertTrue(ids.contains(id), "carte Bingo offerte manquante : " + id);
        }
    }

    @Test
    void theBoutiqueSellsKnownCardsButNeitherChestNorCombatShopCards() {
        Map<String, Long> boutique = CardLoader.loadBoutique(READER);
        assertEquals(10_000_000L, boutique.get("in_the_sleeve"), "le prix donné par Astra");
        java.util.Set<String> shop = CardLoader.loadShop(READER).keySet();
        for (Map.Entry<String, Long> entry : boutique.entrySet()) {
            Card card = CardLoader.cardFactory(READER).apply(entry.getKey());
            assertTrue(entry.getValue() > 0, card.getId());
            assertFalse(shop.contains(card.getId()), card.getId() + " reste dans l'échoppe du combat");
        }
        for (String chest : List.of("pierre_a_aiguiser", "guillotine", "fortune_du_roi", "diamant_brut")) {
            assertFalse(boutique.containsKey(chest), chest + " ne s'obtient qu'au coffre");
        }
    }
}
