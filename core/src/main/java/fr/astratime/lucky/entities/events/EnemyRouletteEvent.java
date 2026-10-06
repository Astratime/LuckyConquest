package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/** Événement émis quand la roulette du Zéro de l'ennemi s'arrête : ses attaques ou ses Boucliers du tour doublent. */
public class EnemyRouletteEvent extends Event {

    /** Case où la bille s'arrête. */
    public enum Pocket { ROUGE, NOIR, ZERO }

    public final Pocket pocket;

    public EnemyRouletteEvent(Pocket pocket) { this.pocket = pocket; }

    @Override
    public String describe() { return "Roulette de l'ennemi : " + pocket; }

    @Override
    public List<EffectPopup> getPopups() {
        String text = switch (pocket) {
            case ROUGE -> Lang.t("ROUGE ! ATTAQUE x2");
            case NOIR  -> Lang.t("NOIR ! BOUCLIER x2");
            case ZERO  -> Lang.t("ZÉRO ! TOUT x2");
        };
        EffectPopup.Style style = pocket == Pocket.NOIR ? EffectPopup.Style.DEFENSE : EffectPopup.Style.ATTACK;
        return List.of(new EffectPopup(text, style, PopupScale.SECONDARY_INTENSITY));
    }
}
