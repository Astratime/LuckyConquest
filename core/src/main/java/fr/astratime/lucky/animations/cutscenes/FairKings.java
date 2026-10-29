package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import fr.astratime.lucky.assets.FairDesertPortraits;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import java.util.Random;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les rois de la Fête foraine entrent en scène (choix d'Astra, 2026-10-24) :
 * <ul>
 *   <li>Monsieur Loyal : le chapiteau est noir, un projecteur s'allume sur la piste. Il fait claquer son
 *   fouet, quatre bannières tombent (Pique, Coeur, Trèfle, Carreau). Il salue : son chapeau quitte sa tête,
 *   fait un tour en l'air et retombe dessus.</li>
 *   <li>Conducteur fantôme : des rails dans le noir, deux phares approchent. Le train défonce une porte en
 *   carton peinte d'une tête de mort ; il fait sonner la cloche, les squelettes des wagons saluent.</li>
 *   <li>Roue de la Fortune : la grande roue dort, ses lumières s'allument une à une. Elle se défait : ses
 *   morceaux se rejoignent et forment la Roue. Ses yeux s'ouvrent, quatre cases apparaissent (Épée, Bouclier,
 *   Potion, Rien) ; elle roule vers toi.</li>
 *   <li>Roi des forains : des stands, des lanternes, des canards. Il surgit derrière son comptoir avec un
 *   maillet géant et frappe la machine à mesurer la force : la cloche sonne. Dans sa main, son 2 de Carreau.</li>
 * </ul>
 */
public class FairKings extends KingEntrance {

    /** Monsieur Loyal : le projecteur s'allume (décor), il apparaît, le fouet claque, les bannières tombent. */
    static final float SPOT_ON = 1.3f, LOYAL_IN = 0.3f, WHIP = 1.1f, BANNERS = 1.5f, HAT_UP = 3.6f, HAT_DOWN = 4.5f;
    /** Conducteur : la porte en carton vole en éclats, la cloche sonne. */
    static final float DOOR_BREAK = 0.35f, TRAIN_STOP = 1.4f, BELL = 3.6f;
    /** Roue : ses lumières s'allument (décor), ses morceaux se rejoignent, l'oeil s'ouvre, les cases, elle roule vers toi. */
    static final float LIGHTS_FROM = 0.2f, LIGHTS_TO = 1.8f, ASSEMBLED = 1.3f, EYE_OPEN = 1.4f, CELLS = 1.8f,
        ROLL_FROM = 2.3f, ROLL_TO = 3.2f;
    /** Les morceaux de la Roue : des carrés de {@value #PIECE} pixels du portrait. */
    private static final int PIECE = 4;
    /** Roi des forains : il surgit, il frappe, le palet monte, la cloche sonne. */
    static final float KING_POP = 0.5f, HIT = 3.5f, RING = 3.85f;

    private static final Color RED = hex("d8202c"), GOLD = hex("ffc93a"), SPOT = hex("fff2c0"), NIGHT = hex("0a0814");
    /** Monsieur Loyal : les lignes du haut de son portrait qui font son haut-de-forme. */
    private static final int HAT_ROWS = 10;
    /** Ses cheveux sous le chapeau : de la colonne 11 à 23, lignes 7 à 10 du portrait. */
    private static final String[] LOYAL_HAIR = {
        "....ooooo....", "..oohhhhhoo..", ".ohhhhhhhhho.", "ohhhhhhhhhhho"};
    private static final String[] SKULL = {
        ".oooooo.", "owwwwwwo", "owkwwkwo", "owkwwkwo", "owwwkwwo", ".owwwwo.", ".okwkwo.", "..oooo.."};
    private static final String[] DUCK = {
        "...oo....", "..oyyo...", "..oyko.o.", "...yyyooo", ".oyyyyyo.", "oyyyyyyo.", ".oooooo.."};
    private static final String[] BELL_ART = {
        "....o....", "...ogo...", "..ogggo..", "..oggwo..", ".ogggwgo.", ".ogggggo.", "ogggggggo", "ooooooooo"};

