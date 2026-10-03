package fr.astratime.lucky.views;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
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
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.EnemyTextures;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.entities.enemy.EnemyKind;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.IntConsumer;

/**
 * Transitions de la Tour des épreuves, par-dessus le combat terminé.
 * <ul>
 *   <li>{@link #showChoice} : trois cartes faces cachées, chacune cachant un
 *       ennemi. Le joueur en retourne une ; les deux autres se retournent
 *       ensuite pour montrer ce qu'il a évité. « Combattre ! » lance le combat ;</li>
 *   <li>{@link #showBoss} : le boss se dévoile, avec « Affronter ».</li>
 * </ul>
 */
public class EnemyPickOverlay implements Disposable {

    private static final float CARD_WIDTH    = 228f;   // dos du jeu sombre (190 x 270), agrandi
    private static final float CARD_HEIGHT   = 324f;
    private static final float CARD_GAP      = 70f;
    private static final float HOVER_LIFT    = 18f;
    private static final float FLIP_TIME     = 0.14f;  // chaque moitié du retournement
    private static final float OTHERS_DELAY  = 0.7f;   // les deux autres cartes se retournent ensuite
    private static final float PORTRAIT_SIZE = 4f;     // pixels du portrait sur une carte
    private static final float BOSS_SIZE     = 7f;     // pixels du portrait du boss
    private static final float BUTTON_DELAY  = 0.5f;

    private final Stage          stage;
    private final HudTextures    hud;
    private final EnemyTextures  enemyTextures;
    private final Sound          flipSound;
    private final Sound          hoverSound;
    private final Sound          revealSound;
    private final BitmapFont     titleFont = Fonts.jersey(72, Palette.GOLD, 5f, Palette.TEXT_SHADE);
    private final BitmapFont     textFont  = Fonts.jersey(34, Palette.CREAM, 2f, Palette.TEXT_SHADE);
    private final BitmapFont     nameFont  = Fonts.jersey(30, Color.WHITE, 2f, Palette.TEXT_SHADE);

    private final Group root = new Group();
    private final Image veil;
    private final Label title;
    private final Label subtitle;
    private final Label description;
    private final List<Group> cards = new ArrayList<>();
    private TextButton button;
    private boolean picked;

    /**
     * @param flipSound   bruitage d'une carte retournée
     * @param hoverSound  bruitage du survol d'une carte
     * @param revealSound bruitage de l'apparition du boss
     */
    public EnemyPickOverlay(Stage stage, HudTextures hud, EnemyTextures enemyTextures, Sound flipSound,
                            Sound hoverSound, Sound revealSound) {
        this.stage         = stage;
        this.hud           = hud;
        this.enemyTextures = enemyTextures;
        this.flipSound     = flipSound;
        this.hoverSound    = hoverSound;
        this.revealSound   = revealSound;

        veil = new Image(new TextureRegionDrawable(new TextureRegion(hud.pixel)));
        veil.setColor(0f, 0f, 0f, 0.86f);
        veil.setTouchable(Touchable.enabled); // bloque les clics vers le jeu
        title = new Label("", new Label.LabelStyle(titleFont, Color.WHITE));
        title.setAlignment(Align.center);
        subtitle = new Label("", new Label.LabelStyle(textFont, Color.WHITE));
        subtitle.setAlignment(Align.center);
        description = new Label("", new Label.LabelStyle(textFont, Color.WHITE));
        description.setAlignment(Align.center);
        description.setWrap(true);
        root.addActor(veil);
        root.addActor(title);
        root.addActor(subtitle);
        root.addActor(description);
        root.setVisible(false);
    }

    /** @return le calque des transitions, à ajouter au Stage au-dessus du jeu. */
    public Group getActor() { return root; }

    /** @return {@code true} tant qu'une transition est affichée. */
    public boolean isShown() { return root.isVisible(); }

