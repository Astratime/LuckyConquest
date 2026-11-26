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
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.progress.PlaceHistory;
import fr.astratime.lucky.progress.PlayerProfile;
import fr.astratime.lucky.progress.PlayerStats;
import fr.astratime.lucky.progress.PlayerStats.Stat;

/**
 * Fenêtre « Statistiques et succès » des Options : les statistiques de la
 * partie à gauche, les succès à droite (atteints en or avec leur pièce,
 * les autres en gris avec leur récompense), sur plusieurs pages. Le bouton « Historique » remplace les
 * succès par l'historique de l'Exploration, lieu par lieu (2026-11-20).
 * « Retour », Échap ou un clic hors du cadre la ferment.
 */
public class AchievementsOverlay implements Disposable {

    private static final float PANEL_WIDTH  = 1760f;
    private static final float PANEL_HEIGHT = 980f;
    private static final float PAD          = 46f;
    private static final float STATS_WIDTH  = 430f;
    private static final float ROW_HEIGHT   = 74f;
    private static final float ICON         = 40f;
    private static final float FADE         = 0.2f;
    /** Succès par page : deux colonnes de 11. */
    private static final int   PER_PAGE     = 22;
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
    private final CasinoButton previous;
    private final CasinoButton next;
    private final Label pageLabel;
    private int page;
    private final CasinoButton historyButton;
    private final CasinoButton achievementsButton;
    private final Group history = new Group();
    private final Label historyTitle;
    private final Label historyEmpty;
    private boolean historyMode;
    /** Colonnes de l'historique : lieu, combats, victoires, rois battus, pièces par combat, meilleur combat. */
    private static final float[] HISTORY_COLUMNS = {0f, 0.38f, 0.50f, 0.64f, 0.82f, 1f};
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
        previous = buttons.create(Lang.t("Précédent"), clickSound, () -> showPage(page - 1));
        next     = buttons.create(Lang.t("Suivant"), clickSound, () -> showPage(page + 1));
        pageLabel = label(statFont, Palette.CREAM);
        root.addActor(previous);
        root.addActor(next);
        root.addActor(pageLabel);
        historyButton = buttons.create(Lang.t("Historique"), clickSound, () -> showHistory(true));
        achievementsButton = buttons.create(Lang.t("Succès"), clickSound, () -> showHistory(false));
        root.addActor(historyButton);
        root.addActor(achievementsButton);
        historyTitle = label(headFont, Color.WHITE);
        historyEmpty = label(statFont, Palette.CREAM);
        root.addActor(historyTitle);
        root.addActor(historyEmpty);
        root.addActor(history);
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
            rewards[i].setText((done ? "" : "+") + PlayerProfile.priceTag(achievement.getReward()));
            rewards[i].setColor(done ? Palette.GOLD : LOCKED);
        }
        fillHistory(profile.getHistory());
        page = 0;
        historyMode = false;
        layout();
        root.setVisible(true);
        root.toFront();
        root.getColor().a = 0f;
        root.clearActions();
        root.addAction(Actions.fadeIn(FADE));
    }

    /** Remplit le tableau de l'historique : en-tête, puis un lieu par ligne (le mode difficile s'il a été joué). */
    private void fillHistory(PlaceHistory places) {
        history.clearChildren();
        historyTitle.setText(Lang.t("Historique de l'Exploration"));
        historyEmpty.setText(Lang.t("Aucun combat de l'Exploration pour l'instant."));
        addHistoryRow(Palette.GOLD, textFont, Lang.t("Lieu"), Lang.t("Combats"), Lang.t("Victoires"), Lang.t("Rois battus"),
            Lang.t("Pièces / combat"), Lang.t("Meilleur combat"));
        for (boolean hard : new boolean[]{false, true}) {
            boolean titled = !hard;
            for (Place place : Place.values()) {
                PlaceHistory.Line line = places.get(place, hard);
                if (line.fights() == 0) continue;
                if (!titled) { // les lieux du mode difficile, sous leur propre titre
                    addHistoryRow(Palette.GOLD, textFont, Lang.t("Mode difficile"), "", "", "", "", "");
                    titled = true;
                }
                addHistoryRow(hard ? Palette.CREAM : Color.WHITE, statFont, place.getName(),
                    Lang.grouped(line.fights()), line.winPercent() + " %",
                    Lang.grouped(line.kingWins()) + " / " + Lang.grouped(line.kings()),
                    PlayerProfile.formatCoins(line.coinsPerFight()), PlayerProfile.formatCoins(line.best()));
            }
        }
    }

    private void addHistoryRow(Color color, BitmapFont font, String... cells) {
        for (String cell : cells) {
            Label label = label(font, color);
            label.setText(cell);
            history.addActor(label);
        }
    }

    /** Montre l'historique ({@code true}) ou les succès à la place. */
    private void showHistory(boolean shown) {
        historyMode = shown;
        layout();
    }

    private static int pageCount() { return (Achievement.values().length + PER_PAGE - 1) / PER_PAGE; }

    /** Montre la page {@code index} des succès (bornée aux pages existantes). */
    private void showPage(int index) {
        page = Math.max(0, Math.min(pageCount() - 1, index));
        layout();
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
            case SPINS       -> Lang.t("Tirages");
            case CLOSE_WINS, FLAWLESS_WINS -> ""; // pas montrées
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
            boolean shown = Stat.values()[i].isShown();
            statNames[i].setVisible(shown);
            statValues[i].setVisible(shown);
            if (!shown) continue;
            statNames[i].pack();
            statValues[i].pack();
            y -= ROW_HEIGHT;
            statNames[i].setPosition(sx, y);
            statValues[i].setPosition(sx + STATS_WIDTH - statValues[i].getWidth(), y);
        }
        float dividerX = sx + STATS_WIDTH + PAD / 2f;
        divider.setBounds(dividerX, py + PAD + 70f, 3f, headY - (py + PAD + 70f));

        // Succès, à droite : deux colonnes, une page à la fois ; les boutons de page en bas.
        float ax = dividerX + PAD / 2f + 10f;
        achievementsTitle.pack();
        achievementsTitle.setPosition(ax, headY - achievementsTitle.getHeight());
        float right = px + PANEL_WIDTH - PAD;
        float columnWidth = (right - ax - PAD) / 2f;
        int perColumn = PER_PAGE / 2;
        boolean paged = pageCount() > 1;
        float rowsBottom = paged ? py + PAD + CasinoButtons.HEIGHT + 12f : py + PAD / 2f;
        float rowsTop = headY - achievementsTitle.getHeight() - 16f;
        float rowHeight = Math.min(ROW_HEIGHT, (rowsTop - rowsBottom) / perColumn); // une page tient dans le cadre
        int first = page * PER_PAGE;
        for (int i = 0; i < names.length; i++) {
            boolean onPage = i >= first && i < first + PER_PAGE;
            icons[i].setVisible(onPage);
            names[i].setVisible(onPage);
            descriptions[i].setVisible(onPage);
            rewards[i].setVisible(onPage);
            if (!onPage) continue;
            int slot = i - first;
            float cx = ax + (slot / perColumn) * (columnWidth + PAD);
            float ry = rowsTop - (slot % perColumn + 1) * rowHeight;
            icons[i].setBounds(cx, ry + (rowHeight - ICON) / 2f, ICON, ICON);
            names[i].pack();
            descriptions[i].pack();
            rewards[i].pack();
            float textX = cx + ICON + 14f;
            names[i].setPosition(textX, ry + rowHeight / 2f);
            descriptions[i].setPosition(textX, ry + rowHeight / 2f - descriptions[i].getHeight() - 2f);
            rewards[i].setPosition(cx + columnWidth - rewards[i].getWidth(), ry + rowHeight / 2f);
        }

        // « Retour » et le bouton qui passe des succès à l'historique, côte à côte sous les statistiques
        CasinoButton toggle = historyMode ? achievementsButton : historyButton;
        historyButton.setVisible(!historyMode);
        achievementsButton.setVisible(historyMode);
        back.setPosition(sx + (STATS_WIDTH / 2f - back.getWidth()) / 2f, py + PAD);
        toggle.setPosition(sx + STATS_WIDTH / 2f + (STATS_WIDTH / 2f - toggle.getWidth()) / 2f, py + PAD);

        achievementsTitle.setVisible(!historyMode);
        historyTitle.setVisible(historyMode);
        history.setVisible(historyMode);
        int cells = HISTORY_COLUMNS.length;
        int rows = history.getChildren().size / cells;
        historyEmpty.setVisible(historyMode && rows <= 1);
        if (historyMode) {
            for (int i = 0; i < names.length; i++) {
                icons[i].setVisible(false);
                names[i].setVisible(false);
                descriptions[i].setVisible(false);
                rewards[i].setVisible(false);
            }
            historyTitle.pack();
            historyTitle.setPosition(ax, headY - historyTitle.getHeight());
            float tableTop = headY - historyTitle.getHeight() - 24f;
            float tableWidth = right - ax;
            float lineHeight = Math.min(ROW_HEIGHT * 0.7f, (tableTop - (py + PAD)) / Math.max(1, rows));
            for (int r = 0; r < rows; r++) {
                float ry = tableTop - (r + 1) * lineHeight;
                for (int c = 0; c < cells; c++) {
                    Label cell = (Label) history.getChildren().get(r * cells + c);
                    cell.pack();
                    float x = c == 0 ? ax : ax + tableWidth * HISTORY_COLUMNS[c] - cell.getWidth(); // chiffres à droite
                    cell.setPosition(x, ry);
                }
            }
            historyEmpty.pack();
            historyEmpty.setPosition(ax, tableTop - 2.5f * lineHeight);
            previous.setVisible(false);
            next.setVisible(false);
            pageLabel.setVisible(false);
            return;
        }

        previous.setVisible(paged);
        next.setVisible(paged);
        pageLabel.setVisible(paged);
        if (paged) {
            previous.setDisabled(page == 0);
            next.setDisabled(page == pageCount() - 1);
            pageLabel.setText(Lang.f("Page {0} / {1}", page + 1, pageCount()));
            pageLabel.pack();
            next.setPosition(right - next.getWidth(), py + PAD);
            pageLabel.setPosition(next.getX() - 24f - pageLabel.getWidth(),
                py + PAD + (CasinoButtons.HEIGHT - pageLabel.getHeight()) / 2f);
            previous.setPosition(pageLabel.getX() - 24f - previous.getWidth(), py + PAD);
        }
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
