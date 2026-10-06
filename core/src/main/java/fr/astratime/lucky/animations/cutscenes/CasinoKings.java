package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.i18n.Lang;

/**
 * Les rois du Casino Englouti entrent en scène, sous la mer :
 * <ul>
 *   <li>Sirène du Bar : un verre glisse seul sur le comptoir. Des bulles montent, un chant ; les verres
 *   dansent ; elle apparaît derrière le comptoir. Elle souffle une bulle, une carte flotte dedans.</li>
 *   <li>Jackpot Vivant : un joueur fantôme tire un levier ; rien. Les machines s'allument, JACKPOT clignote
 *   partout ; la plus grande se lève et marche. Elle te crache des pièces et se met en garde.</li>
 *   <li>Grand Requin Banquier : une petite banque sous l'eau, le coffre ouvert. Des pièces coulent, une ombre
 *   tourne autour ; le requin surgit. Il tamponne « REFUSÉ » sur un chèque et sourit.</li>
 *   <li>Kraken : l'eau devient noire, des cartes coulent. La table VIP se fend, huit bras en sortent avec
 *   chacun une carte ; un œil s'ouvre dessous. Les bras battent les cartes et les étalent en éventail,
 *   un Joker au centre.</li>
 * </ul>
 */
public class CasinoKings extends KingEntrance {

    /** Sirène : le chant commence, les verres dansent, elle apparaît. */
    static final float SONG = 0.5f, DANCE = 0.8f, SIREN = 2.0f;
    /** Jackpot Vivant : les machines s'allument une à une, JACKPOT clignote, la grande machine marche. */
    static final float LIGHTS = 0.3f, LIGHT_STEP = 0.13f, BLINK = 1.3f, WALK = 2.1f, WALK_END = 3.0f;
    /** Requin : l'ombre tourne, puis il surgit. */
    static final float CIRCLE = 0.4f, LUNGE = 2.1f;
    /** Kraken : la table se fend, les bras sortent un à un, l'œil s'ouvre. */
    static final float CRACK = 0.5f, ARMS = 0.8f, ARM_STEP = 0.18f, EYE = 2.45f;

    /** Le décor. Sirène : le verre glisse puis s'arrête. Jackpot : le fantôme tire le levier ; rien ; il s'efface. */
    static final float SLIDE_END = 1.4f, PULL = 0.55f, NOTHING = 1.1f, GHOST_FADE = 1.3f;
    /** Le geste. Sirène : la bulle. Jackpot : les pièces, la garde. Requin : le tampon, le sourire. */
    static final float BUBBLE_END = 4.3f, SPIT = 3.6f, GUARD = 4.3f, STAMP = 3.9f, SMILE = 4.5f;
    /** Kraken : les cartes passent de bras en bras, puis l'éventail, puis le Joker. */
    static final float FAN = 4.3f, FANNED = 4.8f, JOKER = 4.85f;

    private static final int ARM_COUNT = 8;
    private static final Color DEEP = c("03101a"), SHALLOW = c("0c3a4a"), FOAM = c("bfeeff"), GOLD = c("ffc93a");
    private static final Color TENTACLE = c("8a2a4a"), TENTACLE_LIGHT = c("c25a7a");

    private final BitmapFont font;
    private final TextureRegion glass, note, machine, shark, coin, disc, ghost, safe;
    /** Le centre et l'angle de la carte au bout de chaque bras, pour l'éventail. */
    private final float[] cardX = new float[ARM_COUNT], cardY = new float[ARM_COUNT], cardA = new float[ARM_COUNT];
    private final Array<Particle> bubbles = new Array<>(false, 128);
    private final Array<Particle> coins   = new Array<>(false, 32);
    private float bubbleDebt;

    public CasinoKings(CutsceneKit kit, EnemyKind kind) {
        super(kit, kind);
        font = kit.font();
        Color[] glassColors = {c("0a1418"), c("d8f4ff"), c("ff4fa0"), c("ffe066")};
        glass = art("owpy", glassColors,
            "owwwwwwwo",
            ".oppppyo.",
            "..oppppo.",
            "...oppo..",
            "....oo...",
            "....wo...",
            "....wo...",
            "...owwo..",
            "..owwwwo.");
        note = art("w", new Color[] {Color.WHITE},
            "..www",
            "..w.w",
            "..w.w",
            "..w..",
            "www..",
            "www..");
        Color[] slotColors = {c("0a0808"), c("ffc93a"), c("b8801f"), c("f4ecdc"), c("e0283a")};
        machine = art("ogdwr", slotColors,
            "..oooooooooo..",
            ".oggggggggggo.",
            "oggggggggggggo",
            "ogoooooooooogo",
            "ogowwowwowwogo",
            "ogowrowrowrogo",
            "ogowwowwowwogo",
            "ogoooooooooogo",
            "oggggggggggggo",
            "ogggddddddgggo",
            "oggggggggggggo",
            "oddddddddddddo",
            "oooooooooooooo");
        shark = art("s", new Color[] {Color.WHITE},
            "..........s...........",
            ".........ss...........",
            "........sss...........",
            "......sssssssss......s",
            "...sssssssssssssss..ss",
            ".sssssssssssssssssssss",
            "ssssssssssssssssssss.s",
            "..ssssssssssss.......s",
            ".....ss...ss..........");
        coin = load("hud/coin.png");
        Pixmap circle = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        circle.setColor(Color.WHITE);
        circle.fillCircle(8, 8, 7);
        disc = region(circle, false);
        ghost = art("o", new Color[] {Color.WHITE},
            "..oooo..",
            ".oooooo.",
            ".o.oo.o.",
            ".oooooo.",
            "..oooo..",
            ".oooooo.",
            "oooooooo",
            "oooooooo",
            "oooooooo",
            "oooooooo",
            "o.o..o.o");
        safe = art("ogdk", new Color[] {c("0a0808"), c("9a9aa8"), c("5a5a6a"), c("2a2a34")},
            "oooooooooooo",
            "ogggggggggdo",
            "ogkkkkkkkkdo",
            "ogkkkkkkkkdo",
            "ogkkkkkkkkdo",
            "ogkkkkkkkkdo",
            "ogkkkkkkkkdo",
            "ogkkkkkkkkdo",
            "ogddddddddddo",
            "oooooooooooo");
    }

