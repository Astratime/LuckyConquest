package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;

/**
 * Les rois de la prairie entrent en scène, au fond de leur donjon de pierre :
 * <ul>
 *   <li>Pique : un ciel d'orage, un éclair. Des lames tombent du plafond et se plantent en cercle ; le roi
 *   sort du centre. Il fait tourner son épée, les lames se lèvent.</li>
 *   <li>Trèfle : un trèfle à quatre feuilles pousse dans l'herbe. Une pluie de pièces s'empile ; le tas
 *   éclate, c'est le roi. Il attrape la dernière, la range dans sa poche et te fait signe d'approcher.</li>
 *   <li>Coeur : un vitrail en cœur s'éclaire. Les murs battent ; un cœur géant s'ouvre et le roi en sort.
 *   Il boit dans sa coupe, une goutte rouge tombe.</li>
 *   <li>Carreau : une carte au trésor se déroule, une croix rouge. Un tas de trésor bouge ; le roi se
 *   réveille dessus et lève son écu, éclat de diamant. Il pose son écu sur le tas, les pièces glissent.</li>
 * </ul>
 */
public class PrairieKings extends KingEntrance {

    /** Pique : les lames tombent l'une après l'autre, puis le roi sort. */
    static final float BLADES = 0.25f, BLADE_STEP = 0.17f, BLADE_FALL = 0.22f, SPADE_RISE = 1.8f;
    /** Trèfle : la pluie de pièces, le tas qui éclate, la dernière pièce. */
    static final float RAIN_END = 1.7f, CLUB_BURST = 1.9f, LAST_COIN = 2.55f, CATCH = 2.9f;
    /** Coeur : les battements du cœur géant, puis il s'ouvre. */
    static final float[] HEART_BEATS = {0.3f, 0.55f, 0.95f, 1.18f, 1.5f, 1.68f};
    static final float HEART_OPEN = 2.0f;
    /** Carreau : le tas tremble, le roi en sort, l'éclat du diamant. */
    static final float PILE_SHAKE = 0.5f, DIAMOND_RISE = 1.7f, DIAMOND_FLASH = 2.55f;

    /** Le décor. Pique : les éclairs. Trèfle : la tige, les feuilles une à une, l'éclat. */
    static final float[] BOLTS = {0.6f, 1.4f};
    static final float STEM = 0.2f, LEAVES = 0.85f, LEAF_STEP = 0.18f, CLOVER_SHINE = 1.65f;
    /** Coeur : le vitrail s'éclaire. Carreau : la carte se déroule, le chemin, la croix. */
    static final float GLASS_LIGHT = 0.3f, GLASS_FULL = 1.4f;
    static final float UNROLL = 0.15f, UNROLLED = 0.95f, PATH_END = 1.45f, X_MARK = 1.5f;
    /** Le geste. Pique : l'épée tourne, les lames se lèvent. Trèfle : la poche, le signe. */
    static final float SPIN_END = 4.3f, BLADES_UP = 4.2f, POCKET = 3.9f, BECKON = 4.1f, BECKON_END = 5.3f;
    /** Coeur : il boit, la goutte tombe. Carreau : il pose son écu, les pièces glissent. */
    static final float DRINK_END = 4.4f, DROP = 4.0f, DROP_LAND = 4.55f, SHIELD_DOWN = 3.85f;

    private static final int BLADE_COUNT = 8;

    private final Color tint, wall, wallDark, floor;
    private final TextureRegion blade, coin, heart, heartLeft, heartRight, leaf, stainedGlass;
    private float pile;
    private boolean bursting;

