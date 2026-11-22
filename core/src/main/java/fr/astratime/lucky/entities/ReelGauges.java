package fr.astratime.lucky.entities;

import java.util.Random;

/**
 * Ce que retiennent, le temps d'un combat, les rouleaux qui ont une mémoire
 * (choix d'Astra, 2026-11-01) : la Tirelire qui grossit, l'Ancre qui garde son
 * bouclier, et les jauges du Roi démon (Démonisation) et de l'Apôtre de la
 * Chance (Ange chanceux) qui, pleines, imposent un Bingo de Descente aux enfers
 * ou de Lumière du bingo et rendent le joueur invincible.
 */
public final class ReelGauges {

    /** Démonisation ou Ange chanceux à ce niveau : l'effet s'active. */
    public static final int GAUGE_MAX = 6;
    /** Tirelire : gains de base de sa première sortie, puis en plus à chaque sortie. */
    public static final int PIGGY_STEP = 10;
    /** Invincible pendant ce tour (celui où la jauge se remplit) et le suivant. */
    static final int INVINCIBLE_TURNS = 2;

    /** Vapeur : chaque tirage d'affilée où elle sort ajoute ce bonus à son coup (en %). */
    public static final int STEAM_STEP_PERCENT = 25;
    /** Vapeur : au plus ce nombre de crans (x2 au 4e tirage d'affilée). */
    public static final int STEAM_MAX = 4;

    /** Gong : chances d'étourdir un rouleau de l'ennemi, en %. */
    public static final int GONG_STUN_PERCENT = 25;

    private int piggyDraws;
    private int steam;
    private int frost;
    /** Maillet de gong : le Gong étourdit à coup sûr à ce tirage. */
    private boolean gongSure;
    private int anchoredThisTurn;
    private int anchorKept;
    private int demonization;
    private int luckyAngel;
    /** Le Bingo imposé au prochain tirage (Descente aux enfers ou Lumière du bingo), ou {@code null}. */
    private Symbol forced;
    private int invincibleTurns;

    // ----- Tirelire -----

    /** La Tirelire sort une fois de plus. @return ses gains de base pour cette sortie (10, 20, 30…). */
    public int nextPiggy() { return PIGGY_STEP * ++piggyDraws; }

    /** @return les gains de base de la prochaine sortie de la Tirelire. */
    public int getPiggyNext() { return PIGGY_STEP * (piggyDraws + 1); }

    /** @return le nombre de sorties de la Tirelire depuis le début du combat. */
    public int getPiggyDraws() { return piggyDraws; }

    // ----- Vapeur -----

    /**
     * Après un tirage : la pression monte d'un cran si la Vapeur est sortie, sinon elle retombe à zéro.
     * @return la pression après ce tirage
     */
    public int steam(boolean drawn) {
        steam = drawn ? Math.min(STEAM_MAX, steam + 1) : 0;
        return steam;
    }

    /** Soupape : la Vapeur passe tout de suite à sa pression la plus haute. */
    public void fillSteam() { steam = STEAM_MAX; }

    /** @return la pression de la Vapeur : les tirages d'affilée où elle est sortie (0 à {@link #STEAM_MAX}). */
    public int getSteam() { return steam; }

    /** @return le multiplicateur du coup de Vapeur à ce tirage (x1, x1,25 … x2). */
    public double steamFactor() { return 1.0 + STEAM_STEP_PERCENT * Math.min(STEAM_MAX, steam) / 100.0; }

    // ----- Flocon -----

    /** Flocon : part de son bouclier qui gèle et revient au début du tour suivant, en %. */
    public static final int FROST_PERCENT = 50;

    /** Le Flocon donne {@code shield} de bouclier : {@link #FROST_PERCENT} % gèlent pour le tour suivant. */
    public void freeze(int shield) { frost += Math.max(0, shield) * FROST_PERCENT / 100; }

    /** Début du tour : le bouclier gelé par le Flocon revient (il ne fond pas). @return ce bouclier (0 sans Flocon) */
    public int takeFrost() {
        int kept = frost;
        frost = 0;
        return kept;
    }

    /** @return le bouclier gelé qui reviendra au début du prochain tour. */
    public int getFrost() { return frost; }

