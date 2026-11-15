package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;

/**
 * Pile ou face (carte à usure, demande d'Astra du 2026-11-14 ; « Pièce truquée » existait déjà au coffre de la
 * Salle des machines) : à chaque pose, une pièce est lancée. Pile, son bonus d'attaque est doublé ; face, rien.
 */
public class CoinFlipEffect extends Effect {

    /** Le lancer : {@code true} pour pile (remplacé dans les tests). */
    static BooleanSupplier coin = () -> ThreadLocalRandom.current().nextBoolean();

    private final int percent;

    /** @param percent attaque en plus de la carte, en % (doublée sur pile) */
    public CoinFlipEffect(int percent) { this.percent = percent; }

    @Override
    public void onPlay(PlayContext context) {
        if (coin.getAsBoolean()) {
            int bonus = percent * 2;
            context.queueForSpin(new WornAttack(bonus, "Pile ou face"));
            context.addPopups(List.of(new EffectPopup(Lang.t("PILE !"), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY),
                EffectPopup.scaled(Lang.f("ATTAQUE +{0} %", bonus), EffectPopup.Style.ATTACK, bonus,
                    PopupScale.CARD_ATTACK_BONUS * 2)));
        } else {
            context.addPopups(List.of(new EffectPopup(Lang.t("FACE... RIEN"), EffectPopup.Style.DAMAGE,
                PopupScale.SECONDARY_INTENSITY)));
        }
    }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().multiplyAttack(1f + percent / 100f, "Pile ou face"); }

    @Override
    public String getDescription() {
        return Lang.f("Lance une pièce. Pile : attaque +{0} % (le double de +{1} %). Face : rien.", percent * 2, percent);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("PILE OU FACE"), EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.BET; }
}
