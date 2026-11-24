package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.BigNumber;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.enemy.AspirationRule;
import fr.astratime.lucky.entities.enemy.EnemyCards;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySignature;
import fr.astratime.lucky.entities.enemy.EnemySlotMachine;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.events.EnemyHpLostEvent;
import fr.astratime.lucky.entities.events.EnemyRouletteEvent;
import fr.astratime.lucky.entities.events.GainsStolenEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.StatusEvent;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.IntConsumer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static fr.astratime.lucky.controllers.EnemyTurnResolver.*;

/**
 * Un tour de l'ennemi en cours : ce qu'il a pioché, joué et tiré, et les petites actions communes
 * à ses symboles (voir {@link EnemyTurnResolver}).
 */
abstract class EnemyTurnState {
    final EnemyTurnResolver resolver;
    final Random            random;
    final EnemySlotMachine  machine;
    final Enemy       enemy;
    final Player      player;
    final EnemyKind   kind;
    final List<Event> openingEvents = new ArrayList<>();
    /** Numéro raté : ses attaques du tour frappent d'autant plus fort. */
    int     actFactor;
    /** Sa Prédiction est sortie au tirage du joueur. */
    boolean foreseen;
    /** Présage manqué au tirage du joueur (Temple du Destin) : ses attaques de ce tour montent. */
    boolean omenMissed;

    List<Card> drawn;
    List<Card> played;

    /** Ses cartes propres : celles du tour (par nombre), celles qui agissent tout de suite, celles de l'Ombre. */
    final Map<EnemySignature, Integer> boosts  = new EnumMap<>(EnemySignature.class);
    final List<EnemySignature>         now     = new ArrayList<>();
    final List<EnemyCards.ShadowRole>  shadows = new ArrayList<>();
    boolean forbidReel;
    /** Descente aux enfers du joueur : ce tour, 2 cartes au plus et attaques -50 %. */
    boolean hellBound;
    int     swordBonus, healBonus, shieldBonus;
    final Map<EnemySymbol, Integer> luck = new EnumMap<>(EnemySymbol.class);

    EnemySymbol[] symbols;
    /** Ce qui arrive à un rouleau avant qu'il agisse. */
    final Map<Integer, List<Event>> reelEvents = new HashMap<>();
    Turn                 turn;
    EnemyRouletteEvent[] roulettes;
    boolean              jackpot;
    int                  originalJackpot;

    /** Les mauvais sorts pour le prochain tour du joueur. */
    LastingEffects curses;
    /** Queue de comète : la place du symbole qui agit deux fois, ou -1. */
    int best;
    /** Les Cœurs renforcent aussi ce que rendent ses Crocs et ses Morsures. */
    int healFactor;
    final List<List<Event>> outcomes = new ArrayList<>();

    EnemyTurnState(EnemyTurnResolver resolver, Enemy enemy, Player player) {
        this.resolver = resolver;
        this.random   = resolver.random;
        this.machine  = resolver.machine;
        this.enemy    = enemy;
        this.player   = player;
        this.kind     = enemy.getKind();
    }

    /** @return le symbole que le Croupier impose : le plus rare des rouleaux du joueur (jamais le Joker). */
    Symbol rigSymbol(List<Symbol> reels) {
        Map<Symbol, Integer> counts = new EnumMap<>(Symbol.class);
        for (Symbol symbol : reels) if (symbol != Symbol.JOKER) counts.merge(symbol, 1, Integer::sum);
        if (counts.isEmpty()) return null;
        int fewest = Collections.min(counts.values());
        List<Symbol> rarest = counts.entrySet().stream().filter(e -> e.getValue() == fewest).map(Map.Entry::getKey).toList();
        return rarest.get(random.nextInt(rarest.size()));
    }

    /** Clé : la carte la plus forte d'abord (carte spéciale, puis As, Roi, Dame...). */
    static int keyStrength(Card card) {
        if (!card.hasRank()) return 15;
        return card.getRank() == 1 ? 14 : card.getRank();
    }

