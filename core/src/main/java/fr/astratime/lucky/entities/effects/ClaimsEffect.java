package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Reconnaissance de dette (archétype Rentier) : des Créances sur l'ennemi (voir
 * {@link fr.astratime.lucky.entities.Enemy#addClaims}).
 */
public class ClaimsEffect extends Effect {
    private final int count;

    /**
     * Reconnaissance de dette.
     * @param count Créances posées
     */
    public ClaimsEffect(int count) {
        this.count = count;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().getEnemy().getMaluses().addClaims(count);
    }

    @Override
    public String getDescription() { return Lang.f("+{0} Créances sur l'ennemi (20 au plus). À chacun de ses tours, chaque Créance lui inflige {1} % du prix du tirage en dégâts, sans tenir compte de sa défense, et te rapporte un tirage de gains.", count, fr.astratime.lucky.entities.EnemyMaluses.CLAIM_DAMAGE_PERCENT); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("CRÉANCES +{0}", count), EffectPopup.Style.DRAIN, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.GAIN; }

}
