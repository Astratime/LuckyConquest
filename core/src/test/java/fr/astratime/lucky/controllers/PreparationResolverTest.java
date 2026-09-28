package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Combo;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.effects.AttackEffect;
import fr.astratime.lucky.entities.effects.BoostSymbolEffect;
import fr.astratime.lucky.entities.effects.MultiplierEffect;
import fr.astratime.lucky.entities.events.ComboEvent;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PreparationResolverTest {

    @Test
    void appliesEveryPendingEffectToAFreshTurnContext() {
        Player player = new Player("Joueur", 100, List.of());
        Enemy  enemy  = new Enemy("Ennemi", 1000);

        TurnContext context = new PreparationResolver().resolve(
            List.of(new AttackEffect(10), new AttackEffect(5), new MultiplierEffect(2f), new BoostSymbolEffect(Symbol.BELL, 40)),
            player, enemy);

        assertEquals(15, context.getCombatContext().getAttackBonus());
        assertEquals(3f, context.getCombatContext().getGainMultiplier(), 1e-6);
        assertEquals(40, context.getSpinContext().getWeightBoost(Symbol.BELL));
        assertEquals(1, context.getEvents().size(), "le boost de symbole est journalisé");
        assertSame(player, context.getCombatContext().getPlayer());
    }

    /** @return un joueur qui vient de jouer {@code played} (piochées puis posées). */
    private static Player playerWhoPlayed(Card... played) {
        Player player = new Player("Joueur", 100, List.of(played));
        player.draw(played.length);
        for (Card card : played) player.playCard(card);
        return player;
    }

    private static Card suited(int rank, Card.Suit suit) {
        return new Card(suit.cardId(rank), suit.cardId(rank), "x.png", List.of(), suit, rank);
    }

    @Test
    void aBrelanMultipliesGainsAndAttackWithoutItsOwnPair() {
        Player player = playerWhoPlayed(suited(13, Card.Suit.COEUR), suited(13, Card.Suit.PIQUE),
            suited(13, Card.Suit.TREFLE), suited(1, Card.Suit.COEUR));

        TurnContext context = new PreparationResolver().resolve(List.of(), player, new Enemy("Ennemi", 1000));

        assertEquals(Combo.BRELAN.getFactor(), context.getCombatContext().getGainFactor(), 1e-6, "seul le Brelan compte");
        assertEquals(Combo.BRELAN.getFactor(), context.getCombatContext().getAttackFactor(), 1e-6);
        assertEquals(List.of(Combo.BRELAN), context.getEvents().stream()
            .filter(ComboEvent.class::isInstance).map(event -> ((ComboEvent) event).combo).toList());
    }

    @Test
    void combosStackTheirMultipliers() {
        Player player = playerWhoPlayed(suited(11, Card.Suit.COEUR), suited(12, Card.Suit.COEUR),
            suited(13, Card.Suit.COEUR));

        TurnContext context = new PreparationResolver().resolve(List.of(), player, new Enemy("Ennemi", 1000));

        float both = Combo.COULEUR.getFactor() * Combo.SUITE.getFactor();
        assertEquals(both, context.getCombatContext().getGainFactor(), 1e-6, "Couleur et Suite se cumulent");
        assertEquals(both, context.getCombatContext().getAttackFactor(), 1e-6);
        assertEquals(List.of(Combo.COULEUR, Combo.SUITE), context.getEvents().stream()
            .filter(ComboEvent.class::isInstance).map(event -> ((ComboEvent) event).combo).toList());
    }

    @Test
    void noComboFormedLeavesTheTurnUntouched() {
        Player player = playerWhoPlayed(suited(1, Card.Suit.COEUR), suited(12, Card.Suit.PIQUE));

        TurnContext context = new PreparationResolver().resolve(List.of(), player, new Enemy("Ennemi", 1000));

        assertEquals(1f, context.getCombatContext().getGainFactor(), 1e-6);
        assertEquals(1f, context.getCombatContext().getAttackFactor(), 1e-6);
        assertTrue(context.getEvents().isEmpty(), "aucun texte de combinaison ratée");
    }

    @Test
    void aStraightFillsTheGaugeOfEachPlayedCard() {
        Player player = playerWhoPlayed(suited(11, Card.Suit.PIQUE), suited(12, Card.Suit.COEUR),
            suited(13, Card.Suit.CARREAU));

        new PreparationResolver().resolve(List.of(), player, new Enemy("Ennemi", 1000));

        assertEquals(PreparationResolver.COMBO_BLADES, player.getLastingEffects().getBlades());
        assertEquals(PreparationResolver.COMBO_BLOOD, player.getLastingEffects().getBlood());
        assertEquals(PreparationResolver.COMBO_VAULT, player.getLastingEffects().getVault());
    }
}
