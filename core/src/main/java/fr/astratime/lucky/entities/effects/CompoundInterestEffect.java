package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Intérêts composés (archétype Rentier) : les Créances de l'ennemi grossissent à chaque tirage.
 */
public class CompoundInterestEffect extends Effect {
    private final int turns;
    private final int percent;

    /**
     * Intérêts composés.
     * @param turns tirages où les Créances grossissent
     * @param percent croissance des Créances, en %
     */
    public CompoundInterestEffect(int turns, int percent) {
        this.turns = turns;
        this.percent = percent;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().getEnemy().compound(turns, percent);
    }

    @Override
    public String getDescription() { return Lang.f("Pendant tes {0} prochains tours, les Créances de l'ennemi grossissent de {1} % (au moins +1).", turns, percent); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("INTÉRÊTS COMPOSÉS"), EffectPopup.Style.DRAIN, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.GAINS_MULTIPLIER; }

}
