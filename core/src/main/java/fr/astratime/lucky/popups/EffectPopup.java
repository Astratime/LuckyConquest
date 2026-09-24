package fr.astratime.lucky.popups;

/**
 * Texte affiché brièvement à l'écran pour résumer un bonus ou un résultat :
 * effet d'une carte jouée (ex : "GAINS x+20") ou conséquence d'un symbole tiré
 * (ex : "DÉGÂTS 45"). DAMAGE sert à la vie perdue par le joueur.
 *
 * Reste indépendant de libGDX : le style indique la famille d'effet (EffectPopupAnimator
 * en déduit la couleur), l'intensité (entre 0 et 1) la force du bonus (EffectPopupAnimator
 * en déduit la taille du texte, bornée entre une taille minimale et maximale).
 */
public class EffectPopup {

    /** Famille d'effet, qui détermine la couleur du texte. */
    public enum Style { GAINS, ATTACK, DEFENSE, DRAIN, REFLECT, DRAW, SPECIAL, DAMAGE }

    private final String text;
    private final Style  style;
    private final float  intensity;

    /**
     * @param text      texte affiché (en majuscules de préférence)
     * @param style     famille d'effet
     * @param intensity force du bonus, ramenée entre 0 (petit texte) et 1 (grand texte)
     */
    public EffectPopup(String text, Style style, float intensity) {
        this.text      = text;
        this.style     = style;
        this.intensity = Math.clamp(intensity, 0f, 1f);
    }

    /**
     * Construit un popup dont l'intensité est {@code value / maxValue} : la valeur
     * de référence {@code maxValue} (et au-delà) donne la taille de texte maximale.
     */
    public static EffectPopup scaled(String text, Style style, float value, float maxValue) {
        return new EffectPopup(text, style, value / maxValue);
    }

    /** @return le texte affiché. */
    public String getText()      { return text; }
    /** @return la famille d'effet (couleur). */
    public Style  getStyle()     { return style; }
    /** @return la force du bonus, entre 0 et 1 (taille du texte). */
    public float  getIntensity() { return intensity; }
}
