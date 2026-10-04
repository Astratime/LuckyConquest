package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.ArrayList;
import java.util.List;

/**
 * Événement émis quand une attaque du joueur frappe l'ennemi : sa défense en
 * absorbe d'abord ce qu'elle peut (et s'use d'autant), sauf si l'attaque la
 * perce (Pique).
 */
public class EnemyDamagedEvent extends Event {
    /** Dégâts effectivement infligés à l'ennemi (après défense). */
    public final long damage;
    /** Dégâts bruts avant défense (base du symbole + bonus d'attaque des cartes). */
    public final long rawDamage;
    /** Dégâts absorbés par la défense de l'ennemi. */
    public final int blocked;
    /** Défense de l'ennemi restante après ce coup. */
    public final int defenseLeft;
    /** {@code true} si l'attaque a ignoré la défense de l'ennemi (Pique). */
    public final boolean pierced;

    /**
     * @param damage      dégâts effectivement infligés à l'ennemi (après défense)
     * @param rawDamage   dégâts bruts avant défense (base + bonus d'attaque)
     * @param blocked     dégâts absorbés par la défense
     * @param defenseLeft défense de l'ennemi restante après ce coup
     * @param pierced     {@code true} si l'attaque a ignoré la défense (Pique)
     */
    public EnemyDamagedEvent(long damage, long rawDamage, int blocked, int defenseLeft, boolean pierced) {
        this.damage      = damage;
        this.rawDamage   = rawDamage;
        this.blocked     = blocked;
        this.defenseLeft = defenseLeft;
        this.pierced     = pierced;
    }

    @Override
    public String describe() {
        return "Ennemi -" + damage + " PV" + (blocked > 0 ? " (" + blocked + " bloqués)" : "")
            + (pierced ? " (défense percée)" : "");
    }

    /**
     * Dégâts effectivement infligés, taille maximale selon {@link PopupScale},
     * puis ce que la défense a bloqué. Un coup tout entier bloqué n'affiche que
     * « BLOQUÉ ».
     */
    @Override
    public List<EffectPopup> getPopups() {
        List<EffectPopup> popups = new ArrayList<>();
        if (damage > 0 || blocked == 0) {
            popups.add(EffectPopup.scaled("DÉGÂTS " + damage, EffectPopup.Style.ATTACK, damage, PopupScale.SPIN_DAMAGE));
        }
        if (blocked > 0) popups.add(blockedPopup(blocked, damage == 0));
        return popups;
    }

    /**
     * @param full {@code true} si tout le coup a été bloqué (texte plus grand)
     * @return le texte « BLOQUÉ » d'une défense qui a absorbé {@code blocked} dégâts
     */
    static EffectPopup blockedPopup(int blocked, boolean full) {
        return new EffectPopup((full ? "BLOQUÉ ! -" : "BLOQUÉ -") + blocked, EffectPopup.Style.DEFENSE,
            full ? PopupScale.SECONDARY_INTENSITY : PopupScale.SECONDARY_INTENSITY * 0.75f);
    }
}
