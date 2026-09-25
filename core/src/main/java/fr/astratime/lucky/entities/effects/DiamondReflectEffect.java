package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.SymbolRegistry;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Carreau (non-As) : augmente la probabilité de tirer un symbole de défense et
 * ajoute un pourcentage de renvoi des dégâts ennemis. Le renvoi s'additionne
 * sur toutes les cartes Carreau jouées ce tour, et s'active si au moins un
 * symbole de défense sort (il est calculé lors de la riposte par CombatResolver).
 * Les deux valeurs augmentent avec le rang (voir cards/definitions/carreau.json).
 */
public class DiamondReflectEffect extends Effect {

    private final int percent;
    private final int defenseBoost;

    /**
     * @param percent      pourcentage de l'attaque ennemie renvoyé (additionné aux autres cartes Carreau)
     * @param defenseBoost boost de poids appliqué à chaque symbole de défense pour ce tour
     */
    public DiamondReflectEffect(int percent, int defenseBoost) {
        this.percent      = percent;
        this.defenseBoost = defenseBoost;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addReflectPercentBonus(percent);
        context.getCombatContext().addDefenseBonus(defenseBoost); // bouclier en plus : il remplit le Coffre
        if (defenseBoost > 0) {
            for (Symbol symbol : SymbolRegistry.getDefenseSymbols()) {
                context.getSpinContext().addWeightBoost(symbol, defenseBoost);
            }
        }
    }

    @Override
    public String getDescription() {
        return "Renvoi +" + percent + "% (attaque ennemie + 20% du Coffre) si un symbole de defense sort"
            + (defenseBoost > 0 ? "\nBouclier et boost des symboles de defense +" + defenseBoost : "");
    }

    @Override
    public List<EffectPopup> getPopups() {
        EffectPopup reflect = EffectPopup.scaled("RENVOI +" + percent + "%", EffectPopup.Style.REFLECT,
            percent, PopupScale.CARD_REFLECT_PERCENT);
        if (defenseBoost <= 0) return List.of(reflect);
        return List.of(reflect, EffectPopup.scaled("BOOST DÉFENSE +" + defenseBoost, EffectPopup.Style.DEFENSE,
            defenseBoost, PopupScale.CARD_DEFENSE_BOOST));
    }
}