    public PrairieKings(CutsceneKit kit, EnemyKind kind) {
        super(kit, kind);
        tint = switch (kind) {
            case ROI_PIQUE -> c("6a8ad8");
            case ROI_TREFLE -> c("3fbf5a");
            case ROI_COEUR -> c("e0283a");
            default -> c("ff9a2a");
        };
        wall = c("2e2a3e");
        wallDark = c("1c1a28");
        floor = c("141220");
        Color[] metal = {c("140a0a"), c("e8e8f0"), c("9a9ab0"), c("ffc93a"), c("6b3a1e")};
        blade = art("owsgb", metal,
            ".ggg.",
            ".gog.",
            "..b..",
            "..b..",
            "ggggg",
            ".sws.",
            ".sws.",
            ".sws.",
            ".sws.",
            ".sws.",
            ".sws.",
            ".sws.",
            ".sws.",
            "..s..");
        coin = load("hud/coin.png");
        Color[] red = {c("1a0a0e"), c("e0283a"), c("ff8a9a"), c("8a1020")};
        String[] heartRows = {
            ".ooo...ooo.",
            "orrro.orrro",
            "orwrrorrrro",
            "orwrrrrrrro",
            "orrrrrrrrdo",
            ".orrrrrrdo.",
            "..orrrrdo..",
            "...orrdo...",
            "....odo....",
            ".....o....."};
        heart = art("orwd", red, heartRows);
        heartLeft = new TextureRegion(heart, 0, 0, 6, 10);
        heartRight = new TextureRegion(heart, 5, 0, 6, 10);
        leaf = art("orwd", new Color[] {c("0a1a0c"), c("3fbf5a"), c("9af0a8"), c("1f7a34")}, heartRows);
        Color[] glassColors = {c("140a10"), c("e0283a"), c("ff8a9a"), c("ffc93a"), c("8a1020")};
        stainedGlass = art("orpyd", glassColors,
            ".ooo...ooo.",
            "oyrro.orrpo",
            "orrpoooprro",
            "oprroyorrdo",
            "ooooooooooo",
            ".oyrrorpdo.",
            "..orrordo..",
            "...ooyoo...",
            "....odo....",
            ".....o.....");
    }

    @Override
    protected void restart() {
        pile = 0f;
        bursting = false;
    }

    @Override
    protected void stepPrelude(float delta) {
        switch (kind) {
            case ROI_PIQUE -> {
                for (float bolt : BOLTS) if (at(bolt)) rumble(0.3f, 8f);
            }
            case ROI_TREFLE -> {
                if (at(CLOVER_SHINE)) sparks(cx(), cloverY(), 30, c("bfffc8"), 500f, 12f);
            }
            case ROI_COEUR -> {
                if (at(GLASS_FULL)) sparks(cx(), glassY(), 30, c("ff8a9a"), 500f, 14f);
            }
            default -> {
                if (at(X_MARK)) sparks(markX(), markY(), 26, c("ff4a3a"), 400f, 12f);
            }
        }
    }

    @Override
    protected void prelude(Batch batch) {
        switch (kind) {
            case ROI_PIQUE -> drawStorm(batch);
            case ROI_TREFLE -> drawClover(batch);
            case ROI_COEUR -> drawStainedGlass(batch);
            default -> drawMap(batch);
        }
    }

