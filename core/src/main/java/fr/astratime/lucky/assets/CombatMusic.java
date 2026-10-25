package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.exploration.DungeonRun;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.entities.run.CombatRun;
import fr.astratime.lucky.entities.tower.TowerRun;

import java.util.Map;

/**
 * La musique de chaque combat : une par lieu de l'Exploration, une pour la Tour
 * des épreuves et une pour ses boss. La Prairie, l'Entraînement et le tutoriel
 * gardent la musique de combat d'origine. Certains ennemis ont leur propre
 * musique (fournie par Astra, dans {@code assets/music/boss}), jouée pendant
 * leur combat à la place de celle du lieu ; la Comète Dorée et la Machine
 * Originelle en changent à leur phase 2.
 *
 * <p>Pour changer une musique, il suffit de remplacer son fichier dans
 * {@code assets/music} (même nom, en .ogg). Un fichier absent reprend la
 * musique de combat d'origine.</p>
 */
public final class CombatMusic {

    /** La musique de combat d'origine (fournie par Astra). */
    public static final String DEFAULT = "music/combat.ogg";
    public static final String PORT    = "music/combat_port.ogg";
    public static final String MINES   = "music/combat_mines.ogg";
    public static final String CASINO  = "music/combat_casino.ogg";
    public static final String FOIRE   = "music/combat_foire.ogg";
    public static final String DESERT  = "music/combat_desert.ogg";
    public static final String TOWER   = "music/combat_tour.ogg";
    public static final String BOSS    = "music/combat_boss.ogg";

    /** La musique de la Comète Dorée en phase 2, quand elle s'embrase sous la moitié de ses PV. */
    public static final String COMETE_PHASE_2  = "music/boss/comete_impact.mp3";
    /** La musique de la Machine Originelle à partir de sa phase 2. */
    public static final String MACHINE_PHASE_2 = "music/boss/machine_originelle_phase_2.mp3";

    /** Les ennemis de la Tour des épreuves qui ont leur propre musique (phase 1 pour la Machine). */
    static final Map<EnemyKind, String> TOWER_ENEMIES = Map.of(
        EnemyKind.CROUPIER,           "music/boss/croupier.mp3",
        EnemyKind.GARDIEN,            "music/boss/gardien.mp3",
        EnemyKind.SANGSUE,            "music/boss/sangsue.mp3",
        EnemyKind.BRETTEUR,           "music/boss/mousquetaire.mp3",
        EnemyKind.COMETE,             "music/boss/comete.mp3",
        EnemyKind.REINE,              "music/boss/reine_des_tables.mp3",
        EnemyKind.ECLAT,              "music/boss/eclat_originel.mp3",
        EnemyKind.PRETENDANT,         "music/boss/pretendant.mp3",
        EnemyKind.MACHINE_ORIGINELLE, "music/boss/machine_originelle_phase_1.mp3");

    /** Les ennemis de l'Exploration qui ont leur propre musique. */
    static final Map<EnemyKind, String> EXPLORATION_ENEMIES = Map.of(
        EnemyKind.ROI_PIQUE, "music/boss/roi_de_pique.mp3");

    private CombatMusic() {}

    /** @return la musique du combat en cours de {@code run} ({@code null} : Entraînement). */
    public static String of(CombatRun run) {
        if (run instanceof TowerRun tower) return tower.isBossStage() ? BOSS : TOWER;
        if (run instanceof DungeonRun dungeon) {
            return switch (Place.of(dungeon.getDungeon())) {
                case PRAIRIE -> DEFAULT;
                case PORT    -> PORT;
                case MINES   -> MINES;
                case CASINO  -> CASINO;
                case FOIRE   -> FOIRE;
                case DESERT  -> DESERT;
            };
        }
        return DEFAULT;
    }

    /**
     * @return la musique du combat de {@code run} contre {@code enemy} : la sienne
     *         s'il en a une (selon sa phase pour la Machine Originelle), sinon celle
     *         du lieu ({@link #of(CombatRun)})
     */
    public static String of(CombatRun run, Enemy enemy) {
        EnemyKind kind = enemy == null ? null : enemy.getKind();
        String own = null;
        if (run instanceof TowerRun) {
            boolean phaseTwo = enemy.getPhase() >= 2;
            own = phaseTwo && kind == EnemyKind.MACHINE_ORIGINELLE ? MACHINE_PHASE_2
                : phaseTwo && kind == EnemyKind.COMETE ? COMETE_PHASE_2 : TOWER_ENEMIES.get(kind);
        } else if (run instanceof DungeonRun) {
            own = EXPLORATION_ENEMIES.get(kind);
        }
        return own != null ? own : of(run);
    }
}
