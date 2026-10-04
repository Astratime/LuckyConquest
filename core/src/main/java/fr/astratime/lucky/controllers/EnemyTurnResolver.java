package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.SlotMachine;
import fr.astratime.lucky.entities.enemy.EnemyCards;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySlotMachine;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.events.DamageReflectedEvent;
import fr.astratime.lucky.entities.events.EnemyDrainEvent;
import fr.astratime.lucky.entities.events.EnemyHourglassEvent;
import fr.astratime.lucky.entities.events.EnemyMirrorEvent;
import fr.astratime.lucky.entities.events.EnemyPhaseEvent;
import fr.astratime.lucky.entities.events.EnemyRouletteEvent;
import fr.astratime.lucky.entities.events.EnemyStakeEvent;
import fr.astratime.lucky.entities.events.GainsStolenEvent;
import fr.astratime.lucky.entities.events.GaugesDrainedEvent;
import fr.astratime.lucky.entities.events.EnemyRageEvent;
import fr.astratime.lucky.entities.events.EnemyThornsEvent;
import fr.astratime.lucky.entities.events.EnemyHealedEvent;
import fr.astratime.lucky.entities.events.EnemyShieldedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.PlayerDamagedEvent;
import fr.astratime.lucky.entities.events.ReelForbiddenEvent;
import fr.astratime.lucky.entities.events.ThornsEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Tour de l'ennemi : ses Épines du tour précédent piquent le joueur, puis il
 * pioche {@link Enemy#HAND_SIZE} cartes, en joue autant que son type le permet
 * (voir {@link #choose}), lance sa machine à sous (symboles de son type, voir
 * {@link EnemyKind}), puis chaque symbole agit, renforcé par les cartes jouées :
 * <ul>
 *   <li>Épée : attaque le joueur ({@link EnemySymbol#SWORD_DAMAGE}, + les Piques, + sa Rage) ;
 *       son bouclier absorbe les coups et s'use à chacun, et le renvoi de dégâts (Carreau)
 *       touche l'ennemi une fois toutes ses attaques portées ;</li>
 *   <li>Bouclier : défense ({@link EnemySymbol#SHIELD_DEFENSE}, + les Carreaux)
 *       qui absorbe les attaques du prochain tour du joueur, et s'use à chaque coup ;</li>
 *   <li>Potion : soin ({@link EnemySymbol#POTION_PERCENT} % des PV max, + les Cœurs) ;</li>
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
 *       d'ici là ; un coup reçu la fait retomber.</li>
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

    private final Random           random;
    private final EnemySlotMachine machine;

    public EnemyTurnResolver() {
        this(new Random());
    }

    /** @param random source d'aléatoire (ex : graine fixe pour des tests reproductibles) */
    public EnemyTurnResolver(Random random) {
        this.random  = random;
        this.machine = new EnemySlotMachine(random);
    }

    /**
     * Joue le tour complet de l'ennemi.
     *
     * @param reflectPercent part de chaque attaque renvoyée à l'ennemi (cartes Carreau du joueur)
     * @param vaultShare     part du Coffre du joueur ajoutée à l'attaque renvoyée
     */
    public EnemyTurnResult resolve(Enemy enemy, Player player, int reflectPercent, float vaultShare) {
        return resolve(enemy, player, reflectPercent, vaultShare, 0);
    }

    /**
     * Comme {@link #resolve(Enemy, Player, int, float)}, après un Bingo de bouclier du joueur.
     *
     * @param shieldReflect dégâts renvoyés à l'ennemi s'il attaque ce tour (le bouclier du Bingo), 0 sinon
     */
    public EnemyTurnResult resolve(Enemy enemy, Player player, int reflectPercent, float vaultShare,
                                   int shieldReflect) {
        EnemyKind kind = enemy.getKind();
        List<Event> openingEvents = new ArrayList<>();
        if (enemy.enterPhaseTwo()) openingEvents.add(new EnemyPhaseEvent(enemy.getPhase()));
        if (enemy.raiseStake()) openingEvents.add(new EnemyStakeEvent(enemy.getStake(), false));
        int thorns = enemy.collectThorns(); // ses Épines piquent pour les coups du tour du joueur
        if (thorns > 0) {
            int shieldBefore = player.getShield();
            int lost = player.takeDamage(thorns);
            openingEvents.add(new ThornsEvent(lost, shieldBefore - player.getShield(), player.getShield()));
        }
        enemy.resetDefense(); // la défense de son tour précédent ne valait que pour le tour du joueur
        List<Card> drawn  = enemy.draw(Enemy.HAND_SIZE);
        List<Card> played = kind.playsAtRandom() ? chooseAtRandom(drawn, kind) : choose(drawn, enemy.getHpRatio(), kind);

        int swordBonus = 0, healBonus = 0, shieldBonus = 0;
        int luckFactor = kind.royalBet() && enemy.getHpRatio() < LOW_HP_RATIO ? 2 : 1; // Mise royale
        Map<EnemySymbol, Integer> luck = new EnumMap<>(EnemySymbol.class);
        boolean forbidReel = false;
        for (Card card : played) {
            if (EnemyCards.isForbiddenReel(card)) {
                forbidReel = true;
                continue;
            }
            switch (card.getSuit()) {
                case PIQUE   -> swordBonus  += EnemyCards.swordBonus(card);
                case COEUR   -> healBonus   += EnemyCards.healBonus(card);
                case CARREAU -> shieldBonus += EnemyCards.shieldBonus(card);
                case TREFLE  -> {
                    List<EnemySymbol> symbols = enemy.getSymbols();
                    EnemySymbol lucky = symbols.get(random.nextInt(symbols.size()));
                    luck.merge(lucky, EnemyCards.luckBonus(card) * luckFactor, Integer::sum);
                }
            }
        }

        EnemySymbol[] symbols = machine.spin(enemy.getWeights(), luck);
        // Les roulettes du Zéro tournent d'abord : elles valent pour tout le tour.
        Turn turn = new Turn(enemy, player, swordBonus);
        EnemyRouletteEvent[] roulettes = new EnemyRouletteEvent[symbols.length];
        for (int i = 0; i < symbols.length; i++) {
            if (symbols[i] != EnemySymbol.ZERO) continue;
            roulettes[i] = new EnemyRouletteEvent(spinRoulette());
            if (roulettes[i].pocket != EnemyRouletteEvent.Pocket.NOIR) turn.attackFactor *= 2;
            if (roulettes[i].pocket != EnemyRouletteEvent.Pocket.ROUGE) turn.shieldFactor *= 2;
        }

        List<List<Event>> outcomes = new ArrayList<>();
        for (int i = 0; i < symbols.length; i++) {
            List<Event> events = new ArrayList<>();
            switch (symbols[i]) {
                case SWORD  -> events.add(turn.strike(enemy.getKind().swordDamage()));
                case FANG   -> bite(turn, events);
                case THORNS -> {
                    enemy.addThorns(kind.thornsPercent());
                    events.add(new EnemyThornsEvent(kind.thornsPercent(), enemy.getThornsPercent()));
                }
                case RAGE   -> events.add(rage(enemy));
                case SHIELD -> events.add(turn.shield(EnemySymbol.SHIELD_DEFENSE + shieldBonus));
                case POTION -> {
                    events.add(heal(enemy, kind.potionPercent(healBonus)));
                    if (kind.potionShields()) events.add(turn.shield(EnemySymbol.SHIELD_DEFENSE + shieldBonus));
                }
                case LOADED_DIE -> {
                    int drained = player.getLastingEffects().drainGauges(kind.diePercent());
                    events.add(new GaugesDrainedEvent(kind.diePercent(), drained));
                }
                case INTEREST -> {
                    int stolen = Math.round(player.getGains() * kind.interestPercent() / 100f);
                    if (stolen > 0) {
                        player.addGains(-stolen);
                        events.add(new GainsStolenEvent(stolen, enemy.addInterest(stolen)));
                    } else {
                        events.add(turn.strike(EnemySymbol.FANG_DAMAGE)); // rien à prendre : il mord
                    }
                }
                case ZERO   -> events.add(roulettes[i]);
                case MIRROR -> mirror(turn, events);
                case HOURGLASS -> {
                    boolean exploded = enemy.tickHourglass();
                    events.add(new EnemyHourglassEvent(exploded ? EnemySymbol.HOURGLASS_MAX : enemy.getHourglass(),
                        EnemySymbol.HOURGLASS_MAX, exploded));
                    if (exploded) events.add(turn.strike(EnemySymbol.HOURGLASS_DAMAGE));
                }
                case ALL_IN -> {
                    enemy.goAllIn();
                    events.add(new EnemyStakeEvent(enemy.getStake(), true));
                }
            }
            outcomes.add(events);
        }

        List<Event> afterEvents = new ArrayList<>();
        if (turn.totalAttack > 0 && shieldReflect > 0) {
            // Bingo de bouclier : il frappe dans le bouclier, qui lui est renvoyé entier.
            enemy.takeDamage(shieldReflect);
            afterEvents.add(new DamageReflectedEvent(shieldReflect));
        }
        if (turn.totalAttack > 0 && reflectPercent > 0 && !enemy.isDefeated()) {
            // Le Coffre arme le renvoi : une part de son contenu s'ajoute à l'attaque renvoyée.
            float reflectBase = turn.totalAttack + player.getLastingEffects().getVault() * vaultShare;
            int reflected = Math.round(reflectBase * (reflectPercent / 100f));
            if (reflected > 0) {
                enemy.takeDamage(reflected);
                afterEvents.add(new DamageReflectedEvent(reflected));
            }
        }

        if (forbidReel) {
            int reel = random.nextInt(SlotMachine.SYMBOL_COUNT);
            player.getLastingEffects().forbidReel(reel);
            afterEvents.add(new ReelForbiddenEvent(reel));
        }

        enemy.discard(drawn);
        enemy.resetDamageTaken(); // ses Épines ne comptent que les coups du prochain tour du joueur
        return new EnemyTurnResult(drawn, played, luck, symbols, outcomes, afterEvents, openingEvents);
    }

    /** Croc : il mord, et chaque PV volé lui rend une part de ses PV max. */
    private static void bite(Turn turn, List<Event> events) {
        PlayerDamagedEvent hit = turn.strike(EnemySymbol.FANG_DAMAGE);
        events.add(hit);
        Enemy enemy = turn.enemy;
        int drained = Math.round(enemy.getMaxHp() * hit.damage * enemy.getKind().drainPercent() / 100f);
        if (drained > 0) events.add(new EnemyDrainEvent(enemy.heal(drained)));
    }

    private static EnemyRageEvent rage(Enemy enemy) {
        int added = enemy.addRage(EnemySymbol.RAGE_ATTACK);
        return new EnemyRageEvent(added, enemy.getRage());
    }

    /** Il se soigne de {@code percent} % de ses PV max. */
    private static EnemyHealedEvent heal(Enemy enemy, float percent) {
        return new EnemyHealedEvent(enemy.heal(Math.round(enemy.getMaxHp() * percent / 100f)));
    }

    /**
     * Reflet : il rejoue la dernière carte (d'une couleur) jouée par le joueur
     * ce tour, à moitié de sa force, comme une de ses cartes sombres : Pique, il
     * frappe ; Cœur, il se soigne ; Carreau et Trèfle, il se protège.
     * Sans carte à copier, il frappe simplement.
     */
    private static void mirror(Turn turn, List<Event> events) {
        Card copied = null;
        List<Card> playedByPlayer = turn.player.getPlayedCards();
        for (int i = playedByPlayer.size() - 1; i >= 0 && copied == null; i--) {
            if (playedByPlayer.get(i).getSuit() != null) copied = playedByPlayer.get(i);
        }
        events.add(new EnemyMirrorEvent(copied != null ? copied.getName() : null));
        if (copied == null) {
            events.add(turn.strike(EnemySymbol.SWORD_DAMAGE));
            return;
        }
        Enemy enemy = turn.enemy;
        switch (copied.getSuit()) {
            case PIQUE   -> events.add(turn.strike(EnemySymbol.SWORD_DAMAGE + EnemyCards.swordBonus(copied) / 2));
            case COEUR   -> events.add(heal(enemy, enemy.getKind().potionPercent(EnemyCards.healBonus(copied)) / 2f));
            case CARREAU, TREFLE -> events.add(turn.shield((EnemySymbol.SHIELD_DEFENSE + EnemyCards.shieldBonus(copied)) / 2));
        }
    }

    /** @return la case où s'arrête la bille de la roulette du Zéro. */
    private EnemyRouletteEvent.Pocket spinRoulette() {
        int pocket = random.nextInt(37);
        if (pocket < EnemySymbol.ZERO_POCKETS) return EnemyRouletteEvent.Pocket.ZERO;
        return pocket % 2 == 0 ? EnemyRouletteEvent.Pocket.ROUGE : EnemyRouletteEvent.Pocket.NOIR;
    }

    /** Ce qui vaut pour toutes ses attaques et ses Boucliers d'un tour. */
    private static final class Turn {
        final Enemy  enemy;
        final Player player;
        final int    swordBonus;
        int attackFactor = 1;
        int shieldFactor = 1;
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
        PlayerDamagedEvent strike(int base) {
            int attack = enemy.getKind().empowered(
                (base + swordBonus + enemy.getRage() + enemy.spendInterest()) * enemy.getStake() * attackFactor);
            totalAttack += attack;
            int shieldBefore = player.getShield();
            int lost = player.takeDamage(attack);
            return new PlayerDamagedEvent(lost, shieldBefore - player.getShield(), player.getShield());
        }

        /** Il se protège de {@code defense}, multipliée par la roulette du Zéro et sa force. */
        EnemyShieldedEvent shield(int defense) {
            int total = enemy.getKind().empowered(defense * shieldFactor);
            enemy.addShieldDefense(total);
            return new EnemyShieldedEvent(total);
        }
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
        List<Card.Suit> priority = hpRatio < LOW_HP_RATIO ? kind.getLowHpPriority() : kind.getPriority();
        return hand.stream()
            .sorted(Comparator.comparingInt((Card card) -> card.getSuit() == null ? -1 : priority.indexOf(card.getSuit()))
                .thenComparing(Comparator.comparingInt(Card::getRank).reversed()))
            .limit(kind.getPlaysPerTurn())
            .toList();
    }

    /** IA de la Roulette Vivante : elle n'en a pas, ses cartes sortent au hasard. */
    private List<Card> chooseAtRandom(List<Card> hand, EnemyKind kind) {
        List<Card> shuffled = new ArrayList<>(hand);
        java.util.Collections.shuffle(shuffled, random);
        return List.copyOf(shuffled.subList(0, Math.min(kind.getPlaysPerTurn(), shuffled.size())));
    }
}
