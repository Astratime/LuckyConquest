package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.choices.RouletteChoice;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Roulette russe : le joueur retourne une carte parmi trois, faces cachées.
 * Deux sont des cartes Roulette : le pistolet multiplie après le tirage les
 * dégâts du meilleur symbole d'attaque ({@link PistolEffect}). La troisième
 * est le Joker maudit : le joueur perd une partie de ses gains.
 */
public class RussianRouletteEffect extends Effect {

    /** Nombre de cartes proposées, dont une seule maudite. */
    public static final int CARDS = 3;

    private final int multiplier;
    private final int penaltyPercent;

    /**
     * @param multiplier     multiplicateur des dégâts du pistolet
     * @param penaltyPercent pourcentage des gains perdus avec le Joker maudit
     */
    public RussianRouletteEffect(int multiplier, int penaltyPercent) {
        this.multiplier     = multiplier;
        this.penaltyPercent = penaltyPercent;
    }

    /** Mélange les cartes proposées et demande au joueur d'en choisir une. */
    @Override
    public void onPlay(PlayContext context) {
        List<Boolean> cursed = new ArrayList<>(Collections.nCopies(CARDS, false));
        cursed.set(0, true);
        Collections.shuffle(cursed);
        context.requestChoice(new RouletteChoice(multiplier, penaltyPercent, cursed));
        context.addPopups(getPopups());
    }

    /** Aucun effet propre au spin : le pistolet est mis en attente par GameController si le joueur survit. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() {
        return "Choisissez une carte : pistolet (dégâts d'un symbole x" + multiplier
            + ") ou Joker maudit (-" + penaltyPercent + "% de gains)";
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("ROULETTE RUSSE !", EffectPopup.Style.DAMAGE, PopupScale.MAX_INTENSITY));
    }
}
