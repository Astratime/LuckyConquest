package fr.astratime.lucky.entities.effects;

import fr.astratime.lucky.entities.context.TurnContext;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.popups.PopupScale;

import java.util.ArrayList;
import java.util.List;

/**
 * Cartes d'archétype : bonus d'attaque, de défense et de gains du tour, pour
 * qu'un deck d'archétype se passe des Coeurs, Carreaux, Piques et Trèfles sans
 * être freiné. Chaque carte a un bonus principal et un secondaire (voir
 * cards/definitions/archetypes.json) ; une valeur à 0 ne fait rien.
 * <ul>
 *   <li>Attaque : bonus plat sur chaque symbole d'attaque, comme un Pique ;</li>
 *   <li>Défense : bouclier en plus sur chaque symbole de défense (sans renvoi). Les symboles de
 *       défense ne deviennent pas plus fréquents : c'est réservé aux Carreaux ;</li>
 *   <li>Gains : ajouté au multiplicateur de gains, comme un Trèfle.</li>
 * </ul>
 */
public class ArchetypeStatsEffect extends Effect {

    private final int   attackBonus;
    private final int   defenseBonus;
    private final float gainMultiplierAdd;

    /**
     * @param attackBonus       bonus d'attaque plat sur chaque symbole d'attaque ce tour
     * @param defenseBonus      bouclier en plus sur chaque symbole de défense ce tour
     * @param gainMultiplierAdd valeur ajoutée au multiplicateur de gains ce tour
     */
    public ArchetypeStatsEffect(int attackBonus, int defenseBonus, float gainMultiplierAdd) {
        this.attackBonus       = attackBonus;
        this.defenseBonus      = defenseBonus;
        this.gainMultiplierAdd = gainMultiplierAdd;
    }

    public int   getAttackBonus()       { return attackBonus; }
    public int   getDefenseBonus()      { return defenseBonus; }
    public float getGainMultiplierAdd() { return gainMultiplierAdd; }

    @Override
    public void apply(TurnContext context) {
        if (attackBonus > 0) context.getCombatContext().addAttackBonus(attackBonus, "Cartes");
        // Pas de symboles de défense plus fréquents : seuls les Carreaux le font (Astra).
        if (defenseBonus > 0) context.getCombatContext().addDefenseBonus(defenseBonus, "Cartes");
        if (gainMultiplierAdd > 0) context.getCombatContext().addGainMultiplier(gainMultiplierAdd);
    }

    @Override
    public String getDescription() {
        List<String> parts = new ArrayList<>();
        if (attackBonus > 0) parts.add(Lang.f("Attaque +{0} sur chaque symbole d'attaque", attackBonus));
        if (defenseBonus > 0) parts.add(Lang.f("Symboles de défense : bouclier +{0}", defenseBonus));
        if (gainMultiplierAdd > 0) parts.add(Lang.f("Multiplicateur de gains +{0}", format(gainMultiplierAdd)));
        return String.join(". ", parts);
    }

    @Override
    public List<EffectPopup> getPopups() {
        List<EffectPopup> popups = new ArrayList<>();
        if (attackBonus > 0) popups.add(EffectPopup.scaled(Lang.f("ATTAQUE +{0}", attackBonus),
            EffectPopup.Style.ATTACK, attackBonus, PopupScale.CARD_ATTACK_BONUS));
        if (defenseBonus > 0) popups.add(EffectPopup.scaled(Lang.f("BOOST DÉFENSE +{0}", defenseBonus),
            EffectPopup.Style.DEFENSE, defenseBonus, PopupScale.CARD_DEFENSE_BOOST));
        if (gainMultiplierAdd > 0) popups.add(EffectPopup.scaled(Lang.f("GAINS x+{0}", format(gainMultiplierAdd)),
            EffectPopup.Style.GAINS, Math.round(gainMultiplierAdd), PopupScale.CARD_GAIN_MULTIPLIER));
        return popups;
    }

    /** @return {@code value} sans décimale inutile, avec la virgule en français (ex : "6", "4,5"). */
    private static String format(float value) {
        if (value == (int) value) return String.valueOf((int) value);
        String text = String.valueOf(value);
        return Lang.isEnglish() ? text : text.replace('.', ',');
    }

    /** Le son du bonus le plus marqué : celui d'un Pique, d'un Carreau ou d'un Trèfle. */
    @Override
    public EffectSound getSound() {
        if (attackBonus >= 100) return EffectSound.SPADE_IGNORE_DEFENSE;
        if (defenseBonus >= 24) return EffectSound.DIAMOND_REFLECT;
        return EffectSound.CLUB_GAIN_ATTACK;
    }
}
