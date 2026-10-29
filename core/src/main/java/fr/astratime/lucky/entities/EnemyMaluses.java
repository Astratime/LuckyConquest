package fr.astratime.lucky.entities;

/**
 * Les malus que les cartes du joueur posent sur l'ennemi et qui attendent son
 * prochain tour : Rayon du phare, Mutinerie, Huissier, Pièce truquée, Harpon
 * (cartes des lieux), Créances du Rentier, malus du Bluffeur. Le pendant, côté
 * ennemi, des {@link LastingEffects} du joueur (voir {@link Enemy#getMaluses()}).
 *
 * Chaque malus se pose ({@code add…}, {@code dazzle}…), se lit ({@code get…}),
 * puis se consomme quand son tour arrive ({@code take…}).
 */
public class EnemyMaluses {

    private final boolean boss;
    /** Rayon du phare : ébloui, il passe son prochain tour. */
    private boolean dazzled;
    /** Mutinerie : à son prochain tour, ses cartes se retournent contre lui. */
    private int     mutinies;
    /** Huissier : le joueur est très endetté, il joue une carte de plus à son prochain tour. */
    private boolean bailiff;
    /** Pièce truquée : ses prochains tirages ne font pas de Jackpot. */
    private int     loadedCoins;
    /** Harpon : cartes de moins à son prochain tour. */
    private int     harpoons;
    /** Créances du Rentier. */
    private int     claims;
    /** Sceau du Pharaon : ses tours où les Créances comptent double. */
    private int     pharaohSealTurns;
    /** Intérêts composés : tirages du joueur où ses Créances grossissent encore, et de combien (%). */
    private int     compoundTurns;
    private int     compoundPercent;
    /** Malus du Bluffeur : Intimidation (%), Cartes marquées (tours), Fausses pistes, Tours de passe-passe. */
    private int     intimidation;
    private int     markedTurns;
    private int     redHerrings;
    private int     sleights;

    /** @param boss {@code true} pour un boss ou un roi : il ne subit qu'une part des malus du Bluffeur */
    EnemyMaluses(boolean boss) {
        this.boss = boss;
    }

    /** Rayon du phare : il passe son prochain tour. */
    public void dazzle() { dazzled = true; }
    /** @return {@code true} s'il est ébloui. */
    public boolean isDazzled() { return dazzled; }
    /** Son tour arrive : l'éblouissement vaut pour celui-ci. @return {@code true} s'il était ébloui */
    public boolean takeDazzle() {
        boolean was = dazzled;
        dazzled = false;
        return was;
    }

    /** Mutinerie : ses cartes du prochain tour se retournent contre lui. */
    public void addMutiny() { mutinies++; }
    /** @return les Mutineries en attente. */
    public int getMutinies() { return mutinies; }
    /** Son tour : la Mutinerie vaut pour celui-ci. @return {@code true} si elle était en attente */
    public boolean takeMutiny() {
        if (mutinies <= 0) return false;
        mutinies--;
        return true;
    }

    /** Huissier (le joueur est très endetté) : il joue une carte de plus à son prochain tour. */
    public void sendBailiff() { bailiff = true; }
    /** Son tour : l'Huissier vaut pour celui-ci. @return {@code true} s'il était là */
    public boolean takeBailiff() {
        boolean was = bailiff;
        bailiff = false;
        return was;
    }

    /** Pièce truquée : son prochain tirage ne fera pas de Jackpot. */
    public void addLoadedCoin() { loadedCoins++; }
    /** @return les Pièces truquées en attente. */
    public int getLoadedCoins() { return loadedCoins; }
    /** Son tirage : une Pièce truquée vaut pour celui-ci. @return {@code true} s'il y en avait une */
    public boolean takeLoadedCoin() {
        if (loadedCoins <= 0) return false;
        loadedCoins--;
        return true;
    }

    /** Harpon : une carte de moins à son prochain tour. */
    public void harpoon() { harpoons++; }
    /** @return les cartes de moins qu'il jouera à son prochain tour (Harpon). */
    public int getHarpoons() { return harpoons; }
    /** Son tour : les Harpons valent pour celui-ci. @return le nombre de cartes de moins */
    public int takeHarpoons() {
        int n = harpoons;
        harpoons = 0;
        return n;
    }

    // -------------------------------------------------------------------------
    // Archétypes de deck : Créances (Rentier) et malus du Bluffeur
    // -------------------------------------------------------------------------

    /** Créances qu'il peut porter au plus. */
    public static final int MAX_CLAIMS = 20;
    /** Ce que chaque Créance lui retire à son tour, en pour mille de ses PV max (0,5 %). */
    public static final int CLAIM_HP_PER_MILLE = 5;
    /** Ce que chaque Créance rapporte au joueur à chaque tour de l'ennemi, en coûts de tirage. */
    public static final int CLAIM_SPINS = 1;
    /** Un boss ou un roi ne subit que cette part des malus du Bluffeur, en %. */
    public static final int BOSS_MALUS_PERCENT = 50;

