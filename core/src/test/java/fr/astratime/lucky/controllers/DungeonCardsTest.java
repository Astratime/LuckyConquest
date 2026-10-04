package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolRegistry;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.SpinContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.events.ExecutionEvent;
import fr.astratime.lucky.entities.effects.Effect;
import fr.astratime.lucky.loaders.CardLoader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/** Les cartes trouvées dans les coffres des donjons, chargées depuis leur JSON. */
class DungeonCardsTest {

    private static final Function<String, Card> CARDS = CardLoader.cardFactory(path -> {
        try {
            return Files.readString(Path.of("..", "assets").resolve(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    });

    private final Player player = new Player("Joueur", 100, List.of());
    private final Enemy  enemy  = new Enemy("Ennemi", 10_000);

    /** Joue {@code id} : ses effets à la pose, puis ceux mis en attente pour le tirage. */
    private TurnContext play(String id) {
        PlayContext playContext = new PlayContext(player);
        Card card = CARDS.apply(id);
        card.getEffects().forEach(effect -> effect.onPlay(playContext));
        if (playContext.getGains() != 0) player.addGains(playContext.getGains());
        TurnContext turn = new TurnContext(new SpinContext(), new CombatContext(player, enemy));
        List<Effect> forSpin = new ArrayList<>(playContext.getEffectsForSpin());
        forSpin.forEach(effect -> effect.apply(turn));
        return turn;
    }

    @Test
    void whetstoneAddsThreeBladesAndAttack() {
        TurnContext turn = play("pierre_a_aiguiser");
        assertEquals(3, player.getLastingEffects().getBlades());
        assertEquals(150, turn.getCombatContext().getAttackBonus());
    }

    @Test
    void shadowDaggerIgnoresDefenseAndAddsAttackPerBladeWithoutSpendingThem() {
        player.getLastingEffects().addBlades(4);
        TurnContext turn = play("dague_de_l_ombre");
        assertTrue(turn.getCombatContext().isIgnoreDefense());
        assertEquals(4 * 60, turn.getCombatContext().getAttackBonus());
        assertEquals(4, player.getLastingEffects().getBlades(), "les Lames restent");
    }

    @Test
    void guillotineSpendsTheBladesForAShareOfTheEnemysRemainingLife() {
        player.getLastingEffects().addBlades(5);
        TurnContext turn = play("guillotine");
        assertEquals(0, player.getLastingEffects().getBlades());
        assertEquals(10, turn.getCombatContext().getExecutionPercent());

        new CombatResolver().resolve(turn.getCombatContext(), List.of(), new Symbol[3], List.of());
        assertEquals(9_000, enemy.getHp(), "10 % de 10 000 PV, défense ignorée");
    }

    @Test
    void guillotineTakesAtMostHalfOfTheEnemysLife() {
        player.getLastingEffects().addBlades(40);
        TurnContext turn = play("guillotine");
        var result = new CombatResolver().resolve(turn.getCombatContext(), List.of(), new Symbol[3], List.of());
        assertEquals(5_000, enemy.getHp());
        assertTrue(result.getEvents().stream().anyMatch(ExecutionEvent.class::isInstance));
    }

    @Test
    void guillotineWithoutBladesDoesNothing() {
        TurnContext turn = play("guillotine");
        assertEquals(0, turn.getCombatContext().getExecutionPercent());
    }

    @Test
    void fourLeafCloverBoostsGainsAndGainSymbols() {
        TurnContext turn = play("trefle_a_quatre_feuilles");
        assertEquals(41f, turn.getCombatContext().getGainMultiplier(), 1e-6);
        for (Symbol symbol : SymbolRegistry.getGainSymbols()) {
            assertEquals(30, turn.getSpinContext().getWeightBoost(symbol), symbol.name());
        }
    }

    @Test
    void fullPurseGivesGainsThenAttackFromThem() {
        TurnContext turn = play("bourse_garnie");
        assertEquals(1500, player.getGains());
        assertEquals(75, turn.getCombatContext().getAttackBonus(), "5 % de 1 500");
    }

    @Test
    void kingsFortuneDoublesGainsThenAddsTenPercentToAttack() {
        player.addGains(2000);
        TurnContext turn = play("fortune_du_roi");
        assertEquals(4000, player.getGains());
        assertEquals(400, turn.getCombatContext().getAttackBonus());
    }

    @Test
    void transfusionHealsAndOverflowFillsTheBlood() {
        player.sacrificeHp(10);
        play("transfusion");
        assertEquals(100, player.getHp());
        assertEquals(5, player.getLastingEffects().getBlood(), "15 PV de soin, 10 manquaient");
    }

    @Test
    void vampireKissDrainsThirtyPercent() {
        assertEquals(30, play("baiser_vampire").getCombatContext().getLifeDrainPercent());
    }

    @Test
    void bloodPactCostsLifeButTriplesAttackAndNeverKills() {
        TurnContext turn = play("pacte_de_sang");
        assertEquals(80, player.getHp());
        assertEquals(3f, turn.getCombatContext().getAttackFactor(), 1e-6);
        assertEquals(30, turn.getCombatContext().getLifeDrainPercent());

        player.sacrificeHp(78);
        play("pacte_de_sang");
        assertEquals(1, player.getHp(), "jamais sous 1 PV");
    }

    @Test
    void rampartGivesAShieldAndBoostsDefenseSymbols() {
        TurnContext turn = play("rempart");
        assertEquals(40, player.getShield());
        for (Symbol symbol : SymbolRegistry.getDefenseSymbols()) {
            assertEquals(30, turn.getSpinContext().getWeightBoost(symbol), symbol.name());
        }
    }

    @Test
    void cutMirrorReflectsWithoutADefenseSymbol() {
        assertEquals(200, play("miroir_taille").getCombatContext().getTotalReflectPercent());
    }

    @Test
    void roughDiamondDoublesTheVaultThenCountersWithIt() {
        LastingEffects lasting = player.getLastingEffects();
        lasting.addVault(100);
        TurnContext turn = play("diamant_brut");
        assertEquals(200, lasting.getVault());
        assertEquals(2, turn.getCombatContext().getCounterAttack());

        new CombatResolver().resolve(turn.getCombatContext(), List.of(), new Symbol[3], List.of());
        assertEquals(10_000 - 400, enemy.getHp());
    }
}
