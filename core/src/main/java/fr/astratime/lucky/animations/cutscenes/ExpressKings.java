package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les rois du Rapide de Minuit entrent en scène (10 lieux, Astra 2026-11-20) :
 * <ul>
 *   <li>La Locomotive vivante : des rails dans la nuit, un phare approche. Elle surgit de la droite dans un
 *   nuage de fumée, freine dans une gerbe d'étincelles, puis siffle : une colonne de vapeur monte.</li>
 *   <li>Le Grand Chef : le wagon-restaurant, ses tables et ses bougies. Il surgit derrière son comptoir et
 *   soulève sa cloche d'argent : sur le plateau, une vraie carte du jeu, l'As de Coeur.</li>
 *   <li>La Dame en noir : le couloir du wagon-lit, la lune qui défile aux fenêtres. Elle sort du brouillard,
 *   les lampes s'éteignent une à une ; sa voilette se soulève, ses yeux luisent.</li>
 *   <li>Le Desperado : le toit du fourgon blindé qui file sous la lune. Il saute sur le toit, tire deux
 *   coups en l'air, et des pièces jaillissent du fourgon.</li>
 * </ul>
 */
public class ExpressKings extends KingEntrance {

    /** Locomotive : elle entre (de 0 à {@link #BRAKE}), freine, siffle. */
    static final float BRAKE = 1.0f, WHISTLE = 3.4f;
    /** Grand Chef : il surgit, il soulève sa cloche. */
    static final float CHEF_POP = 0.5f, CLOCHE_LIFT = 3.4f;
    /** Dame en noir : elle sort du brouillard, les lampes s'éteignent, sa voilette se lève. */
    static final float LADY_IN = 0.3f, LAMPS_OUT = 1.6f, VEIL_UP = 3.4f;
    /** Desperado : il atterrit sur le toit, deux coups de feu, les pièces. */
    static final float LAND = 0.6f, SHOT_1 = 3.4f, SHOT_2 = 3.75f;

    private static final Color NIGHT = hex("0a0c1e"), STEAM = hex("e8eef4"), SMOKE = hex("6a6a78"), GOLD = hex("ffc93a");
    private static final Color LAMP = hex("ffd27a"), VARNISH = hex("4a0e1a"), VIOLET = hex("b8a0ff");
    private static final String[] CLOCHE = {
        ".....oo.....", "....owwo....", "...owwwso...", "..owwwwwso..", ".owwwwwwwso.", ".owwwwwwwwso", "oooooooooooo"};

    private final TextureRegion cloche, heldCard, coinRegion;

    public ExpressKings(CutsceneKit kit, EnemyKind kind) {
        super(kit, kind);
        cloche = art("ows", new Color[] {hex("3a3a4e"), hex("dce4ee"), hex("8a97a8")}, CLOCHE);
        heldCard = kind == EnemyKind.GRAND_CHEF ? gameCard("cards/light/1-H.png") : null;
        coinRegion = kind == EnemyKind.DESPERADO ? load("hud/coin.png") : null;
    }

    @Override
    protected void stepPrelude(float delta) {
        if (kind == EnemyKind.LOCOMOTIVE && at(1.6f)) rumble(0.3f, 4f);
        if (kind == EnemyKind.DESPERADO && at(1.2f)) rumble(0.2f, 3f);
    }

    @Override
    protected void prelude(Batch batch) {
        switch (kind) {
            case LOCOMOTIVE -> drawTracks(batch, true);
            case GRAND_CHEF -> drawDiner(batch);
            case DAME_EN_NOIR -> drawCorridor(batch, 1f);
            default -> drawRoof(batch);
        }
    }

