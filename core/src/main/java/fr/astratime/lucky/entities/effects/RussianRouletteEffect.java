package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.choices.RouletteChoice;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Roulette russe : le joueur retourne une carte parmi trois, faces cachées.
 * Quelle que soit la carte, le pistolet multiplie après le tirage les dégâts
 * du meilleur symbole d'attaque ({@link PistolEffect}). Une des trois est le
 * Joker maudit : le pistolet multiplie moins, et le joueur perd une partie de
 * ses gains.
 */
public class RussianRouletteEffect extends Effect {

    /** Nombre de cartes proposées, dont une seule maudite. */
    public static final int CARDS = 3;

    private final int multiplier;
    private final int cursedMultiplier;
    private final int penaltyPercent;

    /**
     * @param multiplier       multiplicateur des dégâts du pistolet avec une bonne carte
     * @param cursedMultiplier multiplicateur des dégâts du pistolet avec le Joker maudit
     * @param penaltyPercent   pourcentage des gains perdus avec le Joker maudit
     */
    public RussianRouletteEffect(int multiplier, int cursedMultiplier, int penaltyPercent) {
        this.multiplier       = multiplier;
        this.cursedMultiplier = cursedMultiplier;
        this.penaltyPercent   = penaltyPercent;
    }

    /** Mélange les cartes proposées et demande au joueur d'en choisir une. */
    @Override
    public void onPlay(PlayContext context) {
        List<Boolean> cursed = new ArrayList<>(Collections.nCopies(CARDS, false));
        cursed.set(0, true);
        Collections.shuffle(cursed);
        context.requestChoice(new RouletteChoice(multiplier, cursedMultiplier, penaltyPercent, cursed));
        context.addPopups(getPopups());
    }

    /** Aucun effet propre au spin : le pistolet est mis en attente par GameController si le joueur survit. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() {
        return "Choisissez une carte. Pistolet : attaque du meilleur symbole x" + multiplier
            + ", sans les multiplicateurs"
            + ". Joker maudit : pistolet x" + cursedMultiplier + " et -" + penaltyPercent + "% de gains";
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("ROULETTE RUSSE !", EffectPopup.Style.DAMAGE, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.RUSSIAN_ROULETTE; }
}
