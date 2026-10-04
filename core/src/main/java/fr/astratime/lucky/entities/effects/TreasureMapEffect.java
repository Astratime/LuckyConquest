package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * Carte au trésor (Port des Contrebandiers) : si le joueur vide le donjon en
 * cours, son coffre donne une carte de plus. Sans effet hors d'un donjon.
 */
public class TreasureMapEffect extends Effect {

    @Override
    public void onPlay(PlayContext context) {
        context.addTreasureMap();
        context.addPopups(getPopups());
    }

    /** Rien au tirage : la carte compte pour le coffre. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return "Si tu vides ce donjon, son coffre donne une carte de plus."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("COFFRE : UNE CARTE DE PLUS", EffectPopup.Style.GAINS, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public boolean canBeDoubled() { return false; }

    @Override
    public EffectSound getSound() { return EffectSound.SAFE; }
}
