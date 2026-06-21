package fr.astratime.lucky;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.List;

public class GameScreen extends ScreenAdapter {

    private final LuckyGame luckyGame;
    private final Stage stage;
    private final Deck deck = new Deck();
    private final Label cardsLabel;

    public GameScreen(LuckyGame luckyGame) {
        this.luckyGame = luckyGame;
        this.stage = new Stage(new ScreenViewport(), luckyGame.getBatch());

        BitmapFont font = new BitmapFont(); // police par défaut, suffisant pour démarrer

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.up = makeColorDrawable(Color.DARK_GRAY);
        buttonStyle.down = makeColorDrawable(Color.GRAY);

        TextButton drawButton = new TextButton("Tirer 3 cartes", buttonStyle);
        drawButton.setPosition(20, 20);
        drawButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                drawThreeCards();
            }
        });

        cardsLabel = new Label("", new Label.LabelStyle(font, Color.WHITE));
        cardsLabel.setPosition(20, 80);

        stage.addActor(drawButton);
        stage.addActor(cardsLabel);
    }

    private void drawThreeCards() {
        List<Card> hand = deck.draw(3);

        StringBuilder text = new StringBuilder();
        for (Card card : hand) {
            text.append(card).append("\n");
        }
        cardsLabel.setText(text.toString());
        System.out.println(deck.getCards().size());
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
    }
}