    private final TextureRegion skull, duck, bell;
    /** Monsieur Loyal : son chapeau (le haut de son portrait), le reste de lui, et ses cheveux sous le chapeau. */
    private TextureRegion loyalHat, loyalBody, loyalHair;
    /** Roi des forains : la vraie carte qu'il tient. */
    private TextureRegion heldCard;
    /** Roue : ses morceaux (image, place dans le portrait, nacelle de départ, départ, vrille). */
    private final Array<TextureRegion> pieces = new Array<>();
    private final FloatArray pieceData = new FloatArray();
    private final TextureRegion[] banners = new TextureRegion[4];
    private final TextureRegion[] cells = new TextureRegion[4];

    public FairKings(CutsceneKit kit, EnemyKind kind) {
        super(kit, kind);
        skull = art("owk", new Color[] {hex("0a0608"), hex("eae4d0"), hex("1a1018")}, SKULL);
        duck = art("oyk", new Color[] {hex("4a3008"), hex("ffd23a"), hex("1a1018")}, DUCK);
        bell = art("ogw", new Color[] {hex("4a3008"), GOLD, hex("fff0a0")}, BELL_ART);
        if (kind == EnemyKind.MONSIEUR_LOYAL) {
            loyalHat = new TextureRegion(king, 0, 0, king.getRegionWidth(), HAT_ROWS);
            loyalBody = new TextureRegion(king, 0, HAT_ROWS + 1, king.getRegionWidth(), king.getRegionHeight() - HAT_ROWS - 1);
            loyalHair = art("oh", new Color[] {hex("1a0f0f"), hex("3a2a1e")}, LOYAL_HAIR);
            String[] aces = {"cards/light/1-P.png", "cards/light/1-H.png", "cards/light/1-C.png", "cards/light/1-D.png"};
            for (int i = 0; i < 4; i++) banners[i] = gameCard(aces[i]);
        }
        if (kind == EnemyKind.ROUE_FORTUNE && kit.enemies() != null) {
            cells[0] = kit.symbol(EnemySymbol.SWORD);
            cells[1] = kit.symbol(EnemySymbol.SHIELD);
            cells[2] = kit.symbol(EnemySymbol.POTION);
        }
        if (kind == EnemyKind.ROUE_FORTUNE) cutPieces();
        if (kind == EnemyKind.ROI_FORAIN) heldCard = gameCard(FairBossScenes.FAIR_KING_CARD);
    }

    /** Coupe le portrait de la Roue en carrés, chacun parti d'une nacelle de la grande roue. */
    private void cutPieces() {
        int[][][] layers = {FairDesertPortraits.wheelLayer(0), FairDesertPortraits.wheelLayer(1), FairDesertPortraits.wheelLayer(2)};
        int width = king.getRegionWidth(), height = king.getRegionHeight();
        Random seed = new Random(7);
        for (int ty = 0; ty < height; ty += PIECE) {
            for (int tx = 0; tx < width; tx += PIECE) {
                int w = Math.min(PIECE, width - tx), h = Math.min(PIECE, height - ty);
                boolean filled = false;
                for (int y = ty; y < ty + h && !filled; y++) {
                    for (int x = tx; x < tx + w && !filled; x++) {
                        for (int[][] layer : layers) if (layer[y][x] != 0) { filled = true; break; }
                    }
                }
                if (!filled) continue;
                pieces.add(new TextureRegion(king, tx, ty, w, h));
                pieceData.addAll(tx + w / 2f, ty + h / 2f, seed.nextInt(12), seed.nextFloat() * 0.45f,
                    (seed.nextFloat() - 0.5f) * 1080f);
            }
        }
    }

    @Override
    protected void stepPrelude(float delta) {
        switch (kind) {
            case MONSIEUR_LOYAL -> { if (at(SPOT_ON)) rumble(0.15f, 4f); }
            case CONDUCTEUR -> { if (at(1.6f)) rumble(0.3f, 4f); }
            case ROUE_FORTUNE -> {
                for (int i = 0; i < 12; i++) {
                    if (at(LIGHTS_FROM + i * (LIGHTS_TO - LIGHTS_FROM) / 12f)) {
                        float[] p = cabin(i, worldWidth() / 2f, worldHeight() * 0.55f, worldHeight() * 0.32f, 0f);
                        sparks(p[0], p[1], 6, GOLD, 120f, 6f);
                    }
                }
            }
            default -> { }
        }
    }

