package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Archetype;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.effects.ArchetypeStatsEffect;
import fr.astratime.lucky.entities.effects.Effect;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.loaders.CardLoader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Les 7 cartes de la Menue monnaie, du 2 au 8 (Astra, 2026-11-23). */
class SmallChangeCardsTest {

    private static final Path ASSETS = Path.of("..", "assets");
    private static final CardLoader.AssetReader READER = path -> {
        try {
            return Files.readString(ASSETS.resolve(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    };
    private static final List<Card> ALL = CardLoader.loadAll(READER);

    private static Card card(String id) {
        return ALL.stream().filter(c -> c.getId().equals(id)).findFirst().orElseThrow();
    }

    private static Player playing(Card... cards) {
        Player player = new Player("Joueur", 1_000_000, List.of(cards));
        player.draw(cards.length);
        for (Card card : cards) assertTrue(player.playCard(card));
        return player;
    }

    private static TurnContext resolve(Player player, List<Effect> pending) {
        return new PreparationResolver().resolve(pending, player, new Enemy(EnemyKind.ENTRAINEMENT, List.of()));
    }

    private static List<Effect> effects(boolean statsOnly, Card... cards) {
        List<Effect> out = new ArrayList<>();
        for (Card card : cards) for (Effect e : card.getEffects()) {
            if (!statsOnly || e instanceof ArchetypeStatsEffect) out.add(e);
        }
        return out;
    }

    @Test
    void theFamilyRunsFromTwoToEight() {
        List<Integer> ranks = ALL.stream().filter(c -> c.getArchetype() == Archetype.MENUE_MONNAIE).map(Card::getRank)
            .sorted().toList();
        assertEquals(List.of(2, 3, 4, 5, 6, 7, 8), ranks);
    }

    @Test
    void eachSmallCardPlayedAddsTheSmallChangeAttack() {
        // 3, 4, 5 de la Menue monnaie : Suite + Couleur. Bas de laine 10 % + Bourse de cuivre 15 % = 25 % par petite carte.
        Card[] cards = {card("bas_de_laine"), card("monnaie_rendue"), card("bourse_de_cuivre")};
        assertEquals(3, PreparationResolver.smallCards(playing(cards).getPlayedCards()));
        TurnContext base = resolve(playing(cards), effects(true, cards));
        TurnContext full = resolve(playing(cards), effects(false, cards));
        assertEquals(25, full.getCombatContext().getSmallChangePercent());
        // Puissance (2026-11-18) : les bonus s'additionnent, 3 petites cartes x 25 % = +0,75.
        assertEquals(0.75f, full.getCombatContext().getAttackFactor() - base.getCombatContext().getAttackFactor(), 1e-4);
    }

    @Test
    void faceCardsDoNotCount() {
        assertEquals(0, PreparationResolver.smallCards(playing(card("heraut"), card("douze_coups")).getPlayedCards()));
    }
}
