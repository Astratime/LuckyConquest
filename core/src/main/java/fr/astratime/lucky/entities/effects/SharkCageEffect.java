package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Cage à requin (les Coffres) : l'ennemi ne peut rien prendre aux gains
 * pendant quelques tours (Morsure, Intérêts, Fausse monnaie, Faillite).
 */
public class SharkCageEffect extends Effect {

    private final int turns;

    /** @param turns tours de protection, celui-ci compris */
    public SharkCageEffect(int turns) { this.turns = turns; }

    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().addCage(turns);
        context.addPopups(getPopups());
    }

    /** Rien de plus au tirage. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return "Tes gains sont protégés pendant " + turns + " tours. Rien ne peut les prendre."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("CAGE À REQUIN", EffectPopup.Style.DEFENSE, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.SAFE; }
}
