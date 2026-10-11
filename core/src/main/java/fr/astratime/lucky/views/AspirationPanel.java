package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.enemy.AspirationRule;
import fr.astratime.lucky.i18n.Lang;

import java.util.List;

/**
 * Panneau de la Machine Originelle, au-dessus de la main : pour chaque
 * Aspiration, la règle à respecter avec les cartes jouées ce tour. Chaque
 * condition s'allume quand les cartes jouées la remplissent ; une règle remplie
 * passe au vert (son rouleau ne sera pas aspiré). Caché sans Aspiration.
 */
public class AspirationPanel extends Table implements Disposable {

    private static final float PAD = 14f, ROW_GAP = 4f;
    private static final Color DIM = new Color(0.62f, 0.58f, 0.72f, 1f);

    private final BitmapFont titleFont = Fonts.jersey(26, Palette.VIOLET, 2f, Palette.TEXT_SHADE);
    private final BitmapFont textFont  = Fonts.jersey(24, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private String shown = "";

    public AspirationPanel(HudTextures hud) {
        setBackground(hud.panelDrawable());
        setTouchable(Touchable.disabled);
        pad(PAD);
        align(Align.topLeft);
        setVisible(false);
    }

    /**
     * Montre les règles {@code rules} et où en sont les cartes {@code played} ;
     * se cache s'il n'y a pas de règle.
     */
    public void update(List<AspirationRule> rules, List<Card> played) {
        StringBuilder key = new StringBuilder();
        for (AspirationRule rule : rules) {
            key.append(rule.text()).append('|');
            for (AspirationRule.Condition condition : rule.getConditions()) key.append(condition.progress(played)).append(',');
        }
        if (key.toString().equals(shown)) return;
        shown = key.toString();
        clearChildren();
        if (rules.isEmpty()) {
            setVisible(false);
            return;
        }
        Label title = new Label(Lang.t("ASPIRATION : JOUE CES CARTES"), new Label.LabelStyle(titleFont, Color.WHITE));
        add(title).left().padBottom(ROW_GAP).row();
        for (AspirationRule rule : rules) {
            boolean met = rule.isMet(played);
            Table row = new Table();
            for (AspirationRule.Condition condition : rule.getConditions()) {
                boolean lit = condition.isMet(played);
                String text = (lit ? "+ " : "- ") + condition.text()
                    + (lit ? "" : " (" + condition.progress(played) + "/" + condition.count() + ")");
                Label label = new Label(text, new Label.LabelStyle(textFont, Color.WHITE));
                label.setColor(met ? Palette.MINT : lit ? Palette.GOLD : DIM);
                row.add(label).left().padRight(PAD);
            }
            Label verdict = new Label(met ? Lang.t("ROULEAU SAUVÉ") : Lang.t("ROULEAU MENACÉ"),
                new Label.LabelStyle(textFont, Color.WHITE));
            verdict.setColor(met ? Palette.MINT : Palette.TEXT_ALERT);
            row.add(verdict).left();
            add(row).left().padTop(ROW_GAP).row();
        }
        pack();
        if (!isVisible()) {
            setVisible(true);
            getColor().a = 0f;
            addAction(Actions.fadeIn(0.25f));
        }
    }

    @Override
    public void dispose() {
        Fonts.release(titleFont);
        Fonts.release(textFont);
    }
}
