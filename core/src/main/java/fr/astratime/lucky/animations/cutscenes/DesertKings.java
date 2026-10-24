package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;

/**
 * Les rois du Désert de Braise entrent en scène (choix d'Astra, 2026-10-24) :
 * <ul>
 *   <li>Émir des sables : les dunes au coucher du soleil, une caravane à l'horizon. Le premier chameau
 *   s'agenouille, l'Émir descend sous son ombrelle ; il déroule un contrat jusqu'à toi et le scelle.</li>
 *   <li>Pharaon maudit : un couloir de torches, des hiéroglyphes en symboles de machine. Le couvercle du
 *   sarcophage glisse, des mains bandées en sortent ; il se lève, yeux verts, crosse et fléau.</li>
 *   <li>Djinn du Mirage : une oasis qui ondule. Une lampe se frotte toute seule, sa fumée violette devient
 *   le Djinn ; derrière lui, l'oasis clignote : c'était un mirage.</li>
 *   <li>Ignar : le volcan gronde. Un oeil immense s'ouvre dans le cratère ; Ignar sort, ses ailes cachent
 *   le ciel, son or dévale les pentes, il rugit. Un peu plus long : c'est le dernier roi.</li>
 * </ul>
 */
public class DesertKings extends KingEntrance {

    /** Émir : le chameau s'agenouille, il descend, le contrat se déroule, le cachet. */
    static final float KNEEL = 0.2f, EMIR_DOWN = 0.7f, UNROLL = 3.5f, SEAL = 4.5f;
    /** Pharaon : le couvercle glisse, les mains, il se lève, ses yeux s'allument. */
    static final float LID = 0.2f, HANDS = 0.9f, RISE = 1.3f, GREEN_EYES = 3.6f;
    /** Djinn : la lampe se frotte (décor), la fumée, le Djinn, le mirage s'efface. */
    static final float RUB = 0.8f, SMOKE = 0.1f, DJINN_IN = 0.8f, MIRAGE = 3.6f;
    /** Ignar : l'oeil s'ouvre, il sort, ses ailes s'ouvrent, l'or dévale, il rugit. */
    static final float CRATER_EYE = 0.3f, EMERGE = 0.9f, WINGS = 2.0f, GOLD_SLIDE = 2.3f, ROAR = 3.6f;
    /** Ignar entre en scène un peu plus longtemps. */
    static final float DRAGON_COVER_START = PRELUDE + 6.4f, DRAGON_COVER_FULL = PRELUDE + 7.0f;

    private static final Color SUNSET_TOP = c("2a0a1e"), SUNSET_LOW = c("ff7a2a"), SAND = c("e8b86a"), SAND_DARK = c("b8843a");
    private static final Color GREEN = c("6aff6a"), GOLD = c("ffc93a"), PURPLE = c("a05aff"), LAVA = c("ff6a1e");
    private static final String[] CAMEL = {
        "...........oo..", "..........oyyo.", "...oo.....oyo..", "..oyyo...oyo...", ".oyyyyoooyyo...",
        "oyyyyyyyyyyo...", "oyyyyyyyyyo....", ".oyyyyyyyo.....", ".oyo..oyo......", ".oyo..oyo......", ".oo...oo......."};
    private static final String[] LAMP = {
        "....oo......", "...oggo.....", "..oggggo...o", "ooggggggoooo", "oggggggggo..", ".oggggggo...", "..oooooo...."};
    private static final String[] PALM = {
        ".gg...gg.", "g..g.g..g", "....g....", "...ggg...", "....b....", "....b....", "...b.....", "...b.....",
        "....b....", "....b...."};

    private final TextureRegion camel, lamp, palm, coin;
    private final TextureRegion[] glyphs;

    public DesertKings(CutsceneKit kit, EnemyKind kind) {
        super(kit, kind);
        camel = art("oy", new Color[] {c("3a2412"), c("a8784a")}, CAMEL);
        lamp = art("og", new Color[] {c("4a3008"), GOLD}, LAMP);
        palm = art("gb", new Color[] {c("3a9a3a"), c("8a5a2a")}, PALM);
        coin = load("hud/coin.png");
        glyphs = kind == EnemyKind.PHARAON
            ? new TextureRegion[] {load("symbols/2-cherry.png"), load("symbols/3-seven.png"), load("symbols/6-bell.png")}
            : new TextureRegion[0];
    }

