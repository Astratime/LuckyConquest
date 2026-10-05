package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.entities.Symbol;
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

    /** @return le score d'un symbole au Dernier tirage : son rang dans la liste des symboles (le Joker en tête). */
    public static int score(Symbol symbol) {
        return symbol == Symbol.JOKER ? Symbol.values().length : symbol.ordinal() + 1;
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
            popups.add(new EffectPopup("ÉGALITÉ : " + mine.get(round).getDisplayName() + " ! ON RELANCE",
                EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY));
        }
        popups.add(new EffectPopup("DERNIER TIRAGE : TOI " + own().getDisplayName() + " (" + score(own()) + "), ELLE "
            + theirs().getDisplayName() + " (" + score(theirs()) + ")", EffectPopup.Style.SPECIAL,
            PopupScale.SECONDARY_INTENSITY));
        popups.add(playerWins
            ? new EffectPopup("TU AS TIRÉ LE LEVIER !", EffectPopup.Style.GAINS, PopupScale.SECONDARY_INTENSITY)
            : new EffectPopup("LA MACHINE GAGNE", EffectPopup.Style.DAMAGE, PopupScale.SECONDARY_INTENSITY));
        return popups;
    }
}
