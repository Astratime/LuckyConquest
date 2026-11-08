package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Combo;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Quinte royale (archétype Cour royale) : si Suite et Couleur sont formées au
 * même tour, l'attaque est multipliée.
 */
public class RoyalFlushEffect extends Effect {
    private final float factor;

    /** @param factor multiplicateur d'attaque quand Suite et Couleur sont formées */
    public RoyalFlushEffect(float factor) { this.factor = factor; }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().addRoyalFlush(factor); }

    @Override
    public String getDescription() {
        return Lang.f("Si une Suite et une Couleur sont formées ce tour : attaque x{0}.", Lang.decimal(Combo.formatFactor(factor)));
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("QUINTE ROYALE"), EffectPopup.Style.ATTACK, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.MULTIPLIER; }
}
