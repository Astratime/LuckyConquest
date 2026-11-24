package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.BigNumber;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.EnemyMaluses;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.SlotMachine;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.enemy.EnemySignature;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.events.EnemyHpLostEvent;
import fr.astratime.lucky.entities.events.EnemyHealedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.ReelForbiddenEvent;
import fr.astratime.lucky.entities.events.StatusEvent;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import java.util.ArrayList;
import java.util.List;

import static fr.astratime.lucky.controllers.EnemyTurnResolver.*;

/** Ce que font les symboles des ennemis de l'Exploration, lieu par lieu (voir {@link EnemyTurnResolver}). */
abstract class EnemyPlaceSymbols extends EnemyTowerSymbols {
    EnemyPlaceSymbols(EnemyTurnResolver resolver, Enemy enemy, Player player) {
        super(resolver, enemy, player);
    }

    /**
     * Exploration : Port des Contrebandiers, Mines d'Or, Casino Englouti.
     *
     * @return {@code false} si {@code symbol} n'est pas de cette famille
     */
    boolean actPortMinesCasino(EnemySymbol symbol, List<Event> events) {
        switch (symbol) {
            case NIBBLE -> curse(events, EnemySignature.DENTS_DE_RAT, times(curses::addNibble),
                Lang.t("GRIGNOTAGE : UNE CARTE RONGÉE AU PROCHAIN TOUR"), Lang.t("GRIGNOTAGE : DEUX CARTES RONGÉES AU PROCHAIN TOUR"));
            case DRUNK -> curse(events, EnemySignature.TOURNEE, times(curses::addDrunk),
                Lang.t("IVRESSE : UN ROULEAU TITUBERA"), Lang.t("IVRESSE : DEUX ROULEAUX TITUBERONT"));
            case BLIND -> curse(events, EnemySignature.LANTERNE, curses::blind,
                Lang.t("AVEUGLEMENT : UNE CARTE CACHÉE AU PROCHAIN TOUR"), Lang.t("AVEUGLEMENT : DEUX CARTES CACHÉES AU PROCHAIN TOUR"));
            case BOARDING -> board(turn, events, enemy.takeCharge(EnemySignature.GRAPPIN));
            case NUGGET -> curse(events, EnemySignature.TAMIS_NOIR, curses::addNugget,
                Lang.t("PÉPITE : TES GAINS SERONT DES PIERRES"), Lang.t("PÉPITE : TES GAINS SERONT DES PIERRES 2 TIRAGES"));
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
                events.add(new StatusEvent(Lang.f("ENCLUME : ATTAQUE +{0}", kind.hurt(enemy.getAnvil(), enemy.getDifficulty())),
                    EffectPopup.Style.ATTACK));
            }
            case SONG -> curse(events, EnemySignature.REFRAIN, times(curses::addSong),
                Lang.t("CHANT : UNE CARTE JOUÉE D'OFFICE"), Lang.t("CHANT : DEUX CARTES JOUÉES D'OFFICE"));
            case BANK_BITE -> {
                double stolen = BigNumber.percent(player.getGains(), Math.min(100, kind.bankBitePercent(enemy.getDifficulty())
                    + EnemySignature.FRENESIE_PERCENT * boosts.getOrDefault(EnemySignature.FRENESIE, 0)));
                if (steal(stolen, events)) {
                    long heal = Math.min((long) BigNumber.toInt(stolen) * healFactor / 100,
                        (long) enemy.getMaxHp() * EnemySymbol.BANK_BITE_HEAL_MAX / 100);
                    events.add(new EnemyHealedEvent(enemy.heal(heal)));
                } else {
                    events.add(turn.strike(EnemySymbol.FANG_DAMAGE)); // rien à dévorer : il mord
                }
            }
            case PLATING -> {
                int turns = EnemySymbol.PLATING_TURNS * (enemy.takeCharge(EnemySignature.OR_MASSIF) ? 2 : 1);
                enemy.plate(turns);
                events.add(new StatusEvent(Lang.n(turns, "PLACAGE : PEAU D'OR 1 TOUR",
                    "PLACAGE : PEAU D'OR {0} TOURS", turns), EffectPopup.Style.DEFENSE));
            }
            case INGOT -> events.add(turn.strike(kind.swordDamage() * ingotPercent(enemy)
                / 100 * (100 + EnemySignature.LINGOT_PERCENT * boosts.getOrDefault(EnemySignature.POIDS_DE_L_OR, 0)) / 100));
            case ROCKFALL -> {
                events.add(turn.strike(kind.swordDamage()));
                curse(events, EnemySignature.GRONDEMENT, curses::addRockfall,
                    Lang.t("ÉBOULEMENT : 1 CARTE DE MOINS"), Lang.t("ÉBOULEMENT : 2 CARTES DE MOINS"));
            }
            case HEARTBEAT -> {
                int beats = enemy.takeCharge(EnemySignature.FILON) ? 2 : 1;
                for (int k = 0; k < beats; k++) events.add(heal(enemy, EnemySymbol.HEARTBEAT_PERCENT * healFactor / 100f));
                enemy.plate(EnemySymbol.PLATING_TURNS);
                events.add(new StatusEvent(Lang.t("BATTEMENT : SA PEAU D'OR REVIENT"), EffectPopup.Style.DEFENSE));
            }
            case SWALLOWED_COIN -> {
                steal(BigNumber.percent(player.getGains(), EnemySymbol.COIN_PERCENT), events);
                int coins = enemy.swallowCoins(enemy.takeCharge(EnemySignature.MONNAIE) ? 2 : 1);
                events.add(new StatusEvent(Lang.f("PIÈCE AVALÉE : {0}/{1}", Math.min(coins, EnemySymbol.COINS_FOR_JACKPOT),
                    EnemySymbol.COINS_FOR_JACKPOT), EffectPopup.Style.SPECIAL));
            }
            case ARMED_ARM -> { } // son effet a joué avant les autres : il a relancé son pire rouleau
            default -> {
                return false;
            }
        }
        return true;
    }

    /**
     * Exploration : Fête foraine et Désert.
     *
     * @return {@code false} si {@code symbol} n'est pas de cette famille
     */
    boolean actFairDesert(EnemySymbol symbol, List<Event> events) {
        switch (symbol) {
            case JUGGLE -> {
                int cards = EnemySymbol.JUGGLE_CARDS * (enemy.takeCharge(EnemySignature.MASSUES) ? 2 : 1);
                curses.addJuggle(cards);
                events.add(new StatusEvent(Lang.f("JONGLAGE : {0} CARTES REPARTIRONT", cards), EffectPopup.Style.DAMAGE));
            }
            case ACT -> announceAct(events);
            case SCARE -> {
                int count = enemy.takeCharge(EnemySignature.BOUH) ? 2 : 1;
                curses.addScare(count, EnemySymbol.SCARE_MAX);
                events.add(new StatusEvent(Lang.f("FRAYEUR : ATTAQUE -{0} % AU PROCHAIN TIRAGE",
                    EnemySymbol.SCARE_PERCENT * curses.getScares()), EffectPopup.Style.DAMAGE));
            }
            case TUNNEL -> curse(events, EnemySignature.TUNNEL_SANS_FIN, curses::addTunnel,
                Lang.t("TUNNEL : UN ROULEAU DANS LE NOIR"), Lang.t("TUNNEL : 2 ROULEAUX DANS LE NOIR"));
            case TURNSTILE -> curse(events, EnemySignature.TOUR_DE_MANIVELLE, curses::addTurnstile,
                Lang.t("TOURNIQUET : TON MEILLEUR ROULEAU SERA RELANCÉ"), Lang.t("TOURNIQUET : 2 ROULEAUX RELANCÉS"));
            case BIG_WHEEL -> bigWheel(events);
            case RIGGED_PRIZE -> curse(events, EnemySignature.LOT_DE_CONSOLATION, curses::addRiggedPrize,
                Lang.t("LOT TRUQUÉ : TES PAIRES NE PAIENT PAS"), Lang.t("LOT TRUQUÉ : TES PAIRES NE PAIENT PAS 2 TIRAGES"));
            case RIGGED_GAME -> {
                int divisor = enemy.takeCharge(EnemySignature.CARTE_BISEAUTEE) ? 3 : EnemySymbol.RIGGED_GAME_DIVISOR;
                curses.addRiggedGame(divisor);
                events.add(new StatusEvent(Lang.f("JEU TRUQUÉ : SANS PAIRE, DÉGÂTS /{0}", divisor), EffectPopup.Style.DAMAGE));
            }
            case TOLL -> {
                int spins = EnemySymbol.TOLL_SPINS * (enemy.takeCharge(EnemySignature.OCTROI) ? 3 : 1);
                curses.addToll(spins);
                events.add(new StatusEvent(Lang.n(spins, "PÉAGE : UN TIRAGE DE PLUS À PAYER",
                    "PÉAGE : {0} TIRAGES DE PLUS À PAYER", spins), EffectPopup.Style.DAMAGE));
            }
            case BILL -> {
                int added = curses.addBills(EnemySymbol.BILL_CLAIMS
                    + EnemySymbol.BILL_CLAIMS * boosts.getOrDefault(EnemySignature.USURE, 0), EnemySymbol.BILL_MAX);
                events.add(new StatusEvent(Lang.f("LETTRE DE CHANGE : {0} CRÉANCES SUR TOI", curses.getBills()),
                    added > 0 ? EffectPopup.Style.DAMAGE : EffectPopup.Style.SPECIAL));
            }
            case WRAP -> {
                int count = enemy.takeCharge(EnemySignature.MOMIFICATION) ? 2 : 1;
                List<String> wrapped = confiscate(count, EnemySymbol.WRAP_TURNS);
                events.add(wrapped.isEmpty() ? turn.strike(EnemySymbol.FANG_DAMAGE)
                    : new StatusEvent(Lang.f("BANDELETTES : {0} LIGOTÉE", String.join(", ", wrapped)), EffectPopup.Style.DAMAGE));
            }
            case VENOM -> {
                int turns = EnemySymbol.VENOM_TURNS + (enemy.takeCharge(EnemySignature.DARD) ? 2 : 0);
                curses.addVenom(turns, EnemySymbol.VENOM_MAX);
                events.add(new StatusEvent(Lang.f("VENIN : -{0} % DE PV PAR TOUR, {1} TOURS", EnemySymbol.VENOM_PERCENT, turns),
                    EffectPopup.Style.DAMAGE));
            }
            case MIRAGE -> {
                int count = 1 + (enemy.takeCharge(EnemySignature.FATA_MORGANA) ? 1 : 0);
                events.add(new StatusEvent(Lang.t("MIRAGE !"), EffectPopup.Style.DAMAGE));
                for (int k = 0; k < count; k++) {
                    int reel = resolver.freeReel(curses.getForbiddenReels());
                    if (reel < 0) break;
                    curses.voidReel(reel);
                    events.add(new ReelForbiddenEvent(reel));
                }
            }
            case BURN -> {
                int percent = boosts.containsKey(EnemySignature.COULEE_DE_LAVE) ? 100 : EnemySymbol.BURN_PERCENT;
                int melted = player.meltShield(percent);
                events.add(melted > 0 ? new StatusEvent(Lang.f("BRÛLURE : -{0} BOUCLIER", Lang.big(melted)), EffectPopup.Style.DAMAGE)
                    : turn.strike(EnemySymbol.FANG_DAMAGE)); // pas de bouclier à faire fondre : il brûle
            }
            case BINGO_LIGHT -> { // Lumière du bingo du joueur : le rouleau ne fait rien, il perd des PV
                long lost = enemy.takeDamage(Math.max(1L, enemy.getMaxHp() * EnemyMaluses.LIGHT_HP_PERCENT / 100));
                events.add(new EnemyHpLostEvent(Lang.f("LUMIÈRE DU BINGO : IL PERD {0} PV", Lang.big(lost)),
                    EffectPopup.Style.SPECIAL, lost));
            }
            case FIRE_BREATH -> {
                events.add(new StatusEvent(Lang.t("SOUFFLE DE FEU !"), EffectPopup.Style.ATTACK));
                events.add(turn.strike(kind.swordDamage(), true));
                int burnt = enemy.takeCharge(EnemySignature.BRAISE) ? 2 : 1;
                curses.addRockfall(burnt);
                events.add(new StatusEvent(Lang.n(burnt, "CARTE BRÛLÉE : 1 DE MOINS",
                    "CARTES BRÛLÉES : {0} DE MOINS", burnt), EffectPopup.Style.DAMAGE));
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    /**
     * Exploration : le Rapide de Minuit (Pression, Vol à la tire, Banquet, Berceuse, Braquage).
     *
     * @return {@code false} si {@code symbol} n'est pas de cette famille
     */
    boolean actExpress(EnemySymbol symbol, List<Event> events) {
        switch (symbol) {
            case PRESSURE -> {
                boolean exploded = enemy.tickPressure();
                events.add(new StatusEvent(Lang.f("PRESSION {0}/{1}", exploded ? EnemySymbol.PRESSURE_MAX : enemy.getPressure(),
                    EnemySymbol.PRESSURE_MAX), exploded ? EffectPopup.Style.ATTACK : EffectPopup.Style.SPECIAL));
                if (exploded) {
                    int factor = EnemySymbol.PRESSURE_FACTOR * (enemy.takeCharge(EnemySignature.PLEIN_REGIME) ? 2 : 1);
                    events.add(new StatusEvent(Lang.t("LA CHAUDIÈRE EXPLOSE !"), EffectPopup.Style.ATTACK));
                    events.add(turn.strike(kind.swordDamage() * factor));
                }
            }
            case PICKPOCKET -> {
                int count = enemy.takeCharge(EnemySignature.DOIGTS_AGILES) ? 2 : 1;
                List<String> taken = new ArrayList<>();
                for (int k = 0; k < count; k++) {
                    List<Card> hand = player.getCurrentHand();
                    if (hand.isEmpty()) break;
                    Card card = hand.get(random.nextInt(hand.size()));
                    if (player.steal(card, EnemySymbol.PICKPOCKET_TURNS)) taken.add(card.getName().toUpperCase());
                }
                events.add(taken.isEmpty() ? turn.strike(EnemySymbol.FANG_DAMAGE) // poches vides : il frappe
                    : new StatusEvent(Lang.f("VOL À LA TIRE : {0}", String.join(", ", taken)), EffectPopup.Style.DAMAGE));
            }
            case BANQUET -> {
                events.add(heal(enemy, EnemySymbol.BANQUET_HEAL_PERCENT * healFactor / 100f));
                int shields = 1 + boosts.getOrDefault(EnemySignature.DESSERT, 0);
                for (int k = 0; k < shields; k++) events.add(turn.shield(kind.shieldDefense() + shieldBonus));
            }
            case LULLABY -> {
                int cards = enemy.takeCharge(EnemySignature.MARCHAND_DE_SABLE) ? 2 : 1;
                curses.addLullaby(cards);
                events.add(new StatusEvent(Lang.n(cards, "BERCEUSE : TA PREMIÈRE CARTE S'ENDORMIRA",
                    "BERCEUSE : TES {0} PREMIÈRES CARTES S'ENDORMIRONT", cards), EffectPopup.Style.DAMAGE));
            }
            case HOLDUP -> {
                boolean forced = enemy.takeCharge(EnemySignature.HAUT_LES_MAINS);
                boolean scared = enemy.getDamageTaken() * 100 < enemy.getMaxHp() * EnemySymbol.HOLDUP_PERCENT;
                int taken = forced || scared ? player.meltShield(100) : 0;
                if (taken > 0) {
                    events.add(new StatusEvent(Lang.f("BRAQUAGE : IL TE PREND {0} DE BOUCLIER", Lang.big(taken)),
                        EffectPopup.Style.DAMAGE));
                    events.add(turn.shield((int) Math.min(taken, (long) kind.shieldDefense() * EnemySymbol.HOLDUP_MAX_SHIELDS)));
                } else {
                    events.add(turn.strike(kind.swordDamage())); // rien à prendre, ou il a eu peur : il tire
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    /**
     * Exploration : le Palace des Neiges (Clé, Gel, Avalanche, Champagne glacé).
     *
     * @return {@code false} si {@code symbol} n'est pas de cette famille
     */
    boolean actPalace(EnemySymbol symbol, List<Event> events) {
        switch (symbol) {
            case KEY -> {
                int count = enemy.takeCharge(EnemySignature.TROUSSEAU) ? 2 : 1;
                List<String> locked = new ArrayList<>();
                for (int k = 0; k < count; k++) {
                    Card best = null;
                    for (Card card : player.getCurrentHand()) if (best == null || keyStrength(card) > keyStrength(best)) best = card;
                    if (best == null) break;
                    if (player.steal(best, EnemySymbol.KEY_TURNS)) locked.add(best.getName().toUpperCase());
                }
                events.add(locked.isEmpty() ? turn.strike(kind.swordDamage()) // main vide : il frappe
                    : new StatusEvent(Lang.f("CLÉ : {0} FERMÉE À CLÉ", String.join(", ", locked)), EffectPopup.Style.DAMAGE));
            }
            case FREEZE -> {
                int count = enemy.takeCharge(EnemySignature.GIVRE_ETERNEL) ? 2 : 1;
                LastingEffects lasting = player.getLastingEffects();
                List<Integer> free = new ArrayList<>();
                for (int r = 0; r < SlotMachine.SYMBOL_COUNT; r++) if (!lasting.getFrozenReels().contains(r)) free.add(r);
                List<String> reels = new ArrayList<>();
                for (int k = 0; k < count && !free.isEmpty(); k++) {
                    int reel = free.remove(random.nextInt(free.size()));
                    lasting.freeze(reel, EnemySymbol.FREEZE_TURNS);
                    reels.add(String.valueOf(reel + 1));
                }
                events.add(new StatusEvent(Lang.f("GEL : ROULEAU {0} GELÉ", String.join(", ", reels)), EffectPopup.Style.DAMAGE));
            }
            case AVALANCHE -> {
                int percent = 100 + EnemySymbol.AVALANCHE_STEP_PERCENT * enemy.getSnow();
                if (enemy.takeCharge(EnemySignature.TEMPETE)) percent *= 2;
                if (enemy.getSnow() > 0) events.add(new StatusEvent(Lang.f("AVALANCHE : +{0} %", percent - 100),
                    EffectPopup.Style.ATTACK));
                events.add(turn.strike((int) Math.min(Integer.MAX_VALUE, (long) kind.swordDamage() * percent / 100)));
                enemy.addSnow();
            }
            case CHAMPAGNE -> {
                int glasses = 1 + boosts.getOrDefault(EnemySignature.GRAND_CRU, 0);
                events.add(heal(enemy, EnemySymbol.CHAMPAGNE_HEAL_PERCENT * glasses * healFactor / 100f));
                events.add(turn.shield(kind.shieldDefense() + shieldBonus));
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    /**
     * Exploration : le Temple du Destin (Glas, Fil du destin, Double six, Karma).
     *
     * @return {@code false} si {@code symbol} n'est pas de cette famille
     */
    boolean actTemple(EnemySymbol symbol, List<Event> events) {
        switch (symbol) {
            case KNELL -> {
                int reels = EnemySymbol.KNELL_REELS * (enemy.takeCharge(EnemySignature.CARILLON) ? 2 : 1);
                int blocked = 0;
                for (int k = 0; k < reels; k++) {
                    List<Integer> free = new ArrayList<>();
                    for (int reel = 0; reel < SlotMachine.SYMBOL_COUNT; reel++) {
                        if (!curses.getForbiddenReels().contains(reel)) free.add(reel);
                    }
                    if (free.size() <= 1) break; // il laisse toujours un rouleau au joueur
                    curses.voidReel(free.get(random.nextInt(free.size())));
                    blocked++;
                }
                events.add(blocked == 0 ? turn.strike(kind.swordDamage())
                    : new StatusEvent(Lang.n(blocked, "GLAS : UN DE TES ROULEAUX BLOQUÉ", "GLAS : {0} DE TES ROULEAUX BLOQUÉS",
                        blocked), EffectPopup.Style.DAMAGE));
                for (int k = 0; k < boosts.getOrDefault(EnemySignature.TOCSIN, 0); k++) events.add(turn.strike(kind.swordDamage()));
            }
            case FATE_THREAD -> {
                Symbol chosen = curses.getRiggedTurns() > 0 ? null : rigSymbol(player.getSlotMachine().getReels());
                if (chosen == null) {
                    events.add(turn.strike(kind.swordDamage())); // déjà lié : il frappe
                } else {
                    int reel = random.nextInt(SlotMachine.SYMBOL_COUNT);
                    curses.rig(reel, chosen, EnemySymbol.FATE_THREAD_TURNS);
                    events.add(new StatusEvent(Lang.f("FIL DU DESTIN : ROULEAU {0} = {1}", reel + 1, chosen.getDisplayName()),
                        EffectPopup.Style.DAMAGE));
                }
                if (enemy.takeCharge(EnemySignature.CISEAUX_DU_DESTIN)) {
                    int cut = player.meltShield(100);
                    if (cut > 0) events.add(new StatusEvent(Lang.f("CISEAUX : {0} DE BOUCLIER COUPÉ", Lang.big(cut)),
                        EffectPopup.Style.DAMAGE));
                }
                for (int k = 0; k < boosts.getOrDefault(EnemySignature.QUENOUILLE, 0); k++) {
                    events.add(heal(enemy, EnemySignature.QUENOUILLE_PERCENT * healFactor / 100f));
                }
            }
            case DOUBLE_SIX -> {
                int first = 1 + random.nextInt(EnemySymbol.DIE_FACES), second = 1 + random.nextInt(EnemySymbol.DIE_FACES);
                if (enemy.takeCharge(EnemySignature.COUP_DE_DES)) second = first;
                if (first == second) {
                    int factor = boosts.containsKey(EnemySignature.QUITTE_OU_DOUBLE) ? EnemySymbol.DOUBLE_FACTOR + 1
                        : EnemySymbol.DOUBLE_FACTOR;
                    turn.attackFactor *= factor;
                    events.add(new StatusEvent(Lang.f("DOUBLE {0} ! ATTAQUES x{1}", first, factor), EffectPopup.Style.ATTACK));
                } else {
                    events.add(new StatusEvent(Lang.f("DÉS : {0} ET {1}", first, second), EffectPopup.Style.SPECIAL));
                }
                events.add(turn.strike(kind.swordDamage()));
            }
            case KARMA -> {
                int bingos = enemy.takeKarma();
                int factor = EnemySymbol.KARMA_FACTOR * (enemy.takeCharge(EnemySignature.JUGEMENT) ? 2 : 1);
                if (bingos == 0) {
                    events.add(new StatusEvent(Lang.t("KARMA : RIEN À RENDRE"), EffectPopup.Style.SPECIAL));
                    events.add(turn.shield(kind.shieldDefense() + shieldBonus)); // pas de Bingo : il se protège
                } else {
                    events.add(new StatusEvent(Lang.n(bingos, "KARMA : TON BINGO TE REVIENT", "KARMA : TES {0} BINGOS TE REVIENNENT",
                        bingos), EffectPopup.Style.ATTACK));
                    events.add(turn.strike(kind.swordDamage() * factor * bingos));
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    /**
     * Exploration : le Manoir hanté (Pacte de sang, Malédiction, Linceul, Fils).
     *
     * @return {@code false} si {@code symbol} n'est pas de cette famille
     */
    boolean actManor(EnemySymbol symbol, List<Event> events) {
        switch (symbol) {
            case BLOOD_PACT -> {
                int percent = EnemySymbol.BLOOD_PACT_PERCENT * (enemy.takeCharge(EnemySignature.PACTE_SCELLE) ? 2 : 1);
                int taken = player.getLastingEffects().takeBlood(percent);
                if (taken > 0) {
                    events.add(new StatusEvent(Lang.f("PACTE DE SANG : IL TE PREND {0} DE SANG", Lang.big(taken)),
                        EffectPopup.Style.DAMAGE));
                    int drinks = 1 + boosts.getOrDefault(EnemySignature.DOUZE_COUPS, 0);
                    events.add(heal(enemy, EnemySymbol.BLOOD_PACT_HEAL_PERCENT * drinks * healFactor / 100f));
                } else {
                    events.add(turn.strike(kind.swordDamage())); // pas de Sang à boire : il frappe
                }
            }
            case CURSE -> curse(events, EnemySignature.PAGE_ARRACHEE, curses::addMalediction,
                Lang.t("MALÉDICTION : TA MEILLEURE CARTE RETOURNERA SOUS TA PIOCHE"),
                Lang.t("MALÉDICTION : TES 2 MEILLEURES CARTES RETOURNERONT SOUS TA PIOCHE"));
            case SHROUD -> {
                if (enemy.armShroud()) {
                    events.add(new StatusEvent(Lang.f("LINCEUL : IL SE RELÈVERA À {0} % DE SES PV", EnemySymbol.SHROUD_RISE_PERCENT),
                        EffectPopup.Style.SPECIAL));
                } else {
                    events.add(turn.shield(kind.shieldDefense() + shieldBonus)); // déjà drapé, ou déjà relevé : il se protège
                }
            }
            case STRINGS -> curse(events, EnemySignature.FIL_DE_SOIE, curses::addStrings,
                Lang.t("FILS : IL CHOISIRA UNE CARTE QUE TU JOUERAS"), Lang.t("FILS : IL CHOISIRA 2 CARTES QUE TU JOUERAS"));
            default -> {
                return false;
            }
        }
        return true;
    }
}
