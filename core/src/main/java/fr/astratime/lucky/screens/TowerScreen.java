package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.assets.BackgroundMusic;
import fr.astratime.lucky.assets.CardTextures;
import fr.astratime.lucky.assets.EnemyTextures;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.assets.VolumeSound;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.tower.Chapter;
import fr.astratime.lucky.entities.tower.TowerRun;
import fr.astratime.lucky.settings.AudioSettings;
import fr.astratime.lucky.views.CasinoButtons;
import fr.astratime.lucky.views.MenuDecor;
import fr.astratime.lucky.views.MenuOption;
import fr.astratime.lucky.views.MinimumScreenViewport;

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
public class TowerScreen extends ScreenAdapter {

    private static final String CLICK_SOUND = "sounds/button-click.ogg";
    private static final String HOVER_SOUND = "sounds/ui/menu_hover.ogg";
    private static final String CHIP_PATH   = "menu/chip_spin.png";
    private static final String MUSIC       = "music/main_menu.ogg";
    private static final float  MUSIC_LEVEL = 0.3f;

    private static final float MIN_WIDTH    = 1600f;
    private static final float MIN_HEIGHT   = 1080f;
    private static final float MARGIN       = 60f;
    private static final float TITLE_TOP    = 110f;    // du haut de l'écran au centre du titre
    private static final float LIST_WIDTH   = 420f;
    private static final float ROW_WIDTH    = 340f;
    private static final float ROW_HEIGHT   = 76f;
    private static final float ROW_GAP      = 18f;
    private static final float PANEL_PAD    = 40f;
    private static final float PANEL_TOP    = 210f;    // du haut de l'écran au haut des panneaux
    private static final float BOTTOM_SPACE = 130f;    // sous les panneaux : boutons
    private static final float ART_SCALE    = 4f;      // pixels de l'illustration
    private static final float FADE_TIME    = 0.4f;
    private static final Color LOCKED_TEXT  = new Color(0.55f, 0.52f, 0.48f, 1f);

