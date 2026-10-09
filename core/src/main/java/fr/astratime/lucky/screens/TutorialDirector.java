package fr.astratime.lucky.screens;

import com.badlogic.gdx.math.Rectangle;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.SpinEconomy;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.tutorial.TutorialRun;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.views.GuideOverlay;
import fr.astratime.lucky.views.GuideOverlay.Step;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Le Croupier mène le tutoriel ({@link TutorialRun}) : à chaque moment clé du
 * combat ({@link Beat}), l'écran de jeu lui passe la main ; il dit ses
 * répliques (voir {@link GuideOverlay}), attend que le joueur fasse ce qu'il
 * demande, puis rend la main au combat. Tant qu'il attend une action, seules
 * les cartes qu'il nomme se jouent, et la machine ne se lance que s'il le
 * demande. Après le troisième tour, il se tait : le joueur achève le combat seul.
 */
final class TutorialDirector {

    /** Les moments du combat où le Croupier peut parler. */
    enum Beat {
        /** La main du tour est piochée. */
        HAND_DEALT,
        /** Les textes du tirage du joueur sont affichés ; le tour de l'ennemi attend. */
        SPIN_RESOLVED,
        /** Le tour de l'ennemi est fini ; le tour suivant attend. */
        ENEMY_TURN_DONE,
        /** L'ennemi est vaincu ; les boutons de fin attendent. */
        VICTORY
    }

    /** Ce que le Croupier montre et regarde sur l'écran de jeu. */
    interface Board {
        Rectangle enemyBar();
        Rectangle playerBar();
        Rectangle enemy();
        Rectangle hand(Predicate<Card> which);
        Rectangle spinButton();
        Rectangle stakeButton();
        Rectangle reels();
        Rectangle playerShield();
        Rectangle enemyDefense();
        Rectangle gains();
        Rectangle combos();
        Rectangle effects();
        Rectangle shopIcon();
        boolean shopShown();
        boolean enemyDefeated();
        boolean lastSpinWasBingo();
        void rig(Symbol[] symbols);
        void rigJackpot(Symbol symbol);
        /** Les cartes que le Croupier ne laisse pas jouer s'assombrissent (ou s'éclairent de nouveau). */
        void refreshHand();
    }

    private static final String REFUSED = "Le Croupier attend une autre carte";

    private final GuideOverlay overlay;
    private final Board        board;

    /** Tour du joueur en cours (1 au premier), compté à chaque main piochée. */
    private int turn;
    /** Cartes jouées depuis le début du combat (par id), machines lancées et achats. */
    private final List<String> played = new ArrayList<>();
    private int spins;
    private int purchases;
    /** Ce que la réplique en cours laisse faire : les cartes jouables ({@code null} : toutes) et la machine. */
    private Set<String> allowed;
    private boolean     spinAllowed = true;
    /** Le palier de Mise choisi en dernier. */
    private SpinEconomy.Stake stake = SpinEconomy.Stake.NONE;
    /** La Mise se choisit : le Croupier ne parle pas, ou il la demande. */
    private boolean     stakeAllowed = true;

    TutorialDirector(GuideOverlay overlay, Board board) {
        this.overlay = overlay;
        this.board   = board;
    }

    /** Le combat recommence (Recommencer) : le Croupier reprend tout depuis le début. */
    void reset() {
        overlay.stop();
        turn = 0;
        played.clear();
        spins = 0;
        purchases = 0;
        stake = SpinEconomy.Stake.NONE;
        restrict(null, true);
    }

    /** Le combat arrive au moment {@code beat} : le Croupier dit ce qu'il a à dire, puis {@code then}. */
    void beat(Beat beat, Runnable then) {
        if (beat == Beat.HAND_DEALT) turn++;
        List<Step> steps = script(beat);
        if (steps.isEmpty()) {
            restrict(null, true);
            then.run();
            return;
        }
        overlay.play(steps, () -> {
            restrict(null, true);
            then.run();
        });
    }

    void onCardPlayed(Card card) { played.add(card.getId()); }

    void onSpin() { spins++; }

    void onPurchase() { purchases++; }

    void onStake(SpinEconomy.Stake chosen) { stake = chosen; }

