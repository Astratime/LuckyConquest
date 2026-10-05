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
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
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
import fr.astratime.lucky.loaders.CardLoader;
import fr.astratime.lucky.progress.PlayerProfile;
import fr.astratime.lucky.progress.Rank;
import fr.astratime.lucky.progress.ReelShop;
import fr.astratime.lucky.settings.AudioSettings;
import fr.astratime.lucky.views.CardDetailOverlay;
import fr.astratime.lucky.views.CasinoButtons;
import fr.astratime.lucky.views.MenuDecor;
import fr.astratime.lucky.views.MinimumScreenViewport;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Boutique : le joueur y dépense les pièces gagnées dans les donjons de
 * l'Exploration (voir {@link PlayerProfile#getCoins()}). Trois onglets :
 * <ul>
 *   <li>« Rang » : les {@link Rank rangs}, achetés dans l'ordre, qui augmentent
 *       les PV et les rouleaux du joueur ;</li>
 *   <li>« Cartes » : des exemplaires de cartes (cards/decks/boutique.json), qui
 *       rejoignent sa collection, puis la Table du croupier ;</li>
 *   <li>« Rouleau » : de nouveaux rouleaux ({@link ReelShop}), à placer dans sa
 *       machine à la Table du croupier.</li>
 * </ul>
 * Un clic choisit un article ; « Acheter » le paie. Échap ou « Retour » ramène au menu principal.
 */
public class ShopScreen extends ScreenAdapter {

    /** Onglets de la boutique. */
    enum Tab { RANG("Rang"), CARTES("Cartes"), ROULEAU("Rouleau");
        final String label;
        Tab(String label) { this.label = label; }
    }

    private static final String CLICK_SOUND    = "sounds/button-click.ogg";
    private static final String PURCHASE_SOUND = "sounds/shop/purchase.ogg";
    private static final String REFUSE_SOUND   = "sounds/coins/loss.ogg";
    private static final String MUSIC          = "music/main_menu.ogg";
    private static final float  MUSIC_LEVEL    = 0.3f;

    private static final float MIN_WIDTH    = 1600f;
    private static final float MIN_HEIGHT   = 1080f;
    private static final float MARGIN       = 60f;
    private static final float TITLE_TOP    = 90f;
    private static final float TABS_TOP     = 175f;
    private static final float PANEL_TOP    = 220f;
    private static final float BOTTOM_SPACE = 190f;
    private static final float PANEL_PAD    = 30f;
    private static final float COIN_SIZE    = 64f;
    private static final float CARD_WIDTH   = 110f;
    private static final float CARD_HEIGHT  = 154f;   // images 480 x 672
    private static final float REEL_WIDTH   = 144f;
    private static final float REEL_HEIGHT  = 120f;
    private static final float CELL_GAP     = 22f;
    private static final float HOVER_SCALE  = 1.06f;
    private static final float FADE_TIME    = 0.4f;
    private static final Color SELECTED     = Palette.GOLD;
    private static final Color DIMMED       = new Color(0.55f, 0.55f, 0.55f, 1f);
    private static final Color REFUSED      = new Color(1f, 0.45f, 0.45f, 1f);

    private final LuckyGame       luckyGame;
    private final PlayerProfile   profile;
    private final Stage           stage;
    private final AudioSettings   audio   = new AudioSettings();
    private final HudTextures     hud     = new HudTextures();
    /** Première visite : le Croupier présente l'écran. */
    private final FirstVisitGuide firstVisit;
    private final CardTextures    cardTextures = new CardTextures();
    private final CasinoButtons   buttons = new CasinoButtons();
    private final MenuDecor       decor   = new MenuDecor();
    private final Sound           clickSound;
    private final Sound           purchaseSound;
    private final Sound           refuseSound;
    private final BackgroundMusic music;
    private final BitmapFont      titleFont = Fonts.jersey(84, Palette.GOLD, 6f, Palette.TEXT_SHADE);
    private final BitmapFont      coinsFont = Fonts.jersey(52, Palette.GOLD, 4f, Palette.TEXT_SHADE);
    private final BitmapFont      headFont  = Fonts.jersey(40, Palette.GOLD, 3f, Palette.TEXT_SHADE);
    private final BitmapFont      bodyFont  = Fonts.jersey(28, Palette.CREAM, 2f, Palette.TEXT_SHADE);
    private final BitmapFont      smallFont = Fonts.jersey(24, Color.WHITE, 2f, Palette.TEXT_SHADE);

