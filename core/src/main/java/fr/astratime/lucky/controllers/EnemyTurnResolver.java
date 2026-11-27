package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.GameRandom;
import fr.astratime.lucky.entities.BigNumber;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.SlotMachine;
import fr.astratime.lucky.entities.enemy.EnemyCards;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySignature;
import fr.astratime.lucky.entities.enemy.EnemySlotMachine;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.events.EnemyHpLostEvent;
import fr.astratime.lucky.entities.events.EnemyDrainEvent;
import fr.astratime.lucky.entities.events.EnemyHourglassEvent;
import fr.astratime.lucky.entities.events.EnemyMirrorEvent;
import fr.astratime.lucky.entities.events.EnemyRouletteEvent;
import fr.astratime.lucky.entities.events.GainsStolenEvent;
import fr.astratime.lucky.entities.events.EnemyRageEvent;
import fr.astratime.lucky.entities.events.EnemyHealedEvent;
import fr.astratime.lucky.entities.events.EnemyShieldedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.PlayerDamagedEvent;
import fr.astratime.lucky.entities.events.StatusEvent;
import fr.astratime.lucky.entities.exploration.PlaceRule;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.entities.events.ThornsEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Tour de l'ennemi : ses Épines du tour précédent piquent le joueur, puis il
 * pioche {@link Enemy#HAND_SIZE} cartes, en joue autant que son type le permet
 * (voir {@link #choose}), lance sa machine à sous (symboles de son type, voir
 * {@link EnemyKind}), puis chaque symbole agit, renforcé par les cartes jouées :
 * <ul>
 *   <li>Épée : attaque le joueur ({@link EnemyKind#swordDamage()}, + les Piques, + sa Rage) ;
 *       son bouclier absorbe les coups et s'use à chacun, et le renvoi de dégâts (Carreau)
 *       touche l'ennemi une fois toutes ses attaques portées ;</li>
 *   <li>Bouclier : défense ({@link EnemyKind#shieldDefense()}, + les Carreaux)
 *       qui absorbe les attaques du prochain tour du joueur, et s'use à chaque coup ;</li>
 *   <li>Potion : soin ({@link EnemyKind#potionPercent(int)} % des PV max, + les Cœurs) ;</li>
 *   <li>Épines : {@link EnemySymbol#THORNS_PERCENT} % des dégâts que le joueur lui
 *       inflige à son prochain tour lui seront renvoyés, au début du tour suivant de l'ennemi ;</li>
 *   <li>Croc : attaque comme une Épée ({@link EnemySymbol#FANG_DAMAGE}) et chaque PV
 *       volé lui rend {@link EnemySymbol#FANG_DRAIN} % de ses PV max ;</li>
 *   <li>Rage : +{@link EnemySymbol#RAGE_ATTACK} à toutes ses attaques, jusqu'à la fin du combat ;</li>
 *   <li>Dé pipé : les jauges du joueur perdent {@link EnemySymbol#DIE_PERCENT} % ;</li>
 *   <li>Intérêts : il prend {@link EnemySymbol#INTEREST_PERCENT} % des gains du joueur, qui
 *       renforcent son prochain coup (sans gains à prendre, il mord) ;</li>
 *   <li>Zéro : une roulette double ses attaques (rouge), ses Boucliers (noir) ou les deux (zéro),
 *       pour tout le tour ;</li>
 *   <li>Reflet : il rejoue la dernière carte du joueur, à moitié de sa force ;</li>
 *   <li>Sablier : son compte à rebours avance, et il explose au bout ;</li>
 *   <li>Tapis : à son tour suivant, sa mise double (ses attaques avec), sauf s'il est touché
 *       d'ici là ; un coup reçu la fait retomber ;</li>
 *   <li>Exploration : Grignotage, Ivresse, Aveuglement, Pépite et Chant jettent un mauvais
 *       sort au prochain tour du joueur ; l'Abordage vole sa meilleure carte ; le Forage
 *       traverse son bouclier ; l'Enclume renforce ses attaques sans limite ; la Morsure
 *       dévore ses gains et le soigne d'autant. Trois symboles identiques font un Jackpot
 *       (Bandit manchot, Jackpot Vivant) ; le Kraken joue une carte par bras.</li>
 * </ul>
 * Sa force ({@link EnemyKind#getPower()}, qui grandit à chaque chapitre) multiplie ses attaques,
 * ses Boucliers et tous ses effets. Le Rouleau interdit (Éclat Originel) bloque un rouleau du
 * joueur à son prochain tirage.
 * Ses Potions et ses Crocs le soignent selon {@link EnemyKind#getHealScale()} ; l'Éclat
 * Originel change de symboles à mi-vie (sa deuxième phase).
 * Toutes les cartes piochées partent ensuite dans sa défausse.
 */
public class EnemyTurnResolver {

    /** Sous cette part de vie, l'ennemi joue ses Cœurs en priorité. */
    static final float LOW_HP_RATIO = 0.5f;

    final Random           random;
    final EnemySlotMachine machine;

    public EnemyTurnResolver() {
        this(GameRandom.live());
    }

    /** @param random source d'aléatoire (ex : graine fixe pour des tests reproductibles) */
    public EnemyTurnResolver(Random random) {
        this.random  = random;
        this.machine = new EnemySlotMachine(random);
    }

    /**
     * Ce que le joueur renvoie à l'ennemi s'il attaque pendant son tour.
     *
     * @param percent       part de chaque attaque renvoyée à l'ennemi (cartes Carreau du joueur), en %
     * @param vaultShare    part du Coffre du joueur ajoutée à l'attaque renvoyée
     * @param shieldReflect dégâts renvoyés s'il attaque ce tour (le bouclier d'un Bingo de bouclier), 0 sinon
     * @param attackPercent part (%) de son attaque du tour qui lui est renvoyée (cartes Carreau)
     * @param hpPercent     part (%) de ses PV max qui lui est renvoyée s'il attaque (Descente aux enfers)
     * @param riposteBonus  ajouté à ses coups pour la riposte des cartes Carreau : le bonus d'attaque du rang
     * @param riposteFactor multiplicateur du renvoi des cartes Carreau : ceux de l'attaque du joueur ce tour (combos, Bingo)
     */
    public record Reflection(int percent, float vaultShare, int shieldReflect, float attackPercent, float hpPercent,
                             long riposteBonus, double riposteFactor) {
        /** Aucun renvoi. */
        public static final Reflection NONE = new Reflection(0, 0f, 0, 0f, 0f, 0L, 1.0);

        /** Renvoi sans riposte des cartes Carreau (bonus 0, multiplicateur 1). */
        public Reflection(int percent, float vaultShare, int shieldReflect, float attackPercent, float hpPercent) {
            this(percent, vaultShare, shieldReflect, attackPercent, hpPercent, 0L, 1.0);
        }
    }

    /** Joue le tour complet de l'ennemi, sans renvoi de dégâts ; ses Épines piquent d'abord. */
    public EnemyTurnResult resolve(Enemy enemy, Player player) {
        return resolve(enemy, player, Reflection.NONE);
    }

    /** Joue le tour complet de l'ennemi, avec le renvoi {@code reflection} ; ses Épines piquent d'abord. */
    public EnemyTurnResult resolve(Enemy enemy, Player player, Reflection reflection) {
        return resolve(enemy, player, reflection, prickThorns(enemy, player));
    }

    /**
     * Ses Épines piquent le joueur pour les coups de son tour : avant le tour
     * de l'ennemi, pour que le combat s'arrête là si le joueur en meurt.
     *
     * @return l'événement des Épines (vide si elles n'ont pas piqué)
     */
    public List<Event> prickThorns(Enemy enemy, Player player) {
        int thorns = enemy.collectThorns();
        if (thorns <= 0) return new ArrayList<>();
        int shieldBefore = player.getShield();
        int lost = player.takeDamage(thorns);
        List<Event> events = new ArrayList<>();
        events.add(new ThornsEvent(lost, shieldBefore - player.getShield(), player.getShield()));
        return events;
    }

    /**
     * Joue le tour complet de l'ennemi, ses Épines ayant déjà piqué (voir
     * {@link #prickThorns}) : {@code thornsEvents} ouvre son tour.
     */
    public EnemyTurnResult resolve(Enemy enemy, Player player, Reflection reflection, List<Event> thornsEvents) {
        EnemyTurnResolution turnPlay = new EnemyTurnResolution(this, enemy, player);
        turnPlay.open(thornsEvents);
        turnPlay.drawCards();
        turnPlay.readPlayedCards();
        turnPlay.spinReels();
        turnPlay.actSymbols();
        List<Event> afterEvents = turnPlay.finish(reflection);
        enemy.discard(turnPlay.drawn);
        boolean lit = enemy.getMaluses().takeLighthouse(); // Feu du phare : sa prochaine main reste visible
        if (!enemy.isDefeated() && (lit || enemy.getKind() == EnemyKind.GRAND_CROUPIER && enemy.getPhase() >= 3)) {
            enemy.revealNextHand(); // cartes sur table : le joueur voit sa prochaine main
        }
        enemy.resetDamageTaken(); // ses Épines ne comptent que les coups du prochain tour du joueur
        return new EnemyTurnResult(turnPlay.drawn, turnPlay.played, turnPlay.luck, turnPlay.symbols, turnPlay.outcomes,
            afterEvents, turnPlay.openingEvents);
    }


    /**
     * Parades des cartes des coffres des lieux : le Piège à rats annule un mauvais
     * sort sur la main (Grignotage, Aveuglement, Chant, Abordage, Fouille) ; la
     * Cage à requin protège les gains (Intérêts, Morsure, Fausse monnaie, Faillite).
     *
     * @return le texte de la parade, ou {@code null} si {@code symbol} agit normalement
     */
    static Event parry(EnemySymbol symbol, Player player) {
        LastingEffects lasting = player.getLastingEffects();
        String spell = switch (symbol) {
            case NIBBLE   -> Lang.t("GRIGNOTAGE");
            case BLIND    -> Lang.t("AVEUGLEMENT");
            case SONG     -> Lang.t("CHANT");
            case BOARDING -> Lang.t("ABORDAGE");
            case FRISK    -> Lang.t("FOUILLE");
            case CLOAKROOM -> Lang.t("VESTIAIRE");
            default       -> null;
        };
        if (spell != null && lasting.useTrap()) {
            return new StatusEvent(Lang.f(symbol == EnemySymbol.FRISK ? "PIÈGE À RATS : {0} ANNULÉE"
                : "PIÈGE À RATS : {0} ANNULÉ", spell),
                EffectPopup.Style.DEFENSE);
        }
        boolean takesGains = switch (symbol) {
            case INTEREST, BANK_BITE, FAKE_MONEY, BANKRUPTCY, RAKE, SWALLOWED_COIN -> true;
            default -> false;
        };
        if (takesGains && lasting.getCageTurns() > 0 && player.getGains() > 0) {
            return new StatusEvent(Lang.t("CAGE À REQUIN : GAINS PROTÉGÉS"), EffectPopup.Style.DEFENSE);
        }
        return null;
    }

    /**
     * Duel : le joueur et l'ennemi tirent chacun une carte au hasard de leur
     * deck (l'As vaut 14, une carte sans couleur 0 ; {@code penalty} rangs de
     * moins pour le joueur après une Botte secrète). La plus haute frappe :
     * l'ennemi attaque comme une Épée, plus le rang de sa carte ; le joueur lui
     * retire {@link EnemySymbol#DUEL_PER_MILLE_PER_RANK} pour mille de ses PV max
     * par rang. À égalité, rien.
     */
    void duel(Turn turn, List<Event> events, int penalty) {
        Card mine   = randomCard(turn.player.getAllCards());
        // il tire parmi ses cartes de couleur : ses cartes propres ne se battent pas en duel
        List<Card> pool = new ArrayList<>(turn.enemy.getDeckCards());
        pool.addAll(turn.enemy.getDiscardCards());
        List<Card> suited = pool.stream().filter(card -> card.getSuit() != null).toList();
        Card theirs = randomCard(suited.isEmpty() ? pool : suited);
        int myRank = Math.max(0, duelRank(mine) - penalty), theirRank = duelRank(theirs); // Botte secrète
        events.add(new StatusEvent(Lang.f("DUEL : {0} CONTRE {1}", duelName(mine), duelName(theirs)),
            EffectPopup.Style.SPECIAL));
        if (theirRank > myRank) {
            events.add(turn.strike(EnemySymbol.SWORD_DAMAGE + theirRank));
        } else if (myRank > theirRank) {
            Enemy enemy = turn.enemy;
            long lost = enemy.takeDamage(Math.round(enemy.getMaxHp() * (double) myRank * EnemySymbol.DUEL_PER_MILLE_PER_RANK / 1000));
            events.add(new EnemyHpLostEvent(Lang.f("DUEL GAGNÉ : -{0} PV", Lang.big(lost)), EffectPopup.Style.ATTACK, lost));
        }
    }

    Card randomCard(List<Card> cards) {
        return cards.isEmpty() ? null : cards.get(random.nextInt(cards.size()));
    }

    /** @return la valeur de {@code card} au Duel : son rang, l'As valant 14 ; 0 sans couleur. */
    static int duelRank(Card card) {
        if (card == null || card.getSuit() == null) return 0;
        return card.getRank() == 1 ? 14 : card.getRank();
    }

    static String duelName(Card card) {
        return card == null ? Lang.t("RIEN") : card.getName().toUpperCase();
    }

    /**
     * Croc : il mord, et chaque PV volé lui rend une part de ses PV max, à
     * {@code healPercent} % (renforcée par ses Cœurs et ses Saignées).
     */
    static void bite(Turn turn, List<Event> events, int healPercent) {
        PlayerDamagedEvent hit = turn.strike(EnemySymbol.FANG_DAMAGE);
        events.add(hit);
        Enemy enemy = turn.enemy;
        long drained = Math.round(enemy.getMaxHp() * (double) hit.damage * enemy.getKind().drainPercent(enemy.getDifficulty()) / 100 * healPercent / 100);
        if (drained > 0) events.add(new EnemyDrainEvent(enemy.heal(drained)));
    }

    static EnemyRageEvent rage(Enemy enemy) {
        int added = enemy.addRage(EnemySymbol.RAGE_ATTACK);
        return new EnemyRageEvent(added, enemy.getRage());
    }

    /** Il se soigne de {@code percent} % de ses PV max. */
    static EnemyHealedEvent heal(Enemy enemy, float percent) {
        return new EnemyHealedEvent(enemy.heal(Math.round(enemy.getMaxHp() * (double) percent / 100)));
    }

    /**
     * Reflet : il rejoue la dernière carte (d'une couleur) jouée par le joueur
     * ce tour, à moitié de sa force (à pleine force avec {@code full}, le Miroir déformant), comme une de ses cartes sombres : Pique, il
     * frappe ; Cœur, il se soigne ; Carreau et Trèfle, il se protège.
     * Sans carte à copier, il frappe simplement.
     */
    static void mirror(Turn turn, List<Event> events, boolean full) {
        Card copied = null;
        List<Card> playedByPlayer = turn.player.getPlayedCards();
        for (int i = playedByPlayer.size() - 1; i >= 0 && copied == null; i--) {
            if (playedByPlayer.get(i).getSuit() != null) copied = playedByPlayer.get(i);
        }
        events.add(new EnemyMirrorEvent(copied != null ? copied.getName() : null));
        if (copied == null) {
            events.add(turn.strike(turn.enemy.getKind().swordDamage()));
            return;
        }
        Enemy enemy = turn.enemy;
        int divisor = full ? 1 : 2; // Miroir déformant : à pleine force
        switch (copied.getSuit()) {
            case PIQUE   -> events.add(turn.strike(turn.enemy.getKind().swordDamage() + turn.enemy.getKind().swordBonus(copied) / divisor));
            case COEUR   -> events.add(heal(enemy, enemy.getKind().potionPercent(EnemyCards.healBonus(copied), enemy.getDifficulty()) / divisor));
            case CARREAU, TREFLE -> events.add(turn.shield((turn.enemy.getKind().shieldDefense() + turn.enemy.getKind().shieldBonus(copied)) / divisor));
        }
    }

    /**
     * Écho : il rejoue contre le joueur toutes les cartes (d'une couleur) de son dernier tour, à moitié de leur force
     * (à pleine force avec {@code full}, la Résonance), comme ses cartes sombres : Pique, il frappe ; Cœur, il se
     * soigne ; Carreau et Trèfle, il se protège. Sans carte à rejouer, il frappe simplement.
     */
    static void echo(Turn turn, List<Event> events, boolean full) {
        List<Card> echoed = turn.player.getPlayedCards().stream().filter(card -> card.getSuit() != null).toList();
        if (echoed.isEmpty()) {
            events.add(new StatusEvent(Lang.t("ÉCHO : RIEN À RENDRE"), EffectPopup.Style.SPECIAL));
            events.add(turn.strike(turn.enemy.getKind().swordDamage()));
            return;
        }
        events.add(new StatusEvent(Lang.n(echoed.size(), "ÉCHO : TA CARTE TE REVIENT", "ÉCHO : TES {0} CARTES TE REVIENNENT",
            echoed.size()), EffectPopup.Style.ATTACK));
        Enemy enemy = turn.enemy;
        EnemyKind kind = enemy.getKind();
        int divisor = full ? 1 : 2;
        int swords = 0, shields = 0;
        float heals = 0f;
        for (Card card : echoed) {
            switch (card.getSuit()) {
                case PIQUE -> swords += kind.swordBonus(card) / divisor;
                case COEUR -> heals += kind.potionPercent(EnemyCards.healBonus(card), enemy.getDifficulty()) / divisor;
                case CARREAU, TREFLE -> shields += kind.shieldBonus(card) / divisor;
            }
        }
        if (swords > 0) events.add(turn.strike(kind.swordDamage() + swords));
        if (heals > 0f) events.add(heal(enemy, kind.cappedHeal(heals, enemy.getDifficulty())));
        if (shields > 0) events.add(turn.shield(kind.shieldDefense() + shields));
    }

    /**
     * Abordage : il vole la meilleure carte du joueur (la plus forte de sa main,
     * sinon parmi celles qu'il a jouées ; jamais le Scorbut), pendant
     * {@link EnemySymbol#BOARDING_TURNS} tours (avec le Grappin, la 2e meilleure aussi), et la joue contre lui à pleine force : Pique, il frappe ; Cœur, il
     * se soigne ; Carreau et Trèfle, il se protège. Une carte sans couleur lui
     * sert d'arme. Sans rien à voler, il frappe simplement.
     */
    static void board(Turn turn, List<Event> events, boolean grapple) {
        Player player = turn.player;
        Card stolen = bestCard(player.getCurrentHand());
        if (stolen == null) stolen = bestCard(player.getPlayedCards());
        if (stolen == null || !player.steal(stolen, EnemySymbol.BOARDING_TURNS)) {
            events.add(turn.strike(turn.enemy.getKind().swordDamage()));
            return;
        }
        if (grapple) { // Grappin : il vole aussi la 2e meilleure carte (sans la jouer)
            Card second = bestCard(player.getCurrentHand());
            if (second == null) second = bestCard(player.getPlayedCards());
            if (second != null && player.steal(second, EnemySymbol.BOARDING_TURNS)) {
                events.add(new StatusEvent(Lang.f("GRAPPIN : {0} VOLÉE", second.getName().toUpperCase()), EffectPopup.Style.DAMAGE));
            }
        }
        events.add(new StatusEvent(Lang.f("ABORDAGE : {0} VOLÉE", stolen.getName().toUpperCase()), EffectPopup.Style.DAMAGE));
        Enemy enemy = turn.enemy;
        if (stolen.getSuit() == null) {
            events.add(turn.strike(turn.enemy.getKind().swordDamage()));
            return;
        }
        switch (stolen.getSuit()) {
            case PIQUE   -> events.add(turn.strike(turn.enemy.getKind().swordDamage() + turn.enemy.getKind().swordBonus(stolen)));
            case COEUR   -> events.add(heal(enemy, enemy.getKind().potionPercent(EnemyCards.healBonus(stolen), enemy.getDifficulty())));
            case CARREAU, TREFLE -> events.add(turn.shield(turn.enemy.getKind().shieldDefense() + turn.enemy.getKind().shieldBonus(stolen)));
        }
    }

    /** @return le nom affiché de la famille {@code suit}, en capitales. */
    static String suitName(Card.Suit suit) {
        return switch (suit) {
            case PIQUE   -> Lang.t("PIQUE");
            case COEUR   -> Lang.t("COEUR");
            case CARREAU -> Lang.t("CARREAU");
            case TREFLE  -> Lang.t("TRÈFLE");
        };
    }

    /** @return la carte la plus forte de {@code cards} (une figure ou un As avant tout, puis le rang), sans le Scorbut, ou {@code null}. */
    static Card bestCard(List<Card> cards) {
        return cards.stream()
            .filter(card -> !PlaceRule.SCURVY_CARD.equals(card.getId()))
            .max(Comparator.comparingInt((Card card) -> card.getSuit() == null ? 0 : 1)
                .thenComparingInt(card -> card.getRank() == 1 ? 14 : card.getRank()))
            .orElse(null);
    }

    /** @return la plus faible carte de {@code cards} (une carte classique basse d'abord), ou {@code null} (Fils). */
    static Card worstCard(List<Card> cards) {
        return cards.stream()
            .filter(card -> !PlaceRule.SCURVY_CARD.equals(card.getId()))
            .min(Comparator.comparingInt((Card card) -> card.getSuit() == null ? 1 : 0)
                .thenComparingInt(card -> card.getRank() == 1 ? 14 : card.getRank()))
            .orElse(null);
    }

    /** @return la case où s'arrête la bille de la roulette du Zéro. */
    EnemyRouletteEvent.Pocket spinRoulette() {
        int pocket = random.nextInt(37);
        if (pocket < EnemySymbol.ZERO_POCKETS) return EnemyRouletteEvent.Pocket.ZERO;
        return pocket % 2 == 0 ? EnemyRouletteEvent.Pocket.ROUGE : EnemyRouletteEvent.Pocket.NOIR;
    }

    /** Ce qui vaut pour toutes ses attaques et ses Boucliers d'un tour. */
    /** Cartes marquées (Bluffeur) : part de l'effet de ses cartes à couleur perdue, en % (moitié moins pour un boss). */
    public static final int MARKED_CARDS_PERCENT = 50;

    static final class Turn {
        final Enemy  enemy;
        final Player player;
        final int    swordBonus;
        int attackFactor = 1;
        int shieldFactor = 1;
        /** Intimidation (Bluffeur) : part de ses attaques qui porte, en %. */
        int malusPercent = 100;
        /** Force en plus de ses coups et de ses Boucliers (Engrenages), en %. */
        int boostPercent;
        int totalAttack;

        Turn(Enemy enemy, Player player, int swordBonus) {
            this.enemy      = enemy;
            this.player     = player;
            this.swordBonus = swordBonus;
        }

        /**
         * Il frappe de {@code base}, plus ses Piques, sa Rage et ses Intérêts,
         * multipliés par sa mise (Tapis), la roulette du Zéro et sa force ; le bouclier du
         * joueur absorbe le coup et s'use d'autant.
         */
        PlayerDamagedEvent strike(int base) { return strike(base, false); }

        /** Comme {@link #strike(int)} ; avec {@code pierce} (Forage), le coup traverse le bouclier du joueur. */
        PlayerDamagedEvent strike(int base, boolean pierce) { return strike(base, pierce ? 0 : 100); }

        /** Comme {@link #strike(int)}, quand seuls {@code shieldPercent} % du bouclier du joueur l'arrêtent (le Videur). */
        PlayerDamagedEvent strike(int base, int shieldPercent) {
            int attack = (int) Math.min(Integer.MAX_VALUE, (long) enemy.getKind().hurt(
                (base + swordBonus + enemy.getRage() + enemy.getAnvil() + enemy.spendInterest())
                    * enemy.getStake() * attackFactor, enemy.getDifficulty()) * enemy.attackPercent() / 100 // un roi en phase 2 frappe plus fort
                * (100 + boostPercent) / 100 // ses Engrenages
                * malusPercent / 100); // l'Intimidation du joueur
            totalAttack += attack;
            int shieldBefore = player.getShield();
            int lost = player.takeDamage(attack, shieldPercent);
            return new PlayerDamagedEvent(lost, shieldBefore - player.getShield(), player.getShield());
        }

        /** La Comète Dorée : {@code percent} % des PV max du joueur, à travers son bouclier. */
        PlayerDamagedEvent comet(int percent) {
            int attack = (int) Math.min(Integer.MAX_VALUE, (long) player.getMaxHp() * percent / 100 * attackFactor
                * malusPercent / 100);
            totalAttack += attack;
            int shieldBefore = player.getShield();
            int lost = player.takeDamage(attack, true);
            return new PlayerDamagedEvent(lost, shieldBefore - player.getShield(), player.getShield());
        }

        /** Il se protège de {@code defense}, multipliée par la roulette du Zéro et sa force. */
        EnemyShieldedEvent shield(int defense) {
            int total = (int) Math.min(Integer.MAX_VALUE,
                (long) enemy.getKind().fortified(defense * shieldFactor, enemy.getDifficulty()) * (100 + boostPercent) / 100);
            enemy.addShieldDefense(total);
            return new EnemyShieldedEvent(total);
        }
    }

    /** Grappin : la valeur d'une carte choisie, pour voler la meilleure (son rang, l'As au-dessus du Roi). */
    static int grappleValue(Card card) {
        return card.getRank() == 1 ? 14 : card.getRank();
    }

    /**
     * IA de l'ennemi : les {@link Enemy#PLAYS_PER_TURN} cartes à jouer parmi
     * {@code hand}. En forme, il attaque (Piques), se protège (Carreaux), tente
     * sa chance (Trèfles), et ne se soigne (Cœurs) qu'en dernier ; sous
     * {@link #LOW_HP_RATIO} de vie, il se soigne d'abord. À couleur égale, le
     * rang le plus fort passe devant. Une carte sans couleur (Rouleau interdit)
     * est toujours jouée.
     */
    static List<Card> choose(List<Card> hand, float hpRatio) {
        return choose(hand, hpRatio, EnemyKind.CROUPIER);
    }

    /**
     * IA d'un ennemi de type {@code kind} : comme {@link #choose(List, float)},
     * avec l'ordre des couleurs et le nombre de cartes de son type (le Gardien
     * se protège d'abord, la Sangsue mord, le Bretteur attaque même blessé).
     */
    static List<Card> choose(List<Card> hand, float hpRatio, EnemyKind kind) {
        return choose(hand, hpRatio, kind, kind.getPlaysPerTurn());
    }

    /** Comme {@link #choose(List, float, EnemyKind)}, en jouant {@code plays} cartes (les bras du Kraken). */
    static List<Card> choose(List<Card> hand, float hpRatio, EnemyKind kind, int plays) {
        List<Card.Suit> priority = hpRatio < LOW_HP_RATIO ? kind.getLowHpPriority() : kind.getPriority();
        return hand.stream()
            .sorted(Comparator.comparingInt((Card card) -> card.getSuit() == null ? -1 : priority.indexOf(card.getSuit()))
                .thenComparing(Comparator.comparingInt(Card::getRank).reversed()))
            .limit(plays)
            .toList();
    }

    /**
     * @return les cartes propres de sa main qu'il joue ce tour, en plus de ses autres
     *         cartes : au plus {@link EnemyKind#getOwnPlaysPerTurn()}, jamais deux de la même
     *         sorte, sauf un Compte à rebours qui ne retire plus de tour
     */
    static List<Card> ownCards(List<Card> hand, Enemy enemy) {
        int max = enemy.getKind().getOwnPlaysPerTurn();
        List<Card> chosen = new ArrayList<>();
        Set<EnemySignature> seen = EnumSet.noneOf(EnemySignature.class);
        for (Card card : hand) {
            EnemySignature signature = EnemySignature.of(card);
            if (chosen.size() < max && signature != null && playable(card, enemy) && seen.add(signature)) chosen.add(card);
        }
        return chosen;
    }

    /** @return un rouleau du joueur pas encore bloqué, au hasard, ou -1. */
    int freeReel(Set<Integer> blocked) {
        List<Integer> free = new ArrayList<>();
        for (int reel = 0; reel < SlotMachine.SYMBOL_COUNT; reel++) if (!blocked.contains(reel)) free.add(reel);
        return free.isEmpty() ? -1 : free.get(random.nextInt(free.size()));
    }

    /** Lingot : la force de son coup, en % d'une Épée, selon sa défense du moment (de x1 à x3). */
    static int ingotPercent(Enemy enemy) {
        int base = Math.max(1, enemy.getKind().getBaseDefense(enemy.getDifficulty()));
        return (int) Math.max(100, Math.min(300, (long) enemy.getDefense() * 100 / base));
    }

    /**
     * Bras armé (Bandit manchot) : pour chacun (deux avec le Levier grillé), il relance
     * son pire rouleau, celui qui l'éloigne le plus du Jackpot : un rouleau qui n'a pas
     * son symbole le plus fréquent (un Bras armé en dernier).
     */
    void armedArms(Enemy enemy, EnemySymbol[] symbols, Map<EnemySymbol, Integer> luck,
                           Map<Integer, List<Event>> reelEvents) {
        long arms = Arrays.stream(symbols).filter(symbol -> symbol == EnemySymbol.ARMED_ARM).count();
        if (arms == 0) return;
        if (enemy.takeCharge(EnemySignature.LEVIER_GRILLE)) arms++;
        for (int k = 0; k < arms; k++) {
            int reel = worstReel(symbols);
            if (reel < 0) return;
            symbols[reel] = machine.spin(enemy.getSpinWeights(), luck, 1)[0];
            reelEvents.computeIfAbsent(reel, r -> new ArrayList<>())
                .add(new StatusEvent(Lang.t("BRAS ARMÉ : ROULEAU RELANCÉ"), EffectPopup.Style.SPECIAL));
        }
    }

    /** @return le rouleau qui l'éloigne le plus du Jackpot (voir {@link #armedArms}), ou -1 s'il l'a déjà. */
    static int worstReel(EnemySymbol[] symbols) {
        Map<EnemySymbol, Integer> counts = new EnumMap<>(EnemySymbol.class);
        for (EnemySymbol symbol : symbols) if (symbol != EnemySymbol.ARMED_ARM) counts.merge(symbol, 1, Integer::sum);
        EnemySymbol target = counts.entrySet().stream()
            .max(Map.Entry.<EnemySymbol, Integer>comparingByValue().thenComparing(entry -> value(entry.getKey())))
            .map(Map.Entry::getKey).orElse(null);
        int worst = -1;
        for (int i = 0; i < symbols.length; i++) {
            if (symbols[i] == target) continue;
            if (worst < 0 || symbols[worst] == EnemySymbol.ARMED_ARM && symbols[i] != EnemySymbol.ARMED_ARM
                || symbols[i] != EnemySymbol.ARMED_ARM && symbols[worst] != EnemySymbol.ARMED_ARM
                    && value(symbols[i]) < value(symbols[worst])) {
                worst = i;
            }
        }
        return worst;
    }

    /** @return la place du premier symbole sorti au moins trois fois sur ses rouleaux (Jackpot originel), ou -1. */
    static int originalJackpot(EnemySymbol[] symbols) {
        for (int i = 0; i < symbols.length; i++) {
            EnemySymbol symbol = symbols[i];
            if (Arrays.stream(symbols).filter(other -> other == symbol).count() >= EnemySymbol.ORIGINAL_JACKPOT_COUNT) {
                return i;
            }
        }
        return -1;
    }

    /** @return la valeur d'un symbole pour lui : un symbole spécial, puis l'Épée, le Bouclier, la Potion. */
    static int value(EnemySymbol symbol) {
        return switch (symbol) {
            case POTION -> 1;
            case SHIELD -> 2;
            case SWORD  -> 3;
            default     -> 4;
        };
    }

    /** @return le nombre de cartes propres dans {@code cards}. */
    static int signatures(List<Card> cards) {
        return (int) cards.stream().filter(card -> EnemySignature.of(card) != null).count();
    }

    /** @return {@code true} si {@code card} sert encore à quelque chose (un Compte à rebours, tant que le combat peut perdre un tour). */
    static boolean playable(Card card, Enemy enemy) {
        return EnemySignature.of(card) != EnemySignature.COMPTE_A_REBOURS
            || enemy.getTurnLimit() > 0 && enemy.getLostTurns() < EnemySignature.COMPTE_MAX;
    }

    /**
     * @return le symbole que rend plus probable une de ses cartes Trèfle : un de ses
     *         rouleaux spéciaux au hasard (ni Épée, ni Bouclier, ni Potion), ou n'importe
     *         lequel s'il n'en a pas
     */
    EnemySymbol luckyTarget(Enemy enemy) {
        List<EnemySymbol> symbols = enemy.getSymbols();
        List<EnemySymbol> special = symbols.stream().filter(symbol -> !isBasic(symbol)).toList();
        List<EnemySymbol> pool = special.isEmpty() ? symbols : special;
        return pool.get(random.nextInt(pool.size()));
    }

    /** @return {@code true} pour un symbole de base : Épée, Bouclier ou Potion. */
    static boolean isBasic(EnemySymbol symbol) {
        return symbol == EnemySymbol.SWORD || symbol == EnemySymbol.SHIELD || symbol == EnemySymbol.POTION;
    }

    /** @return la place de son meilleur symbole du tirage (un symbole spécial, sinon l'Épée, le Bouclier, la Potion), ou -1. */
    static int bestSymbol(EnemySymbol[] symbols) {
        int best = -1, bestValue = -1;
        for (int i = 0; i < symbols.length; i++) {
            int value = switch (symbols[i]) {
                case POTION -> 1;
                case SHIELD -> 2;
                case SWORD  -> 3;
                default     -> 4;
            };
            if (value > bestValue) {
                best = i;
                bestValue = value;
            }
        }
        return best;
    }

    /**
     * Éclat : il ramasse un éclat ; au {@link EnemySymbol#SHARD_MAX}e, les éclats se rejoignent et frappent
     * ({@link EnemySymbol#SHARD_FACTOR} Épées) à travers le bouclier du joueur, puis il recommence.
     */
    static void pickShard(Turn turn, List<Event> events) {
        Enemy enemy = turn.enemy;
        boolean joined = enemy.addShard();
        events.add(new StatusEvent(Lang.f("ÉCLAT : {0}/{1}", joined ? EnemySymbol.SHARD_MAX : enemy.getShards(),
            EnemySymbol.SHARD_MAX), EffectPopup.Style.SPECIAL));
        if (joined) {
            events.add(new StatusEvent(Lang.t("LES ÉCLATS SE REJOIGNENT !"), EffectPopup.Style.ATTACK));
            events.add(turn.strike(enemy.getKind().swordDamage() * EnemySymbol.SHARD_FACTOR, true));
        }
    }

    /** Ses cartes propres qui agissent tout de suite : Remontoir, Compte à rebours, Encre. */
    static void playNow(List<EnemySignature> now, Turn turn, LastingEffects curses, List<Event> events) {
        Enemy enemy = turn.enemy;
        for (EnemySignature signature : now) {
            switch (signature) {
                case REMONTOIR -> {
                    boolean exploded = enemy.tickHourglass();
                    events.add(new EnemyHourglassEvent(exploded ? EnemySymbol.HOURGLASS_MAX : enemy.getHourglass(),
                        EnemySymbol.HOURGLASS_MAX, exploded));
                    if (exploded) events.add(turn.strike(EnemySymbol.HOURGLASS_DAMAGE, true)); // traverse le bouclier
                }
                case COMPTE_A_REBOURS -> {
                    if (enemy.loseTurn()) {
                        events.add(new StatusEvent(Lang.f("COMPTE À REBOURS : PLUS QUE {0} TOURS", enemy.getTurnLimit()),
                            EffectPopup.Style.DAMAGE));
                    }
                }
                case SERMENT_SOLENNEL -> {
                    if (enemy.addVow()) {
                        events.add(new StatusEvent(Lang.t("SERMENT SOLENNEL : SA DÉFENSE DE BASE MONTE"), EffectPopup.Style.DEFENSE));
                    }
                }
                case PROCESSION -> {
                    enemy.addPilgrim();
                    events.add(new StatusEvent(Lang.n(enemy.getPilgrims(), "PROCESSION : UN PÈLERIN LE SUIT",
                        "PROCESSION : {0} PÈLERINS LE SUIVENT", enemy.getPilgrims()),
                        EffectPopup.Style.SPECIAL));
                }
                case BOULE_DE_NEIGE -> {
                    enemy.addSnow();
                    events.add(new StatusEvent(Lang.f("BOULE DE NEIGE : AVALANCHE +{0} %",
                        EnemySymbol.AVALANCHE_STEP_PERCENT * enemy.getSnow()), EffectPopup.Style.SPECIAL));
                }
                case OFFRANDE -> {
                    enemy.addKarma(1);
                    events.add(new StatusEvent(Lang.f("OFFRANDE : KARMA {0}/{1}", enemy.getKarma(), EnemySymbol.KARMA_MAX),
                        EffectPopup.Style.SPECIAL));
                }
                case CHARBON -> {
                    boolean exploded = enemy.tickPressure();
                    events.add(new StatusEvent(Lang.f("CHARBON : PRESSION {0}/{1}", exploded ? EnemySymbol.PRESSURE_MAX
                        : enemy.getPressure(), EnemySymbol.PRESSURE_MAX), EffectPopup.Style.SPECIAL));
                    if (exploded) {
                        events.add(new StatusEvent(Lang.t("LA CHAUDIÈRE EXPLOSE !"), EffectPopup.Style.ATTACK));
                        events.add(turn.strike(enemy.getKind().swordDamage() * EnemySymbol.PRESSURE_FACTOR));
                    }
                }
                case SILENCE -> {
                    curses.addMalediction(1);
                    events.add(new StatusEvent(Lang.t("SILENCE : TA MEILLEURE CARTE RETOURNERA SOUS TA PIOCHE"), EffectPopup.Style.DAMAGE));
                }
                case TERRE_FRAICHE -> {
                    if (enemy.armShroud()) {
                        events.add(new StatusEvent(Lang.f("TERRE FRAÎCHE : IL SE RELÈVERA À {0} % DE SES PV", EnemySymbol.SHROUD_RISE_PERCENT),
                            EffectPopup.Style.SPECIAL));
                    }
                }
                case CLAIRON -> {
                    if (enemy.addTroop()) {
                        events.add(new StatusEvent(Lang.f("CLAIRON : {0}/{1} SOLDATS", enemy.getTroops(), EnemySymbol.RALLY_MAX),
                            EffectPopup.Style.SPECIAL));
                    }
                }
                case DERNIER_ACTE -> {
                    curses.addStrings(1);
                    events.add(new StatusEvent(Lang.t("DERNIER ACTE : IL CHOISIRA UNE CARTE QUE TU JOUERAS"), EffectPopup.Style.DAMAGE));
                }
                case BOUTON_D_APPEL -> {
                    enemy.climb(1);
                    events.add(new StatusEvent(Lang.f("BOUTON D'APPEL : ÉTAGE {0}/{1}", enemy.getFloor(), EnemySymbol.ELEVATOR_MAX),
                        EffectPopup.Style.SPECIAL));
                }
                case FONDATIONS -> {
                    enemy.build(1);
                    events.add(new StatusEvent(Lang.f("FONDATIONS : SA TOUR A {0} ÉTAGES", enemy.getStoreys()),
                        EffectPopup.Style.SPECIAL));
                }
                case POCHE_PLEINE -> pickShard(turn, events);
                case ENCRE -> {
                    curses.blind(1);
                    events.add(new StatusEvent(Lang.t("ENCRE : UNE CARTE CACHÉE AU PROCHAIN TOUR"), EffectPopup.Style.DAMAGE));
                }
                default -> { }
            }
        }
    }

    /**
     * Cartes spéciales du joueur copiées par son Ombre : chacune agit comme le
     * Reflet, à pleine force (une carte de rang {@link EnemyCards#SHADOW_SPECIAL_RANK}).
     * La carte Bingo a déjà rendu son Épée plus probable.
     */
    static void playShadows(List<EnemyCards.ShadowRole> shadows, Turn turn, List<Event> events) {
        Enemy enemy = turn.enemy;
        EnemyKind kind = enemy.getKind();
        Player player = turn.player;
        Card ten = EnemyCards.card(Card.Suit.PIQUE, EnemyCards.SHADOW_SPECIAL_RANK);
        for (EnemyCards.ShadowRole role : shadows) {
            switch (role) {
                case ATTACK -> events.add(turn.strike(kind.swordDamage() + kind.swordBonus(ten)));
                case GAINS  -> {
                    double stolen = BigNumber.percent(player.getGains(), kind.interestPercent(enemy.getDifficulty()));
                    if (stolen > 0) {
                        player.addGains(-stolen);
                        events.add(new GainsStolenEvent(stolen, 0));
                    }
                }
                case HEAL   -> events.add(heal(enemy, kind.potionPercent(EnemyCards.healBonus(ten), enemy.getDifficulty())));
                case SHIELD -> events.add(turn.shield(kind.shieldDefense() + kind.shieldBonus(ten)));
                case BINGO  -> { }
            }
        }
    }

    /** IA de la Roulette Vivante : elle n'en a pas, ses {@code plays} cartes sortent au hasard. */
    List<Card> chooseAtRandom(List<Card> hand, int plays) {
        List<Card> shuffled = new ArrayList<>(hand);
        Collections.shuffle(shuffled, random);
        return List.copyOf(shuffled.subList(0, Math.min(plays, shuffled.size())));
    }
}
