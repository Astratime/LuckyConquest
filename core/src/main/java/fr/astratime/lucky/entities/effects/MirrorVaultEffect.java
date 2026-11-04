package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.events.ShieldGainedEvent;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Miroir taillé (donjon du Carreau) : du bouclier tout de suite, et le bouclier
 * restant en fin de tour remplit le Coffre avec un multiplicateur (additionné à
 * celui des cartes Carreau). Remplace l'ancien renvoi de 200 % (Astra 2026-11-04).
 */
public class MirrorVaultEffect extends Effect {

    private final int shield;
    private final int vaultBonus;

    /**
     * @param shield     bouclier accordé au tirage
     * @param vaultBonus % ajouté au multiplicateur du bouclier restant versé au Coffre
     */
    public MirrorVaultEffect(int shield, int vaultBonus) {
        this.shield     = shield;
        this.vaultBonus = vaultBonus;
    }

    @Override
    public void apply(TurnContext context) {
        context.getCombatContext().getPlayer().addShield(shield);
        context.addEvent(new ShieldGainedEvent(shield));
        context.getCombatContext().addVaultBonus(vaultBonus);
    }

    private String times() {
        float times = vaultBonus / 100f;
        String text = times == (int) times ? String.valueOf((int) times) : String.valueOf(times);
        return Lang.isEnglish() ? text : text.replace('.', ',');
    }

    @Override
    public String getDescription() {
        return Lang.f("Bouclier +{0}. Bouclier restant en fin de tour : Coffre x{1} de plus", shield, times());
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            EffectPopup.scaled(Lang.f("BOUCLIER +{0}", shield), EffectPopup.Style.DEFENSE, shield, PopupScale.CARD_DEFENSE_BONUS),
            new EffectPopup(Lang.f("COFFRE x{0} DE PLUS", times()), EffectPopup.Style.DEFENSE, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.DIAMOND_REFLECT; }
}
