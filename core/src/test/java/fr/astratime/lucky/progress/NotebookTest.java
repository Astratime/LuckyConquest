package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.entities.tower.Chapter;
import fr.astratime.lucky.i18n.Lang;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Le carnet du Croupier : une page par boss de la Tour et par roi de l'Exploration, lue une fois vaincu. */
class NotebookTest {

    @Test
    void everyTowerBossAndEveryKingLeavesOnePage() {
        Set<EnemyKind> expected = new LinkedHashSet<>();
        for (Chapter chapter : Chapter.values()) expected.add(chapter.getBoss());
        for (Place place : Place.values()) for (Dungeon dungeon : place.getDungeons()) expected.add(dungeon.getKing());
        assertEquals(expected, new LinkedHashSet<>(Notebook.owners()));
        assertEquals(30, Notebook.size());
        assertEquals(1, Notebook.number(EnemyKind.COMETE));
        assertEquals(0, Notebook.number(EnemyKind.SOLDAT_PIQUE));
        assertFalse(Notebook.hasPage(EnemyKind.GRAND_CROUPIER));
        for (EnemyKind kind : Notebook.owners()) assertFalse(Notebook.text(kind).contains("œ"), "la police n'a pas de œ");
    }

    @Test
    void everyPageIsTranslated() throws Exception {
        String json = java.nio.file.Files.readString(java.nio.file.Path.of("..", "assets", "i18n", "en.json"), java.nio.charset.StandardCharsets.UTF_8);
        java.util.Map<String, String> english = Lang.parse(json);
        for (EnemyKind kind : Notebook.owners()) {
            String french = Notebook.text(kind);
            assertTrue(english.containsKey(french), kind.name() + " : page sans traduction anglaise");
            assertFalse(english.get(french).isBlank(), kind.name());
        }
    }

    @Test
    void aPageIsFoundWhenItsBossIsBeaten() {
        PlayerProfile profile = PlayerProfileTest.profile(new PlayerProfileTest.MemoryStorage());
        assertEquals(0, Notebook.found(profile));
        profile.defeat(EnemyKind.DRAGON);
        profile.defeat(EnemyKind.SOLDAT_PIQUE);
        assertEquals(1, Notebook.found(profile));
    }

    @Test
    void theCroupierOpensOnceTheDesertIsEmptyAndHardChapterSixIsDone() {
        PlayerProfile profile = PlayerProfileTest.profile(new PlayerProfileTest.MemoryStorage());
        assertFalse(profile.isCroupierOpen());
        profile.clearChapter(Chapter.LE_JACKPOT, true);
        assertFalse(profile.isCroupierOpen(), "le Désert doit aussi être vidé");
        for (Dungeon dungeon : Place.DESERT.getDungeons()) profile.clearDungeon(dungeon.name());
        assertTrue(profile.isCroupierOpen());

        PlayerProfile desertOnly = PlayerProfileTest.profile(new PlayerProfileTest.MemoryStorage());
        for (Dungeon dungeon : Place.DESERT.getDungeons()) desertOnly.clearDungeon(dungeon.name());
        assertFalse(desertOnly.isCroupierOpen(), "le chapitre 6 difficile doit aussi être fini");
        desertOnly.setAdmin(true);
        assertTrue(desertOnly.isCroupierOpen());
    }
}
