package fr.astratime.lucky.entities.actions;

import fr.astratime.lucky.entities.EnemyMaluses;
import fr.astratime.lucky.entities.ReelGauges;
import fr.astratime.lucky.entities.context.CombatContext;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.StatusEvent;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;

import java.util.List;
import java.util.Random;

/**
 * Gong (rouleau du Temple du Destin) : une attaque qui résonne ; une fois sur quatre
 * ({@link ReelGauges#GONG_STUN_PERCENT} %), ou à coup sûr avec le Maillet de gong, un rouleau de
 * l'ennemi est étourdi et ne tourne pas à son prochain tirage (un seul par tirage, voir
 * {@link EnemyMaluses#stun()}).
 */
public final class GongAction extends AttackAction {

    private final Random random;

    public GongAction(int baseDamage) { this(baseDamage, new Random()); }

    /** @param random source d'aléatoire (ex : graine fixe pour des tests reproductibles) */
    public GongAction(int baseDamage, Random random) {
        super(baseDamage);
        this.random = random;
    }

    @Override
    protected void afterHit(CombatContext context, long damage, List<Event> events) {
        EnemyMaluses maluses = context.getEnemy().getMaluses();
        if (context.getEnemy().isDefeated() || maluses.isStunned()) return;
        boolean sure = context.getPlayer().getLastingEffects().getReelGauges().isGongSure();
        if (sure || random.nextInt(100) < ReelGauges.GONG_STUN_PERCENT) {
            maluses.stun();
            events.add(new StatusEvent(Lang.t("GONG : UN ROULEAU ÉTOURDI"), EffectPopup.Style.SPECIAL));
        }
    }

    @Override
    public String getDescription() {
        return Lang.f("{0} dégâts de base. {1} % de chances d'étourdir un rouleau de l'ennemi : il ne tourne pas à son "
            + "prochain tirage", getBaseDamage(), ReelGauges.GONG_STUN_PERCENT);
    }
}
