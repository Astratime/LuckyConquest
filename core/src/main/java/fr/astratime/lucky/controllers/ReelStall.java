package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.BigNumber;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.SpinEconomy;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Les rouleaux de l'échoppe (choix d'Astra, 2026-11-06) : pendant un combat, le
 * joueur peut acheter un des {@link #OFFERS} rouleaux tirés au hasard parmi ceux
 * qui ne sont pas dans sa machine, ou retirer un rouleau de sa machine. Les deux
 * se paient avec les gains du combat, et leur prix double à chaque fois. La
 * machine garde de {@link #MIN_REELS} à {@link #MAX_REELS} rouleaux (Joker à part),
 * et redevient celle de la Table du croupier au combat suivant.
 */
public class ReelStall {

    /** Rouleaux proposés à l'achat à chaque combat. */
    public static final int OFFERS = 3;
    /** La machine ne descend pas sous ce nombre de rouleaux (le Joker ne compte pas). */
    public static final int MIN_REELS = 8;
    /** La machine ne dépasse pas ce nombre de rouleaux (le Joker ne compte pas). */
    public static final int MAX_REELS = 14;
    /** Prix de base d'un rouleau acheté, en prix du tirage. */
    public static final int BUY_SPINS = 30;
    /** Prix de base d'un rouleau retiré, en prix du tirage. */
    public static final int REMOVE_SPINS = 15;

    /** Un rouleau en vente et son prix du moment. */
    public record ReelOffer(Symbol symbol, double price) { }

    private final Random random;
    private final List<Symbol> offers = new ArrayList<>();
    private int bought;
    private int removed;

    public ReelStall(Random random) { this.random = random; }

    /** Nouveau combat : tire les rouleaux proposés parmi ceux qui manquent à la machine, et remet les prix à zéro. */
    public void open(List<Symbol> machine) {
        offers.clear();
        bought  = 0;
        removed = 0;
        List<Symbol> candidates = new ArrayList<>();
        for (Symbol symbol : allReels()) if (!machine.contains(symbol)) candidates.add(symbol);
        Collections.shuffle(candidates, random);
        offers.addAll(candidates.subList(0, Math.min(OFFERS, candidates.size())));
    }

    /** @return tous les rouleaux qui peuvent aller dans une machine (ni le Joker, ni ceux des Bingos imposés). */
    public static List<Symbol> allReels() {
        List<Symbol> reels = new ArrayList<>();
        for (Symbol symbol : Symbol.values()) if (symbol != Symbol.JOKER && !symbol.isSummoned()) reels.add(symbol);
        return reels;
    }

    /** @return les rouleaux encore en vente, avec le prix d'un achat. */
    public List<ReelOffer> getOffers(int spinCost) {
        double price = getBuyPrice(spinCost);
        return offers.stream().map(symbol -> new ReelOffer(symbol, price)).toList();
    }

    /** @return le prix du prochain rouleau acheté dans ce combat. */
    public double getBuyPrice(int spinCost) { return price(BUY_SPINS, bought, spinCost); }

    /** @return le prix du prochain rouleau retiré dans ce combat. */
    public double getRemovePrice(int spinCost) { return price(REMOVE_SPINS, removed, spinCost); }

    /** Prix en prix du tirage (celui de la Prairie hors vrai combat), doublé à chaque achat. */
    private static double price(int spins, int count, int spinCost) {
        int cost = spinCost > 0 ? spinCost : SpinEconomy.BASE_SPIN_COST;
        return BigNumber.round(spins * (double) cost * Math.pow(2, count));
    }

    /** @return pourquoi {@code symbol} ne peut pas être acheté (hors manque de gains), ou {@code null}. */
    public String buyProblem(Player player, Symbol symbol) {
        if (!offers.contains(symbol)) return Lang.t("Ce rouleau n'est pas en vente");
        if (player.getSlotMachine().getReels().size() >= MAX_REELS) {
            return Lang.f("Ta machine a déjà {0} rouleaux, le maximum", MAX_REELS);
        }
        return null;
    }

    /** @return pourquoi {@code symbol} ne peut pas être retiré (hors manque de gains), ou {@code null}. */
    public String removeProblem(Player player, Symbol symbol) {
        List<Symbol> reels = player.getSlotMachine().getReels();
        if (!reels.contains(symbol)) return Lang.t("Ce rouleau n'est pas dans ta machine");
        if (reels.size() <= MIN_REELS) return Lang.f("Il faut garder au moins {0} rouleaux", MIN_REELS);
        return null;
    }

    /** Achète {@code symbol} : il rejoint la machine pour ce combat. @return {@code false} s'il ne peut pas l'être */
    public boolean buy(Player player, Symbol symbol, int spinCost) {
        double price = getBuyPrice(spinCost);
        if (buyProblem(player, symbol) != null || player.getGains() < price) return false;
        player.paySpin(price); // comme un tirage : les gains de la cagnotte partent d'abord
        player.getSlotMachine().addReel(symbol);
        offers.remove(symbol);
        bought++;
        return true;
    }

    /** Retire {@code symbol} de la machine pour ce combat. @return {@code false} s'il ne peut pas l'être */
    public boolean remove(Player player, Symbol symbol, int spinCost) {
        double price = getRemovePrice(spinCost);
        if (removeProblem(player, symbol) != null || player.getGains() < price) return false;
        player.paySpin(price);
        player.getSlotMachine().removeReel(symbol);
        removed++;
        return true;
    }
}
