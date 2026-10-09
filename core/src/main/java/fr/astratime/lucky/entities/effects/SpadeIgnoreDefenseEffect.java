package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.controllers.PreparationResolver;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Pique (non-As) : une part de chaque coup traverse la défense ennemie ce tour
 * (seul le meilleur perçage des Piques jouées compte), et les attaques
 * reçoivent un bonus. La carte ajoute aussi des Lames, qui restent tout le
 * combat (voir PreparationResolver#BLADE_ATTACK) et que l'As de Pique encaisse.
 * Les têtes percent le plus, les petites Piques donnent le plus de Lames
 * (voir cards/definitions/pique.json).
 */
public class SpadeIgnoreDefenseEffect extends Effect {

    private final int attackBonus;
    private final int blades;
    private final int piercePercent;

    /**
     * @param attackBonus   bonus d'attaque plat accordé pour ce tour
     * @param blades        Lames ajoutées quand la carte est jouée
     * @param piercePercent part (%) de chaque coup qui traverse la défense ennemie ce tour
     */
    public SpadeIgnoreDefenseEffect(int attackBonus, int blades, int piercePercent) {
        this.attackBonus   = attackBonus;
        this.blades        = blades;
        this.piercePercent = piercePercent;
    }

    /** Les Lames s'ajoutent tout de suite (elles apparaissent dans le panneau), le reste au spin. */
    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().addBlades(blades);
        super.onPlay(context);
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().pierceDefense(piercePercent);
        context.getCombatContext().addAttackBonus(attackBonus, "Cartes");
    }

    @Override
    public String getDescription() {
        return Lang.f(Lang.plural(blades)
                ? "{3}% de chaque coup traverse la défense. Attaque +{0} sur chaque symbole d'attaque. +{1} Lames "
                    + "(+{2} d'attaque chacune, tout le combat)"
                : "{3}% de chaque coup traverse la défense. Attaque +{0} sur chaque symbole d'attaque. +{1} Lame "
                    + "(+{2} d'attaque chacune, tout le combat)",
            attackBonus, blades, PreparationResolver.BLADE_ATTACK, piercePercent);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(Lang.f("PERCE-DÉFENSE {0}%", piercePercent), EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY),
            EffectPopup.scaled(Lang.f("ATTAQUE +{0}", attackBonus), EffectPopup.Style.ATTACK, attackBonus, PopupScale.CARD_ATTACK_BONUS),
            new EffectPopup(Lang.f("LAMES +{0}", blades), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY)
        );
    }

    @Override
    public EffectSound getSound() { return EffectSound.SPADE_IGNORE_DEFENSE; }
}
