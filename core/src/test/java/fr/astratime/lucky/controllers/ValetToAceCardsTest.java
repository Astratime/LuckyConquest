package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Archetype;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.CourtAndClock;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.effects.ArchetypeStatsEffect;
import fr.astratime.lucky.entities.effects.Effect;
import fr.astratime.lucky.entities.enemy.EnemyCards;
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

/** Les 12 cartes des archétypes du Valet à l'As : Cour royale, Revenant et Horloger (Astra, 2026-11-08). */
class ValetToAceCardsTest {

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

    /** @return un joueur qui a en main {@code cards}, et les a jouées. */
    private static Player playing(Card... cards) {
        Player player = new Player("Joueur", 1_000_000, List.of(cards));
        player.draw(cards.length);
        for (Card card : cards) assertTrue(player.playCard(card));
        return player;
    }

    private static TurnContext resolve(Player player, List<Effect> pending) {
        return new PreparationResolver().resolve(pending, player, new Enemy(EnemyKind.ENTRAINEMENT, List.of()));
    }

    private static List<Effect> effects(Card... cards) {
        List<Effect> out = new ArrayList<>();
        for (Card card : cards) out.addAll(card.getEffects());
        return out;
    }

    private static List<Effect> statsOnly(Card... cards) {
        return effects(cards).stream().filter(ArchetypeStatsEffect.class::isInstance).toList();
    }

    @Test
    void eachValetToAceArchetypeIsJackQueenKingAce() {
        for (Archetype archetype : List.of(Archetype.COUR, Archetype.REVENANT, Archetype.HORLOGER)) {
            List<Integer> ranks = ALL.stream().filter(c -> c.getArchetype() == archetype).map(Card::getRank).sorted().toList();
            assertEquals(List.of(1, 11, 12, 13), ranks, archetype.name());
        }
    }

    @Test
    void theRoyalCourtRewardsTheCombosOnTheAttackOnly() {
        Card[] court = {card("heraut"), card("dame_de_cour"), card("sceptre_du_roi"), card("quinte_royale")};
        TurnContext base = resolve(playing(court), statsOnly(court));
        Player player = playing(court);
        TurnContext full = resolve(player, effects(court));
        // Valet, Dame, Roi, As de la Cour : Suite + Couleur (x10). Dame : 2 combinaisons x 25 %, Quinte royale x2.
        assertEquals(3f, full.getCombatContext().getAttackFactor() / base.getCombatContext().getAttackFactor(), 1e-4);
        assertEquals(base.getCombatContext().getGainFactor(), full.getCombatContext().getGainFactor(), 1e-4,
            "les gains ne changent pas");
        CourtAndClock clock = player.getLastingEffects().getCourtAndClock();
        assertEquals(5f, clock.getSceptre(), 1e-4, "la moitié de x10 reste pour le tirage suivant");
        player.discardHand();
        TurnContext next = resolve(player, List.of());
        assertEquals(5f, next.getCombatContext().getAttackFactor(), 1e-4);
        assertEquals(1f, next.getCombatContext().getGainFactor(), 1e-4, "le Sceptre ne touche pas aux gains");
        assertEquals(1f, clock.getSceptre(), 1e-4, "il sert une seule fois");
    }

    @Test
    void theRoyalFlushNeedsBothAStraightAndAFlush() {
        Card flush = card("quinte_royale");
        Card[] straightOnly = {flush, EnemyCards.card(Card.Suit.PIQUE, 13), EnemyCards.card(Card.Suit.COEUR, 12)};
        TurnContext base = resolve(playing(straightOnly), statsOnly(straightOnly));
        TurnContext played = resolve(playing(straightOnly), effects(straightOnly));
        assertEquals(1f, played.getCombatContext().getAttackFactor() / base.getCombatContext().getAttackFactor(), 1e-4);
    }

    @Test
    void theHeraldCallsAFaceCardAndAnAceFromTheDrawPile() {
        Player player = new Player("Joueur", 100, List.of(EnemyCards.card(Card.Suit.PIQUE, 5),
            EnemyCards.card(Card.Suit.COEUR, 12), EnemyCards.card(Card.Suit.TREFLE, 1), EnemyCards.card(Card.Suit.PIQUE, 7)));
        PlayContext context = new PlayContext(player);
        card("heraut").getEffects().forEach(effect -> effect.onPlay(context));
        List<Integer> ranks = context.getFetched().stream().map(Card::getRank).sorted().toList();
        assertEquals(List.of(1, 12), ranks);
        assertEquals(2, player.getDeck().getCards().size(), "elles ont quitté la pioche");
    }

    @Test
    void theGravediggerTakesBackTheTopOfTheDiscardPile() {
        Player player = new Player("Joueur", 100, List.of());
        Card first = EnemyCards.card(Card.Suit.PIQUE, 3);
        Card top = EnemyCards.card(Card.Suit.COEUR, 9);
        player.getDiscardPile().addAll(List.of(first, top));
        PlayContext context = new PlayContext(player);
        card("fossoyeur").getEffects().forEach(effect -> effect.onPlay(context));
        assertEquals(List.of(top), context.getFetched());
        assertEquals(List.of(first), player.getDiscardPile().getCards());
    }

