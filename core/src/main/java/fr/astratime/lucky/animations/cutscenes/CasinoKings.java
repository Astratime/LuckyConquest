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

/**
 * Les rois du Casino Englouti entrent en scène, sous la mer :
 * <ul>
 *   <li>Sirène du Bar : des bulles montent, un chant ; les verres dansent ; elle apparaît derrière le comptoir.</li>
 *   <li>Jackpot Vivant : les machines s'allument, JACKPOT clignote partout ; la plus grande se lève et marche.</li>
 *   <li>Grand Requin Banquier : des pièces coulent, une ombre tourne autour ; le requin surgit.</li>
 *   <li>Kraken : la table VIP se fend, huit bras en sortent avec chacun une carte ; un œil s'ouvre dessous.</li>
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

    private static final int ARM_COUNT = 8;
    private static final Color DEEP = c("03101a"), SHALLOW = c("0c3a4a"), FOAM = c("bfeeff"), GOLD = c("ffc93a");
    private static final Color TENTACLE = c("8a2a4a"), TENTACLE_LIGHT = c("c25a7a");

    private final BitmapFont font;
    private final TextureRegion glass, note, machine, shark, coin, disc;
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
            }
            case REQUIN_BANQUIER -> {
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
            glow(batch, cx(), counter + 350f, 1100f, c("ff9ad0"), 0.35f * appear);
            drawKing(batch, cx(), counter - 60f, KING_SCALE, 1f - appear, appear);
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
                caption(batch, font, "JACKPOT", x, y, 1f - progress(WALK, WALK + 0.5f));
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
            glow(batch, cx(), y + 25f * scale, 70f * scale, GOLD, 0.4f);
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
            drawKing(batch, cx(), MathUtils.lerp(height * 0.45f, kingY(), k), scale, 0f, 1f);
        }
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
            if (rise > 0.6f) {                                       // la carte au bout du bras
                float cardAngle = angle - 90f;
                batch.setColor(c("0a0808"));
                batch.draw(pixel, x - 34f, y - 2f, 34f, 0f, 68f, 96f, 1f, 1f, cardAngle);
                batch.setColor(c("f4ecdc"));
                batch.draw(pixel, x - 30f, y + 2f, 30f, -4f, 60f, 88f, 1f, 1f, cardAngle);
                // Le logo au centre de la carte : on tourne sa position avec la carte, puis le losange sur lui-même.
                float logoX = x - MathUtils.sinDeg(cardAngle) * 46f;
                float logoY = y - 2f + MathUtils.cosDeg(cardAngle) * 46f;
                batch.setColor(i % 2 == 0 ? c("e0283a") : c("0a0808"));
                batch.draw(pixel, logoX - 9f, logoY - 9f, 9f, 9f, 18f, 18f, 1f, 1f, cardAngle + 45f);
            }
        }
        flash(batch, c("ffe066"), EYE, 0.2f, 0.35f);
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
}
