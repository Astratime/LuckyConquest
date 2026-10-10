package fr.astratime.lucky.entities;

/**
 * Les grands nombres du jeu (gains, attaque) : un {@code double}, c'est-à-dire
 * une mantisse et une puissance, comme dans Tap Titans. Il va jusqu'à 10^308
 * et reste exact jusqu'à 9 millions de milliards ; au-delà, seuls les premiers
 * chiffres comptent, et ce sont les seuls affichés (voir
 * {@link fr.astratime.lucky.i18n.Lang#big(double)}).
 */
public final class BigNumber {

    /** Le plus grand nombre gardé : on ne passe jamais à l'infini. */
    public static final double MAX = Double.MAX_VALUE;

    private BigNumber() { }

    /** @return {@code a + b}, sans passer à l'infini */
    public static double add(double a, double b) { return cap(a + b); }

    /** @return {@code value} arrondi à l'unité, sans passer à l'infini */
    public static double round(double value) { return cap(Math.rint(value)); }

    /** @return {@code percent} % de {@code value}, arrondi à l'unité */
    public static double percent(double value, double percent) { return round(value * percent / 100d); }

    /** @return {@code value} ramené entre -{@link #MAX} et {@link #MAX} (0 si ce n'est pas un nombre) */
    public static double cap(double value) {
        if (Double.isNaN(value)) return 0;
        return Math.max(-MAX, Math.min(MAX, value));
    }

    /** @return {@code value} en {@code long}, plafonné (pour les pièces et les statistiques) */
    public static long toLong(double value) {
        if (value >= Long.MAX_VALUE) return Long.MAX_VALUE;
        if (value <= Long.MIN_VALUE) return Long.MIN_VALUE;
        return Math.round(value);
    }

    /** @return {@code value} en {@code int}, plafonné (pour les PV, la défense et les jauges) */
    public static int toInt(double value) {
        if (value >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (value <= Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) Math.round(value);
    }
}
