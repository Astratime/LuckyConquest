package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Événement émis quand l'ennemi se soigne (Potion de sa machine). */
public class EnemyHealedEvent extends Event {
    /** Points de vie réellement rendus à l'ennemi (plafonnés à ses PV max). */
    public final int amount;

    public EnemyHealedEvent(int amount) { this.amount = amount; }

    @Override
    public String describe() { return "Ennemi soigne de " + amount + " PV"; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(EffectPopup.scaled(Lang.f("SOIN +{0}", amount), EffectPopup.Style.DRAIN, amount, PopupScale.ENEMY_HEAL));
    }
}
