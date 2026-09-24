package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import fr.astratime.lucky.animations.CardDealAnimator;
import fr.astratime.lucky.animations.CardDiscardAnimator;
import fr.astratime.lucky.assets.CardTextures;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.DrawResult;
import fr.astratime.lucky.entities.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * La main du joueur : une rangée de cartes centrée, positionnée à la main (et
 * non via une Table) pour pouvoir animer le réagencement quand des cartes sont
 * piochées pendant le tour. Gère aussi les animations de distribution (depuis
 * le deck) et de défausse (vers la défausse), et signale les cartes cliquées.
 */
public class HandView {

    /** Appelé quand le joueur clique sur une carte de la main. */
    public interface CardClickListener {
        /**
         * @param card       carte cliquée
         * @param cardCenter centre de la carte (coordonnées du Stage), avant son retrait de la main
         * @param clickPos   point cliqué (coordonnées du Stage)
         */
        void onCardClicked(Card card, Vector2 cardCenter, Vector2 clickPos);
    }

    private static final float CARD_ROW_Y    = 350f;
    private static final float CARD_GAP      = 20f;
    private static final float MOVE_DURATION = 0.25f;
    private static final float TOOLTIP_GAP   = 5f;

    private final PlayArea            playArea;
    private final float               cardWidth;
    private final float               cardHeight;
    private final CardTextures        cardTextures;
    private final Tooltip             tooltip;
    private final PilesView           piles;
    private final Supplier<Player> player;
    private final CardDealAnimator    dealAnimator;
    private final CardDiscardAnimator discardAnimator;
    private final CardClickListener   clickListener;

    private final Group       group  = new Group();
    /** Images des cartes de la main (non jouées), dans l'ordre d'affichage. */
    private final List<Image> images = new ArrayList<>();

    public HandView(PlayArea playArea, float cardWidth, float cardHeight, CardTextures cardTextures, Tooltip tooltip,
             PilesView piles, Supplier<Player> player,
             CardDealAnimator dealAnimator, CardDiscardAnimator discardAnimator, CardClickListener clickListener) {
        this.playArea        = playArea;
        this.cardWidth       = cardWidth;
        this.cardHeight      = cardHeight;
        this.cardTextures    = cardTextures;
        this.tooltip         = tooltip;
        this.piles           = piles;
        this.player          = player;
        this.dealAnimator    = dealAnimator;
        this.discardAnimator = discardAnimator;
        this.clickListener   = clickListener;
    }

    /** @return le groupe contenant les cartes de la main, à ajouter au Stage. */
    public Group getActor() { return group; }

    /**
     * Ajoute à la main les cartes piochées : chacune est placée (invisible) à sa
     * position finale, les cartes déjà présentes glissent pour recentrer la
     * rangée, puis l'animation de distribution les révèle depuis le deck. Les
     * cartes piochées sans place dans la main partent du deck vers la défausse.
     */
    public void deal(DrawResult drawResult) {
        piles.refresh(player.get()); // le deck a déjà perdu les cartes piochées

        List<Image> newImages = new ArrayList<>();
        for (Card card : drawResult.getAddedToHand()) {
            Image cardImage = new Image(new TextureRegionDrawable(new TextureRegion(cardTextures.get(card))));
            cardImage.setSize(cardWidth, cardHeight);
            cardImage.setVisible(false); // révélée seulement à la fin de son animation de distribution
            addListeners(cardImage, card);
            group.addActor(cardImage);
            images.add(cardImage);
            newImages.add(cardImage);
        }
        layout(true);
        dealAnimator.deal(newImages, piles.deck().getTopX(), piles.deck().getTopY());

        int overflow = drawResult.getDiscarded().size();
        if (overflow > 0) {
            piles.addInFlight(overflow);
            piles.refresh(player.get());
            discardAnimator.discardFromDeck(overflow,
                piles.deck().getTopX(), piles.deck().getTopY(),
                piles.discard().getTopX(), piles.discard().getTopY(),
                newImages.size() * CardDealAnimator.DEAL_STAGGER_DELAY, // après la distribution des cartes gardées
                this::onCardLandedInDiscard);
        }
    }

    /** Fin de tour : les cartes restées sur la table se retournent puis glissent jusqu'à la défausse. */
    public void discardAll() {
        dealAnimator.cancel(); // révèle d'un coup les cartes encore en cours de distribution
        tooltip.hide();
        piles.addInFlight(images.size());
        piles.refresh(player.get());
        discardAnimator.discardFromTable(new ArrayList<>(images),
            piles.discard().getTopX(), piles.discard().getTopY(), this::onCardLandedInDiscard);
        images.clear();
    }

    /**
     * Retire toutes les cartes de la main affichée, sans rien défausser, et
     * interrompt leur distribution. Les cartes déjà en vol vers la défausse
     * finissent leur trajet (elles doivent y être comptées à leur arrivée).
     */
    public void clear() {
        dealAnimator.cancel();
        images.clear();
        group.clearChildren();
    }

    /** Nouveau combat : retire la main et interrompt toutes les animations, y compris vers la défausse. */
    public void reset() {
        discardAnimator.cancel();
        clear();
    }

    /**
     * Place les cartes de la main sur une rangée centrée dans la zone de jeu.
     * Avec {@code animate}, les cartes déjà révélées glissent vers leur nouvelle
     * place ; les autres (en cours de distribution) y sont placées directement.
     */
    public void layout(boolean animate) {
        int count = images.size();
        float rowWidth = count * cardWidth + Math.max(0, count - 1) * CARD_GAP;
        float startX   = playArea.getCenterX() - rowWidth / 2f;

        for (int i = 0; i < count; i++) {
            Image cardImage = images.get(i);
            float x = startX + i * (cardWidth + CARD_GAP);
            cardImage.clearActions();
            if (animate && cardImage.isVisible()) {
                cardImage.addAction(Actions.moveTo(x, CARD_ROW_Y, MOVE_DURATION, Interpolation.pow2Out));
            } else {
                cardImage.setPosition(x, CARD_ROW_Y);
            }
        }
    }

    private void onCardLandedInDiscard() {
        piles.onCardLanded();
        piles.refresh(player.get());
    }

    /**
     * Attache à une image de carte : l'affichage de sa description au survol,
     * et au clic son retrait de la main puis la notification du clic.
     */
    private void addListeners(Image cardImage, Card card) {
        cardImage.addListener(new InputListener() {

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer != -1) return;
                Vector2 pos = cardImage.localToStageCoordinates(new Vector2(0, cardHeight + TOOLTIP_GAP));
                tooltip.show(card.getDescription(), pos.x, pos.y);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer != -1) return;
                tooltip.hide();
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Vector2 clickPos   = cardImage.localToStageCoordinates(new Vector2(x, y));
                Vector2 cardCenter = cardImage.localToStageCoordinates(new Vector2(cardWidth / 2f, cardHeight / 2f));
                images.remove(cardImage);
                cardImage.remove();
                tooltip.hide();
                clickListener.onCardClicked(card, cardCenter, clickPos);
                return true;
            }
        });
    }
}