    /** Images des rouleaux. */
    private final Map<Symbol, Texture> reelTextures = new EnumMap<>(Symbol.class);
    /** Cartes en vente, une instance de chaque, et leur prix. */
    private final List<Card> cards = new ArrayList<>();
    private final Map<String, Long> cardPrices;

    private final Image      veil;
    private final Label      title;
    private final Image      coin;
    private final Label      coins;
    private final List<TextButton> tabButtons = new ArrayList<>();
    private final Image      panel;
    private final Table      content = new Table();
    private final ScrollPane scroll;
    private final Label      detailTitle;
    private final Label      detailText;
    private final TextButton buyButton;
    private final TextButton backButton;
    private final Image      fade;
    /** Fiche détaillée d'une carte ou d'un rouleau (clic droit). */
    private final CardDetailOverlay detail;

    private Tab     tab = Tab.RANG;
    /** Article choisi, à acheter avec « Acheter » ({@code null} : aucun). */
    private Offer   selected;
    /** Acteur de l'article choisi, mis en valeur. */
    private Actor   selectedActor;
    private boolean leaving;

    /**
     * Article de la boutique.
     *
     * @param name      nom affiché
     * @param text      description et état (déjà possédé…)
     * @param price     prix, en pièces
     * @param blocked   pourquoi il ne peut pas être acheté (hors manque de pièces), ou {@code null}
     * @param purchase  achète l'article (vrai si l'achat a eu lieu)
     */
    private record Offer(String name, String text, long price, Supplier<String> blocked, BooleanSupplier purchase) { }

