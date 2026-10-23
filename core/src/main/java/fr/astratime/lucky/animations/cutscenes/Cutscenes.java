package fr.astratime.lucky.animations.cutscenes;

import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.tower.Chapter;

/**
 * Quelle cinématique joue quand : avant chaque boss de la Tour des épreuves,
 * avant chaque roi de l'Exploration, à chaque nouvelle phase d'un boss ou d'un roi,
 * à sa mort, et à la fin des chapitres 3 et 6.
 */
public final class Cutscenes {

    private Cutscenes() {}

    /** @return la cinématique avant le boss ou le roi {@code boss} ({@code null} s'il n'en a pas). */
    public static Cutscene beforeBoss(CutsceneKit kit, EnemyKind boss) {
        return switch (boss) {
            case COMETE -> new CometCutscene(kit.settings(), kit.shake(), kit.sound("comet"));
            case REINE -> new QueenCutscene(kit);
            case ECLAT -> new ShardCutscene(kit);
            case PRETENDANT -> new PretenderCutscene(kit);
            case MAISON -> new HouseCutscene(kit);
            case MACHINE_ORIGINELLE -> new MachineCutscene(kit);
            default -> KingEntrance.of(kit, boss);
        };
    }

    /**
     * @return la cinématique du boss ou du roi {@code boss} qui passe à sa phase {@code phase}
     *         ({@code null} si ce n'est pas un boss ou s'il n'a pas cette phase)
     */
    public static Cutscene phase(CutsceneKit kit, EnemyKind boss, int phase) {
        if (!boss.isBoss() || phase < 2 || phase > boss.phaseCount()) return null;
        return switch (boss) {
            case COMETE -> new CometPhaseCutscene(kit);
            case MACHINE_ORIGINELLE -> new MachinePhaseCutscene(kit, phase);
            default -> BossMomentCutscene.phase(kit, boss, phase);
        };
    }

    /** @return la cinématique de la mort du boss ou du roi {@code boss} ({@code null} si ce n'est pas un boss). */
    public static Cutscene death(CutsceneKit kit, EnemyKind boss) {
        return boss.isBoss() ? BossMomentCutscene.death(kit, boss) : null;
    }

    /** @return la cinématique de fin du chapitre {@code chapter}, quand il clôt l'histoire ({@code null} sinon). */
    public static Cutscene ending(CutsceneKit kit, Chapter chapter) {
        return switch (chapter) {
            case DERNIER_TIRAGE -> new ShardEndingCutscene(kit);
            case LE_JACKPOT -> new JackpotEndingCutscene(kit);
            default -> null;
        };
    }
}
