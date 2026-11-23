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
import java.util.Random;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les rois du Casino Englouti entrent en scène sous la mer, chacun dans la vraie salle de son combat (le même
 * décor que ses phases et sa mort) :
 * <ul>
 *   <li>Sirène du Bar (le Bar) : un verre glisse seul sur le comptoir. Des bulles montent, un chant ; les
 *   bouteilles des étagères et les verres dansent ; elle apparaît derrière le comptoir. Elle souffle une bulle,
 *   une carte flotte dedans.</li>
 *   <li>Jackpot Vivant (la salle des Machines) : un joueur fantôme tire le levier d'une machine ; rien. Les
 *   machines de la salle s'allument une à une, leurs rouleaux tombent sur 7, JACKPOT clignote ; il sort de
 *   l'allée et marche vers toi. Il te crache des pièces et se met en garde.</li>
 *   <li>Grand Requin Banquier (la salle des Coffres) : la salle forte, la porte ronde, les coffres pleins. Des
 *   pièces coulent, une ombre tourne autour ; le requin surgit de la porte du coffre. Il tamponne « REFUSÉ » sur
 *   un chèque et sourit.</li>
 *   <li>Kraken (le salon VIP) : l'eau devient noire, des cartes coulent. La table VIP se fend, huit bras en
 *   sortent avec chacun une carte ; ses yeux s'allument dans la fente et le Kraken lui-même, couronné, monte
 *   de la table. Ses bras battent les cartes et les étalent en éventail au-dessus de lui, un Joker au centre.</li>
 * </ul>
 */
public class CasinoKings extends KingEntrance {

    /** Sirène : le chant commence, les verres dansent, elle apparaît. */
    static final float SONG = 0.5f, DANCE = 0.8f, SIREN = 2.0f;
    /** Jackpot Vivant : les machines de la salle s'allument, JACKPOT clignote, il sort de l'allée et marche. */
    static final float LIGHTS = 0.3f, BLINK = 1.3f, WALK = 2.1f, WALK_END = 3.0f;
    /** Requin : l'ombre tourne, puis il surgit. */
    static final float CIRCLE = 0.4f, LUNGE = 2.1f;
    /** Kraken : la table se fend, les bras sortent un à un, ses yeux luisent dans la fente, il monte (au cor). */
    static final float CRACK = 0.5f, ARMS = 0.8f, ARM_STEP = 0.18f, GLIMPSE = 1.95f, EYE = 2.45f, RISEN = 3.0f;

    /** Le décor. Sirène : le verre glisse puis s'arrête. Jackpot : le fantôme tire le levier ; rien ; il s'efface. */
    static final float SLIDE_END = 1.4f, PULL = 0.55f, NOTHING = 1.1f, GHOST_FADE = 1.3f;
    /** Le geste. Sirène : la bulle. Jackpot : les pièces, la garde. Requin : le tampon, le sourire. */
    static final float BUBBLE_END = 4.3f, SPIT = 3.6f, GUARD = 4.3f, STAMP = 3.9f, SMILE = 4.5f;
    /** Kraken : les cartes passent de bras en bras, puis l'éventail, puis le Joker. */
    static final float FAN = 4.3f, FANNED = 4.8f, JOKER = 4.85f;

    private static final int ARM_COUNT = 8;
    private static final Color FOAM = hex("bfeeff"), GOLD = hex("ffc93a");
    private static final Color TENTACLE = hex("8a1e2a"), TENTACLE_LIGHT = hex("b8404a");
    /** La lumière du décor : un peu plus claire que dans leurs phases (0,62), pour bien voir la salle. */
    private static final float LIGHT = 0.78f;
    /** Le Bar : le dessus du comptoir et le sol (lignes du décor). */
    private static final int COUNTER_TOP = 124, BAR_FLOOR = 171;
    /** Les Machines : la machine du fantôme (une de devant) et le bas des machines. */
    private static final int GHOST_MACHINE = 8, MACHINES_FLOOR = 161;
    /** Les yeux du Kraken (bords de pixels de son portrait, comme dans ses phases). */
    private static final float[][] KRAKEN_EYES = {{14f, 21f}, {22f, 21f}};

