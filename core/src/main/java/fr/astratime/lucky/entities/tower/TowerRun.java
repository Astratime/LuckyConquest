package fr.astratime.lucky.entities.tower;

import fr.astratime.lucky.entities.enemy.EnemyKind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Une ascension d'un chapitre de la Tour des épreuves : trois combats à la suite.
 * <ol>
 *   <li>le croupier ({@link EnemyKind#CROUPIER}) ;</li>
 *   <li>l'adversaire choisi par le joueur parmi trois cartes faces cachées
 *       (les {@link EnemyKind#CHALLENGERS}, dans un ordre tiré au hasard) ;</li>
 *   <li>le boss du chapitre ({@link EnemyKind#COMETE}).</li>
 * </ol>
 * Une défaite fait recommencer le chapitre au premier combat.
 */
public class TowerRun {

    /** Nombre de combats d'un chapitre. */
    public static final int STAGES = 3;

    private final Chapter chapter;
    private final Random  random;
    private int           stage;
    private EnemyKind     enemy = EnemyKind.CROUPIER;
    private List<EnemyKind> choices = List.of();

    public TowerRun(Chapter chapter) {
        this(chapter, new Random());
    }

    /** @param random ordre des cartes du choix de l'adversaire (graine fixe pour les tests) */
    public TowerRun(Chapter chapter, Random random) {
        this.chapter = chapter;
        this.random  = random;
    }

    /** @return le chapitre en cours. */
    public Chapter getChapter() { return chapter; }
    /** @return le combat en cours, de 0 (le premier) à {@link #STAGES} - 1 (le boss). */
    public int getStage() { return stage; }
    /** @return l'ennemi du combat en cours. */
    public EnemyKind getEnemy() { return enemy; }
    /** @return {@code true} pendant le combat contre le boss. */
    public boolean isBossStage() { return stage == STAGES - 1; }

    /**
     * Combat en cours gagné : prépare le suivant.
     *
     * @return ce qui vient ensuite
     */
    public Next win() {
        if (isBossStage()) return Next.CHAPTER_CLEARED;
        stage++;
        if (isBossStage()) {
            enemy = EnemyKind.COMETE;
            return Next.BOSS;
        }
        List<EnemyKind> shuffled = new ArrayList<>(EnemyKind.CHALLENGERS);
        Collections.shuffle(shuffled, random);
        choices = List.copyOf(shuffled);
        return Next.CHOOSE_ENEMY;
    }

    /** @return les trois adversaires cachés sous les cartes du choix, dans l'ordre des cartes. */
    public List<EnemyKind> getChoices() { return choices; }

    /**
     * Le joueur retourne la carte {@code index} : son ennemi sera l'adversaire du combat.
     *
     * @return l'ennemi choisi
     */
    public EnemyKind choose(int index) {
        if (choices.isEmpty()) throw new IllegalStateException("Aucun choix d'adversaire en cours");
        enemy   = choices.get(index);
        choices = List.of();
        return enemy;
    }

    /** Défaite : le chapitre recommence au premier combat. */
    public void restart() {
        stage   = 0;
        enemy   = EnemyKind.CROUPIER;
        choices = List.of();
    }

    /** Ce qui suit un combat gagné. */
    public enum Next { CHOOSE_ENEMY, BOSS, CHAPTER_CLEARED }
}
