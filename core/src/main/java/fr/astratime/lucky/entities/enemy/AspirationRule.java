package fr.astratime.lucky.entities.enemy;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.i18n.Lang;

import java.util.List;
import java.util.Random;

/**
 * Règle annoncée par une Aspiration de la Machine Originelle : si les cartes
 * jouées par le joueur à son prochain tour la respectent, il garde son rouleau ;
 * sinon, elle l'aspire pour ce tirage. Chaque condition se compte à part : une
 * même carte peut en remplir plusieurs (un As de Carreau compte pour l'As et
 * pour les Carreaux).
 */
public final class AspirationRule {

    /** Sorte de condition. */
    public enum Kind { RANK, SPECIAL, SUIT, COLORS, FACES }

    /**
     * Une condition : {@code count} cartes d'un rang ({@code rank}, l'As valant 1),
     * spéciales, d'une couleur ({@code suit}), de couleurs différentes, ou des figures.
     */
    public record Condition(Kind kind, int count, int rank, Card.Suit suit) {

        /** @return où en est la condition avec les cartes {@code cards} (jusqu'à {@link #count}). */
        public int progress(List<Card> cards) {
            int done = switch (kind) {
                case RANK    -> (int) cards.stream().filter(card -> card.hasRank() && card.getRank() == rank).count();
                case SPECIAL -> (int) cards.stream().filter(card -> card.getSuit() == null).count();
                case SUIT    -> (int) cards.stream().filter(card -> card.getSuit() == suit).count();
                case COLORS  -> (int) cards.stream().map(Card::getSuit).filter(java.util.Objects::nonNull).distinct().count();
                case FACES   -> (int) cards.stream()
                    .filter(card -> card.hasRank() && card.getRank() >= 11 && card.getRank() <= 13).count();
            };
            return Math.min(count, done);
        }

        /** @return {@code true} si les cartes {@code cards} remplissent la condition. */
        public boolean isMet(List<Card> cards) { return progress(cards) >= count; }

        /** @return la condition en quelques mots (ex : "2 Carreaux"). */
        public String text() {
            boolean many = count > 1;
            String template = switch (kind) {
                case RANK -> switch (rank) {
                    case 1  -> "{0} As";
                    case 11 -> many ? "{0} Valets" : "{0} Valet";
                    case 12 -> many ? "{0} Dames" : "{0} Dame";
                    default -> many ? "{0} Rois" : "{0} Roi";
                };
                case SPECIAL -> many ? "{0} cartes spéciales" : "{0} carte spéciale";
                case SUIT -> switch (suit) {
                    case PIQUE   -> many ? "{0} Piques" : "{0} Pique";
                    case COEUR   -> many ? "{0} Coeurs" : "{0} Coeur";
                    case CARREAU -> many ? "{0} Carreaux" : "{0} Carreau";
                    case TREFLE  -> many ? "{0} Trèfles" : "{0} Trèfle";
                };
                case COLORS -> "{0} couleurs différentes";
                case FACES -> many ? "{0} figures" : "{0} figure";
            };
            return Lang.f(template, count);
        }
    }

    private static Condition rank(int count, int rank) { return new Condition(Kind.RANK, count, rank, null); }
    private static Condition special(int count) { return new Condition(Kind.SPECIAL, count, 0, null); }
    private static Condition suit(int count, Card.Suit suit) { return new Condition(Kind.SUIT, count, 0, suit); }

    /** Règles au plus par tirage (choix d'Astra) : une par rouleau du joueur. */
    public static final int MAX_RULES = 3;

    /**
     * Première règle du tour (choix d'Astra) : 2 cartes à jouer, cartes spéciales,
     * figures ou cartes d'une même suite.
     */
    public static final List<Condition> FIRST_RULES = List.of(special(2), new Condition(Kind.FACES, 2, 0, null),
        suit(2, Card.Suit.PIQUE), suit(2, Card.Suit.COEUR), suit(2, Card.Suit.CARREAU), suit(2, Card.Suit.TREFLE));

    /** Règles suivantes : 1 carte à jouer chacune. */
    public static final List<Condition> EXTRA_RULES = List.of(special(1), new Condition(Kind.FACES, 1, 0, null),
        suit(1, Card.Suit.PIQUE), suit(1, Card.Suit.COEUR), suit(1, Card.Suit.CARREAU), suit(1, Card.Suit.TREFLE));

    private final List<Condition> conditions;

    public AspirationRule(List<Condition> conditions) {
        this.conditions = List.copyOf(conditions);
    }

    /**
     * @param first {@code true} pour la première règle du tour (2 cartes), sinon 1 carte
     * @return une règle tirée au hasard
     */
    public static AspirationRule random(Random random, boolean first) {
        List<Condition> pool = first ? FIRST_RULES : EXTRA_RULES;
        return new AspirationRule(List.of(pool.get(random.nextInt(pool.size()))));
    }

    /** @return ses conditions, dans l'ordre affiché. */
    public List<Condition> getConditions() { return conditions; }

    /** @return {@code true} si les cartes {@code cards} remplissent toutes ses conditions. */
    public boolean isMet(List<Card> cards) {
        return conditions.stream().allMatch(condition -> condition.isMet(cards));
    }

    /** @return la règle en quelques mots (ex : "1 As, 2 Carreaux"). */
    public String text() {
        return String.join(", ", conditions.stream().map(Condition::text).toList());
    }
}
