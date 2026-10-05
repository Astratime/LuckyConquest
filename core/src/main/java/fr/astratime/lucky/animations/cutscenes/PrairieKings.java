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
 *   <li>Pique : des lames tombent du plafond et se plantent en cercle ; le roi sort du centre.</li>
 *   <li>Trèfle : une pluie de pièces s'empile ; le tas éclate, c'est le roi. Il attrape la dernière.</li>
 *   <li>Coeur : les murs battent ; un cœur géant s'ouvre et le roi en sort.</li>
 *   <li>Carreau : un tas de trésor bouge ; le roi se réveille dessus et lève son écu, éclat de diamant.</li>
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

    private static final int BLADE_COUNT = 8;

    private final Color tint, wall, wallDark, floor;
    private final TextureRegion blade, coin, heart, heartLeft, heartRight;
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
    }

    @Override
    protected void restart() {
        pile = 0f;
        bursting = false;
    }

    @Override
    protected void step(float delta) {
        switch (kind) {
            case ROI_PIQUE -> {
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
            }
            case ROI_COEUR -> {
                for (float beat : HEART_BEATS) if (at(beat)) rumble(0.12f, 3f);
                if (at(HEART_OPEN)) {
                    rumble(0.4f, 12f);
                    sparks(cx(), worldHeight() * 0.5f, 50, tint, 900f, 20f);
                }
            }
            default -> {
                if (time >= PILE_SHAKE && time < DIAMOND_RISE && random.nextFloat() < 10f * delta) {
                    debris(cx() + (random.nextFloat() - 0.5f) * 400f, kingY() + 120f, 2, coin, 0.6f, 500f);
                }
                if (at(DIAMOND_RISE)) rumble(0.4f, 10f);
                if (at(DIAMOND_FLASH)) sparks(diamondX(), diamondY(), 30, c("bfe8ff"), 600f, 14f);
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
            float x = bladeX(i), y = MathUtils.lerp(worldHeight() + 200f, bladeY(i), fall);
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
            } else if (time >= CATCH) {
                glow(batch, catchX(), catchY(), 160f, c("ffc93a"), 0.7f * pulse(8f, 0.5f));
                batch.setColor(Color.WHITE);
                sprite(batch, coin, catchX(), catchY(), 1.4f, 0f);
            }
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
        flash(batch, tint, HEART_OPEN, 0.25f, 0.5f);
    }

    // -------------------------------------------------------------------------
    // Carreau : le trésor qui bouge
    // -------------------------------------------------------------------------

    private void drawDiamond(Batch batch) {
        float shake = time < PILE_SHAKE ? 0f : time < DIAMOND_RISE ? 4f + 10f * progress(PILE_SHAKE, DIAMOND_RISE) : 0f;
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
}
