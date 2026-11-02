package fr.astratime.lucky.animations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Bingo de la Lumière du bingo : une boule de bingo dorée géante descend du
 * ciel en tournant, le symbole brillant dans sa fenêtre ; elle s'arrête au
 * milieu de la table, se fissure de rayons de lumière ({@link #CRACK_TIME}),
 * puis éclate en deux à {@link #OPEN_TIME} : une couronne de rayons blancs et
 * or tourne autour du symbole libéré, qui grandit (les étincelles, les ondes
 * et la pluie de symboles sont lancées par {@link JackpotCelebration}, qui
 * suit {@link #center}).
 * <p>
 * Deux divinités l'encadrent (Astra, 2026-11-02) : à gauche un dieu grec à la
 * barbe blanche, façon Zeus, qui lance des éclairs ({@link #BOLTS}) ; à droite
 * une femme au manteau bleu étoilé, façon Vierge Marie, qui lève les deux bras à
 * {@link #CRACK_TIME} et fait apparaître une nuée d'anges autour d'elle.
 */
public class LightOrbScene extends BingoScene {

    /** La boule se fissure. */
    public static final float CRACK_TIME = 0.95f;
    /** La boule éclate. */
    public static final float OPEN_TIME  = 1.3f;

    /** Instants où le dieu lance un éclair. */
    public static final float[] BOLTS = {0.75f, 1.3f, 1.55f, 1.8f, 2.05f, 2.25f};

    private static final float GOD_SCALE = 5f;
    /** Épaules dans les dessins (pixels, depuis le haut à gauche) : jackpot/zeus.png et jackpot/marie.png. */
    private static final int ZEUS_SHOULDER_X = 33, ZEUS_SHOULDER_Y = 25;
    private static final int MARY_LEFT_X = 14, MARY_RIGHT_X = 30, MARY_SHOULDER_Y = 21;
    private static final float SIDE = 430f;          // écart entre une divinité et le symbole
    private static final float APPEAR = 0.25f;       // les divinités apparaissent
    private static final int   BOLT_SEGMENTS = 9;
    private static final float BOLT_LIFE = 0.2f;
    private static final int   ANGELS = 46;
    private static final float ANGEL_SCALE = 5f;

    private static final int BALL = 36;
    private static final float BALL_SCALE = 9f;
    private static final float ICON_SIZE = 150f, FREED_SIZE = 300f;
    private static final int RAYS = 14;
    private static final float RAY_LENGTH = 900f;

    private final Image[] rays = new Image[RAYS];
    private final Image glow, top, bottom, cracks, icon;
    private final Vector2 center = new Vector2(), at = new Vector2();
    // Le dieu (corps, bras qui lance, éclair en main) et la femme voilée (corps, auréole, deux bras).
    private final Image zeus, zeusArm, mary, maryStars, maryLeftArm, maryRightArm;
    private final Image[] bolt = new Image[BOLT_SEGMENTS];
    private final Vector2[] boltPoints = new Vector2[BOLT_SEGMENTS + 1];
    private final Vector2 zeusHand = new Vector2(), maryCenter = new Vector2(), boltEnd = new Vector2();
    private int boltShown = -1;
    private final Image[] angels = new Image[ANGELS];
    private final float[] angelAt = new float[ANGELS], angelX = new float[ANGELS], angelY = new float[ANGELS],
        angelPhase = new float[ANGELS];

    /** @param light image du symbole Lumière du bingo */
    public LightOrbScene(TextureRegion light) {
        TextureRegion pixel = new TextureRegion(texture(whitePixel()));
        for (int i = 0; i < RAYS; i++) {
            rays[i] = image(pixel, 1f);
            rays[i].setSize(RAY_LENGTH, i % 2 == 0 ? 40f : 18f);
            rays[i].setOrigin(0f, rays[i].getHeight() / 2f);
            addActor(rays[i]);
        }
        glow = image(texture(halo(56, hex("fff2b0"))));
        glow.setOrigin(glow.getWidth() / 2f, glow.getHeight() / 2f);
        addActor(glow);
        top = image(new TextureRegion(texture(half(true))), BALL_SCALE);
        top.setOrigin(top.getWidth() / 2f, 0f);
        bottom = image(new TextureRegion(texture(half(false))), BALL_SCALE);
        bottom.setOrigin(bottom.getWidth() / 2f, bottom.getHeight());
        addActor(top);
        addActor(bottom);
        icon = image(light, FREED_SIZE / light.getRegionWidth());
        icon.setOrigin(icon.getWidth() / 2f, icon.getHeight() / 2f);
        addActor(icon);
        cracks = image(new TextureRegion(texture(cracks())), BALL_SCALE);
        cracks.setOrigin(cracks.getWidth() / 2f, cracks.getHeight() / 2f);
        addActor(cracks);

        // Le dieu (Astra : l'Olympien, sur un nuage, aux yeux d'éclair) : son bras droit pivote à l'épaule et tient l'éclair.
        zeus = image(new TextureRegion(texture(png("jackpot/zeus.png"))), GOD_SCALE);
        zeusArm = image(new TextureRegion(texture(png("jackpot/zeus_bras.png"))), GOD_SCALE);
        zeusArm.setOrigin(zeusArm.getWidth() / 2f, zeusArm.getHeight());
        addActor(zeusArm);
        addActor(zeus);
        for (int i = 0; i < BOLT_SEGMENTS; i++) {
            bolt[i] = image(pixel, 1f);
            bolt[i].setVisible(false);
            addActor(bolt[i]);
        }
        for (int i = 0; i <= BOLT_SEGMENTS; i++) boltPoints[i] = new Vector2();

        // La femme au manteau étoilé (Astra : sans capuche), couronne de douze étoiles, deux bras qui se lèvent.
        maryStars = image(new TextureRegion(texture(png("jackpot/marie_etoiles.png"))), GOD_SCALE);
        mary = image(new TextureRegion(texture(png("jackpot/marie.png"))), GOD_SCALE);
        TextureRegion maryArm = new TextureRegion(texture(png("jackpot/marie_bras.png")));
        maryLeftArm = image(maryArm, GOD_SCALE);
        maryRightArm = image(maryArm, GOD_SCALE);
        maryLeftArm.setOrigin(maryLeftArm.getWidth() / 2f, maryLeftArm.getHeight());
        maryRightArm.setOrigin(maryRightArm.getWidth() / 2f, maryRightArm.getHeight());
        addActor(maryStars);
        addActor(maryLeftArm);
        addActor(maryRightArm);
        addActor(mary);
        TextureRegion angel = new TextureRegion(texture(angel()));
        for (int i = 0; i < ANGELS; i++) {
            angels[i] = image(angel, ANGEL_SCALE);
            angels[i].setOrigin(angels[i].getWidth() / 2f, angels[i].getHeight() / 2f);
            addActor(angels[i]);
        }
    }

    /** @return la main (Stage) du dieu, d'où partent les éclairs. */
    public Vector2 zeusHand() { return zeusHand; }

    /** @return le point (Stage) où frappe l'éclair en cours. */
    public Vector2 boltEnd() { return boltEnd; }

    /** @return le centre (Stage) de la femme voilée, autour de laquelle apparaissent les anges. */
    public Vector2 maryCenter() { return maryCenter; }

    /** @return la position (Stage) de la boule, puis du symbole libéré. */
    public Vector2 center() { return at; }

    @Override
    protected void start(float x, float y) {
        center.set(x, y);
        for (Image ray : rays) ray.setVisible(false);
        top.setColor(Color.WHITE);
        bottom.setColor(Color.WHITE);
        boltShown = -1;
        for (Image segment : bolt) segment.setVisible(false);
        for (int i = 0; i < ANGELS; i++) {
            angelAt[i] = CRACK_TIME + 0.15f + 1.1f * i / ANGELS;
            float angle = MathUtils.random(-40f, 220f), radius = MathUtils.random(150f, 360f);
            angelX[i] = MathUtils.cosDeg(angle) * radius * 1.1f;
            angelY[i] = MathUtils.sinDeg(angle) * radius + 60f;
            angelPhase[i] = MathUtils.random(MathUtils.PI2);
            angels[i].setVisible(false);
        }
        update(0f);
    }

    @Override
    protected void update(float delta) {
        float sky = getStage() != null ? getStage().getHeight() : 1080f;
        // Descente en se balançant, puis tremblement avant d'éclater.
        float land = Interpolation.pow2Out.apply(Math.min(1f, time / CRACK_TIME));
        float sway = MathUtils.sin(time * 5f) * 60f * (1f - land);
        float tremble = time >= CRACK_TIME && time < OPEN_TIME ? 7f * (time - CRACK_TIME) / (OPEN_TIME - CRACK_TIME) : 0f;
        at.set(center.x + sway + MathUtils.random(-tremble, tremble),
            MathUtils.lerp(sky + BALL * BALL_SCALE, center.y, land) + MathUtils.random(-tremble, tremble));

        float split = time < OPEN_TIME ? 0f : Interpolation.pow2Out.apply(Math.min(1f, (time - OPEN_TIME) / 0.5f));
        float spin = time < OPEN_TIME ? MathUtils.sin(time * 4f) * 12f : 0f;
        top.setRotation(spin + split * 25f);
        bottom.setRotation(spin - split * 25f);
        placeOrigin(top, at.x - split * 120f, at.y + split * 420f);
        placeOrigin(bottom, at.x + split * 120f, at.y - split * 420f);
        float fade = Math.max(0f, 1f - split * 1.4f);
        top.getColor().a = fade;
        bottom.getColor().a = fade;

        cracks.setVisible(time >= CRACK_TIME && time < OPEN_TIME);
        cracks.setRotation(spin);
        cracks.getColor().a = 0.5f + 0.5f * MathUtils.sin(time * 40f);
        placeOrigin(cracks, at.x, at.y);

        // Dans sa fenêtre, puis libéré et géant.
        float freed = time < OPEN_TIME ? 0f : Interpolation.swingOut.apply(Math.min(1f, (time - OPEN_TIME) / 0.35f));
        icon.setScale(ICON_SIZE / FREED_SIZE + (1f - ICON_SIZE / FREED_SIZE) * freed);
        icon.setRotation(time < OPEN_TIME ? spin : MathUtils.sin(time * 6f) * 4f);
        placeOrigin(icon, at.x, at.y);

        glow.setScale(time < OPEN_TIME ? 3f + 1.5f * land : 6f + 0.5f * MathUtils.sin(time * 9f));
        glow.getColor().a = time < OPEN_TIME ? 0.4f : 0.85f;
        placeOrigin(glow, at.x, at.y);

        updateGods();

        if (time < OPEN_TIME) return;
        float since = time - OPEN_TIME;
        for (int i = 0; i < RAYS; i++) {
            Image ray = rays[i];
            ray.setVisible(true);
            ray.setScaleX(Math.min(1f, since / 0.25f));
            ray.setRotation(i * 360f / RAYS + since * 30f);
            ray.setPosition(at.x, at.y - ray.getHeight() / 2f);
            float pulse = 0.25f + 0.1f * MathUtils.sin(since * 8f + i);
            if (i % 2 == 0) ray.setColor(1f, 0.88f, 0.4f, pulse);
            else            ray.setColor(1f, 1f, 1f, pulse);
        }
    }

    /** Le dieu et la femme voilée apparaissent, il lance ses éclairs, elle lève les bras et les anges arrivent. */
    private void updateGods() {
        float appear = Interpolation.pow2Out.apply(Math.min(1f, Math.max(0f, time - APPEAR) / 0.4f));
        float floatY = MathUtils.sin(time * 2.5f) * 8f;
        float godW = zeus.getWidth(), godH = zeus.getHeight();
        float feetY = center.y - godH * 0.55f - 80f * (1f - appear) + floatY;
        float zeusX = center.x - SIDE - godW / 2f, maryX = center.x + SIDE - mary.getWidth() / 2f;
        for (Image part : new Image[] {zeus, zeusArm, mary, maryStars, maryLeftArm, maryRightArm}) {
            part.getColor().a = appear;
            part.setVisible(time >= APPEAR);
        }
        zeus.setPosition(zeusX, feetY);
        mary.setPosition(maryX, feetY);

        // Le bras du dieu, levé derrière sa tête : il arme, puis lance vers le symbole à chaque éclair
        // (angle depuis la verticale basse, sens trigonométrique : 180 = bras levé, 100 = vers la droite).
        float swing = 210f;
        for (float boltAt : BOLTS) {
            float since = time - boltAt;
            if (since >= -0.18f && since < 0f) swing = MathUtils.lerp(210f, 235f, (since + 0.18f) / 0.18f);
            else if (since >= 0f && since < 0.3f) swing = since < 0.12f
                ? MathUtils.lerp(235f, 105f, Interpolation.pow2Out.apply(since / 0.12f))
                : MathUtils.lerp(105f, 210f, (since - 0.12f) / 0.18f);
        }
        float shoulderX = zeusX + ZEUS_SHOULDER_X * GOD_SCALE, shoulderY = feetY + godH - ZEUS_SHOULDER_Y * GOD_SCALE;
        zeusArm.setRotation(swing);
        placeOrigin(zeusArm, shoulderX, shoulderY);
        float armLength = zeusArm.getHeight();
        zeusHand.set(shoulderX + MathUtils.sinDeg(swing) * armLength, shoulderY - MathUtils.cosDeg(swing) * armLength);
        updateBolt();

        // La femme voilée lève les deux bras.
        float raise = Interpolation.swingOut.apply(Math.min(1f, Math.max(0f, time - CRACK_TIME) / 0.35f));
        float wave = time > CRACK_TIME + 0.35f ? MathUtils.sin(time * 6f) * 6f : 0f;
        float maryShoulderY = feetY + mary.getHeight() - MARY_SHOULDER_Y * GOD_SCALE;
        maryLeftArm.setRotation(MathUtils.lerp(-12f, -150f, raise) - wave);
        maryRightArm.setRotation(MathUtils.lerp(12f, 150f, raise) + wave);
        placeOrigin(maryLeftArm, maryX + MARY_LEFT_X * GOD_SCALE, maryShoulderY);
        placeOrigin(maryRightArm, maryX + MARY_RIGHT_X * GOD_SCALE, maryShoulderY);
        // Les douze étoiles au-dessus de sa tête scintillent.
        maryStars.setColor(1f, 1f, 1f, appear * (0.75f + 0.25f * MathUtils.sin(time * 8f)));
        maryStars.setPosition(maryX, feetY + mary.getHeight() + 4 * GOD_SCALE - maryStars.getHeight());
        maryCenter.set(maryX + mary.getWidth() / 2f, feetY + mary.getHeight() * 0.6f);

        // Les anges apparaissent en nombre autour d'elle et volettent.
        for (int i = 0; i < ANGELS; i++) {
            Image angel = angels[i];
            float since = time - angelAt[i];
            angel.setVisible(since >= 0f);
            if (since < 0f) continue;
            float grow = Interpolation.swingOut.apply(Math.min(1f, since / 0.3f));
            float flap = 0.75f + 0.25f * Math.abs(MathUtils.sin(time * 14f + angelPhase[i]));
            angel.setScale(grow * (angelX[i] < 0f ? -1f : 1f), grow * flap);
            angel.getColor().a = Math.min(1f, since / 0.2f);
            placeOrigin(angel, maryCenter.x + angelX[i] + MathUtils.sin(time * 3f + angelPhase[i]) * 14f,
                maryCenter.y + angelY[i] + since * 30f + MathUtils.cos(time * 4f + angelPhase[i]) * 10f);
        }
    }

    /** L'éclair en cours : une ligne brisée de la main du dieu vers le haut de la table, tirée à chaque lancer. */
    private void updateBolt() {
        int current = -1;
        for (int i = 0; i < BOLTS.length; i++) {
            if (time >= BOLTS[i] && time < BOLTS[i] + BOLT_LIFE) current = i;
        }
        if (current < 0) {
            for (Image segment : bolt) segment.setVisible(false);
            return;
        }
        if (current != boltShown) {
            boltShown = current;
            float sky = getStage() != null ? getStage().getHeight() : 1080f;
            boltEnd.set(center.x + MathUtils.random(-250f, 500f), sky * MathUtils.random(0.6f, 0.95f));
            boltPoints[0].set(zeusHand);
            for (int k = 1; k <= BOLT_SEGMENTS; k++) {
                float t = (float) k / BOLT_SEGMENTS;
                float jitter = k == BOLT_SEGMENTS ? 0f : MathUtils.random(-45f, 45f);
                boltPoints[k].set(MathUtils.lerp(zeusHand.x, boltEnd.x, t) + jitter,
                    MathUtils.lerp(zeusHand.y, boltEnd.y, t) + jitter * 0.6f);
            }
        }
        float life = 1f - (time - BOLTS[current]) / BOLT_LIFE;
        for (int k = 0; k < BOLT_SEGMENTS; k++) {
            Image segment = bolt[k];
            Vector2 a = boltPoints[k], b = boltPoints[k + 1];
            float length = a.dst(b);
            segment.setVisible(true);
            segment.setSize(length + 4f, 18f);
            segment.setOrigin(0f, 9f);
            segment.setPosition(a.x, a.y - 9f);
            segment.setRotation(MathUtils.atan2(b.y - a.y, b.x - a.x) * MathUtils.radiansToDegrees);
            segment.setColor(0.92f, 0.97f, 1f, Math.min(1f, life * 1.5f));
        }
    }

    /** @return l'image {@code path} (pixel art fait à part), chargée telle quelle. */
    private static Pixmap png(String path) {
        return new Pixmap(Gdx.files.internal(path));
    }

    /** @return un petit ange : robe blanche, ailes, cheveux blonds et auréole. */
    private static Pixmap angel() {
        Pixmap pixmap = pixmap(15, 13);
        String[] rows = {
            ".....ggggg.....",
            "......hhh......",
            "ww...hsssh...ww",
            "www...sss...www",
            "wwww..rrr..wwww",
            ".wwwwrrrrrwwww.",
            "..wwrrrrrrrww..",
            "....rrrrrrr....",
            "....rrrrrrr....",
            "...rrrrrrrrr...",
            "...rrrrrrrrr...",
            "....s.....s....",
        };
        drawGrid(pixmap, rows, 0, 0, "gwhsr", hex("f2c440"), hex("e8f0ff"), hex("f2d070"), hex("f4d4b8"), hex("ffffff"));
        return pixmap;
    }

    /** @return une moitié de la boule : or brillant, bande blanche, fenêtre ronde au centre (vide). */
    private static Pixmap half(boolean upper) {
        int h = BALL / 2;
        Pixmap pixmap = pixmap(BALL, h);
        Color gold = hex("f2c440"), light = hex("ffe890"), shade = hex("c08a18"), band = hex("fff8e8");
        int r = BALL / 2;
        Shape shape = (x, y) -> {
            int by = upper ? y : y + h;                      // ordonnée dans la boule entière
            float dx = x + 0.5f - r, dy = by + 0.5f - r;
            boolean ball = dx * dx + dy * dy <= (r - 1) * (r - 1);
            boolean window = dx * dx + dy * dy <= 9.5f * 9.5f;
            return ball && !window;
        };
        fillShape(pixmap, shape, (x, y) -> {
            int by = upper ? y : y + h;
            if (Math.abs(by + 0.5f - r) < 12f && Math.abs(by + 0.5f - r) > 9f) return band;
            if (x + by < BALL * 0.7f) return light;
            if (x + by > BALL * 1.35f) return shade;
            return gold;
        });
        return pixmap;
    }

    /** @return des fissures de lumière en étoile, sur toute la boule. */
    private static Pixmap cracks() {
        Pixmap pixmap = pixmap(BALL, BALL);
        pixmap.setColor(1f, 1f, 0.85f, 1f);
        int c = BALL / 2;
        int[][] ends = {{2, 10}, {33, 6}, {30, 33}, {4, 29}, {18, 1}, {35, 20}};
        for (int[] end : ends) {
            int mx = (c + end[0]) / 2 + (end[1] % 3) - 1, my = (c + end[1]) / 2 + (end[0] % 3) - 1;
            pixmap.drawLine(c, c, mx, my);
            pixmap.drawLine(mx, my, end[0], end[1]);
        }
        return pixmap;
    }
}
