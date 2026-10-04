package fr.astratime.lucky.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Le joueur : points de vie, deck/défausse/main courante, machine à sous
 * personnelle, gains accumulés et modificateurs de combat du tour
 * (bouclier).
 */
public class Player {

    /** Nombre maximum de cartes (non jouées) sur la table pendant un tour. */
    public static final int MAX_HAND_SIZE = 8;
    /** PV max du joueur sans rang. */
    public static final int BASE_HP = 100;

    private final String      name;
    private final DiscardPile discardPile = new DiscardPile();
    private final Deck        deck;
    private final SlotMachine slotMachine;
    /** Bonus de son rang (PV max compris), acheté à la boutique. */
    private final RankBonus   rankBonus;
    /** Effets de cartes qui durent plusieurs tours (Recyclage, Porte-bonheur). */
    private final LastingEffects lastingEffects = new LastingEffects();
    /** Cartes sur la table, pas encore jouées ce tour. */
    private final List<Card>  currentHand = new ArrayList<>();
    /** Cartes jouées ce tour : quittent la main, rejoignent la défausse en fin de tour. */
    private final List<Card>  playedCards = new ArrayList<>();
    private       int         hp;
    private final int         maxHp;

    /** Monnaie gagnée en combat. Servira à acheter des bonus en combat. */
    private int gains = 0;

    /** Bouclier accumulé ce tour par les DefenseAction. */
    private int shield = 0;
    /** Assurance : PV que l'ennemi peut retirer en tout ce tour (-1 : pas de plafond). */
    private int damageCap = -1;
    /** PV retirés par l'ennemi depuis le début du tour (pour l'Assurance). */
    private int damageTakenThisTurn = 0;
    /** Casques : chacun bloque entièrement le prochain coup reçu. */
    private int helmets = 0;
    /** Corde de rappel : prête à retenir le joueur à 1 PV ; elle ne sert qu'une fois par combat. */
    private boolean ropeReady = false;
    private boolean ropeUsed  = false;
    /** La Corde de rappel vient de retenir le joueur (texte à montrer). */
    private boolean ropeSaved = false;
    /** Cartes volées (Abordage) ou confisquées (Fouille) : absentes du combat, elles reviennent au suivant. */
    private final List<Card> confiscated = new ArrayList<>();

    /**
     * @param name     nom affiché du joueur
     * @param maxHp    points de vie maximum (le joueur démarre à pleine vie)
     * @param cards    cartes composant le deck initial (mélangées à la construction du Deck)
     */
    public Player(String name, int maxHp, List<Card> cards) {
        this(name, maxHp, cards, RankBonus.NONE, Symbol.classicReels());
    }

    /**
     * @param maxHp     points de vie maximum, avant le bonus du rang
     * @param rankBonus bonus du rang du joueur (PV max, valeur de base des rouleaux)
     * @param reels     symboles de sa machine à sous (le Joker s'y ajoute toujours)
     */
    public Player(String name, int maxHp, List<Card> cards, RankBonus rankBonus, List<Symbol> reels) {
        this.name        = name;
        this.rankBonus   = rankBonus;
        this.maxHp       = maxHp + rankBonus.hp();
        this.hp          = this.maxHp;
        this.deck        = new Deck(cards, discardPile);
        this.slotMachine = new SlotMachine(reels);
    }

    /**
     * Inflige des dégâts au joueur : le bouclier accumulé absorbe les dégâts
     * en priorité, seul l'excédent (s'il y en a) retire des points de vie,
     * sans jamais descendre sous 0.
     *
     * @param damage dégâts bruts, avant absorption par le bouclier
     * @return les dégâts effectivement retirés des points de vie (après bouclier)
     */
    public int takeDamage(int damage) {
        return takeDamage(damage, false);
    }