    /** Ferme à clé les {@code count} plus fortes cartes de la main pour {@code turns} tours (Clé, Voyance). */
    List<String> lockBest(int count, int turns) {
        List<String> locked = new ArrayList<>();
        for (int k = 0; k < count; k++) {
            Card best = null;
            for (Card card : player.getCurrentHand()) if (best == null || keyStrength(card) > keyStrength(best)) best = card;
            if (best == null) break;
            if (player.steal(best, turns)) locked.add(best.getName().toUpperCase());
        }
        return locked;
    }

    /** @return le nom de la couleur {@code suit} au pluriel, en capitales (Relique : « TES PIQUES SONT FERMÉS »). */
    String suitPlural(Card.Suit suit) {
        return switch (suit) {
            case PIQUE   -> Lang.t("PIQUES");
            case COEUR   -> Lang.t("COEURS");
            case CARREAU -> Lang.t("CARREAUX");
            case TREFLE  -> Lang.t("TRÈFLES");
        };
    }

    /** @return les couleurs des cartes en main, de la plus tenue à la moins tenue (Relique). */
    List<Card.Suit> heldSuits() {
        Map<Card.Suit, Integer> counts = new EnumMap<>(Card.Suit.class);
        for (Card card : player.getCurrentHand()) if (card.getSuit() != null) counts.merge(card.getSuit(), 1, Integer::sum);
        List<Card.Suit> suits = new ArrayList<>(counts.keySet());
        suits.sort(Comparator.comparingInt((Card.Suit suit) -> -counts.get(suit)).thenComparingInt(Enum::ordinal));
        return suits;
    }

    /**
     * Va-tout : s'il en tire au moins un, ses attaques de tout le tour doublent (triplent avec l'Ordre de charge),
     * et sa défense tombera à 0 à la fin de son tour (voir {@link EnemyTurnResolution#finish}).
     */
    void goAllOut() {
        if (Arrays.stream(symbols).noneMatch(symbol -> symbol == EnemySymbol.ALL_OUT)) return;
        int factor = enemy.takeCharge(EnemySignature.ORDRE_DE_CHARGE) ? EnemySignature.ORDRE_FACTOR : EnemySymbol.ALL_OUT_FACTOR;
        turn.attackFactor *= factor;
        enemy.goAllOut();
        openingEvents.add(new StatusEvent(Lang.f("VA-TOUT : ATTAQUES x{0}", factor), EffectPopup.Style.ATTACK));
    }

    /** Rassemblement : ses soldats tirent, chacun d'une part d'Épée, au début de son tour. */
    void troopsFire() {
        int troops = enemy.getTroops();
        if (troops <= 0 || player.isDefeated()) return;
        openingEvents.add(new StatusEvent(Lang.n(troops, "SON SOLDAT TIRE", "SES {0} SOLDATS TIRENT", troops),
            EffectPopup.Style.ATTACK));
        for (int k = 0; k < troops; k++) {
            openingEvents.add(turn.strike(kind.swordDamage() * EnemySymbol.RALLY_SHOT_PERCENT / 100));
        }
    }

    /** Prédiction : il annonce un symbole (deux avec le Tarot) pour le prochain tirage du joueur. */
    void predict(List<Event> events) {
        List<Symbol> reels = player.getSlotMachine().getReels();
        Symbol foretold = reels.get(random.nextInt(reels.size()));
        Symbol second = null;
        if (enemy.takeCharge(EnemySignature.TAROT)) { // Tarot : un deuxième symbole, différent si possible
            List<Symbol> others = reels.stream().filter(reel -> reel != foretold).toList();
            second = others.isEmpty() ? foretold : others.get(random.nextInt(others.size()));
        }
        enemy.predict(foretold, second);
        events.add(new StatusEvent(second == null ? Lang.f("PRÉDICTION : {0}", foretold.getDisplayName())
            : Lang.f("PRÉDICTION : {0} OU {1}", foretold.getDisplayName(), second.getDisplayName()),
            EffectPopup.Style.SPECIAL));
    }

