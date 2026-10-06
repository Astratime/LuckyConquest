package fr.astratime.lucky.entities.tower;

import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.run.CombatRun;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Une ascension d'un chapitre de la Tour des épreuves : trois combats à la suite.
 * <ol>
 *   <li>le premier ennemi du chapitre ({@link Chapter#getFirstEnemy()}) ;</li>
 *   <li>l'adversaire choisi par le joueur parmi trois cartes faces cachées
 *       ({@link Chapter#getChallengers()}, dans un ordre tiré au hasard) ;</li>
 *   <li>le boss du chapitre ({@link Chapter#getBoss()}).</li>
 * </ol>
 * Une défaite fait recommencer le chapitre au premier combat.
 */
public class TowerRun implements CombatRun {

    /** Nombre de combats d'un chapitre. */
    public static final int STAGES = 3;

    private final Chapter chapter;
    private final Random  random;
    private final boolean hard;
    private int           stage;
    private EnemyKind     enemy;
    private List<EnemyKind> choices = List.of();

    public TowerRun(Chapter chapter) {
        this(chapter, false);
    }

    /** @param hard mode difficile : ennemis plus forts (voir {@link EnemyKind#HARD_POWER_FACTOR}) */
    public TowerRun(Chapter chapter, boolean hard) {
        this(chapter, new Random(), hard);
    }

    /** @param random ordre des cartes du choix de l'adversaire (graine fixe pour les tests) */
    public TowerRun(Chapter chapter, Random random) {
        this(chapter, random, false);
    }

    private TowerRun(Chapter chapter, Random random, boolean hard) {
        this.chapter = chapter;
        this.random  = random;
        this.hard    = hard;
        this.enemy   = chapter.getFirstEnemy();
    }

    /** @return le chapitre en cours. */
    public Chapter getChapter() { return chapter; }
    @Override
    public int getStage() { return stage; }
    @Override
    public int getStageCount() { return STAGES; }
    @Override
    public EnemyKind getEnemy() { return enemy; }
    /** @return {@code true} en mode difficile. */
    public boolean isHard() { return hard; }
    @Override
    public String getLabel() { return hard ? Lang.f("{0} difficile", chapter.getLabel()) : chapter.getLabel(); }
    @Override
    public String getEnding() { return chapter.getEnding(); }

    @Override
    public Next win() {
        if (isBossStage()) return Next.CLEARED;
        stage++;
        if (isBossStage()) {
            enemy = chapter.getBoss();
            return Next.BOSS;
        }
        List<EnemyKind> shuffled = new ArrayList<>(chapter.getChallengers());
        Collections.shuffle(shuffled, random);
        choices = List.copyOf(shuffled);
        return Next.CHOOSE_ENEMY;
    }

    /** @return les trois adversaires cachés sous les cartes du choix, dans l'ordre des cartes. */
    @Override
    public List<EnemyKind> getChoices() { return choices; }

    @Override
    public EnemyKind choose(int index) {
        if (choices.isEmpty()) throw new IllegalStateException("Aucun choix d'adversaire en cours");
        enemy   = choices.get(index);
        choices = List.of();
        return enemy;
    }

    /** Défaite : le chapitre recommence au premier combat. */
    @Override
    public void restart() {
        stage   = 0;
        enemy   = chapter.getFirstEnemy();
        choices = List.of();
    }
}
