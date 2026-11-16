package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.RankBonus;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.loaders.CardLoader;
import fr.astratime.lucky.progress.CombatLog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Le journal détaillé des combats note la main, les cartes jouées et le tirage, dans l'ordre. */
class CombatLogTest {

    private static final Path ASSETS = Path.of("..", "assets");

    private static final Function<String, Card> CARDS = CardLoader.cardFactory(path -> {
        try {
            return Files.readString(ASSETS.resolve(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    });

    @Test
    void turnIsLoggedInOrder() {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 10; i++) deck.add(CARDS.apply("11_pique"));
        GameController controller = new GameController(() -> new ArrayList<>(deck), CARDS, Map.of(),
            cards -> new Player("Joueur", Player.BASE_HP, cards, RankBonus.NONE, Symbol.classicReels()));
        List<String> lines = new ArrayList<>();
        controller.setCombatLog(lines::add);
        controller.restart();
        controller.drawCards();
        Card first = controller.getGameState().getPlayer().getCurrentHand().get(0);
        controller.playCard(first);
        controller.spin();

        assertEquals(4, lines.size(), String.join("\n", lines));
        assertTrue(lines.get(0).startsWith("TOUR 1 "), lines.get(0));
        assertTrue(lines.get(0).contains("main=11_pique"), lines.get(0));
        assertEquals("JOUE 11_pique", lines.get(1));
        assertTrue(lines.get(2).startsWith("TIRAGE mise=0 "), lines.get(2));
        assertTrue(lines.get(3).startsWith("ENNEMI pv_perdus="), lines.get(3));
    }

    @Test
    void lineIsAppendedToFile(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("combats.log");
        CombatLog.append(file, "COMBAT a");
        CombatLog.append(file, "FIN b");
        assertEquals(List.of("COMBAT a", "FIN b"), Files.readAllLines(file));
    }
}
