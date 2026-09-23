package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;

import java.util.List;

/** Ajoute un bonus d'attaque via le CombatContext. */
public class AttackEffect extends Effect {

    private final int bonus;

    /** @param bonus bonus d'attaque plat accordé pour ce tour. */
    public AttackEffect(int bonus) { this.bonus = bonus; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addAttackBonus(bonus);
    }

    @Override
    public String getDescription() { return "Attaque +" + bonus; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            EffectPopup.scaled("ATTAQUE +" + bonus, EffectPopup.Style.ATTACK, bonus, 30f)
        );
    }
}
