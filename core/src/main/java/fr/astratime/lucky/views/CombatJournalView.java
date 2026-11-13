package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.entities.CombatJournal;
import fr.astratime.lucky.i18n.Lang;

import java.util.List;

/**
 * Panneau repliable du journal du combat ({@link CombatJournal}) : il s'ouvre
 * sous le bouton « Journal » (ou avec la touche J) et se replie de la même
 * façon, ou avec Échap. Les derniers tours s'y lisent du plus récent au plus
 * ancien : ce que le joueur a fait pendant son tour, puis ce qui s'est passé
 * pendant celui de l'ennemi. S'il y a plus de lignes que de place, le panneau
 * défile (molette, ou en glissant). Le panneau ne met pas le combat en pause.
 */
public class CombatJournalView implements Disposable {

    /** Largeur du panneau. */
    public static final float WIDTH = 560f;

    private static final float PADDING    = 22f;
    private static final float TURN_GAP   = 16f;  // entre deux tours
    private static final float SIDE_GAP   = 6f;   // entre le tour du joueur et celui de l'ennemi
    private static final float LINE_GAP   = 2f;
    private static final float INDENT     = 18f;  // retrait des lignes sous leur titre
    private static final float SCROLL_GAP = 12f;  // la barre de défilement ne recouvre jamais le texte

    private final BitmapFont titleFont   = Fonts.jersey(34, Palette.TEXT_TITLE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont captionFont = Fonts.jersey(28, Palette.TEXT_TITLE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont lineFont    = Fonts.jersey(24, Palette.TEXT_BODY, 2f, Palette.TEXT_SHADE);
    private final BitmapFont hintFont    = Fonts.jersey(20, Palette.TEXT_BODY, 2f, Palette.TEXT_SHADE);

    private final Table root = new Table();
    private final Table rows = new Table();
    private final ScrollPane scroll;
    private final float textWidth = WIDTH - PADDING * 2 - INDENT - SCROLL_GAP;

    public CombatJournalView(HudTextures hud) {
        root.setBackground(hud.panelDrawable());
        root.pad(PADDING).top().left();
        root.setTouchable(Touchable.enabled); // les clics sur le panneau ne passent pas à la table dessous
        Label title = new Label(Lang.t("JOURNAL DU COMBAT"), new Label.LabelStyle(titleFont, Color.WHITE));
        root.add(title).left();
        root.row();
        Label hint = new Label(Lang.t("Les derniers tours, du plus récent au plus ancien. J pour replier."),
            new Label.LabelStyle(hintFont, Color.WHITE));
        hint.setWrap(true);
        hint.getColor().a = 0.75f;
        root.add(hint).width(WIDTH - PADDING * 2).left().padTop(LINE_GAP);
        root.row();
        rows.top().left().padRight(SCROLL_GAP);
        scroll = new ScrollPane(rows, SidePanel.scrollStyle(hud));
        scroll.setScrollingDisabled(true, false);
        scroll.setOverscroll(false, false);
        scroll.setFadeScrollBars(false);
        scroll.setScrollbarsOnTop(true);
        scroll.setScrollBarPositions(false, true);
        SidePanel.addWheelFocus(scroll);
        root.add(scroll).width(WIDTH - PADDING * 2).growY().left().padTop(TURN_GAP / 2f);
        root.setVisible(false);
        // Le panneau garde la molette tant que la souris est dessus (même au-dessus du titre).
        root.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && event.getStage() != null) event.getStage().setScrollFocus(scroll);
            }
        });
    }

    /** Remplit le panneau avec {@code entries}, du plus récent au plus ancien. */
    public void setEntries(List<CombatJournal.Entry> entries) {
        rows.clearChildren();
        if (entries.isEmpty()) {
            addLine(Lang.t("Rien pour l'instant : le journal se remplit à la fin de chaque tour."), 0f);
        }
        boolean first = true;
        for (CombatJournal.Entry entry : entries) {
            Label turn = new Label(Lang.f("Tour {0}", entry.turn()), new Label.LabelStyle(captionFont, Color.WHITE));
            rows.add(turn).left().padTop(first ? 0f : TURN_GAP);
            rows.row();
            first = false;
            addCaption(Lang.t("Ton tour"));
            for (String line : entry.playerLines()) addLine(line, INDENT);
            if (entry.enemyPlayed()) {
                addCaption(Lang.f("Tour de {0}", entry.enemyName()));
                for (String line : entry.enemyLines()) addLine(line, INDENT);
            }
        }
        rows.invalidateHierarchy();
        scroll.layout();
        scroll.setScrollY(0f); // le tour le plus récent, en haut
    }

    private void addCaption(String text) {
        Label caption = new Label(text, new Label.LabelStyle(lineFont, Color.WHITE));
        caption.setColor(Palette.GOLD_PALE);
        rows.add(caption).left().padTop(SIDE_GAP);
        rows.row();
    }

    private void addLine(String text, float indent) {
        Label label = new Label(text, new Label.LabelStyle(lineFont, Color.WHITE));
        label.setWrap(true);              // une longue liste de cartes passe à la ligne
        label.setAlignment(Align.left);
        label.setWidth(textWidth);
        rows.add(label).width(textWidth + (INDENT - indent)).padLeft(indent).padTop(LINE_GAP).left();
        rows.row();
    }

    /**
     * Place le panneau : son coin haut gauche en ({@code left}, {@code top}), sa
     * hauteur au plus {@code maxHeight} (au-delà, il défile).
     */
    public void layout(float left, float top, float maxHeight) {
        root.setSize(WIDTH, 0f);
        root.invalidate();
        float height = Math.min(maxHeight, root.getPrefHeight());
        root.setBounds(left, top - height, WIDTH, height);
        root.validate();
    }

    /** Ouvre le panneau s'il est replié, le replie sinon. */
    public void toggle() {
        root.setVisible(!root.isVisible());
        if (root.isVisible()) root.toFront();
    }

    /** Replie le panneau. */
    public void hide() {
        root.setVisible(false);
    }

    /** @return {@code true} si le panneau est ouvert. */
    public boolean isShown() {
        return root.isVisible();
    }

    /** @return le panneau, à ajouter au Stage. */
    public Table getActor() { return root; }

    @Override
    public void dispose() {
        Fonts.release(titleFont);
        Fonts.release(captionFont);
        Fonts.release(lineFont);
        Fonts.release(hintFont);
    }
}
