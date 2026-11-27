package fr.astratime.lucky.entities.tower;

import fr.astratime.lucky.entities.GameRandom;
import fr.astratime.lucky.entities.BonusPot;
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
    /** Combat où l'ascension commence et recommence après une défaite (0, sauf choix du mode ADMIN). */
    private final int       startStage;
    private final EnemyKind startEnemy;
    private List<EnemyKind> choices = List.of();
    /** Cagnotte du Jeu bonus de cette ascension : partagée par ses combats, effacée à la fin du chapitre. */
    private final BonusPot pot = new BonusPot();

    public TowerRun(Chapter chapter) {
        this(chapter, false);
    }

    /** @param hard mode difficile : ennemis plus forts (voir {@link EnemyKind#getPower(boolean)}) */
    public TowerRun(Chapter chapter, boolean hard) {
        this(chapter, GameRandom.live(), hard);
    }

    /** @param random ordre des cartes du choix de l'adversaire (graine fixe pour les tests) */
    public TowerRun(Chapter chapter, Random random) {
        this(chapter, random, false);
    }

    private TowerRun(Chapter chapter, Random random, boolean hard) {
        this(chapter, random, hard, 0, chapter.getFirstEnemy());
    }

    private TowerRun(Chapter chapter, Random random, boolean hard, int startStage, EnemyKind startEnemy) {
        this.chapter    = chapter;
        this.random     = random;
        this.hard       = hard;
        this.startStage = startStage;
        this.startEnemy = startEnemy;
        this.stage      = startStage;
        this.enemy      = startEnemy;
    }

    /**
     * Mode ADMIN : une ascension qui commence directement au combat {@code fight} de
     * {@link #fights(Chapter)} (premier ennemi, un des trois challengers ou le boss).
     * Une défaite fait recommencer à ce combat.
     */
    public static TowerRun startingAt(Chapter chapter, boolean hard, int fight) {
        List<EnemyKind> fights = fights(chapter);
        EnemyKind kind = fights.get(fight);
        int stage = fight == 0 ? 0 : fight == fights.size() - 1 ? STAGES - 1 : 1;
        return new TowerRun(chapter, GameRandom.live(), hard, stage, kind);
    }

    /** @return tous les combats du chapitre, dans l'ordre : premier ennemi, les trois challengers, le boss. */
    public static List<EnemyKind> fights(Chapter chapter) {
        List<EnemyKind> fights = new ArrayList<>();
        fights.add(chapter.getFirstEnemy());
        fights.addAll(chapter.getChallengers());
        fights.add(chapter.getBoss());
        return List.copyOf(fights);
    }

    /** @return le numéro du combat (de 1 à {@link #STAGES}) de l'entrée {@code fight} de {@link #fights(Chapter)}. */
    public static int stageNumber(Chapter chapter, int fight) {
        return fight == 0 ? 1 : fight == fights(chapter).size() - 1 ? STAGES : 2;
    }

    private boolean rivalWaiting;

    /**
     * @param waiting {@code true} si le Joueur Masqué attend encore le joueur dans l'Histoire (pas encore battu) :
     *                il prend alors la place d'un des challengers du chapitre {@link Chapter#RIVAL_CHAPTER}
     * @return cette ascension
     */
    public TowerRun withRival(boolean waiting) {
        this.rivalWaiting = waiting;
        return this;
    }

    /** @return le chapitre en cours. */
    public Chapter getChapter() { return chapter; }
    @Override
    public int getStage() { return stage; }
    @Override
    public int getStageCount() { return STAGES; }
    @Override
    public EnemyKind getEnemy() { return enemy; }
    /** @return la cagnotte du Jeu bonus de cette ascension du chapitre. */
    public BonusPot getPot() { return pot; }
    /** @return {@code true} en mode difficile. */
    @Override
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
        // Le Joueur Masqué se cache sous une des trois cartes (chapitre de la Maison, mode normal, tant qu'il n'est pas battu).
        if (rivalWaiting && !hard && chapter == Chapter.RIVAL_CHAPTER) shuffled.set(random.nextInt(shuffled.size()), EnemyKind.RIVAL_HISTOIRE);
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

    /** Défaite : le chapitre recommence au premier combat (ou au combat choisi en ADMIN), sa cagnotte effacée. */
    @Override
    public void restart() {
        pot.take();
        stage   = startStage;
        enemy   = startEnemy;
        choices = List.of();
    }
}