    @Override
    protected void prelude(Batch batch) {
        switch (kind) {
            case MONSIEUR_LOYAL -> drawRing(batch, progress(SPOT_ON, SPOT_ON + 0.08f));
            case CONDUCTEUR -> drawTracks(batch, true);
            case ROUE_FORTUNE -> drawWheelNight(batch);
            default -> drawStalls(batch);
        }
    }

    @Override
    protected void step(float delta) {
        switch (kind) {
            case MONSIEUR_LOYAL -> {
                if (at(WHIP)) {
                    rumble(0.15f, 6f);
                    sparks(cx() + 360f, kingY() + 520f, 30, Color.WHITE, 500f, 10f);
                }
                for (int i = 0; i < 4; i++) if (at(BANNERS + 0.6f + i * 0.12f)) rumble(0.1f, 3f);
                if (at(HAT_DOWN)) sparks(cx(), kingY() + 42f * KING_SCALE, 18, GOLD, 300f, 10f);
            }
            case CONDUCTEUR -> {
                if (at(DOOR_BREAK)) {
                    rumble(0.4f, 12f);
                    debris(cx(), kingY() + 300f, 16, pixel, 26f, 900f);
                    for (Particle piece : parts) if (piece.region == pixel) piece.color.set(hex("c8a070"));
                    sparks(cx(), kingY() + 300f, 30, hex("fff2c0"), 600f, 14f);
                }
                if (at(TRAIN_STOP)) {
                    rumble(0.3f, 6f);
                    sparks(cx() - 300f, kingY() - 20f, 30, hex("ffb84a"), 500f, 10f);
                    sparks(cx() + 300f, kingY() - 20f, 30, hex("ffb84a"), 500f, 10f);
                }
                if (at(BELL)) sparks(cx() + 300f, kingY() + 560f, 20, GOLD, 300f, 10f);
            }
            case ROUE_FORTUNE -> {
                if (at(ASSEMBLED)) {
                    rumble(0.25f, 8f);
                    sparks(cx(), kingY() + worldHeight() * 0.18f + 19f * KING_SCALE * 0.55f, 40, GOLD, 500f, 12f);
                }
                if (at(ROLL_TO)) {
                    rumble(0.45f, 14f);
                    sparks(cx(), kingY(), 40, hex("ffe8a0"), 700f, 14f);
                }
            }
            default -> {
                if (at(KING_POP)) {
                    rumble(0.2f, 6f);
                    sparks(cx(), counterTop() + 120f, 24, GOLD, 500f, 12f);
                }
                if (at(HIT)) rumble(0.3f, 10f);
                if (at(RING)) {
                    rumble(0.25f, 6f);
                    sparks(testerX(), testerTop(), 40, GOLD, 600f, 14f);
                }
            }
        }
    }

    @Override
    protected void draw(Batch batch) {
        switch (kind) {
            case MONSIEUR_LOYAL -> drawLoyal(batch);
            case CONDUCTEUR -> drawConductor(batch);
            case ROUE_FORTUNE -> drawWheel(batch);
            default -> drawFairKing(batch);
        }
        drawParticles(batch, parts);
    }

    // -------------------------------------------------------------------------
    // Monsieur Loyal : la piste du chapiteau
    // -------------------------------------------------------------------------

