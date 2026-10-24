package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Cartes marquées (archétype Bluffeur) : les cartes à couleur de l'ennemi valent moins pendant
 * ses prochains tours (moins de baisse sur un boss ou un roi).
 */
public class MarkedCardsEffect extends Effect {
    private final int turns;

    /**
     * Cartes marquées.
     * @param turns tours de l'ennemi sous les Cartes marquées
     */
    public MarkedCardsEffect(int turns) {
        this.turns = turns;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().getEnemy().markCards(turns);
    }

    @Override
    public String getDescription() { return Lang.f("Pendant ses {0} prochains tours, les cartes à couleur de l'ennemi ne valent que la moitié (les trois quarts pour un boss ou un roi).", turns); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("CARTES MARQUÉES"), EffectPopup.Style.DEFENSE, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.RIGGED_REEL; }

    @Override
    public boolean canBeDoubled() { return false; }

}
