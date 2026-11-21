package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les rois du Palace des Neiges entrent en scène (10 lieux, Astra 2026-11-20) :
 * <ul>
 *   <li>Le Concierge : le hall de l'hôtel, son lustre givré et son comptoir. Il se lève derrière le comptoir,
 *   frappe la sonnette ; le tableau des clés s'illumine et une vraie carte du jeu, le Roi de Pique, glisse sur
 *   le comptoir.</li>
 *   <li>L'Impératrice du Givre : la patinoire gelée sous la lune. Elle arrive en patinant en grand arc, freine
 *   dans une gerbe de glace, lève son sceptre : des cristaux montent tout autour.</li>
 *   <li>Le Yéti : la montagne sous la tempête, la cabine du téléphérique qui se balance. Il saute du sommet,
 *   atterrit lourdement et rugit : des boules de neige dévalent.</li>
 *   <li>Le Prince des Neiges : la salle de bal, son lustre de glace. Il descend du haut de l'escalier, lève
 *   sa coupe : le champagne mousse et le lustre s'illumine.</li>
 * </ul>
 */
public class PalaceKings extends KingEntrance {

    /** Concierge : il se lève, il frappe la sonnette. */
    static final float RISE = 0.6f, BELL = 3.4f;
    /** Impératrice : elle patine jusqu'au milieu, elle lève son sceptre. */
    static final float GLIDE = 1.2f, SCEPTER = 3.4f;
    /** Yéti : il atterrit, il rugit. */
    static final float YETI_LAND = 0.7f, ROAR = 3.4f;
    /** Prince : il descend l'escalier, il lève sa coupe. */
    static final float DESCEND = 1.4f, TOAST = 3.4f;

    private static final Color NIGHT = hex("0e1a34"), ICE = hex("bfe8ff"), ICE_DEEP = hex("2a5a8a"), SNOW = hex("f2f8ff");
    private static final Color GOLD = hex("ffd54a"), WINE = hex("4a0e1a"), STONE = hex("e8dcc8"), LAMP = hex("ffe8b0");
    private static final String[] BELL_ART = {
        "....oo....", "...oggo...", "..oggggo..", ".oggggsgo.", "oooooooooo"};
    private static final String[] SNOWBALL = {".oo.", "owwo", "owso", ".oo."};

    private final TextureRegion bell, snowball, heldCard;

    public PalaceKings(CutsceneKit kit, EnemyKind kind) {
        super(kit, kind);
        bell = art("ogs", new Color[] {hex("3a2a10"), GOLD, hex("c4891e")}, BELL_ART);
        snowball = art("ows", new Color[] {hex("6a7a98"), SNOW, hex("c8d8ec")}, SNOWBALL);
        heldCard = kind == EnemyKind.CONCIERGE ? gameCard("cards/light/13-P.png") : null;
    }

    @Override
    protected void stepPrelude(float delta) {
        if (kind == EnemyKind.YETI && at(1.4f)) rumble(0.3f, 4f);
    }

    @Override
    protected void prelude(Batch batch) {
        switch (kind) {
            case CONCIERGE -> drawHall(batch, 0f);
            case IMPERATRICE_GIVRE -> drawRink(batch);
            case YETI -> drawMountain(batch);
            default -> drawBallroom(batch, 0f);
        }
    }

    @Override
    protected void step(float delta) {
        switch (kind) {
            case CONCIERGE -> {
                if (at(RISE)) rumble(0.15f, 4f);
                if (at(BELL)) {
                    rumble(0.15f, 5f);
                    sparks(bellX(), deskTop() + 40f, 30, GOLD, 500f, 10f);
                }
            }
            case IMPERATRICE_GIVRE -> {
                if (time < GLIDE && random.nextFloat() < delta * 40f) {
                    Particle chip = particle(skaterX(), kingY() + 10f, 90f + 60f * random.nextFloat(), 300f, 0.6f, 14f);
                    chip.color.set(ICE);
                    chip.gravity = 600f;
                    parts.add(chip);
                }
                if (at(GLIDE)) {
                    rumble(0.25f, 6f);
                    sparks(cx() - 120f, kingY(), 50, ICE, 700f, 12f);
                }
                if (at(SCEPTER)) sparks(kingPixelX(30f, cx(), KING_SCALE), kingPixelY(6f, kingY(), KING_SCALE), 60, SNOW, 800f, 12f);
            }
            case YETI -> {
                if (at(YETI_LAND)) {
                    rumble(0.5f, 14f);
                    sparks(cx(), kingY(), 50, SNOW, 700f, 16f);
                }
                if (at(ROAR)) {
                    rumble(0.6f, 12f);
                    debris(cx(), kingY() + 200f, 16, snowball, 10f, 1300f);
                }
            }
            default -> {
                if (at(DESCEND)) rumble(0.1f, 3f);
                if (at(TOAST)) {
                    sparks(kingPixelX(29f, cx(), KING_SCALE), kingPixelY(10f, kingY(), KING_SCALE), 50, hex("ffe08a"), 500f, 10f);
                    sparks(cx(), worldHeight() * 0.85f, 40, ICE, 600f, 10f);
                }
            }
        }
    }

