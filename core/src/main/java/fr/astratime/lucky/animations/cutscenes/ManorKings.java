package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les rois du Manoir hanté entrent en scène (10 lieux, Astra 2026-11-20) :
 * <ul>
 *   <li>Le Comte de Minuit : le salon de jeu, sa grande horloge et ses chandeliers. Un cercueil debout s'ouvre,
 *   il en sort ; l'horloge sonne minuit, une nuée de chauves-souris s'envole, ses yeux rougissent.</li>
 *   <li>La Bibliothécaire spectrale : des étagères à perte de vue, des livres qui volent en rond. Elle apparaît,
 *   pose un doigt sur sa bouche : les livres tombent d'un coup. L'un s'ouvre sur une vraie carte du jeu, le
 *   Valet de Pique.</li>
 *   <li>Le Fossoyeur : le cimetière dans la brume, une tombe fraîche. Une main sort de terre, il se hisse dehors,
 *   lève sa lanterne verte et plante sa pelle : la terre tremble.</li>
 *   <li>Le Marionnettiste : une petite scène de théâtre au grenier. Le rideau rouge s'ouvre, il descend des
 *   combles au bout de ses fils, et tire dessus : des pièces sautent de la scène comme des pantins.</li>
 * </ul>
 */
public class ManorKings extends KingEntrance {

    /** Comte : le cercueil s'ouvre, l'horloge sonne. */
    static final float LID_OPEN = 0.5f, CHIME = 3.4f;
    /** Bibliothécaire : elle apparaît, elle fait « chut », le livre s'ouvre. */
    static final float LIBRARIAN_IN = 0.3f, SHUSH = 3.4f, BOOK_OPEN = 3.9f;
    /** Fossoyeur : la main sort, il se hisse, il plante sa pelle. */
    static final float HAND_OUT = 0.3f, CLIMB = 0.9f, SHOVEL = 3.4f;
    /** Marionnettiste : le rideau s'ouvre, il descend, il tire ses fils. */
    static final float CURTAIN = 0.2f, DESCEND = 0.8f, PULL = 3.4f;

    private static final Color NIGHT = hex("0e0818"), MOON = hex("fff6d0"), CANDLE = hex("ffd27a"), BLOOD = hex("b0101e");
    private static final Color GHOST = hex("c8f0e8"), VIOLET = hex("b88aff"), GREEN = hex("7dff8a"), WOOD = hex("3e2a18");
    private static final Color STONE = hex("5a5a6e"), CURTAIN_RED = hex("8a1a2a"), THREAD = hex("e8e0f0"), GOLD = hex("ffc93a");
    private static final String[] BOOK = {
        "bbbbbbbbbbbb", "bppppppppppb", "bppppppppppb", "bppppppppppb", "bppppppppppb", "bbbbbbbbbbbb"};
    private static final String[] BAT = {"b.........b", "bb..b.b..bb", "bbbbbbbbbbb", ".bbbbbbbbb.", "...b...b..."};

    private final TextureRegion book, bat, heldCard, coinRegion;

    public ManorKings(CutsceneKit kit, EnemyKind kind) {
        super(kit, kind);
        book = art("bp", new Color[] {hex("5a2a1a"), hex("e8dcc0")}, BOOK);
        bat = art("b", new Color[] {hex("1a1018")}, BAT);
        heldCard = kind == EnemyKind.BIBLIOTHECAIRE ? gameCard("cards/light/11-P.png") : null;
        coinRegion = kind == EnemyKind.MARIONNETTISTE ? load("hud/coin.png") : null;
    }

    @Override
    protected void stepPrelude(float delta) {
        if (kind == EnemyKind.COMTE_MINUIT && at(1.4f)) rumble(0.15f, 3f);
        if (kind == EnemyKind.FOSSOYEUR && at(1.5f)) rumble(0.2f, 3f);
    }

    @Override
    protected void prelude(Batch batch) {
        switch (kind) {
            case COMTE_MINUIT -> drawSalon(batch, 0f);
            case BIBLIOTHECAIRE -> drawLibrary(batch, 1f);
            case FOSSOYEUR -> drawGraveyard(batch);
            default -> drawStage(batch, 0f);
        }
    }

