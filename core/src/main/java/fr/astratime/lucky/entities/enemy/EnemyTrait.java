package fr.astratime.lucky.entities.enemy;

import java.util.EnumSet;
import java.util.Set;

/**
 * Capacités propres à certains ennemis, avec ceux qui les ont : la table de
 * ce que chaque boss fait de spécial, en un seul endroit (voir
 * {@link EnemyKind#has(EnemyTrait)}). Une nouvelle capacité ou un nouvel
 * ennemi qui en hérite s'écrit ici, sans toucher au code qui s'en sert.
 */
public enum EnemyTrait {

    /** Son deck contient le Rouleau interdit (l'Éclat Originel, puis le Prétendant qui porte son éclat). */
    FORBIDDEN_REEL(EnemyKind.ECLAT, EnemyKind.PRETENDANT),
    /** Sa peau d'or encaisse la moitié des coups tant qu'il a plus de la moitié de ses PV. */
    GOLD_SKIN(EnemyKind.GOLEM_OR, EnemyKind.COEUR_MONTAGNE),
    /** Ses trois rouleaux identiques font un Jackpot (attaques et Boucliers x{@link EnemySymbol#JACKPOT_FACTOR}). */
    JACKPOTS(EnemyKind.BANDIT_MANCHOT, EnemyKind.JACKPOT_VIVANT),
    /** Il joue une carte par bras, et perd un bras à chaque huitième de ses PV (le Kraken). */
    ARMS(EnemyKind.KRAKEN),
    /** À sa phase 2, il s'enferme un tour dans son sarcophage et se soigne (le Pharaon). */
    SARCOPHAGUS(EnemyKind.PHARAON),
    /** Il joue ses cartes au hasard (la Roulette Vivante). */
    RANDOM_PLAYS(EnemyKind.ROULETTE),
    /** Son Rien ne va plus sort à part, au plus une fois par tour (le Croupier en chef). */
    NOTHING_GOES_APART(EnemyKind.CHEF),
    /** Ses Potions reforment aussi sa défense (le Croupier en chef). */
    SHIELDING_POTIONS(EnemyKind.CHEF),
    /** Ses Trèfles comptent double quand sa vie est basse (la Reine des Tables). */
    ROYAL_BET(EnemyKind.REINE),
    /** Il garde une copie (un As sombre) de chaque carte achetée à l'échoppe (le Pilleur). */
    COPIES_PURCHASES(EnemyKind.PILLEUR),
    /** Il interdit une famille de cartes tout le combat (le Portier). */
    BANS_FAMILY(EnemyKind.PORTIER),
    /** Il joue avec une copie du deck du joueur (l'Ombre du Joueur). */
    COPIES_PLAYER_DECK(EnemyKind.OMBRE),
    /** Une fois par combat, il annule le plus gros coup du joueur (la Maison). */
    HOUSE_WINS(EnemyKind.MAISON),
    /** Achevé, il résiste et tente un dernier tirage (la Machine Originelle). */
    LAST_DRAW(EnemyKind.MACHINE_ORIGINELLE),
    /**
     * Blessé, il vole des rouleaux au joueur : sa machine en gagne autant, et chaque rouleau pris
     * renforce ses Engrenages (la Machine Originelle).
     */
    REEL_THEFT(EnemyKind.MACHINE_ORIGINELLE),
    /** Trois symboles pareils : ce symbole agit {@link EnemySymbol#ORIGINAL_JACKPOT_ACTS} fois (la Machine Originelle). */
    ORIGINAL_JACKPOT(EnemyKind.MACHINE_ORIGINELLE),
    /** Il porte les éclats des boss d'avant et perd leurs pouvoirs un à un, phase après phase (le Prétendant). */
    SHARDS(EnemyKind.PRETENDANT),
    /** Il tombe en parties : sans sa Façade (phase 2), plus de défense de base (la Maison). */
    FALLING_FACADE(EnemyKind.MAISON);

    private final Set<EnemyKind> kinds;

    EnemyTrait(EnemyKind first, EnemyKind... others) {
        this.kinds = EnumSet.of(first, others);
    }

    /** @return {@code true} si {@code kind} a cette capacité. */
    public boolean of(EnemyKind kind) {
        return kinds.contains(kind);
    }
}