    /**
     * Comme {@link #takeDamage(int)} ; avec {@code pierceShield} (Forage), le
     * coup traverse le bouclier sans l'user. Un Casque bloque le coup entier.
     */
    public int takeDamage(int damage, boolean pierceShield) {
        if (damage > 0 && helmets > 0) { // Casque : le coup rebondit
            helmets--;
            return 0;
        }
        int absorbed  = pierceShield ? 0 : Math.min(shield, damage);
        shield -= absorbed;
        int remaining = damage - absorbed;
        int actualLoss = Math.min(hp, remaining);
        if (damageCap >= 0) actualLoss = Math.min(actualLoss, Math.max(0, damageCap - damageTakenThisTurn)); // Assurance
        if (ropeReady && actualLoss >= hp && hp > 0) { // Corde de rappel : il reste à 1 PV
            actualLoss = hp - 1;
            ropeReady  = false;
            ropeUsed   = true;
            ropeSaved  = true;
        }
        hp -= actualLoss;
        damageTakenThisTurn += actualLoss;
        return actualLoss;
    }
    /**
     * Rend {@code amount} points de vie, sans dépasser le maximum.
     *
     * @return les points de vie réellement rendus (0 si le joueur était déjà au maximum)
     */
    public int heal(int amount) {
        int healed = Math.min(maxHp, hp + amount) - hp;
        hp += healed;
        return healed;
    }
    /**
     * Retire {@code amount} points de vie sans passer par le bouclier, sans
     * jamais descendre sous 1 PV (Pacte de sang : on ne meurt pas de sa propre carte).
     *
     * @return les points de vie réellement retirés
     */
    public int sacrificeHp(int amount) {
        int lost = Math.max(0, Math.min(amount, hp - 1));
        hp -= lost;
        return lost;
    }

    /** @return {@code true} si le joueur n'a plus de points de vie. */
    public boolean isDefeated()        { return hp <= 0; }

    /** @return la proportion de vie restante, entre 0 et 1 (utilisé par les effets conditionnels). */
    public float getHpRatio() { return (float) hp / maxHp; }

    /**
     * Ajoute {@code amount} aux gains accumulés. Une perte ne fait jamais passer
     * les gains sous 0 (ni n'aggrave une dette de la Taxe) ; ce qui est perdu ou
     * dépensé l'est d'abord en Fausse monnaie.
     */
    public void addGains(int amount) {
        int before = gains;
        gains = amount >= 0 ? gains + amount : Math.max(Math.min(0, gains), gains + amount);
        if (gains < before) lastingEffects.spendFakeGains(before - gains);
    }

    /**
     * Taxe (le Comptable) : retire {@code percent} % des gains, puis encore
     * {@code flat} ; les gains peuvent passer sous zéro.
     *
     * @return les gains retirés
     */
    public int payTax(int percent, int flat) {
        int taxed = Math.round(Math.max(0, gains) * percent / 100f) + flat;
        lastingEffects.spendFakeGains(Math.max(0, Math.min(gains, taxed)));
        gains -= taxed;
        return taxed;
    }

    /**
     * Consomme un pourcentage des gains actuels (ex : coût d'un As de Trèfle).
     *
     * @param percent pourcentage à consommer (0.3f = 30%)
     * @return le montant effectivement consommé
     */
    public int consumeGainsPercent(float percent) {
        int amount = Math.round(Math.max(0, gains) * percent);
        lastingEffects.spendFakeGains(amount);
        gains -= amount;
        return amount;
    }

    // -------------------------------------------------------------------------
    // Main, cartes jouées et défausse
    // -------------------------------------------------------------------------

    /**
     * Pioche {@code count} cartes du deck. Celles qui tiennent dans la main
     * (jusqu'à {@link #MAX_HAND_SIZE}) la rejoignent, le surplus part
     * directement à la défausse.
     *
     * @param count nombre de cartes à piocher
     * @return les cartes ajoutées à la main et celles défaussées faute de place
     */
    public DrawResult draw(int count) {
        List<Card> drawn = deck.draw(count);
        int room = Math.max(0, MAX_HAND_SIZE - currentHand.size());
        List<Card> added     = drawn.subList(0, Math.min(room, drawn.size()));
        List<Card> discarded = drawn.subList(added.size(), drawn.size());
        currentHand.addAll(added);
        discardPile.addAll(discarded);
        return new DrawResult(added, discarded);
    }

    /**
     * Joue une carte de la main : elle quitte la table et reste de côté
     * jusqu'à la fin du tour (elle ne peut donc pas être repiochée ce tour-ci).
     *
     * @param card carte jouée
     * @return {@code false} si la carte n'était pas dans la main (rien n'est fait)
     */
    public boolean playCard(Card card) {
        if (!currentHand.remove(card)) return false;
        if (!card.isConsumable()) playedCards.add(card); // une carte consommable disparaît
        return true;
    }

