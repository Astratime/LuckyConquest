package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.entities.GameState;

import java.util.function.Supplier;

/**
 * Barres de vie affichées en permanence, centrées dans la zone de jeu : celle
 * de l'ennemi en haut, celle du joueur en bas. Les gains sont affichés par le
 * panneau latéral ({@link SidePanel}).
 *
 * Les PV perdus ou regagnés pendant un tirage sont « retenus » ({@link #holdBack})
 * puis montrés un à un, quand le texte de chaque coup apparaît : la barre ne
 * trahit pas le résultat avant l'arrêt des rouleaux.
 */
public class CombatHud implements Disposable {

    private static final float HEALTH_BAR_WIDTH         = 440f;
    private static final float HEALTH_BAR_TOP_MARGIN    = 6f;
    private static final float HEALTH_BAR_BOTTOM_MARGIN = 20f;   // sous la table, à côté des boutons
    private static final int   FONT_SIZE                = 28;
    private static final float FONT_BORDER              = 2f;

    private static final Color HIT_FLASH  = Color.WHITE;
    private static final Color HEAL_FLASH = Color.valueOf("7dff8aff");
    private static final float HIT_SHAKE  = 0.3f;

    private final PlayArea            playArea;
    private final Supplier<GameState> gameState;
    private final BitmapFont    font;
    private final HealthBarView enemyHealthBar;
    private final HealthBarView playerHealthBar;

    // PV du tirage en cours pas encore montrés (voir holdBack).
    private int heldEnemyLoss;
    private int heldPlayerDamage;
    private int heldPlayerHeal;

    /** @param gameState état de la partie en cours (il change à chaque nouveau combat) */
    public CombatHud(PlayArea playArea, HudTextures hud, Supplier<GameState> gameState) {
        this.playArea   = playArea;
        this.gameState  = gameState;
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

    /** Met à jour les deux barres de vie à partir de l'état de la partie, sans les PV encore retenus. */
    public void refresh() {
        GameState state = gameState.get();
        int enemyMax  = state.getEnemy().getMaxHp();
        int playerMax = state.getPlayer().getMaxHp();
        enemyHealthBar.refresh(Math.clamp(state.getEnemy().getHp() + heldEnemyLoss, 0, enemyMax), enemyMax);
        playerHealthBar.refresh(
            Math.clamp(state.getPlayer().getHp() + heldPlayerDamage - heldPlayerHeal, 0, playerMax), playerMax);
    }

    /**
     * Retient les PV d'un tirage déjà joué : les barres les montreront au fil
     * des textes ({@link #revealEnemyHit}, {@link #revealPlayerHit},
     * {@link #revealPlayerHeal}).
     *
     * @param enemyLoss    PV réellement perdus par l'ennemi pendant le tirage
     * @param playerDamage dégâts réellement subis par le joueur
     * @param playerHeal   PV réellement rendus au joueur
     */
    public void holdBack(int enemyLoss, int playerDamage, int playerHeal) {
        heldEnemyLoss    += enemyLoss;
        heldPlayerDamage += playerDamage;
        heldPlayerHeal   += playerHeal;
    }

    /** Oublie les PV retenus (nouveau combat : leurs textes ne s'afficheront jamais). */
    public void clearHeldBack() {
        heldEnemyLoss = heldPlayerDamage = heldPlayerHeal = 0;
        refresh();
    }

    /** Montre un coup porté à l'ennemi : sa barre descend, tremble et flashe. */
    public void revealEnemyHit(int damage) {
        heldEnemyLoss -= Math.min(damage, heldEnemyLoss); // un coup fatal peut dépasser les PV restants
        refresh();
        enemyHealthBar.hit(HIT_FLASH, HIT_SHAKE);
    }

    /** Montre un coup porté au joueur : sa barre descend, tremble et flashe. */
    public void revealPlayerHit(int damage) {
        heldPlayerDamage = Math.max(0, heldPlayerDamage - damage);
        refresh();
        playerHealthBar.hit(HIT_FLASH, HIT_SHAKE);
    }

    /** Montre un soin du joueur : sa barre remonte sous un flash vert. */
    public void revealPlayerHeal(int amount) {
        heldPlayerHeal = Math.max(0, heldPlayerHeal - amount);
        refresh();
        playerHealthBar.hit(HEAL_FLASH, 0f);
    }

    /** @return le centre (Stage) du jeton de l'ennemi, d'où partent les jetons de la victoire. */
    public Vector2 getEnemyChipCenter() {
        return new Vector2(enemyHealthBar.getX() + HealthBarView.HEIGHT / 2f,
            enemyHealthBar.getY() + HealthBarView.HEIGHT / 2f);
    }

    /** @return le centre (Stage) de la barre de vie de l'ennemi, cible du pistolet. */
    public Vector2 getEnemyBarCenter() {
        return new Vector2(enemyHealthBar.getX() + enemyHealthBar.getWidth() / 2f,
            enemyHealthBar.getY() + HealthBarView.HEIGHT / 2f);
    }

    /** @return la barre de vie de l'ennemi (pour la faire disparaître à la victoire). */
    public HealthBarView getEnemyHealthBar() { return enemyHealthBar; }

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
