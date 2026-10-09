package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.ArrayList;
import java.util.List;

/**
 * Événement émis quand le pistolet de la Roulette russe tire sur l'ennemi :
 * il rejoue le coup le plus fort du tour (en partie avec le Joker maudit).
 * Sans symbole d'attaque sorti, il tire à blanc.
 */
public class PistolShotEvent extends EnemyDamagedEvent {
    /** Rouleau du symbole dont le coup est rejoué (-1 si aucun symbole d'attaque n'est sorti). */
    public final int slotIndex;
    /** Part (%) du coup rejouée. */
    public final int percent;
    /** {@code true} si aucun symbole d'attaque n'est sorti : le pistolet tire à blanc. */
    public final boolean blank;

    /**
     * @param damage      dégâts effectivement infligés (après défense)
     * @param rawDamage   dégâts bruts du tir, avant défense
     * @param blocked     dégâts absorbés par la défense de l'ennemi
     * @param defenseLeft défense de l'ennemi restante après le tir
     * @param pierce      part (%) du tir qui a traversé la défense (Pique)
     * @param slotIndex   rouleau du symbole visé, -1 si aucun
     * @param percent     part (%) du coup rejouée
     */
    public PistolShotEvent(long damage, long rawDamage, int blocked, int defenseLeft, int pierce,
                           int slotIndex, int percent) {
        super(damage, rawDamage, rawDamage, blocked, defenseLeft, pierce);
        this.slotIndex = slotIndex;
        this.percent   = percent;
        this.blank     = slotIndex < 0;
    }

    /** @return un tir à blanc : aucun symbole d'attaque n'est sorti ce tour. */
    public static PistolShotEvent blank(int defenseLeft, int percent) {
        return new PistolShotEvent(0, 0, 0, defenseLeft, 0, -1, percent);
    }

    @Override
    public String describe() {
        return blank ? "Pistolet : tir à blanc" : "Pistolet " + percent + "% : ennemi -" + damage + " PV";
    }

    @Override
    public List<EffectPopup> getPopups() {
        if (blank) return List.of(new EffectPopup(Lang.t("CLIC ! TIR À BLANC"), EffectPopup.Style.SPECIAL, PopupScale.MAX_INTENSITY));
        List<EffectPopup> popups = new ArrayList<>();
        popups.add(new EffectPopup(percent >= 100 ? Lang.t("PAN !") : Lang.f("PAN ! {0}%", percent),
            EffectPopup.Style.ATTACK, PopupScale.MAX_INTENSITY));
        popups.addAll(super.getPopups());
        return popups;
    }
}