    /** Ajoute {@code count} Créances, sans dépasser {@link #MAX_CLAIMS}. @return les Créances ajoutées */
    public int addClaims(int count) {
        int before = claims;
        claims = Math.max(0, Math.min(MAX_CLAIMS, claims + count));
        return claims - before;
    }
    /** @return ses Créances. */
    public int getClaims() { return claims; }
    /** Recouvrement : toutes ses Créances sont encaissées. @return leur nombre avant */
    public int takeClaims() {
        int n = claims;
        claims = 0;
        return n;
    }

    /** Sceau du Pharaon : ses Créances comptent double pendant ses {@code turns} prochains tours. */
    public void pharaohSeal(int turns) { pharaohSealTurns = Math.max(pharaohSealTurns, turns); }
    /** @return ses prochains tours sous le Sceau du Pharaon. */
    public int getPharaohSealTurns() { return pharaohSealTurns; }
    /** Son tour : le Sceau du Pharaon vaut pour celui-ci. @return {@code true} si ses Créances comptent double */
    public boolean takePharaohSeal() {
        if (pharaohSealTurns <= 0) return false;
        pharaohSealTurns--;
        return true;
    }

    /** Intérêts composés : ses Créances grossissent de {@code percent} % pendant {@code turns} tirages du joueur. */
    public void compound(int turns, int percent) {
        compoundTurns   = Math.max(compoundTurns, turns);
        compoundPercent = Math.max(compoundPercent, percent);
    }
    /** @return les tirages du joueur où ses Créances grossiront encore (Intérêts composés). */
    public int getCompoundTurns() { return compoundTurns; }
    /**
     * Début d'un tirage du joueur : sous les Intérêts composés, ses Créances
     * grossissent (au moins d'une).
     *
     * @return les Créances ajoutées (0 sans Intérêts composés ou sans Créance)
     */
    public int growClaims() {
        if (compoundTurns <= 0) return 0;
        int added = claims > 0 ? addClaims(Math.max(1, (int) Math.ceil(claims * compoundPercent / 100.0))) : 0;
        if (--compoundTurns == 0) compoundPercent = 0;
        return added;
    }

    /** @return la part (en %) d'un malus du Bluffeur qu'il subit : moins s'il est un boss ou un roi. */
    public int malusShare(int percent) {
        return boss ? percent * BOSS_MALUS_PERCENT / 100 : percent;
    }

    /** Intimidation : ses attaques baissent de {@code percent} % à son prochain tour (la plus forte compte). */
    public void intimidate(int percent) { intimidation = Math.max(intimidation, percent); }
    /** @return la baisse de ses attaques à son prochain tour, en %, avant la réduction d'un boss (0 : aucune). */
    public int getIntimidation() { return intimidation; }
    /** Son tour : l'Intimidation vaut pour celui-ci. @return la baisse de ses attaques, en % (réduite pour un boss) */
    public int takeIntimidation() {
        int percent = malusShare(intimidation);
        intimidation = 0;
        return percent;
    }

    /** Cartes marquées : ses cartes à couleur valent moins pendant ses {@code turns} prochains tours. */
    public void markCards(int turns) { markedTurns = Math.max(markedTurns, turns); }
    /** @return ses prochains tours sous les Cartes marquées. */
    public int getMarkedTurns() { return markedTurns; }
    /** Son tour : les Cartes marquées valent pour celui-ci. @return {@code true} si elles étaient actives */
    public boolean takeMarkedCards() {
        if (markedTurns <= 0) return false;
        markedTurns--;
        return true;
    }

    /** Fausse piste : un de ses rouleaux ne tournera pas à son prochain tirage. */
    public void addRedHerring() { redHerrings++; }
    /** @return les Fausses pistes en attente. */
    public int getRedHerrings() { return redHerrings; }
    /** Son tirage : une Fausse piste vaut pour celui-ci. @return {@code true} s'il y en avait une */
    public boolean takeRedHerring() {
        if (redHerrings <= 0) return false;
        redHerrings--;
        return true;
    }

    /** Tour de passe-passe : son prochain effet spécial sera annulé. */
    public void addSleight() { sleights++; }
    /** @return les Tours de passe-passe en attente. */
    public int getSleights() { return sleights; }
    /** Un de ses effets spéciaux arrive : un Tour de passe-passe vaut pour lui. @return {@code true} s'il y en avait un */
    public boolean takeSleight() {
        if (sleights <= 0) return false;
        sleights--;
        return true;
    }

    /**
     * @return le nombre de malus différents qui le gênent en ce moment (Coup de bluff) :
     *         ébloui, Mutinerie, Pièce truquée, Harpon, Intimidation, Cartes marquées,
     *         Fausse piste, Tour de passe-passe, Créances
     */
    public int getActiveMaluses() {
        int count = 0;
        if (dazzled) count++;
        if (mutinies > 0) count++;
        if (loadedCoins > 0) count++;
        if (harpoons > 0) count++;
        if (intimidation > 0) count++;
        if (markedTurns > 0) count++;
        if (redHerrings > 0) count++;
        if (sleights > 0) count++;
        if (claims > 0) count++;
        return count;
    }
}
