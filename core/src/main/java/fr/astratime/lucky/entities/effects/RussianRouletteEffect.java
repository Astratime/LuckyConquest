package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.choices.RouletteChoice;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Roulette russe : le joueur retourne une carte parmi trois, faces cachées.
 * Quelle que soit la carte, le pistolet rejoue après le tirage le coup le
 * plus fort du tour ({@link PistolEffect}). Une des trois est le Joker
 * maudit : le pistolet n'en rejoue qu'une partie, et le joueur perd une
 * partie de ses gains.
 */
public class RussianRouletteEffect extends Effect {

    /** Nombre de cartes proposées, dont une seule maudite. */
    public static final int CARDS = 3;

    private final int percent;
    private final int cursedPercent;
    private final int penaltyPercent;

    /**
     * @param percent       part (%) du meilleur coup du tour rejouée par le pistolet avec une bonne carte
     * @param cursedPercent part (%) du meilleur coup rejouée avec le Joker maudit
     * @param penaltyPercent   pourcentage des gains perdus avec le Joker maudit
     */
    public RussianRouletteEffect(int percent, int cursedPercent, int penaltyPercent) {
        this.percent          = percent;
        this.cursedPercent    = cursedPercent;
        this.penaltyPercent   = penaltyPercent;
    }

    /** Mélange les cartes proposées et demande au joueur d'en choisir une. */
    @Override
    public void onPlay(PlayContext context) {
        List<Boolean> cursed = new ArrayList<>(Collections.nCopies(CARDS, false));
        cursed.set(0, true);
        Collections.shuffle(cursed);
        context.requestChoice(new RouletteChoice(percent, cursedPercent, penaltyPercent, cursed));
        context.addPopups(getPopups());
    }

    /** Aucun effet propre au spin : le pistolet est mis en attente par GameController si le joueur survit. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() {
        return Lang.f("Choisissez une carte. Pistolet : rejoue le coup le plus fort du tour. "
            + "Joker maudit : le coup rejoué perd {0}% et -{1}% de gains",
            percent - cursedPercent, penaltyPercent);
    }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup(Lang.t("ROULETTE RUSSE !"), EffectPopup.Style.DAMAGE, PopupScale.MAX_INTENSITY));
    }

    @Override
    public EffectSound getSound() { return EffectSound.RUSSIAN_ROULETTE; }
}
