package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.effects.MutinyEffect;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.EnemyMaluses;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.SlotMachine;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.enemy.EnemyCards;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySignature;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.enemy.EnemyTrait;
import fr.astratime.lucky.entities.events.DamageReflectedEvent;
import fr.astratime.lucky.entities.events.BreachEvent;
import fr.astratime.lucky.entities.events.EnemyPhaseEvent;
import fr.astratime.lucky.entities.events.EnemyRouletteEvent;
import fr.astratime.lucky.entities.events.EnemyStakeEvent;
import fr.astratime.lucky.entities.events.EnemyHealedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.PlayerDamagedEvent;
import fr.astratime.lucky.entities.events.ReelForbiddenEvent;
import fr.astratime.lucky.entities.events.StatusEvent;
import fr.astratime.lucky.entities.exploration.PlaceRule;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Arrays;

import static fr.astratime.lucky.controllers.EnemyTurnResolver.*;

/**
 * Un tour de l'ennemi, phase par phase (voir {@link EnemyTurnResolver#resolve(Enemy, Player, EnemyTurnResolver.Reflection, List)}) :
 * ce que chaque phase décide sert aux suivantes.
 */
final class EnemyTurnResolution extends EnemyPlaceSymbols {
    EnemyTurnResolution(EnemyTurnResolver resolver, Enemy enemy, Player player) {
        super(resolver, enemy, player);
    }

    /** Début de son tour : rouleau rendu, phases, Créances, Prédiction, Tapis, Épines ; sa défense retombe. */
    void open(List<Event> thornsEvents) {
        boolean heldReel = enemy.getStolenReels() > 0;
        enemy.tickTheft(); // le rouleau volé en phase 2 passe un tour de plus chez la Machine
        enemy.tickPlating(); // son Placage a tenu le tour du joueur
        if (heldReel && enemy.getStolenReels() == 0) {
            openingEvents.add(new StatusEvent(Lang.t("ELLE TE REND TON ROULEAU"), EffectPopup.Style.DEFENSE));
        }
        enemy.unseal(); // son Sarcophage s'ouvre
        enemy.crumble(); // sa Pétrification retombe en poussière
        if (enemy.enterPhaseTwo()) {
            openingEvents.add(new EnemyPhaseEvent(enemy.getPhase(), kind.phaseText(enemy.getPhase())));
            long breach = enemy.releaseBreach(); // Brèche : le surplus arrêté par sa Garde le frappe
            if (breach > 0) openingEvents.add(new BreachEvent(breach));
            if (kind == EnemyKind.GRAND_CROUPIER && enemy.getPhase() == 3) player.getLastingEffects().rig(-1, null, 0);
            if (kind.hasSarcophagus() && enemy.getPhase() == 2) { // le Pharaon s'enferme et se soigne
                enemy.seal();
                openingEvents.add(new StatusEvent(Lang.t("SARCOPHAGE : IL S'ENFERME UN TOUR"), EffectPopup.Style.DEFENSE));
                openingEvents.add(new EnemyHealedEvent(enemy.heal(
                    enemy.getMaxHp() * EnemySymbol.SARCOPHAGUS_HEAL_PERCENT / 100)));
            }
        }
        actFactor = enemy.takeActFactor(player.getPlayedCards()); // Numéro raté : ses attaques du tour frappent plus fort
        if (actFactor > 1) {
            openingEvents.add(new StatusEvent(Lang.f("NUMÉRO RATÉ : ATTAQUES x{0}", actFactor), EffectPopup.Style.ATTACK));
        }
        int bills = player.getLastingEffects().getBills(); // Lettres de change : ses Créances sur le joueur
        if (bills > 0 && !player.isDefeated()) {
            int shieldBefore = player.getShield();
            int lost = player.takeDamage((int) Math.max(1L, (long) player.getMaxHp() * bills * EnemySymbol.BILL_PER_MILLE / 1000), 0);
            openingEvents.add(new StatusEvent(Lang.f("CRÉANCES x{0}", bills), EffectPopup.Style.DAMAGE));
            openingEvents.add(new PlayerDamagedEvent(lost, shieldBefore - player.getShield(), player.getShield()));
        }
        omenMissed = enemy.takeOmenMissed(); // Présage (Temple du Destin) : le symbole annoncé n'est pas sorti
        foreseen = enemy.takePredictionHit(); // sa Prédiction est sortie au tirage du joueur
        if (foreseen) {
            openingEvents.add(new StatusEvent(Lang.f("PRÉDICTION RÉALISÉE : ATTAQUES x{0}",
                EnemySymbol.PREDICTION_FACTOR),
                EffectPopup.Style.ATTACK));
        }
        if (enemy.raiseStake()) openingEvents.add(new EnemyStakeEvent(enemy.getStake(), false));
        openingEvents.addAll(thornsEvents); // ses Épines ont piqué pour les coups du tour du joueur
        enemy.resetDefense(); // la défense de son tour précédent ne valait que pour le tour du joueur
        rigTable();
        seizeBonuses();
    }

