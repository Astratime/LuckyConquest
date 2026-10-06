package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.events.CardBonusEvent;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Guillotine (donjon du Pique, la carte rare) : encaisse toutes les Lames.
 * Après le tirage, chaque Lame inflige une part des PV restants de l'ennemi,
 * d'un coup, sans tenir compte de sa défense (voir CombatResolver).
 */
public class GuillotineEffect extends Effect {

    private final int percentPerBlade;

    /** @param percentPerBlade part des PV restants de l'ennemi infligée par Lame, en % */
    public GuillotineEffect(int percentPerBlade) { this.percentPerBlade = percentPerBlade; }

    @Override
    public void apply(TurnContext context) {
        CombatContext combat = context.getCombatContext();
        int blades = combat.getPlayer().getLastingEffects().consumeBlades();
        if (blades <= 0) return;
        combat.addExecution(blades * percentPerBlade);
        context.addEvent(new CardBonusEvent(Lang.f("Guillotine : {0} lames", blades), List.of(
            new EffectPopup(Lang.f("GUILLOTINE : {0} LAMES", blades), EffectPopup.Style.ATTACK, PopupScale.MAX_INTENSITY))));
    }

    @Override
    public String getDescription() {
        return Lang.f("Encaisse toutes les Lames. Chaque Lame inflige {0}% des PV restants de l'ennemi. Ignore la "
            + "défense",
            percentPerBlade);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("GUILLOTINE"), EffectPopup.Style.ATTACK, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ACE_OF_SPADES; }
}
