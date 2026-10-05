package fr.astratime.lucky.views;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
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
         * La carte vient de quitter la main : à l'écouteur de l'animer (voir {@link #slam}).
         *
         * @param card       carte cliquée
         * @param image      image de la carte, déjà retirée de la main (plus cliquable)
         * @param cardCenter centre de la carte (coordonnées du Stage), avant son retrait de la main
         * @param clickPos   point cliqué (coordonnées du Stage)
         */
        void onCardClicked(Card card, CardImage image, Vector2 cardCenter, Vector2 clickPos);
    }

    /** Appelé quand le joueur clique sur une carte qui ne peut pas être jouée (voir {@link #setBlockedReason}). */
    public interface CardRefusedListener {
        /**
         * @param card       carte refusée, restée dans la main
         * @param reason     pourquoi elle ne peut pas être jouée
         * @param cardCenter centre de la carte (coordonnées du Stage)
         */
        void onCardRefused(Card card, String reason, Vector2 cardCenter);
    }

    private static final float MOVE_DURATION = 0.25f;
    private static final float TOOLTIP_GAP   = 5f;
    // Carte jouée : elle grossit, puis se tasse en s'effaçant.
    private static final float SLAM_SCALE     = 1.3f;
    private static final float SLAM_END_SCALE = 0.85f;
    private static final float SLAM_GROW      = 0.07f;
    private static final float SLAM_FADE      = 0.16f;
    /** Teinte des cartes quand la main est bloquée (Bingo), ou d'une carte qui ne peut pas être jouée. */
    private static final float LOCKED_TINT    = 0.45f;
    /** Image du dos des cartes, montrée à la place des cartes d'une main cachée (Aveuglement). */
    private static final String BACK_PATH = "cards/light/BACK.png";

    /** Infobulle de la carte : son nom et sa description, suivis de la raison si elle ne peut pas être jouée. */
    private void showTooltip(CardImage cardImage) {
        if (hiddenImages.contains(cardImage)) { // Aveuglement : la carte reste un mystère
            Vector2 pos = cardImage.localToStageCoordinates(new Vector2(0, cardHeight + TOOLTIP_GAP));
            tooltip.show("Carte cachée", "Aveuglement : tu joues sans savoir laquelle.", pos.x, pos.y);
            return;
        }
        Card card = cardOf.get(cardImage);
        String reason = blockedReason.apply(card);
        String text = reason == null ? card.getDescription() : card.getDescription() + "\n\n" + reason;
        Vector2 pos = cardImage.localToStageCoordinates(new Vector2(0, cardHeight + TOOLTIP_GAP));
        tooltip.show(card.getName(), text, pos.x, pos.y);
    }

    /** Refus d'une carte : elle tremble sur place. */
    private static final float REFUSE_SHAKE   = 8f;
    /** Taille d'une carte qui vient de changer de couleur, avant de revenir à la normale. */
    private static final float RECOLOR_POP    = 1.25f;

    private final TableView           table;
    private final float               cardWidth;
    private final float               cardHeight;
    private final CardTextures        cardTextures;
    private final Tooltip             tooltip;
    private final Sound               hoverSound;
    private final PilesView           piles;
    private final Supplier<Player> player;
    private final CardDealAnimator    dealAnimator;
    private final CardDiscardAnimator discardAnimator;
    private final CardClickListener   clickListener;

    private final Group       group  = new Group();
    /** Images des cartes de la main (non jouées), dans l'ordre d'affichage. */
    private final List<Image> images = new ArrayList<>();
    /** Appelé au clic droit sur une carte : afficher sa fiche. */
    private Consumer<Card> onInspect;

    /** Carte représentée par chaque image de la main (elle peut changer : Arc-en-ciel). */
    private final Map<Image, Card> cardOf = new HashMap<>();
    /** Pourquoi une carte ne peut pas être jouée en ce moment ({@code null} : elle le peut). */
    private Function<Card, String> blockedReason = card -> null;
    private CardRefusedListener    refusedListener;
    private boolean                locked;
    /** Les cartes piochées arrivent face cachée (Aveuglement de l'ennemi). */
    private boolean                hidden;
    /** Images de la main montrées face cachée. */
    private final java.util.Set<Image> hiddenImages = new java.util.HashSet<>();

    public HandView(TableView table, float cardWidth, float cardHeight, CardTextures cardTextures, Tooltip tooltip,
             Sound hoverSound, PilesView piles, Supplier<Player> player,
             CardDealAnimator dealAnimator, CardDiscardAnimator discardAnimator, CardClickListener clickListener) {
        this.table           = table;
        this.cardWidth       = cardWidth;
        this.cardHeight      = cardHeight;
        this.cardTextures    = cardTextures;
        this.tooltip         = tooltip;
        this.hoverSound      = hoverSound;
        this.piles           = piles;
        this.player          = player;
        this.dealAnimator    = dealAnimator;
        this.discardAnimator = discardAnimator;
        this.clickListener   = clickListener;
    }

    /** @param onInspect appelé au clic droit sur une carte de la main (afficher sa fiche) */
    public void setOnInspect(Consumer<Card> onInspect) { this.onInspect = onInspect; }

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
            CardImage cardImage = createImage(card);
            cardImage.setVisible(false); // révélée seulement à la fin de son animation de distribution
            newImages.add(cardImage);
        }
        layout(true);
        refreshBlocked();
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

    /**
     * Aveuglement : les cartes piochées ensuite arrivent face cachée (jusqu'à
     * {@code false}). Une carte cachée se joue sans qu'on sache laquelle.
     */
    public void setHidden(boolean hidden) { this.hidden = hidden; }

    /**
     * Joue {@code card} d'office (Chant de l'ennemi), comme si le joueur avait
     * cliqué dessus.
     *
     * @return {@code false} si elle n'est pas dans la main affichée
     */
    public boolean forcePlay(Card card) {
        for (Image image : new ArrayList<>(images)) {
            if (cardOf.get(image) != card || !(image instanceof CardImage cardImage)) continue;
            Vector2 cardCenter = cardImage.localToStageCoordinates(new Vector2(cardWidth / 2f, cardHeight / 2f));
            cardOf.remove(cardImage);
            images.remove(cardImage);
            hiddenImages.remove(cardImage);
            detach(cardImage);
            tooltip.hide();
            clickListener.onCardClicked(card, cardImage, cardCenter, cardCenter);
            return true;
        }
        return false;
    }

    /** Crée l'image de {@code card} et l'ajoute à la main (à la fin de la rangée). */
    private CardImage createImage(Card card) {
        Texture face = hidden ? cardTextures.get(BACK_PATH) : cardTextures.get(card);
        CardImage cardImage = new CardImage(new TextureRegionDrawable(new TextureRegion(face)));
        if (hidden) hiddenImages.add(cardImage);
        cardImage.setSize(cardWidth, cardHeight);
        addListeners(cardImage);
        cardOf.put(cardImage, card);
        group.addActor(cardImage);
        images.add(cardImage);
        return cardImage;
    }

    /** @return le centre (Stage) de l'image de {@code card} dans la main, ou {@code null} si elle n'y est pas. */
    public Vector2 centerOf(Card card) {
        for (Image image : images) {
            if (cardOf.get(image) == card) {
                return image.localToStageCoordinates(new Vector2(cardWidth / 2f, cardHeight / 2f));
            }
        }
        return null;
    }

    /**
     * @return le rectangle (Stage) qui réunit, sur la rangée de la main, les
     *         cartes pour lesquelles {@code which} est vrai (le guide du tutoriel
     *         les éclaire), ou {@code null} s'il n'y en a aucune
     */
    public Rectangle boundsOf(Predicate<Card> which) {
        Rectangle bounds = null;
        for (Image image : images) {
            if (!which.test(cardOf.get(image))) continue;
            // La rangée, pas la carte levée au survol : le trou ne bouge pas sous la souris.
            Vector2 corner = group.localToStageCoordinates(new Vector2(image.getX(), table.getHandRowY()));
            Rectangle card = new Rectangle(corner.x, corner.y, cardWidth, cardHeight);
            bounds = bounds == null ? card : bounds.merge(card);
        }
        return bounds;
    }

    /** {@code before} change d'apparence et devient {@code after} (même place), avec un petit rebond. */
    public void recolor(Card before, Card after) {
        for (Image image : images) {
            if (cardOf.get(image) != before) continue;
            if (!hiddenImages.contains(image)) { // une carte cachée le reste
                image.setDrawable(new TextureRegionDrawable(new TextureRegion(cardTextures.get(after))));
            }
            cardOf.put(image, after);
            image.setScale(RECOLOR_POP); // la carte se rétablit d'elle-même à sa taille (voir CardImage)
            return;
        }
    }

    /**
     * Fait apparaître {@code card} sur un emplacement libre de la table : elle
     * surgit de rien pendant que la rangée se recentre.
     *
     * @return le centre (Stage) de son emplacement
     */
    public Vector2 conjure(Card card) {
        boolean wasHidden = hidden;
        hidden = false; // une carte qui surgit (achat, Arc-en-ciel) se voit, même sous l'Aveuglement
        CardImage cardImage = createImage(card);
        hidden = wasHidden;
        cardImage.setVisible(false); // placée directement sur son emplacement, sans glisser
        layout(true);
        cardImage.setVisible(true);
        cardImage.setScale(0f); // grandit jusqu'à sa taille (voir CardImage)
        return cardImage.localToStageCoordinates(new Vector2(cardWidth / 2f, cardHeight / 2f));
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
        cardOf.clear();
        hiddenImages.clear();
        hidden = false;
    }

    /**
     * Retire toutes les cartes de la main affichée, sans rien défausser, et
     * interrompt leur distribution. Les cartes déjà en vol vers la défausse
     * finissent leur trajet (elles doivent y être comptées à leur arrivée).
     */
    public void clear() {
        dealAnimator.cancel();
        images.clear();
        cardOf.clear();
        hiddenImages.clear();
        hidden = false;
        group.clearChildren();
    }

    /** Nouveau combat : retire la main et interrompt toutes les animations, y compris vers la défausse. */
    public void reset() {
        discardAnimator.cancel();
        clear();
    }

    /**
     * Pose les cartes de la main sur les emplacements de la table, centrées dans
     * la rangée (une main impaire est décalée d'un demi-emplacement vers la
     * gauche). Avec {@code animate}, les cartes déjà révélées glissent vers leur
     * nouvelle place ; les autres (en cours de distribution) y sont placées
     * directement.
     */
    public void layout(boolean animate) {
        int count     = images.size();
        int firstSlot = Math.max(0, (Player.MAX_HAND_SIZE - count) / 2);
        float y       = table.getHandRowY();

        for (int i = 0; i < count; i++) {
            Image cardImage = images.get(i);
            float x = table.getHandSlotX(firstSlot + i);
            cardImage.clearActions();
            if (animate && cardImage.isVisible()) {
                cardImage.addAction(Actions.moveTo(x, y, MOVE_DURATION, Interpolation.pow2Out));
            } else {
                cardImage.setPosition(x, y);
            }
        }
    }

    private void onCardLandedInDiscard() {
        piles.onCardLanded();
        piles.refresh(player.get());
    }

    /**
     * Attache à une image de carte : au survol, sa levée et l'affichage de sa
     * description ; au clic, son retrait de la main (elle grossit puis « claque »
     * en s'effaçant) et la notification du clic.
     */
    private void addListeners(CardImage cardImage) {
        cardImage.addListener(new InputListener() {

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer != -1) return;
                hoverSound.play(1f, MathUtils.random(0.95f, 1.06f), 0f); // à peine varié d'une carte à l'autre
                cardImage.setHovered(true);
                cardImage.tiltToward(x);
                showTooltip(cardImage);
            }

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                cardImage.tiltToward(x);
                return false;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer != -1) return;
                cardImage.setHovered(false);
                tooltip.hide();
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button == Input.Buttons.RIGHT) { // clic droit : fiche de la carte, sans la jouer
                    tooltip.hide();
                    if (onInspect != null && !hiddenImages.contains(cardImage)) onInspect.accept(cardOf.get(cardImage));
                    return true;
                }
                Vector2 clickPos   = cardImage.localToStageCoordinates(new Vector2(x, y));
                Vector2 cardCenter = cardImage.localToStageCoordinates(new Vector2(cardWidth / 2f, cardHeight / 2f));
                String reason = blockedReason.apply(cardOf.get(cardImage));
                if (reason != null) { // la carte ne peut pas être jouée : elle tremble et reste dans la main
                    refuse(cardImage);
                    showTooltip(cardImage); // avec la raison du refus
                    if (refusedListener != null) refusedListener.onCardRefused(cardOf.get(cardImage), reason, cardCenter);
                    return true;
                }
                Card card = cardOf.remove(cardImage);
                images.remove(cardImage);
                hiddenImages.remove(cardImage);
                detach(cardImage);
                tooltip.hide();
                clickListener.onCardClicked(card, cardImage, cardCenter, clickPos);
                return true;
            }
        });
    }

    /**
     * Bloque ou débloque la main : bloquée, ses cartes s'assombrissent et ne
     * réagissent plus (une carte comme le Bingo interdit d'en jouer d'autres).
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
        group.setTouchable(locked ? Touchable.disabled : Touchable.childrenOnly);
        refreshBlocked();
        if (locked) {
            tooltip.hide();
            for (Image image : images) {
                if (image instanceof CardImage card) card.setHovered(false);
            }
        }
    }

    /**
     * @param reason    pourquoi une carte ne peut pas être jouée en ce moment ({@code null} : elle le peut)
     * @param onRefused prévenu quand le joueur clique quand même sur une telle carte
     */
    public void setBlockedReason(Function<Card, String> reason, CardRefusedListener onRefused) {
        this.blockedReason   = reason;
        this.refusedListener = onRefused;
    }

    /**
     * Assombrit les cartes qui ne peuvent pas être jouées (toutes quand la main
     * est bloquée). À rappeler quand la raison peut avoir changé (ex : un
     * Recyclage vient de retirer un symbole).
     */
    public void refreshBlocked() {
        for (Image image : images) {
            boolean dark = locked || blockedReason.apply(cardOf.get(image)) != null;
            float tint = dark ? LOCKED_TINT : 1f;
            image.setColor(tint, tint, tint, image.getColor().a);
        }
    }

    /** Refus d'une carte : elle tremble de gauche à droite, sans quitter sa place. */
    private void refuse(CardImage cardImage) {
        cardImage.addAction(Actions.sequence(
            Actions.moveBy(REFUSE_SHAKE, 0f, 0.04f), Actions.moveBy(-REFUSE_SHAKE * 2f, 0f, 0.07f),
            Actions.moveBy(REFUSE_SHAKE * 2f, 0f, 0.07f), Actions.moveBy(-REFUSE_SHAKE, 0f, 0.04f)));
    }

    /** Sort la carte de la main : elle n'est plus cliquable et reste à la même place à l'écran, au-dessus du jeu. */
    private void detach(CardImage cardImage) {
        Vector2 pos = cardImage.localToStageCoordinates(new Vector2(0f, 0f));
        cardImage.clearListeners();
        cardImage.clearActions();
        cardImage.setTouchable(Touchable.disabled);
        cardImage.setRotation(0f);
        group.getStage().addActor(cardImage);
        cardImage.setPosition(pos.x, pos.y);
    }

    /** Carte jouée : sortie de la main, elle grossit brièvement puis se tasse en s'effaçant. */
    public void slam(CardImage cardImage) {
        cardImage.addAction(Actions.sequence(
            Actions.scaleTo(SLAM_SCALE, SLAM_SCALE, SLAM_GROW, Interpolation.pow2Out),
            Actions.parallel(
                Actions.scaleTo(SLAM_END_SCALE, SLAM_END_SCALE, SLAM_FADE, Interpolation.pow2In),
                Actions.fadeOut(SLAM_FADE)),
            Actions.removeActor()));
    }
}
