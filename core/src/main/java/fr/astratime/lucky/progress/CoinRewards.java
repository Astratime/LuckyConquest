package fr.astratime.lucky.progress;

import fr.astratime.lucky.entities.BigNumber;
import fr.astratime.lucky.entities.exploration.Place;

/**
 * Les pièces gagnées en Exploration : à la fin d'un combat, selon le lieu et le rang joué.
 * Sorti de {@link PlayerProfile}, sans rien changer.
 */
public final class CoinRewards {

    private CoinRewards() { }

    /** Part des gains gardée en pièces après une défaite dans un donjon, en %. */
    public static final int DEFEAT_KEEP_PERCENT = 20;

    /**
     * Pièces versées à la fin d'un combat de l'Exploration : les gains affichés
     * dans le cadre « Gains » après la victoire contre le roi du donjon, et
     * {@link #DEFEAT_KEEP_PERCENT} % de ces gains après une défaite (contre le
     * soldat ou le roi), au plus {@link Place#getDefeatCap} (500 coûts de tirage du lieu),
     * multipliés par la part du donjon faite ({@link #dungeonProgress}).
     * Le soldat battu ne rapporte rien tout de suite : ses
     * gains sont gardés pour le roi. L'Entraînement et la Tour des épreuves ne
     * rapportent aucune pièce.
     *
     * @param victory   l'ennemi est vaincu
     * @param lastFight c'était le dernier combat du donjon (le roi)
     * @param gains     les gains du joueur à la fin du combat
     * @param place     lieu du donjon (il fixe la limite après une défaite)
     * @param progress  après une défaite, part du donjon faite, de 0 à 1 (voir {@link #dungeonProgress})
     * @return les pièces à ajouter au profil
     */
    public static long combatReward(boolean victory, boolean lastFight, double gains, Place place, double progress) {
        return combatReward(victory, lastFight, gains, place, false, progress);
    }

    /** Comme {@link #combatReward(boolean, boolean, double, Place, double)}, en Exploration difficile si {@code hard}. */
    public static long combatReward(boolean victory, boolean lastFight, double gains, Place place, boolean hard,
                                    double progress) {
        if (victory) return lastFight ? Math.max(0, BigNumber.toLong(gains)) : 0;
        long kept = Math.min(Math.max(0, BigNumber.toLong(gains * DEFEAT_KEEP_PERCENT / 100.0)), place.getDefeatCap(hard));
        return Math.round(kept * Math.max(0, Math.min(1, progress)));
    }

    /** Pièces dans le lieu juste en dessous de celui du rang joué, en % (Astra, 2026-11-15). */
    public static final int LOWER_PLACE_PERCENT = 25;

    /** Pièces deux lieux ou plus en dessous de celui du rang joué, en %. */
    public static final int FAR_LOWER_PLACE_PERCENT = 5;

    /**
     * @return le lieu d'Exploration fait pour {@code rank} : la Prairie sans rang, puis un lieu pour chaque paire
     *         de rangs (Avare et Parieur à la Foire … Roi du casino et Légende au Désert)
     */
    public static Place placeOf(Rank rank) {
        return Place.atTier(tierOf(rank));
    }

    /**
     * @return le palier de lieu du rang {@code rank} (Astra, 2026-11-20 : 10 lieux) : 1 sans rang, 2 pour l'Avare et
     *         le Parieur, puis un palier par rang (3 le Flambeur … 10 la Légende). Tant qu'un palier n'a pas encore son
     *         lieu, le rang va au lieu suivant (voir {@link Place#atTier(int)}).
     */
    public static int tierOf(Rank rank) {
        return rank == null ? 1 : Math.max(2, rank.ordinal() + 1);
    }

    /**
     * Part des pièces gagnée dans {@code place} avec le rang {@code rank} (Astra, 2026-11-15) : toutes dans le lieu du
     * rang ou plus haut, {@link #LOWER_PLACE_PERCENT} % un lieu en dessous, {@link #FAR_LOWER_PLACE_PERCENT} % deux
     * lieux ou plus en dessous. Le mode difficile est toujours au-dessus des rangs : tout y est gagné.
     *
     * @return la part, en % (100 : rien n'est retenu)
     */
    public static int coinsPercent(Rank rank, Place place, boolean hard) {
        if (hard || place == null) return 100;
        int gap = placeOf(rank).ordinal() - place.ordinal();
        return gap <= 0 ? 100 : gap == 1 ? LOWER_PLACE_PERCENT : FAR_LOWER_PLACE_PERCENT;
    }

    /** @return {@code coins} réduites à {@code percent} % (arrondi). */
    public static long share(long coins, int percent) {
        return percent >= 100 ? coins : Math.round(coins * (percent / 100.0));
    }

    /**
     * Part d'un donjon faite au moment d'une défaite : la moitié pour le soldat,
     * la moitié pour le roi, chacun au prorata des PV qu'il a perdus. Mourir au
     * premier tour contre le soldat vaut presque 0, perdre contre un roi à moitié
     * mort vaut 0,75.
     *
     * @param bossStage le combat perdu était celui du roi (le soldat est donc battu)
     * @param hpRatio   PV restants de l'ennemi qui a gagné, de 0 à 1
     */
    public static double dungeonProgress(boolean bossStage, double hpRatio) {
        double done = 1 - Math.max(0, Math.min(1, hpRatio));
        return (bossStage ? 0.5 : 0) + 0.5 * done;
    }
}