    @Override
    protected void restart() {
        bubbles.clear();
        coins.clear();
        bubbleDebt = 0f;
        if (kind == EnemyKind.REQUIN_BANQUIER) {
            for (int i = 0; i < 14; i++) {                  // des pièces qui coulent lentement
                Particle piece = particle(worldWidth() * (0.08f + 0.84f * random.nextFloat()),
                    worldHeight() + 80f + 900f * random.nextFloat(), 270f, 120f + 80f * random.nextFloat(), 99f,
                    0.8f + 0.5f * random.nextFloat());
                piece.spin = random.nextFloat() * 10f;
                coins.add(piece);
            }
        }
    }

    @Override
    protected void stepPrelude(float delta) {
        if (kind == EnemyKind.JACKPOT_VIVANT && at(PULL + 0.3f)) rumble(0.1f, 3f);
        if (kind == EnemyKind.SIRENE && at(SLIDE_END)) sparks(cx(), worldHeight() * 0.3f + 60f, 10, FOAM, 200f, 8f);
    }

    @Override
    protected void prelude(Batch batch) {
        drawWater(batch);
        switch (kind) {
            case SIRENE -> drawSlidingGlass(batch);
            case JACKPOT_VIVANT -> drawGhost(batch);
            case REQUIN_BANQUIER -> drawBank(batch);
            default -> drawSinkingCards(batch);
        }
    }

    @Override
    protected void step(float delta) {
        float width = worldWidth();
        bubbleDebt += (kind == EnemyKind.SIRENE ? 45f : 18f) * delta;
        while (bubbleDebt >= 1f) {
            bubbleDebt -= 1f;
            Particle bubble = particle(width * random.nextFloat(), -30f, 90f, 120f + 160f * random.nextFloat(), 99f,
                6f + 14f * random.nextFloat());
            bubble.spin = random.nextFloat() * 10f;
            bubbles.add(bubble);
        }
        for (int i = bubbles.size - 1; i >= 0; i--) {
            Particle bubble = bubbles.get(i);
            bubble.age += delta;
            bubble.x += 30f * MathUtils.sin(bubble.age * 3f + bubble.spin) * delta;
            bubble.y += bubble.vy * delta;
            if (bubble.y > worldHeight() + 40f) bubbles.removeIndex(i);
        }
        for (Particle piece : coins) {
            piece.age += delta;
            piece.y += piece.vy * delta;
        }
        switch (kind) {
            case SIRENE -> {
                if (at(SIREN)) sparks(cx(), kingY() + 300f, 30, c("ff9ad0"), 500f, 14f);
            }
            case JACKPOT_VIVANT -> {
                for (int i = 0; i < 4; i++) if (at(WALK + 0.15f + i * 0.22f)) rumble(0.15f, 7f);
                if (at(WALK_END)) sparks(cx(), kingY() + 300f, 40, GOLD, 800f, 14f);
                if (at(SPIT)) {                                  // la machine te crache des pièces dessus
                    rumble(0.3f, 9f);
                    for (int i = 0; i < 40; i++) {
                        Particle piece = particle(cx(), kingY() + 22f * KING_SCALE, 360f * random.nextFloat(),
                            600f + 900f * random.nextFloat(), 1.0f + 0.4f * random.nextFloat(), 1.5f + 2.5f * random.nextFloat());
                        piece.gravity = 600f;
                        piece.region = coin;
                        piece.spin = (random.nextFloat() - 0.5f) * 900f;
                        parts.add(piece);
                    }
                }
                if (at(GUARD)) rumble(0.2f, 8f);
            }
            case REQUIN_BANQUIER -> {
                if (at(STAMP)) {
                    rumble(0.25f, 10f);
                    sparks(cx(), chequeY(), 20, c("ff4a3a"), 400f, 10f);
                }
                if (at(LUNGE)) {
                    rumble(0.4f, 14f);
                    for (int i = 0; i < 40; i++) {
                        Particle bubble = particle(cx(), worldHeight() * 0.45f, 360f * random.nextFloat(),
                            300f + 600f * random.nextFloat(), 0.8f, 10f + 20f * random.nextFloat());
                        bubble.drag = 0.1f;
                        bubble.color.set(FOAM);
                        parts.add(bubble);
                    }
                }
            }
            default -> {
                if (at(CRACK)) rumble(0.35f, 12f);
                for (int i = 0; i < ARM_COUNT; i++) if (at(ARMS + i * ARM_STEP)) rumble(0.1f, 5f);
                if (at(EYE)) rumble(0.5f, 10f);
                if (at(JOKER)) sparks(cx(), fanY(), 40, GOLD, 700f, 14f);
            }
        }
    }

