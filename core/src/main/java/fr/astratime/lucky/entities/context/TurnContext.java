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

    /**
     * @param spinContext   modificateurs de probabilité pour le spin de ce tour
     * @param combatContext modificateurs de combat pour ce tour
     */
    public TurnContext(SpinContext spinContext, CombatContext combatContext) {
        this.spinContext   = spinContext;
        this.combatContext = combatContext;
    }

    /** @return le contexte de spin de ce tour. */
    public SpinContext   getSpinContext()   { return spinContext; }
    /** @return le contexte de combat de ce tour. */
    public CombatContext getCombatContext() { return combatContext; }
    /** @return le nombre de cartes à piocher au prochain tour. */
    public int           getDrawCount()    { return drawCount; }
    /** Ajoute {@code extra} cartes au nombre de cartes à piocher au prochain tour. */
    public void          addDrawCount(int extra) { drawCount += extra; }
}
