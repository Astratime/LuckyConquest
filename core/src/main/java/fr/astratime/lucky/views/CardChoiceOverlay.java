package fr.astratime.lucky.views;

import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.entities.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;

/**
 * Fenêtre posée par-dessus le jeu quand une carte demande un choix au joueur :
 * le symbole du Pari, ou la carte retournée à la Roulette russe (trois cartes
 * faces cachées, dont un Joker maudit). Le joueur doit choisir : ni clic à
 * côté, ni Échap ne la referment.
 */
public class CardChoiceOverlay implements Disposable {

    private static final Color VEIL        = new Color(0f, 0f, 0f, 0.72f);
    private static final Color GOLD        = Color.valueOf("ffd454ff");
    private static final Color CREAM       = Color.valueOf("f0e0b0ff");
    private static final Color RED         = Color.valueOf("ff5a5aff");
    private static final Color TEXT_SHADE  = Color.valueOf("1a0f0fff");
    private static final float FADE        = 0.2f;
    private static final float PADDING     = 48f;
    private static final float TITLE_GAP   = 10f;
    private static final float OPTIONS_GAP = 30f;

    // Pari : une case par symbole.
    private static final int   BET_COLUMNS  = 6;
    private static final float BET_CELL_W   = 140f;
    private static final float BET_CELL_H   = 122f;
    private static final float BET_PAD      = 8f;
    private static final float HOVER_SCALE  = 1.12f;

    // Roulette : trois cartes, retournées l'une après l'autre.
    private static final float CARD_SCALE    = 2.3f;
    private static final float CARD_GAP      = 60f;
    private static final float HOVER_LIFT    = 16f;
    private static final float FLIP_TIME     = 0.18f;
    private static final float OTHERS_DELAY  = 0.6f;
    private static final float RESULT_TIME   = 1.9f;   // résultat affiché avant de refermer

    private final Stage   stage;
    private final Texture cardBack;
    private final Texture rouletteFace = new Texture(Gdx.files.internal("cards/special/russian_roulette.png"));
    private final Texture cursedFace   = new Texture(Gdx.files.internal("cards/special/cursed_joker.png"));
    private final HudTextures hud;
    private final BitmapFont titleFont    = Fonts.jersey(72, GOLD, 3f, TEXT_SHADE);
    private final BitmapFont subtitleFont = Fonts.jersey(34, CREAM, 2f, TEXT_SHADE);
    private final BitmapFont resultFont   = Fonts.jersey(56, Color.WHITE, 3f, TEXT_SHADE);

    private final Group root  = new Group();
    private final Image veil;
    private final Table panel = new Table();
    private final float cardWidth;
    private final float cardHeight;

    /**
     * @param cardBack   dos des cartes (faces cachées de la Roulette russe)
     * @param cardWidth  taille d'une carte de la main (agrandie ici)
     */
    public CardChoiceOverlay(Stage stage, HudTextures hud, Texture cardBack, float cardWidth, float cardHeight) {
        this.stage      = stage;
        this.hud        = hud;
        this.cardBack   = cardBack;
        this.cardWidth  = cardWidth * CARD_SCALE;
        this.cardHeight = cardHeight * CARD_SCALE;
        veil = new Image(new TextureRegionDrawable(new TextureRegion(hud.pixel)));
        veil.setColor(VEIL);
        root.addActor(veil);
        root.addActor(panel);
        root.setVisible(false);
        panel.setBackground(hud.panelDrawable());
        panel.pad(PADDING);
    }

    /** @return l'acteur racine de la fenêtre, à ajouter au Stage par-dessus le jeu. */
    public Group getActor() { return root; }

    /** @return {@code true} tant que la fenêtre attend le choix du joueur (ou montre son résultat). */
    public boolean isShown() { return root.isVisible(); }

