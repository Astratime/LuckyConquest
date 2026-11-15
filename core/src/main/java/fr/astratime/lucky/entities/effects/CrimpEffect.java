package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Carte biseautée (carte du Roi forain) : les attaques de l'ennemi à son prochain tour baissent.
 */
public class CrimpEffect extends Effect {
    private final int percent;

    /** @param percent baisse de ses attaques, en % */
    public CrimpEffect(int percent) { this.percent = percent; }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().getEnemy().getMaluses().shiftNextAttack(-Math.min(100, percent)); }

    @Override
    public String getDescription() { return Lang.f("À son prochain tour, les attaques de l'ennemi baissent de {0} %.", Math.min(100, percent)); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("CARTE BISEAUTÉE : ATTAQUES -{0} %", Math.min(100, percent)), EffectPopup.Style.DEFENSE, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.INSURANCE; }
}
