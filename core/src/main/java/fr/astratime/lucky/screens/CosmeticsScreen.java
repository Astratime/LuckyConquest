package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Align;
import fr.astratime.lucky.LuckyGame;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.assets.TableTextures;
import fr.astratime.lucky.assets.VolumeSound;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.progress.Cosmetic;
import fr.astratime.lucky.progress.PlayerProfile;
import fr.astratime.lucky.views.CasinoButtons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cosmétiques : le joueur y choisit le dos de ses cartes, le tapis de la table
 * de combat et l'habillage de sa machine à sous (voir {@link Cosmetic}). Ils ne
 * changent rien aux combats.
 *
 * Un onglet par sorte. Chaque case montre l'aperçu, le nom et l'état : choisi,
 * possédé, l'objectif à atteindre (succès, bestiaire) ou le prix en pièces.
 * Un clic choisit la case ; le bouton du bas l'habille (« Choisir ») ou
 * l'achète (« Acheter », puis il est habillé). Le choix est enregistré tout de suite.
 */
public class CosmeticsScreen extends CasinoMenuScreen {

    private static final String PURCHASE_SOUND = "sounds/shop/purchase.ogg";
    private static final String REFUSE_SOUND   = "sounds/coins/loss.ogg";
    private static final String CLASSIC_BACK   = "cards/light/BACK.png";

    private static final float TITLE_TOP    = 90f;
    private static final float TABS_TOP     = 175f;
    private static final float PANEL_TOP    = 220f;
    private static final float BOTTOM_SPACE = 190f;
    private static final float PANEL_PAD    = 30f;
    private static final float COIN_SIZE    = 64f;
    private static final float PREVIEW_W    = 210f;
    private static final float PREVIEW_H    = 210f;
    private static final float CELL_GAP     = 26f;
    private static final float HOVER_SCALE  = 1.05f;
    private static final Color DIMMED       = new Color(0.45f, 0.45f, 0.45f, 1f);
    private static final Color REFUSED      = new Color(1f, 0.45f, 0.45f, 1f);
    /** Symboles montrés dans l'aperçu d'une machine. */
    private static final Symbol[] PREVIEW_SYMBOLS = {Symbol.SEVEN, Symbol.CHERRY, Symbol.BELL};

    private final PlayerProfile profile;
    private final Sound         purchaseSound;
    private final Sound         refuseSound;
    private final BitmapFont    coinsFont = Fonts.jersey(52, Palette.GOLD, 4f, Palette.TEXT_SHADE);
    private final BitmapFont    headFont  = Fonts.jersey(40, Palette.GOLD, 3f, Palette.TEXT_SHADE);
    private final BitmapFont    bodyFont  = Fonts.jersey(28, Palette.CREAM, 2f, Palette.TEXT_SHADE);
    private final BitmapFont    smallFont = Fonts.jersey(24, Color.WHITE, 2f, Palette.TEXT_SHADE);

    /** Images des aperçus, par chemin d'asset, libérées avec l'écran. */
    private final Map<String, Texture> textures = new HashMap<>();

    private final Image      coin;
    private final Label      coins;
    private final List<TextButton> tabButtons = new ArrayList<>();
    private final Image      panel;
    private final Table      content = new Table();
    private final ScrollPane scroll;
    private final Label      detailTitle;
    private final Label      detailText;
    private final TextButton actionButton;
    private final TextButton backButton;

    private Cosmetic.Kind tab = Cosmetic.Kind.BACK;
    /** Cosmétique choisi dans la grille ({@code null} : aucun). */
    private Cosmetic      selected;