    @Override protected float coverStart() { return kind == EnemyKind.DRAGON ? DRAGON_COVER_START : super.coverStart(); }
    @Override protected float coverFull()  { return kind == EnemyKind.DRAGON ? DRAGON_COVER_FULL : super.coverFull(); }

    @Override
    protected void stepPrelude(float delta) {
        if (kind == EnemyKind.DRAGON && at(0.4f)) rumble(1.4f, 4f);
        if (kind == EnemyKind.DJINN && at(RUB)) sparks(cx() + 200f, worldHeight() * 0.3f, 10, GOLD, 200f, 8f);
    }

    @Override
    protected void prelude(Batch batch) {
        switch (kind) {
            case EMIR -> drawDunes(batch, true);
            case PHARAON -> drawCorridor(batch);
            case DJINN -> drawOasis(batch, 1f, true);
            default -> drawVolcano(batch, 0f);
        }
    }

    @Override
    protected void step(float delta) {
        switch (kind) {
            case EMIR -> {
                if (at(KNEEL + 0.4f)) rumble(0.2f, 5f);
                if (at(SEAL)) {
                    rumble(0.2f, 8f);
                    sparks(cx() + 200f, worldHeight() * 0.12f, 30, c("e0283a"), 400f, 12f);
                }
            }
            case PHARAON -> {
                if (at(LID + 0.7f)) {
                    rumble(0.3f, 8f);
                    sparks(cx() + 300f, kingY(), 30, c("e8d49a"), 400f, 10f);
                }
                if (at(GREEN_EYES)) sparks(cx(), kingY() + 26f * KING_SCALE, 20, GREEN, 300f, 10f);
            }
            case DJINN -> {
                if (time >= SMOKE && time < DJINN_IN + 0.4f && random.nextFloat() < 0.8f) {
                    Particle puff = particle(cx() + 220f, worldHeight() * 0.3f, 80f + 40f * random.nextFloat(),
                        200f + 200f * random.nextFloat(), 1.0f, 60f + 80f * random.nextFloat());
                    puff.drag = 0.3f;
                    puff.color.set(PURPLE.r, PURPLE.g, PURPLE.b, 0.5f);
                    parts.add(puff);
                }
                if (at(MIRAGE)) sparks(cx(), worldHeight() * 0.5f, 30, c("fff2c0"), 600f, 10f);
            }
            default -> {
                if (at(CRATER_EYE)) rumble(0.4f, 6f);
                if (at(EMERGE)) rumble(1.0f, 10f);
                if (at(WINGS)) rumble(0.3f, 8f);
                if (time >= GOLD_SLIDE && time < GOLD_SLIDE + 1.2f && random.nextFloat() < 0.5f) {
                    float side = random.nextBoolean() ? -1f : 1f;
                    Particle piece = particle(cx() + side * 200f, worldHeight() * 0.42f, side < 0 ? 200f : -20f,
                        300f + 300f * random.nextFloat(), 1.6f, 4f);
                    piece.region = coin;
                    piece.gravity = 900f;
                    piece.spin = side * 400f;
                    parts.add(piece);
                }
                if (at(ROAR)) {
                    rumble(1.2f, 18f);
                    sparks(cx(), kingY() + 30f * KING_SCALE, 60, LAVA, 900f, 18f);
                }
                if (time >= ROAR && time < ROAR + 2f && random.nextFloat() < 0.9f) {     // les flammes montent
                    Particle flame = particle(worldWidth() * random.nextFloat(), -MARGIN, 80f + 20f * random.nextFloat(),
                        500f + 400f * random.nextFloat(), 1.0f, 60f + 60f * random.nextFloat());
                    flame.color.set(random.nextBoolean() ? LAVA : GOLD);
                    flame.drag = 0.6f;
                    parts.add(flame);
                }
            }
        }
    }

    @Override
    protected void draw(Batch batch) {
        switch (kind) {
            case EMIR -> drawEmir(batch);
            case PHARAON -> drawPharaoh(batch);
            case DJINN -> drawDjinn(batch);
            default -> drawDragon(batch);
        }
        drawParticles(batch, parts);
    }

    // -------------------------------------------------------------------------
    // Émir des sables
    // -------------------------------------------------------------------------

