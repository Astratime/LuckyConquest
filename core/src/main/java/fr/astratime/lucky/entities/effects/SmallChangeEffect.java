package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Menue monnaie (archétype du 2 au 8, Astra 2026-11-23) : plus d'attaque pour chaque petite carte (rang 2 à 8,
 * à suite ou d'archétype, elle-même comprise) jouée ce tour. Les pourcentages de plusieurs cartes s'ajoutent, et
 * l'attaque est multipliée une seule fois (voir {@code PreparationResolver.smallChange}).
 */
public class SmallChangeEffect extends Effect {
    private final int percent;

    /** @param percent attaque en plus par petite carte jouée ce tour, en % */
    public SmallChangeEffect(int percent) { this.percent = percent; }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().addSmallChange(percent); }

    @Override
    public String getDescription() {
        return Lang.f("Attaque +{0} % par carte de 2 à 8 jouée ce tour (celle-ci comprise).", percent);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("MENUE MONNAIE"), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }
}
