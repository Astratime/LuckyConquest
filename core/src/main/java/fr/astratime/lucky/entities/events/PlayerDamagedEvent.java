package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.ArrayList;
import java.util.List;

/**
 * Événement émis quand le joueur subit une attaque (riposte de l'ennemi) : son
 * bouclier en absorbe d'abord ce qu'il peut, et s'use d'autant.
 */
public class PlayerDamagedEvent extends Event {
    /** Dégâts effectivement infligés au joueur (après bouclier). */
    public final int damage;
    /** Dégâts absorbés par le bouclier du joueur. */
    public final int blocked;
    /** Bouclier du joueur restant après ce coup. */
    public final int shieldLeft;

    /** @param damage dégâts effectivement infligés au joueur (sans bouclier en jeu). */
    public PlayerDamagedEvent(int damage) { this(damage, 0, 0); }

    /**
     * @param damage     dégâts effectivement infligés au joueur (après bouclier)
     * @param blocked    dégâts absorbés par son bouclier
     * @param shieldLeft bouclier restant après ce coup
     */
    public PlayerDamagedEvent(int damage, int blocked, int shieldLeft) {
        this.damage     = damage;
        this.blocked    = blocked;
        this.shieldLeft = shieldLeft;
    }

    @Override
    public String describe() { return "Joueur -" + damage + " PV" + (blocked > 0 ? " (" + blocked + " bloqués)" : ""); }

    /**
     * Vie perdue par le joueur, taille maximale selon {@link PopupScale}, puis
     * ce que son bouclier a bloqué ; « BLOQUÉ ! » si le bouclier a tout absorbé.
     */
    @Override
    public List<EffectPopup> getPopups() {
        if (damage == 0 && blocked == 0) {
            return List.of(new EffectPopup(Lang.t("BLOQUÉ !"), EffectPopup.Style.DEFENSE, PopupScale.SECONDARY_INTENSITY));
        }
        List<EffectPopup> popups = new ArrayList<>();
        if (damage > 0) popups.add(EffectPopup.scaled(Lang.f("PV -{0}", damage), EffectPopup.Style.DAMAGE, damage, PopupScale.SPIN_LIFE_LOST));
        if (blocked > 0) popups.add(EnemyDamagedEvent.blockedPopup(blocked, damage == 0));
        return popups;
    }
}
