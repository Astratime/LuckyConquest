package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.entities.tower.Chapter;
import fr.astratime.lucky.progress.PlayerProfileTest.MemoryStorage;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static fr.astratime.lucky.progress.PlayerProfileTest.profile;
import static org.junit.jupiter.api.Assertions.*;

/** Le bestiaire : ennemis rencontrés et vaincus, leur enregistrement, et les cinématiques à revoir. */
class BestiaryTest {

    @Test
    void theBestiaryListsEveryEnemyOnceInGameOrder() {
        List<EnemyKind> enemies = Bestiary.enemies();

        assertEquals(new HashSet<>(enemies).size(), enemies.size());
        assertEquals(1 + Chapter.values().length * 5 + Place.values().length * 8, enemies.size());
        assertEquals(EnemyKind.ENTRAINEMENT, enemies.get(0));
        for (EnemyKind kind : enemies) assertFalse(Bestiary.whereMet(kind).isBlank(), kind.name());
    }

    @Test
    void metAndDefeatedEnemiesAreSavedFromOneLaunchToTheNext() {
        MemoryStorage storage = new MemoryStorage();
        PlayerProfile profile = profile(storage);
        assertFalse(profile.hasMet(EnemyKind.CROUPIER));

        profile.meet(EnemyKind.CROUPIER);
        profile.meet(EnemyKind.GARDIEN);
        profile.defeat(EnemyKind.CROUPIER);

        PlayerProfile reloaded = profile(storage);
        assertTrue(reloaded.hasMet(EnemyKind.CROUPIER));
        assertTrue(reloaded.hasDefeated(EnemyKind.CROUPIER));
        assertTrue(reloaded.hasMet(EnemyKind.GARDIEN));
        assertFalse(reloaded.hasDefeated(EnemyKind.GARDIEN));
        assertFalse(reloaded.hasMet(EnemyKind.SANGSUE));
    }

    @Test
    void adminFightsDoNotFillTheBestiaryButAdminSeesEverything() {
        MemoryStorage storage = new MemoryStorage();
        PlayerProfile profile = profile(storage);
        profile.setAdmin(true);
        assertTrue(profile.hasDefeated(EnemyKind.KRAKEN));
        profile.meet(EnemyKind.KRAKEN);
        profile.defeat(EnemyKind.KRAKEN);
        profile.setAdmin(false);

        assertFalse(profile.hasMet(EnemyKind.KRAKEN));
        assertFalse(profile(storage).hasMet(EnemyKind.KRAKEN));
    }

    @Test
    void anOldSaveFindsTheEnemiesOfItsClearedDungeonsAndChapters() {
        MemoryStorage storage = new MemoryStorage();
        PlayerProfile old = profile(storage);
        Dungeon dungeon = Place.PRAIRIE.getDungeons().get(0);
        old.clearDungeon(dungeon.name());
        old.clearChapter(Chapter.GENESE, false);
        storage.values.remove(PlayerProfile.KEY_MET);       // une sauvegarde d'avant le bestiaire
        storage.values.remove(PlayerProfile.KEY_DEFEATED);

        PlayerProfile profile = profile(storage);
        assertTrue(profile.hasDefeated(dungeon.getSoldier()));
        assertTrue(profile.hasDefeated(dungeon.getKing()));
        assertTrue(profile.hasDefeated(Chapter.GENESE.getFirstEnemy()));
        assertTrue(profile.hasDefeated(Chapter.GENESE.getBoss()));
        assertFalse(profile.hasMet(Chapter.GENESE.getChallengers().get(0)));
        assertFalse(profile.hasMet(Place.PRAIRIE.getDungeons().get(1).getKing()));
    }

    @Test
    void cutscenesOpenOnceSeenInGame() {
        PlayerProfile profile = profile(new MemoryStorage());
        int[] none = Bestiary.cutscenes(profile);
        assertEquals(0, none[0]);
        int bosses = 0;
        for (Chapter chapter : Chapter.values()) bosses += chapter.getBoss().phaseCount() + 1;      // entrée, phases, mort
        for (Place place : Place.values()) {
            for (var dungeon : place.getDungeons()) bosses += dungeon.getKing().phaseCount() + 1;
        }
        bosses -= 2; // la Machine Originelle et l'Éclat Originel n'ont pas de cinématique de mort : leurs fins existent déjà
        assertEquals(bosses + 2 + Bestiary.ENDINGS.size() + 1, none[1]);

        profile.meet(Chapter.GENESE.getBoss());
        assertTrue(Bestiary.bossCutsceneSeen(profile, Chapter.GENESE.getBoss()));
        assertEquals(2, Bestiary.cutscenes(profile)[0], "son entrée et sa phase 2");
        assertFalse(Bestiary.deathSeen(profile, Chapter.GENESE.getBoss()));

        profile.setAdmin(true);
        assertEquals(none[1], Bestiary.cutscenes(profile)[0]);
    }

    @Test
    void theCollectionCountsOwnedCardsOfTheCatalog() {
        PlayerProfile profile = profile(new MemoryStorage());
        profile.setCatalog(List.of("carte_0", "carte_1", "pas_possedee"));
        int catalog = profile.getCatalogSize();

        assertTrue(catalog >= 3);
        assertEquals(2, profile.getCollectedCardCount());
        assertTrue(PlayerProfile.getReelCount() > 11);
    }
}