    public CosmeticsScreen(LuckyGame luckyGame) {
        super(luckyGame, Lang.t("COSMÉTIQUES"), 84, 0.65f);
        this.profile  = luckyGame.getProfile();
        purchaseSound = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(PURCHASE_SOUND)), assets.audio);
        refuseSound   = new VolumeSound(Gdx.audio.newSound(Gdx.files.internal(REFUSE_SOUND)), assets.audio);

        coin = new Image(new TextureRegionDrawable(new TextureRegion(hud.coin)));
        coin.setSize(COIN_SIZE, COIN_SIZE);
        coins = new Label("", new Label.LabelStyle(coinsFont, Color.WHITE));
        for (Cosmetic.Kind kind : Cosmetic.Kind.values()) {
            tabButtons.add(buttons.create(kind.getLabel(), clickSound, () -> showTab(kind)));
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
        actionButton = buttons.createAction(Lang.t("Choisir"), clickSound, this::onAction);
        backButton   = buttons.create(Lang.t("Retour"), clickSound, this::onBack);
        stage.addActor(coin);
        stage.addActor(coins);
        tabButtons.forEach(stage::addActor);
        stage.addActor(panel);
        stage.addActor(scroll);
        stage.addActor(detailTitle);
        stage.addActor(detailText);
        stage.addActor(actionButton);
        stage.addActor(backButton);
        reveal();

        showTab(Cosmetic.Kind.BACK);
        fade.toFront();
    }

    // -------------------------------------------------------------------------
    // Onglets et grille
    // -------------------------------------------------------------------------

    /** Affiche l'onglet {@code shown}, avec le cosmétique habillé choisi d'office. */
    private void showTab(Cosmetic.Kind shown) {
        tab = shown;
        for (int i = 0; i < tabButtons.size(); i++) {
            tabButtons.get(i).setColor(Cosmetic.Kind.values()[i] == shown ? Color.WHITE : DIMMED);
        }
        rebuild();
        select(profile.getCosmetic(shown));
        scroll.setScrollY(0f);
    }

    /** Refait la grille de l'onglet affiché (états à jour). */
    private void rebuild() {
        content.clearChildren();
        content.top().left();
        List<Cosmetic> items = new ArrayList<>();
        for (Cosmetic cosmetic : Cosmetic.values()) if (cosmetic.getKind() == tab) items.add(cosmetic);
        float cellWidth = PREVIEW_W + 20f;
        int columns = Math.max(1, (int) ((gridWidth() + CELL_GAP) / (cellWidth + CELL_GAP)));
        for (int i = 0; i < items.size(); i++) {
            Group cell = cell(items.get(i), cellWidth);
            content.add(cell).size(cell.getWidth(), cell.getHeight()).pad(CELL_GAP / 2f);
            if ((i + 1) % columns == 0) content.row();
        }
        refreshCoins();
        layout();
    }

    /** Case d'un cosmétique : son aperçu, son nom et son état. Un clic le choisit. */
    private Group cell(Cosmetic cosmetic, float width) {
        boolean owned = profile.ownsCosmetic(cosmetic);
        boolean worn  = profile.getCosmetic(cosmetic.getKind()) == cosmetic;
        Group cell = new Group();
        cell.setSize(width, PREVIEW_H + 70f);
        Group holder = preview(cosmetic);
        holder.setPosition((width - PREVIEW_W) / 2f, 70f);
        holder.setOrigin(Align.center);
        holder.setTransform(true);
        if (!owned) holder.setColor(DIMMED);
        Label name = new Label(cosmetic.getName(), new Label.LabelStyle(smallFont, worn ? Palette.GOLD : Color.WHITE));
        name.setAlignment(Align.center);
        name.setBounds(0f, 36f, width, 28f);
        Label state = new Label(stateText(cosmetic), new Label.LabelStyle(smallFont, worn ? Palette.GOLD : Palette.CREAM));
        state.setAlignment(Align.center);
        state.setBounds(0f, 4f, width, 28f);
        cell.addActor(holder);
        cell.addActor(name);
        cell.addActor(state);
        if (cosmetic == selected) {
            Image frame = new Image(hud.insetDrawable());
            frame.setBounds(holder.getX() - 8f, holder.getY() - 8f, PREVIEW_W + 16f, PREVIEW_H + 16f);
            frame.setColor(Palette.GOLD);
            frame.setTouchable(Touchable.disabled);
            cell.addActorAt(0, frame);
        }
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
                select(cosmetic);
                rebuild();
            }
        });
        return cell;
    }

    /** @return l'aperçu de {@code cosmetic}, de taille {@link #PREVIEW_W} x {@link #PREVIEW_H}. */
    private Group preview(Cosmetic cosmetic) {
        Group group = new Group();
        group.setSize(PREVIEW_W, PREVIEW_H);
        group.setTouchable(Touchable.disabled);
        String folder = cosmetic.getStyleFolder();
        switch (cosmetic.getKind()) {
            case BACK -> {
                String path = folder == null ? CLASSIC_BACK : cosmetic.getBackPath();
                float h = PREVIEW_H, w = h * 190f / 270f;
                Image back = new Image(new TextureRegionDrawable(new TextureRegion(texture(path))));
                back.setBounds((PREVIEW_W - w) / 2f, 0f, w, h);
                group.addActor(back);
            }
            case FELT -> {
                String path = folder == null ? "table/felt.png" : cosmetic.getFeltPath();
                NinePatch railPatch = new NinePatch(texture("table/table_rail.png"), TableTextures.RAIL_BORDER,
                    TableTextures.RAIL_BORDER, TableTextures.RAIL_BORDER, TableTextures.RAIL_BORDER);
                railPatch.scale(0.5f, 0.5f);
                Image rail = new Image(new NinePatchDrawable(railPatch));
                rail.setBounds(0f, 0f, PREVIEW_W, PREVIEW_H);
                Image felt = new Image(new TiledDrawable(new TextureRegion(texture(path))));
                float inset = 12f;
                felt.setBounds(inset, inset, PREVIEW_W - inset * 2, PREVIEW_H - inset * 2);
                group.addActor(felt);
                group.addActor(rail);
            }
            case MACHINE -> {
                String dir = folder == null ? "table/" : cosmetic.getMachineFolder();
                float frameH = 110f, frameY = (PREVIEW_H - frameH) / 2f;
                Image frame = new Image(nine(dir + "reel_frame.png", TableTextures.REEL_FRAME_BORDER));
                frame.setBounds(0f, frameY, PREVIEW_W, frameH);
                group.addActor(frame);
                float pad = 18f, gap = 6f;
                float cellW = (PREVIEW_W - pad * 2 - gap * (PREVIEW_SYMBOLS.length - 1)) / PREVIEW_SYMBOLS.length;
                float cellH = frameH - pad * 2;
                for (int i = 0; i < PREVIEW_SYMBOLS.length; i++) {
                    float x = pad + i * (cellW + gap);
                    Image cellImage = new Image(nine(dir + "reel_cell.png", TableTextures.REEL_CELL_BORDER));
                    cellImage.setBounds(x, frameY + pad, cellW, cellH);
                    group.addActor(cellImage);
                    float size = Math.min(cellW, cellH) - 10f;
                    Image symbol = new Image(new TextureRegionDrawable(new TextureRegion(
                        texture(PREVIEW_SYMBOLS[i].getAssetPath()))));
                    symbol.setBounds(x + (cellW - size) / 2f, frameY + pad + (cellH - size) / 2f, size, size);
                    group.addActor(symbol);
                }
            }
        }
        return group;
    }

    /** @return la ligne d'état d'une case : choisi, possédé, objectif ou prix. */
    private String stateText(Cosmetic cosmetic) {
        if (profile.getCosmetic(cosmetic.getKind()) == cosmetic) return Lang.t("Choisi");
        if (profile.ownsCosmetic(cosmetic)) return Lang.t("Possédé");
        int[] progress = profile.getCosmeticProgress(cosmetic);
        return switch (cosmetic.getUnlock()) {
            case ACHIEVEMENTS -> Lang.f("Succès : {0} / {1}", progress[0], progress[1]);
            case BESTIARY     -> Lang.f("Bestiaire : {0} / {1}", progress[0], progress[1]);
            case COINS        -> Lang.f("{0} pièces", Lang.big(cosmetic.getGoal()));
            case DEFAULT      -> "";
        };
    }

    // -------------------------------------------------------------------------
    // Choix et achat
    // -------------------------------------------------------------------------

    /** Choisit {@code cosmetic} dans la grille et montre son détail et le bouton qui convient. */
    private void select(Cosmetic cosmetic) {
        selected = cosmetic;
        boolean owned = profile.ownsCosmetic(cosmetic);
        boolean worn  = profile.getCosmetic(cosmetic.getKind()) == cosmetic;
        String unlock = owned ? "" : "\n" + unlockText(cosmetic);
        showDetail(Lang.f("{0} : {1}", cosmetic.getKind().getLabel(), cosmetic.getName()), cosmetic.getDescription() + unlock);
        boolean buy = !owned && cosmetic.getUnlock() == Cosmetic.Unlock.COINS;
        buttons.setText(actionButton, buy ? Lang.t("Acheter") : Lang.t("Choisir"));
        actionButton.setDisabled(worn || (!owned && !buy));
        layout();
    }

    /** @return comment débloquer {@code cosmetic}. */
    private String unlockText(Cosmetic cosmetic) {
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

    private void showDetail(String heading, String text) {
        detailTitle.setText(heading);
        detailTitle.setColor(Color.WHITE);
        detailText.setText(text);
        detailText.setColor(Color.WHITE);
    }

    /** Bouton du bas : habille le cosmétique choisi, ou l'achète puis l'habille. */
    private void onAction() {
        if (selected == null || leaving) return;
        Cosmetic cosmetic = selected;
        if (profile.ownsCosmetic(cosmetic)) {
            profile.chooseCosmetic(cosmetic);
            rebuild();
            select(cosmetic);
            detailTitle.setColor(Palette.GOLD);
            return;
        }
        if (cosmetic.getUnlock() != Cosmetic.Unlock.COINS) return;
        if (profile.isAdmin()) {
            refuseSound.play();
            detailText.setText(Lang.t("Achat impossible."));
            detailText.setColor(REFUSED);
            return;
        }
        if (!profile.buyCosmetic(cosmetic)) {
            refuseSound.play();
            detailText.setText(Lang.f("Il te manque {0} pièces.",
                PlayerProfile.formatCoins(cosmetic.getGoal() - profile.getCoins())));
            detailText.setColor(REFUSED);
            return;
        }
        purchaseSound.play();
        rebuild();
        select(cosmetic);
        detailTitle.setText(Lang.f("{0} : acheté !", cosmetic.getName()));
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
    // Images
    // -------------------------------------------------------------------------

    private Texture texture(String path) {
        return textures.computeIfAbsent(path, p -> new Texture(Gdx.files.internal(p)));
    }

    private NinePatchDrawable nine(String path, int border) {
        return new NinePatchDrawable(new NinePatch(texture(path), border, border, border, border));
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
        actionButton.setPosition(width - MARGIN - actionButton.getWidth(), buttonY);
        float detailX = backButton.getX() + backButton.getWidth() + 40f;
        float detailWidth = actionButton.getX() - 40f - detailX;
        detailTitle.pack();
        detailTitle.setPosition(detailX, BOTTOM_SPACE - 18f - detailTitle.getHeight());
        detailText.setWidth(detailWidth);
        detailText.setHeight(detailText.getPrefHeight());
        detailText.setPosition(detailX, detailTitle.getY() - 6f - detailText.getHeight());
    }

    /** Échap revient au menu principal. */
    @Override
    protected boolean onKey(int keycode) {
        if (keycode != Input.Keys.ESCAPE) return false;
        clickSound.play();
        onBack();
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
        rebuild(); // la grille suit la largeur
    }

    @Override
    public void dispose() {
        super.dispose();
        textures.values().forEach(Texture::dispose);
        purchaseSound.dispose();
        refuseSound.dispose();
        Fonts.release(coinsFont);
        Fonts.release(headFont);
        Fonts.release(bodyFont);
        Fonts.release(smallFont);
    }
}
