package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.loaders.CardLoader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/** Les Bingos d'un rouleau précis : à l'échoppe de l'Entraînement seulement, partout en mode ADMIN. */
class StallReelBingoTest {

    private static final Function<String, Card> CARDS = CardLoader.cardFactory(path -> {
        try {
            return Files.readString(Path.of("..", "assets").resolve(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    });

    private static final Map<String, Integer> SHOP = new LinkedHashMap<>();
    static {
        SHOP.put("bingo", 20000);
        SHOP.put("russian_roulette", 15000);
        SHOP.put(GameController.SHOP_BONUS_GAME, 0);
        SHOP.put("bingo_cherry", 0);
        SHOP.put("bingo_triple_seven", 0);
    }

    @Test
    void exploration_and_tower_sell_no_reel_bingo() {
        assertEquals(java.util.List.of("bingo", "russian_roulette"),
            java.util.List.copyOf(GameController.stallFor(SHOP, CARDS, false, false).keySet()));
    }

    @Test
    void training_keeps_reel_bingos_but_not_the_bonus_game_test_card() {
        assertEquals(java.util.List.of("bingo", "russian_roulette", "bingo_cherry", "bingo_triple_seven"),
            java.util.List.copyOf(GameController.stallFor(SHOP, CARDS, false, true).keySet()));
    }

    @Test
    void admin_keeps_everything_in_every_fight() {
        assertEquals(SHOP.keySet(), GameController.stallFor(SHOP, CARDS, true, false).keySet());
        assertEquals(SHOP.keySet(), GameController.stallFor(SHOP, CARDS, true, true).keySet());
    }

    @Test
    void the_random_shop_bingo_is_not_a_reel_bingo() {
        assertFalse(GameController.isReelBingo(CARDS.apply("bingo")));
        assertTrue(GameController.isReelBingo(CARDS.apply("bingo_bingo_light")));
    }
}
