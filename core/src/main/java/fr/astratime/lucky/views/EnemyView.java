package fr.astratime.lucky.views;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.animations.EffectPopupAnimator;
import fr.astratime.lucky.assets.EnemyTextures;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.enemy.EnemyCards;
import fr.astratime.lucky.entities.enemy.EnemySlotMachine;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.events.EnemyShieldedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Côté de l'ennemi, sur la table (voir {@link TableView}) : le croupier
 * démoniaque, sa machine à sous, ses cartes, son deck et sa défausse, et sa
 * défense. Joue en animation le tour de l'ennemi déjà résolu
 * ({@link #playTurn}) : il pioche ses cartes face cachée, retourne les trois
 * qu'il joue (avec leur bonus), lance ses rouleaux (chaque symbole agit à son
 * arrêt), puis toutes ses cartes filent dans sa défausse.
 *
 * Survoler le croupier, un rouleau ou une carte jouée affiche son effet.
 */
public class EnemyView implements Disposable {

    private static final float DEAL_STAGGER   = 0.07f;
    private static final float DEAL_TIME      = 0.25f;
    private static final float REVEAL_START   = 0.9f;   // après la distribution
    private static final float REVEAL_STEP    = 0.5f;   // entre deux cartes retournées
    private static final float FLIP_TIME      = 0.1f;   // chaque moitié du retournement
    private static final float PLAYED_LIFT    = 14f;
    private static final float UNPLAYED_ALPHA = 0.45f;
    private static final float SPIN_FRAME     = 0.06f;  // défilement des rouleaux
    private static final float FIRST_STOP     = 0.7f;
    private static final float STOP_STEP      = 0.35f;
    private static final float AFTER_STOPS    = 0.9f;   // temps de lecture des textes des symboles
    private static final float AFTER_EVENTS   = 0.8f;   // renvoi de dégâts
    private static final float DISCARD_STAGGER = 0.04f;
    private static final float DISCARD_TIME   = 0.3f;
    private static final float END_PAUSE      = 0.4f;
    private static final float POPUP_GAP      = 16f;    // au-dessus du croupier
    private static final float CARD_POPUP_GAP = 26f;    // sous une carte jouée
    /** Textes des symboles : sous le filet, côté joueur (vide pendant le tour de l'ennemi), écartés. */
    private static final float RESULT_BELOW   = 70f;
    private static final float RESULT_SPREAD  = 250f;
    private static final float SYMBOL_SIZE    = 80f;    // 16 pixels x5, comme les symboles du joueur
    private static final float BOB            = 4f;     // respiration du croupier
    private static final float DEFENSE_GAP    = 40f;    // entre les rouleaux et la défense (au-delà du cadre)

    private final TableView           table;
    private final EnemyTextures       textures;
    private final Tooltip             tooltip;
    private final EffectPopupAnimator popups;
    private final Sound               cardSound;
    private final Sound               flipSound;
    private final Sound               reelSpinSound;
    private final Sound               reelStopSound;
    private final Supplier<Enemy>     enemy;
    private final BitmapFont          defenseFont = Fonts.jersey(26, Color.WHITE, 2f, Palette.TEXT_SHADE);

    private final Group         group     = new Group();
    private final Group         character = new Group();
    private final Image         croupier;
    private final Image         flash;
    private final Image         defenseIcon;
    private final Label         defenseLabel;
    private final CardPileView  deckPile;
    private final CardPileView  discardPile;
    private final List<Image>   reels     = new ArrayList<>();
    private final EnemySymbol[] shownSymbols = {EnemySymbol.SWORD, EnemySymbol.SHIELD, EnemySymbol.POTION};
    private final List<Image>   handCards = new ArrayList<>();
    private int shownDefense  = Enemy.BASE_DEFENSE;
    private int shownDeck;
    private int shownDiscard;
    /** Boucle du bruit de ses rouleaux pendant son lancer (-1 : aucune). */
    private long spinSoundId = -1;

    /**
     * @param reelSpinSound boucle jouée tant que ses rouleaux tournent
     * @param reelStopSound bruitage joué quand un de ses rouleaux s'arrête
     * @param enemy         l'ennemi du combat en cours (il change à chaque nouveau combat)
     */
    public EnemyView(TableView table, EnemyTextures textures, BitmapFont pileFont, Tooltip tooltip,
                     EffectPopupAnimator popups, Sound cardSound, Sound flipSound, Sound reelSpinSound,
                     Sound reelStopSound, Supplier<Enemy> enemy) {
        this.table         = table;
        this.textures      = textures;
        this.tooltip       = tooltip;
        this.popups        = popups;
        this.cardSound     = cardSound;
        this.flipSound     = flipSound;
        this.reelSpinSound = reelSpinSound;
        this.reelStopSound = reelStopSound;
        this.enemy         = enemy;

        croupier = new Image(new TextureRegionDrawable(new TextureRegion(textures.croupier)));
        croupier.setSize(textures.croupier.getWidth() * EnemyTextures.CROUPIER_SCALE,
            textures.croupier.getHeight() * EnemyTextures.CROUPIER_SCALE);
        flash = new Image(new TextureRegionDrawable(new TextureRegion(textures.croupierFlash)));
        flash.setSize(croupier.getWidth(), croupier.getHeight());
        flash.getColor().a = 0f;
        flash.setTouchable(Touchable.disabled);
        character.addActor(croupier);
        character.addActor(flash);
        character.setSize(croupier.getWidth(), croupier.getHeight());
        character.setOrigin(croupier.getWidth() / 2f, 0f);
        hoverTooltip(croupier, () -> "Croupier démoniaque",
            () -> "Défense : " + shownDefense + "\nPioche " + Enemy.HAND_SIZE + " cartes et en joue "
                + Enemy.PLAYS_PER_TURN + " à chaque tour");

        deckPile    = new CardPileView("DECK", textures.cardBack, pileFont,
            table.getCardWidth(), table.getCardHeight());
        discardPile = new CardPileView("DÉFAUSSE", textures.cardBack, pileFont,
            table.getCardWidth(), table.getCardHeight());

        for (int i = 0; i < EnemySlotMachine.SYMBOL_COUNT; i++) {
            int reel = i;
            Image image = new Image(new TextureRegionDrawable(new TextureRegion(textures.symbol(shownSymbols[i]))));
            image.setSize(SYMBOL_SIZE, SYMBOL_SIZE);
            hoverTooltip(image, () -> shownSymbols[reel].getDisplayName(), () -> shownSymbols[reel].getDescription());
            reels.add(image);
        }

        defenseIcon  = new Image(new TextureRegionDrawable(new TextureRegion(textures.symbol(EnemySymbol.SHIELD))));
        defenseIcon.setSize(32f, 32f);
        defenseLabel = new Label("", new Label.LabelStyle(defenseFont, Color.WHITE));
        hoverTooltip(defenseIcon, () -> "Défense de l'ennemi",
            () -> "Réduit les dégâts de chacune de tes attaques\nLes Boucliers de son tour s'y ajoutent jusqu'à son tour suivant");

        group.addActor(deckPile);
        group.addActor(discardPile);
        reels.forEach(group::addActor);
        group.addActor(defenseIcon);
        group.addActor(defenseLabel);
        group.addActor(character);
        reset();
    }

    /** @return le côté de l'ennemi, à ajouter au Stage au-dessus de la table. */
    public Group getActor() { return group; }

    /** Nouveau combat : croupier debout, piles pleines, défense de base, rouleaux au repos. */
    public void reset() {
        stopSpinSound();
        group.clearActions();
        handCards.forEach(Actor::remove);
        handCards.clear();
        character.clearActions();
        character.setRotation(0f);
        character.getColor().a = 1f;
        flash.clearActions();
        flash.getColor().a = 0f;
        shownDefense = Enemy.BASE_DEFENSE;
        shownDeck    = enemy.get().getDeckCards().size();
        shownDiscard = enemy.get().getDiscardCards().size();
        refreshCounts();
        layout();
        idle();
    }

    /** Place le croupier, ses rouleaux et ses piles aux positions de la table (après un redimensionnement). */
    public void layout() {
        character.clearActions();
        // Le plus grand agrandissement entier qui tient entre les cartes de l'ennemi et le haut du feutre.
        float room  = table.getFeltTop() - table.getEnemyCharacterY();
        int   scale = Math.clamp((int) (room / textures.croupier.getHeight()), 2, EnemyTextures.CROUPIER_SCALE);
        croupier.setSize(textures.croupier.getWidth() * scale, textures.croupier.getHeight() * scale);
        flash.setSize(croupier.getWidth(), croupier.getHeight());
        character.setSize(croupier.getWidth(), croupier.getHeight());
        character.setOrigin(croupier.getWidth() / 2f, 0f);
        character.setPosition(table.getEnemyCharacterCenterX() - character.getWidth() / 2f, table.getEnemyCharacterY());
        idle();
        deckPile.setPosition(table.getEnemyDeckX(), table.getEnemyPilesY());
        discardPile.setPosition(table.getEnemyDiscardX(), table.getEnemyPilesY());
        float padX = (SlotView.CELL_WIDTH - SYMBOL_SIZE) / 2f, padY = (SlotView.CELL_HEIGHT - SYMBOL_SIZE) / 2f;
        for (int i = 0; i < reels.size(); i++) {
            reels.get(i).setPosition(table.getReelRowX() + i * SlotView.CELL_WIDTH + padX,
                table.getEnemyReelRowY() + padY);
        }
        float reelsRight = table.getReelRowX() + EnemySlotMachine.SYMBOL_COUNT * SlotView.CELL_WIDTH;
        float centerY    = table.getEnemyReelRowY() + SlotView.CELL_HEIGHT / 2f;
        defenseIcon.setPosition(reelsRight + DEFENSE_GAP, centerY - defenseIcon.getHeight() / 2f);
        refreshDefense();
        for (int i = 0; i < handCards.size(); i++) {
            handCards.get(i).setPosition(table.getEnemyCardSlotX(i), table.getEnemyCardRowY());
        }
    }

    /** Respiration du croupier, au repos. */
    private void idle() {
        character.addAction(Actions.forever(Actions.sequence(
            Actions.moveBy(0f, BOB, 1.2f, Interpolation.sine),
            Actions.moveBy(0f, -BOB, 1.2f, Interpolation.sine))));
    }

    // -------------------------------------------------------------------------
    // Tour de l'ennemi
    // -------------------------------------------------------------------------

    /**
     * Joue en animation le tour de l'ennemi {@code turn} (déjà résolu).
     *
     * @param onEventShown reçoit chaque événement du tour à l'instant où son texte apparaît
     * @param onDone       appelé quand ses cartes ont rejoint sa défausse
     */
    public void playTurn(EnemyTurnResult turn, Consumer<Event> onEventShown, Runnable onDone) {
        handCards.forEach(Actor::remove);
        handCards.clear();
        shownDefense = Enemy.BASE_DEFENSE; // les Boucliers de son tour précédent ne valent plus
        refreshDefense();

        // Distribution, face cachée, depuis son deck (remélangé depuis sa défausse s'il s'épuise).
        List<Card> drawn = turn.drawn();
        for (int i = 0; i < drawn.size(); i++) {
            if (shownDeck == 0) {
                shownDeck    = shownDiscard;
                shownDiscard = 0;
            }
            shownDeck = Math.max(0, shownDeck - 1);
            Image card = new Image(new TextureRegionDrawable(new TextureRegion(textures.cardBack)));
            card.setSize(table.getCardWidth(), table.getCardHeight());
            card.setOrigin(table.getCardWidth() / 2f, table.getCardHeight() / 2f);
            card.setPosition(deckPile.getTopX(), deckPile.getTopY());
            card.setVisible(false);
            card.addAction(Actions.sequence(
                Actions.delay(i * DEAL_STAGGER),
                Actions.visible(true),
                Actions.run(cardSound::play),
                Actions.moveTo(table.getEnemyCardSlotX(i), table.getEnemyCardRowY(), DEAL_TIME, Interpolation.pow2Out)));
            group.addActorBefore(character, card);
            handCards.add(card);
        }
        refreshCounts();

        // Les cartes jouées se retournent une à une, avec leur bonus ; les autres s'effacent.
        float time = REVEAL_START;
        for (Card played : turn.played()) {
            int index = drawn.indexOf(played);
            if (index < 0) continue;
            reveal(handCards.get(index), played, time);
            time += REVEAL_STEP;
        }
        for (int i = 0; i < drawn.size(); i++) {
            if (!turn.played().contains(drawn.get(i))) {
                handCards.get(i).addAction(Actions.delay(time, Actions.alpha(UNPLAYED_ALPHA, 0.3f)));
            }
        }

        // Lancer : les rouleaux défilent puis s'arrêtent l'un après l'autre ; chaque symbole agit à son arrêt.
        float spinStart = time + 0.2f;
        EnemySymbol[] symbols = turn.symbols();
        group.addAction(Actions.delay(spinStart, Actions.run(() -> spinSoundId = reelSpinSound.loop())));
        for (int i = 0; i < symbols.length; i++) {
            int reel = i;
            float stopAt = spinStart + FIRST_STOP + i * STOP_STEP;
            spinReel(reel, spinStart, stopAt);
            group.addAction(Actions.delay(stopAt, Actions.run(() -> {
                stopReel(reel, symbols[reel], turn.outcomes().get(reel), onEventShown);
                int stillSpinning = symbols.length - 1 - reel;
                if (stillSpinning == 0) {
                    stopSpinSound();
                } else {
                    reelSpinSound.setVolume(spinSoundId, stillSpinning / (float) symbols.length);
                }
            })));
        }
        float afterStops = spinStart + FIRST_STOP + (symbols.length - 1) * STOP_STEP + AFTER_STOPS;

        // Suites de ses attaques (renvoi de dégâts), au-dessus du croupier.
        float discardAt = afterStops;
        if (!turn.afterEvents().isEmpty()) {
            group.addAction(Actions.delay(afterStops, Actions.run(() -> {
                Vector2 top = characterTop();
                List<EffectPopup> texts = new ArrayList<>();
                turn.afterEvents().forEach(event -> texts.addAll(event.getPopups()));
                popups.play(texts, top.x, top.y);
                turn.afterEvents().forEach(onEventShown);
            })));
            discardAt += AFTER_EVENTS;
        }

        // Toutes ses cartes filent dans sa défausse.
        group.addAction(Actions.delay(discardAt, Actions.run(this::discardHand)));
        float end = discardAt + handCards.size() * DISCARD_STAGGER + DISCARD_TIME + END_PAUSE;
        group.addAction(Actions.delay(end, Actions.run(() -> {
            syncWithModel();
            onDone.run();
        })));
    }

    /** Une carte jouée se soulève, se retourne (sa face sombre apparaît) et annonce son bonus. */
    private void reveal(Image card, Card played, float delay) {
        Texture face = textures.card(played);
        card.addAction(Actions.sequence(
            Actions.delay(delay),
            Actions.moveBy(0f, PLAYED_LIFT, 0.12f, Interpolation.pow2Out),
            Actions.scaleTo(0f, 1f, FLIP_TIME, Interpolation.pow2In),
            Actions.run(() -> {
                ((TextureRegionDrawable) card.getDrawable()).setRegion(new TextureRegion(face));
                flipSound.play();
                nod();
                hoverTooltip(card, played::getName, () -> EnemyCards.describe(played));
                popups.play(List.of(new EffectPopup(bonusText(played), styleOf(played), PopupScale.SECONDARY_INTENSITY * 0.7f)),
                    card.getX() + card.getWidth() / 2f, card.getY() - CARD_POPUP_GAP);
            }),
            Actions.scaleTo(1f, 1f, FLIP_TIME, Interpolation.pow2Out)));
    }

    /** @return le bonus d'une carte jouée, en quelques mots (ex : "ÉPÉE +10"). */
    private static String bonusText(Card card) {
        return switch (card.getSuit()) {
            case PIQUE   -> "ÉPÉE +" + EnemyCards.swordBonus(card);
            case COEUR   -> "POTION +" + EnemyCards.healBonus(card) + "%";
            case CARREAU -> "BOUCLIER +" + EnemyCards.shieldBonus(card);
            case TREFLE  -> "CHANCE +" + EnemyCards.luckBonus(card) + "%";
        };
    }

    private static EffectPopup.Style styleOf(Card card) {
        return switch (card.getSuit()) {
            case PIQUE   -> EffectPopup.Style.ATTACK;
            case COEUR   -> EffectPopup.Style.DRAIN;
            case CARREAU -> EffectPopup.Style.DEFENSE;
            case TREFLE  -> EffectPopup.Style.GAINS;
        };
    }

    /** Coupe la boucle du bruit de ses rouleaux si elle est en cours. */
    private void stopSpinSound() {
        if (spinSoundId != -1) reelSpinSound.stop(spinSoundId);
        spinSoundId = -1;
    }

    /** Le rouleau {@code reel} fait défiler les symboles de {@code start} à {@code stop}. */
    private void spinReel(int reel, float start, float stop) {
        Image image = reels.get(reel);
        int frames = Math.max(1, Math.round((stop - start) / SPIN_FRAME));
        image.addAction(Actions.delay(start, Actions.repeat(frames, Actions.sequence(
            Actions.run(() -> setReel(reel, EnemySymbol.values()[MathUtils.random(EnemySymbol.values().length - 1)])),
            Actions.delay(SPIN_FRAME)))));
    }

    /** Le rouleau {@code reel} s'arrête sur {@code symbol}, qui agit : ses textes surgissent au-dessus. */
    private void stopReel(int reel, EnemySymbol symbol, List<Event> events, Consumer<Event> onEventShown) {
        Image image = reels.get(reel);
        image.clearActions();
        setReel(reel, symbol);
        float baseY = table.getEnemyReelRowY() + (SlotView.CELL_HEIGHT - SYMBOL_SIZE) / 2f;
        image.setY(baseY - 8f);
        image.addAction(Actions.moveTo(image.getX(), baseY, 0.18f, Interpolation.swingOut));
        reelStopSound.play();
        if (symbol == EnemySymbol.SWORD) lunge();

        List<EffectPopup> texts = new ArrayList<>();
        events.forEach(event -> texts.addAll(event.getPopups()));
        float centerX = table.getReelRowX() + EnemySlotMachine.SYMBOL_COUNT * SlotView.CELL_WIDTH / 2f;
        popups.play(texts, centerX + (reel - 1) * RESULT_SPREAD, table.getDividerY() - RESULT_BELOW);
        for (Event event : events) {
            if (event instanceof EnemyShieldedEvent shield) {
                shownDefense += shield.defense;
                refreshDefense();
                pulse(Color.valueOf("6ab0ffff"));
            }
            onEventShown.accept(event);
        }
    }

    private void setReel(int reel, EnemySymbol symbol) {
        shownSymbols[reel] = symbol;
        ((TextureRegionDrawable) reels.get(reel).getDrawable()).setRegion(new TextureRegion(textures.symbol(symbol)));
    }

    /** Toutes les cartes du tour filent vers la défausse de l'ennemi. */
    private void discardHand() {
        for (int i = 0; i < handCards.size(); i++) {
            Image card = handCards.get(i);
            card.clearListeners();
            card.addAction(Actions.sequence(
                Actions.delay(i * DISCARD_STAGGER),
                Actions.alpha(1f),
                Actions.parallel(
                    Actions.moveTo(discardPile.getTopX(), discardPile.getTopY(), DISCARD_TIME, Interpolation.pow2In),
                    Actions.rotateBy(MathUtils.random(-20f, 20f), DISCARD_TIME)),
                Actions.run(() -> {
                    shownDiscard++;
                    refreshCounts();
                }),
                Actions.removeActor()));
        }
        handCards.clear();
    }

    /** Le compte des piles et la défense reprennent les valeurs de l'ennemi (fin de son tour). */
    private void syncWithModel() {
        Enemy current = enemy.get();
        shownDeck    = current.getDeckCards().size();
        shownDiscard = current.getDiscardCards().size();
        shownDefense = current.getDefense();
        refreshCounts();
        refreshDefense();
    }

    private void refreshCounts() {
        deckPile.setCount(shownDeck);
        discardPile.setCount(shownDiscard);
    }

    private void refreshDefense() {
        defenseLabel.setText("Défense " + shownDefense);
        defenseLabel.setColor(shownDefense > Enemy.BASE_DEFENSE ? Palette.SKY : Palette.TEXT_BODY);
        defenseLabel.pack();
        defenseLabel.setPosition(defenseIcon.getX() + defenseIcon.getWidth() + 8f,
            defenseIcon.getY() + (defenseIcon.getHeight() - defenseLabel.getHeight()) / 2f);
    }

    // -------------------------------------------------------------------------
    // Réactions du croupier
    // -------------------------------------------------------------------------

    /** Il est touché : sa silhouette flashe en blanc et il tremble. */
    public void hit() {
        flash.clearActions();
        flash.setColor(Color.WHITE);
        flash.addAction(Actions.sequence(Actions.alpha(0.9f), Actions.fadeOut(0.2f)));
        croupier.clearActions();
        croupier.setX(0f);
        croupier.addAction(Actions.sequence(
            Actions.moveBy(6f, 0f, 0.04f), Actions.moveBy(-12f, 0f, 0.06f), Actions.moveBy(6f, 0f, 0.04f)));
    }

    /** Il se soigne : sa silhouette luit en vert. */
    public void heal() {
        pulse(Color.valueOf("7dff8aff"));
    }

    /** Vaincu : il bascule en arrière et disparaît. */
    public void defeat() {
        character.clearActions();
        character.addAction(Actions.parallel(
            Actions.rotateBy(-25f, 0.8f, Interpolation.pow2In),
            Actions.moveBy(0f, -30f, 0.8f, Interpolation.pow2In),
            Actions.fadeOut(0.8f)));
    }

    /** Sa silhouette luit un instant de {@code color}. */
    private void pulse(Color color) {
        flash.clearActions();
        flash.setColor(color.r, color.g, color.b, 0f);
        flash.addAction(Actions.sequence(Actions.alpha(0.6f, 0.12f), Actions.fadeOut(0.5f)));
    }

    /** Il hoche la tête en jouant une carte. */
    private void nod() {
        croupier.addAction(Actions.sequence(Actions.moveBy(0f, -5f, 0.08f), Actions.moveBy(0f, 5f, 0.14f)));
    }

    /** Il se penche vers le joueur pour frapper (Épée). */
    private void lunge() {
        croupier.addAction(Actions.sequence(
            Actions.moveBy(18f, -14f, 0.08f, Interpolation.pow2Out),
            Actions.moveBy(-18f, 14f, 0.22f, Interpolation.pow2In)));
    }

    /** @return le point (Stage) juste au-dessus de la tête du croupier. */
    private Vector2 characterTop() {
        return new Vector2(character.getX() + character.getWidth() / 2f,
            Math.min(character.getY() + character.getHeight() + POPUP_GAP, table.getFeltTop()));
    }

    /** Survoler {@code actor} affiche son infobulle ({@code heading}, {@code text}) au-dessus de lui. */
    private void hoverTooltip(Actor actor, Supplier<String> heading, Supplier<String> text) {
        actor.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer != -1) return;
                Vector2 top = actor.localToStageCoordinates(new Vector2(actor.getWidth() / 2f, actor.getHeight()));
                tooltip.show(heading.get(), text.get(), top.x, top.y + 6f);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) tooltip.hide();
            }
        });
    }

    @Override
    public void dispose() {
        Fonts.release(defenseFont);
    }
}
