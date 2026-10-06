package fr.astratime.lucky.views;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.CardTextures;
import fr.astratime.lucky.assets.EnemyTextures;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Symbol;
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.i18n.Lang;
import fr.astratime.lucky.progress.PlayerProfile;

/**
 * Coffre au trésor, au bout d'un donjon de l'Exploration. Le coffre fermé
 * tremble ; un clic l'ouvre : la carte trouvée en sort, avec son nom, ce
 * qu'elle fait, le nombre d'exemplaires possédés et les pièces gagnées. Le
 * bouton reçu (retour à l'Exploration) apparaît ensuite.
 */
public class ChestOverlay implements Disposable {

    private static final float CHEST_SCALE = 8f;    // pixels du coffre
    private static final float CARD_WIDTH  = 228f;  // carte trouvée (480 x 672), réduite
    private static final float CARD_HEIGHT = 319f;
    private static final float TEXT_WIDTH  = 560f;
    private static final float GAP         = 60f;

    /** Reçoit le centre du coffre ouvert, pour y lancer des confettis. */
    public interface Burst {
        void at(float x, float y);
    }

    private final Stage         stage;
    private final EnemyTextures enemyTextures;
    private final CardTextures  cardTextures;
    private final Sound         openSound;
    private final Sound         coinsSound;
    private final Burst         burst;
    private final BitmapFont    titleFont = Fonts.jersey(72, Palette.GOLD, 5f, Palette.TEXT_SHADE);
    private final BitmapFont    nameFont  = Fonts.jersey(56, Palette.GOLD, 4f, Palette.TEXT_SHADE);
    private final BitmapFont    textFont  = Fonts.jersey(32, Palette.CREAM, 2f, Palette.TEXT_SHADE);
    private final BitmapFont    rewardFont = Fonts.jersey(40, Color.WHITE, 3f, Palette.TEXT_SHADE);

    private final Group root = new Group();
    private final Image veil;
    private final Label title;
    private final Label subtitle;
    private final Image chest;
    private final Label hint;
    private final Image card;
    private final Label cardName;
    private final Label cardText;
    private final Label copiesText;
    private final Label coinsText;
    private final Label reelText;
    private Symbol      earnedReel;
    private TextButton button;
    private boolean    opened;

