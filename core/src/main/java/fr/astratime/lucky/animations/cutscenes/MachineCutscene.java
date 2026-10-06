package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;

/**
 * Avant la Machine Originelle (chapitre 6) : on traverse les étages de la Tour
 * en montant ; dans chacun, un ancien boss figé dans le verre (la Comète, la
 * Reine, l'Éclat, le Prétendant, la Maison). Puis le sommet, dans les étoiles :
 * on monte le long de la machine, immense, entre câbles et engrenages, des
 * pièces coulent dans ses tuyaux comme du sang. Une fente s'ouvre au sommet,
 * une seule pièce tombe dedans. Silence. Le levier descend, tout seul,
 * lentement. Cinq rouleaux s'allument un par un, le sol tremble. Les rouleaux
 * tournent : on voit passer les symboles de tous les chapitres. Ils s'arrêtent
 * sur cinq yeux, qui te regardent. Fondu blanc.
 */
public class MachineCutscene extends Cutscene {

    /** On monte à travers les étages de la Tour (voir le son cutscene/machine), puis on passe au sommet. */
    public static final float FLOORS_END  = 1.5f;
    /** La caméra a fini de monter le long de la machine. */
    public static final float CLIMB_END   = 4.5f;
    /** La fente du sommet s'ouvre, une pièce y tombe ; puis le silence. */
    public static final float SLOT_OPEN   = 4.4f;
    public static final float COIN_DROP   = 4.65f;
    public static final float COIN_IN     = 5.2f;
    /** Le levier descend. */
    public static final float LEVER_START = 5.9f;
    public static final float LEVER_DOWN  = 7.2f;
    /** Les rouleaux s'allument, un toutes les {@link #LIGHT_STEP} secondes. */
    public static final float LIGHT_START = 7.35f;
    public static final float LIGHT_STEP  = 0.2f;
    /** Les rouleaux tournent. */
    public static final float SPIN_START  = 8.3f;
    /** Ils s'arrêtent, un toutes les {@link #STOP_STEP} secondes. */
    public static final float STOP_START  = 9.5f;
    public static final float STOP_STEP   = 0.14f;
    public static final float COVER_START = 10.5f;
    public static final float COVER_FULL  = 11.2f;

    private static final int   REELS = 5;
    private static final float U     = 14f;          // un « pixel » de la machine, à l'écran
    /** Les symboles de tous les chapitres, qui défilent sur les rouleaux. */
    private static final EnemySymbol[] PARADE = {EnemySymbol.THORNS, EnemySymbol.LOADED_DIE, EnemySymbol.MIRROR,
        EnemySymbol.FAKE_MONEY, EnemySymbol.TAX, EnemySymbol.FANG, EnemySymbol.ZERO, EnemySymbol.HOURGLASS,
        EnemySymbol.PREDICTION, EnemySymbol.BANKRUPTCY, EnemySymbol.RAGE, EnemySymbol.INTEREST, EnemySymbol.ALL_IN,
        EnemySymbol.DUEL, EnemySymbol.NEW_RULE};
    /** Les anciens boss, figés dans le verre, un par étage (du bas vers le haut). */
    private static final EnemyKind[] FROZEN = {EnemyKind.COMETE, EnemyKind.REINE, EnemyKind.ECLAT,
        EnemyKind.PRETENDANT, EnemyKind.MAISON};

    private static final Color OUT = c("140a0a"), GOLD = c("ffc93a"), GOLD_LIGHT = c("ffe58a"), GOLD_DARK = c("b8801f");
    private static final Color GLASS = c("f4ecdc"), RED = c("e0283a"), SKY_TOP = c("05071a"), SKY_LOW = c("2b1a4a");
    private static final Color STONE = c("3b3550"), STONE_DARK = c("262238");

    private static final Color NIGHT_LOW = c("5a2c5e"), HOT_FLAME = c("fff2b0");
    private static final Color ICE = c("9fd8ff"), PIPE = c("6a4a1a"), WALL = c("1a1626");

