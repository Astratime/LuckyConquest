package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.BigNumber;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.enemy.EnemySignature;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.events.EnemyHourglassEvent;
import fr.astratime.lucky.entities.events.EnemyStakeEvent;
import fr.astratime.lucky.entities.events.GainsStolenEvent;
import fr.astratime.lucky.entities.events.GaugesDrainedEvent;
import fr.astratime.lucky.entities.events.EnemyThornsEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.ReelForbiddenEvent;
import fr.astratime.lucky.entities.events.StatusEvent;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import java.util.ArrayList;
import java.util.List;

import static fr.astratime.lucky.controllers.EnemyTurnResolver.*;

/** Ce que font les symboles des ennemis de l'Histoire, chapitre par chapitre (voir {@link EnemyTurnResolver}). */
abstract class EnemyTowerSymbols extends EnemyTurnState {
    EnemyTowerSymbols(EnemyTurnResolver resolver, Enemy enemy, Player player) {
        super(resolver, enemy, player);
    }

    /**
     * Chapitres 1 à 3 de la Tour : Épée, Bouclier, Potion, Épines, Croc, Rage, Dé pipé, Intérêts, Zéro, Reflet, Sablier, Tapis.
     *
     * @return {@code false} si {@code symbol} n'est pas de cette famille
     */
    boolean actTowerOpening(EnemySymbol symbol, int i, List<Event> events) {
        switch (symbol) {
            case SWORD  -> events.add(turn.strike(enemy.getKind().swordDamage()));
            case FANG   -> bite(turn, events,
                healFactor * (100 + EnemySignature.SAIGNEE_PERCENT * boosts.getOrDefault(EnemySignature.SAIGNEE, 0)) / 100);
            case THORNS -> {
                int thorns = kind.thornsPercent(enemy.getDifficulty()) + EnemySignature.RONCES_PERCENT * boosts.getOrDefault(EnemySignature.RONCES, 0);
                enemy.addThorns(thorns);
                events.add(new EnemyThornsEvent(thorns, enemy.getThornsPercent()));
            }
            case RAGE   -> {
                for (int k = 0; k <= boosts.getOrDefault(EnemySignature.PROVOCATION, 0); k++) events.add(rage(enemy));
            }
            case SHIELD -> events.add(turn.shield(kind.shieldDefense() + shieldBonus));
            case POTION -> {
                events.add(heal(enemy, kind.potionPercent(healBonus, enemy.getDifficulty())));
                if (kind.potionShields()) events.add(turn.shield(kind.shieldDefense() + shieldBonus));
            }
            case LOADED_DIE -> {
                int percent = Math.min(100, kind.diePercent(enemy.getDifficulty())
                    + EnemySignature.DE_CHARGE_PERCENT * boosts.getOrDefault(EnemySignature.DE_CHARGE, 0));
                int drained = player.getLastingEffects().drainGauges(percent);
                events.add(new GaugesDrainedEvent(percent, drained));
            }
            case INTEREST -> {
                double stolen = BigNumber.percent(player.getGains(), Math.min(100, kind.interestPercent(enemy.getDifficulty())
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
                if (exploded) events.add(turn.strike(EnemySymbol.HOURGLASS_DAMAGE, true)); // traverse le bouclier
            }
            case ALL_IN -> {
                enemy.goAllIn();
                if (enemy.takeCharge(EnemySignature.TOUT_SUR_LE_ROUGE)) enemy.holdAllIn();
                events.add(new EnemyStakeEvent(enemy.getStake(), true));
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    /**
     * Chapitres 4 à 6 de la Tour : du Pilleur à la Machine Originelle.
     *
     * @return {@code false} si {@code symbol} n'est pas de cette famille
     */
    boolean actTowerLate(EnemySymbol symbol, List<Event> events) {
        switch (symbol) {
            case FAKE_MONEY -> fakeMoney(events);
            case PREDICTION -> predict(events);
            case DUEL   -> resolver.duel(turn, events, enemy.takeCharge(EnemySignature.BOTTE_SECRETE) ? EnemySignature.BOTTE_RANKS : 0);
            case TAX -> curse(events, EnemySignature.REDRESSEMENT, curses::addTax,
                Lang.t("TAXE SUR TES PROCHAINES CARTES"), Lang.t("TAXE SUR TES CARTES DES 2 PROCHAINS TOURS"));
            case NEW_RULE -> {
                LastingEffects.HouseRule[] rules = LastingEffects.HouseRule.values();
                LastingEffects.HouseRule rule = rules[random.nextInt(rules.length)];
                curses.setHouseRule(rule, EnemySymbol.NEW_RULE_TURNS
                    + (enemy.takeCharge(EnemySignature.REGLEMENT_INTERIEUR) ? 1 : 0));
                events.add(new StatusEvent(Lang.f("NOUVELLE RÈGLE : {0}", rule.getAnnounce()), EffectPopup.Style.SPECIAL));
            }
            case FRISK -> frisk(events);
            case BANKRUPTCY -> bankruptcy(events);
            case NOTHING_GOES -> { // un seul rouleau bloqué (nerf d'Astra)
                events.add(new StatusEvent(Lang.t("RIEN NE VA PLUS !"), EffectPopup.Style.DAMAGE));
                int reel = resolver.freeReel(curses.getForbiddenReels());
                if (reel >= 0) {
                    curses.voidReel(reel);
                    events.add(new ReelForbiddenEvent(reel));
                }
            }
            case RAKE -> {
                int percent = EnemySymbol.RAKE_PERCENT * (enemy.takeCharge(EnemySignature.COUP_DE_RATEAU) ? 2 : 1);
                double raked = BigNumber.percent(player.getGains(), percent);
                if (steal(raked, events)) {
                    events.add(turn.shield((int) Math.min(raked, (double) kind.shieldDefense() * EnemySymbol.RAKE_MAX_SHIELDS)));
                } else {
                    events.add(turn.strike(EnemySymbol.FANG_DAMAGE)); // rien à ramasser : il frappe
                }
            }
            case CLOAKROOM -> {
                int count = 1 + (enemy.takeCharge(EnemySignature.TENUE_EXIGEE) ? 1 : 0);
                List<String> kept = confiscate(count, EnemySymbol.CLOAKROOM_TURNS);
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
                steal(BigNumber.percent(player.getGains(), EnemySymbol.SAFE_STEAL_PERCENT), events);
            }
            case HOUSE_EDGE -> {
                events.add(turn.strike(kind.swordDamage())); // la Salle de jeu frappe, et la Maison prend sa part
                curses.addHouseEdge();
                events.add(new StatusEvent(Lang.f("AVANTAGE DE LA MAISON : TES GAINS -{0} %", EnemySymbol.HOUSE_EDGE_PERCENT),
                    EffectPopup.Style.DAMAGE));
            }
            case GEAR -> events.add(new StatusEvent(Lang.f("ENGRENAGE : SES SYMBOLES +{0} %", turn.boostPercent),
                EffectPopup.Style.SPECIAL));
            case SHORT_CIRCUIT -> curse(events, EnemySignature.SURTENSION, curses::addShortCircuit,
                Lang.t("COURT-CIRCUIT : TON MEILLEUR ROULEAU GRILLERA"), Lang.t("COURT-CIRCUIT : 2 ROULEAUX GRILLERONT"));
            case GOLDEN_COMET -> {
                int falls = enemy.takeCharge(EnemySignature.PLUIE_D_ETOILES) ? 2 : 1;
                events.add(new StatusEvent(Lang.t("LA COMÈTE DORÉE TOMBE !"), EffectPopup.Style.ATTACK));
                for (int k = 0; k < falls; k++) events.add(turn.comet(kind.goldenCometPercent(enemy.getDifficulty())));
            }
            case ASPIRATION -> aspire(events);
            default -> {
                return false;
            }
        }
        return true;
    }

    /**
     * Tour, les Éclats dispersés (Éclat, Héritage, Valse, Scellé).
     *
     * @return {@code false} si {@code symbol} n'est pas de cette famille
     */
    boolean actScatteredShards(EnemySymbol symbol, List<Event> events) {
        switch (symbol) {
            case SHARD -> pickShard(turn, events);
            case LEGACY -> {
                int percent = enemy.takeCharge(EnemySignature.TESTAMENT) ? 100 : EnemySymbol.LEGACY_PERCENT;
                int taken = player.meltShield(percent);
                if (taken > 0) {
                    events.add(new StatusEvent(Lang.f("HÉRITAGE : IL TE PREND {0} DE BOUCLIER", Lang.big(taken)),
                        EffectPopup.Style.DAMAGE));
                }
                events.add(turn.strike(kind.swordDamage()));
            }
            case WALTZ -> {
                int missing = player.getMaxHp() <= 0 ? 0
                    : (int) (100L * (player.getMaxHp() - player.getHp()) / player.getMaxHp());
                int bonus = Math.min(EnemySymbol.WALTZ_MAX_PERCENT, Math.max(0, missing));
                if (bonus > 0) events.add(new StatusEvent(Lang.f("VALSE : +{0} %", bonus), EffectPopup.Style.ATTACK));
                events.add(turn.strike((int) Math.min(Integer.MAX_VALUE, (long) kind.swordDamage() * (100 + bonus) / 100)));
                for (int k = 0; k < boosts.getOrDefault(EnemySignature.DERNIERE_DANSE, 0); k++) {
                    events.add(heal(enemy, EnemySignature.DANSE_PERCENT * healFactor / 100f));
                }
            }
            case SEAL -> curse(events, EnemySignature.PARAPHE, n -> curses.addSeals(n * EnemySymbol.SEAL_PLAYS),
                Lang.t("SCELLÉ : UNE CARTE DE MOINS À TON PROCHAIN TOUR"),
                Lang.t("SCELLÉ : DEUX CARTES DE MOINS À TON PROCHAIN TOUR"));
            default -> {
                return false;
            }
        }
        return true;
    }

    /**
     * La Tour, chapitre « L'Ascension » (Ascenseur, Écho, Vertige, Pétrification, Plan).
     *
     * @return {@code false} si {@code symbol} n'est pas de cette famille
     */
    boolean actAscension(EnemySymbol symbol, List<Event> events) {
        switch (symbol) {
            case ELEVATOR -> {
                events.add(turn.strike(kind.swordDamage()));
                if (enemy.getFloor() < EnemySymbol.ELEVATOR_MAX) {
                    enemy.climb(1);
                    events.add(new StatusEvent(Lang.f("ASCENSEUR : ÉTAGE {0}/{1}", enemy.getFloor(), EnemySymbol.ELEVATOR_MAX),
                        EffectPopup.Style.SPECIAL));
                }
            }
            case ECHO -> echo(turn, events, enemy.takeCharge(EnemySignature.RESONANCE));
            case VERTIGO -> {
                int pending = curses.getVertigos();
                int added = Math.min(EnemySymbol.VERTIGO_MAX - pending, enemy.takeCharge(EnemySignature.LE_VIDE) ? 2 : 1);
                if (added <= 0) {
                    events.add(turn.strike(kind.swordDamage())); // déjà pris de vertige : il frappe
                } else {
                    curses.addVertigo(added);
                    events.add(new StatusEvent(Lang.n(curses.getVertigos(), "VERTIGE : {0} CARTE JOUÉE DE MOINS",
                        "VERTIGE : {0} CARTES JOUÉES DE MOINS", curses.getVertigos()), EffectPopup.Style.DAMAGE));
                }
            }
            case STONE -> {
                int percent = boosts.containsKey(EnemySignature.GRANIT) ? EnemySymbol.GRANITE_PERCENT : EnemySymbol.STONE_PERCENT;
                if (enemy.getStonePercent() >= percent) {
                    events.add(turn.strike(kind.swordDamage())); // déjà de pierre : il frappe
                } else {
                    enemy.petrify(percent);
                    events.add(new StatusEvent(Lang.f("PÉTRIFICATION : TES COUPS -{0} %", percent), EffectPopup.Style.DEFENSE));
                }
            }
            case BLUEPRINT -> {
                int floors = enemy.takeCharge(EnemySignature.CLE_DE_VOUTE) ? 2 : 1;
                if (enemy.getStoreys() >= EnemySymbol.BLUEPRINT_MAX) {
                    events.add(turn.strike(kind.swordDamage())); // sa Tour est finie : il frappe
                } else {
                    enemy.build(floors);
                    events.add(new StatusEvent(Lang.f("PLAN : SA TOUR A {0} ÉTAGES", enemy.getStoreys()), EffectPopup.Style.SPECIAL));
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    /**
     * Tour, chapitre 3 : Les Pèlerins de la Comète (Serment, Voyance, Relique, Pèlerinage).
     *
     * @return {@code false} si {@code symbol} n'est pas de cette famille
     */
    boolean actPilgrims(EnemySymbol symbol, List<Event> events) {
        switch (symbol) {
            case IRON_VOW -> {
                if (enemy.addVow()) {
                    events.add(new StatusEvent(Lang.f("SERMENT : SA DÉFENSE DE BASE +{0}",
                        Lang.big(kind.fortified(EnemySymbol.VOW_DEFENSE, enemy.getDifficulty()))), EffectPopup.Style.DEFENSE));
                } else {
                    events.add(turn.shield(kind.shieldDefense() + shieldBonus)); // déjà tous ses Serments : il se protège
                }
            }
            case SIGHT -> {
                boolean crystal = enemy.takeCharge(EnemySignature.BOULE_DE_CRISTAL);
                int held = player.getCurrentHand().size();
                List<String> locked = lockBest(EnemySymbol.SIGHT_LOCKS * (crystal ? 2 : 1), EnemySymbol.KEY_TURNS);
                if (!locked.isEmpty()) {
                    events.add(new StatusEvent(Lang.f("VOYANCE : {0} FERMÉE", String.join(", ", locked)), EffectPopup.Style.DAMAGE));
                }
                long hit = (long) kind.swordDamage() * EnemySymbol.SIGHT_PERCENT_PER_CARD * Math.max(1, held) / 100 * (crystal ? 2 : 1);
                events.add(turn.strike((int) Math.min(Integer.MAX_VALUE, Math.max(1, hit))));
            }
            case RELIC -> {
                boolean altar = enemy.takeCharge(EnemySignature.AUTEL);
                List<Card.Suit> suits = heldSuits();
                List<String> blessed = new ArrayList<>();
                int left = EnemySymbol.RELIC_MAX_CARDS * (altar ? 2 : 1);
                for (int k = 0; k < Math.min(altar ? 2 : 1, suits.size()); k++) {
                    Card.Suit suit = suits.get(k);
                    for (Card card : new ArrayList<>(player.getCurrentHand())) {
                        if (left <= 0) break;
                        if (card.getSuit() == suit && player.steal(card, EnemySymbol.RELIC_TURNS)) left--;
                    }
                    blessed.add(suitPlural(suit));
                }
                events.add(blessed.isEmpty() ? turn.strike(kind.swordDamage()) // pas de carte de couleur en main : il frappe
                    : new StatusEvent(Lang.f("RELIQUE : TES {0} SONT FERMÉS", String.join(", ", blessed)), EffectPopup.Style.DAMAGE));
            }
            case PILGRIMAGE -> {
                enemy.addPilgrim();
                int percent = 100 + EnemySymbol.PILGRIM_STEP_PERCENT * enemy.getPilgrims();
                events.add(new StatusEvent(Lang.n(enemy.getPilgrims(), "PÈLERINAGE : 1 PÈLERIN, +{1} %",
                    "PÈLERINAGE : {0} PÈLERINS, +{1} %", enemy.getPilgrims(), percent - 100), EffectPopup.Style.ATTACK));
                events.add(turn.strike((int) Math.min(Integer.MAX_VALUE, (long) kind.swordDamage() * percent / 100)));
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    /**
     * Guerre des Tables : Rassemblement, Canon à jetons, Espionnage, Contrat et Va-tout.
     *
     * @return {@code false} si {@code symbol} n'est pas de cette famille
     */
    boolean actWar(EnemySymbol symbol, List<Event> events) {
        switch (symbol) {
            case RALLY -> {
                if (enemy.addTroop()) {
                    events.add(new StatusEvent(Lang.f("RASSEMBLEMENT : {0}/{1} SOLDATS", enemy.getTroops(), EnemySymbol.RALLY_MAX),
                        EffectPopup.Style.SPECIAL));
                } else {
                    events.add(turn.strike(kind.swordDamage())); // son armée est au complet : il charge
                }
            }
            case CANNON -> {
                // Poudre noire : le boulet traverse tout le bouclier ; sinon la moitié seulement l'arrête
                int stops = boosts.getOrDefault(EnemySignature.POUDRE_NOIRE, 0) > 0 ? 0 : 100 - EnemySymbol.CANNON_PIERCE_PERCENT;
                if (steal(BigNumber.percent(player.getGains(), EnemySymbol.CANNON_PERCENT), events)) {
                    events.add(new StatusEvent(Lang.t("CANON À JETONS : FEU !"), EffectPopup.Style.ATTACK));
                    events.add(turn.strike(kind.swordDamage() * EnemySymbol.CANNON_FACTOR, stops));
                } else {
                    events.add(turn.strike(kind.swordDamage(), stops)); // rien à charger : il tire à vide
                }
            }
            case SPY -> {
                int seen = Math.min(EnemySymbol.SPY_MAX, player.getPlayedCards().size())
                    * (enemy.takeCharge(EnemySignature.MICROFILM) ? 2 : 1);
                if (seen > 0) {
                    events.add(new StatusEvent(Lang.n(seen, "ESPIONNAGE : ELLE A VU TA CARTE", "ESPIONNAGE : ELLE A VU TES {0} CARTES",
                        seen), EffectPopup.Style.SPECIAL));
                    events.add(turn.shield(kind.shieldDefense() * seen + shieldBonus));
                } else {
                    events.add(turn.strike(kind.swordDamage())); // rien à espionner : elle frappe
                }
            }
            case CONTRACT -> {
                boolean paid = enemy.getDamageTaken() * 100 < enemy.getMaxHp() * EnemySymbol.CONTRACT_PERCENT;
                int percent = 100 + EnemySignature.PRIME_PERCENT * boosts.getOrDefault(EnemySignature.PRIME_DE_RISQUE, 0);
                if (paid) events.add(new StatusEvent(Lang.t("CONTRAT : IL TOUCHE SA PRIME !"), EffectPopup.Style.ATTACK));
                events.add(turn.strike((int) Math.min(Integer.MAX_VALUE,
                    (long) kind.swordDamage() * (paid ? EnemySymbol.CONTRACT_FACTOR : 1) * percent / 100)));
            }
            case ALL_OUT -> events.add(new StatusEvent(Lang.t("VA-TOUT !"), EffectPopup.Style.ATTACK));
            default -> {
                return false;
            }
        }
        return true;
    }
}
