package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les rois du Temple du Destin entrent en scène (10 lieux, Astra 2026-11-20) :
 * <ul>
 *   <li>Le Maître des cloches : le parvis au crépuscule, un grand bourdon pend sous le portique. Il monte
 *   les marches, puis frappe le bourdon : les ondes de bronze balaient l'écran.</li>
 *   <li>La Tisseuse de destins : le jardin des oracles dans la nuit, des fils d'or tombent du ciel. Elle
 *   descend le long de ses fils, qui s'illuminent quand ses yeux-bobines s'ouvrent.</li>
 *   <li>Le Seigneur des Dés : la salle au damier, deux braseros. Un dé géant tombe, roule et s'arrête sur
 *   six ; le Seigneur en sort et lance deux dés d'or qui retombent sur six.</li>
 *   <li>L'Idole d'Or : le sanctuaire dans le noir. Les bougies s'allument une à une, l'idole sort de
 *   l'ombre, son troisième œil s'ouvre et des pièces jaillissent de l'autel.</li>
 * </ul>
 */
public class TempleKings extends KingEntrance {

    /** Maître des cloches : il monte (de 0 à {@link #CLIMB}), puis frappe le bourdon. */
    static final float CLIMB = 1.2f, STRIKE = 3.4f;
    /** Tisseuse : elle descend le long de ses fils (jusqu'à {@link #DESCENT}), ses yeux s'ouvrent. */
    static final float DESCENT = 1.6f, EYES_OPEN = 3.4f;
    /** Seigneur des Dés : le grand dé tombe, s'arrête, il en sort, il lance ses dés. */
    static final float DIE_LAND = 0.8f, STEP_OUT = 1.4f, THROW = 3.4f;
    /** Idole d'Or : les bougies s'allument, l'idole sort de l'ombre, son œil s'ouvre. */
    static final float CANDLES = 0.2f, IDOL_IN = 1.0f, THIRD_EYE = 3.4f;

    private static final Color DUSK = hex("2a1430"), SUNSET = hex("d86a3a"), STONE = hex("a89a88"), STONE_DARK = hex("6e6254");
    private static final Color RED = hex("c8323a"), INK = hex("1e141a"), BRONZE = hex("c87a3a"), AMBER = hex("ffd27a");
    private static final Color THREAD = hex("fff2a0"), NIGHT = hex("1a1030"), IVORY = hex("f6f0e2"), RUBY = hex("e8323a");
    private static final String[] BELL = {
        "....bb....", "..bbbbbb..", ".bbbbbbBB.", ".bbbbbbBB.", ".bbbbbbBB.", "bbbbbbbBBB", "gggggggggg", "....BB...."};
    private static final String[] DIE = {
        "ooooooooo", "oiiiiiiio", "oiriiirio", "oiiiiiiio", "oiriiirio", "oiiiiiiio", "oiriiirio", "oiiiiiiio", "ooooooooo"};

    private final TextureRegion bell, die, goldDie, coinRegion;

    public TempleKings(CutsceneKit kit, EnemyKind kind) {
        super(kit, kind);
        bell = art("bBg", new Color[] {BRONZE, hex("8a4a1e"), hex("c4891e")}, BELL);
        die = art("oir", new Color[] {hex("1a1018"), IVORY, RUBY}, DIE);
        goldDie = art("oir", new Color[] {hex("1a1018"), hex("ffd54a"), hex("7a1a22")}, DIE);
        coinRegion = kind == EnemyKind.IDOLE_OR ? load("hud/coin.png") : null;
    }

    @Override
    protected void stepPrelude(float delta) {
        if (kind == EnemyKind.SEIGNEUR_DES && at(1.4f)) rumble(0.2f, 4f);
        if (kind == EnemyKind.MAITRE_CLOCHES && at(1.0f)) rumble(0.15f, 3f);
    }

    @Override
    protected void prelude(Batch batch) {
        switch (kind) {
            case MAITRE_CLOCHES -> drawParvis(batch, 0f);
            case TISSEUSE -> drawGarden(batch, 0f);
            case SEIGNEUR_DES -> drawHall(batch);
            default -> drawSanctuary(batch, 0f);
        }
    }

