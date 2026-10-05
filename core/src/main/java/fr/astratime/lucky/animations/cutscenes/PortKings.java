package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.enemy.EnemyKind;

/**
 * Les rois du Port des Contrebandiers entrent en scène :
 * <ul>
 *   <li>Capitaine Rat : des yeux rouges dans le noir, des dizaines ; ils grimpent les uns sur les autres.</li>
 *   <li>Tavernier : les chopes glissent sur le comptoir, l'écran tangue ; il pose la sienne. Bam.</li>
 *   <li>Gardien du Phare : le faisceau balaie la mer, s'arrête sur toi ; écran blanc, il est là.</li>
 *   <li>Capitaine Noir : un galion sort du brouillard ; coup de canon ; il saute sur le pont.</li>
 * </ul>
 */
public class PortKings extends KingEntrance {

    /** Capitaine Rat : les yeux s'ouvrent, grimpent, puis le capitaine apparaît. */
    static final float EYES_END = 1.5f, CLIMB = 1.6f, RAT_REVEAL = 2.2f;
    /** Tavernier : les chopes glissent, il apparaît, il pose sa chope. */
    static final float MUGS = 0.15f, MUG_STEP = 0.4f, BARMAN = 1.9f, BAM = 2.55f;
    /** Gardien du Phare : le faisceau se tourne vers toi, l'écran blanchit, il est là. */
    static final float BEAM_TURN = 1.8f, BEAM_WHITE = 2.2f;
    /** Capitaine Noir : le galion approche, le canon tonne, le capitaine saute. */
    static final float SHIP_END = 1.8f, CANNON = 1.9f, JUMP = 2.3f, LAND = 2.65f;

    private static final Color NIGHT_TOP = c("070b1e"), NIGHT_LOW = c("1a2a50"), SEA = c("0c1630"), SEA_LIGHT = c("2a4a80");
    private static final Color RED = c("ff2a2a"), GOLD = c("ffc93a"), BEAM = c("fff2a0");

    private final TextureRegion mug, ship;
    private final Matrix4 saved = new Matrix4(), tilted = new Matrix4();
    private final Array<Particle> eyes = new Array<>(false, 64);
    private final Array<Particle> fog = new Array<>(false, 32);

    public PortKings(CutsceneKit kit, EnemyKind kind) {
        super(kit, kind);
        Color[] mugColors = {c("1a0a0e"), c("fff8e8"), c("ffb01e"), c("d88a10"), c("c8c8d0")};
        mug = art("owbdg", mugColors,
            ".wwwwww..",
            "owwwwwwo.",
            "obbbbbbooo",
            "obdbbbbo.o",
            "obdbbbbo.o",
            "obdbbbbo.o",
            "obdbbbbooo",
            "obbbbbbo..",
            "oggggggo..",
            ".oooooo...");
        Color[] shipColors = {c("0a0608"), c("f0f0f0"), c("3a2618"), c("d8d0c0"), c("1c120c"), c("ffd060")};
        ship = art("kwpshy", shipColors,
            "...............kk.............",
            "...............kwk............",
            "...............kkk............",
            "...............p..............",
            "......p........p.......p......",
            ".....sss......sss.....sss.....",
            "....sssss....sssss...sssss....",
            "....sssss....sssss...sssss....",
            "...ssssss...sssssss..ssssss...",
            "...ssssss...sssssss..ssssss...",
            "...ssssss...sssssss..ssssss...",
            "....ssss.....sssss....ssss....",
            "......p........p.......p......",
            "hhhhhhhhhhhhhhhhhhhhhhhhhhhhhh",
            ".hhhyhhhhyhhhhyhhhhyhhhhyhhhh.",
            "..hhhhhhhhhhhhhhhhhhhhhhhhhh..",
            "...hhhhhhhhhhhhhhhhhhhhhhhh...",
            "....hhhhhhhhhhhhhhhhhhhhhh....");
    }

    @Override
    protected void restart() {
        eyes.clear();
        fog.clear();
        if (kind == EnemyKind.CAPITAINE_RAT) {
            for (int i = 0; i < 34; i++) {                    // des dizaines d'yeux rouges
                Particle eye = particle(worldWidth() * (0.06f + 0.88f * random.nextFloat()),
                    worldHeight() * (0.1f + 0.75f * random.nextFloat()), 0f, 0f, 99f, 5f + 4f * random.nextFloat());
                eye.delay = 0.15f + (EYES_END - 0.15f) * random.nextFloat();      // quand il s'ouvre
                eye.targetX = cx() + (random.nextFloat() - 0.5f) * 260f;          // sa place dans la pile
                eye.targetY = kingY() + 40f + i * 14f;
                eye.spin = random.nextFloat() * 10f;
                eyes.add(eye);
            }
        }
        if (kind == EnemyKind.CAPITAINE_NOIR) {
            for (int i = 0; i < 16; i++) {
                Particle cloud = particle(worldWidth() * random.nextFloat(), worldHeight() * (0.25f + 0.4f
                    * random.nextFloat()), random.nextBoolean() ? 0f : 180f, 30f + 40f * random.nextFloat(), 99f,
                    500f + 500f * random.nextFloat());
                fog.add(cloud);
            }
        }
    }

