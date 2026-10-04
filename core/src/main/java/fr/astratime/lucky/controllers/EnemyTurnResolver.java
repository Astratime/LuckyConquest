package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.effects.MutinyEffect;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.SlotMachine;
import fr.astratime.lucky.entities.Symbol;
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
import fr.astratime.lucky.entities.events.StatusEvent;
import fr.astratime.lucky.entities.exploration.PlaceRule;
import fr.astratime.lucky.popups.EffectPopup;
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
        return resolve(enemy, player, reflectPercent, vaultShare, shieldReflect, prickThorns(enemy, player));
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
     * Comme {@link #resolve(Enemy, Player, int, float, int)}, ses Épines ayant
     * déjà piqué (voir {@link #prickThorns}) : {@code thornsEvents} ouvre son tour.
     */
    public EnemyTurnResult resolve(Enemy enemy, Player player, int reflectPercent, float vaultShare,
                                   int shieldReflect, List<Event> thornsEvents) {
        EnemyKind kind = enemy.getKind();
        List<Event> openingEvents = new ArrayList<>();
        if (enemy.enterPhaseTwo()) {
            openingEvents.add(new EnemyPhaseEvent(enemy.getPhase(), kind.phaseText(enemy.getPhase())));
        }
        boolean foreseen = enemy.takePredictionHit(); // sa Prédiction est sortie au tirage du joueur
        if (foreseen) {
            openingEvents.add(new StatusEvent("PRÉDICTION RÉALISÉE : ATTAQUES x" + EnemySymbol.PREDICTION_FACTOR,
                EffectPopup.Style.ATTACK));
        }
        if (enemy.raiseStake()) openingEvents.add(new EnemyStakeEvent(enemy.getStake(), false));
        openingEvents.addAll(thornsEvents); // ses Épines ont piqué pour les coups du tour du joueur
        enemy.resetDefense(); // la défense de son tour précédent ne valait que pour le tour du joueur
        List<Card> drawn  = enemy.draw(Enemy.HAND_SIZE);
        int plays = enemy.getPlaysPerTurn(); // le Kraken joue une carte par bras qui lui reste
        int harpooned = enemy.takeHarpoons(); // Harpon : une carte de moins
        if (harpooned > 0) {
            plays = Math.max(0, plays - harpooned);
            openingEvents.add(new StatusEvent("HARPON : " + harpooned + " CARTE" + (harpooned > 1 ? "S" : "")
                + " DE MOINS", EffectPopup.Style.ATTACK));
        }
        if (enemy.takeMutiny()) { // Mutinerie : son équipage refuse de jouer une partie de ses cartes
            plays = Math.max(0, plays - MutinyEffect.CARDS_LESS);
            openingEvents.add(new StatusEvent("MUTINERIE : " + MutinyEffect.CARDS_LESS + " CARTES DE MOINS",
                EffectPopup.Style.SPECIAL));
        }
        List<Card> played = kind.playsAtRandom() ? chooseAtRandom(drawn, plays)
            : choose(drawn, enemy.getHpRatio(), kind, plays);

        int swordBonus = 0, healBonus = 0, shieldBonus = 0;
        // Mise royale : la Reine blessée, ou le Prétendant tant qu'il porte l'éclat de la Reine
        boolean royal = kind.royalBet() && enemy.getHpRatio() < LOW_HP_RATIO
            || kind == EnemyKind.PRETENDANT && EnemyKind.shards(enemy.getPhase()) >= 2;
        int luckFactor = royal ? 2 : 1;
        Map<EnemySymbol, Integer> luck = new EnumMap<>(EnemySymbol.class);
        boolean forbidReel = false;
        for (Card card : played) {
            if (EnemyCards.isForbiddenReel(card)) {
                // le Prétendant n'a plus le Rouleau interdit une fois l'éclat de l'Éclat Originel perdu
                forbidReel = kind != EnemyKind.PRETENDANT || EnemyKind.shards(enemy.getPhase()) >= 3;
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

        EnemySymbol[] symbols = machine.spin(enemy.getWeights(), luck, enemy.getReelCount());
        // Les roulettes du Zéro tournent d'abord : elles valent pour tout le tour.
        Turn turn = new Turn(enemy, player, swordBonus);
        if (foreseen) turn.attackFactor *= EnemySymbol.PREDICTION_FACTOR;
        EnemyRouletteEvent[] roulettes = new EnemyRouletteEvent[symbols.length];
        for (int i = 0; i < symbols.length; i++) {
            if (symbols[i] != EnemySymbol.ZERO) continue;
            roulettes[i] = new EnemyRouletteEvent(spinRoulette());
            if (roulettes[i].pocket != EnemyRouletteEvent.Pocket.NOIR) turn.attackFactor *= 2;
            if (roulettes[i].pocket != EnemyRouletteEvent.Pocket.ROUGE) turn.shieldFactor *= 2;
        }

        // Jackpot (Bandit manchot, Jackpot Vivant) : trois symboles identiques, tout le tour est multiplié.
        boolean jackpot = kind.hitsJackpots() && symbols.length > 0
            && java.util.Arrays.stream(symbols).allMatch(symbol -> symbol == symbols[0]);
        boolean loadedCoin = enemy.takeLoadedCoin(); // Pièce truquée : pas de Jackpot ce tirage
        if (jackpot && loadedCoin) {
            jackpot = false;
            openingEvents.add(new StatusEvent("PIÈCE TRUQUÉE : PAS DE JACKPOT", EffectPopup.Style.SPECIAL));
        }
        if (jackpot) {
            turn.attackFactor *= EnemySymbol.JACKPOT_FACTOR;
            turn.shieldFactor *= EnemySymbol.JACKPOT_FACTOR;
        }

        LastingEffects curses = player.getLastingEffects(); // les mauvais sorts pour le prochain tour du joueur
        List<List<Event>> outcomes = new ArrayList<>();
        for (int i = 0; i < symbols.length; i++) {
            List<Event> events = new ArrayList<>();
            if (jackpot && i == 0) {
                events.add(new StatusEvent("JACKPOT ! TOUT x" + EnemySymbol.JACKPOT_FACTOR, EffectPopup.Style.SPECIAL));
            }
            Event parried = parry(symbols[i], player);
            if (parried != null) { // Piège à rats, Cage à requin : le mauvais sort tombe à l'eau
                events.add(parried);
                outcomes.add(events);
                continue;
            }
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
                case NIBBLE -> {
                    curses.addNibble();
                    events.add(new StatusEvent("GRIGNOTAGE : UNE CARTE RONGÉE AU PROCHAIN TOUR", EffectPopup.Style.DAMAGE));
                }
                case DRUNK -> {
                    curses.addDrunk();
                    events.add(new StatusEvent("IVRESSE : UN ROULEAU TITUBERA", EffectPopup.Style.DAMAGE));
                }
                case BLIND -> {
                    curses.blind();
                    events.add(new StatusEvent("AVEUGLEMENT : MAIN CACHÉE AU PROCHAIN TOUR", EffectPopup.Style.DAMAGE));
                }
                case BOARDING -> board(turn, events);
                case NUGGET -> {
                    curses.addNugget();
                    events.add(new StatusEvent("PÉPITE : TES GAINS SERONT DES PIERRES", EffectPopup.Style.DAMAGE));
                }
                case DRILL  -> {
                    events.add(new StatusEvent("FORAGE !", EffectPopup.Style.ATTACK));
                    boolean propped = player.getLastingEffects().getPropTurns() > 0; // Étai : le bouclier tient
                    if (propped) events.add(new StatusEvent("ÉTAI : LE FORAGE NE PERCE PAS", EffectPopup.Style.DEFENSE));
                    events.add(turn.strike(kind.swordDamage(), !propped));
                }
                case ANVIL  -> {
                    enemy.addAnvil(EnemySymbol.ANVIL_ATTACK);
                    events.add(new StatusEvent("ENCLUME : ATTAQUE +" + kind.empowered(enemy.getAnvil()),
                        EffectPopup.Style.ATTACK));
                }
                case SONG   -> {
                    curses.addSong();
                    events.add(new StatusEvent("CHANT : UNE CARTE JOUÉE D'OFFICE", EffectPopup.Style.DAMAGE));
                }
                case BANK_BITE -> {
                    int stolen = Math.round(player.getGains() * kind.empowered(EnemySymbol.BANK_BITE_PERCENT) / 100f);
                    if (stolen > 0) {
                        player.addGains(-stolen);
                        events.add(new GainsStolenEvent(stolen, 0));
                        events.add(new EnemyHealedEvent(enemy.heal(stolen)));
                    } else {
                        events.add(turn.strike(EnemySymbol.FANG_DAMAGE)); // rien à dévorer : il mord
                    }
                }
                case FAKE_MONEY -> {
                    int real = Math.max(0, player.getGains() - curses.getFakeGains());
                    int faked = Math.round(real * kind.fakePercent() / 100f);
                    if (faked > 0) {
                        curses.addFakeGains(faked);
                        events.add(new StatusEvent("FAUSSE MONNAIE : " + faked + " GAINS FAUX", EffectPopup.Style.DAMAGE));
                    } else {
                        events.add(turn.strike(EnemySymbol.FANG_DAMAGE)); // rien à contrefaire : il frappe
                    }
                }
                case PREDICTION -> {
                    List<Symbol> reels = player.getSlotMachine().getReels();
                    Symbol foretold = reels.get(random.nextInt(reels.size()));
                    enemy.predict(foretold);
                    events.add(new StatusEvent("PRÉDICTION : " + foretold.getDisplayName(), EffectPopup.Style.SPECIAL));
                }
                case DUEL   -> duel(turn, events);
                case TAX    -> {
                    curses.addTax();
                    events.add(new StatusEvent("TAXE SUR TES PROCHAINES CARTES", EffectPopup.Style.DAMAGE));
                }
                case NEW_RULE -> {
                    LastingEffects.HouseRule[] rules = LastingEffects.HouseRule.values();
                    LastingEffects.HouseRule rule = rules[random.nextInt(rules.length)];
                    curses.setHouseRule(rule, EnemySymbol.NEW_RULE_TURNS);
                    events.add(new StatusEvent("NOUVELLE RÈGLE : " + rule.getAnnounce(), EffectPopup.Style.SPECIAL));
                }
                case FRISK  -> {
                    int count = Math.min(EnemySymbol.FRISK_MAX, 1 + Math.max(0, player.getGains()) / EnemySymbol.FRISK_GAINS_STEP);
                    List<String> taken = new ArrayList<>();
                    for (int k = 0; k < count; k++) {
                        Card card = player.frisk(random);
                        if (card != null) taken.add(card.getName().toUpperCase());
                    }
                    events.add(taken.isEmpty() ? turn.strike(EnemySymbol.FANG_DAMAGE)
                        : new StatusEvent("FOUILLE : " + String.join(", ", taken) + " CONFISQUÉ"
                            + (taken.size() > 1 ? "ES" : "E"), EffectPopup.Style.DAMAGE));
                }
                case BANKRUPTCY -> {
                    int fortune = Math.round(enemy.getHp() * EnemySymbol.BANKRUPTCY_FORTUNE_PERCENT / 100f);
                    if (player.getGains() > fortune) {
                        int lost = player.getGains();
                        player.addGains(-lost);
                        events.add(new StatusEvent("FAILLITE : IL TE PREND TOUT", EffectPopup.Style.DAMAGE));
                        events.add(new GainsStolenEvent(lost, 0));
                    } else {
                        int lost = enemy.takeDamage(Math.round(enemy.getHp() * EnemySymbol.BANKRUPTCY_HP_PERCENT / 100f));
                        events.add(new StatusEvent("FAILLITE : IL PERD " + lost + " PV", EffectPopup.Style.SPECIAL));
                    }
                }
            }
            outcomes.add(events);
        }

        List<Event> afterEvents = new ArrayList<>();
        if (turn.totalAttack > 0 && shieldReflect > 0) {
            // Bingo de bouclier : il frappe dans le bouclier, qui lui est renvoyé entier.
            afterEvents.add(new DamageReflectedEvent(enemy.skinned(shieldReflect)));
            enemy.takeDamage(shieldReflect);
        }
        if (turn.totalAttack > 0 && reflectPercent > 0 && !enemy.isDefeated()) {
            // Le Coffre arme le renvoi : une part de son contenu s'ajoute à l'attaque renvoyée.
            float reflectBase = turn.totalAttack + player.getLastingEffects().getVault() * vaultShare;
            int reflected = Math.round(reflectBase * (reflectPercent / 100f));
            if (reflected > 0) {
                afterEvents.add(new DamageReflectedEvent(enemy.skinned(reflected)));
                enemy.takeDamage(reflected);
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
            case NIBBLE   -> "GRIGNOTAGE";
            case BLIND    -> "AVEUGLEMENT";
            case SONG     -> "CHANT";
            case BOARDING -> "ABORDAGE";
            case FRISK    -> "FOUILLE";
            default       -> null;
        };
        if (spell != null && lasting.useTrap()) {
            return new StatusEvent("PIÈGE À RATS : " + spell + " ANNULÉ" + (symbol == EnemySymbol.FRISK ? "E" : ""),
                EffectPopup.Style.DEFENSE);
        }
        boolean takesGains = switch (symbol) {
            case INTEREST, BANK_BITE, FAKE_MONEY, BANKRUPTCY -> true;
            default -> false;
        };
        if (takesGains && lasting.getCageTurns() > 0 && player.getGains() > 0) {
            return new StatusEvent("CAGE À REQUIN : GAINS PROTÉGÉS", EffectPopup.Style.DEFENSE);
        }
        return null;
    }

    /**
     * Duel : le joueur et l'ennemi tirent chacun une carte au hasard de leur
     * deck (l'As vaut 14, une carte sans couleur 0). La plus haute frappe :
     * l'ennemi attaque comme une Épée, plus le rang de sa carte ; le joueur lui
     * retire {@link EnemySymbol#DUEL_PER_MILLE_PER_RANK} pour mille de ses PV max
     * par rang. À égalité, rien.
     */
    private void duel(Turn turn, List<Event> events) {
        Card mine   = randomCard(turn.player.getAllCards());
        Card theirs = randomCard(turn.enemy.getDeckCards().isEmpty() ? turn.enemy.getDiscardCards()
            : turn.enemy.getDeckCards());
        int myRank = duelRank(mine), theirRank = duelRank(theirs);
        events.add(new StatusEvent("DUEL : " + duelName(mine) + " CONTRE " + duelName(theirs),
            EffectPopup.Style.SPECIAL));
        if (theirRank > myRank) {
            events.add(turn.strike(EnemySymbol.SWORD_DAMAGE + theirRank));
        } else if (myRank > theirRank) {
            Enemy enemy = turn.enemy;
            int lost = enemy.takeDamage(Math.round(enemy.getMaxHp() * myRank * EnemySymbol.DUEL_PER_MILLE_PER_RANK / 1000f));
            events.add(new StatusEvent("DUEL GAGNÉ : -" + lost + " PV", EffectPopup.Style.ATTACK));
        }
    }

    private Card randomCard(List<Card> cards) {
        return cards.isEmpty() ? null : cards.get(random.nextInt(cards.size()));
    }

    /** @return la valeur de {@code card} au Duel : son rang, l'As valant 14 ; 0 sans couleur. */
    static int duelRank(Card card) {
        if (card == null || card.getSuit() == null) return 0;
        return card.getRank() == 1 ? 14 : card.getRank();
    }

    private static String duelName(Card card) {
        return card == null ? "RIEN" : card.getName().toUpperCase();
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

    /**
     * Abordage : il vole la meilleure carte du joueur (la plus forte de sa main,
     * sinon parmi celles qu'il a jouées ; jamais le Scorbut), pour tout le
     * combat, et la joue contre lui à pleine force : Pique, il frappe ; Cœur, il
     * se soigne ; Carreau et Trèfle, il se protège. Une carte sans couleur lui
     * sert d'arme. Sans rien à voler, il frappe simplement.
     */
    private static void board(Turn turn, List<Event> events) {
        Player player = turn.player;
        Card stolen = bestCard(player.getCurrentHand());
        if (stolen == null) stolen = bestCard(player.getPlayedCards());
        if (stolen == null || !player.steal(stolen)) {
            events.add(turn.strike(EnemySymbol.SWORD_DAMAGE));
            return;
        }
        events.add(new StatusEvent("ABORDAGE : " + stolen.getName().toUpperCase() + " VOLÉE", EffectPopup.Style.DAMAGE));
        Enemy enemy = turn.enemy;
        if (stolen.getSuit() == null) {
            events.add(turn.strike(EnemySymbol.SWORD_DAMAGE));
            return;
        }
        switch (stolen.getSuit()) {
            case PIQUE   -> events.add(turn.strike(EnemySymbol.SWORD_DAMAGE + EnemyCards.swordBonus(stolen)));
            case COEUR   -> events.add(heal(enemy, enemy.getKind().potionPercent(EnemyCards.healBonus(stolen))));
            case CARREAU, TREFLE -> events.add(turn.shield(EnemySymbol.SHIELD_DEFENSE + EnemyCards.shieldBonus(stolen)));
        }
    }

    /** @return la carte la plus forte de {@code cards} (une figure ou un As avant tout, puis le rang), sans le Scorbut, ou {@code null}. */
    static Card bestCard(List<Card> cards) {
        return cards.stream()
            .filter(card -> !PlaceRule.SCURVY_CARD.equals(card.getId()))
            .max(Comparator.comparingInt((Card card) -> card.getSuit() == null ? 0 : 1)
                .thenComparingInt(card -> card.getRank() == 1 ? 14 : card.getRank()))
            .orElse(null);
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
        PlayerDamagedEvent strike(int base) { return strike(base, false); }

        /** Comme {@link #strike(int)} ; avec {@code pierce} (Forage), le coup traverse le bouclier du joueur. */
        PlayerDamagedEvent strike(int base, boolean pierce) {
            int attack = enemy.getKind().empowered(
                (base + swordBonus + enemy.getRage() + enemy.getAnvil() + enemy.spendInterest())
                    * enemy.getStake() * attackFactor);
            totalAttack += attack;
            int shieldBefore = player.getShield();
            int lost = player.takeDamage(attack, pierce);
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

    /** IA de la Roulette Vivante : elle n'en a pas, ses {@code plays} cartes sortent au hasard. */
    private List<Card> chooseAtRandom(List<Card> hand, int plays) {
        List<Card> shuffled = new ArrayList<>(hand);
        java.util.Collections.shuffle(shuffled, random);
        return List.copyOf(shuffled.subList(0, Math.min(plays, shuffled.size())));
    }
}