    @Override
    protected void step(float delta) {
        switch (kind) {
            case LOCOMOTIVE -> {
                if (time < BRAKE && random.nextFloat() < delta * 30f) {
                    Particle puff = particle(kingPixelX(17f, engineX(), KING_SCALE), kingY() + 40f * KING_SCALE,
                        60f + 60f * random.nextFloat(), 200f, 1.2f, 60f + 40f * random.nextFloat());
                    puff.color.set(SMOKE);
                    parts.add(puff);
                }
                if (at(BRAKE)) {
                    rumble(0.4f, 10f);
                    sparks(cx() - 200f, kingY(), 40, hex("ffb84a"), 600f, 10f);
                    sparks(cx() + 200f, kingY(), 40, hex("ffb84a"), 600f, 10f);
                }
                if (at(WHISTLE)) {
                    rumble(0.3f, 6f);
                    sparks(cx(), kingY() + 42f * KING_SCALE, 50, STEAM, 700f, 16f);
                }
            }
            case GRAND_CHEF -> {
                if (at(CHEF_POP)) {
                    rumble(0.2f, 6f);
                    sparks(cx(), counterTop() + 100f, 24, GOLD, 500f, 12f);
                }
                if (at(CLOCHE_LIFT)) sparks(plateX(), counterTop() + 60f, 40, GOLD, 500f, 12f);
            }
            case DAME_EN_NOIR -> {
                for (int i = 0; i < 4; i++) if (at(LAMPS_OUT + i * 0.18f)) rumble(0.05f, 2f);
                if (at(VEIL_UP)) sparks(cx(), kingY() + 26f * KING_SCALE, 30, VIOLET, 400f, 10f);
            }
            default -> {
                if (at(LAND)) {
                    rumble(0.35f, 10f);
                    sparks(cx(), roofY(), 30, hex("c8b8a0"), 500f, 12f);
                }
                if (at(SHOT_1) || at(SHOT_2)) {
                    rumble(0.15f, 8f);
                    sparks(gunX(), gunY(), 24, hex("fff2c0"), 600f, 10f);
                }
                if (at(SHOT_2 + 0.1f) && coinRegion != null) debris(cx(), roofY(), 18, coinRegion, 6f, 1200f);
            }
        }
    }

    @Override
    protected void draw(Batch batch) {
        switch (kind) {
            case LOCOMOTIVE -> drawEngine(batch);
            case GRAND_CHEF -> drawChef(batch);
            case DAME_EN_NOIR -> drawLady(batch);
            default -> drawDesperado(batch);
        }
        drawParticles(batch, parts);
    }

    // -------------------------------------------------------------------------
    // La Locomotive vivante
    // -------------------------------------------------------------------------