    /**
     * Remplace une carte de la main par une autre, à la même place (ex : sa
     * couleur changée par l'Arc-en-ciel).
     *
     * @return {@code false} si {@code card} n'était pas dans la main (rien n'est fait)
     */
    public boolean replaceInHand(Card card, Card replacement) {
        int index = currentHand.indexOf(card);
        if (index < 0) return false;
        currentHand.set(index, replacement);
        return true;
    }

    /**
     * Remplace, dans la main et parmi les cartes jouées, chaque carte présente
     * dans {@code originals} par sa carte d'origine (fin de l'effet d'un Arc-en-ciel).
     */
    public void restoreCards(Map<Card, Card> originals) {
        currentHand.replaceAll(card -> originals.getOrDefault(card, card));
        playedCards.replaceAll(card -> originals.getOrDefault(card, card));
    }

    /**
     * Pose une nouvelle carte sur la table s'il reste un emplacement libre,
     * sinon la glisse dans le deck, à une place au hasard.
     *
     * @return {@code true} si la carte a rejoint la main
     */
    public boolean addToHandOrDeck(Card card) {
        if (currentHand.size() < MAX_HAND_SIZE) {
            currentHand.add(card);
            return true;
        }
        deck.insertRandomly(card);
        return false;
    }

    /**
     * Pose une nouvelle carte sur la table s'il reste un emplacement libre,
     * sinon la met directement dans la défausse.
     *
     * @return {@code true} si la carte a rejoint la main
     */
    public boolean addToHandOrDiscard(Card card) {
        if (currentHand.size() < MAX_HAND_SIZE) {
            currentHand.add(card);
            return true;
        }
        discardPile.addAll(List.of(card));
        return false;
    }

    /**
     * Fin de tour : envoie à la défausse les cartes jouées puis celles restées
     * sur la table, et vide la main.
     *
     * @return les cartes restées sur la table (non jouées), dans leur ordre dans la main
     */
    public List<Card> discardHand() {
        List<Card> remaining = new ArrayList<>(currentHand);
        discardPile.addAll(playedCards);
        discardPile.addAll(remaining);
        playedCards.clear();
        currentHand.clear();
        return remaining;
    }

    /**
     * Combat suivant d'une épreuve : un joueur sans effet en cours ni bouclier,
     * qui garde ses PV, ses gains et toutes ses cartes (achats compris),
     * réunies dans son deck.
     *
     * @return le joueur prêt pour le combat suivant
     */
    public Player nextCombat() {
        List<Card> cards = new ArrayList<>(deck.getCards());
        cards.addAll(discardPile.getCards());
        cards.addAll(currentHand);
        cards.addAll(playedCards);
        cards.addAll(confiscated); // volées ou confisquées le temps d'un combat seulement
        Player next = new Player(name, maxHp - rankBonus.hp(), cards, rankBonus, slotMachine.getReels());
        next.hp = hp; // les PV perdus ne reviennent pas d'un combat à l'autre
        next.gains = gains;
        return next;
    }

    /**
     * Le joueur perd tous ses PV d'un coup, sans que rien ne le protège (Dernier
     * tirage perdu, Temps Mort écoulé).
     *
     * @return les PV perdus
     */
    public int loseAllHp() {
        int lost = hp;
        hp = 0;
        return lost;
    }

    /** Ajoute {@code amount} au bouclier accumulé ce tour. */
    public void addShield(int amount) { shield += amount; }

    /** Réinitialise le bouclier en fin de tour. */
    public void resetTurnDefenses() {
        shield = 0;
        damageCap = -1;
        damageTakenThisTurn = 0;
    }

    /**
     * Assurance : jusqu'à la fin du tour, les coups de l'ennemi ne retirent pas
     * plus de {@code percent} % des PV max en tout.
     */
    public void insure(int percent) {
        int cap = Math.round(maxHp * percent / 100f);
        damageCap = damageCap < 0 ? cap : Math.min(damageCap, cap);
    }

    /** Casque : le prochain coup reçu est bloqué entièrement. */
    public void addHelmet() { helmets++; }

    /**
     * Corde de rappel : le prochain coup qui devrait tuer le joueur le laisse
     * à 1 PV. Une fois par combat.
     *
     * @return {@code false} si elle a déjà servi dans ce combat
     */
    public boolean addRope() {
        if (ropeUsed) return false;
        ropeReady = true;
        return true;
    }

