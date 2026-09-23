package fr.astratime.lucky.screens;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import fr.astratime.lucky.entities.Player;

/**
 * Le deck (à gauche) et la défausse (à droite, son symétrique) : piles de dos
 * de cartes avec leur compteur. Au survol, une infobulle invite à cliquer ; au
 * clic, le contenu de la pile s'affiche (voir {@link PileContentOverlay}).
 *
 * Les cartes animées en route vers la défausse sont déjà comptées dans le
 * modèle : la défausse affichée ne les compte qu'à leur arrivée
 * ({@link #addInFlight(int)} puis {@link #onCardLanded()}).
 */
class PilesView {

    private final Stage        stage;
    private final CardPileView deckPile;
    private final CardPileView discardPile;
    private       int          discardInFlight = 0;

    /**
     * @param onDeckClicked    appelé au clic sur le deck
     * @param onDiscardClicked appelé au clic sur la défausse
     */
    PilesView(Stage stage, Texture cardBackTexture, BitmapFont font, Tooltip tooltip, Sound clickSound,
              float cardWidth, float cardHeight, Runnable onDeckClicked, Runnable onDiscardClicked) {
        this.stage  = stage;
        deckPile    = buildPile("Deck", cardBackTexture, font, tooltip, clickSound, cardWidth, cardHeight, onDeckClicked);
        discardPile = buildPile("Defausse", cardBackTexture, font, tooltip, clickSound, cardWidth, cardHeight, onDiscardClicked);
    }

    /** Ajoute les deux piles au Stage. */
    void addTo(Stage target) {
        target.addActor(deckPile);
        target.addActor(discardPile);
    }

    /**
     * Place le deck en {@code (deckX, y)} et la défausse en symétrique par rapport
     * au centre de l'écran (ancrée à droite).
     */
    void layout(float deckX, float y) {
        deckPile.setPosition(deckX, y);
        discardPile.setPosition(stage.getViewport().getWorldWidth() - deckX - deckPile.getWidth(), y);
    }

    /** Met à jour les compteurs du deck et de la défausse (sans les cartes encore en vol vers celle-ci). */
    void refresh(Player player) {
        deckPile.setCount(player.getDeck().getCards().size());
        discardPile.setCount(Math.max(0, player.getDiscardPile().size() - discardInFlight));
    }

    /** {@code count} cartes partent en animation vers la défausse : elles n'y seront affichées qu'à leur arrivée. */
    void addInFlight(int count) { discardInFlight += count; }

    /** Une carte animée vient d'arriver sur la défausse : elle y est désormais comptée. */
    void onCardLanded() { discardInFlight = Math.max(0, discardInFlight - 1); }

    /** Oublie les cartes en vol (animations annulées, ex : nouveau combat). */
    void resetInFlight() { discardInFlight = 0; }

    /** @return la pile du deck (point de départ des cartes distribuées). */
    CardPileView deck()    { return deckPile; }
    /** @return la pile de la défausse (point d'arrivée des cartes défaussées). */
    CardPileView discard() { return discardPile; }

    private static CardPileView buildPile(String name, Texture cardBackTexture, BitmapFont font, Tooltip tooltip,
                                          Sound clickSound, float cardWidth, float cardHeight, Runnable onClick) {
        CardPileView pile = new CardPileView(name, cardBackTexture, font, cardWidth, cardHeight);
        pile.addListener(new InputListener() {

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer != -1 || (fromActor != null && fromActor.isDescendantOf(pile))) return;
                tooltip.show("Cliquer pour voir les cartes", pile.getX(), pile.getLabelTopY() + 5f);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer != -1 || (toActor != null && toActor.isDescendantOf(pile))) return;
                tooltip.hide();
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                clickSound.play();
                onClick.run();
                return true;
            }
        });
        return pile;
    }
}
