package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** As de Carreau sans symbole d'attaque : le Coffre vidé explose sur l'ennemi, sans tenir compte de sa défense. */
public class VaultBurstEvent extends EnemyDamagedEvent {

    /**
     * @param damage      dégâts infligés à l'ennemi
     * @param defenseLeft défense de l'ennemi, ignorée par l'explosion
     */
    public VaultBurstEvent(long damage, int defenseLeft) {
        super(damage, damage, 0, defenseLeft, true);
    }

    @Override
    public String describe() { return "Explosion du Coffre : ennemi -" + damage + " PV"; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(Lang.t("LE COFFRE EXPLOSE !"), EffectPopup.Style.DEFENSE, PopupScale.MAX_INTENSITY),
            EffectPopup.scaled(Lang.f("DÉGÂTS {0}", Lang.big(damage)), EffectPopup.Style.ATTACK, damage, PopupScale.SPIN_DAMAGE));
    }
}
