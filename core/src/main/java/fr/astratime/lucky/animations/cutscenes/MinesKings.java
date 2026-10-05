package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.enemy.EnemyKind;

/**
 * Les rois des Mines d'Or entrent en scène :
 * <ul>
 *   <li>Baron de l'Or : un wagonnet fonce vers toi, freine net ; le baron descend, couvert d'or.</li>
 *   <li>Grand Foreur : le sol vibre, une foreuse géante perce le mur ; la poussière retombe.</li>
 *   <li>Maître de Forge : trois coups d'enclume, des étincelles ; il lève une lame rouge.</li>
 *   <li>Coeur de la Montagne : des rochers d'or roulent et s'assemblent ; un cœur s'allume dans la poitrine.</li>
 * </ul>
 */
public class MinesKings extends KingEntrance {

    /** Baron de l'Or : le wagonnet arrive, freine, le baron descend. */
    static final float CART_BRAKE = 1.35f, CART_STOP = 1.9f, BARON = 2.0f;
    /** Grand Foreur : le mur tremble, la foreuse perce, elle repart, le foreur est dans le trou. */
    static final float DRILL_IN = 1.4f, DRILL_OUT = 2.1f, DRILLER = 2.3f;
    /** Maître de Forge : les trois coups d'enclume, le maître, la lame levée. */
    static final float[] HITS = {0.6f, 1.15f, 1.7f};
    static final float SMITH = 1.95f, BLADE_UP = 2.4f;
    /** Coeur de la Montagne : les rochers roulent, s'assemblent, s'allument ; le cœur. */
    static final float ROLL_END = 1.4f, BUILT = 2.0f, GOLEM = 2.25f, HEART = 2.6f;

    /** La silhouette que forment les rochers d'or (o = un rocher). */
    private static final String[] BODY = {
        "..ooo..",
        "..ooo..",
        ".ooooo.",
        "ooooooo",
        "ooooooo",
        "o.ooo.o",
        "..ooo..",
        ".oo.oo.",
        ".oo.oo."};

    private static final Color ROCK = c("2a1c14"), ROCK_DARK = c("180f0a"), GOLD = c("ffc93a"), EMBER = c("ff6a1a");

    private final TextureRegion cart, anvil, boulder, pebble;
    private final Array<Particle> rocks = new Array<>(false, 64);
    private final Array<Particle> dust  = new Array<>(false, 64);
    private float rumbleAt;

    public MinesKings(CutsceneKit kit, EnemyKind kind) {
        super(kit, kind);
        Color[] cartColors = {c("140a0a"), c("4a4a5a"), c("6a6a7a"), c("ffc93a"), c("ffe58a"), c("9a9aa8")};
        cart = art("oiIgGw", cartColors,
            "...gGgggGgggGgggGg..",
            "..gggGgggggGggggGgg.",
            "oooooooooooooooooooo",
            "oiiiiiiiiiiiiiiiiiio",
            "oiIIIIIIIIIIIIIIIIio",
            "oiiiiiiiiiiiiiiiiiio",
            "oiIIIIIIIIIIIIIIIIio",
            "oiiiiiiiiiiiiiiiiiio",
            "oooooooooooooooooooo",
            "..owwo........owwo..",
            "..owwo........owwo..",
            "...oo..........oo...");
        Color[] anvilColors = {c("0a0808"), c("8a8a9a"), c("4a4a5a"), c("2e2e3a")};
        anvil = art("oLgd", anvilColors,
            "oooooooooooooooooooooo",
            "oLLLLLLLLLLLLLLLLLLLLo",
            "ogggggggggggggggggggo.",
            ".ooggggggggggggggoo...",
            "....oggggggggggo......",
            ".....oddddddddo.......",
            "....oddddddddddo......",
            "...oddddddddddddo.....",
            "...oooooooooooooo.....");
        Color[] rockColors = {c("1a0e06"), c("ffc93a"), c("b8801f"), c("fff0a0")};
        boulder = art("ogdw", rockColors,
            "..oooo..",
            ".ogwggo.",
            "ogwggggo",
            "oggggdgo",
            "ogggdddo",
            "oggdddgo",
            ".odddgo.",
            "..oooo..");
        pebble = art("od", new Color[] {c("1a0e06"), c("5a4030")},
            ".oo.",
            "oddo",
            "oddo",
            ".oo.");
    }