    @Override
    protected void step(float delta) {
        switch (kind) {
            case MAITRE_CLOCHES -> {
                if (at(STRIKE)) {
                    rumble(0.6f, 12f);
                    sparks(bellX(), bellY(), 40, AMBER, 700f, 12f);
                }
            }
            case TISSEUSE -> {
                if (at(DESCENT)) sparks(cx(), kingY(), 20, THREAD, 400f, 8f);
                if (at(EYES_OPEN)) {
                    rumble(0.3f, 6f);
                    sparks(cx(), kingY() + 26f * KING_SCALE, 40, THREAD, 600f, 10f);
                }
            }
            case SEIGNEUR_DES -> {
                if (at(DIE_LAND)) {
                    rumble(0.5f, 12f);
                    sparks(cx(), kingY(), 40, hex("c8b8a0"), 600f, 14f);
                }
                if (at(STEP_OUT)) sparks(cx(), kingY() + 20f * KING_SCALE, 30, IVORY, 500f, 10f);
                if (at(THROW + 0.6f)) rumble(0.3f, 8f);
            }
            default -> {
                if (at(THIRD_EYE)) {
                    rumble(0.5f, 10f);
                    sparks(kingPixelX(17f, cx(), KING_SCALE), kingPixelY(11.5f, kingY(), KING_SCALE), 40, RUBY, 600f, 12f);
                    if (coinRegion != null) debris(cx(), kingY(), 30, coinRegion, 1.1f, 1300f);
                }
            }
        }
    }

    @Override
    protected void draw(Batch batch) {
        switch (kind) {
            case MAITRE_CLOCHES -> drawBellMaster(batch);
            case TISSEUSE -> drawWeaver(batch);
            case SEIGNEUR_DES -> drawDiceLord(batch);
            default -> drawIdol(batch);
        }
        drawParticles(batch, parts);
    }

    // -------------------------------------------------------------------------
    // Le Maître des cloches
    // -------------------------------------------------------------------------

    private float bellX() { return cx() + worldWidth() * 0.28f; }
    private float bellY() { return worldHeight() * 0.62f; }

