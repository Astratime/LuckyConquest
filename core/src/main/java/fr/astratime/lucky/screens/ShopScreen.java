package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
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
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.assets.CardTextures;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.assets.VolumeSound;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.loaders.CardLoader;
import fr.astratime.lucky.progress.Cosmetic;
import fr.astratime.lucky.progress.PlayerProfile;
import fr.astratime.lucky.progress.Rank;
import fr.astratime.lucky.progress.ReelShop;
import fr.astratime.lucky.views.CardDetailOverlay;
import fr.astratime.lucky.views.CasinoButtons;
import fr.astratime.lucky.views.CosmeticPreview;
import fr.astratime.lucky.views.GuideOverlay;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Boutique : le joueur y dépense les pièces gagnées dans les donjons de
 * l'Exploration (voir {@link PlayerProfile#getCoins()}). Quatre onglets :
 * <ul>
 *   <li>« Rang » : les {@link Rank rangs}, achetés dans l'ordre, qui augmentent
 *       les PV et les rouleaux du joueur ;</li>
 *   <li>« Cartes » : des exemplaires de cartes (cards/decks/boutique.json), qui
 *       rejoignent sa collection, puis la Table du croupier ;</li>
 *   <li>« Rouleau » : de nouveaux rouleaux ({@link ReelShop}), à placer dans sa
 *       machine à la Table du croupier ;</li>
 *   <li>« Cosmétiques » : dos de cartes, tapis et machine ({@link Cosmetic}), sans
 *       effet en combat ; certains se débloquent par les succès ou le bestiaire,
 *       les autres s'achètent. Un cosmétique possédé s'habille avec « Choisir ».</li>
 * </ul>
 * Un clic choisit un article ; « Acheter » le paie. Échap ou « Retour » ramène au menu principal.
 */
public class ShopScreen extends CasinoMenuScreen {

    /** Onglets de la boutique. */
    enum Tab { RANG("Rang"), CARTES("Cartes"), ROULEAU("Rouleau"), COSMETIQUES("Cosmétiques");
        final String label;
        Tab(String label) { this.label = label; }
    }

    private static final String PURCHASE_SOUND = "sounds/shop/purchase.ogg";
    private static final String REFUSE_SOUND   = "sounds/coins/loss.ogg";

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
    private static final float COSMETIC_SIZE = 120f;
    /** Largeur du nom de la sorte, à gauche de sa ligne de cosmétiques. */
    private static final float COSMETIC_HEAD = 280f;
    private static final float CELL_GAP     = 22f;
    private static final float HOVER_SCALE  = 1.06f;
    private static final Color SELECTED     = Palette.GOLD;
    private static final Color DIMMED       = new Color(0.55f, 0.55f, 0.55f, 1f);
    private static final Color REFUSED      = new Color(1f, 0.45f, 0.45f, 1f);

    private final PlayerProfile   profile;
    /** Première visite : le Croupier présente l'écran. */
    private final FirstVisitGuide firstVisit;
    private final CardTextures    cardTextures = assets.cardTextures();
    private final Sound           purchaseSound;
    private final Sound           refuseSound;
    private final BitmapFont      coinsFont = Fonts.jersey(52, Palette.GOLD, 4f, Palette.TEXT_SHADE);
    private final BitmapFont      headFont  = Fonts.jersey(40, Palette.GOLD, 3f, Palette.TEXT_SHADE);
    private final BitmapFont      bodyFont  = Fonts.jersey(28, Palette.CREAM, 2f, Palette.TEXT_SHADE);
    private final BitmapFont      smallFont = Fonts.jersey(24, Color.WHITE, 2f, Palette.TEXT_SHADE);

    /** Images des rouleaux. */
    private final Map<Symbol, Texture> reelTextures = new EnumMap<>(Symbol.class);
    /** Cartes en vente, une instance de chaque, et leur prix. */
    private final List<Card> cards = new ArrayList<>();
    private final Map<String, Long> cardPrices;

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
    /** Rejoue la présentation du Croupier. */
    private final TextButton tutorialButton;
    /** Fiche détaillée d'une carte ou d'un rouleau (clic droit). */
    private final CardDetailOverlay detail;

