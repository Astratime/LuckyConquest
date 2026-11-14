package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Dents de rat (carte du Capitaine Rat) : des cartes de la prochaine main de l'ennemi partent à sa défausse.
 */
public class GnawEffect extends Effect {
    private final int cards;

    /** @param cards cartes rongées */
    public GnawEffect(int cards) { this.cards = cards; }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().getEnemy().getMaluses().gnaw(cards); }

    @Override
    public String getDescription() { return Lang.n(cards, "L'ennemi perd {0} carte de sa prochaine main, au hasard.", "L'ennemi perd {0} cartes de sa prochaine main, au hasard.", cards); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("DENTS DE RAT"), EffectPopup.Style.DEFENSE, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.INSURANCE; }
}
