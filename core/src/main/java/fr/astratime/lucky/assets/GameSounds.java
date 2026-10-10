package fr.astratime.lucky.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.popups.EffectSound;
import fr.astratime.lucky.settings.AudioSettings;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tous les bruitages de GameScreen, chargés et libérés ensemble. Leur volume suit
 * le réglage « Sons » ({@link VolumeSound}). Les sons de cartes distribuées et de
 * bouton viennent de Kenney.nl (CC0), le Bingo classique est celui d'origine du jeu ;
 * tous les autres sont synthétisés par tools/sounds/generate_sounds.py (voir
 * assets/sounds/CREDITS.txt).
 */
public class GameSounds implements Disposable {

    /** Clic de bouton (validation). */
    public final Sound buttonClick;
    public final Sound cardDeal;
    public final Sound cardFlip;

    /** Survol d'une option d'un menu (pause). */
    public final Sound menuHover;
    /** Survol d'une carte de la main. */
    public final Sound cardHover;
    /** Survol du deck ou de la défausse. */
    public final Sound pileHover;
    /** Survol de l'échoppe. */
    public final Sound shopHover;
    /** Clic droit sur une carte : sa fiche s'ouvre. */
    public final Sound cardInspect;
    /** Clic gauche sur une carte : elle est jouée. */
    public final Sound cardPlay;

    /** Carte achetée à l'échoppe. */
    public final Sound purchase;
    /** Une combinaison vient d'être formée par les cartes jouées. */
    public final Sound comboFormed;
    /** Gains crédités, ou perdus. */
    public final Sound coinsGain;
    public final Sound coinsLoss;

    /** Bouton « Lancer machine ». */
    public final Sound spinButton;
    /** Rouleaux qui tournent : à jouer en boucle (une seconde qui se raccorde sans blanc). */
    public final Sound reelSpin;
    /** Un rouleau s'arrête sur son symbole. */
    public final Sound reelStop;
    /** Le dernier rouleau ralentit pour faire durer le suspense (dure jusqu'à son arrêt). */
    public final Sound reelSuspense;
    /** Résultat d'un tirage : aucune paire, ou une paire (le jackpot a son propre son, voir {@link #bingo}). */
    public final Sound resultNone;
    public final Sound resultPair;
    /** Les trois symboles alignés : le son de Bingo d'origine, joué avec celui de la scène du symbole ({@link #bingo}). */
    public final Sound bingoClassic;

    /** Coup encaissé par le joueur, ou par l'ennemi. */
    public final Sound playerHurt;
    public final Sound enemyHurt;
    /** Bouclier gagné (le joueur, ou l'ennemi joué un ton plus bas). */
    public final Sound shieldGain;
    /** Coup absorbé par un bouclier, et bouclier brisé (usé jusqu'au bout, ou percé par une attaque qui l'ignore). */
    public final Sound shieldBlock;
    public final Sound shieldBreak;

    /** Gerbe et pluie de confettis, départ et explosion d'une fusée de feu d'artifice. */
    public final Sound confettiPop;
    public final Sound confettiRain;
    public final Sound fireworkLaunch;
    public final Sound fireworkBurst;

    public final Sound victory;
    public final Sound defeat;

    private final Map<EffectSound, Sound> effects = new EnumMap<>(EffectSound.class);
    private final Map<Symbol, Sound>      bingos  = new EnumMap<>(Symbol.class);
    private final List<Sound>             all     = new ArrayList<>();
    private final Map<String, Sound>      cutscenes = new HashMap<>();
    private final AudioSettings           audio;

