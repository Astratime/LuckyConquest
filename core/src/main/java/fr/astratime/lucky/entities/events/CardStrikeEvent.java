package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Un coup de plus, porté par une carte après le tirage (Lame forgée,
 * Dynamite, Cœur d'or), sans tenir compte de la défense ennemie.
 */
public class CardStrikeEvent extends EnemyDamagedEvent {

    /** Nom du coup, affiché en titre (ex : "LAME FORGÉE !"). */
    public final String title;

    /**
     * @param title       nom du coup, en majuscules
     * @param damage      dégâts infligés à l'ennemi
     * @param rawDamage   dégâts avant sa peau d'or
     * @param defenseLeft défense de l'ennemi, ignorée par le coup
     */
    public CardStrikeEvent(String title, long damage, long rawDamage, int defenseLeft) {
        super(damage, rawDamage, 0, defenseLeft, true);
        this.title = title;
    }

    @Override
    public String describe() { return title + " : ennemi -" + damage + " PV"; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(
            new EffectPopup(title, EffectPopup.Style.ATTACK, PopupScale.MAX_INTENSITY),
            EffectPopup.scaled("DÉGÂTS " + damage, EffectPopup.Style.ATTACK, damage, PopupScale.SPIN_DAMAGE));
    }
}