    private final Starfield stars = new Starfield(200, 0f);
    private final Color low = new Color();
    protected final TextureRegion[] parade = new TextureRegion[PARADE.length + 1];
    private final TextureRegion[] frozen = new TextureRegion[FROZEN.length];
    private final TextureRegion   eye, gem;
    protected final TextureRegion coin;
    private final float[] reelSpin = new float[REELS];
    /** Pour la fin du chapitre : la machine telle qu'au combat (levier baissé, tout allumé, cinq yeux). */
    protected boolean settled;
    /** Assombrit la machine (1 = normale) : elle s'éteint en s'effondrant. */
    protected float   bright = 1f;
    /** Paupières sur les cinq yeux des rouleaux (0 ouvert, 1 fermé) : ils se ferment à la fin du chapitre. */
    protected final float[] lids = new float[REELS];
    private float nextTremble;
    private boolean clunk, clink;
    private int stoppedCount;

    public MachineCutscene(CutsceneKit kit) {
        this(kit, "machine");
    }

    protected MachineCutscene(CutsceneKit kit, String sound) {
        super(kit.settings(), kit.shake(), kit.sound(sound));
        Color[] eyeColors = {OUT, Color.WHITE, c("e0283a"), c("ffc93a"), c("6e0f1e")};
        eye = art("owrgd", eyeColors,
            "...oooooo...",
            ".oowwwwwwoo.",
            "owwwggggwwwo",
            "owwgrrrrgwwo",
            "owwgrddrgwwo",
            "owwgrrrrgwwo",
            "owwwggggwwwo",
            ".oowwwwwwoo.",
            "...oooooo...");
        gem = art("ow", new Color[] {OUT, Color.WHITE},
            "..o..",
            ".owo.",
            "owwwo",
            ".owo.",
            "..o..");
        for (int i = 0; i < PARADE.length; i++) parade[i] = kit.symbol(PARADE[i]);
        parade[PARADE.length] = eye;                    // l'œil, sur lequel les rouleaux s'arrêtent
        for (int i = 0; i < FROZEN.length; i++) frozen[i] = kit.portrait(FROZEN[i]);
        coin = load("hud/coin.png");
    }

    @Override protected float coverStart() { return COVER_START; }
    @Override protected float coverFull()  { return COVER_FULL; }

    @Override
    protected void reset() {
        for (int i = 0; i < REELS; i++) reelSpin[i] = settled ? PARADE.length : i * 3f;
        nextTremble = LIGHT_START;
        clunk = clink = false;
        stoppedCount = 0;
    }

    @Override
    protected void simulate(float delta) {
        if (!clink && time >= COIN_IN) {
            clink = true;
            rumble(0.1f, 3f);
        }
        if (!clunk && time >= LEVER_DOWN) {
            clunk = true;
            rumble(0.35f, 10f);
        }
        if (time >= nextTremble && time < STOP_START + REELS * STOP_STEP) {
            rumble(0.2f, 2.5f + 4f * progress(LIGHT_START, STOP_START));
            nextTremble += 0.18f;
        }
        for (int i = 0; i < REELS; i++) {
            float stopAt = STOP_START + i * STOP_STEP;
            if (time >= SPIN_START && time < stopAt) {
                reelSpin[i] += MathUtils.lerp(2f, 22f, progress(SPIN_START, SPIN_START + 0.6f)) * delta;
            } else if (time >= stopAt && i >= stoppedCount) {
                reelSpin[i] = PARADE.length;              // pile sur l'œil
                stoppedCount = i + 1;
                rumble(0.12f, 6f);
            }
        }
    }

