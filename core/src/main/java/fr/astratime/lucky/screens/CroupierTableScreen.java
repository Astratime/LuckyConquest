package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
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
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.loaders.CardLoader;
import fr.astratime.lucky.progress.DeckDraft;
import fr.astratime.lucky.progress.MachineDraft;
import fr.astratime.lucky.progress.ReelShop;
import fr.astratime.lucky.progress.PlayerProfile;
import fr.astratime.lucky.settings.AudioSettings;
import fr.astratime.lucky.views.CasinoButtons;
import fr.astratime.lucky.views.GuideOverlay;
import fr.astratime.lucky.views.MenuDecor;
import fr.astratime.lucky.views.MinimumScreenViewport;
import fr.astratime.lucky.views.Tooltip;
import fr.astratime.lucky.views.UpgradeOverlay;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Table du croupier : le joueur y prépare ce qu'il emmène au combat, en deux onglets.
 * <ul>
 *   <li>« Deck » : les {@link PlayerProfile#DECK_SIZE} cartes de son deck, parmi
 *       celles qu'il possède (collection de départ, coffres, boutique) ;</li>
 *   <li>« Rouleaux » : les {@link Symbol#MACHINE_SIZE} rouleaux de sa machine,
 *       tous différents, parmi les classiques et ceux achetés à la boutique.</li>
 * </ul>
 *
 * À gauche, sa collection : chaque carte, avec le nombre d'exemplaires déjà
 * dans le deck sur le nombre qu'il peut y mettre (ceux qu'il possède, au plus
 * {@link PlayerProfile#MAX_COPIES}). Clic gauche : un exemplaire de plus ; clic
 * droit : un de moins. À droite, le deck : le compteur de cartes et la liste,
 * où un clic retire un exemplaire. « Enregistrer » n'est possible qu'avec
 * exactement {@link PlayerProfile#DECK_SIZE} cartes ; « Retour » abandonne les changements.
 * Dans l'onglet « Rouleaux », un clic place un rouleau dans la machine ou l'en
 * retire. « Enregistrer » enregistre le deck et la machine, complets tous les deux.
 *
 * Une carte possédée en {@link Card#UPGRADE_COST} exemplaires montre le bouton
 * « FUSIONNER » : ils deviennent un exemplaire de sa version « + » (voir
 * {@link PlayerProfile#upgradeCard(String)}), tout de suite enregistré.
 */
public class CroupierTableScreen extends ScreenAdapter {

    private static final String CLICK_SOUND  = "sounds/button-click.ogg";
    private static final String DEAL_SOUND   = "sounds/card-deal.ogg";
    private static final String REFUSE_SOUND = "sounds/coins/loss.ogg";
    private static final String UPGRADE_SOUND = "sounds/shop/card_upgrade.ogg";
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
    private static final float REEL_WIDTH   = 144f;
    private static final float REEL_HEIGHT  = 120f;
    private static final float CELL_GAP     = 16f;
    private static final float HOVER_SCALE  = 1.06f;
    private static final float FADE_TIME    = 0.4f;
    private static final Color REFUSED      = new Color(1f, 0.45f, 0.45f, 1f);
    private static final Color USED_UP      = new Color(0.6f, 0.6f, 0.6f, 1f);

    private final LuckyGame       luckyGame;
    private final PlayerProfile   profile;
    private final DeckDraft       draft;
    private final MachineDraft    machineDraft;
    private final Stage           stage;
    private final AudioSettings   audio  = new AudioSettings();
    private final HudTextures     hud    = new HudTextures();
    /** Première visite : le Croupier présente l'écran. */
    private final FirstVisitGuide firstVisit;
    private final CardTextures    cardTextures = new CardTextures();
    private final CasinoButtons   buttons = new CasinoButtons();
    private final MenuDecor       decor  = new MenuDecor();
    private final Tooltip         tooltip = new Tooltip(hud);
    private final Sound           clickSound;
    private final Sound           dealSound;
    private final Sound           refuseSound;
    private final Sound           upgradeSound;
    /** Fenêtre de fusion d'une carte en sa version « + ». */
    private final UpgradeOverlay  upgradeOverlay;
    private final Function<String, Card> factory = CardLoader.cardFactory();
    private final BackgroundMusic music;
    private final BitmapFont      titleFont   = Fonts.jersey(84, Palette.GOLD, 6f, Palette.TEXT_SHADE);
    private final BitmapFont      headFont    = Fonts.jersey(44, Palette.GOLD, 3f, Palette.TEXT_SHADE);
    private final BitmapFont      counterFont = Fonts.jersey(64, Color.WHITE, 4f, Palette.TEXT_SHADE);
    private final BitmapFont      countFont   = Fonts.jersey(28, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont      rowFont     = Fonts.jersey(30, Color.WHITE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont      hintFont    = Fonts.jersey(26, Palette.CREAM, 2f, Palette.TEXT_SHADE);
    private final BitmapFont      upgradeFont = Fonts.jersey(22, Color.WHITE, 2f, Palette.TEXT_SHADE);

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
    /** Bouton « FUSIONNER » de chaque carte de la collection, par id. */
    private final Map<String, Group> cellUpgrades = new LinkedHashMap<>();
    private final Table       deckList = new Table();
    /** Images des rouleaux possédés. */
    private final Map<Symbol, Texture> reelTextures = new EnumMap<>(Symbol.class);
    private final Table       reelGrid = new Table();
    private final ScrollPane  reelScroll;
    private final List<Group> reelCells = new ArrayList<>();
    private final Map<Symbol, Label> reelStates = new EnumMap<>(Symbol.class);
    private final Table       machineList = new Table();
    private final TextButton  deckTab;
    private final TextButton  reelsTab;
    private final TextButton  classicButton;
    private final Label       hint;
    private final TextButton  saveButton;
    private final TextButton  starterButton;
    private final TextButton  clearButton;
    private final TextButton  backButton;
    /** Rejoue la présentation du Croupier. */
    private final TextButton  tutorialButton;
    private final Image       fade;

    private int     columns = 6;
    private int     reelColumns = 4;
    /** Onglet « Rouleaux » affiché (sinon « Deck »). */
    private boolean showingReels;
    private boolean leaving;

    public CroupierTableScreen(LuckyGame luckyGame) {
        this.luckyGame = luckyGame;
        this.profile   = luckyGame.getProfile();
        this.draft     = new DeckDraft(profile);
        this.machineDraft = new MachineDraft(profile);
        this.stage     = new Stage(new MinimumScreenViewport(MIN_WIDTH, MIN_HEIGHT), luckyGame.getBatch());
        clickSound  = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(CLICK_SOUND)), audio);
        dealSound   = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(DEAL_SOUND)), audio);
        refuseSound = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(REFUSE_SOUND)), audio);
        upgradeSound = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(UPGRADE_SOUND)), audio);
        music       = new BackgroundMusic(MUSIC, audio, MUSIC_LEVEL);


        TextureRegionDrawable pixel = new TextureRegionDrawable(new TextureRegion(hud.pixel));
        veil = new Image(pixel);
        veil.setColor(0f, 0f, 0f, 0.65f);
        veil.setTouchable(Touchable.disabled);
        title = new Label(Lang.t("TABLE DU CROUPIER"), new Label.LabelStyle(titleFont, Color.WHITE));
        title.pack();
        collectionPanel = new Image(hud.panelDrawable());
        deckPanel       = new Image(hud.panelDrawable());
        collectionPanel.setTouchable(Touchable.disabled);
        deckPanel.setTouchable(Touchable.disabled);
        collectionHead = new Label(Lang.t("MA COLLECTION"), new Label.LabelStyle(headFont, Color.WHITE));
        deckHead       = new Label(Lang.t("MON DECK"), new Label.LabelStyle(headFont, Color.WHITE));
        counter        = new Label("", new Label.LabelStyle(counterFont, Color.WHITE));
        hint = new Label(Lang.t("Clic gauche : ajouter. Clic droit : retirer."), new Label.LabelStyle(hintFont, Color.WHITE));

        buildCollection();
        scroll = new ScrollPane(grid);
        scroll.setScrollingDisabled(true, false);
        scroll.setFadeScrollBars(false);
        scroll.setOverscroll(false, false);

        for (Symbol symbol : profile.getOwnedReels()) {
            reelTextures.put(symbol, new Texture(Gdx.files.internal(symbol.getAssetPath())));
            reelCells.add(buildReelCell(symbol));
        }
        reelScroll = new ScrollPane(reelGrid);
        reelScroll.setScrollingDisabled(true, false);
        reelScroll.setFadeScrollBars(false);
        reelScroll.setOverscroll(false, false);

        deckTab  = buttons.create(Lang.t("Deck"), clickSound, () -> showTab(false));
        reelsTab = buttons.create(Lang.t("Rouleaux"), clickSound, () -> showTab(true));
        saveButton    = buttons.createAction(Lang.t("Enregistrer"), clickSound, this::onSave);
        starterButton = buttons.create(Lang.t("Deck de départ"), clickSound, () -> { draft.resetToStarter(); refresh(); });
        classicButton = buttons.create(Lang.t("Rouleaux de départ"), clickSound, () -> { machineDraft.resetToClassic(); refresh(); });
        clearButton   = buttons.create(Lang.t("Vider"), clickSound, () -> {
            if (showingReels) machineDraft.clear(); else draft.clear();
            refresh();
        });
        backButton    = buttons.create(Lang.t("Retour"), clickSound, this::onBack);
        tutorialButton = buttons.create(Lang.t("Tutoriel"), clickSound, this::replayGuide);

        upgradeOverlay = new UpgradeOverlay(hud, buttons, clickSound, upgradeSound);

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
        stage.addActor(reelScroll);
        stage.addActor(machineList);
        stage.addActor(deckTab);
        stage.addActor(reelsTab);
        stage.addActor(classicButton);
        stage.addActor(hint);
        stage.addActor(saveButton);
        stage.addActor(starterButton);
        stage.addActor(clearButton);
        stage.addActor(backButton);
        stage.addActor(tutorialButton);
        stage.addActor(tooltip.getActor());
        stage.addActor(upgradeOverlay.getActor());
        stage.addActor(fade);
        fade.addAction(Actions.fadeOut(FADE_TIME));

        layout();
        showTab(false);
        firstVisit = new FirstVisitGuide(stage, hud, clickSound, profile, PlayerProfile.GUIDE_TABLE, this::guideSteps);
        fade.toFront();
    }

    /**
     * Le Croupier présente la table, puis fait manipuler le joueur : retirer
     * une carte du deck et en ajouter une, puis la même chose avec un rouleau.
     */
    private List<GuideOverlay.Step> guideSteps() {
        int[] before = new int[1];
        List<GuideOverlay.Step> steps = new ArrayList<>();
        steps.add(GuideOverlay.Step.say(Lang.t("La Table du croupier. Ici, tu prépares ce que tu emmènes au combat."))
            .onStart(() -> showTab(false)));
        steps.add(GuideOverlay.Step.say(Lang.t("À gauche, ta collection : toutes les cartes que tu possèdes."),
            () -> GuideOverlay.boundsOf(collectionPanel)));
        steps.add(GuideOverlay.Step.say(Lang.f("À droite, ton deck. Il fait {0} cartes, {1} exemplaires au plus de chacune. Tu le joues dans "
            + "tous les modes.",
            PlayerProfile.DECK_SIZE, PlayerProfile.MAX_COPIES),
            () -> GuideOverlay.boundsOf(deckPanel)));
        steps.add(GuideOverlay.Step.action(Lang.t("Retire une carte : clique sur une ligne de ton deck."),
                () -> GuideOverlay.boundsOf(deckPanel), () -> draft.size() < before[0])
            .onStart(() -> before[0] = draft.size()));
        steps.add(GuideOverlay.Step.action(Lang.t("Ajoute une carte : clic gauche sur une carte de ta collection. Clic droit l'enlève du deck."),
                () -> GuideOverlay.boundsOf(collectionPanel), () -> draft.size() > before[0])
            .onStart(() -> before[0] = draft.size()));
        steps.add(GuideOverlay.Step.say(Lang.f("Quand tu as {0} exemplaires d'une carte, FUSIONNER apparaît dessus : ils "
            + "deviennent une carte +, avec un liseré doré et des valeurs +50 %.", Card.UPGRADE_COST),
            () -> GuideOverlay.boundsOf(collectionPanel)));
        steps.add(GuideOverlay.Step.action(Lang.t("Ta machine a ses propres rouleaux. Ouvre l'onglet Rouleaux."),
            () -> GuideOverlay.boundsOf(reelsTab), () -> showingReels));
        steps.add(GuideOverlay.Step.say(Lang.f("Tes rouleaux. Ceux en or tournent dans ta machine. Elle en prend {0}, tous différents.",
            Symbol.MACHINE_SIZE), () -> GuideOverlay.boundsOf(collectionPanel)));
        steps.add(GuideOverlay.Step.action(Lang.t("Retire un rouleau : clique sur un rouleau en or, ou sur une ligne de ta machine."),
                () -> GuideOverlay.boundsOf(collectionPanel).merge(GuideOverlay.boundsOf(deckPanel)),
                () -> machineDraft.size() < before[0])
            .onStart(() -> before[0] = machineDraft.size()));
        steps.add(GuideOverlay.Step.action(Lang.t("Place un rouleau : clique sur un rouleau qui n'est pas en or."),
                () -> GuideOverlay.boundsOf(collectionPanel), () -> machineDraft.size() > before[0])
            .onStart(() -> before[0] = machineDraft.size()));
        steps.add(GuideOverlay.Step.say(Lang.t("Enregistre pour garder ton deck et ta machine. Retour les laisse comme avant."),
            () -> GuideOverlay.boundsOf(saveButton).merge(GuideOverlay.boundsOf(backButton))));
        return steps;
    }

    /** Bouton « Tutoriel » : le Croupier présente la table de nouveau. */
    private void replayGuide() {
        firstVisit.replay();
        fade.toFront();
    }

    /** Affiche l'onglet « Rouleaux » ({@code reels}) ou « Deck ». */
    private void showTab(boolean reels) {
        showingReels = reels;
        deckTab.setColor(reels ? USED_UP : Color.WHITE);
        reelsTab.setColor(reels ? Color.WHITE : USED_UP);
        scroll.setVisible(!reels);
        deckList.setVisible(!reels);
        starterButton.setVisible(!reels);
        reelScroll.setVisible(reels);
        machineList.setVisible(reels);
        classicButton.setVisible(reels);
        collectionHead.setText(reels ? Lang.t("MES ROULEAUX") : Lang.t("MA COLLECTION"));
        deckHead.setText(reels ? Lang.t("MA MACHINE") : Lang.t("MON DECK"));
        stage.setScrollFocus(reels ? reelScroll : scroll);
        refresh();
    }

    // -------------------------------------------------------------------------
    // Rouleaux
    // -------------------------------------------------------------------------

    /**
     * Case d'un rouleau possédé : son image et, dessous, son nom (en or s'il est
     * dans la machine). Un clic le place dans la machine ou l'en retire.
     */
    private Group buildReelCell(Symbol symbol) {
        Group cell = new Group();
        cell.setSize(REEL_WIDTH, REEL_HEIGHT + 34f);
        Group holder = new Group();
        holder.setSize(REEL_WIDTH, REEL_HEIGHT);
        holder.setPosition(0f, 34f);
        holder.setOrigin(Align.center);
        holder.setTransform(true);
        Image image = new Image(new TextureRegionDrawable(new TextureRegion(reelTextures.get(symbol))));
        image.setSize(REEL_WIDTH, REEL_HEIGHT);
        holder.addActor(image);
        Label state = new Label(symbol.getDisplayName(), new Label.LabelStyle(countFont, Color.WHITE));
        state.setAlignment(Align.center);
        state.setBounds(0f, 0f, REEL_WIDTH, 30f);
        cell.addActor(holder);
        cell.addActor(state);
        reelStates.put(symbol, state);

        cell.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                if (pointer != -1) return;
                holder.clearActions();
                holder.addAction(Actions.scaleTo(HOVER_SCALE, HOVER_SCALE, 0.1f, Interpolation.pow2Out));
                Vector2 pos = holder.localToStageCoordinates(new Vector2(0f, REEL_HEIGHT + 8f));
                tooltip.show(symbol.getDisplayName(), ReelShop.describe(symbol), pos.x, pos.y);
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
                if (machineDraft.contains(symbol)) {
                    machineDraft.remove(symbol);
                    clickSound.play();
                    refresh();
                } else {
                    addReel(symbol, holder);
                }
            }
        });
        return cell;
    }

    /** Place le rouleau {@code symbol} dans la machine, ou dit pourquoi ce n'est pas possible. */
    private void addReel(Symbol symbol, Actor feedback) {
        String problem = machineDraft.addProblem(symbol);
        if (problem != null) {
            refuse(problem, feedback);
            return;
        }
        machineDraft.add(symbol);
        dealSound.play();
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

        // « FUSIONNER », en bas de la carte, quand elle peut devenir une carte « + ».
        Group upgrade = new Group();
        upgrade.setBounds(0f, 0f, CARD_WIDTH, 34f);
        Image band = new Image(new TextureRegionDrawable(new TextureRegion(hud.pixel)));
        band.setColor(0.12f, 0.07f, 0.02f, 0.88f);
        band.setSize(CARD_WIDTH, 34f);
        Label upgradeText = new Label(Lang.t("FUSIONNER"), new Label.LabelStyle(upgradeFont, Color.WHITE));
        upgradeText.setColor(Palette.GOLD);
        upgradeText.setAlignment(Align.center);
        upgradeText.setBounds(0f, 2f, CARD_WIDTH, 30f);
        upgrade.addActor(band);
        upgrade.addActor(upgradeText);
        upgrade.setVisible(false);
        holder.addActor(upgrade);
        cellUpgrades.put(card.getId(), upgrade);

        ClickListener listener = new ClickListener(-1) {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                if (pointer != -1) return;
                holder.clearActions();
                holder.addAction(Actions.scaleTo(HOVER_SCALE, HOVER_SCALE, 0.1f, Interpolation.pow2Out));
                Vector2 pos = holder.localToStageCoordinates(new Vector2(0f, CARD_HEIGHT + 8f));
                tooltip.show(card.getName(), card.getDescription(), pos.x, pos.y);
                if (upgrade.isVisible()) upgradeText.setColor(Color.WHITE);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                if (pointer != -1) return;
                holder.clearActions();
                holder.addAction(Actions.scaleTo(1f, 1f, 0.1f, Interpolation.pow2Out));
                tooltip.hide();
                upgradeText.setColor(Palette.GOLD);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (upgrade.isVisible() && event.getTarget().isDescendantOf(upgrade)) openUpgrade(card);
                else if (event.getButton() == Input.Buttons.RIGHT) removeCard(card.getId());
                else addCard(card.getId(), holder);
            }
        };
        cell.addListener(listener);
        return cell;
    }

    /** Crée une case par carte de la collection (dans son ordre). */
    private void buildCollection() {
        cards.clear();
        cells.clear();
        cellCounts.clear();
        cellUpgrades.clear();
        for (String id : profile.getCollection().keySet()) cards.put(id, factory.apply(id));
        for (Card card : cards.values()) cells.add(buildCell(card));
        grid.clearChildren(); // la grille se refait à la mise en page
    }

    /** Ouvre la fenêtre de fusion de la carte {@code card} en sa version « + ». */
    private void openUpgrade(Card card) {
        String id = card.getId();
        Card plus = factory.apply(Card.upgradedId(id));
        tooltip.hide();
        upgradeOverlay.show(card, cardTextures.get(card), plus, cardTextures.get(plus), () -> {
            if (!profile.upgradeCard(id)) return;
            draft.afterUpgrade(id);
            buildCollection();
            refresh();
        }, () -> stage.setScrollFocus(scroll));
    }

    /** Ajoute un exemplaire de la carte {@code id} au deck, ou dit pourquoi ce n'est pas possible. */
    private void addCard(String id, Actor feedback) {
        String problem = draft.addProblem(id);
        if (problem != null) {
            refuse(problem, feedback);
            return;
        }
        draft.add(id);
        dealSound.play();
        refresh();
    }

    /** Refus : bruitage, explication sous la liste, et {@code feedback} qui tremble. */
    private void refuse(String problem, Actor feedback) {
        refuseSound.play();
        hint.setText(problem + ".");
        hint.setColor(REFUSED);
        feedback.clearActions();
        feedback.addAction(Actions.sequence(
            Actions.moveBy(-6f, 0f, 0.04f), Actions.moveBy(12f, 0f, 0.08f), Actions.moveBy(-6f, 0f, 0.04f),
            Actions.scaleTo(HOVER_SCALE, HOVER_SCALE, 0f)));
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
            cellUpgrades.get(id).setVisible(profile.upgradeProblem(id) == null);
        }
        int index = 0;
        for (String id : cards.keySet()) {
            // Les cartes dont tous les exemplaires sont dans le deck s'assombrissent.
            Group holder = (Group) cells.get(index++).getChildren().first();
            holder.setColor(draft.getCopies(id) >= draft.getMaxCopies(id) ? USED_UP : Color.WHITE);
        }

        refreshMachine();
        saveButton.setDisabled(!draft.isComplete() || !machineDraft.isComplete());
        hint.setColor(Color.WHITE);
        if (showingReels) {
            int reels = machineDraft.size();
            counter.setText(reels + " / " + Symbol.MACHINE_SIZE);
            counter.setColor(machineDraft.isComplete() ? Palette.GOLD : REFUSED);
            int missing = Symbol.MACHINE_SIZE - reels;
            hint.setText(machineDraft.isComplete() ? (draft.isComplete() ? Lang.t("La machine est prête.") : Lang.t("La machine est prête. Le deck n'est pas complet."))
                : Lang.f(Lang.plural(missing) ? "Encore {0} rouleaux à choisir." : "Encore {0} rouleau à choisir.", missing));
        } else {
            int size = draft.size();
            counter.setText(size + " / " + PlayerProfile.DECK_SIZE);
            counter.setColor(draft.isComplete() ? Palette.GOLD : REFUSED);
            hint.setText(draft.isComplete() ? (machineDraft.isComplete() ? Lang.t("Le deck est prêt.") : Lang.t("Le deck est prêt. La machine n'est pas complète."))
                : size < PlayerProfile.DECK_SIZE ? Lang.f(Lang.plural(PlayerProfile.DECK_SIZE - size)
                    ? "Encore {0} cartes à choisir." : "Encore {0} carte à choisir.", PlayerProfile.DECK_SIZE - size)
                : Lang.t("Clic droit : retirer une carte."));
        }

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

    /** Met à jour les cases des rouleaux (dans la machine ou non) et la liste de la machine. */
    private void refreshMachine() {
        for (Map.Entry<Symbol, Label> entry : reelStates.entrySet()) {
            boolean in = machineDraft.contains(entry.getKey());
            entry.getValue().setColor(in ? Palette.GOLD : USED_UP); // en or : dans la machine
        }
        int index = 0;
        for (Symbol symbol : profile.getOwnedReels()) {
            Group holder = (Group) reelCells.get(index++).getChildren().first();
            holder.getChildren().first().setColor(machineDraft.contains(symbol) ? Color.WHITE : USED_UP);
        }
        machineList.clearChildren();
        Label.LabelStyle style = new Label.LabelStyle(rowFont, Color.WHITE);
        for (Symbol symbol : machineDraft.getReels()) {
            Label row = new Label(symbol.getDisplayName() + "   " + ReelShop.describe(symbol), style);
            row.setEllipsis(true);
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
                public void clicked(InputEvent event, float x, float y) {
                    machineDraft.remove(symbol);
                    clickSound.play();
                    refresh();
                }
            });
            machineList.add(row).left().width(DECK_WIDTH - PANEL_PAD * 2).height(30f).row();
        }
    }

    /** Enregistre le deck et la machine (complets) et revient au menu principal. */
    private void onSave() {
        if (leaving || !draft.isComplete() || !machineDraft.isComplete() || !draft.save() || !machineDraft.save()) return;
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
        title.setPosition(MARGIN, height - TITLE_TOP - title.getHeight() / 2f);
        reelsTab.setPosition(width - MARGIN - reelsTab.getWidth(), height - TITLE_TOP - reelsTab.getHeight() / 2f);
        deckTab.setPosition(reelsTab.getX() - 20f - deckTab.getWidth(), reelsTab.getY());

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
        int newReelColumns = Math.max(1, (int) ((gridWidth + CELL_GAP) / (REEL_WIDTH + CELL_GAP)));
        if (newReelColumns != reelColumns || reelGrid.getChildren().isEmpty()) {
            reelColumns = newReelColumns;
            reelGrid.clearChildren();
            for (int i = 0; i < reelCells.size(); i++) {
                reelGrid.add(reelCells.get(i)).size(REEL_WIDTH, REEL_HEIGHT + 34f).pad(CELL_GAP / 2f);
                if ((i + 1) % reelColumns == 0) reelGrid.row();
            }
        }
        reelGrid.top().left();
        reelScroll.setBounds(scroll.getX(), scroll.getY(), scroll.getWidth(), scroll.getHeight());
        reelScroll.layout();

        deckHead.pack();
        deckHead.setPosition(deckX + PANEL_PAD, panelTop - PANEL_PAD - deckHead.getHeight());
        counter.pack();
        counter.setPosition(deckX + DECK_WIDTH - PANEL_PAD - counter.getWidth(),
            deckHead.getY() + (deckHead.getHeight() - counter.getHeight()) / 2f);
        deckList.pack();
        deckList.setPosition(deckX + PANEL_PAD, deckHead.getY() - 20f - deckList.getHeight());
        machineList.pack();
        machineList.setPosition(deckX + PANEL_PAD, deckHead.getY() - 20f - machineList.getHeight());
        hint.setWrap(true);
        hint.setWidth(DECK_WIDTH - PANEL_PAD * 2);
        hint.setHeight(hint.getPrefHeight());
        hint.setPosition(deckX + PANEL_PAD, panelBottom + PANEL_PAD);

        float buttonY = (BOTTOM_SPACE - saveButton.getHeight()) / 2f;
        saveButton.setPosition(width - MARGIN - saveButton.getWidth(), buttonY);
        clearButton.setPosition(saveButton.getX() - 24f - clearButton.getWidth(), buttonY);
        starterButton.setPosition(clearButton.getX() - 24f - starterButton.getWidth(), buttonY);
        classicButton.setPosition(clearButton.getX() - 24f - classicButton.getWidth(), buttonY);
        backButton.setPosition(MARGIN, buttonY);
        tutorialButton.setPosition(backButton.getX() + backButton.getWidth() + 20f, buttonY);
        upgradeOverlay.layout(width, height);
    }

    /** Stage d'abord (souris), puis clavier : Échap revient au menu principal sans enregistrer. */
    @Override
    public void show() {
        InputAdapter keyboard = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (leaving || keycode != Input.Keys.ESCAPE) return false;
                clickSound.play();
                if (upgradeOverlay.isShown()) upgradeOverlay.hide();
                else onBack();
                return true;
            }
        };
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, keyboard));
        stage.setScrollFocus(showingReels ? reelScroll : scroll);
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
        firstVisit.dispose();
        decor.dispose();
        cardTextures.dispose();
        reelTextures.values().forEach(Texture::dispose);
        buttons.dispose();
        tooltip.dispose();
        clickSound.dispose();
        dealSound.dispose();
        refuseSound.dispose();
        upgradeSound.dispose();
        upgradeOverlay.dispose();
        music.dispose();
        hud.dispose();
        Fonts.release(titleFont);
        Fonts.release(headFont);
        Fonts.release(counterFont);
        Fonts.release(countFont);
        Fonts.release(rowFont);
        Fonts.release(hintFont);
        Fonts.release(upgradeFont);
    }
}
