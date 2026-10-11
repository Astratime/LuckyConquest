package fr.astratime.lucky.entities.enemy;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
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
                case RANK    -> (int) cards.stream().filter(card -> card.getSuit() != null && card.getRank() == rank).count();
                case SPECIAL -> (int) cards.stream().filter(card -> card.getSuit() == null).count();
                case SUIT    -> (int) cards.stream().filter(card -> card.getSuit() == suit).count();
                case COLORS  -> (int) cards.stream().map(Card::getSuit).filter(java.util.Objects::nonNull).distinct().count();
                case FACES   -> (int) cards.stream()
                    .filter(card -> card.getSuit() != null && card.getRank() >= 11 && card.getRank() <= 13).count();
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

        /** @return {@code true} si {@code other} compte les mêmes cartes (seul son nombre change). */
        boolean sameAs(Condition other) {
            return kind == other.kind && rank == other.rank && suit == other.suit;
        }

        Condition plusOne() { return new Condition(kind, count + 1, rank, suit); }
    }

    private static Condition rank(int count, int rank) { return new Condition(Kind.RANK, count, rank, null); }
    private static Condition special(int count) { return new Condition(Kind.SPECIAL, count, 0, null); }
    private static Condition suit(int count, Card.Suit suit) { return new Condition(Kind.SUIT, count, 0, suit); }

    /** Règles possibles (choix d'Astra), toutes faisables avec 4 cartes. */
    public static final List<List<Condition>> RULES = List.of(
        List.of(rank(1, 11), special(2)),
        List.of(rank(1, 1), suit(2, Card.Suit.CARREAU)),
        List.of(suit(4, Card.Suit.TREFLE)),
        List.of(suit(3, Card.Suit.PIQUE)),
        List.of(rank(1, 12), suit(2, Card.Suit.COEUR)),
        List.of(rank(1, 13), special(1)),
        List.of(new Condition(Kind.COLORS, 3, 0, null)),
        List.of(new Condition(Kind.FACES, 2, 0, null)));

    /** Conditions qu'une Grande aspiration peut ajouter. */
    static final List<Condition> EXTRAS = List.of(special(1), suit(1, Card.Suit.PIQUE), suit(1, Card.Suit.COEUR),
        suit(1, Card.Suit.CARREAU), suit(1, Card.Suit.TREFLE), new Condition(Kind.FACES, 1, 0, null));

    private final List<Condition> conditions;

    public AspirationRule(List<Condition> conditions) {
        this.conditions = List.copyOf(conditions);
    }

    /**
     * @param grand Grande aspiration : une condition de plus (ou une carte de plus
     *              pour une condition qu'elle a déjà)
     * @return une règle tirée au hasard
     */
    public static AspirationRule random(Random random, boolean grand) {
        List<Condition> conditions = new ArrayList<>(RULES.get(random.nextInt(RULES.size())));
        if (grand) {
            Condition extra = EXTRAS.get(random.nextInt(EXTRAS.size()));
            int same = -1;
            for (int i = 0; i < conditions.size(); i++) if (conditions.get(i).sameAs(extra)) same = i;
            if (same >= 0) conditions.set(same, conditions.get(same).plusOne());
            else conditions.add(extra);
        }
        return new AspirationRule(conditions);
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
