package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Dame de cour (archétype Cour royale) : plus d'attaque pour chaque combinaison
 * formée ce tour (un Carré compte Carré, Brelan et Paire, donc 3).
 */
public class CourtLadyEffect extends Effect {
    private final int percent;

    /** @param percent attaque en plus par combinaison formée, en % */
    public CourtLadyEffect(int percent) { this.percent = percent; }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().addCourtLady(percent); }

    @Override
    public String getDescription() {
        return Lang.f("Attaque +{0} % par combinaison formée ce tour (un Carré compte aussi Brelan et Paire).", percent);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("DAME DE COUR"), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }
}
