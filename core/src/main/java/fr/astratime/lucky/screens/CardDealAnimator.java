package fr.astratime.lucky.screens;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Anime l'arrivée d'une main de cartes : chacune part, dos visible, d'un point
 * de départ commun (le sommet de la pile), glisse vers sa place finale (déjà
 * calculée mais invisible), puis se retourne (flip) pour révéler sa face.
 *
 * Ne possède pas cardBackTexture/dealSound/dealFlip : ces ressources restent
 * possédées (et disposées) par l'appelant, qui les réutilise ailleurs (deck
 * décoratif, autres bruitages).
 */
public class CardDealAnimator {

    private static final float DEAL_STAGGER_DELAY = 0.15f;
    private static final float DEAL_MOVE_DURATION = 0.35f;
    private static final float FLIP_PAUSE_DELAY   = 0.05f;
    private static final float FLIP_HALF_DURATION = 0.12f;

    private final Stage   stage;
    private final Texture cardBackTexture;
    private final float   cardWidth;
    private final float   cardHeight;
    private final Sound   dealSound;
    private final Sound   flipSound;

    /** Cartes dos visible actuellement en vol, à nettoyer si une nouvelle donne démarre. */
    private final List<Image> flyingCards = new ArrayList<>();

    public CardDealAnimator(Stage stage, Texture cardBackTexture, float cardWidth, float cardHeight,
                             Sound dealSound, Sound flipSound) {
        this.stage           = stage;
        this.cardBackTexture = cardBackTexture;
        this.cardWidth       = cardWidth;
        this.cardHeight      = cardHeight;
        this.dealSound       = dealSound;
        this.flipSound       = flipSound;
    }

    /**
     * Anime l'arrivée de {@code targets} : chacune part de {@code (startX, startY)}
     * (coordonnées du Stage) dos visible, glisse jusqu'à sa position actuelle (déjà
     * calculée mais invisible), puis se retourne pour révéler sa face.
     */
    public void deal(List<Image> targets, float startX, float startY) {
        for (int i = 0; i < targets.size(); i++) {
            Image target = targets.get(i);
            Vector2 targetPos = target.localToStageCoordinates(new Vector2(0f, 0f));

            Image flyingCard = new Image(new TextureRegionDrawable(new TextureRegion(cardBackTexture)));
            flyingCard.setSize(cardWidth, cardHeight);
            flyingCard.setOrigin(cardWidth / 2f, cardHeight / 2f);
            flyingCard.setPosition(startX, startY);
            stage.addActor(flyingCard);
            flyingCards.add(flyingCard);

            flyingCard.addAction(Actions.sequence(
                Actions.delay(i * DEAL_STAGGER_DELAY),
                Actions.run(dealSound::play),
                Actions.moveTo(targetPos.x, targetPos.y, DEAL_MOVE_DURATION, Interpolation.pow2Out),
                Actions.delay(FLIP_PAUSE_DELAY),
                Actions.run(flipSound::play),
                Actions.scaleTo(0f, 1f, FLIP_HALF_DURATION),
                Actions.run(() -> flyingCard.setDrawable(target.getDrawable())),
                Actions.scaleTo(1f, 1f, FLIP_HALF_DURATION),
                Actions.run(() -> {
                    flyingCards.remove(flyingCard);
                    flyingCard.remove();
                    target.setVisible(true);
                })
            ));
        }
    }

    /** Retire toute carte encore en cours de distribution (ex : nouvelle donne avant la fin de l'animation). */
    public void cancel() {
        for (Image flyingCard : flyingCards) {
            flyingCard.remove();
        }
        flyingCards.clear();
    }
}
