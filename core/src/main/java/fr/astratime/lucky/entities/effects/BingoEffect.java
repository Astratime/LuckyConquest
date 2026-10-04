package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Bingo : les trois rouleaux affichent le même symbole, dont la valeur
 * (dégâts, bouclier ou gains) est multipliée. Plus aucune carte ne peut être
 * jouée : la machine se lance d'elle-même. Le symbole peut être imposé
 * (cartes de test de l'échoppe) ou tiré au hasard. Un Bingo de bouclier renvoie
 * tout le bouclier à l'ennemi s'il attaque (voir EnemyTurnResolver). La carte
 * « Bingo » (symbole au hasard) qui donne un Bingo de gains glisse une carte
 * Bingo d'un symbole au hasard dans le deck (voir
 * GameController#claimBonusCard).
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
        String bingo = symbol != null ? "Bingo " + symbol.getDisplayName() : "Bingo";
        return "Lance la machine avec un " + bingo + " garanti"
            + (power > 1 ? ". Attaque, bouclier et gains des symboles x" + power : "")
            + ". Bingo de bouclier : s'il attaque, ton bouclier lui est renvoyé"
            + (symbol == null ? ". Bingo de gains : un Bingo au hasard rejoint ton deck" : "");
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup("BINGO GARANTI !", EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY),
            new EffectPopup("SYMBOLES x" + power, EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    /** Un Bingo doublé ferait exploser sa puissance : il ne compte qu'une fois. */
    @Override
    public boolean canBeDoubled() { return false; }

    @Override
    public EffectSound getSound() { return EffectSound.BINGO; }
}