    /** Fouille : il confisque des cartes du deck du joueur, d'autant plus qu'il est riche ; sinon il mord. */
    void frisk(List<Event> events) {
        int count = Math.min(EnemySymbol.FRISK_MAX, (int) Math.min(EnemySymbol.FRISK_MAX, 1 + Math.max(0, player.getGains()) / EnemySymbol.FRISK_GAINS_STEP))
            + (enemy.takeCharge(EnemySignature.CAMERAS) ? 1 : 0); // Caméras : une carte de plus
        List<String> taken = new ArrayList<>();
        for (int k = 0; k < count; k++) {
            Card card = player.frisk(random);
            if (card != null) taken.add(card.getName().toUpperCase());
        }
        events.add(taken.isEmpty() ? turn.strike(EnemySymbol.FANG_DAMAGE)
            : new StatusEvent(Lang.n(taken.size(), "FOUILLE : {0} CONFISQUÉE", "FOUILLE : {0} CONFISQUÉES",
                String.join(", ", taken)), EffectPopup.Style.DAMAGE));
    }

    /** Faillite : plus riche que lui, le joueur perd tous ses gains ; sinon, lui perd une part de ses PV. */
    void bankruptcy(List<Event> events) {
        // Saisie : le seuil baisse de 10 points par carte, jamais sous 10 %
        int percent = Math.max(EnemySignature.SAISIE_FLOOR, EnemySymbol.BANKRUPTCY_FORTUNE_PERCENT
            - EnemySignature.SAISIE_POINTS * boosts.getOrDefault(EnemySignature.SAISIE, 0));
        long fortune = Math.round(enemy.getHp() * (double) percent / 100);
        if (player.getGains() > fortune) {
            double lost = player.getGains();
            player.addGains(-lost);
            events.add(new StatusEvent(Lang.t("FAILLITE : IL TE PREND TOUT"), EffectPopup.Style.DAMAGE));
            events.add(new GainsStolenEvent(lost, 0));
        } else {
            long lost = enemy.takeDamage(Math.round(enemy.getHp() * (double) EnemySymbol.BANKRUPTCY_HP_PERCENT / 100));
            events.add(new EnemyHpLostEvent(Lang.f("FAILLITE : IL PERD {0} PV", Lang.big(lost)), EffectPopup.Style.SPECIAL, lost));
        }
    }

    /** Grand tour : sa roue tombe sur deux Épées, deux Boucliers, une Potion ou une case vide. */
    void bigWheel(List<Event> events) {
        int slot = random.nextInt(boosts.containsKey(EnemySignature.ROUE_LESTEE) ? 3 : 4);
        switch (slot) {
            case 0 -> {
                events.add(new StatusEvent(Lang.t("GRAND TOUR : DEUX ÉPÉES !"), EffectPopup.Style.ATTACK));
                events.add(turn.strike(kind.swordDamage()));
                events.add(turn.strike(kind.swordDamage()));
            }
            case 1 -> {
                events.add(new StatusEvent(Lang.t("GRAND TOUR : DEUX BOUCLIERS"), EffectPopup.Style.DEFENSE));
                events.add(turn.shield(kind.shieldDefense() + shieldBonus));
                events.add(turn.shield(kind.shieldDefense() + shieldBonus));
            }
            case 2 -> {
                events.add(new StatusEvent(Lang.t("GRAND TOUR : UNE POTION"), EffectPopup.Style.DEFENSE));
                events.add(heal(enemy, kind.potionPercent(healBonus, enemy.getDifficulty())));
            }
            default -> events.add(new StatusEvent(Lang.t("GRAND TOUR : CASE VIDE"), EffectPopup.Style.SPECIAL));
        }
    }

