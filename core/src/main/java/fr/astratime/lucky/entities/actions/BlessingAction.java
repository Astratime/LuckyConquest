package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.EnemyMaluses;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.StatusEvent;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;

import java.util.ArrayList;
import java.util.List;

/**
 * Descente aux enfers (Roi démon) et Lumière du bingo (Apôtre de la Chance) :
 * un des trois effets, tiré au hasard une fois pour tout le Bingo imposé
 * (voir {@link CombatContext#getBlessing()}). Les valeurs sont celles d'un
 * symbole : le Bingo les donne trois fois, comme pour tous les rouleaux ; ce
 * qui n'est pas une valeur (renvoi, malus, absorption, cartes) ne s'additionne pas.
 */
public final class BlessingAction extends MixedAction {

    /** Descente aux enfers ({@code true}) ou Lumière du bingo. */
    private final boolean hell;

    // Descente aux enfers
    public static final int HELL_ATTACK = 1000, HELL_DRAIN_PERCENT = 50, HELL_SHIELD = 3000, HELL_REFLECT_PERCENT = 30,
        HELL_GAINS = 200;
    // Lumière du bingo
    public static final int LIGHT_ATTACK = 800, LIGHT_SHIELD = 3000, LIGHT_GAINS = 400, LIGHT_PLAYS = 8;

    private final Action first, second, third;

    public BlessingAction(boolean hell) {
        super();
        this.hell = hell;
        if (hell) {
            first  = new DrainAttackAction(HELL_ATTACK, HELL_DRAIN_PERCENT);
            second = new DefenseAction(HELL_SHIELD);
            third  = new GainAction(HELL_GAINS);
        } else {
            first  = new MixedAction(new AttackAction(LIGHT_ATTACK), new DefenseAction(LIGHT_ATTACK));
            second = new DefenseAction(LIGHT_SHIELD);
            third  = new GainAction(LIGHT_GAINS);
        }
    }

    @Override
    public List<Event> resolve(CombatContext context) {
        CombatContext.Blessing blessing = context.getBlessing();
        if (blessing == null) blessing = CombatContext.Blessing.FIRST; // sorti sans jauge (test) : le premier effet
        List<Event> events = new ArrayList<>();
        if (context.takeBlessingShown()) events.add(new StatusEvent(title(blessing), EffectPopup.Style.SPECIAL));
        switch (blessing) {
            case FIRST -> events.addAll(first.resolve(context));
            case SECOND -> {
                events.addAll(second.resolve(context));
                if (hell) context.setBlessedReflectHp(HELL_REFLECT_PERCENT);
                else context.getPlayer().absorbWithLight();
            }
            case THIRD -> {
                events.addAll(third.resolve(context));
                if (hell) {
                    context.getEnemy().getMaluses().bindToHell();
                } else {
                    context.getPlayer().getLastingEffects().addExtraPlays(LIGHT_PLAYS, 2); // le tour suivant
                    context.getEnemy().getMaluses().lightUp();
                }
            }
        }
        return events;
    }

    /** @return le texte de l'effet tiré, affiché une fois au Bingo. */
    public String title(CombatContext.Blessing blessing) {
        if (hell) {
            return switch (blessing) {
                case FIRST  -> Lang.f("DESCENTE AUX ENFERS : {0} ET VAMPIRISME", HELL_ATTACK);
                case SECOND -> Lang.f("DESCENTE AUX ENFERS : BOUCLIER {0}, RENVOI {1} %", HELL_SHIELD, HELL_REFLECT_PERCENT);
                case THIRD  -> Lang.f("DESCENTE AUX ENFERS : ENNEMI -{0} %, {1} CARTES", EnemyMaluses.HELL_DAMAGE_PERCENT,
                    EnemyMaluses.HELL_MAX_CARDS);
            };
        }
        return switch (blessing) {
            case FIRST  -> Lang.f("LUMIÈRE DU BINGO : ATTAQUE ET BOUCLIER {0}", LIGHT_ATTACK);
            case SECOND -> Lang.t("LUMIÈRE DU BINGO : LES COUPS TE SOIGNENT");
            case THIRD  -> Lang.f("LUMIÈRE DU BINGO : {0} CARTES, SES ROULEAUX S'ILLUMINENT", LIGHT_PLAYS);
        };
    }

    @Override
    public String getDescription() {
        if (hell) {
            return Lang.f("Un effet au hasard : {0} d'attaque et vampirisme ({1} % des dégâts). Ou bouclier {2}, "
                    + "et {3} % de ses PV max renvoyés s'il attaque. Ou {4} gains, et à son tour l'ennemi frappe -{5} % "
                    + "et joue {6} cartes au plus", HELL_ATTACK, HELL_DRAIN_PERCENT, HELL_SHIELD, HELL_REFLECT_PERCENT,
                HELL_GAINS, EnemyMaluses.HELL_DAMAGE_PERCENT, EnemyMaluses.HELL_MAX_CARDS);
        }
        return Lang.f("Un effet au hasard : {0} d'attaque et de bouclier. Ou bouclier {1} : ses coups absorbés te "
                + "soignent et le frappent au tour suivant. Ou {2} gains, {3} cartes au tour suivant, et chacun de ses "
                + "rouleaux a une chance sur deux de devenir Lumière (il perd {4} % de ses PV max)", LIGHT_ATTACK,
            LIGHT_SHIELD, LIGHT_GAINS, LIGHT_PLAYS, EnemyMaluses.LIGHT_HP_PERCENT);
    }
}