    /** Le chapiteau dans le noir, la piste, et le projecteur ({@code spot} de 0 à 1). */
    private void drawRing(Batch batch, float spot) {
        float width = worldWidth(), height = worldHeight();
        fill(batch, NIGHT, 1f);
        for (int i = 0; i < 16; i++) {                                     // les bandes de la toile, à peine visibles
            Color stripe = i % 2 == 0 ? hex("1e0a10") : hex("120a14");
            float x = i * width / 16f;
            tmp.set(stripe);
            line(batch, tmp, x, height + MARGIN, width / 2f + (x - width / 2f) * 0.3f, height * 0.55f, width / 14f);
        }
        // La piste : un anneau rouge et or, en perspective.
        float ringY = height * 0.2f;
        batch.setColor(hex("5a1018"));
        batch.draw(soft, width * 0.1f, ringY - 90f, width * 0.8f, 180f);
        batch.setColor(hex("2a1208"));
        batch.draw(soft, width * 0.16f, ringY - 70f, width * 0.68f, 140f);
        if (spot <= 0f) return;
        additive(batch);                                                   // le cône du projecteur
        batch.setColor(SPOT.r, SPOT.g, SPOT.b, 0.16f * spot);
        for (int k = 0; k < 6; k++) {
            float half = 120f + k * 50f;
            batch.draw(pixel, cx() - half / 2f, ringY, half, height);
        }
        normal(batch);
        glow(batch, cx(), ringY, width * 0.5f, SPOT, 0.45f * spot);
    }

    private void drawLoyal(Batch batch) {
        float height = worldHeight();
        drawRing(batch, 1f);
        float appear = progress(LOYAL_IN, LOYAL_IN + 0.35f);
        float bow = gesture(HAT_UP - 0.2f, HAT_DOWN + 0.2f);
        pose(0f, -20f * bow, 0f, -0.08f * bow);                            // il salue, penché
        float up = progress(HAT_UP, HAT_DOWN);
        boolean hatOff = up > 0f && up < 1f;
        if (hatOff) {                                                      // tête nue : ses cheveux, sous le chapeau
            drawOnKing(batch, loyalBody, 0f, HAT_ROWS + 1, 1f, cx(), kingY(), KING_SCALE, 0f, 1f);
            drawOnKing(batch, loyalHair, 11f, HAT_ROWS - 3, 1f, cx(), kingY(), KING_SCALE, 0f, 1f);
        } else {
            drawKing(batch, cx(), kingY(), KING_SCALE, 1f - appear, appear);
        }
        // Le fouet qui claque, au bout de son bras.
        float whip = gesture(WHIP - 0.15f, WHIP + 0.2f);
        if (whip > 0f) {
            float x0 = cx() + 160f, y0 = kingY() + 200f;
            line(batch, hex("3a2a1e"), x0, y0, x0 + 260f * whip, y0 + 340f * whip, 8f);
            line(batch, hex("3a2a1e"), x0 + 260f * whip, y0 + 340f * whip, x0 + 380f * whip, y0 + 300f * whip, 5f);
        }
        // Quatre bannières aux As du jeu tombent du plafond.
        for (int i = 0; i < 4; i++) {
            float k = Interpolation.bounceOut.apply(progress(BANNERS + i * 0.12f, BANNERS + 0.6f + i * 0.12f));
            if (k <= 0f) continue;
            float x = worldWidth() * (i < 2 ? 0.1f + 0.2f * i : 0.7f + 0.2f * (i - 2)); // de part et d'autre de lui
            float bottom = MathUtils.lerp(height + 40f, height * 0.42f, k);
            rect(batch, i % 2 == 0 ? hex("2a0a3a") : hex("3a0a14"), x - 110f, bottom, 220f, height - bottom + MARGIN);
            rect(batch, GOLD, x - 110f, bottom, 220f, 10f);
            line(batch, hex("2a0a3a"), x - 110f, bottom, x, bottom - 60f, 4f);
            batch.setColor(Color.WHITE);
            sprite(batch, banners[i], x, bottom + 170f, 9f, 0f);
        }
        // Son chapeau quitte sa tête, fait un tour en l'air et retombe dessus.
        if (hatOff) {
            float x = kingPixelX(king.getRegionWidth() / 2f, cx(), KING_SCALE);
            float y = kingPixelY(HAT_ROWS / 2f, kingY(), KING_SCALE) + 420f * MathUtils.sin(up * MathUtils.PI);
            batch.setColor(Color.WHITE);
            sprite(batch, loyalHat, x, y, KING_SCALE, up * 360f);
        }
    }