    /** Les rails dans la nuit, en perspective ; dans le décor, un phare approche. */
    private void drawTracks(Batch batch, boolean headlight) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, hex("141a3a"), NIGHT);
        for (int i = 0; i < 30; i++) {
            batch.setColor(1f, 0.95f, 0.7f, 0.5f + 0.5f * MathUtils.sin(time * 3f + i));
            batch.draw(pixel, (i * 197.3f) % width, height * 0.6f + (i * 71.9f) % (height * 0.4f), 4f, 4f);
        }
        float horizon = height * 0.5f;
        rect(batch, hex("0e1424"), -MARGIN, -MARGIN, width + 2f * MARGIN, horizon + MARGIN);
        for (int k = 0; k < 14; k++) {
            float t = k / 14f, y = MathUtils.lerp(-MARGIN, horizon, (float) Math.pow(t, 0.7));
            float half = MathUtils.lerp(width * 0.45f, 30f, t);
            rect(batch, hex("2a1e18"), cx() - half, y, half * 2f, 14f * (1f - t) + 2f);
        }
        line(batch, hex("8a97a8"), cx() - width * 0.32f, -MARGIN, cx() - 14f, horizon, 8f);
        line(batch, hex("8a97a8"), cx() + width * 0.32f, -MARGIN, cx() + 14f, horizon, 8f);
        if (!headlight) return;
        float near = Interpolation.pow2In.apply(progress(0f, 2f));
        glow(batch, cx(), MathUtils.lerp(horizon, height * 0.35f, near), 80f + 700f * near, hex("fff2c0"), 0.9f);
    }

    /** @return où la locomotive se tient : elle arrive de la droite, puis s'arrête au milieu. */
    private float engineX() {
        return MathUtils.lerp(worldWidth() + 300f, cx(), Interpolation.pow3Out.apply(progress(0f, BRAKE)));
    }

    private void drawEngine(Batch batch) {
        drawTracks(batch, false);
        float braking = gesture(BRAKE - 0.1f, BRAKE + 0.4f);
        float whistle = gesture(WHISTLE - 0.1f, WHISTLE + 1.2f);
        float shake = time < BRAKE ? MathUtils.sin(time * 50f) * 6f : 0f;
        pose(0f, shake - 20f * whistle, -4f * braking, 0.12f * whistle);
        drawKing(batch, engineX(), kingY(), KING_SCALE, 0f, 1f);
        if (whistle > 0f) {                                                // la colonne de vapeur
            float top = kingPixelY(0f, kingY(), KING_SCALE);
            additive(batch);
            for (int k = 0; k < 8; k++) {
                float y = top + k * 90f * whistle, size = 120f + k * 40f;
                glow(batch, engineX() + MathUtils.sin(time * 8f + k) * 20f, y, size, STEAM, 0.5f * whistle);
            }
            normal(batch);
        }
        flash(batch, hex("fff2c0"), WHISTLE, 0.2f, 0.4f);
    }

    // -------------------------------------------------------------------------
    // Le Grand Chef
    // -------------------------------------------------------------------------

    private float counterTop() { return worldHeight() * 0.32f; }
    private float plateX()     { return cx() + worldWidth() * 0.22f; }

    /** Le wagon-restaurant : boiseries bordeaux, fenêtres sur la nuit, tables nappées, bougies qui vacillent. */
    private void drawDiner(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        fill(batch, VARNISH, 1f);
        for (int i = 0; i < 4; i++) {
            float x = width * (0.08f + 0.24f * i);
            rect(batch, NIGHT, x, height * 0.5f, width * 0.16f, height * 0.3f);
            rect(batch, GOLD, x, height * 0.5f - 6f, width * 0.16f, 6f);
            batch.setColor(1f, 0.95f, 0.7f, 0.8f);
            batch.draw(pixel, x + (time * 400f + i * 90f) % (width * 0.16f), height * 0.7f, 4f, 4f); // les étoiles qui défilent
        }
        for (int i = 0; i < 3; i++) {                                      // les tables nappées et leurs bougies
            float x = width * (0.2f + 0.3f * i);
            rect(batch, hex("f6f0e2"), x - 120f, height * 0.2f, 240f, 90f);
            rect(batch, hex("c8c0b4"), x - 120f, height * 0.2f, 240f, 14f);
            rect(batch, hex("f6f0e2"), x - 6f, height * 0.2f + 90f, 12f, 40f);
            glow(batch, x, height * 0.2f + 140f, 90f * (0.85f + 0.15f * MathUtils.sin(time * 13f + i)), LAMP, 0.7f);
        }
    }

    private void drawChef(Batch batch) {
        float width = worldWidth();
        drawDiner(batch);
        float pop = Interpolation.swingOut.apply(progress(CHEF_POP - 0.3f, CHEF_POP + 0.2f));
        float bow = gesture(CLOCHE_LIFT - 0.2f, CLOCHE_LIFT + 0.8f);
        pose(0f, -400f * (1f - pop), -6f * bow, 0.05f * bow);
        drawKing(batch, cx() - width * 0.08f, counterTop() - 140f, KING_SCALE, 0f, pop);
        // Le comptoir, devant lui, et le plateau d'argent.
        rect(batch, hex("6a1a2a"), -MARGIN, -MARGIN, width + 2f * MARGIN, counterTop() + MARGIN);
        rect(batch, GOLD, -MARGIN, counterTop() - 10f, width + 2f * MARGIN, 10f);
        rect(batch, hex("dce4ee"), plateX() - 150f, counterTop(), 300f, 14f);
        float lift = Interpolation.pow2Out.apply(progress(CLOCHE_LIFT, CLOCHE_LIFT + 0.5f));
        if (lift > 0f) {                                                   // la carte sous la cloche
            batch.setColor(Color.WHITE);
            sprite(batch, heldCard, plateX(), counterTop() + 120f + 40f * lift, 6f, 0f);
            glow(batch, plateX(), counterTop() + 140f, 300f, GOLD, 0.4f * lift);
        }
        batch.setColor(Color.WHITE);
        sprite(batch, cloche, plateX() + 120f * lift, counterTop() + 60f + 340f * lift, 22f, -25f * lift);
    }

    // -------------------------------------------------------------------------
    // La Dame en noir
    // -------------------------------------------------------------------------

    /** Le couloir du wagon-lit : les portes des compartiments, des lampes ({@code lamps} : allumées), la lune aux fenêtres. */
    private void drawCorridor(Batch batch, float lamps) {
        float width = worldWidth(), height = worldHeight();
        fill(batch, hex("1a0a14"), 1f);
        for (int i = 0; i < 5; i++) {
            float x = width * (0.02f + 0.2f * i);
            rect(batch, hex("0a0c1e"), x, height * 0.45f, width * 0.14f, height * 0.3f);
            float moon = (time * 300f + i * 170f) % (width * 0.14f);
            glow(batch, x + moon, height * 0.65f, 60f, hex("fff6d0"), 0.6f);
            rect(batch, VARNISH, x - 10f, height * 0.42f, width * 0.16f, 10f);
        }
        rect(batch, hex("2a0a1a"), -MARGIN, -MARGIN, width + 2f * MARGIN, height * 0.2f + MARGIN); // le tapis
        for (int i = 0; i < 4; i++) glow(batch, width * (0.12f + 0.25f * i), height * 0.85f, 160f, LAMP, 0.6f * lamps);
    }

    private void drawLady(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        drawCorridorAfterPrelude(batch);
        float appear = progress(LADY_IN, LADY_IN + 1.2f);
        float rise = gesture(VEIL_UP - 0.2f, VEIL_UP + 1.0f);
        pose(0f, 30f * rise, 0f, 0.04f * rise);
        drawKing(batch, cx(), kingY(), KING_SCALE, 1f - appear, appear);
        // Le brouillard, qui s'efface quand elle est là.
        float fog = 1f - progress(LADY_IN + 0.8f, LADY_IN + 2.0f);
        for (int k = 0; k < 6; k++) glow(batch, width * (0.1f + 0.16f * k) + MathUtils.sin(time + k) * 40f, height * 0.25f,
            420f, hex("8a80a0"), 0.35f * fog);
        // Ses yeux luisent sous la voilette qui se soulève.
        float eyes = progress(VEIL_UP, VEIL_UP + 0.2f) * pulse(6f, 0.6f);
        if (eyes > 0f) {
            for (float px : new float[] {13.5f, 20.5f}) {
                glow(batch, kingPixelX(px, cx(), KING_SCALE), kingPixelY(15.5f, kingY(), KING_SCALE), 120f, VIOLET, eyes);
            }
        }
        flash(batch, VIOLET, VEIL_UP, 0.2f, 0.3f);
    }

    /** Le couloir pendant l'entrée : les lampes s'éteignent une à une à partir de {@link #LAMPS_OUT}. */
    private void drawCorridorAfterPrelude(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        fill(batch, hex("1a0a14"), 1f);
        for (int i = 0; i < 5; i++) {
            float x = width * (0.02f + 0.2f * i);
            rect(batch, hex("0a0c1e"), x, height * 0.45f, width * 0.14f, height * 0.3f);
            glow(batch, x + (time * 300f + i * 170f) % (width * 0.14f), height * 0.65f, 60f, hex("fff6d0"), 0.6f);
            rect(batch, VARNISH, x - 10f, height * 0.42f, width * 0.16f, 10f);
        }
        rect(batch, hex("2a0a1a"), -MARGIN, -MARGIN, width + 2f * MARGIN, height * 0.2f + MARGIN);
        for (int i = 0; i < 4; i++) {
            float on = 1f - progress(LAMPS_OUT + i * 0.18f, LAMPS_OUT + i * 0.18f + 0.1f);
            if (on > 0f) glow(batch, width * (0.12f + 0.25f * i), height * 0.85f, 160f, LAMP, 0.6f * on);
        }
    }

    // -------------------------------------------------------------------------
    // Le Desperado
    // -------------------------------------------------------------------------

    private float roofY() { return worldHeight() * 0.3f; }
    private float gunX()  { return kingPixelX(30f, cx(), KING_SCALE); }
    private float gunY()  { return kingPixelY(12f, kingY(), KING_SCALE); }

    /** Le toit du fourgon blindé qui file sous la lune : rivets, et le paysage qui défile en traits. */
    private void drawRoof(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, hex("1a2a4a"), NIGHT);
        glow(batch, width * 0.8f, height * 0.8f, 260f, hex("fff6d0"), 0.8f);
        for (int i = 0; i < 18; i++) {                                     // la vitesse
            float y = height * (0.35f + 0.6f * ((i * 0.137f) % 1f));
            float x = width - ((time * 2400f + i * 413f) % (width + 600f));
            line(batch, hex("3a4a6a"), x, y, x + 300f, y, 4f);
        }
        rect(batch, hex("5a6070"), -MARGIN, -MARGIN, width + 2f * MARGIN, roofY() + MARGIN);
        rect(batch, hex("8a97a8"), -MARGIN, roofY() - 12f, width + 2f * MARGIN, 12f);
        for (int i = 0; i < 16; i++) rect(batch, hex("b8c0cc"), i * width / 16f + 20f, roofY() - 40f, 10f, 10f);
    }

    private void drawDesperado(Batch batch) {
        drawRoof(batch);
        float fall = Interpolation.pow2In.apply(progress(0f, LAND));
        float squash = gesture(LAND, LAND + 0.3f);
        float recoil = Math.max(gesture(SHOT_1, SHOT_1 + 0.25f), gesture(SHOT_2, SHOT_2 + 0.25f));
        pose(0f, worldHeight() * (1f - fall), 3f * recoil, -0.12f * squash);
        drawKing(batch, cx(), roofY(), KING_SCALE, 0f, 1f);
        if (recoil > 0.6f) glow(batch, gunX(), gunY() + 40f, 200f, hex("fff2c0"), recoil);
        flash(batch, hex("fff2c0"), SHOT_1, 0.1f, 0.3f);
        flash(batch, hex("fff2c0"), SHOT_2, 0.1f, 0.3f);
    }
}
