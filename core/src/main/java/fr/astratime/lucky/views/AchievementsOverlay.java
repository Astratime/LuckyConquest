package fr.astratime.lucky.views;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.progress.Achievement;
import fr.astratime.lucky.progress.PlayerProfile;
import fr.astratime.lucky.progress.PlayerStats;
import fr.astratime.lucky.progress.PlayerStats.Stat;

/**
 * Fenêtre « Statistiques et succès » des Options : les statistiques de la
 * partie à gauche, les succès à droite (atteints en or avec leur pièce,
 * les autres en gris avec leur récompense). « Retour », Échap ou un clic
 * hors du cadre la ferment.
 */
public class AchievementsOverlay implements Disposable {

    private static final float PANEL_WIDTH  = 1760f;
    private static final float PANEL_HEIGHT = 980f;
    private static final float PAD          = 46f;
    private static final float STATS_WIDTH  = 430f;
    private static final float ROW_HEIGHT   = 74f;
    private static final float ICON         = 40f;
    private static final float FADE         = 0.2f;
    private static final Color LOCKED       = new Color(0.55f, 0.52f, 0.5f, 1f);
    private static final Color LOCKED_COIN  = new Color(0.22f, 0.2f, 0.2f, 1f);

    private final Group root = new Group();
    private final Image veil;
    private final Image panel;
    private final Image divider;
    private final Label title;
    private final Label statsTitle;
    private final Label achievementsTitle;
    private final Label[] statNames  = new Label[Stat.values().length];
    private final Label[] statValues = new Label[Stat.values().length];
    private final Image[] icons        = new Image[Achievement.values().length];
    private final Label[] names        = new Label[Achievement.values().length];
    private final Label[] descriptions = new Label[Achievement.values().length];
    private final Label[] rewards      = new Label[Achievement.values().length];
    private final CasinoButton back;
    private final TextureRegionDrawable coin;
    private final BitmapFont titleFont   = Fonts.jersey(64, Palette.GOLD, 4f, Palette.TEXT_SHADE);
    private final BitmapFont headFont    = Fonts.jersey(36, Palette.GOLD, 2f, Palette.TEXT_SHADE);
    private final BitmapFont nameFont    = Fonts.jersey(28, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont textFont    = Fonts.jersey(22, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont statFont    = Fonts.jersey(28, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private Runnable onClose;

    /** @param clickSound bruitage du bouton « Retour » */
    public AchievementsOverlay(HudTextures hud, CasinoButtons buttons, Sound clickSound) {
        TextureRegionDrawable pixel = new TextureRegionDrawable(new TextureRegion(hud.pixel));
        coin = new TextureRegionDrawable(new TextureRegion(hud.coin));
        veil = new Image(pixel);
        veil.setColor(0f, 0f, 0f, 0.78f);
        panel = new Image(hud.panelDrawable());
        divider = new Image(pixel);
        divider.setColor(Palette.GOLD.r, Palette.GOLD.g, Palette.GOLD.b, 0.35f);
        title = label(titleFont, Color.WHITE);
        statsTitle = label(headFont, Color.WHITE);
        achievementsTitle = label(headFont, Color.WHITE);
        root.addActor(veil);
        root.addActor(panel);
        root.addActor(divider);
        root.addActor(title);
        root.addActor(statsTitle);
        root.addActor(achievementsTitle);
        for (int i = 0; i < statNames.length; i++) {
            statNames[i]  = label(statFont, Palette.CREAM);
            statValues[i] = label(statFont, Color.WHITE);
            root.addActor(statNames[i]);
            root.addActor(statValues[i]);
        }
        for (int i = 0; i < names.length; i++) {
            icons[i]        = new Image();
            names[i]        = label(nameFont, Color.WHITE);
            descriptions[i] = label(textFont, Palette.CREAM);
            rewards[i]      = label(nameFont, Palette.GOLD);
            root.addActor(icons[i]);
            root.addActor(names[i]);
            root.addActor(descriptions[i]);
            root.addActor(rewards[i]);
        }
        back = buttons.create(Lang.t("Retour"), clickSound, this::hide);
        root.addActor(back);
        root.setVisible(false);
        root.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // Un clic hors du cadre ferme la fenêtre ; dedans, il ne traverse pas.
                if (event.getTarget() == veil) hide();
                return true;
            }
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

    /** Ouvre la fenêtre avec les statistiques et les succès de {@code profile} ; {@code onClose} à la fermeture. */
    public void show(PlayerProfile profile, Runnable onClose) {
        this.onClose = onClose;
        PlayerStats stats = profile.getStats();
        title.setText(Lang.t("STATISTIQUES ET SUCCÈS"));
        statsTitle.setText(Lang.t("Statistiques"));
        int unlocked = 0;
        for (Achievement achievement : Achievement.values()) if (profile.isUnlocked(achievement)) unlocked++;
        achievementsTitle.setText(Lang.f("Succès : {0} / {1}", unlocked, Achievement.values().length));
        for (Stat stat : Stat.values()) {
            statNames[stat.ordinal()].setText(statName(stat));
            long value = stats.get(stat);
            statValues[stat.ordinal()].setText(stat == Stat.COINS || stat == Stat.GAINS || stat == Stat.BEST_HIT
                ? Lang.big(value) : Lang.grouped(value));
        }
        for (Achievement achievement : Achievement.values()) {
            int i = achievement.ordinal();
            boolean done = profile.isUnlocked(achievement);
            icons[i].setDrawable(coin);
            icons[i].setColor(done ? Color.WHITE : LOCKED_COIN); // pas encore atteint : une pièce éteinte
            names[i].setText(achievement.getName());
            names[i].setColor(done ? Palette.GOLD : LOCKED);
            descriptions[i].setText(achievement.getDescription());
            descriptions[i].setColor(done ? Palette.CREAM : LOCKED);
            rewards[i].setText((done ? "" : "+") + PlayerProfile.formatCoins(achievement.getReward()));
            rewards[i].setColor(done ? Palette.GOLD : LOCKED);
        }
        layout();
        root.setVisible(true);
        root.toFront();
        root.getColor().a = 0f;
        root.clearActions();
        root.addAction(Actions.fadeIn(FADE));
    }

    /** Ferme la fenêtre. */
    public void hide() {
        if (!root.isVisible()) return;
        root.clearActions();
        root.addAction(Actions.sequence(Actions.fadeOut(FADE), Actions.visible(false)));
        if (onClose != null) onClose.run();
    }

    private static String statName(Stat stat) {
        return switch (stat) {
            case WINS        -> Lang.t("Combats gagnés");
            case BEST_HIT    -> Lang.t("Plus gros coup");
            case BINGOS      -> Lang.t("Bingos");
            case BONUS_GAMES -> Lang.t("Jeux bonus");
            case GAINS       -> Lang.t("Gains totaux");
            case COINS       -> Lang.t("Pièces gagnées");
            case SQUARES     -> Lang.t("Carrés");
        };
    }

    /** Place la fenêtre au centre du Stage. */
    public void layout() {
        if (root.getStage() == null) return;
        float width  = root.getStage().getViewport().getWorldWidth();
        float height = root.getStage().getViewport().getWorldHeight();
        veil.setBounds(0f, 0f, width, height);
        float px = (width - PANEL_WIDTH) / 2f;
        float py = (height - PANEL_HEIGHT) / 2f;
        panel.setBounds(px, py, PANEL_WIDTH, PANEL_HEIGHT);

        title.pack();
        float top = py + PANEL_HEIGHT - PAD;
        title.setPosition(px + (PANEL_WIDTH - title.getWidth()) / 2f, top - title.getHeight());
        float headY = top - title.getHeight() - 24f;

        // Statistiques, à gauche.
        float sx = px + PAD;
        statsTitle.pack();
        statsTitle.setPosition(sx, headY - statsTitle.getHeight());
        float y = headY - statsTitle.getHeight() - 30f;
        for (int i = 0; i < statNames.length; i++) {
            statNames[i].pack();
            statValues[i].pack();
            y -= ROW_HEIGHT;
            statNames[i].setPosition(sx, y);
            statValues[i].setPosition(sx + STATS_WIDTH - statValues[i].getWidth(), y);
        }
        float dividerX = sx + STATS_WIDTH + PAD / 2f;
        divider.setBounds(dividerX, py + PAD + 70f, 3f, headY - (py + PAD + 70f));

        // Succès, à droite : deux colonnes.
        float ax = dividerX + PAD / 2f + 10f;
        achievementsTitle.pack();
        achievementsTitle.setPosition(ax, headY - achievementsTitle.getHeight());
        float columnWidth = (px + PANEL_WIDTH - PAD - ax - PAD) / 2f;
        int perColumn = (names.length + 1) / 2;
        float rowsTop = headY - achievementsTitle.getHeight() - 16f;
        for (int i = 0; i < names.length; i++) {
            float cx = ax + (i / perColumn) * (columnWidth + PAD);
            float ry = rowsTop - (i % perColumn + 1) * ROW_HEIGHT;
            icons[i].setBounds(cx, ry + (ROW_HEIGHT - ICON) / 2f, ICON, ICON);
            names[i].pack();
            descriptions[i].pack();
            rewards[i].pack();
            float textX = cx + ICON + 14f;
            names[i].setPosition(textX, ry + ROW_HEIGHT / 2f);
            descriptions[i].setPosition(textX, ry + ROW_HEIGHT / 2f - descriptions[i].getHeight() - 2f);
            rewards[i].setPosition(cx + columnWidth - rewards[i].getWidth(), ry + ROW_HEIGHT / 2f);
        }

        back.setPosition(sx + (STATS_WIDTH - back.getWidth()) / 2f, py + PAD);
    }

    @Override
    public void dispose() {
        Fonts.release(titleFont);
        Fonts.release(headFont);
        Fonts.release(nameFont);
        Fonts.release(textFont);
        Fonts.release(statFont);
    }
}
