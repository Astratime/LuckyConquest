package fr.astratime.lucky.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Disposable;
import fr.astratime.lucky.assets.EnemyTextures;
import fr.astratime.lucky.entities.Card;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.enemy.EnemyKind;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Vue de face du combat : le décor du lieu, et l'ennemi en grand, debout
 * derrière la table vue en perspective ({@link TableProjection}).
 *
 * <p>L'ennemi est son portrait de la vue classique, agrandi, et animé ici : il
 * respire (la tête un peu plus que le corps), se balance doucement, tourne la
 * tête de temps en temps et cligne des yeux ({@link EnemyTextures#portraitBlink}).
 * Il recopie aussi les gestes de son petit portrait ({@link EnemyView}, caché en
 * vue de face) : il tremble et flashe en blanc quand il est touché, luit en vert
 * quand il se soigne, hoche la tête, se penche pour frapper et bascule, vaincu.
 * Chaque ennemi a en plus son animation à lui ({@link EnemyIdle}) : le clown
 * jongle, la roue tourne, le rat renifle...
 */
public class FrontScene extends Actor implements Disposable {

    /** Décor derrière l'ennemi. */
    public enum Decor {
        CALE("front/decor_cale.png"),
        PRAIRIE_PIQUE("front/decor_prairie_pique.png"),
        PRAIRIE_TREFLE("front/decor_prairie_trefle.png"),
        PRAIRIE_COEUR("front/decor_prairie_coeur.png"),
        PRAIRIE_CARREAU("front/decor_prairie_carreau.png"),
        CASINO("menu/casino_interior.png");

        final String path;

        Decor(String path) { this.path = path; }

        /** @return le décor de la Prairie pour un donjon de la couleur {@code suit}. */
        public static Decor prairie(Card.Suit suit) {
            return switch (suit) {
                case PIQUE   -> PRAIRIE_PIQUE;
                case TREFLE  -> PRAIRIE_TREFLE;
                case COEUR   -> PRAIRIE_COEUR;
                case CARREAU -> PRAIRIE_CARREAU;
            };
        }
    }

    /** Place laissée en haut de l'écran (barre de vie de l'ennemi), pour régler la taille de l'ennemi. */
    private static final float TOP_ROOM        = 40f;
    /** Part de la hauteur de l'ennemi cachée derrière le rebord de la table. */
    private static final float HIDDEN_PART     = 0.06f;
    /** Respiration : amplitude (en pixels du portrait) et période, en secondes. */
    private static final float BREATH          = 0.6f;
    private static final float BREATH_PERIOD   = 2.6f;
    /** La tête (le haut du portrait) bouge plus que le corps ; elle déborde d'un peu sur le corps. */
    private static final float HEAD_PART       = 0.5f;
    private static final int   HEAD_OVERLAP    = 2;
    /** Balancement de tout le corps, en degrés, et sa période. */
    private static final float SWAY            = 1.2f;
    private static final float SWAY_PERIOD     = 5.3f;
    /** La tête se tourne d'un pixel : toutes les {@code LOOK_EVERY} secondes, pendant {@code LOOK_TIME}. */
    private static final float LOOK_EVERY      = 4.7f;
    private static final float LOOK_TIME       = 1.1f;
    /** Clignement des yeux : toutes les {@code BLINK_EVERY} secondes, pendant {@code BLINK_TIME}. */
    private static final float BLINK_EVERY     = 3.1f;
    private static final float BLINK_TIME      = 0.14f;
    /** Les gestes du petit portrait sont amplifiés d'autant sur le grand. */
    private static final float GESTURE_SCALE   = 2f;
    /** Lanternes de la Cale : centre de leur flamme, en pixels du décor (origine en haut à gauche). */
    private static final float[][] CALE_LANTERNS = {{128f, 30f}, {236f, 30f}};
    /** Décor du casino (portrait des autres lieux) : assombri pour que l'ennemi ressorte. */
    private static final Color CASINO_TINT     = new Color(0.55f, 0.5f, 0.55f, 1f);

    private final TableProjection projection;
    private final PlayArea        playArea;
    private final EnemyTextures   enemyTextures;
    private final EnemyView       enemyView;
    private final Supplier<Enemy> enemy;

    private final Texture glow = glowTexture();
    private final Texture white = whiteTexture();
    private final Map<Decor, Texture>     decors = new EnumMap<>(Decor.class);
    private final Map<EnemyKind, Optional<EnemyIdle>> idles = new EnumMap<>(EnemyKind.class);
    private final EnemyIdle.Pose pose = new EnemyIdle.Pose();
    private final Rows rows = new Rows();
    private final PortraitFx fx = new PortraitFx();

    private Decor decor = Decor.CASINO;
    private float time;

    // Placement de l'ennemi, recalculé à chaque image : coin bas gauche et taille d'un pixel du dessin.
    private float enemyX, enemyY, pixel;

    /**
     * @param enemyTextures portraits des ennemis (non possédés)
     * @param enemyView     le côté de l'ennemi sur la table, dont le grand ennemi recopie les gestes
     * @param enemy         l'ennemi du combat en cours
     */
    public FrontScene(TableProjection projection, PlayArea playArea, EnemyTextures enemyTextures, EnemyView enemyView,
                      Tooltip tooltip, Supplier<Enemy> enemy) {
        this.projection    = projection;
        this.playArea      = playArea;
        this.enemyTextures = enemyTextures;
        this.enemyView     = enemyView;
        this.enemy         = enemy;
        addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer != -1) return;
                tooltip.show(enemy.get().getName(), enemyView.describeEnemy(), getX() + getWidth() / 2f, getY() + getHeight());
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) tooltip.hide();
            }
        });
    }

    /** Choisit le décor du lieu du combat. */
    public void setDecor(Decor decor) { this.decor = decor; }

    @Override
    public void act(float delta) {
        super.act(delta);
        time += delta;
        boolean shown = projection.isEnabled();
        setVisible(shown);
        if (shown) place();
    }

    /** Taille et place de l'ennemi : debout derrière le bord de la table, la tête sous la barre de vie. */
    private void place() {
        Texture body = shownTexture();
        float worldHeight = getStage().getViewport().getWorldHeight();
        float room = worldHeight - TOP_ROOM - projection.getTopY();
        pixel = Math.max(2, Math.round(room / (body.getHeight() * (1f - HIDDEN_PART))));
        float width = body.getWidth() * pixel, height = body.getHeight() * pixel;
        float centerX = (projection.getTopLeftX() + projection.getTopRightX()) / 2f;
        enemyX = centerX - width / 2f;
        enemyY = projection.getTopY() - height * HIDDEN_PART;
        setBounds(enemyX, enemyY, width, height); // survol : son infobulle
    }

    /** @return le portrait de l'ennemi, qui donne sa taille. */
    private Texture shownTexture() {
        return enemyTextures.portrait(enemy.get().getKind());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        drawDecor(batch, parentAlpha);
        fx.batch = batch;
        Actor character = enemyView.getCharacter();
        Actor portrait  = enemyView.getPortrait();
        Color flash     = enemyView.getFlashColor();
        float alpha     = parentAlpha * character.getColor().a;
        float rotation  = character.getRotation();
        float breath    = MathUtils.sin(time * MathUtils.PI2 / BREATH_PERIOD) * BREATH * pixel;
        float dx = portrait.getX() * GESTURE_SCALE, dy = portrait.getY() * GESTURE_SCALE;
        float x = enemyX + dx, y = enemyY + dy;
        float originX = getWidth() / 2f; // vaincu, il bascule en arrière autour de ses pieds

        EnemyKind kind = enemy.get().getKind();
        Texture portraitTexture = enemyTextures.portrait(kind), flashTexture = enemyTextures.portraitFlash(kind);
        Texture blink = enemyTextures.portraitBlink(kind);
        boolean blinking = blink != null && time % BLINK_EVERY < BLINK_TIME;
        rotation += MathUtils.sin(time * MathUtils.PI2 / SWAY_PERIOD) * SWAY;

        // L'animation propre à l'ennemi : elle déplace tout le portrait, sa tête ou des bandes de rangées.
        EnemyIdle idle = idles.computeIfAbsent(kind, k -> Optional.ofNullable(EnemyIdle.of(k))).orElse(null);
        pose.reset();
        if (idle != null) idle.pose(time, pose);
        alpha *= pose.alpha;
        rotation += pose.rotation;
        x += pose.dx * pixel;
        y -= pose.dy * pixel;

        int height = portraitTexture.getHeight();
        int split = Math.round(height * HEAD_PART);
        float bodyY = y + breath * 0.5f;
        float headX = x + (lookOffset() + pose.headDx) * pixel, headY = y + breath - pose.headDy * pixel;
        float feetX = x + originX;
        fx.set(x, bodyY, feetX, bodyY, rotation, alpha, height);
        if (idle != null) idle.drawBack(fx, time);

        // Le corps, puis la tête par-dessus, qui respire plus fort et se tourne de temps en temps ; les bandes à part.
        rows.clear();
        rows.add(0, height);
        for (EnemyIdle.Pose.Band band : pose.bands) rows.remove(band.top, band.bottom);
        Texture head = blinking ? blink : portraitTexture;
        for (int i = 0; i < rows.size(); i += 2) {
            int top = Math.max(rows.get(i), split), bottom = rows.get(i + 1);
            if (top < bottom) drawRows(batch, portraitTexture, flashTexture, top, bottom, x, bodyY, feetX, bodyY, rotation, alpha, flash);
        }
        for (int i = 0; i < rows.size(); i += 2) {
            int top = rows.get(i), bottom = Math.min(rows.get(i + 1), split + HEAD_OVERLAP);
            if (top < bottom) drawRows(batch, head, flashTexture, top, bottom, headX, headY, feetX, headY, rotation, alpha, flash);
        }
        for (EnemyIdle.Pose.Band band : pose.bands) {
            boolean inHead = band.top < split;
            float bx = (inHead ? headX : x) + band.dx * pixel, by = (inHead ? headY : bodyY) - band.dy * pixel;
            // Le pivot de la bande suit la rotation de tout le corps autour des pieds, puis la bande tourne sur lui.
            float px = bx + band.pivotX * pixel, py = by + (height - band.pivotY) * pixel;
            float cos = MathUtils.cosDeg(rotation), sin = MathUtils.sinDeg(rotation);
            float rx = feetX + (px - feetX) * cos - (py - bodyY) * sin, ry = bodyY + (px - feetX) * sin + (py - bodyY) * cos;
            drawRows(batch, inHead ? head : portraitTexture, flashTexture, band.top, band.bottom, bx + rx - px, by + ry - py,
                rx, ry, rotation + band.rotation, alpha, flash);
        }
        if (idle != null) idle.drawFront(fx, time);
        batch.setColor(Color.WHITE);
    }

    /** @return le décalage de la tête qui se tourne (en pixels du portrait) : vers la gauche, puis la droite. */
    private float lookOffset() {
        float t = time % (LOOK_EVERY * 2f);
        float side = t < LOOK_EVERY ? -1f : 1f;
        t %= LOOK_EVERY;
        if (t > LOOK_TIME) return 0f;
        return side * MathUtils.sin(t / LOOK_TIME * MathUtils.PI);
    }

    /**
     * Les rangées {@code [top, bottom)} du portrait (comptées depuis le haut) à
     * leur place quand le portrait a son coin bas gauche en {@code (x, y)},
     * tournées autour du point {@code (pivotX, pivotY)} de l'écran, puis la même
     * tranche de sa silhouette quand il flashe.
     */
    private void drawRows(Batch batch, Texture texture, Texture silhouette, int top, int bottom, float x, float y,
                          float pivotX, float pivotY, float rotation, float alpha, Color flash) {
        int rows = Math.min(bottom, texture.getHeight()) - top;
        if (rows <= 0) return;
        float width = texture.getWidth() * pixel, height = rows * pixel;
        float sliceY = y + (texture.getHeight() - top - rows) * pixel;
        float originX = pivotX - x, originY = pivotY - sliceY;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(texture, x, sliceY, originX, originY, width, height, 1f, 1f, rotation,
            0, top, texture.getWidth(), rows, false, false);
        if (silhouette != null && flash.a > 0f) {
            batch.setColor(flash.r, flash.g, flash.b, flash.a * alpha);
            batch.draw(silhouette, x, sliceY, originX, originY, width, height, 1f, 1f, rotation,
                0, top, silhouette.getWidth(), rows, false, false);
        }
    }

    /** Les rangées encore dessinées d'un bloc : des paires {@code [top, bottom)}, d'où on retire celles des bandes. */
    private static final class Rows {
        private final List<Integer> bounds = new ArrayList<>();

        void clear() { bounds.clear(); }

        void add(int top, int bottom) {
            bounds.add(top);
            bounds.add(bottom);
        }

        void remove(int top, int bottom) {
            List<Integer> kept = new ArrayList<>();
            for (int i = 0; i < bounds.size(); i += 2) {
                int a = bounds.get(i), b = bounds.get(i + 1);
                if (top > a) { kept.add(a); kept.add(Math.min(b, top)); }
                if (bottom < b) { kept.add(Math.max(a, bottom)); kept.add(b); }
            }
            bounds.clear();
            for (int i = 0; i < kept.size(); i += 2) if (kept.get(i) < kept.get(i + 1)) add(kept.get(i), kept.get(i + 1));
        }

        int size() { return bounds.size(); }

        int get(int i) { return bounds.get(i); }
    }

    /**
     * Les dessins des animations : en pixels du portrait (origine en haut à
     * gauche), placés comme le corps de l'ennemi et tournés avec lui.
     */
    private final class PortraitFx implements EnemyIdle.Fx {
        private Batch batch;
        private float x, y, feetX, feetY, cos, sin, alpha;
        private int height;

        void set(float x, float y, float feetX, float feetY, float rotation, float alpha, int height) {
            this.x = x;
            this.y = y;
            this.feetX = feetX;
            this.feetY = feetY;
            this.cos = MathUtils.cosDeg(rotation);
            this.sin = MathUtils.sinDeg(rotation);
            this.alpha = alpha;
            this.height = height;
        }

        /** Le point (px, py) du portrait à l'écran : {@code out[0], out[1]}. */
        private void toScreen(float px, float py, float[] out) {
            float sx = x + px * pixel, sy = y + (height - py) * pixel;
            out[0] = feetX + (sx - feetX) * cos - (sy - feetY) * sin;
            out[1] = feetY + (sx - feetX) * sin + (sy - feetY) * cos;
        }

        private final float[] point = new float[2];

        @Override
        public void rect(float px, float py, float width, float h, Color color, float a) {
            if (a <= 0f) return;
            toScreen(px, py, point);
            batch.setColor(color.r, color.g, color.b, color.a * Math.min(1f, a) * alpha);
            batch.draw(white, Math.round(point[0]), Math.round(point[1] - h * pixel), width * pixel, h * pixel);
        }

        @Override
        public void glow(float px, float py, float radius, Color color, float a) {
            if (a <= 0f) return;
            toScreen(px, py, point);
            int src = batch.getBlendSrcFunc(), dst = batch.getBlendDstFunc();
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            float size = radius * pixel;
            batch.setColor(color.r, color.g, color.b, Math.min(1f, a) * alpha);
            batch.draw(glow, point[0] - size, point[1] - size, size * 2f, size * 2f);
            batch.setBlendFunction(src, dst);
        }
    }

    /** Le décor couvre la zone de jeu, sans lissage, centré sur la table ; les lanternes de la Cale vacillent. */
    private void drawDecor(Batch batch, float parentAlpha) {
        Texture texture = decors.computeIfAbsent(decor, d -> load(d.path));
        float worldWidth  = getStage().getViewport().getWorldWidth();
        float worldHeight = getStage().getViewport().getWorldHeight();
        float areaWidth   = worldWidth - playArea.getX();
        float scale  = Math.max(areaWidth / texture.getWidth(), worldHeight / texture.getHeight());
        float width  = texture.getWidth() * scale, height = texture.getHeight() * scale;
        float left   = playArea.getCenterX() - width / 2f, bottom = worldHeight - height;
        Color tint = decor == Decor.CASINO ? CASINO_TINT : Color.WHITE;
        batch.setColor(tint.r, tint.g, tint.b, parentAlpha);
        batch.draw(texture, left, bottom, width, height);
        if (decor == Decor.CALE) {
            int src = batch.getBlendSrcFunc(), dst = batch.getBlendDstFunc();
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            for (int i = 0; i < CALE_LANTERNS.length; i++) {
                float flicker = 0.8f + 0.12f * MathUtils.sin(time * 9f + i * 2.1f) + 0.08f * MathUtils.sin(time * 23f + i);
                float size = 150f * scale / 4f * (0.95f + 0.05f * flicker);
                float cx = left + CALE_LANTERNS[i][0] * scale, cy = bottom + height - CALE_LANTERNS[i][1] * scale;
                batch.setColor(1f, 0.75f, 0.4f, 0.55f * flicker * parentAlpha);
                batch.draw(glow, cx - size, cy - size, size * 2f, size * 2f);
            }
            batch.setBlendFunction(src, dst);
        }
        batch.setColor(Color.WHITE);
    }

    private static Texture load(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return texture;
    }

    /** Un pixel blanc, teinté pour dessiner les objets des animations. */
    private static Texture whiteTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /** Halo rond et doux, blanc au centre, pour la lumière des lanternes. */
    private static Texture glowTexture() {
        int size = 64;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        Vector2 center = new Vector2(size / 2f, size / 2f);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                float d = Math.min(1f, center.dst(x + 0.5f, y + 0.5f) / (size / 2f));
                float a = (1f - d) * (1f - d);
                pixmap.drawPixel(x, y, Color.rgba8888(1f, 1f, 1f, a));
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        glow.dispose();
        white.dispose();
        for (Texture texture : decors.values()) texture.dispose();
    }
}
