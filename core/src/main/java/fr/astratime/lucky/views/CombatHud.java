package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.entities.GameState;

/**
 * Barres de vie affichées en permanence, centrées dans la zone de jeu : celle
 * de l'ennemi en haut, celle du joueur en bas. Les gains sont affichés par le
 * panneau latéral ({@link SidePanel}).
 */
public class CombatHud implements Disposable {

    private static final float HEALTH_BAR_WIDTH         = 440f;
    private static final float HEALTH_BAR_TOP_MARGIN    = 6f;
    private static final float HEALTH_BAR_BOTTOM_MARGIN = 88f;
    private static final int   FONT_SIZE                = 28;
    private static final float FONT_BORDER              = 2f;

    private final PlayArea      playArea;
    private final BitmapFont    font;
    private final HealthBarView enemyHealthBar;
    private final HealthBarView playerHealthBar;

    public CombatHud(PlayArea playArea, HudTextures hud) {
        this.playArea   = playArea;
        font            = Fonts.jersey(FONT_SIZE, Color.WHITE, FONT_BORDER, Color.valueOf("1a0f0fff"));
        enemyHealthBar  = new HealthBarView("ENNEMI", font, hud, hud.barFillEnemy, hud.chipEnemy, HEALTH_BAR_WIDTH);
        playerHealthBar = new HealthBarView("JOUEUR", font, hud, hud.barFillPlayer, hud.chipPlayer, HEALTH_BAR_WIDTH);
        layout();
    }

    /** Ajoute les deux barres de vie au Stage. */
    public void addTo(Stage target) {
        target.addActor(enemyHealthBar);
        target.addActor(playerHealthBar);
    }

    /** Met à jour les deux barres de vie à partir de l'état de la partie. */
    public void refresh(GameState gameState) {
        enemyHealthBar.refresh(gameState.getEnemy().getHp(), gameState.getEnemy().getMaxHp());
        playerHealthBar.refresh(gameState.getPlayer().getHp(), gameState.getPlayer().getMaxHp());
    }

    /** Recentre les barres dans la zone de jeu (après un redimensionnement). */
    public void layout() {
        float barX = playArea.getCenterX() - enemyHealthBar.getWidth() / 2f;
        enemyHealthBar.setPosition(barX, playArea.getHeight() - HealthBarView.HEIGHT - HEALTH_BAR_TOP_MARGIN);
        playerHealthBar.setPosition(barX, HEALTH_BAR_BOTTOM_MARGIN);
    }

    /**
     * @param gap distance horizontale à droite de la barre de vie du joueur
     * @return un point (Stage) à droite de la barre de vie du joueur, à mi-hauteur :
     *         là où s'affichent les textes de la riposte ennemie
     */
    public Vector2 besidePlayerHealthBar(float gap) {
        return new Vector2(playerHealthBar.getRight() + gap, playerHealthBar.getY() + HealthBarView.HEIGHT / 2f);
    }

    @Override
    public void dispose() {
        font.dispose();
    }
}
