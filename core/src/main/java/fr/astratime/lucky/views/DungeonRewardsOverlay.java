package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.CardTextures;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.effects.Effect;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.exploration.Place;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.progress.PlayerProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Détail des récompenses d'un donjon de l'Exploration, par-dessus la carte :
 * les pièces (gains du combat, coffre, carte en double, part gardée après une
 * défaite, rouleau du lieu) puis les cartes du coffre, chacune avec son image,
 * sa chance, les exemplaires possédés et ses effets. Un clic n'importe où ou
 * Échap (géré par l'écran via {@link #hide()}) le referme.
 */
public class DungeonRewardsOverlay implements Disposable {

    private static final Color VEIL        = new Color(0f, 0f, 0f, 0.8f);
    private static final float PADDING     = 44f;
    private static final float WIDTH       = 1380f;
    private static final float CARD_WIDTH  = 152f;
    private static final float CARD_HEIGHT = 216f;
    private static final float COLUMN_GAP  = 36f;
    private static final float LINE_GAP    = 6f;
    private static final float FADE        = 0.18f;

    private final Stage                  stage;
    private final CardTextures           cardTextures;
    private final Function<String, Card> cards;
    private final BitmapFont titleFont = Fonts.jersey(60, Palette.TEXT_TITLE, 3f, Palette.TEXT_SHADE);
    private final BitmapFont keyFont   = Fonts.jersey(34, Palette.TEXT_TITLE, 2f, Palette.TEXT_SHADE);
    private final BitmapFont bodyFont  = Fonts.jersey(28, Palette.TEXT_BODY, 2f, Palette.TEXT_SHADE);
    private final BitmapFont hintFont  = Fonts.jersey(24, Color.LIGHT_GRAY, 2f, Palette.TEXT_SHADE);

    private final Group root  = new Group();
    private final Image veil;
    private final Table panel = new Table();
    private final HudTextures hud;

    /**
     * @param cards fabrique des cartes, par id (celles des coffres)
     */
    public DungeonRewardsOverlay(Stage stage, HudTextures hud, CardTextures cardTextures, Function<String, Card> cards) {
        this.stage        = stage;
        this.hud          = hud;
        this.cardTextures = cardTextures;
        this.cards        = cards;
        veil = new Image(new TextureRegionDrawable(new TextureRegion(hud.pixel)));
        veil.setColor(VEIL);
        panel.setBackground(hud.tooltipDrawable());
        panel.pad(PADDING);
        root.addActor(veil);
        root.addActor(panel);
        root.setVisible(false);
        root.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { hide(); }
        });
    }

    /** @return l'acteur racine, à ajouter au Stage par-dessus l'écran. */
    public Group getActor() { return root; }

    /** @return {@code true} tant que le détail est affiché. */
    public boolean isShown() { return root.isVisible(); }

    /** Affiche les récompenses de {@code dungeon}, avec ce que possède déjà le joueur de {@code profile}. */
    public void show(Dungeon dungeon, PlayerProfile profile) {
        panel.clearChildren();
        Place place = Place.of(dungeon);
        float inner = WIDTH - PADDING * 2;

        panel.add(label(Lang.f("Récompenses : {0}", dungeon.getName()), titleFont)).left().padBottom(LINE_GAP * 3);
        panel.row();
        Image rule = new Image(new TextureRegionDrawable(new TextureRegion(hud.tooltipRule)));
        panel.add(rule).width(inner).height(rule.getPrefHeight() * 1.5f).padBottom(LINE_GAP * 3);
        panel.row();

        panel.add(label(Lang.t("Pièces"), keyFont)).left();
        panel.row();
        for (String line : coinLines(place, profile.isAdmin())) {
            panel.add(wrapped("- " + line, inner)).width(inner).left().padTop(LINE_GAP);
            panel.row();
        }

        panel.add(label(Lang.t("Cartes du coffre (une par coffre)"), keyFont)).left().padTop(LINE_GAP * 5);
        panel.row();
        Table columns = new Table();
        List<Dungeon.Loot> loot = dungeon.getLoot();
        float columnWidth = (inner - COLUMN_GAP * (loot.size() - 1)) / loot.size();
        for (int i = 0; i < loot.size(); i++) {
            columns.add(cardColumn(loot.get(i), profile, columnWidth)).width(columnWidth).top()
                .padLeft(i == 0 ? 0f : COLUMN_GAP);
        }
        panel.add(columns).width(inner).left().padTop(LINE_GAP * 3);
        panel.row();
        panel.add(label(Lang.t("Clic ou Échap pour fermer"), hintFont)).left().padTop(LINE_GAP * 5);

        layout();
        root.clearActions();
        root.setVisible(true);
        root.setTouchable(Touchable.enabled);
        root.getColor().a = 0f;
        root.addAction(Actions.fadeIn(FADE, Interpolation.pow2Out));
        root.toFront();
    }

    /** @return les lignes de la partie « Pièces » pour un donjon de {@code place}. */
    static List<String> coinLines(Place place, boolean admin) {
        int cost = place.getSpinCost();
        List<String> lines = new ArrayList<>();
        if (admin) lines.add(Lang.t("Mode ADMIN : ses combats ne rapportent rien."));
        lines.add(Lang.t("Roi battu : tes gains du combat (cadre Gains) deviennent des pièces."));
        lines.add(Lang.f("Coffre : {0} pièces ({1} tirages à {2}).", PlayerProfile.formatCoins(PlayerProfile.chestCoins(cost)),
            PlayerProfile.CHEST_SPINS, PlayerProfile.formatCoins(cost)));
        lines.add(Lang.f("Carte du coffre déjà en {0} exemplaires : {1} pièces en plus.", PlayerProfile.MAX_COPIES,
            PlayerProfile.formatCoins(PlayerProfile.duplicateCoins(cost))));
        lines.add(Lang.f("Défaite : tu gardes {0} % de tes gains, en pièces.", PlayerProfile.DEFEAT_KEEP_PERCENT));
        lines.add(Lang.t("Carte au trésor jouée : un coffre de plus."));
        Symbol reel = place.getReelReward();
        if (reel != null) {
            lines.add(Lang.f("Les quatre donjons du lieu vidés : le rouleau {0}.", reel.getDisplayName()));
        }
        return lines;
    }

    /** Une carte du coffre : son image, son nom, sa chance, les exemplaires possédés et ses effets. */
    private Table cardColumn(Dungeon.Loot loot, PlayerProfile profile, float width) {
        Card card = cards.apply(loot.cardId());
        Table column = new Table();
        column.top().left();
        Image image = new Image(new TextureRegionDrawable(new TextureRegion(cardTextures.get(card))));
        Table head = new Table();
        head.top().left();
        head.add(image).size(CARD_WIDTH, CARD_HEIGHT).top().padRight(18f);
        Table facts = new Table();
        facts.top().left();
        float factsWidth = width - CARD_WIDTH - 18f;
        facts.add(wrapped(card.getName(), factsWidth, keyFont, Palette.GOLD)).width(factsWidth).left();
        facts.row();
        facts.add(wrapped(Lang.f("Chance : {0} %", loot.percent()), factsWidth)).width(factsWidth).left().padTop(LINE_GAP);
        facts.row();
        int owned = profile.getOwnedCopies(loot.cardId());
        facts.add(wrapped(Lang.f("Possédée : {0}/{1}", owned, PlayerProfile.MAX_COPIES), factsWidth))
            .width(factsWidth).left().padTop(LINE_GAP);
        facts.row();
        facts.add(wrapped(card.isConsumable() ? Lang.t("Consommable") : Lang.t("Carte spéciale"), factsWidth))
            .width(factsWidth).left().padTop(LINE_GAP);
        head.add(facts).width(factsWidth).top();
        column.add(head).width(width).left();
        column.row();
        for (Effect effect : card.getEffects()) {
            for (String part : effect.getDescription().split("\n")) {
                column.add(wrapped("- " + part, width)).width(width).left().padTop(LINE_GAP);
                column.row();
            }
        }
        return column;
    }

    private Label label(String text, BitmapFont font) {
        return new Label(text, new Label.LabelStyle(font, Color.WHITE));
    }

    private Label wrapped(String text, float width) {
        return wrapped(text, width, bodyFont, Color.WHITE);
    }

    private Label wrapped(String text, float width, BitmapFont font, Color color) {
        Label label = new Label(text, new Label.LabelStyle(font, color));
        label.setWrap(true);
        label.setAlignment(Align.left);
        label.setWidth(width);
        return label;
    }

    /** Referme le détail. */
    public void hide() {
        root.clearActions();
        root.setVisible(false);
    }

    /** Recentre le détail (après un redimensionnement). */
    public void layout() {
        float width  = stage.getViewport().getWorldWidth();
        float height = stage.getViewport().getWorldHeight();
        root.setSize(width, height);
        veil.setSize(width, height);
        panel.pack();
        panel.setPosition((width - panel.getWidth()) / 2f, (height - panel.getHeight()) / 2f);
    }

    @Override
    public void dispose() {
        Fonts.release(titleFont);
        Fonts.release(keyFont);
        Fonts.release(bodyFont);
        Fonts.release(hintFont);
    }
}