    @Override
    protected void drawScene(Batch batch) {
        if (time < FLOORS_END) {
            drawFloors(batch);
            fadeFromBlack(batch, 0.5f);
            fadeThroughBlack(batch, FLOORS_END, 0.25f);
            return;
        }
        float width = worldWidth(), height = worldHeight();
        // La caméra monte : le décor descend.
        float climb = Interpolation.pow2.apply(progress(FLOORS_END, CLIMB_END));
        float camera = MathUtils.lerp(-height * 0.75f, 0f, climb);
        drawSky(batch, 0f);
        float cx = width / 2f;
        float baseY = height * 0.04f - camera;           // bas de la machine à l'écran
        // Halo derrière la machine.
        float lit = progress(LIGHT_START, LIGHT_START + REELS * LIGHT_STEP);
        glow(batch, cx, baseY + 30f * U, 140f * U, c("6a4aff"), 0.25f);
        glow(batch, cx, baseY + 30f * U, 110f * U, GOLD, 0.15f + 0.35f * lit);
        // Rayons de lumière quand tout s'allume.
        if (lit > 0f) {
            additive(batch);
            for (int i = 0; i < 12; i++) {
                batch.setColor(1f, 0.85f, 0.4f, 0.12f * lit);
                batch.draw(trail, cx, baseY + 30f * U - 20f, 0f, 20f, width, 40f, 1f, 1f, i * 30f + time * 8f);
            }
            normal(batch);
        }
        drawPipes(batch, cx, baseY);
        drawTower(batch, cx, baseY);
        drawMachine(batch, cx, baseY);
        drawSlot(batch, cx, baseY);
        flash(batch, Color.WHITE, LEVER_DOWN, 0.15f, 0.25f);
        fadeThroughBlack(batch, FLOORS_END, 0.25f);
    }

    /**
     * Les étages de la Tour, qu'on traverse en montant : murs de pierre, et à
     * chaque étage une vitrine où un ancien boss est figé dans le verre.
     */
    private void drawFloors(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        float floor = height * 0.62f;                                  // hauteur d'un étage à l'écran
        float rise = Interpolation.pow2Out.apply(progress(0f, FLOORS_END)) * floor * (FROZEN.length - 1.2f);
        fill(batch, WALL, 1f);
        float u = height / 80f, brickW = 9f * u, brickH = 4.5f * u;
        float shift = rise % (2f * brickH);
        for (int row = -2; row * brickH < height + 2f * MARGIN; row++) {     // briques qui défilent
            float y = row * brickH - shift;
            for (float x = -MARGIN + (Math.floorMod(row, 2)) * brickW / 2f; x < width + MARGIN; x += brickW) {
                batch.setColor(STONE_DARK.r, STONE_DARK.g, STONE_DARK.b, 1f);
                batch.draw(pixel, x + u * 0.4f, y + u * 0.4f, brickW - u * 0.8f, brickH - u * 0.8f);
            }
        }
        for (int i = 0; i < FROZEN.length; i++) {
            float base = height * 0.18f + i * floor - rise;            // le plancher de l'étage
            if (base > height + MARGIN || base + floor < -MARGIN) continue;
            batch.setColor(STONE.r, STONE.g, STONE.b, 1f);             // plancher
            batch.draw(pixel, -MARGIN, base - 3f * u, width + 2f * MARGIN, 3f * u);
            batch.setColor(OUT);
            batch.draw(pixel, -MARGIN, base - 4f * u, width + 2f * MARGIN, u);
            float x = width * (i % 2 == 0 ? 0.36f : 0.64f);
            drawShowcase(batch, frozen[i], x, base, u, i);
            for (int side = -1; side <= 1; side += 2) {                // torches de part et d'autre
                float tx = x + side * 26f * u, ty = base + 24f * u;
                float flicker = 0.75f + 0.25f * MathUtils.sin(time * 17f + i * 3f + side);
                glow(batch, tx, ty, 26f * u, c("ff9a3a"), 0.35f * flicker);
                batch.setColor(OUT);
                batch.draw(pixel, tx - 0.8f * u, ty - 6f * u, 1.6f * u, 6f * u);
                glow(batch, tx, ty + u, 5f * u, HOT_FLAME, 0.9f * flicker);
            }
        }
        glow(batch, width / 2f, height, width * 1.2f, ICE, 0.12f);    // la lumière du sommet, au-dessus
    }

