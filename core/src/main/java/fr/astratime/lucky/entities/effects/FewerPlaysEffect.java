package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Tournée du patron, Momification : l'ennemi joue moins de cartes à ses prochains tours ; la Tournée fait aussi jouer plus de cartes au joueur.
 */
public class FewerPlaysEffect extends Effect {
    private final int cards;
    private final int turns;
    private final int bonusPlays;
    private final String label;

    /** @param cards cartes de moins pour l'ennemi ; @param turns ses tours touchés ; @param bonusPlays cartes de plus pour le joueur ce tour ; @param label nom de la carte, pour le texte */
    public FewerPlaysEffect(int cards, int turns, int bonusPlays, String label) { this.cards = cards; this.turns = turns; this.bonusPlays = bonusPlays; this.label = label; }

    @Override
    public void onPlay(PlayContext context) {
        if (bonusPlays > 0) context.getLastingEffects().addBonusPlays(bonusPlays);
        context.queueForSpin(this);
        context.addPopups(getPopups());
    }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().getEnemy().getMaluses().playFewer(cards, turns); }

    @Override
    public String getDescription() { return (bonusPlays > 0 ? Lang.n(bonusPlays, "Joue {0} carte de plus ce tour. ", "Joue {0} cartes de plus ce tour. ", bonusPlays) : "")
            + (turns <= 1 ? Lang.n(cards, "L'ennemi joue {0} carte de moins à son prochain tour.", "L'ennemi joue {0} cartes de moins à son prochain tour.", cards)
            : Lang.n(cards, "L'ennemi joue {0} carte de moins pendant ses {1} prochains tours.",
                "L'ennemi joue {0} cartes de moins pendant ses {1} prochains tours.", cards, turns)); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t(label).toUpperCase(), EffectPopup.Style.DEFENSE, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.EXTRA_PLAYS; }
}
