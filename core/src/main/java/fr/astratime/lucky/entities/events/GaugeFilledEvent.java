package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Une jauge de couleur se remplit pendant le tirage : Sang (soin au-delà des
 * PV max) ou Coffre (bouclier resté inutilisé à la fin du tour).
 */
public class GaugeFilledEvent extends Event {

    /** Jauge remplie. */
    public enum Gauge { SANG, COFFRE }

    public final Gauge gauge;
    public final int   amount;

    public GaugeFilledEvent(Gauge gauge, int amount) {
        this.gauge  = gauge;
        this.amount = amount;
    }

    @Override
    public String describe() { return gauge + " +" + amount; }

    @Override
    public List<EffectPopup> getPopups() {
        EffectPopup.Style style = gauge == Gauge.SANG ? EffectPopup.Style.DRAIN : EffectPopup.Style.DEFENSE;
        return List.of(new EffectPopup(gauge.name() + " +" + amount, style, PopupScale.SECONDARY_INTENSITY * 0.5f));
    }
}
