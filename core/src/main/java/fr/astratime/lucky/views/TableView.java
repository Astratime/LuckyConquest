package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import fr.astratime.lucky.animations.GlowBorder;
import fr.astratime.lucky.animations.MarqueeLights;
import fr.astratime.lucky.animations.RainbowBorder;
import fr.astratime.lucky.assets.TableTextures;
import fr.astratime.lucky.entities.Player;
import fr.astratime.lucky.entities.SlotMachine;

import java.util.ArrayList;
import java.util.List;

/**
 * Table de casino, dans la zone de jeu : rebord en cuir garni d'une guirlande
 * d'ampoules en chenillard, feutre vert éclairé comme sous une lampe, et les
 * emplacements imprimés où se posent les éléments du joueur. De bas en haut :
 * le deck (à gauche) et la défausse (à droite) sur leur tapis, la machine à
 * sous (un rouleau par symbole), la rangée des cartes de la main
 * ({@link Player#MAX_HAND_SIZE} emplacements), puis un filet doré au-delà
 * duquel le côté adverse de la table reste libre. Lors d'un jackpot, les
 * rouleaux prennent une bordure arc-en-ciel animée.
 *
 * La table est assemblée à partir de pièces étirables ou répétées (et non
 * d'une seule image) : elle s'adapte à la taille de l'écran et reste alignée
 * avec les éléments posés dessus. Elle est la référence de cette géométrie :
 * la main, les symboles et les piles se placent aux positions qu'elle donne.
 */
public class TableView {

    // Table dans la zone de jeu
    private static final float SIDE_MARGIN  = 30f;
    private static final float BOTTOM       = 100f;  // sous la table : boutons et barre de vie du joueur
    private static final float TOP_MARGIN   = 80f;   // au-dessus : barre de vie de l'ennemi
    private static final float RAIL         = 48f;   // épaisseur du rebord
    private static final float FELT_OVERLAP = 3f;    // le feutre passe sous le rebord

    // Tapis du deck et de la défausse
    private static final float PILE_MARGIN     = 20f;  // entre le rebord et le tapis
    private static final float MAT_PAD         = 16f;  // entre le bord du tapis et la pile
    private static final float MAT_LABEL_SPACE = 50f;  // au-dessus de la pile, pour son nom
    private static final float PILE_STACK      = 9f;   // épaisseur de la pile (dos décalés)

    // Machine à sous et rangée de cartes
    private static final float REEL_BOTTOM_GAP = 24f;  // entre le rebord et la machine
    private static final float REEL_FRAME_PAD  = 18f;  // carrosserie autour des rouleaux
    private static final float HAND_GAP        = 40f;  // entre la machine et la rangée de cartes
    private static final float CARD_GAP        = 20f;  // entre deux cartes de la rangée
    private static final float SLOT_OUTLINE    = 4f;   // contour imprimé autour d'un emplacement

    // Séparation avec le côté adverse
    private static final float DIVIDER_GAP  = 60f;     // au-dessus de la rangée de cartes
    private static final float DIVIDER_SIDE = 60f;     // retrait du filet par rapport au rebord

    private final PlayArea playArea;
    private final float    cardWidth;
    private final float    cardHeight;

    private final Group               group = new Group();
    private final Image               felt;
    private final Image               rail;
    private final Image               feltLight;
    private final MarqueeLights       lights;
    private final Image               dividerLine;
    private final Image               dividerEmblem;
    private final Image               deckMat;
    private final Image               discardMat;
    private final Image               reelFrame;
    private final List<Image>         reelCells    = new ArrayList<>();
    private final List<RainbowBorder> reelRainbows = new ArrayList<>();
    private final List<GlowBorder>    reelGlows    = new ArrayList<>();
    private final List<Image>         cardSlots    = new ArrayList<>();

