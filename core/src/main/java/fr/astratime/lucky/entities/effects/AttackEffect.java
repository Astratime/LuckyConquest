package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Ajoute un bonus d'attaque via le CombatContext. */
public class AttackEffect extends Effect {

    private final int bonus;
    /** Attaque tirée des gains (Fortune du Roi) : convertie en Puissance (Astra, 2026-11-18). */
    private final boolean asPower;

    /** @param bonus bonus d'attaque plat accordé pour ce tour. */
    public AttackEffect(int bonus) { this(bonus, false); }

    /**
     * @param bonus   bonus d'attaque accordé pour ce tour
     * @param asPower {@code true} : il devient de la Puissance (+1 par coup de référence, voir
     *                {@link fr.astratime.lucky.entities.context.CombatContext#addFlatAsPower})
     */
    public AttackEffect(int bonus, boolean asPower) {
        this.bonus   = bonus;
        this.asPower = asPower;
    }

    @Override
    public void apply(TurnContext context) {
        if (asPower) context.getCombatContext().addFlatAsPower(bonus, "Fortune");
        else context.getCombatContext().addAttackBonus(bonus, "Cartes");
    }

    @Override
    public String getDescription() { return Lang.f("Attaque +{0}", bonus); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            EffectPopup.scaled(Lang.f("ATTAQUE +{0}", bonus), EffectPopup.Style.ATTACK, bonus, PopupScale.CARD_ATTACK_BONUS)
        );
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }
}
