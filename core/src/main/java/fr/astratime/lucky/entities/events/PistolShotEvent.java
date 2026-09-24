package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Événement émis quand le pistolet de la Roulette russe tire sur l'ennemi :
 * il multiplie les dégâts du symbole d'attaque le plus fort du tirage.
 */
public class PistolShotEvent extends EnemyDamagedEvent {
    /** Rouleau du symbole dont les dégâts sont multipliés (-1 si aucun symbole d'attaque n'est sorti). */
    public final int slotIndex;
    /** Multiplicateur appliqué aux dégâts du symbole. */
    public final int multiplier;

    /**
     * @param damage     dégâts effectivement infligés (après défense)
     * @param rawDamage  dégâts bruts du tir, avant défense
     * @param slotIndex  rouleau du symbole visé, -1 si aucun
     * @param multiplier multiplicateur appliqué aux dégâts du symbole
     */
    public PistolShotEvent(int damage, int rawDamage, int slotIndex, int multiplier) {
        super(damage, rawDamage);
        this.slotIndex  = slotIndex;
        this.multiplier = multiplier;
    }

    @Override
    public String describe() { return "Pistolet x" + multiplier + " : ennemi -" + damage + " PV"; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup("PAN ! x" + multiplier, EffectPopup.Style.ATTACK, PopupScale.MAX_INTENSITY),
            EffectPopup.scaled("DÉGÂTS " + damage, EffectPopup.Style.ATTACK, damage, PopupScale.SPIN_DAMAGE));
    }
}
