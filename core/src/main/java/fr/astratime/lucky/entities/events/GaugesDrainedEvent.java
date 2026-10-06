package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Événement émis quand l'ennemi pipe ses dés : les jauges du joueur (Coffre, Sang, Lames) se vident en partie. */
public class GaugesDrainedEvent extends Event {
    /** Part de chaque jauge retirée, en %. */
    public final int percent;
    /** Total retiré des trois jauges. */
    public final int amount;

    public GaugesDrainedEvent(int percent, int amount) {
        this.percent = percent;
        this.amount  = amount;
    }

    @Override
    public String describe() { return "Jauges du joueur -" + percent + " % (" + amount + ")"; }

    @Override
    public List<EffectPopup> getPopups() {
        String text = amount > 0 ? Lang.f("JAUGES -{0} %", percent) : Lang.t("DÉ PIPÉ");
        return List.of(new EffectPopup(text, EffectPopup.Style.DAMAGE, PopupScale.SECONDARY_INTENSITY));
    }
}
