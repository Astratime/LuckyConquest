package fr.astratime.lucky;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ControlsScreen extends ScreenAdapter {

    private final LuckyGame luckyGame;
    private final SpriteBatch batch;
    private final Viewport viewport = new ScreenViewport();
    private final Texture background;
    private final Texture blackPixel;
    private BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();

    private static final String TEXT = "Press SPACE to start";
    private static final float PADDING = 20f;

    public ControlsScreen(LuckyGame luckyGame) {
        this.luckyGame = luckyGame;
        this.batch = luckyGame.getBatch();
        String menuPath = "menu/casino_menu.png";
        this.background = new Texture(Gdx.files.internal(menuPath));

        // Un pixel noir, étiré pour servir de cadre derrière le texte
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.GOLDENROD);
        pixmap.fill();
        blackPixel = new Texture(pixmap);
        pixmap.dispose();

        // Génération de la police à la taille voulue
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Jersey10-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 48; // taille en pixels, directement nette à cette taille
        parameter.color = Color.WHITE;
        font = generator.generateFont(parameter);
        generator.dispose(); // le générateur ne sert plus une fois la police créée


        font.setColor(Color.WHITE);
        layout.setText(font, TEXT);
    }

    @Override
    public void resize(int width, int height){
        viewport.update(width, height, true);
    }

    @Override
    public void render(float delta){
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            luckyGame.setScreen(new GameScreen(luckyGame));
            dispose();
            return;
        }

        ScreenUtils.clear(Color.WHITE);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        // Dimensions du cadre = taille du texte + marge de chaque côté
        float boxWidth = layout.width + PADDING * 2;
        float boxHeight = layout.height + PADDING * 2;
        float boxX = (worldWidth - boxWidth) / 2f;
        float boxY = (worldHeight - boxHeight) / 2f;

        batch.begin();
        batch.setColor(1f, 1f, 1f, 1f);

        // 1. Fond
        batch.draw(background, 0, 0, worldWidth, worldHeight);

        // 2. Cadre noir, centré
        batch.draw(blackPixel, boxX, boxY, boxWidth, boxHeight);

        // 3. Texte, centré dans le cadre
        font.draw(batch, layout, boxX + PADDING, boxY + boxHeight - PADDING);

        batch.end();
    }

    @Override
    public void dispose(){
        background.dispose();
        blackPixel.dispose();
        font.dispose();
    }
}
