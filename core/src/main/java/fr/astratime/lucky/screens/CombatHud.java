package fr.astratime.lucky.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.entities.GameState;

/**
 * Informations de combat affichées en permanence : barre de vie de l'ennemi
 * (en haut), barre de vie du joueur (en bas), toutes deux centrées, et points
 * (gains) du joueur en haut à gauche.
 */
class CombatHud implements Disposable {

    private static final float HEALTH_BAR_WIDTH         = 300f;
    private static final float HEALTH_BAR_HEIGHT        = 22f;
    private static final float HEALTH_BAR_TOP_MARGIN    = 10f;
    private static final float HEALTH_BAR_BOTTOM_MARGIN = 100f;
    static final float         SCORE_LABEL_TOP_MARGIN   = 40f;
    private static final float SCORE_LABEL_LEFT         = 20f;

    private final Stage         stage;
    private final HealthBarView enemyHealthBar;
    private final HealthBarView playerHealthBar;
    private final Label         scoreLabel;

    CombatHud(Stage stage, BitmapFont font) {
        this.stage      = stage;
        enemyHealthBar  = new HealthBarView(font, Color.valueOf("550000ff"), Color.RED, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
        playerHealthBar = new HealthBarView(font, Color.valueOf("005500ff"), Color.GREEN, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
        scoreLabel      = new Label("Points : 0", new Label.LabelStyle(font, Color.WHITE));
        layout();
    }

    /** Ajoute les barres de vie et le score au Stage. */
    void addTo(Stage target) {
        enemyHealthBar.addTo(target);
        playerHealthBar.addTo(target);
        target.addActor(scoreLabel);
    }

    /** Met à jour les deux barres de vie et le score à partir de l'état de la partie. */
    void refresh(GameState gameState) {
        enemyHealthBar.refresh(gameState.getEnemy().getHp(), gameState.getEnemy().getMaxHp());
        playerHealthBar.refresh(gameState.getPlayer().getHp(), gameState.getPlayer().getMaxHp());
        scoreLabel.setText("Points : " + gameState.getPlayer().getGains());
    }

    /** Repositionne les éléments ancrés en haut et au centre de l'écran (après un redimensionnement). */
    void layout() {
        float worldWidth  = stage.getViewport().getWorldWidth();
        float worldHeight = stage.getViewport().getWorldHeight();
        float barX        = (worldWidth - HEALTH_BAR_WIDTH) / 2f;
        enemyHealthBar.setPosition(barX, worldHeight - HEALTH_BAR_HEIGHT - HEALTH_BAR_TOP_MARGIN);
        playerHealthBar.setPosition(barX, HEALTH_BAR_BOTTOM_MARGIN);
        scoreLabel.setPosition(SCORE_LABEL_LEFT, worldHeight - SCORE_LABEL_TOP_MARGIN);
    }

    /**
     * @param gap distance horizontale à droite de la barre de vie du joueur
     * @return un point (Stage) à droite de la barre de vie du joueur, à mi-hauteur :
     *         là où s'affichent les textes de la riposte ennemie
     */
    Vector2 besidePlayerHealthBar(float gap) {
        float worldWidth = stage.getViewport().getWorldWidth();
        return new Vector2((worldWidth + HEALTH_BAR_WIDTH) / 2f + gap, HEALTH_BAR_BOTTOM_MARGIN + HEALTH_BAR_HEIGHT / 2f);
    }

    @Override
    public void dispose() {
        enemyHealthBar.dispose();
        playerHealthBar.dispose();
    }
}
