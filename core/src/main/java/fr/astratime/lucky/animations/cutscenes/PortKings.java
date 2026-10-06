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
 *   <li>Capitaine Rat : un fromage au milieu du quai disparaît. Des yeux rouges dans le noir, des dizaines ;
 *   ils grimpent les uns sur les autres. Il ajuste son chapeau, les rats saluent.</li>
 *   <li>Tavernier : des marins chantent et trinquent. Les chopes glissent sur le comptoir, l'écran tangue ;
 *   il pose la sienne. Bam. Il essuie le comptoir et te sert une chope.</li>
 *   <li>Gardien du Phare : l'orage, les vagues frappent les rochers. Le faisceau balaie la mer, s'arrête
 *   sur toi ; écran blanc, il est là. Il tourne sa lanterne vers toi, elle t'éblouit.</li>
 *   <li>Capitaine Noir : la cloche du port sonne dans le brouillard. Un galion en sort ; coup de canon ;
 *   il saute sur le pont. Il plante son sabre, le pavillon claque.</li>
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

    /** Le décor. Rat : le fromage disparaît. Tavernier : les marins trinquent. */
    static final float CHEESE_GONE = 1.35f;
    static final float[] CLINKS = {0.6f, 1.35f};
    /** Gardien : les vagues sur les rochers, l'éclair. Capitaine Noir : la cloche. */
    static final float[] CRASHES = {0.3f, 0.95f, 1.6f};
    static final float STORM_BOLT = 1.25f;
    static final float[] RINGS = {0.35f, 1.15f};
    /** Le geste. Rat : le chapeau, les rats sortent, ils saluent. Tavernier : le chiffon, la chope servie. */
    static final float HAT_END = 4.0f, RATS_UP = 4.0f, SALUTE = 4.4f, WIPE_END = 4.4f, SERVE = 4.5f, SERVED = 5.0f;
    /** Gardien : la lanterne éblouit. Capitaine Noir : le sabre se plante. */
    static final float DAZZLE = 4.15f, SABRE = 3.8f;

    /** Où sortent les six rats qui saluent (en largeur d'écran), de part et d'autre du capitaine. */
    private static final float[] RAT_SPOTS = {0.07f, 0.18f, 0.29f, 0.71f, 0.82f, 0.93f};

    private static final Color NIGHT_TOP = c("070b1e"), NIGHT_LOW = c("1a2a50"), SEA = c("0c1630"), SEA_LIGHT = c("2a4a80");
    private static final Color RED = c("ff2a2a"), GOLD = c("ffc93a"), BEAM = c("fff2a0");

    private final TextureRegion mug, ship, cheese, rat, sailor, bell, note;
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
        cheese = art("oyh", new Color[] {c("8a5a10"), c("ffd040"), c("c89a20")},
            "..........oo",
            "........ooyo",
            "......ooyyyo",
            "....ooyyyyyo",
            "..ooyyhyyyyo",
            "ooyyyyyyyhyo",
            "oyyhyyyyyyyo",
            "oyyyyyhyyyyo",
            "oyyyyyyyyyyo",
            "oooooooooooo");
        rat = art("ogrp", new Color[] {c("0a0808"), c("4a4048"), RED, c("c87a8a")},
            "..oo....oo..",
            ".opgo..ogpo.",
            ".ogggoogggo.",
            "..oggggggo..",
            "..orgggrgo..",
            "..oggggggo..",
            "...ogppgo...",
            "..oggggggo..",
            ".oggggggggo.",
            ".oggggggggo.",
            "..oo....oo..");
        sailor = art("oBsewb", new Color[] {c("0a0808"), c("1a2a5a"), c("e8b080"), c("2a1a10"), c("f0f0f0"),
                c("2a4aa0")},
            "..oooooo..",
            ".oBBBBBBo.",
            "oBBBBBBBBo",
            ".osssssso.",
            ".osesseso.",
            ".osssssso.",
            "..osssso..",
            "...owwo...",
            "..owbwbo..",
            ".owbwbwbo.",
            "owbwbwbwbo",
            "owbwbwbwbo",
            "owbwbwbwbo");
        bell = art("ogw", new Color[] {c("4a3008"), GOLD, c("fff0a0")},
            "....o....",
            "...ogo...",
            "..ogggo..",
            "..oggwo..",
            ".ogggwgo.",
            ".ogggggo.",
            "ogggggggo",
            "ooooooooo",
            "....o....");
        note = art("w", new Color[] {Color.WHITE},
            "..www",
            "..w.w",
            "..w.w",
            "..w..",
            "www..",
            "www..");
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
        if (kind == EnemyKind.CAPITAINE_NOIR || kind == EnemyKind.GARDIEN_PHARE) {
            for (int i = 0; i < 16; i++) {
                Particle cloud = particle(worldWidth() * random.nextFloat(), worldHeight() * (0.25f + 0.4f
                    * random.nextFloat()), random.nextBoolean() ? 0f : 180f, 30f + 40f * random.nextFloat(), 99f,
                    500f + 500f * random.nextFloat());
                fog.add(cloud);
            }
        }
    }

    @Override
    protected void stepPrelude(float delta) {
        switch (kind) {
            case CAPITAINE_RAT -> {
                if (at(CHEESE_GONE)) {
                    for (int i = 0; i < 14; i++) {
                        Particle puff = particle(cx(), quayY() + 60f, 360f * random.nextFloat(),
                            100f + 200f * random.nextFloat(), 0.8f, 60f + 60f * random.nextFloat());
                        puff.drag = 0.2f;
                        puff.color.set(0.7f, 0.65f, 0.55f, 0.5f);
                        parts.add(puff);
                    }
                    sparks(cx(), quayY() + 60f, 12, c("ffd040"), 300f, 8f);
                }
            }
            case TAVERNIER -> {
                for (int i = 0; i < CLINKS.length; i++) {
                    if (at(CLINKS[i])) {
                        sparks(cx(), clinkY(), 24, c("fff8e8"), 400f, 12f);
                        rumble(0.1f, 3f);
                    }
                }
            }
            case GARDIEN_PHARE -> {
                for (Particle cloud : fog) cloud.x += cloud.vx * 3f * delta;
                for (int r = 0; r < CRASHES.length; r++) {
                    if (!at(CRASHES[r])) continue;
                    float x = worldWidth() * (0.25f + 0.25f * r);
                    rumble(0.2f, 6f);
                    for (int i = 0; i < 30; i++) {                     // les gerbes d'écume
                        Particle spray = particle(x, worldHeight() * 0.26f, 60f + 60f * random.nextFloat(),
                            500f + 700f * random.nextFloat(), 1.2f, 30f + 50f * random.nextFloat());
                        spray.gravity = 1300f;
                        spray.color.set(0.85f, 0.95f, 1f, 0.8f);
                        parts.add(spray);
                    }
                }
                if (at(STORM_BOLT)) rumble(0.3f, 8f);
            }
            default -> {
                for (Particle cloud : fog) cloud.x += cloud.vx * delta;
            }
        }
    }

    @Override
    protected void prelude(Batch batch) {
        switch (kind) {
            case CAPITAINE_RAT -> drawQuay(batch);
            case TAVERNIER -> drawSailors(batch);
            case GARDIEN_PHARE -> drawStorm(batch);
            default -> drawBell(batch);
        }
    }

    @Override
    protected void step(float delta) {
        switch (kind) {
            case CAPITAINE_RAT -> {
                if (at(SALUTE)) sparks(cx(), kingY() + 44f * KING_SCALE, 16, GOLD, 300f, 10f);
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
                if (at(SERVED)) {
                    rumble(0.2f, 8f);
                    sparks(cx() - worldWidth() * 0.24f, worldHeight() * 0.42f, 30, c("fff8e8"), 500f, 14f);
                }
            }
            case GARDIEN_PHARE -> {
                if (at(BEAM_WHITE)) rumble(0.25f, 6f);
                if (at(DAZZLE)) rumble(0.2f, 4f);
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
                if (at(SABRE)) {
                    rumble(0.3f, 10f);
                    sparks(sabreX(), kingY() - 10f, 30, Color.WHITE, 500f, 12f);
                    debris(sabreX(), kingY(), 8, pixel, 10f, 500f);
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
            float hat = gesture(GESTURE, HAT_END);                   // il ajuste son chapeau
            pose(0f, 10f * hat, 4f * MathUtils.sin((time - GESTURE) * 18f) * hat, 0.02f * hat);
            glow(batch, cx(), kingY() + 250f, 1000f, RED, 0.35f * reveal);
            drawKing(batch, cx(), kingY(), KING_SCALE, 1f - reveal, 1f);
            if (hat > 0f) glow(batch, cx(), kingY() + 44f * KING_SCALE, 260f, GOLD, 0.4f * hat);
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
        // Les rats sortent devant et saluent leur capitaine.
        float up = Interpolation.swingOut.apply(progress(RATS_UP, RATS_UP + 0.3f));
        if (up > 0f) {
            float scale = 17f, ratW = rat.getRegionWidth() * scale, ratH = rat.getRegionHeight() * scale;
            for (int i = 0; i < 6; i++) {
                float x = width * RAT_SPOTS[i], y = MathUtils.lerp(-ratH - MARGIN, -ratH * 0.05f, up) + (i % 2) * 20f;
                batch.setColor(Color.WHITE);
                batch.draw(rat, x - ratW / 2f, y, ratW, ratH);
                float salute = progress(SALUTE + i * 0.05f, SALUTE + 0.15f + i * 0.05f);
                if (salute > 0f) {                                     // la patte levée au front
                    float sx = x + ratW * 0.3f, sy = y + ratH * 0.35f;
                    line(batch, c("0a0808"), sx, sy, sx + 10f, sy + ratH * 0.5f * salute, 26f);
                    line(batch, c("4a4048"), sx, sy, sx + 10f, sy + ratH * 0.5f * salute, 16f);
                }
            }
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
        float wipe = gesture(GESTURE, WIPE_END);
        float cloth = MathUtils.sin((time - GESTURE) * 11f);
        pose(30f * cloth * wipe, 0f, -3f * cloth * wipe, 0f);
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
        // Il essuie le comptoir, le chiffon va et vient.
        if (wipe > 0f) {
            float x = cx() - 40f + 220f * cloth;
            rect(batch, c("b8b0a0"), x - 80f, counterTop() - 14f, 160f, 34f);
            rect(batch, c("e8e0c8"), x - 74f, counterTop() - 6f, 148f, 22f);
        }
        // Puis il te sert une chope : elle glisse vers toi et grossit.
        if (time >= SERVE) {
            float k = Interpolation.pow2Out.apply(progress(SERVE, SERVED));
            float w = MathUtils.lerp(180f, 560f, k), h = w * 290f / 260f;
            float x = MathUtils.lerp(cx() - 120f, cx() - width * 0.24f, k), y = MathUtils.lerp(counterTop() - 8f, -h * 0.2f, k);
            glow(batch, x, y + h * 0.6f, w * 1.6f, c("ffb040"), 0.3f * k);
            batch.setColor(Color.WHITE);
            batch.draw(mug, x - w / 2f, y, w, h);
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
            float turn2 = gesture(GESTURE, DAZZLE + 0.6f);            // il tourne sa lanterne vers toi
            pose(0f, 0f, -4f * turn2, 0.03f * turn2);
            glow(batch, cx(), kingY() + 300f, 1200f, BEAM, 0.35f);
            drawKing(batch, cx(), kingY(), KING_SCALE, 0f, 1f);
            float dazzle = Interpolation.pow2In.apply(progress(GESTURE, DAZZLE)) * (1f - progress(DAZZLE, DAZZLE + 0.7f));
            if (dazzle > 0f) {
                glow(batch, lanternX(), lanternY(), MathUtils.lerp(200f, 2600f, dazzle), BEAM, 0.9f);
                glow(batch, lanternX(), lanternY(), 260f, Color.WHITE, dazzle);
                fill(batch, Color.WHITE, 0.8f * dazzle * dazzle * (settings.isReducedEffects() ? 0.4f : 1f));
            }
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
            float plant = gesture(GESTURE, SABRE + 0.4f);           // il plante son sabre dans le pont
            pose(0f, -40f * plant, -4f * plant, -0.04f * plant);
            drawKing(batch, cx(), y, KING_SCALE, 0f, 1f);
        }
        drawFlag(batch);
        if (time >= GESTURE) {                                     // le sabre, qui tombe et se plante
            float k = Interpolation.pow2In.apply(progress(GESTURE, SABRE));
            float tip = MathUtils.lerp(kingY() + 500f, kingY() - 30f, k);
            float x = sabreX();
            line(batch, c("2a2a34"), x, tip, x + 30f, tip + 300f, 22f);
            line(batch, c("d8e0f0"), x, tip, x + 30f, tip + 300f, 12f);
            rect(batch, GOLD, x + 30f - 50f, tip + 300f - 8f, 100f, 16f);
            line(batch, c("3a2618"), x + 30f, tip + 300f, x + 36f, tip + 380f, 18f);
        }
        flash(batch, c("ffd8a0"), CANNON, 0.18f, 0.5f);
    }

    /** Le pavillon noir à tête de mort sur son mât, qui claque au vent (plus fort une fois le sabre planté). */
    private void drawFlag(Batch batch) {
        float poleX = worldWidth() * 0.86f, top = worldHeight() * 0.92f;
        line(batch, c("2a1a10"), poleX, -MARGIN, poleX, top + 20f, 16f);
        float strength = 14f + 40f * progress(SABRE, SABRE + 0.2f) * (1f - 0.5f * progress(SABRE + 0.6f, SABRE + 1.6f));
        float flagW = 300f, flagH = 190f, strips = 24f;
        for (int j = 0; j < strips; j++) {
            float k = j / strips, wave = MathUtils.sin(time * 9f - j * 0.45f) * strength * k;
            float x = poleX - (j + 1) * flagW / strips;
            rect(batch, j % 6 == 0 ? c("141418") : c("0a0a0c"), x, top - flagH + wave, flagW / strips + 1f, flagH);
            if (j >= 8 && j <= 15) {                                  // la tête de mort
                float skull = j <= 13 ? 1f : 0f;
                if (skull > 0f) rect(batch, c("e8e8e0"), x, top - flagH * 0.42f + wave, flagW / strips + 1f, 56f);
                rect(batch, c("e8e8e0"), x, top - flagH * 0.82f + wave + (j % 2) * 16f, flagW / strips + 1f, 14f);
            }
        }
    }

    private float sabreX()  { return cx() + worldWidth() * 0.14f; }
    private float lanternX() { return cx() - 13f * KING_SCALE; }
    private float lanternY() { return kingY() + 27f * KING_SCALE; }

    private float cannonX() { return cx() - worldWidth() * 0.18f; }
    private float cannonY() { return worldHeight() * 0.36f; }

    // -------------------------------------------------------------------------
    // Le décor, avant l'entrée
    // -------------------------------------------------------------------------

    /** Capitaine Rat : le quai la nuit, un fromage sous la lanterne. Des yeux s'allument, il disparaît. */
    private void drawQuay(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, NIGHT_LOW, NIGHT_TOP);
        glow(batch, width * 0.75f, height * 0.85f, 260f, c("fff2c0"), 0.6f);                 // la lune
        rect(batch, SEA, -MARGIN, quayY(), width + 2f * MARGIN, height * 0.2f);
        for (int k = 0; k < 6; k++) {                                                       // son reflet
            batch.setColor(1f, 0.95f, 0.75f, 0.3f);
            batch.draw(pixel, width * 0.75f - 60f + 20f * MathUtils.sin(time * 3f + k), quayY() + 20f + k * 30f, 120f
                - k * 12f, 6f);
        }
        for (int i = 0; i < 8; i++) {                                                       // les planches du quai
            rect(batch, i % 2 == 0 ? c("3a2618") : c("2e1e12"), -MARGIN, quayY() - (i + 1) * 70f, width + 2f * MARGIN, 66f);
        }
        rect(batch, c("1c120c"), width * 0.25f - 10f, quayY(), 20f, height * 0.5f);         // le réverbère
        float lamp = pulse(11f, 0.85f);
        glow(batch, width * 0.25f, quayY() + height * 0.5f, 600f * lamp, c("ffb040"), 0.4f);
        glow(batch, cx(), quayY() + 40f, 700f, c("ffb040"), 0.25f * lamp);
        if (time < CHEESE_GONE) {
            batch.setColor(Color.WHITE);
            sprite(batch, cheese, cx(), quayY() + 90f, 20f, 0f);
        } else {                                                                            // des miettes
            for (int i = 0; i < 6; i++) rect(batch, c("ffd040"), cx() - 70f + i * 26f, quayY() + 8f + (i % 2) * 10f, 10f, 8f);
            float dash = progress(CHEESE_GONE, CHEESE_GONE + 0.2f);                         // une ombre file
            if (dash < 1f) {
                batch.setColor(0f, 0f, 0f, 0.8f);
                batch.draw(soft, MathUtils.lerp(cx(), -300f, dash) - 150f, quayY() + 20f, 300f, 90f);
            }
        }
        for (int i = 0; i < 5; i++) {                                                       // des yeux rouges, dans les coins
            float open = progress(0.6f + i * 0.13f, 0.7f + i * 0.13f) * (1f - progress(CHEESE_GONE, CHEESE_GONE + 0.3f));
            if (open <= 0f) continue;
            float x = width * (i % 2 == 0 ? 0.08f + i * 0.03f : 0.88f - i * 0.03f), y = quayY() - 60f - i * 60f;
            glow(batch, x, y, 80f, RED, 0.4f * open);
            rect(batch, RED, x - 14f, y - 3f * open, 8f, 6f * open);
            rect(batch, RED, x + 6f, y - 3f * open, 8f, 6f * open);
        }
    }

    private float quayY() { return worldHeight() * 0.42f; }

    /** Tavernier : des marins chantent autour d'une table et trinquent. */
    private void drawSailors(Batch batch) {
        float width = worldWidth(), height = worldHeight(), table = height * 0.3f;
        rect(batch, c("3a2014"), -MARGIN, -MARGIN, width + 2f * MARGIN, height + 2f * MARGIN);
        for (int i = 0; i < 20; i++) rect(batch, c("2e180e"), -MARGIN + i * 110f, -MARGIN, 8f, height + 2f * MARGIN);
        glow(batch, cx(), height * 0.75f, width, c("ffb040"), 0.3f * pulse(9f, 0.85f));
        float scale = 30f, sw = sailor.getRegionWidth() * scale, sh = sailor.getRegionHeight() * scale;
        for (int i = 0; i < 4; i++) {
            float x = cx() + (i - 1.5f) * 380f, bob = 12f * Math.abs(MathUtils.sin(time * 5f + i * 0.8f));
            batch.setColor(Color.WHITE);
            batch.draw(sailor, x - sw / 2f, table - 40f + bob, sw / 2f, 0f, sw, sh, 1f, 1f, 4f * MathUtils.sin(time * 5f + i));
        }
        // La table, puis les chopes qui se lèvent et se cognent au milieu.
        rect(batch, c("1c0e08"), -MARGIN, -MARGIN, width + 2f * MARGIN, table + MARGIN);
        rect(batch, c("6b3a1e"), cx() - width * 0.4f, table - 30f, width * 0.8f, 30f);
        float raise = 0f;
        for (float clink : CLINKS) raise = Math.max(raise, MathUtils.sin(MathUtils.clamp((time - clink + 0.35f) / 0.6f,
            0f, 1f) * MathUtils.PI));
        for (int i = 0; i < 4; i++) {
            float restX = cx() + (i - 1.5f) * 380f + (i < 2 ? 130f : -130f);
            float meetX = cx() + (i - 1.5f) * 70f;
            float x = MathUtils.lerp(restX, meetX, raise), y = MathUtils.lerp(table, clinkY() - 80f, raise);
            batch.setColor(Color.WHITE);
            batch.draw(mug, x - 80f, y, 80f, 0f, 160f, 180f, 1f, 1f, (i < 2 ? -1f : 1f) * 20f * raise);
        }
        for (int i = 0; i < 8; i++) {                                                       // les notes de la chanson
            float k = ((time * 0.6f + i * 0.125f) % 1f);
            float x = cx() + (i - 3.5f) * 170f + 30f * MathUtils.sin(time * 4f + i);
            batch.setColor(1f, 0.95f, 0.8f, 1f - k);
            sprite(batch, note, x, height * 0.6f + k * height * 0.35f, 8f, 10f * MathUtils.sin(time * 5f + i));
        }
    }

    private float clinkY() { return worldHeight() * 0.62f; }

    /** Gardien du Phare : l'orage sur la mer, les vagues frappent les rochers, un éclair. */
    private void drawStorm(Batch batch) {
        float width = worldWidth(), height = worldHeight(), horizon = height * 0.45f;
        float lit = time >= STORM_BOLT ? Math.max(0f, 1f - (time - STORM_BOLT) / 0.3f) : 0f;
        drawSea(batch, horizon);
        for (Particle cloud : fog) {                                                        // les nuages d'orage
            float x = ((cloud.x % (width + 800f)) + width + 800f) % (width + 800f) - 400f;
            float grey = 0.1f + 0.4f * lit;
            batch.setColor(grey, grey, grey + 0.05f, 0.7f);
            batch.draw(soft, x - cloud.size / 2f, height * 0.75f + (cloud.y - height * 0.45f) * 0.6f, cloud.size,
                cloud.size * 0.4f);
        }
        bolt(batch, width * 0.62f, horizon, 21L, lit);
        // Les grosses vagues, puis les rochers devant.
        for (int i = 0; i < 6; i++) {
            float x = ((i * 330f + time * 260f) % (width + 400f)) - 200f, y = height * 0.2f + 30f * MathUtils.sin(time * 3f + i);
            batch.setColor(SEA_LIGHT.r, SEA_LIGHT.g, SEA_LIGHT.b, 0.8f);
            batch.draw(soft, x - 220f, y - 50f, 440f, 140f);
        }
        for (int i = 0; i < 3; i++) {
            float x = width * (0.25f + 0.25f * i);
            rect(batch, c("12121a"), x - 160f, -MARGIN, 320f, height * 0.2f + MARGIN);
            rect(batch, c("12121a"), x - 100f, height * 0.2f, 200f, 60f);
            rect(batch, c("1e1e2a"), x - 60f, height * 0.25f, 90f, 40f);
        }
        rain(batch, 180, 0.4f);
        fill(batch, c("c8d8ff"), 0.4f * lit * (settings.isReducedEffects() ? 0.3f : 1f));
    }

    /** Capitaine Noir : la cloche du port sonne deux fois dans le brouillard. */
    private void drawBell(Batch batch) {
        float width = worldWidth(), height = worldHeight(), horizon = height * 0.42f;
        drawSea(batch, horizon);
        for (int i = 0; i < 6; i++) rect(batch, i % 2 == 0 ? c("2e1e12") : c("261810"), -MARGIN, height * 0.2f - (i + 1) * 60f,
            width + 2f * MARGIN, 56f);
        float bellX = width * 0.4f, beam = height * 0.78f;
        rect(batch, c("2a1a10"), bellX - 220f, height * 0.18f, 26f, beam - height * 0.18f);
        rect(batch, c("2a1a10"), bellX + 194f, height * 0.18f, 26f, beam - height * 0.18f);
        rect(batch, c("3a2618"), bellX - 240f, beam, 480f, 30f);
        float swing = 0f;
        for (float strike : RINGS) if (time >= strike) swing = 24f * MathUtils.sin((time - strike) * 9f) * Math.max(0f, 1f
            - (time - strike) * 1.1f);
        float scale = 26f, bw = bell.getRegionWidth() * scale, bh = bell.getRegionHeight() * scale;
        glow(batch, bellX, beam - bh * 0.5f, 600f, GOLD, 0.25f);
        batch.setColor(Color.WHITE);
        batch.draw(bell, bellX - bw / 2f, beam - bh, bw / 2f, bh, bw, bh, 1f, 1f, swing);
        for (float strike : RINGS) {                                                        // les ondes du son
            float k = progress(strike, strike + 0.9f);
            if (k <= 0f || k >= 1f) continue;
            float size = 200f + 1400f * k;
            batch.setColor(1f, 0.9f, 0.6f, 0.35f * (1f - k));
            batch.draw(ring, bellX - size / 2f, beam - bh * 0.5f - size / 2f, size, size);
        }
        for (Particle cloud : fog) {                                                        // le brouillard, épais
            float x = ((cloud.x % (width + 800f)) + width + 800f) % (width + 800f) - 400f;
            batch.setColor(0.8f, 0.82f, 0.9f, 0.35f);
            batch.draw(soft, x - cloud.size / 2f, cloud.y - cloud.size * 0.25f, cloud.size, cloud.size * 0.5f);
        }
    }
}
