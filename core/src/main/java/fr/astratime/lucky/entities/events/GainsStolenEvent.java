package fr.astratime.lucky.entities.events;

import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.ArrayList;
import java.util.List;

/** Événement émis quand l'ennemi prend des gains au joueur (Intérêts) : ils renforcent son prochain coup. */
public class GainsStolenEvent extends GainsLostEvent {
    /** Attaque ajoutée à son prochain coup. */
    public final int attack;

    public GainsStolenEvent(int amount, int attack) {
        super(amount);
        this.attack = attack;
    }

    @Override
    public String describe() { return "Ennemi vole " + amount + " gains (attaque +" + attack + ")"; }

    @Override
    public List<EffectPopup> getPopups() {
        List<EffectPopup> popups = new ArrayList<>();
        popups.add(EffectPopup.scaled("GAINS VOLÉS -" + amount, EffectPopup.Style.DAMAGE, amount, PopupScale.SPIN_GAINS));
        if (attack > 0) popups.add(new EffectPopup("ATTAQUE +" + attack, EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY));
        return popups;
    }
}
