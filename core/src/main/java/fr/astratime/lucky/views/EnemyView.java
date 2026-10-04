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
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.animations.EffectPopupAnimator;
import fr.astratime.lucky.assets.EnemyTextures;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.enemy.EnemyCards;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySlotMachine;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.entities.enemy.EnemyTurnResult;
import fr.astratime.lucky.entities.events.EnemyDamagedEvent;
import fr.astratime.lucky.entities.events.EnemyPhaseEvent;
import fr.astratime.lucky.entities.events.EnemyShieldedEvent;
import fr.astratime.lucky.entities.events.Event;
import fr.astratime.lucky.entities.events.PlayerDamagedEvent;
import fr.astratime.lucky.popups.EffectPopup;
import fr.astratime.lucky.popups.PopupScale;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Côté de l'ennemi, sur la table (voir {@link TableView}) : son portrait (le
 * croupier démoniaque, ou un ennemi de la Tour des épreuves), sa machine à
 * sous, ses cartes, son deck et sa défausse, et sa défense. Joue en animation
 * le tour de l'ennemi déjà résolu ({@link #playTurn}) : ses Épines piquent
 * d'abord, puis il pioche ses cartes face cachée, retourne celles qu'il joue
 * (avec leur bonus), lance ses rouleaux (chaque symbole agit à son arrêt), puis
 * toutes ses cartes filent dans sa défausse.
 *
 * Survoler l'ennemi, un rouleau ou une carte jouée affiche son effet.
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
    private static final float OPENING_TIME   = 0.9f;   // ses Épines piquent, avant sa pioche

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
    private final ShieldBadge   defense;
    private final CardPileView  deckPile;
    private final CardPileView  discardPile;
    private final List<Image>   reels     = new ArrayList<>();
    private final EnemySymbol[] shownSymbols = {EnemySymbol.SWORD, EnemySymbol.SHIELD, EnemySymbol.POTION,
        EnemySymbol.SWORD, EnemySymbol.SHIELD, EnemySymbol.POTION};
    /** Rouleaux affichés (3, sauf la Machine Originelle : 5 puis 6). */
    private int shownReels = EnemySlotMachine.SYMBOL_COUNT;
    /** Portrait affiché (il change avec l'ennemi du combat). */
    private EnemyKind shownKind = EnemyKind.CROUPIER;
    /** Phase de l'ennemi dont les symboles sont sur les rouleaux au repos. */
    private int shownPhase = 1;
    private final List<Image>   handCards = new ArrayList<>();
    private int shownDeck;
    private int shownDiscard;
    /** Boucle du bruit de ses rouleaux pendant son lancer (-1 : aucune). */
    private long spinSoundId = -1;

    /**
     * @param reelSpinSound boucle jouée tant que ses rouleaux tournent
     * @param reelStopSound bruitage joué quand un de ses rouleaux s'arrête
     * @param enemy         l'ennemi du combat en cours (il change à chaque nouveau combat)
     * @param pixel         texture d'un pixel blanc (trait qui barre sa défense percée)
     */
    public EnemyView(TableView table, EnemyTextures textures, BitmapFont pileFont, Tooltip tooltip,
                     EffectPopupAnimator popups, Sound cardSound, Sound flipSound, Sound reelSpinSound,
                     Sound reelStopSound, Supplier<Enemy> enemy, Texture pixel) {
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
        hoverTooltip(croupier, () -> enemy.get().getName(), this::describeEnemy);

        deckPile    = new CardPileView("DECK", textures.cardBack, pileFont,
            table.getCardWidth(), table.getCardHeight());
        discardPile = new CardPileView("DÉFAUSSE", textures.cardBack, pileFont,
            table.getCardWidth(), table.getCardHeight());

        for (int i = 0; i < EnemySlotMachine.MAX_SYMBOL_COUNT; i++) {
            int reel = i;
            Image image = new Image(new TextureRegionDrawable(new TextureRegion(textures.symbol(shownSymbols[i]))));
            image.setSize(SYMBOL_SIZE, SYMBOL_SIZE); // réduit dans layout() quand ils sont plus de trois
            hoverTooltip(image, () -> shownSymbols[reel].getDisplayName(),
                () -> shownSymbols[reel].getDescription(shownKind));
            reels.add(image);
        }

        defense = new ShieldBadge("Défense", enemy.get().getBaseDefense(), textures.symbol(EnemySymbol.SHIELD), defenseFont, pixel);
        hoverTooltip(defense, () -> "Défense de l'ennemi",
            () -> "Absorbe les dégâts de tes attaques et s'use à chaque coup\n"
                + "Les Piques l'ignorent\nSe reforme à son tour, avec ses Boucliers");

        group.addActor(deckPile);
        group.addActor(discardPile);
        reels.forEach(group::addActor);
        group.addActor(defense);
        group.addActor(character);
        reset();
    }

    /** @return le côté de l'ennemi, à ajouter au Stage au-dessus de la table. */
    public Group getActor() { return group; }

    /**
     * @return l'infobulle de l'ennemi : sa présentation, sa défense, ses
     *         rouleaux, ses cartes, sa Rage et ses Épines en cours
     */
    private String describeEnemy() {
        Enemy current = enemy.get();
        EnemyKind kind = current.getKind();
        StringBuilder text = new StringBuilder(kind.getDescription());
        if (kind.getPower() > 100) text.append("\nForce ").append(kind.powerText()).append(" : attaques, défense et effets");
        text.append("\nDéfense : ").append(shownDefense());
        text.append("\nRouleaux : ").append(kind.describeReels(current.getPhase()));
        if (kind.hasPhaseTwo() && current.getPhase() < 2) {
            text.append("\nSous la moitié de ses PV : ").append(kind.describeReels(2));
        }
        if (kind.playsAtRandom()) text.append("\nJoue ses cartes au hasard");
        if (kind.potionShields()) text.append("\nSes Potions reforment aussi sa défense");
        if (kind.royalBet()) text.append("\nMise royale : ses Trèfles comptent double sous la moitié de ses PV");
        if (kind.forbidsReels()) text.append("\nRouleau interdit : bloque un de tes rouleaux. Pas de Bingo possible");
        if (kind.hasGoldSkin()) text.append("\nPeau d'or : tes dégâts sont divisés par 2 tant qu'il a plus de la moitié de ses PV");
        if (kind.hitsJackpots()) {
            text.append("\nJackpot : 3 symboles identiques, attaques et Boucliers x").append(EnemySymbol.JACKPOT_FACTOR);
        }
        text.append("\nPioche ").append(Enemy.HAND_SIZE).append(" cartes et en joue ").append(current.getPlaysPerTurn())
            .append(" à chaque tour");
        if (kind.hasArms()) text.append("\nChaque bras perdu lui retire une carte jouée");
        if (kind == EnemyKind.PRETENDANT) {
            int shards = EnemyKind.shards(current.getPhase());
            text.append("\nÉclats : ").append(shards).append("/3 (Comète");
            if (shards >= 2) text.append(", Reine : Trèfles doublés");
            if (shards >= 3) text.append(", Éclat : Rouleau interdit");
            text.append(")");
        }
        if (kind == EnemyKind.MAISON) {
            text.append("\nDebout : ").append(current.getPhase() <= 1 ? "Façade, " : "")
                .append(current.getPhase() <= 2 ? "Coffre, " : "").append("Salle de jeu");
            if (!current.isHouseUsed()) text.append("\nLa Maison gagne toujours : ton prochain gros coup sera annulé");
        }
        if (kind.hasLastDraw()) {
            text.append("\nRouleaux : ").append(current.getReelCount()).append(". Rouleaux volés : ").append(current.getStolenReels());
        }
        if (current.getBannedFamily() != null) {
            text.append("\nInterdit : les cartes ").append(current.getBannedFamily().getDisplayName());
        }
        if (current.getLoot() > 0) text.append("\nButin : ").append(current.getLoot()).append(" As volés");
        if (current.getPrediction() != null) text.append("\nPrédiction : ").append(current.getPrediction().getDisplayName());
        if (kind.getTurnLimit() > 0) text.append("\nLe combat dure ").append(kind.getTurnLimit()).append(" tours");
        if (current.getAnvil() > 0) text.append("\nEnclume : attaque +").append(current.getAnvil());
        if (current.getRage() > 0) text.append("\nRage : attaque +").append(current.getRage());
        if (current.getThornsPercent() > 0) {
            text.append("\nÉpines : ").append(current.getThornsPercent()).append(" % de tes dégâts te reviendront");
        }
        if (current.getInterest() > 0) text.append("\nIntérêts : prochain coup +").append(current.getInterest());
        if (kind.getSymbols(2).contains(EnemySymbol.HOURGLASS) || kind.getSymbols().contains(EnemySymbol.HOURGLASS)) {
            text.append("\nSablier : ").append(current.getHourglass()).append("/").append(EnemySymbol.HOURGLASS_MAX);
        }
        if (current.getStake() > 1) text.append("\nMise : attaques x").append(current.getStake());
        if (current.isAllIn()) text.append("\nTapis posé : sa mise doublera à son tour, sauf si tu le touches");
        return text.toString();
    }

    /** Montre le portrait de l'ennemi du combat en cours, et ses symboles au repos sur les rouleaux. */
    private void showKind() {
        Enemy current = enemy.get();
        EnemyKind kind = current.getKind();
        if (kind == shownKind && current.getPhase() == shownPhase) return;
        shownKind  = kind;
        shownPhase = current.getPhase();
        ((TextureRegionDrawable) croupier.getDrawable()).setRegion(new TextureRegion(textures.portrait(kind)));
        ((TextureRegionDrawable) flash.getDrawable()).setRegion(new TextureRegion(textures.portraitFlash(kind)));
        List<EnemySymbol> symbols = current.getSymbols();
        for (int i = 0; i < reels.size(); i++) setReel(i, symbols.get(i % symbols.size()));
        if (current.getReelCount() != shownReels) {
            shownReels = current.getReelCount();
            table.setEnemyReelCount(shownReels);
            layout();
        }
    }

    /** @return la taille d'un symbole sur ses rouleaux : plus petit quand ils sont plus de trois. */
    private float symbolSize() {
        return Math.min(SYMBOL_SIZE, table.getEnemyCellWidth() - 8f);
    }

    /** Nouveau combat : l'ennemi debout, piles pleines, défense de base, rouleaux au repos. */
    public void reset() {
        showKind();
        stopSpinSound();
        group.clearActions();
        handCards.forEach(Actor::remove);
        handCards.clear();
        character.clearActions();
        character.setRotation(0f);
        character.getColor().a = 1f;
        flash.clearActions();
        flash.getColor().a = 0f;
        defense.setValue(enemy.get().getBaseDefense());
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
        Texture portrait = textures.portrait(shownKind);
        int   scale = Math.clamp((int) (room / portrait.getHeight()), 2, EnemyTextures.CROUPIER_SCALE);
        croupier.setSize(portrait.getWidth() * scale, portrait.getHeight() * scale);
        flash.setSize(croupier.getWidth(), croupier.getHeight());
        character.setSize(croupier.getWidth(), croupier.getHeight());
        character.setOrigin(croupier.getWidth() / 2f, 0f);
        character.setPosition(table.getEnemyCharacterCenterX() - character.getWidth() / 2f, table.getEnemyCharacterY());
        idle();
        deckPile.setPosition(table.getEnemyDeckX(), table.getEnemyPilesY());
        discardPile.setPosition(table.getEnemyDiscardX(), table.getEnemyPilesY());
        float size = symbolSize(), cell = table.getEnemyCellWidth();
        float padX = (cell - size) / 2f, padY = (SlotView.CELL_HEIGHT - size) / 2f;
        for (int i = 0; i < reels.size(); i++) {
            reels.get(i).setSize(size, size);
            reels.get(i).setPosition(table.getReelRowX() + i * cell + padX, table.getEnemyReelRowY() + padY);
            reels.get(i).setVisible(i < shownReels);
        }
        float reelsRight = table.getReelRowX() + EnemySlotMachine.SYMBOL_COUNT * SlotView.CELL_WIDTH;
        float centerY    = table.getEnemyReelRowY() + SlotView.CELL_HEIGHT / 2f;
        defense.setPosition(reelsRight + DEFENSE_GAP, centerY - defense.getHeight() / 2f);
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
        if (turn.openingEvents().isEmpty()) {
            playCardsAndReels(turn, onEventShown, onDone);
            return;
        }
        // Ses Épines piquent d'abord, au-dessus de lui.
        Vector2 top = characterTop();
        List<EffectPopup> texts = new ArrayList<>();
        turn.openingEvents().forEach(event -> texts.addAll(event.getPopups()));
        popups.play(texts, top.x, top.y);
        boolean newPhase = turn.openingEvents().stream().anyMatch(event -> event instanceof EnemyPhaseEvent);
        if (newPhase) showKind(); // ses rouleaux prennent les symboles de sa nouvelle phase
        boolean hits = turn.openingEvents().stream().anyMatch(event -> event instanceof PlayerDamagedEvent);
        pulse(Color.valueOf(newPhase ? "ffd54aff" : hits ? "7dd87aff" : "ff4a3aff"));
        turn.openingEvents().forEach(onEventShown);
        group.addAction(Actions.delay(OPENING_TIME, Actions.run(() -> playCardsAndReels(turn, onEventShown, onDone))));
    }

    /** Le reste de son tour : pioche, cartes jouées, rouleaux, suites de ses attaques, défausse. */
    private void playCardsAndReels(EnemyTurnResult turn, Consumer<Event> onEventShown, Runnable onDone) {
        defense.restore(enemy.get().getBaseDefense()); // les Boucliers de son tour précédent ne valent plus

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
                EnemyKind kind = enemy.get().getKind();
                hoverTooltip(card, played::getName, () -> EnemyCards.describe(played, kind));
                popups.play(List.of(new EffectPopup(bonusText(played, kind), styleOf(played), PopupScale.SECONDARY_INTENSITY * 0.7f)),
                    card.getX() + card.getWidth() / 2f, card.getY() - CARD_POPUP_GAP);
            }),
            Actions.scaleTo(1f, 1f, FLIP_TIME, Interpolation.pow2Out)));
    }

    /** @return le bonus d'une carte jouée, en quelques mots (ex : "ÉPÉE +10"), chez un ennemi {@code kind}. */
    private static String bonusText(Card card, EnemyKind kind) {
        if (card.getSuit() == null) return "ROULEAU INTERDIT";
        return switch (card.getSuit()) {
            case PIQUE   -> "ATTAQUE +" + kind.empowered(EnemyCards.swordBonus(card));
            case COEUR   -> "POTION +" + EnemyCards.healBonus(card) + "%";
            case CARREAU -> "BOUCLIER +" + kind.empowered(EnemyCards.shieldBonus(card));
            case TREFLE  -> "CHANCE +" + EnemyCards.luckBonus(card) + "%";
        };
    }

    private static EffectPopup.Style styleOf(Card card) {
        if (card.getSuit() == null) return EffectPopup.Style.DAMAGE;
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
            Actions.run(() -> {
                List<EnemySymbol> symbols = enemy.get().getSymbols();
                setReel(reel, symbols.get(MathUtils.random(symbols.size() - 1)));
            }),
            Actions.delay(SPIN_FRAME)))));
    }

    /** Le rouleau {@code reel} s'arrête sur {@code symbol}, qui agit : ses textes surgissent au-dessus. */
    private void stopReel(int reel, EnemySymbol symbol, List<Event> events, Consumer<Event> onEventShown) {
        Image image = reels.get(reel);
        image.clearActions();
        setReel(reel, symbol);
        float baseY = table.getEnemyReelRowY() + (SlotView.CELL_HEIGHT - symbolSize()) / 2f;
        image.setY(baseY - 8f);
        image.addAction(Actions.moveTo(image.getX(), baseY, 0.18f, Interpolation.swingOut));
        reelStopSound.play();
        if (events.stream().anyMatch(event -> event instanceof PlayerDamagedEvent)) lunge(); // il frappe
        switch (symbol) {
            case RAGE, ALL_IN -> pulse(Color.valueOf("ff4a3aff"));
            case THORNS       -> pulse(Color.valueOf("7dd87aff"));
            case LOADED_DIE   -> pulse(Color.valueOf("c060ffff"));
            case INTEREST, HOURGLASS -> pulse(Color.valueOf("ffd54aff"));
            case MIRROR       -> pulse(Color.valueOf("b8e0ffff"));
            case ZERO         -> pulse(Color.valueOf("2ec060ff"));
            default           -> { }
        }

        List<EffectPopup> texts = new ArrayList<>();
        events.forEach(event -> texts.addAll(event.getPopups()));
        float centerX = table.getReelRowX() + EnemySlotMachine.SYMBOL_COUNT * SlotView.CELL_WIDTH / 2f;
        float spread  = RESULT_SPREAD * (EnemySlotMachine.SYMBOL_COUNT - 1) / Math.max(1, shownReels - 1);
        popups.play(texts, centerX + (reel - (shownReels - 1) / 2f) * spread, table.getDividerY() - RESULT_BELOW);
        for (Event event : events) {
            if (event instanceof EnemyShieldedEvent shield) {
                defense.add(shield.defense);
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
        defense.setValue(current.getDefense());
        refreshCounts();
    }

    private void refreshCounts() {
        deckPile.setCount(shownDeck);
        discardPile.setCount(shownDiscard);
    }

    // -------------------------------------------------------------------------
    // Défense, face aux attaques du joueur
    // -------------------------------------------------------------------------

    /** Ce que fait sa défense face à un coup du joueur. */
    public enum DefenseReaction { NONE, BLOCKED, BROKEN, PIERCED }

    /**
     * Un coup du joueur atteint l'ennemi (à l'apparition de son texte) : sa
     * défense l'absorbe et s'use, se brise, ou est percée (attaque qui l'ignore).
     *
     * @return ce que la défense a fait, pour le bruitage
     */
    public DefenseReaction onPlayerAttack(EnemyDamagedEvent hit) {
        if (hit.pierced) return defense.pierce() ? DefenseReaction.PIERCED : DefenseReaction.NONE;
        if (hit.blocked <= 0) return DefenseReaction.NONE;
        return defense.block(hit.defenseLeft) ? DefenseReaction.BROKEN : DefenseReaction.BLOCKED;
    }

    /** @return la défense affichée (elle suit les textes du tirage). */
    private int shownDefense() { return defense.getValue(); }

    /** @return le centre (Stage) de l'icône de sa défense. */
    public Vector2 getDefenseCenter() {
        return defense.localToStageCoordinates(new Vector2(ShieldBadge.ICON_SIZE / 2f, ShieldBadge.ICON_SIZE / 2f));
    }

    /** @return le centre (Stage) du portrait de l'ennemi, cible des animations d'attaque des symboles. */
    public Vector2 getCroupierCenter() {
        return croupier.localToStageCoordinates(new Vector2(croupier.getWidth() / 2f, croupier.getHeight() / 2f));
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
