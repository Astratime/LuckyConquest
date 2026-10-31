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

import java.util.EnumMap;
import java.util.Map;
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
    /** La tête (le haut du portrait) bouge plus que le corps ; le cou, autour de {@code HEAD_PART}, se plie sur {@code NECK} rangées. */
    private static final float HEAD_PART       = 0.5f;
    private static final float NECK            = 10f;
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
    private final Map<Decor, Texture>     decors = new EnumMap<>(Decor.class);
    // Mouvement de la tête de l'image en cours : centre du cou (rangée), décalage à l'écran.
    private float headCenter, headDx, headDy;

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

        int height = portraitTexture.getHeight();
        float bodyY = y + breath * 0.5f;
        float feetX = x + originX;

        // Chaque rangée suit la tête autant qu'elle en est proche : la tête bouge (respire plus fort, se tourne),
        // le corps non, et le cou se plie entre les deux, sans que la tête se détache.
        headCenter = height * HEAD_PART;
        headDx = lookOffset() * pixel;
        headDy = breath * 0.5f;
        Texture shown = blinking ? blink : portraitTexture;
        for (int r = height - 1; r >= 0; r--) {
            float top = rowEdge(r, bodyY, height), bottom = rowEdge(r + 1, bodyY, height);
            if (top <= bottom) continue;
            float rx = x + headWeight(r + 0.5f) * headDx;
            drawRow(batch, shown, flashTexture, r, rx, bottom, top - bottom, feetX, bodyY, rotation, alpha, flash);
        }
        batch.setColor(Color.WHITE);
    }

    /** @return combien la rangée {@code row} suit la tête : 1 dans la tête, 0 dans le corps, entre les deux au cou. */
    private float headWeight(float row) {
        float u = (headCenter + NECK / 2f - row) / NECK;
        u = MathUtils.clamp(u, 0f, 1f);
        return u * u * (3f - 2f * u);
    }

    /** @return la hauteur à l'écran du haut de la rangée {@code row} (le bas de la précédente) : elles se touchent toujours. */
    private float rowEdge(int row, float bodyY, int height) {
        return bodyY + (height - row) * pixel + headWeight(row) * headDy;
    }

    /** Une rangée du portrait, posée de {@code bottom} à {@code bottom + rowHeight}, tournée autour des pieds. */
    private void drawRow(Batch batch, Texture texture, Texture silhouette, int row, float x, float bottom, float rowHeight,
                         float pivotX, float pivotY, float rotation, float alpha, Color flash) {
        float width = texture.getWidth() * pixel;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(texture, x, bottom, pivotX - x, pivotY - bottom, width, rowHeight, 1f, 1f, rotation,
            0, row, texture.getWidth(), 1, false, false);
        if (silhouette != null && flash.a > 0f) {
            batch.setColor(flash.r, flash.g, flash.b, flash.a * alpha);
            batch.draw(silhouette, x, bottom, pivotX - x, pivotY - bottom, width, rowHeight, 1f, 1f, rotation,
                0, row, silhouette.getWidth(), 1, false, false);
        }
    }

    /** @return le décalage de la tête qui se tourne (en pixels du portrait) : vers la gauche, puis la droite. */
    private float lookOffset() {
        float t = time % (LOOK_EVERY * 2f);
        float side = t < LOOK_EVERY ? -1f : 1f;
        t %= LOOK_EVERY;
        if (t > LOOK_TIME) return 0f;
        return side * MathUtils.sin(t / LOOK_TIME * MathUtils.PI);
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
        for (Texture texture : decors.values()) texture.dispose();
    }
}
