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

    public DungeonRun(Dungeon dungeon) {
        this.dungeon = dungeon;
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
    public void restart() { stage = 0; }

    @Override
    public String getLabel() { return dungeon.getName(); }
}
