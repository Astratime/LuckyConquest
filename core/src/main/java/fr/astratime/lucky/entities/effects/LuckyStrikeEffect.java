package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.SlotMachine;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Coup de chance (échoppe) : ce tour, le Joker sort {@code factor} fois plus souvent. */
public class LuckyStrikeEffect extends BoostSymbolEffect {

    private final int factor;

    /** @param factor le Joker sort {@code factor} fois plus souvent (son poids de base est multiplié) */
    public LuckyStrikeEffect(int factor) {
        super(Symbol.JOKER, SlotMachine.JOKER_WEIGHT * (factor - 1));
        this.factor = factor;
    }

    @Override
    public String getDescription() { return Lang.f("Ce tour, le Joker sort {0} fois plus souvent", factor); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(Lang.t("COUP DE CHANCE !"), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY),
            new EffectPopup(Lang.f("JOKER x{0}", factor), EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.GHOST_REEL; }
}
