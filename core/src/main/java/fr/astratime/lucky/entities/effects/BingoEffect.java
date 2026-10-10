package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
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

    private final int     power;
    private final Symbol  symbol;
    /** Carte de test « Jeu bonus » (mode ADMIN) : le Jeu bonus s'ouvre à coup sûr après ce Bingo. */
    private final boolean bonusGame;

    /** @param power multiplicateur de la valeur de chaque symbole. */
    public BingoEffect(int power) { this(power, null); }

    /**
     * @param power  multiplicateur de la valeur de chaque symbole
     * @param symbol symbole aligné sur les trois rouleaux ; {@code null} : tiré au hasard
     */
    public BingoEffect(int power, Symbol symbol) {
        this(power, symbol, false);
    }

    /** @param bonusGame {@code true} : le Jeu bonus s'ouvre à coup sûr après ce Bingo */
    public BingoEffect(int power, Symbol symbol, boolean bonusGame) {
        this.power     = power;
        this.symbol    = symbol;
        this.bonusGame = bonusGame;
    }

    /** @return le symbole imposé au jackpot, ou {@code null} s'il est tiré au hasard. */
    public Symbol getSymbol() { return symbol; }

    /** @return {@code true} pour la carte de test « Jeu bonus » : le Jeu bonus s'ouvre à coup sûr. */
    public boolean isBonusGame() { return bonusGame; }

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
        if (bonusGame) context.getSpinContext().forceBonusGame();
        context.getCombatContext().multiplySymbolPower(power, "Bingo");
    }

    @Override
    public String getDescription() {
        if (bonusGame) return Lang.t("Test : lance la machine avec un Bingo garanti, et le Jeu bonus s'ouvre à coup sûr");
        String bingo = symbol != null ? Lang.f("Bingo {0}", symbol.getDisplayName()) : Lang.t("Bingo");
        return Lang.f("Lance la machine avec un {0} garanti{1}. Bingo de bouclier : s'il attaque, ton bouclier lui est "
            + "renvoyé{2}",
            bingo, (power > 1 ? Lang.f(". Attaque, bouclier et gains des symboles x{0}", power) : ""), (symbol == null ? Lang.t(". Bingo de gains : un Bingo au hasard rejoint ton deck") : ""));
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(Lang.t("BINGO GARANTI !"), EffectPopup.Style.GAINS, PopupScale.MAX_INTENSITY),
            new EffectPopup(Lang.f("SYMBOLES x{0}", power), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    /** Un Bingo doublé ferait exploser sa puissance : il ne compte qu'une fois. */
    @Override
    public boolean canBeDoubled() { return false; }

    @Override
    public EffectSound getSound() { return EffectSound.BINGO; }
}
