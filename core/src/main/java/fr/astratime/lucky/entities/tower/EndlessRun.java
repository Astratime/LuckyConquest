package fr.astratime.lucky.entities.tower;

import fr.astratime.lucky.entities.BonusPot;
import fr.astratime.lucky.entities.enemy.Difficulty;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.run.CombatRun;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Le mode sans fin de la Tour des épreuves : des étages sans limite, un combat
 * par étage, de plus en plus forts (voir {@link EnemyKind#endlessPower(int)}).
 * Dès l'étage 1, les ennemis sont plus forts qu'au chapitre 6 en mode difficile.
 * <ul>
 *   <li>Étage 1 : un ennemi de la Tour tiré au hasard.</li>
 *   <li>Après chaque étage gagné : le joueur choisit son adversaire parmi trois
 *       cartes faces cachées (ennemis des six chapitres, hors boss).</li>
 *   <li>Tous les {@link #BOSS_EVERY} étages : un boss de la Tour, à tour de rôle
 *       ({@link #BOSSES}).</li>
 * </ul>
 * Comme dans la Tour, le joueur garde ses PV, ses gains et ses cartes d'un étage
 * à l'autre, et ne gagne aucune pièce. Une défaite fait recommencer à l'étage 1 ;
 * le plus haut étage gagné est le record ({@link fr.astratime.lucky.progress.PlayerProfile#getEndlessBest()}).
 */
public class EndlessRun implements CombatRun {

    /** Un boss tous les N étages. */
    public static final int BOSS_EVERY = 5;
    /** Adversaires proposés après chaque étage. */
    public static final int CHOICES = 3;

    /**
     * Boss du mode sans fin, dans l'ordre. La Machine Originelle n'y est pas :
     * son dernier tirage et son duel du levier finissent l'histoire.
     */
    public static final List<EnemyKind> BOSSES = List.of(Chapter.GENESE.getBoss(), Chapter.TABLES_SACREES.getBoss(),
        Chapter.DERNIER_TIRAGE.getBoss(), Chapter.MONDE_SANS_MAITRE.getBoss(), Chapter.LA_MAISON.getBoss());

    /** Ennemis des étages sans boss : les premiers ennemis et les challengers des six chapitres. */
    public static final List<EnemyKind> ENEMIES;
    static {
        List<EnemyKind> enemies = new ArrayList<>();
        for (Chapter chapter : Chapter.values()) {
            enemies.add(chapter.getFirstEnemy());
            enemies.addAll(chapter.getChallengers());
        }
        ENEMIES = List.copyOf(enemies);
    }

    private final Random    random;
    /** Étage en cours, à partir de 1. */
    private int             floor;
    private EnemyKind       enemy;
    private List<EnemyKind> choices = List.of();
    /** Cagnotte du Jeu bonus de cette montée : partagée par ses étages, effacée après une défaite. */
    private final BonusPot  pot = new BonusPot();

    public EndlessRun() {
        this(new Random());
    }

    /** @param random ennemis tirés (graine fixe pour les tests) */
    public EndlessRun(Random random) {
        this.random = random;
        start();
    }

    /** Étage 1, contre un ennemi tiré au hasard. */
    private void start() {
        floor   = 1;
        enemy   = ENEMIES.get(random.nextInt(ENEMIES.size()));
        choices = List.of();
    }

    /** @return l'étage en cours, à partir de 1. */
    public int getFloor() { return floor; }

    /** @return {@code true} si l'étage {@code floor} est celui d'un boss. */
    public static boolean isBossFloor(int floor) { return floor % BOSS_EVERY == 0; }

    /** @return le boss de l'étage {@code floor} (un étage de boss). */
    public static EnemyKind bossOf(int floor) { return BOSSES.get((floor / BOSS_EVERY - 1) % BOSSES.size()); }

    /** @return la cagnotte du Jeu bonus de cette montée. */
    public BonusPot getPot() { return pot; }

    @Override
    public EnemyKind getEnemy() { return enemy; }

    @Override
    public int getStage() { return floor - 1; }

    /** @return l'étage suivant compris : le mode n'a pas de fin, le compte grandit avec lui. */
    @Override
    public int getStageCount() { return floor + 1; }

    @Override
    public boolean isBossStage() { return isBossFloor(floor); }

    @Override
    public boolean isHard() { return true; }

    @Override
    public Difficulty getDifficulty() { return Difficulty.endless(floor); }

    @Override
    public String getLabel() { return Lang.t("Mode sans fin"); }

    @Override
    public Next win() {
        floor++;
        if (isBossFloor(floor)) {
            enemy   = bossOf(floor);
            choices = List.of();
            return Next.BOSS;
        }
        List<EnemyKind> shuffled = new ArrayList<>(ENEMIES);
        shuffled.remove(enemy); // pas deux fois de suite le même adversaire
        Collections.shuffle(shuffled, random);
        choices = List.copyOf(shuffled.subList(0, CHOICES));
        return Next.CHOOSE_ENEMY;
    }

    @Override
    public List<EnemyKind> getChoices() { return choices; }

    @Override
    public EnemyKind choose(int index) {
        if (choices.isEmpty()) throw new IllegalStateException("Aucun choix d'adversaire en cours");
        enemy   = choices.get(index);
        choices = List.of();
        return enemy;
    }

    /** Défaite : la montée recommence à l'étage 1, sa cagnotte effacée. */
    @Override
    public void restart() {
        pot.take();
        start();
    }
}
