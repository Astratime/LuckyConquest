package fr.astratime.lucky.controllers;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.effects.AttackEffect;
import fr.astratime.lucky.entities.effects.GainEffect;
import fr.astratime.lucky.entities.enemy.EnemyCards;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySignature;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.events.EnemyRouletteEvent;
import fr.astratime.lucky.entities.events.EnemyThornsEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.GainsStolenEvent;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/** Les cartes propres des ennemis, liées à leurs rouleaux, et le deck de l'Ombre du Joueur. */
class EnemySignaturesTest {

    private static Player player() { return new Player("Joueur", 1_000_000, List.of()); }

    /** @return un ennemi {@code kind} dont le deck ne contient que {@code count} cartes {@code signature}. */
    private static Enemy only(EnemyKind kind, EnemySignature signature, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) cards.add(signature.card());
        return new Enemy(kind, cards);
    }

    /** Un tour où les rouleaux de l'ennemi montrent {@code symbol} (en cherchant la graine). */
    private static EnemyTurnResult turnWith(EnemySymbol symbol, Supplier<Enemy> enemy, Supplier<Player> player) {
        for (int seed = 0; seed < 500; seed++) {
            EnemyTurnResult turn = new EnemyTurnResolver(new Random(seed)).resolve(enemy.get(), player.get());
            if (Arrays.asList(turn.symbols()).contains(symbol)) return turn;
        }
        throw new AssertionError("aucun tirage avec " + symbol);
    }

    private static <T extends Event> List<T> events(EnemyTurnResult turn, Class<T> type) {
        return turn.events().stream().filter(type::isInstance).map(type::cast).toList();
    }

    @Test
    void theFirstEnemiesKeepTheBaseDeck() {
        for (EnemyKind kind : List.of(EnemyKind.ENTRAINEMENT, EnemyKind.CROUPIER, EnemyKind.SOLDAT_PIQUE,
            EnemyKind.SOLDAT_COEUR, EnemyKind.SOLDAT_CARREAU, EnemyKind.SOLDAT_TREFLE)) {
            assertTrue(kind.keepsBaseDeck(), kind.name());
            assertTrue(kind.getSignatures().isEmpty(), kind.name());
            assertTrue(kind.removedSuits().isEmpty(), kind.name());
            Set<Card.Suit> suits = EnumSet.noneOf(Card.Suit.class);
            kind.createDeck().forEach(card -> suits.add(card.getSuit()));
            assertEquals(EnumSet.allOf(Card.Suit.class), suits, kind.name());
        }
    }

    @Test
    void uselessSuitsLeaveTheDeckAndOwnCardsComeInFourCopies() {
        // Le Gardien : Épée, Bouclier, Épines. Pas de soin : plus de Cœur. 4 Ronces.
        List<Card> deck = EnemyKind.GARDIEN.createDeck();
        assertTrue(deck.stream().noneMatch(card -> card.getSuit() == Card.Suit.COEUR));
        assertEquals(EnemySignature.COPIES, deck.stream().filter(card -> EnemySignature.of(card) == EnemySignature.RONCES).count());
        // Le Joueur Fou : Tapis et Épée seulement. Ni Cœur ni Carreau.
        assertEquals(EnumSet.of(Card.Suit.COEUR, Card.Suit.CARREAU), EnemyKind.FOU.removedSuits());
        // La Sangsue se soigne (Croc, Potion) et se protège : elle garde tout.
        assertTrue(EnemyKind.SANGSUE.removedSuits().isEmpty());
        // Le Gardien du Levier : 3 cartes propres, 4 de chaque.
        assertEquals(3 * EnemySignature.COPIES, EnemyKind.GARDIEN_LEVIER.createDeck().stream()
            .filter(card -> EnemySignature.of(card) != null).count());
    }

    @Test
    void everyOwnCardBelongsToAnEnemyHasItsArtAndItsSymbol() {
        Set<EnemySignature> used = EnumSet.noneOf(EnemySignature.class);
        for (EnemyKind kind : EnemyKind.values()) {
            used.addAll(kind.getSignatures());
            for (EnemySignature signature : kind.getSignatures()) {
                EnemySymbol symbol = signature.getSymbol();
                if (symbol == null || signature == EnemySignature.ENCRE || signature == EnemySignature.COMPTE_A_REBOURS) continue;
                boolean hasSymbol = false;
                for (int phase = 1; phase <= 3; phase++) hasSymbol |= kind.getWeights(phase).containsKey(symbol);
                assertTrue(hasSymbol, kind.name() + " joue " + signature.name() + " sans son symbole");
            }
        }
        assertEquals(EnumSet.allOf(EnemySignature.class), used, "chaque carte propre a son ennemi");
        String assets = System.getProperty("lucky.assets", "../assets");
        for (EnemySignature signature : EnemySignature.values()) {
            assertTrue(new File(assets, signature.assetPath()).isFile(), signature.assetPath());
            assertSame(signature, EnemySignature.of(signature.card()));
            assertFalse(signature.getDescription().isBlank());
        }
    }

    @Test
    void ownCardsArePlayedOnTopOfTheSuitsButOnlyOneOfEachPerTurn() {
        List<Card> hand = List.of(EnemySignature.SAISIE.card(), EnemySignature.SAISIE.card(),
            EnemyCards.card(Card.Suit.PIQUE, 14), EnemyCards.card(Card.Suit.PIQUE, 13));
        List<Card> own = EnemyTurnResolver.ownCards(hand, new Enemy(EnemyKind.BANQUEROUTE));
        assertEquals(1, own.size());
        assertEquals(EnemySignature.SAISIE, EnemySignature.of(own.get(0)));

        Enemy deadTime = new Enemy(EnemyKind.TEMPS_MORT);
        List<Card> countdowns = List.of(EnemySignature.COMPTE_A_REBOURS.card(), EnemySignature.COMPTE_A_REBOURS.card(),
            EnemyCards.card(Card.Suit.PIQUE, 14));
        assertEquals(1, EnemyTurnResolver.ownCards(countdowns, deadTime).size());
        for (int i = 0; i < EnemySignature.COMPTE_MAX; i++) deadTime.loseTurn();
        assertTrue(EnemyTurnResolver.ownCards(countdowns, deadTime).isEmpty(), "plus de tour à retirer : il le garde");
    }

    @Test
    void anEnemyPlaysOneOwnCardPerTurnButUpToThreeInChapterSix() {
        List<Card> lever = List.of(EnemySignature.PLANCHE_A_BILLETS.card(), EnemySignature.REDRESSEMENT.card(),
            EnemySignature.BOTTE_SECRETE.card(), EnemyCards.card(Card.Suit.PIQUE, 14));
        assertEquals(3, EnemyTurnResolver.ownCards(lever, new Enemy(EnemyKind.GARDIEN_LEVIER)).size(), "chapitre 6 : 3 + 3 cartes");
        for (EnemyKind kind : List.of(EnemyKind.MAISON, EnemyKind.ROI_PIQUE, EnemyKind.KRAKEN)) {
            List<Card> hand = new ArrayList<>();
            for (EnemySignature signature : kind.getSignatures()) hand.add(signature.card());
            assertTrue(EnemyTurnResolver.ownCards(hand, new Enemy(kind)).size() <= 1, kind + " : une carte propre par tour");
        }
    }

    @Test
    void theCountdownShortensTheDeadTimeThreeTimesAtMost() {
        Enemy enemy = only(EnemyKind.TEMPS_MORT, EnemySignature.COMPTE_A_REBOURS, 12);
        assertEquals(EnemyKind.TIME_LIMIT, enemy.getTurnLimit());
        for (int turn = 0; turn < 6; turn++) new EnemyTurnResolver(new Random(turn)).resolve(enemy, player());
        assertEquals(EnemySignature.COMPTE_MAX, enemy.getLostTurns());
        assertEquals(EnemyKind.TIME_LIMIT - EnemySignature.COMPTE_MAX, enemy.getTurnLimit());
        assertFalse(new Enemy(EnemyKind.MAISON).loseTurn(), "sans limite de tours, rien à retirer");
    }

    @Test
    void brambleThornsReturnMoreDamage() {
        EnemyTurnResult turn = turnWith(EnemySymbol.THORNS, () -> only(EnemyKind.GARDIEN, EnemySignature.RONCES, 3), EnemySignaturesTest::player);
        EnemyThornsEvent thorns = events(turn, EnemyThornsEvent.class).get(0);
        assertEquals(EnemyKind.GARDIEN.thornsPercent(false) + EnemySignature.RONCES_PERCENT, thorns.percent, "une Ronce par tour");
    }

    @Test
    void theRiggedBallAlwaysLandsOnZero() {
        for (int seed = 0; seed < 40; seed++) {
            Enemy enemy = only(EnemyKind.ROULETTE, EnemySignature.BILLE_TRUQUEE, 3);
            EnemyTurnResult turn = new EnemyTurnResolver(new Random(seed)).resolve(enemy, player());
            List<EnemyRouletteEvent> roulettes = events(turn, EnemyRouletteEvent.class);
            if (roulettes.isEmpty()) continue;
            assertEquals(EnemyRouletteEvent.Pocket.ZERO, roulettes.get(0).pocket, "seed " + seed);
        }
    }

    @Test
    void theSeizureLowersTheBankruptcyThreshold() {
        // Fortune de la Banqueroute : 40 % de ses PV restants. Gains à 35 % : sans Saisie, elle perd des PV.
        Supplier<Player> rich = () -> {
            Player player = player();
            player.addGains(EnemyKind.BANQUEROUTE.getMaxHp(false) * 0.35);
            return player;
        };
        EnemyTurnResult plain = turnWith(EnemySymbol.BANKRUPTCY,
            () -> new Enemy(EnemyKind.BANQUEROUTE, List.of(EnemyCards.card(Card.Suit.PIQUE, 2))), rich);
        assertTrue(events(plain, GainsStolenEvent.class).isEmpty(), "sous 40 % : il perd des PV");
        EnemyTurnResult seized = turnWith(EnemySymbol.BANKRUPTCY, () -> only(EnemyKind.BANQUEROUTE, EnemySignature.SAISIE, 1), rich);
        assertFalse(events(seized, GainsStolenEvent.class).isEmpty(), "avec une Saisie, le seuil tombe à 30 % : il prend tout");
    }

    @Test
    void nextCardsWaitForTheirSymbol() {
        Enemy enemy = only(EnemyKind.FORGERON, EnemySignature.TREMPE, 3);
        Player player = player();
        int seed = 0;
        EnemyTurnResult turn;
        do {
            turn = new EnemyTurnResolver(new Random(seed++)).resolve(enemy, player);
        } while (!Arrays.asList(turn.symbols()).contains(EnemySymbol.ANVIL) && seed < 500);
        long anvils = Arrays.stream(turn.symbols()).filter(symbol -> symbol == EnemySymbol.ANVIL).count();
        assertTrue(enemy.getAnvil() > anvils * EnemySymbol.ANVIL_ATTACK, "la Trempe double au moins une Enclume");
    }

    @Test
    void lanternAndReassessmentLastTwoTurns() {
        Player player = player();
        player.getLastingEffects().blind(2); // Lanterne : 2 cartes cachées
        player.getLastingEffects().blind();  // un autre Aveuglement s'y ajoute
        assertEquals(3, player.getLastingEffects().getBlindCards());
        assertEquals(3, player.getLastingEffects().takeBlind());
        assertEquals(0, player.getLastingEffects().takeBlind());

        player.getLastingEffects().addTax(2);
        assertEquals(1, player.getLastingEffects().takeTaxes());
        assertEquals(1, player.getLastingEffects().takeTaxes());
        assertEquals(0, player.getLastingEffects().takeTaxes());
    }

    @Test
    void clubsAimAtTheSpecialReel() {
        // Le Tricheur : Dé pipé, Épée, Bouclier, Potion. Ses Trèfles visent le Dé pipé.
        for (int seed = 0; seed < 20; seed++) {
            List<Card> clubs = new ArrayList<>();
            for (int i = 0; i < 8; i++) clubs.add(EnemyCards.card(Card.Suit.TREFLE, 10));
            EnemyTurnResult turn = new EnemyTurnResolver(new Random(seed))
                .resolve(new Enemy(EnemyKind.TRICHEUR, clubs), player());
            assertEquals(Set.of(EnemySymbol.LOADED_DIE), turn.luck().keySet(), "seed " + seed);
        }
    }

    @Test
    void theShadowPlaysTheRealCardsOfThePlayer() {
        Card attack = new Card("lame", "Lame", "cards/special/x.png", List.of(new AttackEffect(10)), null, 1);
        Card gains  = new Card("gains", "Gains", "cards/special/y.png", List.of(new GainEffect(500)), null, 1);
        List<Card> deck = new ArrayList<>(Collections.nCopies(4, attack));
        deck.addAll(Collections.nCopies(4, gains));
        List<Card> shadow = EnemyCards.shadowDeck(deck);
        assertEquals("Lame", shadow.get(0).getName());
        assertEquals("cards/special/x.png", shadow.get(0).getAssetPath());

        Player player = player();
        player.addGains(10_000);
        Enemy enemy = new Enemy(EnemyKind.OMBRE, shadow);
        EnemyTurnResult turn = new EnemyTurnResolver(new Random(3)).resolve(enemy, player);
        assertEquals(3, turn.played().size());
        long stealing = turn.played().stream().filter(card -> EnemyCards.shadowRoleOf(card) == EnemyCards.ShadowRole.GAINS).count();
        assertEquals(stealing, events(turn, GainsStolenEvent.class).size(),
            "chaque carte de gains copiée lui vole des gains");
    }
}
