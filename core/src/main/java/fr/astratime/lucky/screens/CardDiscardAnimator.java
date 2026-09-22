package fr.astratime.lucky.screens;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Anime le départ de cartes vers la défausse :
 *  - cartes de la table : elles se retournent (dos visible, même flip que
 *    {@link CardDealAnimator}) puis glissent jusqu'à la défausse ;
 *  - cartes piochées en surplus (main pleine) : elles partent dos visible du
 *    deck et glissent directement jusqu'à la défausse.
 *
 * Ne possède pas cardBackTexture/dealSound/flipSound : ces ressources restent
 * possédées (et disposées) par l'appelant.
 */
public class CardDiscardAnimator {

    private static final float STAGGER_DELAY      = 0.08f;
    private static final float FLIP_HALF_DURATION = 0.12f;
    private static final float FLIP_PAUSE_DELAY   = 0.1f;
    private static final float MOVE_DURATION      = 0.35f;

    private final Stage   stage;
    private final Texture cardBackTexture;
    private final float   cardWidth;
    private final float   cardHeight;
    private final Sound   dealSound;
    private final Sound   flipSound;

    /** Cartes actuellement en vol vers la défausse, à nettoyer en cas d'annulation. */
    private final List<Image> flyingCards = new ArrayList<>();

    public CardDiscardAnimator(Stage stage, Texture cardBackTexture, float cardWidth, float cardHeight,
                               Sound dealSound, Sound flipSound) {
        this.stage           = stage;
        this.cardBackTexture = cardBackTexture;
        this.cardWidth       = cardWidth;
        this.cardHeight      = cardHeight;
        this.dealSound       = dealSound;
        this.flipSound       = flipSound;
    }

    /**
     * Retourne chaque carte de {@code cards} (face → dos) sur place, puis la fait
     * glisser jusqu'à {@code (toX, toY)} (coordonnées du Stage) où elle disparaît.
     * Les images sont détachées de leur parent et ne sont plus cliquables.
     *
     * @param onLand appelé à l'arrivée de chaque carte (ex : incrémenter le compteur de la défausse)
     */
    public void discardFromTable(List<Image> cards, float toX, float toY, Runnable onLand) {
        for (int i = 0; i < cards.size(); i++) {
            Image card = cards.get(i);
            Vector2 pos = card.localToStageCoordinates(new Vector2(0f, 0f));
            card.clearActions();
            card.clearListeners();
            card.setTouchable(Touchable.disabled);
            card.setVisible(true);
            card.setSize(cardWidth, cardHeight);
            card.setOrigin(cardWidth / 2f, cardHeight / 2f);
            card.setScale(1f);
            stage.addActor(card); // détache la carte de la main, garde sa position à l'écran
            card.setPosition(pos.x, pos.y);
            flyingCards.add(card);

            card.addAction(Actions.sequence(
                Actions.delay(i * STAGGER_DELAY),
                Actions.run(flipSound::play),
                Actions.scaleTo(0f, 1f, FLIP_HALF_DURATION),
                Actions.run(() -> card.setDrawable(backDrawable())),
                Actions.scaleTo(1f, 1f, FLIP_HALF_DURATION),
                Actions.delay(FLIP_PAUSE_DELAY),
                Actions.run(dealSound::play),
                Actions.moveTo(toX, toY, MOVE_DURATION, Interpolation.pow2In),
                Actions.run(() -> land(card, onLand))
            ));
        }
    }

    /**
     * Fait partir {@code count} cartes dos visible de {@code (fromX, fromY)} (le deck)
     * jusqu'à {@code (toX, toY)} (la défausse) : cartes piochées sans place dans la main.
     *
     * @param initialDelay délai avant la première carte (ex : laisser la distribution se faire d'abord)
     * @param onLand       appelé à l'arrivée de chaque carte
     */
    public void discardFromDeck(int count, float fromX, float fromY, float toX, float toY,
                                float initialDelay, Runnable onLand) {
        for (int i = 0; i < count; i++) {
            Image card = new Image(backDrawable());
            card.setSize(cardWidth, cardHeight);
            card.setTouchable(Touchable.disabled);
            card.setPosition(fromX, fromY);
            stage.addActor(card);
            flyingCards.add(card);

            card.addAction(Actions.sequence(
                Actions.delay(initialDelay + i * STAGGER_DELAY * 2f),
                Actions.run(dealSound::play),
                Actions.moveTo(toX, toY, MOVE_DURATION * 2f, Interpolation.smooth),
                Actions.run(() -> land(card, onLand))
            ));
        }
    }

    /** Retire toute carte encore en vol, sans appeler les callbacks d'arrivée. */
    public void cancel() {
        for (Image card : flyingCards) {
            card.remove();
        }
        flyingCards.clear();
    }

    private void land(Image card, Runnable onLand) {
        flyingCards.remove(card);
        card.remove();
        onLand.run();
    }

    private TextureRegionDrawable backDrawable() {
        return new TextureRegionDrawable(new TextureRegion(cardBackTexture));
    }
}
