package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Combo;
import fr.astratime.lucky.entities.LastingEffects;
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

    /** Jauges remplies par carte jouée quand une Couleur ou une Suite réussit. */
    static final int COMBO_BLADES = 1;
    static final int COMBO_BLOOD  = 25;
    static final int COMBO_VAULT  = 40;

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
            if (combo == Combo.COULEUR || combo == Combo.SUITE) fillGauges(combat);
        }
        context.addEvent(new ComboEvent(combo.getDisplayName(), success, factor));
    }

    /** Couleur ou Suite réussie : chaque carte à suite jouée remplit la jauge de sa couleur. */
    private static void fillGauges(CombatContext combat) {
        LastingEffects lasting = combat.getPlayer().getLastingEffects();
        for (Card card : combat.getPlayer().getPlayedCards()) {
            if (card.getSuit() == null) continue;
            switch (card.getSuit()) {
                case PIQUE   -> lasting.addBlades(COMBO_BLADES);
                case COEUR   -> lasting.addBlood(COMBO_BLOOD);
                case CARREAU -> lasting.addVault(COMBO_VAULT);
                case TREFLE  -> { } // le Trèfle rapporte déjà des gains
            }
        }
    }

    @Override
    public String getDescription() {
        String rule = switch (combo) {
            case PAIRE   -> "2 cartes du même rang";
            case SUITE   -> "3 rangs qui se suivent";
            case COULEUR -> "3 cartes ou plus, toutes de la même suite";
            case BRELAN  -> "3 cartes du même rang";
            case FULL    -> "un brelan et une paire";
        };
        return combo.getDisplayName() + " : si les cartes jouées ce tour forment " + rule
            + ", gains et attaque x" + formatFactor()
            + (combo == Combo.COULEUR || combo == Combo.SUITE ? "\nRemplit la jauge de chaque carte jouée" : "");
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
