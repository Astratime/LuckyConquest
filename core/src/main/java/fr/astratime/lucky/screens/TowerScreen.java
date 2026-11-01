package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.progress.PlayerProfile;
import fr.astratime.lucky.assets.EnemyTextures;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.tower.Chapter;
import fr.astratime.lucky.entities.tower.TowerRun;
import fr.astratime.lucky.views.MenuOption;

import java.util.ArrayList;
import java.util.List;

/**
 * Tour des épreuves : choix du chapitre, devant le décor du casino assombri.
 *
 * À gauche, un panneau liste les chapitres ; à droite, le chapitre sélectionné
 * (survol de la souris ou flèches) montre son titre, son illustration et son
 * récit. Un clic sur un chapitre ouvert (ou Entrée, ou « Commencer ») le lance :
 * trois combats à la suite (voir {@link TowerRun}, {@link GameScreen}). Un
 * chapitre s'ouvre quand le précédent est terminé, dans le même mode ; les
 * autres sont grisés. Échap ou « Retour » ramène au menu
 * principal.
 */
public class TowerScreen extends CasinoMenuScreen {

    private static final String CHIP_PATH   = "menu/chip_spin.png";

    private static final float TITLE_TOP    = 110f;    // du haut de l'écran au centre du titre
    private static final float LIST_WIDTH   = 420f;
    private static final float ROW_WIDTH    = 340f;
    private static final float ROW_HEIGHT   = 76f;
    private static final float ROW_GAP      = 18f;
    private static final float PANEL_PAD    = 40f;
    private static final float PANEL_TOP    = 210f;    // du haut de l'écran au haut des panneaux
    private static final float BOTTOM_SPACE = 130f;    // sous les panneaux : boutons
    private static final float ART_SCALE    = 4f;      // pixels de l'illustration
    private static final Color LOCKED_TEXT  = new Color(0.55f, 0.52f, 0.48f, 1f);

