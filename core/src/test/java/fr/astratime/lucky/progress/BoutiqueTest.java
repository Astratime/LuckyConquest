package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.RankBonus;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.loaders.CardLoader;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static fr.astratime.lucky.progress.PlayerProfileTest.MemoryStorage;
import static fr.astratime.lucky.progress.PlayerProfileTest.profile;
import static org.junit.jupiter.api.Assertions.*;

/** La boutique : rangs, cartes et rouleaux achetés avec les pièces, et la machine du joueur. */
class BoutiqueTest {

    private static final CardLoader.AssetReader READER = path -> {
        try {
            return java.nio.file.Files.readString(java.nio.file.Path.of("..", "assets").resolve(path),
                java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    };

    @Test
    void thereAreTenRanksAndTheFirstIsTheMiser() {
        assertEquals(10, Rank.values().length);
        assertEquals("Avare", Rank.AVARE.getTitle());
        assertEquals(new RankBonus(100, 100, 10, 35, 70), Rank.AVARE.getBonus());
        assertEquals(4_400_000L, Rank.AVARE.getPrice());
        for (int i = 1; i < Rank.values().length; i++) {
            Rank previous = Rank.values()[i - 1], rank = Rank.values()[i];
            assertTrue(rank.getPrice() > previous.getPrice(), rank.name());
            assertTrue(rank.getBonus().hp() > previous.getBonus().hp(), rank.name());
        }
    }

    @Test
    void ranksAreBoughtInOrderWithCoinsAndKeptAfterARestart() {
        MemoryStorage storage = new MemoryStorage();
        PlayerProfile profile = profile(storage);
        assertNull(profile.getRank());
        assertEquals(RankBonus.NONE, profile.getRankBonus());

        profile.addCoins(Rank.AVARE.getPrice() - 1);
        assertFalse(profile.buyNextRank(), "pas assez de pièces");
        profile.addCoins(1 + Rank.PARIEUR.getPrice());
        assertTrue(profile.buyNextRank());
        assertEquals(Rank.AVARE, profile.getRank());
        assertEquals(Rank.PARIEUR, profile.getNextRank());
        assertTrue(profile.buyNextRank());
        assertEquals(0L, profile.getCoins());

        PlayerProfile reloaded = profile(storage);
        assertEquals(Rank.PARIEUR, reloaded.getRank());
        assertEquals(Rank.PARIEUR.getBonus(), reloaded.getRankBonus());
    }

    @Test
    void thePlayerPlaysWithAnyOwnedRankAndTheChoiceIsSaved() {
        MemoryStorage storage = new MemoryStorage();
        PlayerProfile profile = profile(storage);
        profile.addCoins(Rank.AVARE.getPrice() + Rank.PARIEUR.getPrice() + Rank.FLAMBEUR.getPrice());
        for (int i = 0; i < 3; i++) assertTrue(profile.buyNextRank());
        assertEquals(Rank.FLAMBEUR, profile.getActiveRank(), "un rang acheté se joue tout de suite");

        assertFalse(profile.chooseRank(Rank.HABITUE), "rang pas encore acheté");
        assertEquals(Rank.FLAMBEUR, profile.getActiveRank());
        assertTrue(profile.chooseRank(Rank.AVARE));
        assertEquals(Rank.AVARE.getBonus(), profile.getRankBonus());
        assertEquals(Rank.FLAMBEUR, profile.getRank(), "le plus haut rang acheté ne change pas");

        PlayerProfile reloaded = profile(storage);
        assertEquals(Rank.AVARE, reloaded.getActiveRank());
        assertEquals(Rank.AVARE.getBonus(), reloaded.getRankBonus());

        assertTrue(reloaded.chooseRank(null));
        assertEquals(RankBonus.NONE, reloaded.getRankBonus());
        assertNull(profile(storage).getActiveRank());
    }

    @Test
    void anOldSaveWithoutAChosenRankPlaysItsHighestRank() {
        MemoryStorage storage = new MemoryStorage();
        storage.put(PlayerProfile.KEY_RANK, "4");
        assertEquals(Rank.HABITUE, profile(storage).getActiveRank());
    }

    @Test
    void theAdminChoiceOfRankIsNeverSaved() {
        MemoryStorage storage = new MemoryStorage();
        PlayerProfile profile = profile(storage);
        profile.setAdmin(true);
        assertEquals(Rank.LEGENDE, profile.getActiveRank());
        assertTrue(profile.chooseRank(Rank.BARON));
        assertEquals(Rank.BARON.getBonus(), profile.getRankBonus());
        profile.setAdmin(false);
        assertNull(profile.getActiveRank());
        assertNull(profile(storage).getActiveRank());
    }

    @Test
    void aBoughtCardJoinsTheCollectionUpToThreeCopies() {
        PlayerProfile profile = profile(new MemoryStorage());
        profile.addCoins(30_000_000);

        assertTrue(profile.buyCard("in_the_sleeve", 10_000_000));
        assertEquals(1, profile.getOwnedCopies("in_the_sleeve"));
        assertEquals(20_000_000, profile.getCoins());
        assertTrue(profile.buyCard("double", 1));
        assertFalse(profile.buyCard("double", 1), "déjà 3 exemplaires");
        assertFalse(profile.buyCard("in_the_sleeve", 30_000_000), "pas assez de pièces");
    }

    @Test
    void theMachineStartsWithTheElevenClassicReels() {
        PlayerProfile profile = profile(new MemoryStorage());

        assertEquals(Symbol.classicReels(), profile.getMachine());
        assertEquals(Symbol.MACHINE_SIZE, profile.getMachine().size());
        assertEquals(Symbol.classicReels(), profile.getOwnedReels());
    }

    @Test
    void aBoughtReelCanReplaceAClassicOneInTheMachine() {
        MemoryStorage storage = new MemoryStorage();
        PlayerProfile profile = profile(storage);
        List<Symbol> machine = new ArrayList<>(Symbol.classicReels());
        machine.set(machine.indexOf(Symbol.BAR), Symbol.CROWN);
        assertNotNull(profile.machineProblem(machine), "Couronne pas encore achetée");

        profile.addCoins(ReelShop.getPrices().get(Symbol.CROWN));
        assertTrue(profile.buyReel(Symbol.CROWN, ReelShop.getPrices().get(Symbol.CROWN)));
        assertFalse(profile.buyReel(Symbol.CROWN, 0), "déjà possédée");
        assertNull(profile.machineProblem(machine));
        profile.setMachine(machine);

        PlayerProfile reloaded = profile(storage);
        assertTrue(reloaded.ownsReel(Symbol.CROWN));
        assertEquals(machine, reloaded.getMachine());
    }

    @Test
    void theMachineHasExactlyElevenDifferentReelsWithoutTheJoker() {
        PlayerProfile profile = profile(new MemoryStorage());
        List<Symbol> ten = new ArrayList<>(Symbol.classicReels());
        ten.remove(Symbol.BAR);
        assertNotNull(profile.machineProblem(ten), "10 rouleaux");

        List<Symbol> twice = new ArrayList<>(ten);
        twice.add(Symbol.SEVEN);
        assertNotNull(profile.machineProblem(twice), "un rouleau en double");

        List<Symbol> joker = new ArrayList<>(ten);
        joker.add(Symbol.JOKER);
        assertNotNull(profile.machineProblem(joker), "le Joker");
        assertThrows(IllegalArgumentException.class, () -> profile.setMachine(joker));
    }

    @Test
    void theStrongestCardsAndReelsAreSoldOnlyAfterAHardPlace() {
        Map<String, Place> gates = CardLoader.loadHardGates(READER);
        assertEquals(Place.PRAIRIE, gates.get("masque_du_joker"));
        assertEquals(Place.PRAIRIE, gates.get("1_carreau"));
        assertEquals(Place.PRAIRIE, gates.get("1_trefle"));
        assertEquals(Place.FOIRE, gates.get("in_the_sleeve"));
        assertEquals(Place.FOIRE, gates.get("1_pique"));
        assertEquals(Place.FOIRE, gates.get("1_coeur"));
        for (String star : List.of("etoile_filante", "etoile_du_berger", "etoile_d_or")) {
            assertEquals(Place.PRAIRIE, gates.get(star), star);
        }
        assertEquals(9, gates.size());
        assertEquals(Place.RAPIDE, ReelShop.getHardGate(Symbol.DEMON_KING));
        assertEquals(Place.RAPIDE, ReelShop.getHardGate(Symbol.LUCK_APOSTLE));
        assertNull(ReelShop.getHardGate(Symbol.BOMB));

        // plus chers que tout ce qui reste libre : ils se paient avec les pièces du difficile
        Map<String, Long> prices = CardLoader.loadBoutique(READER);
        long open = 0;
        for (Map.Entry<String, Long> price : prices.entrySet()) {
            if (!gates.containsKey(price.getKey())) open = Math.max(open, price.getValue());
        }
        for (String id : gates.keySet()) assertTrue(prices.get(id) > open, id);

        PlayerProfile profile = profile(new MemoryStorage());
        assertTrue(profile.isHardGateOpen(null));
        assertFalse(profile.isHardGateOpen(Place.PRAIRIE));
        for (Dungeon dungeon : Place.PRAIRIE.getDungeons()) profile.clearHardDungeon(dungeon.name());
        assertTrue(profile.isHardGateOpen(Place.PRAIRIE));
        assertFalse(profile.isHardGateOpen(Place.FOIRE));
        profile.setAdmin(true);
        assertTrue(profile.isHardGateOpen(Place.RAPIDE), "le mode ADMIN ouvre tout");
    }

    @Test
    void everyReelOnSaleIsANewSymbolWithItsOwnDescription() {
        assertEquals(15, ReelShop.getPrices().size()); // 8 de 2026-10, 7 de 2026-11-01
        for (Symbol symbol : ReelShop.getPrices().keySet()) {
            assertFalse(symbol.isClassic(), symbol.name());
            assertFalse(ReelShop.describe(symbol).isBlank(), symbol.name());
        }
    }

    @Test
    void theMachineDraftIsSavedOnlyWithElevenReels() {
        PlayerProfile profile = profile(new MemoryStorage());
        MachineDraft draft = new MachineDraft(profile);
        assertTrue(draft.isComplete());
        assertNotNull(draft.addProblem(Symbol.SEVEN), "déjà dans la machine");
        assertNotNull(draft.addProblem(Symbol.STAR), "pas encore achetée");

        assertTrue(draft.remove(Symbol.BAR));
        assertFalse(draft.save(), "10 rouleaux");
        profile.addCoins(5_000_000);
        profile.buyReel(Symbol.STAR, 5_000_000);
        assertTrue(draft.add(Symbol.STAR));
        assertTrue(draft.save());
        assertTrue(profile.getMachine().contains(Symbol.STAR));
        assertFalse(profile.getMachine().contains(Symbol.BAR));

        draft.resetToClassic();
        assertEquals(Symbol.classicReels(), draft.getReels());
    }
}
