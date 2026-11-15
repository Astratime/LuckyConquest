package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;

import java.util.List;

/**
 * Bonus d'attaque d'une carte à usure, fixé au moment où elle est jouée (la Bougie, la Graine et la Pile ou face
 * calculent leur bonus à la pose, puis le tirage l'applique).
 */
final class WornAttack extends Effect {

    private final int    percent;
    private final String label;

    /**
     * @param percent attaque en plus pour ce tirage, en %
     * @param label   nom de la carte, montré dans le détail des symboles
     */
    WornAttack(int percent, String label) {
        this.percent = percent;
        this.label   = label;
    }

    @Override
    public void apply(TurnContext context) {
        if (percent > 0) context.getCombatContext().multiplyAttack(1f + percent / 100f, label);
    }

    @Override
    public String getDescription() { return Lang.f("Attaque +{0} %", percent); }

    @Override
    public List<EffectPopup> getPopups() { return List.of(); }

    @Override
    public EffectSound getSound() { return EffectSound.ATTACK; }
}
