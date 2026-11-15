package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.events.StatusEvent;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

import java.util.Random;

/**
 * Roulement de tambour (carte de Monsieur Loyal) : le numéro réussit deux fois sur trois (attaque multipliée), sinon il rate (attaque réduite).
 */
public class DrumrollEffect extends Effect {

    /** Chance que le numéro réussisse, en %. */
    public static final int SUCCESS_PERCENT = 67;
    private static final Random RANDOM = new Random();

    private final int   factor;
    private final float missFactor;

    /** @param factor attaque du tirage si le numéro réussit (x) ; @param missFactor attaque du tirage s'il rate (x) */
    public DrumrollEffect(int factor, float missFactor) { this.factor = factor; this.missFactor = missFactor; }

    @Override
    public void apply(TurnContext context) {
        boolean success = RANDOM.nextInt(100) < SUCCESS_PERCENT;
        context.getCombatContext().multiplyAttack(success ? factor : missFactor, "Roulement de tambour");
        context.addEvent(new StatusEvent(success ? Lang.f("NUMÉRO RÉUSSI : ATTAQUE x{0}", factor)
            : Lang.f("NUMÉRO RATÉ : ATTAQUE x{0}", Lang.decimal(String.valueOf(missFactor))),
            success ? EffectPopup.Style.ATTACK : EffectPopup.Style.DAMAGE));
    }

    @Override
    public String getDescription() {
        return Lang.f("Deux chances sur trois : attaque du tour x{0}. Sinon le numéro rate : attaque x{1}.", factor,
            Lang.decimal(String.valueOf(missFactor)));
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("ROULEMENT DE TAMBOUR"), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.RUSSIAN_ROULETTE; }
}
