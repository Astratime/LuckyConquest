package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

/**
 * Tous les bruitages de GameScreen (CC0, Kenney.nl / The Motion Monkey — voir
 * assets/sounds/CREDITS.txt), chargés et libérés ensemble.
 */
public class GameSounds implements Disposable {

    public final Sound buttonClick;
    public final Sound spinButton;
    public final Sound cardDeal;
    public final Sound cardFlip;
    public final Sound cardClick;
    /** Résultat d'un tirage de symboles : aucune paire, une paire, ou les trois identiques. */
    public final Sound oneSymbol;
    public final Sound twoSymbols;
    public final Sound bingoThreeSymbols;

    public GameSounds() {
        buttonClick       = load("sounds/button-click.ogg");
        spinButton        = load("sounds/spin_machine.mp3");
        cardDeal          = load("sounds/card-deal.ogg");
        cardFlip          = load("sounds/card-flip.ogg");
        cardClick         = load("sounds/card-click.ogg");
        oneSymbol         = load("sounds/1_symbol.wav");
        twoSymbols        = load("sounds/2_symbols.wav");
        bingoThreeSymbols = load("sounds/bingo_3_symbols.wav");
    }

    private static Sound load(String path) {
        return Gdx.audio.newSound(Gdx.files.internal(path));
    }

    @Override
    public void dispose() {
        buttonClick.dispose();
        spinButton.dispose();
        cardDeal.dispose();
        cardFlip.dispose();
        cardClick.dispose();
        oneSymbol.dispose();
        twoSymbols.dispose();
        bingoThreeSymbols.dispose();
    }
}
