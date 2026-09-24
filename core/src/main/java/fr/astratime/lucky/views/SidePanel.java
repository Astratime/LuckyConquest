package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;

/**
 * Panneau latéral gauche, sur toute la hauteur de l'écran : titre du jeu,
 * encadré des gains (pièce d'or et montant en grand), puis un emplacement en
 * bas pour un bouton (ex : "Recommencer"). La place libre entre les deux est
 * prévue pour d'autres informations.
 *
 * Quand les gains changent, le montant défile jusqu'à sa nouvelle valeur et la
 * pièce rebondit (seulement si les gains augmentent).
 */
public class SidePanel implements Disposable {

    /** Largeur réservée au panneau à gauche de l'écran (marges comprises). */
    public static final float WIDTH = 340f;

    private static final float MARGIN         = 10f;   // entre le panneau et le bord de l'écran
    private static final float PADDING        = 24f;   // entre le liseré du panneau et son contenu
    private static final float SECTION_GAP    = 28f;
    private static final float INSET_PADDING  = 16f;
    private static final float COIN_SIZE      = 60f;
    private static final float COIN_GAP       = 12f;
    private static final float COUNT_DURATION = 0.6f;
    private static final float BUMP_SCALE     = 1.3f;

    private static final Color GOLD       = Color.valueOf("ffd454ff");
    private static final Color CREAM      = Color.valueOf("f0e0b0ff");
    private static final Color TEXT_SHADE = Color.valueOf("1a0f0fff");

    private final BitmapFont titleFont   = Fonts.jersey(56, GOLD, 3f, TEXT_SHADE);
    private final BitmapFont captionFont = Fonts.jersey(30, CREAM, 2f, TEXT_SHADE);
    private final BitmapFont gainsFont   = Fonts.jersey(80, GOLD, 3f, TEXT_SHADE);

    private final Table root = new Table();
    private final Image coin;
    private final Label gainsLabel;
    private final float gainsMaxWidth;
    private       int   shownGains;
    private       int   targetGains;

    public SidePanel(HudTextures hud) {
        root.setBackground(hud.panelDrawable());
        root.pad(PADDING).top();

        Label title = new Label("LUCKY\nCONQUEST", new Label.LabelStyle(titleFont, Color.WHITE));
        title.setAlignment(Align.center);
        root.add(title).growX();
        root.row();

        coin       = new Image(new TextureRegionDrawable(new TextureRegion(hud.coin)));
        gainsLabel = new Label("0", new Label.LabelStyle(gainsFont, Color.WHITE));

        float insetWidth = WIDTH - MARGIN * 2 - PADDING * 2;
        gainsMaxWidth = insetWidth - INSET_PADDING * 2 - COIN_SIZE - COIN_GAP;

        Table gainsBox = new Table();
        gainsBox.setBackground(hud.insetDrawable());
        gainsBox.pad(INSET_PADDING);
        gainsBox.add(new Label("GAINS", new Label.LabelStyle(captionFont, Color.WHITE))).colspan(2).left();
        gainsBox.row();
        gainsBox.add(coin).size(COIN_SIZE).padRight(COIN_GAP);
        gainsBox.add(gainsLabel).growX().left();
        root.add(gainsBox).width(insetWidth).padTop(SECTION_GAP);
        root.row();

        root.add().expandY(); // place libre pour de futures informations
        root.row();
    }

    /** @return le panneau, à ajouter au Stage. */
    public Table getActor() { return root; }

    /** Place {@code actor} (à sa taille actuelle) en bas du panneau, centré. */
    public void setFooter(Actor actor) {
        root.add(actor).size(actor.getWidth(), actor.getHeight());
    }

    /** Étire le panneau sur toute la hauteur de l'écran (après un redimensionnement). */
    public void layout(Stage stage) {
        root.setBounds(MARGIN, MARGIN, WIDTH - MARGIN * 2, stage.getViewport().getWorldHeight() - MARGIN * 2);
        root.validate();
    }

    /** Fait défiler le montant affiché jusqu'à {@code gains} ; la pièce rebondit si les gains augmentent. */
    public void setGains(int gains) {
        if (gains == targetGains) return;
        int from = shownGains;
        if (gains > targetGains) bumpCoin();
        targetGains = gains;

        gainsLabel.clearActions();
        gainsLabel.addAction(new TemporalAction(COUNT_DURATION, Interpolation.pow2Out) {
            @Override
            protected void update(float percent) {
                showGains(Math.round(from + (gains - from) * percent));
            }
        });
    }

    /** @return le centre (Stage) de la pièce des gains, cible des pièces du jackpot. */
    public Vector2 getCoinCenter() {
        return coin.localToStageCoordinates(new Vector2(coin.getWidth() / 2f, coin.getHeight() / 2f));
    }

    /** Fait rebondir la pièce des gains (à chaque gain, ou à l'arrivée d'une pièce du jackpot). */
    public void bumpCoin() {
        coin.clearActions();
        coin.setOrigin(Align.center);
        coin.setScale(1f);
        coin.addAction(Actions.sequence(
            Actions.scaleTo(BUMP_SCALE, BUMP_SCALE, 0.08f, Interpolation.pow2Out),
            Actions.scaleTo(1f, 1f, 0.35f, Interpolation.bounceOut)));
    }

    /** Affiche {@code gains}, réduit si besoin pour tenir à côté de la pièce. */
    private void showGains(int gains) {
        shownGains = gains;
        gainsLabel.setText(formatGains(gains));
        gainsLabel.setFontScale(1f);
        float width = gainsLabel.getPrefWidth();
        if (width > gainsMaxWidth) gainsLabel.setFontScale(gainsMaxWidth / width);
    }

    /** @return le montant avec une espace entre chaque groupe de trois chiffres (ex : "12 500"). */
    static String formatGains(int gains) {
        String digits = Integer.toString(Math.abs(gains));
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && (digits.length() - i) % 3 == 0) text.append(' ');
            text.append(digits.charAt(i));
        }
        return gains < 0 ? "-" + text : text.toString();
    }

    @Override
    public void dispose() {
        titleFont.dispose();
        captionFont.dispose();
        gainsFont.dispose();
    }
}
