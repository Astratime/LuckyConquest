package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Longue-vue (Port des Contrebandiers) : le résultat du rouleau du milieu est
 * tiré à la pose, parmi les symboles de la machine qui peuvent sortir, et
 * montré tout de suite ; il est imposé au tirage (voir {@link ForceReelEffect}).
 */
public class SpyglassEffect extends Effect {

    private final Random random;

    public SpyglassEffect() { this(new Random()); }

    /** @param random source d'aléatoire (graine fixe dans les tests) */
    public SpyglassEffect(Random random) { this.random = random; }

    @Override
    public void onPlay(PlayContext context) {
        List<Symbol> options = new ArrayList<>(context.getReels());
        options.removeIf(context.getLastingEffects().getRemovedSymbols()::containsKey);
        if (options.isEmpty()) return;
        ForceReelEffect seen = new ForceReelEffect(options.get(random.nextInt(options.size())));
        context.queueForSpin(seen);
        context.addPopups(List.of(new EffectPopup("LONGUE-VUE", EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY)));
        context.addPopups(seen.getPopups());
    }

    /** Rien de plus au tirage : le symbole vu est imposé par l'effet mis en attente à la pose. */
    @Override
    public void apply(TurnContext context) { }

    @Override
    public String getDescription() { return "Montre le prochain résultat du rouleau du milieu."; }

    @Override
    public List<EffectPopup> getPopups() {
        return List.of(new EffectPopup("LONGUE-VUE", EffectPopup.Style.SPECIAL, PopupScale.SECONDARY_INTENSITY));
    }

    @Override
    public boolean canBeDoubled() { return false; }

    @Override
    public EffectSound getSound() { return EffectSound.RIGGED_REEL; }
}
