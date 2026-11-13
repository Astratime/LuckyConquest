package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Trèfle (non-As) : la couleur de la chance. La carte remplit la Bonne étoile, qui
 * reste tout le combat : chaque point donne 1 % de chance de Paire en plus et 1 % de
 * chance qu'une Paire devienne un Bingo (voir {@link fr.astratime.lucky.controllers.TurnEngine}) ;
 * l'As de Trèfle la vide pour un Bingo. La carte donne aussi un bonus d'attaque.
 */
public class ClubLuckyStarEffect extends Effect {

    private final int attackBonus;
    private final int stars;

    /**
     * @param attackBonus bonus d'attaque plat accordé pour ce tour
     * @param stars       points de Bonne étoile ajoutés quand la carte est jouée
     */
    public ClubLuckyStarEffect(int attackBonus, int stars) {
        this.attackBonus = attackBonus;
        this.stars       = stars;
    }

    /** La Bonne étoile se remplit tout de suite (elle apparaît dans le panneau), l'attaque au tirage. */
    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().addLuckyStar(stars);
        super.onPlay(context);
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addAttackBonus(attackBonus, "Cartes");
    }

    @Override
    public String getDescription() {
        return Lang.f("Bonne étoile +{0} (tout le combat : plus de Paires, et des Paires qui deviennent des Bingos x{2}). "
            + "Attaque +{1} sur chaque symbole d'attaque", stars, attackBonus, LastingEffects.STAR_BINGO_POWER);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(Lang.f("BONNE ÉTOILE +{0}", stars), EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY),
            EffectPopup.scaled(Lang.f("ATTAQUE +{0}", attackBonus), EffectPopup.Style.ATTACK, attackBonus, PopupScale.CARD_ATTACK_BONUS)
        );
    }

    @Override
    public EffectSound getSound() { return EffectSound.CLUB_GAIN_ATTACK; }
}
