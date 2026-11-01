package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.enemy.EnemyCards;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySignature;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.enemy.EnemyTrait;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * L'ennemi : points de vie, défense, et son propre jeu de cartes (jeu sombre,
 * voir {@link EnemyCards}). À son tour, il pioche {@link #HAND_SIZE} cartes,
 * en joue {@link #PLAYS_PER_TURN} pour renforcer les symboles de sa machine à
 * sous, puis toutes rejoignent sa défausse (voir
 * {@link fr.astratime.lucky.controllers.EnemyTurnResolver}).
 */
public class Enemy {

    /** Cartes piochées par l'ennemi à chaque tour. */
    public static final int HAND_SIZE      = 8;
    /** Cartes que le croupier joue parmi celles piochées (voir {@link EnemyKind#getPlaysPerTurn()}). */
    public static final int PLAYS_PER_TURN = 3;
    /** Défense de base du croupier, reformée à chacun de ses tours : elle absorbe les dégâts des attaques du joueur. */
    public static final int BASE_DEFENSE   = 30;

    private final EnemyKind   kind;
    /** Mode difficile de la Tour : ses PV et sa force sont multipliés (voir {@link EnemyKind#HARD_POWER_FACTOR}). */
    private final boolean     hard;
    /** Malus posés par les cartes du joueur (voir {@link EnemyMaluses}). */
    private final EnemyMaluses maluses;
    private final String      name;
    private final int         maxHp;
    private       int         hp;
    private final DiscardPile discardPile = new DiscardPile();
    private final Deck        deck;
    /**
     * Défense restante : base et Boucliers de son dernier tour. Elle absorbe
     * les attaques du joueur et s'use à chaque coup, jusqu'à son tour suivant.
     */
    private       int         defense;
    /** Bonus d'attaque de la Rage, gardé jusqu'à la fin du combat. */
    private       int         rage;
    /** Part des dégâts du joueur renvoyée par ses Épines (en %), jusqu'à son tour suivant. */
    private       int         thornsPercent;
    /** Dégâts encaissés depuis le début de son dernier tour (pour ses Épines). */
    private       int         damageTaken;
    /** Attaque ajoutée à son prochain coup par les gains volés (Intérêts). */
    private       int         interest;
    /** Compte à rebours de son Sablier, de 0 à {@link EnemySymbol#HOURGLASS_MAX}. */
    private       int         hourglass;
    /** Sa mise (Tapis) : ses attaques sont multipliées d'autant, jusqu'à ce qu'il soit touché. */
    private       int         stake = 1;
    /** Tapis posé à son dernier tour : sa mise doublera à son tour suivant, s'il n'est pas touché d'ici là. */
    private       boolean     allIn;
    /** Sa phase (1, puis 2 sous le premier seuil d'un boss, voir {@link EnemyKind#phaseThresholds()}). */
    private       int         phase = 1;
    /** Bonus d'attaque de ses Enclumes, gardé jusqu'à la fin du combat, sans limite. */
    private       int         anvil;
    /** Symbole annoncé par sa Prédiction pour le prochain tirage du joueur ({@code null} : aucun). */
    private       Symbol      prediction;
    /** Sa Prédiction est sortie au dernier tirage du joueur : ses attaques de ce tour triplent. */
    private       boolean     predictionHit;
    /** La Maison a déjà annulé un coup (une fois par combat). */
    private       boolean     houseUsed;
    /** La Machine Originelle a résisté au coup fatal : le Dernier tirage l'attend. */
    private       boolean     lastDrawPending;
    /** Le Dernier tirage a eu lieu : le prochain coup fatal l'est vraiment. */
    private       boolean     lastDrawDone;
    /** Ce qu'il vient de faire à un coup (Maison, Machine Originelle), à annoncer ({@code null} : rien). */
    private       String      notice;
    /** Il a atteint le seuil de sa phase suivante : il y passe à son prochain tour. */
    private       boolean     phaseWallReached;
    /** Famille de cartes interdite tout le combat (le Portier), ou {@code null}. */
    private       CardFamily  bannedFamily;
    /** As sombres gardés en copie des achats du joueur (le Pilleur). */
    private       int         loot;
    /** Harpon : bras coupés au Kraken, pour tout le combat. */
    private       int         cutArms;
    private       Symbol      secondPrediction;
    private       int         allInHolds;
    private       int         lostTurns;
    /** Tours du joueur pendant lesquels la Machine Originelle garde encore le rouleau volé en phase 2. */
    private       int         theftTurns;
    /** Mur porteur : sa défense ne s'use pas sous les coups jusqu'à son prochain tour. */
    private       boolean     solidDefense;
    /** Placage, Battement : tours où sa peau d'or tient, même sous la moitié de ses PV. */
    private       int         platingTurns;
    /** Pièces avalées par le Bandit manchot. */
    private       int         coins;
    /** Rouleaux du joueur aspirés à son dernier tirage (Aspiration de la Machine Originelle). */
    private       int         aspired;
    private final Map<EnemySignature, Integer> charges = new EnumMap<>(EnemySignature.class);
    /** Numéro (Monsieur Loyal) : la famille annoncée pour le tour du joueur, et ce que vaut le coup s'il la rate. */
    private       Card.Suit   actSuit;
    private       int         actFactor;
    /** Sarcophage (Pharaon maudit) : aucun coup ne passe jusqu'à son prochain tour. */
    private       boolean     sealed;

    // ----- Archétypes de deck : Rentier (Créances) et Bluffeur (malus) -----

    /**
     * Ennemi avec le deck de départ du jeu sombre.
     *
     * @param name  nom affiché de l'ennemi
     * @param maxHp points de vie maximum (l'ennemi démarre à pleine vie)
     */
    public Enemy(String name, int maxHp) {
        this(name, maxHp, EnemyCards.starterDeck());
    }

    /** @param cards cartes composant le deck de l'ennemi (mélangées) */
    public Enemy(String name, int maxHp, List<Card> cards) {
        this(EnemyKind.CROUPIER, name, maxHp, cards, false);
    }

    /** Ennemi {@code kind} : son nom, ses PV, sa défense, ses rouleaux et son deck. */
    public Enemy(EnemyKind kind) {
        this(kind, false);
    }

    /** Comme {@link #Enemy(EnemyKind)}, en mode difficile si {@code hard} (Tour des épreuves). */
    public Enemy(EnemyKind kind, boolean hard) {
        this(kind, kind.getDisplayName(), kind.getMaxHp(hard), kind.createDeck(), hard);
    }

    /** Ennemi {@code kind} qui joue avec le deck {@code cards} (l'Ombre du Joueur, qui a celui du joueur). */
    public Enemy(EnemyKind kind, List<Card> cards) {
        this(kind, cards, false);
    }

    /** Comme {@link #Enemy(EnemyKind, List)}, en mode difficile si {@code hard}. */
    public Enemy(EnemyKind kind, List<Card> cards, boolean hard) {
        this(kind, kind.getDisplayName(), kind.getMaxHp(hard), cards, hard);
    }

    private Enemy(EnemyKind kind, String name, int maxHp, List<Card> cards, boolean hard) {
        this.kind    = kind;
        this.hard    = hard;
        this.maluses = new EnemyMaluses(kind.isBoss());
        this.name    = name;
        this.maxHp   = maxHp;
        this.hp      = maxHp;
        this.defense = kind.getBaseDefense(hard);
        this.deck    = new Deck(cards, discardPile);
    }

    /**
     * Retire {@code damage} points de vie, sans descendre sous 0. Une peau d'or
     * (Golem d'or) en encaisse la moitié tant qu'il a plus de la moitié de ses PV.
     * Un coup qui touche fait retomber sa mise (Tapis) ; un gros coup fait reculer son Sablier.
     *
     * @return les points de vie réellement retirés
     */
    public int takeDamage(long damage) {
        int lost = (int) Math.min(hp, Math.max(0, guard(skinned(damage))));
        hp -= lost;
        damageTaken += lost;
        if (lost <= 0) return 0;
        if (allInHolds > 0 && (allIn || stake > 1)) {
            allInHolds--; // Tout sur le rouge : son Tapis tient ce coup
        } else {
            stake = 1;
            allIn = false;
        }
        if (hourglass > 0 && lost * 1000L >= (long) maxHp * EnemySymbol.HOURGLASS_HIT_PER_MILLE) hourglass--;
        return lost;
    }

    /**
     * Ce que deviennent {@code damage} dégâts qui le touchent : la Maison annule
     * une fois le premier gros coup (au moins {@link EnemyKind#HOUSE_CANCEL_PERCENT} %
     * de ses PV max) ; la Garde d'un boss ou d'un roi l'arrête au seuil de sa phase (le surplus est perdu)
     * et le rend invincible jusqu'à son tour, où il change de phase (choix d'Astra : plus de limite par tour) ;
     * la Machine Originelle résiste au premier coup fatal, à 1 PV,
     * et le Dernier tirage décidera (les coups suivants du même tour ne la touchent plus). Ce qui arrive est à lire dans {@link #takeNotice()}.
     */
    private long guard(long damage) {
        if (kind.houseWins() && !houseUsed && damage * 100 >= (long) maxHp * EnemyKind.HOUSE_CANCEL_PERCENT) {
            houseUsed = true;
            notice = Lang.t("LA MAISON GAGNE TOUJOURS : COUP ANNULÉ");
            return 0;
        }
        if (sealed && damage > 0) {                     // Sarcophage : rien ne passe avant son tour
            if (notice == null) notice = Lang.t("SARCOPHAGE : AUCUN COUP NE PASSE");
            return 0;
        }
        if (lastDrawPending) return 0;                  // à 1 PV, elle attend le Dernier tirage : plus rien ne la touche
        long wall = phaseWall();
        if (wall > 0 && damage > 0 && hp - damage <= wall) { // Garde : au seuil de phase, invincible jusqu'à son tour
            damage = Math.max(0, hp - wall);
            phaseWallReached = true;
            notice = kind == EnemyKind.MACHINE_ORIGINELLE ? Lang.f("ELLE RÉSISTE : PHASE {0} À SON TOUR", phase + 1)
                : Lang.f("GARDE : INVINCIBLE JUSQU'À SON TOUR, PHASE {0}", phase + 1);
        }
        if (kind.hasLastDraw() && !lastDrawDone && damage >= hp) {
            lastDrawPending = true;
            notice = Lang.t("ELLE RÉSISTE ! DERNIER TIRAGE");
            return hp - 1;
        }
        return damage;
    }

    /**
     * Seuils de phase des boss et des rois (choix d'Astra, voir {@link EnemyKind#phaseThresholds()}) :
     * ses PV ne descendent pas sous le seuil de sa phase avant qu'il soit passé à la suivante, à son tour.
     *
     * @return les PV sous lesquels il ne descend pas encore, ou 0
     */
    private long phaseWall() {
        float[] thresholds = kind.phaseThresholds();
        if (phase > thresholds.length) return 0;
        return (long) Math.ceil(maxHp * (double) thresholds[phase - 1]);
    }

    /** @return {@code true} s'il a atteint le seuil de sa phase : il passera à la suivante à son tour. */
    public boolean isPhaseWallReached() { return phaseWallReached; }

    /** @return ce qu'il vient de faire à un coup (coup annulé, résistance), une seule fois, ou {@code null}. */
    public String takeNotice() {
        String taken = notice;
        notice = null;
        return taken;
    }

    /** @return {@code true} si la Maison a déjà annulé un coup. */
    public boolean isHouseUsed() { return houseUsed; }

    /** @return {@code true} si la Machine Originelle attend son Dernier tirage. */
    public boolean isLastDrawPending() { return lastDrawPending; }

    /**
     * Fin du Dernier tirage : gagné par le joueur, la Machine Originelle tombe ;
     * perdu, elle reste à 1 PV (le joueur, lui, a perdu).
     */
    public void endLastDraw(boolean playerWins) {
        lastDrawPending = false;
        lastDrawDone    = true;
        if (playerWins) hp = 0;
    }

    /** Sa Prédiction annonce {@code symbol} pour le prochain tirage du joueur. */
    public void predict(Symbol symbol) { predict(symbol, null); }

    /** Sa Prédiction annonce {@code symbol}, et {@code second} avec le Tarot ({@code null} sinon). */
    public void predict(Symbol symbol, Symbol second) {
        prediction       = symbol;
        secondPrediction = second;
    }

    /** @return le deuxième symbole annoncé (Tarot), ou {@code null}. */
    public Symbol getSecondPrediction() { return secondPrediction; }

    /** @return le symbole annoncé par sa Prédiction, ou {@code null}. */
    public Symbol getPrediction() { return prediction; }

    /**
     * Tirage du joueur : si le symbole prédit est sorti, ses attaques du tour triplent.
     *
     * @return {@code true} si sa Prédiction s'est réalisée
     */
    public boolean checkPrediction(Symbol[] symbols) {
        if (prediction == null) return false;
        for (Symbol symbol : symbols) {
            if (symbol == prediction || symbol != null && symbol == secondPrediction) predictionHit = true;
        }
        prediction       = null;
        secondPrediction = null;
        return predictionHit;
    }

    /** @return {@code true} (une seule fois) si sa Prédiction s'est réalisée au dernier tirage du joueur. */
    public boolean takePredictionHit() {
        boolean hit = predictionHit;
        predictionHit = false;
        return hit;
    }

    /** Le Portier laisse dehors la famille {@code family} tout le combat. */
    public void banFamily(CardFamily family) { bannedFamily = family; }

    /** @return la famille de cartes interdite (le Portier), ou {@code null}. */
    public CardFamily getBannedFamily() { return bannedFamily; }

    /** Le Pilleur garde une copie d'un achat : un As sombre de la couleur {@code suit} rejoint son deck. */
    public void addLoot(Card.Suit suit) {
        deck.insertRandomly(EnemyCards.loot(suit));
        loot++;
    }

    /** @return les As gardés en copie des achats du joueur. */
    public int getLoot() { return loot; }

    /** @return le nombre de rouleaux de sa machine, dans sa phase. */
    public int getReelCount() {
        return kind.has(EnemyTrait.REEL_THEFT) ? EnemyKind.MACHINE_REELS + getStolenReels() : kind.getReelCount(phase);
    }

    /** @return les rouleaux qu'il a volés au joueur (la Machine Originelle blessée). */
    public int getStolenReels() {
        if (!kind.has(EnemyTrait.REEL_THEFT)) return kind.stolenReels(phase);
        // le rouleau de la phase 2 revient au bout de quelques tours ; celui de la phase 3 reste (choix d'Astra)
        return (phase >= 2 && theftTurns > 0 ? 1 : 0) + (phase >= 3 ? 1 : 0);
    }

    /** @return les tours du joueur pendant lesquels elle garde encore le rouleau volé en phase 2. */
    public int getTheftTurns() { return theftTurns; }

    /** Début de son tour : le rouleau volé en phase 2 a passé un tour de plus chez elle. */
    public void tickTheft() { if (theftTurns > 0) theftTurns--; }

    /** Mur porteur : sa défense ne s'use pas jusqu'à son prochain tour. */
    public void holdDefense() { solidDefense = true; }

    /** @return {@code true} si sa défense ne s'use pas sous les coups (Mur porteur). */
    public boolean isDefenseSolid() { return solidDefense; }

    /** Placage, Battement : sa peau d'or tient {@code turns} tours, même sous la moitié de ses PV. */
    public void plate(int turns) { platingTurns = Math.max(platingTurns, turns); }

    /** @return les tours où sa peau d'or tient encore grâce à un Placage ou un Battement. */
    public int getPlatingTurns() { return platingTurns; }

    /** Début de son tour : son Placage a tenu un tour de plus. */
    public void tickPlating() { if (platingTurns > 0) platingTurns--; }

    /** Pièce avalée : {@code count} pièces de plus. @return ses pièces */
    public int swallowCoins(int count) { return coins += count; }

    /** @return ses pièces avalées. */
    public int getCoins() { return coins; }

    /** @return {@code true} (et les dépense) s'il a assez de pièces pour un Jackpot. */
    public boolean spendCoins() {
        if (coins < EnemySymbol.COINS_FOR_JACKPOT) return false;
        coins -= EnemySymbol.COINS_FOR_JACKPOT;
        return true;
    }

    /** Aspiration : {@code count} rouleaux du joueur aspirés à son dernier tirage. */
    public void setAspired(int count) { aspired = count; }

    /** @return les rouleaux aspirés au dernier tirage du joueur. */
    public int getAspired() { return aspired; }

    /** @return les dégâts qui le touchent vraiment : la moitié, tant que sa peau d'or tient (plus de la moitié de ses PV). */
    /**
     * Part d'un coup qui compte vraiment contre lui : de quoi user toute sa
     * défense et tous ses PV, même à travers sa peau d'or (au-delà, ça ne
     * change rien, et les calculs restent loin des limites des nombres).
     * Le nombre affiché, lui, reste le vrai coup.
     */
    public long capHit(long damage) { return Math.min(damage, 2L * maxHp + defense); }

    public long skinned(long damage) {
        return hasGoldSkin() ? damage / 2 : damage;
    }

    /** @return {@code true} si sa peau d'or encaisse la moitié des coups en ce moment. */
    public boolean hasGoldSkin() { return kind.hasGoldSkin() && (hp * 2L > maxHp || platingTurns > 0); }

    /**
     * @return les cartes qu'il joue ce tour : celles de son type, ou une par bras
     *         qui lui reste (le Kraken perd un bras à chaque huitième de ses PV)
     */
    public int getPlaysPerTurn() {
        if (!kind.hasArms()) return kind.getPlaysPerTurn();
        return Math.max(1, (int) Math.ceil(EnemyKind.KRAKEN_ARMS * (double) hp / maxHp) - cutArms);
    }

    /** @return les malus posés sur lui par les cartes du joueur, en attente de son prochain tour. */
    public EnemyMaluses getMaluses() { return maluses; }

    /** Harpon : une carte de moins à son prochain tour ; le Kraken, lui, perd un bras pour de bon. */
    public void harpoon() {
        if (kind.hasArms()) cutArms++;
        else maluses.harpoon();
    }

    /**
     * Retire {@code damage} PV sans que sa peau d'or n'en encaisse la moitié
     * (Dynamite). La Maison et la Machine Originelle se gardent comme d'habitude.
     *
     * @return les points de vie réellement retirés
     */
    public int takeTrueDamage(long damage) {
        return takeDamage(hasGoldSkin() ? damage * 2 : damage);
    }

    /** Ajoute {@code amount} à son Enclume (sans limite). */
    public void addAnvil(int amount) { anvil += amount; }

    /** @return l'attaque ajoutée par ses Enclumes, jusqu'à la fin du combat. */
    public int getAnvil() { return anvil; }

    /**
     * Rend {@code amount} points de vie, sans dépasser le maximum. Chaque soin
     * rend au plus {@link EnemyKind#MAX_HEAL_PERCENT} % de ses PV max (Astra, 2026-11-01).
     *
     * @return les points de vie réellement rendus
     */
    public int heal(int amount) {
        amount = (int) Math.min(amount, (long) maxHp * EnemyKind.MAX_HEAL_PERCENT / 100);
        amount = (int) ((long) amount * healPercent() / 100); // un roi en phase 2 se soigne moins
        int healed = Math.min(maxHp, hp + amount) - hp;
        hp += healed;
        return healed;
    }

    /** @return le % de ses attaques : plus fortes pour un roi de l'Exploration en phase 2. */
    public int attackPercent() {
        return phase >= 2 && kind.kingPhaseSymbol() != null ? EnemyKind.KING_PHASE_ATTACK_PERCENT : 100;
    }

    /** @return le % de ses soins : réduits pour un roi de l'Exploration en phase 2. */
    public int healPercent() {
        return phase >= 2 && kind.kingPhaseSymbol() != null ? EnemyKind.KING_PHASE_HEAL_PERCENT : 100;
    }

    /** @return {@code true} si l'ennemi n'a plus de points de vie. */
    public boolean isDefeated() { return hp <= 0; }

    /** Ajoute {@code amount} à la défense (Bouclier de son tour). */
    public void addShieldDefense(int amount) { defense += amount; }

    /** Reforme la défense de base, sans les Boucliers du tour précédent (au début d'un nouveau tour de l'ennemi). */
    public void resetDefense() {
        defense = getBaseDefense();
        solidDefense = false;
    }

    /** Pot-de-vin : sa défense tombe à 0 jusqu'à son tour. @return la défense retirée */
    public int bribe() {
        int removed = defense;
        defense = 0;
        return removed;
    }

    /**
     * Début de son tour : ses Épines du tour précédent tombent.
     *
     * @return les dégâts à renvoyer au joueur (part des dégâts encaissés depuis la fin de son dernier tour)
     */
    public int collectThorns() {
        int thorns = Math.min(kind.thornsMax(hard), Math.round(damageTaken * thornsPercent / 100f));
        thornsPercent = 0;
        return thorns;
    }

    /** Fin de son tour : ses Épines ne compteront que les coups encaissés à partir de maintenant. */
    public void resetDamageTaken() { damageTaken = 0; }

    /** Ajoute {@code percent} à ses Épines, jusqu'à son tour suivant. */
    public void addThorns(int percent) { thornsPercent += percent; }

    /** Ajoute {@code amount} à sa Rage (plafonnée à {@link EnemySymbol#RAGE_MAX}). @return la Rage ajoutée */
    public int addRage(int amount) {
        int added = Math.min(amount, EnemySymbol.RAGE_MAX - rage);
        rage += Math.max(0, added);
        return Math.max(0, added);
    }

    /** Ses Intérêts : {@code stolen} gains volés renforcent son prochain coup. @return l'attaque ajoutée */
    public int addInterest(int stolen) {
        int added = Math.min(EnemySymbol.INTEREST_MAX - interest, stolen / EnemySymbol.INTEREST_PER_ATTACK);
        interest += Math.max(0, added);
        return Math.max(0, added);
    }

    /** Son coup part : l'attaque de ses Intérêts est dépensée. @return l'attaque à ajouter à ce coup */
    public int spendInterest() {
        int spent = interest;
        interest = 0;
        return spent;
    }

    /**
     * Son Sablier avance d'un cran.
     *
     * @return {@code true} s'il arrive au bout : il explose et repart de zéro
     */
    public boolean tickHourglass() {
        hourglass++;
        if (hourglass < EnemySymbol.HOURGLASS_MAX) return false;
        hourglass = 0;
        return true;
    }

    /** Tapis : sa mise doublera à son tour suivant, s'il n'est pas touché d'ici là. */
    public void goAllIn() { allIn = true; }

    /**
     * Début de son tour : son Tapis du tour précédent, s'il tient encore, double
     * sa mise (plafonnée à {@link EnemySymbol#ALL_IN_MAX}).
     *
     * @return {@code true} si sa mise vient de doubler
     */
    public boolean raiseStake() {
        if (!allIn) return false;
        allIn = false;
        int before = stake;
        stake = Math.min(EnemySymbol.ALL_IN_MAX, stake * 2);
        return stake > before;
    }

    /** Numéro : il annonce la famille {@code suit} ; si le joueur n'en joue pas, ses attaques suivantes valent x{@code factor}. */
    public void announceAct(Card.Suit suit, int factor) {
        actSuit = suit;
        actFactor = Math.max(actFactor, factor);
    }

    /** @return la famille annoncée par son Numéro, ou {@code null} */
    public Card.Suit getActSuit() { return actSuit; }

    /**
     * Début de son tour : son Numéro est-il raté ?
     *
     * @return le facteur de ses attaques du tour (1 si le joueur a joué la famille annoncée, ou sans Numéro)
     */
    public int takeActFactor(List<Card> played) {
        if (actSuit == null) return 1;
        Card.Suit suit = actSuit;
        int factor = actFactor;
        actSuit = null;
        actFactor = 0;
        return played.stream().anyMatch(card -> card.getSuit() == suit) ? 1 : factor;
    }

    /** Sarcophage : il s'y enferme jusqu'à son prochain tour. */
    public void seal() { sealed = true; }

    /** Début de son tour : le Sarcophage s'ouvre. */
    public void unseal() { sealed = false; }

    /** @return {@code true} s'il est enfermé dans son Sarcophage. */
    public boolean isSealed() { return sealed; }

    /**
     * Début de son tour : l'Éclat Originel passe à sa deuxième phase sous
     * {@link EnemyKind#PHASE_TWO_RATIO} de vie (ou dès que sa Garde l'a arrêté au seuil de sa phase).
     *
     * @return {@code true} s'il vient de changer de phase
     */
    public boolean enterPhaseTwo() {
        int reached = kind.phaseFor(getHpRatio());
        if (phaseWallReached) reached = Math.max(reached, phase + 1); // il a atteint son seuil de phase
        phaseWallReached = false;
        reached = Math.min(reached, Math.min(phase + 1, kind.phaseCount())); // une phase à la fois : chacune s'annonce
        if (reached <= phase) return false;
        if (kind.has(EnemyTrait.REEL_THEFT) && phase < 2) theftTurns = EnemySymbol.MACHINE_THEFT_TURNS;
        phase = reached;
        return true;
    }

    /**
     * Une attaque du joueur frappe la défense : elle absorbe tout ce qu'elle
     * peut et s'use d'autant.
     *
     * @param damage dégâts bruts de l'attaque
     * @return les dégâts absorbés par la défense (le reste touche l'ennemi)
     */
    /**
     * Comme {@link #absorb(long)}, quand {@code piercePercent} % du coup traverse
     * la défense (Pique) : seul le reste peut être absorbé.
     *
     * @return les dégâts absorbés
     */
    public int absorb(long damage, int piercePercent) {
        int pierce = Math.max(0, Math.min(100, piercePercent));
        return absorb(damage - damage * pierce / 100);
    }

    public int absorb(long damage) {
        int absorbed = (int) Math.min(defense, Math.max(0, damage));
        if (!solidDefense) defense -= absorbed; // Mur porteur : la défense tient
        return absorbed;
    }

    /** Pioche {@code count} cartes (la défausse est remélangée si le deck s'épuise). */
    public List<Card> draw(int count) { return deck.draw(count); }

    /** Range son deck dans l'ordre {@code order} : il pioche par la fin (le tutoriel lui donne ses cartes faibles d'abord). */
    public void arrangeDeck(Comparator<Card> order) { deck.getCards().sort(order); }

    /** Envoie {@code cards} dans la défausse (fin du tour de l'ennemi). */
    public void discard(List<Card> cards) { discardPile.addAll(cards); }

    /** Carte propre {@code signature} jouée : son effet attend le prochain symbole qu'elle renforce. */
    public void addCharge(EnemySignature signature) { charges.merge(signature, 1, Integer::sum); }

    /** @return les cartes propres en attente de leur symbole, et leur nombre. */
    public Map<EnemySignature, Integer> getCharges() { return Collections.unmodifiableMap(charges); }

    /** @return {@code true} (et la consomme) si une carte {@code signature} attendait son symbole. */
    public boolean takeCharge(EnemySignature signature) {
        Integer count = charges.get(signature);
        if (count == null || count <= 0) return false;
        if (count == 1) charges.remove(signature);
        else charges.put(signature, count - 1);
        return true;
    }

    /** Tout sur le rouge : son Tapis tient un coup de plus avant de retomber. */
    public void holdAllIn() { allInHolds++; }

    /** @return les coups que son Tapis peut encore encaisser sans retomber. */
    public int getAllInHolds() { return allInHolds; }

    /**
     * Compte à rebours : le combat perd un tour (au plus {@link EnemySignature#COMPTE_MAX} sur tout le combat).
     *
     * @return {@code true} si le combat a perdu un tour
     */
    public boolean loseTurn() {
        if (lostTurns >= EnemySignature.COMPTE_MAX || kind.getTurnLimit() <= 0) return false;
        lostTurns++;
        return true;
    }

    /** @return les tours retirés au combat par ses Comptes à rebours. */
    public int getLostTurns() { return lostTurns; }

    /** @return les tours que dure le combat contre lui (Temps Mort, moins ses Comptes à rebours), 0 sans limite. */
    public int getTurnLimit() {
        int limit = kind.getTurnLimit();
        return limit > 0 ? limit - lostTurns : 0;
    }

    /** @return le type d'ennemi : ses rouleaux, sa façon de jouer, son portrait. */
    public EnemyKind getKind()     { return kind; }
    /** @return {@code true} s'il est en mode difficile (Tour des épreuves). */
    public boolean isHard()        { return hard; }
    /** @return le bonus d'attaque de sa Rage. */
    public int    getRage()        { return rage; }
    /** @return l'attaque que ses Intérêts ajouteront à son prochain coup. */
    public int    getInterest()    { return interest; }
    /** @return le compte à rebours de son Sablier. */
    public int    getHourglass()   { return hourglass; }
    /** @return {@code true} si son Tapis doublera sa mise à son prochain tour. */
    public boolean isAllIn()       { return allIn; }
    /** @return sa mise : ses attaques sont multipliées d'autant (Tapis). */
    public int    getStake()       { return stake; }
    /** @return sa phase (1, ou 2 pour l'Éclat Originel blessé). */
    public int    getPhase()       { return phase; }
    /** @return les symboles de ses rouleaux et leur poids, dans sa phase. */
    public Map<EnemySymbol, Integer> getWeights() { return kind.getWeights(phase); }

    /** @return les poids que ses rouleaux tirent vraiment (sans le Rien ne va plus du Croupier en chef, tiré à part). */
    public Map<EnemySymbol, Integer> getSpinWeights() { return kind.getSpinWeights(phase); }
    /** @return les symboles qui peuvent sortir sur ses rouleaux, dans sa phase. */
    public List<EnemySymbol> getSymbols() { return kind.getSymbols(phase); }
    /** @return la part des dégâts du joueur que renverront ses Épines, en %. */
    public int    getThornsPercent() { return thornsPercent; }
    /** @return sa défense de base, reformée à chacun de ses tours. */
    public int    getBaseDefense() { return kind.has(EnemyTrait.FALLING_FACADE) && phase >= 2 ? 0 : kind.getBaseDefense(hard); }
    /** @return le nom affiché de l'ennemi. */
    public String getName()        { return name; }
    /** @return les points de vie actuels. */
    public int    getHp()          { return hp; }
    /** @return les points de vie maximum. */
    public int    getMaxHp()       { return maxHp; }
    /** @return la proportion de vie restante, entre 0 et 1. */
    public float  getHpRatio()     { return (float) hp / maxHp; }
    /** @return la défense restante de l'ennemi (base et Boucliers, moins ce que les attaques ont déjà usé). */
    public int    getDefense()     { return defense; }
    /** @return les cartes restantes dans le deck de l'ennemi. */
    public List<Card> getDeckCards()    { return Collections.unmodifiableList(new ArrayList<>(deck.getCards())); }
    /** @return les cartes de la défausse de l'ennemi. */
    public List<Card> getDiscardCards() { return discardPile.getCards(); }
}