    @Test
    void theWhiteLadyCountsTheDiscardPileWithACap() {
        Player player = new Player("Joueur", 100, List.of());
        for (int i = 0; i < 8; i++) player.getDiscardPile().add(EnemyCards.card(Card.Suit.PIQUE, 2));
        assertEquals(1.4f, attackOf(player, card("dame_blanche")), 1e-4, "8 cartes x 5 %");
        for (int i = 0; i < 40; i++) player.getDiscardPile().add(EnemyCards.card(Card.Suit.PIQUE, 2));
        assertEquals(2f, attackOf(player, card("dame_blanche")), 1e-4, "+100 % au plus");
    }

    /** @return le multiplicateur d'attaque des effets (sauf l'archétype) de {@code card} jouée par {@code player}. */
    private static float attackOf(Player player, Card card) {
        PlayContext context = new PlayContext(player);
        card.getEffects().stream().filter(e -> !(e instanceof ArchetypeStatsEffect)).forEach(effect -> effect.onPlay(context));
        TurnContext turn = new TurnContext(new fr.astratime.lucky.entities.context.SpinContext(),
            new fr.astratime.lucky.entities.context.CombatContext(player, new Enemy(EnemyKind.ENTRAINEMENT, List.of())));
        context.getEffectsForSpin().forEach(effect -> effect.apply(turn));
        return turn.getCombatContext().getAttackFactor();
    }

    @Test
    void theShadowKingReplaysTheLastCardOfThePreviousTurn() {
        Player player = new Player("Joueur", 100, List.of());
        Card shadow = card("roi_des_ombres");
        Card lady = card("dame_de_cour");
        player.getLastingEffects().getCourtAndClock().rememberPlayed(List.of(lady, shadow));
        PlayContext context = new PlayContext(player);
        shadow.getEffects().forEach(effect -> effect.onPlay(context));
        assertTrue(context.getEffectsForSpin().containsAll(lady.getEffects()),
            "la Dame de cour est rejouée (le Roi des ombres ne se rejoue pas lui-même)");
    }

    @Test
    void theRequiemShufflesTheDiscardPileBackForBlades() {
        Player player = new Player("Joueur", 100, List.of());
        for (int i = 0; i < 9; i++) player.getDiscardPile().add(EnemyCards.card(Card.Suit.PIQUE, 2));
        card("requiem").getEffects().forEach(effect -> effect.onPlay(new PlayContext(player)));
        assertTrue(player.getDiscardPile().isEmpty());
        assertEquals(9, player.getDeck().getCards().size());
        assertEquals(2, player.getLastingEffects().getBlades(), "1 Lame par 4 cartes");
        for (int i = 0; i < 60; i++) player.getDiscardPile().add(EnemyCards.card(Card.Suit.PIQUE, 2));
        card("requiem").getEffects().forEach(effect -> effect.onPlay(new PlayContext(player)));
        assertEquals(7, player.getLastingEffects().getBlades(), "5 Lames au plus par Requiem");
    }

    @Test
    void springsCuckoosAndTheMinuteHandWorkOverTheTurns() {
        CourtAndClock clock = new CourtAndClock();
        assertEquals(10, clock.addSprings(14, 5), "10 Ressorts au plus");
        assertEquals(50, clock.getSpringAttack());
        assertEquals(10, clock.takeSprings());
        clock.addCuckoo(2, 1.5f);
        assertEquals(1f, clock.tickCuckoos(), 1e-4);
        assertEquals(1.5f, clock.tickCuckoos(), 1e-4, "il chante au 2e tirage");
        assertEquals(1f, clock.tickCuckoos(), 1e-4, "une seule fois");
        clock.addMinuteHand(3, 1, 5);
        for (int i = 0; i < 4; i++) clock.tickMinuteHand();
        assertEquals(3, clock.getSprings(), "un Ressort par tour pendant 3 tours");
        assertTrue(new CourtAndClock().isEmpty());
    }

    @Test
    void twelveStrokesEmptiesTheSpringsForABigHit() {
        Player player = new Player("Joueur", 100, List.of());
        player.getLastingEffects().getCourtAndClock().addSprings(4, 5);
        TurnContext turn = resolve(player, card("douze_coups").getEffects().stream()
            .filter(e -> !(e instanceof ArchetypeStatsEffect)).toList());
        // Les 4 Ressorts donnent +20 % au début du tirage, puis les Douze coups les vident : +80 %.
        assertEquals(1.2f * 1.8f, turn.getCombatContext().getAttackFactor(), 1e-4);
        assertEquals(0, player.getLastingEffects().getCourtAndClock().getSprings());
    }

    @Test
    void theWindingKeyAndTheCuckooStartOnTheNextSpin() {
        Player player = new Player("Joueur", 100, List.of());
        List<Effect> key = card("cle_de_remontage").getEffects().stream().filter(e -> !(e instanceof ArchetypeStatsEffect)).toList();
        assertEquals(1f, resolve(player, key).getCombatContext().getAttackFactor(), 1e-4, "pas au tirage où elle est jouée");
        assertEquals(1.1f, resolve(player, List.of()).getCombatContext().getAttackFactor(), 1e-4, "2 Ressorts x 5 %");
    }
}
