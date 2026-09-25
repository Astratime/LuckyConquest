package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Corruption : pendant quelques tirages (celui de ce tour compris), chacun
 * consomme {@link #GAINS_PERCENT} % des gains, et l'attaque et la défense des
 * symboles sont multipliées par {@link #FACTOR} (voir PreparationResolver).
 */
public class CorruptionEffect extends Effect {

    /** Part des gains consommée à chaque tirage sous Corruption. */
    public static final int GAINS_PERCENT = 10;
    /** Multiplicateur de l'attaque et de la défense des symboles. */
    public static final int FACTOR        = 10;

    private final int turns;

    /** @param turns nombre de tirages sous Corruption. */
    public CorruptionEffect(int turns) { this.turns = turns; }

    /** La Corruption s'installe tout de suite (elle apparaît dans le panneau latéral). */
    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().addCorruption(turns);
        context.addPopups(getPopups());
    }

    /** Aucun effet propre au spin : la Corruption active est appliquée par PreparationResolver. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() {
        return "Pendant " + turns + " tours : attaque et defense des symboles x" + FACTOR
            + ", mais chaque tour consomme " + GAINS_PERCENT + "% des gains";
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup("CORRUPTION !", EffectPopup.Style.DAMAGE, PopupScale.MAX_INTENSITY),
            new EffectPopup("ATTAQUE ET DÉFENSE x" + FACTOR + " (" + turns + " TOURS)", EffectPopup.Style.SPECIAL,
                PopupScale.SECONDARY_INTENSITY));
    }
}
