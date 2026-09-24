package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Combo;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.events.ComboEvent;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Carte combo : au lancer de la machine, si les cartes jouées pendant le tour
 * forment la combinaison, les gains et l'attaque du tirage sont multipliés.
 * L'ordre dans lequel les cartes sont jouées ne compte pas.
 */
public class ComboEffect extends Effect {

    private final Combo combo;
    private final float factor;

    /**
     * @param combo  combinaison à former avec les cartes jouées ce tour
     * @param factor multiplicateur des gains et de l'attaque si elle est formée
     */
    public ComboEffect(Combo combo, float factor) {
        this.combo  = combo;
        this.factor = factor;
    }

    @Override
    public void apply(TurnContext context) {
        CombatContext combat  = context.getCombatContext();
        boolean       success = combo.matches(combat.getPlayer().getPlayedCards());
        if (success) {
            combat.multiplyGains(factor);
            combat.multiplyAttack(factor);
        }
        context.addEvent(new ComboEvent(combo.getDisplayName(), success, factor));
    }

    @Override
    public String getDescription() {
        String rule = switch (combo) {
            case SUITE   -> "3 rangs qui se suivent";
            case COULEUR -> "3 cartes ou plus, toutes de la meme suite";
            case BRELAN  -> "3 cartes du meme rang";
            case FULL    -> "un brelan et une paire";
        };
        return combo.getDisplayName() + " : si les cartes jouees ce tour forment " + rule
            + ", gains et attaque x" + formatFactor();
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(combo.getDisplayName() + " ?", EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY),
            new EffectPopup("GAINS ET ATTAQUE x" + formatFactor(), EffectPopup.Style.GAINS, PopupScale.SECONDARY_INTENSITY));
    }

    private String formatFactor() {
        return factor == (int) factor ? String.valueOf((int) factor) : String.valueOf(factor);
    }
}
