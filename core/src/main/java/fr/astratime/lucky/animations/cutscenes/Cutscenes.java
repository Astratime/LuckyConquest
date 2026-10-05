package fr.astratime.lucky.animations.cutscenes;

import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.tower.Chapter;

/**
 * Quelle cinématique joue quand : avant chaque boss de la Tour des épreuves,
 * avant chaque roi de l'Exploration, et à la fin des chapitres 3 et 6.
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

    /** @return la cinématique de fin du chapitre {@code chapter}, quand il clôt l'histoire ({@code null} sinon). */
    public static Cutscene ending(CutsceneKit kit, Chapter chapter) {
        return switch (chapter) {
            case DERNIER_TIRAGE -> new ShardEndingCutscene(kit);
            case LE_JACKPOT -> new JackpotEndingCutscene(kit);
            default -> null;
        };
    }
}