    /** Une vitrine : le boss {@code boss} figé dans un bloc de verre bleuté, sur son socle. */
    private void drawShowcase(Batch batch, TextureRegion boss, float x, float base, float u, int index) {
        float w = 36f * u, h = 40f * u, bottom = base + 2f * u;
        batch.setColor(OUT);
        batch.draw(pixel, x - w / 2f - 2f * u, base - u, w + 4f * u, 3f * u);       // socle
        batch.setColor(GOLD_DARK);
        batch.draw(pixel, x - w / 2f - u, base, w + 2f * u, 2f * u);
        batch.setColor(c("0d1a2e"));
        batch.draw(pixel, x - w / 2f, bottom, w, h);
        float scale = (h - 6f * u) / Math.max(boss.getRegionWidth(), boss.getRegionHeight());
        float pw = boss.getRegionWidth() * scale;
        batch.setColor(0.45f, 0.65f, 0.95f, 1f);                    // figé : couleurs glacées
        batch.draw(boss, x - pw / 2f, bottom + 2f * u, pw, boss.getRegionHeight() * scale);
        batch.setColor(ICE.r, ICE.g, ICE.b, 0.22f);                 // le verre
        batch.draw(pixel, x - w / 2f, bottom, w, h);
        additive(batch);
        for (int k = 0; k < 2; k++) {                               // reflets en biais
            float sx = x - w / 2f + (0.2f + 0.45f * k) * w;
            batch.setColor(1f, 1f, 1f, 0.12f - 0.04f * k);
            batch.draw(pixel, sx, bottom, 0f, 0f, (2.5f - k) * u, h * 1.05f, 1f, 1f, -18f);
        }
        float shine = MathUtils.clamp(1f - Math.abs(((time * 0.9f + index * 0.37f) % 1.4f) - 0.7f) * 4f, 0f, 1f);
        batch.setColor(1f, 1f, 1f, 0.25f * shine);
        batch.draw(pixel, x - w / 2f + ((time * 0.9f + index * 0.37f) % 1.4f) / 1.4f * w, bottom, 0f, 0f, 2f * u, h, 1f, 1f, -18f);
        normal(batch);
        batch.setColor(ICE);                                        // le cadre
        batch.draw(pixel, x - w / 2f - u, bottom, u, h);
        batch.draw(pixel, x + w / 2f, bottom, u, h);
        batch.draw(pixel, x - w / 2f - u, bottom + h, w + 2f * u, u);
        glow(batch, x, bottom + h / 2f, w * 1.6f, ICE, 0.15f);
    }

    /**
     * Câbles, engrenages et tuyaux le long de la machine : des pièces y
     * coulent vers le haut, par à-coups, comme du sang.
     */
    private void drawPipes(Batch batch, float cx, float baseY) {
        float bottom = baseY + 3f * U, h = 44f * U, w = 72f * U;
        for (int side = -1; side <= 1; side += 2) {
            for (int k = 0; k < 2; k++) {
                float x = cx + side * (w / 2f + (6f + 7f * k) * U);
                float joinY = bottom + h * (0.25f + 0.4f * k);              // où le tuyau entre dans la machine
                float from = baseY - 70f * U;
                rect(batch, OUT, x - 2.5f * U, from, 5f * U, joinY - from + 2.5f * U);
                rect(batch, PIPE, x - 1.5f * U, from, 3f * U, joinY - from + 1.5f * U);
                float edge = cx + side * w / 2f;
                float left = Math.min(x, edge), right = Math.max(x, edge);
                rect(batch, OUT, left - 2.5f * U, joinY - 2.5f * U, right - left + 5f * U, 5f * U);
                rect(batch, PIPE, left - 1.5f * U, joinY - 1.5f * U, right - left + 3f * U, 3f * U);
                for (float ry = from + 10f * U; ry < joinY - 4f * U; ry += 16f * U) {    // colliers
                    rect(batch, GOLD_DARK, x - 3f * U, ry, 6f * U, 1.5f * U);
                }
                // Les pièces montent par à-coups : un battement par seconde.
                float beat = time * 1.4f + k * 0.5f;
                float flow = (MathUtils.floor(beat) + Interpolation.pow3Out.apply(beat - MathUtils.floor(beat))) * 9f * U;
                for (float py = from + flow % (9f * U); py < joinY - 2f * U; py += 9f * U) {
                    batch.setColor(GOLD.r * bright, GOLD.g * bright, GOLD.b * bright, 1f);
                    batch.draw(coin, x - 1.2f * U, py, 2.4f * U, 2.4f * U);
                }
            }
            // Un engrenage, plus loin, et un câble qui pend vers lui.
            float gx = cx + side * (w / 2f + 19f * U), gy = baseY + 12f * U;
            rect(batch, OUT, gx + side * 3f * U - 0.5f * U, gy + 13f * U, U, 60f * U);
            gear(batch, gx, gy, 5.5f * U, side * time * 40f);
            gear(batch, gx + side * 3f * U, gy + 13f * U, 3.5f * U, -side * time * 65f);
        }
    }

