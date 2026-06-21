package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Deck;
import fr.astratime.lucky.entities.DiscardPile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameScreen extends ScreenAdapter {

    private static final String THEME = "light"; // ou "dark"
    private static final float CARD_WIDTH = 95f;  // taille affichée = moitié de la taille source (190x270)
    private static final float CARD_HEIGHT = 135f;
    private static final String BACKGROUND_PATH = "playTable/play_table1.png"; // à adapter au nom réel du fichier ajouté

    private final LuckyGame luckyGame;
    private final Stage stage;
    private final DiscardPile discardPile = new DiscardPile();
    private final Deck deck = new Deck(discardPile);
    private final Table cardTable = new Table();
    private final Map<String, Texture> cardTextures = new HashMap<>();
    private List<Card> currentHand = new ArrayList<>();
    private final Texture backgroundTexture;
    private final Image background;

    public GameScreen(LuckyGame luckyGame) {
        this.luckyGame = luckyGame;
        this.stage = new Stage(new ScreenViewport(), luckyGame.getBatch());

        // Fond d'écran : ajouté en premier sur le stage => dessiné derrière tout le reste.
        backgroundTexture = new Texture(Gdx.files.internal(BACKGROUND_PATH));
        background = new Image(new TextureRegionDrawable(new TextureRegion(backgroundTexture)));
        background.setSize(stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        stage.addActor(background);

        BitmapFont font = new BitmapFont(); // police par défaut, suffisant pour démarrer

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.up = makeColorDrawable(Color.DARK_GRAY);
        buttonStyle.down = makeColorDrawable(Color.GRAY);

        TextButton drawButton = new TextButton("Tirer 3 cartes", buttonStyle);
        drawButton.setSize(150f,60f);
        drawButton.setPosition(20, 20);
        drawButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                drawThreeCards();
            }
        });

        // Le Table se redimensionne tout seul (pack()) ; on le recentre à chaque tirage.
        stage.addActor(drawButton);
        stage.addActor(cardTable);
    }

    private void drawThreeCards() {
        // La main du tour précédent part à la défausse avant de repiocher.
        discardPile.addAll(currentHand);
        currentHand = deck.draw(3);

        cardTable.clearChildren();
        for (Card card : currentHand) {
            Texture texture = getCardTexture(card);
            Image cardImage = new Image(new TextureRegionDrawable(new TextureRegion(texture)));
            cardTable.add(cardImage).size(CARD_WIDTH, CARD_HEIGHT).pad(10f);
        }
        cardTable.pack();

        float worldWidth = stage.getViewport().getWorldWidth();
        cardTable.setPosition((worldWidth - cardTable.getWidth()) / 2f, 350f);

        System.out.println("Deck: " + deck.getCards().size() + " | Discard pile: " + discardPile.size());
    }

    /**
     * Charge (ou récupère depuis le cache) la texture correspondant à une carte,
     * pour éviter de recharger 52 fois le même fichier depuis le disque.
     */
    private Texture getCardTexture(Card card) {
        String path = card.getAssetPath(THEME);
        return cardTextures.computeIfAbsent(path, p -> new Texture(Gdx.files.internal(p)));
    }

    private TextureRegionDrawable makeColorDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        background.setSize(stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLUE);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
        for (Texture texture : cardTextures.values()) {
            texture.dispose();
        }
    }
}