    @Override
    protected void draw(Batch batch) {
        drawWater(batch);
        switch (kind) {
            case SIRENE -> drawSiren(batch);
            case JACKPOT_VIVANT -> drawJackpot(batch);
            case REQUIN_BANQUIER -> drawShark(batch);
            default -> drawKraken(batch);
        }
        // Bulles devant tout.
        for (Particle bubble : bubbles) {
            batch.setColor(FOAM.r, FOAM.g, FOAM.b, 0.55f);
            batch.draw(ring, bubble.x - bubble.size, bubble.y - bubble.size, bubble.size * 2f, bubble.size * 2f);
        }
        drawParticles(batch, parts);
    }

    /** Sous la mer : dégradé bleu-vert, rayons de lumière qui tombent de la surface. */
    private void drawWater(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, DEEP, SHALLOW);
        additive(batch);
        for (int i = 0; i < 7; i++) {
            float x = width * (0.1f + i * 0.14f) + 40f * MathUtils.sin(time * 0.7f + i);
            batch.setColor(0.6f, 0.95f, 1f, 0.07f + 0.04f * MathUtils.sin(time * 1.3f + i * 2f));
            batch.draw(trail, x, height + 40f, 0f, 60f, height * 1.3f, 120f, 1f, 1f, 250f + i * 2f);
        }
        normal(batch);
        rect(batch, c("041820"), -MARGIN, -MARGIN, width + 2f * MARGIN, height * 0.12f + MARGIN);
    }

    // -------------------------------------------------------------------------
    // Sirène du Bar
    // -------------------------------------------------------------------------

    private void drawSiren(Batch batch) {
        float width = worldWidth(), height = worldHeight(), counter = height * 0.3f;
        // Les notes du chant, qui montent en ondulant.
        for (int i = 0; i < 10; i++) {
            float start = SONG + i * 0.28f;
            if (time < start) continue;
            float k = (time - start) / 2.2f;
            if (k > 1f) continue;
            float x = width * (0.15f + 0.07f * i) + 40f * MathUtils.sin(time * 4f + i);
            float y = counter + k * height * 0.6f;
            batch.setColor(1f, 0.85f, 0.95f, 1f - k);
            sprite(batch, note, x, y, 9f, 10f * MathUtils.sin(time * 5f + i));
        }
        float appear = progress(SIREN, SIREN + 0.5f);
        if (time >= SIREN) {
            float blow = gesture(GESTURE, BUBBLE_END);
            pose(0f, 0f, -3f * blow, 0.02f * blow);
            glow(batch, cx(), counter + 350f, 1100f, c("ff9ad0"), 0.35f * appear);
            drawKing(batch, cx(), counter - 60f, KING_SCALE, 1f - appear, appear);
        }
        if (time >= GESTURE) {                                      // la bulle soufflée, une carte dedans
            float grow = Interpolation.pow2Out.apply(progress(GESTURE, BUBBLE_END));
            float rise = Math.max(0f, time - BUBBLE_END);
            float size = 40f + 190f * grow;
            float bx = cx() + 20f + 240f * grow + 60f * MathUtils.sin(rise * 2f);
            float by = counter - 60f + 22f * KING_SCALE + 120f * grow + rise * 140f;
            glow(batch, bx, by, size * 2.6f, FOAM, 0.25f);
            drawCard(batch, bx, by, 15f * MathUtils.sin(time * 2f), 0.9f * grow, c("e0283a"));
            batch.setColor(FOAM.r, FOAM.g, FOAM.b, 0.8f);
            batch.draw(ring, bx - size, by - size, size * 2f, size * 2f);
            glow(batch, bx - size * 0.4f, by + size * 0.4f, size * 0.4f, Color.WHITE, 0.6f);
        }
        // Le comptoir englouti.
        rect(batch, c("1a2a2e"), -MARGIN, -MARGIN, width + 2f * MARGIN, counter + MARGIN);
        rect(batch, c("3a5a5e"), -MARGIN, counter - 24f, width + 2f * MARGIN, 24f);
        // Les verres qui dansent.
        for (int i = 0; i < 6; i++) {
            float x = width * (0.12f + i * 0.152f);
            if (Math.abs(x - cx()) < 160f) continue;
            float dance = progress(DANCE, DANCE + 0.4f);
            float hop = Math.abs(MathUtils.sin(time * 6f + i)) * 60f * dance;
            float angle = 14f * MathUtils.sin(time * 6f + i * 1.7f) * dance;
            batch.setColor(Color.WHITE);
            float scale = 19f;
            batch.draw(glass, x - glass.getRegionWidth() * scale / 2f, counter + hop, glass.getRegionWidth() * scale / 2f,
                0f, glass.getRegionWidth() * scale, glass.getRegionHeight() * scale, 1f, 1f, angle);
        }
    }

    // -------------------------------------------------------------------------
    // Jackpot Vivant
    // -------------------------------------------------------------------------

    private void drawJackpot(Batch batch) {
        float width = worldWidth(), height = worldHeight(), floor = height * 0.3f;
        // La rangée de machines au fond, qui s'allument une à une.
        int count = 7;
        for (int i = 0; i < count; i++) {
            float x = width * (0.08f + i * 0.14f);
            boolean center = i == count / 2;
            if (center && time >= WALK) continue;                  // la grande s'est levée
            float lit = progress(LIGHTS + i * LIGHT_STEP, LIGHTS + i * LIGHT_STEP + 0.1f);
            float blink = time >= BLINK ? (((int) (time * 10f) + i) % 2 == 0 ? 1f : 0.6f) : 1f;
            float scale = center ? 22f : 14f;
            if (lit > 0f) glow(batch, x, floor + 120f, 380f, GOLD, 0.3f * lit * blink);
            float light = 0.35f + 0.65f * lit * blink;
            batch.setColor(light, light, light, 1f);
            batch.draw(machine, x - machine.getRegionWidth() * scale / 2f, floor, machine.getRegionWidth() * scale,
                machine.getRegionHeight() * scale);
        }
        // JACKPOT clignote partout.
        if (time >= BLINK && time < WALK + 0.5f) {
            font.getData().setScale(3f);
            java.util.Random spots = new java.util.Random((int) (time * 6f));
            for (int i = 0; i < 4; i++) {
                float x = width * (0.15f + 0.7f * spots.nextFloat()), y = height * (0.6f + 0.3f * spots.nextFloat());
                caption(batch, font, Lang.t("JACKPOT"), x, y, 1f - progress(WALK, WALK + 0.5f));
            }
            font.getData().setScale(1f);
        }
        rect(batch, c("0a1a20"), -MARGIN, -MARGIN, width + 2f * MARGIN, floor + MARGIN);
        // La grande machine se lève et marche vers toi, pas à pas.
        if (time >= WALK) {
            float k = progress(WALK, WALK_END);
            float steps = Interpolation.linear.apply(k);
            float scale = MathUtils.lerp(7f, KING_SCALE, steps);
            float bob = Math.abs(MathUtils.sin(k * MathUtils.PI * 4f)) * 30f * (1f - k);
            float y = MathUtils.lerp(floor, kingY(), steps) + bob;
            float spit = gesture(SPIT - 0.15f, SPIT + 0.25f);
            float guard = Interpolation.swingOut.apply(progress(GUARD, GUARD + 0.3f));
            pose(0f, -30f * guard - 20f * spit, 0f, 0.05f * spit - 0.07f * guard);   // il crache, puis se met en garde
            glow(batch, cx(), y + 25f * scale, 70f * scale, GOLD, 0.4f + 0.3f * guard * pulse(8f, 0.5f));
            drawKing(batch, cx(), y, scale, 0f, 1f);
        }
    }

    // -------------------------------------------------------------------------
    // Grand Requin Banquier
    // -------------------------------------------------------------------------

    private void drawShark(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        for (Particle piece : coins) {                              // les pièces qui coulent
            float x = piece.x + 30f * MathUtils.sin(piece.age * 2f + piece.spin);
            float squash = Math.abs(MathUtils.cos(piece.age * 3f + piece.spin));
            float w = coin.getRegionWidth() * piece.size, h = coin.getRegionHeight() * piece.size;
            glow(batch, x, piece.y, w * 1.6f, GOLD, 0.25f);
            batch.setColor(Color.WHITE);
            batch.draw(coin, x - w * squash / 2f, piece.y - h / 2f, w * Math.max(0.15f, squash), h);
        }
        // L'ombre qui tourne autour.
        if (time >= CIRCLE && time < LUNGE) {
            float k = (time - CIRCLE) * 2.4f;
            float x = cx() + MathUtils.cos(k) * width * 0.34f, y = height * 0.5f + MathUtils.sin(k) * height * 0.14f;
            boolean left = -MathUtils.sin(k) < 0f;                   // sens de la nage
            float scale = 16f * (1f - 0.25f * MathUtils.sin(k));
            float w = shark.getRegionWidth() * scale, h = shark.getRegionHeight() * scale;
            batch.setColor(0f, 0.04f, 0.06f, 0.6f * progress(CIRCLE, CIRCLE + 0.4f));
            batch.draw(shark, left ? x + w / 2f : x - w / 2f, y - h / 2f, left ? -w : w, h);
        }
        // Il surgit : cravate et dents.
        if (time >= LUNGE) {
            float k = Interpolation.swingOut.apply(progress(LUNGE, LUNGE + 0.3f));
            float scale = MathUtils.lerp(2f, KING_SCALE, k);
            glow(batch, cx(), kingY() + 280f, 1000f, FOAM, 0.25f);
            float press = gesture(STAMP - 0.3f, STAMP + 0.2f);
            pose(0f, -30f * press, 0f, -0.03f * press);
            drawKing(batch, cx(), MathUtils.lerp(height * 0.45f, kingY(), k), scale, 0f, 1f);
            if (time >= SMILE) {                                    // le sourire : ses dents brillent
                float smile = progress(SMILE, SMILE + 0.3f);
                for (int i = 0; i < 4; i++) {
                    float t = MathUtils.clamp((time - SMILE - i * 0.1f) / 0.3f, 0f, 1f);
                    glow(batch, cx() + (i - 1.5f) * 2.5f * KING_SCALE, kingY() + 24f * KING_SCALE, 120f * MathUtils.sin(t
                        * MathUtils.PI), Color.WHITE, smile);
                }
            }
        }
        if (time >= GESTURE) drawCheque(batch);
        flash(batch, FOAM, LUNGE, 0.2f, 0.4f);
    }

    // -------------------------------------------------------------------------
    // Kraken
    // -------------------------------------------------------------------------

    private void drawKraken(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        float tableY = height * 0.2f, rx = width * 0.42f, ry = height * 0.12f;
        // L'œil s'ouvre sous la table, dans la fente.
        float open = Interpolation.pow2Out.apply(progress(EYE, EYE + 0.4f));
        // La table VIP : rebord doré, tapis vert, la fente qui s'ouvre.
        ellipse(batch, c("b8801f"), cx(), tableY, rx + 24f, ry + 14f);
        ellipse(batch, c("1f6e3a"), cx(), tableY, rx, ry);
        float crack = progress(CRACK, CRACK + 0.2f);
        if (crack > 0f) {
            float gap = 10f + 40f * progress(ARMS, EYE) + 60f * open;
            batch.setColor(c("020608"));
            for (int k = 0; k < 12; k++) {
                float x0 = cx() - rx * 0.9f * crack + k * rx * 1.8f * crack / 12f;
                float wobble = (k % 2 == 0 ? 1f : -1f) * 10f;
                batch.draw(pixel, x0, tableY - gap / 2f + wobble, rx * 1.8f * crack / 12f + 2f, gap);
            }
        }
        if (open > 0f) {
            float eyeW = 300f, eyeH = 130f * open;
            glow(batch, cx(), tableY, 800f, c("ffe066"), 0.4f * open);
            ellipse(batch, c("f4ecdc"), cx(), tableY, eyeW, eyeH);
            ellipse(batch, c("ffc93a"), cx(), tableY, eyeH * 0.95f, eyeH * 0.95f);
            ellipse(batch, c("0a0808"), cx(), tableY, eyeH * 0.18f, eyeH * 0.85f);
        }
        // Les huit bras, chacun une carte au bout.
        for (int i = 0; i < ARM_COUNT; i++) {
            float start = ARMS + i * ARM_STEP;
            if (time < start) continue;
            float rise = Interpolation.pow2Out.apply(progress(start, start + 0.45f));
            float baseX = cx() + (i - (ARM_COUNT - 1) / 2f) * rx * 1.6f / ARM_COUNT;
            float side = i < ARM_COUNT / 2 ? -1f : 1f;
            float x = baseX, y = tableY, angle = 90f - side * 8f;
            int segments = 16;
            float segment = 34f * rise;
            for (int s = 0; s < segments; s++) {
                float radius = MathUtils.lerp(40f, 12f, s / (float) segments);
                angle += side * (6f + 4f * MathUtils.sin(time * 3f + i + s * 0.4f)) * (s / (float) segments);
                x += MathUtils.cosDeg(angle) * segment;
                y += MathUtils.sinDeg(angle) * segment;
                batch.setColor(s % 3 == 0 ? TENTACLE_LIGHT : TENTACLE);
                batch.draw(disc, x - radius, y - radius, radius * 2f, radius * 2f);
            }
            if (rise > 0.6f) {                                       // la carte au bout du bras (battue après le geste)
                float cardAngle = angle - 90f;
                if (time < GESTURE) {
                    batch.setColor(c("0a0808"));
                    batch.draw(pixel, x - 34f, y - 2f, 34f, 0f, 68f, 96f, 1f, 1f, cardAngle);
                    batch.setColor(c("f4ecdc"));
                    batch.draw(pixel, x - 30f, y + 2f, 30f, -4f, 60f, 88f, 1f, 1f, cardAngle);
                }
                // Le logo au centre de la carte : on tourne sa position avec la carte, puis le losange sur lui-même.
                float logoX = x - MathUtils.sinDeg(cardAngle) * 46f;
                float logoY = y - 2f + MathUtils.cosDeg(cardAngle) * 46f;
                cardX[i] = logoX;
                cardY[i] = logoY;
                cardA[i] = cardAngle;
                if (time < GESTURE) {
                    batch.setColor(i % 2 == 0 ? c("e0283a") : c("0a0808"));
                    batch.draw(pixel, logoX - 9f, logoY - 9f, 9f, 9f, 18f, 18f, 1f, 1f, cardAngle + 45f);
                }
            }
        }
        if (time >= GESTURE) drawShuffle(batch);
        flash(batch, c("ffe066"), EYE, 0.2f, 0.35f);
    }

    /**
     * Les bras battent les cartes : elles sautent de bras en bras, puis se rangent
     * en éventail au-dessus de la table ; un Joker monte au centre.
     */
    private void drawShuffle(Batch batch) {
        float fan = Interpolation.pow2Out.apply(progress(FAN, FANNED));
        int hop = (int) ((time - GESTURE) * 7f);
        float between = ((time - GESTURE) * 7f) % 1f;
        for (int i = 0; i < ARM_COUNT; i++) {
            // Pendant le battage, chaque carte passe au bras suivant (un bras sur deux dans l'autre sens).
            int from = Math.floorMod(i + (i % 2 == 0 ? hop : -hop), ARM_COUNT);
            int to = Math.floorMod(i + (i % 2 == 0 ? hop + 1 : -hop - 1), ARM_COUNT);
            float e = time < FAN ? Interpolation.pow2.apply(between) : 1f;
            float sx = MathUtils.lerp(cardX[from], cardX[to], e), sy = MathUtils.lerp(cardY[from], cardY[to], e)
                + 80f * MathUtils.sin(e * MathUtils.PI);
            float sa = MathUtils.lerp(cardA[from], cardA[to], e);
            int slot = i < ARM_COUNT / 2 ? i : i + 1;                  // la place du milieu est pour le Joker
            float fanAngle = (slot - ARM_COUNT / 2f) * 11f;
            float fx = cx() - MathUtils.sinDeg(fanAngle) * 420f, fy = fanY() - 420f + MathUtils.cosDeg(fanAngle) * 420f;
            drawCard(batch, MathUtils.lerp(sx, fx, fan), MathUtils.lerp(sy, fy, fan), MathUtils.lerp(sa, fanAngle, fan),
                MathUtils.lerp(1f, 1.5f, fan), i % 2 == 0 ? c("e0283a") : c("0a0808"));
        }
        if (time >= JOKER) {                                           // le Joker, au centre de l'éventail
            float k = Interpolation.swingOut.apply(progress(JOKER, JOKER + 0.35f));
            glow(batch, cx(), fanY(), 500f * k, GOLD, 0.6f * pulse(6f, 0.6f));
            drawCard(batch, cx(), MathUtils.lerp(fanY() - 200f, fanY() + 30f, k), 0f, 1.9f * k, c("8a2adf"));
            if (k > 0.5f) {
                font.getData().setScale(1.4f * k);
                caption(batch, font, Lang.t("JOKER"), cx(), fanY() + 30f - 52f * k, Math.min(1f, (k - 0.5f) * 2f));
                font.getData().setScale(1f);
            }
        }
    }

    private float fanY() { return worldHeight() * 0.62f; }

    /** Une carte à jouer centrée sur ({@code x}, {@code y}), tournée de {@code angle}, son logo de couleur {@code logo}. */
    private void drawCard(Batch batch, float x, float y, float angle, float scale, Color logo) {
        if (scale <= 0f) return;
        float w = 68f * scale, h = 96f * scale;
        batch.setColor(c("0a0808"));
        batch.draw(pixel, x - w / 2f, y - h / 2f, w / 2f, h / 2f, w, h, 1f, 1f, angle);
        batch.setColor(c("f4ecdc"));
        batch.draw(pixel, x - w / 2f + 4f * scale, y - h / 2f + 4f * scale, w / 2f - 4f * scale, h / 2f - 4f * scale,
            w - 8f * scale, h - 8f * scale, 1f, 1f, angle);
        batch.setColor(logo);
        float d = 18f * scale;
        batch.draw(pixel, x - d / 2f, y - d / 2f, d / 2f, d / 2f, d, d, 1f, 1f, angle + 45f);
    }

    /** Une ellipse pleine, en lignes horizontales. */
    private void ellipse(Batch batch, Color color, float x, float y, float rx, float ry) {
        batch.setColor(color);
        int lines = Math.max(4, (int) (ry / 3f));
        for (int i = -lines; i <= lines; i++) {
            float k = i / (float) lines;
            float half = rx * (float) Math.sqrt(Math.max(0f, 1f - k * k));
            batch.draw(pixel, x - half, y + k * ry - ry / lines, half * 2f, ry * 2f / lines + 1f);
        }
    }

    // -------------------------------------------------------------------------
    // Le geste du requin : le chèque refusé
    // -------------------------------------------------------------------------

    private void drawCheque(Batch batch) {
        float in = Interpolation.pow2Out.apply(progress(GESTURE, GESTURE + 0.3f));
        float x = cx() - worldWidth() * 0.22f, y = MathUtils.lerp(-200f, chequeY(), in), w = 460f, h = 200f;
        batch.setColor(c("3a3020"));
        batch.draw(pixel, x - w / 2f - 6f, y - h / 2f - 6f, w / 2f + 6f, h / 2f + 6f, w + 12f, h + 12f, 1f, 1f, -6f);
        batch.setColor(c("f0e6c8"));
        batch.draw(pixel, x - w / 2f, y - h / 2f, w / 2f, h / 2f, w, h, 1f, 1f, -6f);
        for (int i = 0; i < 3; i++) {                                  // les lignes du chèque
            batch.setColor(c("a89a78"));
            batch.draw(pixel, x - w * 0.4f, y + 40f - i * 45f, w * 0.4f, 2f, w * (i == 2 ? 0.4f : 0.8f), 4f, 1f, 1f, -6f);
        }
        // Le tampon descend, puis « REFUSÉ » en rouge.
        float press = Interpolation.pow2In.apply(progress(STAMP - 0.3f, STAMP));
        if (time < STAMP + 0.2f) {
            float sy = MathUtils.lerp(y + 400f, y + 40f, press);
            rect(batch, c("4a2a14"), x - 26f, sy + 60f, 52f, 140f);
            rect(batch, c("2a2a34"), x - 90f, sy, 180f, 60f);
        }
        if (time >= STAMP) {
            float k = progress(STAMP, STAMP + 0.12f);
            font.getData().setScale(MathUtils.lerp(4.4f, 3.2f, k));
            Color old = font.getColor().cpy();
            font.setColor(c("d81e1e"));
            caption(batch, font, Lang.t("REFUSÉ"), x, y, 1f);
            font.setColor(old);
            font.getData().setScale(1f);
        }
    }

    private float chequeY() { return worldHeight() * 0.3f; }

    // -------------------------------------------------------------------------
    // Le décor, avant l'entrée
    // -------------------------------------------------------------------------

    /** Sirène : un verre glisse seul sur le comptoir englouti, et s'arrête devant toi. */
    private void drawSlidingGlass(Batch batch) {
        float width = worldWidth(), height = worldHeight(), counter = height * 0.3f;
        glow(batch, cx(), counter + 300f, 900f, c("ff9ad0"), 0.15f);
        rect(batch, c("1a2a2e"), -MARGIN, -MARGIN, width + 2f * MARGIN, counter + MARGIN);
        rect(batch, c("3a5a5e"), -MARGIN, counter - 24f, width + 2f * MARGIN, 24f);
        float k = Interpolation.pow2Out.apply(progress(0.1f, SLIDE_END));
        float x = MathUtils.lerp(-120f, cx(), k);
        float wobble = time >= SLIDE_END ? 6f * MathUtils.sin((time - SLIDE_END) * 25f) * Math.max(0f, 1f - (time
            - SLIDE_END) * 2f) : -4f;
        float scale = 22f;
        glow(batch, x, counter + 120f, 300f, c("ff4fa0"), 0.3f);
        batch.setColor(Color.WHITE);
        batch.draw(glass, x - glass.getRegionWidth() * scale / 2f, counter, glass.getRegionWidth() * scale / 2f, 0f,
            glass.getRegionWidth() * scale, glass.getRegionHeight() * scale, 1f, 1f, wobble);
        for (int i = 0; i < 6; i++) {                                  // le sillage de bulles derrière le verre
            float bx = x - 80f - i * 70f, alpha = (1f - i / 6f) * (1f - progress(SLIDE_END, SLIDE_END + 0.4f));
            batch.setColor(FOAM.r, FOAM.g, FOAM.b, 0.5f * alpha);
            batch.draw(ring, bx - 12f, counter + 10f + (i % 2) * 20f, 24f, 24f);
        }
    }

    /** Jackpot Vivant : un joueur fantôme tire le levier d'une machine ; rien ; il s'efface. */
    private void drawGhost(Batch batch) {
        float width = worldWidth(), height = worldHeight(), floor = height * 0.3f;
        rect(batch, c("0a1a20"), -MARGIN, -MARGIN, width + 2f * MARGIN, floor + MARGIN);
        float scale = 26f, mw = machine.getRegionWidth() * scale, mh = machine.getRegionHeight() * scale;
        float mx = cx() + 120f;
        float pull = gesture(PULL - 0.25f, PULL + 0.25f);
        float spinning = time >= PULL && time < NOTHING ? 1f : 0f;
        float dim = time >= NOTHING ? 0.4f : 0.7f;
        glow(batch, mx, floor + mh * 0.5f, 700f, GOLD, 0.15f * dim);
        batch.setColor(dim, dim, dim, 1f);
        batch.draw(machine, mx - mw / 2f, floor, mw, mh);
        if (spinning > 0f) {                                           // les rouleaux défilent un instant
            additive(batch);
            batch.setColor(1f, 1f, 1f, 0.25f * pulse(40f, 0.3f));
            batch.draw(pixel, mx - mw * 0.36f, floor + mh * 0.47f, mw * 0.72f, mh * 0.23f);
            normal(batch);
        }
        // Le levier sur le côté.
        float leverX = mx + mw / 2f + 10f, leverY = floor + mh * 0.55f, angle = MathUtils.lerp(80f, -60f, pull);
        line(batch, c("9a9aa8"), leverX, leverY, leverX + MathUtils.cosDeg(angle) * 200f, leverY + MathUtils.sinDeg(angle)
            * 200f, 14f);
        glow(batch, leverX + MathUtils.cosDeg(angle) * 200f, leverY + MathUtils.sinDeg(angle) * 200f, 60f, c("e0283a"), 1f);
        // Le fantôme, transparent, qui s'efface après le « rien ».
        float alpha = 0.45f * (1f - progress(GHOST_FADE, PRELUDE - 0.2f)) * (0.85f + 0.15f * MathUtils.sin(time * 6f));
        float gs = 24f, gw = ghost.getRegionWidth() * gs, gh = ghost.getRegionHeight() * gs;
        float gx = leverX + 230f, gy = floor + 20f + 15f * MathUtils.sin(time * 3f) + 200f * progress(GHOST_FADE, PRELUDE);
        glow(batch, gx, gy + gh * 0.5f, 600f, c("a8f0ff"), alpha * 0.6f);
        batch.setColor(0.75f, 0.95f, 1f, alpha);
        batch.draw(ghost, gx - gw / 2f, gy, gw, gh);
        if (time >= NOTHING && time < GHOST_FADE + 0.4f) {
            font.getData().setScale(2.4f);
            caption(batch, font, "...", mx, floor + mh + 60f, 1f - progress(GHOST_FADE, GHOST_FADE + 0.4f));
            font.getData().setScale(1f);
        }
    }

    /** Requin : une petite banque engloutie, son coffre grand ouvert, plein d'or. */
    private void drawBank(Batch batch) {
        float width = worldWidth(), height = worldHeight(), floor = height * 0.12f;
        float bx = cx() - 160f, bw = 760f, bh = height * 0.5f;
        rect(batch, c("1a3a48"), bx - bw / 2f, floor, bw, bh);                          // la façade
        rect(batch, c("24505e"), bx - bw / 2f - 30f, floor + bh, bw + 60f, 40f);
        for (int i = 0; i < 4; i++) rect(batch, c("3a6a78"), bx - bw / 2f + 60f + i * 200f, floor, 60f, bh);   // les colonnes
        for (int r = 0; r < 4; r++) rect(batch, c("24505e"), bx - (bw / 2f - r * 90f), floor + bh + 40f + r * 34f,
            bw - r * 180f, 34f);                                                            // le fronton
        font.getData().setScale(2.2f);
        caption(batch, font, Lang.t("BANQUE"), bx, floor + bh - 50f, 0.8f);
        font.getData().setScale(1f);
        // Le coffre, au premier plan : la porte ouverte, l'or qui brille dedans.
        float sx = width * 0.74f, scale = 30f, sw = safe.getRegionWidth() * scale, sh = safe.getRegionHeight() * scale;
        glow(batch, sx, floor + sh * 0.5f, 700f, GOLD, 0.4f * pulse(4f, 0.7f));
        batch.setColor(Color.WHITE);
        batch.draw(safe, sx - sw / 2f, floor, sw, sh);
        for (int i = 0; i < 9; i++) {
            batch.setColor(Color.WHITE);
            sprite(batch, coin, sx - sw * 0.3f + (i % 3) * sw * 0.3f, floor + 70f + (i / 3) * 60f, 1.4f, 0f);
        }
        float swing = 70f + 6f * MathUtils.sin(time * 2f);                              // la porte, entrouverte, qui ondule
        float doorW = sw * MathUtils.cosDeg(swing);
        rect(batch, c("5a5a6a"), sx - sw / 2f - doorW, floor + 10f, doorW, sh - 20f);
        glow(batch, sx - sw / 2f - doorW * 0.5f, floor + sh * 0.5f, 120f, c("c8c8d8"), 0.5f);
        for (int i = 0; i < 6; i++) {                                                   // des éclats d'or
            float t = (time * 1.3f + i * 0.37f) % 1f;
            glow(batch, sx - sw * 0.35f + (i * 97 % 100) / 100f * sw * 0.7f, floor + 60f + (i * 53 % 100) / 100f * sh * 0.6f,
                80f * MathUtils.sin(t * MathUtils.PI), Color.WHITE, 0.8f);
        }
    }

    /** Kraken : l'eau devient noire, des cartes coulent lentement en tournant. */
    private void drawSinkingCards(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        for (int i = 0; i < 12; i++) {
            float x = width * ((i * 0.083f + 0.04f) % 1f) + 50f * MathUtils.sin(time * 1.5f + i);
            float y = height + 120f - (time * (90f + (i % 4) * 30f) + (i * 137 % 500)) ;
            drawCard(batch, x, y, 30f * MathUtils.sin(time * (1f + i * 0.1f) + i), 1f + (i % 3) * 0.3f,
                i % 2 == 0 ? c("e0283a") : c("0a0808"));
        }
        fill(batch, Color.BLACK, 0.75f * Interpolation.pow2In.apply(progress(0.2f, PRELUDE)));
        float eyes = progress(1.2f, 1.5f);                                               // un éclat jaune, tout au fond
        if (eyes > 0f) glow(batch, cx(), height * 0.2f, 200f, c("ffe066"), 0.4f * eyes * pulse(5f, 0.5f));
    }
}
