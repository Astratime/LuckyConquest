package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Événement émis quand l'ennemi rejoue la dernière carte du joueur (Reflet). Son effet suit. */
public class EnemyMirrorEvent extends Event {
    /** Nom de la carte copiée ({@code null} : aucune carte à copier, il frappe simplement). */
    public final String cardName;

    public EnemyMirrorEvent(String cardName) { this.cardName = cardName; }

    @Override
    public String describe() { return "Reflet : " + (cardName != null ? cardName : "aucune carte"); }

    @Override
    public List<EffectPopup> getPopups() {
        String text = cardName != null ? Lang.f("REFLET : {0}", cardName.toUpperCase()) : Lang.t("REFLET");
        return List.of(new EffectPopup(text, EffectPopup.Style.REFLECT, PopupScale.SECONDARY_INTENSITY));
    }
}