    /** Fausse monnaie : une part des gains du joueur devient fausse ; sans gains, il frappe. */
    void fakeMoney(List<Event> events) {
        double real = Math.max(0, player.getGains() - curses.getFakeGains());
        double faked = BigNumber.percent(real, Math.min(100, kind.fakePercent(enemy.getDifficulty())
            + EnemySignature.PLANCHE_PERCENT * boosts.getOrDefault(EnemySignature.PLANCHE_A_BILLETS, 0)));
        if (faked > 0) {
            curses.addFakeGains(faked);
            events.add(new StatusEvent(Lang.f("FAUSSE MONNAIE : {0} GAINS FAUX", Lang.big(faked)), EffectPopup.Style.DAMAGE));
        } else {
            events.add(turn.strike(EnemySymbol.FANG_DAMAGE)); // rien à contrefaire : il frappe
        }
    }

    /** Aspiration : une règle de plus (deux avec la Grande aspiration), trois au plus. */
    void aspire(List<Event> events) {
        // La première règle demande 2 cartes, chaque suivante 1 carte, et 3 règles au plus.
        int rules = enemy.takeCharge(EnemySignature.GRANDE_ASPIRATION) ? 2 : 1;
        for (int added = 0; added < rules && curses.getAspirations().size() < AspirationRule.MAX_RULES; added++) {
            AspirationRule rule = AspirationRule.random(random, curses.getAspirations().isEmpty());
            curses.addAspiration(rule);
            events.add(new StatusEvent(Lang.f("ASPIRATION : {0}", rule.text().toUpperCase()),
                EffectPopup.Style.SPECIAL));
        }
    }

    /** Numéro : il annonce une famille ; si le joueur n'en joue aucune, son prochain coup frappe plus fort. */
    void announceAct(List<Event> events) {
        Card.Suit[] suits = Card.Suit.values();
        Card.Suit suit = suits[random.nextInt(suits.length)];
        int factor = enemy.takeCharge(EnemySignature.ROULEMENT_DE_TAMBOUR) ? 3 : EnemySymbol.ACT_FACTOR;
        enemy.announceAct(suit, factor);
        events.add(new StatusEvent(Lang.f("NUMÉRO : JOUE DU {0}, OU x{1} !", suitName(suit), factor),
            EffectPopup.Style.SPECIAL));
    }

    /**
     * Un mauvais sort pour le prochain tour du joueur : {@code add} le pose une fois,
     * ou deux si sa carte {@code doubler} était chargée ; {@code once} ou {@code twice} l'annonce.
     */
    void curse(List<Event> events, EnemySignature doubler, IntConsumer add, String once, String twice) {
        boolean doubled = enemy.takeCharge(doubler);
        add.accept(doubled ? 2 : 1);
        events.add(new StatusEvent(doubled ? twice : once, EffectPopup.Style.DAMAGE));
    }

    /** @return {@code add} appelé {@code n} fois (pour un mauvais sort qui ne se pose qu'un par un) */
    IntConsumer times(Runnable add) {
        return n -> {
            for (int k = 0; k < n; k++) add.run();
        };
    }

    /**
     * Il prend {@code amount} gains au joueur.
     *
     * @return {@code false} s'il n'y avait rien à prendre
     */
    boolean steal(double amount, List<Event> events) {
        if (amount <= 0) return false;
        player.addGains(-amount);
        events.add(new GainsStolenEvent(amount, 0));
        return true;
    }

    /** @return les noms des {@code count} meilleures cartes du joueur (main, sinon jouées) qu'il garde {@code turns} tours */
    List<String> confiscate(int count, int turns) {
        List<String> taken = new ArrayList<>();
        for (int k = 0; k < count; k++) {
            Card card = bestCard(player.getCurrentHand());
            if (card == null) card = bestCard(player.getPlayedCards());
            if (card != null && player.steal(card, turns)) taken.add(card.getName().toUpperCase());
        }
        return taken;
    }
}
