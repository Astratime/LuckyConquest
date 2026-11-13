package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.events.ComboEvent;
import fr.astratime.lucky.entities.events.DamageReflectedEvent;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.EnemyHealedEvent;
import fr.astratime.lucky.entities.events.EnemyHpLostEvent;
import fr.astratime.lucky.entities.events.EnemyPhaseEvent;
import fr.astratime.lucky.entities.events.EnemyShieldedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GainsEarnedEvent;
import fr.astratime.lucky.entities.events.GainsLostEvent;
import fr.astratime.lucky.entities.events.GainsStolenEvent;
import fr.astratime.lucky.entities.events.JackpotEvent;
import fr.astratime.lucky.entities.events.PlayerDamagedEvent;
import fr.astratime.lucky.entities.events.PlayerHealedEvent;
import fr.astratime.lucky.entities.events.ShieldGainedEvent;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Journal du combat : ce qui s'est passé pendant les derniers tours, pour
 * comprendre une victoire ou une défaite. Pour chaque tour, ce que le joueur a
 * fait pendant le sien (cartes jouées, combinaisons, Bingo, dégâts, gains...),
 * puis ce qui s'est passé pendant celui de l'ennemi (ses cartes, les PV perdus,
 * ses soins, les ripostes...). Jamais l'intention de l'ennemi : seulement ce qui
 * a déjà eu lieu. Garde les {@link #MAX_TURNS} derniers tours ; remis à zéro au
 * début de chaque combat.
 */
public class CombatJournal {

    /** Nombre de tours gardés (les plus anciens s'effacent). */
    public static final int MAX_TURNS = 5;

    /**
     * Un tour du journal.
     *
     * @param turn        numéro du tour dans le combat (à partir de 1)
     * @param playerLines ce qui s'est passé pendant le tour du joueur
     * @param enemyName   nom de l'ennemi
     * @param enemyLines  ce qui s'est passé pendant le tour de l'ennemi (vide s'il n'a pas joué)
     * @param enemyPlayed {@code true} si l'ennemi a joué ce tour (il n'a pas été vaincu avant)
     */
    public record Entry(int turn, List<String> playerLines, String enemyName, List<String> enemyLines,
                        boolean enemyPlayed) { }

    private final Deque<Entry> entries = new ArrayDeque<>();
    private final List<String> cardsPlayed = new ArrayList<>();
    private int turn;

    /** Nouveau combat : le journal repart de zéro. */
    public void reset() {
        entries.clear();
        cardsPlayed.clear();
        turn = 0;
    }

    /** Le joueur a joué {@code card} ce tour. */
    public void cardPlayed(Card card) {
        cardsPlayed.add(card.getName());
    }

    /**
     * Ajoute le tour qui vient de finir : le tirage du joueur et le tour de l'ennemi qui a suivi.
     *
     * @param result    le résultat complet du tour (tour de l'ennemi compris)
     * @param enemyName nom de l'ennemi affronté
     */
    public void record(TurnResult result, String enemyName) {
        turn++;
        EnemyTurnResult enemyTurn = result.getEnemyTurn();
        Set<Event> enemyEvents = identitySet();
        if (enemyTurn != null) enemyEvents.addAll(enemyTurn.events());
        Set<Event> playerEvents = identitySet();
        for (Event event : result.getCardEvents()) if (!enemyEvents.contains(event)) playerEvents.add(event);
        for (Event event : result.getEvents()) if (!enemyEvents.contains(event)) playerEvents.add(event);

        List<String> playerLines = new ArrayList<>();
        if (!cardsPlayed.isEmpty()) playerLines.add(Lang.f("Cartes : {0}", String.join(", ", cardsPlayed)));
        Tally mine = Tally.of(playerEvents);
        if (!mine.combos.isEmpty()) playerLines.add(Lang.f("Combinaisons : {0}", String.join(", ", mine.combos)));
        for (String bingo : mine.bingos) playerLines.add(Lang.f("Bingo {0} !", bingo));
        mine.describe(playerLines, false);
        if (result.getBonusGame() != null && result.getBonusGame().getTotal() > 0) {
            playerLines.add(Lang.f("Jeu bonus : +{0}", Lang.big(result.getBonusGame().getTotal())));
        }
        if (playerLines.isEmpty()) playerLines.add(Lang.t("Aucun effet"));

        List<String> enemyLines = new ArrayList<>();
        if (enemyTurn != null) {
            List<String> cards = enemyTurn.played().stream().map(Card::getName).toList();
            if (!cards.isEmpty()) enemyLines.add(Lang.f("Cartes : {0}", String.join(", ", cards)));
            Tally its = Tally.of(enemyEvents);
            its.describe(enemyLines, true);
            if (enemyLines.isEmpty()) enemyLines.add(Lang.t("Aucun effet"));
        }

        entries.addFirst(new Entry(turn, List.copyOf(playerLines), enemyName, List.copyOf(enemyLines), enemyTurn != null));
        while (entries.size() > MAX_TURNS) entries.removeLast();
        cardsPlayed.clear();
    }

    /** @return les derniers tours, du plus récent au plus ancien. */
    public List<Entry> getEntries() {
        return List.copyOf(entries);
    }

    /** @return {@code true} si aucun tour n'est encore fini. */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    private static Set<Event> identitySet() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }

    /** Les totaux d'une moitié de tour (celle du joueur ou celle de l'ennemi). */
    private static final class Tally {
        final Set<String> combos = new LinkedHashSet<>();
        final List<String> bingos = new ArrayList<>();
        long dealt, dealtBlocked, taken, takenBlocked, healed, shield, enemyHealed, enemyDefense, stolen;
        double gains;
        int phase;

        static Tally of(Iterable<Event> events) {
            Tally tally = new Tally();
            for (Event event : events) tally.add(event);
            return tally;
        }

        private void add(Event event) {
            if (event instanceof ComboEvent combo) combos.add(capitalized(combo.combo.getDisplayName()));
            else if (event instanceof JackpotEvent jackpot) bingos.add(capitalized(jackpot.symbol.getDisplayName()));
            else if (event instanceof EnemyDamagedEvent hit) {
                dealt = add(dealt, Math.max(0L, hit.damage));
                dealtBlocked = add(dealtBlocked, Math.max(0L, hit.blocked));
            } else if (event instanceof DamageReflectedEvent reflected) dealt = add(dealt, Math.max(0L, reflected.damage));
            else if (event instanceof EnemyHpLostEvent lost) dealt = add(dealt, Math.max(0L, lost.lost));
            else if (event instanceof PlayerDamagedEvent hurt) {
                taken = add(taken, Math.max(0, hurt.damage));
                takenBlocked = add(takenBlocked, Math.max(0, hurt.blocked));
            } else if (event instanceof PlayerHealedEvent heal) healed = add(healed, Math.max(0, heal.amount));
            else if (event instanceof ShieldGainedEvent gained) shield = add(shield, Math.max(0, gained.amount));
            else if (event instanceof EnemyHealedEvent heal) enemyHealed = add(enemyHealed, Math.max(0L, heal.amount));
            else if (event instanceof EnemyShieldedEvent shielded) enemyDefense = add(enemyDefense, Math.max(0, shielded.defense));
            else if (event instanceof GainsStolenEvent theft) stolen += theft.amount;
            else if (event instanceof GainsLostEvent loss) gains -= loss.amount;
            else if (event instanceof GainsEarnedEvent earned) gains += earned.amount;
            else if (event instanceof EnemyPhaseEvent change) phase = Math.max(phase, change.phase);
        }

        /** Ajoute les lignes de ces totaux ; {@code enemyTurn} : les dégâts infligés sont des ripostes. */
        void describe(List<String> lines, boolean enemyTurn) {
            if (phase >= 2) lines.add(Lang.f("Phase {0} !", phase));
            if (dealt > 0 || dealtBlocked > 0) {
                String text = enemyTurn ? Lang.f("Ripostes : {0}", Lang.big(dealt)) : Lang.f("Dégâts infligés : {0}", Lang.big(dealt));
                lines.add(dealtBlocked > 0 ? text + Lang.f(" ({0} bloqués)", Lang.big(dealtBlocked)) : text);
            }
            if (taken > 0 || takenBlocked > 0) {
                String text = Lang.f("PV perdus : {0}", Lang.big(taken));
                lines.add(takenBlocked > 0 ? text + Lang.f(" ({0} bloqués)", Lang.big(takenBlocked)) : text);
            }
            if (healed > 0) lines.add(Lang.f("PV rendus : {0}", Lang.big(healed)));
            if (shield > 0) lines.add(Lang.f("Bouclier : +{0}", Lang.big(shield)));
            if (enemyHealed > 0) lines.add(Lang.f("Soins de l'ennemi : {0}", Lang.big(enemyHealed)));
            if (enemyDefense > 0) lines.add(Lang.f("Défense de l'ennemi : +{0}", Lang.big(enemyDefense)));
            if (stolen > 0) lines.add(Lang.f("Gains volés : {0}", Lang.big(stolen)));
            if (Math.round(gains) > 0) lines.add(Lang.f("Gains : +{0}", Lang.big(gains)));
            else if (Math.round(gains) < 0) lines.add(Lang.f("Gains : -{0}", Lang.big(-gains)));
        }

        /** Somme bloquée au plus grand {@code long} (les dégâts peuvent devenir immenses). */
        private static long add(long total, long amount) {
            long sum = total + amount;
            return sum < total ? Long.MAX_VALUE : sum;
        }

        /** @return {@code text} en minuscules, sauf sa première lettre (ex : "Triple sept"). */
        private static String capitalized(String text) {
            if (text == null || text.isEmpty()) return "";
            return text.charAt(0) + text.substring(1).toLowerCase();
        }
    }
}