    public TableView(PlayArea playArea, TableTextures textures, float cardWidth, float cardHeight) {
        this.playArea   = playArea;
        this.cardWidth  = cardWidth;
        this.cardHeight = cardHeight;
        group.setTouchable(Touchable.disabled); // décor : laisse passer les clics

        felt          = add(new Image(textures.feltDrawable()));
        feltLight     = add(new Image(new TextureRegionDrawable(new TextureRegion(textures.feltLight))));
        dividerLine   = add(new Image(new TextureRegionDrawable(new TextureRegion(textures.feltLine))));
        dividerEmblem = add(new Image(new TextureRegionDrawable(new TextureRegion(textures.feltEmblem))));
        for (int i = 0; i < Player.MAX_HAND_SIZE; i++) {
            cardSlots.add(add(new Image(textures.cardSlotDrawable())));
        }
        deckMat    = add(new Image(textures.pileMatDrawable()));
        discardMat = add(new Image(textures.pileMatDrawable()));
        reelFrame  = add(new Image(textures.reelFrameDrawable()));
        for (int i = 0; i < SlotMachine.SYMBOL_COUNT; i++) {
            reelCells.add(add(new Image(textures.reelCellDrawable())));
        }
        for (int i = 0; i < SlotMachine.SYMBOL_COUNT; i++) {
            RainbowBorder rainbow = new RainbowBorder(new TextureRegion(textures.pixel));
            rainbow.setVisible(false);
            group.addActor(rainbow);
            reelRainbows.add(rainbow);
            GlowBorder glow = new GlowBorder(new TextureRegion(textures.pixel), Color.WHITE);
            group.addActor(glow);
            reelGlows.add(glow);
        }
        rail = add(new Image(textures.railDrawable())); // par-dessus le bord du feutre
        lights = new MarqueeLights(textures.bulbs, textures.bulbGlow);
        group.addActor(lights); // sur le rebord
        layout();
    }

    private Image add(Image image) {
        group.addActor(image);
        return image;
    }

    /** @return la table, à ajouter au Stage avant tout ce qui se pose dessus. */
    public Group getActor() { return group; }

    /** Redimensionne la table et replace ses emplacements (après un redimensionnement). */
    public void layout() {
        float x = getX();
        float width = playArea.getWidth() - SIDE_MARGIN * 2;
        float height = playArea.getHeight() - BOTTOM - TOP_MARGIN;
        rail.setBounds(x, BOTTOM, width, height);
        felt.setBounds(x + RAIL - FELT_OVERLAP, BOTTOM + RAIL - FELT_OVERLAP,
            width - (RAIL - FELT_OVERLAP) * 2, height - (RAIL - FELT_OVERLAP) * 2);
        feltLight.setBounds(felt.getX(), felt.getY(), felt.getWidth(), felt.getHeight());
        lights.setBounds(x + RAIL / 2f, BOTTOM + RAIL / 2f, width - RAIL, height - RAIL);

        float matWidth  = cardWidth + PILE_STACK + MAT_PAD * 2;
        float matHeight = MAT_PAD + cardHeight + PILE_STACK + MAT_LABEL_SPACE;
        deckMat.setBounds(getDeckX() - MAT_PAD, getPilesY() - MAT_PAD, matWidth, matHeight);
        discardMat.setBounds(2 * playArea.getCenterX() - deckMat.getX() - matWidth, deckMat.getY(),
            matWidth, matHeight);

        reelFrame.setBounds(getReelRowX() - REEL_FRAME_PAD, getReelRowY() - REEL_FRAME_PAD,
            reelRowWidth() + REEL_FRAME_PAD * 2, SlotView.CELL_HEIGHT + REEL_FRAME_PAD * 2);
        for (int i = 0; i < reelCells.size(); i++) {
            reelCells.get(i).setBounds(getReelRowX() + i * SlotView.CELL_WIDTH, getReelRowY(),
                SlotView.CELL_WIDTH, SlotView.CELL_HEIGHT);
            reelRainbows.get(i).setBounds(reelCells.get(i).getX(), reelCells.get(i).getY(),
                SlotView.CELL_WIDTH, SlotView.CELL_HEIGHT);
            reelGlows.get(i).setBounds(reelCells.get(i).getX(), reelCells.get(i).getY(),
                SlotView.CELL_WIDTH, SlotView.CELL_HEIGHT);
        }

        for (int i = 0; i < cardSlots.size(); i++) {
            cardSlots.get(i).setBounds(getHandSlotX(i) - SLOT_OUTLINE, getHandRowY() - SLOT_OUTLINE,
                cardWidth + SLOT_OUTLINE * 2, cardHeight + SLOT_OUTLINE * 2);
        }

        float dividerY = getHandRowY() + cardHeight + DIVIDER_GAP;
        float lineX    = x + RAIL + DIVIDER_SIDE;
        dividerLine.setBounds(lineX, dividerY - dividerLine.getPrefHeight() / 2f,
            width - (RAIL + DIVIDER_SIDE) * 2, dividerLine.getPrefHeight());
        dividerEmblem.setBounds(playArea.getCenterX() - dividerEmblem.getPrefWidth() / 2f,
            dividerY - dividerEmblem.getPrefHeight() / 2f,
            dividerEmblem.getPrefWidth(), dividerEmblem.getPrefHeight());
    }