    /** Saisie du Croupier : il prend tes bonus les plus remplis (voir {@link EnemyKind#CROUPIER_SEIZE_PERCENT}). */
    void seizeBonuses() {
        if (kind != EnemyKind.GRAND_CROUPIER || enemy.isDefeated() || player.isDefeated()) return;
        LastingEffects lasting = player.getLastingEffects();
        boolean combos = !fr.astratime.lucky.entities.Combo.formed(player.getPlayedCards()).isEmpty();
        List<LastingEffects.Seizable> targets = new ArrayList<>(List.of(LastingEffects.Seizable.VAULT,
            LastingEffects.Seizable.BLADES, LastingEffects.Seizable.BLOOD, LastingEffects.Seizable.STAR));
        targets.removeIf(bonus -> lasting.fill(bonus) <= 0);
        targets.sort(Comparator.comparingDouble(lasting::fill).reversed());
        if (combos) { // tes combinaisons passent avant une jauge presque vide
            int at = 0;
            while (at < targets.size() && lasting.fill(targets.get(at)) >= EnemyKind.CROUPIER_SEIZE_COMBO_FILL) at++;
            targets.add(at, LastingEffects.Seizable.COMBOS);
        }
        int seizures = Math.min(targets.size(), enemy.getPhase() >= 3 ? 2 : 1);
        for (LastingEffects.Seizable bonus : targets.subList(0, seizures)) {
            lasting.seize(bonus, EnemyKind.CROUPIER_SEIZE_PERCENT);
            openingEvents.add(new StatusEvent(bonus == LastingEffects.Seizable.COMBOS
                ? Lang.t("SAISIE : TES COMBINAISONS COMPTENT À MOITIÉ")
                : Lang.f("SAISIE : {0} -{1} %", seizedName(bonus), EnemyKind.CROUPIER_SEIZE_PERCENT),
                EffectPopup.Style.DAMAGE));
        }
    }

    /** @return le nom affiché d'une jauge saisie. */
    String seizedName(LastingEffects.Seizable bonus) {
        return switch (bonus) {
            case VAULT -> Lang.t("COFFRE");
            case BLADES -> Lang.t("LAMES");
            case BLOOD -> Lang.t("SANG");
            case STAR -> Lang.t("BONNE ÉTOILE");
            case COMBOS -> Lang.t("COMBINAISONS");
        };
    }

    /** Table truquée (Croupier, phase 2) : un rouleau du joueur affichera le symbole qu'il choisit. */
    void rigTable() {
        if (kind != EnemyKind.GRAND_CROUPIER || enemy.getPhase() != 2) return;
        LastingEffects lasting = player.getLastingEffects();
        if (lasting.getRiggedTurns() > 0) return;
        Symbol symbol = rigSymbol(player.getSlotMachine().getReels());
        if (symbol == null) return;
        int reel = random.nextInt(SlotMachine.SYMBOL_COUNT);
        lasting.rig(reel, symbol, EnemyKind.CROUPIER_RIG_TURNS);
        openingEvents.add(new StatusEvent(Lang.f("TABLE TRUQUÉE : ROULEAU {0} = {1}", reel + 1,
            symbol.getDisplayName()), EffectPopup.Style.DAMAGE));
    }

