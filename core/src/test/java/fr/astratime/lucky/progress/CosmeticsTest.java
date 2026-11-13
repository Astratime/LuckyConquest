package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.enemy.EnemyKind;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Cosmétiques : possédés d'origine, débloqués par le bestiaire ou achetés, choisis et enregistrés. */
class CosmeticsTest {

    private static PlayerProfile profile(PlayerProfileTest.MemoryStorage storage) {
        return PlayerProfileTest.profile(storage);
    }

    @Test
    void aNewPlayerWearsTheOriginalCosmeticsAndOwnsOnlyThem() {
        PlayerProfile profile = profile(new PlayerProfileTest.MemoryStorage());

        for (Cosmetic.Kind kind : Cosmetic.Kind.values()) {
            assertEquals(Cosmetic.defaultOf(kind), profile.getCosmetic(kind));
        }
        for (Cosmetic cosmetic : Cosmetic.values()) {
            assertEquals(cosmetic.isDefault(), profile.ownsCosmetic(cosmetic), cosmetic.name());
        }
        assertFalse(profile.chooseCosmetic(Cosmetic.BACK_NEON));
        assertEquals(Cosmetic.BACK_CLASSIC, profile.getCosmetic(Cosmetic.Kind.BACK));
    }

    @Test
    void eachKindHasOneOriginalAndEveryStyleHasItsImages() {
        for (Cosmetic.Kind kind : Cosmetic.Kind.values()) {
            assertEquals(1, List.of(Cosmetic.values()).stream()
                .filter(cosmetic -> cosmetic.getKind() == kind && cosmetic.isDefault()).count());
        }
        assertNull(Cosmetic.BACK_CLASSIC.getBackPath());
        assertEquals("cosmetics/neon/back.png", Cosmetic.BACK_NEON.getBackPath());
        assertEquals("cosmetics/or/felt.png", Cosmetic.FELT_GOLD.getFeltPath());
        assertEquals("cosmetics/abysses/", Cosmetic.MACHINE_ABYSS.getMachineFolder());
        assertNull(Cosmetic.BACK_NEON.getFeltPath());
    }

    @Test
    void buyingACosmeticCostsItsPriceWearsItAndIsSaved() {
        PlayerProfileTest.MemoryStorage storage = new PlayerProfileTest.MemoryStorage();
        PlayerProfile profile = profile(storage);
        long price = Cosmetic.FELT_WOOD.getGoal();

        assertFalse(profile.buyCosmetic(Cosmetic.FELT_WOOD), "pas assez de pièces");
        profile.addCoins(price + 5);
        assertFalse(profile.buyCosmetic(Cosmetic.FELT_NEON), "un cosmétique de succès ne s'achète pas");
        assertTrue(profile.buyCosmetic(Cosmetic.FELT_WOOD));
        assertEquals(5L, profile.getCoins());
        assertEquals(Cosmetic.FELT_WOOD, profile.getCosmetic(Cosmetic.Kind.FELT));
        assertFalse(profile.buyCosmetic(Cosmetic.FELT_WOOD), "déjà acheté");

        PlayerProfile relaunched = profile(storage);
        assertTrue(relaunched.ownsCosmetic(Cosmetic.FELT_WOOD));
        assertEquals(Cosmetic.FELT_WOOD, relaunched.getCosmetic(Cosmetic.Kind.FELT));
        assertTrue(relaunched.chooseCosmetic(Cosmetic.FELT_CLASSIC));
        assertEquals(Cosmetic.FELT_CLASSIC, profile(storage).getCosmetic(Cosmetic.Kind.FELT));
    }

    @Test
    void bestiaryCosmeticsUnlockWithDefeatedEnemies() {
        PlayerProfile profile = profile(new PlayerProfileTest.MemoryStorage());
        List<EnemyKind> enemies = fr.astratime.lucky.progress.Bestiary.enemies();
        int goal = (int) Cosmetic.BACK_ABYSS.getGoal();

        for (int i = 0; i < goal - 1; i++) profile.defeat(enemies.get(i));
        assertFalse(profile.ownsCosmetic(Cosmetic.BACK_ABYSS));
        assertArrayEquals(new int[]{goal - 1, goal}, profile.getCosmeticProgress(Cosmetic.BACK_ABYSS));
        profile.defeat(enemies.get(goal - 1));
        assertTrue(profile.ownsCosmetic(Cosmetic.BACK_ABYSS));
        assertTrue(profile.chooseCosmetic(Cosmetic.BACK_ABYSS));

        assertEquals(enemies.size(), profile.getCosmeticProgress(Cosmetic.MACHINE_ABYSS)[1]);
        enemies.forEach(profile::defeat);
        assertTrue(profile.ownsCosmetic(Cosmetic.MACHINE_ABYSS));
    }

    @Test
    void adminWearsEverythingWithoutBuyingOrSavingTheChoice() {
        PlayerProfileTest.MemoryStorage storage = new PlayerProfileTest.MemoryStorage();
        PlayerProfile profile = profile(storage);
        profile.addCoins(Cosmetic.BACK_GOLD.getGoal());
        profile.setAdmin(true);

        assertTrue(profile.ownsCosmetic(Cosmetic.MACHINE_GOLD));
        assertFalse(profile.buyCosmetic(Cosmetic.BACK_GOLD));
        assertTrue(profile.chooseCosmetic(Cosmetic.MACHINE_GOLD));
        assertEquals(Cosmetic.MACHINE_GOLD, profile.getCosmetic(Cosmetic.Kind.MACHINE));

        profile.setAdmin(false);
        assertEquals(Cosmetic.MACHINE_CLASSIC, profile.getCosmetic(Cosmetic.Kind.MACHINE));
        assertEquals(Cosmetic.MACHINE_CLASSIC, profile(storage).getCosmetic(Cosmetic.Kind.MACHINE));
        assertEquals(Cosmetic.BACK_GOLD.getGoal(), profile.getCoins());
    }
}
