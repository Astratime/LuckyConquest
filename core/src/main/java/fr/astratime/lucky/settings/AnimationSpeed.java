package fr.astratime.lucky.settings;

import fr.astratime.lucky.i18n.Lang;

/**
 * Vitesse des animations du combat (réglage des Options) : les tirages, les
 * cartes, les Bingos et le tour de l'ennemi passent 1,5 ou 2 fois plus vite.
 * Les cinématiques accompagnées de leur musique restent à leur vitesse.
 */
public enum AnimationSpeed {
    X1(1f, "1"),
    X1_5(1.5f, "1.5"),
    X2(2f, "2");

    private final float  factor;
    private final String number;

    AnimationSpeed(float factor, String number) {
        this.factor = factor;
        this.number = number;
    }

    /** @return le facteur appliqué au temps des animations (1 : vitesse normale). */
    public float factor() { return factor; }

    /** @return le texte du réglage, ex : « Animations : x1,5 » (« Animations: x1.5 » en anglais). */
    public String label() { return Lang.f("Animations : x{0}", Lang.decimal(number)); }

    /** @return la vitesse suivante ({@code direction} 1) ou précédente (-1), en boucle. */
    public AnimationSpeed step(int direction) {
        AnimationSpeed[] all = values();
        return all[Math.floorMod(ordinal() + Integer.signum(direction), all.length)];
    }

    /** @return la vitesse enregistrée sous {@code name}, ou {@link #X1} si elle est inconnue. */
    public static AnimationSpeed parse(String name) {
        for (AnimationSpeed speed : values()) if (speed.name().equals(name)) return speed;
        return X1;
    }
}