    @Override
    protected void restart() {
        rocks.clear();
        dust.clear();
        rumbleAt = 0.2f;
        if (kind == EnemyKind.COEUR_MONTAGNE) {
            float spacing = 64f;
            for (int row = 0; row < BODY.length; row++) {
                for (int col = 0; col < BODY[row].length(); col++) {
                    if (BODY[row].charAt(col) != 'o') continue;
                    boolean left = random.nextBoolean();
                    Particle rock = particle(left ? -150f : worldWidth() + 150f, kingY() + 30f, 0f, 0f, 99f,
                        7f + 2f * random.nextFloat());
                    rock.scatterX = cx() + (left ? -1f : 1f) * worldWidth() * (0.1f + 0.3f * random.nextFloat());
                    rock.scatterY = kingY() + 20f;
                    rock.targetX = cx() + (col - 3f) * spacing;
                    rock.targetY = kingY() + 40f + (BODY.length - 1 - row) * spacing;
                    rock.delay = random.nextFloat();
                    rocks.add(rock);
                }
            }
        }
    }

    @Override
    protected void step(float delta) {
        switch (kind) {
            case BARON_OR -> {
                if (time >= CART_BRAKE && time < CART_STOP && random.nextFloat() < 30f * delta) {
                    for (int side = -1; side <= 1; side += 2) {
                        sparks(cx() + side * cartWidth() * 0.32f, cartY(), 4, EMBER, 500f, 10f);
                    }
                }
                if (at(CART_STOP)) rumble(0.3f, 10f);
                if (at(BARON + 0.3f)) sparks(cx(), kingY() + 300f, 40, GOLD, 700f, 14f);
            }
            case GRAND_FOREUR -> {
                if (time < DRILL_IN && time >= rumbleAt) {     // le sol vibre de plus en plus
                    rumble(0.15f, 2f + 8f * progress(0f, DRILL_IN));
                    rumbleAt += 0.14f;
                    debris(worldWidth() * random.nextFloat(), worldHeight(), 2, pebble, 6f, 100f);
                }
                if (at(DRILL_IN)) {
                    rumble(0.6f, 18f);
                    debris(cx(), worldHeight() * 0.5f, 40, pebble, 14f, 1400f);
                    for (int i = 0; i < 24; i++) {
                        Particle cloud = particle(cx(), worldHeight() * 0.5f, 360f * random.nextFloat(),
                            200f + 400f * random.nextFloat(), 1.4f + random.nextFloat(), 200f + 200f * random.nextFloat());
                        cloud.drag = 0.2f;
                        cloud.gravity = 40f;
                        cloud.color.set(0.55f, 0.42f, 0.32f, 0.45f);
                        dust.add(cloud);
                    }
                }
            }
            case MAITRE_FORGE -> {
                for (float hit : HITS) {
                    if (at(hit)) {
                        rumble(0.2f, 9f);
                        sparks(cx(), anvilTop(), 50, GOLD, 900f, 12f);
                        sparks(cx(), anvilTop(), 20, EMBER, 600f, 16f);
                    }
                }
                if (at(BLADE_UP + 0.3f)) sparks(bladeX(), kingY() + 40f * KING_SCALE, 30, EMBER, 500f, 14f);
            }
            default -> {
                if (time < ROLL_END && time >= rumbleAt) {
                    rumble(0.15f, 4f);
                    rumbleAt += 0.2f;
                }
                if (at(BUILT)) {
                    rumble(0.3f, 10f);
                    sparks(cx(), kingY() + 300f, 40, GOLD, 800f, 14f);
                }
                if (at(HEART)) sparks(heartX(), heartY(), 24, c("ffe58a"), 400f, 12f);
            }
        }
        update(dust, delta);
    }

    @Override
    protected void draw(Batch batch) {
        drawCave(batch);
        switch (kind) {
            case BARON_OR -> drawBaron(batch);
            case GRAND_FOREUR -> drawDriller(batch);
            case MAITRE_FORGE -> drawSmith(batch);
            default -> drawGolem(batch);
        }
        for (Particle cloud : dust) {
            float life = cloud.life();
            batch.setColor(cloud.color.r, cloud.color.g, cloud.color.b, cloud.color.a * (1f - life));
            batch.draw(soft, cloud.x - cloud.size / 2f, cloud.y - cloud.size / 2f, cloud.size, cloud.size);
        }
        drawParticles(batch, parts);
    }

