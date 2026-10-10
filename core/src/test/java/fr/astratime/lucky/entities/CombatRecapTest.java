package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.JackpotEvent;
import fr.astratime.lucky.entities.events.PlayerDamagedEvent;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Le récapitulatif de fin de combat : tours, dégâts, plus gros coup, Bingos, cartes jouées. */
class CombatRecapTest {

    private static TurnResult turn(Event... events) {
        Symbol[] symbols = { Symbol.SEVEN, Symbol.BAR, Symbol.CHERRY };
        return new TurnResult(List.of(events), symbols, 0, List.of(), List.of(), List.of());
    }

    private static EnemyDamagedEvent hit(long damage) {
        return new EnemyDamagedEvent(damage, damage, 0, 0, false);
    }

    @Test
    void theRecapAddsUpTheTurnsOfTheFight() {
        CombatRecap recap = new CombatRecap();
        recap.cardPlayed();
        recap.cardPlayed();
        recap.record(turn(hit(300), hit(1200), new PlayerDamagedEvent(40)));
        recap.record(turn(new JackpotEvent(Symbol.SEVEN), hit(5_000_000_000L), new PlayerDamagedEvent(15)));

        assertEquals(2, recap.getTurns());
        assertEquals(5_000_001_500L, recap.getDamageDealt());
        assertEquals(5_000_000_000L, recap.getBestHit(), "un coup peut dépasser la limite des int");
        assertEquals(55, recap.getDamageTaken());
        assertEquals(1, recap.getBingos());
        assertEquals(2, recap.getCardsPlayed());
    }

    @Test
    void aNewFightStartsFromZeroAndHugeTotalsDoNotOverflow() {
        CombatRecap recap = new CombatRecap();
        recap.record(turn(hit(Long.MAX_VALUE - 1), hit(10)));
        assertEquals(Long.MAX_VALUE, recap.getDamageDealt());

        recap.reset();
        assertEquals(0, recap.getTurns());
        assertEquals(0L, recap.getDamageDealt());
        assertEquals(0L, recap.getBestHit());
        assertEquals(0, recap.getCardsPlayed());
    }
}
