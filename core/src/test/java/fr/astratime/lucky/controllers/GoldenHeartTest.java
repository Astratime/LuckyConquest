package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.progress.Rank;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Cœur d'or : les bonus de gains du tour (rang + multiplicateurs des cartes) deviennent des bonus d'attaque. */
class GoldenHeartTest {

    private static CombatContext legend() {
        Player player = new Player("Joueur", 100, List.of(), Rank.LEGENDE.getBonus(), Symbol.classicReels());
        return new CombatContext(player, new Enemy("Ennemi", 1000));
    }

    @Test
    void legendWithAKingOfClubsGetsRankGainsPlusCardMultiplier() {
        CombatContext context = legend();
        context.addGainMultiplier(13); // Roi de Trèfle
        context.addGoldenHeart();
        CombatResolver.goldenHeart(context);
        assertEquals(4_100, context.getAttackBonus()); // 2 800 + 13 x 100
    }

    @Test
    void eachGoldenHeartCountsAndRankAloneWithoutClubs() {
        CombatContext context = legend();
        context.addGoldenHeart();
        context.addGoldenHeart();
        assertEquals(5_600, CombatResolver.goldenHeartBonus(context));
    }

    @Test
    void nothingWithoutTheCard() {
        CombatContext context = legend();
        context.addGainMultiplier(13);
        CombatResolver.goldenHeart(context);
        assertEquals(0, context.getAttackBonus());
    }
}
