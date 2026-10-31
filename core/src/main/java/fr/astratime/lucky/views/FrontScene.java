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
import com.badlogic.gdx.scenes.scene2d.Touchable;
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
 * <p>L'ennemi respire et recopie les gestes de son petit portrait de la vue
 * classique ({@link EnemyView}, caché en vue de face) : il tremble et flashe en
 * blanc quand il est touché, luit en vert quand il se soigne, hoche la tête,
 * se penche pour frapper et bascule, vaincu. Les ennemis qui ont un grand
 * dessin ({@link BigArt} : le Rat des cales, les soldats et les rois de la
 * Prairie) l'ont en pièces détachées (corps, tête, paupière, une pièce qui se
 * balance, mains), animées ici ; les autres montrent pour l'instant leur
 * portrait, agrandi.
 *
 * <p>Deux acteurs : la scène elle-même, sous la table, et les pattes de
 * l'ennemi ({@link #getPaws()}), posées sur le rebord, par-dessus la table.
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

    /**
     * Grand dessin d'un ennemi, en pièces détachées dans {@code folder} : body,
     * head, eyelid, swing (queue, plumet, sceptre...) et paw, chacune avec sa
     * silhouette blanche *_flash. Toutes partagent le même cadre, sauf la main.
     *
     * @param pivotX    pied de la pièce qui se balance (pixels du dessin, origine en haut à gauche)
     * @param swing     son balancement, en degrés, et sa vitesse
     * @param pawLeft   bord extérieur des mains sur le rebord, en pixels du dessin
     */
    private record BigArt(String folder, float pivotX, float pivotY, float swing, float swingSpeed,
                          float pawLeft, float pawRight) {
        static BigArt soldier(String suit) { return new BigArt("front/prairie/soldat_" + suit, 65f, 12f, 6f, 1.9f, 6.5f, 123.5f); }
        static BigArt king(String suit)    { return new BigArt("front/prairie/roi_" + suit, 114f, 108f, 2.5f, 1.3f, 6.5f, 123.5f); }
    }

    private static final Map<EnemyKind, BigArt> BIG_ART = new EnumMap<>(Map.of(
        EnemyKind.RAT_CALES,      new BigArt("front/rat_cales", 96f, 96f, 9f, 2.3f, 6.5f, 123.5f),
        EnemyKind.SOLDAT_PIQUE,   BigArt.soldier("pique"),
        EnemyKind.SOLDAT_TREFLE,  BigArt.soldier("trefle"),
        EnemyKind.SOLDAT_COEUR,   BigArt.soldier("coeur"),
        EnemyKind.SOLDAT_CARREAU, BigArt.soldier("carreau"),
        EnemyKind.ROI_PIQUE,      BigArt.king("pique"),
        EnemyKind.ROI_TREFLE,     BigArt.king("trefle"),
        EnemyKind.ROI_COEUR,      BigArt.king("coeur"),
        EnemyKind.ROI_CARREAU,    BigArt.king("carreau")));

    /** Les textures d'un grand dessin, chargées au premier combat contre cet ennemi. */
    private static final class Parts {
        final BigArt  art;
        final Texture body, head, swing, eyelid, paw, bodyFlash, headFlash, swingFlash, pawFlash;

        Parts(BigArt art) {
            this.art   = art;
            body       = load(art.folder() + "/body.png");
            head       = load(art.folder() + "/head.png");
            swing      = load(art.folder() + "/swing.png");
            eyelid     = load(art.folder() + "/eyelid.png");
            paw        = load(art.folder() + "/paw.png");
            bodyFlash  = load(art.folder() + "/body_flash.png");
            headFlash  = load(art.folder() + "/head_flash.png");
            swingFlash = load(art.folder() + "/swing_flash.png");
            pawFlash   = load(art.folder() + "/paw_flash.png");
        }

        void dispose() {
            for (Texture texture : new Texture[] {body, head, swing, eyelid, paw, bodyFlash, headFlash, swingFlash, pawFlash}) {
                texture.dispose();
            }
        }
    }

    /** Place laissée en haut de l'écran (barre de vie de l'ennemi), pour régler la taille de l'ennemi. */
    private static final float TOP_ROOM        = 40f;
    /** Part de la hauteur de l'ennemi cachée derrière le rebord de la table. */
    private static final float HIDDEN_PART     = 0.06f;
    /** Respiration : amplitude (en pixels du dessin) et période, en secondes. */
    private static final float BREATH          = 1f;
    private static final float BREATH_PERIOD   = 2.4f;
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
    private final Map<EnemyKind, Parts>   parts  = new EnumMap<>(EnemyKind.class);

    private final Paws paws = new Paws();
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
        paws.setTouchable(Touchable.disabled);
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

    /** @return les pattes de l'ennemi, à ajouter au Stage juste après la table. */
    public Actor getPaws() { return paws; }

    /** Choisit le décor du lieu du combat. */
    public void setDecor(Decor decor) { this.decor = decor; }

    @Override
    public void act(float delta) {
        super.act(delta);
        time += delta;
        boolean shown = projection.isEnabled();
        setVisible(shown);
        paws.setVisible(shown && parts() != null);
        if (shown) place();
    }

    /** @return le grand dessin de l'ennemi du combat, ou {@code null} s'il n'en a pas (son portrait, agrandi). */
    private Parts parts() {
        EnemyKind kind = enemy.get().getKind();
        BigArt art = BIG_ART.get(kind);
        return art == null ? null : parts.computeIfAbsent(kind, k -> new Parts(art));
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

    /** @return l'image qui donne la taille de l'ennemi : le corps de son grand dessin, ou le portrait. */
    private Texture shownTexture() {
        Parts big = parts();
        return big != null ? big.body : enemyTextures.portrait(enemy.get().getKind());
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

        Parts big = parts();
        if (big != null) {
            BigArt art = big.art;
            float swing = MathUtils.sin(time * art.swingSpeed()) * art.swing();
            float pivotX = art.pivotX() * pixel, pivotY = (big.swing.getHeight() - art.pivotY()) * pixel;
            drawPart(batch, big.swing, big.swingFlash, x, y, pivotX, pivotY, swing + rotation, alpha, flash);
            drawPart(batch, big.body, big.bodyFlash, x, y + breath * 0.5f, originX, 0f, rotation, alpha, flash);
            drawPart(batch, big.head, big.headFlash, x, y + breath, originX, 0f, rotation, alpha, flash);
            if (time % BLINK_EVERY < BLINK_TIME) {
                drawPart(batch, big.eyelid, null, x, y + breath, originX, 0f, rotation, alpha, flash);
            }
        } else {
            EnemyKind kind = enemy.get().getKind();
            drawPart(batch, enemyTextures.portrait(kind), enemyTextures.portraitFlash(kind), x, y + breath, originX, 0f,
                rotation, alpha, flash);
        }
        batch.setColor(Color.WHITE);
    }

    /** Une pièce de l'ennemi en {@code (x, y)}, puis sa silhouette par-dessus quand elle flashe. */
    private void drawPart(Batch batch, Texture texture, Texture silhouette, float x, float y, float originX, float originY,
                          float rotation, float alpha, Color flash) {
        float width = texture.getWidth() * pixel, height = texture.getHeight() * pixel;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(texture, x, y, originX, originY, width, height, 1f, 1f, rotation,
            0, 0, texture.getWidth(), texture.getHeight(), false, false);
        if (silhouette != null && flash.a > 0f) {
            batch.setColor(flash.r, flash.g, flash.b, flash.a * alpha);
            batch.draw(silhouette, x, y, originX, originY, width, height, 1f, 1f, rotation,
                0, 0, silhouette.getWidth(), silhouette.getHeight(), false, false);
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

    /** Les pattes (ou les mains) de l'ennemi, agrippées au rebord de la table, qui serrent et relâchent tour à tour. */
    private class Paws extends Actor {
        @Override
        public void draw(Batch batch, float parentAlpha) {
            Actor character = enemyView.getCharacter();
            Actor portrait  = enemyView.getPortrait();
            Color flash     = enemyView.getFlashColor();
            float alpha = parentAlpha * character.getColor().a;
            Parts big = parts();
            if (alpha <= 0f || big == null) return;
            Texture paw = big.paw, pawFlash = big.pawFlash;
            float width = paw.getWidth() * pixel, height = paw.getHeight() * pixel;
            float y = projection.getTopY() - height * 0.6f + portrait.getY() * GESTURE_SCALE;
            float x = enemyX + portrait.getX() * GESTURE_SCALE;
            boolean leftGrips = time % 0.9f < 0.45f;
            float leftX = x + big.art.pawLeft() * pixel, rightX = x + big.art.pawRight() * pixel - width;
            drawPart(batch, paw, pawFlash, leftX, y - (leftGrips ? pixel : 0f), 0f, 0f, 0f, alpha, flash);
            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(paw, rightX, y - (leftGrips ? 0f : pixel), width, height,
                0, 0, paw.getWidth(), paw.getHeight(), true, false); // patte droite : en miroir
            if (flash.a > 0f) {
                batch.setColor(flash.r, flash.g, flash.b, flash.a * alpha);
                batch.draw(pawFlash, rightX, y - (leftGrips ? 0f : pixel), width, height,
                    0, 0, pawFlash.getWidth(), pawFlash.getHeight(), true, false);
            }
            batch.setColor(Color.WHITE);
        }
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
        for (Parts big : parts.values()) big.dispose();
    }
}