    @Override
    protected void draw(Batch batch) {
        switch (kind) {
            case CONCIERGE -> drawConcierge(batch);
            case IMPERATRICE_GIVRE -> drawEmpress(batch);
            case YETI -> drawYeti(batch);
            default -> drawPrince(batch);
        }
        snow(batch, 70, 0.7f);
        drawParticles(batch, parts);
    }

    /** De la neige qui tombe doucement sur tout l'écran ({@code count} flocons, {@code alpha}). */
    private void snow(Batch batch, int count, float alpha) {
        float width = worldWidth() + 2f * MARGIN, height = worldHeight() + 2f * MARGIN;
        batch.setColor(0.95f, 0.97f, 1f, alpha);
        for (int i = 0; i < count; i++) {
            float speed = 90f + (i * 37 % 11) * 14f;
            float x = ((i * 157.3f + MathUtils.sin(time + i) * 30f) % width) - MARGIN;
            float y = height - ((i * 91.7f + time * speed) % height) - MARGIN;
            float size = 4f + (i % 3) * 2f;
            batch.draw(pixel, x, y, size, size);
        }
    }

    // -------------------------------------------------------------------------
    // Le Concierge
    // -------------------------------------------------------------------------

    private float deskTop() { return worldHeight() * 0.3f; }
    private float bellX()   { return cx() + worldWidth() * 0.2f; }

    /** Le hall : boiseries, tableau des clés derrière le comptoir ({@code lit} : ses clés brillent), lustre givré. */
    private void drawHall(Batch batch, float lit) {
        float width = worldWidth(), height = worldHeight();
        fill(batch, hex("3a2418"), 1f);
        rect(batch, hex("2a1a10"), width * 0.2f, height * 0.42f, width * 0.6f, height * 0.4f);    // le tableau des clés
        for (int row = 0; row < 4; row++) for (int col = 0; col < 10; col++) {
            float x = width * (0.24f + 0.054f * col), y = height * (0.48f + 0.08f * row);
            rect(batch, GOLD, x, y, 10f, 26f);
            if (lit > 0f) glow(batch, x + 5f, y + 13f, 50f, GOLD, 0.5f * lit);
        }
        glow(batch, cx(), height * 0.92f, 420f, LAMP, 0.5f);                                       // le lustre
        for (int k = -3; k <= 3; k++) line(batch, ICE, cx() + k * 40f, height * 0.95f, cx() + k * 46f, height * 0.86f, 6f);
    }

    private void drawConcierge(Batch batch) {
        float width = worldWidth();
        drawHall(batch, progress(BELL, BELL + 0.15f));
        float rise = Interpolation.pow2Out.apply(progress(RISE - 0.4f, RISE + 0.6f));
        float strike = gesture(BELL - 0.25f, BELL + 0.3f);
        pose(0f, -300f * (1f - rise), -6f * strike, 0.04f * strike);
        drawKing(batch, cx() - width * 0.06f, deskTop() - 140f, KING_SCALE, 0f, rise);
        // Le comptoir de marbre, la sonnette, et la carte qui glisse.
        rect(batch, STONE, -MARGIN, -MARGIN, width + 2f * MARGIN, deskTop() + MARGIN);
        rect(batch, hex("b8a890"), -MARGIN, deskTop() - 14f, width + 2f * MARGIN, 14f);
        rect(batch, GOLD, -MARGIN, deskTop() - 24f, width + 2f * MARGIN, 6f);
        batch.setColor(Color.WHITE);
        sprite(batch, bell, bellX(), deskTop() + 40f - 10f * strike, 12f, 0f);
        float slide = Interpolation.pow3Out.apply(progress(BELL + 0.3f, BELL + 1.0f));
        if (slide > 0f) {
            batch.setColor(Color.WHITE);
            sprite(batch, heldCard, MathUtils.lerp(cx() - width * 0.06f, cx() - width * 0.28f, slide), deskTop() + 120f, 6f,
                -8f * slide);
            glow(batch, cx() - width * 0.28f * slide, deskTop() + 140f, 280f, GOLD, 0.35f * slide);
        }
        flash(batch, GOLD, BELL, 0.1f, 0.25f);
    }

