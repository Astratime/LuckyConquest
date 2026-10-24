package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Farce du Lutin (archétype Lutin) : si toute la défense de l'ennemi tombe ce tour, des gains et des Lames.
 */
public class LeprechaunPrankEffect extends Effect {
    private final int spins;
    private final int blades;

    /**
     * Farce du Lutin.
     * @param spins gains, en coûts de tirage
     * @param blades Lames en plus
     */
    public LeprechaunPrankEffect(int spins, int blades) {
        this.spins = spins;
        this.blades = blades;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addPrank(spins, blades);
    }

    @Override
    public String getDescription() { return Lang.f("Si toute la défense de l'ennemi tombe ce tour : gains +{0} tirages et +{1} Lames.", spins, blades); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("FARCE DU LUTIN ?"), EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.BRIBE; }

}
