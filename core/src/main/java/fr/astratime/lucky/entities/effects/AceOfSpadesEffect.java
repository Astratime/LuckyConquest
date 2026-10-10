package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.LastingEffects;
import fr.astratime.lucky.entities.context.PlayContext;
import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.entities.events.CardBonusEvent;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.List;

/**
 * As de Pique, « Exécution » : à la pose, il consomme une part des gains pour
 * aiguiser de nouvelles Lames ; au lancer, il encaisse toutes les Lames :
 * l'attaque du tour est multipliée selon leur nombre, et {@link #PIERCE_PERCENT} %
 * de chaque coup traverse la défense ennemie (tout, pour sa version « + »). Les Lames comptent encore pour ce tirage, puis sont vidées.
 */
public class AceOfSpadesEffect extends Effect {

    /** Part des gains consommée à la pose. */
    static final float CONSUME_PERCENT = 0.1f;
    /** Gains à consommer pour une Lame. */
    static final int   GAINS_PER_BLADE = 250;
    /** Lames au plus obtenues avec les gains. */
    static final int   MAX_BOUGHT_BLADES = 5;
    /** Multiplicateur d'attaque ajouté par Lame encaissée. */
    static final float FACTOR_PER_BLADE = 0.5f;
    /** Part (%) de chaque coup qui traverse la défense ennemie (Astra : 80, au lieu de toute la défense). */
    static final int   PIERCE_PERCENT = 80;

    /** Puissance de la carte : 1, ou {@link fr.astratime.lucky.entities.Card#UPGRADE_FACTOR} pour sa version « + ». */
    private final float power;

    public AceOfSpadesEffect() { this(1f); }

    /** @param power puissance : ses valeurs sont multipliées par autant (version « + » de la carte) */
    public AceOfSpadesEffect(float power) { this.power = power; }

    @Override
    public void onPlay(PlayContext context) {
        int consumed = context.consumeGainsPercent(CONSUME_PERCENT);
        int blades   = Math.min(MAX_BOUGHT_BLADES, consumed / GAINS_PER_BLADE);
        context.getLastingEffects().addBlades(blades);
        context.queueForSpin(this);
        context.addPopups(List.of(
            new EffectPopup(Lang.t("EXÉCUTION"), EffectPopup.Style.ATTACK, PopupScale.MAX_INTENSITY),
            new EffectPopup(Lang.f("-10% GAINS : LAMES +{0}", blades), EffectPopup.Style.GAINS, PopupScale.SECONDARY_INTENSITY)));
    }

    /** Encaisse les Lames : attaque multipliée pour ce tour, défense ennemie percée de {@link #piercePercent()} %. */
    @Override
    public void apply(TurnContext context) {
        LastingEffects lasting = context.getCombatContext().getPlayer().getLastingEffects();
        int   blades = lasting.consumeBlades();
        float factor = 1f + FACTOR_PER_BLADE * power * blades;
        context.getCombatContext().pierceDefense(piercePercent());
        context.getCombatContext().multiplyAttack(factor, "As de Pique");
        String times = factor == (int) factor ? String.valueOf((int) factor) : String.valueOf(factor);
        context.addEvent(new CardBonusEvent(Lang.f("Execution : {0} lames, attaque x{1}", blades, times), List.of(
            new EffectPopup(Lang.f("EXÉCUTION : {0} LAMES", blades), EffectPopup.Style.ATTACK, PopupScale.MAX_INTENSITY),
            new EffectPopup(Lang.f("ATTAQUE x{0}", times), EffectPopup.Style.ATTACK, PopupScale.SECONDARY_INTENSITY))));
    }

    /** @return la part (%) de chaque coup qui traverse la défense, au plus 100 (version « + »). */
    public int piercePercent() { return Math.min(100, Math.round(PIERCE_PERCENT * power)); }

    @Override
    public String getDescription() {
        return Lang.f("Exécution : consomme 10% des gains. +1 Lame tous les {0} gains consommés. Attaque multipliée "
            + "par (1 + {1} x Lame). Les symboles d'attaque percent {2} % de la défense",
            GAINS_PER_BLADE, Lang.decimal(String.valueOf(FACTOR_PER_BLADE * power)), piercePercent());
    }

    /** Textes fixes : aucun, ils dépendent des gains consommés (voir {@link #onPlay}). */
    @Override
    public List<EffectPopup> getPopups() { return List.of(); }

    @Override
    public EffectSound getSound() { return EffectSound.ACE_OF_SPADES; }
}
