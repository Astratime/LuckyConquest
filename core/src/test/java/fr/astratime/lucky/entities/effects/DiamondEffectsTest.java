package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolRegistry;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.context.SpinContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiamondEffectsTest {

    private final TurnContext context = new TurnContext(new SpinContext(),
        new CombatContext(new Player("Joueur", 100, List.of()), new Enemy("Ennemi", 1000)));

    @Test
    void diamondCardBoostsEveryDefenseSymbolAndAddsReflect() {
        new DiamondReflectEffect(3.75f, 39).apply(context);

        List<Symbol> defenseSymbols = SymbolRegistry.getDefenseSymbols();
        assertFalse(defenseSymbols.isEmpty());
        for (Symbol symbol : defenseSymbols) {
            assertEquals(39, context.getSpinContext().getWeightBoost(symbol), symbol.name());
        }
        assertEquals(3.75f, context.getCombatContext().getReflectHpBonus(), 1e-6);
    }

    @Test
    void diamondCardsStack() {
        new DiamondReflectEffect(3.5f, 33).apply(context);
        new DiamondReflectEffect(3.75f, 39).apply(context);

        assertEquals(72, context.getSpinContext().getWeightBoost(SymbolRegistry.getDefenseSymbols().get(0)));
        assertEquals(7.25f, context.getCombatContext().getReflectHpBonus(), 1e-6);
    }

    @Test
    void diamondCardDoesNotBoostAttackSymbols() {
        new DiamondReflectEffect(3.75f, 39).apply(context);

        for (Symbol symbol : SymbolRegistry.getAttackSymbols()) {
            assertEquals(0, context.getSpinContext().getWeightBoost(symbol), symbol.name());
        }
    }

    @Test
    void diamondCardShowsReflectAndDefenseBoost() {
        List<String> texts = new DiamondReflectEffect(3.75f, 39).getPopups().stream().map(EffectPopup::getText).toList();

        assertEquals(List.of("RENVOI 3,75% DES PV", "BOOST DÉFENSE +39"), texts);
    }

    @Test
    void aceOfDiamondsNoLongerReflects() {
        new AceOfDiamondsEffect().apply(context);

        assertEquals(0, context.getCombatContext().getTotalReflectPercent(), "renvoi de 500 % supprimé (Astra)");
        assertEquals(0, context.getCombatContext().getCounterAttack(), "plus de contre-attaque non plus");
    }

    @Test
    void diamondCardsAddUpTheVaultMultiplier() {
        new DiamondReflectEffect(2f, 6, 50).apply(context);
        new DiamondReflectEffect(7.5f, 39, 150).apply(context);

        assertEquals(3f, context.getCombatContext().getVaultMultiplier(), 1e-6, "x1 + 0,5 + 1,5");
    }
}