    @Override
    protected void step(float delta) {
        switch (kind) {
            case ROI_PIQUE -> {
                if (at(BLADES_UP)) {
                    for (int i = 0; i < BLADE_COUNT; i++) sparks(bladeX(i), bladeY(i), 6, Color.WHITE, 300f, 8f);
                    rumble(0.25f, 6f);
                }
                for (int i = 0; i < BLADE_COUNT; i++) {
                    if (at(BLADES + i * BLADE_STEP + BLADE_FALL)) {
                        sparks(bladeX(i), bladeY(i), 10, Color.WHITE, 400f, 10f);
                        rumble(0.12f, 4f);
                    }
                }
                if (at(SPADE_RISE + 0.5f)) {
                    rumble(0.3f, 10f);
                    sparks(cx(), kingY(), 40, tint, 700f, 18f);
                }
            }
            case ROI_TREFLE -> {
                if (time < RAIN_END) {                       // pluie de pièces sur le tas
                    for (int i = 0; i < 3; i++) {
                        if (random.nextFloat() > 40f * delta) continue;
                        Particle drop = particle(cx() + (random.nextFloat() - 0.5f) * worldWidth() * 0.35f,
                            worldHeight() + 60f, 270f, 300f, 3f, 1f);
                        drop.gravity = 1600f;
                        drop.region = coin;
                        drop.spin = (random.nextFloat() - 0.5f) * 600f;
                        parts.add(drop);
                    }
                    pile = Math.min(1f, pile + delta / (RAIN_END - 0.2f));
                }
                for (int i = parts.size - 1; i >= 0; i--) {   // les pièces se posent sur le tas
                    Particle drop = parts.get(i);
                    if (drop.region == coin && drop.vy < 0f && drop.y < pileTop() && !bursting) parts.removeIndex(i);
                }
                if (at(CLUB_BURST)) {
                    bursting = true;
                    rumble(0.35f, 12f);
                    debris(cx(), kingY() + 80f, 50, coin, 1.1f, 1300f);
                    sparks(cx(), kingY() + 200f, 40, c("ffc93a"), 800f, 16f);
                }
                if (at(CATCH)) sparks(catchX(), catchY(), 20, c("ffe58a"), 300f, 10f);
                if (at(POCKET)) sparks(pocketX(), pocketY(), 14, c("ffe58a"), 200f, 8f);
            }
            case ROI_COEUR -> {
                for (float beat : HEART_BEATS) if (at(beat)) rumble(0.12f, 3f);
                if (at(HEART_OPEN)) {
                    rumble(0.4f, 12f);
                    sparks(cx(), worldHeight() * 0.5f, 50, tint, 900f, 20f);
                }
                if (at(DROP_LAND)) sparks(cupX(), kingY() + 6f, 18, tint, 300f, 10f);
            }
            default -> {
                if (time >= PILE_SHAKE && time < DIAMOND_RISE && random.nextFloat() < 10f * delta) {
                    debris(cx() + (random.nextFloat() - 0.5f) * 400f, kingY() + 120f, 2, coin, 0.6f, 500f);
                }
                if (at(DIAMOND_RISE)) rumble(0.4f, 10f);
                if (at(DIAMOND_FLASH)) sparks(diamondX(), diamondY(), 30, c("bfe8ff"), 600f, 14f);
                if (at(SHIELD_DOWN)) {
                    rumble(0.3f, 8f);
                    for (int side = -1; side <= 1; side += 2) {      // les pièces glissent de chaque côté du tas
                        for (int i = 0; i < 10; i++) {
                            Particle piece = particle(cx() + side * (60f + 140f * random.nextFloat()),
                                worldHeight() * 0.04f + 220f + 60f * random.nextFloat(), side < 0 ? 160f + 15f
                                * random.nextFloat() : 20f - 15f * random.nextFloat(), 300f + 400f * random.nextFloat(),
                                1.4f, 1.1f);
                            piece.gravity = 900f;
                            piece.region = coin;
                            piece.spin = side * 400f;
                            parts.add(piece);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void draw(Batch batch) {
        drawDungeon(batch);
        switch (kind) {
            case ROI_PIQUE -> drawSpade(batch);
            case ROI_TREFLE -> drawClub(batch);
            case ROI_COEUR -> drawHeart(batch);
            default -> drawDiamond(batch);
        }
        drawParticles(batch, parts);
    }

    /** Le fond du donjon : mur de pierre, sol, deux torches, teinté de la couleur du roi. */
    private void drawDungeon(Batch batch) {
        float width = worldWidth(), height = worldHeight(), brick = 48f;
        rect(batch, wallDark, -MARGIN, -MARGIN, width + 2f * MARGIN, height + 2f * MARGIN);
        for (int row = 0; row * brick < height + MARGIN; row++) {
            for (float x = -MARGIN - (row % 2) * brick; x < width + MARGIN; x += brick * 2f) {
                rect(batch, wall, x + 3f, row * brick + 3f, brick * 2f - 6f, brick - 6f);
            }
        }
        rect(batch, floor, -MARGIN, -MARGIN, width + 2f * MARGIN, height * 0.2f + MARGIN);
        glow(batch, cx(), height * 0.35f, width * 1.1f, tint, 0.18f);
        for (int side = -1; side <= 1; side += 2) {                // torches
            float x = cx() + side * width * 0.38f, y = height * 0.6f;
            rect(batch, c("4a2614"), x - 8f, y - 60f, 16f, 60f);
            float flicker = pulse(19f + side * 3f, 0.75f);
            glow(batch, x, y + 10f, 340f * flicker, c("ff7a1a"), 0.4f);
            glow(batch, x, y + 10f, 70f * flicker, c("ffe066"), 0.9f);
        }
        // Vignette sombre.
        batch.setColor(0f, 0f, 0f, 0.5f);
        batch.draw(ring, -width * 0.25f, -height * 0.6f, width * 1.5f, height * 2.2f);
    }

    // -------------------------------------------------------------------------
    // Pique : les lames en cercle
    // -------------------------------------------------------------------------

    private void drawSpade(Batch batch) {
        float spin = gesture(GESTURE, SPIN_END);
        pose(0f, 14f * spin, 0f, 0.03f * spin);
        float rise = Interpolation.pow2Out.apply(progress(SPADE_RISE, SPADE_RISE + 0.5f));
        // Les lames du fond, le roi, puis les lames de devant.
        drawBlades(batch, true);
        if (time >= SPADE_RISE) {
            float y = MathUtils.lerp(kingY() - 400f, kingY(), rise);
            glow(batch, cx(), kingY() + 200f, 900f * rise, tint, 0.45f);
            drawKing(batch, cx(), y, KING_SCALE, 1f - progress(SPADE_RISE + 0.4f, SPADE_RISE + 0.8f), rise);
            float raised = progress(SPADE_RISE + 0.6f, SPADE_RISE + 1f);       // l'épée levée qui brille
            glow(batch, cx() + 12f * KING_SCALE, kingY() + 38f * KING_SCALE, 260f, Color.WHITE, 0.7f * raised
                * pulse(10f, 0.6f));
            if (spin > 0f) {                                                   // l'épée tourne : un cercle de lumière
                float sx = cx() + 12f * KING_SCALE, sy = kingY() + 38f * KING_SCALE + 14f * spin;
                for (int j = 0; j < 14; j++) {
                    float angle = -(time - GESTURE) * 1100f + j * 14f;
                    glow(batch, sx + MathUtils.cosDeg(angle) * 190f, sy + MathUtils.sinDeg(angle) * 190f,
                        120f * (1f - j / 16f), j == 0 ? Color.WHITE : tint, spin * (1f - j / 14f));
                }
            }
        }
        drawBlades(batch, false);
        flash(batch, Color.WHITE, SPADE_RISE + 0.5f, 0.2f, 0.5f);
    }

    private void drawBlades(Batch batch, boolean back) {
        for (int i = 0; i < BLADE_COUNT; i++) {
            boolean isBack = MathUtils.sinDeg(bladeAngle(i)) > 0f;
            if (isBack != back) continue;
            float start = BLADES + i * BLADE_STEP;
            if (time < start) continue;
            float fall = Interpolation.pow2In.apply(progress(start, start + BLADE_FALL));
            float up = Interpolation.pow2Out.apply(progress(BLADES_UP + i * 0.04f, BLADES_UP + 0.45f + i * 0.04f));
            float lift = up * (150f + 12f * MathUtils.sin(time * 3f + i));           // les lames se lèvent
            float x = bladeX(i), y = MathUtils.lerp(worldHeight() + 200f, bladeY(i), fall) + lift;
            if (up > 0f) glow(batch, x, y + 60f, 200f, tint, 0.4f * up);
            float scale = 14f * (back ? 0.85f : 1.05f);
            batch.setColor(back ? 0.75f : 1f, back ? 0.75f : 1f, back ? 0.8f : 1f, 1f);
            batch.draw(blade, x - blade.getRegionWidth() * scale / 2f, y, blade.getRegionWidth() * scale,
                blade.getRegionHeight() * scale);
        }
    }

    private float bladeAngle(int i) { return 270f + i * 360f / BLADE_COUNT + 22.5f; }
    private float bladeX(int i)     { return cx() + MathUtils.cosDeg(bladeAngle(i)) * worldWidth() * 0.32f; }
    private float bladeY(int i)     { return kingY() - 40f + MathUtils.sinDeg(bladeAngle(i)) * worldHeight() * 0.09f; }

    // -------------------------------------------------------------------------
    // Trèfle : la pluie de pièces
    // -------------------------------------------------------------------------

    private void drawClub(Batch batch) {
        float beckoning = gesture(BECKON, BECKON_END);
        if (beckoning > 0f) {
            float wave = Math.abs(MathUtils.sin((time - BECKON) * 2f * MathUtils.PI / (BECKON_END - BECKON)));
            pose(0f, -10f * wave * beckoning, -7f * wave * beckoning, 0f);
        }
        if (!bursting) {
            drawPile(batch, cx(), kingY() - 30f, pile, 0f);
        } else {
            float since = time - CLUB_BURST;
            glow(batch, cx(), kingY() + 220f, 1000f, tint, 0.4f * Math.max(0.4f, 1f - since));
            drawKing(batch, cx(), kingY(), KING_SCALE, 0f, 1f);
            float white = Math.max(0f, 1f - since / 0.35f);              // il sort du tas en blanc
            if (white > 0f) fill(batch, Color.WHITE, 0.6f * white);
            if (time >= LAST_COIN && time < CATCH) {                      // la dernière pièce tombe dans sa main
                float k = Interpolation.pow2In.apply(progress(LAST_COIN, CATCH));
                float y = MathUtils.lerp(worldHeight() + 60f, catchY(), k);
                batch.setColor(Color.WHITE);
                sprite(batch, coin, catchX(), y, 1.4f, time * 600f);
            } else if (time >= CATCH && time < POCKET) {               // il la range dans sa poche
                float k = Interpolation.pow2In.apply(progress(GESTURE, POCKET));
                float x = MathUtils.lerp(catchX(), pocketX(), k), y = MathUtils.lerp(catchY(), pocketY(), k)
                    + 60f * MathUtils.sin(k * MathUtils.PI);
                glow(batch, x, y, 160f, c("ffc93a"), 0.7f * pulse(8f, 0.5f));
                batch.setColor(Color.WHITE);
                sprite(batch, coin, x, y, 1.4f * (1f - 0.5f * k), 0f);
            }
            // Puis il te fait signe d'approcher : il se penche vers toi, deux fois, la main qui luit.
            if (beckoning > 0f) glow(batch, catchX(), catchY() + 30f, 200f, tint, 0.5f * beckoning);
        }
    }

    /** Un tas de pièces en pyramide, rempli à {@code fill} (0 à 1). */
    private void drawPile(Batch batch, float x, float y, float fill, float shake) {
        int rows = 9;
        int shown = MathUtils.ceil(rows * fill * 6f);               // nombre de pièces posées
        int placed = 0;
        batch.setColor(Color.WHITE);
        for (int row = 0; row < rows && placed < shown; row++) {
            int count = rows - row;
            for (int k = 0; k < count && placed < shown; k++, placed++) {
                float px = x + (k - (count - 1) / 2f) * 58f + MathUtils.sin(time * 40f + k + row) * shake;
                float py = y + row * 34f;
                sprite(batch, coin, px, py, 1.25f, (k * 37 + row * 11) % 30 - 15f);
            }
        }
    }

    private float pileTop() { return kingY() - 30f + pile * 9f * 34f / 1.5f; }
    private float catchX()  { return cx() + 13f * KING_SCALE; }
    private float catchY()  { return kingY() + 16f * KING_SCALE; }
    private float pocketX() { return cx() - 6f * KING_SCALE; }
    private float pocketY() { return kingY() + 12f * KING_SCALE; }

    // -------------------------------------------------------------------------
    // Coeur : le cœur géant qui s'ouvre
    // -------------------------------------------------------------------------

    private void drawHeart(Batch batch) {
        // Les murs battent : une lueur rouge à chaque battement.
        float beat = 0f;
        for (float at : HEART_BEATS) if (time >= at) beat = Math.max(beat, 1f - (time - at) / 0.22f);
        beat = Math.max(0f, beat);
        fill(batch, tint, 0.18f * beat);
        float heartX = cx(), heartY = worldHeight() * 0.5f;
        float drink = gesture(GESTURE, DRINK_END);
        pose(0f, 0f, 7f * drink, 0.02f * drink);                 // il boit : la tête en arrière
        float open = Interpolation.pow2Out.apply(progress(HEART_OPEN, HEART_OPEN + 0.6f));
        if (time >= HEART_OPEN) {                                 // le roi, derrière le cœur
            glow(batch, heartX, heartY, 1100f * open, tint, 0.5f);
            drawKing(batch, cx(), kingY(), KING_SCALE, 1f - open, open);
            glow(batch, cx() + 12f * KING_SCALE, kingY() + 20f * KING_SCALE, 200f, c("ffc93a"), 0.6f * open
                * pulse(7f, 0.5f));
        }
        float scale = 40f * (1f + 0.12f * beat);
        if (open < 1f) {
            float alpha = 1f - open;
            float gap = open * worldWidth() * 0.45f;
            batch.setColor(1f, 1f, 1f, alpha);
            float w = heartLeft.getRegionWidth() * scale, h = heartLeft.getRegionHeight() * scale;
            batch.draw(heartLeft, heartX - w + scale / 2f - gap, heartY - h / 2f, w - scale / 2f, h / 2f, w, h, 1f, 1f,
                open * 25f);
            batch.draw(heartRight, heartX - scale / 2f + gap, heartY - h / 2f, scale / 2f, h / 2f, w, h, 1f, 1f,
                -open * 25f);
        }
        if (time >= DROP && time < DROP_LAND) {                   // la goutte rouge qui tombe de la coupe
            float k = Interpolation.pow2In.apply(progress(DROP, DROP_LAND));
            float y = MathUtils.lerp(cupY(), kingY() + 6f, k);
            glow(batch, cupX(), y, 90f, tint, 0.7f);
            rect(batch, c("8a1020"), cupX() - 8f, y - 10f, 16f, 22f);
            rect(batch, tint, cupX() - 5f, y - 8f, 10f, 18f);
        } else if (time >= DROP_LAND) {                           // la tache sur le sol
            float k = progress(DROP_LAND, DROP_LAND + 0.2f);
            rect(batch, c("8a1020"), cupX() - 30f * k, kingY() - 2f, 60f * k, 10f);
        }
        flash(batch, tint, HEART_OPEN, 0.25f, 0.5f);
    }

    private float cupX() { return cx() + 12f * KING_SCALE; }
    private float cupY() { return kingY() + 18f * KING_SCALE; }

    // -------------------------------------------------------------------------
    // Carreau : le trésor qui bouge
    // -------------------------------------------------------------------------

    private void drawDiamond(Batch batch) {
        float shake = time < PILE_SHAKE ? 0f : time < DIAMOND_RISE ? 4f + 10f * progress(PILE_SHAKE, DIAMOND_RISE)
            : time >= SHIELD_DOWN && time < SHIELD_DOWN + 0.5f ? 10f * (1f - progress(SHIELD_DOWN, SHIELD_DOWN + 0.5f)) : 0f;
        float bow = gesture(GESTURE, SHIELD_DOWN + 0.45f);         // il se penche et pose son écu sur le tas
        pose(0f, -50f * bow, -5f * bow, -0.04f * bow);
        float rise = Interpolation.swingOut.apply(progress(DIAMOND_RISE, DIAMOND_RISE + 0.6f));
        if (time >= DIAMOND_RISE) {
            glow(batch, cx(), kingY() + 260f, 1000f * rise, tint, 0.4f);
            float y = MathUtils.lerp(kingY() - 300f, kingY() + 40f, rise);
            drawKing(batch, cx(), y, KING_SCALE, 1f - progress(DIAMOND_RISE + 0.3f, DIAMOND_RISE + 0.7f),
                Math.min(1f, rise * 2f));
        }
        // Le tas de trésor, devant le roi : pièces et gemmes.
        drawPile(batch, cx(), worldHeight() * 0.04f, 1f, shake);
        for (int i = 0; i < 7; i++) {
            float gx = cx() + (i - 3) * 90f + MathUtils.sin(time * 40f + i) * shake, gy = worldHeight() * 0.04f
                + 40f + (i % 3) * 50f;
            Color gem = i % 2 == 0 ? c("4fd8ff") : c("e0283a");
            glow(batch, gx, gy, 70f, gem, 0.6f * pulse(5f + i, 0.4f));
            rect(batch, gem, gx - 9f, gy - 9f, 18f, 18f);
        }
        if (time >= DIAMOND_FLASH) {                              // l'éclat de diamant sur l'écu
            float k = progress(DIAMOND_FLASH, DIAMOND_FLASH + 0.6f);
            float size = 600f * Interpolation.pow2Out.apply(k) * (1f - k * 0.5f);
            additive(batch);
            batch.setColor(0.8f, 0.95f, 1f, 1f - k);
            batch.draw(trail, diamondX() - size, diamondY() - 6f, size, 6f, size, 12f, 1f, 1f, 0f);
            batch.draw(trail, diamondX() - size, diamondY() - 6f, size, 6f, size, 12f, 1f, 1f, 180f);
            batch.draw(trail, diamondX() - size, diamondY() - 6f, size, 6f, size, 12f, 1f, 1f, 90f);
            batch.draw(trail, diamondX() - size, diamondY() - 6f, size, 6f, size, 12f, 1f, 1f, 270f);
            normal(batch);
            glow(batch, diamondX(), diamondY(), 220f, c("bfe8ff"), 1f - k);
        }
    }

    private float diamondX() { return cx() - 1f * KING_SCALE; }
    private float diamondY() { return kingY() + 40f + 9f * KING_SCALE; }

    // -------------------------------------------------------------------------
    // Le décor, avant l'entrée
    // -------------------------------------------------------------------------

    /** Pique : un ciel d'orage au-dessus du donjon, la pluie, deux éclairs. */
    private void drawStorm(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        float lit = 0f;
        for (float bolt : BOLTS) if (time >= bolt) lit = Math.max(lit, 1f - (time - bolt) / 0.3f);
        lit = Math.max(0f, lit);
        gradient(batch, c("0a0c18"), c("262c4a"));
        for (int i = 0; i < 14; i++) {                               // les nuages qui roulent
            float x = ((i * 233f + time * (40f + i * 6f)) % (width + 600f)) - 300f;
            float y = height * (0.72f + 0.05f * (i % 4)), size = 420f + 60f * (i % 5);
            float grey = 0.12f + 0.3f * lit;
            batch.setColor(grey, grey, grey + 0.06f, 0.85f);
            batch.draw(soft, x - size / 2f, y - size * 0.22f, size, size * 0.45f);
        }
        for (int i = 0; i < BOLTS.length; i++) {
            float alpha = time >= BOLTS[i] ? 1f - (time - BOLTS[i]) / 0.3f : 0f;
            bolt(batch, width * (i == 0 ? 0.3f : 0.68f), height * 0.3f, 11L + i, alpha);
        }
        // Le donjon en ombre, ses tours et ses créneaux.
        Color wallColor = tmp.set(0.03f, 0.03f, 0.06f, 1f).lerp(c("2e2a3e"), 0.5f * lit);
        rect(batch, wallColor, -MARGIN, -MARGIN, width + 2f * MARGIN, height * 0.22f + MARGIN);
        for (int t = 0; t < 3; t++) {
            float x = width * (0.18f + t * 0.32f), w = t == 1 ? 260f : 180f, h = height * (t == 1 ? 0.5f : 0.36f);
            rect(batch, wallColor, x - w / 2f, height * 0.2f, w, h);
            for (int k = 0; k < 4; k++) rect(batch, wallColor, x - w / 2f + k * w / 3.5f, height * 0.2f + h, w / 7f, 34f);
            glow(batch, x, height * 0.2f + h * 0.6f, 60f, c("ffb040"), 0.6f * pulse(9f + t, 0.6f));
        }
        rain(batch, 160, 0.35f);
        fill(batch, c("c8d8ff"), 0.45f * lit * (settings.isReducedEffects() ? 0.3f : 1f));
    }

    /** Trèfle : la prairie au crépuscule ; un trèfle à quatre feuilles pousse au milieu. */
    private void drawClover(Batch batch) {
        float width = worldWidth(), height = worldHeight(), ground = height * 0.24f;
        gradient(batch, c("2a3a3a"), c("0e1430"));
        glow(batch, width * 0.8f, height * 0.8f, 300f, c("fff2c0"), 0.5f);              // la lune
        rect(batch, c("123a1e"), -MARGIN, -MARGIN, width + 2f * MARGIN, ground + MARGIN);
        for (int i = 0; i < 90; i++) {                                                 // l'herbe qui ondule
            float x = i * (width + 2f * MARGIN) / 90f - MARGIN, h = 50f + (i * 37 % 7) * 14f;
            float sway = 10f * MathUtils.sin(time * 2f + i * 0.5f);
            line(batch, i % 3 == 0 ? c("2a7a3e") : c("1d5a2c"), x, ground - 10f, x + sway, ground + h, 7f);
        }
        float grow = Interpolation.pow2Out.apply(progress(STEM, LEAVES));
        float top = MathUtils.lerp(ground, cloverY(), grow);
        if (grow > 0f) line(batch, c("2f9a46"), cx(), ground - 10f, cx() + 8f * MathUtils.sin(time * 2f), top, 26f);
        for (int i = 0; i < 4; i++) {
            float start = LEAVES + i * LEAF_STEP;
            float open = Interpolation.swingOut.apply(progress(start, start + 0.3f));
            if (open <= 0f) continue;
            float angle = 45f + i * 90f, distance = 120f * open;
            batch.setColor(Color.WHITE);
            sprite(batch, leaf, cx() + MathUtils.cosDeg(angle) * distance, top + MathUtils.sinDeg(angle) * distance,
                24f * open, angle - 90f);
        }
        float shine = progress(CLOVER_SHINE, CLOVER_SHINE + 0.3f);
        if (shine > 0f) glow(batch, cx(), top, 700f, c("9af0a8"), 0.5f * shine * pulse(6f, 0.7f));
        for (int i = 0; i < 12; i++) {                                                 // des lucioles
            float x = width * ((i * 0.137f + 0.05f * MathUtils.sin(time + i)) % 1f), y = ground + 60f + (i * 53 % 300)
                + 30f * MathUtils.sin(time * 1.5f + i * 2f);
            glow(batch, x, y, 40f, c("e8ff8a"), 0.7f * pulse(4f + i, 0.2f));
        }
    }

    private float cloverY() { return worldHeight() * 0.5f; }

    /** Coeur : un vitrail en forme de cœur dans le mur de la chapelle, qui s'éclaire. */
    private void drawStainedGlass(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        drawDungeon(batch);
        fill(batch, Color.BLACK, 0.45f);
        float light = Interpolation.pow2In.apply(progress(GLASS_LIGHT, GLASS_FULL));
        float scale = 46f, w = stainedGlass.getRegionWidth() * scale, h = stainedGlass.getRegionHeight() * scale;
        // Les rayons de lumière colorée qui tombent du vitrail.
        additive(batch);
        for (int i = 0; i < 5; i++) {
            batch.setColor(tint.r, tint.g * 0.6f, tint.b * 0.7f, 0.12f * light);
            batch.draw(trail, cx() + (i - 2) * 90f, glassY(), 0f, 70f, height * 0.75f, 140f, 1f, 1f,
                -90f - (i - 2) * 6f);
        }
        normal(batch);
        glow(batch, cx(), height * 0.18f, width * 0.6f, tint, 0.35f * light);
        glow(batch, cx(), glassY(), w * 1.6f, tint, 0.5f * light * pulse(5f, 0.8f));
        rect(batch, c("100c18"), cx() - w / 2f - 30f, glassY() - h / 2f - 30f, w + 60f, h + 60f);
        rect(batch, c("6a6078"), cx() - w / 2f, glassY() - h / 2f, w, h);                // la pierre derrière le plomb
        float shade = MathUtils.lerp(0.18f, 1f, light);
        batch.setColor(shade, shade, shade, 1f);
        sprite(batch, stainedGlass, cx(), glassY(), scale, 0f);
    }

    private float glassY() { return worldHeight() * 0.6f; }

    /** Carreau : une carte au trésor se déroule sur la table ; un chemin, puis une croix rouge. */
    private void drawMap(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        rect(batch, c("2a180e"), -MARGIN, -MARGIN, width + 2f * MARGIN, height + 2f * MARGIN);
        for (int i = 0; i < 9; i++) rect(batch, c("3a2416"), -MARGIN, i * 130f, width + 2f * MARGIN, 120f);
        glow(batch, width * 0.12f, height * 0.75f, 600f, c("ff9a2a"), 0.35f * pulse(13f, 0.8f));     // une bougie
        float left = width * 0.18f, mapW = width * 0.64f, mapH = height * 0.58f, bottom = height * 0.2f;
        float unroll = Interpolation.pow2Out.apply(progress(UNROLL, UNROLLED));
        float edge = left + 40f + (mapW - 40f) * unroll;
        rect(batch, c("b8945a"), left - 8f, bottom - 8f, edge - left + 16f, mapH + 16f);
        rect(batch, c("e8d4a0"), left, bottom, edge - left, mapH);
        // Les dessins de la carte, découverts à mesure qu'elle se déroule.
        java.util.Random marks = new java.util.Random(5);
        for (int i = 0; i < 16; i++) {
            float x = left + 40f + (mapW - 80f) * marks.nextFloat(), y = bottom + 40f + (mapH - 80f) * marks.nextFloat();
            if (x > edge - 30f) continue;
            if (i % 3 == 0) {                                                          // une montagne
                for (int k = 0; k < 4; k++) rect(batch, c("8a6a3a"), x - 36f + k * 9f, y + k * 14f, 72f - k * 18f, 14f);
            } else if (i % 3 == 1) {                                                   // un arbre
                rect(batch, c("4a6a2a"), x - 18f, y + 14f, 36f, 30f);
                rect(batch, c("5a3a1a"), x - 4f, y, 8f, 14f);
            } else {                                                                   // une vague
                rect(batch, c("4a7aa0"), x - 30f, y, 24f, 6f);
                rect(batch, c("4a7aa0"), x, y + 6f, 24f, 6f);
            }
        }
        // Le chemin en pointillés jusqu'à la croix.
        float path = progress(UNROLLED, PATH_END);
        for (int i = 0; i < 24; i++) {
            float k = i / 23f;
            if (k > path) break;
            float x = MathUtils.lerp(left + 90f, markX(), k), y = MathUtils.lerp(bottom + 70f, markY(), k)
                + 90f * MathUtils.sin(k * MathUtils.PI * 2f);
            rect(batch, c("7a2a1a"), x - 7f, y - 4f, 14f, 8f);
        }
        float mark = Interpolation.swingOut.apply(progress(X_MARK, X_MARK + 0.25f));
        if (mark > 0f) {
            float size = 70f * mark;
            glow(batch, markX(), markY(), 300f, c("ff4a3a"), 0.4f * mark * pulse(8f, 0.6f));
            line(batch, c("c41e1e"), markX() - size, markY() - size, markX() + size, markY() + size, 22f);
            line(batch, c("c41e1e"), markX() - size, markY() + size, markX() + size, markY() - size, 22f);
        }
        if (unroll < 1f) {                                                             // le rouleau qui reste à dérouler
            rect(batch, c("a07a44"), edge - 20f, bottom - 20f, 60f, mapH + 40f);
            rect(batch, c("d8bc84"), edge - 10f, bottom - 20f, 14f, mapH + 40f);
        }
    }

    private float markX() { return worldWidth() * 0.66f; }
    private float markY() { return worldHeight() * 0.58f; }
}
