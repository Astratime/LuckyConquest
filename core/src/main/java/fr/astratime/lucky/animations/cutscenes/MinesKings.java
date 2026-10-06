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
 *   <li>Baron de l'Or : les rails brillent dans le noir. Un wagonnet fonce vers toi, freine net ; le baron
 *   descend, couvert d'or. Il mord une pépite pour vérifier qu'elle est vraie.</li>
 *   <li>Grand Foreur : les lanternes tremblent au plafond. Le sol vibre, une foreuse géante perce le mur ;
 *   la poussière retombe. Il retire ses lunettes et crache de la poussière.</li>
 *   <li>Maître de Forge : le soufflet attise les braises. Trois coups d'enclume, des étincelles ; il lève une
 *   lame rouge, puis la trempe dans l'eau : un nuage de vapeur.</li>
 *   <li>Coeur de la Montagne : des mineurs lâchent leurs pioches et fuient. Des rochers d'or roulent et
 *   s'assemblent ; un cœur s'allume dans la poitrine. Il bat trois fois, les murs se fissurent.</li>
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

    /** Le décor. Baron : les reflets sur les rails. Forge : les coups de soufflet. Coeur : les mineurs fuient. */
    static final float[] GLINTS = {0.2f, 1.0f};
    static final float[] PUMPS = {0.3f, 0.9f, 1.5f};
    static final float DROP_PICKS = 0.5f, FLEE = 0.75f;
    /** Le geste. Baron : il mord la pépite. Foreur : les lunettes, il crache. */
    static final float BITE = 3.9f, BITE_END = 4.5f, GOGGLES_OFF = 3.9f, SPIT = 4.4f;
    /** Forge : la lame plonge dans l'eau. Coeur : les trois battements. */
    static final float QUENCH = 3.85f;
    static final float[] THUMPS = {3.6f, 4.15f, 4.7f};

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

    private final TextureRegion cart, anvil, boulder, pebble, miner, lantern;
    private final Array<Particle> rocks = new Array<>(false, 64);
    private final Array<Particle> dust  = new Array<>(false, 64);
    private float rumbleAt, quakeAt;

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
        miner = art("oyhsbl", new Color[] {c("0a0808"), c("ffe066"), c("d8a020"), c("e8b080"), c("4a5a8a"),
                c("6b4a2a")},
            "...oyo...",
            "..ohhho..",
            ".ohhhhho.",
            "..ossso..",
            "..ossso..",
            "..ossso..",
            ".obbbbbo.",
            "obbbbbbbo",
            "obobbbobo",
            "o.obbbo.o",
            "..olllo..",
            "..ol.lo..",
            "..oo.oo..");
        lantern = art("odyw", new Color[] {c("0a0808"), c("3a3a48"), c("ffb040"), c("fff0a0")},
            "..oo..",
            ".o..o.",
            "dddddd",
            "dyywyd",
            "dywwyd",
            "dyyyyd",
            "dddddd");
    }

    @Override
    protected void restart() {
        rocks.clear();
        dust.clear();
        rumbleAt = 0.2f;
        quakeAt = 0f;
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
    protected void stepPrelude(float delta) {
        switch (kind) {
            case GRAND_FOREUR -> {
                if (time >= quakeAt) {                          // le plafond tremble, la poussière tombe
                    rumble(0.12f, 1f + 3f * time);
                    quakeAt += 0.25f;
                    debris(worldWidth() * random.nextFloat(), worldHeight() * 0.86f, 2, pebble, 4f, 80f);
                }
            }
            case MAITRE_FORGE -> {
                for (float pump : PUMPS) {
                    if (!at(pump + 0.2f)) continue;
                    for (int i = 0; i < 24; i++) {             // les braises qui s'envolent
                        Particle ember = particle(cx() + (random.nextFloat() - 0.5f) * 300f, forgeY(), 70f + 40f
                            * random.nextFloat(), 300f + 400f * random.nextFloat(), 1f + random.nextFloat(), 10f + 8f
                            * random.nextFloat());
                        ember.color.set(random.nextBoolean() ? EMBER : GOLD);
                        ember.drag = 0.5f;
                        parts.add(ember);
                    }
                }
            }
            case COEUR_MONTAGNE -> {
                if (at(DROP_PICKS)) rumble(0.25f, 6f);
            }
            default -> {}
        }
    }

    @Override
    protected void prelude(Batch batch) {
        switch (kind) {
            case BARON_OR -> drawGlints(batch);
            case GRAND_FOREUR -> drawLanterns(batch);
            case MAITRE_FORGE -> drawBellows(batch);
            default -> drawMiners(batch);
        }
        for (Particle cloud : dust) {
            batch.setColor(cloud.color.r, cloud.color.g, cloud.color.b, cloud.color.a * (1f - cloud.life()));
            batch.draw(soft, cloud.x - cloud.size / 2f, cloud.y - cloud.size / 2f, cloud.size, cloud.size);
        }
    }

    @Override
    protected void step(float delta) {
        switch (kind) {
            case BARON_OR -> {
                if (at(BITE)) {
                    sparks(nuggetX(), mouthY(), 16, GOLD, 300f, 8f);
                    rumble(0.1f, 3f);
                }
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
                if (at(SPIT)) {                                  // il crache un nuage de poussière vers toi
                    rumble(0.2f, 5f);
                    for (int i = 0; i < 26; i++) {
                        Particle cloud = particle(faceX(), faceY() - 2f * KING_SCALE, 250f + 40f * random.nextFloat(),
                            300f + 600f * random.nextFloat(), 1.2f + 0.6f * random.nextFloat(), 80f + 160f * random.nextFloat());
                        cloud.drag = 0.15f;
                        cloud.color.set(0.6f, 0.48f, 0.36f, 0.55f);
                        dust.add(cloud);
                    }
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
                if (at(QUENCH)) {                                // la vapeur jaillit du baquet
                    rumble(0.2f, 5f);
                    for (int i = 0; i < 30; i++) {
                        Particle steam = particle(barrelX(), barrelTop(), 60f + 60f * random.nextFloat(),
                            200f + 500f * random.nextFloat(), 1.4f + random.nextFloat(), 140f + 220f * random.nextFloat());
                        steam.drag = 0.25f;
                        steam.gravity = -60f;
                        steam.color.set(0.9f, 0.92f, 0.95f, 0.5f);
                        dust.add(steam);
                    }
                }
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
                for (float thump : THUMPS) {
                    if (!at(thump)) continue;
                    rumble(0.25f, 10f);
                    sparks(heartX(), heartY(), 20, GOLD, 600f, 12f);
                    debris(worldWidth() * random.nextFloat(), worldHeight() * 0.9f, 3, pebble, 6f, 200f);
                }
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
            float bite = gesture(GESTURE, BITE_END);                  // il porte la pépite à sa bouche
            pose(0f, 0f, 5f * bite, 0.02f * bite);
            drawKing(batch, x, y, KING_SCALE, 1f - k, Math.min(1f, k * 3f));
            if (bite > 0f) {
                float ny = MathUtils.lerp(mouthY() - 8f * KING_SCALE, mouthY(), bite);
                glow(batch, nuggetX(), ny, 220f, GOLD, 0.6f * bite);
                batch.setColor(Color.WHITE);
                sprite(batch, boulder, nuggetX(), ny, 7f, 20f * bite);
                if (time >= BITE) rect(batch, ROCK_DARK, nuggetX() + 6f, ny + 10f, 20f, 16f);  // la trace de dent
            }
        }
        float scale = cartWidth() / cart.getRegionWidth();
        batch.setColor(Color.WHITE);
        batch.draw(cart, cx() - cartWidth() / 2f, cartY(), cartWidth(), cart.getRegionHeight() * scale);
    }

    private float nuggetX() { return cx() + worldWidth() * 0.02f + 4f * KING_SCALE; }
    private float mouthY()  { return kingY() + 60f + 24f * KING_SCALE; }
    /** Le visage du foreur (la foreuse est à droite du portrait). */
    private float faceX()   { return cx() - 2f * KING_SCALE; }
    private float faceY()   { return kingY() + 22f * KING_SCALE; }

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
            float spit = gesture(SPIT - 0.25f, SPIT + 0.3f);            // il se penche pour cracher
            pose(0f, -20f * spit, -4f * spit, -0.03f * spit);
            glow(batch, cx(), kingY() + 280f, 900f, c("ff9a2a"), 0.3f * k);
            drawKing(batch, cx(), kingY(), KING_SCALE, 1f - k, k);
            if (time >= GESTURE) {                                      // les lunettes, retirées et jetées
                float off = Interpolation.pow2Out.apply(progress(GESTURE, GOGGLES_OFF));
                float toss = Interpolation.pow2In.apply(progress(GOGGLES_OFF + 0.2f, GOGGLES_OFF + 0.7f));
                float gx = faceX() + 300f * toss, gy = MathUtils.lerp(faceY() + 3f * KING_SCALE, faceY() + 14f
                    * KING_SCALE, off) - 700f * toss * toss;
                for (int side = -1; side <= 1; side += 2) {
                    batch.setColor(c("2a2a34"));
                    batch.draw(soft, gx + side * 50f - 46f, gy - 46f, 92f, 92f);
                    glow(batch, gx + side * 50f, gy, 70f, c("8ad8ff"), 0.8f);
                }
                line(batch, c("2a2a34"), gx - 100f, gy, gx + 100f, gy, 10f);
            }
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
        pose(0f, -20f * gesture(GESTURE, QUENCH + 0.5f), 0f, 0f);        // il se penche vers le baquet
        glow(batch, width * 0.12f, height * 0.3f, width * 0.8f, EMBER, 0.35f * pulse(11f, 0.7f));   // le feu de la forge
        float heat = 0f;
        for (float hit : HITS) if (time >= hit) heat = Math.max(heat, 1f - (time - hit) / 0.25f);
        glow(batch, cx(), anvilTop(), 900f, GOLD, 0.5f * Math.max(0f, heat));
        if (time >= SMITH) {
            float k = progress(SMITH, SMITH + 0.4f);
            drawKing(batch, cx(), anvilTop() - 40f, KING_SCALE, 1f - k, k);
        }
        // Le baquet d'eau, où il trempera la lame.
        rect(batch, c("3a2414"), barrelX() - 110f, barrelTop() - 220f, 220f, 220f);
        rect(batch, c("6a6a7a"), barrelX() - 116f, barrelTop() - 60f, 232f, 14f);
        rect(batch, c("6a6a7a"), barrelX() - 116f, barrelTop() - 180f, 232f, 14f);
        rect(batch, c("2a5a7a"), barrelX() - 100f, barrelTop() - 16f, 200f, 16f);
        // La lame rouge, levée à bout de bras, puis trempée dans l'eau.
        if (time >= BLADE_UP) {
            float k = Interpolation.swingOut.apply(progress(BLADE_UP, BLADE_UP + 0.35f));
            float dip = Interpolation.pow2In.apply(progress(GESTURE, QUENCH));
            float angle = MathUtils.lerp(MathUtils.lerp(0f, 80f, k), quenchAngle(), dip);
            float cool = progress(QUENCH, QUENCH + 0.5f);
            float baseX = bladeX(), baseY = kingY() + 22f * KING_SCALE;
            glow(batch, baseX + MathUtils.cosDeg(angle) * 180f, baseY + MathUtils.sinDeg(angle) * 180f, 420f, EMBER,
                0.6f * pulse(9f, 0.7f) * (1f - cool));
            batch.setColor(c("1a0a0a"));
            batch.draw(pixel, baseX, baseY - 18f, 0f, 18f, 400f, 36f, 1f, 1f, angle);
            batch.setColor(tmp.set(c("ff4a1a")).lerp(c("6a6a7a"), cool));
            batch.draw(pixel, baseX, baseY - 12f, 0f, 12f, 390f, 24f, 1f, 1f, angle);
            batch.setColor(tmp.set(c("ffd060")).lerp(c("c8c8d8"), cool));
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
    private float barrelX()  { return bladeX() + MathUtils.cosDeg(quenchAngle()) * 330f; }
    private float barrelTop() { return kingY() + 22f * KING_SCALE + MathUtils.sinDeg(quenchAngle()) * 330f + 20f; }
    private float forgeY()   { return worldHeight() * 0.3f; }
    /** L'angle de la lame plongée dans le baquet. */
    private static float quenchAngle() { return -35f; }

    // -------------------------------------------------------------------------
    // Coeur de la Montagne : les rochers d'or
    // -------------------------------------------------------------------------

    private void drawGolem(Batch batch) {
        float thumping = 0f;
        for (float thump : THUMPS) thumping = Math.max(thumping, gesture(thump - 0.05f, thump + 0.25f));
        pose(0f, 0f, 0f, 0.035f * thumping);
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
            float beat = 0f;
            for (float thump : THUMPS) if (time >= thump) beat = Math.max(beat, 1f - (time - thump) / 0.3f);
            beat = Math.max(0f, beat);
            glow(batch, heartX(), heartY(), 260f * pulse(8f, 0.7f) * (1f + beat), c("ffe58a"), 0.9f * k);
            glow(batch, heartX(), heartY(), 600f * pulse(8f, 0.7f) * (1f + 1.5f * beat), GOLD, 0.4f * k + 0.4f * beat);
        }
        // Les murs se fissurent à chaque battement : des éclairs d'or dans la roche.
        java.util.Random cracks = new java.util.Random(3);
        for (int i = 0; i < 9; i++) {
            float sx = worldWidth() * (i < 5 ? 0.02f + 0.06f * i : 0.98f - 0.06f * (i - 5)), sy = worldHeight()
                * (0.3f + 0.6f * cracks.nextFloat());
            int shown = 0;
            for (float thump : THUMPS) if (time >= thump) shown += 3;
            float x = sx, y = sy;
            for (int s = 0; s < 9; s++) {
                float nx = x + (i < 5 ? 1f : -1f) * (30f + 40f * cracks.nextFloat()), ny = y + (cracks.nextFloat() - 0.5f) * 90f;
                if (s < shown) {
                    line(batch, c("0a0604"), x, y, nx, ny, 16f);
                    additive(batch);
                    line(batch, tmp.set(GOLD.r, GOLD.g, GOLD.b, 0.8f), x, y, nx, ny, 6f);
                    normal(batch);
                }
                x = nx;
                y = ny;
            }
        }
        flash(batch, GOLD, BUILT, 0.2f, 0.35f);
    }

    private float heartX() { return cx(); }
    private float heartY() { return kingY() + 13f * KING_SCALE; }

    // -------------------------------------------------------------------------
    // Le décor, avant l'entrée
    // -------------------------------------------------------------------------

    /** Baron : les rails dans le noir, deux reflets filent le long des rails. */
    private void drawGlints(Batch batch) {
        float width = worldWidth(), height = worldHeight(), vanishY = height * 0.55f;
        drawCave(batch);
        fill(batch, Color.BLACK, 0.65f);
        for (int i = 0; i < 12; i++) {
            float k = i / 12f, y = MathUtils.lerp(vanishY, -MARGIN, k * k), half = MathUtils.lerp(20f, width * 0.32f, k * k);
            rect(batch, c("1e140c"), cx() - half * 1.15f, y - 6f * k, half * 2.3f, 14f * k + 2f);
        }
        for (int side = -1; side <= 1; side += 2) {
            float x0 = cx() + side * 20f, x1 = cx() + side * width * 0.32f;
            line(batch, c("4a4a58"), x0, vanishY, x1, -MARGIN, 6f);
            for (float glint : GLINTS) {                                 // le reflet qui file vers toi
                float k = progress(glint, glint + 0.7f);
                if (k <= 0f || k >= 1f) continue;
                float e = k * k;
                float gx = MathUtils.lerp(x0, x1, e), gy = MathUtils.lerp(vanishY, -MARGIN, e);
                glow(batch, gx, gy, 60f + 260f * e, c("fff0c0"), 0.9f * (1f - k * 0.5f));
            }
        }
        glow(batch, cx(), vanishY, 160f, c("ffb040"), 0.6f * pulse(7f, 0.5f));          // une lueur au bout du tunnel
    }

    /** Foreur : des lanternes au plafond, qui tremblent de plus en plus. */
    private void drawLanterns(Batch batch) {
        float width = worldWidth(), height = worldHeight(), beam = height * 0.88f;
        drawCave(batch);
        float shake = 2f + 12f * progress(0f, PRELUDE);
        for (int i = 0; i < 4; i++) {
            float x = width * (0.2f + 0.2f * i), angle = shake * MathUtils.sin(time * (9f + i * 2f) + i);
            float length = 260f + 80f * (i % 2);
            float lx = x + MathUtils.sinDeg(angle) * length, ly = beam - MathUtils.cosDeg(angle) * length;
            line(batch, c("2a2a34"), x, beam, lx, ly, 6f);
            float flicker = pulse(17f + i, 0.6f);
            glow(batch, lx, ly - 50f, 520f * flicker, c("ff9a2a"), 0.4f);
            batch.setColor(Color.WHITE);
            sprite(batch, lantern, lx, ly - 50f, 18f, angle);
        }
    }

    /** Forge : le grand soufflet souffle trois fois sur les braises du foyer. */
    private void drawBellows(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        drawCave(batch);
        float blow = 0f;
        for (float pump : PUMPS) blow = Math.max(blow, gesture(pump, pump + 0.45f));
        // Le foyer : une bouche de pierre pleine de braises.
        rect(batch, c("1a100a"), cx() - 340f, forgeY() - 160f, 680f, 420f);
        rect(batch, c("0a0604"), cx() - 260f, forgeY() - 60f, 520f, 240f);
        glow(batch, cx(), forgeY(), 900f * (1f + 0.6f * blow), EMBER, 0.45f + 0.4f * blow);
        for (int i = 0; i < 14; i++) {
            float heat = pulse(6f + i, 0.5f) * (0.6f + 0.4f * blow);
            rect(batch, tmp.set(EMBER).lerp(GOLD, heat), cx() - 240f + i * 34f, forgeY() - 50f + (i % 3) * 10f, 28f, 22f);
        }
        // Le soufflet, à gauche, qui se ferme et s'ouvre.
        float bx = width * 0.18f, by = forgeY() + 40f, open = 1f - blow;
        float h = MathUtils.lerp(70f, 220f, open);
        rect(batch, c("5a3018"), bx - 170f, by - h / 2f, 300f, h);
        for (int f = 0; f < 4; f++) rect(batch, c("3a1e0e"), bx - 150f + f * 70f, by - h / 2f, 12f, h);
        rect(batch, c("8a5236"), bx - 190f, by + h / 2f, 340f, 24f);
        rect(batch, c("8a5236"), bx - 190f, by - h / 2f - 24f, 340f, 24f);
        line(batch, c("6a6a7a"), bx + 130f, by, cx() - 300f, forgeY(), 26f);                  // la buse
        line(batch, c("6b3e1e"), bx - 190f, by + h / 2f + 12f, bx - 320f, by + h / 2f + 60f, 20f); // les poignées
        line(batch, c("6b3e1e"), bx - 190f, by - h / 2f - 12f, bx - 320f, by - h / 2f - 60f, 20f);
    }

    /** Coeur de la Montagne : des mineurs lâchent leurs pioches et s'enfuient. */
    private void drawMiners(Batch batch) {
        float width = worldWidth(), height = worldHeight(), ground = height * 0.16f;
        drawCave(batch);
        float scale = 26f, mw = miner.getRegionWidth() * scale, mh = miner.getRegionHeight() * scale;
        for (int i = 0; i < 3; i++) {
            float home = cx() + (i - 1f) * 440f, dir = i == 0 ? -1f : 1f;
            float run = Math.max(0f, time - FLEE - i * 0.08f);
            float x = home + dir * run * run * 900f, hop = run > 0f ? Math.abs(MathUtils.sin(run * 18f)) * 30f : 0f;
            float shiver = time < FLEE ? 4f * MathUtils.sin(time * 50f + i) : 0f;
            // La pioche, lâchée, qui tombe et reste au sol.
            float drop = Interpolation.bounceOut.apply(progress(DROP_PICKS, DROP_PICKS + 0.35f));
            float px = home + 120f, py = MathUtils.lerp(ground + mh * 0.5f, ground + 10f, drop);
            float pa = MathUtils.lerp(60f, 10f, drop);
            line(batch, c("6b3e1e"), px, py, px + MathUtils.cosDeg(pa) * 240f, py + MathUtils.sinDeg(pa) * 240f, 20f);
            float hx = px + MathUtils.cosDeg(pa) * 240f, hy = py + MathUtils.sinDeg(pa) * 240f;
            line(batch, c("9a9aa8"), hx + MathUtils.sinDeg(pa) * 80f, hy - MathUtils.cosDeg(pa) * 80f,
                hx - MathUtils.sinDeg(pa) * 80f, hy + MathUtils.cosDeg(pa) * 80f, 24f);
            glow(batch, x + shiver, ground + mh - 20f + hop, 320f, c("ffe066"), 0.45f);            // la lampe du casque
            batch.setColor(Color.WHITE);
            batch.draw(miner, dir < 0 ? x + shiver + mw / 2f : x + shiver - mw / 2f, ground + hop, dir < 0 ? -mw : mw, mh);
        }
    }
}
