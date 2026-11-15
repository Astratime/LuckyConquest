package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Encre (carte du Kraken) : à son prochain tour, l'ennemi joue ses cartes au hasard.
 */
public class InkEffect extends Effect {
    private final int turns;

    /** @param turns ses tours où il joue à l'aveugle */
    public InkEffect(int turns) { this.turns = turns; }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().getEnemy().getMaluses().ink(turns); }

    @Override
    public String getDescription() { return (turns <= 1 ? Lang.t("À son prochain tour, l'ennemi joue à l'aveugle : ses cartes au hasard.")
            : Lang.f("Pendant ses {0} prochains tours, l'ennemi joue à l'aveugle : ses cartes au hasard.", turns)); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("ENCRE"), EffectPopup.Style.DEFENSE, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public boolean canBeDoubled() { return false; }

    @Override
    public EffectSound getSound() { return EffectSound.INSURANCE; }
}