    private final BitmapFont font;
    private final TextureRegion glass, note, shark, coin, disc, ghost;
    /** Les symboles des rouleaux des machines du décor. */
    private final TextureRegion[] symbols;
    /** Les cartes du jeu : As de Coeur, As de Pique et le Joker. */
    private final TextureRegion aceHeart, aceSpade, joker;
    /** Le centre et l'angle de la carte au bout de chaque bras, pour l'éventail. */
    private final float[] cardX = new float[ARM_COUNT], cardY = new float[ARM_COUNT], cardA = new float[ARM_COUNT];
    private final Array<Particle> bubbles = new Array<>(false, 128);
    private final Array<Particle> coins   = new Array<>(false, 32);
    private float bubbleDebt;

    public CasinoKings(CutsceneKit kit, EnemyKind kind) {
        super(kit, kind);
        font = kit.font();
        Color[] glassColors = {hex("0a1418"), hex("d8f4ff"), hex("ff4fa0"), hex("ffe066")};
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
        aceHeart = gameCard("cards/light/1-H.png");
        aceSpade = gameCard("cards/light/1-P.png");
        joker    = gameCard("cards/special_classic/blanc/joker.png");
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
        symbols = new TextureRegion[] {load("symbols/2-cherry.png"), load("symbols/3-seven.png"), load("symbols/4-bar.png"),
            load("symbols/6-bell.png"), load("symbols/7-diamond.png"), load("symbols/11-watermelon.png")};
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
        switch (kind) {
            case SIRENE -> drawSlidingGlass(batch);
            case JACKPOT_VIVANT -> drawGhost(batch);
            case REQUIN_BANQUIER -> drawVault(batch);
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
                if (at(SIREN)) sparks(cx(), kingY() + 300f, 30, hex("ff9ad0"), 500f, 14f);
            }
            case JACKPOT_VIVANT -> {
                for (int i = 0; i < 4; i++) if (at(WALK + 0.15f + i * 0.22f)) rumble(0.15f, 7f);
                if (at(WALK_END)) sparks(cx(), kingY() + 300f, 40, GOLD, 800f, 14f);
                if (at(SPIT)) {                                  // la machine te crache des pièces dessus
                    rumble(0.3f, 9f);
                    for (int i = 0; i < 40; i++) {
                        Particle piece = particle(cx() - 14f * KING_SCALE, kingY() + 16f * KING_SCALE, 360f * random.nextFloat(),   // par sa bouche
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
                    sparks(cx(), chequeY(), 20, hex("ff4a3a"), 400f, 10f);
                }
                if (at(LUNGE)) {
                    rumble(0.4f, 14f);
                    for (int i = 0; i < 40; i++) {
                        Particle bubble = particle(placeX(CasinoBossScenes.VAULT_X), placeY(CasinoBossScenes.VAULT_Y), 360f * random.nextFloat(),
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

    // -------------------------------------------------------------------------
    // Sirène du Bar
    // -------------------------------------------------------------------------

    private void drawSiren(Batch batch) {
        float width = worldWidth(), height = worldHeight(), counter = placeY(COUNTER_TOP);
        drawPlace(batch, LIGHT);
        float dance = progress(DANCE, DANCE + 0.4f);
        drawBottles(batch, dance);
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
            glow(batch, cx(), counter + 350f, 1100f, hex("ff9ad0"), 0.35f * appear);
            drawKing(batch, cx(), counter - 60f, KING_SCALE, 1f - appear, appear);
        }
        if (time >= GESTURE) {                                      // la bulle soufflée, une carte dedans
            float grow = Interpolation.pow2Out.apply(progress(GESTURE, BUBBLE_END));
            float rise = Math.max(0f, time - BUBBLE_END);
            float size = 40f + 190f * grow;
            float bx = cx() + 20f + 240f * grow + 60f * MathUtils.sin(rise * 2f);
            float by = counter - 60f + 22f * KING_SCALE + 120f * grow + rise * 140f;
            glow(batch, bx, by, size * 2.6f, FOAM, 0.25f);
            drawCard(batch, bx, by, 15f * MathUtils.sin(time * 2f), 0.9f * grow, aceHeart);
            batch.setColor(FOAM.r, FOAM.g, FOAM.b, 0.8f);
            batch.draw(ring, bx - size, by - size, size * 2f, size * 2f);
            glow(batch, bx - size * 0.4f, by + size * 0.4f, size * 0.4f, Color.WHITE, 0.6f);
        }
        // Le vrai comptoir du Bar, devant elle.
        placePart(batch, 0, COUNTER_TOP, 360, BAR_FLOOR - COUNTER_TOP, 0f, 0f, LIGHT);
        // Les verres qui dansent dessus.
        for (int i = 0; i < 6; i++) {
            float x = width * (0.12f + i * 0.152f);
            if (Math.abs(x - cx()) < 160f) continue;
            float hop = Math.abs(MathUtils.sin(time * 6f + i)) * 40f * dance;
            float angle = 14f * MathUtils.sin(time * 6f + i * 1.7f) * dance;
            batch.setColor(Color.WHITE);
            float scale = 14f;
            batch.draw(glass, x - glass.getRegionWidth() * scale / 2f, counter + hop, glass.getRegionWidth() * scale / 2f,
                0f, glass.getRegionWidth() * scale, glass.getRegionHeight() * scale, 1f, 1f, angle);
        }
    }

    /**
     * Les vraies bouteilles des étagères du Bar dansent à son chant : chacune se balance et sautille sur place,
     * le verre luit en rose.
     */
    private void drawBottles(Batch batch, float dance) {
        if (dance <= 0f) return;
        float ds = placeScale();
        for (int i = 0; i < CasinoBossScenes.BOTTLES; i++) {
            int left = CasinoBossScenes.bottleLeft(i), top = CasinoBossScenes.bottleTop(i);
            float beat = time * 7f + i * 0.9f;
            float dx = MathUtils.sin(beat) * 0.8f * ds * dance, dy = Math.abs(MathUtils.cos(beat)) * 0.9f * ds * dance;
            placePart(batch, left, top, CasinoBossScenes.BOTTLE_W, CasinoBossScenes.BOTTLE_H, dx, dy, LIGHT);
            if (i % 3 == 0) {
                glow(batch, placeX(left + CasinoBossScenes.BOTTLE_W / 2f) + dx, placeY(top + CasinoBossScenes.BOTTLE_H / 2f) + dy,
                    120f, hex("ff7ad8"), 0.35f * dance * pulse(9f + i, 0.3f));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Jackpot Vivant
    // -------------------------------------------------------------------------

    private void drawJackpot(Batch batch) {
        float width = worldWidth(), height = worldHeight(), floor = placeY(MACHINES_FLOOR), ds = placeScale();
        drawPlace(batch, 0.45f + (LIGHT - 0.45f) * progress(LIGHTS, BLINK));
        // Les machines de la salle s'allument une à une, du milieu vers les bords ; leurs rouleaux tournent, puis 7.
        int count = CasinoBossScenes.MACHINES.length;
        for (int m = 0; m < count; m++) {
            int[] sign = CasinoBossScenes.SIGNS[m], w = CasinoBossScenes.MACHINES[m];
            float start = lightAt(m);
            float lit = progress(start, start + 0.1f);
            if (lit <= 0f) continue;
            float blink = time >= BLINK ? (((int) (time * 10f) + m) % 2 == 0 ? 1f : 0.6f) : 1f;
            Color bulb = time >= BLINK ? GOLD : CasinoBossScenes.BULBS[(int) (time * 8f + m) % CasinoBossScenes.BULBS.length];
            tmp.set(bulb).lerp(Color.WHITE, 0.25f).mul(blink);
            tmp.a = lit;
            rect(batch, tmp, placeX(sign[0]), placeY(sign[1] + sign[3]), sign[2] * ds, sign[3] * ds);
            glow(batch, placeX(sign[0] + sign[2] / 2f), placeY(sign[1] + sign[3] / 2f), sign[2] * ds * 1.4f, bulb, 0.4f * lit * blink);
            for (int cell = 0; cell < 3; cell++) {
                float wx = placeX(w[0] + cell * w[2]), wy = placeY(w[1] + w[4]);
                float ww = (cell == 2 && w[4] > 10 ? w[3] + 1 : w[3]) * ds, wh = w[4] * ds;
                float stop = BLINK - 0.25f + cell * 0.08f, speed = 14f + 3f * cell;
                float offset = time >= stop ? SEVEN : SEVEN - (stop - time) * speed + m * 0.37f;
                rect(batch, placeLit(CasinoBossScenes.SCREEN, 1f, tmp), wx, wy, ww, wh);
                batch.setColor(Color.WHITE);
                CasinoBossScenes.slot(batch, symbols, wx, wy, ww, wh, offset);
            }
            if (time >= BLINK) glow(batch, placeX(w[0] + w[2] * 1.5f), placeY(w[1] + w[4] / 2f), 180f, GOLD, 0.4f * pulse(12f, 0.4f));
        }
        // JACKPOT clignote partout.
        if (time >= BLINK && time < WALK + 0.5f) {
            font.getData().setScale(3f);
            Random spots = new Random((int) (time * 6f));
            for (int i = 0; i < 4; i++) {
                float x = width * (0.15f + 0.7f * spots.nextFloat()), y = height * (0.6f + 0.3f * spots.nextFloat());
                caption(batch, font, Lang.t("JACKPOT"), x, y, 1f - progress(WALK, WALK + 0.5f));
            }
            font.getData().setScale(1f);
        }
        // Il sort de l'allée, entre les machines, et marche vers toi, pas à pas.
        if (time >= WALK) {
            float k = progress(WALK, WALK_END);
            float steps = Interpolation.linear.apply(k);
            float scale = MathUtils.lerp(5f, KING_SCALE, steps);
            float bob = Math.abs(MathUtils.sin(k * MathUtils.PI * 4f)) * 30f * (1f - k);
            float y = MathUtils.lerp(floor + 40f, kingY(), steps) + bob;
            float spit = gesture(SPIT - 0.15f, SPIT + 0.25f);
            float guard = Interpolation.swingOut.apply(progress(GUARD, GUARD + 0.3f));
            pose(0f, -30f * guard - 20f * spit, 0f, 0.05f * spit - 0.07f * guard);   // il crache, puis se met en garde
            glow(batch, cx(), y + 25f * scale, 70f * scale, GOLD, 0.4f + 0.3f * guard * pulse(8f, 0.5f));
            drawKing(batch, cx(), y, scale, 0.5f * (1f - progress(WALK, WALK + 0.5f)), progress(WALK, WALK + 0.2f));
        }
    }

    /** @return quand la machine {@code m} du décor s'allume : du milieu de la salle vers les bords. */
    private float lightAt(int m) {
        int[] sign = CasinoBossScenes.SIGNS[m];
        float off = Math.abs(sign[0] + sign[2] / 2f - 180f) / 180f;
        return LIGHTS + off * (BLINK - LIGHTS - 0.25f);
    }

    /** Le 7 parmi les symboles des rouleaux. */
    private static final int SEVEN = 1;

    // -------------------------------------------------------------------------
    // Grand Requin Banquier
    // -------------------------------------------------------------------------

    private void drawShark(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        drawPlace(batch, LIGHT);
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
        // Il surgit de la porte ronde du coffre : cravate et dents.
        if (time >= LUNGE) {
            float k = Interpolation.swingOut.apply(progress(LUNGE, LUNGE + 0.3f));
            float scale = MathUtils.lerp(2f, KING_SCALE, k);
            float vaultX = placeX(CasinoBossScenes.VAULT_X), vaultY = placeY(CasinoBossScenes.VAULT_Y);
            glow(batch, vaultX, vaultY, 600f, GOLD, 0.5f * (1f - progress(LUNGE, LUNGE + 0.6f)));
            glow(batch, cx(), kingY() + 280f, 1000f, FOAM, 0.25f);
            float press = gesture(STAMP - 0.3f, STAMP + 0.2f);
            pose(0f, -30f * press, 0f, -0.03f * press);
            drawKing(batch, MathUtils.lerp(vaultX, cx(), k), MathUtils.lerp(vaultY - 21f * 2f, kingY(), k), scale, 0f, 1f);
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
        drawPlace(batch, LIGHT - 0.22f * progress(CRACK, EYE));      // le salon s'assombrit
        float chandelier = 1f - 0.6f * progress(CRACK, EYE);           // le lustre du salon VIP
        glow(batch, placeX(CasinoBossScenes.CHANDELIER_X), placeY(CasinoBossScenes.CHANDELIER_Y), 600f, GOLD, 0.35f * chandelier);
        // La table VIP : rebord doré, tapis vert, la fente qui s'ouvre.
        ellipse(batch, hex("b8801f"), cx(), tableY, rx + 24f, ry + 14f);
        ellipse(batch, hex("1f6e3a"), cx(), tableY, rx, ry);
        float rise = Interpolation.pow2Out.apply(progress(EYE, RISEN));
        float crack = progress(CRACK, CRACK + 0.2f);
        if (crack > 0f) {
            float gap = 10f + 40f * progress(ARMS, EYE) + 60f * rise;
            batch.setColor(hex("020608"));
            for (int k = 0; k < 12; k++) {
                float x0 = cx() - rx * 0.9f * crack + k * rx * 1.8f * crack / 12f;
                float wobble = (k % 2 == 0 ? 1f : -1f) * 10f;
                batch.draw(pixel, x0, tableY - gap / 2f + wobble, rx * 1.8f * crack / 12f + 2f, gap);
            }
        }
        // Ses huit bras, chacun une carte au bout.
        for (int i = 0; i < ARM_COUNT; i++) {
            float start = ARMS + i * ARM_STEP;
            if (time < start) continue;
            float out = Interpolation.pow2Out.apply(progress(start, start + 0.45f));
            float baseX = cx() + (i - (ARM_COUNT - 1) / 2f) * rx * 1.6f / ARM_COUNT;
            float side = i < ARM_COUNT / 2 ? -1f : 1f;
            float x = baseX, y = tableY, angle = 90f - side * (8f + 14f * rise);       // ils s'écartent pour lui
            int segments = 16;
            float segment = 34f * out;
            for (int s = 0; s < segments; s++) {
                float radius = MathUtils.lerp(40f, 12f, s / (float) segments);
                angle += side * (6f + 4f * MathUtils.sin(time * 3f + i + s * 0.4f)) * (s / (float) segments);
                x += MathUtils.cosDeg(angle) * segment;
                y += MathUtils.sinDeg(angle) * segment;
                batch.setColor(s % 3 == 0 ? TENTACLE_LIGHT : TENTACLE);
                batch.draw(disc, x - radius, y - radius, radius * 2f, radius * 2f);
            }
            if (out > 0.6f) {                                        // la carte au bout du bras (battue après le geste)
                float cardAngle = angle - 90f;
                if (time < GESTURE) {
                    batch.setColor(Color.WHITE);
                    batch.draw(i % 2 == 0 ? aceHeart : aceSpade, x - 34f, y - 2f, 34f, 0f, 68f, 96f, 1f, 1f, cardAngle);
                }
                // Le logo au centre de la carte : on tourne sa position avec la carte, puis le losange sur lui-même.
                float logoX = x - MathUtils.sinDeg(cardAngle) * 46f;
                float logoY = y - 2f + MathUtils.cosDeg(cardAngle) * 46f;
                cardX[i] = logoX;
                cardY[i] = logoY;
                cardA[i] = cardAngle;
            }
        }
        // Ses deux yeux luisent dans la fente, juste avant qu'il monte.
        float glimpse = progress(GLIMPSE, GLIMPSE + 0.3f) * (1f - progress(EYE, EYE + 0.25f));
        if (glimpse > 0f) {
            for (float[] eye : KRAKEN_EYES) {
                float ex = kingPixelX(eye[0], cx(), KING_SCALE);
                glow(batch, ex, tableY + 6f, 220f, hex("ffe066"), 0.8f * glimpse * pulse(7f, 0.6f));
                glow(batch, ex, tableY + 6f, 60f, Color.WHITE, glimpse);
            }
        }
        // Le Kraken lui-même, couronné, monte de la fente ; ses yeux s'allument en sortant.
        if (time >= EYE) {
            float y = MathUtils.lerp(tableY - KING_SCALE * 42f, kingY(), rise);
            glow(batch, cx(), tableY + 260f * rise, 900f * rise, hex("ffe066"), 0.3f);
            drawKingAbove(batch, cx(), y, KING_SCALE, 1f - progress(EYE + 0.15f, RISEN), 1f, tableY - 20f);
            float stare = progress(EYE, EYE + 0.2f) * (1f - progress(RISEN + 0.3f, RISEN + 0.9f));
            for (float[] eye : KRAKEN_EYES) {
                float ex = kingPixelX(eye[0], cx(), KING_SCALE), ey = kingPixelY(eye[1], y, KING_SCALE);
                if (ey < tableY) continue;
                glow(batch, ex, ey, 9f * KING_SCALE, hex("ffe066"), 0.9f * stare);
                glow(batch, ex, ey, 2.2f * KING_SCALE, Color.WHITE, stare);
            }
        }
        if (time >= GESTURE) drawShuffle(batch);
        flash(batch, hex("ffe066"), EYE, 0.2f, 0.35f);
    }

    /**
     * Les bras battent les cartes : elles sautent de bras en bras, puis se rangent
     * en éventail au-dessus de lui ; un Joker monte au centre.
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
                MathUtils.lerp(1f, 1.5f, fan), i % 2 == 0 ? aceHeart : aceSpade);
        }
        if (time >= JOKER) {                                           // le Joker, au centre de l'éventail
            float k = Interpolation.swingOut.apply(progress(JOKER, JOKER + 0.35f));
            glow(batch, cx(), fanY(), 500f * k, GOLD, 0.6f * pulse(6f, 0.6f));
            drawCard(batch, cx(), MathUtils.lerp(fanY() - 200f, fanY() + 30f, k), 0f, 1.9f * k, joker);
        }
    }

    private float fanY() { return worldHeight() * 0.84f; }

    /** La carte du jeu {@code card} centrée sur ({@code x}, {@code y}), tournée de {@code angle}. */
    private void drawCard(Batch batch, float x, float y, float angle, float scale, TextureRegion card) {
        if (scale <= 0f) return;
        float w = 68f * scale, h = 96f * scale;
        batch.setColor(Color.WHITE);
        batch.draw(card, x - w / 2f, y - h / 2f, w / 2f, h / 2f, w, h, 1f, 1f, angle);
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
        batch.setColor(hex("3a3020"));
        batch.draw(pixel, x - w / 2f - 6f, y - h / 2f - 6f, w / 2f + 6f, h / 2f + 6f, w + 12f, h + 12f, 1f, 1f, -6f);
        batch.setColor(hex("f0e6c8"));
        batch.draw(pixel, x - w / 2f, y - h / 2f, w / 2f, h / 2f, w, h, 1f, 1f, -6f);
        for (int i = 0; i < 3; i++) {                                  // les lignes du chèque
            batch.setColor(hex("a89a78"));
            batch.draw(pixel, x - w * 0.4f, y + 40f - i * 45f, w * 0.4f, 2f, w * (i == 2 ? 0.4f : 0.8f), 4f, 1f, 1f, -6f);
        }
        // Le tampon descend, puis « REFUSÉ » en rouge.
        float press = Interpolation.pow2In.apply(progress(STAMP - 0.3f, STAMP));
        if (time < STAMP + 0.2f) {
            float sy = MathUtils.lerp(y + 400f, y + 40f, press);
            rect(batch, hex("4a2a14"), x - 26f, sy + 60f, 52f, 140f);
            rect(batch, hex("2a2a34"), x - 90f, sy, 180f, 60f);
        }
        if (time >= STAMP) {
            float k = progress(STAMP, STAMP + 0.12f);
            font.getData().setScale(MathUtils.lerp(4.4f, 3.2f, k));
            Color old = font.getColor().cpy();
            font.setColor(hex("d81e1e"));
            caption(batch, font, Lang.t("REFUSÉ"), x, y, 1f);
            font.setColor(old);
            font.getData().setScale(1f);
        }
    }

    private float chequeY() { return worldHeight() * 0.3f; }

    // -------------------------------------------------------------------------
    // Le décor, avant l'entrée
    // -------------------------------------------------------------------------

    /** Sirène : un verre glisse seul sur le vrai comptoir du Bar, et s'arrête devant toi. */
    private void drawSlidingGlass(Batch batch) {
        float counter = placeY(COUNTER_TOP);
        drawPlace(batch, LIGHT);
        glow(batch, cx(), counter + 300f, 900f, hex("ff9ad0"), 0.15f);
        float k = Interpolation.pow2Out.apply(progress(0.1f, SLIDE_END));
        float x = MathUtils.lerp(-120f, cx(), k);
        float wobble = time >= SLIDE_END ? 6f * MathUtils.sin((time - SLIDE_END) * 25f) * Math.max(0f, 1f - (time
            - SLIDE_END) * 2f) : -4f;
        float scale = 22f;
        glow(batch, x, counter + 120f, 300f, hex("ff4fa0"), 0.3f);
        batch.setColor(Color.WHITE);
        batch.draw(glass, x - glass.getRegionWidth() * scale / 2f, counter, glass.getRegionWidth() * scale / 2f, 0f,
            glass.getRegionWidth() * scale, glass.getRegionHeight() * scale, 1f, 1f, wobble);
        for (int i = 0; i < 6; i++) {                                  // le sillage de bulles derrière le verre
            float bx = x - 80f - i * 70f, alpha = (1f - i / 6f) * (1f - progress(SLIDE_END, SLIDE_END + 0.4f));
            batch.setColor(FOAM.r, FOAM.g, FOAM.b, 0.5f * alpha);
            batch.draw(ring, bx - 12f, counter + 10f + (i % 2) * 20f, 24f, 24f);
        }
    }

    /**
     * Jackpot Vivant : la salle des Machines dans la pénombre ; un joueur fantôme tire le levier d'une machine de
     * devant, ses rouleaux tournent et tombent sur trois symboles différents ; rien ; il s'efface.
     */
    private void drawGhost(Batch batch) {
        float floor = placeY(MACHINES_FLOOR), ds = placeScale();
        drawPlace(batch, 0.42f);
        int[] w = CasinoBossScenes.MACHINES[GHOST_MACHINE], sign = CasinoBossScenes.SIGNS[GHOST_MACHINE];
        float pull = gesture(PULL - 0.25f, PULL + 0.25f);
        boolean played = time >= PULL;
        float dim = time >= NOTHING ? 0.45f : 0.8f;
        if (played) {                                                  // l'enseigne s'allume, les rouleaux tournent
            tmp.set(GOLD).mul(dim);
            tmp.a = 1f;
            rect(batch, tmp, placeX(sign[0]), placeY(sign[1] + sign[3]), sign[2] * ds, sign[3] * ds);
            glow(batch, placeX(sign[0] + sign[2] / 2f), placeY(sign[1] + sign[3] / 2f), sign[2] * ds * 1.4f, GOLD, 0.35f * dim);
            for (int cell = 0; cell < 3; cell++) {
                float wx = placeX(w[0] + cell * w[2]), wy = placeY(w[1] + w[4]);
                float ww = (cell == 2 ? w[3] + 1 : w[3]) * ds, wh = w[4] * ds;
                float stop = NOTHING - 0.24f + cell * 0.12f;
                float offset = time >= stop ? LOSS[cell] : LOSS[cell] - (stop - time) * (14f + 3f * cell);
                rect(batch, placeLit(CasinoBossScenes.SCREEN, dim + 0.2f, tmp), wx, wy, ww, wh);
                batch.setColor(dim + 0.2f, dim + 0.2f, dim + 0.2f, 1f);
                CasinoBossScenes.slot(batch, symbols, wx, wy, ww, wh, offset);
            }
        }
        // La boule rouge du levier de la machine, qu'il tire vers le bas.
        glow(batch, placeX(LEVER_X), placeY(LEVER_Y) - pull * 9f * ds, 60f, hex("e0283a"), 0.9f);
        // Le fantôme, transparent, qui s'efface après le « rien ».
        float alpha = 0.45f * (1f - progress(GHOST_FADE, PRELUDE - 0.2f)) * (0.85f + 0.15f * MathUtils.sin(time * 6f));
        float gs = 24f, gw = ghost.getRegionWidth() * gs, gh = ghost.getRegionHeight() * gs;
        float gx = placeX(LEVER_X) + gw * 0.45f;
        float gy = floor - 20f - 40f * pull + 15f * MathUtils.sin(time * 3f) + 200f * progress(GHOST_FADE, PRELUDE);
        glow(batch, gx, gy + gh * 0.5f, 600f, hex("a8f0ff"), alpha * 0.6f);
        batch.setColor(0.75f, 0.95f, 1f, alpha);
        batch.draw(ghost, gx - gw / 2f, gy, gw, gh);
        if (time >= NOTHING && time < GHOST_FADE + 0.4f) {
            font.getData().setScale(2.4f);
            caption(batch, font, "...", placeX(sign[0] + sign[2] / 2f), placeY(sign[1]) + 60f,
                1f - progress(GHOST_FADE, GHOST_FADE + 0.4f));
            font.getData().setScale(1f);
        }
    }

    /** Le levier de la machine du fantôme : sa boule rouge (pixels du décor). */
    private static final float LEVER_X = 152.5f, LEVER_Y = 89f;
    /** Les trois symboles différents où tombent ses rouleaux : cerise, barre, cloche. */
    private static final float[] LOSS = {0f, 2f, 3f};

    /**
     * Requin : la salle des Coffres, sa porte ronde et ses deux coffres pleins d'or ; la roue dorée de la porte
     * luit, des éclats d'or brillent sur les coffres.
     */
    private void drawVault(Batch batch) {
        drawPlace(batch, LIGHT);
        float vaultX = placeX(CasinoBossScenes.VAULT_X), vaultY = placeY(CasinoBossScenes.VAULT_Y), ds = placeScale();
        glow(batch, vaultX, vaultY, 30f * ds, GOLD, 0.3f * pulse(4f, 0.6f));
        glow(batch, placeX(CHESTS_X), placeY(CHESTS_Y), 60f * ds, GOLD, 0.3f * pulse(3f, 0.7f));
        for (int i = 0; i < 8; i++) {                                                   // des éclats d'or
            float t = (time * 1.3f + i * 0.37f) % 1f;
            float px = i < 6 ? CHESTS_X - 40f + (i * 97 % 100) / 100f * 80f : CasinoBossScenes.VAULT_X + (i == 6 ? -12f : 12f);
            float py = i < 6 ? CHESTS_Y - 6f + (i * 53 % 100) / 100f * 8f : CasinoBossScenes.VAULT_Y;
            glow(batch, placeX(px), placeY(py), 80f * MathUtils.sin(t * MathUtils.PI), Color.WHITE, 0.8f);
        }
    }

    /** Le dessus doré des deux coffres de la salle (pixels du décor). */
    private static final float CHESTS_X = 297f, CHESTS_Y = 128f;

    /** Kraken : le salon VIP ; l'eau devient noire, des cartes coulent lentement en tournant. */
    private void drawSinkingCards(Batch batch) {
        float width = worldWidth(), height = worldHeight();
        drawPlace(batch, LIGHT);
        glow(batch, placeX(CasinoBossScenes.CHANDELIER_X), placeY(CasinoBossScenes.CHANDELIER_Y), 600f, GOLD, 0.35f);
        for (int i = 0; i < 12; i++) {
            float x = width * ((i * 0.083f + 0.04f) % 1f) + 50f * MathUtils.sin(time * 1.5f + i);
            float y = height + 120f - (time * (90f + (i % 4) * 30f) + (i * 137 % 500)) ;
            drawCard(batch, x, y, 30f * MathUtils.sin(time * (1f + i * 0.1f) + i), 1f + (i % 3) * 0.3f,
                i % 2 == 0 ? aceHeart : aceSpade);
        }
        fill(batch, Color.BLACK, 0.75f * Interpolation.pow2In.apply(progress(0.2f, PRELUDE)));
        float eyes = progress(1.2f, 1.5f);                                               // un éclat jaune, tout au fond
        if (eyes > 0f) glow(batch, cx(), height * 0.2f, 200f, hex("ffe066"), 0.4f * eyes * pulse(5f, 0.5f));
    }
}