    /**
     * Trois cartes faces cachées, une par ennemi de {@code choices} (dans
     * l'ordre des cartes).
     *
     * @param buttonFactory crée un bouton (texte, action)
     * @param onFight       reçoit l'index de la carte retournée, au clic sur « Combattre ! »
     */
    public void showChoice(List<EnemyKind> choices, BiFunction<String, Runnable, TextButton> buttonFactory,
                           IntConsumer onFight) {
        open("CHOISIS TON ADVERSAIRE", "Trois cartes. Trois ennemis. Une seule carte se retourne.");
        picked = false;
        for (int i = 0; i < choices.size(); i++) {
            int index = i;
            Group card = buildCard(choices.get(i));
            Image back = (Image) card.getChildren().first();
            card.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    super.enter(event, x, y, pointer, fromActor);
                    if (pointer != -1 || picked) return;
                    hoverSound.play();
                    back.setColor(Palette.GOLD_PALE);
                    card.addAction(Actions.moveTo(card.getX(), restY() + HOVER_LIFT, 0.12f, Interpolation.pow2Out));
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    super.exit(event, x, y, pointer, toActor);
                    if (pointer != -1 || picked) return;
                    back.setColor(Color.WHITE);
                    card.addAction(Actions.moveTo(card.getX(), restY(), 0.12f, Interpolation.pow2Out));
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (picked) return;
                    pick(index, choices, buttonFactory, onFight);
                }
            });
            cards.add(card);
            root.addActor(card);
        }
        layout();
        for (int i = 0; i < cards.size(); i++) {
            Group card = cards.get(i);
            float y = card.getY();
            card.setY(y - stage.getViewport().getWorldHeight());
            card.addAction(Actions.delay(i * 0.1f, Actions.moveTo(card.getX(), y, 0.45f, Interpolation.swingOut)));
        }
    }

    /** La carte {@code index} se retourne, puis les deux autres ; « Combattre ! » apparaît. */
    private void pick(int index, List<EnemyKind> choices, BiFunction<String, Runnable, TextButton> buttonFactory,
                      IntConsumer onFight) {
        picked = true;
        EnemyKind chosen = choices.get(index);
        for (int i = 0; i < cards.size(); i++) {
            Group card = cards.get(i);
            card.clearActions();
            card.setY(i == index ? restY() + HOVER_LIFT : restY());
            ((Image) card.getChildren().first()).setColor(Color.WHITE);
            float delay = i == index ? 0f : OTHERS_DELAY;
            flip(card, delay);
            if (i != index) card.addAction(Actions.delay(delay + FLIP_TIME * 2, Actions.alpha(0.45f, 0.3f)));
        }
        subtitle.setText(chosen.getDisplayName().toUpperCase() + " !");
        description.setText(chosen.getDescription());
        description.getColor().a = 0f;
        description.addAction(Actions.delay(FLIP_TIME * 2, Actions.fadeIn(0.3f)));
        showButton(buttonFactory.apply("Combattre !", () -> {
            hide();
            onFight.accept(index);
        }), OTHERS_DELAY + BUTTON_DELAY);
    }

    /**
     * Le boss {@code boss} se dévoile : son portrait grandit, son nom et sa présentation suivent.
     *
     * @param onFight appelé au clic sur « Affronter »
     */
    public void showBoss(EnemyKind boss, BiFunction<String, Runnable, TextButton> buttonFactory, Runnable onFight) {
        open("BOSS", boss.getDisplayName().toUpperCase());
        revealSound.play();
        Texture texture = enemyTextures.portrait(boss);
        Image portrait = new Image(new TextureRegionDrawable(new TextureRegion(texture)));
        portrait.setSize(texture.getWidth() * BOSS_SIZE, texture.getHeight() * BOSS_SIZE);
        portrait.setOrigin(portrait.getWidth() / 2f, portrait.getHeight() / 2f);
        Group holder = new Group();
        holder.addActor(portrait);
        holder.setSize(portrait.getWidth(), portrait.getHeight());
        cards.add(holder);
        root.addActor(holder);
        description.setText(boss.getDescription());
        layout();
        portrait.setScale(0.2f);
        portrait.getColor().a = 0f;
        portrait.addAction(Actions.parallel(Actions.fadeIn(0.5f),
            Actions.scaleTo(1f, 1f, 0.7f, Interpolation.swingOut)));
        portrait.addAction(Actions.delay(0.8f, Actions.forever(Actions.sequence(
            Actions.moveBy(0f, 8f, 1.2f, Interpolation.sine), Actions.moveBy(0f, -8f, 1.2f, Interpolation.sine)))));
        showButton(buttonFactory.apply("Affronter", () -> {
            hide();
            onFight.run();
        }), 0.9f);
    }

    /** Ferme la transition. */
    public void hide() {
        root.clearActions();
        root.setVisible(false);
        cards.forEach(Actor::remove);
        cards.clear();
        if (button != null) button.remove();
        button = null;
    }

    /** Replace les éléments selon la taille de l'écran. */
    public void layout() {
        float width  = stage.getViewport().getWorldWidth();
        float height = stage.getViewport().getWorldHeight();
        veil.setBounds(0f, 0f, width, height);
        title.setBounds(0f, height * 0.84f, width, 80f);
        subtitle.setBounds(0f, height * 0.77f, width, 50f);
        float rowWidth = cards.size() * CARD_WIDTH + Math.max(0, cards.size() - 1) * CARD_GAP;
        float x = (width - rowWidth) / 2f;
        for (Group card : cards) {
            if (card.getWidth() == CARD_WIDTH) {
                card.setPosition(x, restY());
                x += CARD_WIDTH + CARD_GAP;
            } else { // portrait du boss, centré
                card.setPosition((width - card.getWidth()) / 2f, height * 0.5f - card.getHeight() / 2f + 40f);
            }
        }
        float descriptionWidth = Math.min(900f, width - 80f);
        description.setBounds((width - descriptionWidth) / 2f, height * 0.17f, descriptionWidth, 90f);
        if (button != null) button.setPosition((width - button.getWidth()) / 2f, height * 0.08f);
    }

    /** @return la hauteur des cartes au repos. */
    private float restY() {
        return stage.getViewport().getWorldHeight() * 0.48f - CARD_HEIGHT / 2f;
    }

    private void open(String titleText, String subtitleText) {
        hide();
        title.setText(titleText);
        subtitle.setText(subtitleText);
        description.setText("");
        root.setVisible(true);
        root.toFront();
        root.getColor().a = 0f;
        root.addAction(Actions.fadeIn(0.3f));
    }

    private void showButton(TextButton newButton, float delay) {
        button = newButton;
        root.addActor(button);
        layout();
        button.setVisible(false);
        button.getColor().a = 0f;
        button.addAction(Actions.delay(delay, Actions.sequence(Actions.visible(true), Actions.fadeIn(0.3f))));
    }

    /**
     * Carte du choix : le dos du jeu sombre et, caché derrière, sa face (le
     * portrait de l'ennemi et son nom).
     */
    private Group buildCard(EnemyKind kind) {
        Group card = new Group();
        card.setSize(CARD_WIDTH, CARD_HEIGHT);
        card.setOrigin(CARD_WIDTH / 2f, CARD_HEIGHT / 2f);
        card.setTransform(true);

        Image back = new Image(new TextureRegionDrawable(new TextureRegion(enemyTextures.cardBack)));
        back.setSize(CARD_WIDTH, CARD_HEIGHT);
        back.setTouchable(Touchable.disabled);

        Group face = new Group();
        face.setSize(CARD_WIDTH, CARD_HEIGHT);
        face.setTouchable(Touchable.disabled);
        Image frame = new Image(hud.panelDrawable());
        frame.setSize(CARD_WIDTH, CARD_HEIGHT);
        Texture texture = enemyTextures.portrait(kind);
        Image portrait = new Image(new TextureRegionDrawable(new TextureRegion(texture)));
        portrait.setSize(texture.getWidth() * PORTRAIT_SIZE, texture.getHeight() * PORTRAIT_SIZE);
        portrait.setPosition((CARD_WIDTH - portrait.getWidth()) / 2f, CARD_HEIGHT - portrait.getHeight() - 24f);
        Label name = new Label(kind.getDisplayName(), new Label.LabelStyle(nameFont, Color.WHITE));
        name.setWrap(true);
        name.setAlignment(Align.center);
        name.setBounds(12f, 14f, CARD_WIDTH - 24f, portrait.getY() - 18f);
        face.addActor(frame);
        face.addActor(portrait);
        face.addActor(name);
        face.setVisible(false);

        card.addActor(back);
        card.addActor(face);
        return card;
    }

    /** Retourne {@code card} après {@code delay} : elle se rétrécit, montre sa face, puis reprend sa largeur. */
    private void flip(Group card, float delay) {
        Actor back = card.getChildren().get(0), face = card.getChildren().get(1);
        card.addAction(Actions.sequence(
            Actions.delay(delay),
            Actions.scaleTo(0f, 1f, FLIP_TIME, Interpolation.pow2In),
            Actions.run(() -> {
                back.setVisible(false);
                face.setVisible(true);
                flipSound.play();
            }),
            Actions.scaleTo(1f, 1f, FLIP_TIME, Interpolation.pow2Out)));
    }

    @Override
    public void dispose() {
        Fonts.release(titleFont);
        Fonts.release(textFont);
        Fonts.release(nameFont);
    }
}
