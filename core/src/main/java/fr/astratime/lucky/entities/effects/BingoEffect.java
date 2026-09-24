package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Bingo : les trois rouleaux affichent le même symbole, dont la valeur
 * (dégâts, bouclier ou gains) est multipliée. Plus aucune carte ne peut être
 * jouée : la machine se lance d'elle-même.
 */
public class BingoEffect extends Effect {

    private final int power;

    /** @param power multiplicateur de la valeur de chaque symbole. */
    public BingoEffect(int power) { this.power = power; }

    @Override
    public void onPlay(PlayContext context) {
        super.onPlay(context);
        context.requestAutoSpin();
    }

    @Override
    public void apply(TurnContext context) {
        context.getSpinContext().forceJackpot();
        context.getCombatContext().multiplySymbolPower(power);
    }

    @Override
    public String getDescription() {
        return "Bingo garanti : attaque, bouclier et gains des symboles x" + power + ". Lance la machine";
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup("BINGO GARANTI !", EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY),
            new EffectPopup("SYMBOLES x" + power, EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }
}
