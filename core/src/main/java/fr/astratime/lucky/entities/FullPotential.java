package fr.astratime.lucky.entities;

import java.util.Optional;

import fr.astratime.lucky.entities.effects.AceOfClubsEffect;
import fr.astratime.lucky.entities.effects.AceOfDiamondsEffect;
import fr.astratime.lucky.entities.effects.AceOfHeartsEffect;
import fr.astratime.lucky.entities.effects.AceOfSpadesEffect;
import fr.astratime.lucky.entities.effects.Effect;

/**
 * Un As joué à plein potentiel (Astra 2026-11-14) : une petite animation le
 * salue à l'écran.
 * <ul>
 *   <li>As de Coeur : le Sang est au maximum ({@link LastingEffects#MAX_BLOOD}) ;</li>
 *   <li>As de Pique : {@link #FULL_BLADES} Lames ou plus à encaisser ;</li>
 *   <li>As de Trèfle (et les trois Étoiles) : la Bonne étoile garantit le Bingo ;</li>
 *   <li>As de Carreau : le Coffre est plein ({@link LastingEffects#MAX_VAULT}).</li>
 * </ul>
 */
public enum FullPotential {
    HEARTS, SPADES, CLUBS, DIAMONDS;

    /** Lames qui font un As de Pique à plein potentiel : le plus qu'il achète d'un coup avec les gains. */
    public static final int FULL_BLADES = 10;

    /**
     * Jauges juste avant la pose de la carte (le Sang, le Coffre et la Bonne
     * étoile sont lus avant ; les Lames après, l'As de Pique en achète à la pose).
     */
    public record Gauges(int bloodBefore, int vaultBefore, int starsBefore, int bladesAfter) {
        /** @return les jauges de {@code lasting} avant la pose, les Lames n'étant pas encore achetées. */
        public static Gauges before(LastingEffects lasting) {
            // Sang : compté plein (MAX_BLOOD) dès que la jauge du joueur est pleine, quel que soit son maximum
            int blood = lasting.getBlood() >= lasting.getMaxBlood() ? LastingEffects.MAX_BLOOD
                : Math.min(lasting.getBlood(), LastingEffects.MAX_BLOOD - 1);
            return new Gauges(blood, lasting.getVault(), lasting.getLuckyStar(), lasting.getBlades());
        }

        /** @return ces jauges, avec les Lames lues après la pose. */
        public Gauges withBladesAfter(int blades) { return new Gauges(bloodBefore, vaultBefore, starsBefore, blades); }
    }

    /** @return le plein potentiel atteint par {@code card} avec ces jauges, ou rien. */
    public static Optional<FullPotential> of(Card card, Gauges gauges) {
        for (Effect effect : card.getEffects()) {
            switch (effect) {
                case AceOfHeartsEffect hearts when gauges.bloodBefore() >= LastingEffects.MAX_BLOOD ->
                    { return Optional.of(HEARTS); }
                case AceOfSpadesEffect spades when gauges.bladesAfter() >= FULL_BLADES ->
                    { return Optional.of(SPADES); }
                case AceOfDiamondsEffect diamonds when gauges.vaultBefore() >= LastingEffects.MAX_VAULT ->
                    { return Optional.of(DIAMONDS); }
                case AceOfClubsEffect clubs when clubs.chance(Math.min(LastingEffects.MAX_LUCKY_STAR,
                        gauges.starsBefore() + AceOfClubsEffect.OWN_STARS)) >= 1f ->
                    { return Optional.of(CLUBS); }
                default -> { }
            }
        }
        return Optional.empty();
    }
}