    /** Il pioche, puis choisit ses cartes (ses cartes propres en plus de ses couleurs). */
    void drawCards() {
        drawn = new ArrayList<>(enemy.takeRevealedHand()); // cartes sur table : il joue la main que le joueur a vue
        drawn.addAll(enemy.draw(Math.max(0, Enemy.HAND_SIZE - drawn.size())));
        int plays = enemy.getPlaysPerTurn(); // le Kraken joue une carte par bras qui lui reste
        if (kind == EnemyKind.GRAND_CROUPIER && enemy.getPhase() >= 3) plays += EnemyKind.CROUPIER_OPEN_PLAYS;
        // Main trop pleine de cartes propres : il pioche juste assez pour jouer ses cartes habituelles.
        // (Le Joueur Masqué garde une carte propre par lieu traversé : il lui faut parfois plus de pioches.)
        // Comme pour le joueur, sa main ne dépasse jamais Enemy.HAND_SIZE : une carte propre qu'il
        // ne jouera pas ce tour (en double, ou au-delà de ce qu'il peut jouer) part dans sa défausse
        // pour laisser la place à la nouvelle carte. Il joue donc les mêmes cartes qu'avant. Seul le
        // Kraken (8 bras + sa carte propre) peut encore en avoir 9 : toutes lui servent.
        for (int tries = 0; drawn.size() - signatures(drawn) < plays && tries < 3 * Enemy.HAND_SIZE; tries++) {
            if (drawn.size() >= Enemy.HAND_SIZE) discardUselessOwnCard();
            List<Card> more = enemy.draw(1);
            if (more.isEmpty()) break;
            drawn.addAll(more);
        }
        int gnawed = enemy.getMaluses().takeGnawed(); // Dents de rat : des cartes de sa main partent à la défausse
        for (int k = 0; k < gnawed && !drawn.isEmpty(); k++) enemy.discard(List.of(drawn.remove(random.nextInt(drawn.size()))));
        if (gnawed > 0) {
            openingEvents.add(new StatusEvent(Lang.n(gnawed, "DENTS DE RAT : {0} CARTE RONGÉE", "DENTS DE RAT : {0} CARTES RONGÉES",
                gnawed), EffectPopup.Style.DEFENSE));
        }
        int fewer = enemy.getMaluses().takeFewerPlays(); // Tournée du patron, Momification : des cartes de moins
        if (fewer > 0) {
            plays = Math.max(0, plays - fewer);
            openingEvents.add(new StatusEvent(Lang.n(fewer, "LIGOTÉ : {0} CARTE DE MOINS", "LIGOTÉ : {0} CARTES DE MOINS",
                fewer), EffectPopup.Style.DEFENSE));
        }
        int harpooned = enemy.getMaluses().takeHarpoons(); // Harpon : une carte de moins
        if (harpooned > 0) {
            plays = Math.max(0, plays - harpooned);
            openingEvents.add(new StatusEvent(Lang.n(harpooned, "HARPON : {0} CARTE DE MOINS",
                "HARPON : {0} CARTES DE MOINS", harpooned), EffectPopup.Style.ATTACK));
        }
        if (enemy.getMaluses().takeMutiny()) { // Mutinerie : son équipage refuse de jouer une partie de ses cartes
            plays = Math.max(0, plays - MutinyEffect.CARDS_LESS);
            openingEvents.add(new StatusEvent(Lang.f("MUTINERIE : {0} CARTES DE MOINS", MutinyEffect.CARDS_LESS),
                EffectPopup.Style.SPECIAL));
        }
        if (enemy.getMaluses().takeBailiff()) { // Huissier : le joueur est très endetté, il en profite
            plays++;
            openingEvents.add(new StatusEvent(Lang.t("HUISSIER : UNE CARTE DE PLUS"), EffectPopup.Style.ATTACK));
        }
        // Ses cartes propres se jouent en plus de ses cartes habituelles, une seule par tour (jusqu'à 3 au
        // chapitre 6) : elles ne prennent pas la place de ses couleurs, il n'est jamais moins fort qu'avant elles.
        List<Card> regular = drawn.stream().filter(card -> EnemySignature.of(card) == null).toList();
        played = new ArrayList<>(ownCards(drawn, enemy));
        boolean inked = enemy.getMaluses().takeInk(); // Encre : il joue à l'aveugle, ses cartes au hasard
        if (inked) openingEvents.add(new StatusEvent(Lang.t("ENCRE : IL JOUE À L'AVEUGLE"), EffectPopup.Style.DEFENSE));
        List<Card> chosen = new ArrayList<>(kind.playsAtRandom() || inked ? resolver.chooseAtRandom(regular, plays)
            : choose(regular, enemy.getHpRatio(), kind, plays));
        int grapples = enemy.getMaluses().takeGrapples(); // Grappin : sa meilleure carte choisie lui est volée
        for (int k = 0; k < grapples && !chosen.isEmpty(); k++) {
            Card best = chosen.stream().max(java.util.Comparator.comparingInt(EnemyTurnResolver::grappleValue)).orElseThrow();
            chosen.remove(best);
            openingEvents.add(new StatusEvent(Lang.f("GRAPPIN : {0} VOLÉE", best.getName().toUpperCase()), EffectPopup.Style.ATTACK));
        }
        played.addAll(chosen);
        hellBound = enemy.getMaluses().takeHellBound(); // Descente aux enfers : 2 cartes au plus, attaques -50 %
        if (hellBound && played.size() > EnemyMaluses.HELL_MAX_CARDS) {
            played = new ArrayList<>(played.subList(0, EnemyMaluses.HELL_MAX_CARDS));
        }
        if (hellBound) {
            openingEvents.add(new StatusEvent(Lang.f("ENFERS : L'ENNEMI NE JOUE QUE {0} CARTES, ATTAQUES -{1} %",
                EnemyMaluses.HELL_MAX_CARDS, EnemyMaluses.HELL_DAMAGE_PERCENT), EffectPopup.Style.DEFENSE));
        }

    }

