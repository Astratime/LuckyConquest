package fr.astratime.lucky.views;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.EnemyTextures;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.DungeonRun;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.entities.tower.Chapter;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.progress.Bestiary;
import fr.astratime.lucky.progress.Notebook;
import fr.astratime.lucky.progress.PlayerProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fenêtre « Bestiaire et collection » des Options. À gauche, la Tour des
 * épreuves (l'Entraînement puis une ligne par chapitre) ; à droite, l'Exploration
 * (une ligne par lieu, soldat puis roi de chaque donjon). Un ennemi pas encore
 * rencontré reste une silhouette « ??? » ; un ennemi vaincu porte une pièce.
 * Survoler ou cliquer un portrait montre sa fiche. Sous le titre, le compte
 * des ennemis, des cartes, des rouleaux et des cinématiques. « Revoir les
 * cinématiques » ouvre celles déjà vues ; « Retour », Échap ou un clic hors du
 * cadre ferment la fenêtre.
 *
 * <p>Les cases prennent la place qu'il y a : elles rapetissent (jusqu'à
 * {@link #MIN_STEP}) pour que les dix chapitres et les dix lieux tiennent avec
 * la fiche et les boutons ; s'il y a encore trop de lignes, chaque colonne
 * défile à la molette.</p>
 *
 * <p>La même fenêtre sert à l'Entraînement libre ({@link #showTraining}) : on y
 * choisit le croupier d'entraînement ou un ennemi déjà vaincu, puis « Combattre »
 * (ou un double-clic sur son portrait) lance le combat.</p>
 */
public class BestiaryOverlay implements Disposable {

    private static final float PANEL_WIDTH  = 1760f;
    /** Hauteur du cadre au plus ; il garde {@link #MARGIN} au-dessus et au-dessous dans une fenêtre plus basse. */
    private static final float PANEL_HEIGHT = 1040f;
    private static final float MARGIN       = 20f;
    private static final float PAD          = 36f;
    /** Pas des cases (case + espace) au plus et au moins ; entre les deux, ce qui tient à l'écran. */
    private static final float MAX_STEP     = 84f;
    private static final float MIN_STEP     = 50f;
    /** Espace entre deux cases. */
    private static final float CELL_GAP     = 8f;
    /** Bord laissé autour des cases dans une colonne qui défile (contour de sélection, pièce). */
    private static final float EDGE         = 6f;
    /** Place de la barre de défilement, à droite d'une colonne. */
    private static final float SCROLL_ROOM  = 14f;
    /** Espace entre le nom d'une ligne (« Chapitre 10 ») et sa première case. */
    private static final float LABEL_GAP    = 24f;
    /** Hauteur gardée pour la fiche, sous l'Exploration (portrait, nom, lieu, quatre lignes de description). */
    private static final float FICHE_HEIGHT = 190f;
    private static final float BUTTON_GAP   = 24f;
    private static final float BLOCK_GAP    = 60f;
    private static final float ART          = 150f;
    private static final float FADE         = 0.2f;
    /** Couleur d'un portrait pas encore découvert : une silhouette sombre. */
    private static final Color UNKNOWN      = new Color(0.06f, 0.05f, 0.08f, 1f);
    private static final Color LOCKED       = new Color(0.55f, 0.52f, 0.5f, 1f);

    /** Une case : son ennemi et ses acteurs. */
    private record Cell(EnemyKind kind, Image frame, Image portrait, Image mark, Image golden) { }

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
    /** Les lignes de chaque colonne, dans un groupe qui défile si elles ne tiennent pas. */
    private final Group towerContent = new Group();
    private final Group placeContent = new Group();
    private final ScrollPane towerScroll;
    private final ScrollPane placeScroll;
    /** Pas des cases, case et portrait, calculés à chaque mise en page. */
    private float step = MAX_STEP, cellSize = MAX_STEP - CELL_GAP;
    /** Les morceaux du compte sous le titre, mis sur une ou deux lignes selon la largeur. */
    private final List<String> summaryParts = new ArrayList<>();
    private final List<Row> towerRows = new ArrayList<>();
    private final List<Row> placeRows = new ArrayList<>();
    private final List<Cell> cells    = new ArrayList<>();
    private final Image artFrame;
    private final Image art;
    /** Contour doré du portrait de la fiche, quand la version dorée du soldat est vaincue. */
    private final Image artGolden;
    private final Label name;
    private final Label where;
    private final Label status;
    private final Label description;
    private final CasinoButton cutscenes;
    /** « Carnet » : la fiche montre la page du carnet du Croupier au lieu de la description (et l'inverse). */
    private final CasinoButton notebook;
    /** {@code true} quand la fiche montre la page du carnet. */
    private boolean reading;
    private final CasinoButton back;
    private final CasinoButton fight;
    private final TextureRegionDrawable coin;
    private final BitmapFont titleFont = Fonts.jersey(64, Palette.GOLD, 4f, Palette.TEXT_SHADE);
    private final BitmapFont headFont  = Fonts.jersey(36, Palette.GOLD, 2f, Palette.TEXT_SHADE);
    private final BitmapFont nameFont  = Fonts.jersey(40, Color.WHITE, 3f, Palette.TEXT_SHADE);
    private final BitmapFont rowFont   = Fonts.jersey(28, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont textFont  = Fonts.jersey(24, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private PlayerProfile profile;
    private Cell          selected;
    private Runnable      onClose;
    /** Entraînement libre : le combat contre l'ennemi choisi ({@code null} dans le bestiaire des Options). */
    private Consumer<EnemyKind> onFight;

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
        summary = label(textFont, Palette.CREAM);
        towerTitle = label(headFont, Color.WHITE);
        explorationTitle = label(headFont, Color.WHITE);
        root.addActor(veil);
        root.addActor(panel);
        root.addActor(divider);
        root.addActor(title);
        root.addActor(summary);
        root.addActor(towerTitle);
        root.addActor(explorationTitle);
        towerScroll = scroll(hud, towerContent);
        placeScroll = scroll(hud, placeContent);
        root.addActor(towerScroll);
        root.addActor(placeScroll);

        towerRows.add(row(hud, List.of(EnemyKind.ENTRAINEMENT), towerContent));
        for (Chapter chapter : Chapter.values()) {
            List<EnemyKind> kinds = new ArrayList<>();
            kinds.add(chapter.getFirstEnemy());
            kinds.addAll(chapter.getChallengers());
            kinds.add(chapter.getBoss());
            towerRows.add(row(hud, kinds, towerContent));
        }
        for (Place place : Place.values()) {
            List<EnemyKind> kinds = new ArrayList<>();
            for (Dungeon dungeon : place.getDungeons()) {
                kinds.add(dungeon.getSoldier());
                kinds.add(dungeon.getKing());
            }
            placeRows.add(row(hud, kinds, placeContent));
        }

        artFrame = new Image(hud.insetDrawable());
        art = new Image();
        artGolden = new Image();
        artGolden.setColor(GOLDEN);
        artGolden.setTouchable(Touchable.disabled);
        name = label(nameFont, Palette.GOLD);
        where = label(textFont, Palette.CREAM);
        status = label(textFont, Color.WHITE);
        description = label(textFont, Color.WHITE);
        description.setWrap(true);
        description.setAlignment(Align.topLeft);
        root.addActor(artFrame);
        root.addActor(artGolden);
        root.addActor(art);
        root.addActor(name);
        root.addActor(where);
        root.addActor(status);
        root.addActor(description);

        cutscenes = buttons.create(Lang.t("Revoir les cinématiques"), clickSound, onCutscenes);
        back = buttons.create(Lang.t("Retour"), clickSound, this::hide);
        notebook = buttons.create(Lang.t("Carnet"), clickSound, this::toggleNotebook);
        root.addActor(notebook);
        fight = buttons.create(Lang.t("Combattre"), clickSound, this::fightSelected);
        root.addActor(cutscenes);
        root.addActor(back);
        root.addActor(fight);
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

    /** Une colonne qui défile à la molette (verticalement), sa barre fine à droite. */
    private static ScrollPane scroll(HudTextures hud, Group content) {
        ScrollPane pane = new ScrollPane(content, SidePanel.scrollStyle(hud));
        pane.setScrollingDisabled(true, false);
        pane.setFlickScroll(false); // un clic choisit un ennemi, il ne fait pas glisser la liste
        pane.setFadeScrollBars(false);
        pane.setOverscroll(false, false);
        SidePanel.addWheelFocus(pane);
        return pane;
    }

    private Row row(HudTextures hud, List<EnemyKind> kinds, Group parent) {
        Label label = label(rowFont, Palette.CREAM);
        parent.addActor(label);
        List<Cell> rowCells = new ArrayList<>();
        for (EnemyKind kind : kinds) {
            Image frame = new Image(hud.insetDrawable());
            Image portrait = new Image();
            portrait.setTouchable(Touchable.disabled);
            Image mark = new Image(coin);
            mark.setTouchable(Touchable.disabled);
            Image golden = goldenImage(kind);
            Cell cell = new Cell(kind, frame, portrait, mark, golden);
            frame.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    super.enter(event, x, y, pointer, fromActor);
                    if (pointer == -1) select(cell);
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    select(cell);
                    if (getTapCount() >= 2 && onFight != null) fightSelected(); // double-clic : on y va
                }
            });
            parent.addActor(frame);
            parent.addActor(golden);
            parent.addActor(portrait);
            parent.addActor(mark);
            rowCells.add(cell);
            cells.add(cell);
        }
        return new Row(label, rowCells);
    }

    /** @return le contour doré du portrait de {@code kind} (caché tant que sa version dorée n'est pas vaincue). */
    private Image goldenImage(EnemyKind kind) {
        Texture outline = textures.goldenOutline(kind);
        Image image = outline != null ? new Image(new TextureRegionDrawable(new TextureRegion(outline))) : new Image();
        image.setColor(GOLDEN);
        image.setTouchable(Touchable.disabled);
        image.setVisible(false);
        return image;
    }

    /** Couleur du contour d'une version dorée vaincue. */
    private static final Color GOLDEN = new Color(1f, 0.82f, 0.25f, 1f);
    /** Teinte de la case d'un soldat dont la version dorée est vaincue. */
    private static final Color GOLDEN_FRAME = new Color(1f, 0.85f, 0.3f, 1f);

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
        open(profile, onClose, null);
    }

    /**
     * Ouvre la fenêtre pour l'Entraînement libre : choisir le croupier d'entraînement
     * ou un ennemi déjà vaincu de {@code profile}, puis {@code onFight} avec lui.
     */
    public void showTraining(PlayerProfile profile, Consumer<EnemyKind> onFight, Runnable onClose) {
        open(profile, onClose, onFight);
    }

    /** @return {@code true} si l'ennemi choisi peut être combattu à l'Entraînement libre. */
    public static boolean canTrainAgainst(PlayerProfile profile, EnemyKind kind) {
        return kind == EnemyKind.ENTRAINEMENT || profile.hasDefeated(kind);
    }

    /** Entraînement libre : lance le combat contre l'ennemi choisi, s'il a déjà été vaincu. */
    public void fightSelected() {
        if (onFight == null || selected == null || !canTrainAgainst(profile, selected.kind())) return;
        Consumer<EnemyKind> action = onFight;
        EnemyKind kind = selected.kind();
        hide();
        action.accept(kind);
    }

    /** @return {@code true} si la fiche de {@code kind} est connue (à l'Entraînement libre, le croupier l'est toujours). */
    private boolean known(EnemyKind kind) {
        return profile.hasMet(kind) || (onFight != null && kind == EnemyKind.ENTRAINEMENT);
    }

    /** @return {@code true} si la fenêtre est ouverte pour l'Entraînement libre. */
    public boolean isTraining() { return onFight != null; }

    private void open(PlayerProfile profile, Runnable onClose, Consumer<EnemyKind> onFight) {
        this.profile = profile;
        this.onClose = onClose;
        this.onFight = onFight;
        boolean training = onFight != null;
        cutscenes.setVisible(!training);
        notebook.setVisible(!training);
        reading = false;
        fight.setVisible(training);
        title.setText(training ? Lang.t("ENTRAÎNEMENT LIBRE") : Lang.t("BESTIAIRE ET COLLECTION"));
        towerTitle.setText(Lang.t("Histoire"));
        explorationTitle.setText(Lang.t("Exploration"));
        towerRows.get(0).label().setText(Lang.t("Entraînement"));
        for (Chapter chapter : Chapter.values()) towerRows.get(chapter.ordinal() + 1).label().setText(chapter.getLabel());
        for (Place place : Place.values()) placeRows.get(place.ordinal()).label().setText(place.getShortName());

        int met = 0, defeated = 0;
        for (Cell cell : cells) {
            boolean known = known(cell.kind());
            boolean beaten = profile.hasDefeated(cell.kind());
            if (known) met++;
            if (beaten) defeated++;
            Texture texture = textures.portrait(cell.kind());
            cell.portrait().setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
            cell.portrait().setColor(known ? Color.WHITE : UNKNOWN);
            cell.mark().setVisible(beaten);
            boolean golden = Bestiary.hasGoldenVersion(cell.kind()) && profile.hasDefeatedGolden(cell.kind());
            cell.golden().setVisible(golden);
            cell.frame().setColor(golden ? GOLDEN_FRAME : Color.WHITE); // sa case dorée
        }
        int[] seenCutscenes = Bestiary.cutscenes(profile);
        summaryParts.clear();
        summaryParts.add(Lang.f("Ennemis rencontrés : {0} / {1}", met, cells.size()));
        summaryParts.add(Lang.f("Vaincus : {0} / {1}", defeated, cells.size()));
        summaryParts.add(Lang.f("Cartes : {0} / {1}", profile.getCollectedCardCount(), profile.getCatalogSize()));
        summaryParts.add(Lang.f("Rouleaux : {0} / {1}", profile.getOwnedReels().size(), PlayerProfile.getReelCount()));
        summaryParts.add(Lang.f("Cinématiques : {0} / {1}", seenCutscenes[0], seenCutscenes[1]));
        summaryParts.add(Lang.f("Carnet : {0} / {1}", Notebook.found(profile), Notebook.size()));
        summaryParts.add(Lang.f("Dorés : {0} / {1}", golden(profile), Bestiary.goldenEnemies().size()));
        cutscenes.setDisabled(seenCutscenes[0] == 0);
        if (training) {
            summaryParts.clear();
            summaryParts.add(Lang.t("Choisis un ennemi déjà vaincu pour tester ton deck. Ce combat ne rapporte rien."));
        }

        Cell first = training ? cells.get(0)
            : cells.stream().filter(cell -> profile.hasMet(cell.kind())).findFirst().orElse(cells.get(0));
        selected = null;
        select(first);
        layout();
        root.setVisible(true);
        root.toFront();
        root.getColor().a = 0f;
        root.clearActions();
        root.addAction(Actions.fadeIn(FADE));
    }

    /** @return les versions dorées vaincues (toutes en mode ADMIN). */
    private static int golden(PlayerProfile profile) {
        return profile.isAdmin() ? Bestiary.goldenEnemies().size() : profile.getGoldenDefeatedCount();
    }

    /** Montre la fiche de l'ennemi de {@code cell}. */
    private void select(Cell cell) {
        if (cell == selected || profile == null) return;
        selected = cell;
        EnemyKind kind = cell.kind();
        boolean known = known(kind);
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
        String text = known ? kind.getDescription() : Lang.t("Affronte-le pour découvrir sa fiche.");
        if (known && Place.of(kind) != null && Place.of(kind).isKing(kind)) { // carte de roi : ses victoires
            text += "\n" + Lang.f("Victoires : {0} / {1} pour sa carte à la boutique.",
                Math.min(profile.getKingWins(kind), PlayerProfile.KING_CARD_WINS), PlayerProfile.KING_CARD_WINS);
        }
        description.setText(text);
        description.setColor(known ? Color.WHITE : LOCKED);
        boolean goldenBeaten = Bestiary.hasGoldenVersion(kind) && profile.hasDefeatedGolden(kind);
        Texture outline = textures.goldenOutline(kind);
        artGolden.setDrawable(goldenBeaten && outline != null ? new TextureRegionDrawable(new TextureRegion(outline)) : null);
        artGolden.setVisible(goldenBeaten && outline != null);
        artFrame.setColor(goldenBeaten ? GOLDEN_FRAME : Color.WHITE);
        if (known && Bestiary.hasGoldenVersion(kind) && onFight == null) {
            description.setText(description.getText() + "\n\n" + (goldenBeaten
                ? Lang.t("Version dorée : vaincue !")
                : Lang.f("Version dorée : pas encore vaincue. Un soldat sur {0} arrive doré : PV x{1} et un rouleau de plus.",
                    DungeonRun.GOLDEN_CHANCE, Enemy.GOLDEN_HP_FACTOR)));
        }
        notebook.setDisabled(!Notebook.hasPage(kind));
        notebook.setText(reading && Notebook.hasPage(kind) ? Lang.t("Fiche") : Lang.t("Carnet"));
        if (reading && Notebook.hasPage(kind) && onFight == null) { // la page du carnet, à la place de la description
            boolean found = profile.hasDefeated(kind);
            description.setText(found
                ? Lang.f("Carnet du Croupier, page {0} / {1}\n« {2} »", Notebook.number(kind), Notebook.size(), Notebook.text(kind))
                : Lang.f("Carnet du Croupier, page {0} / {1}\nBats-le pour trouver sa page.", Notebook.number(kind), Notebook.size()));
            description.setColor(found ? Palette.CREAM : LOCKED);
        }
        if (onFight != null) {
            boolean open = canTrainAgainst(profile, kind);
            fight.setDisabled(!open);
            if (kind == EnemyKind.ENTRAINEMENT && !profile.hasDefeated(kind)) {
                status.setText(Lang.t("Toujours disponible"));
                status.setColor(Palette.GOLD);
            }
            if (!open) {
                description.setText(Lang.t("Bats-le d'abord dans son mode pour t'entraîner contre lui."));
                description.setColor(LOCKED);
            }
        }
        layout();
    }

    /** « Carnet » / « Fiche » : la fiche bascule entre la description et la page du carnet du Croupier. */
    private void toggleNotebook() {
        if (selected == null || !Notebook.hasPage(selected.kind())) return;
        reading = !reading;
        Cell cell = selected;
        selected = null;
        select(cell);
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
        float panelWidth  = Math.min(PANEL_WIDTH, width - 2f * MARGIN);
        float panelHeight = Math.min(PANEL_HEIGHT, height - 2f * MARGIN);
        float px = (width - panelWidth) / 2f;
        float py = (height - panelHeight) / 2f;
        float inner = panelWidth - 2f * PAD;
        panel.setBounds(px, py, panelWidth, panelHeight);

        title.pack();
        float top = py + panelHeight - PAD;
        title.setPosition(px + (panelWidth - title.getWidth()) / 2f, top - title.getHeight());
        summary.setText(joinLines(summaryParts, inner));
        summary.setAlignment(Align.center);
        summary.pack();
        float summaryY = top - title.getHeight() - 8f - summary.getHeight();
        summary.setPosition(px + (panelWidth - summary.getWidth()) / 2f, summaryY);
        float headY = summaryY - 24f;
        towerTitle.pack();
        explorationTitle.pack();
        float rowsTop = headY - towerTitle.getHeight() - 14f;

        // Le pas des cases : ce qui tient en largeur (la Tour au moins aussi large que ses
        // boutons), et en hauteur avec les boutons sous la Tour et la fiche sous l'Exploration.
        int towerColumns = columns(towerRows), placeColumns = columns(placeRows);
        float towerLabel = labelWidth(towerRows), placeLabel = labelWidth(placeRows);
        float buttonsWidth = buttonsWidth();
        float buttonsTop = py + PAD + back.getHeight() + 16f;
        float ficheBottom = py + PAD + FICHE_HEIGHT;
        float grid = inner - BLOCK_GAP - 2f * SCROLL_ROOM;
        float byWidth = (grid - towerLabel - placeLabel) / (towerColumns + placeColumns);
        if (towerLabel + towerColumns * byWidth < buttonsWidth) byWidth = (grid - buttonsWidth - placeLabel) / placeColumns;
        float byHeight = Math.min((rowsTop - buttonsTop - 2f * EDGE) / towerRows.size(),
            (rowsTop - ficheBottom - 2f * EDGE) / placeRows.size());
        step = Math.max(MIN_STEP, Math.min(MAX_STEP, Math.min(byWidth, byHeight)));
        cellSize = step - CELL_GAP;

        // La Tour, à gauche ; l'Exploration, à droite.
        float towerWidth = Math.max(towerLabel + towerColumns * step, buttonsWidth);
        float placeWidth = placeLabel + placeColumns * step;
        float tx = px + (panelWidth - towerWidth - BLOCK_GAP - placeWidth) / 2f;
        float ex = tx + towerWidth + BLOCK_GAP;
        towerTitle.setPosition(tx, headY - towerTitle.getHeight());
        explorationTitle.setPosition(ex, headY - explorationTitle.getHeight());
        placeColumn(towerScroll, towerContent, towerRows, tx, towerLabel, towerWidth, rowsTop, buttonsTop);
        placeColumn(placeScroll, placeContent, placeRows, ex, placeLabel, placeWidth, rowsTop, ficheBottom);
        divider.setBounds(tx + towerWidth + BLOCK_GAP / 2f - 1.5f, py + PAD + 90f, 3f, headY - (py + PAD + 90f));

        if (selected != null) {
            selection.setVisible(true);
            // Le contour suit la case dans sa colonne (et défile avec elle), juste sous la case.
            selected.frame().getParent().addActorBefore(selected.frame(), selection);
            selection.setBounds(selected.frame().getX() - 4f, selected.frame().getY() - 4f, cellSize + 8f, cellSize + 8f);
        } else {
            selection.setVisible(false);
        }

        layoutFiche(ex, placeScroll.getY() - 14f, px + panelWidth - PAD);

        // Les boutons, sous la Tour : ils ne cachent plus la fiche.
        back.setPosition(tx + towerWidth - back.getWidth(), py + PAD);
        cutscenes.setPosition(back.getX() - BUTTON_GAP - cutscenes.getWidth(), py + PAD);
        notebook.setPosition(cutscenes.getX() - BUTTON_GAP - notebook.getWidth(), py + PAD);
        fight.setPosition(back.getX() - BUTTON_GAP - fight.getWidth(), py + PAD);
    }

    /** La fiche sous l'Exploration : le portrait à gauche ; le nom, le lieu et l'état sur une ligne, la description. */
    private void layoutFiche(float x, float top, float right) {
        artFrame.setBounds(x, top - ART, ART, ART);
        fit(art, x + 10f, top - ART + 10f, ART - 20f);
        around(artGolden, art);
        float textX = x + ART + 30f;
        float textWidth = right - textX;
        name.pack();
        name.setPosition(textX, top - name.getHeight());
        where.pack();
        where.setPosition(textX, name.getY() - 4f - where.getHeight());
        status.pack();
        status.setPosition(where.getX() + where.getWidth() + 24f, where.getY());
        description.setWidth(textWidth);
        description.setHeight(description.getPrefHeight());
        description.setPosition(textX, where.getY() - 10f - description.getHeight());
    }

    /** @return les morceaux du compte, séparés par des espaces, avec un retour à la ligne là où ils dépassent {@code width}. */
    private String joinLines(List<String> parts, float width) {
        GlyphLayout measure = new GlyphLayout();
        StringBuilder text = new StringBuilder();
        StringBuilder line = new StringBuilder();
        for (String part : parts) {
            String candidate = line.length() == 0 ? part : line + SUMMARY_SEPARATOR + part;
            measure.setText(textFont, candidate);
            if (line.length() > 0 && measure.width > width) {
                text.append(line).append('\n');
                line.setLength(0);
                line.append(part);
            } else {
                line.setLength(0);
                line.append(candidate);
            }
        }
        return text.append(line).toString();
    }

    private static final String SUMMARY_SEPARATOR = "     ";

    /** @return la largeur de la rangée de boutons sous la Tour (ceux de ce mode). */
    private float buttonsWidth() {
        float width = back.getWidth();
        for (CasinoButton button : isTraining() ? List.of(fight) : List.of(cutscenes, notebook)) {
            width += BUTTON_GAP + button.getWidth();
        }
        return width;
    }

    /** @return la largeur des noms de lignes de {@code rows} (le plus long), plus l'espace avant les cases. */
    private static float labelWidth(List<Row> rows) {
        float max = 0f;
        for (Row row : rows) max = Math.max(max, row.label().getPrefWidth());
        return max + LABEL_GAP;
    }

    /** @return le plus grand nombre de cases d'une ligne de {@code rows}. */
    private static int columns(List<Row> rows) {
        int max = 1;
        for (Row row : rows) max = Math.max(max, row.cells().size());
        return max;
    }

    /**
     * Place une colonne : ses lignes dans {@code content}, et la zone qui défile
     * de {@code top} jusqu'à {@code bottom} au plus (moins haute si tout tient).
     */
    private void placeColumn(ScrollPane pane, Group content, List<Row> rows, float x, float labelWidth,
                             float columnWidth, float top, float bottom) {
        float contentWidth  = columnWidth + 2f * EDGE;
        float contentHeight = rows.size() * step + 2f * EDGE;
        content.setSize(contentWidth, contentHeight);
        placeRows(rows, EDGE, labelWidth, contentHeight - EDGE);
        float paneHeight = Math.min(contentHeight, Math.max(step, top + EDGE - bottom));
        float paneWidth  = contentWidth + (contentHeight > paneHeight ? SCROLL_ROOM : 0f);
        pane.setBounds(x - EDGE, top + EDGE - paneHeight, paneWidth, paneHeight);
        pane.invalidate();
        pane.validate();
    }

    /** Place les lignes {@code rows} à partir de ({@code x}, {@code top}) ; @return le bas de la dernière. */
    private float placeRows(List<Row> rows, float x, float labelWidth, float top) {
        float portraitSize = cellSize * 0.82f;
        float markSize = Math.max(18f, cellSize * 0.32f);
        float y = top;
        for (Row row : rows) {
            y -= step;
            row.label().pack();
            row.label().setPosition(x, y + (cellSize - row.label().getHeight()) / 2f);
            float cx = x + labelWidth;
            for (Cell cell : row.cells()) {
                cell.frame().setBounds(cx, y, cellSize, cellSize);
                fit(cell.portrait(), cx + (cellSize - portraitSize) / 2f, y + (cellSize - portraitSize) / 2f, portraitSize);
                around(cell.golden(), cell.portrait());
                cell.mark().setBounds(cx + cellSize - markSize + 4f, y - 4f, markSize, markSize);
                cx += step;
            }
        }
        return y;
    }

    /** Place le contour doré {@code outline} autour du portrait {@code portrait}, à la même échelle. */
    private static void around(Image outline, Image portrait) {
        if (outline.getDrawable() == null || portrait.getDrawable() == null) return;
        float scale  = portrait.getWidth() / portrait.getDrawable().getMinWidth();
        float border = EnemyTextures.GOLD_OUTLINE * scale;
        outline.setBounds(portrait.getX() - border, portrait.getY() - border,
            portrait.getWidth() + 2f * border, portrait.getHeight() + 2f * border);
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
