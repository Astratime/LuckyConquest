package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Bingo de la Descente aux enfers : une porte de pierre gravée de runes surgit
 * de la table en tremblant, ses deux battants s'écartent sur un brasier rouge
 * ({@link #OPEN_TIME}), le symbole tombe du haut de l'écran en tournoyant et
 * s'engouffre dans la porte, puis l'enfer recrache tout dans une éruption
 * à {@link #ERUPT_TIME} et le symbole se dresse, géant, devant la porte (les
 * flammes, les ondes et les cendres sont lancées par {@link JackpotCelebration},
 * qui suit {@link #gate}). Dès l'ouverture, une nuée de diablotins s'échappe de
 * la porte en battant des ailes et envahit tout l'écran (Astra, 2026-11-02).
 */
public class HellGateScene extends BingoScene {

    /** Les battants commencent à s'ouvrir. */
    public static final float OPEN_TIME  = 0.35f;
    /** Le symbole commence sa chute. */
    public static final float FALL_TIME  = 0.8f;
    /** L'enfer entre en éruption. */
    public static final float ERUPT_TIME = 1.35f;

    private static final int DOOR_W = 18, DOOR_H = 40;
    private static final float DOOR_SCALE = 13f;
    private static final float ICON_SIZE = 280f;

    /** Diablotins : nombre, taille, et fenêtre pendant laquelle ils sortent. */
    private static final int   IMPS = 70;
    private static final float IMP_SCALE = 6f;
    private static final float IMPS_FROM = 0.5f, IMPS_UNTIL = 2.3f;

    private final Image glow, hole, left, right, icon;
    private final Image[] imps = new Image[IMPS];
    private final float[] impAt = new float[IMPS], impAngle = new float[IMPS], impSpeed = new float[IMPS],
        impWobble = new float[IMPS];
    private final Vector2 gate = new Vector2();

    /** @param descent image du symbole Descente aux enfers */
    public HellGateScene(TextureRegion descent) {
        glow = image(texture(halo(56, hex("ff2a10"))));
        glow.setOrigin(glow.getWidth() / 2f, glow.getHeight() / 2f);
        addActor(glow);
        hole = image(new TextureRegion(texture(hole())), DOOR_SCALE);
        hole.setOrigin(hole.getWidth() / 2f, 0f);
        addActor(hole);
        icon = image(descent, ICON_SIZE / descent.getRegionWidth());
        icon.setOrigin(icon.getWidth() / 2f, icon.getHeight() / 2f);
        addActor(icon);
        TextureRegion imp = new TextureRegion(texture(imp()));
        for (int i = 0; i < IMPS; i++) {
            imps[i] = image(imp, IMP_SCALE);
            imps[i].setOrigin(imps[i].getWidth() / 2f, imps[i].getHeight() / 2f);
            addActor(imps[i]);
        }
        left = image(new TextureRegion(texture(door(false))), DOOR_SCALE);
        right = image(new TextureRegion(texture(door(true))), DOOR_SCALE);
        addActor(left);
        addActor(right);
    }

    /** @return le centre (Stage) de l'ouverture de la porte. */
    public Vector2 gate() { return gate; }

    @Override
    protected void start(float x, float y) {
        gate.set(x, y + DOOR_H * DOOR_SCALE / 2f);
        icon.setVisible(false);
        icon.setColor(Color.WHITE);
        left.setColor(Color.WHITE);
        right.setColor(Color.WHITE);
        // Chaque diablotin a son instant de sortie, sa direction, sa vitesse et son battement d'ailes.
        for (int i = 0; i < IMPS; i++) {
            impAt[i] = IMPS_FROM + (IMPS_UNTIL - IMPS_FROM) * i / IMPS + MathUtils.random(0f, 0.04f);
            impAngle[i] = MathUtils.random(-170f, -10f) + 180f;              // vers le haut et les côtés
            impSpeed[i] = MathUtils.random(380f, 900f);
            impWobble[i] = MathUtils.random(0f, MathUtils.PI2);
            imps[i].setVisible(false);
        }
        update(0f);
    }

    @Override
    protected void update(float delta) {
        float doorW = DOOR_W * DOOR_SCALE, doorH = DOOR_H * DOOR_SCALE;
        float baseY = gate.y - doorH / 2f;
        // La porte monte de la table en tremblant.
        float rise = Interpolation.pow2Out.apply(Math.min(1f, time / OPEN_TIME));
        float shake = time < OPEN_TIME || (time >= ERUPT_TIME && time < ERUPT_TIME + 0.3f) ? 6f : 0f;
        float y = baseY - doorH * (1f - rise) + MathUtils.random(-shake, shake);
        // Les battants s'écartent.
        float open = time < OPEN_TIME ? 0f : Interpolation.pow2Out.apply(Math.min(1f, (time - OPEN_TIME) / 0.4f));
        float gap = open * doorW * 0.95f;
        left.setPosition(gate.x - doorW - gap, y);
        right.setPosition(gate.x + gap, y);
        hole.setScale(Math.max(0.05f, open), rise);
        placeOrigin(hole, gate.x, y);
        float flicker = 0.8f + 0.2f * MathUtils.sin(time * 30f);
        hole.setColor(1f, 0.6f + 0.4f * flicker * open, 0.5f, 1f);
        glow.setScale((2.5f + 4f * open) * flicker, (3f + 3f * open) * flicker);
        glow.getColor().a = 0.3f + 0.5f * open;
        placeOrigin(glow, gate.x, gate.y);

        updateImps();

        // Le symbole tombe et s'engouffre, puis l'éruption le recrache, géant.
        if (time < FALL_TIME) return;
        icon.setVisible(true);
        if (time < ERUPT_TIME) {
            float fall = Interpolation.pow2In.apply((time - FALL_TIME) / (ERUPT_TIME - FALL_TIME));
            float top = getStage() != null ? getStage().getHeight() : 1080f;
            icon.setScale(1f - 0.85f * fall);
            icon.setRotation(fall * 900f);
            placeOrigin(icon, gate.x, MathUtils.lerp(top + ICON_SIZE, gate.y, fall));
        } else {
            float since = time - ERUPT_TIME;
            float pop = Interpolation.swingOut.apply(Math.min(1f, since / 0.35f));
            icon.setScale(0.15f + 1.15f * pop * (1f + 0.05f * MathUtils.sin(since * 9f)));
            icon.setRotation(MathUtils.sin(since * 7f) * 4f);
            icon.setColor(1f, 0.75f + 0.25f * pop, 0.75f + 0.25f * pop, 1f);
            placeOrigin(icon, gate.x, gate.y + 60f * pop);
            float fade = Math.max(0f, 1f - since / 0.5f);
            left.getColor().a = fade;
            right.getColor().a = fade;
        }
    }

    /** Les diablotins jaillissent de la porte, volent en zigzag en battant des ailes et sortent de l'écran. */
    private void updateImps() {
        for (int i = 0; i < IMPS; i++) {
            Image imp = imps[i];
            float since = time - impAt[i];
            if (since < 0f || since > 2.2f) {
                imp.setVisible(false);
                continue;
            }
            imp.setVisible(true);
            float angle = impAngle[i], speed = impSpeed[i];
            float wobble = MathUtils.sin(since * 9f + impWobble[i]) * 40f;
            float x = gate.x + MathUtils.random(-2f, 2f) + MathUtils.cosDeg(angle) * speed * since + wobble;
            float y = gate.y - 60f + MathUtils.sinDeg(angle) * speed * since + 40f * since * since;
            float flap = 0.7f + 0.3f * Math.abs(MathUtils.sin(since * 22f + impWobble[i]));
            float grow = Math.min(1f, since / 0.2f);
            boolean faceLeft = MathUtils.cosDeg(angle) < 0f;
            imp.setScale((faceLeft ? -1f : 1f) * grow, flap * grow);
            imp.setRotation(MathUtils.sin(since * 7f + impWobble[i]) * 15f);
            placeOrigin(imp, x, y);
        }
    }

    /** @return un diablotin : corps rouge, cornes, ailes de chauve-souris, yeux jaunes, queue fourchue. */
    private static Pixmap imp() {
        Pixmap pixmap = pixmap(17, 14);
        Color red = hex("d8202a"), dark = hex("8a1018"), wing = hex("4a0a14"), eye = hex("ffe040"), horn = hex("f4e6c8");
        String[] rows = {
            "......h...h......",
            "......hrrrh......",
            "w....rrrrrrr....w",
            "ww...reyrrey...ww",
            "www..rrrrrrr..www",
            "wwww..rrkkrr.wwww",
            ".wwwwrrrrrrrwwww.",
            "..wwrrrrrrrrrww..",
            "...w.rrrrrrr.w...",
            "......rrrrr......",
            "......rr.rr....d.",
            ".....rr...rr..dd.",
            ".....d.....d.d...",
            "..........ddd....",
        };
        drawGrid(pixmap, rows, 0, 0, "rwehkd", red, wing, eye, horn, dark, dark);
        return pixmap;
    }

    /** @return un battant de pierre noire : blocs, rune rouge, ferrures, bord arrondi vers l'extérieur. */
    private static Pixmap door(boolean rightSide) {
        Pixmap pixmap = pixmap(DOOR_W, DOOR_H);
        Color stone = hex("3a2a30"), light = hex("5a4048"), dark = hex("221418"), iron = hex("6a6070");
        Shape shape = (x, y) -> {
            int outer = rightSide ? x : DOOR_W - 1 - x;      // 0 côté ouverture
            // Le haut s'arrondit vers l'extérieur : une arche à deux battants.
            float arch = outer >= DOOR_W - 8 ? (outer - (DOOR_W - 8)) * (outer - (DOOR_W - 8)) / 6f : 0f;
            return y >= arch;
        };
        fillShape(pixmap, shape, (x, y) -> {
            int row = y / 5, offset = row % 2 == 0 ? 0 : 3;
            boolean mortar = y % 5 == 0 || (x + offset) % 6 == 0;
            if (mortar) return dark;
            return (x + y) % 7 == 0 ? light : stone;
        });
        // Rune rouge, au milieu du battant.
        Color rune = hex("ff3020");
        String[] glyph = {
            "..r..",
            ".rrr.",
            "r.r.r",
            "..r..",
            ".r.r.",
            "r...r",
        };
        drawGrid(pixmap, glyph, DOOR_W / 2 - 2, 15, "r", rune);
        // Ferrures.
        pixmap.setColor(iron);
        for (int y : new int[] {8, 30}) pixmap.fillRectangle(1, y, DOOR_W - 2, 2);
        return pixmap;
    }

    /** @return l'ouverture : un brasier, jaune au fond, rouge sur les bords. */
    private static Pixmap hole() {
        int w = 2 * DOOR_W - 2, h = DOOR_H - 2;
        Pixmap pixmap = pixmap(w, h);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float u = Math.abs(x + 0.5f - w / 2f) / (w / 2f), v = (float) y / h;
                float heat = (1f - u) * (0.5f + 0.5f * v);
                pixmap.setColor(heat > 0.6f ? hex("ffd040") : heat > 0.35f ? hex("ff7a1a") : heat > 0.15f ? hex("c01818")
                    : hex("400810"));
                pixmap.drawPixel(x, y);
            }
        }
        return pixmap;
    }
}
