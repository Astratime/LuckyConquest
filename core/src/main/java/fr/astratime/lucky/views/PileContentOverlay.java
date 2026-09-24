package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Textures;
import fr.astratime.lucky.entities.Card;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Voile gris foncé semi-transparent posé par-dessus tout l'écran de jeu, qui
 * affiche le contenu d'une pile (deck ou défausse).
 *
 * Les cartes sont triées (suite, puis rang, cartes spéciales à la fin) et non
 * affichées dans leur ordre de pioche : le joueur voit ce qu'il reste, sans
 * pouvoir anticiper la prochaine carte. Un clic n'importe où (ou Échap, géré
 * par l'appelant via {@link #hide()}) referme le voile.
 */
public class PileContentOverlay implements Disposable {

    private static final Color BACKGROUND_COLOR = new Color(0.08f, 0.08f, 0.08f, 0.85f);
    private static final float MARGIN   = 60f;
    private static final float CARD_PAD = 8f;
    private static final int   MAX_COLUMNS = 10;

    /** Suite (ordre de l'enum, cartes sans suite à la fin), puis rang, puis nom. */
    private static final Comparator<Card> DISPLAY_ORDER = Comparator
        .comparingInt((Card c) -> c.getSuit() == null ? Integer.MAX_VALUE : c.getSuit().ordinal())
        .thenComparingInt(Card::getRank)
        .thenComparing(Card::getName);

    private final Stage                   stage;
    private final Tooltip                 tooltip;
    private final Function<Card, Texture> textureForCard;
    private final float                   cardWidth;
    private final float                   cardHeight;

    private final Texture backgroundTexture;
    private final Group   root = new Group();
    private final Image   background;
    private final Label   title;
    private final Label   hint;
    private final Table   grid = new Table();
    private final ScrollPane scrollPane;

    /**
     * @param stage          Stage de l'écran de jeu (l'acteur racine y est ajouté par l'appelant via {@link #getActor()})
     * @param tooltip        infobulle partagée, pour afficher la description d'une carte au survol
     * @param textureForCard fournit la texture (mise en cache par l'appelant) de chaque carte
     */
    public PileContentOverlay(Stage stage, BitmapFont font, Tooltip tooltip,
                              Function<Card, Texture> textureForCard, float cardWidth, float cardHeight) {
        this.stage          = stage;
        this.tooltip        = tooltip;
        this.textureForCard = textureForCard;
        this.cardWidth      = cardWidth;
        this.cardHeight     = cardHeight;

        backgroundTexture = Textures.solidColor(Color.WHITE);

        background = new Image(new TextureRegionDrawable(new TextureRegion(backgroundTexture)));
        background.setColor(BACKGROUND_COLOR);

        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        title = new Label("", style);
        title.setFontScale(1.5f);
        hint  = new Label("Cliquer n'importe ou (ou Echap) pour fermer", new Label.LabelStyle(font, Color.LIGHT_GRAY));

        scrollPane = new ScrollPane(grid);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        root.addActor(background);
        root.addActor(title);
        root.addActor(scrollPane);
        root.addActor(hint);
        root.setVisible(false);
        root.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });
    }

    /** @return l'acteur racine du voile, à ajouter au Stage au-dessus des éléments de jeu. */
    public Group getActor() { return root; }

    /** @return {@code true} si le voile est actuellement affiché. */
    public boolean isShown() { return root.isVisible(); }

    /**
     * Affiche le voile avec {@code cards}, triées pour ne pas révéler leur ordre.
     *
     * @param name nom de la pile, affiché en titre avec le nombre de cartes
     */
    public void show(String name, List<Card> cards) {
        List<Card> sorted = new ArrayList<>(cards);
        sorted.sort(DISPLAY_ORDER);

        float worldWidth  = stage.getViewport().getWorldWidth();
        float worldHeight = stage.getViewport().getWorldHeight();

        background.setSize(worldWidth, worldHeight);

        title.setText(name + " : " + cards.size() + " carte(s)");
        title.pack();
        title.setPosition((worldWidth - title.getWidth()) / 2f, worldHeight - MARGIN - title.getHeight());

        hint.pack();
        hint.setPosition((worldWidth - hint.getWidth()) / 2f, MARGIN / 2f);

        grid.clearChildren();
        float cellWidth = cardWidth + CARD_PAD * 2;
        int columns = Math.clamp((int) ((worldWidth - MARGIN * 2) / cellWidth), 1, MAX_COLUMNS);
        if (sorted.isEmpty()) {
            grid.add(new Label("Aucune carte", new Label.LabelStyle(hint.getStyle().font, Color.WHITE)));
        }
        for (int i = 0; i < sorted.size(); i++) {
            Card card = sorted.get(i);
            Image image = new Image(new TextureRegionDrawable(new TextureRegion(textureForCard.apply(card))));
            addTooltipListener(image, card);
            grid.add(image).size(cardWidth, cardHeight).pad(CARD_PAD);
            if ((i + 1) % columns == 0) grid.row();
        }
        grid.pack();

        float paneTop    = title.getY() - MARGIN / 2f;
        float paneBottom = hint.getY() + hint.getHeight() + MARGIN / 2f;
        float paneWidth  = Math.min(grid.getPrefWidth(), worldWidth - MARGIN * 2);
        float paneHeight = Math.min(grid.getPrefHeight(), paneTop - paneBottom);
        scrollPane.setSize(paneWidth, paneHeight);
        scrollPane.setPosition((worldWidth - paneWidth) / 2f, paneTop - paneHeight);
        scrollPane.layout();
        scrollPane.setScrollY(0f);

        tooltip.hide();
        root.setVisible(true);
        root.toFront();
        tooltip.getActor().toFront(); // l'infobulle reste au-dessus du voile
        stage.setScrollFocus(scrollPane);
    }

    /** Referme le voile. */
    public void hide() {
        root.setVisible(false);
        tooltip.hide();
        if (stage.getScrollFocus() == scrollPane) stage.setScrollFocus(null);
    }

    private void addTooltipListener(Image image, Card card) {
        image.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer != -1) return;
                Vector2 pos = image.localToStageCoordinates(new Vector2(0, cardHeight + 5f));
                tooltip.show(card.getName() + "\n" + card.getDescription(), pos.x, pos.y);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer != -1) return;
                tooltip.hide();
            }
        });
    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
    }
}
