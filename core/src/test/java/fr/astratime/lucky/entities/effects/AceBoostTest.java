package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.RankBonus;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.SpinContext;
import fr.astratime.lucky.entities.context.TurnContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** L'As de Trèfle et l'As de Cœur boostent un symbole d'attaque de la machine du joueur, jamais un absent. */
class AceBoostTest {

    /** Une machine où le BAR est le seul symbole d'attaque. */
    private static Player playerWithOnlyBar() {
        List<Symbol> reels = List.of(Symbol.BAR, Symbol.GRAPE, Symbol.BELL, Symbol.DIAMOND, Symbol.WATERMELON,
            Symbol.GOLD_BAR, Symbol.HORSESHOE, Symbol.ECU, Symbol.HEART, Symbol.CROWN, Symbol.STAR);
        Player player = new Player("Joueur", Player.BASE_HP, List.of(), RankBonus.NONE, reels);
        player.addGains(1_000);
        return player;
    }

    @Test
    void theAceOfClubsBoostsAnAttackSymbolOfTheMachine() {
        for (int i = 0; i < 50; i++) {
            PlayContext context = new PlayContext(playerWithOnlyBar());
            new AceOfClubsEffect().onPlay(context);
            SpinContext spin = new SpinContext();
            TurnContext turn = new TurnContext(spin, new CombatContext(context.getPlayer(), null));
            context.getEffectsForSpin().forEach(effect -> effect.apply(turn));
            assertTrue(spin.getWeightBoost(Symbol.BAR) > 0, "le BAR, seul symbole d'attaque de la machine");
            assertEquals(0, spin.getWeightBoost(Symbol.SEVEN), "le Sept n'est pas dans la machine");
        }
    }

    @Test
    void theAceOfHeartsBoostsAnAttackSymbolOfTheMachine() {
        for (int i = 0; i < 50; i++) {
            SpinContext spin = new SpinContext();
            new AceOfHeartsEffect().apply(new TurnContext(spin, new CombatContext(playerWithOnlyBar(), null)));
            assertTrue(spin.getWeightBoost(Symbol.BAR) > 0);
            assertEquals(0, spin.getWeightBoost(Symbol.CHERRY));
        }
    }
}
