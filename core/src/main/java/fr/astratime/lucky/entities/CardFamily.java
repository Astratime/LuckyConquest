package fr.astratime.lucky.entities;

import fr.astratime.lucky.entities.effects.AceOfClubsEffect;
import fr.astratime.lucky.entities.effects.AceOfSpadesEffect;
import fr.astratime.lucky.entities.effects.AttackEffect;
import fr.astratime.lucky.entities.effects.BetEffect;
import fr.astratime.lucky.entities.effects.BladesAttackEffect;
import fr.astratime.lucky.entities.effects.ClubGainAttackEffect;
import fr.astratime.lucky.entities.effects.Effect;
import fr.astratime.lucky.entities.effects.ExtraDrawEffect;
import fr.astratime.lucky.entities.effects.FortuneEffect;
import fr.astratime.lucky.entities.effects.FourLeafCloverEffect;
import fr.astratime.lucky.entities.effects.GainEffect;
import fr.astratime.lucky.entities.effects.GainsMultiplierEffect;
import fr.astratime.lucky.entities.effects.GoldVeinEffect;
import fr.astratime.lucky.entities.effects.GuillotineEffect;
import fr.astratime.lucky.entities.effects.RussianRouletteEffect;
import fr.astratime.lucky.entities.effects.ShadowDaggerEffect;
import fr.astratime.lucky.entities.effects.SpadeIgnoreDefenseEffect;
import fr.astratime.lucky.entities.effects.TridentEffect;
import fr.astratime.lucky.i18n.Lang;

import java.util.List;

/**
 * Familles de cartes, que le Portier (chapitre 5) peut laisser dehors tout un
 * combat : une carte en fait partie si un de ses effets est de cette famille.
 */
public enum CardFamily {

    /** Les cartes qui piochent (Pioche +2, Pioche du mineur...). */
    PIOCHE("Pioche", List.of(ExtraDrawEffect.class)),
    /** Les cartes qui rapportent des gains : les Trèfles, Gains +500, Fortune... */
    GAINS("Gains", List.of(ClubGainAttackEffect.class, AceOfClubsEffect.class, GainEffect.class,
        GainsMultiplierEffect.class, FortuneEffect.class, FourLeafCloverEffect.class, GoldVeinEffect.class,
        BetEffect.class)),
    /** Les cartes qui attaquent : les Piques, la Guillotine, le Trident, la Roulette russe... */
    ATTAQUE("Attaque", List.of(SpadeIgnoreDefenseEffect.class, AceOfSpadesEffect.class, AttackEffect.class,
        BladesAttackEffect.class, GuillotineEffect.class, ShadowDaggerEffect.class, TridentEffect.class,
        RussianRouletteEffect.class));

    private final String                              displayName;
    private final List<Class<? extends Effect>>       effects;

    CardFamily(String displayName, List<Class<? extends Effect>> effects) {
        this.displayName = displayName;
        this.effects     = effects;
    }

    /** @return le nom de la famille (ex : "Gains"). */
    public String getDisplayName() { return Lang.t(displayName); }

    /** @return {@code true} si {@code card} fait partie de cette famille. */
    public boolean contains(Card card) {
        return card.getEffects().stream().anyMatch(effect -> effects.stream().anyMatch(type -> type.isInstance(effect)));
    }
}
