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
    public String getDescription() { return Lang.f("+{0} Créances sur l'ennemi. À chacun de ses tours, chaque Créance lui retire 0,5 % de ses PV max et te rapporte un tirage de gains (20 au plus).", count); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("CRÉANCES +{0}", count), EffectPopup.Style.DRAIN, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.GAIN; }

}