    /**
     * Sa main est pleine : une carte propre qu'il ne jouera pas ce tour part dans sa défausse.
     *
     * @return {@code false} s'il n'en a aucune (toutes ses cartes servent)
     */
    boolean discardUselessOwnCard() {
        List<Card> kept = ownCards(drawn, enemy);
        for (int i = drawn.size() - 1; i >= 0; i--) {
            Card card = drawn.get(i);
            if (EnemySignature.of(card) != null && kept.stream().noneMatch(own -> own == card)) {
                enemy.discard(List.of(drawn.remove(i)));
                return true;
            }
        }
        return false;
    }

    /** Ce que valent les cartes jouées : cartes propres, Ombre, bonus de chaque couleur, chance. */
    void readPlayedCards() {
        // Ses cartes propres d'abord : celles du tour, celles qui attendent leur symbole, celles qui agissent tout de suite.
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

        // Mise royale : la Reine en phase 2 (sous la moitié de ses PV), ou le Prétendant tant qu'il porte l'éclat de la Reine, ou la Main de la Reine
        boolean royal = kind.royalBet() && enemy.getPhase() >= 2
            || kind.has(EnemyTrait.SHARDS) && EnemyKind.shards(enemy.getPhase()) >= 2
            || boosts.containsKey(EnemySignature.MAIN_DE_LA_REINE);
        int luckFactor = royal ? 2 : 1;
        for (EnemyCards.ShadowRole role : shadows) {
            if (role == EnemyCards.ShadowRole.BINGO) luck.merge(EnemySymbol.SWORD, EnemyCards.SHADOW_BINGO_LUCK, Integer::sum);
        }
        for (Card card : played) {
            if (EnemyCards.isForbiddenReel(card)) {
                // le Prétendant n'a plus le Rouleau interdit une fois l'éclat de l'Éclat Originel perdu
                forbidReel = !kind.has(EnemyTrait.SHARDS) || EnemyKind.shards(enemy.getPhase()) >= 3;
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
                    case TREFLE  -> luck.merge(resolver.luckyTarget(enemy), EnemyCards.luckBonus(card) * luckFactor, Integer::sum);
                }
            }
        }

