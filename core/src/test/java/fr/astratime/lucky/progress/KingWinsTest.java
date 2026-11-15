package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.progress.PlayerProfileTest.MemoryStorage;
import org.junit.jupiter.api.Test;

import static fr.astratime.lucky.progress.PlayerProfileTest.profile;
import static org.junit.jupiter.api.Assertions.*;

/** Victoires contre chaque roi d'Exploration : à 10, sa carte se vend à la Boutique (proposition C3). */
class KingWinsTest {

    @Test
    void winsAgainstAKingAreCountedSavedAndOpenItsCardAtTen() {
        MemoryStorage storage = new MemoryStorage();
        PlayerProfile profile = profile(storage);
        assertEquals(0, profile.getKingWins(EnemyKind.DRAGON));
        for (int i = 0; i < PlayerProfile.KING_CARD_WINS - 1; i++) profile.winAgainstKing(EnemyKind.DRAGON);
        assertFalse(profile.isKingCardOpen(EnemyKind.DRAGON));
        profile.winAgainstKing(EnemyKind.DRAGON);
        assertTrue(profile.isKingCardOpen(EnemyKind.DRAGON));

        PlayerProfile reloaded = profile(storage);
        assertEquals(PlayerProfile.KING_CARD_WINS, reloaded.getKingWins(EnemyKind.DRAGON), "enregistré");
        assertFalse(reloaded.isKingCardOpen(EnemyKind.ROI_PIQUE));
    }

    @Test
    void anOldSaveCountsOneWinForEachKingAlreadyBeaten() {
        MemoryStorage storage = new MemoryStorage();
        PlayerProfile profile = profile(storage);
        profile.defeat(EnemyKind.ROI_COEUR);
        storage.values.remove(PlayerProfile.KEY_KING_WINS); // sauvegarde d'avant le compteur
        PlayerProfile old = profile(storage);
        assertEquals(1, old.getKingWins(EnemyKind.ROI_COEUR));
        assertEquals(0, old.getKingWins(EnemyKind.ROI_PIQUE));
    }
}