    /** Les dunes au coucher du soleil ; {@code caravan} : la file de chameaux à l'horizon. */
    private void drawDunes(Batch batch, boolean caravan) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, SUNSET_LOW, SUNSET_TOP);
        glow(batch, width * 0.5f, height * 0.42f, height * 0.6f, c("ffd23a"), 0.8f);
        for (int band = 0; band < 3; band++) {                            // trois rangées de dunes
            Color sand = band == 0 ? SAND_DARK : band == 1 ? c("d0a058") : SAND;
            float base = height * (0.42f - band * 0.12f);
            for (int x = -40; x < width + 40; x += 20) {
                float y = base + 40f * MathUtils.sin((x + band * 300) / 260f);
                rect(batch, sand, x, -MARGIN, 22f, y + MARGIN);
            }
        }
        if (!caravan) return;
        for (int k = 0; k < 6; k++) {
            float x = ((time * 90f + k * 150f) % (width + 300f)) - 150f;
            batch.setColor(c("2a0a1e"));
            sprite(batch, camel, x, height * 0.44f + 4f * MathUtils.sin(time * 6f + k), 6f, 0f);
        }
    }

    private void drawEmir(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        drawDunes(batch, false);
        // Le premier chameau s'agenouille.
        float kneel = Interpolation.pow2Out.apply(progress(KNEEL, KNEEL + 0.5f));
        batch.setColor(Color.WHITE);
        sprite(batch, camel, width * 0.2f, kingY() + 200f - 60f * kneel, 26f, 0f);
        float down = Interpolation.pow2Out.apply(progress(EMIR_DOWN, EMIR_DOWN + 0.6f));
        drawKing(batch, MathUtils.lerp(width * 0.24f, cx(), down), kingY() + 120f * (1f - down), KING_SCALE, 0f, down > 0f ? 1f : 0f);
        // Son ombrelle.
        float ux = MathUtils.lerp(width * 0.24f, cx(), down) - 230f, uy = kingY() + 120f * (1f - down) + 560f;
        if (down > 0f) {
            line(batch, c("6e4524"), ux, uy, ux + 60f, kingY() + 200f, 8f);
            for (int k = 0; k < 9; k++) {
                tmp.set(k % 2 == 0 ? c("e0283a") : GOLD);
                line(batch, tmp, ux, uy, ux + MathUtils.cosDeg(160f - k * 17.5f) * 200f, uy + MathUtils.sinDeg(160f - k * 17.5f) * 60f - 40f, 26f);
            }
        }
        // Le contrat qui roule jusqu'à toi, et son cachet rouge.
        float unroll = Interpolation.pow2Out.apply(progress(UNROLL, UNROLL + 0.7f));
        if (unroll > 0f) {
            float top = kingY() + 160f, bottom = MathUtils.lerp(top, -MARGIN, unroll);
            rect(batch, c("f0e2b8"), cx() + 120f, bottom, 200f, top - bottom);
            for (float y = top - 30f; y > bottom + 20f; y -= 34f) rect(batch, c("8a6a3a"), cx() + 140f, y, 160f, 6f);
            rect(batch, c("c8b080"), cx() + 110f, bottom - 20f, 220f, 40f);
            float stamp = progress(SEAL - 0.1f, SEAL);
            if (stamp > 0f) {
                batch.setColor(c("c8102e"));
                batch.draw(soft, cx() + 160f, height * 0.08f, 120f * stamp, 120f * stamp);
            }
        }
        flash(batch, c("e0283a"), SEAL, 0.2f, 0.25f);
    }

    // -------------------------------------------------------------------------
    // Pharaon maudit
    // -------------------------------------------------------------------------

    /** Le couloir du tombeau : des torches, des hiéroglyphes en symboles de machine. */
    private void drawCorridor(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        fill(batch, c("2a1a0e"), 1f);
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 10; col++) {
                float x = col * width / 9f + (row % 2) * 40f, y = row * height / 7f;
                rect(batch, c("3a2414"), x, y, width / 9f - 8f, height / 7f - 8f);
            }
        }
        for (int i = 0; i < 12 && glyphs.length > 0; i++) {               // les hiéroglyphes : Cerise, Sept, Cloche
            batch.setColor(0.85f, 0.7f, 0.45f, 0.55f);
            sprite(batch, glyphs[i % glyphs.length], width * (0.08f + 0.168f * (i % 6)), height * (i < 6 ? 0.78f : 0.6f), 0.8f, 0f);
        }
        for (int t = 0; t < 2; t++) {                                      // les torches
            float x = t == 0 ? width * 0.1f : width * 0.9f, y = height * 0.5f;
            rect(batch, c("6e4524"), x - 10f, y - 120f, 20f, 120f);
            float flicker = pulse(17f + t, 0.75f);
            glow(batch, x, y + 20f, 500f * flicker, c("ff9a3a"), 0.6f);
            glow(batch, x, y + 10f, 90f * flicker, c("ffe8a0"), 0.9f);
        }
        rect(batch, c("1a0e06"), -MARGIN, -MARGIN, width + 2f * MARGIN, height * 0.18f + MARGIN);
    }

    private void drawPharaoh(Batch batch) {
        drawCorridor(batch);
        float base = kingY() - 40f, width = 520f;
        // Il se lève, derrière la cuve.
        float rise = Interpolation.pow2Out.apply(progress(RISE, RISE + 0.8f));
        if (rise > 0f) {
            pose(0f, 0f, 0f, 0f);
            drawKing(batch, cx(), MathUtils.lerp(base - 400f, base + 60f, rise), KING_SCALE, 0f, 1f);
        }
        // La cuve du sarcophage, or et bleu.
        rect(batch, c("c4891e"), cx() - width / 2f, -MARGIN, width, base + 160f + MARGIN);
        for (int k = 0; k < 6; k++) rect(batch, k % 2 == 0 ? c("1a3a8e") : c("ffd54a"), cx() - width / 2f, base + 10f + k * 24f, width, 12f);
        // Des mains bandées sortent sur le rebord.
        float hands = progress(HANDS, HANDS + 0.3f) * (1f - progress(RISE + 0.2f, RISE + 0.5f));
        if (hands > 0f) {
            rect(batch, c("e0d4b4"), cx() - 200f, base + 160f, 90f, 60f * hands);
            rect(batch, c("e0d4b4"), cx() + 110f, base + 160f, 90f, 60f * hands);
        }
        // Le couvercle glisse sur le côté.
        float lid = Interpolation.pow2Out.apply(progress(LID, LID + 0.7f));
        rect(batch, c("e8b84a"), cx() - width / 2f + lid * width * 0.9f, base + 160f, width, 40f);
        // Crosse et fléau, et ses yeux verts.
        if (rise >= 1f) {
            float y0 = base + 60f + 120f;
            line(batch, c("ffd54a"), cx() - 240f, y0, cx() - 200f, y0 + 340f, 14f);
            line(batch, c("1a3a8e"), cx() - 200f, y0 + 340f, cx() - 260f, y0 + 380f, 14f);
            line(batch, c("ffd54a"), cx() + 240f, y0, cx() + 220f, y0 + 300f, 12f);
            for (int k = 0; k < 3; k++) line(batch, c("1a3a8e"), cx() + 220f, y0 + 300f, cx() + 260f + k * 20f, y0 + 220f, 8f);
        }
        float eyes = progress(GREEN_EYES, GREEN_EYES + 0.3f);
        if (eyes > 0f) {
            float ey = base + 60f + (42f - 14.5f) * KING_SCALE;
            glow(batch, cx() - 3.5f * KING_SCALE, ey, 160f, GREEN, eyes);
            glow(batch, cx() + 3.5f * KING_SCALE, ey, 160f, GREEN, eyes);
        }
    }

    // -------------------------------------------------------------------------
    // Djinn du Mirage
    // -------------------------------------------------------------------------

    /** L'oasis qui ondule dans la chaleur ({@code show} : 1 pleine, 0 effacée) ; la lampe sur son rocher. */
    private void drawOasis(Batch batch, float show, boolean lampOnly) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, c("f0c878"), c("4aa8e8"));
        rect(batch, SAND, -MARGIN, -MARGIN, width + 2f * MARGIN, height * 0.35f + MARGIN);
        for (int k = 0; k < 18; k++) rect(batch, c("c89858"), (k * 173f) % width, (k * 61f) % (height * 0.3f), 60f, 6f); // la terre craquelée
        if (show > 0f) {
            float wobble = 10f * MathUtils.sin(time * 4f);
            batch.setColor(c("2a8ad8").r, c("2a8ad8").g, c("2a8ad8").b, show);
            batch.draw(soft, width * 0.25f + wobble, height * 0.2f, width * 0.5f, height * 0.14f);
            for (int p = 0; p < 4; p++) {
                batch.setColor(1f, 1f, 1f, show);
                sprite(batch, palm, width * (0.18f + 0.22f * p) + wobble * (p % 2 == 0 ? 1 : -1), height * 0.42f, 26f, 4f * MathUtils.sin(time * 2f + p));
            }
        }
        rect(batch, c("8a7a6a"), cx() + 120f, height * 0.18f, 200f, 90f);       // le rocher
        float rub = lampOnly ? MathUtils.sin(time * 30f) * 6f * progress(RUB, RUB + 0.2f) : 0f;
        batch.setColor(Color.WHITE);
        sprite(batch, lamp, cx() + 220f + rub, height * 0.3f, 12f, rub);
    }

    private void drawDjinn(Batch batch) {
        // Après son entrée, l'oasis clignote et s'efface : c'était un mirage.
        float blink = time < MIRAGE ? 1f : (MathUtils.sin(time * 40f) > 0f ? 1f : 0.2f) * (1f - progress(MIRAGE, MIRAGE + 0.8f));
        drawOasis(batch, blink, false);
        float appear = progress(DJINN_IN, DJINN_IN + 0.6f);
        float float_ = 14f * MathUtils.sin(time * 2.5f);
        drawKing(batch, cx(), kingY() + 40f + float_, KING_SCALE, 0f, appear);
        if (appear < 1f && time >= SMOKE) glow(batch, cx(), kingY() + 260f, 700f, PURPLE, 0.5f * (1f - appear) * progress(SMOKE, DJINN_IN));
    }

    // -------------------------------------------------------------------------
    // Ignar, le Dragon du Volcan
    // -------------------------------------------------------------------------

    /** Le volcan qui gronde, la lave qui coule ; {@code eye} : l'oeil qui s'ouvre dans le cratère. */
    private void drawVolcano(Batch batch, float eye) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, c("8a2a1e"), c("1a0606"));
        float peak = height * 0.62f;
        for (int x = -40; x < width + 40; x += 12) {                     // les pentes
            float y = peak - Math.abs(x - width / 2f) * 0.9f;
            if (Math.abs(x - width / 2f) < 140f) y = peak;
            rect(batch, x < width / 2f ? c("3a1e18") : c("2a1410"), x, -MARGIN, 14f, Math.max(0f, y) + MARGIN);
        }
        for (int s = 0; s < 3; s++) {                                     // coulées de lave
            float x0 = width / 2f + (s - 1) * 90f;
            for (int k = 0; k < 12; k++) {
                float y = peak - k * 40f, x = x0 + (s - 1) * k * 30f + 12f * MathUtils.sin(time * 3f + k + s);
                glow(batch, x, y, 80f, LAVA, 0.6f);
                rect(batch, c("ffb84a"), x - 8f, y - 20f, 16f, 40f);
            }
        }
        glow(batch, width / 2f, peak, 420f * pulse(5f, 0.7f), LAVA, 0.7f);
        if (eye > 0f) {                                                   // l'oeil immense, fendu d'or
            batch.setColor(GOLD);
            batch.draw(soft, width / 2f - 180f, peak - 40f * eye, 360f, 80f * eye);
            rect(batch, c("1a0606"), width / 2f - 10f, peak - 35f * eye, 20f, 70f * eye);
        }
    }

    private void drawDragon(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        drawVolcano(batch, progress(CRATER_EYE, CRATER_EYE + 0.3f) * (1f - progress(EMERGE, EMERGE + 0.3f)));
        // Ses ailes s'ouvrent et cachent le ciel.
        float wings = Interpolation.pow2Out.apply(progress(WINGS, WINGS + 0.5f));
        if (wings > 0f) {
            float y = kingY() + 30f * KING_SCALE;
            for (int side = -1; side <= 1; side += 2) {
                float tipX = cx() + side * width * 0.62f * wings, tipY = height + MARGIN;
                for (int k = 0; k < 5; k++) {
                    float ex = cx() + side * width * (0.15f + 0.1f * k) * wings, ey = y - 200f + k * 30f;
                    line(batch, c("3a0a08"), cx() + side * 80f, y, tipX, tipY - k * 80f * wings, 18f);
                    line(batch, c("5a1410"), tipX, tipY - k * 80f * wings, ex, ey, 40f * wings);
                }
            }
        }
        float rise = Interpolation.pow2Out.apply(progress(EMERGE, EMERGE + 1.0f));
        float roar = gesture(ROAR - 0.2f, ROAR + 1.4f);
        pose(0f, 30f * roar, 0f, 0.06f * roar);
        drawKing(batch, cx(), MathUtils.lerp(height * 0.62f - 300f, kingY(), rise), KING_SCALE * 1.15f, 0f, rise > 0f ? 1f : 0f);
        if (roar > 0f) glow(batch, cx(), kingY() + 18f * KING_SCALE, 900f * roar, LAVA, 0.6f * roar);
        flash(batch, LAVA, ROAR, 0.4f, 0.35f);
    }
}
