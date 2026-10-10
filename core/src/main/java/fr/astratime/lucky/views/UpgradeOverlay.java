package fr.astratime.lucky.views;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.i18n.Lang;

/**
 * Fenêtre de fusion de la Table du croupier : les {@link Card#UPGRADE_COST}
 * exemplaires d'une carte à gauche, sa version « + » à droite, et ce que fait
 * chacune. « Fusionner » lance la fusion : les trois cartes glissent sur la
 * carte « + », qui s'allume ; « Annuler » ferme sans rien changer.
 */
public class UpgradeOverlay implements Disposable {

    private static final float PANEL_WIDTH  = 1180f;
    private static final float PANEL_HEIGHT = 800f;
    private static final float CARD_WIDTH   = 200f;
    private static final float CARD_HEIGHT  = 280f;
    private static final float TEXT_WIDTH   = 460f;
    private static final float FAN          = 46f;
    private static final float FADE         = 0.2f;
    /** Transparence de la carte « + » avant la fusion (un aperçu). */
    private static final float PREVIEW      = 0.45f;

    private final Group      root = new Group();
    private final Image      veil;
    private final Image      panel;
    private final Label      title;
    private final Image[]    copies = new Image[Card.UPGRADE_COST];
    private final Image      upgraded;
    private final Image      flash;
    private final Label      arrow;
    private final Label      baseName;
    private final Label      upgradedName;
    private final Label      before;
    private final Label      after;
    private final TextButton confirm;
    private final TextButton cancel;
    private final TextButton close;
    private final Sound      upgradeSound;
    private final BitmapFont titleFont = Fonts.jersey(72, Palette.GOLD, 5f, Palette.TEXT_SHADE);
    private final BitmapFont nameFont  = Fonts.jersey(36, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont textFont  = Fonts.jersey(28, Palette.CREAM, 2f, Palette.TEXT_SHADE);
    private final BitmapFont arrowFont = Fonts.jersey(96, Palette.GOLD, 5f, Palette.TEXT_SHADE);

    private Runnable onConfirm;
    private Runnable onClose;

    /**
     * @param buttons      fabrique des boutons du casino
     * @param clickSound   bruitage des boutons
     * @param upgradeSound bruitage de la fusion (l'appelant le dispose)
     */
    public UpgradeOverlay(HudTextures hud, CasinoButtons buttons, Sound clickSound, Sound upgradeSound) {
        this.upgradeSound = upgradeSound;
        TextureRegionDrawable pixel = new TextureRegionDrawable(new TextureRegion(hud.pixel));
        veil = new Image(pixel);
        veil.setColor(0f, 0f, 0f, 0.78f);
        panel = new Image(hud.panelDrawable());
        title = new Label(Lang.t("FUSION"), new Label.LabelStyle(titleFont, Color.WHITE));
        for (int i = 0; i < copies.length; i++) copies[i] = new Image();
        upgraded = new Image();
        flash = new Image(pixel);
        flash.setTouchable(Touchable.disabled);
        arrow = new Label(">", new Label.LabelStyle(arrowFont, Color.WHITE));
        baseName     = label(nameFont, Color.WHITE);
        upgradedName = label(nameFont, Palette.GOLD);
        before = label(textFont, Palette.CREAM);
        after  = label(textFont, Color.WHITE);
        before.setWrap(true);
        after.setWrap(true);
        confirm = buttons.createAction(Lang.t("Fusionner"), clickSound, this::fuse);
        cancel  = buttons.create(Lang.t("Annuler"), clickSound, this::hide);
        close   = buttons.createAction(Lang.t("Super !"), clickSound, this::hide);

        root.addActor(veil);
        root.addActor(panel);
        root.addActor(title);
        for (Image copy : copies) root.addActor(copy);
        root.addActor(arrow);
        root.addActor(upgraded);
        root.addActor(flash);
        root.addActor(baseName);
        root.addActor(upgradedName);
        root.addActor(before);
        root.addActor(after);
        root.addActor(confirm);
        root.addActor(cancel);
        root.addActor(close);
        root.setVisible(false);
        // Les clics ne traversent pas la fenêtre.
        root.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) { return true; }
        });
    }

    private static Label label(BitmapFont font, Color color) {
        Label label = new Label("", new Label.LabelStyle(font, Color.WHITE));
        label.setColor(color);
        return label;
    }

    /** @return l'acteur racine de la fenêtre, à ajouter au Stage par-dessus l'écran. */
    public Group getActor() { return root; }

    /** @return {@code true} tant que la fenêtre est affichée. */
    public boolean isShown() { return root.isVisible(); }

    /**
     * Ouvre la fenêtre pour fusionner la carte {@code base} en {@code plus}.
     *
     * @param onConfirm fait la fusion (appelé au clic sur « Fusionner »)
     * @param onClose   appelé à la fermeture, qu'il y ait eu fusion ou non
     */
    public void show(Card base, Texture baseTexture, Card plus, Texture plusTexture,
                     Runnable onConfirm, Runnable onClose) {
        this.onConfirm = onConfirm;
        this.onClose   = onClose;
        for (Image copy : copies) {
            copy.setDrawable(new TextureRegionDrawable(new TextureRegion(baseTexture)));
            copy.setSize(CARD_WIDTH, CARD_HEIGHT);
            copy.setOrigin(Align.center);
            copy.clearActions();
            copy.setColor(Color.WHITE);
            copy.setScale(1f);
            copy.setVisible(true);
        }
        upgraded.setDrawable(new TextureRegionDrawable(new TextureRegion(plusTexture)));
        upgraded.setSize(CARD_WIDTH * 1.1f, CARD_HEIGHT * 1.1f);
        upgraded.setOrigin(Align.center);
        upgraded.clearActions();
        upgraded.setScale(1f);
        upgraded.setColor(1f, 1f, 1f, PREVIEW);
        flash.clearActions();
        flash.setColor(1f, 0.95f, 0.7f, 0f);
        arrow.clearActions();
        arrow.getColor().a = 1f;
        baseName.setText(Lang.f("{0} x {1}", Card.UPGRADE_COST, base.getName()));
        upgradedName.setText(plus.getName());
        before.setText(Lang.t("Avant :") + "\n" + base.getDescription());
        after.setText(Lang.t("Après :") + "\n" + plus.getDescription());
        confirm.setVisible(true);
        cancel.setVisible(true);
        close.setVisible(false);
        confirm.setDisabled(false);
        layout(root.getStage() != null ? root.getStage().getWidth() : 1920f,
            root.getStage() != null ? root.getStage().getHeight() : 1080f);
        root.clearActions();
        root.setVisible(true);
        root.getColor().a = 0f;
        root.addAction(Actions.fadeIn(FADE, Interpolation.pow2Out));
        root.toFront();
    }

    /** Fusionne : les trois cartes glissent sur la carte « + », qui s'allume. */
    private void fuse() {
        if (onConfirm == null) return;
        Runnable fusion = onConfirm;
        onConfirm = null;
        fusion.run();
        upgradeSound.play();
        confirm.setVisible(false);
        cancel.setVisible(false);
        float targetX = upgraded.getX() + (upgraded.getWidth() - CARD_WIDTH) / 2f;
        float targetY = upgraded.getY() + (upgraded.getHeight() - CARD_HEIGHT) / 2f;
        for (int i = 0; i < copies.length; i++) {
            copies[i].addAction(Actions.sequence(
                Actions.delay(i * 0.12f),
                Actions.parallel(
                    Actions.moveTo(targetX, targetY, 0.4f, Interpolation.pow2In),
                    Actions.rotateTo(0f, 0.4f),
                    Actions.scaleTo(0.8f, 0.8f, 0.4f)),
                Actions.visible(false)));
        }
        arrow.addAction(Actions.fadeOut(0.3f));
        float lit = 0.12f * (copies.length - 1) + 0.4f;
        upgraded.addAction(Actions.sequence(
            Actions.delay(lit),
            Actions.alpha(1f),
            Actions.scaleTo(1.3f, 1.3f, 0f),
            Actions.scaleTo(1f, 1f, 0.5f, Interpolation.swingOut)));
        flash.setBounds(upgraded.getX() - 20f, upgraded.getY() - 20f, upgraded.getWidth() + 40f, upgraded.getHeight() + 40f);
        flash.addAction(Actions.sequence(Actions.delay(lit), Actions.alpha(0.9f), Actions.fadeOut(0.6f, Interpolation.pow2Out)));
        close.addAction(Actions.sequence(Actions.delay(lit + 0.5f), Actions.visible(true)));
    }

    /** Ferme la fenêtre. */
    public void hide() {
        if (!root.isVisible()) return;
        onConfirm = null;
        Runnable then = onClose;
        onClose = null;
        root.clearActions();
        root.addAction(Actions.sequence(Actions.fadeOut(FADE), Actions.visible(false)));
        if (then != null) then.run();
    }

    /** Place la fenêtre au centre d'un écran de {@code width} x {@code height}. */
    public void layout(float width, float height) {
        veil.setBounds(0f, 0f, width, height);
        float left = (width - PANEL_WIDTH) / 2f, bottom = (height - PANEL_HEIGHT) / 2f;
        panel.setBounds(left, bottom, PANEL_WIDTH, PANEL_HEIGHT);
        title.pack();
        title.setPosition(left + (PANEL_WIDTH - title.getWidth()) / 2f, bottom + PANEL_HEIGHT - 40f - title.getHeight());

        float cardsTop = title.getY() - 30f;
        float leftCenter = left + PANEL_WIDTH * 0.28f, rightCenter = left + PANEL_WIDTH * 0.72f;
        for (int i = 0; i < copies.length; i++) {
            if (copies[i].hasActions()) continue;
            float offset = (i - (copies.length - 1) / 2f) * FAN;
            copies[i].setPosition(leftCenter - CARD_WIDTH / 2f + offset, cardsTop - CARD_HEIGHT - Math.abs(offset) * 0.3f);
            copies[i].setRotation(-offset * 0.18f);
        }
        upgraded.setPosition(rightCenter - upgraded.getWidth() / 2f, cardsTop - upgraded.getHeight());
        arrow.pack();
        arrow.setPosition(left + (PANEL_WIDTH - arrow.getWidth()) / 2f, cardsTop - CARD_HEIGHT / 2f - arrow.getHeight() / 2f);

        float namesY = cardsTop - upgraded.getHeight() - 24f;
        place(baseName, leftCenter, namesY);
        place(upgradedName, rightCenter, namesY);
        before.setWidth(TEXT_WIDTH);
        after.setWidth(TEXT_WIDTH);
        before.setAlignment(Align.topLeft);
        after.setAlignment(Align.topLeft);
        before.setHeight(before.getPrefHeight());
        after.setHeight(after.getPrefHeight());
        float textTop = Math.min(baseName.getY(), upgradedName.getY()) - 18f;
        before.setPosition(leftCenter - TEXT_WIDTH / 2f, textTop - before.getHeight());
        after.setPosition(rightCenter - TEXT_WIDTH / 2f, textTop - after.getHeight());

        float buttonY = bottom + 40f;
        float gap = 30f;
        cancel.setPosition(left + PANEL_WIDTH / 2f - gap / 2f - cancel.getWidth(), buttonY);
        confirm.setPosition(left + PANEL_WIDTH / 2f + gap / 2f, buttonY);
        close.setPosition(left + (PANEL_WIDTH - close.getWidth()) / 2f, buttonY);
    }

    /** Place {@code label}, centré sur {@code centerX}, sous {@code top}. */
    private static void place(Label label, float centerX, float top) {
        label.pack();
        label.setPosition(centerX - label.getWidth() / 2f, top - label.getHeight());
    }

    @Override
    public void dispose() {
        Fonts.release(titleFont);
        Fonts.release(nameFont);
        Fonts.release(textFont);
        Fonts.release(arrowFont);
    }
}
