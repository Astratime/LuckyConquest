package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Bingo : les trois rouleaux affichent le même symbole, dont la valeur
 * (dégâts, bouclier ou gains) est multipliée. Plus aucune carte ne peut être
 * jouée : la machine se lance d'elle-même. Le symbole peut être imposé
 * (cartes de test de l'échoppe) ou tiré au hasard.
 */
public class BingoEffect extends Effect {

    private final int    power;
    private final Symbol symbol;

    /** @param power multiplicateur de la valeur de chaque symbole. */
    public BingoEffect(int power) { this(power, null); }

    /**
     * @param power  multiplicateur de la valeur de chaque symbole
     * @param symbol symbole aligné sur les trois rouleaux ; {@code null} : tiré au hasard
     */
    public BingoEffect(int power, Symbol symbol) {
        this.power  = power;
        this.symbol = symbol;
    }

    /** @return le symbole imposé au jackpot, ou {@code null} s'il est tiré au hasard. */
    public Symbol getSymbol() { return symbol; }

    @Override
    public void onPlay(PlayContext context) {
        super.onPlay(context);
        context.requestAutoSpin();
    }

    @Override
    public void apply(TurnContext context) {
        if (symbol != null) {
            context.getSpinContext().forceJackpot(symbol);
        } else {
            context.getSpinContext().forceJackpot();
        }
        context.getCombatContext().multiplySymbolPower(power);
    }

    @Override
    public String getDescription() {
        String bingo = symbol != null ? "Bingo " + symbol.getDisplayName() + " garanti" : "Bingo garanti";
        return bingo + " : attaque, bouclier et gains des symboles x" + power + ". Lance la machine";
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup("BINGO GARANTI !", EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY),
            new EffectPopup("SYMBOLES x" + power, EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }
}
