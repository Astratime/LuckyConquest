package fr.astratime.lucky.entities.exploration;

import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.run.CombatRun;

import java.util.Random;

/**
 * Une descente dans un donjon de l'Exploration : le soldat de la couleur (ou parfois le
 * Joueur Masqué à sa place, voir {@link Rival}), puis
 * son roi, avec entre les deux le carrefour (voir {@link Crossroads}). Une défaite fait recommencer le donjon au soldat ; après le roi, le
 * joueur ouvre le coffre au trésor (voir {@link Dungeon#rollLoot}).
 */
public class DungeonRun implements CombatRun {

    /** Nombre de combats d'un donjon. */
    public static final int STAGES = 2;
    /** Un soldat sur {@value} arrive doré (voir {@link fr.astratime.lucky.entities.Enemy#gild()}). */
    public static final int GOLDEN_CHANCE = 20;

    private final Dungeon dungeon;
    private int           stage;
    /** Combat où la descente commence et recommence après une défaite (0, sauf choix du mode ADMIN). */
    private final int     startStage;
    /** Cartes au trésor jouées pendant la descente (perdues si le donjon recommence). */
    private int           treasureMaps;
    /** Le Joueur Masqué, qui prend la place du soldat dans cette descente (voir {@link Rival}), ou {@code null}. */
    private final EnemyKind  rival;
    /** Le carrefour entre le soldat et le roi, et ce qu'il donne contre le roi. */
    private final Crossroads crossroads = new Crossroads(new Random());
    /** Mode ADMIN : le soldat arrive toujours doré. */
    private final boolean    goldenSoldier;

    public DungeonRun(Dungeon dungeon) {
        this(dungeon, 0);
    }

    /**
     * @param startStage combat du début (0 : le soldat, 1 : le roi), choisi en mode ADMIN ;
     *                   une défaite fait recommencer à ce combat
     */
    public DungeonRun(Dungeon dungeon, int startStage) {
        this(dungeon, startStage, false, null);
    }

    /**
     * @param goldenSoldier {@code true} : le soldat arrive toujours doré (choix du mode ADMIN)
     */
    public DungeonRun(Dungeon dungeon, int startStage, boolean goldenSoldier) {
        this(dungeon, startStage, goldenSoldier, null);
    }

    /**
     * @param startStage combat du début (0 : le premier, 1 : le roi)
     * @param rival      le Joueur Masqué, qui prend la place du soldat (il revient si le donjon recommence),
     *                   ou {@code null} pour le soldat habituel
     */
    public DungeonRun(Dungeon dungeon, int startStage, EnemyKind rival) {
        this(dungeon, startStage, false, rival);
    }

    private DungeonRun(Dungeon dungeon, int startStage, boolean goldenSoldier, EnemyKind rival) {
        if (rival != null && !rival.isRival()) throw new IllegalArgumentException("Pas le Joueur Masqué : " + rival);
        this.goldenSoldier = goldenSoldier;
        if (startStage < 0 || startStage >= STAGES) throw new IllegalArgumentException("Combat " + startStage);
        this.dungeon    = dungeon;
        this.startStage = startStage;
        this.stage      = startStage;
        this.rival      = rival;
    }

    /** @return le donjon en cours. */
    public Dungeon getDungeon() { return dungeon; }

    @Override
    public EnemyKind getEnemy() {
        if (stage > 0) return dungeon.getKing();
        return rival != null ? rival : dungeon.getSoldier();
    }

    /** @return le Joueur Masqué de cette descente, à la place du soldat, ou {@code null}. */
    public EnemyKind getRival() { return rival; }

    @Override
    public int getStage() { return stage; }

    @Override
    public int getStageCount() { return STAGES; }

    @Override
    public Next win() {
        if (isBossStage()) return Next.CLEARED;
        stage++;
        return Next.BOSS;
    }

    @Override
    public void restart() {
        stage = startStage;
        treasureMaps = 0;
        crossroads.reset();
    }

    @Override
    public String getLabel() { return dungeon.getName(); }

    @Override
    public PlaceRule getPlaceRule() { return Place.of(dungeon).getRule(); }

    /**
     * Début du combat en cours : le soldat arrive doré une fois sur {@value #GOLDEN_CHANCE}
     * (toujours si le mode ADMIN l'a choisi) ; le roi, jamais.
     *
     * @return {@code true} si l'ennemi de ce combat est doré
     */
    public boolean rollGolden(Random random) {
        if (stage != 0 || rival != null) return false; // le Joueur Masqué n'est jamais doré
        return goldenSoldier || random.nextInt(GOLDEN_CHANCE) == 0;
    }

    /** @return le carrefour du donjon, entre le soldat et le roi. */
    public Crossroads getCrossroads() { return crossroads; }

    /** Carte au trésor jouée : le coffre de ce donjon donnera une carte de plus. */
    public void addTreasureMap() { treasureMaps++; }

    /** @return les cartes que donnera le coffre : une, plus une par Carte au trésor jouée dans le donjon. */
    public int getChestCards() { return 1 + treasureMaps; }
}
