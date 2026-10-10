package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.exploration.DungeonRun;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.entities.run.CombatRun;
import fr.astratime.lucky.entities.tower.TowerRun;

/**
 * La musique de chaque combat : une par lieu de l'Exploration, une pour la Tour
 * des épreuves et une pour ses boss. La Prairie, l'Entraînement et le tutoriel
 * gardent la musique de combat d'origine.
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
    public static final String TOWER   = "music/combat_tour.ogg";
    public static final String BOSS    = "music/combat_boss.ogg";

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
            };
        }
        return DEFAULT;
    }
}
