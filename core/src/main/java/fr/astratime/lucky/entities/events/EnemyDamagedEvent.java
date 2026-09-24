package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Événement émis quand l'ennemi subit des dégâts (attaque du joueur). */
public class EnemyDamagedEvent extends Event {
    /** Dégâts effectivement infligés à l'ennemi (après défense). */
    public final int damage;
    /** Dégâts bruts avant défense (base du symbole + bonus d'attaque des cartes). */
    public final int rawDamage;

    /**
     * @param damage    dégâts effectivement infligés à l'ennemi (après défense).
     * @param rawDamage dégâts bruts avant défense (base + bonus d'attaque).
     */
    public EnemyDamagedEvent(int damage, int rawDamage) {
        this.damage = damage;
        this.rawDamage = rawDamage;
    }

    @Override
    public String describe() { return "Ennemi -" + damage + " PV"; }

    /** Dégâts effectivement infligés (après défense), taille maximale selon {@link PopupScale}. */
    @Override
    public List<EffectPopup> getPopups() {
        return List.of(EffectPopup.scaled("DÉGÂTS " + damage, EffectPopup.Style.ATTACK, damage, PopupScale.SPIN_DAMAGE));
    }
}