    /** Première visite : le Croupier présente l'écran. */
    private final FirstVisitGuide firstVisit;
    private final EnemyTextures   enemyTextures;
    private final Texture         chipTexture = new Texture(Gdx.files.internal(CHIP_PATH));
    private final BitmapFont      rowFont     = Fonts.jersey(50, Color.WHITE, 4f, Palette.TEXT_SHADE);
    private final BitmapFont      labelFont   = Fonts.jersey(36, Palette.CREAM, 3f, Palette.TEXT_SHADE);
    private final BitmapFont      chapterFont = Fonts.jersey(72, Palette.GOLD, 5f, Palette.TEXT_SHADE);
    private final BitmapFont      bodyFont    = Fonts.jersey(36, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont      lockFont    = Fonts.jersey(160, LOCKED_TEXT, 6f, Palette.TEXT_SHADE);

    private final Image             listPanel;
    private final Image             detailPanel;
    private final List<MenuOption>  rows = new ArrayList<>();
    private final Label             chapterLabel;
    private final Label             chapterTitle;
    private final Image             art;
    private final Image             artFrame;
    private final Label             lockMark;
    private final Label             description;
    private final TextButton        startButton;
    private final TextButton        modeButton;
    /** Mode ADMIN : choix du combat où l'ascension commence (premier ennemi, un challenger ou le boss). */
    private final TextButton        fightButton;
    private final TextButton        backButton;
    /** Rejoue la présentation du Croupier. */
    private final TextButton        tutorialButton;

    private int     selected = -1;
    /** Mode difficile choisi (ouvert une fois la Machine Originelle battue). */
    private boolean hard;
    /** Mode ADMIN : combat choisi, index dans {@link TowerRun#fights(Chapter)} (0 : le premier ennemi). */
    private int     fight;

    public TowerScreen(LuckyGame luckyGame) {
        this(luckyGame, Chapter.values()[0], false);
    }

    /**
     * Écran des chapitres ouvert sur {@code chapter} dans le mode {@code hardMode} :
     * au retour d'un combat, le chapitre qui vient d'être joué.
     */
    public TowerScreen(LuckyGame luckyGame, Chapter chapter, boolean hardMode) {
        super(luckyGame, Lang.t("TOUR DES ÉPREUVES"), 96, VEIL_ALPHA);
        this.hard      = hardMode && luckyGame.getProfile().isTowerHardOpen();
        this.enemyTextures = assets.enemyTextures();

        listPanel   = new Image(hud.panelDrawable());
        detailPanel = new Image(hud.panelDrawable());
        listPanel.setTouchable(Touchable.disabled);
        detailPanel.setTouchable(Touchable.disabled);

        Label.LabelStyle rowStyle = new Label.LabelStyle(rowFont, Color.WHITE);
        Chapter[] chapters = Chapter.values();
        for (int i = 0; i < chapters.length; i++) {
            int index = i;
            MenuOption row = new MenuOption(chapters[i].getLabel(), rowStyle, hud.insetDrawable(),
                new TextureRegion(chipTexture), ROW_WIDTH, ROW_HEIGHT);
            row.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    super.enter(event, x, y, pointer, fromActor);
                    if (pointer == -1) select(index, true);
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    select(index, false);
                    launch();
                }
            });
            rows.add(row);
        }

        chapterLabel = new Label("", new Label.LabelStyle(labelFont, Color.WHITE));
        chapterTitle = new Label("", new Label.LabelStyle(chapterFont, Color.WHITE));
        artFrame     = new Image(hud.insetDrawable());
        art          = new Image(new TextureRegionDrawable(new TextureRegion(enemyTextures.chapterArt(Chapter.GENESE))));
        lockMark     = new Label("?", new Label.LabelStyle(lockFont, Color.WHITE));
        lockMark.setAlignment(Align.center);
        description  = new Label("", new Label.LabelStyle(bodyFont, Color.WHITE));
        description.setWrap(true);
        description.setAlignment(Align.topLeft);
        startButton  = buttons.createAction(Lang.t("Commencer"), clickSound, this::launch);
        backButton   = buttons.create(Lang.t("Retour"), clickSound, this::onBack);
        tutorialButton = buttons.create(Lang.t("Tutoriel"), clickSound, this::replayGuide);
        // Mode difficile : ouvert après la Machine Originelle ; le bouton bascule entre les deux modes.
        modeButton   = buttons.create(Lang.t("Mode difficile"), clickSound, this::toggleMode);
        modeButton.setText(hard ? Lang.t("Mode difficile") : Lang.t("Mode normal"));
        modeButton.setVisible(luckyGame.getProfile().isTowerHardOpen());
        fightButton  = buttons.create("", clickSound, this::nextFight);
        fightButton.setVisible(luckyGame.getProfile().isAdmin());

        stage.addActor(listPanel);
        stage.addActor(detailPanel);
        rows.forEach(stage::addActor);
        stage.addActor(chapterLabel);
        stage.addActor(chapterTitle);
        stage.addActor(artFrame);
        stage.addActor(art);
        stage.addActor(lockMark);
        stage.addActor(description);
        stage.addActor(startButton);
        stage.addActor(modeButton);
        stage.addActor(fightButton);
        stage.addActor(backButton);
        stage.addActor(tutorialButton);
        reveal();

        select(chapter.ordinal(), false);
        layout();
        firstVisit = new FirstVisitGuide(stage, hud, clickSound, luckyGame.getProfile(), PlayerProfile.GUIDE_TOWER,
            Lang.t("La Tour des épreuves. Six chapitres, de plus en plus durs."),
            Lang.t("Un chapitre : un premier combat, puis tu choisis ton adversaire parmi trois cartes, puis le "
                + "boss. Tes PV et tes gains te suivent."),
            Lang.t("Bats le boss pour ouvrir le chapitre suivant. La Tour ne rapporte pas de pièces."));
        fade.toFront();
    }

    // -------------------------------------------------------------------------
    // Sélection et lancement
    // -------------------------------------------------------------------------

    /** Sélectionne le chapitre {@code index} et montre son titre, son illustration et son récit. */
    private void select(int index, boolean sound) {
        if (index == selected) return;
        if (sound) hoverSound.play();
        selected = index;
        showChapter();
    }

    /** Montre le chapitre sélectionné : titre, illustration et récit (avec la règle du mode difficile). */
    private void showChapter() {
        int index = selected;
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setSelected(i == index);
            rows.get(i).setColor(isOpen(Chapter.values()[i]) ? Color.WHITE : LOCKED_TEXT);
        }
        Chapter chapter = Chapter.values()[index];
        boolean open = isOpen(chapter);
        chapterLabel.setText(hard ? Lang.f("{0} · DIFFICILE", chapter.getLabel().toUpperCase())
            : chapter.getLabel().toUpperCase());
        chapterTitle.setText(chapter.getTitle());
        description.setText(!open
            ? Lang.f(hard ? "Termine le {0} en mode difficile pour ouvrir ce chapitre."
                    : "Termine le {0} pour ouvrir ce chapitre.",
                chapter.getPrevious().getLabel().toLowerCase())
            : hard
            ? Lang.f("{0}\n\nMode difficile : ennemis PV x{1}, force x{2}.",
                chapter.getDescription(), EnemyKind.HARD_HP_FACTOR, EnemyKind.HARD_POWER_FACTOR)
            : chapter.getDescription());
        ((TextureRegionDrawable) art.getDrawable()).setRegion(new TextureRegion(enemyTextures.chapterArt(chapter)));
        art.setVisible(open);
        lockMark.setVisible(!open);
        buttons.setText(fightButton, fightText(chapter));
        startButton.setDisabled(!open);
        startButton.setText(open ? Lang.t("Commencer") : Lang.t("Verrouillé"));
        // Le panneau de droite apparaît en fondu à chaque changement.
        for (Actor actor : List.of(chapterLabel, chapterTitle, art, lockMark, description)) {
            actor.clearActions();
            actor.getColor().a = 0f;
            actor.addAction(Actions.fadeIn(0.25f, Interpolation.pow2Out));
        }
        layout();
    }

    /** Lance le chapitre sélectionné, s'il est ouvert : fondu au noir puis premier combat. */
    private void launch() {
        if (leaving) return;
        Chapter chapter = Chapter.values()[selected];
        if (!isOpen(chapter)) return;
        boolean admin = luckyGame.getProfile().isAdmin() && fight > 0;
        int chosen = fight;
        goTo(() -> new GameScreen(luckyGame,
            admin ? TowerRun.startingAt(chapter, hard, chosen) : new TowerRun(chapter, hard)));
    }

    /** Mode ADMIN : passe au combat suivant du chapitre (après le boss, revient au premier). */
    private void nextFight() {
        fight = (fight + 1) % TowerRun.fights(Chapter.values()[selected]).size();
        buttons.setText(fightButton, fightText(Chapter.values()[selected]));
        layout();
    }

    /** @return le texte du bouton du combat choisi en mode ADMIN (ex : « Combat 2 : Tricheur »). */
    private String fightText(Chapter chapter) {
        EnemyKind kind = TowerRun.fights(chapter).get(fight);
        return Lang.f("Combat {0} : {1}", TowerRun.stageNumber(chapter, fight), kind.getDisplayName());
    }

    /** @return {@code true} si {@code chapter} peut être joué dans le mode choisi (le précédent est terminé). */
    private boolean isOpen(Chapter chapter) {
        return luckyGame.getProfile().isOpen(chapter, hard);
    }

    /** Bascule entre le mode normal et le mode difficile. */
    private void toggleMode() {
        hard = !hard;
        modeButton.setText(hard ? Lang.t("Mode difficile") : Lang.t("Mode normal"));
        showChapter();
    }

    // -------------------------------------------------------------------------
    // Mise en page et cycle de vie
    // -------------------------------------------------------------------------

    /** Place le titre, les deux panneaux, leur contenu et les boutons selon la taille de l'écran. */
    @Override
    protected void layoutContent(float width, float height) {
        title.setPosition((width - title.getWidth()) / 2f, height - TITLE_TOP - title.getHeight() / 2f);

        float panelTop    = height - PANEL_TOP;
        float panelBottom = BOTTOM_SPACE;
        float panelHeight = panelTop - panelBottom;
        listPanel.setBounds(MARGIN, panelBottom, LIST_WIDTH, panelHeight);
        float rowX = MARGIN + (LIST_WIDTH - ROW_WIDTH) / 2f;
        for (int i = 0; i < rows.size(); i++) {
            MenuOption row = rows.get(i);
            row.setPosition(rowX, panelTop - PANEL_PAD - ROW_HEIGHT - i * (ROW_HEIGHT + ROW_GAP));
        }

        float detailX     = MARGIN + LIST_WIDTH + MARGIN / 2f;
        float detailWidth = width - detailX - MARGIN;
        detailPanel.setBounds(detailX, panelBottom, detailWidth, panelHeight);
        float innerX     = detailX + PANEL_PAD;
        float innerWidth = detailWidth - PANEL_PAD * 2;

        chapterLabel.pack();
        chapterLabel.setPosition(innerX, panelTop - PANEL_PAD - chapterLabel.getHeight());
        chapterTitle.pack();
        chapterTitle.setPosition(innerX, chapterLabel.getY() - chapterTitle.getHeight());

        // Illustration centrée, à la plus grande taille qui laisse la place au récit.
        float artScale = ART_SCALE;
        float artTop   = chapterTitle.getY() - 16f;
        float maxArtHeight = artTop - panelBottom - PANEL_PAD - 200f;
        Texture texture = enemyTextures.chapterArt(Chapter.values()[Math.max(0, selected)]);
        while (artScale > 1f && (texture.getHeight() * artScale > maxArtHeight || texture.getWidth() * artScale > innerWidth)) {
            artScale -= 1f;
        }
        float artWidth = texture.getWidth() * artScale, artHeight = texture.getHeight() * artScale;
        float artX = innerX + (innerWidth - artWidth) / 2f, artY = artTop - artHeight;
        art.setBounds(artX, artY, artWidth, artHeight);
        float frame = 10f;
        artFrame.setBounds(artX - frame, artY - frame, artWidth + frame * 2, artHeight + frame * 2);
        lockMark.setBounds(artX, artY, artWidth, artHeight);

        float descriptionTop = artY - frame - 24f;
        description.setWidth(innerWidth);
        description.setHeight(Math.max(60f, descriptionTop - panelBottom - PANEL_PAD));
        description.setPosition(innerX, panelBottom + PANEL_PAD);

        startButton.setPosition(detailX + detailWidth - startButton.getWidth(), (BOTTOM_SPACE - startButton.getHeight()) / 2f);
        modeButton.setPosition(startButton.getX() - modeButton.getWidth() - 20f, startButton.getY());
        float fightRight = modeButton.isVisible() ? modeButton.getX() : startButton.getX();
        fightButton.setPosition(fightRight - fightButton.getWidth() - 20f, startButton.getY());
        backButton.setPosition(MARGIN, (BOTTOM_SPACE - backButton.getHeight()) / 2f);
        tutorialButton.setPosition(backButton.getX() + backButton.getWidth() + 20f, backButton.getY());
    }

    /**
     * Haut/bas pour choisir un chapitre, Entrée ou Espace pour le lancer, F (mode
     * ADMIN) pour choisir le combat, Échap pour revenir au menu principal.
     */
    @Override
    protected boolean onKey(int keycode) {
        switch (keycode) {
            case Input.Keys.UP, Input.Keys.W -> select((selected + rows.size() - 1) % rows.size(), true);
            case Input.Keys.DOWN, Input.Keys.S -> select((selected + 1) % rows.size(), true);
            case Input.Keys.ENTER, Input.Keys.SPACE -> {
                if (isOpen(Chapter.values()[selected])) {
                    clickSound.play();
                    launch();
                }
            }
            case Input.Keys.F -> {
                if (!fightButton.isVisible()) return false;
                clickSound.play();
                nextFight();
            }
            case Input.Keys.ESCAPE -> {
                clickSound.play();
                onBack();
            }
            default -> { return false; }
        }
        return true;
    }

    @Override
    public void dispose() {
        super.dispose();
        firstVisit.dispose();
        chipTexture.dispose();
        Fonts.release(rowFont);
        Fonts.release(labelFont);
        Fonts.release(chapterFont);
        Fonts.release(bodyFont);
        Fonts.release(lockFont);
    }

    /** Bouton « Tutoriel » : le Croupier présente l'écran de nouveau. */
    private void replayGuide() {
        firstVisit.replay();
        fade.toFront();
    }
}
