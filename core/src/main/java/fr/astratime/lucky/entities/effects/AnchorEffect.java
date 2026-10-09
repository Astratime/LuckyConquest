package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Ancre (la Salle VIP) : la marée haute ne baisse plus l'attaque pendant
 * quelques tours, et la défense du tirage augmente.
 */
public class AnchorEffect extends Effect {

    private final int turns;
    private final int defensePercent;

    /**
     * @param turns          tours sans marée haute, celui-ci compris
     * @param defensePercent défense ajoutée au tirage, en %
     */
    public AnchorEffect(int turns, int defensePercent) {
        this.turns          = turns;
        this.defensePercent = defensePercent;
    }

    @Override
    public void onPlay(PlayContext context) {
        context.getLastingEffects().addAnchor(turns);
        context.queueForSpin(this);
        context.addPopups(getPopups());
    }

    @Override
    public void apply(TurnContext context) { context.getCombatContext().multiplyDefense(1f + defensePercent / 100f, "Ancre"); }

    @Override
    public String getDescription() { return Lang.f("Pendant {0} tours, la marée haute ne baisse plus ton attaque. Défense +{1} %.",
        turns, defensePercent); }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("ANCRE JETÉE"), EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY),
            new EffectPopup(Lang.f("DÉFENSE +{0} %", defensePercent), EffectPopup.Style.DEFENSE, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.DEFENSE; }
}
