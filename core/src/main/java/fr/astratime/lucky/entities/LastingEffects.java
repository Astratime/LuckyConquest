package fr.astratime.lucky.entities;

import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Effets de cartes qui durent plus d'un tour, pour le combat en cours,
 * affichés dans le panneau latéral :
 *  - symboles retirés des rouleaux (Recyclage, pour quelques tours) ;
 *  - bonus de gains (Porte-bonheur, jusqu'à la fin du combat) ;
 *  - Corruption (attaque et défense des symboles décuplées, quelques tours) ;
 *  - Dans la manche (plus de cartes jouables par tour, quelques tours) ;
 *  - Coffres-forts (gains mis de côté, rendus doublés quelques tours plus tard) ;
 *  - les jauges des couleurs, remplies au fil du combat et vidées par leur As :
 *    Lames (Pique), Sang (Coeur) et Coffre (Carreau) ;
 *  - Veine d'or (gains x3, quelques tours) et Bulle d'air (la règle du lieu ne
 *    joue plus, quelques tours) ;
 *  - les mauvais sorts des ennemis de l'Exploration, pour le prochain tour :
 *    Grignotage, Aveuglement, Chant (à la pioche) ; Ivresse, Pépite (au tirage) ;
 *  - les parades des cartes des coffres des lieux : Piège à rats, Lanterne,
 *    Bouchons d'oreille, Étai, Lampe à carbure, Cage à requin, Ancre, et la
 *    Trempe qui renforce l'attaque à chaque tour.
 */
public class LastingEffects {

    /** Symboles retirés des rouleaux, avec le nombre de tirages restants avant leur retour. */
    private final Map<Symbol, Integer> removedSymbols = new EnumMap<>(Symbol.class);
    /** Bonus de gains pour tout le combat (0.5 = +50 %), cumulé sur les Porte-bonheur joués. */
    private float gainBonus = 0f;
    /** Tirages restants sous l'effet de la Corruption (0 : inactive). */
    private int corruptionTurns = 0;
    /** Dans la manche : cartes jouables par tour tant qu'il reste des tours d'effet. */
    private int extraPlays = 0;
    /** Tours restants sous l'effet de Dans la manche (0 : inactif). */
    private int extraPlaysTurns = 0;
    /** Lames (Pique) : chacune ajoute de l'attaque à tous les symboles. */
    private int blades = 0;
    /** Sang (Coeur) : soin reçu au-delà des PV max. */
    private int blood  = 0;
    /** Coffre (Carreau) : bouclier resté inutilisé à la fin des tours. */
    private int vault  = 0;
    /** Rouleau bloqué au prochain tirage par le Rouleau interdit de l'ennemi (-1 : aucun). */
    private int forbiddenReel = -1;
    /** Coffres-forts : gains mis de côté (déjà doublés) et tours restants avant leur ouverture. */
    private final List<int[]> safes = new ArrayList<>();
    /** Veine d'or : tirages restants dont les gains sont multipliés (0 : inactive). */
    private int goldVeinTurns = 0;
    /** Bulle d'air : tours restants pendant lesquels la règle du lieu ne joue pas (0 : inactive). */
    private int bubbleTurns = 0;
    /** Grignotage : cartes rongées dans la main, à la prochaine pioche. */
    private int nibbles = 0;
    /** Aveuglement : la prochaine main est piochée face cachée. */
    private boolean blind = false;
    /** Chant : cartes de la prochaine main jouées d'office, au hasard. */
    private int songs = 0;
    /** Ivresse : rouleaux qui tourneront deux fois au prochain tirage (le pire résultat reste). */
    private int drunk = 0;
    /** Pépite : au prochain tirage, les symboles de gain ne rapportent rien. */
    private boolean nugget = false;
    /** Gains devenus faux (Fausse monnaie) : ils disparaissent au prochain tirage, sauf ce qui est dépensé avant. */
    private int fakeGains = 0;
    /** Taxes (le Comptable) pour le prochain tour : chaque carte jouée paie autant de fois la Taxe. */
    private int taxes = 0;
    /** Règle changée par le Directeur des Jeux ({@code null} : aucune), et ses tirages restants. */
    private HouseRule houseRule;
    private int houseRuleTurns = 0;
    /** Piège à rats : mauvais sorts de l'ennemi sur la main (Grignotage, Aveuglement, Chant, Abordage, Fouille) annulés. */
    private int traps = 0;
    /** Lanterne : pioches restantes où la main ne peut pas être cachée. */
    private int lanternDraws = 0;
    /** Bouchons d'oreille : pioches restantes où le Chant n'a pas d'effet. */
    private int earplugDraws = 0;
    /** Étai : tours restants où le Forage ne perce pas le bouclier. */
    private int propTurns = 0;
    /** Lampe à carbure : coups de grisou à éviter. */
    private int lamps = 0;
    /** Cage à requin : tours restants où l'ennemi ne peut rien prendre aux gains. */
    private int cageTurns = 0;
    /** Ancre : tours restants où la marée haute ne retire pas d'attaque. */
    private int anchorTurns = 0;
    /** Trempe : bonus d'attaque gagné à chaque tour, et bonus atteint, en %. */
    private int temperCards = 0;
    private int temperPercent = 0;
    /** Cocktail des abysses : cartes jouables en plus, ce tour seulement. */
    private int bonusPlays = 0;

    /** Règles que peut changer le Directeur des Jeux (Nouvelle règle). */
    public enum HouseRule {
        /** Les combinaisons de cartes ne comptent plus. */
        NO_COMBOS("Sans combinaisons", "LES COMBINAISONS NE COMPTENT PLUS"),
        /** Les rouleaux ne peuvent plus faire de Bingo. */
        NO_BINGO("Sans Bingo", "BINGO INTERDIT"),
        /** Chaque rouleau tourne deux fois et garde le pire résultat. */
        DOUBLE_SPIN("Rouleaux x2", "TES ROULEAUX TOURNENT DEUX FOIS");

        private final String shortName;
        private final String announce;

        HouseRule(String shortName, String announce) {
            this.shortName = shortName;
            this.announce  = announce;
        }

        /** @return le nom court de la règle (panneau des effets). */
        public String getShortName() { return Lang.t(shortName); }
        /** @return le texte qui annonce la règle. */
        public String getAnnounce() { return Lang.t(announce); }
    }

    /** Retire {@code symbol} des rouleaux pour les {@code turns} prochains tirages (prolonge s'il l'est déjà). */
    public void removeSymbol(Symbol symbol, int turns) {
        removedSymbols.merge(symbol, turns, Math::max);
    }

    /** @return les symboles retirés et leurs tirages restants (vue non modifiable, dans l'ordre des symboles). */
    public Map<Symbol, Integer> getRemovedSymbols() {
        return Collections.unmodifiableMap(removedSymbols);
    }

    /** Ajoute {@code bonus} au bonus de gains du combat (0.5 = +50 %). */
    public void addGainBonus(float bonus) { gainBonus += bonus; }

    /** @return le bonus de gains du combat (0 si aucun). */
    public float getGainBonus() { return gainBonus; }

    /** Active la Corruption pour les {@code turns} prochains tirages (prolonge si elle l'est déjà). */
    public void addCorruption(int turns) { corruptionTurns = Math.max(corruptionTurns, turns); }

    /** @return les tirages restants sous l'effet de la Corruption (0 si inactive). */
    public int getCorruptionTurns() { return corruptionTurns; }

    /**
     * Dans la manche : {@code plays} cartes jouables par tour pendant les
     * {@code turns} prochains tours, celui-ci compris (prolonge et garde la
     * meilleure limite si l'effet est déjà actif).
     */
    public void addExtraPlays(int plays, int turns) {
        extraPlays      = Math.max(extraPlays, plays);
        extraPlaysTurns = Math.max(extraPlaysTurns, turns);
    }

    /** @return les cartes jouables par tour sous Dans la manche, ou 0 s'il est inactif. */
    public int getExtraPlays() { return extraPlaysTurns > 0 ? extraPlays : 0; }

    /** @return les tours restants sous Dans la manche (0 s'il est inactif). */
    public int getExtraPlaysTurns() { return extraPlaysTurns; }

    /** Ajoute {@code count} Lames. */
    public void addBlades(int count) { blades += count; }
    /** @return le nombre de Lames. */
    public int getBlades() { return blades; }
    /** Vide les Lames. @return leur nombre avant d'être vidées */
    public int consumeBlades() { int n = blades; blades = 0; return n; }

    /** Ajoute {@code amount} au Sang. */
    public void addBlood(int amount) { blood += amount; }
    /** @return la réserve de Sang. */
    public int getBlood() { return blood; }
    /** Vide le Sang. @return la réserve avant d'être vidée */
    public int consumeBlood() { int n = blood; blood = 0; return n; }

    /** Ajoute {@code amount} au Coffre. */
    public void addVault(int amount) { vault += amount; }
    /** @return le contenu du Coffre. */
    public int getVault() { return vault; }
    /** Vide le Coffre. @return son contenu avant d'être vidé */
    public int consumeVault() { int n = vault; vault = 0; return n; }

    /** Multiplie les trois jauges (Lames, Sang, Coffre) par {@code factor} (ex : Pot de Lutin). */
    public void multiplyGauges(int factor) {
        blades *= factor;
        blood  *= factor;
        vault  *= factor;
    }

    /**
     * Retire {@code percent} % de chaque jauge (Lames, Sang, Coffre), arrondi au-dessus (Dé pipé de l'ennemi).
     *
     * @return le total retiré des trois jauges
     */
    public int drainGauges(int percent) {
        int lostBlades = (int) Math.ceil(blades * percent / 100.0);
        int lostBlood  = (int) Math.ceil(blood * percent / 100.0);
        int lostVault  = (int) Math.ceil(vault * percent / 100.0);
        blades -= lostBlades;
        blood  -= lostBlood;
        vault  -= lostVault;
        return lostBlades + lostBlood + lostVault;
    }

    /** Le Rouleau interdit de l'ennemi bloque le rouleau {@code reel} au prochain tirage. */
    public void forbidReel(int reel) { forbiddenReel = reel; }

    /** @return le rouleau bloqué au prochain tirage, ou -1 si aucun. */
    public int getForbiddenReel() { return forbiddenReel; }

    /** Tirage : le rouleau bloqué l'est pour ce tirage, puis se libère. @return ce rouleau, ou -1 */
    public int takeForbiddenReel() {
        int reel = forbiddenReel;
        forbiddenReel = -1;
        return reel;
    }

    /** Coffre-fort : {@code amount} gains reviennent à la fin du {@code turns}-ième tour, celui-ci compris. */
    public void addSafe(int amount, int turns) { safes.add(new int[] {amount, turns}); }

    /** @return les Coffres-forts fermés : pour chacun, ses gains et ses tours restants. */
    public List<int[]> getSafes() {
        List<int[]> copy = new ArrayList<>();
        safes.forEach(safe -> copy.add(safe.clone()));
        return copy;
    }

    /**
     * Fin du tour, avant {@link #endTurn()} : ouvre les Coffres-forts arrivés à
     * leur dernier tour, ou tous si {@code all} (ennemi vaincu).
     *
     * @return les gains rendus
     */
    public int openSafes(boolean all) {
        int total = 0;
        for (Iterator<int[]> it = safes.iterator(); it.hasNext(); ) {
            int[] safe = it.next();
            if (all || safe[1] <= 1) {
                total += safe[0];
                it.remove();
            }
        }
        return total;
    }

    /** Veine d'or : les gains sont multipliés pendant les {@code turns} prochains tirages (prolonge si elle l'est déjà). */
    public void addGoldVein(int turns) { goldVeinTurns = Math.max(goldVeinTurns, turns); }
    /** @return les tirages restants sous la Veine d'or (0 si inactive). */
    public int getGoldVeinTurns() { return goldVeinTurns; }

    /** Bulle d'air : la règle du lieu ne joue pas pendant les {@code turns} prochains tours, celui-ci compris. */
    public void addBubble(int turns) { bubbleTurns = Math.max(bubbleTurns, turns); }
    /** @return les tours restants sous la Bulle d'air (0 si inactive). */
    public int getBubbleTurns() { return bubbleTurns; }

    /** Grignotage : une carte de plus sera rongée à la prochaine pioche. */
    public void addNibble() { nibbles++; }
    /** @return les cartes qui seront rongées à la prochaine pioche. */
    public int getNibbles() { return nibbles; }
    /** Pioche : les cartes à ronger le sont. @return leur nombre */
    public int takeNibbles() { int n = nibbles; nibbles = 0; return n; }

    /** Aveuglement : la prochaine main sera face cachée. */
    public void blind() { blind = true; }
    /** @return {@code true} si la prochaine main sera face cachée. */
    public boolean isBlind() { return blind; }
    /** Pioche : l'Aveuglement vaut pour cette main. @return {@code true} s'il était actif */
    public boolean takeBlind() { boolean was = blind; blind = false; return was; }

    /** Chant : une carte de plus sera jouée d'office au prochain tour. */
    public void addSong() { songs++; }
    /** @return les cartes qui seront jouées d'office au prochain tour. */
    public int getSongs() { return songs; }
    /** Pioche : les cartes à jouer d'office le sont. @return leur nombre */
    public int takeSongs() { int n = songs; songs = 0; return n; }

    /** Ivresse : un rouleau de plus tournera deux fois au prochain tirage. */
    public void addDrunk() { drunk++; }
    /** @return les rouleaux qui tourneront deux fois au prochain tirage. */
    public int getDrunk() { return drunk; }
    /** Tirage : l'Ivresse vaut pour celui-ci. @return le nombre de rouleaux qui tournent deux fois */
    public int takeDrunk() { int n = drunk; drunk = 0; return n; }

    /** Pépite : au prochain tirage, les symboles de gain ne rapportent rien. */
    public void addNugget() { nugget = true; }
    /** @return {@code true} si les symboles de gain ne rapporteront rien au prochain tirage. */
    public boolean hasNugget() { return nugget; }
    /** Tirage : la Pépite vaut pour celui-ci. @return {@code true} si elle était active */
    public boolean takeNugget() { boolean was = nugget; nugget = false; return was; }

    /** Fausse monnaie : {@code amount} gains de plus deviennent faux. */
    public void addFakeGains(int amount) { fakeGains += Math.max(0, amount); }

    /** @return les gains faux, qui disparaîtront au prochain tirage. */
    public int getFakeGains() { return fakeGains; }

    /** Des gains sont perdus ou dépensés : la Fausse monnaie part la première. */
    public void spendFakeGains(int amount) { fakeGains = Math.max(0, fakeGains - Math.max(0, amount)); }

    /** Tirage : la Fausse monnaie qui reste disparaît. @return les gains faux à retirer */
    public int takeFakeGains() {
        int fake = fakeGains;
        fakeGains = 0;
        return fake;
    }

    /** Taxe : une Taxe de plus sur chaque carte du prochain tour. */
    public void addTax() { taxes++; }

    /** @return les Taxes en attente pour le prochain tour. */
    public int getTaxes() { return taxes; }

    /** Début du tour : les Taxes valent pour ce tour. @return leur nombre */
    public int takeTaxes() {
        int taken = taxes;
        taxes = 0;
        return taken;
    }

    /** Nouvelle règle : {@code rule} vaut pour les {@code turns} prochains tirages. */
    public void setHouseRule(HouseRule rule, int turns) {
        houseRule      = rule;
        houseRuleTurns = turns;
    }

    /** @return la règle changée par le Directeur des Jeux, ou {@code null}. */
    public HouseRule getHouseRule() { return houseRuleTurns > 0 ? houseRule : null; }

    /** @return les tirages restants sous la règle changée. */
    public int getHouseRuleTurns() { return houseRuleTurns; }

    /** Tirage : la règle changée vaut pour ce tirage. @return la règle, ou {@code null} */
    public HouseRule useHouseRule() {
        if (houseRuleTurns <= 0) return null;
        houseRuleTurns--;
        return houseRule;
    }

    /** Piège à rats : le prochain mauvais sort de l'ennemi sur la main est annulé. */
    public void addTrap() { traps++; }
    /** @return les Pièges à rats posés. */
    public int getTraps() { return traps; }
    /** Un mauvais sort tombe : un Piège à rats l'annule s'il y en a un. @return {@code true} si annulé */
    public boolean useTrap() {
        if (traps <= 0) return false;
        traps--;
        return true;
    }

    /** Lanterne : la main ne peut plus être cachée pendant les {@code draws} prochaines pioches. */
    public void addLantern(int draws) { lanternDraws = Math.max(lanternDraws, draws); }
    /** @return les pioches restantes sous la Lanterne. */
    public int getLanternDraws() { return lanternDraws; }
    /** Pioche : la Lanterne éclaire celle-ci. @return {@code true} si elle était allumée */
    public boolean useLantern() {
        if (lanternDraws <= 0) return false;
        lanternDraws--;
        return true;
    }

    /** Bouchons d'oreille : le Chant n'a plus d'effet pendant les {@code draws} prochaines pioches. */
    public void addEarplugs(int draws) { earplugDraws = Math.max(earplugDraws, draws); }
    /** @return les pioches restantes avec les Bouchons d'oreille. */
    public int getEarplugDraws() { return earplugDraws; }
    /** Pioche : les Bouchons valent pour celle-ci. @return {@code true} s'ils étaient mis */
    public boolean useEarplugs() {
        if (earplugDraws <= 0) return false;
        earplugDraws--;
        return true;
    }

    /** Étai : le Forage ne perce plus le bouclier pendant {@code turns} tours, celui-ci compris. */
    public void addProp(int turns) { propTurns = Math.max(propTurns, turns); }
    /** @return les tours restants sous l'Étai. */
    public int getPropTurns() { return propTurns; }

    /** Lampe à carbure : le prochain coup de grisou est évité. */
    public void addLamp() { lamps++; }
    /** @return les Lampes à carbure allumées. */
    public int getLamps() { return lamps; }
    /** Coup de grisou : une Lampe l'évite s'il y en a une. @return {@code true} si évité */
    public boolean useLamp() {
        if (lamps <= 0) return false;
        lamps--;
        return true;
    }

    /** Cage à requin : l'ennemi ne peut rien prendre aux gains pendant {@code turns} tours, celui-ci compris. */
    public void addCage(int turns) { cageTurns = Math.max(cageTurns, turns); }
    /** @return les tours restants dans la Cage à requin. */
    public int getCageTurns() { return cageTurns; }

    /** Ancre : la marée haute ne retire plus d'attaque pendant {@code turns} tours, celui-ci compris. */
    public void addAnchor(int turns) { anchorTurns = Math.max(anchorTurns, turns); }
    /** @return les tours restants sous l'Ancre. */
    public int getAnchorTurns() { return anchorTurns; }

    /** Trempe : l'attaque gagne {@code percent} % tout de suite, puis autant à chaque tour, jusqu'à la fin du combat. */
    public void addTemper(int percent) {
        temperCards += percent;
        temperPercent += percent;
    }
    /** @return le bonus d'attaque de la Trempe, en % (0 si aucune). */
    public int getTemperPercent() { return temperPercent; }

    /** Cocktail des abysses : {@code plays} cartes jouables en plus, ce tour seulement. */
    public void addBonusPlays(int plays) { bonusPlays += plays; }
    /** @return les cartes jouables en plus ce tour (Cocktail des abysses). */
    public int getBonusPlays() { return bonusPlays; }

    /** Fin d'un tirage : chaque symbole retiré se rapproche de son retour, la Corruption et Dans la manche de leur fin. */
    public void endTurn() {
        removedSymbols.replaceAll((symbol, turns) -> turns - 1);
        removedSymbols.values().removeIf(turns -> turns <= 0);
        if (corruptionTurns > 0) corruptionTurns--;
        if (extraPlaysTurns > 0 && --extraPlaysTurns == 0) extraPlays = 0;
        safes.forEach(safe -> safe[1]--);
        if (goldVeinTurns > 0) goldVeinTurns--;
        if (bubbleTurns > 0) bubbleTurns--;
        if (propTurns > 0) propTurns--;
        if (cageTurns > 0) cageTurns--;
        if (anchorTurns > 0) anchorTurns--;
        temperPercent += temperCards; // la Trempe durcit l'épée à chaque tour
        bonusPlays = 0;
    }

    /** @return {@code true} si aucun effet n'est actif et les jauges sont vides. */
    public boolean isEmpty() {
        return removedSymbols.isEmpty() && gainBonus == 0f && corruptionTurns == 0 && extraPlaysTurns == 0
            && blades == 0 && blood == 0 && vault == 0 && safes.isEmpty() && goldVeinTurns == 0 && bubbleTurns == 0
            && nibbles == 0 && !blind && songs == 0 && drunk == 0 && !nugget
            && fakeGains == 0 && taxes == 0 && houseRuleTurns == 0
            && traps == 0 && lanternDraws == 0 && earplugDraws == 0 && propTurns == 0 && lamps == 0
            && cageTurns == 0 && anchorTurns == 0 && temperPercent == 0 && bonusPlays == 0;
    }
}