    /**
     * @param openSound  bruitage du coffre qui s'ouvre
     * @param coinsSound bruitage des pièces gagnées
     * @param burst      lance des confettis depuis le coffre ouvert
     */
    public ChestOverlay(Stage stage, HudTextures hud, EnemyTextures enemyTextures, CardTextures cardTextures,
                        Sound openSound, Sound coinsSound, Burst burst) {
        this.stage         = stage;
        this.enemyTextures = enemyTextures;
        this.cardTextures  = cardTextures;
        this.openSound     = openSound;
        this.coinsSound    = coinsSound;
        this.burst         = burst;

        veil = new Image(new TextureRegionDrawable(new TextureRegion(hud.pixel)));
        veil.setColor(0f, 0f, 0f, 0.88f);
        veil.setTouchable(Touchable.enabled); // bloque les clics vers le jeu
        title = new Label(Lang.t("COFFRE AU TRÉSOR"), new Label.LabelStyle(titleFont, Color.WHITE));
        title.setAlignment(Align.center);
        subtitle = new Label("", new Label.LabelStyle(textFont, Color.WHITE));
        subtitle.setAlignment(Align.center);
        chest = new Image(new TextureRegionDrawable(new TextureRegion(enemyTextures.chestClosed)));
        chest.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { open(); }
        });
        hint = new Label(Lang.t("Clique sur le coffre"), new Label.LabelStyle(textFont, Color.WHITE));
        hint.setAlignment(Align.center);
        card = new Image();
        card.setTouchable(Touchable.disabled);
        cardName = new Label("", new Label.LabelStyle(nameFont, Color.WHITE));
        cardName.setWrap(true);
        cardText = new Label("", new Label.LabelStyle(textFont, Color.WHITE));
        cardText.setWrap(true);
        copiesText = new Label("", new Label.LabelStyle(rewardFont, Color.WHITE));
        coinsText  = new Label("", new Label.LabelStyle(rewardFont, Palette.GOLD));
        reelText   = new Label("", new Label.LabelStyle(rewardFont, Palette.GOLD));

        root.addActor(veil);
        root.addActor(title);
        root.addActor(subtitle);
        root.addActor(chest);
        root.addActor(hint);
        root.addActor(card);
        root.addActor(cardName);
        root.addActor(cardText);
        root.addActor(copiesText);
        root.addActor(coinsText);
        root.addActor(reelText);
        root.setVisible(false);
    }

    /** @return le calque du coffre, à ajouter au Stage au-dessus du jeu. */
    public Group getActor() { return root; }

    /** @return {@code true} tant que le coffre est affiché. */
    public boolean isShown() { return root.isVisible(); }

    /**
     * Rouleau gagné avec ce donjon (le Rouleau de la Mine), annoncé sous les
     * pièces du prochain coffre ouvert ; {@code null} : aucun.
     */
    public void setEarnedReel(Symbol reel) { earnedReel = reel; }

    /**
     * Montre le coffre fermé de {@code dungeon}.
     *
     * @param reward    ce qu'il contient (déjà enregistré dans le profil du joueur)
     * @param found     la carte trouvée
     * @param newButton bouton affiché une fois le coffre ouvert
     */
    public void show(Dungeon dungeon, PlayerProfile.ChestReward reward, Card found, TextButton newButton) {
        hide();
        opened = false;
        subtitle.setText(dungeon.getSuit() != null
            ? Lang.f("{0} TERMINÉ !", dungeon.getName().toUpperCase())
            : Lang.f("VICTOIRE : {0} !", dungeon.getName().toUpperCase()));
        ((TextureRegionDrawable) chest.getDrawable()).setRegion(new TextureRegion(enemyTextures.chestClosed));
        chest.setTouchable(Touchable.enabled);
        card.setDrawable(new TextureRegionDrawable(new TextureRegion(cardTextures.get(found))));
        card.setVisible(false);
        cardName.setText(found.getName());
        String description = found.getDescription().replace("\n", ". ");
        cardText.setText(description.endsWith(".") ? description : description + ".");
        copiesText.setText(reward.newCopy()
            ? Lang.f("NOUVELLE CARTE ! {0}/{1} EXEMPLAIRES", reward.copies(), PlayerProfile.MAX_COPIES)
            : Lang.f("DÉJÀ {0} EXEMPLAIRES : {1} PIÈCES EN PLUS",
                PlayerProfile.MAX_COPIES, PlayerProfile.formatCoins(PlayerProfile.DUPLICATE_COINS)));
        coinsText.setText(Lang.f("PIÈCES +{0}", PlayerProfile.formatCoins(reward.coins())));
        reelText.setText(earnedReel == null ? ""
            : Lang.f("NOUVEAU ROULEAU : {0} ! (TABLE DU CROUPIER)", earnedReel.getDisplayName()));
        earnedReel = null; // annoncé une seule fois, au premier coffre
        for (Label label : new Label[] {cardName, cardText, copiesText, coinsText, reelText}) label.setVisible(false);
        hint.setVisible(true);
        button = newButton;
        button.setVisible(false);
        root.addActor(button);

        root.setVisible(true);
        root.toFront();
        root.getColor().a = 0f;
        root.addAction(Actions.fadeIn(0.3f));
        layout();
        // Le coffre tremble en attendant qu'on l'ouvre.
        chest.addAction(Actions.forever(Actions.sequence(
            Actions.delay(0.9f),
            Actions.rotateBy(4f, 0.06f), Actions.rotateBy(-8f, 0.12f), Actions.rotateBy(4f, 0.06f))));
        hint.addAction(Actions.forever(Actions.sequence(Actions.alpha(0.4f, 0.6f), Actions.alpha(1f, 0.6f))));
    }

    /** Ouvre le coffre : la carte en sort, puis le texte, les pièces et le bouton. */
    private void open() {
        if (opened) return;
        opened = true;
        openSound.play();
        chest.clearActions();
        chest.setRotation(0f);
        chest.setTouchable(Touchable.disabled);
        ((TextureRegionDrawable) chest.getDrawable()).setRegion(new TextureRegion(enemyTextures.chestOpen));
        hint.clearActions();
        hint.setVisible(false);
        burst.at(chest.getX() + chest.getWidth() / 2f, chest.getY() + chest.getHeight() * 0.7f);

        // La carte jaillit du coffre jusqu'à sa place, en grandissant.
        float cardX = card.getX(), cardY = card.getY();
        card.setVisible(true);
        card.setOrigin(Align.center);
        card.setPosition(chest.getX() + (chest.getWidth() - CARD_WIDTH) / 2f, chest.getY() + chest.getHeight() / 2f);
        card.setScale(0.2f);
        card.getColor().a = 0f;
        card.addAction(Actions.parallel(
            Actions.fadeIn(0.25f),
            Actions.scaleTo(1f, 1f, 0.6f, Interpolation.swingOut),
            Actions.moveTo(cardX, cardY, 0.6f, Interpolation.pow2Out)));
        card.addAction(Actions.delay(0.7f, Actions.forever(Actions.sequence(
            Actions.moveBy(0f, 6f, 1.1f, Interpolation.sine), Actions.moveBy(0f, -6f, 1.1f, Interpolation.sine)))));

        Label[] texts = {cardName, cardText, copiesText, coinsText, reelText};
        for (int i = 0; i < texts.length; i++) {
            Label label = texts[i];
            label.getColor().a = 0f;
            label.addAction(Actions.delay(0.5f + i * 0.2f, Actions.sequence(Actions.visible(true), Actions.fadeIn(0.3f))));
        }
        coinsText.addAction(Actions.delay(1.1f, Actions.run(coinsSound::play)));
        button.getColor().a = 0f;
        button.addAction(Actions.delay(1.4f, Actions.sequence(Actions.visible(true), Actions.fadeIn(0.3f))));
    }

    /** Ferme le coffre. */
    public void hide() {
        root.clearActions();
        chest.clearActions();
        chest.setRotation(0f);
        card.clearActions();
        hint.clearActions();
        root.setVisible(false);
        if (button != null) button.remove();
        button = null;
    }

    /** Replace les éléments selon la taille de l'écran. */
    public void layout() {
        float width  = stage.getViewport().getWorldWidth();
        float height = stage.getViewport().getWorldHeight();
        veil.setBounds(0f, 0f, width, height);
        title.setBounds(0f, height * 0.86f, width, 80f);
        subtitle.setBounds(0f, height * 0.80f, width, 50f);

        Texture closed = enemyTextures.chestClosed;
        chest.setSize(closed.getWidth() * CHEST_SCALE, closed.getHeight() * CHEST_SCALE);
        chest.setOrigin(Align.bottom);
        chest.setPosition((width - chest.getWidth()) / 2f, height * 0.16f);
        hint.setBounds(0f, chest.getY() - 50f, width, 40f);

        // La carte au-dessus du coffre, à gauche ; ses textes à sa droite.
        float rowWidth = CARD_WIDTH + GAP + TEXT_WIDTH;
        float left = (width - rowWidth) / 2f;
        float cardY = chest.getTop() + 30f;
        if (!card.hasActions()) card.setBounds(left, cardY, CARD_WIDTH, CARD_HEIGHT);
        else card.setSize(CARD_WIDTH, CARD_HEIGHT);
        float textX = left + CARD_WIDTH + GAP;
        float top = cardY + CARD_HEIGHT;
        cardName.setWidth(TEXT_WIDTH);
        cardName.pack();
        cardName.setWidth(TEXT_WIDTH);
        cardName.setPosition(textX, top - cardName.getHeight());
        cardText.setWidth(TEXT_WIDTH);
        cardText.pack();
        cardText.setWidth(TEXT_WIDTH);
        cardText.setPosition(textX, cardName.getY() - cardText.getHeight() - 14f);
        copiesText.pack();
        copiesText.setPosition(textX, cardText.getY() - copiesText.getHeight() - 24f);
        coinsText.pack();
        coinsText.setPosition(textX, copiesText.getY() - coinsText.getHeight() - 8f);
        reelText.pack();
        reelText.setPosition(textX, coinsText.getY() - reelText.getHeight() - 8f);
        if (button != null) button.setPosition((width - button.getWidth()) / 2f, height * 0.04f);
    }

    @Override
    public void dispose() {
        Fonts.release(titleFont);
        Fonts.release(nameFont);
        Fonts.release(textFont);
        Fonts.release(rewardFont);
    }
}
