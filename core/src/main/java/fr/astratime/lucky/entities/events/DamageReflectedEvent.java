package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.entities.effects.EffectPopup;

import java.util.List;

/** Événement émis quand le bouclier de renvoi du joueur (Carreau) renvoie des dégâts à l'ennemi. */
public class DamageReflectedEvent extends Event {
    /** Dégâts renvoyés à l'ennemi. */
    public final int damage;

    /** @param damage dégâts renvoyés à l'ennemi. */
    public DamageReflectedEvent(int damage) { this.damage = damage; }

    @Override
    public String describe() { return "Renvoi -" + damage + " PV a l'ennemi"; }

    /** Dégâts renvoyés à l'ennemi ; 30 donne la taille de texte maximale. */
    @Override
    public List<EffectPopup> getPopups() {
        return List.of(EffectPopup.scaled("RENVOI " + damage, EffectPopup.Style.REFLECT, damage, 30f));
    }
}