    // -------------------------------------------------------------------------
    // Conducteur fantôme : les rails dans le noir
    // -------------------------------------------------------------------------

    /** Les rails qui filent vers le fond ; dans le décor, deux phares approchent. */
    private void drawTracks(Batch batch, boolean headlights) {
        float width = worldWidth(), height = worldHeight();
        fill(batch, hex("05040a"), 1f);
        float horizon = height * 0.55f;
        for (int k = 0; k < 14; k++) {                                     // traverses en perspective
            float t = k / 14f, y = MathUtils.lerp(-MARGIN, horizon, (float) Math.pow(t, 0.7));
            float half = MathUtils.lerp(width * 0.45f, 30f, t);
            rect(batch, hex("2a1e18"), cx() - half, y, half * 2f, 14f * (1f - t) + 2f);
        }
        line(batch, hex("8a97a8"), cx() - width * 0.32f, -MARGIN, cx() - 14f, horizon, 8f);
        line(batch, hex("8a97a8"), cx() + width * 0.32f, -MARGIN, cx() + 14f, horizon, 8f);
        if (!headlights) return;
        float near = Interpolation.pow2In.apply(progress(0f, 2f));
        float y = MathUtils.lerp(horizon, height * 0.3f, near), gap = MathUtils.lerp(20f, 380f, near);
        glow(batch, cx() - gap, y, 80f + 600f * near, hex("fff2c0"), 0.9f);
        glow(batch, cx() + gap, y, 80f + 600f * near, hex("fff2c0"), 0.9f);
    }

    private void drawConductor(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        drawTracks(batch, false);
        // La porte en carton, peinte d'une tête de mort, avant qu'elle vole en éclats.
        if (time < DOOR_BREAK) {
            rect(batch, hex("a8885a"), width * 0.25f, -MARGIN, width * 0.5f, height * 0.85f);
            batch.setColor(Color.WHITE);
            sprite(batch, skull, cx(), height * 0.45f, 40f, 0f);
        }
        // Le train : la locomotive et ses wagons, dont les squelettes saluent.
        float arrive = Interpolation.pow2Out.apply(progress(DOOR_BREAK - 0.1f, TRAIN_STOP));
        float base = MathUtils.lerp(-height * 0.4f, -MARGIN, arrive);
        rect(batch, hex("2a2a4a"), width * 0.2f, base, width * 0.6f, height * 0.26f);
        rect(batch, hex("1a1a30"), width * 0.2f, base + height * 0.26f, width * 0.6f, 18f);
        glow(batch, width * 0.24f, base + height * 0.12f, 300f, hex("fff2c0"), 0.8f);
        glow(batch, width * 0.76f, base + height * 0.12f, 300f, hex("fff2c0"), 0.8f);
        for (int w = 0; w < 2; w++) {                                      // les wagons, de chaque côté
            float x = w == 0 ? -MARGIN : width * 0.82f;
            rect(batch, hex("3a1a4a"), x, base, width * 0.2f, height * 0.2f);
            for (int s = 0; s < 2; s++) {
                float sx = x + width * (0.05f + 0.09f * s), wave = gesture(GESTURE + 0.2f * s, GESTURE + 0.9f + 0.2f * s);
                batch.setColor(Color.WHITE);
                sprite(batch, skull, sx, base + height * 0.24f + 10f * MathUtils.sin(time * 6f + s), 10f, 0f);
                if (wave > 0f) line(batch, hex("eae4d0"), sx + 50f, base + height * 0.22f, sx + 80f, base + height * 0.22f
                    + 120f * wave, 14f);
            }
        }
        float appear = progress(DOOR_BREAK + 0.2f, DOOR_BREAK + 0.8f);
        drawKing(batch, cx(), base + height * 0.27f, KING_SCALE * 0.9f, 0f, 0.75f * appear); // transparent : un fantôme
        // La cloche qui sonne.
        float swing = time >= BELL ? MathUtils.sin((time - BELL) * 18f) * 25f * (1f - progress(BELL, BELL + 1.2f)) : 0f;
        batch.setColor(Color.WHITE);
        sprite(batch, bell, cx() + 300f, base + height * 0.27f + 380f, 12f, swing);
        flash(batch, hex("fff2c0"), DOOR_BREAK, 0.25f, 0.5f);
    }

