package fr.astratime.lucky.screens;

import com.badlogic.gdx.math.Rectangle;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.tutorial.TutorialRun;
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

    /** @return pourquoi {@code card} ne se joue pas maintenant (le Croupier en attend une autre), ou {@code null}. */
    String refusal(Card card) {
        if (!overlay.isActive() || allowed == null) return null;
        return allowed.contains(card.getId()) ? null : REFUSED;
    }

    /** @return {@code true} si la machine peut être lancée (le Croupier ne parle pas, ou il le demande). */
    boolean allowsSpin() { return !overlay.isActive() || spinAllowed; }

    // -------------------------------------------------------------------------
    // Le script
    // -------------------------------------------------------------------------

    private List<Step> script(Beat beat) {
        List<Step> steps = new ArrayList<>();
        switch (beat) {
            case HAND_DEALT -> handDealt(steps);
            case SPIN_RESOLVED -> spinResolved(steps);
            case ENEMY_TURN_DONE -> {
                if (turn == 1) steps.add(say("Moi aussi, je joue des cartes et je tire mon levier. Épée : je frappe. "
                    + "Bouclier : je me protège. Potion : je me soigne.", board::enemy));
            }
            case VICTORY -> {
                steps.add(say("Bien joué. Tu connais la table, maintenant."));
                steps.add(say("Retourne au menu. Je t'y montre le reste."));
            }
        }
        return steps;
    }

    private void handDealt(List<Step> steps) {
        switch (turn) {
            case 1 -> {
                steps.add(say("Bienvenue à ma table. Je suis le Croupier. Je vais t'apprendre à jouer."));
                steps.add(say("Là-haut, mes points de vie. Fais-les tomber à zéro et tu gagnes.", board::enemyBar));
                steps.add(say("En bas, les tiens. À zéro, tu perds.", board::playerBar));
                steps.add(say("Ta main. Chaque tour, tu pioches 6 cartes. Tu en joues 4 au plus.",
                    () -> board.hand(card -> true)));
                steps.add(play("Joue la carte Gains +500. Clique dessus.", Set.of(TutorialRun.GAINS_CARD), 1));
                steps.add(say("Tes gains montent. Ils servent à acheter des cartes à l'échoppe.", board::gains));
                steps.add(play("Survole une carte pour lire son effet. Clic droit : sa fiche. Joue le 7 de Trèfle.",
                    Set.of(TutorialRun.CLUB_SEVEN), 1));
                steps.add(spin("Tes cartes sont posées. Lance la machine.", TutorialRun.rigged(1)));
            }
            case 2 -> {
                steps.add(play("Deux cartes de même valeur font une Paire. Joue tes deux 7.",
                    Set.copyOf(TutorialRun.PAIR), TutorialRun.PAIR.size()));
                steps.add(say("Les combinaisons multiplient tout ton tirage. Elles s'additionnent : "
                    + "une Paire et une Suite, c'est x9,5.", board::combos));
                steps.add(play("Joue le Porte-bonheur.", Set.of(TutorialRun.LUCKY_CHARM), 1));
                steps.add(say("Ses effets durent. Ils s'affichent ici. Survole-les pour les relire.", board::effects));
                steps.add(spin("Lance la machine.", TutorialRun.rigged(2)));
            }
            case 3 -> {
                steps.add(play("Mon bouclier est épais. Les Piques le transpercent. Joue le Valet de Pique.",
                    Set.of(TutorialRun.SPADE), 1));
                steps.add(Step.action("Tes gains se dépensent à l'échoppe. Ouvre-la.", board::shopIcon, board::shopShown)
                    .onStart(() -> restrict(Set.of(), false)));
                int bought = purchases;
                steps.add(Step.passive("Achète la carte Bingo. Elle coûte " + TutorialRun.BINGO_PRICE + " gains.",
                        () -> purchases > bought && !board.shopShown())
                    .onStart(() -> restrict(Set.of(), false)));
                steps.add(play("La carte Bingo aligne trois symboles pareils. Joue-la.", Set.of(TutorialRun.BINGO), 1)
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
                steps.add(say("Chaque symbole agit. Le Sept frappe. La Cloche rapporte des gains. Le Raisin te protège.",
                    board::reels));
                steps.add(say("Ton bouclier. Il absorbe mes coups.", board::playerShield));
                steps.add(say("Mon bouclier. Il encaisse tes coups et s'use. Il revient à chacun de mes tours.",
                    board::enemyDefense));
                steps.add(say("À moi de jouer. Regarde bien."));
            }
            case 2 -> steps.add(say("Ta Paire a multiplié tout le tirage : attaque, gains et bouclier.", board::reels));
            case 3 -> {
                if (board.lastSpinWasBingo()) {
                    steps.add(say("BINGO ! Trois symboles pareils. Leur effet est multiplié.", board::reels));
                }
                if (!board.enemyDefeated()) steps.add(say("Je tiens encore. Achève-moi. Je te laisse jouer seul."));
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
        board.refreshHand();
    }
}
