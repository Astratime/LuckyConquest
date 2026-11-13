package fr.astratime.lucky.views;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.List;

/**
 * Fenêtre « Lexique des règles » (Options > Jeu, et menu pause) : les mots du
 * jeu ({@link Lexicon}) en onglets par thème, chacun avec son explication,
 * sur deux colonnes. Quand un thème ne tient pas dans le cadre (petite
 * fenêtre), il passe sur plusieurs pages. « Retour », Échap (géré par
 * l'appelant via {@link #hide()}) ou un clic hors du cadre la ferment.
 */
public class LexiconOverlay implements Disposable {

    private static final float MAX_WIDTH   = 1760f;
    private static final float MAX_HEIGHT  = 980f;
    private static final float MARGIN      = 30f;   // entre le cadre et le bord de l'écran
    private static final float PAD         = 46f;
    private static final float TAB_GAP     = 12f;
    private static final float TERM_GAP    = 18f;   // entre deux mots
    private static final float COLUMN_GAP  = 60f;
    private static final float FADE        = 0.2f;
    private static final Color TAB_DIMMED  = new Color(0.55f, 0.55f, 0.55f, 1f);

    private final Group root = new Group();
    private final Image veil;
    private final Image panel;
    private final Label title;
    private final Label pageLabel;
    private final CasinoButtons buttons;
    private final Sound clickSound;
    private final CasinoButton back;
    private final CasinoButton previous;
    private final CasinoButton next;
    private final List<CasinoButton> tabs  = new ArrayList<>();
    private final List<Label>        names = new ArrayList<>();
    private final List<Label>        texts = new ArrayList<>();
    private final BitmapFont titleFont = Fonts.jersey(64, Palette.GOLD, 4f, Palette.TEXT_SHADE);
    private final BitmapFont nameFont  = Fonts.jersey(30, Palette.GOLD, 2f, Palette.TEXT_SHADE);
    private final BitmapFont textFont  = Fonts.jersey(24, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont pageFont  = Fonts.jersey(28, Color.WHITE, 2f, Palette.TEXT_SHADE);

    private List<Lexicon.Category> categories = List.of();
    private int category;
    private int page;
    /** Premier mot de chaque page du thème affiché (recalculé à chaque mise en page). */
    private final List<Integer> pageStarts = new ArrayList<>();
    private Runnable onClose;

    /** @param clickSound bruitage des boutons (onglets, pages, « Retour ») */
    public LexiconOverlay(HudTextures hud, CasinoButtons buttons, Sound clickSound) {
        this.buttons    = buttons;
        this.clickSound = clickSound;
        veil = new Image(new TextureRegionDrawable(new TextureRegion(hud.pixel)));
        veil.setColor(0f, 0f, 0f, 0.78f);
        panel = new Image(hud.panelDrawable());
        title = new Label("", new Label.LabelStyle(titleFont, Color.WHITE));
        pageLabel = new Label("", new Label.LabelStyle(pageFont, Color.WHITE));
        pageLabel.setColor(Palette.CREAM);
        root.addActor(veil);
        root.addActor(panel);
        root.addActor(title);
        root.addActor(pageLabel);
        back     = buttons.create(Lang.t("Retour"), clickSound, this::hide);
        previous = buttons.create(Lang.t("Précédent"), clickSound, () -> showPage(page - 1));
        next     = buttons.create(Lang.t("Suivant"), clickSound, () -> showPage(page + 1));
        root.addActor(back);
        root.addActor(previous);
        root.addActor(next);
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

    /** @return l'acteur racine de la fenêtre, à ajouter au Stage par-dessus l'écran. */
    public Group getActor() { return root; }

    /** @return {@code true} tant que la fenêtre est affichée. */
    public boolean isShown() { return root.isVisible(); }

    /** Ouvre la fenêtre sur le premier thème ; {@code onClose} à la fermeture. */
    public void show(Runnable onClose) {
        this.onClose = onClose;
        categories = Lexicon.categories(); // relus à chaque ouverture : la langue a pu changer
        title.setText(Lang.t("LEXIQUE DES RÈGLES"));
        buttons.setText(back, Lang.t("Retour"));
        buttons.setText(previous, Lang.t("Précédent"));
        buttons.setText(next, Lang.t("Suivant"));
        showCategory(0);
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

    /** Affiche le thème {@code index} (onglet enfoncé), sur sa première page. */
    private void showCategory(int index) {
        category = index;
        page = 0;
        tabs.forEach(Actor::remove);
        tabs.clear();
        for (int i = 0; i < categories.size(); i++) {
            int other = i;
            CasinoButton tab = buttons.create(categories.get(i).title(), clickSound, () -> {
                if (other != category) showCategory(other);
            });
            if (i == index) tab.getStyle().up = tab.getStyle().down; // l'onglet affiché reste « enfoncé »
            else tab.setColor(TAB_DIMMED);
            tabs.add(tab);
            root.addActor(tab);
        }
        names.forEach(Actor::remove);
        texts.forEach(Actor::remove);
        names.clear();
        texts.clear();
        for (Lexicon.Term term : categories.get(index).terms()) {
            Label name = new Label(term.name(), new Label.LabelStyle(nameFont, Color.WHITE));
            Label text = new Label(term.description(), new Label.LabelStyle(textFont, Color.WHITE));
            text.setColor(Palette.CREAM);
            text.setWrap(true);
            text.setAlignment(Align.topLeft);
            names.add(name);
            texts.add(text);
            root.addActor(name);
            root.addActor(text);
        }
        layout();
    }

    /** Montre la page {@code index} du thème (bornée aux pages existantes). */
    private void showPage(int index) {
        page = Math.max(0, Math.min(pageStarts.size() - 1, index));
        layout();
    }

    /** Place la fenêtre au centre du Stage, et les mots du thème en deux colonnes, page par page. */
    public void layout() {
        if (root.getStage() == null || categories.isEmpty()) return;
        float width  = root.getStage().getViewport().getWorldWidth();
        float height = root.getStage().getViewport().getWorldHeight();
        veil.setBounds(0f, 0f, width, height);
        float panelWidth  = Math.min(MAX_WIDTH, width - MARGIN * 2f);
        float panelHeight = Math.min(MAX_HEIGHT, height - MARGIN * 2f);
        float px = (width - panelWidth) / 2f;
        float py = (height - panelHeight) / 2f;
        panel.setBounds(px, py, panelWidth, panelHeight);

        title.pack();
        float top = py + panelHeight - PAD * 0.8f;
        title.setPosition(px + (panelWidth - title.getWidth()) / 2f, top - title.getHeight());

        // Onglets des thèmes, centrés sous le titre.
        float tabsWidth = -TAB_GAP;
        for (CasinoButton tab : tabs) tabsWidth += tab.getWidth() + TAB_GAP;
        float tabX = px + (panelWidth - tabsWidth) / 2f;
        float tabY = top - title.getHeight() - 16f - CasinoButtons.HEIGHT;
        for (CasinoButton tab : tabs) {
            tab.setPosition(tabX, tabY);
            tabX += tab.getWidth() + TAB_GAP;
        }

        // Les mots : la première colonne se remplit, puis la seconde, puis une nouvelle page.
        float left   = px + PAD;
        float right  = px + panelWidth - PAD;
        float columnWidth = (right - left - COLUMN_GAP) / 2f;
        float rowsTop    = tabY - 30f;
        float rowsBottom = py + PAD + CasinoButtons.HEIGHT + 20f;
        float[] heights = new float[names.size()];
        for (int i = 0; i < names.size(); i++) {
            names.get(i).pack();
            texts.get(i).setWidth(columnWidth);
            heights[i] = names.get(i).getHeight() + texts.get(i).getPrefHeight() + TERM_GAP;
        }
        pageStarts.clear();
        int[] column = new int[names.size()];
        float[] ys = new float[names.size()];
        int col = 0;
        float y = rowsTop;
        for (int i = 0; i < names.size(); i++) {
            boolean firstOfPage = pageStarts.isEmpty();
            if (!firstOfPage && y - heights[i] < rowsBottom && y < rowsTop) { // ne tient plus : colonne suivante
                col++;
                y = rowsTop;
            }
            if (firstOfPage || col == 2) {
                pageStarts.add(i);
                col = 0;
                y = rowsTop;
            }
            column[i] = col;
            ys[i] = y;
            y -= heights[i];
        }
        page = Math.max(0, Math.min(pageStarts.size() - 1, page));
        int first = pageStarts.get(page);
        int end   = page + 1 < pageStarts.size() ? pageStarts.get(page + 1) : names.size();
        for (int i = 0; i < names.size(); i++) {
            boolean onPage = i >= first && i < end;
            Label name = names.get(i);
            Label text = texts.get(i);
            name.setVisible(onPage);
            text.setVisible(onPage);
            if (!onPage) continue;
            float x = left + column[i] * (columnWidth + COLUMN_GAP);
            name.setPosition(x, ys[i] - name.getHeight());
            text.setSize(columnWidth, text.getPrefHeight());
            text.setPosition(x, ys[i] - name.getHeight() - text.getHeight());
        }

        back.setPosition(left, py + PAD);
        boolean paged = pageStarts.size() > 1;
        previous.setVisible(paged);
        next.setVisible(paged);
        pageLabel.setVisible(paged);
        if (paged) {
            previous.setDisabled(page == 0);
            next.setDisabled(page == pageStarts.size() - 1);
            pageLabel.setText(Lang.f("Page {0} / {1}", page + 1, pageStarts.size()));
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
        Fonts.release(nameFont);
        Fonts.release(textFont);
        Fonts.release(pageFont);
    }
}
