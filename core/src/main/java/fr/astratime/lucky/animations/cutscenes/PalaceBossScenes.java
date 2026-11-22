package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.assets.PalacePortraits;
import fr.astratime.lucky.assets.PalacePortraits.Prop;
import fr.astratime.lucky.entities.enemy.EnemyKind;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les scènes de phase et de mort des quatre rois du Palace des Neiges (10 lieux, Astra 2026-11-20 ; redessinées le
 * 2026-11-22 pour que l'image montre la phrase : les plateaux de champagne du service d'étage, le tableau où le
 * Concierge raccroche ses vraies clés, le bal de glace et sa pyramide de coupes, le souffle polaire qui givre l'écran,
 * le Yéti qui s'endort sous la neige, les portes du bal qui se ferment, le lustre qui s'éteint à la fin du bal).
 */
final class PalaceBossScenes {

    private PalaceBossScenes() {}

    static BossScene phase(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case CONCIERGE -> new ConciergePhase(kit);
            case IMPERATRICE_GIVRE -> new EmpressPhase(kit);
            case YETI -> new YetiPhase(kit);
            case PRINCE_NEIGES -> new PrincePhase(kit);
            default -> null;
        };
    }

    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case CONCIERGE -> new ConciergeDeath(kit);
            case IMPERATRICE_GIVRE -> new EmpressDeath(kit);
            case YETI -> new YetiDeath(kit);
            case PRINCE_NEIGES -> new PrinceDeath(kit);
            default -> null;
        };
    }

    static final Color ICE = hex("bfe8ff"), ICE_DEEP = hex("6aa8d8"), SNOW = hex("f2f8ff"), BUBBLE = hex("ffe08a");
    static final Color WATER = hex("4a8ac0"), NIGHT = hex("2a3a6a"), GLINT = hex("7ad0ff"), SMOKE = hex("a8b0c0");

    /** Les yeux des portraits du Palace (pixels 13-14 et 20-21, lignes 15-16 ; ceux du Yéti, 13-15 et 19-21, lignes 16-17). */
    static final float[] LEFT_EYE = {14f, 16f}, RIGHT_EYE = {21f, 16f};
    static final float[] YETI_LEFT_EYE = {14.5f, 17f}, YETI_RIGHT_EYE = {20.5f, 17f};

    // ----- Accessoires en pixel art -----

    static final String[] DESK_BELL = {"....oo....", "...oggo...", "..oggggo..", ".oggggsgo.", "oooooooooo"};

    static final Color[] CUP_COLORS = {hex("1a1018"), hex("fff6d8"), BUBBLE, ICE, hex("dce4ee"), hex("8a97a8")};
    /** Un plateau d'argent et sa coupe de champagne glacé (k contour, w mousse, b champagne, s verre, t argent, d ombre). */
    static final String[] TRAY = {
        "...kwwwwwk...",
        "...kbbbbbk...",
        "....kbbbk....",
        ".....kbk.....",
        "......s......",
        "......s......",
        ".....sss.....",
        "kkkkkkkkkkkkk",
        "kttttttttttwk",
        ".kdddddddddk.",
        "..kkkkkkkkk.."};
    /** Une coupe seule, pour la pyramide du bal. */
    static final String[] COUPE = {"kwwwwwk", "kbbbbbk", ".kbbbk.", "..kbk..", "...s...", "...s...", "..sss.."};

    static final Color[] SHARD_COLORS = {hex("6aa8d8"), hex("dcecf8"), hex("ffffff")};
    /** Un éclat de glace. */
    static final String[] SHARD = {"..k", ".wk", "kwk", "kik", "kk."};

    static final Color[] CHANDELIER_COLORS = {hex("2a5a8a"), ICE, hex("ffffff"), ICE_DEEP};
    /** Le lustre de glace du bal (k contour, i glace, w éclat, s bougie). */
    static final String[] CHANDELIER = {
        "............k............",
        "............k............",
        "............k............",
        "...w...w...kik...w...w...",
        "...s...s..kiwik..s...s...",
        "..kik.kik.kiiik.kik.kik..",
        "..kiiiiiiiiiiiiiiiiiiik..",
        "...kiiiwiiiiiiiiiwiiik...",
        "....kiiiiiiiiiiiiiiik....",
        ".....kkiiiiiiiiiiikk.....",
        ".......kkiiiiiiikk.......",
        "...i.....kkiiikk.....i...",
        "...i.......kik.......i...",
        "...w........i........w...",
        "............w............"};
    /** Les mèches du lustre (colonnes, ligne 2) : une flamme sur chacune. */
    static final int[] WICKS = {3, 7, 17, 21};

    static final Color[] DANCER_COLORS = {hex("ffffff")};
    /** Un couple de danseurs fantômes, enlacés (dessiné en transparence bleu glace). */
    static final String[] DANCERS = {
        "..ww..ww..",
        "..ww..ww..",
        "...w..w...",
        ".wwwwwwww.",
        "w.wwwwww.w",
        "..wwwwww..",
        "..wwwwww..",
        ".wwwwwwww.",
        ".wwwwwwww.",
        "wwwwwwwwww",
        "wwwwwwwwww",
        "..w....w..",
        "..w....w..",
        ".ww....ww."};

    static final Color[] SNOW_COLORS = {hex("9ab8d4"), hex("ffffff"), hex("eef4fc"), hex("c8d8ec")};
    /** Un tas de neige arrondi de {@code width x height} pixels (o contour, w reflet, s neige, d ombre). */
    static String[] snowHeap(int width, int height) {
        String[] rows = new String[height];
        for (int y = 0; y < height; y++) {
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < width; x++) {
                float dx = (x + 0.5f - width / 2f) / (width / 2f), top = height * (1f - (float) Math.pow(Math.abs(dx), 2.2f));
                float depth = y - (height - top);
                char c = depth < 0f ? '.' : depth < 1f ? 'o' : depth < 2.5f && dx < 0.3f ? 'w' : dx > 0.35f && depth > 2f ? 'd' : 's';
                if (c == 's' && (x * 7 + y * 13) % 29 == 0) c = 'w';                          // quelques cristaux
                row.append(c);
            }
            rows[y] = row.toString();
        }
        return rows;
    }

    static final Color[] DOOR_COLORS = {hex("1a1018"), hex("eef2f8"), hex("c8d0e0"), hex("ffc93a"), hex("b8d8f0"), hex("ffffff")};

    /** Un battant de la grande porte du bal (o contour, w bois laqué blanc, s ombre, g moulure d'or, i panneau givré, f givre). */
    static String[] doorLeaf() {
        int width = 20, height = 52;
        String[] rows = new String[height];
        for (int y = 0; y < height; y++) {
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < width; x++) {
                char c = x == 0 || y == 0 || x == width - 1 || y == height - 1 ? 'o' : x >= width - 3 ? 's' : 'w';
                boolean upper = y >= 4 && y <= 24, lower = y >= 28 && y <= 47;
                if ((upper || lower) && x >= 3 && x <= width - 5) {
                    boolean edge = x == 3 || x == width - 5 || y == 4 || y == 24 || y == 28 || y == 47;
                    c = edge ? 'g' : ((x * 5 + y * 3) % 17 == 0 ? 'f' : 'i');
                }
                if (x == width - 3 && y >= 25 && y <= 27) c = 'g';                             // la poignée
                row.append(c);
            }
            rows[y] = row.toString();
        }
        return rows;
    }

    static final Color[] ICICLE_COLORS = {hex("6aa8d8"), hex("bfe8ff"), hex("ffffff")};
    static final String[] ICICLE = {"kiwik", "kiwk.", ".kik.", ".kik.", ".kwk.", "..k..", "..k.."};

    // -------------------------------------------------------------------------
    // Le Concierge
    // -------------------------------------------------------------------------

    /**
     * « Service d'étage : Champagne glacé ! » : derrière son comptoir, il frappe trois fois la sonnette ; à chaque
     * coup, un plateau d'argent chargé d'une coupe arrive tout seul (de gauche, de droite, puis du plafond) ; à
     * l'apogée, les coupes se couvrent de givre et pétillent.
     */
    static final class ConciergePhase extends BossScene {
        private static final float[] RING = {0.5f, 0.95f, 1.4f};
        private final TextureRegion bell, tray;

        ConciergePhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.CONCIERGE, 2);
            bell = art("ogs", new Color[] {hex("3a2a10"), GOLD, hex("c4891e")}, DESK_BELL);
            tray = art("kwbstd", CUP_COLORS, TRAY);
        }

        private float counterTop()  { return bossBottom() + bossHeight() * 0.2f; }
        private float bellX()       { return bossX() + bossWidth() * 0.62f; }
        private float trayScale()   { return worldHeight() * 0.11f / TRAY.length; }

        /** Où le plateau {@code k} se pose sur le comptoir, et d'où il arrive. */
        private float trayX(int k)  { return bossX() + new float[] {-1.05f, 1.15f, -0.5f}[k] * bossWidth(); }
        private float trayY(int k)  { return counterTop() + TRAY.length * trayScale() / 2f + (k == 2 ? worldHeight() * 0.2f : 0f); }
        private float fromX(int k)  { return k == 0 ? -worldWidth() * 0.1f : k == 1 ? worldWidth() * 1.1f : trayX(k); }
        private float fromY(int k)  { return k == 2 ? worldHeight() * 1.15f : trayY(k) + worldHeight() * 0.05f; }

        @Override
        protected void step(float delta) {
            for (float ring : RING) if (at(ring)) { rumble(0.06f, 2f); sparks(bellX(), counterTop() + 30f, 14, GOLD, 380f, 8f); }
            if (at(CLIMAX_PHASE)) {
                rumble(0.4f, 8f);
                for (int k = 0; k < 3; k++) sparks(trayX(k), trayY(k) + 20f, 24, ICE, 420f, 10f);
            }
            if (time > CLIMAX_PHASE && time < CLIMAX_PHASE + 1.5f && random.nextFloat() < delta * 30f) {   // les bulles
                int k = random.nextInt(3);
                Particle bubble = particle(trayX(k) + (random.nextFloat() - 0.5f) * 20f, trayY(k) + 30f, 90f, 120f, 0.9f, 7f);
                bubble.color.set(BUBBLE);
                glows.add(bubble);
            }
        }

        @Override
        protected void back(Batch batch) {
            float strike = 0f;
            for (float ring : RING) strike = Math.max(strike, gesture(ring - 0.12f, ring + 0.08f));
            poseTilt = -3f * strike;
            poseTint = 0.25f * progress(1.8f, CLIMAX_PHASE);
            poseTintColor.set(ICE);
        }

        @Override
        protected void front(Batch batch) {
            float w = bossWidth() * 2.9f, h = counterTop() - groundY() + 10f, left = bossX() - w / 2f;   // le comptoir de marbre
            rect(batch, hex("1a1018"), 1f, left - 4f, groundY() - 4f, w + 8f, h + 8f);
            rect(batch, hex("3a2418"), 1f, left, groundY(), w, h - 14f);
            for (int k = 1; k < 6; k++) rect(batch, hex("2a1810"), 1f, left + w * k / 6f - 3f, groundY(), 6f, h - 14f);
            rect(batch, hex("e8eef8"), 1f, left - 10f, counterTop() - 14f, w + 20f, 18f);
            rect(batch, hex("c8d0e0"), 1f, left - 10f, counterTop() - 14f, w + 20f, 5f);
            float press = 0f;
            for (float ring : RING) press = Math.max(press, 1f - Math.abs(time - ring) / 0.07f);
            float scale = worldHeight() * 0.05f / DESK_BELL.length;
            stretch(batch, bell, bellX(), counterTop() + DESK_BELL.length * scale / 2f, scale, scale * (1f - 0.15f * Math.max(0f, press)), 0f,
                WHITE, 1f);
            for (float ring : RING) wave(batch, ring, 0.4f, bellX(), counterTop() + 20f, GOLD, worldHeight() * 0.18f);
            for (int k = 0; k < 3; k++) {                                                     // les plateaux qui arrivent seuls
                float come = Interpolation.pow2Out.apply(progress(RING[k], RING[k] + 0.5f));
                if (come <= 0f) continue;
                float x = MathUtils.lerp(fromX(k), trayX(k), come), y = MathUtils.lerp(fromY(k), trayY(k), come);
                float sway = (1f - come) * 8f * MathUtils.sin(time * 20f + k);
                if (k == 2) line(batch, hex("c8d0e0"), 0.8f, x, y + 30f, x, worldHeight() + MARGIN, 3f);   // le monte-plats
                draw(batch, tray, x, y, trayScale(), sway, WHITE, 1f);
                float frost = progress(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.3f);
                if (frost > 0f) {
                    draw(batch, tray, x, y, trayScale(), sway, WHITE, 0.5f * frost);
                    glow(batch, x, y + 20f, trayScale() * 16f, ICE, 0.5f * frost * pulse(5f + k, 0.6f));
                }
            }
            flash(batch, ICE, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /**
     * « Le Concierge raccroche ses clés » : au mur du hall, le tableau des clés a deux places vides ; ses deux clés
     * d'or quittent le revers de sa redingote (les vraies : elles disparaissent du portrait) et vont s'y pendre ; il
     * s'incline une dernière fois, le givre le prend, il se fend et vole en éclats de glace ; les clés se balancent.
     */
    static final class ConciergeDeath extends BossScene {
        private static final float LEAVE_LEFT = 0.6f, LEAVE_RIGHT = 1.0f, FLIGHT = 0.7f, BOW = 1.9f, SHATTER = 3.1f;
        /** Les deux places vides du tableau des clés du décor (colonne, ligne) ; une clé du décor y fait 6 x 14 pixels. */
        private static final int[][] SLOTS = {{0, 0}, {8, 1}};
        private static final Color WALL = hex("422919");
        private final TextureRegion shard, noLeft, noKeys;
        private final Piece left, right;

        ConciergeDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.CONCIERGE, 0);
            shard = art("kwi", SHARD_COLORS, SHARD);
            int[][] rest = PalacePortraits.without(EnemyKind.CONCIERGE, Prop.KEY_LEFT);
            int[][] none = PalacePortraits.without(EnemyKind.CONCIERGE, Prop.KEY_LEFT, Prop.KEY_RIGHT);
            left = lift(PalacePortraits.without(EnemyKind.CONCIERGE, Prop.KEY_RIGHT), none);     // chaque clé entière, croisement compris
            right = lift(rest, none);
            noLeft = pixels(rest);
            noKeys = pixels(none);
        }

        @Override protected float textAt() { return SHATTER + 0.2f; }

        private float slotX(int k)   { return decorX(124f + 14f * SLOTS[k][0]); }
        private float slotTop(int k) { return decorY(33f + 20f * SLOTS[k][1]); }

        @Override
        protected void step(float delta) {
            if (at(LEAVE_LEFT)) wear(noLeft);
            if (at(LEAVE_RIGHT)) wear(noKeys);
            if (at(LEAVE_LEFT + FLIGHT)) sparks(slotX(0), slotTop(0), 12, GOLD, 240f, 7f);
            if (at(LEAVE_RIGHT + FLIGHT)) sparks(slotX(1), slotTop(1), 12, GOLD, 240f, 7f);
            for (int k = 0; k < 4; k++) if (at(2.3f + k * 0.2f)) { rumble(0.06f, 2f + k); sparks(bossX(), bossY(), 8, SNOW, 260f, 7f); }
            if (at(SHATTER)) {
                rumble(0.6f, 12f);
                sparks(bossX(), bossY(), 80, ICE, 900f, 14f);
                spray(shard, bossX(), bossY(), 26, 0f, 360f, 900f, bossScale() * 1.2f, 1600f, 1.6f);
            }
        }

        @Override
        protected float decorLight() { return 0.62f - 0.22f * progress(SHATTER, SHATTER + 0.8f); }

        @Override
        protected void back(Batch batch) {
            for (int[] slot : SLOTS) decorRect(batch, WALL, 120f + 14f * slot[0], 32f + 20f * slot[1], 8f, 16f);   // les places vides
            float bow = gesture(BOW, BOW + 0.7f);
            poseTilt = 8f * bow;
            poseY = -14f * bow;
            poseTint = 0.7f * progress(2.2f, SHATTER - 0.1f);
            poseTintColor.set(ICE);
            poseX = time > 2.3f && time < SHATTER ? MathUtils.sin(time * 60f) * 3f * progress(2.3f, SHATTER) : 0f;
            showBoss = time < SHATTER;
        }

        private void flyKey(Batch batch, Piece key, float leave, int slot, float turn) {
            if (key == null) return;
            if (time < leave) { attached(batch, key, 1f); return; }
            float k = Interpolation.pow2InInverse.apply(progress(leave, leave + FLIGHT));
            float scale = bossScale(), length = key.height * scale * 1.41f;
            float x = MathUtils.lerp(pieceX(key), slotX(slot), k);
            float y = MathUtils.lerp(pieceY(key), slotTop(slot) - length * 0.5f, k) + MathUtils.sin(k * MathUtils.PI) * worldHeight() * 0.1f;
            float after = time - leave - FLIGHT;
            float swing = after > 0f ? 14f * MathUtils.sin(after * 7f) * (float) Math.exp(-after * 1.2f) : 0f;
            draw(batch, key.region, x, y, scale, turn * k + swing, WHITE, 1f);
            glow(batch, x, y, length * (k < 1f ? 2.5f : 1.6f), GOLD, k < 1f ? 0.45f : 0.25f * pulse(3f, 0.6f));
        }

        @Override
        protected void front(Batch batch) {
            flyKey(batch, left, LEAVE_LEFT, 0, -45f);
            flyKey(batch, right, LEAVE_RIGHT, 1, 45f);
            float cracks = progress(2.3f, SHATTER);                                           // les fêlures du givre
            if (time < SHATTER) for (int k = 0; k < 5 && k < cracks * 6f; k++) {
                crack(batch, bossX(), bossY(), 30f + k * 72f, bossHeight() * 0.45f * Math.min(1f, cracks * 2f), 11L + k, WHITE, 0.9f, 5f);
            }
            flash(batch, ICE, SHATTER, 0.3f, 0.5f);
        }
    }

    // -------------------------------------------------------------------------
    // L'Impératrice du Givre
    // -------------------------------------------------------------------------

    /**
     * « Le bal de glace commence : Champagne glacé ! » : le lustre de glace descend et s'allume, le sol devient une
     * patinoire, des couples de danseurs fantômes valsent autour d'elle pendant qu'elle tourne ; à l'apogée, une
     * pyramide de coupes de champagne se dresse et gèle d'un coup.
     */
    static final class EmpressPhase extends BossScene {
        private final TextureRegion chandelier, dancers, coupe;

        EmpressPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.IMPERATRICE_GIVRE, 2);
            chandelier = art("kiws", CHANDELIER_COLORS, CHANDELIER);
            dancers = art("w", DANCER_COLORS, DANCERS);
            coupe = art("kwbstd", CUP_COLORS, COUPE);
        }

        private float lusterScale() { return worldHeight() * 0.2f / CHANDELIER.length; }
        private float lusterY()     { return MathUtils.lerp(worldHeight() * 1.2f, worldHeight() * 0.86f, Interpolation.pow2Out.apply(progress(0.1f, 0.8f))); }
        private float coupeScale()  { return worldHeight() * 0.055f / COUPE.length; }
        private float waltz()       { return progress(0.5f, 4.6f) * 4.1f * 360f; }

        @Override
        protected void step(float delta) {
            if (at(0.8f)) sparks(bossX(), lusterY(), 30, ICE, 400f, 9f);
            if (time > 0.5f && time < CLIMAX_PHASE && random.nextFloat() < delta * 25f) {   // la neige du bal
                Particle flake = particle(random.nextFloat() * worldWidth(), worldHeight() + 20f, 270f, 120f + 80f * random.nextFloat(), 3f, 10f);
                flake.color.set(SNOW);
                glows.add(flake);
            }
            for (int k = 0; k < 6; k++) if (at(1.3f + k * 0.12f)) sparks(coupeAtX(k), coupeAtY(k), 6, BUBBLE, 160f, 6f);
            if (at(CLIMAX_PHASE)) {
                rumble(0.5f, 10f);
                sparks(coupeAtX(5), coupeAtY(5), 60, ICE, 700f, 12f);
            }
        }

        /** La pyramide de six coupes (3, 2, 1), à droite de l'Impératrice. */
        private float coupeAtX(int k) {
            int row = k < 3 ? 0 : k < 5 ? 1 : 2, col = k < 3 ? k : k < 5 ? k - 3 : 0;
            return bossX() + bossWidth() * 1.15f + (col - (2 - row) / 2f) * 7.2f * coupeScale();
        }
        private float coupeAtY(int k) {
            int row = k < 3 ? 0 : k < 5 ? 1 : 2;
            return groundY() + worldHeight() * 0.05f + COUPE.length * coupeScale() * (0.5f + row);
        }

        @Override
        protected float decorLight() { return 0.5f + 0.25f * progress(0.6f, 1.0f); }

        private void dancers(Batch batch, boolean behind) {
            float appear = progress(0.5f, 1.0f) * (1f - progress(CLIMAX_PHASE + 1.6f, CLIMAX_PHASE + 2.2f));
            if (appear <= 0f) return;
            for (int k = 0; k < 4; k++) {
                float a = waltz() + k * 90f, depth = MathUtils.sinDeg(a);
                if ((depth > 0f) != behind) continue;
                float x = bossX() + MathUtils.cosDeg(a) * bossWidth() * 1.4f;
                float y = groundY() + worldHeight() * 0.07f + depth * worldHeight() * 0.04f;
                float scale = worldHeight() * (0.15f - 0.03f * depth) / DANCERS.length;
                float bob = 6f * MathUtils.sin(waltz() * MathUtils.degreesToRadians * 3f + k);
                draw(batch, dancers, x, y + DANCERS.length * scale / 2f + bob, scale, 8f * MathUtils.cosDeg(a * 3f), ICE, 0.45f * appear);
            }
        }

        @Override
        protected void back(Batch batch) {
            float rink = progress(0.4f, 1.0f);                                                // la patinoire
            rect(batch, ICE, 0.25f * rink, -MARGIN, groundY() - MARGIN, worldWidth() + 2f * MARGIN, worldHeight() * 0.06f + MARGIN);
            for (int k = 0; k < 5; k++) {
                float x = (k * 0.23f + time * 0.08f) % 1.2f * worldWidth() - worldWidth() * 0.1f;
                line(batch, WHITE, 0.35f * rink, x, groundY() + 6f, x + worldWidth() * 0.08f, groundY() + 6f + worldHeight() * 0.03f, 3f);
            }
            float lit = progress(0.7f, 1.0f), scale = lusterScale(), ly = lusterY();       // le lustre
            line(batch, hex("2a5a8a"), 1f, bossX(), ly + CHANDELIER.length * scale / 2f, bossX(), worldHeight() + MARGIN, 4f);
            draw(batch, chandelier, bossX(), ly, scale, 2f * MathUtils.sin(time * 1.5f), WHITE, 1f);
            for (int wick : WICKS) {
                float x = bossX() + (wick + 0.5f - CHANDELIER[0].length() / 2f) * scale, y = ly + (CHANDELIER.length / 2f - 2.5f) * scale;
                glow(batch, x, y, scale * 9f * pulse(13f + wick, 0.8f), hex("ffd27a"), 0.9f * lit);
            }
            if (lit > 0f) glow(batch, bossX(), ly, worldHeight() * 0.7f, ICE, 0.18f * lit);
            dancers(batch, true);
            float spin = progress(0.6f, CLIMAX_PHASE);                                        // elle tourne sur elle-même
            float turn = MathUtils.cos(spin * MathUtils.PI * 6f);
            poseTilt = 4f * MathUtils.sin(spin * MathUtils.PI * 6f) * (1f - progress(CLIMAX_PHASE - 0.2f, CLIMAX_PHASE));
            poseStretch = -0.12f * (1f - Math.abs(turn)) * (1f - progress(CLIMAX_PHASE - 0.2f, CLIMAX_PHASE));
            poseY = 18f * Interpolation.pow2Out.apply(progress(CLIMAX_PHASE - 0.2f, CLIMAX_PHASE + 0.3f));
            poseTint = 0.35f * spin;
            poseTintColor.set(ICE);
        }

        @Override
        protected void front(Batch batch) {
            dancers(batch, false);
            for (int k = 0; k < 6; k++) {                                                     // la pyramide de coupes
                float pop = Interpolation.swingOut.apply(progress(1.3f + k * 0.12f, 1.5f + k * 0.12f));
                if (pop <= 0f) continue;
                float x = coupeAtX(k), y = coupeAtY(k), s = coupeScale() * pop;
                draw(batch, coupe, x, y, s, 0f, WHITE, 1f);
                float frost = progress(CLIMAX_PHASE - 0.05f, CLIMAX_PHASE + 0.25f);
                if (frost > 0f) draw(batch, coupe, x, y, s, 0f, ICE, 0.6f * frost);
            }
            float frost = progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.3f);
            if (frost > 0f) {
                glow(batch, coupeAtX(4), coupeAtY(4), coupeScale() * 30f, ICE, 0.4f * frost * pulse(4f, 0.7f));
                eyes(batch, frost * pulse(6f, 0.7f), GLINT, LEFT_EYE, RIGHT_EYE);
            }
            flash(batch, ICE, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /**
     * « L'Impératrice du Givre a fondu » : elle ruisselle et s'affaisse dans une flaque qui s'étend ; sa vraie
     * couronne de stalactites glisse de sa tête, tombe dans l'eau en faisant des ronds, et reste là, seule.
     */
    static final class EmpressDeath extends BossScene {
        private static final float LOOSE = 2.4f, LAND = 3.1f;
        private final TextureRegion bare;
        private final Piece crown;
        private float crownStartX, crownStartY;
        private boolean loose;

        EmpressDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.IMPERATRICE_GIVRE, 0);
            int[][] full = PalacePortraits.pixels(EnemyKind.IMPERATRICE_GIVRE);
            int[][] rest = PalacePortraits.without(EnemyKind.IMPERATRICE_GIVRE, Prop.CROWN);
            crown = lift(full, rest);
            bare = pixels(rest);
        }

        @Override protected float textAt() { return LAND + 0.15f; }
        @Override protected void restart() { loose = false; }

        private float poolY() { return bossBottom() - 4f; }

        private float melt() { return Interpolation.pow2In.apply(progress(0.6f, 3.6f)); }


        @Override
        protected void step(float delta) {
            if (time > 0.6f && time < 3.6f && random.nextFloat() < delta * 24f) {             // les gouttes
                Particle drop = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth(), bossBottom() + bossHeight()
                    * random.nextFloat() * (1f - melt()) * 0.8f, 270f, 200f, 0.8f, 10f);
                drop.color.set(ICE);
                drop.gravity = 900f;
                glows.add(drop);
            }
            if (at(LOOSE) && crown != null) wear(bare);
            if (at(LAND)) { rumble(0.25f, 5f); sparks(bossX() + bossWidth() * 0.5f, poolY() + 10f, 30, ICE, 380f, 9f); }
        }

        @Override
        protected void back(Batch batch) {
            poseSink = 0.92f * melt();
            poseStretch = -0.3f * melt();
            poseTint = 0.5f * progress(0.4f, 1.4f);
            poseTintColor.set(WATER);
            poseAlpha = 1f - progress(3.2f, 3.7f);
            showBoss = time < 3.7f;
            float pool = progress(0.8f, 3.6f);
            if (pool > 0f) {                                                                 // la flaque
                float w = bossWidth() * 2.2f * pool;
                rect(batch, hex("2a5a8a"), 0.8f, bossX() - w / 2f - 6f, poolY() - 12f, w + 12f, 30f);
                rect(batch, WATER, 0.85f, bossX() - w / 2f, poolY() - 8f, w, 22f);
                rect(batch, ICE, 0.5f, bossX() - w / 2f + 20f, poolY() + 6f, w * 0.4f, 5f);
                glow(batch, bossX(), poolY(), w, ICE, 0.25f * pool);
            }
        }

        @Override
        protected void front(Batch batch) {
            if (crown == null) return;
            float landX = bossX() + bossWidth() * 0.5f, landY = poolY() + crown.height * bossScale() * 0.35f;
            if (time < LOOSE) {
                attached(batch, crown, 1f);
            } else {
                if (!loose) {                                                                 // elle glisse d'où elle était
                    loose = true;
                    crownStartX = pieceX(crown);
                    crownStartY = portraitY(crown.top + crown.height / 2f);
                }
                float fall = progress(LOOSE, LAND), k = Interpolation.bounceOut.apply(fall);
                float x = MathUtils.lerp(crownStartX, landX, Interpolation.pow2Out.apply(fall));
                float y = MathUtils.lerp(crownStartY, landY, k);
                draw(batch, crown.region, x, y, bossScale(), -18f * fall, WHITE, 1f);
                if (time > LAND) glow(batch, x, y, crown.width * bossScale() * 1.6f, GLINT, 0.4f * pulse(3f, 0.5f));
            }
            for (int r = 0; r < 3; r++) wave(batch, LAND + r * 0.35f, 1.2f, landX, poolY() + 4f, ICE, bossWidth() * 1.4f);
        }
    }

    // -------------------------------------------------------------------------
    // Le Yéti
    // -------------------------------------------------------------------------

    /**
     * « Souffle polaire : Gel ! » : il se frappe la poitrine, gonfle ses poumons, puis souffle une tempête de glace
     * droit vers nous ; le givre pousse en fougères depuis les coins de l'écran et des stalactites tombent du haut ;
     * ses yeux bleus s'allument.
     */
    static final class YetiPhase extends BossScene {
        private static final float BLOW = 1.75f;
        private final TextureRegion icicle;

        YetiPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.YETI, 2);
            icicle = art("kiw", ICICLE_COLORS, ICICLE);
        }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 4; k++) if (at(0.45f + k * 0.28f)) rumble(0.12f, 7f);
            if (time > BLOW && time < CLIMAX_PHASE + 0.9f) {
                for (int n = 0; n < 4; n++) if (random.nextFloat() < delta * 45f) {             // le souffle, vers nous
                    float dir = -90f + (random.nextFloat() - 0.5f) * 150f;
                    Particle wind = particle(portraitX(17.5f), portraitY(22f), dir, 500f + 700f * random.nextFloat(), 1.1f,
                        70f + 140f * random.nextFloat());
                    wind.color.set(SNOW.r, SNOW.g, SNOW.b, 0.6f);
                    glows.add(wind);
                }
            }
            if (at(BLOW)) rumble(1.2f, 6f);
            if (at(CLIMAX_PHASE)) rumble(0.5f, 14f);
        }

        @Override
        protected float decorLight() { return 0.62f - 0.15f * progress(BLOW, CLIMAX_PHASE); }

        @Override
        protected void back(Batch batch) {
            float beat = 0f;
            for (int k = 0; k < 4; k++) beat = Math.max(beat, gesture(0.4f + k * 0.28f, 0.52f + k * 0.28f));
            float inhale = progress(1.3f, BLOW) * (1f - progress(BLOW, BLOW + 0.15f));
            poseStretch = -0.06f * beat + 0.1f * inhale - 0.04f * gesture(BLOW, CLIMAX_PHASE + 0.6f);
            poseScale = 1f + 0.06f * gesture(BLOW, CLIMAX_PHASE + 1.0f);
            poseTint = 0.3f * progress(BLOW, CLIMAX_PHASE);
            poseTintColor.set(ICE);
        }

        @Override
        protected void front(Batch batch) {
            float blow = gesture(BLOW, CLIMAX_PHASE + 1.2f);
            if (blow > 0f) {                                                                  // le cône de vent glacé
                for (int k = 0; k < 9; k++) {
                    float x = portraitX(17.5f) + (k - 4) * worldWidth() * 0.09f * progress(BLOW, BLOW + 0.4f);
                    float y0 = portraitY(22f), drift = (time * 1800f + k * 210f) % (worldHeight() * 0.7f);
                    line(batch, WHITE, 0.4f * blow, MathUtils.lerp(portraitX(17.5f), x, drift / (worldHeight() * 0.7f)), y0 - drift,
                        MathUtils.lerp(portraitX(17.5f), x, (drift + 120f) / (worldHeight() * 0.7f)), y0 - drift - 120f, 4f);
                }
                fill(batch, ICE, 0.12f * blow);
            }
            float frost = Interpolation.pow2Out.apply(progress(BLOW + 0.2f, CLIMAX_PHASE + 0.4f));   // le givre aux quatre coins
            if (frost > 0f) {
                float[][] corners = {{0f, 0f, 45f}, {worldWidth(), 0f, 135f}, {0f, worldHeight(), -45f}, {worldWidth(), worldHeight(), -135f}};
                for (int c = 0; c < 4; c++) {
                    glow(batch, corners[c][0], corners[c][1], worldHeight() * 0.9f * frost, SNOW, 0.55f * frost);
                    for (int b = 0; b < 7; b++) {                                             // des fougères de givre, en plumes
                        float angle = corners[c][2] + (b - 3f) * 12f, length = worldHeight() * (0.14f + 0.03f * (b % 3)) * frost;
                        float x0 = corners[c][0], y0 = corners[c][1];
                        float x1 = x0 + MathUtils.cosDeg(angle) * length, y1 = y0 + MathUtils.sinDeg(angle) * length;
                        line(batch, WHITE, 0.8f, x0, y0, x1, y1, 3f);
                        for (int f = 1; f <= 5; f++) {
                            float along = f / 6f, bx = MathUtils.lerp(x0, x1, along), by = MathUtils.lerp(y0, y1, along);
                            float barb = length * 0.16f * (1f - along * 0.6f);
                            for (int side = -1; side <= 1; side += 2) {
                                float a = angle + side * 40f;
                                line(batch, ICE, 0.75f, bx, by, bx + MathUtils.cosDeg(a) * barb, by + MathUtils.sinDeg(a) * barb, 2f);
                            }
                        }
                    }
                }
            }
            float scale = worldHeight() * 0.11f / ICICLE.length;                              // les stalactites qui poussent du haut
            for (int k = 0; k < 11; k++) {
                float grow = Interpolation.pow2Out.apply(progress(BLOW + 0.3f + (k % 4) * 0.08f, CLIMAX_PHASE + 0.1f));
                if (grow <= 0f) continue;
                float x = worldWidth() * (k + 0.5f) / 11f, sy = scale * (0.6f + 0.4f * ((k * 7) % 3) / 2f) * grow;
                stretch(batch, icicle, x, worldHeight() - ICICLE.length * sy / 2f + 2f, scale, sy, 0f, WHITE, 1f);
            }
            eyes(batch, progress(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.2f) * pulse(7f, 0.7f), GLINT, YETI_LEFT_EYE, YETI_RIGHT_EYE);
            flash(batch, SNOW, CLIMAX_PHASE, 0.3f, 0.45f);
        }
    }

    /**
     * « Le Yéti dort sous la neige » : il bâille, ses paupières tombent, il se couche en boule ; la nuit tombe sur le
     * Palace et la neige le recouvre en un gros tas d'où dépassent ses cornes de glace ; le tas se soulève au rythme
     * de son souffle et de petites bouffées de buée montent.
     */
    static final class YetiDeath extends BossScene {
        private static final float LIE = 1.0f, COVERED = 2.8f;
        private final TextureRegion heap, lid;

        YetiDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.YETI, 0);
            heap = art("owsd", SNOW_COLORS, snowHeap(48, 18));
            lid = art("fk", new Color[] {hex("7a8aa8"), hex("22222e")}, "fff", "kkk");     // une paupière fermée, sa ligne de cils
        }

        private float heapScale()    { return bossWidth() * 1.9f / 48f; }
        private float breath()       { return time > COVERED ? MathUtils.sin((time - COVERED) * 3.2f) : 0f; }

        @Override
        protected void step(float delta) {
            if (time > 0.4f && random.nextFloat() < delta * 40f) {                            // la neige qui tombe doucement
                Particle flake = particle(random.nextFloat() * worldWidth(), worldHeight() + 20f, 265f + 10f * random.nextFloat(),
                    90f + 70f * random.nextFloat(), 4f, 8f + 8f * random.nextFloat());
                flake.color.set(SNOW);
                glows.add(flake);
            }
            for (int k = 0; k < 3; k++) {
                if (at(COVERED + 0.4f + k * 0.98f)) {                                         // la buée du dormeur
                    for (int p = 0; p < 6; p++) {
                        Particle puff = particle(bossX() - bossWidth() * 0.1f, heapTop() - 10f, 80f + 20f * random.nextFloat(),
                            60f + 40f * random.nextFloat(), 1.4f, 26f + 20f * random.nextFloat());
                        puff.color.set(SNOW.r, SNOW.g, SNOW.b, 0.5f);
                        glows.add(puff);
                    }
                }
            }
            if (at(CLIMAX_DEATH)) rumble(0.3f, 4f);
        }

        private float heapTop() {
            float grow = Interpolation.pow2Out.apply(progress(LIE, COVERED));
            return groundY() + 18f * heapScale() * grow * (1f + 0.025f * breath());
        }

        @Override
        protected float decorLight() { return 0.62f - 0.3f * progress(0.8f, COVERED); }

        @Override
        protected void back(Batch batch) {
            fill(batch, NIGHT, 0.3f * progress(0.8f, COVERED));                              // la nuit tombe
            float yawn = gesture(0.2f, 0.9f);
            float lie = Interpolation.pow2.apply(progress(LIE, COVERED - 0.4f));
            poseStretch = 0.08f * yawn - 0.1f * lie;
            poseTilt = 14f * lie;
            poseSink = 0.36f * lie;
            poseX = -bossWidth() * 0.1f * lie;
            poseTint = 0.25f * progress(0.8f, COVERED);
            poseTintColor.set(NIGHT);
        }

        @Override
        protected void front(Batch batch) {
            float lids = progress(0.5f, 1.0f);                                                 // ses paupières tombent sur ses yeux
            if (lids > 0f && time < COVERED) {
                for (float[] eye : new float[][] {YETI_LEFT_EYE, YETI_RIGHT_EYE}) onPortrait(batch, lid, eye[0] - 1.5f, eye[1] - 1f, 1f, lids);
            }
            float grow = Interpolation.pow2Out.apply(progress(LIE, COVERED));
            if (grow > 0f) {
                float scale = heapScale();
                stretch(batch, heap, bossX(), groundY() - 6f + 18f * scale * grow * (1f + 0.025f * breath()) / 2f, scale,
                    scale * grow * (1f + 0.025f * breath()), 0f, WHITE, 1f);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Le Prince des Neiges
    // -------------------------------------------------------------------------

    /**
     * « Les portes du bal se ferment : Clé ! » : derrière lui, les deux grands battants laqués de blanc et d'or se
     * referment et claquent ; une lourde barre d'or glisse en travers et verrouille le bal ; ses yeux s'allument.
     */
    static final class PrincePhase extends BossScene {
        private static final float SLAM = 1.5f, LOCK = 1.95f;
        private final TextureRegion leaf;

        PrincePhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.PRINCE_NEIGES, 2);
            leaf = art("owsgif", DOOR_COLORS, doorLeaf());
        }

        private float doorH()   { return worldHeight() * 0.74f; }
        private float leafW()   { return doorH() * 20f / 52f; }
        private float doorBottom() { return groundY() - 4f; }

        @Override
        protected void step(float delta) {
            if (at(SLAM)) {
                rumble(0.5f, 12f);
                sparks(bossX(), doorBottom() + doorH() * 0.5f, 40, ICE, 600f, 12f);
                sparks(bossX(), doorBottom() + 10f, 30, SNOW, 500f, 10f);
            }
            if (at(LOCK)) { rumble(0.25f, 8f); sparks(bossX(), doorBottom() + doorH() * 0.5f, 26, GOLD, 500f, 9f); }
            if (at(CLIMAX_PHASE)) rumble(0.4f, 8f);
        }

        @Override
        protected float decorLight() { return 0.62f - 0.2f * progress(SLAM, SLAM + 0.4f); }

        @Override
        protected void back(Batch batch) {
            float h = doorH(), w = leafW(), y = doorBottom();
            rect(batch, hex("0a1020"), 1f, bossX() - w - 26f, y, 2f * w + 52f, h + 26f);    // l'encadrement de la porte
            rect(batch, GOLD, 1f, bossX() - w - 18f, y, 2f * w + 36f, h + 18f);
            rect(batch, hex("ffe08a"), 1f, bossX() - w - 18f, y + h + 10f, 2f * w + 36f, 8f);
            rect(batch, hex("0a1020"), 1f, bossX() - w, y, 2f * w, h);
            glow(batch, bossX(), y + h * 0.4f, w * 2.2f, hex("ffd27a"), 0.5f * (1f - progress(SLAM - 0.3f, SLAM)));   // la lumière du bal, dehors
            float close = Interpolation.pow3In.apply(progress(0.4f, SLAM)), bounce = 1f;
            if (time > SLAM) bounce = 1f - 0.04f * MathUtils.sin((time - SLAM) * 30f) * (float) Math.exp(-(time - SLAM) * 8f);
            float open = Math.max(0.12f, close * bounce);                                     // un battant vu de face : sa largeur
            float scale = h / 52f;
            batch.setColor(WHITE);
            batch.draw(leaf, bossX() - w, y, w * open, h);                                     // le battant gauche, charnière à gauche
            batch.draw(leaf, bossX() + w, y, -w * open, h);                                   // le battant droit, en miroir
            float bar = Interpolation.pow2Out.apply(progress(SLAM + 0.15f, LOCK));          // la barre d'or qui verrouille
            if (bar > 0f) {
                float bw = 2f * w + 80f, bx = MathUtils.lerp(bossX() + w + 40f, bossX() - bw / 2f, bar), by = y + h * 0.5f;
                rect(batch, hex("1a1018"), 1f, bx - 4f, by - 4f * scale - 4f, bw + 8f, 8f * scale + 8f);
                rect(batch, GOLD, 1f, bx, by - 4f * scale, bw, 8f * scale);
                rect(batch, hex("ffe08a"), 1f, bx, by + 1f * scale, bw, 2f * scale);
            }
            poseY = 16f * progress(LOCK, CLIMAX_PHASE + 0.2f);
            poseTint = 0.25f * progress(LOCK, CLIMAX_PHASE);
            poseTintColor.set(ICE);
        }

        @Override
        protected void front(Batch batch) {
            flash(batch, WHITE, SLAM, 0.1f, 0.3f);
            eyes(batch, progress(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.2f) * pulse(6f, 0.7f), GLINT, LEFT_EYE, RIGHT_EYE);
            flash(batch, ICE, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /**
     * « Le bal du Prince est fini » : sa coupe (la vraie, celle de sa main) lui échappe et se brise ; les bougies du
     * lustre s'éteignent une à une ; il salue une dernière fois, puis s'effondre en un tas de neige ; sa couronne
     * glisse de sa tête et se pose au sommet du tas.
     */
    static final class PrinceDeath extends BossScene {
        private static final float DROP = 0.5f, BREAK = 1.05f, CRUMBLE = 2.7f, CROWN_LAND = 4.0f;
        private final TextureRegion chandelier, heap, noCup, bare;
        private final Piece cup, crown;
        private float crownStartX, crownStartY;
        private boolean loose;

        PrinceDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.PRINCE_NEIGES, 0);
            chandelier = art("kiws", CHANDELIER_COLORS, CHANDELIER);
            heap = art("owsd", SNOW_COLORS, snowHeap(36, 16));
            int[][] full = PalacePortraits.pixels(EnemyKind.PRINCE_NEIGES);
            int[][] rest = PalacePortraits.without(EnemyKind.PRINCE_NEIGES, Prop.CUP);
            int[][] none = PalacePortraits.without(EnemyKind.PRINCE_NEIGES, Prop.CUP, Prop.CROWN);
            cup = lift(full, rest);
            crown = lift(rest, none);
            noCup = pixels(rest);
            bare = pixels(none);
        }

        @Override protected float textAt() { return 3.2f; }
        @Override protected void restart() { loose = false; }

        private float out(int k)        { return 1.4f + k * 0.3f; }
        private float lusterScale()     { return worldHeight() * 0.2f / CHANDELIER.length; }
        private float lusterY()         { return worldHeight() * 0.86f; }
        private float heapScale()       { return bossWidth() * 1.3f / 36f; }
        private float heapGrow()        { return Interpolation.pow2Out.apply(progress(CRUMBLE, CRUMBLE + 1.2f)); }
        private float heapTop()         { return groundY() - 4f + 16f * heapScale() * heapGrow(); }


        @Override
        protected void step(float delta) {
            if (at(DROP)) wear(noCup);
            if (at(BREAK)) {
                rumble(0.15f, 5f);
                sparks(cup == null ? bossX() : pieceX(cup), groundY() + 8f, 34, ICE, 520f, 9f);
                sparks(cup == null ? bossX() : pieceX(cup), groundY() + 8f, 14, BUBBLE, 300f, 7f);
            }
            for (int k = 0; k < WICKS.length; k++) {
                if (at(out(k))) {                                                              // une bougie du lustre s'éteint
                    Particle puff = particle(wickX(k), wickY(), 90f, 60f, 1.2f, 18f);
                    puff.color.set(SMOKE);
                    glows.add(puff);
                }
            }
            if (at(CRUMBLE) && crown != null) wear(bare);
            if (time > CRUMBLE && time < CRUMBLE + 1.3f && random.nextFloat() < delta * 50f) {   // il part en neige
                Particle flake = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth(), heapTop() + random.nextFloat() * 60f,
                    random.nextFloat() * 180f, 120f + 120f * random.nextFloat(), 0.9f, 10f + 8f * random.nextFloat());
                flake.color.set(SNOW);
                glows.add(flake);
            }
            if (at(CROWN_LAND)) { rumble(0.15f, 3f); sparks(bossX(), heapTop() + 20f, 14, GOLD, 240f, 7f); }
        }

        private float wickX(int k) { return bossX() + (WICKS[k] + 0.5f - CHANDELIER[0].length() / 2f) * lusterScale(); }
        private float wickY()      { return lusterY() + (CHANDELIER.length / 2f - 2.5f) * lusterScale(); }

        @Override
        protected float decorLight() {
            int lit = 0;
            for (int k = 0; k < WICKS.length; k++) if (time < out(k)) lit++;
            return 0.3f + 0.08f * lit;
        }

        @Override
        protected void back(Batch batch) {
            float scale = lusterScale();                                                      // le lustre du bal
            line(batch, hex("2a5a8a"), 1f, bossX(), lusterY() + CHANDELIER.length * scale / 2f, bossX(), worldHeight() + MARGIN, 4f);
            draw(batch, chandelier, bossX(), lusterY(), scale, 0f, WHITE, 1f);
            for (int k = 0; k < WICKS.length; k++) {
                float on = 1f - progress(out(k), out(k) + 0.12f);
                if (on > 0f) glow(batch, wickX(k), wickY(), scale * 9f * pulse(13f + k, 0.8f), hex("ffd27a"), 0.9f * on);
            }
            float bow = gesture(1.7f, CRUMBLE);
            poseTilt = 10f * bow;
            poseY = -12f * bow;
            poseSink = Interpolation.pow2In.apply(progress(CRUMBLE, CRUMBLE + 1.2f));
            poseStretch = -0.15f * poseSink;
            poseTint = 0.5f * progress(CRUMBLE - 0.3f, CRUMBLE + 0.4f);
            poseTintColor.set(SNOW);
            showBoss = poseSink < 0.98f;
        }

        @Override
        protected void front(Batch batch) {
            if (cup != null) {                                                                // la coupe qui lui échappe
                if (time < DROP) attached(batch, cup, 1f);
                else if (time < BREAK) {
                    float fall = Interpolation.pow2In.apply(progress(DROP, BREAK));
                    draw(batch, cup.region, pieceX(cup) + 30f * fall, MathUtils.lerp(pieceY(cup), groundY() + 10f, fall), bossScale(), -120f * fall,
                        WHITE, 1f);
                }
            }
            if (crown != null && time < CRUMBLE) attached(batch, crown, 1f);
            float grow = heapGrow();
            if (grow > 0f) {
                float scale = heapScale();
                stretch(batch, heap, bossX(), groundY() - 4f + 16f * scale * grow / 2f, scale, scale * grow, 0f, WHITE, 1f);
            }
            if (crown != null && time >= CRUMBLE) {                                           // sa couronne, posée sur le tas
                if (!loose) {
                    loose = true;
                    crownStartX = pieceX(crown);
                    crownStartY = portraitY(crown.top + crown.height / 2f);
                }
                float k = progress(CRUMBLE, CROWN_LAND), land = Interpolation.bounceOut.apply(k);
                float x = MathUtils.lerp(crownStartX, bossX(), Interpolation.pow2Out.apply(k)) + MathUtils.sin(k * MathUtils.PI) * bossWidth() * 0.45f;
                float y = MathUtils.lerp(crownStartY, heapTop() + crown.height * bossScale() * 0.4f, land);   // elle glisse sur le côté
                draw(batch, crown.region, x, y, bossScale(), 10f * k, WHITE, 1f);
            }
            flash(batch, SNOW, CRUMBLE, 0.25f, 0.3f);
        }
    }
}