    /** Mode fête des ampoules du rebord (jackpot, victoire) : elles clignotent toutes, ou reprennent le chenillard. */
    public void setLightsParty(boolean party) {
        lights.setParty(party);
    }

    /** Affiche (jackpot) ou cache la bordure arc-en-ciel animée autour des rouleaux. */
    public void setReelsRainbow(boolean shown) {
        reelRainbows.forEach(rainbow -> rainbow.setVisible(shown));
    }

    /**
     * Entoure le rouleau {@code reel} d'un contour lumineux de {@code color}, qui
     * pulse à {@code pulseSpeed} radians par seconde (suspense, paire).
     *
     * @param duration durée avant qu'il s'éteigne seul, en secondes (0 : jusqu'à {@link #clearReelHighlight})
     */
    public void highlightReel(int reel, Color color, float pulseSpeed, float duration) {
        GlowBorder glow = reelGlows.get(reel);
        glow.clearActions();
        glow.setGlow(color, pulseSpeed);
        glow.setVisible(true);
        if (duration > 0f) glow.addAction(Actions.delay(duration, Actions.visible(false)));
    }

    /** Éteint le contour lumineux du rouleau {@code reel}. */
    public void clearReelHighlight(int reel) {
        reelGlows.get(reel).clearActions();
        reelGlows.get(reel).setVisible(false);
    }

    /** @return l'abscisse (Stage) du deck, posé sur son tapis à gauche ; la défausse est son symétrique. */
    public float getDeckX() { return getX() + RAIL + PILE_MARGIN + MAT_PAD; }

    /** @return l'ordonnée (Stage) du bas du deck et de la défausse. */
    public float getPilesY() { return BOTTOM + RAIL + PILE_MARGIN + MAT_PAD; }

    /** @return l'abscisse (Stage) du premier rouleau de la machine à sous. */
    public float getReelRowX() { return playArea.getCenterX() - reelRowWidth() / 2f; }

    /** @return l'ordonnée (Stage) du bas des rouleaux. */
    public float getReelRowY() { return BOTTOM + RAIL + REEL_BOTTOM_GAP + REEL_FRAME_PAD; }

    /** @return l'ordonnée (Stage) du bas de la rangée de cartes. */
    public float getHandRowY() {
        return getReelRowY() + SlotView.CELL_HEIGHT + REEL_FRAME_PAD + HAND_GAP;
    }

    /** @return l'abscisse (Stage) de l'emplacement de carte {@code slot} (0 à gauche). */
    public float getHandSlotX(int slot) {
        int   slots    = Player.MAX_HAND_SIZE;
        float rowWidth = slots * cardWidth + (slots - 1) * CARD_GAP;
        return playArea.getCenterX() - rowWidth / 2f + slot * (cardWidth + CARD_GAP);
    }

    private float getX() { return playArea.getX() + SIDE_MARGIN; }

    private static float reelRowWidth() { return SlotMachine.SYMBOL_COUNT * SlotView.CELL_WIDTH; }
}
