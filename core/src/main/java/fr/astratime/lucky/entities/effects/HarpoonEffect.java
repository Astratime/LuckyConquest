package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Harpon (la Salle VIP) : l'attaque du tirage augmente ; le Kraken perd un bras,
 * les autres ennemis jouent une carte de moins à leur prochain tour.
 */
public class HarpoonEffect extends Effect {

    private final int attackPercent;

    /** @param attackPercent attaque ajoutée au tirage, en % */
    public HarpoonEffect(int attackPercent) { this.attackPercent = attackPercent; }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().multiplyAttack(1f + attackPercent / 100f, "Harpon");
        context.getCombatContext().getEnemy().harpoon();
    }

    @Override
    public String getDescription() { return Lang.f("Attaque +{0} %. Coupe un bras du Kraken. Les autres ennemis jouent une carte de moins à leur "
        + "prochain tour.",
        attackPercent); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("HARPON : ATTAQUE +{0} %", attackPercent), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }
}