    // ----- Gong -----

    /** Maillet de gong : à ce tirage, le Gong étourdit à coup sûr un rouleau de l'ennemi. */
    public void sureGong() { gongSure = true; }

    /** @return {@code true} si le Gong étourdit à coup sûr à ce tirage. */
    public boolean isGongSure() { return gongSure; }

    /** Après le tirage : le Maillet de gong ne valait que pour celui-ci. */
    public void endGong() { gongSure = false; }

    // ----- Ancre -----

    /** L'Ancre donne {@code shield} de bouclier ce tour : ce qu'il en restera sera gardé un tour de plus. */
    public void anchor(int shield) { anchoredThisTurn += Math.max(0, shield); }

    /**
     * Fin du tour : sur le bouclier restant {@code leftover}, la part venue de
     * l'Ancre est mise de côté pour le tour suivant.
     *
     * @return le bouclier gardé (à retirer de ce qui part au Coffre)
     */
    public int keepAnchored(int leftover) {
        anchorKept = Math.min(Math.max(0, leftover), anchoredThisTurn);
        anchoredThisTurn = 0;
        return anchorKept;
    }

    /** Début du tour : le bouclier gardé par l'Ancre revient. @return ce bouclier (0 sans Ancre) */
    public int takeAnchorKept() {
        int kept = anchorKept;
        anchorKept = 0;
        return kept;
    }

    /** @return le bouclier que l'Ancre rendra au début du prochain tour. */
    public int getAnchorKept() { return anchorKept; }

    // ----- Roi démon et Apôtre de la Chance -----

    /**
     * Ajoute {@code demons} à la Démonisation et {@code angels} à l'Ange chanceux
     * (sans dépasser {@link #GAUGE_MAX}), puis active une jauge pleine si aucun
     * Bingo imposé n'attend déjà (l'autre attendra son tour).
     *
     * @return le symbole du Bingo imposé qui vient de s'activer, ou {@code null}
     */
    public Symbol fill(int demons, int angels, Random random) {
        demonization = Math.min(GAUGE_MAX, demonization + demons);
        luckyAngel   = Math.min(GAUGE_MAX, luckyAngel + angels);
        if (forced != null) return null;
        boolean demon = demonization >= GAUGE_MAX, angel = luckyAngel >= GAUGE_MAX;
        if (demon && angel) {
            if (random.nextBoolean()) angel = false;
            else demon = false;
        }
        if (demon) {
            demonization = 0;
            forced = Symbol.DESCENT;
        } else if (angel) {
            luckyAngel = 0;
            forced = Symbol.BINGO_LIGHT;
        } else {
            return null;
        }
        invincibleTurns = INVINCIBLE_TURNS;
        return forced;
    }

    /** @return le Bingo imposé au prochain tirage, ou {@code null}. */
    public Symbol getForced() { return forced; }

    /** Le tirage prend le Bingo imposé. @return son symbole, ou {@code null} s'il n'y en a pas */
    public Symbol takeForced() {
        Symbol symbol = forced;
        forced = null;
        return symbol;
    }

    /** @return la Démonisation (0 à {@link #GAUGE_MAX}). */
    public int getDemonization() { return demonization; }

    /** @return l'Ange chanceux (0 à {@link #GAUGE_MAX}). */
    public int getLuckyAngel() { return luckyAngel; }

    /** @return {@code true} tant que le joueur est invincible. */
    public boolean isInvincible() { return invincibleTurns > 0; }

    /** @return les tours d'invincibilité restants, celui-ci compris. */
    public int getInvincibleTurns() { return invincibleTurns; }

    /** Fin du tour : l'invincibilité se rapproche de sa fin. */
    public void endTurn() {
        if (invincibleTurns > 0) invincibleTurns--;
    }

    /** @return {@code true} si aucun de ces rouleaux n'a rien retenu. */
    public boolean isEmpty() {
        return piggyDraws == 0 && steam == 0 && frost == 0 && !gongSure && anchoredThisTurn == 0 && anchorKept == 0 && demonization == 0 && luckyAngel == 0
            && forced == null && invincibleTurns == 0;
    }
}
