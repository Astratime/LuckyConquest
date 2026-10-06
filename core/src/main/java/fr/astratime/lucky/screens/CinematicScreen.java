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
import fr.astratime.lucky.animations.ScreenShake;
import fr.astratime.lucky.animations.cutscenes.Cutscene;
import fr.astratime.lucky.animations.cutscenes.CutsceneKit;
import fr.astratime.lucky.animations.cutscenes.Cutscenes;
import fr.astratime.lucky.animations.cutscenes.LastDrawCutscene;
import fr.astratime.lucky.assets.BackgroundMusic;
import fr.astratime.lucky.assets.CardTextures;
import fr.astratime.lucky.assets.EnemyTextures;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.GameSounds;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.assets.VolumeSound;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.events.LastDrawEvent;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.entities.tower.Chapter;
import fr.astratime.lucky.settings.AudioSettings;
import fr.astratime.lucky.settings.VisualSettings;
import fr.astratime.lucky.views.CasinoButtons;
import fr.astratime.lucky.views.MenuDecor;
import fr.astratime.lucky.views.MenuOption;
import fr.astratime.lucky.views.MinimumScreenViewport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Mode Cinématique (mode ADMIN seulement) : rejoue n'importe quelle cinématique
 * du jeu, sur le modèle de l'écran de la Tour des épreuves.
 *
 * À gauche, les groupes : les boss de la Tour, les fins et le duel des leviers,
 * puis les rois de chaque lieu de l'Exploration. À droite, les cinématiques du
 * groupe choisi, avec le portrait (ou l'illustration) de celle sélectionnée.
 * Un clic sur une cinématique (ou Entrée, ou « Lancer ») la joue par-dessus
 * l'écran ; finie ou passée (clic, ou une touche), on revient ici. Échap ou
 * « Retour » ramène au menu principal.
 */
public class CinematicScreen extends ScreenAdapter {

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
    private static final float ITEM_WIDTH   = 500f;
    private static final float ITEM_HEIGHT  = 64f;
    private static final float ITEM_GAP     = 14f;
    private static final float PANEL_PAD    = 40f;
    private static final float PANEL_TOP    = 210f;    // du haut de l'écran au haut des panneaux
    private static final float BOTTOM_SPACE = 130f;    // sous les panneaux : boutons
    private static final float ART_SCALE    = 4f;      // pixels du portrait
    private static final float FADE_TIME    = 0.4f;

    /** Une cinématique de la liste : son nom, une ligne d'explication, son image et de quoi la créer. */
    private record Entry(String name, String description, Texture art, Function<CutsceneKit, Cutscene> create) { }

    /** Un groupe de cinématiques (une ligne du panneau de gauche). */
    private record Group(String label, String title, List<Entry> entries, List<MenuOption> rows) { }

    private final LuckyGame       luckyGame;
    private final Stage           stage;
    private final VisualSettings  settings = new VisualSettings();
    private final AudioSettings   audio  = new AudioSettings();
    private final HudTextures     hud    = new HudTextures();
    private final CardTextures    cardTextures = new CardTextures();
    private final EnemyTextures   enemyTextures = new EnemyTextures(cardTextures);
    private final GameSounds      sounds = new GameSounds(audio);
    private final ScreenShake     shake  = new ScreenShake(settings);
    private final CasinoButtons   buttons = new CasinoButtons();
    private final MenuDecor       decor  = new MenuDecor();
    private final Texture         chipTexture = new Texture(Gdx.files.internal(CHIP_PATH));
    private final Sound           clickSound;
    private final Sound           hoverSound;
    private final BackgroundMusic music;
    private final BitmapFont      titleFont   = Fonts.jersey(96, Palette.GOLD, 6f, Palette.TEXT_SHADE);
    private final BitmapFont      rowFont     = Fonts.jersey(50, Color.WHITE, 4f, Palette.TEXT_SHADE);
    private final BitmapFont      itemFont    = Fonts.jersey(40, Color.WHITE, 3f, Palette.TEXT_SHADE);
    private final BitmapFont      labelFont   = Fonts.jersey(36, Palette.CREAM, 3f, Palette.TEXT_SHADE);
    private final BitmapFont      groupFont   = Fonts.jersey(72, Palette.GOLD, 5f, Palette.TEXT_SHADE);
    private final BitmapFont      bodyFont    = Fonts.jersey(36, Color.WHITE, 2f, Palette.TEXT_SHADE);
    /** Police des répliques des cinématiques. */
    private final BitmapFont      cutsceneFont = Fonts.jersey(30, Palette.GOLD, 2f, Palette.TEXT_SHADE);
    private final CutsceneKit     kit;

    private final Image             veil;
    private final Label             title;
    private final Image             listPanel;
    private final Image             detailPanel;
    private final List<Group>       groups = new ArrayList<>();
    private final List<MenuOption>  groupRows = new ArrayList<>();
    private final Label             groupLabel;
    private final Label             groupTitle;
    private final Image             art;
    private final Image             artFrame;
    private final Label             description;
    private final TextButton        playButton;
    private final TextButton        backButton;
    private final Image             fade;

    private int      group = -1;
    private int      selected = -1;
    private boolean  leaving;
    private Cutscene cutscene;

    public CinematicScreen(LuckyGame luckyGame) {
        this.luckyGame = luckyGame;
        this.stage     = new Stage(new MinimumScreenViewport(MIN_WIDTH, MIN_HEIGHT), luckyGame.getBatch());
        clickSound = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(CLICK_SOUND)), audio);
        hoverSound = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(HOVER_SOUND)), audio);
        music      = new BackgroundMusic(MUSIC, audio, MUSIC_LEVEL);
        kit        = new CutsceneKit(settings, shake, sounds, enemyTextures, cutsceneFont);

        TextureRegionDrawable pixel = new TextureRegionDrawable(new TextureRegion(hud.pixel));
        veil = new Image(pixel);
        veil.setColor(0f, 0f, 0f, 0.6f);
        veil.setTouchable(Touchable.disabled);
        title = new Label("CINÉMATIQUES", new Label.LabelStyle(titleFont, Color.WHITE));
        title.pack();
        listPanel   = new Image(hud.panelDrawable());
        detailPanel = new Image(hud.panelDrawable());
        listPanel.setTouchable(Touchable.disabled);
        detailPanel.setTouchable(Touchable.disabled);

        buildGroups();
        Label.LabelStyle rowStyle = new Label.LabelStyle(rowFont, Color.WHITE);
        for (int i = 0; i < groups.size(); i++) {
            int index = i;
            MenuOption row = new MenuOption(groups.get(i).label(), rowStyle, hud.insetDrawable(),
                new TextureRegion(chipTexture), ROW_WIDTH, ROW_HEIGHT);
            row.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    super.enter(event, x, y, pointer, fromActor);
                    if (pointer == -1) selectGroup(index, true);
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selectGroup(index, false);
                }
            });
            groupRows.add(row);
        }

        groupLabel  = new Label("", new Label.LabelStyle(labelFont, Color.WHITE));
        groupTitle  = new Label("", new Label.LabelStyle(groupFont, Color.WHITE));
        artFrame    = new Image(hud.insetDrawable());
        art         = new Image(new TextureRegionDrawable(new TextureRegion(enemyTextures.portrait(EnemyKind.COMETE))));
        description = new Label("", new Label.LabelStyle(bodyFont, Color.WHITE));
        description.setWrap(true);
        description.setAlignment(Align.topLeft);
        playButton  = buttons.createAction("Lancer", clickSound, this::launch);
        backButton  = buttons.create("Retour", clickSound, this::onBack);

        fade = new Image(pixel);
        fade.setColor(Color.BLACK);
        fade.setTouchable(Touchable.disabled);

        stage.addActor(shake);                   // invisible : met à jour la caméra
        stage.addActor(decor);
        stage.addActor(veil);
        stage.addActor(title);
        stage.addActor(listPanel);
        stage.addActor(detailPanel);
        groupRows.forEach(stage::addActor);
        stage.addActor(groupLabel);
        stage.addActor(groupTitle);
        for (Group g : groups) g.rows().forEach(stage::addActor);
        stage.addActor(artFrame);
        stage.addActor(art);
        stage.addActor(description);
        stage.addActor(playButton);
        stage.addActor(backButton);
        stage.addActor(fade);
        fade.addAction(Actions.fadeOut(FADE_TIME));

        selectGroup(0, false);
        layout();
    }

    // -------------------------------------------------------------------------
    // Les cinématiques
    // -------------------------------------------------------------------------

    /** Les groupes : boss de la Tour (par chapitre), fins et duel, puis les rois de chaque lieu. */
    private void buildGroups() {
        List<Entry> bosses = new ArrayList<>();
        for (Chapter chapter : Chapter.values()) {
            EnemyKind boss = chapter.getBoss();
            bosses.add(new Entry(chapter.getNumber() + ". " + boss.getDisplayName(),
                chapter.getLabel() + ", « " + chapter.getTitle() + " » : avant le combat contre le boss.",
                enemyTextures.portrait(boss), kit -> Cutscenes.beforeBoss(kit, boss)));
        }
        addGroup("Tour : boss", "Les boss de la Tour", bosses);

        List<Entry> endings = new ArrayList<>();
        Symbol[] reels = Symbol.classicReels().toArray(new Symbol[0]);
        endings.add(new Entry("Duel des leviers : victoire",
            "Le Dernier tirage de la Machine Originelle. Tire ton levier à la souris. "
                + "Une égalité, puis tu gagnes.",
            enemyTextures.portrait(EnemyKind.MACHINE_ORIGINELLE),
            kit -> new LastDrawCutscene(kit, new LastDrawEvent(List.of(reels),
                List.of(Symbol.BELL, Symbol.SEVEN), List.of(Symbol.BELL, Symbol.GOLD_BAR), true))));
        endings.add(new Entry("Duel des leviers : défaite",
            "Le Dernier tirage de la Machine Originelle. Tire ton levier à la souris. La Machine gagne.",
            enemyTextures.portrait(EnemyKind.MACHINE_ORIGINELLE),
            kit -> new LastDrawCutscene(kit, new LastDrawEvent(List.of(reels),
                List.of(Symbol.CHERRY), List.of(Symbol.DIAMOND), false))));
        for (Chapter chapter : List.of(Chapter.DERNIER_TIRAGE, Chapter.LE_JACKPOT)) { // voir Cutscenes.ending
            endings.add(new Entry("Fin du " + chapter.getLabel().toLowerCase(),
                "La fin de « " + chapter.getTitle() + " », après la victoire contre "
                    + chapter.getBoss().getDisplayName() + ".",
                enemyTextures.chapterArt(chapter), kit -> Cutscenes.ending(kit, chapter)));
        }
        addGroup("Tour : fins", "Le duel et les fins", endings);

        for (Place place : Place.values()) {
            List<Entry> kings = new ArrayList<>();
            for (Dungeon dungeon : place.getDungeons()) {
                EnemyKind king = dungeon.getKing();
                kings.add(new Entry(king.getDisplayName(),
                    place.getName() + ", « " + dungeon.getName() + " » : l'entrée en scène du roi, après le soldat.",
                    enemyTextures.portrait(king), kit -> Cutscenes.beforeBoss(kit, king)));
            }
            addGroup(place.getShortName(), place.getName(), kings);
        }
    }

    private void addGroup(String label, String groupName, List<Entry> entries) {
        int groupIndex = groups.size();
        Label.LabelStyle style = new Label.LabelStyle(itemFont, Color.WHITE);
        List<MenuOption> rows = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            int index = i;
            MenuOption row = new MenuOption(entries.get(i).name(), style, hud.insetDrawable(),
                new TextureRegion(chipTexture), ITEM_WIDTH, ITEM_HEIGHT);
            row.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    super.enter(event, x, y, pointer, fromActor);
                    if (pointer == -1 && group == groupIndex) select(index, true);
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (group != groupIndex) return;
                    select(index, false);
                    launch();
                }
            });
            rows.add(row);
        }
        groups.add(new Group(label, groupName, entries, rows));
    }

    // -------------------------------------------------------------------------
    // Sélection et lecture
    // -------------------------------------------------------------------------

    /** Sélectionne le groupe {@code index} et montre ses cinématiques (la première sélectionnée). */
    private void selectGroup(int index, boolean sound) {
        if (index == group) return;
        if (sound) hoverSound.play();
        group = index;
        for (int i = 0; i < groupRows.size(); i++) groupRows.get(i).setSelected(i == index);
        for (int i = 0; i < groups.size(); i++) {
            for (MenuOption row : groups.get(i).rows()) row.setVisible(i == index);
        }
        Group current = groups.get(index);
        groupLabel.setText(current.label().toUpperCase());
        groupTitle.setText(current.title());
        selected = -1;
        select(0, false);
        fadeIn(groupLabel);
        fadeIn(groupTitle);
    }

    /** Sélectionne la cinématique {@code index} du groupe et montre son image et son explication. */
    private void select(int index, boolean sound) {
        if (index == selected) return;
        if (sound) hoverSound.play();
        selected = index;
        Group current = groups.get(group);
        for (int i = 0; i < current.rows().size(); i++) current.rows().get(i).setSelected(i == index);
        Entry entry = current.entries().get(index);
        ((TextureRegionDrawable) art.getDrawable()).setRegion(new TextureRegion(entry.art()));
        description.setText(entry.description());
        fadeIn(art);
        fadeIn(description);
        layout();
    }

    private static void fadeIn(Actor actor) {
        actor.clearActions();
        actor.getColor().a = 0f;
        actor.addAction(Actions.fadeIn(0.25f, Interpolation.pow2Out));
    }

    /** Joue la cinématique sélectionnée par-dessus l'écran (la musique s'efface pendant la scène). */
    private void launch() {
        if (leaving || isPlaying()) return;
        Cutscene next = groups.get(group).entries().get(selected).create().apply(kit);
        if (next == null) return;
        if (cutscene != null) {
            cutscene.remove();
            cutscene.dispose();
        }
        cutscene = next;
        stage.addActor(cutscene);
        music.setDucked(true);
        // Écran couvert : l'écran de choix est déjà dessous, le fondu final s'estompe par-dessus.
        cutscene.play(() -> {
            music.setDucked(false);
            shake.stop();
        });
    }

    /** @return {@code true} tant qu'une cinématique joue (jusqu'à son fondu final). */
    private boolean isPlaying() {
        return cutscene != null && cutscene.isPlaying();
    }

    /** Retour au menu principal. */
    private void onBack() {
        if (leaving || isPlaying()) return;
        leaving = true;
        fade.clearActions();
        fade.toFront();
        fade.addAction(Actions.sequence(Actions.fadeIn(FADE_TIME), Actions.run(() -> Gdx.app.postRunnable(() -> {
            luckyGame.setScreen(new MenuScreen(luckyGame));
            dispose();
        }))));
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
        for (int i = 0; i < groupRows.size(); i++) {
            groupRows.get(i).setPosition(rowX, panelTop - PANEL_PAD - ROW_HEIGHT - i * (ROW_HEIGHT + ROW_GAP));
        }

        float detailX     = MARGIN + LIST_WIDTH + MARGIN / 2f;
        float detailWidth = width - detailX - MARGIN;
        detailPanel.setBounds(detailX, panelBottom, detailWidth, panelHeight);
        float innerX = detailX + PANEL_PAD;

        groupLabel.pack();
        groupLabel.setPosition(innerX, panelTop - PANEL_PAD - groupLabel.getHeight());
        groupTitle.pack();
        groupTitle.setPosition(innerX, groupLabel.getY() - groupTitle.getHeight());

        // Les cinématiques du groupe, en colonne sous le titre.
        float itemsTop = groupTitle.getY() - 24f;
        for (Group g : groups) {
            for (int i = 0; i < g.rows().size(); i++) {
                g.rows().get(i).setPosition(innerX, itemsTop - ITEM_HEIGHT - i * (ITEM_HEIGHT + ITEM_GAP));
            }
        }

        // À droite des cinématiques : l'image de celle sélectionnée, puis son explication.
        float sideX     = innerX + ITEM_WIDTH + PANEL_PAD + 40f; // place des jetons de la cinématique choisie
        float sideWidth = detailX + detailWidth - PANEL_PAD - sideX;
        float frame     = 10f;
        Texture texture = groups.isEmpty() || selected < 0 ? null : groups.get(group).entries().get(selected).art();
        float artScale  = ART_SCALE;
        float maxArtHeight = itemsTop - panelBottom - PANEL_PAD - 220f;
        if (texture != null) {
            while (artScale > 1f && (texture.getHeight() * artScale > maxArtHeight
                || texture.getWidth() * artScale > sideWidth - frame * 2)) {
                artScale -= 1f;
            }
        }
        float artWidth  = texture == null ? 0f : texture.getWidth() * artScale;
        float artHeight = texture == null ? 0f : texture.getHeight() * artScale;
        float artX = sideX + (sideWidth - artWidth) / 2f, artY = itemsTop - frame - artHeight;
        art.setBounds(artX, artY, artWidth, artHeight);
        artFrame.setBounds(artX - frame, artY - frame, artWidth + frame * 2, artHeight + frame * 2);

        float descriptionTop = artY - frame - 24f;
        description.setWidth(sideWidth);
        description.setHeight(Math.max(60f, descriptionTop - panelBottom - PANEL_PAD));
        description.setPosition(sideX, panelBottom + PANEL_PAD);

        playButton.setPosition(detailX + detailWidth - playButton.getWidth(), (BOTTOM_SPACE - playButton.getHeight()) / 2f);
        backButton.setPosition(MARGIN, (BOTTOM_SPACE - backButton.getHeight()) / 2f);
    }

    /**
     * Stage d'abord (souris), puis clavier : pendant une cinématique, une touche
     * la passe. Sinon gauche/droite (ou Tab) changent de groupe, haut/bas
     * choisissent une cinématique, Entrée ou Espace la lance, Échap revient au menu.
     */
    @Override
    public void show() {
        InputAdapter keyboard = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (leaving) return false;
                if (isPlaying()) {
                    cutscene.skip();
                    return true;
                }
                int count = groups.get(group).entries().size();
                switch (keycode) {
                    case Input.Keys.UP, Input.Keys.W -> select((selected + count - 1) % count, true);
                    case Input.Keys.DOWN, Input.Keys.S -> select((selected + 1) % count, true);
                    case Input.Keys.LEFT, Input.Keys.A -> selectGroup((group + groups.size() - 1) % groups.size(), true);
                    case Input.Keys.RIGHT, Input.Keys.D, Input.Keys.TAB -> selectGroup((group + 1) % groups.size(), true);
                    case Input.Keys.ENTER, Input.Keys.SPACE -> {
                        clickSound.play();
                        launch();
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
        if (cutscene != null) cutscene.cancel();
        stage.dispose();
        if (cutscene != null) cutscene.dispose();
        decor.dispose();
        enemyTextures.dispose();
        cardTextures.dispose();
        sounds.dispose();
        buttons.dispose();
        chipTexture.dispose();
        clickSound.dispose();
        hoverSound.dispose();
        music.dispose();
        hud.dispose();
        Fonts.release(titleFont);
        Fonts.release(rowFont);
        Fonts.release(itemFont);
        Fonts.release(labelFont);
        Fonts.release(groupFont);
        Fonts.release(bodyFont);
        Fonts.release(cutsceneFont);
    }
}
