package fr.astratime.lucky.entities.exploration;

import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.run.CombatRun;

/**
 * Une descente dans un donjon de l'Exploration : le soldat de la couleur, puis
 * son roi. Une défaite fait recommencer le donjon au soldat ; après le roi, le
 * joueur ouvre le coffre au trésor (voir {@link Dungeon#rollLoot}).
 */
public class DungeonRun implements CombatRun {

    /** Nombre de combats d'un donjon. */
    public static final int STAGES = 2;

    private final Dungeon dungeon;
    private int           stage;
    /** Combat où la descente commence et recommence après une défaite (0, sauf choix du mode ADMIN). */
    private final int     startStage;
    /** Cartes au trésor jouées pendant la descente (perdues si le donjon recommence). */
    private int           treasureMaps;

    public DungeonRun(Dungeon dungeon) {
        this(dungeon, 0);
    }

    /**
     * @param startStage combat du début (0 : le soldat, 1 : le roi), choisi en mode ADMIN ;
     *                   une défaite fait recommencer à ce combat
     */
    public DungeonRun(Dungeon dungeon, int startStage) {
        if (startStage < 0 || startStage >= STAGES) throw new IllegalArgumentException("Combat " + startStage);
        this.dungeon    = dungeon;
        this.startStage = startStage;
        this.stage      = startStage;
    }

    /** @return le donjon en cours. */
    public Dungeon getDungeon() { return dungeon; }

    @Override
    public EnemyKind getEnemy() { return stage == 0 ? dungeon.getSoldier() : dungeon.getKing(); }

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
    }

    @Override
    public String getLabel() { return dungeon.getName(); }

    @Override
    public PlaceRule getPlaceRule() { return Place.of(dungeon).getRule(); }

    /** Carte au trésor jouée : le coffre de ce donjon donnera une carte de plus. */
    public void addTreasureMap() { treasureMaps++; }

    /** @return les cartes que donnera le coffre : une, plus une par Carte au trésor jouée dans le donjon. */
    public int getChestCards() { return 1 + treasureMaps; }
}
