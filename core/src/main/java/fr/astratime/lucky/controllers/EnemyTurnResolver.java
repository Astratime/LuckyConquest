package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.enemy.EnemyCards;
import fr.astratime.lucky.entities.enemy.EnemySlotMachine;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.events.DamageReflectedEvent;
import fr.astratime.lucky.entities.events.EnemyHealedEvent;
import fr.astratime.lucky.entities.events.EnemyShieldedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.PlayerDamagedEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Tour de l'ennemi : il pioche {@link Enemy#HAND_SIZE} cartes, en joue
 * {@link Enemy#PLAYS_PER_TURN} (voir {@link #choose}), lance sa machine à sous,
 * puis chaque symbole agit, renforcé par les cartes jouées :
 * <ul>
 *   <li>Épée : attaque le joueur ({@link EnemySymbol#SWORD_DAMAGE}, + les Piques) ;
 *       son bouclier absorbe les coups, et le renvoi de dégâts (Carreau)
 *       touche l'ennemi une fois toutes ses attaques portées ;</li>
 *   <li>Bouclier : défense ({@link EnemySymbol#SHIELD_DEFENSE}, + les Carreaux)
 *       pendant le prochain tour du joueur ;</li>
 *   <li>Potion : soin ({@link EnemySymbol#POTION_PERCENT} % des PV max, + les Cœurs).</li>
 * </ul>
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
        enemy.resetShieldDefense(); // la défense de son tour précédent ne valait que pour le tour du joueur
        List<Card> drawn  = enemy.draw(Enemy.HAND_SIZE);
        List<Card> played = choose(drawn, enemy.getHpRatio());

        int swordBonus = 0, healBonus = 0, shieldBonus = 0;
        Map<EnemySymbol, Integer> luck = new EnumMap<>(EnemySymbol.class);
        for (Card card : played) {
            switch (card.getSuit()) {
                case PIQUE   -> swordBonus  += EnemyCards.swordBonus(card);
                case COEUR   -> healBonus   += EnemyCards.healBonus(card);
                case CARREAU -> shieldBonus += EnemyCards.shieldBonus(card);
                case TREFLE  -> {
                    EnemySymbol lucky = EnemySymbol.values()[random.nextInt(EnemySymbol.values().length)];
                    luck.merge(lucky, EnemyCards.luckBonus(card), Integer::sum);
                }
            }
        }

        EnemySymbol[] symbols = machine.spin(luck);
        List<List<Event>> outcomes = new ArrayList<>();
        int totalAttack = 0;
        for (EnemySymbol symbol : symbols) {
            List<Event> events = new ArrayList<>();
            switch (symbol) {
                case SWORD -> {
                    int attack = EnemySymbol.SWORD_DAMAGE + swordBonus;
                    totalAttack += attack;
                    events.add(new PlayerDamagedEvent(player.takeDamage(attack)));
                }
                case SHIELD -> {
                    int defense = EnemySymbol.SHIELD_DEFENSE + shieldBonus;
                    enemy.addShieldDefense(defense);
                    events.add(new EnemyShieldedEvent(defense));
                }
                case POTION -> {
                    int percent = EnemySymbol.POTION_PERCENT + healBonus;
                    events.add(new EnemyHealedEvent(enemy.heal(Math.round(enemy.getMaxHp() * percent / 100f))));
                }
            }
            outcomes.add(events);
        }

        List<Event> afterEvents = new ArrayList<>();
        if (totalAttack > 0 && reflectPercent > 0) {
            // Le Coffre arme le renvoi : une part de son contenu s'ajoute à l'attaque renvoyée.
            float reflectBase = totalAttack + player.getLastingEffects().getVault() * vaultShare;
            int reflected = Math.round(reflectBase * (reflectPercent / 100f));
            if (reflected > 0) {
                enemy.takeDamage(reflected);
                afterEvents.add(new DamageReflectedEvent(reflected));
            }
        }

        enemy.discard(drawn);
        return new EnemyTurnResult(drawn, played, luck, symbols, outcomes, afterEvents);
    }

    /**
     * IA de l'ennemi : les {@link Enemy#PLAYS_PER_TURN} cartes à jouer parmi
     * {@code hand}. En forme, il attaque (Piques), se protège (Carreaux), tente
     * sa chance (Trèfles), et ne se soigne (Cœurs) qu'en dernier ; sous
     * {@link #LOW_HP_RATIO} de vie, il se soigne d'abord. À couleur égale, le
     * rang le plus fort passe devant.
     */
    static List<Card> choose(List<Card> hand, float hpRatio) {
        List<Card.Suit> priority = hpRatio < LOW_HP_RATIO
            ? List.of(Card.Suit.COEUR, Card.Suit.CARREAU, Card.Suit.PIQUE, Card.Suit.TREFLE)
            : List.of(Card.Suit.PIQUE, Card.Suit.CARREAU, Card.Suit.TREFLE, Card.Suit.COEUR);
        return hand.stream()
            .sorted(Comparator.comparingInt((Card card) -> priority.indexOf(card.getSuit()))
                .thenComparing(Comparator.comparingInt(Card::getRank).reversed()))
            .limit(Enemy.PLAYS_PER_TURN)
            .toList();
    }
}
