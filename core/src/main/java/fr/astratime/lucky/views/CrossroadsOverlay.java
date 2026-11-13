package fr.astratime.lucky.views;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
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
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.entities.exploration.Crossroads;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Le carrefour d'un donjon de l'Exploration, par-dessus le combat du soldat gagné :
 * <ul>
 *   <li>{@link #showDoors} : deux portes, chacune avec le nom de sa salle ; le joueur en ouvre une ;</li>
 *   <li>{@link #showRoom} : la salle ouverte, ce qui s'y passe (une image : carte ou symbole) et ses boutons.</li>
 * </ul>
 */
public class CrossroadsOverlay implements Disposable {

    private static final float DOOR_WIDTH   = 300f;
    private static final float DOOR_HEIGHT  = 470f;
    private static final float DOOR_GAP     = 90f;
    private static final float HOVER_LIFT   = 18f;
    private static final float PICTURE_SIZE = 150f;
    private static final float BUTTON_GAP   = 30f;

    private final Stage       stage;
    private final HudTextures hud;
    private final Sound       openSound;
    private final Sound       hoverSound;
    private final BitmapFont  titleFont = Fonts.jersey(72, Palette.GOLD, 5f, Palette.TEXT_SHADE);
    private final BitmapFont  textFont  = Fonts.jersey(34, Palette.CREAM, 2f, Palette.TEXT_SHADE);
    private final BitmapFont  nameFont  = Fonts.jersey(36, Palette.GOLD, 2f, Palette.TEXT_SHADE);
    private final BitmapFont  hintFont  = Fonts.jersey(24, Palette.CREAM, 2f, Palette.TEXT_SHADE);

    private final Group root = new Group();
    private final Image veil;
    private final Label title;
    private final Label subtitle;
    private final Label description;
    private final List<Group> doors = new ArrayList<>();
    private final List<TextButton> buttons = new ArrayList<>();
    private Image picture;
    private boolean opened;

    /**
     * @param openSound  bruitage d'une porte ouverte
     * @param hoverSound bruitage du survol d'une porte
     */
    public CrossroadsOverlay(Stage stage, HudTextures hud, Sound openSound, Sound hoverSound) {
        this.stage      = stage;
        this.hud        = hud;
        this.openSound  = openSound;
        this.hoverSound = hoverSound;
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

    /** @return le calque du carrefour, à ajouter au Stage au-dessus du jeu. */
    public Group getActor() { return root; }

    /** @return {@code true} tant que le carrefour est affiché. */
    public boolean isShown() { return root.isVisible(); }

    /**
     * Deux portes, une par salle de {@code rooms}.
     *
     * @param onOpen reçoit la salle de la porte ouverte (une seule fois)
     */
    public void showDoors(List<Crossroads.Room> rooms, Consumer<Crossroads.Room> onOpen) {
        open(Lang.t("CARREFOUR"), Lang.t("Deux portes avant le roi. Tu n'en ouvres qu'une."));
        opened = false;
        for (Crossroads.Room room : rooms) {
            Group door = buildDoor(room);
            door.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (opened || pointer != -1 || fromActor != null && fromActor.isDescendantOf(door)) return;
                    hoverSound.play();
                    door.clearActions();
                    door.addAction(Actions.moveTo(door.getX(), restY() + HOVER_LIFT, 0.12f, Interpolation.pow2Out));
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    if (opened || pointer != -1 || toActor != null && toActor.isDescendantOf(door)) return;
                    door.clearActions();
                    door.addAction(Actions.moveTo(door.getX(), restY(), 0.12f, Interpolation.pow2Out));
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (opened) return;
                    opened = true;
                    openSound.play();
                    doors.stream().filter(other -> other != door).forEach(other ->
                        other.addAction(Actions.alpha(0.25f, 0.3f)));
                    door.addAction(Actions.sequence(Actions.scaleTo(1.06f, 1.06f, 0.15f), Actions.scaleTo(1f, 1f, 0.15f),
                        Actions.delay(0.2f), Actions.run(() -> onOpen.accept(room))));
                }
            });
            doors.add(door);
            root.addActor(door);
        }
        layout();
    }

    /**
     * La salle ouverte : son nom, ce qui s'y passe et une image ({@code null} : aucune).
     * Ses boutons suivent avec {@link #setButtons}.
     */
    public void showRoom(Crossroads.Room room, String text, TextureRegion image) {
        open(room.getName().toUpperCase(), "");
        description.setText(text);
        if (image != null) {
            picture = new Image(new TextureRegionDrawable(image));
            float scale = Math.min(PICTURE_SIZE / image.getRegionWidth(), PICTURE_SIZE * 1.4f / image.getRegionHeight());
            picture.setSize(image.getRegionWidth() * scale, image.getRegionHeight() * scale);
            picture.setOrigin(picture.getWidth() / 2f, picture.getHeight() / 2f);
            picture.setScale(0.3f);
            picture.addAction(Actions.scaleTo(1f, 1f, 0.45f, Interpolation.swingOut));
            root.addActor(picture);
        }
        layout();
    }

    /** Ajoute les boutons de la salle (créés par l'écran de jeu, au style du casino). */
    public void setButtons(List<TextButton> newButtons) {
        buttons.forEach(Actor::remove);
        buttons.clear();
        for (TextButton button : newButtons) {
            buttons.add(button);
            root.addActor(button);
            button.getColor().a = 0f;
            button.addAction(Actions.delay(0.3f, Actions.fadeIn(0.3f)));
        }
        layout();
    }

    /** Ferme le carrefour. */
    public void hide() {
        root.clearActions();
        root.setVisible(false);
        clearContent();
    }

    /** Replace les éléments selon la taille de l'écran. */
    public void layout() {
        float width  = stage.getViewport().getWorldWidth();
        float height = stage.getViewport().getWorldHeight();
        veil.setBounds(0f, 0f, width, height);
        title.setBounds(0f, height * 0.84f, width, 80f);
        subtitle.setBounds(0f, height * 0.77f, width, 50f);
        float rowWidth = doors.size() * DOOR_WIDTH + Math.max(0, doors.size() - 1) * DOOR_GAP;
        float x = (width - rowWidth) / 2f;
        for (Group door : doors) {
            door.setPosition(x, restY());
            x += DOOR_WIDTH + DOOR_GAP;
        }
        if (picture != null) {
            picture.setPosition((width - picture.getWidth()) / 2f, height * 0.5f);
        }
        float descriptionWidth = Math.min(1000f, width - 80f);
        description.setBounds((width - descriptionWidth) / 2f, height * (doors.isEmpty() ? 0.3f : 0.17f),
            descriptionWidth, 120f);
        float buttonsWidth = 0f;
        for (TextButton button : buttons) buttonsWidth += button.getWidth();
        buttonsWidth += Math.max(0, buttons.size() - 1) * BUTTON_GAP;
        float bx = (width - buttonsWidth) / 2f;
        for (TextButton button : buttons) {
            button.setPosition(bx, height * 0.12f);
            bx += button.getWidth() + BUTTON_GAP;
        }
    }

    /** @return la hauteur des portes au repos. */
    private float restY() {
        return stage.getViewport().getWorldHeight() * 0.47f - DOOR_HEIGHT / 2f;
    }

    private void open(String titleText, String subtitleText) {
        clearContent();
        title.setText(titleText);
        subtitle.setText(subtitleText);
        description.setText("");
        if (!root.isVisible()) {
            root.setVisible(true);
            root.getColor().a = 0f;
            root.addAction(Actions.fadeIn(0.3f));
        }
        root.toFront();
    }

    private void clearContent() {
        doors.forEach(Actor::remove);
        doors.clear();
        buttons.forEach(Actor::remove);
        buttons.clear();
        if (picture != null) picture.remove();
        picture = null;
    }

    /**
     * Une porte de bois dans son cadre, sous une plaque : le nom de la salle,
     * et ce qu'elle fait en petit en dessous.
     */
    private Group buildDoor(Crossroads.Room room) {
        Group door = new Group();
        door.setSize(DOOR_WIDTH, DOOR_HEIGHT);
        door.setOrigin(DOOR_WIDTH / 2f, DOOR_HEIGHT / 2f);
        door.setTransform(true);

        Image frame = new Image(hud.panelDrawable());
        frame.setSize(DOOR_WIDTH, DOOR_HEIGHT);
        door.addActor(frame);

        // La porte : cadre sombre, battant de bois, planches, ferrures et poignée dorée.
        float doorW = 140f, doorH = 190f;
        float dx = (DOOR_WIDTH - doorW) / 2f, dy = DOOR_HEIGHT - doorH - 30f;
        door.addActor(block(dx - 10f, dy - 4f, doorW + 20f, doorH + 14f, Palette.OUTLINE));
        door.addActor(block(dx - 6f, dy, doorW + 12f, doorH + 6f, Palette.WOOD_DARK));
        door.addActor(block(dx, dy, doorW, doorH, Palette.WOOD));
        for (int plank = 1; plank < 4; plank++) {
            door.addActor(block(dx + plank * doorW / 4f - 2f, dy, 4f, doorH, Palette.WOOD_DARK));
        }
        door.addActor(block(dx + 4f, dy + doorH * 0.75f, doorW - 8f, 10f, Palette.IRON));
        door.addActor(block(dx + 4f, dy + doorH * 0.2f, doorW - 8f, 10f, Palette.IRON));
        door.addActor(block(dx + doorW - 30f, dy + doorH * 0.45f, 14f, 14f, Palette.GOLD));
        door.addActor(block(dx + doorW / 2f - 3f, dy + doorH - 34f, 6f, 6f, Palette.OUTLINE)); // judas
        for (Actor part : door.getChildren()) part.setTouchable(Touchable.disabled);

        Label name = new Label(room.getName(), new Label.LabelStyle(nameFont, Color.WHITE));
        name.setAlignment(Align.center);
        float space = DOOR_WIDTH - 28f; // un nom trop long rétrécit pour tenir sur une ligne
        if (name.getPrefWidth() > space) name.setFontScale(space / name.getPrefWidth());
        name.setBounds(14f, dy - 62f, space, 48f);
        name.setTouchable(Touchable.disabled);
        Label hint = new Label(room.getHint(), new Label.LabelStyle(hintFont, Color.WHITE));
        hint.setAlignment(Align.top);
        hint.setWrap(true);
        hint.setBounds(18f, 14f, DOOR_WIDTH - 36f, dy - 80f);
        hint.setTouchable(Touchable.disabled);
        door.addActor(name);
        door.addActor(hint);
        return door;
    }

    private Image block(float x, float y, float w, float h, Color color) {
        Image image = new Image(new TextureRegionDrawable(new TextureRegion(hud.pixel)));
        image.setBounds(x, y, w, h);
        image.setColor(color);
        return image;
    }

    @Override
    public void dispose() {
        Fonts.release(titleFont);
        Fonts.release(textFont);
        Fonts.release(nameFont);
        Fonts.release(hintFont);
    }
}
