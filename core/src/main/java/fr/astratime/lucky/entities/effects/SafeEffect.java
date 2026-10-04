package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.ArrayList;
import java.util.List;

/**
 * Coffre-fort : une part des gains est mise de côté tout de suite. Elle
 * revient doublée à la fin du dernier tour (ou dès que l'ennemi tombe).
 */
public class SafeEffect extends Effect {

    private final int percent;
    private final int turns;

    /**
     * @param percent part des gains mise de côté, en %
     * @param turns   tours avant le retour des gains, celui-ci compris
     */
    public SafeEffect(int percent, int turns) {
        this.percent = percent;
        this.turns   = turns;
    }

    @Override
    public void onPlay(PlayContext context) {
        int stored = context.consumeGainsPercent(percent / 100f);
        context.getLastingEffects().addSafe(stored * 2, turns);
        List<EffectPopup> popups = new ArrayList<>(getPopups());
        if (stored > 0) {
            popups.add(EffectPopup.scaled("GAINS -" + stored, EffectPopup.Style.DAMAGE, stored, PopupScale.SPIN_GAINS));
        }
        context.addPopups(popups);
    }

    /** Tout se passe à la pose et en fin de tour (voir LastingEffects#openSafes). */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() {
        return "Mets " + percent + " % de tes gains de côté. Ils reviennent x2 dans " + turns + " tours.";
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("COFFRE-FORT : " + percent + " % DES GAINS", EffectPopup.Style.GAINS,
            PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.SAFE; }
}