    @Override
    protected void step(float delta) {
        switch (kind) {
            case COMTE_MINUIT -> {
                if (at(LID_OPEN)) {
                    rumble(0.3f, 8f);
                    sparks(cx(), kingY() + 20f * KING_SCALE, 30, GHOST, 400f, 12f);
                }
                if (at(CHIME)) {
                    rumble(0.5f, 10f);
                    for (int k = 0; k < 24; k++) {                       // la nuée de chauves-souris
                        Particle flyer = particle(cx(), kingY() + 26f * KING_SCALE, 30f + 120f * random.nextFloat(),
                            500f + 500f * random.nextFloat(), 1.6f, 0f);
                        flyer.region = bat;
                        flyer.size = 6f + 3f * random.nextFloat();
                        parts.add(flyer);
                    }
                }
            }
            case BIBLIOTHECAIRE -> {
                if (at(SHUSH)) {
                    rumble(0.4f, 8f);
                    sparks(cx(), kingY() + 20f * KING_SCALE, 40, VIOLET, 500f, 10f);
                }
                if (at(BOOK_OPEN)) sparks(bookX(), shelfFloor() + 120f, 30, CANDLE, 400f, 10f);
            }
            case FOSSOYEUR -> {
                if (at(HAND_OUT)) sparks(cx(), graveTop(), 30, hex("4a3420"), 400f, 12f);
                if (at(CLIMB + 0.6f)) rumble(0.3f, 8f);
                if (at(SHOVEL)) {
                    rumble(0.6f, 14f);
                    sparks(kingPixelX(30f, cx(), KING_SCALE), graveTop(), 50, hex("4a3420"), 700f, 14f);
                    sparks(kingPixelX(3f, cx(), KING_SCALE), kingPixelY(34f, kingY(), KING_SCALE), 30, GREEN, 400f, 10f);
                }
            }
            default -> {
                if (at(DESCEND + 0.8f)) rumble(0.2f, 5f);
                if (at(PULL)) {
                    rumble(0.4f, 10f);
                    if (coinRegion != null) debris(cx(), stageFloor(), 24, coinRegion, 2.5f, 1300f);
                    sparks(cx(), kingY() + 40f * KING_SCALE, 30, hex("ff5a3a"), 500f, 10f);
                }
            }
        }
    }

    @Override
    protected void draw(Batch batch) {
        switch (kind) {
            case COMTE_MINUIT -> drawCount(batch);
            case BIBLIOTHECAIRE -> drawLibrarian(batch);
            case FOSSOYEUR -> drawGravedigger(batch);
            default -> drawPuppeteer(batch);
        }
        drawParticles(batch, parts);
    }

    // -------------------------------------------------------------------------
    // Le Comte de Minuit
    // -------------------------------------------------------------------------

