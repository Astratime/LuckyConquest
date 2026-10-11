package fr.astratime.lucky.progress;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Les trois emplacements de sauvegarde : la partie d'avant reste dans le 1, le dernier joué, l'effacement. */
class SaveSlotsTest {

    @TempDir Path folder;

    private void save(SaveSlots slots, int slot, String rank) {
        SecureProfileStorage storage = new SecureProfileStorage(slots.file(slot), Map::of, () -> { });
        storage.put(PlayerProfile.KEY_RANK, rank);
        storage.flush();
    }

    @Test
    void theSaveFromBeforeTheSlotsIsSlotOne() {
        SaveSlots slots = new SaveSlots(folder);
        assertEquals(folder.resolve("lucky-conquest-save"), slots.file(1));
        assertNotEquals(slots.file(1), slots.file(2));
        assertNotEquals(slots.file(2), slots.file(3));
        assertThrows(IllegalArgumentException.class, () -> slots.file(4));
    }

    @Test
    void emptySlotsAreFreeAndNothingIsToContinue() {
        SaveSlots slots = new SaveSlots(folder);
        assertEquals(List.of(), slots.usedSlots());
        assertEquals(0, slots.lastUsed());
        assertNull(slots.peek(2));
    }

    @Test
    void aSavedGameShowsItsRank() {
        SaveSlots slots = new SaveSlots(folder);
        save(slots, 2, "2");
        save(slots, 3, "0");
        assertEquals(List.of(2, 3), slots.usedSlots());
        assertEquals(Rank.PARIEUR, slots.rank(2));
        assertNull(slots.rank(3), "aucun rang acheté");
    }

    @Test
    void continueTakesTheLastPlayedSlotOrElseTheLatestSave() throws Exception {
        SaveSlots slots = new SaveSlots(folder);
        save(slots, 1, "0");
        save(slots, 3, "0");
        Files.setLastModifiedTime(slots.file(1), FileTime.fromMillis(2_000_000));
        Files.setLastModifiedTime(slots.file(3), FileTime.fromMillis(1_000_000));
        assertEquals(1, slots.lastUsed(), "rien de noté : la sauvegarde la plus récente");

        slots.setLastUsed(3);
        assertEquals(3, new SaveSlots(folder).lastUsed());

        slots.setLastUsed(2);
        assertEquals(1, slots.lastUsed(), "l'emplacement noté est vide");
    }

    @Test
    void erasingASlotLeavesTheOthers() {
        SaveSlots slots = new SaveSlots(folder);
        save(slots, 1, "1");
        save(slots, 1, "2"); // la première sauvegarde devient la copie de secours
        save(slots, 2, "3");
        slots.erase(1);
        assertFalse(slots.isUsed(1));
        assertFalse(Files.exists(folder.resolve("lucky-conquest-save.bak")));
        assertEquals(Rank.FLAMBEUR, slots.rank(2));
    }
}