    /** Un engrenage de rayon {@code r}, tourné de {@code angle} degrés. */
    private void gear(Batch batch, float x, float y, float r, float angle) {
        for (int pass = 0; pass < 2; pass++) {
            Color color = pass == 0 ? OUT : GOLD_DARK;
            float grow = pass == 0 ? U : 0f;
            batch.setColor(color.r * bright, color.g * bright, color.b * bright, 1f);
            for (int k = 0; k < 4; k++) {                               // huit dents
                float len = 2f * r + 2f * grow, thick = 0.45f * r + grow;
                batch.draw(pixel, x - len / 2f, y - thick / 2f, len / 2f, thick / 2f, len, thick, 1f, 1f, angle + k * 45f);
            }
            float body = 1.6f * r + grow;
            batch.draw(pixel, x - body / 2f, y - body / 2f, body / 2f, body / 2f, body, body, 1f, 1f, angle + 22.5f);
        }
        rect(batch, OUT, x - 0.25f * r, y - 0.25f * r, 0.5f * r, 0.5f * r);
    }

    /** La fente au sommet : elle s'ouvre, une seule pièce tombe dedans (clinc), puis plus rien. */
    private void drawSlot(Batch batch, float cx, float baseY) {
        if (time < SLOT_OPEN) return;
        float bottom = baseY + 3f * U, h = 44f * U;
        float sx = cx + 12f * U, sy = bottom + h + 5.5f * U;               // sur le 2e gradin du dôme
        float open = Interpolation.pow2Out.apply(progress(SLOT_OPEN, SLOT_OPEN + 0.25f));
        float sw = 7f * U * open;
        rect(batch, OUT, sx - sw / 2f - 0.5f * U, sy - U, sw + U, 2f * U);
        rect(batch, c("05030a"), sx - sw / 2f, sy - 0.5f * U, sw, U);
        if (time >= COIN_IN) {                                              // la pièce est passée : la fente luit
            glow(batch, sx, sy, 16f * U, GOLD, 0.6f * Math.max(0f, 1f - (time - COIN_IN) / 0.8f) * bright);
            return;
        }
        if (time < COIN_DROP) return;
        float k = Interpolation.pow2In.apply(progress(COIN_DROP, COIN_IN));
        float y = MathUtils.lerp(worldHeight() + 40f, sy + 2f * U, k);
        float turn = Math.abs(MathUtils.cos(time * 9f));                   // elle tourne sur elle-même
        float size = 6f * U;
        glow(batch, sx, y, 3f * size, GOLD, 0.35f);
        batch.setColor(Color.WHITE);
        batch.draw(coin, sx - size * Math.max(0.15f, turn) / 2f, y - size / 2f, size * Math.max(0.15f, turn), size);
    }

    /** Le ciel étoilé ; {@code night} (0 à 1) le fait virer à la nuit du chapitre 1, à l'horizon rose. */
    protected void drawSky(Batch batch, float night) {
        gradient(batch, low.set(SKY_LOW).lerp(NIGHT_LOW, night), SKY_TOP);
        stars.draw(batch, 1f);
    }

