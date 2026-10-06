package fr.astratime.lucky.views;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.entities.Combo;
import fr.astratime.lucky.i18n.Lang;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Panneau latéral gauche, sur toute la hauteur de l'écran : titre du jeu,
 * encadré des gains (pièce d'or et montant en grand), aide-mémoire des
 * combinaisons de poker (celles que forment les cartes jouées ce tour brillent
 * en or, avec leur multiplicateur total), puis encadré des effets de cartes
 * actifs (symboles retirés, Porte-bonheur, paris en cours ; masqué s'il n'y
 * en a pas). Survoler un effet affiche sa description ; un clic droit ouvre
 * sa fiche.
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
    private static final float EFFECT_ICON    = 52f;
    private static final float OVERLAY_ICON   = 30f;   // croix posée dans le coin de l'icône
    private static final float EFFECT_GAP     = 8f;
    private static final float COMBO_GAP      = 2f;
    private static final float COMBO_IDLE     = 0.5f;  // opacité des combinaisons non formées
    private static final float COMBO_PULSE    = 1.15f;
    private static final float COMBO_RULE     = 5f;    // filet entre les combinaisons et leur total
    private static final float COMBO_RULE_GAP = 8f;
    private static final float TOOLTIP_GAP    = 8f;    // entre le panneau et l'infobulle d'un effet

    private final BitmapFont titleFont   = Fonts.jersey(56, Palette.TEXT_TITLE, 3f, Palette.TEXT_SHADE);
    private final BitmapFont captionFont = Fonts.jersey(30, Palette.TEXT_BODY, 2f, Palette.TEXT_SHADE);
    private final BitmapFont gainsFont   = Fonts.jersey(80, Palette.TEXT_TITLE, 3f, Palette.TEXT_SHADE);
    private final BitmapFont effectFont  = Fonts.jersey(28, Palette.TEXT_BODY, 2f, Palette.TEXT_SHADE);

    private final Table root = new Table();
    private final Image coin;
    private final Label gainsLabel;
    private final float gainsMaxWidth;
    private       int   shownGains;
    private       int   targetGains;
    private final Table effectsBox  = new Table();
    private final Table gainsBox    = new Table();
    private final Table combosBox   = new Table();
    private final Table effectsRows = new Table();
    private final Map<Combo, Label[]> comboLabels = new EnumMap<>(Combo.class); // nom, multiplicateur
    private       List<Combo>         shownCombos = List.of();
    private       Label[]             totalLabels;                                  // "Total", multiplicateur
    private       Tooltip             tooltip;
    private       Consumer<EffectRow> onInspect;

    /**
     * Une ligne des effets actifs : une icône, éventuellement barrée, et un texte.
     * Au survol, son infobulle donne son nom et sa description ; un clic droit
     * ouvre sa fiche.
     *
     * @param icon        image de l'effet (ex : le symbole retiré)
     * @param overlay     image posée par-dessus l'icône (ex : une croix), ou {@code null}
     * @param text        texte de l'effet (ex : "3 tours")
     * @param name        nom de l'effet (ex : "Recyclage")
     * @param description ce que fait l'effet, en phrases courtes
     */
    public record EffectRow(TextureRegion icon, TextureRegion overlay, String text, String name, String description) { }

    public SidePanel(HudTextures hud) {
        root.setBackground(hud.panelDrawable());
        root.pad(PADDING).top();

        Label title = new Label(Lang.t("LUCKY\nCONQUEST"), new Label.LabelStyle(titleFont, Color.WHITE));
        title.setAlignment(Align.center);
        root.add(title).growX();
        root.row();

        coin       = new Image(new TextureRegionDrawable(new TextureRegion(hud.coin)));
        gainsLabel = new Label("0", new Label.LabelStyle(gainsFont, Color.WHITE));

        float insetWidth = WIDTH - MARGIN * 2 - PADDING * 2;
        gainsMaxWidth = insetWidth - INSET_PADDING * 2 - COIN_SIZE - COIN_GAP;

        gainsBox.setBackground(hud.insetDrawable());
        gainsBox.pad(INSET_PADDING);
        gainsBox.add(new Label(Lang.t("GAINS"), new Label.LabelStyle(captionFont, Color.WHITE))).colspan(2).left();
        gainsBox.row();
        gainsBox.add(coin).size(COIN_SIZE).padRight(COIN_GAP);
        gainsBox.add(gainsLabel).growX().left();
        root.add(gainsBox).width(insetWidth).padTop(SECTION_GAP);
        root.row();

        combosBox.setBackground(hud.insetDrawable());
        combosBox.pad(INSET_PADDING).top().left();
        combosBox.add(new Label(Lang.t("COMBINAISONS"), new Label.LabelStyle(captionFont, Color.WHITE))).colspan(2).left();
        combosBox.row();
        for (Combo combo : Combo.values()) {
            comboLabels.put(combo, comboRow(combosBox, capitalized(combo.getDisplayName()), "x" + combo.formatFactor()));
        }
        combosBox.add(new Image(new TextureRegionDrawable(new TextureRegion(hud.tooltipRule)))).colspan(2)
            .height(COMBO_RULE).growX().padTop(COMBO_RULE_GAP);
        combosBox.row();
        totalLabels = comboRow(combosBox, Lang.t("Total"), "x1");
        root.add(combosBox).width(insetWidth).padTop(SECTION_GAP);
        root.row();
        setCombos(List.of());

        effectsBox.setBackground(hud.insetDrawable());
        effectsBox.pad(INSET_PADDING).top().left();
        effectsBox.add(new Label(Lang.t("EFFETS"), new Label.LabelStyle(captionFont, Color.WHITE))).left();
        effectsBox.row();
        effectsBox.add(effectsRows).growX().left();
        effectsBox.setVisible(false);
        root.add(effectsBox).width(insetWidth).padTop(SECTION_GAP);
        root.row();

        root.add().expandY(); // place libre pour de futures informations
        root.row();
    }

    /** Ajoute à {@code box} une ligne : un nom à gauche, un multiplicateur à droite. @return ces deux textes */
    private Label[] comboRow(Table box, String nameText, String factorText) {
        Label name   = new Label(nameText, new Label.LabelStyle(effectFont, Color.WHITE));
        Label factor = new Label(factorText, new Label.LabelStyle(effectFont, Color.WHITE));
        factor.setAlignment(Align.right);
        // Hauteur fixe : la ligne qui grossit un instant ne pousse pas les encadrés suivants.
        float height = name.getPrefHeight();
        box.add(name).height(height).padTop(COMBO_GAP).growX().left();
        box.add(factor).height(height).padTop(COMBO_GAP).right();
        box.row();
        return new Label[] {name, factor};
    }

    /**
     * Fait briller {@code combos}, les combinaisons que forment les cartes
     * jouées ce tour, et affiche leur multiplicateur total ; les autres restent
     * en retrait. Celles qui viennent d'être formées grossissent un instant.
     */
    public void setCombos(List<Combo> combos) {
        for (Map.Entry<Combo, Label[]> entry : comboLabels.entrySet()) {
            boolean formed = combos.contains(entry.getKey());
            highlight(entry.getValue(), formed, formed && !shownCombos.contains(entry.getKey()));
        }
        float total = Combo.totalFactor(combos);
        totalLabels[1].setText("x" + Combo.formatFactor(total));
        highlight(totalLabels, !combos.isEmpty(), !combos.equals(shownCombos) && !combos.isEmpty());
        shownCombos = List.copyOf(combos);
    }

    /** Met en or une ligne ({@code on}) ou la laisse en retrait ; la fait grossir un instant si {@code pulse}. */
    private static void highlight(Label[] row, boolean on, boolean pulse) {
        for (Label label : row) {
            label.setColor(on ? Palette.TEXT_TITLE : Palette.TEXT_BODY);
            label.getColor().a = on ? 1f : COMBO_IDLE;
            if (pulse) pulse(label);
        }
    }

    /** Fait grossir un instant {@code label}, pour signaler une combinaison qui vient d'être formée. */
    private static void pulse(Label label) {
        label.clearActions();
        label.setFontScale(1f);
        label.addAction(new TemporalAction(0.45f) {
            @Override
            protected void update(float percent) {
                float bump = percent < 0.3f ? percent / 0.3f : 1f - (percent - 0.3f) / 0.7f;
                label.setFontScale(1f + (COMBO_PULSE - 1f) * Interpolation.pow2Out.apply(bump));
            }
        });
    }

    /** @return {@code text} en minuscules, sauf sa première lettre (ex : "Brelan"). */
    private static String capitalized(String text) {
        return text.charAt(0) + text.substring(1).toLowerCase();
    }

    /**
     * @param tooltip   infobulle partagée : survoler un effet affiche son nom et sa description
     * @param onInspect appelé au clic droit sur un effet (afficher sa fiche)
     */
    public void setEffectHelp(Tooltip tooltip, Consumer<EffectRow> onInspect) {
        this.tooltip   = tooltip;
        this.onInspect = onInspect;
    }

    /** Affiche les effets de cartes actifs, ou masque leur encadré s'il n'y en a aucun. */
    public void setActiveEffects(List<EffectRow> rows) {
        if (tooltip != null && effectsRows.hasChildren()) tooltip.hide(); // la ligne survolée a pu disparaître
        effectsRows.clearChildren();
        for (EffectRow row : rows) {
            Stack icon = new Stack();
            icon.add(new Image(row.icon()));
            if (row.overlay() != null) {
                Container<Image> corner = new Container<>(new Image(row.overlay()));
                corner.size(OVERLAY_ICON).bottom().right();
                icon.add(corner);
            }
            Table line = new Table();
            line.setTouchable(Touchable.enabled); // toute la ligne réagit, l'espace entre l'icône et le texte compris
            line.add(icon).size(EFFECT_ICON).padRight(EFFECT_GAP);
            line.add(new Label(row.text(), new Label.LabelStyle(effectFont, Color.WHITE))).growX().left();
            addHelp(line, row);
            effectsRows.add(line).padTop(EFFECT_GAP).growX().left();
            effectsRows.row();
        }
        effectsBox.setVisible(!rows.isEmpty());
        root.invalidateHierarchy();
    }

    /** Survoler {@code line} affiche l'infobulle de l'effet, à droite du panneau ; un clic droit ouvre sa fiche. */
    private void addHelp(Table line, EffectRow row) {
        line.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer != -1 || tooltip == null) return;
                Vector2 bottom = line.localToStageCoordinates(new Vector2(0f, 0f));
                tooltip.show(row.name(), row.description(), WIDTH + TOOLTIP_GAP, bottom.y);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1 && tooltip != null) tooltip.hide();
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button != Input.Buttons.RIGHT || onInspect == null) return false;
                if (tooltip != null) tooltip.hide();
                onInspect.accept(row);
                return true;
            }
        });
    }

    /** @return les encadrés des gains, des combinaisons et des effets (le guide du tutoriel les éclaire). */
    public Actor getGainsBox()   { return gainsBox; }
    public Actor getCombosBox()  { return combosBox; }
    public Actor getEffectsBox() { return effectsBox; }

    /** @return le panneau, à ajouter au Stage. */
    public Table getActor() { return root; }

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
        Fonts.release(titleFont);
        Fonts.release(captionFont);
        Fonts.release(gainsFont);
        Fonts.release(effectFont);
    }
}
