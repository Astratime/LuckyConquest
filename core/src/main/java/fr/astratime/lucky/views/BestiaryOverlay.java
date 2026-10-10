package fr.astratime.lucky.views;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.EnemyTextures;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.entities.tower.Chapter;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.progress.Bestiary;
import fr.astratime.lucky.progress.PlayerProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Fenêtre « Bestiaire et collection » des Options. À gauche, la Tour des
 * épreuves (l'Entraînement puis une ligne par chapitre) ; à droite, l'Exploration
 * (une ligne par lieu, soldat puis roi de chaque donjon). Un ennemi pas encore
 * rencontré reste une silhouette « ??? » ; un ennemi vaincu porte une pièce.
 * Survoler ou cliquer un portrait montre sa fiche. Sous le titre, le compte
 * des ennemis, des cartes, des rouleaux et des cinématiques. « Revoir les
 * cinématiques » ouvre celles déjà vues ; « Retour », Échap ou un clic hors du
 * cadre ferment la fenêtre.
 */
public class BestiaryOverlay implements Disposable {

    private static final float PANEL_WIDTH  = 1760f;
    private static final float PANEL_HEIGHT = 980f;
    private static final float PAD          = 46f;
    private static final float CELL         = 76f;
    private static final float CELL_STEP    = 84f;
    private static final float PORTRAIT     = 62f;
    private static final float MARK         = 24f;
    private static final float TOWER_LABEL  = 196f;
    private static final float PLACE_LABEL  = 200f;
    private static final float BLOCK_GAP    = 60f;
    private static final float ART          = 220f;
    private static final float FADE         = 0.2f;
    /** Couleur d'un portrait pas encore découvert : une silhouette sombre. */
    private static final Color UNKNOWN      = new Color(0.06f, 0.05f, 0.08f, 1f);
    private static final Color LOCKED       = new Color(0.55f, 0.52f, 0.5f, 1f);

    /** Une case : son ennemi et ses acteurs. */
    private record Cell(EnemyKind kind, Image frame, Image portrait, Image mark) { }

    /** Une ligne de la grille : son nom et ses cases. */
    private record Row(Label label, List<Cell> cells) { }

    private final EnemyTextures textures;
    private final Group root = new Group();
    private final Image veil;
    private final Image panel;
    private final Image divider;
    private final Image selection;
    private final Label title;
    private final Label summary;
    private final Label towerTitle;
    private final Label explorationTitle;
    private final List<Row> towerRows = new ArrayList<>();
    private final List<Row> placeRows = new ArrayList<>();
    private final List<Cell> cells    = new ArrayList<>();
    private final Image artFrame;
    private final Image art;
    private final Label name;
    private final Label where;
    private final Label status;
    private final Label description;
    private final CasinoButton cutscenes;
    private final CasinoButton back;
    private final TextureRegionDrawable coin;
    private final BitmapFont titleFont = Fonts.jersey(64, Palette.GOLD, 4f, Palette.TEXT_SHADE);
    private final BitmapFont headFont  = Fonts.jersey(36, Palette.GOLD, 2f, Palette.TEXT_SHADE);
    private final BitmapFont nameFont  = Fonts.jersey(40, Color.WHITE, 3f, Palette.TEXT_SHADE);
    private final BitmapFont rowFont   = Fonts.jersey(28, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont textFont  = Fonts.jersey(24, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private PlayerProfile profile;
    private Cell          selected;
    private Runnable      onClose;

    /**
     * @param textures       portraits des ennemis (à l'appelant de les libérer)
     * @param clickSound     bruitage des boutons
     * @param onCutscenes    « Revoir les cinématiques »
     */
    public BestiaryOverlay(HudTextures hud, EnemyTextures textures, CasinoButtons buttons, Sound clickSound,
                           Runnable onCutscenes) {
        this.textures = textures;
        TextureRegionDrawable pixel = new TextureRegionDrawable(new TextureRegion(hud.pixel));
        coin = new TextureRegionDrawable(new TextureRegion(hud.coin));
        veil = new Image(pixel);
        veil.setColor(0f, 0f, 0f, 0.78f);
        panel = new Image(hud.panelDrawable());
        divider = new Image(pixel);
        divider.setColor(Palette.GOLD.r, Palette.GOLD.g, Palette.GOLD.b, 0.35f);
        selection = new Image(pixel);
        selection.setColor(Palette.GOLD);
        selection.setTouchable(Touchable.disabled);
        title = label(titleFont, Color.WHITE);
        summary = label(rowFont, Palette.CREAM);
        towerTitle = label(headFont, Color.WHITE);
        explorationTitle = label(headFont, Color.WHITE);
        root.addActor(veil);
        root.addActor(panel);
        root.addActor(divider);
        root.addActor(title);
        root.addActor(summary);
        root.addActor(towerTitle);
        root.addActor(explorationTitle);
        root.addActor(selection);

        towerRows.add(row(hud, List.of(EnemyKind.ENTRAINEMENT)));
        for (Chapter chapter : Chapter.values()) {
            List<EnemyKind> kinds = new ArrayList<>();
            kinds.add(chapter.getFirstEnemy());
            kinds.addAll(chapter.getChallengers());
            kinds.add(chapter.getBoss());
            towerRows.add(row(hud, kinds));
        }
        for (Place place : Place.values()) {
            List<EnemyKind> kinds = new ArrayList<>();
            for (Dungeon dungeon : place.getDungeons()) {
                kinds.add(dungeon.getSoldier());
                kinds.add(dungeon.getKing());
            }
            placeRows.add(row(hud, kinds));
        }

        artFrame = new Image(hud.insetDrawable());
        art = new Image();
        name = label(nameFont, Palette.GOLD);
        where = label(textFont, Palette.CREAM);
        status = label(textFont, Color.WHITE);
        description = label(textFont, Color.WHITE);
        description.setWrap(true);
        description.setAlignment(Align.topLeft);
        root.addActor(artFrame);
        root.addActor(art);
        root.addActor(name);
        root.addActor(where);
        root.addActor(status);
        root.addActor(description);

        cutscenes = buttons.create(Lang.t("Revoir les cinématiques"), clickSound, onCutscenes);
        back = buttons.create(Lang.t("Retour"), clickSound, this::hide);
        root.addActor(cutscenes);
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

    private Row row(HudTextures hud, List<EnemyKind> kinds) {
        Label label = label(rowFont, Palette.CREAM);
        root.addActor(label);
        List<Cell> rowCells = new ArrayList<>();
        for (EnemyKind kind : kinds) {
            Image frame = new Image(hud.insetDrawable());
            Image portrait = new Image();
            portrait.setTouchable(Touchable.disabled);
            Image mark = new Image(coin);
            mark.setTouchable(Touchable.disabled);
            Cell cell = new Cell(kind, frame, portrait, mark);
            frame.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    super.enter(event, x, y, pointer, fromActor);
                    if (pointer == -1) select(cell);
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    select(cell);
                }
            });
            root.addActor(frame);
            root.addActor(portrait);
            root.addActor(mark);
            rowCells.add(cell);
            cells.add(cell);
        }
        return new Row(label, rowCells);
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

    /** Ouvre la fenêtre avec le bestiaire et la collection de {@code profile} ; {@code onClose} à la fermeture. */
    public void show(PlayerProfile profile, Runnable onClose) {
        this.profile = profile;
        this.onClose = onClose;
        title.setText(Lang.t("BESTIAIRE ET COLLECTION"));
        towerTitle.setText(Lang.t("Tour des épreuves"));
        explorationTitle.setText(Lang.t("Exploration"));
        towerRows.get(0).label().setText(Lang.t("Entraînement"));
        for (Chapter chapter : Chapter.values()) towerRows.get(chapter.ordinal() + 1).label().setText(chapter.getLabel());
        for (Place place : Place.values()) placeRows.get(place.ordinal()).label().setText(place.getShortName());

        int met = 0, defeated = 0;
        for (Cell cell : cells) {
            boolean known = profile.hasMet(cell.kind());
            boolean beaten = profile.hasDefeated(cell.kind());
            if (known) met++;
            if (beaten) defeated++;
            Texture texture = textures.portrait(cell.kind());
            cell.portrait().setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
            cell.portrait().setColor(known ? Color.WHITE : UNKNOWN);
            cell.mark().setVisible(beaten);
        }
        int[] seenCutscenes = Bestiary.cutscenes(profile);
        summary.setText(Lang.f("Ennemis rencontrés : {0} / {1}     Vaincus : {2} / {1}     Cartes : {3} / {4}     "
                + "Rouleaux : {5} / {6}     Cinématiques : {7} / {8}",
            met, cells.size(), defeated, profile.getCollectedCardCount(), profile.getCatalogSize(),
            profile.getOwnedReels().size(), PlayerProfile.getReelCount(), seenCutscenes[0], seenCutscenes[1]));
        cutscenes.setDisabled(seenCutscenes[0] == 0);

        Cell first = cells.stream().filter(cell -> profile.hasMet(cell.kind())).findFirst().orElse(cells.get(0));
        selected = null;
        select(first);
        layout();
        root.setVisible(true);
        root.toFront();
        root.getColor().a = 0f;
        root.clearActions();
        root.addAction(Actions.fadeIn(FADE));
    }

    /** Montre la fiche de l'ennemi de {@code cell}. */
    private void select(Cell cell) {
        if (cell == selected || profile == null) return;
        selected = cell;
        EnemyKind kind = cell.kind();
        boolean known = profile.hasMet(kind);
        art.setDrawable(new TextureRegionDrawable(new TextureRegion(textures.portrait(kind))));
        art.setColor(known ? Color.WHITE : UNKNOWN);
        name.setText(known ? kind.getDisplayName() : "???");
        name.setColor(known ? Palette.GOLD : LOCKED);
        where.setText(Bestiary.whereMet(kind));
        if (profile.hasDefeated(kind)) {
            status.setText(Lang.t("Vaincu"));
            status.setColor(Palette.GOLD);
        } else if (known) {
            status.setText(Lang.t("Rencontré, pas encore vaincu"));
            status.setColor(Color.WHITE);
        } else {
            status.setText(Lang.t("Pas encore rencontré"));
            status.setColor(LOCKED);
        }
        description.setText(known ? kind.getDescription() : Lang.t("Affronte-le pour découvrir sa fiche."));
        description.setColor(known ? Color.WHITE : LOCKED);
        layout();
    }

    /** Ferme la fenêtre. */
    public void hide() {
        if (!root.isVisible()) return;
        root.clearActions();
        root.addAction(Actions.sequence(Actions.fadeOut(FADE), Actions.visible(false)));
        if (onClose != null) onClose.run();
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
        summary.pack();
        float summaryY = top - title.getHeight() - 8f - summary.getHeight();
        summary.setPosition(px + (PANEL_WIDTH - summary.getWidth()) / 2f, summaryY);
        float headY = summaryY - 24f;

        // La Tour, à gauche ; l'Exploration, à droite.
        float towerWidth = TOWER_LABEL + 5 * CELL_STEP;
        float placeWidth = PLACE_LABEL + 8 * CELL_STEP;
        float tx = px + (PANEL_WIDTH - towerWidth - BLOCK_GAP - placeWidth) / 2f;
        float ex = tx + towerWidth + BLOCK_GAP;
        towerTitle.pack();
        towerTitle.setPosition(tx, headY - towerTitle.getHeight());
        explorationTitle.pack();
        explorationTitle.setPosition(ex, headY - explorationTitle.getHeight());
        float rowsTop = headY - towerTitle.getHeight() - 14f;
        placeRows(towerRows, tx, TOWER_LABEL, rowsTop);
        float placesBottom = placeRows(placeRows, ex, PLACE_LABEL, rowsTop);
        divider.setBounds(tx + towerWidth + BLOCK_GAP / 2f - 1.5f, py + PAD + 90f, 3f, headY - (py + PAD + 90f));

        if (selected != null) {
            selection.setVisible(true);
            selection.setBounds(selected.frame().getX() - 4f, selected.frame().getY() - 4f, CELL + 8f, CELL + 8f);
        } else {
            selection.setVisible(false);
        }

        // La fiche, sous l'Exploration.
        float ficheTop = placesBottom - 30f;
        artFrame.setBounds(ex, ficheTop - ART, ART, ART);
        fit(art, ex + 12f, ficheTop - ART + 12f, ART - 24f);
        float textX = ex + ART + 30f;
        float textWidth = ex + placeWidth - textX;
        name.pack();
        name.setPosition(textX, ficheTop - name.getHeight());
        where.pack();
        where.setPosition(textX, name.getY() - 6f - where.getHeight());
        status.pack();
        status.setPosition(textX, where.getY() - 4f - status.getHeight());
        description.setWidth(textWidth);
        description.pack();
        description.setWidth(textWidth);
        description.setPosition(textX, status.getY() - 12f - description.getHeight());

        back.setPosition(ex + placeWidth - back.getWidth(), py + PAD);
        cutscenes.setPosition(back.getX() - 24f - cutscenes.getWidth(), py + PAD);
    }

    /** Place les lignes {@code rows} à partir de ({@code x}, {@code top}) ; @return le bas de la dernière. */
    private float placeRows(List<Row> rows, float x, float labelWidth, float top) {
        float y = top;
        for (Row row : rows) {
            y -= CELL_STEP;
            row.label().pack();
            row.label().setPosition(x, y + (CELL - row.label().getHeight()) / 2f);
            float cx = x + labelWidth;
            for (Cell cell : row.cells()) {
                cell.frame().setBounds(cx, y, CELL, CELL);
                fit(cell.portrait(), cx + (CELL - PORTRAIT) / 2f, y + (CELL - PORTRAIT) / 2f, PORTRAIT);
                cell.mark().setBounds(cx + CELL - MARK + 4f, y - 4f, MARK, MARK);
                cx += CELL_STEP;
            }
        }
        return y;
    }

    /** Met {@code image} dans un carré de côté {@code size}, sans le déformer. */
    private static void fit(Image image, float x, float y, float size) {
        if (image.getDrawable() == null) return;
        float w = image.getDrawable().getMinWidth();
        float h = image.getDrawable().getMinHeight();
        float scale = size / Math.max(w, h);
        image.setBounds(x + (size - w * scale) / 2f, y + (size - h * scale) / 2f, w * scale, h * scale);
    }

    @Override
    public void dispose() {
        Fonts.release(titleFont);
        Fonts.release(headFont);
        Fonts.release(nameFont);
        Fonts.release(rowFont);
        Fonts.release(textFont);
    }
}
