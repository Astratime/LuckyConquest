package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.BigNumber;
import fr.astratime.lucky.entities.effects.MutinyEffect;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.SlotMachine;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.enemy.AspirationRule;
import fr.astratime.lucky.entities.enemy.EnemyCards;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySignature;
import fr.astratime.lucky.entities.enemy.EnemySlotMachine;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.events.EnemyHpLostEvent;
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
import fr.astratime.lucky.i18n.Lang;
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
        return resolve(enemy, player, reflectPercent, vaultShare, shieldReflect, thornsEvents, 0f);
    }

    /**
     * Comme {@link #resolve(Enemy, Player, int, float, int, List)}, avec le
     * renvoi des cartes Carreau du joueur.
     *
     * @param reflectHpPercent part (%) des PV max de l'ennemi qui lui est renvoyée s'il attaque (cartes Carreau)
     */
    public EnemyTurnResult resolve(Enemy enemy, Player player, int reflectPercent, float vaultShare,
                                   int shieldReflect, List<Event> thornsEvents, float reflectHpPercent) {
        EnemyKind kind = enemy.getKind();
        List<Event> openingEvents = new ArrayList<>();
        boolean heldReel = enemy.getStolenReels() > 0;
        enemy.tickTheft(); // le rouleau volé en phase 2 passe un tour de plus chez la Machine
        enemy.tickPlating(); // son Placage a tenu le tour du joueur
        if (heldReel && enemy.getStolenReels() == 0) {
            openingEvents.add(new StatusEvent(Lang.t("ELLE TE REND TON ROULEAU"), EffectPopup.Style.DEFENSE));
        }
        if (enemy.enterPhaseTwo()) {
            openingEvents.add(new EnemyPhaseEvent(enemy.getPhase(), kind.phaseText(enemy.getPhase())));
        }
        boolean foreseen = enemy.takePredictionHit(); // sa Prédiction est sortie au tirage du joueur
        if (foreseen) {
            openingEvents.add(new StatusEvent(Lang.f("PRÉDICTION RÉALISÉE : ATTAQUES x{0}",
                EnemySymbol.PREDICTION_FACTOR),
                EffectPopup.Style.ATTACK));
        }
        if (enemy.raiseStake()) openingEvents.add(new EnemyStakeEvent(enemy.getStake(), false));
        openingEvents.addAll(thornsEvents); // ses Épines ont piqué pour les coups du tour du joueur
        enemy.resetDefense(); // la défense de son tour précédent ne valait que pour le tour du joueur
        List<Card> drawn  = new ArrayList<>(enemy.draw(Enemy.HAND_SIZE));
        int plays = enemy.getPlaysPerTurn(); // le Kraken joue une carte par bras qui lui reste
        // Main trop pleine de cartes propres : il pioche juste assez pour jouer ses cartes habituelles.
        for (int tries = 0; drawn.size() - signatures(drawn) < plays && tries < Enemy.HAND_SIZE; tries++) {
            List<Card> more = enemy.draw(1);
            if (more.isEmpty()) break;
            drawn.addAll(more);
        }
        int harpooned = enemy.takeHarpoons(); // Harpon : une carte de moins
        if (harpooned > 0) {
            plays = Math.max(0, plays - harpooned);
            openingEvents.add(new StatusEvent(Lang.f(Lang.plural(harpooned) ? "HARPON : {0} CARTES DE MOINS"
                : "HARPON : {0} CARTE DE MOINS", harpooned), EffectPopup.Style.ATTACK));
        }
        if (enemy.takeMutiny()) { // Mutinerie : son équipage refuse de jouer une partie de ses cartes
            plays = Math.max(0, plays - MutinyEffect.CARDS_LESS);
            openingEvents.add(new StatusEvent(Lang.f("MUTINERIE : {0} CARTES DE MOINS", MutinyEffect.CARDS_LESS),
                EffectPopup.Style.SPECIAL));
        }
        if (enemy.takeBailiff()) { // Huissier : le joueur est très endetté, il en profite
            plays++;
            openingEvents.add(new StatusEvent(Lang.t("HUISSIER : UNE CARTE DE PLUS"), EffectPopup.Style.ATTACK));
        }
        // Ses cartes propres se jouent en plus de ses cartes habituelles (une de chaque sorte par tour) :
        // elles ne prennent pas la place de ses couleurs, il n'est jamais moins fort qu'avant elles.
        List<Card> regular = drawn.stream().filter(card -> EnemySignature.of(card) == null).toList();
        List<Card> played = new ArrayList<>(ownCards(drawn, enemy));
        played.addAll(kind.playsAtRandom() ? chooseAtRandom(regular, plays) : choose(regular, enemy.getHpRatio(), kind, plays));

        // Ses cartes propres d'abord : celles du tour, celles qui attendent leur symbole, celles qui agissent tout de suite.
        Map<EnemySignature, Integer> boosts = new EnumMap<>(EnemySignature.class);
        List<EnemySignature> now = new ArrayList<>();
        List<EnemyCards.ShadowRole> shadows = new ArrayList<>();
        boolean forbidReel = false;
        for (Card card : played) {
            EnemySignature signature = EnemySignature.of(card);
            if (signature == EnemySignature.ECLAT_VOLE) { // une carte de la Reine ou de l'Éclat, au hasard
                if (random.nextBoolean()) signature = EnemySignature.MAIN_DE_LA_REINE;
                else {
                    forbidReel = true;
                    continue;
                }
            }
            if (signature != null) {
                switch (signature.getTiming()) {
                    case TURN -> boosts.merge(signature, 1, Integer::sum);
                    case NEXT -> enemy.addCharge(signature);
                    case NOW  -> now.add(signature);
                }
            }
            EnemyCards.ShadowRole role = EnemyCards.shadowRoleOf(card);
            if (role != null) shadows.add(role);
        }

        int swordBonus = 0, healBonus = 0, shieldBonus = 0;
        // Mise royale : la Reine blessée, ou le Prétendant tant qu'il porte l'éclat de la Reine, ou la Main de la Reine
        boolean royal = kind.royalBet() && enemy.getHpRatio() < LOW_HP_RATIO
            || kind == EnemyKind.PRETENDANT && EnemyKind.shards(enemy.getPhase()) >= 2
            || boosts.containsKey(EnemySignature.MAIN_DE_LA_REINE);
        int luckFactor = royal ? 2 : 1;
        Map<EnemySymbol, Integer> luck = new EnumMap<>(EnemySymbol.class);
        for (EnemyCards.ShadowRole role : shadows) {
            if (role == EnemyCards.ShadowRole.BINGO) luck.merge(EnemySymbol.SWORD, EnemyCards.SHADOW_BINGO_LUCK, Integer::sum);
        }
        for (Card card : played) {
            if (EnemyCards.isForbiddenReel(card)) {
                // le Prétendant n'a plus le Rouleau interdit une fois l'éclat de l'Éclat Originel perdu
                forbidReel = kind != EnemyKind.PRETENDANT || EnemyKind.shards(enemy.getPhase()) >= 3;
                continue;
            }
            if (card.getSuit() == null) continue; // carte propre, ou carte spéciale copiée par l'Ombre
            // Sac de butin : ses As volés à l'échoppe comptent double
            int times = EnemyCards.isLoot(card) ? 1 + boosts.getOrDefault(EnemySignature.SAC_DE_BUTIN, 0) : 1;
            for (int k = 0; k < times; k++) {
                switch (card.getSuit()) {
                    case PIQUE   -> swordBonus  += kind.swordBonus(card);
                    case COEUR   -> healBonus   += EnemyCards.healBonus(card);
                    case CARREAU -> shieldBonus += kind.shieldBonus(card);
                    case TREFLE  -> luck.merge(luckyTarget(enemy), EnemyCards.luckBonus(card) * luckFactor, Integer::sum);
                }
            }
        }

        EnemySymbol[] symbols = machine.spin(enemy.getWeights(), luck, enemy.getReelCount());
        Map<Integer, List<Event>> reelEvents = new java.util.HashMap<>(); // ce qui arrive à un rouleau avant qu'il agisse
        if (enemy.spendCoins()) { // Pièces avalées : le Bandit manchot s'offre un Jackpot
            java.util.Arrays.fill(symbols, EnemySymbol.SWORD);
            openingEvents.add(new StatusEvent(Lang.t("PIÈCES AVALÉES : JACKPOT !"), EffectPopup.Style.SPECIAL));
        }
        armedArms(enemy, symbols, luck, reelEvents);
        // Les roulettes du Zéro tournent d'abord : elles valent pour tout le tour.
        Turn turn = new Turn(enemy, player, swordBonus);
        if (foreseen) turn.attackFactor *= EnemySymbol.PREDICTION_FACTOR;
        EnemyRouletteEvent[] roulettes = new EnemyRouletteEvent[symbols.length];
        for (int i = 0; i < symbols.length; i++) {
            if (symbols[i] != EnemySymbol.ZERO) continue;
            // Bille truquée : la bille tombe sur le zéro
            roulettes[i] = new EnemyRouletteEvent(enemy.takeCharge(EnemySignature.BILLE_TRUQUEE)
                ? EnemyRouletteEvent.Pocket.ZERO : spinRoulette());
            if (roulettes[i].pocket != EnemyRouletteEvent.Pocket.NOIR) turn.attackFactor *= 2;
            if (roulettes[i].pocket != EnemyRouletteEvent.Pocket.ROUGE) turn.shieldFactor *= 2;
        }

        // Jackpot (Bandit manchot, Jackpot Vivant) : trois symboles identiques, tout le tour est multiplié.
        boolean jackpot = kind.hitsJackpots() && symbols.length > 0
            && java.util.Arrays.stream(symbols).allMatch(symbol -> symbol == symbols[0]);
        boolean loadedCoin = enemy.takeLoadedCoin(); // Pièce truquée : pas de Jackpot ce tirage
        if (jackpot && loadedCoin) {
            jackpot = false;
            openingEvents.add(new StatusEvent(Lang.t("PIÈCE TRUQUÉE : PAS DE JACKPOT"), EffectPopup.Style.SPECIAL));
        }
        if (jackpot) {
            turn.attackFactor *= EnemySymbol.JACKPOT_FACTOR;
            turn.shieldFactor *= EnemySymbol.JACKPOT_FACTOR;
        }
        // Jackpot originel (Machine Originelle) : trois symboles pareils, et ce symbole agit cinq fois.
        int originalJackpot = kind == EnemyKind.MACHINE_ORIGINELLE ? originalJackpot(symbols) : -1;
        // Engrenages : ses autres symboles sont plus forts ; un de plus par rouleau qu'elle a pris au joueur.
        int gears = (int) java.util.Arrays.stream(symbols).filter(symbol -> symbol == EnemySymbol.GEAR).count()
            * (1 + boosts.getOrDefault(EnemySignature.HUILE_NOIRE, 0));
        if (kind == EnemyKind.MACHINE_ORIGINELLE) {
            int taken = enemy.getStolenReels() + enemy.getAspired();
            if (taken > 0) {
                gears += taken;
                openingEvents.add(new StatusEvent(Lang.f("ROULEAUX PRIS : ENGRENAGES +{0}", taken), EffectPopup.Style.SPECIAL));
            }
        }
        turn.boostPercent = EnemySymbol.GEAR_PERCENT * gears;

        LastingEffects curses = player.getLastingEffects(); // les mauvais sorts pour le prochain tour du joueur
        // Queue de comète : son meilleur symbole du tour agit deux fois
        int best = boosts.containsKey(EnemySignature.QUEUE_DE_COMETE) ? bestSymbol(symbols) : -1;
        int healFactor = 100 + healBonus; // les Cœurs renforcent aussi ce que rendent ses Crocs et ses Morsures
        List<List<Event>> outcomes = new ArrayList<>();
        for (int i = 0; i < symbols.length; i++) {
            List<Event> events = new ArrayList<>();
            if (jackpot && i == 0) {
                events.add(new StatusEvent(Lang.f("JACKPOT ! TOUT x{0}", EnemySymbol.JACKPOT_FACTOR), EffectPopup.Style.SPECIAL));
            }
            Event parried = parry(symbols[i], player);
            if (parried != null) { // Piège à rats, Cage à requin : le mauvais sort tombe à l'eau
                events.add(parried);
                outcomes.add(events);
                continue;
            }
            events.addAll(reelEvents.getOrDefault(i, List.of()));
            int repeats = i == best ? 1 + boosts.get(EnemySignature.QUEUE_DE_COMETE) : 1;
            if (repeats > 1) events.add(new StatusEvent(Lang.t("QUEUE DE COMÈTE : IL AGIT DEUX FOIS"), EffectPopup.Style.SPECIAL));
            if (i == originalJackpot) {
                EnemySymbol triple = symbols[i];
                int same = (int) java.util.Arrays.stream(symbols).filter(symbol -> symbol == triple).count();
                repeats += Math.max(0, EnemySymbol.ORIGINAL_JACKPOT_ACTS - same);
                events.add(new StatusEvent(Lang.f("JACKPOT ORIGINEL : {0} x{1}", symbols[i].getDisplayName(),
                    EnemySymbol.ORIGINAL_JACKPOT_ACTS), EffectPopup.Style.SPECIAL));
            }
            // Maçonnerie, Double serrure, La banque ne saute jamais : le rouleau de la Maison agit deux fois
            EnemySignature house = switch (symbols[i]) {
                case LOAD_BEARING -> EnemySignature.MACONNERIE;
                case SAFE         -> EnemySignature.DOUBLE_SERRURE;
                case HOUSE_EDGE   -> EnemySignature.BANQUE_GAGNE;
                default           -> null;
            };
            if (house != null) repeats += boosts.getOrDefault(house, 0);
            for (int r = 0; r < repeats; r++) switch (symbols[i]) {
                case SWORD  -> events.add(turn.strike(enemy.getKind().swordDamage()));
                case FANG   -> bite(turn, events,
                    healFactor * (100 + EnemySignature.SAIGNEE_PERCENT * boosts.getOrDefault(EnemySignature.SAIGNEE, 0)) / 100);
                case THORNS -> {
                    int thorns = kind.thornsPercent() + EnemySignature.RONCES_PERCENT * boosts.getOrDefault(EnemySignature.RONCES, 0);
                    enemy.addThorns(thorns);
                    events.add(new EnemyThornsEvent(thorns, enemy.getThornsPercent()));
                }
                case RAGE   -> {
                    for (int k = 0; k <= boosts.getOrDefault(EnemySignature.PROVOCATION, 0); k++) events.add(rage(enemy));
                }
                case SHIELD -> events.add(turn.shield(kind.shieldDefense() + shieldBonus));
                case POTION -> {
                    events.add(heal(enemy, kind.potionPercent(healBonus)));
                    if (kind.potionShields()) events.add(turn.shield(kind.shieldDefense() + shieldBonus));
                }
                case LOADED_DIE -> {
                    int percent = Math.min(100, kind.diePercent()
                        + EnemySignature.DE_CHARGE_PERCENT * boosts.getOrDefault(EnemySignature.DE_CHARGE, 0));
                    int drained = player.getLastingEffects().drainGauges(percent);
                    events.add(new GaugesDrainedEvent(percent, drained));
                }
                case INTEREST -> {
                    double stolen = BigNumber.percent(player.getGains(), Math.min(100, kind.interestPercent()
                        + EnemySignature.TAUX_PERCENT * boosts.getOrDefault(EnemySignature.TAUX_USURAIRE, 0)));
                    if (stolen > 0) {
                        player.addGains(-stolen);
                        events.add(new GainsStolenEvent(stolen, enemy.addInterest(BigNumber.toInt(stolen))));
                    } else {
                        events.add(turn.strike(EnemySymbol.FANG_DAMAGE)); // rien à prendre : il mord
                    }
                }
                case ZERO   -> events.add(roulettes[i]);
                case MIRROR -> mirror(turn, events, enemy.takeCharge(EnemySignature.MIROIR_DEFORMANT));
                case HOURGLASS -> {
                    boolean exploded = enemy.tickHourglass();
                    events.add(new EnemyHourglassEvent(exploded ? EnemySymbol.HOURGLASS_MAX : enemy.getHourglass(),
                        EnemySymbol.HOURGLASS_MAX, exploded));
                    if (exploded) events.add(turn.strike(EnemySymbol.HOURGLASS_DAMAGE));
                }
                case ALL_IN -> {
                    enemy.goAllIn();
                    if (enemy.takeCharge(EnemySignature.TOUT_SUR_LE_ROUGE)) enemy.holdAllIn();
                    events.add(new EnemyStakeEvent(enemy.getStake(), true));
                }
                case NIBBLE -> {
                    boolean twice = enemy.takeCharge(EnemySignature.DENTS_DE_RAT);
                    curses.addNibble();
                    if (twice) curses.addNibble();
                    events.add(new StatusEvent(twice ? Lang.t("GRIGNOTAGE : DEUX CARTES RONGÉES AU PROCHAIN TOUR")
                        : Lang.t("GRIGNOTAGE : UNE CARTE RONGÉE AU PROCHAIN TOUR"), EffectPopup.Style.DAMAGE));
                }
                case DRUNK -> {
                    boolean twice = enemy.takeCharge(EnemySignature.TOURNEE);
                    curses.addDrunk();
                    if (twice) curses.addDrunk();
                    events.add(new StatusEvent(twice ? Lang.t("IVRESSE : DEUX ROULEAUX TITUBERONT")
                        : Lang.t("IVRESSE : UN ROULEAU TITUBERA"), EffectPopup.Style.DAMAGE));
                }
                case BLIND -> {
                    boolean twice = enemy.takeCharge(EnemySignature.LANTERNE);
                    curses.blind(twice ? 2 : 1);
                    events.add(new StatusEvent(twice ? Lang.t("AVEUGLEMENT : MAIN CACHÉE 2 TOURS")
                        : Lang.t("AVEUGLEMENT : MAIN CACHÉE AU PROCHAIN TOUR"), EffectPopup.Style.DAMAGE));
                }
                case BOARDING -> board(turn, events, enemy.takeCharge(EnemySignature.GRAPPIN));
                case NUGGET -> {
                    boolean twice = enemy.takeCharge(EnemySignature.TAMIS_NOIR);
                    curses.addNugget(twice ? 2 : 1);
                    events.add(new StatusEvent(twice ? Lang.t("PÉPITE : TES GAINS SERONT DES PIERRES 2 TIRAGES")
                        : Lang.t("PÉPITE : TES GAINS SERONT DES PIERRES"), EffectPopup.Style.DAMAGE));
                }
                case DRILL  -> {
                    events.add(new StatusEvent(Lang.t("FORAGE !"), EffectPopup.Style.ATTACK));
                    boolean propped = player.getLastingEffects().getPropTurns() > 0; // Étai : le bouclier tient
                    if (propped) events.add(new StatusEvent(Lang.t("ÉTAI : LE FORAGE NE PERCE PAS"), EffectPopup.Style.DEFENSE));
                    int meche = EnemySignature.MECHE_PERCENT * boosts.getOrDefault(EnemySignature.MECHE_DE_DIAMANT, 0);
                    events.add(turn.strike(kind.swordDamage() * (100 + meche) / 100, !propped));
                }
                case ANVIL  -> {
                    boolean twice = enemy.takeCharge(EnemySignature.TREMPE);
                    enemy.addAnvil(EnemySymbol.ANVIL_ATTACK * (twice ? 2 : 1));
                    events.add(new StatusEvent(Lang.f("ENCLUME : ATTAQUE +{0}", kind.hurt(enemy.getAnvil())),
                        EffectPopup.Style.ATTACK));
                }
                case SONG   -> {
                    boolean twice = enemy.takeCharge(EnemySignature.REFRAIN);
                    curses.addSong();
                    if (twice) curses.addSong();
                    events.add(new StatusEvent(twice ? Lang.t("CHANT : DEUX CARTES JOUÉES D'OFFICE")
                        : Lang.t("CHANT : UNE CARTE JOUÉE D'OFFICE"), EffectPopup.Style.DAMAGE));
                }
                case BANK_BITE -> {
                    double stolen = BigNumber.percent(player.getGains(), Math.min(100, kind.empowered(EnemySymbol.BANK_BITE_PERCENT)
                        + EnemySignature.FRENESIE_PERCENT * boosts.getOrDefault(EnemySignature.FRENESIE, 0)));
                    if (stolen > 0) {
                        player.addGains(-stolen);
                        events.add(new GainsStolenEvent(stolen, 0));
                        events.add(new EnemyHealedEvent(enemy.heal(BigNumber.toInt(stolen) * healFactor / 100)));
                    } else {
                        events.add(turn.strike(EnemySymbol.FANG_DAMAGE)); // rien à dévorer : il mord
                    }
                }
                case FAKE_MONEY -> {
                    double real = Math.max(0, player.getGains() - curses.getFakeGains());
                    double faked = BigNumber.percent(real, Math.min(100, kind.fakePercent()
                        + EnemySignature.PLANCHE_PERCENT * boosts.getOrDefault(EnemySignature.PLANCHE_A_BILLETS, 0)));
                    if (faked > 0) {
                        curses.addFakeGains(faked);
                        events.add(new StatusEvent(Lang.f("FAUSSE MONNAIE : {0} GAINS FAUX", Lang.big(faked)), EffectPopup.Style.DAMAGE));
                    } else {
                        events.add(turn.strike(EnemySymbol.FANG_DAMAGE)); // rien à contrefaire : il frappe
                    }
                }
                case PREDICTION -> {
                    List<Symbol> reels = player.getSlotMachine().getReels();
                    Symbol foretold = reels.get(random.nextInt(reels.size()));
                    Symbol second = null;
                    if (enemy.takeCharge(EnemySignature.TAROT)) { // Tarot : un deuxième symbole, différent si possible
                        List<Symbol> others = reels.stream().filter(symbol -> symbol != foretold).toList();
                        second = others.isEmpty() ? foretold : others.get(random.nextInt(others.size()));
                    }
                    enemy.predict(foretold, second);
                    events.add(new StatusEvent(second == null ? Lang.f("PRÉDICTION : {0}", foretold.getDisplayName())
                        : Lang.f("PRÉDICTION : {0} OU {1}", foretold.getDisplayName(), second.getDisplayName()),
                        EffectPopup.Style.SPECIAL));
                }
                case DUEL   -> duel(turn, events, enemy.takeCharge(EnemySignature.BOTTE_SECRETE) ? EnemySignature.BOTTE_RANKS : 0);
                case TAX    -> {
                    boolean twice = enemy.takeCharge(EnemySignature.REDRESSEMENT);
                    curses.addTax(twice ? 2 : 1);
                    events.add(new StatusEvent(twice ? Lang.t("TAXE SUR TES CARTES DES 2 PROCHAINS TOURS")
                        : Lang.t("TAXE SUR TES PROCHAINES CARTES"), EffectPopup.Style.DAMAGE));
                }
                case NEW_RULE -> {
                    LastingEffects.HouseRule[] rules = LastingEffects.HouseRule.values();
                    LastingEffects.HouseRule rule = rules[random.nextInt(rules.length)];
                    curses.setHouseRule(rule, EnemySymbol.NEW_RULE_TURNS
                        + (enemy.takeCharge(EnemySignature.REGLEMENT_INTERIEUR) ? 1 : 0));
                    events.add(new StatusEvent(Lang.f("NOUVELLE RÈGLE : {0}", rule.getAnnounce()), EffectPopup.Style.SPECIAL));
                }
                case FRISK  -> {
                    int count = Math.min(EnemySymbol.FRISK_MAX, (int) Math.min(EnemySymbol.FRISK_MAX, 1 + Math.max(0, player.getGains()) / EnemySymbol.FRISK_GAINS_STEP))
                        + (enemy.takeCharge(EnemySignature.CAMERAS) ? 1 : 0); // Caméras : une carte de plus
                    List<String> taken = new ArrayList<>();
                    for (int k = 0; k < count; k++) {
                        Card card = player.frisk(random);
                        if (card != null) taken.add(card.getName().toUpperCase());
                    }
                    events.add(taken.isEmpty() ? turn.strike(EnemySymbol.FANG_DAMAGE)
                        : new StatusEvent(Lang.f(Lang.plural(taken.size()) ? "FOUILLE : {0} CONFISQUÉES" : "FOUILLE : {0} CONFISQUÉE",
                            String.join(", ", taken)), EffectPopup.Style.DAMAGE));
                }
                case BANKRUPTCY -> {
                    // Saisie : le seuil baisse de 10 points par carte, jamais sous 10 %
                    int percent = Math.max(EnemySignature.SAISIE_FLOOR, EnemySymbol.BANKRUPTCY_FORTUNE_PERCENT
                        - EnemySignature.SAISIE_POINTS * boosts.getOrDefault(EnemySignature.SAISIE, 0));
                    int fortune = Math.round(enemy.getHp() * percent / 100f);
                    if (player.getGains() > fortune) {
                        double lost = player.getGains();
                        player.addGains(-lost);
                        events.add(new StatusEvent(Lang.t("FAILLITE : IL TE PREND TOUT"), EffectPopup.Style.DAMAGE));
                        events.add(new GainsStolenEvent(lost, 0));
                    } else {
                        int lost = enemy.takeDamage(Math.round(enemy.getHp() * EnemySymbol.BANKRUPTCY_HP_PERCENT / 100f));
                        events.add(new EnemyHpLostEvent(Lang.f("FAILLITE : IL PERD {0} PV", Lang.big(lost)), EffectPopup.Style.SPECIAL, lost));
                    }
                }
                case NOTHING_GOES -> {
                    int count = 1 + (enemy.takeCharge(EnemySignature.ANNONCE) ? 1 : 0);
                    events.add(new StatusEvent(Lang.t("RIEN NE VA PLUS !"), EffectPopup.Style.DAMAGE));
                    for (int k = 0; k < count; k++) {
                        int reel = freeReel(curses.getForbiddenReels());
                        if (reel < 0) break;
                        curses.voidReel(reel);
                        events.add(new ReelForbiddenEvent(reel));
                    }
                }
                case RAKE -> {
                    int percent = EnemySymbol.RAKE_PERCENT * (enemy.takeCharge(EnemySignature.COUP_DE_RATEAU) ? 2 : 1);
                    double raked = BigNumber.percent(player.getGains(), percent);
                    if (raked > 0) {
                        player.addGains(-raked);
                        events.add(new GainsStolenEvent(raked, 0));
                        events.add(turn.shield((int) Math.min(raked, (double) kind.shieldDefense() * EnemySymbol.RAKE_MAX_SHIELDS)));
                    } else {
                        events.add(turn.strike(EnemySymbol.FANG_DAMAGE)); // rien à ramasser : il frappe
                    }
                }
                case CLOAKROOM -> {
                    int count = 1 + (enemy.takeCharge(EnemySignature.TENUE_EXIGEE) ? 1 : 0);
                    List<String> kept = new ArrayList<>();
                    for (int k = 0; k < count; k++) {
                        Card card = bestCard(player.getCurrentHand());
                        if (card == null) card = bestCard(player.getPlayedCards());
                        if (card != null && player.steal(card, EnemySymbol.CLOAKROOM_TURNS)) kept.add(card.getName().toUpperCase());
                    }
                    events.add(kept.isEmpty() ? turn.strike(EnemySymbol.FANG_DAMAGE)
                        : new StatusEvent(Lang.f("VESTIAIRE : {0} AU VESTIAIRE", String.join(", ", kept)), EffectPopup.Style.DAMAGE));
                }
                case BOUNCER -> events.add(turn.strike(kind.swordDamage()
                    * (100 + EnemySignature.VIDEUR_PERCENT * boosts.getOrDefault(EnemySignature.PAS_CE_SOIR, 0)) / 100,
                    100 - EnemySymbol.BOUNCER_PIERCE_PERCENT));
                case LOAD_BEARING -> {
                    events.add(turn.shield(kind.shieldDefense() + shieldBonus));
                    enemy.holdDefense();
                    events.add(new StatusEvent(Lang.t("MUR PORTEUR : SA DÉFENSE NE S'USE PAS"), EffectPopup.Style.DEFENSE));
                }
                case SAFE -> {
                    events.add(heal(enemy, EnemySymbol.SAFE_HEAL_PERCENT * healFactor / 100f));
                    double stolen = BigNumber.percent(player.getGains(), EnemySymbol.SAFE_STEAL_PERCENT);
                    if (stolen > 0) {
                        player.addGains(-stolen);
                        events.add(new GainsStolenEvent(stolen, 0));
                    }
                }
                case HOUSE_EDGE -> {
                    events.add(turn.strike(kind.swordDamage())); // la Salle de jeu frappe, et la Maison prend sa part
                    curses.addHouseEdge();
                    events.add(new StatusEvent(Lang.f("AVANTAGE DE LA MAISON : TES GAINS -{0} %", EnemySymbol.HOUSE_EDGE_PERCENT),
                        EffectPopup.Style.DAMAGE));
                }
                case GEAR -> events.add(new StatusEvent(Lang.f("ENGRENAGE : SES SYMBOLES +{0} %", turn.boostPercent),
                    EffectPopup.Style.SPECIAL));
                case SHORT_CIRCUIT -> {
                    boolean twice = enemy.takeCharge(EnemySignature.SURTENSION);
                    curses.addShortCircuit(twice ? 2 : 1);
                    events.add(new StatusEvent(twice ? Lang.t("COURT-CIRCUIT : 2 ROULEAUX GRILLERONT")
                        : Lang.t("COURT-CIRCUIT : TON MEILLEUR ROULEAU GRILLERA"), EffectPopup.Style.DAMAGE));
                }
                case GOLDEN_COMET -> {
                    int falls = enemy.takeCharge(EnemySignature.PLUIE_D_ETOILES) ? 2 : 1;
                    events.add(new StatusEvent(Lang.t("LA COMÈTE DORÉE TOMBE !"), EffectPopup.Style.ATTACK));
                    for (int k = 0; k < falls; k++) events.add(turn.comet(EnemySymbol.GOLDEN_COMET_PERCENT));
                }
                case ASPIRATION -> {
                    AspirationRule rule = AspirationRule.random(random, enemy.takeCharge(EnemySignature.GRANDE_ASPIRATION));
                    curses.addAspiration(rule);
                    events.add(new StatusEvent(Lang.f("ASPIRATION : {0}", rule.text().toUpperCase()), EffectPopup.Style.SPECIAL));
                }
                case PLATING -> {
                    int turns = EnemySymbol.PLATING_TURNS * (enemy.takeCharge(EnemySignature.OR_MASSIF) ? 2 : 1);
                    enemy.plate(turns);
                    events.add(new StatusEvent(Lang.plural(turns) ? Lang.f("PLACAGE : PEAU D'OR {0} TOURS", turns)
                        : Lang.t("PLACAGE : PEAU D'OR 1 TOUR"), EffectPopup.Style.DEFENSE));
                }
                case INGOT -> events.add(turn.strike(kind.swordDamage() * ingotPercent(enemy)
                    / 100 * (100 + EnemySignature.LINGOT_PERCENT * boosts.getOrDefault(EnemySignature.POIDS_DE_L_OR, 0)) / 100));
                case ROCKFALL -> {
                    events.add(turn.strike(kind.swordDamage()));
                    boolean twice = enemy.takeCharge(EnemySignature.GRONDEMENT);
                    curses.addRockfall(twice ? 2 : 1);
                    events.add(new StatusEvent(twice ? Lang.t("ÉBOULEMENT : 2 CARTES DE MOINS")
                        : Lang.t("ÉBOULEMENT : 1 CARTE DE MOINS"), EffectPopup.Style.DAMAGE));
                }
                case HEARTBEAT -> {
                    int beats = enemy.takeCharge(EnemySignature.FILON) ? 2 : 1;
                    for (int k = 0; k < beats; k++) events.add(heal(enemy, EnemySymbol.HEARTBEAT_PERCENT * healFactor / 100f));
                    enemy.plate(EnemySymbol.PLATING_TURNS);
                    events.add(new StatusEvent(Lang.t("BATTEMENT : SA PEAU D'OR REVIENT"), EffectPopup.Style.DEFENSE));
                }
                case SWALLOWED_COIN -> {
                    double stolen = BigNumber.percent(player.getGains(), EnemySymbol.COIN_PERCENT);
                    if (stolen > 0) {
                        player.addGains(-stolen);
                        events.add(new GainsStolenEvent(stolen, 0));
                    }
                    int coins = enemy.swallowCoins(enemy.takeCharge(EnemySignature.MONNAIE) ? 2 : 1);
                    events.add(new StatusEvent(Lang.f("PIÈCE AVALÉE : {0}/{1}", Math.min(coins, EnemySymbol.COINS_FOR_JACKPOT),
                        EnemySymbol.COINS_FOR_JACKPOT), EffectPopup.Style.SPECIAL));
                }
                case ARMED_ARM -> { } // son effet a joué avant les autres : il a relancé son pire rouleau
            }
            outcomes.add(events);
        }

        List<Event> afterEvents = new ArrayList<>();
        playNow(now, turn, curses, afterEvents);
        playShadows(shadows, turn, afterEvents);
        if (turn.totalAttack > 0 && shieldReflect > 0) {
            // Bingo de bouclier : il frappe dans le bouclier, qui lui est renvoyé entier.
            afterEvents.add(new DamageReflectedEvent((int) enemy.skinned(shieldReflect)));
            enemy.takeDamage(shieldReflect);
        }
        // Le Coffre arme le renvoi : une part de son contenu s'ajoute aux dégâts renvoyés.
        float vaultReflect = player.getLastingEffects().getVault() * vaultShare;
        if (turn.totalAttack > 0 && reflectPercent > 0 && !enemy.isDefeated()) {
            float reflectBase = turn.totalAttack + vaultReflect;
            int reflected = Math.round(reflectBase * (reflectPercent / 100f));
            if (reflected > 0) {
                afterEvents.add(new DamageReflectedEvent((int) enemy.skinned(reflected)));
                enemy.takeDamage(reflected);
            }
        }
        if (turn.totalAttack > 0 && reflectHpPercent > 0 && !enemy.isDefeated()) {
            // Cartes Carreau : une part de ses PV max, quel que soit le lieu.
            int reflected = Math.round(enemy.getMaxHp() * (reflectHpPercent / 100f) + vaultReflect);
            if (reflected > 0) {
                afterEvents.add(new DamageReflectedEvent((int) enemy.skinned(reflected)));
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
    private void duel(Turn turn, List<Event> events, int penalty) {
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
            int lost = enemy.takeDamage(Math.round(enemy.getMaxHp() * myRank * EnemySymbol.DUEL_PER_MILLE_PER_RANK / 1000f));
            events.add(new EnemyHpLostEvent(Lang.f("DUEL GAGNÉ : -{0} PV", Lang.big(lost)), EffectPopup.Style.ATTACK, lost));
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
        return card == null ? Lang.t("RIEN") : card.getName().toUpperCase();
    }

    /**
     * Croc : il mord, et chaque PV volé lui rend une part de ses PV max, à
     * {@code healPercent} % (renforcée par ses Cœurs et ses Saignées).
     */
    private static void bite(Turn turn, List<Event> events, int healPercent) {
        PlayerDamagedEvent hit = turn.strike(EnemySymbol.FANG_DAMAGE);
        events.add(hit);
        Enemy enemy = turn.enemy;
        int drained = Math.round(enemy.getMaxHp() * hit.damage * enemy.getKind().drainPercent() / 100f * healPercent / 100f);
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
     * ce tour, à moitié de sa force (à pleine force avec {@code full}, le Miroir déformant), comme une de ses cartes sombres : Pique, il
     * frappe ; Cœur, il se soigne ; Carreau et Trèfle, il se protège.
     * Sans carte à copier, il frappe simplement.
     */
    private static void mirror(Turn turn, List<Event> events, boolean full) {
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
            case COEUR   -> events.add(heal(enemy, enemy.getKind().potionPercent(EnemyCards.healBonus(copied)) / divisor));
            case CARREAU, TREFLE -> events.add(turn.shield((turn.enemy.getKind().shieldDefense() + turn.enemy.getKind().shieldBonus(copied)) / divisor));
        }
    }

    /**
     * Abordage : il vole la meilleure carte du joueur (la plus forte de sa main,
     * sinon parmi celles qu'il a jouées ; jamais le Scorbut), pendant
     * {@link EnemySymbol#BOARDING_TURNS} tours (avec le Grappin, la 2e meilleure aussi), et la joue contre lui à pleine force : Pique, il frappe ; Cœur, il
     * se soigne ; Carreau et Trèfle, il se protège. Une carte sans couleur lui
     * sert d'arme. Sans rien à voler, il frappe simplement.
     */
    private static void board(Turn turn, List<Event> events, boolean grapple) {
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
            case COEUR   -> events.add(heal(enemy, enemy.getKind().potionPercent(EnemyCards.healBonus(stolen))));
            case CARREAU, TREFLE -> events.add(turn.shield(turn.enemy.getKind().shieldDefense() + turn.enemy.getKind().shieldBonus(stolen)));
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
    static final class Turn {
        final Enemy  enemy;
        final Player player;
        final int    swordBonus;
        int attackFactor = 1;
        int shieldFactor = 1;
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
                    * enemy.getStake() * attackFactor) * enemy.attackPercent() / 100 // un roi en phase 2 frappe plus fort
                * (100 + boostPercent) / 100); // ses Engrenages
            totalAttack += attack;
            int shieldBefore = player.getShield();
            int lost = player.takeDamage(attack, shieldPercent);
            return new PlayerDamagedEvent(lost, shieldBefore - player.getShield(), player.getShield());
        }

        /** La Comète Dorée : {@code percent} % des PV max du joueur, à travers son bouclier. */
        PlayerDamagedEvent comet(int percent) {
            int attack = (int) Math.min(Integer.MAX_VALUE, (long) player.getMaxHp() * percent / 100 * attackFactor);
            totalAttack += attack;
            int shieldBefore = player.getShield();
            int lost = player.takeDamage(attack, true);
            return new PlayerDamagedEvent(lost, shieldBefore - player.getShield(), player.getShield());
        }

        /** Il se protège de {@code defense}, multipliée par la roulette du Zéro et sa force. */
        EnemyShieldedEvent shield(int defense) {
            int total = (int) Math.min(Integer.MAX_VALUE,
                (long) enemy.getKind().empowered(defense * shieldFactor) * (100 + boostPercent) / 100);
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

    /**
     * @return les cartes propres de sa main qu'il joue ce tour, en plus de ses autres
     *         cartes : une de chaque sorte, sauf un Compte à rebours qui ne retire plus de tour
     */
    static List<Card> ownCards(List<Card> hand, Enemy enemy) {
        List<Card> chosen = new ArrayList<>();
        java.util.Set<EnemySignature> seen = java.util.EnumSet.noneOf(EnemySignature.class);
        for (Card card : hand) {
            EnemySignature signature = EnemySignature.of(card);
            if (signature != null && playable(card, enemy) && seen.add(signature)) chosen.add(card);
        }
        return chosen;
    }

    /** @return un rouleau du joueur pas encore bloqué, au hasard, ou -1. */
    private int freeReel(java.util.Set<Integer> blocked) {
        List<Integer> free = new ArrayList<>();
        for (int reel = 0; reel < SlotMachine.SYMBOL_COUNT; reel++) if (!blocked.contains(reel)) free.add(reel);
        return free.isEmpty() ? -1 : free.get(random.nextInt(free.size()));
    }

    /** Lingot : la force de son coup, en % d'une Épée, selon sa défense du moment (de x1 à x3). */
    static int ingotPercent(Enemy enemy) {
        int base = Math.max(1, enemy.getKind().getBaseDefense());
        return (int) Math.max(100, Math.min(300, (long) enemy.getDefense() * 100 / base));
    }

    /**
     * Bras armé (Bandit manchot) : pour chacun (deux avec le Levier grillé), il relance
     * son pire rouleau, celui qui l'éloigne le plus du Jackpot : un rouleau qui n'a pas
     * son symbole le plus fréquent (un Bras armé en dernier).
     */
    private void armedArms(Enemy enemy, EnemySymbol[] symbols, Map<EnemySymbol, Integer> luck,
                           Map<Integer, List<Event>> reelEvents) {
        long arms = java.util.Arrays.stream(symbols).filter(symbol -> symbol == EnemySymbol.ARMED_ARM).count();
        if (arms == 0) return;
        if (enemy.takeCharge(EnemySignature.LEVIER_GRILLE)) arms++;
        for (int k = 0; k < arms; k++) {
            int reel = worstReel(symbols);
            if (reel < 0) return;
            symbols[reel] = machine.spin(enemy.getWeights(), luck, 1)[0];
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
            if (java.util.Arrays.stream(symbols).filter(other -> other == symbol).count() >= EnemySymbol.ORIGINAL_JACKPOT_COUNT) {
                return i;
            }
        }
        return -1;
    }

    /** @return la valeur d'un symbole pour lui : un symbole spécial, puis l'Épée, le Bouclier, la Potion. */
    private static int value(EnemySymbol symbol) {
        return switch (symbol) {
            case POTION -> 1;
            case SHIELD -> 2;
            case SWORD  -> 3;
            default     -> 4;
        };
    }

    /** @return le nombre de cartes propres dans {@code cards}. */
    private static int signatures(List<Card> cards) {
        return (int) cards.stream().filter(card -> EnemySignature.of(card) != null).count();
    }

    /** @return {@code true} si {@code card} sert encore à quelque chose (un Compte à rebours, tant que le combat peut perdre un tour). */
    private static boolean playable(Card card, Enemy enemy) {
        return EnemySignature.of(card) != EnemySignature.COMPTE_A_REBOURS
            || enemy.getTurnLimit() > 0 && enemy.getLostTurns() < EnemySignature.COMPTE_MAX;
    }

    /**
     * @return le symbole que rend plus probable une de ses cartes Trèfle : un de ses
     *         rouleaux spéciaux au hasard (ni Épée, ni Bouclier, ni Potion), ou n'importe
     *         lequel s'il n'en a pas
     */
    private EnemySymbol luckyTarget(Enemy enemy) {
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

    /** Ses cartes propres qui agissent tout de suite : Remontoir, Compte à rebours, Encre. */
    private static void playNow(List<EnemySignature> now, Turn turn, LastingEffects curses, List<Event> events) {
        Enemy enemy = turn.enemy;
        for (EnemySignature signature : now) {
            switch (signature) {
                case REMONTOIR -> {
                    boolean exploded = enemy.tickHourglass();
                    events.add(new EnemyHourglassEvent(exploded ? EnemySymbol.HOURGLASS_MAX : enemy.getHourglass(),
                        EnemySymbol.HOURGLASS_MAX, exploded));
                    if (exploded) events.add(turn.strike(EnemySymbol.HOURGLASS_DAMAGE));
                }
                case COMPTE_A_REBOURS -> {
                    if (enemy.loseTurn()) {
                        events.add(new StatusEvent(Lang.f("COMPTE À REBOURS : PLUS QUE {0} TOURS", enemy.getTurnLimit()),
                            EffectPopup.Style.DAMAGE));
                    }
                }
                case ENCRE -> {
                    curses.blind(1);
                    events.add(new StatusEvent(Lang.t("ENCRE : MAIN CACHÉE AU PROCHAIN TOUR"), EffectPopup.Style.DAMAGE));
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
    private static void playShadows(List<EnemyCards.ShadowRole> shadows, Turn turn, List<Event> events) {
        Enemy enemy = turn.enemy;
        EnemyKind kind = enemy.getKind();
        Player player = turn.player;
        Card ten = EnemyCards.card(Card.Suit.PIQUE, EnemyCards.SHADOW_SPECIAL_RANK);
        for (EnemyCards.ShadowRole role : shadows) {
            switch (role) {
                case ATTACK -> events.add(turn.strike(kind.swordDamage() + kind.swordBonus(ten)));
                case GAINS  -> {
                    double stolen = BigNumber.percent(player.getGains(), kind.interestPercent());
                    if (stolen > 0) {
                        player.addGains(-stolen);
                        events.add(new GainsStolenEvent(stolen, 0));
                    }
                }
                case HEAL   -> events.add(heal(enemy, kind.potionPercent(EnemyCards.healBonus(ten))));
                case SHIELD -> events.add(turn.shield(kind.shieldDefense() + kind.shieldBonus(ten)));
                case BINGO  -> { }
            }
        }
    }

    /** IA de la Roulette Vivante : elle n'en a pas, ses {@code plays} cartes sortent au hasard. */
    private List<Card> chooseAtRandom(List<Card> hand, int plays) {
        List<Card> shuffled = new ArrayList<>(hand);
        java.util.Collections.shuffle(shuffled, random);
        return List.copyOf(shuffled.subList(0, Math.min(plays, shuffled.size())));
    }
}
