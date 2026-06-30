package fr.astratime.lucky.entities.context;

/**
 * Agrège les deux contextes du tour : spin et combat.
 * Construit par PreparationResolver en début de résolution,
 * à partir des effets de cartes joués par le joueur.
 * drawCount : nombre de cartes à piocher au prochain tour (modifiable par effets).
 */
public class TurnContext {

    private static final int DEFAULT_DRAW_COUNT = 3;

    private int drawCount = DEFAULT_DRAW_COUNT;

    private final SpinContext   spinContext;
    private final CombatContext combatContext;

    public TurnContext(SpinContext spinContext, CombatContext combatContext) {
        this.spinContext   = spinContext;
        this.combatContext = combatContext;
    }

    public SpinContext   getSpinContext()   { return spinContext; }
    public CombatContext getCombatContext() { return combatContext; }
    public int           getDrawCount()    { return drawCount; }
    public void          addDrawCount(int extra) { drawCount += extra; }
}