    /** La galerie : roche sombre, veines d'or qui luisent, deux poutres de bois. */
    private void drawCave(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, ROCK, ROCK_DARK);
        java.util.Random veins = new java.util.Random(kind.ordinal() * 31L);
        additive(batch);
        for (int i = 0; i < 18; i++) {                              // veines d'or
            float x = width * veins.nextFloat(), y = height * (0.2f + 0.8f * veins.nextFloat());
            float length = 80f + 220f * veins.nextFloat(), angle = -40f + 80f * veins.nextFloat();
            batch.setColor(GOLD.r, GOLD.g, GOLD.b, 0.25f + 0.2f * MathUtils.sin(time * 2f + i));
            batch.draw(pixel, x, y, 0f, 3f, length, 6f, 1f, 1f, angle);
        }
        normal(batch);
        rect(batch, c("120a06"), -MARGIN, -MARGIN, width + 2f * MARGIN, height * 0.16f + MARGIN);
        for (int side = -1; side <= 1; side += 2) {                 // poutres et lanternes
            float x = cx() + side * width * 0.42f;
            rect(batch, c("4a2a14"), x - 26f, -MARGIN, 52f, height + 2f * MARGIN);
            rect(batch, c("6b3e1e"), x - 26f, -MARGIN, 12f, height + 2f * MARGIN);
            glow(batch, x - side * 70f, height * 0.62f, 380f * pulse(13f, 0.85f), c("ff9a2a"), 0.35f);
            glow(batch, x - side * 70f, height * 0.62f, 60f, c("ffe066"), 0.9f);
        }
        rect(batch, c("4a2a14"), -MARGIN, height * 0.88f, width + 2f * MARGIN, 44f);
        rect(batch, c("6b3e1e"), -MARGIN, height * 0.88f + 32f, width + 2f * MARGIN, 12f);
    }

    // -------------------------------------------------------------------------
    // Baron de l'Or : le wagonnet
    // -------------------------------------------------------------------------

    private void drawBaron(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        float vanishY = height * 0.55f;
        // Les rails en perspective, et les traverses qui défilent tant que le wagonnet roule.
        float speed = time < CART_STOP ? 1f - 0.8f * progress(CART_BRAKE, CART_STOP) : 0f;
        for (int i = 0; i < 12; i++) {
            float k = ((i / 12f + time * 0.9f * speed) % 1f);
            float y = MathUtils.lerp(vanishY, -MARGIN, k * k);
            float half = MathUtils.lerp(20f, width * 0.32f, k * k);
            rect(batch, c("3a2414"), cx() - half * 1.15f, y - 6f * k, half * 2.3f, 14f * k + 2f);
        }
        for (int side = -1; side <= 1; side += 2) {
            batch.setColor(c("9a9aa8"));
            float x0 = cx() + side * 20f, x1 = cx() + side * width * 0.32f;
            float dx = x1 - x0, dy = -MARGIN - vanishY, length = (float) Math.sqrt(dx * dx + dy * dy);
            batch.draw(pixel, x0, vanishY - 3f, 0f, 3f, length, 6f, 1f, 1f, MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees);
        }
        // Le baron descend du wagonnet.
        if (time >= BARON) {
            float k = Interpolation.pow2Out.apply(progress(BARON, BARON + 0.45f));
            float y = MathUtils.lerp(cartY() + cartWidth() * 0.15f, kingY() + 60f, k);
            float x = MathUtils.lerp(cx(), cx() + width * 0.02f, k);
            glow(batch, x, kingY() + 300f, 1000f * k, GOLD, 0.4f);
            drawKing(batch, x, y, KING_SCALE, 1f - k, Math.min(1f, k * 3f));
        }
        float scale = cartWidth() / cart.getRegionWidth();
        batch.setColor(Color.WHITE);
        batch.draw(cart, cx() - cartWidth() / 2f, cartY(), cartWidth(), cart.getRegionHeight() * scale);
    }

    private float cartProgress() { return Interpolation.pow2Out.apply(progress(0f, CART_STOP)); }
    private float cartWidth()    { return MathUtils.lerp(60f, worldWidth() * 0.42f, cartProgress()); }
    private float cartY()        { return MathUtils.lerp(worldHeight() * 0.55f, worldHeight() * 0.02f, cartProgress()); }

    // -------------------------------------------------------------------------
    // Grand Foreur : la foreuse qui perce le mur
    // -------------------------------------------------------------------------

    private void drawDriller(Batch batch) {
        float width = worldWidth(), height = worldHeight(), y = height * 0.5f;
        // Le mur de roche, en gros blocs.
        java.util.Random blocks = new java.util.Random(7);
        for (int i = 0; i < 60; i++) {
            float bx = width * blocks.nextFloat() - 100f, by = height * blocks.nextFloat() - 60f;
            rect(batch, blocks.nextBoolean() ? c("3a281c") : c("241810"), bx, by, 160f + 160f * blocks.nextFloat(),
                90f + 90f * blocks.nextFloat());
        }
        float hole = Interpolation.pow2Out.apply(progress(DRILL_IN, DRILL_IN + 0.3f));
        if (hole > 0f) {
            batch.setColor(0f, 0f, 0f, 0.95f);
            batch.draw(soft, cx() - 650f * hole, y - 650f * hole, 1300f * hole, 1300f * hole);
            batch.draw(soft, cx() - 450f * hole, y - 450f * hole, 900f * hole, 900f * hole);
        }
        if (time >= DRILLER) {
            float k = progress(DRILLER, DRILLER + 0.4f);
            glow(batch, cx(), kingY() + 280f, 900f, c("ff9a2a"), 0.3f * k);
            drawKing(batch, cx(), kingY(), KING_SCALE, 1f - k, k);
        }
        // La foreuse vue de face : une pointe qui tourne, qui grossit en perçant puis repart.
        if (time >= DRILL_IN - 0.1f && time < DRILLER) {
            float out = progress(DRILL_OUT, DRILLER);
            float size = MathUtils.lerp(80f, 520f, Interpolation.pow2Out.apply(progress(DRILL_IN - 0.1f, DRILL_IN + 0.2f)))
                * (1f - out);
            for (int ring = 0; ring < 5; ring++) {                       // le cône d'acier, en anneaux
                float r = size * (1f - ring * 0.18f);
                batch.setColor(tmp.set(c("3a3a48")).lerp(c("d8d8e8"), ring / 4f));
                batch.draw(soft, cx() - r, y - r, r * 2f, r * 2f);
            }
            for (int arm = 0; arm < 3; arm++) {                         // les rainures en spirale qui tournent
                for (int k = 2; k < 24; k++) {
                    float r = size * 0.62f * k / 24f, angle = arm * 120f + k * 22f - time * 1100f;
                    float dot = size * 0.05f * (0.4f + k / 24f);
                    batch.setColor(c("1a1a24"));
                    batch.draw(pixel, cx() + MathUtils.cosDeg(angle) * r - dot / 2f,
                        y + MathUtils.sinDeg(angle) * r - dot / 2f, dot, dot);
                }
            }
            glow(batch, cx(), y, size * 0.5f, Color.WHITE, 0.7f);
        }
    }

    // -------------------------------------------------------------------------
    // Maître de Forge : l'enclume
    // -------------------------------------------------------------------------

    private void drawSmith(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        glow(batch, width * 0.12f, height * 0.3f, width * 0.8f, EMBER, 0.35f * pulse(11f, 0.7f));   // le feu de la forge
        float heat = 0f;
        for (float hit : HITS) if (time >= hit) heat = Math.max(heat, 1f - (time - hit) / 0.25f);
        glow(batch, cx(), anvilTop(), 900f, GOLD, 0.5f * Math.max(0f, heat));
        if (time >= SMITH) {
            float k = progress(SMITH, SMITH + 0.4f);
            drawKing(batch, cx(), anvilTop() - 40f, KING_SCALE, 1f - k, k);
        }
        // La lame rouge, levée à bout de bras.
        if (time >= BLADE_UP) {
            float k = Interpolation.swingOut.apply(progress(BLADE_UP, BLADE_UP + 0.35f));
            float angle = MathUtils.lerp(0f, 80f, k);
            float baseX = bladeX(), baseY = kingY() + 22f * KING_SCALE;
            glow(batch, baseX + MathUtils.cosDeg(angle) * 180f, baseY + MathUtils.sinDeg(angle) * 180f, 420f, EMBER,
                0.6f * pulse(9f, 0.7f));
            batch.setColor(c("1a0a0a"));
            batch.draw(pixel, baseX, baseY - 18f, 0f, 18f, 400f, 36f, 1f, 1f, angle);
            batch.setColor(c("ff4a1a"));
            batch.draw(pixel, baseX, baseY - 12f, 0f, 12f, 390f, 24f, 1f, 1f, angle);
            batch.setColor(c("ffd060"));
            batch.draw(pixel, baseX, baseY - 4f, 0f, 4f, 380f, 8f, 1f, 1f, angle);
        }
        // L'enclume.
        float scale = 26f;
        batch.setColor(Color.WHITE);
        batch.draw(anvil, cx() - anvil.getRegionWidth() * scale / 2f, anvilTop() - anvil.getRegionHeight() * scale,
            anvil.getRegionWidth() * scale, anvil.getRegionHeight() * scale);
        // Le marteau qui tombe avant chaque coup.
        for (float hit : HITS) {
            if (time < hit - 0.25f || time > hit + 0.1f) continue;
            float k = time < hit ? Interpolation.pow2In.apply(progress(hit - 0.25f, hit)) : 1f;
            float angle = MathUtils.lerp(70f, 0f, k);
            float pivotX = cx() + 520f, pivotY = anvilTop() + 40f;
            batch.setColor(c("6b3a1e"));
            batch.draw(pixel, pivotX, pivotY - 12f, 0f, 12f, -400f, 24f, 1f, 1f, -angle);
            float headX = pivotX - MathUtils.cosDeg(angle) * 420f, headY = pivotY + MathUtils.sinDeg(angle) * 420f;
            rect(batch, c("2e2e3a"), headX - 60f, headY - 40f, 120f, 90f);
            rect(batch, c("6a6a7a"), headX - 60f, headY + 30f, 120f, 20f);
        }
        flash(batch, c("ffe0a0"), HITS[2], 0.15f, 0.35f);
    }

    private float anvilTop() { return worldHeight() * 0.32f; }
    private float bladeX()   { return cx() + 12f * KING_SCALE; }

    // -------------------------------------------------------------------------
    // Coeur de la Montagne : les rochers d'or
    // -------------------------------------------------------------------------

    private void drawGolem(Batch batch) {
        float built = progress(BUILT, BUILT + 0.3f);
        float golem = progress(GOLEM, GOLEM + 0.35f);
        if (time >= GOLEM) {
            glow(batch, cx(), kingY() + 280f, 1100f, GOLD, 0.35f * golem);
            drawKing(batch, cx(), kingY(), KING_SCALE, 0f, golem);
        }
        if (golem < 1f) {
            for (Particle rock : rocks) {
                float x, y, angle;
                if (time < ROLL_END) {                               // ils roulent sur le sol
                    float k = MathUtils.clamp((time - rock.delay * 0.4f) / (ROLL_END - 0.4f), 0f, 1f);
                    x = MathUtils.lerp(rock.x, rock.scatterX, Interpolation.pow2Out.apply(k));
                    y = rock.y;
                    angle = (rock.scatterX - x) * 0.8f;
                } else {                                             // puis s'élèvent et s'assemblent
                    float k = MathUtils.clamp((time - ROLL_END - rock.delay * 0.2f) / (BUILT - ROLL_END - 0.2f), 0f, 1f);
                    float e = Interpolation.pow2.apply(k);
                    x = MathUtils.lerp(rock.scatterX, rock.targetX, e);
                    y = MathUtils.lerp(rock.scatterY, rock.targetY, e) + 4f * e * (1f - e) * 120f;
                    angle = (1f - e) * 200f;
                }
                if (built > 0f) glow(batch, x, y, 140f, GOLD, 0.5f * built);
                batch.setColor(1f, 1f, 1f, 1f - golem);
                sprite(batch, boulder, x, y, rock.size, angle);
            }
        }
        if (time >= HEART) {                                         // le cœur s'allume dans la poitrine
            float k = progress(HEART, HEART + 0.3f);
            glow(batch, heartX(), heartY(), 260f * pulse(8f, 0.7f), c("ffe58a"), 0.9f * k);
            glow(batch, heartX(), heartY(), 600f * pulse(8f, 0.7f), GOLD, 0.4f * k);
        }
        flash(batch, GOLD, BUILT, 0.2f, 0.35f);
    }

    private float heartX() { return cx(); }
    private float heartY() { return kingY() + 13f * KING_SCALE; }
}
