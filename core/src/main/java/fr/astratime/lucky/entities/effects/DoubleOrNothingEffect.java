package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Double ou rien : la prochaine carte jouée ce tour compte deux fois (voir
 * GameController#playCard). Une carte qui demande un choix (Pari, Roulette
 * russe, Rouleau truqué) ne compte qu'une fois : le doublement attend la suivante.
 */
public class DoubleOrNothingEffect extends Effect {

    @Override
    public void onPlay(PlayContext context) {
        context.requestDouble();
        context.addPopups(getPopups());
    }

    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return "La prochaine carte jouée ce tour compte deux fois."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("PROCHAINE CARTE x2", EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.DOUBLE_OR_NOTHING; }
}
