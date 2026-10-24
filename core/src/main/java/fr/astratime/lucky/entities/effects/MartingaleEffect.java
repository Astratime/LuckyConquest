package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Martingale (archétype Relanceur) : un tirage sans paire prépare le suivant (plus d'attaque, plus de
 * paires), jusqu'à {@link fr.astratime.lucky.entities.LastingEffects#MAX_MARTINGALE} fois d'affilée.
 */
public class MartingaleEffect extends Effect {
    private final int attackPercent;
    private final int pairPercent;

    /**
     * Martingale.
     * @param attackPercent attaque en plus au tirage suivant, par tirage raté, en %
     * @param pairPercent chance de paire en plus au tirage suivant, par tirage raté, en %
     */
    public MartingaleEffect(int attackPercent, int pairPercent) {
        this.attackPercent = attackPercent;
        this.pairPercent = pairPercent;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().addMartingale(attackPercent, pairPercent);
    }

    @Override
    public String getDescription() { return Lang.f("Si ce tirage n'a pas de paire : au suivant, attaque +{0} % et paire +{1} %. Se cumule jusqu'à 3 fois d'affilée.", attackPercent, pairPercent); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("MARTINGALE"), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.BET; }

}
