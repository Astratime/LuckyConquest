package fr.astratime.lucky.animations.cutscenes;

import fr.astratime.lucky.entities.enemy.EnemyKind;

/**
 * Les scènes uniques de phase et de mort de chaque boss et de chaque roi (idées validées
 * par Astra le 2026-10-24) : une classe par lieu.
 */
final class BossScenes {

    private BossScenes() {}

    /** @return la scène du boss {@code kind} qui entre dans sa phase {@code phase}, {@code null} s'il n'en a pas. */
    static BossScene phase(CutsceneKit kit, EnemyKind kind, int phase) {
        BossScene scene = TowerBossScenes.phase(kit, kind, phase);
        if (scene == null) scene = PrairieBossScenes.phase(kit, kind);
        if (scene == null) scene = PortBossScenes.phase(kit, kind);
        if (scene == null) scene = MinesBossScenes.phase(kit, kind);
        if (scene == null) scene = CasinoBossScenes.phase(kit, kind);
        return scene;
    }

    /** @return la scène de la mort du boss {@code kind}, {@code null} s'il n'en a pas. */
    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        BossScene scene = TowerBossScenes.death(kit, kind);
        if (scene == null) scene = PrairieBossScenes.death(kit, kind);
        if (scene == null) scene = PortBossScenes.death(kit, kind);
        if (scene == null) scene = MinesBossScenes.death(kit, kind);
        if (scene == null) scene = CasinoBossScenes.death(kit, kind);
        return scene;
    }
}