    /** Le sommet de la Tour : créneaux de pierre sous la machine. */
    protected void drawTower(Batch batch, float cx, float top) {
        float machineBright = bright;
        bright = 1f;                                   // la pierre ne s'éteint pas avec la machine
        float w = 100f * U;
        rect(batch, STONE_DARK, cx - w / 2f, top - 60f * U, w, 60f * U);
        for (int row = 0; row < 12; row++) {                       // briques
            float y = top - (row + 1) * 5f * U;
            for (int col = 0; col < 12; col++) {
                float x = cx - w / 2f + col * 8.4f * U + (row % 2) * 4.2f * U;
                if (x + 8f * U > cx + w / 2f) continue;
                rect(batch, STONE, x + U * 0.5f, y + U * 0.5f, 7.4f * U, 4f * U);
            }
        }
        for (int i = 0; i < 9; i++) {                              // créneaux
            float x = cx - w / 2f + i * 12f * U;
            rect(batch, STONE_DARK, x, top - U, 7f * U, 5f * U);
            rect(batch, STONE, x + U, top, 5f * U, 3f * U);
        }
        bright = machineBright;
    }

    /** La machine : corps doré, cinq rouleaux, plateau à pièces, dôme et levier. */
    protected void drawMachine(Batch batch, float cx, float baseY) {
        float w = 72f * U, h = 44f * U;
        float left = cx - w / 2f, bottom = baseY + 3f * U;
        // Levier (derrière le corps) : il descend tout seul.
        float lever = settled ? 1f : Interpolation.sine.apply(progress(LEVER_START, LEVER_DOWN));
        float pivotX = left + w + 2f * U, pivotY = bottom + h * 0.55f;
        float angle = MathUtils.lerp(80f, -70f, lever), length = 24f * U;
        rect(batch, OUT, pivotX - U, pivotY - 3f * U, 5f * U, 6f * U);
        rect(batch, GOLD_DARK, pivotX, pivotY - 2f * U, 3f * U, 4f * U);
        batch.setColor(OUT);
        batch.draw(pixel, pivotX + U, pivotY - 1.5f * U, 0f, 1.5f * U, length, 3f * U, 1f, 1f, angle);
        batch.setColor(c("d8d8e0"));
        batch.draw(pixel, pivotX + U, pivotY - 0.7f * U, 0f, 0.7f * U, length, 1.4f * U, 1f, 1f, angle);
        float knobX = pivotX + U + MathUtils.cosDeg(angle) * length, knobY = pivotY + MathUtils.sinDeg(angle) * length;
        rect(batch, OUT, knobX - 3.5f * U, knobY - 3.5f * U, 7f * U, 7f * U);
        rect(batch, RED, knobX - 2.5f * U, knobY - 2.5f * U, 5f * U, 5f * U);
        rect(batch, c("ff8a9a"), knobX - 1.5f * U, knobY + 0.5f * U, 1.5f * U, 1.5f * U);

        // Corps.
        rect(batch, OUT, left - U, bottom - U, w + 2f * U, h + 2f * U);
        rect(batch, GOLD, left, bottom, w, h);
        rect(batch, GOLD_LIGHT, left, bottom + h - 3f * U, w, 2f * U);
        rect(batch, GOLD_LIGHT, left, bottom, 2f * U, h);
        rect(batch, GOLD_DARK, left + w - 3f * U, bottom, 3f * U, h);
        rect(batch, GOLD_DARK, left, bottom, w, 2f * U);
        // Dôme à gradins et diamant au sommet.
        for (int step = 0; step < 4; step++) {
            float sw = w - (step + 1) * 14f * U, sy = bottom + h + step * 4f * U;
            rect(batch, OUT, cx - sw / 2f - U, sy, sw + 2f * U, 5f * U);
            rect(batch, step % 2 == 0 ? GOLD : GOLD_LIGHT, cx - sw / 2f, sy, sw, 4f * U);
        }
        float gemY = bottom + h + 20f * U;
        glow(batch, cx, gemY, 30f * U, Color.WHITE, (0.4f + 0.3f * MathUtils.sin(time * 5f)) * bright);
        batch.setColor(Color.WHITE);
        sprite(batch, gem, cx, gemY, 2f * U, 0f);

        // Ampoules sur le pourtour : elles s'allument avec les rouleaux.
        float lit = settled ? 1f : progress(LIGHT_START, LIGHT_START + REELS * LIGHT_STEP);
        int bulbs = 18;
        for (int i = 0; i < bulbs; i++) {
            float x = left + (i + 0.5f) * w / bulbs;
            boolean on = lit > i / (float) bulbs && ((int) (time * 8f) + i) % 2 == 0;
            for (float y : new float[] {bottom + h - 1.5f * U, bottom + 1.5f * U}) {
                rect(batch, OUT, x - U, y - U, 2f * U, 2f * U);
                if (on) glow(batch, x, y, 6f * U, GOLD, 0.6f * bright);
                rect(batch, on ? c("fff6c8") : GOLD_DARK, x - 0.5f * U, y - 0.5f * U, U, U);
            }
        }

        // Les cinq rouleaux.
        float reelW = 11f * U, reelH = 14f * U, gap = 2f * U;
        float panelW = REELS * reelW + (REELS + 1) * gap;
        float reelY = bottom + h * 0.45f;
        rect(batch, OUT, cx - panelW / 2f - U, reelY - gap - U, panelW + 2f * U, reelH + 2f * gap + 2f * U);
        rect(batch, GOLD_DARK, cx - panelW / 2f, reelY - gap, panelW, reelH + 2f * gap);
        for (int i = 0; i < REELS; i++) {
            float x = cx - panelW / 2f + gap + i * (reelW + gap);
            float on = settled ? 1f : progress(LIGHT_START + i * LIGHT_STEP, LIGHT_START + i * LIGHT_STEP + 0.12f);
            rect(batch, OUT, x, reelY, reelW, reelH);
            if (on <= 0f) continue;
            if (on > 0.9f) glow(batch, x + reelW / 2f, reelY + reelH / 2f, reelW * 2.2f, GOLD, 0.25f * bright);
            on *= bright;
            batch.setColor(GLASS.r * on, GLASS.g * on, GLASS.b * on, 1f);
            batch.draw(pixel, x + 0.5f * U, reelY + 0.5f * U, reelW - U, reelH - U);
            batch.setColor(on, on, on, 1f);
            float pad = 1.5f * U;
            reel(batch, parade, x + pad, reelY + pad + U, reelW - 2f * pad, reelW - 2f * pad,
                reelSpin[i] % parade.length);
            if (lids[i] > 0f) {                                          // la paupière qui descend
                float lid = lids[i] * (reelH - U);
                rect(batch, GOLD_DARK, x + 0.5f * U, reelY + reelH - 0.5f * U - lid, reelW - U, lid);
                rect(batch, OUT, x + 0.5f * U, reelY + reelH - 0.5f * U - lid, reelW - U, 0.8f * U);
            }
            if (i < stoppedCount && !settled) {                         // l'œil qui te fixe
                float since = time - (STOP_START + i * STOP_STEP);
                glow(batch, x + reelW / 2f, reelY + reelH / 2f, reelW * 1.6f, RED, 0.5f * Math.max(0f, 1f - since));
            }
        }
        // Ligne de gain rouge.
        rect(batch, RED, cx - panelW / 2f - 2f * U, reelY + reelH / 2f - 0.5f * U, U, U);
        rect(batch, RED, cx + panelW / 2f + U, reelY + reelH / 2f - 0.5f * U, U, U);
        // Plateau à pièces.
        float trayW = 40f * U, trayY = bottom + 4f * U;
        rect(batch, OUT, cx - trayW / 2f, trayY, trayW, 7f * U);
        rect(batch, c("2a1a10"), cx - trayW / 2f + U, trayY + U, trayW - 2f * U, 5f * U);
        for (int i = 0; i < 9; i++) {
            float x = cx - trayW / 2f + 3f * U + i * 4f * U;
            rect(batch, GOLD, x, trayY + U, 3f * U, 2f * U + (i % 3) * U);
        }
    }

    private void rect(Batch batch, Color color, float x, float y, float w, float h) {
        batch.setColor(color.r * bright, color.g * bright, color.b * bright, color.a);
        batch.draw(pixel, x, y, w, h);
    }
}