    public GameSounds(AudioSettings audio) {
        this.audio   = audio;
        buttonClick  = load("sounds/button-click.ogg");
        cardDeal     = load("sounds/card-deal.ogg");
        cardFlip     = load("sounds/card-flip.ogg");

        menuHover    = load("sounds/ui/menu_hover.ogg");
        cardHover    = load("sounds/ui/card_hover.ogg");
        pileHover    = load("sounds/ui/pile_hover.ogg");
        shopHover    = load("sounds/ui/shop_hover.ogg");
        cardInspect  = load("sounds/ui/card_inspect.ogg");
        cardPlay     = load("sounds/ui/card_play.ogg");
        purchase     = load("sounds/shop/purchase.ogg");
        comboFormed  = load("sounds/combo/formed.ogg");
        coinsGain    = load("sounds/coins/gain.ogg");
        coinsLoss    = load("sounds/coins/loss.ogg");

        spinButton   = load("sounds/slots/spin_button.ogg");
        reelSpin     = load("sounds/slots/reel_spin.wav");
        reelStop     = load("sounds/slots/reel_stop.ogg");
        reelSuspense = load("sounds/slots/reel_suspense.ogg");
        resultNone   = load("sounds/slots/result_none.ogg");
        resultPair   = load("sounds/slots/result_pair.ogg");
        bingoClassic = load("sounds/bingo_3_symbols.wav");

        playerHurt   = load("sounds/combat/player_hurt.ogg");
        enemyHurt    = load("sounds/combat/enemy_hurt.ogg");
        shieldGain   = load("sounds/combat/shield_gain.ogg");
        shieldBlock  = load("sounds/combat/shield_block.ogg");
        shieldBreak  = load("sounds/combat/shield_break.ogg");

        confettiPop    = load("sounds/party/confetti_pop.ogg");
        confettiRain   = load("sounds/party/confetti_rain.ogg");
        fireworkLaunch = load("sounds/party/firework_launch.ogg");
        fireworkBurst  = load("sounds/party/firework_burst.ogg");

        victory      = load("sounds/combat/victory.ogg");
        defeat       = load("sounds/combat/defeat.ogg");

        for (EffectSound effect : EffectSound.values()) effects.put(effect, load(effect.getAssetPath()));
        Map<String, Sound> byName = new HashMap<>(); // Triple Sept et Joker partagent le même son
        for (Symbol symbol : Symbol.values()) {
            bingos.put(symbol, byName.computeIfAbsent(bingoName(symbol), name -> load("sounds/bingo/" + name + ".ogg")));
        }
    }

    /** @return le bruitage de l'effet de carte {@code effect}. */
    public Sound effect(EffectSound effect) { return effects.get(effect); }

    /** @return le bruitage du Bingo de {@code symbol}, accordé à sa célébration (voir JackpotCelebration). */
    public Sound bingo(Symbol symbol) { return bingos.get(symbol); }

    /** @return le nom du son de Bingo de {@code symbol} : un par mise en scène. */
    private static String bingoName(Symbol symbol) {
        return switch (symbol) {
            case SEVEN               -> "seven";
            case DOUBLE_BAR          -> "double_bar";
            case BAR                 -> "bar";
            case CHERRY              -> "cherry";
            case TRIPLE_CHERRY       -> "triple_cherry";
            case GRAPE               -> "grape";
            case BELL                -> "bell";
            case DIAMOND             -> "diamond";
            case GOLD_BAR            -> "gold_bar";
            case WATERMELON          -> "watermelon";
            case HORSESHOE           -> "bell";
            case CROWN, NUGGET       -> "gold_bar";
            case ECU                 -> "diamond";
            case HEART               -> "cherry";
            case SWORD, DIE, STAR    -> "seven";
            case BOMB                -> "triple_cherry";
            case TRIPLE_SEVEN, JOKER -> "casino";
        };
    }

    /**
     * @return le son de la cinématique {@code name} (sounds/cutscene/{@code name}.ogg),
     * chargé la première fois qu'il sert, puis gardé.
     */
    public Sound cutscene(String name) {
        return cutscenes.computeIfAbsent(name, key -> load("sounds/cutscene/" + key + ".ogg"));
    }

    /**
     * @return la boucle de la cinématique {@code name} (sounds/cutscene/{@code name}.wav, sans blanc au raccord),
     * chargée la première fois qu'elle sert, puis gardée.
     */
    public Sound cutsceneLoop(String name) {
        return cutscenes.computeIfAbsent(name, key -> load("sounds/cutscene/" + key + ".wav"));
    }

    private Sound load(String path) {
        Sound sound = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(path)), audio);
        all.add(sound);
        return sound;
    }

    @Override
    public void dispose() {
        all.forEach(Sound::dispose);
        all.clear();
    }
}
