package fr.astratime.lucky.entities.exploration;

import fr.astratime.lucky.entities.enemy.EnemyKind;

import java.util.Random;
import java.util.function.Predicate;

/**
 * Le Joueur Masqué (idée A2 du 2026-11-11, nom choisi par Astra) : un autre élève du Croupier,
 * qui prend parfois la place du soldat d'un donjon de l'Exploration (1 fois sur {@link #CHANCE}).
 * Une rencontre par lieu à partir de la Fête foraine (cinq, puis une de plus par nouveau lieu) : sa machine et son deck grandissent
 * à chaque fois, avec une petite scène avant chaque duel. Le duel final, au Désert de Braise,
 * n'arrive qu'une fois toutes les autres rencontres gagnées : il y enlève son masque.
 * <p>
 * Il remplace un combat qui rapporte déjà des pièces et paie comme le soldat qu'il remplace
 * (ses tirages coûtent le prix du lieu, rien de plus). Il est au moins aussi fort que le roi
 * du lieu : PV du roi x1,25 (x1,5 au Désert), un symbole et une carte propre de plus par lieu.
 * Une rencontre gagnée ne revient plus (notée au bestiaire du joueur, voir {@link #due}).
 */
public final class Rival {

    /** Il apparaît une fois sur {@code CHANCE} à la place du soldat, quand une rencontre l'attend dans le lieu. */
    public static final int CHANCE = 8;

    private Rival() {}

    /** @return le Joueur Masqué de la rencontre du lieu {@code place}, ou {@code null} (la prairie n'en a pas). */
    public static EnemyKind of(Place place) {
        return switch (place) {
            case PRAIRIE -> null;
            case FOIRE   -> EnemyKind.RIVAL_FOIRE;
            case RAPIDE  -> EnemyKind.RIVAL_RAPIDE;
            case MANOIR  -> EnemyKind.RIVAL_MANOIR;
            case PORT    -> EnemyKind.RIVAL_PORT;
            case PALACE  -> EnemyKind.RIVAL_PALACE;
            case MINES   -> EnemyKind.RIVAL_MINES;
            case CASINO  -> EnemyKind.RIVAL_CASINO;
            case TEMPLE  -> EnemyKind.RIVAL_TEMPLE;
            case DESERT  -> EnemyKind.RIVAL_DESERT;
        };
    }

    /** @return le lieu de la rencontre {@code kind} du Joueur Masqué, ou {@code null} si ce n'est pas lui. */
    public static Place placeOf(EnemyKind kind) {
        return switch (kind) {
            case RIVAL_FOIRE  -> Place.FOIRE;
            case RIVAL_RAPIDE -> Place.RAPIDE;
            case RIVAL_MANOIR -> Place.MANOIR;
            case RIVAL_PORT   -> Place.PORT;
            case RIVAL_PALACE -> Place.PALACE;
            case RIVAL_MINES  -> Place.MINES;
            case RIVAL_CASINO -> Place.CASINO;
            case RIVAL_TEMPLE -> Place.TEMPLE;
            case RIVAL_DESERT -> Place.DESERT;
            default           -> null;
        };
    }

    /** @return le numéro de la rencontre {@code kind} (1 à la Foire, 5 au Désert), 0 si ce n'est pas lui. */
    public static int encounter(EnemyKind kind) {
        Place place = placeOf(kind);
        return place == null ? 0 : place.ordinal();
    }

    /** @return {@code true} pour sa rencontre de l'Histoire, à la Maison du Croupier (Astra, 2026-11-23). */
    public static boolean isStory(EnemyKind kind) { return kind == EnemyKind.RIVAL_HISTOIRE; }

    /** @return {@code true} pour le duel final, au Désert de Braise, où il enlève son masque. */
    public static boolean isFinal(EnemyKind kind) { return kind == EnemyKind.RIVAL_DESERT; }

    /**
     * @param defeated {@code true} pour un ennemi déjà vaincu par le joueur
     * @return le Joueur Masqué qui attend le joueur dans {@code place}, ou {@code null} : sa rencontre
     *         du lieu n'est pas encore gagnée, et pour le Désert, les quatre premières le sont
     */
    public static EnemyKind due(Place place, Predicate<EnemyKind> defeated) {
        EnemyKind rival = of(place);
        if (rival == null || defeated.test(rival)) return null;
        if (isFinal(rival)) {
            for (Place before = Place.DESERT.getPrevious(); before != Place.PRAIRIE; before = before.getPrevious()) {
                if (!defeated.test(of(before))) return null;
            }
        }
        return rival;
    }

    /** @return le Joueur Masqué qui prend la place du soldat cette fois-ci (1 fois sur {@link #CHANCE}), ou {@code null}. */
    public static EnemyKind roll(Place place, Predicate<EnemyKind> defeated, Random random) {
        EnemyKind rival = due(place, defeated);
        return rival != null && random.nextInt(CHANCE) == 0 ? rival : null;
    }
}
