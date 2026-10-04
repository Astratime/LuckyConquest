package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.assets.BackgroundMusic;
import fr.astratime.lucky.assets.CardTextures;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.assets.VolumeSound;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.loaders.CardLoader;
import fr.astratime.lucky.progress.DeckDraft;
import fr.astratime.lucky.progress.PlayerProfile;
import fr.astratime.lucky.settings.AudioSettings;
import fr.astratime.lucky.views.CasinoButtons;
import fr.astratime.lucky.views.MenuDecor;
import fr.astratime.lucky.views.MinimumScreenViewport;
import fr.astratime.lucky.views.Tooltip;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Construction de deck : le joueur choisit les {@link PlayerProfile#DECK_SIZE}
 * cartes qu'il emmène au combat, parmi celles qu'il possède.
 *
 * À gauche, sa collection : chaque carte, avec le nombre d'exemplaires déjà
 * dans le deck sur le nombre qu'il peut y mettre (ceux qu'il possède, au plus
 * {@link PlayerProfile#MAX_COPIES}). Clic gauche : un exemplaire de plus ; clic
 * droit : un de moins. À droite, le deck : le compteur de cartes et la liste,
 * où un clic retire un exemplaire. « Enregistrer » n'est possible qu'avec
 * exactement {@link PlayerProfile#DECK_SIZE} cartes ; « Retour » abandonne les changements.
 */
public class DeckBuilderScreen extends ScreenAdapter {

    private static final String CLICK_SOUND  = "sounds/button-click.ogg";
    private static final String DEAL_SOUND   = "sounds/card-deal.ogg";
    private static final String REFUSE_SOUND = "sounds/coins/loss.ogg";
    private static final String MUSIC        = "music/main_menu.ogg";
    private static final float  MUSIC_LEVEL  = 0.3f;

    private static final float MIN_WIDTH    = 1600f;
    private static final float MIN_HEIGHT   = 1080f;
    private static final float MARGIN       = 60f;
    private static final float TITLE_TOP    = 100f;
    private static final float PANEL_TOP    = 190f;
    private static final float BOTTOM_SPACE = 130f;
    private static final float PANEL_PAD    = 30f;
    private static final float DECK_WIDTH   = 470f;
    private static final float CARD_WIDTH   = 110f;
    private static final float CARD_HEIGHT  = 154f;   // images 480 x 672
    private static final float CELL_GAP     = 16f;
    private static final float HOVER_SCALE  = 1.06f;
    private static final float FADE_TIME    = 0.4f;
    private static final Color REFUSED      = new Color(1f, 0.45f, 0.45f, 1f);
    private static final Color USED_UP      = new Color(0.6f, 0.6f, 0.6f, 1f);

    private final LuckyGame       luckyGame;
    private final PlayerProfile   profile;
    private final DeckDraft       draft;
    private final Stage           stage;
    private final AudioSettings   audio  = new AudioSettings();
    private final HudTextures     hud    = new HudTextures();
    private final CardTextures    cardTextures = new CardTextures();
    private final CasinoButtons   buttons = new CasinoButtons();
    private final MenuDecor       decor  = new MenuDecor();
    private final Tooltip         tooltip = new Tooltip(hud);
    private final Sound           clickSound;
    private final Sound           dealSound;
    private final Sound           refuseSound;
    private final BackgroundMusic music;
    private final BitmapFont      titleFont   = Fonts.jersey(84, Palette.GOLD, 6f, Palette.TEXT_SHADE);
    private final BitmapFont      headFont    = Fonts.jersey(44, Palette.GOLD, 3f, Palette.TEXT_SHADE);
    private final BitmapFont      counterFont = Fonts.jersey(64, Color.WHITE, 4f, Palette.TEXT_SHADE);
    private final BitmapFont      countFont   = Fonts.jersey(28, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont      rowFont     = Fonts.jersey(30, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont      hintFont    = Fonts.jersey(26, Palette.CREAM, 2f, Palette.TEXT_SHADE);

    /** Une carte de chaque sorte possédée, par id, dans l'ordre de la collection. */
    private final Map<String, Card> cards = new LinkedHashMap<>();

    private final Image       veil;
    private final Label       title;
    private final Image       collectionPanel;
    private final Image       deckPanel;
    private final Label       collectionHead;
    private final Label       deckHead;
    private final Label       counter;
    private final Table       grid = new Table();
    private final ScrollPane  scroll;
    private final List<Group> cells = new ArrayList<>();
    private final Map<String, Label> cellCounts = new LinkedHashMap<>();
    private final Table       deckList = new Table();
    private final Label       hint;
    private final TextButton  saveButton;
    private final TextButton  starterButton;
    private final TextButton  clearButton;
    private final TextButton  backButton;
    private final Image       fade;

    private int     columns = 6;
    private boolean leaving;

    public DeckBuilderScreen(LuckyGame luckyGame) {
        this.luckyGame = luckyGame;
        this.profile   = luckyGame.getProfile();
        this.draft     = new DeckDraft(profile);
        this.stage     = new Stage(new MinimumScreenViewport(MIN_WIDTH, MIN_HEIGHT), luckyGame.getBatch());
        clickSound  = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(CLICK_SOUND)), audio);
        dealSound   = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(DEAL_SOUND)), audio);
        refuseSound = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(REFUSE_SOUND)), audio);
        music       = new BackgroundMusic(MUSIC, audio, MUSIC_LEVEL);

        Function<String, Card> factory = CardLoader.cardFactory();
        for (String id : profile.getCollection().keySet()) cards.put(id, factory.apply(id));

        TextureRegionDrawable pixel = new TextureRegionDrawable(new TextureRegion(hud.pixel));
        veil = new Image(pixel);
        veil.setColor(0f, 0f, 0f, 0.65f);
        veil.setTouchable(Touchable.disabled);
        title = new Label("CONSTRUCTION DE DECK", new Label.LabelStyle(titleFont, Color.WHITE));
        title.pack();
        collectionPanel = new Image(hud.panelDrawable());
        deckPanel       = new Image(hud.panelDrawable());
        collectionPanel.setTouchable(Touchable.disabled);
        deckPanel.setTouchable(Touchable.disabled);
        collectionHead = new Label("MA COLLECTION", new Label.LabelStyle(headFont, Color.WHITE));
        deckHead       = new Label("MON DECK", new Label.LabelStyle(headFont, Color.WHITE));
        counter        = new Label("", new Label.LabelStyle(counterFont, Color.WHITE));
        hint = new Label("Clic gauche : ajouter. Clic droit : retirer.", new Label.LabelStyle(hintFont, Color.WHITE));

        for (Card card : cards.values()) cells.add(buildCell(card));
        scroll = new ScrollPane(grid);
        scroll.setScrollingDisabled(true, false);
        scroll.setFadeScrollBars(false);
        scroll.setOverscroll(false, false);

        saveButton    = buttons.createAction("Enregistrer", clickSound, this::onSave);
        starterButton = buttons.create("Deck de départ", clickSound, () -> { draft.resetToStarter(); refresh(); });
        clearButton   = buttons.create("Vider", clickSound, () -> { draft.clear(); refresh(); });
        backButton    = buttons.create("Retour", clickSound, this::onBack);

        fade = new Image(pixel);
        fade.setColor(Color.BLACK);
        fade.setTouchable(Touchable.disabled);

        stage.addActor(decor);
        stage.addActor(veil);
        stage.addActor(title);
        stage.addActor(collectionPanel);
        stage.addActor(deckPanel);
        stage.addActor(collectionHead);
        stage.addActor(deckHead);
        stage.addActor(counter);
        stage.addActor(scroll);
        stage.addActor(deckList);
        stage.addActor(hint);
        stage.addActor(saveButton);
        stage.addActor(starterButton);
        stage.addActor(clearButton);
        stage.addActor(backButton);
        stage.addActor(tooltip.getActor());
        stage.addActor(fade);
        fade.addAction(Actions.fadeOut(FADE_TIME));

        layout();
        refresh();
    }

    // -------------------------------------------------------------------------
    // Collection
    // -------------------------------------------------------------------------

    /**
     * Case d'une carte de la collection : son image et, dessous, « dans le deck /
     * au plus ». Clic gauche : ajoute un exemplaire ; clic droit : en retire un.
     */
    private Group buildCell(Card card) {
        Group cell = new Group();
        cell.setSize(CARD_WIDTH, CARD_HEIGHT + 34f);
        Group holder = new Group();
        holder.setSize(CARD_WIDTH, CARD_HEIGHT);
        holder.setPosition(0f, 34f);
        holder.setOrigin(Align.center);
        holder.setTransform(true);
        Image image = new Image(new TextureRegionDrawable(new TextureRegion(cardTextures.get(card))));
        image.setSize(CARD_WIDTH, CARD_HEIGHT);
        holder.addActor(image);
        Label count = new Label("", new Label.LabelStyle(countFont, Color.WHITE));
        count.setAlignment(Align.center);
        count.setBounds(0f, 0f, CARD_WIDTH, 30f);
        cell.addActor(holder);
        cell.addActor(count);
        cellCounts.put(card.getId(), count);

        ClickListener listener = new ClickListener(-1) {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                if (pointer != -1) return;
                holder.clearActions();
                holder.addAction(Actions.scaleTo(HOVER_SCALE, HOVER_SCALE, 0.1f, Interpolation.pow2Out));
                Vector2 pos = holder.localToStageCoordinates(new Vector2(0f, CARD_HEIGHT + 8f));
                tooltip.show(card.getName(), card.getDescription(), pos.x, pos.y);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                if (pointer != -1) return;
                holder.clearActions();
                holder.addAction(Actions.scaleTo(1f, 1f, 0.1f, Interpolation.pow2Out));
                tooltip.hide();
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (event.getButton() == Input.Buttons.RIGHT) removeCard(card.getId());
                else addCard(card.getId(), holder);
            }
        };
        cell.addListener(listener);
        return cell;
    }

    /** Ajoute un exemplaire de la carte {@code id} au deck, ou dit pourquoi ce n'est pas possible. */
    private void addCard(String id, Actor feedback) {
        String problem = draft.addProblem(id);
        if (problem != null) {
            refuseSound.play();
            hint.setText(problem + ".");
            hint.setColor(REFUSED);
            feedback.clearActions();
            feedback.addAction(Actions.sequence(
                Actions.moveBy(-6f, 0f, 0.04f), Actions.moveBy(12f, 0f, 0.08f), Actions.moveBy(-6f, 0f, 0.04f),
                Actions.scaleTo(HOVER_SCALE, HOVER_SCALE, 0f)));
            return;
        }
        draft.add(id);
        dealSound.play();
        refresh();
    }

    /** Retire un exemplaire de la carte {@code id} du deck. */
    private void removeCard(String id) {
        if (!draft.remove(id)) return;
        clickSound.play();
        refresh();
    }

    // -------------------------------------------------------------------------
    // Deck
    // -------------------------------------------------------------------------

    /** Met à jour les compteurs de la collection, le compteur du deck, sa liste et le bouton « Enregistrer ». */
    private void refresh() {
        for (Map.Entry<String, Label> entry : cellCounts.entrySet()) {
            String id = entry.getKey();
            int inDeck = draft.getCopies(id), max = draft.getMaxCopies(id);
            Label count = entry.getValue();
            count.setText(inDeck + " / " + max);
            count.setColor(inDeck >= max ? Palette.GOLD : inDeck > 0 ? Color.WHITE : Palette.CREAM);
        }
        int index = 0;
        for (String id : cards.keySet()) {
            // Les cartes dont tous les exemplaires sont dans le deck s'assombrissent.
            Group holder = (Group) cells.get(index++).getChildren().first();
            holder.setColor(draft.getCopies(id) >= draft.getMaxCopies(id) ? USED_UP : Color.WHITE);
        }

        int size = draft.size();
        counter.setText(size + " / " + PlayerProfile.DECK_SIZE);
        counter.setColor(draft.isComplete() ? Palette.GOLD : REFUSED);
        saveButton.setDisabled(!draft.isComplete());
        hint.setColor(Color.WHITE);
        hint.setText(draft.isComplete() ? "Le deck est prêt."
            : size < PlayerProfile.DECK_SIZE ? "Encore " + (PlayerProfile.DECK_SIZE - size) + " carte" + (PlayerProfile.DECK_SIZE - size > 1 ? "s" : "") + " à choisir."
            : "Clic droit : retirer une carte.");

        deckList.clearChildren();
        Label.LabelStyle style = new Label.LabelStyle(rowFont, Color.WHITE);
        for (Map.Entry<String, Card> entry : cards.entrySet()) {
            int copies = draft.getCopies(entry.getKey());
            if (copies <= 0) continue;
            String id = entry.getKey();
            Label row = new Label("x" + copies + "   " + entry.getValue().getName(), style);
            row.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    super.enter(event, x, y, pointer, fromActor);
                    if (pointer == -1) row.setColor(REFUSED);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    super.exit(event, x, y, pointer, toActor);
                    if (pointer == -1) row.setColor(Color.WHITE);
                }

                @Override
                public void clicked(InputEvent event, float x, float y) { removeCard(id); }
            });
            deckList.add(row).left().height(30f).row();
        }
        layout();
    }

    /** Enregistre le deck (complet) et revient au menu principal. */
    private void onSave() {
        if (leaving || !draft.save()) return;
        leaveTo(() -> new MenuScreen(luckyGame));
    }

    /** Retour au menu principal, sans enregistrer. */
    private void onBack() {
        if (leaving) return;
        leaveTo(() -> new MenuScreen(luckyGame));
    }

    private void leaveTo(Supplier<Screen> next) {
        leaving = true;
        tooltip.hide();
        fade.clearActions();
        fade.addAction(Actions.sequence(Actions.fadeIn(FADE_TIME), Actions.run(() -> Gdx.app.postRunnable(() -> {
            luckyGame.setScreen(next.get());
            dispose();
        }))));
    }

    // -------------------------------------------------------------------------
    // Mise en page et cycle de vie
    // -------------------------------------------------------------------------

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
        float deckX = width - MARGIN - DECK_WIDTH;
        float collectionWidth = deckX - MARGIN / 2f - MARGIN;
        collectionPanel.setBounds(MARGIN, panelBottom, collectionWidth, panelHeight);
        deckPanel.setBounds(deckX, panelBottom, DECK_WIDTH, panelHeight);

        collectionHead.pack();
        collectionHead.setPosition(MARGIN + PANEL_PAD, panelTop - PANEL_PAD - collectionHead.getHeight());
        float gridTop = collectionHead.getY() - 12f;
        float gridWidth = collectionWidth - PANEL_PAD * 2;
        int newColumns = Math.max(1, (int) ((gridWidth + CELL_GAP) / (CARD_WIDTH + CELL_GAP)));
        if (newColumns != columns || grid.getChildren().isEmpty()) {
            columns = newColumns;
            grid.clearChildren();
            for (int i = 0; i < cells.size(); i++) {
                grid.add(cells.get(i)).size(CARD_WIDTH, CARD_HEIGHT + 34f).pad(CELL_GAP / 2f);
                if ((i + 1) % columns == 0) grid.row();
            }
        }
        grid.top().left();
        scroll.setBounds(MARGIN + PANEL_PAD, panelBottom + PANEL_PAD, gridWidth, gridTop - panelBottom - PANEL_PAD);
        scroll.layout();

        deckHead.pack();
        deckHead.setPosition(deckX + PANEL_PAD, panelTop - PANEL_PAD - deckHead.getHeight());
        counter.pack();
        counter.setPosition(deckX + DECK_WIDTH - PANEL_PAD - counter.getWidth(),
            deckHead.getY() + (deckHead.getHeight() - counter.getHeight()) / 2f);
        deckList.pack();
        deckList.setPosition(deckX + PANEL_PAD, deckHead.getY() - 20f - deckList.getHeight());
        hint.setWrap(true);
        hint.setWidth(DECK_WIDTH - PANEL_PAD * 2);
        hint.setHeight(hint.getPrefHeight());
        hint.setPosition(deckX + PANEL_PAD, panelBottom + PANEL_PAD);

        float buttonY = (BOTTOM_SPACE - saveButton.getHeight()) / 2f;
        saveButton.setPosition(width - MARGIN - saveButton.getWidth(), buttonY);
        clearButton.setPosition(saveButton.getX() - 24f - clearButton.getWidth(), buttonY);
        starterButton.setPosition(clearButton.getX() - 24f - starterButton.getWidth(), buttonY);
        backButton.setPosition(MARGIN, buttonY);
    }

    /** Stage d'abord (souris), puis clavier : Échap revient au menu principal sans enregistrer. */
    @Override
    public void show() {
        InputAdapter keyboard = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (leaving || keycode != Input.Keys.ESCAPE) return false;
                clickSound.play();
                onBack();
                return true;
            }
        };
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, keyboard));
        stage.setScrollFocus(scroll);
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
        cardTextures.dispose();
        buttons.dispose();
        tooltip.dispose();
        clickSound.dispose();
        dealSound.dispose();
        refuseSound.dispose();
        music.dispose();
        hud.dispose();
        Fonts.release(titleFont);
        Fonts.release(headFont);
        Fonts.release(counterFont);
        Fonts.release(countFont);
        Fonts.release(rowFont);
        Fonts.release(hintFont);
    }
}
