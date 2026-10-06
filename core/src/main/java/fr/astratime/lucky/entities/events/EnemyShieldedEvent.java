package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Événement émis quand l'ennemi gagne de la défense (Bouclier de sa machine), pour le tour suivant du joueur. */
public class EnemyShieldedEvent extends Event {
    /** Défense ajoutée. */
    public final int defense;

    public EnemyShieldedEvent(int defense) { this.defense = defense; }

    @Override
    public String describe() { return "Ennemi defense +" + defense; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(EffectPopup.scaled(Lang.f("DÉFENSE +{0}", defense), EffectPopup.Style.DEFENSE, defense,
            PopupScale.ENEMY_SHIELD));
    }
}
