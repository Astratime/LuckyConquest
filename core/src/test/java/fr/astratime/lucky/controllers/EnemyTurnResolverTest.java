package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.enemy.EnemyCards;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.events.DamageReflectedEvent;
import fr.astratime.lucky.entities.events.EnemyHealedEvent;
import fr.astratime.lucky.entities.events.EnemyShieldedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.PlayerDamagedEvent;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class EnemyTurnResolverTest {

    private static Card card(Card.Suit suit, int rank) {
        return EnemyCards.card(suit, rank);
    }

    @Test
    void theEnemyDrawsEightPlaysThreeAndDiscardsThemAll() {
        Enemy  enemy  = new Enemy("Ennemi", 5000);
        Player player = new Player("Joueur", 10_000, List.of());

        EnemyTurnResult turn = new EnemyTurnResolver(new Random(1)).resolve(enemy, player, 0, 0f);

        assertEquals(Enemy.HAND_SIZE, turn.drawn().size());
        assertEquals(Enemy.PLAYS_PER_TURN, turn.played().size());
        assertTrue(turn.drawn().containsAll(turn.played()));
        assertEquals(Enemy.HAND_SIZE, enemy.getDiscardCards().size(), "jouées ou non, toutes partent à la défausse");
        assertEquals(20 - Enemy.HAND_SIZE, enemy.getDeckCards().size());
        assertEquals(3, turn.symbols().length);
        assertEquals(3, turn.outcomes().size());
    }

    @Test
    void eachSymbolDoesItsJobBoostedByThePlayedCards() {
        // Deck : les 3 cartes jouées (Pique 14, Cœur 14, Carreau 14) et 5 cartes de remplissage.
        List<Card> cards = new ArrayList<>(List.of(card(Card.Suit.PIQUE, 14), card(Card.Suit.COEUR, 14),
            card(Card.Suit.CARREAU, 14)));
        for (int i = 0; i < 5; i++) cards.add(card(Card.Suit.TREFLE, 2));
        for (int seed = 0; seed < 30; seed++) {
            Enemy  enemy  = new Enemy("Ennemi", 10_000, cards);
            enemy.takeDamage(9_000);
            Player player = new Player("Joueur", 10_000, List.of());

            EnemyTurnResult turn = new EnemyTurnResolver(new Random(seed)).resolve(enemy, player, 0, 0f);

            assertTrue(turn.played().stream().allMatch(card -> card.getRank() == 14), "les meilleures cartes d'abord");
            int swords = 0, shields = 0, hp = 1_000;
            for (int i = 0; i < 3; i++) {
                Event event = turn.outcomes().get(i).get(0);
                switch (turn.symbols()[i]) {
                    case SWORD  -> { swords++;  assertEquals(20 + 10, ((PlayerDamagedEvent) event).damage); }
                    case SHIELD -> { shields++; assertEquals(100 + 600, ((EnemyShieldedEvent) event).defense); }
                    case POTION -> {
                        int healed = Math.min(5_000, 10_000 - hp); // 30 % + 20 % des PV max, plafonné
                        assertEquals(healed, ((EnemyHealedEvent) event).amount);
                        hp += healed;
                    }
                }
            }
            assertEquals(10_000 - swords * 30, player.getHp());
            assertEquals(Enemy.BASE_DEFENSE + shields * 700, enemy.getDefense());
            assertEquals(hp, enemy.getHp());
        }
    }

    @Test
    void theShieldDefenseOnlyLastsUntilTheEnemysNextTurn() {
        Enemy enemy = new Enemy("Ennemi", 5000);
        enemy.addShieldDefense(700);
        assertEquals(Enemy.BASE_DEFENSE + 700, enemy.getDefense());

        EnemyTurnResult turn = new EnemyTurnResolver(new Random(2))
            .resolve(enemy, new Player("Joueur", 10_000, List.of()), 0, 0f);

        int shieldsThisTurn = turn.events().stream()
            .filter(e -> e instanceof EnemyShieldedEvent).mapToInt(e -> ((EnemyShieldedEvent) e).defense).sum();
        assertEquals(Enemy.BASE_DEFENSE + shieldsThisTurn, enemy.getDefense(),
            "sa défense ne compte que la base et les Boucliers de ce tour");
    }

    @Test
    void thePlayersShieldAbsorbsSwordsAndTheReflectHitsBack() {
        List<Card> spades = new ArrayList<>();
        for (int i = 0; i < 8; i++) spades.add(card(Card.Suit.PIQUE, 1)); // +5 par Épée
        int attacked = 0;
        for (int seed = 0; seed < 40 && attacked == 0; seed++) {
            Enemy  enemy  = new Enemy("Ennemi", 1000, spades);
            Player player = new Player("Joueur", 1000, List.of());
            player.addShield(1000);

            EnemyTurnResult turn = new EnemyTurnResolver(new Random(seed)).resolve(enemy, player, 50, 0f);

            int swords = (int) java.util.Arrays.stream(turn.symbols()).filter(s -> s == EnemySymbol.SWORD).count();
            if (swords == 0) continue;
            attacked = swords;
            assertEquals(1000, player.getHp(), "le bouclier absorbe les Épées");
            int reflected = Math.round(swords * (20 + 3 * 5) * 0.5f);
            assertTrue(turn.afterEvents().stream().anyMatch(e -> e instanceof DamageReflectedEvent r && r.damage == reflected));
        }
        assertTrue(attacked > 0, "au moins un tirage avec une Épée");
    }

    @Test
    void theAiHealsFirstWhenLowAndAttacksOtherwise() {
        List<Card> hand = List.of(card(Card.Suit.COEUR, 5), card(Card.Suit.PIQUE, 2), card(Card.Suit.PIQUE, 14),
            card(Card.Suit.CARREAU, 8), card(Card.Suit.TREFLE, 11), card(Card.Suit.COEUR, 11));

        assertEquals(List.of(card(Card.Suit.PIQUE, 14).getId(), card(Card.Suit.PIQUE, 2).getId(),
                card(Card.Suit.CARREAU, 8).getId()),
            EnemyTurnResolver.choose(hand, 1f).stream().map(Card::getId).toList());
        assertEquals(List.of(card(Card.Suit.COEUR, 11).getId(), card(Card.Suit.COEUR, 5).getId(),
                card(Card.Suit.CARREAU, 8).getId()),
            EnemyTurnResolver.choose(hand, 0.3f).stream().map(Card::getId).toList());
    }

    @Test
    void cardBonusesGrowWithTheRank() {
        assertEquals(5, EnemyCards.swordBonus(card(Card.Suit.PIQUE, 1)));
        assertEquals(10, EnemyCards.swordBonus(card(Card.Suit.PIQUE, 14)));
        assertEquals(300, EnemyCards.shieldBonus(card(Card.Suit.CARREAU, 1)));
        assertEquals(600, EnemyCards.shieldBonus(card(Card.Suit.CARREAU, 14)));
        assertEquals(10, EnemyCards.luckBonus(card(Card.Suit.TREFLE, 1)));
        assertEquals(50, EnemyCards.luckBonus(card(Card.Suit.TREFLE, 14)));
        assertEquals(10, EnemyCards.healBonus(card(Card.Suit.COEUR, 1)));
        assertEquals(20, EnemyCards.healBonus(card(Card.Suit.COEUR, 14)));
        assertEquals(20, EnemyCards.starterDeck().size());
    }
}
