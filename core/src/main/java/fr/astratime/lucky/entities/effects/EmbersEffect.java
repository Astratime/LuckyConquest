package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Braise (carte du Dragon) : l'ennemi brûlera à ses prochains tours, d'une part du plus gros coup du tirage.
 */
public class EmbersEffect extends Effect {
    private final int turns;
    private final int percent;

    /** @param turns ses tours de brûlure ; @param percent part du plus gros coup à chaque brûlure, en % */
    public EmbersEffect(int turns, int percent) { this.turns = turns; this.percent = percent; }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().addEmbers(turns, percent); }

    @Override
    public String getDescription() { return Lang.f("L'ennemi brûle : au début de ses {0} prochains tours, il perd {1} % de ton plus gros coup de ce tour.", turns, percent); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("BRAISE"), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }
}