    private Tab     tab = Tab.RANG;
    /** Article choisi, à acheter avec « Acheter » ({@code null} : aucun). */
    private Offer   selected;
    /** Acteur de l'article choisi, mis en valeur. */
    private Actor   selectedActor;
    /** Cosmétique choisi dans l'onglet « Cosmétiques » ({@code null} : aucun) : « Acheter » ou « Choisir ». */
    private Cosmetic selectedCosmetic;
    /** Aperçus des cosmétiques (leurs images). */
    private final CosmeticPreview cosmeticPreview = new CosmeticPreview();
    /** Ligne d'état de chaque cosmétique affiché, pour le choisir de nouveau après un achat. */
    private final Map<Cosmetic, Label> cosmeticLabels = new EnumMap<>(Cosmetic.class);
    /** Cadre doré autour de l'aperçu du cosmétique choisi. */
    private Image cosmeticFrame;

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
        super(luckyGame, Lang.t("BOUTIQUE"), 84, 0.65f);
        this.profile   = luckyGame.getProfile();
        purchaseSound = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(PURCHASE_SOUND)), assets.audio);
        refuseSound   = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(REFUSE_SOUND)), assets.audio);

        for (Symbol symbol : ReelShop.getPrices().keySet()) {
            reelTextures.put(symbol, new Texture(Gdx.files.internal(symbol.getAssetPath())));
        }
        cardPrices = CardLoader.loadBoutique();
        Function<String, Card> factory = CardLoader.cardFactory();
        for (String id : cardPrices.keySet()) cards.add(factory.apply(id));
        TextureRegionDrawable pixel = new TextureRegionDrawable(new TextureRegion(hud.pixel));
        coin = new Image(new TextureRegionDrawable(new TextureRegion(hud.coin)));
        coin.setSize(COIN_SIZE, COIN_SIZE);
        coins = new Label("", new Label.LabelStyle(coinsFont, Color.WHITE));
        for (Tab each : Tab.values()) {
            TextButton button = buttons.create(Lang.t(each.label), clickSound, () -> showTab(each));
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
        buyButton  = buttons.createAction(Lang.t("Acheter"), clickSound, this::onBuy);
        backButton = buttons.create(Lang.t("Retour"), clickSound, this::onBack);
        tutorialButton = buttons.create(Lang.t("Tutoriel"), clickSound, this::replayGuide);
        stage.addActor(coin);
        stage.addActor(coins);
        tabButtons.forEach(stage::addActor);
        stage.addActor(panel);
        stage.addActor(scroll);
        stage.addActor(detailTitle);
        stage.addActor(detailText);
        stage.addActor(buyButton);
        stage.addActor(backButton);
        stage.addActor(tutorialButton);
        detail = new CardDetailOverlay(stage, hud, null, CARD_WIDTH, CARD_HEIGHT);
        stage.addActor(detail.getActor());
        reveal();

        showTab(Tab.RANG);
        firstVisit = new FirstVisitGuide(stage, hud, clickSound, profile, PlayerProfile.GUIDE_SHOP, this::guideSteps);
        fade.toFront();
    }

    /**
     * Le Croupier présente la boutique et ses trois onglets, et fait choisir un
     * article au joueur (sans l'acheter : ses pièces restent à lui).
     */
    private List<GuideOverlay.Step> guideSteps() {
        List<GuideOverlay.Step> steps = new ArrayList<>();
        steps.add(GuideOverlay.Step.say(Lang.t("La Boutique. Tes pièces s'y dépensent. Seuls les combats d'Exploration en rapportent."),
            () -> GuideOverlay.boundsOf(coin).merge(GuideOverlay.boundsOf(coins))).onStart(() -> showTab(Tab.RANG)));
        steps.add(GuideOverlay.Step.say(Lang.t("Quatre onglets : Rang, Cartes, Rouleau et Cosmétiques."), this::tabsBounds));
        steps.add(GuideOverlay.Step.say(Lang.t("Onglet Rang. Un rang te rend plus fort dans tous les combats. Ils s'achètent dans l'ordre ; "
            + "chacun remplace le précédent."), () -> GuideOverlay.boundsOf(panel)));
        steps.add(GuideOverlay.Step.action(Lang.t("Ouvre l'onglet Cartes."),
            () -> GuideOverlay.boundsOf(tabButtons.get(Tab.CARTES.ordinal())), () -> tab == Tab.CARTES));
        steps.add(GuideOverlay.Step.action(Lang.t("Clique sur une carte pour la choisir."),
            () -> GuideOverlay.boundsOf(panel), () -> tab == Tab.CARTES && selected != null));
        steps.add(GuideOverlay.Step.say(Lang.t("Son prix et son effet s'affichent ici. Acheter la paie : elle rejoint ta collection. Clic droit "
            + "sur un article : sa fiche complète."), this::detailBounds));
        steps.add(GuideOverlay.Step.action(Lang.t("Ouvre l'onglet Rouleau."),
            () -> GuideOverlay.boundsOf(tabButtons.get(Tab.ROULEAU.ordinal())), () -> tab == Tab.ROULEAU));
        steps.add(GuideOverlay.Step.say(Lang.t("Un rouleau acheté se place dans ta machine, à la Table du croupier."),
            () -> GuideOverlay.boundsOf(panel)));
        steps.add(GuideOverlay.Step.say(Lang.t("Onglet Cosmétiques : le dos de tes cartes, ton tapis et ta machine. "
            + "Ils ne changent rien aux combats."), () -> GuideOverlay.boundsOf(tabButtons.get(Tab.COSMETIQUES.ordinal()))));
        return steps;
    }

    private Rectangle tabsBounds() {
        Rectangle bounds = GuideOverlay.boundsOf(tabButtons.get(0));
        for (TextButton button : tabButtons) bounds.merge(GuideOverlay.boundsOf(button));
        return bounds;
    }

    /** La fiche de l'article choisi, sous le panneau, et le bouton « Acheter ». */
    private Rectangle detailBounds() {
        return GuideOverlay.boundsOf(detailTitle).merge(GuideOverlay.boundsOf(detailText)).merge(GuideOverlay.boundsOf(buyButton));
    }

    /** Bouton « Tutoriel » : le Croupier présente la boutique de nouveau. */
    private void replayGuide() {
        firstVisit.replay();
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
            case COSMETIQUES -> buildCosmetics();
        }
        refreshCoins();
        layout();
        scroll.setScrollY(0f);
    }

    /** Onglet « Rang » : une ligne par rang ; seul le suivant peut être choisi. */
    private void buildRanks() {
        content.top().left();
        Rank current = profile.getRank(), next = profile.getNextRank();
        Rank active = profile.getActiveRank();
        String headerText = current == null ? Lang.t("Tu n'as pas encore de rang.")
            : active == current ? Lang.f("Ton rang : {0}", current.getTitle())
            : Lang.f("Ton rang : {0}. Tu joues avec : {1} (Table du croupier).", current.getTitle(),
                active == null ? Lang.t("Sans rang") : active.getTitle());
        Label header = new Label(headerText,
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
            Label state = new Label(owned ? Lang.t("Acquis") : Lang.f("{0} pièces",
                PlayerProfile.formatCoins(rank.getPrice())),
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
        if (next == null) showDetail(Lang.t("Tous les rangs sont acquis."), Lang.t("Tu es une légende. Les tables murmurent ton nom."));
    }

    /** Onglet « Cartes » : chaque carte en vente, son prix et les exemplaires possédés. */
    private List<Group> cardCells() {
        List<Group> cells = new ArrayList<>();
        for (Card card : cards) {
            long price = cardPrices.get(card.getId());
            int owned = profile.getOwnedCopies(card.getId());
            Offer offer = new Offer(card.getName(), Lang.f("{0}\nPossédées : {1} / {2}.",
                card.getDescription(), owned, PlayerProfile.MAX_COPIES), price,
                () -> profile.getOwnedCopies(card.getId()) >= PlayerProfile.MAX_COPIES
                    ? Lang.f("Tu as déjà {0} exemplaires.", PlayerProfile.MAX_COPIES) : null,
                () -> profile.buyCard(card.getId(), price));
            cells.add(cell(new TextureRegion(cardTextures.get(card)), CARD_WIDTH, CARD_HEIGHT,
                PlayerProfile.formatCoins(price), owned + " / " + PlayerProfile.MAX_COPIES,
                owned >= PlayerProfile.MAX_COPIES, offer,
                () -> detail.showForSale(card, cardTextures.get(card), Lang.f("{0} pièces",
                    PlayerProfile.formatCoins(price)))));
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
            Offer offer = new Offer(symbol.getDisplayName(), ReelShop.describe(symbol) + "\n"
                + (owned ? Lang.t("Déjà dans ta collection de rouleaux.") : Lang.t("À placer dans ta machine, à la Table du croupier.")),
                price, () -> profile.ownsReel(symbol) ? Lang.t("Tu possèdes déjà ce rouleau.") : null,
                () -> profile.buyReel(symbol, price));
            cells.add(cell(new TextureRegion(reelTextures.get(symbol)), REEL_WIDTH, REEL_HEIGHT,
                PlayerProfile.formatCoins(price), owned ? Lang.t("Possédé") : symbol.getDisplayName(), owned, offer,
                () -> detail.showReel(symbol.getDisplayName(), new TextureRegion(reelTextures.get(symbol)),
                    ReelShop.describe(symbol), Lang.f("{0} pièces", PlayerProfile.formatCoins(price)),
                    profile.ownsReel(symbol) ? Lang.t("Possédé") : Lang.t("Pas encore acheté"))));
        }
        return cells;
    }

    /** Case d'un article : son image, son prix et une ligne d'état. Un clic le choisit, un clic droit montre sa fiche. */
    private Group cell(TextureRegion region, float width, float height, String price, String state, boolean owned,
                       Offer offer, Runnable onInspect) {
        Image image = new Image(new TextureRegionDrawable(region));
        image.setSize(width, height);
        return cell(image, width, height, price, state, owned ? DIMMED : Color.WHITE,
            label -> select(offer, label), onInspect);
    }

    /**
     * Case d'un article : son image ({@code visual}, de taille {@code width} x {@code height}), une ligne dorée
     * (prix ou état) et une ligne en dessous. Un clic le choisit ({@code onSelect} reçoit la ligne dorée,
     * mise en valeur), un clic droit montre sa fiche ({@code onInspect}, {@code null} : aucune).
     */
    private Group cell(Actor visual, float width, float height, String price, String state, Color tint,
                       Consumer<Label> onSelect, Runnable onInspect) {
        Group cell = new Group();
        cell.setSize(Math.max(width, 150f), height + 64f);
        Group holder = new Group();
        holder.setSize(width, height);
        holder.setPosition((cell.getWidth() - width) / 2f, 64f);
        holder.setOrigin(Align.center);
        holder.setTransform(true);
        visual.setPosition(0f, 0f);
        holder.addActor(visual);
        holder.setColor(tint);
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
                if (button != Input.Buttons.RIGHT || onInspect == null) return false;
                clickSound.play();
                onSelect.accept(priceLabel);
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
                onSelect.accept(priceLabel);
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

    /**
     * Onglet « Cosmétiques » : une ligne par sorte (dos de cartes, tapis, machine), son nom à gauche, puis
     * l'aperçu de chaque style et son état : choisi, possédé, l'objectif à atteindre (succès, bestiaire) ou son prix.
     */
    private void buildCosmetics() {
        content.top().left();
        cosmeticLabels.clear();
        cosmeticFrame = new Image(hud.insetDrawable());
        cosmeticFrame.setColor(Palette.GOLD);
        cosmeticFrame.setTouchable(Touchable.disabled);
        for (Cosmetic.Kind kind : Cosmetic.Kind.values()) {
            Label header = new Label(kind.getLabel(), new Label.LabelStyle(headFont, Color.WHITE));
            header.setWrap(true);
            content.add(header).width(COSMETIC_HEAD).left();
            for (Cosmetic cosmetic : Cosmetic.values()) {
                if (cosmetic.getKind() != kind) continue;
                boolean owned = profile.ownsCosmetic(cosmetic);
                Group cell = cell(cosmeticPreview.build(cosmetic, COSMETIC_SIZE, COSMETIC_SIZE), COSMETIC_SIZE,
                    COSMETIC_SIZE, cosmeticState(cosmetic), cosmetic.getName(), owned ? Color.WHITE : DIMMED,
                    label -> selectCosmetic(cosmetic, label), null);
                cosmeticLabels.put(cosmetic, (Label) cell.getChildren().get(1));
                content.add(cell).size(cell.getWidth() + 20f, cell.getHeight()).pad(6f, CELL_GAP / 2f, 6f, CELL_GAP / 2f);
            }
            content.row();
        }
    }

    /** @return la ligne d'état d'un cosmétique : choisi, possédé, objectif ou prix. */
    private String cosmeticState(Cosmetic cosmetic) {
        if (profile.getCosmetic(cosmetic.getKind()) == cosmetic) return Lang.t("Choisi");
        if (profile.ownsCosmetic(cosmetic)) return Lang.t("Possédé");
        int[] progress = profile.getCosmeticProgress(cosmetic);
        return switch (cosmetic.getUnlock()) {
            case ACHIEVEMENTS -> Lang.f("Succès : {0} / {1}", progress[0], progress[1]);
            case BESTIARY     -> Lang.f("Bestiaire : {0} / {1}", progress[0], progress[1]);
            case COINS        -> PlayerProfile.formatCoins(cosmetic.getGoal());
            case DEFAULT      -> "";
        };
    }

    /** @return comment débloquer {@code cosmetic}. */
    private String cosmeticUnlockText(Cosmetic cosmetic) {
        int[] progress = profile.getCosmeticProgress(cosmetic);
        return switch (cosmetic.getUnlock()) {
            case ACHIEVEMENTS -> Lang.f("Se débloque avec {0} succès (tu en as {1}).", progress[1], progress[0]);
            case BESTIARY     -> cosmetic.getGoal() == Cosmetic.Unlock.ALL
                ? Lang.f("Se débloque en vainquant tous les ennemis du bestiaire ({0} / {1}).", progress[0], progress[1])
                : Lang.f("Se débloque en vainquant {0} ennemis du bestiaire ({1} / {0}).", progress[1], progress[0]);
            case COINS        -> Lang.f("Prix : {0} pièces.", PlayerProfile.formatCoins(cosmetic.getGoal()));
            case DEFAULT      -> "";
        };
    }

    /**
     * Choisit {@code cosmetic} (sa ligne d'état {@code actor} mise en valeur) et montre son détail. Le bouton
     * devient « Choisir » s'il est possédé (pour l'habiller), reste « Acheter » s'il s'achète, et ne fait
     * rien s'il est déjà habillé ou pas encore débloqué.
     */
    private void selectCosmetic(Cosmetic cosmetic, Actor actor) {
        select(null, null);
        selectedCosmetic = cosmetic;
        selectedActor    = actor;
        if (actor instanceof Label label) label.setColor(Color.WHITE);
        if (actor != null && cosmeticFrame != null) {
            Group cell = actor.getParent();
            Actor holder = cell.getChildren().get(0);
            cosmeticFrame.setBounds(holder.getX() - 8f, holder.getY() - 8f, holder.getWidth() + 16f, holder.getHeight() + 16f);
            cell.addActorAt(0, cosmeticFrame);
        }
        boolean owned = profile.ownsCosmetic(cosmetic);
        boolean worn  = profile.getCosmetic(cosmetic.getKind()) == cosmetic;
        boolean buy   = !owned && cosmetic.getUnlock() == Cosmetic.Unlock.COINS;
        buttons.setText(buyButton, buy ? Lang.t("Acheter") : Lang.t("Choisir"));
        buyButton.setDisabled(worn || (!owned && !buy));
        showDetail(Lang.f("{0} : {1}", cosmetic.getKind().getLabel(), cosmetic.getName()),
            cosmetic.getDescription() + (owned ? "" : "\n" + cosmeticUnlockText(cosmetic)));
    }

    /** Bouton du bas sur un cosmétique : l'habille s'il est possédé, sinon l'achète (puis il est habillé). */
    private void onCosmetic(Cosmetic cosmetic) {
        if (profile.ownsCosmetic(cosmetic)) {
            profile.chooseCosmetic(cosmetic);
            refreshCosmetics(cosmetic);
            detailTitle.setColor(Palette.GOLD);
            return;
        }
        if (cosmetic.getUnlock() != Cosmetic.Unlock.COINS) return;
        if (profile.isAdmin() || !profile.buyCosmetic(cosmetic)) {
            refuseSound.play();
            detailText.setText(profile.isAdmin() ? Lang.t("Achat impossible.") : Lang.f("Il te manque {0} pièces.",
                PlayerProfile.formatCoins(cosmetic.getGoal() - profile.getCoins())));
            detailText.setColor(REFUSED);
            return;
        }
        purchaseSound.play();
        refreshCosmetics(cosmetic);
        detailTitle.setText(Lang.f("{0} : acheté !", cosmetic.getName()));
        detailTitle.setColor(Palette.GOLD);
        coin.clearActions();
        coin.addAction(Actions.sequence(Actions.scaleTo(1.2f, 1.2f, 0.08f), Actions.scaleTo(1f, 1f, 0.15f)));
    }

    /** Refait l'onglet « Cosmétiques » (états à jour) sans bouger la grille, et choisit de nouveau {@code cosmetic}. */
    private void refreshCosmetics(Cosmetic cosmetic) {
        float scrollY = scroll.getScrollY();
        showTab(Tab.COSMETIQUES);
        scroll.layout();
        scroll.setScrollY(scrollY);
        selectCosmetic(cosmetic, cosmeticLabels.get(cosmetic));
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
        selectedCosmetic = null;
        if (cosmeticFrame != null) cosmeticFrame.remove();
        buttons.setText(buyButton, Lang.t("Acheter"));
        selected      = offer;
        selectedActor = actor;
        if (actor instanceof Label label) label.setColor(Color.WHITE);
        if (offer == null) {
            showDetail(tab == Tab.RANG ? "" : Lang.t("Choisis un article."),
                tab == Tab.CARTES ? Lang.t("Les cartes achetées rejoignent ta collection, puis la Table du croupier.")
                    : tab == Tab.ROULEAU ? Lang.t("Les rouleaux achetés se placent dans ta machine, à la Table du croupier.")
                    : tab == Tab.COSMETIQUES ? Lang.t("Les cosmétiques habillent ton jeu sans rien changer aux combats. "
                        + "Certains se débloquent par les succès ou le bestiaire, les autres s'achètent.")
                    : "");
            buyButton.setDisabled(true);
            return;
        }
        showDetail(Lang.f("{0}  -  {1} pièces", offer.name(), PlayerProfile.formatCoins(offer.price())), offer.text());
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
        if (leaving) return;
        if (selectedCosmetic != null) {
            onCosmetic(selectedCosmetic);
            return;
        }
        if (selected == null) return;
        String blocked = selected.blocked().get();
        if (blocked == null && profile.getCoins() < selected.price()) {
            blocked = Lang.f("Il te manque {0} pièces.",
                PlayerProfile.formatCoins(selected.price() - profile.getCoins()));
        }
        if (blocked != null || !selected.purchase().getAsBoolean()) {
            refuseSound.play();
            detailText.setText(blocked != null ? blocked : Lang.t("Achat impossible."));
            detailText.setColor(REFUSED);
            return;
        }
        purchaseSound.play();
        String name = selected.name();
        float scroll = this.scroll.getScrollY();
        showTab(tab); // met à jour les états (possédé, rang suivant…)
        this.scroll.layout();
        this.scroll.setScrollY(scroll);
        showDetail(Lang.f("{0} : acheté !", name), tab == Tab.RANG ? Lang.t("Ton nouveau rang vaut dans tous les combats.")
            : tab == Tab.CARTES ? Lang.t("La carte t'attend à la Table du croupier.")
            : Lang.t("Place-le dans ta machine, à la Table du croupier."));
        detailTitle.setColor(Palette.GOLD);
        coin.clearActions();
        coin.addAction(Actions.sequence(Actions.scaleTo(1.2f, 1.2f, 0.08f), Actions.scaleTo(1f, 1f, 0.15f)));
    }

    private void refreshCoins() {
        long amount = profile.getCoins();
        coins.setText(Lang.n(amount, "{0} pièce", "{0} pièces", PlayerProfile.formatCoins(amount)));
        coins.pack();
    }


    // -------------------------------------------------------------------------
    // Mise en page et cycle de vie
    // -------------------------------------------------------------------------

    private float gridWidth() {
        return stage.getViewport().getWorldWidth() - MARGIN * 2 - PANEL_PAD * 2;
    }

    @Override
    protected void layoutContent(float width, float height) {
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
        tutorialButton.setPosition(backButton.getX() + backButton.getWidth() + 20f, buttonY);
        buyButton.setPosition(width - MARGIN - buyButton.getWidth(), buttonY);
        float detailX = tutorialButton.getX() + tutorialButton.getWidth() + 40f;
        float detailWidth = buyButton.getX() - 40f - detailX;
        detailTitle.pack();
        detailTitle.setPosition(detailX, BOTTOM_SPACE - 18f - detailTitle.getHeight());
        detailText.setWidth(detailWidth);
        detailText.setHeight(detailText.getPrefHeight());
        detailText.setPosition(detailX, detailTitle.getY() - 6f - detailText.getHeight());
    }

    /** Échap referme la fiche d'un article, sinon revient au menu principal. */
    @Override
    protected boolean onKey(int keycode) {
        if (keycode != Input.Keys.ESCAPE) return false;
        clickSound.play();
        if (detail.isShown()) detail.hide();
        else onBack();
        return true;
    }

    @Override
    public void show() {
        super.show();
        stage.setScrollFocus(scroll);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        if (tab != Tab.RANG) showTab(tab); // la grille suit la largeur
        layout();
        detail.layout();
    }


    @Override
    public void dispose() {
        super.dispose();
        firstVisit.dispose();
        reelTextures.values().forEach(Texture::dispose);
        purchaseSound.dispose();
        refuseSound.dispose();
        detail.dispose();
        cosmeticPreview.dispose();
        Fonts.release(coinsFont);
        Fonts.release(headFont);
        Fonts.release(bodyFont);
        Fonts.release(smallFont);
    }
}