    private final LuckyGame       luckyGame;
    private final Stage           stage;
    private final AudioSettings   audio  = new AudioSettings();
    private final HudTextures     hud    = new HudTextures();
    private final CardTextures    cardTextures = new CardTextures();
    private final EnemyTextures   enemyTextures = new EnemyTextures(cardTextures);
    private final CasinoButtons   buttons = new CasinoButtons();
    private final MenuDecor       decor  = new MenuDecor();
    private final Texture         chipTexture = new Texture(Gdx.files.internal(CHIP_PATH));
    private final Sound           clickSound;
    private final Sound           hoverSound;
    private final BackgroundMusic music;
    private final BitmapFont      titleFont   = Fonts.jersey(96, Palette.GOLD, 6f, Palette.TEXT_SHADE);
    private final BitmapFont      rowFont     = Fonts.jersey(50, Color.WHITE, 4f, Palette.TEXT_SHADE);
    private final BitmapFont      labelFont   = Fonts.jersey(36, Palette.CREAM, 3f, Palette.TEXT_SHADE);
    private final BitmapFont      chapterFont = Fonts.jersey(72, Palette.GOLD, 5f, Palette.TEXT_SHADE);
    private final BitmapFont      bodyFont    = Fonts.jersey(36, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont      lockFont    = Fonts.jersey(160, LOCKED_TEXT, 6f, Palette.TEXT_SHADE);

    private final Image             veil;
    private final Label             title;
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
    private final TextButton        backButton;
    private final Image             fade;

    private int     selected = -1;
    private boolean leaving;
    /** Mode difficile choisi (ouvert une fois la Machine Originelle battue). */
    private boolean hard;

    public TowerScreen(LuckyGame luckyGame) {
        this.luckyGame = luckyGame;
        this.stage     = new Stage(new MinimumScreenViewport(MIN_WIDTH, MIN_HEIGHT), luckyGame.getBatch());
        clickSound = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(CLICK_SOUND)), audio);
        hoverSound = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(HOVER_SOUND)), audio);
        music      = new BackgroundMusic(MUSIC, audio, MUSIC_LEVEL);

        TextureRegionDrawable pixel = new TextureRegionDrawable(new TextureRegion(hud.pixel));
        veil = new Image(pixel);
        veil.setColor(0f, 0f, 0f, 0.6f);
        veil.setTouchable(Touchable.disabled);
        title = new Label("TOUR DES ÉPREUVES", new Label.LabelStyle(titleFont, Color.WHITE));
        title.pack();
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
        startButton  = buttons.createAction("Commencer", clickSound, this::launch);
        backButton   = buttons.create("Retour", clickSound, this::onBack);
        // Mode difficile : ouvert après la Machine Originelle ; le bouton bascule entre les deux modes.
        modeButton   = buttons.create("Mode difficile", clickSound, this::toggleMode);
        modeButton.setText("Mode normal");
        modeButton.setVisible(luckyGame.getProfile().isTowerHardOpen());

        fade = new Image(pixel);
        fade.setColor(Color.BLACK);
        fade.setTouchable(Touchable.disabled);

        stage.addActor(decor);
        stage.addActor(veil);
        stage.addActor(title);
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
        stage.addActor(backButton);
        stage.addActor(fade);
        fade.addAction(Actions.fadeOut(FADE_TIME));

        select(0, false);
        layout();
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
        chapterLabel.setText(chapter.getLabel().toUpperCase() + (hard ? " · DIFFICILE" : ""));
        chapterTitle.setText(chapter.getTitle());
        description.setText(!open
            ? "Termine le " + chapter.getPrevious().getLabel().toLowerCase()
                + (hard ? " en mode difficile" : "") + " pour ouvrir ce chapitre."
            : hard
            ? chapter.getDescription() + "\n\nMode difficile : ennemis PV x" + EnemyKind.HARD_HP_FACTOR
                + ", force x" + EnemyKind.HARD_POWER_FACTOR + "."
            : chapter.getDescription());
        ((TextureRegionDrawable) art.getDrawable()).setRegion(new TextureRegion(enemyTextures.chapterArt(chapter)));
        art.setVisible(open);
        lockMark.setVisible(!open);
        startButton.setDisabled(!open);
        startButton.setText(open ? "Commencer" : "Verrouillé");
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
        fadeOutThen(() -> {
            luckyGame.setScreen(new GameScreen(luckyGame, new TowerRun(chapter, hard)));
            dispose();
        });
    }

    /** @return {@code true} si {@code chapter} peut être joué dans le mode choisi (le précédent est terminé). */
    private boolean isOpen(Chapter chapter) {
        return luckyGame.getProfile().isOpen(chapter, hard);
    }

    /** Bascule entre le mode normal et le mode difficile. */
    private void toggleMode() {
        hard = !hard;
        modeButton.setText(hard ? "Mode difficile" : "Mode normal");
        showChapter();
    }

    /** Retour au menu principal. */
    private void onBack() {
        if (leaving) return;
        fadeOutThen(() -> {
            luckyGame.setScreen(new MenuScreen(luckyGame));
            dispose();
        });
    }

    private void fadeOutThen(Runnable next) {
        leaving = true;
        fade.clearActions();
        fade.addAction(Actions.sequence(Actions.fadeIn(FADE_TIME), Actions.run(() -> Gdx.app.postRunnable(next))));
    }

    // -------------------------------------------------------------------------
    // Mise en page et cycle de vie
    // -------------------------------------------------------------------------

    /** Place le titre, les deux panneaux, leur contenu et les boutons selon la taille de l'écran. */
    private void layout() {
        float width  = stage.getViewport().getWorldWidth();
        float height = stage.getViewport().getWorldHeight();
        decor.layout(width, height);
        decor.setPosition(0f, 0f);
        veil.setBounds(0f, 0f, width, height);
        fade.setBounds(0f, 0f, width, height);
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
        backButton.setPosition(MARGIN, (BOTTOM_SPACE - backButton.getHeight()) / 2f);
    }

    /**
     * Stage d'abord (souris), puis clavier : haut/bas pour choisir un chapitre,
     * Entrée ou Espace pour le lancer, Échap pour revenir au menu principal.
     */
    @Override
    public void show() {
        InputAdapter keyboard = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (leaving) return false;
                switch (keycode) {
                    case Input.Keys.UP, Input.Keys.W -> select((selected + rows.size() - 1) % rows.size(), true);
                    case Input.Keys.DOWN, Input.Keys.S -> select((selected + 1) % rows.size(), true);
                    case Input.Keys.ENTER, Input.Keys.SPACE -> {
                        if (isOpen(Chapter.values()[selected])) {
                            clickSound.play();
                            launch();
                        }
                    }
                    case Input.Keys.ESCAPE -> {
                        clickSound.play();
                        onBack();
                    }
                    default -> { return false; }
                }
                return true;
            }
        };
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, keyboard));
        music.play();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        layout();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        music.update();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        decor.dispose();
        enemyTextures.dispose();
        cardTextures.dispose();
        buttons.dispose();
        chipTexture.dispose();
        clickSound.dispose();
        hoverSound.dispose();
        music.dispose();
        hud.dispose();
        Fonts.release(titleFont);
        Fonts.release(rowFont);
        Fonts.release(labelFont);
        Fonts.release(chapterFont);
        Fonts.release(bodyFont);
        Fonts.release(lockFont);
    }
}
