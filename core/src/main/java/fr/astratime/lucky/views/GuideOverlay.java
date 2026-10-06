package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.Fonts;
import fr.astratime.lucky.assets.HudTextures;
import fr.astratime.lucky.assets.Palette;
import fr.astratime.lucky.i18n.Lang;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Le Croupier guide le joueur : une bulle (son portrait, sa réplique qui
 * s'écrit lettre à lettre) et, souvent, un voile sombre percé d'un « trou »
 * lumineux sur ce dont il parle. Joue une suite d'{@link Step étapes} :
 * <ul>
 *   <li>une réplique simple attend un clic n'importe où ;</li>
 *   <li>une réplique qui demande une action (jouer une carte, lancer la
 *       machine...) laisse passer les clics dans le trou seulement, et passe à
 *       la suite dès que sa condition est remplie ;</li>
 *   <li>une réplique discrète (sans voile) laisse tout l'écran au joueur, par
 *       exemple pendant que l'échoppe est ouverte.</li>
 * </ul>
 * Le bouton « Passer », s'il y en a un, reste en haut à droite.
 */
public class GuideOverlay extends Group implements Disposable {

    /** La réplique s'écrit à cette vitesse (lettres par seconde). */
    private static final float LETTERS     = 55f;
    private static final float TEXT_WIDTH  = 520f;
    private static final float PORTRAIT    = 96f;
    private static final float PAD         = 18f;
    private static final float GAP         = 16f;
    /** Écart entre la bulle et ce qu'elle montre, et marge au bord de l'écran. */
    private static final float BUBBLE_GAP  = 26f;
    private static final float MARGIN      = 16f;
    /** Le trou déborde autant autour de sa cible. */
    private static final float HOLE_PAD    = 10f;
    private static final float BORDER      = 4f;
    private static final float VEIL_ALPHA  = 0.6f;
    private static final float APPEAR      = 0.2f;

    /** Une réplique du Croupier. */
    public static final class Step {
        final String              text;
        final Supplier<Rectangle> target;
        final BooleanSupplier     done;
        final boolean             passive;
        Runnable                  onStart;
        final List<Actor>         buttons = new ArrayList<>();

        private Step(String text, Supplier<Rectangle> target, BooleanSupplier done, boolean passive) {
            this.text    = text;
            this.target  = target;
            this.done    = done;
            this.passive = passive;
        }

        /** Une réplique, sous un voile ; un clic passe à la suite. */
        public static Step say(String text) { return new Step(text, null, null, false); }

        /** Une réplique qui montre {@code target} (rectangle du Stage, recalculé à chaque image) ; un clic passe à la suite. */
        public static Step say(String text, Supplier<Rectangle> target) { return new Step(text, target, null, false); }

        /** Une réplique qui attend que le joueur agisse dans {@code target} : la suite vient quand {@code done} est vrai. */
        public static Step action(String text, Supplier<Rectangle> target, BooleanSupplier done) {
            return new Step(text, target, done, false);
        }

        /** Une réplique discrète, sans voile : tout l'écran reste au joueur jusqu'à ce que {@code done} soit vrai. */
        public static Step passive(String text, BooleanSupplier done) { return new Step(text, null, done, true); }

        /** {@code action} est lancé quand la réplique commence. @return cette réplique */
        public Step onStart(Runnable action) {
            this.onStart = action;
            return this;
        }

        /** Des boutons sous la réplique : ce sont eux qui décident de la suite (un clic ailleurs ne fait rien). @return cette réplique */
        public Step buttons(Actor... buttons) {
            this.buttons.addAll(List.of(buttons));
            return this;
        }

        /** @return {@code true} si la réplique attend que le joueur agisse (et non un simple clic). */
        public boolean waitsForAction() { return done != null; }
    }

    private final TextureRegion pixel;
    private final BitmapFont    textFont = Fonts.jersey(30, Palette.TEXT_BODY, 2f, Palette.TEXT_SHADE);
    private final BitmapFont    hintFont = Fonts.jersey(22, Palette.GOLD, 2f, Palette.TEXT_SHADE);
    private final Table         bubble   = new Table();
    private final Label         text;
    private final Label         hint;
    private Cell<Table>         buttonCell;
    private Cell<Label>         hintCell;
    private final Table         buttonRow = new Table();
    private final Cell<Label>   textCell;
    private Actor               skip;
    /** Coin haut gauche du bouton « Passer » (Stage) ; {@code null} : en haut à droite de l'écran. */
    private Vector2             skipCorner;

    private final List<Step> queue = new ArrayList<>();
    private Step       step;
    private Runnable   then;
    private Rectangle  hole;
    private float      typed;
    private float      time;

    /**
     * @param hud      cadre de la bulle (celui des infobulles) et pixel du voile
     * @param portrait portrait du Croupier, à gauche de sa réplique (non possédé ; {@code null} : aucun)
     */
    public GuideOverlay(HudTextures hud, Texture portrait) {
        pixel = new TextureRegion(hud.pixel);
        text  = new Label("", new Label.LabelStyle(textFont, Color.WHITE));
        text.setWrap(true);
        text.setAlignment(Align.topLeft);
        hint  = new Label("", new Label.LabelStyle(hintFont, Color.WHITE));
        hint.setAlignment(Align.right);

        bubble.setBackground(hud.tooltipDrawable());
        bubble.pad(PAD);
        bubble.setTouchable(Touchable.enabled);
        if (portrait != null) {
            float portraitWidth = PORTRAIT * portrait.getWidth() / portrait.getHeight();
            bubble.add(new Image(new TextureRegionDrawable(new TextureRegion(portrait))))
                .size(portraitWidth, PORTRAIT).top().padRight(GAP);
        }
        Table column = new Table();
        textCell = column.add(text).width(TEXT_WIDTH).left();
        column.row();
        buttonCell = column.add(buttonRow).padTop(GAP).left();
        column.row();
        hintCell = column.add(hint).growX().right().padTop(4f);
        bubble.add(column).top();
        addActor(bubble);

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Actor target = event.getTarget();
                if (buttonRow.isAscendantOf(target)) return; // les boutons décident eux-mêmes de la suite
                if (target != GuideOverlay.this && !bubble.isAscendantOf(target)) return;
                onClick();
            }
        });
        setTouchable(Touchable.enabled);
        setVisible(false);
    }

    /** Ajoute le bouton « Passer », en haut à droite, visible tant que le Croupier parle. */
    public void setSkipButton(Actor button) {
        if (skip != null) skip.remove();
        skip = button;
        addActor(button);
    }

    /** Place le bouton « Passer » sous {@code (x, top)} (Stage), au lieu du coin haut droit de l'écran. */
    public void setSkipCorner(float x, float top) {
        skipCorner = new Vector2(x, top);
    }

    /** Joue {@code steps} dans l'ordre, puis {@code then} (rien : {@code null}). Remplace ce qui se jouait. */
    public void play(List<Step> steps, Runnable then) {
        queue.clear();
        queue.addAll(steps);
        this.then = then;
        next();
    }

    /** Arrête le Croupier et masque le voile, sans lancer la suite. */
    public void stop() {
        queue.clear();
        step = null;
        then = null;
        setVisible(false);
    }

    /** @return {@code true} tant que le Croupier parle. */
    public boolean isActive() { return step != null; }

    /** @return la réplique en cours, ou {@code null}. */
    public Step getStep() { return step; }

    /** Passe à la réplique suivante, ou termine (le voile disparaît, puis la suite). */
    public void next() {
        if (queue.isEmpty()) {
            step = null;
            setVisible(false);
            Runnable done = then;
            then = null;
            if (done != null) done.run();
            return;
        }
        step = queue.remove(0);
        typed = 0f;
        text.setText(step.text);
        buttonRow.clearChildren();
        for (Actor button : step.buttons) buttonRow.add(button).size(button.getWidth(), button.getHeight()).padRight(GAP);
        boolean clickStep = step.done == null && step.buttons.isEmpty();
        hint.setText(clickStep ? Lang.t("Clic pour continuer") : "");
        // Les lignes vides (pas de boutons, pas d'indice) ne laissent pas de blanc dans la bulle.
        buttonCell.padTop(step.buttons.isEmpty() ? 0f : GAP);
        hintCell.padTop(clickStep ? 4f : 0f).height(clickStep ? hint.getPrefHeight() : 0f);
        // La bulle prend la taille de la réplique entière : elle ne bouge plus pendant qu'elle s'écrit.
        textCell.height(textHeight(step.text));
        bubble.pack();
        text.setText("");
        setVisible(true);
        bubble.getColor().a = 0f;
        bubble.clearActions();
        bubble.addAction(Actions.fadeIn(APPEAR, Interpolation.pow2Out));
        if (step.onStart != null) step.onStart.run();
        place();
    }

    /** @return la hauteur de {@code full}, écrit sur la largeur de la bulle. */
    private float textHeight(String full) {
        text.setText(full);
        text.setWidth(TEXT_WIDTH);
        text.invalidate();
        return text.getPrefHeight();
    }

    /** Un clic : la réplique s'écrit d'un coup, ou (déjà écrite) le Croupier passe à la suivante. */
    private void onClick() {
        if (step == null) return;
        if (typed < step.text.length()) {
            typed = step.text.length();
            return;
        }
        if (step.done == null && step.buttons.isEmpty()) next();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (step == null) return;
        time += delta;
        if (typed < step.text.length()) {
            typed = Math.min(step.text.length(), typed + LETTERS * delta);
            text.setText(step.text.substring(0, (int) typed));
        }
        place();
        if (step.done != null && step.done.getAsBoolean()) next();
    }

    /** Recalcule le trou (sa cible a pu bouger) et place la bulle à côté, le bouton « Passer » en haut à droite. */
    private void place() {
        if (getStage() == null || step == null) return;
        float width  = getStage().getViewport().getWorldWidth();
        float height = getStage().getViewport().getWorldHeight();
        setBounds(0f, 0f, width, height);
        Rectangle target = step.target != null ? step.target.get() : null;
        hole = target == null ? null : new Rectangle(target.x - HOLE_PAD, target.y - HOLE_PAD,
            target.width + HOLE_PAD * 2f, target.height + HOLE_PAD * 2f);

        float w = bubble.getWidth(), h = bubble.getHeight();
        float x, y;
        if (hole == null) {
            x = (width - w) / 2f;
            y = step.passive ? MARGIN * 2f : height * 0.55f - h / 2f;
        } else {
            x = hole.x + hole.width / 2f - w / 2f;
            boolean above = hole.y + hole.height / 2f < height / 2f;
            y = above ? hole.y + hole.height + BUBBLE_GAP : hole.y - BUBBLE_GAP - h;
            // Pas la place au-dessus ou en dessous : à côté de la cible.
            if (y < MARGIN || y + h > height - MARGIN) {
                y = hole.y + hole.height / 2f - h / 2f;
                x = hole.x + hole.width + BUBBLE_GAP;
                if (x + w > width - MARGIN) x = hole.x - BUBBLE_GAP - w;
            }
        }
        bubble.setPosition(MathUtils.clamp(x, MARGIN, width - w - MARGIN), MathUtils.clamp(y, MARGIN, height - h - MARGIN));
        if (skip != null && skipCorner != null) skip.setPosition(skipCorner.x, skipCorner.y - skip.getHeight());
        else if (skip != null) skip.setPosition(width - skip.getWidth() - MARGIN, height - skip.getHeight() - MARGIN);
        // La bulle ne cache pas le bouton « Passer » : elle se pousse à sa gauche, sinon dessous.
        if (skip != null && bubble.getX() < skip.getX() + skip.getWidth() && bubble.getX() + w > skip.getX()
                && bubble.getY() < skip.getY() + skip.getHeight() && bubble.getY() + h > skip.getY()) {
            if (skip.getX() - BUBBLE_GAP - w >= MARGIN) bubble.setX(skip.getX() - BUBBLE_GAP - w);
            else bubble.setY(Math.max(MARGIN, skip.getY() - BUBBLE_GAP - h));
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (step != null && !step.passive) drawVeil(batch, parentAlpha);
        super.draw(batch, parentAlpha);
    }

    /** Le voile, autour du trou, et un liseré doré qui palpite sur son bord. */
    private void drawVeil(Batch batch, float parentAlpha) {
        Color old = batch.getColor().cpy();
        float width = getWidth(), height = getHeight();
        batch.setColor(0f, 0f, 0f, VEIL_ALPHA * parentAlpha);
        if (hole == null) {
            batch.draw(pixel, 0f, 0f, width, height);
        } else {
            float left = Math.max(0f, hole.x), right = Math.min(width, hole.x + hole.width);
            float bottom = Math.max(0f, hole.y), top = Math.min(height, hole.y + hole.height);
            batch.draw(pixel, 0f, 0f, left, height);
            batch.draw(pixel, right, 0f, width - right, height);
            batch.draw(pixel, left, 0f, right - left, bottom);
            batch.draw(pixel, left, top, right - left, height - top);
            float glow = 0.55f + 0.45f * MathUtils.sin(time * 5f);
            batch.setColor(Palette.GOLD.r, Palette.GOLD.g, Palette.GOLD.b, glow * parentAlpha);
            batch.draw(pixel, left - BORDER, bottom - BORDER, right - left + BORDER * 2f, BORDER);
            batch.draw(pixel, left - BORDER, top, right - left + BORDER * 2f, BORDER);
            batch.draw(pixel, left - BORDER, bottom, BORDER, top - bottom);
            batch.draw(pixel, right, bottom, BORDER, top - bottom);
        }
        batch.setColor(old);
    }

    /**
     * La bulle et le bouton « Passer » restent cliquables. Ailleurs, une
     * réplique simple prend tous les clics (ils la font avancer) ; une réplique
     * qui attend une action laisse passer ceux du trou ; une réplique discrète
     * laisse passer tous les clics.
     */
    @Override
    public Actor hit(float x, float y, boolean touchable) {
        if (!isVisible() || step == null) return null;
        Actor child = super.hit(x, y, touchable);
        if (child != null && child != this) return child;
        if (step.passive) return null;
        if (step.done != null && hole != null && hole.contains(x, y)) return null;
        return this;
    }

    /** @return le rectangle (Stage) occupé par {@code actor}, ou {@code null} s'il n'est pas affiché. */
    public static Rectangle boundsOf(Actor actor) {
        if (actor == null || actor.getStage() == null) return null;
        Vector2 corner = actor.localToStageCoordinates(new Vector2(0f, 0f));
        return new Rectangle(corner.x, corner.y, actor.getWidth() * actor.getScaleX(), actor.getHeight() * actor.getScaleY());
    }

    /** @return le carré (Stage) de côté {@code size} centré sur {@code center}. */
    public static Rectangle around(Vector2 center, float width, float height) {
        return new Rectangle(center.x - width / 2f, center.y - height / 2f, width, height);
    }

    @Override
    public void dispose() {
        Fonts.release(textFont);
        Fonts.release(hintFont);
    }
}
