package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Combo;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Coucou (archétype Horloger) : quelques tirages plus tard, le coucou chante et
 * l'attaque de ce tirage-là est multipliée.
 */
public class CuckooEffect extends Effect {
    private final int turns;
    private final float factor;

    /**
     * Coucou.
     * @param turns  tirages avant le chant (2 : le tirage d'après le suivant)
     * @param factor multiplicateur d'attaque du tirage où il chante
     */
    public CuckooEffect(int turns, float factor) {
        this.turns  = turns;
        this.factor = factor;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().getPlayer().getLastingEffects().getCourtAndClock().addCuckoo(turns, factor);
    }

    @Override
    public String getDescription() {
        return Lang.f("Dans {0} tours, le coucou chante : attaque x{1} à ce tirage-là.", turns, Lang.decimal(Combo.formatFactor(factor)));
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("COUCOU DANS {0} TOURS", turns), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.MULTIPLIER; }
}
