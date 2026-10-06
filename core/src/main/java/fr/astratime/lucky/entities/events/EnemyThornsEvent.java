package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Événement émis quand l'ennemi sort ses Épines : une part des dégâts du joueur lui sera renvoyée. */
public class EnemyThornsEvent extends Event {
    /** Part des dégâts renvoyée ajoutée, en %. */
    public final int percent;
    /** Part totale renvoyée jusqu'à son tour suivant, en %. */
    public final int total;

    public EnemyThornsEvent(int percent, int total) {
        this.percent = percent;
        this.total   = total;
    }

    @Override
    public String describe() { return "Ennemi epines +" + percent + " % (total " + total + " %)"; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.f("ÉPINES {0} %", total), EffectPopup.Style.REFLECT, PopupScale.SECONDARY_INTENSITY));
    }
}
