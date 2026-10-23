package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.entities.tower.Chapter;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Le bestiaire (Options > Bestiaire et collection) : tous les ennemis qu'on
 * peut combattre, dans l'ordre du jeu, où on les trouve, et les cinématiques
 * qu'ils ouvrent à la revoir.
 *
 * Hors mode ADMIN, une cinématique se revoit une fois vue en jeu : celle d'un
 * boss ou d'un roi une fois rencontré, le duel des leviers une fois la Machine
 * Originelle battue, une fin une fois son chapitre terminé, le Jeu bonus une
 * fois joué.
 */
public final class Bestiary {

    private Bestiary() {}

    /**
     * @return les ennemis du bestiaire : l'Entraînement, la Tour des épreuves
     *         chapitre par chapitre, puis l'Exploration lieu par lieu, donjon par donjon
     */
    public static List<EnemyKind> enemies() {
        Set<EnemyKind> enemies = new LinkedHashSet<>();
        enemies.add(EnemyKind.ENTRAINEMENT);
        for (Chapter chapter : Chapter.values()) {
            enemies.add(chapter.getFirstEnemy());
            enemies.addAll(chapter.getChallengers());
            enemies.add(chapter.getBoss());
        }
        for (Place place : Place.values()) {
            for (Dungeon dungeon : place.getDungeons()) {
                enemies.add(dungeon.getSoldier());
                enemies.add(dungeon.getKing());
            }
        }
        return new ArrayList<>(enemies);
    }

    /** @return où l'on combat {@code kind} (ex : « La prairie : Donjon de Pique », « Tour des épreuves, chapitre 2 »). */
    public static String whereMet(EnemyKind kind) {
        if (kind == EnemyKind.ENTRAINEMENT) return Lang.t("Entraînement");
        for (Place place : Place.values()) {
            for (Dungeon dungeon : place.getDungeons()) {
                if (dungeon.getSoldier() == kind || dungeon.getKing() == kind) {
                    return Lang.f("{0} : {1}", place.getName(), dungeon.getName());
                }
            }
        }
        for (Chapter chapter : Chapter.values()) {
            if (chapter.getFirstEnemy() == kind || chapter.getChallengers().contains(kind) || chapter.getBoss() == kind) {
                return Lang.f("Tour des épreuves, {0}", chapter.getLabel().toLowerCase());
            }
        }
        return "";
    }

    // -------------------------------------------------------------------------
    // Cinématiques à revoir
    // -------------------------------------------------------------------------

    /** @return {@code true} si la cinématique d'entrée du boss ou du roi {@code boss} a été vue. */
    public static boolean bossCutsceneSeen(PlayerProfile profile, EnemyKind boss) { return profile.hasMet(boss); }

    /** @return {@code true} si la cinématique de la mort du boss ou du roi {@code boss} a été vue (il a été battu). */
    public static boolean deathSeen(PlayerProfile profile, EnemyKind boss) { return profile.hasDefeated(boss); }

    /** @return {@code true} si le duel des leviers a été vu (la Machine Originelle battue). */
    public static boolean duelSeen(PlayerProfile profile) { return profile.hasDefeated(EnemyKind.MACHINE_ORIGINELLE); }

    /** @return {@code true} si la fin du chapitre {@code chapter} a été vue (le chapitre terminé, dans un des modes). */
    public static boolean endingSeen(PlayerProfile profile, Chapter chapter) {
        return profile.isAdmin() || profile.isCleared(chapter, false) || profile.isCleared(chapter, true);
    }

    /** @return {@code true} si un Jeu bonus a été joué. */
    public static boolean bonusGameSeen(PlayerProfile profile) {
        return profile.isAdmin() || profile.getStats().get(PlayerStats.Stat.BONUS_GAMES) > 0;
    }

    /** Chapitres qui finissent sur une cinématique (voir {@code Cutscenes.ending}). */
    public static final List<Chapter> ENDINGS = List.of(Chapter.DERNIER_TIRAGE, Chapter.LE_JACKPOT);

    /**
     * @return {seen, total} : les cinématiques déjà vues et toutes celles du jeu (le duel en compte deux ;
     *         chaque boss, son entrée, une par nouvelle phase et sa mort)
     */
    public static int[] cutscenes(PlayerProfile profile) {
        int seen = 0, total = 0;
        List<EnemyKind> bosses = new ArrayList<>();
        for (Chapter chapter : Chapter.values()) bosses.add(chapter.getBoss());
        for (Place place : Place.values()) for (Dungeon dungeon : place.getDungeons()) bosses.add(dungeon.getKing());
        for (EnemyKind boss : bosses) {
            int phases = boss.phaseCount() - 1;          // une cinématique par nouvelle phase (voir Cutscenes.phase)
            boolean death = boss != EnemyKind.MACHINE_ORIGINELLE; // sa fin : le duel des leviers et « Le Jackpot »
            total += 1 + phases + (death ? 1 : 0);       // l'entrée, les phases, la mort
            if (bossCutsceneSeen(profile, boss)) seen += 1 + phases;
            if (death && deathSeen(profile, boss)) seen++;
        }
        total += 2;
        if (duelSeen(profile)) seen += 2;
        for (Chapter chapter : ENDINGS) {
            total++;
            if (endingSeen(profile, chapter)) seen++;
        }
        total++;
        if (bonusGameSeen(profile)) seen++;
        return new int[] {seen, total};
    }
}
