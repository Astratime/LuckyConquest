package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Événement émis quand l'ennemi fait tapis (sa mise doublera à son tour
 * suivant), puis quand ce Tapis tient et que sa mise double : ses attaques
 * avec, jusqu'à ce qu'il soit touché.
 */
public class EnemyStakeEvent extends Event {
    /** Sa mise (multiplicateur de ses attaques). */
    public final int stake;
    /** {@code true} : il vient de poser son Tapis ; {@code false} : sa mise vient de doubler. */
    public final boolean placed;

    public EnemyStakeEvent(int stake, boolean placed) {
        this.stake  = stake;
        this.placed = placed;
    }

    @Override
    public String describe() { return placed ? "Tapis posé (mise x" + stake + ")" : "Tapis : mise x" + stake; }

    @Override
    public List<EffectPopup> getPopups() {
        String text = placed ? "TAPIS !" : "MISE x" + stake;
        return List.of(new EffectPopup(text, EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
    }
}
