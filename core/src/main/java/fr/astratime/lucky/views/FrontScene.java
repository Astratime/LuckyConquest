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
import fr.astratime.lucky.entities.exploration.Dungeon;
import fr.astratime.lucky.entities.Enemy;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.tower.Chapter;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Vue de face du combat : le décor du donjon, et l'ennemi en grand, debout
 * derrière la table vue en perspective ({@link TableProjection}).
 *
 * <p>L'ennemi est son portrait de la vue classique, agrandi, qui bouge comme en
 * vue classique : il recopie le mouvement et les gestes de son petit portrait
 * ({@link EnemyView}, caché en vue de face) : il monte et descend doucement,
 * tremble et flashe en blanc quand il est touché, luit en vert quand il se soigne, hoche la tête, se penche pour frapper et bascule, vaincu.
 */
public class FrontScene extends Actor implements Disposable {

    /**
     * Décor derrière l'ennemi : un par donjon de l'Exploration (la Prairie en a
     * un par couleur), un par chapitre de la Tour, un pour l'Entraînement, le
     * casino ailleurs. Ses lumières (torches, lanternes,
     * lave, projecteurs) vacillent : leur centre en pixels du décor (origine en
     * haut à gauche), leur couleur et leur rayon. Dessins :
     * tools/front/generate_front_art.py et generate_decors.py.
     */
    public enum Decor {
        PRAIRIE_PIQUE("prairie_pique"),
        PRAIRIE_TREFLE("prairie_trefle"),
        PRAIRIE_COEUR("prairie_coeur"),
        PRAIRIE_CARREAU("prairie_carreau"),
        CHAPITEAU("chapiteau", WARM, 30f, 60, 22, 300, 22),
        TRAIN_FANTOME("train_fantome", GHOST, 14f, 24, 140, 336, 132, 60, 120),
        GRANDE_ROUE("grande_roue", WARM, 24f, 78, 82, 78, 16, 12, 82, 144, 82),
        STANDS("stands", WARM, 30f, 60, 60, 300, 60),
        CALE("cale", WARM, 37f, 128, 30, 236, 30),
        TAVERNE("taverne", WARM, 34f, 140, 30, 226, 30, 316, 150),
        PHARE("phare", WARM, 40f, 70, 39),
        GALION("galion", WARM, 34f, 120, 40, 240, 40),
        FILON("filon", WARM, 34f, 55, 34, 305, 34),
        PUITS("puits", WARM, 34f, 66, 40, 290, 40),
        FORGE("forge", LAVA, 40f, 65, 140, 40, 100, 90, 100),
        GOUFFRE("gouffre", GOLDEN, 24f, 36, 120, 78, 140, 300, 118, 330, 140),
        BAR("bar", WATER, 40f, 60, 20, 300, 20),
        MACHINES("machines", WATER, 22f, 22, 80, 74, 80, 126, 80, 262, 80),
        COFFRES("coffres", GOLDEN, 26f, 272, 126, 322, 126),
        VIP("vip", WARM, 40f, 180, 30),
        CARAVANE("caravane"),
        TOMBEAU("tombeau", WARM, 34f, 122, 46, 242, 46),
        OASIS("oasis"),
        VOLCAN("volcan", LAVA, 40f, 70, 30, 80, 110, 60, 172, 300, 184),
        GENESE("genese", WARM, 30f, 150, 60, 50, 70, 310, 70),
        TABLES_SACREES("tables_sacrees", WARM, 30f, 43, 40, 303, 40),
        DERNIER_TIRAGE("dernier_tirage", WATER, 30f, 320, 50, 60, 96, 290, 98),
        MONDE_SANS_MAITRE("monde_sans_maitre", LAVA, 34f, 50, 128, 310, 128),
        LA_MAISON("la_maison", GOLDEN, 30f, 80, 16, 280, 16, 180, 60),
        LE_JACKPOT("le_jackpot", GOLDEN, 30f, 180, 40, 40, 50, 320, 60),
        ENTRAINEMENT("entrainement", WARM, 40f, 180, 32),
        CASINO("menu/casino_interior.png", true);

        final String  path;
        final boolean dimmed;
        final Color   glowColor;
        final float   glowRadius;
        final float[] glows;

        Decor(String name) { this(name, WARM, 0f); }

        Decor(String name, Color glowColor, float glowRadius, float... glows) {
            this.path       = "front/decor_" + name + ".png";
            this.dimmed     = false;
            this.glowColor  = glowColor;
            this.glowRadius = glowRadius;
            this.glows      = glows;
        }

        /** Le décor du casino (portrait des autres combats) : assombri pour que l'ennemi ressorte. */
        Decor(String path, boolean dimmed) {
            this.path       = path;
            this.dimmed     = dimmed;
            this.glowColor  = WARM;
            this.glowRadius = 0f;
            this.glows      = new float[0];
        }

