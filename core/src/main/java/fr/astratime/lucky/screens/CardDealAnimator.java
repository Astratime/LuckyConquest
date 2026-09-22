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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /** Cartes dos visible actuellement en vol (associées à leur carte cible), à nettoyer en cas d'annulation. */
    private final Map<Image, Image> flyingCards = new LinkedHashMap<>();

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
            flyingCards.put(flyingCard, target);

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

    /**
     * Interrompt la distribution en cours : retire les cartes encore en vol et
     * révèle directement leur carte cible à sa place (ex : spin lancé avant la
     * fin de l'animation).
     */
    public void cancel() {
        for (Map.Entry<Image, Image> entry : flyingCards.entrySet()) {
            entry.getKey().remove();
            entry.getValue().setVisible(true);
        }
        flyingCards.clear();
    }
}