    public ShopScreen(LuckyGame luckyGame) {
        this.luckyGame = luckyGame;
        this.profile   = luckyGame.getProfile();
        this.stage     = new Stage(new MinimumScreenViewport(MIN_WIDTH, MIN_HEIGHT), luckyGame.getBatch());
        clickSound    = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(CLICK_SOUND)), audio);
        purchaseSound = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(PURCHASE_SOUND)), audio);
        refuseSound   = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(REFUSE_SOUND)), audio);
        music         = new BackgroundMusic(MUSIC, audio, MUSIC_LEVEL);

        for (Symbol symbol : ReelShop.getPrices().keySet()) {
            reelTextures.put(symbol, new Texture(Gdx.files.internal(symbol.getAssetPath())));
        }
        cardPrices = CardLoader.loadBoutique();
        Function<String, Card> factory = CardLoader.cardFactory();
        for (String id : cardPrices.keySet()) cards.add(factory.apply(id));

        TextureRegionDrawable pixel = new TextureRegionDrawable(new TextureRegion(hud.pixel));
        veil = new Image(pixel);
        veil.setColor(0f, 0f, 0f, 0.65f);
        veil.setTouchable(Touchable.disabled);
        title = new Label("BOUTIQUE", new Label.LabelStyle(titleFont, Color.WHITE));
        title.pack();
        coin = new Image(new TextureRegionDrawable(new TextureRegion(hud.coin)));
        coin.setSize(COIN_SIZE, COIN_SIZE);
        coins = new Label("", new Label.LabelStyle(coinsFont, Color.WHITE));
        for (Tab each : Tab.values()) {
            TextButton button = buttons.create(each.label, clickSound, () -> showTab(each));
            tabButtons.add(button);
        }
        panel = new Image(hud.panelDrawable());
        panel.setTouchable(Touchable.disabled);
        scroll = new ScrollPane(content);
        scroll.setScrollingDisabled(true, false);
        scroll.setFadeScrollBars(false);
        scroll.setOverscroll(false, false);
        detailTitle = new Label("", new Label.LabelStyle(headFont, Color.WHITE));
        detailText  = new Label("", new Label.LabelStyle(bodyFont, Color.WHITE));
        detailText.setWrap(true);
        buyButton  = buttons.createAction("Acheter", clickSound, this::onBuy);
        backButton = buttons.create("Retour", clickSound, this::onBack);

        fade = new Image(pixel);
        fade.setColor(Color.BLACK);
        fade.setTouchable(Touchable.disabled);

        stage.addActor(decor);
        stage.addActor(veil);
        stage.addActor(title);
        stage.addActor(coin);
        stage.addActor(coins);
        tabButtons.forEach(stage::addActor);
        stage.addActor(panel);
        stage.addActor(scroll);
        stage.addActor(detailTitle);
        stage.addActor(detailText);
        stage.addActor(buyButton);
        stage.addActor(backButton);
        detail = new CardDetailOverlay(stage, hud, null, CARD_WIDTH, CARD_HEIGHT);
        stage.addActor(detail.getActor());
        stage.addActor(fade);
        fade.addAction(Actions.fadeOut(FADE_TIME));

        showTab(Tab.RANG);
        firstVisit = new FirstVisitGuide(stage, hud, clickSound, luckyGame.getProfile(), PlayerProfile.GUIDE_SHOP,
            "La Boutique. Tes pièces y achètent des rangs, des cartes et des rouleaux.",
            "Un rang te rend plus fort dans tous les combats. Une carte rejoint ta collection.",
            "Un rouleau se place dans ta machine, à la Table du croupier.");
        fade.toFront();
    }

    // -------------------------------------------------------------------------
    // Onglets
    // -------------------------------------------------------------------------

    /** Affiche l'onglet {@code shown} : son contenu, et l'onglet mis en valeur. */
    private void showTab(Tab shown) {
        tab = shown;
        for (int i = 0; i < tabButtons.size(); i++) {
            tabButtons.get(i).setColor(Tab.values()[i] == shown ? Color.WHITE : DIMMED);
        }
        select(null, null);
        content.clearChildren();
        switch (shown) {
            case RANG    -> buildRanks();
            case CARTES  -> buildGrid(cardCells());
            case ROULEAU -> buildGrid(reelCells());
        }
        refreshCoins();
        layout();
        scroll.setScrollY(0f);
    }

    /** Onglet « Rang » : une ligne par rang ; seul le suivant peut être choisi. */
    private void buildRanks() {
        content.top().left();
        Rank current = profile.getRank(), next = profile.getNextRank();
        Label header = new Label(current == null ? "Tu n'as pas encore de rang." : "Ton rang : " + current.getTitle(),
            new Label.LabelStyle(headFont, Color.WHITE));
        content.add(header).left().colspan(3).padBottom(16f).row();
        for (Rank rank : Rank.values()) {
            boolean owned = current != null && rank.ordinal() <= current.ordinal();
            Table row = new Table();
            row.setTouchable(Touchable.enabled);
            row.setBackground(hud.insetDrawable());
            row.pad(14f, 22f, 14f, 22f);
            Label name = new Label(rank.getTitle(), new Label.LabelStyle(headFont, Color.WHITE));
            Label text = new Label(rank.getDescription() + "\n" + rank.getBonusText(),
                new Label.LabelStyle(bodyFont, Color.WHITE));
            text.setWrap(true);
            Label state = new Label(owned ? "Acquis" : PlayerProfile.formatCoins(rank.getPrice()) + " pièces",
                new Label.LabelStyle(bodyFont, Color.WHITE));
            state.setAlignment(Align.right);
            row.add(name).width(330f).left();
            row.add(text).growX().left().padRight(20f);
            row.add(state).width(230f).right();
            if (owned) row.setColor(SELECTED);
            else if (rank != next) row.setColor(DIMMED);
            content.add(row).growX().padBottom(10f).row();
            if (rank == next) {
                Offer offer = new Offer(rank.getTitle(), rank.getDescription() + " " + rank.getBonusText(),
                    rank.getPrice(), () -> null, profile::buyNextRank);
                row.addListener(selectOnClick(offer, row));
                select(offer, row);
            }
        }
        if (next == null) showDetail("Tous les rangs sont acquis.", "Tu es une légende. Les tables murmurent ton nom.");
    }

    /** Onglet « Cartes » : chaque carte en vente, son prix et les exemplaires possédés. */
    private List<Group> cardCells() {
        List<Group> cells = new ArrayList<>();
        for (Card card : cards) {
            long price = cardPrices.get(card.getId());
            int owned = profile.getOwnedCopies(card.getId());
            Offer offer = new Offer(card.getName(), card.getDescription() + "\nPossédées : " + owned + " / "
                + PlayerProfile.MAX_COPIES + ".", price,
                () -> profile.getOwnedCopies(card.getId()) >= PlayerProfile.MAX_COPIES
                    ? "Tu as déjà " + PlayerProfile.MAX_COPIES + " exemplaires." : null,
                () -> profile.buyCard(card.getId(), price));
            cells.add(cell(new TextureRegion(cardTextures.get(card)), CARD_WIDTH, CARD_HEIGHT,
                PlayerProfile.formatCoins(price), owned + " / " + PlayerProfile.MAX_COPIES,
                owned >= PlayerProfile.MAX_COPIES, offer,
                () -> detail.showForSale(card, cardTextures.get(card), PlayerProfile.formatCoins(price) + " pièces")));
        }
        return cells;
    }

    /** Onglet « Rouleau » : chaque rouleau en vente, son effet et son prix. */
    private List<Group> reelCells() {
        List<Group> cells = new ArrayList<>();
        for (Map.Entry<Symbol, Long> entry : ReelShop.getPrices().entrySet()) {
            Symbol symbol = entry.getKey();
            long price = entry.getValue();
            boolean owned = profile.ownsReel(symbol);
            Offer offer = new Offer(symbol.getDisplayName(), ReelShop.describe(symbol)
                + (owned ? "\nDéjà dans ta collection de rouleaux." : "\nÀ placer dans ta machine, à la Table du croupier."),
                price, () -> profile.ownsReel(symbol) ? "Tu possèdes déjà ce rouleau." : null,
                () -> profile.buyReel(symbol, price));
            cells.add(cell(new TextureRegion(reelTextures.get(symbol)), REEL_WIDTH, REEL_HEIGHT,
                PlayerProfile.formatCoins(price), owned ? "Possédé" : symbol.getDisplayName(), owned, offer,
                () -> detail.showReel(symbol.getDisplayName(), new TextureRegion(reelTextures.get(symbol)),
                    ReelShop.describe(symbol), PlayerProfile.formatCoins(price) + " pièces",
                    profile.ownsReel(symbol) ? "Possédé" : "Pas encore acheté")));
        }
        return cells;
    }

    /** Case d'un article : son image, son prix et une ligne d'état. Un clic le choisit, un clic droit montre sa fiche. */
    private Group cell(TextureRegion region, float width, float height, String price, String state, boolean owned,
                       Offer offer, Runnable onInspect) {
        Group cell = new Group();
        cell.setSize(Math.max(width, 150f), height + 64f);
        Group holder = new Group();
        holder.setSize(width, height);
        holder.setPosition((cell.getWidth() - width) / 2f, 64f);
        holder.setOrigin(Align.center);
        holder.setTransform(true);
        Image image = new Image(new TextureRegionDrawable(region));
        image.setSize(width, height);
        holder.addActor(image);
        if (owned) holder.setColor(DIMMED);
        Label priceLabel = new Label(price, new Label.LabelStyle(smallFont, Palette.GOLD));
        priceLabel.setAlignment(Align.center);
        priceLabel.setBounds(0f, 32f, cell.getWidth(), 28f);
        Label stateLabel = new Label(state, new Label.LabelStyle(smallFont, Color.WHITE));
        stateLabel.setAlignment(Align.center);
        stateLabel.setBounds(0f, 2f, cell.getWidth(), 28f);
        cell.addActor(holder);
        cell.addActor(priceLabel);
        cell.addActor(stateLabel);
        cell.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button != Input.Buttons.RIGHT) return false;
                clickSound.play();
                select(offer, priceLabel);
                onInspect.run();
                return true;
            }
        });
        cell.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                if (pointer != -1) return;
                holder.clearActions();
                holder.addAction(Actions.scaleTo(HOVER_SCALE, HOVER_SCALE, 0.1f, Interpolation.pow2Out));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                if (pointer != -1) return;
                holder.clearActions();
                holder.addAction(Actions.scaleTo(1f, 1f, 0.1f, Interpolation.pow2Out));
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                select(offer, priceLabel);
            }
        });
        return cell;
    }

    private ClickListener selectOnClick(Offer offer, Actor actor) {
        return new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                select(offer, actor);
            }
        };
    }

    /** Range les cases en grille, autant par ligne que la largeur du panneau le permet. */
    private void buildGrid(List<Group> cells) {
        content.top().left();
        float width = gridWidth();
        float cellWidth = cells.isEmpty() ? 1f : cells.get(0).getWidth();
        int columns = Math.max(1, (int) ((width + CELL_GAP) / (cellWidth + CELL_GAP)));
        for (int i = 0; i < cells.size(); i++) {
            Group cell = cells.get(i);
            content.add(cell).size(cell.getWidth(), cell.getHeight()).pad(CELL_GAP / 2f);
            if ((i + 1) % columns == 0) content.row();
        }
    }

    // -------------------------------------------------------------------------
    // Achat
    // -------------------------------------------------------------------------

    /** Choisit {@code offer} (mis en valeur par {@code actor}) et montre son détail. */
    private void select(Offer offer, Actor actor) {
        if (selectedActor != null && selectedActor instanceof Label label) label.setColor(Palette.GOLD);
        selected      = offer;
        selectedActor = actor;
        if (actor instanceof Label label) label.setColor(Color.WHITE);
        if (offer == null) {
            showDetail(tab == Tab.RANG ? "" : "Choisis un article.",
                tab == Tab.CARTES ? "Les cartes achetées rejoignent ta collection, puis la Table du croupier."
                    : tab == Tab.ROULEAU ? "Les rouleaux achetés se placent dans ta machine, à la Table du croupier."
                    : "");
            buyButton.setDisabled(true);
            return;
        }
        showDetail(offer.name() + "  -  " + PlayerProfile.formatCoins(offer.price()) + " pièces", offer.text());
        buyButton.setDisabled(offer.blocked().get() != null);
    }

    private void showDetail(String heading, String text) {
        detailTitle.setText(heading);
        detailTitle.setColor(Color.WHITE);
        detailText.setText(text);
        detailText.setColor(Color.WHITE);
        layout();
    }

    /** Paie l'article choisi, ou dit pourquoi ce n'est pas possible. */
    private void onBuy() {
        if (selected == null || leaving) return;
        String blocked = selected.blocked().get();
        if (blocked == null && profile.getCoins() < selected.price()) {
            blocked = "Il te manque " + PlayerProfile.formatCoins(selected.price() - profile.getCoins()) + " pièces.";
        }
        if (blocked != null || !selected.purchase().getAsBoolean()) {
            refuseSound.play();
            detailText.setText(blocked != null ? blocked : "Achat impossible.");
            detailText.setColor(REFUSED);
            return;
        }
        purchaseSound.play();
        String name = selected.name();
        float scroll = this.scroll.getScrollY();
        showTab(tab); // met à jour les états (possédé, rang suivant…)
        this.scroll.layout();
        this.scroll.setScrollY(scroll);
        showDetail(name + " : acheté !", tab == Tab.RANG ? "Ton nouveau rang vaut dans tous les combats."
            : tab == Tab.CARTES ? "La carte t'attend à la Table du croupier."
            : "Place-le dans ta machine, à la Table du croupier.");
        detailTitle.setColor(Palette.GOLD);
        coin.clearActions();
        coin.addAction(Actions.sequence(Actions.scaleTo(1.2f, 1.2f, 0.08f), Actions.scaleTo(1f, 1f, 0.15f)));
    }

    private void refreshCoins() {
        long amount = profile.getCoins();
        coins.setText(PlayerProfile.formatCoins(amount) + (amount > 1 ? " pièces" : " pièce"));
        coins.pack();
    }

    /** Retour au menu principal. */
    private void onBack() {
        if (leaving) return;
        leaving = true;
        fade.clearActions();
        fade.addAction(Actions.sequence(Actions.fadeIn(FADE_TIME), Actions.run(() -> Gdx.app.postRunnable(() -> {
            luckyGame.setScreen(new MenuScreen(luckyGame));
            dispose();
        }))));
    }

    // -------------------------------------------------------------------------
    // Mise en page et cycle de vie
    // -------------------------------------------------------------------------

    private float gridWidth() {
        return stage.getViewport().getWorldWidth() - MARGIN * 2 - PANEL_PAD * 2;
    }

    private void layout() {
        float width  = stage.getViewport().getWorldWidth();
        float height = stage.getViewport().getWorldHeight();
        decor.layout(width, height);
        decor.setPosition(0f, 0f);
        veil.setBounds(0f, 0f, width, height);
        fade.setBounds(0f, 0f, width, height);
        title.setPosition(MARGIN, height - TITLE_TOP - title.getHeight() / 2f);

        coins.pack();
        coins.setPosition(width - MARGIN - coins.getWidth(), height - TITLE_TOP - coins.getHeight() / 2f);
        coin.setOrigin(Align.center);
        coin.setPosition(coins.getX() - 14f - COIN_SIZE, height - TITLE_TOP - COIN_SIZE / 2f);

        float tabsWidth = 0f;
        for (TextButton button : tabButtons) tabsWidth += button.getWidth() + 20f;
        float x = (width - tabsWidth + 20f) / 2f;
        for (TextButton button : tabButtons) {
            button.setPosition(x, height - TABS_TOP - button.getHeight() / 2f);
            x += button.getWidth() + 20f;
        }

        float panelTop = height - PANEL_TOP, panelBottom = BOTTOM_SPACE;
        panel.setBounds(MARGIN, panelBottom, width - MARGIN * 2, panelTop - panelBottom);
        scroll.setBounds(MARGIN + PANEL_PAD, panelBottom + PANEL_PAD, gridWidth(), panelTop - panelBottom - PANEL_PAD * 2);
        scroll.layout();

        float buttonY = (BOTTOM_SPACE - CasinoButtons.HEIGHT) / 2f;
        backButton.setPosition(MARGIN, buttonY);
        buyButton.setPosition(width - MARGIN - buyButton.getWidth(), buttonY);
        float detailX = backButton.getX() + backButton.getWidth() + 40f;
        float detailWidth = buyButton.getX() - 40f - detailX;
        detailTitle.pack();
        detailTitle.setPosition(detailX, BOTTOM_SPACE - 18f - detailTitle.getHeight());
        detailText.setWidth(detailWidth);
        detailText.setHeight(detailText.getPrefHeight());
        detailText.setPosition(detailX, detailTitle.getY() - 6f - detailText.getHeight());
    }

    @Override
    public void show() {
        InputAdapter keyboard = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (leaving || keycode != Input.Keys.ESCAPE) return false;
                clickSound.play();
                if (detail.isShown()) {
                    detail.hide();
                    return true;
                }
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
        if (tab != Tab.RANG) showTab(tab); // la grille suit la largeur
        layout();
        detail.layout();
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
        clickSound.dispose();
        purchaseSound.dispose();
        refuseSound.dispose();
        music.dispose();
        detail.dispose();
        hud.dispose();
        Fonts.release(titleFont);
        Fonts.release(coinsFont);
        Fonts.release(headFont);
        Fonts.release(bodyFont);
        Fonts.release(smallFont);
    }
}
