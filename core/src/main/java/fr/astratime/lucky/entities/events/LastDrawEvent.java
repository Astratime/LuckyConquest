package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.ArrayList;
import java.util.List;

/**
 * Le Dernier tirage de la Machine Originelle : chaque manche, le joueur et elle
 * tirent leur levier ; les égalités font relancer, la dernière manche décide.
 * L'écran de jeu en fait une cinématique (le duel des leviers).
 */
public class LastDrawEvent extends Event {
    /** Les rouleaux du joueur, d'où vient son symbole. */
    public final List<Symbol> playerReels;
    /** Ses symboles, une manche par case ; le dernier décide. */
    public final List<Symbol> mine;
    /** Ceux de la Machine (un rouleau classique), manche par manche. */
    public final List<Symbol> hers;
    /** Le joueur a gagné : la Machine tombe. Sinon, c'est lui. */
    public final boolean playerWins;

    public LastDrawEvent(List<Symbol> playerReels, List<Symbol> mine, List<Symbol> hers, boolean playerWins) {
        this.playerReels = List.copyOf(playerReels);
        this.mine        = List.copyOf(mine);
        this.hers        = List.copyOf(hers);
        this.playerWins  = playerWins;
    }

    /**
     * L'échelle des rangs au Dernier tirage, du plus faible au plus fort : les
     * fruits, les porte-bonheur, les bars et les armes, puis les raretés et l'or,
     * la couronne, le 7, le 777, et le Joker tout en haut. Un triple bat toujours
     * son simple (Triple cerise sur Cerise, Triple sept sur Sept).
     */
    private static final List<Symbol> LADDER = List.of(
        Symbol.CHERRY, Symbol.GRAPE, Symbol.WATERMELON, Symbol.HORSESHOE, Symbol.BELL, Symbol.HEART, Symbol.DIE,
        Symbol.BAR, Symbol.ECU, Symbol.SWORD, Symbol.DOUBLE_BAR, Symbol.BOMB, Symbol.TRIPLE_CHERRY, Symbol.STAR,
        Symbol.DIAMOND, Symbol.NUGGET, Symbol.GOLD_BAR, Symbol.CROWN, Symbol.SEVEN, Symbol.TRIPLE_SEVEN, Symbol.JOKER);

    /** @return le rang d'un symbole au Dernier tirage (1 : Cerise, ..., 21 : Joker), voir {@link #LADDER}. */
    public static int score(Symbol symbol) {
        return LADDER.indexOf(symbol) + 1;
    }

    /** @return le symbole décisif du joueur (la dernière manche). */
    public Symbol own() { return mine.get(mine.size() - 1); }

    /** @return le symbole décisif de la Machine (la dernière manche). */
    public Symbol theirs() { return hers.get(hers.size() - 1); }

    @Override
    public String describe() {
        return "Dernier tirage : toi " + own().getDisplayName() + " (" + score(own()) + "), elle "
            + theirs().getDisplayName() + " (" + score(theirs()) + ") : " + (playerWins ? "gagné" : "perdu");
    }

    @Override
    public List<EffectPopup> getPopups() {
        List<EffectPopup> popups = new ArrayList<>();
        for (int round = 0; round < mine.size() - 1; round++) {
            popups.add(new EffectPopup(Lang.f("ÉGALITÉ : {0} ! ON RELANCE", mine.get(round).getDisplayName()),
                EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY));
        }
        popups.add(new EffectPopup(Lang.f("DERNIER TIRAGE : TOI {0} ({1}), ELLE {2} ({3})",
            own().getDisplayName(), score(own()), theirs().getDisplayName(), score(theirs())), EffectPopup.Style.SPECIAL,
            PopupScale.SECONDARY_INTENSITY));
        popups.add(playerWins
            ? new EffectPopup(Lang.t("TU AS TIRÉ LE LEVIER !"), EffectPopup.Style.GAINS, PopupScale.SECONDARY_INTENSITY)
            : new EffectPopup(Lang.t("LA MACHINE GAGNE"), EffectPopup.Style.DAMAGE, PopupScale.SECONDARY_INTENSITY));
        return popups;
    }
}
