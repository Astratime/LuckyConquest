package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;

import java.util.List;

/**
 * Bonus d'une carte révélé au lancer de la machine, avec ses textes (ex :
 * « EXÉCUTION x3 » de l'As de Pique, calculé à partir des Lames du moment).
 */
public class CardBonusEvent extends Event {

    private final String            description;
    private final List<EffectPopup> popups;

    public CardBonusEvent(String description, List<EffectPopup> popups) {
        this.description = description;
        this.popups      = List.copyOf(popups);
    }

    @Override
    public String describe() { return description; }

    @Override
    public List<EffectPopup> getPopups() { return popups; }
}
