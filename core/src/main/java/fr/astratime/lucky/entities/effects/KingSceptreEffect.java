package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Sceptre du roi (archétype Cour royale) : si une combinaison est formée ce
 * tour, une part de son multiplicateur reste pour l'attaque du tirage suivant
 * (jamais pour les gains).
 */
public class KingSceptreEffect extends Effect {
    private final int percent;

    /** @param percent part du multiplicateur des combinaisons gardée, en % */
    public KingSceptreEffect(int percent) { this.percent = percent; }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().addSceptre(percent); }

    @Override
    public String getDescription() {
        return Lang.f("Si une combinaison est formée ce tour, {0} % de son multiplicateur restent pour l'attaque de ton prochain tirage (pas pour les gains).",
            percent);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("SCEPTRE DU ROI"), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.MULTIPLIER; }
}
