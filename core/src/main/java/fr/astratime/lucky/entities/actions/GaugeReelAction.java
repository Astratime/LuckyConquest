package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.ReelGauges;
import fr.astratime.lucky.i18n.Lang;

/**
 * Roi démon et Apôtre de la Chance : attaque, bouclier et gains, et chaque
 * sortie remplit leur jauge (voir {@link ReelGauges#fill}, appelé après le tirage).
 */
public final class GaugeReelAction extends MixedAction {

    private final String gauge;
    private final String reward;

    /**
     * @param value  attaque, bouclier et gains de base
     * @param gauge  nom de la jauge (texte français, traduit à l'affichage)
     * @param reward nom du symbole du Bingo imposé (texte français, traduit à l'affichage)
     */
    public GaugeReelAction(int value, String gauge, String reward) {
        super(new AttackAction(value), new DefenseAction(value), new GainAction(value));
        this.gauge  = gauge;
        this.reward = reward;
    }

    @Override
    public String getDescription() {
        return Lang.f("{0}. {1} +1. À {2} : invincible, Bingo de {3}", super.getDescription(), Lang.t(gauge),
            ReelGauges.GAUGE_MAX, Lang.t(reward));
    }
}
