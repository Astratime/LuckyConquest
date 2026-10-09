package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Guillotine : une part des PV restants de l'ennemi, infligée d'un coup, sans tenir compte de sa défense. */
public class ExecutionEvent extends EnemyDamagedEvent {

    /** Part des PV restants de l'ennemi infligée, en %. */
    public final int percent;

    /**
     * @param damage      dégâts infligés à l'ennemi
     * @param percent     part de ses PV restants infligée, en %
     * @param defenseLeft défense de l'ennemi, ignorée par la Guillotine
     */
    public ExecutionEvent(long damage, int percent, int defenseLeft) {
        super(damage, damage, 0, defenseLeft, true);
        this.percent = percent;
    }

    @Override
    public String describe() { return "Guillotine (" + percent + "%) : ennemi -" + damage + " PV"; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(Lang.t("GUILLOTINE !"), EffectPopup.Style.ATTACK, PopupScale.MAX_INTENSITY),
            EffectPopup.scaled(Lang.f("DÉGÂTS {0}", Lang.big(damage)), EffectPopup.Style.ATTACK, damage, PopupScale.SPIN_DAMAGE));
    }
}