    /**
     * Pari : propose {@code options} ; le symbole cliqué est transmis à {@code onPicked}, puis la fenêtre se referme.
     *
     * @param regionOf image de chaque symbole
     */
    public void showBet(List<Symbol> options, Function<Symbol, TextureRegion> regionOf,
                        Consumer<Symbol> onPicked) {
        open("PARI", "Choisis un symbole : gains x2, x3 ou x4 s'il sort 1, 2 ou 3 fois.\nS'il ne sort pas : gains /2.");
        Table grid = new Table();
        for (int i = 0; i < options.size(); i++) {
            Symbol symbol = options.get(i);
            Container<Image> cell = new Container<>(new Image(regionOf.apply(symbol)));
            cell.setBackground(hud.insetDrawable());
            cell.pad(BET_PAD).fill();
            cell.setTransform(true);
            cell.setSize(BET_CELL_W, BET_CELL_H);
            cell.setOrigin(BET_CELL_W / 2f, BET_CELL_H / 2f);
            cell.setTouchable(Touchable.enabled);
            cell.addListener(new InputListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (pointer != -1) return;
                    cell.clearActions();
                    cell.addAction(Actions.scaleTo(HOVER_SCALE, HOVER_SCALE, 0.1f, Interpolation.pow2Out));
                    cell.setColor(GOLD);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    if (pointer != -1) return;
                    cell.clearActions();
                    cell.addAction(Actions.scaleTo(1f, 1f, 0.1f));
                    cell.setColor(Color.WHITE);
                }

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    close(() -> onPicked.accept(symbol));
                    return true;
                }
            });
            grid.add(cell).size(BET_CELL_W, BET_CELL_H).pad(BET_PAD);
            if ((i + 1) % BET_COLUMNS == 0) grid.row();
        }
        panel.add(grid).padTop(OPTIONS_GAP);
        layout();
    }

    /**
     * Roulette russe : trois cartes faces cachées. La carte cliquée se
     * retourne, puis les autres ; l'indice choisi est transmis à
     * {@code onPicked} quand la fenêtre se referme.
     *
     * @param cursed pour chaque carte, {@code true} si c'est le Joker maudit
     * @param multiplier multiplicateur du pistolet, et {@code penaltyPercent} les gains perdus (pour le texte)
     */
    public void showRoulette(List<Boolean> cursed, int multiplier, int penaltyPercent, IntConsumer onPicked) {
        open("ROULETTE RUSSE", "Retourne une carte. Pistolet : degats d'un symbole x" + multiplier
            + ".\nJoker maudit : -" + penaltyPercent + "% de gains.");
        Table row = new Table();
        Label result = new Label(" ", new Label.LabelStyle(resultFont, Color.WHITE));
        result.setAlignment(Align.center);
        List<Image> cards = new ArrayList<>();
        boolean[] picked = { false };
        for (int i = 0; i < cursed.size(); i++) {
            int index = i;
            Image card = new Image(new TextureRegionDrawable(new TextureRegion(cardBack)));
            card.setTouchable(Touchable.enabled);
            Group slot = new Group();                // la carte se soulève dans son emplacement
            slot.setSize(cardWidth, cardHeight + HOVER_LIFT);
            card.setSize(cardWidth, cardHeight);
            card.setOrigin(cardWidth / 2f, cardHeight / 2f);
            slot.addActor(card);
            cards.add(card);
            card.addListener(new InputListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (pointer != -1 || picked[0]) return;
                    card.clearActions();
                    card.addAction(Actions.moveTo(0f, HOVER_LIFT, 0.12f, Interpolation.pow2Out));
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    if (pointer != -1 || picked[0]) return;
                    card.clearActions();
                    card.addAction(Actions.moveTo(0f, 0f, 0.12f));
                }

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (picked[0]) return true;
                    picked[0] = true;
                    boolean lost = cursed.get(index);
                    flip(card, lost ? cursedFace : rouletteFace, 0f);
                    card.addAction(Actions.delay(FLIP_TIME * 2f, Actions.run(() -> {
                        result.setText(lost ? "JOKER MAUDIT !  -" + penaltyPercent + "% DE GAINS"
                            : "PAN ! PISTOLET CHARGE  x" + multiplier);
                        result.setColor(lost ? RED : GOLD);
                        result.setOrigin(Align.center);
                        result.setFontScale(1f);
                    })));
                    for (int other = 0; other < cards.size(); other++) {
                        if (other == index) continue;
                        Image otherCard = cards.get(other);
                        flip(otherCard, cursed.get(other) ? cursedFace : rouletteFace, OTHERS_DELAY);
                        otherCard.addAction(Actions.delay(OTHERS_DELAY + FLIP_TIME * 2f,
                            Actions.color(new Color(0.45f, 0.45f, 0.45f, 1f), 0.2f)));
                    }
                    root.addAction(Actions.delay(RESULT_TIME, Actions.run(() -> close(() -> onPicked.accept(index)))));
                    return true;
                }
            });
            row.add(slot).size(cardWidth, cardHeight + HOVER_LIFT).padLeft(i == 0 ? 0f : CARD_GAP);
        }
        panel.add(row).padTop(OPTIONS_GAP);
        panel.row();
        panel.add(result).padTop(TITLE_GAP).growX();
        layout();
    }

    /** Retourne {@code card} (elle s'aplatit, change de face, puis se redéplie) après {@code delay} secondes. */
    private static void flip(Image card, Texture face, float delay) {
        card.addAction(Actions.sequence(
            Actions.delay(delay),
            Actions.scaleTo(0f, 1f, FLIP_TIME, Interpolation.pow2In),
            Actions.run(() -> card.setDrawable(new TextureRegionDrawable(new TextureRegion(face)))),
            Actions.scaleTo(1f, 1f, FLIP_TIME, Interpolation.pow2Out)));
    }

    private void open(String title, String subtitle) {
        panel.clearChildren();
        root.clearActions();
        Label titleLabel = new Label(title, new Label.LabelStyle(titleFont, Color.WHITE));
        Label subtitleLabel = new Label(subtitle, new Label.LabelStyle(subtitleFont, Color.WHITE));
        subtitleLabel.setAlignment(Align.center);
        panel.add(titleLabel);
        panel.row();
        panel.add(subtitleLabel).padTop(TITLE_GAP);
        panel.row();
        root.setVisible(true);
        root.setTouchable(Touchable.enabled);
        root.getColor().a = 0f;
        root.addAction(Actions.fadeIn(FADE));
        root.toFront();
    }

    private void close(Runnable then) {
        root.setTouchable(Touchable.disabled);
        root.clearActions();
        root.addAction(Actions.sequence(Actions.fadeOut(FADE), Actions.visible(false), Actions.run(then)));
    }

    /** Referme immédiatement la fenêtre, sans transmettre de choix (nouveau combat). */
    public void hide() {
        root.clearActions();
        root.setVisible(false);
    }

    /** Recentre la fenêtre (après un redimensionnement). */
    public void layout() {
        float width  = stage.getViewport().getWorldWidth();
        float height = stage.getViewport().getWorldHeight();
        veil.setSize(width, height);
        root.setSize(width, height);
        panel.pack();
        panel.setPosition((width - panel.getWidth()) / 2f, (height - panel.getHeight()) / 2f);
    }

    @Override
    public void dispose() {
        rouletteFace.dispose();
        cursedFace.dispose();
        titleFont.dispose();
        subtitleFont.dispose();
        resultFont.dispose();
    }
}
