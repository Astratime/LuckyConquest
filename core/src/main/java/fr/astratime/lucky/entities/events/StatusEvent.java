package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Un effet qui se montre par un simple texte : la règle d'un lieu de
 * l'Exploration (Scorbut, marée haute…), ou un mauvais sort jeté par
 * l'ennemi pour le prochain tour (Grignotage, Aveuglement…).
 */
public class StatusEvent extends Event {
    /** Texte affiché à l'écran. */
    public final String text;
    /** Famille d'effet du texte (sa couleur). */
    public final EffectPopup.Style style;

    public StatusEvent(String text, EffectPopup.Style style) {
        this.text  = text;
        this.style = style;
    }

    @Override
    public String describe() { return text; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(text, style, PopupScale.SECONDARY_INTENSITY));
    }
}