        // Cartes marquées (Bluffeur) : ses cartes à couleur ne valent qu'une part de leur effet
        if (enemy.getMaluses().takeMarkedCards()) {
            int kept = 100 - enemy.getMaluses().malusShare(MARKED_CARDS_PERCENT);
            swordBonus  = swordBonus * kept / 100;
            healBonus   = healBonus * kept / 100;
            shieldBonus = shieldBonus * kept / 100;
            luck.replaceAll((symbol, bonus) -> bonus * kept / 100);
            openingEvents.add(new StatusEvent(Lang.f("CARTES MARQUÉES : SES CARTES -{0} %", 100 - kept), EffectPopup.Style.DEFENSE));
        }
    }

    /** Sa machine tourne : Fausse piste, Rien ne va plus, Zéro, Jackpot et Engrenages règlent tout le tour. */
    void spinReels() {
        // Fausse piste (Bluffeur) : un de ses rouleaux ne tourne pas (une chance sur deux pour un boss ou un roi)
        int reelCount = enemy.getReelCount();
        if (enemy.getMaluses().takeStun() && reelCount > 1) { // Gong (Temple du Destin) : un rouleau étourdi
            reelCount--;
            openingEvents.add(new StatusEvent(Lang.t("GONG : UN ROULEAU ÉTOURDI NE TOURNE PAS"), EffectPopup.Style.DEFENSE));
        }
        if (enemy.getMaluses().takeRedHerring()) {
            if (kind.isBoss() && random.nextBoolean()) {
                openingEvents.add(new StatusEvent(Lang.t("FAUSSE PISTE : IL NE S'Y LAISSE PAS PRENDRE"), EffectPopup.Style.SPECIAL));
            } else if (reelCount > 1) {
                reelCount--;
                openingEvents.add(new StatusEvent(Lang.t("FAUSSE PISTE : UN ROULEAU DE MOINS"), EffectPopup.Style.DEFENSE));
            }
        }
        symbols = machine.spin(enemy.getSpinWeights(), luck, reelCount);
        if (enemy.getMaluses().takeBingoLight()) { // Lumière du bingo : un rouleau sur deux s'illumine
            for (int i = 0; i < symbols.length; i++) if (random.nextBoolean()) symbols[i] = EnemySymbol.BINGO_LIGHT;
        }
        // Croupier en chef : son Rien ne va plus sort au plus une fois par tour, 5 % de chance
        // + 2 % par carte Annonce jouée ce tour (ses Trèfles ne le rendent pas plus probable)
        if (kind.rollsNothingGoesApart() && symbols.length > 0 && random.nextInt(100) < EnemyKind.CHEF_NOTHING_GOES_PERCENT
                + EnemyKind.CHEF_NOTHING_GOES_CARD_PERCENT * boosts.getOrDefault(EnemySignature.ANNONCE, 0)) {
            symbols[random.nextInt(symbols.length)] = EnemySymbol.NOTHING_GOES;
        }
        if (enemy.spendCoins()) { // Pièces avalées : le Bandit manchot s'offre un Jackpot
            Arrays.fill(symbols, EnemySymbol.SWORD);
            openingEvents.add(new StatusEvent(Lang.t("PIÈCES AVALÉES : JACKPOT !"), EffectPopup.Style.SPECIAL));
        }
        resolver.armedArms(enemy, symbols, luck, reelEvents);
        // Les roulettes du Zéro tournent d'abord : elles valent pour tout le tour.
        turn = new Turn(enemy, player, swordBonus);
        if (foreseen) turn.attackFactor *= EnemySymbol.PREDICTION_FACTOR;
        turn.attackFactor *= actFactor;
        int intimidated = enemy.getMaluses().takeIntimidation(); // Intimidation (Bluffeur) : ses attaques baissent
        if (intimidated > 0) {
            turn.malusPercent = 100 - intimidated;
            openingEvents.add(new StatusEvent(Lang.f("INTIMIDÉ : SES ATTAQUES -{0} %", intimidated), EffectPopup.Style.DEFENSE));
        }
        if (hellBound) turn.malusPercent = turn.malusPercent * (100 - EnemyMaluses.HELL_DAMAGE_PERCENT) / 100;
        if (omenMissed) turn.malusPercent = turn.malusPercent * (100 + PlaceRule.OMEN_ENEMY_PERCENT) / 100; // Présage manqué
        int shifted = enemy.getMaluses().takeNextAttackPercent(); // Provocation, Carte biseautée
        if (shifted != 100) {
            turn.malusPercent = turn.malusPercent * shifted / 100;
            openingEvents.add(new StatusEvent(shifted > 100 ? Lang.f("PROVOQUÉ : SES ATTAQUES +{0} %", shifted - 100)
                : Lang.f("CARTE BISEAUTÉE : SES ATTAQUES -{0} %", 100 - shifted),
                shifted > 100 ? EffectPopup.Style.DAMAGE : EffectPopup.Style.DEFENSE));
        }
        roulettes = new EnemyRouletteEvent[symbols.length];
        for (int i = 0; i < symbols.length; i++) {
            if (symbols[i] != EnemySymbol.ZERO) continue;
            // Bille truquée : la bille tombe sur le zéro
            roulettes[i] = new EnemyRouletteEvent(enemy.takeCharge(EnemySignature.BILLE_TRUQUEE)
                ? EnemyRouletteEvent.Pocket.ZERO : resolver.spinRoulette());
            if (roulettes[i].pocket != EnemyRouletteEvent.Pocket.NOIR) turn.attackFactor *= 2;
            if (roulettes[i].pocket != EnemyRouletteEvent.Pocket.ROUGE) turn.shieldFactor *= 2;
        }

        // Jackpot (Bandit manchot, Jackpot Vivant) : trois symboles identiques, tout le tour est multiplié.
        jackpot = kind.hitsJackpots() && symbols.length > 0
            && Arrays.stream(symbols).allMatch(symbol -> symbol == symbols[0]);
        boolean loadedCoin = enemy.getMaluses().takeLoadedCoin(); // Pièce truquée : pas de Jackpot ce tirage
        if (jackpot && loadedCoin) {
            jackpot = false;
            openingEvents.add(new StatusEvent(Lang.t("PIÈCE TRUQUÉE : PAS DE JACKPOT"), EffectPopup.Style.SPECIAL));
        }
        if (jackpot) {
            turn.attackFactor *= EnemySymbol.JACKPOT_FACTOR;
            turn.shieldFactor *= EnemySymbol.JACKPOT_FACTOR;
        }
        // Jackpot originel (Machine Originelle) : trois symboles pareils, et ce symbole agit cinq fois.
        originalJackpot = kind.has(EnemyTrait.ORIGINAL_JACKPOT) ? originalJackpot(symbols) : -1;
        // Engrenages : ses autres symboles sont plus forts ; un de plus par rouleau qu'elle a pris au joueur.
        int gears = (int) Arrays.stream(symbols).filter(symbol -> symbol == EnemySymbol.GEAR).count()
            * (1 + boosts.getOrDefault(EnemySignature.HUILE_NOIRE, 0));
        if (kind.has(EnemyTrait.REEL_THEFT)) {
            int taken = enemy.getStolenReels() + enemy.getAspired();
            if (taken > 0) {
                gears += taken;
                openingEvents.add(new StatusEvent(Lang.f("ROULEAUX PRIS : ENGRENAGES +{0}", taken), EffectPopup.Style.SPECIAL));
            }
        }
        turn.boostPercent = EnemySymbol.GEAR_PERCENT * gears;
        if (enemy.getFloor() > 0) { // Ascenseur : chaque étage renforce ses coups et ses Boucliers
            turn.boostPercent += EnemySymbol.ELEVATOR_STEP_PERCENT * enemy.getFloor();
            openingEvents.add(new StatusEvent(Lang.f("ÉTAGE {0} : SES COUPS +{1} %", enemy.getFloor(),
                EnemySymbol.ELEVATOR_STEP_PERCENT * enemy.getFloor()), EffectPopup.Style.ATTACK));
        }
        if (enemy.getStoreys() > 0) { // Plan : chaque étage de la Tour de l'Architecte lui donne un Bouclier
            if (kind.has(EnemyTrait.KEYSTONE) && enemy.getPhase() >= 3) { // Clé de voûte : en phase 3, sa Tour frappe aussi
                turn.boostPercent += EnemySymbol.KEYSTONE_STEP_PERCENT * enemy.getStoreys();
            }
            openingEvents.add(new StatusEvent(Lang.f("SA TOUR : {0} ÉTAGES", enemy.getStoreys()), EffectPopup.Style.DEFENSE));
            openingEvents.add(turn.shield(kind.shieldDefense() * enemy.getStoreys()));
        }
        goAllOut();
        troopsFire();

    }

    /** Chaque symbole agit, dans l'ordre des rouleaux. */
    void actSymbols() {
        curses = player.getLastingEffects();
        // Queue de comète : son meilleur symbole du tour agit deux fois
        best = boosts.containsKey(EnemySignature.QUEUE_DE_COMETE) ? bestSymbol(symbols) : -1;
        healFactor = 100 + healBonus; // les Cœurs renforcent aussi ce que rendent ses Crocs et ses Morsures
        for (int i = 0; i < symbols.length; i++) outcomes.add(actReel(i));
    }

    /** @return ce que fait le rouleau {@code i} : parades, puis son symbole, autant de fois qu'il agit. */
    List<Event> actReel(int i) {
        List<Event> events = new ArrayList<>();
        if (jackpot && i == 0) {
            events.add(new StatusEvent(Lang.f("JACKPOT ! TOUT x{0}", EnemySymbol.JACKPOT_FACTOR), EffectPopup.Style.SPECIAL));
        }
        Event parried = parry(symbols[i], player);
        if (parried != null) { // Piège à rats, Cage à requin : le mauvais sort tombe à l'eau
            events.add(parried);
            return events;
        }
        if (symbols[i].isSpecial() && enemy.getMaluses().getSleights() > 0) { // Tour de passe-passe (Bluffeur)
            enemy.getMaluses().takeSleight();
            if (!kind.isBoss() || random.nextBoolean()) {
                events.add(new StatusEvent(Lang.f("TOUR DE PASSE-PASSE : {0} ANNULÉ", symbols[i].getDisplayName()),
                    EffectPopup.Style.DEFENSE));
                return events;
            }
            events.add(new StatusEvent(Lang.t("TOUR DE PASSE-PASSE RATÉ"), EffectPopup.Style.SPECIAL));
        }
        events.addAll(reelEvents.getOrDefault(i, List.of()));
        int repeats = i == best ? 1 + boosts.get(EnemySignature.QUEUE_DE_COMETE) : 1;
        if (repeats > 1) events.add(new StatusEvent(Lang.t("QUEUE DE COMÈTE : IL AGIT DEUX FOIS"), EffectPopup.Style.SPECIAL));
        if (i == originalJackpot) {
            EnemySymbol triple = symbols[i];
            int same = (int) Arrays.stream(symbols).filter(symbol -> symbol == triple).count();
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
        for (int r = 0; r < repeats; r++) act(symbols[i], i, events);
        return events;
    }

    /** Le symbole {@code symbol} du rouleau {@code i} agit une fois. */
    void act(EnemySymbol symbol, int i, List<Event> events) {
        boolean done = actTowerOpening(symbol, i, events) || actTowerLate(symbol, events)
            || actPortMinesCasino(symbol, events) || actFairDesert(symbol, events) || actExpress(symbol, events)
            || actPalace(symbol, events) || actTemple(symbol, events) || actManor(symbol, events)
            || actAscension(symbol, events)
            || actScatteredShards(symbol, events)
            || actPilgrims(symbol, events)
            || actWar(symbol, events);
        if (!done) throw new IllegalStateException("Symbole sans effet : " + symbol);
    }

    /** Fin de son tour : cartes qui agissent tout de suite, Ombre, renvois de dégâts, Rouleau interdit. */
    List<Event> finish(Reflection reflection) {
        List<Event> afterEvents = new ArrayList<>();
        playNow(now, turn, curses, afterEvents);
        playShadows(shadows, turn, afterEvents);
        if (turn.totalAttack > 0 && reflection.shieldReflect() > 0) {
            // Bingo de bouclier : il frappe dans le bouclier, qui lui est renvoyé entier.
            afterEvents.add(new DamageReflectedEvent(enemy.skinned(reflection.shieldReflect())));
            enemy.takeDamage(reflection.shieldReflect());
        }
        // Le Coffre arme le renvoi : une part de son contenu s'ajoute aux dégâts renvoyés.
        float vaultReflect = player.getLastingEffects().getVault() * reflection.vaultShare();
        if (turn.totalAttack > 0 && reflection.percent() > 0 && !enemy.isDefeated()) {
            float reflectBase = turn.totalAttack + vaultReflect;
            int reflected = Math.round(reflectBase * (reflection.percent() / 100f));
            if (reflected > 0) {
                afterEvents.add(new DamageReflectedEvent(enemy.skinned(reflected)));
                enemy.takeDamage(reflected);
            }
        }
        if (turn.totalAttack > 0 && reflection.attackPercent() > 0 && !enemy.isDefeated()) {
            // Cartes Carreau, une riposte (Astra 2026-11-06 : plus rien sur ses PV max) : une part de ses coups et
            // du bonus d'attaque du rang, plus la part du Coffre, multipliées comme l'attaque du joueur ce tour.
            double base = (turn.totalAttack + reflection.riposteBonus()) * (reflection.attackPercent() / 100d) + vaultReflect;
            long reflected = enemy.capHit(Math.round(base * reflection.riposteFactor()));
            if (reflected > 0) {
                afterEvents.add(new DamageReflectedEvent(enemy.skinned(reflected)));
                enemy.takeDamage(reflected);
            }
        }
        if (turn.totalAttack > 0 && reflection.hpPercent() > 0 && !enemy.isDefeated()) {
            // Descente aux enfers : une part de ses PV max (la part du Coffre compte une fois en tout).
            long reflected = Math.round(enemy.getMaxHp() * (reflection.hpPercent() / 100d)
                + (reflection.attackPercent() > 0 ? 0 : vaultReflect));
            if (reflected > 0) {
                afterEvents.add(new DamageReflectedEvent(enemy.skinned(reflected)));
                enemy.takeDamage(reflected);
            }
        }

        if (enemy.takeAllOut() && !enemy.isDefeated()) { // Va-tout : il a tout misé, plus de défense jusqu'à son tour
            enemy.bribe();
            afterEvents.add(new StatusEvent(Lang.t("VA-TOUT : SA DÉFENSE TOMBE À 0"), EffectPopup.Style.DEFENSE));
        }

        if (forbidReel) {
            int reel = random.nextInt(SlotMachine.SYMBOL_COUNT);
            player.getLastingEffects().forbidReel(reel);
            afterEvents.add(new ReelForbiddenEvent(reel));
        }

        return afterEvents;
    }
}
