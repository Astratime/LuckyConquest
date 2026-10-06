package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolRegistry;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.events.ShieldGainedEvent;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Rempart (donjon du Carreau) : un bouclier au tirage, même sans symbole de
 * défense, et des symboles de défense plus fréquents ce tour.
 */
public class RampartEffect extends Effect {

    private final int shield;
    private final int defenseBoost;

    /**
     * @param shield       bouclier accordé au tirage
     * @param defenseBoost boost de poids appliqué à chaque symbole de défense pour ce tour
     */
    public RampartEffect(int shield, int defenseBoost) {
        this.shield       = shield;
        this.defenseBoost = defenseBoost;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().getPlayer().addShield(shield);
        context.addEvent(new ShieldGainedEvent(shield));
        for (Symbol symbol : SymbolRegistry.getDefenseSymbols()) {
            context.getSpinContext().addWeightBoost(symbol, defenseBoost);
        }
    }

    @Override
    public String getDescription() {
        return Lang.f("Bouclier +{0}. Les symboles de défense sortent plus souvent", shield);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            EffectPopup.scaled(Lang.f("BOUCLIER +{0}", shield), EffectPopup.Style.DEFENSE, shield, PopupScale.CARD_DEFENSE_BONUS),
            new EffectPopup(Lang.t("CHANCE DE DÉFENSE"), EffectPopup.Style.DEFENSE, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.DEFENSE; }
}