    /** @return pourquoi {@code card} ne se joue pas maintenant (le Croupier en attend une autre), ou {@code null}. */
    String refusal(Card card) {
        if (!overlay.isActive() || allowed == null) return null;
        return allowed.contains(card.getId()) ? null : REFUSED;
    }

    /** @return {@code true} si la machine peut être lancée (le Croupier ne parle pas, ou il le demande). */
    boolean allowsSpin() { return !overlay.isActive() || spinAllowed; }

    /** @return {@code true} si la Mise peut changer (le Croupier ne parle pas, ou il la demande). */
    boolean allowsStake() { return !overlay.isActive() || stakeAllowed; }

    // -------------------------------------------------------------------------
    // Le script
    // -------------------------------------------------------------------------

    private List<Step> script(Beat beat) {
        List<Step> steps = new ArrayList<>();
        switch (beat) {
            case HAND_DEALT -> handDealt(steps);
            case SPIN_RESOLVED -> spinResolved(steps);
            case ENEMY_TURN_DONE -> {
                if (turn == 1) steps.add(say(Lang.t("Moi aussi, je joue des cartes et je tire mon levier. Épée : je frappe. Bouclier : je me "
                    + "protège. Potion : je me soigne."), board::enemy));
            }
            case VICTORY -> {
                steps.add(say(Lang.t("Bien joué. Tu connais la table, maintenant.")));
                steps.add(say(Lang.t("Retourne au menu. Je t'y montre le reste.")));
            }
        }
        return steps;
    }

    private void handDealt(List<Step> steps) {
        switch (turn) {
            case 1 -> {
                steps.add(say(Lang.t("Bienvenue à ma table. Je suis le Croupier. Je vais t'apprendre à jouer.")));
                steps.add(say(Lang.t("Là-haut, mes points de vie. Fais-les tomber à zéro et tu gagnes."), board::enemyBar));
                steps.add(say(Lang.t("En bas, les tiens. À zéro, tu perds."), board::playerBar));
                steps.add(say(Lang.t("Ta main. Chaque tour, tu pioches 6 cartes. Tu en joues 4 au plus."),
                    () -> board.hand(card -> true)));
                steps.add(play(Lang.t("Joue la carte Gains +500. Clique dessus."), Set.of(TutorialRun.GAINS_CARD), 1));
                steps.add(say(Lang.t("Tes gains montent. Tu en avais 500 au départ. Chaque tirage en coûte 100."), board::gains));
                steps.add(say(Lang.t("Une Paire sur les rouleaux en rapporte 300. Un Bingo, bien plus. Tes gains servent aussi "
                    + "à l'échoppe."), board::gains));
                steps.add(play(Lang.t("Survole une carte pour lire son effet. Clic droit : sa fiche. Joue le 7 de Trèfle."),
                    Set.of(TutorialRun.CLUB_SEVEN), 1));
                steps.add(spin(Lang.t("Tes cartes sont posées. Lance la machine. Elle te prend 100 gains."), TutorialRun.rigged(1)));
            }
            case 2 -> {
                steps.add(play(Lang.t("Deux cartes de même valeur font une Paire. Joue tes deux 7."),
                    Set.copyOf(TutorialRun.PAIR), TutorialRun.PAIR.size()));
                steps.add(say(Lang.t("Les combinaisons multiplient tout ton tirage. Elles s'additionnent : une Paire et une Suite, "
                    + "c'est x9,5."), board::combos));
                steps.add(play(Lang.t("Joue le Porte-bonheur."), Set.of(TutorialRun.LUCKY_CHARM), 1));
                steps.add(say(Lang.t("Ses effets durent. Ils s'affichent ici. Survole-les pour les relire."), board::effects));
                steps.add(stake(Lang.t("Avant de lancer, mise une part de tes gains. Clique sur Mise : 10 %. Sur un Bingo, "
                    + "tes symboles x2. Encore : 25 %, x3. Puis 50 %, x5.")));
                steps.add(spin(Lang.t("Ton jeton est sur la table. Lance la machine."), TutorialRun.rigged(2)));
            }
            case 3 -> {
                steps.add(play(Lang.t("Mon bouclier est épais. Les Piques le percent en partie. Joue le Valet de Pique."),
                    Set.of(TutorialRun.SPADE), 1));
                steps.add(Step.action(Lang.t("Tes gains se dépensent à l'échoppe. Ouvre-la."), board::shopIcon, board::shopShown)
                    .onStart(() -> restrict(Set.of(), false)));
                int bought = purchases;
                steps.add(Step.passive(Lang.f("Achète la carte Bingo. Elle coûte {0} gains.", TutorialRun.BINGO_PRICE),
                        () -> purchases > bought && !board.shopShown())
                    .onStart(() -> restrict(Set.of(), false)));
                steps.add(play(Lang.t("La carte Bingo aligne trois symboles pareils. Joue-la."), Set.of(TutorialRun.BINGO), 1)
                    .onStart(() -> {
                        restrict(Set.of(TutorialRun.BINGO), false);
                        board.rigJackpot(TutorialRun.BINGO_SYMBOL);
                    }));
            }
            default -> { }
        }
    }

