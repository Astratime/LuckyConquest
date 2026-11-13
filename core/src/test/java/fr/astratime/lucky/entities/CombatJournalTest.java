package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.events.ComboEvent;
import fr.astratime.lucky.entities.events.DamageReflectedEvent;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.EnemyHealedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;
import fr.astratime.lucky.entities.events.GainsStolenEvent;
import fr.astratime.lucky.entities.events.JackpotEvent;
import fr.astratime.lucky.entities.events.PlayerDamagedEvent;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Le journal du combat : ce qui s'est passé pendant les derniers tours, côté joueur puis côté ennemi. */
class CombatJournalTest {

    private static final Symbol[] SYMBOLS = { Symbol.SEVEN, Symbol.BAR, Symbol.CHERRY };

    private static Card card(String name) {
        return new Card(name, name, "x.png", List.of(), null, 1);
    }

    private static TurnResult playerTurn(Event... events) {
        return new TurnResult(List.of(events), SYMBOLS, 0, List.of(), List.of(), List.of());
    }

    /** Le tour du joueur ({@code mine}), suivi de celui de l'ennemi qui joue {@code played} et produit {@code its}. */
    private static TurnResult fullTurn(List<Event> mine, List<Card> played, Event... its) {
        EnemyTurnResult enemy = new EnemyTurnResult(played, played, Map.of(), new EnemySymbol[0],
            List.of(List.of(its)), List.of());
        return new TurnResult(mine, SYMBOLS, 0, List.of(), List.of(), List.of()).withEnemyTurn(enemy, List.of());
    }

    @Test
    void aTurnTellsWhatThePlayerDidThenWhatTheEnemyDid() {
        CombatJournal journal = new CombatJournal();
        journal.cardPlayed(card("As de Pique"));
        journal.cardPlayed(card("Roi de Pique"));
        journal.record(fullTurn(
            List.of(new ComboEvent(Combo.PAIRE), new JackpotEvent(Symbol.SEVEN),
                new EnemyDamagedEvent(120_000, 125_000, 5_000, 0, false), new GainsEarnedEvent(3_000)),
            List.of(card("Soin du Kraken")),
            new PlayerDamagedEvent(8_000, 2_000, 0), new EnemyHealedEvent(30_000),
            new DamageReflectedEvent(4_000), new GainsStolenEvent(500, 0)), "Kraken");

        CombatJournal.Entry entry = journal.getEntries().get(0);
        assertEquals(1, entry.turn());
        assertEquals("Kraken", entry.enemyName());
        assertTrue(entry.enemyPlayed());
        assertEquals(List.of("Cartes : As de Pique, Roi de Pique", "Combinaisons : Paire", "Bingo Sept !",
            "Dégâts infligés : 120 000 (5 000 bloqués)", "Gains : +3 000"), entry.playerLines());
        assertEquals(List.of("Cartes : Soin du Kraken", "Ripostes : 4 000", "PV perdus : 8 000 (2 000 bloqués)",
            "Soins de l'ennemi : 30 000", "Gains volés : 500"), entry.enemyLines(),
            "les coups de l'ennemi ne sont jamais comptés dans le tour du joueur");
    }

    @Test
    void theJournalKeepsTheLastTurnsNewestFirst() {
        CombatJournal journal = new CombatJournal();
        for (int i = 1; i <= CombatJournal.MAX_TURNS + 2; i++) {
            journal.record(playerTurn(new EnemyDamagedEvent(i, i, 0, 0, false)), "Soldat");
        }
        List<CombatJournal.Entry> entries = journal.getEntries();
        assertEquals(CombatJournal.MAX_TURNS, entries.size());
        assertEquals(CombatJournal.MAX_TURNS + 2, entries.get(0).turn(), "le plus récent en haut");
        assertEquals(3, entries.get(entries.size() - 1).turn());
        assertFalse(entries.get(0).enemyPlayed(), "vaincu avant son tour : l'ennemi n'a pas joué");
    }

    @Test
    void playedCardsBelongToTheirTurnAndANewFightStartsEmpty() {
        CombatJournal journal = new CombatJournal();
        journal.cardPlayed(card("Bingo"));
        journal.record(playerTurn(), "Soldat");
        journal.record(playerTurn(), "Soldat");
        assertEquals(List.of("Aucun effet"), journal.getEntries().get(0).playerLines());
        assertEquals(List.of("Cartes : Bingo"), journal.getEntries().get(1).playerLines());

        journal.reset();
        assertTrue(journal.isEmpty());
        journal.record(playerTurn(), "Soldat");
        assertEquals(1, journal.getEntries().get(0).turn());
    }
}