    @Override
    protected void step(float delta) {
        switch (kind) {
            case CAPITAINE_RAT -> {
                if (at(RAT_REVEAL)) {
                    rumble(0.3f, 9f);
                    sparks(cx(), kingY() + 250f, 40, RED, 800f, 16f);
                }
            }
            case TAVERNIER -> {
                if (at(BAM)) {
                    rumble(0.35f, 14f);
                    sparks(bamX(), counterTop() + 120f, 40, Color.WHITE, 600f, 16f);
                }
            }
            case GARDIEN_PHARE -> {
                if (at(BEAM_WHITE)) rumble(0.25f, 6f);
            }
            default -> {
                for (Particle cloud : fog) cloud.x += cloud.vx * delta;
                if (at(CANNON)) {
                    rumble(0.4f, 12f);
                    for (int i = 0; i < 16; i++) {
                        Particle smoke = particle(cannonX(), cannonY(), 160f + 40f * random.nextFloat(),
                            120f + 200f * random.nextFloat(), 1.2f, 120f + 100f * random.nextFloat());
                        smoke.drag = 0.3f;
                        smoke.color.set(0.75f, 0.75f, 0.8f, 0.5f);
                        parts.add(smoke);
                    }
                    sparks(cannonX(), cannonY(), 24, c("ff8a1e"), 500f, 18f);
                }
                if (at(LAND)) {
                    rumble(0.35f, 12f);
                    sparks(cx(), kingY(), 30, c("c8d8ff"), 600f, 14f);
                }
            }
        }
    }

    @Override
    protected void draw(Batch batch) {
        switch (kind) {
            case CAPITAINE_RAT -> drawRat(batch);
            case TAVERNIER -> drawTavern(batch);
            case GARDIEN_PHARE -> drawLighthouse(batch);
            default -> drawGalleon(batch);
        }
        drawParticles(batch, parts);
    }

    // -------------------------------------------------------------------------
    // Le décor de mer, partagé par le phare et le galion
    // -------------------------------------------------------------------------

