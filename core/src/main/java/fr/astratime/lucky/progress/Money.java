package fr.astratime.lucky.progress;

import fr.astratime.lucky.i18n.Lang;

/**
 * Les deux monnaies de la boutique : les pièces et les Lingots (1 Lingot = 1 milliard de pièces),
 * et leur écriture à l'écran. Sorti de {@link PlayerProfile}, sans rien changer.
 */
public final class Money {

    private Money() { }

    /** Valeur d'un Lingot, en pièces. */
    public static final long INGOT_VALUE = 1_000_000_000L;

    /**
     * Un prix d'au moins ce montant (en pièces) s'affiche et se paie en Lingots. À 10 milliards, tous les prix
     * concernés tombent sur un nombre rond de milliards : aucun prix ne change. En dessous, on paie en pièces.
     */
    public static final long INGOT_THRESHOLD = 10 * INGOT_VALUE;

    /** @return {@code true} si le prix {@code price} (en pièces) se paie en Lingots. */
    public static boolean inIngots(long price) { return price >= INGOT_THRESHOLD; }

    /** @return le prix {@code price} (en pièces) en Lingots, arrondi au Lingot supérieur. */
    public static long ingotsOf(long price) { return price <= 0 ? 0 : (price - 1) / INGOT_VALUE + 1; }

    /** @return ce que coûte vraiment {@code price}, en pièces (un prix en Lingots compte des Lingots entiers). */
    static long cost(long price) {
        if (!inIngots(price)) return price;
        long n = ingotsOf(price);
        return n > Long.MAX_VALUE / INGOT_VALUE ? Long.MAX_VALUE : n * INGOT_VALUE;
    }

    /** @return {@code price} (en pièces) écrit dans sa monnaie : « 38 Lingots » ou « 1 300 000 000 pièces ». */
    public static String formatPrice(long price) {
        if (inIngots(price)) {
            long n = ingotsOf(price);
            return Lang.n(n, "{0} Lingot", "{0} Lingots", formatCoins(n));
        }
        return Lang.n(price, "{0} pièce", "{0} pièces", formatCoins(price));
    }

    /** @return {@code price} pour une case de la boutique : « 38 Lingots » ou juste « 1 300 000 000 ». */
    public static String priceTag(long price) {
        return inIngots(price) ? formatPrice(price) : formatCoins(price);
    }

    /** @return {@code amount} écrit avec des espaces entre les milliers (ex : "10 000"). */
    public static String formatCoins(long amount) {
        String digits = String.valueOf(Math.abs(amount));
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && (digits.length() - i) % 3 == 0) text.append(Lang.thousands());
            text.append(digits.charAt(i));
        }
        return (amount < 0 ? "-" : "") + text;
    }

    static long parseCoins(String text) {
        if (text == null) return 0L;
        try {
            return Math.max(0L, Long.parseLong(text.trim()));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