    private void spinResolved(List<Step> steps) {
        switch (turn) {
            case 1 -> {
                steps.add(say(Lang.t("Chaque symbole agit. Le Sept frappe. La Cloche rapporte des gains. Le Raisin te protège."),
                    board::reels));
                steps.add(say(Lang.t("Ton bouclier. Il absorbe mes coups."), board::playerShield));
                steps.add(say(Lang.t("Mon bouclier. Il encaisse tes coups et s'use. Il revient à chacun de mes tours."),
                    board::enemyDefense));
                steps.add(say(Lang.t("À moi de jouer. Regarde bien.")));
            }
            case 2 -> {
                steps.add(say(Lang.t("Ta Paire a multiplié tout le tirage : attaque, gains et bouclier. Pas de Bingo : "
                    + "ta Mise est perdue."),
                    board::reels));
                steps.add(say(Lang.t("Attention : sous 0 gains, tu es endetté. Tes symboles faiblissent. "
                    + "Plus bas, l'Huissier arrive."), board::gains));
            }
            case 3 -> {
                if (board.lastSpinWasBingo()) {
                    steps.add(say(Lang.t("BINGO ! Trois symboles pareils. Leur effet est multiplié."), board::reels));
                }
                if (!board.enemyDefeated()) steps.add(say(Lang.t("Je tiens encore. Achève-moi. Je te laisse jouer seul.")));
            }
            default -> { }
        }
    }

    /** Une réplique simple (un clic passe à la suite), qui montre {@code target}. */
    private Step say(String text, Supplier<Rectangle> target) {
        return Step.say(text, target).onStart(() -> restrict(null, false));
    }

    private Step say(String text) {
        return Step.say(text).onStart(() -> restrict(null, false));
    }

    /** Le joueur doit jouer {@code count} cartes parmi {@code ids} ; elles seules s'éclairent. */
    private Step play(String text, Set<String> ids, int count) {
        int[] before = new int[1];
        return Step.action(text, () -> board.hand(card -> ids.contains(card.getId())),
                () -> played.stream().skip(before[0]).filter(ids::contains).count() >= count)
            .onStart(() -> {
                before[0] = played.size();
                restrict(ids, false);
            });
    }

    /** Le joueur doit miser (au moins 10 %) ; seul le bouton de Mise répond. */
    private Step stake(String text) {
        return Step.action(text, board::stakeButton, () -> stake != SpinEconomy.Stake.NONE)
            .onStart(() -> {
                restrict(Set.of(), false);
                stakeAllowed = true;
            });
    }

    /** Le joueur doit lancer la machine ; le tirage affichera {@code rigged}. */
    private Step spin(String text, Symbol[] rigged) {
        int[] before = new int[1];
        return Step.action(text, board::spinButton, () -> spins > before[0])
            .onStart(() -> {
                before[0] = spins;
                restrict(Set.of(), true);
                if (rigged != null) board.rig(rigged);
            });
    }

    /** Seules les cartes {@code ids} se jouent ({@code null} : toutes) ; la machine se lance si {@code spin}. */
    private void restrict(Set<String> ids, boolean spin) {
        allowed = ids;
        spinAllowed = spin;
        stakeAllowed = false;
        board.refreshHand();
    }
}