    /** @return {@code true} si la Corde de rappel est prête. */
    public boolean hasRope() { return ropeReady; }

    /** @return {@code true} si la Corde de rappel vient de retenir le joueur (une seule fois). */
    public boolean takeRopeSaved() {
        boolean saved = ropeSaved;
        ropeSaved = false;
        return saved;
    }

    /** @return les Casques prêts à bloquer un coup. */
    public int getHelmets() { return helmets; }

    /**
     * Abordage : {@code card} est volée, sur la table ou parmi les cartes
     * jouées ; elle ne revient pas de tout le combat.
     *
     * @return {@code false} si le joueur ne l'avait ni en main ni parmi les cartes jouées
     */
    public boolean steal(Card card) {
        if (!currentHand.remove(card) && !playedCards.remove(card)) return false;
        confiscated.add(card);
        return true;
    }

    /**
     * Fouille (la Sécurité) : une carte tirée au hasard du deck (sinon de la
     * défausse) est confisquée jusqu'à la fin du combat.
     *
     * @return la carte confisquée, ou {@code null} s'il n'y en avait plus
     */
    public Card frisk(java.util.Random random) {
        List<Card> pile = !deck.getCards().isEmpty() ? deck.getCards() : discardPile.getCards();
        if (pile.isEmpty()) return null;
        Card card = pile.get(random.nextInt(pile.size()));
        if (pile == deck.getCards()) deck.getCards().remove(card);
        else discardPile.remove(card);
        confiscated.add(card);
        return card;
    }

    /** @return les cartes volées ou confisquées ce combat. */
    public List<Card> getConfiscated() { return Collections.unmodifiableList(confiscated); }

    /** @return toutes les cartes du joueur ce combat : deck, défausse, main et cartes jouées. */
    public List<Card> getAllCards() {
        List<Card> cards = new ArrayList<>(deck.getCards());
        cards.addAll(discardPile.getCards());
        cards.addAll(currentHand);
        cards.addAll(playedCards);
        return cards;
    }

    /**
     * Retire {@code card} de la main sans la défausser (une carte qui disparaît
     * en fin de tour, comme le Scorbut).
     *
     * @return {@code false} si elle n'était pas dans la main
     */
    public boolean removeFromHand(Card card) { return currentHand.remove(card); }

    /** Envoie {@code card}, retirée de la main, dans la défausse (Grignotage, Scorbut qui la remplace). */
    public boolean discardFromHand(Card card) {
        if (!currentHand.remove(card)) return false;
        discardPile.addAll(List.of(card));
        return true;
    }

    /** @return les PV que l'ennemi peut encore retirer ce tour (Assurance), ou -1 sans plafond. */
    public int getDamageCapLeft() { return damageCap < 0 ? -1 : Math.max(0, damageCap - damageTakenThisTurn); }

    /** @return le nom affiché du joueur. */
    public String      getName()                   { return name; }
    /** @return les points de vie actuels. */
    public int         getHp()                     { return hp; }
    /** @return les points de vie maximum. */
    public int         getMaxHp()                  { return maxHp; }
    /** @return la défausse du joueur. */
    public DiscardPile getDiscardPile()             { return discardPile; }
    /** @return le deck du joueur. */
    public Deck        getDeck()                    { return deck; }
    /** @return les bonus de son rang. */
    public RankBonus   getRankBonus()               { return rankBonus; }
    /** @return la machine à sous personnelle du joueur. */
    public SlotMachine getSlotMachine()             { return slotMachine; }
    /** @return les effets de cartes qui durent plusieurs tours pendant ce combat. */
    public LastingEffects getLastingEffects()      { return lastingEffects; }
    /** @return les cartes sur la table, pas encore jouées ce tour (vue non modifiable). */
    public List<Card>  getCurrentHand()             { return Collections.unmodifiableList(currentHand); }
    /** @return les cartes jouées ce tour (vue non modifiable). */
    public List<Card>  getPlayedCards()             { return Collections.unmodifiableList(playedCards); }
    /** @return les gains accumulés. */
    public int         getGains()                   { return gains; }
    /** @return le bouclier accumulé ce tour. */
    public int         getShield()                  { return shield; }
}
