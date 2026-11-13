package fr.astratime.lucky.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.input.FocusNavigation.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Manette (Xbox, PlayStation, Steam Deck…) : elle pilote le pointeur de la
 * souris, si bien que tout ce qui se fait à la souris se fait aussi à la
 * manette, sur tous les écrans, sans que chaque écran ait à la connaître.
 * <ul>
 *   <li>croix ou stick gauche : le pointeur saute sur l'élément cliquable le plus proche dans
 *       cette direction (un cadre doré le montre, son infobulle s'ouvre) ; s'il n'y en a pas,
 *       la flèche du clavier part à l'écran (réglage gauche/droite des options…) ;</li>
 *   <li>stick droit : déplace le pointeur librement ;</li>
 *   <li>A (Croix) : clic gauche ; Entrée sur une cinématique (levier du duel) ou quand le clic
 *       ne sert à rien ;</li>
 *   <li>X (Carré) : clic droit (fiche d'une carte…) ;</li>
 *   <li>B (Rond) et Start (Options) : Échap (retour, fermer, menu pause) ;</li>
 *   <li>Y (Triangle), LB, RB : touches {@link Input.Keys#BUTTON_Y}, {@link Input.Keys#BUTTON_L1},
 *       {@link Input.Keys#BUTTON_R1} pour l'écran (en combat : lancer, Mise, échoppe) ; LB/RB
 *       deviennent Page haut/bas si l'écran ne s'en sert pas (onglets des options) ;</li>
 *   <li>gâchettes LT/RT : molette (listes, bloc des effets).</li>
 * </ul>
 * Les manettes sont lues à chaque image ({@link #update}) ; le cadre de l'élément choisi
 * est dessiné par-dessus l'écran ({@link #draw}).
 */
public final class GamepadInput implements Disposable {

    /** Les boutons utiles, lus sur toutes les manettes branchées (la première qui appuie gagne). */
    private enum Pad { A, B, X, Y, START, L1, R1, UP, DOWN, LEFT, RIGHT }

    /** Stick poussé au-delà de ce seuil : il compte comme la croix. */
    private static final float STICK_PRESS = 0.55f;
    private static final float DEAD_ZONE   = 0.2f;
    /** Croix maintenue : première répétition puis cadence (secondes). */
    private static final float REPEAT_DELAY = 0.38f, REPEAT_EVERY = 0.12f;
    /** Gâchette maintenue : un cran de molette toutes les… (secondes). */
    private static final float SCROLL_EVERY = 0.09f;
    /** Vitesse du pointeur au stick droit, en hauteurs d'écran par seconde (stick à fond). */
    private static final float POINTER_SPEED = 1.1f;
    /** Pas du pointeur (pixels) quand la croix le pousse pendant un glisser (A maintenu). */
    private static final float DRAG_STEP = 28f;
    /** Un élément plus grand que cette part de l'écran est un fond (voile d'une fenêtre, cinématique), pas un bouton. */
    private static final float MAX_TARGET_SHARE = 0.45f;
    /** Axes des gâchettes de la manette ordinateur (Jamepad : LEFTX, LEFTY, RIGHTX, RIGHTY, TRIGGERLEFT, TRIGGERRIGHT). */
    private static final int AXIS_TRIGGER_LEFT = 4, AXIS_TRIGGER_RIGHT = 5;
    private static final Color FRAME = new Color(1f, 0.82f, 0.25f, 1f);

    private final BooleanSupplier enabled;
    private final boolean[] down = new boolean[Pad.values().length];
    private final boolean[] was  = new boolean[Pad.values().length];
    /** {@code false} si les manettes n'ont pas pu être lues (pas de bibliothèque native) : on n'essaie plus. */
    private boolean available = true;

    private Direction held;
    private float repeatIn;
    private float scrollIn;

    /** Position du pointeur (coordonnées de la fenêtre, y vers le bas), et la dernière posée par la manette. */
    private float pointerX = -1f, pointerY = -1f;
    private int placedX = Integer.MIN_VALUE, placedY = Integer.MIN_VALUE;

    /** Élément choisi à la croix (encadré), ou {@code null}. */
    private Actor focus;
    /** A a été pressé comme un clic (relâcher = fin du clic), X comme un clic droit. */
    private boolean leftPressed, rightPressed;

    private SpriteBatch batch;
    private Texture white;
    private final Matrix4 projection = new Matrix4();
    private float time;

    /** @param enabled lu à chaque image : {@code false} = manette ignorée (réglage des options) */
    public GamepadInput(BooleanSupplier enabled) {
        this.enabled = enabled;
    }

    // -------------------------------------------------------------------------
    // Lecture des manettes
    // -------------------------------------------------------------------------

    /** Lit les manettes et agit sur l'écran actif. À appeler une fois par image. */
    public void update(float delta) {
        time += delta;
        if (!available) return;
        if (!enabled.getAsBoolean()) {
            release();
            return;
        }
        Array<Controller> pads;
        try {
            pads = Controllers.getControllers();
        } catch (Throwable t) { // pas de manettes sur cette machine (bibliothèque native absente…)
            available = false;
            Gdx.app.log("Manette", "manettes indisponibles : " + t);
            return;
        }
        System.arraycopy(down, 0, was, 0, down.length);
        java.util.Arrays.fill(down, false);
        float rightX = 0f, rightY = 0f, scroll = 0f;
        for (Controller pad : pads) {
            if (!pad.isConnected()) continue;
            ControllerMapping m = pad.getMapping();
            read(pad, m.buttonA, Pad.A);
            read(pad, m.buttonB, Pad.B);
            read(pad, m.buttonX, Pad.X);
            read(pad, m.buttonY, Pad.Y);
            read(pad, m.buttonStart, Pad.START);
            read(pad, m.buttonL1, Pad.L1);
            read(pad, m.buttonR1, Pad.R1);
            read(pad, m.buttonDpadUp, Pad.UP);
            read(pad, m.buttonDpadDown, Pad.DOWN);
            read(pad, m.buttonDpadLeft, Pad.LEFT);
            read(pad, m.buttonDpadRight, Pad.RIGHT);
            float lx = axis(pad, m.axisLeftX), ly = axis(pad, m.axisLeftY);
            if (Math.abs(lx) >= STICK_PRESS && Math.abs(lx) >= Math.abs(ly)) down[(lx < 0 ? Pad.LEFT : Pad.RIGHT).ordinal()] = true;
            else if (Math.abs(ly) >= STICK_PRESS) down[(ly < 0 ? Pad.UP : Pad.DOWN).ordinal()] = true;
            float rx = axis(pad, m.axisRightX), ry = axis(pad, m.axisRightY);
            if (Math.abs(rx) > Math.abs(rightX)) rightX = rx;
            if (Math.abs(ry) > Math.abs(rightY)) rightY = ry;
            if (pad.getAxisCount() > AXIS_TRIGGER_RIGHT) {
                scroll += Math.max(0f, pad.getAxis(AXIS_TRIGGER_RIGHT)) - Math.max(0f, pad.getAxis(AXIS_TRIGGER_LEFT));
            }
        }
        InputProcessor input = Gdx.input.getInputProcessor();
        if (input == null) return;
        syncWithMouse();
        Stage stage = findStage(input);
        // (son infobulle peut le couvrir : on ne refait pas le test du clic, seulement « toujours affiché »)
        if (focus != null && (stage == null || focus.getStage() != stage || !visibleChain(focus))) focus = null;

        // Boutons : à l'appui (et au relâchement pour les clics)
        if (pressed(Pad.A)) pressA(input, stage);
        if (releasedNow(Pad.A) && leftPressed) {
            leftPressed = false;
            input.touchUp(px(), py(), 0, Input.Buttons.LEFT);
        }
        if (pressed(Pad.X)) {
            rightPressed = true;
            input.touchDown(px(), py(), 0, Input.Buttons.RIGHT);
        }
        if (releasedNow(Pad.X) && rightPressed) {
            rightPressed = false;
            input.touchUp(px(), py(), 0, Input.Buttons.RIGHT);
        }
        if (pressed(Pad.B) || pressed(Pad.START)) key(input, Input.Keys.ESCAPE);
        if (pressed(Pad.Y)) key(input, Input.Keys.BUTTON_Y);
        if (pressed(Pad.L1) && !key(input, Input.Keys.BUTTON_L1)) key(input, Input.Keys.PAGE_UP);
        if (pressed(Pad.R1) && !key(input, Input.Keys.BUTTON_R1)) key(input, Input.Keys.PAGE_DOWN);
        // Un bouton a pu changer d'écran : on relit le processeur pour la suite.
        input = Gdx.input.getInputProcessor();
        if (input == null) return;
        stage = findStage(input);

        // Croix / stick gauche, avec répétition quand on la maintient
        Direction direction = direction();
        if (direction == null) {
            held = null;
        } else if (direction != held) {
            held = direction;
            repeatIn = REPEAT_DELAY;
            move(input, stage, direction);
        } else if ((repeatIn -= delta) <= 0f) {
            repeatIn = REPEAT_EVERY;
            move(input, stage, direction);
        }

        // Stick droit : pointeur libre
        if (Math.abs(rightX) > DEAD_ZONE || Math.abs(rightY) > DEAD_ZONE) {
            float speed = POINTER_SPEED * Gdx.graphics.getHeight() * delta;
            float sx = Math.signum(rightX) * curve(rightX), sy = Math.signum(rightY) * curve(rightY);
            focus = null;
            placePointer(input, pointerX + sx * speed, pointerY + sy * speed);
        }

        // Gâchettes : molette
        if (Math.abs(scroll) > DEAD_ZONE) {
            if ((scrollIn -= delta) <= 0f) {
                scrollIn = SCROLL_EVERY;
                input.scrolled(0f, Math.signum(scroll));
            }
        } else {
            scrollIn = 0f;
        }
    }

    private void read(Controller pad, int button, Pad as) {
        if (button != ControllerMapping.UNDEFINED && pad.getButton(button)) down[as.ordinal()] = true;
    }

    private static float axis(Controller pad, int axis) {
        return axis == ControllerMapping.UNDEFINED ? 0f : pad.getAxis(axis);
    }

    /** Courbe du stick : précis près du centre, rapide à fond. */
    private static float curve(float value) {
        float v = (Math.abs(value) - DEAD_ZONE) / (1f - DEAD_ZONE);
        return v <= 0f ? 0f : v * v;
    }

    private boolean pressed(Pad pad)     { return down[pad.ordinal()] && !was[pad.ordinal()]; }
    private boolean releasedNow(Pad pad) { return !down[pad.ordinal()] && was[pad.ordinal()]; }

    private Direction direction() {
        if (down[Pad.UP.ordinal()])    return Direction.UP;
        if (down[Pad.DOWN.ordinal()])  return Direction.DOWN;
        if (down[Pad.LEFT.ordinal()])  return Direction.LEFT;
        if (down[Pad.RIGHT.ordinal()]) return Direction.RIGHT;
        return null;
    }

    /** Manette désactivée : rien ne reste « appuyé ». */
    private void release() {
        InputProcessor input = Gdx.input.getInputProcessor();
        if (input != null && leftPressed)  input.touchUp(px(), py(), 0, Input.Buttons.LEFT);
        if (input != null && rightPressed) input.touchUp(px(), py(), 0, Input.Buttons.RIGHT);
        leftPressed = rightPressed = false;
        java.util.Arrays.fill(down, false);
        java.util.Arrays.fill(was, false);
        held = null;
        focus = null;
    }

    // -------------------------------------------------------------------------
    // Actions
    // -------------------------------------------------------------------------

    /**
     * Élément plein écran qui préfère recevoir Entrée plutôt qu'un clic quand on appuie sur A
     * (une cinématique : Entrée y tire le levier du duel, ou la passe).
     */
    public interface PrefersEnter { }

    /**
     * A : un clic si un élément cliquable est sous le pointeur. Sinon, sur une cinématique
     * ({@link PrefersEnter}) : Entrée ; ailleurs un clic (bulle du Croupier, fond d'une
     * fenêtre…), et Entrée si personne ne l'a pris.
     */
    private void pressA(InputProcessor input, Stage stage) {
        if (stage != null && !clickableAtPointer(stage) && prefersEnterAtPointer(stage)) {
            if (key(input, Input.Keys.ENTER)) return;
        }
        leftPressed = true;
        if (input.touchDown(px(), py(), 0, Input.Buttons.LEFT)) return;
        if (stage != null && clickableAtPointer(stage)) return;
        leftPressed = false;
        input.touchUp(px(), py(), 0, Input.Buttons.LEFT);
        key(input, Input.Keys.ENTER);
    }

    /** Envoie une touche à l'écran (appui puis relâchement). @return {@code true} si l'écran l'a utilisée */
    private static boolean key(InputProcessor input, int keycode) {
        boolean used = input.keyDown(keycode);
        input.keyUp(keycode);
        return used;
    }

    /** Croix : l'élément suivant dans cette direction, sinon la flèche du clavier. */
    private void move(InputProcessor input, Stage stage, Direction direction) {
        if (leftPressed) { // A maintenu : la croix fait glisser (levier, etc.)
            placePointer(input, pointerX + direction.dx * DRAG_STEP, pointerY - direction.dy * DRAG_STEP);
            return;
        }
        if (stage != null) {
            List<Actor> actors = new ArrayList<>();
            collect(stage.getRoot(), stage, actors);
            List<Rectangle> rects = new ArrayList<>(actors.size());
            for (Actor actor : actors) rects.add(bounds(actor));
            Rectangle from;
            if (focus != null) {
                from = bounds(focus);
            } else {
                Vector2 p = stage.screenToStageCoordinates(new Vector2(pointerX, pointerY));
                from = new Rectangle(p.x - 1f, p.y - 1f, 2f, 2f);
            }
            int index = FocusNavigation.pick(from, rects, direction);
            if (index < 0 && focus == null) { // premier appui, pointeur loin de tout : l'élément le plus proche
                index = FocusNavigation.nearest(from.x, from.y, rects);
            }
            if (index >= 0) {
                focusOn(input, stage, actors.get(index));
                return;
            }
        }
        key(input, switch (direction) {
            case UP -> Input.Keys.UP;
            case DOWN -> Input.Keys.DOWN;
            case LEFT -> Input.Keys.LEFT;
            case RIGHT -> Input.Keys.RIGHT;
        });
    }

    /** Pose le pointeur au centre de {@code actor} (en faisant défiler sa liste si besoin) et l'encadre. */
    private void focusOn(InputProcessor input, Stage stage, Actor actor) {
        scrollIntoView(actor);
        Vector2 center = actor.localToStageCoordinates(new Vector2(actor.getWidth() / 2f, actor.getHeight() / 2f));
        stage.stageToScreenCoordinates(center);
        focus = actor;
        placePointer(input, center.x, center.y);
    }

    /** Déplace le vrai pointeur (le jeu reçoit un « survol » comme avec la souris), et glisse si A est maintenu. */
    private void placePointer(InputProcessor input, float x, float y) {
        pointerX = MathUtils.clamp(x, 0f, Gdx.graphics.getWidth() - 1f);
        pointerY = MathUtils.clamp(y, 0f, Gdx.graphics.getHeight() - 1f);
        placedX = px();
        placedY = py();
        Gdx.input.setCursorPosition(placedX, placedY);
        if (leftPressed) input.touchDragged(placedX, placedY, 0);
    }

    /** La souris a bougé d'elle-même : la manette repart de là, sans cadre. */
    private void syncWithMouse() {
        int mx = Gdx.input.getX(), my = Gdx.input.getY();
        if (pointerX < 0f || Math.abs(mx - placedX) > 1 || Math.abs(my - placedY) > 1) {
            if (pointerX >= 0f) focus = null;
            pointerX = mx;
            pointerY = my;
            placedX = mx;
            placedY = my;
        }
    }

    private int px() { return Math.round(pointerX); }
    private int py() { return Math.round(pointerY); }

    // -------------------------------------------------------------------------
    // Éléments cliquables de l'écran
    // -------------------------------------------------------------------------

    /** @return la scène qui reçoit la souris (le processeur lui-même, ou la première d'un multiplexeur) */
    static Stage findStage(InputProcessor input) {
        if (input instanceof Stage stage) return stage;
        if (input instanceof InputMultiplexer multiplexer) {
            for (InputProcessor p : multiplexer.getProcessors()) {
                Stage stage = findStage(p);
                if (stage != null) return stage;
            }
        }
        return null;
    }

    private void collect(Group group, Stage stage, List<Actor> out) {
        for (Actor child : group.getChildren()) {
            if (!child.isVisible() || child.getTouchable() == Touchable.disabled) continue;
            if (isTarget(child, stage) && reachable(child, stage)) out.add(child);
            if (child instanceof Group g) collect(g, stage, out);
        }
    }

    /** @return {@code true} si l'élément réagit à la souris (clic, survol) et n'est ni un fond ni une liste. */
    private static boolean isTarget(Actor actor, Stage stage) {
        if (actor.getTouchable() != Touchable.enabled || actor instanceof ScrollPane) return false;
        if (actor instanceof Button button && button.isDisabled()) return false;
        if (!hasInputListener(actor)) return false;
        Rectangle r = bounds(actor);
        if (r.width < 4f || r.height < 4f) return false; // encore en train d'apparaître (échelle 0)
        float screen = stage.getViewport().getWorldWidth() * stage.getViewport().getWorldHeight();
        return r.width * r.height <= screen * MAX_TARGET_SHARE;
    }

    private static boolean hasInputListener(Actor actor) {
        for (EventListener listener : actor.getListeners()) {
            if (listener instanceof InputListener) return true;
        }
        return false;
    }

    /** @return {@code true} si un clic au centre de l'élément le toucherait bien (rien ne le couvre, il est à l'écran). */
    private static boolean reachable(Actor actor, Stage stage) {
        if (!visibleChain(actor)) return false;
        if (hitsCenter(actor, stage)) return true;
        ScrollPane pane = scrollPaneOf(actor); // caché par le défilement : on l'atteindra en faisant défiler
        return pane != null && hitsCenter(pane, stage);
    }

    private static boolean hitsCenter(Actor actor, Stage stage) {
        Vector2 c = actor.localToStageCoordinates(new Vector2(actor.getWidth() / 2f, actor.getHeight() / 2f));
        Vector2 screen = stage.stageToScreenCoordinates(new Vector2(c));
        if (screen.x < 0f || screen.y < 0f || screen.x >= Gdx.graphics.getWidth() || screen.y >= Gdx.graphics.getHeight()) {
            return false;
        }
        Actor hit = stage.hit(c.x, c.y, true);
        return hit != null && (hit == actor || actor.isAscendantOf(hit));
    }

    private static boolean visibleChain(Actor actor) {
        for (Actor a = actor; a != null; a = a.getParent()) {
            if (!a.isVisible()) return false;
        }
        return actor.getStage() != null;
    }

    private static ScrollPane scrollPaneOf(Actor actor) {
        for (Actor a = actor.getParent(); a != null; a = a.getParent()) {
            if (a instanceof ScrollPane pane) return pane;
        }
        return null;
    }

    private static void scrollIntoView(Actor actor) {
        ScrollPane pane = scrollPaneOf(actor);
        if (pane == null || pane.getActor() == null) return;
        Vector2 p = actor.localToActorCoordinates(pane.getActor(), new Vector2(0f, 0f));
        pane.scrollTo(p.x, p.y, actor.getWidth(), actor.getHeight());
        pane.updateVisualScroll();
        pane.validate();
    }

    /** @return {@code true} si le pointeur est sur un élément cliquable (ou dans l'un d'eux). */
    private boolean clickableAtPointer(Stage stage) {
        Vector2 p = stage.screenToStageCoordinates(new Vector2(pointerX, pointerY));
        for (Actor a = stage.hit(p.x, p.y, true); a != null; a = a.getParent()) {
            if (isTarget(a, stage)) return true;
        }
        return false;
    }

    private boolean prefersEnterAtPointer(Stage stage) {
        Vector2 p = stage.screenToStageCoordinates(new Vector2(pointerX, pointerY));
        for (Actor a = stage.hit(p.x, p.y, true); a != null; a = a.getParent()) {
            if (a instanceof PrefersEnter) return true;
        }
        return false;
    }

    /** @return le rectangle de l'élément dans la scène (y vers le haut), échelle et rotation comprises. */
    private static Rectangle bounds(Actor actor) {
        Vector2 a = actor.localToStageCoordinates(new Vector2(0f, 0f));
        Vector2 b = actor.localToStageCoordinates(new Vector2(actor.getWidth(), actor.getHeight()));
        return new Rectangle(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.abs(b.x - a.x), Math.abs(b.y - a.y));
    }

    // -------------------------------------------------------------------------
    // Cadre de l'élément choisi
    // -------------------------------------------------------------------------

    /** Dessine le cadre doré autour de l'élément choisi à la croix, par-dessus l'écran. */
    public void draw() {
        if (focus == null || focus.getStage() == null || !visibleChain(focus)) return;
        Stage stage = focus.getStage();
        if (batch == null) {
            batch = new SpriteBatch(16);
            Pixmap pixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixel.setColor(Color.WHITE);
            pixel.fill();
            white = new Texture(pixel);
            pixel.dispose();
        }
        Rectangle r = bounds(focus);
        Vector2 lo = stage.stageToScreenCoordinates(new Vector2(r.x, r.y));
        Vector2 hi = stage.stageToScreenCoordinates(new Vector2(r.x + r.width, r.y + r.height));
        int w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
        float pad = 4f, t = 3f;
        float x0 = Math.min(lo.x, hi.x) - pad, x1 = Math.max(lo.x, hi.x) + pad;
        float y0 = h - Math.max(lo.y, hi.y) - pad, y1 = h - Math.min(lo.y, hi.y) + pad; // fenêtre → y vers le haut
        HdpiUtils.glViewport(0, 0, w, h);
        projection.setToOrtho2D(0f, 0f, w, h);
        batch.setProjectionMatrix(projection);
        batch.begin();
        batch.setColor(FRAME.r, FRAME.g, FRAME.b, 0.65f + 0.35f * MathUtils.sin(time * 6f));
        batch.draw(white, x0, y0, x1 - x0, t);
        batch.draw(white, x0, y1 - t, x1 - x0, t);
        batch.draw(white, x0, y0, t, y1 - y0);
        batch.draw(white, x1 - t, y0, t, y1 - y0);
        batch.end();
        batch.setColor(Color.WHITE);
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (white != null) white.dispose();
    }
}
