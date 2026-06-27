package fr.astratime.lucky.entities;

/**
 * Données propres au tour en cours.
 * Réinitialisées à chaque appel à GameState.nextTurn().
 */
public class TurnContext {

    private static final int   DEFAULT_DRAW_COUNT = 3;
    private static final float DEFAULT_MULTIPLIER = 1f;

    private int   drawCount    = DEFAULT_DRAW_COUNT;
    private int   attackBonus  = 0;
    private int   defenseBonus = 0;
    private float multiplier   = DEFAULT_MULTIPLIER;

    public void reset() {
        drawCount    = DEFAULT_DRAW_COUNT;
        attackBonus  = 0;
        defenseBonus = 0;
        multiplier   = DEFAULT_MULTIPLIER;
    }

    public int   getDrawCount()    { return drawCount; }
    public int   getAttackBonus()  { return attackBonus; }
    public int   getDefenseBonus() { return defenseBonus; }
    public float getMultiplier()   { return multiplier; }

    public void addDrawCount(int extra)        { drawCount    += extra; }
    public void addAttackBonus(int bonus)      { attackBonus  += bonus; }
    public void addDefenseBonus(int bonus)     { defenseBonus += bonus; }
    public void setMultiplier(float multiplier) { this.multiplier = multiplier; }
}