    /** Le parvis au crépuscule : le ciel du soir, les marches, le grand portique rouge ; {@code swing} : le bourdon oscille. */
    private void drawParvis(Batch batch, float swing) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, SUNSET, DUSK);
        glow(batch, cx(), height * 0.45f, 500f, AMBER, 0.6f);
        for (int k = 0; k < 10; k++) {                                     // les marches
            float y = height * 0.02f + k * height * 0.03f, half = width * (0.5f - k * 0.03f);
            rect(batch, k % 2 == 0 ? STONE : STONE_DARK, cx() - half, y, half * 2f, height * 0.03f + 1f);
        }
        rect(batch, STONE_DARK, -MARGIN, -MARGIN, width + 2f * MARGIN, height * 0.02f + MARGIN);
        float left = cx() - width * 0.36f, right = cx() + width * 0.36f;  // le portique
        rect(batch, RED, left - 30f, height * 0.3f, 60f, height * 0.5f);
        rect(batch, RED, right - 30f, height * 0.3f, 60f, height * 0.5f);
        rect(batch, INK, left - 120f, height * 0.8f, right - left + 240f, 40f);
        rect(batch, INK, left - 60f, height * 0.72f, right - left + 120f, 24f);
        // Le bourdon, sous le linteau, à droite.
        line(batch, hex("5a3a20"), bellX(), height * 0.8f, bellX() + swing * 30f, bellY() + 60f, 8f);
        batch.setColor(Color.WHITE);
        sprite(batch, bell, bellX() + swing * 40f, bellY(), 18f, swing * 12f);
    }

    private void drawBellMaster(Batch batch) {
        float strike = gesture(STRIKE - 0.2f, STRIKE + 0.3f);
        float swing = time > STRIKE ? MathUtils.sin((time - STRIKE) * 9f) * Math.max(0f, 1f - (time - STRIKE) / 2f) : 0f;
        drawParvis(batch, swing);
        float climb = Interpolation.pow2Out.apply(progress(0f, CLIMB));
        pose(18f * strike, -worldHeight() * 0.4f * (1f - climb), -10f * strike, 0.05f * strike);
        drawKing(batch, cx() - worldWidth() * 0.05f, kingY(), KING_SCALE, 0f, 1f);
        if (time >= STRIKE) {                                              // les ondes de bronze
            additive(batch);
            for (int k = 0; k < 3; k++) {
                float t = (time - STRIKE - k * 0.25f) / 1.2f;
                if (t <= 0f || t >= 1f) continue;
                glow(batch, bellX(), bellY(), 200f + 1400f * t, AMBER, 0.5f * (1f - t));
            }
            normal(batch);
        }
        flash(batch, AMBER, STRIKE, 0.15f, 0.35f);
    }

    // -------------------------------------------------------------------------
    // La Tisseuse de destins
    // -------------------------------------------------------------------------

    /** Le jardin des oracles la nuit : des arbres sombres, des fils d'or qui tombent du ciel ({@code lit} : allumés). */
    private void drawGarden(Batch batch, float lit) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, hex("3a2a4a"), NIGHT);
        for (int i = 0; i < 30; i++) {
            batch.setColor(1f, 0.95f, 0.8f, 0.4f + 0.4f * MathUtils.sin(time * 2f + i));
            batch.draw(pixel, (i * 211.7f) % width, height * 0.5f + (i * 67.3f) % (height * 0.5f), 4f, 4f);
        }
        rect(batch, hex("1e2a1a"), -MARGIN, -MARGIN, width + 2f * MARGIN, height * 0.18f + MARGIN);
        for (int k = 0; k < 4; k++) {                                      // les arbres, en silhouette
            float x = width * (0.08f + 0.28f * k);
            line(batch, hex("2a1a14"), x, height * 0.18f, x + 40f * (k % 2 == 0 ? 1f : -1f), height * 0.7f, 30f);
            glow(batch, x, height * 0.72f, 360f, hex("8a2a3a"), 0.7f);
        }
        for (int k = 0; k < 7; k++) {                                      // les fils d'or, du ciel jusqu'au sol
            float x = width * (0.1f + 0.13f * k);
            line(batch, THREAD, x, height + MARGIN, x + MathUtils.sin(time + k) * 20f, height * 0.18f, 3f + 3f * lit);
            if (lit > 0f) glow(batch, x, height * 0.6f, 120f, THREAD, 0.4f * lit);
        }
    }

    private void drawWeaver(Batch batch) {
        float lit = progress(EYES_OPEN, EYES_OPEN + 0.4f);
        drawGarden(batch, lit);
        float down = Interpolation.pow2Out.apply(progress(0f, DESCENT));
        float rise = gesture(EYES_OPEN - 0.2f, EYES_OPEN + 1.0f);
        pose(0f, worldHeight() * 0.7f * (1f - down) + 20f * rise, MathUtils.sin(time * 2f) * 3f * (1f - down), 0.04f * rise);
        float top = kingPixelY(0f, kingY(), KING_SCALE);                   // ses propres fils, qui la retiennent
        line(batch, THREAD, cx() - 60f, worldHeight() + MARGIN, cx() - 60f, top - 20f, 4f);
        line(batch, THREAD, cx() + 60f, worldHeight() + MARGIN, cx() + 60f, top - 20f, 4f);
        drawKing(batch, cx(), kingY(), KING_SCALE, 0f, 1f);
        float eyes = lit * pulse(6f, 0.6f);
        if (eyes > 0f) {
            for (float px : new float[] {13.5f, 20.5f}) {
                glow(batch, kingPixelX(px, cx(), KING_SCALE), kingPixelY(15.5f, kingY(), KING_SCALE), 120f, THREAD, eyes);
            }
        }
        flash(batch, THREAD, EYES_OPEN, 0.2f, 0.3f);
    }

    // -------------------------------------------------------------------------
    // Le Seigneur des Dés
    // -------------------------------------------------------------------------

    /** La salle des dés : colonnes rouges, damier en perspective, deux braseros qui crépitent. */
    private void drawHall(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, hex("5a2428"), hex("2a1014"));
        for (int k = 0; k < 4; k++) rect(batch, RED, width * (0.05f + 0.3f * k), height * 0.4f, 50f, height * 0.6f);
        float horizon = height * 0.42f;
        for (int row = 0; row < 8; row++) {                                // le damier
            float y0 = MathUtils.lerp(-MARGIN, horizon, row / 8f), y1 = MathUtils.lerp(-MARGIN, horizon, (row + 1) / 8f);
            float cell = MathUtils.lerp(220f, 60f, row / 8f);
            for (int i = -10; i <= 10; i++) {
                rect(batch, (i + row) % 2 == 0 ? IVORY : INK, cx() + i * cell - cell / 2f, y0, cell, y1 - y0 + 1f);
            }
        }
        for (float x : new float[] {width * 0.2f, width * 0.8f}) {          // les braseros
            rect(batch, hex("503c28"), x - 30f, horizon, 60f, 90f);
            glow(batch, x, horizon + 130f, 220f * (0.85f + 0.15f * MathUtils.sin(time * 11f + x)), hex("ff8a2a"), 0.8f);
        }
    }

    private void drawDiceLord(Batch batch) {
        drawHall(batch);
        float out = Interpolation.swingOut.apply(progress(STEP_OUT - 0.2f, STEP_OUT + 0.4f));
        float throwing = gesture(THROW - 0.2f, THROW + 0.4f);
        pose(0f, -worldHeight() * 0.3f * (1f - out), 8f * throwing, 0.08f * throwing);
        drawKing(batch, cx(), kingY(), KING_SCALE, 0f, out);
        // Le grand dé : il tombe, s'arrête sur six, puis s'efface quand il en sort.
        float fall = Interpolation.bounceOut.apply(progress(0f, DIE_LAND));
        float dieAlpha = 1f - progress(STEP_OUT - 0.1f, STEP_OUT + 0.3f);
        if (dieAlpha > 0f) {
            batch.setColor(1f, 1f, 1f, dieAlpha);
            sprite(batch, die, cx(), MathUtils.lerp(worldHeight() + 300f, kingY() + 200f, fall), 50f, (1f - fall) * 540f);
        }
        // Ses deux dés d'or, lancés en l'air, retombent sur six.
        float flight = progress(THROW, THROW + 0.8f);
        if (flight > 0f) {
            for (int k = 0; k < 2; k++) {
                float x = cx() + (k == 0 ? -1f : 1f) * worldWidth() * 0.22f * flight;
                float y = kingY() + 40f * KING_SCALE * (1f - flight) + 600f * MathUtils.sin(flight * MathUtils.PI);
                batch.setColor(Color.WHITE);
                sprite(batch, goldDie, x, Math.max(kingY() + 60f, y), 16f, (1f - flight) * 720f * (k == 0 ? 1f : -1f));
            }
        }
        flash(batch, IVORY, DIE_LAND, 0.1f, 0.3f);
    }

    // -------------------------------------------------------------------------
    // L'Idole d'Or
    // -------------------------------------------------------------------------

    /** Le sanctuaire dans le noir ; {@code lit} (0 à 1) : la part des bougies allumées. */
    private void drawSanctuary(Batch batch, float lit) {
        float width = worldWidth(), height = worldHeight();
        fill(batch, hex("1a120a"), 1f);
        for (int k = 0; k < 9; k++) rect(batch, hex("3a2a10"), width * (0.02f + 0.11f * k), height * 0.3f, width * 0.08f, height * 0.6f);
        rect(batch, hex("3a1410"), -MARGIN, -MARGIN, width + 2f * MARGIN, height * 0.22f + MARGIN);
        for (int k = 0; k < 16; k++) {                                     // les bougies, allumées une à une
            float x = width * (0.04f + 0.06f * k + (k >= 8 ? 0.04f : 0f)), y = height * (0.16f + 0.04f * (k % 3));
            rect(batch, IVORY, x - 5f, y, 10f, 40f);
            float on = progress(k / 16f * lit * 0.9f, k / 16f * lit * 0.9f + 0.05f) * (lit > k / 16f ? 1f : 0f);
            if (on > 0f) glow(batch, x, y + 50f, 90f * (0.85f + 0.15f * MathUtils.sin(time * 13f + k)), AMBER, 0.8f * on);
        }
    }

    private void drawIdol(Batch batch) {
        float candles = progress(CANDLES, CANDLES + 1.2f);
        drawSanctuary(batch, Math.max(0.01f, candles));
        float appear = progress(IDOL_IN, IDOL_IN + 1.2f);
        float awake = gesture(THIRD_EYE - 0.2f, THIRD_EYE + 1.0f);
        pose(0f, 24f * awake, 0f, 0.04f * awake);
        glow(batch, cx(), kingY() + 20f * KING_SCALE, 900f, hex("ffc93a"), 0.35f * appear);
        drawKing(batch, cx(), kingY(), KING_SCALE, 1f - appear, appear);
        float eye = progress(THIRD_EYE, THIRD_EYE + 0.2f) * pulse(7f, 0.6f);
        if (eye > 0f) glow(batch, kingPixelX(17f, cx(), KING_SCALE), kingPixelY(11.5f, kingY(), KING_SCALE), 160f, RUBY, eye);
        flash(batch, hex("ffc93a"), THIRD_EYE, 0.2f, 0.35f);
    }
}
