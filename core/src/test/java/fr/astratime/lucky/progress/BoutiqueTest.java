package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.RankBonus;
import fr.astratime.lucky.entities.Symbol;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static fr.astratime.lucky.progress.PlayerProfileTest.MemoryStorage;
import static fr.astratime.lucky.progress.PlayerProfileTest.profile;
import static org.junit.jupiter.api.Assertions.*;

/** La boutique : rangs, cartes et rouleaux achetés avec les pièces, et la machine du joueur. */
class BoutiqueTest {

    @Test
    void thereAreTenRanksAndTheFirstIsTheMiser() {
        assertEquals(10, Rank.values().length);
        assertEquals("Avare", Rank.AVARE.getTitle());
        assertEquals(new RankBonus(100, 100, 10, 70), Rank.AVARE.getBonus());
        assertEquals(1_000_000L, Rank.AVARE.getPrice());
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

        profile.addCoins(999_999);
        assertFalse(profile.buyNextRank(), "pas assez de pièces");
        profile.addCoins(1 + 2_500_000);
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
    void everyReelOnSaleIsANewSymbolWithItsOwnDescription() {
        assertEquals(8, ReelShop.getPrices().size());
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
