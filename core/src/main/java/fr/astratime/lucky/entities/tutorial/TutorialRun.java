package fr.astratime.lucky.entities.tutorial;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.run.CombatRun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Le tutoriel : un seul combat contre le croupier d'entraînement, que le
 * Croupier commente (voir {@code fr.astratime.lucky.screens.TutorialDirector}).
 * Pour que chaque leçon tombe au bon moment, le deck n'est pas mélangé (les
 * mains de chaque tour sont prévues, voir {@link #HANDS}), les premiers tirages
 * sont imposés ({@link #rigged(int)}, puis le Bingo, {@link #BINGO_SYMBOL}) et l'échoppe ne vend que la carte Bingo,
 * à un prix que les gains des premiers tours paient. Il ne rapporte rien et ne
 * touche pas à la partie : ni le deck ni la machine du joueur ne servent.
 */
public class TutorialRun implements CombatRun {

    /** Les mains des trois premiers tours, par id de carte, dans l'ordre où elles sont piochées. */
    public static final List<List<String>> HANDS = List.of(
        List.of("gain_500", "7_trefle", "11_coeur", "2_carreau", "12_pique", "13_trefle"),
        List.of("7_coeur", "7_carreau", "lucky_charm", "2_trefle", "13_pique", "11_trefle"),
        List.of("5_trefle", "2_pique", "5_coeur", "12_carreau", "11_pique", "12_coeur"));

    /**
     * Les cartes que le Croupier fait jouer, dans l'ordre : le Gains +500 et le 7 de Trèfle, les deux 7 (une
     * Paire), le Porte-bonheur, puis le Valet de Pique (il perce la défense) et la carte Bingo achetée.
     */
    public static final String GAINS_CARD = "gain_500";
    public static final String CLUB_SEVEN = "7_trefle";
    public static final List<String> PAIR = List.of("7_coeur", "7_carreau");
    public static final String LUCKY_CHARM = "lucky_charm";
    public static final String SPADE = "11_pique";
    public static final String BINGO = "bingo";

    /** Prix de la carte Bingo à l'échoppe du tutoriel : le Gains +500 du premier tour suffit. */
    public static final int BINGO_PRICE = 500;

    /** @return l'échoppe du tutoriel : la carte Bingo seule. */
    public static Map<String, Integer> shop() {
        Map<String, Integer> shop = new LinkedHashMap<>();
        shop.put(BINGO, BINGO_PRICE);
        return shop;
    }

    /** @return les ids des cartes du deck du tutoriel (les mains prévues, à la suite). */
    public static Map<String, Integer> deck() {
        Map<String, Integer> copies = new LinkedHashMap<>();
        for (List<String> hand : HANDS) for (String id : hand) copies.merge(id, 1, Integer::sum);
        return copies;
    }

    /**
     * Range le deck {@code cards} (mélangé à sa création) pour que les mains
     * prévues sortent dans l'ordre : la pioche prend la dernière carte de la liste.
     */
    public static void arrange(List<Card> cards) {
        List<String> order = new ArrayList<>();
        HANDS.forEach(order::addAll);
        cards.sort(Comparator.comparingInt((Card card) -> {
            int index = order.indexOf(card.getId());
            return index < 0 ? order.size() : index;
        }));
        Collections.reverse(cards);
    }

    /**
     * @param turn tour du joueur, à partir de 1
     * @return les symboles imposés aux trois rouleaux à ce tour, ou {@code null} (tirage libre)
     */
    public static Symbol[] rigged(int turn) {
        return switch (turn) {
            case 1 -> new Symbol[] {Symbol.SEVEN, Symbol.BELL, Symbol.GRAPE};      // une attaque, des gains, un bouclier
            case 2 -> new Symbol[] {Symbol.CHERRY, Symbol.WATERMELON, Symbol.DIAMOND};
            default -> null;
        };
    }

    /** Le Bingo de la carte Bingo, au troisième tour : un Triple Sept, qui achève le croupier. */
    public static final Symbol BINGO_SYMBOL = Symbol.TRIPLE_SEVEN;

    @Override
    public EnemyKind getEnemy() { return EnemyKind.ENTRAINEMENT; }

    @Override
    public int getStage() { return 0; }

    @Override
    public int getStageCount() { return 1; }

    @Override
    public Next win() { return Next.CLEARED; }

    @Override
    public void restart() { }

    @Override
    public String getLabel() { return "Tutoriel"; }
}
