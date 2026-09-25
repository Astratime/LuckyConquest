package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;

/**
 * Infobulle au thème du casino, partagée par tous les acteurs survolables de
 * l'écran (cartes, symboles, piles...) : fond laqué sombre à double liseré
 * doré et rivets rouges, titre doré souligné d'un filet, puis le texte en
 * crème, renvoyé à la ligne. Elle apparaît en fondu avec un léger zoom et
 * reste toujours entièrement à l'écran.
 */
public class Tooltip implements Disposable {

    private static final Color GOLD       = Color.valueOf("ffd454ff");
    private static final Color CREAM      = Color.valueOf("f0e0b0ff");
    private static final Color TEXT_SHADE = Color.valueOf("1a0f0fff");
    private static final float PAD        = 16f;
    private static final float MAX_WIDTH  = 380f;   // largeur du texte avant retour à la ligne
    private static final float RULE_GAP   = 6f;
    private static final float MARGIN     = 8f;     // distance minimale au bord de l'écran
    private static final float APPEAR     = 0.12f;

    private final BitmapFont titleFont = Fonts.jersey(30, GOLD, 2f, TEXT_SHADE);
    private final BitmapFont bodyFont  = Fonts.jersey(24, CREAM, 2f, TEXT_SHADE);
    private final Label      title;
    private final Image      rule;
    private final Label      body;
    private final Table      table = new Table();

    public Tooltip(HudTextures hud) {
        title = new Label("", new Label.LabelStyle(titleFont, Color.WHITE));
        body  = new Label("", new Label.LabelStyle(bodyFont, Color.WHITE));
        body.setWrap(true);
        rule  = new Image(new TextureRegionDrawable(new TextureRegion(hud.tooltipRule)));
        table.setBackground(hud.tooltipDrawable());
        table.pad(PAD);
        table.setTouchable(Touchable.disabled);
        table.setTransform(true);
        table.setVisible(false);
    }

    /** @return l'acteur de l'infobulle, à ajouter au Stage en dernier pour rester toujours au-dessus. */
    public Table getActor() { return table; }

    /** Affiche {@code text} (sans titre), juste au-dessus de {@code (x, y)} (coordonnées du Stage). */
    public void show(String text, float x, float y) {
        show(null, text, x, y);
    }

    /**
     * Affiche {@code heading} en doré, souligné, puis {@code text} ; le bas de
     * l'infobulle est posé en {@code (x, y)} (coordonnées du Stage), décalé si
     * besoin pour rester à l'écran.
     *
     * @param heading titre (ex : le nom de la carte), ou {@code null} pour n'afficher que le texte
     */
    public void show(String heading, String text, float x, float y) {
        table.clearChildren();
        if (heading != null) {
            title.setText(heading);
            table.add(title).left();
            table.row();
            table.add(rule).growX().height(rule.getPrefHeight()).padTop(RULE_GAP / 2f).padBottom(RULE_GAP);
            table.row();
        }
        body.setText(text);
        body.setWidth(MAX_WIDTH);
        float bodyWidth = Math.min(MAX_WIDTH, naturalWidth(text));
        if (heading != null) bodyWidth = Math.max(bodyWidth, title.getPrefWidth());
        table.add(body).width(bodyWidth).left();
        table.pack();

        float worldWidth  = table.getStage() != null ? table.getStage().getViewport().getWorldWidth() : Float.MAX_VALUE;
        float worldHeight = table.getStage() != null ? table.getStage().getViewport().getWorldHeight() : Float.MAX_VALUE;
        float px = Math.max(MARGIN, Math.min(worldWidth - table.getWidth() - MARGIN, x));
        float py = Math.max(MARGIN, Math.min(worldHeight - table.getHeight() - MARGIN, y));
        table.setPosition(px, py);
        table.setOrigin(Align.bottomLeft);

        table.clearActions();
        table.setVisible(true);
        table.getColor().a = 0f;
        table.setScale(0.94f);
        table.addAction(Actions.parallel(
            Actions.fadeIn(APPEAR),
            Actions.scaleTo(1f, 1f, APPEAR, Interpolation.pow2Out)));
        table.toFront();
    }

    /** @return la largeur de {@code text} sur une ligne (la plus longue de ses lignes). */
    private float naturalWidth(String text) {
        Label probe = new Label(text, body.getStyle());
        return probe.getPrefWidth();
    }

    /** Cache l'infobulle. */
    public void hide() {
        table.clearActions();
        table.setVisible(false);
    }

    @Override
    public void dispose() {
        titleFont.dispose();
        bodyFont.dispose();
    }
}