    /** Le salon de jeu : boiseries violettes, la table de jeu, deux chandeliers, la grande horloge ({@code hands} : minuit). */
    private void drawSalon(Batch batch, float hands) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, hex("1a0e24"), hex("2a1a3e"));
        for (int i = 0; i < 8; i++) rect(batch, hex("22142e"), i * width / 8f, 0f, 10f, height);
        // La grande horloge, à gauche, et son cadran.
        float clockX = width * 0.16f, clockY = height * 0.62f;
        rect(batch, hex("3e2a18"), clockX - 70f, height * 0.18f, 140f, height * 0.62f);
        glow(batch, clockX, clockY, 220f, MOON, 0.3f);
        rect(batch, hex("f6f0e2"), clockX - 50f, clockY - 50f, 100f, 100f);
        float angle = MathUtils.lerp(70f, 90f, hands);
        line(batch, hex("1a1018"), clockX, clockY, clockX + MathUtils.cosDeg(angle) * 40f, clockY + MathUtils.sinDeg(angle) * 40f, 6f);
        line(batch, hex("1a1018"), clockX, clockY, clockX + MathUtils.cosDeg(angle + 30f * (1f - hands)) * 30f,
            clockY + MathUtils.sinDeg(angle + 30f * (1f - hands)) * 30f, 8f);
        float swing = MathUtils.sin(time * 3f) * 18f;                                      // le balancier
        line(batch, GOLD, clockX, clockY - 60f, clockX + swing, clockY - 200f, 4f);
        // La table de jeu au feutre vert, et les chandeliers.
        rect(batch, hex("1a4a2a"), width * 0.3f, height * 0.12f, width * 0.6f, 60f);
        rect(batch, hex("3e2a18"), width * 0.3f, height * 0.1f, width * 0.6f, 20f);
        for (float x : new float[] {width * 0.34f, width * 0.86f}) {
            rect(batch, GOLD, x - 6f, height * 0.12f + 60f, 12f, 120f);
            for (int k = -1; k <= 1; k++) glow(batch, x + k * 40f, height * 0.12f + 200f, 70f * (0.85f + 0.15f
                * MathUtils.sin(time * 12f + k)), CANDLE, 0.7f);
        }
    }

    private void drawCount(Batch batch) {
        float chime = progress(CHIME - 0.2f, CHIME);
        drawSalon(batch, chime);
        // Le cercueil debout, au milieu : son couvercle bascule à LID_OPEN.
        float coffinW = 34f * KING_SCALE, coffinH = 44f * KING_SCALE;
        float left = cx() - coffinW / 2f, bottom = kingY() - 20f;
        rect(batch, hex("2a1a10"), left - 20f, bottom, coffinW + 40f, coffinH + 40f);
        rect(batch, hex("120a06"), left, bottom + 20f, coffinW, coffinH);
        float appear = progress(LID_OPEN, LID_OPEN + 0.8f);
        float spread = gesture(CHIME - 0.1f, CHIME + 1.0f);
        pose(0f, 0f, 0f, 0.12f * spread);
        drawKing(batch, cx(), kingY(), KING_SCALE, 1f - appear, appear);
        float lid = Interpolation.pow2In.apply(progress(LID_OPEN - 0.1f, LID_OPEN + 0.4f));
        if (lid < 1f) rect(batch, hex("4a2e18"), left - 20f - coffinW * lid, bottom, coffinW + 40f, coffinH + 40f);
        // Ses yeux rougissent quand minuit sonne.
        float eyes = progress(CHIME, CHIME + 0.3f) * pulse(5f, 0.6f);
        if (eyes > 0f) {
            for (float px : new float[] {13.5f, 20.5f}) {
                glow(batch, kingPixelX(px, cx(), KING_SCALE), kingPixelY(15.5f, kingY(), KING_SCALE), 120f, BLOOD, eyes);
            }
        }
        flash(batch, BLOOD, CHIME, 0.2f, 0.4f);
    }

    // -------------------------------------------------------------------------
    // La Bibliothécaire spectrale
    // -------------------------------------------------------------------------

    private float shelfFloor() { return worldHeight() * 0.16f; }
    private float bookX()      { return cx() + worldWidth() * 0.26f; }

    /** La bibliothèque : des étagères pleines, la lune derrière une haute fenêtre ; {@code flying} : les livres volent. */
    private void drawLibrary(Batch batch, float flying) {
        float width = worldWidth(), height = worldHeight();
        fill(batch, hex("1a0e14"), 1f);
        glow(batch, cx(), height * 0.75f, 360f, MOON, 0.25f);
        for (int col = 0; col < 6; col++) {
            float x = width * (0.02f + col * 0.165f);
            if (col == 2 || col == 3) continue;                                            // la fenêtre au milieu
            rect(batch, hex("3e2a18"), x, shelfFloor(), width * 0.14f, height * 0.8f);
            for (int row = 0; row < 6; row++) {
                float y = shelfFloor() + 20f + row * height * 0.13f;
                for (int b = 0; b < 9; b++) {
                    Color c = (b + row + col) % 3 == 0 ? hex("6a2a2a") : (b + row) % 3 == 1 ? hex("2a4a3a") : hex("3a2a5a");
                    rect(batch, c, x + 10f + b * width * 0.014f, y, width * 0.012f, height * 0.1f);
                }
            }
        }
        rect(batch, hex("0a0614"), width * 0.36f, height * 0.4f, width * 0.28f, height * 0.5f);
        rect(batch, hex("2a1a10"), -MARGIN, -MARGIN, width + 2f * MARGIN, shelfFloor() + MARGIN);
        if (flying <= 0f) return;
        batch.setColor(Color.WHITE);
        for (int k = 0; k < 8; k++) {                                                     // les livres qui volent en rond
            float angle = time * 70f + k * 45f;
            float x = cx() + MathUtils.cosDeg(angle) * width * 0.32f;
            float y = height * 0.55f + MathUtils.sinDeg(angle) * height * 0.22f;
            sprite(batch, book, x, y, 6f, angle);
        }
    }

    private void drawLibrarian(Batch batch) {
        float height = worldHeight();
        float falling = progress(SHUSH, SHUSH + 0.5f);
        drawLibrary(batch, 0f);
        float appear = progress(LIBRARIAN_IN, LIBRARIAN_IN + 1.2f);
        float shush = gesture(SHUSH - 0.2f, SHUSH + 0.8f);
        pose(0f, 10f * shush, 0f, -0.04f * shush);
        drawKing(batch, cx(), kingY(), KING_SCALE, 1f - appear, appear);
        batch.setColor(Color.WHITE);
        for (int k = 0; k < 8; k++) {                                                     // ils tournent, puis tombent d'un coup
            float angle = Math.min(time, SHUSH) * 70f + k * 45f;
            float x = cx() + MathUtils.cosDeg(angle) * worldWidth() * 0.32f;
            float y = height * 0.55f + MathUtils.sinDeg(angle) * height * 0.22f;
            y = MathUtils.lerp(y, shelfFloor() + 20f, Interpolation.bounceOut.apply(falling));
            if (k == 0) continue;                                                          // le livre qui s'ouvre
            sprite(batch, book, x, y, 6f, angle * (1f - falling));
        }
        float open = Interpolation.pow2Out.apply(progress(BOOK_OPEN, BOOK_OPEN + 0.4f));
        sprite(batch, book, bookX(), shelfFloor() + 40f, 7f, 0f);
        if (open > 0f && heldCard != null) {
            batch.setColor(Color.WHITE);
            sprite(batch, heldCard, bookX(), shelfFloor() + 100f + 80f * open, 5f * open, 0f);
            glow(batch, bookX(), shelfFloor() + 160f, 260f, CANDLE, 0.4f * open);
        }
        flash(batch, VIOLET, SHUSH, 0.2f, 0.35f);
    }

    // -------------------------------------------------------------------------
    // Le Fossoyeur
    // -------------------------------------------------------------------------

    private float graveTop() { return worldHeight() * 0.22f; }

    /** Le cimetière : nuit brumeuse, tombes et croix, un arbre mort, la terre fraîche d'une tombe ouverte. */
    private void drawGraveyard(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        gradient(batch, hex("0e0a18"), hex("2a2a40"));
        glow(batch, width * 0.82f, height * 0.78f, 300f, MOON, 0.8f);
        for (int i = 0; i < 7; i++) {
            float x = width * (0.05f + 0.14f * i), h = 120f + (i * 37 % 60);
            if (Math.abs(x - cx()) < width * 0.12f) continue;
            rect(batch, STONE, x - 40f, graveTop(), 80f, h);
            glow(batch, x, graveTop() + h, 80f, STONE, 0.4f);
        }
        line(batch, WOOD, width * 0.1f, graveTop(), width * 0.12f, height * 0.7f, 18f);
        line(batch, WOOD, width * 0.115f, height * 0.55f, width * 0.2f, height * 0.68f, 10f);
        rect(batch, hex("1a1410"), -MARGIN, -MARGIN, width + 2f * MARGIN, graveTop() + MARGIN);
        rect(batch, hex("4a3420"), cx() - 260f, graveTop() - 30f, 520f, 40f);             // la terre fraîche
        for (int k = 0; k < 6; k++) glow(batch, width * (0.1f + 0.17f * k) + MathUtils.sin(time * 0.6f + k) * 60f,
            graveTop() + 20f, 420f, hex("6a6a88"), 0.25f);
    }

    private void drawGravedigger(Batch batch) {
        drawGraveyard(batch);
        float climb = Interpolation.pow2Out.apply(progress(CLIMB, CLIMB + 1.2f));
        float plant = gesture(SHOVEL - 0.15f, SHOVEL + 0.5f);
        pose(0f, -kingYSpan() * (1f - climb), -5f * plant, -0.08f * plant);
        drawKing(batch, cx(), kingY(), KING_SCALE, 0f, climb > 0f ? 1f : 0f);
        // Devant lui, la terre de la tombe cache ses jambes tant qu'il se hisse.
        rect(batch, hex("1a1410"), -MARGIN, -MARGIN, worldWidth() + 2f * MARGIN, graveTop() - 30f + MARGIN);
        float hand = progress(HAND_OUT, HAND_OUT + 0.3f) * (1f - progress(CLIMB, CLIMB + 0.3f));
        if (hand > 0f) {                                                                  // la main qui sort de terre
            rect(batch, hex("b0a8c0"), cx() - 30f, graveTop() - 30f, 60f, 140f * hand);
            for (int k = 0; k < 4; k++) rect(batch, hex("b0a8c0"), cx() - 30f + k * 18f, graveTop() - 30f + 140f * hand, 10f, 40f * hand);
        }
        float lantern = progress(CLIMB + 1.2f, CLIMB + 1.6f);
        if (lantern > 0f) glow(batch, kingPixelX(3f, cx(), KING_SCALE), kingPixelY(34f, kingY(), KING_SCALE), 260f, GREEN,
            0.5f * lantern * pulse(4f, 0.7f));
        flash(batch, GREEN, SHOVEL, 0.2f, 0.3f);
    }

    /** @return de combien il est encore sous terre au début : toute sa hauteur. */
    private float kingYSpan() { return king.getRegionHeight() * KING_SCALE; }

    // -------------------------------------------------------------------------
    // Le Marionnettiste
    // -------------------------------------------------------------------------

    private float stageFloor() { return worldHeight() * 0.2f; }

    /** La petite scène du grenier : poutres, planches, le rideau rouge ({@code open} : 0 fermé, 1 ouvert). */
    private void drawStage(Batch batch, float open) {
        float width = worldWidth(), height = worldHeight();
        fill(batch, hex("140c0a"), 1f);
        for (int i = 0; i < 5; i++) line(batch, WOOD, i * width / 4f, height, width / 2f, height * 0.7f, 20f); // la charpente
        glow(batch, cx(), height * 0.5f, 500f, CANDLE, 0.25f);
        rect(batch, hex("5a3a20"), -MARGIN, -MARGIN, width + 2f * MARGIN, stageFloor() + MARGIN);
        for (int k = 0; k < 12; k++) rect(batch, hex("3e2a18"), k * width / 12f, stageFloor() - 6f, 4f, 6f);
        float half = width * 0.5f * (1f - 0.85f * open);
        rect(batch, CURTAIN_RED, -MARGIN, stageFloor(), half + MARGIN, height);
        rect(batch, CURTAIN_RED, width - half, stageFloor(), half + MARGIN, height);
        for (int k = 0; k < 6; k++) {
            rect(batch, hex("5a0a18"), half - 40f - k * half / 6f, stageFloor(), 8f, height);
            rect(batch, hex("5a0a18"), width - half + 32f + k * half / 6f, stageFloor(), 8f, height);
        }
        rect(batch, hex("c4891e"), -MARGIN, height * 0.88f, width + 2f * MARGIN, 24f);    // la frise dorée
    }

    private void drawPuppeteer(Batch batch) {
        float open = Interpolation.pow2Out.apply(progress(CURTAIN, CURTAIN + 0.8f));
        drawStage(batch, open);
        float down = Interpolation.bounceOut.apply(progress(DESCEND, DESCEND + 1.0f));
        float yank = gesture(PULL - 0.1f, PULL + 0.4f);
        float top = worldHeight() * 1.1f;
        float y = MathUtils.lerp(top, kingY(), down) + 40f * yank;
        pose(0f, 0f, 0f, 0.06f * yank);
        drawKing(batch, cx(), y, KING_SCALE, 0f, down > 0f ? 1f : 0f);
        // Les fils qui le tiennent depuis les combles.
        for (float px : new float[] {4f, 30f}) {
            float x = kingPixelX(px, cx(), KING_SCALE);
            line(batch, THREAD, x, kingPixelY(2f, y, KING_SCALE), x, worldHeight() + MARGIN, 3f);
        }
        flash(batch, hex("ff5a3a"), PULL, 0.15f, 0.3f);
    }
}