    private void drawSea(Batch batch, float horizon) {
        float width = worldWidth();
        gradient(batch, NIGHT_LOW, NIGHT_TOP);
        rect(batch, SEA, -MARGIN, -MARGIN, width + 2f * MARGIN, horizon + MARGIN);
        for (int row = 0; row < 9; row++) {                         // vagues qui ondulent
            float y = horizon - 20f - row * horizon / 9f;
            for (int k = 0; k < 14; k++) {
                float x = ((k * 160f + row * 70f + time * (30f + row * 8f)) % (width + 200f)) - 100f;
                batch.setColor(SEA_LIGHT.r, SEA_LIGHT.g, SEA_LIGHT.b, 0.5f);
                batch.draw(pixel, x, y + 4f * MathUtils.sin(time * 2f + k + row), 50f + row * 6f, 4f);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Capitaine Rat : les yeux dans le noir
    // -------------------------------------------------------------------------

    private void drawRat(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        rect(batch, c("0a0606"), -MARGIN, -MARGIN, width + 2f * MARGIN, height + 2f * MARGIN);
        for (int i = 0; i < 12; i++) {                              // planches de la cale, à peine visibles
            rect(batch, c("140c0a"), -MARGIN, i * 90f, width + 2f * MARGIN, 84f);
        }
        float climb = progress(CLIMB, RAT_REVEAL);
        float reveal = progress(RAT_REVEAL, RAT_REVEAL + 0.4f);
        if (time >= RAT_REVEAL) {
            glow(batch, cx(), kingY() + 250f, 1000f, RED, 0.35f * reveal);
            drawKing(batch, cx(), kingY(), KING_SCALE, 1f - reveal, 1f);
        }
        for (Particle eye : eyes) {
            if (time < eye.delay) continue;
            float open = progress(eye.delay, eye.delay + 0.1f);
            float blink = MathUtils.sin(time * 3f + eye.spin) > 0.96f ? 0.1f : 1f;
            float e = Interpolation.pow2In.apply(MathUtils.clamp(climb * 1.4f - eye.spin * 0.04f, 0f, 1f));
            float x = MathUtils.lerp(eye.x, eye.targetX, e), y = MathUtils.lerp(eye.y, eye.targetY, e);
            float fade = 1f - reveal;
            if (fade <= 0f) continue;
            float size = eye.size, h = size * open * blink;
            glow(batch, x, y, size * 14f, RED, 0.35f * fade);
            batch.setColor(RED.r, RED.g, RED.b, fade);
            batch.draw(pixel, x - size * 2.2f, y - h / 2f, size, h);
            batch.draw(pixel, x + size * 1.2f, y - h / 2f, size, h);
        }
        flash(batch, RED, RAT_REVEAL, 0.25f, 0.45f);
    }

    // -------------------------------------------------------------------------
    // Tavernier : les chopes et le roulis
    // -------------------------------------------------------------------------

    private void drawTavern(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        // L'écran tangue de plus en plus, puis se fige au « bam ».
        float sway = time < BAM ? 3.5f * progress(0f, 1.2f) * MathUtils.sin(time * 2.6f) : 0f;
        saved.set(batch.getTransformMatrix());
        tilted.set(saved).translate(cx(), height / 2f, 0f).rotate(0f, 0f, 1f, sway).translate(-cx(), -height / 2f, 0f);
        batch.setTransformMatrix(tilted);

        rect(batch, c("3a2014"), -MARGIN * 4f, -MARGIN * 4f, width + 8f * MARGIN, height + 8f * MARGIN);
        for (int i = 0; i < 20; i++) rect(batch, c("2e180e"), -MARGIN * 4f + i * 110f, -MARGIN, 8f, height + 4f * MARGIN);
        for (int shelf = 0; shelf < 2; shelf++) {                   // étagères à bouteilles
            float y = height * (0.62f + shelf * 0.17f);
            rect(batch, c("1c0e08"), -MARGIN * 4f, y - 10f, width + 8f * MARGIN, 14f);
            for (int b = 0; b < 26; b++) {
                Color glass = b % 3 == 0 ? c("2f8f4a") : b % 3 == 1 ? c("8a3a1a") : c("c8a040");
                float bx = b * 72f + shelf * 30f;
                rect(batch, glass, bx, y + 4f, 26f, 62f);
                rect(batch, glass, bx + 8f, y + 66f, 10f, 22f);
            }
        }
        glow(batch, cx(), height * 0.75f, width * 0.9f, c("ffb040"), 0.25f * pulse(9f, 0.85f));

        float appear = progress(BARMAN, BARMAN + 0.4f);
        if (time >= BARMAN) {
            float y = MathUtils.lerp(counterTop() - 260f, counterTop() - 30f, Interpolation.pow2Out.apply(appear));
            drawKing(batch, cx(), y, KING_SCALE, 1f - appear, appear);
        }
        // Le comptoir.
        rect(batch, c("1c0e08"), -MARGIN * 4f, -MARGIN * 4f, width + 8f * MARGIN, counterTop() + MARGIN * 4f);
        rect(batch, c("6b3a1e"), -MARGIN * 4f, counterTop() - 30f, width + 8f * MARGIN, 30f);
        rect(batch, c("8a5236"), -MARGIN * 4f, counterTop() - 8f, width + 8f * MARGIN, 8f);
        // Les chopes qui glissent.
        for (int i = 0; i < 4; i++) {
            float start = MUGS + i * MUG_STEP;
            if (time < start || time > start + 1.1f) continue;
            float k = (time - start) / 1.1f;
            float x = MathUtils.lerp(i % 2 == 0 ? -200f : width + 200f, i % 2 == 0 ? width + 200f : -200f, k);
            batch.setColor(Color.WHITE);
            batch.draw(mug, x - 90f, counterTop() - 8f, 180f, 200f);
        }
        // Sa chope, posée d'un coup.
        if (time >= BAM - 0.15f) {
            float drop = Interpolation.pow2In.apply(progress(BAM - 0.15f, BAM));
            float y = MathUtils.lerp(counterTop() + 300f, counterTop() - 4f, drop);
            batch.setColor(Color.WHITE);
            batch.draw(mug, bamX() - 130f, y, 260f, 290f);
        }
        batch.setTransformMatrix(saved);
        flash(batch, Color.WHITE, BAM, 0.12f, 0.35f);
    }

    private float counterTop() { return worldHeight() * 0.3f; }
    private float bamX()       { return cx() + worldWidth() * 0.18f; }

    // -------------------------------------------------------------------------
    // Gardien du Phare : le faisceau
    // -------------------------------------------------------------------------

    private void drawLighthouse(Batch batch) {
        float width = worldWidth(), height = worldHeight(), horizon = height * 0.4f;
        drawSea(batch, horizon);
        float towerX = width * 0.82f, lampY = height * 0.72f;
        // La tour rayée rouge et blanc.
        for (int i = 0; i < 7; i++) {
            float y = horizon - 80f + i * (lampY - horizon) / 7f;
            float w = MathUtils.lerp(150f, 90f, i / 7f);
            rect(batch, i % 2 == 0 ? c("c41e30") : c("e8e0d0"), towerX - w / 2f, y, w, (lampY - horizon) / 7f + 2f);
        }
        rect(batch, c("1a1418"), towerX - 70f, lampY - 6f, 140f, 16f);
        rect(batch, c("1a1418"), towerX - 50f, lampY + 70f, 100f, 30f);
        rect(batch, BEAM, towerX - 40f, lampY + 10f, 80f, 60f);
        // Le faisceau : il balaie la mer, puis se tourne vers toi.
        float turn = progress(BEAM_TURN, BEAM_WHITE);
        float angle = 180f + 28f * MathUtils.sin(time * 2.4f);
        float lampMidY = lampY + 40f;
        additive(batch);
        float length = width * 1.3f, thick = MathUtils.lerp(160f, 900f, turn * turn);
        batch.setColor(BEAM.r, BEAM.g, BEAM.b, 0.45f * (1f - turn));
        batch.draw(trail, towerX - length, lampMidY - thick / 2f, length, thick / 2f, length, thick, 1f, 1f,
            angle - 180f);
        normal(batch);
        glow(batch, towerX, lampMidY, 300f + 2600f * turn * turn, BEAM, 0.9f);
        // Écran blanc, puis le gardien, éclairé par sa lanterne.
        if (time >= BEAM_WHITE) {
            float out = progress(BEAM_WHITE + 0.1f, BEAM_WHITE + 0.7f);
            glow(batch, cx(), kingY() + 300f, 1200f, BEAM, 0.35f);
            drawKing(batch, cx(), kingY(), KING_SCALE, 0f, 1f);
            fill(batch, Color.WHITE, 1f - out);
        } else if (turn > 0f) {
            fill(batch, Color.WHITE, turn * turn);
        }
    }

    // -------------------------------------------------------------------------
    // Capitaine Noir : le galion dans le brouillard
    // -------------------------------------------------------------------------

    private void drawGalleon(Batch batch) {
        float width = worldWidth(), height = worldHeight(), horizon = height * 0.42f;
        drawSea(batch, horizon);
        float approach = Interpolation.pow2Out.apply(progress(0f, SHIP_END));
        float scale = MathUtils.lerp(6f, 36f, approach);
        float shipW = ship.getRegionWidth() * scale, shipH = ship.getRegionHeight() * scale;
        float bob = 8f * MathUtils.sin(time * 2f);
        float shipY = horizon - shipH * 0.25f - approach * height * 0.2f + bob;
        batch.setColor(0.85f, 0.85f, 0.9f, 1f);
        batch.draw(ship, cx() - shipW / 2f, shipY, shipW, shipH);
        // Le brouillard, qui se lève à mesure que le galion approche.
        for (Particle cloud : fog) {
            float x = ((cloud.x % (width + 800f)) + width + 800f) % (width + 800f) - 400f;
            batch.setColor(0.8f, 0.82f, 0.9f, 0.32f * (1f - 0.7f * approach));
            batch.draw(soft, x - cloud.size / 2f, cloud.y - cloud.size * 0.25f, cloud.size, cloud.size * 0.5f);
        }
        if (time >= CANNON) glow(batch, cannonX(), cannonY(), 500f, c("ff8a1e"), Math.max(0f, 1f - (time - CANNON) * 3f));
        // Le capitaine saute sur le pont.
        if (time >= JUMP) {
            float k = progress(JUMP, LAND);
            float y = MathUtils.lerp(height + 100f, kingY(), Interpolation.pow2In.apply(k));
            glow(batch, cx(), kingY() + 250f, 900f, c("8aa0ff"), 0.25f * k);
            drawKing(batch, cx(), y, KING_SCALE, 0f, 1f);
        }
        flash(batch, c("ffd8a0"), CANNON, 0.18f, 0.5f);
    }

    private float cannonX() { return cx() - worldWidth() * 0.18f; }
    private float cannonY() { return worldHeight() * 0.36f; }
}