    // -------------------------------------------------------------------------
    // Roue de la Fortune : la grande roue dans la nuit
    // -------------------------------------------------------------------------

    /** @return la place de la nacelle {@code i} (sur 12) d'une roue centrée en ({@code x}, {@code y}), de rayon {@code r}. */
    private static float[] cabin(int i, float x, float y, float r, float turn) {
        float angle = 90f - i * 30f + turn;
        return new float[] {x + MathUtils.cosDeg(angle) * r, y + MathUtils.sinDeg(angle) * r};
    }

    private void drawWheelNight(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, hex("10142e"), hex("02030a"));
        for (int i = 0; i < 50; i++) rect(batch, hex("fff2a0"), (i * 211.7f) % width, height * 0.5f + (i * 97.3f) % (height * 0.5f), 4f, 4f);
        rect(batch, hex("0a1410"), -MARGIN, -MARGIN, width + 2f * MARGIN, height * 0.2f + MARGIN);
        float x = width / 2f, y = height * 0.55f, r = height * 0.32f;
        line(batch, hex("2a3048"), x, y, x - r * 0.6f, height * 0.2f, 14f);
        line(batch, hex("2a3048"), x, y, x + r * 0.6f, height * 0.2f, 14f);
        for (int i = 0; i < 12; i++) {
            float[] p = cabin(i, x, y, r, 0f);
            line(batch, hex("2a3048"), x, y, p[0], p[1], 6f);
            boolean lit = time >= LIGHTS_FROM + i * (LIGHTS_TO - LIGHTS_FROM) / 12f;
            rect(batch, lit ? (i % 2 == 0 ? hex("e86a8a") : GOLD) : hex("1a1a2a"), p[0] - 22f, p[1] - 22f, 44f, 36f);
            if (lit) glow(batch, p[0], p[1], 120f, GOLD, 0.5f);
        }
    }


    private void drawWheel(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, hex("10142e"), hex("02030a"));
        for (int i = 0; i < 50; i++) rect(batch, hex("fff2a0"), (i * 211.7f) % width, height * 0.5f + (i * 97.3f) % (height * 0.5f), 4f, 4f);
        rect(batch, hex("0a1410"), -MARGIN, -MARGIN, width + 2f * MARGIN, height * 0.2f + MARGIN);
        // Elle se décroche et roule vers toi : elle grandit et tourne.
        float roll = Interpolation.pow2In.apply(progress(ROLL_FROM, ROLL_TO));
        float spin = gesture(GESTURE, GESTURE + 1.6f);
        float scale = KING_SCALE * MathUtils.lerp(0.55f, 1f, roll);
        pose(0f, MathUtils.lerp(height * 0.18f, 0f, roll), -roll * 720f - spin * 360f, 0f);
        if (time < ASSEMBLED) drawPieces(batch, scale);                    // la grande roue se défait et la forme
        else drawKing(batch, cx(), kingY(), scale, 0f, 1f);
        float eye = progress(EYE_OPEN, EYE_OPEN + 0.3f) * (1f - roll);
        if (eye > 0f) {                                                    // ses yeux s'ouvrent
            glow(batch, kingPixelX(15f, cx(), scale), kingPixelY(18f, kingY(), scale), 160f, GOLD, 0.9f * eye * (1f - progress(CELLS, ROLL_FROM)));
            glow(batch, kingPixelX(20f, cx(), scale), kingPixelY(18f, kingY(), scale), 160f, GOLD, 0.9f * eye * (1f - progress(CELLS, ROLL_FROM)));
        }
        // Quatre cases apparaissent autour d'elle : Épée, Bouclier, Potion, Rien.
        float show = progress(CELLS, CELLS + 0.3f) * (1f - progress(ROLL_FROM, ROLL_FROM + 0.3f));
        if (show > 0f) {
            for (int i = 0; i < 4; i++) {
                float[] p = cabin(i * 3, cx(), kingY() + height * 0.18f + 19f * scale, 15f * scale + 70f, 0f);
                rect(batch, hex("1a1018"), p[0] - 60f, p[1] - 60f, 120f, 120f);
                rect(batch, hex("f6f0e2"), p[0] - 52f, p[1] - 52f, 104f, 104f);
                if (cells[i] != null) {
                    batch.setColor(1f, 1f, 1f, show);
                    sprite(batch, cells[i], p[0], p[1], 6f, 0f);
                }
            }
        }
    }

    /**
     * La grande roue du décor se défait : ses rayons s'effacent et, de chaque nacelle, des morceaux s'envolent
     * et se rejoignent pour former la Roue de la Fortune (en place à {@link #ASSEMBLED}).
     */
    private void drawPieces(Batch batch, float scale) {
        float height = worldHeight(), x0 = worldWidth() / 2f, y0 = height * 0.55f, r = height * 0.32f;
        float fade = 1f - progress(0f, 0.5f);
        if (fade > 0f) {                                                   // la grande roue, qui se défait
            tmp.set(hex("2a3048")).mul(1f, 1f, 1f, fade);
            line(batch, tmp, x0, y0, x0 - r * 0.6f, height * 0.2f, 14f);
            line(batch, tmp, x0, y0, x0 + r * 0.6f, height * 0.2f, 14f);
            for (int i = 0; i < 12; i++) {
                float[] p = cabin(i, x0, y0, r, 0f);
                line(batch, tmp, x0, y0, p[0], p[1], 6f);
            }
            for (int i = 0; i < 12; i++) {                                 // ses nacelles éclatent en morceaux
                float[] p = cabin(i, x0, y0, r, 0f);
                tmp.set(i % 2 == 0 ? hex("e86a8a") : GOLD).mul(1f, 1f, 1f, fade);
                rect(batch, tmp, p[0] - 22f, p[1] - 22f, 44f, 36f);
            }
        }
        for (int i = 0; i < pieces.size; i++) {
            float px = pieceData.get(i * 5), py = pieceData.get(i * 5 + 1);
            float[] from = cabin((int) pieceData.get(i * 5 + 2), x0, y0, r, 0f);
            float delay = pieceData.get(i * 5 + 3), twist = pieceData.get(i * 5 + 4);
            float k = Interpolation.pow2Out.apply(MathUtils.clamp((time - delay) / (ASSEMBLED - 0.45f), 0f, 1f));
            float tx = kingPixelX(px, cx(), scale), ty = kingPixelY(py, kingY(), scale);
            float x = MathUtils.lerp(from[0], tx, k), y = MathUtils.lerp(from[1], ty, k) + 120f * MathUtils.sin(k * MathUtils.PI);
            batch.setColor(1f, 1f, 1f, Math.min(1f, 0.4f + k));
            sprite(batch, pieces.get(i), x, y, scale, (1f - k) * twist);
        }
    }

    // -------------------------------------------------------------------------
    // Roi des forains : les stands et la machine à mesurer la force
    // -------------------------------------------------------------------------

    private float counterTop() { return worldHeight() * 0.3f; }
    private float testerX()    { return worldWidth() * 0.82f; }
    private float testerTop()  { return worldHeight() * 0.86f; }

    /** La rangée de stands, les lanternes qui se balancent, les canards en plastique sur l'eau. */
    private void drawStalls(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, hex("1a0a2a"), hex("05030a"));
        for (int s = 0; s < 5; s++) {
            float x = -MARGIN + s * width / 4.4f;
            rect(batch, hex("5a3418"), x, height * 0.2f, width / 5f, height * 0.5f);
            for (int k = 0; k < 8; k++) {
                rect(batch, k % 2 == 0 ? RED : hex("f6f0e2"), x + k * width / 40f, height * 0.7f, width / 40f, 60f);
            }
        }
        for (int l = 0; l < 7; l++) {                                      // lanternes qui se balancent
            float x = width * (0.08f + 0.14f * l), y = height * 0.85f, swing = 18f * MathUtils.sin(time * 2.2f + l);
            line(batch, hex("2a2a2a"), x, height + MARGIN, x + swing, y, 3f);
            rect(batch, l % 2 == 0 ? hex("ff8a3a") : hex("ffd23a"), x + swing - 18f, y - 30f, 36f, 40f);
            glow(batch, x + swing, y - 10f, 140f, hex("ffb84a"), 0.5f);
        }
        rect(batch, hex("1a4a8a"), -MARGIN, -MARGIN, width + 2f * MARGIN, height * 0.2f + MARGIN); // l'eau des canards
        for (int d = 0; d < 9; d++) {
            float x = ((d * 230f + time * 140f) % (width + 200f)) - 100f;
            batch.setColor(Color.WHITE);
            sprite(batch, duck, x, height * 0.15f + 6f * MathUtils.sin(time * 3f + d), 8f, 0f);
        }
    }

    private void drawFairKing(Batch batch) {
        drawStalls(batch);
        // La machine à mesurer la force : la colonne graduée, la cloche en haut.
        float x = testerX();
        rect(batch, hex("3a2a1e"), x - 30f, counterTop(), 60f, testerTop() - counterTop());
        for (int k = 0; k < 10; k++) rect(batch, k > 7 ? RED : GOLD, x - 22f, counterTop() + 20f + k * (testerTop() - counterTop()) / 10.5f, 44f, 10f);
        float ring = time >= RING ? MathUtils.sin((time - RING) * 20f) * 20f * (1f - progress(RING, RING + 1f)) : 0f;
        batch.setColor(Color.WHITE);
        sprite(batch, bell, x, testerTop() + 30f, 10f, ring);
        float puck = time < HIT ? 0f : time < RING ? Interpolation.pow2Out.apply(progress(HIT, RING))
            : 1f - Interpolation.pow2In.apply(progress(RING + 0.2f, RING + 0.9f));
        rect(batch, hex("dce4ee"), x - 26f, counterTop() + puck * (testerTop() - counterTop() - 30f), 52f, 30f);
        // Il surgit derrière son comptoir.
        float pop = Interpolation.swingOut.apply(progress(KING_POP, KING_POP + 0.4f));
        float swingHit = gesture(HIT - 0.4f, HIT + 0.25f);
        pose(60f * swingHit, 0f, -12f * swingHit, 0f);
        float kingBottom = MathUtils.lerp(counterTop() - 500f, counterTop() - 60f, pop);
        drawKing(batch, cx(), kingBottom, KING_SCALE, 0f, pop > 0f ? 1f : 0f);
        if (pop > 0f) drawOnKing(batch, heldCard, FairBossScenes.CARD_LEFT, FairBossScenes.CARD_TOP, FairBossScenes.CARD_PIXELS,
            cx(), kingBottom, KING_SCALE, 0f, 1f);                         // sa carte : un vrai 2 de Carreau
        // Le maillet géant, levé puis abattu.
        if (pop > 0f) {
            float angle = time < HIT - 0.4f ? 60f : time < HIT ? MathUtils.lerp(120f, -10f, progress(HIT - 0.4f, HIT)) : -10f;
            float hx = cx() + 180f, hy = counterTop() + 220f * pop;
            float ex = hx + MathUtils.cosDeg(angle) * 380f, ey = hy + MathUtils.sinDeg(angle) * 380f;
            line(batch, hex("8a5a2a"), hx, hy, ex, ey, 16f);
            rect(batch, hex("a82a1e"), ex - 60f, ey - 40f, 120f, 80f);
        }
        rect(batch, hex("6a3a1a"), -MARGIN, -MARGIN, worldWidth() + 2f * MARGIN, counterTop() + MARGIN); // le comptoir
        rect(batch, GOLD, -MARGIN, counterTop() - 12f, worldWidth() + 2f * MARGIN, 12f);
        flash(batch, GOLD, RING, 0.3f, 0.3f);
    }
}
