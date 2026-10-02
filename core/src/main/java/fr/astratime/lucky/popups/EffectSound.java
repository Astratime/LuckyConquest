package fr.astratime.lucky.popups;

import java.util.Locale;

/**
 * Bruitage propre à chaque effet de carte, joué quand apparaît le premier texte
 * de l'effet (voir {@link EffectPopup#getSound()}). Reste indépendant de libGDX :
 * GameSounds charge le fichier {@link #getAssetPath()}.
 *
 * Les sons sont générés par tools/sounds/generate_sounds.py (effects/&lt;nom&gt;).
 */
public enum EffectSound {

    ATTACK,
    DEFENSE,
    GAIN,
    MULTIPLIER,
    EXTRA_DRAW,
    BOOST_SYMBOL,
    CLUB_GAIN_ATTACK,
    HEART_DRAIN,
    DIAMOND_REFLECT,
    ACE_OF_CLUBS,
    ACE_OF_DIAMONDS,
    ACE_OF_HEARTS,
    ACE_OF_SPADES,
    BET,
    BET_ON_SYMBOL,
    BINGO,
    CORRUPTION,
    EXTRA_PLAYS,
    GAINS_MULTIPLIER,
    LUCKY_CHARM,
    MAGNET,
    PISTOL,
    RAINBOW,
    RECYCLE,
    RUSSIAN_ROULETTE,
    SPADE_IGNORE_DEFENSE;

    /** @return le chemin interne du fichier son (ex : "sounds/effects/attack.ogg"). */
    public String getAssetPath() {
        return "sounds/effects/" + name().toLowerCase(Locale.ROOT) + ".ogg";
    }
}