        /** @return le décor d'un donjon de l'Exploration (celui de sa couleur dans la Prairie). */
        public static Decor of(Dungeon dungeon) {
            if (dungeon.getSuit() != null) {
                return switch (dungeon.getSuit()) {
                    case PIQUE   -> PRAIRIE_PIQUE;
                    case TREFLE  -> PRAIRIE_TREFLE;
                    case COEUR   -> PRAIRIE_COEUR;
                    case CARREAU -> PRAIRIE_CARREAU;
                };
            }
            for (Decor decor : values()) {
                if (decor.name().equals(dungeon.name())) return decor;
            }
            return CASINO;
        }

        /** @return le décor d'un chapitre de la Tour des épreuves. */
        public static Decor of(Chapter chapter) {
            return switch (chapter) {
                case GENESE            -> GENESE;
                case TABLES_SACREES    -> TABLES_SACREES;
                case DERNIER_TIRAGE    -> DERNIER_TIRAGE;
                case MONDE_SANS_MAITRE -> MONDE_SANS_MAITRE;
                case LA_MAISON         -> LA_MAISON;
                case LE_JACKPOT        -> LE_JACKPOT;
            };
        }
    }

    /** Place laissée en haut de l'écran (barre de vie de l'ennemi), pour régler la taille de l'ennemi. */
    private static final float TOP_ROOM        = 40f;
    /** Part de la hauteur de l'ennemi cachée derrière le rebord de la table. */
    private static final float HIDDEN_PART     = 0.06f;
    /** Les gestes du petit portrait sont amplifiés d'autant sur le grand. */
    private static final float GESTURE_SCALE   = 2f;
    /** Décor du casino (portrait des autres lieux) : assombri pour que l'ennemi ressorte. */
    private static final Color CASINO_TINT     = new Color(0.55f, 0.5f, 0.55f, 1f);
    /** Couleur des lumières des décors : flamme, lueur verte du train fantôme, eau, lave, or. */
    private static final Color WARM   = new Color(1f, 0.75f, 0.4f, 1f);
    private static final Color GHOST  = new Color(0.45f, 1f, 0.55f, 1f);
    private static final Color WATER  = new Color(0.55f, 0.9f, 1f, 1f);
    private static final Color LAVA   = new Color(1f, 0.45f, 0.15f, 1f);
    private static final Color GOLDEN = new Color(1f, 0.85f, 0.3f, 1f);

    private final TableProjection projection;
    private final PlayArea        playArea;
    private final EnemyTextures   enemyTextures;
    private final EnemyView       enemyView;
    private final Supplier<Enemy> enemy;

    private final Texture glow = glowTexture();
    private final Map<Decor, Texture>     decors = new EnumMap<>(Decor.class);

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
        // Le même mouvement qu'en vue classique : il monte et descend doucement, à l'échelle du grand portrait.
        float lift = enemyView.getIdleLift() / character.getHeight() * getHeight();
        float x = enemyX + portrait.getX() * GESTURE_SCALE, y = enemyY + portrait.getY() * GESTURE_SCALE + lift;
        float originX = getWidth() / 2f; // vaincu, il bascule en arrière autour de ses pieds

        EnemyKind kind = enemy.get().getKind();
        float time = enemyView.getAnimationTime();
        Texture texture = enemyTextures.portrait(kind, time), silhouette = enemyTextures.portraitFlash(kind, time);
        float width = texture.getWidth() * pixel, height = texture.getHeight() * pixel;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(texture, x, y, originX, 0f, width, height, 1f, 1f, rotation,
            0, 0, texture.getWidth(), texture.getHeight(), false, false);
        if (silhouette != null && flash.a > 0f) {
            batch.setColor(flash.r, flash.g, flash.b, flash.a * alpha);
            batch.draw(silhouette, x, y, originX, 0f, width, height, 1f, 1f, rotation,
                0, 0, silhouette.getWidth(), silhouette.getHeight(), false, false);
        }
        batch.setColor(Color.WHITE);
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
        Color tint = decor.dimmed ? CASINO_TINT : Color.WHITE;
        batch.setColor(tint.r, tint.g, tint.b, parentAlpha);
        batch.draw(texture, left, bottom, width, height);
        if (decor.glows.length > 0) {
            int src = batch.getBlendSrcFunc(), dst = batch.getBlendDstFunc();
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            Color c = decor.glowColor;
            for (int i = 0; i < decor.glows.length / 2; i++) {
                float flicker = 0.8f + 0.12f * MathUtils.sin(time * 9f + i * 2.1f) + 0.08f * MathUtils.sin(time * 23f + i);
                float size = decor.glowRadius * scale * (0.95f + 0.05f * flicker);
                float cx = left + decor.glows[i * 2] * scale, cy = bottom + height - decor.glows[i * 2 + 1] * scale;
                batch.setColor(c.r, c.g, c.b, 0.55f * flicker * parentAlpha);
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