    // -------------------------------------------------------------------------
    // L'Impératrice du Givre
    // -------------------------------------------------------------------------

    /** La patinoire gelée sous la lune : sapins noirs, glace bleutée, rayures des lames. */
    private void drawRink(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, hex("1a3058"), NIGHT);
        glow(batch, width * 0.82f, height * 0.82f, 260f, hex("f6f8ff"), 0.8f);
        for (int i = 0; i < 9; i++) {                                                  // les sapins
            float x = width * (i / 8f), h = 160f + (i * 37 % 5) * 30f;
            for (int k = 0; k < 6; k++) {
                float half = (6 - k) * 18f;
                rect(batch, hex("0a1a20"), x - half, height * 0.42f + k * h / 6f, half * 2f, h / 6f + 2f);
            }
        }
        rect(batch, hex("8ac8f0"), -MARGIN, -MARGIN, width + 2f * MARGIN, height * 0.42f + MARGIN);
        for (int i = 0; i < 8; i++) {
            float y = height * (0.05f + 0.045f * i);
            line(batch, hex("d8f0ff"), width * (0.1f + 0.07f * i), y, width * (0.5f + 0.05f * i), y + 20f, 3f);
        }
    }

    /** @return où l'Impératrice se tient : elle arrive en arc par la gauche, puis s'arrête au milieu. */
    private float skaterX() {
        return MathUtils.lerp(-300f, cx(), Interpolation.pow2Out.apply(progress(0f, GLIDE)));
    }

    private void drawEmpress(Batch batch) {
        drawRink(batch);
        float glide = progress(0f, GLIDE);
        float lift = gesture(SCEPTER - 0.2f, SCEPTER + 1.0f);
        float arc = MathUtils.sin(glide * MathUtils.PI) * 80f;
        pose(0f, arc + 30f * lift, glide < 1f ? -10f * (1f - glide) : 0f, 0.06f * lift);
        drawKing(batch, skaterX(), kingY(), KING_SCALE, 0f, 1f);
        if (lift > 0f) {                                                                // les cristaux qui montent
            additive(batch);
            for (int k = 0; k < 10; k++) {
                float x = cx() + (k - 4.5f) * 160f, y = kingY() + (time - SCEPTER) * 300f + (k % 3) * 80f;
                glow(batch, x, y, 90f, ICE, 0.6f * lift);
            }
            normal(batch);
        }
        flash(batch, ICE, SCEPTER, 0.15f, 0.35f);
    }

    // -------------------------------------------------------------------------
    // Le Yéti
    // -------------------------------------------------------------------------

    /** La montagne sous la tempête : pente blanche, câble du téléphérique et sa cabine qui se balance. */
    private void drawMountain(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, hex("5a7aa8"), hex("2a3a5a"));
        for (int x = 0; x < 8; x++) {                                                  // les sommets au loin
            float px = width * x / 7f;
            for (int k = 0; k < 8; k++) {
                float half = (8 - k) * 40f;
                rect(batch, k > 5 ? SNOW : hex("8aa0bc"), px - half, height * 0.4f + k * 40f, half * 2f, 42f);
            }
        }
        line(batch, hex("3a3a4a"), -MARGIN, height * 0.95f, width + MARGIN, height * 0.6f, 4f);
        float swing = MathUtils.sin(time * 2f) * 8f;
        float cabX = width * 0.3f, cabY = MathUtils.lerp(height * 0.95f, height * 0.6f, (cabX + MARGIN) / (width + 2f * MARGIN));
        line(batch, hex("3a3a4a"), cabX, cabY, cabX + swing, cabY - 60f, 4f);
        rect(batch, hex("d8202c"), cabX + swing - 50f, cabY - 140f, 100f, 80f);
        rect(batch, hex("bfe8ff"), cabX + swing - 38f, cabY - 110f, 76f, 30f);
        rect(batch, SNOW, -MARGIN, -MARGIN, width + 2f * MARGIN, height * 0.25f + MARGIN);
    }

    private void drawYeti(Batch batch) {
        drawMountain(batch);
        float fall = Interpolation.pow2In.apply(progress(0f, YETI_LAND));
        float squash = gesture(YETI_LAND, YETI_LAND + 0.35f);
        float roar = gesture(ROAR - 0.2f, ROAR + 1.0f);
        pose(0f, worldHeight() * (1f - fall), 0f, -0.15f * squash + 0.1f * roar);
        drawKing(batch, cx(), kingY(), KING_SCALE, 0f, 1f);
        if (roar > 0f) {                                                                 // le souffle glacé du rugissement
            float mouthY = kingPixelY(22f, kingY(), KING_SCALE);
            additive(batch);
            for (int k = 1; k <= 5; k++) glow(batch, cx(), mouthY - k * 40f * roar, 140f + k * 80f * roar, ICE, 0.25f * roar);
            normal(batch);
        }
        flash(batch, SNOW, YETI_LAND, 0.1f, 0.3f);
    }

    // -------------------------------------------------------------------------
    // Le Prince des Neiges
    // -------------------------------------------------------------------------

    /** La salle de bal : colonnes de marbre, grand escalier, lustre de glace ({@code lit} : il brille). */
    private void drawBallroom(Batch batch, float lit) {
        float width = worldWidth(), height = worldHeight();
        fill(batch, hex("1a2a5a"), 1f);
        for (int i = 0; i < 6; i++) {
            float x = width * (0.04f + 0.19f * i);
            rect(batch, STONE, x, height * 0.2f, 60f, height * 0.7f);
            rect(batch, hex("b8a890"), x + 40f, height * 0.2f, 20f, height * 0.7f);
        }
        for (int k = 0; k < 8; k++) {                                                    // le grand escalier
            float w = width * (0.5f - 0.04f * k);
            rect(batch, k % 2 == 0 ? WINE : hex("6a1a3a"), cx() - w / 2f, height * 0.2f + k * 30f, w, 30f);
        }
        rect(batch, hex("e8eef8"), -MARGIN, -MARGIN, width + 2f * MARGIN, height * 0.2f + MARGIN);
        glow(batch, cx(), height * 0.88f, 300f + 400f * lit, ICE, 0.5f + 0.4f * lit);
        for (int k = -4; k <= 4; k++) line(batch, ICE, cx() + k * 30f, height * 0.95f, cx() + k * 36f, height * 0.82f, 5f);
    }

    private void drawPrince(Batch batch) {
        float toast = progress(TOAST, TOAST + 0.3f);
        drawBallroom(batch, toast);
        float down = Interpolation.pow2Out.apply(progress(0f, DESCEND));
        float lift = gesture(TOAST - 0.2f, TOAST + 1.0f);
        float step = time < DESCEND ? Math.abs(MathUtils.sin(time * 9f)) * 12f : 0f;
        pose(0f, 240f * (1f - down) + step + 20f * lift, 0f, 0.05f * lift);
        drawKing(batch, cx(), kingY(), KING_SCALE * MathUtils.lerp(0.7f, 1f, down), 0f, 1f);
        if (lift > 0f) {                                                                 // les bulles de champagne
            float glassX = kingPixelX(29f, cx(), KING_SCALE), glassY = kingPixelY(10f, kingY(), KING_SCALE);
            for (int k = 0; k < 8; k++) {
                float y = glassY + ((time - TOAST) * 200f + k * 40f) % 300f;
                glow(batch, glassX + MathUtils.sin(time * 5f + k) * 20f, y, 30f, hex("ffe08a"), 0.8f * lift);
            }
        }
        flash(batch, ICE, TOAST, 0.15f, 0.3f);
    }
}
